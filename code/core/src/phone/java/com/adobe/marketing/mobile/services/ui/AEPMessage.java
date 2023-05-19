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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.cardview.widget.CardView;
import com.adobe.marketing.mobile.services.DeviceInforming;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.MessagingDelegate;
import com.adobe.marketing.mobile.services.ServiceConstants;
import com.adobe.marketing.mobile.services.ServiceProvider;
import com.adobe.marketing.mobile.services.ui.internal.MessagesMonitor;
import com.adobe.marketing.mobile.util.StringUtils;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The Android implementation for {@link FullscreenMessage}. It creates and starts a {@link
 * MessageFragment} then adds a {@link WebView} containing an in-app message.
 */
class AEPMessage implements FullscreenMessage {

    private static final String TAG = "AEPMessage";
    private static final String FRAGMENT_TAG = "AEPMessageFragment";
    private static final String UNEXPECTED_NULL_VALUE = "Unexpected Null Value";
    private static final int ANIMATION_DURATION = 300;
    private static final String UTF_8 = "UTF-8";

    // package private vars
    int parentViewHeight;
    int parentViewWidth;
    final FullscreenMessageDelegate listener;
    final MessagesMonitor messagesMonitor;
    MessageFragment messageFragment;

    // private vars
    private WebView webView;
    private CardView webViewFrame;
    private ViewGroup.LayoutParams params;
    private final String html;
    private MessageSettings settings;
    private Animation dismissAnimation;
    private Animation.AnimationListener animationListener;
    private Map<String, String> assetMap = Collections.emptyMap();
    private final Executor executor;
    private MessageWebViewClient webViewClient;

    /**
     * Constructor.
     *
     * @param html {@code String} containing the html payload
     * @param listener {@link FullscreenMessageDelegate} listening for message lifecycle events
     * @param isLocalImageUsed {@code boolean} If true, an image from the app bundle will be used
     *     for the message
     * @param messagesMonitor {@link MessagesMonitor} instance that tracks and provides the
     *     displayed status for a message
     * @param settings {@link MessageSettings} object defining layout and behavior of the new
     *     message
     * @param executor {@link Executor} to be used for executing code checking if a {@link
     *     AEPMessage} should be displayed
     * @throws MessageCreationException If the passed in {@code FullscreenMessageDelegate} is null
     */
    AEPMessage(
            final String html,
            final FullscreenMessageDelegate listener,
            final boolean isLocalImageUsed,
            final MessagesMonitor messagesMonitor,
            final MessageSettings settings,
            final Executor executor)
            throws MessageCreationException {
        if (listener == null) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    "Message couldn't be created because the FullscreenMessageDelegate was null.");
            throw new MessageCreationException(
                    "Message couldn't be created because the FullscreenMessageDelegate was null.");
        }

        this.listener = listener;
        this.messagesMonitor = messagesMonitor;
        this.settings = settings;
        this.html = html;
        this.executor = executor;
    }

    @Override
    @Nullable public WebView getWebView() {
        return webView;
    }

    @Nullable CardView getWebViewFrame() {
        return webViewFrame;
    }

    void setWebViewFrame(final CardView webViewFrame) {
        this.webViewFrame = webViewFrame;
    }

    @Override
    @Nullable public MessageSettings getMessageSettings() {
        return this.settings;
    }

    @VisibleForTesting
    void setWebView(final WebView webView) {
        this.webView = webView;
    }

    /**
     * Returns the {@link ViewGroup.LayoutParams} created for this message.
     *
     * @return the created {@code ViewGroup.LayoutParams}
     */
    ViewGroup.LayoutParams getParams() {
        return params;
    }

    /**
     * Sets the {@link ViewGroup.LayoutParams} for this message.
     *
     * @param params the {@code ViewGroup.LayoutParams} to be set
     */
    void setParams(final ViewGroup.LayoutParams params) {
        this.params = params;
    }

    /**
     * Shows the {@link AEPMessage} by starting the {@link MessageFragment}.
     *
     * <p>The {@code MessageFragment} will not be shown if {@link MessagesMonitor#isDisplayed()} is
     * true.
     */
    @SuppressLint("ResourceType")
    @Override
    public void show() {
        show(true);
    }

    @Override
    public void show(final boolean withMessagingDelegateControl) {
        final Context appContext = getApplicationContext();
        if (appContext == null) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    "%s (context), failed to show the message.",
                    UNEXPECTED_NULL_VALUE);
            listener.onShowFailure();
            return;
        }

        final Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    "%s (current activity), failed to show the message.",
                    UNEXPECTED_NULL_VALUE);
            listener.onShowFailure();
            return;
        }

        executor.execute(
                () -> {
                    final AEPMessage message = this;

                    // create the webview if needed
                    if (webView == null) {
                        webView = createWebView();
                    }

                    // bail if we shouldn't be displaying a message
                    if (!messagesMonitor.show(message, withMessagingDelegateControl)) {
                        listener.onShowFailure();
                        return;
                    }

                    if (messageFragment == null) {
                        messageFragment = new MessageFragment();
                    }

                    messageFragment.setAEPMessage(AEPMessage.this);

                    currentActivity.runOnUiThread(
                            () -> {
                                Log.debug(
                                        ServiceConstants.LOG_TAG,
                                        TAG,
                                        "Preparing message fragment to be used in displaying the"
                                                + " in-app message.");

                                // Show the MessageFragment with iam.
                                final FragmentManager fragmentManager =
                                        currentActivity.getFragmentManager();
                                messageFragment.show(fragmentManager, FRAGMENT_TAG);
                            });
                });
    }

    /** Dismisses the message. */
    @Override
    public void dismiss() {
        if (!messagesMonitor.dismiss()) {
            return;
        }
        // add a dismiss animation if the webview wasn't previously removed via a swipe gesture
        if (!messageFragment.dismissedWithGesture) {
            dismissAnimation = setupDismissAnimation();
            animationListener =
                    new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(final Animation animation) {}

                        @Override
                        public void onAnimationEnd(final Animation animation) {
                            // wait for the animation to end then clean the views
                            cleanup();
                        }

                        @Override
                        public void onAnimationRepeat(final Animation animation) {}
                    };
            dismissAnimation.setAnimationListener(animationListener);
            webViewFrame.startAnimation(dismissAnimation);
            return;
        } // otherwise, just clean the views

        cleanup();
    }

    /**
     * Opens the url as a {@link Intent#ACTION_VIEW} intent.
     *
     * @param url {@code String} to be opened
     */
    @Override
    public void openUrl(final String url) {
        if (StringUtils.isNullOrEmpty(url)) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    "Could not open url because it is null or empty.");
            return;
        }

        try {
            final Intent intent = getUIService().getIntentWithURI(url);
            final Activity currentActivity = getCurrentActivity();
            if (currentActivity != null) {
                currentActivity.startActivity(intent);
            }
        } catch (final NullPointerException ex) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    "Could not open the url from the message %s",
                    ex.getMessage());
        }
    }

    /**
     * Returns the parent of this {@link AEPMessage}.
     *
     * @return {@code Object} which created this {@code AEPMessage}
     */
    @Override
    public Object getParent() {
        return this.settings.getParent();
    }

    /** Invoked after the message is successfully shown. */
    void viewed() {
        // notify listeners
        listener.onShow(this);
        final MessagingDelegate delegate = getMessagingDelegate();
        if (delegate != null) {
            delegate.onShow(this);
        }
    }

    /**
     * Returns the {@link MessageFragment} created for this message.
     *
     * @return the created {@code MessageFragment}
     */
    MessageFragment getMessageFragment() {
        return messageFragment;
    }

    /**
     * Returns the message html payload.
     *
     * @return the {@link AEPMessage}'s HTML payload {@code String}
     */
    String getMessageHtml() {
        return html;
    }

    /** Tears down views and listeners used to display the {@link AEPMessage}. */
    void cleanup() {
        Log.trace(ServiceConstants.LOG_TAG, TAG, "Cleaning the AEPMessage.");

        listener.onDismiss(this);
        webViewFrame.setOnTouchListener(null);
        webView.setOnTouchListener(null);
        if (dismissAnimation != null) {
            dismissAnimation.setAnimationListener(null);
            dismissAnimation = null;
        }

        delegateFullscreenMessageDismiss();
        removeFullscreenMessage();
    }

    /** Removes then cleans up the Messaging IAM. */
    void removeFullscreenMessage() {
        messageFragment.dismiss();
        webViewFrame = null;
        webView = null;
        messageFragment = null;
    }

    /**
     * Checks if a custom {@link MessagingDelegate} was set in the {@link
     * com.adobe.marketing.mobile.MobileCore}. If it was set, {@code MessagingDelegate#onDismiss} is
     * called and the {@link AEPMessage} object is passed to the custom delegate.
     */
    private void delegateFullscreenMessageDismiss() {
        final MessagingDelegate messageDelegate = getMessagingDelegate();

        if (messageDelegate != null) {
            messageDelegate.onDismiss(this);
        }
    }

    /**
     * The asset map contains the mapping between a remote image asset url and it's cached location.
     *
     * @param assetMap The {@code Map<String, String} object containing the mapping between a remote
     *     asset url and its cached location.
     */
    @Override
    public void setLocalAssetsMap(final Map<String, String> assetMap) {
        if (assetMap != null && !assetMap.isEmpty()) {
            this.assetMap = new HashMap<>(assetMap);
        }
    }

    /**
     * Sets or updates the {@link MessageSettings} for the current fullscreen message.
     *
     * @param messageSettings {@link MessageSettings} object defining layout and behavior of the new
     *     message.
     */
    @Override
    public void setMessageSetting(final MessageSettings messageSettings) {
        this.settings = messageSettings;
    }

    /**
     * Recreates the {@link WebView} frame used for displaying the Messaging IAM using the {@link
     * MessageWebViewRunner}. This method should be called after a device orientation change occurs.
     *
     * @param parentViewWidth {@code int} containing the width of the parent activity
     * @param parentViewHeight {@code int} containing the height of the parent activity
     */
    void recreateWebViewFrame(final int parentViewWidth, final int parentViewHeight) {
        this.parentViewWidth = parentViewWidth;
        this.parentViewHeight = parentViewHeight;

        try {
            MessageWebViewRunner messageWebViewRunner = new MessageWebViewRunner(this);
            messageWebViewRunner.run();
        } catch (final Exception exception) {
            Log.warning(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    "Exception occurred when creating the MessageWebViewRunner: %s",
                    exception.getMessage());
        }
    }

    /**
     * Creates a {@code WebView} to use for displaying an in-app message.
     *
     * @return {@link WebView} to use for displaying the in-app message.
     */
    private WebView createWebView() {
        final AtomicReference<WebView> webViewAtomicReference = new AtomicReference<>();
        @SuppressLint("SetJavaScriptEnabled")
        final Runnable createWebViewRunnable =
                () -> {
                    final WebView newWebView = new WebView(getApplicationContext());
                    // assign a random resource id to identify this webview
                    newWebView.setId(Math.abs(new Random().nextInt()));
                    newWebView.setVerticalScrollBarEnabled(true);
                    newWebView.setHorizontalScrollBarEnabled(true);
                    newWebView.setScrollbarFadingEnabled(true);
                    newWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
                    newWebView.setBackgroundColor(Color.TRANSPARENT);

                    webViewClient = new MessageWebViewClient(AEPMessage.this);
                    webViewClient.setLocalAssetsMap(assetMap);
                    newWebView.setWebViewClient(webViewClient);

                    final WebSettings webviewSettings = newWebView.getSettings();
                    webviewSettings.setJavaScriptEnabled(true);
                    webviewSettings.setAllowFileAccess(false);
                    webviewSettings.setDomStorageEnabled(true);
                    webviewSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
                    webviewSettings.setDefaultTextEncodingName(UTF_8);

                    // Disallow need for a user gesture to play media.
                    webviewSettings.setMediaPlaybackRequiresUserGesture(false);

                    if (ServiceProvider.getInstance()
                                    .getDeviceInfoService()
                                    .getApplicationCacheDir()
                            != null) {
                        webviewSettings.setDatabaseEnabled(true);
                    }

                    webViewAtomicReference.set(newWebView);
                };

        final RunnableFuture<Void> createWebviewTask =
                new FutureTask<>(createWebViewRunnable, null);

        getCurrentActivity().runOnUiThread(createWebviewTask);

        try {
            createWebviewTask.get(1, TimeUnit.SECONDS);
            return webViewAtomicReference.get();
        } catch (final InterruptedException | ExecutionException | TimeoutException exception) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    "Exception occurred when creating the webview: %s",
                    exception.getLocalizedMessage());
            listener.onShowFailure();
            createWebviewTask.cancel(true);
            return null;
        }
    }

    /**
     * Create a message dismissal {@link Animation}.
     *
     * @return {@code Animation} object defining the animation that will be performed when the
     *     message is dismissed.
     */
    private Animation setupDismissAnimation() {
        final MessageSettings.MessageAnimation animation = settings.getDismissAnimation();

        if (animation == null) {
            Log.trace(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    "No dismiss animation found in the message settings. Message will be removed.");
            return new TranslateAnimation(0, 0, 0, 0);
        }

        Log.trace(
                ServiceConstants.LOG_TAG,
                TAG,
                "Creating dismiss animation for " + animation.name());
        final Animation dismissAnimation;

        switch (animation) {
            case TOP:
                dismissAnimation = new TranslateAnimation(0, 0, 0, -parentViewHeight);
                break;
            case FADE:
                // fade out from 100% to 0%.
                dismissAnimation = new AlphaAnimation(1, 0);
                dismissAnimation.setInterpolator(new DecelerateInterpolator());
                break;
            case LEFT:
                dismissAnimation = new TranslateAnimation(0, -parentViewWidth, 0, 0);
                break;
            case RIGHT:
                dismissAnimation = new TranslateAnimation(0, parentViewWidth, 0, 0);
                break;
            case BOTTOM:
                dismissAnimation = new TranslateAnimation(0, 0, 0, parentViewHeight * 2);
                break;
            case CENTER:
                dismissAnimation = new TranslateAnimation(0, parentViewWidth, 0, parentViewHeight);
                break;
            default:
                // no animation
                dismissAnimation = new TranslateAnimation(0, 0, 0, 0);
                break;
        }

        // extend the fade animation as it was fading out too fast
        if (animation.equals(MessageSettings.MessageAnimation.FADE)) {
            dismissAnimation.setDuration(ANIMATION_DURATION * 2);
        } else {
            dismissAnimation.setDuration(ANIMATION_DURATION);
        }

        dismissAnimation.setFillAfter(true);

        return dismissAnimation;
    }

    // service provider getters
    private UIService getUIService() {
        return ServiceProvider.getInstance().getUIService();
    }

    private DeviceInforming getDeviceInfoService() {
        return ServiceProvider.getInstance().getDeviceInfoService();
    }

    private MessagingDelegate getMessagingDelegate() {
        return ServiceProvider.getInstance().getMessageDelegate();
    }

    private Context getApplicationContext() {
        return ServiceProvider.getInstance().getAppContextService().getApplicationContext();
    }

    private Activity getCurrentActivity() {
        return ServiceProvider.getInstance().getAppContextService().getCurrentActivity();
    }

    // added for unit testing
    @VisibleForTesting
    Animation.AnimationListener getAnimationListener() {
        return animationListener;
    }
}
