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

public class FakeJSONArray implements JsonUtilityService.JSONArray {
	private org.json.JSONArray jsonArray;

	FakeJSONArray(org.json.JSONArray jsonArray) {
		this.jsonArray = jsonArray;
	}

	@Override
	public Object get(int index) throws JsonException {
		try {
			Object value = jsonArray.get(index);

			if (value == JSONObject.NULL) {
				return null;
			}

			if (value instanceof org.json.JSONObject) {
				return new FakeJSONObject((JSONObject) value);
			}

			if (value instanceof org.json.JSONArray) {
				return new FakeJSONArray((JSONArray) value);
			}

			return jsonArray.get(index);
		} catch (JSONException e) {
			throw new JsonException(e);
		}
	}

	@Override
	public JsonUtilityService.JSONArray put(Object object) throws JsonException {
		if (object == null) {
			jsonArray.put(JSONObject.NULL);
		} else if (object instanceof JsonUtilityService.JSONObject) {
			return put((JsonUtilityService.JSONObject)object);
		} else if (object instanceof JsonUtilityService.JSONArray) {
			return put((JsonUtilityService.JSONArray)object);
		} else {
			jsonArray.put(object);
		}

		return this;
	}

	@Override
	public JsonUtilityService.JSONArray put(int index, Object object) throws JsonException {
		try {
			if (object == null) {
				jsonArray.put(index, JSONObject.NULL);
			} else if (object instanceof JsonUtilityService.JSONObject) {
				return put(index, (JsonUtilityService.JSONObject)object);
			} else if (object instanceof JsonUtilityService.JSONArray) {
				return put(index, (JsonUtilityService.JSONArray)object);
			} else {
				jsonArray.put(index, object);
			}

			return this;
		} catch (JSONException e) {
			throw new JsonException(e);
		}
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
		} catch (JSONException e) {
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
		} catch (JSONException e) {
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
		} catch (JSONException e) {
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
		} catch (JSONException e) {
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
		} catch (JSONException e) {
			throw new JsonException(e);
		}

		return this;
	}

	@Override
	public JsonUtilityService.JSONObject getJSONObject(int index) throws JsonException {
		try {
			if (jsonArray.isNull(index)) {
				return null;
			}

			return new FakeJSONObject(jsonArray.getJSONObject(index));
		} catch (JSONException e) {
			throw new JsonException(e);
		}
	}

	@Override
	public JsonUtilityService.JSONArray getJSONArray(int index) throws JsonException {
		try {
			if (jsonArray.isNull(index)) {
				return null;
			}

			return new FakeJSONArray(jsonArray.getJSONArray(index));
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
			if (index < 0 || index >= length()) {
				throw new JsonException("index out of bounds: " + index);
			}

			if (jsonArray.isNull(index)) {
				return null;
			}

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
		try {
			return get(index);
		} catch (JsonException ex) {
			return null;
		}
	}

	private <T> T optT(final int index, final Class<T> klass, final T defaultValue) {
		final Object element = opt(index);

		if (element == null || !klass.isInstance(element)) {
			return defaultValue;
		}

		return (T) element;
	}

	@Override
	public JsonUtilityService.JSONObject optJSONObject(final int index) {
		return optT(index, JsonUtilityService.JSONObject.class, null);
	}

	@Override
	public JsonUtilityService.JSONArray optJSONArray(final int index) {
		return optT(index, JsonUtilityService.JSONArray.class, null);
	}

	@Override
	public int optInt(int index, int defaultValue) {
		return optT(index, Number.class, (Number)new Integer(defaultValue)).intValue();
	}

	@Override
	public long optLong(int index, long defaultValue) {
		return optT(index, Number.class, (Number)new Long(defaultValue)).longValue();
	}

	@Override
	public double optDouble(int index, double defaultValue) {
		return optT(index, Number.class, (Number)new Double(defaultValue)).doubleValue();
	}

	@Override
	public String optString(int index, String defaultValue) {
		return optT(index, String.class, defaultValue);
	}

	@Override
	public boolean optBoolean(int index, boolean defaultValue) {
		return optT(index, Boolean.class, new Boolean(defaultValue)).booleanValue();
	}

	@Override
	public int length() {
		return jsonArray.length();
	}

	@Override
	public String toString() {
		return jsonArray.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		FakeJSONArray that = (FakeJSONArray) o;
		return this.jsonArray != null ? true : that.jsonArray == null;
	}

	@Override
	public int hashCode() {
		return jsonArray != null ? jsonArray.hashCode() : 0;
	}
}
