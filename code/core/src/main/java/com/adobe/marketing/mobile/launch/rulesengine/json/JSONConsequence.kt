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

import com.adobe.marketing.mobile.internal.CoreConstants
import com.adobe.marketing.mobile.internal.util.toMap
import com.adobe.marketing.mobile.launch.rulesengine.RuleConsequence
import com.adobe.marketing.mobile.services.Log
import org.json.JSONObject

/**
 * The class representing a Rule's consequence
 */
internal class JSONConsequence private constructor(
    private val id: String,
    private val type: String,
    private val detail: Map<String, Any?>?
) {
    companion object {
        private const val KEY_ID = "id"
        private const val KEY_TYPE = "type"
        private const val KEY_DETAIL = "detail"
        operator fun invoke(jsonObject: JSONObject?): JSONConsequence? {
            if (jsonObject !is JSONObject) return null
            return JSONConsequence(
                jsonObject.optString(KEY_ID),
                jsonObject.optString(KEY_TYPE),
                jsonObject.optJSONObject(KEY_DETAIL)?.toMap()
            )
        }
    }

    /**
     * Converts this object into a validated [RuleConsequence].
     *
     * @return a valid [RuleConsequence] or `null` if any validation check fails
     */
    @JvmSynthetic
    internal fun toRuleConsequence(): RuleConsequence? {
        val LOG_SOURCE = "JSONConsequence"
        if (id.isEmpty()) {
            Log.warning(CoreConstants.LOG_TAG, LOG_SOURCE, "Unable to find required field \"id\" in rules consequence.")
            return null
        }

        if (type.isEmpty()) {
            Log.warning(CoreConstants.LOG_TAG, LOG_SOURCE, "Unable to find required field \"type\" in rules consequence.")
            return null
        }

        if (detail.isNullOrEmpty()) {
            Log.warning(CoreConstants.LOG_TAG, LOG_SOURCE, "Unable to find required field \"detail\" in rules consequence.")
            return null
        }

        return RuleConsequence(id, type, detail)
    }
}
