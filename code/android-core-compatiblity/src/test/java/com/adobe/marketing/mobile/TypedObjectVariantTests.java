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

import static org.junit.Assert.assertEquals;

public final class TypedObjectVariantTests extends TypedVariantTests {
	private final VariantSerializer<Food> serializer = new FoodSerializer();

	private void testFrom(final Food value, final Variant expectedVariant) throws Exception {
		assertEquals(expectedVariant, Variant.fromTypedObject(value, serializer));
	}

	@Test
	public void testFromull() throws Exception {
		testFrom(null, Variant.fromNull());
	}

	@Test
	public void testFromNormal() throws Exception {
		testFrom(SPAGHETTI, SPAGHETTI_VARIANT);
	}

	@Test
	public void testFromSubclass() throws Exception {
		testFrom(FRUIT, FRUIT_VARIANT);
	}

	@Test
	public void testFromNull() throws Exception {
		testFrom(null, Variant.fromNull());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFromThrowsWhenPassedNullSerializer() throws Exception {
		Variant.fromTypedObject(SPAGHETTI, null);
	}

	@Test(expected = VariantSerializationFailedException.class)
	public void testFromThrowsWhenSerializerThrows() throws Exception {
		Variant.fromTypedObject(FOOD_THAT_THROWS, serializer);
	}

	private void testGet(final Variant variant, final Food expectedValue) throws Exception {
		assertEquals(expectedValue, variant.getTypedObject(serializer));
	}

	@Test
	public void testGetNormal() throws Exception {
		testGet(SPAGHETTI_VARIANT, SPAGHETTI);
	}

	@Test
	public void testGetSubclass() throws Exception {
		testGet(FRUIT_VARIANT, FRUIT);
	}

	@Test
	public void testGetVariantNull() throws Exception {
		testGet(Variant.fromNull(), null);
	}

	@Test(expected = VariantSerializationFailedException.class)
	public void testGetThrowsWhenSerializerThrows() throws Exception {
		FOOD_VARIANT_THAT_THROWS.getTypedObject(serializer);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetThrowsWhenPassedNullSerializer() throws Exception {
		SPAGHETTI_VARIANT.getTypedObject(null);
	}
}
