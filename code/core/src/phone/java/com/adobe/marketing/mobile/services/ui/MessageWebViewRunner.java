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

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.webkit.WebSettings;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.ServiceConstants;
import com.adobe.marketing.mobile.services.ServiceProvider;
import com.adobe.marketing.mobile.services.ui.MessageSettings.MessageAlignment;
import com.adobe.marketing.mobile.services.ui.MessageSettings.MessageAnimation;
import com.adobe.marketing.mobile.util.StringUtils;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@link Runnable} object running on the main thread that creates the webview and adds it to
 * the root view.
 */
class MessageWebViewRunner implements Runnable {

    private static final String TAG = "MessageWebViewRunner";
    private static final String UNEXPECTED_NULL_VALUE = "Unexpected Null Value";
    private static final int ANIMATION_DURATION = 300;
    private static final String BASE_URL = "file:///android_asset/";
    private static final String MIME_TYPE = "text/html";

    protected View backdrop;
    private final AEPMessage message;
    private WebSettings webviewSettings;
    private MessageWebView webView;
    private MessageWebViewClient webViewClient;
    private MessageSettings settings;
    private int messageHeight, messageWidth, originX, originY;
    private Map<String, String> assetMap = Collections.emptyMap();

    /**
     * Constructor.
     *
     * @param message the {@link AEPMessage} which created the {@link MessageWebViewRunner} object.
     */
    MessageWebViewRunner(final AEPMessage message) {
        this.message = message;
        this.settings = message.getSettings();
        // calculate the dimensions of the webview to be displayed
        messageHeight = getPixelValueForHeight(settings.getHeight());
        messageWidth = getPixelValueForWidth(settings.getWidth());
        originX = getOriginX(settings);
        originY = getOriginY(settings);
    }

    /**
     * Validates the message passed in the constructor. If valid, the webview is created and an
     * attempt is made to show the message.
     */
    @SuppressWarnings("SetJavaScriptEnabled")
    @Override
    public void run() {
        try {
            if (StringUtils.isNullOrEmpty(message.getMessageHtml())) {
                Log.warning(
                        ServiceConstants.LOG_TAG,
                        TAG,
                        UNEXPECTED_NULL_VALUE + " (message html), failed to show the message.");
                message.cleanup();
                return;
            }

            if (message.rootViewGroup == null) {
                Log.warning(
                        ServiceConstants.LOG_TAG,
                        TAG,
                        UNEXPECTED_NULL_VALUE + " (root view group), failed to show the message.");
                message.cleanup();
                return;
            }

            int width = message.rootViewGroup.getWidth();
            int height = message.rootViewGroup.getHeight();

            // ensure the rootview has been measured before trying to display the message
            if (width == 0 || height == 0) {
                Log.warning(
                        ServiceConstants.LOG_TAG,
                        TAG,
                        "Failed to show the message, root view group has not been measured.");
                message.cleanup();
                return;
            }

            final Context context =
                    ServiceProvider.getInstance().getAppContextService().getApplicationContext();

            if (context == null) {
                Log.warning(
                        ServiceConstants.LOG_TAG,
                        TAG,
                        "Failed to show the message, the app context is null.");
                message.cleanup();
                return;
            }

            // create the webview and configure the settings
            webView =
                    new MessageWebView(
                            context, settings.getCornerRadius(), 0, 0, messageWidth, messageHeight);
            webView.setVerticalScrollBarEnabled(false);
            webView.setHorizontalScrollBarEnabled(false);
            webView.setBackgroundColor(Color.TRANSPARENT);
            webViewClient = new MessageWebViewClient(message);
            webViewClient.setLocalAssetsMap(assetMap);
            webView.setWebViewClient(webViewClient);

            webviewSettings = webView.getSettings();
            webviewSettings.setJavaScriptEnabled(true);
            webviewSettings.setAllowFileAccess(false);
            webviewSettings.setDomStorageEnabled(true);

            message.webView = webView;

            // setup onTouchListeners for the webview and the rootview.
            // the rootview touch listener is added to handle dismissing messages via tapping
            // outside the IAM.
            final MessageFragment messageFragment = message.getMessageFragment();

            if (messageFragment == null) {
                Log.warning(
                        ServiceConstants.LOG_TAG,
                        TAG,
                        "Failed to show the message, the message fragment is null.");
                message.cleanup();
                return;
            }

            message.rootViewGroup.setOnTouchListener(message.getMessageFragment());
            message.fragmentFrameLayout.setOnTouchListener(message.getMessageFragment());
            webView.setOnTouchListener(message.getMessageFragment());

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
                // Disallow need for a user gesture to play media. Works on API 17 and above.
                final Method method =
                        webviewSettings
                                .getClass()
                                .getMethod("setMediaPlaybackRequiresUserGesture", Boolean.TYPE);
                method.setAccessible(true);
                method.invoke(webviewSettings, false);
            }

            final Context appContext =
                    ServiceProvider.getInstance().getAppContextService().getApplicationContext();
            File cacheDirectory = null;

            if (appContext != null) {
                cacheDirectory = appContext.getCacheDir();
            }

            if (cacheDirectory != null) {
                webviewSettings.setDatabaseEnabled(true);
            }

            webviewSettings.setDefaultTextEncodingName(StandardCharsets.UTF_8.name());
            webView.loadDataWithBaseURL(
                    BASE_URL,
                    message.getMessageHtml(),
                    MIME_TYPE,
                    StandardCharsets.UTF_8.name(),
                    null);

            // if we are re-showing after an orientation change, no need to animate
            final MessageSettings messageSettings = message.getSettings();

            if (!message.isMessageVisible()) {
                final Animation animation = setupDisplayAnimation();

                if (animation == null) {
                    Log.debug(
                            ServiceConstants.LOG_TAG,
                            TAG,
                            UNEXPECTED_NULL_VALUE
                                    + " (MessageAnimation), failed to setup a display animation.");
                    return;
                }

                webView.setAnimation(animation);
            }

            createMessageFrameAndAddMessageToRootView(messageSettings);
            message.viewed();
        } catch (final Exception ex) {
            Log.warning(
                    ServiceConstants.LOG_TAG, TAG, "Failed to show the message " + ex.getMessage());
        }
    }

    /**
     * Create a message display {@link Animation}.
     *
     * @return {@code Animation} object defining the animation that will be performed when the
     *     message is displayed.
     */
    private Animation setupDisplayAnimation() {
        final MessageAnimation animation = message.getSettings().getDisplayAnimation();

        if (animation == null) {
            return null;
        }

        Log.trace(
                ServiceConstants.LOG_TAG,
                TAG,
                "Creating display animation for " + animation.name());
        final Animation displayAnimation;

        switch (animation) {
            case TOP:
                displayAnimation = new TranslateAnimation(0, 0, -message.baseRootViewHeight, 0);
                break;
            case FADE:
                // fade in from 0% to 100%
                displayAnimation = new AlphaAnimation(0, 1);
                displayAnimation.setInterpolator(new DecelerateInterpolator());
                break;
            case LEFT:
                displayAnimation = new TranslateAnimation(-message.baseRootViewWidth, 0, 0, 0);
                break;
            case RIGHT:
                displayAnimation = new TranslateAnimation(message.baseRootViewWidth, 0, 0, 0);
                break;
            case BOTTOM:
                displayAnimation =
                        new TranslateAnimation(
                                0, 0, message.baseRootViewHeight * 2, message.webView.getTop());
                break;
            case CENTER:
                displayAnimation =
                        new TranslateAnimation(
                                message.baseRootViewWidth, 0, message.baseRootViewHeight, 0);
                break;
            default:
                // no animation
                displayAnimation = new TranslateAnimation(0, 0, 0, 0);
                break;
        }

        if (animation.equals(MessageAnimation.FADE)) {
            displayAnimation.setDuration(ANIMATION_DURATION * 2);
        } else {
            displayAnimation.setDuration(ANIMATION_DURATION);
        }

        displayAnimation.setFillAfter(true);

        return displayAnimation;
    }

    /**
     * Creates a {@link MessageWebView} and a {@link LinearLayout} backdrop and adds them to the
     * root view.
     *
     * @param settings The {@link MessageSettings} object containing customization settings for the
     *     {@link AEPMessage}.
     */
    private void createMessageFrameAndAddMessageToRootView(final MessageSettings settings) {
        FrameLayout.LayoutParams params =
                generateLayoutParams(messageHeight, messageWidth, originX, originY);

        // if we have non fullscreen messages, fill the webview
        final int fullScreenMessageHeight = 100;
        if (settings.getHeight() != fullScreenMessageHeight) {
            webviewSettings.setLoadWithOverviewMode(true);
            webviewSettings.setUseWideViewPort(true);
        }

        // create a new view to apply a background dimming effect behind a displayed message
        backdrop =
                new View(
                        ServiceProvider.getInstance()
                                .getAppContextService()
                                .getApplicationContext());
        backdrop.setLayoutParams(
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        backdrop.setBackgroundColor(Color.parseColor(settings.getBackdropColor()));
        backdrop.setAlpha(settings.getBackdropOpacity());

        // add the webview overlaid on the backdrop
        message.rootViewGroup.addView(
                backdrop, message.baseRootViewWidth, message.baseRootViewHeight);
        message.rootViewGroup.addView(webView, params);
    }

    /**
     * Generates {@link FrameLayout.LayoutParams} for the {@link MessageWebView} given the provided
     * message height, width, origin x, and origin y.
     *
     * @param messageHeight a {@code int} specifying the height of the webview.
     * @param messageWidth a {@code int} specifying the width of the webview.
     * @param originX a {@code int} specifying the x origin of the webview.
     * @param originY a {@code int} specifying the y origin of the webview.
     * @return the generated {@code FrameLayout.LayoutParams} object.
     */
    private FrameLayout.LayoutParams generateLayoutParams(
            final int messageHeight, final int messageWidth, final int originX, final int originY) {
        final FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(messageWidth, messageHeight, Gravity.NO_GRAVITY);
        params.topMargin = originY;
        params.leftMargin = originX;

        return params;
    }

    /**
     * Converts the percentage into pixels based on the total screen height in pixels.
     *
     * @param percentage A {@code float} percentage to be converted to pixels
     * @return a {@code int} containing the percentage converted to pixels
     */
    @SuppressWarnings("checkstyle:MagicNumber")
    private int getPixelValueForHeight(final float percentage) {
        return (int) (message.baseRootViewHeight * (percentage / 100));
    }

    /**
     * Converts the percentage into pixels based on the total screen width in pixels.
     *
     * @param percentage A {@code float} percentage to be converted to pixels
     * @return a {@code int} containing the percentage converted to pixels
     */
    @SuppressWarnings("checkstyle:MagicNumber")
    private int getPixelValueForWidth(final float percentage) {
        return (int) (message.baseRootViewWidth * (percentage / 100));
    }

    /**
     * Calculates the left most point of the {@link MessageWebView}.
     *
     * <p>The x origin is calculated by the settings values of horizontal alignment and horizontal
     * inset. If the horizontal alignment is center, horizontal inset is ignored and x is calculated
     * so that the message will be centered according to its width. If horizontal alignment is left
     * or right, the inset will be calculated as a percentage width from the respective alignment
     * origin
     *
     * @param settings The {@link MessageSettings} object containing customization settings for the
     *     {@link AEPMessage}.
     * @return a {@code int} containing the left most point of the {@code MessageWebView}
     */
    private int getOriginX(final MessageSettings settings) {
        // default to 0 for x origin if unspecified
        if (settings == null) {
            return 0;
        }

        final int screenWidth = message.baseRootViewWidth;

        if (settings.getHorizontalAlign().equals(MessageAlignment.LEFT)) {
            // check for an inset, otherwise left alignment means return 0
            if (settings.getHorizontalInset() != 0) {
                // since x alignment starts at 0 on the left, this value just needs to be
                // the percentage value translated to actual pixels
                return getPixelValueForWidth(settings.getHorizontalInset());
            } else {
                return 0;
            }
        } else if (settings.getHorizontalAlign().equals(MessageAlignment.RIGHT)) {
            // check for an inset
            if (settings.getHorizontalInset() != 0) {
                // x alignment here is screen width - message width - inset value converted from
                // percentage to pixels
                return (screenWidth
                        - getPixelValueForWidth(settings.getWidth())
                        - getPixelValueForWidth(settings.getHorizontalInset()));
            } else {
                // no inset, right x alignment means screen width - message width
                return screenWidth - getPixelValueForWidth(settings.getWidth());
            }
        }

        // handle center alignment, x is (screen width - message width) / 2
        return (screenWidth - getPixelValueForWidth(settings.getWidth())) / 2;
    }

    /**
     * Calculates the top most point of the {@link MessageWebView}.
     *
     * <p>The y origin is calculated by the settings values of vertical alignment and vertical
     * inset. If vertical alignment is center, vertical inset is ignored and y is calculated so that
     * the message will be centered according to its height. If vertical alignment is top or bottom,
     * the inset will be calculated as a percentage height from the respective alignment origin.
     *
     * @param settings The {@link MessageSettings} object containing customization settings for the
     *     {@link AEPMessage}.
     * @return a {@code int} containing the top most point of the {@code MessageWebView}
     */
    private int getOriginY(final MessageSettings settings) {
        // default to 0 for y origin if unspecified
        if (settings == null) {
            return 0;
        }

        final int screenHeight = message.baseRootViewHeight;

        if (settings.getVerticalAlign().equals(MessageAlignment.TOP)) {
            // check for an inset, otherwise top alignment means return 0
            if (settings.getVerticalInset() != 0) {
                // since y alignment starts at 0 on the top, this value just needs to be
                // the percentage value translated to actual pixels
                return getPixelValueForHeight(settings.getVerticalInset());
            } else {
                return 0;
            }
        } else if (settings.getVerticalAlign().equals(MessageAlignment.BOTTOM)) {
            // check for an inset
            if (settings.getVerticalInset() != 0) {
                // y alignment here is screen height - message height - inset value converted from
                // percentage to pixels
                return (screenHeight
                        - getPixelValueForHeight(settings.getHeight())
                        - getPixelValueForHeight(settings.getVerticalInset()));
            } else {
                // no inset, bottom y alignment means screen height - message height
                return screenHeight - getPixelValueForHeight(settings.getHeight());
            }
        }

        // handle center alignment, y is (screen height - message height) / 2
        return (screenHeight - getPixelValueForHeight(settings.getHeight())) / 2;
    }

    /**
     * Sets the asset map which contains the mapping between a remote image asset url and it's
     * cached location.
     *
     * @param assetMap The {@code Map<String, String} object containing the mapping between a remote
     *     asset url and its cached location.
     */
    void setLocalAssetsMap(final Map<String, String> assetMap) {
        if (assetMap != null && !assetMap.isEmpty()) {
            this.assetMap = new HashMap<>(assetMap);
        }
    }
}
