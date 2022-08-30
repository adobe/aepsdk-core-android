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

package com.adobe.marketing.mobile.internal.configuration

import androidx.annotation.VisibleForTesting
import com.adobe.marketing.mobile.Log
import com.adobe.marketing.mobile.internal.utility.FileUtils
import com.adobe.marketing.mobile.services.CacheFileService
import com.adobe.marketing.mobile.services.Networking
import com.adobe.marketing.mobile.utils.remotedownload.RemoteDownloader
import java.io.File

/**
 * Responsible for downloading the launch rules zip using [RemoteDownloader] and extracting and processing
 * them. Uses the [ZipFileMetadataProvider] for creating the zip file metadata to facilitate partial and conditional
 * downloads.
 */
internal class ConfigurationRulesDownloader {
    companion object {
        const val LOG_TAG = "ConfigurationRulesDownloader"
    }

    private val cacheFileService: CacheFileService
    private val metadataProvider: ZipFileMetadataProvider
    private val remoteDownloader: RemoteDownloader

    constructor(
        networkService: Networking,
        cacheFileService: CacheFileService,
        metadataProvider: ZipFileMetadataProvider
    ) : this(cacheFileService, metadataProvider, RemoteDownloader(networkService, cacheFileService))

    @VisibleForTesting
    internal constructor(
        cacheFileService: CacheFileService,
        metadataProvider: ZipFileMetadataProvider,
        remoteDownloader: RemoteDownloader
    ) {
        this.cacheFileService = cacheFileService
        this.metadataProvider = metadataProvider
        this.remoteDownloader = remoteDownloader
    }

    /**
     * Triggers the download of the launch rules zip from [url] into [cacheSubDirectory] and provides
     * the extracted zip via the [completionCallback]
     *
     * @param url the url from which the rules must be downloaded from
     * @param cacheSubDirectory optional directory within the default directory used by [cacheFileService]
     *        into which the rules must be placed
     * @param completionCallback the callback that is invoked with the extracted zip file
     *        (or null if extraction/download fails)
     */
    fun download(
        url: String,
        cacheSubDirectory: String,
        completionCallback: (file: File?) -> Unit
    ) {
        remoteDownloader.download(url, cacheSubDirectory, metadataProvider) { downloadResult ->
            val downloadedZip = downloadResult.data
            val zipFile: File? = processRulesZip(url, cacheSubDirectory, downloadedZip)
            completionCallback.invoke(zipFile)
        }
    }

    /**
     * Responsible for extracting the downloaded zip file into the cache sub directory.
     * Additionally creates metadata to facilitate partial and conditional downloads.
     *
     * @param url url the url from which rules were downloaded
     * @param directory optional sub-directory of the default cache directory used by [cacheFileService]
     *        into which the rules must be placed
     * @param downloadedZipFile the zip file that is to be extracted
     *
     * @return a [File] representing the extracted contents of [downloadedZipFile] if successful,
     *         null if extraction of zip fails
     */
    private fun processRulesZip(url: String, directory: String, downloadedZipFile: File?): File? {
        Log.trace(
            ConfigurationExtension.TAG,
            LOG_TAG,
            "Processing Rules bundle."
        )

        if (downloadedZipFile == null) {
            Log.trace(
                ConfigurationExtension.TAG,
                LOG_TAG,
                "Downloaded rules zip file is null."
            )
            return null
        }

        val processedFile = if (downloadedZipFile.isDirectory) {
            downloadedZipFile
        } else {
            val outputPath: String? = cacheFileService.getBaseFilePath(url, directory)
            if (outputPath == null) {
                Log.trace(
                    ConfigurationExtension.TAG,
                    LOG_TAG,
                    "Failed to create output path for extracting rules bundle."
                )
                null
            } else {
                val extracted = FileUtils.extractFromZip(downloadedZipFile, outputPath)
                if (extracted) {
                    // Create and save metadata with the original zip content and not the extracted one
                    val lastModifiedDate: Long = parseLastModified(downloadedZipFile)
                    metadataProvider.createMetadata(
                        outputPath,
                        downloadedZipFile.length(),
                        lastModifiedDate
                    )
                    downloadedZipFile.delete()
                    Log.trace(
                        ConfigurationExtension.TAG,
                        LOG_TAG,
                        "Successfully extracted rules zip."
                    )

                    File(outputPath)
                } else {
                    Log.trace(
                        ConfigurationExtension.TAG,
                        LOG_TAG,
                        "Failed to extract downloaded rules zip."
                    )
                    downloadedZipFile.delete()
                    null
                }
            }
        }

        return processedFile
    }

    private fun parseLastModified(downloadedZipFile: File): Long {
        return try {
            val epochString = cacheFileService.getMetadata(
                CacheFileService.METADATA_KEY_LAST_MODIFIED_EPOCH,
                downloadedZipFile.path
            )
            epochString?.toLong() ?: 0L
        } catch (nfe: NumberFormatException) {
            0L
        }
    }
}
