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

import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.adobe.marketing.mobile.services.ui.Presentable
import com.adobe.marketing.mobile.services.ui.common.PresentationStateManager
import com.adobe.marketing.mobile.services.ui.message.GestureTracker
import com.adobe.marketing.mobile.services.ui.message.InAppMessageSettings
import com.adobe.marketing.mobile.services.ui.message.mapping.MessageAnimationMapper

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
        onDisposed = { onDisposed() },
        onBackPressed = onBackPressed
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
    onDisposed: () -> Unit,
    onBackPressed: () -> Unit
) {
    if (inAppMessageSettings.shouldTakeOverUi) {
        /* Dialog is used to take over the UI when the InAppMessage is set to take over the UI.
         This is necessary to ensure that the InAppMessage is displayed on top of the UI.
         Which will ensure that ScreenReader can read the content of the InAppMessage only and not the underlying UI.
         */

        val context = LocalContext.current
        val targetSdkVersion = remember {
            try {
                context.packageManager.getPackageInfo(context.packageName, 0).applicationInfo?.targetSdkVersion
            } catch (e: NameNotFoundException) {
                null
            }
        }

        Dialog(
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            ),
            onDismissRequest = {
                onBackPressed()
            }
        ) {
            val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
            dialogWindow?.let {
                // Remove the default dim and animations for the dialog window
                // Customer can set their own dim and animations if needed and those will be honoured in MessageBackdrop inside Message
                it.setDimAmount(0f)
                it.setWindowAnimations(-1)

                // These settings are used to ensure that the dialog displays correctly on Compose <= v 1.8
                // when edge-to-edge is enforced on apps targeting Android 35 and above.

                // This setting makes sure that the system bars have transparent backgrounds
                // If edge-to-edge is enabled, the system bars will not have any background color
                // If not, it will use the default or theme provided system bar colors
                it.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

                it.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                // For apps targeting Android 35 and above and running on device with Android 35 or above,
                // the system will automatically enable edge-to-edge display for the app.
                // This ensures the dialog window does not have any additional insets and is also displayed edge-to-edge.
                if (Build.VERSION.SDK_INT >= 35 && targetSdkVersion != null && targetSdkVersion >= 35) {
                    it.attributes.fitInsetsTypes = 0
                    it.attributes.fitInsetsSides = 0
                }
            }

            // Backdrop for the InAppMessage only takes into effect if the InAppMessage is taking over the UI
            MessageBackdrop(
                visibility = isVisible,
                inAppMessageSettings = inAppMessageSettings,
                gestureTracker = gestureTracker
            )

            // Frame that holds the InAppMessage
            MessageFrame(
                visibility = isVisible,
                inAppMessageSettings = inAppMessageSettings,
                gestureTracker = gestureTracker,
                onCreated = onCreated,
                onDisposed = onDisposed
            )
        }
    } else {
        // Frame that holds the InAppMessage
        MessageFrame(
            visibility = isVisible,
            inAppMessageSettings = inAppMessageSettings,
            gestureTracker = gestureTracker,
            onCreated = onCreated,
            onDisposed = onDisposed
        )
    }
}
