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

package com.adobe.marketing.mobile.services.ui.vnext.message.mapping

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import com.adobe.marketing.mobile.services.ui.vnext.message.InAppMessageSettings

/**
 * Mapper class to map [InAppMessageSettings.MessageAnimation] to a Compose [EnterTransition] and [ExitTransition]
 */
internal object MessageAnimationMapper {

    private const val DEFAULT_ANIMATION_DURATION_MS = 300

    /**
     * Map of [InAppMessageSettings.MessageAnimation] to [EnterTransition]`
     */
    private val enterAnimationMap: Map<InAppMessageSettings.MessageAnimation, EnterTransition> =
        mapOf(
            InAppMessageSettings.MessageAnimation.LEFT to slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(DEFAULT_ANIMATION_DURATION_MS)
            ),

            InAppMessageSettings.MessageAnimation.RIGHT to slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(DEFAULT_ANIMATION_DURATION_MS)
            ),

            InAppMessageSettings.MessageAnimation.TOP to slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(DEFAULT_ANIMATION_DURATION_MS)
            ),

            InAppMessageSettings.MessageAnimation.BOTTOM to slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(DEFAULT_ANIMATION_DURATION_MS)
            ),
            InAppMessageSettings.MessageAnimation.FADE to fadeIn(
                animationSpec = tween(DEFAULT_ANIMATION_DURATION_MS)
            )
        )

    /**
     * Map of [InAppMessageSettings.MessageAnimation] to [ExitTransition]
     */
    private val exitAnimationMap: Map<InAppMessageSettings.MessageAnimation, ExitTransition> =
        mapOf(
            InAppMessageSettings.MessageAnimation.LEFT to slideOutHorizontally(
                animationSpec = tween(DEFAULT_ANIMATION_DURATION_MS),
                targetOffsetX = { -it }
            ),
            InAppMessageSettings.MessageAnimation.RIGHT to slideOutHorizontally(
                animationSpec = tween(DEFAULT_ANIMATION_DURATION_MS),
                targetOffsetX = { it }
            ),
            InAppMessageSettings.MessageAnimation.TOP to slideOutVertically(
                animationSpec = tween(DEFAULT_ANIMATION_DURATION_MS),
                targetOffsetY = { -it }
            ),

            InAppMessageSettings.MessageAnimation.BOTTOM to slideOutVertically(
                animationSpec = tween(DEFAULT_ANIMATION_DURATION_MS),
                targetOffsetY = { it }
            ),
            InAppMessageSettings.MessageAnimation.FADE to fadeOut(
                animationSpec = tween(DEFAULT_ANIMATION_DURATION_MS)
            )
        )

    private val gestureAnimationMap: Map<InAppMessageSettings.MessageGesture, ExitTransition> = mapOf(
        InAppMessageSettings.MessageGesture.SWIPE_UP to slideOutVertically(
            animationSpec = tween(DEFAULT_ANIMATION_DURATION_MS),
            targetOffsetY = { -it }
        ),

        InAppMessageSettings.MessageGesture.SWIPE_DOWN to slideOutVertically(
            animationSpec = tween(DEFAULT_ANIMATION_DURATION_MS),
            targetOffsetY = { it }
        ),
        InAppMessageSettings.MessageGesture.SWIPE_LEFT to slideOutHorizontally(
            animationSpec = tween(DEFAULT_ANIMATION_DURATION_MS),
            targetOffsetX = { -it }
        ),
        InAppMessageSettings.MessageGesture.SWIPE_RIGHT to slideOutHorizontally(
            animationSpec = tween(DEFAULT_ANIMATION_DURATION_MS),
            targetOffsetX = { it }
        ),
        InAppMessageSettings.MessageGesture.BACKGROUND_TAP to fadeOut(
            animationSpec = tween(DEFAULT_ANIMATION_DURATION_MS)
        )
    )

    /**
     * Get the [EnterTransition] for the given [InAppMessageSettings.MessageAnimation]
     */
    fun getEnterTransitionFor(animation: InAppMessageSettings.MessageAnimation): EnterTransition = enterAnimationMap[animation] ?: EnterTransition.None

    /**
     * Get the [ExitTransition] for the given [InAppMessageSettings.MessageAnimation]
     */
    fun getExitTransitionFor(animation: InAppMessageSettings.MessageAnimation): ExitTransition = exitAnimationMap[animation] ?: ExitTransition.None

    fun getExitTransitionFor(gesture: InAppMessageSettings.MessageGesture): ExitTransition = gestureAnimationMap[gesture] ?: ExitTransition.None
}
