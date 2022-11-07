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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;

/**
 * The Android {@link Activity} used to display the fullscreen message.
 */
@SuppressLint("Registered")
public class FullscreenMessageActivity extends Activity {
	private static final String TAG = AndroidFullscreenMessage.class.getSimpleName();
	protected static AndroidFullscreenMessage message;

	protected static void setFullscreenMessage(AndroidFullscreenMessage fullscreenMessage) {
		message = fullscreenMessage;
	}

	//TODO: AMSDK-6105 implement onSaveInstanceState as in v4
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// make sure we have a valid message before trying to proceed
		if (message == null) {
			Log.debug(TAG, "%s (message), failed to show the fullscreen message.", Log.UNEXPECTED_NULL_VALUE);
			dismiss();
			return;
		}

		message.messageFullScreenActivity = this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// we don't actually use this layout, but we need it to get on the activity stack
		RelativeLayout relativeLayout = new RelativeLayout(this);
		setContentView(relativeLayout);
	}


	@Override
	public void onResume() {
		super.onResume();

		// make sure we have a valid message before trying to proceed
		if (message == null) {
			Log.debug(TAG, "%s (message), failed to show the fullscreen message.", Log.UNEXPECTED_NULL_VALUE);
			dismiss();
			return;
		}

		// if we can't get root view, can't show the message
		try {
			final ViewGroup rootViewGroup = ((ViewGroup) findViewById(android.R.id.content));

			if (rootViewGroup == null) {
				Log.debug(TAG, "%s (root view group), failed to show the fullscreen message.", Log.UNEXPECTED_NULL_VALUE);
				dismiss();
			} else {
				rootViewGroup.post(new Runnable() {
					@Override
					public void run() {
						message.rootViewGroup = rootViewGroup;
						message.showInRootViewGroup();
					}
				});
			}
		} catch (NullPointerException ex) {
			Log.error(TAG, "Failed to show the fullscreen message (%s).", ex.toString());
			dismiss();
		}
	}

	@Override
	public void onBackPressed() {
		if (message != null) {
			message.dismissed();
		}

		dismiss();
	}

	private void dismiss() {
		finish();
		overridePendingTransition(0, 0);
	}

}