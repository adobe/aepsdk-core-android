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

package com.adobe.marketing.mobile.internal.eventhub.history

import android.content.ContentValues
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adobe.marketing.mobile.TestUtils
import com.adobe.marketing.mobile.internal.util.FileUtils.deleteFile
import com.adobe.marketing.mobile.internal.util.SQLiteDatabaseHelper
import com.adobe.marketing.mobile.services.MockAppContextService
import com.adobe.marketing.mobile.services.ServiceProviderModifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class AndroidEventHistoryDatabaseTests {
    private lateinit var androidEventHistoryDatabase: EventHistoryDatabase
    private lateinit var context: Context

    @Before
    fun beforeEach() {
        context = ApplicationProvider.getApplicationContext()
        val mockAppContextService = MockAppContextService().apply {
            appContext = context
        }
        ServiceProviderModifier.setAppContextService(mockAppContextService)

        // Make sure databases directory exist
        context.applicationContext.getDatabasePath(DATABASE_NAME).parentFile?.mkdirs()
        TestUtils.deleteAllFilesInCacheDir(context)
        context.applicationContext.getDatabasePath(DATABASE_NAME).delete()

        try {
            androidEventHistoryDatabase = AndroidEventHistoryDatabase()
        } catch (e: EventHistoryDatabaseCreationException) {
            fail(e.message)
        }
    }

    @Test
    fun testInsertThenSelect_Happy() {
        // test insert
        val startTimestamp = 1000000L
        val endTimestamp = startTimestamp + 10000

        for (i in 0..10) {
            assertTrue(
                androidEventHistoryDatabase.insert(
                    (1234567890 + i).toLong(),
                    startTimestamp + (i * 1000)
                )
            )
        }

        // test select
        val record = 5
        val result = androidEventHistoryDatabase.query(
            (1234567890 + record).toLong(),
            0,
            endTimestamp
        )

        val timestamp = startTimestamp + (record * 1000)
        // verify
        assertNotNull(result)
        assertEquals(1, result?.count)
        assertEquals(timestamp, result?.oldestTimestamp)
        assertEquals(timestamp, result?.newestTimeStamp)
    }

    @Test
    fun testInsertThenSelectMultiple_Happy() {
        // test insert
        val startTimestamp = 1000000L
        val endTimestamp = startTimestamp + 10000

        for (i in 0..10) {
            assertTrue(
                androidEventHistoryDatabase.insert(
                    (1234567890).toLong(),
                    startTimestamp + (i * 1000)
                )
            )
        }

        // test select
        val result = androidEventHistoryDatabase.query(
            (1234567890).toLong(),
            0,
            endTimestamp
        )

        // verify
        assertEquals(11, result?.count)
        assertEquals(startTimestamp, result?.oldestTimestamp)
        assertEquals(endTimestamp, result?.newestTimeStamp)
    }

    @Test
    fun testInsertThenDelete_Happy() {
        // test insert
        val startTimestamp = 100000L
        for (i in 0..14) {
            assertTrue(
                androidEventHistoryDatabase.insert(
                    1111111111,
                    startTimestamp + (i * 10000)
                )
            )
        }

        // test delete
        val deleteCount = androidEventHistoryDatabase.delete(
            1111111111,
            startTimestamp,
            System.currentTimeMillis()
        )
        assertEquals(15, deleteCount)
    }

    @Test(expected = EventHistoryDatabaseCreationException::class)
    fun testInsert_ApplicationContextIsNull() {
        val mockAppContextService = MockAppContextService()
        mockAppContextService.appContext = null
        ServiceProviderModifier.setAppContextService(mockAppContextService)
        AndroidEventHistoryDatabase().insert(1111111111, System.currentTimeMillis())
    }

    @Test
    fun testInsert_DatabasesDirectoryAbsent() {
        // delete databases dir
        deleteFile(context.getDatabasePath(DATABASE_NAME).parentFile, true)

        // create new event history database
        val eventHistoryDatabase = AndroidEventHistoryDatabase()
        assertTrue(eventHistoryDatabase.insert(222222222, System.currentTimeMillis()))
        val res = eventHistoryDatabase.query(222222222, 0, System.currentTimeMillis())
        assertNotNull(res)
        assertEquals(1, res?.count)
    }

    @Test
    fun testInsert_MigrationFromCacheDirectory() {
        // create event history database in cache directory
        createEventHistoryDatabaseInCacheDirectory()

        // delete any existing event history database
        context.getDatabasePath(DATABASE_NAME).delete()

        // create new event history database
        val eventHistoryDatabase = AndroidEventHistoryDatabase()
        assertTrue(eventHistoryDatabase.insert(222222222, System.currentTimeMillis()))

        // assert cache event history database content is copied to new event history database
        val query1 = eventHistoryDatabase.query(1111111111, 0, System.currentTimeMillis())
        assertEquals(1, query1?.count)

        val query2 = eventHistoryDatabase.query(222222222, 0, System.currentTimeMillis())
        assertEquals(1, query2?.count)
    }

    @Test
    fun testInsert_MigrationFromCacheDirectory_DatabasesDirectoryAbsent() {
        // create event history database in cache directory
        createEventHistoryDatabaseInCacheDirectory()

        // delete databases dir
        deleteFile(context.getDatabasePath(DATABASE_NAME).parentFile, true)

        // create new event history database
        val eventHistoryDatabase = AndroidEventHistoryDatabase()
        assertTrue(eventHistoryDatabase.insert(222222222, System.currentTimeMillis()))

        // assert cache event history database content is copied to new event history database
        val query1 = eventHistoryDatabase.query(1111111111, 0, System.currentTimeMillis())
        assertEquals(1, query1?.count)

        val query2 = eventHistoryDatabase.query(222222222, 0, System.currentTimeMillis())
        assertEquals(1, query2?.count)
    }

    @Throws(Exception::class)
    private fun createEventHistoryDatabaseInCacheDirectory() {
        val cacheDatabaseFile = File(context.cacheDir, DATABASE_NAME_1X)
        val cacheDatabase = SQLiteDatabaseHelper.openDatabase(
            cacheDatabaseFile.path,
            SQLiteDatabaseHelper.DatabaseOpenMode.READ_WRITE
        )
        val tableCreationQuery = (
            "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME +
                " (eventHash INTEGER, timestamp INTEGER);"
            )
        SQLiteDatabaseHelper.createTableIfNotExist(
            cacheDatabaseFile.canonicalPath,
            tableCreationQuery
        )
        val contentValues = ContentValues()
        contentValues.put(COLUMN_HASH, 1111111111)
        contentValues.put(COLUMN_TIMESTAMP, System.currentTimeMillis())
        cacheDatabase.insert(TABLE_NAME, null, contentValues)
    }

    companion object {
        private const val DATABASE_NAME = "com.adobe.module.core.eventhistory"
        private const val DATABASE_NAME_1X = "EventHistory"
        private const val TABLE_NAME = "Events"
        private const val COLUMN_HASH = "eventHash"
        private const val COLUMN_TIMESTAMP = "timestamp"
    }
}
