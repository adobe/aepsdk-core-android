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

interface SystemNotificationService {
	/**
	 * Supported notification types that the underlying platform could notify the core about.
	 */
	enum NotificationType {
		CONNECTIVITY_CHANGE, REFERRER_INFO_AVAILABLE
	}

	/**
	 * Register to receive an Event with source EventSource.OS
	 * for a particular {@link NotificationType}
	 *
	 * @param notificationType The notification type you would like to receive events about.
	 *
	 * @return true if the notification type is supported on the platform and the registration was successful. False
	 * otherwise.
	 */
	boolean registerForNotification(NotificationType notificationType);

	/**
	 * Unregister for a previously registered notification.
	 *
	 * @param notificationType The notification type that was previously registered for.
	 */
	void unregisterForNotification(NotificationType notificationType);

}
