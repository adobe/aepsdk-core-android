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

package com.adobe.marketing.mobile.internal.util;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class TimeUtilsTest {
	private static final String DATE_REGEX_TIMEZONE_RFC822 =
		"^((19|2[0-9])[0-9]{2})-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])T{1}[0-2][0-9]:[0-5][0-9]:[0-5][0-9][-+][0-9]{4}$";

	private static final String DATE_REGEX_TIMEZONE_ISO8601 =
		"^((19|2[0-9])[0-9]{2})-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])T{1}[0-2][0-9]:[0-5][0-9]:[0-5][0-9][-+][0-9]{2}:[0-9]{2}$";

	@Before
	public void setup() {
		TimeZone.setDefault(TimeZone.getTimeZone("GMT-7"));
	}

	@Test
	public void testClassIsWellDefined() {
		try {
			TestHelper.assertUtilityClassWellDefined(TimeUtils.class);
		} catch (Exception e) {
			fail("TimeUtil class is not well defined, throwing exception " + e);
		}
	}

	@Test
	public void testGetUnixTime_returnTimestampInSeconds() {
		long timestamp = TimeUtils.getUnixTimeInSeconds();
		long currentTimestamp = System.currentTimeMillis() / 1000;
		assertTrue(timestamp - currentTimestamp <= 0);
	}

	@Test
	public void testGetIso8601Date_TimeZone_RFC822_when_ValidDate() {
		long timestamp = 1526405606000L;
		String formattedDate = TimeUtils.getIso8601Date(new Date(timestamp), "yyyy-MM-dd'T'HH:mm:ssZZZ");
		assertEquals("2018-05-15T10:33:26-0700", formattedDate);
	}

	@Test
	public void testGetIso8601Date_TimeZone_RFC822_when_NullDate() {
		String formattedDate = TimeUtils.getIso8601Date(null, "yyyy-MM-dd'T'HH:mm:ssZZZ");
		assertNotNull(formattedDate);
		assertTrue(formattedDate.matches(DATE_REGEX_TIMEZONE_RFC822));
	}

	@Test
	public void testGetIso8601Date_TimeZone_ISO8601_when_ValidDate() {
		long timestamp = 1526405606000L;
		String formattedDate = TimeUtils.getIso8601Date(new Date(timestamp), "yyyy-MM-dd'T'HH:mm:ssXXX");
		assertEquals("2018-05-15T10:33:26-07:00", formattedDate);
	}

	@Test
	public void testGetIso8601Date_TimeZone_ISO8601_when_NullDate() {
		String formattedDate = TimeUtils.getIso8601Date(null, "yyyy-MM-dd'T'HH:mm:ssXXX");
		assertNotNull(formattedDate);
		assertTrue(formattedDate.matches(DATE_REGEX_TIMEZONE_ISO8601));
	}

	@Test
	public void testGetIso8601Date_TimeZone_ISO8601_returns_milliseconds_and_UTC() {
		String formattedDate = TimeUtils.getIso8601DateTimeZoneISO8601();
		assertTrue(formattedDate.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}T([0-9]{2}:){2}[0-9]{2}.[0-9]{3}Z"));
	}
}
