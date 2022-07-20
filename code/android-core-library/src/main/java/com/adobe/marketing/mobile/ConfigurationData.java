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


import com.adobe.marketing.mobile.internal.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

class ConfigurationData {
	private final Map<String, Variant> internalMap;
	private final JsonUtilityService  jsonUtilityService;

	ConfigurationData(final JsonUtilityService jsonUtilityService) {
		this.jsonUtilityService = jsonUtilityService;
		this.internalMap = new HashMap<String, Variant>();
	}

	/**
	 * Loops through the key-value pair present in the JSONString and throws them into this ConfigData Object
	 *
	 * @param jsonString Flattened Configuration String in JSON format. Most Probably its the String from the
	 *                   bundled JSON file or the configuration response downloaded from the remote.
	 *
	 * @return the ConfigData object with the config parameters from json string appended in them.
	 */
	ConfigurationData put(final String jsonString) {
		if (!StringUtils.isNullOrEmpty(jsonString)) {
			JsonUtilityService.JSONObject jsonObject;

			try {
				jsonObject = jsonUtilityService.createJSONObject(jsonString);
				return put(jsonObject);
			} catch (Exception exception) {
				Log.debug(ConfigurationExtension.LOG_SOURCE, "Unable to parse the Configuration from JSON String. Exception: (%s)",
						  exception);
			}
		}

		return this;
	}

	/**
	 * Loops through the key-value pair present in the Map and throws them into the internal Map
	 *
	 * @param map Map with flattened configuration data. Usually its the map passed from the updateconfig
	 *            event.
	 *
	 * @return the ConfigData object with the map appended in them.
	 */
	ConfigurationData put(final Map<String, Variant> map) {
		if (map != null) {

			try {
				for (Map.Entry<String, Variant> entry : map.entrySet()) {
					String key = entry.getKey();

					if (key != null) {
						internalMap.put(key, entry.getValue());
					}
				}
			} catch (Exception exception) {
				Log.debug(ConfigurationExtension.LOG_SOURCE, "Unable to parse the Configuration from HashMap. Exception: (%s)",
						  exception);
			}
		}

		return this;
	}


	/**
	 * Loops through the key-value pair present in the ConfigData Object and throws them into the internal Map
	 *
	 * @param configData Another config Data objects whose values will be copied into the calling object.
	 *
	 * @return the ConfigData object.
	 */
	ConfigurationData put(final ConfigurationData configData) {
		if (configData != null && configData.size() > 0) {
			try {
				for (Map.Entry<String, Variant> kv : configData.internalMap.entrySet()) {
					String key = kv.getKey();

					if (key != null) {
						internalMap.put(key, kv.getValue());
					}
				}
			} catch (Exception exception) {
				Log.debug(ConfigurationExtension.LOG_SOURCE, "Unable to put new ConfigData. Exception: (%s)",
						  exception);
			}
		}

		return this;
	}

	/**
	 * Loops through the key-value pair present in the JSONObject and throws them into this ConfigData Object.
	 *
	 * @param jsonObject JSONObject containing the flattened configuration elements.
	 *
	 * @return the ConfigData object itself.
	 */
	ConfigurationData put(final JsonUtilityService.JSONObject jsonObject) {
		if (jsonObject == null) {
			return this;
		}

		try {
			String buildEnvironment = jsonObject.optString(ConfigurationConstants.EventDataKeys.Configuration.BUILD_ENVIRONMENT,
									  "");
			final Variant jsonObjectAsVariant = Variant.fromTypedObject(jsonObject,
												new JsonObjectVariantSerializer(jsonUtilityService));
			final Map<String, Variant> variantMap = jsonObjectAsVariant.getVariantMap();

			// if this key is specific to a build environment, we don't need to process it
			for (Map.Entry<String, Variant> entry : variantMap.entrySet()) {
				String key = entry.getKey();

				if (key.startsWith(ConfigurationConstants.EventDataKeys.Configuration.ENVIRONMENT_PREFIX_DELIMITER)) {
					continue;
				}

				// if the environment-specific key doesn't exist, use the original
				String environmentAwareKey = getKeyForEnvironment(key, buildEnvironment);

				if (!variantMap.containsKey(environmentAwareKey)) {
					environmentAwareKey = key;
				}

				Variant value = variantMap.get(environmentAwareKey);

				if (value != null) {
					internalMap.put(key, value);
				}
			}
		} catch (VariantException exception) {
			Log.debug(ConfigurationExtension.LOG_SOURCE, "Unable to parse the Configuration from JSON Object. Exception: (%s)",
					  exception);
		}

		return this;
	}

	/**
	 * Creates a Event Data from the given ConfigData.
	 *
	 * @return the EventData loaded with configuration.
	 */
	EventData getEventData() {
		EventData eventData = new EventData();

		for (Map.Entry<String, Variant> kv : internalMap.entrySet()) {
			eventData.putVariant(kv.getKey(), kv.getValue());
		}

		return eventData;
	}

	/**
	 * Returns the configuration from ConfigData as a JSONString
	 *
	 * @return A JSON String.
	 */
	String getJSONString() {
		try {
			final Variant variant = Variant.fromVariantMap(internalMap);
			final JsonUtilityService.JSONObject jsonObject = variant.getTypedObject(new JsonObjectVariantSerializer(
						jsonUtilityService));
			return jsonObject.toString();
		} catch (Exception exception) {
			Log.debug(ConfigurationExtension.LOG_SOURCE, "Unable create a JSON from ConfigurationData. Exception: (%s)",
					  exception);
			return null;
		}
	}

	/**
	 * Returns true if this ConfigurationData instance contains no entries.
	 * @return true if this ConfigurationData instance contains no entries, false otherwise.
	 */
	boolean isEmpty() {
		return internalMap.isEmpty();
	}

	/**
	 * Returns the number of entries in this ConfigurationData instance
	 *
	 * @return the number of entries in this ConfigurationData instance
	 */
	private int size() {
		return internalMap.size();
	}

	@Override
	public String toString() {
		return internalMap.toString();
	}

	/**
	 * Returns the correct key from configuration json based on the build environment and base key
	 *
	 * For configuration, the base_key will always be the name of the production configuration value. e.g. :
	 * - Production Key  -  myKeyName
	 * - Staging Key     -  __stage__myKeyName
	 * - Development Key -  __dev__myKeyName
	 *
	 * @param baseKey the production key name to use as the base for the result
	 * @param environment the value from build.environment in the configuration json provided by Launch
	 * @return a string representing the correct key to use given the provided environment
	 */
	String getKeyForEnvironment(final String baseKey, final String environment) {
		return environment.isEmpty() ? baseKey : ConfigurationConstants.EventDataKeys.Configuration.ENVIRONMENT_PREFIX_DELIMITER
			   + environment + ConfigurationConstants.EventDataKeys.Configuration.ENVIRONMENT_PREFIX_DELIMITER +
			   baseKey;
	}
}
