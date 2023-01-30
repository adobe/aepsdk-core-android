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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** The util class to marshal data from {@link Activity}. */
class DataMarshaller {

    private static final String TAG = DataMarshaller.class.getSimpleName();
    static final String DEEPLINK_KEY = "deeplink";

    // legacy
    static final String LEGACY_PUSH_MESSAGE_ID = "adb_m_id";
    // acquisition
    static final String PUSH_MESSAGE_ID_KEY = "pushmessageid";
    static final String LOCAL_NOTIFICATION_ID_KEY = "notificationid";

    private final Map<String, Object> launchData = new HashMap<>();
    private final List<String> adobeQueryKeys = new ArrayList<>();

    private static final String ADOBE_QUERY_KEYS_PREVIEW_TOKEN = "at_preview_token";
    private static final String ADOBE_QUERY_KEYS_PREVIEW_URL = "at_preview_endpoint";
    private static final String ADOBE_QUERY_KEYS_DEEPLINK_ID = "a.deeplink.id";

    /** Constructor. */
    DataMarshaller() {
        adobeQueryKeys.add(ADOBE_QUERY_KEYS_DEEPLINK_ID);
        adobeQueryKeys.add(ADOBE_QUERY_KEYS_PREVIEW_TOKEN);
        adobeQueryKeys.add(ADOBE_QUERY_KEYS_PREVIEW_URL);
    }

    /**
     * Marshal an {@code Activity} instance into a generic data map.
     *
     * @param activity Instance of an {@code Activity}.
     * @return An instance of this same marshaller, to help with chaining calls.
     */
    DataMarshaller marshal(final Activity activity) {
        do {
            if (activity == null) {
                break;
            }

            if (activity.getIntent() == null) {
                break;
            }

            Intent intent = activity.getIntent();
            marshalIntentExtras(intent.getExtras());
            Uri data = intent.getData();

            if (data == null || data.toString().isEmpty()) {
                break;
            }

            Log.trace(
                    CoreConstants.LOG_TAG, TAG, "Receiving the Activity Uri (%s)", data.toString());
            launchData.put(DEEPLINK_KEY, data.toString());

            // This will remove the adobe specific keys from the intent data
            // This ensures that if this intent is marshaled again, we will not track
            // duplicate data
            if (containAdobeQueryKeys(data)) {
                Uri cleanDataUri = cleanUpUri(data);
                intent.setData(cleanDataUri);
            }
        } while (false);

        return this;
    }

    /**
     * Check if the URI contains the {@link #adobeQueryKeys} keys
     *
     * @param data The {@link java.net.URI} to be verified.
     * @return true if the URI contains the Adobe specific query parameters
     */
    private boolean containAdobeQueryKeys(final Uri data) {
        if (!data.isHierarchical()) {
            return false;
        }

        List<String> keys = new ArrayList<>(data.getQueryParameterNames());

        if (keys.isEmpty()) {
            return false;
        }

        keys.retainAll(adobeQueryKeys);
        return keys.size() != 0;
    }

    /**
     * Get the data map created by the marshaller instance.
     *
     * @return map of marshalled data
     */
    Map<String, Object> getData() {
        return launchData;
    }

    /**
     * Marshal an {@code Intent}'s {@code Bundle} of extras into a generic data map.
     *
     * @param extraBundle {@code Bundle} containing all the extras data
     */
    private void marshalIntentExtras(final Bundle extraBundle) {
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
                    this.launchData.put(newKey, value);
                }
            }

            extraBundle.remove(LEGACY_PUSH_MESSAGE_ID);
            extraBundle.remove(AndroidUIService.NOTIFICATION_IDENTIFIER_KEY);
        }
    }

    /**
     * Remove the {@link #adobeQueryKeys} keys from the URI if found.
     *
     * @param data The {@link java.net.URI} to be cleaned
     * @return The cleaned URI
     */
    private Uri cleanUpUri(final Uri data) {
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
                if (!adobeQueryKeys.contains(key)) {
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
