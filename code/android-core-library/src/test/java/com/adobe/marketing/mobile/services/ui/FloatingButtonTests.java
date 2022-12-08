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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.MotionEvent;
import android.view.View;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class FloatingButtonTests {

    class TestableOnPositionChangedListener
            implements FloatingButtonView.OnPositionChangedListener {

        float x;
        float y;

        @Override
        public void onPositionChanged(float newX, float newY) {
            x = newX;
            y = newY;
        }
    }

    class TestableFloatingButtonListener implements FloatingButtonListener {

        boolean onTapDetectedCalled;

        @Override
        public void onTapDetected() {
            onTapDetectedCalled = true;
        }

        boolean onPanDetectedCalled;

        @Override
        public void onPanDetected() {
            onPanDetectedCalled = true;
        }
    }

    @Mock private Context mockContext;

    @Mock private MotionEvent mockMotionEvent;

    @Mock private Bitmap mockBitmap;

    @Test
    public void setXYCallsOnPositionListener_When_ValidCoOrdinates() {
        // Setup
        FloatingButtonView floatingButtonView = new FloatingButtonView(mockContext);
        TestableOnPositionChangedListener onPositionChangedListener =
                new TestableOnPositionChangedListener();
        floatingButtonView.setOnPositionChangedListener(onPositionChangedListener);
        // test
        floatingButtonView.setXYCompat(100, 100);
        // verify
        assertEquals(100f, onPositionChangedListener.x);
        assertEquals(100f, onPositionChangedListener.y);
    }

    @Test
    public void buttonCallsOnTapDetected_When_OnClickCalled() {
        // Setup a spy for the Context constructor
        FloatingButtonView floatingButtonViewSpy =
                mock(
                        FloatingButtonView.class,
                        withSettings()
                                .useConstructor(mockContext)
                                .defaultAnswer(CALLS_REAL_METHODS));

        ArgumentCaptor<View.OnClickListener> onClickListenerArgumentCaptor =
                ArgumentCaptor.forClass(View.OnClickListener.class);
        doNothing()
                .when(floatingButtonViewSpy)
                .setOnClickListener(onClickListenerArgumentCaptor.capture());
        TestableFloatingButtonListener floatingButtonListener =
                new TestableFloatingButtonListener();
        floatingButtonViewSpy.setFloatingButtonListener(floatingButtonListener);
        // test
        onClickListenerArgumentCaptor.getValue().onClick(floatingButtonViewSpy);
        // verify
        assertTrue(floatingButtonListener.onTapDetectedCalled);
    }

    @Test
    public void buttonCallsOnPanDetected_When_SetXYCalled() {
        // Setup
        FloatingButtonView floatingButtonView = new FloatingButtonView(mockContext);
        TestableFloatingButtonListener floatingButtonListener =
                new TestableFloatingButtonListener();
        floatingButtonView.setFloatingButtonListener(floatingButtonListener);
        // test
        floatingButtonView.setXYCompat(100, 100);
        // verify
        assertTrue(floatingButtonListener.onPanDetectedCalled);
    }

    @Test
    public void onTouchSetsXYCoordinates_When_ButtonMoved() {
        // Setup
        FloatingButtonView floatingButtonView = new FloatingButtonView(mockContext);
        when(mockMotionEvent.getAction()).thenReturn(MotionEvent.ACTION_MOVE);
        when(mockMotionEvent.getRawX()).thenReturn(200f);
        when(mockMotionEvent.getRawY()).thenReturn(400f);
        TestableOnPositionChangedListener onPositionChangedListener =
                new TestableOnPositionChangedListener();
        floatingButtonView.setOnPositionChangedListener(onPositionChangedListener);
        // test
        floatingButtonView.onTouch(floatingButtonView, mockMotionEvent);
        // verify
        assertEquals(200f, onPositionChangedListener.x);
        assertEquals(400f, onPositionChangedListener.y);
    }

    @Test
    public void buttonOnClickNotCalled_When_ButtonDragged() {
        // Setup
        FloatingButtonView floatingButtonViewSpy =
                mock(
                        FloatingButtonView.class,
                        withSettings()
                                .useConstructor(mockContext)
                                .defaultAnswer(CALLS_REAL_METHODS));
        when(mockMotionEvent.getAction()).thenReturn(MotionEvent.ACTION_DOWN);
        when(mockMotionEvent.getRawX()).thenReturn(200f);
        when(mockMotionEvent.getRawY()).thenReturn(400f);
        TestableOnPositionChangedListener onPositionChangedListener =
                new TestableOnPositionChangedListener();
        floatingButtonViewSpy.setOnPositionChangedListener(onPositionChangedListener);
        // test
        floatingButtonViewSpy.onTouch(floatingButtonViewSpy, mockMotionEvent);
        when(mockMotionEvent.getAction()).thenReturn(MotionEvent.ACTION_MOVE);
        when(mockMotionEvent.getRawX()).thenReturn(250f);
        when(mockMotionEvent.getRawY()).thenReturn(450f);
        floatingButtonViewSpy.onTouch(floatingButtonViewSpy, mockMotionEvent);
        when(mockMotionEvent.getAction()).thenReturn(MotionEvent.ACTION_UP);
        floatingButtonViewSpy.onTouch(floatingButtonViewSpy, mockMotionEvent);
        // verify
        verify(floatingButtonViewSpy, times(0)).performClick();
    }

    @Test
    public void buttonOnClickCalled_When_ButtonTapped() {
        // Setup
        FloatingButtonView floatingButtonViewSpy =
                mock(
                        FloatingButtonView.class,
                        withSettings()
                                .useConstructor(mockContext)
                                .defaultAnswer(CALLS_REAL_METHODS));
        when(mockMotionEvent.getAction()).thenReturn(MotionEvent.ACTION_DOWN);
        when(mockMotionEvent.getRawX()).thenReturn(200f);
        when(mockMotionEvent.getRawY()).thenReturn(400f);
        TestableOnPositionChangedListener onPositionChangedListener =
                new TestableOnPositionChangedListener();
        floatingButtonViewSpy.setOnPositionChangedListener(onPositionChangedListener);
        // test
        floatingButtonViewSpy.onTouch(floatingButtonViewSpy, mockMotionEvent);
        when(mockMotionEvent.getAction()).thenReturn(MotionEvent.ACTION_MOVE);
        when(mockMotionEvent.getRawX()).thenReturn(200f);
        when(mockMotionEvent.getRawY())
                .thenReturn(410f); // The threshold of tap detection is a displacement of < 20f
        floatingButtonViewSpy.onTouch(floatingButtonViewSpy, mockMotionEvent);
        when(mockMotionEvent.getAction()).thenReturn(MotionEvent.ACTION_UP);
        floatingButtonViewSpy.onTouch(floatingButtonViewSpy, mockMotionEvent);
        // verify
        verify(floatingButtonViewSpy, times(1)).performClick();
    }

    @Test(expected = IllegalArgumentException.class)
    public void setBitmapCallsSetBackgroundDrawable_When_BitmapNull() throws Exception {
        // Setup
        FloatingButtonView floatingButtonViewSpy =
                mock(
                        FloatingButtonView.class,
                        withSettings()
                                .useConstructor(mockContext)
                                .defaultAnswer(CALLS_REAL_METHODS));
        // test
        floatingButtonViewSpy.setBitmap(null);
    }
}
