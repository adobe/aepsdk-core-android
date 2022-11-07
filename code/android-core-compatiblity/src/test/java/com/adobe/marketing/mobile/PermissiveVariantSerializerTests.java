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

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;

public final class PermissiveVariantSerializerTests extends VariantTests {
	@Rule
	public ExpectedException exception = ExpectedException.none();

	private static Map<String, Object> mapWithDepth(int depth) {
		final Map<String, Object> map = new HashMap<String, Object>();

		if (depth > 0) {
			map.put("k", mapWithDepth(depth - 1));
		}

		return map;
	}

	private static Variant variantMapWithDepth(int depth) {
		final Map<String, Variant> map = new HashMap<String, Variant>();

		if (depth > 0) {
			map.put("k", variantMapWithDepth(depth - 1));
		}

		return Variant.fromVariantMap(map);
	}

	private void testSerialize(final Object value, final Variant expectedVariant) throws Exception {
		testSerialize(PermissiveVariantSerializer.DEFAULT_INSTANCE, value, expectedVariant);
	}

	private void testSerialize(final Object value, final Class<? extends Throwable> expectedException) throws Exception {
		testSerialize(PermissiveVariantSerializer.DEFAULT_INSTANCE, value, expectedException);
	}

	private void testSerialize(final PermissiveVariantSerializer serializer, final Object value,
							   final Variant expectedVariant) throws Exception {
		final Variant variant = serializer.serialize(value);
		assertEquals(
			expectedVariant,
			variant
		);
	}

	private void testSerialize(final PermissiveVariantSerializer serializer, final Object value,
							   final Class<? extends Throwable> expectedException) throws Exception {
		exception.expect(expectedException);
		serializer.serialize(value);
	}

	@Test
	public void testSerializeString() throws Exception {
		testSerialize("hi", Variant.fromString("hi"));
	}

	@Test
	public void testSerializeBoolean() throws Exception {
		testSerialize(true, Variant.fromBoolean(true));
	}

	@Test
	public void testSerializeInteger() throws Exception {
		testSerialize(42, Variant.fromInteger(42));
	}

	@Test
	public void testSerializeLong() throws Exception {
		testSerialize(42L, Variant.fromLong(42L));
	}

	@Test
	public void testSerializeDouble() throws Exception {
		testSerialize(42.0, Variant.fromDouble(42.0));
	}

	@Test
	public void testSerializeMap() throws Exception {
		testSerialize(new HashMap<String, Integer>() {
			{
				put("k1", 1);
				put("k2", 2);
				put("k3", 3);
			}
		}, Variant.fromVariantMap(new HashMap<String, Variant>() {
			{
				put("k1", Variant.fromInteger(1));
				put("k2", Variant.fromInteger(2));
				put("k3", Variant.fromInteger(3));
			}
		}
								 ));
	}

	@Test
	public void testSerializeEmptyMap() throws Exception {
		testSerialize(new HashMap<String, Integer>(), Variant.fromVariantMap(new HashMap<String, Variant>()));
	}

	@Test
	public void testSerializeMapWithDifferentKindsOfValues() throws Exception {
		testSerialize(new HashMap<String, Serializable>() {
			{
				put("k1", 1);
				put("k2", "hi");
				put("k3", 42.0);
			}
		}, Variant.fromVariantMap(new HashMap<String, Variant>() {
			{
				put("k1", Variant.fromInteger(1));
				put("k2", Variant.fromString("hi"));
				put("k3", Variant.fromDouble(42.0));
			}
		}
								 ));
	}

	@Test
	public void testSerializeEmptyList() throws Exception {
		testSerialize(new ArrayList<Object>(), Variant.fromVariantList(new ArrayList<Variant>()));
	}

	@Test
	public void testSerializeList() throws Exception {
		testSerialize(Arrays.asList(1, 2, 3), Variant.fromVariantList(Arrays.asList(
						  Variant.fromInteger(1),
						  Variant.fromInteger(2),
						  Variant.fromInteger(3)
					  )));
	}

	@Test
	public void testSerializeListWithDifferentKindsOfValues() throws Exception {
		testSerialize(Arrays.asList(1, "hi", 42.0), Variant.fromVariantList(Arrays.asList(
						  Variant.fromInteger(1),
						  Variant.fromString("hi"),
						  Variant.fromDouble(42.0)
					  )));
	}

	@Test
	public void testSerializeVariant() throws Exception {
		testSerialize(Variant.fromInteger(42), Variant.fromInteger(42));
	}

	@Test
	public void testSerializeShort() throws Exception {
		testSerialize((short)42, Variant.fromInteger(42));
	}

	@Test
	public void testSerializeByte() throws Exception {

		testSerialize((byte)42, Variant.fromInteger(42));
	}

	@Test
	public void testSerializeFloat() throws Exception {

		testSerialize(42f, Variant.fromDouble(42.0));
	}

	@Test
	public void testSerializeMapWithMaxDepth() throws Exception {

		testSerialize(
			mapWithDepth(PermissiveVariantSerializer.MAX_DEPTH),
			variantMapWithDepth(PermissiveVariantSerializer.MAX_DEPTH)
		);
	}

	@Test
	public void testSerializeMapBeyondMaxDepth() throws Exception {
		testSerialize(
			mapWithDepth(PermissiveVariantSerializer.MAX_DEPTH + 1),
			VariantSerializationFailedException.class
		);
	}

	@Test
	public void testSerializeNull() throws Exception {
		testSerialize(null, Variant.fromNull());
	}

	@Test
	public void testSerializeMapContainingNull() throws Exception {
		testSerialize(
		new HashMap<String, Integer>() {
			{
				put("k1", 1);
				put("k2", null);
				put("k3", 3);
			}
		},
		Variant.fromVariantMap(new HashMap<String, Variant>() {
			{
				put("k1", Variant.fromInteger(1));
				put("k2", Variant.fromNull());
				put("k3", Variant.fromInteger(3));
			}
		})
		);
	}

	@Test
	public void testSerializeMapContainingNullKey() throws Exception {
		testSerialize(
		new HashMap<String, Integer>() {
			{
				put("k1", 1);
				put(null, 2);
				put("k3", 3);
			}
		},
		Variant.fromVariantMap(new HashMap<String, Variant>() {
			{
				put("k1", Variant.fromInteger(1));
				put("k3", Variant.fromInteger(3));
			}
		})
		);
	}

	@Test
	public void testSerializeListContainingNull() throws Exception {
		testSerialize(
			Arrays.asList(1, null, 3),
			Variant.fromVariantList(Arrays.asList(Variant.fromInteger(1), Variant.fromNull(), Variant.fromInteger(3)))
		);
	}

	@Test
	public void testSerializeMapWithObjectKeys() throws Exception {
		testSerialize(
		new HashMap<Object, Object>() {
			{
				put("k1", (Object) 1);
				put(2, "hi");
				put("k3", Variant.fromDouble(42.0));
			}
		},
		Variant.fromVariantMap(new HashMap<String, Variant>() {
			{
				put("k1", Variant.fromInteger(1));
				put("2", Variant.fromString("hi"));
				put("k3", Variant.fromDouble(42.0));
			}
		}
							  )
		);
	}

	private void testDeserialize(final Variant variant, final Object expectedValue) throws Exception {
		testDeserialize(PermissiveVariantSerializer.DEFAULT_INSTANCE, variant, expectedValue);
	}

	private void testDeserialize(final Variant variant,
								 final Class<? extends Throwable> expectedException) throws Exception {
		testDeserialize(PermissiveVariantSerializer.DEFAULT_INSTANCE, variant, expectedException);
	}

	private void testDeserialize(final PermissiveVariantSerializer serializer, final Variant variant,
								 final Object expectedValue) throws Exception {
		final Object value = serializer.deserialize(variant);
		assertEquals(
			expectedValue,
			value
		);
	}

	private void testDeserialize(final PermissiveVariantSerializer serializer, final Variant variant,
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
		testDeserialize(Variant.fromBoolean(true), true);
	}

	@Test
	public void testDeserializeInteger() throws Exception {
		testDeserialize(Variant.fromInteger(42), 42);
	}

	@Test
	public void testDeserializeLong() throws Exception {
		testDeserialize(Variant.fromLong(42L), 42L);
	}

	@Test
	public void testDeserializeDouble() throws Exception {
		testDeserialize(Variant.fromDouble(42.0), 42.0);
	}

	@Test
	public void testDeserializeMap() throws Exception {
		testDeserialize(Variant.fromVariantMap(new HashMap<String, Variant>() {
			{
				put("k1", Variant.fromInteger(1));
				put("k2", Variant.fromInteger(2));
				put("k3", Variant.fromInteger(3));
			}
		}
											  ), new HashMap<String, Integer>() {
			{
				put("k1", 1);
				put("k2", 2);
				put("k3", 3);
			}
		});
	}

	@Test
	public void testDeserializeEmptyMap() throws Exception {
		testDeserialize(Variant.fromVariantMap(new HashMap<String, Variant>()), new HashMap<String, Integer>());
	}

	@Test
	public void testDeserializeMapWithDifferentKindsOfValues() throws Exception {
		testDeserialize(Variant.fromVariantMap(new HashMap<String, Variant>() {
			{
				put("k1", Variant.fromInteger(1));
				put("k2", Variant.fromString("hi"));
				put("k3", Variant.fromDouble(42.0));
			}
		}
											  ), new HashMap<String, Serializable>() {
			{
				put("k1", 1);
				put("k2", "hi");
				put("k3", 42.0);
			}
		});
	}

	@Test
	public void testDeserializeEmptyList() throws Exception {
		testDeserialize(
			Variant.fromVariantList(new ArrayList<Variant>()),
			new ArrayList<Object>()
		);
	}

	@Test
	public void testDeserializeList() throws Exception {
		testDeserialize(Variant.fromVariantList(Arrays.asList(
				Variant.fromInteger(1),
				Variant.fromInteger(2),
				Variant.fromInteger(3)
												)), Arrays.asList(1, 2, 3));
	}

	@Test
	public void testDeserializeListWithDifferentKindsOfValues() throws Exception {
		testDeserialize(Variant.fromVariantList(Arrays.asList(
				Variant.fromInteger(1),
				Variant.fromString("hi"),
				Variant.fromDouble(42.0)
												)), Arrays.asList(1, "hi", 42.0));
	}

	@Test
	public void testDeserializeJavaNull() throws Exception {
		testDeserialize(null, IllegalArgumentException.class);
	}

	@Test
	public void testDeserializeVariantNull() throws Exception {
		testDeserialize(Variant.fromNull(), (Object)null);
	}

	@Test
	public void testDeserializeMapContainingVariantNull() throws Exception {
		testDeserialize(
		Variant.fromVariantMap(new HashMap<String, Variant>() {
			{
				put("k1", Variant.fromInteger(1));
				put("k2", Variant.fromNull());
				put("k3", Variant.fromInteger(3));
			}
		}
								  ),
		new HashMap<String, Integer>() {
			{
				put("k1", 1);
				put("k2", null);
				put("k3", 3);
			}
		}
		);
	}

	@Test
	public void testDeserializeListContainingVariantNull() throws Exception {
		testDeserialize(
			Variant.fromVariantList(Arrays.asList(Variant.fromInteger(1), Variant.fromNull(), Variant.fromInteger(3))),
			Arrays.asList(1, null, 3)
		);
	}
}
