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
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.cardview.widget.CardView;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.ServiceConstants;
import com.adobe.marketing.mobile.services.ServiceProvider;
import com.adobe.marketing.mobile.services.ui.MessageSettings.MessageAlignment;
import com.adobe.marketing.mobile.services.ui.MessageSettings.MessageAnimation;
import com.adobe.marketing.mobile.util.MapUtils;
import com.adobe.marketing.mobile.util.StringUtils;
import java.nio.charset.StandardCharsets;

/** Prepares the {@link WebView} prior to the {@link android.app.DialogFragment} being attached. */
class MessageWebViewUtil {

    private static final String TAG = "MessageWebViewRunner";
    private static final String UNEXPECTED_NULL_VALUE = "Unexpected Null Value";
    private static final int FULLSCREEN_PERCENTAGE = 100;
    private static final int ANIMATION_DURATION = 300;
    private static final String BASE_URL = "file:///android_asset/";
    private static final String MIME_TYPE = "text/html";

    private int messageHeight;
    private int messageWidth;
    private int originX;
    private int originY;

    /**
     * Validates the passed in {@code AEPMessage}. If valid, the {@link WebView} is created and an
     * attempt is made to show the message.
     *
     * @param message {@link AEPMessage} containing the in-app message payload
     */
    @SuppressWarnings("SetJavaScriptEnabled")
    void show(final AEPMessage message) {
        try {

            if (message == null) {
                Log.warning(
                        ServiceConstants.LOG_TAG,
                        TAG,
                        UNEXPECTED_NULL_VALUE + " (message), failed to show the message.");
                return;
            }

            if (StringUtils.isNullOrEmpty(message.getMessageHtml())) {
                Log.warning(
                        ServiceConstants.LOG_TAG,
                        TAG,
                        UNEXPECTED_NULL_VALUE + " (message html), failed to show the message.");
                message.cleanup(false);
                return;
            }

            final Context context =
                    ServiceProvider.getInstance().getAppContextService().getApplicationContext();

            if (context == null) {
                Log.warning(
                        ServiceConstants.LOG_TAG,
                        TAG,
                        "Failed to show the message, the app context is null.");
                message.cleanup(false);
                return;
            }

            final MessageFragment messageFragment = message.getMessageFragment();

            if (messageFragment == null) {
                Log.warning(
                        ServiceConstants.LOG_TAG,
                        TAG,
                        "Failed to show the message, the message fragment is null.");
                message.cleanup(false);
                return;
            }

            final MessageSettings settings = message.getMessageSettings();

            // calculate the dimensions of the webview to be displayed
            messageHeight = getPixelValueForHeight(message.parentViewHeight, settings.getHeight());
            messageWidth = getPixelValueForWidth(message.parentViewWidth, settings.getWidth());
            originX = getOriginX(message.parentViewWidth, settings);
            originY = getOriginY(message.parentViewHeight, settings);

            // load the in-app message in the webview
            final WebView webView = message.getWebView();

            webView.loadDataWithBaseURL(
                    BASE_URL,
                    message.getMessageHtml(),
                    MIME_TYPE,
                    StandardCharsets.UTF_8.name(),
                    null);

            // use CardView so rounded corners can be applied
            final CardView webViewFrame = new CardView(context);

            // set a display animation for the webview
            final Animation animation = setupDisplayAnimation(message, webView);
            if (animation == null) {
                Log.debug(
                        TAG,
                        UNEXPECTED_NULL_VALUE,
                        " (MessageAnimation), failed to setup a display animation.");
                return;
            }
            webViewFrame.setAnimation(animation);
            webViewFrame.setBackgroundColor(Color.TRANSPARENT);

            // set touch listener
            webView.setOnTouchListener(message.getMessageFragment());

            // if swipe gestures are provided disable the scrollbars
            if (!MapUtils.isNullOrEmpty(settings.getGestures())) {
                webView.setVerticalScrollBarEnabled(false);
                webView.setHorizontalScrollBarEnabled(false);
            }

            // if we have non fullscreen messages, fill the webview
            final WebSettings webviewSettings = webView.getSettings();
            if (settings.getHeight() != FULLSCREEN_PERCENTAGE) {
                webviewSettings.setLoadWithOverviewMode(true);
                webviewSettings.setUseWideViewPort(true);
            }

            webViewFrame.addView(webView);

            // add the created cardview containing the webview to the message object
            message.setWebViewFrame(webViewFrame);

            setMessageLayoutParameters(message);
        } catch (final Exception ex) {
            Log.warning(
                    ServiceConstants.LOG_TAG, TAG, "Failed to show the message " + ex.getMessage());
        }
    }

    /**
     * Create a message display {@link Animation}.
     *
     * @param message {@link AEPMessage} containing the in-app message payload
     * @param webView {@link WebView} containing the in-app message
     * @return {@code Animation} object defining the animation that will be performed when the
     *     message is displayed.
     */
    private Animation setupDisplayAnimation(final AEPMessage message, final WebView webView) {
        final MessageAnimation animation = message.getMessageSettings().getDisplayAnimation();

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
                displayAnimation = new TranslateAnimation(0, 0, -message.parentViewHeight, 0);
                break;
            case FADE:
                // fade in from 0% to 100%
                displayAnimation = new AlphaAnimation(0, 1);
                displayAnimation.setInterpolator(new DecelerateInterpolator());
                break;
            case LEFT:
                displayAnimation = new TranslateAnimation(-message.parentViewWidth, 0, 0, 0);
                break;
            case RIGHT:
                displayAnimation = new TranslateAnimation(message.parentViewWidth, 0, 0, 0);
                break;
            case BOTTOM:
                displayAnimation =
                        new TranslateAnimation(
                                0, 0, message.parentViewHeight * 2, webView.getTop());
                break;
            case CENTER:
                displayAnimation =
                        new TranslateAnimation(
                                message.parentViewWidth, 0, message.parentViewHeight, 0);
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
     * Set the in-app message layout parameters in the {@code AEPMessage} object.
     *
     * @param message {@link AEPMessage} containing the in-app message payload
     */
    private void setMessageLayoutParameters(final AEPMessage message) {
        ViewGroup.MarginLayoutParams params =
                new ViewGroup.MarginLayoutParams(messageWidth, messageHeight);
        params.topMargin = originY;
        params.leftMargin = originX;
        message.setParams(params);
    }

    /**
     * Converts the percentage into pixels based on the total screen height in pixels.
     *
     * @param parentViewHeight A {@code int} containing the parent view height in pixels
     * @param percentage A {@code float} percentage to be converted to pixels
     * @return a {@code int} containing the percentage converted to pixels
     */
    @SuppressWarnings("checkstyle:MagicNumber")
    private int getPixelValueForHeight(final int parentViewHeight, final float percentage) {
        return (int) (parentViewHeight * (percentage / 100));
    }

    /**
     * Converts the percentage into pixels based on the total screen width in pixels.
     *
     * @param parentViewWidth A {@code int} containing the parent view width in pixels
     * @param percentage A {@code float} percentage to be converted to pixels
     * @return a {@code int} containing the percentage converted to pixels
     */
    @SuppressWarnings("checkstyle:MagicNumber")
    private int getPixelValueForWidth(final int parentViewWidth, final float percentage) {
        return (int) (parentViewWidth * (percentage / 100));
    }

    /**
     * Calculates the left most point of the {@link WebView}.
     *
     * <p>The x origin is calculated by the settings values of horizontal alignment and horizontal
     * inset. If the horizontal alignment is center, horizontal inset is ignored and x is calculated
     * so that the message will be centered according to its width. If horizontal alignment is left
     * or right, the inset will be calculated as a percentage width from the respective alignment
     * origin
     *
     * @param parentViewWidth A {@code int} containing the parent view width in pixels
     * @param settings The {@link MessageSettings} object containing customization settings for the
     *     {@link AEPMessage}.
     * @return a {@code int} containing the left most point of the {@code MessageWebView}
     */
    private int getOriginX(final int parentViewWidth, final MessageSettings settings) {
        // default to 0 for x origin if unspecified
        if (settings == null) {
            return 0;
        }

        if (settings.getHorizontalAlign().equals(MessageAlignment.LEFT)) {
            // check for an inset, otherwise left alignment means return 0
            if (settings.getHorizontalInset() != 0) {
                // since x alignment starts at 0 on the left, this value just needs to be
                // the percentage value translated to actual pixels
                return getPixelValueForWidth(parentViewWidth, settings.getHorizontalInset());
            } else {
                return 0;
            }
        } else if (settings.getHorizontalAlign().equals(MessageAlignment.RIGHT)) {
            // check for an inset
            if (settings.getHorizontalInset() != 0) {
                // x alignment here is screen width - message width - inset value converted from
                // percentage to pixels
                return parentViewWidth
                        - getPixelValueForWidth(parentViewWidth, settings.getWidth())
                        - getPixelValueForWidth(parentViewWidth, settings.getHorizontalInset());
            } else {
                // no inset, right x alignment means screen width - message width
                return parentViewWidth
                        - getPixelValueForWidth(parentViewWidth, settings.getWidth());
            }
        }

        // handle center alignment, x is (screen width - message width) / 2
        return (parentViewWidth - getPixelValueForWidth(parentViewWidth, settings.getWidth())) / 2;
    }

    /**
     * Calculates the top most point of the {@link WebView}.
     *
     * <p>The y origin is calculated by the settings values of vertical alignment and vertical
     * inset. If vertical alignment is center, vertical inset is ignored and y is calculated so that
     * the message will be centered according to its height. If vertical alignment is top or bottom,
     * the inset will be calculated as a percentage height from the respective alignment origin.
     *
     * @param parentViewHeight A {@code int} containing the parent view height in pixels
     * @param settings The {@link MessageSettings} object containing customization settings for the
     *     {@link AEPMessage}.
     * @return a {@code int} containing the top most point of the {@code MessageWebView}
     */
    private int getOriginY(final int parentViewHeight, final MessageSettings settings) {
        // default to 0 for y origin if unspecified
        if (settings == null) {
            return 0;
        }

        if (settings.getVerticalAlign().equals(MessageAlignment.TOP)) {
            // check for an inset, otherwise top alignment means return 0
            if (settings.getVerticalInset() != 0) {
                // since y alignment starts at 0 on the top, this value just needs to be
                // the percentage value translated to actual pixels
                return getPixelValueForHeight(parentViewHeight, settings.getVerticalInset());
            } else {
                return 0;
            }
        } else if (settings.getVerticalAlign().equals(MessageAlignment.BOTTOM)) {
            // check for an inset
            if (settings.getVerticalInset() != 0) {
                // y alignment here is screen height - message height - inset value converted from
                // percentage to pixels
                return parentViewHeight
                        - getPixelValueForHeight(parentViewHeight, settings.getHeight())
                        - getPixelValueForHeight(parentViewHeight, settings.getVerticalInset());
            } else {
                // no inset, bottom y alignment means screen height - message height
                return parentViewHeight
                        - getPixelValueForHeight(parentViewHeight, settings.getHeight());
            }
        }

        // handle center alignment, y is (screen height - message height) / 2
        return (parentViewHeight - getPixelValueForHeight(parentViewHeight, settings.getHeight()))
                / 2;
    }
}
