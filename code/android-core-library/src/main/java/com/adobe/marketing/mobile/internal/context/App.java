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
package com.adobe.marketing.mobile.internal.context;

import android.app.Activity;
import android.content.Context;

public class App {
	private AppContextProvider appContextProvider;

	public interface AppContextProvider {
		Context getAppContext();
		Activity getCurrentActivity();
	}

	private static App INSTANCE = new App();
	private App() {
	}

	public static App getInstance() {
		return  INSTANCE;
	}

	public void initializeApp(AppContextProvider appContextProvider) {
		this.appContextProvider = appContextProvider;
	}

	public Context getAppContext() {
		if (appContextProvider == null) {
			return null;
		}

		return appContextProvider.getAppContext();
	}
	public Activity getCurrentActivity() {
		if (appContextProvider == null) {
			return null;
		}

		return appContextProvider.getCurrentActivity();
	}

	public int getCurrentOrientation() {
		if (appContextProvider == null || appContextProvider.getCurrentActivity() == null) {
			return 0;
		}

		return appContextProvider.getCurrentActivity().getResources().getConfiguration().orientation;
	}

}