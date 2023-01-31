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

package com.adobe.marketing.mobile.internal.util;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import com.adobe.marketing.mobile.internal.CoreConstants;
import com.adobe.marketing.mobile.services.Log;
import java.io.File;

/** Helper class for performing atomic operation on SQLite Database. */
public class SQLiteDatabaseHelper {

    private static final String LOG_PREFIX = "SQLiteDatabaseHelper";

    private SQLiteDatabaseHelper() {}

    /**
     * Creates the Table if not already exists in database.
     *
     * @param dbPath the path to Database.
     * @param query the query for creating table.
     * @return true if successfully created table else false.
     */
    public static boolean createTableIfNotExist(final String dbPath, final String query) {
        SQLiteDatabase database = null;

        try {
            database = openDatabase(dbPath, DatabaseOpenMode.READ_WRITE);
            database.execSQL(query);
            return true;
        } catch (final SQLiteException e) {
            Log.warning(
                    CoreConstants.LOG_TAG,
                    LOG_PREFIX,
                    String.format(
                            "createTableIfNotExists - Error in creating/accessing table. Error:"
                                    + " (%s)",
                            e.getMessage()));
            return false;
        } finally {
            closeDatabase(database);
        }
    }

    /**
     * Returns the count of rows in table @tableName
     *
     * @param dbPath path to database
     * @param tableName name of table to calculate size of.
     * @return number of rows in Table @tableName.
     */
    public static int getTableSize(final String dbPath, final String tableName) {
        SQLiteDatabase database = null;
        Cursor cursor = null;

        try {
            database = openDatabase(dbPath, DatabaseOpenMode.READ_ONLY);
            SQLiteStatement selectStatement =
                    database.compileStatement("Select Count (*) from " + tableName);
            return (int) selectStatement.simpleQueryForLong();
        } catch (final SQLiteException e) {
            Log.warning(
                    CoreConstants.LOG_TAG,
                    LOG_PREFIX,
                    String.format(
                            "getTableSize - Error in querying table(%s) size. Returning 0. Error:"
                                    + " (%s)",
                            tableName, e.getMessage()));
            return 0;
        } finally {
            closeDatabase(database);
        }
    }

    /**
     * Deletes all the rows in table.
     *
     * @param dbPath path to database.
     * @param tableName name of table to empty.
     * @return true if successfully clears the table else returns false.
     */
    public static boolean clearTable(final String dbPath, final String tableName) {
        SQLiteDatabase database = null;

        try {
            database = openDatabase(dbPath, DatabaseOpenMode.READ_WRITE);
            database.delete(tableName, "1", null);
            return true;
        } catch (final SQLiteException e) {
            Log.warning(
                    CoreConstants.LOG_TAG,
                    LOG_PREFIX,
                    String.format(
                            "clearTable - Error in clearing table(%s). Returning false. Error:"
                                    + " (%s)",
                            tableName, e.getMessage()));
            return false;
        } finally {
            closeDatabase(database);
        }
    }

    /**
     * Opens the database exists at path @filePath. If database doesn't exist than creates the new
     * one.
     *
     * @param filePath the absolute path to database.
     * @param dbOpenMode an instance of {@link DatabaseOpenMode}
     * @return an instance of {@link SQLiteDatabase} to interact with database.
     * @throws SQLiteException if there is an error in opening database.
     */
    public static SQLiteDatabase openDatabase(
            final String filePath, final DatabaseOpenMode dbOpenMode) throws SQLiteException {
        if (filePath == null || filePath.isEmpty()) {
            Log.debug(
                    CoreConstants.LOG_TAG,
                    LOG_PREFIX,
                    "openDatabase - Failed to open database - filepath is null or empty");
            throw new SQLiteException("Invalid database path. Database path is null or empty.");
        }

        // Create parent directory if it don't exist.
        try {
            File databasePath = new File(filePath);
            File parentDir = databasePath.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                Log.debug(
                        CoreConstants.LOG_TAG,
                        LOG_PREFIX,
                        "openDatabase - Creating parent directory (%s)",
                        parentDir.getPath());
                parentDir.mkdirs();
            }
        } catch (Exception ex) {
            Log.debug(
                    CoreConstants.LOG_TAG,
                    LOG_PREFIX,
                    "openDatabase - Failed to create parent directory for path (%s)",
                    filePath);
            throw new SQLiteException(
                    "Invalid database path. Unable to create parent directory for database.");
        }

        SQLiteDatabase database =
                SQLiteDatabase.openDatabase(
                        filePath,
                        null,
                        SQLiteDatabase.NO_LOCALIZED_COLLATORS
                                | SQLiteDatabase.CREATE_IF_NECESSARY
                                | dbOpenMode.mode);
        Log.trace(
                CoreConstants.LOG_TAG,
                LOG_PREFIX,
                String.format(
                        "openDatabase - Successfully opened the database at path (%s)", filePath));
        return database;
    }

    /**
     * Closes the database.
     *
     * @param database, an instance of {@link SQLiteDatabase}, pointing to database to close.
     */
    public static void closeDatabase(final SQLiteDatabase database) {
        if (database == null) {
            Log.debug(
                    CoreConstants.LOG_TAG,
                    LOG_PREFIX,
                    "closeDatabase - Unable to close database, database passed is null.");
            return;
        }

        database.close();
        Log.trace(
                CoreConstants.LOG_TAG,
                LOG_PREFIX,
                "closeDatabase - Successfully closed the database.");
    }

    /**
     * Open the database and begin processing database operations in {@link
     * DatabaseProcessing#execute(SQLiteDatabase)}, then disconnecting the database.
     *
     * @param filePath path to database
     * @param dbOpenMode an instance of {@link DatabaseOpenMode}
     * @param databaseProcessing the function interface to include database operations
     * @return the result of the {@link DatabaseProcessing#execute(SQLiteDatabase)}
     */
    public static boolean process(
            final String filePath,
            final DatabaseOpenMode dbOpenMode,
            final DatabaseProcessing databaseProcessing) {
        SQLiteDatabase database = null;
        try {
            database = openDatabase(filePath, dbOpenMode);
            return databaseProcessing.execute(database);
        } catch (Exception e) {
            Log.warning(
                    CoreConstants.LOG_TAG,
                    LOG_PREFIX,
                    "Failed to open database -" + e.getLocalizedMessage());
            return false;
        } finally {
            if (database != null) {
                closeDatabase(database);
            }
        }
    }

    /**
     * Enum type to pass to function open database. It determined whether to open Database
     * connection in READ only mode or READ WRITE mode.
     */
    public enum DatabaseOpenMode {
        READ_ONLY(SQLiteDatabase.OPEN_READONLY),
        READ_WRITE(SQLiteDatabase.OPEN_READWRITE);

        final int mode;

        DatabaseOpenMode(final int mode) {
            this.mode = mode;
        }
    }
}
