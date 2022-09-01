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

import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.internal.util.FileUtils
import com.adobe.marketing.mobile.internal.util.toMap
import com.adobe.marketing.mobile.util.remotedownload.RemoteDownloader
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener

/**
 * Responsible for downloading configuration json file and processing it to provide a
 * usable configuration for the app implementing AEP SDK.
 */
internal class ConfigurationDownloader(
    private val remoteDownloader: RemoteDownloader,
    private val metadataProvider: FileMetadataProvider
) {

    companion object {
        const val LOG_TAG = "ConfigurationDownloader"
    }

    /**
     * Triggers the download of configuration from [url] into [directory] and invokes
     * [completionCallback] after processing the downloaded content.
     *
     * @param url the URL from which the configuration should be downloaded
     * @param directory the optional sub-directory within the cache directory used by [remoteDownloader]
     *        into which the file should be cached
     * @param completionCallback the callback to invoke with the parsed/processed configuration
     *
     */
    fun download(
        url: String,
        directory: String?,
        completionCallback: (Map<String, Any?>?) -> Unit
    ) {
        remoteDownloader.download(url, directory, metadataProvider) { downloadResult ->
            val file = downloadResult.data
            val content: String? = FileUtils.readAsString(file)
            val config = when {

                content == null -> null

                content.isEmpty() -> {
                    Log.debug(
                        ConfigurationExtension.TAG,
                        LOG_TAG,
                        "Downloaded configuration is empty."
                    )
                    emptyMap()
                }

                else -> {
                    try {
                        val downloadedConfig = JSONObject(JSONTokener(content))
                        downloadedConfig.toMap()
                    } catch (exception: JSONException) {
                        Log.error(
                            ConfigurationExtension.TAG,
                            LOG_TAG,
                            "Exception processing downloaded configuration $exception"
                        )
                        null
                    }
                }
            }

            // Invoke callback with the processed configuration
            completionCallback.invoke(config)
        }
    }
}
