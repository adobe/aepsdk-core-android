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

import android.util.Log;

/**
 * The Android implementation for for {@link LoggingService}.
 */
class AndroidLoggingService implements LoggingService {

	private static final String TAG = "AdobeExperienceSDK";

	@Override
	public void trace(String tag, String message) {
		Log.v(TAG, tag + " - " + message);
	}

	@Override
	public void debug(String tag, String message) {
		Log.d(TAG, tag + " - " + message);
	}

	@Override
	public void warning(String tag, String message) {
		Log.w(TAG, tag + " - " + message);
	}

	@Override
	public void error(String tag, String message) {
		Log.e(TAG, tag + " - " + message);
	}

}
