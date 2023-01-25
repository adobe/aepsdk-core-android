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
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.File;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SqliteDataQueueTests {

    private File dbFile;
    private DataQueue dataQueue;
    private static final String QUEUE_NAME = "test.dataQueue";

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        dbFile = context.getDatabasePath(QUEUE_NAME);
        dataQueue = new SQLiteDataQueue(context.getDatabasePath(QUEUE_NAME).getPath());
    }

    @After
    public void tearDown() {
        if (dbFile != null && dbFile.exists()) {
            dbFile.delete();
        }
    }

    @Test
    public void testAddPeek() {
        dataQueue.add(new DataEntity("test_data_1"));
        dataQueue.add(new DataEntity("test_data_2"));
        dataQueue.add(new DataEntity("test_data_3"));
        Assert.assertEquals("test_data_1", dataQueue.peek().getData());
    }

    @Test
    public void testAddPeekN() {
        dataQueue.add(new DataEntity("test_data_1"));
        dataQueue.add(new DataEntity("test_data_2"));
        dataQueue.add(new DataEntity("test_data_3"));
        List<DataEntity> results = dataQueue.peek(3);
        Assert.assertEquals(3, results.size());
        Assert.assertEquals("test_data_1", results.get(0).getData());
        Assert.assertEquals("test_data_2", results.get(1).getData());
        Assert.assertEquals("test_data_3", results.get(2).getData());
    }

    @Test
    public void testAddRemove() {
        dataQueue.add(new DataEntity("test_data_1"));
        dataQueue.add(new DataEntity("test_data_2"));
        dataQueue.add(new DataEntity("test_data_3"));
        Assert.assertEquals(3, dataQueue.peek(4).size());
        dataQueue.remove();
        List<DataEntity> results = dataQueue.peek(4);
        Assert.assertEquals(2, results.size());
        Assert.assertEquals("test_data_2", results.get(0).getData());
        Assert.assertEquals("test_data_3", results.get(1).getData());
    }

    @Test
    public void testAddRemoveN() {
        dataQueue.add(new DataEntity("test_data_1"));
        dataQueue.add(new DataEntity("test_data_2"));
        dataQueue.add(new DataEntity("test_data_3"));
        Assert.assertEquals(3, dataQueue.peek(4).size());
        dataQueue.remove(2);
        List<DataEntity> results = dataQueue.peek(4);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("test_data_3", results.get(0).getData());
    }

    @Test
    public void testClear() {
        dataQueue.add(new DataEntity("test_data_1"));
        dataQueue.add(new DataEntity("test_data_2"));
        dataQueue.add(new DataEntity("test_data_3"));
        Assert.assertEquals(3, dataQueue.peek(4).size());
        dataQueue.clear();
        List<DataEntity> results = dataQueue.peek(4);
        Assert.assertEquals(0, results.size());
    }
}
