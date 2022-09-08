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
import java.util.Map;

interface NetworkService {

	/**
	 * HttpCommand enum contains the HTTP commands supported by SDK.
	 * <p>
	 * Currently supported command types are GET/POST
	 **/
	enum HttpCommand {
		GET,
		HEAD,
		POST,
		PUT,
		DELETE,
		TRACE,
		OPTIONS,
		CONNECT,
		PATCH;

		HttpCommand() {}
	}

	interface Callback {
		/**
		 * Callback method invoked with the {@code HttpConnection} instance once the connection is established.
		 *
		 * @param connection {@link HttpConnection} instance
		 */
		void call(final HttpConnection connection);
	}


	interface HttpConnection extends HttpConnecting {

	}

	/**
	 * Connect to a url.
	 *
	 * @param url {@link String} containing the full url for connection
	 * @param command {@link HttpCommand}, for example "POST", "GET" etc.
	 * @param connectPayload {@code byte[]} array specifying payload to send to the server
	 * @param requestProperty {@code Map<String, String>} containing any additional key value pairs to be used while requesting a
	 *                        connection to the url depending on the {@code command} used
	 * @param connectTimeout {@code int} indicating connect timeout value in seconds
	 * @param readTimeout {@code int} indicating the timeout, in seconds, that will be used to wait for a read to finish after a successful connect
	 *
	 * @return {@link HttpConnection} instance, representing a connection attempt
	 */
	HttpConnection connectUrl(final String url,
							  final HttpCommand command,
							  final byte[] connectPayload,
							  final Map<String, String> requestProperty,
							  final int connectTimeout,
							  final int readTimeout);

	/**
	 * Async variation of the connectUrl API, that will deliver the result through the callback.
	 *
	 * @param url {@link String} containing the full url for connection
	 * @param command {@link HttpCommand}, for example "POST", "GET" etc.
	 * @param connectPayload {@code byte[]} array specifying payload to send to the server
	 * @param requestProperty {@code Map<String, String>} containing any additional key value pairs to be used while requesting a
	 *                        connection to the url depending on the {@code command} used
	 * @param connectTimeout {@code int} indicating connect timeout value in seconds
	 * @param readTimeout {@code int} indicating the timeout, in seconds, that will be used to wait for a read to finish after a successful connect
	 * @param resultCallback {@link Callback} that will receive the {@link HttpConnection} instance after the connection has been made
	 *
	 * @see Callback
	 */
	void connectUrlAsync(final String url,
						 final HttpCommand command,
						 final byte[] connectPayload,
						 final Map<String, String> requestProperty,
						 final int connectTimeout,
						 final int readTimeout,
						 final Callback resultCallback);
}
