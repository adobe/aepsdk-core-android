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

import android.os.Build;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.net.ssl.HttpsURLConnection;

class HttpConnectionHandler {

    private static final String TAG = HttpConnectionHandler.class.getSimpleName();

    protected final HttpsURLConnection httpsUrlConnection;
    protected Command command = Command.GET; // Default

    /** Commands supported by this {@code HttpConnectionHandler}. */
    protected enum Command {
        GET(false),
        POST(true);

        private boolean doOutputSetting;

        /**
         * Constructor which initializes the {@link #doOutputSetting}.
         *
         * <p>Set the {@code doOutputSetting} flag to true if you intend to write data to the URL
         * connection, otherwise set it to false.
         *
         * @param doOutputSetting {@code boolean} indicating whether this command writes to the
         *     connection output stream
         */
        Command(final boolean doOutputSetting) {
            this.doOutputSetting = doOutputSetting;
        }

        /**
         * Returns the setting specifying whether this command will need to write data to the URL
         * connection.
         *
         * @return {@code boolean} indicating whether this command writes to the connection output
         *     stream
         */
        public boolean isDoOutput() {
            return doOutputSetting;
        }
    }

    /**
     * Constructor to create a connection to the resource indicated by the specified {@code url}.
     *
     * <p>Initializes {@link #httpsUrlConnection} with a {@link java.net.URLConnection} instance
     * representing the connection, returned by a call to {@link URL#openConnection()}.
     *
     * @param url {@link URL} to connect to
     * @throws IOException if an exception occurs
     */
    HttpConnectionHandler(final URL url) throws IOException {
        httpsUrlConnection = (HttpsURLConnection) url.openConnection();
        final int buildVersion = Build.VERSION.SDK_INT;

        // https://developer.android.com/reference/javax/net/ssl/SSLSocket#default-configuration-for-different-android-versions
        if (buildVersion < Build.VERSION_CODES.KITKAT_WATCH) { // 20
            httpsUrlConnection.setSSLSocketFactory(TLSSocketFactory.getInstance());
        }
    }

    /**
     * Sets the command to be used for this connection attempt.
     *
     * <p>The command should be set before {@link #connect(byte[])} method is called.
     *
     * @param command {@link HttpMethod} representing the command to be used
     * @return {@code boolean} indicating whether the command was successfully set
     * @see HttpsURLConnection#setRequestMethod(String)
     */
    boolean setCommand(final HttpMethod command) {
        if (command == null) {
            return false;
        }

        try {
            Command requestedCommand = Command.valueOf(command.name());

            // Set the HTTP method for this request. Supported methods - GET/POST.
            httpsUrlConnection.setRequestMethod(requestedCommand.name());

            // Set doOutput flag for this URLConnection. A true value indicates intention to write
            // data to URL connection, read otherwise.
            httpsUrlConnection.setDoOutput(requestedCommand.isDoOutput());

            // AMSDK-8629, avoid a crash inside Android code
            httpsUrlConnection.setUseCaches(false);

            this.command = requestedCommand;
            return true;
        } catch (final ProtocolException e) {
            Log.warning(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    String.format("%s is not a valid HTTP command (%s)!", command.toString(), e));
        } catch (final IllegalStateException e) {
            Log.warning(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    String.format("Cannot set command after connect (%s)!", e));
        } catch (final IllegalArgumentException e) {
            Log.warning(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    String.format("%s command is not supported (%s)!", command.toString(), e));
        } catch (final Exception e) {
            Log.warning(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    String.format("Failed to set http command (%s)!", e));
        } catch (final Error e) {
            Log.warning(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    String.format("Failed to set http command (%s)!", e));
        }

        return false;
    }

    /**
     * Sets the header fields specified by the {@code requestProperty} for the connection.
     *
     * <p>This method should be called before {@link #connect(byte[])} is called.
     *
     * @param requestProperty {@code Map<String, String>} containing the header fields and their
     *     values
     * @see HttpsURLConnection#setRequestProperty(String, String)
     */
    void setRequestProperty(final Map<String, String> requestProperty) {
        if (requestProperty == null || requestProperty.isEmpty()) {
            return;
        }

        Set<Map.Entry<String, String>> entries = requestProperty.entrySet();
        Iterator<Map.Entry<String, String>> it = entries.iterator();

        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();

            try {
                httpsUrlConnection.setRequestProperty(entry.getKey(), entry.getValue());
            } catch (final IllegalStateException e) {
                Log.warning(
                        ServiceConstants.LOG_TAG,
                        TAG,
                        String.format("Cannot set header field after connect (%s)!", e));
                return;
            } catch (final Exception e) {
                Log.warning(
                        ServiceConstants.LOG_TAG,
                        TAG,
                        String.format("Failed to set request property (%s)!", e));
            } catch (final Error e) {
                Log.warning(
                        ServiceConstants.LOG_TAG,
                        TAG,
                        String.format("Failed to set request property (%s)!", e));
            }
        }
    }

    /**
     * Sets the connect timeout value for this connection.
     *
     * @param connectTimeout {@code int} indicating connect timeout value in milliseconds
     * @see HttpURLConnection#setConnectTimeout(int)
     */
    void setConnectTimeout(final int connectTimeout) {
        try {
            httpsUrlConnection.setConnectTimeout(connectTimeout);
        } catch (final IllegalArgumentException e) {
            Log.warning(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    String.format(connectTimeout + " is not valid timeout value (%s)", e));
        } catch (final Exception e) {
            Log.warning(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    String.format("Failed to set connection timeout (%s)!", e));
        } catch (final Error e) {
            Log.warning(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    String.format("Failed to set connection timeout (%s)!", e));
        }
    }

    /**
     * Sets the timeout that will be used to wait for a read to finish after a successful connect.
     *
     * @param readTimeout {@code int} indicating read timeout value in milliseconds
     * @see HttpURLConnection#setReadTimeout(int)
     */
    void setReadTimeout(final int readTimeout) {
        try {
            httpsUrlConnection.setReadTimeout(readTimeout);
        } catch (final IllegalArgumentException e) {
            Log.warning(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    String.format(readTimeout + " is not valid timeout value (%s)", e));
        } catch (final Exception e) {
            Log.warning(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    String.format("Failed to set read timeout (%s)!", e));
        } catch (final Error e) {
            Log.warning(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    String.format("Failed to set read timeout (%s)!", e));
        }
    }

    /**
     * Performs the actual connection to the resource referenced by this {@code httpUrlConnection}.
     *
     * <p>If the {@code command} set for this connection is {@link Command#POST}, then the {@code
     * payload} will be sent to the server, otherwise ignored.
     *
     * @param payload {@code byte} array representing the payload to be sent to the server
     * @return {@link HttpConnecting} instance, representing a connection attempt
     * @see HttpURLConnection#connect()
     */
    HttpConnecting connect(final byte[] payload) {
        Log.debug(
                ServiceConstants.LOG_TAG,
                TAG,
                String.format(
                        "Connecting to URL %s (%s)",
                        (httpsUrlConnection.getURL() == null
                                ? ""
                                : httpsUrlConnection.getURL().toString()),
                        command.toString()));

        // If the command to be used is POST, set the length before connection
        if (command == Command.POST && payload != null) {
            httpsUrlConnection.setFixedLengthStreamingMode(payload.length);
        }

        // Try to connect
        try {
            httpsUrlConnection.connect();

            // if the command is POST, send the data to the URL.
            if (command == Command.POST && payload != null) {
                // Consume the payload
                OutputStream os = new BufferedOutputStream(httpsUrlConnection.getOutputStream());
                os.write(payload);
                os.flush();
                os.close();
            }
        } catch (final SocketTimeoutException e) {
            Log.warning(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    String.format("Connection failure, socket timeout (%s)", e));
        } catch (final IOException e) {
            Log.warning(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    String.format(
                            "Connection failure (%s)",
                            (e.getLocalizedMessage() != null
                                    ? e.getLocalizedMessage()
                                    : e.getMessage())));
        } catch (final Exception e) {
            Log.warning(ServiceConstants.LOG_TAG, TAG, String.format("Connection failure (%s)", e));
        } catch (final Error e) {
            Log.warning(ServiceConstants.LOG_TAG, TAG, String.format("Connection failure (%s)", e));
        }

        // Create a connection object here
        // Even if there might be an IOException, let the user query for response code etc.
        return new HttpConnection(httpsUrlConnection);
    }
}
