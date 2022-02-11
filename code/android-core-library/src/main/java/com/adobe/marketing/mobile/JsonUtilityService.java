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

import java.util.Iterator;
import java.util.Map;

interface JsonUtilityService {

	/**
	 * Creates a {@code JSONObject} from the JSON formatted {@code String} provided as input.
	 * <p>
	 * The JSON formatted {@code String} must have the JSON Object formatted {@code String} as the root element.
	 *
	 * @param json A JSON formatted {@link String}
	 * @return A valid {@link JSONObject} if parse was successful, null otherwise
	 */
	JSONObject createJSONObject(String json);

	/**
	 * Create a {@code JSONObject} from a {@code Map}.
	 *
	 * @param map {@code Map} from which to create a {@link JSONObject}
	 * @return A valid {@code JSONObject} if parse was successful, null otherwise
	 */
	JSONObject createJSONObject(Map map);

	/**
	 * Creates a {@code JSONArray} from the JSON formatted {@code String} provided as input.
	 * <p>
	 * The JSON formatted {@code String} must have the JSON Array formatted {@code String} as the root element.
	 *
	 * @param json A JSON formatted {@link String}
	 * @return A valid {@link JSONArray} if parse was successful, null otherwise
	 */
	JSONArray createJSONArray(String json);

	/**
	 * Creates a {@code Map<String,String>} from the given {@code JSONObject}.
	 * <p>
	 * Ignores the values for those keys that are not of type {@code String}.
	 *
	 * @param jsonData {@link JSONObject} that needs to be converted to a {@code Map<String,String>}
	 * @return A valid {@link Map} if parse was successful, null otherwise
	 */
	Map<String, String> mapFromJsonObject(JSONObject jsonData);

	interface JSONObject {
		/**
		 * Returns an {@code Object} value for the key specified.
		 * <p>
		 * For the special case of a JSON NULL value, this function will return an Object and not null.
		 * To check if the return value is null in all cases, use the following code:
		 *
		 * Object value = jsonObject.get(key)
		 * if (value == null || value.equals(null)) {
		 *     // handle 'null' String value ...
		 * }
		 *
		 * @param name {@link String} key name
		 * @return {@link Object} for the given key name, and the return value can be null
		 * @throws JsonException if the value is not an {@code Object}, or the key is not present
		 */
		Object get(String name) throws JsonException;

		/**
		 * Returns a {@code JSONObject} value for the key specified.
		 *
		 * @param name {@link String} key name
		 * @return {@link JSONObject} for the given key name, or null if the value was null
		 * @throws JsonException if the value is not a {@code JSONObject}, or the key is not present
		 */
		JSONObject getJSONObject(String name) throws JsonException;

		/**
		 * Returns a {@code JSONArray} value for the key specified.
		 *
		 * @param name {@link String} key name
		 * @return {@link JSONArray} for the given key name, or null if the value was null
		 * @throws JsonException if the value is not a {@code JSONArray}, or the key is not present
		 */
		JSONArray getJSONArray(String name) throws JsonException;

		/**
		 * Returns an {@code int} value for the key specified.
		 *
		 * @param name {@link String} key name
		 * @return {@code int} value for the given key name
		 * @throws JsonException if the value is not a {@code int}, the key is not present, or
		 * the value is null
		 */
		int getInt(String name) throws JsonException;

		/**
		 * Returns a {@code long} value for the key specified.
		 *
		 * @param name {@link String} key name
		 * @return {@code long} value for the given key name
		 * @throws JsonException if the value is not a {@code long}, the key is not present, or
		 * the value is null
		 */
		long getLong(String name) throws JsonException;

		/**
		 * Returns a {@code double} value for the key specified.
		 *
		 * @param name {@link String} key name
		 * @return {@code double} value for the given key name
		 * @throws JsonException if the value is not a {@code double}, the key is not present, or
		 * the value is null
		 */
		double getDouble(String name) throws JsonException;

		/**
		 * Returns a {@code String} value for the key specified.
		 *
		 * @param name {@link String} key name
		 * @return {@code String} value for the given key name, or null if the value was null
		 * @throws JsonException if the value is not a {@code String}, or the key is not present
		 */
		String getString(String name) throws JsonException;

		/**
		 * Returns a {@code boolean} value for the key specified.
		 *
		 * @param name {@link String} key name
		 * @return {@code boolean} value for the given key name
		 * @throws JsonException if the value is not a {@code boolean}, the key is not present, or
		 * the value is null
		 */
		boolean getBoolean(String name) throws JsonException;

		/**
		 * Returns an {@code Object} value if a mapping exists, null otherwise.
		 *
		 * @param name {@link String} key name
		 * @return the {@link Object} value
		 */
		Object opt(String name);

		/**
		 * Insert an {@code Object} value.
		 *
		 * In particular, calling put(name, null) removes the named entry from the object.
		 *
		 * @param name {@link String} key name
		 * @param object {@link Object} the value. Can be null.
		 * @return {@link JSONObject} this object
		 * @throws JsonException on failure
		 */
		JSONObject put(String name, Object object) throws JsonException;

		/**
		 * Insert a {@code JSONObject} value.
		 *
		 * In particular, calling put(name, null) removes the named entry from the object.
		 *
		 * @param name {@link String} key name
		 * @param jsonObject {@link JSONObject} the value. Can be null.
		 * @return {@code JSONObject} this object
		 * @throws JsonException on failure
		 */
		JSONObject put(String name, JSONObject jsonObject) throws JsonException;

		/**
		 * Insert a {@code JSONArray} value.
		 *
		 * In particular, calling put(name, null) removes the named entry from the object.
		 *
		 * @param name {@link String} key name
		 * @param jsonArray {@link JSONArray} value. Can be null.
		 * @return {@link JSONObject} this object
		 * @throws JsonException on failure
		 */
		JSONObject put(String name, JSONArray jsonArray) throws JsonException;

		/**
		 * Insert an {@code int} value.
		 *
		 * @param name {@link String} key name
		 * @param value {@code int} the value
		 * @return {@link JSONObject} this object
		 * @throws JsonException on failure
		 */
		JSONObject put(String name, int value) throws JsonException;

		/**
		 * Insert a {@code long} value.
		 *
		 * @param name {@link String} key name
		 * @param value {@code long} the value
		 * @return {@link JSONObject} this object
		 * @throws JsonException on failure
		 */
		JSONObject put(String name, long value) throws JsonException;

		/**
		 * Insert a {@code double} value.
		 *
		 * @param name {@link String} key name
		 * @param value {@code double} the value
		 * @return {@link JSONObject} this object
		 * @throws JsonException on failure
		 */
		JSONObject put(String name, double value) throws JsonException;

		/**
		 * Insert a {@code String}value.
		 *
		 * @param name {@link String} key name
		 * @param value {@code String} the value. Can be null.
		 * @return {@link JSONObject} this object
		 * @throws JsonException on failure
		 */
		JSONObject put(String name, String value) throws JsonException;

		/**
		 * Insert a {@code boolean} value.
		 *
		 * @param name {@link String} key name
		 * @param value {@code boolean} the value
		 * @return {@link JSONObject} this object
		 * @throws JsonException on failure
		 */
		JSONObject put(String name, boolean value) throws JsonException;

		/**
		 * Returns a {@code JSONObject} if a mapping exists, null otherwise.
		 *
		 * @param name {@link String} key name
		 * @return {@link JSONObject}, if mapping exists, null otherwise
		 */
		JSONObject optJSONObject(String name);

		/**
		 * Returns a {@code JSONArray} if a mapping exists, null otherwise.
		 *
		 * @param name {@link String} key name
		 * @return {@link JSONArray}, if mapping exists, null otherwise
		 */
		JSONArray optJSONArray(String name);

		/**
		 * Returns the {@code int} value, if a valid mapping exists.
		 *
		 * @param name {@link String} key name
		 * @param defaultValue {@code int} value to be returned if a valid mapping does not exist
		 * @return {@code int} value if a valid mapping exists and is a number, {@code defaultValue}
		 * otherwise
		 */
		int optInt(String name, int defaultValue);

		/**
		 * Returns the {@code long} value, if a valid mapping exists.
		 *
		 * @param name {@link String} key name
		 * @param defaultValue {@code long} value to be returned if a valid mapping does not exist
		 * @return {@code long} value if a valid mapping exists and is a number,
		 * {@code defaultValue} otherwise
		 */
		long optLong(String name, long defaultValue);

		/**
		 * Returns the {@code double} value, if a valid mapping exists.
		 *
		 * @param name {@link String} key name
		 * @param defaultValue {@code double} value to be returned if a valid mapping does not exist
		 * @return {@code double} value if a valid mapping exists and is a number,
		 * {@code defaultValue} otherwise
		 */
		double optDouble(String name, double defaultValue);

		/**
		 * Returns the {@code String} value, if a valid mapping exists.
		 *
		 * @param name {@link String} key name
		 * @param defaultValue {@code String} value to be returned if a valid mapping does not exist
		 * @return {@link String} value if a valid mapping exists and is a non-null string,
		 * {@code defaultValue} otherwise
		 */
		String optString(String name, String defaultValue);

		/**
		 * Returns the {@code boolean} value, if a valid mapping exists.
		 *
		 * @param name {@link String} key name
		 * @param defaultValue {@code boolean} value to be returned if a valid mapping does not exist
		 * @return {@code boolean} value if a valid mapping exists and is a boolean,
		 * {@code defaultValue} otherwise
		 */
		boolean optBoolean(String name, boolean defaultValue);

		/**
		 * Returns an iterator of the {@code String} keys in this object.
		 *
		 * @return {@link Iterator} of the {@code String} keys
		 */
		Iterator<String> keys();

		/**
		 * Removes the specified key and its value
		 *
		 * @param name key that will be removed from the {@link JSONObject}
		 */
		void remove(String name);

		/**
		 * Returns the number of mappings in this object.
		 *
		 * @return {@code int} The number of mappings
		 */
		int length();
	}

	interface JSONArray {
		/**
		 * Returns the {@code Object} value if a valid value is present at the index specified.
		 *
		 * @param index The index into the JSONArray
		 * @return {@link Object} value (may be null if the JSON value was null)
		 * @throws JsonException thrown if the index is invalid, or the value at index is not a
		 * {@link JSONObject}
		 */
		Object get(int index) throws JsonException;

		/**
		 * Append an {@code Object} value to the end of this array.
		 *
		 * @param object {@link Object} value (may be null to append null JSON value)
		 * @return {@link JSONArray} this array
		 * @throws JsonException on failure
		 */
		JSONArray put(Object object) throws JsonException;

		/**
		 * Sets the value at index to value, null padding this array to the required length if necessary. If a value
		 * already exists at index, it will be replaced.
		 *
		 * @param object {@link Object} value (may be null to assign null JSON value)
		 * @param index {@code int} insertion index
		 * @return {@link JSONArray} this array
		 * @throws JsonException on failure
		 */
		JSONArray put(int index, Object object) throws JsonException;

		/**
		 * Append a {@code JSONObject} value to the end of this array.
		 *
		 * @param jsonObject {@link JSONObject} value (may be null to append null JSON value)
		 * @return {@link JSONArray} this array
		 * @throws JsonException on failure
		 */
		JSONArray put(JSONObject jsonObject) throws JsonException;

		/**
		 * Sets the value at index to value, null padding this array to the required length if necessary. If a value
		 * already exists at index, it will be replaced.
		 *
		 * @param index {@code int} insertion index
		 * @param jsonObject {@link JSONObject} value (may be null to assign null JSON value)
		 * @return {@link JSONArray} this array
		 * @throws JsonException on failure
		 */
		JSONArray put(int index, JSONObject jsonObject) throws JsonException;

		/**
		 * Append a {@code JSONObject} value to the end of this array.
		 *
		 * @param jsonArray {@link JSONArray} value (may be null to append null JSON value)
		 * @return {@code JSONArray} this array
		 * @throws JsonException on failure
		 */
		JSONArray put(JSONArray jsonArray) throws JsonException;

		/**
		 * Sets the value at index to value, null padding this array to the required length if necessary. If a value
		 * already exists at index, it will be replaced.
		 *
		 * @param index {@code int} insertion index
		 * @param jsonArray {@link JSONArray} value (may be null to assign null JSON value)
		 * @return {@code JSONArray} this array
		 * @throws JsonException on failure
		 */
		JSONArray put(int index, JSONArray jsonArray) throws JsonException;

		/**
		 * Append a {@code JSONObject} value to the end of this array.
		 *
		 * @param value {@code int} value
		 * @return {@link JSONArray} this array
		 * @throws JsonException on failure
		 */
		JSONArray put(int value) throws JsonException;

		/**
		 * Sets the value at index to value, null padding this array to the required length if necessary. If a value
		 * already exists at index, it will be replaced.
		 *
		 * @param index {@code int} insertion index
		 * @param value {@code int} value
		 * @return {@link JSONArray} this array
		 * @throws JsonException on failure
		 */
		JSONArray put(int index, int value) throws JsonException;

		/**
		 * Append a {@code JSONObject} value to the end of this array.
		 *
		 * @param value {@code long} value
		 * @return {@link JSONArray} this array
		 * @throws JsonException on failure
		 */
		JSONArray put(long value) throws JsonException;

		/**
		 * Sets the value at index to value, null padding this array to the required length if necessary. If a value
		 * already exists at index, it will be replaced.
		 *
		 * @param index {@code int} insertion index
		 * @param value {@code long} value
		 * @return {@link JSONArray} this array
		 * @throws JsonException on failure
		 */
		JSONArray put(int index, long value) throws JsonException;

		/**
		 * Append a {@code JSONObject} value to the end of this array.
		 *
		 * @param value {@code double} value
		 * @return {@link JSONArray} this array
		 * @throws JsonException on failure
		 */
		JSONArray put(double value) throws JsonException;

		/**
		 * Sets the value at index to value, null padding this array to the required length if necessary. If a value
		 * already exists at index, it will be replaced.
		 *
		 * @param index {@code int} insertion index
		 * @param value {@code double} value
		 * @return {@link JSONArray} this array
		 * @throws JsonException on failure
		 */
		JSONArray put(int index, double value) throws JsonException;

		/**
		 * Append a {@code String} value to the end of this array.
		 *
		 * @param value {@link String} value (may be null to append null JSON value)
		 * @return {@link JSONArray} this array
		 * @throws JsonException on failure
		 */
		JSONArray put(String value) throws JsonException;

		/**
		 * Sets the value at index to value, null padding this array to the required length if necessary. If a value
		 * already exists at index, it will be replaced.
		 *
		 * @param index {@code int} insertion index
		 * @param value {@link String} value (may be null to assign null JSON value)
		 * @return {@link JSONArray} this array
		 * @throws JsonException on failure
		 */
		JSONArray put(int index, String value) throws JsonException;

		/**
		 * Append a {@code boolean} value to the end of this array.
		 *
		 * @param value {@code boolean} value
		 * @return {@link JSONArray} this array
		 * @throws JsonException on failure
		 */
		JSONArray put(boolean value) throws JsonException;

		/**
		 * Sets the value at index to value, null padding this array to the required length if necessary. If a value
		 * already exists at index, it will be replaced.
		 *
		 * @param index {@code int} insertion index
		 * @param value {@code boolean} value
		 * @return {@link JSONArray} this array
		 * @throws JsonException on failure
		 */
		JSONArray put(int index, boolean value) throws JsonException;

		/**
		 * Returns the {@code JSONObject} value if a valid value is present at the index specified.
		 *
		 * @param index The index into the {@link JSONArray}
		 * @return {@link JSONObject} (may be null if the JSON value was null)
		 * @throws JsonException thrown if the index is invalid, or the value at index is not a {@code JSONObject}
		 */
		JSONObject getJSONObject(int index) throws JsonException;

		/**
		 * Returns the JSONArray value if a valid value is present at the index specified.
		 *
		 * @param index The index into the {@link JSONArray}
		 * @return {@link JSONArray} (may be null if the JSON value was null)
		 * @throws JsonException thrown if the index is invalid, or the value at index is not a {@code JSONArray}
		 */
		JSONArray getJSONArray(int index) throws JsonException;

		/**
		 * Returns the {@code int} value if a valid value is present at the index specified.
		 *
		 * @param index {@code int} the index into the {@link JSONArray}
		 * @return {@code int} value at given index
		 * @throws JsonException thrown if the index is invalid, the value at index is not a {@code int},
		 * or the value at index is null
		 */
		int getInt(int index) throws JsonException;

		/**
		 * Returns the {@code long} value if a valid value is present at the index specified.
		 *
		 * @param index {@code int} the index into the {@link JSONArray}
		 * @return {@code long} value at given index
		 * @throws JsonException thrown if the index is invalid, the value at index is not a {@code long},
		 * or the value at index is null
		 */
		long getLong(int index) throws JsonException;

		/**
		 * Returns the {@code double} value if a valid value is present at the index specified.
		 *
		 * @param index {@code int} the index into the {@link JSONArray}
		 *
		 * @return {@code double} value at given index
		 * @throws JsonException thrown if the index is invalid, or the value at index is not a double
		 */
		double getDouble(int index) throws JsonException;

		/**
		 * Returns the {@code String} value if a valid value is present at the index specified.
		 *
		 * @param index {@code int} the index into the {@link JSONArray}
		 * @return String (may be null if the JSON value was null)
		 * @throws JsonException thrown if the index is invalid, or the value at index is not a String
		 */
		String getString(int index) throws JsonException;

		/**
		 * Returns the {@code boolean} value if a valid value is present at the index specified.
		 *
		 * @param index {@code int} the index into the {@link JSONArray}
		 * @return {@code boolean}
		 * @throws JsonException thrown if the index is invalid, the value at index is not a boolean,
		 * or the value at index is null
		 */
		boolean getBoolean(int index) throws JsonException;

		/**
		 * Returns an {@code Object} value if it exists at the specified index, null otherwise.
		 *
		 * @param index {@code int} the index of the {@link JSONArray}
		 * @return {@link Object} at given index
		 */
		Object opt(int index);

		/**
		 * Returns the {@code JSONObject} value if a valid value is present at the index specified.
		 *
		 * @param index {@code int} the index into the {@link JSONArray}
		 * @return {@link JSONObject} value at the index if exists and valid, null otherwise
		 */
		JSONObject optJSONObject(int index);

		/**
		 * Returns the {@code JSONArray} value if a valid value is present at the index specified.
		 *
		 * @param index {@code int} the index into the {@link JSONArray}
		 * @return {@code JSONArray} value at the index, if exists and valid, null otherwise
		 */
		JSONArray optJSONArray(int index);

		/**
		 * Returns the {@code int} value if a valid value is present at the index specified.
		 *
		 * @param index {@code int} the index into the {@link JSONArray}
		 * @param defaultValue {@code int} value to be returned if a valid value does not exist at index
		 * @return {@code int} value at the index if it exists, is valid, and is not null,
		 * {@code defaultValue} otherwise
		 */
		int optInt(int index, int defaultValue);

		/**
		 * Returns the {@code long} value if a valid value is present at the index specified.
		 *
		 * @param index {@code int} the index into the {@link JSONArray}
		 * @param defaultValue {@code long} value to be returned if a valid value does not exist at index
		 * @return {@code long} value at the index if it exists, is valid, and is not null,
		 * {@code defaultValue} otherwise
		 */
		long optLong(int index, long defaultValue);

		/**
		 * Returns the {@code double} value if a valid value is present at the index specified.
		 *
		 * @param index {@code int} the index into the {@link JSONArray}
		 * @param defaultValue {@code double} value to be returned if a valid value does not exist at index
		 * @return {@code double} value at the index if it exists, is valid, and is not null,
		 * {@code defaultValue} otherwise
		 */

		double optDouble(int index, double defaultValue);

		/**
		 * Returns the {@code String} value if a valid value is present at the index specified.
		 *
		 * @param index {@code int} the index into the {@link JSONArray}
		 * @param defaultValue {@link String} value to be returned if a valid value does not exist at index
		 * @return {@code String} value at the index if it exists, is valid, and is not null,
		 * {@code defaultValue} otherwise
		 */
		String optString(int index, String defaultValue);

		/**
		 * Returns the {@code boolean} value if a valid value is present at the index specified.
		 *
		 * @param index {@code int} the index into the {@link JSONArray}
		 * @param defaultValue {@code boolean} value to be returned if a valid value does not exist at index
		 * @return {@code boolean} value at the index if it exists, is valid, and is not null,
		 * {@code defaultValue} otherwise
		 */
		boolean optBoolean(int index, boolean defaultValue);

		/**
		 * Returns the number of values in the array.
		 *
		 * @return {@code int} Number of values in the array
		 */
		int length();
	}

}
