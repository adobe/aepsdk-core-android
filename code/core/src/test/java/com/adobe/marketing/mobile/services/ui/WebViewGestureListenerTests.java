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

import static org.mockito.Mockito.doCallRealMethod;

import android.animation.ObjectAnimator;
import android.view.MotionEvent;
import com.adobe.marketing.mobile.services.ui.MessageSettings.MessageGesture;
import java.lang.reflect.Field;
import java.util.HashMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class WebViewGestureListenerTests {

    @Mock private AEPMessage mockAEPMessage;

    @Mock private FullscreenMessageDelegate mockFullscreenMessageDelegate;

    @Mock private MessageWebView mockMessageWebView;

    @Mock private MotionEvent mockMotionEvent2;

    @Mock private MotionEvent mockMotionEvent;

    @Mock private MessageFragment mockMessageFragment;

    @Mock private ObjectAnimator mockObjectAnimator;

    private WebViewGestureListener gestureListener;
    private HashMap<MessageGesture, String> gestureMap = new HashMap<>();

    @Before
    public void setup() throws Exception {
        gestureMap.put(MessageGesture.BACKGROUND_TAP, "adbinapp://dismiss");
        gestureMap.put(MessageGesture.SWIPE_LEFT, "adbinapp://dismiss?interaction=negative");
        gestureMap.put(MessageGesture.SWIPE_RIGHT, "adbinapp://dismiss?interaction=positive");
        gestureMap.put(MessageGesture.SWIPE_UP, "adbinapp://dismiss");
        gestureMap.put(MessageGesture.SWIPE_DOWN, "adbinapp://dismiss");

        Mockito.when(mockMessageWebView.getX()).thenReturn(2000.0f);
        Mockito.when(mockMessageWebView.getY()).thenReturn(3000.0f);
        Mockito.when(mockMessageWebView.getTop()).thenReturn(0);
        Mockito.when(mockMessageWebView.getBottom()).thenReturn(3000);

        doCallRealMethod().when(mockMessageFragment).isDismissedWithGesture();

        // set the private fullscreen message delegate var using reflection
        final Field listener = mockAEPMessage.getClass().getDeclaredField("listener");
        listener.setAccessible(true);
        listener.set(mockAEPMessage, mockFullscreenMessageDelegate);

        mockAEPMessage.webView = mockMessageWebView;
        mockAEPMessage.baseRootViewHeight = 3000;
        mockAEPMessage.baseRootViewWidth = 2000;
        mockMessageFragment.gestures = gestureMap;
        mockMessageFragment.message = mockAEPMessage;

        gestureListener = new WebViewGestureListener(mockMessageFragment);
    }

    @Test
    public void testOnDown_ReturnsTrue() {
        // test
        final boolean didHandleMotionEvent = gestureListener.onDown(mockMotionEvent);
        // verify
        Assert.assertTrue(didHandleMotionEvent);
    }

    @Test
    public void
            testOnFling_HorizontalSwipeLeft_And_FlingVelocityOverThreshold_Then_MessageDismissed()
                    throws InterruptedException {
        try (MockedStatic<ObjectAnimator> staticMock = Mockito.mockStatic(ObjectAnimator.class)) {
            staticMock
                    .when(
                            () ->
                                    ObjectAnimator.ofFloat(
                                            ArgumentMatchers.any(MessageWebView.class),
                                            ArgumentMatchers.anyString(),
                                            ArgumentMatchers.anyFloat(),
                                            ArgumentMatchers.anyFloat()))
                    .thenReturn(mockObjectAnimator);
            // setup
            Mockito.when(mockMotionEvent.getX()).thenReturn(0f);
            Mockito.when(mockMotionEvent2.getX()).thenReturn(-300.0f);
            Mockito.when(mockMotionEvent.getY()).thenReturn(0f);
            Mockito.when(mockMotionEvent2.getY()).thenReturn(0f);
            // test
            gestureListener.onFling(mockMotionEvent, mockMotionEvent2, 310.0f, 0.0f);
            Thread.sleep(100);
            gestureListener.getAnimationListener().onAnimationEnd(mockObjectAnimator);
            // verify
            Assert.assertTrue(mockMessageFragment.isDismissedWithGesture());
            Mockito.verify(mockFullscreenMessageDelegate, Mockito.times(1))
                    .overrideUrlLoad(
                            ArgumentMatchers.any(AEPMessage.class), ArgumentMatchers.anyString());
        }
    }

    @Test
    public void
            testOnFling_HorizontalSwipeRight_And_FlingVelocityOverThreshold_Then_MessageDismissed()
                    throws InterruptedException {
        try (MockedStatic<ObjectAnimator> staticMock = Mockito.mockStatic(ObjectAnimator.class)) {
            staticMock
                    .when(
                            () ->
                                    ObjectAnimator.ofFloat(
                                            ArgumentMatchers.any(MessageWebView.class),
                                            ArgumentMatchers.anyString(),
                                            ArgumentMatchers.anyFloat(),
                                            ArgumentMatchers.anyFloat()))
                    .thenReturn(mockObjectAnimator);
            // setup
            Mockito.when(mockMotionEvent.getX()).thenReturn(0f);
            Mockito.when(mockMotionEvent2.getX()).thenReturn(300.0f);
            Mockito.when(mockMotionEvent.getY()).thenReturn(0f);
            Mockito.when(mockMotionEvent2.getY()).thenReturn(0f);
            // test
            gestureListener.onFling(mockMotionEvent, mockMotionEvent2, 310.0f, 0.0f);
            Thread.sleep(100);
            gestureListener.getAnimationListener().onAnimationEnd(mockObjectAnimator);
            // verify
            Assert.assertTrue(mockMessageFragment.isDismissedWithGesture());
            Mockito.verify(mockFullscreenMessageDelegate, Mockito.times(1))
                    .overrideUrlLoad(
                            ArgumentMatchers.any(AEPMessage.class), ArgumentMatchers.anyString());
        }
    }

    @Test
    public void testOnFling_VerticalSwipeUp_And_FlingVelocityOverThreshold_Then_MessageDismissed()
            throws InterruptedException {
        try (MockedStatic<ObjectAnimator> staticMock = Mockito.mockStatic(ObjectAnimator.class)) {
            staticMock
                    .when(
                            () ->
                                    ObjectAnimator.ofFloat(
                                            ArgumentMatchers.any(MessageWebView.class),
                                            ArgumentMatchers.anyString(),
                                            ArgumentMatchers.anyFloat(),
                                            ArgumentMatchers.anyFloat()))
                    .thenReturn(mockObjectAnimator);
            // setup
            Mockito.when(mockMotionEvent.getX()).thenReturn(0f);
            Mockito.when(mockMotionEvent2.getX()).thenReturn(0f);
            Mockito.when(mockMotionEvent.getY()).thenReturn(0f);
            Mockito.when(mockMotionEvent2.getY()).thenReturn(300.0f);
            // test
            gestureListener.onFling(mockMotionEvent, mockMotionEvent2, 0.0f, 310.0f);
            Thread.sleep(100);
            gestureListener.getAnimationListener().onAnimationEnd(mockObjectAnimator);
            // verify
            Assert.assertTrue(mockMessageFragment.isDismissedWithGesture());
            Mockito.verify(mockFullscreenMessageDelegate, Mockito.times(1))
                    .overrideUrlLoad(
                            ArgumentMatchers.any(AEPMessage.class), ArgumentMatchers.anyString());
        }
    }

    @Test
    public void testOnFling_VerticalSwipeDown_And_FlingVelocityOverThreshold_Then_MessageDismissed()
            throws InterruptedException {
        try (MockedStatic<ObjectAnimator> staticMock = Mockito.mockStatic(ObjectAnimator.class)) {
            staticMock
                    .when(
                            () ->
                                    ObjectAnimator.ofFloat(
                                            ArgumentMatchers.any(MessageWebView.class),
                                            ArgumentMatchers.anyString(),
                                            ArgumentMatchers.anyFloat(),
                                            ArgumentMatchers.anyFloat()))
                    .thenReturn(mockObjectAnimator);
            // setup
            Mockito.when(mockMotionEvent.getX()).thenReturn(0f);
            Mockito.when(mockMotionEvent2.getX()).thenReturn(0f);
            Mockito.when(mockMotionEvent.getY()).thenReturn(0f);
            Mockito.when(mockMotionEvent2.getY()).thenReturn(-300.0f);
            // test
            gestureListener.onFling(mockMotionEvent, mockMotionEvent2, 0.0f, 310.0f);
            Thread.sleep(100);
            gestureListener.getAnimationListener().onAnimationEnd(mockObjectAnimator);
            // verify
            Assert.assertTrue(mockMessageFragment.isDismissedWithGesture());
            Mockito.verify(mockFullscreenMessageDelegate, Mockito.times(1))
                    .overrideUrlLoad(
                            ArgumentMatchers.any(AEPMessage.class), ArgumentMatchers.anyString());
        }
    }

    @Test
    public void
            testOnFling_HorizontalSwipe_And_FlingVelocityUnderThreshold_Then_MessageNotDismissed()
                    throws InterruptedException {
        // setup
        Mockito.when(mockMotionEvent.getX()).thenReturn(0f);
        Mockito.when(mockMotionEvent2.getX()).thenReturn(-300.0f);
        Mockito.when(mockMotionEvent.getY()).thenReturn(0f);
        Mockito.when(mockMotionEvent2.getY()).thenReturn(0f);
        // test
        gestureListener.onFling(mockMotionEvent, mockMotionEvent2, 290.0f, 0.0f);
        // verify
        Assert.assertFalse(mockMessageFragment.isDismissedWithGesture());
        Mockito.verify(mockFullscreenMessageDelegate, Mockito.times(0))
                .overrideUrlLoad(
                        ArgumentMatchers.any(AEPMessage.class), ArgumentMatchers.anyString());
    }

    @Test
    public void testOnFling_VerticalSwipe_And_FlingVelocityUnderThreshold_Then_MessageNotDismissed()
            throws InterruptedException {
        // setup
        Mockito.when(mockMotionEvent.getX()).thenReturn(0f);
        Mockito.when(mockMotionEvent2.getX()).thenReturn(0f);
        Mockito.when(mockMotionEvent.getY()).thenReturn(0f);
        Mockito.when(mockMotionEvent2.getY()).thenReturn(-300.0f);
        // test
        gestureListener.onFling(mockMotionEvent, mockMotionEvent2, 0.0f, 290.0f);
        // verify
        Assert.assertFalse(mockMessageFragment.isDismissedWithGesture());
        Mockito.verify(mockFullscreenMessageDelegate, Mockito.times(0))
                .overrideUrlLoad(
                        ArgumentMatchers.any(AEPMessage.class), ArgumentMatchers.anyString());
    }

    @Test
    public void testHandleGesture_BackgroundTap_Then_MessageDismissed()
            throws InterruptedException {
        // setup
        Mockito.when(mockMotionEvent.getX()).thenReturn(0f);
        Mockito.when(mockMotionEvent2.getX()).thenReturn(0f);
        Mockito.when(mockMotionEvent.getY()).thenReturn(0f);
        Mockito.when(mockMotionEvent2.getY()).thenReturn(-300.0f);
        // test
        gestureListener.handleGesture(MessageGesture.BACKGROUND_TAP);
        // verify
        Assert.assertFalse(mockMessageFragment.isDismissedWithGesture());
        Mockito.verify(mockFullscreenMessageDelegate, Mockito.times(1))
                .overrideUrlLoad(
                        ArgumentMatchers.any(AEPMessage.class), ArgumentMatchers.anyString());
    }
}
