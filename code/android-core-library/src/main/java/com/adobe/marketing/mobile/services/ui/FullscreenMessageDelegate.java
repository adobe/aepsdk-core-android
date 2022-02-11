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
 * Delegate for Messaging extension in-app message events.
 */
public interface FullscreenMessageDelegate {
	/**
	 * Invoked when the in-app message is displayed.
	 *
	 * @param message FullscreenMessage the in-app message being displayed
	 */
	void onShow(final FullscreenMessage message);

	/**
	 * Invoked when the in-app message is dismissed.
	 *
	 * @param message FullscreenMessage the in-app message being dismissed
	 */
	void onDismiss(final FullscreenMessage message);

	/**
	 * Used to determine if the in-app message should be shown.
	 *
	 * @param message FullscreenMessage the in-app message that is about to get displayed
	 */
	boolean shouldShowMessage(final FullscreenMessage message);

	/**
	 * Invoked when the in-app message is attempting to load a url.
	 *
	 * @param message FullscreenMessage the in-app message attempting to load the url
	 * @param url     String the url being loaded by the message
	 *
	 * @return True if the core wants to handle the URL (and not the fullscreen message view implementation)
	 */
	boolean overrideUrlLoad(final FullscreenMessage message, final String url);

	/**
	 * Invoked when the in-app message failed to be displayed.
	 */
	void onShowFailure();
}
