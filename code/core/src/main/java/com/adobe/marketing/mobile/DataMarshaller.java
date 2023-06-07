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

package com.adobe.marketing.mobile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.adobe.marketing.mobile.internal.CoreConstants;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.ui.AndroidUIService;
import com.adobe.marketing.mobile.util.CloneFailedException;
import com.adobe.marketing.mobile.util.EventDataUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** The util class to marshal data from {@link Activity}. */
class DataMarshaller {

    private static final String TAG = DataMarshaller.class.getSimpleName();

    private static final String DEEPLINK_KEY = "deeplink";
    // legacy
    private static final String LEGACY_PUSH_MESSAGE_ID = "adb_m_id";
    // acquisition
    private static final String PUSH_MESSAGE_ID_KEY = "pushmessageid";
    private static final String LOCAL_NOTIFICATION_ID_KEY = "notificationid";

    private static final String ADOBE_QUERY_KEYS_PREVIEW_TOKEN = "at_preview_token";
    private static final String ADOBE_QUERY_KEYS_PREVIEW_URL = "at_preview_endpoint";
    private static final String ADOBE_QUERY_KEYS_DEEPLINK_ID = "a.deeplink.id";
    private static final List<String> ADOBE_QUERY_KEYS =
            Arrays.asList(
                    ADOBE_QUERY_KEYS_DEEPLINK_ID,
                    ADOBE_QUERY_KEYS_PREVIEW_TOKEN,
                    ADOBE_QUERY_KEYS_PREVIEW_URL);

    private DataMarshaller() {}

    /**
     * Marshal an {@code Activity} instance into a generic data map that is compatible with types
     * supported with event data. If the extras of the {@code Activity} contains types unsupported
     * by event data, the entire extras will be dropped from the returned map.
     *
     * @param activity Instance of an {@code Activity}.
     * @return a generic data map containing the marshalled data from the {@code Activity}.
     */
    static Map<String, Object> marshal(final Activity activity) {
        final Map<String, Object> launchData = new HashMap<>();
        if (activity == null) {
            return launchData;
        }

        if (activity.getIntent() == null) {
            return launchData;
        }

        Intent intent = activity.getIntent();
        final Map<String, Object> marshalledExtras = marshalIntentExtras(intent.getExtras());
        launchData.putAll(marshalledExtras);

        Uri data = intent.getData();

        if (data == null || data.toString().isEmpty()) {
            return launchData;
        }

        Log.trace(CoreConstants.LOG_TAG, TAG, "Receiving the Activity Uri (%s)", data.toString());
        launchData.put(DEEPLINK_KEY, data.toString());

        // This will remove the adobe specific keys from the intent data
        // This ensures that if this intent is marshaled again, we will not track
        // duplicate data
        if (containAdobeQueryKeys(data)) {
            Uri cleanDataUri = cleanUpUri(data);
            intent.setData(cleanDataUri);
        }

        return launchData;
    }

    /**
     * Check if the URI contains the {@link #ADOBE_QUERY_KEYS} keys
     *
     * @param data The {@link java.net.URI} to be verified.
     * @return true if the URI contains the Adobe specific query parameters
     */
    private static boolean containAdobeQueryKeys(final Uri data) {
        if (!data.isHierarchical()) {
            return false;
        }

        List<String> keys = new ArrayList<>(data.getQueryParameterNames());

        if (keys.isEmpty()) {
            return false;
        }

        keys.retainAll(ADOBE_QUERY_KEYS);
        return keys.size() != 0;
    }

    /**
     * Marshal an {@code Intent}'s {@code Bundle} of extras into a generic data map.
     *
     * @param extraBundle {@code Bundle} containing all the extras data
     */
    private static Map<String, Object> marshalIntentExtras(final Bundle extraBundle) {
        final Map<String, Object> extraData = new HashMap<>();
        if (extraBundle != null) {
            for (String key : extraBundle.keySet()) {
                String newKey = key;

                if (LEGACY_PUSH_MESSAGE_ID.equals(key)) {
                    newKey = PUSH_MESSAGE_ID_KEY;
                }

                if (AndroidUIService.NOTIFICATION_IDENTIFIER_KEY.equals(key)) {
                    newKey = LOCAL_NOTIFICATION_ID_KEY;
                }

                Object value = extraBundle.get(key);

                if (value != null && value.toString().length() > 0) {
                    extraData.put(newKey, value);
                }
            }

            extraBundle.remove(LEGACY_PUSH_MESSAGE_ID);
            extraBundle.remove(AndroidUIService.NOTIFICATION_IDENTIFIER_KEY);
        }

        // Verify if the extra data map can be cloned to be compatible with event data
        try {
            EventDataUtils.clone(extraData);
        } catch (CloneFailedException e) {
            Log.warning(CoreConstants.LOG_TAG, TAG, "Cannot intent extras as event data.");
            return Collections.EMPTY_MAP;
        }

        return extraData;
    }

    /**
     * Remove the {@link #ADOBE_QUERY_KEYS} keys from the URI if found.
     *
     * @param data The {@link java.net.URI} to be cleaned
     * @return The cleaned URI
     */
    private static Uri cleanUpUri(final Uri data) {
        if (!data.isHierarchical()) {
            return data;
        }

        try {
            Set<String> keys = data.getQueryParameterNames();

            if (keys == null || keys.isEmpty()) {
                return data;
            }

            Uri.Builder cleanUriBuilder = data.buildUpon();
            cleanUriBuilder.clearQuery();

            for (String key : keys) {
                if (!ADOBE_QUERY_KEYS.contains(key)) {
                    for (String value : data.getQueryParameters(key)) {
                        cleanUriBuilder.appendQueryParameter(key, value);
                    }
                }
            }

            return cleanUriBuilder.build();
        } catch (UnsupportedOperationException e) {
            // AMSDK-8863
            return data;
        }
    }
}
