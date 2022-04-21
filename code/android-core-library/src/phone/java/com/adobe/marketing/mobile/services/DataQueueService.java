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

import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to create instances of {@link DataQueue}.
 * It caches the instances of DataQueue to ensure one instance is created per database.
 */
class DataQueueService implements DataQueuing {


	private static final String LOG_TAG = "DataQueueService";
	private Map<String, DataQueue> dataQueueCache;
	private final SQLiteDatabaseHelper databaseHelper;

	DataQueueService() {
		dataQueueCache = new HashMap<>();
		databaseHelper = new SQLiteDatabaseHelper();
	}

	@Override
	public DataQueue getDataQueue(final String databaseName) {
		DataQueue dataQueue = dataQueueCache.get(databaseName);

		if (dataQueue == null) {
			synchronized (this) {
				dataQueue = dataQueueCache.get(databaseName);

				if (dataQueue == null) {
					Context appContext = ServiceProvider.getInstance().getApplicationContext();

					if(appContext == null) {
						MobileCore.log(LoggingMode.WARNING,
								LOG_TAG,
								String.format("Failed to create DataQueue for database (%s), the ApplicationContext is null", databaseName));
						return null;
					}

					final String cleanedDatabasePath = removeRelativePath(databaseName);
					final File databaseDirDataQueue = ServiceProvider.getInstance().getApplicationContext().getDatabasePath(cleanedDatabasePath);

					final File cacheDir = ServiceProvider.getInstance().getDeviceInfoService().getApplicationCacheDir();
					if (!databaseDirDataQueue.exists() && cacheDir != null) {
						final File cacheDirDataQueue = new File(cacheDir, cleanedDatabasePath);
						if (cacheDirDataQueue.exists()) {
							try {
								if(databaseDirDataQueue.createNewFile()) {
									copyFile(cacheDirDataQueue, databaseDirDataQueue);
									MobileCore.log(LoggingMode.DEBUG,
											LOG_TAG,
											String.format("Successfully moved DataQueue for database (%s) from cache directory to database directory", databaseName));
								}
							} catch (Exception e) {
								MobileCore.log(LoggingMode.WARNING,
										LOG_TAG,
										String.format("Failed to move DataQueue for database (%s), could not create new file in database directory", databaseName));
								return null;
							}
						}
					}
					dataQueue = new SQLiteDataQueue(databaseDirDataQueue.getPath(), databaseHelper);
					dataQueueCache.put(databaseName, dataQueue);
				}
			}
		}

		return dataQueue;
	}

	/**
	 * Removes the relative part of the file name(if exists).
	 * <p>
	 * for ex: File name `/mydatabase/../../database1` will be converted to `mydatabase_database1`
	 * <p/>
	 *
	 * @param filePath the file name
	 * @return file name without relative path
	 */
	String removeRelativePath(final String filePath) {
		if (filePath == null || filePath.isEmpty()) {
			return filePath;
		}

		try {
			String result = filePath.replaceAll("\\.[/\\\\]", "\\.");
			result = result.replaceAll("[/\\\\](\\.{2,})", "_");
			result = result.replaceAll("/","");
			return result;
		} catch (IllegalArgumentException e) {
			return filePath;
		}
	}

	/**
	 * Copies the contents from {@code src} to {@code dest}.
	 *
	 * @param src {@link File} from which the contents are read
	 * @param dest {@link File} to which contents are written to
	 * @throws IOException if {@code src} or {@code dest} is not present or it does not have read permissions
	 */
	private void copyFile(final File src, final File dest) throws IOException, NullPointerException{
		final int STREAM_READ_BUFFER_SIZE = 1024;

		try (InputStream input = new FileInputStream(src)) {
			try (OutputStream output = new FileOutputStream(dest)) {
				byte[] buffer = new byte[STREAM_READ_BUFFER_SIZE];
				int length;
				while ((length = input.read(buffer)) != -1) {
					output.write(buffer, 0, length);
				}
				MobileCore.log(LoggingMode.DEBUG,
						LOG_TAG,
						String.format("Successfully copied (%s) to (%s)", src.getCanonicalPath(), dest.getCanonicalPath()));
			}
		}
	}
}
