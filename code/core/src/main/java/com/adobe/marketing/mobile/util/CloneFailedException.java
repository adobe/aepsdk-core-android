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

package com.adobe.marketing.mobile.util;

import androidx.annotation.NonNull;

/**
 * Exception thrown by {@link EventDataUtils} to indicate that an exception occurred during deep
 * clone.
 */
public class CloneFailedException extends Exception {

    private final Reason reason;

    /** Enum to indicate the reason for the exception. */
    enum Reason {
        MAX_DEPTH_REACHED,
        UNSUPPORTED_TYPE,
        UNKNOWN
    }

    /**
     * Constructor.
     *
     * @param message {@code String} message for the exception
     */
    public CloneFailedException(final String message) {
        this(message, Reason.UNKNOWN);
    }

    /**
     * Constructor.
     *
     * @param reason the {@link Reason} for the exception
     */
    CloneFailedException(@NonNull final Reason reason) {
        this(reason.toString(), reason);
    }

    /**
     * Private constructor to unify public constructors and allow cascading.
     *
     * @param message {@code String} message for the exception
     * @param reason the {@link Reason} for the exception
     */
    private CloneFailedException(@NonNull final String message, @NonNull final Reason reason) {
        super(message);
        this.reason = reason;
    }

    /**
     * Returns the {@link Reason} for the exception.
     *
     * @return returns the {@link Reason} for the exception
     */
    @NonNull Reason getReason() {
        return reason;
    }
}
