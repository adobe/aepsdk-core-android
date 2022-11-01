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

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The helper methods used to encode/decode an Event to/from json String
 */
public class EventCoder {

    private static final String NAME = "name";
    private static final String UUID = "uuid";
    private static final String SOURCE = "source";
    private static final String TYPE = "type";
    private static final String DATA = "data";
    private static final String TIMESTAMP = "timestamp";
    private static final String RESPONSE_ID = "responseId";
    private static final String MASK = "mask";

    /**
     * Decode an event from json string
     *
     * @param eventString the json string
     * @return the decoded event if the json is valid, otherwise null
     */
    public static Event decode(final String eventString) {
        if (eventString == null) {
            return null;
        }

        try {
            final JSONObject json = new JSONObject(eventString);
            final String name = optString(json, NAME, null);
            final String uniqueIdentifier = optString(json, UUID, null);
            final String source = optString(json, SOURCE, null);
            final String type = optString(json, TYPE, null);
            final Map<String, Object> data = toMap(json.optJSONObject(DATA));
            final long timestamp = json.optLong(TIMESTAMP, 0);
            final String responseId = optString(json, RESPONSE_ID, null);
            final JSONArray maskJsonArray = json.optJSONArray(MASK);

            String[] mask = null;
            if (maskJsonArray != null) {
                mask = toList(maskJsonArray).toArray(new String[0]);
            }

            final Event ret = new Event.Builder(name, type, source, mask)
                    .setUniqueIdentifier(uniqueIdentifier)
                    .setTimestamp(timestamp)
                    .setEventData(data)
                    .setResponseId(responseId)
                    .build();
            return ret;
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * Encode an event to a json string
     *
     * @param event the event to encode
     * @return json string represents all the fields of the event, otherwise returns null if the event is null or there is json error
     */
    public static String encode(final Event event) {
        if (event == null) {
            return null;
        }

        JSONObject json = new JSONObject();
        try {
            json.put(NAME, event.getName());
            json.put(TYPE, event.getType());
            json.put(SOURCE, event.getSource());
            json.put(UUID, event.getUniqueIdentifier());
            json.put(TIMESTAMP, event.getTimestamp());
            json.put(DATA, JSONObject.wrap(event.getEventData()));
            json.put(RESPONSE_ID, event.getResponseID());
            json.put(MASK, JSONObject.wrap(event.getMask()));
        } catch (JSONException e) {
            return null;
        }

        return json.toString();
    }

    /**
     * Returns the value mapped by {@code key} if it exists, or the {@code fallback}
     * if no such mapping exists.
     * Exists because {@code JSONObject#optString} does not allow a null fallback.
     *
     * @param jsonObject the json from which the key is to be fetched
     * @param key the key that is to be fetched
     * @param fallback the fallback value if the key does not exist in {@code jsonObject}
     *
     * @return value mapped by {@code key} if it exists in {@code jsonObject}, or {@code fallback}
     * if no such mapping exists.
     */
    private static String optString(final JSONObject jsonObject, final String key, @Nullable final String fallback) {
        try {
            return jsonObject.getString(key);
        } catch (final JSONException e) {
            return fallback;
        }
    }

    // TODO: Methods below can be removed in favor of a json utility

    private static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<>();

        if (object == null) {
            return null;
        }

        Iterator<String> keys = object.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            map.put(key, fromJson(object.get(key)));
        }

        return map;
    }

    private static Object fromJson(Object json) throws JSONException {
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

    private static List<Object> toList(JSONArray array) throws JSONException {
        final List<Object> list = new ArrayList<Object>();

        for (int i = 0; i < array.length(); i++) {
            list.add(fromJson(array.get(i)));
        }

        return list;
    }
}
