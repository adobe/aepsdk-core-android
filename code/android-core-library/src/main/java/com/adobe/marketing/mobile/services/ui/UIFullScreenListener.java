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

/**
 * Fullscreen message event listener
 */
public interface UIFullScreenListener {
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
