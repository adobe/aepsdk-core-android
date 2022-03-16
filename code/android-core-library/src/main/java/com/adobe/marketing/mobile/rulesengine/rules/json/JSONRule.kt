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

package com.adobe.marketing.mobile.rulesengine.rules.json

import com.adobe.marketing.mobile.rulesengine.rules.LaunchRule
import org.json.JSONObject

internal class JSONRule private constructor(val jsonObject: JSONObject) {
    companion object {
        private const val KEY_CONDITION = "condition"
        private const val KEY_CONSEQUENCES = "consequences"
        operator fun invoke(jsonObject: JSONObject): JSONRule? {
            jsonObject?.let {
                if (it.has(KEY_CONDITION) && (it.optString(KEY_CONDITION) != null) && it.has(
                        KEY_CONSEQUENCES
                    ) && (it.optJSONArray(
                        KEY_CONSEQUENCES
                    ) != null)
                ) {
                    return JSONRule(jsonObject)
                }
            }
            return null
        }
    }

    fun toLaunchRule(): LaunchRule? {
        return null
    }
}