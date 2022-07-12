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

package com.adobe.marketing.mobile.rulesengine;

public class Log {
	private static Logging logging;

	public static void setLogging(Logging logging) {
		Log.logging = logging;
	}

	/**
	 * Used to print more verbose information.
	 *
	 * @param tag the tag of the localize message
	 * @param message the log message to be logged
	 */
	static void verbose(final String tag, final String message) {
		log(LogLevel.VERBOSE, tag, message);
	}

	/**
	 * Information provided to the debug method should contain high-level details about the data being processed.
	 *
	 * @param tag the tag of the localize message
	 * @param message the log message to be logged
	 */
	static void debug(final String tag, final String message) {
		log(LogLevel.DEBUG, tag, message);
	}

	/**
	 * Information provided to the warning method indicates that a request has been made to the SDK, but the SDK will be unable to perform the requested task.
	 *
	 * @param tag the tag of the localize message
	 * @param message the log message to be logged
	 */
	static void warning(final String tag, final String message) {
		log(LogLevel.WARNING, tag, message);
	}

	/**
	 * Information provided to the error method indicates that there has been an unrecoverable error.
	 *
	 * @param tag the tag of the localize message
	 * @param message the log message to be logged
	 */
	static void error(final String tag, final String message) {
		log(LogLevel.ERROR, tag, message);
	}

	private Log() {}
	private static void log(final LogLevel level, final String tag, final String message) {
		if (logging != null) {
			logging.log(level, tag, message);
		}
	}
}
