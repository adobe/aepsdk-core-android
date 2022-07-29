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

import java.util.concurrent.TimeUnit;

/**
 * Lifecycle DataStore Cache layer for persisting the timestamp values required for Lifecycle session computation in XDM,
 * including close timestamp to be used for app close time approximation, start and pause timestamps.
 * This class is not thread safe, so it should be used in a thread safe setup.
 */
class LifecycleV2DataStoreCache {
	private static final String SELF_LOG_TAG = "LifecycleV2DataStoreCache";
	private final LocalStorageService.DataStore dataStore;
	private final long closeTimestampMillis; // close timestamp at initialization time (persisted in the previous session)
	// used to keep track of the last persisted value to optimize the commits to persistence
	private long lastClosePersistedValue;

	/**
	 * Initializes the class and loads the app close timestamp from persistence
	 *
	 * @param dataStore the DataStore used to read/write last known timestamp
	 */
	LifecycleV2DataStoreCache(final LocalStorageService.DataStore dataStore) {
		this.dataStore = dataStore;

		if (dataStore == null) {
			Log.warning(LifecycleConstants.LOG_TAG, "%s - Unexpected null DataStore was provided, the functionality is limited.",
						SELF_LOG_TAG);
			closeTimestampMillis = 0L;
			return;
		}

		// DataStore is set, migrate any old timestamps
		migrateTimestampsSecToMillis();

		final long tempTs = dataStore.getLong(LifecycleV2Constants.DataStoreKeys.APP_CLOSE_TIMESTAMP_MILLIS, 0L);
		this.closeTimestampMillis = tempTs > 0 ? tempTs + LifecycleV2Constants.CACHE_TIMEOUT_MILLIS : tempTs;
	}

	/**
	 * The last known close timestamp value to be updated in cache and, if needed, in persistence as well.
	 * The write will execute after {@link LifecycleV2Constants#CACHE_TIMEOUT_MILLIS} since last update.
	 *
	 * @param timestampMillis current timestamp (milliseconds)
	 */
	void setLastKnownTimestamp(final long timestampMillis) {
		if (dataStore != null && timestampMillis - lastClosePersistedValue >= LifecycleV2Constants.CACHE_TIMEOUT_MILLIS) {
			dataStore.setLong(LifecycleV2Constants.DataStoreKeys.APP_CLOSE_TIMESTAMP_MILLIS, timestampMillis);
			lastClosePersistedValue = timestampMillis;
		}
	}

	/**
	 * Returns the approximated app close timestamp in milliseconds. This value is loaded from persistence when
	 * {@link LifecycleV2DataStoreCache} is initialized and it includes the {@link LifecycleV2Constants#CACHE_TIMEOUT_MILLIS}
	 * for the eventuality when the application was closed before the last commit was executed.
	 *
	 * @return the last known close timestamp value or 0 if not found, for example on first launch
	 */
	long getCloseTimestampMillis() {
		return closeTimestampMillis;
	}

	/**
	 * Updates the last app start timestamp in persistence.
	 *
	 * @param timestampMillis start timestamp (milliseconds)
	 */
	void setAppStartTimestamp(final long timestampMillis) {
		if (dataStore != null) {
			dataStore.setLong(LifecycleV2Constants.DataStoreKeys.APP_START_TIMESTAMP_MILLIS, timestampMillis);
		}
	}

	/**
	 * Reads the last app start timestamp from persistence and returns the value.
	 *
	 * @return app start timestamp (milliseconds) or 0 if not found
	 */
	long getAppStartTimestampMillis() {
		return dataStore != null ? dataStore.getLong(LifecycleV2Constants.DataStoreKeys.APP_START_TIMESTAMP_MILLIS, 0L) : 0L;
	}

	/**
	 * Updates the last app pause timestamp in persistence.
	 *
	 * @param timestampMillis pause timestamp (milliseconds)
	 */
	void setAppPauseTimestamp(final long timestampMillis) {
		if (dataStore != null) {
			dataStore.setLong(LifecycleV2Constants.DataStoreKeys.APP_PAUSE_TIMESTAMP_MILLIS, timestampMillis);
		}
	}

	/**
	 * Reads the last pause timestamp from persistence and returns the value.
	 *
	 * @return app pause timestamp (milliseconds) or 0 if not found
	 */
	long getAppPauseTimestampMillis() {
		return dataStore != null ? dataStore.getLong(LifecycleV2Constants.DataStoreKeys.APP_PAUSE_TIMESTAMP_MILLIS, 0L) : 0L;
	}

	/**
	 * Migrates any timestamps stored in seconds to timestamps stored in milliseconds.
	 * Removes timestamps in seconds from persistent storage.
	 */
	private void migrateTimestampsSecToMillis() {
		migrateDataStoreKey(LifecycleV2Constants.DataStoreKeys.APP_START_TIMESTAMP_SEC,
							LifecycleV2Constants.DataStoreKeys.APP_START_TIMESTAMP_MILLIS);
		migrateDataStoreKey(LifecycleV2Constants.DataStoreKeys.APP_PAUSE_TIMESTAMP_SEC,
							LifecycleV2Constants.DataStoreKeys.APP_PAUSE_TIMESTAMP_MILLIS);
		migrateDataStoreKey(LifecycleV2Constants.DataStoreKeys.APP_CLOSE_TIMESTAMP_SEC,
							LifecycleV2Constants.DataStoreKeys.APP_CLOSE_TIMESTAMP_MILLIS);
	}

	/**
	 * Migrate a single data store key holding a timestamp in seconds to a new key holding the same timestamp in milliseconds.
	 * After migration, {@code keyMilliseconds} is added to the data store while {@code keySeconds} is removed.
	 *
	 * @param keySeconds the data store key name holding a timestamp in seconds
	 * @param keyMilliseconds the data store key name to add holding a timestamp in milliseconds.
	 */
	private void migrateDataStoreKey(final String keySeconds, final String keyMilliseconds) {
		if (dataStore == null) {
			return;
		}

		if (dataStore.contains(keySeconds)) {
			long value = dataStore.getLong(keySeconds, 0L);

			if (value > 0) {
				dataStore.setLong(keyMilliseconds, TimeUnit.SECONDS.toMillis(value));
				Log.trace(LifecycleConstants.LOG_TAG, "%s - Migrated persisted '%s' to '%s'.",
						  SELF_LOG_TAG, keySeconds, keyMilliseconds);
			}

			dataStore.remove(keySeconds);
		}
	}
}
