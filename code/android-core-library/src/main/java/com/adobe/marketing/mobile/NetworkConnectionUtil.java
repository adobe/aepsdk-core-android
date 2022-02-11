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
import java.net.HttpURLConnection;
import java.util.*;


final class NetworkConnectionUtil {
	private static final String DEFAULT_CHARSET = "UTF-8";
	private static final int    BUF_SIZE        = 1024;

	static final String HTTP_HEADER_KEY_CONTENT_TYPE = "Content-Type";
	static final String HTTP_HEADER_KEY_ACCEPT_LANGUAGE = "Accept-Language";
	static final String HTTP_HEADER_KEY_ACCEPT = "Accept";


	static final String HTTP_HEADER_CONTENT_TYPE_JSON_APPLICATION = "application/json";
	static final String HTTP_HEADER_CONTENT_TYPE_WWW_FORM_URLENCODED =
		"application/x-www-form-urlencoded";
	static final String HTTP_HEADER_ACCEPT_TEXT_HTML = "text/html";

	private NetworkConnectionUtil() {}

	static ArrayList<Integer> recoverableNetworkErrorCodes = new ArrayList<Integer>(Arrays.asList(
				HttpURLConnection.HTTP_CLIENT_TIMEOUT,
				HttpURLConnection.HTTP_GATEWAY_TIMEOUT,
				HttpURLConnection.HTTP_UNAVAILABLE
			));

	/**
	 * Calls {@link #readFromInputStream(InputStream, String)} with the default charset - UTF-8
	 *
	 * @param inputStream connection's input stream
	 * @return data from input stream as string
	 * @throws IOException if reading from input stream or conversion to string fails
	 */
	static String readFromInputStream(final InputStream inputStream) throws IOException {
		return readFromInputStream(inputStream, DEFAULT_CHARSET);
	}

	/**
	 * Reads data from the provided inputStream in chunks of 1024 and returns it as string. It uses charsetName UTF-8
	 * for conversion to string
	 *
	 * @param inputStream connection's input stream
	 * @param charset     optional parameter for charset, if it is missing UTF-8 constant will be used
	 *
	 * @return data from input stream as {@code String}
	 * @throws IOException if reading from input stream or conversion to string fails
	 */
	static String readFromInputStream(final InputStream inputStream, final String charset) throws IOException {
		if (inputStream == null) {
			return null;
		}

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[BUF_SIZE];

		while (true) {
			final int len = inputStream.read(buffer);

			if (len == -1) {
				break;
			}

			baos.write(buffer, 0, len);
		}

		inputStream.close();
		String charsetName = !StringUtils.isNullOrEmpty(charset) ? charset : DEFAULT_CHARSET;
		return baos.toString(charsetName);
	}

	/**
	 * Returns the default headers for connection: Content-Type and connection
	 *
	 * @param ssl {@code boolean} indicating if https is enabled
	 * @return {@code Map<String, String>} with the specified keys and values
	 */
	static Map<String, String> getHeaders(final boolean ssl) {
		return getHeaders(ssl, null);
	}

	/**
	 * Returns the default headers for connection: Content-Type and connection
	 *
	 * @param ssl 			{@code boolean} indicating if https is enabled
	 * @param contentType 	the custom content type {@code String}
	 * @return {@code Map<String, String>} with the specified keys and values
	 */
	static Map<String, String> getHeaders(final boolean ssl, final String contentType) {
		final Map<String, String> headers = new HashMap<String, String>();

		if (!ssl) {
			headers.put("connection", "close");
		}

		headers.put(HTTP_HEADER_KEY_CONTENT_TYPE, StringUtils.isNullOrEmpty(contentType) ?
					HTTP_HEADER_CONTENT_TYPE_WWW_FORM_URLENCODED : contentType);
		return headers;
	}

}