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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

final class UrlUtilities {

	// lookup tables used by urlEncode
	private static final String[] encodedChars = new String[] {
		"%00", "%01", "%02", "%03", "%04", "%05", "%06", "%07", "%08", "%09", "%0A", "%0B", "%0C", "%0D", "%0E", "%0F",
		"%10", "%11", "%12", "%13", "%14", "%15", "%16", "%17", "%18", "%19", "%1A", "%1B", "%1C", "%1D", "%1E", "%1F",
		"%20", "%21", "%22", "%23", "%24", "%25", "%26", "%27", "%28", "%29", "%2A", "%2B", "%2C", "-", ".", "%2F",
		"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "%3A", "%3B", "%3C", "%3D", "%3E", "%3F",
		"%40", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O",
		"P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "%5B", "%5C", "%5D", "%5E", "_",
		"%60", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o",
		"p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "%7B", "%7C", "%7D", "~", "%7F",
		"%80", "%81", "%82", "%83", "%84", "%85", "%86", "%87", "%88", "%89", "%8A", "%8B", "%8C", "%8D", "%8E", "%8F",
		"%90", "%91", "%92", "%93", "%94", "%95", "%96", "%97", "%98", "%99", "%9A", "%9B", "%9C", "%9D", "%9E", "%9F",
		"%A0", "%A1", "%A2", "%A3", "%A4", "%A5", "%A6", "%A7", "%A8", "%A9", "%AA", "%AB", "%AC", "%AD", "%AE", "%AF",
		"%B0", "%B1", "%B2", "%B3", "%B4", "%B5", "%B6", "%B7", "%B8", "%B9", "%BA", "%BB", "%BC", "%BD", "%BE", "%BF",
		"%C0", "%C1", "%C2", "%C3", "%C4", "%C5", "%C6", "%C7", "%C8", "%C9", "%CA", "%CB", "%CC", "%CD", "%CE", "%CF",
		"%D0", "%D1", "%D2", "%D3", "%D4", "%D5", "%D6", "%D7", "%D8", "%D9", "%DA", "%DB", "%DC", "%DD", "%DE", "%DF",
		"%E0", "%E1", "%E2", "%E3", "%E4", "%E5", "%E6", "%E7", "%E8", "%E9", "%EA", "%EB", "%EC", "%ED", "%EE", "%EF",
		"%F0", "%F1", "%F2", "%F3", "%F4", "%F5", "%F6", "%F7", "%F8", "%F9", "%FA", "%FB", "%FC", "%FD", "%FE", "%FF"
	};

	private static final int ALL_BITS_ENABLED = 0xFF;
	private static final boolean[] utf8Mask = new boolean[] {
		false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
		false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
		false, false, false, false, false, false, false, false, false, false,  false, false, false,  true,  true, false,
		true,  true,  true,  true,  true,  true,  true,  true,  true,  true, false, false, false, false, false, false,
		false,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,
		true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true, false, false, false, false,  true,
		false,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,
		true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true, false, false, false, true, false,
		false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
		false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
		false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
		false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
		false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
		false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
		false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
		false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false
	};
	private static final String LOG_TAG = UrlUtilities.class.getSimpleName();

	private UrlUtilities() {}

	/**
	 * Serializes a {@code Map<String,Object>} of key-value pairs to a {@code String}.
	 * <p>
	 * This method will return null if the given map is null
	 *
	 * @param parameters nullable {@code Map<String,Object>} of key-value pairs to be serialized
	 * @return resulted serialized query parameters as {@link String}
	 */
	public static String serializeToQueryString(final Map<String, Variant> parameters) {
		// bail if we have nothing
		if (parameters == null) {
			return null;
		}

		final StringBuilder sBuilder = new StringBuilder();

		for (Map.Entry<String, Variant> entry : parameters.entrySet()) {
			final String encodedKey = urlEncode(entry.getKey());
			final Variant value = entry.getValue();
			String encodedValue = null;

			try {
				if (value.getKind() == VariantKind.VECTOR) {
					encodedValue = urlEncode(join(value.getStringList(), ","));
				} else {
					encodedValue = urlEncode(value.convertToString());
				}
			} catch (VariantException ex) {
				continue; // type is not convertible to string
			}

			final String serializedKVP = serializeKeyValuePair(encodedKey, encodedValue);

			if (serializedKVP != null) {
				sBuilder.append(serializedKVP);
			}
		}

		return sBuilder.toString();
	}

	/**
	 * Encodes an URL given as {@code String}.
	 *
	 * @param unencodedString nullable {@link String} value to be encoded
	 * @return the encoded {@code String}
	 */
	public static String urlEncode(final String unencodedString) {
		// bail fast
		if (unencodedString == null) {
			return null;
		}

		try {
			final byte[] stringBytes = unencodedString.getBytes("UTF-8");
			final int len = stringBytes.length;
			int curIndex = 0;

			// iterate looking for any characters that don't match our "safe" mask
			while (curIndex < len && utf8Mask[stringBytes[curIndex] & ALL_BITS_ENABLED]) {
				curIndex++;
			}

			// if our iterator got all the way to the end of the string, no unsafe characters existed
			// and it's safe to return the original value that was passed in
			if (curIndex == len) {
				return unencodedString;
			}

			// if we get here we know there's at least one character we need to encode
			final StringBuilder encodedString = new StringBuilder(stringBytes.length << 1);

			// if i > than 1 then we have some characters we can just "paste" in
			if (curIndex > 0) {
				encodedString.append(new String(stringBytes, 0, curIndex, "UTF-8"));
			}

			// rip through the rest of the string character by character
			for (; curIndex < len; curIndex++) {
				encodedString.append(encodedChars[stringBytes[curIndex] & ALL_BITS_ENABLED]);
			}

			// return the completed string
			return encodedString.toString();
		} catch (UnsupportedEncodingException e) {
			Log.debug(LOG_TAG, "Failed to url encode string %s (%s)", unencodedString, e);
			return null;
		}
	}

	/**
	 * Serializes a key/value pair for URL consumption, e.g. {@code &key=value}.
	 * <p>
	 * It returns null if the key or the value is null or empty.
	 *
	 * @param key nullable {@link String} to be appended before "="
	 * @param value nullable {@code String} to be appended after "="
	 * @return nullable serialized key-value pair
	 */
	public static String serializeKeyValuePair(final String key, final String value) {
		if (key == null || value == null || key.length() == 0) {
			return null;
		}

		final StringBuilder sBuilder = new StringBuilder(key.length() + value.length() + 2);
		sBuilder.append("&");
		sBuilder.append(key);
		sBuilder.append("=");
		sBuilder.append(value);
		return sBuilder.toString();
	}

	/**
	 * Returns a {@code String} containing the elements joined by delimiters.
	 *
	 * @param elements an array objects to be joined. A {@link String} will be formed from the objects
	 *                 by calling object.toString().
	 * @param delimiter	the {@code String} to be used as the delimiter between all elements
	 * @return {@code String} containing the elements joined by delimiters
	 */
	static String join(final Iterable<?> elements, final String delimiter) {
		final StringBuilder sBuilder = new StringBuilder();
		final Iterator iterator = elements.iterator();

		// TODO: consider breaking on null items, otherwise we end up with sample1,null,sample3 instead of sample1,sample3
		while (iterator.hasNext()) {
			sBuilder.append(iterator.next());

			if (iterator.hasNext()) {
				sBuilder.append(delimiter);
			}
		}

		return sBuilder.toString();
	}

	static Map<String, String> extractQueryParameters(final String queryString) {
		if (StringUtils.isNullOrEmpty(queryString)) {
			return null;
		}

		final Map<String, String> parameters = new HashMap<String, String>();
		final String[] paramArray = queryString.split("&");

		for (String currentParam : paramArray) {
			// quick out in case this entry is null or empty string
			if (StringUtils.isNullOrEmpty(currentParam)) {
				continue;
			}

			final String[] currentParamArray = currentParam.split("=", 2);

			if (currentParamArray.length != 2 ||
					(currentParamArray[0].isEmpty() || currentParamArray[1].isEmpty())) {
				continue;
			}

			final String key = currentParamArray[0];
			final String value = currentParamArray[1];
			parameters.put(key, value);
		}

		return parameters;
	}
}
