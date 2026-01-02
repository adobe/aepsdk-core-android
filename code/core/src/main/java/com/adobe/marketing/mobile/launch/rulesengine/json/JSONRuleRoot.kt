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

package com.adobe.marketing.mobile.launch.rulesengine.json

import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.internal.util.map
import com.adobe.marketing.mobile.launch.rulesengine.LaunchRule
import com.adobe.marketing.mobile.launch.rulesengine.LaunchRulesEngineConstants
import com.adobe.marketing.mobile.services.Log
import org.json.JSONArray
import org.json.JSONObject

/**
 * The class representing a set of Rules
 */
internal class JSONRuleRoot private constructor(val version: String, val jsonArray: JSONArray, val reEvaluable: Boolean) {
    companion object {
        private const val LOG_TAG = "JSONRuleRoot"
        private const val KEY_VERSION = "version"
        private const val KEY_RULES = "rules"
        private const val KEY_REEVALUABLE = "reEvaluable"
        operator fun invoke(jsonObject: JSONObject): JSONRuleRoot? {
            val version = jsonObject.optString(KEY_VERSION, "0")
            val rules = jsonObject.optJSONArray(KEY_RULES)
            val reeValuable = jsonObject.optBoolean(KEY_REEVALUABLE, false)
            if (rules !is JSONArray) {
                Log.error(
                    LaunchRulesEngineConstants.LOG_TAG,
                    LOG_TAG,
                    "Failed to extract [launch_json.rules]"
                )
                return null
            }
            return JSONRuleRoot(version, rules, reeValuable)
        }
    }

    /**
     * Converts itself to a list of [LaunchRule]s
     *
     * @return a list of [LaunchRule]s
     */
    @JvmSynthetic
    fun toLaunchRules(extensionApi: ExtensionApi): List<LaunchRule> {
        return jsonArray.map {
            JSONRule(it as? JSONObject)?.toLaunchRule(extensionApi, reEvaluable) ?: throw Exception()
        }
    }
}
