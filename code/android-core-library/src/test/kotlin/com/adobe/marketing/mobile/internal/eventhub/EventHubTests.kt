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
import com.adobe.marketing.mobile.Extension
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.ExtensionError
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.modules.junit4.PowerMockRunner
import java.lang.Exception
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

private object MockExtensions {
    class MockExtensionInvalidConstructor(api: ExtensionApi, name: String?) : Extension(api) {
        override fun getName(): String {
            return MockExtensionInvalidConstructor::javaClass.name
        }
    }

    class MockExtensionInitFailure(api: ExtensionApi) : Extension(api) {
        init {
            throw Exception("Init Exception")
        }

        override fun getName(): String {
            return MockExtensionInitFailure::javaClass.name
        }
    }

    class MockExtensionNullName(api: ExtensionApi) : Extension(api) {
        override fun getName(): String? {
            return null
        }
    }

    class MockExtensionNameException(api: ExtensionApi) : Extension(api) {
        override fun getName(): String {
            throw Exception()
        }
    }

    class MockExtensionKotlin(api: ExtensionApi) : Extension(api) {
        override fun getName(): String {
            return MockExtensionKotlin::javaClass.name
        }
    }

    class TestExtension(api: ExtensionApi) : Extension(api) {
        companion object {
            const val version = "0.1"
            const val extensionName = "TestExtension"
            const val friendlyExtensionName = "FriendlyTestExtension"
        }

        override fun getName(): String {
            return TestExtension.extensionName
        }

        override fun getFriendlyName(): String {
            return TestExtension.friendlyExtensionName
        }

        override fun getVersion(): String {
            return TestExtension.version
        }
    }
}

@RunWith(PowerMockRunner::class)
internal class EventHubTests {
    private lateinit var eventHub: EventHub
    private val eventType = "Type"
    private val eventSource = "Source"
    private val event1: Event = Event.Builder("Event1", eventType, eventSource).build()
    private val event2: Event = Event.Builder("Event2", eventType, eventSource).build()

    // Helper to register extensions
    fun registerExtension(extensionClass: Class<out Extension>): EventHubError {
        var ret: EventHubError = EventHubError.Unknown

        val latch = CountDownLatch(1)
        eventHub.registerExtension(extensionClass) { error ->
            ret = error
            latch.countDown()
        }
        if (!latch.await(1, TimeUnit.SECONDS)) throw Exception("Timeout registering extension")
        return ret
    }

    fun unregisterExtension(extensionClass: Class<out Extension>): EventHubError {
        var ret: EventHubError = EventHubError.Unknown

        val latch = CountDownLatch(1)
        eventHub.unregisterExtension(extensionClass) { error ->
            ret = error
            latch.countDown()
        }
        if (!latch.await(1, TimeUnit.SECONDS)) throw Exception("Timeout unregistering extension")
        return ret
    }

    @Before
    fun setup() {
        eventHub = EventHub()
        registerExtension(MockExtensions.TestExtension::class.java)
    }

    @After
    fun teardown() {
        eventHub.shutdown()
    }

    // Register, Unregister tests
    @Test
    fun testRegisterExtensionSuccess() {
        var ret = registerExtension(MockExtension::class.java)
        assertEquals(EventHubError.None, ret)

        ret = registerExtension(MockExtensions.MockExtensionKotlin::class.java)
        assertEquals(EventHubError.None, ret)
    }

    @Test
    fun testRegisterExtensionFailure_DuplicateExtension() {
        registerExtension(MockExtension::class.java)

        val ret = registerExtension(MockExtension::class.java)
        assertEquals(EventHubError.DuplicateExtensionName, ret)
    }

    @Test
    fun testRegisterExtensionFailure_ExtensionInitialization() {
        var ret = registerExtension(MockExtensions.MockExtensionInitFailure::class.java)
        assertEquals(EventHubError.ExtensionInitializationFailure, ret)

        ret = registerExtension(MockExtensions.MockExtensionInvalidConstructor::class.java)
        assertEquals(EventHubError.ExtensionInitializationFailure, ret)
    }

    @Test
    fun testRegisterExtensionFailure_InvalidExceptionName() {
        var ret = registerExtension(MockExtensions.MockExtensionNullName::class.java)
        assertEquals(EventHubError.InvalidExtensionName, ret)

        ret = registerExtension(MockExtensions.MockExtensionNameException::class.java)
        assertEquals(EventHubError.InvalidExtensionName, ret)
    }

    @Test
    fun testUnregisterExtensionSuccess() {
        registerExtension(MockExtensions.MockExtensionKotlin::class.java)

        val ret = unregisterExtension(MockExtensions.MockExtensionKotlin::class.java)
        assertEquals(EventHubError.None, ret)
    }

    @Test
    fun testUnregisterExtensionFailure() {
        val ret = unregisterExtension(MockExtensions.MockExtensionKotlin::class.java)
        assertEquals(EventHubError.ExtensionNotRegistered, ret)
    }

    @Test
    fun testRegisterAfterUnregister() {
        registerExtension(MockExtensions.MockExtensionKotlin::class.java)

        var ret = unregisterExtension(MockExtensions.MockExtensionKotlin::class.java)
        assertEquals(EventHubError.None, ret)

        ret = registerExtension(MockExtensions.MockExtensionKotlin::class.java)
        assertEquals(EventHubError.None, ret)
    }

    // Shared state tests
    @Test
    fun testSetSharedState_NullOrEmptyExtensionName() {
        var result: ExtensionError? = null

        eventHub.dispatch(event1) // Dispatch Event1

        val stateAtEvent1: MutableMap<String, Any?> = mutableMapOf("One" to 1, "Yes" to true)

        // Set state at event1 with null extension name
        assertFalse(
            eventHub.setSharedState(
                SharedStateType.STANDARD,
                null, stateAtEvent1, event1
            ) {
                result = it
            }
        )
        assertEquals(result, ExtensionError.BAD_NAME)

        // Set state at event1 with empty extension name
        assertFalse(
            eventHub.setSharedState(
                SharedStateType.STANDARD,
                "", stateAtEvent1, event1
            ) {
                result = it
            }
        )
        assertEquals(result, ExtensionError.BAD_NAME)
    }

    @Test
    fun testSetSharedState_ExtensionNotRegistered() {
        eventHub = EventHub()
        eventHub.dispatch(event1) // Dispatch Event1

        val stateAtEvent1: MutableMap<String, Any?> = mutableMapOf("One" to 1, "Yes" to true)

        var result: ExtensionError? = null
        // Set state at event1
        assertFalse(
            eventHub.setSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, stateAtEvent1, event1
            ) {
                result = it
            }
        )
        assertEquals(result, ExtensionError.UNEXPECTED_ERROR)
    }

    @Test
    fun testSetSharedState_PendingState() {
        eventHub.dispatch(event1) // Dispatch Event1

        // Set state at event1
        assertTrue(
            eventHub.setSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, null, event1
            ) {
                fail("State should have been set successfully ${it.errorCode} - ${it.errorName}")
            }
        )
    }

    @Test
    fun testSetSharedState_OverwritePendingStateWithNonPendingState() {

        eventHub.dispatch(event1) // Dispatch Event1

        // Set pending state at event1
        assertTrue(
            eventHub.setSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, null, event1
            ) {
                fail("State should have been set successfully ${it.errorCode} - ${it.errorName}")
            }
        )

        val stateAtEvent: MutableMap<String, Any?> = mutableMapOf("One" to 1, "Yes" to true)

        assertTrue(
            eventHub.setSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, stateAtEvent, event1
            ) {
                fail("State should have been set successfully ${it.errorCode} - ${it.errorName}")
            }
        )
    }

    @Test
    fun testSetSharedState_NoPendingStateAtEvent() {

        eventHub.dispatch(event1) // Dispatch Event1

        // Set non pending state at event1
        val stateAtEvent1: MutableMap<String, Any?> = mutableMapOf("One" to 1, "Yes" to true)
        assertTrue(
            eventHub.setSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, stateAtEvent1, event1
            ) {
                fail("State should have been set successfully. ${it.errorCode} - ${it.errorName}")
            }
        )

        // Verify that state at event1 cannot be overwritten
        val overwriteState: MutableMap<String, Any?> = mutableMapOf("Two" to 2, "No" to false)

        assertFalse(
            eventHub.setSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, overwriteState, event1
            ) {
                fail("${it.errorCode} - ${it.errorName}")
            }
        )
    }

    @Test
    fun testSetSharedState_OverwriteNonPendingStateWithPendingState() {

        eventHub.dispatch(event1) // Dispatch Event1

        // Set non pending state at Event 1
        val stateAtEvent1: MutableMap<String, Any?> = mutableMapOf("One" to 1, "Yes" to true)
        assertTrue(
            eventHub.setSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, stateAtEvent1, event1
            ) {
                fail("State should have been set successfully. ${it.errorCode} - ${it.errorName}")
            }
        )

        // Verify that state at event1 cannot be overwritten with a pending state
        val overwriteState: MutableMap<String, Any?>? = null

        assertFalse(
            eventHub.setSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, overwriteState, event1
            ) {
                fail("${it.errorCode} - ${it.errorName}")
            }
        )
    }

    @Test
    fun testGetSharedState_NullOrEmptyExtensionName() {
        var result: ExtensionError? = null

        // Get state at event1 with null extension name
        eventHub.getSharedState(
            SharedStateType.STANDARD,
            null, event1
        ) {
            result = it
        }
        assertEquals(result, ExtensionError.BAD_NAME)

        // Get state at event1 with empty extension name
        eventHub.getSharedState(
            SharedStateType.STANDARD, "", event1
        ) {
            result = it
        }
        assertEquals(result, ExtensionError.BAD_NAME)
    }

    @Test
    fun testGetSharedState_ExtensionNotRegistered() {
        var result: ExtensionError? = null

        eventHub = EventHub()
        // Set state at event1
        eventHub.getSharedState(
            SharedStateType.STANDARD, MockExtensions.TestExtension.extensionName, event1
        ) {
            result = it
        }
        assertEquals(result, ExtensionError.UNEXPECTED_ERROR)
    }

    @Test
    fun testGetSharedState_NoStateExistsYet() {

        val errorCallback: (ExtensionError) -> Unit = {
            fail("Test failed ${it.errorCode} - ${it.errorName}")
        }

        // Dispatch Event 1
        eventHub.dispatch(event1)

        assertNull(
            eventHub.getSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, event1, errorCallback
            )
        )
    }

    @Test
    fun testGetSharedState_StateExistsAtVersion() {

        val errorCallback: (ExtensionError) -> Unit = {
            fail("Test failed ${it.errorCode} - ${it.errorName}")
        }

        // Dispatch event1
        eventHub.dispatch(event1)

        // Set non pending state at event1
        val stateAtEvent1: MutableMap<String, Any?> = mutableMapOf("One" to 1, "Yes" to true)
        assertTrue(
            eventHub.setSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, stateAtEvent1, event1
            ) {
                fail("State should have been set successfully. ${it.errorCode} - ${it.errorName}")
            }
        )

        // Dispatch event2
        eventHub.dispatch(event2)

        // Set state at event2
        val stateAtEvent2: MutableMap<String, Any?> = mutableMapOf("Two" to 1, "No" to false)
        assertTrue(
            eventHub.setSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, stateAtEvent2, event2, errorCallback
            )
        )

        // Verify that the state at event1 and event2
        assertEquals(
            stateAtEvent1,
            eventHub.getSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, event1, errorCallback
            )
        )
        assertEquals(
            stateAtEvent2,
            eventHub.getSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, event2, errorCallback
            )
        )
    }

    @Test
    fun testGetSharedState_PreviousStateDoesNotExist() {

        val errorCallback: (ExtensionError) -> Unit = {
            fail("Test failed ${it.errorCode} - ${it.errorName}")
        }

        // Dispatch event 1 & event2
        eventHub.dispatch(event1)
        eventHub.dispatch(event2)

        // Set state at event2
        val stateAtEvent2: MutableMap<String, Any?> = mutableMapOf("One" to 1, "Yes" to true)
        assertTrue(
            eventHub.setSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, stateAtEvent2, event2, errorCallback
            )
        )

        // Verify that the state at event1 is still null
        assertNull(
            eventHub.getSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, event1, errorCallback
            )
        )
        assertEquals(
            stateAtEvent2,
            eventHub.getSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, event2, errorCallback
            )
        )
    }

    @Test
    fun testGetSharedState_FetchesLatestStateOnNullEvent() {

        val errorCallback: (ExtensionError) -> Unit = {
            fail("Test failed ${it.errorCode} - ${it.errorName}")
        }

        // Dispatch event1
        eventHub.dispatch(event1)

        // Set state at event1
        val state: MutableMap<String, Any?> = mutableMapOf("One" to 1, "Yes" to true)
        assertTrue(
            eventHub.setSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, state, event1, errorCallback
            )
        )

        assertEquals(
            state,
            eventHub.getSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, null, errorCallback
            )
        )
    }

    @Test
    fun testGetSharedState_OlderStateExists() {

        val errorCallback: (ExtensionError) -> Unit = {
            fail("Test failed ${it.errorCode} - ${it.errorName}")
        }

        // Dispatch event1
        eventHub.dispatch(event1)

        // Set state at event1
        val stateAtEvent1: MutableMap<String, Any?> = mutableMapOf("One" to 1, "Yes" to true)
        assertTrue(
            eventHub.setSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, stateAtEvent1, event1, errorCallback
            )
        )

        // Dispatch event2
        eventHub.dispatch(event2)

        // Verify that the state at event1 and event2  are the same and they equal [stateAtEvent1]
        assertEquals(
            stateAtEvent1,
            eventHub.getSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, event1, errorCallback
            )
        )
        assertEquals(
            stateAtEvent1,
            eventHub.getSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, event2, errorCallback
            )
        )
    }

    @Test
    fun testClearSharedState_NullOrEmptyExtensionName() {
        var result: ExtensionError? = null
        assertFalse(
            eventHub.clearSharedState(
                SharedStateType.STANDARD,
                null
            ) {
                result = it
            }
        )
        assertEquals(result, ExtensionError.BAD_NAME)

        assertFalse(
            eventHub.clearSharedState(
                SharedStateType.STANDARD, ""
            ) {
                result = it
            }
        )
        assertEquals(result, ExtensionError.BAD_NAME)
    }

    @Test
    fun testClearSharedState_ExtensionNotRegistered() {
        var result: ExtensionError? = null
        eventHub = EventHub()
        assertFalse(
            eventHub.clearSharedState(
                SharedStateType.STANDARD, MockExtensions.TestExtension.extensionName,
            ) {
                result = it
            }
        )

        assertEquals(result, ExtensionError.UNEXPECTED_ERROR)
    }

    @Test
    fun testClearSharedState_NoStateYet() {

        assertTrue(
            eventHub.clearSharedState(
                SharedStateType.STANDARD, MockExtensions.TestExtension.extensionName,
            ) {
                fail("State should have been cleared successfully")
            }
        )
    }

    @Test
    fun testClearSharedState() {

        val errorCallback: (ExtensionError) -> Unit = {
            fail("Test failed ${it.errorCode} - ${it.errorName}")
        }
        eventHub.dispatch(event1)
        eventHub.dispatch(event2)

        val stateAtEvent1: MutableMap<String, Any?> = mutableMapOf("One" to 1, "Yes" to true)
        assertTrue(
            eventHub.setSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, stateAtEvent1, event1, errorCallback
            )
        )

        val stateAtEvent2: MutableMap<String, Any?> = mutableMapOf("Twi" to 2, "No" to false)
        assertTrue(
            eventHub.setSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, stateAtEvent2, event2, errorCallback
            )
        )

        // Verify that all the states are cleared
        assertTrue(
            eventHub.clearSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, errorCallback
            )
        )
        assertNull(
            eventHub.getSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, event1, errorCallback
            )
        )
        assertNull(
            eventHub.getSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, event2, errorCallback
            )
        )
    }

    @Test
    fun testClearSharedState_DifferentStateType() {

        val errorCallback: (ExtensionError) -> Unit = {
            fail("Test failed ${it.errorCode} - ${it.errorName}")
        }
        eventHub.dispatch(event1)

        val stateAtEvent1: MutableMap<String, Any?> = mutableMapOf("One" to 1, "Yes" to true)
        val xdmStateAtEvent1: MutableMap<String, Any?> = mutableMapOf("Two" to 1, "No" to false)

        // Set Standard shared state
        assertTrue(
            eventHub.setSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, stateAtEvent1, event1, errorCallback
            )
        )

        // Set Standard XDM shared state
        assertTrue(
            eventHub.setSharedState(
                SharedStateType.XDM,
                MockExtensions.TestExtension.extensionName, xdmStateAtEvent1, event1, errorCallback
            )
        )

        // Set Standard Standard shared state
        assertTrue(
            eventHub.clearSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, errorCallback
            )
        )

        // Verify that only standard state is cleared.
        assertNull(
            eventHub.getSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, event1, errorCallback
            )
        )
        assertEquals(
            xdmStateAtEvent1,
            eventHub.getSharedState(
                SharedStateType.XDM,
                MockExtensions.TestExtension.extensionName, event1, errorCallback
            )
        )
    }

    // Event listener tests
    @Test
    fun testExtensionListener() {
        val latch = CountDownLatch(1)
        val testEvent = Event.Builder("Sample event", eventType, eventSource).build()

        val extensionContainer = eventHub.getExtensionContainer(MockExtensions.TestExtension::class.java)
        extensionContainer?.registerEventListener(eventType, eventSource) {
            assertTrue { it == testEvent }
            latch.countDown()
        }

        eventHub.start()
        eventHub.dispatch(testEvent)

        assertTrue {
            latch.await(250, TimeUnit.MILLISECONDS)
        }
    }

    @Test
    fun testExtensionListener_UnmatchedEvent() {
        val latch = CountDownLatch(1)
        val testEvent = Event.Builder("Test event", eventType, eventSource).build()

        val extensionContainer = eventHub.getExtensionContainer(MockExtensions.TestExtension::class.java)
        extensionContainer?.registerEventListener("customEventType", "customEventSource") {
            latch.countDown()
        }

        eventHub.start()
        eventHub.dispatch(testEvent)

        assertFalse {
            latch.await(250, TimeUnit.MILLISECONDS)
        }
    }

    @Test
    fun testExtensionListener_NeverDispatchEventsWithoutStart() {
        val latch = CountDownLatch(1)
        val testEvent = Event.Builder("Test event", eventType, eventSource).build()

        val extensionContainer = eventHub.getExtensionContainer(MockExtensions.TestExtension::class.java)
        extensionContainer?.registerEventListener(
            eventType, eventSource,
            {
                latch.countDown()
            }
        )

        eventHub.dispatch(testEvent)

        assertFalse {
            latch.await(250, TimeUnit.MILLISECONDS)
        }
    }

    @Test
    fun testExtensionListener_QueuesEventsBeforeStart() {
        val latch = CountDownLatch(2)
        val testEvent = Event.Builder("Test event", eventType, eventSource).build()

        val extensionContainer = eventHub.getExtensionContainer(MockExtensions.TestExtension::class.java)
        extensionContainer?.registerEventListener(eventType, eventSource) {
            assertTrue { it == testEvent }
            latch.countDown()
        }

        eventHub.dispatch(testEvent)
        eventHub.dispatch(testEvent)
        eventHub.start()
        assertTrue {
            latch.await(250, TimeUnit.MILLISECONDS)
        }
    }

    @Test
    fun testExtensionListener_IgnoresNonMatchingEvent() {
        val latch = CountDownLatch(1)
        val testEvent = Event.Builder("Test event", eventType, eventSource).build()
        val testEvent1 = Event.Builder("Test event 2", "customEventType", "customEventSource").build()

        val extensionContainer = eventHub.getExtensionContainer(MockExtensions.TestExtension::class.java)
        extensionContainer?.registerEventListener(
            eventType, eventSource
        ) {
            assertTrue { it == testEvent }
            latch.countDown()
        }

        eventHub.dispatch(testEvent)
        eventHub.dispatch(testEvent1)
        eventHub.start()
        assertTrue {
            latch.await(250, TimeUnit.MILLISECONDS)
        }
    }

    @Test
    fun testRegisterListener_DispatchesEventToListener() {
        val latch = CountDownLatch(1)
        val testEvent = Event.Builder("Sample event", eventType, eventSource).build()

        eventHub.registerListener(eventType, eventSource) {
            assertTrue { it == testEvent }
            latch.countDown()
        }

        eventHub.start()
        eventHub.dispatch(testEvent)

        assertTrue {
            latch.await(250, TimeUnit.MILLISECONDS)
        }
    }

    @Test
    fun testRegisterListener_NotInvokeListenerWrongType() {
        val latch = CountDownLatch(1)
        val testEvent = Event.Builder("Test event", eventType, eventSource).build()

        eventHub.registerListener("customEventType", "customEventSource") {
            latch.countDown()
        }

        eventHub.start()
        eventHub.dispatch(testEvent)

        assertFalse {
            latch.await(250, TimeUnit.MILLISECONDS)
        }
    }

    @Test
    fun testRegisterListener_NeverDispatchEventsWithoutStart() {
        val latch = CountDownLatch(1)
        val testEvent = Event.Builder("Test event", eventType, eventSource).build()

        eventHub.registerListener(eventType, eventSource) {
            latch.countDown()
        }

        eventHub.dispatch(testEvent)

        assertFalse {
            latch.await(250, TimeUnit.MILLISECONDS)
        }
    }

    @Test
    fun testRegisterListener_QueuesEventsBeforeStart() {
        val latch = CountDownLatch(2)
        val testEvent = Event.Builder("Test event", eventType, eventSource).build()

        eventHub.registerListener(eventType, eventSource) {
            assertTrue { it == testEvent }
            latch.countDown()
        }

        eventHub.dispatch(testEvent)
        eventHub.dispatch(testEvent)
        eventHub.start()
        assertTrue {
            latch.await(250, TimeUnit.MILLISECONDS)
        }
    }

    @Test
    fun testRegisterListener_IgnoresNonMatchingEvent() {
        val latch = CountDownLatch(1)
        val testEvent = Event.Builder("Test event", eventType, eventSource).build()
        val testEvent1 = Event.Builder("Test event 2", "customEventType", "customEventSource").build()

        eventHub.registerListener(eventType, eventSource) {
            assertTrue { it == testEvent }
            latch.countDown()
        }

        eventHub.dispatch(testEvent)
        eventHub.dispatch(testEvent1)
        eventHub.start()
        assertTrue {
            latch.await(250, TimeUnit.MILLISECONDS)
        }
    }

    @Test
    fun testRegisterListener_NotInvokedForPairedResponseEvent() {
        val latch = CountDownLatch(2)
        val capturedEvents = mutableListOf<Event>()

        val testEvent = Event.Builder("Test event", eventType, eventSource).build()
        val testResponseEvent = Event.Builder("Test response event", eventType, eventSource).setTriggerEvent(testEvent).build()
        eventHub.registerListener(eventType, eventSource) {
            capturedEvents.add(it)
            latch.countDown()
        }

        eventHub.start()
        eventHub.dispatch(testEvent)
        eventHub.dispatch(testResponseEvent)
        assertFalse {
            latch.await(250, TimeUnit.MILLISECONDS)
        }
        assertEquals(capturedEvents, listOf<Event>(testEvent))
    }

    @Test
    fun testRegisterListener_WildcardEvents() {
        val latch = CountDownLatch(2)
        val capturedEvents = mutableListOf<Event>()

        val testEvent = Event.Builder("Test event", eventType, eventSource).build()
        val testResponseEvent = Event.Builder("Test response event", eventType, eventSource).setTriggerEvent(testEvent).build()
        eventHub.registerListener(EventType.TYPE_WILDCARD, EventSource.TYPE_WILDCARD) {
            capturedEvents.add(it)
            latch.countDown()
        }

        eventHub.start()
        eventHub.dispatch(testEvent)
        eventHub.dispatch(testResponseEvent)
        assertTrue {
            latch.await(250, TimeUnit.MILLISECONDS)
        }
        assertEquals(capturedEvents, listOf<Event>(testEvent, testResponseEvent))
    }

    @Test
    fun testResponseListener() {
        val latch = CountDownLatch(1)
        val capturedEvents = mutableListOf<Pair<Event?, AdobeError?>>()

        val testEvent = Event.Builder("Test event", eventType, eventSource).build()
        val testResponseEvent = Event.Builder("Test response event", eventType, eventSource).setTriggerEvent(testEvent).build()

        eventHub.registerResponseListener(
            testEvent, 250,
            object : AdobeCallbackWithError<Event> {
                override fun call(value: Event?) {
                    capturedEvents.add(Pair(value, null))
                    latch.countDown()
                }

                override fun fail(error: AdobeError?) {
                    capturedEvents.add(Pair(null, error))
                    latch.countDown()
                }
            }
        )

        eventHub.start()
        eventHub.dispatch(testResponseEvent)
        assertTrue {
            latch.await(250, TimeUnit.MILLISECONDS)
        }
        assertEquals(capturedEvents, listOf<Pair<Event?, AdobeError?>>(Pair(testResponseEvent, null)))
    }

    @Test
    fun testResponseListener_RemovedAfterInvoked() {
        val latch = CountDownLatch(2)
        val capturedEvents = mutableListOf<Pair<Event?, AdobeError?>>()

        val testEvent = Event.Builder("Test event", eventType, eventSource).build()
        val testResponseEvent = Event.Builder("Test response event", eventType, eventSource).setTriggerEvent(testEvent).build()

        eventHub.registerResponseListener(
            testEvent, 5000,
            object : AdobeCallbackWithError<Event> {
                override fun call(value: Event?) {
                    capturedEvents.add(Pair(value, null))
                    latch.countDown()
                }

                override fun fail(error: AdobeError?) {
                    capturedEvents.add(Pair(null, error))
                    latch.countDown()
                }
            }
        )

        eventHub.start()
        eventHub.dispatch(testResponseEvent)
        eventHub.dispatch(testResponseEvent)
        assertFalse {
            latch.await(500, TimeUnit.MILLISECONDS)
        }

        assertEquals(capturedEvents, listOf<Pair<Event?, AdobeError?>>(Pair(testResponseEvent, null)))
    }

    @Test
    fun testResponseListenerTimeout() {
        val latch = CountDownLatch(1)
        val capturedEvents = mutableListOf<Pair<Event?, AdobeError?>>()

        val testEvent = Event.Builder("Test event", eventType, eventSource).build()

        eventHub.registerResponseListener(
            testEvent, 250,
            object : AdobeCallbackWithError<Event> {
                override fun call(value: Event?) {
                    capturedEvents.add(Pair(value, null))
                    latch.countDown()
                }

                override fun fail(error: AdobeError?) {
                    capturedEvents.add(Pair(null, error))
                    latch.countDown()
                }
            }
        )

        eventHub.start()
        assertTrue {
            latch.await(500, TimeUnit.MILLISECONDS)
        }
        assertEquals(capturedEvents, listOf<Pair<Event?, AdobeError?>>(Pair(null, AdobeError.CALLBACK_TIMEOUT)))
    }

    @Test
    fun testListener_LongRunningListenerShouldNotBlockOthers() {
        class Extension1(api: ExtensionApi) : Extension(api) {
            override fun getName(): String {
                return "ext1"
            }
        }

        class Extension2(api: ExtensionApi) : Extension(api) {
            override fun getName(): String {
                return "ext2"
            }
        }

        val latch = CountDownLatch(2)

        val testEvent = Event.Builder("Test event", eventType, eventSource).build()

        registerExtension(Extension1::class.java)
        registerExtension(Extension2::class.java)

        eventHub.getExtensionContainer(Extension1::class.java)?.registerEventListener(eventType, eventSource) {
            latch.countDown()
            Thread.sleep(5000)
        }

        eventHub.getExtensionContainer(Extension2::class.java)?.registerEventListener(eventType, eventSource) {
            latch.countDown()
        }

        eventHub.start()
        eventHub.dispatch(testEvent)
        assertTrue {
            latch.await(500, TimeUnit.MILLISECONDS)
        }
    }
}
