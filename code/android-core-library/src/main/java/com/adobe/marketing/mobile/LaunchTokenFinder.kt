package com.adobe.marketing.mobile

import java.security.SecureRandom

internal class LaunchTokenFinder(val event: Event, val module: Module, val platformServices: PlatformServices) {

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
     * Returns the value for the `key` provided as input.
     *
     *
     * If the `key` is a special key recognized by SDK, the value is determined based on incoming `Event`,
     * or `EventHub#moduleSharedStates` data. Otherwise the key is searched in the current `Event`'s data
     * and the corresponding value is returned.
     *
     * @param key `String` containing the key whose value needs to be determined
     *
     * @return `Object` containing value to be substituted for the `key`
     */
     fun get(key: String): Any? {
        if (StringUtils.isNullOrEmpty(key)) {
            return null
        }

        return when (key) {
            KEY_EVENT_TYPE -> event.getEventType().getName()
            KEY_EVENT_SOURCE -> event.getEventSource().getName()
            KEY_TIMESTAMP_UNIX -> TimeUtil.getUnixTimeInSeconds().toString()
            KEY_TIMESTAMP_ISO8601 -> TimeUtil.getIso8601Date()
            KEY_TIMESTAMP_PLATFORM -> TimeUtil.getIso8601DateTimeZoneISO8601()
            KEY_SDK_VERSION -> {
                MobileCore.extensionVersion()
            }
            KEY_CACHEBUST -> SecureRandom().nextInt(RANDOM_INT_BOUNDARY).toString()
            KEY_ALL_URL -> {
                if (event.getData() == null) {
                    Log.debug(LOG_TAG, "Triggering event data is null, can not use it to generate an url query string")
                    return EMPTY_STRING
                }
                val eventDataAsObjectMap = EventDataFlattener.getFlattenedDataMap(event.getData())
                UrlUtilities.serializeToQueryString(eventDataAsObjectMap)
            }
            KEY_ALL_JSON -> {
                if (event.getData() == null) {
                    Log.debug(LOG_TAG, "Triggering event data is null, can not use it to generate a json string")
                    return EMPTY_STRING
                }
                generateJsonString(event.getData())
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
     * Returns the value for shared state key specified by the `key`.
     *
     *
     * The key is provided in the format ~state.valid_shared_state_name/key
     * For example: ~state.com.adobe.marketing.mobile.Identity/mid
     *
     * @param key `String` containing the key to search for in `EventHub#moduleSharedStates`
     *
     * @return `Object` containing the value for the shared state key if valid, null otherwise
     */
    private fun getValueFromSharedState(key: String): Any? {
        val sharedStateKeyString = key.substring(KEY_SHARED_STATE.length)
        if (StringUtils.isNullOrEmpty(sharedStateKeyString)) {
            return null
        }
        val index = sharedStateKeyString.indexOf(SHARED_STATE_KEY_DELIMITER)
        if (index == -1) {
            return null
        }
        val sharedStateName = sharedStateKeyString.substring(0, index)
        val dataKeyName = sharedStateKeyString.substring(index + 1)
        val sharedStateMap = EventDataFlattener.getFlattenedDataMap(module.getSharedEventState(
                sharedStateName, event))
        if (sharedStateMap.isEmpty() || StringUtils.isNullOrEmpty(dataKeyName) || !sharedStateMap.containsKey(dataKeyName)) {
            return null
        }
        val variant = sharedStateMap[dataKeyName]
        return try {
            PermissiveVariantSerializer.DEFAULT_INSTANCE.deserialize(variant)
        } catch (ex: VariantException) {
            null
        }
    }

    /**
     * Returns the value for the `key` provided as input by searching in the current `Event`'s data.
     *
     * @param key `String` containing the key whose value needs to be determined
     *
     * @return `Object` containing value to be substituted for the `key` from the `Event`'s data
     */
    private fun getValueFromEvent(key: String): Any? {
        if (event.getData() == null) {
            Log.debug(LOG_TAG, String.format("Unable to replace the token %s, triggering event data is null", key))
            return EMPTY_STRING
        }
        val eventDataMap = EventDataFlattener.getFlattenedDataMap(event.getData())
        if (!eventDataMap.containsKey(key)) {
            return null
        }
        val value = eventDataMap[key]
        return if (value == null || value is NullVariant) {
            null
        } else try {
            PermissiveVariantSerializer.DEFAULT_INSTANCE.deserialize(value)
        } catch (ex: VariantException) {
            EMPTY_STRING
        }
    }

    /**
     * Returns the `EventData` in json format
     *
     * @param eventData `EventData` which needs to be encoded
     * @return `String` containing `event`'s data encoded in json format
     */
    private fun generateJsonString(eventData: EventData): String {
        val jsonUtilityService = platformServices.getJsonUtilityService()
                ?: return EMPTY_STRING
        val jsonObject = try {
            val dataMap = eventData.asMapCopy()
            val variant = Variant.fromVariantMap(dataMap)
            variant.getTypedObject(JsonObjectVariantSerializer(
                    jsonUtilityService))
        } catch (exception: Exception) {
            return EMPTY_STRING
        }
        return jsonObject?.toString() ?: EMPTY_STRING
    }
}