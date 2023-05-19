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

import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import androidx.annotation.VisibleForTesting;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.ServiceConstants;
import com.adobe.marketing.mobile.services.ServiceProvider;
import com.adobe.marketing.mobile.services.ui.MessageSettings.MessageGesture;
import com.adobe.marketing.mobile.services.ui.internal.MessagesMonitor;
import com.adobe.marketing.mobile.util.MapUtils;
import java.util.Map;

/**
 * An extension of {@link android.app.DialogFragment} used to display in-app messages with custom
 * locations and dimensions.
 */
public class MessageFragment extends android.app.DialogFragment implements View.OnTouchListener {

    private static final String TAG = "MessageFragment";
    private static final String UNEXPECTED_NULL_VALUE = "Unexpected Null Value";
    private static final int FULLY_OPAQUE_ALPHA_VALUE = 255;

    protected boolean dismissedWithGesture = false;
    protected GestureDetector gestureDetector;
    protected WebViewGestureListener webViewGestureListener;
    protected Map<MessageGesture, String> gestures;
    private MessagesMonitor messagesMonitor;
    private AEPMessage message;

    // layout change listener to listen for layout changes in parent activity's content ViewGroup.
    // upon orientation change, wait until the re-layouting of the content view is completed before
    // creating the webview.
    private final View.OnLayoutChangeListener layoutChangeListener =
            new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(
                        final View v,
                        final int left,
                        final int top,
                        final int right,
                        final int bottom,
                        final int oldLeft,
                        final int oldTop,
                        final int oldRight,
                        final int oldBottom) {
                    final int contentViewWidth = right - left;
                    final int contentViewHeight = bottom - top;
                    message.recreateWebViewFrame(contentViewWidth, contentViewHeight);
                    updateDialogView();
                }
            };

    /**
     * Setter for {@link AEPMessage}
     *
     * @param message instance of {@code AEPMessage}
     */
    public void setAEPMessage(final AEPMessage message) {
        this.message = message;

        if (message != null) {
            this.messagesMonitor = message.messagesMonitor;
        }
    }

    /**
     * Getter for {@code AEPMessage}
     *
     * @return {@link AEPMessage} instance
     */
    public AEPMessage getAEPMessage() {
        return message;
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);

        // make sure we have a valid message before trying to proceed
        if (message == null) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    "%s (Message Fragment), failed to attach the fragment.",
                    UNEXPECTED_NULL_VALUE);
            return;
        }

        message.viewed();

        if (messagesMonitor != null) {
            messagesMonitor.displayed();
        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        final MessageSettings messageSettings = message.getMessageSettings();
        if (messageSettings == null) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    "%s (Message Settings), failed to create the fragment.",
                    UNEXPECTED_NULL_VALUE);
            return;
        }

        // store message gestures if available
        final Map<MessageGesture, String> retrievedGestures = messageSettings.getGestures();

        if (!MapUtils.isNullOrEmpty(retrievedGestures)) {
            gestures = retrievedGestures;
        }

        // initialize the gesture detector and listener
        webViewGestureListener = new WebViewGestureListener(this);
        gestureDetector =
                new GestureDetector(
                        ServiceProvider.getInstance()
                                .getAppContextService()
                                .getApplicationContext(),
                        webViewGestureListener);

        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Translucent_NoTitleBar);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Dialog dialog = getDialog();

        if (dialog != null) {
            dialog.setCancelable(false);
        }

        applyBackdropColor();
        addListeners();
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }

        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (messagesMonitor != null) {
            messagesMonitor.dismissed();
        }

        removeListeners();

        // clean webview parent in case the detach is occurring due to an orientation change
        final WebView webView = message.getWebView();
        if (webView != null && webView.getParent() != null) {
            ((ViewGroup) webView.getParent()).removeView(webView);
        }
    }

    @Override
    public boolean onTouch(final View view, final MotionEvent motionEvent) {
        if (message == null) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    "%s (AEPMessage), unable to handle the touch event on %s.",
                    UNEXPECTED_NULL_VALUE,
                    view.getClass().getSimpleName());
            return true;
        }

        final WebView webView = message.getWebView();
        if (webView == null) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    "%s (WebView), unable to handle the touch event on %s.",
                    UNEXPECTED_NULL_VALUE,
                    view.getClass().getSimpleName());
            return true;
        }

        final MessageSettings messageSettings = message.getMessageSettings();
        if (messageSettings == null) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    "%s (MessageSettings), unable to handle the touch event on %s.",
                    UNEXPECTED_NULL_VALUE,
                    view.getClass().getSimpleName());
            return true;
        }

        final int motionEventAction = motionEvent.getAction();

        // determine if the tap occurred outside the webview
        if ((motionEventAction == MotionEvent.ACTION_DOWN
                        || motionEventAction == MotionEvent.ACTION_BUTTON_PRESS)
                && view.getId() != webView.getId()) {
            Log.trace(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    "Detected tap on %s",
                    view.getClass().getSimpleName());

            final boolean uiTakeoverEnabled = messageSettings.getUITakeover();

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
        if (view.getId() == webView.getId()) {
            // if we have no gestures just pass the touch event to the webview
            if (MapUtils.isNullOrEmpty(messageSettings.getGestures())) {
                return view.onTouchEvent(motionEvent);
            }
            // otherwise, pass the event to the gesture detector to determine if a motion event
            // occurred on the webview
            gestureDetector.onTouchEvent(motionEvent);
            // we want to ignore scroll events (ACTION_MOVE) with gestures present
            return motionEvent.getAction() == MotionEvent.ACTION_MOVE;
        }

        return false;
    }

    /**
     * Adds the {@link android.view.View.OnLayoutChangeListener} and {@link
     * android.view.View.OnTouchListener}
     */
    private void addListeners() {
        final View contentView = getActivity().findViewById(android.R.id.content);

        // we have an orientation change, wait for it to complete then proceed with webview creation
        if (contentView.getHeight() == 0 || contentView.getWidth() == 0) {
            contentView.addOnLayoutChangeListener(layoutChangeListener);
        } else { // just create the webview and add it to the DialogView
            message.recreateWebViewFrame(contentView.getWidth(), contentView.getHeight());
            updateDialogView();
        }

        final Dialog dialog = getDialog();
        if (dialog != null) {
            // set this fragment onTouchListener to dismiss the IAM if a touch occurs on the decor
            // view
            dialog.getWindow().getDecorView().setOnTouchListener(this);

            // handle on back pressed to dismiss the message
            dialog.setOnKeyListener(
                    (dialogInterface, keyCode, event) -> {
                        if (message != null && keyCode == KeyEvent.KEYCODE_BACK) {
                            message.dismiss();
                        }
                        return false;
                    });
        }
    }

    /** Remove set listeners. */
    private void removeListeners() {
        getActivity()
                .findViewById(android.R.id.content)
                .removeOnLayoutChangeListener(layoutChangeListener);

        final Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().getDecorView().setOnTouchListener(null);
            dialog.setOnKeyListener(null);
        }
    }

    /** Apply the backdrop color and alpha on Dialog's window */
    private void applyBackdropColor() {
        if (message == null) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    "%s (AEPMessage), unable to apply backdrop color.",
                    UNEXPECTED_NULL_VALUE);
            return;
        }

        final MessageSettings messageSettings = message.getMessageSettings();

        if (messageSettings == null) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    "%s (Message Settings), unable to apply backdrop color.",
                    UNEXPECTED_NULL_VALUE);
            return;
        }

        final Dialog dialog = getDialog();

        if (dialog != null) {
            final String backdropColor = messageSettings.getBackdropColor();
            final float backdropOpacity = messageSettings.getBackdropOpacity();
            // alpha values range from 0-255 (0 means fully transparent, 255 means fully opaque)
            final int convertedAlpha = (int) (backdropOpacity * FULLY_OPAQUE_ALPHA_VALUE);
            final Drawable dialogBgDrawable = new ColorDrawable(Color.parseColor(backdropColor));
            dialogBgDrawable.setAlpha(convertedAlpha);
            dialog.getWindow().setBackgroundDrawable(dialogBgDrawable);
        }
    }

    /** Add the IAM WebView as the {@link MessageFragment} Dialog's content view. */
    private void updateDialogView() {
        final Dialog dialog = getDialog();
        final ViewGroup.LayoutParams params = message.getParams();
        final CardView webViewFrame = message.getWebViewFrame();

        if (dialog == null || webViewFrame == null || params == null) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    "%s (Message Fragment), unable to update the MessageFragment Dialog.",
                    UNEXPECTED_NULL_VALUE);
            return;
        }

        webViewFrame.addView(message.getWebView());
        dialog.setContentView(webViewFrame);
        webViewFrame.setOnTouchListener(this);
    }

    /** Show this {@link MessageFragment} */
    @Override
    public int show(final FragmentTransaction transaction, final String tag) {
        Log.trace(ServiceConstants.LOG_TAG, TAG, "MessageFragment was shown.");
        return super.show(transaction, tag);
    }

    /** Dismiss this {@link MessageFragment} */
    @Override
    public void dismiss() {
        Log.trace(ServiceConstants.LOG_TAG, TAG, "MessageFragment was dismissed.");
        super.dismiss();
    }

    // for unit tests
    @VisibleForTesting
    public Map<MessageGesture, String> getGestures() {
        return gestures;
    }

    @VisibleForTesting
    public GestureDetector getGestureDetector() {
        return gestureDetector;
    }

    @VisibleForTesting
    public boolean isDismissedWithGesture() {
        return this.dismissedWithGesture;
    }

    @VisibleForTesting
    public WebViewGestureListener getWebViewGestureListener() {
        return this.webViewGestureListener;
    }

    @VisibleForTesting
    public void setMessagesMonitor(final MessagesMonitor messagesMonitor) {
        this.messagesMonitor = messagesMonitor;
    }
}
