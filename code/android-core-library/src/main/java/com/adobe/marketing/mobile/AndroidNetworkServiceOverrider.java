package com.adobe.marketing.mobile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.adobe.marketing.mobile.NetworkService.HttpCommand;
import com.adobe.marketing.mobile.NetworkService.HttpConnection;
import com.adobe.marketing.mobile.services.NetworkRequest;
import com.adobe.marketing.mobile.services.ServiceProvider;
import com.adobe.marketing.mobile.services.DeviceInforming;
import com.adobe.marketing.mobile.services.NetworkCallback;
import com.adobe.marketing.mobile.services.Networking;

@SuppressWarnings({"WeakerAccess", "unused"})
public final class AndroidNetworkServiceOverrider {
	private final static String LOG_TAG = "AndroidNetworkServiceOverrider";
	/**
	 * Wraps private interface into a publicly accessible one.
	 *
	 * This is to avoid needing to modify java core to change visibility of internal interfaces.
	 */
	public interface Connecting extends HttpConnection {}

	/**
	 * Base class for network overrides.
	 */
	public static abstract class HTTPConnectionPerformer {
		/**
		 * Used by {@link AndroidNetworkService} to translate internal network-stack calls to the provided override.
		 *
		 * @param url {@code String} containing the full url for connection
		 * @param command {@link HttpCommand}, for example "POST", "GET" etc.
		 * @param connectPayload {@code byte[]} array specifying payload to send to the server
		 * @param requestProperty {@code Map<String, String>} containing any additional key value pairs to be used while requesting a
		 *                        connection to the url depending on the {@code command} used
		 * @param connectTimeout {@code int} indicating connect timeout value in seconds
		 * @param readTimeout {@code int} indicating the timeout, in seconds, that will be used to wait for a read to finish after a successful connect
		 *
		 * @return {@link HttpConnection} instance, representing a connection attempt
		 */
		HttpConnection doConnection(final String url,
									final HttpCommand command,
									final byte[] connectPayload,
									final Map<String, String> requestProperty,
									final int connectTimeout,
									final int readTimeout) {

			return connect(url, HTTP_COMMAND_MAP.get(command), connectPayload, requestProperty,
						   connectTimeout, readTimeout);
		}

		/**
		 * Determines if the provided URL and Method should be overridden by this instance.  Used by
		 * {@link AndroidNetworkService} in-order to proxy the {@code shouldOverride(String, String)} call.
		 *
		 * @param url {@code String} containing the url for the request.
		 * @param command {@link HttpCommand} containing the connection method for the provided url.
		 *
		 * @return boolean {@code true} if the connection should be overridden, {@code false} if the default Network stack should be used.
		 *
		 * @see AndroidNetworkService
		 */
		final boolean shouldOverrideInternal(final String url, final HttpCommand command) {
			return shouldOverride(url, HTTP_COMMAND_MAP.get(command));
		}

		/**
		 * Used to determine if a URL and HTTP Method combination should be overridden by this {@link HTTPConnectionPerformer}
		 * or if it should be handled by the default network implementation.
		 *
		 * Overriding this method is optional.  If no override is provided, this method always returns true.
		 *
		 * @param url {@code String} containing the full url for connection
		 * @param method {@code String}, for example "POST", "GET" etc.
		 *
		 * @return boolean {@code true} if the connection should use this performer, false if it should use the default network stack.
		 */
		public boolean shouldOverride(final String url, final String method) {
			return true;
		}

		/**
		 * Used instead of internal implementation of Network stack.
		 *
		 * Provided function must support all standard HTTP properties, as well as return a valid {@link Connecting} object.
		 * This call must block until connection is completed (either successfully or error).
		 *
		 * @param url {@code String} containing the full url for connection
		 * @param method {@code String}, for example "POST", "GET" etc.
		 * @param payload {@code byte[]} array specifying payload to send to the server
		 * @param headers {@code Map<String, String>} containing any additional key value pairs to be used while requesting a
		 *                        connection to the url depending on the {@code command} used
		 * @param connectionTimeoutSeconds {@code int} indicating connect timeout value in seconds
		 * @param readTimeoutSeconds {@code int} indicating the timeout, in seconds, that will be used to wait for a read to finish after a successful connect
		 *
		 * @return {@link Connecting} object for use by calling code, must not be null.  If an unknown error occurred, return {@link }
		 */
		public abstract Connecting connect(final String url,
										   final String method,
										   final byte[] payload,
										   final Map<String, String> headers,
										   final int connectionTimeoutSeconds,
										   final int readTimeoutSeconds);

		/**
		 * Error response that should be returned when an IOException occurs during connection.
		 */
		protected static final Connecting CONNECTION_ERROR_IO = new Connecting() {
			@Override
			public InputStream getInputStream() {
				return null;
			}

			@Override
			public InputStream getErrorStream() {
				return null;
			}

			@Override
			public int getResponseCode() {
				return -1;
			}

			@Override
			public String getResponseMessage() {
				return null;
			}

			@Override
			public String getResponsePropertyValue(String responsePropertyKey) {
				return null;
			}

			@Override
			public void close() {

			}
		};

		/**
		 * Error response that should be returned when a MalformedURLException occurs during connection.
		 */
		protected static final Connecting CONNECTION_ERROR_URL = null;
	}

	/**
	 * Used to map {@link com.adobe.marketing.mobile.services.HttpMethod} enum values into HTTP Method strings suitable for external use.
	 */
	private final static Map<com.adobe.marketing.mobile.services.HttpMethod, String> HTTP_COMMAND_MAP;
	static {
		HTTP_COMMAND_MAP = new HashMap<>();
		HTTP_COMMAND_MAP.put(com.adobe.marketing.mobile.services.HttpMethod.GET, "GET");
		HTTP_COMMAND_MAP.put(com.adobe.marketing.mobile.services.HttpMethod.POST, "POST");
	}

	/**
	 * Holds the default implementation of the {@link Networking}
	 */
	private final static Networking defaultNetworkService = ServiceProvider.getInstance().getNetworkService();

	/**
	 * Sets new HTTPConnectionPerformer to override default network activity.  If null, resets to default network stack.
	 *
	 * @param newPerformer {@link HTTPConnectionPerformer} new performer implementation.
	 */
	@SuppressWarnings("unused")
	public static void setHTTPConnectionPerformer(final HTTPConnectionPerformer newPerformer) {
		if (newPerformer != null) {
			Log.debug(LOG_TAG, "Enabling network override provided by class: %s", newPerformer.getClass().getName());
		} else {
			Log.debug(LOG_TAG, "Network override disabled, default connection interface restored.");
		}

		ServiceProvider.getInstance().setNetworkService(new NetworkServiceWrapper(newPerformer, defaultNetworkService));
	}

	/**
	 * Wrapper the {@link HTTPConnectionPerformer} into a {@link Networking} implementation class
	 */
	static class NetworkServiceWrapper implements Networking {

		private static final String REQUEST_HEADER_KEY_USER_AGENT = "User-Agent";
		private static final String REQUEST_HEADER_KEY_LANGUAGE = "Accept-Language";

		private final HTTPConnectionPerformer performer;
		private final ExecutorService executorService;
		private final Networking defaultNetworkService;

		NetworkServiceWrapper(final HTTPConnectionPerformer performer, final Networking defaultNetworkService) {
			this.performer = performer;
			this.defaultNetworkService = defaultNetworkService;
			executorService = Executors.newCachedThreadPool();
		}

		@Override
		public void connectAsync(final NetworkRequest request,
								 final NetworkCallback callback) {
			if (performer != null && performer.shouldOverride(request.getUrl(), HTTP_COMMAND_MAP.get(request.getMethod()))) {
				Log.trace(LOG_TAG, "Using network stack override for request to %s.", request.getUrl());

				executorService.submit(new Runnable() {
					@Override
					public void run() {
						final Map<String, String> headers = getDefaultHeaders();

						if (request.getHeaders() != null) {
							headers.putAll(request.getHeaders());
						}

						final HttpConnection connection = performer.connect(request.getUrl(), HTTP_COMMAND_MAP.get(request.getMethod()),
														  request.getBody(), headers,
														  request.getConnectTimeout(), request.getReadTimeout());

						if (callback != null) {
							callback.call(connection);
						}
					}
				});
			} else if (defaultNetworkService != null) {
				defaultNetworkService.connectAsync(request, callback);
			}
		}

		private Map<String, String> getDefaultHeaders() {
			final DeviceInforming deviceInfoService = ServiceProvider.getInstance().getDeviceInfoService();
			final Map<String, String> defaultHeaders = new HashMap<>();

			if (deviceInfoService == null) {
				return defaultHeaders;
			}

			final String userAgent = deviceInfoService.getDefaultUserAgent();

			if (!StringUtils.isNullOrEmpty(userAgent)) {
				defaultHeaders.put(REQUEST_HEADER_KEY_USER_AGENT, userAgent);
			}

			final String locale = deviceInfoService.getLocaleString();

			if (!StringUtils.isNullOrEmpty(locale)) {
				defaultHeaders.put(REQUEST_HEADER_KEY_LANGUAGE, locale);
			}

			return defaultHeaders;
		}

	}
}
