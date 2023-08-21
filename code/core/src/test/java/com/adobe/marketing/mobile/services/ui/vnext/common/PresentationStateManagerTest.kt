/*
  Copyright 2023 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.services.ui.vnext.common

import com.adobe.marketing.mobile.services.ui.vnext.Presentable
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PresentationStateManagerTest {

    @Test
    fun `Test initial state is detached with visibility as false`() {
        // setup
        val presentationStateManager = PresentationStateManager()

        // verify
        assert(presentationStateManager.presentableState.value == Presentable.State.DETACHED)
        assertFalse(presentationStateManager.visibilityState.currentState)
        assertFalse(presentationStateManager.visibilityState.targetState)
    }

    @Test
    fun `Test #onShown sets the presentable state to visible and the visibility state to true`() {
        // setup
        val presentationStateManager = PresentationStateManager()

        // test
        presentationStateManager.onShown()

        // verify
        assert(presentationStateManager.presentableState.value == Presentable.State.VISIBLE)
        assertTrue(presentationStateManager.visibilityState.targetState)
    }

    @Test
    fun `Test #onHidden sets the presentable state to hidden and the visibility state to false`() {
        // setup
        val presentationStateManager = PresentationStateManager()

        // test
        presentationStateManager.onHidden()

        // verify
        assert(presentationStateManager.presentableState.value == Presentable.State.HIDDEN)
        assertFalse(presentationStateManager.visibilityState.targetState)
    }

    @Test
    fun `Test #onDetached sets the presentable state to detached and the visibility state to false`() {
        // setup
        val presentationStateManager = PresentationStateManager()

        // test
        presentationStateManager.onDetached()

        // verify
        assert(presentationStateManager.presentableState.value == Presentable.State.DETACHED)
        assertFalse(presentationStateManager.visibilityState.targetState)
    }
}
