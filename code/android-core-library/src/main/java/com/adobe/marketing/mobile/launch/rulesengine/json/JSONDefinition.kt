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
import com.adobe.marketing.mobile.internal.util.toMap
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * The class representing a Rule condition's definition
 *
 * @property logic the `logic` filed of the JSON object
 * @property conditions the `conditions` filed of the JSON object
 * @property key the `key` filed of the JSON object
 * @property matcher the `matcher` filed of the JSON object
 * @property values the `values` filed of the JSON object
 * @property events the `events` filed of the JSON object
 * @property value the `value` filed of the JSON object
 * @property from the `from` filed of the JSON object
 * @property to the `to` filed of the JSON object
 * @property searchType the `searchType` filed of the JSON object
 * @constructor Constructs a new [JSONDefinition]
 */
internal data class JSONDefinition(
    val logic: String?,
    val conditions: List<JSONCondition>?,
    val key: String?,
    val matcher: String?,
    val values: List<Any?>?,
    val events: List<Map<String, Any?>>?,
    val value: Any?,
    val from: Long?,
    val to: Long?,
    val searchType: String?
) {
    companion object {
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

        /**
         * Builds a new [JSONDefinition]
         *
         * @param jsonObject a [JSONObject] of a Rule condition's definition
         * @return a new [JSONDefinition]
         */
        @JvmSynthetic
        internal fun buildDefinitionFromJSON(
            jsonObject: JSONObject,
            extensionApi: ExtensionApi
        ): JSONDefinition {
            val logic = jsonObject.opt(DEFINITION_KEY_LOGIC) as? String
            val conditions =
                buildConditionList(jsonObject.optJSONArray(DEFINITION_KEY_CONDITIONS), extensionApi)
            val key = jsonObject.opt(DEFINITION_KEY_KEY) as? String
            val matcher = jsonObject.opt(DEFINITION_KEY_MATCHER) as? String
            val values =
                buildValueList(jsonObject.optJSONArray(DEFINITION_KEY_VALUES))
            val events =
                buildValueMapList(jsonObject.optJSONArray(DEFINITION_KEY_EVENTS))
            val value = jsonObject.opt(DEFINITION_KEY_VALUE)
            val from = jsonObject.opt(DEFINITION_KEY_FROM) as? Long
            val to = jsonObject.opt(DEFINITION_KEY_TO) as? Long
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

        private fun buildConditionList(
            jsonArray: JSONArray?,
            extensionApi: ExtensionApi
        ): List<JSONCondition>? {
            return jsonArray?.map {
                JSONCondition.build(it as? JSONObject, extensionApi)
                    ?: throw JSONException("Unsupported [rule.condition] JSON format: $it ")
            }
        }

        private fun buildValueList(jsonArray: JSONArray?): List<Any?>? {
            return jsonArray?.map { it }
        }

        private fun buildValueMapList(jsonArray: JSONArray?): List<Map<String, Any?>>? {
            return jsonArray?.map {
                (it as? JSONObject)?.toMap()
                    ?: throw JSONException("Unsupported [rule.condition.historical.events] JSON format: $it ")
            }
        }
    }
}
