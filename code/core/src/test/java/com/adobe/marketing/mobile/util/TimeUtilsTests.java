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

import static org.junit.Assert.*;

import com.adobe.marketing.mobile.TestHelper;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;

public class TimeUtilsTests {
    private static final String DATE_REGEX_ISO8601_TIMEZONE_ISO8601_2X_PRECISION_SECOND =
            "^((19|2[0-9])[0-9]{2})-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])T[0-2][0-9]:[0-5][0-9]:[0-5][0-9][-+][0-9]{4}$";

    private static final String DATE_REGEX_ISO8601_TIMEZONE_ISO8601_3X_PRECISION_SECOND =
            "^((19|2[0-9])[0-9]{2})-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])T[0-2][0-9]:[0-5][0-9]:[0-5][0-9][-+][0-9]{2}:[0-9]{2}$";

    private static final String DATE_REGEX_ISO8601_FULL_DATE = "^[0-9]{4}-[0-9]{2}-[0-9]{2}$";

    private static final String DATE_REGEX_ISO8601_TIMEZONE_ISO8601_UTCZ_PRECISION_MILLISECOND =
            "^[0-9]{4}-[0-9]{2}-[0-9]{2}T([0-9]{2}:){2}[0-9]{2}.[0-9]{3}Z$";

    private static final String RFC2822_DATE_GMT = "Mon, 19 Dec 2022 16:56:23 GMT";
    private static final String RFC2822_DATE_PST = "Mon, 19 Dec 2022 08:56:23 PST";
    private static final long TEST_EPOCH = 1671468983756L;

    private Date defaultDate;

    private String expectedString_ISO8601_FULL_DATE;
    private String expectedString_ISO8601_TIMEZONE_ISO8601_2X_PRECISION_SECOND;
    private String expectedString_ISO8601_TIMEZONE_ISO8601_3X_PRECISION_SECOND;
    private String expectedString_ISO8601_TIMEZONE_ISO8601_UTCZ_PRECISION_MILLISECOND;

    private Locale defaultLocale;

    @Before
    public void setup() throws ParseException {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT-7"));
        defaultLocale = new Locale(Locale.US.getLanguage(), Locale.US.getCountry(), "POSIX");
        // Setup static date whose expected formats are well-known
        expectedString_ISO8601_FULL_DATE = "2022-11-30";
        expectedString_ISO8601_TIMEZONE_ISO8601_2X_PRECISION_SECOND = "2022-11-30T06:50:53-0700";
        expectedString_ISO8601_TIMEZONE_ISO8601_3X_PRECISION_SECOND = "2022-11-30T06:50:53-07:00";
        expectedString_ISO8601_TIMEZONE_ISO8601_UTCZ_PRECISION_MILLISECOND =
                "2022-11-30T13:50:53.945Z";

        SimpleDateFormat formatter =
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", defaultLocale);
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        defaultDate =
                formatter.parse(expectedString_ISO8601_TIMEZONE_ISO8601_UTCZ_PRECISION_MILLISECOND);
        formatter.applyPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    }

    @Test
    public void testClassIsWellDefined() {
        try {
            TestHelper.assertUtilityClassWellDefined(TimeUtils.class);
        } catch (Exception e) {
            fail("TimeUtil class is not well defined, throwing exception " + e);
        }
    }

    // Testing each API with a well-known date and corresponding formatting result
    @Test
    public void testGetISO8601Date_when_validDate() {
        String formattedDate = TimeUtils.getISO8601Date(defaultDate);
        assertEquals(expectedString_ISO8601_TIMEZONE_ISO8601_3X_PRECISION_SECOND, formattedDate);
    }

    @Test
    public void testGetISO8601DateNoColon_when_validDate() {
        String formattedDate = TimeUtils.getISO8601DateNoColon(defaultDate);
        assertEquals(expectedString_ISO8601_TIMEZONE_ISO8601_2X_PRECISION_SECOND, formattedDate);
    }

    @Test
    public void testGetISO8601UTCDateWithMilliseconds_when_validDate() {
        String formattedDate = TimeUtils.getISO8601UTCDateWithMilliseconds(defaultDate);
        assertEquals(
                expectedString_ISO8601_TIMEZONE_ISO8601_UTCZ_PRECISION_MILLISECOND, formattedDate);
    }

    @Test
    public void testGetISO8601FullDate_when_validDate() {
        String formattedDate = TimeUtils.getISO8601FullDate(defaultDate);
        assertEquals(expectedString_ISO8601_FULL_DATE, formattedDate);
    }

    // Testing each API with a new Date instance and verifying output using regex
    @Test
    public void testGetISO8601Date_returns_correctFormat() {
        String formattedDate = TimeUtils.getISO8601Date();
        assertTrue(formattedDate.matches(DATE_REGEX_ISO8601_TIMEZONE_ISO8601_3X_PRECISION_SECOND));
    }

    @Test
    public void testGetISO8601DateNoColon_returns_correctFormat() {
        String formattedDate = TimeUtils.getISO8601DateNoColon();
        assertTrue(formattedDate.matches(DATE_REGEX_ISO8601_TIMEZONE_ISO8601_2X_PRECISION_SECOND));
    }

    @Test
    public void testGetISO8601FullDate_returns_correctFormat() {
        String formattedDate = TimeUtils.getISO8601FullDate();
        assertTrue(formattedDate.matches(DATE_REGEX_ISO8601_FULL_DATE));
    }

    @Test
    public void testGetISO8601UTCDateWithMilliseconds_returns_correctFormat() {
        String formattedDate = TimeUtils.getISO8601UTCDateWithMilliseconds();
        assertTrue(
                formattedDate.matches(
                        DATE_REGEX_ISO8601_TIMEZONE_ISO8601_UTCZ_PRECISION_MILLISECOND));
    }

    @Test
    public void testGetRFC2822Date_localeUS() {
        final String rfc2822Date =
                TimeUtils.getRFC2822Date(TEST_EPOCH, TimeZone.getTimeZone("GMT"), Locale.US);
        assertEquals(RFC2822_DATE_GMT, rfc2822Date);
    }

    @Test
    public void testGetRFC2822Date_localePST() {
        final String rfc2822Date =
                TimeUtils.getRFC2822Date(TEST_EPOCH, TimeZone.getTimeZone("PST"), Locale.US);
        assertEquals(RFC2822_DATE_PST, rfc2822Date);
    }

    @Test
    public void testParseRFC2822Date_localeGMT() {
        final Date rfc2822Date =
                TimeUtils.parseRFC2822Date(
                        RFC2822_DATE_GMT, TimeZone.getTimeZone("GMT"), Locale.US);
        assertEquals(
                new Date(TEST_EPOCH).toInstant().getEpochSecond(),
                rfc2822Date.toInstant().getEpochSecond());
    }

    @Test
    public void testParseRFC2822Date_localePST() {
        final Date rfc2822Date =
                TimeUtils.parseRFC2822Date(
                        RFC2822_DATE_PST, TimeZone.getTimeZone("PST"), Locale.US);
        assertEquals(
                new Date(TEST_EPOCH).toInstant().getEpochSecond(),
                rfc2822Date.toInstant().getEpochSecond());
    }

    @Test
    public void testParseRFC2822Date_nullRFC2822Date() {
        final Date rfc2822Date =
                TimeUtils.parseRFC2822Date(null, TimeZone.getTimeZone("PST"), Locale.US);
        assertNull(rfc2822Date);
    }
}
