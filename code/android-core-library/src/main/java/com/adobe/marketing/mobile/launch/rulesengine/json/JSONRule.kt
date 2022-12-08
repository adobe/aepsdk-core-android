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
import com.adobe.marketing.mobile.rulesengine.Evaluable
import com.adobe.marketing.mobile.services.Log
import org.json.JSONArray
import org.json.JSONObject

/**
 * The class representing a Rule
 */
internal class JSONRule private constructor(
    val condition: JSONObject,
    val consequences: JSONArray
) {
    companion object {

        private const val LOG_TAG = "JSONRule"
        private const val KEY_CONDITION = "condition"
        private const val KEY_CONSEQUENCES = "consequences"

        /**
         * Optionally constructs a new [JSONRule]
         *
         * @param jsonObject a [JSONObject] of the Rule
         * @return a new [JSONRule] or null
         */
        operator fun invoke(jsonObject: JSONObject?): JSONRule? {
            if (jsonObject !is JSONObject) return null
            val condition = jsonObject.getJSONObject(KEY_CONDITION)
            val consequences = jsonObject.getJSONArray(KEY_CONSEQUENCES)
            if (condition !is JSONObject || consequences !is JSONArray) {
                Log.error(
                    LaunchRulesEngineConstants.LOG_TAG,
                    LOG_TAG,
                    "Failed to extract [rule.condition] or [rule.consequences]."
                )
                return null
            }
            return JSONRule(condition, consequences)
        }
    }

    /**
     * Converts itself to a [LaunchRule]
     *
     * @return an object of [LaunchRule]
     */
    @JvmSynthetic
    internal fun toLaunchRule(extensionApi: ExtensionApi): LaunchRule? {
        val evaluable = JSONCondition.build(condition, extensionApi)?.toEvaluable()
        if (evaluable !is Evaluable) {
            Log.error(
                LaunchRulesEngineConstants.LOG_TAG,
                LOG_TAG,
                "Failed to build LaunchRule from JSON, the [rule.condition] can't be parsed to Evaluable."
            )
            return null
        }
        val consequenceList = consequences.map {
            JSONConsequence(it as? JSONObject)?.toRuleConsequence() ?: throw Exception()
        }
        return LaunchRule(evaluable, consequenceList)
    }
}
