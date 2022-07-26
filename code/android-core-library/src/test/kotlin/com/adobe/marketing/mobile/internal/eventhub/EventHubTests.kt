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
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.modules.junit4.PowerMockRunner
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
}

@RunWith(PowerMockRunner::class)
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
    private fun verifySharedState(type: SharedStateType, event: Event?, ret: SharedStateResult, resolution: SharedStateResolution = SharedStateResolution.ANY, barrier: Boolean = false) {
        val res = eventHub.getSharedState(
            type,
            TestExtension.EXTENSION_NAME,
            event,
            barrier,
            resolution
        )

        assertEquals(res?.status, ret.status)
        assertEquals(res?.value, ret.value)
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
        extensionContainer?.registerEventListener(EventType.TYPE_HUB, EventSource.TYPE_SHARED_STATE) {
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
        val latch = CountDownLatch(1)

        val capturedEvents = mutableListOf<Event>()
        val extensionContainer = eventHub.getExtensionContainer(TestExtension::class.java)
        extensionContainer?.registerEventListener(EventType.TYPE_HUB, EventSource.TYPE_SHARED_STATE) {
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
            SharedStateResult(SharedStateStatus.SET, state2),
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
        eventHub.getExtensionContainer(EventHubPlaceholderExtension::class.java)?.registerEventListener(EventType.TYPE_HUB, EventSource.TYPE_SHARED_STATE) {
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
                    EventHubConstants.EventDataKeys.METADATA to TestExtension2.METADATA,
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
    fun testEventHubRegisterAndUnregisterExtensionSharesState() {
        MobileCore.setLogLevel(LoggingMode.VERBOSE)
        val latch = CountDownLatch(2)

        val capturedEvents = mutableListOf<Event>()
        eventHub.getExtensionContainer(EventHubPlaceholderExtension::class.java)?.registerEventListener(EventType.TYPE_HUB, EventSource.TYPE_SHARED_STATE) {
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
                    EventHubConstants.EventDataKeys.METADATA to TestExtension2.METADATA,
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
//        // setup
//        let sharedStateExpectation = XCTestExpectation(description: "Shared state should be shared by event hub once")
//        sharedStateExpectation.expectedFulfillmentCount = 2
//        sharedStateExpectation.assertForOverFulfill = true
//
//        eventHub.getExtensionContainer(MockExtension.self)?.registerListener(type: EventType.hub, source: EventSource.sharedState) { event in
//                if event.data?[EventHubConstants.EventDataKeys.Configuration.EVENT_STATE_OWNER] as? String == EventHubConstants.NAME { sharedStateExpectation.fulfill() }
//        }
//
//        // test
//        registerMockExtension(MockExtensionTwo.self)
//        eventHub.start()
//        eventHub.unregisterExtension(MockExtensionTwo.self, completion: { (_) in })
//
//        // verify
//        wait(for: [sharedStateExpectation], timeout: 1)
//        let sharedState = eventHub.getSharedState(extensionName: EventHubConstants.NAME, event: nil)!.value
//
//        let mockExtension = MockExtension(runtime: TestableExtensionRuntime())
//
//        let coreVersion = sharedState?[EventHubConstants.EventDataKeys.VERSION] as! String
//                let registeredExtensions = sharedState?[EventHubConstants.EventDataKeys.EXTENSIONS] as? [String: Any]
//        let mockDetails = registeredExtensions?[mockExtension.name] as? [String: String]
//
//        XCTAssertEqual(ConfigurationConstants.EXTENSION_VERSION, coreVersion) // should contain {version: coreVersion}
//        XCTAssertEqual(MockExtension.extensionVersion, mockDetails?[EventHubConstants.EventDataKeys.VERSION])
//    }

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
        val latch = CountDownLatch(3)
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

    // WrapperType Tests
    @Test
    fun testDefaultWrapperType() {
        assertEquals(
            eventHub.wrapperType, WrapperType.NONE
        )
    }

    @Test
    fun testUpdateWrapperTypeBeforeStart() {
        eventHub.wrapperType = WrapperType.FLUTTER
        assertEquals(
            eventHub.wrapperType, WrapperType.FLUTTER
        )

        eventHub.wrapperType = WrapperType.REACT_NATIVE
        assertEquals(
            eventHub.wrapperType, WrapperType.REACT_NATIVE
        )

        eventHub.wrapperType = WrapperType.CORDOVA
        assertEquals(
            eventHub.wrapperType, WrapperType.CORDOVA
        )
    }

    @Test
    fun testUpdateWrapperTypeAfterStart() {
        eventHub.wrapperType = WrapperType.FLUTTER
        assertEquals(
            eventHub.wrapperType, WrapperType.FLUTTER
        )

        eventHub.start()

        // Updates to wrapper type fail after start() call
        eventHub.wrapperType = WrapperType.REACT_NATIVE
        assertEquals(
            eventHub.wrapperType, WrapperType.FLUTTER
        )

        eventHub.wrapperType = WrapperType.CORDOVA
        assertEquals(
            eventHub.wrapperType, WrapperType.FLUTTER
        )
    }
}
