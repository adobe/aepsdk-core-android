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

package com.adobe.marketing.mobile.services.ui.internal;

import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.MessagingDelegate;
import com.adobe.marketing.mobile.services.ServiceConstants;
import com.adobe.marketing.mobile.services.ServiceProvider;
import com.adobe.marketing.mobile.services.ui.FullscreenMessage;
import com.adobe.marketing.mobile.services.ui.UIService;

public class MessagesMonitor {
    private static final String TAG = "MessagesMonitor";

    private static final MessagesMonitor INSTANCE = new MessagesMonitor();

    private MessagesMonitor() {}

    public static MessagesMonitor getInstance() {
        return INSTANCE;
    }

    private volatile boolean messageDisplayed;

    /**
     * Returns true if a message is already displaying on the device.
     *
     * <p>This is a service provided (used by {@link UIService} for example) to determine if a UI
     * message can be displayed at a particular moment.
     *
     * @return The displayed status of a message
     */
    public boolean isDisplayed() {
        return messageDisplayed;
    }

    /** Notifies that a message was dismissed */
    public void dismissed() {
        messageDisplayed = false;
    }

    /** Notifies that a message was displayed */
    public void displayed() {
        messageDisplayed = true;
    }

    /**
     * Determines whether the provided {@link FullscreenMessage} should be shown. If a UI message is
     * already showing, this method will return false. If a {@link
     * com.adobe.marketing.mobile.services.MessagingDelegate} exists, this method will call its
     * {@link
     * com.adobe.marketing.mobile.services.MessagingDelegate#shouldShowMessage(FullscreenMessage)}
     * method.
     *
     * @param message {@code FullscreenMessage} to be shown
     * @return {@code boolean} true if message needs to be shown
     */
    public boolean show(final FullscreenMessage message) {
        return show(message, true);
    }

    /**
     * Determines whether the provided {@link FullscreenMessage} should be shown. If a UI message is
     * already showing, this method will return false. If a {@link
     * com.adobe.marketing.mobile.services.MessagingDelegate} exists, this method will call its
     * {@link
     * com.adobe.marketing.mobile.services.MessagingDelegate#shouldShowMessage(FullscreenMessage)}
     * method.
     *
     * @param message {@code FullscreenMessage} to be shown
     * @param delegateControl {@code boolean} If true, the {@code FullscreenMessageDelegate} will
     *     control whether the message should be shown
     * @return {@code boolean} true if message needs to be shown
     */
    public boolean show(final FullscreenMessage message, final boolean delegateControl) {
        if (isDisplayed()) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    "Message couldn't be displayed, another message is displayed at this time.");
            return false;
        }

        if (delegateControl) {
            final MessagingDelegate messagingDelegate =
                    ServiceProvider.getInstance().getMessageDelegate();
            if (messagingDelegate != null && !messagingDelegate.shouldShowMessage(message)) {
                Log.debug(
                        ServiceConstants.LOG_TAG,
                        TAG,
                        "Message couldn't be displayed, MessagingDelegate#shouldShowMessage states"
                                + " the message should not be displayed.");
                return false;
            }
        }

        // Change message monitor to display
        displayed();

        return true;
    }

    public boolean dismiss() {
        if (!isDisplayed()) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    "Message failed to be dismissed, nothing is currently displayed.");
            return false;
        }

        // Change message visibility to dismiss
        dismissed();

        return true;
    }
}
