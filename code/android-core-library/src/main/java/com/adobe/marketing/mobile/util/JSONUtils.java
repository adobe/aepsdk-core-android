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

package com.adobe.marketing.mobile.util;

import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Utility class for transforming json objects. */
public final class JSONUtils {

    private JSONUtils() {}

    /**
     * Converts contents of a {@code JSONObject} into a {@code Map<String, Object>}
     *
     * @param jsonObject the {@code JSONObject} that is to be converted to Map
     * @return {@code Map<String, Object>} obtained after converting the {@code JSONObject}
     */
    @Nullable public static Map<String, Object> toMap(@Nullable final JSONObject jsonObject)
            throws JSONException {
        if (jsonObject == null) {
            return null;
        }

        final Map<String, Object> map = new HashMap<>();

        Iterator<String> keys = jsonObject.keys();

        while (keys.hasNext()) {
            final String key = keys.next();
            map.put(key, fromJson(jsonObject.get(key)));
        }

        return map;
    }

    /**
     * Converts contents of a {@code JSONObject} into a {@code List<Object>}
     *
     * @param jsonArray the {@code JSONArray} that is to be converted to a List
     * @return {@code List<Object>} obtained after converting the {@code JSONObject}
     */
    @Nullable public static List<Object> toList(@Nullable final JSONArray jsonArray) throws JSONException {
        if (jsonArray == null) {
            return null;
        }

        final List<Object> list = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(fromJson(jsonArray.get(i)));
        }

        return list;
    }

    private static Object fromJson(final Object json) throws JSONException {
        if (json == null || json == JSONObject.NULL) {
            return null;
        } else if (json instanceof JSONObject) {
            return toMap((JSONObject) json);
        } else if (json instanceof JSONArray) {
            return toList((JSONArray) json);
        } else {
            return json;
        }
    }
}
