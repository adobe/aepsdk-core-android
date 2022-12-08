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

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import com.adobe.marketing.mobile.services.AppContextService;
import com.adobe.marketing.mobile.services.ServiceProviderModifier;
import com.adobe.marketing.mobile.services.ui.MessageSettings.MessageAnimation;
import com.adobe.marketing.mobile.services.ui.MessageSettings.MessageGesture;
import com.adobe.marketing.mobile.services.ui.internal.MessagesMonitor;
import java.util.HashMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AEPMessageTests {

    @Mock private MessageSettings mockAEPMessageSettings;

    @Mock private AppContextService mockAppContextService;

    @Mock private Context mockApplicationContext;

    @Mock private Activity mockActivity;

    @Mock private FrameLayout mockFrameLayout;

    @Mock private FullscreenMessageDelegate mockFullscreenMessageDelegate;

    @Mock private ViewGroup mockViewGroup;

    @Mock private View mockBackdrop;

    @Mock private MessageWebView mockWebView;

    @Mock private MotionEvent mockMotionEvent;

    @Mock private MessagesMonitor mockMessageMonitor;

    @Mock private MessageWebViewRunner mockMessageWebViewRunner;

    @Mock private FragmentManager mockFragmentManager;

    @Mock private FragmentTransaction mockFragmentTransaction;

    @Mock private MessageFragment mockMessageFragment;

    @Mock private Animation mockAnimation;

    private AEPMessage message;
    private HashMap<MessageGesture, String> gestureMap = new HashMap<>();

    @Before
    public void setup() throws Exception {
        gestureMap.put(MessageGesture.BACKGROUND_TAP, "adbinapp://dismiss");
        gestureMap.put(MessageGesture.SWIPE_LEFT, "adbinapp://dismiss?interaction=negative");
        gestureMap.put(MessageGesture.SWIPE_RIGHT, "adbinapp://dismiss?interaction=positive");
        gestureMap.put(MessageGesture.SWIPE_UP, "adbinapp://dismiss");
        gestureMap.put(MessageGesture.SWIPE_DOWN, "adbinapp://dismiss");

        Mockito.when(mockAEPMessageSettings.getGestures()).thenReturn(gestureMap);
        Mockito.when(mockMotionEvent.getAction()).thenReturn(MotionEvent.ACTION_DOWN);

        ServiceProviderModifier.setAppContextService(mockAppContextService);
        Mockito.when(mockAppContextService.getApplicationContext())
                .thenReturn(mockApplicationContext);
        Mockito.when(mockAppContextService.getCurrentActivity()).thenReturn(mockActivity);
    }

    // AEPMessage creation tests
    @Test
    public void testCreateAEPMessage() {
        // test
        try {
            message =
                    new AEPMessage(
                            "html",
                            mockFullscreenMessageDelegate,
                            false,
                            mockMessageMonitor,
                            mockAEPMessageSettings);
        } catch (final MessageCreationException ex) {
            Assert.fail(ex.getMessage());
        }

        // verify
        Assert.assertNotNull(message);
    }

    @Test(expected = MessageCreationException.class)
    public void testCreateAEPMessage_nullMessageDelegate() throws MessageCreationException {
        // test and verify
        message = new AEPMessage("html", null, false, mockMessageMonitor, mockAEPMessageSettings);
    }

    // AEMessage show tests
    @Test
    public void aepMessageIsShown_When_NoOtherMessagesAreDisplayed() {
        // setup
        Mockito.when(mockMessageMonitor.isDisplayed()).thenReturn(false);
        Mockito.when(
                        mockFullscreenMessageDelegate.shouldShowMessage(
                                ArgumentMatchers.any(AEPMessage.class)))
                .thenReturn(true);

        try {
            message =
                    new AEPMessage(
                            "html",
                            mockFullscreenMessageDelegate,
                            false,
                            mockMessageMonitor,
                            mockAEPMessageSettings);
        } catch (MessageCreationException ex) {
            Assert.fail(ex.getMessage());
        }
        message.rootViewGroup = mockViewGroup;
        Mockito.when(mockViewGroup.getMeasuredWidth()).thenReturn(1000);
        Mockito.when(mockViewGroup.getMeasuredHeight()).thenReturn(1000);
        // test
        message.show();
        // verify
        Mockito.verify(mockMessageMonitor, Mockito.times(1)).displayed();
    }

    @Test
    public void aepMessageIsShown_When_RootViewIsNull() {
        // setup
        Mockito.when(mockMessageMonitor.isDisplayed()).thenReturn(false);
        Mockito.when(
                        mockFullscreenMessageDelegate.shouldShowMessage(
                                ArgumentMatchers.any(AEPMessage.class)))
                .thenReturn(true);
        Mockito.when(mockActivity.findViewById(ArgumentMatchers.anyInt()))
                .thenReturn(mockViewGroup);

        try {
            message =
                    new AEPMessage(
                            "html",
                            mockFullscreenMessageDelegate,
                            false,
                            mockMessageMonitor,
                            mockAEPMessageSettings);
        } catch (MessageCreationException ex) {
            Assert.fail(ex.getMessage());
        }
        message.rootViewGroup = null;
        // test
        message.show();
        // verify
        Mockito.verify(mockMessageMonitor, Mockito.times(1)).displayed();
    }

    @Test
    public void aepMessageIsNotShown_When_AnotherMessageIsDisplayed() {
        // setup
        Mockito.when(mockMessageMonitor.isDisplayed()).thenReturn(true);
        Mockito.when(
                        mockFullscreenMessageDelegate.shouldShowMessage(
                                ArgumentMatchers.any(AEPMessage.class)))
                .thenReturn(true);

        try {
            message =
                    new AEPMessage(
                            "html",
                            mockFullscreenMessageDelegate,
                            false,
                            mockMessageMonitor,
                            mockAEPMessageSettings);
        } catch (MessageCreationException ex) {
            Assert.fail(ex.getMessage());
        }

        // test
        message.show();
        // verify
        Mockito.verify(mockMessageMonitor, Mockito.times(0)).displayed();
        Mockito.verify(mockFullscreenMessageDelegate, Mockito.times(1)).onShowFailure();
    }

    @Test
    public void messageMonitorDismissedCalled_When_aepMessageDismissCalled() {
        // setup
        Mockito.when(mockMessageMonitor.isDisplayed()).thenReturn(false);
        Mockito.when(
                        mockFullscreenMessageDelegate.shouldShowMessage(
                                ArgumentMatchers.any(AEPMessage.class)))
                .thenReturn(true);
        Mockito.when(mockActivity.getFragmentManager()).thenReturn(mockFragmentManager);
        Mockito.when(mockFragmentManager.beginTransaction()).thenReturn(mockFragmentTransaction);
        Mockito.when(mockFragmentTransaction.remove(ArgumentMatchers.any(MessageFragment.class)))
                .thenReturn(mockFragmentTransaction);
        Mockito.when(mockFragmentManager.findFragmentByTag(ArgumentMatchers.anyString()))
                .thenReturn(mockMessageFragment);
        Mockito.when(mockAEPMessageSettings.getGestures()).thenReturn(gestureMap);
        Mockito.when(mockAEPMessageSettings.getDismissAnimation())
                .thenReturn(MessageAnimation.BOTTOM);

        try {
            message =
                    new AEPMessage(
                            "html",
                            mockFullscreenMessageDelegate,
                            false,
                            mockMessageMonitor,
                            mockAEPMessageSettings);
        } catch (MessageCreationException ex) {
            Assert.fail(ex.getMessage());
        }
        message.rootViewGroup = mockViewGroup;
        message.fragmentFrameLayout = mockFrameLayout;
        message.messageWebViewRunner = mockMessageWebViewRunner;
        message.webView = mockWebView;
        message.messageFragment = mockMessageFragment;
        mockMessageWebViewRunner.backdrop = mockViewGroup;

        // test
        message.dismiss();
        // simulate the dismiss animation ending
        message.getAnimationListener().onAnimationEnd(null);
        // verify message monitor dismiss called and the fragment is removed
        Mockito.verify(mockMessageMonitor, Mockito.times(1)).dismissed();
        Mockito.verify(mockFragmentManager, Mockito.times(1)).beginTransaction();
        Mockito.verify(mockFragmentTransaction, Mockito.times(1))
                .remove(ArgumentMatchers.any(MessageFragment.class));
        Mockito.verify(mockFragmentTransaction, Mockito.times(1)).commit();
    }

    @Test
    public void messageMonitorDisplayedCalled_When_aepMessageShown() {
        // setup
        Mockito.when(mockMessageMonitor.isDisplayed()).thenReturn(false);
        Mockito.when(
                        mockFullscreenMessageDelegate.shouldShowMessage(
                                ArgumentMatchers.any(AEPMessage.class)))
                .thenReturn(true);
        Mockito.when(mockActivity.findViewById(ArgumentMatchers.anyInt()))
                .thenReturn(mockViewGroup);
        Mockito.when(mockViewGroup.getMeasuredWidth()).thenReturn(1000);
        Mockito.when(mockViewGroup.getMeasuredHeight()).thenReturn(1000);

        try {
            message =
                    new AEPMessage(
                            "html",
                            mockFullscreenMessageDelegate,
                            false,
                            mockMessageMonitor,
                            mockAEPMessageSettings);
        } catch (MessageCreationException ex) {
            Assert.fail(ex.getMessage());
        }
        message.rootViewGroup = mockViewGroup;
        message.webView = mockWebView;
        message.messageFragment = mockMessageFragment;

        // test
        message.show();
        // verify
        Mockito.verify(mockMessageMonitor, Mockito.times(1)).displayed();
    }

    @Test
    public void aepMessageIsNotShown_When_ShouldShowMessageIsFalse() {
        // setup
        Mockito.when(mockMessageMonitor.isDisplayed()).thenReturn(false);
        Mockito.when(
                        mockFullscreenMessageDelegate.shouldShowMessage(
                                ArgumentMatchers.any(AEPMessage.class)))
                .thenReturn(false);
        Mockito.when(mockActivity.findViewById(ArgumentMatchers.anyInt()))
                .thenReturn(mockViewGroup);
        Mockito.when(mockViewGroup.getMeasuredWidth()).thenReturn(1000);
        Mockito.when(mockViewGroup.getMeasuredHeight()).thenReturn(1000);

        try {
            message =
                    new AEPMessage(
                            "html",
                            mockFullscreenMessageDelegate,
                            false,
                            mockMessageMonitor,
                            mockAEPMessageSettings);
        } catch (MessageCreationException ex) {
            Assert.fail(ex.getMessage());
        }

        message.rootViewGroup = mockViewGroup;
        message.webView = mockWebView;
        message.messageFragment = mockMessageFragment;

        // test
        message.show();
        // verify
        Mockito.verify(mockMessageMonitor, Mockito.times(0)).displayed();
    }

    @Test
    public void aepMessageIsNotShown_When_CurrentActivityIsNull() {
        // setup
        Mockito.when(mockMessageMonitor.isDisplayed()).thenReturn(false);
        Mockito.when(
                        mockFullscreenMessageDelegate.shouldShowMessage(
                                ArgumentMatchers.any(AEPMessage.class)))
                .thenReturn(true);

        // set the current activity to null
        Mockito.when(mockAppContextService.getCurrentActivity()).thenReturn(null);

        try {
            message =
                    new AEPMessage(
                            "html",
                            mockFullscreenMessageDelegate,
                            false,
                            mockMessageMonitor,
                            mockAEPMessageSettings);
        } catch (MessageCreationException ex) {
            Assert.fail(ex.getMessage());
        }
        message.rootViewGroup = mockViewGroup;
        message.webView = mockWebView;
        message.messageFragment = mockMessageFragment;

        // test
        message.show();
        // verify
        Mockito.verify(mockMessageMonitor, Mockito.times(0)).displayed();
        Mockito.verify(mockFullscreenMessageDelegate, Mockito.times(1)).onShowFailure();
    }

    // openUrl tests
    @Test
    public void urlOpened_When_ActivityIsNotNull() {
        // setup
        String url = "https://www.adobe.com";

        try {
            message =
                    new AEPMessage(
                            "html",
                            mockFullscreenMessageDelegate,
                            false,
                            mockMessageMonitor,
                            mockAEPMessageSettings);
        } catch (MessageCreationException ex) {
            Assert.fail(ex.getMessage());
        }
        message.rootViewGroup = mockViewGroup;
        message.webView = mockWebView;
        message.messageFragment = mockMessageFragment;

        // test
        message.openUrl(url);
        // verify
        Mockito.verify(mockActivity, Mockito.times(1))
                .startActivity(ArgumentMatchers.any(Intent.class));
    }

    @Test
    public void urlNotOpened_When_ActivityIsNull() {
        // setup
        Mockito.when(mockAppContextService.getCurrentActivity()).thenReturn(null);

        String url = "https://www.adobe.com";

        try {
            message =
                    new AEPMessage(
                            "html",
                            mockFullscreenMessageDelegate,
                            false,
                            mockMessageMonitor,
                            mockAEPMessageSettings);
        } catch (MessageCreationException ex) {
            Assert.fail(ex.getMessage());
        }

        message.rootViewGroup = mockViewGroup;
        message.webView = mockWebView;
        message.messageFragment = mockMessageFragment;

        // test
        message.openUrl(url);
        // verify
        Mockito.verify(mockActivity, Mockito.times(0))
                .startActivity(ArgumentMatchers.any(Intent.class));
    }

    @Test
    public void urlNotOpened_When_UrlStringIsEmpty() {
        // setup
        String url = "";

        try {
            message =
                    new AEPMessage(
                            "html",
                            mockFullscreenMessageDelegate,
                            false,
                            mockMessageMonitor,
                            mockAEPMessageSettings);
        } catch (MessageCreationException ex) {
            Assert.fail(ex.getMessage());
        }

        message.rootViewGroup = mockViewGroup;
        message.webView = mockWebView;
        message.messageFragment = mockMessageFragment;

        // test
        message.openUrl(url);
        // verify
        Mockito.verify(mockActivity, Mockito.times(0))
                .startActivity(ArgumentMatchers.any(Intent.class));
    }

    // AEMessage dismiss tests
    @Test
    public void aepMessageIsDismissed_When_MessageDismissCalled() {
        // setup
        mockMessageFragment.dismissedWithGesture = false;
        Mockito.when(mockAEPMessageSettings.getDismissAnimation())
                .thenReturn(MessageAnimation.BOTTOM);
        Mockito.when(mockMessageMonitor.isDisplayed()).thenReturn(false);
        Mockito.when(
                        mockFullscreenMessageDelegate.shouldShowMessage(
                                ArgumentMatchers.any(AEPMessage.class)))
                .thenReturn(true);
        Mockito.when(mockActivity.getFragmentManager()).thenReturn(mockFragmentManager);
        Mockito.when(mockFragmentManager.beginTransaction()).thenReturn(mockFragmentTransaction);
        Mockito.when(mockFragmentTransaction.remove(ArgumentMatchers.any(MessageFragment.class)))
                .thenReturn(mockFragmentTransaction);
        Mockito.when(mockFragmentManager.findFragmentByTag(ArgumentMatchers.anyString()))
                .thenReturn(mockMessageFragment);

        try {
            message =
                    new AEPMessage(
                            "html",
                            mockFullscreenMessageDelegate,
                            false,
                            mockMessageMonitor,
                            mockAEPMessageSettings);
        } catch (MessageCreationException ex) {
            Assert.fail(ex.getMessage());
        }

        message.rootViewGroup = mockViewGroup;
        message.messageFragment = mockMessageFragment;
        message.messageWebViewRunner = mockMessageWebViewRunner;
        mockMessageWebViewRunner.backdrop = mockBackdrop;
        message.webView = mockWebView;
        Mockito.when(mockViewGroup.getMeasuredWidth()).thenReturn(1000);
        Mockito.when(mockViewGroup.getMeasuredHeight()).thenReturn(1000);
        // test
        message.show();
        message.dismiss();
        message.getAnimationListener().onAnimationEnd(mockAnimation);
        // verify
        Mockito.verify(mockMessageMonitor, Mockito.times(1)).displayed();
        Mockito.verify(mockMessageMonitor, Mockito.times(1)).dismissed();
        Mockito.verify(mockWebView, Mockito.times(1))
                .startAnimation(ArgumentMatchers.any(Animation.class));
    }

    @Test
    public void aepMessageIsDismissed_When_MessageDismissedWithGesture() {
        // setup
        mockMessageFragment.dismissedWithGesture = true;
        Mockito.when(mockAEPMessageSettings.getDismissAnimation())
                .thenReturn(MessageAnimation.BOTTOM);
        Mockito.when(mockMessageMonitor.isDisplayed()).thenReturn(false);
        Mockito.when(
                        mockFullscreenMessageDelegate.shouldShowMessage(
                                ArgumentMatchers.any(AEPMessage.class)))
                .thenReturn(true);
        Mockito.when(mockActivity.getFragmentManager()).thenReturn(mockFragmentManager);
        Mockito.when(mockFragmentManager.beginTransaction()).thenReturn(mockFragmentTransaction);
        Mockito.when(mockFragmentTransaction.remove(ArgumentMatchers.any(MessageFragment.class)))
                .thenReturn(mockFragmentTransaction);
        Mockito.when(mockFragmentManager.findFragmentByTag(ArgumentMatchers.anyString()))
                .thenReturn(mockMessageFragment);

        try {
            message =
                    new AEPMessage(
                            "html",
                            mockFullscreenMessageDelegate,
                            false,
                            mockMessageMonitor,
                            mockAEPMessageSettings);
        } catch (MessageCreationException ex) {
            Assert.fail(ex.getMessage());
        }

        message.rootViewGroup = mockViewGroup;
        message.messageFragment = mockMessageFragment;
        message.messageWebViewRunner = mockMessageWebViewRunner;
        message.webView = mockWebView;
        mockMessageWebViewRunner.backdrop = mockBackdrop;
        Mockito.when(mockViewGroup.getMeasuredWidth()).thenReturn(1000);
        Mockito.when(mockViewGroup.getMeasuredHeight()).thenReturn(1000);
        // test
        message.show();
        message.dismiss();
        // verify
        Mockito.verify(mockMessageMonitor, Mockito.times(1)).displayed();
        Mockito.verify(mockMessageMonitor, Mockito.times(1)).dismissed();
        Mockito.verify(mockWebView, Mockito.times(0))
                .startAnimation(ArgumentMatchers.any(Animation.class));
    }
}
