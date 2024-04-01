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

package com.adobe.marketing.mobile.services.ui.floatingbutton.views

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import com.adobe.marketing.mobile.services.ui.common.PresentationStateManager
import com.adobe.marketing.mobile.services.ui.floatingbutton.FloatingButtonSettings
import com.adobe.marketing.mobile.services.ui.floatingbutton.FloatingButtonViewModel

/**
 * Represents the floating button screen. Composes the [FloatingButton] composable and
 * manages its visibility.
 * @param presentationStateManager the [PresentationStateManager] for the floating button
 * @param floatingButtonSettings the [FloatingButtonSettings] for the floating button
 * @param floatingButtonViewModel the [FloatingButtonViewModel] for the floating button
 * @param onTapDetected the callback to be notified when the floating button is tapped
 * @param onPanDetected the callback to be notified when the floating button is dragged
 */
@Composable
internal fun FloatingButtonScreen(
    presentationStateManager: PresentationStateManager,
    floatingButtonSettings: FloatingButtonSettings,
    floatingButtonViewModel: FloatingButtonViewModel,
    onTapDetected: () -> Unit,
    onPanDetected: (Offset) -> Unit
) {
    AnimatedVisibility(
        visibleState = presentationStateManager.visibilityState,
        enter = fadeIn()
    ) {
        val orientation = LocalConfiguration.current.orientation

        FloatingButton(
            settings = floatingButtonSettings,
            graphic = floatingButtonViewModel.currentGraphic,
            offset = if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                floatingButtonViewModel.landscapeOffSet
            } else {
                floatingButtonViewModel.portraitOffSet
            },
            onClick = { onTapDetected() },
            onDragFinished = {
                floatingButtonViewModel.onPositionUpdate(it, orientation)
                onPanDetected(it)
            }
        )
    }
}
