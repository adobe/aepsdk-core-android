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

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.webkit.MimeTypeMap;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.internal.utility.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements {@link WebViewClient} to intercept the url href being clicked and determine the action based
 * on the url.
 */
class MessageWebViewClient extends WebViewClient {
	private static final String TAG = "MessageWebViewClient";
	private final AEPMessage message;
	private Map<String, String> assetMap = Collections.emptyMap();

	/**
	 * Constructor.
	 *
	 * @param message the {@link AEPMessage} that was displayed.
	 */
	public MessageWebViewClient(final AEPMessage message) {
		this.message = message;
	}

	@Override
	public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
		return handleUrl(url);
	}

	@TargetApi(Build.VERSION_CODES.N)
	@Override
	public boolean shouldOverrideUrlLoading(final WebView view, final WebResourceRequest request) {
		final Uri uri = request.getUrl();
		return handleUrl(uri.toString());
	}

	@Nullable
	@Override
	public WebResourceResponse shouldInterceptRequest(final WebView view, final String url) {

		final WebResourceResponse webResourceResponse = handleWebResourceRequest(url);

		if (webResourceResponse != null) {
			return webResourceResponse;
		}

		return super.shouldInterceptRequest(view, url);
	}

	@Nullable
	@Override
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public WebResourceResponse shouldInterceptRequest(final WebView view, final WebResourceRequest request) {

		final WebResourceResponse webResourceResponse = handleWebResourceRequest(request.getUrl().toString());

		if (webResourceResponse != null) {
			return webResourceResponse;
		}

		return super.shouldInterceptRequest(view, request);
	}

	void setLocalAssetsMap(final Map<String, String> assetMap) {
		if (assetMap != null && !assetMap.isEmpty()) {
			this.assetMap = new HashMap<>(assetMap);
		}
	}

	/**
	 * Returns an instance of {@link WebResourceResponse} containing an input stream from cached remote image.
	 * Returns null in the following cases:
	 * 1). If the url is not a http/https URL.
	 * 2). If the cached image is not present for the remote image.
	 * 3). If there is any {@link Exception} thrown in opening @{@link java.io.InputStream} from cached image.
	 *
	 * @param url, a {@code String}, URL to remote resource.
	 * @return an instance of {@code WebResourceResponse}.
	 */
	private WebResourceResponse handleWebResourceRequest(final String url) {
		if (StringUtils.stringIsUrl(url) && assetMap.get(url) != null) {
			try {
				final String cachedPath = assetMap.get(url);
				final String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(
											url));
				return new WebResourceResponse(mimeType, null, new FileInputStream(cachedPath));
			} catch (IOException e) {
				MobileCore.log(LoggingMode.DEBUG, TAG,
							   "Unable to create WebResourceResponse for remote asset " + url + "and local asset " + assetMap.get(url));
			}
		}

		return null;
	}

	/**
	 * Passes a URL string to the AEP SDK for processing.
	 *
	 * @param url a {@code String} url href intercepted from a button tap that occurred on the webview.
	 * @return true if the SDK didn't handle the url or false if the url was handled.
	 */
	private boolean handleUrl(final String url) {
		if (StringUtils.isNullOrEmpty(url)) {
			MobileCore.log(LoggingMode.DEBUG, TAG, "Unable to handle a null or empty url.");
			return true;
		}

		return message.fullScreenMessageDelegate == null || message.fullScreenMessageDelegate.overrideUrlLoad(message, url);
	}
}