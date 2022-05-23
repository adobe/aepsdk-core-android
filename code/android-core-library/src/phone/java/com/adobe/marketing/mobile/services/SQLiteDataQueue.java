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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;

import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.internal.utility.SQLiteDatabaseHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SQLite backed implementation of {@link DataQueue}.
 */
final class SQLiteDataQueue implements DataQueue {

    private static final String TABLE_NAME = "TB_AEP_DATA_ENTITY";
    private static final String TB_KEY_UNIQUE_IDENTIFIER = "uniqueIdentifier";
    private static final String TB_KEY_TIMESTAMP = "timestamp";
    private static final String TB_KEY_DATA = "data";
    private static final String LOG_PREFIX = "SQLiteDataQueue";
    private final String databasePath;
    private boolean isClose = false;
    private final Object dbMutex = new Object();

    SQLiteDataQueue(final File cacheDir, final String databaseName) {
        this.databasePath = new File(cacheDir, removeRelativePath(databaseName)).getPath();
        createTableIfNotExists();
    }

    @Override
    public boolean add(final DataEntity dataEntity) {
        if (isClose) {
            MobileCore.log(LoggingMode.DEBUG, LOG_PREFIX, "add - Returning false, DataQueue is closed.");
            return false;
        }

        if (dataEntity == null) {
            MobileCore.log(LoggingMode.DEBUG, LOG_PREFIX, "add - Returning false, DataEntity is null.");
            return false;
        }

        synchronized (dbMutex) {
            return SQLiteDatabaseHelper.process(databasePath, SQLiteDatabaseHelper.DatabaseOpenMode.READ_WRITE,
                    (connected, database) -> {
                        if (connected && database != null) {
                            SQLiteStatement insertStatement = database.compileStatement(
                                    "INSERT INTO " + TABLE_NAME + " (uniqueIdentifier, timestamp, data) VALUES (?, ?, ?)");
                            insertStatement.bindString(1, dataEntity.getUniqueIdentifier());
                            insertStatement.bindLong(2, dataEntity.getTimestamp().getTime());
                            insertStatement.bindString(3, dataEntity.getData() != null ? dataEntity.getData() : "");
                            long rowId = insertStatement.executeInsert();
                            return rowId >= 0;
                        } else {
                            return false;
                        }
                    });
        }
    }

    @Override
    public List<DataEntity> peek(final int n) {
        if (isClose) {
            MobileCore.log(LoggingMode.DEBUG, LOG_PREFIX, "peek n - Returning null, DataQueue is closed.");
            return null;
        }

        if (n <= 0) {
            MobileCore.log(LoggingMode.DEBUG, LOG_PREFIX, "peek n - Returning null, n <= 0.");
            return null;
        }

        final List<ContentValues> rows = new ArrayList<>();

        synchronized (dbMutex) {
            SQLiteDatabaseHelper.process(databasePath, SQLiteDatabaseHelper.DatabaseOpenMode.READ_ONLY,
                    (connected, database) -> {
                        if (connected && database != null) {
                            Cursor cursor = null;

                            try {
                                cursor = database.query(TABLE_NAME, new String[]{TB_KEY_TIMESTAMP, TB_KEY_UNIQUE_IDENTIFIER, TB_KEY_DATA},
                                        null, null, null, null, "id ASC", String.valueOf(n));

                                if (cursor.moveToFirst()) {
                                    do {
                                        ContentValues contentValues = new ContentValues();
                                        DatabaseUtils.cursorRowToContentValues(cursor, contentValues);
                                        rows.add(contentValues);
                                    } while (cursor.moveToNext());
                                }

                                MobileCore.log(LoggingMode.DEBUG, LOG_PREFIX, String.format("query - Successfully read %d rows from table(%s)",
                                        rows.size(), TABLE_NAME));
                                return true;
                            } catch (final SQLiteException e) {
                                MobileCore.log(LoggingMode.DEBUG, LOG_PREFIX,
                                        String.format("query - Error in querying database table (%s). Error: (%s)", TABLE_NAME, e.getMessage()));
                                return false;
                            }
                        } else {
                            return false;
                        }
                    });
        }

        if (rows.isEmpty()) {
            return new ArrayList<>();
        }

        final List<DataEntity> dataEntitiesList = new ArrayList<>(rows.size());

        for (ContentValues row : rows) {
            dataEntitiesList.add(new DataEntity(
                    row.getAsString(TB_KEY_UNIQUE_IDENTIFIER),
                    new Date(row.getAsLong(TB_KEY_TIMESTAMP)),
                    row.getAsString(TB_KEY_DATA)
            ));
        }

        MobileCore.log(LoggingMode.VERBOSE, LOG_PREFIX, String.format("peek n - Successfully returned %d DataEntities",
                dataEntitiesList.size()));
        return dataEntitiesList;
    }

    @Override
    public DataEntity peek() {
        if (isClose) {
            MobileCore.log(LoggingMode.DEBUG, LOG_PREFIX, "peek - Returning null, DataQueue is closed");
            return null;
        }

        final List<DataEntity> dataEntities = peek(1);

        if (dataEntities == null) {
            MobileCore.log(LoggingMode.DEBUG, LOG_PREFIX, "peek - Unable to fetch DataEntity, returning null");
            return null;
        }

        if (dataEntities.isEmpty()) {
            MobileCore.log(LoggingMode.DEBUG, LOG_PREFIX, "peek - 0 DataEntities fetch, returning null");
            return null;
        }

        MobileCore.log(LoggingMode.VERBOSE, LOG_PREFIX, String.format("peek - Successfully returned DataEntity (%s)",
                dataEntities.get(0).toString()));
        return dataEntities.get(0);
    }

    @Override
    public boolean remove(final int n) {
        if (isClose) {
            MobileCore.log(LoggingMode.DEBUG, LOG_PREFIX, "remove n - Returning false, DataQueue is closed");
            return false;
        }

        if (n <= 0) {
            MobileCore.log(LoggingMode.DEBUG, LOG_PREFIX, "remove n - Returning false, n <= 0");
            return false;
        }

        AtomicInteger deletedRowsCount = new AtomicInteger(-1);

        synchronized (dbMutex) {
            return SQLiteDatabaseHelper.process(databasePath, SQLiteDatabaseHelper.DatabaseOpenMode.READ_WRITE,
                    (connected, database) -> {
                        if (!connected && database == null) {
                            return false;
                        }
                        SQLiteStatement statement = null;
                        try {
                            StringBuilder builder = new StringBuilder("DELETE FROM ").append(
                                    TABLE_NAME).append(" WHERE id in (").append("SELECT id from ").append(TABLE_NAME).append(" order by id ASC").append(" limit ").append(n).append(')');
                            statement = database.compileStatement(builder.toString());
                            deletedRowsCount.set(statement.executeUpdateDelete());
                            MobileCore.log(LoggingMode.VERBOSE, LOG_PREFIX, String.format("remove n - Removed %d DataEntities",
                                    deletedRowsCount));
                            return deletedRowsCount.get() > -1;
                        } catch (final SQLiteException e) {
                            MobileCore.log(LoggingMode.WARNING, LOG_PREFIX,
                                    String.format("removeRows - Error in deleting rows from table(%s). Returning 0. Error: (%s)", TABLE_NAME,
                                            e.getMessage()));
                            return false;
                        } finally {
                            if (statement != null) {
                                statement.close();
                            }
                        }
                    });
        }
    }

    @Override
    public boolean remove() {
        if (isClose) {
            MobileCore.log(LoggingMode.DEBUG, LOG_PREFIX, "remove - Returning false, DataQueue is closed");
            return false;
        }

        return remove(1);
    }

    @Override
    public boolean clear() {
        if (isClose) {
            MobileCore.log(LoggingMode.DEBUG, LOG_PREFIX, "clear - Returning false, DataQueue is closed");
            return false;
        }

        synchronized (dbMutex) {
            boolean result = SQLiteDatabaseHelper.clearTable(databasePath, TABLE_NAME);
            MobileCore.log(LoggingMode.VERBOSE, LOG_PREFIX, String.format("clear - %s in clearing Table %s",
                    (result ? "Successful" : "Failed"), TABLE_NAME));
            return result;
        }
    }

    @Override
    public int count() {
        if (isClose) {
            MobileCore.log(LoggingMode.DEBUG, LOG_PREFIX, "count - Returning 0, DataQueue is closed");
            return 0;
        }

        synchronized (dbMutex) {
            return SQLiteDatabaseHelper.getTableSize(databasePath, TABLE_NAME);
        }
    }

    @Override
    public void close() {
        isClose = true;
    }

    /**
     * Creates a Table with name {@link #TABLE_NAME}, if not already exists in database at path {@link #databasePath}.
     */
    private void createTableIfNotExists() {
        final String tableCreationQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                " (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, " +
                "uniqueIdentifier TEXT NOT NULL UNIQUE, " +
                "timestamp INTEGER NOT NULL, " +
                "data TEXT);";

        synchronized (dbMutex) {
            if (SQLiteDatabaseHelper.createTableIfNotExist(databasePath, tableCreationQuery)) {
                MobileCore.log(LoggingMode.VERBOSE, LOG_PREFIX,
                        String.format("createTableIfNotExists - Successfully created/already existed table (%s) ", TABLE_NAME));
                return;
            }
        }

        MobileCore.log(LoggingMode.DEBUG, LOG_PREFIX,
                String.format("createTableIfNotExists - Error creating/accessing table (%s)  ", TABLE_NAME));
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
    private String removeRelativePath(final String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return filePath;
        }

        try {
            String result = filePath.replaceAll("\\.[/\\\\]", "\\.");
            result = result.replaceAll("[/\\\\](\\.{2,})", "_");
            return result;
        } catch (IllegalArgumentException e) {
            return filePath;
        }
    }
}
