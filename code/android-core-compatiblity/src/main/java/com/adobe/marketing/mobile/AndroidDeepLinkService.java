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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.adobe.marketing.mobile.services.internal.context.App;

/**
 * The Android implementation for for {@link DeepLinkService}.
 */
class AndroidDeepLinkService implements DeepLinkService {
	private static final String LOG_TAG = AndroidDeepLinkService.class.getSimpleName();

	@Override
	public void triggerDeepLink(final String deepLink) {
		if (deepLink == null || deepLink.isEmpty()) {
			Log.debug(LOG_TAG, "Unable to trigger deep link, link url is empty or null");
			return;
		}

		final Activity currentActivity = App.getCurrentActivity();

		if (currentActivity == null) {
			Log.debug(LOG_TAG, "%s (current activity), unable to trigger deep link", Log.UNEXPECTED_NULL_VALUE);
			return;
		}

		try {
			final Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(deepLink));
			currentActivity.startActivity(intent);
		} catch (Exception ex) {
			Log.warning(LOG_TAG, "Could not load deep link intent for Acquisition (%s)", ex.toString());
		}
	}
}
