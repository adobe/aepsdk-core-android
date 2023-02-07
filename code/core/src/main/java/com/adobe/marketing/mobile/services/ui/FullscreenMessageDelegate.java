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

/** Fullscreen message listener for {@link FullscreenMessage} lifecycle events. */
public interface FullscreenMessageDelegate {
    /**
     * Invoked when the fullscreen message is displayed.
     *
     * @param message {@link FullscreenMessage} message which is currently shown
     */
    void onShow(final FullscreenMessage message);

    /**
     * Invoked when the fullscreen message is dismissed.
     *
     * @param message {@link FullscreenMessage} message which is dismissed
     */
    void onDismiss(final FullscreenMessage message);

    /**
     * Invoked when the fullscreen message is attempting to load a url.
     *
     * @param message {@link FullscreenMessage} message attempting to load the url
     * @param url {@code String} the url being loaded by the message
     * @return True if the core wants to handle the URL (and not the fullscreen message view
     *     implementation)
     */
    boolean overrideUrlLoad(final FullscreenMessage message, final String url);

    /** Invoked when the fullscreen message failed to be displayed. */
    void onShowFailure();
}
