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

import java.util.*;

public class FakeDataStore implements LocalStorageService.DataStore {

	private Map<String, Object> internalMap;

	FakeDataStore() {
		internalMap = new HashMap<String, Object>();
	}

	@Override
	public void setInt(String key, int value) {
		internalMap.put(key, value);
	}

	@Override
	public int getInt(String key, int defaultValue) {
		if (internalMap.get(key) == null) {
			return defaultValue;
		}

		return (Integer) internalMap.get(key);
	}

	@Override
	public void setString(String key, String value) {
		internalMap.put(key, value);
	}

	@Override
	public String getString(String key, String defaultValue) {
		if (internalMap.get(key) == null) {
			return defaultValue;
		}

		return (String) internalMap.get(key);
	}

	@Override
	public void setDouble(String key, double value) {
		internalMap.put(key, value);
	}

	@Override
	public double getDouble(String key, double defaultValue) {
		if (internalMap.get(key) == null) {
			return defaultValue;
		}

		return (Double) internalMap.get(key);
	}

	@Override
	public void setLong(String key, long value) {
		internalMap.put(key, value);
	}

	@Override
	public long getLong(String key, long defaultValue) {
		if (internalMap.get(key) == null) {
			return defaultValue;
		}

		return (Long) internalMap.get(key);
	}

	@Override
	public void setFloat(String key, float value) {
		internalMap.put(key, value);
	}

	@Override
	public float getFloat(String key, float defaultValue) {
		if (internalMap.get(key) == null) {
			return defaultValue;
		}

		return (Float) internalMap.get(key);
	}

	@Override
	public void setBoolean(String key, boolean value) {
		internalMap.put(key, value);
	}

	@Override
	public boolean getBoolean(String key, boolean defaultValue) {
		if (internalMap.get(key) == null) {
			return defaultValue;
		}

		return (Boolean) internalMap.get(key);
	}

	@Override
	public void setMap(String key, Map<String, String> value) {
		internalMap.put(key, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, String> getMap(String key) {
		if (internalMap.get(key) == null) {
			return null;
		}

		return (Map<String, String>) internalMap.get(key);
	}

	@Override
	public boolean contains(String key) {
		return internalMap.containsKey(key);
	}

	@Override
	public void remove(String key) {
		internalMap.remove(key);
	}

	@Override
	public void removeAll() {
		internalMap.clear();
	}

	@Override
	public String toString() {
		return internalMap.toString();
	}
}
