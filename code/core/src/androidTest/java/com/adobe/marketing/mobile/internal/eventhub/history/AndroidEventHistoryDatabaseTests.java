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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.adobe.marketing.mobile.TestUtils;
import com.adobe.marketing.mobile.internal.util.FileUtils;
import com.adobe.marketing.mobile.internal.util.SQLiteDatabaseHelper;
import com.adobe.marketing.mobile.services.MockAppContextService;
import com.adobe.marketing.mobile.services.ServiceProviderModifier;
import java.io.File;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AndroidEventHistoryDatabaseTests {

    private static final String DATABASE_NAME = "com.adobe.module.core.eventhistory";
    private static final String DATABASE_NAME_1X = "EventHistory";
    private static final String TABLE_NAME = "Events";
    private static final String COLUMN_HASH = "eventHash";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private EventHistoryDatabase androidEventHistoryDatabase;
    private Context context;

    @Before
    public void beforeEach() {
        context = ApplicationProvider.getApplicationContext();
        MockAppContextService mockAppContextService = new MockAppContextService();
        mockAppContextService.appContext = context;
        ServiceProviderModifier.setAppContextService(mockAppContextService);

        TestUtils.deleteAllFilesInCacheDir(context);

        // Make sure databases directory exist
        context.getApplicationContext().getDatabasePath(DATABASE_NAME).getParentFile().mkdirs();
        context.getApplicationContext().getDatabasePath(DATABASE_NAME).delete();

        try {
            androidEventHistoryDatabase = new AndroidEventHistoryDatabase();
        } catch (EventHistoryDatabaseCreationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testInsertThenSelect_Happy() {
        // test insert
        long startTimestamp = System.currentTimeMillis();

        for (int i = 0; i < 10; i++) {
            assertTrue(androidEventHistoryDatabase.insert(1234567890 + i));
        }

        long endTimestamp = System.currentTimeMillis();

        // test select
        final Cursor cursor =
                androidEventHistoryDatabase.select(1234567890 + 5, 0, System.currentTimeMillis());
        // verify
        String count = cursor.getString(0);
        String oldest = cursor.getString(1);
        String newest = cursor.getString(2);
        assertEquals("1", count);
        assertTrue(TestUtils.almostEqual(Long.parseLong(oldest), startTimestamp, 1000));
        assertTrue(TestUtils.almostEqual(Long.parseLong(newest), endTimestamp, 1000));
    }

    @Test
    public void testInsertThenDelete_Happy() {
        // test insert
        long startTimestamp = System.currentTimeMillis();

        for (int i = 0; i < 15; i++) {
            assertTrue(androidEventHistoryDatabase.insert(1111111111));
        }

        for (int i = 0; i < 10; i++) {
            assertTrue(androidEventHistoryDatabase.insert(222222222));
        }

        String dbPath = context.getDatabasePath(DATABASE_NAME).getPath();
        SQLiteDatabase database =
                SQLiteDatabaseHelper.openDatabase(
                        dbPath, SQLiteDatabaseHelper.DatabaseOpenMode.READ_WRITE);
        long dbSize = DatabaseUtils.queryNumEntries(database, "Events");
        SQLiteDatabaseHelper.closeDatabase(database);
        assertEquals(25, dbSize);
        // test delete
        int deleteCount =
                androidEventHistoryDatabase.delete(
                        1111111111, startTimestamp, System.currentTimeMillis());
        database =
                SQLiteDatabaseHelper.openDatabase(
                        dbPath, SQLiteDatabaseHelper.DatabaseOpenMode.READ_WRITE);
        dbSize = DatabaseUtils.queryNumEntries(database, "Events");
        SQLiteDatabaseHelper.closeDatabase(database);
        assertEquals(15, deleteCount);
        assertEquals(10, dbSize);
    }

    @Test(expected = EventHistoryDatabaseCreationException.class)
    public void testInsert_ApplicationContextIsNull() throws EventHistoryDatabaseCreationException {
        MockAppContextService mockAppContextService = new MockAppContextService();
        mockAppContextService.appContext = null;
        ServiceProviderModifier.setAppContextService(mockAppContextService);

        new AndroidEventHistoryDatabase().insert(1111111111);
    }

    @Test
    public void testInsert_DatabasesDirectoryAbsent() {
        try {
            // delete databases dir
            FileUtils.deleteFile(context.getDatabasePath(DATABASE_NAME).getParentFile(), true);

            // create new event history database
            AndroidEventHistoryDatabase eventHistoryDatabase = new AndroidEventHistoryDatabase();
            assertTrue(eventHistoryDatabase.insert(222222222));

            final Cursor cursor2 =
                    eventHistoryDatabase.select(222222222, 0, System.currentTimeMillis());
            assertEquals("1", cursor2.getString(0));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testInsert_MigrationFromCacheDirectory() {
        try {
            // create event history database in cache directory
            createEventHistoryDatabaseInCacheDirectory();

            // delete any existing event history database
            context.getDatabasePath(DATABASE_NAME).delete();

            // create new event history database
            AndroidEventHistoryDatabase eventHistoryDatabase = new AndroidEventHistoryDatabase();
            assertTrue(eventHistoryDatabase.insert(222222222));

            // assert cache event history database content is copied to new event history database
            String dbPath = context.getDatabasePath(DATABASE_NAME).getPath();
            SQLiteDatabase database =
                    SQLiteDatabaseHelper.openDatabase(
                            dbPath, SQLiteDatabaseHelper.DatabaseOpenMode.READ_WRITE);

            long dbSize = DatabaseUtils.queryNumEntries(database, "Events");
            assertEquals(2, dbSize);

            final Cursor cursor1 =
                    eventHistoryDatabase.select(1111111111, 0, System.currentTimeMillis());
            assertEquals("1", cursor1.getString(0));

            final Cursor cursor2 =
                    eventHistoryDatabase.select(222222222, 0, System.currentTimeMillis());
            assertEquals("1", cursor2.getString(0));

            SQLiteDatabaseHelper.closeDatabase(database);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testInsert_MigrationFromCacheDirectory_DatabasesDirectoryAbsent() {
        try {
            // create event history database in cache directory
            createEventHistoryDatabaseInCacheDirectory();

            // delete databases dir
            FileUtils.deleteFile(context.getDatabasePath(DATABASE_NAME).getParentFile(), true);

            // create new event history database
            AndroidEventHistoryDatabase eventHistoryDatabase = new AndroidEventHistoryDatabase();
            assertTrue(eventHistoryDatabase.insert(222222222));

            // assert cache event history database content is copied to new event history database
            String dbPath = context.getDatabasePath(DATABASE_NAME).getPath();
            SQLiteDatabase database =
                    SQLiteDatabaseHelper.openDatabase(
                            dbPath, SQLiteDatabaseHelper.DatabaseOpenMode.READ_WRITE);

            long dbSize = DatabaseUtils.queryNumEntries(database, "Events");
            assertEquals(2, dbSize);

            final Cursor cursor1 =
                    eventHistoryDatabase.select(1111111111, 0, System.currentTimeMillis());
            assertEquals("1", cursor1.getString(0));

            final Cursor cursor2 =
                    eventHistoryDatabase.select(222222222, 0, System.currentTimeMillis());
            assertEquals("1", cursor2.getString(0));

            SQLiteDatabaseHelper.closeDatabase(database);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testInsert_DatabaseExists() {
        try {
            // insert into existing event history database
            assertTrue(androidEventHistoryDatabase.insert(1111111111));

            // create new event history database instance
            AndroidEventHistoryDatabase eventHistoryDatabase = new AndroidEventHistoryDatabase();
            assertTrue(eventHistoryDatabase.insert(222222222));

            // assert contents of event history database
            String dbPath = context.getDatabasePath(DATABASE_NAME).getPath();
            SQLiteDatabase database =
                    SQLiteDatabaseHelper.openDatabase(
                            dbPath, SQLiteDatabaseHelper.DatabaseOpenMode.READ_WRITE);

            long dbSize = DatabaseUtils.queryNumEntries(database, "Events");
            assertEquals(2, dbSize);

            final Cursor cursor1 =
                    eventHistoryDatabase.select(1111111111, 0, System.currentTimeMillis());
            assertEquals("1", cursor1.getString(0));

            final Cursor cursor2 =
                    eventHistoryDatabase.select(222222222, 0, System.currentTimeMillis());
            assertEquals("1", cursor2.getString(0));

            SQLiteDatabaseHelper.closeDatabase(database);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private void createEventHistoryDatabaseInCacheDirectory() throws Exception {
        File cacheDatabaseFile = new File(context.getCacheDir(), DATABASE_NAME_1X);
        SQLiteDatabase cacheDatabase =
                SQLiteDatabaseHelper.openDatabase(
                        cacheDatabaseFile.getPath(),
                        SQLiteDatabaseHelper.DatabaseOpenMode.READ_WRITE);
        final String tableCreationQuery =
                "CREATE TABLE IF NOT EXISTS "
                        + TABLE_NAME
                        + " (eventHash INTEGER, timestamp INTEGER);";
        SQLiteDatabaseHelper.createTableIfNotExist(
                cacheDatabaseFile.getCanonicalPath(), tableCreationQuery);
        final ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_HASH, 1111111111);
        contentValues.put(COLUMN_TIMESTAMP, System.currentTimeMillis());
        cacheDatabase.insert(TABLE_NAME, null, contentValues);
    }
}
