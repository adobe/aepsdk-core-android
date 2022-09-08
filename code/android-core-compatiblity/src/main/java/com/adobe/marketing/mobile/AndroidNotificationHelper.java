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

import android.content.IntentFilter;
import android.net.ConnectivityManager;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.adobe.marketing.mobile.SystemNotificationService.NotificationType.CONNECTIVITY_CHANGE;
import static com.adobe.marketing.mobile.SystemNotificationService.NotificationType.REFERRER_INFO_AVAILABLE;

class AndroidNotificationHelper {

	/**
	 * Holds the mapping between {@link SystemNotificationService.NotificationType}
	 * and the corresponding android intent action strings.
	 *
	 * <p>
	 * <b>Please note:</b> If new keys are added to {@link SystemNotificationService.NotificationType}
	 * then, please add the corresponding mapping in {@link #initializeAndroidIntentMap()}
	 * </p>
	 */
	private static final EnumMap<SystemNotificationService.NotificationType, String> androidIntentActionMap = new
	EnumMap<SystemNotificationService.NotificationType, String>(SystemNotificationService.NotificationType.class);

	private static final String INSTALL_REFERRER_ACTION = "com.android.vending.INSTALL_REFERRER";

	static {
		initializeAndroidIntentMap();
	}

	private static void initializeAndroidIntentMap() {
		androidIntentActionMap.put(CONNECTIVITY_CHANGE,
								   ConnectivityManager.CONNECTIVITY_ACTION);
		androidIntentActionMap.put(REFERRER_INFO_AVAILABLE, INSTALL_REFERRER_ACTION);
	}

	/**
	 * Creates an intent filter for a supported {@link SystemNotificationService.NotificationType}
	 * @param notificationType The NotificationType we want to create a IntentFilter for.
	 * @return A valid intent filter constructed using a Intent Action string. If no mapping exists in {@link #androidIntentActionMap}, then this will return null.
	 */
	static IntentFilter createIntentFilter(final SystemNotificationService.NotificationType notificationType) {
		IntentFilter intentFilter = null;
		String action = getIntentActionForNotificationType(notificationType);

		if (action != null) {
			intentFilter = new IntentFilter(action);
		}

		return intentFilter;
	}

	/**
	 * Returns the {@link SystemNotificationService.NotificationType}
	 * corresponding to the action, provided the action has been already mapped in {@link #androidIntentActionMap}
	 * @param action The Intent action we want to search for
	 * @return A corresponding {@link SystemNotificationService.NotificationType} if found, or null.
	 */
	public static SystemNotificationService.NotificationType getNotificationTypeForAction(final String action) {
		Set<Map.Entry<SystemNotificationService.NotificationType, String>> entries = androidIntentActionMap.entrySet();
		Iterator<Map.Entry<SystemNotificationService.NotificationType, String>> iterator = entries.iterator();

		while (iterator.hasNext()) {
			Map.Entry<SystemNotificationService.NotificationType, String> entry = iterator.next();

			if (entry.getValue() != null && entry.getValue().equalsIgnoreCase(action)) {
				return entry.getKey();
			}
		}

		return null;
	}

	private static String getIntentActionForNotificationType(SystemNotificationService.NotificationType notificationType) {
		return androidIntentActionMap.get(notificationType);
	}


}
