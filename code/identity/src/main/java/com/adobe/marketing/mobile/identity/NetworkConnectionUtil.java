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

package com.adobe.marketing.mobile.identity;

import com.adobe.marketing.mobile.util.StringUtils;
import java.net.HttpURLConnection;
import java.util.*;

final class NetworkConnectionUtil {

    static final String HTTP_HEADER_KEY_CONTENT_TYPE = "Content-Type";
    static final String HTTP_HEADER_CONTENT_TYPE_WWW_FORM_URLENCODED =
            "application/x-www-form-urlencoded";

    private NetworkConnectionUtil() {}

    static ArrayList<Integer> recoverableNetworkErrorCodes =
            new ArrayList<Integer>(
                    Arrays.asList(
                            HttpURLConnection.HTTP_CLIENT_TIMEOUT,
                            HttpURLConnection.HTTP_GATEWAY_TIMEOUT,
                            HttpURLConnection.HTTP_UNAVAILABLE));

    /**
     * Returns the default headers for connection: Content-Type and connection
     *
     * @param ssl {@code boolean} indicating if https is enabled
     * @return {@code Map<String, String>} with the specified keys and values
     */
    static Map<String, String> getHeaders(final boolean ssl) {
        return getHeaders(ssl, null);
    }

    /**
     * Returns the default headers for connection: Content-Type and connection
     *
     * @param ssl {@code boolean} indicating if https is enabled
     * @param contentType the custom content type {@code String}
     * @return {@code Map<String, String>} with the specified keys and values
     */
    static Map<String, String> getHeaders(final boolean ssl, final String contentType) {
        final Map<String, String> headers = new HashMap<String, String>();

        if (!ssl) {
            headers.put("connection", "close");
        }

        headers.put(
                HTTP_HEADER_KEY_CONTENT_TYPE,
                StringUtils.isNullOrEmpty(contentType)
                        ? HTTP_HEADER_CONTENT_TYPE_WWW_FORM_URLENCODED
                        : contentType);
        return headers;
    }
}
