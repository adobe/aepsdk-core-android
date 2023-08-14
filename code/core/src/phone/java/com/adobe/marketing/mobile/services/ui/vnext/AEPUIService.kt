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

package com.adobe.marketing.mobile.services.ui.vnext

import com.adobe.marketing.mobile.services.ui.vnext.common.AppLifecycleProvider
import com.adobe.marketing.mobile.services.ui.vnext.message.MessagePresentable

class AEPUIService : UIService {
    private var presentationDelegate: PresentationDelegate? = null

    @Suppress("UNCHECKED_CAST")
    override fun <T : Presentation<*>> create(
        presentation: T,
        presentationUtilityProvider: PresentationUtilityProvider
    ): Presentable<T> {
        // start the app lifecycle provider if not started
        AppLifecycleProvider.INSTANCE.start(presentationUtilityProvider.getApplication())

        when (presentation) {
            is InAppMessage -> {
                return MessagePresentable(
                    presentation,
                    presentationDelegate,
                    presentationUtilityProvider,
                    AppLifecycleProvider.INSTANCE
                ) as Presentable<T>
            }
            else -> {
                throw IllegalArgumentException("Presentation type not supported")
            }
        }
    }

    override fun setPresentationDelegate(presentationDelegate: PresentationDelegate) {
        this.presentationDelegate = presentationDelegate
    }
}
