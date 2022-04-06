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

package com.adobe.marketing.mobile.internal.eventhub.history;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.internal.utility.SQLiteDatabaseHelper;
import com.adobe.marketing.mobile.services.ServiceProvider;

import java.io.File;
import java.io.IOException;

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
    private File databaseFile = null;
    private SQLiteDatabase database = null;

    /**
     * Constructor.
     *
     * @throws {@link EventHistoryDatabaseCreationException} if any error occurred while creating the database
     *                or database table.
     */
    AndroidEventHistoryDatabase() throws EventHistoryDatabaseCreationException {
        try {
            final File applicationCacheDir = ServiceProvider.getInstance().getDeviceInfoService().getApplicationCacheDir();
            if (applicationCacheDir != null) {
                final String cacheDirCanonicalPath = applicationCacheDir.getCanonicalPath();
                databaseFile = new File(cacheDirCanonicalPath + "/" + DATABASE_NAME);
            }
            final String tableCreationQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                    " (eventHash INTEGER, timestamp INTEGER);";

            synchronized (dbMutex) {
                if (SQLiteDatabaseHelper.createTableIfNotExist(databaseFile.getCanonicalPath(), tableCreationQuery)) {
                    MobileCore.log(LoggingMode.VERBOSE, LOG_TAG,
                            String.format("createTableIfNotExists - Successfully created/already existed table (%s) ", TABLE_NAME));
                } else {
                    throw new EventHistoryDatabaseCreationException("An error occurred while creating the \"Events\" table in the Android Event History database.");
                }
            }
        } catch (final IOException e) {
            throw new EventHistoryDatabaseCreationException(String.format("An error occurred while creating the \"Events\" table in the Android Event History database, error message: %s",
                    (e.getLocalizedMessage() != null ? e.getLocalizedMessage() : e.getMessage())));
        }
    }

    /**
     * Insert a row into the database. Each row will contain a hash and a timestamp.
     *
     * @param hash {@code long} containing the 32-bit FNV-1a hashed representation of an Event's data
     * @return a {@code boolean} which will contain the status of the database insert operation
     */
    @Override
    public boolean insert(final long hash) {
        boolean result;
        synchronized (dbMutex) {
            try {
                openDatabase();
                final ContentValues contentValues = new ContentValues();
                contentValues.put(COLUMN_HASH, hash);
                contentValues.put(COLUMN_TIMESTAMP, System.currentTimeMillis());
                result = database.insert(TABLE_NAME, null, contentValues) != -1;
            } catch (final SQLException | IOException e) {
                MobileCore.log(LoggingMode.WARNING, LOG_TAG,
                        String.format("Failed to insert rows into the table (%s)",
                                (e.getLocalizedMessage() != null ? e.getLocalizedMessage() : e.getMessage())));
                return false;
            } finally {
                closeDatabase();
            }
            return result;
        }
    }

    private void openDatabase() throws IOException {
        database = SQLiteDatabaseHelper.openDatabase(databaseFile.getCanonicalPath(), SQLiteDatabaseHelper.DatabaseOpenMode.READ_WRITE);
    }

    /**
     * Queries the event history database to search for the existence of an event.
     * <p>
     * This method will count all records in the event history database that match the provided hash and are within
     * the bounds of the provided from and to timestamps.
     * If the "from" date is equal to 0, the search will use the beginning of event history as the lower bounds of the date range.
     * If the "to" date is equal to 0, the search will use the current system timestamp as the upper bounds of the date range.
     * The {@link EventHistoryResultHandler} will be called with a {@link Cursor} which contains the number of matching records,
     * the oldest timestamp, and the newest timestamp for a matching event.
     * If no database connection is available, the handler will be called with a null {@code DatabaseService.QueryResult}.
     *
     * @param hash {@code long} containing the 32-bit FNV-1a hashed representation of an Event's data
     * @param from {@code long} a timestamp representing the lower bounds of the date range to use when searching for the hash
     * @param to   {@code long} a timestamp representing the upper bounds of the date range to use when searching for the hash
     * @return a {@code DatabaseService.QueryResult} which will contain the matching events
     */
    @Override
    public Cursor select(final long hash, final long from, final long to) {
        // if the provided "to" date is equal to 0, use the current date
        final long toValue = to == 0 ? System.currentTimeMillis() : to;

        synchronized (dbMutex) {
            try {
                openDatabase();
                final String[] whereArgs = new String[]{String.valueOf(hash), String.valueOf(from), String.valueOf(toValue)};
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

                return cursor;
            } catch (final SQLException | IOException e) {
                MobileCore.log(LoggingMode.WARNING, LOG_TAG,
                        String.format("Failed to execute query (%s)",
                                (e.getLocalizedMessage() != null ? e.getLocalizedMessage() : e.getMessage())));
            } finally {
                closeDatabase();
            }
            return null;
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
    public int delete(final long hash, final long from, final long to) {

        // if the provided "to" date is equal to 0, use the current date
        final long toValue = to == 0 ? System.currentTimeMillis() : to;

        synchronized (dbMutex) {
            try {
                openDatabase();
                final String[] whereArgs = new String[]{String.valueOf(hash), String.valueOf(from), String.valueOf(toValue)};
                final int affectedRowsCount = database.delete(TABLE_NAME,
                        COLUMN_HASH + " = ?"
                                + " AND " + COLUMN_TIMESTAMP + " >= ?"
                                + " AND " + COLUMN_TIMESTAMP + " <= ?",
                        whereArgs);
                MobileCore.log(LoggingMode.VERBOSE, LOG_TAG,
                        String.format("Count of rows deleted in table %s are %d", TABLE_NAME, affectedRowsCount));

                return affectedRowsCount;
            } catch (final SQLException | IOException e) {
                MobileCore.log(LoggingMode.DEBUG, LOG_TAG,
                        String.format("Failed to delete table rows (%s)",
                                (e.getLocalizedMessage() != null ? e.getLocalizedMessage() : e.getMessage())));
            } finally {
                closeDatabase();
            }
            return 0;
        }
    }

    @Override
    public void closeDatabase() {
        SQLiteDatabaseHelper.closeDatabase(database);
    }
}