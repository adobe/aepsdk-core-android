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

import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.rulesengine.Evaluable
import org.json.JSONObject

internal abstract class JSONCondition {

    companion object {
        private const val LOG_TAG = "JSONCondition"
        private const val KEY_TYPE = "type"
        private const val KEY_DEFINITION = "definition"
        private const val TYPE_VALUE_GROUP = "group"
        private const val TYPE_VALUE_MATCHER = "matcher"
        private const val TYPE_VALUE_HISTORICAL = "historical"

        @JvmSynthetic
        internal fun build(jsonCondition: JSONObject?): JSONCondition? {
            if (jsonCondition !is JSONObject) return null
            return try {
                when (val type = jsonCondition.getString(KEY_TYPE)) {
                    TYPE_VALUE_GROUP -> GroupCondition(
                        JSONDefinition.buildDefinitionFromJSON(
                            jsonCondition.getJSONObject(
                                KEY_DEFINITION
                            )
                        )
                    )
                    TYPE_VALUE_MATCHER -> MatcherCondition(
                        JSONDefinition.buildDefinitionFromJSON(
                            jsonCondition.getJSONObject(
                                KEY_DEFINITION
                            )
                        )
                    )
                    TYPE_VALUE_HISTORICAL -> HistoricalCondition(
                        JSONDefinition.buildDefinitionFromJSON(
                            jsonCondition.getJSONObject(
                                KEY_DEFINITION
                            )
                        )
                    )
                    else -> {
                        MobileCore.log(
                            LoggingMode.ERROR,
                            LOG_TAG,
                            "Unsupported condition type - $type"
                        )
                        null
                    }
                }
            } catch (e: Exception) {
                MobileCore.log(
                    LoggingMode.ERROR,
                    LOG_TAG,
                    "Failed to parse [rule.condition] JSON, the error is: ${e.message}"
                )
                null
            }
        }
    }

    @JvmSynthetic
    abstract fun toEvaluable(): Evaluable?
}



