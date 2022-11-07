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
@Deprecated
class Log {
    static final String UNEXPECTED_NULL_VALUE = "Unexpected Null Value";
    static final String UNEXPECTED_EMPTY_VALUE = "Unexpected Empty Value";
    static final String INVALID_FORMAT = "Invalid Format";

    /**
     * private constructor to prevent accidental instantiation
     */
    private Log() {
    }

    /**
     * Used to print more verbose information. Info logging is expected to follow end-to-end every method an event hits.
     * Prints information to the console only when the SDK is in LoggingMode: VERBOSE
     *
     * @param source the source of the information to be logged
     * @param format the string format to be logged
     * @param params values to be inserted into the format
     * @see LoggingMode
     */
    @Deprecated
    static void trace(final String source, final String format, final Object... params) {
        com.adobe.marketing.mobile.services.Log.trace("", source, format, params);
    }

    /**
     * Information provided to the debug method should contain high-level details about the data being processed.
     * Prints information to the console only when the SDK is in LoggingMode: VERBOSE, DEBUG
     *
     * @param source the source of the information to be logged
     * @param format the string format to be logged
     * @param params values to be inserted into the format
     * @see LoggingMode
     */
    @Deprecated
    static void debug(final String source, final String format, final Object... params) {
        com.adobe.marketing.mobile.services.Log.debug("", source, format, params);
    }

    /**
     * Information provided to the warning method indicates that a request has been made to the SDK, but the SDK
     * will be unable to perform the requested task.  An example is catching an expected or unexpected but
     * recoverable exception.
     * Prints information to the console only when the SDK is in LoggingMode: VERBOSE, DEBUG, WARNING
     *
     * @param source the source of the information to be logged
     * @param format the string format to be logged
     * @param params values to be inserted into the format
     * @see LoggingMode
     */
    @Deprecated
    static void warning(final String source, final String format, final Object... params) {
        com.adobe.marketing.mobile.services.Log.warning("", source, format, params);
    }

    /**
     * Information provided to the error method indicates that there has been an unrecoverable error.
     * Prints information to the console regardless of current LoggingMode of the SDK.
     *
     * @param source the source of the information to be logged
     * @param format the string format to be logged
     * @param params values to be inserted into the format
     * @see LoggingMode
     */
    @Deprecated
    static void error(final String source, final String format, final Object... params) {
        com.adobe.marketing.mobile.services.Log.error("", source, format, params);
    }
}
