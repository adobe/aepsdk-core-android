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
import com.adobe.marketing.mobile.EventHistoryRequest
import com.adobe.marketing.mobile.EventHistoryResult
import com.adobe.marketing.mobile.EventSource
import com.adobe.marketing.mobile.EventType
import com.adobe.marketing.mobile.Extension
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.internal.eventhub.history.EventHistory
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner.Silent::class)
internal class ExtensionContainerTests {
    private class TestExtension(api: ExtensionApi) : Extension(api) {
        companion object {
            const val EXTENSION_NAME = "TestExtension"
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

    private class TestExtension_NameError(api: ExtensionApi) : Extension(api) {
        override fun getName(): String {
            return ""
        }
    }

    private class TestExtension_InitError(api: ExtensionApi) : Extension(api) {
        companion object {
            const val EXTENSION_NAME = "TestExtension_InitError"
        }

        init {
            throw Exception()
        }

        override fun getName(): String {
            return EXTENSION_NAME
        }
    }

    private class TestExtension_InvalidConstructor(api: ExtensionApi, private val extensionName: String?) : Extension(api) {
        companion object {
            const val EXTENSION_NAME = "TestExtension_InvalidConstructor"
        }

        override fun getName(): String {
            return EXTENSION_NAME
        }
    }

    private var container: ExtensionContainer? = null

    @Mock
    private lateinit var eventHub: EventHub

    @Mock
    private lateinit var eventHistory: EventHistory

    @Before
    fun setup() {
        EventHub.shared = eventHub
        `when`(eventHub.eventHistory).thenReturn(eventHistory)

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
            TestExtension_InvalidConstructor::class.java
        ) { error = it }
        Thread.sleep(100)
        assertEquals(error, EventHubError.ExtensionInitializationFailure)
    }

    @Test
    fun testExtensionCallback_FailedRegistration_InvalidConstructor() {
        var error: EventHubError? = null
        container = ExtensionContainer(
            TestExtension_InitError::class.java
        ) { error = it }
        Thread.sleep(100)
        assertEquals(error, EventHubError.ExtensionInitializationFailure)
    }

    @Test
    fun testExtensionCallback_FailedRegistration_EmptyName() {
        var error: EventHubError? = null
        container = ExtensionContainer(
            TestExtension_NameError::class.java
        ) { error = it }
        Thread.sleep(100)
        assertEquals(EventHubError.InvalidExtensionName, error)
    }

    @Test
    fun testExtensionCallback_Shutdown() {
        container?.shutdown()
        Thread.sleep(100)
        assertTrue { (container?.extension as TestExtension).unregisterCalled }
    }

    @Test
    fun testStopEvents_shouldStopProcessingEvents() {
        val capturedEvents = mutableListOf<Event>()
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
        val capturedEvents = mutableListOf<Event>()
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

    @Test
    fun testGetHistoricalEvents_whenEventHistoryAvailable() {
        val requests = arrayOf(EventHistoryRequest(mapOf("key" to "value"), 0, System.currentTimeMillis()))
        val enforceOrder = true
        val result = arrayOf(EventHistoryResult(10))

        val latch = CountDownLatch(1)
        var callbackResult: Array<EventHistoryResult>? = null
        val callback = object : AdobeCallbackWithError<Array<EventHistoryResult>> {
            override fun call(value: Array<EventHistoryResult>) {
                callbackResult = value
                latch.countDown()
            }

            override fun fail(error: AdobeError) {
                // Should not be called in this test
                kotlin.test.fail()
            }
        }

        // Configure the mock to call the success callback
        `when`(eventHistory.getEvents(requests, enforceOrder, callback)).thenAnswer { invocation ->
            val callback = invocation.getArgument<AdobeCallbackWithError<Array<EventHistoryResult>>>(2)
            callback.call(result)
            null
        }

        container?.getHistoricalEvents(requests, enforceOrder, callback)

        assertTrue(latch.await(1, TimeUnit.SECONDS))
        assertEquals(result, callbackResult)
        verify(eventHistory).getEvents(requests, enforceOrder, callback)
    }

    @Test
    fun testGetHistoricalEvents_whenEventHistoryNull() {
        // Set eventHistory to null
        `when`(eventHub.eventHistory).thenReturn(null)

        val requests = arrayOf(EventHistoryRequest(mapOf("key" to "value"), 0, System.currentTimeMillis()))
        val enforceOrder = true

        val latch = CountDownLatch(1)
        var callbackResult: Array<EventHistoryResult>? = null

        container?.getHistoricalEvents(
            requests, enforceOrder,
            object : AdobeCallbackWithError<Array<EventHistoryResult>> {
                override fun call(value: Array<EventHistoryResult>) {
                    // Should not be called in this test
                    kotlin.test.fail()
                }

                override fun fail(error: AdobeError) {
                    assertEquals(AdobeError.UNEXPECTED_ERROR.errorCode, error.errorCode)
                    latch.countDown()
                }
            }
        )

        assertTrue(latch.await(1, TimeUnit.SECONDS))
    }

    @Test
    fun testGetHistoricalEvents_whenEventHistoryError() {
        val requests = arrayOf(EventHistoryRequest(mapOf("key" to "value"), 0, System.currentTimeMillis()))
        val enforceOrder = true
        val result = arrayOf(EventHistoryResult(10))

        val latch = CountDownLatch(1)
        val callback = object : AdobeCallbackWithError<Array<EventHistoryResult>> {
            override fun call(value: Array<EventHistoryResult>) {
                // Should not be called in this test
                kotlin.test.fail()
            }

            override fun fail(error: AdobeError) {
                assertEquals(AdobeError.DATABASE_ERROR.errorCode, error.errorCode)
                latch.countDown()
            }
        }

        // Configure the mock to call the fail callback
        `when`(eventHistory.getEvents(requests, enforceOrder, callback)).thenAnswer { invocation ->
            val callback = invocation.getArgument<AdobeCallbackWithError<Array<EventHistoryResult>>>(2)
            callback.fail(AdobeError.DATABASE_ERROR)
            null
        }

        container?.getHistoricalEvents(requests, enforceOrder, callback)

        assertTrue(latch.await(1, TimeUnit.SECONDS))
    }

    @Test
    fun testRecordHistoricalEvent_whenEventHistoryAvailable() {
        val event = Event.Builder("TestEvent", "eventtype", "eventsource").build()
        val latch = CountDownLatch(1)
        var callbackResult = false
        val adobeCallback = object : AdobeCallbackWithError<Boolean> {
            override fun call(value: Boolean) {
                callbackResult = value
                latch.countDown()
            }

            override fun fail(error: AdobeError) {
                // Should not be called in this test
                kotlin.test.fail()
            }
        }

        // Configure the mock to call the success callback
        `when`(eventHistory.recordEvent(event, adobeCallback)).thenAnswer { invocation ->
            val callback = invocation.getArgument<AdobeCallbackWithError<Boolean>>(1)
            callback.call(true)
            null
        }

        container?.recordHistoricalEvent(event, adobeCallback)

        assertTrue(latch.await(1, TimeUnit.SECONDS))
        assertTrue(callbackResult)
        verify(eventHistory).recordEvent(event, adobeCallback)
    }

    @Test
    fun testRecordHistoricalEvent_whenEventHistoryNull() {
        // Set eventHistory to null
        `when`(eventHub.eventHistory).thenReturn(null)

        val event = Event.Builder("TestEvent", "eventtype", "eventsource").build()

        val latch = CountDownLatch(1)
        var callbackResult = true

        container?.recordHistoricalEvent(
            event,
            object : AdobeCallbackWithError<Boolean> {
                override fun call(value: Boolean) {
                    // Should not be called in this test
                    kotlin.test.fail()
                }

                override fun fail(error: AdobeError) {
                    assertEquals(AdobeError.UNEXPECTED_ERROR.errorCode, error.errorCode)
                    latch.countDown()
                }
            }
        )

        assertTrue(latch.await(1, TimeUnit.SECONDS))
    }

    @Test
    fun testRecordHistoricalEvent_whenEventHistoryError() {
        val event = Event.Builder("TestEvent", "eventtype", "eventsource").build()
        val latch = CountDownLatch(1)
        val adobeCallback = object : AdobeCallbackWithError<Boolean> {
            override fun call(value: Boolean) {
                // Should not be called in this test
                kotlin.test.fail()
            }

            override fun fail(error: AdobeError) {
                assertEquals(AdobeError.DATABASE_ERROR.errorCode, error.errorCode)
                latch.countDown()
            }
        }

        // Configure the mock to call the fail callback
        `when`(eventHistory.recordEvent(event, adobeCallback)).thenAnswer { invocation ->
            val callback = invocation.getArgument<AdobeCallbackWithError<Boolean>>(1)
            callback.fail(AdobeError.DATABASE_ERROR)
            null
        }

        container?.recordHistoricalEvent(event, adobeCallback)

        assertTrue(latch.await(1, TimeUnit.SECONDS))
    }
}
