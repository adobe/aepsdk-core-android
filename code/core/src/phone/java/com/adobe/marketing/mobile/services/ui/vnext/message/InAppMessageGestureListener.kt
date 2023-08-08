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

import android.view.GestureDetector
import android.view.MotionEvent
import androidx.compose.animation.ExitTransition
import com.adobe.marketing.mobile.services.ui.vnext.message.mapping.MessageAnimationMapper
import kotlin.math.abs

internal class InAppMessageGestureListener(private val gestureTracker: GestureTracker) :
    GestureDetector.SimpleOnGestureListener() {
    override fun onFling(
        motionEvent1: MotionEvent,
        motionEvent2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        val deltaX: Float = motionEvent2.x - motionEvent1.x
        val deltaY: Float = motionEvent2.y - motionEvent1.y

        var isHorizontalSwipe = false
        var isVerticalSwipe = false

        if (Math.abs(deltaX) > abs(deltaY)) { // detect horizontal swipe
            isHorizontalSwipe = (abs(deltaX) > 300 && abs(velocityX) > 400)
            if (isHorizontalSwipe && deltaX > 0) {
                gestureTracker.onGestureDetected(InAppMessageSettings.MessageGesture.SWIPE_RIGHT)
            } else if (isHorizontalSwipe && deltaX <= 0) {
                gestureTracker.onGestureDetected(InAppMessageSettings.MessageGesture.SWIPE_LEFT)
            }
        } else { // detect vertical swipe
            isVerticalSwipe = (abs(deltaY) > 300 && abs(velocityY) > 400)
            if (isVerticalSwipe && deltaY > 0) {
                gestureTracker.onGestureDetected(InAppMessageSettings.MessageGesture.SWIPE_DOWN)
            } else if (isVerticalSwipe && deltaY <= 0) {
                gestureTracker.onGestureDetected(InAppMessageSettings.MessageGesture.SWIPE_UP)
            }
        }

        return isHorizontalSwipe || isVerticalSwipe
    }
}

internal class GestureTracker(
    defaultExitTransition: ExitTransition,
    private val onGesture: (InAppMessageSettings.MessageGesture) -> Unit
) {

    private var currentExitTransition: ExitTransition = defaultExitTransition
    fun onGestureDetected(gesture: InAppMessageSettings.MessageGesture) {
        currentExitTransition = MessageAnimationMapper.getExitTransitionFor(gesture)
        onGesture(gesture)
    }

    internal fun getExitTransition(): ExitTransition {
        return currentExitTransition
    }
}
