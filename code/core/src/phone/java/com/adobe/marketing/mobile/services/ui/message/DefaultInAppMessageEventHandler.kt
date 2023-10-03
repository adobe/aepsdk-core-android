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

import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.annotation.MainThread
import androidx.annotation.VisibleForTesting
import com.adobe.marketing.mobile.AdobeCallback
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import java.io.UnsupportedEncodingException
import java.lang.ref.WeakReference
import java.net.URLDecoder

/**
 * Default implementation of [InAppMessageEventHandler] that handles inbound interactions with the web view
 * associated with an in-app message.
 */
internal class DefaultInAppMessageEventHandler internal constructor(
    private val scriptHandlers: MutableMap<String, WebViewJavascriptInterface>,
    private val mainScope: CoroutineScope
) : InAppMessageEventHandler {
    companion object {
        private const val LOG_SOURCE = "DefaultInAppMessageEventHandler"
    }

    internal var webView: WeakReference<WebView?> = WeakReference(null)
        @VisibleForTesting get
        private set

    override fun handleJavascriptMessage(handlerName: String, callback: AdobeCallback<String>) {
        val javascriptInterface = WebViewJavascriptInterface { js ->
            callback.call(js)
        }

        scriptHandlers[handlerName] = javascriptInterface

        Log.debug(
            ServiceConstants.LOG_TAG,
            LOG_SOURCE,
            "Adding javascript interface for handler: $handlerName"
        )

        mainScope.launch {
            val activeWebView = webView.get()
            if (activeWebView == null) {
                Log.warning(
                    ServiceConstants.LOG_TAG,
                    LOG_SOURCE,
                    "Web view is null. Cannot add javascript interface."
                )
                return@launch
            }
            activeWebView.addJavascriptInterface(javascriptInterface, handlerName)
        }
    }

    override fun evaluateJavascript(jsContent: String, callback: AdobeCallback<String>) {
        if (jsContent.isEmpty()) {
            Log.debug(
                ServiceConstants.LOG_TAG,
                LOG_SOURCE,
                "Javascript content is empty. Cannot evaluate javascript."
            )
            return
        }

        val activeWebView = webView.get()
        if (activeWebView == null) {
            Log.debug(
                ServiceConstants.LOG_TAG,
                LOG_SOURCE,
                "Web view is null. Cannot evaluate javascript."
            )
            return
        }

        val urlDecodedString = try {
            URLDecoder.decode(jsContent, "UTF-8")
        } catch (encodingException: UnsupportedEncodingException) {
            Log.warning(
                ServiceConstants.LOG_TAG,
                LOG_SOURCE,
                "Unsupported encoding exception while decoding javascript content. ${encodingException.message}"
            )
            return
        }

        mainScope.launch {
            activeWebView.evaluateJavascript(urlDecodedString) { result ->
                Log.trace(
                    ServiceConstants.LOG_TAG,
                    LOG_SOURCE,
                    "Invoking callback with result: $result"
                )
                callback.call(result)
            }
        }
    }

    /**
     * Called when the web view associated with the in-app message is reset.
     * This will re-add all the javascript interfaces to the new web view.
     * @param webView the new web view associated with the in-app message
     */
    @MainThread
    internal fun onNewWebView(webView: WebView?) {
        Log.debug(ServiceConstants.LOG_TAG, LOG_SOURCE, "Internal web view was reset.")
        mainScope.coroutineContext.cancelChildren()

        webView?.let {
            this@DefaultInAppMessageEventHandler.webView = WeakReference(it)

            // re-add all the javascript interfaces
            scriptHandlers.forEach { (handlerName, javascriptInterface) ->
                Log.debug(
                    ServiceConstants.LOG_TAG,
                    LOG_SOURCE,
                    "Re-adding javascript interface for handler: $handlerName"
                )
                it.addJavascriptInterface(javascriptInterface, handlerName)
            }
        }
    }

    /**
     * A wrapper class for annotating the [callback] as a javascript interface for adding to the web view.
     */
    internal class WebViewJavascriptInterface(private val callback: (String) -> Unit) {
        @JavascriptInterface
        fun run(js: String) {
            callback(js)
        }
    }
}
