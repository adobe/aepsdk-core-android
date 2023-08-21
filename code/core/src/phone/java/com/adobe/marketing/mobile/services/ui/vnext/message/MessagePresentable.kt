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

    private var animationCompleteCallback: (() -> Unit)? = null
    init {
        inAppMessage.eventHandler = inAppMessageEventHandler
    }

    override fun getPresentation(): InAppMessage {
        return inAppMessage
    }

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
        return true
    }

    override fun awaitExitAnimation(onAnimationComplete: () -> Unit) {
        animationCompleteCallback = onAnimationComplete
        // now wait for onDisposed to be called on the composable
    }

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
     */
    private fun handleInAppUrl(url: String): Boolean {
        // Check if the component that created this message is able to handle the url
        val handledByListener =
            inAppMessage.eventListener.onUrlLoading(this@MessagePresentable, url)
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
