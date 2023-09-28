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
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceConstants
import com.adobe.marketing.mobile.services.ui.message.GestureTracker
import com.adobe.marketing.mobile.services.ui.message.InAppMessagePresentable
import com.adobe.marketing.mobile.services.ui.message.InAppMessageSettings
import java.nio.charset.StandardCharsets

/**
 * Represents the content of the InAppMessage. Holds a WebView that loads the HTML content of the message
 * @param inAppMessageSettings [InAppMessageSettings] settings for the InAppMessage
 * @param onCreated callback to be invoked when the WenView that this composable holds is created
 * @param gestureTracker [GestureTracker] to track the swipe/drag gestures on the webview
 */
@Composable
internal fun MessageContent(
    inAppMessageSettings: InAppMessageSettings,
    onCreated: (WebView) -> Unit,
    gestureTracker: GestureTracker
) {
    // Size variables
    val currentConfiguration = LocalConfiguration.current
    val heightDp: Dp =
        remember { ((currentConfiguration.screenHeightDp * inAppMessageSettings.height) / 100).dp }
    val widthDp: Dp =
        remember { ((currentConfiguration.screenWidthDp * inAppMessageSettings.width) / 100).dp }

    // Swipe/Drag variables
    val offsetX = remember { mutableStateOf(0f) }
    val offsetY = remember { mutableStateOf(0f) }
    val dragVelocity = remember { mutableStateOf(0f) }

    AndroidView(
        factory = {
            WebView(it).apply {
                Log.debug(
                    ServiceConstants.LOG_TAG,
                    "MessageContent",
                    "Creating MessageContent"
                )

                // call on created before loading the content. This is to ensure that the webview script
                // handlers and interfaces are ready to be used
                onCreated(this)

                loadDataWithBaseURL(
                    InAppMessagePresentable.BASE_URL,
                    inAppMessageSettings.content,
                    InAppMessagePresentable.TEXT_HTML_MIME_TYPE,
                    StandardCharsets.UTF_8.name(),
                    null
                )
            }
        },
        modifier = Modifier
            .height(heightDp)
            .width(widthDp)
            .clip(RoundedCornerShape(inAppMessageSettings.cornerRadius.dp))
            .draggable(
                state = rememberDraggableState { delta ->
                    offsetX.value += delta
                },
                orientation = Orientation.Horizontal,
                onDragStopped = { velocity ->
                    gestureTracker.onDragFinished(offsetX.value, offsetY.value, velocity)
                    dragVelocity.value = 0f
                    offsetY.value = 0f
                    offsetX.value = 0f
                }
            )
            .draggable(
                state = rememberDraggableState { delta ->
                    offsetY.value += delta
                },
                orientation = Orientation.Vertical,
                onDragStopped = { velocity ->
                    gestureTracker.onDragFinished(offsetX.value, offsetY.value, velocity)
                    dragVelocity.value = 0f
                    offsetY.value = 0f
                    offsetX.value = 0f
                }
            ).testTag(MessageTestTags.MESSAGE_CONTENT)

    )
}
