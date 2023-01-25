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
import com.adobe.marketing.mobile.internal.util.SQLiteDatabaseHelper;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** SQLite backed implementation of {@link DataQueue}. */
final class SQLiteDataQueue implements DataQueue {

    private static final String TABLE_NAME = "TB_AEP_DATA_ENTITY";
    private static final String TB_KEY_UNIQUE_IDENTIFIER = "uniqueIdentifier";
    private static final String TB_KEY_TIMESTAMP = "timestamp";
    private static final String TB_KEY_DATA = "data";
    private static final String LOG_PREFIX = "SQLiteDataQueue";
    private final String databasePath;
    private boolean isClose = false;
    private final Object dbMutex = new Object();

    SQLiteDataQueue(final String databasePath) {
        this.databasePath = databasePath;
        createTableIfNotExists();
    }

    @Override
    public boolean add(final DataEntity dataEntity) {
        if (dataEntity == null) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    LOG_PREFIX,
                    "add - Returning false, DataEntity is null.");
            return false;
        }

        synchronized (dbMutex) {
            if (isClose) {
                Log.debug(
                        ServiceConstants.LOG_TAG,
                        LOG_PREFIX,
                        "add - Returning false, DataQueue is closed.");
                return false;
            }

            return SQLiteDatabaseHelper.process(
                    databasePath,
                    SQLiteDatabaseHelper.DatabaseOpenMode.READ_WRITE,
                    database -> {
                        if (database == null) {
                            return false;
                        }
                        final int INDEX_UUID = 1;
                        final int INDEX_TIMESTAMP = 2;
                        final int INDEX_DATA = 3;
                        try (SQLiteStatement insertStatement =
                                database.compileStatement(
                                        "INSERT INTO "
                                                + TABLE_NAME
                                                + " (uniqueIdentifier, timestamp, data) VALUES (?,"
                                                + " ?, ?)")) {
                            insertStatement.bindString(
                                    INDEX_UUID, dataEntity.getUniqueIdentifier());
                            insertStatement.bindLong(
                                    INDEX_TIMESTAMP, dataEntity.getTimestamp().getTime());
                            insertStatement.bindString(
                                    INDEX_DATA,
                                    dataEntity.getData() != null ? dataEntity.getData() : "");
                            long rowId = insertStatement.executeInsert();
                            return rowId >= 0;
                        } catch (Exception e) {
                            Log.debug(
                                    ServiceConstants.LOG_TAG,
                                    LOG_PREFIX,
                                    "add - Returning false: " + e.getLocalizedMessage());
                            return false;
                        }
                    });
        }
    }

    @Override
    public List<DataEntity> peek(final int n) {
        if (n <= 0) {
            Log.warning(ServiceConstants.LOG_TAG, LOG_PREFIX, "peek n - Returning null, n <= 0.");
            return null;
        }

        final List<ContentValues> rows = new ArrayList<>();

        synchronized (dbMutex) {
            if (isClose) {
                Log.warning(
                        ServiceConstants.LOG_TAG,
                        LOG_PREFIX,
                        "peek n - Returning null, DataQueue is closed.");
                return null;
            }

            SQLiteDatabaseHelper.process(
                    databasePath,
                    SQLiteDatabaseHelper.DatabaseOpenMode.READ_ONLY,
                    database -> {
                        if (database == null) {
                            return false;
                        }

                        try (Cursor cursor =
                                database.query(
                                        TABLE_NAME,
                                        new String[] {
                                            TB_KEY_TIMESTAMP, TB_KEY_UNIQUE_IDENTIFIER, TB_KEY_DATA
                                        },
                                        null,
                                        null,
                                        null,
                                        null,
                                        "id ASC",
                                        String.valueOf(n))) {
                            if (cursor.moveToFirst()) {
                                do {
                                    ContentValues contentValues = new ContentValues();
                                    DatabaseUtils.cursorRowToContentValues(cursor, contentValues);
                                    rows.add(contentValues);
                                } while (cursor.moveToNext());
                            }

                            Log.trace(
                                    ServiceConstants.LOG_TAG,
                                    LOG_PREFIX,
                                    String.format(
                                            "query - Successfully read %d rows from table(%s)",
                                            rows.size(), TABLE_NAME));
                            return true;
                        } catch (final SQLiteException e) {
                            Log.warning(
                                    ServiceConstants.LOG_TAG,
                                    LOG_PREFIX,
                                    String.format(
                                            "query - Error in querying database table (%s). Error:"
                                                    + " (%s)",
                                            TABLE_NAME, e.getLocalizedMessage()));
                            return false;
                        }
                    });
        }

        if (rows.isEmpty()) {
            return new ArrayList<>();
        }

        final List<DataEntity> dataEntitiesList = new ArrayList<>(rows.size());

        for (ContentValues row : rows) {
            dataEntitiesList.add(
                    new DataEntity(
                            row.getAsString(TB_KEY_UNIQUE_IDENTIFIER),
                            new Date(row.getAsLong(TB_KEY_TIMESTAMP)),
                            row.getAsString(TB_KEY_DATA)));
        }

        Log.trace(
                ServiceConstants.LOG_TAG,
                LOG_PREFIX,
                String.format(
                        "peek n - Successfully returned %d DataEntities", dataEntitiesList.size()));
        return dataEntitiesList;
    }

    @Override
    public DataEntity peek() {
        final List<DataEntity> dataEntities = peek(1);

        if (dataEntities == null) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    LOG_PREFIX,
                    "peek - Unable to fetch DataEntity, returning null");
            return null;
        }

        if (dataEntities.isEmpty()) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    LOG_PREFIX,
                    "peek - 0 DataEntities fetch, returning null");
            return null;
        }

        Log.trace(
                ServiceConstants.LOG_TAG,
                LOG_PREFIX,
                String.format(
                        "peek - Successfully returned DataEntity (%s)",
                        dataEntities.get(0).toString()));
        return dataEntities.get(0);
    }

    @Override
    public boolean remove(final int n) {
        if (n <= 0) {
            Log.debug(ServiceConstants.LOG_TAG, LOG_PREFIX, "remove n - Returning false, n <= 0");
            return false;
        }

        synchronized (dbMutex) {
            if (isClose) {
                Log.warning(
                        ServiceConstants.LOG_TAG,
                        LOG_PREFIX,
                        "remove n - Returning false, DataQueue is closed");
                return false;
            }

            return SQLiteDatabaseHelper.process(
                    databasePath,
                    SQLiteDatabaseHelper.DatabaseOpenMode.READ_WRITE,
                    database -> {
                        int deletedRowsCount = -1;
                        if (database == null) {
                            return false;
                        }
                        String builder =
                                "DELETE FROM "
                                        + TABLE_NAME
                                        + " WHERE id in ("
                                        + "SELECT id from "
                                        + TABLE_NAME
                                        + " order by id ASC"
                                        + " limit "
                                        + n
                                        + ')';
                        try (SQLiteStatement statement = database.compileStatement(builder)) {
                            deletedRowsCount = statement.executeUpdateDelete();
                            Log.trace(
                                    ServiceConstants.LOG_TAG,
                                    LOG_PREFIX,
                                    String.format(
                                            "remove n - Removed %d DataEntities",
                                            deletedRowsCount));
                            return deletedRowsCount > -1;
                        } catch (final SQLiteException e) {
                            Log.warning(
                                    ServiceConstants.LOG_TAG,
                                    LOG_PREFIX,
                                    String.format(
                                            "removeRows - Error in deleting rows from table(%s)."
                                                    + " Returning 0. Error: (%s)",
                                            TABLE_NAME, e.getMessage()));
                            return false;
                        }
                    });
        }
    }

    @Override
    public boolean remove() {
        return remove(1);
    }

    @Override
    public boolean clear() {
        synchronized (dbMutex) {
            if (isClose) {
                Log.warning(
                        ServiceConstants.LOG_TAG,
                        LOG_PREFIX,
                        "clear - Returning false, DataQueue is closed");
                return false;
            }

            boolean result = SQLiteDatabaseHelper.clearTable(databasePath, TABLE_NAME);
            Log.trace(
                    ServiceConstants.LOG_TAG,
                    LOG_PREFIX,
                    String.format(
                            "clear - %s in clearing Table %s",
                            (result ? "Successful" : "Failed"), TABLE_NAME));
            return result;
        }
    }

    @Override
    public int count() {
        synchronized (dbMutex) {
            if (isClose) {
                Log.warning(
                        ServiceConstants.LOG_TAG,
                        LOG_PREFIX,
                        "count - Returning 0, DataQueue is closed");
                return 0;
            }

            return SQLiteDatabaseHelper.getTableSize(databasePath, TABLE_NAME);
        }
    }

    @Override
    public void close() {
        synchronized (dbMutex) {
            isClose = true;
        }
    }

    /**
     * Creates a Table with name {@link #TABLE_NAME}, if not already exists in database at path
     * {@link #databasePath}.
     */
    private void createTableIfNotExists() {
        final String tableCreationQuery =
                "CREATE TABLE IF NOT EXISTS "
                        + TABLE_NAME
                        + " (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, "
                        + "uniqueIdentifier TEXT NOT NULL UNIQUE, "
                        + "timestamp INTEGER NOT NULL, "
                        + "data TEXT);";

        synchronized (dbMutex) {
            if (SQLiteDatabaseHelper.createTableIfNotExist(databasePath, tableCreationQuery)) {
                Log.trace(
                        ServiceConstants.LOG_TAG,
                        LOG_PREFIX,
                        String.format(
                                "createTableIfNotExists - Successfully created/already existed"
                                        + " table (%s) ",
                                TABLE_NAME));
                return;
            }
        }

        Log.warning(
                ServiceConstants.LOG_TAG,
                LOG_PREFIX,
                String.format(
                        "createTableIfNotExists - Error creating/accessing table (%s)  ",
                        TABLE_NAME));
    }
}
