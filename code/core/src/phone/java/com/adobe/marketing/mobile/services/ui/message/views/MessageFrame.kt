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

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.view.View
import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceConstants
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
    // The current context is the activity that is hosting the message. We can safely cast it to
    // an Activity because this composable is always used within the context of the activity in
    // the current implementation of UIService
    val currentActivity = LocalContext.current.findActivity() ?: run {
        onDisposed()
        Log.debug(ServiceConstants.LOG_TAG, "MessageFrame", "Unable to get the current activity. Dismissing the message.")
        return
    }

    // We want to calculate the height and width of the message based on the host activity's
    // content view. This is because the message is displayed on top of the content view and
    // we want to ensure that the message is displayed relative to the activity's size.
    // This allows the message to be displayed correctly in split screen mode and other
    // multi-window modes.
    val density = LocalDensity.current
    val contentView = currentActivity.findViewById<View>(android.R.id.content)
    val contentHeightDp = with(density) { contentView.height.toDp() }
    val contentWidthDp = with(density) { contentView.width.toDp() }
    val heightDp = remember { mutableStateOf(((contentHeightDp * inAppMessageSettings.height) / 100)) }
    val widthDp = remember { mutableStateOf(((contentWidthDp * inAppMessageSettings.width) / 100)) }

    val horizontalOffset = MessageOffsetMapper.getHorizontalOffset(
        inAppMessageSettings.horizontalAlignment,
        inAppMessageSettings.horizontalInset,
        widthDp.value
    )
    val verticalOffset = MessageOffsetMapper.getVerticalOffset(
        inAppMessageSettings.verticalAlignment,
        inAppMessageSettings.verticalInset,
        heightDp.value
    )

    val allowGestures = remember { inAppMessageSettings.gestureMap.isNotEmpty() }
    val offsetX = remember { mutableStateOf(0f) }
    val offsetY = remember { mutableStateOf(0f) }
    val dragVelocity = remember { mutableStateOf(0f) }

    val adjustAlphaForClipping = remember { Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1 }
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
                .onPlaced {
                    if (!inAppMessageSettings.fitToContent) {
                        heightDp.value =
                            with(density) { ((contentView.height.toDp() * inAppMessageSettings.height) / 100) }
                    }
                    widthDp.value =
                        with(density) { ((contentView.width.toDp() * inAppMessageSettings.width) / 100) }
                }
                .offset(x = horizontalOffset, y = verticalOffset)
                .background(Color.Transparent)
                .testTag(MessageTestTags.MESSAGE_FRAME),
            horizontalArrangement = MessageArrangementMapper.getHorizontalArrangement(
                inAppMessageSettings.horizontalAlignment
            ),
            verticalAlignment = MessageAlignmentMapper.getVerticalAlignment(inAppMessageSettings.verticalAlignment)
        ) {
            // The content of the InAppMessage.
            Card(
                backgroundColor = Color.Transparent,
                elevation = 0.dp, // Ensure that the card does not cast a shadow
                modifier = Modifier
                    .clip(RoundedCornerShape(inAppMessageSettings.cornerRadius.dp))
                    .let {
                        // Needs .99 alpha to ensure that the WebView message is clipped to
                        // the rounded corners for API versions 22 and below.
                        if (adjustAlphaForClipping) it.alpha(0.99f) else it
                    }
                    .draggable(
                        enabled = allowGestures,
                        state = rememberDraggableState { delta ->
                            offsetX.value += delta
                        },
                        orientation = Orientation.Horizontal,
                        onDragStopped = { velocity ->
                            gestureTracker.onDragFinished(
                                offsetX.value,
                                offsetY.value,
                                velocity
                            )
                            dragVelocity.value = 0f
                            offsetY.value = 0f
                            offsetX.value = 0f
                        }
                    )
                    .draggable(
                        enabled = allowGestures,
                        state = rememberDraggableState { delta ->
                            offsetY.value += delta
                        },
                        orientation = Orientation.Vertical,
                        onDragStopped = { velocity ->
                            gestureTracker.onDragFinished(
                                offsetX.value,
                                offsetY.value,
                                velocity
                            )
                            dragVelocity.value = 0f
                            offsetY.value = 0f
                            offsetX.value = 0f
                        }
                    )
            ) {
                MessageContent(
                    Modifier
                        .height(heightDp.value)
                        .width(widthDp.value),
                    inAppMessageSettings,
                    onHeightReceived = { heightFromJs ->
                        if (inAppMessageSettings.fitToContent) {
                            val newHeight = heightFromJs?.toIntOrNull()?.dp
                            heightDp.value = newHeight ?: run {
                                Log.warning(
                                    ServiceConstants.LOG_TAG,
                                    "MessageFrame",
                                    "Invalid height value received: $heightFromJs. Falling back to ${heightDp.value}"
                                )
                                heightDp.value
                            }
                        }
                    },
                    onCreated
                )
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

/**
 * An extension for finding the activity from a context.
 */
private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
