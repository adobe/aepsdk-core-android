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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public final class StringVariantSerializerTests extends VariantTests {
	@Rule
	public ExpectedException exception = ExpectedException.none();

	private void testSerialize(final String value, final Variant expectedVariant) throws Exception {
		testSerialize(new StringVariantSerializer(), value, expectedVariant);
	}

	private void testSerialize(final String value, final Class<? extends Throwable> expectedException) throws Exception {
		testSerialize(new StringVariantSerializer(), value, expectedException);
	}

	private void testSerialize(final StringVariantSerializer serializer, final String value,
							   final Variant expectedVariant) throws Exception {
		final Variant variant = serializer.serialize(value);
		assertEquals(
			expectedVariant,
			variant
		);
	}

	private void testSerialize(final StringVariantSerializer serializer, final String value,
							   final Class<? extends Throwable> expectedException) throws Exception {
		exception.expect(expectedException);
		serializer.serialize(value);
	}

	@Test
	public void testSerializeNull() throws Exception {
		testSerialize(null, Variant.fromNull());
	}

	@Test
	public void testSerializeString() throws Exception {
		testSerialize("hi", Variant.fromString("hi"));
	}

	private void testDeserialize(final Variant variant, final String expectedValue) throws Exception {
		testDeserialize(new StringVariantSerializer(), variant, expectedValue);
	}

	private void testDeserialize(final Variant variant,
								 final Class<? extends Throwable> expectedException) throws Exception {
		testDeserialize(new StringVariantSerializer(), variant, expectedException);
	}

	private void testDeserialize(final StringVariantSerializer serializer, final Variant variant,
								 final String expectedValue) throws Exception {
		final String value = serializer.deserialize(variant);
		assertEquals(
			expectedValue,
			value
		);
	}

	private void testDeserialize(final StringVariantSerializer serializer, final Variant variant,
								 final Class<? extends Throwable> expectedException) throws Exception {
		exception.expect(expectedException);
		serializer.deserialize(variant);
	}

	@Test
	public void testDeserializeString() throws Exception {
		testDeserialize(Variant.fromString("hi"), "hi");
	}

	@Test
	public void testDeserializeBoolean() throws Exception {
		testDeserialize(Variant.fromBoolean(true), "true");
	}

	@Test
	public void testDeserializeInteger() throws Exception {
		testDeserialize(Variant.fromInteger(42), "42");
	}

	@Test
	public void testDeserializeLong() throws Exception {
		testDeserialize(Variant.fromLong(42L), "42");
	}

	@Test
	public void testDeserializeDouble() throws Exception {
		testDeserialize(Variant.fromDouble(42.5), "42.5");
	}

	@Test
	public void testDeserializeMap() throws Exception {
		testDeserialize(Variant.fromVariantMap(new HashMap<String, Variant>()), VariantException.class);
	}

	@Test
	public void testDeserializeList() throws Exception {
		testDeserialize(Variant.fromVariantList(new ArrayList<Variant>()), VariantException.class);
	}

	@Test
	public void testDeserializeVariantNull() throws Exception {
		testDeserialize(Variant.fromNull(), (String)null);
	}
}
