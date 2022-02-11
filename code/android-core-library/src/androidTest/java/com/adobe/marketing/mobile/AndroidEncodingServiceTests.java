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
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class AndroidEncodingServiceTests {

	@Test
	public void decode_returnNull_When_InputIsNull() throws Exception {
		AndroidEncodingService encodingService = new AndroidEncodingService();
		assertNull(encodingService.base64Decode(null));
	}

	@Test
	public void decode_returnEmptyByteArray_When_InputIsEmptyString() throws Exception {
		AndroidEncodingService encodingService = new AndroidEncodingService();
		assertEquals(0, encodingService.base64Decode("").length);
		assertEquals("", new String(encodingService.base64Decode("")));
	}

	@Test
	public void decode_returnValidData_When_InputIsValidString() throws Exception {
		AndroidEncodingService encodingService = new AndroidEncodingService();
		assertEquals("test!@#", new String(encodingService.base64Decode("dGVzdCFAIw==")));
	}

	@Test
	public void encode_returnNull_When_InputIsNull() throws Exception {
		AndroidEncodingService encodingService = new AndroidEncodingService();
		assertNull(encodingService.base64Encode(null));
	}

	@Test
	public void encode_returnEmptyByteArray_When_InputIsEmptyString() throws Exception {
		AndroidEncodingService encodingService = new AndroidEncodingService();
		assertEquals(0, encodingService.base64Encode("".getBytes()).length);
		assertEquals("", new String(encodingService.base64Encode("".getBytes())));
	}

	@Test
	public void encode_returnValidData_When_InputIsValidString() throws Exception {
		AndroidEncodingService encodingService = new AndroidEncodingService();
		assertEquals("dGVzdCFAIw==", new String(encodingService.base64Encode("test!@#".getBytes())));
	}

	@Test
	public void encode_and_decode() throws Exception {
		AndroidEncodingService encodingService = new AndroidEncodingService();
		String result = new String(encodingService.base64Encode("test!@#".getBytes()));
		assertEquals("test!@#", new String(encodingService.base64Decode(result)));
	}
}
