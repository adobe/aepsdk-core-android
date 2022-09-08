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
import java.util.Map;

/**
 * VariantSerializer implementation for typed maps.
 *
 * Used by {@link Variant} and {@link EventData} implementations of get/put/opt operations on
 * typed maps.
 */
final class TypedMapVariantSerializer<T> implements VariantSerializer<Map<String, T>> {
	private final VariantSerializer<T> elementSerializer;

	/**
	 * Constructor.
	 *
	 * @param elementSerializer {@code VariantSerializer<T>} serializer for entry values
	 * @throws IllegalArgumentException if {@code elementSerializer} is null
	 */
	public TypedMapVariantSerializer(final VariantSerializer<T> elementSerializer) {
		if (elementSerializer == null) {
			throw new IllegalArgumentException();
		}

		this.elementSerializer = elementSerializer;
	}

	@Override
	public Variant serialize(final Map<String, T> customMap) {
		return serializeMap(customMap);
	}

	/**
	 * Serializes {@code Map<String, ? extends T>} to a {@code Variant}.
	 *
	 * Null keys will be skipped.
	 * Null values will be deserialized to `VariantKind.NULL`.
	 * Entries where serialization fails will be omitted from the resulting map.
	 *
	 * @param customMap {@code Map<String, ? extends T>} to serialize
	 * @return serialization of {@code customMap} to a map variant, or a null variant if
	 * {@code customMap} is null
	 */
	public Variant serializeMap(final Map<String, ? extends T> customMap) {
		final Map<String, Variant> serializedMap = serializeMapToVariantMap(customMap);
		return Variant.fromVariantMap(serializedMap);
	}

	/**
	 * Serializes {@code Map<String, ? extends T>} to a {@code Map<String, Variant>}.
	 *
	 * Null keys will be skipped.
	 * Null values will be deserialized to `VariantKind.NULL`.
	 * Entries where serialization fails will be omitted from the resulting map.
	 *
	 * @param customMap {@code Map<String, ? extends T>} to serialize
	 * @return serialization of {@code customMap} to {@code Map<String,Variant>}, or null if
	 * {@code customMap} is null
	 */
	public Map<String, Variant> serializeMapToVariantMap(final Map<String, ? extends T> customMap) {
		if (customMap == null) {
			return null;
		}

		final Map<String, Variant> variantMap = new HashMap<String, Variant>();

		for (final Map.Entry<String, ? extends T> entry : customMap.entrySet()) {
			final String key = entry.getKey();

			if (key == null) {
				continue;
			}

			final T value = entry.getValue();
			Variant serializedValue;

			if (value == null) {
				serializedValue = Variant.fromNull();
			} else {
				try {
					serializedValue = Variant.fromTypedObject(value, elementSerializer);
				} catch (VariantException ex) {
					continue;
				}
			}

			variantMap.put(key, serializedValue);
		}

		return variantMap;
	}

	@Override
	public Map<String, T> deserialize(final Variant variant) throws VariantException {
		if (variant == null) {
			throw new IllegalArgumentException();
		}

		if (variant.getKind() == VariantKind.NULL) {
			return null;
		}

		final Map<String, Variant> serializedMap = variant.getVariantMap();
		return deserializeMap(serializedMap);
	}

	/**
	 * Deserializes {@code Map<String, Variant>} to a {@code Map<String, T>}.
	 *
	 * Null keys will be skipped.
	 * Null and `VariantKind.NULL` values will be deserialized to null.
	 * Entries where deserialization fails will be omitted from the resulting map.
	 *
	 * @param variantMap {@code Map<String, Variant>} to deserialize
	 * @return deserialization of {@code variantMap} to {@code Map<String,T>}, or null if
	 * {@code variantMap} is null
	 */
	public Map<String, T> deserializeMap(final Map<String, Variant> variantMap) {
		if (variantMap == null) {
			return null;
		}

		final Map<String, T> customMap = new HashMap<String, T>();

		for (final Map.Entry<String, Variant> entry : variantMap.entrySet()) {
			final String key = entry.getKey();

			if (key == null) {
				continue;
			}

			final Variant serializedValue = entry.getValue();

			T value;

			if (serializedValue == null || serializedValue.getKind() == VariantKind.NULL) {
				value = null;
			} else {
				try {
					value = serializedValue.getTypedObject(elementSerializer);
				} catch (VariantException ex) {
					continue;
				}
			}

			customMap.put(key, value);
		}

		return customMap;
	}
}
