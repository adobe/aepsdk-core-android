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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public final class TypedMapVariantTests extends TypedVariantTests {
	private final VariantSerializer<Food> elementSerializer = new FoodSerializer();

	private final TypedMapVariantSerializer<Food> mapSerializer = new TypedMapVariantSerializer<Food>(new FoodSerializer());

	private final Map<String, Food> foodMap = new HashMap<String, Food>();

	private final Map<String, Variant> variantMap = new HashMap<String, Variant>();

	private void testSerialize(final Map<String, Food> value, final Map<String, Variant> expectedVariantMap) {
		final Variant expectedVariant = Variant.fromVariantMap(expectedVariantMap);
		testSerialize(value, expectedVariant);
	}


	private void testSerialize(final Map<String, Food> value, final Variant expectedVariant) {
		assertEquals(expectedVariant, Variant.fromTypedMap(value, new FoodSerializer()));
		assertEquals(expectedVariant, mapSerializer.serialize(value));
		assertEquals(expectedVariant, mapSerializer.serializeMap(value));
		assertEquals(expectedVariant.optVariantMap(null), mapSerializer.serializeMapToVariantMap(value));
	}

	@Test
	public void testSerializeNull() throws Exception {
		testSerialize(null, Variant.fromNull());
	}

	@Test
	public void testSerializeEmptyMap() throws Exception {
		testSerialize(foodMap, variantMap);
	}

	@Test
	public void testSerializeMapWithOneEntry() throws Exception {
		foodMap.put("key1", SPAGHETTI);
		variantMap.put("key1", SPAGHETTI_VARIANT);
		testSerialize(foodMap, variantMap);
	}

	@Test
	public void testSerializeMapWithSubclass() throws Exception {
		foodMap.put("key1", FRUIT);
		variantMap.put("key1", FRUIT_VARIANT);
		testSerialize(foodMap, variantMap);
	}

	@Test
	public void testSerializeMapWithMultipleEntries() throws Exception {
		foodMap.put("key1", SPAGHETTI);
		foodMap.put("key2", BURRITO);
		foodMap.put("key3", FRUIT);
		variantMap.put("key1", SPAGHETTI_VARIANT);
		variantMap.put("key2", BURRITO_VARIANT);
		variantMap.put("key3", FRUIT_VARIANT);
		testSerialize(foodMap, variantMap);
	}

	@Test
	public void testSerializeMapWithNullValue() throws Exception {
		foodMap.put("key1", SPAGHETTI);
		foodMap.put("key2", null);
		foodMap.put("key3", FRUIT);
		variantMap.put("key1", SPAGHETTI_VARIANT);
		variantMap.put("key2", Variant.fromNull());
		variantMap.put("key3", FRUIT_VARIANT);
		testSerialize(foodMap, variantMap);
	}

	@Test
	public void testSerializeMapWithNullKey() throws Exception {
		foodMap.put("key1", SPAGHETTI);
		foodMap.put(null, BURRITO);
		foodMap.put("key3", FRUIT);
		variantMap.put("key1", SPAGHETTI_VARIANT);
		variantMap.put("key3", FRUIT_VARIANT);
		testSerialize(foodMap, variantMap);
	}

	@Test
	public void testSerializeMapWithUnserializableValue() throws Exception {
		foodMap.put("key1", SPAGHETTI);
		foodMap.put("key2", FOOD_THAT_THROWS);
		foodMap.put("key3", FRUIT);
		variantMap.put("key1", SPAGHETTI_VARIANT);
		variantMap.put("key3", FRUIT_VARIANT);
		testSerialize(foodMap, variantMap);
	}

	private void testDeserialize(final Map<String, Variant> variantMap,
								 final Map<String, Food> expectedValue) throws Exception {
		final Variant variant = Variant.fromVariantMap(variantMap);
		testDeserialize(variant, expectedValue);
	}

	private void testDeserialize(final Variant variant, final Map<String, Food> expectedValue) throws Exception {
		assertEquals(expectedValue, variant.getTypedMap(elementSerializer));
		assertEquals(expectedValue, mapSerializer.deserialize(variant));
		assertEquals(expectedValue, mapSerializer.deserializeMap(variant.optVariantMap(null)));
	}

	@Test
	public void testDeserializeNull() throws Exception {
		final Variant variant = Variant.fromNull();
		assertEquals(null, mapSerializer.deserialize(variant));
		assertEquals(null, mapSerializer.deserializeMap(variant.optVariantMap(null)));
	}

	@Test
	public void testDeserializeEmptyMap() throws Exception {
		testDeserialize(variantMap, foodMap);
	}

	@Test
	public void testDeserializeMapWithOneEntry() throws Exception {
		variantMap.put("key1", SPAGHETTI_VARIANT);
		foodMap.put("key1", SPAGHETTI);
		testDeserialize(variantMap, foodMap);
	}

	@Test
	public void testDeserializeMapWithSubclass() throws Exception {
		variantMap.put("key1", FRUIT_VARIANT);
		foodMap.put("key1", FRUIT);
		testDeserialize(variantMap, foodMap);
	}

	@Test
	public void testDeserializeMapWithMultipleEntries() throws Exception {
		variantMap.put("key1", SPAGHETTI_VARIANT);
		variantMap.put("key2", BURRITO_VARIANT);
		variantMap.put("key3", FRUIT_VARIANT);
		foodMap.put("key1", SPAGHETTI);
		foodMap.put("key2", BURRITO);
		foodMap.put("key3", FRUIT);
		testDeserialize(variantMap, foodMap);
	}

	@Test
	public void testDeserializeMapWithUnserializableValue() throws Exception {
		variantMap.put("key1", SPAGHETTI_VARIANT);
		variantMap.put("key2", FOOD_VARIANT_THAT_THROWS);
		variantMap.put("key3", FRUIT_VARIANT);
		foodMap.put("key1", SPAGHETTI);
		foodMap.put("key3", FRUIT);
		testDeserialize(variantMap, foodMap);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFromTypedMapThrowsWhenPassedNullSerializer() {
		Variant.fromTypedMap(foodMap, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetTypedMapThrowsWhenPassedNullSerializer() throws Exception {
		variantMap.put("key1", SPAGHETTI_VARIANT);
		Variant.fromVariantMap(variantMap).getTypedMap(null);
	}

	@Test(expected = VariantKindException.class)
	public void testGetTypedMapThrowsWhenVariantIsNotAMap() throws Exception {
		final Variant variant = Variant.fromInteger(1);
		variant.getTypedMap(elementSerializer);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSerializerConstructorThrowsWhenPassedNullSerializer() {
		new TypedMapVariantSerializer<Food>(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSerializerDeserializeThrowsWhenPassedNull() throws Exception {
		mapSerializer.deserialize(null);
	}

	@Test
	public void testSerializerDeserializeMapNull() throws Exception {
		assertNull(mapSerializer.deserializeMap(null));
	}
}
