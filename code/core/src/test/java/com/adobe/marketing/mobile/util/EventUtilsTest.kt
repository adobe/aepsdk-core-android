/*
  Copyright 2024 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.util

import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.EventSource
import com.adobe.marketing.mobile.EventType
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.test.assertEquals

class EventUtilsTest {
    companion object {
        private const val TEST_EVENT_NAME = "testName"
        private const val TEST_EVENT_TYPE = EventType.SYSTEM
        private const val TEST_EVENT_SOURCE = EventSource.DEBUG
    }

    @Test
    fun `Test getDebugEventType returns null on non debug event`() {
        val event = Event.Builder(TEST_EVENT_NAME, EventType.HUB, EventSource.REQUEST_CONTENT).build()

        assertNull(event.getDebugEventType())
    }

    @Test
    fun `Test getDebugEventType returns null on no eventData`() {
        val event = Event.Builder(TEST_EVENT_NAME, TEST_EVENT_TYPE, TEST_EVENT_SOURCE).build()

        assertNull(event.getDebugEventType())
    }

    @Test
    fun `Test getDebugEventType returns null on no debug data`() {
        val event = Event.Builder(TEST_EVENT_NAME, TEST_EVENT_TYPE, TEST_EVENT_SOURCE)
            .setEventData(mapOf("key" to "value")).build()

        assertNull(event.getDebugEventType())
    }

    @Test
    fun `Test getDebugEventType returns null when debug is not a map`() {
        val event = Event.Builder(TEST_EVENT_NAME, TEST_EVENT_TYPE, TEST_EVENT_SOURCE)
            .setEventData(mapOf("debug" to "value")).build()

        assertNull(event.getDebugEventType())
    }

    @Test
    fun `Test getDebugEventType returns null on invalid debug key`() {
        val event = Event.Builder(TEST_EVENT_NAME, TEST_EVENT_TYPE, TEST_EVENT_SOURCE)
            .setEventData(
                mapOf(
                    "_debug" to mapOf(
                        "eventType" to EventType.RULES_ENGINE,
                        "eventSource" to EventSource.RESET_COMPLETE
                    )
                )
            ).build()

        assertNull(event.getDebugEventType())
    }

    @Test
    fun `Test getDebugEventType returns null when key is absent`() {
        val event = Event.Builder(TEST_EVENT_NAME, TEST_EVENT_TYPE, TEST_EVENT_SOURCE)
            .setEventData(
                mapOf(
                    "debug" to mapOf(
                        "eventSource" to EventSource.RESET_COMPLETE
                    )
                )
            ).build()

        assertNull(event.getDebugEventType())
    }

    @Test
    fun `Test getDebugEventType returns debug event type`() {
        val event = Event.Builder(TEST_EVENT_NAME, TEST_EVENT_TYPE, TEST_EVENT_SOURCE)
            .setEventData(
                mapOf(
                    "debug" to mapOf(
                        "eventType" to EventType.RULES_ENGINE,
                        "eventSource" to EventSource.RESET_COMPLETE
                    )
                )
            ).build()

        assertEquals(EventType.RULES_ENGINE, event.getDebugEventType())
    }

    @Test
    fun `Test getDebugEventType returns null when debugEvent type is not a string`() {
        val event = Event.Builder(TEST_EVENT_NAME, TEST_EVENT_TYPE, TEST_EVENT_SOURCE)
            .setEventData(
                mapOf(
                    "debug" to mapOf(
                        "eventType" to mapOf("1" to "Hi"),
                        "eventSource" to "testEventSource"
                    )
                )
            ).build()

        assertNull(event.getDebugEventType())
    }

    @Test
    fun `Test getDebugEventSource returns null on non debug event`() {
        val event = Event.Builder(TEST_EVENT_NAME, EventType.HUB, EventSource.REQUEST_CONTENT).build()

        assertNull(event.getDebugEventSource())
    }

    @Test
    fun `Test getDebugEventSource returns null on no eventData`() {
        val event = Event.Builder(TEST_EVENT_NAME, TEST_EVENT_TYPE, TEST_EVENT_SOURCE).build()

        assertNull(event.getDebugEventSource())
    }

    @Test
    fun `Test getDebugEventSource returns null when debug is not a map`() {
        val event = Event.Builder(TEST_EVENT_NAME, TEST_EVENT_TYPE, TEST_EVENT_SOURCE)
            .setEventData(mapOf("debug" to "value")).build()

        assertNull(event.getDebugEventSource())
    }

    @Test
    fun `Test getDebugEventSource returns null on no debug data`() {
        val event = Event.Builder(TEST_EVENT_NAME, TEST_EVENT_TYPE, TEST_EVENT_SOURCE)
            .setEventData(mapOf("key" to "value")).build()

        assertNull(event.getDebugEventSource())
    }

    @Test
    fun `Test getDebugEventSource returns null on invalid debug key`() {
        val event = Event.Builder(TEST_EVENT_NAME, TEST_EVENT_TYPE, TEST_EVENT_SOURCE)
            .setEventData(
                mapOf(
                    "_debug" to mapOf(
                        "eventType" to EventType.RULES_ENGINE,
                        "eventSource" to EventSource.RESET_COMPLETE
                    )
                )
            ).build()

        assertNull(event.getDebugEventSource())
    }

    @Test
    fun `Test getDebugEventSource returns debug event source`() {
        val event = Event.Builder(TEST_EVENT_NAME, TEST_EVENT_TYPE, TEST_EVENT_SOURCE)
            .setEventData(
                mapOf(
                    "debug" to mapOf(
                        "eventType" to EventType.RULES_ENGINE,
                        "eventSource" to EventSource.RESET_COMPLETE
                    )
                )
            ).build()

        assertEquals(EventSource.RESET_COMPLETE, event.getDebugEventSource())
    }

    @Test
    fun `Test getDebugEventSource returns null when debugEvent source is not a string`() {
        val event = Event.Builder(TEST_EVENT_NAME, TEST_EVENT_TYPE, TEST_EVENT_SOURCE)
            .setEventData(
                mapOf(
                    "debug" to mapOf(
                        "eventType" to "testEventType",
                        "eventSource" to mapOf("1" to "Hi")
                    )
                )
            ).build()

        assertNull(event.getDebugEventSource())
    }

    @Test
    fun `Test getDebugEventSource returns null when key is absent`() {
        val event = Event.Builder(TEST_EVENT_NAME, TEST_EVENT_TYPE, TEST_EVENT_SOURCE)
            .setEventData(
                mapOf(
                    "debug" to mapOf(
                        "eventType" to EventType.RULES_ENGINE
                    )
                )
            ).build()

        assertNull(event.getDebugEventSource())
    }
}
