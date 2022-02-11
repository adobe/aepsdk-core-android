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


import org.json.JSONException;

import android.os.Build;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class AndroidJsonUtility implements JsonUtilityService {
	private static final String LOG_TAG = AndroidJsonUtility.class.getSimpleName();

	@Override
	public JSONObject createJSONObject(String json) {
		if (json == null) {
			return null;
		}

		try {
			org.json.JSONObject jsonObject = new org.json.JSONObject(json);
			return new AndroidJsonObject(jsonObject);
		} catch (JSONException ex) {
			return null;
		}
	}

	@Override
	public JSONObject createJSONObject(Map map) {
		if (map == null) {
			return null;
		}

		if (Build.VERSION.SDK_INT <= 18) {
			//This is because API 18 and below, the platform behaves differently w.r.t converting
			//native java containers (Maps, Arrays, Collections) into Json types. Therefore, we are doing it
			//for ourselves before we create the JsonObject. This keeps the behavior consistent between versions
			//of the platform.
			return new AndroidJsonObject(createNativeJsonObject(map));
		} else {
			//bypass all the conversion magic
			return new AndroidJsonObject(new org.json.JSONObject(map));
		}
	}

	@Override
	public JSONArray createJSONArray(String json) {
		if (json == null) {
			return null;
		}

		try {
			org.json.JSONArray jsonArray = new org.json.JSONArray(json);
			return new AndroidJsonArray(jsonArray);
		} catch (JSONException ex) {
			return null;
		}
	}

	@Override
	public Map<String, String> mapFromJsonObject(JSONObject jsonData) {
		if (jsonData == null) {
			return null;
		}

		Iterator<String> keyItr = jsonData.keys();
		Map<String, String> map = new HashMap<String, String>();

		while (keyItr.hasNext()) {
			String name = keyItr.next();

			try {
				map.put(name, jsonData.getString(name));
			} catch (JsonException e) {
				Log.warning(LOG_TAG, "Unable to convert jsonObject key %s into map, %s", name, e);
			}
		}

		return map;
	}

	/**
	 * Returns a {@code org.json.JSONObject} instance from a map, converting the java native containers into Json types if required.
	 *
	 * @param map The {@link Map} to be converted into {@link org.json.JSONObject}
	 * @return The converted {@code JsonObject}. Will be null if the {@code map} is null.
	 */
	private org.json.JSONObject createNativeJsonObject(Map<?, ?> map) {
		if (map == null) {
			return null;
		}

		Map<String, Object> convertedMap = new HashMap<String, Object>();

		for (Map.Entry<?, ?> entry : map.entrySet()) {
			Object o = entry.getValue();

			if (entry.getKey() != null) {
				convertedMap.put(entry.getKey().toString(), wrap(o));
			}
		}

		return new org.json.JSONObject(convertedMap);
	}

	/**
	 * Returns a {@code org.json.JSONArray} instance from an array, converting the java native containers into Json types if required.
	 *
	 * @param array The {@code array} to be converted into {@link org.json.JSONArray}
	 * @return The converted {@code JSONArray}. Will be null if the {@code array} is null or the {@code array} is not a java array.
	 */
	private org.json.JSONArray createNativeJsonArray(Object array) {
		if (array == null) {
			return null;
		}

		if (!array.getClass().isArray()) {
			return null;
		}

		org.json.JSONArray jsonArray = new org.json.JSONArray();
		final int length = Array.getLength(array);

		for (int i = 0; i < length; ++i) {
			jsonArray.put(wrap(Array.get(array, i)));
		}

		return jsonArray;

	}


	/**
	 * Returns a supported Json type converting the Object {@code o} into Json containers if required.
	 *
	 * <p>
	 *
	 * If {@code o} is a {@link Collection} or {@code array} then a {@link org.json.JSONArray} will be returned.
	 * If the {@code o} is a {@link Map} then a {@link org.json.JSONObject} will be returned. Otherwise, the object will
	 * be returned as is.
	 *
	 * @param o The Object to be wrapped
	 * @return The wrapped object
	 */
	private Object wrap(Object o) {
		if (o == null ||
				o instanceof JSONArray ||
				o instanceof JSONObject ||
				o instanceof Boolean ||
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

		if (o instanceof Collection) {
			return new org.json.JSONArray((Collection) o);
		}

		if (o.getClass().isArray()) {
			return createNativeJsonArray(o);
		}

		if (o instanceof Map) {
			return createNativeJsonObject((Map)o);
		}

		return null;
	}

}
