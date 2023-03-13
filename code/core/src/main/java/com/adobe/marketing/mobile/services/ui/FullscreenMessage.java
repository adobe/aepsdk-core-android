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

import android.webkit.WebView;
import java.util.Map;

/** Interface defining a Messaging extension in-app message. */
public interface FullscreenMessage {
    /** Display the fullscreen message. */
    void show();

    /**
     * Display the fullscreen message.
     *
     * @param withMessagingDelegateControl boolean signaling if the {@link
     *     MessagingDelegate#shouldShowMessage(FullscreenMessage)} should be bypassed
     */
    void show(final boolean withMessagingDelegateControl);

    /** Remove the fullscreen message from view. */
    void dismiss();

    /**
     * Open a url from this message.
     *
     * @param url String the url to open
     */
    void openUrl(final String url);

    /** Returns the object that created this message. */
    Object getParent();

    /**
     * The asset map contains the mapping between a remote image asset url and it's cached location.
     *
     * @param assetMap The {@code Map<String, String} object containing the mapping between a remote
     *     asset url and its cached location.
     */
    void setLocalAssetsMap(final Map<String, String> assetMap);

    /**
     * Sets or updates the {@link MessageSettings} for the current fullscreen message.
     *
     * @param messageSettings {@link MessageSettings} object defining layout and behavior of the new
     *     message.
     */
    void setMessageSetting(final MessageSettings messageSettings);

    /**
     * Return an instance of {@link WebView} setup for the current {@link FullscreenMessage}
     *
     * @return an instance of {@link WebView}
     */
    WebView getWebView();

    /**
     * Return an instance of {@link MessageSettings} setup for the current {@link FullscreenMessage}
     *
     * @return an instance of {@link MessageSettings}
     */
    MessageSettings getMessageSettings();
}
