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

package com.adobe.marketing.mobile.internal.utility

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal object TimeUtil {
    private const val MILLISECONDS_PER_SECOND = 1000L
    private const val ISO8601_DATE_FORMATTER_TIMEZONE_RFC822 = "yyyy-MM-dd'T'HH:mm:ssZZZ"
    private const val ISO8601_DATE_FORMATTER_TIMEZONE_ISO8601 = "yyyy-MM-dd'T'HH:mm:ssXXX"

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
     * Gets the the ISO 8601 formatted date `String` for the current date.
     * TimeZone format used is ISO8601 using XXX formatting letters. ex: 9th July 2020 PDT will be formatted as 2020-07-09T15:09:18-07:00.
     * AMSDK-10273 :: ExEdge requires time zone offset formatted in form [+-]HH:MM.
     *
     * @return Iso8601 formatted date [String]
     */
    @JvmStatic
    fun getIso8601DateTimeZoneISO8601(): String? {
        return getIso8601Date(Date(), ISO8601_DATE_FORMATTER_TIMEZONE_ISO8601)
    }

    /**
     * Gets the the ISO 8601 formatted date `String` for the passed in date - "yyyy-MM-dd'T'HH:mm:ssZZZ"
     *
     * @param date the [Date] to generate the [String] for
     * @return ISO 8601 formatted date [String]
     */
    @JvmStatic
    fun getIso8601Date(date: Date?, format: String?): String? {
        // AMSDK-8374 -
        // we should explicitly ignore the device's locale when formatting an ISO 8601 timestamp
        val posixLocale = Locale(Locale.US.language, Locale.US.country, "POSIX")
        val iso8601Format: DateFormat = SimpleDateFormat(format, posixLocale)
        return iso8601Format.format(date ?: Date())
    }
}
