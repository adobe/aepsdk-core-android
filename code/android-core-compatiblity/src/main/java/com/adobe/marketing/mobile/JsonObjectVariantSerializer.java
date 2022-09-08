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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * {@code VariantSerializer} implementation for {@code JsonUtilityService.JSONObject}.
 */
public final class JsonObjectVariantSerializer implements VariantSerializer<JsonUtilityService.JSONObject> {
	private final JsonUtilityService jsonUtilityService;
	private final JsonValueVariantSerializer valueVariantSerializer;

	/**
	 * Constructor.
	 *
	 * @param jsonUtilityService {@code JsonUtilityService} to use when deserializing
	 */
	public JsonObjectVariantSerializer(final JsonUtilityService jsonUtilityService) {
		if (jsonUtilityService == null) {
			throw new IllegalArgumentException();
		}

		this.jsonUtilityService = jsonUtilityService;
		valueVariantSerializer = new JsonValueVariantSerializer(jsonUtilityService);
	}


	@Override
	public Variant serialize(final JsonUtilityService.JSONObject jsonValueAsJsonObject) throws VariantException {
		if (jsonValueAsJsonObject == null) {
			return Variant.fromNull();
		}

		final Map<String, Variant> serializedMap = new HashMap<String, Variant>();
		final Iterator<String> keysIterator = jsonValueAsJsonObject.keys();

		while (keysIterator.hasNext()) {
			final String key = keysIterator.next();

			if (key == null) {
				continue; // skip null keys
			}

			Object entryValue;

			try {
				entryValue = jsonValueAsJsonObject.get(key);
			} catch (JsonException ex) {
				throw new VariantSerializationFailedException(ex);
			}

			final Variant serializedEntryValue = valueVariantSerializer.serialize(entryValue);
			serializedMap.put(key, serializedEntryValue);
		}

		return Variant.fromVariantMap(serializedMap);
	}

	@Override
	public JsonUtilityService.JSONObject deserialize(final Variant variant) throws VariantException {
		if (variant == null) {
			throw new IllegalArgumentException();
		}

		if (variant.getKind() == VariantKind.NULL) {
			return null;
		}

		final JsonUtilityService.JSONObject jsonObject = jsonUtilityService.createJSONObject(new HashMap<String, Object>());
		final Map<String, Variant> variantMap = variant.getVariantMap();

		for (Map.Entry<String, Variant> entry : variantMap.entrySet()) {
			final String key = entry.getKey();
			final Variant value = entry.getValue();
			final Object jsonValueAsObject = valueVariantSerializer.deserialize(value);

			// JsonValueVariantSerializer may deserialize to null
			try {
				jsonObject.put(key, jsonValueAsObject);
			} catch (JsonException ex) {
				throw new VariantSerializationFailedException(ex);
			}
		}

		return jsonObject;
	}
}
