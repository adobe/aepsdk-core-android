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

public final class DoubleVariantTests extends VariantTests {
	private void testBasics(final double value, final Integer expectedInteger, final Long expectedLong) {
		final BasicTest testCase = new BasicTest();
		testCase.variant = Variant.fromDouble(value);
		testCase.expectedKind = VariantKind.DOUBLE;
		testCase.expectedInteger = expectedInteger;

		if (expectedInteger == null) {
			testCase.expectedIntegerException = VariantRangeException.class;
		}

		testCase.expectedLong = expectedLong;

		if (expectedLong == null) {
			testCase.expectedLongException = VariantRangeException.class;
		}

		testCase.expectedDouble = value;
		testCase.expectedConvertedDouble = value;
		testCase.expectedConvertedString = Double.toString(value);
		testCase.expectedToString = Double.toString(value);
		testCase.test();
	}

	@Test
	public void testNormal() {
		testBasics(123.0, 123, 123L);
	}

	@Test
	public void testRoundingUp() {
		testBasics(1.9999, 2, 2L);
	}

	@Test
	public void testRoundingDown() {
		testBasics(2.0001, 2, 2L);
	}

	@Test
	public void testIntegerMax() {
		testBasics(2147483647.0, 2147483647, 2147483647L);
	}

	@Test
	public void testIntegerMin() {
		testBasics(-2147483648.0, -2147483648, -2147483648L);
	}

	@Test
	public void testSlightlyLessThanIntegerMax() {
		testBasics(2147483646.9, 2147483647, 2147483647L);
	}

	@Test
	public void testSlightlyMoreThanIntegerMax() {
		testBasics(2147483647.1, 2147483647, 2147483647L);
	}

	@Test
	public void testMoreThanIntegerMax() {
		testBasics(2147483647.9, null, 2147483648L);
	}

	@Test
	public void testSlightlyMoreThanIntegerMin() {
		testBasics(-2147483647.9, -2147483648, -2147483648L);
	}

	@Test
	public void testSlightlyLessThanIntegerMin() {
		testBasics(-2147483648.1, -2147483648, -2147483648L);
	}

	@Test
	public void testLessThanIntegerMin() {
		testBasics(-2147483648.9, null, -2147483649L);
	}

	@Test
	public void testLongMax() {
		testBasics(9223372036854775807.0, null, 9223372036854775807L);
	}

	@Test
	public void testLongMin() {
		testBasics(-9223372036854775808.0, null, -9223372036854775808L);
	}

	@Test
	public void testSlightlyLessThanLongMax() {
		testBasics(9223372036854775806.9, null, 9223372036854775807L);
	}

	@Test
	public void testSlightlyMoreThanLongMax() {
		testBasics(9223372036854775807.1, null, 9223372036854775807L);
	}

	@Test
	public void testMoreThanLongMax() {
		testBasics(9223372036854780000.0, null, null);
	}

	@Test
	public void testSlightlyMoreThanLongMin() {
		testBasics(-9223372036854775807.9, null, -9223372036854775808L);
	}

	@Test
	public void testSlightlyLessThanLongMin() {
		testBasics(-9223372036854775808.1, null, -9223372036854775808L);
	}

	@Test
	public void testLessThanLongMin() {
		testBasics(-9223372036854780000.0, null, null);
	}

	@Test
	public void testDoubleMax() {
		testBasics(Double.MAX_VALUE, null, null);
	}

	@Test
	public void testDoubleMin() {
		testBasics(-Double.MAX_VALUE, null, null);
	}

	@Test
	public void testNegativeInfinity() {
		testBasics(Double.NEGATIVE_INFINITY, null, null);
	}

	@Test
	public void testPositiveInfinity() {
		testBasics(Double.POSITIVE_INFINITY, null, null);
	}

	@Test
	public void testNaN() {
		testBasics(Double.NaN, null, null);
	}

	@Test
	public void testEqualsSelf() {
		final Variant selfTest = Variant.fromDouble(1.0);
		assertEquals(selfTest, selfTest);;
	}

	@Test
	public void testEqualsSameDouble() {
		assertEquals(Variant.fromDouble(1.0), Variant.fromDouble(1.0));;
	}

	@Test
	public void testNotEqualsDifferentDouble() {
		assertNotEquals(Variant.fromDouble(1.0), Variant.fromDouble(2.0));;
	}

	@Test
	public void testNotEqualsOtherVariants() {
		assertNotEquals(Variant.fromDouble(1.0), Variant.fromInteger(1));;
	}

	@Test
	public void testNotEqualsNull() {
		assertNotEquals(Variant.fromDouble(1.0), null);;
	}

	@Test
	public void testNotEqualsOtherClasses() {
		assertNotEquals(Variant.fromDouble(1.0), new Object());;
	}
}
