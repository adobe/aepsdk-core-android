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

public final class LongVariantTests extends VariantTests {
	private void testBasics(final long value,
							final Integer expectedInteger,
							final Double expectedDouble) {
		final BasicTest testCase = new BasicTest();
		testCase.variant = Variant.fromLong(value);
		testCase.expectedKind = VariantKind.LONG;
		testCase.expectedLong = value;
		testCase.expectedInteger = expectedInteger;

		if (expectedInteger == null) {
			testCase.expectedIntegerException = VariantRangeException.class;
		}

		testCase.expectedDouble = expectedDouble;

		if (expectedDouble == null) {
			testCase.expectedDoubleException = VariantRangeException.class;
		}

		testCase.expectedToString = Long.toString(value);
		testCase.test();
	}

	@Test
	public void testNormal() {
		testBasics(123L, 123, 123.0);
	}

	@Test
	public void testIntegerMax() {
		testBasics(2147483647L, 2147483647, 2147483647.0);
	}

	@Test
	public void testIntegerMin() {
		testBasics(-2147483648L, -2147483648, -2147483648.0);
	}

	@Test
	public void testMoreThanIntegerMax() {
		testBasics(2147483648L, null, 2147483648.0);
	}

	@Test
	public void testLessThanIntegerMin() {
		testBasics(-2147483649L, null, -2147483649.0);
	}

	@Test
	public void testLongMaxSafeInteger() {
		assertEquals(Variant.MAX_SAFE_INTEGER, 9007199254740991L);
		testBasics(9007199254740991L, null, 9007199254740991.0);
	}

	@Test
	public void testLongMinSafeInteger() {
		assertEquals(Variant.MIN_SAFE_INTEGER, -9007199254740991L);
		testBasics(-9007199254740991L, null, -9007199254740991.0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGreaterThanLongMaxSafeInteger() {
		Variant.fromLong(9007199254740992L);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testLessThanLongMinSafeInteger() {
		Variant.fromLong(-9007199254740992L);
	}

	@Test
	public void testEqualsSelf() {
		final Variant selfTest = Variant.fromLong(1L);
		assertEquals(selfTest, selfTest);
	}

	@Test
	public void testEqualsSameLong() {
		assertEquals(Variant.fromLong(1L), Variant.fromLong(1L));
	}

	@Test
	public void testNotEqualsDifferentLong() {
		assertNotEquals(Variant.fromLong(1L), Variant.fromLong(2L));
	}

	@Test
	public void testNotEqualsOtherVariants() {
		assertNotEquals(Variant.fromLong(1L), Variant.fromInteger(1));
	}

	@Test
	public void testNotEqualsNull() {
		assertNotEquals(Variant.fromLong(1L), null);
	}

	@Test
	public void testNotEqualsOtherClasses() {
		assertNotEquals(Variant.fromLong(1L), new Object());
	}
}
