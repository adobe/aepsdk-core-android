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

/**
 * A {@code VariantSerializer} can serialize a custom object to a {@code Variant} and deserialize a
 * custom object from a {@code Variant}.
 *
 * <p>To enable serialization of {@code MyCustomClass} (and maps/lists of {@code MyCustomClass}):</p>
 * <ul>
 *     <li>
 *         Write a {@code MyCustomClassVariantSerializer} that implements
 *         {@code VariantSerializer<MyCustomClass>}
 *     </li>
 *     <li>
 *         Pass an instance of the serializer to a  "TypedObject", "TypedList", or "TypedMap"
 *         methods of {@link Variant} (such as {@code Variant.getTypedObject} or
 *         {@code Variant.fromTypedObject}).
 *     </li>
 * </ul>
 *
 */
interface VariantSerializer<T> {
	/**
	 * Serializes the given {@code T} to a {@code Variant}.
	 *
	 * {@link VariantSerializationFailedException}s thrown by this function will be bubbled up.
	 *
	 * Other exceptions thrown by this function will be wrapped in a
	 * {@code VariantSerializationFailedException} and bubbled up.
	 *
	 * @param value {@code T} instance to serialize (can be null)
	 * @return a {@code Variant} representing {@code T}, or the null variant if {@code value} is null
	 *
	 * @throws VariantException if serialization failed
	 */
	Variant serialize(final T value) throws VariantException;

	/**
	 * Deserializes the given {@code Variant} to a {@code T}.
	 *
	 * {@link VariantSerializationFailedException}s thrown by this function will be bubbled up.
	 *
	 * Other exceptions thrown by this function will be wrapped in a
	 * {@code VariantSerializationFailedException} and bubbled up.
	 *
	 * @param variant {@code Variant} to deserialize (not null)
	 * @return a {@code T} that was deserialized from the variant. Can be null.
	 *
	 * @throws IllegalArgumentException if variant is null
	 * @throws VariantException if serialization failed
	 */
	T deserialize(final Variant variant) throws VariantException;
}
