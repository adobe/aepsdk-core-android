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

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;


public class HexStringUtilTests {

	@Test
	public void testHexStringDecodeEncode() {
		String etag = "W/“3a2-bMnM1spT5zNBH3xgDTaqZQ”";
		String hexString = HexStringUtil.getHexString(etag);
		assertEquals(etag, HexStringUtil.hexToString(hexString));
	}

	@Test
	public void testDecodeOutputComplianceWithStandardLibrary() {
		String message = "Hello world! 123!@#$%^&*(){}/\\\"\'";
		String hexString1 = HexStringUtil.getHexString(message);
		byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
		char[] result = Hex.encodeHex(bytes, false);
		String hexString2 = new String(result);
		assertEquals(hexString2, hexString1);
		assertEquals(message, HexStringUtil.hexToString(hexString1));
	}

	@Test
	public void testDecodeNullString() {
		String result = HexStringUtil.getHexString(null);
		assertEquals(null, result);
	}

	@Test
	public void testDecodeEmptyString() {
		String result = HexStringUtil.getHexString("");
		assertEquals(null, result);
	}


}