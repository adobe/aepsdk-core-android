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
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.adobe.marketing.mobile.EventHistoryResultHandler;
import com.adobe.marketing.mobile.internal.CoreConstants;
import com.adobe.marketing.mobile.internal.util.FileUtils;
import com.adobe.marketing.mobile.internal.util.SQLiteDatabaseHelper;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.ServiceProvider;
import java.io.File;

class AndroidEventHistoryDatabase implements EventHistoryDatabase {

    private static final String LOG_TAG = "AndroidEventHistoryDatabase";
    private static final String DATABASE_NAME = "com.adobe.module.core.eventhistory";
    private static final String DATABASE_NAME_1X = "EventHistory";
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
     * @throws {@link EventHistoryDatabaseCreationException} if any error occurred while creating
     *     the database or database table.
     */
    AndroidEventHistoryDatabase() throws EventHistoryDatabaseCreationException {
        databaseFile = openOrMigrateEventHistoryDatabaseFile();
        if (databaseFile == null) {
            throw new EventHistoryDatabaseCreationException(
                    "An error occurred while creating the \"Events\" tablein the Android Event"
                            + " History database, error message: ApplicationContext is null");
        }
        final String tableCreationQuery =
                "CREATE TABLE IF NOT EXISTS "
                        + TABLE_NAME
                        + " (eventHash INTEGER, timestamp INTEGER);";

        synchronized (dbMutex) {
            if (!SQLiteDatabaseHelper.createTableIfNotExist(
                    databaseFile.getPath(), tableCreationQuery)) {
                throw new EventHistoryDatabaseCreationException(
                        "An error occurred while creating the \"Events\" table in the Android"
                                + " Event History database.");
            }
        }
    }

    @SuppressWarnings("checkstyle:NestedIfDepth")
    private File openOrMigrateEventHistoryDatabaseFile() {
        final Context appContext =
                ServiceProvider.getInstance().getAppContextService().getApplicationContext();
        if (appContext == null) {
            Log.debug(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    LOG_TAG,
                    "Failed to create database (%s), the ApplicationContext is null",
                    DATABASE_NAME);
            return null;
        }

        File database = appContext.getDatabasePath(DATABASE_NAME);

        if (database.exists()) {
            return database;
        }

        // If db exists in cache directory, migrate it to new path.
        final File applicationCacheDir =
                ServiceProvider.getInstance().getDeviceInfoService().getApplicationCacheDir();
        if (applicationCacheDir != null) {
            final File cacheDirDatabaseFile = new File(applicationCacheDir, DATABASE_NAME_1X);
            try {
                if (cacheDirDatabaseFile.exists()) {
                    FileUtils.moveFile(cacheDirDatabaseFile, database);
                    Log.debug(
                            CoreConstants.LOG_TAG,
                            LOG_TAG,
                            "Successfully moved database (%s) from cache directory to database"
                                    + " directory",
                            DATABASE_NAME_1X);
                }
            } catch (Exception e) {
                Log.debug(
                        CoreConstants.LOG_TAG,
                        LOG_TAG,
                        "Failed to move database (%s) from cache directory to database directory",
                        DATABASE_NAME_1X);
            }
        }
        return database;
    }

    /**
     * Insert a row into the database. Each row will contain a hash and a timestamp.
     *
     * @param hash {@code long} containing the 32-bit FNV-1a hashed representation of an Event's
     *     data
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
            } catch (final SQLException e) {
                Log.warning(
                        CoreConstants.LOG_TAG,
                        LOG_TAG,
                        "Failed to insert rows into the table (%s)",
                        (e.getLocalizedMessage() != null
                                ? e.getLocalizedMessage()
                                : e.getMessage()));
                return false;
            } finally {
                closeDatabase();
            }
            return result;
        }
    }

    private void openDatabase() {
        database =
                SQLiteDatabaseHelper.openDatabase(
                        databaseFile.getPath(), SQLiteDatabaseHelper.DatabaseOpenMode.READ_WRITE);
    }

    /**
     * Queries the event history database to search for the existence of an event.
     *
     * <p>This method will count all records in the event history database that match the provided
     * hash and are within the bounds of the provided from and to timestamps. If the "from" date is
     * equal to 0, the search will use the beginning of event history as the lower bounds of the
     * date range. If the "to" date is equal to 0, the search will use the current system timestamp
     * as the upper bounds of the date range. The {@link EventHistoryResultHandler} will be called
     * with a {@link Cursor} which contains the number of matching records, the oldest timestamp,
     * and the newest timestamp for a matching event. If no database connection is available, the
     * handler will be called with a null {@code DatabaseService.QueryResult}.
     *
     * @param hash {@code long} containing the 32-bit FNV-1a hashed representation of an Event's
     *     data
     * @param from {@code long} a timestamp representing the lower bounds of the date range to use
     *     when searching for the hash
     * @param to {@code long} a timestamp representing the upper bounds of the date range to use
     *     when searching for the hash
     * @return a {@code DatabaseService.QueryResult} which will contain the matching events
     */
    @Override
    public Cursor select(final long hash, final long from, final long to) {
        // if the provided "to" date is equal to 0, use the current date
        final long toValue = to == 0 ? System.currentTimeMillis() : to;

        synchronized (dbMutex) {
            try {
                openDatabase();
                final String[] whereArgs =
                        new String[] {
                            String.valueOf(hash), String.valueOf(from), String.valueOf(toValue),
                        };
                final Cursor cursor =
                        database.rawQuery(
                                "SELECT "
                                        + COUNT
                                        + "(*) as "
                                        + COUNT
                                        + ", "
                                        + "min("
                                        + COLUMN_TIMESTAMP
                                        + ") as "
                                        + OLDEST
                                        + ", "
                                        + "max("
                                        + COLUMN_TIMESTAMP
                                        + ") as "
                                        + NEWEST
                                        + " FROM "
                                        + TABLE_NAME
                                        + " "
                                        + " WHERE "
                                        + COLUMN_HASH
                                        + " = ?"
                                        + " AND "
                                        + COLUMN_TIMESTAMP
                                        + " >= ?"
                                        + " AND "
                                        + COLUMN_TIMESTAMP
                                        + " <= ?",
                                whereArgs);
                cursor.moveToFirst();

                return cursor;
            } catch (final SQLException e) {
                Log.warning(
                        CoreConstants.LOG_TAG,
                        LOG_TAG,
                        "Failed to execute query (%s)",
                        (e.getLocalizedMessage() != null
                                ? e.getLocalizedMessage()
                                : e.getMessage()));
            } finally {
                closeDatabase();
            }
            return null;
        }
    }

    /**
     * Delete entries from the event history database.
     *
     * @param hash {@code long} containing the 32-bit FNV-1a hashed representation of an Event's
     *     data
     * @param from {@code long} representing the lower bounds of the date range to use when
     *     searching for the hash
     * @param to {@code long} representing the upper bounds of the date range to use when searching
     *     for the hash
     * @return {@code int} containing the number of entries deleted for the given hash.
     */
    @Override
    public int delete(final long hash, final long from, final long to) {
        // if the provided "to" date is equal to 0, use the current date
        final long toValue = to == 0 ? System.currentTimeMillis() : to;

        synchronized (dbMutex) {
            try {
                openDatabase();
                final String[] whereArgs =
                        new String[] {
                            String.valueOf(hash), String.valueOf(from), String.valueOf(toValue),
                        };
                final int affectedRowsCount =
                        database.delete(
                                TABLE_NAME,
                                COLUMN_HASH
                                        + " = ?"
                                        + " AND "
                                        + COLUMN_TIMESTAMP
                                        + " >= ?"
                                        + " AND "
                                        + COLUMN_TIMESTAMP
                                        + " <= ?",
                                whereArgs);
                Log.trace(
                        CoreConstants.LOG_TAG,
                        LOG_TAG,
                        "Count of rows deleted in table %s are %d",
                        TABLE_NAME,
                        affectedRowsCount);

                return affectedRowsCount;
            } catch (final SQLException e) {
                Log.debug(
                        CoreConstants.LOG_TAG,
                        LOG_TAG,
                        "Failed to delete table rows (%s)",
                        (e.getLocalizedMessage() != null
                                ? e.getLocalizedMessage()
                                : e.getMessage()));
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
