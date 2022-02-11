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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.adobe.marketing.mobile.internal.context.App;
import com.adobe.marketing.mobile.services.ui.internal.MessagesMonitor;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AndroidUIServiceTests {
	@Mock
	private MessagesMonitor mockMessagesMonitor;
	@Mock
	private Activity mockActivity;

	private AndroidUIService androidUIService;

	private static AppContextProvider appContextProvider = new AppContextProvider();

	private static class AppContextProvider implements App.AppContextProvider {

		private Context context;

		private Activity currentActivity;
		public void setCurrentActivity(Activity currentActivity) {
			this.currentActivity = currentActivity;
		}
		public void setContext(Context context) {
			this.context = context;
		}

		@Override
		public Context getAppContext() {
			return this.context;
		}

		@Override
		public Activity getCurrentActivity() {
			return this.currentActivity;
		}
	}

	@Before
	public void setup() {
		androidUIService = new AndroidUIService();
		App.getInstance().initializeApp(appContextProvider);
	}

	@After
	public void cleanup() {
		FullscreenMessageActivity.message = null;
	}
	@Test
	public void fullscreenMessageIsShown_When_NoOtherMessagesAreDisplayed() {
		//setup
		when(mockMessagesMonitor.isDisplayed()).thenReturn(false);
		androidUIService.messagesMonitor = mockMessagesMonitor;


		appContextProvider.setCurrentActivity(mockActivity);
		//test
		UIService.UIFullScreenMessage fullScreenMessage =  androidUIService.createFullscreenMessage("", null);
		fullScreenMessage.show();
		//verify
		verify(mockActivity).startActivity(any(Intent.class));
		assertEquals(FullscreenMessageActivity.message, fullScreenMessage);

	}

	@Test
	public void fullscreenMessageIsNotShown_When_OtherMessagesAreDisplayed() {
		//setup
		when(mockMessagesMonitor.isDisplayed()).thenReturn(true);
		androidUIService.messagesMonitor = mockMessagesMonitor;

		appContextProvider.setCurrentActivity(mockActivity);
		//test
		UIService.UIFullScreenMessage fullScreenMessage =  androidUIService.createFullscreenMessage("", null);
		fullScreenMessage.show();
		//verify
		verify(mockActivity, times(0)).startActivity(any(Intent.class));
		assertNull(FullscreenMessageActivity.message);

	}

	@Test
	public void messageMonitorDismissedCalled_When_FullscreenMessageRemovedCalled() {
		//Setup
		androidUIService.messagesMonitor = mockMessagesMonitor;
		UIService.UIFullScreenMessage uiFullScreenMessage =
			androidUIService.createFullscreenMessage("", null);
		//test
		uiFullScreenMessage.remove();
		//verify
		verify(mockMessagesMonitor).dismissed();

	}

	@Test
	public void messageMonitorDismissedCalled_When_FullscreenMessageDismissCalled() {
		//Setup
		androidUIService.messagesMonitor = mockMessagesMonitor;
		UIService.UIFullScreenMessage uiFullScreenMessage =
			androidUIService.createFullscreenMessage("", null);
		//test
		((AndroidFullscreenMessage)uiFullScreenMessage).dismissed();
		//verify
		verify(mockMessagesMonitor).dismissed();

	}

	@Test
	public void messageMonitorDisplayedCalled_When_FullscreenMessageShown() {
		//Setup
		when(mockMessagesMonitor.isDisplayed()).thenReturn(false);
		androidUIService.messagesMonitor = mockMessagesMonitor;

		appContextProvider.setCurrentActivity(mockActivity);

		UIService.UIFullScreenMessage uiFullScreenMessage =
			androidUIService.createFullscreenMessage("", null);
		//test
		uiFullScreenMessage.show();
		//verify
		verify(mockMessagesMonitor).displayed();

	}
}