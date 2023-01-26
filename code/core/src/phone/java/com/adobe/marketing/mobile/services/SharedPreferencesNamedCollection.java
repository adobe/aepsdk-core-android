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

package com.adobe.marketing.mobile.services;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/** Implementation of {@link NamedCollection} */
@SuppressLint("CommitPrefEdits")
class SharedPreferencesNamedCollection implements NamedCollection {

    private static final String TAG = SharedPreferencesNamedCollection.class.getSimpleName();
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;

    SharedPreferencesNamedCollection(
            final SharedPreferences sharedPreferences,
            final SharedPreferences.Editor sharedPreferencesEditor) {
        this.sharedPreferences = sharedPreferences;
        this.sharedPreferencesEditor = sharedPreferencesEditor;
    }

    @Override
    public void setInt(final String key, final int value) {
        if (sharedPreferencesEditor == null) {
            return;
        }

        sharedPreferencesEditor.putInt(key, value);
        sharedPreferenceCommit();
    }

    @Override
    public int getInt(final String key, final int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    @Override
    public void setString(final String key, final String value) {
        sharedPreferencesEditor.putString(key, value);
        sharedPreferenceCommit();
    }

    @Override
    public String getString(final String key, final String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    @Override
    public void setDouble(final String key, final double value) {
        sharedPreferencesEditor.putLong(key, Double.doubleToRawLongBits(value));
        sharedPreferenceCommit();
    }

    @Override
    public double getDouble(final String key, final double defaultValue) {
        long doubleRawLongBits =
                sharedPreferences.getLong(key, Double.doubleToRawLongBits(defaultValue));
        return Double.longBitsToDouble(doubleRawLongBits);
    }

    @Override
    public void setLong(final String key, final long value) {
        sharedPreferencesEditor.putLong(key, value);
        sharedPreferenceCommit();
    }

    @Override
    public long getLong(final String key, final long defaultValue) {
        return sharedPreferences.getLong(key, defaultValue);
    }

    @Override
    public void setFloat(final String key, final float value) {
        sharedPreferencesEditor.putFloat(key, value);
        sharedPreferenceCommit();
    }

    @Override
    public float getFloat(final String key, final float defaultValue) {
        return sharedPreferences.getFloat(key, defaultValue);
    }

    @Override
    public void setBoolean(final String key, final boolean value) {
        sharedPreferencesEditor.putBoolean(key, value);
        sharedPreferenceCommit();
    }

    @Override
    public boolean getBoolean(final String key, final boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    @Override
    public void setMap(final String key, final Map<String, String> value) {
        try {
            JSONObject jsonFromMap = new JSONObject(value);
            sharedPreferencesEditor.putString(key, jsonFromMap.toString());
            sharedPreferenceCommit();
        } catch (NullPointerException e) {
            Log.error(ServiceConstants.LOG_TAG, TAG, "Map contains null key.");
        }
    }

    @Override
    public Map<String, String> getMap(final String key) {
        String mapJsonString = sharedPreferences.getString(key, null);
        Map<String, String> map = new HashMap<String, String>();

        if (mapJsonString == null) {
            return null;
        }

        try {
            JSONObject jsonObject = new JSONObject(mapJsonString);
            Iterator<String> keyItr = jsonObject.keys();

            while (keyItr.hasNext()) {
                String keyName = keyItr.next();

                try {
                    map.put(keyName, jsonObject.getString(keyName));
                } catch (JSONException jsonException) {
                    Log.error(
                            ServiceConstants.LOG_TAG,
                            TAG,
                            String.format(
                                    "Unable to convert jsonObject key %s into map, %s",
                                    keyName, jsonException.getLocalizedMessage()));
                }
            }
        } catch (Exception e) {
            Log.error(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    String.format(
                            "Failed to convert [%s] to String Map, %s",
                            mapJsonString, e.getLocalizedMessage()));
            map = null;
        }

        return map;
    }

    @Override
    public boolean contains(final String key) {
        return sharedPreferences.contains(key);
    }

    @Override
    public void remove(final String key) {
        sharedPreferencesEditor.remove(key);
        sharedPreferenceCommit();
    }

    @Override
    public void removeAll() {
        sharedPreferencesEditor.clear();
        sharedPreferenceCommit();
    }

    private void sharedPreferenceCommit() {
        if (!sharedPreferencesEditor.commit()) {
            Log.error(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    "Android SharedPreference unable to commit the persisted data");
        }
    }
}
