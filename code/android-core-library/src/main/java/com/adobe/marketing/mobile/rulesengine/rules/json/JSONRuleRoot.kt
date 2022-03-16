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
import org.json.JSONArray
import org.json.JSONObject

internal class JSONRuleRoot private constructor(val version: String, val jsonArray: JSONArray) {
    companion object {
        private const val KEY_VERSION = "version"
        private const val KEY_RULES = "rules"
        operator fun invoke(jsonObject: JSONObject): JSONRuleRoot? {
            val version = jsonObject.optString(KEY_VERSION, "0")
            val rules = jsonObject.optJSONArray(KEY_RULES)
            if (rules !is JSONArray) return null
            return JSONRuleRoot(version, rules)
        }
    }

    fun toLaunchRules(): List<LaunchRule>? {
        try {
            val launchRules = (0 until jsonArray.length()).associate {
                val launchRule = JSONRule(jsonArray.getJSONObject(it))?.toLaunchRule()
                Pair(it, launchRule)
            }.filterValues { it != null }.values.toList() as? List<LaunchRule>
            return launchRules
        } catch (e: Exception) {
            //TODO: logging error
        }
        return null
    }
}