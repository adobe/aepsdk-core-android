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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Use {@link com.adobe.marketing.mobile.internal.util.StringUtils} instead.
 */
@Deprecated
final class StringUtils {
	static final String CHARSET_UTF_8 = "UTF-8";

	private static final int STREAM_READ_BUFFER_SIZE = 1024;
	private static final String LOG_SOURCE = "StringUtils";

	private StringUtils() {}

	/**
	 * Checks if a {@code String} is null, empty or it only contains whitespaces.
	 *
	 * @param str the {@link String} that we want to check
	 * @return {@code boolean} with the evaluation result
	 */
	static boolean isNullOrEmpty(final String str) {
		return str == null || str.trim().isEmpty();
	}

	/**
	 * Get the String representation of an {@code InputStream}
	 *
	 * @param inputStream {@link InputStream} to read
	 * @return {@link String} representation of the input stream
	 */
	static String streamToString(final InputStream inputStream) {
		if (inputStream == null) {
			return null;
		}

		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		final byte[] data = new byte[STREAM_READ_BUFFER_SIZE];
		int bytesRead;

		try {
			while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, bytesRead);
			}

			return buffer.toString(CHARSET_UTF_8);
		} catch (final IOException ex) {
			Log.debug(LOG_SOURCE, "Unable to convert InputStream to String, %s", ex);
			return null;
		}
	}

	/**
	 * Check if the given {@code String} is a valid URL.
	 * <p>
	 * It uses {@link URL} class to identify that.
	 *
	 * @param stringUrl URL that needs to be tested
	 * @return return a {@code boolean} indicating if the given parameter is a valid URL
	 */
	static boolean stringIsUrl(final String stringUrl) {
		if (isNullOrEmpty(stringUrl)) {
			return false;
		}

		try {
			new URL(stringUrl);
			return true;
		} catch (MalformedURLException ex) {
			return false;
		}
	}
}
