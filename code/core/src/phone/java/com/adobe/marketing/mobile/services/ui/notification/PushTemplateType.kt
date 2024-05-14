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

package com.adobe.marketing.mobile.services.ui.notification

/**
 * Enum class representing the different types of out-of-the-box push templates.
 */
internal enum class PushTemplateType(val value: String) {
    BASIC("basic"), CAROUSEL("car"), INPUT_BOX("input"), ZERO_BEZEL("zb"), UNKNOWN("unknown");

    companion object {
        /**
         * Returns the [PushTemplateType] for the given string value.
         * @param value the string value to convert to [PushTemplateType]
         * @return the [PushTemplateType] for the given string value
         */
        @JvmStatic
        fun fromString(value: String?): PushTemplateType {
            return when (value) {
                "basic" -> BASIC
                "car" -> CAROUSEL
                "input" -> INPUT_BOX
                "zb" -> ZERO_BEZEL
                else -> UNKNOWN
            }
        }
    }
}
