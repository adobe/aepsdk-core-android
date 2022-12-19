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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class TimeUtilsTests {
    companion object {
        private const val RFC2822_DATE_GMT: String = "Mon, 19 Dec 2022 16:56:23 GMT"
        private const val RFC2822_DATE_PST: String = "Mon, 19 Dec 2022 08:56:23 PST"
        private const val TEST_EPOCH = 1671468983756L
    }

    @Test
    fun testGetRFC2822Date_localeUS() {
        val rfc2822Date: String = TimeUtils.getRFC2822Date(TEST_EPOCH, TimeZone.getTimeZone("GMT"), Locale.US)
        assertEquals(RFC2822_DATE_GMT, rfc2822Date)
    }

    @Test
    fun testGetRFC2822Date_localePST() {
        val rfc2822Date: String = TimeUtils.getRFC2822Date(TEST_EPOCH, TimeZone.getTimeZone("PST"), Locale.US)
        assertEquals(RFC2822_DATE_PST, rfc2822Date)
    }

    @Test
    fun testParseRFC2822Date_localeGMT() {
        val rfc2822Date: Date? = TimeUtils.parseRFC2822Date(RFC2822_DATE_GMT, TimeZone.getTimeZone("GMT"), Locale.US)
        assertEquals(Date(TEST_EPOCH).toInstant().epochSecond, rfc2822Date?.toInstant()?.epochSecond)
    }

    @Test
    fun testParseRFC2822Date_localePST() {
        val rfc2822Date: Date? = TimeUtils.parseRFC2822Date(RFC2822_DATE_PST, TimeZone.getTimeZone("PST"), Locale.US)
        assertEquals(Date(TEST_EPOCH).toInstant().epochSecond, rfc2822Date?.toInstant()?.epochSecond)
    }

    @Test
    fun testParseRFC2822Date_nullRFC2822Date() {
        val rfc2822Date: Date? = TimeUtils.parseRFC2822Date(null, TimeZone.getTimeZone("PST"), Locale.US)
        assertNull(rfc2822Date)
    }
}
