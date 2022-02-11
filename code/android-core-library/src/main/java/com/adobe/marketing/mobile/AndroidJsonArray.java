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

class AndroidJsonArray implements JsonUtilityService.JSONArray {

	private JSONArray jsonArray;

	AndroidJsonArray(JSONArray jsonArray) {
		this.jsonArray = jsonArray;
	}

	@Override
	public Object get(int index) throws JsonException {
		try {
			Object value = jsonArray.get(index);

			if (jsonArray.isNull(index)) {
				return null;
			}

			if (value instanceof JSONObject) {
				return new AndroidJsonObject((JSONObject) value);
			}

			if (value instanceof JSONArray) {
				return new AndroidJsonArray((JSONArray) value);
			}

			return jsonArray.get(index);
		} catch (JSONException e) {
			throw new JsonException(e);
		}
	}

	@Override
	public JsonUtilityService.JSONArray put(Object object) throws JsonException {
		jsonArray.put(object);
		return this;
	}

	@Override
	public JsonUtilityService.JSONArray put(int index, Object object) throws JsonException {
		try {

			if (object instanceof JsonUtilityService.JSONObject) {
				jsonArray.put(index, new JSONObject(object.toString()));
			} else if (object instanceof JsonUtilityService.JSONArray) {
				jsonArray.put(index, new JSONArray(object.toString()));
			} else {
				jsonArray.put(index, object);
			}
		} catch (Exception e) {
			throw new JsonException(e);
		}

		return this;
	}

	@Override
	public JsonUtilityService.JSONArray put(JsonUtilityService.JSONObject jsonObject) throws JsonException {
		return put(jsonArray.length(), jsonObject);
	}

	@Override
	public JsonUtilityService.JSONArray put(int index, JsonUtilityService.JSONObject jsonObject) throws JsonException {
		if (jsonObject == null) {
			return put(index, (Object) null);
		}

		try {
			return put(index, new JSONObject(jsonObject.toString()));
		} catch (JSONException e) {
			throw new JsonException(e);
		}
	}

	@Override
	public JsonUtilityService.JSONArray put(JsonUtilityService.JSONArray jsonArray) throws JsonException {
		return put(this.jsonArray.length(), jsonArray);
	}

	@Override
	public JsonUtilityService.JSONArray put(int index, JsonUtilityService.JSONArray jsonArray) throws JsonException {
		if (jsonArray == null) {
			return put(index, (Object) null);
		}

		try {
			return put(index, new JSONArray(jsonArray.toString()));
		} catch (JSONException e) {
			throw new JsonException(e);
		}
	}

	@Override
	public JsonUtilityService.JSONArray put(int value) throws JsonException {
		return put(jsonArray.length(), value);
	}

	@Override
	public JsonUtilityService.JSONArray put(int index, int value) throws JsonException {
		try {
			jsonArray.put(index, value);
		} catch (Exception e) {
			throw new JsonException(e);
		}

		return this;
	}

	@Override
	public JsonUtilityService.JSONArray put(long value) throws JsonException {
		return put(jsonArray.length(), value);
	}

	@Override
	public JsonUtilityService.JSONArray put(int index, long value) throws JsonException {
		try {
			jsonArray.put(index, value);
		} catch (Exception e) {
			throw new JsonException(e);
		}

		return this;
	}

	@Override
	public JsonUtilityService.JSONArray put(double value) throws JsonException {
		return put(jsonArray.length(), value);
	}

	@Override
	public JsonUtilityService.JSONArray put(int index, double value) throws JsonException {
		try {
			jsonArray.put(index, value);
		} catch (Exception e) {
			throw new JsonException(e);
		}

		return this;
	}

	@Override
	public JsonUtilityService.JSONArray put(String value) throws JsonException {
		return put(jsonArray.length(), value);
	}

	@Override
	public JsonUtilityService.JSONArray put(int index, String value) throws JsonException {
		try {
			jsonArray.put(index, value);
		} catch (Exception e) {
			throw new JsonException(e);
		}

		return this;
	}

	@Override
	public JsonUtilityService.JSONArray put(boolean value) throws JsonException {
		return put(jsonArray.length(), value);
	}

	@Override
	public JsonUtilityService.JSONArray put(int index, boolean value) throws JsonException {
		try {
			jsonArray.put(index, value);
		} catch (Exception e) {
			throw new JsonException(e);
		}

		return this;
	}

	@Override
	public JsonUtilityService.JSONObject getJSONObject(int index) throws JsonException {
		try {
			return new AndroidJsonObject(jsonArray.getJSONObject(index));
		} catch (JSONException e) {
			throw new JsonException(e);
		}
	}

	@Override
	public JsonUtilityService.JSONArray getJSONArray(int index) throws JsonException {
		try {
			return new AndroidJsonArray(jsonArray.getJSONArray(index));
		} catch (JSONException e) {
			throw new JsonException(e);
		}
	}

	@Override
	public int getInt(int index) throws JsonException {
		try {
			return jsonArray.getInt(index);
		} catch (JSONException e) {
			throw new JsonException(e);
		}
	}

	@Override
	public long getLong(int index) throws JsonException {
		try {
			return jsonArray.getLong(index);
		} catch (JSONException e) {
			throw new JsonException(e);
		}
	}

	@Override
	public double getDouble(int index) throws JsonException {
		try {
			return jsonArray.getDouble(index);
		} catch (JSONException e) {
			throw new JsonException(e);
		}
	}

	@Override
	public String getString(int index) throws JsonException {
		try {
			return jsonArray.getString(index);
		} catch (JSONException e) {
			throw new JsonException(e);
		}
	}

	@Override
	public boolean getBoolean(int index) throws JsonException {
		try {
			return jsonArray.getBoolean(index);
		} catch (JSONException e) {
			throw new JsonException(e);
		}
	}

	@Override
	public Object opt(int index) {
		return jsonArray.opt(index);
	}

	@Override
	public JsonUtilityService.JSONObject optJSONObject(int index) {
		JSONObject androidJsonObj = jsonArray.optJSONObject(index);

		if (androidJsonObj != null) {
			return new AndroidJsonObject(androidJsonObj);
		} else {
			return null;
		}
	}

	@Override
	public JsonUtilityService.JSONArray optJSONArray(int index) {
		JSONArray androidJsonArr = jsonArray.optJSONArray(index);

		if (androidJsonArr != null) {
			return new AndroidJsonArray(androidJsonArr);
		} else {
			return null;
		}
	}

	@Override
	public int optInt(int index, int defaultValue) {
		return jsonArray.optInt(index, defaultValue);
	}

	@Override
	public long optLong(int index, long defaultValue) {
		return jsonArray.optLong(index, defaultValue);
	}

	@Override
	public double optDouble(int index, double defaultValue) {
		return jsonArray.optDouble(index, defaultValue);
	}

	@Override
	public String optString(int index, String defaultValue) {
		return jsonArray.optString(index, defaultValue);
	}

	@Override
	public boolean optBoolean(int index, boolean defaultValue) {
		return jsonArray.optBoolean(index, defaultValue);
	}

	@Override
	public int length() {
		return jsonArray.length();
	}

	@Override
	public String toString() {
		return jsonArray.toString();
	}

}
