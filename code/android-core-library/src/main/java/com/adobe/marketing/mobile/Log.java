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
package com.adobe.marketing.mobile;

/**
 * Logging class to handle log levels and platform-specific log output
 *
 * @author Adobe Systems Incorporated
 * @version 5.0
 */
class Log {
	private static LoggingService loggingService = null;
	private static LoggingMode loggingMode = LoggingMode.ERROR;

	static final String UNEXPECTED_NULL_VALUE = "Unexpected Null Value";
	static final String UNEXPECTED_EMPTY_VALUE = "Unexpected Empty Value";
	static final String INVALID_FORMAT = "Invalid Format";

	/**
	 * private constructor to prevent accidental instantiation
	 */
	private Log() {}

	/**
	 * Sets the platform specific logging service to use for log output
	 *
	 * @param 	loggingService 	LoggingService to use for log output
	 *
	 * @see LoggingService
	 */
	static void setLoggingService(final LoggingService loggingService) {
		Log.loggingService = loggingService;
	}

	/**
	 * Sets the log level to operate at
	 *
	 * @param 	loggingMode 	LoggingMode to use for log output
	 *
	 * @see LoggingMode
	 */
	static void setLogLevel(final LoggingMode loggingMode) {
		Log.loggingMode = loggingMode;
	}

	/**
	 * Gets the log level that the SDK is currently operating at
	 *
	 * @return LoggingMode describing the current level of logging.
	 */
	static LoggingMode getLogLevel() {
		return Log.loggingMode;
	}

	/**
	 * Used to print more verbose information. Info logging is expected to follow end-to-end every method an event hits.
	 * Prints information to the console only when the SDK is in LoggingMode: VERBOSE
	 *
	 * @param	source	the source of the information to be logged
	 * @param	format	the string format to be logged
	 * @param	params	values to be inserted into the format
	 *
	 * @see LoggingMode
	 */
	static void trace(final String source, final String format, final Object... params) {
		if (loggingService != null && loggingMode.id >= LoggingMode.VERBOSE.id) {
			try {
				loggingService.trace(source, String.format(format, params));
			} catch (Exception e) {
				loggingService.trace(source, format);
			}
		}
	}

	/**
	 * Information provided to the debug method should contain high-level details about the data being processed.
	 * Prints information to the console only when the SDK is in LoggingMode: VERBOSE, DEBUG
	 *
	 * @param	source	the source of the information to be logged
	 * @param	format	the string format to be logged
	 * @param	params	values to be inserted into the format
	 *
	 * @see LoggingMode
	 */
	static void debug(final String source, final String format, final Object... params) {
		if (loggingService != null && loggingMode.id >= LoggingMode.DEBUG.id) {
			try {
				loggingService.debug(source, String.format(format, params));
			} catch (Exception e) {
				loggingService.debug(source, format);
			}
		}
	}

	/**
	 * Information provided to the warning method indicates that a request has been made to the SDK, but the SDK
	 * will be unable to perform the requested task.  An example is catching an expected or unexpected but
	 * recoverable exception.
	 * Prints information to the console only when the SDK is in LoggingMode: VERBOSE, DEBUG, WARNING
	 *
	 * @param	source	the source of the information to be logged
	 * @param	format	the string format to be logged
	 * @param	params	values to be inserted into the format
	 *
	 * @see LoggingMode
	 */
	static void warning(final String source, final String format, final Object... params) {
		if (loggingService != null && loggingMode.ordinal() >= LoggingMode.WARNING.id) {
			try {
				loggingService.warning(source, String.format(format, params));
			} catch (Exception e) {
				loggingService.warning(source, format);
			}
		}
	}

	/**
	 * Information provided to the error method indicates that there has been an unrecoverable error.
	 * Prints information to the console regardless of current LoggingMode of the SDK.
	 *
	 * @param	source	the source of the information to be logged
	 * @param	format	the string format to be logged
	 * @param	params	values to be inserted into the format
	 *
	 * @see LoggingMode
	 */
	static void error(final String source, final String format, final Object... params) {
		if (loggingService != null && loggingMode.ordinal() >= LoggingMode.ERROR.id) {
			try {
				loggingService.error(source, String.format(format, params));
			} catch (Exception e) {
				loggingService.error(source, format);
			}
		}
	}
}
