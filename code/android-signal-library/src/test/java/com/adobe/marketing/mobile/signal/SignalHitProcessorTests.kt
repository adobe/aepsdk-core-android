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
package com.adobe.marketing.mobile.signal

import com.adobe.marketing.mobile.services.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

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

    @Test
    fun `retryInterval() should return positive integer`() {
        assertTrue(signalHitProcessor.retryInterval(null) >= 0)
    }

    @Test
    fun `processHit() should return true for null data`() {
        // notes: if return false, HitQueue will reprocess this data entity
        assertTrue(signalHitProcessor.processHit(null))
    }

    @Test
    fun `processHit() should return true for data with bad format`() {
        // notes: if return false, HitQueue will reprocess this data entity
        val entity = DataEntity("{}")
        assertTrue(signalHitProcessor.processHit(entity))
    }

    @Test
    fun `processHit() should return true for data with empty url`() {
        // notes: if return false, HitQueue will reprocess this data entity
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
        assertTrue(signalHitProcessor.processHit(entity))
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
        signalHitProcessor.processHit(entity)
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
        signalHitProcessor.processHit(entity)
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
        signalHitProcessor.processHit(entity)
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
        signalHitProcessor.processHit(entity)
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
        signalHitProcessor.processHit(entity)
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
        signalHitProcessor.processHit(entity)
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
        signalHitProcessor.processHit(entity)
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
        assertTrue(signalHitProcessor.processHit(entity))
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
        assertFalse(signalHitProcessor.processHit(entity))
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
        assertTrue(signalHitProcessor.processHit(entity))
    }

}