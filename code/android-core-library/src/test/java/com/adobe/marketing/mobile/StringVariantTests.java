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

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public final class StringVariantTests extends VariantTests {
	private void testBasics(final String value, final Double expectedConvertedDouble) {
		final BasicTest testCase = new BasicTest();
		testCase.variant = Variant.fromString(value);
		testCase.expectedKind = VariantKind.STRING;
		testCase.expectedString = value;
		testCase.expectedConvertedDouble = expectedConvertedDouble;
		testCase.expectedConvertedString = value;
		testCase.expectedToString = "\"" + value + "\"";
		testCase.test();
	}

	@Test
	public void testNull() {
		final Variant variant = Variant.fromString(null);
		assertNotNull(variant);
		assertEquals(VariantKind.NULL, variant.getKind());
	}

	@Test
	public void testNormal() {
		testBasics("hello");
	}

	@Test
	public void testEmptyString() {
		testBasics("");
	}

	@Test
	public void testIntegerString() {
		testBasics("5", 5.0);
	}

	@Test
	public void testDoubleString() {
		testBasics("5.0", 5.0);
	}

	@Test
	public void testNonNumericStringThatBeginsWithANumber() {
		testBasics("5hello");
	}

	private void testBasics(final String value) {
		testBasics(value, null);
	}

	@Test
	public void testEqualsSelf() {
		final Variant selfTest = Variant.fromString("hello");
		assertEquals(selfTest, selfTest);
	}

	@Test
	public void testEqualsSame() {
		assertEquals(Variant.fromString("hello"), Variant.fromString("hello"));
	}

	@Test
	public void testNotEqualsDifferentString() {
		assertNotEquals(Variant.fromString("hello"), Variant.fromString("world"));
	}

	@Test
	public void testNotEqualsOtherVariants() {
		assertNotEquals(Variant.fromString("hello"), Variant.fromInteger(1));
	}

	@Test
	public void testNotEqualsNull() {
		// null tests
		assertNotEquals(Variant.fromString("hello"), null);
	}

	@Test
	public void testNotEqualsOtherClasses() {
		assertNotEquals(Variant.fromString("hello"), new Object());
	}
}
