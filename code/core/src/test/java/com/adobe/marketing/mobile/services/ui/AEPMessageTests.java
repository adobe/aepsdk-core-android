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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import com.adobe.marketing.mobile.services.AppContextService;
import com.adobe.marketing.mobile.services.MessagingDelegate;
import com.adobe.marketing.mobile.services.ServiceProviderModifier;
import com.adobe.marketing.mobile.services.ui.MessageSettings.MessageAnimation;
import com.adobe.marketing.mobile.services.ui.MessageSettings.MessageGesture;
import com.adobe.marketing.mobile.services.ui.internal.MessagesMonitor;
import java.util.HashMap;
import java.util.concurrent.Executor;
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

    @Mock private MessagingDelegate mockMessagingDelegate;

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

    @Mock private Executor mockExecutor;

    @Mock private Resources mockResources;

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
        ServiceProviderModifier.setMessagingDelegate(mockMessagingDelegate);
        Mockito.when(mockAppContextService.getApplicationContext())
                .thenReturn(mockApplicationContext);
        Mockito.when(mockApplicationContext.getResources()).thenReturn(mockResources);
        Mockito.when(mockAppContextService.getCurrentActivity()).thenReturn(mockActivity);

        // Actually run the executor runnable - mocking the executor.execute()
        doAnswer(
                        invocation -> {
                            Runnable r = invocation.getArgument(0);
                            r.run();
                            return null;
                        })
                .when(mockExecutor)
                .execute(any(Runnable.class));
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
                            mockAEPMessageSettings,
                            mockExecutor);
        } catch (final MessageCreationException ex) {
            Assert.fail(ex.getMessage());
        }

        // verify
        Assert.assertNotNull(message);
    }

    @Test(expected = MessageCreationException.class)
    public void testCreateAEPMessage_nullMessageDelegate() throws MessageCreationException {
        // test and verify
        message =
                new AEPMessage(
                        "html",
                        null,
                        false,
                        mockMessageMonitor,
                        mockAEPMessageSettings,
                        mockExecutor);
    }

    // AEPMessage show tests
    @Test
    public void aepMessageIsShown_When_NoOtherMessagesAreDisplayed() {
        // setup
        Mockito.when(mockMessageMonitor.show(any(FullscreenMessage.class), anyBoolean()))
                .thenCallRealMethod()
                .thenReturn(true);
        Mockito.when(mockMessageMonitor.isDisplayed()).thenReturn(false);
        Mockito.when(
                        mockMessagingDelegate.shouldShowMessage(
                                ArgumentMatchers.any(AEPMessage.class)))
                .thenReturn(true);

        try {
            message =
                    new AEPMessage(
                            "html",
                            mockFullscreenMessageDelegate,
                            false,
                            mockMessageMonitor,
                            mockAEPMessageSettings,
                            mockExecutor);
        } catch (MessageCreationException ex) {
            Assert.fail(ex.getMessage());
        }
        message.rootViewGroup = mockViewGroup;
        Mockito.when(mockViewGroup.getMeasuredWidth()).thenReturn(1000);
        Mockito.when(mockViewGroup.getMeasuredHeight()).thenReturn(1000);
        setupFragmentTransactionMocks();

        // test
        message.show();
        // verify
        Mockito.verify(mockMessageMonitor, Mockito.times(1))
                .show(any(FullscreenMessage.class), eq(true));
        Mockito.verify(mockMessageMonitor, Mockito.times(1)).displayed();
    }

    @Test
    public void aepMessageIsShown_When_RootViewIsNull() {
        // setup
        Mockito.when(mockMessageMonitor.show(any(FullscreenMessage.class), anyBoolean()))
                .thenCallRealMethod()
                .thenReturn(true);
        Mockito.when(mockMessageMonitor.isDisplayed()).thenReturn(false);
        Mockito.when(
                        mockMessagingDelegate.shouldShowMessage(
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
                            mockAEPMessageSettings,
                            mockExecutor);
        } catch (MessageCreationException ex) {
            Assert.fail(ex.getMessage());
        }
        message.rootViewGroup = null;
        setupFragmentTransactionMocks();

        // test
        message.show();
        // verify
        Mockito.verify(mockMessageMonitor, Mockito.times(1))
                .show(any(FullscreenMessage.class), eq(true));
        Mockito.verify(mockMessageMonitor, Mockito.times(1)).displayed();
    }

    @Test
    public void aepMessageIsNotShown_When_AnotherMessageIsDisplayed() {
        // setup
        Mockito.when(mockMessageMonitor.isDisplayed()).thenReturn(true);
        Mockito.when(
                        mockMessagingDelegate.shouldShowMessage(
                                ArgumentMatchers.any(AEPMessage.class)))
                .thenReturn(true);

        try {
            message =
                    new AEPMessage(
                            "html",
                            mockFullscreenMessageDelegate,
                            false,
                            mockMessageMonitor,
                            mockAEPMessageSettings,
                            mockExecutor);
        } catch (MessageCreationException ex) {
            Assert.fail(ex.getMessage());
        }
        message.rootViewGroup = mockViewGroup;
        Mockito.when(mockViewGroup.getMeasuredWidth()).thenReturn(1000);
        Mockito.when(mockViewGroup.getMeasuredHeight()).thenReturn(1000);
        setupFragmentTransactionMocks();

        // test
        message.show();
        // verify
        Mockito.verify(mockMessageMonitor, Mockito.times(1))
                .show(any(FullscreenMessage.class), eq(true));
        Mockito.verify(mockMessageMonitor, Mockito.times(0)).displayed();
    }

    @Test
    public void aepMessageIsNotShown_When_MessagingDelegateSet_And_ShouldShowMessageIsFalse() {
        // setup
        Mockito.when(mockMessageMonitor.show(any(FullscreenMessage.class), anyBoolean()))
                .thenCallRealMethod();
        Mockito.when(
                        mockMessagingDelegate.shouldShowMessage(
                                ArgumentMatchers.any(AEPMessage.class)))
                .thenReturn(false);
        Mockito.when(mockActivity.findViewById(ArgumentMatchers.anyInt()))
                .thenReturn(mockViewGroup);
        Mockito.when(mockViewGroup.getMeasuredWidth()).thenReturn(1000);
        Mockito.when(mockViewGroup.getMeasuredHeight()).thenReturn(1000);
        setupFragmentTransactionMocks();

        try {
            message =
                    new AEPMessage(
                            "html",
                            mockFullscreenMessageDelegate,
                            false,
                            mockMessageMonitor,
                            mockAEPMessageSettings,
                            mockExecutor);
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

    @Test
    public void aepMessageIsShown_When_NoMessageDelegateSet() {
        // setup
        ServiceProviderModifier.setMessagingDelegate(null);
        Mockito.when(mockMessageMonitor.isDisplayed()).thenReturn(false);
        Mockito.when(mockMessageMonitor.show(any(FullscreenMessage.class), anyBoolean()))
                .thenCallRealMethod();
        Mockito.when(mockActivity.findViewById(ArgumentMatchers.anyInt()))
                .thenReturn(mockViewGroup);
        Mockito.when(mockViewGroup.getMeasuredWidth()).thenReturn(1000);
        Mockito.when(mockViewGroup.getMeasuredHeight()).thenReturn(1000);
        setupFragmentTransactionMocks();

        try {
            message =
                    new AEPMessage(
                            "html",
                            mockFullscreenMessageDelegate,
                            false,
                            mockMessageMonitor,
                            mockAEPMessageSettings,
                            mockExecutor);
        } catch (MessageCreationException ex) {
            Assert.fail(ex.getMessage());
        }

        message.rootViewGroup = mockViewGroup;
        message.webView = mockWebView;
        message.messageFragment = mockMessageFragment;

        // test
        message.show(false);
        // verify
        Mockito.verify(mockMessageMonitor, Mockito.times(1)).displayed();
    }

    @Test
    public void aepMessageIsNotShown_When_CurrentActivityIsNull() {
        // setup
        Mockito.when(mockMessageMonitor.isDisplayed()).thenReturn(true);
        Mockito.when(
                        mockMessagingDelegate.shouldShowMessage(
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
                            mockAEPMessageSettings,
                            mockExecutor);
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
                            mockAEPMessageSettings,
                            mockExecutor);
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
                            mockAEPMessageSettings,
                            mockExecutor);
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
                            mockAEPMessageSettings,
                            mockExecutor);
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

    // AEPMessage dismiss tests
    @Test
    public void aepMessageIsDismissed_When_MessagingDelegateSet_And_MessageDismissCalled() {
        // setup
        mockMessageFragment.dismissedWithGesture = false;
        Mockito.when(mockAEPMessageSettings.getDismissAnimation())
                .thenReturn(MessageAnimation.BOTTOM);
        Mockito.when(mockMessageMonitor.isDisplayed()).thenReturn(true);
        Mockito.when(mockMessageMonitor.dismiss()).thenReturn(true);
        Mockito.when(
                        mockMessagingDelegate.shouldShowMessage(
                                ArgumentMatchers.any(AEPMessage.class)))
                .thenReturn(true);
        setupFragmentTransactionMocks();

        try {
            message =
                    new AEPMessage(
                            "html",
                            mockFullscreenMessageDelegate,
                            false,
                            mockMessageMonitor,
                            mockAEPMessageSettings,
                            mockExecutor);
        } catch (MessageCreationException ex) {
            Assert.fail(ex.getMessage());
        }

        message.rootViewGroup = mockViewGroup;
        message.fragmentFrameLayout = mockFrameLayout;
        message.messageFragment = mockMessageFragment;
        message.messageWebViewRunner = mockMessageWebViewRunner;
        mockMessageWebViewRunner.backdrop = mockBackdrop;
        message.webView = mockWebView;
        message.setVisible(true);
        Mockito.when(mockViewGroup.getMeasuredWidth()).thenReturn(1000);
        Mockito.when(mockViewGroup.getMeasuredHeight()).thenReturn(1000);
        // test
        message.dismiss();
        message.getAnimationListener().onAnimationEnd(mockAnimation);
        // verify listeners are called for a message dismiss
        Mockito.verify(mockMessageMonitor, Mockito.times(1)).dismiss();
        Mockito.verify(mockMessagingDelegate, Mockito.times(1))
                .onDismiss(any(FullscreenMessage.class));
        Mockito.verify(mockFullscreenMessageDelegate, Mockito.times(1))
                .onDismiss(any(FullscreenMessage.class));
        Mockito.verify(mockWebView, Mockito.times(1))
                .startAnimation(ArgumentMatchers.any(Animation.class));
    }

    @Test
    public void aepMessageIsDismissed_When_NoMessagingDelegateSet_And_MessageDismissCalled() {
        // setup
        ServiceProviderModifier.setMessagingDelegate(null);
        mockMessageFragment.dismissedWithGesture = false;
        Mockito.when(mockAEPMessageSettings.getDismissAnimation())
                .thenReturn(MessageAnimation.BOTTOM);
        Mockito.when(mockMessageMonitor.show(any(FullscreenMessage.class), anyBoolean()))
                .thenReturn(true);
        Mockito.when(mockMessageMonitor.dismiss()).thenReturn(true);
        setupFragmentTransactionMocks();

        try {
            message =
                    new AEPMessage(
                            "html",
                            mockFullscreenMessageDelegate,
                            false,
                            mockMessageMonitor,
                            mockAEPMessageSettings,
                            mockExecutor);
        } catch (MessageCreationException ex) {
            Assert.fail(ex.getMessage());
        }

        message.rootViewGroup = mockViewGroup;
        message.fragmentFrameLayout = mockFrameLayout;
        message.messageFragment = mockMessageFragment;
        message.messageWebViewRunner = mockMessageWebViewRunner;
        mockMessageWebViewRunner.backdrop = mockBackdrop;
        message.webView = mockWebView;
        message.setVisible(true);
        Mockito.when(mockViewGroup.getMeasuredWidth()).thenReturn(1000);
        Mockito.when(mockViewGroup.getMeasuredHeight()).thenReturn(1000);
        // test
        message.dismiss();
        message.getAnimationListener().onAnimationEnd(mockAnimation);
        // verify listeners except for the messaging delegate are called for a message dismiss
        Mockito.verify(mockMessageMonitor, Mockito.times(1)).dismiss();
        Mockito.verifyNoInteractions(mockMessagingDelegate);
        Mockito.verify(mockFullscreenMessageDelegate, Mockito.times(1))
                .onDismiss(any(FullscreenMessage.class));
        Mockito.verify(mockWebView, Mockito.times(1))
                .startAnimation(ArgumentMatchers.any(Animation.class));
    }

    @Test
    public void aepMessageIsDismissed_When_MessagingDelegateSet_And_MessageDismissedWithGesture() {
        // setup
        mockMessageFragment.dismissedWithGesture = true;
        Mockito.when(mockAEPMessageSettings.getDismissAnimation())
                .thenReturn(MessageAnimation.BOTTOM);
        Mockito.when(mockMessageMonitor.show(any(FullscreenMessage.class), anyBoolean()))
                .thenReturn(true);
        Mockito.when(mockMessageMonitor.dismiss()).thenReturn(true);
        Mockito.when(mockMessageMonitor.isDisplayed()).thenReturn(true);
        Mockito.when(
                        mockMessagingDelegate.shouldShowMessage(
                                ArgumentMatchers.any(AEPMessage.class)))
                .thenReturn(true);
        setupFragmentTransactionMocks();

        try {
            message =
                    new AEPMessage(
                            "html",
                            mockFullscreenMessageDelegate,
                            false,
                            mockMessageMonitor,
                            mockAEPMessageSettings,
                            mockExecutor);
        } catch (MessageCreationException ex) {
            Assert.fail(ex.getMessage());
        }

        message.rootViewGroup = mockViewGroup;
        message.fragmentFrameLayout = mockFrameLayout;
        message.messageWebViewRunner = mockMessageWebViewRunner;
        message.messageFragment = mockMessageFragment;
        mockMessageWebViewRunner.backdrop = mockBackdrop;
        message.webView = mockWebView;
        message.setVisible(true);
        Mockito.when(mockViewGroup.getMeasuredWidth()).thenReturn(1000);
        Mockito.when(mockViewGroup.getMeasuredHeight()).thenReturn(1000);
        // test
        message.dismiss();
        // verify listeners are called for a message dismiss
        Mockito.verify(mockMessageMonitor, Mockito.times(1)).dismiss();
        Mockito.verify(mockMessagingDelegate, Mockito.times(1))
                .onDismiss(any(FullscreenMessage.class));
        Mockito.verify(mockFullscreenMessageDelegate, Mockito.times(1))
                .onDismiss(any(FullscreenMessage.class));
    }

    @Test
    public void
            aepMessageIsDismissed_When_NoMessagingDelegateSet_And_MessageDismissedWithGesture() {
        // setup
        ServiceProviderModifier.setMessagingDelegate(null);
        mockMessageFragment.dismissedWithGesture = true;
        Mockito.when(mockAEPMessageSettings.getDismissAnimation())
                .thenReturn(MessageAnimation.BOTTOM);
        Mockito.when(mockMessageMonitor.show(any(FullscreenMessage.class), anyBoolean()))
                .thenCallRealMethod()
                .thenReturn(true);
        Mockito.when(mockMessageMonitor.show(any(FullscreenMessage.class), anyBoolean()))
                .thenReturn(true);
        Mockito.when(mockMessageMonitor.dismiss()).thenReturn(true);
        setupFragmentTransactionMocks();

        try {
            message =
                    new AEPMessage(
                            "html",
                            mockFullscreenMessageDelegate,
                            false,
                            mockMessageMonitor,
                            mockAEPMessageSettings,
                            mockExecutor);
        } catch (MessageCreationException ex) {
            Assert.fail(ex.getMessage());
        }

        message.rootViewGroup = mockViewGroup;
        message.fragmentFrameLayout = mockFrameLayout;
        message.messageWebViewRunner = mockMessageWebViewRunner;
        message.messageFragment = mockMessageFragment;
        mockMessageWebViewRunner.backdrop = mockBackdrop;
        message.webView = mockWebView;
        message.setVisible(true);
        Mockito.when(mockViewGroup.getMeasuredWidth()).thenReturn(1000);
        Mockito.when(mockViewGroup.getMeasuredHeight()).thenReturn(1000);
        // test
        message.dismiss();
        // verify listeners except for the messaging delegate are called for a message dismiss
        Mockito.verify(mockMessageMonitor, Mockito.times(1)).dismiss();
        Mockito.verifyNoInteractions(mockMessagingDelegate);
        Mockito.verify(mockFullscreenMessageDelegate, Mockito.times(1))
                .onDismiss(any(FullscreenMessage.class));
        Mockito.verify(mockWebView, Mockito.times(0))
                .startAnimation(ArgumentMatchers.any(Animation.class));
    }

    // mock fragment setup helper
    void setupFragmentTransactionMocks() {
        Mockito.when(mockActivity.getFragmentManager()).thenReturn(mockFragmentManager);
        Mockito.when(mockFragmentManager.beginTransaction()).thenReturn(mockFragmentTransaction);
        Mockito.when(mockFragmentTransaction.remove(ArgumentMatchers.any(MessageFragment.class)))
                .thenReturn(mockFragmentTransaction);
        Mockito.when(mockFragmentManager.findFragmentByTag(ArgumentMatchers.anyString()))
                .thenReturn(mockMessageFragment);
        Mockito.when(
                        mockFragmentTransaction.replace(
                                anyInt(), any(MessageFragment.class), anyString()))
                .thenReturn(mockFragmentTransaction);
        Mockito.when(mockFragmentTransaction.addToBackStack(isNull()))
                .thenReturn(mockFragmentTransaction);
        Mockito.when(mockFragmentTransaction.commit()).thenReturn(123);
    }
}
