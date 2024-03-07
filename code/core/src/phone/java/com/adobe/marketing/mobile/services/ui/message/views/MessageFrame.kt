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

package com.adobe.marketing.mobile.services.ui.message.views

import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.adobe.marketing.mobile.services.ui.message.GestureTracker
import com.adobe.marketing.mobile.services.ui.message.InAppMessageSettings
import com.adobe.marketing.mobile.services.ui.message.mapping.MessageAlignmentMapper
import com.adobe.marketing.mobile.services.ui.message.mapping.MessageAnimationMapper
import com.adobe.marketing.mobile.services.ui.message.mapping.MessageArrangementMapper
import com.adobe.marketing.mobile.services.ui.message.mapping.MessageOffsetMapper

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
    val horizontalOffset =
        remember {
            MessageOffsetMapper.getHorizontalOffset(
                inAppMessageSettings.horizontalAlignment,
                inAppMessageSettings.horizontalInset,
                currentConfiguration.screenWidthDp.dp
            )
        }
    val verticalOffset =
        remember {
            MessageOffsetMapper.getVerticalOffset(
                inAppMessageSettings.verticalAlignment,
                inAppMessageSettings.verticalInset,
                currentConfiguration.screenHeightDp.dp
            )
        }

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
                .offset(x = horizontalOffset, y = verticalOffset)
                .background(Color.Transparent)
                .testTag(MessageTestTags.MESSAGE_FRAME),
            horizontalArrangement = MessageArrangementMapper.getHorizontalArrangement(
                inAppMessageSettings.horizontalAlignment
            ),
            verticalAlignment = MessageAlignmentMapper.getVerticalAlignment(inAppMessageSettings.verticalAlignment)
        ) {
            // The content of the InAppMessage. This needs to be placed inside a Card with .99 alpha to ensure that
            // the WebView message is clipped to the rounded corners for API versions 22 and below. This does not
            // affect the appearance of the message on API versions 23 and above.
            Card(modifier = Modifier.clip(RoundedCornerShape(inAppMessageSettings.cornerRadius.dp)).alpha(0.99f)) {
                MessageContent(inAppMessageSettings, onCreated, gestureTracker)
            }

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
