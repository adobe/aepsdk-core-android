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
	private final Map<String, DataQueue> dataQueueCache;

	DataQueueService() {
		dataQueueCache = new HashMap<>();
	}

	@Override
	public DataQueue getDataQueue(final String databaseName) {
		if(StringUtils.isNullOrEmpty(databaseName)) {
			MobileCore.log(LoggingMode.WARNING,
					LOG_TAG,
					"Failed to create DataQueue, database name is null");
			return null;
		}
		DataQueue dataQueue = dataQueueCache.get(databaseName);

		if (dataQueue == null) {
			synchronized (this) {
				dataQueue = dataQueueCache.get(databaseName);

				if (dataQueue == null) {
					final File databaseDirDataQueue = FileUtil.openOrMigrateDatabase(
							null,
							ServiceProvider.getInstance().getApplicationContext()
					);

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
}
