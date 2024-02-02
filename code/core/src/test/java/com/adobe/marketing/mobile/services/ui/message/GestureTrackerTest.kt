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

package com.adobe.marketing.mobile.services.ui.message

import androidx.compose.animation.ExitTransition
import com.adobe.marketing.mobile.services.ui.message.mapping.MessageAnimationMapper
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class GestureTrackerTest {

    private val allGestures = setOf(
        InAppMessageSettings.MessageGesture.SWIPE_LEFT,
        InAppMessageSettings.MessageGesture.SWIPE_RIGHT,
        InAppMessageSettings.MessageGesture.SWIPE_UP,
        InAppMessageSettings.MessageGesture.SWIPE_DOWN,
        InAppMessageSettings.MessageGesture.TAP_BACKGROUND
    )

    @Before
    fun setUp() {
    }

    @Test
    fun `Test that #onDragFinished does not register gesture when horizontal drag is insufficient`() {
        val detectedGestures = mutableListOf<InAppMessageSettings.MessageGesture>()
        val gestureTracker = GestureTracker(
            defaultExitTransition = ExitTransition.None,
            acceptedGestures = allGestures,
            onGestureDetected = {
                detectedGestures.add(it)
            }
        )

        gestureTracker.onDragFinished(-100f, 20f, 450f)
        gestureTracker.onDragFinished(100f, 20f, 450f)

        assert(detectedGestures.isEmpty())
    }

    @Test
    fun `Test that #onDragFinished does not register gesture when horizontal velocity is insufficient`() {
        val detectedGestures = mutableListOf<InAppMessageSettings.MessageGesture>()
        val gestureTracker = GestureTracker(
            defaultExitTransition = ExitTransition.None,
            acceptedGestures = allGestures,
            onGestureDetected = {
                detectedGestures.add(it)
            }
        )

        gestureTracker.onDragFinished(-450f, 20f, 250f)
        gestureTracker.onDragFinished(450f, 20f, -250f)

        assert(detectedGestures.isEmpty())
    }

    @Test
    fun `test that #onDragFinished registers swipe left gesture when horizontal drag and velocity is sufficient`() {
        val detectedGestures = mutableListOf<InAppMessageSettings.MessageGesture>()
        val gestureTracker = GestureTracker(
            defaultExitTransition = ExitTransition.None,
            acceptedGestures = allGestures,
            onGestureDetected = {
                detectedGestures.add(it)
            }
        )

        gestureTracker.onDragFinished(-450f, 20f, 450f)

        assert(detectedGestures.size == 1)
        assert(detectedGestures[0] == InAppMessageSettings.MessageGesture.SWIPE_LEFT)
    }

    @Test
    fun `Test that #onDragFinished registers swipe right gesture when horizontal drag and velocity is sufficient`() {
        val detectedGestures = mutableListOf<InAppMessageSettings.MessageGesture>()
        val gestureTracker = GestureTracker(
            defaultExitTransition = ExitTransition.None,
            acceptedGestures = allGestures,
            onGestureDetected = {
                detectedGestures.add(it)
            }
        )

        gestureTracker.onDragFinished(450f, 20f, 450f)

        assert(detectedGestures.size == 1)
        assert(detectedGestures[0] == InAppMessageSettings.MessageGesture.SWIPE_RIGHT)
    }

    @Test
    fun `Test that #onDragFinished does not register gesture when vertical drag is insufficient`() {
        val detectedGestures = mutableListOf<InAppMessageSettings.MessageGesture>()
        val gestureTracker = GestureTracker(
            defaultExitTransition = ExitTransition.None,
            acceptedGestures = allGestures,
            onGestureDetected = {
                detectedGestures.add(it)
            }
        )

        gestureTracker.onDragFinished(20f, -100f, 450f)
        gestureTracker.onDragFinished(20f, 100f, 450f)

        assert(detectedGestures.isEmpty())
    }

    @Test
    fun `Test that #onDragFinished does not register gesture when vertical velocity is insufficient`() {
        val detectedGestures = mutableListOf<InAppMessageSettings.MessageGesture>()
        val gestureTracker = GestureTracker(
            defaultExitTransition = ExitTransition.None,
            acceptedGestures = allGestures,
            onGestureDetected = {
                detectedGestures.add(it)
            }
        )

        gestureTracker.onDragFinished(20f, -450f, 250f)
        gestureTracker.onDragFinished(20f, 450f, -250f)

        assert(detectedGestures.isEmpty())
    }

    @Test
    fun `Test that #onDragFinished registers swipe up gesture when vertical drag and velocity is sufficient`() {
        val detectedGestures = mutableListOf<InAppMessageSettings.MessageGesture>()
        val gestureTracker = GestureTracker(
            defaultExitTransition = ExitTransition.None,
            acceptedGestures = allGestures,
            onGestureDetected = {
                detectedGestures.add(it)
            }
        )

        gestureTracker.onDragFinished(20f, -450f, 450f)

        assert(detectedGestures.size == 1)
        assert(detectedGestures[0] == InAppMessageSettings.MessageGesture.SWIPE_UP)
    }

    @Test
    fun `Test that #onDragFinished registers swipe down gesture when vertical drag and velocity is sufficient`() {
        val detectedGestures = mutableListOf<InAppMessageSettings.MessageGesture>()
        val gestureTracker = GestureTracker(
            defaultExitTransition = ExitTransition.None,
            acceptedGestures = allGestures,
            onGestureDetected = {
                detectedGestures.add(it)
            }
        )

        gestureTracker.onDragFinished(20f, 450f, 450f)

        assert(detectedGestures.size == 1)
        assert(detectedGestures[0] == InAppMessageSettings.MessageGesture.SWIPE_DOWN)
    }

    @Test
    fun `Test that #onGesture does not register unaccepted gestures`() {
        val detectedGestures = mutableListOf<InAppMessageSettings.MessageGesture>()
        val gestureTracker = GestureTracker(
            defaultExitTransition = ExitTransition.None,
            acceptedGestures = setOf(
                InAppMessageSettings.MessageGesture.SWIPE_LEFT,
                InAppMessageSettings.MessageGesture.SWIPE_RIGHT
            ),
            onGestureDetected = {
                detectedGestures.add(it)
            }
        )

        gestureTracker.onDragFinished(20f, 450f, 450f)
        gestureTracker.onDragFinished(20f, -450f, 450f)

        assert(detectedGestures.isEmpty())

        gestureTracker.onDragFinished(-450f, 20f, 450f)
        gestureTracker.onDragFinished(450f, 20f, 450f)
        assert(detectedGestures.size == 2)
    }

    @Test
    fun `Test that onGesture updates currentExitTransition`() {
        val gestureTracker = GestureTracker(
            defaultExitTransition = ExitTransition.None,
            acceptedGestures = setOf(
                InAppMessageSettings.MessageGesture.SWIPE_LEFT,
                InAppMessageSettings.MessageGesture.SWIPE_UP
            ),
            onGestureDetected = {}
        )

        gestureTracker.onDragFinished(-450f, 20f, 450f)
        assertEquals(
            MessageAnimationMapper.getExitTransitionFor(InAppMessageSettings.MessageGesture.SWIPE_LEFT),
            gestureTracker.getExitTransition()
        )

        gestureTracker.onDragFinished(20f, -450f, 450f)
        assertEquals(
            MessageAnimationMapper.getExitTransitionFor(InAppMessageSettings.MessageGesture.SWIPE_UP),
            gestureTracker.getExitTransition()
        )
    }
}
