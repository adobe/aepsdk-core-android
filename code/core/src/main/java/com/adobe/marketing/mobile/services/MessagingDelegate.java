/*
  Copyright 2023 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.services;

import com.adobe.marketing.mobile.services.ui.FullscreenMessage;
import com.adobe.marketing.mobile.services.ui.Showable;

/**
 * UI Message delegate which is used to listen for current message lifecycle events and control if
 * the message should be displayed.
 */
public interface MessagingDelegate {
    /**
     * Invoked when a message is displayed.
     *
     * @param message {@link FullscreenMessage} that is being displayed
     */
    @Deprecated
    default void onShow(final FullscreenMessage message) {
        Log.debug(ServiceConstants.LOG_TAG, "MessagingDelegate", "Fullscreen message shown.");
    }

    /**
     * Invoked when a {@code Showable} message is displayed.
     *
     * @param message {@link Showable} message that is being displayed
     */
    default void onShow(final Showable message) {
        Log.debug(ServiceConstants.LOG_TAG, "MessagingDelegate", "Message shown.");
    }

    /**
     * Invoked when a message is dismissed.
     *
     * @param message {@link FullscreenMessage} that is being dismissed
     */
    @Deprecated
    default void onDismiss(final FullscreenMessage message) {
        Log.debug(ServiceConstants.LOG_TAG, "MessagingDelegate", "Fullscreen message dismissed.");
    }

    /**
     * Invoked when a {@code Showable} message is dismissed.
     *
     * @param message {@link Showable} message that is being dismissed
     */
    default void onDismiss(final Showable message) {
        Log.debug(ServiceConstants.LOG_TAG, "MessagingDelegate", "Message shown.");
    }

    /**
     * Used to determine if a message should be shown.
     *
     * @param message {@link FullscreenMessage} that is about to get displayed
     * @return true if the message should be displayed, false otherwise
     */
    @Deprecated
    boolean shouldShowMessage(final FullscreenMessage message);

    /**
     * Used to determine if a message should be shown.
     *
     * @param message {@link Showable} message that is about to get displayed
     * @return true if the message should be displayed, false otherwise
     */
    default boolean shouldShowMessage(final Showable message) {
        return true;
    }

    /**
     * Called when the {@link FullscreenMessage} loads a url.
     *
     * @param url {@code String} being loaded by the {@code FullscreenMessage}
     * @param message {@link FullscreenMessage} loading a url {@code String}
     */
    @Deprecated
    default void urlLoaded(final String url, final FullscreenMessage message) {
        Log.debug(
                ServiceConstants.LOG_TAG,
                "MessagingDelegate",
                "Fullscreen message loaded url: %s",
                url);
    }

    /**
     * Called when the {@link Showable} message loads a url.
     *
     * @param url {@code String} being loaded by the {@code Presentable}
     * @param message {@link Showable} message loading a url {@code String}
     */
    default void urlLoaded(final String url, final Showable message) {
        Log.debug(ServiceConstants.LOG_TAG, "MessagingDelegate", "Message loaded url: %s", url);
    }
}
