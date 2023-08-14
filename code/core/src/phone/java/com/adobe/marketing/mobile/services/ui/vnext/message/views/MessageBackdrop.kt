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

package com.adobe.marketing.mobile.services.ui.vnext.message.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import com.adobe.marketing.mobile.services.ui.vnext.message.GestureTracker
import com.adobe.marketing.mobile.services.ui.vnext.message.InAppMessageSettings

/**
 * A composable representing the backdrop for the InAppMessage.
 * @param visibility the visibility state of the backdrop
 * @param inAppMessageSettings the settings for the InAppMessage
 */
@Composable
internal fun MessageBackdrop(
    visibility: MutableTransitionState<Boolean>,
    inAppMessageSettings: InAppMessageSettings,
    gestureTracker: GestureTracker
) {
    val backdropColor = remember { Color(inAppMessageSettings.backdropColor.toColorInt()) }

    // Note that the background enter and exit animations are simple fades. Primarily used to ease in and out the backdrop
    // separate from the message itself.  This allows smoother visuals and reduces flicker in the animation.
    AnimatedVisibility(visibleState = visibility, enter = fadeIn(), exit = fadeOut()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backdropColor.copy(alpha = inAppMessageSettings.backdropOpacity))
                .clickable(
                    enabled = true,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {
                        gestureTracker.onGesture(InAppMessageSettings.MessageGesture.BACKGROUND_TAP)
                    }
                )
        )
    }
}
