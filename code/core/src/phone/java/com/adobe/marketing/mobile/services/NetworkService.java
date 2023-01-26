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

package com.adobe.marketing.mobile.services;

import androidx.annotation.VisibleForTesting;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/** Implementation of {@link Networking} service */
class NetworkService implements Networking {

    private static final String TAG = NetworkService.class.getSimpleName();
    private static final String REQUEST_HEADER_KEY_USER_AGENT = "User-Agent";
    private static final String REQUEST_HEADER_KEY_LANGUAGE = "Accept-Language";
    private static final int THREAD_POOL_CORE_SIZE = 0;
    private static final int THREAD_POOL_MAXIMUM_SIZE = 32;
    private static final int THREAD_POOL_KEEP_ALIVE_TIME = 60;
    private static final int SEC_TO_MS_MULTIPLIER = 1000;
    private final ExecutorService executorService;

    NetworkService() {
        // define THREAD_POOL_MAXIMUM_SIZE instead of using a unbounded thread pool, mainly to
        // prevent a wrong usage from extensions
        // to blow off the Android system.
        executorService =
                new ThreadPoolExecutor(
                        THREAD_POOL_CORE_SIZE,
                        THREAD_POOL_MAXIMUM_SIZE,
                        THREAD_POOL_KEEP_ALIVE_TIME,
                        TimeUnit.SECONDS,
                        new SynchronousQueue<Runnable>());
    }

    @VisibleForTesting
    NetworkService(final ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void connectAsync(final NetworkRequest request, final NetworkCallback callback) {
        try {
            executorService.submit(
                    new Runnable() {
                        @Override
                        public void run() {
                            HttpConnecting connection = doConnection(request);

                            if (callback != null) {
                                callback.call(connection);
                            } else {
                                // If no callback is passed by the client, close the connection.
                                if (connection != null) {
                                    connection.close();
                                }
                            }
                        }
                    });
        } catch (final Exception e) {
            // to catch RejectedExecutionException when the thread pool is saturated
            Log.warning(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    String.format(
                            "Failed to send request for (%s) [%s]",
                            request.getUrl(),
                            (e.getLocalizedMessage() != null
                                    ? e.getLocalizedMessage()
                                    : e.getMessage())));

            if (callback != null) {
                callback.call(null);
            }
        }
    }

    /**
     * Performs the actual connection to the specified {@code url}.
     *
     * <p>It sets the default connection headers if none were provided through the {@code
     * requestProperty} parameter. You can override the default user agent and language headers if
     * they are present in {@code requestProperty}
     *
     * <p>This method will return null, if failed to establish connection to the resource.
     *
     * @param request {@link NetworkRequest} used for connection
     * @return {@link HttpConnecting} instance, representing a connection attempt
     */
    private HttpConnecting doConnection(final NetworkRequest request) {
        HttpConnecting connection = null;

        if (request.getUrl() == null || !request.getUrl().contains("https")) {
            Log.warning(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    String.format(
                            "Invalid URL (%s), only HTTPS protocol is supported",
                            request.getUrl()));
            return null;
        }

        final Map<String, String> headers = getDefaultHeaders();

        if (request.getHeaders() != null) {
            headers.putAll(request.getHeaders());
        }

        try {
            final URL serverUrl = new URL(request.getUrl());
            final String protocol = serverUrl.getProtocol();

            /*
             * Only https is supported as of now.
             * No special handling for https is supported for now.
             */
            if (protocol != null && "https".equalsIgnoreCase(protocol)) {
                try {
                    final HttpConnectionHandler httpConnectionHandler =
                            new HttpConnectionHandler(serverUrl);

                    if (httpConnectionHandler.setCommand(request.getMethod())) {
                        httpConnectionHandler.setRequestProperty(headers);
                        httpConnectionHandler.setConnectTimeout(
                                request.getConnectTimeout() * SEC_TO_MS_MULTIPLIER);
                        httpConnectionHandler.setReadTimeout(
                                request.getReadTimeout() * SEC_TO_MS_MULTIPLIER);
                        connection = httpConnectionHandler.connect(request.getBody());
                    }
                } catch (final IOException e) {
                    Log.warning(
                            ServiceConstants.LOG_TAG,
                            TAG,
                            String.format(
                                    "Could not create a connection to URL (%s) [%s]",
                                    request.getUrl(),
                                    (e.getLocalizedMessage() != null
                                            ? e.getLocalizedMessage()
                                            : e.getMessage())));
                } catch (final SecurityException e) {
                    Log.warning(
                            ServiceConstants.LOG_TAG,
                            TAG,
                            String.format(
                                    "Could not create a connection to URL (%s) [%s]",
                                    request.getUrl(),
                                    (e.getLocalizedMessage() != null
                                            ? e.getLocalizedMessage()
                                            : e.getMessage())));
                }
            }
        } catch (final MalformedURLException e) {
            Log.warning(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    String.format(
                            "Could not connect, invalid URL (%s) [%s]!!", request.getUrl(), e));
        }

        return connection;
    }

    /**
     * Creates a {@code Map<String, String>} with the default headers: default user agent and active
     * language.
     *
     * <p>This method is used to retrieve the default headers to be appended to any network
     * connection made by the SDK.
     *
     * @return {@code Map<String, String>} containing the default user agent and active language if
     *     {@code #DeviceInforming} is not null or an empty Map otherwise
     * @see DeviceInforming#getDefaultUserAgent()
     * @see DeviceInforming#getLocaleString()
     */
    private Map<String, String> getDefaultHeaders() {
        final Map<String, String> defaultHeaders = new HashMap<>();
        final DeviceInforming deviceInfoService =
                ServiceProvider.getInstance().getDeviceInfoService();

        if (deviceInfoService == null) {
            return defaultHeaders;
        }

        String userAgent = deviceInfoService.getDefaultUserAgent();

        if (!isNullOrEmpty(userAgent)) {
            defaultHeaders.put(REQUEST_HEADER_KEY_USER_AGENT, userAgent);
        }

        String locale = deviceInfoService.getLocaleString();

        if (!isNullOrEmpty(locale)) {
            defaultHeaders.put(REQUEST_HEADER_KEY_LANGUAGE, locale);
        }

        return defaultHeaders;
    }

    private boolean isNullOrEmpty(final String str) {
        return str == null || str.trim().isEmpty();
    }
}
