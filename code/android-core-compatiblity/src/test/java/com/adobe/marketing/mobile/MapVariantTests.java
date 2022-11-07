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
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

public final class MapVariantTests extends VariantTests {
	private final Map<String, Variant> EMPTY_MAP = Collections.<String, Variant>emptyMap();

	private final String EMPTY_MAP_STRING = "{}";

	private final Map<String, Variant> MAP1 = new HashMap<String, Variant>() {
		{
			put("k1", Variant.fromString("v1"));
		}
	};

	private final String MAP1_STRING = "{\"k1\":\"v1\"}";

	private final Map<String, Variant> MAP2 = new HashMap<String, Variant>() {
		{
			put("k2", Variant.fromString("v2"));
		}
	};

	private final String MAP2_STRING = "{\"k2\":\"v2\"}";

	private final Map<String, Variant> MAP12 = new HashMap<String, Variant>() {
		{
			put("k1", Variant.fromString("v1"));
			put("k2", Variant.fromString("v2"));
		}
	};

	private final String MAP12_STRING = "{\"k1\":\"v1\",\"k2\":\"v2\"}";

	private final Map<String, Variant> MAP12_DUPLICATE = new HashMap<String, Variant>() {
		{
			put("k1", Variant.fromString("v1"));
			put("k2", Variant.fromString("v2"));
		}
	};

	private final Map<String, Variant> MAP123 = new HashMap<String, Variant>() {
		{
			put("k1", Variant.fromString("v1"));
			put("k2", Variant.fromString("v2"));
			put("k3", Variant.fromString("v3"));
		}
	};

	private final String MAP123_STRING = "{\"k1\":\"v1\",\"k2\":\"v2\",\"k3\":\"v3\"}";

	private final Map<String, Variant> MAP12_WITH_NULL_VALUE1 = new HashMap<String, Variant>() {
		{
			put("k1", null);
			put("k2", Variant.fromString("v2"));
		}
	};

	private final Map<String, Variant> MAP12_WITH_NULL_VARIANT_VALUE1 = new HashMap<String, Variant>() {
		{
			put("k1", Variant.fromNull());
			put("k2", Variant.fromString("v2"));
		}
	};

	private final String MAP12_WITH_NULL_VALUE1_STRING = "{\"k1\":null,\"k2\":\"v2\"}";

	private final Map<String, Variant> MAP12_WITH_NULL_KEY1 = new HashMap<String, Variant>() {
		{
			put(null, Variant.fromString("v1"));
			put("k2", Variant.fromString("v2"));
		}
	};

	private final Map<String, Variant> MAP12_WITH_DIFFERENT_VALUE1 = new HashMap<String, Variant>() {
		{
			put("k1", Variant.fromString("different"));
			put("k2", Variant.fromString("v2"));
		}
	};

	private void testBasics(final Map<String, Variant> value, final String expectedToString) {
		testBasics(value, value, expectedToString);
	}

	private void testBasics(final Map<String, Variant> value,
							final Map<String, Variant> expectedMap,
							final String expectedToString) {
		final BasicTest testCase = new BasicTest();
		testCase.variant = Variant.fromVariantMap(value);
		testCase.expectedKind = VariantKind.MAP;
		testCase.expectedMap = expectedMap;
		testCase.expectedToString = expectedToString;
		testCase.test();
	}

	@Test
	public void testNull() {
		final Variant variant = Variant.fromVariantMap(null);
		assertNotNull(variant);
		assertEquals(VariantKind.NULL, variant.getKind());
	}

	@Test
	public void testEmptyMap() {
		testBasics(EMPTY_MAP, EMPTY_MAP_STRING);
	}

	@Test
	public void testMapWithOneKey() {
		testBasics(MAP1, MAP1_STRING);
	}

	@Test
	public void testMapWithMultipleKeys() {
		testBasics(MAP123, MAP123_STRING);
	}

	@Test
	public void testMapContainingNullValue() {
		testBasics(MAP12_WITH_NULL_VALUE1, MAP12_WITH_NULL_VARIANT_VALUE1, MAP12_WITH_NULL_VALUE1_STRING);
	}

	@Test
	public void testMapContainignNullKey() {
		testBasics(MAP12_WITH_NULL_KEY1, MAP2, MAP2_STRING);
	}

	@Test
	public void testEqualsSelf() {
		final Variant selfTest = Variant.fromVariantMap(EMPTY_MAP);
		assertEquals(selfTest, selfTest);
	}

	@Test
	public void testEmptyMapEqualsEmptyMap() {
		assertEquals(Variant.fromVariantMap(EMPTY_MAP), Variant.fromVariantMap(EMPTY_MAP));
	}

	@Test
	public void testEqualsMapWithMultipleKeys() {
		assertEquals(Variant.fromVariantMap(MAP12), Variant.fromVariantMap(MAP12));
	}

	@Test
	public void testEqualsIdenticalVariantFromDifferentMap() {
		assertEquals(Variant.fromVariantMap(MAP12), Variant.fromVariantMap(MAP12_DUPLICATE));
	}

	@Test
	public void testNotEqualsMapWithDifferentValue() {
		assertNotEquals(Variant.fromVariantMap(MAP12), Variant.fromVariantMap(MAP12_WITH_DIFFERENT_VALUE1));
	}

	@Test
	public void testNotEqualsMapWithFewerKeys() {
		assertNotEquals(Variant.fromVariantMap(MAP12), Variant.fromVariantMap(MAP1));
	}

	@Test
	public void testNotEqualsMapWithMoreKeys() {
		assertNotEquals(Variant.fromVariantMap(MAP12), Variant.fromVariantMap(MAP123));
	}

	@Test
	public void testNonEmptyMapNotEqualsEmptyMap() {
		assertNotEquals(Variant.fromVariantMap(MAP12), Variant.fromVariantMap(EMPTY_MAP));
	}

	@Test
	public void testEmptyMapNotEqualsNonEmptyMap() {
		assertNotEquals(Variant.fromVariantMap(EMPTY_MAP), Variant.fromVariantMap(MAP12));
	}

	@Test
	public void testNotEqualsOtherVariants() {
		assertNotEquals(Variant.fromVariantMap(MAP123), Variant.fromInteger(1));
	}

	@Test
	public void testNotEqualsNull() {
		assertNotEquals(Variant.fromVariantMap(MAP123), null);
	}

	@Test
	public void testNotEqualsOtherClasses() {
		assertNotEquals(Variant.fromVariantMap(MAP123), new Object());
	}

	@Test
	public void testFromVariantMapStoresCopyOfVariantMap() throws Exception {
		final Map<String, Variant> map = new HashMap<String, Variant>() {
			{
				put("key1", Variant.fromInteger(1));
				put("key2", Variant.fromInteger(2));
			}
		};

		final Variant variant = Variant.fromVariantMap(map);
		assertNotNull(variant);

		map.put("key3", Variant.fromInteger(3));

		final Map<String, Variant> mapGottenFromVariant = variant.getVariantMap();
		assertNotNull(mapGottenFromVariant);
		assertNotSame(map, mapGottenFromVariant);

		assertNull(mapGottenFromVariant.get("key3"));
	}

	@Test
	public void testGetVariantMapReturnsCopyOfVariantMap() throws Exception {
		final Map<String, Variant> map = new HashMap<String, Variant>() {
			{
				put("key1", Variant.fromInteger(1));
				put("key2", Variant.fromInteger(2));
			}
		};
		final Variant variant = Variant.fromVariantMap(map);
		assertNotNull(variant);

		final Map<String, Variant> map1GottenFromVariant = variant.getVariantMap();
		assertNotNull(map1GottenFromVariant);

		final Map<String, Variant> map2GottenFromVariant = variant.getVariantMap();
		assertNotNull(map2GottenFromVariant);
		assertNotSame(map1GottenFromVariant, map2GottenFromVariant);

		map1GottenFromVariant.put("key3", Variant.fromInteger(3));
		assertNull(map2GottenFromVariant.get("key3"));
	}
}
