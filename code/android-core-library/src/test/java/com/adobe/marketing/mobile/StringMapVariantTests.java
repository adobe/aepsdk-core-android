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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

public final class StringMapVariantTests extends VariantTests {
	private final Map<String, Variant> EMPTY_VARIANT_MAP = Collections.<String, Variant>emptyMap();
	private final Map<String, String> stringMap = new HashMap<String, String>();
	private final Map<String, Variant> variantMap = new HashMap<String, Variant>();

	@Test
	public void testNull() {
		final Variant variant = Variant.fromStringMap(null);
		assertNotNull(variant);
		assertEquals(VariantKind.NULL, variant.getKind());
	}

	@Test
	public void testFromEmptyMap() throws Exception {
		assertEquals(Variant.fromVariantMap(variantMap), Variant.fromStringMap(stringMap));
	}

	@Test
	public void testFromMapWithOneEntry() throws Exception {
		stringMap.put("key1", "1");
		variantMap.put("key1", Variant.fromString("1"));
		assertEquals(Variant.fromVariantMap(variantMap), Variant.fromStringMap(stringMap));
	}

	@Test
	public void testFromMapWithMultipleEntries() throws Exception {
		stringMap.put("key1", "1");
		stringMap.put("key2", "2");
		stringMap.put("key3", "3");
		variantMap.put("key1", Variant.fromString("1"));
		variantMap.put("key2", Variant.fromString("2"));
		variantMap.put("key3", Variant.fromString("3"));
		assertEquals(Variant.fromVariantMap(variantMap), Variant.fromStringMap(stringMap));
	}

	@Test
	public void testFromMapContainingNullValue() throws Exception {
		stringMap.put("key1", "1");
		stringMap.put("key2", null);
		stringMap.put("key3", "3");
		variantMap.put("key1", Variant.fromString("1"));
		variantMap.put("key2", Variant.fromNull());
		variantMap.put("key3", Variant.fromString("3"));
		assertEquals(Variant.fromVariantMap(variantMap), Variant.fromStringMap(stringMap));
	}

	@Test
	public void testFromMapContainingNullKey() throws Exception {
		stringMap.put("key1", "1");
		stringMap.put(null, "2");
		stringMap.put("key3", "3");
		variantMap.put("key1", Variant.fromString("1"));
		variantMap.put("key3", Variant.fromString("3"));
		assertEquals(Variant.fromVariantMap(variantMap), Variant.fromStringMap(stringMap));
	}

	@Test
	public void testGetEmptyMap() throws Exception {
		assertEquals(stringMap, Variant.fromVariantMap(variantMap).getStringMap());
	}

	@Test
	public void testGetMapWithOneEntry() throws Exception {
		variantMap.put("key1", Variant.fromString("1"));
		stringMap.put("key1", "1");
		assertEquals(stringMap, Variant.fromVariantMap(variantMap).getStringMap());
	}

	@Test
	public void testGetMapWithMultipleEntries() throws Exception {
		variantMap.put("key1", Variant.fromString("1"));
		variantMap.put("key2", Variant.fromString("2"));
		variantMap.put("key3", Variant.fromString("3"));
		stringMap.put("key1", "1");
		stringMap.put("key2", "2");
		stringMap.put("key3", "3");
		assertEquals(stringMap, Variant.fromVariantMap(variantMap).getStringMap());
	}

	@Test
	public void testGetMapWithInteger() throws Exception {
		variantMap.put("key1", Variant.fromInteger(42));
		stringMap.put("key1", "42");
		assertEquals(stringMap, Variant.fromVariantMap(variantMap).getStringMap());
	}

	@Test
	public void testGetMapWithLong() throws Exception {
		variantMap.put("key1", Variant.fromLong(42L));
		stringMap.put("key1", "42");
		assertEquals(stringMap, Variant.fromVariantMap(variantMap).getStringMap());
	}

	@Test
	public void testGetMapWithDouble() throws Exception {
		variantMap.put("key1", Variant.fromDouble(42.5));
		stringMap.put("key1", "42.5");
		assertEquals(stringMap, Variant.fromVariantMap(variantMap).getStringMap());
	}

	@Test
	public void testGetMapWithBoolean() throws Exception {
		variantMap.put("key1", Variant.fromBoolean(true));
		stringMap.put("key1", "true");
		assertEquals(stringMap, Variant.fromVariantMap(variantMap).getStringMap());
	}


	@Test
	public void testGetMapWithVariantNull() throws Exception {
		stringMap.put("key1", null);
		variantMap.put("key1", Variant.fromNull());
		assertEquals(stringMap, Variant.fromVariantMap(variantMap).getStringMap());
	}

	@Test
	public void testGetMapWithList() throws Exception {
		variantMap.put("key1", Variant.fromVariantList(Collections.<Variant>emptyList()));
		assertEquals(stringMap, Variant.fromVariantMap(variantMap).getStringMap());
	}

	@Test
	public void testGetMapWithMap() throws Exception {
		variantMap.put("key1", Variant.fromVariantMap(EMPTY_VARIANT_MAP));
		assertEquals(stringMap, Variant.fromVariantMap(variantMap).getStringMap());
	}

	@Test
	public void testGetMapContainingSomeNonConvertibleVariants() throws Exception {
		variantMap.put("key1", Variant.fromString("1"));
		variantMap.put("key2", Variant.fromVariantMap(EMPTY_VARIANT_MAP));
		variantMap.put("key3", Variant.fromString("3"));
		stringMap.put("key1", "1");
		stringMap.put("key3", "3");
		assertEquals(stringMap, Variant.fromVariantMap(variantMap).getStringMap());
	}

	@Test(expected = VariantKindException.class)
	public void testGetStringMapThrowsWhenVariantIsNotAMap() throws Exception {
		final Variant variant = Variant.fromInteger(1);
		variant.getStringMap();
	}
}
