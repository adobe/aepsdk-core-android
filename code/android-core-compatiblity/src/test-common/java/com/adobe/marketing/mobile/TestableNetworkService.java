package com.adobe.marketing.mobile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by jgeng on 5/8/17.
 */
public class TestableNetworkService implements NetworkService {
	private List<NetworkRequest> networkRequestList = new ArrayList<NetworkRequest>();
	private NetworkResponse defaultResponse = new NetworkResponse("");
	private Map<NetworkRequestMatcher, NetworkResponse> responseMatchers = new
	HashMap<NetworkRequestMatcher, NetworkResponse>();
	private CountDownLatch latch = new CountDownLatch(0);
	private CountUpLatch countUpLatch = new CountUpLatch();
	private List<String> ignoredURLs = new ArrayList<String>();

	@Override
	public HttpConnection connectUrl(final String url, final HttpCommand command, final byte[] connectPayload,
									 final Map<String, String> requestProperty,
									 final int connectTimeout, final int readTimeout) {
		return processAsyncNetworkRequest(
				   new NetworkRequest(url, command, connectPayload, requestProperty,
									  connectTimeout, readTimeout, null, NetworkRequestType.SYNC));
	}

	@Override
	public void connectUrlAsync(final String url, final HttpCommand command, final byte[] connectPayload,
								final Map<String, String>
								requestProperty,
								final int connectTimeout, final int readTimeout, final Callback resultCallback) {
		processAsyncNetworkRequest(
			new NetworkRequest(url, command, connectPayload, requestProperty,
							   connectTimeout, readTimeout, resultCallback, NetworkRequestType.ASYNC));
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
		byte[] response;
		int responseDelay;
		Map<String, String> headers;

		public NetworkResponse(String response) {
			this(response, 200);
		}

		public NetworkResponse(byte[] response) {
			this(response, 200);
		}

		public NetworkResponse(byte[] response, int responseCode) {
			this(response, responseCode, 0);
		}

		public NetworkResponse(String response, int responseCode) {
			this(response, responseCode, 0);
		}

		public NetworkResponse(byte[] response, int responseCode, int responseDelay) {
			this(response, responseCode, responseDelay, null);
		}

		public NetworkResponse(String response, int responseCode, int responseDelay) {
			this(response, responseCode, responseDelay, null);
		}

		public NetworkResponse(byte[] response, int responseCode, Map<String, String> headers) {
			this(response, responseCode, 0, headers);
		}

		public NetworkResponse(String response, int responseCode, Map<String, String> headers) {
			this(response, responseCode, 0, headers);
		}

		public NetworkResponse(String response, int responseCode, int responseDelay, Map<String, String> headers) {
			this.response = (response != null ? response.getBytes(Charset.forName("UTF-8")) : null);
			this.responseCode = responseCode;
			this.responseDelay = responseDelay;
			this.headers = headers == null ? new HashMap<String, String>() : headers;
		}

		public NetworkResponse(byte[] response, int responseCode, int responseDelay, Map<String, String> headers) {
			this.response = response;
			this.responseCode = responseCode;
			this.responseDelay = responseDelay;
			this.headers = headers == null ? new HashMap<String, String>() : headers;
		}

	}

	public static class NetworkRequest {
		public String url;
		public NetworkService.HttpCommand command;
		public byte[] connectPayload;
		public String connectPayloadString;
		public Map<String, String> requestProperty;
		public int connectTimeout;
		public int readTimeout;
		public NetworkService.Callback resultCallback;
		public NetworkRequestType type;

		NetworkRequest(String url, NetworkService.HttpCommand command, byte[] connectPayload,
					   Map<String, String> requestProperty,
					   int connectTimeout, int readTimeout, NetworkService.Callback resultCallback, NetworkRequestType
					   type) {
			this.url = url;
			this.command = command;
			this.connectPayload = connectPayload;

			this.connectPayloadString = new String(connectPayload == null ? new byte[] {} : connectPayload);
			this.requestProperty = requestProperty;
			this.connectTimeout = connectTimeout;
			this.readTimeout = readTimeout;
			this.resultCallback = resultCallback;
			this.type = type;
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

	public NetworkService.HttpConnection processAsyncNetworkRequest(final NetworkRequest networkRequest) {
		if (!ignoredURLs.contains(networkRequest.url)) {
			networkRequestList.add(networkRequest);
		}

		final NetworkResponse networkResponse = getResponse(networkRequest);

		final NetworkService.HttpConnection httpConnection = new NetworkService.HttpConnection() {

			@Override
			public InputStream getInputStream() {
				return new ByteArrayInputStream(networkResponse.response);
			}

			@Override
			public InputStream getErrorStream() {
				return null;
			}

			@Override
			public int getResponseCode() {
				return networkResponse.responseCode;
			}

			@Override
			public String getResponseMessage() {
				return null;
			}

			@Override
			public String getResponsePropertyValue(final String responsePropertyKey) {
				return networkResponse.headers.get(responsePropertyKey);
			}

			@Override
			public void close() {

			}
		};

		if (networkRequest.resultCallback != null) {
			if (networkResponse.responseDelay > networkRequest.connectTimeout) {
				networkRequest.resultCallback.call(null);

				//				try {
				//					Thread.sleep(networkResponse.responseDelay);
				//				} catch (InterruptedException v) {}
			} else {
				networkRequest.resultCallback.call(httpConnection);
			}
		}

		latch.countDown();
		countUpLatch.countUp();
		return httpConnection;
	}

	public void setExpectedCount(int count) {
		latch = new CountDownLatch(count);
	}

	public int waitAndGetCount() {
		return waitAndGetCount(500);
	}

	public int waitAndGetCount(int milliseconds) {
		try {
			latch.await(milliseconds, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return networkRequestList.size();
	}

	public void waitFor(int count) {
		countUpLatch.await(count);
	}

	public int count() {

		return networkRequestList.size();
	}

	public void clearNetworkRequestList() {
		countUpLatch = new CountUpLatch();
		this.networkRequestList.clear();
	}

	public NetworkRequest getItem(int index) {
		return networkRequestList.get(index);
	}

	public void setResponse(NetworkRequestMatcher matcher, NetworkResponse response) {
		responseMatchers.put(matcher, response);
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

	void clearNetworkRequests() {
		networkRequestList.clear();
	}

	void clearResponseMatchers() {
		responseMatchers.clear();
	}

	void clearDefaultResponse() {
		defaultResponse = new NetworkResponse("");
	}

	void reset() {
		clearNetworkRequests();
		clearResponseMatchers();
		clearDefaultResponse();
		clearIgnoredURLs();
	}

	void ignoreNetworkRequestURL(final String url) {
		if (!ignoredURLs.contains(url)) {
			ignoredURLs.add(url);
		}
	}

	void clearIgnoredURLs() {
		ignoredURLs.clear();
	}
}
