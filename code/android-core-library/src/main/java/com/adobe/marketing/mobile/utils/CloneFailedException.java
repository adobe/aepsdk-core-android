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

package com.adobe.marketing.mobile.utils;

/**
 * Exception thrown by {@link EventDataUtils} to indicate that an
 * exception occurred during deep clone.
 */
public class CloneFailedException extends Exception {
    /**
     * Constructor.
     */
    public CloneFailedException() {
        super("Object clone error occurred.");
    }

    /**
     * Constructor.
     *
     * @param message {@code String} message for the exception
     */
    public CloneFailedException(final String message) {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param inner {@code Exception} that caused this exception
     */
    public CloneFailedException(final Exception inner) {
        super(inner);
    }
}
