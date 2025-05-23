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

package com.adobe.marketing.mobile.internal

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.adobe.marketing.mobile.services.Log

/** The util class to marshal data from [Activity].  */
internal object DataMarshaller {

    private const val LOG_TAG = "DataMarshaller"

    private const val DEEPLINK_KEY = "deeplink"

    private const val LEGACY_PUSH_MESSAGE_ID = "adb_m_id"
    private const val PUSH_MESSAGE_ID_KEY = "pushmessageid"
    private const val NOTIFICATION_IDENTIFIER_KEY = "NOTIFICATION_IDENTIFIER"
    private const val LOCAL_NOTIFICATION_ID_KEY = "notificationid"
    /**
     * Map of known keys that need to be transformed during marshalling.
     * Key: original key in the bundle, Value: transformed key for marshalled data
     */
    private val KNOWN_KEYS_MAP = mapOf(
        LEGACY_PUSH_MESSAGE_ID to PUSH_MESSAGE_ID_KEY,
        NOTIFICATION_IDENTIFIER_KEY to LOCAL_NOTIFICATION_ID_KEY
    )

    private const val ADOBE_QUERY_KEYS_PREVIEW_TOKEN = "at_preview_token"
    private const val ADOBE_QUERY_KEYS_PREVIEW_URL = "at_preview_endpoint"
    private const val ADOBE_QUERY_KEYS_DEEPLINK_ID = "a.deeplink.id"

    private val adobeQueryKeys = arrayListOf(
        ADOBE_QUERY_KEYS_DEEPLINK_ID,
        ADOBE_QUERY_KEYS_PREVIEW_TOKEN,
        ADOBE_QUERY_KEYS_PREVIEW_URL
    )

    /**
     * Marshal an [Activity] instance and returns a data map
     *
     * @param activity Instance of an [Activity].
     * @return map of marshalled data
     */
    @JvmStatic
    fun marshal(activity: Activity?): Map<String, Any>? {
        activity ?: return null
        val intent = activity.intent ?: return null

        val ret = mutableMapOf<String, Any>()
        marshalIntentExtras(intent, ret)
        marshalIntentData(intent, ret)
        return ret
    }

    /**
     * Marshal an Intent's [Bundle] of extras into a generic data map.
     * Remove adobe specific keys from the bundle.
     *
     * @param intent [Intent]
     * @param marshalledData [Map] to add the marshalled data
     */
    @VisibleForTesting
    internal fun marshalIntentExtras(intent: Intent, marshalledData: MutableMap<String, Any>) {
        val extraBundle = intent.extras ?: return

        // First, handle known keys directly to avoid potential exceptions from keySet()
        KNOWN_KEYS_MAP.forEach { (originalKey, transformedKey) ->
            processAndRemoveKnownKey(extraBundle, originalKey, transformedKey, marshalledData)
        }

        // Then extract other keys
        try {
            extraBundle.keySet()?.forEach { key ->
                try {
                    if (KNOWN_KEYS_MAP.containsKey(key)) {
                        // returning from a lambda expression, the loop will continue.
                        return@forEach
                    }
                    val value = extraBundle[key]
                    if (value?.toString()?.isNotEmpty() == true) {
                        marshalledData[key] = value
                    }
                } catch (e: Exception) {
                    Log.error(CoreConstants.LOG_TAG, LOG_TAG, "Failed to retrieve data (key = $key) from Activity, error is: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.error(CoreConstants.LOG_TAG, LOG_TAG, "Failed to retrieve data from Activity, error is: ${e.message}")
        }
    }

    /**
     * Processes a known key from the bundle by reading its value and adding it to the marshalled data,
     * then removes the original key from the bundle to prevent duplicate processing.
     *
     * @param bundle [Bundle] containing the extras
     * @param originalKey original key in the bundle to read and remove
     * @param transformedKey key to use in the marshalled data
     * @param marshalledData [MutableMap] to add the marshalled data
     */
    private fun processAndRemoveKnownKey(
        bundle: Bundle,
        originalKey: String,
        transformedKey: String,
        marshalledData: MutableMap<String, Any>
    ) {
        try {
            bundle.getString(originalKey)?.takeIf { it.isNotEmpty() }?.let {
                marshalledData[transformedKey] = it
            }
        } catch (e: Exception) {
            Log.error(CoreConstants.LOG_TAG, LOG_TAG, "Failed to retrieve data (key = $originalKey) from Activity, error is: ${e.message}")
        }

        try {
            bundle.remove(originalKey)
        } catch (e: Exception) {
            Log.error(CoreConstants.LOG_TAG, LOG_TAG, "Failed to remove known key ($originalKey) from bundle, error is: ${e.message}")
        }
    }

    /**
     * Marshal an Intent's data uri into a generic data map.
     * This function also removes adobe specific keys from the map.
     *
     * @param intent [Intent]
     * @param marshalledData [Map] to add the marshalled data
     */
    private fun marshalIntentData(intent: Intent, marshalledData: MutableMap<String, Any>) {
        val data = intent.data ?: return
        if (data.toString().isEmpty()) {
            return
        }

        Log.trace(CoreConstants.LOG_TAG, LOG_TAG, "Receiving the Activity Uri $data")
        marshalledData[DEEPLINK_KEY] = data.toString()

        // This will remove the adobe specific keys from the intent data
        // This ensures that if this intent is marshaled again, we will not track
        // duplicate data
        if (data.containAdobeQueryKeys()) {
            intent.data = data.cleanAdobeQueryKeys()
        }
    }

    /**
     * Check if the URI contains the [adobeQueryKeys] keys
     *
     * @return true if the URI contains the Adobe specific query parameters
     */
    private fun Uri.containAdobeQueryKeys(): Boolean {
        if (!isHierarchical) {
            return false
        }

        val queryParams = queryParameterNames ?: return false
        return adobeQueryKeys.any {
            queryParams.contains(it)
        }
    }

    /**
     * Remove the [adobeQueryKeys] query params from the URI if found.
     *
     * @return The cleaned URI
     */
    private fun Uri.cleanAdobeQueryKeys(): Uri {
        if (!isHierarchical) {
            return this
        }

        try {
            val queryParamsNames = this.queryParameterNames ?: return this
            if (queryParamsNames.isEmpty()) {
                return this
            }

            val cleanUriBuilder = buildUpon()
            cleanUriBuilder.clearQuery()
            for (key in queryParamsNames) {
                if (!adobeQueryKeys.contains(key)) {
                    getQueryParameters(key)?.forEach {
                        cleanUriBuilder.appendQueryParameter(key, it)
                    }
                }
            }
            return cleanUriBuilder.build()
        } catch (e: UnsupportedOperationException) {
            // AMSDK-8863
            return this
        }
    }
}
