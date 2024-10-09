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

import com.adobe.marketing.mobile.util.StringUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.UnknownServiceException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class HttpConnection implements HttpConnecting {

    private static final String TAG = HttpConnection.class.getSimpleName();
    private final HttpURLConnection httpUrlConnection;

    /**
     * Constructor
     *
     * @param httpURLConnection {@link HttpURLConnection} instance, supports HTTP specific features
     */
    HttpConnection(final HttpURLConnection httpURLConnection) {
        this.httpUrlConnection = httpURLConnection;
    }

    /**
     * Returns an input stream to read the application server response from this open connection, if
     * available.
     *
     * <p>This method invokes {@link HttpURLConnection#getInputStream()} and returns null if {@code
     * getInputSream()} throws an exception.
     *
     * @return {@link InputStream} connection response input stream
     */
    @Override
    public InputStream getInputStream() {
        try {
            return httpUrlConnection.getInputStream();
        } catch (final UnknownServiceException e) {
            Log.warning(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    String.format(
                            "Could not get the input stream, protocol does not support input. (%s)",
                            e));
        } catch (final Exception | Error e) {
            Log.warning(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    String.format("Could not get the input stream. (%s)", e));
        }

        return null;
    }

    /**
     * Returns an input stream from the connection to read the application server error response, if
     * available.
     *
     * @return {@link InputStream} connection response error stream
     */
    @Override
    public InputStream getErrorStream() {
        try {
            return httpUrlConnection.getErrorStream();
        } catch (final Exception | Error e) {
            Log.warning(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    String.format("Could not get the input stream. (%s)", e));
        }

        return null;
    }

    /**
     * Returns the connection attempt response code for this connection request.
     *
     * <p>This method invokes {@link HttpURLConnection#getResponseCode()} and returns -1 if {@code
     * getResponseCode()} throws an exception or the response is not valid HTTP.
     *
     * @return {@code int} indicating connection status code
     */
    @Override
    public int getResponseCode() {
        try {
            return httpUrlConnection.getResponseCode();
        } catch (final Exception | Error e) {
            Log.warning(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    String.format("Could not get response code. (%s)", e));
        }

        return -1;
    }

    /**
     * Returns the connection attempt response message for this connection request, if available.
     *
     * <p>This method invokes {@link HttpURLConnection#getResponseMessage()} and returns null if
     * {@code getResponseMessage()} throws an exception or the result is not valid HTTP.
     *
     * @return {@link String} containing connection response message
     */
    @Override
    public String getResponseMessage() {
        try {
            return httpUrlConnection.getResponseMessage();
        } catch (final Exception | Error e) {
            Log.warning(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    String.format("Could not get the response message. (%s)", e));
        }

        return null;
    }

    /**
     * Returns the value of the header field specified by the {@code responsePropertyKey} that might
     * have been set when a connection was made to the resource pointed to by the URL.
     *
     * <p>This is protocol specific. For example, HTTP urls could have properties like
     * "last-modified", or "ETag" set.
     *
     * In alignment with OS behavior for responses where there are duplicate entries in the header,
     * calling this method produces a non-deterministic result. Per HTTP RFC, header field name
     * look ups should be case-insensitive.
     *
     * @param responsePropertyKey {@link String} containing response property key
     * @return {@code String} corresponding to the response property value for the key specified, or
     *     null, if the key does not exist
     */
    @Override
    public String getResponsePropertyValue(final String responsePropertyKey) {
        if (StringUtils.isNullOrEmpty(responsePropertyKey)) {
            return null;
        }

        return getLowercaseHeaders().get(responsePropertyKey.toLowerCase());
    }

    /**
     * Converts header keys to lowercase.
     *
     * @return a map of headers with all lowercase keys.
     */
    private Map<String, String> getLowercaseHeaders() {
        final Map<String, String> lowercaseHeaders = new HashMap<>();
        final Map<String, List<String>> originalHeaders = httpUrlConnection.getHeaderFields();

        for (final String header : originalHeaders.keySet()) {
            final List<String> valuesForHeader = originalHeaders.get(header);
            if (valuesForHeader != null && !valuesForHeader.isEmpty()) {
                lowercaseHeaders.put(header.toLowerCase(), valuesForHeader.get(0));
            }
        }

        return lowercaseHeaders;
    }

    /**
     * Closes this open connection.
     *
     * <p>Invokes {@link HttpURLConnection#disconnect()} method to release the resources for this
     * connection.
     */
    @Override
    public void close() {
        final InputStream inputStream = this.getInputStream();

        final InputStream errorStream = this.getErrorStream();
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (final Exception | Error e) {
                Log.warning(
                        ServiceConstants.LOG_TAG,
                        TAG,
                        String.format("Could not close the input stream. (%s)", e));
            }
        }
        if (errorStream != null) {
            try {
                errorStream.close();
            } catch (final Exception | Error e) {
                Log.warning(
                        ServiceConstants.LOG_TAG,
                        TAG,
                        String.format("Could not close the error stream. (%s)", e));
            }
        }

        httpUrlConnection.disconnect();
    }
}
