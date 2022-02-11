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

/**
 * VariantSerializer implementation for typed lists.
 *
 * Used by {@link Variant} and {@link EventData} implementations of get/put/opt operations on
 * typed lists.
 */
final class TypedListVariantSerializer<T> implements VariantSerializer<List<T>> {
	private final VariantSerializer<T> elementSerializer;

	/**
	 * Constructor.
	 *
	 * @param elementSerializer {@code VariantSerializer<T>} serializer for list elements
	 */
	public TypedListVariantSerializer(final VariantSerializer<T> elementSerializer) {
		if (elementSerializer == null) {
			throw new IllegalArgumentException();
		}

		this.elementSerializer = elementSerializer;
	}

	@Override
	public Variant serialize(final List<T> list) {
		return serializeList(list);
	}

	/**
	 * Serializes {@code List<T>} to a {@code Variant}.
	 *
	 * Null elements will be serialized to null variants.
	 * Elements where serialization fails will be omitted from the resulting vector variant.
	 *
	 * @param list (nullable) list to serialize
	 * @return vector variant serialization of {@code list}, or a null variant if {@code list} is
	 * null
	 */
	public Variant serializeList(final List<? extends T> list) {
		if (list == null) {
			return Variant.fromNull();
		}

		final List<Variant> serializedList = new ArrayList<Variant>();

		for (final T value : list) {
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

			serializedList.add(serializedValue);
		}

		return Variant.fromVariantList(serializedList);
	}

	@Override
	public List<T> deserialize(final Variant variant) throws VariantException  {
		if (variant == null) {
			throw new IllegalArgumentException();
		}

		if (variant.getKind() == VariantKind.NULL) {
			return null;
		}

		final List<Variant> variantList = variant.getVariantList();
		return deserializeList(variantList);
	}

	/**
	 * Deserializes {@code List<Variant>} to a {@code List<T>}.
	 *
	 * Null variant elements will be deserialized to null elements.
	 * Elements where deserialization fails will be omitted from the resulting list.
	 *
	 * @param variantList {@code List<Variant>} to deserialize
	 * @return deserialization of {@code variantList} to {@code List<T>}, or null if
	 * {@code variantList} is null
	 */
	public List<T> deserializeList(final List<Variant> variantList) {
		if (variantList == null) {
			return null;
		}

		final List<T> list = new ArrayList<T>();

		for (final Variant elementVariant : variantList) {
			if (elementVariant == null) {
				continue;
			}

			T element;

			if (elementVariant.getKind() == VariantKind.NULL) {
				element = null;
			} else {
				try {
					element = elementVariant.getTypedObject(elementSerializer);
				} catch (VariantException ex) {
					continue;
				}
			}

			list.add(element);
		}

		return list;
	}
}
