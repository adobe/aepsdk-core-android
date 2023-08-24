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

package com.adobe.marketing.mobile.services.ui.vnext.floatingbutton

import android.content.Context
import androidx.compose.ui.platform.ComposeView
import com.adobe.marketing.mobile.services.ui.vnext.FloatingButton
import com.adobe.marketing.mobile.services.ui.vnext.PresentationDelegate
import com.adobe.marketing.mobile.services.ui.vnext.PresentationUtilityProvider
import com.adobe.marketing.mobile.services.ui.vnext.common.AEPPresentable
import com.adobe.marketing.mobile.services.ui.vnext.common.AppLifecycleProvider
import com.adobe.marketing.mobile.services.ui.vnext.floatingbutton.views.FloatingButtonScreen
import java.io.InputStream

/**
 * Represents a presentable floating button presentation
 * @param floatingButton the floating button to be presented
 * @param floatingButtonViewModel the view model for the floating button
 * @param presentationDelegate the presentation delegate used when for notifying actions on the floating button
 * @param presentationUtilityProvider the presentation utility provider used for performing actions on the floating button
 * @param appLifecycleProvider the app lifecycle provider used for listening to app lifecycle events
 */
internal class FloatingButtonPresentable(
    private val floatingButton: FloatingButton,
    private val floatingButtonViewModel: FloatingButtonViewModel,
    presentationDelegate: PresentationDelegate?,
    presentationUtilityProvider: PresentationUtilityProvider,
    appLifecycleProvider: AppLifecycleProvider
) : AEPPresentable<FloatingButton>(
    floatingButton,
    presentationUtilityProvider,
    presentationDelegate,
    appLifecycleProvider
) {
    // event handler for the floating button
    private val floatingButtonEventHandler = object : FloatingButtonEventHandler {
        override fun updateGraphic(graphic: InputStream) {
            floatingButtonViewModel.onGraphicUpdate(graphic)
        }
    }

    init {
        floatingButton.eventHandler = floatingButtonEventHandler

        // update the graphic on the view model if it is already set
        floatingButton.settings.initialGraphic?.let {
            floatingButtonViewModel.onGraphicUpdate(it)
        }
    }

    override fun getContent(activityContext: Context): ComposeView {
        return ComposeView(activityContext).apply {
            setContent {
                FloatingButtonScreen(
                    presentationStateManager = presentationStateManager,
                    floatingButtonSettings = floatingButton.settings,
                    floatingButtonViewModel = floatingButtonViewModel,
                    onTapDetected = { floatingButton.eventListener.onTapDetected(this@FloatingButtonPresentable) },
                    onPanDetected = { floatingButton.eventListener.onPanDetected(this@FloatingButtonPresentable) }
                )
            }
        }
    }

    override fun gateDisplay(): Boolean {
        // Floating button presentation display does not need to be gated via delegate consultation
        return false
    }

    override fun getPresentation(): FloatingButton {
        return floatingButton
    }
}
