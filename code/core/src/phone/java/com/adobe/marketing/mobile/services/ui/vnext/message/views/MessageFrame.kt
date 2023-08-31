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

import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.adobe.marketing.mobile.services.ui.vnext.message.GestureTracker
import com.adobe.marketing.mobile.services.ui.vnext.message.InAppMessageSettings
import com.adobe.marketing.mobile.services.ui.vnext.message.mapping.MessageAlignmentMapper
import com.adobe.marketing.mobile.services.ui.vnext.message.mapping.MessageAnimationMapper
import com.adobe.marketing.mobile.services.ui.vnext.message.mapping.MessageArrangementMapper

/**
 * Represents the frame of the InAppMessage that contains the content of the message.
 * Manages the animations associated with the message, and message arrangement/alignment on the screen.
 * @param visibility the visibility of the message
 * @param inAppMessageSettings the settings of the message
 * @param gestureTracker the gesture tracker of the message
 * @param onCreated the callback to be invoked when the message is created
 */
@Composable
internal fun MessageFrame(
    visibility: MutableTransitionState<Boolean>,
    inAppMessageSettings: InAppMessageSettings,
    gestureTracker: GestureTracker,
    onCreated: (WebView) -> Unit,
    onDisposed: () -> Unit
) {
    val currentConfiguration = LocalConfiguration.current
    val horizontalPadding =
        remember { ((inAppMessageSettings.horizontalInset * currentConfiguration.screenWidthDp) / 100).dp }
    val verticalPadding =
        remember { ((inAppMessageSettings.verticalInset * currentConfiguration.screenHeightDp) / 100).dp }

    AnimatedVisibility(
        visibleState = visibility,
        enter = MessageAnimationMapper.getEnterTransitionFor(inAppMessageSettings.displayAnimation),
        // Use a combination of the exit transition and the most recent gesture to animate the message out of the screen
        // This allows animating out in the direction of the gesture as opposed to the default exit animation always
        exit = gestureTracker.getExitTransition()
    ) {
        Row(

            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding, vertical = verticalPadding)
                .background(Color.Transparent)
                .testTag(MessageTestTags.MESSAGE_FRAME),
            horizontalArrangement = MessageArrangementMapper.getHorizontalArrangement(
                inAppMessageSettings.horizontalAlignment
            ),
            verticalAlignment = MessageAlignmentMapper.getVerticalAlignment(inAppMessageSettings.verticalAlignment)
        ) {
            // The content of the InAppMessage
            MessageContent(inAppMessageSettings, onCreated, gestureTracker)

            // This is a one-time effect that will be called when this composable is completely removed from the composition
            // (after any animations if any). Use this to clean up any resources that were created in onCreated.
            DisposableEffect(Unit) {
                onDispose {
                    onDisposed()
                }
            }
        }
    }
}
