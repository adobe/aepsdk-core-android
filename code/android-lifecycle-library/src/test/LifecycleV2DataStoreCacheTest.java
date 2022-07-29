/* **************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2021 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 *
 * *************************************************************************/

package com.adobe.marketing.mobile;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class LifecycleV2DataStoreCacheTest extends BaseTest {
	private FakeDataStore    dataStore;
	private LifecycleV2DataStoreCache dataStoreCache;
	private long currentTimeMilliseconds;
	private long twoMinutesBeforeCurrentTimeMilliseconds;

	@Before
	public void beforeEach() {
		FakePlatformServices platformServices = new FakePlatformServices();
		FakeLocalStorageService localStorageService = (FakeLocalStorageService) platformServices
				.getLocalStorageService();
		dataStore = (FakeDataStore) localStorageService.getDataStore("lifecycle");
		currentTimeMilliseconds = System.currentTimeMillis();
		twoMinutesBeforeCurrentTimeMilliseconds = currentTimeMilliseconds - 120000;
	}

	@Test
	public void testConstructor_getCloseTimestamp_returnsPersistedValuePlusTimeout() {
		long testTimestamp = 1623726101L;
		long expectedCloseTimestamp = testTimestamp + (LifecycleV2Constants.CACHE_TIMEOUT_MILLIS);
		dataStore.setLong(LifecycleV2Constants.DataStoreKeys.APP_CLOSE_TIMESTAMP_MILLIS, testTimestamp);
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

		dataStore.setLong(LifecycleV2Constants.DataStoreKeys.APP_CLOSE_TIMESTAMP_MILLIS, initialTimestamp);
		dataStoreCache = new LifecycleV2DataStoreCache(dataStore);

		// test
		dataStoreCache.setLastKnownTimestamp(initialTimestamp + 1000);

		// verify: persisted value updated
		assertEquals(expectedTimestamp, dataStore.getLong(LifecycleV2Constants.DataStoreKeys.APP_CLOSE_TIMESTAMP_MILLIS, 0L));
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
		assertEquals(expectedTimestamp, dataStore.getLong(LifecycleV2Constants.DataStoreKeys.APP_CLOSE_TIMESTAMP_MILLIS, 0L));
	}

	@Test
	public void testSetLastKnownTimestamp_doesNotUpdatePersistence_whenSameTimestamp() {
		final long initialTimestamp = 1623726102L;
		final int updateTimes = 10;
		final long expectedTimestamp = initialTimestamp + 1000;

		dataStore.setLong(LifecycleV2Constants.DataStoreKeys.APP_CLOSE_TIMESTAMP_MILLIS, initialTimestamp);
		dataStoreCache = new LifecycleV2DataStoreCache(dataStore);

		// test
		setLastKnownTimestampConsecutiveTs(initialTimestamp + 1000, 1);

		// verify: persisted value updated
		assertEquals(expectedTimestamp, dataStore.getLong(LifecycleV2Constants.DataStoreKeys.APP_CLOSE_TIMESTAMP_MILLIS, 0L));

		dataStore.remove(LifecycleV2Constants.DataStoreKeys.APP_CLOSE_TIMESTAMP_MILLIS);
		setLastKnownTimestampMultipleTimes(initialTimestamp + 1000, updateTimes);

		// verify: persistence not updated again
		assertEquals(0, dataStore.getLong(LifecycleV2Constants.DataStoreKeys.APP_CLOSE_TIMESTAMP_MILLIS, 0L));
	}

	@Test
	public void testSetLastKnownTimestamp_doesNotCrash_whenNullDataStore() {
		dataStoreCache = new LifecycleV2DataStoreCache(null);
		dataStoreCache.setLastKnownTimestamp(currentTimeMilliseconds);
	}

	@Test
	public void testSetGetStartTimestamp_updatesValueInPersistence() {
		dataStore.setLong(LifecycleV2Constants.DataStoreKeys.APP_START_TIMESTAMP_MILLIS,
						  twoMinutesBeforeCurrentTimeMilliseconds);
		dataStoreCache = new LifecycleV2DataStoreCache(dataStore);
		assertEquals(twoMinutesBeforeCurrentTimeMilliseconds, dataStoreCache.getAppStartTimestampMillis());

		dataStoreCache.setAppStartTimestamp(currentTimeMilliseconds);
		assertEquals(currentTimeMilliseconds, dataStore.getLong(LifecycleV2Constants.DataStoreKeys.APP_START_TIMESTAMP_MILLIS,
					 0));
		assertEquals(currentTimeMilliseconds, dataStoreCache.getAppStartTimestampMillis());
	}

	@Test
	public void testSetGetStartTimestamp_doesNotCrash_whenNullDataStore() {
		dataStoreCache = new LifecycleV2DataStoreCache(null);
		assertEquals(0, dataStoreCache.getAppStartTimestampMillis());
		dataStoreCache.setAppStartTimestamp(currentTimeMilliseconds);
	}

	@Test
	public void testSetGetPauseTimestamp_updatesValueInPersistence() {
		dataStore.setLong(LifecycleV2Constants.DataStoreKeys.APP_PAUSE_TIMESTAMP_MILLIS,
						  twoMinutesBeforeCurrentTimeMilliseconds);
		dataStoreCache = new LifecycleV2DataStoreCache(dataStore);
		assertEquals(twoMinutesBeforeCurrentTimeMilliseconds, dataStoreCache.getAppPauseTimestampMillis());

		dataStoreCache.setAppPauseTimestamp(currentTimeMilliseconds);
		assertEquals(currentTimeMilliseconds, dataStore.getLong(LifecycleV2Constants.DataStoreKeys.APP_PAUSE_TIMESTAMP_MILLIS,
					 0));
		assertEquals(currentTimeMilliseconds, dataStoreCache.getAppPauseTimestampMillis());
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
		dataStore.setLong(LifecycleV2Constants.DataStoreKeys.APP_START_TIMESTAMP_SEC, timeSeconds);
		dataStore.setLong(LifecycleV2Constants.DataStoreKeys.APP_PAUSE_TIMESTAMP_SEC, timeSeconds);
		dataStore.setLong(LifecycleV2Constants.DataStoreKeys.APP_CLOSE_TIMESTAMP_SEC, timeSeconds);

		dataStoreCache = new LifecycleV2DataStoreCache(dataStore);

		// Validate data store values migrated
		long timeMillis = 1623726101000L;
		assertEquals(timeMillis, dataStore.getLong(LifecycleV2Constants.DataStoreKeys.APP_START_TIMESTAMP_MILLIS, 0));
		assertEquals(timeMillis, dataStore.getLong(LifecycleV2Constants.DataStoreKeys.APP_PAUSE_TIMESTAMP_MILLIS, 0));
		assertEquals(timeMillis, dataStore.getLong(LifecycleV2Constants.DataStoreKeys.APP_CLOSE_TIMESTAMP_MILLIS, 0));

		// Validate data store keys for seconds are removed
		assertFalse(dataStore.contains(LifecycleV2Constants.DataStoreKeys.APP_START_TIMESTAMP_SEC));
		assertFalse(dataStore.contains(LifecycleV2Constants.DataStoreKeys.APP_PAUSE_TIMESTAMP_SEC));
		assertFalse(dataStore.contains(LifecycleV2Constants.DataStoreKeys.APP_CLOSE_TIMESTAMP_SEC));

		// Validate cache returns correct values
		assertEquals(timeMillis, dataStoreCache.getAppStartTimestampMillis());
		assertEquals(timeMillis, dataStoreCache.getAppPauseTimestampMillis());
		assertEquals(timeMillis + LifecycleV2Constants.CACHE_TIMEOUT_MILLIS, dataStoreCache.getCloseTimestampMillis());
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

	private void setLastKnownTimestampMultipleTimes(final long timestamp, final int times) {
		if (times <= 0 || timestamp <= 0) {
			return;
		}

		for (int i = 0; i < times; i++) {
			dataStoreCache.setLastKnownTimestamp(timestamp);
		}
	}

}
