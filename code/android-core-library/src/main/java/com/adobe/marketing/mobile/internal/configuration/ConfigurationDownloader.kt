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

import com.adobe.marketing.mobile.internal.util.toMap
import com.adobe.marketing.mobile.services.HttpConnecting
import com.adobe.marketing.mobile.services.HttpMethod
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.NetworkCallback
import com.adobe.marketing.mobile.services.NetworkRequest
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.caching.CacheEntry
import com.adobe.marketing.mobile.services.caching.CacheExpiry
import com.adobe.marketing.mobile.services.caching.CacheService
import com.adobe.marketing.mobile.util.StreamUtils
import com.adobe.marketing.mobile.util.TimeUtils
import com.adobe.marketing.mobile.util.UrlUtils
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.io.InputStream
import java.lang.NumberFormatException
import java.net.HttpURLConnection
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Responsible for downloading configuration json file and processing it to provide a
 * usable configuration for the app implementing AEP SDK.
 */
internal class ConfigurationDownloader {

    companion object {
        const val LOG_TAG = "ConfigurationDownloader"
        const val HTTP_HEADER_IF_MODIFIED_SINCE = "If-Modified-Since"
        const val HTTP_HEADER_IF_NONE_MATCH = "If-None-Match"
        const val HTTP_HEADER_LAST_MODIFIED = "Last-Modified"
        const val HTTP_HEADER_ETAG = "ETag"
        internal const val CONFIG_CACHE_NAME = "config"
        private const val DEFAULT_CONNECTION_TIMEOUT_MS = 10000
        private const val DEFAULT_READ_TIMEOUT_MS = 10000
    }

    /**
     * Triggers the download of configuration from [url] and invokes
     * [completionCallback] after processing the downloaded content.
     * Internally, a successful download result is cached and it accessible via [CacheService]
     * from the cache name [CONFIG_CACHE_NAME] and key [url]
     *
     * @param url the URL from which the configuration should be downloaded
     * @param completionCallback the callback to invoke with the parsed/processed configuration
     *
     */
    fun download(
        url: String,
        completionCallback: (Map<String, Any?>?) -> Unit
    ) {
        if (!UrlUtils.isValidUrl(url)) {
            completionCallback.invoke(null)
            return
        }

        val cacheResult = ServiceProvider.getInstance().cacheService.get(CONFIG_CACHE_NAME, url)

        // Compute headers
        val headers: MutableMap<String, String> = HashMap()
        if (cacheResult != null) {
            // use the HTTP_HEADER_ETAG of cached result to populate
            // HTTP_HEADER_IF_NONE_MATCH for the request
            val eTag = cacheResult.metadata?.get(HTTP_HEADER_ETAG) ?: ""
            headers[HTTP_HEADER_IF_NONE_MATCH] = eTag

            // Use HTTP_HEADER_LAST_MODIFIED of the cached result to
            // populate HTTP_HEADER_IF_MODIFIED_SINCE for the reqest
            // Last modified in cache metadata is stored in epoch string. So Convert it to RFC-2822 date format.
            val lastModified = cacheResult.metadata?.get(HTTP_HEADER_LAST_MODIFIED)
            val lastModifiedEpoch = try {
                lastModified?.toLong() ?: 0L
            } catch (e: NumberFormatException) {
                0L
            }

            val ifModifiedSince = TimeUtils.getRFC2822Date(
                lastModifiedEpoch,
                TimeZone.getTimeZone("GMT"),
                Locale.US
            )
            headers[HTTP_HEADER_IF_MODIFIED_SINCE] = ifModifiedSince
        }

        val networkRequest = NetworkRequest(
            url,
            HttpMethod.GET,
            null,
            headers,
            DEFAULT_CONNECTION_TIMEOUT_MS,
            DEFAULT_READ_TIMEOUT_MS
        )

        val networkCallback = NetworkCallback { response: HttpConnecting ->
            val config = handleDownloadResponse(url, response)
            completionCallback.invoke(config)
        }

        ServiceProvider.getInstance().networkService.connectAsync(networkRequest, networkCallback)
    }

    /**
     * Handles the processing a new configuration from [response] or fetching cached configuration
     * based on [HttpConnecting.responseCode] of the [response]
     *
     * @param url the url for which the [response] is obtained. Used for caching the downloaded content.
     * @param response the response which is to be processed
     * @return a map representation of the json configuration obtained from [response]
     */
    private fun handleDownloadResponse(url: String, response: HttpConnecting): Map<String, Any?>? {
        return when (response.responseCode) {
            HttpURLConnection.HTTP_OK -> {
                val metadata = mutableMapOf<String, String>()
                val lastModifiedProp = response.getResponsePropertyValue(HTTP_HEADER_LAST_MODIFIED)
                val lastModifiedDate = TimeUtils.parseRFC2822Date(lastModifiedProp, TimeZone.getTimeZone("GMT"), Locale.US) ?: Date(0L)
                val lastModifiedMetadata = lastModifiedDate.time.toString()
                metadata[HTTP_HEADER_LAST_MODIFIED] = lastModifiedMetadata

                val eTagProp = response.getResponsePropertyValue(HTTP_HEADER_ETAG)
                metadata[HTTP_HEADER_ETAG] = eTagProp ?: ""

                // extract the rules file and return.
                processDownloadedConfig(url, response.inputStream, metadata)
            }

            HttpURLConnection.HTTP_NOT_MODIFIED -> {
                Log.debug(
                    ConfigurationExtension.TAG,
                    LOG_TAG,
                    "Configuration from $url has not been modified. Fetching from cache."
                )

                val cacheResult = ServiceProvider.getInstance().cacheService.get(CONFIG_CACHE_NAME, url)
                processDownloadedConfig(url, cacheResult?.data, cacheResult?.metadata)
            }

            else -> {
                Log.debug(
                    ConfigurationExtension.TAG,
                    LOG_TAG,
                    "Download result :${response.responseCode}"
                )
                null
            }
        }
    }

    private fun processDownloadedConfig(
        url: String,
        response: InputStream?,
        metadata: Map<String, String?>?
    ): Map<String, Any?>? {
        val content: String? = StreamUtils.readAsString(response)
        return when {
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
                    // Downloaded configuration is expected to be a JSON string.
                    val downloadedConfig = JSONObject(JSONTokener(content))
                    val configMap = downloadedConfig.toMap()

                    // Cache the downloaded configuration before returning
                    val cacheEntry = CacheEntry(content.byteInputStream(), CacheExpiry.never(), metadata)
                    ServiceProvider.getInstance().cacheService.set(CONFIG_CACHE_NAME, url, cacheEntry)
                    configMap
                } catch (exception: JSONException) {
                    Log.debug(
                        ConfigurationExtension.TAG,
                        LOG_TAG,
                        "Exception processing downloaded configuration $exception"
                    )
                    null
                }
            }
        }
    }
}
