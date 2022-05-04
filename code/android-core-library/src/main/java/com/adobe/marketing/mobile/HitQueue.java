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

import com.adobe.marketing.mobile.DatabaseService.QueryResult;
import com.adobe.marketing.mobile.internal.context.App;
import com.adobe.marketing.mobile.services.utility.FileUtil;

import java.io.File;
import java.util.Map;

/**
 * Provides basic interactions with a database table.
 *
 * @param <T> class extending {@link AbstractHit} which represents a record in the database table
 * @param <E> class extending {@link AbstractHitSchema} which contains the definition of the database table
 */
class HitQueue<T extends AbstractHit, E extends AbstractHitSchema<T>> extends AbstractHitsDatabase {
	private static final int DEFAULT_NETWORK_CONNECTION_FAIL_DELAY_TIME = 30;
	private static final int ONE_SECOND = 1000;
	private static final String LOG_TAG = HitQueue.class.getSimpleName();
	private final Object backgroundMutex = new Object();
	private final SystemInfoService systemInfoService;
	private final IHitProcessor<T> hitProcessor;
	private final E hitSchema;
	private volatile boolean bgThreadActive;
	private volatile boolean isSuspended;

	/**
	 * Used to communicate from the {@link IHitProcessor} to the {@link HitQueue} whether the processed
	 * {@link AbstractHit} should be removed from the queue
	 */
	enum RetryType {
		NO,
		YES,
		WAIT,
	}

	/**
	 * Constructor calls {@link AbstractHitsDatabase#AbstractHitsDatabase(DatabaseService, String, String)} then calls
	 * {@link #openOrCreateDatabase()} after initializing the following fields:
	 * <ul>
	 *     <li>{@link #bgThreadActive}</li>
	 *     <li>{@link #isSuspended}</li>
	 *     <li>{@link #systemInfoService}</li>
	 *     <li>{@link #hitSchema}</li>
	 *     <li>{@link #hitProcessor}</li>
	 * </ul>
	 *
	 * @param services     the {@code PlatformServices} instance
	 * @param dbFilePath   the {@code String} representing the path of the underlying database
	 * @param tableName    {@code String} containing the database table name
	 * @param hitSchema    {@code AbstractHitSchema} containing the database table definition
	 * @param hitProcessor object implementing the {@code IHitProcessor} responsible for processing hits
	 */
	HitQueue(final PlatformServices services, final String dbFilePath, final String tableName,
			 final E hitSchema, final IHitProcessor<T> hitProcessor) {
		super(services.getDatabaseService(), dbFilePath, tableName);
		bgThreadActive = false;
		isSuspended = false;
		this.systemInfoService = services.getSystemInfoService();
		this.hitSchema = hitSchema;
		this.hitProcessor = hitProcessor;
		openOrCreateDatabase();
	}

	/**
	 * Indicates to the {@link HitQueue} that it should resume processing of queued hits.
	 */
	void bringOnline() {
		this.isSuspended = false;

		if (!bgThreadActive) {
			// indicate that the background thread is now active
			bgThreadActive = true;

			synchronized (backgroundMutex) {
				new Thread(workerThread(), "ADBMobileBackgroundThread").start();
			}
		}
	}

	/**
	 * Creates the table specified by {@link #tableName} in the underlying {@link #database}.
	 */
	@Override
	void initializeDatabase() {
		synchronized (dbMutex) {
			boolean isSuccess = database.createTable(tableName, hitSchema.getColumnNames(), hitSchema.getColumnDataTypes(),
								hitSchema.getColumnConstraints());

			if (!isSuccess) {
				Log.warning(LOG_TAG, "Unable to initialize the database properly, table name (%s)", tableName);
			}
		}
	}

	/**
	 * Queries the {@link #database} and returns the first matching record.
	 *
	 * @param query {@code Query} defining the database query
	 * @return {@link AbstractHit} that represents the first hit matched by the {@code query}
	 */
	T queryHit(final Query query) {
		synchronized (dbMutex) {

			if (this.database == null || this.databaseStatus == DatabaseStatus.FATAL_ERROR) {
				Log.warning(LOG_TAG, "Update hit operation failed due to database error");
				return null;
			}

			T hit = null;
			QueryResult queryResult = null;

			try {
				queryResult = database.query(query);

				if (queryResult != null && queryResult.getCount() > 0 && queryResult.moveToFirst()) {
					hit = hitSchema.generateHit(queryResult);
				}
			} catch (Exception e) {
				Log.error(LOG_TAG, "Unable to read from database. Query failed with error %s", e);
			} finally {
				if (queryResult != null) {
					queryResult.close();
				}
			}

			return hit;
		}
	}

	/**
	 * Inserts the provided {@link AbstractHit} into the {@link #database}.
	 *
	 * @param hit {@code AbstractHit} to be queued
	 * @return {@code boolean} indicating whether the database insert was successful
	 */
	protected boolean queue(final T hit) {
		if (hit == null) {
			Log.debug(LOG_TAG, "Ignoring null hit");
			return false;
		}

		synchronized (dbMutex) {
			if (this.database == null || this.databaseStatus == DatabaseStatus.FATAL_ERROR) {
				Log.warning(LOG_TAG, "Ignoring hit due to database error");
				return false;
			}

			if (!database.insert(tableName, hitSchema.generateDataMap(hit))) {
				Log.warning(LOG_TAG, "A database error occurred preventing a hit from being inserted");
				reset();
				return false;
			} else {
				Log.trace(LOG_TAG, "Hit queued (%s)", hit.getClass().toString());
			}
		}

		return true;
	}

	/**
	 * Select the oldest hit from the {@link #database}.
	 * <p>
	 * Returns null if there are no hits in the database or if the query operation failed. If an error occurred,
	 * an error message will be logged.
	 *
	 * @return the {@link AbstractHit} object representing the oldest record in the {@code database}
	 */
	T selectOldestHit() {
		final Query.Builder queryBuilder = new Query.Builder(tableName, hitSchema.getColumnNames());
		queryBuilder.orderBy("ID ASC");
		queryBuilder.limit("1");
		return queryHit(queryBuilder.build());
	}

	/**
	 * Suspends the queue.  New hits can still be queued, but processing will not be resumed until
	 * {@link #bringOnline()} is called.
	 */
	void suspend() {
		isSuspended = true;
	}

	/**
	 * Returns the status of suspended queue processing.
	 *
	 * @return {@code boolean} indicated if the queue processing is suspended.
	 */
	boolean isSuspended() {
		return isSuspended;
	}

	/**
	 * Updates all hits in the {@link #database} table with new values provided.
	 *
	 * @param parameters {@code Map<String, Object>} of column names and values to be updated in the database table
	 * @return {@code boolean} indicating whether the update operation was successful
	 */
	boolean updateAllHits(final Map<String, Object> parameters) {
		synchronized (dbMutex) {
			if (this.database == null || this.databaseStatus == DatabaseStatus.FATAL_ERROR) {
				Log.warning(LOG_TAG, "Update hits operation failed due to database error");
				return false;
			}

			// make sure we have records in the database that need to be updated to prevent false positive database errors
			if (getSize() <= 0) {
				return true;
			}

			if (!database.update(tableName, parameters, null, null)) {
				Log.warning(LOG_TAG, "An error occurred updating database. Resetting database.");
				reset();
				return false;
			}

			return true;
		}
	}

	/**
	 * Updates the hit in the {@link #database}.
	 *
	 * @param hit {@link AbstractHit} containing the hit to be updated in the database
	 * @return {@code boolean} indicating whether the update operation was successful
	 */
	boolean updateHit(final T hit) {
		if (StringUtils.isNullOrEmpty(hit.identifier)) {
			Log.warning(LOG_TAG, "Unable to update hit with empty identifier");
			return false;
		}

		synchronized (dbMutex) {
			if (this.database == null || this.databaseStatus == DatabaseStatus.FATAL_ERROR) {
				Log.warning(LOG_TAG, "Update hit operation failed due to database error");
				return false;
			}

			if (!database.update(tableName, hitSchema.generateDataMap(hit), "ID = ?", new String[] {hit.identifier})) {
				Log.warning(LOG_TAG, "Unable to update hit in database");
				return false;
			}

			return true;
		}
	}

	/**
	 * Loops through the table in the {@link #database} and causes the {@link #hitProcessor} to process them, in FIFO order.
	 * <p>
	 * The processing loop will terminate under the following conditions:
	 * <ul>
	 *     <li>There are no hits left in the table</li>
	 *     <li>The {@code hitProcessor} is null</li>
	 *     <li>After processing, we attempt to delete the processed hit and fail</li>
	 * </ul>
	 * <p>
	 * If the {@code hitProcessor} indicates that we should attempt to re-process a hit, the loop will sleep for
	 * 30 seconds prior to resuming.
	 *
	 * @return a {@link Runnable} object to be dispatched by the caller
	 */
	private Runnable workerThread() {
		return new Runnable() {
			@Override
			public void run() {
				// loop while privacy status is opt-in, we have network, and we are not waiting on referrerData
				while (!isSuspended && systemInfoService != null
						&& SystemInfoService.ConnectionStatus.CONNECTED == systemInfoService.getNetworkConnectionStatus()) {
					// pull the top hit from the database
					final T hit = selectOldestHit();

					// if we do not have a hit, break out of this loop
					if (hit == null || hitProcessor == null) {
						break;
					}

					final RetryType result = hitProcessor.process(hit);

					if (result == RetryType.YES) {
						delayNetworkAttempts();
					} else if (result == RetryType.NO) {
						if (!deleteHitWithIdentifier(hit.identifier)) {
							break;
						}
					} else {
						break;
					}
				}

				// indicate that the background thread is no longer active
				bgThreadActive = false;
			}
		};
	}

	/**
	 * Sleeps the current thread in one second intervals for {@link #DEFAULT_NETWORK_CONNECTION_FAIL_DELAY_TIME} iterations.
	 * <p>
	 * This method is called from {@link #workerThread()} and is a soft delay used between failed network calls.
	 */
	private void delayNetworkAttempts() {
		try {
			// pause for delay interval as long as the network remains available.
			for (int i = 0; i < DEFAULT_NETWORK_CONNECTION_FAIL_DELAY_TIME
					&& SystemInfoService.ConnectionStatus.CONNECTED == systemInfoService.getNetworkConnectionStatus(); i++) {
				Thread.sleep(ONE_SECOND);
			}
		} catch (final Exception e) {
			Log.debug(LOG_TAG, "Background Thread Interrupted (%s)", e);
		}
	}

	/**
	 * Defines the interface providing the {@link HitQueue} instance with a way to process hits from its {@link #database}.
	 *
	 * @param <T> A class extending {@link AbstractHit}
	 */
	interface IHitProcessor<T extends AbstractHit> {
		/**
		 * Called when a hit retrieved from the {@link #database} needs to be processed.
		 * <p>
		 * The return value of this method determines whether the processed hit will be removed from the database.
		 *
		 * @param hit {@link AbstractHit} retrieved from the database
		 * @return {@link RetryType} indicating whether the hit should be re-processed
		 */
		RetryType process(T hit);
	}

	static boolean migrate(final File src, final String newDBName){
		if(src == null) {
			Log.warning(LOG_TAG, "Failed to copy database to database folder. Source file is null");
			return false;
		}
		if(!src.exists()){
			Log.warning(LOG_TAG, "Failed to copy database to database folder. Source file (%s) does not exist", src.getAbsolutePath());
			return false;
		}
		if(StringUtils.isNullOrEmpty(newDBName)) {
			Log.warning(LOG_TAG, "Failed to copy database to database folder. New database name is null or empty");
			return false;
		}

		final int STREAM_READ_BUFFER_SIZE = 1024;
		final File newHitQueueFile = App.getInstance().getAppContext().getDatabasePath(newDBName);

		try {
			newHitQueueFile.createNewFile();
			FileUtil.copyFile(src, newHitQueueFile);
			return true;
		}  catch (Exception e) {
			Log.debug(LOG_TAG, "Failed to copy database (%s) to database folder", src.getAbsolutePath());
			return false;
		}
	}
}