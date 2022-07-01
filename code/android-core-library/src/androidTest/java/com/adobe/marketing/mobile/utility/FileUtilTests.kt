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
package com.adobe.marketing.mobile.utility

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.utility.FileUtil
import java.io.File
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FileUtilTests {

    private lateinit var context: Context
    private val testDatabaseName = "test.sqlite"

    @Before
    fun beforeEach() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        ServiceProvider.getInstance().setContext(context)
    }

    @After
    fun afterEach() {
        context.getDatabasePath(testDatabaseName).delete()
        File(context.cacheDir, testDatabaseName).delete()
    }

    @Test
    fun testRemoveRelativePath_RelativePathBackslashClearnedUp() {
        assertEquals(FileUtil.removeRelativePath("/mydatabase\\..\\..\\database1"), "mydatabase_database1")
    }

    @Test
    fun testRemoveRelativePath_RelativePathForwardslashClearnedUp() {
        assertEquals(FileUtil.removeRelativePath("/mydatabase/../../database1"), "mydatabase_database1")
    }

    @Test
    fun testRemoveRelativePath_RelativePathBackslashDoesNotChangeDir() {
        assertEquals(FileUtil.removeRelativePath("/mydatabase\\..\\database1"), "mydatabase_database1")
    }

    @Test
    fun testRemoveRelativePath_RelativePathForwardslashDoesNotChangeDir() {
        assertEquals(FileUtil.removeRelativePath("/mydatabase/../database1"), "mydatabase_database1")
    }

    @Test
    fun testOpenOrCreateDatabase_DatabaseNameIsNullOrEmpty() {
        assertNull(FileUtil.openOrCreateDatabase("", context))
    }

    @Test
    fun testOpenOrMigrateDatabase_DatabaseDoesNotExist() {
        assertNotNull(FileUtil.openOrCreateDatabase(testDatabaseName, context))
        assertTrue(context.getDatabasePath(testDatabaseName).exists())
    }

    @Test
    fun testOpenOrMigrateDatabase_DatabaseExistsInDatabaseDir() {
        val file = context.getDatabasePath(testDatabaseName)
        try {
            file.createNewFile()
            val database = FileUtil.openOrCreateDatabase(testDatabaseName, context)
            assertNotNull(database)
        } catch (e: Exception) {}
    }

    @Test
    fun testMigrateAndODeleteOldDatabase() {
        val file = File(context.cacheDir, testDatabaseName)
        try {
            file.createNewFile()
            assertTrue(File(context.cacheDir, testDatabaseName).exists())
            val database = FileUtil.openOrCreateDatabase(testDatabaseName, context)
            if (database != null) {
                FileUtil.migrateAndDeleteOldDatabase(database)
            }
            assertNotNull(database)
            assertTrue(context.getDatabasePath(testDatabaseName).exists())
            assertFalse(File(context.cacheDir, testDatabaseName).exists())
        } catch (e: Exception) {}
    }
}
