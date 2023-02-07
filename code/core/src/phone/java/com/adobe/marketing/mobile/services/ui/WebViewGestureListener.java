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

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.GestureDetector;
import android.view.MotionEvent;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.ServiceConstants;
import com.adobe.marketing.mobile.services.ui.MessageSettings.MessageAnimation;
import com.adobe.marketing.mobile.services.ui.MessageSettings.MessageGesture;
import com.adobe.marketing.mobile.util.StringUtils;

/**
 * Listens for {@link MotionEvent}s and determines if a swipe gesture occurred on the {@link
 * MessageWebView}.
 */
class WebViewGestureListener extends GestureDetector.SimpleOnGestureListener {

    private static final String TAG = "WebViewGestureListener";

    private static final int ANIMATION_DURATION = 300;
    // the number of pixels that define a swipe on the message
    private static final int SWIPE_THRESHOLD = 200;
    // the fling velocity needed to swipe a message off the screen
    private static final int SWIPE_VELOCITY_THRESHOLD = 300;
    private final MessageFragment parentFragment;
    private Animator.AnimatorListener animatorListener;

    /**
     * Constructor.
     *
     * @param parent the {@link MessageFragment} that owns this listener.
     */
    public WebViewGestureListener(final MessageFragment parent) {
        this.parentFragment = parent;
    }

    /**
     * Invoked when a tap occurs with the "onDown" {@link MotionEvent}.
     *
     * @param motionEvent The on down {@code MotionEvent}
     * @return true as every on down motion event is consumed by this listener.
     */
    @Override
    public boolean onDown(final MotionEvent motionEvent) {
        // this listener only handles touches that occurred on the webview so we always want to
        // consume the touch.
        Log.trace(ServiceConstants.LOG_TAG, TAG, "onDown: " + motionEvent.toString());
        return true;
    }

    /**
     * Invoked when a fling event is detected after an initial "onDown" MotionEvent and a matching
     * "up" MotionEvent. The calculated velocity is supplied along the x and y axis in pixels per
     * second.
     *
     * @param motionEvent The on down {@code MotionEvent}
     * @param motionEvent2 The up {@code MotionEvent}
     * @param velocityX The fling velocity along the x axis
     * @param velocityY The fling velocity along the y axis
     * @return true if the event was consumed, false otherwise
     */
    @Override
    public boolean onFling(
            final MotionEvent motionEvent,
            final MotionEvent motionEvent2,
            final float velocityX,
            final float velocityY) {
        final float deltaX = motionEvent2.getX() - motionEvent.getX();
        final float deltaY = motionEvent2.getY() - motionEvent.getY();

        boolean isHorizontalSwipe = false;
        boolean isVerticalSwipe = false;

        if (Math.abs(deltaX) > Math.abs(deltaY)) { // detect horizontal swipe
            isHorizontalSwipe =
                    Math.abs(deltaX) > SWIPE_THRESHOLD
                            && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD;
            if (isHorizontalSwipe && deltaX > 0) {
                Log.trace(ServiceConstants.LOG_TAG, TAG, "Detected swipe right.");
                handleGesture(MessageGesture.SWIPE_RIGHT);
            } else if (isHorizontalSwipe && deltaX <= 0) {
                Log.trace(ServiceConstants.LOG_TAG, TAG, "Detected swipe left.");
                handleGesture(MessageGesture.SWIPE_LEFT);
            }
        } else { // detect vertical swipe
            isVerticalSwipe =
                    Math.abs(deltaY) > SWIPE_THRESHOLD
                            && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD;
            if (isVerticalSwipe && deltaY > 0) {
                Log.trace(ServiceConstants.LOG_TAG, TAG, "Detected swipe down.");
                handleGesture(MessageGesture.SWIPE_DOWN);
            } else if (isVerticalSwipe && deltaY <= 0) {
                Log.trace(ServiceConstants.LOG_TAG, TAG, "Detected swipe up.");
                handleGesture(MessageGesture.SWIPE_UP);
            }
        }

        return isHorizontalSwipe || isVerticalSwipe;
    }

    /**
     * Generates a dismiss animation using the {@link ObjectAnimator}. The {@link MessageWebView}
     * will be dismissed at the direction of the detected swipe {@link MessageGesture}. If the
     * webview was dismissed via a {@code MessageGesture.BACKGROUND_TAP} then the {@code
     * MessageWebView} will be dismissed using the dismissal {@link MessageAnimation} specified in
     * the {@link MessageSettings}.
     *
     * @param gesture The detected swipe {@code MessageGesture} that occurred.
     */
    public void handleGesture(final MessageGesture gesture) {
        if (gesture.equals(MessageSettings.MessageGesture.BACKGROUND_TAP)) {
            // we are handling a background tap. message will be dismissed via the dismiss animation
            // specified in the MessageSettings.
            dismissMessage(gesture, false);
            return;
        }

        ObjectAnimator animation;

        switch (gesture) {
            case SWIPE_RIGHT:
                animation =
                        ObjectAnimator.ofFloat(
                                parentFragment.message.webView,
                                "x",
                                parentFragment.message.webView.getX(),
                                parentFragment.message.baseRootViewWidth);
                break;
            case SWIPE_LEFT:
                animation =
                        ObjectAnimator.ofFloat(
                                parentFragment.message.webView,
                                "x",
                                parentFragment.message.webView.getX(),
                                -parentFragment.message.baseRootViewWidth);
                break;
            case SWIPE_UP:
                animation =
                        ObjectAnimator.ofFloat(
                                parentFragment.message.webView,
                                "y",
                                parentFragment.message.webView.getTop(),
                                -parentFragment.message.baseRootViewHeight);
                break;
            default: // default, dismiss to bottom if not a background tap
                animation =
                        ObjectAnimator.ofFloat(
                                parentFragment.message.webView,
                                "y",
                                parentFragment.message.webView.getTop(),
                                parentFragment.message.baseRootViewHeight);
                break;
        }

        if (animation != null) {
            animation.setDuration(ANIMATION_DURATION);
            animatorListener =
                    new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(final Animator animator) {}

                        // wait for the animation to complete then dismiss the message
                        @Override
                        public void onAnimationEnd(final Animator animator) {
                            dismissMessage(gesture, true);
                        }

                        @Override
                        public void onAnimationCancel(final Animator animator) {}

                        @Override
                        public void onAnimationRepeat(final Animator animator) {}
                    };
            animation.addListener(animatorListener);
            animation.start();
        }
    }

    /**
     * Dismisses the {@link MessageWebView} by calling the {@link
     * FullscreenMessageDelegate#overrideUrlLoad} function present in the parent {@link
     * MessageFragment}'s message listener.
     *
     * @param gesture The detected {@code MessageGesture} that occurred.
     * @param dismissedWithGesture true if a swipe gesture occurred, false if a background tap
     *     occurred.
     */
    private void dismissMessage(final MessageGesture gesture, final boolean dismissedWithGesture) {
        parentFragment.dismissedWithGesture = dismissedWithGesture;

        if (parentFragment.message != null && parentFragment.message.listener != null) {
            final String behavior =
                    parentFragment.gestures == null ? null : parentFragment.gestures.get(gesture);

            // if we have a gesture mapping with behaviors, use the specified behavior. otherwise,
            // just dismiss the message.
            if (!StringUtils.isNullOrEmpty(behavior)) {
                parentFragment.message.listener.overrideUrlLoad(parentFragment.message, behavior);
            } else {
                parentFragment.message.dismiss();
            }
        }
    }

    // added for testing
    public Animator.AnimatorListener getAnimationListener() {
        return animatorListener;
    }
}
