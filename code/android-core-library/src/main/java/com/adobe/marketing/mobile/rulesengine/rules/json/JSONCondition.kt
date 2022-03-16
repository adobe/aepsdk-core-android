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
import org.json.JSONArray
import org.json.JSONObject

internal abstract class JSONCondition {

    companion object {
        private const val KEY_TYPE = "type"
        private const val KEY_DEFINITION = "definition"
        private const val TYPE_VALUE_GROUP = "group"
        private const val TYPE_VALUE_MATCHER = "matcher"
        private const val TYPE_VALUE_HISTORICAL = "historical"
        private const val DEFINITION_KEY_LOGIC = "logic"
        private const val DEFINITION_KEY_CONDITIONS = "conditions"
        private const val DEFINITION_KEY_KEY = "key"
        private const val DEFINITION_KEY_MATCHER = "matcher"
        private const val DEFINITION_KEY_VALUES = "values"
        private const val DEFINITION_KEY_EVENTS = "events"
        private const val DEFINITION_KEY_VALUE = "value"
        private const val DEFINITION_KEY_FROM = "from"
        private const val DEFINITION_KEY_TO = "to"
        private const val DEFINITION_KEY_SEARCH_TYPE = "searchType"

        fun build(jsonObject: JSONObject): JSONCondition? {
            return try {
                when (jsonObject.getString(KEY_TYPE)) {
                    TYPE_VALUE_GROUP -> GroupCondition(
                        buildDefinition(
                            jsonObject.getJSONObject(
                                KEY_DEFINITION
                            )
                        )
                    )
                    TYPE_VALUE_MATCHER -> MatcherCondition(
                        buildDefinition(
                            jsonObject.getJSONObject(
                                KEY_DEFINITION
                            )
                        )
                    )
                    TYPE_VALUE_HISTORICAL -> HistoricalCondition(
                        buildDefinition(jsonObject.getJSONObject(KEY_DEFINITION))
                    )
                    else -> {
                        //TODO: logging error
                        null
                    }
                }
            } catch (e: Exception) {
                //TODO: logging error
                null
            }
        }

        private fun buildConditionList(jsonArray: JSONArray?): List<JSONCondition>? {
            //TODO:
            return null
        }

        private fun buildAnyList(jsonArray: JSONArray?): List<Any?>? {
            //TODO:
            return null
        }

        private fun buildMapList(jsonArray: JSONArray?): List<Map<String, Any?>>? {
            //TODO:
            return null
        }

        private fun buildDefinition(jsonObject: JSONObject): JSONDefinition {

            val logic = jsonObject.opt(DEFINITION_KEY_LOGIC) as? String
            val conditions = buildConditionList(jsonObject.optJSONArray(DEFINITION_KEY_CONDITIONS))
            val key = jsonObject.opt(DEFINITION_KEY_KEY) as? String
            val matcher = jsonObject.opt(DEFINITION_KEY_MATCHER) as? String
            val values = buildAnyList(jsonObject.optJSONArray(DEFINITION_KEY_VALUES))
            val events = buildMapList(jsonObject.optJSONArray(DEFINITION_KEY_EVENTS))
            val value = jsonObject.opt(DEFINITION_KEY_VALUE)
            val from = jsonObject.opt(DEFINITION_KEY_FROM) as? Int
            val to = jsonObject.opt(DEFINITION_KEY_TO) as? Int
            val searchType = jsonObject.opt(DEFINITION_KEY_SEARCH_TYPE) as? String
            return JSONDefinition(
                logic,
                conditions,
                key,
                matcher,
                values,
                events,
                value,
                from,
                to,
                searchType
            )
        }
    }

    abstract fun toEvaluable(): Evaluable?
}



