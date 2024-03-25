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

package com.adobe.marketing.mobile.services.ui.pushtemplate

enum class PushTemplateType(val value: String) {
    BASIC("basic"), CAROUSEL("car"), INPUT_BOX("input"), UNKNOWN("unknown");

    companion object {
        @JvmStatic
        fun fromString(value: String?): PushTemplateType {
            return when (value) {
                "basic" -> BASIC
                "car" -> CAROUSEL
                "input" -> INPUT_BOX
                else -> UNKNOWN
            }
        }
    }
}
