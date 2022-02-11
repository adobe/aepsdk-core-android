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

import java.util.Map;
import java.util.concurrent.CountDownLatch;

import com.adobe.marketing.mobile.services.HttpMethod;
import com.adobe.marketing.mobile.services.HttpConnecting;
import com.adobe.marketing.mobile.services.NetworkCallback;
import com.adobe.marketing.mobile.services.NetworkRequest;
import com.adobe.marketing.mobile.services.Networking;

class AndroidNetworkService implements NetworkService {

	private static final String TAG = AndroidNetworkService.class.getSimpleName();
	private final Networking newNetworkService;

	AndroidNetworkService(final Networking networkService) {
		this.newNetworkService = networkService;
	}

	/**
	 * Connect to a url.
	 *
	 * @param url {@code String} containing the full url for connection
	 * @param command {@link HttpCommand}, for example "POST", "GET" etc.
	 * @param connectPayload {@code byte[]} array specifying payload to send to the server
	 * @param requestProperty {@code Map<String, String>} containing any additional key value pairs to be used while requesting a
	 *                        connection to the url depending on the {@code command} used
	 * @param connectTimeout {@code int} indicating connect timeout value in seconds
	 * @param readTimeout {@code int} indicating the timeout, in seconds, that will be used to wait for a read to finish after a successful connect
	 *
	 * @return {@link HttpConnecting} instance, representing a connection attempt
	 *
	 */
	@Override
	public HttpConnection connectUrl(final String url,
									 final HttpCommand command,
									 final byte[] connectPayload,
									 final Map<String, String> requestProperty,
									 final int connectTimeout,
									 final int readTimeout) {

		final CountDownLatch countDownLatch = new CountDownLatch(1);
		final HttpConnecting[] httpConnecting = new HttpConnecting[1];

		newNetworkService.connectAsync(new NetworkRequest(url, convert(command), connectPayload, requestProperty,
									   connectTimeout, readTimeout),
		new NetworkCallback() {

			@Override
			public void call(final HttpConnecting connection) {
				httpConnecting[0] = connection;
				countDownLatch.countDown();
			}
		});

		try {
			countDownLatch.await();
			return new AndroidHttpConnection(httpConnecting[0]);
		} catch (final InterruptedException e) {
			Log.warning(TAG, "Connection failure (%s)", e);
		} catch (final IllegalArgumentException e) {
			Log.warning(TAG, "Connection failure (%s)", e);
		}

		return null;
	}

	/**
	 * Converts the com.adobe.marketing.mobile.HttpCommand instance to com.adobe.marketing.mobile.services.HttpCommand
	 * @param command the {@link HttpCommand} instance to convert
	 * @return the corresponding com.adobe.marketing.mobile.services.HttpCommand object
	 */
	private HttpMethod convert(final HttpCommand command) {
		if (command == HttpCommand.POST) {
			return HttpMethod.POST;
		}

		return HttpMethod.GET;
	}

	/**
	 * Async variation of the {@code connectUrl} api, that will deliver the result through the callback.
	 *
	 * @param url {@code String} containing the full url for connection
	 * @param command {@link HttpCommand}, for example "POST", "GET" etc.
	 * @param connectPayload {@code byte[]} array specifying payload to send to the server
	 * @param requestProperty {@code Map<String, String>} containing any additional key value pairs to be used while requesting a
	 *                        connection to the url depending on the {@code command} used
	 * @param connectTimeout {@code int} indicating connect timeout value in seconds
	 * @param readTimeout {@code int} indicating the timeout, in seconds, that will be used to wait for a read to finish after a successful connect
	 * @param resultCallback {@link Callback} that will receive the {@link HttpConnecting} instance after the connection has been made
	 *
	 * @see Callback
	 */
	@Override
	public void connectUrlAsync(final String url,
								final HttpCommand command,
								final byte[] connectPayload,
								final Map<String, String> requestProperty,
								final int connectTimeout,
								final int readTimeout,
								final Callback resultCallback) {

		Thread backgroundThread = new Thread(new Runnable() {
			@Override
			public void run() {
				final HttpConnection connection = connectUrl(url, command, connectPayload, requestProperty, connectTimeout,
												  readTimeout);

				if (resultCallback != null) {
					resultCallback.call(connection);
				} else {
					// If no callback is passed by the client, close the connection.
					if (connection != null) {
						connection.close();
					}
				}
			}
		});
		backgroundThread.start();
	}

}
