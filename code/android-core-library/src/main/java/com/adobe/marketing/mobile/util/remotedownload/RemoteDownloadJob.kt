/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
 */
package com.adobe.marketing.mobile.util.remotedownload

import com.adobe.marketing.mobile.internal.util.FileUtils
import com.adobe.marketing.mobile.internal.util.StringUtils
import com.adobe.marketing.mobile.launch.rulesengine.LaunchRulesEngineConstants
import com.adobe.marketing.mobile.services.CacheFileService
import com.adobe.marketing.mobile.services.HttpConnecting
import com.adobe.marketing.mobile.services.HttpMethod
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.NetworkCallback
import com.adobe.marketing.mobile.services.NetworkRequest
import com.adobe.marketing.mobile.services.Networking
import com.adobe.marketing.mobile.util.TimeUtils
import com.adobe.marketing.mobile.util.remotedownload.DownloadResult.Reason
import com.adobe.marketing.mobile.util.remotedownload.MetadataProvider.ETAG
import com.adobe.marketing.mobile.util.remotedownload.MetadataProvider.HTTP_HEADER_LAST_MODIFIED
import java.io.File
import java.net.HttpURLConnection
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Facilitates downloading from a URL
 */
internal class RemoteDownloadJob(
    private val networkService: Networking,
    private val cacheFileService: CacheFileService,
    private val url: String,
    private val downloadDirectory: String?,
    private val metadataProvider: MetadataProvider
) {
    companion object {
        private const val TAG = "RemoteDownloadJob"
        internal const val DEFAULT_CONNECTION_TIMEOUT_MS = 10000
        internal const val DEFAULT_READ_TIMEOUT_MS = 10000
        internal const val HTTP_REQUESTED_RANGE_NOT_SATISFIABLE = 416
    }

    /**
     * Triggers content download from [url] into [downloadDirectory] inside the cache directory
     * maintained by [cacheFileService]
     *
     * @param completionCallback the callback that should be notified with the download result
     */
    internal fun download(completionCallback: (DownloadResult) -> Unit) {
        if (!StringUtils.stringIsUrl(url)) {
            Log.debug(
                LaunchRulesEngineConstants.LOG_TAG,
                TAG,
                "Invalid URL: ($url). Contents cannot be downloaded."
            )
            completionCallback.invoke(DownloadResult(null, Reason.INVALID_URL))
            return
        }

        val cachedFile = cacheFileService.getCacheFile(url, downloadDirectory, false)
        val params = mutableMapOf<String, String>()

        cachedFile?.let {
            // Fetch metadata params for the existing cache file
            metadataProvider.getMetadata(cachedFile)?.let { params.putAll(it) }
        }

        val networkRequest = NetworkRequest(
            url,
            HttpMethod.GET,
            null,
            params, DEFAULT_CONNECTION_TIMEOUT_MS, DEFAULT_READ_TIMEOUT_MS
        )
        val networkCallback =
            NetworkCallback { response ->

                if (response == null) {
                    completionCallback.invoke(DownloadResult(null, Reason.NO_DATA))
                } else {
                    val downloadResult: DownloadResult = handleDownloadResponse(response, cachedFile)
                    completionCallback.invoke(downloadResult)
                }
            }

        networkService.connectAsync(networkRequest, networkCallback)
    }

    /**
     * Handles the response from download request.
     *
     * @param response the response of the download request
     * @param cacheFile previously cached file (if any) to which responses need to be appended (if possible)
     * @return the [RemoteDownloadResult] with file that has been saved into the cache if successful, null otherwise
     *         along with the reason for such a result.
     */
    private fun handleDownloadResponse(
        response: HttpConnecting,
        cacheFile: File?
    ): DownloadResult {
        return when (response.responseCode) {

            HttpURLConnection.HTTP_OK -> {
                saveContent(response, downloadDirectory, null)
            }

            HttpURLConnection.HTTP_PARTIAL -> {
                saveContent(response, downloadDirectory, cacheFile)
            }

            HttpURLConnection.HTTP_NOT_MODIFIED -> {
                DownloadResult(cacheFile, Reason.NOT_MODIFIED)
            }

            else -> DownloadResult(null, Reason.NO_DATA)
        }
    }

    /**
     * Saves the network response into a file locally. If a file has been created for the [url], an attempt
     * to append is made if necessary/
     *
     * @param connection the [HttpConnecting] from which the response is to be read
     * @param directory the optional sub directory inside the default cache directory used by [CacheFileService]
     *        into which the response needs to be downloaded
     * @param cacheFile an optional cache file that is previously associated with the url that this [RemoteDownloadJob]
     *        is downloading from
     * @return the [DownloadResult] with file that has been saved into the cache if successful, null otherwise
     *         along with the reason for such a result.
     */
    private fun saveContent(
        connection: HttpConnecting,
        directory: String?,
        cacheFile: File?
    ): DownloadResult {

        val workFile: File? = if (cacheFile == null) {
            // If no cache file is provided, clear the cache directory for the url and
            // then create a new cache work file.
            cacheFileService.deleteCacheFile(url, directory)

            val lastModifiedHeader: String =
                connection.getResponsePropertyValue(HTTP_HEADER_LAST_MODIFIED)
            val lastModifiedDate: Date =
                TimeUtils.parseRFC2822Date(
                    lastModifiedHeader,
                    TimeZone.getTimeZone("GMT"),
                    Locale.US
                )
                    ?: Date()
            val etag = connection.getResponsePropertyValue(ETAG)

            val cacheFileMetadata = mutableMapOf<String, String>()
            etag?.let { cacheFileMetadata[CacheFileService.METADATA_KEY_ETAG] = etag }
            cacheFileMetadata[CacheFileService.METADATA_KEY_LAST_MODIFIED_EPOCH] = lastModifiedDate.time.toString()

            cacheFileService.createCacheFile(url, cacheFileMetadata, directory)
        } else {
            // Use the cache file to append the new content
            cacheFile
        }

        if (workFile == null) {
            Log.debug(
                LaunchRulesEngineConstants.LOG_TAG,
                TAG,
                "Cannot find/create cache file on disk. Will not download from url ($url)"
            )
            return DownloadResult(null, Reason.CANNOT_WRITE_TO_CACHE_DIR)
        }

        val append = (cacheFile != null)
        val result = FileUtils.readInputStreamIntoFile(workFile, connection.inputStream, append)

        return if (result) {
            val completedFile = cacheFileService.markComplete(workFile)
            if (completedFile == null) {
                Log.debug(
                    LaunchRulesEngineConstants.LOG_TAG,
                    TAG,
                    "Cached Files - Could not save cached file ($url)"
                )
                DownloadResult(null, Reason.CANNOT_WRITE_TO_CACHE_DIR)
            } else {
                Log.debug(
                    LaunchRulesEngineConstants.LOG_TAG,
                    TAG,
                    "Cached Files -Successfully downloaded content from ($url) into ${completedFile.absolutePath}"
                )
                DownloadResult(completedFile, Reason.SUCCESS)
            }
        } else {
            DownloadResult(null, Reason.RESPONSE_PROCESSING_FAILED)
        }
    }
}
