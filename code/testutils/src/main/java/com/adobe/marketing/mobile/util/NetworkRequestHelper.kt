/*
  Copyright 2021 Adobe. All rights reserved.
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
import com.adobe.marketing.mobile.services.NetworkRequest
import com.adobe.marketing.mobile.services.TestableNetworkRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import java.util.concurrent.TimeUnit

/**
 * Provides shared utilities and logic for implementations of `Networking` classes used for testing.
 *
 * @see [MockNetworkService]
 * @see [RealNetworkService]
 */
class NetworkRequestHelper {
    private val _networkRequests: MutableList<TestableNetworkRequest> = mutableListOf()
    // Read-only access to network requests
    val networkRequests: List<TestableNetworkRequest>
        get() = _networkRequests

    private val _networkResponses: MutableMap<TestableNetworkRequest, MutableList<HttpConnecting?>> = HashMap()
    // Read-only access to network responses
    val networkResponses: Map<TestableNetworkRequest, List<HttpConnecting?>>
        get() = _networkResponses.mapValues { it.value.toList() }

    private val expectedNetworkRequests: MutableMap<TestableNetworkRequest, ADBCountDownLatch> = HashMap()

    companion object {
        private const val LOG_SOURCE = "NetworkRequestHelper"
    }

    /**
     * Records a sent network request.
     *
     * @param request The [TestableNetworkRequest] that is to be recorded.
     */
    fun recordNetworkRequest(request: TestableNetworkRequest) {
        Log.trace(
            TestConstants.LOG_TAG,
            LOG_SOURCE,
            "Recording network request with URL ${request.url} and HTTPMethod ${request.method}"
        )

        _networkRequests.add(request)
    }

    /**
     * Resets the helper state by clearing all test expectations and recorded network requests and responses.
     */
    fun reset() {
        Log.trace(
            TestConstants.LOG_TAG,
            LOG_SOURCE,
            "Reset network request expectations and recorded network requests and responses."
        )
        _networkRequests.clear()
        _networkResponses.clear()
        expectedNetworkRequests.clear()
    }

    /**
     * Decrements the expectation count for a given network request.
     *
     * @param request The [TestableNetworkRequest] for which the expectation count should be decremented.
     */
    fun countDownExpected(request: TestableNetworkRequest) {
        expectedNetworkRequests[request]?.countDown()
    }

    /**
     * Asserts on the expectation for the given network request, validating that all expected responses are received
     * within the provided timeout duration and that expected count is not exceeded. If no expectation is
     * set for the request, a configurable default wait time is applied.
     *
     * @param request The [NetworkRequest] for which the expectation should be asserted.
     * @param timeoutMillis The maximum duration (in milliseconds) to wait for the expected responses before timing out.
     * @param waitForUnexpectedEvents If `true`, a default wait time will be used when no expectation is set for the request.
     *                                Defaults to `true`.
     *
     * @throws InterruptedException If the current thread is interrupted while waiting.
     */
    @Throws(InterruptedException::class)
    fun awaitRequest(request: TestableNetworkRequest, timeoutMillis: Int, waitForUnexpectedEvents: Boolean = true) {
        val expectation = expectedNetworkRequests[request]
        if (expectation != null) {
            val awaitResult = expectation.await(timeoutMillis.toLong(), TimeUnit.MILLISECONDS)
            // Verify that the expectation passes within the given timeout
            assertTrue(
                """
				Time out waiting for network request with URL '${request.url}' and HTTP method 
				'${request.method.name}'. Received (${expectation.currentCount}/${expectation.initialCount}) expected requests. 
				""".trimIndent(),
                awaitResult
            )
            // Validate that the actual count does not exceed the expected count
            assertEquals(
                """
                Expected only ${expectation.initialCount} network request(s) for URL ${request.url} and 
				HTTP method ${request.method.name}, but received ${expectation.currentCount}. 
                """.trimIndent(),
                expectation.initialCount,
                expectation.currentCount
            )
        }
        // Default wait time for network request with no previously set expectation
        else {
            if (waitForUnexpectedEvents) {
                TestHelper.waitForThreads(timeoutMillis)
            }
        }
    }

    /**
     * Immediately returns all network requests that match the provided network request. Does **not**
     * await.
     *
     * The matching logic relies on [TestableNetworkRequest.equals].
     *
     * @param request The [TestableNetworkRequest] for which to get matching requests.
     *
     * @return A list of [TestableNetworkRequest]s that match the provided [request]. If no matches are found, an empty list is returned.
     */
    fun getRequestsMatching(request: TestableNetworkRequest): List<TestableNetworkRequest> {
        return _networkRequests.filter { it == request }
    }

    /**
     * Adds a network response for the provided network request. If a response already exists, adds
     * the given response to the end of the list.
     *
     * @param request The [TestableNetworkRequest] for which the response will be set.
     * @param responseConnection The [HttpConnecting] to add as a response.
     */
    fun addResponseFor(
        request: TestableNetworkRequest,
        responseConnection: HttpConnecting?
    ) {
        if (_networkResponses[request] != null) {
            _networkResponses[request]?.add(responseConnection)
        } else {
            // If there's no response for this request yet, start a new list with the first response
            _networkResponses[request] = mutableListOf(responseConnection)
        }
    }

    /**
     * Removes all network responses for the provided network request.
     *
     * @param request The [TestableNetworkRequest] for which all responses will be removed.
     */
    fun removeResponsesFor(request: TestableNetworkRequest) {
        _networkResponses.remove(request)
    }

    /**
     * Returns the network responses for the given network request.
     *
     * @param request The [TestableNetworkRequest] for which the associated responses should be returned.
     * @return The list of [HttpConnecting] responses for the given request or `null` if not found.
     * @see [TestableNetworkRequest.equals] for the logic used to match network requests.
     */
    fun getResponsesFor(request: TestableNetworkRequest): List<HttpConnecting?>? {
        return _networkResponses[request]
    }

    /**
     * Sets the expected number of times a network request should be sent. If there is already an existing expectation
     * for the same request, it is replaced with the new value.
     *
     * @param request The [TestableNetworkRequest] to set the expectation for.
     * @param count The expected number of times the request should be sent.
     * @see [assertAllNetworkRequestExpectations] for checking all expectations.
     */
    fun setExpectationFor(request: TestableNetworkRequest, count: Int) {
        expectedNetworkRequests[request] = ADBCountDownLatch(count)
    }

    /**
     * Asserts that the correct number of network requests have been sent based on previously set expectations.
     * It waits for expected requests to complete and optionally checks for unexpected ones.
     *
     * @param ignoreUnexpectedRequests If `true`, skips validation of unexpected requests. Defaults to `true`.
     * @param waitForUnexpectedRequests If `true`, waits for unexpected requests to occur within the given timeout. Defaults to `true`.
     * @param timeoutMillis The maximum time to wait (in milliseconds) for expected requests to complete. Defaults to [TestConstants.Defaults.WAIT_NETWORK_REQUEST_TIMEOUT_MS].
     *
     * @throws InterruptedException If the current thread is interrupted while waiting.
     * @see [setExpectationFor] to set expectations for specific network requests.
     */
    @Throws(InterruptedException::class)
    fun assertAllNetworkRequestExpectations(
        ignoreUnexpectedRequests: Boolean = true,
        waitForUnexpectedRequests: Boolean = true,
        timeoutMillis: Int = TestConstants.Defaults.WAIT_NETWORK_REQUEST_TIMEOUT_MS
    ) {
        // Allow for some extra time for threads to finish before asserts
        TestHelper.waitForThreads(2000)
        // Validate expected events
        for (expectedRequest in expectedNetworkRequests.keys) {
            awaitRequest(expectedRequest, timeoutMillis, false)
        }
        // Validate unexpected requests if required
        if (ignoreUnexpectedRequests) {
            return
        }
        if (waitForUnexpectedRequests) {
            TestHelper.waitForThreads(timeoutMillis)
        }
        assertNoUnexpectedRequests()
    }

    /**
     * Asserts that there are no unexpected network requests.
     */
    private fun assertNoUnexpectedRequests() {
        // Group unexpected network requests (those not in expected keys) by TestableNetworkRequest equality
        val groupedRequests = _networkRequests
            .filter { it !in expectedNetworkRequests.keys }
            .groupBy { it }

        val failureDetails = groupedRequests.entries.joinToString(separator = "\n") { (request, group) ->
            "(URL: ${request.url}, HTTPMethod: ${request.method.name}, Count: ${group.size})"
        }

        // If there are any unexpected requests, fail with a message
        if (failureDetails.isNotEmpty()) {
            fail("Received unexpected network request(s): \n$failureDetails")
        }
    }

    /**
     * Fetches the network request(s) matching the provided [url] and [method], returning an empty list if none were found.
     * To wait for expected requests, this method should be used after calling [setExpectationFor].
     *
     * @param url The URL string of the [NetworkRequest] to match.
     * @param method The HTTP method of the [NetworkRequest] to match.
     * @param timeoutMillis The time in milliseconds to wait for the expected network requests. Defaults to [TestConstants.Defaults.WAIT_NETWORK_REQUEST_TIMEOUT_MS].
     *
     * @return A list of [NetworkRequest]s that match the provided [url] and [method]. If no matching requests are found,
     * an empty list is returned.
     *
     * @throws InterruptedException If the current thread is interrupted while waiting.
     */
    @Throws(InterruptedException::class)
    fun getNetworkRequestsWith(
        url: String,
        method: HttpMethod,
        timeoutMillis: Int = TestConstants.Defaults.WAIT_NETWORK_REQUEST_TIMEOUT_MS
    ): List<TestableNetworkRequest> {
        val testableRequest = TestableNetworkRequest(url, method)
        awaitRequest(testableRequest, timeoutMillis)
        return getRequestsMatching(testableRequest)
    }
}
