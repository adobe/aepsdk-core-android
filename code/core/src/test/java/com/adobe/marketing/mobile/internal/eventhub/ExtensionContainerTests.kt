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
import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.EventSource
import com.adobe.marketing.mobile.EventType
import com.adobe.marketing.mobile.Extension
import com.adobe.marketing.mobile.ExtensionApi
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner.Silent::class)
internal class ExtensionContainerTests {
    private class TestExtension(api: ExtensionApi) : Extension(api) {
        companion object {
            const val VERSION = "0.1"
            const val EXTENSION_NAME = "TestExtension"
            const val FRIENDLY_NAME = "FriendlyTestExtension"
        }

        var registerCalled = false
        var unregisterCalled = false

        override fun getName(): String {
            return EXTENSION_NAME
        }

        override fun onRegistered() {
            registerCalled = true
        }

        override fun onUnregistered() {
            unregisterCalled = true
        }
    }

    class TestExtensionNameError(api: ExtensionApi) : Extension(api) {
        override fun getName(): String {
            return ""
        }
    }

    private var container: ExtensionContainer? = null

    @Mock
    private lateinit var eventHub: EventHub

    @Before
    fun setup() {
        EventHub.shared = eventHub

        container = ExtensionContainer(TestExtension::class.java) {}
        Thread.sleep(100)
    }

    @Test
    fun testExtensionCallback_SuccessfulRegistration() {
        var error: EventHubError? = null
        container = ExtensionContainer(
            TestExtension::class.java
        ) { error = it }
        Thread.sleep(100)
        assertTrue { (container?.extension as TestExtension).registerCalled }
        assertEquals(error, EventHubError.None)
    }

    @Test
    fun testExtensionCallback_FailedRegistration_InitException() {
        var error: EventHubError? = null
        container = ExtensionContainer(
            MockExtensions.MockExtensionInvalidConstructor::class.java
        ) { error = it }
        Thread.sleep(100)
        assertEquals(error, EventHubError.ExtensionInitializationFailure)
    }

    @Test
    fun testExtensionCallback_FailedRegistration_InvalidConstructor() {
        var error: EventHubError? = null
        container = ExtensionContainer(
            MockExtensions.MockExtensionInitFailure::class.java
        ) { error = it }
        Thread.sleep(100)
        assertEquals(error, EventHubError.ExtensionInitializationFailure)
    }

    @Test
    fun testExtensionCallback_FailedRegistration_EmptyName() {
        var error: EventHubError? = null
        container = ExtensionContainer(
            TestExtensionNameError::class.java
        ) { error = it }
        Thread.sleep(100)
        assertEquals(EventHubError.InvalidExtensionName, error)
    }

    @Test
    fun testExtensionCallback_Shutdown() {
        container?.shutdown()
        Thread.sleep(100)
        assertTrue { (container?.extension as TestExtension)?.unregisterCalled ?: false }
    }

    @Test
    fun testStopEvents_shouldStopProcessingEvents() {
        var capturedEvents = mutableListOf<Event>()
        container?.registerEventListener(EventType.WILDCARD, EventSource.WILDCARD) {
            capturedEvents.add(it)
        }

        val event1: Event = Event.Builder("Event1", "eventtype", "eventsource").build()
        val event2: Event = Event.Builder("Event1", "eventtype", "eventsource").build()
        container?.eventProcessor?.offer(event1)
        container?.eventProcessor?.offer(event2)
        Thread.sleep(100)
        assertEquals(mutableListOf(event1, event2), capturedEvents)

        container?.stopEvents()
        Thread.sleep(100)

        val event3: Event = Event.Builder("Event1", "eventtype", "eventsource").build()
        container?.eventProcessor?.offer(event3)
        Thread.sleep(100)

        assertEquals(mutableListOf(event1, event2), capturedEvents)
    }

    @Test
    fun testStartEvents_shouldResumeProcessingEvents() {
        var capturedEvents = mutableListOf<Event>()
        container?.registerEventListener(EventType.WILDCARD, EventSource.WILDCARD) {
            capturedEvents.add(it)
        }

        container?.stopEvents()

        val event1: Event = Event.Builder("Event1", "eventtype", "eventsource").build()
        val event2: Event = Event.Builder("Event1", "eventtype", "eventsource").build()
        container?.eventProcessor?.offer(event1)
        container?.eventProcessor?.offer(event2)
        Thread.sleep(100)
        assertEquals(mutableListOf(), capturedEvents)

        container?.startEvents()
        Thread.sleep(100)
        assertEquals(mutableListOf(event1, event2), capturedEvents)
    }
}
