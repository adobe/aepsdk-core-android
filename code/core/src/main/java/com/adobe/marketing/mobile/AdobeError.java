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

import java.io.Serializable;

/** */
public class AdobeError implements Serializable {

    private static final long serialVersionUID = 1L;

    /** when something unexpected happens internally. */
    public static final AdobeError UNEXPECTED_ERROR = new AdobeError("general.unexpected", 0);

    /** when a timeout happens. */
    public static final AdobeError CALLBACK_TIMEOUT = new AdobeError("general.callback.timeout", 1);

    /** when a callback is null. */
    public static final AdobeError CALLBACK_NULL = new AdobeError("general.callback.null", 2);

    /** when a extension is not initialized. */
    public static final AdobeError EXTENSION_NOT_INITIALIZED =
            new AdobeError("general.extension.not.initialized", 11);

    private final String errorName;
    private final int errorCode;

    protected AdobeError(final String errorName, final int errorCode) {
        this.errorName = errorName;
        this.errorCode = errorCode;
    }

    /**
     * Retrieves current error's name as a {@code String}.
     *
     * @return the error name {@link String}
     */
    public String getErrorName() {
        return errorName;
    }

    /**
     * Retrieves current error's code as a {@code int}.
     *
     * @return the error code {@code int}
     */
    public int getErrorCode() {
        return errorCode;
    }
}
