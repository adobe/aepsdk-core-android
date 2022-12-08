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
import com.adobe.marketing.mobile.launch.rulesengine.LaunchRulesEngineConstants
import com.adobe.marketing.mobile.rulesengine.Evaluable
import com.adobe.marketing.mobile.services.Log
import org.json.JSONObject

/**
 * The class representing a Rule's condition
 *
 * @constructor Constructs a new [JSONCondition]
 */
internal abstract class JSONCondition {

    companion object {
        private const val LOG_TAG = "JSONCondition"
        private const val KEY_TYPE = "type"
        private const val KEY_DEFINITION = "definition"
        private const val TYPE_VALUE_GROUP = "group"
        private const val TYPE_VALUE_MATCHER = "matcher"
        private const val TYPE_VALUE_HISTORICAL = "historical"

        /**
         * Build a subclass of [JSONCondition]
         *
         * @param jsonCondition a JSON object of a Rule's condition
         * @return a subclass of [JSONCondition]
         */
        @JvmSynthetic
        internal fun build(jsonCondition: JSONObject?, extensionApi: ExtensionApi): JSONCondition? {
            if (jsonCondition !is JSONObject) return null
            return try {
                when (val type = jsonCondition.getString(KEY_TYPE)) {
                    TYPE_VALUE_GROUP -> GroupCondition(
                        JSONDefinition.buildDefinitionFromJSON(
                            jsonCondition.getJSONObject(
                                KEY_DEFINITION
                            ),
                            extensionApi
                        )
                    )
                    TYPE_VALUE_MATCHER -> MatcherCondition(
                        JSONDefinition.buildDefinitionFromJSON(
                            jsonCondition.getJSONObject(
                                KEY_DEFINITION
                            ),
                            extensionApi
                        )
                    )
                    TYPE_VALUE_HISTORICAL -> HistoricalCondition(
                        JSONDefinition.buildDefinitionFromJSON(
                            jsonCondition.getJSONObject(
                                KEY_DEFINITION
                            ),
                            extensionApi
                        ),
                        extensionApi
                    )
                    else -> {
                        Log.error(
                            LaunchRulesEngineConstants.LOG_TAG,
                            LOG_TAG,
                            "Unsupported condition type - $type"
                        )
                        null
                    }
                }
            } catch (e: Exception) {
                Log.error(
                    LaunchRulesEngineConstants.LOG_TAG,
                    LOG_TAG,
                    "Failed to parse [rule.condition] JSON, the error is: ${e.message}"
                )
                null
            }
        }
    }

    /**
     * Converts itself to a [Evaluable]
     *
     * @return an optional [Evaluable]
     */
    @JvmSynthetic
    abstract fun toEvaluable(): Evaluable?
}
