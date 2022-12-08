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

package com.adobe.marketing.mobile.internal.eventhub

import com.adobe.marketing.mobile.AdobeCallbackWithError
import com.adobe.marketing.mobile.AdobeError
import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.EventSource
import com.adobe.marketing.mobile.EventType
import org.junit.Test
import java.lang.Exception
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

internal class EventListenerContainerTests {

    val eventType = "eventtype"
    val eventSource = "eventsource"

    @Test
    fun testResponseListener_MatchingTrigger() {
        val testEvent = Event.Builder("Test event", eventType, eventSource).build()
        val listener = ResponseListenerContainer(
            testEvent.uniqueIdentifier,
            null,
            object : AdobeCallbackWithError<Event> {
                override fun call(value: Event?) {}
                override fun fail(error: AdobeError?) {}
            }
        )
        val testResponseEvent = Event.Builder("Test response event", eventType, eventSource).inResponseToEvent(testEvent).build()
        assertTrue { listener.shouldNotify(testResponseEvent) }
    }

    @Test
    fun testResponseListener_ListenerException() {
        val testEvent = Event.Builder("Test event", eventType, eventSource).build()
        val listener = ResponseListenerContainer(
            testEvent.uniqueIdentifier,
            null,
            object : AdobeCallbackWithError<Event> {
                override fun call(value: Event?) {
                    throw Exception()
                }

                override fun fail(error: AdobeError?) {
                    throw Exception()
                }
            }
        )

        try {
            listener.notify(testEvent)
        } catch (ex: Exception) {
            fail()
        }
    }

    @Test
    fun testEventListener_MatchingTypeSource() {
        val listener = ExtensionListenerContainer(eventType, eventSource) {}

        val testEvent = Event.Builder("Test event", eventType, eventSource).build()
        assertTrue { listener.shouldNotify(testEvent) }

        val testEvent1 = Event.Builder("Test event 1", "customType", eventSource).build()
        assertFalse { listener.shouldNotify(testEvent1) }
    }

    @Test
    fun testEventListener_WildcardListener() {
        val listener = ExtensionListenerContainer(EventType.WILDCARD, EventSource.WILDCARD) {}

        val testEvent = Event.Builder("Test event", eventType, eventSource).build()
        assertTrue { listener.shouldNotify(testEvent) }

        val testEvent1 = Event.Builder("Test event 1", "customType", eventSource).build()
        assertTrue { listener.shouldNotify(testEvent1) }
    }

    @Test
    fun testEventListener_NotTriggerForResponseEvent() {
        val listener = ExtensionListenerContainer(eventType, eventSource) {}

        val testEvent = Event.Builder("Test event", eventType, eventSource).build()
        val testResponseEvent = Event.Builder("Test response event", eventType, eventSource).inResponseToEvent(testEvent).build()

        assertFalse { listener.shouldNotify(testResponseEvent) }
    }

    @Test
    fun testEventListener_WildcardTriggerForResponseEvent() {
        val listener = ExtensionListenerContainer(EventType.WILDCARD, EventSource.WILDCARD) {}

        val testEvent = Event.Builder("Test event", eventType, eventSource).build()
        val testResponseEvent = Event.Builder("Test response event", eventType, eventSource).inResponseToEvent(testEvent).build()

        assertTrue { listener.shouldNotify(testResponseEvent) }
    }

    @Test
    fun testEventListener_HandleListenerException() {
        val listener = ExtensionListenerContainer(EventType.WILDCARD, EventSource.WILDCARD) {
            throw Exception()
        }

        val testEvent = Event.Builder("Test event", eventType, eventSource).build()

        try {
            listener.notify(testEvent)
        } catch (ex: Exception) {
            fail()
        }
    }
}
