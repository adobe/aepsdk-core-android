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

import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

internal fun buildJSONObject(jsonObject: String): JSONObject {
    val jsonObj = JSONTokener(jsonObject).nextValue() as? JSONObject
    if (jsonObj !is JSONObject) throw IllegalArgumentException()
    return jsonObj
}

internal fun buildJSONArray(jsonArray: String): JSONArray {
    val jsonAry = JSONTokener(jsonArray).nextValue() as? JSONArray
    if (jsonAry !is JSONArray) throw IllegalArgumentException()
    return jsonAry
}