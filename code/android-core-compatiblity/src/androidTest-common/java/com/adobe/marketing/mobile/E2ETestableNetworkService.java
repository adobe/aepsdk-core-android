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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class E2ETestableNetworkService extends AndroidNetworkService {

	private List<NetworkRequest> networkRequestList = new ArrayList<NetworkRequest>();
	private NetworkResponse defaultResponse = new NetworkResponse("");
	private Map<NetworkRequestMatcher, NetworkResponse> responseMatchers = new
	HashMap<NetworkRequestMatcher, NetworkResponse>();
	private CountUpLatch countUpLatch = new CountUpLatch();


	public E2ETestableNetworkService() {
		super(null);
	}

	public enum NetworkRequestType {
		ASYNC,
		SYNC;

		NetworkRequestType() {
		}
	}

	public static class NetworkRequestMatcher {
		public boolean match(NetworkRequest request) {
			return false;
		}
	}

	public static class NetworkResponse {
		int responseCode;
		InputStream stream;
		Map<String, String> headers;
		int latency = 0;
		public NetworkResponse(String response) {
			this(response, 200, null, 0);
		}
		public NetworkResponse(InputStream inputStream) {
			this(inputStream, 200, null);
		}


		public NetworkResponse(String response, int responseCode) {
			this(response, responseCode, null);
		}

		public NetworkResponse(InputStream inputStream, int responseCode) {
			this(inputStream, responseCode, null);
		}

		public NetworkResponse(String response, int responseCode, Map<String, String> headers) {
			this(response, responseCode, headers, 0);
		}

		public NetworkResponse(InputStream inputStream, int responseCode, Map<String, String> headers) {
			this(inputStream, responseCode, headers, 0);
		}

		public NetworkResponse(String response, int responseCode, Map<String, String> headers, int latency) {
			this.stream = new ByteArrayInputStream(response.getBytes(Charset.forName("UTF-8")));
			this.responseCode = responseCode;
			this.headers = headers == null ? new HashMap<String, String>() : headers ;
			this.latency = latency;
		}

		public NetworkResponse(InputStream inputStream, int responseCode, Map<String, String> headers, int latency) {
			BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
			DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);
			byte[] data = null;

			try {
				data = new byte[dataInputStream.available()];
				dataInputStream.readFully(data);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					dataInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (data != null) {
				this.stream = new ByteArrayInputStream(data);
				this.responseCode = responseCode;
				this.headers = headers == null ? new HashMap<String, String>() : headers;
				this.latency = latency;
			}
		}
	}


	public static class NetworkRequest {
		public String url;
		public NetworkService.HttpCommand command;
		public byte[] connectPayload;
		public Map<String, String> requestProperty;
		public int connectTimeout;
		public int readTimeout;
		public NetworkService.Callback resultCallback;
		public NetworkRequestType type;

		NetworkRequest(String url, NetworkService.HttpCommand command, byte[] connectPayload,
					   Map<String, String> requestProperty,
					   int connectTimeout, int readTimeout, NetworkService.Callback resultCallback, NetworkRequestType type) {
			this.url = url;
			this.command = command;
			this.connectPayload = connectPayload;
			this.requestProperty = requestProperty;
			this.connectTimeout = connectTimeout;
			this.readTimeout = readTimeout;
			this.resultCallback = resultCallback;
			this.type = type;
		}

		public String getPostString() {
			try {
				return new String(connectPayload, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				return null;
			}
		}

		public Map<String, String> getContextData() {
			return new HashMap<>();
		}
	}

	private NetworkResponse getResponse(NetworkRequest networkRequest) {
		for (Map.Entry<NetworkRequestMatcher, NetworkResponse> entry : responseMatchers.entrySet()) {
			if (entry.getKey().match(networkRequest)) {
				return entry.getValue();
			}
		}

		return defaultResponse;
	}

	public HttpConnection processAsyncNetworkRequest(final NetworkRequest networkRequest) {
		networkRequestList.add(networkRequest);
		final NetworkResponse response = getResponse(networkRequest);
		final HttpConnection httpConnection = new HttpConnection() {
			@Override
			public InputStream getInputStream() {
				return response.stream;
			}

			@Override
			public InputStream getErrorStream() {
				return null;
			}

			@Override
			public int getResponseCode() {
				return response.responseCode;
			}

			@Override
			public String getResponseMessage() {
				return null;
			}

			@Override
			public String getResponsePropertyValue(String responsePropertyKey) {
				return response.headers.get(responsePropertyKey);
			}

			@Override
			public void close() {
			}
		};

		if (networkRequest.resultCallback != null) {
			new Thread(new Runnable() {
				@Override
				public void run() {

					networkRequest.resultCallback.call(httpConnection);
					countUpLatch.countUp();
				}
			}).start();
			return null;
		}

		if (response.latency > 0) {
			try {
				Thread.sleep(response.latency);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		countUpLatch.countUp();
		return httpConnection;
	}


	/**
	 * This method returns all the URLs from the network requests list. It can be used for logging
	 * on requests count assertions
	 *
	 * @return {@link String} with all the URLs appended
	 */
	public String getAllRequestsAsString() {
		StringBuilder requestsString = new StringBuilder();
		requestsString.append("Requests list: ");

		for (NetworkRequest request : networkRequestList) {
			if (request != null) {
				if (request.connectPayload != null) {
					requestsString.append("[ " + request.url + " - " + new String(request.connectPayload) + " ], ");
				} else {
					requestsString.append("[ " + request.url + " - no payload ], ");
				}
			}
		}

		return requestsString.toString();
	}

	public void setResponse(NetworkRequestMatcher matcher, NetworkResponse response) {
		responseMatchers.put(matcher, response);
	}

	public int waitAndGetCount(int expectedCount) {
		com.adobe.marketing.mobile.Log.warning("Test", "waitAndGetCount for %d", expectedCount);
		countUpLatch.await(expectedCount);
		return networkRequestList.size();
	}


	public int waitAndGetCount(int expectedCount, int timeoutInMilli) {
		com.adobe.marketing.mobile.Log.warning("Test", "waitAndGetCount for %d", expectedCount);
		countUpLatch.await(expectedCount, timeoutInMilli);
		return networkRequestList.size();
	}


	public NetworkRequest getItem(int index) {
		return networkRequestList.get(index);
	}

	public void setResponse(NetworkRequestMatcher matcher, String response) {
		responseMatchers.put(matcher, new NetworkResponse(response));
	}

	public void setDefaultResponse(String response) {
		this.defaultResponse = new NetworkResponse(response);
	}

	public void setDefaultResponse(NetworkResponse response) {
		this.defaultResponse = response;
	}

	public void resetNetworkRequestList() {
		countUpLatch = new CountUpLatch();
		networkRequestList = new ArrayList<NetworkRequest>();
	}

	public void resetNetworkResponse() {
		defaultResponse = new NetworkResponse("");
	}

	public void resetTestableNetworkService() {
		resetNetworkRequestList();
		resetNetworkResponse();
		this.responseMatchers.clear();
	}
}