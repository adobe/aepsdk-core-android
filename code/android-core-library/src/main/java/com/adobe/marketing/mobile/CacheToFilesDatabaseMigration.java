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

package com.adobe.marketing.mobile;

import com.adobe.marketing.mobile.services.ServiceProvider;

import java.io.File;

class CacheToFilesDatabaseMigration {
    private static final String LOG_TAG = "CacheToFilesDatabaseMigration";

    protected void migrate() {
        final File applicationCacheDir = ServiceProvider.getInstance().getDeviceInfoService().getApplicationCacheDir();
        if (applicationCacheDir != null) {
            final File cacheDirEdgeDataQueue = new File(applicationCacheDir, CoreConstants.ExtensionNames.Edge.EDGE_EXTENSION_NAME);

            if (cacheDirEdgeDataQueue.exists()) {
                final File applicationFilesDir = ServiceProvider.getInstance().getDeviceInfoService().getApplicationFilesDir();
                if (applicationFilesDir != null) {
                    final File filesDirEdgeDataQueue = new File(applicationFilesDir, CoreConstants.ExtensionNames.Edge.EDGE_EXTENSION_NAME);
                    try {
                        FileUtil.copyFile(cacheDirEdgeDataQueue, filesDirEdgeDataQueue);
                        Log.debug(LOG_TAG, String.format("Successfully moved DataQueue for database (%s) from cache directory to files directory",
                                        CoreConstants.ExtensionNames.Edge.EDGE_EXTENSION_NAME));
                    } catch (Exception e) {
                        Log.warning(LOG_TAG, String.format("Failed in moving DataQueue for database (%s), Files dir is null.",
                                        CoreConstants.ExtensionNames.Edge.EDGE_EXTENSION_NAME));
                    }
                }
            }
        }
    }
}
