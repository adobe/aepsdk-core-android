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
import android.net.Uri
import android.os.Bundle
import com.adobe.marketing.mobile.services.Log

/** The util class to marshal data from [Activity].  */
internal object DataMarshaller {

    private const val LOG_TAG = "DataMarshaller"

    private const val DEEPLINK_KEY = "deeplink"
    private const val LEGACY_PUSH_MESSAGE_ID = "adb_m_id"
    private const val PUSH_MESSAGE_ID_KEY = "pushmessageid"
    private const val NOTIFICATION_IDENTIFIER_KEY = "NOTIFICATION_IDENTIFIER"
    private const val LOCAL_NOTIFICATION_ID_KEY = "notificationid"

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
    fun marshal(activity: Activity?): Map<String, Any>? {
        activity ?: return null
        val intent = activity.intent ?: return null

        val ret = marshalIntentExtras(intent.extras)
        val data = intent.data
        if (data == null || data.toString().isEmpty()) {
            return ret
        }
        Log.trace(CoreConstants.LOG_TAG, LOG_TAG, "Receiving the Activity Uri $data")
        ret[DEEPLINK_KEY] = data.toString()

        // This will remove the adobe specific keys from the intent data
        // This ensures that if this intent is marshaled again, we will not track
        // duplicate data
        if (data.containAdobeQueryKeys) {
            intent.data = data.cleanAdobeQueryKeys
        }
        return ret
    }

    /**
     * Marshal an Intent's [Bundle] of extras into a generic data map.
     *
     * @param extraBundle [Bundle] containing all the extras data
     */
    private fun marshalIntentExtras(extraBundle: Bundle?): MutableMap<String, Any> {
        var ret = mutableMapOf<String, Any>()

        extraBundle ?: return ret
        for (key in extraBundle.keySet()) {
            val newKey = when (key) {
                LEGACY_PUSH_MESSAGE_ID -> PUSH_MESSAGE_ID_KEY
                NOTIFICATION_IDENTIFIER_KEY -> LOCAL_NOTIFICATION_ID_KEY
                else -> key
            }

            val value = extraBundle[key]
            if (value?.toString()?.isNotEmpty() == true) {
                ret[newKey] = value
            }
        }
        extraBundle.remove(LEGACY_PUSH_MESSAGE_ID)
        extraBundle.remove(NOTIFICATION_IDENTIFIER_KEY)
        return ret
    }

    /**
     * Check if the URI contains the [adobeQueryKeys] keys
     *
     * @return true if the URI contains the Adobe specific query parameters
     */
    private val Uri.containAdobeQueryKeys: Boolean
        get() {
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
    private val Uri.cleanAdobeQueryKeys: Uri
        get() {
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
