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

import com.adobe.marketing.mobile.internal.utility.StringEncoder;

import java.util.*;

/**
 * An {@code EventData} is data that can be included with events or shared states.
 *
 * An {@link EventData} is a mutable map of {@code String} keys to {@link Variant} values.
 *
 * <p>The value of an {@code EventData} key can be obtained in multiple ways:</p>
 * <ul>
 *     <li>
 *         The {@code EventData.optXyz(...)} methods are typically the best choice for users. These
 *         methods return the value as an {@code xyz}. If the value is missing or is not an
 *         {@code xyz}, the method will return a default value. Implicit conversions between types
 *         are not performed, except between numeric types.
 *     </li>
 *     <li>
 *         The {@code EventData.getXyz(...)} methods return the value as an {@code xyz}. If the value
 *         is missing or is not an {@code xyz} the method will throw. Implicit conversions between
 *         types are not performed, except between numeric types.
 *     </li>
 * </ul>
 *
 * <p>The value of an {@code EventData} key can be set using the {@code EventData.putXyz(...)} methods.</p>
 *
 * @author Adobe Systems Incorporated
 * @version 5.0
 */
class EventData {
	private static final String LOG_TAG = "EventData";
	private final Map<String, Variant> internalMap;

	/**
	 * Creates an {@code EventData} instance
	 */
	EventData() {
		this.internalMap = new HashMap<String, Variant>();
	}

	/**
	 * Creates an {@code EventData} instance
	 *
	 * @param eventData {@link EventData} the source where the data is copied from
	 * @throws IllegalArgumentException if {@code eventData} is null
	 */
	EventData(final EventData eventData) {
		this();

		if (eventData == null) {
			throw new IllegalArgumentException();
		}

		this.internalMap.putAll(eventData.internalMap);
	}

	/**
	 * Creates an EventData instance
	 *
	 * Entries with null keys will be skipped.
	 * Null values will be converted to {@code Variant.NULL} values.
	 *
	 * @param map {@code Map<String, Variant>} the source where the data is copied from
	 * @throws IllegalArgumentException if {@code map} is null
	 */
	EventData(final Map<String, Variant> map) {
		this();

		if (map == null) {
			throw new IllegalArgumentException();
		}

		for (final Map.Entry<String, Variant> entry : map.entrySet()) {
			final String key = entry.getKey();

			if (key == null) {
				continue; // do not allow null keys
			}

			final Variant entryValue = entry.getValue();
			final Variant value = entryValue == null ? Variant.fromNull() : entryValue;
			this.internalMap.put(key, value);
		}
	}

	/**
	 * Copies provided {@code Map<String, Object>} into an {@code EventData} object.  It only supports the types listed
	 * in the {@code PermissiveVariantSerializer} class, unsupported Object types are skipped. Null keys are skipped,
	 * and null values are serialized in {@code NullVariant}.
	 *
	 * @param objectMap {@code Map<String, Object>} to be converted to {@code Map<String, Variant>} and assigned to the
	 * returned {@link EventData}
	 * @return {@link EventData} containing the keys and values from the provided map
	 * @throws VariantException when the serialization fails
	 * @see PermissiveVariantSerializer
	 * @see PermissiveVariantSerializer#serializeToVariantMap(Map)
	 */
	static EventData fromObjectMap(final Map<String, ?> objectMap) throws VariantException {
		final Map<String, Variant> variantMap = PermissiveVariantSerializer.DEFAULT_INSTANCE.serializeToVariantMap(objectMap);
		return new EventData(variantMap);
	}

	/**
	 * Copies current {@code EventData} into a {@code Map<String, Object>}. It only supports the types listed in the
	 * {@code PermissiveVariantSerializer} class; {@code NullVariant} values are deserialized in null values.
	 * Null keys and unsupported Object type values are skipped.
	 *
	 * @return 	{@code Map<String, Object>} containing the deserialized {@code EventData} content
	 * @see PermissiveVariantSerializer
	 * @see PermissiveVariantSerializer#deserializeToObjectMap(Map)
	 */
	Map<String, Object> toObjectMap() {
		return PermissiveVariantSerializer.DEFAULT_INSTANCE.deserializeToObjectMap(this.internalMap);
	}

	/**
	 * Checks if this is empty.
	 *
	 * @return whether this contains any entries.
	 */
	boolean isEmpty() {
		return this.internalMap.isEmpty();
	}

	/**
	 * Checks how many entries this contains.
	 *
	 * @return the number of key-value mappings in this map
	 */
	int size() {
		return this.internalMap.size();
	}

	/**
	 * Gets the kind of value associated with a key.
	 *
	 * @param key {@code String} key to query
	 * @return {@link VariantKind} of the value associated with {@code key}
	 * @throws IllegalArgumentException if {@code key} is null
	 * @throws VariantKeyNotFoundException if {@code key} is not present
	 */
	VariantKind getKind(final String key) throws VariantException {
		if (key == null) {
			throw new IllegalArgumentException();
		}

		final Variant variantValue = getVariant(key); // throws on missing key
		return variantValue.getKind();
	}

	/**
	 * Checks if a key exists in this.
	 *
	 * @param key {@code String} key to query
	 * @return whether a value is associated with {@code key}
	 * @throws IllegalArgumentException if {@code key} is null
	 */
	boolean containsKey(final String key) {
		if (key == null) {
			throw new IllegalArgumentException();
		}

		return this.internalMap.containsKey(key);
	}

	/**
	 * Copies this into a {@code Map<String, Variant>}.
	 *
	 * @return a {@code Map<String, Variant>} containing the same keys and values as this
	 */
	Map<String, Variant> asMapCopy() {
		return new HashMap<String, Variant>(internalMap);
	}

	/**
	 * Copies of this instance.
	 * @return a copy of this instance.
	 */
	EventData copy() {
		EventData data = new EventData(this.asMapCopy());
		return data;
	}

	/**
	 * Sets the value for {@code key} to a {@code Variant}.
	 *
	 * If this previously contained a value for {@code key}, the old value will be replaced by the
	 * new specified value.
	 *
	 * @param key {@code String} key to assign
	 * @param value {@link Variant} value to assign,
	 *              or null to assign {@link VariantKind#NULL}
	 * @return this
	 * @throws IllegalArgumentException if {@code key} is null
	 */
	EventData putVariant(final String key, final Variant value) {
		if (key == null) {
			throw new IllegalArgumentException();
		}

		if (value == null) {
			this.internalMap.put(key, Variant.fromNull());
		} else {
			this.internalMap.put(key, value);
		}

		return this;
	}

	/**
	 * Sets the value for {@code key} to a variant containing null.
	 *
	 * If this previously contained a value for {@code key}, the old value will be replaced
	 * `VariantKind.NULL`.
	 *
	 * @param key {@code String} key to set
	 * @return this
	 * @throws IllegalArgumentException if {@code key} is null
	 *
	 * @see #putVariant
	 * @see Variant#fromNull
	 */
	EventData putNull(final String key) {
		return putVariant(key, Variant.fromNull());
	}

	/**
	 * Sets the value for {@code key} to a variant containing a {@code String}.
	 *
	 * If this previously contained a value for {@code key}, the old value will be replaced by the
	 * new specified value.
	 *
	 * @param key {@code String} key to assign
	 * @param value {@code String} value to assign,
	 *              or null to assign {@link VariantKind#NULL}
	 * @return this
	 * @throws IllegalArgumentException if {@code key} is null
	 *
	 * @see #putVariant
	 * @see Variant#fromString
	 */
	EventData putString(final String key, final String value) {
		return putVariant(key, Variant.fromString(value));
	}

	/**
	 * Sets the value for {@code key} to a variant containing an {@code int}.
	 *
	 * If this previously contained a value for {@code key}, the old value will be replaced by the
	 * new specified value.
	 *
	 * @param key {@code String} key to assign
	 * @param value {@code int} value to assign
	 * @return this
	 * @throws IllegalArgumentException if {@code key} is null
	 *
	 * @see #putVariant
	 * @see Variant#fromInteger
	 */
	EventData putInteger(final String key, final int value) {
		return putVariant(key, Variant.fromInteger(value));
	}

	/**
	 * Sets the value for {@code key} to a variant containing a {@code long}.
	 *
	 * If this previously contained a value for {@code key}, the old value will be replaced by the
	 * new specified value.
	 *
	 * @param key {@code String} key to assign
	 * @param value {@code long} value to assign
	 * @return this
	 * @throws IllegalArgumentException if {@code key} is null
	 *
	 * @see Variant#fromLong
	 */
	EventData putLong(final String key, final long value) {
		return putVariant(key, Variant.fromLong(value));
	}

	/**
	 * Sets the value for {@code key} to a variant containing a {@code boolean}.
	 *
	 * If this previously contained a value for {@code key}, the old value will be replaced by the
	 * new specified value.
	 *
	 * @param key {@code String} key to assign
	 * @param value {@code boolean} value to assign
	 * @return this
	 * @throws IllegalArgumentException if {@code key} is null
	 *
	 * @see #putVariant
	 * @see Variant#fromBoolean
	 */
	EventData putBoolean(final String key, final boolean value) {
		return putVariant(key, Variant.fromBoolean(value));
	}

	/**
	 * Sets the value for {@code key} to a variant containing a {@code double}.
	 *
	 * If this previously contained a value for {@code key}, the old value will be replaced by the
	 * new specified value.
	 *
	 * @param key {@code String} key to assign
	 * @param value {@code double} value to assign
	 * @return this
	 * @throws IllegalArgumentException if {@code key} is null
	 *
	 * @see Variant#fromDouble
	 */
	EventData putDouble(final String key, final double value) {
		return putVariant(key, Variant.fromDouble(value));
	}

	/**
	 * Sets the value for {@code key} to a variant containing a {@code Map<String, String>}.
	 *
	 * If this previously contained a value for {@code key}, the old value will be replaced by the
	 * new specified value.
	 *
	 * Entries with null keys will be skipped.
	 * Null values will be converted to {@link VariantKind#NULL}.
	 *
	 * @param key {@code String} key to assign
	 * @param value {@code Map<String, String>} value to assign,
	 *              or null to assign {@link VariantKind#NULL}
	 * @return this
	 * @throws IllegalArgumentException if {@code key} is null
	 *
	 * @see #putVariant
	 * @see Variant#fromStringMap
	 */
	EventData putStringMap(final String key, final Map<String, String> value) {
		return putVariant(key, Variant.fromStringMap(value));
	}

	/**
	 * Sets the value for {@code key} to a variant containing a {@code List<String>}.
	 *
	 * If this previously contained a value for {@code key}, the old value will be replaced by the
	 * new specified value.
	 *
	 * Null elements will be converted to {@link VariantKind#NULL}.
	 *
	 * @param key {@code String} key to assign
	 * @param value {@code List<String>} value to assign,
	 *              or null to assign {@link VariantKind#NULL}
	 * @return this
	 * @throws IllegalArgumentException if {@code key} is null
	 *
	 * @see #putVariant
	 * @see Variant#fromStringList
	 */
	EventData putStringList(final String key, final List<String> value) {
		return putVariant(key, Variant.fromStringList(value));
	}

	/**
	 * Sets the value for {@code key} to a variant containing a {@code Map<String, Variant>}.
	 *
	 * If this previously contained a value for {@code key}, the old value will be replaced by the
	 * new specified value.
	 *
	 * Entries with null keys will be skipped.
	 * Null values will be converted to {@link VariantKind#NULL}.
	 *
	 * @param key {@code String} key to assign
	 * @param value {@code Map<String, Variant>} value to assign,
	 *              or null to assign {@link VariantKind#NULL}
	 * @return this
	 * @throws IllegalArgumentException if {@code key} is null
	 *
	 * @see #putVariant
	 * @see Variant#fromVariantMap
	 */
	EventData putVariantMap(final String key, final Map<String, Variant> value) {
		return putVariant(key, Variant.fromVariantMap(value));
	}

	/**
	 * Sets the value for {@code key} to a variant containing a {@code List<Variant>}.
	 *
	 * If this previously contained a value for {@code key}, the old value will be replaced by the
	 * new specified value.
	 *
	 * Null elements will be converted to {@link VariantKind#NULL}.
	 *
	 * @param key {@code String} key to assign
	 * @param value {@code Map<String, Variant>} value to assign,
	 *              or null to assign {@link VariantKind#NULL}
	 * @return this
	 * @throws IllegalArgumentException if {@code key} is null
	 *
	 * @see #putVariant
	 * @see Variant#fromVariantList
	 */
	EventData putVariantList(final String key, final List<Variant> value) {
		return putVariant(key, Variant.fromVariantList(value));
	}

	/**
	 * Sets the value for {@code key} to a variant serialized from a custom object.
	 *
	 * If this previously contained a value for {@code key}, the old value will be replaced by the
	 * new specified value.
	 *
	 * @param <T> type of {@code value}
	 * @param key {@code String} key to assign
	 * @param value {@code T} custom object to serialize,
	 *              or null to assign {@link VariantKind#NULL}
	 * @param serializer {@code VariantSerializer<T>} serializer to convert a {@code T} to a {@link Variant}
	 * @return this
	 * @throws IllegalArgumentException if {@code key} or {@code serializer} is null
	 * @throws VariantSerializationFailedException if serialization fails
	 *
	 * @see #putVariant
	 * @see Variant#fromTypedObject
	 * @see VariantSerializer
	 */
	<T> EventData putTypedObject(final String key, final T value,
								 final VariantSerializer<T> serializer) throws VariantException {
		return putVariant(key, Variant.fromTypedObject(value, serializer));
	}

	/**
	 * Sets the value for {@code key} to a variant serialized from a list of custom objects.
	 *
	 * If this previously contained a value for {@code key}, the old value will be replaced by the
	 * new specified value.
	 *
	 * Elements in {@code value} that fail to serialize will be skipped.
	 * Null elements will be converted to {@link VariantKind#NULL}.
	 *
	 * @param <T> element type of {@code list}
	 * @param key {@code String} key to assign
	 * @param list {@code List<T>} list of custom objects to serialize,
	 *              or null to assign {@link VariantKind#NULL}
	 * @param serializer {@code VariantSerializer<T>} serializer to convert a {@code T} to a {@code Variant}
	 * @return this
	 * @throws IllegalArgumentException if {@code key}, {@code value}, or {@code serializer} is null
	 *
	 * @see #putVariant
	 * @see Variant#fromTypedList
	 * @see VariantSerializer
	 */
	<T> EventData putTypedList(final String key, final List<T> list, final VariantSerializer<T> serializer) {
		return putVariant(key, Variant.fromTypedList(list, serializer));
	}

	/**
	 * Sets the value for {@code key} to a variant serialized from a map of custom objects.
	 *
	 * If this previously contained a value for {@code key}, the old value will be replaced by the
	 * new specified value.
	 *
	 * Entries in {@code value} with null keys will be skipped.
	 * Entries with values that fail to serialize will be skipped.
	 * Null values will be converted to {@link VariantKind#NULL}.
	 *
	 * @param <T> value type of {@code map}
	 * @param key {@code String} key to assign
	 * @param map {@code Map<String, T>} map of custom objects to serialize,
	 *              or null to assign {@link VariantKind#NULL}
	 * @param serializer {@code Map<String, Variant>} serializer to convert a {@code T} to a {@link Variant}
	 * @return this
	 * @throws IllegalArgumentException if {@code key} or {@code serializer} is null
	 *
	 * @see #putVariant
	 * @see Variant#fromTypedMap
	 * @see VariantSerializer
	 */
	<T> EventData putTypedMap(final String key, final Map<String, T> map, final VariantSerializer<T> serializer) {
		return putVariant(key, Variant.fromTypedMap(map, serializer));
	}

	/**
	 * Gets the value for {@code key} as a {@code Variant}.
	 *
	 * @param key {@code String} key to fetch
	 * @return value of the specified key
	 * @throws IllegalArgumentException if {@code key} is null
	 * @throws VariantKeyNotFoundException if the key was not found
	 */
	Variant getVariant(final String key) throws VariantException {
		return Variant.getVariantFromMap(internalMap, key);
	}

	/**
	 * Gets the value for {@code key} as a {@code String}.
	 *
	 * Equivalent to: {@code getVariant(key).getString()}
	 *
	 * @param key {@code String} key to fetch
	 * @return value of the specified key as a {@code String}
	 * @throws IllegalArgumentException if {@code key} is null
	 * @throws VariantKeyNotFoundException if the key was not found
	 * @throws VariantKindException if the value kind was not {@link VariantKind#STRING}
	 *
	 * @see Variant#getString
	 */
	String getString2(final String key) throws VariantException {
		return getVariant(key).getString();
	}

	/**
	 * Gets the value for {@code key} as an {@code int}.
	 *
	 * Equivalent to: {@code getVariant(key).getInteger()}
	 *
	 * This method performs conversions between numeric types ({@link VariantKind#INTEGER},
	 * {@link VariantKind#LONG}, and {@link VariantKind#DOUBLE}). See {@link Variant#getInteger}.
	 *
	 * @param key {@code String} key to fetch
	 * @return value associated with {@code key} as an {@code int}
	 * @throws IllegalArgumentException if {@code key} is null
	 * @throws VariantKeyNotFoundException if the key was not found
	 * @throws VariantKindException if the value kind is not {@link VariantKind#INTEGER},
	 * {@link VariantKind#LONG}, or {@link VariantKind#DOUBLE}
	 * @throws VariantRangeException if the value kind is {@link VariantKind#LONG} or
	 * {@link VariantKind#DOUBLE}, but the value is not expressible as an {@code int}
	 *
	 * @see Variant#getInteger
	 */
	int getInteger(final String key) throws VariantException {
		return getVariant(key).getInteger();
	}

	/**
	 * Gets the value for {@code key} as a {@code long}.
	 *
	 * Equivalent to: {@code getVariant(key).getLong()}
	 *
	 * This method performs conversions between numeric types ({@link VariantKind#INTEGER},
	 * {@link VariantKind#LONG}, and {@link VariantKind#DOUBLE}). See {@link Variant#getLong}.
	 *
	 * @param key {@code String} key to fetch
	 * @return value associated with {@code key} as a {@code long}
	 * @throws IllegalArgumentException if {@code key} is null
	 * @throws VariantKeyNotFoundException if the key was not found
	 * @throws VariantKindException if the value kind is not {@link VariantKind#INTEGER},
	 * {@link VariantKind#LONG}, or {@link VariantKind#DOUBLE}
	 * @throws VariantRangeException if the value kind is {@link VariantKind#DOUBLE}, but
	 * the value is not expressible as an {@code long}
	 *
	 * @see Variant#getLong
	 */
	long getLong(final String key) throws VariantException {
		return getVariant(key).getLong();
	}

	/**
	 * Gets the value for {@code key} as a {@code double}.
	 *
	 * Equivalent to: {@code getVariant(key).getDouble()}
	 *
	 * This method performs conversions between numeric types ({@link VariantKind#INTEGER},
	 * {@link VariantKind#LONG}, and {@link VariantKind#DOUBLE}). See {@link Variant#getDouble}.
	 *
	 * @param key {@code String} key to fetch
	 * @return value associated with {@code key} as a {@code double}
	 * @throws IllegalArgumentException if {@code key} is null
	 * @throws VariantKeyNotFoundException if the key was not found
	 * @throws VariantKindException if the value kind is not {@link VariantKind#INTEGER},
	 * {@link VariantKind#LONG}, or {@link VariantKind#DOUBLE}
	 * @throws VariantRangeException if the value kind is {@link VariantKind#LONG}, but
	 * the value is not expressible as an {@code double}
	 *
	 * @see Variant#getLong
	 */
	double getDouble(final String key) throws VariantException {
		return getVariant(key).getDouble();
	}

	/**
	 * Gets the value for {@code key} as a {@code boolean}.
	 *
	 * Equivalent to: {@code getVariant(key).getBoolean()}
	 *
	 * @param key {@code String} key to fetch
	 * @return value associated with {@code key} as a {@code boolean}
	 * @throws IllegalArgumentException if {@code key} is null
	 * @throws VariantKeyNotFoundException if the key was not found
	 * @throws VariantKindException if the value kind was not {@link VariantKind#BOOLEAN}
	 *
	 * @see Variant#getBoolean
	 */
	boolean getBoolean(final String key) throws VariantException {
		return getVariant(key).getBoolean();
	}

	/**
	 * Gets the value for {@code key} as a {@code Map<String, Variant>}.
	 *
	 * Equivalent to: {@code getVariant(key).getVariantMap()}
	 *
	 * @param key {@code String} key to fetch
	 * @return value associated with {@code key}
	 * @throws IllegalArgumentException if {@code key} is null
	 * @throws VariantKeyNotFoundException if the key was not found
	 * @throws VariantKindException if the value kind was not {@link VariantKind#MAP}
	 *
	 * @see Variant#getVariantMap
	 */
	Map<String, Variant> getVariantMap(final String key) throws VariantException {
		return getVariant(key).getVariantMap();
	}

	/**
	 * Gets the value for {@code key} as an {@code List<Variant>}.
	 *
	 * Equivalent to: {@code getVariant(key).getVariantList()}
	 *
	 * @param key {@code String} key to fetch
	 * @return value associated with {@code key}
	 * @throws IllegalArgumentException if {@code key} is null
	 * @throws VariantKeyNotFoundException if the key was not found
	 * @throws VariantKindException if the value kind was not {@link VariantKind#VECTOR}
	 *
	 * @see Variant#getVariantList
	 */
	List<Variant> getVariantList(final String key) throws VariantException {
		return getVariant(key).getVariantList();
	}

	/**
	 * Gets the value for {@code key} as a custom object.
	 *
	 * Equivalent to: {@code getVariant(key).getTypedObject(serializer)}
	 *
	 * @param <T> return type
	 * @param key {@code String} key to fetch
	 * @param serializer {@code VariantSerializer<T>} serializer to convert a {@link Variant} to a {@code T}
	 * @return value associated with {@code key} deserialized to a {@code T}
	 * @throws IllegalArgumentException if {@code key} or {@code serializer} is null
	 * @throws VariantKeyNotFoundException if the key was not found
	 * @throws VariantSerializationFailedException if deserialization failed
	 *
	 * @see Variant#getTypedObject
	 */
	<T> T getTypedObject(final String key, final VariantSerializer<T> serializer) throws VariantException {
		return getVariant(key).getTypedObject(serializer);
	}

	/**
	 * Gets the value for {@code key} as a list of custom objects.
	 *
	 * Equivalent to: {@code getVariant(key).getTypedList(serializer)}
	 *
	 * @see Variant#getTypedList
	 *
	 * @param <T> element type of the returned @{code Map}
	 * @param key {@code String} key to fetch
	 * @param serializer {@code VariantSerializer<T>} serializer to convert a {@link Variant} to a {@code T}
	 * @return value associated with {@code key} deserialized to {@code List<T>}
	 * @throws IllegalArgumentException if {@code key} or {@code serializer} is null
	 * @throws VariantKeyNotFoundException if the key was not found
	 * @throws VariantKindException if the value kind was not {@link VariantKind#VECTOR}
	 * @throws VariantSerializationFailedException if deserialization failed
	 */
	<T> List<T> getTypedList(final String key, final VariantSerializer<T> serializer) throws VariantException {
		return getVariant(key).getTypedList(serializer);
	}

	/**
	 * Gets the value for {@code key} as a map of custom objects.
	 *
	 * Equivalent to: {@code getVariant(key).getTypedMap(serializer)}
	 *
	 * @see Variant#getTypedMap
	 *
	 * @param <T> value type of the returned @{code Map}
	 * @param key {@code String} key to fetch
	 * @param serializer {@code VariantSerializer<T>} serializer to convert a {@link Variant} to a {@code T}
	 * @return value associated with {@code key} deserialized to {@code Map<String, T>}
	 * @throws IllegalArgumentException if {@code key} or {@code serializer} is null
	 * @throws VariantKeyNotFoundException if the key was not found
	 * @throws VariantKindException if the value kind was not {@link VariantKind#MAP}
	 * @throws VariantSerializationFailedException if deserialization failed
	 */
	<T> Map<String, T> getTypedMap(final String key, final VariantSerializer<T> serializer) throws VariantException {
		return getVariant(key).getTypedMap(serializer);
	}

	/**
	 * Gets the value for {@code key} as a {@code Map<String,String>}.
	 *
	 * Equivalent to: {@code getVariant(key).getStringMap()}
	 *
	 * @see Variant#getStringMap
	 *
	 * @param key {@code String} key to fetch
	 * @return value associated with {@code key}, converted to a {@code Map<String, String>}
	 * @throws IllegalArgumentException if {@code key} is null
	 * @throws VariantKeyNotFoundException if the key was not found
	 * @throws VariantKindException if the value kind was not {@link VariantKind#MAP}
	 */
	Map<String, String> getStringMap(final String key) throws VariantException {
		return getVariant(key).getStringMap();
	}

	/**
	 * Gets the value for {@code key} and converts it to a {@code List<String>}.
	 *
	 * Equivalent to: {@code getVariant(key).getStringList()}
	 *
	 * @param key {@code String} key to fetch
	 * @return value associated with {@code key}, converted to a {@code List<String>}
	 * @throws IllegalArgumentException if {@code key} is null
	 * @throws VariantKeyNotFoundException if the key was not found
	 * @throws VariantKindException if the value kind was not {@link VariantKind#VECTOR}
	 *
	 * @see #getVariant
	 * @see Variant#getStringList
	 */
	List<String> getStringList(final String key) throws VariantException {
		return getVariant(key).getStringList();
	}

	/**
	 * Gets the {@code Variant} value associated with {@code key} or returns a default.
	 *
	 * @param key {@code String} key to fetch
	 * @param fallback {@link Variant} value to return in case of failure. Can be null.
	 * @return the {@code Variant} value associated with {@code key}, or {@code fallback} if the kind of this is
	 * not gettable as a {@code Variant}
	 * @throws IllegalArgumentException if {@code key} is null
	 */
	Variant optVariant(final String key, final Variant fallback) {
		try {
			return getVariant(key);
		} catch (VariantException ex) {
			return fallback;
		}
	}

	/**
	 * Gets the {@code String} value associated with {@code key} or returns a default.
	 *
	 * @param key {@code String} key to fetch
	 * @param fallback {@code String} value to return in case of failure. Can be null.
	 * @return the {@code String} value associated with {@code key}, or {@code fallback} if the kind of this is
	 * not gettable as a {@code String}
	 * @throws IllegalArgumentException if {@code key} is null
	 */
	String optString(final String key, final String fallback) {
		try {
			return getString2(key);
		} catch (VariantException ex) {
			return fallback;
		}
	}

	/**
	 * Gets the {@code int} value associated with {@code key} or returns a default.
	 *
	 * @param key {@code String} key to fetch
	 * @param fallback {@code int} value to return in case of failure
	 * @return the {@code int} value associated with {@code key}, or {@code fallback} if the kind of this is
	 * not gettable as a {@code int}
	 * @throws IllegalArgumentException if {@code key} is null
	 */
	int optInteger(final String key, final int fallback) {
		try {
			return getInteger(key);
		} catch (VariantException ex) {
			return fallback;
		}
	}

	/**
	 * Gets the {@code long} value associated with {@code key} or returns a default.
	 *
	 * @param key {@code String} key to fetch
	 * @param fallback {@code long} value to return in case of failure
	 * @return the {@code long} value associated with {@code key}, or {@code fallback} if the kind of this is
	 * not gettable as a {@code long}
	 * @throws IllegalArgumentException if {@code key} is null
	 */
	long optLong(final String key, final long fallback) {
		try {
			return getLong(key);
		} catch (VariantException ex) {
			return fallback;
		}
	}

	/**
	 * Gets the {@code double} value associated with {@code key} or returns a default.
	 *
	 * @param key {@code String} key to fetch
	 * @param fallback {@code double} value to return in case of failure
	 * @return the {@code double} value associated with {@code key}, or {@code fallback} if the kind of this is
	 * not gettable as a {@code double}
	 */
	double optDouble(final String key, final double fallback) {
		try {
			return getDouble(key);
		} catch (VariantException ex) {
			return fallback;
		}
	}

	/**
	 * Gets the {@code boolean} value associated with {@code key} or returns a default.
	 *
	 * @param key {@code String} key to fetch
	 * @param fallback {@code boolean} value to return in case of failure
	 * @return the {@code boolean} value associated with {@code key}, or {@code fallback} if the kind of this is
	 * not gettable as a {@code boolean}
	 * @throws IllegalArgumentException if {@code key} is null
	 */
	boolean optBoolean(final String key, final boolean fallback) {
		try {
			return getBoolean(key);
		} catch (VariantException ex) {
			return fallback;
		}
	}

	/**
	 * Gets the {@code Map<String, Variant>} value associated with {@code key} or returns a default.
	 *
	 * @param key {@code String} key to fetch
	 * @param fallback {@code Map<String, Variant>} value to return in case of failure
	 * @return the {@code Map<String, Variant>} value associated with {@code key}, or {@code fallback} if the value is
	 * not gettable as a {@code Map<String, Variant>}
	 */
	Map<String, Variant> optVariantMap(final String key, final Map<String, Variant> fallback) {
		try {
			return getVariantMap(key);
		} catch (VariantException ex) {
			return fallback;
		}
	}

	/**
	 * Gets the {@code List<Variant>} value associated with {@code key} or returns a default.
	 *
	 * @param key {@code String} key to fetch
	 * @param fallback {@code List<Variant>} value to return in case of failure
	 * @return the {@code List<Variant>} value associated with {@code key}, or {@code fallback} if the value is
	 * not gettable as a {@code List<Variant>}
	 * @throws IllegalArgumentException if {@code key} is null
	 */
	List<Variant> optVariantList(final String key, final List<Variant> fallback) {
		try {
			return getVariantList(key);
		} catch (VariantException ex) {
			return fallback;
		}
	}

	/**
	 * Gets the value associated with {@code key} as a custom object or returns a default.
	 *
	 * This method uses the given serializer to deserialize the value to a custom object.
	 *
	 * @param <T> return type
	 * @param key {@code String} key to fetch
	 * @param fallback {@code T} value to return in case of failure. Can be null.
	 * @param serializer {@code VariantSerializer<T>} serializer that will convert {@code Variant} to a {@code T}
	 * @return a {@code T} deserialized from the value, or {@code fallback} if deserialization failed
	 * @throws IllegalArgumentException if {@code key} or {@code serializer} is null
	 *
	 * @see Variant#getTypedObject
	 */
	<T> T optTypedObject(final String key, final T fallback, final VariantSerializer<T> serializer) {
		try {
			return getTypedObject(key, serializer);
		} catch (VariantException ex) {
			return fallback;
		}
	}

	/**
	 * Gets the value associated with {@code key} as a map of custom objects or returns a default.
	 *
	 * @param <T> value type of the returned {@link Map}
	 * @param key {@code String} key to fetch
	 * @param fallback {@code T} value to return in case of failure. Can be null.
	 * @param serializer {@code VariantSerializer<T>} serializer that will convert each {@link Variant} map value to a {@code T}
	 * @return a {@code Map<String, T>} deserialized from the value, or {@code fallback} if the value is
	 * not gettable as a {@code Map<String, T>}
	 * @throws IllegalArgumentException if {@code key} or {@code serializer} is null
	 *
	 * @see Variant#getTypedMap
	 */
	<T> Map<String, T> optTypedMap(final String key, final Map<String, T> fallback,
								   final VariantSerializer<T> serializer) {
		try {
			return getTypedMap(key, serializer);
		} catch (VariantException ex) {
			return fallback;
		}
	}

	/**
	 * Gets the value associated with {@code key} as a list of custom objects or returns a default.
	 *
	 * @param <T> element type of the returned {@link List}
	 * @param key {@code String} key to fetch
	 * @param fallback {@code T} value to return in case of failure. Can be null.
	 * @param serializer {@code VariantSerializer<T>} serializer that will convert each {@link Variant} map value to a {@code T}
	 * @return a {@code List<T>} deserialized from the value, or {@code fallback} if the value is
	 * not gettable as a {@code List<T>}
	 * @throws IllegalArgumentException if {@code key} or {@code serializer} is null
	 *
	 * @see Variant#getTypedList
	 */
	<T> List<T> optTypedList(final String key, final List<T> fallback, final VariantSerializer<T> serializer) {
		try {
			return getTypedList(key, serializer);
		} catch (VariantException ex) {
			return fallback;
		}
	}

	/**
	 * Gets the value associated with {@code key} as a {@code Map<String, String>} or returns a default.
	 *
	 * @param key {@code String} key to fetch
	 * @param fallback {@code Map<String, String>} value to return in case of failure. Can be null.
	 * @return a {@code Map<String, String> } deserialized from the value, or {@code fallback} if the value is
	 * not gettable as a {@code Map<String, String>}
	 * @throws IllegalArgumentException if {@code key} is null
	 *
	 * @see Variant#getStringMap
	 */
	Map<String, String> optStringMap(final String key, final Map<String, String> fallback) {
		try {
			return getStringMap(key);
		} catch (VariantException ex) {
			return fallback;
		}
	}

	/**
	 * Gets the value associated with {@code key} as a {@code List<String>} or returns a default.
	 *
	 * @param key {@code String} key to fetch
	 * @param fallback {@code List<String>} value to return in case of failure. Can be null.
	 * @return a {@code List<String> } deserialized from the value, or {@code fallback} if the value is
	 * not gettable as a {@code List<String>}
	 * @throws IllegalArgumentException if {@code key} is null
	 *
	 * @see Variant#getStringList
	 */
	List<String> optStringList(final String key, final List<String> fallback) {
		try {
			return getStringList(key);
		} catch (VariantException ex) {
			return fallback;
		}
	}

	/**
	 * @return the set of keys in this
	 */
	Set<String> keys() {
		return internalMap.keySet();
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		EventData eventData = (EventData) o;
		return internalMap.equals(eventData.internalMap);
	}

	@Override
	public int hashCode() {
		return internalMap.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("{");

		boolean isFirst = true;

		for (Map.Entry<String, Variant> entry : internalMap.entrySet()) {
			if (isFirst) {
				isFirst = false;
			} else {
				b.append(",");
			}

			final String quote = "\"";
			final String slashQuote = "\\\"";
			b.append(quote);
			b.append(entry.getKey().replaceAll(quote, slashQuote));
			b.append(quote);
			b.append(":");
			b.append(entry.getValue().toString());
		}

		b.append("}");
		return b.toString();
	}

	/**
	 * Merges the contents of another EventData instance into this EventData
	 *
	 * This method will merge all members of the otherData EventData object into the calling
	 * EventData object.  When an identical key exists in both instances, this instance will take priority.
	 *
	 * @param otherData the other {@link EventData} object to merge into this one
	 */
	void merge(final EventData otherData) {
		// quick bail if otherData is null or empty
		if (otherData == null || otherData.size() == 0) {
			return;
		}

		// putAll prioritizes the KVPs of the map passed in
		internalMap.putAll(CollectionUtils.addDataToMap(internalMap, otherData.internalMap));
	}

	/**
	 * Overwrites the contents of EventData with the contents of another EventData instance
	 *
	 * This method will merge all members of the otherData EventData object into the calling
	 * EventData object.  When an identical key exists in both instances, otherData instance will take priority.
	 * If a key exists in both instances, and the value for that key in otherData is null or empty string,
	 * the key will be removed from this EventData.
	 *
	 * @param otherData the other {@link EventData} object to merge into this one
	 * @return
	 */
	void overwrite(final EventData otherData) {
		// quick bail if otherData is null or empty
		if (otherData == null || otherData.size() == 0) {
			return;
		}

		// putAll prioritizes the KVPs of the map passed in
		Map<String, Variant> resultMap = CollectionUtils.addDataToMap(otherData.internalMap, internalMap, true, true);
		internalMap.clear();
		internalMap.putAll(resultMap);
	}

	String prettyString(final int indentDepth) {
		return CollectionUtils.getPrettyString(internalMap, indentDepth);
	}

	private String convertVariantToString(final Variant variant) throws VariantException {
		if (variant instanceof VectorVariant) {
			return variant.toString();
		}

		return variant.convertToString();
	}

	//////////////////////////////////////////////////////////////////////////////////////////////
	// The following methods were added in order to ease merging. They are designed to (mostly) //
	// act like their old counterparts. You may continue to use them until AMSDK-6573 and       //
	// AMSDK-6616 are complete.                                                                 //
	//////////////////////////////////////////////////////////////////////////////////////////////

	// TODO: AMSDK-6616 remove these methods

	/**
	 * Assigns a string map to the specified key (deprecated).
	 *
	 * @param key {@code String} key to assign
	 * @param value {@code Map<String, String>} value to assign,
	 *              or null to assign {@link VariantKind#NULL}
	 * @return this
	 * @throws IllegalArgumentException if {@code key} is null
	 *
	 * @deprecated Replaced by {@link #putStringMap(String, Map)}
	 */
	@Deprecated
	EventData putMap(final String key, final Map<String, String> value) {
		return putStringMap(key, value);
	}

	/**
	 * Gets the specified key as an {@code int} or returns a default value (deprecated).
	 *
	 * @param key {@code String} key to fetch
	 * @param fallback {@code int} value to return on failure
	 * @return the {@code int} value associated with {@code key} or {@code fallback} upon failure
	 * @throws IllegalArgumentException if {@code key} is null
	 *
	 * @deprecated Replaced by {@link #optInteger(String, int)}
	 */
	@Deprecated
	int getInt(final String key, final int fallback) {
		return optInteger(key, fallback);
	}

	/**
	 * Assigns an {@code int} to the specified key (deprecated).
	 *
	 * @param key {@code String} key to fetch
	 * @param value {@code int} value to assign
	 * @return this
	 * @throws IllegalArgumentException if {@code key} is null
	 *
	 * @deprecated Replaced by {@link #putInteger(String, int)}
	 */
	@Deprecated
	EventData putInt(final String key, final int value) {
		return putInteger(key, value);
	}

	/**
	 * Gets the specified key as a {@code long} or returns a default value (deprecated).
	 *
	 * @param key {@code String} key to fetch
	 * @param fallback {@code long} value to return on failure
	 * @return the {@code long} value associated with {@code key} or {@code fallback} upon failure
	 * @throws IllegalArgumentException if {@code key} is null
	 *
	 * @deprecated Replaced by {@link #optLong(String, long)}
	 */
	@Deprecated
	long getLong(final String key, final long fallback) {
		return optLong(key, fallback);
	}

	/**
	 * Gets the specified key as a {@code boolean} or returns a default value (deprecated).
	 *
	 * @param key {@code String} key to fetch
	 * @param fallback {@code boolean} value to return on failure
	 * @return the {@code boolean} value associated with {@code key} or {@code fallback} upon failure
	 * @throws IllegalArgumentException if {@code key} is null
	 *
	 * @deprecated Replaced by {@link #optBoolean(String, boolean)}
	 */
	@Deprecated
	boolean getBoolean(final String key, final boolean fallback) {
		return optBoolean(key, fallback);
	}

	/**
	 * Gets the specified key as a string map or returns null (deprecated).
	 *
	 * @param key {@code String} key to fetch
	 * @return the {@code Map<String, String>} value associated with {@code key} or null upon failure
	 * @throws IllegalArgumentException if {@code key} is null
	 *
	 * @deprecated Replaced by {@link #optStringMap(String, Map)}
	 */
	@Deprecated
	Map<String, String> getMap(final String key) {
		return optStringMap(key, null);
	}

	/**
	 * Gets the specified key as a string or returns null (deprecated).
	 *
	 * @param key {@code String} key to fetch
	 * @return the {@code String} value associated with {@code key} or null upon failure
	 * @throws IllegalArgumentException if {@code key} is null
	 *
	 * @deprecated Replaced by {@link #optString(String, String)}
	 */
	@Deprecated
	String getString(final String key) {
		return optString(key, null);
	}

	/**
	 * @param key {@code String} key to assign
	 * @param o custom object to serialize
	 * @return this {@link EventData}
	 *
	 * @deprecated Replaced by {@link #putTypedObject}
	 */
	@Deprecated
	EventData putObject(final String key, final Object o) {
		return putVariant(key, Variant.fromObject(o));
	}

	/**
	 * @param key {@code String} key to look for in the {@link EventData}
	 * @return the associated value if the key exists, null if the key does not exist
	 *
	 * @deprecated Replaced by {@link #getTypedObject}
	 */
	@Deprecated
	Object getObject(final String key) {
		try {
			return optVariant(key, Variant.fromNull()).getObject();
		} catch (VariantException ex) {
			return null;
		}
	}

	/**
	 * @param key {@code String} key to look for in the {@link EventData}
	 * @param klass Custom class
	 * @param <T> Custom type
	 * @return the associated value if the key exists, null if the key does not exist
	 *
	 * @deprecated Replaced by {@link #getTypedObject}
	 */
	@Deprecated
	<T> T getObject(final String key, final Class<T> klass) {
		try {
			return optVariant(key, Variant.fromNull()).getObject(klass);
		} catch (VariantException ex) {
			return null;
		}
	}

	/**
	 * @param key the key to look for in the {@link EventData}
	 * @param klass Custom class
	 * @param <T> Custom type
	 * @return the list associated with the provided key if present, {@link NullVariant} otherwise
	 * @throws ClassCastException if the type is not supported
	 *
	 * @deprecated Replaced by {@link #getTypedList}
	 */
	@Deprecated
	<T> List<T> getObjectList(final String key, final Class<T> klass) throws ClassCastException {
		return optVariant(key, Variant.fromNull()).getObjectList(klass);
	}
}
