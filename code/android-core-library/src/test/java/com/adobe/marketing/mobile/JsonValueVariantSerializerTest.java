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

import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public final class JsonValueVariantSerializerTest {
	@Rule
	public final ExpectedException exception = ExpectedException.none();

	private final JsonUtilityService jsonUtilityService = new FakeJsonUtilityService();

	private final JsonValueVariantSerializer serializer = new JsonValueVariantSerializer(jsonUtilityService);

	private void testSerialize(final Variant expectedVariant, final String jsonString) throws Exception {
		final JsonUtilityService.JSONObject objectContainingValue = jsonUtilityService.createJSONObject("{ \"v\" : " +
				jsonString + " }");
		final Object value = objectContainingValue.get("v");
		final Variant variant = serializer.serialize(value);
		assertEquals(expectedVariant, variant);
	}

	@Test
	public void testSerializeNull() throws Exception {
		assertEquals(Variant.fromNull(), serializer.serialize(null));
	}

	@Test
	public void testSerializeBoolean() throws Exception {
		testSerialize(
			Variant.fromBoolean(true),
			"true"
		);
	}

	@Test
	public void testSerializeDouble() throws Exception {
		testSerialize(
			Variant.fromDouble(4.2),
			"4.2"
		);
	}

	@Test
	public void testSerializeInteger() throws Exception {
		testSerialize(
			Variant.fromInteger(42),
			"42"
		);
	}

	@Test
	public void testSerializeLong() throws Exception {
		testSerialize(
			Variant.fromLong(2147483648L),
			"2147483648"
		);
	}

	@Test
	public void testSerializeVariantNull() throws Exception {
		testSerialize(
			Variant.fromNull(),
			"null"
		);
	}

	@Test
	public void testSerializeMap() throws Exception {
		testSerialize(
		Variant.fromVariantMap(new HashMap<String, Variant>() {
			{
				put("k1", Variant.fromString("v1"));
			}
		}),
		"{\"k1\":\"v1\"}"
		);
	}

	@Test
	public void testSerializeList() throws Exception {
		testSerialize(
			Variant.fromVariantList(Arrays.asList(Variant.fromString("a"))),
			"[\"a\"]"
		);
	}

	private void testDeserialize(final String expectedJsonString, final Variant variant) throws Exception {
		final Object value = serializer.deserialize(variant);
		final JsonUtilityService.JSONObject mapContainingExpectedValue = jsonUtilityService.createJSONObject("{ \"v\" : " +
				expectedJsonString + " }");
		final Object expectedValue = mapContainingExpectedValue.get("v");
		assertEquals(expectedValue, value);
	}

	private void testDeserialize(final Class<? extends Exception> expectedException,
								 final Variant variant) throws Exception {
		exception.expect(expectedException);
		serializer.deserialize(variant);
	}

	@Test
	public void testDeserializeBoolean() throws Exception {
		testDeserialize("true", Variant.fromBoolean(true));
	}

	@Test
	public void testDeserializeDouble() throws Exception {
		testDeserialize("4.2", Variant.fromDouble(4.2));
	}

	@Test
	public void testDeserializeInteger() throws Exception {
		testDeserialize("42", Variant.fromInteger(42));
	}

	@Test
	public void testDeserializeLong() throws Exception {
		testDeserialize("2147483648", Variant.fromLong(2147483648L));
	}

	@Test
	public void testDeserializeVariantNull() throws Exception {
		testDeserialize("null", Variant.fromNull());
	}

	@Test
	public void testDeserializeMap() throws Exception {
		testDeserialize("{\"k1\":\"v1\"}", Variant.fromVariantMap(new HashMap<String, Variant>() {
			{
				put("k1", Variant.fromString("v1"));
			}
		}));
	}

	@Test
	public void testDeserializeList() throws Exception {
		testDeserialize("[\"a\"]", Variant.fromVariantList(Arrays.asList(Variant.fromString("a"))));
	}

	@Test
	public void testDeserializeNull() throws Exception {
		testDeserialize(IllegalArgumentException.class, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIfConstructorThrowsWithNullJsonUtilityService() {
		new JsonObjectVariantSerializer(null);
	}
}
