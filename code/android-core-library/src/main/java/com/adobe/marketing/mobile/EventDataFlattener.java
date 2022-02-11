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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides methods to "flatten" {@code EventData} objects with nested structures into {@code Map} objects.
 */
class EventDataFlattener {
	private static final String LOG_TAG = EventDataFlattener.class.getSimpleName();

	private static final String NAMESPACE_DELIMITER = ".";

	/**
	 * Private constructor.
	 */
	private EventDataFlattener()
	{}

	/**
	 * Returns a {@code Map<String, Object>} which has been flattened in the following way:
	 * The Keys are of the format "eventdataKey"."nestedMapKeyOrFlatDictionaryKey"....
	 * So For example, an event data in the following format:
	 * {@literal [mapKey]--> [key1-->value1]
	 *            [key2-->value2]}
	 * will be flattened to
	 * {@literal [mapKey.key1]--> [value1]}
	 * {@literal [mapKey.key2]--> [value2]}
	 *
	 * @param eventData the {@link EventData} to flatten
	 * @return Flattened KV pairs
	 */
	public static Map<String, Variant> getFlattenedDataMap(final EventData eventData) {
		Map<String, Variant> dataMap = new HashMap<String, Variant>();

		if (eventData != null) {
			for (String key : eventData.keys()) {
				Variant value;

				try {
					value = eventData.getVariant(key);
				} catch (VariantException ex) {
					// TODO: - Do not log the eventData if it contains PII
					Log.warning(LOG_TAG,
								"Unexpected exception in EventDataFlattener.getFlattenedDataMap: eventData = %s, key = %s, ex = %s",
								eventData, key, ex);
					continue;
				}

				dataMap.putAll(flatten(key, value));
			}
		}

		return dataMap;
	}

	/**
	 * Returns a {@code Map<String, Object>} which has been flattened in the following way:
	 * The Keys are of the format "eventdataKey"."nestedMapKeyOrFlatMapKey"....
	 * So For example, an event data in the following format:
	 * {@literal [mapKey]--> [key1-->value1]
	 *            [key2-->value2]}
	 * will be flattened to
	 * {@literal [mapKey.key1]--> [value1]}
	 * {@literal [mapKey.key2]--> [value2]}
	 *
	 * @param mask the {@link Map<String, Variant>} to flatten
	 * @return Flattened KV pairs
	 */
	public static Map<String, Variant> getFlattenedEventDataMask(final Map<String, Variant> mask) {
		final Map<String, Variant> dataMap = new HashMap<String, Variant>();

		if (mask != null && !mask.isEmpty()) {
			for (final String key : mask.keySet()) {
				Variant value;
				value = mask.get(key);
				dataMap.putAll(flatten(key, value));
			}
		}

		return dataMap;
	}

	/**
	 * Inserts the {@code value} into a Map by flattening the value if needed.
	 *
	 * If the value is a Map it will be flattened (the namespaced key will be prefixed to the nested keys).
	 * will be inserted (the namespaced key will be prefixed to the keys).
	 * Else, the value will be inserted as is with the key being the {@code namespaced key}
	 *
	 * @param nameSpacedKey The key (which has been appropriately namespaced) that will be used as the key for the
	 *                         value inserted.
	 *                      If the value is further flattened for inserting into the returned map, then the key will
	 *                      be used as the
	 *                      prefix for the keys in the flattened representation.
	 * @param value         The Object that will be inserted into the map after flattening (if needed)
	 *
	 * @return A Map containing the value (flattened if required)
	 */
	private static Map<String, Variant> flatten(final String nameSpacedKey, final Variant value) {

		Map<String, Variant> dataMap = new HashMap<String, Variant>();

		// add current entry to the map
		dataMap.put(nameSpacedKey, value);

		// flatten it if it is a map
		if (value.getKind() == VariantKind.MAP) {
			Map<String, Variant> valueAsVariantMap;

			try {
				valueAsVariantMap = value.getVariantMap();
			} catch (VariantException ex) {
				Log.warning(LOG_TAG, "Unexpected exception in EventDataFlattener.flatten: nameSpacedKey = %s, value = %s, ex = %s",
							nameSpacedKey, value, ex);
				return Collections.emptyMap();
			}

			dataMap.putAll(flatten(nameSpacedKey, valueAsVariantMap));
		}


		return dataMap;

	}

	/**
	 * Returns a map after flattening the {@code mapValue}. It is done by recursively calling
	 * {@link #flatten(String, Variant)}
	 *
	 * @param keyPrefix The string that will be prefixed to the map keys.
	 * @param mapValue  The map that will be flattened.
	 *
	 * @return The flattened map.
	 */
	private static Map<String, Variant> flatten(final String keyPrefix, final Map<String, Variant> mapValue) {
		Map<String, Variant> dataMap = new HashMap<String, Variant>();

		if (mapValue != null) {
			for (Map.Entry<String, Variant> entry : mapValue.entrySet()) {
				Variant value = entry.getValue();
				String key = entry.getKey() == null ? "" : entry.getKey();
				dataMap.putAll(flatten(keyPrefix + NAMESPACE_DELIMITER + key, value));
			}
		}

		return dataMap;
	}
}
