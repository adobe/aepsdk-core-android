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

package com.adobe.marketing.mobile.lifecycle;

import android.annotation.SuppressLint;
import android.os.Build;
import androidx.annotation.VisibleForTesting;
import com.adobe.marketing.mobile.util.StringUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

final class LifecycleUtil {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    private LifecycleUtil() {}

    /**
     * Formats a {@code Date} to an ISO 8601 date-time string in UTC as defined in <a
     * href="https://tools.ietf.org/html/rfc3339#section-5.6">RFC 3339, section 5.6</a> For example,
     * 2017-09-26T15:52:25Z
     *
     * @param timestamp a timestamp
     * @return {@code timestamp} formatted to a string in the format of 'yyyy-MM-dd'T'HH:mm:ss'Z'',
     *     or an empty string if {@code timestamp} is null
     */
    static String dateTimeISO8601String(final Date timestamp) {
        return dateToISO8601String(timestamp, DATE_TIME_FORMAT);
    }

    /**
     * Formats a {@code Date} with the provided {@code timestampFormat}
     *
     * @param timestamp a timestamp
     * @param timestampFormat the format in which to format the date or the default {@link
     *     #DATE_TIME_FORMAT} if this parameter is null or empty
     * @return {@code timestamp} formatted to a string
     */
    private static String dateToISO8601String(final Date timestamp, final String timestampFormat) {
        if (timestamp == null) {
            return "";
        }

        final String timePattern =
                StringUtils.isNullOrEmpty(timestampFormat) ? DATE_TIME_FORMAT : timestampFormat;
        final Locale posixLocale =
                new Locale(Locale.US.getLanguage(), Locale.US.getCountry(), "POSIX");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(timePattern, posixLocale);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return simpleDateFormat.format(timestamp);
    }

    /**
     * Formats the locale value by replacing '_' with '-'. Uses {@link Locale#toString()} to
     * retrieve the language tag.
     *
     * <p>Note. the use of {@code Locale#toString()} does not return a value formatted to BCP 47.
     * For example, script codes are appended to the locale as "-#scriptCode", as in "zh-HK-#Hant",
     * where BCP 47 requires the format as "zh-Hant-HK".
     *
     * @see #formatLocaleXDM(Locale)
     * @param locale active locale value
     * @return string representation of the locale
     */
    static String formatLocale(final Locale locale) {
        return locale == null ? null : locale.toString().replace('_', '-');
    }

    /**
     * Format the locale to the string format used in XDM. For Android API version >= Lollipop (21),
     * returns {@link Locale#toLanguageTag()}. For Android API version < 21, returns a concatenation
     * of {@link Locale#getLanguage()} and {@link Locale#getCountry()}, separated by '-'.
     *
     * @param locale active Locale value
     * @return String representation of the locale
     */
    @SuppressLint("NewApi")
    static String formatLocaleXDM(final Locale locale) {
        if (locale == null) {
            return null;
        }

        if (isLollipopOrGreater.check()) {
            return locale.toLanguageTag();
        }

        String language = locale.getLanguage();
        String region = locale.getCountry();

        if (StringUtils.isNullOrEmpty(language)) {
            return null;
        }

        return StringUtils.isNullOrEmpty(region)
                ? language
                : String.format("%s-%s", language, region);
    }

    interface BuildVersionCheck {
        boolean check();
    }

    @VisibleForTesting
    static BuildVersionCheck isLollipopOrGreater =
            () -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
}
