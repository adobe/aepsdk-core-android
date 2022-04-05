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

import static org.junit.Assert.*;

import com.adobe.marketing.mobile.internal.utility.StringEncoder;

public class StringEncoderTest {

	@Test
	public void testClassIsWellDefined() {
		try {
			TestHelper.assertUtilityClassWellDefined(StringEncoder.class);
		} catch (Exception e) {
			fail("StringEncoder class is not well defined, throwing exception " + e);
		}
	}

	// ==============================================================================================================
	// static String getSha1HashedString(final String inputString)
	// ==============================================================================================================
	@Test
	public void testGetSha1HashedString_when_inputStringIsNull_then_returnNull() throws Exception {
		// test
		final String result = StringEncoder.getSha1HashedString(null);

		// verify
		assertNull("Result should be null when input is null", result);
	}

	@Test
	public void testGetSha1HashedString_when_inputStringIsEmpty_then_returnNull() throws Exception {

		// test
		final String result = StringEncoder.getSha1HashedString("");

		// verify
		assertNull("Result should be null when input is null", result);
	}

	@Test
	public void testGetSha1HashedString_when_inputStringIsValid_then_returnCorrectSha1HashedValue() throws Exception {
		// setup
		final String inputString = "RAINBOWSANDUNICORNS";

		// test
		final String result = StringEncoder.getSha1HashedString(inputString);

		// verify
		// results tested against http://www.sha1hash.com/
		assertEquals("Result should have correct sha1 hash result", "f657da75ffcbbe9f5618c326eddd1b51d9b94439", result);
	}
}
