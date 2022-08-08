/* ************************************************************************
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
 **************************************************************************/

package com.adobe.marketing.mobile;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

final class LifecycleUtil {
	private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	private LifecycleUtil() {}

	/**
	 * Formats a {@code Date} to an ISO 8601 date-time string in UTC as defined in
	 * <a href="https://tools.ietf.org/html/rfc3339#section-5.6">RFC 3339, section 5.6</a>
	 * For example, 2017-09-26T15:52:25Z
	 *
	 * @param timestamp a timestamp
	 * @return {@code timestamp} formatted to a string in the format of 'yyyy-MM-dd'T'HH:mm:ss'Z'',
	 * or an empty string if {@code timestamp} is null
	 */
	static String dateTimeISO8601String(final Date timestamp) {
		return dateToISO8601String(timestamp, DATE_TIME_FORMAT);
	}

	/**
	 * Formats a {@code Date} with the provided {@code timestampFormat}
	 *
	 * @param timestamp a timestamp
	 * @param timestampFormat the format in which to format the date or the default {@link #DATE_TIME_FORMAT} if
	 * 	 					  this parameter is null or empty
	 * @return {@code timestamp} formatted to a string
	 */
	private static String dateToISO8601String(final Date timestamp, final String timestampFormat) {
		if (timestamp == null) {
			return "";
		}

		final String timePattern = StringUtils.isNullOrEmpty(timestampFormat) ? DATE_TIME_FORMAT : timestampFormat;
		final Locale posixLocale = new Locale(Locale.US.getLanguage(), Locale.US.getCountry(), "POSIX");
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(timePattern, posixLocale);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return simpleDateFormat.format(timestamp);
	}

	/**
	 * Formats the locale value from SystemInfoService and replaces '_' with '-'
	 *
	 * @param locale active locale value
	 * @return string representation of the locale
	 */
	static String formatLocale(final Locale locale) {
		return locale == null ? null : locale.toString().replace('_', '-');
	}

}