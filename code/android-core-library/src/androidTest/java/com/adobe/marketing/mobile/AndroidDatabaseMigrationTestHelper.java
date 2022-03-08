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
import java.io.FileWriter;
import java.io.IOException;

class AndroidDatabaseMigrationTestHelper {

    static final String MOCK_FILE_CONTENT 	= "Sample database file contents";

    void createMockEdgeDatabaseInCacheDir() {
        File cacheFile = new File(ServiceProvider.getInstance().getDeviceInfoService().getApplicationCacheDir(),
                CoreConstants.ExtensionNames.Edge.EDGE_EXTENSION_NAME);

        try {
            FileWriter fileWriter = new FileWriter(cacheFile);
            fileWriter.write(MOCK_FILE_CONTENT);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException ex) {}
    }

    /**
     * Removes the cache directory recursively
     */
    void deleteDirectory(final File directory) {
        if (directory != null) {
            String[] files = directory.list();

            if (files != null) {
                for (String file : files) {
                    File currentFile = new File(directory.getPath(), file);

                    if (currentFile.isDirectory()) {
                        deleteDirectory(currentFile);
                    } else {
                        currentFile.delete();
                    }
                }
            }

            directory.delete();
        }
    }
}
