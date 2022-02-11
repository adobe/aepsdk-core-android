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

import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.Rule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public final class TypedListVariantTests extends TypedVariantTests {
	private final VariantSerializer<Food> serializer = new FoodSerializer();
	private final TypedListVariantSerializer<Food> listSerializer = new TypedListVariantSerializer<Food>
	(new FoodSerializer());
	private final List<Food> foodList = new ArrayList<Food>();
	private final List<Variant> variantList = new ArrayList<Variant>();

	private void testSerialize(final List<Food> value, final Variant expectedVariant) {
		assertEquals(expectedVariant, Variant.fromTypedList(value, serializer));
		assertEquals(expectedVariant, listSerializer.serialize(value));
		assertEquals(expectedVariant, listSerializer.serializeList(value));
	}

	private void testSerialize(final List<Food> value, final List<Variant> expectedVariantList) {
		final Variant expectedVariant = Variant.fromVariantList(expectedVariantList);
		testSerialize(value, expectedVariant);
	}

	@Test
	public void testSerializeNull() {
		testSerialize(null, Variant.fromNull());
	}

	@Test
	public void testSerializeEmptyList() {
		testSerialize(foodList, variantList);
	}

	@Test
	public void testSerializeListWithOneElement() {
		foodList.add(SPAGHETTI);
		testSerialize(foodList,
					  Arrays.asList(SPAGHETTI_VARIANT)
					 );
	}

	@Test
	public void testSerializeListWithSubclassElement() {
		foodList.add(FRUIT);
		testSerialize(foodList,
					  Arrays.asList(FRUIT_VARIANT)
					 );
	}

	@Test
	public void testSerializeListWithMultipleElements() {
		foodList.add(SPAGHETTI);
		foodList.add(BURRITO);
		foodList.add(FRUIT);
		variantList.add(SPAGHETTI_VARIANT);
		variantList.add(BURRITO_VARIANT);
		variantList.add(FRUIT_VARIANT);
		testSerialize(foodList, variantList);
	}

	@Test
	public void testSerializeListWithNullElement() {
		foodList.add(SPAGHETTI);
		foodList.add(null);
		foodList.add(FRUIT);
		variantList.add(SPAGHETTI_VARIANT);
		variantList.add(Variant.fromNull());
		variantList.add(FRUIT_VARIANT);
		testSerialize(foodList, variantList);
	}

	@Test
	public void testSerializeListWithUnserializableElement() {
		foodList.add(SPAGHETTI);
		foodList.add(FOOD_THAT_THROWS);
		foodList.add(FRUIT);
		variantList.add(SPAGHETTI_VARIANT);
		variantList.add(FRUIT_VARIANT);
		testSerialize(foodList, variantList);
	}

	private void testDeserialize(final List<Variant> variantList, final List<Food> expectedValue) throws Exception {
		final Variant variant = Variant.fromVariantList(variantList);
		testDeserialize(variant, expectedValue);
	}

	private void testDeserialize(final Variant variant, final List<Food> expectedValue) throws Exception {
		assertEquals(expectedValue, variant.getTypedList(serializer));

		List<Food> value = null;

		try {
			value = listSerializer.deserialize(variant);
		} catch (Exception ex) {}

		assertEquals(expectedValue, value);

		try {
			value = listSerializer.deserializeList(variant.optVariantList(null));
		} catch (Exception ex) {}

		assertEquals(expectedValue, value);
	}

	@Test
	public void testDeserializeNull() throws Exception {
		assertNull(listSerializer.deserialize(Variant.fromNull()));
		assertNull(listSerializer.deserializeList(null));
	}

	@Test
	public void testDeserializeEmptyList() throws Exception {
		testDeserialize(variantList, foodList);
	}

	@Test
	public void testDeserializeListWithOneElement() throws Exception {
		variantList.add(SPAGHETTI_VARIANT);
		foodList.add(SPAGHETTI);
		testDeserialize(variantList, foodList);
	}

	@Test
	public void testDeserializeListWithSubclass() throws Exception {
		variantList.add(FRUIT_VARIANT);
		foodList.add(FRUIT);
		testDeserialize(variantList, foodList);
	}

	@Test
	public void testDeserializeListWithMultipleElements() throws Exception {
		variantList.add(SPAGHETTI_VARIANT);
		variantList.add(BURRITO_VARIANT);
		variantList.add(FRUIT_VARIANT);
		foodList.add(SPAGHETTI);
		foodList.add(BURRITO);
		foodList.add(FRUIT);
		testDeserialize(variantList, foodList);
	}

	@Test
	public void testDeserializeListWithUnserializableElement() throws Exception {
		variantList.add(SPAGHETTI_VARIANT);
		variantList.add(FOOD_VARIANT_THAT_THROWS);
		variantList.add(FRUIT_VARIANT);
		foodList.add(SPAGHETTI);
		foodList.add(FRUIT);
		testDeserialize(variantList, foodList);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFromTypedListThrowsWhenPassedNullSerializer() {
		Variant.fromTypedList(Arrays.asList(SPAGHETTI), null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetTypeListThrowsWhenPassedNullSerializer() throws Exception {
		final Variant variant = Variant.fromVariantList(Arrays.asList(SPAGHETTI_VARIANT));
		variant.getTypedList(null);
	}

	@Test(expected = VariantKindException.class)
	public void testGetTypeListThrowsWhenVariantIsNotAVector() throws Exception {
		final Variant variant = Variant.fromInteger(1);
		variant.getTypedList(new FoodSerializer());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSerializerConstructorThrowsWhenPassedNullSerializer() {
		final TypedListVariantSerializer<Food> serializer = new TypedListVariantSerializer<Food>(null);
	}
}
