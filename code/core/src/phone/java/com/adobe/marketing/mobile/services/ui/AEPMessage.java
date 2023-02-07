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
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.webkit.WebView;
import android.widget.FrameLayout;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
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
import java.util.concurrent.Executor;

/**
 * The Android implementation for {@link FullscreenMessage}. It creates and starts a {@link
 * MessageFragment} then adds a {@link MessageWebView} containing an in-app message.
 */
class AEPMessage implements FullscreenMessage {

    private static final String TAG = "AEPMessage";
    private static final String FRAGMENT_TAG = "AEPMessageFragment";
    private static final String UNEXPECTED_NULL_VALUE = "Unexpected Null Value";
    private static final int ANIMATION_DURATION = 300;

    // package private vars
    WebView webView;
    ViewGroup rootViewGroup;
    FrameLayout fragmentFrameLayout;
    MessageWebViewRunner messageWebViewRunner;
    int baseRootViewHeight;
    int baseRootViewWidth;
    int frameLayoutResourceId = 0;
    final MessagesMonitor messagesMonitor;
    final FullscreenMessageDelegate listener;
    MessageFragment messageFragment;

    // private vars
    private final String html;
    private MessageSettings settings;
    private final boolean isLocalImageUsed;
    private int orientationWhenShown;
    private boolean isVisible;
    private Animation dismissAnimation;
    private Animation.AnimationListener animationListener;
    private Map<String, String> assetMap = Collections.emptyMap();
    private final Executor executor;

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
        this.isLocalImageUsed = isLocalImageUsed;
        this.executor = executor;
    }

    @Override
    @Nullable public WebView getWebView() {
        return this.webView;
    }

    @Override
    @Nullable public MessageSettings getMessageSettings() {
        return this.settings;
    }

    @VisibleForTesting
    void setVisible(final boolean isVisible) {
        this.isVisible = isVisible;
    }

    /**
     * Starts the {@link MessageFragment}.
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
        final Context appContext =
                ServiceProvider.getInstance().getAppContextService().getApplicationContext();
        if (appContext == null) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    UNEXPECTED_NULL_VALUE + " (context), failed to show the message.");
            listener.onShowFailure();
            return;
        }

        final Activity currentActivity =
                ServiceProvider.getInstance().getAppContextService().getCurrentActivity();
        if (currentActivity == null) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    UNEXPECTED_NULL_VALUE + " (current activity), failed to show the message.");
            listener.onShowFailure();
            return;
        }

        executor.execute(
                () -> {
                    final AEPMessage message = this;

                    // bail if we shouldn't be displaying a message
                    if (!messagesMonitor.show(message, withMessagingDelegateControl)) {
                        listener.onShowFailure();
                        return;
                    }

                    ServiceProvider.getInstance()
                            .getAppContextService()
                            .getCurrentActivity()
                            .runOnUiThread(
                                    () -> {
                                        // find the base root view group and add a frame layout to
                                        // be used for
                                        // displaying the in-app
                                        // message
                                        if (rootViewGroup == null) {
                                            rootViewGroup =
                                                    currentActivity.findViewById(
                                                            android.R.id.content);
                                            // preserve the base root view group height and width
                                            // for future in-app
                                            // message
                                            // measurement calculations
                                            baseRootViewHeight = rootViewGroup.getHeight();
                                            baseRootViewWidth = rootViewGroup.getWidth();
                                        }

                                        // use a random int as a resource id for the message
                                        // fragment frame layout to
                                        // prevent any
                                        // collisions
                                        frameLayoutResourceId = new Random().nextInt();

                                        if (fragmentFrameLayout == null) {
                                            fragmentFrameLayout = new FrameLayout(appContext);
                                            fragmentFrameLayout.setId(frameLayoutResourceId);
                                        }

                                        // add the frame layout to be replaced with the message
                                        // fragment
                                        rootViewGroup.addView(fragmentFrameLayout);

                                        Log.debug(
                                                ServiceConstants.LOG_TAG,
                                                TAG,
                                                "Preparing message fragment to be used in"
                                                        + " displaying the in-app message.");
                                        final FragmentManager fragmentManager =
                                                currentActivity.getFragmentManager();

                                        final Fragment currentMessageFragment =
                                                fragmentManager.findFragmentByTag(FRAGMENT_TAG);

                                        if (currentMessageFragment != null) {
                                            fragmentManager
                                                    .beginTransaction()
                                                    .remove(currentMessageFragment)
                                                    .commit();
                                        }

                                        // prepare a message fragment and replace the frame layout
                                        // with the
                                        // fragment
                                        messageFragment = new MessageFragment();
                                        messageFragment.setAEPMessage(message);

                                        final int id =
                                                ServiceProvider.getInstance()
                                                        .getAppContextService()
                                                        .getApplicationContext()
                                                        .getResources()
                                                        .getIdentifier(
                                                                Integer.toString(
                                                                        frameLayoutResourceId),
                                                                "id",
                                                                ServiceProvider.getInstance()
                                                                        .getAppContextService()
                                                                        .getApplicationContext()
                                                                        .getPackageName());
                                        final FragmentTransaction transaction =
                                                fragmentManager.beginTransaction();
                                        transaction
                                                .replace(id, messageFragment, FRAGMENT_TAG)
                                                .addToBackStack(null)
                                                .commit();
                                        fragmentManager.executePendingTransactions();
                                    });
                });
    }

    /** Dismisses the message. */
    @Override
    public void dismiss() {
        removeFromRootViewGroup();
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
            final Intent intent =
                    ServiceProvider.getInstance().getUIService().getIntentWithURI(url);
            Activity currentActivity =
                    ServiceProvider.getInstance().getAppContextService().getCurrentActivity();
            if (currentActivity != null) {
                currentActivity.startActivity(intent);
            }
        } catch (final NullPointerException ex) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    "Could not open the url from the message " + ex.getMessage());
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

    /**
     * Returns the {@link MessageSettings} passed in the {@link AEPMessage} constructor.
     *
     * @return {@code MessageSettings} object defining layout and behavior of the new message
     */
    MessageSettings getSettings() {
        return this.settings;
    }

    /** Invoked after the message is successfully shown. */
    void viewed() {
        isVisible = true;

        // notify listeners
        listener.onShow(this);
        final MessagingDelegate delegate = ServiceProvider.getInstance().getMessageDelegate();
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

    /**
     * Returns the message visibility status.
     *
     * @return a {@code boolean} containing true if the message is currently visible, false
     *     otherwise
     */
    boolean isMessageVisible() {
        return isVisible;
    }

    /**
     * Creates the {@link MessageWebViewRunner} and posts it to the main {@link Handler} to create
     * the {@link MessageWebView}.
     */
    void showInRootViewGroup() {
        final int currentOrientation =
                ServiceProvider.getInstance().getDeviceInfoService().getCurrentOrientation();

        if (isVisible && orientationWhenShown == currentOrientation) {
            return;
        }

        orientationWhenShown = currentOrientation;
        messageWebViewRunner = new MessageWebViewRunner(this);
        messageWebViewRunner.setLocalAssetsMap(assetMap);
        final Activity currentActivity =
                ServiceProvider.getInstance().getAppContextService().getCurrentActivity();
        if (currentActivity != null) {
            currentActivity.runOnUiThread(messageWebViewRunner);
        }
    }

    /** Tears down views and listeners used to display the {@link AEPMessage}. */
    void cleanup() {
        Log.trace(ServiceConstants.LOG_TAG, TAG, "Cleaning the AEPMessage.");
        if (!messagesMonitor.dismiss()) {
            return;
        }

        // notify message listeners
        if (isVisible) { // If this flag is false, it means we had some error and did not call
            // onShow() notifiers
            listener.onDismiss(this);

            final MessagingDelegate delegate = ServiceProvider.getInstance().getMessageDelegate();
            if (delegate != null) {
                delegate.onDismiss(this);
            }
        }

        isVisible = false;

        // remove touch listeners
        webView.setOnTouchListener(null);
        fragmentFrameLayout.setOnTouchListener(null);
        rootViewGroup.setOnTouchListener(null);
        // remove message webview, frame layout, and backdrop from the root view group
        rootViewGroup.removeView(webView);
        rootViewGroup.removeView(fragmentFrameLayout);
        rootViewGroup.removeView(messageWebViewRunner.backdrop);
        messageFragment = null;
        fragmentFrameLayout = null;
        webView = null;
        // clean the message fragment
        final Activity currentActivity =
                ServiceProvider.getInstance().getAppContextService().getCurrentActivity();
        if (currentActivity == null) {
            return;
        }
        final FragmentManager fragmentManager = currentActivity.getFragmentManager();
        if (fragmentManager == null) {
            return;
        }

        final Fragment messageFragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG);
        if (messageFragment != null) {
            fragmentManager.beginTransaction().remove(messageFragment).commit();
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
     * Removes the {@link WebView} from the root view group. If the {@link WebView} was dismissed
     * via a swipe {@link MessageSettings.MessageGesture}, no additional dismiss {@link
     * MessageSettings.MessageAnimation} is applied. Otherwise, the dismissal {@code
     * MessageSettings.MessageAnimation} retrieved from the {@link MessageSettings} object is used.
     */
    private void removeFromRootViewGroup() {
        if (rootViewGroup == null) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    TAG,
                    UNEXPECTED_NULL_VALUE + " (root viewgroup), failed to dismiss the message.");
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
                            cleanup();
                        }

                        @Override
                        public void onAnimationRepeat(final Animation animation) {}
                    };
            dismissAnimation.setAnimationListener(animationListener);
            webView.startAnimation(dismissAnimation);
        } else { // otherwise, just clean the views
            cleanup();
        }
    }

    /**
     * Create a message dismissal {@link Animation}.
     *
     * @return {@code Animation} object defining the animation that will be performed when the
     *     message is dismissed.
     */
    private Animation setupDismissAnimation() {
        final MessageSettings.MessageAnimation animation = getSettings().getDismissAnimation();

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
                dismissAnimation = new TranslateAnimation(0, 0, 0, -baseRootViewHeight);
                break;
            case FADE:
                // fade out from 100% to 0%.
                dismissAnimation = new AlphaAnimation(1, 0);
                dismissAnimation.setInterpolator(new DecelerateInterpolator());
                break;
            case LEFT:
                dismissAnimation = new TranslateAnimation(0, -baseRootViewWidth, 0, 0);
                break;
            case RIGHT:
                dismissAnimation = new TranslateAnimation(0, baseRootViewWidth, 0, 0);
                break;
            case BOTTOM:
                dismissAnimation = new TranslateAnimation(0, 0, 0, baseRootViewHeight * 2);
                break;
            case CENTER:
                dismissAnimation =
                        new TranslateAnimation(0, baseRootViewWidth, 0, baseRootViewHeight);
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

    // added for unit testing
    Animation.AnimationListener getAnimationListener() {
        return this.animationListener;
    }
}
