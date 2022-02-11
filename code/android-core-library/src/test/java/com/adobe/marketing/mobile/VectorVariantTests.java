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
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

public final class VectorVariantTests extends VariantTests {
	private final List<Variant> LIST_1 = Arrays.asList(Variant.fromInteger(1));
	private final List<Variant> LIST_2 = Arrays.asList(Variant.fromInteger(2));
	private final List<Variant> LIST_12 = Arrays.asList(Variant.fromInteger(1), Variant.fromInteger(2));
	private final List<Variant> LIST_123 = Arrays.asList(Variant.fromInteger(1), Variant.fromInteger(2),
										   Variant.fromInteger(3));
	private final List<Variant> LIST_NULL2 = Arrays.asList(null, Variant.fromInteger(2));
	private final List<Variant> LIST_VNULL2 = Arrays.asList(Variant.fromNull(), Variant.fromInteger(2));
	private final ArrayList<Variant> EMPTY_LIST = new ArrayList<Variant>();

	private void testBasics(final List<Variant> value, final String expectedToString) {
		testBasics(value, value, expectedToString);
	}

	private void testBasics(final List<Variant> value, final List<Variant> expectedList, final String expectedToString) {
		final BasicTest testCase = new BasicTest();
		testCase.variant = Variant.fromVariantList(value);
		testCase.expectedKind = VariantKind.VECTOR;
		testCase.expectedList = expectedList;
		testCase.expectedToString = expectedToString;
		testCase.test();
	}

	@Test
	public void testNull() {
		final Variant variant = Variant.fromVariantList(null);
		assertNotNull(variant);
		assertEquals(VariantKind.NULL, variant.getKind());
	}

	@Test
	public void testEmptyList() {
		testBasics(EMPTY_LIST, "[]");
	}

	@Test
	public void testListWithOneElement() {
		testBasics(LIST_1, "[1]");
	}

	@Test
	public void testListWithMultipleElement() {
		testBasics(LIST_123, "[1,2,3]");
	}

	@Test
	public void testListWithNullElement() {
		testBasics(LIST_NULL2, LIST_VNULL2, "[null,2]");
	}

	@Test
	public void testEqualsSelf() {
		final Variant selfTest = Variant.fromVariantList(LIST_12);
		assertEquals(selfTest, selfTest);
	}

	@Test
	public void testEqualsSame() {
		assertEquals(Variant.fromVariantList(LIST_12), Variant.fromVariantList(LIST_12));
	}

	@Test
	public void testNotEqualsMissingElement() {
		assertNotEquals(Variant.fromVariantList(LIST_12), Variant.fromVariantList(Arrays.asList(Variant.fromInteger(1))));
	}

	@Test
	public void testNotEqualsDifferentElement() {
		assertNotEquals(Variant.fromVariantList(LIST_12), Variant.fromVariantList(Arrays.asList(Variant.fromInteger(1),
						Variant.fromInteger(3))));
	}

	@Test
	public void testNotEqualsExtraElemented() {
		assertNotEquals(Variant.fromVariantList(LIST_12), Variant.fromVariantList(LIST_123));
	}

	@Test
	public void testNotEqualsOtherVariants() {
		assertNotEquals(Variant.fromVariantList(LIST_12), Variant.fromInteger(1));
	}

	@Test
	public void testNotEqualsNull() {
		assertNotEquals(Variant.fromVariantList(LIST_12), null);
	}

	@Test
	public void testNotEqualsOtherClasses() {
		assertNotEquals(Variant.fromVariantList(LIST_12), new Object());
	}

	@Test
	public void testFromVariantVectorStoresCopyOfVariantVector() throws Exception {
		final List<Variant> list = new ArrayList<Variant>(LIST_12);

		final Variant variant = Variant.fromVariantList(list);
		assertNotNull(variant);

		list.add(Variant.fromInteger(3));

		final List<Variant> listGottenFromVariant = variant.getVariantList();
		assertNotNull(listGottenFromVariant);
		assertNotSame(list, listGottenFromVariant);

		assertEquals(2, listGottenFromVariant.size());
	}

	@Test
	public void testGetVariantVectorReturnsCopyOfVariantVector() throws Exception  {
		final List<Variant> list = new ArrayList<Variant>(LIST_12);
		final Variant variant = Variant.fromVariantList(list);
		assertNotNull(variant);

		final List<Variant> list1GottenFromVariant = variant.getVariantList();
		assertNotNull(list1GottenFromVariant);

		final List<Variant> list2GottenFromVariant = variant.getVariantList();
		assertNotNull(list2GottenFromVariant);
		assertNotSame(list1GottenFromVariant, list2GottenFromVariant);

		list1GottenFromVariant.add(Variant.fromInteger(3));
		assertEquals(2, list2GottenFromVariant.size());
	}
}
