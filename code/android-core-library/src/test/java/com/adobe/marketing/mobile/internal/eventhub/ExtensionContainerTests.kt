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

@file:Suppress("DEPRECATION")

package com.adobe.marketing.mobile.internal.eventhub
import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.EventSource
import com.adobe.marketing.mobile.EventType
import com.adobe.marketing.mobile.Extension
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.ExtensionError
import com.adobe.marketing.mobile.ExtensionListener
import com.adobe.marketing.mobile.ExtensionUnexpectedError
import com.adobe.marketing.mobile.SharedStateResolver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
        var unverifiedException: ExtensionUnexpectedError? = null

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

    companion object {
        var TestExtensionNameError_UnVerifiedExtension: ExtensionUnexpectedError? = null
        var ValidExtensionListener_Events: MutableList<Event>? = null
    }
    class TestExtensionNameError(api: ExtensionApi) : Extension(api) {
        init {
            TestExtensionNameError_UnVerifiedExtension = null
        }

        override fun getName(): String {
            return ""
        }

        override fun onUnexpectedError(extensionUnexpectedError: ExtensionUnexpectedError) {
            TestExtensionNameError_UnVerifiedExtension = extensionUnexpectedError
        }
    }

    private class InvalidExtensionListener(api: ExtensionApi, type: String, source: String, additional: String) :
        ExtensionListener(api, type, source) {
        override fun hear(event: Event) {}
    }

    private class ValidExtensionListener(api: ExtensionApi, type: String, source: String) :
        ExtensionListener(api, type, source) {

        init {
            ValidExtensionListener_Events = mutableListOf()
        }
        override fun hear(event: Event) {
            ValidExtensionListener_Events?.add(event)
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
    fun testSetSharedState_PendingWithValidEvent() {
        val event1: Event = Event.Builder("Event1", "eventtype", "eventsource").build()
        container?.setSharedEventState(null, event1) {}
        verify(EventHub.shared, times(1)).createPendingSharedState(SharedStateType.STANDARD, TestExtension.EXTENSION_NAME, event1)
    }

    @Test
    fun testSetXDMSharedState_PendingWithValidEvent() {
        val event1: Event = Event.Builder("Event1", "eventtype", "eventsource").build()
        container?.setXDMSharedEventState(null, event1) {}
        verify(EventHub.shared, times(1)).createPendingSharedState(SharedStateType.XDM, TestExtension.EXTENSION_NAME, event1)
    }

    @Test
    fun testSetSharedState_PendingWithNullEvent() {
        var capturedError: ExtensionError? = null
        container?.setSharedEventState(null, null) {
            capturedError = it
        }

        assertEquals(ExtensionError.UNEXPECTED_ERROR, capturedError)
    }

    @Test
    fun testSetXDMSharedState_PendingWithNullEvent() {
        var capturedError: ExtensionError? = null
        container?.setXDMSharedEventState(null, null) {
            capturedError = it
        }

        assertEquals(ExtensionError.UNEXPECTED_ERROR, capturedError)
    }

    @Test
    fun testSetSharedState_ResolveAfterPendingState() {
        val event1: Event = Event.Builder("Event1", "eventtype", "eventsource").build()
        val state = mutableMapOf<String, Any?>("k1" to "v1")
        var resolvedState: MutableMap<String, Any?>? = null

        doAnswer {
            return@doAnswer SharedStateResolver {
                resolvedState = it
            }
        }.`when`(EventHub.shared).createPendingSharedState(SharedStateType.STANDARD, TestExtension.EXTENSION_NAME, event1)

        container?.setSharedEventState(null, event1) {}
        container?.setSharedEventState(state, event1) {}

        assertEquals(state, resolvedState)
    }

    @Test
    fun testSetXDMSharedState_ResolveAfterPendingState() {
        val event1: Event = Event.Builder("Event1", "eventtype", "eventsource").build()
        val state = mutableMapOf<String, Any?>("k1" to "v1")
        var resolvedState: MutableMap<String, Any?>? = null

        doAnswer {
            return@doAnswer SharedStateResolver {
                resolvedState = it
            }
        }.`when`(EventHub.shared).createPendingSharedState(SharedStateType.XDM, TestExtension.EXTENSION_NAME, event1)

        container?.setXDMSharedEventState(null, event1) {}
        container?.setXDMSharedEventState(state, event1) {}

        assertEquals(state, resolvedState)
    }

    @Test
    fun testSetSharedState_ValidStateWithNonNullEvent() {
        val event1: Event = Event.Builder("Event1", "eventtype", "eventsource").build()
        val state1 = mutableMapOf<String, Any?>("k1" to "v1")
        container?.setSharedEventState(state1, event1) {}
        verify(EventHub.shared, times(1)).createSharedState(SharedStateType.STANDARD, TestExtension.EXTENSION_NAME, state1, event1)
    }

    @Test
    fun testSetXDMSharedState_ValidStateWithNonNullEvent() {
        val event1: Event = Event.Builder("Event1", "eventtype", "eventsource").build()
        val state1 = mutableMapOf<String, Any?>("k1" to "v1")
        container?.setXDMSharedEventState(state1, event1) {}
        verify(EventHub.shared, times(1)).createSharedState(SharedStateType.XDM, TestExtension.EXTENSION_NAME, state1, event1)
    }

    @Test
    fun testSetSharedState_ValidStateWithNullEvent() {
        val state1 = mutableMapOf<String, Any?>("k1" to "v1")
        container?.setSharedEventState(state1, null) {}
        verify(EventHub.shared, times(1)).createSharedState(SharedStateType.STANDARD, TestExtension.EXTENSION_NAME, state1, null)
    }

    @Test
    fun testSetXDMSharedState_ValidStateWithNullEvent() {
        val state1 = mutableMapOf<String, Any?>("k1" to "v1")
        container?.setXDMSharedEventState(state1, null) {}
        verify(EventHub.shared, times(1)).createSharedState(SharedStateType.XDM, TestExtension.EXTENSION_NAME, state1, null)
    }

    @Test
    fun testRegisterEventListener_InvalidListener() {
        var error: ExtensionError? = null
        var ret = container?.registerEventListener("eventtype", "eventsource", InvalidExtensionListener::class.java) {
            error = it
        }
        assertFalse { ret ?: true }
        assertEquals(error, ExtensionError.UNEXPECTED_ERROR)

        ret = container?.registerEventListener(null, "eventsource", InvalidExtensionListener::class.java) {
            error = it
        }
        assertFalse { ret ?: true }
        assertEquals(error, ExtensionError.UNEXPECTED_ERROR)

        ret = container?.registerEventListener("eventtype", null, InvalidExtensionListener::class.java) {
            error = it
        }
        assertFalse { ret ?: true }
        assertEquals(error, ExtensionError.UNEXPECTED_ERROR)

        ret = container?.registerEventListener<ExtensionListener>("eventtype", "eventsource", null) {
            error = it
        }
        assertFalse { ret ?: true }
        assertEquals(error, ExtensionError.UNEXPECTED_ERROR)
    }

    @Test
    fun testRegisterEventListener_ValidListener() {
        var ret = container?.registerEventListener(
            "eventtype",
            "eventsource",
            ValidExtensionListener::class.java
        ) {}
        assertTrue { ret ?: false }

        val event = Event.Builder("eventname", "eventtype", "eventsource").build()
        container?.eventProcessor?.start()
        container?.eventProcessor?.offer(event)
        Thread.sleep(100)
        assertEquals(mutableListOf(event), ValidExtensionListener_Events)
    }

    @Test
    fun testRegisterWildCardListener_InvalidListener() {
        var error: ExtensionError? = null
        var ret = container?.registerWildcardListener(InvalidExtensionListener::class.java) {
            error = it
        }
        assertFalse { ret ?: true }
        assertEquals(error, ExtensionError.UNEXPECTED_ERROR)

        ret = container?.registerWildcardListener<ExtensionListener>(null) {
            error = it
        }
        assertFalse { ret ?: true }
        assertEquals(error, ExtensionError.UNEXPECTED_ERROR)
    }

    @Test
    fun testRegisterWildCardListener_ValidListener() {
        var ret = container?.registerWildcardListener(ValidExtensionListener::class.java) {}
        assertTrue { ret ?: false }

        val event1 = Event.Builder("eventname", "eventtype", "eventsource").build()
        val event2 = Event.Builder("eventname1", "eventtype1", "eventsource1").build()
        container?.eventProcessor?.start()
        container?.eventProcessor?.offer(event1)
        container?.eventProcessor?.offer(event2)
        Thread.sleep(100)
        assertEquals(mutableListOf(event1, event2), ValidExtensionListener_Events)
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
        assertEquals(TestExtensionNameError_UnVerifiedExtension?.errorCode, ExtensionError.BAD_NAME)
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
