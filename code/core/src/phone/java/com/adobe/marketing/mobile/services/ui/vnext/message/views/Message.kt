package com.adobe.marketing.mobile.services.ui.vnext.message.views

import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceConstants
import com.adobe.marketing.mobile.services.ui.vnext.message.GestureTracker
import com.adobe.marketing.mobile.services.ui.vnext.message.InAppMessageSettings
import com.adobe.marketing.mobile.services.ui.vnext.message.mapping.MessageAnimationMapper

@Composable
internal fun Message(
    isVisible: State<Boolean>,
    inAppMessageSettings: InAppMessageSettings,
    onCreated: (WebView) -> Unit
) {
    val visibility: State<Boolean> = remember { isVisible }

    val gestureTracker: GestureTracker =
        remember {
            GestureTracker(MessageAnimationMapper.getExitTransitionFor(inAppMessageSettings.dismissAnimation)) {
                // send an event to change visibility and dismiss the message
                Log.debug(
                    ServiceConstants.LOG_TAG, "MessageFrame",
                    "GestureTracker: onGestureDetected: $it"
                )
            }
        }

    // Backdrop for the InAppMessage
    MessageBackdrop(visibility = visibility, inAppMessageSettings = inAppMessageSettings)

    // Frame that holds the InAppMessage
    MessageFrame(
        isVisible = visibility,
        inAppMessageSettings = inAppMessageSettings,
        gestureTracker = gestureTracker,
        onCreated = onCreated
    )
}