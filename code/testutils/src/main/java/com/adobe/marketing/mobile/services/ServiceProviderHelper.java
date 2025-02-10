/*
  Copyright 2021 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.services;

import android.content.Context;
import com.adobe.marketing.mobile.util.TestConstants;
import java.io.File;

/**
 * Helper class to give testing access to protected methods of the {@link ServiceProvider} class.
 */
public class ServiceProviderHelper {

	private static final String LOG_SOURCE = "ServiceProviderHelper";

	/**
	 * Reset the {@link ServiceProvider}.
	 * @see ServiceProvider#resetServices()
	 */
	public static void resetServices() {
		ServiceProvider.getInstance().resetServices();
	}

	/**
	 * Attempt to recursively delete all the files under the application cache directory.
	 * @see DeviceInforming#getApplicationCacheDir()
	 */
	public static void cleanCacheDir() {
		File cacheDir = ServiceProvider.getInstance().getDeviceInfoService().getApplicationCacheDir();
		deleteFiles(cacheDir);
	}

	/**
	 * Attempt to recursively delete all the files under the application database directory.
	 * Requires the application context to be set in the {@link ServiceProvider}.
	 * @see AppContextService#getApplicationContext()
	 */
	public static void cleanDatabaseDir() {
		final String databaseName = TestConstants.EXTENSION_NAME;
		Context appContext = ServiceProvider.getInstance().getAppContextService().getApplicationContext();
		if (appContext == null) {
			Log.debug(
				TestConstants.LOG_TAG,
				LOG_SOURCE,
				"Failed to clean database directory for (%s), the ApplicationContext is null",
				databaseName
			);
			return;
		}

		final File databaseDirDataQueue = appContext.getDatabasePath(databaseName);
		if (databaseDirDataQueue != null) {
			deleteFiles(databaseDirDataQueue.getParentFile());
		}
	}

	/**
	 * Recursively delete all files under the given {@code directory}.
	 * @param directory the directory to clean of files
	 */
	private static void deleteFiles(final File directory) {
		if (directory == null) {
			return;
		}

		for (File f : directory.listFiles()) {
			if (f.isDirectory()) {
				deleteFiles(f);
			}

			boolean wasDeleted = f.delete();
			String msg = wasDeleted ? "Successfully deleted cache file/folder " : "Unable to delete cache file/folder ";
			Log.debug(TestConstants.LOG_TAG, LOG_SOURCE, msg + "'" + f.getName() + "'");
		}
	}
}
