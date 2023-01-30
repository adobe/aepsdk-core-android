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

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.adobe.marketing.mobile.services.DataEntity;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SQLiteDatabaseHelperTests {

    private static final String TABLE_NAME = "TB_AEP_DATA_ENTITY";
    private static final String TB_KEY_UNIQUE_IDENTIFIER = "uniqueIdentifier";
    private static final String TB_KEY_TIMESTAMP = "timestamp";
    private static final String TB_KEY_DATA = "data";
    private File dbFile;
    private String dbPath;

    @Before
    public void setUp() {
        dbFile = ApplicationProvider.getApplicationContext().getDatabasePath("test.sqlite");
        dbPath = dbFile.getPath();
        createTable();
    }

    @After
    public void dispose() {
        SQLiteDatabaseHelper.clearTable(dbPath, TABLE_NAME);
        dbFile.delete();
    }

    private void createTable() {
        final String tableCreationQuery =
                "CREATE TABLE IF NOT EXISTS "
                        + TABLE_NAME
                        + " (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, "
                        + "uniqueIdentifier TEXT NOT NULL UNIQUE, "
                        + "timestamp INTEGER NOT NULL, "
                        + "data TEXT);";

        SQLiteDatabaseHelper.createTableIfNotExist(dbPath, tableCreationQuery);
    }

    @Test
    public void testTableIsEmptyInitially() {
        // Action
        int size = SQLiteDatabaseHelper.getTableSize(dbPath, TABLE_NAME);

        // Assert
        Assert.assertEquals(size, 0);
    }

    @Test
    public void testGetTableSize_Success() {
        // Prepare data
        final String dataEntityName = "dataentity";
        DataEntity dataEntity = new DataEntity(dataEntityName);
        Map<String, Object> row = new HashMap<>();
        row.put(TB_KEY_DATA, dataEntity.getData());
        row.put(TB_KEY_UNIQUE_IDENTIFIER, dataEntity.getUniqueIdentifier());
        row.put(TB_KEY_TIMESTAMP, dataEntity.getTimestamp().getTime());

        // Action
        SQLiteDatabaseHelper.process(
                dbPath,
                SQLiteDatabaseHelper.DatabaseOpenMode.READ_WRITE,
                database -> {
                    Assert.assertNotNull(database);
                    return database.insert(TABLE_NAME, null, getContentValueFromMap(row)) > -1;
                });

        int tableSize = SQLiteDatabaseHelper.getTableSize(dbPath, TABLE_NAME);

        // Assert
        Assert.assertEquals(tableSize, 1);
    }

    @Test
    public void testGetTableSize_Failure() {
        // Prepare data
        final String dataEntityName = "dataentity";
        DataEntity dataEntity = new DataEntity(dataEntityName);
        Map<String, Object> row = new HashMap<>();
        row.put(TB_KEY_DATA, dataEntity.getData());
        row.put(TB_KEY_UNIQUE_IDENTIFIER, dataEntity.getUniqueIdentifier());
        row.put(TB_KEY_TIMESTAMP, dataEntity.getTimestamp().getTime());

        final String incorrectDbPath = "incorrect_database_path";

        // Action
        SQLiteDatabaseHelper.process(
                dbPath,
                SQLiteDatabaseHelper.DatabaseOpenMode.READ_WRITE,
                database -> {
                    Assert.assertNotNull(database);
                    return database.insert(TABLE_NAME, null, getContentValueFromMap(row)) > -1;
                });

        // Action
        Assert.assertEquals(SQLiteDatabaseHelper.getTableSize(incorrectDbPath, TABLE_NAME), 0);
    }

    @Test
    public void testClearTable_Success() {
        // Prepare data
        final String dataEntityName = "dataentity";
        DataEntity dataEntity = new DataEntity(dataEntityName);
        Map<String, Object> row = new HashMap<>();
        row.put(TB_KEY_DATA, dataEntity.getData());
        row.put(TB_KEY_UNIQUE_IDENTIFIER, dataEntity.getUniqueIdentifier());
        row.put(TB_KEY_TIMESTAMP, dataEntity.getTimestamp().getTime());

        // Action
        SQLiteDatabaseHelper.process(
                dbPath,
                SQLiteDatabaseHelper.DatabaseOpenMode.READ_WRITE,
                database -> {
                    Assert.assertNotNull(database);
                    return database.insert(TABLE_NAME, null, getContentValueFromMap(row)) > -1;
                });
        Assert.assertEquals(1, SQLiteDatabaseHelper.getTableSize(dbPath, TABLE_NAME));

        SQLiteDatabaseHelper.clearTable(dbPath, TABLE_NAME);

        // Assert
        Assert.assertTrue(SQLiteDatabaseHelper.clearTable(dbPath, TABLE_NAME));
        Assert.assertEquals(0, SQLiteDatabaseHelper.getTableSize(dbPath, TABLE_NAME));
    }

    @Test
    public void testClearTable_Failure() {
        // Prepare data
        final String dataEntityName = "dataentity";
        DataEntity dataEntity = new DataEntity(dataEntityName);
        Map<String, Object> row = new HashMap<>();
        row.put(TB_KEY_DATA, dataEntity.getData());
        row.put(TB_KEY_UNIQUE_IDENTIFIER, dataEntity.getUniqueIdentifier());
        row.put(TB_KEY_TIMESTAMP, dataEntity.getTimestamp().getTime());

        // Action
        SQLiteDatabaseHelper.process(
                dbPath,
                SQLiteDatabaseHelper.DatabaseOpenMode.READ_WRITE,
                database -> {
                    Assert.assertNotNull(database);
                    return database.insert(TABLE_NAME, null, getContentValueFromMap(row)) > -1;
                });
        Assert.assertEquals(1, SQLiteDatabaseHelper.getTableSize(dbPath, TABLE_NAME));

        String incorrectDBPath = "incorrect_database_path";

        // Assert
        Assert.assertFalse(SQLiteDatabaseHelper.clearTable(incorrectDBPath, TABLE_NAME));
        Assert.assertEquals(1, SQLiteDatabaseHelper.getTableSize(dbPath, TABLE_NAME));
    }

    @Test
    public void testProcessShouldCloseDatabase() {
        final AtomicReference<SQLiteDatabase> processedDatabase = new AtomicReference<>(null);
        SQLiteDatabaseHelper.process(
                dbPath,
                SQLiteDatabaseHelper.DatabaseOpenMode.READ_WRITE,
                database -> {
                    Assert.assertNotNull(database);
                    Assert.assertTrue(database.isOpen());
                    processedDatabase.set(database);
                    return true;
                });
        Assert.assertNotNull(processedDatabase.get());
        Assert.assertFalse(processedDatabase.get().isOpen());
    }

    @Test
    public void testProcessShouldCatchException_badDatabaseConnection() {
        boolean result =
                SQLiteDatabaseHelper.process(
                        "xxx",
                        SQLiteDatabaseHelper.DatabaseOpenMode.READ_WRITE,
                        database -> {
                            Assert.assertNull(database);
                            return false;
                        });
        Assert.assertFalse(result);
    }

    @Test
    public void testProcessShouldCatchException_badDatabaseOperations() {
        boolean result =
                SQLiteDatabaseHelper.process(
                        dbPath,
                        SQLiteDatabaseHelper.DatabaseOpenMode.READ_WRITE,
                        database -> {
                            Assert.assertNotNull(database);
                            throw new RuntimeException("xxxx");
                        });
        Assert.assertFalse(result);
    }

    private ContentValues getContentValueFromMap(final Map<String, Object> values) {
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
            }
        }

        return contentValues;
    }
}
