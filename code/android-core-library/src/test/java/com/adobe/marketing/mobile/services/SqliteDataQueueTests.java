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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.anyString;

import android.database.sqlite.SQLiteException;
import com.adobe.marketing.mobile.internal.util.SQLiteDatabaseHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SqliteDataQueueTests {

    private DataQueue dataQueue;

    private static final String DATABASE_NAME = "test.sqlite";
    private static final String TABLE_NAME = "TB_AEP_DATA_ENTITY";
    private static final String EMPTY_JSON_STRING = "{}";
    private static final String TB_KEY_UNIQUE_IDENTIFIER = "uniqueIdentifier";
    private static final String TB_KEY_TIMESTAMP = "timestamp";
    private static final String TB_KEY_DATA = "data";

    public SqliteDataQueueTests() {}

    @Before
    public void setUp() {
        //        PowerMockito.when(SQLiteDatabaseHelper.createTableIfNotExist(Mockito.anyString(),
        // Mockito.anyString())).thenReturn(true);
        dataQueue = null;
    }

    @Test
    public void addDataEntitySuccess() {
        DataEntity dataEntity = new DataEntity(EMPTY_JSON_STRING);
        try (MockedStatic<SQLiteDatabaseHelper> helperMock =
                Mockito.mockStatic(SQLiteDatabaseHelper.class)) {
            dataQueue = new SQLiteDataQueue(DATABASE_NAME);
            helperMock
                    .when(
                            () ->
                                    SQLiteDatabaseHelper.createTableIfNotExist(
                                            Mockito.anyString(), Mockito.anyString()))
                    .thenReturn(true);
            helperMock
                    .when(
                            () ->
                                    SQLiteDatabaseHelper.process(
                                            Mockito.anyString(), Mockito.any(), Mockito.any()))
                    .thenReturn(true);
            boolean result = dataQueue.add(dataEntity);

            assertTrue(result);
        }
    }

    @Test
    public void addDataEntityFailure() {
        // Setup

        DataEntity dataEntity = new DataEntity(EMPTY_JSON_STRING);
        try (MockedStatic<SQLiteDatabaseHelper> helperMock =
                Mockito.mockStatic(SQLiteDatabaseHelper.class)) {
            dataQueue = new SQLiteDataQueue(DATABASE_NAME);
            helperMock
                    .when(
                            () ->
                                    SQLiteDatabaseHelper.createTableIfNotExist(
                                            Mockito.anyString(), Mockito.anyString()))
                    .thenReturn(true);
            helperMock
                    .when(
                            () ->
                                    SQLiteDatabaseHelper.process(
                                            Mockito.anyString(), Mockito.any(), Mockito.any()))
                    .thenReturn(false);
            // Action
            boolean result = dataQueue.add(dataEntity);

            // Assertions
            assertFalse(result);
        }
    }

    @Test
    public void testClearTable() {
        // setup
        try (MockedStatic<SQLiteDatabaseHelper> helperMock =
                Mockito.mockStatic(SQLiteDatabaseHelper.class)) {
            helperMock
                    .when(() -> SQLiteDatabaseHelper.clearTable(anyString(), anyString()))
                    .thenReturn(true);
            // Actions
            boolean result = SQLiteDatabaseHelper.clearTable(DATABASE_NAME, TABLE_NAME);

            // Assertions
            assertTrue(result);
        }
    }

    @Test
    public void testTableCount() {
        // Setup
        final int mockedTableSize = 10;
        try (MockedStatic<SQLiteDatabaseHelper> helperMock =
                Mockito.mockStatic(SQLiteDatabaseHelper.class)) {
            helperMock
                    .when(() -> SQLiteDatabaseHelper.getTableSize(anyString(), anyString()))
                    .thenReturn(mockedTableSize);
            // Actions
            int tableSize = SQLiteDatabaseHelper.getTableSize(DATABASE_NAME, TABLE_NAME);

            // Assertions
            assertEquals(tableSize, mockedTableSize);
        }
    }

    @Test
    public void testClose() {
        try (MockedStatic<SQLiteDatabaseHelper> helperMock =
                Mockito.mockStatic(SQLiteDatabaseHelper.class)) {
            dataQueue = new SQLiteDataQueue(DATABASE_NAME);
            helperMock
                    .when(
                            () ->
                                    SQLiteDatabaseHelper.createTableIfNotExist(
                                            Mockito.anyString(), Mockito.anyString()))
                    .thenReturn(true);
            // Actions
            dataQueue.close();

            // Assertions
            assertFalse(dataQueue.add(new DataEntity(EMPTY_JSON_STRING)));
            assertNull(dataQueue.peek());
            assertNull(dataQueue.peek(10));
            assertFalse(dataQueue.remove());
            assertFalse(dataQueue.remove(10));
            assertFalse(dataQueue.clear());
            assertEquals(dataQueue.count(), 0);
        }
    }

    // Unit test failure in opening database in different scenarios.

    @Test
    public void addDataEntityWithDatabaseOpenError() {
        try (MockedStatic<SQLiteDatabaseHelper> helperMock =
                Mockito.mockStatic(SQLiteDatabaseHelper.class)) {
            dataQueue = new SQLiteDataQueue(DATABASE_NAME);
            helperMock
                    .when(
                            () ->
                                    SQLiteDatabaseHelper.createTableIfNotExist(
                                            Mockito.anyString(), Mockito.anyString()))
                    .thenReturn(true);
            helperMock
                    .when(
                            () ->
                                    SQLiteDatabaseHelper.process(
                                            Mockito.anyString(), Mockito.any(), Mockito.any()))
                    .thenCallRealMethod();
            helperMock
                    .when(
                            () ->
                                    SQLiteDatabaseHelper.openDatabase(
                                            DATABASE_NAME,
                                            SQLiteDatabaseHelper.DatabaseOpenMode.READ_WRITE))
                    .thenThrow(SQLiteException.class);
            boolean result = dataQueue.add(new DataEntity(EMPTY_JSON_STRING));

            // Assertions
            Assert.assertFalse(result);
        }
    }

    @Test
    public void clearTableWithDatabaseOpenError() {
        try (MockedStatic<SQLiteDatabaseHelper> helperMock =
                Mockito.mockStatic(SQLiteDatabaseHelper.class)) {
            dataQueue = new SQLiteDataQueue(DATABASE_NAME);
            helperMock
                    .when(
                            () ->
                                    SQLiteDatabaseHelper.createTableIfNotExist(
                                            Mockito.anyString(), Mockito.anyString()))
                    .thenReturn(true);
            helperMock
                    .when(() -> SQLiteDatabaseHelper.clearTable(anyString(), anyString()))
                    .thenCallRealMethod();
            helperMock
                    .when(
                            () ->
                                    SQLiteDatabaseHelper.openDatabase(
                                            DATABASE_NAME,
                                            SQLiteDatabaseHelper.DatabaseOpenMode.READ_WRITE))
                    .thenThrow(SQLiteException.class);
            boolean result = dataQueue.clear();

            // Assertions
            Assert.assertFalse(result);
        }
    }

    @Test
    public void getTableSizeWithDatabaseOpenError() {
        try (MockedStatic<SQLiteDatabaseHelper> helperMock =
                Mockito.mockStatic(SQLiteDatabaseHelper.class)) {
            dataQueue = new SQLiteDataQueue(DATABASE_NAME);
            helperMock
                    .when(
                            () ->
                                    SQLiteDatabaseHelper.createTableIfNotExist(
                                            Mockito.anyString(), Mockito.anyString()))
                    .thenReturn(true);
            helperMock
                    .when(() -> SQLiteDatabaseHelper.getTableSize(anyString(), anyString()))
                    .thenCallRealMethod();
            helperMock
                    .when(
                            () ->
                                    SQLiteDatabaseHelper.openDatabase(
                                            DATABASE_NAME,
                                            SQLiteDatabaseHelper.DatabaseOpenMode.READ_ONLY))
                    .thenThrow(SQLiteException.class);
            int result = dataQueue.count();

            // Assertions
            Assert.assertEquals(result, 0);
        }
    }
}
