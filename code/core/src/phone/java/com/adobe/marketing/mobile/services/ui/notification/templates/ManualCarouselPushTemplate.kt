/*
  Copyright 2024 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.services.ui.notification.templates

import android.content.Intent
import com.adobe.marketing.mobile.services.ui.notification.PushTemplateConstants

internal class ManualCarouselPushTemplate : CarouselPushTemplate {
    internal var intentAction: String? = null
        private set
    internal var centerImageIndex: Int = PushTemplateConstants.DefaultValues.NO_CENTER_INDEX_SET

    constructor(data: Map<String, String>) : super(data) {
        centerImageIndex = getDefaultCarouselIndex(carouselLayoutType)
    }

    constructor(intent: Intent) : super(intent) {
        intentAction = intent.action
        centerImageIndex = intent.getIntExtra(
            PushTemplateConstants.IntentKeys.CENTER_IMAGE_INDEX,
            getDefaultCarouselIndex(carouselLayoutType)
        )
    }

    companion object {
        private fun getDefaultCarouselIndex(carouselLayoutType: String): Int {
            return if (carouselLayoutType == PushTemplateConstants.DefaultValues.FILMSTRIP_CAROUSEL_MODE) {
                PushTemplateConstants.DefaultValues.FILMSTRIP_CAROUSEL_CENTER_INDEX
            } else {
                PushTemplateConstants.DefaultValues.MANUAL_CAROUSEL_START_INDEX
            }
        }
    }
}
