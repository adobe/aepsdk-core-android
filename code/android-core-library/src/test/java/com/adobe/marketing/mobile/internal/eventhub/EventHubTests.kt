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
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.SharedStateResolution
import com.adobe.marketing.mobile.SharedStateResult
import com.adobe.marketing.mobile.SharedStateStatus
import com.adobe.marketing.mobile.WrapperType
import com.adobe.marketing.mobile.internal.eventhub.history.EventHistory
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

object MockExtensions {
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
}

@RunWith(MockitoJUnitRunner.Silent::class)
internal class EventHubTests {

    private class TestExtension(api: ExtensionApi) : Extension(api) {
        companion object {
            const val VERSION = "0.1"
            const val EXTENSION_NAME = "TestExtension"
            const val FRIENDLY_NAME = "FriendlyTestExtension"
        }

        override fun getName(): String {
            return EXTENSION_NAME
        }

        override fun getFriendlyName(): String {
            return FRIENDLY_NAME
        }

        override fun getVersion(): String {
            return VERSION
        }
    }

    private class TestExtension2(api: ExtensionApi) : Extension(api) {
        companion object {
            const val VERSION = "0.2"
            const val EXTENSION_NAME = "TestExtension2"
            const val FRIENDLY_NAME = "FriendlyTestExtension2"
            val METADATA = mutableMapOf("k1" to "v1")
        }

        override fun getName(): String {
            return EXTENSION_NAME
        }

        override fun getFriendlyName(): String {
            return FRIENDLY_NAME
        }

        override fun getVersion(): String {
            return VERSION
        }

        override fun getMetadata(): MutableMap<String, String> {
            return METADATA
        }
    }

    private class TestExtension_InitError(api: ExtensionApi) : Extension(api) {
        companion object {
            const val VERSION = "0.2"
            const val EXTENSION_NAME = "TestExtension_InitError"
            const val FRIENDLY_NAME = "FriendlyTestExtension_InitError"
            val METADATA = mutableMapOf("k1" to "v1")
        }

        init {
            throw Exception()
        }

        override fun getName(): String {
            return EXTENSION_NAME
        }

        override fun getFriendlyName(): String {
            return FRIENDLY_NAME
        }

        override fun getVersion(): String {
            return VERSION
        }

        override fun getMetadata(): MutableMap<String, String> {
            return METADATA
        }
    }

    private class TestExtension_Barrier(api: ExtensionApi) : Extension(api) {
        companion object {
            const val VERSION = "0.1"
            const val EXTENSION_NAME = "TestExtension_Barrier"
            const val FRIENDLY_NAME = "FriendlyTestExtension_Barrier"

            // Will stop processing once it sees this event
            var BARRIER_EVENT: Event? = null
        }

        // Clear everytime extension get registered
        init {
            BARRIER_EVENT = null
        }

        override fun getName(): String {
            return EXTENSION_NAME
        }

        override fun readyForEvent(event: Event): Boolean {
            return event != BARRIER_EVENT
        }
    }

    private class TestExtension_DelayedInit(api: ExtensionApi) : Extension(api) {
        companion object {
            const val VERSION = "0.1"
            const val EXTENSION_NAME = "TestExtension_DelayedInit"
            const val FRIENDLY_NAME = "FriendlyTestExtension_DelayedInit"

            // Init will take DELAY ms
            var DELAY_MS: Long? = null
        }

        // Clear everytime extension get registered
        init {
            DELAY_MS?.let { Thread.sleep(it) }
        }

        override fun getName(): String {
            return EXTENSION_NAME
        }
    }

    private lateinit var eventHub: EventHub
    private val eventType = "Type"
    private val eventSource = "Source"
    private val event1: Event = Event.Builder("Event1", eventType, eventSource).build()
    private val event2: Event = Event.Builder("Event2", eventType, eventSource).build()
    private val event3: Event = Event.Builder("Event3", eventType, eventSource).build()
    private val event4: Event = Event.Builder("Event4", eventType, eventSource).build()

    // Helper to register extensions
    private fun registerExtension(extensionClass: Class<out Extension>): EventHubError {
        var ret: EventHubError = EventHubError.Unknown

        val latch = CountDownLatch(1)
        eventHub.registerExtension(extensionClass) { error ->
            ret = error
            latch.countDown()
        }
        if (!latch.await(1, TimeUnit.SECONDS)) throw Exception("Timeout registering extension")
        return ret
    }

    private fun unregisterExtension(extensionClass: Class<out Extension>): EventHubError {
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
        registerExtension(TestExtension::class.java)
    }

    @After
    fun teardown() {
        eventHub.shutdown()
    }

    private fun resetEventHub() {
        eventHub.shutdown()
        eventHub = EventHub()
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
        assertNull(eventHub.getExtensionContainer(MockExtensions.MockExtensionInitFailure::class.java))

        ret = registerExtension(MockExtensions.MockExtensionInvalidConstructor::class.java)
        assertEquals(EventHubError.ExtensionInitializationFailure, ret)
        assertNull(eventHub.getExtensionContainer(MockExtensions.MockExtensionInvalidConstructor::class.java))
    }

    @Test
    fun testRegisterExtensionFailure_InvalidExceptionName() {
        val ret = registerExtension(MockExtensions.MockExtensionNameException::class.java)
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

    @Test
    fun testStartHub() {
        resetEventHub()

        val latch = CountDownLatch(1)
        eventHub.start {
            latch.countDown()
        }

        assertTrue { latch.await(1000, TimeUnit.MILLISECONDS) }
    }

    @Test
    fun testStartAfterExtensionRegistration() {
        resetEventHub()

        TestExtension_DelayedInit.DELAY_MS = 1000
        val latch = CountDownLatch(1)
        var registeredExtension = mutableListOf<Class<out Extension>>()

        eventHub.registerExtension(TestExtension::class.java) { registeredExtension.add(TestExtension::class.java) }
        eventHub.registerExtension(TestExtension_DelayedInit::class.java) { registeredExtension.add(TestExtension_DelayedInit::class.java) }
        eventHub.start {
            latch.countDown()
        }

        assertTrue { latch.await(2000, TimeUnit.MILLISECONDS) }
        assertEquals(listOf(TestExtension::class.java, TestExtension_DelayedInit::class.java), registeredExtension)
    }

    @Test
    fun testStartBetweenExtensionRegistration() {
        resetEventHub()

        TestExtension_DelayedInit.DELAY_MS = 1000
        val latch = CountDownLatch(1)
        var registeredExtension = mutableListOf<Class<out Extension>>()

        eventHub.registerExtension(TestExtension::class.java) { registeredExtension.add(TestExtension::class.java) }
        eventHub.start {
            latch.countDown()
        }
        eventHub.registerExtension(TestExtension_DelayedInit::class.java) { registeredExtension.add(TestExtension_DelayedInit::class.java) }

        assertTrue { latch.await(750, TimeUnit.MILLISECONDS) }
        assertEquals(listOf<Class<out Extension>>(TestExtension::class.java), registeredExtension)
    }

    // Shared state tests
    private fun verifySharedState(type: SharedStateType, event: Event?, expectedResult: SharedStateResult?, resolution: SharedStateResolution = SharedStateResolution.ANY, barrier: Boolean = false, extensionName: String = TestExtension.EXTENSION_NAME) {
        val actualResult = eventHub.getSharedState(
            type,
            extensionName,
            event,
            barrier,
            resolution
        )

        assertEquals(expectedResult?.status, actualResult?.status)
        assertEquals(expectedResult?.value, actualResult?.value)
    }

    @Test
    fun testCreateSharedState_ExtensionNotRegistered() {
        val stateAtEvent1: MutableMap<String, Any?> = mutableMapOf("One" to 1, "Yes" to true)

        eventHub.start()
        eventHub.dispatch(event1)
        assertFalse {
            eventHub.createSharedState(
                SharedStateType.STANDARD,
                "NotRegisteredExtension",
                stateAtEvent1,
                event1
            )
        }
    }

    @Test
    fun testCreateSharedState_InvalidExtension() {
        val stateAtEvent1: MutableMap<String, Any?> = mutableMapOf("One" to 1, "Yes" to true)

        registerExtension(TestExtension_InitError::class.java)
        eventHub.start()
        eventHub.dispatch(event1)
        assertFalse {
            eventHub.createSharedState(
                SharedStateType.STANDARD,
                TestExtension_InitError.EXTENSION_NAME,
                stateAtEvent1,
                event1
            )
        }
    }

    @Test
    fun testCreateSharedState_CaseInsensitive() {
        val stateAtEvent1: MutableMap<String, Any?> = mutableMapOf("One" to 1, "Yes" to true)

        eventHub.start()
        eventHub.dispatch(event1)
        assertTrue {
            eventHub.createSharedState(
                SharedStateType.STANDARD,
                TestExtension.EXTENSION_NAME.toUpperCase(),
                stateAtEvent1,
                event1
            )
        }
    }

    @Test
    fun testCreateSharedState_FailsSecondTime() {
        val stateAtEvent1: MutableMap<String, Any?> = mutableMapOf("One" to 1, "Yes" to true)

        eventHub.start()
        eventHub.dispatch(event1)
        assertTrue {
            eventHub.createSharedState(
                SharedStateType.STANDARD,
                TestExtension.EXTENSION_NAME,
                stateAtEvent1,
                event1
            )
        }

        assertFalse {
            eventHub.createSharedState(
                SharedStateType.STANDARD,
                TestExtension.EXTENSION_NAME,
                stateAtEvent1,
                event1
            )
        }
    }

    @Test
    fun testCreateSharedState_FailsOlderEvents() {
        val stateAtEvent1: MutableMap<String, Any?> = mutableMapOf("One" to 1, "Yes" to true)

        eventHub.start()
        eventHub.dispatch(event1)
        assertTrue {
            eventHub.createSharedState(
                SharedStateType.STANDARD,
                TestExtension.EXTENSION_NAME.toUpperCase(),
                stateAtEvent1,
                event1
            )
        }
    }

    @Test
    fun testCreateSharedState_DispatchEvent() {
        val latch = CountDownLatch(2)

        val capturedEvents = mutableListOf<Event>()
        val extensionContainer = eventHub.getExtensionContainer(TestExtension::class.java)
        extensionContainer?.registerEventListener(EventType.HUB, EventSource.SHARED_STATE) {
            capturedEvents.add(it)
            latch.countDown()
        }

        val stateAtEvent1: MutableMap<String, Any?> = mutableMapOf("One" to 1, "Yes" to true)
        eventHub.start()
        eventHub.dispatch(event1)
        eventHub.createSharedState(
            SharedStateType.STANDARD,
            TestExtension.EXTENSION_NAME,
            stateAtEvent1,
            event1
        )
        assertTrue {
            latch.await(250, TimeUnit.MILLISECONDS)
        }

        assertEquals(capturedEvents[0].name, EventHubConstants.STATE_CHANGE)
        assertEquals(
            capturedEvents[0].eventData,
            mapOf(
                EventHubConstants.EventDataKeys.Configuration.EVENT_STATE_OWNER to EventHubConstants.NAME
            )
        )

        assertEquals(capturedEvents[1].name, EventHubConstants.STATE_CHANGE)
        assertEquals(
            capturedEvents[1].eventData,
            mapOf(
                EventHubConstants.EventDataKeys.Configuration.EVENT_STATE_OWNER to TestExtension.EXTENSION_NAME
            )
        )
    }

    @Test
    fun testCreateXDMSharedState_DispatchEvent() {
        val latch = CountDownLatch(2)

        val capturedEvents = mutableListOf<Event>()
        val extensionContainer = eventHub.getExtensionContainer(TestExtension::class.java)
        extensionContainer?.registerEventListener(EventType.HUB, EventSource.SHARED_STATE) {
            capturedEvents.add(it)
            latch.countDown()
        }

        val stateAtEvent1: MutableMap<String, Any?> = mutableMapOf("One" to 1, "Yes" to true)
        eventHub.start()
        eventHub.dispatch(event1)
        eventHub.createSharedState(
            SharedStateType.XDM,
            TestExtension.EXTENSION_NAME,
            stateAtEvent1,
            event1
        )
        assertTrue {
            latch.await(250, TimeUnit.MILLISECONDS)
        }

        assertEquals(capturedEvents[0].name, EventHubConstants.STATE_CHANGE)
        assertEquals(
            capturedEvents[0].eventData,
            mapOf(
                EventHubConstants.EventDataKeys.Configuration.EVENT_STATE_OWNER to EventHubConstants.NAME
            )
        )

        assertEquals(capturedEvents[1].name, EventHubConstants.XDM_STATE_CHANGE)
        assertEquals(
            capturedEvents[1].eventData,
            mapOf(
                EventHubConstants.EventDataKeys.Configuration.EVENT_STATE_OWNER to TestExtension.EXTENSION_NAME
            )
        )
    }

    @Test
    fun testCreatePendingSharedState_Success() {
        eventHub.start()
        eventHub.dispatch(event1)

        val ret = eventHub.createPendingSharedState(
            SharedStateType.STANDARD,
            "NotRegisteredExtension",
            event1
        )
        assertNull(ret)
    }

    @Test
    fun testCreatePendingSharedState_ExtensionNotRegistered() {
        eventHub.start()
        eventHub.dispatch(event1)

        val ret = eventHub.createPendingSharedState(
            SharedStateType.STANDARD,
            "NotRegisteredExtension",
            event1
        )
        assertNull(ret)
    }

    @Test
    fun testCreatePendingSharedState_InvalidExtension() {
        registerExtension(TestExtension_InitError::class.java)

        eventHub.start()
        eventHub.dispatch(event1)

        val ret = eventHub.createPendingSharedState(
            SharedStateType.XDM,
            TestExtension_InitError.EXTENSION_NAME,
            event1
        )
        assertNull(ret)
    }

    @Test
    fun testCreatePendingSharedState_SharedStateManagerError() {
        eventHub.start()
        eventHub.dispatch(event1)
        eventHub.dispatch(event2)

        val ret = eventHub.createPendingSharedState(
            SharedStateType.STANDARD,
            TestExtension.EXTENSION_NAME,
            event2
        )

        val ret1 = eventHub.createPendingSharedState(
            SharedStateType.STANDARD,
            TestExtension.EXTENSION_NAME,
            event1
        )

        assertNotNull(ret)
        assertNull(ret1)
    }

    @Test
    fun testResolvePendingSharedState_Success() {
        registerExtension(TestExtension::class.java)

        eventHub.start()
        eventHub.dispatch(event1)

        val pendingStateResolver = eventHub.createPendingSharedState(
            SharedStateType.XDM,
            TestExtension.EXTENSION_NAME,
            event1
        )

        pendingStateResolver?.resolve(mapOf("key" to "value"))

        val state = eventHub.getSharedState(SharedStateType.XDM, TestExtension.EXTENSION_NAME, event1, false, SharedStateResolution.ANY)
        assertEquals(state?.status, SharedStateStatus.SET)
        assertEquals(state?.value, mapOf("key" to "value"))
    }

    @Test
    fun testResolvePendingSharedState_ExtensionUnregistered() {
        registerExtension(TestExtension::class.java)

        eventHub.start()
        eventHub.dispatch(event1)

        val pendingStateResolver = eventHub.createPendingSharedState(
            SharedStateType.XDM,
            TestExtension.EXTENSION_NAME,
            event1
        )

        eventHub.unregisterExtension(TestExtension::class.java) {}
        // Resolve after extension is unregistered
        pendingStateResolver?.resolve(mapOf())

        val state = eventHub.getSharedState(SharedStateType.XDM, TestExtension.EXTENSION_NAME, event1, false, SharedStateResolution.ANY)
        assertNull(state)
    }

    @Test
    fun testResolvePendingSharedState_MultipleTimes() {
        registerExtension(TestExtension::class.java)

        eventHub.start()
        eventHub.dispatch(event1)

        val pendingStateResolver = eventHub.createPendingSharedState(
            SharedStateType.XDM,
            TestExtension.EXTENSION_NAME,
            event1
        )

        pendingStateResolver?.resolve(mapOf("key" to "value"))
        // This fails
        pendingStateResolver?.resolve(mapOf("key1" to "value1"))

        val state = eventHub.getSharedState(SharedStateType.XDM, TestExtension.EXTENSION_NAME, event1, false, SharedStateResolution.ANY)
        assertEquals(state?.status, SharedStateStatus.SET)
        assertEquals(state?.value, mapOf("key" to "value"))
    }

    @Test
    fun testClearSharedState_Success() {
        registerExtension(TestExtension::class.java)

        eventHub.start()
        eventHub.dispatch(event1)

        eventHub.createSharedState(
            SharedStateType.XDM,
            TestExtension.EXTENSION_NAME,
            mutableMapOf("xkey" to "xvalue"),
            event1
        )
        eventHub.createSharedState(
            SharedStateType.STANDARD,
            TestExtension.EXTENSION_NAME,
            mutableMapOf("skey" to "svalue"),
            event1
        )

        val state = eventHub.getSharedState(SharedStateType.STANDARD, TestExtension.EXTENSION_NAME, event1, false, SharedStateResolution.ANY)
        val xdmState = eventHub.getSharedState(SharedStateType.XDM, TestExtension.EXTENSION_NAME, event1, false, SharedStateResolution.ANY)

        assertEquals(state?.status, SharedStateStatus.SET)
        assertEquals(state?.value, mapOf("skey" to "svalue"))
        assertEquals(xdmState?.status, SharedStateStatus.SET)
        assertEquals(xdmState?.value, mapOf("xkey" to "xvalue"))

        assertTrue { eventHub.clearSharedState(SharedStateType.XDM, TestExtension.EXTENSION_NAME) }
        val state1 = eventHub.getSharedState(SharedStateType.STANDARD, TestExtension.EXTENSION_NAME, event1, false, SharedStateResolution.ANY)
        val xdmState1 = eventHub.getSharedState(SharedStateType.XDM, TestExtension.EXTENSION_NAME, event1, false, SharedStateResolution.ANY)

        assertEquals(state1?.status, SharedStateStatus.SET)
        assertEquals(state1?.value, mapOf("skey" to "svalue"))
        assertEquals(xdmState1?.status, SharedStateStatus.NONE)
        assertNull(xdmState1?.value)
    }

    @Test
    fun testClearSharedState_ExtensionNotRegistered() {
        eventHub.start()

        assertFalse { eventHub.clearSharedState(SharedStateType.XDM, "invalidextension") }
    }

    @Test
    fun testClearSharedState_InvalidExtension() {
        registerExtension(TestExtension_InitError::class.java)

        eventHub.start()

        assertFalse { eventHub.clearSharedState(SharedStateType.XDM, TestExtension_InitError.EXTENSION_NAME) }
    }

    @Test
    fun testGetSharedState_ExtensionNotPresent() {
        eventHub.start()

        val actualResult = eventHub.getSharedState(
            SharedStateType.XDM,
            "invalidextension",
            null,
            true,
            SharedStateResolution.ANY
        )

        assertNull(actualResult)
    }

    @Test
    fun testGetSharedState_InvalidExtension() {
        registerExtension(TestExtension_InitError::class.java)

        eventHub.start()

        val actualResult = eventHub.getSharedState(
            SharedStateType.XDM,
            TestExtension_InitError.EXTENSION_NAME,
            null,
            true,
            SharedStateResolution.ANY
        )

        assertNull(actualResult)
    }

    @Test
    fun testGetSharedState_AfterSet() {
        val stateAtEvent1: MutableMap<String, Any?> = mutableMapOf("One" to 1, "Yes" to true)
        eventHub.start()
        eventHub.dispatch(event1)
        eventHub.createSharedState(
            SharedStateType.STANDARD,
            TestExtension.EXTENSION_NAME,
            stateAtEvent1,
            event1
        )

        verifySharedState(
            SharedStateType.STANDARD,
            event1,
            SharedStateResult(SharedStateStatus.SET, stateAtEvent1)
        )

        verifySharedState(
            SharedStateType.STANDARD,
            event1,
            SharedStateResult(SharedStateStatus.SET, stateAtEvent1),
            resolution = SharedStateResolution.LAST_SET
        )
    }

    @Test
    fun testGetSharedState_CaseInsensitive() {
        val stateAtEvent1: MutableMap<String, Any?> = mutableMapOf("One" to 1, "Yes" to true)
        eventHub.start()
        eventHub.dispatch(event1)
        eventHub.createSharedState(
            SharedStateType.STANDARD,
            TestExtension.EXTENSION_NAME,
            stateAtEvent1,
            event1
        )

        val res = eventHub.getSharedState(
            SharedStateType.STANDARD,
            TestExtension.EXTENSION_NAME.toUpperCase(),
            event1,
            false,
            SharedStateResolution.ANY
        )

        assertEquals(res?.status, SharedStateStatus.SET)
        assertEquals(res?.value, stateAtEvent1)
    }

    @Test
    fun testGetSharedState_AfterPending() {
        eventHub.start()
        eventHub.dispatch(event1)
        eventHub.createPendingSharedState(
            SharedStateType.STANDARD,
            TestExtension.EXTENSION_NAME,
            event1
        )

        verifySharedState(
            SharedStateType.STANDARD,
            event1,
            SharedStateResult(SharedStateStatus.PENDING, null)
        )

        verifySharedState(
            SharedStateType.STANDARD,
            event1,
            SharedStateResult(SharedStateStatus.NONE, null),
            resolution = SharedStateResolution.LAST_SET
        )
    }

    @Test
    fun testGetSharedState_AfterPendingResolved() {
        val stateAtEvent1: MutableMap<String, Any?> = mutableMapOf("One" to 1, "Yes" to true)
        eventHub.start()
        eventHub.dispatch(event1)
        val resolver = eventHub.createPendingSharedState(
            SharedStateType.STANDARD,
            TestExtension.EXTENSION_NAME,
            event1
        )

        resolver?.resolve(stateAtEvent1)

        verifySharedState(
            SharedStateType.STANDARD,
            event1,
            SharedStateResult(SharedStateStatus.SET, stateAtEvent1)
        )

        verifySharedState(
            SharedStateType.STANDARD,
            event1,
            SharedStateResult(SharedStateStatus.SET, stateAtEvent1),
            resolution = SharedStateResolution.LAST_SET
        )
    }

    @Test
    fun testGetSharedState_NoState() {
        eventHub.dispatch(event1)
        verifySharedState(
            SharedStateType.STANDARD,
            event1,
            SharedStateResult(SharedStateStatus.NONE, null)
        )

        verifySharedState(
            SharedStateType.STANDARD,
            event1,
            SharedStateResult(SharedStateStatus.NONE, null),
            resolution = SharedStateResolution.LAST_SET
        )
    }

    @Test
    fun testGetSharedState_MultipleEvents() {
        eventHub.start()

        eventHub.dispatch(event1)
        val stateAtEvent1: MutableMap<String, Any?> = mutableMapOf("One" to 1)
        eventHub.createSharedState(
            SharedStateType.STANDARD,
            TestExtension.EXTENSION_NAME,
            stateAtEvent1,
            event1
        )

        eventHub.dispatch(event2)
        val stateAtEvent2: MutableMap<String, Any?> = mutableMapOf("Two" to 2)
        val resolverEvent2 = eventHub.createPendingSharedState(
            SharedStateType.STANDARD,
            TestExtension.EXTENSION_NAME,
            event2
        )

        eventHub.dispatch(event3)
        val stateAtEvent3: MutableMap<String, Any?> = mutableMapOf("Three" to 3)
        eventHub.createSharedState(
            SharedStateType.STANDARD,
            TestExtension.EXTENSION_NAME,
            stateAtEvent3,
            event3
        )

        eventHub.dispatch(event4)

        verifySharedState(
            SharedStateType.STANDARD,
            event1,
            SharedStateResult(SharedStateStatus.SET, stateAtEvent1)
        )

        verifySharedState(
            SharedStateType.STANDARD,
            event1,
            SharedStateResult(SharedStateStatus.SET, stateAtEvent1),
            resolution = SharedStateResolution.LAST_SET

        )

        verifySharedState(
            SharedStateType.STANDARD,
            event2,
            SharedStateResult(SharedStateStatus.PENDING, stateAtEvent1)
        )

        verifySharedState(
            SharedStateType.STANDARD,
            event2,
            SharedStateResult(SharedStateStatus.SET, stateAtEvent1),
            resolution = SharedStateResolution.LAST_SET
        )

        verifySharedState(
            SharedStateType.STANDARD,
            event3,
            SharedStateResult(SharedStateStatus.SET, stateAtEvent3)
        )

        verifySharedState(
            SharedStateType.STANDARD,
            event3,
            SharedStateResult(SharedStateStatus.SET, stateAtEvent3),
            resolution = SharedStateResolution.LAST_SET
        )

        verifySharedState(
            SharedStateType.STANDARD,
            event4,
            SharedStateResult(SharedStateStatus.SET, stateAtEvent3)
        )

        verifySharedState(
            SharedStateType.STANDARD,
            event4,
            SharedStateResult(SharedStateStatus.SET, stateAtEvent3),
            resolution = SharedStateResolution.LAST_SET
        )

        resolverEvent2?.resolve(stateAtEvent2)
        verifySharedState(
            SharedStateType.STANDARD,
            event2,
            SharedStateResult(SharedStateStatus.SET, stateAtEvent2)
        )

        verifySharedState(
            SharedStateType.STANDARD,
            event2,
            SharedStateResult(SharedStateStatus.SET, stateAtEvent2),
            resolution = SharedStateResolution.LAST_SET
        )
    }

    @Test
    fun testGetSharedState_NullEvent() {
        eventHub.start()

        val state1: MutableMap<String, Any?> = mutableMapOf("One" to 1)
        eventHub.createSharedState(
            SharedStateType.STANDARD,
            TestExtension.EXTENSION_NAME,
            state1,
            null
        )

        verifySharedState(
            SharedStateType.STANDARD,
            null,
            SharedStateResult(SharedStateStatus.SET, state1)
        )

        verifySharedState(
            SharedStateType.STANDARD,
            null,
            SharedStateResult(SharedStateStatus.SET, state1),
            resolution = SharedStateResolution.LAST_SET
        )

        eventHub.dispatch(event1)

        val state2: MutableMap<String, Any?> = mutableMapOf("Two" to 2)
        eventHub.createSharedState(
            SharedStateType.STANDARD,
            TestExtension.EXTENSION_NAME,
            state2,
            null
        )

        verifySharedState(
            SharedStateType.STANDARD,
            null,
            SharedStateResult(SharedStateStatus.SET, state2)
        )

        verifySharedState(
            SharedStateType.STANDARD,
            null,
            SharedStateResult(SharedStateStatus.SET, state2),
            resolution = SharedStateResolution.LAST_SET
        )

        eventHub.dispatch(event2)

        verifySharedState(
            SharedStateType.STANDARD,
            event1,
            SharedStateResult(SharedStateStatus.SET, state1)
        )

        verifySharedState(
            SharedStateType.STANDARD,
            event1,
            SharedStateResult(SharedStateStatus.SET, state1),
            resolution = SharedStateResolution.LAST_SET
        )

        verifySharedState(
            SharedStateType.STANDARD,
            event2,
            SharedStateResult(SharedStateStatus.SET, state2)
        )

        verifySharedState(
            SharedStateType.STANDARD,
            event2,
            SharedStateResult(SharedStateStatus.SET, state2),
            resolution = SharedStateResolution.LAST_SET
        )
    }

    @Test
    fun testGetSharedState_AfterSettingInvalidState() {
        class CustomClass
        val stateAtEvent1: MutableMap<String, Any?> = mutableMapOf("One" to 1, "Yes" to CustomClass())

        eventHub.start()
        eventHub.dispatch(event1)
        assertTrue {
            eventHub.createSharedState(
                SharedStateType.STANDARD,
                TestExtension.EXTENSION_NAME,
                stateAtEvent1,
                event1
            )
        }

        verifySharedState(SharedStateType.STANDARD, event1, SharedStateResult(SharedStateStatus.SET, null))
    }

    @Test
    fun testGetSharedState_Any_WithBarrier() {
        registerExtension(TestExtension_Barrier::class.java)
        //  Stop processing after this event
        TestExtension_Barrier.BARRIER_EVENT = event2

        eventHub.start()
        eventHub.dispatch(event1)
        eventHub.dispatch(event2)
        // Wait for events to get processed
        val latch = CountDownLatch(1)
        latch.await(500, TimeUnit.MILLISECONDS)

        val state1: MutableMap<String, Any?> = mutableMapOf("One" to 1)
        eventHub.createSharedState(
            SharedStateType.STANDARD,
            TestExtension_Barrier.EXTENSION_NAME,
            state1,
            event1
        )

        // event1: Returns valid shared state
        verifySharedState(
            SharedStateType.STANDARD,
            event1,
            SharedStateResult(SharedStateStatus.SET, state1),
            SharedStateResolution.ANY,
            true,
            TestExtension_Barrier.EXTENSION_NAME
        )

        // event2: With barrier returns pending state as it has not processed the event
        verifySharedState(
            SharedStateType.STANDARD,
            event2,
            SharedStateResult(SharedStateStatus.PENDING, state1),
            SharedStateResolution.ANY,
            true,
            TestExtension_Barrier.EXTENSION_NAME
        )
    }

    @Test
    fun testGetSharedState_Any_NoBarrier() {
        registerExtension(TestExtension_Barrier::class.java)
        //  Stop processing after this event
        TestExtension_Barrier.BARRIER_EVENT = event2

        eventHub.start()
        eventHub.dispatch(event1)
        eventHub.dispatch(event2)
        // Wait for events to get processed
        val latch = CountDownLatch(1)
        latch.await(500, TimeUnit.MILLISECONDS)

        val state1: MutableMap<String, Any?> = mutableMapOf("One" to 1)
        eventHub.createSharedState(
            SharedStateType.STANDARD,
            TestExtension_Barrier.EXTENSION_NAME,
            state1,
            event1
        )

        // event1: Returns valid shared state
        verifySharedState(
            SharedStateType.STANDARD,
            event1,
            SharedStateResult(SharedStateStatus.SET, state1),
            SharedStateResolution.ANY,
            false,
            TestExtension_Barrier.EXTENSION_NAME
        )

        // event2: Without barrier returns state1
        verifySharedState(
            SharedStateType.STANDARD,
            event2,
            SharedStateResult(SharedStateStatus.SET, state1),
            SharedStateResolution.ANY,
            false,
            TestExtension_Barrier.EXTENSION_NAME
        )
    }

    @Test
    fun testGetSharedState_LastSet_WithBarrier() {
        registerExtension(TestExtension_Barrier::class.java)
        //  Stop processing after this event
        TestExtension_Barrier.BARRIER_EVENT = event3

        eventHub.start()
        eventHub.dispatch(event1)
        eventHub.dispatch(event2)
        eventHub.dispatch(event3)
        // Wait for events to get processed
        val latch = CountDownLatch(1)
        latch.await(500, TimeUnit.MILLISECONDS)

        val state1: MutableMap<String, Any?> = mutableMapOf("One" to 1)
        eventHub.createSharedState(
            SharedStateType.STANDARD,
            TestExtension_Barrier.EXTENSION_NAME,
            state1,
            event1
        )
        eventHub.createPendingSharedState(
            SharedStateType.STANDARD,
            TestExtension_Barrier.EXTENSION_NAME,
            event2
        )

        // event1: Returns valid shared state
        verifySharedState(
            SharedStateType.STANDARD,
            event1,
            SharedStateResult(SharedStateStatus.SET, state1),
            SharedStateResolution.LAST_SET,
            true,
            TestExtension_Barrier.EXTENSION_NAME
        )

        // event2: Returns state set for event1 as shared state for event2 is pending
        verifySharedState(
            SharedStateType.STANDARD,
            event2,
            SharedStateResult(SharedStateStatus.SET, state1),
            SharedStateResolution.LAST_SET,
            true,
            TestExtension_Barrier.EXTENSION_NAME
        )

        // event3: Returns pending shared state as the extension has not processed the event
        verifySharedState(
            SharedStateType.STANDARD,
            event3,
            SharedStateResult(SharedStateStatus.PENDING, state1),
            SharedStateResolution.LAST_SET,
            true,
            TestExtension_Barrier.EXTENSION_NAME
        )
    }

    @Test
    fun testGetSharedState_LastSet_NoBarrier() {
        registerExtension(TestExtension_Barrier::class.java)
        //  Stop processing after this event
        TestExtension_Barrier.BARRIER_EVENT = event3

        eventHub.start()
        eventHub.dispatch(event1)
        eventHub.dispatch(event2)
        eventHub.dispatch(event3)
        // Wait for events to get processed
        val latch = CountDownLatch(1)
        latch.await(500, TimeUnit.MILLISECONDS)

        val state1: MutableMap<String, Any?> = mutableMapOf("One" to 1)
        eventHub.createSharedState(
            SharedStateType.STANDARD,
            TestExtension_Barrier.EXTENSION_NAME,
            state1,
            event1
        )
        eventHub.createPendingSharedState(
            SharedStateType.STANDARD,
            TestExtension_Barrier.EXTENSION_NAME,
            event2
        )

        // event1: Returns valid shared state
        verifySharedState(
            SharedStateType.STANDARD,
            event1,
            SharedStateResult(SharedStateStatus.SET, state1),
            SharedStateResolution.LAST_SET,
            false,
            TestExtension_Barrier.EXTENSION_NAME
        )

        // event2: Returns state set for event1 as shared state for event2 is pending
        verifySharedState(
            SharedStateType.STANDARD,
            event2,
            SharedStateResult(SharedStateStatus.SET, state1),
            SharedStateResolution.LAST_SET,
            false,
            TestExtension_Barrier.EXTENSION_NAME
        )

        // event3: Returns state set for event1 as event3 has not been processed
        verifySharedState(
            SharedStateType.STANDARD,
            event3,
            SharedStateResult(SharedStateStatus.SET, state1),
            SharedStateResolution.LAST_SET,
            false,
            TestExtension_Barrier.EXTENSION_NAME
        )
    }

    // / ExtensionInfo shared state tests
    /*
     Expected format:
     {
       "version" : "0.0.1",
       "wrapper" : {
         "type" : "F",
         "friendlyName" : "Flutter"
       }
       "extensions" : {
         "mockExtension" : {
           "version" : "0.0.1"
         },
         "mockExtensionTwo" : {
           "version" : "0.0.1"
         }
       }
     }
     */
    @Test
    fun testEventHubRegisteredExtensionSharesState() {
        val latch = CountDownLatch(1)

        val capturedEvents = mutableListOf<Event>()
        eventHub.getExtensionContainer(EventHubPlaceholderExtension::class.java)?.registerEventListener(EventType.HUB, EventSource.SHARED_STATE) {
            capturedEvents.add(it)
            latch.countDown()
        }

        eventHub.wrapperType = WrapperType.FLUTTER
        registerExtension(TestExtension2::class.java)
        eventHub.start()
        latch.await(250, TimeUnit.MILLISECONDS)

        assertEquals(EventHubConstants.STATE_CHANGE, capturedEvents[0]?.name)
        assertEquals(
            mapOf(
                EventHubConstants.EventDataKeys.Configuration.EVENT_STATE_OWNER to EventHubConstants.NAME
            ),
            capturedEvents[0]?.eventData
        )

        val expectedData = mapOf(
            EventHubConstants.EventDataKeys.VERSION to EventHubConstants.VERSION_NUMBER,
            EventHubConstants.EventDataKeys.WRAPPER to mapOf(
                EventHubConstants.EventDataKeys.TYPE to WrapperType.FLUTTER.wrapperTag,
                EventHubConstants.EventDataKeys.FRIENDLY_NAME to WrapperType.FLUTTER.friendlyName
            ),
            EventHubConstants.EventDataKeys.EXTENSIONS to mapOf(
                TestExtension.EXTENSION_NAME to mapOf(
                    EventHubConstants.EventDataKeys.FRIENDLY_NAME to TestExtension.FRIENDLY_NAME,
                    EventHubConstants.EventDataKeys.VERSION to TestExtension.VERSION
                ),
                TestExtension2.EXTENSION_NAME to mapOf(
                    EventHubConstants.EventDataKeys.FRIENDLY_NAME to TestExtension2.FRIENDLY_NAME,
                    EventHubConstants.EventDataKeys.VERSION to TestExtension2.VERSION,
                    EventHubConstants.EventDataKeys.METADATA to TestExtension2.METADATA
                )
            )
        )
        val sharedStateResult = eventHub.getSharedState(
            SharedStateType.STANDARD,
            EventHubConstants.NAME,
            null,
            false,
            SharedStateResolution.ANY
        )

        assertEquals(expectedData, sharedStateResult?.value)
    }

    @Test
    fun testGetSharedState_AfterResolvingWithInvalidState() {
        class CustomClass
        val stateAtEvent1: MutableMap<String, Any?> = mutableMapOf("One" to 1, "Yes" to CustomClass())

        eventHub.start()
        eventHub.dispatch(event1)

        val resolver = eventHub.createPendingSharedState(
            SharedStateType.STANDARD,
            TestExtension.EXTENSION_NAME,
            event1
        )

        verifySharedState(SharedStateType.STANDARD, event1, SharedStateResult(SharedStateStatus.PENDING, null))

        resolver?.resolve(stateAtEvent1)

        verifySharedState(SharedStateType.STANDARD, event1, SharedStateResult(SharedStateStatus.SET, null))
    }

    @Test
    fun testEventHubRegisterAndUnregisterExtensionSharesState() {
        MobileCore.setLogLevel(LoggingMode.VERBOSE)
        val latch = CountDownLatch(2)

        val capturedEvents = mutableListOf<Event>()
        eventHub.getExtensionContainer(EventHubPlaceholderExtension::class.java)?.registerEventListener(EventType.HUB, EventSource.SHARED_STATE) {
            capturedEvents.add(it)
            latch.countDown()
        }

        eventHub.wrapperType = WrapperType.FLUTTER
        registerExtension(TestExtension2::class.java)
        eventHub.start()
        unregisterExtension(TestExtension2::class.java)

        latch.await(250, TimeUnit.MILLISECONDS)

        // Shared state published after start
        assertEquals(capturedEvents[0]?.name, EventHubConstants.STATE_CHANGE)
        assertEquals(
            capturedEvents[0]?.eventData,
            mapOf(
                EventHubConstants.EventDataKeys.Configuration.EVENT_STATE_OWNER to EventHubConstants.NAME
            )
        )
        val expectedData1 = mapOf(
            EventHubConstants.EventDataKeys.VERSION to EventHubConstants.VERSION_NUMBER,
            EventHubConstants.EventDataKeys.WRAPPER to mapOf(
                EventHubConstants.EventDataKeys.TYPE to WrapperType.FLUTTER.wrapperTag,
                EventHubConstants.EventDataKeys.FRIENDLY_NAME to WrapperType.FLUTTER.friendlyName
            ),
            EventHubConstants.EventDataKeys.EXTENSIONS to mapOf(
                TestExtension.EXTENSION_NAME to mapOf(
                    EventHubConstants.EventDataKeys.FRIENDLY_NAME to TestExtension.FRIENDLY_NAME,
                    EventHubConstants.EventDataKeys.VERSION to TestExtension.VERSION
                ),
                TestExtension2.EXTENSION_NAME to mapOf(
                    EventHubConstants.EventDataKeys.FRIENDLY_NAME to TestExtension2.FRIENDLY_NAME,
                    EventHubConstants.EventDataKeys.VERSION to TestExtension2.VERSION,
                    EventHubConstants.EventDataKeys.METADATA to TestExtension2.METADATA
                )
            )
        )
        val sharedStateResult1 = eventHub.getSharedState(
            SharedStateType.STANDARD,
            EventHubConstants.NAME,
            capturedEvents[0],
            false,
            SharedStateResolution.ANY
        )
        assertEquals(expectedData1, sharedStateResult1?.value)

        // Shared state published after unregister
        assertEquals(EventHubConstants.STATE_CHANGE, capturedEvents[1]?.name)
        assertEquals(
            mapOf(
                EventHubConstants.EventDataKeys.Configuration.EVENT_STATE_OWNER to EventHubConstants.NAME
            ),
            capturedEvents[1]?.eventData
        )

        val expectedData2 = mapOf(
            EventHubConstants.EventDataKeys.VERSION to EventHubConstants.VERSION_NUMBER,
            EventHubConstants.EventDataKeys.WRAPPER to mapOf(
                EventHubConstants.EventDataKeys.TYPE to WrapperType.FLUTTER.wrapperTag,
                EventHubConstants.EventDataKeys.FRIENDLY_NAME to WrapperType.FLUTTER.friendlyName
            ),
            EventHubConstants.EventDataKeys.EXTENSIONS to mapOf(
                TestExtension.EXTENSION_NAME to mapOf(
                    EventHubConstants.EventDataKeys.FRIENDLY_NAME to TestExtension.FRIENDLY_NAME,
                    EventHubConstants.EventDataKeys.VERSION to TestExtension.VERSION
                )
            )
        )
        val sharedStateResult2 = eventHub.getSharedState(
            SharedStateType.STANDARD,
            EventHubConstants.NAME,
            capturedEvents[1],
            false,
            SharedStateResolution.ANY
        )
        assertEquals(expectedData2, sharedStateResult2?.value)
    }

    // Event listener tests
    @Test
    fun testExtensionListener() {
        val latch = CountDownLatch(1)
        val testEvent = Event.Builder("Sample event", eventType, eventSource).build()

        val extensionContainer = eventHub.getExtensionContainer(TestExtension::class.java)
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

        val extensionContainer = eventHub.getExtensionContainer(TestExtension::class.java)
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

        val extensionContainer = eventHub.getExtensionContainer(TestExtension::class.java)
        extensionContainer?.registerEventListener(eventType, eventSource) {
            latch.countDown()
        }

        eventHub.dispatch(testEvent)

        assertFalse {
            latch.await(250, TimeUnit.MILLISECONDS)
        }
    }

    @Test
    fun testExtensionListener_QueuesEventsBeforeStart() {
        val latch = CountDownLatch(2)
        val testEvent = Event.Builder("Test event", eventType, eventSource).build()

        val extensionContainer = eventHub.getExtensionContainer(TestExtension::class.java)
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

        val extensionContainer = eventHub.getExtensionContainer(TestExtension::class.java)
        extensionContainer?.registerEventListener(
            eventType,
            eventSource
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
        val testResponseEvent = Event.Builder("Test response event", eventType, eventSource).inResponseToEvent(testEvent).build()
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
        val latch = CountDownLatch(3)
        val capturedEvents = mutableListOf<Event>()

        val testEvent = Event.Builder("Test event", eventType, eventSource).build()
        val testResponseEvent = Event.Builder("Test response event", eventType, eventSource).inResponseToEvent(testEvent).build()
        eventHub.registerListener(EventType.WILDCARD, EventSource.WILDCARD) {
            capturedEvents.add(it)
            latch.countDown()
        }

        eventHub.start()
        eventHub.dispatch(testEvent)
        eventHub.dispatch(testResponseEvent)
        assertTrue {
            latch.await(250, TimeUnit.MILLISECONDS)
        }

        // EventHub shared state event is dispatched first.
        assertEquals(capturedEvents[0].name, EventHubConstants.STATE_CHANGE)
        assertEquals(capturedEvents[0].eventData, mapOf(EventHubConstants.EventDataKeys.Configuration.EVENT_STATE_OWNER to EventHubConstants.NAME))

        assertEquals(capturedEvents[1], testEvent)
        assertEquals(capturedEvents[2], testResponseEvent)
    }

    @Test
    fun testResponseListener() {
        val latch = CountDownLatch(1)
        val capturedEvents = mutableListOf<Pair<Event?, AdobeError?>>()

        val testEvent = Event.Builder("Test event", eventType, eventSource).build()
        val testResponseEvent = Event.Builder("Test response event", eventType, eventSource).inResponseToEvent(testEvent).build()

        eventHub.registerResponseListener(
            testEvent,
            250,
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
        val testResponseEvent = Event.Builder("Test response event", eventType, eventSource).inResponseToEvent(testEvent).build()

        eventHub.registerResponseListener(
            testEvent,
            5000,
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
            testEvent,
            250,
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
    fun testResponseListener_HandlesException() {
        val latch = CountDownLatch(2)
        val capturedEvents = mutableListOf<Pair<Event?, AdobeError?>>()

        val testEvent = Event.Builder("Test event", eventType, eventSource).build()
        val testResponseEvent = Event.Builder("Test response event", eventType, eventSource).inResponseToEvent(testEvent).build()

        val testEvent1 = Event.Builder("Test event1", eventType, eventSource).build()

        val responseCallback = object : AdobeCallbackWithError<Event> {
            override fun call(value: Event?) {
                capturedEvents.add(Pair(value, null))
                latch.countDown()
                throw Exception()
            }

            override fun fail(error: AdobeError?) {
                capturedEvents.add(Pair(null, error))
                latch.countDown()
                throw Exception()
            }
        }

        eventHub.registerResponseListener(testEvent, 250, responseCallback)
        eventHub.registerResponseListener(testEvent1, 250, responseCallback)

        eventHub.start()
        eventHub.dispatch(testEvent)
        eventHub.dispatch(testResponseEvent)
        eventHub.dispatch(testEvent1)
        latch.await(1000, TimeUnit.MILLISECONDS)

        assertEquals(capturedEvents, listOf<Pair<Event?, AdobeError?>>(Pair(testResponseEvent, null), Pair(null, AdobeError.CALLBACK_TIMEOUT)))
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

    // WrapperType Tests
    @Test
    fun testDefaultWrapperType() {
        assertEquals(
            eventHub.wrapperType,
            WrapperType.NONE
        )
    }

    @Test
    fun testUpdateWrapperTypeBeforeStart() {
        eventHub.wrapperType = WrapperType.FLUTTER
        assertEquals(
            eventHub.wrapperType,
            WrapperType.FLUTTER
        )

        eventHub.wrapperType = WrapperType.REACT_NATIVE
        assertEquals(
            eventHub.wrapperType,
            WrapperType.REACT_NATIVE
        )

        eventHub.wrapperType = WrapperType.CORDOVA
        assertEquals(
            eventHub.wrapperType,
            WrapperType.CORDOVA
        )
    }

    @Test
    fun testUpdateWrapperTypeAfterStart() {
        eventHub.wrapperType = WrapperType.FLUTTER
        assertEquals(
            eventHub.wrapperType,
            WrapperType.FLUTTER
        )

        eventHub.start()

        // Updates to wrapper type fail after start() call
        eventHub.wrapperType = WrapperType.REACT_NATIVE
        assertEquals(
            eventHub.wrapperType,
            WrapperType.FLUTTER
        )

        eventHub.wrapperType = WrapperType.CORDOVA
        assertEquals(
            eventHub.wrapperType,
            WrapperType.FLUTTER
        )
    }

    // Preprocessor tests
    @Test
    fun testPreprocessor_HandlesEvents() {
        val event = Event.Builder("evt1", "type", "source").build()
        val processedEvent = Event.Builder("evt2", "processedtype", "processedsource").build()

        val processor = object :
            EventPreprocessor {
            override fun process(it: Event): Event {
                if (it == event) {
                    return processedEvent
                } else {
                    return it
                }
            }
        }

        val capturedEvents = mutableListOf<Event>()
        val extensionContainer = eventHub.getExtensionContainer(TestExtension::class.java)
        val latch = CountDownLatch(2)
        extensionContainer?.registerEventListener("type", "source") {
            capturedEvents.add(it)
            latch.countDown()
        }
        extensionContainer?.registerEventListener("processedtype", "processedsource") {
            capturedEvents.add(it)
            latch.countDown()
        }

        eventHub.registerEventPreprocessor(processor)
        eventHub.start()
        eventHub.dispatch(event)
        // This will timeout as only one event is dispatched
        assertFalse {
            latch.await(500, TimeUnit.MILLISECONDS)
        }
        assertEquals(listOf(processedEvent), capturedEvents)
    }

    @Test
    fun testPreprocessors_ChainedProcessing() {
        val event = Event.Builder("evt1", "type", "source").build()
        val processedEvent1 = Event.Builder("evt2", "processedtype", "processedsource").build()
        val processedEvent2 = Event.Builder("evt3", "processedtype2", "processedsource2").build()

        val processor1 = object :
            EventPreprocessor {
            override fun process(it: Event): Event {
                if (it == event) {
                    return processedEvent1
                } else {
                    return it
                }
            }
        }

        val processor2 = object :
            EventPreprocessor {
            override fun process(it: Event): Event {
                if (it == processedEvent1) {
                    return processedEvent2
                } else {
                    return it
                }
            }
        }

        val capturedEvents = mutableListOf<Event>()
        val extensionContainer = eventHub.getExtensionContainer(TestExtension::class.java)
        val latch = CountDownLatch(1)
        extensionContainer?.registerEventListener("processedtype2", "processedsource2") {
            capturedEvents.add(it)
            latch.countDown()
        }

        eventHub.registerEventPreprocessor(processor1)
        eventHub.registerEventPreprocessor(processor2)

        eventHub.start()
        eventHub.dispatch(event)
        // This will timeout as only one event is dispatched
        assertTrue {
            latch.await(1000, TimeUnit.MILLISECONDS)
        }
        assertEquals(listOf(processedEvent2), capturedEvents)
    }

    @Test
    fun testPreprocessor_IgnoreDuplicateProcessor() {
        val event = Event.Builder("evt", "type", "source").build()
        val processedEvent1 = Event.Builder("evt1", "processedtype", "processedsource").build()
        val processedEvent2 = Event.Builder("evt2", "processedtype2", "processedsource2").build()

        val processor = object :
            EventPreprocessor {
            override fun process(it: Event): Event {
                if (it == event) {
                    return processedEvent1
                }
                if (it == processedEvent1) {
                    return processedEvent2
                }
                return it
            }
        }

        val capturedEvents = mutableListOf<Event>()
        val extensionContainer = eventHub.getExtensionContainer(TestExtension::class.java)
        val latch = CountDownLatch(1)
        extensionContainer?.registerEventListener("processedtype", "processedsource") {
            capturedEvents.add(it)
            latch.countDown()
        }

        eventHub.registerEventPreprocessor(processor)
        eventHub.registerEventPreprocessor(processor)

        eventHub.start()
        eventHub.dispatch(event)
        // This will timeout as only one event is dispatched
        assertTrue {
            latch.await(500, TimeUnit.MILLISECONDS)
        }
        assertEquals(listOf(processedEvent1), capturedEvents)
    }

    // Event History tests
    @Test
    fun testEventHistory_recordsEventWithMask() {
        val eventHistory = mock(EventHistory::class.java)
        val eventHub = EventHub(eventHistory)

        val event = Event.Builder("name", "type", "source", arrayOf("key"))
            .setEventData(mapOf("key" to "value"))
            .build()
        // No Mask
        val event1 = Event.Builder("name", "type", "source")
            .setEventData(mapOf("key" to "value"))
            .build()
        val event2 = Event.Builder("name", "type", "source", arrayOf("key1"))
            .setEventData(mapOf("key1" to "value1"))
            .build()

        eventHub.start()
        eventHub.dispatch(event)
        eventHub.dispatch(event1)
        eventHub.dispatch(event2)

        val latch = CountDownLatch(1)
        latch.await(1000, TimeUnit.MILLISECONDS)

        val captor = ArgumentCaptor.forClass(Event::class.java)
        verify(eventHistory, times(2)).recordEvent(captor.capture(), any())
        assertEquals(listOf(event, event2), captor.allValues)
    }
}
