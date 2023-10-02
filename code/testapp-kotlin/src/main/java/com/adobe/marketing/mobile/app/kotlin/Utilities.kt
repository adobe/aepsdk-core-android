/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
 */
package com.adobe.marketing.mobile.app.kotlin

import android.app.Activity
import android.app.Application
import com.adobe.marketing.mobile.AdobeCallbackWithError
import com.adobe.marketing.mobile.AdobeError
import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.ui.Alert
import com.adobe.marketing.mobile.services.ui.Presentable
import com.adobe.marketing.mobile.services.ui.PresentationError
import com.adobe.marketing.mobile.services.ui.PresentationUtilityProvider
import com.adobe.marketing.mobile.services.ui.alert.AlertEventListener
import com.adobe.marketing.mobile.services.ui.alert.AlertSettings
import java.io.InputStream

internal val presentationUtilityProvider = object : PresentationUtilityProvider {
    override fun getApplication(): Application {
        return ServiceProvider.getInstance().appContextService.application!!
    }

    override fun getCurrentActivity(): Activity? {
        return ServiceProvider.getInstance().appContextService.currentActivity
    }

    override fun getCachedContent(cacheName: String, key: String): InputStream? {
        return null
    }

    override fun openUri(uri: String): Boolean {
        ServiceProvider.getInstance().uriService.openUri(uri)
        return true
    }
}

internal val alertEventListener = object : AlertEventListener {
    override fun onPositiveResponse(alert: Presentable<Alert>) {}
    override fun onNegativeResponse(alert: Presentable<Alert>) {}
    override fun onShow(presentable: Presentable<Alert>) {}
    override fun onHide(presentable: Presentable<Alert>) {}
    override fun onDismiss(presentable: Presentable<Alert>) {}
    override fun onError(presentable: Presentable<Alert>, error: PresentationError) {}
}

internal fun registerEventListener(
    eventType: String,
    eventSource: String,
    hear: (event: Event) -> Unit
) {
    MobileCore.registerEventListener(
        eventType,
        eventSource,
        object :
            AdobeCallbackWithError<Event> {
            override fun call(event: Event?) {
                event?.run(hear)
            }

            override fun fail(error: AdobeError?) {
                throw RuntimeException("Failed to register EventListener!!!")
            }

        })
}

internal fun showAlert(event: Event) {
    val alert = Alert(AlertSettings.Builder()
        .title("Event")
        .message(event.toString())
        .positiveButtonText("OK")
        .build()
        , alertEventListener)

   ServiceProvider.getInstance().uiService.create(alert, presentationUtilityProvider)
}

internal fun showAlert(message: String) {
    val alert = Alert(AlertSettings.Builder()
        .title("Message")
        .message(message)
        .positiveButtonText("OK")
        .build()
        , alertEventListener)

    ServiceProvider.getInstance().uiService.create(alert, presentationUtilityProvider)
}
