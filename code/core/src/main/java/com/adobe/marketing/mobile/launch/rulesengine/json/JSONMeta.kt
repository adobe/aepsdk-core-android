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

import com.adobe.marketing.mobile.launch.rulesengine.RuleMeta
import org.json.JSONObject

/**
 * The class representing a Rule's consequence
 */
internal class JSONMeta private constructor(
    private val reEvaluate: Boolean,
) {
    companion object {
        private const val KEY_REEVALUATE = "reEvaluate"
        operator fun invoke(jsonObject: JSONObject?): JSONMeta {
            return JSONMeta(
                jsonObject?.optBoolean(KEY_REEVALUATE, false) ?: false
            )
        }
    }

    /**
     * Converts this object into a validated [RuleMeta].
     *
     * @return a valid [RuleMeta] or `null` if any validation check fails
     */
    @JvmSynthetic
    internal fun toMeta(): RuleMeta {
        return RuleMeta(reEvaluate)
    }
}
