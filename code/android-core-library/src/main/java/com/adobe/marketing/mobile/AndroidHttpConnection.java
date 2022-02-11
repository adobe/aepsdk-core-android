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

import com.adobe.marketing.mobile.services.HttpConnecting;

import java.io.InputStream;
import java.net.HttpURLConnection;

class AndroidHttpConnection implements NetworkService.HttpConnection {

	private static final String TAG = AndroidHttpConnection.class.getSimpleName();
	private final HttpConnecting innerConnection;

	/**
	 * Constructor
	 *
	 * @param httpConnecting {@link HttpConnecting} instance, supports HTTP specific features
	 *
	 * @throws IllegalArgumentException if {@code httpConnecting} is null
	 */
	AndroidHttpConnection(final HttpConnecting httpConnecting) {
		if (httpConnecting == null) {
			throw new IllegalArgumentException();
		}

		this.innerConnection = httpConnecting;
	}
	/**
	 * Returns an input stream to read the application server response from this open connection, if available.
	 * <p>
	 * This method invokes {@link HttpURLConnection#getInputStream()} and returns null if {@code getInputSream()} throws an exception.
	 *
	 * @return {@link InputStream} connection response input stream
	 */
	@Override
	public InputStream getInputStream() {
		return innerConnection.getInputStream();
	}

	/**
	 * Returns an input stream from the connection to read the application server error response,
	 * if available.
	 *
	 * @return {@link InputStream} connection response error stream
	 */
	@Override
	public InputStream getErrorStream() {
		return innerConnection.getErrorStream();
	}

	/**
	 * Returns the connection attempt response code for this connection request.
	 * <p>
	 * This method invokes {@link HttpURLConnection#getResponseCode()} and returns -1 if {@code getResponseCode()} throws an exception
	 * or the response is not valid HTTP.
	 *
	 * @return {@code int} indicating connection status code
	 */
	@Override
	public int getResponseCode() {
		return innerConnection.getResponseCode();
	}

	/**
	 * Returns the connection attempt response message for this connection request, if available.
	 * <p>
	 * This method invokes {@link HttpURLConnection#getResponseMessage()} and returns null if {@code getResponseMessage()} throws an exception
	 * or the result is not valid HTTP.
	 *
	 * @return {@link String} containing connection response message
	 */
	@Override
	public String getResponseMessage() {
		return innerConnection.getResponseMessage();
	}

	/**
	 * Returns the value of the header field specified by the {@code responsePropertyKey} that might have been set when a connection was made to the
	 * resource pointed to by the URL.
	 * <p>
	 * This is protocol specific. For example, HTTP urls could have properties like "last-modified", or "ETag" set.
	 *
	 * @param responsePropertyKey {@link String} containing response property key
	 * @return {@code String} corresponding to the response property value for the key specified, or null, if the key does
	 * not exist
	 */
	@Override
	public String getResponsePropertyValue(final String responsePropertyKey) {
		return innerConnection.getResponsePropertyValue(responsePropertyKey);
	}

	/**
	 * Closes this open connection.
	 * <p>
	 * Invokes {@link HttpURLConnection#disconnect()} method to release the resources for this connection.
	 */
	@Override
	public void close() {
		innerConnection.close();
	}
}
