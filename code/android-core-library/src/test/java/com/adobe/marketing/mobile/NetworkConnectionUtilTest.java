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
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.*;

public class NetworkConnectionUtilTest {

	@Test
	public void testClassIsWellDefined() {
		try {
			TestHelper.assertUtilityClassWellDefined(NetworkConnectionUtil.class);
		} catch (Exception e) {
			fail("NetworkConnectionUtil class is not well defined, throwing exception " + e);
		}
	}

	@Test
	public void testGetHeaders_When_SslEnabled() throws Exception {
		Map<String, String> result = NetworkConnectionUtil.getHeaders(true);
		assertEquals(1, result.size());
		assertEquals("application/x-www-form-urlencoded", result.get("Content-Type"));
	}

	@Test
	public void testGetHeaders_When_SslDisabled() throws Exception {
		Map<String, String> result = NetworkConnectionUtil.getHeaders(false);
		assertEquals(2, result.size());
		assertEquals("close", result.get("connection"));
		assertEquals("application/x-www-form-urlencoded", result.get("Content-Type"));
	}

	@Test
	public void testReadFromInputStreamReturnsNull_When_NullInputStream() throws Exception {
		String result = NetworkConnectionUtil.readFromInputStream(null);
		assertNull(result);
	}

	@Test
	public void testReadFromInputStreamReturnsEmptyString_When_EmptyInputStream() throws Exception {
		String result = NetworkConnectionUtil.readFromInputStream(new ByteArrayInputStream("".getBytes("UTF-8")));
		assertEquals("", result);
	}

	@Test
	public void testReadFromInputStreamReturnsString_When_ValidInputStream() throws Exception {
		String result = NetworkConnectionUtil.readFromInputStream(
							new ByteArrayInputStream("testing this method".getBytes("UTF-8")));
		assertEquals("testing this method", result);
	}

	@Test(expected = IOException.class)
	public void testReadFromInputStreamThrowsIOException_When_InvalidCharset() throws Exception {
		NetworkConnectionUtil.readFromInputStream(new ByteArrayInputStream("".getBytes("UTF-8")),
				"testCharset");
	}
}
