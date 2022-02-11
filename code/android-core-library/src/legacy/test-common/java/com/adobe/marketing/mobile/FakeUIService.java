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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.fail;

public class FakeUIService implements UIService {

	private FakeFullscreenMessage fakeFullscreenMessage = new FakeFullscreenMessage();
	private UIFullScreenListener fullScreenListener;
	//	private FakeMessagingInAppMessage fakeMessagingInAppMessage = new FakeMessagingInAppMessage();
	//	private FullscreenMessageDelegate fullscreenMessageDelegate;
	private UIAlertListener alertListener;
	private FloatingButtonListener floatingButtonListener;
	private FakeFloatingButton fakeFloatingButton = new FakeFloatingButton();
	private List<String> fullScreenMessageHtmlContents = new ArrayList<String>();
	private CountDownLatch countDownLatch;
	private boolean isMessageDisplayed;
	private int alertShowCalledCount = 0;
	private String lastUrlShown;

	@Override
	public void showAlert(String title, String message, String positiveButtonText, String negativeButtonText,
						  UIAlertListener uiAlertListener) {
		alertShowCalledCount++;
		alertListener = uiAlertListener;
		alertListener.onShow();
	}

	public UIAlertListener getAlertListener() {
		return alertListener;
	}

	public int getNumberOfAlertShowCalls() {
		return alertShowCalledCount;
	}

	public List<String> getFullScreenMessageHtmlContents() {
		return fullScreenMessageHtmlContents;
	}

	public FakeFullscreenMessage getFakeFullscreenMessage() {
		return fakeFullscreenMessage;
	}

	public UIFullScreenListener getFullScreenListener() {
		return fullScreenListener;
	}

	public FloatingButtonListener getFloatingButtonListener() {
		return floatingButtonListener;
	}

	public FakeFloatingButton getFakeFloatingButton() {
		return fakeFloatingButton;
	}

	public void waitForFullScreenCreate(long timeoutMs) {
		countDownLatch = new CountDownLatch(1);

		try {
			countDownLatch.await(timeoutMs, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			fail("Timed out waiting for fullscreen message create");
		}
	}


	public void setMessageDisplayed(boolean messageDisplayed) {
		isMessageDisplayed = messageDisplayed;
	}

	public String getLastUrlShown() {
		return lastUrlShown;
	}

	@Override
	public UIFullScreenMessage createFullscreenMessage(String html, UIFullScreenListener uiFullScreenListener) {
		if (countDownLatch != null) {
			countDownLatch.countDown();
		}

		fullScreenMessageHtmlContents.add(html);
		fullScreenListener = uiFullScreenListener;
		fullScreenListener.onShow(fakeFullscreenMessage);

		return fakeFullscreenMessage;
	}
	//
	//	@Override
	//	public FullscreenMessage createFullscreenMessage(String html, FullscreenMessageDelegate listener,
	//			boolean isLocalImageUsed, Object parent) {
	//		if (countDownLatch != null) {
	//			countDownLatch.countDown();
	//		}
	//
	//		fullScreenMessageHtmlContents.add(html);
	//		fullscreenMessageDelegate = listener;
	//		listener.onShow(fakeMessagingInAppMessage);
	//
	//		return fakeMessagingInAppMessage;
	//	}

	@Override
	public void showLocalNotification(String identifier, String content, long fireDate, int delaySeconds, String deeplink,
									  Map<String, Object> userInfo, String sound) {
	}

	@Override
	public void showLocalNotification(String identifier, String content, long fireDate, int delaySeconds, String deeplink,
									  Map<String, Object> userInfo, String sound, String title) {
	}

	@Override
	public boolean showUrl(String url) {
		lastUrlShown = url;
		return false;
	}

	@Override
	public AppState getAppState() {
		return null;
	}

	@Override
	public void registerAppStateListener(AppStateListener listener) {
	}

	@Override
	public void unregisterAppStateListener(AppStateListener listener) {

	}

	@Override
	public boolean isMessageDisplayed() {
		return isMessageDisplayed;
	}

	@Override
	public FloatingButton createFloatingButton(FloatingButtonListener buttonListener) {
		return fakeFloatingButton;
	}

}
