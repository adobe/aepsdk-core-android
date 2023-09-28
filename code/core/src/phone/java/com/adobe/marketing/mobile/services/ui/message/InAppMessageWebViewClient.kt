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

import android.net.Uri
import android.webkit.MimeTypeMap
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceConstants
import com.adobe.marketing.mobile.services.ui.PresentationUtilityProvider
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL

/**
 * A [WebViewClient] that handles content loading for an in-app message.
 * @param messageSettings the [InAppMessageSettings] for the current message.
 * @param presentationUtilityProvider the [PresentationUtilityProvider] for accessing cached resources.
 * @param onUrlLoading a callback invoked when a url is intercepted by the webview. The callback returns true if the
 * url was handled by the SDK or false if the url will not be handled.
 */
class InAppMessageWebViewClient(
    private val messageSettings: InAppMessageSettings,
    private val presentationUtilityProvider: PresentationUtilityProvider,
    private val onUrlLoading: (String) -> Boolean
) : WebViewClient() {

    companion object {
        private const val LOG_TAG = "InAppMessageWebViewClient"

        fun isValidUrl(stringUrl: String?): Boolean {
            return if (stringUrl.isNullOrBlank()) {
                false
            } else {
                try {
                    URL(stringUrl)
                    true
                } catch (ex: MalformedURLException) {
                    false
                }
            }
        }
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val uri: Uri? = request?.url
        return handleUrl(uri?.toString())
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
     * @param url a []String] url intercepted from a on the webview.
     * @return false if the SDK didn't handle the url or true if the url was handled.
     */
    private fun handleUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) {
            Log.trace(
                ServiceConstants.LOG_TAG,
                LOG_TAG,
                "Unable to handle a null or empty url."
            )
            // returning true here implies we "handled" the url by preventing the url loading
            return true
        }
        return onUrlLoading(url)
    }

    /**
     * Returns an instance of [WebResourceResponse] containing an input stream from a cached
     * remote resource.
     *
     * @param url a `String` URL to a remote resource.
     * @return an instance of `WebResourceResponse` containing an input stream from a cached
     * remote resource if it exists, otherwise null. Also returns null in the following cases:
     * <li> If the url is not a http/https URL. </li>
     * <li> If the cache location in the asset map is null. </li>
     * <li> If the cached image is not present </li>
     */
    private fun handleWebResourceRequest(url: String?): WebResourceResponse? {
        if (url.isNullOrBlank() || !isValidUrl(url)) {
            Log.trace(
                ServiceConstants.LOG_TAG,
                LOG_TAG,
                "Cannot handle url: $url"
            )
            return null
        }
        val cacheLocation: String? = messageSettings.assetMap[url]
        if (cacheLocation.isNullOrBlank()) {
            Log.trace(
                ServiceConstants.LOG_TAG,
                LOG_TAG,
                "No cache location found for url: $url"
            )
            return null
        }

        val cachedContent: InputStream? =
            presentationUtilityProvider.getCachedContent(cacheLocation, url)
        if (cachedContent == null) {
            Log.trace(
                ServiceConstants.LOG_TAG,
                LOG_TAG,
                "Cached asset not found for url: $url from cache location $cacheLocation."
            )
            return null
        }
        val mimeType = MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url))

        return WebResourceResponse(mimeType, null, cachedContent)
    }
}
