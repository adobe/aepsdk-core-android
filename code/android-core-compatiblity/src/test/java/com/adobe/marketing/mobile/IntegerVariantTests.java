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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public final class IntegerVariantTests extends VariantTests {
	private void testBasics(final int value) {
		final BasicTest testCase = new BasicTest();
		testCase.variant = Variant.fromInteger(value);
		testCase.expectedKind = VariantKind.INTEGER;
		testCase.expectedInteger = value;
		testCase.expectedLong = (long) value;
		testCase.expectedDouble = (double) value;
		testCase.expectedConvertedDouble = (double) value;
		testCase.expectedConvertedString = Integer.toString(value);
		testCase.expectedToString = Integer.toString(value);
		testCase.test();
	}
	@Test
	public void testNormal() {
		testBasics(123);;
	}

	@Test
	public void testZero() {
		testBasics(0);;
	}

	@Test
	public void testIntegerMax() {
		testBasics(2147483647);;
	}

	@Test
	public void testIntegerMin() {
		testBasics(-2147483648);;
	}

	@Test
	public void testEqualsSelf() {
		final Variant selfTest = Variant.fromInteger(1);
		assertEquals(selfTest, selfTest);
	}

	@Test
	public void testEqualsSameInteger() {
		assertEquals(Variant.fromInteger(1), Variant.fromInteger(1));;
	}

	@Test
	public void testNotEqualsDifferentInteger() {
		assertNotEquals(Variant.fromInteger(1), Variant.fromInteger(2));;
	}

	@Test
	public void testNotEqualsOtherVariatns() {
		assertNotEquals(Variant.fromInteger(1), Variant.fromLong(1L));;
	}

	@Test
	public void testNotEqualsNull() {
		assertNotEquals(Variant.fromInteger(1), null);;
	}

	@Test
	public void testNotEqualsOtherClasses() {
		assertNotEquals(Variant.fromInteger(1), new Object());;
	}
}