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

package com.adobe.marketing.mobile.services

import com.adobe.marketing.mobile.util.TestConstants
import org.json.JSONException
import org.json.JSONObject
import java.net.MalformedURLException
import java.net.URL

/**
 * A NetworkRequest conforming class that provides additional functionality that is helpful in testing
 * scenarios.
 */
class TestableNetworkRequest @JvmOverloads constructor(
    url: String,
    method: HttpMethod,
    body: ByteArray? = null,
    headers: Map<String, String?>? = null,
    connectTimeout: Int = 5,
    readTimeout: Int = 5
) : NetworkRequest(url, method, body, headers, connectTimeout, readTimeout) {

    private val queryParamMap: Map<String, String> = splitQueryParameters(url)

    companion object {
        private const val LOG_SOURCE = "TestableNetworkRequest"

        /**
         * Creates an instance of [TestableNetworkRequest] from a given [NetworkRequest].
         * If the provided [NetworkRequest] is null, this method returns null.
         *
         * @param request The [NetworkRequest] to convert into a [TestableNetworkRequest].
         *                Can be null.
         * @return A new instance of [TestableNetworkRequest] if [request] is not null;
         *         otherwise, null.
         */
        @JvmStatic
        fun from(request: NetworkRequest?): TestableNetworkRequest? {
            return if (request == null) {
                null
            } else {
                TestableNetworkRequest(
                    request.url,
                    request.method,
                    request.body,
                    request.headers,
                    request.connectTimeout,
                    request.readTimeout
                )
            }
        }

        private fun splitQueryParameters(url: String): Map<String, String> {
            val queryParamMap = mutableMapOf<String, String>()
            try {
                val urlObj = URL(url)
                urlObj.query?.let { query ->
                    val pairs = query.split("&")
                    for (pair in pairs) {
                        val index = pair.indexOf("=")
                        if (index > 0) {
                            queryParamMap[pair.substring(0, index)] = pair.substring(index + 1)
                        }
                    }
                }
            } catch (e: MalformedURLException) {
                Log.warning(TestConstants.LOG_TAG, LOG_SOURCE, "Failed to decode Network Request URL '$url'")
            }
            return queryParamMap
        }
    }

    fun queryParam(key: String): String? {
        return queryParamMap[key]
    }

    /**
     * Two [TestableNetworkRequest]/[NetworkRequest]s are equal if:
     * 1. Their URLs have the same: protocol, host and path (*excluding* query parameters)
     * 2. Use the same [HttpMethod].
     *
     * @param other the other [TestableNetworkRequest] to compare to
     * @return true if the provided request is equal to this instance
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NetworkRequest) return false

        if (this.method != other.method) return false

        if (this.url == null && other.url == null) return true

        return try {
            val thisUrl = URL(this.url)
            val otherUrl = URL(other.url)
            thisUrl.protocol == otherUrl.protocol &&
                thisUrl.host == otherUrl.host &&
                thisUrl.path == otherUrl.path
        } catch (e: MalformedURLException) {
            false
        }
    }

    override fun hashCode(): Int {
        return try {
            val url = URL(this.url)
            listOf(url.protocol, url.host, url.path, this.method).hashCode()
        } catch (e: MalformedURLException) {
            listOf(this.url, this.method).hashCode()
        }
    }

    /**
     * Converts the body of the [TestableNetworkRequest] into a [JSONObject].
     *
     * @return A [JSONObject] representation of the body if it is valid JSON, otherwise null.
     */
    fun getBodyJson(): JSONObject? {
        val payload = body?.let { String(it) } ?: return null
        return try {
            JSONObject(payload)
        } catch (e: JSONException) {
            Log.warning(TestConstants.LOG_TAG, LOG_SOURCE, "Failed to create JSONObject from body with error: ${e.message}")
            null
        }
    }
}
