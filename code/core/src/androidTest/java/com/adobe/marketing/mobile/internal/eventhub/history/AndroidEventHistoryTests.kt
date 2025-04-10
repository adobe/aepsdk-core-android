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

package com.adobe.marketing.mobile.internal.eventhub.history

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.EventHistoryRequest
import com.adobe.marketing.mobile.TestUtils
import com.adobe.marketing.mobile.copyWithNewTimeStamp
import com.adobe.marketing.mobile.services.MockAppContextService
import com.adobe.marketing.mobile.services.ServiceProviderModifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class AndroidEventHistoryTests {

    private lateinit var androidEventHistory: AndroidEventHistory

    init {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val mockAppContextService = MockAppContextService().apply {
            appContext = context
        }
        ServiceProviderModifier.setAppContextService(mockAppContextService)
        TestUtils.deleteAllFilesInCacheDir(context)
        context.applicationContext.getDatabasePath(DATABASE_NAME).delete()
        try {
            androidEventHistory = AndroidEventHistory()
        } catch (e: EventHistoryDatabaseCreationException) {
            fail(e.localizedMessage)
        }
    }

    @Test
    fun testRecordEvent() {
        val data = mapOf("key" to "value")
        assertTrue(record(data))
    }

    @Test
    fun testGetEventsWithEnforceOrder() {
        val data = mapOf("key" to "value")
        assertTrue(record(data))

        val data1 = mapOf("key1" to "value1")
        assertTrue(record(data1))

        val requests = arrayOf(
            EventHistoryRequest(data, 0, 0), // toDate is assumed to be current timestamp.
            EventHistoryRequest(data1, 0, System.currentTimeMillis())
        )

        // if enforceOrder == true, 1 denotes success satisfying all event history requests
        assertEquals(1, query(requests, true))
    }

    @Test
    fun testGetEventsWithEnforceOrderSuccessWithRange() {
        val data = mapOf("key" to "value")
        assertTrue(record(data, 5000))

        val data1 = mapOf("key1" to "value1")
        assertTrue(record(data1, 10000))

        val requests = arrayOf(
            EventHistoryRequest(data, 0, 6000),
            EventHistoryRequest(data1, 8000, System.currentTimeMillis())
        )

        // if enforceOrder == true, 1 denotes success satisfying all event history requests
        assertEquals(1, query(requests, true))
    }

    @Test
    fun testGetEventsWithEnforceOrderSuccessWithMultipleIdenticalEvents() {
        val data = mapOf("key" to "value")
        assertTrue(record(data, 5000))

        val data1 = mapOf("key1" to "value1")
        assertTrue(record(data1, 10000))

        val data2 = mapOf("key2" to "value2")
        assertTrue(record(data2, 15000))

        assertTrue(record(data, 20000))

        assertTrue(record(data, 30000))

        val requests = arrayOf(
            EventHistoryRequest(data, 0, 0),
            EventHistoryRequest(data1, 0, 0),
            EventHistoryRequest(data2, 0, 0)
        )

        // if enforceOrder == true, 1 denotes success satisfying all event history requests
        assertEquals(1, query(requests, true))
    }

    @Test
    fun testGetEventsWithEnforceOrderFailureWithMultipleIdenticalEvents() {
        val data = mapOf("key" to "value")
        assertTrue(record(data, 5000))

        val data1 = mapOf("key1" to "value1")
        assertTrue(record(data1, 10000))

        val data2 = mapOf("key2" to "value2")

        assertTrue(record(data, 20000))

        assertTrue(record(data, 30000))

        val requests = arrayOf(
            EventHistoryRequest(data, 0, 0),
            EventHistoryRequest(data1, 0, 0),
            EventHistoryRequest(data2, 0, 0)
        )

        // if enforceOrder == true, 0 denotes failure looking all event history requests
        assertEquals(0, query(requests, true))
    }

    @Test
    fun testGetEventsWithEnforceOrderOverlappingTimestamp() {
        val data = mapOf("key" to "value")
        assertTrue(record(data, 10000))

        val data1 = mapOf("key1" to "value1")
        assertTrue(record(data1, 5000))

        val requests = arrayOf(
            EventHistoryRequest(data, 0, System.currentTimeMillis()),
            EventHistoryRequest(data1, 0, System.currentTimeMillis())
        )

        // if enforceOrder == true, 0 denotes failure satisfying all event history requests
        assertEquals(0, query(requests, true))
    }

    @Test
    fun testGetEventsWithOutEnforceOrder() {
        val data = mapOf("key" to "value")
        record(data)

        val data1 = mapOf("key1" to "value1")
        record(data1)

        val requests = arrayOf(
            EventHistoryRequest(data, 0, System.currentTimeMillis()),
            EventHistoryRequest(data1, 0, System.currentTimeMillis())
        )

        assertEquals(2, query(requests, false))
    }

    @Test
    fun testGetEventsWithOutEnforceOrderOutsideRange() {
        val data = mapOf("key" to "value")
        record(data, 10000)

        val data1 = mapOf("key1" to "value1")
        record(data1, 20000)

        val requests = arrayOf(
            EventHistoryRequest(data, 20000, System.currentTimeMillis()),
            EventHistoryRequest(data1, 0, 10000)
        )

        assertEquals(0, query(requests, false))
    }

    @Test
    fun testDeleteEvents() {
        val data = mapOf("key" to "value")
        for (i in 1..5) {
            assertTrue(record(data))
        }

        val data1 = mapOf("key1" to "value1")
        for (i in 1..10) {
            assertTrue(record(data1))
        }

        val deleteRequests = arrayOf(
            EventHistoryRequest(mapOf("key" to "value"), 0, 0), // toDate is assumed to be current timestamp.
            EventHistoryRequest(mapOf("key1" to "value1"), 0, System.currentTimeMillis())
        )
        assertEquals(15, delete(deleteRequests))
    }

    @Test
    fun testDeleteEventsFailsOutsideRange() {
        val data = mapOf("key" to "value")
        for (i in 1..5) {
            assertTrue(record(data, 10000))
        }

        val deleteRequests = arrayOf(
            EventHistoryRequest(mapOf("key" to "value"), 0, 9999),
            EventHistoryRequest(mapOf("key" to "value"), 10001, System.currentTimeMillis())
        )
        assertEquals(0, delete(deleteRequests))
    }

    private fun record(data: Map<String, String>, timestamp: Long = System.currentTimeMillis()): Boolean {
        val latch = CountDownLatch(1)
        var ret = false
        val event = Event.Builder("name", "type", "source").setEventData(data).build()
        androidEventHistory.recordEvent(event.copyWithNewTimeStamp(timestamp)) {
            ret = it
            latch.countDown()
        }
        latch.await()
        return ret
    }

    private fun delete(requests: Array<out EventHistoryRequest>): Int {
        val latch = CountDownLatch(1)
        var ret = 0
        androidEventHistory.deleteEvents(requests) {
            ret = it
            latch.countDown()
        }
        latch.await()
        return ret
    }

    private fun query(requests: Array<out EventHistoryRequest>, enforceOrder: Boolean): Int {
        val latch = CountDownLatch(1)
        var ret = 0
        androidEventHistory.getEvents(requests, enforceOrder) {
            ret = it
            latch.countDown()
        }
        latch.await()
        return ret
    }

    companion object {
        private const val DATABASE_NAME = "com.adobe.module.core.eventhistory"
    }
}
