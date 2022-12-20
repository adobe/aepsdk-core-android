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

package com.adobe.marketing.mobile.util

import java.lang.Exception
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object TimeUtils {
    private const val MILLISECONDS_PER_SECOND = 1000L
    private const val ISO8601_TIMEZONE_ISO8601_UTCZ_PRECISION_MILLISECOND = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    private const val ISO8601_TIMEZONE_ISO8601_3X_PRECISION_SECOND = "yyyy-MM-dd'T'HH:mm:ssXXX"
    private const val ISO8601_TIMEZONE_ISO8601_2X_PRECISION_SECOND = "yyyy-MM-dd'T'HH:mm:ssXX"
    private const val ISO8601_FULL_DATE = "yyyy-MM-dd"
    private const val RFC2822_DATE_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z"

    /**
     * Gets current unix timestamp in seconds.
     *
     * @return {code long} current timestamp
     */
    @JvmStatic
    fun getUnixTimeInSeconds(): Long {
        return System.currentTimeMillis() / MILLISECONDS_PER_SECOND
    }

    /**
     * Gets the ISO 8601 formatted with colon time zone, second precision date `String`
     * for the provide date using the system local time zone.
     *
     * Date pattern used is [ISO8601_TIMEZONE_ISO8601_3X_PRECISION_SECOND]
     * which has timezone ISO 8601 'XXX' pattern, which gives a timezone of ex: "-07:00" ("Z" for UTC +0)
     * Ex: (device in time zone "America/Los_Angeles") Wed Nov 30 11:53:09.497 GMT-07:00 2022 -> 2022-11-30T11:53:09-07:00
     *
     * @param date the [Date] to apply the formatting to; defaults to the current [Date]
     * @return date [String] formatted as ISO 8601 with colon time zone, second precision
     */
    @JvmStatic
    @JvmOverloads
    fun getISO8601Date(date: Date = Date()): String {
        return getFormattedDate(date, ISO8601_TIMEZONE_ISO8601_3X_PRECISION_SECOND) ?: ""
    }

    /**
     * Gets the ISO 8601 formatted with no colon time zone, second precision date `String`
     * for the provide date using the system local time zone.
     *
     * Date pattern used is [ISO8601_TIMEZONE_ISO8601_2X_PRECISION_SECOND]
     * which has timezone ISO 8601 'XX' pattern, which gives a timezone of ex: "-0700" ("Z" for UTC +0)
     * Ex: (device in time zone "America/Los_Angeles") Wed Nov 30 11:53:09.497 GMT-07:00 2022 -> 2022-11-30T11:53:09-0700
     *
     * @param date the [Date] to apply the formatting to; defaults to the current [Date]
     * @return date [String] formatted as ISO 8601 with no colon time zone, second precision
     */
    @JvmStatic
    @JvmOverloads
    fun getISO8601DateNoColon(date: Date = Date()): String {
        return getFormattedDate(date, ISO8601_TIMEZONE_ISO8601_2X_PRECISION_SECOND) ?: ""
    }

    /**
     * Gets the ISO 8601 formatted with UTC(Z) time zone, millisecond precision date `String` for the
     * provided date using the UTC +0 time zone.
     *
     * Date pattern used is [ISO8601_TIMEZONE_ISO8601_UTCZ_PRECISION_MILLISECOND]
     * which has timezone ISO 8601 'Z' char terminator, which gives a timezone of 'Z'.
     * Ex: Wed Nov 30 11:53:09.497 GMT-07:00 2022 -> 2022-11-30T18:53:09.497Z (notice the hour shift because the date must be evaluated from UTC +0)
     *
     * Note that ISO 8601 requires date strings terminating with the char 'Z' to be in the time zone UTC +0 (no offset).
     *
     * AMSDK-10273 :: ExEdge requires time zone offset formatted in form [+-]HH:MM.
     *
     * @param date the [Date] to apply the formatting to; defaults to the current [Date]
     * @return date [String] formatted as ISO 8601 with UTC(Z) time zone, millisecond precision
     */
    @JvmStatic
    @JvmOverloads
    fun getISO8601UTCDateWithMilliseconds(date: Date = Date()): String {
        return getFormattedDate(date, ISO8601_TIMEZONE_ISO8601_UTCZ_PRECISION_MILLISECOND, TimeZone.getTimeZone("GMT")) ?: ""
    }

    /**
     * Gets the ISO 8601 formatted full date `String` for the provided date using the system local time zone.
     *
     * Date pattern used is [ISO8601_FULL_DATE]
     * Ex: Wed Nov 30 11:53:09.497 GMT-07:00 2022 -> 2022-11-30
     *
     * @param date the [Date] to apply the formatting to; defaults to the current [Date]
     * @return date [String] formatted as ISO 8601 full date, using system local time zone
     */
    @JvmStatic
    @JvmOverloads
    fun getISO8601FullDate(date: Date = Date()): String {
        return getFormattedDate(date, ISO8601_FULL_DATE) ?: ""
    }

    /**
     * Parses the RFC-2822 formatted date string [rfc2822Date] into a [Date]
     * @param rfc2822Date RFC-2822 formatted date string to be parsed
     * @param timeZone the timezone that should be used
     * @param locale the locale whose date format symbols should be used
     * @return a valid date from the RF-2822 date if successful, null otherwise
     */
    @JvmStatic
    fun parseRFC2822Date(rfc2822Date: String?, timeZone: TimeZone, locale: Locale): Date? {
        if (rfc2822Date == null) return null
        val rfc2822formatter: DateFormat = SimpleDateFormat(RFC2822_DATE_PATTERN, locale)
        rfc2822formatter.timeZone = timeZone
        return try {
            rfc2822formatter.parse(rfc2822Date) ?: Date()
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Converts the epoch into a RFC-2822 date string pattern - [RFC2822_DATE_PATTERN]
     * @param epoch the epoch (milliseconds) that should be converted to [Date]
     * @param timeZone the timezone that should be used
     * @param locale the locale whose date format symbols should be used
     * @return a RFC-2822 formatted date string for the epoch provided
     */
    @JvmStatic
    fun getRFC2822Date(epoch: Long, timeZone: TimeZone, locale: Locale): String {
        val rfc2822formatter: DateFormat = SimpleDateFormat(RFC2822_DATE_PATTERN, locale)
        rfc2822formatter.timeZone = timeZone
        return rfc2822formatter.format(epoch)
    }

    /**
     * Gets the formatted date `String` for the passed in date; if no date passed in, uses current `Date()`
     *
     * @param date the [Date] to apply the formatting to
     * @param pattern the pattern [String] to use to format the date
     * @param timeZone the [TimeZone] to evaluate the formatted date from; defaults to device time zone if not specified
     * @return the formatted date [String], null if formatting fails
     */
    private fun getFormattedDate(date: Date, pattern: String, timeZone: TimeZone? = null): String? {
        // AMSDK-8374 -
        // we should explicitly ignore the device's locale when formatting an ISO 8601 timestamp
        val posixLocale = Locale(Locale.US.language, Locale.US.country, "POSIX")
        val dateFormat: DateFormat = SimpleDateFormat(pattern, posixLocale)

        if (timeZone != null) {
            dateFormat.timeZone = timeZone
        }
        return dateFormat.format(date)
    }
}
