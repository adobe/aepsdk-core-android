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

import java.util.concurrent.atomic.AtomicBoolean;

public class Lifecycle {
	private final static String TAG = Lifecycle.class.getSimpleName();
	private final static String EXTENSION_VERSION = "1.1.1";
	private static final AtomicBoolean extensionIsRegistered = new AtomicBoolean(false);

	private Lifecycle() {

	}

	/**
	 * Returns the version of the {@link Lifecycle} extension
	 *
	 * @return The version as {@code String}
	 */
	public static String extensionVersion() {
		return EXTENSION_VERSION;
	}

	/**
	 * Registers the extension with the Mobile SDK. This method should be called only once in your application class.
	 */
	public static void registerExtension() throws InvalidInitException {
		MobileCore.registerExtension(LifecycleExtension.class, extensionError -> {
			extensionIsRegistered.set(false);
			Log.error(TAG, "There was an error when registering the Lifecycle extension: %s",
					extensionError.getErrorName());
		});
		extensionIsRegistered.set(true);
	}

}




