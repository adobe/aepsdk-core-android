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

package com.adobe.marketing.mobile.internal.utility;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Helper class for performing atomic operation on SQLite Database.
 */
public class SQLiteDatabaseHelper {

    private static final String LOG_PREFIX = "SQLiteDatabaseHelper";

    private SQLiteDatabaseHelper() {
    }

    /**
     * Creates the Table if not already exists in database.
     *
     * @param dbPath the path to Database.
     * @param query  the query for creating table.
     * @return true if successfully created table else false.
     */
    public static boolean createTableIfNotExist(final String dbPath, final String query) {
        SQLiteDatabase database = null;

        try {
            database = openDatabase(dbPath, DatabaseOpenMode.READ_WRITE);
            database.execSQL(query);
            return true;
        } catch (final SQLiteException e) {
            MobileCore.log(LoggingMode.WARNING, LOG_PREFIX,
                    String.format("createTableIfNotExists - Error in creating/accessing table. Error: (%s)", e.getMessage()));
            return false;
        } finally {
            closeDatabase(database);
        }
    }

    /**
     * Inserts a new entity in database.
     *
     * @param dbPath    path to database.
     * @param tableName table name in which new row has to be inserted.
     * @param data      a {@link Map} contains mapping of column and values for new row.
     * @return true if row is successfully inserted else false.
     */
    public static boolean insertRow(final String dbPath, final String tableName, final Map<String, Object> data) {
        SQLiteDatabase database = null;

        try {
            database = openDatabase(dbPath, DatabaseOpenMode.READ_WRITE);
            long rowId = database.insert(tableName, null, getContentValueFromMap(data));
            return rowId != -1;
        } catch (final SQLiteException e) {
            MobileCore.log(LoggingMode.DEBUG, LOG_PREFIX,
                    String.format("insertRow - Error in inserting row into table (%s). Error: (%s)", tableName, e.getMessage()));
            return false;
        } finally {
            closeDatabase(database);
        }
    }

    /**
     * Returns a read only instance of {@link SQLiteDatabase} for reading data from database at path @dbPath.
     *
     * @param dbPath  path to database from where data has to be read.
     * @param columns the names of columns to read.
     * @param count   the number of rows to be read
     * @return {@link List} of {@link ContentValues} where each ContentValue represents a row read from database.
     */
    public static List<ContentValues> query(final String dbPath, final String tableName, final String[] columns, final int count) {
        SQLiteDatabase database = null;
        Cursor cursor = null;

        try {
            database = openDatabase(dbPath, DatabaseOpenMode.READ_ONLY);
            cursor = database.query(tableName, columns,
                    null, null, null, null, "id ASC", String.valueOf(count));
            List<ContentValues> rows = new ArrayList<>(cursor.getCount());

            if (cursor.moveToFirst()) {
                do {
                    ContentValues contentValues = new ContentValues();
                    DatabaseUtils.cursorRowToContentValues(cursor, contentValues);
                    rows.add(contentValues);
                } while (cursor.moveToNext());
            }

            MobileCore.log(LoggingMode.DEBUG, LOG_PREFIX, String.format("query - Successfully read %d rows from table(%s)",
                    rows.size(), tableName));
            return Collections.unmodifiableList(rows);
        } catch (final SQLiteException e) {
            MobileCore.log(LoggingMode.DEBUG, LOG_PREFIX,
                    String.format("query - Error in querying database table (%s). Error: (%s)", tableName, e.getMessage()));
            return Collections.EMPTY_LIST;
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            closeDatabase(database);
        }
    }

    /**
     * Returns the count of rows in table @tableName
     *
     * @param dbPath    path to database
     * @param tableName name of table to calculate size of.
     * @return number of rows in Table @tableName.
     */
    public static int getTableSize(final String dbPath, final String tableName) {
        final String tableSizeQuery = "Select Count (*) from " + tableName;
        SQLiteDatabase database = null;
        Cursor cursor = null;

        try {
            database = openDatabase(dbPath, DatabaseOpenMode.READ_ONLY);
            cursor = database.rawQuery(tableSizeQuery, null);

            if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                return cursor.getInt(0);
            } else {
                MobileCore.log(LoggingMode.DEBUG, LOG_PREFIX,
                        String.format("getTableSize - Error in querying table(%s) size. Returning 0.", tableName));
                return 0;
            }
        } catch (final SQLiteException e) {
            MobileCore.log(LoggingMode.WARNING, LOG_PREFIX,
                    String.format("getTableSize - Error in querying table(%s) size. Returning 0. Error: (%s)", tableName,
                            e.getMessage()));
            return 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            closeDatabase(database);
        }
    }

    /**
     * Deletes the @count rows from Database.
     *
     * @param dbPath    path to database.
     * @param tableName name of table
     * @param orderBy   the order in which rows need to be read. It should be in the format "{columnname} asc/des"
     * @param count     the number of rows need to be deleted.
     * @return number of affected rows.
     */
    public static int removeRows(final String dbPath, final String tableName, final String orderBy, final int count) {
        SQLiteDatabase database = null;
        SQLiteStatement statement = null;

        try {
            database = openDatabase(dbPath, DatabaseOpenMode.READ_WRITE);
            StringBuilder builder = new StringBuilder("DELETE FROM ").append(
                    tableName).append(" WHERE id in (").append("SELECT id from ").append(tableName).append(" order by ").append(
                    orderBy).append(" limit ").append(count).append(')');
            statement = database.compileStatement(builder.toString());
            int deletedRowsCount = statement.executeUpdateDelete();
            return deletedRowsCount;
        } catch (final SQLiteException e) {
            MobileCore.log(LoggingMode.WARNING, LOG_PREFIX,
                    String.format("removeRows - Error in deleting rows from table(%s). Returning 0. Error: (%s)", tableName,
                            e.getMessage()));
            return -1; //-1 indicates error in deleting rows.
        } finally {
            if (statement != null) {
                statement.close();
            }

            closeDatabase(database);
        }
    }

    /**
     * Deletes all the rows in table.
     *
     * @param dbPath    path to database.
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
            MobileCore.log(LoggingMode.WARNING, LOG_PREFIX,
                    String.format("clearTable - Error in clearing table(%s). Returning false. Error: (%s)", tableName,
                            e.getMessage()));
            return false;
        } finally {
            closeDatabase(database);
        }
    }

    /**
     * Opens the database exists at path @filePath. If database doesn't exist than creates the new one.
     *
     * @param filePath   the absolute path to database.
     * @param dbOpenMode an instance of {@link DatabaseOpenMode}
     * @return an instance of {@link SQLiteDatabase} to interact with database.
     * @throws SQLiteException if there is an error in opening database.
     */
    public static SQLiteDatabase openDatabase(final String filePath, final DatabaseOpenMode dbOpenMode) throws SQLiteException {
        if (filePath == null || filePath.isEmpty()) {
            MobileCore.log(LoggingMode.DEBUG, LOG_PREFIX, "openDatabase - Failed to open database - filepath is null or empty");
            throw new SQLiteException("Invalid database path. Database path is null or empty.");
        }

        SQLiteDatabase database = SQLiteDatabase.openDatabase(filePath,
                null,
                SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.CREATE_IF_NECESSARY | dbOpenMode.mode);
        MobileCore.log(LoggingMode.VERBOSE, LOG_PREFIX,
                String.format("openDatabase - Successfully opened the database at path (%s)", filePath));
        return database;
    }

    /**
     * Closes the database.
     *
     * @param database, an instance of {@link SQLiteDatabase}, pointing to database to close.
     */
    public static void closeDatabase(final SQLiteDatabase database) {
        if (database == null) {
            MobileCore.log(LoggingMode.DEBUG, LOG_PREFIX, "closeDatabase - Unable to close database, database passed is null.");
            return;
        }

        database.close();
        MobileCore.log(LoggingMode.VERBOSE, LOG_PREFIX, "closeDatabase - Successfully closed the database.");
    }

    /**
     * Convert values in {@link Map} to @{@link ContentValues}
     *
     * @param values and instance of {@link Map}
     * @return instance of {@link ContentValues}
     */
    private static ContentValues getContentValueFromMap(final Map<String, Object> values) {
        ContentValues contentValues = new ContentValues();

        for (Map.Entry<String, Object> value : values.entrySet()) {
            String columnName = value.getKey();
            Object columnValue = value.getValue();

            if (columnValue == null) {
                contentValues.putNull(columnName);
            } else if (columnValue instanceof String) {
                contentValues.put(columnName, (String) columnValue);
            } else if (columnValue instanceof Long) {
                contentValues.put(columnName, (Long) columnValue);
            } else if (columnValue instanceof Integer) {
                contentValues.put(columnName, (Integer) columnValue);
            } else if (columnValue instanceof Short) {
                contentValues.put(columnName, (Short) columnValue);
            } else if (columnValue instanceof Byte) {
                contentValues.put(columnName, (Byte) columnValue);
            } else if (columnValue instanceof Double) {
                contentValues.put(columnName, (Double) columnValue);
            } else if (columnValue instanceof Float) {
                contentValues.put(columnName, (Float) columnValue);
            } else if (columnValue instanceof Boolean) {
                contentValues.put(columnName, (Boolean) columnValue);
            } else if (columnValue instanceof byte[]) {
                contentValues.put(columnName, (byte[]) columnValue);
            } else {
                MobileCore.log(LoggingMode.WARNING, LOG_PREFIX,
                        String.format("Unsupported data type received for database insertion: columnName (%s) value (%s)", columnName,
                                columnValue));
            }
        }

        return contentValues;
    }

    /**
     * Enum type to pass to function open database. It determined whether to open Database connection in READ only mode or READ WRITE mode.
     */
    public enum DatabaseOpenMode {
        READ_ONLY(SQLiteDatabase.OPEN_READONLY),
        READ_WRITE(SQLiteDatabase.OPEN_READWRITE);

        final int mode;

        DatabaseOpenMode(int mode) {
            this.mode = mode;
        }
    }
}
