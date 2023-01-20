/*
  Copyright 2023 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.util;

import com.adobe.marketing.mobile.internal.CoreConstants;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.ServiceProvider;
import java.io.File;

public class FileUtils {
    private static final String LOG_SOURCE = "FileUtils";

    private FileUtils() {}

    /**
     * Deletes a file in the Application's cache folder.
     *
     * @param fileName the file name to be deleted
     * @return true, if the file is successfully deleted; false otherwise
     */
    public static boolean deleteFileFromCacheDir(final String fileName) {
        try {
            final File cacheDir =
                    ServiceProvider.getInstance().getDeviceInfoService().getApplicationCacheDir();
            if (cacheDir == null || StringUtils.isNullOrEmpty(fileName)) {
                return false;
            }
            final File filePath = new File(cacheDir, fileName);
            if (filePath.exists()) {
                return filePath.delete();
            }
            return false;
        } catch (Exception e) {
            Log.debug(
                    CoreConstants.LOG_TAG,
                    LOG_SOURCE,
                    "Failed to delete (%s) in cache folder.",
                    fileName);
            return false;
        }
    }
}
