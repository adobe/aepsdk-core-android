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

import com.adobe.marketing.mobile.services.ui.notification.PushTemplateConstants
import com.adobe.marketing.mobile.util.DataReader

internal class ZeroBezelPushTemplate : AEPPushTemplate {

    internal enum class ZeroBezelStyle(val collapsedStyle: String) {
        IMAGE("img"),
        TEXT("txt");
        companion object {
            private val zeroBezelStyleMap = values().associateBy { it.collapsedStyle }
            internal fun getCollapsedStyleFromString(style: String): ZeroBezelStyle {
                return zeroBezelStyleMap[style] ?: TEXT
            }
        }
    }

    internal var collapsedStyle: ZeroBezelStyle
        private set

    constructor(data: Map<String, String>) : super(data) {
        collapsedStyle = ZeroBezelStyle.getCollapsedStyleFromString(
            DataReader.optString(
                data,
                PushTemplateConstants.PushPayloadKeys.ZERO_BEZEL_COLLAPSED_STYLE,
                "txt"
            )
        )
    }
}
