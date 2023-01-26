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
 * Defines all the error codes that can be returned by any of the extension APIs.
 *
 * @author Adobe Systems Incorporated
 * @version 5.0
 */
@Deprecated
public class ExtensionError extends AdobeError {

    private static final long serialVersionUID = 1L;

    /**
     * Unexpected error is returned when something happened internally while processing an
     * extension.
     */
    public static final ExtensionError UNEXPECTED_ERROR =
            new ExtensionError("extension.unexpected", 0);

    /** Extension bad name error is returned when the extension name is invalid (null, empty). */
    public static final ExtensionError BAD_NAME =
            new ExtensionError("extension.bad_extension_name", 1);

    /**
     * Extension duplicated name error is returned when an extension with the same name is already
     * registered.
     */
    public static final ExtensionError DUPLICATE_NAME =
            new ExtensionError("extension.dup_extension_name", 2);

    /**
     * Event type not supported is returned when a new listener is registered for an invalid event
     * type (null/empty).
     */
    public static final ExtensionError EVENT_TYPE_NOT_SUPPORTED =
            new ExtensionError("extension.event_type_not_supported", 3);

    /**
     * Event source not supported is returned when a new listener is registered for an invalid event
     * source (null/empty).
     */
    public static final ExtensionError EVENT_SOURCE_NOT_SUPPORTED =
            new ExtensionError("extension.event_source_not_supported", 4);

    /**
     * Event data not supported is returned when the event data cannot be converted to the supported
     * JSON format.
     */
    public static final ExtensionError EVENT_DATA_NOT_SUPPORTED =
            new ExtensionError("extension.event_data_not_supported", 5);

    /** Event null is returned when the provided event is null. */
    static final ExtensionError EVENT_NULL = new ExtensionError("extension.event_null", 6);

    /**
     * Listener timeout error is returned when the registered extension listener takes more than the
     * accepted timeout (~100ms).
     */
    public static final ExtensionError LISTENER_TIMEOUT =
            new ExtensionError("extension.listener_timeout_exception", 8);

    /** This error is returned when a null callback is provided for a required parameter. */
    public static final ExtensionError CALLBACK_NULL =
            new ExtensionError("extension.callback_null", 9);

    private ExtensionError(final String errorName, final int errorCode) {
        super(errorName, errorCode);
    }
}
