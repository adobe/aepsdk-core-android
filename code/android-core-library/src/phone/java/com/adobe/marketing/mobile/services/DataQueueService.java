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

import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.internal.utility.SQLiteDatabaseHelper;

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
	public DataQueue getDataQueue(final String databaseName) {
		DataQueue dataQueue = dataQueueCache.get(databaseName);

		if (dataQueue == null) {
			synchronized (this) {
				dataQueue = dataQueueCache.get(databaseName);

				if (dataQueue == null) {
					final File cacheDir = ServiceProvider.getInstance().getDeviceInfoService().getApplicationCacheDir();

					if (cacheDir == null) {
						MobileCore.log(LoggingMode.WARNING, LOG_TAG,
									   String.format("Failed in creating DataQueue for database (%s), Cache dir is null.", databaseName));
						return null;
					}

					dataQueue = new SQLiteDataQueue(cacheDir, databaseName);
					dataQueueCache.put(databaseName, dataQueue);
				}
			}
		}

		return dataQueue;
	}
}
