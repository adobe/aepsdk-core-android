package com.adobe.marketing.mobile.services;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.adobe.marketing.mobile.services.utility.FileUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

@RunWith(AndroidJUnit4.class)
public class DataQueueServiceTests {

    private static final String DATABASE_NAME = "testDatabase";

    @Before
    public void beforeEach() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        ServiceProvider.getInstance().setContext(context);
    }

    @After
    public void tearDown() {
        if(ServiceProvider.getInstance().getApplicationContext() != null) {
            new File(ServiceProvider.getInstance().getDeviceInfoService().getApplicationCacheDir(), DATABASE_NAME).delete();
            ServiceProvider.getInstance().getApplicationContext().getDatabasePath(DATABASE_NAME).delete();
        }
    }

    @Test
    public void testApplicationContextIsNotSet() {
        ServiceProvider.getInstance().setContext(null);
        DataQueue dataQueue = new DataQueueService().getDataQueue(DATABASE_NAME);
        assertNull(dataQueue);
    }

    @Test
    public void testGetDataQueue_NewQueue() {
        DataQueue dataQueue = new DataQueueService().getDataQueue(DATABASE_NAME);
        assertNotNull(dataQueue);
    }

    @Test
    public void testGetDataQueue_MigrateFromCacheDir() {
        if(createDataQueue(new File(ServiceProvider.getInstance().getDeviceInfoService().getApplicationCacheDir(), DATABASE_NAME), 1)) {
            DataQueue dataQueue = new DataQueueService().getDataQueue(DATABASE_NAME);

            assertNotNull(dataQueue);
            assertEquals(dataQueue.count(), 1);
        }
    }

    @Test
    public void testGetDataQueue_DataQueueInCacheDirAndDatabaseDir() {
        if(createDataQueue(new File(ServiceProvider.getInstance().getDeviceInfoService().getApplicationCacheDir(), DATABASE_NAME),1)
        && createDataQueue(ServiceProvider.getInstance().getApplicationContext().getDatabasePath(DATABASE_NAME), 2)){
            DataQueue dataQueue = new DataQueueService().getDataQueue(DATABASE_NAME);

            assertNotNull(dataQueue);
            assertEquals(dataQueue.count(), 2);
        }
    }

    @Test
    public void testGetDataQueue_NonEmptyDataQueueCache() {
        DataQueueService dataQueueService = new DataQueueService();
        DataQueue dataQueue = dataQueueService.getDataQueue(DATABASE_NAME);
        dataQueue.add(new DataEntity("{}"));

        assertEquals(dataQueueService.getDataQueue(DATABASE_NAME).count(), 1);
    }

    @Test
    public void testGetDataQueue_RelativePathBackslashCleanedUp() {
        DataQueueService dataQueueService = new DataQueueService();
        DataQueue dataQueue = dataQueueService.getDataQueue("/mydatabase\\..\\..\\database1");
        dataQueue.add(new DataEntity("{}"));

        assertEquals(dataQueueService.getDataQueue("/mydatabase\\..\\..\\database1").count(), 1);
        ServiceProvider.getInstance().getApplicationContext().getDatabasePath(FileUtil.removeRelativePath("/mydatabase\\..\\..\\database1")).delete();
    }

    @Test
    public void testGetDataQueue_RelativePathForwardslashCleanedUp() {
        DataQueueService dataQueueService = new DataQueueService();
        DataQueue dataQueue = dataQueueService.getDataQueue("/mydatabase/../../database1");
        dataQueue.add(new DataEntity("{}"));

        assertEquals(dataQueueService.getDataQueue("/mydatabase/../../database1").count(), 1);
        ServiceProvider.getInstance().getApplicationContext().getDatabasePath(FileUtil.removeRelativePath("/mydatabase/../../database1")).delete();
    }

    @Test
    public void testGetDataQueue_RelativePathMixedWorkTheSameWhenNotMatch() {
        DataQueueService dataQueueService = new DataQueueService();
        DataQueue dataQueue = dataQueueService.getDataQueue("/mydatabase\\..\\database1");
        dataQueue.add(new DataEntity("{}"));

        assertEquals(dataQueueService.getDataQueue("/mydatabase/../../database1").count(), 1);
        ServiceProvider.getInstance().getApplicationContext().getDatabasePath(FileUtil.removeRelativePath("/mydatabase/../../database1")).delete();
    }

    @Test
    public void testGetDataQueue_RelativePathMixedWorkTheSameWhenMatch() {
        DataQueueService dataQueueService = new DataQueueService();
        DataQueue dataQueue = dataQueueService.getDataQueue("/mydatabase\\..\\database1");
        dataQueue.add(new DataEntity("{}"));

        assertEquals(dataQueueService.getDataQueue("/mydatabase/../database1").count(), 1);
        ServiceProvider.getInstance().getApplicationContext().getDatabasePath(FileUtil.removeRelativePath("/mydatabase/../database1")).delete();
    }

    private boolean createDataQueue(File dataQueueFile, int numberOfEntities) {
        try {
            if(!dataQueueFile.exists()) {
                dataQueueFile.createNewFile();
            }
            DataQueue dataQueue = new SQLiteDataQueue(dataQueueFile.getPath(), new SQLiteDatabaseHelper());
            while(numberOfEntities > 0) {
                dataQueue.add(new DataEntity("{}"));
                numberOfEntities--;
            }
        } catch (Exception e){
            return false;
        }
        return true;
    }
}
