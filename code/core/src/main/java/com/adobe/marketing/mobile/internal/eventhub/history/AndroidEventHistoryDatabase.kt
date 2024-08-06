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
import android.database.sqlite.SQLiteDatabase
import com.adobe.marketing.mobile.internal.CoreConstants
import com.adobe.marketing.mobile.internal.eventhub.Tenant
import com.adobe.marketing.mobile.internal.util.FileUtils.moveFile
import com.adobe.marketing.mobile.internal.util.SQLiteDatabaseHelper
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceProvider
import java.io.File

internal class AndroidEventHistoryDatabase(tenant: Tenant) : EventHistoryDatabase {
    private val dbMutex = Any()
    private val databaseFile: File
    private var database: SQLiteDatabase? = null
    private val DB_SUFFIX = tenant.id ?: "default"
    private val DB_NAME: String = DATABASE_NAME + "_" + DB_SUFFIX
    private val DB_NAME_1X = DATABASE_NAME_1X + "_" + DB_SUFFIX

    /**
     * Constructor.
     *
     * @throws [EventHistoryDatabaseCreationException] if any error occurred while creating
     * the database or database table.
     */
    init {

        databaseFile = openOrMigrateEventHistoryDatabaseFile()

        val tableCreationQuery =
            "CREATE TABLE IF NOT EXISTS $TABLE_NAME (eventHash INTEGER, timestamp INTEGER);"
        synchronized(dbMutex) {
            if (!SQLiteDatabaseHelper.createTableIfNotExist(
                    databaseFile.path,
                    tableCreationQuery
                )
            ) {
                throw EventHistoryDatabaseCreationException(
                    "An error occurred while creating the $TABLE_NAME table in the Android Event History database."
                )
            }
        }
    }

    private fun openOrMigrateEventHistoryDatabaseFile(): File {
        val appContext = ServiceProvider.getInstance().appContextService.applicationContext
            ?: throw EventHistoryDatabaseCreationException(
                "Failed to create/open database $DB_NAME, error message: ApplicationContext is null"
            )
        val database = appContext.getDatabasePath(DB_NAME)
        if (database.exists()) {
            return database
        }

        // If db exists in cache directory, migrate it to new path.
        val applicationCacheDir =
            ServiceProvider.getInstance().deviceInfoService.applicationCacheDir ?: return database
        try {
            val cacheDirDatabase = File(applicationCacheDir, DB_NAME_1X)
            if (cacheDirDatabase.exists()) {
                moveFile(cacheDirDatabase, database)
                Log.debug(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "Successfully moved database $DB_NAME_1X from cache directory to database directory"
                )
            }
        } catch (e: Exception) {
            Log.debug(
                CoreConstants.LOG_TAG,
                LOG_TAG,
                "Failed to move database $DB_NAME_1X from cache directory to database directory"
            )
        }
        return database
    }

    /**
     * Insert a row into a table in the database.
     *
     * @param hash `long` containing the 32-bit FNV-1a hashed representation of an Event's data
     * @param timestampMS `long` Event's timestamp in milliseconds
     * @return a `boolean` which will contain the status of the database insert operation
     */
    override fun insert(hash: Long, timestampMS: Long): Boolean {
        synchronized(dbMutex) {
            try {
                openDatabase()
                val contentValues = ContentValues().apply {
                    put(COLUMN_HASH, hash)
                    put(COLUMN_TIMESTAMP, timestampMS)
                }
                val res = database?.insert(TABLE_NAME, null, contentValues) ?: -1
                return res > 0
            } catch (e: Exception) {
                Log.warning(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "Failed to insert rows into the table (%s)",
                    if (e.localizedMessage != null) e.localizedMessage else e.message
                )
                return false
            } finally {
                closeDatabase()
            }
        }
    }

    /**
     * Queries the database to search for the existence of events.
     * This method will count all records in the event history database that match the provided
     * hash and are within the bounds of the provided from and to timestamps.
     *
     * @param hash `long` containing the 32-bit FNV-1a hashed representation of an Event's data
     * @param from `long` a timestamp representing the lower bounds of the date range to use when searching for the hash
     * @param to `long` a timestamp representing the upper bounds of the date range to use when searching for the hash
     * @return a `QueryResult` object containing details of the matching records. If no database connection is available, returns null
     */
    override fun query(hash: Long, from: Long, to: Long): EventHistoryDatabase.QueryResult? {
        synchronized(dbMutex) {
            try {
                openDatabase()
                val rawQuery =
                    "SELECT COUNT(*) as $QUERY_COUNT, min($COLUMN_TIMESTAMP) as $QUERY_OLDEST, max($COLUMN_TIMESTAMP) as $QUERY_NEWEST FROM $TABLE_NAME WHERE $COLUMN_HASH = ? AND $COLUMN_TIMESTAMP >= ? AND $COLUMN_TIMESTAMP <= ?"
                val whereArgs = arrayOf(hash.toString(), from.toString(), to.toString())
                val cursor = database?.rawQuery(rawQuery, whereArgs) ?: return null
                cursor.use {
                    cursor.moveToFirst()
                    val count = cursor.getInt(QUERY_COUNT_INDEX)
                    val oldest = cursor.getLong(QUERY_OLDEST_INDEX)
                    val newest = cursor.getLong(QUERY_NEWEST_INDEX)
                    return EventHistoryDatabase.QueryResult(count, oldest, newest)
                }
            } catch (e: Exception) {
                Log.warning(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "Failed to execute query (%s)",
                    if (e.localizedMessage != null) e.localizedMessage else e.message
                )
                return null
            } finally {
                closeDatabase()
            }
        }
    }

    /**
     * Delete entries from the event history database.
     *
     * @param hash `long` containing the 32-bit FNV-1a hashed representation of an Event's data
     * @param from `long` representing the lower bounds of the date range to use when searching for the hash
     * @param to `long` representing the upper bounds of the date range to use when searching for the hash
     * @return `int` which will contain the number of rows deleted.
     */
    override fun delete(hash: Long, from: Long, to: Long): Int {
        synchronized(dbMutex) {
            try {
                openDatabase()
                val whereClause =
                    "$COLUMN_HASH = ? AND $COLUMN_TIMESTAMP >= ? AND $COLUMN_TIMESTAMP <= ?"
                val whereArgs = arrayOf(hash.toString(), from.toString(), to.toString())
                val affectedRowsCount = database?.delete(TABLE_NAME, whereClause, whereArgs) ?: 0
                Log.trace(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "Count of rows deleted in table $TABLE_NAME are $affectedRowsCount"
                )
                return affectedRowsCount
            } catch (e: Exception) {
                Log.debug(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "Failed to delete table rows (%s)",
                    if (e.localizedMessage != null) e.localizedMessage else e.message
                )
            } finally {
                closeDatabase()
            }
            return 0
        }
    }

    private fun openDatabase() {
        database = SQLiteDatabaseHelper.openDatabase(
            databaseFile.path,
            SQLiteDatabaseHelper.DatabaseOpenMode.READ_WRITE
        )
    }

    private fun closeDatabase() {
        SQLiteDatabaseHelper.closeDatabase(database)
        database = null
    }

    companion object {
        private const val LOG_TAG = "AndroidEventHistoryDatabase"
        private const val DATABASE_NAME = "com.adobe.module.core.eventhistory"
        private const val DATABASE_NAME_1X = "EventHistory"
        private const val TABLE_NAME = "Events"
        private const val COLUMN_HASH = "eventHash"
        private const val COLUMN_TIMESTAMP = "timestamp"
        private const val QUERY_COUNT = "count"
        private const val QUERY_COUNT_INDEX = 0
        private const val QUERY_OLDEST = "oldest"
        private const val QUERY_OLDEST_INDEX = 1
        private const val QUERY_NEWEST = "newest"
        private const val QUERY_NEWEST_INDEX = 2
    }
}
