/* **************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2021 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 *
 * *************************************************************************/

package com.adobe.marketing.mobile;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

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
}
