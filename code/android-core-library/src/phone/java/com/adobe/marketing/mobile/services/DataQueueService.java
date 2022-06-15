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

package com.adobe.marketing.mobile.services;

import android.content.Context;

import androidx.annotation.NonNull;

import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.internal.utility.StringUtils;
import com.adobe.marketing.mobile.services.utility.FileUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to create instances of {@link DataQueue}.
 * It caches the instances of DataQueue to ensure one instance is created per database.
 */
class DataQueueService implements DataQueuing {


	private static final String LOG_TAG = "DataQueueService";
	private Map<String, DataQueue> dataQueueCache;

	DataQueueService() {
		dataQueueCache = new HashMap<>();
	}

	@Override
	public DataQueue getDataQueue(@NonNull final String databaseName) {
		DataQueue dataQueue = dataQueueCache.get(databaseName);

		if (dataQueue == null) {
			synchronized (this) {
				dataQueue = dataQueueCache.get(databaseName);

				if (dataQueue == null) {
					final File databaseDirDataQueue = openOrMigrateExistingDataQueue(databaseName);

					if(databaseDirDataQueue == null){
						return null;
					}
					dataQueue = new SQLiteDataQueue(databaseDirDataQueue.getPath());
					dataQueueCache.put(databaseName, dataQueue);
				}
			}
		}

		return dataQueue;
	}

	/**
	 * Returns the database if it exists in the path returned by {@link Context#getDatabasePath(String)}
	 * Else copies the existing database from {@link Context#getCacheDir()} to {@code Context#getDatabasePath(String)}
	 * Database is migrated from cache directory because of Android 12 app hibernation changes
	 * which can clear app's cache folder when user doesn't interact with app for few months.
	 *
	 * @param databaseName name of the database to be migrated or opened
	 * @return {@code File} representing the database in {@code Context#getDatabasePath(String)}
	 */
	private File openOrMigrateExistingDataQueue(String databaseName) {
		final String cleanedDatabaseName = FileUtil.removeRelativePath(databaseName);

		if(StringUtils.isNullOrEmpty(databaseName)) {
			MobileCore.log(LoggingMode.WARNING,
					LOG_TAG,
					"Failed to create DataQueue, database name is null");
			return null;
		}

		Context appContext = ServiceProvider.getInstance().getApplicationContext();

		if(appContext == null) {
			MobileCore.log(LoggingMode.WARNING,
					LOG_TAG,
					String.format("Failed to create DataQueue for database (%s), the ApplicationContext is null", databaseName));
			return null;
		}

		final File databaseDirDataQueue = appContext.getDatabasePath(cleanedDatabaseName);

		final File cacheDir = ServiceProvider.getInstance().getDeviceInfoService().getApplicationCacheDir();
		if (!databaseDirDataQueue.exists() && cacheDir != null) {
			final File cacheDirDataQueue = new File(cacheDir, cleanedDatabaseName);
			if (cacheDirDataQueue.exists()) {
				try {
					if(databaseDirDataQueue.createNewFile()) {
						FileUtil.copyFile(cacheDirDataQueue, databaseDirDataQueue);
						MobileCore.log(LoggingMode.DEBUG,
								LOG_TAG,
								String.format("Successfully moved DataQueue for database (%s) from cache directory to database directory", databaseName));
						if(cacheDirDataQueue.delete()) {
							MobileCore.log(LoggingMode.DEBUG,
									LOG_TAG,
									String.format("Successfully delete DataQueue for database (%s) from cache directory", databaseName));
						}
					}
				} catch (Exception e) {
					MobileCore.log(LoggingMode.WARNING,
							LOG_TAG,
							String.format("Failed to move DataQueue for database (%s), could not create new file in database directory", databaseName));
					return null;
				}
			}
		}
		return databaseDirDataQueue;
	}
}
