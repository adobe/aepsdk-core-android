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
 * Wraps an unexpected asynchronous error encountered by a 3rd party extension while processing. It
 * may contain a custom message, the {@link Throwable} cause and the custom error code {@link
 * ExtensionError}.
 *
 * @author Adobe Systems Incorporated
 * @version 5.0
 */
@SuppressWarnings("unused")
@Deprecated
public class ExtensionUnexpectedError extends Exception {

    private static final long serialVersionUID = 1L;
    private ExtensionError errorCode;

    /**
     * Constructs an {@code ExtensionUnexpectedError} with an error code.
     *
     * @param code the {@link ExtensionError} code
     */
    public ExtensionUnexpectedError(final ExtensionError code) {
        super();
        this.errorCode = code;
    }

    /**
     * Constructs an {@code ExtensionUnexpectedError} with a detailed message, cause, and error
     * code.
     *
     * @param message a {@link String} detailed message
     * @param cause the {@link Throwable} cause
     * @param code the {@link ExtensionError} code
     */
    ExtensionUnexpectedError(
            final String message, final Throwable cause, final ExtensionError code) {
        super(message, cause);
        this.errorCode = code;
    }

    /**
     * Constructs an {@code ExtensionUnexpectedError} with a detailed message, and error code.
     *
     * @param message a {@link String} detailed message
     * @param code the {@link ExtensionError} code
     */
    ExtensionUnexpectedError(final String message, final ExtensionError code) {
        super(message);
        this.errorCode = code;
    }

    /**
     * Constructs an {@code ExtensionUnexpectedError} with a cause, and error code.
     *
     * @param cause the {@link Throwable} cause
     * @param code the {@link ExtensionError} code
     */
    ExtensionUnexpectedError(final Throwable cause, final ExtensionError code) {
        super(cause);
        this.errorCode = code;
    }

    /**
     * Retrieves the custom error code that is associated with this unexpected error.
     *
     * @return {@link ExtensionError} the custom error code
     */
    public ExtensionError getErrorCode() {
        return this.errorCode;
    }
}
