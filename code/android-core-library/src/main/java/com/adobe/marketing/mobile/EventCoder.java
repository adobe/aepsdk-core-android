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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The helper methods used to encode/decode an Event to/from json String
 */
public class EventCoder {

	private static final String NAME = "name";
	private static final String TYPE = "type";
	private static final String SOURCE = "source";
	private static final String UUID = "uuid";
	private static final String RESPONSE_ID = "responseId";
	private static final String TIMESTAMP = "timestamp";
	private static final String DATA = "data";

	/**
	 * Decode an event from json string
	 * @param eventString the json string
	 * @return the decoded event if the json is valid, otherwise null
	 */
	public static Event decode(final String eventString) {
		if (eventString == null) {
			return null;
		}

		try {
			JSONObject json = new JSONObject(eventString);

			String name = json.optString(NAME, null);
			String type = json.optString(TYPE, null);
			String source = json.optString(SOURCE, null);
			String uniqueIdentifier = json.optString(UUID, null);
			long timestamp = json.optLong(TIMESTAMP, 0);
			Map<String, Object> data = toMap(json.optJSONObject(DATA));
			String responseId = json.optString(RESPONSE_ID, null);

			Event ret = new Event.Builder(name, type, source)
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
			json.put(DATA, wrap(event.getEventData()));
			json.put(RESPONSE_ID, event.getResponseID());
		} catch (JSONException e) {
			return null;
		}

		return json.toString();
	}

	/**
	 * Copied from Android source code, this API is only available since API 19.
	 *
	 * Wraps the given object if necessary.
	 *
	 * <p>If the object is null or , returns {@code #NULL}.
	 * If the object is a {@code JSONArray} or {@code JSONObject}, no wrapping is necessary.
	 * If the object is {@code NULL}, no wrapping is necessary.
	 * If the object is an array or {@code Collection}, returns an equivalent {@code JSONArray}.
	 * If the object is a {@code Map}, returns an equivalent {@code JSONObject}.
	 * If the object is a primitive wrapper type or {@code String}, returns the object.
	 * Otherwise if the object is from a {@code java} package, returns the result of {@code toString}.
	 * If wrapping fails, returns null.
	 */
	private static Object wrap(Object o) {
		if (o == null) {
			return JSONObject.NULL;
		}

		if (o instanceof JSONArray || o instanceof JSONObject) {
			return o;
		}

		if (o.equals(JSONObject.NULL)) {
			return o;
		}

		try {
			if (o instanceof Collection) {
				return new JSONArray((Collection) o);
			} else if (o.getClass().isArray()) {
				return wrapArray(o);
			}

			if (o instanceof Map) {
				return new JSONObject((Map) o);
			}

			if (o instanceof Boolean ||
					o instanceof Byte ||
					o instanceof Character ||
					o instanceof Double ||
					o instanceof Float ||
					o instanceof Integer ||
					o instanceof Long ||
					o instanceof Short ||
					o instanceof String) {
				return o;
			}

			if (o.getClass().getPackage().getName().startsWith("java.")) {
				return o.toString();
			}
		} catch (Exception ignored) {
		}

		return null;
	}

	private static JSONArray wrapArray(Object array) throws JSONException {
		if (!array.getClass().isArray()) {
			throw new JSONException("Not a primitive array: " + array.getClass());
		}

		final JSONArray jsonArray = new JSONArray();
		final int length = Array.getLength(array);

		for (int i = 0; i < length; ++i) {
			jsonArray.put(wrap(Array.get(array, i)));
		}

		return jsonArray;
	}

	private static Map<String, Object> toMap(JSONObject object) throws JSONException {
		Map<String, Object> map = new HashMap<String, Object>();

		if (object == null) {
			return null;
		}

		Iterator<String> keys = object.keys();

		while (keys.hasNext()) {
			String key = (String) keys.next();
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
