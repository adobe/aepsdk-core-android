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

import android.webkit.MimeTypeMap
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceConstants
import com.adobe.marketing.mobile.services.ui.vnext.PresentationUtilityProvider
import com.adobe.marketing.mobile.util.StringUtils
import com.adobe.marketing.mobile.util.UrlUtils
import java.io.InputStream

class InAppMessageWebViewClient(
    private val messageSettings: InAppMessageSettings,
    private val presentationUtilityProvider: PresentationUtilityProvider,
    private val onUrlLoading: (String) -> Boolean
) : WebViewClient() {

    companion object {
        private const val LOG_TAG = "InAppMessageWebViewClient"
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest): Boolean {
        val uri = request.url
        return handleUrl(uri.toString())
    }

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest
    ): WebResourceResponse? {
        val webResourceResponse: WebResourceResponse? =
            handleWebResourceRequest(request.url.toString())
        return webResourceResponse ?: super.shouldInterceptRequest(view, request)
    }

    /**
     * Passes a URL string to the AEP SDK for processing.
     *
     * @param url a `String` url href intercepted from a button tap that occurred on the
     * webview.
     * @return true if the SDK didn't handle the url or false if the url was handled.
     */
    private fun handleUrl(url: String): Boolean {
        if (StringUtils.isNullOrEmpty(url)) {
            Log.trace(
                ServiceConstants.LOG_TAG,
                LOG_TAG,
                "Unable to handle a null or empty url."
            )
            return true
        }
        return onUrlLoading(url)
    }

    /**
     * Returns an instance of [WebResourceResponse] containing an input stream from a cached
     * remote image. Returns null in the following cases: 1). If the url is not a http/https URL.
     * 2). If the cache location in the asset map is null. 3). If the cached image is not present
     * for the remote image.
     *
     * @param url a `String` URL to a remote resource.
     * @return an instance of `WebResourceResponse`.
     */
    private fun handleWebResourceRequest(url: String?): WebResourceResponse? {
        if (url.isNullOrBlank() || !UrlUtils.isValidUrl(url)) {
            Log.trace(
                ServiceConstants.LOG_TAG,
                LOG_TAG,
                "Cannot handle invalid url $url"
            )
            return null
        }
        val cacheLocation: String? = messageSettings.assetMap[url]
        if (cacheLocation.isNullOrBlank()) {
            Log.trace(
                ServiceConstants.LOG_TAG,
                LOG_TAG,
                "Cannot retrieve asset for null cache location"
            )
            return null
        }

        val cachedContent: InputStream? = presentationUtilityProvider.getCachedContent(cacheLocation, url)
        if (cachedContent == null) {
            Log.trace(
                ServiceConstants.LOG_TAG,
                LOG_TAG,
                "Cached asset not found for $url"
            )
            return null
        }
        val mimeType = MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url))

        return WebResourceResponse(mimeType, null, cachedContent)
    }
}
