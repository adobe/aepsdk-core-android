/*
  Copyright 2024 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.util;

import com.adobe.marketing.mobile.services.NamedCollection;
import java.util.HashMap;
import java.util.Map;

public class FakeNamedCollection implements NamedCollection {

	private HashMap<String, Object> dataStore = new HashMap<>();

	@Override
	public void setInt(String key, int val) {
		dataStore.put(key, val);
	}

	@Override
	public int getInt(String key, int fallback) {
		return DataReader.optInt(dataStore, key, fallback);
	}

	@Override
	public void setString(String key, String val) {
		dataStore.put(key, val);
	}

	@Override
	public String getString(String key, String fallback) {
		return DataReader.optString(dataStore, key, fallback);
	}

	@Override
	public void setDouble(String key, double val) {
		dataStore.put(key, val);
	}

	@Override
	public double getDouble(String key, double fallback) {
		return DataReader.optDouble(dataStore, key, fallback);
	}

	@Override
	public void setLong(String key, long val) {
		dataStore.put(key, val);
	}

	@Override
	public long getLong(String key, long fallback) {
		return DataReader.optLong(dataStore, key, fallback);
	}

	@Override
	public void setFloat(String key, float val) {
		dataStore.put(key, val);
	}

	@Override
	public float getFloat(String key, float fallback) {
		return DataReader.optFloat(dataStore, key, fallback);
	}

	@Override
	public void setBoolean(String key, boolean val) {
		dataStore.put(key, val);
	}

	@Override
	public boolean getBoolean(String key, boolean fallback) {
		return DataReader.optBoolean(dataStore, key, fallback);
	}

	@Override
	public void setMap(String key, Map<String, String> val) {
		dataStore.put(key, val);
	}

	@Override
	public Map<String, String> getMap(String key) {
		return DataReader.optTypedMap(String.class, dataStore, key, null);
	}

	@Override
	public boolean contains(String key) {
		return dataStore.containsKey(key);
	}

	@Override
	public void remove(String key) {
		dataStore.remove(key);
	}

	@Override
	public void removeAll() {
		dataStore = new HashMap<>();
	}
}
