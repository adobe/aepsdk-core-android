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

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import android.app.Application;
import android.content.Context;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.widget.FrameLayout;
import com.adobe.marketing.mobile.services.AppContextService;
import com.adobe.marketing.mobile.services.ServiceProviderModifier;
import com.adobe.marketing.mobile.services.ui.MessageSettings.MessageAlignment;
import com.adobe.marketing.mobile.services.ui.MessageSettings.MessageAnimation;
import com.adobe.marketing.mobile.services.ui.MessageSettings.MessageGesture;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class MessageWebViewRunnerTests {

    @Mock private AEPMessage mockAEPMessage;

    @Mock private MessageFragment mockMessageFragment;

    @Mock private ViewGroup mockViewGroup;

    @Mock private FrameLayout mockFrameLayout;

    @Mock private WebSettings mockWebSettings;

    @Mock private Context mockContext;

    @Mock private AppContextService mockAppContextService;

    @Mock private Application mockApp;

    private MessageWebViewRunner messageFragmentRunner;
    private MessageSettings aepMessageSettings;

    private HashMap<MessageGesture, String> gestureMap = new HashMap<>();
    private MessageWebView mockMessageWebview;

    @Before
    public void setup() throws Exception {
        ServiceProviderModifier.setAppContextService(mockAppContextService);
        when(mockAppContextService.getApplicationContext()).thenReturn(mockContext);
        gestureMap.put(MessageGesture.BACKGROUND_TAP, "adbinapp://dismiss");
        gestureMap.put(MessageGesture.SWIPE_LEFT, "adbinapp://dismiss?interaction=negative");
        gestureMap.put(MessageGesture.SWIPE_RIGHT, "adbinapp://dismiss?interaction=positive");
        gestureMap.put(MessageGesture.SWIPE_UP, "adbinapp://dismiss");
        gestureMap.put(MessageGesture.SWIPE_DOWN, "adbinapp://dismiss");
        aepMessageSettings = new MessageSettings();
        aepMessageSettings.setWidth(100);
        aepMessageSettings.setHeight(100);
        aepMessageSettings.setBackdropColor("808080");
        aepMessageSettings.setBackdropOpacity(0.5f);
        aepMessageSettings.setCornerRadius(70.0f);
        aepMessageSettings.setDismissAnimation(MessageAnimation.FADE);
        aepMessageSettings.setDisplayAnimation(MessageAnimation.CENTER);
        aepMessageSettings.setGestures(gestureMap);
        aepMessageSettings.setHorizontalAlign(MessageAlignment.CENTER);
        aepMessageSettings.setHorizontalInset(5);
        aepMessageSettings.setVerticalAlign(MessageAlignment.TOP);
        aepMessageSettings.setVerticalInset(10);
        when(mockAEPMessage.getSettings()).thenReturn(aepMessageSettings);
        when(mockAEPMessage.getMessageFragment()).thenReturn(mockMessageFragment);
        when(mockAEPMessage.getMessageHtml()).thenReturn("some html");

        when(mockViewGroup.getWidth()).thenReturn(200);
        when(mockViewGroup.getHeight()).thenReturn(400);
        mockAEPMessage.rootViewGroup = mockViewGroup;
        mockAEPMessage.fragmentFrameLayout = mockFrameLayout;
        mockMessageWebview = null;
    }

    @Test
    public void testRunnable_WithValidAEPMessage_ThenMessageShown() {
        try (MockedConstruction<MessageWebView> constructionMock =
                mockConstruction(
                        MessageWebView.class,
                        (mock, context) -> {
                            when(mock.getSettings()).thenReturn(mockWebSettings);
                            mockMessageWebview = mock;
                        })) {
            // test
            messageFragmentRunner = new MessageWebViewRunner(mockAEPMessage);
            messageFragmentRunner.run();
            // verify
            Mockito.verify(mockMessageWebview, Mockito.times(1))
                    .setOnTouchListener(mockMessageFragment);
            Mockito.verify(mockViewGroup, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
            Mockito.verify(mockAEPMessage, Mockito.times(1)).isMessageVisible();
            Mockito.verify(mockMessageWebview, Mockito.times(1)).getSettings();
            Mockito.verify(mockAEPMessage, Mockito.times(1)).viewed();
        }
    }

    @Test
    public void
            testRunnable_WithValidAEPMessage_And_MessageAnimationStartsFromTop_ThenMessageShown() {
        try (MockedConstruction<MessageWebView> constructionMock =
                mockConstruction(
                        MessageWebView.class,
                        (mock, context) -> {
                            when(mock.getSettings()).thenReturn(mockWebSettings);
                            mockMessageWebview = mock;
                        })) {
            // setup
            aepMessageSettings.setDisplayAnimation(MessageAnimation.TOP);
            when(mockAEPMessage.getSettings()).thenReturn(aepMessageSettings);
            messageFragmentRunner = new MessageWebViewRunner(mockAEPMessage);
            // test
            messageFragmentRunner.run();
            // verify
            Mockito.verify(mockMessageWebview, Mockito.times(1))
                    .setOnTouchListener(mockMessageFragment);
            Mockito.verify(mockViewGroup, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
            Mockito.verify(mockAEPMessage, Mockito.times(1)).isMessageVisible();
            Mockito.verify(mockMessageWebview, Mockito.times(1)).getSettings();
            Mockito.verify(mockAEPMessage, Mockito.times(1)).viewed();
        }
    }

    @Test
    public void
            testRunnable_WithValidAEPMessage_And_MessageAnimationStartsFromLeft_ThenMessageShown() {
        try (MockedConstruction<MessageWebView> constructionMock =
                mockConstruction(
                        MessageWebView.class,
                        (mock, context) -> {
                            when(mock.getSettings()).thenReturn(mockWebSettings);
                            mockMessageWebview = mock;
                        })) {
            // setup
            aepMessageSettings.setDisplayAnimation(MessageAnimation.LEFT);
            when(mockAEPMessage.getSettings()).thenReturn(aepMessageSettings);
            messageFragmentRunner = new MessageWebViewRunner(mockAEPMessage);
            // test
            messageFragmentRunner.run();
            // verify
            Mockito.verify(mockMessageWebview, Mockito.times(1))
                    .setOnTouchListener(mockMessageFragment);
            Mockito.verify(mockViewGroup, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
            Mockito.verify(mockAEPMessage, Mockito.times(1)).isMessageVisible();
            Mockito.verify(mockMessageWebview, Mockito.times(1)).getSettings();
            Mockito.verify(mockAEPMessage, Mockito.times(1)).viewed();
        }
    }

    @Test
    public void
            testRunnable_WithValidAEPMessage_And_MessageAnimationStartsFromRight_ThenMessageShown() {
        try (MockedConstruction<MessageWebView> constructionMock =
                mockConstruction(
                        MessageWebView.class,
                        (mock, context) -> {
                            when(mock.getSettings()).thenReturn(mockWebSettings);
                            mockMessageWebview = mock;
                        })) {
            // setup
            aepMessageSettings.setDisplayAnimation(MessageAnimation.RIGHT);
            when(mockAEPMessage.getSettings()).thenReturn(aepMessageSettings);
            messageFragmentRunner = new MessageWebViewRunner(mockAEPMessage);
            // test
            messageFragmentRunner.run();
            // verify
            Mockito.verify(mockMessageWebview, Mockito.times(1))
                    .setOnTouchListener(mockMessageFragment);
            Mockito.verify(mockViewGroup, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
            Mockito.verify(mockAEPMessage, Mockito.times(1)).isMessageVisible();
            Mockito.verify(mockMessageWebview, Mockito.times(1)).getSettings();
            Mockito.verify(mockAEPMessage, Mockito.times(1)).viewed();
        }
    }

    @Test
    public void
            testRunnable_WithValidAEPMessage_And_MessageAnimationStartsFromBottom_ThenMessageShown() {
        try (MockedConstruction<MessageWebView> constructionMock =
                mockConstruction(
                        MessageWebView.class,
                        (mock, context) -> {
                            when(mock.getSettings()).thenReturn(mockWebSettings);
                            mockMessageWebview = mock;
                        })) {
            // setup
            aepMessageSettings.setDisplayAnimation(MessageAnimation.BOTTOM);
            when(mockAEPMessage.getSettings()).thenReturn(aepMessageSettings);
            messageFragmentRunner = new MessageWebViewRunner(mockAEPMessage);
            // test
            messageFragmentRunner.run();
            // verify
            Mockito.verify(mockMessageWebview, Mockito.times(1))
                    .setOnTouchListener(mockMessageFragment);
            Mockito.verify(mockViewGroup, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
            Mockito.verify(mockAEPMessage, Mockito.times(1)).isMessageVisible();
            Mockito.verify(mockMessageWebview, Mockito.times(1)).getSettings();
            Mockito.verify(mockAEPMessage, Mockito.times(1)).viewed();
        }
    }

    @Test
    public void testRunnable_WithValidAEPMessage_And_MessageAnimationFadesIn_ThenMessageShown() {
        try (MockedConstruction<MessageWebView> constructionMock =
                mockConstruction(
                        MessageWebView.class,
                        (mock, context) -> {
                            when(mock.getSettings()).thenReturn(mockWebSettings);
                            mockMessageWebview = mock;
                        })) {
            // setup
            aepMessageSettings.setDisplayAnimation(MessageAnimation.FADE);
            when(mockAEPMessage.getSettings()).thenReturn(aepMessageSettings);
            messageFragmentRunner = new MessageWebViewRunner(mockAEPMessage);
            // test
            messageFragmentRunner.run();
            // verify
            Mockito.verify(mockMessageWebview, Mockito.times(1))
                    .setOnTouchListener(mockMessageFragment);
            Mockito.verify(mockViewGroup, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
            Mockito.verify(mockAEPMessage, Mockito.times(1)).isMessageVisible();
            Mockito.verify(mockMessageWebview, Mockito.times(1)).getSettings();
            Mockito.verify(mockAEPMessage, Mockito.times(1)).viewed();
        }
    }

    @Test
    public void testRunnable_WithValidAEPMessage_And_NoMessageAnimation_ThenMessageShown() {
        try (MockedConstruction<MessageWebView> constructionMock =
                mockConstruction(
                        MessageWebView.class,
                        (mock, context) -> {
                            when(mock.getSettings()).thenReturn(mockWebSettings);
                            mockMessageWebview = mock;
                        })) {
            // setup
            aepMessageSettings.setDisplayAnimation(MessageAnimation.NONE);
            when(mockAEPMessage.getSettings()).thenReturn(aepMessageSettings);
            messageFragmentRunner = new MessageWebViewRunner(mockAEPMessage);
            // test
            messageFragmentRunner.run();
            // verify
            Mockito.verify(mockMessageWebview, Mockito.times(1))
                    .setOnTouchListener(mockMessageFragment);
            Mockito.verify(mockViewGroup, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
            Mockito.verify(mockAEPMessage, Mockito.times(1)).isMessageVisible();
            Mockito.verify(mockMessageWebview, Mockito.times(1)).getSettings();
            Mockito.verify(mockAEPMessage, Mockito.times(1)).viewed();
        }
    }

    @Test
    public void testRunnable_WithValidAEPMessage_And_NonFullscreenMessage_ThenMessageShown() {
        try (MockedConstruction<MessageWebView> constructionMock =
                mockConstruction(
                        MessageWebView.class,
                        (mock, context) -> {
                            when(mock.getSettings()).thenReturn(mockWebSettings);
                            mockMessageWebview = mock;
                        })) {
            // setup
            aepMessageSettings.setHeight(50);
            when(mockAEPMessage.getSettings()).thenReturn(aepMessageSettings);
            messageFragmentRunner = new MessageWebViewRunner(mockAEPMessage);
            // test
            messageFragmentRunner.run();
            // verify
            Mockito.verify(mockMessageWebview, Mockito.times(1))
                    .setOnTouchListener(mockMessageFragment);
            Mockito.verify(mockViewGroup, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
            Mockito.verify(mockAEPMessage, Mockito.times(1)).isMessageVisible();
            Mockito.verify(mockMessageWebview, Mockito.times(1)).getSettings();
            Mockito.verify(mockAEPMessage, Mockito.times(1)).viewed();
        }
    }

    @Test
    public void testRunnable_WithValidAEPMessage_And_MessageHorizontalAlignLeftThenMessageShown() {
        try (MockedConstruction<MessageWebView> constructionMock =
                mockConstruction(
                        MessageWebView.class,
                        (mock, context) -> {
                            when(mock.getSettings()).thenReturn(mockWebSettings);
                            mockMessageWebview = mock;
                        })) {
            // setup
            aepMessageSettings.setHorizontalAlign(MessageAlignment.LEFT);
            when(mockAEPMessage.getSettings()).thenReturn(aepMessageSettings);
            messageFragmentRunner = new MessageWebViewRunner(mockAEPMessage);
            // test
            messageFragmentRunner.run();
            // verify
            Mockito.verify(mockMessageWebview, Mockito.times(1))
                    .setOnTouchListener(mockMessageFragment);
            Mockito.verify(mockViewGroup, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
            Mockito.verify(mockAEPMessage, Mockito.times(1)).isMessageVisible();
            Mockito.verify(mockMessageWebview, Mockito.times(1)).getSettings();
            Mockito.verify(mockAEPMessage, Mockito.times(1)).viewed();
        }
    }

    @Test
    public void testRunnable_WithValidAEPMessage_And_MessageHorizontalAlignRightThenMessageShown() {
        try (MockedConstruction<MessageWebView> constructionMock =
                mockConstruction(
                        MessageWebView.class,
                        (mock, context) -> {
                            when(mock.getSettings()).thenReturn(mockWebSettings);
                            mockMessageWebview = mock;
                        })) {
            // setup
            aepMessageSettings.setHorizontalAlign(MessageAlignment.RIGHT);
            when(mockAEPMessage.getSettings()).thenReturn(aepMessageSettings);
            messageFragmentRunner = new MessageWebViewRunner(mockAEPMessage);
            // test
            messageFragmentRunner.run();
            // verify
            Mockito.verify(mockMessageWebview, Mockito.times(1))
                    .setOnTouchListener(mockMessageFragment);
            Mockito.verify(mockViewGroup, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
            Mockito.verify(mockAEPMessage, Mockito.times(1)).isMessageVisible();
            Mockito.verify(mockMessageWebview, Mockito.times(1)).getSettings();
            Mockito.verify(mockAEPMessage, Mockito.times(1)).viewed();
        }
    }

    @Test
    public void testRunnable_WithValidAEPMessage_And_MessageVerticalAlignBottomThenMessageShown() {
        try (MockedConstruction<MessageWebView> constructionMock =
                mockConstruction(
                        MessageWebView.class,
                        (mock, context) -> {
                            when(mock.getSettings()).thenReturn(mockWebSettings);
                            mockMessageWebview = mock;
                        })) {
            // setup
            aepMessageSettings.setVerticalAlign(MessageAlignment.BOTTOM);
            when(mockAEPMessage.getSettings()).thenReturn(aepMessageSettings);
            messageFragmentRunner = new MessageWebViewRunner(mockAEPMessage);
            // test
            messageFragmentRunner.run();
            // verify
            Mockito.verify(mockMessageWebview, Mockito.times(1))
                    .setOnTouchListener(mockMessageFragment);
            Mockito.verify(mockViewGroup, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
            Mockito.verify(mockAEPMessage, Mockito.times(1)).isMessageVisible();
            Mockito.verify(mockMessageWebview, Mockito.times(1)).getSettings();
            Mockito.verify(mockAEPMessage, Mockito.times(1)).viewed();
        }
    }

    @Test
    public void testRunnable_WithValidAEPMessage_And_MessageVerticalAlignCenterThenMessageShown() {
        try (MockedConstruction<MessageWebView> constructionMock =
                mockConstruction(
                        MessageWebView.class,
                        (mock, context) -> {
                            when(mock.getSettings()).thenReturn(mockWebSettings);
                            mockMessageWebview = mock;
                        })) {
            // setup
            aepMessageSettings.setVerticalAlign(MessageAlignment.CENTER);
            when(mockAEPMessage.getSettings()).thenReturn(aepMessageSettings);
            messageFragmentRunner = new MessageWebViewRunner(mockAEPMessage);
            // test
            messageFragmentRunner.run();
            // verify
            Mockito.verify(mockMessageWebview, Mockito.times(1))
                    .setOnTouchListener(mockMessageFragment);
            Mockito.verify(mockViewGroup, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
            Mockito.verify(mockAEPMessage, Mockito.times(1)).isMessageVisible();
            Mockito.verify(mockMessageWebview, Mockito.times(1)).getSettings();
            Mockito.verify(mockAEPMessage, Mockito.times(1)).viewed();
        }
    }

    @Test
    public void testRunnable_WithValidAEPMessage_And_BuildVersionLessThanAPI17_ThenMessageShown() {
        try (MockedConstruction<MessageWebView> constructionMock =
                mockConstruction(
                        MessageWebView.class,
                        (mock, context) -> {
                            when(mock.getSettings()).thenReturn(mockWebSettings);
                            mockMessageWebview = mock;
                        })) {
            // setup
            messageFragmentRunner = new MessageWebViewRunner(mockAEPMessage);
            // test
            messageFragmentRunner.run();
            // verify
            Mockito.verify(mockMessageWebview, Mockito.times(1))
                    .setOnTouchListener(mockMessageFragment);
            Mockito.verify(mockViewGroup, Mockito.times(1)).setOnTouchListener(mockMessageFragment);
            Mockito.verify(mockAEPMessage, Mockito.times(1)).isMessageVisible();
            Mockito.verify(mockMessageWebview, Mockito.times(1)).getSettings();
            Mockito.verify(mockAEPMessage, Mockito.times(1)).viewed();
        }
    }

    @Test
    public void testRunnable_WithValidAEPMessage_And_NullRootViewGroup_ThenMessageNotShown() {
        try (MockedConstruction<MessageWebView> constructionMock =
                mockConstruction(
                        MessageWebView.class,
                        (mock, context) -> {
                            when(mock.getSettings()).thenReturn(mockWebSettings);
                            mockMessageWebview = mock;
                        })) {
            // setup
            mockAEPMessage.rootViewGroup = null;
            messageFragmentRunner = new MessageWebViewRunner(mockAEPMessage);
            // test
            messageFragmentRunner.run();
            // verify
            assertNull(mockMessageWebview);
            Mockito.verify(mockViewGroup, Mockito.times(0)).setOnTouchListener(mockMessageFragment);
            Mockito.verify(mockAEPMessage, Mockito.times(0)).isMessageVisible();
            Mockito.verify(mockAEPMessage, Mockito.times(1)).cleanup();
        }
    }

    @Test
    public void
            testRunnable_WithValidAEPMessage_And_RootviewHasWidthEqualToZero_ThenMessageNotShown() {
        try (MockedConstruction<MessageWebView> constructionMock =
                mockConstruction(
                        MessageWebView.class,
                        (mock, context) -> {
                            when(mock.getSettings()).thenReturn(mockWebSettings);
                            mockMessageWebview = mock;
                        })) {
            // setup
            when(mockViewGroup.getWidth()).thenReturn(0);
            messageFragmentRunner = new MessageWebViewRunner(mockAEPMessage);
            // test
            messageFragmentRunner.run();
            // verify
            assertNull(mockMessageWebview);
            Mockito.verify(mockViewGroup, Mockito.times(0)).setOnTouchListener(mockMessageFragment);
            Mockito.verify(mockAEPMessage, Mockito.times(0)).isMessageVisible();
            Mockito.verify(mockAEPMessage, Mockito.times(1)).cleanup();
        }
    }

    @Test
    public void
            testRunnable_WithValidAEPMessage_And_RootviewHasHeightEqualToZero_ThenMessageNotShown() {
        try (MockedConstruction<MessageWebView> constructionMock =
                mockConstruction(
                        MessageWebView.class,
                        (mock, context) -> {
                            when(mock.getSettings()).thenReturn(mockWebSettings);
                            mockMessageWebview = mock;
                        })) {
            // setup
            when(mockViewGroup.getHeight()).thenReturn(0);
            messageFragmentRunner = new MessageWebViewRunner(mockAEPMessage);
            // test
            messageFragmentRunner.run();
            // verify
            assertNull(mockMessageWebview);
            Mockito.verify(mockViewGroup, Mockito.times(0)).setOnTouchListener(mockMessageFragment);
            Mockito.verify(mockAEPMessage, Mockito.times(0)).isMessageVisible();
            Mockito.verify(mockAEPMessage, Mockito.times(1)).cleanup();
        }
    }

    @Test
    public void testRunnable_WithInvalidAEPMessage_ThenMessageNotShown() {
        try (MockedConstruction<MessageWebView> constructionMock =
                mockConstruction(
                        MessageWebView.class,
                        (mock, context) -> {
                            when(mock.getSettings()).thenReturn(mockWebSettings);
                            mockMessageWebview = mock;
                        })) {
            // setup
            when(mockAEPMessage.getMessageHtml()).thenReturn(null);
            messageFragmentRunner = new MessageWebViewRunner(mockAEPMessage);
            // test
            messageFragmentRunner.run();
            // verify
            assertNull(mockMessageWebview);
            Mockito.verify(mockViewGroup, Mockito.times(0)).setOnTouchListener(mockMessageFragment);
            Mockito.verify(mockAEPMessage, Mockito.times(0)).isMessageVisible();
            Mockito.verify(mockAEPMessage, Mockito.times(1)).cleanup();
        }
    }
}
