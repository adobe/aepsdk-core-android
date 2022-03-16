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

import org.json.JSONArray
import org.json.JSONObject

internal object JSONUtils {
    @JvmStatic
    fun jsonObjectToMap(jsonObject: JSONObject?): Map<String, Any?>? {
        if (jsonObject !is JSONObject) return null
        return jsonObject.keys().asSequence().associateWith { key ->
            when (val value = jsonObject.get(key)) {
                is JSONObject -> {
                    jsonObjectToMap(value)
                }
                is JSONArray -> {
                    jsonArrayToList(value)
                }
                JSONObject.NULL -> null
                else -> value
            }
        }
    }

    @JvmStatic
    fun jsonArrayToList(jsonArray: JSONArray?): List<Any?>? {
        if (jsonArray !is JSONArray) return null
        val map = (0 until jsonArray.length()).associate { Pair(it, jsonArray[it]) }
        return jsonObjectToMap(JSONObject(map))?.values?.toList()
    }
}