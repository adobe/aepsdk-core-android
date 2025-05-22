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
import com.adobe.marketing.mobile.AdobeCallbackWithError
import com.adobe.marketing.mobile.AdobeError
import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.EventHistoryRequest
import com.adobe.marketing.mobile.EventHistoryResult
import com.adobe.marketing.mobile.TestUtils
import com.adobe.marketing.mobile.copyWithNewTimeStamp
import com.adobe.marketing.mobile.services.MockAppContextService
import com.adobe.marketing.mobile.services.ServiceProviderModifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class AndroidEventHistoryTests {

    private lateinit var androidEventHistory: AndroidEventHistory
    private lateinit var context: Context
    private lateinit var databaseFile: File

    init {
        context = ApplicationProvider.getApplicationContext()
        val mockAppContextService = MockAppContextService().apply {
            appContext = context
        }
        ServiceProviderModifier.setAppContextService(mockAppContextService)
        databaseFile = context.getDatabasePath(DATABASE_NAME)
        deleteDatabase()
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
    fun testRecordEventDeletedDatabase() {
        val data = mapOf("key" to "value")
        if (deleteDatabase()) {
            val latch = CountDownLatch(1)
            androidEventHistory.recordEvent(
                Event.Builder("name", "type", "source").setEventData(data).build(),
                object : AdobeCallbackWithError<Boolean> {
                    override fun call(result: Boolean) {
                        // Should not be called
                    }

                    override fun fail(error: AdobeError) {
                        assertEquals(AdobeError.DATABASE_ERROR.errorCode, error.errorCode)
                        latch.countDown()
                    }
                }
            )
            assertTrue(latch.await(5, java.util.concurrent.TimeUnit.SECONDS))
        }
    }

    @Test
    fun testRecordEventDatabaseError() {
        val data = mapOf("key" to "value")

        // Corrupt the database to force errors
        corruptDatabase()

        val latch = CountDownLatch(1)
        androidEventHistory.recordEvent(
            Event.Builder("name", "type", "source").setEventData(data).build(),
            object : AdobeCallbackWithError<Boolean> {
                override fun call(result: Boolean) {
                    // Should not be called
                }

                override fun fail(error: AdobeError) {
                    assertEquals(AdobeError.DATABASE_ERROR.errorCode, error.errorCode)
                    latch.countDown()
                }
            }
        )
        assertTrue(latch.await(5, java.util.concurrent.TimeUnit.SECONDS))
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

        val results = query(requests, true)
        assertEquals(requests.size, results.size)
        for (result in results) {
            assertEquals(1, result.count)
        }
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

        val results = query(requests, true)
        assertEquals(requests.size, results.size)
        for (result in results) {
            assertEquals(1, result.count)
        }
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

        val results = query(requests, true)
        assertEquals(requests.size, results.size)
        assertEquals(3, results[0].count)
        assertEquals(1, results[1].count)
        assertEquals(1, results[2].count)
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

        val results = query(requests, true)
        assertEquals(requests.size, results.size)
        assertEquals(3, results[0].count)
        assertEquals(1, results[1].count)
        assertEquals(0, results[2].count)
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
        val results = query(requests, true)
        assertEquals(requests.size, results.size)
        assertEquals(1, results[0].count)
        assertEquals(0, results[1].count)
    }

    @Test
    fun testGetEventsWithEnforceDeletedDatabase() {
        val data = mapOf("key" to "value")
        assertTrue(record(data))

        val data1 = mapOf("key1" to "value1")
        assertTrue(record(data1))

        if (deleteDatabase()) {
            val latch = CountDownLatch(1)

            val requests = arrayOf(
                EventHistoryRequest(data, 0, 0), // toDate is assumed to be current timestamp.
                EventHistoryRequest(data1, 0, System.currentTimeMillis())
            )

            androidEventHistory.getEvents(
                requests, true,
                object : AdobeCallbackWithError<Array<EventHistoryResult>> {
                    override fun call(result: Array<EventHistoryResult>) {
                        fail()
                    }

                    override fun fail(error: AdobeError) {
                        assertEquals(AdobeError.DATABASE_ERROR.errorCode, error.errorCode)
                        latch.countDown()
                    }
                }
            )
            assertTrue(latch.await(5, java.util.concurrent.TimeUnit.SECONDS))
        }
    }

    @Test
    fun testGetEventsWithEnforceOrderDatabaseError() {
        val data = mapOf("key" to "value")

        // First record succeeds
        assertTrue(record(data))

        // Corrupt the database to force errors
        corruptDatabase()

        val latch = CountDownLatch(1)
        val requests = arrayOf(
            EventHistoryRequest(data, 0, System.currentTimeMillis()),
        )

        androidEventHistory.getEvents(
            requests, true,
            object : AdobeCallbackWithError<Array<EventHistoryResult>> {
                override fun call(result: Array<EventHistoryResult>) {
                    fail()
                }

                override fun fail(error: AdobeError) {
                    assertEquals(AdobeError.DATABASE_ERROR.errorCode, error.errorCode)
                    latch.countDown()
                }
            }
        )
        assertTrue(latch.await(50, java.util.concurrent.TimeUnit.SECONDS))
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

        val results = query(requests, false)
        assertEquals(requests.size, results.size)
        for (result in results) {
            assertEquals(1, result.count)
        }
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

        val results = query(requests, false)
        assertEquals(requests.size, results.size)
        for (result in results) {
            assertEquals(0, result.count)
        }
    }

    @Test
    fun testGetEventsWithoutEnforceDeletedDatabase() {
        val data = mapOf("key" to "value")
        assertTrue(record(data))

        val data1 = mapOf("key1" to "value1")
        assertTrue(record(data1))

        if (deleteDatabase()) {
            val requests = arrayOf(
                EventHistoryRequest(data, 0, System.currentTimeMillis()),
                EventHistoryRequest(data1, 0, System.currentTimeMillis())
            )

            val latch = CountDownLatch(1)
            var results = emptyArray<EventHistoryResult>()
            androidEventHistory.getEvents(
                requests, false,
                object : AdobeCallbackWithError<Array<EventHistoryResult>> {
                    override fun call(result: Array<EventHistoryResult>) {
                        results = result
                        latch.countDown()
                    }

                    override fun fail(error: AdobeError) {
                        // Should not be called
                    }
                }
            )
            assertTrue(latch.await(5, java.util.concurrent.TimeUnit.SECONDS))
            assertEquals(requests.size, results.size)
            for (result in results) {
                assertEquals(-1, result.count)
            }
        }
    }

    @Test
    fun testGetEventsWithoutEnforceOrderDatabaseError() {
        val data = mapOf("key" to "value")
        assertTrue(record(data))

        val data1 = mapOf("key1" to "value1")
        assertTrue(record(data1))

        // Corrupt the database to force errors
        corruptDatabase()

        val requests = arrayOf(
            EventHistoryRequest(data, 0, System.currentTimeMillis()),
            EventHistoryRequest(data1, 0, System.currentTimeMillis())
        )

        val latch = CountDownLatch(1)
        var results = emptyArray<EventHistoryResult>()
        androidEventHistory.getEvents(
            requests, false,
            object : AdobeCallbackWithError<Array<EventHistoryResult>> {
                override fun call(result: Array<EventHistoryResult>) {
                    results = result
                    latch.countDown()
                }

                override fun fail(error: AdobeError) {
                    // Should not be called
                }
            }
        )
        assertTrue(latch.await(5, java.util.concurrent.TimeUnit.SECONDS))
        assertEquals(requests.size, results.size)
        for (result in results) {
            assertEquals(-1, result.count)
        }
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

    @Test
    fun testDeleteEventsDeletedDatabase() {
        val data = mapOf("key" to "value")
        for (i in 1..5) {
            assertTrue(record(data))
        }

        if (deleteDatabase()) {
            val latch = CountDownLatch(1)
            val deleteRequests = arrayOf(EventHistoryRequest(mapOf("key" to "value"), 0, System.currentTimeMillis()))
            androidEventHistory.deleteEvents(
                deleteRequests,
                object : AdobeCallbackWithError<Int> {
                    override fun call(result: Int) {
                        // Should not be called
                    }

                    override fun fail(error: AdobeError) {
                        assertEquals(AdobeError.DATABASE_ERROR.errorCode, error.errorCode)
                        latch.countDown()
                    }
                }
            )
            assertTrue(latch.await(5, java.util.concurrent.TimeUnit.SECONDS))
        }
    }

    @Test
    fun testDeleteEventsDatabaseError() {
        val data = mapOf("key" to "value")
        for (i in 1..5) {
            assertTrue(record(data))
        }

        // Corrupt the database to force errors
        corruptDatabase()

        val latch = CountDownLatch(1)
        val deleteRequests =
            arrayOf(EventHistoryRequest(mapOf("key" to "value"), 0, System.currentTimeMillis()))
        androidEventHistory.deleteEvents(
            deleteRequests,
            object : AdobeCallbackWithError<Int> {
                override fun call(result: Int) {
                    // Should not be called
                }

                override fun fail(error: AdobeError) {
                    assertEquals(AdobeError.DATABASE_ERROR.errorCode, error.errorCode)
                    latch.countDown()
                }
            }
        )
        assertTrue(latch.await(5, java.util.concurrent.TimeUnit.SECONDS))
    }

    private fun record(data: Map<String, String>, timestamp: Long = System.currentTimeMillis()): Boolean {
        val latch = CountDownLatch(1)
        var ret = false
        val event = Event.Builder("name", "type", "source").setEventData(data).build()
        androidEventHistory.recordEvent(
            event.copyWithNewTimeStamp(timestamp),
            object : AdobeCallbackWithError<Boolean> {
                override fun call(result: Boolean) {
                    ret = result
                    latch.countDown()
                }

                override fun fail(error: AdobeError) {
                    latch.countDown()
                }
            }
        )
        latch.await()
        return ret
    }

    private fun delete(requests: Array<out EventHistoryRequest>): Int {
        val latch = CountDownLatch(1)
        var ret = 0
        androidEventHistory.deleteEvents(
            requests,
            object : AdobeCallbackWithError<Int> {
                override fun call(result: Int) {
                    ret = result
                    latch.countDown()
                }

                override fun fail(error: AdobeError) {
                    latch.countDown()
                }
            }
        )
        latch.await()
        return ret
    }

    private fun query(requests: Array<out EventHistoryRequest>, enforceOrder: Boolean): Array<EventHistoryResult> {
        val latch = CountDownLatch(1)
        var ret = emptyArray<EventHistoryResult>()
        androidEventHistory.getEvents(
            requests, enforceOrder,
            object : AdobeCallbackWithError<Array<EventHistoryResult>> {
                override fun call(result: Array<EventHistoryResult>) {
                    ret = result
                    latch.countDown()
                }

                override fun fail(error: AdobeError) {
                    latch.countDown()
                }
            }
        )
        latch.await()
        return ret
    }

    private fun deleteDatabase(): Boolean {
        TestUtils.deleteAllFilesInCacheDir(context)
        return databaseFile.delete()
    }

    private fun corruptDatabase() {
        // Write invalid data to corrupt the database
        databaseFile.writeBytes(ByteArray(10) { 0 })
    }

    companion object {
        private const val DATABASE_NAME = "com.adobe.module.core.eventhistory"
    }
}
