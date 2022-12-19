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

package com.adobe.marketing.mobile.internal.util

import java.lang.Exception
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Internal utility class for components requiring date time manipulations.
 * This needs to be public because of it being accessed in other internal java classes from a different package.
 */
object RFC2822DateUtil {
    private const val RFC2822_DATE_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z"

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
}
