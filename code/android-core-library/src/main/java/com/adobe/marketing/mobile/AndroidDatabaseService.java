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

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * AndroidDatabaseService class implements DatabaseService Interface to manage a SQLite database.
 */
class AndroidDatabaseService implements DatabaseService {
	private final SystemInfoService systemInfoService;
	private final Object dbServiceMutex = new Object();
	private static final String TAG = AndroidDatabaseService.class.getSimpleName();
	private Map<String, Database> map = new HashMap<>();

	AndroidDatabaseService(final SystemInfoService systemInfoService) {
		this.systemInfoService = systemInfoService;

		if (systemInfoService == null) {
			Log.warning(TAG, "Unable to access system info service while creating the database service");
		}
	}

	private String removeRelativePath(final String filePath) {
		try {
			// we don't want to leave any extra "/" or "\" but also can't touch existing slashes
			// first use a regex to find all of the ".\" and "./" and turn them into "."
			// (\\. is escape for ., \\\\ is escape for \\, which means \ in a String)
			// for example: /data/user/0/com.adobe.marketing.mobile.test/cache/mydatabase/../../database1
			// will become /data/user/0/com.adobe.marketing.mobile.test/cache/mydatabase/....database1
			String result = filePath.replaceAll("\\.[/\\\\]", "\\.");
			// now use a regex to find all of the "/.." and "\.." and turn into "_", any "." occurs more than 2 times will be counted
			// for example : /data/user/0/com.adobe.marketing.mobile.test/cache/mydatabase/....database1
			// will become /data/user/0/com.adobe.marketing.mobile.test/cache/mydatabase_database1
			result = result.replaceAll("[/\\\\](\\.{2,})", "_");
			return result;
		} catch (IllegalArgumentException e) {
			return filePath;
		}
	}

	/**
	 * Opens a database if it exists, otherwise creates a new one at the specified path.
	 *
	 * @param filePath {@link String} containing the database file path
	 *
	 * @return {@link Database} instance, or null if error occurs
	 */
	@Override
	public Database openDatabase(final String filePath) {
		if (StringUtils.isNullOrEmpty(filePath)) {
			Log.debug(TAG, "Failed to open database - filepath is null or empty");
			return null;
		}

		final String cleanedPath = removeRelativePath(filePath);

		if (this.systemInfoService != null && this.systemInfoService.getApplicationCacheDir() != null) {
			try {
				final String cacheDirCanonicalPath = this.systemInfoService.getApplicationCacheDir().getCanonicalPath();
				final File file = new File(cleanedPath);
				final String dbFileCanonicalPath = file.getCanonicalPath();

				if (!dbFileCanonicalPath.startsWith(cacheDirCanonicalPath)) {
					Log.warning(TAG, "Invalid database file path (%s)", cleanedPath);
					return null;
				}
			} catch (Exception e) {
				Log.warning(TAG, "Failed to read database file (%s)", e);
				return null;
			}
		}

		synchronized (dbServiceMutex) {
			try {
				SQLiteDatabase database = SQLiteDatabase.openDatabase(cleanedPath,
										  null,
										  SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.CREATE_IF_NECESSARY);

				AndroidDatabase androidDatabase = new AndroidDatabase(database);
				map.put(cleanedPath, androidDatabase);
				return androidDatabase;
			} catch (Exception e) {
				Log.error(TAG, "Failed to open database (%s)", e);
				return null;
			}
		}
	}

	/**
	 * Delete database at the specified path, if it exists.
	 *
	 * @param filePath {@link String} containing the database file path
	 *
	 * @return {@code boolean} indicating whether the database file delete operation was successful
	 */
	@Override
	public boolean deleteDatabase(final String filePath) {
		if (StringUtils.isNullOrEmpty(filePath)) {
			Log.debug(TAG, "Failed to delete database - filepath is null or empty");
			return false;
		}

		String cleanedPath = removeRelativePath(filePath);

		synchronized (dbServiceMutex) {
			if (map.containsKey(cleanedPath)) {
				try {
					File databaseFile = new File(cleanedPath);
					map.remove(cleanedPath);
					return databaseFile.delete();
				} catch (SecurityException e) {
					Log.error(TAG, "Failed to delete database (%s)", e);
					return false;
				}
			} else {
				return false;
			}
		}

	}


}
