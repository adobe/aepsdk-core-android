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

import android.content.Context;
import androidx.annotation.NonNull;
import com.adobe.marketing.mobile.internal.util.FileUtils;
import com.adobe.marketing.mobile.util.StringUtils;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to create instances of {@link DataQueue}. It caches the instances of DataQueue to ensure
 * one instance is created per database.
 */
class DataQueueService implements DataQueuing {

    private static final String LOG_TAG = "DataQueueService";
    private final Map<String, DataQueue> dataQueueCache;

    DataQueueService() {
        dataQueueCache = new HashMap<>();
    }

    @SuppressWarnings("checkstyle:NestedIfDepth")
    @Override
    public DataQueue getDataQueue(final String databaseName) {
        if (StringUtils.isNullOrEmpty(databaseName)) {
            Log.warning(
                    ServiceConstants.LOG_TAG,
                    LOG_TAG,
                    "Failed to create DataQueue, database name is null");
            return null;
        }
        DataQueue dataQueue = dataQueueCache.get(databaseName);

        if (dataQueue == null) {
            synchronized (this) {
                dataQueue = dataQueueCache.get(databaseName);

                if (dataQueue == null) {
                    final File databaseDirDataQueue = openOrMigrateExistingDataQueue(databaseName);

                    if (databaseDirDataQueue == null) {
                        Log.warning(
                                ServiceConstants.LOG_TAG,
                                LOG_TAG,
                                "Failed to create DataQueue for database (%s).",
                                databaseName);
                        return null;
                    }
                    dataQueue = new SQLiteDataQueue(databaseDirDataQueue.getPath());
                    dataQueueCache.put(databaseName, dataQueue);
                }
            }
        }

        return dataQueue;
    }

    /**
     * Returns the database if it exists in the path returned by {@link
     * Context#getDatabasePath(String)} Else copies the existing database from {@link
     * Context#getCacheDir()} to {@code Context#getDatabasePath(String)} Database is migrated from
     * cache directory because of Android 12 app hibernation changes which can clear app's cache
     * folder when user doesn't interact with app for few months.
     *
     * @param databaseName name of the database to be migrated or opened
     * @return {@code File} representing the database in {@code Context#getDatabasePath(String)}
     */
    private File openOrMigrateExistingDataQueue(@NonNull final String databaseName) {
        Context appContext =
                ServiceProvider.getInstance().getAppContextService().getApplicationContext();
        if (appContext == null) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    LOG_TAG,
                    "Failed to create DataQueue for database (%s), the ApplicationContext is null",
                    databaseName);
            return null;
        }

        final String cleanedDatabaseName = FileUtils.removeRelativePath(databaseName);
        final File databaseDirDataQueue = appContext.getDatabasePath(cleanedDatabaseName);

        // Return the db which exists in database directory.
        if (databaseDirDataQueue.exists()) {
            return databaseDirDataQueue;
        }

        // If db exists in cache directory, migrate it to new path.
        try {
            final File cacheDir =
                    ServiceProvider.getInstance().getDeviceInfoService().getApplicationCacheDir();
            if (cacheDir != null) {
                final File cacheDirDataQueue = new File(cacheDir, cleanedDatabaseName);
                if (cacheDirDataQueue.exists()) {
                    FileUtils.moveFile(cacheDirDataQueue, databaseDirDataQueue);
                    Log.debug(
                            ServiceConstants.LOG_TAG,
                            LOG_TAG,
                            "Successfully moved DataQueue for database (%s) from cache directory"
                                    + " to database directory",
                            databaseName);
                }
            }
        } catch (Exception ex) {
            Log.debug(
                    ServiceConstants.LOG_TAG,
                    LOG_TAG,
                    "Failed to move DataQueue for database (%s) from cache directory to database"
                            + " directory",
                    databaseName);
        }

        return databaseDirDataQueue;
    }
}
