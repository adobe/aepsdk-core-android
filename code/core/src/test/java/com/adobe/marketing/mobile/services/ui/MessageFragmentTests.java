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

import android.content.Context;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.webkit.WebView;
import com.adobe.marketing.mobile.services.AppContextService;
import com.adobe.marketing.mobile.services.ServiceProviderModifier;
import com.adobe.marketing.mobile.services.ui.MessageSettings.MessageGesture;
import com.adobe.marketing.mobile.services.ui.internal.MessagesMonitor;
import java.lang.reflect.Field;
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
public class MessageFragmentTests {

    @Mock private Bundle mockSavedInstanceState;

    @Mock private AEPMessage mockAEPMessage;

    @Mock private MessageSettings mockAEPMessageSettings;

    @Mock private AppContextService mockAppContextService;

    @Mock private MessagesMonitor mockMessagesMonitor;

    @Mock private Context mockContext;

    @Mock private FullscreenMessageDelegate mockFullscreenMessageDelegate;

    @Mock private ViewGroup mockViewGroup;

    @Mock private WebView mockWebView;

    @Mock private MotionEvent mockMotionEvent;

    @Mock private WebViewGestureListener mockWebViewGestureListener;

    @Mock private GestureDetector mockGestureDetector;

    private MessageFragment messageFragment;
    private HashMap<MessageGesture, String> gestureMap = new HashMap<>();

    @Before
    public void setup() throws Exception {
        ServiceProviderModifier.setAppContextService(mockAppContextService);

        gestureMap.put(MessageGesture.BACKGROUND_TAP, "adbinapp://dismiss");
        gestureMap.put(MessageGesture.SWIPE_LEFT, "adbinapp://dismiss?interaction=negative");
        gestureMap.put(MessageGesture.SWIPE_RIGHT, "adbinapp://dismiss?interaction=positive");
        gestureMap.put(MessageGesture.SWIPE_UP, "adbinapp://dismiss");
        gestureMap.put(MessageGesture.SWIPE_DOWN, "adbinapp://dismiss");

        Mockito.when(mockAEPMessage.getMessageSettings()).thenReturn(mockAEPMessageSettings);
        Mockito.when(mockAEPMessageSettings.getGestures()).thenReturn(gestureMap);
        Mockito.when(mockMotionEvent.getAction()).thenReturn(MotionEvent.ACTION_DOWN);
        Mockito.when(mockWebView.getId()).thenReturn(12345);
        Mockito.when(mockAEPMessage.getWebView()).thenReturn(mockWebView);
        Mockito.when(mockAEPMessage.getMessageSettings()).thenReturn(mockAEPMessageSettings);

        // set the private fullscreen message delegate var using reflection
        final Field listener = mockAEPMessage.getClass().getDeclaredField("listener");
        listener.setAccessible(true);
        listener.set(mockAEPMessage, mockFullscreenMessageDelegate);

        messageFragment = new MessageFragment();
        messageFragment.setAEPMessage(mockAEPMessage);
    }

    @Test
    public void testOnCreate_WithAEPMessage_ThenGestureDetectorAndGestureListenerCreated() {
        // test
        messageFragment.onCreate(mockSavedInstanceState);
        // verify
        Mockito.verify(mockAEPMessage, Mockito.times(1)).getMessageSettings();
        Mockito.verify(mockAEPMessageSettings, Mockito.times(1)).getGestures();
        Assert.assertEquals(messageFragment.getGestures(), gestureMap);
        Assert.assertNotNull(messageFragment.getGestureDetector());
        Assert.assertNotNull(messageFragment.getWebViewGestureListener());
    }

    @Test
    public void
            testOAnAttach_AndMessageNotNull_ThenMessageViewedAndMessageMonitorDisplayedCalled() {
        // setup
        messageFragment.setMessagesMonitor(mockMessagesMonitor);
        messageFragment.webViewGestureListener = mockWebViewGestureListener;
        messageFragment.gestureDetector = mockGestureDetector;

        // test
        messageFragment.onAttach(mockContext);

        // verify
        // verify that should not be called on attach
        Mockito.verify(mockAEPMessage, Mockito.times(0)).viewed();
        Mockito.verify(mockMessagesMonitor, Mockito.times(1)).displayed();
    }

    @Test
    public void
            testOAnAttach_AndMessageNull_ThenMessageViewedAndMessageMonitorDisplayedNotCalled() {
        // setup
        messageFragment.setAEPMessage(null);
        messageFragment.setMessagesMonitor(mockMessagesMonitor);
        messageFragment.webViewGestureListener = mockWebViewGestureListener;
        messageFragment.gestureDetector = mockGestureDetector;

        // test
        messageFragment.onAttach(mockContext);

        // verify
        Mockito.verify(mockAEPMessage, Mockito.times(0)).viewed();
        Mockito.verify(mockMessagesMonitor, Mockito.times(0)).displayed();
    }

    @Test
    public void testOnTouchListener_MessageIsNull_ThenTouchEventIsIgnored() {
        // setup
        messageFragment.webViewGestureListener = mockWebViewGestureListener;
        messageFragment.gestureDetector = mockGestureDetector;
        messageFragment.setAEPMessage(null);
        // test
        boolean eventProcessed = messageFragment.onTouch(mockWebView, mockMotionEvent);
        // verify
        Mockito.verify(mockFullscreenMessageDelegate, Mockito.times(0))
                .overrideUrlLoad(
                        ArgumentMatchers.any(AEPMessage.class), ArgumentMatchers.anyString());
        Assert.assertTrue(eventProcessed);
    }

    @Test
    public void
            testOnTouchListener_TouchOccurredOutsideWebview_And_UITakeoverFalse_ThenMessageDismissed() {
        // setup
        messageFragment.webViewGestureListener = mockWebViewGestureListener;
        messageFragment.gestureDetector = mockGestureDetector;
        Mockito.when(mockAEPMessageSettings.getUITakeover()).thenReturn(false);
        Mockito.when(mockWebView.getId()).thenReturn(12345);
        Mockito.when(mockViewGroup.getId()).thenReturn(67890);
        Mockito.when(mockAEPMessage.getWebView()).thenReturn(mockWebView);
        // call onCreate to setup the gestures
        messageFragment.onCreate(mockSavedInstanceState);
        // test
        boolean eventProcessed = messageFragment.onTouch(mockViewGroup, mockMotionEvent);
        // verify
        Mockito.verify(mockFullscreenMessageDelegate, Mockito.times(1))
                .overrideUrlLoad(
                        ArgumentMatchers.any(AEPMessage.class), ArgumentMatchers.anyString());
        // expect false because the touch event was handled by the rootview
        Assert.assertFalse(eventProcessed);
    }

    @Test
    public void
            testOnTouchListener_TouchOccurredOutsideWebview_And_UITakeoverTrue_ThenMessageNotDismissed() {
        // setup
        messageFragment.webViewGestureListener = mockWebViewGestureListener;
        messageFragment.gestureDetector = mockGestureDetector;
        Mockito.when(mockAEPMessageSettings.getUITakeover()).thenReturn(true);
        Mockito.when(mockWebView.getId()).thenReturn(12345);
        Mockito.when(mockViewGroup.getId()).thenReturn(67890);
        Mockito.when(mockAEPMessage.getWebView()).thenReturn(mockWebView);
        // call onCreate to setup the gestures
        messageFragment.onCreate(mockSavedInstanceState);
        // test
        boolean eventProcessed = messageFragment.onTouch(mockViewGroup, mockMotionEvent);
        // verify
        Mockito.verify(mockFullscreenMessageDelegate, Mockito.times(0))
                .overrideUrlLoad(
                        ArgumentMatchers.any(AEPMessage.class), ArgumentMatchers.anyString());
        Assert.assertTrue(eventProcessed);
    }
}
