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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class StringListVariantTests extends VariantTests {
	private final Map<String, Variant> EMPTY_VARIANT_MAP = Collections.<String, Variant>emptyMap();
	private final List<Variant> EMPTY_VARIANT_LIST = Collections.<Variant>emptyList();

	private final List<String> stringList = new ArrayList<String>();
	private final List<Variant> variantList = new ArrayList<Variant>();

	@Test
	public void testNull() {
		final Variant variant = Variant.fromStringList(null);
		assertNotNull(variant);
		assertEquals(VariantKind.NULL, variant.getKind());
	}

	@Test
	public void testFromWithAnEmptyList() throws Exception {
		assertEquals(Variant.fromVariantList(variantList), Variant.fromStringList(stringList));
	}

	@Test
	public void testFromWithAString() throws Exception {
		variantList.add(Variant.fromString("1"));
		stringList.add("1");
		assertEquals(Variant.fromVariantList(variantList), Variant.fromStringList(stringList));
	}

	@Test
	public void testFromWithMultipleStrings() throws Exception {
		variantList.add(Variant.fromString("1"));
		variantList.add(Variant.fromString("2"));
		variantList.add(Variant.fromString("3"));
		stringList.add("1");
		stringList.add("2");
		stringList.add("3");
		assertEquals(Variant.fromVariantList(variantList), Variant.fromStringList(stringList));
	}

	@Test
	public void testFromWithNull() throws Exception {
		variantList.add(Variant.fromString("1"));
		variantList.add(Variant.fromNull());
		variantList.add(Variant.fromString("3"));
		stringList.add("1");
		stringList.add(null);
		stringList.add("3");
		assertEquals(Variant.fromVariantList(variantList), Variant.fromStringList(stringList));
	}

	@Test
	public void testGetWithAnEmptyList() throws Exception {
		assertEquals(stringList, Variant.fromVariantList(variantList).getStringList());
	}

	@Test
	public void testGetWithAString() throws Exception {
		stringList.add("1");
		variantList.add(Variant.fromString("1"));
		assertEquals(stringList, Variant.fromVariantList(variantList).getStringList());
	}

	@Test
	public void testGetWithMultipleStrings() throws Exception {
		stringList.add("1");
		stringList.add("2");
		stringList.add("3");
		variantList.add(Variant.fromString("1"));
		variantList.add(Variant.fromString("2"));
		variantList.add(Variant.fromString("3"));
		assertEquals(stringList, Variant.fromVariantList(variantList).getStringList());
	}

	@Test
	public void testGetWithAnInteger() throws Exception {
		stringList.add("42");
		variantList.add(Variant.fromInteger(42));
		assertEquals(stringList, Variant.fromVariantList(variantList).getStringList());
	}

	@Test
	public void testGetWithALong() throws Exception {
		stringList.add("42");
		variantList.add(Variant.fromLong(42L));
		assertEquals(stringList, Variant.fromVariantList(variantList).getStringList());
	}

	@Test
	public void testGetWithADouble() throws Exception {
		stringList.add("42.5");
		variantList.add(Variant.fromDouble(42.5));
		assertEquals(stringList, Variant.fromVariantList(variantList).getStringList());
	}

	@Test
	public void testGetWithABoolean() throws Exception {
		stringList.add("true");
		variantList.add(Variant.fromBoolean(true));
		assertEquals(stringList, Variant.fromVariantList(variantList).getStringList());
	}

	@Test
	public void testGetWithNull() throws Exception {
		stringList.add(null);
		variantList.add(Variant.fromNull());
		assertEquals(stringList, Variant.fromVariantList(variantList).getStringList());
	}

	@Test
	public void testGetWithAVariantList() throws Exception {
		variantList.add(Variant.fromVariantList(EMPTY_VARIANT_LIST));
		assertEquals(stringList, Variant.fromVariantList(variantList).getStringList());
	}

	@Test
	public void testGetWithAVariantMap() throws Exception {
		variantList.add(Variant.fromVariantMap(EMPTY_VARIANT_MAP));
		assertEquals(stringList, Variant.fromVariantList(variantList).getStringList());
	}

	@Test(expected = VariantKindException.class)
	public void testGetThrowsWhenVariantIsNotAMap() throws Exception {
		final Variant variant = Variant.fromInteger(1);
		variant.getStringList();
	}
}
