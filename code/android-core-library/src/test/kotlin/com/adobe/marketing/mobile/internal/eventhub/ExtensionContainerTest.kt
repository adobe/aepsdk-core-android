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

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.stubbing.Answer
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

@RunWith(PowerMockRunner::class)
@PrepareForTest(ExtensionRuntime::class)
class ExtensionContainerTest {

    @Mock
    private lateinit var mockExtensionRuntime: ExtensionRuntime
    @Mock
    private lateinit var mockExecutorService: ExecutorService
    @Mock
    private lateinit var mockErrorCallback: (EventHubError) -> Unit

    private lateinit var extensionContainer: ExtensionContainer

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        doAnswer(Answer {
            // Create a mock Future to return
            val mockFuture: Future<*> = Mockito.mock(Future::class.java)
            // Make it so that the Callable passed to the ExecutorService is run when future result is queried via get()
            val callableArgument = it.getArgument<Callable<Any>>(0)
            `when`(mockFuture.get()).thenReturn(callableArgument.call())
            return@Answer mockFuture
        }).`when`(mockExecutorService).submit(any(Callable::class.java))

        extensionContainer = ExtensionContainer(MockExtension::class.java,
                mockExtensionRuntime, mockExecutorService, mockErrorCallback)
    }

    @Test
    fun testSetSharedState_NonPending_NoPreviousState() {
        val ret: SharedState.Status = extensionContainer.setSharedState(SharedStateType.STANDARD, mutableMapOf(), 0)

        verify(mockExecutorService).submit(any(Callable::class.java))
        assertEquals(SharedState.Status.SET, ret)
    }

    @Test
    fun testSetSharedState_Pending_NoPreviousState() {
        val ret: SharedState.Status = extensionContainer.setSharedState(SharedStateType.STANDARD, null, 0)

        verify(mockExecutorService).submit(any(Callable::class.java))
        assertEquals(SharedState.Status.PENDING, ret)
    }

    @Test
    fun testSetSharedState_PendingStateExists() {
        var ret: SharedState.Status = extensionContainer.setSharedState(SharedStateType.STANDARD, null, 0)
        assertEquals(SharedState.Status.PENDING, ret)

        val data = mutableMapOf<String, Any?> ("One" to 1, "Yes" to true)
        ret = extensionContainer.setSharedState(SharedStateType.STANDARD, data, 0)
        verify(mockExecutorService, times(2)).submit(any(Callable::class.java))
        assertEquals(SharedState.Status.SET, ret)
    }

    @Test
    fun testSetSharedState_PendingStateDoesNotExist() {
        var ret: SharedState.Status = extensionContainer.setSharedState(SharedStateType.STANDARD, mutableMapOf(), 0)
        assertEquals(SharedState.Status.SET, ret)

        val data = mutableMapOf<String, Any?> ("One" to 1, "Yes" to true)
        ret = extensionContainer.setSharedState(SharedStateType.STANDARD, data, 0)
        verify(mockExecutorService, times(2)).submit(any(Callable::class.java))
        assertEquals(SharedState.Status.NOT_SET, ret)
    }

    @Test
    fun testSetSharedState_ExecutorShutDown() {
        `when`(mockExecutorService.isShutdown).thenReturn(true)

        val ret: SharedState.Status = extensionContainer.setSharedState(SharedStateType.STANDARD, mutableMapOf(), 0)
        verify(mockExecutorService, times(0)).submit(any(Callable::class.java))
        assertEquals(SharedState.Status.NOT_SET, ret)
    }

    @Test
    fun testGetSharedState_StateExistsAtVersion() {
        val dataAtV1 = mutableMapOf<String, Any?> ("One" to 1, "Yes" to true)
        val dataAtV4 = mutableMapOf<String, Any?> ("Three" to 3, "No" to false)
        assertEquals(SharedState.Status.SET, extensionContainer.setSharedState(SharedStateType.STANDARD, dataAtV1, 1))
        assertEquals(SharedState.Status.SET, extensionContainer.setSharedState(SharedStateType.STANDARD, dataAtV4, 4))

        val ret = extensionContainer.getSharedState(SharedStateType.STANDARD, 4)
        assertEquals(dataAtV4, ret?.data)
    }

    @Test
    fun testGetSharedState_PendingStateExistsAtVersion() {
        val dataAtV1 = mutableMapOf<String, Any?> ("One" to 1, "Yes" to true)
        val dataAtV4 = null
        assertEquals(SharedState.Status.SET, extensionContainer.setSharedState(SharedStateType.STANDARD, dataAtV1, 1))
        assertEquals(SharedState.Status.PENDING, extensionContainer.setSharedState(SharedStateType.STANDARD, null, 4))

        val ret = extensionContainer.getSharedState(SharedStateType.STANDARD, 4)
        assertEquals(dataAtV4, ret?.data)
        assertEquals(SharedState.Status.PENDING, ret?.status)
    }

    @Test
    fun testGetSharedState_PendingStateExistsAtOlderVersion() {
        // Create shared states at Version 1 and Version 4
        val dataAtV1 = mutableMapOf<String, Any?> ("One" to 1, "Yes" to true)
        val dataAtV4 = null
        assertEquals(SharedState.Status.SET, extensionContainer.setSharedState(SharedStateType.STANDARD, dataAtV1, 1))
        assertEquals(SharedState.Status.PENDING, extensionContainer.setSharedState(SharedStateType.STANDARD, dataAtV4, 4))

        var ret = extensionContainer.getSharedState(SharedStateType.STANDARD, 7)
        assertEquals(dataAtV4, ret?.data)
        assertEquals(SharedState.Status.PENDING, ret?.status)
    }

    @Test
    fun testGetSharedState_StateExistsAtOlderVersion() {
        val dataAtV1 = mutableMapOf<String, Any?> ("One" to 1, "Yes" to true)
        val dataAtV4 = mutableMapOf<String, Any?> ("Three" to 3, "No" to false)
        assertEquals(SharedState.Status.SET, extensionContainer.setSharedState(SharedStateType.STANDARD, dataAtV1, 1))
        assertEquals(SharedState.Status.SET, extensionContainer.setSharedState(SharedStateType.STANDARD, dataAtV4, 4))

        var ret = extensionContainer.getSharedState(SharedStateType.STANDARD, 7)
        assertEquals(dataAtV4, ret?.data)
        assertEquals(SharedState.Status.SET, ret?.status)

        ret = extensionContainer.getSharedState(SharedStateType.STANDARD, 3)
        assertEquals(dataAtV1, ret?.data)
        assertEquals(SharedState.Status.SET, ret?.status)
    }

    @Test
    fun testGetSharedState_StateDoesNotAtVersion() {
        // Create shared states at Version 3 and Version 4
        val dataAtV3 = mutableMapOf<String, Any?> ("One" to 1, "Yes" to true)
        val dataAtV4 = mutableMapOf<String, Any?> ("Three" to 3, "No" to false)
        assertEquals(SharedState.Status.SET, extensionContainer.setSharedState(SharedStateType.STANDARD, dataAtV3, 3))
        assertEquals(SharedState.Status.SET, extensionContainer.setSharedState(SharedStateType.STANDARD, dataAtV4, 4))

        // Fetch state at version 2
        val ret = extensionContainer.getSharedState(SharedStateType.STANDARD, 2)
        assertNull(ret)
    }

    @Test
    fun testGetSharedState_ExecutorShutdown() {
        // Create shared states at Version 3 and Version 4
        val dataAtV3 = mutableMapOf<String, Any?> ("One" to 1, "Yes" to true)
        val dataAtV4 = mutableMapOf<String, Any?> ("Three" to 3, "No" to false)
        assertEquals(SharedState.Status.SET, extensionContainer.setSharedState(SharedStateType.STANDARD, dataAtV3, 3))
        assertEquals(SharedState.Status.SET, extensionContainer.setSharedState(SharedStateType.STANDARD, dataAtV4, 4))

        // Simulate shutdown
        `when`(mockExecutorService.isShutdown).thenReturn(true)

        // Fetch state at version 4
        val ret = extensionContainer.getSharedState(SharedStateType.STANDARD, 4)
        assertNull(ret)
    }

    @Test
    fun testClearSharedState() {
        // Create shared states at Version 3 and Version 4
        val dataAtV3 = mutableMapOf<String, Any?> ("One" to 1, "Yes" to true)
        val dataAtV4 = mutableMapOf<String, Any?> ("Three" to 3, "No" to false)
        assertEquals(SharedState.Status.SET, extensionContainer.setSharedState(SharedStateType.STANDARD, dataAtV3, 3))
        assertEquals(SharedState.Status.SET, extensionContainer.setSharedState(SharedStateType.STANDARD, dataAtV4, 4))

        assertEquals(SharedState.Status.SET, extensionContainer.setSharedState(SharedStateType.XDM, dataAtV3, 3))
        assertEquals(SharedState.Status.SET, extensionContainer.setSharedState(SharedStateType.XDM, dataAtV4, 4))

        // Clear STANDARD shared state
        assertTrue(extensionContainer.clearSharedState(SharedStateType.STANDARD))
        assertNull(extensionContainer.getSharedState(SharedStateType.STANDARD, 3))
        assertNull(extensionContainer.getSharedState(SharedStateType.STANDARD, 4))

        // Verify nothing affects XDM state
        assertEquals(dataAtV3, extensionContainer.getSharedState(SharedStateType.XDM, 3)?.data)
        assertEquals(dataAtV4, extensionContainer.getSharedState(SharedStateType.XDM, 4)?.data)
    }
}
