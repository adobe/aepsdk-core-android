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

package com.adobe.marketing.mobile.identity

import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.EventCoder
import com.adobe.marketing.mobile.services.DataEntity
import com.adobe.marketing.mobile.services.Networking
import com.adobe.marketing.mobile.services.ServiceProvider
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Spy
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import java.io.InputStream
import java.util.concurrent.CountDownLatch

@RunWith(MockitoJUnitRunner.Silent::class)
class IdentityHitsProcessingTests {

    @Mock
    private lateinit var mockedIdentityExtension: IdentityExtension

    @Spy
    private lateinit var spiedNetworking: Networking

    private val event = Event.Builder("event", "type", "source").build()

    @Before
    fun setup() {
        Mockito.reset(mockedIdentityExtension)
        val event = Event.Builder("event", "type", "source").build()
    }

    private fun initializeIdentityHitsProcessing(): IdentityHitsProcessing {
        return IdentityHitsProcessing(mockedIdentityExtension)
    }

    @Test
    fun `retryInterval() - 30s`() {
        val identityHitsProcessing = initializeIdentityHitsProcessing()
        assertEquals(30, identityHitsProcessing.retryInterval(DataEntity("{}")))
    }

    @Test(timeout = 10000)
    fun `processHit() - bad entity`() {
        val identityHitsProcessing = initializeIdentityHitsProcessing()
        ServiceProvider.getInstance().networkService = spiedNetworking
        val json = """
            {
              "URL": "url",
              "invalid_EVENT": ""
            }
        """.trimIndent()

        val countDownLatch = CountDownLatch(1)
        identityHitsProcessing.processHit(DataEntity(json)) {
            assertTrue(it)
            countDownLatch.countDown()
        }
        countDownLatch.await()
        verify(spiedNetworking, never()).connectAsync(any(), any())
    }

    @Test(timeout = 10000)
    fun `processHit() - invalid event json`() {
        val identityHitsProcessing = initializeIdentityHitsProcessing()
        ServiceProvider.getInstance().networkService = spiedNetworking
        val json = """
            {
              "URL": "url",
              "EVENT": "x{+"
            }
        """.trimIndent()
        val countDownLatch = CountDownLatch(1)
        identityHitsProcessing.processHit(DataEntity(json)) {
            assertTrue(it)
            countDownLatch.countDown()
        }
        countDownLatch.await()
        verify(spiedNetworking, never()).connectAsync(any(), any())
    }

    @Test(timeout = 10000)
    fun `processHit() - invalid event json 2`() {
        val identityHitsProcessing = initializeIdentityHitsProcessing()
        ServiceProvider.getInstance().networkService = spiedNetworking
        val json = """
            {
              "URL": null,
              "EVENT": null
            }
        """.trimIndent()
        val countDownLatch = CountDownLatch(1)
        identityHitsProcessing.processHit(DataEntity(json)) {
            assertTrue(it)
            countDownLatch.countDown()
        }
        countDownLatch.await()
        verify(spiedNetworking, never()).connectAsync(any(), any())
    }

    @Test(timeout = 10000)
    fun `processHit() - null connection`() {
        val identityHitsProcessing = initializeIdentityHitsProcessing()
        ServiceProvider.getInstance().networkService = spiedNetworking
        val jsonObject = JSONObject()
        jsonObject.put("URL", "url")
        jsonObject.put("EVENT", EventCoder.encode(event))

        ServiceProvider.getInstance().networkService = Networking { _, callback ->

            callback.call(null)
        }
        val countDownLatch = CountDownLatch(2)
        doAnswer { invocation ->
            assertNull(invocation.arguments[0])
            countDownLatch.countDown()
        }.`when`(mockedIdentityExtension).networkResponseLoaded(anyOrNull(), any())
        identityHitsProcessing.processHit(DataEntity(jsonObject.toString())) {
            assertTrue(it)
            countDownLatch.countDown()
        }
        countDownLatch.await()
    }

    @Test(timeout = 10000)
    fun `processHit() - response code is unrecoverable (502)`() {
        val identityHitsProcessing = initializeIdentityHitsProcessing()
        ServiceProvider.getInstance().networkService = spiedNetworking
        val jsonObject = JSONObject()
        jsonObject.put("URL", "url")
        jsonObject.put("EVENT", EventCoder.encode(event))

        ServiceProvider.getInstance().networkService = Networking { _, callback ->
            callback.call(object : DefaultHttpConnecting() {
                override fun getResponseCode(): Int {
                    return 502
                }
            })
        }
        val countDownLatch = CountDownLatch(2)
        doAnswer { invocation ->
            assertNull(invocation.arguments[0])
            countDownLatch.countDown()
        }.`when`(mockedIdentityExtension).networkResponseLoaded(anyOrNull(), any())

        identityHitsProcessing.processHit(DataEntity(jsonObject.toString())) {
            assertTrue(it)
            countDownLatch.countDown()
        }
        countDownLatch.await()
    }

    @Test(timeout = 10000)
    fun `processHit() - response code is recoverable (408)`() {
        val identityHitsProcessing = initializeIdentityHitsProcessing()
        ServiceProvider.getInstance().networkService = spiedNetworking
        val jsonObject = JSONObject()
        jsonObject.put("URL", "url")
        jsonObject.put("EVENT", EventCoder.encode(event))

        ServiceProvider.getInstance().networkService = Networking { _, callback ->
            callback.call(object : DefaultHttpConnecting() {
                override fun getResponseCode(): Int {
                    return 408
                }
            })
        }
        val countDownLatch = CountDownLatch(1)
        identityHitsProcessing.processHit(DataEntity(jsonObject.toString())) {
            assertFalse(it)
            countDownLatch.countDown()
        }
        countDownLatch.await()
        verify(mockedIdentityExtension, never()).networkResponseLoaded(any(), any())
    }

    @Test(timeout = 10000)
    fun `processHit() - response code is 200`() {
        val identityHitsProcessing = initializeIdentityHitsProcessing()
        ServiceProvider.getInstance().networkService = spiedNetworking
        val jsonObject = JSONObject()
        jsonObject.put("URL", "url")
        jsonObject.put("EVENT", EventCoder.encode(event))

        ServiceProvider.getInstance().networkService = Networking { _, callback ->
            callback.call(object : DefaultHttpConnecting() {
                override fun getResponseCode(): Int {
                    return 200
                }

                override fun getInputStream(): InputStream? {
                    val json = """
                            {
                                "d_mid":"32392347938908875026252848914224372728",
                                "id_sync_ttl":604800,
                                "d_blob":"hmk_Lq6TPIBMW925SPhw3Q",
                                "dcs_region":9,
                                "d_ottl":7200,
                                "ibs":[],
                                "d_optout":["a", "b"],
                                "subdomain":"obumobile5",
                                "tid":"d47JfAKTTsU="
                            }
                    """.trimIndent()
                    return json.byteInputStream(Charsets.UTF_8)
                }
            })
        }
        val countDownLatch = CountDownLatch(1)
        doAnswer { invocation ->
            val result = invocation.arguments[0] as? IdentityResponseObject
            assertNotNull(result)
            assertEquals("hmk_Lq6TPIBMW925SPhw3Q", result?.blob)
            countDownLatch.countDown()
        }.`when`(mockedIdentityExtension).networkResponseLoaded(any(), any())

        identityHitsProcessing.processHit(DataEntity(jsonObject.toString())) {
            assertTrue(it)
            countDownLatch.countDown()
        }
        countDownLatch.await()
    }

    @Test(timeout = 10000)
    fun `processHit() - response code is 200 - unsupported json array`() {
        val identityHitsProcessing = initializeIdentityHitsProcessing()
        ServiceProvider.getInstance().networkService = spiedNetworking
        val jsonObject = JSONObject()
        jsonObject.put("URL", "url")
        jsonObject.put("EVENT", EventCoder.encode(event))

        ServiceProvider.getInstance().networkService = Networking { _, callback ->
            callback.call(object : DefaultHttpConnecting() {
                override fun getResponseCode(): Int {
                    return 200
                }

                override fun getInputStream(): InputStream? {
                    val json = """
                            {
                                "d_mid":"32392347938908875026252848914224372728",
                                "id_sync_ttl":604800,
                                "d_blob":"hmk_Lq6TPIBMW925SPhw3Q",
                                "dcs_region":9,
                                "d_ottl":7200,
                                "ibs":[],
                                "d_optout":["a", {}],
                                "subdomain":"obumobile5",
                                "tid":"d47JfAKTTsU="
                            }
                    """.trimIndent()
                    return json.byteInputStream(Charsets.UTF_8)
                }
            })
        }
        val countDownLatch = CountDownLatch(2)
        doAnswer { invocation ->
            val result = invocation.arguments[0] as? IdentityResponseObject
            assertNotNull(result)
            assertEquals("hmk_Lq6TPIBMW925SPhw3Q", result?.blob)
            countDownLatch.countDown()
        }.`when`(mockedIdentityExtension).networkResponseLoaded(any(), any())

        identityHitsProcessing.processHit(DataEntity(jsonObject.toString())) {
            assertTrue(it)
            countDownLatch.countDown()
        }
        countDownLatch.await()
    }

    @Test(timeout = 10000)
    fun `processHit() - response code is 200 with bad json`() {
        val identityHitsProcessing = initializeIdentityHitsProcessing()
        ServiceProvider.getInstance().networkService = spiedNetworking
        val jsonObject = JSONObject()
        jsonObject.put("URL", "url")
        jsonObject.put("EVENT", EventCoder.encode(event))

        ServiceProvider.getInstance().networkService = Networking { _, callback ->
            callback.call(object : DefaultHttpConnecting() {
                override fun getResponseCode(): Int {
                    return 200
                }

                override fun getInputStream(): InputStream? {
                    val json = """
                            invalid_json{
                                "d_mid":"32392347938908875026252848914224372728",
                                "id_sync_ttl":604800,
                                "d_blob":"hmk_Lq6TPIBMW925SPhw3Q",
                                "dcs_region":9,
                                "d_ottl":7200,
                                "ibs":[],
                                "subdomain":"obumobile5",
                                "tid":"d47JfAKTTsU="
                            }
                    """.trimIndent()
                    return json.byteInputStream(Charsets.UTF_8)
                }
            })
        }
        val countDownLatch = CountDownLatch(1)
        identityHitsProcessing.processHit(DataEntity(jsonObject.toString())) {
            assertFalse(it)
            countDownLatch.countDown()
        }
        countDownLatch.await()
        verify(mockedIdentityExtension, never()).networkResponseLoaded(any(), any())
    }
}
