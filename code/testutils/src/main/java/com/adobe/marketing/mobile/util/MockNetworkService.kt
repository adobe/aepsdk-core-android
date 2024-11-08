/*
  Copyright 2023 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.util

import com.adobe.marketing.mobile.services.HttpConnecting
import com.adobe.marketing.mobile.services.HttpMethod
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.NetworkCallback
import com.adobe.marketing.mobile.services.NetworkRequest
import com.adobe.marketing.mobile.services.Networking
import com.adobe.marketing.mobile.services.TestableNetworkRequest
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * [Networking] conforming network service test helper utility class used for tests that require mocked
 * network requests and mocked responses.
 */
class MockNetworkService : Networking {
    private val helper = NetworkRequestHelper()
    /**
     * Flag that indicates if the [connectAsync] method was called.
     * Note that this property does not await and returns the status immediately.
     */
    val connectAsyncCalled: Boolean
        get() {
            // Assumes that `NetworkRequestHelper.recordSentNetworkRequest` is always called by `connectAsync`.
            // If this assumption changes, this flag logic needs to be updated.
            return helper.networkRequests.isNotEmpty()
        }
    /**
     * How many times the [connectAsync] method was called.
     * Note that this property does not await and returns the value immediately.
     */
    val connectAsyncCallCount: Int
        get() {
            // Assumes that `NetworkRequestHelper.recordSentNetworkRequest` is always called by `connectAsync`.
            // If this assumption changes, this flag logic needs to be updated.
            return helper.networkRequests.count()
        }

    // Simulating the async network service
    private val executorService: ExecutorService = Executors.newCachedThreadPool()

    companion object {
        private const val LOG_SOURCE = "MockNetworkService"
        private var delayedResponse = 0
        private var defaultResponse: HttpConnecting? = object : HttpConnecting {
            override fun getInputStream(): InputStream {
                return ByteArrayInputStream("".toByteArray())
            }

            override fun getErrorStream(): InputStream? {
                return null
            }

            override fun getResponseCode(): Int {
                return 200
            }

            override fun getResponseMessage(): String {
                return ""
            }

            override fun getResponsePropertyValue(responsePropertyKey: String): String? {
                return null
            }

            override fun close() {}
        }
    }

    override fun connectAsync(networkRequest: NetworkRequest?, resultCallback: NetworkCallback?) {
        val request = TestableNetworkRequest.from(networkRequest)
        if (request == null) {
            Log.error(
                TestConstants.LOG_TAG,
                LOG_SOURCE,
                "Received null network request. Early exiting connectAsync method."
            )
            return
        }
        Log.trace(
            TestConstants.LOG_TAG,
            LOG_SOURCE,
            "Received connectUrlAsync to URL '${request.url}' and HttpMethod '${request.method.name}'."
        )

        helper.recordNetworkRequest(request)

        executorService.submit {
            if (resultCallback != null) {
                if (delayedResponse > 0) {
                    try {
                        Thread.sleep((delayedResponse * 1000).toLong())
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
                // Since null responses are valid responses, only use the default response if no response
                // has been set for this request.
                val responses = helper.getResponsesFor(request)
                val response = if (responses != null) responses.firstOrNull() else defaultResponse
                resultCallback.call(response)
                // Do countdown after notifying completion handler to avoid prematurely ungating awaits
                // before required network logic finishes
                helper.countDownExpected(request)
            }
        }
    }

    /**
     * Clears all test expectations and recorded network requests and responses. Resets the default
     * response.
     */
    fun reset() {
        delayedResponse = 0
        helper.reset()
        defaultResponse = object : HttpConnecting {
            override fun getInputStream(): InputStream {
                return ByteArrayInputStream("".toByteArray())
            }

            override fun getErrorStream(): InputStream? {
                return null
            }

            override fun getResponseCode(): Int {
                return 200
            }

            override fun getResponseMessage(): String {
                return ""
            }

            override fun getResponsePropertyValue(responsePropertyKey: String): String? {
                return null
            }

            override fun close() {}
        }
    }

    /**
     * Sets the provided delay for all network responses, until reset.
     * @param delaySec The delay in seconds.
     */
    fun enableNetworkResponseDelay(delaySec: Int) {
        if (delaySec < 0) {
            return
        }
        delayedResponse = delaySec
    }

    /**
     * Sets a mock network response for the provided network request.
     *
     * @param url The URL `String` of the [TestableNetworkRequest] for which the mock response is being set.
     * @param method The [HttpMethod] of the [TestableNetworkRequest] for which the mock response is being set.
     * @param responseConnection The [HttpConnecting] instance to set as a response. If `null` is provided, the [defaultResponse] is used.
     */
    @JvmOverloads
    fun setMockResponseFor(
        url: String,
        method: HttpMethod = HttpMethod.POST,
        responseConnection: HttpConnecting?
    ) {
        helper.addResponseFor(
            TestableNetworkRequest(
                url,
                method
            ),
            responseConnection
        )
    }

    /**
     * Sets the default network response for all requests.
     *
     * @param responseConnection The [HttpConnecting] instance to be set as the default response.
     */
    fun setDefaultResponse(responseConnection: HttpConnecting?) {
        defaultResponse = responseConnection
    }

    /**
     * Sets the expected number of times a network request should be sent.
     *
     * @param url The URL `String` of the [TestableNetworkRequest] for which the expectation is set.
     * @param method The [HttpMethod] of the [TestableNetworkRequest] for which the expectation is set.
     * @param expectedCount The number of times the request is expected to be sent.
     */
    fun setExpectationForNetworkRequest(
        url: String,
        method: HttpMethod,
        expectedCount: Int
    ) {
        helper.setExpectationFor(
            TestableNetworkRequest(
                url,
                method
            ),
            expectedCount
        )
    }

    /**
     * Asserts that the correct number of network requests were sent based on previously set expectations.
     *
     * @throws InterruptedException If the current thread is interrupted while waiting.
     * @see [setExpectationForNetworkRequest]
     */
    @JvmOverloads
    fun assertAllNetworkRequestExpectations(
        ignoreUnexpectedRequests: Boolean = true,
        waitForUnexpectedEvents: Boolean = true,
        timeoutMillis: Int = TestConstants.Defaults.WAIT_NETWORK_REQUEST_TIMEOUT_MS
    ) {
        helper.assertAllNetworkRequestExpectations(ignoreUnexpectedRequests, waitForUnexpectedEvents, timeoutMillis)
    }

    /**
     * Returns all sent network requests (if any).
     * If a timeout is specified (default is 2000 milliseconds), the function will wait for the specified time for the network requests to be recorded.
     * If the timeout is set to 0, the function will immediately return the network requests without waiting.
     */
    fun getAllNetworkRequests(timeoutMillis: Int = TestConstants.Defaults.WAIT_NETWORK_REQUEST_TIMEOUT_MS): List<TestableNetworkRequest> {
        TestHelper.waitForThreads(timeoutMillis)
        return helper.networkRequests
    }

    /**
     * Returns the network request(s) sent through the Core NetworkService, or an empty list if none was found.
     *
     * Use this method after calling [setExpectationForNetworkRequest] to wait for expected requests.
     *
     * @param url The URL `String` of the [NetworkRequest] to get.
     * @param method The [HttpMethod] of the [NetworkRequest] to get.
     * @param timeoutMillis The duration (in milliseconds) to wait for the expected network requests before
     * timing out. Defaults to [TestConstants.Defaults.WAIT_NETWORK_REQUEST_TIMEOUT_MS].
     *
     * @return A list of matching [TestableNetworkRequest]s. Returns an empty list if no matching requests were dispatched.
     *
     * @throws InterruptedException If the current thread is interrupted while waiting.
     *
     * @see setExpectationForNetworkRequest
     */
    @Throws(InterruptedException::class)
    @JvmOverloads
    fun getNetworkRequestsWith(
        url: String,
        method: HttpMethod,
        timeoutMillis: Int = TestConstants.Defaults.WAIT_NETWORK_REQUEST_TIMEOUT_MS
    ): List<TestableNetworkRequest> {
        return helper.getNetworkRequestsWith(url, method, timeoutMillis)
    }

    /**
     * Create a mock network response to be used when calling [setMockResponseFor].
     * @param responseString the network response string, returned by [HttpConnecting.getInputStream]
     * @param code the HTTP status code, returned by [HttpConnecting.getResponseCode]
     * @return an [HttpConnecting] object
     * @see setMockResponseFor
     */
    fun createMockNetworkResponse(responseString: String?, code: Int): HttpConnecting {
        return createMockNetworkResponse(responseString, null, code, null, null)
    }

    /**
     * Create a mock network response to be used when calling [setMockResponseFor].
     * @param responseString the network response string, returned by [HttpConnecting.getInputStream]
     * @param errorString the network error string, returned by [HttpConnecting.getErrorStream]
     * @param code the HTTP status code, returned by [HttpConnecting.getResponseCode]
     * @param responseMessage the network response message, returned by [HttpConnecting.getResponseMessage]
     * @param propertyMap the network response header map, returned by [HttpConnecting.getResponsePropertyValue]
     * @return an [HttpConnecting] object
     * @see setMockResponseFor
     */
    fun createMockNetworkResponse(
        responseString: String?,
        errorString: String?,
        code: Int,
        responseMessage: String?,
        propertyMap: Map<String?, String?>?
    ): HttpConnecting {
        return object : HttpConnecting {
            override fun getInputStream(): InputStream? {
                return if (responseString != null) {
                    ByteArrayInputStream(responseString.toByteArray(StandardCharsets.UTF_8))
                } else null
            }

            override fun getErrorStream(): InputStream? {
                return if (errorString != null) {
                    ByteArrayInputStream(errorString.toByteArray(StandardCharsets.UTF_8))
                } else null
            }

            override fun getResponseCode(): Int {
                return code
            }

            override fun getResponseMessage(): String? {
                return responseMessage
            }

            override fun getResponsePropertyValue(responsePropertyKey: String): String? {
                return if (propertyMap != null) {
                    propertyMap[responsePropertyKey]
                } else null
            }

            override fun close() {}
        }
    }
}
