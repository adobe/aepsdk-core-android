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
import android.view.GestureDetector
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.platform.ComposeView
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceConstants
import com.adobe.marketing.mobile.services.ui.vnext.InAppMessage
import com.adobe.marketing.mobile.services.ui.vnext.Presentable
import com.adobe.marketing.mobile.services.ui.vnext.PresentationDelegate
import com.adobe.marketing.mobile.services.ui.vnext.PresentationListener
import com.adobe.marketing.mobile.services.ui.vnext.PresentationUtilityProvider
import com.adobe.marketing.mobile.services.ui.vnext.common.AppLifecycleProvider
import com.adobe.marketing.mobile.services.ui.vnext.common.BasePresentable
import com.adobe.marketing.mobile.services.ui.vnext.message.views.Message

internal class MessagePresentable(
    private val inAppMessage: InAppMessage,
    private val presentationDelegate: PresentationDelegate?,
    private val presentationUtilityProvider: PresentationUtilityProvider,
    appLifecycleProvider: AppLifecycleProvider
) : BasePresentable<InAppMessage>(inAppMessage, presentationUtilityProvider, presentationDelegate, appLifecycleProvider) {

    private var webView: WebView? = null

    override fun getPresentation(): InAppMessage {
        return inAppMessage
    }

    override fun getContent(context: Context): ComposeView {
        return ComposeView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            setContent {
                BackHandler(enabled = getState() == Presentable.State.VISIBLE) {
                    dismiss()
                }

                val visibility = derivedStateOf { visibilityStateManager.presentableState.value == Presentable.State.VISIBLE  }

                Message(isVisible = visibility, inAppMessageSettings = inAppMessage.settings) {
                    webView = it
                    it.webViewClient = createWebViewClient()
                    Log.debug(ServiceConstants.LOG_TAG, "MessagePresentable", "Created Frame")

                    // apply web settings
                    // applyWebSettings(it)
                    // apply web ui settings
                    // applyWebUiSettings(it)
                    // apply listeners
                    // applyListeners(it)

                }
            }
            Log.debug(ServiceConstants.LOG_TAG, "MessagePresentable", "Created compose view")
        }
    }

    override fun gateDisplay(): Boolean {
        return false // change to true
    }

    private fun applyWebSettings(webView: WebView) {
        webView.apply {
            // base settings
            settings.javaScriptEnabled = true
            settings.allowFileAccess = false
            settings.domStorageEnabled = true
            settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
            settings.defaultTextEncodingName = "UTF-8"
            settings.mediaPlaybackRequiresUserGesture = false
            settings.databaseEnabled = true
        }
    }

    private fun applyWebUiSettings(webView: WebView) {
        webView.apply {
            isVerticalScrollBarEnabled = true
            isHorizontalScrollBarEnabled = true // do not enable if gestures have to be handled
            isScrollbarFadingEnabled = true
            scrollBarStyle = WebView.SCROLLBARS_INSIDE_OVERLAY
            setBackgroundColor(0)
        }
    }

    private fun applyListeners(context: Context, webView: WebView, gestureTracker: GestureTracker) {
        webView.apply {
            // listeners to handle gesture events
            val gestureListener = InAppMessageGestureListener(gestureTracker = gestureTracker)
            val gestureDetector = GestureDetector(context, gestureListener)
            setOnTouchListener { v, event ->
                performClick()
                gestureDetector.onTouchEvent(event)
            }
        }
    }

    private fun createWebViewClient(): InAppMessageWebViewClient {
        return InAppMessageWebViewClient(
            inAppMessage.settings,
            presentationUtilityProvider
        ) { url ->
            val handled =
                inAppMessage.inAppMessageEventListener.onUrlLoading(this@MessagePresentable, url)
            if (handled) {
                presentationDelegate?.onContentLoaded(
                    this@MessagePresentable,
                    PresentationListener.PresentationContent.UrlContent(url)
                )
            }
            handled
        }
    }
}
