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
import android.app.Application;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.services.AppContextService;
import com.adobe.marketing.mobile.services.ServiceProviderModifier;
import com.adobe.marketing.mobile.services.ui.MessageSettings.MessageGesture;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Random;
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

    @Mock private Application mockApplication;

    @Mock private AppContextService mockAppContextService;

    @Mock private Activity mockActivity;

    @Mock private FrameLayout mockFrameLayout;

    @Mock private FullscreenMessageDelegate mockFullscreenMessageDelegate;

    @Mock private ViewGroup mockViewGroup;

    @Mock private MessageWebView mockWebView;

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

        Mockito.when(mockAEPMessage.getSettings()).thenReturn(mockAEPMessageSettings);
        Mockito.when(mockAEPMessageSettings.getGestures()).thenReturn(gestureMap);
        Mockito.when(mockMotionEvent.getAction()).thenReturn(MotionEvent.ACTION_DOWN);

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
        Mockito.verify(mockAEPMessage, Mockito.times(1)).getSettings();
        Mockito.verify(mockAEPMessageSettings, Mockito.times(1)).getGestures();
        Assert.assertEquals(messageFragment.getGestures(), gestureMap);
        Assert.assertNotNull(messageFragment.getGestureDetector());
        Assert.assertNotNull(messageFragment.getWebViewGestureListener());
    }

    @Test
    public void testOnCreate_WithNullAEPMessage_ThenGestureDetectorAndGestureListenerNotCreated() {
        // setup
        messageFragment.setAEPMessage(null);
        // test
        messageFragment.onCreate(mockSavedInstanceState);
        // verify
        Mockito.verify(mockAEPMessage, Mockito.times(0)).getSettings();
        Mockito.verify(mockAEPMessageSettings, Mockito.times(0)).getGestures();
        Assert.assertNull(messageFragment.getGestureDetector());
        Assert.assertNull(messageFragment.getWebViewGestureListener());
    }

    @Test
    public void testOnResume_FrameLayoutExists_ThenShowInRootViewGroupCalled() {
        // setup
        mockAEPMessage.frameLayoutResourceId = new Random().nextInt();
        messageFragment.webViewGestureListener = mockWebViewGestureListener;
        messageFragment.gestureDetector = mockGestureDetector;

        Mockito.when(mockAppContextService.getCurrentActivity()).thenReturn(mockActivity);
        Mockito.when(mockActivity.findViewById(ArgumentMatchers.anyInt()))
                .thenReturn(mockFrameLayout);
        // test
        messageFragment.onResume();
        // verify
        Mockito.verify(mockAEPMessage, Mockito.times(1)).showInRootViewGroup();
    }

    @Test
    public void testOnResume_FrameLayoutNull_ThenShowInRootViewGroupNotCalled() {
        // setup
        mockAEPMessage.frameLayoutResourceId = new Random().nextInt();
        messageFragment.webViewGestureListener = mockWebViewGestureListener;
        messageFragment.gestureDetector = mockGestureDetector;
        MobileCore.setApplication(mockApplication);
        // return a null frame layout
        Mockito.when(mockActivity.findViewById(ArgumentMatchers.anyInt())).thenReturn(null);
        // test
        messageFragment.onResume();
        // verify
        Mockito.verify(mockAEPMessage, Mockito.times(0)).showInRootViewGroup();
    }

    @Test
    public void
            testOnResume_NullActivityWhenRetrievingFrameLayout_ThenShowInRootViewGroupNotCalled() {
        // setup
        mockAEPMessage.frameLayoutResourceId = new Random().nextInt();
        messageFragment.webViewGestureListener = mockWebViewGestureListener;
        messageFragment.gestureDetector = mockGestureDetector;

        Mockito.when(mockAppContextService.getApplication()).thenReturn(mockApplication);
        Mockito.when(mockAppContextService.getCurrentActivity()).thenReturn(mockActivity);
        // test
        messageFragment.onResume();
        // verify
        Mockito.verify(mockAEPMessage, Mockito.times(0)).showInRootViewGroup();
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
        mockAEPMessage.webView = mockWebView;
        messageFragment.webViewGestureListener = mockWebViewGestureListener;
        messageFragment.gestureDetector = mockGestureDetector;
        Mockito.when(mockAEPMessageSettings.getUITakeover()).thenReturn(false);
        Mockito.when(mockWebView.getId()).thenReturn(12345);
        Mockito.when(mockViewGroup.getId()).thenReturn(67890);
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
        mockAEPMessage.webView = mockWebView;
        messageFragment.webViewGestureListener = mockWebViewGestureListener;
        messageFragment.gestureDetector = mockGestureDetector;
        Mockito.when(mockAEPMessageSettings.getUITakeover()).thenReturn(true);
        Mockito.when(mockWebView.getId()).thenReturn(12345);
        Mockito.when(mockViewGroup.getId()).thenReturn(67890);
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
