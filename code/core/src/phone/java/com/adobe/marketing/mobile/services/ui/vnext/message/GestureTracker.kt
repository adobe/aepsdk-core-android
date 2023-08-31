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

package com.adobe.marketing.mobile.services.ui.vnext.message

import androidx.compose.animation.ExitTransition
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceConstants
import com.adobe.marketing.mobile.services.ui.vnext.message.mapping.MessageAnimationMapper
import kotlin.math.abs

/**
 * Keeps track of the gestures on the InAppMessage and translates them to [ExitTransition] to be used
 * for the exit animation.
 */
internal class GestureTracker(
    defaultExitTransition: ExitTransition = ExitTransition.None,
    private val acceptedGestures: Set<InAppMessageSettings.MessageGesture> = setOf(),
    private val onGestureDetected: (InAppMessageSettings.MessageGesture) -> Unit
) {

    companion object {
        private const val LOG_SOURCE = "GestureTracker"
        private const val DRAG_THRESHOLD_OFFSET = 400
        private const val DRAG_THRESHOLD_VELOCITY = 300
    }
    private var currentExitTransition: ExitTransition = defaultExitTransition

    /**
     * Called when the drag is finished.
     * @param x the x coordinate of the drag (positive is right, negative is left)
     * @param y the y coordinate of the drag (positive is down, negative is up)
     * @param velocity the velocity of the drag
     */
    internal fun onDragFinished(x: Float, y: Float, velocity: Float) {
        val gesture: InAppMessageSettings.MessageGesture? = if (abs(x) > abs(y)) {
            if (x > 0 && abs(velocity) > DRAG_THRESHOLD_VELOCITY && abs(x) > DRAG_THRESHOLD_OFFSET) {
                InAppMessageSettings.MessageGesture.SWIPE_RIGHT
            } else if (x < 0 && abs(velocity) > DRAG_THRESHOLD_VELOCITY && abs(x) > DRAG_THRESHOLD_OFFSET) {
                InAppMessageSettings.MessageGesture.SWIPE_LEFT
            } else {
                null
            }
        } else {
            if (y > 0 && abs(velocity) > DRAG_THRESHOLD_VELOCITY && abs(y) > DRAG_THRESHOLD_OFFSET) {
                InAppMessageSettings.MessageGesture.SWIPE_DOWN
            } else if (y < 0 && abs(velocity) > DRAG_THRESHOLD_VELOCITY && abs(y) > DRAG_THRESHOLD_OFFSET) {
                InAppMessageSettings.MessageGesture.SWIPE_UP
            } else {
                null
            }
        }

        gesture?.let {
            Log.trace(ServiceConstants.LOG_TAG, LOG_SOURCE, "Gesture detected: $gesture with $x, $y, $velocity")
            onGesture(it)
        }
    }

    /**
     * To be invoked when a gesture is detected. Responsible for changing the exit transition.
     */
    internal fun onGesture(gesture: InAppMessageSettings.MessageGesture) {
        Log.trace(ServiceConstants.LOG_TAG, LOG_SOURCE, "Gesture detected: $gesture")

        // Change the exit transition only if the gesture is supported
        if (gesture !in acceptedGestures) return

        currentExitTransition = MessageAnimationMapper.getExitTransitionFor(gesture)
        onGestureDetected(gesture)
    }

    /**
     * Returns the most recent exit transition to be used for the exit animation, based on
     * the gestures detected.
     */
    internal fun getExitTransition(): ExitTransition {
        return currentExitTransition
    }
}
