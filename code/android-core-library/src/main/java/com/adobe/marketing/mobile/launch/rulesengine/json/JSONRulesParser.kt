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
import com.adobe.marketing.mobile.launch.rulesengine.LaunchRule
import com.adobe.marketing.mobile.launch.rulesengine.LaunchRulesEngineConstants
import com.adobe.marketing.mobile.services.Log
import org.json.JSONObject
import org.json.JSONTokener

/**
 * Parses the JSON string to a list of [LaunchRule]s
 */
object JSONRulesParser {
    private const val LOG_TAG = "JSONRulesParser"

    /**
     * Parses a set of JSON rules to a list of [LaunchRule]s
     *
     * @param jsonString a JSON string
     * @return a list of [LaunchRule]s
     */
    @JvmStatic
    fun parse(jsonString: String, extensionApi: ExtensionApi): List<LaunchRule>? {
        try {
            val jsonObject = JSONTokener(jsonString).nextValue()
            if (jsonObject is JSONObject) {
                return JSONRuleRoot(jsonObject)?.toLaunchRules(extensionApi)
            }
        } catch (e: Exception) {
            Log.error(
                LaunchRulesEngineConstants.LOG_TAG,
                LOG_TAG,
                "Failed to parse launch rules JSON: \n $jsonString"
            )
        }
        return null
    }
}
