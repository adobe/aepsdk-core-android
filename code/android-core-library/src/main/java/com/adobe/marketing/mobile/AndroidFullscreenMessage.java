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

package com.adobe.marketing.mobile;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.webkit.MimeTypeMap;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.adobe.marketing.mobile.services.ui.internal.MessagesMonitor;
import com.adobe.marketing.mobile.internal.context.App;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The Android implementation for {@link UIService.UIFullScreenMessage}. It creates and starts a {@link FullscreenMessageActivity}
 * and adds a {@link WebView} to the activity as the container of the the fullscreen message.
 */
class AndroidFullscreenMessage implements UIService.UIFullScreenMessage {

	private static final String TAG = AndroidFullscreenMessage.class.getSimpleName();
	private static final String BASE_URL = "file:///android_asset/";
	private static final String MIME_TYPE = "text/html";
	private static final int ANIMATION_DURATION = 300;
	//Map contains the URL mapping of remote resources to local resources(in cache).
	private Map<String, String> assetMap = Collections.emptyMap();


	Activity messageFullScreenActivity;
	ViewGroup rootViewGroup;
	private final String html;
	private final UIService.UIFullScreenListener fullscreenListener;
	private int orientationWhenShown;
	private WebView webView;
	private boolean isVisible;
	private MessageFullScreenWebViewClient webViewClient;
	private MessagesMonitor messagesMonitor;

	/**
	 * Constructor
	 *
	 * @param html the html {@link String} payload
	 * @param fullscreenListener {@link UIService.UIFullScreenListener} for the message events
	 * @param messagesMonitor {@link MessagesMonitor} instance that tracks and provides the displayed status for a message
	 */
	AndroidFullscreenMessage(final String html, final UIService.UIFullScreenListener fullscreenListener,
							 final MessagesMonitor messagesMonitor) {
		this.messagesMonitor = messagesMonitor;
		this.html = html;
		this.fullscreenListener = fullscreenListener;
	}

	/**
	 * Creates {@code Intent} and starts the {@link FullscreenMessageActivity}.
	 * <p>
	 *
	 * The {@code FullscreenMessageActivity} will not be shown if {@link MessagesMonitor#isDisplayed()} is true.
	 */
	@Override
	public void show() {

		if (messagesMonitor != null && messagesMonitor.isDisplayed()) {
			Log.debug(TAG, "Full screen message couldn't be displayed, another message is displayed at this time");
			return;
		}

		final Activity currentActivity =  App.getInstance().getCurrentActivity();

		if (currentActivity == null) {
			Log.debug(TAG, "%s (current activity), failed to show the fullscreen message.", Log.UNEXPECTED_NULL_VALUE);
			return;
		}

		try {
			final Intent fullscreen = new Intent(currentActivity.getApplicationContext(), FullscreenMessageActivity.class);
			fullscreen.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			FullscreenMessageActivity.setFullscreenMessage(this);
			currentActivity.startActivity(fullscreen);
			currentActivity.overridePendingTransition(0, 0);

			if (messagesMonitor != null) {
				messagesMonitor.displayed();
			}
		} catch (ActivityNotFoundException ex) {
			Log.error(TAG, "Failed to show the fullscreen message, could not start the activity.");
		}
	}

	/**
	 * Opens the url as a {@link Intent#ACTION_VIEW} intent.
	 *
	 * @param url the url to open
	 */
	@Override
	public void openUrl(final String url) {
		try {
			final Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(url));

			if (messageFullScreenActivity != null) {
				messageFullScreenActivity.startActivity(intent);
			}
		} catch (Exception ex) {
			Log.warning(TAG, "Could not open the url from the fullscreen message (%s)", ex.toString());
		}
	}

	/**
	 * Dismisses the message.
	 */
	@Override
	public void remove() {
		removeFromRootViewGroup();
		FullscreenMessageActivity.setFullscreenMessage(null);

		if (messagesMonitor != null) {
			messagesMonitor.dismissed();
		}

		isVisible = false;
	}

	@Override
	public void setLocalAssetsMap(final Map<String, String> assetMap) {
		if (assetMap != null && !assetMap.isEmpty()) {
			this.assetMap = new HashMap<String, String>(assetMap);
		}
	}

	/**
	 * Creates and adds the {@link #webView} to the root view group of {@link #messageFullScreenActivity}.
	 */
	void showInRootViewGroup() {
		final int currentOrientation = App.getInstance().getCurrentOrientation();

		if (isVisible && orientationWhenShown == currentOrientation) {
			return;
		}

		orientationWhenShown = currentOrientation;
		// run on main thread
		Handler mainHandler = new Handler(Looper.getMainLooper());
		mainHandler.post(new MessageFullScreenRunner(this));
	}

	/**
	 * Removes the {@link #webView} from the activity.
	 */
	private void removeFromRootViewGroup() {
		if (rootViewGroup == null) {
			Log.debug(TAG, "%s (root view group), failed to dismiss the fullscreen message.", Log.UNEXPECTED_NULL_VALUE);
			return;
		}

		// make the animation and remove the view
		final Animation animation = new TranslateAnimation(0, 0, 0, rootViewGroup.getMeasuredHeight());
		animation.setDuration(ANIMATION_DURATION);
		animation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationEnd(Animation animation) {
				AndroidFullscreenMessage.this.messageFullScreenActivity.finish();
				AndroidFullscreenMessage.this.messageFullScreenActivity.overridePendingTransition(0, 0);
			}
			@Override
			public void onAnimationStart(Animation animation) {
			}
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
		});
		webView.setAnimation(animation);
		rootViewGroup.removeView(webView);
	}


	/**
	 * Gets called when the back button is pressed.
	 */
	void dismissed() {
		isVisible = false;

		if (fullscreenListener != null) {
			fullscreenListener.onDismiss(this);
		}

		if (messagesMonitor != null) {
			messagesMonitor.dismissed();
		}
	}

	/**
	 * Gets called after the message is successfully shown.
	 */
	void viewed() {
		isVisible = true;

		if (fullscreenListener != null) {
			fullscreenListener.onShow(this);
		}
	}

	/**
	 * The {@link Runnable} object running on the main thread to create the web view and add it to the activity.
	 */
	private static class MessageFullScreenRunner implements Runnable {
		private final AndroidFullscreenMessage message;

		MessageFullScreenRunner(AndroidFullscreenMessage message) {
			this.message = message;
		}

		@SuppressWarnings("SetJavaScriptEnabled")
		@Override
		public void run() {
			try {
				// create the webview
				message.webView = new WebView(message.messageFullScreenActivity);
				message.webView.setVerticalScrollBarEnabled(false);
				message.webView.setHorizontalScrollBarEnabled(false);
				message.webView.setBackgroundColor(Color.TRANSPARENT);
				message.webViewClient = new MessageFullScreenWebViewClient(message);
				message.webView.setWebViewClient(message.webViewClient);
				final WebSettings settings = message.webView.getSettings();
				settings.setJavaScriptEnabled(true);
				settings.setAllowFileAccess(false);
				settings.setDomStorageEnabled(true);

				if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) { //Works on API 17 and above.
					// Disallow need for a user gesture to play media. Works on API 17 and above.
					Method method = settings.getClass().getMethod("setMediaPlaybackRequiresUserGesture", Boolean.TYPE);
					method.setAccessible(true);
					method.invoke(settings, false);
				}

				Context appContext = App.getInstance().getAppContext();
				File cacheDirectory = null;

				if (appContext != null) {
					cacheDirectory  = appContext.getCacheDir();
				}

				if (cacheDirectory != null) {
					settings.setDatabasePath(cacheDirectory.getPath());
					settings.setDatabaseEnabled(true);
				}

				settings.setDefaultTextEncodingName(StringUtils.CHARSET_UTF_8);
				message.webView.loadDataWithBaseURL(BASE_URL, message.html, MIME_TYPE, StringUtils.CHARSET_UTF_8, null);

				if (message.rootViewGroup == null) {
					Log.debug(TAG, "%s (root view group), failed to show the fullscreen message.", Log.UNEXPECTED_NULL_VALUE);
					message.remove();
					return;
				}

				int width = message.rootViewGroup.getMeasuredWidth();
				int height = message.rootViewGroup.getMeasuredHeight();

				// problem now with trying to show the message when our rootview hasn't been measured yet
				if (width == 0 || height == 0) {
					Log.warning(TAG, "Failed to show the fullscreen message, root view group has not been measured.");
					message.remove();
					return;
				}

				// if we are re-showing after an orientation change, no need to animate
				if (message.isVisible) {
					message.rootViewGroup.addView(message.webView, width, height);
				} else {
					// make a sweet slide up animation and add it to our message view
					final Animation translate = new TranslateAnimation(0, 0, height, 0);
					translate.setDuration(ANIMATION_DURATION);
					message.webView.setAnimation(translate);
					// add the message view to the root
					message.rootViewGroup.addView(message.webView, width, height);
				}

				// update our visible flag
				message.viewed();
			} catch (Exception ex) {
				Log.error(TAG, "Failed to show the full screen message (%s).", ex.toString());
			}
		}
	}

	/**
	 * Implements {@link WebViewClient} to intercept the url href being clicked and determine the action based
	 * on the url.
	 */
	private static class MessageFullScreenWebViewClient extends WebViewClient {
		private final AndroidFullscreenMessage message;

		MessageFullScreenWebViewClient(final AndroidFullscreenMessage message) {
			this.message = message;
		}

		@SuppressWarnings("deprecation")
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
		@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
		public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {

			final WebResourceResponse webResourceResponse = handleWebResourceRequest(request.getUrl().toString());

			if (webResourceResponse != null) {
				return webResourceResponse;
			}

			return super.shouldInterceptRequest(view, request);
		}

		@Nullable
		@Override
		public WebResourceResponse shouldInterceptRequest(WebView view, String url) {

			final WebResourceResponse webResourceResponse = handleWebResourceRequest(url);

			if (webResourceResponse != null) {
				return webResourceResponse;
			}

			return super.shouldInterceptRequest(view, url);
		}

		/**
		 * Returns an instance of @{@link WebResourceResponse} containing input stream from cached remote image.
		 * Returns null in following cases.
		 * 1). If the url is not a http/https URL.
		 * 2). If the cached image is not present for the remote image.
		 * 3). If there is any @{@link Exception} thrown in opening @{@link java.io.InputStream} from cached image.
		 *
		 * @param url, a @{@link String}, URL to remote resource.
		 * @return an instance of @{@link WebResourceResponse}.
		 */
		private WebResourceResponse handleWebResourceRequest(final String url) {

			if (StringUtils.stringIsUrl(url) && message.assetMap.get(url) != null) {
				try {
					final String cachedPath = message.assetMap.get(url);
					final String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(
												url));
					return new WebResourceResponse(mimeType, null, new FileInputStream(cachedPath));
				} catch (IOException e) {
					Log.debug(TAG, "Unable to create WebResourceResponse for remote asset %s and local asset %s", url,
							  message.assetMap.get(url));
				}
			}

			return null;
		}

		private boolean handleUrl(final String url) {
			UIService.UIFullScreenListener fullscreenListener = message.fullscreenListener;
			return fullscreenListener == null || fullscreenListener.overrideUrlLoad(message, url);
		}
	}

}