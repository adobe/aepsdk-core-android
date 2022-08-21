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

package com.adobe.marketing.mobile.utils

import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.internal.utility.StringUtils
import com.adobe.marketing.mobile.services.CacheFileService
import com.adobe.marketing.mobile.services.Networking
import java.io.File

/**
 * Facilitates downloading content from a URL into a local cache directory.
 * Stores downloaded content in to a directory governed by [CacheFileService].
 *
 * Currently, triggers to [download] are independent, successive calls.
 * This class is intentionally final and components are required to use this via composition;
 * This allows easier path for components using this class to add queuing logic for jobs internally,
 * maintaining compatibility when this class adds queuing logic in the future, and facilitating
 * cleaner tests in composing components.
 */
class RemoteDownloader(
    private val networkService: Networking,
    private val cacheFileService: CacheFileService
) {

    companion object {
        private const val TAG = "RemoteDownloader"
    }

    enum class Reason {
        INVALID_URL,
        RESPONSE_PROCESSING_FAILED,
        CANNOT_WRITE_TO_CACHE_DIR,
        NOT_MODIFIED,
        NO_DATA,
        SUCCESS
    }

    data class RemoteDownloadResult(val data: File?, val reason: Reason)

    /**
     * Represents a component that provides metadata about file content.
     */
    interface MetadataProvider {
        companion object MetadataKeys {
            internal const val HTTP_HEADER_IF_MODIFIED_SINCE = "If-Modified-Since"
            internal const val HTTP_HEADER_IF_RANGE = "If-Range"
            internal const val HTTP_HEADER_RANGE = "Range"
            internal const val HTTP_HEADER_LAST_MODIFIED = "Last-Modified"
            internal const val ETAG = "ETag"
        }

        /**
         * Retrieves metadata for the [file] provided for the purpose of
         * conditionally fetching content from the remote url.
         *
         * @param file the [File] for which metadata is needed
         * @return the metadata of the [file] if it is valid;
         *         empty map if no metadata is needed,
         *         null if metadata cannot be computed
         */
        fun getMetadata(file: File): Map<String, String>?
    }

    /**
     * Allows easier mocking during tests.
     */
    private val downloadJobSupplier: (Networking, CacheFileService, url: String, downloadSubDirectory: String?, MetadataProvider) -> RemoteDownloadJob =
        { networking, fileCacheService, url, downloadDirectory, metadataProvider ->
            RemoteDownloadJob(
                networking,
                fileCacheService,
                url,
                downloadDirectory,
                metadataProvider
            )
        }

    /**
     * Triggers content download from [url] to [downloadSubDirectory] by using the [metadataProvider]
     * to facilitate any partial downloads.
     * Note that currently, triggers to download are independent of successive calls.
     * [download] when called with the same url without waiting for a result via [completionCallback]
     * will cause ambiguous results.
     *
     * @param url the url from which the content must be downloaded from
     * @param downloadSubDirectory optional directory within the default cache directory used by [cacheFileService]
     *        into which the rules must be placed
     * @param metadataProvider the metadata provider that will be used to fetch conditional headers when making a
     *        download request
     * @param completionCallback the callback to notify about downloaded result
     */
    fun download(
        url: String,
        downloadSubDirectory: String?,
        metadataProvider: MetadataProvider,
        completionCallback: (RemoteDownloadResult) -> Unit
    ) {
        if (!StringUtils.stringIsUrl(url)) {
            MobileCore.log(
                LoggingMode.WARNING,
                TAG,
                "Invalid URL: ($url). Contents cannot be downloaded."
            )
            completionCallback.invoke(RemoteDownloadResult(null, Reason.INVALID_URL))
            return
        }

        val remoteDownloadJob = downloadJobSupplier(
            networkService,
            cacheFileService,
            url,
            downloadSubDirectory,
            metadataProvider
        )
        remoteDownloadJob.download(completionCallback)
    }
}
