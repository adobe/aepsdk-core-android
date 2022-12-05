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

package com.adobe.marketing.mobile.util;

import com.adobe.marketing.mobile.internal.util.UrlEncoder;
import java.net.MalformedURLException;
import java.net.URL;

public final class UrlUtils {

    private UrlUtils() {}

    /**
     * Check if the given {@code String} is a valid URL.
     *
     * <p>It uses {@link URL} class to identify that.
     *
     * @param stringUrl URL that needs to be tested
     * @return return a {@code boolean} indicating if the given parameter is a valid URL
     */
    public static boolean isValidUrl(final String stringUrl) {
        if (StringUtils.isNullOrEmpty(stringUrl)) {
            return false;
        }

        try {
            new URL(stringUrl);
            return true;
        } catch (MalformedURLException ex) {
            return false;
        }
    }

    /**
     * Encodes an URL given as {@code String}.
     *
     * @param unencodedString nullable {@link String} value to be encoded
     * @return the encoded {@code String}
     */
    public static String urlEncode(final String unencodedString) {
        return UrlEncoder.urlEncode(unencodedString);
    }
}
