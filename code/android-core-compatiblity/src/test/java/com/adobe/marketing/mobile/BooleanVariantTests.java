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

public final class BooleanVariantTests extends VariantTests {
	private void testBasics(final boolean value,
							final Double expectedConvertedDouble,
							final String expectedConvertedString) {
		final BasicTest testCase = new BasicTest();
		testCase.variant = Variant.fromBoolean(value);
		testCase.expectedKind = VariantKind.BOOLEAN;
		testCase.expectedBoolean = value;
		testCase.expectedConvertedDouble = expectedConvertedDouble;
		testCase.expectedConvertedString = expectedConvertedString;
		testCase.expectedToString = expectedConvertedString;
		testCase.test();
	}

	@Test
	public void testTrue() {
		testBasics(true, 1.0, "true");
	}

	@Test
	public void testFalse() {
		testBasics(false, 0.0, "false");
	}

	@Test
	public void testSelfEqualsSelf() {
		final Variant selfTest = Variant.fromBoolean(true);
		assertEquals(selfTest, selfTest);
	}

	@Test
	public void testTrueEqualsTrue() {
		assertEquals(Variant.fromBoolean(true), Variant.fromBoolean(true));
	}

	@Test
	public void testFalseEqualsFalse() {
		assertEquals(Variant.fromBoolean(false), Variant.fromBoolean(false));
	}

	@Test
	public void testFalseNotEqualsTrue() {
		assertNotEquals(Variant.fromBoolean(false), Variant.fromBoolean(true));
	}

	@Test
	public void testTrueNotEqualsFalse() {
		assertNotEquals(Variant.fromBoolean(true), Variant.fromBoolean(false));
	}

	@Test
	public void testNotEqualsOtherVariants() {
		assertNotEquals(Variant.fromBoolean(true), Variant.fromInteger(1));
	}

	@Test
	public void testNotEqualNull() {
		assertNotEquals(Variant.fromBoolean(true), null);
	}

	@Test
	public void testNotEqualsOtherClasses() {
		assertNotEquals(Variant.fromBoolean(true), new Object());
	}
}
