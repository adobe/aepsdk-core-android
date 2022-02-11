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

import com.adobe.marketing.mobile.DatabaseService.Database;
import com.adobe.marketing.mobile.DatabaseService.QueryResult;

import java.io.File;

/**
 * Base class that allows implementing subclasses to interface with an underlying {@link Database}.
 */
abstract class AbstractHitsDatabase {
	// ========================================================================================
	// private fields
	// ========================================================================================
	private static final String LOG_TAG = "HitsDatabase";
	private DatabaseService structuredDataService;
	private File databaseFile;

	// ========================================================================================
	// package-private fields
	// ========================================================================================
	String tableName;
	final Object dbMutex = new Object();
	Database database;
	DatabaseStatus databaseStatus;

	// ========================================================================================
	// DatabaseStatus enum
	// ========================================================================================
	enum DatabaseStatus {
		OK(0),
		FATAL_ERROR(1);

		public final int id;

		DatabaseStatus(final int identifier) {
			id = identifier;
		}
	}

	// ========================================================================================
	// Constructor
	// ========================================================================================
	/**
	 * Constructor which sets internal {@link #structuredDataService}, {@link #databaseFile}, and {@link #tableName} properties.
	 *
	 * @param databaseService {@code DatabaseService} instance used for interfacing with a native database
	 * @param databaseFile {@code File} of the underlying database
	 * @param tableName {@code String} containing the name of the table in the database
	 */
	AbstractHitsDatabase(final DatabaseService databaseService, final File databaseFile, final String tableName) {
		this.structuredDataService = databaseService;
		this.databaseFile = databaseFile;
		this.tableName = tableName;
	}

	// ========================================================================================
	// abstract methods
	// ========================================================================================
	/**
	 * Runs after opening or creating a database.
	 */
	abstract void initializeDatabase();

	/**
	 * Optional abstract method.  If overridden, this method is called upon completion of calling the {@link #reset()} method.
	 */
	void postReset() {}

	// ========================================================================================
	// package-private methods
	// ========================================================================================
	/**
	 * Attempts to delete all records from the underlying {@link #database}.
	 */
	void deleteAllHits() {
		synchronized (dbMutex) {
			if (database == null) {
				Log.warning(LOG_TAG, "%s (Database), couldn't delete hits, db file path: %s", Log.UNEXPECTED_NULL_VALUE,
							databaseFile.getAbsolutePath());
				return;
			}

			if (!database.delete(tableName, null, null)) {
				Log.warning(LOG_TAG, "Unable to delete all hits from the database table");
			}

		}
	}

	/**
	 * Attempts to remove the hit with given identifier from database.
	 * <p>
	 * If the database operation fails, this method will reset the {@link #database} and return false.
	 *
	 * @param identifier {@code String} containing the identifier of the hit to be removed
	 * @return {@code boolean} representing the success of the delete operation
	 */
	boolean deleteHitWithIdentifier(final String identifier) {
		if (StringUtils.isNullOrEmpty(identifier)) {
			Log.warning(LOG_TAG, "Unable to delete hit with empty identifier");
			return false;
		}

		synchronized (dbMutex) {
			if (database == null) {
				Log.warning(LOG_TAG, "Couldn't delete hit, %s (Database) - Path to db: %s", Log.UNEXPECTED_NULL_VALUE,
							databaseFile.getAbsolutePath());
				return false;
			}

			if (!database.delete(tableName, "ID = ?", new String[] {identifier})) {
				Log.warning(LOG_TAG, "Unable to delete hit due to unexpected error");
				reset();
				return false;
			}
			return true;
		}
	}

	/**
	 * Opens the existing database at the location represented by {@link #databaseFile}, or creates a new one.
	 * <p>
	 * Logs an error if create or open operation failed.
	 */
	void openOrCreateDatabase() {
		synchronized (dbMutex) {
			closeDatabase();

			if (databaseFile == null) {
				Log.debug(LOG_TAG, "Database creation failed, %s - database file", Log.UNEXPECTED_NULL_VALUE);
				return;
			}

			if (structuredDataService == null) {
				Log.debug(LOG_TAG, "%s (Database service)", Log.UNEXPECTED_NULL_VALUE);
				return;
			}

			Log.trace(LOG_TAG, "Trying to open database file located at %s", databaseFile.getAbsolutePath());
			database = structuredDataService.openDatabase(databaseFile.getPath());

			if (database == null) {
				Log.debug(LOG_TAG, "Database creation failed for %s", databaseFile.getPath());
			} else {
				initializeDatabase();
			}
		}
	}

	// ========================================================================================
	// protected methods
	// ========================================================================================
	/**
	 * Returns the number of hits currently in the {@link #database}.
	 * <p>
	 * If the database query fails, this method logs an error message and returns 0.
	 *
	 * @return {@code long} representing the number of rows in the table
	 */
	protected long getSize() {
		return getSize(new Query.Builder(tableName, new String[] {"ID"}).build());
	}

	/**
	 * Returns the number of hits in the {@link #database} match the query.
	 * <p>
	 * If the database query fails, this method logs an error message and returns 0.
	 *
	 * @param query the {@link Query} object used to query the database
	 * @return {@code long} representing the number of rows match the query in the table
	 */
	protected long getSize(final Query query) {
		synchronized (dbMutex) {
			if (database == null) {
				Log.debug(LOG_TAG, "Couldn't get size, %s (database) - Filepath: %s", Log.UNEXPECTED_NULL_VALUE,
						  databaseFile.getAbsolutePath());
				return 0;
			}

			QueryResult queryResult = null;

			try {
				queryResult = database.query(query);

				if (queryResult == null) {
					Log.debug(LOG_TAG, "%s (query result), unable to get tracking queue size", Log.UNEXPECTED_NULL_VALUE);
					return 0;
				}

				return queryResult.getCount();
			} catch (Exception e) {
				Log.debug(LOG_TAG, "Unable to get the count from the cursor.");
				return 0;
			} finally {
				if (queryResult != null) {
					queryResult.close();
				}
			}
		}
	}

	/**
	 * Resets the underlying {@link #database}, usually as a result of a {@link DatabaseStatus#FATAL_ERROR}.
	 * <p>
	 * This method removes the existing database and creates a new one with the same {@link #databaseFile} and structure.
	 */
	protected final void reset() {
		Log.error(LOG_TAG, "Database in unrecoverable state, resetting.");

		synchronized (dbMutex) {
			if (databaseFile != null && databaseFile.exists() && !structuredDataService.deleteDatabase(databaseFile.getPath())) {
				Log.debug(LOG_TAG, String.format("Failed to delete database file(%s).", databaseFile.getAbsolutePath()));
				databaseStatus = DatabaseStatus.FATAL_ERROR;
				return;
			}
		}

		// Create new database
		openOrCreateDatabase();
		postReset();

	}

	// ========================================================================================
	// private methods
	// ========================================================================================
	/**
	 * Attempts to close the underlying {@link #database}.
	 */
	private void closeDatabase() {
		synchronized (dbMutex) {
			if (database != null) {
				database.close();
			}
		}
	}
}
