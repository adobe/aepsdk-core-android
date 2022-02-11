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

import java.io.UnsupportedEncodingException;

class HexStringUtil {
	private static final String LOG_TAG = HexStringUtil.class.getSimpleName();
	private static final char[] BYTE_TO_HEX = (
				"000102030405060708090A0B0C0D0E0F" +
				"101112131415161718191A1B1C1D1E1F" +
				"202122232425262728292A2B2C2D2E2F" +
				"303132333435363738393A3B3C3D3E3F" +
				"404142434445464748494A4B4C4D4E4F" +
				"505152535455565758595A5B5C5D5E5F" +
				"606162636465666768696A6B6C6D6E6F" +
				"707172737475767778797A7B7C7D7E7F" +
				"808182838485868788898A8B8C8D8E8F" +
				"909192939495969798999A9B9C9D9E9F" +
				"A0A1A2A3A4A5A6A7A8A9AAABACADAEAF" +
				"B0B1B2B3B4B5B6B7B8B9BABBBCBDBEBF" +
				"C0C1C2C3C4C5C6C7C8C9CACBCCCDCECF" +
				"D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF" +
				"E0E1E2E3E4E5E6E7E8E9EAEBECEDEEEF" +
				"F0F1F2F3F4F5F6F7F8F9FAFBFCFDFEFF").toCharArray();
	private static final int OXFF = 0xFF;

	static String getHexString(final String originalString) {
		if (StringUtils.isNullOrEmpty(originalString)) {
			return null;
		}

		byte[] bytes;

		try {
			bytes = originalString.getBytes("UTF-8");
		} catch (UnsupportedEncodingException ex) {
			Log.warning(LOG_TAG, "Failed to get hex from string (%s)", ex.getMessage());
			return null;
		}

		final int bytesLength = bytes.length;
		final char[] chars = new char[bytesLength << 1];
		int hexIndex;
		int index = 0;
		int offset = 0;

		while (offset < bytesLength) {
			hexIndex = (bytes[offset++] & OXFF) << 1;
			chars[index++] = BYTE_TO_HEX[hexIndex++];
			chars[index++] = BYTE_TO_HEX[hexIndex];
		}

		return new String(chars);
	}
	static String hexToString(final String hexString) {
		if (hexString == null || hexString.length() <= 0 || hexString.length() % 2 != 0) {
			return null;
		}

		final int length = hexString.length();
		byte[] data = new byte[length / 2];

		for (int i = 0; i < length; i += 2) {
			final int radix = 16;
			final int fourDigit = 4;
			data[i / 2] = (byte)((Character.digit(hexString.charAt(i), radix) << fourDigit) +
								 Character.digit(hexString.charAt(i + 1), radix));
		}

		String decodedString = null;

		try {
			decodedString = new String(data, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			Log.warning(LOG_TAG, "Failed to get string from hex (%s)", ex.getMessage());
		}

		return decodedString;
	}
}
