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

import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class SharedStateManagerTest {

    private val sharedStateManager: SharedStateManager = SharedStateManager("SampleStateName")

    @Before
    fun setUp() {
        sharedStateManager.clearSharedState()
    }

    @Test
    fun testCreateSharedState_NonNullData() {
        val data = mapOf<String, Any?> ("One" to 1, "Yes" to true)
        assertEquals(SharedState.Status.SET, sharedStateManager.createSharedState(data, 1, false))
    }

    @Test
    fun testCreateSharedState_NullData() {
        assertEquals(SharedState.Status.SET, sharedStateManager.createSharedState(null, 1, false))
    }

    @Test
    fun testCreateSharedState_PendingDataAssumptions() {
        val data = mapOf<String, Any?> ("One" to 1, "Yes" to true)

        // Verify that SharedStateManager does not make assumptions of pending based on data
        assertEquals(SharedState.Status.PENDING, sharedStateManager.createSharedState(data, 1, true))
        assertEquals(SharedState.Status.PENDING, sharedStateManager.createSharedState(null, 3, true))
    }

    @Test
    fun testCreateSharedState_PendingDataOverwrite_WithPending() {
        // Create pending state at version 1
        sharedStateManager.createSharedState(null, 1, true)

        // Overwrite with another pending data
        assertEquals(SharedState.Status.NOT_SET, sharedStateManager.createSharedState(mapOf(), 1, true))
    }

    @Test
    fun testCreateSharedState_PendingDataOverwrite_WithNonPending() {
        // Create pending state at version 1
        sharedStateManager.createSharedState(null, 1, true)

        // Overwrite with non pending data
        assertEquals(SharedState.Status.NOT_SET, sharedStateManager.createSharedState(mapOf(), 1, false))
    }

    @Test
    fun testCreateSharedState_OlderStateDoesNotExist() {
        // Create state at version 3
        sharedStateManager.createSharedState(mapOf(), 3, false)
        // Create state at version 5
        sharedStateManager.createSharedState(mapOf(), 5, false)

        // Verify that state greater than 5 can be created irrespective of pending status
        assertEquals(SharedState.Status.SET, sharedStateManager.createSharedState(mapOf(), 10, false))
        assertEquals(SharedState.Status.PENDING, sharedStateManager.createSharedState(mapOf(), 11, true))
    }

    @Test
    fun testCreateSharedState_OlderStateExists() {
        // Create state at version 5
        sharedStateManager.createSharedState(mapOf(), 5, false)

        // Verify that no state less than or equal to 5 can be created irrespective of pending status
        assertEquals(SharedState.Status.NOT_SET, sharedStateManager.createSharedState(mapOf(), 3, false))
        assertEquals(SharedState.Status.NOT_SET, sharedStateManager.createSharedState(mapOf(), 5, true))
    }

    @Test
    fun testUpdateSharedState_WithPendingState() {
        // Create a pending state at version 1
        sharedStateManager.updateSharedState(null, 1, true)

        // Verify that pending state cannot be updated with another pending state
        assertEquals(SharedState.Status.NOT_SET, sharedStateManager.updateSharedState(null, 1, true))
        assertEquals(SharedState.Status.NOT_SET, sharedStateManager.updateSharedState(mapOf(), 1, true))
    }

    @Test
    fun testUpdateSharedState_NoStateAtVersion() {
        // Verify that a state cannot be updated when no state exists at the version
        assertEquals(SharedState.Status.NOT_SET, sharedStateManager.updateSharedState(mapOf(), 7, false))
    }

    @Test
    fun testUpdateSharedState_NoPendingStateAtVersion() {
        // Create a non pending state at version 7
        sharedStateManager.createSharedState(mapOf(), 7, false)

        // Verify that pending state cannot be updated when no pending state exists at the version
        assertEquals(
            SharedState.Status.NOT_SET,
            sharedStateManager.updateSharedState(mapOf("One" to 1, "Yes" to true), 7, false)
        )
    }

    @Test
    fun testUpdateSharedState_ValidPendingStateAtVersion() {
        // Create a pending state at version 7
        sharedStateManager.createSharedState(null, 7, true)

        // Verify that pending state can be updated when pending state exists at the version
        assertEquals(
            SharedState.Status.SET,
            sharedStateManager.updateSharedState(mapOf("One" to 1, "Yes" to true), 7, false)
        )
    }

    @Test
    fun testGetSharedState_NoStatesYet() {
        assertNull(sharedStateManager.getSharedState(0))
    }

    @Test
    fun testGetSharedState_StateExistsAtQueriedVersion() {
        val data = mapOf("One" to 1, "Yes" to true)
        sharedStateManager.createSharedState(mapOf("One" to 1, "Yes" to true), 3, false)

        val actualState: SharedState? = sharedStateManager.getSharedState(3)
        assertNotNull(actualState)
        assertEquals(data, actualState.data)
        assertEquals(SharedState.Status.SET, actualState.status)
    }

    @Test
    fun testGetSharedState_PendingStateExistsAtQueriedVersion() {
        sharedStateManager.createSharedState(null, 3, true)

        val actualState: SharedState? = sharedStateManager.getSharedState(3)
        assertNotNull(actualState)
        assertNull(actualState.data)
        assertEquals(SharedState.Status.PENDING, actualState.status)
    }

    @Test
    fun testGetSharedState_StateExistsAtOlderVersion() {
        // Create shared states at Version 3 and Version 4
        val dataAtV3 = mapOf("One" to 1, "Yes" to true)
        val dataAtV4 = mapOf("Two" to 2, "No" to false)
        sharedStateManager.createSharedState(dataAtV3, 3, false)
        sharedStateManager.createSharedState(dataAtV4, 4, false)

        // Fetch state at version 8
        val actualState: SharedState? = sharedStateManager.getSharedState(8)
        assertNotNull(actualState)
        assertEquals(dataAtV4, actualState.data)
        assertEquals(SharedState.Status.SET, actualState.status)
    }

    @Test
    fun testGetSharedState_PendingStateExistsAtOlderVersion() {
        // Create shared states at Version 3 and Version 4
        val dataAtV3 = mapOf("One" to 1, "Yes" to true)
        val dataAtV4 = mapOf("Two" to 2, "No" to false)
        sharedStateManager.createSharedState(dataAtV3, 3, false)
        sharedStateManager.createSharedState(dataAtV4, 4, true)

        val actualState: SharedState? = sharedStateManager.getSharedState(8)
        assertNotNull(actualState)
        assertEquals(dataAtV4, actualState.data)
        assertEquals(SharedState.Status.PENDING, actualState.status)
    }

    @Test
    fun testClearSharedState() {
        // Create shared states at Version 3 and Version 4
        val dataAtV3 = mapOf("One" to 1, "Yes" to true)
        val dataAtV4 = mapOf("Two" to 2, "No" to false)
        sharedStateManager.createSharedState(dataAtV3, 3, false)
        sharedStateManager.createSharedState(dataAtV4, 4, false)
        assertNotNull(sharedStateManager.getSharedState(4))

        sharedStateManager.clearSharedState()

        // Verify that previously set states are removed
        assertNull(sharedStateManager.getSharedState(4))
        assertNull(sharedStateManager.getSharedState(3))
    }
}
