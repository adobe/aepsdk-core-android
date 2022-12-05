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

import com.adobe.marketing.mobile.SharedStateResult
import com.adobe.marketing.mobile.SharedStateStatus
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SharedStateManagerTests {

    private var sharedStateManager: SharedStateManager = SharedStateManager("SampleStateName")

    private val STATE_ZERO = mapOf("ZERO" to 0)
    private val STATE_ONE = mapOf("ONE" to 1)
    private val STATE_TWO = mapOf("TWO" to 2)
    private val STATE_THREE = mapOf("THREE" to 3)
    private val STATE_FOUR = mapOf("FOUR" to 4)
    private val STATE_FIVE = mapOf("FIVE" to 5)

    @Before
    fun setUp() {
        sharedStateManager = SharedStateManager("SampleStateName")
    }

    private fun assertResult(expected: SharedStateResult, actual: SharedStateResult) {
        assertEquals(expected.value, actual.value)
        assertEquals(expected.status, actual.status)
    }

    @Test
    fun testSetState_MultipleVersions() {
        assertTrue { sharedStateManager.setState(0, STATE_ZERO) }
        assertTrue { sharedStateManager.setState(1, STATE_ONE) }
        assertTrue { sharedStateManager.setState(2, STATE_TWO) }
        assertTrue { sharedStateManager.setState(5, STATE_FIVE) }

        assertResult(sharedStateManager.resolve(0), SharedStateResult(SharedStateStatus.SET, STATE_ZERO))
        assertResult(sharedStateManager.resolve(1), SharedStateResult(SharedStateStatus.SET, STATE_ONE))
        assertResult(sharedStateManager.resolve(2), SharedStateResult(SharedStateStatus.SET, STATE_TWO))
        assertResult(sharedStateManager.resolve(5), SharedStateResult(SharedStateStatus.SET, STATE_FIVE))
    }

    @Test
    fun testSetState_FailsOlderVersions() {
        assertTrue { sharedStateManager.setState(5, STATE_FIVE) }
        assertFalse { sharedStateManager.setState(0, STATE_ZERO) }
    }

    @Test
    fun testSetState_FailsSameVersions() {
        assertTrue { sharedStateManager.setState(0, STATE_ZERO) }
        assertFalse { sharedStateManager.setState(0, STATE_FIVE) }

        assertResult(sharedStateManager.resolve(0), SharedStateResult(SharedStateStatus.SET, STATE_ZERO))
    }

    @Test
    fun testSetPendingState() {
        assertTrue { sharedStateManager.setPendingState(0) }

        assertResult(sharedStateManager.resolve(0), SharedStateResult(SharedStateStatus.PENDING, null))
    }

    @Test
    fun testSetPendingState_FailsOlderVersion() {
        assertTrue { sharedStateManager.setState(5, STATE_FIVE) }
        assertFalse { sharedStateManager.setPendingState(0) }
    }

    @Test
    fun testSetPendingState_FailExistingVersion() {
        assertTrue { sharedStateManager.setState(0, STATE_ZERO) }
        assertFalse { sharedStateManager.setPendingState(0) }
    }

    @Test
    fun testSetPendingState_ResolveToOlderState() {
        assertTrue { sharedStateManager.setState(0, STATE_ZERO) }
        assertTrue { sharedStateManager.setPendingState(1) }

        assertResult(sharedStateManager.resolve(1), SharedStateResult(SharedStateStatus.PENDING, STATE_ZERO))
    }

    @Test
    fun testUpdatePendingState_FailsSetState() {
        assertTrue { sharedStateManager.setState(0, STATE_ZERO) }
        assertFalse { sharedStateManager.updatePendingState(0, STATE_ONE) }
    }

    @Test
    fun testUpdatePendingState_FailsNoState() {
        assertFalse { sharedStateManager.updatePendingState(0, STATE_ONE) }
    }

    @Test
    fun testUpdatePendingState() {
        assertTrue { sharedStateManager.setPendingState(1) }
        assertTrue { sharedStateManager.updatePendingState(1, STATE_ONE) }

        assertResult(sharedStateManager.resolve(1), SharedStateResult(SharedStateStatus.SET, STATE_ONE))
    }

    @Test
    fun testUpdateInterleavedPendingState() {
        assertTrue { sharedStateManager.setState(0, STATE_ZERO) }
        assertTrue { sharedStateManager.setPendingState(1) }
        assertTrue { sharedStateManager.setState(2, STATE_TWO) }

        assertResult(sharedStateManager.resolve(0), SharedStateResult(SharedStateStatus.SET, STATE_ZERO))
        assertResult(sharedStateManager.resolve(1), SharedStateResult(SharedStateStatus.PENDING, STATE_ZERO))
        assertResult(sharedStateManager.resolve(2), SharedStateResult(SharedStateStatus.SET, STATE_TWO))

        assertTrue { sharedStateManager.updatePendingState(1, STATE_ONE) }

        assertResult(sharedStateManager.resolve(0), SharedStateResult(SharedStateStatus.SET, STATE_ZERO))
        assertResult(sharedStateManager.resolve(1), SharedStateResult(SharedStateStatus.SET, STATE_ONE))
        assertResult(sharedStateManager.resolve(2), SharedStateResult(SharedStateStatus.SET, STATE_TWO))
    }

    @Test
    fun testResolve_ExactVersion() {
        assertTrue { sharedStateManager.setState(0, STATE_ZERO) }
        assertTrue { sharedStateManager.setState(1, STATE_ONE) }

        assertResult(sharedStateManager.resolve(0), SharedStateResult(SharedStateStatus.SET, STATE_ZERO))
        assertResult(sharedStateManager.resolve(1), SharedStateResult(SharedStateStatus.SET, STATE_ONE))
    }

    @Test
    fun testResolve_GreaterVersion() {
        assertTrue { sharedStateManager.setState(0, STATE_ZERO) }
        assertTrue { sharedStateManager.setState(3, STATE_THREE) }

        assertResult(sharedStateManager.resolve(1), SharedStateResult(SharedStateStatus.SET, STATE_ZERO))
        assertResult(sharedStateManager.resolve(2), SharedStateResult(SharedStateStatus.SET, STATE_ZERO))

        assertResult(sharedStateManager.resolve(4), SharedStateResult(SharedStateStatus.SET, STATE_THREE))
        assertResult(sharedStateManager.resolve(5), SharedStateResult(SharedStateStatus.SET, STATE_THREE))
    }

    @Test
    fun testResolve_NoState() {
        assertResult(sharedStateManager.resolve(1), SharedStateResult(SharedStateStatus.NONE, null))
    }

    @Test
    fun testResolve_PendingState() {
        sharedStateManager.setState(0, STATE_ZERO)
        sharedStateManager.setPendingState(1)
        assertResult(sharedStateManager.resolve(2), SharedStateResult(SharedStateStatus.PENDING, STATE_ZERO))
    }

    @Test
    fun testResolve_LessThanInitialVersion() {
        assertTrue { sharedStateManager.setState(5, STATE_FIVE) }

        assertResult(sharedStateManager.resolve(1), SharedStateResult(SharedStateStatus.SET, STATE_FIVE))
    }

    @Test
    fun testResolveLastSet_ExactVersion() {
        assertTrue { sharedStateManager.setState(0, STATE_ZERO) }
        assertTrue { sharedStateManager.setPendingState(1) }

        assertResult(sharedStateManager.resolveLastSet(0), SharedStateResult(SharedStateStatus.SET, STATE_ZERO))
        assertResult(sharedStateManager.resolveLastSet(1), SharedStateResult(SharedStateStatus.SET, STATE_ZERO))
    }

    @Test
    fun testResolveLastSet_NoSetState() {
        assertTrue { sharedStateManager.setPendingState(1) }

        assertResult(sharedStateManager.resolveLastSet(1), SharedStateResult(SharedStateStatus.NONE, null))
    }

    @Test
    fun testResolveLastSet_GreaterVersion() {
        assertTrue { sharedStateManager.setState(0, STATE_ZERO) }
        assertTrue { sharedStateManager.setPendingState(2) }
        assertTrue { sharedStateManager.setState(4, STATE_FOUR) }

        assertResult(sharedStateManager.resolveLastSet(0), SharedStateResult(SharedStateStatus.SET, STATE_ZERO))
        assertResult(sharedStateManager.resolveLastSet(1), SharedStateResult(SharedStateStatus.SET, STATE_ZERO))
        assertResult(sharedStateManager.resolveLastSet(2), SharedStateResult(SharedStateStatus.SET, STATE_ZERO))
        assertResult(sharedStateManager.resolveLastSet(3), SharedStateResult(SharedStateStatus.SET, STATE_ZERO))
        assertResult(sharedStateManager.resolveLastSet(4), SharedStateResult(SharedStateStatus.SET, STATE_FOUR))
        assertResult(sharedStateManager.resolveLastSet(5), SharedStateResult(SharedStateStatus.SET, STATE_FOUR))
    }

    @Test
    fun testResolveLastSet_NoState() {
        assertResult(sharedStateManager.resolveLastSet(1), SharedStateResult(SharedStateStatus.NONE, null))
    }

    @Test
    fun testResolveLastSet_LessThanInitialVersion() {
        assertTrue { sharedStateManager.setState(5, STATE_FIVE) }

        assertResult(sharedStateManager.resolveLastSet(1), SharedStateResult(SharedStateStatus.SET, STATE_FIVE))
    }

    @Test
    fun testResolveLastSet_LessThanPendingInitialVersion() {
        assertTrue { sharedStateManager.setPendingState(5) }

        assertResult(sharedStateManager.resolveLastSet(1), SharedStateResult(SharedStateStatus.NONE, null))
    }

    @Test
    fun testClear() {
        sharedStateManager.setState(1, STATE_ONE)
        assertResult(sharedStateManager.resolve(1), SharedStateResult(SharedStateStatus.SET, STATE_ONE))

        sharedStateManager.clear()
        assertResult(sharedStateManager.resolve(1), SharedStateResult(SharedStateStatus.NONE, null))

        sharedStateManager.setState(2, STATE_TWO)
        assertResult(sharedStateManager.resolve(1), SharedStateResult(SharedStateStatus.SET, STATE_TWO))

        sharedStateManager.clear()
        assertResult(sharedStateManager.resolve(1), SharedStateResult(SharedStateStatus.NONE, null))
    }
}
