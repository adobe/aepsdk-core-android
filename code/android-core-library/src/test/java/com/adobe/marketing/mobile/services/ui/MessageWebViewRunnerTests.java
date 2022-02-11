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

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.widget.FrameLayout;

import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.internal.context.App;
import com.adobe.marketing.mobile.services.ui.MessageSettings.MessageGesture;
import com.adobe.marketing.mobile.services.ui.MessageSettings.MessageAnimation;
import com.adobe.marketing.mobile.services.ui.MessageSettings.MessageAlignment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.HashMap;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MessageWebViewRunner.class, MessageWebView.class, WebSettings.class, Build.VERSION.class})
public class MessageWebViewRunnerTests {
	@Mock
	private AEPMessage mockAEPMessage;
	@Mock
	private MessageFragment mockMessageFragment;
	@Mock
	private ViewGroup mockViewGroup;
	@Mock
	private FrameLayout mockFrameLayout;
	@Mock
	private WebSettings mockWebSettings;
	@Mock
	private Context mockContext;
	@Mock
	private Application mockApp;
	@Mock
	private App.AppContextProvider mockAppContextProvider;

	private MessageWebViewRunner messageFragmentRunner;
	private AEPMessageSettings.Builder messageSettingsBuilder = new AEPMessageSettings.Builder(this);
	private AEPMessageSettings aepMessageSettings;
	private MessageWebView mockMessageWebview;
	private HashMap<MessageGesture, String> gestureMap = new HashMap<>();

	@Before
	public void setup() throws Exception {
		MobileCore.setApplication(mockApp);
		Mockito.when(mockAppContextProvider.getAppContext()).thenReturn(mockContext);
		App.getInstance().initializeApp(mockAppContextProvider);
		gestureMap.put(MessageGesture.BACKGROUND_TAP, "adbinapp://dismiss");
		gestureMap.put(MessageGesture.SWIPE_LEFT, "adbinapp://dismiss?interaction=negative");
		gestureMap.put(MessageGesture.SWIPE_RIGHT, "adbinapp://dismiss?interaction=positive");
		gestureMap.put(MessageGesture.SWIPE_UP, "adbinapp://dismiss");
		gestureMap.put(MessageGesture.SWIPE_DOWN, "adbinapp://dismiss");
		aepMessageSettings = messageSettingsBuilder.setWidth(100)
							 .setHeight(100)
							 .setBackdropColor("808080")
							 .setBackdropOpacity(0.5f)
							 .setCornerRadius(70.0f)
							 .setDismissAnimation(MessageAnimation.FADE)
							 .setDisplayAnimation(MessageAnimation.CENTER)
							 .setGestures(gestureMap)
							 .setHorizontalAlign(MessageAlignment.CENTER)
							 .setHorizontalInset(5)
							 .setVerticalAlign(MessageAlignment.TOP)
							 .setVerticalInset(10)
							 .build();
		Mockito.when(mockAEPMessage.getSettings()).thenReturn(aepMessageSettings);
		Mockito.when(mockAEPMessage.getMessageFragment()).thenReturn(mockMessageFragment);
		Mockito.when(mockAEPMessage.getMessageHtml()).thenReturn("some html");

		mockMessageWebview = PowerMockito.mock(MessageWebView.class);
		PowerMockito.whenNew(MessageWebView.class).withAnyArguments().thenReturn(mockMessageWebview);
		PowerMockito.when(mockMessageWebview.getSettings()).thenReturn(mockWebSettings);

		Mockito.when(mockViewGroup.getWidth()).thenReturn(200);
		Mockito.when(mockViewGroup.getHeight()).thenReturn(400);
		Whitebox.setInternalState(mockAEPMessage, "rootViewGroup", mockViewGroup);
		Whitebox.setInternalState(mockAEPMessage, "fragmentFrameLayout", mockFrameLayout);
	}

	@Test
	public void testRunnable_WithValidAEPMessage_ThenMessageShown() {
		// test
		messageFragmentRunner = new MessageWebViewRunner(mockAEPMessage);
		messageFragmentRunner.run();
		// verify
		Mockito.verify(mockMessageWebview, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
		Mockito.verify(mockViewGroup, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
		Mockito.verify(mockAEPMessage, Mockito.times(1)).isMessageVisible();
		Mockito.verify(mockMessageWebview, Mockito.times(1)).getSettings();
		Mockito.verify(mockAEPMessage, Mockito.times(1)).viewed();
	}

	@Test
	public void testRunnable_WithValidAEPMessage_And_MessageAnimationStartsFromTop_ThenMessageShown() {
		// setup
		aepMessageSettings = messageSettingsBuilder
							 .setDisplayAnimation(MessageAnimation.TOP)
							 .build();
		Mockito.when(mockAEPMessage.getSettings()).thenReturn(aepMessageSettings);
		messageFragmentRunner = new MessageWebViewRunner(mockAEPMessage);
		// test
		messageFragmentRunner.run();
		// verify
		Mockito.verify(mockMessageWebview, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
		Mockito.verify(mockViewGroup, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
		Mockito.verify(mockAEPMessage, Mockito.times(1)).isMessageVisible();
		Mockito.verify(mockMessageWebview, Mockito.times(1)).getSettings();
		Mockito.verify(mockAEPMessage, Mockito.times(1)).viewed();
	}

	@Test
	public void testRunnable_WithValidAEPMessage_And_MessageAnimationStartsFromLeft_ThenMessageShown() {
		// setup
		aepMessageSettings = messageSettingsBuilder
							 .setDisplayAnimation(MessageAnimation.LEFT)
							 .build();
		Mockito.when(mockAEPMessage.getSettings()).thenReturn(aepMessageSettings);
		messageFragmentRunner = new MessageWebViewRunner(mockAEPMessage);
		// test
		messageFragmentRunner.run();
		// verify
		Mockito.verify(mockMessageWebview, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
		Mockito.verify(mockViewGroup, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
		Mockito.verify(mockAEPMessage, Mockito.times(1)).isMessageVisible();
		Mockito.verify(mockMessageWebview, Mockito.times(1)).getSettings();
		Mockito.verify(mockAEPMessage, Mockito.times(1)).viewed();
	}

	@Test
	public void testRunnable_WithValidAEPMessage_And_MessageAnimationStartsFromRight_ThenMessageShown() {
		// setup
		aepMessageSettings = messageSettingsBuilder
							 .setDisplayAnimation(MessageAnimation.RIGHT)
							 .build();
		Mockito.when(mockAEPMessage.getSettings()).thenReturn(aepMessageSettings);
		messageFragmentRunner = new MessageWebViewRunner(mockAEPMessage);
		// test
		messageFragmentRunner.run();
		// verify
		Mockito.verify(mockMessageWebview, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
		Mockito.verify(mockViewGroup, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
		Mockito.verify(mockAEPMessage, Mockito.times(1)).isMessageVisible();
		Mockito.verify(mockMessageWebview, Mockito.times(1)).getSettings();
		Mockito.verify(mockAEPMessage, Mockito.times(1)).viewed();
	}

	@Test
	public void testRunnable_WithValidAEPMessage_And_MessageAnimationStartsFromBottom_ThenMessageShown() {
		// setup
		aepMessageSettings = messageSettingsBuilder
							 .setDisplayAnimation(MessageAnimation.BOTTOM)
							 .build();
		Mockito.when(mockAEPMessage.getSettings()).thenReturn(aepMessageSettings);
		messageFragmentRunner = new MessageWebViewRunner(mockAEPMessage);
		// test
		messageFragmentRunner.run();
		// verify
		Mockito.verify(mockMessageWebview, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
		Mockito.verify(mockViewGroup, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
		Mockito.verify(mockAEPMessage, Mockito.times(1)).isMessageVisible();
		Mockito.verify(mockMessageWebview, Mockito.times(1)).getSettings();
		Mockito.verify(mockAEPMessage, Mockito.times(1)).viewed();
	}

	@Test
	public void testRunnable_WithValidAEPMessage_And_MessageAnimationFadesIn_ThenMessageShown() {
		// setup
		aepMessageSettings = messageSettingsBuilder
							 .setDisplayAnimation(MessageAnimation.FADE)
							 .build();
		Mockito.when(mockAEPMessage.getSettings()).thenReturn(aepMessageSettings);
		messageFragmentRunner = new MessageWebViewRunner(mockAEPMessage);
		// test
		messageFragmentRunner.run();
		// verify
		Mockito.verify(mockMessageWebview, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
		Mockito.verify(mockViewGroup, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
		Mockito.verify(mockAEPMessage, Mockito.times(1)).isMessageVisible();
		Mockito.verify(mockMessageWebview, Mockito.times(1)).getSettings();
		Mockito.verify(mockAEPMessage, Mockito.times(1)).viewed();
	}

	@Test
	public void testRunnable_WithValidAEPMessage_And_NoMessageAnimation_ThenMessageShown() {
		// setup
		aepMessageSettings = messageSettingsBuilder
							 .setDisplayAnimation(MessageAnimation.NONE)
							 .build();
		Mockito.when(mockAEPMessage.getSettings()).thenReturn(aepMessageSettings);
		messageFragmentRunner = new MessageWebViewRunner(mockAEPMessage);
		// test
		messageFragmentRunner.run();
		// verify
		Mockito.verify(mockMessageWebview, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
		Mockito.verify(mockViewGroup, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
		Mockito.verify(mockAEPMessage, Mockito.times(1)).isMessageVisible();
		Mockito.verify(mockMessageWebview, Mockito.times(1)).getSettings();
		Mockito.verify(mockAEPMessage, Mockito.times(1)).viewed();
	}

	@Test
	public void testRunnable_WithValidAEPMessage_And_NonFullscreenMessage_ThenMessageShown() {
		// setup
		aepMessageSettings = messageSettingsBuilder
							 .setHeight(50)
							 .build();
		Mockito.when(mockAEPMessage.getSettings()).thenReturn(aepMessageSettings);
		messageFragmentRunner = new MessageWebViewRunner(mockAEPMessage);
		// test
		messageFragmentRunner.run();
		// verify
		Mockito.verify(mockMessageWebview, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
		Mockito.verify(mockViewGroup, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
		Mockito.verify(mockAEPMessage, Mockito.times(1)).isMessageVisible();
		Mockito.verify(mockMessageWebview, Mockito.times(1)).getSettings();
		Mockito.verify(mockAEPMessage, Mockito.times(1)).viewed();
	}

	@Test
	public void testRunnable_WithValidAEPMessage_And_MessageHorizontalAlignLeftThenMessageShown() {
		// setup
		aepMessageSettings = messageSettingsBuilder
							 .setHorizontalAlign(MessageAlignment.LEFT)
							 .build();
		Mockito.when(mockAEPMessage.getSettings()).thenReturn(aepMessageSettings);
		messageFragmentRunner = new MessageWebViewRunner(mockAEPMessage);
		// test
		messageFragmentRunner.run();
		// verify
		Mockito.verify(mockMessageWebview, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
		Mockito.verify(mockViewGroup, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
		Mockito.verify(mockAEPMessage, Mockito.times(1)).isMessageVisible();
		Mockito.verify(mockMessageWebview, Mockito.times(1)).getSettings();
		Mockito.verify(mockAEPMessage, Mockito.times(1)).viewed();
	}

	@Test
	public void testRunnable_WithValidAEPMessage_And_MessageHorizontalAlignRightThenMessageShown() {
		// setup
		aepMessageSettings = messageSettingsBuilder
							 .setHorizontalAlign(MessageAlignment.RIGHT)
							 .build();
		Mockito.when(mockAEPMessage.getSettings()).thenReturn(aepMessageSettings);
		messageFragmentRunner = new MessageWebViewRunner(mockAEPMessage);
		// test
		messageFragmentRunner.run();
		// verify
		Mockito.verify(mockMessageWebview, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
		Mockito.verify(mockViewGroup, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
		Mockito.verify(mockAEPMessage, Mockito.times(1)).isMessageVisible();
		Mockito.verify(mockMessageWebview, Mockito.times(1)).getSettings();
		Mockito.verify(mockAEPMessage, Mockito.times(1)).viewed();
	}

	@Test
	public void testRunnable_WithValidAEPMessage_And_MessageVerticalAlignBottomThenMessageShown() {
		// setup
		aepMessageSettings = messageSettingsBuilder
							 .setVerticalAlign(MessageAlignment.BOTTOM)
							 .build();
		Mockito.when(mockAEPMessage.getSettings()).thenReturn(aepMessageSettings);
		messageFragmentRunner = new MessageWebViewRunner(mockAEPMessage);
		// test
		messageFragmentRunner.run();
		// verify
		Mockito.verify(mockMessageWebview, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
		Mockito.verify(mockViewGroup, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
		Mockito.verify(mockAEPMessage, Mockito.times(1)).isMessageVisible();
		Mockito.verify(mockMessageWebview, Mockito.times(1)).getSettings();
		Mockito.verify(mockAEPMessage, Mockito.times(1)).viewed();
	}

	@Test
	public void testRunnable_WithValidAEPMessage_And_MessageVerticalAlignCenterThenMessageShown() {
		// setup
		aepMessageSettings = messageSettingsBuilder
							 .setVerticalAlign(MessageAlignment.CENTER)
							 .build();
		Mockito.when(mockAEPMessage.getSettings()).thenReturn(aepMessageSettings);
		messageFragmentRunner = new MessageWebViewRunner(mockAEPMessage);
		// test
		messageFragmentRunner.run();
		// verify
		Mockito.verify(mockMessageWebview, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
		Mockito.verify(mockViewGroup, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
		Mockito.verify(mockAEPMessage, Mockito.times(1)).isMessageVisible();
		Mockito.verify(mockMessageWebview, Mockito.times(1)).getSettings();
		Mockito.verify(mockAEPMessage, Mockito.times(1)).viewed();
	}

	@Test
	public void testRunnable_WithValidAEPMessage_And_BuildVersionLessThanAPI17_ThenMessageShown() {
		// setup
		Whitebox.setInternalState(Build.VERSION.class, "SDK_INT", 16);
		messageFragmentRunner = new MessageWebViewRunner(mockAEPMessage);
		// test
		messageFragmentRunner.run();
		// verify
		Mockito.verify(mockMessageWebview, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
		Mockito.verify(mockViewGroup, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
		Mockito.verify(mockAEPMessage, Mockito.times(1)).isMessageVisible();
		Mockito.verify(mockMessageWebview, Mockito.times(1)).getSettings();
		Mockito.verify(mockAEPMessage, Mockito.times(1)).viewed();
	}

	@Test
	public void testRunnable_WithValidAEPMessage_And_NullRootViewGroup_ThenMessageNotShown() {
		// setup
		Whitebox.setInternalState(mockAEPMessage, "rootViewGroup", (ViewGroup) null);
		messageFragmentRunner = new MessageWebViewRunner(mockAEPMessage);
		// test
		messageFragmentRunner.run();
		// verify
		Mockito.verify(mockMessageWebview, Mockito.times(0)).setOnTouchListener(mockMessageFragment);
		Mockito.verify(mockViewGroup, Mockito.times(0)).setOnTouchListener(mockMessageFragment);
		Mockito.verify(mockAEPMessage, Mockito.times(0)).isMessageVisible();
		Mockito.verify(mockMessageWebview, Mockito.times(0)).getSettings();
		Mockito.verify(mockAEPMessage, Mockito.times(1)).cleanup();
	}

	@Test
	public void testRunnable_WithValidAEPMessage_And_RootviewHasWidthEqualToZero_ThenMessageNotShown() {
		// setup
		Mockito.when(mockViewGroup.getWidth()).thenReturn(0);
		messageFragmentRunner = new MessageWebViewRunner(mockAEPMessage);
		// test
		messageFragmentRunner.run();
		// verify
		Mockito.verify(mockMessageWebview, Mockito.times(0)).setOnTouchListener(mockMessageFragment);
		Mockito.verify(mockViewGroup, Mockito.times(0)).setOnTouchListener(mockMessageFragment);
		Mockito.verify(mockAEPMessage, Mockito.times(0)).isMessageVisible();
		Mockito.verify(mockMessageWebview, Mockito.times(0)).getSettings();
		Mockito.verify(mockAEPMessage, Mockito.times(1)).cleanup();
	}

	@Test
	public void testRunnable_WithValidAEPMessage_And_RootviewHasHeightEqualToZero_ThenMessageNotShown() {
		// setup
		Mockito.when(mockViewGroup.getHeight()).thenReturn(0);
		messageFragmentRunner = new MessageWebViewRunner(mockAEPMessage);
		// test
		messageFragmentRunner.run();
		// verify
		Mockito.verify(mockMessageWebview, Mockito.times(0)).setOnTouchListener(mockMessageFragment);
		Mockito.verify(mockViewGroup, Mockito.times(0)).setOnTouchListener(mockMessageFragment);
		Mockito.verify(mockAEPMessage, Mockito.times(0)).isMessageVisible();
		Mockito.verify(mockMessageWebview, Mockito.times(0)).getSettings();
		Mockito.verify(mockAEPMessage, Mockito.times(1)).cleanup();
	}

	@Test
	public void testRunnable_WithInvalidAEPMessage_ThenMessageNotShown() {
		// setup
		Mockito.when(mockAEPMessage.getMessageHtml()).thenReturn(null);
		messageFragmentRunner = new MessageWebViewRunner(mockAEPMessage);
		// test
		messageFragmentRunner.run();
		// verify
		Mockito.verify(mockMessageWebview, Mockito.times(0)).setOnTouchListener(mockMessageFragment);
		Mockito.verify(mockViewGroup, Mockito.times(0)).setOnTouchListener(mockMessageFragment);
		Mockito.verify(mockAEPMessage, Mockito.times(0)).isMessageVisible();
		Mockito.verify(mockMessageWebview, Mockito.times(0)).getSettings();
		Mockito.verify(mockAEPMessage, Mockito.times(1)).cleanup();
	}
}

