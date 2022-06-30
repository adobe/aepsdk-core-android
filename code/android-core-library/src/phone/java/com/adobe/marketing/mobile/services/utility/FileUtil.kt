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
package com.adobe.marketing.mobile.services.utility

import android.content.Context
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.services.ServiceProvider
import java.io.File

internal object FileUtil {

    private const val LOG_TAG = "FileUtil"

    /**
     * Removes the relative part of the file name(if exists).
     *
     * for ex: File name `/mydatabase/../../database1` will be converted to `mydatabase_database1`
     *
     * @param filePath the file name
     * @return file name without relative path
     */
    @JvmStatic
    fun removeRelativePath(filePath: String): String {
        return if (filePath.isBlank()) {
            filePath
        } else {
            var result = filePath.replace("\\.[/\\\\]".toRegex(), "\\.")
            result = result.replace("[/\\\\](\\.{2,})".toRegex(), "_")
            result = result.replace("/".toRegex(), "")
            result
        }
    }

    /**
     * Returns the database if it exists in the path returned by [Context.getDatabasePath]
     * Else copies the existing database from [Context.getCacheDir] to `Context#getDatabasePath(String)`
     * Database is migrated from cache directory because of Android 12 app hibernation changes
     * which can clear app's cache folder when user doesn't interact with app for few months.
     *
     * @param databaseName name of the database to be migrated or opened
     * @return `File` representing the database in [Context.getDatabasePath]`
     */
    @JvmStatic
    fun openOrMigrateDatabase(databaseName: String, appContext: Context?): File? {
        if (databaseName.isBlank()) {
            MobileCore.log(
                LoggingMode.WARNING,
                LOG_TAG,
                "Failed to create database, database name is empty"
            )
            return null
        }
        if (appContext == null) {
            MobileCore.log(
                LoggingMode.WARNING,
                LOG_TAG,
                String.format(
                    "Failed to create database (%s), the ApplicationContext is null",
                    databaseName
                )
            )
            return null
        }
        val cleanedDatabaseName = removeRelativePath(databaseName)
        if (cleanedDatabaseName.isBlank()) {
            MobileCore.log(
                LoggingMode.WARNING,
                LOG_TAG,
                String.format(
                    "Failed to create database (%s), database name is null",
                    databaseName
                )
            )
            return null
        }
        val databaseDirDatabase = appContext.getDatabasePath(cleanedDatabaseName)
        if (!databaseDirDatabase.exists()) {
            try {
                if (databaseDirDatabase.createNewFile()) {
                    val cacheDir =
                        ServiceProvider.getInstance().deviceInfoService.applicationCacheDir
                    if (cacheDir != null) {
                        val cacheDirDatabase = File(cacheDir, cleanedDatabaseName)
                        if (cacheDirDatabase.exists()) {
                            cacheDirDatabase.copyTo(databaseDirDatabase, true)
                            MobileCore.log(
                                LoggingMode.DEBUG,
                                LOG_TAG,
                                String.format(
                                    "Successfully moved DataQueue for database (%s) from cache directory to database directory",
                                    databaseName
                                )
                            )
                            if (cacheDirDatabase.delete()) {
                                MobileCore.log(
                                    LoggingMode.DEBUG,
                                    LOG_TAG,
                                    String.format(
                                        "Successfully delete DataQueue for database (%s) from cache directory",
                                        databaseName
                                    )
                                )
                            }
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                MobileCore.log(
                    LoggingMode.WARNING,
                    LOG_TAG,
                    String.format(
                        "Failed to move DataQueue for database (%s), could not create new file in database directory",
                        databaseName
                    )
                )
                return null
            }
        }
        return databaseDirDatabase
    }
}
