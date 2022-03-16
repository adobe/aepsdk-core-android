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

import com.adobe.marketing.mobile.rulesengine.Evaluable
import com.adobe.marketing.mobile.rulesengine.rules.LaunchRule
import com.adobe.marketing.mobile.rulesengine.rules.RuleConsequence
import org.json.JSONArray
import org.json.JSONObject

internal class JSONRule private constructor(
    val condition: JSONObject,
    val consequences: JSONArray
) {
    companion object {
        private const val KEY_CONDITION = "condition"
        private const val KEY_CONSEQUENCES = "consequences"
        operator fun invoke(jsonObject: JSONObject): JSONRule? {
            val condition = jsonObject.getJSONObject(KEY_CONDITION)
            val consequences = jsonObject.getJSONArray(KEY_CONSEQUENCES)
            if (condition !is JSONObject || consequences !is JSONArray) return null

            return JSONRule(condition, consequences)
        }
    }

    fun toLaunchRule(): LaunchRule? {
        val evaluable = JSONCondition.build(condition)?.toEvaluable()

        val consequenceList = (0 until consequences.length()).associate {
            Pair(it, JSONConsequence(consequences.getJSONObject(it))?.toRuleConsequences())
        }.filterValues { it != null }.values.toList() as? List<RuleConsequence>
        if (evaluable !is Evaluable) return null
        if (consequenceList !is List<*>) return null

        return LaunchRule(evaluable, consequenceList)
    }
}