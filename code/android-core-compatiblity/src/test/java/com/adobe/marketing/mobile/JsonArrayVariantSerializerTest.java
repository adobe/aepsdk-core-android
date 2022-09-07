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
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public final class JsonArrayVariantSerializerTest {
	private JsonUtilityService jsonUtilityService = new FakeJsonUtilityService();
	private VariantSerializer<JsonUtilityService.JSONArray> serializer = new JsonArrayVariantSerializer(jsonUtilityService);

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	public void testSerialize(final List<Variant> expectedVariantList, final String jsonString) throws Exception {
		final JsonUtilityService.JSONArray jsonArray = jsonUtilityService.createJSONArray(jsonString);
		final Variant variant = serializer.serialize(jsonArray);
		Variant expectedVariant = Variant.fromVariantList(expectedVariantList);
		assertEquals(expectedVariant, variant);
	}

	@Test
	public void testSerializeNull() throws Exception {
		assertEquals(Variant.fromNull(), serializer.serialize(null));
	}

	@Test
	public void testSerializeEmptyArray() throws Exception {
		testSerialize(
			Collections.<Variant>emptyList(),
			"[]"
		);
	}

	@Test
	public void testSerializeArrayWithMultipleElements() throws Exception {
		testSerialize(
			Arrays.asList(Variant.fromString("a"), Variant.fromString("b"), Variant.fromString("c")),
			"[\"a\",\"b\",\"c\"]"
		);
	}

	@Test
	public void testSerializeArrayWithBoolean() throws Exception {
		testSerialize(
			Arrays.asList(Variant.fromBoolean(true)),
			"[true]"
		);
	}

	@Test
	public void testSerializeArrayWithDouble() throws Exception {
		testSerialize(
			Arrays.asList(Variant.fromDouble(4.2)),
			"[4.2]"
		);
	}

	@Test
	public void testSerializeArrayWithInteger() throws Exception {
		testSerialize(
			Arrays.asList(Variant.fromInteger(42)),
			"[42]"
		);
	}

	@Test
	public void testSerializeArrayWithLong() throws Exception {
		testSerialize(
			Arrays.asList(Variant.fromLong(2147483648L)),
			"[2147483648]"
		);
	}

	@Test
	public void testSerializeArrayWithNull() throws Exception {
		testSerialize(
			Arrays.asList(Variant.fromNull()),
			"[null]"
		);
	}

	@Test
	public void testSerializeArrayWithObject() throws Exception {
		testSerialize(
			Arrays.asList(Variant.fromStringMap(Collections.singletonMap("k", "v"))),
			"[{\"k\": \"v\"}]"
		);
	}

	@Test
	public void testSerializeArrayWithArray() throws Exception {
		testSerialize(
			Arrays.asList(Variant.fromStringList(Arrays.asList("s"))),
			"[[\"s\"]]"
		);
	}

	public void testDeserialize(final String expectedJsonString, final List<Variant> variant) throws Exception {
		final JsonUtilityService.JSONArray expectedJsonArray = jsonUtilityService.createJSONArray(expectedJsonString);
		final JsonUtilityService.JSONArray jsonArray = serializer.deserialize(Variant.fromVariantList(variant));
		assertEquals(expectedJsonArray, jsonArray);
	}

	@Test
	public void testDeserializeVariantNull() throws Exception {
		assertEquals(null,  serializer.deserialize(Variant.fromNull()));
	}

	@Test
	public void testDeserializeEmptyArray() throws Exception {
		testDeserialize("[]", Collections.<Variant>emptyList());
	}

	@Test
	public void testDeserializeArrayWithMultipleElements() throws Exception {
		testDeserialize("[\"a\",\"b\",\"c\"]", Arrays.asList(Variant.fromString("a"), Variant.fromString("b"),
						Variant.fromString("c")));
	}

	@Test
	public void testDeserializeArrayWithBoolean() throws Exception {
		testDeserialize("[true]", Arrays.asList(Variant.fromBoolean(true)));
	}

	@Test
	public void testDeserializeArrayWithDouble() throws Exception {
		testDeserialize("[4.2]", Arrays.asList(Variant.fromDouble(4.2)));
	}

	@Test
	public void testDeserializeArrayWithInteger() throws Exception {
		testDeserialize("[42]", Arrays.asList(Variant.fromInteger(42)));
	}

	@Test
	public void testDeserializeArrayWithLong() throws Exception {
		testDeserialize("[2147483648]", Arrays.asList(Variant.fromLong(2147483648L)));
	}

	@Test
	public void testDeserializeArrayWithNull() throws Exception {
		testDeserialize("[null]", Arrays.asList(Variant.fromNull()));
	}

	@Test
	public void testDeserializeArrayWithObject() throws Exception {
		testDeserialize("[{\"k\": \"v\"}]", Arrays.asList(Variant.fromStringMap(Collections.singletonMap("k", "v"))));
	}

	@Test
	public void testDeserializeArrayWithArray() throws Exception {
		testDeserialize("[[\"s\"]]", Arrays.asList(Variant.fromStringList(Arrays.asList("s"))));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDeserializeNullFails() throws Exception {
		serializer.deserialize(null);
	}

	@Test(expected = VariantKindException.class)
	public void testDeserializeBooleanFails() throws Exception {
		serializer.deserialize(Variant.fromBoolean(true));
	}

	@Test(expected = VariantKindException.class)
	public void testDeserializeLongFails() throws Exception {
		serializer.deserialize(Variant.fromLong(42L));
	}

	@Test(expected = VariantKindException.class)
	public void testDeserializeIntegerFails() throws Exception {
		serializer.deserialize(Variant.fromInteger(42));
	}

	@Test(expected = VariantKindException.class)
	public void testDeserializeMapFails() throws Exception {
		serializer.deserialize(Variant.fromStringMap(Collections.singletonMap("k", "v")));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIfConstructorThrowsWithNullJsonUtilityService() {
		new JsonArrayVariantSerializer(null);
	}
}
