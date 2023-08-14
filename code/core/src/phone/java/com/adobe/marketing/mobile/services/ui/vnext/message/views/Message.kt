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
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.adobe.marketing.mobile.services.ui.vnext.Presentable
import com.adobe.marketing.mobile.services.ui.vnext.common.PresentationStateManager
import com.adobe.marketing.mobile.services.ui.vnext.message.GestureTracker
import com.adobe.marketing.mobile.services.ui.vnext.message.InAppMessageSettings
import com.adobe.marketing.mobile.services.ui.vnext.message.mapping.MessageAnimationMapper

/**
 * Represents the InAppMessage screen. Composes a Message and a BackHandler. This component is primarily responsible
 * for hosting the overall InAppMessage and provides notifications about the creation, disposal, handling
 * the back buttons and gestures.
 * @param presentationStateManager [PresentationStateManager] to manage the presentation of the InAppMessage
 * @param inAppMessageSettings [InAppMessageSettings] for the InAppMessage
 * @param onCreated callback invoked when the [WebView] that holds the message content is created
 * @param onDisposed callback invoked when the composable that holds message content is disposed. This signifies the completion
 *                   exit animations
 * @param onBackPressed callback invoked when the back button is pressed while the message is visible
 * @param onGestureDetected callback invoked when a gesture is detected on the message
 */
@Composable
internal fun MessageScreen(
    presentationStateManager: PresentationStateManager,
    inAppMessageSettings: InAppMessageSettings,
    onCreated: (WebView) -> Unit,
    onDisposed: () -> Unit,
    onBackPressed: () -> Unit,
    onGestureDetected: (InAppMessageSettings.MessageGesture) -> Unit
) {
    // A gesture tracker to be injected into all subcomponents for identifying and
    // propagating gestures
    val gestureTracker: GestureTracker =
        remember {
            GestureTracker(
                defaultExitTransition = MessageAnimationMapper.getExitTransitionFor(
                    inAppMessageSettings.dismissAnimation
                ),
                acceptedGestures = inAppMessageSettings.gestureMap.keys
            ) {
                onGestureDetected(it)
            }
        }

    // BackHandler conditionally handles the back press event based on the visibility of the message
    BackHandler(enabled = presentationStateManager.presentableState.value == Presentable.State.VISIBLE) {
        onBackPressed()
    }

    Message(
        isVisible = presentationStateManager.visibilityState,
        inAppMessageSettings = inAppMessageSettings,
        gestureTracker = gestureTracker,
        onCreated = { onCreated(it) },
        onDisposed = { onDisposed() }
    )
}

/**
 * Represents an InAppMessage view. Composes an optional MessageBackdrop and MessageFrame.
 * @param isVisible transition state of the visibility of the InAppMessage
 * @param inAppMessageSettings [InAppMessageSettings] for the InAppMessage
 * @param gestureTracker
 * @param onCreated callback invoked when the [WebView] that holds the message content is created
 * @param onDisposed callback invoked when the composable that holds message content is disposed
 */
@Composable
internal fun Message(
    isVisible: MutableTransitionState<Boolean>,
    inAppMessageSettings: InAppMessageSettings,
    gestureTracker: GestureTracker,
    onCreated: (WebView) -> Unit,
    onDisposed: () -> Unit
) {
    // Backdrop for the InAppMessage only takes into effect if the InAppMessage is taking over the UI
    if (inAppMessageSettings.shouldTakeOverUi) {
        MessageBackdrop(
            visibility = isVisible,
            inAppMessageSettings = inAppMessageSettings,
            gestureTracker = gestureTracker
        )
    }

    // Frame that holds the InAppMessage
    MessageFrame(
        visibility = isVisible,
        inAppMessageSettings = inAppMessageSettings,
        gestureTracker = gestureTracker,
        onCreated = onCreated,
        onDisposed = onDisposed
    )
}
