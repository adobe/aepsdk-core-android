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

import android.content.ContentValues;

import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SQLite backed implementation of {@link DataQueue}.
 */
final class SQLiteDataQueue implements DataQueue {

	private static final String TABLE_NAME = "TB_AEP_DATA_ENTITY";
	private static final String TB_KEY_UNIQUE_IDENTIFIER = "uniqueIdentifier";
	private static final String TB_KEY_TIMESTAMP = "timestamp";
	private static final String TB_KEY_DATA = "data";
	private static final String LOG_PREFIX = "SQLiteDataQueue";

	private final String databasePath;
	private final SQLiteDatabaseHelper databaseHelper;
	private boolean isClose = false;
	private final Object dbMutex = new Object();

	SQLiteDataQueue(final File filesDir, final String databaseName, final SQLiteDatabaseHelper databaseHelper) {
		this.databaseHelper = databaseHelper;
		this.databasePath = new File(filesDir, removeRelativePath(databaseName)).getPath();
		createTableIfNotExists();
	}

	@Override
	public boolean add(final DataEntity dataEntity) {
		if (isClose) {
			MobileCore.log(LoggingMode.DEBUG, LOG_PREFIX, "add - Returning false, DataQueue is closed.");
			return false;
		}

		if (dataEntity == null) {
			MobileCore.log(LoggingMode.DEBUG, LOG_PREFIX, "add - Returning false, DataEntity is null.");
			return false;
		}

		Map<String, Object> dataToInsert = new HashMap<>();
		dataToInsert.put(TB_KEY_UNIQUE_IDENTIFIER, dataEntity.getUniqueIdentifier());
		dataToInsert.put(TB_KEY_TIMESTAMP, dataEntity.getTimestamp().getTime());
		dataToInsert.put(TB_KEY_DATA, dataEntity.getData() != null ? dataEntity.getData() : "");

		synchronized (dbMutex) {
			boolean result = databaseHelper.insertRow(databasePath, TABLE_NAME, dataToInsert);
			MobileCore.log(LoggingMode.VERBOSE, LOG_PREFIX, String.format("add - Successfully added DataEntity (%s) to DataQueue",
						   dataEntity.toString()));
			return result;
		}
	}

	@Override
	public List<DataEntity> peek(final int n) {
		if (isClose) {
			MobileCore.log(LoggingMode.DEBUG, LOG_PREFIX, "peek n - Returning null, DataQueue is closed.");
			return null;
		}

		if (n <= 0) {
			MobileCore.log(LoggingMode.DEBUG, LOG_PREFIX, "peek n - Returning null, n <= 0.");
			return null;
		}

		List<ContentValues> rows;

		synchronized (dbMutex) {
			rows = databaseHelper.query(databasePath, TABLE_NAME, new String[] {TB_KEY_TIMESTAMP, TB_KEY_UNIQUE_IDENTIFIER, TB_KEY_DATA},
										n);
		}

		if (rows == null || rows.isEmpty()) {
			return new ArrayList<>();
		}

		final List<DataEntity> dataEntitiesList = new ArrayList<>(rows.size());

		for (ContentValues row : rows) {
			dataEntitiesList.add(new DataEntity(
									 row.getAsString(TB_KEY_UNIQUE_IDENTIFIER),
									 new Date(row.getAsLong(TB_KEY_TIMESTAMP)),
									 row.getAsString(TB_KEY_DATA)
								 ));
		}

		MobileCore.log(LoggingMode.VERBOSE, LOG_PREFIX, String.format("peek n - Successfully returned %d DataEntities",
					   dataEntitiesList.size()));
		return dataEntitiesList;
	}

	@Override
	public DataEntity peek() {
		if (isClose) {
			MobileCore.log(LoggingMode.DEBUG, LOG_PREFIX, "peek - Returning null, DataQueue is closed");
			return null;
		}

		final List<DataEntity> dataEntities = peek(1);

		if (dataEntities == null) {
			MobileCore.log(LoggingMode.DEBUG, LOG_PREFIX, "peek - Unable to fetch DataEntity, returning null");
			return null;
		}

		if (dataEntities.isEmpty()) {
			MobileCore.log(LoggingMode.DEBUG, LOG_PREFIX, "peek - 0 DataEntities fetch, returning null");
			return null;
		}

		MobileCore.log(LoggingMode.VERBOSE, LOG_PREFIX, String.format("peek - Successfully returned DataEntity (%s)",
					   dataEntities.get(0).toString()));
		return dataEntities.get(0);
	}

	@Override
	public boolean remove(final int n) {
		if (isClose) {
			MobileCore.log(LoggingMode.DEBUG, LOG_PREFIX, "remove n - Returning false, DataQueue is closed");
			return false;
		}

		if (n <= 0) {
			MobileCore.log(LoggingMode.DEBUG, LOG_PREFIX, "remove n - Returning false, n <= 0");
			return false;
		}

		synchronized (dbMutex) {
			int count = databaseHelper.removeRows(databasePath, TABLE_NAME, "id ASC", n);
			MobileCore.log(LoggingMode.VERBOSE, LOG_PREFIX, String.format("remove n - Successfully removed %d DataEntities",
						   count));
			return count != -1;
		}
	}

	@Override
	public boolean remove() {
		if (isClose) {
			MobileCore.log(LoggingMode.DEBUG, LOG_PREFIX, "remove - Returning false, DataQueue is closed");
			return false;
		}

		return remove(1);
	}

	@Override
	public boolean clear() {
		if (isClose) {
			MobileCore.log(LoggingMode.DEBUG, LOG_PREFIX, "clear - Returning false, DataQueue is closed");
			return false;
		}

		synchronized (dbMutex) {
			boolean result = databaseHelper.clearTable(databasePath, TABLE_NAME);
			MobileCore.log(LoggingMode.VERBOSE, LOG_PREFIX, String.format("clear - %s in clearing Table %s",
						   (result ? "Successful" : "Failed"), TABLE_NAME));
			return result;
		}
	}

	@Override
	public int count() {
		if (isClose) {
			MobileCore.log(LoggingMode.DEBUG, LOG_PREFIX, "count - Returning 0, DataQueue is closed");
			return 0;
		}

		synchronized (dbMutex) {
			return databaseHelper.getTableSize(databasePath, TABLE_NAME);
		}
	}

	@Override
	public void close() {
		isClose = true;
	}

	/**
	 * Creates a Table with name {@link #TABLE_NAME}, if not already exists in database at path {@link #databasePath}.
	 */
	private void createTableIfNotExists() {
		if (databaseHelper == null) {
			MobileCore.log(LoggingMode.DEBUG, LOG_PREFIX, String.format("Unable to create table (%s), database helper is null",
						   TABLE_NAME));
			return;
		}

		final String tableCreationQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
										  " (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, " +
										  "uniqueIdentifier TEXT NOT NULL UNIQUE, " +
										  "timestamp INTEGER NOT NULL, " +
										  "data TEXT);";

		synchronized (dbMutex) {
			if (databaseHelper.createTableIfNotExist(databasePath, tableCreationQuery)) {
				MobileCore.log(LoggingMode.VERBOSE, LOG_PREFIX,
							   String.format("createTableIfNotExists - Successfully created/already existed table (%s) ", TABLE_NAME));
				return;
			}
		}

		MobileCore.log(LoggingMode.DEBUG, LOG_PREFIX,
					   String.format("createTableIfNotExists - Error creating/accessing table (%s)  ", TABLE_NAME));
	}

	/**
	 * Removes the relative part of the file name(if exists).
	 * <p>
	 * for ex: File name `/mydatabase/../../database1` will be converted to `mydatabase_database1`
	 * <p/>
	 *
	 * @param filePath the file name
	 * @return file name without relative path
	 */
	private String removeRelativePath(final String filePath) {
		if (filePath == null || filePath.isEmpty()) {
			return filePath;
		}

		try {
			String result = filePath.replaceAll("\\.[/\\\\]", "\\.");
			result = result.replaceAll("[/\\\\](\\.{2,})", "_");
			return result;
		} catch (IllegalArgumentException e) {
			return filePath;
		}
	}
}
