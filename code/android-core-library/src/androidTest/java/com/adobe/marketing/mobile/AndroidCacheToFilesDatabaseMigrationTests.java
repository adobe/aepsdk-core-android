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

import static com.adobe.marketing.mobile.AndroidDatabaseMigrationTestHelper.MOCK_FILE_CONTENT;
import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertNull;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.adobe.marketing.mobile.services.ServiceProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class AndroidCacheToFilesDatabaseMigrationTests {

    private CacheToFilesDatabaseMigration databaseMigrationTool;
    private AndroidDatabaseMigrationTestHelper migrationTestHelper;

    @Before
    public void setup() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        ServiceProvider.getInstance().setContext(appContext);
        databaseMigrationTool = new CacheToFilesDatabaseMigration();
        migrationTestHelper = new AndroidDatabaseMigrationTestHelper();
    }

    @After
    public void tearDown() {
        migrationTestHelper.deleteDirectory(ServiceProvider.getInstance().getDeviceInfoService().getApplicationCacheDir());
        migrationTestHelper.deleteDirectory(ServiceProvider.getInstance().getDeviceInfoService().getApplicationFilesDir());
    }

    @Test
    public void testDatabaseMigration_ExistingDatabase() {
        migrationTestHelper.createMockEdgeDatabaseInCacheDir();
        databaseMigrationTool.migrate();
        assertEquals(MOCK_FILE_CONTENT, FileUtil.readStringFromFile(new File(ServiceProvider.getInstance().getDeviceInfoService().getApplicationFilesDir(),
                CoreConstants.ExtensionNames.Edge.EDGE_EXTENSION_NAME)));
    }

    @Test
    public void testDatabaseMigration_NoDatabase() {
        databaseMigrationTool.migrate();
        assertNull(FileUtil.readStringFromFile(new File(ServiceProvider.getInstance().getDeviceInfoService().getApplicationFilesDir(),
                CoreConstants.ExtensionNames.Edge.EDGE_EXTENSION_NAME)));
    }
}
