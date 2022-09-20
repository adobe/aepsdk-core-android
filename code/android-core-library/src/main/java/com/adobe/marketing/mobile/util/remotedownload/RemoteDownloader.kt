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

import com.adobe.marketing.mobile.internal.util.StringUtils
import com.adobe.marketing.mobile.launch.rulesengine.LaunchRulesEngineConstants
import com.adobe.marketing.mobile.services.CacheFileService
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.Networking

/**
 * Facilitates downloading content from a URL into a local cache directory.
 * Stores downloaded content in to a directory governed by [CacheFileService].
 *
 * Currently, triggers to [download] are independent, successive calls.
 * This class is intentionally final and components are required to use this via composition;
 * This allows easier path for components using this class to add queuing logic for jobs internally,
 * maintaining compatibility when this class adds queuing logic in the future, and facilitating
 * cleaner tests in composing components.
 *
 * TODO: Refactor this class to either eliminate default caching or make caching optional.
 */
class RemoteDownloader(
    private val networkService: Networking,
    private val cacheFileService: CacheFileService
) {

    companion object {
        private const val TAG = "RemoteDownloader"
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
        completionCallback: (DownloadResult) -> Unit
    ) {
        if (!StringUtils.stringIsUrl(url)) {
            Log.debug(
                LaunchRulesEngineConstants.LOG_TAG,
                TAG,
                "Invalid URL: ($url). Contents cannot be downloaded."
            )
            completionCallback.invoke(DownloadResult(null, DownloadResult.Reason.INVALID_URL))
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
