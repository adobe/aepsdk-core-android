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

package com.adobe.marketing.mobile.launch.rulesengine

import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.SharedStateResolution
import com.adobe.marketing.mobile.internal.util.flattening
import com.adobe.marketing.mobile.internal.util.serializeToQueryString
import com.adobe.marketing.mobile.rulesengine.TokenFinder
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.util.TimeUtils
import org.json.JSONObject
import java.security.SecureRandom

internal class LaunchTokenFinder(val event: Event, val extensionApi: ExtensionApi) : TokenFinder {

    companion object {
        private const val LOG_TAG = "LaunchTokenFinder"
        private const val KEY_EVENT_TYPE = "~type"
        private const val KEY_EVENT_SOURCE = "~source"
        private const val KEY_TIMESTAMP_UNIX = "~timestampu"
        private const val KEY_TIMESTAMP_ISO8601 = "~timestampz"
        private const val KEY_TIMESTAMP_PLATFORM = "~timestampp"
        private const val KEY_SDK_VERSION = "~sdkver"
        private const val KEY_CACHEBUST = "~cachebust"
        private const val KEY_ALL_URL = "~all_url"
        private const val KEY_ALL_JSON = "~all_json"
        private const val KEY_SHARED_STATE = "~state."
        private const val EMPTY_STRING = ""
        private const val RANDOM_INT_BOUNDARY = 100000000
        private const val SHARED_STATE_KEY_DELIMITER = "/"
    }

    // ========================================================
    // public methods
    // ========================================================

    /**
     * Returns the value for the [key] provided as input.
     *
     *
     * If the `key` is a special key recognized by SDK, the value is determined based on incoming `Event`,
     * or `EventHub#moduleSharedStates` data. Otherwise the key is searched in the current `Event`'s data
     * and the corresponding value is returned.
     *
     * @param key [String] containing the key whose value needs to be determined
     *
     * @return [Any] containing value to be substituted for the [key], null if the key does not exist
     */
    override fun get(key: String): Any? {
        return when (key.trim()) {
            EMPTY_STRING -> null
            KEY_EVENT_TYPE -> event.type
            KEY_EVENT_SOURCE -> event.source
            KEY_TIMESTAMP_UNIX -> TimeUtils.getUnixTimeInSeconds().toString()
            KEY_TIMESTAMP_ISO8601 -> TimeUtils.getISO8601DateNoColon()
            KEY_TIMESTAMP_PLATFORM -> TimeUtils.getISO8601UTCDateWithMilliseconds()
            KEY_SDK_VERSION -> MobileCore.extensionVersion()
            KEY_CACHEBUST -> SecureRandom().nextInt(RANDOM_INT_BOUNDARY).toString()
            KEY_ALL_URL -> {
                if (event.eventData == null) {
                    Log.debug(
                        LaunchRulesEngineConstants.LOG_TAG,
                        LOG_TAG,
                        "Triggering event ${event.uniqueIdentifier} - Event data is null, can not use it to generate an url query string"
                    )
                    return EMPTY_STRING
                }
                val eventDataAsObjectMap = event.eventData.flattening()
                eventDataAsObjectMap.serializeToQueryString()
            }
            KEY_ALL_JSON -> {
                if (event.eventData == null) {
                    Log.debug(
                        LaunchRulesEngineConstants.LOG_TAG,
                        LOG_TAG,
                        "Triggering event ${event.uniqueIdentifier} - Event data is null, can not use it to generate a json string"
                    )
                    return EMPTY_STRING
                }
                try {
                    JSONObject(event.eventData).toString()
                } catch (e: Exception) {
                    Log.debug(
                        LaunchRulesEngineConstants.LOG_TAG,
                        LOG_TAG,
                        "Triggering event ${event.uniqueIdentifier} - Failed to generate a json string ${e.message}"
                    )
                    return EMPTY_STRING
                }
            }
            else -> {
                if (key.startsWith(KEY_SHARED_STATE)) {
                    getValueFromSharedState(key)
                } else {
                    getValueFromEvent(key)
                }
            }
        }
    }

    // ========================================================
    // private getter methods
    // ========================================================

    private fun getValueFromSharedState(key: String): Any? {
        val sharedStateKeyString = key.substring(KEY_SHARED_STATE.length)
        if (sharedStateKeyString.isBlank()) {
            return null
        }
        if (!sharedStateKeyString.contains(SHARED_STATE_KEY_DELIMITER)) {
            return null
        }
        val (sharedStateName, dataKeyName) = sharedStateKeyString.split(SHARED_STATE_KEY_DELIMITER)
        val sharedStateMap = extensionApi.getSharedState(
            sharedStateName,
            event,
            false,
            SharedStateResolution.ANY
        )?.value?.flattening()
        if (sharedStateMap.isNullOrEmpty() || dataKeyName.isBlank() || !sharedStateMap.containsKey(dataKeyName)) {
            return null
        }
        return sharedStateMap[dataKeyName]
    }

    /**
     * Returns the value for the [key] provided as input by searching in the current [Event]'s data.
     *
     * @param key [String] containing the key whose value needs to be determined
     *
     * @return [Any] containing value to be substituted for the [key] from the [Event]'s data if [key] is present, null otherwise
     */
    private fun getValueFromEvent(key: String): Any? {
        if (event.eventData == null) {
            Log.debug(
                LaunchRulesEngineConstants.LOG_TAG,
                LOG_TAG,
                "Triggering event ${event.uniqueIdentifier} - Event data is null, unable to replace the token $key"
            )
            return EMPTY_STRING
        }
        val eventDataMap = event.eventData.flattening()
        return eventDataMap[key]
    }
}
