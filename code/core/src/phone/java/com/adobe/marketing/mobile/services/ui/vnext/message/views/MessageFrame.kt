package com.adobe.marketing.mobile.services.ui.vnext.message.views

import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.adobe.marketing.mobile.services.ui.vnext.message.GestureTracker
import com.adobe.marketing.mobile.services.ui.vnext.message.InAppMessageSettings
import com.adobe.marketing.mobile.services.ui.vnext.message.mapping.MessageAlignmentMapper
import com.adobe.marketing.mobile.services.ui.vnext.message.mapping.MessageAnimationMapper
import com.adobe.marketing.mobile.services.ui.vnext.message.mapping.MessageArrangementMapper

@Composable
internal fun MessageFrame(isVisible: State<Boolean>,
                          inAppMessageSettings: InAppMessageSettings,
                          gestureTracker: GestureTracker,
                          onCreated: (WebView) -> Unit) {
    val currentConfiguration = LocalConfiguration.current
    val horizontalPadding =
        remember { ((inAppMessageSettings.horizontalInset * currentConfiguration.screenWidthDp) / 100).dp }
    val verticalPadding =
        remember { ((inAppMessageSettings.horizontalInset * currentConfiguration.screenHeightDp) / 100).dp }

    AnimatedVisibility(
        visible = isVisible.value,
        enter = MessageAnimationMapper.getEnterTransitionFor(inAppMessageSettings.displayAnimation),
        exit = gestureTracker.getExitTransition()
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding, vertical = verticalPadding)
                .clickable(
                    enabled = !inAppMessageSettings.shouldTakeOverUi,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {
                        gestureTracker.onGestureDetected(InAppMessageSettings.MessageGesture.BACKGROUND_TAP)
                        // send an event to change visibility and dismiss the message
                    }
                ),
            horizontalArrangement = MessageArrangementMapper.getHorizontalArrangement(
                inAppMessageSettings.horizontalAlignment
            ),
            verticalAlignment = MessageAlignmentMapper.getVerticalAlignment(inAppMessageSettings.verticalAlignment)
        ) {
            // The content of the InAppMessage
            MessageContent(inAppMessageSettings, onCreated, gestureTracker)
        }
    }
}