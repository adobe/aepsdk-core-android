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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.Locale;
import org.junit.Test;

public class LifecycleUtilTest {

    private long timestamp = 1483889568301L; // GMT: Sunday, January 8, 2017 3:32:48.301 PM

    @Test
    public void testDateTimeISO8601String_onValidTimestamp_returnsFormattedString() {
        String serializedDate = LifecycleUtil.dateTimeISO8601String(new Date(timestamp));
        assertEquals("2017-01-08T15:32:48.301Z", serializedDate);
    }

    @Test
    public void testDateTimeISO8601String_onNull_returnsEmptyString() {
        String serializedDate = LifecycleUtil.dateTimeISO8601String(null);
        assertEquals("", serializedDate);
    }

    @Test
    public void testFormatLocale_happy() {
        String formattedLocale = LifecycleUtil.formatLocale(Locale.US);
        assertEquals("en-US", formattedLocale);
    }

    @Test
    public void testFormatLocale_null() {
        assertNull(LifecycleUtil.formatLocale(null));
    }
}
