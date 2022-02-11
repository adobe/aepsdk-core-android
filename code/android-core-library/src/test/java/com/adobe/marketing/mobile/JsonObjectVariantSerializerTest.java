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
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public final class JsonObjectVariantSerializerTest {
	@Rule
	public final ExpectedException exception = ExpectedException.none();

	protected JsonUtilityService jsonUtilityService = new FakeJsonUtilityService();

	protected JsonObjectVariantSerializer serializer = new JsonObjectVariantSerializer(jsonUtilityService);

	private void testSerialize(final Variant expectedVariant, final String jsonString) throws Exception {
		final JsonUtilityService.JSONObject value = jsonUtilityService.createJSONObject(jsonString);
		final Variant variant = serializer.serialize(value);
		assertEquals(expectedVariant, variant);
	}

	private void testSerialize(final Class<? extends Throwable> expectedException,
							   final String jsonString) throws Exception {
		final JsonUtilityService.JSONObject value = jsonUtilityService.createJSONObject(jsonString);
		exception.expect(expectedException);
		serializer.serialize(value);
	}

	@Test
	public void testSerializeNull() throws Exception {
		assertEquals(Variant.fromNull(), serializer.serialize(null));
	}

	@Test
	public void testSerializeEmptyMap() throws Exception {
		testSerialize(
			Variant.fromVariantMap(Collections.<String, Variant>emptyMap()),
			"{}"
		);
	}

	@Test
	public void testSerializeMap() throws Exception {
		testSerialize(
		Variant.fromVariantMap(new HashMap<String, Variant>() {
			{
				put("k1", Variant.fromString("v1"));
				put("k2", Variant.fromString("v2"));
				put("k3", Variant.fromString("v3"));
			}
		}),
		"{\"k1\":\"v1\",\"k2\":\"v2\",\"k3\":\"v3\"}"
		);
	}

	@Test
	public void testSerializeMapWithBoolean() throws Exception {
		testSerialize(
		Variant.fromVariantMap(new HashMap<String, Variant>() {
			{
				put("k1", Variant.fromBoolean(true));
			}
		}),
		"{\"k1\":true}"
		);
	}

	@Test
	public void testSerializeMapWithDouble() throws Exception {
		testSerialize(
		Variant.fromVariantMap(new HashMap<String, Variant>() {
			{
				put("k1", Variant.fromDouble(4.2));
			}
		}),
		"{\"k1\":4.2}"
		);
	}

	@Test
	public void testSerializeMapWithInteger() throws Exception {
		testSerialize(
		Variant.fromVariantMap(new HashMap<String, Variant>() {
			{
				put("k1", Variant.fromInteger(42));
			}
		}),
		"{\"k1\":42}"
		);
	}

	@Test
	public void testSerializeMapWithLong() throws Exception {
		testSerialize(
		Variant.fromVariantMap(new HashMap<String, Variant>() {
			{
				put("k1", Variant.fromLong(2147483648L));
			}
		}),
		"{\"k1\":2147483648}"
		);
	}

	@Test
	public void testSerializeMapWithNull() throws Exception {
		testSerialize(
		Variant.fromVariantMap(new HashMap<String, Variant>() {
			{
				put("k1", Variant.fromNull());
			}
		}),
		"{\"k1\":null}"
		);
	}

	@Test
	public void testSerializeMapWithString() throws Exception {
		testSerialize(
		Variant.fromVariantMap(new HashMap<String, Variant>() {
			{
				final Variant innerMap = Variant.fromVariantMap(new HashMap<String, Variant>() {
					{
						put("sk1", Variant.fromString("sv1"));
					}
				});
				put("k1", innerMap);
			}
		}),
		"{\"k1\":{\"sk1\":\"sv1\"}}"
		);
	}

	@Test
	public void testSerializeMapWithArray() throws Exception {
		testSerialize(
		Variant.fromVariantMap(new HashMap<String, Variant>() {
			{
				put("k1", Variant.fromVariantList(Arrays.asList(Variant.fromString("a"))));
			}
		}),
		"{\"k1\":[\"a\"]}"
		);
	}

	private void testDeserialize(final Class<? extends Throwable> expectedException,
								 final Variant variant) throws Exception {
		exception.expect(expectedException);
		serializer.deserialize(variant);
	}

	private void testDeserialize(final String expectedJsonString, final Variant variant) throws Exception {
		final JsonUtilityService.JSONObject expectedValue = jsonUtilityService.createJSONObject(expectedJsonString);
		final JsonUtilityService.JSONObject value = serializer.deserialize(variant);
		assertEquals(expectedValue, value);
	}

	@Test
	public void testDeserializeVariantNull() throws Exception {
		assertEquals(null,  serializer.deserialize(Variant.fromNull()));
	}

	@Test
	public void testDeserializeNull() throws Exception {
		testDeserialize(IllegalArgumentException.class, null);
	}

	@Test
	public void testDeserializeEmptyMap() throws Exception {
		testDeserialize(
			"{}",
			Variant.fromVariantMap(Collections.<String, Variant>emptyMap())
		);
	}

	@Test
	public void testDeserializeMap() throws Exception {
		testDeserialize(
			"{\"k1\":\"v1\",\"k2\":\"v2\",\"k3\":\"v3\"}",
		Variant.fromVariantMap(new HashMap<String, Variant>() {
			{
				put("k1", Variant.fromString("v1"));
				put("k2", Variant.fromString("v2"));
				put("k3", Variant.fromString("v3"));
			}
		})
		);
	}

	@Test
	public void testDeserializeMapWithBoolean() throws Exception {
		testDeserialize(
			"{\"k1\":true}",
		Variant.fromVariantMap(new HashMap<String, Variant>() {
			{
				put("k1", Variant.fromBoolean(true));
			}
		})
		);
	}

	@Test
	public void testDeserializeMapWithDouble() throws Exception {
		testDeserialize(
			"{\"k1\":4.2}",
		Variant.fromVariantMap(new HashMap<String, Variant>() {
			{
				put("k1", Variant.fromDouble(4.2));
			}
		})
		);
	}

	@Test
	public void testDeserializeMapWithInteger() throws Exception {
		testDeserialize(
			"{\"k1\":42}",
		Variant.fromVariantMap(new HashMap<String, Variant>() {
			{
				put("k1", Variant.fromInteger(42));
			}
		})
		);
	}

	@Test
	public void testDeserializeMapWithLong() throws Exception {
		testDeserialize(
			"{\"k1\":2147483648}",
		Variant.fromVariantMap(new HashMap<String, Variant>() {
			{
				put("k1", Variant.fromLong(2147483648L));
			}
		})
		);
	}

	@Test
	public void testDeserializeMapWithNull() throws Exception {
		testDeserialize(
			"{\"k1\":null}",
		Variant.fromVariantMap(new HashMap<String, Variant>() {
			{
				put("k1", Variant.fromNull());
			}
		})
		);
	}

	@Test
	public void testDeserializeMapWithMap() throws Exception {
		testDeserialize(
			"{\"k1\":{\"sk1\":\"sv1\"}}",
		Variant.fromVariantMap(new HashMap<String, Variant>() {
			{
				final Variant innerMap = Variant.fromVariantMap(new HashMap<String, Variant>() {
					{
						put("sk1", Variant.fromString("sv1"));
					}
				});
				put("k1", innerMap);
			}
		})
		);
	}

	@Test
	public void testDeserializeMapWithArray() throws Exception {
		testDeserialize(
			"{\"k1\":[\"a\"]}",
		Variant.fromVariantMap(new HashMap<String, Variant>() {
			{
				put("k1", Variant.fromVariantList(Arrays.asList(Variant.fromString("a"))));
			}
		})
		);
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

	@Test(expected = IllegalArgumentException.class)
	public void testIfConstructorThrowsWithNullJsonUtilityService() {
		new JsonObjectVariantSerializer(null);
	}
}
