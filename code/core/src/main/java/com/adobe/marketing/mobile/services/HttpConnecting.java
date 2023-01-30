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

import java.io.InputStream;

// The HttpConnecting represents the response to NetworkRequest, to be used for network completion
// handlers and when overriding the network stack in place of internal network connection
// implementation.
public interface HttpConnecting {
    /**
     * Returns an input stream from the connection to read the application server response, if
     * available.
     *
     * @return {@link InputStream} connection response input stream
     */
    InputStream getInputStream();

    /**
     * Returns an input stream from the connection to read the application server error response, if
     * available.
     *
     * @return {@link InputStream} connection response error stream
     */
    InputStream getErrorStream();

    /**
     * Returns the connection attempt response code for the connection request.
     *
     * @return {@code int} indicating connection response code
     */
    int getResponseCode();

    /**
     * Returns a connection attempt response message, if available.
     *
     * @return {@link String} containing connection response message
     */
    String getResponseMessage();

    /**
     * Returns a value for the response property key that might have been set when a connection was
     * made to the resource pointed to by the Url.
     *
     * <p>This is protocol specific. For example, HTTP urls could have properties like
     * "last-modified", or "ETag" set.
     *
     * @param responsePropertyKey {@link String} containing response property key
     * @return {@code String} corresponding to the response property value for the key specified, or
     *     null, if the key does not exist.
     */
    String getResponsePropertyValue(final String responsePropertyKey);

    /** Close this connection. */
    void close();
}
