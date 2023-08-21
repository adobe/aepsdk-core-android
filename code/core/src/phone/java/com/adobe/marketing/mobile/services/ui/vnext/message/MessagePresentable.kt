/*
  Copyright 2023 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.services.ui.vnext.message

import android.content.Context
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.ui.platform.ComposeView
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.ui.vnext.InAppMessage
import com.adobe.marketing.mobile.services.ui.vnext.Presentable
import com.adobe.marketing.mobile.services.ui.vnext.PresentationDelegate
import com.adobe.marketing.mobile.services.ui.vnext.PresentationListener
import com.adobe.marketing.mobile.services.ui.vnext.PresentationUtilityProvider
import com.adobe.marketing.mobile.services.ui.vnext.common.AEPPresentable
import com.adobe.marketing.mobile.services.ui.vnext.common.AppLifecycleProvider
import com.adobe.marketing.mobile.services.ui.vnext.message.views.MessageScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.nio.charset.StandardCharsets

/**
 * Presentable for InAppMessage visuals.
 * @param inAppMessage the in-app message that this presentable will be tied to
 * @param presentationDelegate the presentation delegate to use for lifecycle events
 * @param presentationUtilityProvider the presentation utility provider to use for the presentable
 */
internal class MessagePresentable(
    private val inAppMessage: InAppMessage,
    private val presentationDelegate: PresentationDelegate?,
    private val presentationUtilityProvider: PresentationUtilityProvider,
    appLifecycleProvider: AppLifecycleProvider
) : AEPPresentable<InAppMessage>(
    inAppMessage,
    presentationUtilityProvider,
    presentationDelegate,
    appLifecycleProvider
) {

    companion object {
        private const val LOG_SOURCE = "MessagePresentable"
        internal const val TEXT_HTML_MIME_TYPE = "text/html"
        internal const val BASE_URL = "file:///android_asset/"
    }

    private val inAppMessageEventHandler: DefaultInAppMessageEventHandler = DefaultInAppMessageEventHandler(
        mutableMapOf(),
        CoroutineScope(Dispatchers.Main)
    )

    init {
        // Set the event handler for the in-app message right at creation
        inAppMessage.eventHandler = inAppMessageEventHandler
    }

    private var animationCompleteCallback: (() -> Unit)? = null

    override fun getPresentation(): InAppMessage {
        return inAppMessage
    }

    /**
     * Returns the content of the Message presentable  i.e MessageScreen as a ComposeView.
     * @param activityContext the context of the activity
     */
    override fun getContent(activityContext: Context): ComposeView {
        return ComposeView(activityContext).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            setContent {
                MessageScreen(
                    presentationStateManager = presentationStateManager,
                    inAppMessageSettings = inAppMessage.settings,
                    onCreated = {
                        applyWebViewSettings(it)
                        // Notify the event handler that there is a new webview ready
                        inAppMessageEventHandler.onNewWebView(webView = it)
                    },
                    onDisposed = {
                        if (getState() != Presentable.State.DETACHED) {
                            // If the state is not DETACHED, the the presentable will be reattached
                            // again, so don't cleanup
                            return@MessageScreen
                        }
                        animationCompleteCallback?.invoke()
                        animationCompleteCallback = null
                    },
                    onBackPressed = {
                        inAppMessage.eventListener.onBackPressed(this@MessagePresentable)
                        dismiss()
                    },
                    onGestureDetected = { gesture ->
                        // The message creation wizard only allows gesture association with message dismissal
                        // So always dismiss the message when a gesture is detected
                        dismiss()

                        // If a gesture mapping exists, the notify the listener about the url associated with the gesture
                        inAppMessage.settings.gestureMap[gesture]?.let { link ->
                            handleInAppUrl(link)
                        }
                    }
                )
            }
        }
    }

    override fun gateDisplay(): Boolean {
        // InAppMessages require consulting the presentation delegate to determine if they should be displayed
        return true
    }

    override fun awaitExitAnimation(onAnimationComplete: () -> Unit) {
        animationCompleteCallback = onAnimationComplete
        // now wait for onDisposed to be called on the composable
    }

    /**
     * Applies the default webview configuration to the webview and attaches an internal webview client for
     * handling in-app urls.
     * @param webView the webview to apply the settings to
     * @return the webview with the settings applied
     */
    private fun applyWebViewSettings(webView: WebView): WebView {
        webView.settings.apply {
            // base settings
            javaScriptEnabled = true
            allowFileAccess = false
            domStorageEnabled = true
            layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
            defaultTextEncodingName = StandardCharsets.UTF_8.name()
            mediaPlaybackRequiresUserGesture = false
            databaseEnabled = true
        }

        webView.apply {
            // do not enable if gestures have to be handled
            isVerticalScrollBarEnabled = inAppMessage.settings.gestureMap.isEmpty()
            isHorizontalScrollBarEnabled = inAppMessage.settings.gestureMap.isEmpty()

            isScrollbarFadingEnabled = true
            scrollBarStyle = WebView.SCROLLBARS_INSIDE_OVERLAY
            setBackgroundColor(0)
        }

        webView.webViewClient = createWebViewClient()
        return webView
    }

    private fun createWebViewClient(): InAppMessageWebViewClient {
        return InAppMessageWebViewClient(
            inAppMessage.settings,
            presentationUtilityProvider
        ) { url -> handleInAppUrl(url) }
    }

    /**
     * Handles the in-app url. Does so by first checking if the component that created this message
     * is able to handle the url.
     * @param url the url to handle
     * @return true if the url was handled internally by the web-view client, false otherwise
     */
    private fun handleInAppUrl(url: String): Boolean {
        // Check if the component that created this message is able to handle the url
        val handledByListener =
            inAppMessage.eventListener.onUrlLoading(this@MessagePresentable, url)

        // Check if this URL can be opened by the URLOpening
        val handled = handledByListener || if (InAppMessageWebViewClient.isValidUrl(url)) {
            // TODO: open this url using a proxy for URLOpening.
            ServiceProvider.getInstance().uiService.showUrl(url)
            presentationDelegate?.onContentLoaded(this@MessagePresentable, PresentationListener.PresentationContent.UrlContent(url))
            true
        } else {
            false
        }

        return handled
    }
}
