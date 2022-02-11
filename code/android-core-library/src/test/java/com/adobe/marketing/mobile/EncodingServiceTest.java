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

public class EncodingServiceTest {
	private FakeEncodingService fakeEncodingService = new FakeEncodingService();

	@Test
	public void decode_returnNull_When_InputIsNull() throws Exception {
		assertNull(fakeEncodingService.base64Decode(null));
	}

	@Test
	public void decode_returnEmptyByteArray_When_InputIsEmptyString() throws Exception {
		assertEquals(0, fakeEncodingService.base64Decode("").length);
		assertEquals("", new String(fakeEncodingService.base64Decode("")));
	}

	@Test
	public void decode_returnValidData_When_InputIsValidString() throws Exception {
		assertEquals("test!@#", new String(fakeEncodingService.base64Decode("dGVzdCFAIw==")));
	}

	@Test
	public void encode_returnNull_When_InputIsNull() throws Exception {
		assertNull(fakeEncodingService.base64Encode(null));
	}

	@Test
	public void encode_returnEmptyByteArray_When_InputIsEmptyString() throws Exception {
		assertEquals(0, fakeEncodingService.base64Encode("".getBytes()).length);
		assertEquals("", new String(fakeEncodingService.base64Encode("".getBytes())));
	}

	@Test
	public void encode_returnValidData_When_InputIsValidString() throws Exception {
		assertEquals("dGVzdCFAIw==", new String(fakeEncodingService.base64Encode("test!@#".getBytes())));
	}

	@Test
	public void encode_and_decode() throws Exception {
		String result = new String(fakeEncodingService.base64Encode("test!@#".getBytes()));
		assertEquals("test!@#", new String(fakeEncodingService.base64Decode(result)));
	}

}
