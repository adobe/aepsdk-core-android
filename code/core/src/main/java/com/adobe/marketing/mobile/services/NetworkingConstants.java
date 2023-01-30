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

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;

public class NetworkingConstants {

    private NetworkingConstants() {}

    public class Headers {

        private Headers() {}

        public static final String IF_MODIFIED_SINCE = "If-Modified-Since";
        public static final String IF_NONE_MATCH = "If-None-Match";
        public static final String LAST_MODIFIED = "Last-Modified";
        public static final String ETAG = "Etag";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String ACCEPT_LANGUAGE = "Accept-Language";
        public static final String ACCEPT = "Accept";
    }

    public class HeaderValues {

        private HeaderValues() {}

        public static final String CONTENT_TYPE_JSON_APPLICATION = "application/json";
        public static final String CONTENT_TYPE_URL_ENCODED = "application/x-www-form-urlencoded";
        public static final String ACCEPT_TEXT_HTML = "text/html";
    }

    public static ArrayList<Integer> RECOVERABLE_ERROR_CODES =
            new ArrayList<Integer>(
                    Arrays.asList(
                            HttpURLConnection.HTTP_CLIENT_TIMEOUT,
                            HttpURLConnection.HTTP_GATEWAY_TIMEOUT,
                            HttpURLConnection.HTTP_UNAVAILABLE));
}
