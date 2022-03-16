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

import com.adobe.marketing.mobile.rulesengine.rules.RuleConsequence
import org.json.JSONObject
import java.lang.Exception

internal class JSONConsequence private constructor(
    private val id: String,
    private val type: String,
    private val detail: Map<String, Any?>?
) {
    companion object {
        private const val KEY_ID = "id"
        private const val KEY_TYPE = "type"
        private const val KEY_DETAIL = "detail"
        operator fun invoke(jsonObject: JSONObject): JSONConsequence? {
            return try {
                JSONConsequence(
                    jsonObject.optString(KEY_ID),
                    jsonObject.optString(KEY_TYPE),
                    JSONUtils.jsonObjectToMap(jsonObject.optJSONObject(KEY_DETAIL))
                )
            } catch (e: Exception) {
                //TODO: logging error
                null
            }
        }
    }

    fun toRuleConsequences(): RuleConsequence {
        return RuleConsequence(this.id, this.type, this.detail)
    }
}