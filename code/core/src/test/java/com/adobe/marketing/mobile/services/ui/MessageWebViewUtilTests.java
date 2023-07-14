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
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import androidx.cardview.widget.CardView;
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
public class MessageWebViewUtilTests {

    @Mock private AEPMessage mockAEPMessage;

    @Mock private MessageFragment mockMessageFragment;

    @Mock private WebView mockWebview;

    @Mock private CardView mockCardView;

    @Mock private WebSettings mockWebSettings;

    @Mock private Context mockContext;

    @Mock private AppContextService mockAppContextService;

    @Mock private Resources mockResources;

    @Mock private DisplayMetrics mockDisplayMetrics;

    private MessageWebViewUtil messageWebViewUtil;
    private MessageSettings aepMessageSettings;

    private HashMap<MessageGesture, String> gestureMap = new HashMap<>();

    @Before
    public void setup() throws Exception {
        ServiceProviderModifier.setAppContextService(mockAppContextService);
        when(mockResources.getDisplayMetrics()).thenReturn(mockDisplayMetrics);
        when(mockContext.getResources()).thenReturn(mockResources);
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
        when(mockAEPMessage.getMessageSettings()).thenReturn(aepMessageSettings);
        when(mockAEPMessage.getMessageFragment()).thenReturn(mockMessageFragment);
        when(mockAEPMessage.getMessageHtml()).thenReturn("some html");
        when(mockWebview.getSettings()).thenReturn(mockWebSettings);
        when(mockAEPMessage.getWebView()).thenReturn(mockWebview);
        when(mockAEPMessage.getWebViewFrame()).thenReturn(mockCardView);
    }

    @Test
    public void testRunnable_WithValidAEPMessage_ThenWebviewLoadDataCalled() {
        // test
        try (MockedConstruction<CardView> ignored = mockConstruction(CardView.class)) {
            messageWebViewUtil = new MessageWebViewUtil();
            messageWebViewUtil.show(mockAEPMessage);
            // verify
            Mockito.verify(mockWebview, Mockito.times(1)).getSettings();
            Mockito.verify(mockWebview, Mockito.times(1))
                    .loadDataWithBaseURL(any(), any(), any(), any(), any());
            Mockito.verify(mockWebview, Mockito.times(1))
                    .setOnTouchListener(any(MessageFragment.class));
            Mockito.verify(mockAEPMessage, Mockito.times(1))
                    .setParams(any(FrameLayout.LayoutParams.class));
        }
    }

    @Test
    public void
            testRunnable_WithValidAEPMessage_And_MessageAnimationStartsFromTop_ThenWebviewLoadDataCalled() {
        // setup
        try (MockedConstruction<CardView> ignored = mockConstruction(CardView.class)) {
            aepMessageSettings.setDisplayAnimation(MessageAnimation.TOP);
            when(mockAEPMessage.getMessageSettings()).thenReturn(aepMessageSettings);
            messageWebViewUtil = new MessageWebViewUtil();
            // test
            messageWebViewUtil.show(mockAEPMessage);
            // verify
            Mockito.verify(mockWebview, Mockito.times(1)).getSettings();
            Mockito.verify(mockWebview, Mockito.times(1))
                    .loadDataWithBaseURL(any(), any(), any(), any(), any());
            Mockito.verify(mockWebview, Mockito.times(1))
                    .setOnTouchListener(any(MessageFragment.class));
            Mockito.verify(mockAEPMessage, Mockito.times(1))
                    .setParams(any(FrameLayout.LayoutParams.class));
        }
    }

    @Test
    public void
            testRunnable_WithValidAEPMessage_And_MessageAnimationStartsFromLeft_ThenWebviewLoadDataCalled() {
        // setup
        try (MockedConstruction<CardView> ignored = mockConstruction(CardView.class)) {
            aepMessageSettings.setDisplayAnimation(MessageAnimation.LEFT);
            when(mockAEPMessage.getMessageSettings()).thenReturn(aepMessageSettings);
            messageWebViewUtil = new MessageWebViewUtil();
            // test
            messageWebViewUtil.show(mockAEPMessage);
            // verify
            Mockito.verify(mockWebview, Mockito.times(1)).getSettings();
            Mockito.verify(mockWebview, Mockito.times(1))
                    .loadDataWithBaseURL(any(), any(), any(), any(), any());
            Mockito.verify(mockWebview, Mockito.times(1))
                    .setOnTouchListener(any(MessageFragment.class));
            Mockito.verify(mockAEPMessage, Mockito.times(1))
                    .setParams(any(FrameLayout.LayoutParams.class));
        }
    }

    @Test
    public void
            testRunnable_WithValidAEPMessage_And_MessageAnimationStartsFromRight_ThenWebviewLoadDataCalled() {
        // setup
        try (MockedConstruction<CardView> ignored = mockConstruction(CardView.class)) {
            aepMessageSettings.setDisplayAnimation(MessageAnimation.RIGHT);
            when(mockAEPMessage.getMessageSettings()).thenReturn(aepMessageSettings);
            messageWebViewUtil = new MessageWebViewUtil();
            // test
            messageWebViewUtil.show(mockAEPMessage);
            // verify
            Mockito.verify(mockWebview, Mockito.times(1)).getSettings();
            Mockito.verify(mockWebview, Mockito.times(1))
                    .loadDataWithBaseURL(any(), any(), any(), any(), any());
            Mockito.verify(mockWebview, Mockito.times(1))
                    .setOnTouchListener(any(MessageFragment.class));
            Mockito.verify(mockAEPMessage, Mockito.times(1))
                    .setParams(any(FrameLayout.LayoutParams.class));
        }
    }

    @Test
    public void
            testRunnable_WithValidAEPMessage_And_MessageAnimationStartsFromBottom_ThenWebviewLoadDataCalled() {
        // setup
        try (MockedConstruction<CardView> ignored = mockConstruction(CardView.class)) {
            aepMessageSettings.setDisplayAnimation(MessageAnimation.BOTTOM);
            when(mockAEPMessage.getMessageSettings()).thenReturn(aepMessageSettings);
            messageWebViewUtil = new MessageWebViewUtil();
            // test
            messageWebViewUtil.show(mockAEPMessage);
            // verify
            Mockito.verify(mockWebview, Mockito.times(1)).getSettings();
            Mockito.verify(mockWebview, Mockito.times(1))
                    .loadDataWithBaseURL(any(), any(), any(), any(), any());
            Mockito.verify(mockWebview, Mockito.times(1))
                    .setOnTouchListener(any(MessageFragment.class));
            Mockito.verify(mockAEPMessage, Mockito.times(1))
                    .setParams(any(FrameLayout.LayoutParams.class));
        }
    }

    @Test
    public void
            testRunnable_WithValidAEPMessage_And_MessageAnimationFadesIn_ThenWebviewLoadDataCalled() {
        // setup
        try (MockedConstruction<CardView> ignored = mockConstruction(CardView.class)) {
            aepMessageSettings.setDisplayAnimation(MessageAnimation.FADE);
            when(mockAEPMessage.getMessageSettings()).thenReturn(aepMessageSettings);
            messageWebViewUtil = new MessageWebViewUtil();
            // test
            messageWebViewUtil.show(mockAEPMessage);
            // verify
            Mockito.verify(mockWebview, Mockito.times(1)).getSettings();
            Mockito.verify(mockWebview, Mockito.times(1))
                    .loadDataWithBaseURL(any(), any(), any(), any(), any());
            Mockito.verify(mockWebview, Mockito.times(1))
                    .setOnTouchListener(any(MessageFragment.class));
            Mockito.verify(mockAEPMessage, Mockito.times(1))
                    .setParams(any(FrameLayout.LayoutParams.class));
        }
    }

    @Test
    public void
            testRunnable_WithValidAEPMessage_And_NoMessageAnimation_ThenWebviewLoadDataCalled() {
        // setup
        try (MockedConstruction<CardView> ignored = mockConstruction(CardView.class)) {
            aepMessageSettings.setDisplayAnimation(MessageAnimation.NONE);
            when(mockAEPMessage.getMessageSettings()).thenReturn(aepMessageSettings);
            messageWebViewUtil = new MessageWebViewUtil();
            // test
            messageWebViewUtil.show(mockAEPMessage);
            // verify
            Mockito.verify(mockWebview, Mockito.times(1)).getSettings();
            Mockito.verify(mockWebview, Mockito.times(1))
                    .loadDataWithBaseURL(any(), any(), any(), any(), any());
            Mockito.verify(mockWebview, Mockito.times(1))
                    .setOnTouchListener(any(MessageFragment.class));
            Mockito.verify(mockAEPMessage, Mockito.times(1))
                    .setParams(any(FrameLayout.LayoutParams.class));
        }
    }

    @Test
    public void
            testRunnable_WithValidAEPMessage_And_NonFullscreenMessage_ThenWebviewLoadDataCalled() {
        // setup
        try (MockedConstruction<CardView> ignored = mockConstruction(CardView.class)) {
            aepMessageSettings.setHeight(50);
            when(mockAEPMessage.getMessageSettings()).thenReturn(aepMessageSettings);
            messageWebViewUtil = new MessageWebViewUtil();
            // test
            messageWebViewUtil.show(mockAEPMessage);
            // verify
            Mockito.verify(mockWebview, Mockito.times(1)).getSettings();
            Mockito.verify(mockWebview, Mockito.times(1))
                    .loadDataWithBaseURL(any(), any(), any(), any(), any());
            Mockito.verify(mockWebview, Mockito.times(1))
                    .setOnTouchListener(any(MessageFragment.class));
            Mockito.verify(mockAEPMessage, Mockito.times(1))
                    .setParams(any(FrameLayout.LayoutParams.class));
        }
    }

    @Test
    public void
            testRunnable_WithValidAEPMessage_And_MessageHorizontalAlignLeft_ThenWebviewLoadDataCalled() {
        // setup
        try (MockedConstruction<CardView> ignored = mockConstruction(CardView.class)) {
            aepMessageSettings.setHorizontalAlign(MessageAlignment.LEFT);
            when(mockAEPMessage.getMessageSettings()).thenReturn(aepMessageSettings);
            messageWebViewUtil = new MessageWebViewUtil();
            // test
            messageWebViewUtil.show(mockAEPMessage);
            // verify
            Mockito.verify(mockWebview, Mockito.times(1)).getSettings();
            Mockito.verify(mockWebview, Mockito.times(1))
                    .loadDataWithBaseURL(any(), any(), any(), any(), any());
            Mockito.verify(mockWebview, Mockito.times(1))
                    .setOnTouchListener(any(MessageFragment.class));
            Mockito.verify(mockAEPMessage, Mockito.times(1))
                    .setParams(any(FrameLayout.LayoutParams.class));
        }
    }

    @Test
    public void
            testRunnable_WithValidAEPMessage_And_MessageHorizontalAlignRight_ThenWebviewLoadDataCalled() {
        // setup
        try (MockedConstruction<CardView> ignored = mockConstruction(CardView.class)) {
            aepMessageSettings.setHorizontalAlign(MessageAlignment.RIGHT);
            when(mockAEPMessage.getMessageSettings()).thenReturn(aepMessageSettings);
            messageWebViewUtil = new MessageWebViewUtil();
            // test
            messageWebViewUtil.show(mockAEPMessage);
            // verify
            Mockito.verify(mockWebview, Mockito.times(1)).getSettings();
            Mockito.verify(mockWebview, Mockito.times(1))
                    .loadDataWithBaseURL(any(), any(), any(), any(), any());
            Mockito.verify(mockWebview, Mockito.times(1))
                    .setOnTouchListener(any(MessageFragment.class));
            Mockito.verify(mockAEPMessage, Mockito.times(1))
                    .setParams(any(FrameLayout.LayoutParams.class));
        }
    }

    @Test
    public void
            testRunnable_WithValidAEPMessage_And_MessageVerticalAlignBottom_ThenWebviewLoadDataCalled() {
        // setup
        try (MockedConstruction<CardView> ignored = mockConstruction(CardView.class)) {
            aepMessageSettings.setVerticalAlign(MessageAlignment.BOTTOM);
            when(mockAEPMessage.getMessageSettings()).thenReturn(aepMessageSettings);
            messageWebViewUtil = new MessageWebViewUtil();
            // test
            messageWebViewUtil.show(mockAEPMessage);
            // verify
            Mockito.verify(mockWebview, Mockito.times(1)).getSettings();
            Mockito.verify(mockWebview, Mockito.times(1))
                    .loadDataWithBaseURL(any(), any(), any(), any(), any());
            Mockito.verify(mockWebview, Mockito.times(1))
                    .setOnTouchListener(any(MessageFragment.class));
            Mockito.verify(mockAEPMessage, Mockito.times(1))
                    .setParams(any(FrameLayout.LayoutParams.class));
        }
    }

    @Test
    public void
            testRunnable_WithValidAEPMessage_And_MessageVerticalAlignCenter_ThenWebviewLoadDataCalled() {
        // setup
        try (MockedConstruction<CardView> ignored = mockConstruction(CardView.class)) {
            aepMessageSettings.setVerticalAlign(MessageAlignment.CENTER);
            when(mockAEPMessage.getMessageSettings()).thenReturn(aepMessageSettings);
            messageWebViewUtil = new MessageWebViewUtil();
            // test
            messageWebViewUtil.show(mockAEPMessage);
            // verify
            Mockito.verify(mockWebview, Mockito.times(1)).getSettings();
            Mockito.verify(mockWebview, Mockito.times(1))
                    .loadDataWithBaseURL(any(), any(), any(), any(), any());
            Mockito.verify(mockWebview, Mockito.times(1))
                    .setOnTouchListener(any(MessageFragment.class));
            Mockito.verify(mockAEPMessage, Mockito.times(1))
                    .setParams(any(FrameLayout.LayoutParams.class));
        }
    }

    @Test
    public void
            testRunnable_WithValidAEPMessage_And_BuildVersionLessThanAPI17_ThenWebviewLoadDataCalled() {
        // setup
        try (MockedConstruction<CardView> ignored = mockConstruction(CardView.class)) {
            messageWebViewUtil = new MessageWebViewUtil();
            // test
            messageWebViewUtil.show(mockAEPMessage);
            // verify
            Mockito.verify(mockWebview, Mockito.times(1)).getSettings();
            Mockito.verify(mockWebview, Mockito.times(1))
                    .loadDataWithBaseURL(any(), any(), any(), any(), any());
            Mockito.verify(mockWebview, Mockito.times(1))
                    .setOnTouchListener(any(MessageFragment.class));
            Mockito.verify(mockAEPMessage, Mockito.times(1))
                    .setParams(any(FrameLayout.LayoutParams.class));
        }
    }

    @Test
    public void testRunnable_WithInvalidAEPMessage_ThenWebviewLoadDataNotCalled() {
        // setup
        try (MockedConstruction<CardView> ignored = mockConstruction(CardView.class)) {
            when(mockAEPMessage.getMessageHtml()).thenReturn(null);
            messageWebViewUtil = new MessageWebViewUtil();
            // test
            messageWebViewUtil.show(mockAEPMessage);
            // verify
            Mockito.verify(mockWebview, Mockito.times(0)).getSettings();
            Mockito.verify(mockWebview, Mockito.times(0))
                    .loadDataWithBaseURL(any(), any(), any(), any(), any());
            Mockito.verify(mockWebview, Mockito.times(0))
                    .setOnTouchListener(any(MessageFragment.class));
            Mockito.verify(mockAEPMessage, Mockito.times(0))
                    .setParams(any(FrameLayout.LayoutParams.class));
        }
    }
}
