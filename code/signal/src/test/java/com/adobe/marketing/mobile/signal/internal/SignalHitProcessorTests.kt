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

package com.adobe.marketing.mobile.signal.internal

import com.adobe.marketing.mobile.services.DataEntity
import com.adobe.marketing.mobile.services.HttpConnecting
import com.adobe.marketing.mobile.services.HttpMethod
import com.adobe.marketing.mobile.services.NetworkRequest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(MockitoJUnitRunner.Silent::class)
class SignalHitProcessorTests {

    @Mock
    private lateinit var httpResponseConnection: HttpConnecting
    private lateinit var signalHitProcessor: SignalHitProcessor

    @Before
    fun setup() {
        Mockito.reset(httpResponseConnection)
        signalHitProcessor = SignalHitProcessor()
    }

    @Test(timeout = 100)
    fun `processHit() should return true for data with bad format`() {
        val entity = DataEntity("{}")
        val countDownLatch = CountDownLatch(1)
        signalHitProcessor.processHit(entity) { result ->
            if (result) {
                countDownLatch.countDown()
            }
        }
        countDownLatch.await()
    }

    @Test
    fun `processHit() should return true for data with empty url`() {
        val entity = DataEntity(
            """
            {
              "contentType": "application/json",
              "body": "{\"key\":\"value\"}",
              "url": "",
              "timeout": 4
            }
            """.trimIndent()
        )

        val countDownLatch = CountDownLatch(1)
        signalHitProcessor.processHit(entity) { result ->
            if (result) {
                countDownLatch.countDown()
            }
        }
        countDownLatch.await()
    }

    @Test
    fun `processHit() - request method should be GET if request body is empty`() {
        val entity = DataEntity(
            """
            {
              "contentType": "application/json",
              "body": "",
              "url": "https://www.postback.com",
              "timeout": 4
            }
            """.trimIndent()
        )
        var requestRecorder: NetworkRequest? = null
        signalHitProcessor = SignalHitProcessor { request, callback ->
            requestRecorder = request
            callback.call(null)
        }
        val countDownLatch = CountDownLatch(1)
        signalHitProcessor.processHit(entity) { countDownLatch.countDown() }

        countDownLatch.await()
        assertEquals(HttpMethod.GET, requestRecorder?.method)
    }

    @Test
    fun `processHit() - request method should be POST if request body is not empty`() {
        val entity = DataEntity(
            """
            {
              "contentType": "application/json",
              "body": "{\"key\":\"value\"}",
              "url": "https://www.postback.com",
              "timeout": 4
            }
            """.trimIndent()
        )
        var requestRecorder: NetworkRequest? = null
        signalHitProcessor = SignalHitProcessor { request, callback ->
            requestRecorder = request
            callback.call(null)
        }
        val countDownLatch = CountDownLatch(1)
        signalHitProcessor.processHit(entity) { countDownLatch.countDown() }

        countDownLatch.await()
        assertEquals(HttpMethod.POST, requestRecorder?.method)
    }

    @Test
    fun `processHit() - timeout should be a positive integer (default value is 2)`() {
        val entity = DataEntity(
            """
            {
              "contentType": "application/json",
              "body": "{\"key\":\"value\"}",
              "url": "https://www.postback.com",
              "timeout": -1
            }
            """.trimIndent()
        )
        var requestRecorder: NetworkRequest? = null
        signalHitProcessor = SignalHitProcessor { request, callback ->
            requestRecorder = request
            callback.call(null)
        }
        signalHitProcessor.processHit(entity) { }
        assertEquals(2, requestRecorder?.connectTimeout)
    }

    @Test
    fun `processHit() - timeout should non-zero (default value is 2)`() {
        val entity = DataEntity(
            """
            {
              "contentType": "application/json",
              "body": "{\"key\":\"value\"}",
              "url": "https://www.postback.com",
              "timeout": 0
            }
            """.trimIndent()
        )
        var requestRecorder: NetworkRequest? = null
        signalHitProcessor = SignalHitProcessor { request, callback ->
            requestRecorder = request
            callback.call(null)
        }
        signalHitProcessor.processHit(entity) { }
        assertEquals(2, requestRecorder?.connectTimeout)
    }

    @Test
    fun `processHit() - request header could be empty if the content type is not presented`() {
        val entity = DataEntity(
            """
            {
              "contentType": "",
              "body": "{\"key\":\"value\"}",
              "url": "https://www.postback.com",
              "timeout": 2
            }
            """.trimIndent()
        )
        var requestRecorder: NetworkRequest? = null
        signalHitProcessor = SignalHitProcessor { request, callback ->
            requestRecorder = request
            callback.call(null)
        }
        signalHitProcessor.processHit(entity) {}
        val headerSize = requestRecorder?.headers?.size
        assertEquals(0, headerSize)
    }

    @Test
    fun `processHit() - request should set both read timeout and connection timeout`() {
        val entity = DataEntity(
            """
            {
              "contentType": "",
              "body": "{\"key\":\"value\"}",
              "url": "https://www.postback.com",
              "timeout": 2
            }
            """.trimIndent()
        )
        var requestRecorder: NetworkRequest? = null
        signalHitProcessor = SignalHitProcessor { request, callback ->
            requestRecorder = request
            callback.call(null)
        }
        signalHitProcessor.processHit(entity) { }
        assertEquals(2, requestRecorder?.connectTimeout)
        assertEquals(2, requestRecorder?.readTimeout)
    }

    @Test
    fun `processHit() - should handle callback with null connection`() {
        val entity = DataEntity(
            """
            {
              "contentType": "",
              "body": "{\"key\":\"value\"}",
              "url": "https://www.postback.com",
              "timeout": 2
            }
            """.trimIndent()
        )
        signalHitProcessor = SignalHitProcessor { _, callback ->
            callback.call(null)
        }
        val countDownLatch = CountDownLatch(1)
        signalHitProcessor.processHit(entity) { countDownLatch.countDown() }

        countDownLatch.await()
    }

    @Test
    fun `processHit() - return true if network response code is in (200-299)`() {
        val entity = DataEntity(
            """
            {
              "contentType": "",
              "body": "{\"key\":\"value\"}",
              "url": "https://www.postback.com",
              "timeout": 2
            }
            """.trimIndent()
        )
        `when`(httpResponseConnection.responseCode).thenReturn(200)
        signalHitProcessor = SignalHitProcessor { _, callback ->
            callback.call(httpResponseConnection)
        }
        val countDownLatch = CountDownLatch(1)
        signalHitProcessor.processHit(entity) { if (it) countDownLatch.countDown() }

        countDownLatch.await()
    }

    @Test
    fun `processHit() - return false if network response is in (408, 504, 503)`() {
        val entity = DataEntity(
            """
            {
              "contentType": "",
              "body": "{\"key\":\"value\"}",
              "url": "https://www.postback.com",
              "timeout": 2
            }
            """.trimIndent()
        )
        `when`(httpResponseConnection.responseCode).thenReturn(408)
        signalHitProcessor = SignalHitProcessor { _, callback ->
            callback.call(httpResponseConnection)
        }
        val countDownLatch = CountDownLatch(1)
        signalHitProcessor.processHit(entity) { if (!it) countDownLatch.countDown() }

        countDownLatch.await()
    }

    @Test
    fun `processHit() - return true to drop this data entity if network response code is not in (200-299) or in (408, 504, 503)`() {
        val entity = DataEntity(
            """
            {
              "contentType": "",
              "body": "{\"key\":\"value\"}",
              "url": "https://www.postback.com",
              "timeout": 2
            }
            """.trimIndent()
        )
        `when`(httpResponseConnection.responseCode).thenReturn(300)
        signalHitProcessor = SignalHitProcessor { _, callback ->
            callback.call(httpResponseConnection)
        }
        val countDownLatch = CountDownLatch(1)
        signalHitProcessor.processHit(entity) { if (it) countDownLatch.countDown() }

        countDownLatch.await()
    }

    @Test
    fun `processHit() - drops the hit if the response is null`() {
        val entity = DataEntity(
            """
            {
              "contentType": "",
              "body": "{\"key\":\"value\"}",
              "url": "https://www.postback.com",
              "timeout": 2
            }
            """.trimIndent()
        )
        signalHitProcessor = SignalHitProcessor { _, callback ->
            callback.call(null)
        }
        val countDownLatch = CountDownLatch(1)
        signalHitProcessor.processHit(entity) { result ->
            if (result) countDownLatch.countDown()
        }

        countDownLatch.await(100, TimeUnit.MILLISECONDS)
    }
}
