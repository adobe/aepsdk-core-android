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

import java.util.Map;

/**
 * An extension of {@link android.app.DialogFragment} used to display in-app messages with custom
 * locations and dimensions.
 */
public class MessageFragment extends android.app.DialogFragment implements View.OnTouchListener {

    private static final String TAG = "MessageFragment";
    private static final String UNEXPECTED_NULL_VALUE = "Unexpected Null Value";
    private static final int FULLY_OPAQUE_ALPHA_VALUE = 255;

    // keys for Arguments Bundle
    private static final String ARGUMENT_KEY_BACKDROP_COLOR = "backdropColor";
    private static final String ARGUMENT_KEY_BACKDROP_OPACITY = "backdropOpacity";
    private static final String ARGUMENT_KEY_IS_UI_TAKE_OVER = "isUiTakeOver";

    protected boolean dismissedWithGesture = false;
    protected AEPMessage message;
    protected GestureDetector gestureDetector;
    private MessagesMonitor messagesMonitor;

    protected WebViewGestureListener webViewGestureListener;
    protected Map<MessageGesture, String> gestures;

    // layout change listener to listen for layout changes in parent activity's content ViewGroup.
    // upon orientation change, wait until the re-layouting of the content view is completed before creating the webview.
    private final View.OnLayoutChangeListener layoutChangeListener = new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(final View v, final int left, final int top, final int right, final int bottom,
                                   final int oldLeft, final int oldTop, final int oldRight,
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

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);

        // make sure we have a valid message before trying to proceed
        if (message == null) {
            Log.debug(ServiceConstants.LOG_TAG,
                    TAG,
                    "%s (Message Fragment), failed to show the message.",UNEXPECTED_NULL_VALUE );
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

        // store message gestures if available
        final Map<MessageGesture, String> retrievedGestures = message.getSettings().getGestures();

        if (retrievedGestures != null && !retrievedGestures.isEmpty()) {
            gestures = retrievedGestures;
        }

        // initialize the gesture detector and listener
        webViewGestureListener = new WebViewGestureListener(this);
        gestureDetector = new GestureDetector(ServiceProvider.getInstance().getAppContextService().getApplicationContext(), webViewGestureListener);

        setStyle(DialogFragment.STYLE_NORMAL,
                android.R.style.Theme_Translucent_NoTitleBar);
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
        if (webView != null && message.getWebView().getParent() != null) {
            ((ViewGroup) message.getWebView().getParent()).removeView(message.getWebView());
        }
    }

    @Override
    public boolean onTouch(final View view, final MotionEvent motionEvent) {
        if (message == null) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    TAG, "%s (message), unable to handle the touch event on %s",
                    UNEXPECTED_NULL_VALUE, view.getClass().getSimpleName());
            return true;
        }

        final WebView webView = message.getWebView();
        if (webView == null) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    UNEXPECTED_NULL_VALUE
                            + " (webview), unable to handle the touch event on "
                            + view.getClass().getSimpleName());
            return true;
        }

        final int motionEventAction = motionEvent.getAction();

        // determine if the tap occurred outside the webview
        if ((motionEventAction == MotionEvent.ACTION_DOWN
                || motionEventAction == MotionEvent.ACTION_BUTTON_PRESS)
                && view.getId() != message.getWebView().getId()) {
            Log.trace(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    "Detected tap on %s", view.getClass().getSimpleName());

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
            Log.trace(ServiceConstants.LOG_TAG,
                    TAG, "UI takeover is true, ignoring the tap.");
            return true;
        }

        // determine if the tapped view is the webview
        if (view.getId() == message.getWebView().getId()) {
            // if we have no gestures just pass the touch event to the webview
            if (message.getMessageSettings().getGestures() == null
                    || message.getMessageSettings().getGestures().isEmpty()) {
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
     * Adds the {@link android.view.View.OnLayoutChangeListener} and {@link android.view.View.OnTouchListener}
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
            // if ui takeover is disabled, dismiss the IAM if a touch occurs on the decor view
            if (!isUiTakeOverEnabled()) {
                dialog.getWindow().getDecorView().setOnTouchListener(this);
            }

            // handle on back pressed to dismiss the message
            dialog.setOnKeyListener((dialogInterface, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (message != null) {
                        message.dismiss();
                    }
                    return true;
                }
                return false;
            });
        }
    }

    /**
     * Remove the {@link android.view.View.OnLayoutChangeListener}
     */
    private void removeListeners() {
        getActivity().findViewById(android.R.id.content).removeOnLayoutChangeListener(layoutChangeListener);
    }

    /**
     * Apply the backdrop color and alpha on Dialog's window
     */
    private void applyBackdropColor() {
        final Dialog dialog = getDialog();

        if (dialog != null) {
            final Bundle bundle = getArguments();

            final String backdropColor = bundle.getString(ARGUMENT_KEY_BACKDROP_COLOR);
            final float backdropOpacity = bundle.getFloat(ARGUMENT_KEY_BACKDROP_OPACITY);
            // alpha values range from 0-255 (0 means fully transparent, 255 means fully opaque)
            final int convertedAlpha = (int) (backdropOpacity * FULLY_OPAQUE_ALPHA_VALUE);
            final Drawable dialogBgDrawable = new ColorDrawable(Color.parseColor(backdropColor));
            dialogBgDrawable.setAlpha(convertedAlpha);
            dialog.getWindow().setBackgroundDrawable(dialogBgDrawable);
        }
    }

    /**
     * Add the IAM WebView as the {@link MessageFragment} Dialog's content view.
     */
    private void updateDialogView() {
        final Dialog dialog = getDialog();
        final ViewGroup.LayoutParams params = message.getParams();
        final CardView framedWebView = message.getFramedWebView();

        if (dialog == null || framedWebView == null || params == null) {
            Log.debug(ServiceConstants.LOG_TAG,
                    TAG, "%s (Message Fragment), unable to update the MessageFragment Dialog.",
                    UNEXPECTED_NULL_VALUE);
            return;
        }

        dialog.setContentView(framedWebView, params);
        framedWebView.setOnTouchListener(this);
        message.setFramedWebView(framedWebView);
    }

    /**
     * Show this {@link MessageFragment}
     */
    @Override
    public int show(final FragmentTransaction transaction, String tag) {
        Log.trace(ServiceConstants.LOG_TAG,
                TAG, "MessageFragment was shown.");
        return super.show(transaction, tag);
    }

    /**
     * Dismiss this {@link MessageFragment}
     */
    @Override
    public void dismiss() {
        Log.trace(ServiceConstants.LOG_TAG,
                TAG, "MessageFragment was dismissed.");
        super.dismiss();
    }

    /**
     * Returns true if the {@link MessageFragment} should take over the UI and not allow touches outside the
     * fragment to dismiss the message.
     *
     * @return {@code boolean} containing true if UI takeover is enabled and false otherwise
     */
    private boolean isUiTakeOverEnabled() {
        final Bundle bundle = getArguments();
        return bundle != null && bundle.getBoolean(ARGUMENT_KEY_IS_UI_TAKE_OVER, false);
    }

    /**
     * Return the {@code String} containing the backdrop color key name.
     *
     * @return a {@code String} containing the backdrop color key name.
     */
    public static String getArgumentKeyBackdropColor() {
        return ARGUMENT_KEY_BACKDROP_COLOR;
    }

    /**
     * Return the {@code String} containing the backdrop opacity key name.
     *
     * @return a {@code String} containing the backdrop opacity key name.
     */
    public static String getArgumentKeyBackdropOpacity() {
        return ARGUMENT_KEY_BACKDROP_OPACITY;
    }

    /**
     * Return the {@code String} containing the ui takeover key name.
     *
     * @return a {@code String} containing the ui takeover key name.
     */
    public static String getArgumentKeyIsUiTakeOver() {
        return ARGUMENT_KEY_IS_UI_TAKE_OVER;
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