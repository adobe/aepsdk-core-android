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


import android.util.Base64;

/**
 * The Android implementation for for {@link EncodingService}.
 */
class AndroidEncodingService implements EncodingService {

	private static final String LOG_TAG = AndroidEncodingService.class.getSimpleName();

	@Override
	public byte[] base64Decode(final String input) {
		if (input == null) {
			return null;
		}

		try {
			return Base64.decode(input, Base64.DEFAULT);
		} catch (IllegalArgumentException ex) {
			Log.debug(LOG_TAG, "Unable to Base64 decode string (%s).", input);
			return null;
		}
	}

	@Override
	public byte[] base64Encode(final byte[] input) {
		if (input == null) {
			return null;
		}

		try {
			return Base64.encode(input, Base64.NO_WRAP);	// do not generate newline character at the end
		} catch (IllegalArgumentException ex) {
			Log.debug(LOG_TAG, "Unable to Base64 encode string (%s).", input);
			return null;
		}
	}
}
