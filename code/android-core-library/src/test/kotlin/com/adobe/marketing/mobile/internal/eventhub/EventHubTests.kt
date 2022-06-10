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
import com.adobe.marketing.mobile.Extension
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.ExtensionError
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
    private val eventType = "Type"
    private val eventSource = "Source"
    private val event1: Event = Event.Builder("Event1", eventType, eventSource).build()
    private val event2: Event = Event.Builder("Event2", eventType, eventSource).build()

    // Helper to register extensions
    fun registerExtension(extensionClass: Class<out Extension>): EventHubError {
        var ret: EventHubError = EventHubError.Unknown

        val latch = CountDownLatch(1)
        EventHub.shared.registerExtension(extensionClass) { error ->
            ret = error
            latch.countDown()
        }
        if (!latch.await(1, TimeUnit.SECONDS)) throw Exception("Timeout registering extension")
        return ret
    }

    fun unregisterExtension(extensionClass: Class<out Extension>): EventHubError {
        var ret: EventHubError = EventHubError.Unknown

        val latch = CountDownLatch(1)
        EventHub.shared.unregisterExtension(extensionClass) { error ->
            ret = error
            latch.countDown()
        }
        if (!latch.await(1, TimeUnit.SECONDS)) throw Exception("Timeout unregistering extension")
        return ret
    }

    @Before
    fun setup() {
        EventHub.shared.shutdown()
        EventHub.shared = EventHub()
    }

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

        var ret = registerExtension(MockExtension::class.java)
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

        var ret = unregisterExtension(MockExtensions.MockExtensionKotlin::class.java)
        assertEquals(EventHubError.None, ret)
    }

    @Test
    fun testUnregisterExtensionFailure() {
        var ret = unregisterExtension(MockExtensions.MockExtensionKotlin::class.java)
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
    fun testSetSharedState_NullOrEmptyExtensionName() {
        var result: ExtensionError? = null

        registerExtension(MockExtensions.TestExtension::class.java)
        EventHub.shared.dispatch(event1) // Dispatch Event1

        val stateAtEvent1: MutableMap<String, Any?> = mutableMapOf("One" to 1, "Yes" to true)

        // Set state at event1 with null extension name
        assertFalse(
            EventHub.shared.setSharedState(
                SharedStateType.STANDARD,
                null, stateAtEvent1, event1
            ) {
                result = it
            }
        )
        assertEquals(result, ExtensionError.BAD_NAME)

        // Set state at event1 with empty extension name
        assertFalse(
            EventHub.shared.setSharedState(
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
        EventHub.shared.dispatch(event1) // Dispatch Event1

        val stateAtEvent1: MutableMap<String, Any?> = mutableMapOf("One" to 1, "Yes" to true)

        var result: ExtensionError? = null

        // Set state at event1
        assertFalse(
            EventHub.shared.setSharedState(
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
        registerExtension(MockExtensions.TestExtension::class.java)
        EventHub.shared.dispatch(event1) // Dispatch Event1

        // Set state at event1
        assertTrue(
            EventHub.shared.setSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, null, event1
            ) {
                fail("State should have been set successfully ${it.errorCode} - ${it.errorName}")
            }
        )
    }

    @Test
    fun testSetSharedState_OverwritePendingStateWithNonPendingState() {
        registerExtension(MockExtensions.TestExtension::class.java)
        EventHub.shared.dispatch(event1) // Dispatch Event1

        // Set pending state at event1
        assertTrue(
            EventHub.shared.setSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, null, event1
            ) {
                fail("State should have been set successfully ${it.errorCode} - ${it.errorName}")
            }
        )

        val stateAtEvent: MutableMap<String, Any?> = mutableMapOf("One" to 1, "Yes" to true)

        assertTrue(
            EventHub.shared.setSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, stateAtEvent, event1
            ) {
                fail("State should have been set successfully ${it.errorCode} - ${it.errorName}")
            }
        )
    }

    @Test
    fun testSetSharedState_NoPendingStateAtEvent() {
        registerExtension(MockExtensions.TestExtension::class.java)
        EventHub.shared.dispatch(event1) // Dispatch Event1

        // Set non pending state at event1
        val stateAtEvent1: MutableMap<String, Any?> = mutableMapOf("One" to 1, "Yes" to true)
        assertTrue(
            EventHub.shared.setSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, stateAtEvent1, event1
            ) {
                fail("State should have been set successfully. ${it.errorCode} - ${it.errorName}")
            }
        )

        // Verify that state at event1 cannot be overwritten
        val overwriteState: MutableMap<String, Any?> = mutableMapOf("Two" to 2, "No" to false)

        assertFalse(
            EventHub.shared.setSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, overwriteState, event1
            ) {
                fail("${it.errorCode} - ${it.errorName}")
            }
        )
    }

    @Test
    fun testSetSharedState_OverwriteNonPendingStateWithPendingState() {
        registerExtension(MockExtensions.TestExtension::class.java)
        EventHub.shared.dispatch(event1) // Dispatch Event1

        // Set non pending state at Event 1
        val stateAtEvent1: MutableMap<String, Any?> = mutableMapOf("One" to 1, "Yes" to true)
        assertTrue(
            EventHub.shared.setSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, stateAtEvent1, event1
            ) {
                fail("State should have been set successfully. ${it.errorCode} - ${it.errorName}")
            }
        )

        // Verify that state at event1 cannot be overwritten with a pending state
        val overwriteState: MutableMap<String, Any?>? = null

        assertFalse(
            EventHub.shared.setSharedState(
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
        EventHub.shared.getSharedState(
            SharedStateType.STANDARD,
            null, event1
        ) {
            result = it
        }
        assertEquals(result, ExtensionError.BAD_NAME)

        // Get state at event1 with empty extension name
        EventHub.shared.getSharedState(
            SharedStateType.STANDARD, "", event1
        ) {
            result = it
        }
        assertEquals(result, ExtensionError.BAD_NAME)
    }

    @Test
    fun testGetSharedState_ExtensionNotRegistered() {
        var result: ExtensionError? = null

        // Set state at event1
        EventHub.shared.getSharedState(
            SharedStateType.STANDARD, MockExtensions.TestExtension.extensionName, event1
        ) {
            result = it
        }
        assertEquals(result, ExtensionError.UNEXPECTED_ERROR)
    }

    @Test
    fun testGetSharedState_NoStateExistsYet() {
        registerExtension(MockExtensions.TestExtension::class.java)
        val errorCallback: (ExtensionError) -> Unit = {
            fail("Test failed ${it.errorCode} - ${it.errorName}")
        }

        // Dispatch Event 1
        EventHub.shared.dispatch(event1)

        assertNull(
            EventHub.shared.getSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, event1, errorCallback
            )
        )
    }

    @Test
    fun testGetSharedState_StateExistsAtVersion() {
        registerExtension(MockExtensions.TestExtension::class.java)
        val errorCallback: (ExtensionError) -> Unit = {
            fail("Test failed ${it.errorCode} - ${it.errorName}")
        }

        // Dispatch event1
        EventHub.shared.dispatch(event1)

        // Set non pending state at event1
        val stateAtEvent1: MutableMap<String, Any?> = mutableMapOf("One" to 1, "Yes" to true)
        assertTrue(
            EventHub.shared.setSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, stateAtEvent1, event1
            ) {
                fail("State should have been set successfully. ${it.errorCode} - ${it.errorName}")
            }
        )

        // Dispatch event2
        EventHub.shared.dispatch(event2)

        // Set state at event2
        val stateAtEvent2: MutableMap<String, Any?> = mutableMapOf("Two" to 1, "No" to false)
        assertTrue(
            EventHub.shared.setSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, stateAtEvent2, event2, errorCallback
            )
        )

        // Verify that the state at event1 and event2
        assertEquals(
            stateAtEvent1,
            EventHub.shared.getSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, event1, errorCallback
            )
        )
        assertEquals(
            stateAtEvent2,
            EventHub.shared.getSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, event2, errorCallback
            )
        )
    }

    @Test
    fun testGetSharedState_PreviousStateDoesNotExist() {
        registerExtension(MockExtensions.TestExtension::class.java)
        val errorCallback: (ExtensionError) -> Unit = {
            fail("Test failed ${it.errorCode} - ${it.errorName}")
        }

        // Dispatch event 1 & event2
        EventHub.shared.dispatch(event1)
        EventHub.shared.dispatch(event2)

        // Set state at event2
        val stateAtEvent2: MutableMap<String, Any?> = mutableMapOf("One" to 1, "Yes" to true)
        assertTrue(
            EventHub.shared.setSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, stateAtEvent2, event2, errorCallback
            )
        )

        // Verify that the state at event1 is still null
        assertNull(
            EventHub.shared.getSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, event1, errorCallback
            )
        )
        assertEquals(
            stateAtEvent2,
            EventHub.shared.getSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, event2, errorCallback
            )
        )
    }

    @Test
    fun testGetSharedState_FetchesLatestStateOnNullEvent() {
        registerExtension(MockExtensions.TestExtension::class.java)
        val errorCallback: (ExtensionError) -> Unit = {
            fail("Test failed ${it.errorCode} - ${it.errorName}")
        }

        // Dispatch event1
        EventHub.shared.dispatch(event1)

        // Set state at event1
        val state: MutableMap<String, Any?> = mutableMapOf("One" to 1, "Yes" to true)
        assertTrue(
            EventHub.shared.setSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, state, event1, errorCallback
            )
        )

        assertEquals(
            state,
            EventHub.shared.getSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, null, errorCallback
            )
        )
    }

    @Test
    fun testGetSharedState_OlderStateExists() {
        registerExtension(MockExtensions.TestExtension::class.java)
        val errorCallback: (ExtensionError) -> Unit = {
            fail("Test failed ${it.errorCode} - ${it.errorName}")
        }

        // Dispatch event1
        EventHub.shared.dispatch(event1)

        // Set state at event1
        val stateAtEvent1: MutableMap<String, Any?> = mutableMapOf("One" to 1, "Yes" to true)
        assertTrue(
            EventHub.shared.setSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, stateAtEvent1, event1, errorCallback
            )
        )

        // Dispatch event2
        EventHub.shared.dispatch(event2)

        // Verify that the state at event1 and event2  are the same and they equal [stateAtEvent1]
        assertEquals(
            stateAtEvent1,
            EventHub.shared.getSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, event1, errorCallback
            )
        )
        assertEquals(
            stateAtEvent1,
            EventHub.shared.getSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, event2, errorCallback
            )
        )
    }

    @Test
    fun testClearSharedState_NullOrEmptyExtensionName() {
        var result: ExtensionError? = null
        assertFalse(
            EventHub.shared.clearSharedState(
                SharedStateType.STANDARD,
                null
            ) {
                result = it
            }
        )
        assertEquals(result, ExtensionError.BAD_NAME)

        assertFalse(
            EventHub.shared.clearSharedState(
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
        assertFalse(
            EventHub.shared.clearSharedState(
                SharedStateType.STANDARD, MockExtensions.TestExtension.extensionName,
            ) {
                result = it
            }
        )

        assertEquals(result, ExtensionError.UNEXPECTED_ERROR)
    }

    @Test
    fun testClearSharedState_NoStateYet() {
        registerExtension(MockExtensions.TestExtension::class.java)

        assertTrue(
            EventHub.shared.clearSharedState(
                SharedStateType.STANDARD, MockExtensions.TestExtension.extensionName,
            ) {
                fail("State should have been cleared successfully")
            }
        )
    }

    @Test
    fun testClearSharedState() {
        registerExtension(MockExtensions.TestExtension::class.java)
        val errorCallback: (ExtensionError) -> Unit = {
            fail("Test failed ${it.errorCode} - ${it.errorName}")
        }
        EventHub.shared.dispatch(event1)
        EventHub.shared.dispatch(event2)

        val stateAtEvent1: MutableMap<String, Any?> = mutableMapOf("One" to 1, "Yes" to true)
        assertTrue(
            EventHub.shared.setSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, stateAtEvent1, event1, errorCallback
            )
        )

        val stateAtEvent2: MutableMap<String, Any?> = mutableMapOf("Twi" to 2, "No" to false)
        assertTrue(
            EventHub.shared.setSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, stateAtEvent2, event2, errorCallback
            )
        )

        // Verify that all the states are cleared
        assertTrue(
            EventHub.shared.clearSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, errorCallback
            )
        )
        assertNull(
            EventHub.shared.getSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, event1, errorCallback
            )
        )
        assertNull(
            EventHub.shared.getSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, event2, errorCallback
            )
        )
    }

    @Test
    fun testClearSharedState_DifferentStateType() {
        registerExtension(MockExtensions.TestExtension::class.java)
        val errorCallback: (ExtensionError) -> Unit = {
            fail("Test failed ${it.errorCode} - ${it.errorName}")
        }
        EventHub.shared.dispatch(event1)

        val stateAtEvent1: MutableMap<String, Any?> = mutableMapOf("One" to 1, "Yes" to true)
        val xdmStateAtEvent1: MutableMap<String, Any?> = mutableMapOf("Two" to 1, "No" to false)

        // Set Standard shared state
        assertTrue(
            EventHub.shared.setSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, stateAtEvent1, event1, errorCallback
            )
        )

        // Set Standard XDM shared state
        assertTrue(
            EventHub.shared.setSharedState(
                SharedStateType.XDM,
                MockExtensions.TestExtension.extensionName, xdmStateAtEvent1, event1, errorCallback
            )
        )

        // Set Standard Standard shared state
        assertTrue(
            EventHub.shared.clearSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, errorCallback
            )
        )

        // Verify that only standard state is cleared.
        assertNull(
            EventHub.shared.getSharedState(
                SharedStateType.STANDARD,
                MockExtensions.TestExtension.extensionName, event1, errorCallback
            )
        )
        assertEquals(
            xdmStateAtEvent1,
            EventHub.shared.getSharedState(
                SharedStateType.XDM,
                MockExtensions.TestExtension.extensionName, event1, errorCallback
            )
        )
    }
}
