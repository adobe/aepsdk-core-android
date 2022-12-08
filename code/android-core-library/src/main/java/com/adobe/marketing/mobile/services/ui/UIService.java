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

package com.adobe.marketing.mobile.services.ui;

import android.content.Intent;

/** Interface for displaying alerts, local notifications, and fullscreen web views. */
public interface UIService {
    /**
     * Display an alert.
     *
     * @param alertSetting An {@link AlertSetting} instance used for building an alert.
     * @param alertListener An {@link AlertListener} instance for alert message events
     */
    void showAlert(final AlertSetting alertSetting, final AlertListener alertListener);

    /**
     * Display a local notification.
     *
     * @param notificationSetting An {@link NotificationSetting} instance used for building a local
     *     notification.
     */
    void showLocalNotification(final NotificationSetting notificationSetting);

    boolean showUrl(String url);

    /**
     * Provides an {@link URIHandler} to decide the destination of the given URI
     *
     * @param uriHandler An {@link URIHandler} instance used to decide the Android link's
     *     destination
     */
    void setURIHandler(URIHandler uriHandler);

    /**
     * Returns a destination Intent for the given URI.
     *
     * @param uri the URI to open
     * @return an {@link Intent} instance
     */
    Intent getIntentWithURI(String uri);

    /**
     * Creates a floating button instance
     *
     * @param buttonListener {@link FloatingButtonListener} instance used for tracking UI floating
     *     button activity (tap/drag)
     * @return A {@link FloatingButton} instance
     */
    FloatingButton createFloatingButton(FloatingButtonListener buttonListener);

    /**
     * Create a Messaging extension in-app message.
     *
     * <p>WARNING: This API consumes HTML/CSS/JS using an embedded browser control. This means it is
     * subject to all the risks of rendering untrusted web pages and running untrusted JS. Treat all
     * calls to this API with caution and make sure input is vetted for safety somewhere.
     *
     * @param html String html content to be displayed with the message
     * @param listener FullscreenMessageDelegate for listening to Messaging extension in-app message
     *     events
     * @param isLocalImageUsed If true, an image from the app's assets directory will be used for
     *     the fullscreen message.
     * @param settings MessageSettings object defining layout and behavior of the new message.
     * @return FullscreenMessage object if the html is valid, null otherwise
     */
    FullscreenMessage createFullscreenMessage(
            final String html,
            final FullscreenMessageDelegate listener,
            final boolean isLocalImageUsed,
            final MessageSettings settings);
}
