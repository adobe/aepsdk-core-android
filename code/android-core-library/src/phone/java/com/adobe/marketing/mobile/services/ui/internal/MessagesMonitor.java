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

import com.adobe.marketing.mobile.services.ui.UIService;

public class MessagesMonitor {

	private static MessagesMonitor INSTANCE = new MessagesMonitor();

	private MessagesMonitor() {}
	public static MessagesMonitor getInstance() {
		return  INSTANCE;
	}

	private volatile boolean messageDisplayed;

	/**
	 * Returns true if a message is already displaying on the device.
	 *
	 * <p>
	 *
	 * This is a service provided (used by {@link UIService} for example) to determine if a UI message can be displayed at a particular moment.
	 *
	 * @return The displayed status of a message
	 *
	 */
	public boolean isDisplayed() {
		return messageDisplayed;
	}

	/**
	 * Notifies that a message was dismissed
	 */
	public void dismissed() {
		messageDisplayed = false;
	}

	/**
	 * Notifies that a message was displayed
	 */
	public void displayed() {
		messageDisplayed = true;
	}

}