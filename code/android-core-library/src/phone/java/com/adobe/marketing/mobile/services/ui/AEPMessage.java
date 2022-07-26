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
import android.content.Intent;
import android.os.Handler;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.internal.context.App;
import com.adobe.marketing.mobile.internal.utility.StringUtils;
import com.adobe.marketing.mobile.services.ServiceProvider;
import com.adobe.marketing.mobile.services.ui.internal.MessagesMonitor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * The Android implementation for {@link FullscreenMessage}. It creates and starts a {@link MessageFragment}
 * then adds a {@link MessageWebView} containing an in-app message.
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
    int baseRootViewHeight;
    int baseRootViewWidth;
    int frameLayoutResourceId = 0;
    final MessagesMonitor messagesMonitor;
    FullscreenMessageDelegate fullScreenMessageDelegate;

    // private vars
    private final String html;
    private MessageSettings settings;
    private final boolean isLocalImageUsed;
    private int orientationWhenShown;
    private boolean isVisible;
    private MessageFragment messageFragment;
    private MessageWebViewRunner messageWebViewRunner;
    private Animation dismissAnimation;
    private Animation.AnimationListener animationListener;
    private Map<String, String> assetMap = Collections.emptyMap();

    /**
     * Constructor.
     *
     * @param html             {@code String} containing the html payload
     * @param messageDelegate  {@link FullscreenMessageDelegate} listening for message lifecycle events
     * @param isLocalImageUsed {@code boolean} If true, an image from the app bundle will be used for the message
     * @param messagesMonitor  {@link MessagesMonitor} instance that tracks and provides the displayed status for a message
     * @param settings         {@link MessageSettings} object defining layout and behavior of the new message
     * @throws MessageCreationException If the passed in {@code FullscreenMessageDelegate} is null
     */
    AEPMessage(final String html, final FullscreenMessageDelegate messageDelegate,
               final boolean isLocalImageUsed,
               final MessagesMonitor messagesMonitor, final MessageSettings settings) throws MessageCreationException {
        if (messageDelegate == null) {
            MobileCore.log(LoggingMode.DEBUG, TAG, "Message couldn't be created because the FullscreenMessageDelegate was null.");
            throw new MessageCreationException("Message couldn't be created because the FullscreenMessageDelegate was null.");
        }

        this.fullScreenMessageDelegate = messageDelegate;
        this.messagesMonitor = messagesMonitor;
        this.settings = settings;
        this.html = html;
        this.isLocalImageUsed = isLocalImageUsed;
    }

    /**
     * Starts the {@link MessageFragment}.
     * <p>
     * The {@code MessageFragment} will not be shown if {@link MessagesMonitor#isDisplayed()} is true.
     */
    @SuppressLint("ResourceType")
    @Override
    public void show() {
        if (messagesMonitor != null && messagesMonitor.isDisplayed()) {
            MobileCore.log(LoggingMode.DEBUG, TAG, "Message couldn't be displayed, another message is displayed at this time.");
            fullScreenMessageDelegate.onShowFailure();
            return;
        }

        if (!fullScreenMessageDelegate.shouldShowMessage(this)) {
            MobileCore.log(LoggingMode.DEBUG, TAG,
                    "Message couldn't be displayed, FullscreenMessageDelegate#shouldShowMessage states the message should not be displayed.");
            return;
        }

        final Activity currentActivity = App.getInstance().getCurrentActivity();

        if (currentActivity == null) {
            MobileCore.log(LoggingMode.DEBUG, TAG, UNEXPECTED_NULL_VALUE + " (current activity), failed to show the message.");
            fullScreenMessageDelegate.onShowFailure();
            return;
        }

        // find the base root view group and add a frame layout to be used for displaying the in-app message.
        if (rootViewGroup == null) {
            rootViewGroup = currentActivity.findViewById(android.R.id.content);
            // preserve the base root view group height and width for future in-app message measurement calculations
            baseRootViewHeight = rootViewGroup.getHeight();
            baseRootViewWidth = rootViewGroup.getWidth();
        }

        // use a random int as a resource id for the message fragment frame layout to prevent any collisions
        frameLayoutResourceId = new Random().nextInt();

        if (fragmentFrameLayout == null) {
            fragmentFrameLayout = new FrameLayout(App.getInstance().getAppContext());
            fragmentFrameLayout.setId(frameLayoutResourceId);
        }

        if (messagesMonitor != null) {
            messagesMonitor.displayed();
        }

        // replace the existing frame layout (if present) with a new MessageFragment
        final AEPMessage message = this;

        currentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // add the frame layout to be replaced with the message fragment
                rootViewGroup.addView(fragmentFrameLayout);

                final FragmentManager fragmentManager = App.getInstance().getCurrentActivity().getFragmentManager();

                // ensure there are no existing webview fragments before creating a new one
                final Fragment currentMessageFragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG);

                if (currentMessageFragment != null) {
                    fragmentManager.beginTransaction().remove(currentMessageFragment).commit();
                }

                // prepare a message fragment and replace the frame layout with the fragment
                messageFragment = new MessageFragment();
                messageFragment.setAEPMessage(message);
                final int id = App.getInstance().getAppContext().getResources().getIdentifier(Integer.toString(frameLayoutResourceId),
                        "id",
                        App.getInstance().getAppContext().getPackageName());
                final FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(id, messageFragment, FRAGMENT_TAG).addToBackStack(null).commit();
                fragmentManager.executePendingTransactions();
            }
        });
    }

    /**
     * Dismisses the message.
     */
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
            MobileCore.log(LoggingMode.DEBUG, TAG, "Could not open url because it is null or empty.");
            return;
        }

        try {
            final Intent intent = ServiceProvider.getInstance().getUIService().getIntentWithURI(url);
            App.getInstance().getCurrentActivity().startActivity(intent);
        } catch (final NullPointerException ex) {
            MobileCore.log(LoggingMode.WARNING, TAG, "Could not open the url from the message " + ex.getMessage());
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

    /**
     * Invoked after the message is successfully shown.
     */
    void viewed() {
        isVisible = true;

        if (fullScreenMessageDelegate != null) {
            fullScreenMessageDelegate.onShow(this);
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
     * @return a {@code boolean} containing true if the message is currently visible, false otherwise
     */
    boolean isMessageVisible() {
        return isVisible;
    }

    /**
     * Creates the {@link MessageWebViewRunner} and posts it to the main {@link Handler} to
     * create the {@link MessageWebView}.
     */
    void showInRootViewGroup() {
        final int currentOrientation = App.getInstance().getCurrentOrientation();

        if (isVisible && orientationWhenShown == currentOrientation) {
            return;
        }

        orientationWhenShown = currentOrientation;
        messageWebViewRunner = new MessageWebViewRunner(this);
        messageWebViewRunner.setLocalAssetsMap(assetMap);
        final Activity currentActivity = App.getInstance().getCurrentActivity();
        currentActivity.runOnUiThread(messageWebViewRunner);
    }

    /**
     * Tears down views and listeners used to display the {@link AEPMessage}.
     */
    void cleanup() {
        MobileCore.log(LoggingMode.DEBUG, TAG, "Cleaning the AEPMessage.");
        delegateFullscreenMessageDismiss();

        if (messagesMonitor != null) {
            messagesMonitor.dismissed();
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
        final FragmentManager fragmentManager = App.getInstance().getCurrentActivity().getFragmentManager();
        final Fragment messageFragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG);

        if (messageFragment != null) {
            fragmentManager.beginTransaction().remove(messageFragment).commit();
        }
    }

    /**
     * The asset map contains the mapping between a remote image asset url and it's cached location.
     *
     * @param assetMap The {@code Map<String, String} object containing the mapping between a remote
     *                 asset url and its cached location.
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
     * @param messageSettings {@link MessageSettings} object defining layout and behavior of the new message.
     */
    @Override
    public void setMessageSetting(MessageSettings messageSettings) {
        this.settings = messageSettings;
    }

    /**
     * Checks if a custom {@link FullscreenMessageDelegate} was set in the {@link MobileCore}.
     * If it was set, {@code FullscreenMessageDelegate#onDismiss} is called and the {@link AEPMessage}
     * object is passed to the custom delegate. Note, this method applies to custom delegates only.
     * onDismiss is called automatically if the internal {@code FullscreenMessageDelegate} is used.
     */
    private void delegateFullscreenMessageDismiss() {
        final FullscreenMessageDelegate messageDelegate = ServiceProvider.getInstance().getMessageDelegate();

        if (messageDelegate != null) {
            messageDelegate.onDismiss(this);
        }
    }

    /**
     * Removes the {@link WebView} from the root view group.
     * If the {@link WebView} was dismissed via a swipe {@link MessageSettings.MessageGesture}, no additional dismiss
     * {@link MessageSettings.MessageAnimation} is applied.
     * Otherwise, the dismissal {@code MessageSettings.MessageAnimation} retrieved from the {@link MessageSettings} object is used.
     */
    private void removeFromRootViewGroup() {
        if (rootViewGroup == null) {
            MobileCore.log(LoggingMode.DEBUG, TAG, UNEXPECTED_NULL_VALUE + " (root viewgroup), failed to dismiss the message.");
            return;
        }

        // add a dismiss animation if the webview wasn't previously removed via a swipe gesture
        if (!messageFragment.dismissedWithGesture) {
            dismissAnimation = setupDismissAnimation();

            animationListener = new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    cleanup();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
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
     * @return {@code Animation} object defining the animation that will be performed when the message is dismissed.
     */
    private Animation setupDismissAnimation() {
        final MessageSettings.MessageAnimation animation = getSettings().getDismissAnimation();

        if (animation == null) {
            MobileCore.log(LoggingMode.VERBOSE, TAG,
                    "No dismiss animation found in the message settings. Message will be removed.");
            return new TranslateAnimation(0, 0, 0, 0);
        }

        MobileCore.log(LoggingMode.VERBOSE, TAG, "Creating dismiss animation for " + animation.name());
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
                dismissAnimation = new TranslateAnimation(0, baseRootViewWidth, 0, baseRootViewHeight);
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
