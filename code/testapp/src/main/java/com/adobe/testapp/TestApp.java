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

package com.adobe.testapp;

import com.adobe.marketing.mobile.Extension;
import com.adobe.marketing.mobile.Identity;
import com.adobe.marketing.mobile.Lifecycle;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.Signal;

import android.app.Application;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TestApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		MobileCore.setApplication(this);
		MobileCore.setLogLevel(com.adobe.marketing.mobile.LoggingMode.VERBOSE);
		// MobileCore.configureWithAppID("YOUR_APP_ID")

		List<Class<? extends Extension>> extensions = Arrays.asList(
				Identity.EXTENSION,
				Signal.EXTENSION,
				Lifecycle.EXTENSION
		);

		MobileCore.registerExtensions(extensions, value -> {
				MobileCore.collectPii(new HashMap<String, String>() {
					{
						put("triggerKey", "collectPIIIOS");
						put("cusFirstName", "aa");
						put("cusLastName", "bb");
						put("cusEmail", "aa.bb@gmail.com");
					}
				});
		});

	}
}

