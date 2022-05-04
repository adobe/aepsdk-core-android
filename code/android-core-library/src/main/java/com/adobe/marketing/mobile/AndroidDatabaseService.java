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
	private final Object dbServiceMutex = new Object();
	private static final String TAG = AndroidDatabaseService.class.getSimpleName();
	private Map<String, Database> map = new HashMap<>();

	/**
	 * Opens a database if it exists, otherwise creates a new one at the specified path.
	 *
	 * @param databasePath {@link String} containing the database file path
	 *
	 * @return {@link Database} instance, or null if error occurs
	 */
	@Override
	public Database openDatabase(final String databasePath) {
		if (StringUtils.isNullOrEmpty(databasePath)) {
			Log.debug(TAG, "Failed to open database - filepath is null or empty");
			return null;
		}

		synchronized (dbServiceMutex) {
			try {
				SQLiteDatabase database = SQLiteDatabase.openDatabase(databasePath,
										  null,
										  SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.CREATE_IF_NECESSARY);

				AndroidDatabase androidDatabase = new AndroidDatabase(database);
				map.put(databasePath, androidDatabase);
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
	 * @param databasePath {@link String} containing the database file path
	 *
	 * @return {@code boolean} indicating whether the database file delete operation was successful
	 */
	@Override
	public boolean deleteDatabase(final String databasePath) {
		if (StringUtils.isNullOrEmpty(databasePath)) {
			Log.debug(TAG, "Failed to delete database - filepath is null or empty");
			return false;
		}

		synchronized (dbServiceMutex) {
			if (map.containsKey(databasePath)) {
				try {
					File databaseFile = new File(databasePath);
					map.remove(databasePath);
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
