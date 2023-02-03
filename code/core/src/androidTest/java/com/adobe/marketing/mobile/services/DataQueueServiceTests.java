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

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.adobe.marketing.mobile.internal.util.FileUtils;
import java.io.File;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DataQueueServiceTests {

    private static final String TEST_DATABASE_NAME = "test.sqlite";
    MockAppContextService mockAppContextService;
    private Context context;

    @Before
    public void beforeEach() {
        context = ApplicationProvider.getApplicationContext();
        mockAppContextService = new MockAppContextService();
        mockAppContextService.appContext = context;

        ServiceProviderModifier.setAppContextService(mockAppContextService);

        // Ensure databases directory exist
        context.getDatabasePath(TEST_DATABASE_NAME).getParentFile().mkdirs();
    }

    @After
    public void afterEach() {
        context.getDatabasePath(TEST_DATABASE_NAME).delete();
        new File(context.getCacheDir(), TEST_DATABASE_NAME).delete();
    }

    @Test
    public void testGetDataQueue_DatabaseNameIsNullOrEmpty() {
        DataQueue dataQueue = new DataQueueService().getDataQueue(null);
        assertNull(dataQueue);
        dataQueue = new DataQueueService().getDataQueue("");
        assertNull(dataQueue);
    }

    @Test
    public void testGetDataQueue_ApplicationContextIsNotSet() {
        mockAppContextService.appContext = null;
        DataQueue dataQueue = new DataQueueService().getDataQueue(TEST_DATABASE_NAME);
        assertNull(dataQueue);
    }

    @Test
    public void testGetDataQueue_DataQueueDoesNotExist() {
        assertFalse(context.getDatabasePath(TEST_DATABASE_NAME).exists());
        DataQueue dataQueue = new DataQueueService().getDataQueue(TEST_DATABASE_NAME);
        assertNotNull(dataQueue);
        assertTrue(context.getDatabasePath(TEST_DATABASE_NAME).exists());
    }

    @Test
    public void testGetDataQueue_DatabasesDirectoryAbsent() {
        // Delete databases directory
        FileUtils.deleteFile(context.getDatabasePath(TEST_DATABASE_NAME).getParentFile(), true);
        assertFalse(context.getDatabasePath(TEST_DATABASE_NAME).exists());
        DataQueue dataQueue = new DataQueueService().getDataQueue(TEST_DATABASE_NAME);
        assertNotNull(dataQueue);
        assertTrue(context.getDatabasePath(TEST_DATABASE_NAME).exists());
    }

    @Test
    public void testGetDataQueue_DataQueueMigrationFromCacheDirectory() {
        // Delete databases directory
        FileUtils.deleteFile(context.getDatabasePath(TEST_DATABASE_NAME).getParentFile(), true);
        assertFalse(context.getDatabasePath(TEST_DATABASE_NAME).exists());
        File cacheDatabaseFile = new File(context.getCacheDir(), TEST_DATABASE_NAME);
        DataQueue dataQueue = new SQLiteDataQueue(cacheDatabaseFile.getPath());
        dataQueue.add(new DataEntity("test_data_1"));
        DataQueue dataQueueExisting = new DataQueueService().getDataQueue(TEST_DATABASE_NAME);
        Assert.assertEquals("test_data_1", dataQueueExisting.peek().getData());
        assertFalse(cacheDatabaseFile.exists());
    }

    @Test
    public void testGetDataQueue_DataQueueMigrationFromCacheDirectory_DatabasesDirectoryAbsent() {
        assertFalse(context.getDatabasePath(TEST_DATABASE_NAME).exists());
        File cacheDatabaseFile = new File(context.getCacheDir(), TEST_DATABASE_NAME);
        DataQueue dataQueue = new SQLiteDataQueue(cacheDatabaseFile.getPath());
        dataQueue.add(new DataEntity("test_data_1"));
        DataQueue dataQueueExisting = new DataQueueService().getDataQueue(TEST_DATABASE_NAME);
        Assert.assertEquals("test_data_1", dataQueueExisting.peek().getData());
        assertFalse(cacheDatabaseFile.exists());
    }

    @Test
    public void testGetDataQueue_DataQueueExistsInDatabaseDirectory() {
        File databaseFile = context.getDatabasePath(TEST_DATABASE_NAME);
        DataQueue dataQueue = new SQLiteDataQueue(databaseFile.getPath());
        dataQueue.add(new DataEntity("test_data_1"));
        DataQueue dataQueueExisting = new DataQueueService().getDataQueue(TEST_DATABASE_NAME);
        Assert.assertEquals("test_data_1", dataQueueExisting.peek().getData());
    }

    @Test
    public void testGetDataQueue_DataQueueExistsInDataQueueCache() {
        DataQueueService dataQueueService = new DataQueueService();
        DataQueue dataQueue = dataQueueService.getDataQueue(TEST_DATABASE_NAME);
        dataQueue.add(new DataEntity("test_data_1"));
        DataQueue dataQueueExisting = dataQueueService.getDataQueue(TEST_DATABASE_NAME);
        Assert.assertEquals("test_data_1", dataQueueExisting.peek().getData());
    }
}
