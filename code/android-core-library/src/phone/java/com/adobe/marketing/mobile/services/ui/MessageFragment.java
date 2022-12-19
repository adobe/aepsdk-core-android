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
import android.content.Context;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.ServiceConstants;
import com.adobe.marketing.mobile.services.ServiceProvider;
import com.adobe.marketing.mobile.services.ui.MessageSettings.MessageGesture;
import java.util.Map;

/**
 * An extension of {@link android.app.Fragment} used to display in-app messages with custom
 * locations and dimensions.
 */
public class MessageFragment extends android.app.Fragment implements View.OnTouchListener {

    private static final String TAG = "MessageFragment";
    private static final String UNEXPECTED_NULL_VALUE = "Unexpected Null Value";

    protected boolean dismissedWithGesture = false;
    protected AEPMessage message;
    protected GestureDetector gestureDetector;

    protected WebViewGestureListener webViewGestureListener;
    protected Map<MessageGesture, String> gestures;

    /**
     * Sets the in-app message to be displayed in the {@link MessageFragment}
     *
     * @param message the {@link AEPMessage} object which created this fragment
     */
    public void setAEPMessage(final AEPMessage message) {
        this.message = message;
    }

    @Override
    public boolean onTouch(final View view, final MotionEvent motionEvent) {
        if (message == null) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    UNEXPECTED_NULL_VALUE
                            + " (message), unable to handle the touch event on "
                            + view.getClass().getSimpleName());
            return true;
        }

        final int motionEventAction = motionEvent.getAction();

        // determine if the tap occurred outside the webview
        if ((motionEventAction == MotionEvent.ACTION_DOWN
                        || motionEventAction == MotionEvent.ACTION_BUTTON_PRESS)
                && view.getId() != message.webView.getId()) {
            Log.trace(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    "Detected tap on " + view.getClass().getSimpleName());

            final boolean uiTakeoverEnabled = message.getSettings().getUITakeover();

            // if ui takeover is false, dismiss the message
            if (!uiTakeoverEnabled) {
                Log.trace(
                        ServiceConstants.LOG_TAG,
                        TAG,
                        "UI takeover is false, dismissing the message.");
                webViewGestureListener.handleGesture(MessageGesture.BACKGROUND_TAP);
                // perform the tap to allow interaction with ui elements outside the webview
                return view.onTouchEvent(motionEvent);
            }

            // ui takeover is true, consume the tap and ignore it
            Log.trace(ServiceConstants.LOG_TAG, TAG, "UI takeover is true, ignoring the tap.");
            return true;
        }

        // determine if the tapped view is the webview
        if (view.getId() == message.webView.getId()) {
            // pass the event to the gesture detector to determine if a motion event occurred on the
            // webview.
            gestureDetector.onTouchEvent(motionEvent);
            // perform the tap to allow interaction with buttons within the webview
            return view.onTouchEvent(motionEvent);
        }

        return false;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // make sure we have a valid message before trying to proceed
        if (message == null) {
            Log.warning(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    UNEXPECTED_NULL_VALUE + " (message), failed to show the message.");
            return;
        }

        // store message gestures if available
        final Map<MessageGesture, String> retrievedGestures = message.getSettings().getGestures();

        if (retrievedGestures != null && !retrievedGestures.isEmpty()) {
            gestures = retrievedGestures;
        }

        // initialize the gesture detector and listener
        webViewGestureListener = new WebViewGestureListener(this);
        final Context appContext =
                ServiceProvider.getInstance().getAppContextService().getApplicationContext();
        gestureDetector = new GestureDetector(appContext, webViewGestureListener);
    }

    @Override
    public void onResume() {
        super.onResume();

        final Activity currentActivity =
                ServiceProvider.getInstance().getAppContextService().getCurrentActivity();

        if (currentActivity == null
                || currentActivity.findViewById(message.frameLayoutResourceId) == null) {
            Log.warning(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    UNEXPECTED_NULL_VALUE + " (frame layout), failed to show the message.");
            return;
        }

        // show the message
        message.showInRootViewGroup();
    }

    // for unit tests
    public WebViewGestureListener getWebViewGestureListener() {
        return webViewGestureListener;
    }

    public Map<MessageGesture, String> getGestures() {
        return gestures;
    }

    public GestureDetector getGestureDetector() {
        return gestureDetector;
    }

    public boolean isDismissedWithGesture() {
        return this.dismissedWithGesture;
    }
}
