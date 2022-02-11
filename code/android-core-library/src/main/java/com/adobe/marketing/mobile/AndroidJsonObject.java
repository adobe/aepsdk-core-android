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

import java.util.Iterator;

class AndroidJsonObject implements JsonUtilityService.JSONObject {

	private JSONObject jsonObject;

	AndroidJsonObject(JSONObject jsonObject) {
		this.jsonObject = jsonObject;
	}

	@Override
	public Object get(String name) throws JsonException {
		try {
			Object value = jsonObject.get(name);

			if (jsonObject.isNull(name)) {
				return null;
			}

			if (value instanceof JSONObject) {
				return new AndroidJsonObject((JSONObject) value);
			}

			if (value instanceof JSONArray) {
				return new AndroidJsonArray((JSONArray) value);
			}

			return jsonObject.get(name);
		} catch (JSONException e) {
			throw new JsonException(e);
		}
	}

	@Override
	public JsonUtilityService.JSONObject getJSONObject(String name) throws JsonException {
		try {
			return new AndroidJsonObject(jsonObject.getJSONObject(name));
		} catch (JSONException e) {
			throw new JsonException(e);
		}
	}

	@Override
	public JsonUtilityService.JSONArray getJSONArray(String name) throws JsonException {
		try {
			return new AndroidJsonArray(jsonObject.getJSONArray(name));
		} catch (JSONException e) {
			throw new JsonException(e);
		}
	}

	@Override
	public int getInt(String name) throws JsonException {
		try {
			return jsonObject.getInt(name);
		} catch (JSONException e) {
			throw new JsonException(e);
		}
	}

	@Override
	public long getLong(String name) throws JsonException {
		try {
			return jsonObject.getLong(name);
		} catch (JSONException e) {
			throw new JsonException(e);
		}
	}

	@Override
	public double getDouble(String name) throws JsonException {
		try {
			return jsonObject.getDouble(name);
		} catch (JSONException e) {
			throw new JsonException(e);
		}
	}

	@Override
	public String getString(String name) throws JsonException {
		try {
			return jsonObject.getString(name);
		} catch (JSONException e) {
			throw new JsonException(e);
		}
	}

	@Override
	public boolean getBoolean(String name) throws JsonException {
		try {
			return jsonObject.getBoolean(name);
		} catch (JSONException e) {
			throw new JsonException(e);
		}
	}

	@Override
	public Object opt(String name) {
		return jsonObject.opt(name);
	}

	@Override
	public JsonUtilityService.JSONObject put(String name, Object object) throws JsonException {
		try {
			if (object instanceof JsonUtilityService.JSONObject) {
				jsonObject.put(name, new JSONObject(object.toString()));
			} else if (object instanceof JsonUtilityService.JSONArray) {
				jsonObject.put(name, new JSONArray(object.toString()));
			} else {
				jsonObject.put(name, object);
			}
		} catch (JSONException e) {
			throw new JsonException(e);
		}

		return this;
	}

	@Override
	public JsonUtilityService.JSONObject put(String name, JsonUtilityService.JSONObject jsonObject) throws JsonException {
		if (jsonObject == null) {
			return put(name, (Object) null);
		}

		try {
			return put(name, new JSONObject(jsonObject.toString()));
		} catch (JSONException e) {
			throw new JsonException(e);
		}
	}

	@Override
	public JsonUtilityService.JSONObject put(String name, JsonUtilityService.JSONArray jsonArray) throws JsonException {
		if (jsonArray == null) {
			return put(name, (Object) null);
		}

		try {
			return put(name, new JSONArray(jsonArray.toString()));
		} catch (JSONException e) {
			throw new JsonException(e);
		}
	}

	@Override
	public JsonUtilityService.JSONObject put(String name, int value) throws JsonException {
		try {
			jsonObject.put(name, value);
			return this;
		} catch (JSONException e) {
			throw new JsonException(e);
		}
	}

	@Override
	public JsonUtilityService.JSONObject put(String name, long value) throws JsonException {
		try {
			jsonObject.put(name, value);
			return this;
		} catch (JSONException e) {
			throw new JsonException(e);
		}
	}

	@Override
	public JsonUtilityService.JSONObject put(String name, double value) throws JsonException {
		try {
			jsonObject.put(name, value);
			return this;
		} catch (JSONException e) {
			throw new JsonException(e);
		}
	}

	@Override
	public JsonUtilityService.JSONObject put(String name, String value) throws JsonException {
		try {
			jsonObject.put(name, value);
			return this;
		} catch (JSONException e) {
			throw new JsonException(e);
		}
	}

	@Override
	public JsonUtilityService.JSONObject put(String name, boolean value) throws JsonException {
		try {
			jsonObject.put(name, value);
			return this;
		} catch (JSONException e) {
			throw new JsonException(e);
		}
	}

	@Override
	public JsonUtilityService.JSONObject optJSONObject(String name) {
		JSONObject androidJsonObj = jsonObject.optJSONObject(name);

		if (androidJsonObj != null) {
			return new AndroidJsonObject(androidJsonObj);
		} else {
			return null;
		}
	}

	@Override
	public JsonUtilityService.JSONArray optJSONArray(String name) {
		JSONArray androidJsonArr = jsonObject.optJSONArray(name);

		if (androidJsonArr != null) {
			return new AndroidJsonArray(androidJsonArr);
		} else {
			return null;
		}
	}

	@Override
	public int optInt(String name, int defaultValue) {
		return jsonObject.optInt(name, defaultValue);
	}

	@Override
	public long optLong(String name, long defaultValue) {
		return jsonObject.optLong(name, defaultValue);
	}

	@Override
	public double optDouble(String name, double defaultValue) {
		return jsonObject.optDouble(name, defaultValue);
	}

	@Override
	public String optString(String name, String defaultValue) {
		return jsonObject.optString(name, defaultValue);
	}

	@Override
	public boolean optBoolean(String name, boolean defaultValue) {
		return jsonObject.optBoolean(name, defaultValue);
	}

	@Override
	public Iterator<String> keys() {
		return jsonObject.keys();
	}

	@Override
	public int length() {
		return jsonObject.length();
	}

	@Override
	public String toString() {
		return jsonObject.toString();
	}

	@Override
	public void remove(String name) {
		jsonObject.remove(name);
	}
}
