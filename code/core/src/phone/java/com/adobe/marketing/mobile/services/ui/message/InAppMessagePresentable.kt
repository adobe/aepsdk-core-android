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

package com.adobe.marketing.mobile.services.ui.message

import android.content.Context
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.annotation.VisibleForTesting
import androidx.compose.ui.platform.ComposeView
import com.adobe.marketing.mobile.services.ui.Alert
import com.adobe.marketing.mobile.services.ui.InAppMessage
import com.adobe.marketing.mobile.services.ui.Presentable
import com.adobe.marketing.mobile.services.ui.Presentation
import com.adobe.marketing.mobile.services.ui.PresentationDelegate
import com.adobe.marketing.mobile.services.ui.PresentationListener
import com.adobe.marketing.mobile.services.ui.PresentationUtilityProvider
import com.adobe.marketing.mobile.services.ui.common.AEPPresentable
import com.adobe.marketing.mobile.services.ui.common.AppLifecycleProvider
import com.adobe.marketing.mobile.services.ui.message.views.MessageScreen
import kotlinx.coroutines.CoroutineScope
import java.nio.charset.StandardCharsets

/**
 * Presentable for InAppMessage visuals.
 * @param inAppMessage the in-app message that this presentable will be tied to
 * @param presentationDelegate the presentation delegate to use for lifecycle events
 * @param presentationUtilityProvider the presentation utility provider to use for the presentable
 */
internal class InAppMessagePresentable(
    private val inAppMessage: InAppMessage,
    private val presentationDelegate: PresentationDelegate?,
    private val presentationUtilityProvider: PresentationUtilityProvider,
    appLifecycleProvider: AppLifecycleProvider,
    mainScope: CoroutineScope
) : AEPPresentable<InAppMessage>(
    inAppMessage,
    presentationUtilityProvider,
    presentationDelegate,
    appLifecycleProvider,
    mainScope
) {

    companion object {
        private const val LOG_SOURCE = "InAppMessagePresentable"
        internal const val TEXT_HTML_MIME_TYPE = "text/html"
        internal const val BASE_URL = "file:///android_asset/"
    }

    private val inAppMessageEventHandler: DefaultInAppMessageEventHandler = DefaultInAppMessageEventHandler(
        scriptHandlers = mutableMapOf(),
        mainScope = mainScope
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
                    inAppMessageEventHandler = inAppMessageEventHandler,
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
                        inAppMessage.eventListener.onBackPressed(this@InAppMessagePresentable)
                        dismiss()
                    },
                    onGestureDetected = { gesture ->
                        // The message creation wizard only allows gesture association with message dismissal
                        // So always dismiss the message when a gesture is detected
                        dismiss()

                        // If a gesture mapping exists, the notify the listener about the uri associated with the gesture
                        inAppMessage.settings.gestureMap[gesture]?.let { link ->
                            handleInAppUri(link)
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

    override fun hasConflicts(visiblePresentations: List<Presentation<*>>): Boolean {
        // InAppMessages can be shown if there are no other visible in-app messages or alerts
        return visiblePresentations.any { (it is InAppMessage || it is Alert) }
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
        ) { url -> handleInAppUri(url) }
    }

    /**
     * Handles the in-app uri. Does so by first checking if the component that created this message
     * is able to handle the uri.
     * @param uri the uri to handle
     * @return true if the url was handled internally by the web-view client, false otherwise
     */
    @VisibleForTesting
    internal fun handleInAppUri(uri: String): Boolean {
        // First check if the component that created this message is able to handle the uri.
        // Otherwise check if this URI can be opened by the utility provider via UriOpening service
        val handled = inAppMessage.eventListener.onUrlLoading(this@InAppMessagePresentable, uri) ||
            presentationUtilityProvider.openUri(uri)

        // Notify the presentation delegate only if the url was handled
        if (handled) {
            presentationDelegate?.onContentLoaded(
                this@InAppMessagePresentable,
                PresentationListener.PresentationContent.UrlContent(uri)
            )
        }

        return handled
    }
}
