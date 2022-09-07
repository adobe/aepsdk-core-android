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

public final class NullVariantTests extends VariantTests {
	@Test
	public void testBasics() {
		final BasicTest testCase = new BasicTest();
		testCase.variant = Variant.fromNull();
		testCase.expectedKind = VariantKind.NULL;
		testCase.expectedToString = "null";
		testCase.test();
	}

	@Test
	public void testEqualsSelf() {
		final Variant selfTest = Variant.fromNull();
		assertEquals(selfTest, selfTest);
	}

	@Test
	public void testEqualsVariantNull() {
		assertEquals(Variant.fromNull(), Variant.fromNull());
	}

	@Test
	public void testNotEqualsOtherVariants() {
		assertNotEquals(Variant.fromNull(), Variant.fromInteger(1));
	}

	@Test
	public void testNotEqualsNull() {
		assertNotEquals(Variant.fromNull(), null);
	}

	@Test
	public void testNotEqualsOtherClasses() {
		assertNotEquals(Variant.fromNull(), new Object());
	}
}
