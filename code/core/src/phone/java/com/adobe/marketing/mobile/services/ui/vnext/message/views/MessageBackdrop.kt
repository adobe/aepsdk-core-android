package com.adobe.marketing.mobile.services.ui.vnext.message.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import com.adobe.marketing.mobile.services.ui.vnext.message.InAppMessageSettings


@Composable
internal fun MessageBackdrop(
    visibility: State<Boolean>,
    inAppMessageSettings: InAppMessageSettings
) {
    val backdropColor = remember { Color(inAppMessageSettings.backdropColor.toColorInt()) }

    AnimatedVisibility(visible = visibility.value, enter = fadeIn(), exit = fadeOut()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backdropColor.copy(alpha = inAppMessageSettings.backdropOpacity))
        ) {
            // Intentionally empty. Primarily used to ease in and out the backdrop separate from the message itself.
            // This allows the backdrop to be faded in and out while the message is animating in and out reducing jankiness
            // in the animation.
        }
    }
}