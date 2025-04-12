/*
  Copyright 2025 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.internal.eventhub.history

import com.adobe.marketing.mobile.Event
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class EventExtensionTests {

    @Test
    fun `test toEventHistoryRequest with null mask`() {
        val event = Event.Builder("Test Event", "test.type", "test.source", null)
            .setEventData(
                mapOf(
                    "key1" to "value1",
                    "key2" to "value2"
                )
            )
            .build()

        val request = event.toEventHistoryRequest()

        assertEquals(0L, request.fromDate)
        assertEquals(0L, request.toDate)
        // Note: We can't directly test the map contents since it's private,
        // but we can verify the hash is non-zero for valid data
        assert(request.maskAsDecimalHash > 0L)
    }

    @Test
    fun `test toEventHistoryRequest with empty mask`() {
        val event = Event.Builder("Test Event", "test.type", "test.source")
            .setEventData(
                mapOf(
                    "key1" to "value1",
                    "key2" to "value2"
                )
            )
            .build()

        val request = event.toEventHistoryRequest()

        assertEquals(0L, request.fromDate)
        assertEquals(0L, request.toDate)
        // Note: We can't directly test the map contents since it's private,
        // but we can verify the hash is non-zero for valid data
        assert(request.maskAsDecimalHash > 0L)
    }

    @Test
    fun `test toEventHistoryRequest with mask`() {
        val event = Event.Builder("Test Event", "test.type", "test.source", arrayOf("key1", "key3"))
            .setEventData(
                mapOf(
                    "key1" to "value1",
                    "key2" to "value2",
                    "key3" to "value3"
                )
            )
            .build()

        val request = event.toEventHistoryRequest()
        assert(request.maskAsDecimalHash > 0L)
    }

    @Test
    fun `test toEventHistoryRequest with custom time range`() {
        val event = Event.Builder("Test Event", "test.type", "test.source", arrayOf("key1", "key3"))
            .setEventData(mapOf("key1" to "value1"))
            .build()

        val from = 1000L
        val to = 2000L
        val request = event.toEventHistoryRequest(from, to)

        assertEquals(from, request.fromDate)
        assertEquals(to, request.toDate)
        assert(request.maskAsDecimalHash > 0L)
    }

    @Test
    fun `test toEventHistoryRequest with null event data`() {
        val event = Event.Builder("Test Event", "test.type", "test.source")
            .setEventData(null)
            .build()

        val request = event.toEventHistoryRequest()

        assertEquals(0L, request.maskAsDecimalHash)
        assertEquals(0L, request.fromDate)
        assertEquals(0L, request.toDate)
    }

    @Test
    fun `test toEventHistoryRequest with empty event data`() {
        val event = Event.Builder("Test Event", "test.type", "test.source")
            .build()

        val request = event.toEventHistoryRequest()

        assertEquals(0L, request.maskAsDecimalHash)
        assertEquals(0L, request.fromDate)
        assertEquals(0L, request.toDate)
    }

    @Test
    fun `test toEventHistoryRequest with nested event data`() {
        val event = Event.Builder("Test Event", "test.type", "test.source", arrayOf("parent.child1"))
            .setEventData(
                mapOf(
                    "parent" to mapOf(
                        "child1" to "value1",
                        "child2" to "value2"
                    )
                )
            )
            .build()

        val request = event.toEventHistoryRequest()
        assert(request.maskAsDecimalHash > 0L)
    }
}
