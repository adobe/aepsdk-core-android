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

import com.adobe.marketing.mobile.internal.eventhub.EventHistoryResultHandler;

import java.util.List;

@SuppressWarnings("unused")
/**
 * Interface defining a database to be used by the SDK for storing event history.
 */
public interface EventHistoryDatabase {

	/**
	 * Opens an {#EventHistoryDatabase} database file if it exists, otherwise creates a new one in the cache directory.
	 *
	 * @return {@link boolean} indicating whether the open database operation was successful
	 */
	boolean openDatabase();

	/**
	 * Delete {@link EventHistoryDatabase} file created in the cache directory, if it exists.
	 *
	 * @return {@code boolean} indicating whether the {@code EventHistoryDatabase} file delete operation was successful
	 */
	boolean deleteDatabase();

	/**
	 * Create a table if it doesn't exist.
	 *
	 * @param columnNames {@code String[]} array containing column names
	 * @param columnDataTypes {@code ColumnDataType[]} array containing data types for each column
	 * @param columnConstraints {@code List<List<ColumnConstraint>>} a list of lists containing column constraints
	 *                          for each table column
	 *
	 * @return {@code boolean} indicating whether the create table operation was successful
	 *
	 * @see {@link DatabaseService.Database.ColumnConstraint}
	 * @see {@link  DatabaseService.Database.ColumnDataType}
	 */
	boolean createTable(final String[] columnNames,
						final DatabaseService.Database.ColumnDataType[] columnDataTypes,
						final List<List<DatabaseService.Database.ColumnConstraint>> columnConstraints);

	/**
	 * Insert a row into a table in the database.
	 *
	 * @param hash {@code long} containing the 32-bit FNV-1a hashed representation of an Event's data
	 * @return a {@code boolean} which will contain the status of the database insert operation
	 */
	boolean insert(final long hash);

	/**
	 * Queries the event history database to search for the existence of an event.
	 * <p>
	 * This method will count all records in the event history database that match the provided hash and are within
	 * the bounds of the provided from and to timestamps.
	 * If the "from" date is equal to 0, the search will use the beginning of event history as the lower bounds of the date range.
	 * If the "to" date is equal to 0, the search will use the current system timestamp as the upper bounds of the date range.
	 * The {@link EventHistoryResultHandler} will be called with a {@link DatabaseService.QueryResult} which contains the number of matching records,
	 * the oldest timestamp, and the newest timestamp for a matching event.
	 * If no database connection is available, the handler will be called with a null {@code DatabaseService.QueryResult}.
	 *
	 * @param hash {@code long} containing the 32-bit FNV-1a hashed representation of an Event's data
	 * @param from {@code long} a timestamp representing the lower bounds of the date range to use when searching for the hash
	 * @param to {@code long} a timestamp representing the upper bounds of the date range to use when searching for the hash
	 * @return a {@code DatabaseService.QueryResult} which will contain the matching events
	 */
	DatabaseService.QueryResult select(final long hash,
									   final long from,
									   final long to);

	/**
	 * Delete entries from the event history database.
	 *
	 * @param hash {@code long} containing the 32-bit FNV-1a hashed representation of an Event's data
	 * @param from {@code long} representing the lower bounds of the date range to use when searching for the hash
	 * @param to {@code long} representing the upper bounds of the date range to use when searching for the hash
	 * @return {@code int} which will contain the number of rows deleted.
	 */
	int delete (final long hash,
				final long from,
				final long to);

	/**
	 * Close this database.
	 */
	void close();
}