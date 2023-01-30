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

import com.adobe.marketing.mobile.AdobeCallbackWithError
import com.adobe.marketing.mobile.AdobeError
import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.ui.AlertSetting
import java.lang.RuntimeException

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
    ServiceProvider.getInstance().uiService.showAlert(
        AlertSetting.build(
            "show event",
            "$event",
            "OK",
            "Cancel"
        ), null
    )
}

internal fun showAlert(message: String) {
    ServiceProvider.getInstance().uiService.showAlert(
        AlertSetting.build(
            "message",
            message,
            "OK",
            "Cancel"
        ), null
    )
}
