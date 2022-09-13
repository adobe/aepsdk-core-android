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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.adobe.marketing.mobile.internal.context.App;

import java.util.Map;

@SuppressLint("CommitPrefEdits")

/**
  * The Android implementation for {@link LocalStorageService.DataStore}.
  */
class AndroidDataStore implements LocalStorageService.DataStore {
	private static final String LOG_TAG = AndroidDataStore.class.getSimpleName();

	private SharedPreferences        sharedPreferences;
	private SharedPreferences.Editor sharedPreferencesEditor;

	/**
	 * Create a dataStore if it doesn't exist
	 *
	 * @param dataStoreName the name of the DataStore
	 * @return  AndroidDataStore created DataStore object
	 */
	static AndroidDataStore createDataStore(String dataStoreName) {
		Context appContext = App.getAppContext();

		if (appContext == null || dataStoreName == null || dataStoreName.isEmpty()) {
			return null;
		}

		AndroidDataStore dataStore = new AndroidDataStore(dataStoreName);

		if (dataStore.sharedPreferences == null || dataStore.sharedPreferencesEditor == null) {
			return null;
		}

		return dataStore;
	}

	/**
	 * Constructor
	 *
	 * @param dataStoreName the name of the DataStore
	 */
	private AndroidDataStore(String dataStoreName) {
		sharedPreferences = App.getAppContext().getSharedPreferences(dataStoreName, 0);

		if (sharedPreferences != null) {
			sharedPreferencesEditor = sharedPreferences.edit();
		}
	}

	@Override
	public void setInt(String key, int value) {
		if (sharedPreferencesEditor == null) {
			return;
		}

		sharedPreferencesEditor.putInt(key, value);
		sharedPreferencesEditor.commit();
	}

	@Override
	public int getInt(String key, int defaultValue) {
		return sharedPreferences.getInt(key, defaultValue);
	}

	@Override
	public void setString(String key, String value) {
		sharedPreferencesEditor.putString(key, value);
		sharedPreferencesEditor.commit();
	}

	@Override
	public String getString(String key, String defaultValue) {
		return sharedPreferences.getString(key, defaultValue);
	}

	@Override
	public void setDouble(String key, double value) {
		sharedPreferencesEditor.putLong(key, Double.doubleToRawLongBits(value));
		sharedPreferencesEditor.commit();
	}

	@Override
	public double getDouble(String key, double defaultValue) {
		long doubleRawLongBits = sharedPreferences.getLong(key, Double.doubleToRawLongBits(defaultValue));
		return Double.longBitsToDouble(doubleRawLongBits);
	}

	@Override
	public void setLong(String key, long value) {
		sharedPreferencesEditor.putLong(key, value);
		sharedPreferencesEditor.commit();
	}

	@Override
	public long getLong(String key, long defaultValue) {
		return sharedPreferences.getLong(key, defaultValue);
	}

	@Override
	public void setFloat(String key, float value) {
		sharedPreferencesEditor.putFloat(key, value);
		sharedPreferencesEditor.commit();
	}

	@Override
	public float getFloat(String key, float defaultValue) {
		return sharedPreferences.getFloat(key, defaultValue);
	}

	@Override
	public void setBoolean(String key, boolean value) {
		sharedPreferencesEditor.putBoolean(key, value);
		sharedPreferencesEditor.commit();
	}

	@Override
	public boolean getBoolean(String key, boolean defaultValue) {
		return sharedPreferences.getBoolean(key, defaultValue);
	}

	@Override
	public void setMap(String key, Map<String, String> value) {
		try {
			org.json.JSONObject jsonFromMap = new org.json.JSONObject(value);
			sharedPreferencesEditor.putString(key, jsonFromMap.toString());
			sharedPreferencesEditor.commit();
		} catch (NullPointerException e) {
			Log.error(LOG_TAG, "Map contains null key.");
		}
	}

	@Override
	public Map<String, String> getMap(String key) {
		String mapJsonString = sharedPreferences.getString(key, null);

		if (mapJsonString == null) {
			return null;
		}

		AndroidJsonUtility jsonUtility = new AndroidJsonUtility();	// only use case of AndroidJsonUtility here
		JsonUtilityService.JSONObject jsonObject;
		jsonObject = jsonUtility.createJSONObject(mapJsonString);
		return jsonUtility.mapFromJsonObject(jsonObject);
	}

	@Override
	public boolean contains(String key) {
		return sharedPreferences.contains(key);
	}

	@Override
	public void remove(String key) {
		sharedPreferencesEditor.remove(key);
		sharedPreferencesEditor.commit();
	}

	@Override
	public void removeAll() {
		sharedPreferencesEditor.clear();
		sharedPreferencesEditor.commit();
	}
}
