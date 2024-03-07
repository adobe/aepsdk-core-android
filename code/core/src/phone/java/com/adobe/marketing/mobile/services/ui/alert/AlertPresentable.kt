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

package com.adobe.marketing.mobile.services.ui.alert

import android.content.Context
import androidx.compose.ui.platform.ComposeView
import com.adobe.marketing.mobile.services.ui.Alert
import com.adobe.marketing.mobile.services.ui.InAppMessage
import com.adobe.marketing.mobile.services.ui.Presentation
import com.adobe.marketing.mobile.services.ui.PresentationDelegate
import com.adobe.marketing.mobile.services.ui.PresentationUtilityProvider
import com.adobe.marketing.mobile.services.ui.alert.views.AlertScreen
import com.adobe.marketing.mobile.services.ui.common.AEPPresentable
import com.adobe.marketing.mobile.services.ui.common.AppLifecycleProvider

/**
 * Represents an Alert presentable.
 * @param alert the alert that this presentable will be tied to
 * @param presentationDelegate the presentation delegate to use for notifying lifecycle events
 * @param presentationUtilityProvider the presentation utility provider to use for performing operations on the presentable
 * @param appLifecycleProvider the app lifecycle provider to use for listening to lifecycle events
 */
internal class AlertPresentable(
    val alert: Alert,
    presentationDelegate: PresentationDelegate?,
    presentationUtilityProvider: PresentationUtilityProvider,
    appLifecycleProvider: AppLifecycleProvider
) : AEPPresentable<Alert>(
    alert,
    presentationUtilityProvider,
    presentationDelegate,
    appLifecycleProvider
) {
    override fun getContent(activityContext: Context): ComposeView {
        return ComposeView(activityContext).apply {
            setContent {
                AlertScreen(
                    presentationStateManager = presentationStateManager,
                    alertSettings = alert.settings,
                    onPositiveResponse = {
                        alert.eventListener.onPositiveResponse(this@AlertPresentable)
                        dismiss()
                    },
                    onNegativeResponse = {
                        alert.eventListener.onNegativeResponse(this@AlertPresentable)
                        dismiss()
                    },
                    onBackPressed = {
                        dismiss()
                    }
                )
            }
        }
    }

    override fun gateDisplay(): Boolean {
        return false
    }

    override fun getPresentation(): Alert {
        return alert
    }

    override fun hasConflicts(visiblePresentations: List<Presentation<*>>): Boolean {
        // Only show if there are no other alerts or in-app messages visible
        return visiblePresentations.any { (it is Alert || it is InAppMessage) }
    }
}
