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

import java.util.Map;

public interface NamedCollection {
    /**
     * Set or update an int value
     *
     * @param key String key name
     * @param value int value
     */
    void setInt(String key, int value);

    /**
     * Get int value for key
     *
     * @param key String key name
     * @param defaultValue int the default value to return if key does not exist
     * @return persisted value if it exists, defaultValue otherwise
     */
    int getInt(String key, int defaultValue);

    /**
     * Set or update a String value for key
     *
     * @param key String key name
     * @param value String the default value to return if key does not exist
     */
    void setString(String key, String value);

    /**
     * Get String value for key
     *
     * @param key String key name
     * @param defaultValue String the default value to return if key does not exist
     * @return persisted value if it exists, defaultValue otherwise
     */
    String getString(String key, String defaultValue);

    /**
     * Set or update a double value for key
     *
     * @param key String key name
     * @param value double the default value to return if key does not exist
     */
    void setDouble(String key, double value);

    /**
     * Get double value for key
     *
     * @param key String key name
     * @param defaultValue double the default value to return if key does not exist
     * @return persisted value if it exists, defaultValue otherwise
     */
    double getDouble(String key, double defaultValue);

    /**
     * Set or update a long value for key
     *
     * @param key String key name
     * @param value long the default value to return if key does not exist
     */
    void setLong(String key, long value);

    /**
     * Get long value for key
     *
     * @param key String key name
     * @param defaultValue long the default value to return if key does not exist
     * @return persisted value if it exists, defaultValue otherwise
     */
    long getLong(String key, long defaultValue);

    /**
     * Set or update a float value for key
     *
     * @param key String key name
     * @param value float the default value to return if key does not exist
     */
    void setFloat(String key, float value);

    /**
     * Get float value for key
     *
     * @param key String key name
     * @param defaultValue float the default value to return if key does not exist
     * @return persisted value if it exists, defaultValue otherwise
     */
    float getFloat(String key, float defaultValue);

    /**
     * Set or update a boolean value for key
     *
     * @param key String key name
     * @param value boolean the default value to return if key does not exist
     */
    void setBoolean(String key, boolean value);

    /**
     * Get boolean value for key
     *
     * @param key String key name
     * @param defaultValue boolean the default value to return if key does not exist
     * @return persisted value if it exists, defaultValue otherwise
     */
    boolean getBoolean(String key, boolean defaultValue);

    /**
     * Set or update a Map value for key
     *
     * @param key String key name
     * @param value Map the default value to return if key does not exist
     */
    void setMap(String key, Map<String, String> value);

    /**
     * Get Map value for key
     *
     * @param key String key name
     * @return persisted value if it exists, null otherwise
     */
    Map<String, String> getMap(String key);

    /**
     * Check if the named collection contains key
     *
     * @param key String key name
     * @return true if key exists, false otherwise
     */
    boolean contains(String key);

    /**
     * Remove persisted value for key
     *
     * @param key String key name
     */
    void remove(String key);

    /** Remove all key-value pairs from this named collection */
    void removeAll();
}
