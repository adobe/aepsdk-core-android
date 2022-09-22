package com.adobe.marketing.mobile.util;

import java.net.MalformedURLException;
import java.net.URL;
import com.adobe.marketing.mobile.internal.util.UrlEncoder;

public final class UrlUtils {

    private UrlUtils() {}

    /**
     * Check if the given {@code String} is a valid URL.
     * <p>
     * It uses {@link URL} class to identify that.
     *
     * @param stringUrl URL that needs to be tested
     * @return return a {@code boolean} indicating if the given parameter is a valid URL
     */
    public static boolean stringIsUrl(final String stringUrl) {
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
    public static String urlEncode(String unencodedString) {
        return UrlEncoder.urlEncode(unencodedString);
    }
}
