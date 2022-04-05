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

import static com.adobe.marketing.mobile.DatabaseService.Database.ColumnDataType.INTEGER;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.adobe.marketing.mobile.DatabaseService.Database.ColumnConstraint;
import com.adobe.marketing.mobile.DatabaseService.Database.ColumnDataType;
import com.adobe.marketing.mobile.DatabaseService.QueryResult;
import com.adobe.marketing.mobile.internal.eventhub.EventHistoryResultHandler;
import com.adobe.marketing.mobile.services.ServiceProvider;

import java.io.File;
import java.io.IOException;
import java.util.List;

class AndroidEventHistoryDatabase implements EventHistoryDatabase {
	private static final String LOG_TAG = "AndroidEventHistoryDatabase";
	private static final String DATABASE_NAME = "EventHistory";
	private static final String TABLE_NAME = "Events";
	private static final String COLUMN_HASH = "eventHash";
	private static final String COLUMN_TIMESTAMP = "timestamp";
	private static final String COUNT = "count";
	private static final String OLDEST = "oldest";
	private static final String NEWEST = "newest";

	private final Object dbMutex = new Object();
	private SQLiteDatabase database;
	private File databaseFile = null;

	/**
	 * Constructor.
	 *
	 * @throws {@link EventHistoryDatabaseCreationException} if any error occurred while creating the database
	 *                or database table.
	 */
	AndroidEventHistoryDatabase() throws EventHistoryDatabaseCreationException {
		// create the database file in the device cache directory
		if (!openDatabase()) {
			throw new EventHistoryDatabaseCreationException("An error occurred while opening the Android Event History database.");
		}

		// create the "Events" table in the database
		if (!createTable(new String[] {COLUMN_HASH, COLUMN_TIMESTAMP}, new
						 DatabaseService.Database.ColumnDataType[] {INTEGER, INTEGER},
						 null)) {
			throw new EventHistoryDatabaseCreationException("An error occurred while creating the \"Events\" table in the Android Event History database.");
		}
	}

	/**
	 * Opens an {@link EventHistoryDatabase} database file if it exists, otherwise creates a new one in the cache directory.
	 *
	 * @return {@code boolean} indicating whether the table already exists or the create table operation was successful
	 */
	@Override
	public boolean openDatabase() {
		final File applicationCacheDir = ServiceProvider.getInstance().getDeviceInfoService().getApplicationCacheDir();

		if (applicationCacheDir != null) {
			try {
				final String cacheDirCanonicalPath = applicationCacheDir.getCanonicalPath();
				databaseFile = new File(cacheDirCanonicalPath + "/" + DATABASE_NAME);
			} catch (final IOException e) {
				Log.warning(LOG_TAG, "Failed to read %s database file (%s)", DATABASE_NAME,
							(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : e.getMessage()));
				return false;
			}
		}

		synchronized (dbMutex) {
			try {
				database = SQLiteDatabase.openDatabase(databaseFile.getCanonicalPath(),
													   null,
													   SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.CREATE_IF_NECESSARY);
				return true;
			} catch (final IOException e) {
				Log.error(LOG_TAG, "Failed to open %s database (%s)", DATABASE_NAME,
						  (e.getLocalizedMessage() != null ? e.getLocalizedMessage() : e.getMessage()));
				return false;
			}
		}
	}

	/**
	 * Deletes an {@link EventHistoryDatabase} previously created in the cache directory, if it exists.
	 *
	 * @return {@code boolean} indicating whether the database file delete operation was successful
	 */
	@Override
	public boolean deleteDatabase() {
		// close the database before deleting it
		close();

		synchronized (dbMutex) {
			if (databaseFile != null) {
				try {
					return databaseFile.delete();
				} catch (final SecurityException e) {
					Log.error(LOG_TAG, "Failed to delete %s database (%s)", DATABASE_NAME,
							  (e.getLocalizedMessage() != null ? e.getLocalizedMessage() : e.getMessage()));
					return false;
				}
			}

			return false;
		}
	}

	/**
	 * Create a table if it doesn't exist.
	 *
	 * @param columnNames       {@code String[]} array containing column names
	 * @param columnDataTypes   {@link ColumnDataType[]} array containing data types for each column
	 * @param columnConstraints {@link List<ColumnConstraint>} a list of lists containing column constraints
	 *                          for each table column
	 * @return {@code boolean} indicating whether the create table operation was successful
	 * @see ColumnConstraint
	 * @see ColumnDataType
	 */
	@Override
	public boolean createTable(final String[] columnNames, final ColumnDataType[] columnDataTypes,
							   final List<List<ColumnConstraint>> columnConstraints) {
		return createTable(columnNames, columnDataTypes, columnConstraints, false);

	}

	public boolean createTable(final String[] columnNames, final ColumnDataType[] columnDataTypes,
							   final List<List<ColumnConstraint>> columnConstraints, final boolean setColumnsDefault) {
		if (columnNames == null || columnNames.length == 0
				|| columnDataTypes == null || columnDataTypes.length == 0
				|| columnDataTypes.length != columnNames.length || (columnConstraints != null
						&& columnConstraints.size() != columnNames.length)) {
			Log.warning(LOG_TAG, "Failed to create table, one or more input parameters is invalid.");
			return false;
		}

		if (!databaseIsWritable()) {
			Log.warning(LOG_TAG, "Failed to create table, database is not writeable.");
			return false;
		}

		synchronized (dbMutex) {
			final String createTableQuery = QueryStringBuilder.getCreateTableQueryString(TABLE_NAME, columnNames,
											columnDataTypes,
											columnConstraints, setColumnsDefault);
			final SQLiteStatement stmt = database.compileStatement(createTableQuery);

			try {
				stmt.execute();
				stmt.close();
				Log.debug(LOG_TAG, "Table with name %s created.", TABLE_NAME);
				return true;
			} catch (final SQLException e) {
				Log.warning(LOG_TAG, "Failed to create table (%s)",
							(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : e.getMessage()));
				return false;
			} finally {
				stmt.close();
			}
		}
	}

	/**
	 * Insert a row into the database. Each row will contain a hash and a timestamp.
	 *
	 * @param hash    {@code long} containing the 32-bit FNV-1a hashed representation of an Event's data
	 * @return a {@code boolean} which will contain the status of the database insert operation
	 */
	@Override
	public boolean insert(final long hash) {
		if (!databaseIsWritable()) {
			return false;
		}

		synchronized (dbMutex) {
			try {
				final ContentValues contentValues = new ContentValues();
				contentValues.put(COLUMN_HASH, hash);
				contentValues.put(COLUMN_TIMESTAMP, System.currentTimeMillis());
				return database.insert(TABLE_NAME, null, contentValues) != -1;
			} catch (final SQLException e) {
				Log.warning(LOG_TAG, "Failed to insert rows into the table (%s)",
							(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : e.getMessage()));
				return false;
			}
		}
	}

	/**
	 * Queries the event history database to search for the existence of an event.
	 * <p>
	 * This method will count all records in the event history database that match the provided hash and are within
	 * the bounds of the provided from and to timestamps.
	 * If the "from" date is equal to 0, the search will use the beginning of event history as the lower bounds of the date range.
	 * If the "to" date is equal to 0, the search will use the current system timestamp as the upper bounds of the date range.
	 * The {@link EventHistoryResultHandler} will be called with a {@link DatabaseService.QueryResult} which contains the number of matching records,
	 * the oldest timestamp, and the newest timestamp for a matching event.
	 * If no database connection is available, the handler will be called with a null {@code DatabaseService.QueryResult}.
	 *
	 * @param hash    {@code long} containing the 32-bit FNV-1a hashed representation of an Event's data
	 * @param from    {@code long} a timestamp representing the lower bounds of the date range to use when searching for the hash
	 * @param to      {@code long} a timestamp representing the upper bounds of the date range to use when searching for the hash
	 * @return a {@code DatabaseService.QueryResult} which will contain the matching events
	 */
	@Override
	public QueryResult select(final long hash, final long from, final long to) {
		// if the provided "to" date is equal to 0, use the current date
		final long toValue = to == 0 ? System.currentTimeMillis() : to;

		synchronized (dbMutex) {
			try {
				final String[] whereArgs = new String[] {String.valueOf(hash), String.valueOf(from), String.valueOf(toValue)};
				final Cursor cursor = database.rawQuery(
										  "SELECT " + COUNT + "(*) as " + COUNT + ", " +
										  "min(" + COLUMN_TIMESTAMP + ") as " + OLDEST + ", " +
										  "max(" + COLUMN_TIMESTAMP + ") as " + NEWEST
										  + " FROM " + TABLE_NAME + " "
										  + " WHERE " + COLUMN_HASH + " = ?"
										  + " AND " + COLUMN_TIMESTAMP + " >= ?"
										  + " AND " + COLUMN_TIMESTAMP + " <= ?",
										  whereArgs);
				cursor.moveToFirst();

				return new AndroidCursor(cursor);
			} catch (final SQLException e) {
				Log.warning(LOG_TAG, "Failed to execute query (%s)",
							(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : e.getMessage()));
				return null;
			}
		}
	}

	/**
	 * Delete entries from the event history database.
	 *
	 * @param hash {@code long} containing the 32-bit FNV-1a hashed representation of an Event's data
	 * @param from {@code long} representing the lower bounds of the date range to use when searching for the hash
	 * @param to   {@code long} representing the upper bounds of the date range to use when searching for the hash
	 * @return {@code int} containing the number of entries deleted for the given hash.
	 */
	@Override
	public int delete (final long hash, final long from, final long to) {
		if (!databaseIsWritable()) {
			Log.debug("Event history database is not writeable. Delete failed for hash %s", Long.toString(hash));
			return 0;
		}

		// if the provided "to" date is equal to 0, use the current date
		final long toValue = to == 0 ? System.currentTimeMillis() : to;

		synchronized (dbMutex) {
			try {
				final String[] whereArgs = new String[] {String.valueOf(hash), String.valueOf(from), String.valueOf(toValue)};
				final int affectedRowsCount = database.delete(TABLE_NAME,
											  COLUMN_HASH + " = ?"
											  + " AND " + COLUMN_TIMESTAMP + " >= ?"
											  + " AND " + COLUMN_TIMESTAMP + " <= ?",
											  whereArgs);
				Log.trace(LOG_TAG, "Count of rows deleted in table %s are %d", TABLE_NAME, affectedRowsCount);

				return affectedRowsCount;
			} catch (final SQLException e) {
				Log.debug(LOG_TAG, "Failed to delete table rows (%s)",
						  (e.getLocalizedMessage() != null ? e.getLocalizedMessage() : e.getMessage()));
				return 0;
			}
		}
	}

	/**
	 * Close this database.
	 */
	@Override
	public void close() {
		synchronized (dbMutex) {
			database.close();
		}
	}

	private boolean databaseIsWritable() {
		synchronized (dbMutex) {
			if (database == null) {
				Log.debug(LOG_TAG, "%s (Database), unable to write", Log.UNEXPECTED_NULL_VALUE);
				return false;
			}

			if (!database.isOpen()) {
				Log.debug(LOG_TAG, "Unable to write to database, it is not open");
				return false;
			}

			if (database.isReadOnly()) {
				Log.debug(LOG_TAG, "Unable to write to database, it is read-only");
				return false;
			}

			return true;
		}
	}

	// for testing
	SQLiteDatabase getDatabase() {
		return database;
	}
}