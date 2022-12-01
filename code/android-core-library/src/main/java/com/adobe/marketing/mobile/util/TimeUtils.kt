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
    private const val ISO8601_DATE_FORMATTER_TIMEZONE_RFC822 = "yyyy-MM-dd'T'HH:mm:ssZZZ"
    private const val ISO8601_DATE_FORMATTER_TIMEZONE_ISO8601 = "yyyy-MM-dd'T'HH:mm:ssXXX"
    private const val RFC2822_DATE_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z"
    private const val ISO8601_DATE_FORMATTER_TIMEZONE_UTC = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

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
     * Gets the the ISO 8601 formatted date `String` for the current date.
     * TimeZone format used is RFC822 using ZZZ formatting letter. ex: 9th July 2020 PDT will be formatted as 2020-07-09T15:09:18-0700.
     *
     * @return Iso8601 formatted date [String]
     */
    @JvmStatic
    fun getIso8601Date(): String? {
        return getIso8601Date(Date(), ISO8601_DATE_FORMATTER_TIMEZONE_RFC822)
    }

    /**
     * Gets the the ISO 8601 formatted UTC(Z) millisecond precision date `String` for the provided date.
     * Date format used is [ISO8601_FORMAT_TIMEZONE_ISO8601_UTCZ_PRECISION_MILLISECOND]
     * ex: Wed Nov 30 11:53:09.497 GMT-07:00 2022 -> 2022-11-30T18:53:09.497Z
     *
     * Note that ISO 8601 requires date strings terminating with 'Z' to be in the timezone UTC +0 (no offset).
     *
     * AMSDK-10273 :: ExEdge requires time zone offset formatted in form [+-]HH:MM.
     *
     * @param date the [Date] to apply the formatting to; defaults to the current [Date]
     * @return date [String] formatted as ISO 8601 timezone UTC(Z) millisecond precision
     */
    @JvmStatic
    @JvmOverloads
    fun getIso8601DateTimeZoneISO8601(date: Date? = Date()): String? {
        return getIso8601Date(date, ISO8601_DATE_FORMATTER_TIMEZONE_UTC, TimeZone.getTimeZone("Etc/UTC"))
    }

    /**
     * Gets the the ISO 8601 formatted date `String` for the passed in date - "yyyy-MM-dd'T'HH:mm:ssZZZ"
     *
     * @param date the [Date] to generate the [String] for
     * @return ISO 8601 formatted date [String]
     */
    @JvmStatic
    @JvmOverloads
    fun getIso8601Date(date: Date?, format: String?, timeZone: TimeZone? = null): String? {
        // AMSDK-8374 -
        // we should explicitly ignore the device's locale when formatting an ISO 8601 timestamp
        val posixLocale = Locale(Locale.US.language, Locale.US.country, "POSIX")
        val iso8601Format: DateFormat = SimpleDateFormat(format, posixLocale)
        if (timeZone != null) {
            iso8601Format.timeZone = timeZone
        }
        return iso8601Format.format(date ?: Date())
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
     * @param epoch the epoch that should be converted to Date
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
}
