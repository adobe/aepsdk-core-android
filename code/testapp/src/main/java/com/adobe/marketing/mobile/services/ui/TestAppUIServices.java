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

import com.adobe.marketing.mobile.services.ui.AlertSetting;
import com.adobe.marketing.mobile.services.ui.AndroidUIService;
import com.adobe.marketing.mobile.services.ui.FloatingButton;
import com.adobe.marketing.mobile.services.ui.FloatingButtonView;
import com.adobe.marketing.mobile.services.ui.FullscreenMessage;
import com.adobe.marketing.mobile.services.ui.NotificationSetting;

import java.util.Map;

public class TestAppUIServices {

	private AndroidUIService uiService;

	public TestAppUIServices() {
		uiService = new AndroidUIService();
	}

	public void showAlert(final String title, final String message, final String positiveButtonText,
						  final String negativeButtonText) {
		AlertSetting settings =  AlertSetting.build(title, message, positiveButtonText, negativeButtonText);
		uiService.showAlert(settings, null);
	}

	public void showLocalNotification(final String identifier, final String content, final long fireDate,
									  final int delaySeconds, final String deeplink, final Map<String, Object> userInfo,
									  final String sound) {
		NotificationSetting notificationSetting = NotificationSetting.build(identifier, content, fireDate, delaySeconds, deeplink, userInfo, sound, null);
		uiService.showLocalNotification(notificationSetting);
	}

	public void showLocalNotification(final String identifier, final String content, final long fireDate,
									  final int delaySeconds, final String deeplink, final Map<String, Object> userInfo,
									  final String sound, final String title) {
		NotificationSetting notificationSetting = NotificationSetting.build(identifier, content, fireDate, delaySeconds, deeplink, userInfo, sound, title);
		uiService.showLocalNotification(notificationSetting);
	}

	public void showFullscreenMessage(final String html) {
		FullscreenMessage fullScreenMessage = uiService.createFullscreenMessage(html, null, false, null);
		fullScreenMessage.show();
	}

	public void showUrl(final String url) {
		uiService.showUrl(url);
	}

	public void showFloatingButton() {
		FloatingButton floatingButtonManager = uiService.createFloatingButton(null);
		floatingButtonManager.display();
	}

	public void hideFloatingButton() {
		FloatingButton floatingButtonManager = uiService.createFloatingButton(null);
		floatingButtonManager.remove();
	}

	public static String getFloatingButtonTag() {
		return FloatingButtonView.VIEW_TAG;
	}
}
