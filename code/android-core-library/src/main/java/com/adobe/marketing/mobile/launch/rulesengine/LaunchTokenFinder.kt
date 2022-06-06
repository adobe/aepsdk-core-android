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
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.internal.utility.StringUtils
import com.adobe.marketing.mobile.internal.utility.TimeUtil
import com.adobe.marketing.mobile.internal.utility.flattening
import com.adobe.marketing.mobile.internal.utility.serializeToQueryString
import com.adobe.marketing.mobile.rulesengine.TokenFinder
import java.security.SecureRandom
import org.json.JSONObject

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
            KEY_TIMESTAMP_UNIX -> TimeUtil.getUnixTimeInSeconds().toString()
            KEY_TIMESTAMP_ISO8601 -> TimeUtil.getIso8601Date()
            KEY_TIMESTAMP_PLATFORM -> TimeUtil.getIso8601DateTimeZoneISO8601()
            KEY_SDK_VERSION -> MobileCore.extensionVersion()
            KEY_CACHEBUST -> SecureRandom().nextInt(RANDOM_INT_BOUNDARY).toString()
            KEY_ALL_URL -> {
                if (event.eventData == null) {
                    MobileCore.log(
                        LoggingMode.DEBUG,
                        LOG_TAG,
                        "Triggering event data is null, can not use it to generate an url query string"
                    )
                    return EMPTY_STRING
                }
                val eventDataAsObjectMap = event.eventData.flattening()
                eventDataAsObjectMap.serializeToQueryString()
            }
            KEY_ALL_JSON -> {
                if (event.eventData == null) {
                    MobileCore.log(LoggingMode.DEBUG,
                            LOG_TAG,
                        "Triggering event data is null, can not use it to generate a json string"
                    )
                    return EMPTY_STRING
                }
                try {
                    JSONObject(event.eventData).toString()
                } catch (e: Exception) {
                    MobileCore.log(LoggingMode.DEBUG,
                        LOG_TAG,
                        "Failed to generate a json string ${e.message}"
                    )
                    return EMPTY_STRING
                }
            }
            else -> {
                if (key.startsWith(KEY_SHARED_STATE)) {
                    getValueFromSharedState(key)
                } else getValueFromEvent(key)
            }
        }
    }

    // ========================================================
    // private getter methods
    // ========================================================

    /**
     * Returns the value for shared state key specified by the [key].
     *
     *
     * The [key] is provided in the format ~state.valid_shared_state_name/key
     * For example: ~state.com.adobe.marketing.mobile.Identity/mid
     *
     * @param key [String] containing the key to search for in `EventHub#moduleSharedStates`
     *
     * @return [Any] containing the value for the shared state key if valid, null otherwise
     */
    private fun getValueFromSharedState(key: String): Any? {
        val sharedStateKeyString = key.substring(KEY_SHARED_STATE.length)
        if (StringUtils.isNullOrEmpty(sharedStateKeyString)) {
            return null
        }
        if (!sharedStateKeyString.contains(SHARED_STATE_KEY_DELIMITER)) {
            return null
        }
        val (sharedStateName, dataKeyName) = sharedStateKeyString.split(SHARED_STATE_KEY_DELIMITER)
        // TODO change once map flattening logic is finalized
        val sharedStateMap = extensionApi.getSharedEventState(sharedStateName, event) {
            MobileCore.log(LoggingMode.DEBUG,
                LOG_TAG,
                String.format("Unable to replace the token %s, token not found in shared state for the event", key)
            )
        }?.flattening()
        if (sharedStateMap == null || sharedStateMap.isEmpty() || StringUtils.isNullOrEmpty(dataKeyName) || !sharedStateMap.containsKey(dataKeyName)) {
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
            MobileCore.log(
                LoggingMode.DEBUG,
                LOG_TAG,
                String.format("Unable to replace the token %s, triggering event data is null", key)
            )
            return EMPTY_STRING
        }
        // TODO uncomment once map flattening logic is finalized
        val eventDataMap = event.eventData.flattening()
        return eventDataMap[key]
        return EMPTY_STRING
    }
}
