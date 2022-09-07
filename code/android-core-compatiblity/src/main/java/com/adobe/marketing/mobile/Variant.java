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
import java.util.List;
import java.util.Map;


/**
 * The {@code Variant} class represents different types of JSON-compatible data.
 *
 * <p>A variant contains one of the following:</p>
 * <ul>
 *     <li>null</li>
 *     <li>a string</li>
 *     <li>a number (represented as an int, long, double)</li>
 *     <li>a boolean</li>
 *     <li>a variant map, map of string keys to variant values</li>
 *     <li>a variant vector, a list of variant values</li>
 * </ul>
 *
 * <p>Variants can be constructed from Java {@code String}s, {@code int}s, {@code long}s,
 * {@code double}s, {@code boolean}s, {@code Map<String, Variant>}s, or {@code List<Variant>}s
 * using the {@code Variant.fromXyz(...)} methods. Once constructed, a variant's value
 * is constant and will not change.</p>
 *
 * <p>The value of a variant can be obtained in multiple ways:</p>
 * <ul>
 *     <li>
 *         The {@code Variant.optXyz(...)} methods are typically the best choice for users. These
 *         methods return the value as an {@code xyz}. If the value is missing or is not an
 *         {@code xyz}, the method will return a default value. Implicit conversions between types
 *         are not performed, except between numeric types.
 *     </li>
 *     <li>
 *         The {@code Variant.getXyz(...)} methods return the value as an {@code xyz}. If the value
 *         is missing or is not an {@code xyz} the method will throw. Implicit conversions between
 *         types are not performed, except between numeric types.
 *     </li>
 *     <li>
 *         The {@code Variant.convertToXyz(...)} methods return the value as an {@code xyz},
 *         performing type conversions to {@code String} or {@code double} as needed. If the value
 *         cannot be converted, the method will throw.
 *     </li>
 * </ul>
 *
 * <p>Variants are serializable to/from JSON by using {@link #fromTypedObject} and
 * {@link #getTypedObject} with a {@link JsonObjectVariantSerializer}.</p>
 *
 * <p>Variants are serializable to/from custom classes that provide a {@link VariantSerializer}
 * implementation. See {@link VariantSerializer}, {@link #fromTypedObject}, {@link #getTypedObject}, and
 * {@link #optTypedObject}. Collections of custom classes are also supported.</p>
 */
abstract class Variant implements Cloneable {
	/**
	 * The maximum value that a long variant can hold.
	 */
	public final static long MAX_SAFE_INTEGER = 9007199254740991L;
	/**
	 * The maximum value that a long variant can hold.
	 */
	public final static long MIN_SAFE_INTEGER = -9007199254740991L;

	/**
	 * The minimum value that a long variant can hold.
	 */

	/**
	 * Constructs a variant containing null.
	 *
	 * @return a variant containing null
	 */
	public static Variant fromNull() {
		return NullVariant.from();
	}

	/**
	 * Constructs a variant containing a {@code String}.
	 *
	 * @param value {@code String} value for the variant. Can be null.
	 * @return a variant containing the given string, or a null variant if value is null
	 */
	public static Variant fromString(final String value) {
		return value == null ? fromNull() : StringVariant.from(value);
	}

	/**
	 * Constructs a variant containing an {@code int}.
	 *
	 * @param value {@code int} value for the variant
	 * @return a variant containing the given number
	 */
	public static Variant fromInteger(final int value) {
		return IntegerVariant.from(value);
	}

	/**
	 * Constructs a variant containing a {@code long}.
	 *
	 * For interoperability, the maximum and minimum long values that a variant can contain are
	 * {@link Variant#MAX_SAFE_INTEGER} and {@link Variant#MIN_SAFE_INTEGER} respectively.
	 *
	 * @param value {@code long} value for the variant
	 * @return a variant containing the given number
	 * @throws IllegalArgumentException if value is greater than {@code Variant.MAX_SAFE_INTEGER} or
	 *         less than {@code Variant.MIN_SAFE_INTEGER}.
	 */
	public static Variant fromLong(final long value) {
		return LongVariant.from(value);
	}

	/**
	 * Constructs a variant containing a {@code double}.
	 *
	 * @param value {@code double} value for the variant
	 * @return variant containing the given number
	 */
	public static Variant fromDouble(final double value) {
		return DoubleVariant.from(value);
	}

	/**
	 * Constructs a variant containing a {@code boolean}.
	 *
	 * @param value {@code boolean} value for the variant
	 * @return a variant containing the given boolean
	 */
	public static Variant fromBoolean(final boolean value) {
		return BooleanVariant.from(value);
	}

	/**
	 * Constructs a variant containing a {@code Map<String, Variant>}.
	 *
	 * A copy of the given map will be stored in the variant. Map entries with null keys will
	 * be omitted from the variant's map. Null values will be converted to null variant values.
	 *
	 * @param value {@code Map<String, Variant>} value for the variant. Can be null.
	 * @return a variant containing a copy of the given map, or a null variant if value is null
	 */
	public static Variant fromVariantMap(final Map<String, Variant> value) {
		return value == null ? fromNull() : MapVariant.from(value);
	}

	/**
	 * Constructs a variant from a {@code List<Variant>}.
	 *
	 * A copy of the given list will be stored in the variant. Null elements will be converted
	 * to null variants.
	 *
	 * @param value {@code List<Variant>} value for the variant. Can be null.
	 * @return a variant containing a copy of the given list, or a null variant if value is null
	 */
	public static Variant fromVariantList(final List<Variant> value) {
		return value == null ? fromNull() : VectorVariant.from(value);
	}

	/**
	 * Constructs a variant from a custom object.
	 *
	 * @param <T> the type of object to serialize to a variant
	 * @param value {@code T} custom object to serialize. Can be null.
	 * @param serializer {@code VariantSerializer<T>} serializer that will convert the {@code T} to a {@code Variant}
	 * @return a variant containing a serialization of the given custom class, or a null variant if value is null
	 * @throws IllegalArgumentException if {@code serializer} is null
	 * @throws VariantSerializationFailedException if serialization failed
	 *
	 * @see VariantSerializer
	 */
	public static <T> Variant fromTypedObject(final T value,
			final VariantSerializer<T> serializer) throws VariantException {
		if (serializer == null) {
			throw new IllegalArgumentException();
		}

		if (value == null) {
			return Variant.fromNull();
		}

		try {
			final Variant variant = serializer.serialize(value);

			if (variant == null) {
				throw new VariantSerializationFailedException("cannot serialize to null");
			}

			return variant;
		} catch (VariantSerializationFailedException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new VariantSerializationFailedException(ex);
		}
	}

	/**
	 * Constructs a variant from a map of custom objects.
	 *
	 * The returned variant will be a variant map whose values are serializations of {@code map}
	 * values.  Map entries with null keys and map entries with unserializable values will be
	 * omitted from the variant's map. Null values will be converted to null variant values.
	 *
	 * @param <T> the map value type to serialize
	 * @param map {@code Map<String, ? extends T>} map of custom objects to serialize. Can be null.
	 * @param serializer {@code VariantSerializer<T>} serializer that will convert each {@code T} to a {@code Variant}
	 * @return a variant containing a serialization of the given map, or a null variant if {@code map} is null
	 * @throws IllegalArgumentException if {@code serializer} is null
	 *
	 * @see VariantSerializer
	 */
	public static <T> Variant fromTypedMap(final Map<String, ? extends T> map,
										   final VariantSerializer<T> serializer) {
		if (serializer == null) {
			throw new IllegalArgumentException();
		}

		return new TypedMapVariantSerializer<T>(serializer).serializeMap(map);
	}

	/**
	 * Constructs a variant from a list of custom objects.
	 *
	 * The returned variant will be a variant list containing serializations of {@code list}
	 * elements. Elements that cannot be serialized will be omitted from the variant list.
	 * Null elements will be converted to null variants.
	 *
	 * @param <T> the list element type to serialize
	 * @param list {@code List<? extends T>} list of custom objects to serialize. Can be null.
	 * @param serializer {@code VariantSerializer<T>} serializer that will convert each {@code T} to a {@code Variant}
	 * @return a variant containing a serialization of the given list, or a null variant if value is null
	 * @throws IllegalArgumentException if {@code serializer} is null
	 *
	 * @see VariantSerializer
	 */
	public static <T> Variant fromTypedList(final List<? extends T> list, final VariantSerializer<T> serializer) {
		if (serializer == null) {
			throw new IllegalArgumentException();
		}

		if (list == null) {
			return Variant.fromNull();
		}

		return new TypedListVariantSerializer<T>(serializer).serializeList(list);
	}

	/**
	 * Constructs a variant from a {@code Map<String, String>}.
	 *
	 * The returned variant will be a variant map whose values are string or null variants.
	 * Map entries with null keys will be omitted from the variant's map. Null values will be
	 * converted to null variants.
	 *
	 * @param map {@code Map<String, String>} map of strings to serialize. Can be null.
	 * @return a variant containing a serialization of the given map, or a null variant if {@code map} is null
	 */
	public static Variant fromStringMap(final Map<String, String> map) {
		return fromTypedMap(map, new StringVariantSerializer());
	}

	/**
	 * Constructs a variant from a {@code List<String>}.
	 *
	 * The returned variant will be a variant list whose values are string or null variants.
	 * Null elements will be converted to null variants.
	 *
	 * @param list {@code List<String>} list of strings to serialize. Can be null.
	 * @return variant containing a serialization of the given list, or a null variant if {@code list} is null
	 */
	public static Variant fromStringList(final List<String> list) {
		return fromTypedList(list, new StringVariantSerializer());
	}

	/**
	 * @return the kind of value this contains
	 */
	public abstract VariantKind getKind();

	/**
	 * Gets the string value of this.
	 *
	 * @return the {@code String} value of this
	 * @throws VariantKindException if this is not a {@link VariantKind#STRING}
	 */
	public String getString() throws VariantException  {
		throw new VariantKindException("value not gettable as a string");
	}

	/**
	 * Gets the {@code int} value of this.
	 *
	 * While the {@code get} methods do not typically perform implicit conversions, this method
	 * performs conversions between numeric types ({@link VariantKind#INTEGER},
	 * {@link VariantKind#LONG}, and {@link VariantKind#DOUBLE}):
	 * <ul>
	 *     <li>
	 *         If this contains a {@code long} that is within the range of {@code int},
	 *         this method returns the {@code long} casted to an {@code int}.
	 *     </li>
	 *     <li>
	 *         If this contains a {@code double} whose rounded value is within the range
	 *         {@code int}, this method returns the {@code double} rounded to an {@code int}.
	 *     </li>
	 *     <li>
	 *         If this contains a {@code long} or {@code double} that is out the range of
	 *         {@code int}, this method throws a VariantRangeException.
	 *     </li>
	 * </ul>
	 *
	 * @return the {@code int} value of this
	 * @throws VariantKindException if the kind of this is not {@link VariantKind#INTEGER},
	 * {@link VariantKind#LONG}, or {@link VariantKind#DOUBLE}
	 * @throws VariantRangeException if the kind of this is {@link VariantKind#LONG} or
	 * {@link VariantKind#DOUBLE}, but its value is not expressible as an {@code int}
	 */
	public int getInteger() throws VariantException {
		throw new VariantKindException("value not gettable as an int");
	}

	/**
	 * Gets the {@code long} value of this.
	 *
	 * While the {@code get} methods do not typically perform implicit conversions, this method
	 * performs conversions between numeric types ({@link VariantKind#INTEGER},
	 * {@link VariantKind#LONG}, and {@link VariantKind#DOUBLE}):
	 * <ul>
	 *     <li>
	 *         If this contains a {@code int}, this method returns the {@code int} casted to a
	 *         {@code long}.
	 *     </li>
	 *     <li>
	 *         If this contains a {@code double} whose rounded value is within the range
	 *         {@code long}, this method returns the {@code double} rounded to an {@code long}.
	 *     </li>
	 *     <li>
	 *         If this contains a {@code double} that is out the range of {@code long}, this method
	 *         throws a VariantRangeException.
	 *     </li>
	 * </ul>
	 *
	 * @return the {@code long} value of this
	 * @throws VariantKindException if the kind of this is not {@link VariantKind#INTEGER},
	 * {@link VariantKind#LONG}, or {@link VariantKind#DOUBLE}
	 * @throws VariantRangeException if the kind of this is {@link VariantKind#DOUBLE}, but
	 * its value is not expressible as an {@code long}
	 */
	public long getLong() throws VariantException {
		throw new VariantKindException("value not gettable as a long");
	}

	/**
	 * Gets the {@code double} value of this.
	 *
	 * While the {@code get} methods do not typically perform implicit conversions, this method
	 * performs conversions between numeric types ({@link VariantKind#INTEGER},
	 * {@link VariantKind#LONG}, and {@link VariantKind#DOUBLE}):
	 * <ul>
	 *     <li>
	 *         If this contains a {@code int}, this method returns the {@code int} casted to a
	 *         {@code double}.
	 *     </li>
	 *     <li>
	 *         If this contains a {@code long} that is within the range of {@code double},
	 *         this method returns the {@code long} casted to a {@code double}.
	 *     </li>
	 *     <li>
	 *         If this contains a {@code long} that is out the range of {@code double}, this method
	 *         throws a VariantRangeException.
	 *     </li>
	 * </ul>
	 *
	 * @return the {@code double} value of this
	 * @throws VariantKindException if the kind of this is not {@link VariantKind#INTEGER},
	 * {@link VariantKind#LONG}, or {@link VariantKind#DOUBLE}
	 * @throws VariantRangeException if the kind of this is {@link VariantKind#LONG}, but
	 * its value is not expressible as an {@code double}
	 */
	public double getDouble() throws VariantException {
		throw new VariantKindException("value not gettable as a double");
	}

	/**
	 * Gets the {@code boolean} value of this.
	 *
	 * @return the {@code boolean} value of this
	 * @throws VariantKindException if the kind of this is not {@link VariantKind#BOOLEAN}
	 */
	public boolean getBoolean() throws VariantException {
		throw new VariantKindException("value not gettable as a boolean");
	}

	/**
	 * Gets a copy of the {@code Map<String, Variant>} value contained within this.
	 *
	 * @return a copy of the {@code Map<String, Variant>} value contained within this.
	 * @throws VariantKindException if the kind of this is not {@link VariantKind#MAP}
	 */
	public Map<String, Variant> getVariantMap() throws VariantException {
		throw new VariantKindException("value not gettable as a map");
	}

	/**
	 * Gets a copy of the {@code List<Variant>} value contained within this.
	 *
	 * @return a copy of the {@code List<Variant>} value contained within this.
	 *
	 * @throws VariantKindException if the kind of this is not {@link VariantKind#VECTOR}
	 */
	public List<Variant> getVariantList() throws VariantException {
		throw new VariantKindException("value not gettable as a vector");
	}

	/**
	 * Gets the value of this as a custom object.
	 *
	 * This method uses the given serializer to deserialize this to a custom object.
	 * @param <T> the type to deserialize
	 * @param serializer {@code VariantSerializer<T>} serializer that will convert {@code Variant} to a {@code T}
	 * @return a {@code T} deserialized from this. Can be null if the serializer supports null.
	 * @throws IllegalArgumentException if {@code serializer} is null
	 * @throws VariantSerializationFailedException if deserialization failed
	 *
	 * @see VariantSerializer
	 */
	public final <T> T getTypedObject(final VariantSerializer<T> serializer) throws VariantException {
		if (serializer == null) {
			throw new IllegalArgumentException();
		}

		try {
			return serializer.deserialize(this);
		} catch (VariantSerializationFailedException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new VariantSerializationFailedException(ex);
		}
	}

	/**
	 * Gets the value of this as a map of custom objects.
	 *
	 * This method uses the given serializer to deserialize this into a map of custom objects. Values that
	 * cannot be deserialized are skipped.  Map entries with null variant values will be converted
	 * to map entries with (Java) null values.
	 *
	 * @param <T> the map value type to deserialize
	 * @param serializer {@code VariantSerializer<T>} serializer that will convert each {@code Variant} map value to a {@code T}
	 * @return a map of custom objects deserialized from this
	 * @throws IllegalArgumentException if {@code serializer} is null
	 * @throws VariantKindException if the kind of this is not {@link VariantKind#MAP}
	 *
	 * @see VariantSerializer
	 */
	public final <T> Map<String, T> getTypedMap(final VariantSerializer<T> serializer) throws VariantException {
		if (serializer == null) {
			throw new IllegalArgumentException();
		}

		final Map<String, Variant> variantMap = getVariantMap();
		return new TypedMapVariantSerializer<T>(serializer).deserializeMap(variantMap);
	}

	/**
	 * Gets the value of this as a list of custom objects.
	 *
	 * This method uses the given serializer to deserialize this into a list of custom objects. Values that
	 * cannot be deserialized are skipped.  Null variant elements will be converted
	 * to (Java) null elements.
	 *
	 * @param <T> the list element type to deserialize
	 * @param serializer {@code VariantSerializer<T>} serializer that will convert each {@code Variant} element to a {@code T}
	 * @return a list of custom objects deserialized from this
	 * @throws IllegalArgumentException if {@code serializer} is null
	 * @throws VariantKindException if the kind of this is not {@link VariantKind#VECTOR}
	 *
	 * @see VariantSerializer
	 */
	public final <T> List<T> getTypedList(final VariantSerializer<T> serializer) throws VariantException {
		if (serializer == null) {
			throw new IllegalArgumentException();
		}

		final List<Variant> variantList = getVariantList();
		return new TypedListVariantSerializer<T>(serializer).deserializeList(variantList);
	}

	/**
	 * Get the value of this as a {@code Map<String, String>}.
	 *
	 * If this is a variant map, converts each entry value of this to a string using
	 * {@link Variant#convertToString} and returns the resulting map. Values that
	 * cannot be converted are skipped. Map entries with null variant values will be converted
	 * to map entries with (Java) null values.
	 *
	 * @return a {@code Map<String, String>} derived from this
	 * @throws VariantKindException if the kind of this is not {@link VariantKind#MAP}
	 */
	public final Map<String, String> getStringMap() throws VariantException {
		return getTypedMap(new StringVariantSerializer());
	}

	/**
	 * Get the value of this as a {@code List<String>}.
	 *
	 * If this is a variant list, converts each element to a string using
	 * {@link Variant#convertToString} and returns the resulting list. Elements that
	 * cannot be converted are skipped. Null variant elements will be inserted as null.
	 *
	 * @return a {@code List<String>} derived from this
	 * @throws VariantKindException if the kind of this is not {@link VariantKind#VECTOR}
	 */
	public final List<String> getStringList() throws VariantException {
		return getTypedList(new StringVariantSerializer());
	}

	/**
	 * Gets the string value of this or returns a default.
	 *
	 * @param fallback {@code String} value to return in case of failure. Can be null.
	 * @return the {@code String} value of this, or {@code fallback} if the kind of this is
	 * not {@link VariantKind#STRING}
	 */
	public final String optString(final String fallback) {
		try {
			return getString();
		} catch (VariantException ex) {
			return fallback;
		}
	}

	/**
	 * Gets the {@code int} value of this or returns a default.
	 *
	 * This method performs conversions between numeric types ({@link VariantKind#INTEGER},
	 * {@link VariantKind#LONG}, and {@link VariantKind#DOUBLE}). See {@link #getInteger}.
	 *
	 * @param fallback {@code int} value to return in case of failure
	 * @return the {@code int} value of this, or {@code fallback} if this is not expressible
	 * as an {@code int}
	 */
	public final int optInteger(final int fallback) {
		try {
			return getInteger();
		} catch (VariantException ex) {
			return fallback;
		}
	}

	/**
	 * Gets the {@code long} value of this or returns a default.
	 *
	 * This method performs conversions between numeric types ({@link VariantKind#INTEGER},
	 * {@link VariantKind#LONG}, and {@link VariantKind#DOUBLE}). See {@link #getLong}.
	 *
	 * @param fallback {@code int} value to return in case of failure
	 * @return the {@code long} value of this, or {@code fallback} if this is not expressible
	 * as a {@code long}
	 */
	public final long optLong(final long fallback) {
		try {
			return getLong();
		} catch (VariantException ex) {
			return fallback;
		}
	}

	/**
	 * Gets the {@code double} value of this or returns a default.
	 *
	 * This method performs conversions between numeric types ({@link VariantKind#INTEGER},
	 * {@link VariantKind#LONG}, and {@link VariantKind#DOUBLE}). See {@link #getDouble}.
	 *
	 * @param fallback {@code double} value to return in case of failure
	 * @return the {@code double} value of this, or {@code fallback} if this is not expressible
	 * as a {@code double}
	 */
	public final double optDouble(final double fallback) {
		try {
			return getDouble();
		} catch (VariantException ex) {
			return fallback;
		}
	}

	/**
	 * Gets the {@code boolean} value of this or returns a default.
	 *
	 * @param fallback {@code double} value to return in case of failure
	 * @return the {@code double} value of this, or {@code fallback} if the kind of this is
	 * not {@link VariantKind#BOOLEAN}
	 */
	public final boolean optBoolean(final boolean fallback) {
		try {
			return getBoolean();
		} catch (VariantException ex) {
			return fallback;
		}
	}

	/**
	 * Gets the {@code Map<String, Variant>} value of this or returns a default.
	 *
	 * @param fallback {@code Map<String, Variant>} value to return in case of failure. Can be null.
	 * @return the {@code double} value of this, or {@code fallback} if the kind of this is
	 * not {@link VariantKind#MAP}
	 */
	public final Map<String, Variant> optVariantMap(final Map<String, Variant> fallback) {
		try {
			return getVariantMap();
		} catch (VariantException ex) {
			return fallback;
		}
	}

	/**
	 * Gets the {@code List<Variant>} value of this or returns a default.
	 *
	 * @param fallback {@code List<Variant>} value to return in case of failure. Can be null.
	 * @return the {@code List<Variant>} value of this, or {@code fallback} if the kind of this is
	 * not {@link VariantKind#VECTOR}
	 */
	public final List<Variant> optVariantList(final List<Variant> fallback) {
		try {
			return getVariantList();
		} catch (VariantException ex) {
			return fallback;
		}
	}

	/**
	 * Gets the value of this as a custom object or returns a default.
	 *
	 * @param <T> the object type to deserialize
	 * @param fallback {@code T} value to return in case of failure. Can be null.
	 * @param serializer {@code VariantSerializer<T>} serializer that will convert {@code Variant} to a {@code T}
	 * @return a custom object deserialized from this, or {@code fallback} if deserialization failed
	 * @throws IllegalArgumentException if {@code serializer} is null
	 *
	 * @see VariantSerializer
	 */
	public final <T> T optTypedObject(final T fallback, final VariantSerializer<T> serializer) {
		try {
			return getTypedObject(serializer);
		} catch (VariantException ex) {
			return fallback;
		}
	}

	/**
	 * Gets the value of this as a map of custom objects or returns a default.
	 *
	 * @param <T> the map value type to deserialize
	 * @param fallback {@code T} value to return in case of failure. Can be null.
	 * @param serializer {@code VariantSerializer<T>} serializer that will convert each {@code Variant} map value to a {@code T}
	 * @return a map of custom objects deserialized from this, or {@code fallback}
	 * if kind of this is not {@link VariantKind#MAP}
	 * @throws IllegalArgumentException if {@code serializer} is null
	 *
	 * @see VariantSerializer
	 */
	public final <T> Map<String, T> optTypedMap(final Map<String, T> fallback, final VariantSerializer<T> serializer) {
		try {
			return getTypedMap(serializer);
		} catch (VariantException ex) {
			return fallback;
		}
	}

	/**
	 * Gets the value of this as a list of custom objects or returns a default.
	 *
	 * @param <T> the list element type to deserialize
	 * @param fallback {@code T} value to return in case of failure. Can be null.
	 * @param serializer {@code VariantSerializer<T>} serializer that will convert each {@code Variant} element to a {@code T}
	 * @return a list of custom objects deserialized from this, or {@code fallback}
	 * if kind of this is not {@link VariantKind#VECTOR}
	 * @throws IllegalArgumentException if {@code serializer} is null
	 *
	 * @see VariantSerializer
	 */
	public final <T> List<T> optTypedList(final List<T> fallback, final VariantSerializer<T> serializer) {
		try {
			return getTypedList(serializer);
		} catch (VariantException ex) {
			return fallback;
		}
	}

	/**
	 * Get the value of this as a {@code Map<String, String>} or returns a default.
	 *
	 * @param fallback value to return in case of failure. Can be null.
	 * @return a {@code Map<String, String>} derived from this, or {@code fallback} if the kind
	 * of this is not {@link VariantKind#MAP}
	 */
	public final Map<String, String> optStringMap(final Map<String, String> fallback) {
		try {
			return getStringMap();
		} catch (VariantException ex) {
			return fallback;
		}
	}

	/**
	 * Get the value of this as a {@code List<String>} or returns a default.
	 *
	 * @param fallback value to return if this is not a list (can be null)
	 * @return a {@code List<String>} derived from this, or {@code fallback} if the the get fails
	 */
	public final List<String> optStringList(final List<String> fallback) {
		try {
			return getStringList();
		} catch (VariantException ex) {
			return fallback;
		}
	}

	/**
	 * Two variants have the same hash code if they are the same kind and
	 * their values have the same hash code.
	 */
	@Override
	public final int hashCode() {
		final Object value = getValue();
		return (getKind().hashCode() + "," + (value == null ? "" : value.hashCode())).hashCode();
	}

	/**
	 * Convert value of this to a {@code String}.
	 *
	 * <p>The following types are convertible to string:</p>
	 * <ul>
	 *     <li>{@code string}</li>
	 *     <li>{@code int}, {@code long}, {@code double}</li>
	 *     <li>{@code boolean}</li>
	 * </ul>
	 *
	 * Other types will throw.
	 *
	 * @return a {@code String} derived from this
	 * @throws VariantException if this is not convertible to a string.
	 */
	public String convertToString() throws VariantException {
		throw new VariantException("value is not convertible to a string");
	}

	/**
	 * Convert value of this to a {@code double}.
	 *
	 * <p>The following types are convertible to double:</p>
	 * <ul>
	 *     <li>{@code int}, {@code long}, {@code double}</li>
	 *     <li>{@code string} if the value is a string representation of a double</li>
	 *     <li>{@code boolean} will be expressed as {@code 1.0} or {@code 0.0}</li>
	 * </ul>
	 *
	 * Other types will throw.
	 *
	 * @return a {@code String} derived from this
	 * @throws VariantException if this is not convertible to a string.
	 */
	public double convertToDouble() throws VariantException {
		throw new VariantException("value is not convertible to a double");
	}

	/**
	 * Two variants are equal if they are the same kind and have values that are equal.
	 */
	@Override
	public final boolean equals(final Object right) {
		if (this == right) {
			return true;
		}

		if (right == null) {
			return false;
		}

		if (!(right instanceof Variant)) {
			return false;
		}

		final Variant rightVariant = (Variant)right;
		final VariantKind myKind = getKind();

		if (myKind != rightVariant.getKind()) {
			return false;
		}

		final Object myValue = getValue();
		final Object rightValue = rightVariant.getValue();

		if (myValue == rightValue) {
			return true;
		}

		if (myValue == null || rightValue == null) {
			return false;
		}

		return myValue.equals(rightValue);
	}

	private Object getValue() {
		try {
			final VariantKind kind = getKind();

			switch (kind) {
				case BOOLEAN:
					return getBoolean();

				case STRING:
					return getString();

				case INTEGER:
					return getInteger();

				case LONG:
					return getLong();

				case DOUBLE:
					return getDouble();

				case NULL:
					return null;

				case MAP:
					return getVariantMap();

				case VECTOR:
					return getVariantList();

				default:
					throw new IllegalStateException(); // shouldn't happen
			}
		} catch (VariantException ex) {
			throw new IllegalStateException(ex); // shouldn't happen
		}
	}

	@Override
	public abstract Variant clone();

	/**
	 * @return the given key of the map, throwing if the key is missing or the value is null
	 * @param map {@code Map<String, Variant>} to search. Cannot be null.
	 * @param key {@code String} key. Cannot be null.
	 * @throws IllegalArgumentException if {@code key} or {@code map} is null
	 * @throws VariantKeyNotFoundException if {@code key} was not found in map
	 */
	public static Variant getVariantFromMap(final Map<String, Variant> map, final String key) throws VariantException {
		if (map == null) {
			throw new IllegalArgumentException();
		}

		if (key == null) {
			throw new IllegalArgumentException();
		}

		final Variant value = map.get(key);

		if (value == null) {
			throw new VariantKeyNotFoundException("missing key " + key);
		}

		return value;
	}

	/**
	 * @return the given key of the map, or a default value on failure
	 * @param map {@code Map<String, Variant>} to search. Cannot be null.
	 * @param key {@code String} key. Cannot be null.
	 * @param fallback {@code Variant} default value to return on failure
	 * @throws IllegalArgumentException if {@code key} or {@code map} is null
	 */
	public static Variant optVariantFromMap(final Map<String, Variant> map, final String key, final Variant fallback) {
		if (map == null) {
			throw new IllegalArgumentException();
		}

		if (key == null) {
			throw new IllegalArgumentException();
		}

		try {
			return getVariantFromMap(map, key);
		} catch (VariantException ex) {
			return fallback;
		}
	}

	/**
	 * @return the given key of the map, or variant null on failure
	 * @param map {@code Map<String, Variant>} to search. Cannot be null.
	 * @param key {@code String} key. Cannot be null.
	 * @throws IllegalArgumentException if {@code key} or {@code map} is null
	 */
	public static Variant optVariantFromMap(final Map<String, Variant> map, final String key) {
		return optVariantFromMap(map, key, Variant.fromNull());
	}

	public static Map<String, Variant> toVariantMap(final Map<?, ?> map) throws VariantException {
		return PermissiveVariantSerializer.DEFAULT_INSTANCE.serializeToVariantMap(map);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////
	// The following methods were added in order to ease merging. They will be removed once     //
	// AMSDK-6573 and AMSDK-6616 are complete.                                                  //
	//////////////////////////////////////////////////////////////////////////////////////////////

	@Deprecated
	static Variant fromObject(final Object o) {
		try {
			return PermissiveVariantSerializer.DEFAULT_INSTANCE.serialize(o);
		} catch (VariantException ex) {
			return new ObjectVariant(o);
		}
	}

	@Deprecated
	Object getObject() throws VariantException  {
		return getObject(Object.class);
	}

	@SuppressWarnings("unchecked")
	@Deprecated
	<T> T getObject(final Class<T> klass) throws VariantException  {
		final Object o = PermissiveVariantSerializer.DEFAULT_INSTANCE.deserialize(this);

		if (o == null) {
			return null;
		}

		if (!klass.isInstance(o)) {
			throw new VariantException();
		}

		return (T)o;
	}

	@SuppressWarnings("unchecked")
	@Deprecated
	<T> List<T> getObjectList(final Class<T> klass) throws ClassCastException {
		List untypedList;

		try {
			untypedList = getObject(List.class);
		} catch (VariantException ex) {
			throw new ClassCastException(); // weird yes, i know, but this method is temporary
		}

		if (untypedList == null) {
			return null;
		}

		final List<T> typedList = new ArrayList<T>();

		for (final Object untypedElement : untypedList) {
			typedList.add((T)untypedElement);
		}

		return typedList;
	}

	@Deprecated
	static final class ObjectVariant extends Variant {
		private final Object value;

		public ObjectVariant(final Object value) {
			this.value = value;
		}

		@Override
		public VariantKind getKind() {
			return VariantKind.OBJECT;
		}

		@Override
		<T> T getObject(final Class<T> klass) throws VariantException  {
			if (value == null) {
				return null;
			}

			if (!klass.isInstance(value)) {
				throw new VariantException();
			}

			return (T)value;
		}

		@Override
		public Variant clone() {
			return new ObjectVariant(this.value);
		}
	};
}
