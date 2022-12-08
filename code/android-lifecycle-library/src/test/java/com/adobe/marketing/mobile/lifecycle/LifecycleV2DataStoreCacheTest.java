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

package com.adobe.marketing.mobile.lifecycle;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.adobe.marketing.mobile.services.NamedCollection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class LifecycleV2DataStoreCacheTest {

    @Mock private NamedCollection dataStore;

    private LifecycleV2DataStoreCache dataStoreCache;
    private long currentTimeMilliseconds;
    private long twoMinutesBeforeCurrentTimeMilliseconds;

    private static final String APP_CLOSE_TIMESTAMP_MILLIS = "v2AppCloseTimestampMillis";
    private static final String APP_START_TIMESTAMP_MILLIS = "v2AppStartTimestampMillis";
    private static final String APP_PAUSE_TIMESTAMP_MILLIS = "v2AppPauseTimestampMillis";

    private static final String APP_START_TIMESTAMP_SEC = "v2AppStartTimestamp";
    private static final String APP_PAUSE_TIMESTAMP_SEC = "v2AppPauseTimestamp";
    private static final String APP_CLOSE_TIMESTAMP_SEC = "v2AppCloseTimestamp";

    @Before
    public void beforeEach() {
        currentTimeMilliseconds = System.currentTimeMillis();
        twoMinutesBeforeCurrentTimeMilliseconds = currentTimeMilliseconds - 120000;
    }

    @Test
    public void testConstructor_getCloseTimestamp_returnsPersistedValuePlusTimeout() {
        long testTimestamp = 1623726101L;
        long expectedCloseTimestamp = testTimestamp + (LifecycleV2Constants.CACHE_TIMEOUT_MILLIS);
        when(dataStore.getLong(eq(APP_CLOSE_TIMESTAMP_MILLIS), anyLong()))
                .thenReturn(testTimestamp);
        dataStoreCache = new LifecycleV2DataStoreCache(dataStore);
        assertEquals(expectedCloseTimestamp, dataStoreCache.getCloseTimestampMillis());
    }

    @Test
    public void testConstructor_getCloseTimestamp_returnZero_whenNotFound() {
        dataStoreCache = new LifecycleV2DataStoreCache(dataStore);
        assertEquals(0, dataStoreCache.getCloseTimestampMillis());
    }

    @Test
    public void testConstructor_getCloseTimestamp_doesNotCrash_whenNullDataStore() {
        dataStoreCache = new LifecycleV2DataStoreCache(null);
    }

    @Test
    public void testSetLastKnownTimestamp_updatesPersistenceImmediately() {
        final long initialTimestamp = 1623726101L;
        final long expectedTimestamp = initialTimestamp + 1000;

        when(dataStore.getLong(eq(APP_CLOSE_TIMESTAMP_MILLIS), anyLong()))
                .thenReturn(initialTimestamp);
        dataStoreCache = new LifecycleV2DataStoreCache(dataStore);

        // test
        dataStoreCache.setLastKnownTimestamp(initialTimestamp + 1000);

        // verify: persisted value updated
        ArgumentCaptor<Long> appCloseTimestampCaptor = ArgumentCaptor.forClass(Long.class);
        verify(dataStore, times(1))
                .setLong(eq(APP_CLOSE_TIMESTAMP_MILLIS), appCloseTimestampCaptor.capture());
        assertEquals(expectedTimestamp, appCloseTimestampCaptor.getValue().longValue());
    }

    @Test
    public void testSetLastKnownTimestamp_updatePersistence_multipleUpdates() {
        final long initialTimestamp = 1623726101L;
        final int updateTimes = 5;
        final long expectedTimestamp = initialTimestamp + 4000;

        dataStoreCache = new LifecycleV2DataStoreCache(dataStore);

        // test
        // Each consecutive update adds 1 sec to timestamp, so 5 update times will add 4 seconds
        setLastKnownTimestampConsecutiveTs(initialTimestamp, updateTimes);

        // verify: persisted value updated 3 times, latest value initialTimestamp + 4
        ArgumentCaptor<Long> appCloseTimestampCaptor = ArgumentCaptor.forClass(Long.class);
        verify(dataStore, times(3))
                .setLong(eq(APP_CLOSE_TIMESTAMP_MILLIS), appCloseTimestampCaptor.capture());
        assertEquals(expectedTimestamp, appCloseTimestampCaptor.getAllValues().get(2).longValue());
    }

    @Test
    public void testSetLastKnownTimestamp_doesNotUpdatePersistence_whenSameTimestamp() {
        final long initialTimestamp = 1623726102L;
        final int updateTimes = 10;
        final long expectedTimestamp = initialTimestamp + 1000;

        when(dataStore.getLong(eq(APP_CLOSE_TIMESTAMP_MILLIS), anyLong()))
                .thenReturn(initialTimestamp);
        dataStoreCache = new LifecycleV2DataStoreCache(dataStore);

        // test
        setLastKnownTimestampConsecutiveTs(initialTimestamp + 1000, 1);

        // verify: persisted value updated
        ArgumentCaptor<Long> appCloseTimestampCaptor = ArgumentCaptor.forClass(Long.class);
        verify(dataStore, times(1))
                .setLong(eq(APP_CLOSE_TIMESTAMP_MILLIS), appCloseTimestampCaptor.capture());
        assertEquals(expectedTimestamp, appCloseTimestampCaptor.getValue().longValue());

        when(dataStore.getLong(eq(APP_CLOSE_TIMESTAMP_MILLIS), anyLong())).thenReturn(0L);
        setLastKnownTimestampMultipleTimes();

        // verify: persistence not updated again
        ArgumentCaptor<Long> appCloseTimestampCaptor2 = ArgumentCaptor.forClass(Long.class);
        verify(dataStore, times(1))
                .setLong(eq(APP_CLOSE_TIMESTAMP_MILLIS), appCloseTimestampCaptor2.capture());
        assertEquals(expectedTimestamp, appCloseTimestampCaptor2.getValue().longValue());
    }

    @Test
    public void testSetLastKnownTimestamp_doesNotCrash_whenNullDataStore() {
        dataStoreCache = new LifecycleV2DataStoreCache(null);
        dataStoreCache.setLastKnownTimestamp(currentTimeMilliseconds);
    }

    @Test
    public void testSetGetStartTimestamp_updatesValueInPersistence() {
        when(dataStore.getLong(eq(APP_START_TIMESTAMP_MILLIS), anyLong()))
                .thenReturn(twoMinutesBeforeCurrentTimeMilliseconds);

        dataStoreCache = new LifecycleV2DataStoreCache(dataStore);
        assertEquals(
                twoMinutesBeforeCurrentTimeMilliseconds,
                dataStoreCache.getAppStartTimestampMillis());

        dataStoreCache.setAppStartTimestamp(currentTimeMilliseconds);
        ArgumentCaptor<Long> appStartTimestampCaptor = ArgumentCaptor.forClass(Long.class);
        verify(dataStore, times(1))
                .setLong(eq(APP_START_TIMESTAMP_MILLIS), appStartTimestampCaptor.capture());
        assertEquals(currentTimeMilliseconds, appStartTimestampCaptor.getValue().longValue());
    }

    @Test
    public void testSetGetStartTimestamp_doesNotCrash_whenNullDataStore() {
        dataStoreCache = new LifecycleV2DataStoreCache(null);
        assertEquals(0, dataStoreCache.getAppStartTimestampMillis());
        dataStoreCache.setAppStartTimestamp(currentTimeMilliseconds);
    }

    @Test
    public void testSetGetPauseTimestamp_updatesValueInPersistence() {
        when(dataStore.getLong(eq(APP_PAUSE_TIMESTAMP_MILLIS), anyLong()))
                .thenReturn(twoMinutesBeforeCurrentTimeMilliseconds);
        dataStoreCache = new LifecycleV2DataStoreCache(dataStore);
        assertEquals(
                twoMinutesBeforeCurrentTimeMilliseconds,
                dataStoreCache.getAppPauseTimestampMillis());

        dataStoreCache.setAppPauseTimestamp(currentTimeMilliseconds);
        ArgumentCaptor<Long> appPauseTimestampCaptor = ArgumentCaptor.forClass(Long.class);
        verify(dataStore, times(1))
                .setLong(eq(APP_PAUSE_TIMESTAMP_MILLIS), appPauseTimestampCaptor.capture());
        assertEquals(currentTimeMilliseconds, appPauseTimestampCaptor.getValue().longValue());
    }

    @Test
    public void testSetGetPauseTimestamp_doesNotCrash_whenNullDataStore() {
        dataStoreCache = new LifecycleV2DataStoreCache(null);
        assertEquals(0, dataStoreCache.getAppPauseTimestampMillis());
        dataStoreCache.setAppPauseTimestamp(currentTimeMilliseconds);
    }

    @Test
    public void testMigrateTimestampSecToMillis_migratesTimestampsOnConstruction() {
        long timeSeconds = 1623726101L;
        when(dataStore.getLong(eq(APP_PAUSE_TIMESTAMP_SEC), anyLong())).thenReturn(timeSeconds);
        when(dataStore.contains(eq(APP_PAUSE_TIMESTAMP_SEC))).thenReturn(true);
        when(dataStore.getLong(eq(APP_START_TIMESTAMP_SEC), anyLong())).thenReturn(timeSeconds);
        when(dataStore.contains(eq(APP_START_TIMESTAMP_SEC))).thenReturn(true);
        when(dataStore.getLong(eq(APP_CLOSE_TIMESTAMP_SEC), anyLong())).thenReturn(timeSeconds);
        when(dataStore.contains(eq(APP_CLOSE_TIMESTAMP_SEC))).thenReturn(true);

        dataStoreCache = new LifecycleV2DataStoreCache(dataStore);

        // Validate data store values migrated
        long timeMillis = 1623726101000L;
        ArgumentCaptor<Long> appPauseTimestampCaptor = ArgumentCaptor.forClass(Long.class);
        verify(dataStore, times(1))
                .setLong(eq(APP_PAUSE_TIMESTAMP_MILLIS), appPauseTimestampCaptor.capture());
        assertEquals(timeMillis, appPauseTimestampCaptor.getValue().longValue());

        ArgumentCaptor<Long> appStartTimestampCaptor = ArgumentCaptor.forClass(Long.class);
        verify(dataStore, times(1))
                .setLong(eq(APP_START_TIMESTAMP_MILLIS), appStartTimestampCaptor.capture());
        assertEquals(timeMillis, appStartTimestampCaptor.getValue().longValue());

        ArgumentCaptor<Long> appCloseTimestampCaptor = ArgumentCaptor.forClass(Long.class);
        verify(dataStore, times(1))
                .setLong(eq(APP_CLOSE_TIMESTAMP_MILLIS), appCloseTimestampCaptor.capture());
        assertEquals(timeMillis, appCloseTimestampCaptor.getValue().longValue());

        // Validate data store keys for seconds are removed
        verify(dataStore, times(1)).remove(APP_PAUSE_TIMESTAMP_SEC);
        verify(dataStore, times(1)).remove(APP_START_TIMESTAMP_SEC);
        verify(dataStore, times(1)).remove(APP_CLOSE_TIMESTAMP_SEC);
    }

    private void setLastKnownTimestampConsecutiveTs(final long startingTimestamp, final int times) {
        if (times <= 0 || startingTimestamp <= 0) {
            return;
        }

        long timestamp = startingTimestamp;

        for (int i = 0; i < times; i++) {
            dataStoreCache.setLastKnownTimestamp(timestamp);
            timestamp += 1000;
        }
    }

    private void setLastKnownTimestampMultipleTimes() {
        for (int i = 0; i < 10; i++) {
            dataStoreCache.setLastKnownTimestamp(1623727102);
        }
    }
}
