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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * VariantSerializer implementation for {@code Object}.
 *
 * <p>This serializer performs the following conversions:</p>
 * <ul>
 *     <li>{@code Double} serializes to a double variant, and vice versa.</li>
 *     <li>{@code String} serializes to a string variant, and vice versa.</li>
 *     <li>{@code Integer} serializes to an integer variant, and vice versa.</li>
 *     <li>{@code Long} serializes to a long variant, and vice versa.</li>
 *     <li>{@code Boolean} serializes to a boolean variant, and vice versa.</li>
 *     <li>{@code Map<String, Object>} serializes to a map variant, and vice versa.</li>
 *     <li>{@code List<Object>} serializes to a vector variant, and vice versa.</li>
 *     <li>{@code Collection<Object>} serializes to a vector variant.</li>
 *     <li>{@code Byte} and {@code Short} serializes to an integer variant.</li>
 *     <li>{@code Float} serializes to a double variant.</li>
 *     <li>{@code Variant} serializes to itself.</li>
 *     <li>Null variants deserialize to null.</li>
 *     <li>Other classes are skipped.</li>
 * </ul>
 *
 * The behavior when encountering null during serialization or deserialization is controlled by
 * {@code PermissiveVariantSerializer.NullKeyBehavior} and
 * {@code PermissiveVariantSerializer.NullValueBehavior}.
 *
 */
final class PermissiveVariantSerializer implements VariantSerializer<Object> {
	public static final PermissiveVariantSerializer DEFAULT_INSTANCE = new PermissiveVariantSerializer();
	public static final int MAX_DEPTH = 256;

	private static final String LOG_TAG = PermissiveVariantSerializer.class.getSimpleName();

	/**
	 * Constructs an instance.
	 */
	private PermissiveVariantSerializer() {
	}

	@Override
	public Variant serialize(final Object value) throws VariantException {
		return serialize(value, 0);
	}

	private Variant serialize(final Object value, final int currentDepth) throws VariantException {
		if (value == null) {
			return Variant.fromNull();
		}

		if (currentDepth > MAX_DEPTH) {
			// prevent infinite recursion
			throw new VariantSerializationFailedException("infinite recursion");
		} else if (value instanceof Variant) {
			return (Variant) value; // pass through variants
		} else if (value instanceof Integer) {
			return Variant.fromInteger((Integer) value);
		} else if (value instanceof Long) {
			return Variant.fromLong((Long) value);
		} else if (value instanceof Double) {
			return Variant.fromDouble((Double) value);
		} else if (value instanceof Float) {
			return Variant.fromDouble((Float) value);
		} else if (value instanceof Short) {
			return Variant.fromInteger((Short) value);
		} else if (value instanceof Byte) {
			return Variant.fromInteger((Byte) value);
		} else if (value instanceof String) {
			return Variant.fromString((String) value);
		} else if (value instanceof Boolean) {
			return Variant.fromBoolean((Boolean) value);
		} else if (value instanceof Map) {
			final Map<String, Variant> variantMap = serializeToVariantMap((Map<?, ?>)value, currentDepth);
			return Variant.fromVariantMap(variantMap);
		} else if (value instanceof Collection) {
			final Collection<?> valueAsCollection = (Collection<?>) value;
			List<Variant> variantList = new ArrayList<Variant>();

			for (final Object element : valueAsCollection) {
				final Variant elementAsVariant = serialize(element, currentDepth + 1);
				variantList.add(elementAsVariant);
			}

			return Variant.fromVariantList(variantList);
		} else {
			return new Variant.ObjectVariant(value); // temporary
		}
	}

	Map<String, Variant> serializeToVariantMap(final Map<?, ?> map) throws VariantException {
		return serializeToVariantMap(map, 0);
	}

	private Map<String, Variant> serializeToVariantMap(final Map<?, ?> map,
			final int currentDepth) throws VariantException {
		final Map<?, ?> valueAsMap = map;
		final Map<String, Variant> variantMap = new HashMap<String, Variant>();

		for (final Map.Entry<?, ?> entry : valueAsMap.entrySet()) {
			final Object key = entry.getKey();

			if (key == null) {
				continue;
			}

			String keyString;

			if (key instanceof String) {
				keyString = (String) key;
			} else {
				keyString = key.toString();
			}

			if (keyString == null) {
				continue;
			}

			final Object entryValue = entry.getValue();
			final Variant entryValueAsVariant = serialize(entryValue, currentDepth + 1);
			variantMap.put(keyString, entryValueAsVariant);
		}

		return variantMap;
	}

	@Override
	public Object deserialize(final Variant variant) throws VariantException {
		if (variant == null) {
			throw new IllegalArgumentException();
		}

		final VariantKind variantKind = variant.getKind();

		switch (variantKind) {
			case NULL:
				return null;

			case INTEGER:
				return variant.getInteger();

			case LONG:
				return variant.getLong();

			case DOUBLE:
				return variant.getDouble();

			case BOOLEAN:
				return variant.getBoolean();

			case STRING:
				return variant.getString();

			case MAP:
				final Map<String, Object> objectMap = new HashMap<String, Object>();
				final Map<String, Variant> variantMap = variant.getVariantMap();

				for (final Map.Entry<String, Variant> entry : variantMap.entrySet()) {
					final String entryKey = entry.getKey();
					final Variant entryValue = entry.getValue();
					final Object deserializedEntryValue = deserialize(entryValue);
					objectMap.put(entryKey, deserializedEntryValue);
				}

				return objectMap;

			case VECTOR:
				final List<Object> objectList = new ArrayList<Object>();
				final List<Variant> variantList = variant.getVariantList();

				for (final Variant element : variantList) {
					final Object deserializedElement = deserialize(element);
					objectList.add(deserializedElement);
				}

				return objectList;

			case OBJECT:
				return variant.getObject();

			default:
				throw new VariantSerializationFailedException("unexpected variant kind: " + variantKind); // shouldn't happen
		}
	}

	/**
	 * Deserializes provided {@code Map<String, Variant>} into a {@code Map<String, Object>}. It only supports the types
	 * listed in the {@code PermissiveVariantSerializer} class; {@code NullVariant} values are deserialized in null values.
	 * Null keys and unsupported Object type values are skipped.
	 *
	 * @param map {@code Map<String, Variant>} to deserialize
	 * @return {@code Map<String, Object>} containing the deserialized content or null if the provided map is null
	 * @see PermissiveVariantSerializer
	 */
	Map<String, Object> deserializeToObjectMap(final Map<String, Variant> map) {
		if (map == null) {
			return null;
		}

		Map<String, Object> result = new HashMap<String, Object>();

		for (final Map.Entry<String, Variant> entry : map.entrySet()) {
			final String key = entry.getKey();

			if (key == null) {
				continue;
			}

			final Variant entryValue = entry.getValue();
			final Object value;

			try {
				value = deserialize(entryValue);
				result.put(key, value);
			} catch (final IllegalArgumentException e) {
				Log.debug(LOG_TAG, "Unable to deserialize value for key %s, value was null, pair will be skipped, %s", key, e);
			} catch (final VariantException e) {
				Log.debug(LOG_TAG, "Unable to deserialize value for key %s, value has an unknown type, pair will be skipped, %s", key,
						  e);
			}
		}

		return result;
	}
}

