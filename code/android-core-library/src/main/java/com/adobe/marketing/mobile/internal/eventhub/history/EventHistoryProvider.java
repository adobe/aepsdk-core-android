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

package com.adobe.marketing.mobile.internal.eventhub.history;

/**
 * Defines a class for setting or retrieving an {@link EventHistory} service.
 */
public class EventHistoryProvider {
	private static EventHistory eventHistory;

	/**
	 * Sets the {@link EventHistory} to use for historical event database operations.
	 *
	 * @param platformEventHistory {@code EventHistory} instance created on the platform side
	 */
	public static void setEventHistory(final EventHistory platformEventHistory) {
		eventHistory = platformEventHistory;
	}

	/**
	 * Retrieves the {@link EventHistory} to use for historical event database operations.
	 *
	 * @return {@code EventHistory} to use for historical event database operations
	 */
	public static EventHistory getEventHistory() {
		return eventHistory;
	}
}
