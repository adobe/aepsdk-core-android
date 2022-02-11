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

import java.util.Map;

public class MockUIService implements UIService {

	public boolean showAlertWasCalled = false;
	public String showAlertTitle;
	public String showAlertMessage;
	public String showAlertPositiveButtonText;
	public String showAlertNegativeButtonText;
	public UIAlertListener showAlertUIAlertListener;

	@Override
	public void showAlert(String title, String message, String positiveButtonText, String negativeButtonText,
						  UIAlertListener UIAlertListener) {
		showAlertWasCalled = true;
		showAlertTitle = title;
		showAlertMessage = message;
		showAlertPositiveButtonText = positiveButtonText;
		showAlertNegativeButtonText = negativeButtonText;
		showAlertUIAlertListener = UIAlertListener;
	}

	public boolean createFullscreenMessageWasCalled = false;
	public String createFullscreenMessageHtml;
	public UIFullScreenListener createFullscreenMessageUIFullScreenListener;
	public UIFullScreenMessage createUIFullScreenMessageReturn;

	@Override
	public UIFullScreenMessage createFullscreenMessage(String html, UIFullScreenListener UIFullScreenListener) {
		createFullscreenMessageWasCalled = true;
		createFullscreenMessageHtml = html;
		createFullscreenMessageUIFullScreenListener = UIFullScreenListener;
		return createUIFullScreenMessageReturn;
	}

	public boolean createMessagingInAppMessageWasCalled = false;
	public String createMessagingInAppMessageHtml;
	//	public FullscreenMessageDelegate createFullscreenMessageDelegate;
	//	public FullscreenMessage createFullScreenMessageReturn;
	//	@Override
	//	public FullscreenMessage createFullscreenMessage(String html, FullscreenMessageDelegate listener,
	//			boolean isLocalImageUsed, Object parent) {
	//		createMessagingInAppMessageWasCalled = true;
	//		createMessagingInAppMessageHtml = html;
	//		createFullscreenMessageDelegate = listener;
	//		return createFullScreenMessageReturn;
	//	}

	public boolean showLocalNotificationWasCalled = false;
	public String showLocalNotificationIdentifier;
	public String showLocalNotificationContent;
	public long showLocalNotificationFireDate;
	public int showLocalNotificationDelaySeconds;
	public String showLocalNotificationDeeplink;
	public Map<String, Object> showLocalNotificationUserInfo;
	public String showLocalNotificationSound;
	public String showLocalNotificationTitle;

	@Override
	public void showLocalNotification(final String identifier, final String content, final long fireDate,
									  final int delaySeconds, final String deeplink, final Map<String, Object> userInfo,
									  final String sound, final String title) {
		showLocalNotificationWasCalled = true;
		showLocalNotificationContent = content;
		showLocalNotificationDeeplink = deeplink;
		showLocalNotificationDelaySeconds = delaySeconds;
		showLocalNotificationFireDate = fireDate;
		showLocalNotificationIdentifier = identifier;
		showLocalNotificationUserInfo = userInfo;
		showLocalNotificationSound = sound;
		showLocalNotificationTitle = title;
	}

	@Override
	public void showLocalNotification(final String identifier, final String content, final long fireDate,
									  final int delaySeconds, final String deeplink, final Map<String, Object> userInfo,
									  final String sound) {
		showLocalNotificationWasCalled = true;
		showLocalNotificationContent = content;
		showLocalNotificationDeeplink = deeplink;
		showLocalNotificationDelaySeconds = delaySeconds;
		showLocalNotificationFireDate = fireDate;
		showLocalNotificationIdentifier = identifier;
		showLocalNotificationUserInfo = userInfo;
		showLocalNotificationSound = sound;
	}

	public boolean showUrlWasCalled = false;
	public String showUrlUrl;
	public boolean showUrlReturn;

	@Override
	public boolean showUrl(String url) {
		showUrlUrl = url;
		showUrlWasCalled = true;
		return showUrlReturn;
	}

	AppState appState;
	@Override
	public AppState getAppState() {
		return appState;
	}

	@Override
	public void registerAppStateListener(final AppStateListener listener) {

	}

	@Override
	public void unregisterAppStateListener(final AppStateListener listener) {

	}

	public boolean isMessageDisplayedReturnValue = false;
	@Override
	public boolean isMessageDisplayed() {
		return isMessageDisplayedReturnValue;
	}


	public boolean createFloatingButtonCalled;
	public FloatingButtonListener createFloatingButtonParamListener;
	public FloatingButton createFloatingButtonReturn;
	@Override
	public FloatingButton createFloatingButton(FloatingButtonListener buttonListener) {
		createFloatingButtonCalled = true;
		createFloatingButtonParamListener = buttonListener;
		return createFloatingButtonReturn;
	}
}
