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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;


public class StringUtilsTest {

	@Test
	public void testClassIsWellDefined() {
		try {
			TestHelper.assertUtilityClassWellDefined(StringUtils.class);
		} catch (Exception e) {
			fail("StringUtils class is not well defined, throwing exception " + e);
		}
	}

	// ==============================================================================================================
	// static String getSha1HashedString(final String inputString)
	// ==============================================================================================================
	@Test
	public void testGetSha1HashedString_when_inputStringIsNull_then_returnNull() throws Exception {
		// setup
		final String inputString = null;

		// test
		final String result = StringEncoder.getSha1HashedString(inputString);

		// verify
		assertNull("result should be null when input is null", result);
	}

	@Test
	public void testGetSha1HashedString_when_inputStringIsEmpty_then_returnNull() throws Exception {
		// setup
		final String inputString = "";

		// test
		final String result = StringEncoder.getSha1HashedString(inputString);

		// verify
		assertNull("result should be null when input is null", result);
	}

	@Test
	public void testGetSha1HashedString_when_inputStringIsValid_then_returnCorrectSha1HashedValue() throws Exception {
		// setup
		final String inputString = "BIGSOFTHANDS";

		// test
		final String result = StringEncoder.getSha1HashedString(inputString);

		// verify
		// results tested against http://www.sha1hash.com/
		assertEquals("result should have correct sha1 hash result", "185fdf9fa24b073d1b3bfc724c6dd77825130607", result);
	}

	@Test
	public void testStringIsUrl_when_validUrl() {
		assertTrue(StringUtils.stringIsUrl("http://is.valid.url/path?key=value&abc=def"));
	}

	@Test
	public void testStringIsUrl_when_invalidUrl() {
		assertFalse(StringUtils.stringIsUrl("wrong.url"));
	}

	@Test
	public void testStringIsUrl_when_emptyUrl() {
		assertFalse(StringUtils.stringIsUrl(""));
	}

	@Test
	public void testStringIsUrl_when_nullUrl() {
		assertFalse(StringUtils.stringIsUrl(null));
	}

	@Test
	public void testIsNullOrEmpty_when_nullInputString() {
		assertTrue(StringUtils.isNullOrEmpty(null));
	}

	@Test
	public void testIsNullOrEmpty_when_emptyInputString() {
		assertTrue(StringUtils.isNullOrEmpty(""));
	}

	@Test
	public void testIsNullOrEmpty_when_validInputString() {
		assertFalse(StringUtils.isNullOrEmpty("non empty string"));
	}

	@Test
	public void testIsNullOrEmpty_when_whitespacesInputString() {
		assertTrue(StringUtils.isNullOrEmpty("        "));
	}

	@Test
	public void testStreamToString_when_nullInputStream() {
		assertNull(StringUtils.streamToString(null));
	}

	@Test
	public void testStreamToString_when_validInputStream() throws Exception {
		InputStream stream = new ByteArrayInputStream("myTestExample".getBytes("UTF-8"));
		assertEquals("myTestExample", StringUtils.streamToString(stream));
	}
}

