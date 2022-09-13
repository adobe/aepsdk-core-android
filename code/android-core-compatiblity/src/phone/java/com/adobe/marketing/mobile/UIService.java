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

import com.adobe.marketing.mobile.internal.context.AppState;

import java.util.Map;

/**
 * Interface for displaying alerts, local notifications, and fullscreen web views.
 */
interface UIService {

    /**
     * Alert message event listener.
     */
    interface UIAlertListener {
        /**
         * Invoked on positive button clicks.
         */
        void onPositiveResponse();

        /**
         * Invoked on negative button clicks.
         */
        void onNegativeResponse();

        /**
         * Invoked when the alert is displayed.
         */
        void onShow();

        /**
         * Invoked when the alert is dismissed.
         */
        void onDismiss();
    }

    /**
     * Display an alert.
     *
     * @param title              String alert title
     * @param message            String alert message
     * @param positiveButtonText String positive response button text. Positive button will not be displayed if this value is null or empty
     * @param negativeButtonText String negative response button text. Negative button will not be displayed if this value is null or empty
     * @param uiAlertListener    UIAlertListener listener for alert message events
     */
    void showAlert(final String title, final String message, final String positiveButtonText,
                   final String negativeButtonText, final UIAlertListener uiAlertListener);

    /**
     * Fullscreen message event listener
     */
    interface UIFullScreenListener {
        /**
         * Invoked when the fullscreen message is displayed.
         *
         * @param message UIFullScreenMessage the message being displayed
         */
        void onShow(final UIFullScreenMessage message);

        /**
         * Invoked when the fullscreen message is dismissed.
         *
         * @param message UIFullScreenMessage the message being dismissed
         */
        void onDismiss(final UIFullScreenMessage message);

        /**
         * Invoked when the fullscreen message is attempting to load a url.
         *
         * @param message UIFullScreenMessage the message attempting to load the url
         * @param url     String the url being loaded by the message
         * @return True if the core wants to handle the URL (and not the fullscreen message view implementation)
         */
        boolean overrideUrlLoad(final UIFullScreenMessage message, final String url);
    }

    /**
     * Interface defining a fullscreen message.
     */
    interface UIFullScreenMessage {
        /**
         * Display the fullscreen message.
         */
        void show();

        /**
         * Open a url from this message.
         *
         * @param url String the url to open
         */
        void openUrl(final String url);

        /**
         * Remove the fullscreen message from view.
         */
        void remove();

        /**
         * Add the URL mapping of remote resource to local resource(in cache).
         *
         * @param assetMap, A @{@link Map<String, String>} contains the URL mapping of remote resource to local resource.
         */
        void setLocalAssetsMap(final Map<String, String> assetMap);
    }

    /**
     * Create a fullscreen message.
     * <p>
     * WARNING: This API consumes HTML/CSS/JS using an embedded browser control.
     * This means it is subject to all the risks of rendering untrusted web pages and running untrusted JS.
     * Treat all calls to this API with caution and make sure input is vetted for safety somewhere.
     *
     * @param html                 String html content to be displayed with the message
     * @param uiFullScreenListener UIFullScreenListener listener for fullscreen message events
     * @return UIFullScreenMessage object if the html is valid, null otherwise
     */
    UIFullScreenMessage createFullscreenMessage(final String html, final UIFullScreenListener uiFullScreenListener);

    /**
     * Display a local notification.
     *
     * @param identifier   String unique identifier for the local notification
     * @param content      String notification message content
     * @param fireDate     {@code long} containing a specific date and time to show the notification, represented as number of seconds since epoch
     * @param delaySeconds int number of seconds to wait before displaying this local notification
     * @param deeplink     String the link to be opened on notification clickthrough
     * @param userInfo     {@code Map<String, Object>} of additional data for the local notification
     * @param sound        {@code String} containing a custom sound to play when the notification is shown
     */
    void showLocalNotification(final String identifier, final String content, final long fireDate,
                               final int delaySeconds, final String deeplink, final Map<String, Object> userInfo, final String sound);

    /**
     * Display a local notification.
     *
     * @param identifier   String unique identifier for the local notification
     * @param content      String notification message content
     * @param fireDate     {@code long} containing a specific date and time to show the notification, represented as number of seconds since epoch
     * @param delaySeconds int number of seconds to wait before displaying this local notification
     * @param deeplink     String the link to be opened on notification clickthrough
     * @param userInfo     {@code Map<String, Object>} of additional data for the local notification
     * @param sound        {@code String} containing a custom sound to play when the notification is shown
     * @param title        (@code String} notification message title
     */
    void showLocalNotification(final String identifier, final String content, final long fireDate,
                               final int delaySeconds, final String deeplink, final Map<String, Object> userInfo, final String sound,
                               final String title);

    boolean showUrl(String url);

    /**
     * Enum representing application states.
     */
    enum AppState {
        FOREGROUND,
        BACKGROUND,
        UNKNOWN;

        static AppState from(com.adobe.marketing.mobile.internal.context.AppState appState) {
            switch (appState) {
                case FOREGROUND:
                    return AppState.FOREGROUND;
                case BACKGROUND:
                    return AppState.BACKGROUND;
                default:
                    return AppState.UNKNOWN;
            }
        }
    }

    /**
     * Get the current application state.
     *
     * @return AppState the current application state
     */
    AppState getAppState();

    /**
     * Listener for app state transition events.
     */
    interface AppStateListener {
        /**
         * invoked when the application transitions into the AppState.FOREGROUND state.
         */
        void onForeground();

        /**
         * invoked when the application transitions into the AppState.BACKGROUND state.
         */
        void onBackground();
    }

    /**
     * Register application state transition listener.
     *
     * @param listener an implementation of AppStateListener
     */
    void registerAppStateListener(final AppStateListener listener);

    /**
     * Unregister application state transition listener.
     *
     * @param listener the AppStateListener to unregister
     */
    void unregisterAppStateListener(final AppStateListener listener);

    /**
     * Returns true if there is another message displayed at this time, false otherwise.
     * The status is collected from the platform messages monitor and it applies if either
     * an alert message or a full screen message is displayed at some point.
     *
     * @return Whether a UI message is already displaying
     */
    boolean isMessageDisplayed();

    /**
     * UI service interface defining a floating button.
     */
    interface FloatingButton {
        /**
         * Display the floating button on the screen.
         */
        void display();

        /**
         * Remove the floating button.
         */
        void remove();
    }

    interface FloatingButtonListener extends com.adobe.marketing.mobile.services.ui.FloatingButtonListener {
    }

    /**
     * Creates a floating button instance
     *
     * @param buttonListener {@link FloatingButtonListener} instance used for tracking UI floating button activity (tap/drag)
     * @return A {@link FloatingButton} instance
     */
    FloatingButton createFloatingButton(FloatingButtonListener buttonListener);


}