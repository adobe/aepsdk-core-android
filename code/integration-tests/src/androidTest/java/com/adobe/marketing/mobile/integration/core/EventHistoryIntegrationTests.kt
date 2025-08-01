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

package com.adobe.marketing.mobile.integration.core

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adobe.marketing.mobile.AdobeCallbackWithError
import com.adobe.marketing.mobile.AdobeError
import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.EventHistoryRequest
import com.adobe.marketing.mobile.EventHistoryResult
import com.adobe.marketing.mobile.Extension
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.SDKHelper
import com.adobe.marketing.mobile.copyWithNewTimeStamp
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.RuntimeException
import java.util.concurrent.CountDownLatch

private class MockExtension(extensionApi: ExtensionApi) : Extension(extensionApi) {
    companion object {
        var extensionApi: ExtensionApi? = null
    }
    override fun getName(): String {
        return "mockextension"
    }

    override fun onRegistered() {
        super.onRegistered()
        extensionApi = api
    }
}
@RunWith(AndroidJUnit4::class)
class EventHistoryIntegrationTests {
    companion object {
        const val TEST_APP_ID = "appId"
        const val TEST_RULES_RESOURCE = "rules_attach.zip"
        const val WAIT_TIME_MILLIS = 5000L
    }

    @Before
    fun setup() {
        SDKHelper.initializeSDK(listOf(MockExtension::class.java))
    }

    @After
    fun cleanup() {
        SDKHelper.resetSDK()
    }

    private fun getEventHistoryResult(
        requests: Array<EventHistoryRequest>,
        enforceOrder: Boolean
    ): Array<EventHistoryResult> {
        if (MockExtension.extensionApi == null) {
            throw RuntimeException("ExtensionApi is null")
        }
        val countDownLatch = CountDownLatch(1)
        var ret = emptyArray<EventHistoryResult>()
        MockExtension.extensionApi?.getHistoricalEvents(requests, enforceOrder, object : AdobeCallbackWithError<Array<EventHistoryResult>> {
            override fun call(result: Array<EventHistoryResult>) {
                ret = result
                countDownLatch.countDown()
            }

            override fun fail(error: AdobeError?) {
                countDownLatch.countDown()
            }
        })
        countDownLatch.await()
        return ret
    }

    @Test
    fun testEventHistoryWithoutEnforceOrder() {
        // hashed string will be "key:value,numeric:552" - 1254850096
        val eventData = mapOf<String, Any>("key" to "value", "key2" to "value2", "numeric" to 552)
        val event2 = Event.Builder("name", "type", "source", arrayOf("key", "numeric"))
            .setEventData(eventData)
            .build()

        val event1 = event2.copyWithNewTimeStamp(event2.timestamp - 10)

        MobileCore.dispatchEvent(event1)
        MobileCore.dispatchEvent(event2)

        Thread.sleep(500)

        val validReq = EventHistoryRequest(
            mapOf("key" to "value", "numeric" to 552),
            0,
            System.currentTimeMillis()
        )
        val results1 = getEventHistoryResult(arrayOf(validReq), false)
        assertEquals(1, results1.size)
        assertEquals(2, results1[0].count)

        val invalidReq = EventHistoryRequest(mapOf("key" to "value"), 0, System.currentTimeMillis())
        val results2 = getEventHistoryResult(arrayOf(invalidReq), false)
        assertEquals(1, results2.size)
        assertEquals(0, results2[0].count)

        val reqInvalidTime = EventHistoryRequest(
            mapOf("key" to "value", "numeric" to 552),
            event1.timestamp - 5,
            event1.timestamp + 5
        )
        val results3 = getEventHistoryResult(arrayOf(reqInvalidTime), false)
        assertEquals(1, results3.size)
        assertEquals(1, results3[0].count)

        val results4 = getEventHistoryResult(arrayOf(validReq, invalidReq, reqInvalidTime), false)
        assertEquals(3, results4.size)
        assertEquals(2, results4[0].count)
        assertEquals(0, results4[1].count)
        assertEquals(1, results4[2].count)
    }

    @Test
    fun testEventHistoryWithEnforceOrder() {
        // hashed string will be "key:value,numeric:552" - 1254850096
        val eventData2 = mapOf<String, Any>("key" to "value", "key2" to "value2", "numeric" to 552)
        val event2 = Event.Builder("name", "type", "source", arrayOf("key", "numeric"))
            .setEventData(eventData2)
            .build()

        val eventData1 = mapOf<String, Any>("key" to "value")
        var event1 = Event.Builder("name", "type", "source", arrayOf("key", "numeric"))
            .setEventData(eventData1)
            .build()
        event1 = event1.copyWithNewTimeStamp(event2.timestamp - 10)

        MobileCore.dispatchEvent(event1)
        MobileCore.dispatchEvent(event2)

        Thread.sleep(500)

        val reqEvent1 = EventHistoryRequest(mapOf("key" to "value"), 0, System.currentTimeMillis())
        val reqEvent2 = EventHistoryRequest(
            mapOf("key" to "value", "numeric" to 552),
            0,
            System.currentTimeMillis()
        )

        val results1 = getEventHistoryResult(arrayOf(reqEvent1, reqEvent2), true)
        assertEquals(2, results1.size)
        assertEquals(1, results1[0].count)
        assertEquals(1, results1[1].count)

        val results2 = getEventHistoryResult(arrayOf(reqEvent2, reqEvent1), true)
        assertEquals(2, results2.size)
        assertEquals(1, results2[0].count)
        assertEquals(0, results2[1].count)
    }

    @Test
    fun testRulesAreAppliedBeforePersistingEvents() {
        SDKHelper.setupConfiguration(TEST_APP_ID, mapOf("global.privacy" to "optedin"), TEST_RULES_RESOURCE)
        // Wait for rules to be processed
        Thread.sleep(500)

        // hashed string will be "key:value,numeric:552" - 1254850096
        val eventData = mapOf<String, Any>("key2" to "value2", "numeric" to 552)
        val event = Event.Builder("name", "type", "source", arrayOf("key", "numeric"))
            .setEventData(eventData)
            .build()
        MobileCore.dispatchEvent(event)

        Thread.sleep(500)

        val reqEvent1 = EventHistoryRequest(
            mapOf("key" to "value", "numeric" to 552),
            0,
            System.currentTimeMillis()
        )
        val result = getEventHistoryResult(arrayOf(reqEvent1), false)
        assertEquals(1, result.size)
        assertEquals(1, result[0].count)
    }

    @Test
    fun testRecordHistoricalEvent() {
        // hashed string will be "key:value,numeric:552" - 1254850096
        val eventData = mapOf<String, Any>("key" to "value", "key2" to "value2", "numeric" to 552)
        val event = Event.Builder("name", "type", "source", arrayOf("key", "numeric"))
            .setEventData(eventData)
            .build()

        val validReq = EventHistoryRequest(
            mapOf("key" to "value", "numeric" to 552),
            0,
            System.currentTimeMillis()
        )

        val latch = CountDownLatch(1)
        MockExtension.extensionApi?.recordHistoricalEvent(event, object : AdobeCallbackWithError<Boolean> {
            override fun call(result: Boolean) {
                assertEquals(result, true)
                latch.countDown()
            }

            override fun fail(error: AdobeError?) {
                fail()
                latch.countDown()
            }
        })
        assertTrue(latch.await(WAIT_TIME_MILLIS, java.util.concurrent.TimeUnit.MILLISECONDS))
        val results = getEventHistoryResult(arrayOf(validReq), false)
        assertEquals(1, results.size)
        assertEquals(1, results[0].count)
    }

    @Test
    fun testRecordHistoricalEventNoMask() {
        // hashed string will be "key:value,key2:value2,numeric:552"
        val eventData = mapOf<String, Any>("key" to "value", "key2" to "value2", "numeric" to 552)
        val event = Event.Builder("name", "type", "source")
            .setEventData(eventData)
            .build()

        val validReq = EventHistoryRequest(
            eventData,
            0,
            System.currentTimeMillis()
        )

        val latch = CountDownLatch(1)
        MockExtension.extensionApi?.recordHistoricalEvent(event, object : AdobeCallbackWithError<Boolean> {
            override fun call(result: Boolean) {
                assertEquals(result, true)
                latch.countDown()
            }

            override fun fail(error: AdobeError?) {
                fail()
                latch.countDown()
            }
        })
        assertTrue(latch.await(WAIT_TIME_MILLIS, java.util.concurrent.TimeUnit.MILLISECONDS))
        val results = getEventHistoryResult(arrayOf(validReq), false)
        assertEquals(1, results.size)
        assertEquals(1, results[0].count)
    }
}
