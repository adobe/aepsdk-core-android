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

import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
interface DatabaseService {

	/**
	 * Opens a database if it exists, otherwise creates a new one at the specified path.
	 *
	 * @param filePath {@link String} containing the database file path
	 *
	 * @return {@link Database} instance, or null if error occurs
	 *
	 * @throws IllegalArgumentException if the database cannot be opened
	 */
	Database openDatabase(final String filePath);

	/**
	 * Delete database at the specified path, if it exists.
	 *
	 * @param filePath {@link String} containing the database file path
	 *
	 * @return {@code boolean} indicating whether the database file delete operation was successful
	 */
	boolean deleteDatabase(final String filePath);

	/**
	 * Interface defining relational database operations.
	 */
	interface Database {

		/**
		 * Allowed data types for database columns
		 */
		enum ColumnDataType {
			INTEGER,
			REAL,
			TEXT;
		}

		/**
		 * Allowed constraints for database columns
		 */
		enum ColumnConstraint {
			NOT_NULL,
			UNIQUE,
			PRIMARY_KEY,
			AUTOINCREMENT;
		}

		/**
		 * Create a table if it doesn't exist.
		 *
		 * @param name {@link String} containing table name
		 * @param columnNames {@code String[]} array containing column names
		 * @param columnDataTypes {@code ColumnDataType[]} array containing data types for each column
		 * @param columnConstraints {@code List<List<ColumnConstraint>>} a list of lists containing column constraints
		 *                          for each table column
		 *
		 * @return {@code boolean} indicating whether the create table operation was successful
		 *
		 * @see ColumnConstraint
		 * @see ColumnDataType
		 */
		boolean createTable(final String name,
							final String[] columnNames,
							final ColumnDataType[] columnDataTypes,
							final List<List<ColumnConstraint>> columnConstraints);

		/**
		 * Query a table in the database.
		 *
		 * @param query {@link Query} object indicating the query to execute
		 *
		 * @return {@link QueryResult} the result of this query, positioned at the first row
		 */
		QueryResult query(final Query query);

		/**
		 * Insert a row into a table in the database.
		 *
		 * @param table {@link String} containing the table name to insert the row into
		 * @param values {@code Map<String, Object>} map containing the column name value pairs for the row
		 *
		 * @return {@code boolean} indicating whether the insert operation was successful
		 */
		boolean insert(final String table,
					   final Map<String, Object> values);

		/**
		 * Update rows for a table in the database.
		 * <p>
		 * A null {@code whereClause} will cause updation of all table rows.
		 *
		 * @param table {@link String} containing the table name to update rows in
		 * @param values {@code Map<String, Object>} containing column name value pairs
		 * @param whereClause {@code String} indicating the optional WHERE clause to apply when updating
		 * @param whereArgs {@code String[]} array containing values that shall replace ?s in the WHERE clause
		 *
		 * @return {@code boolean} indicating whether the update operation was successful
		 */
		boolean update(final String table,
					   final Map<String, Object> values,
					   final String whereClause,
					   final String[] whereArgs);

		/**
		 * Delete rows for a table in the database.
		 * <p>
		 * A null {@code whereClause} will result in deletion of all table rows.
		 *
		 * @param table {@link String} containing the table name to delete rows from
		 * @param whereClause {@code String} containing the optional WHERE clause to apply when deleting
		 * @param whereArgs {@code String[]} array containing values that shall replace ?s in the WHERE clause
		 *
		 * @return {@code boolean} indicating whether the delete operation was successful
		 */
		boolean delete (final String table,
						final String whereClause,
						final String[] whereArgs);

		/**
		 * Close this database.
		 */
		void close();

	}

	/**
	 * Interface defining methods for reading query results from a Database.
	 */
	interface QueryResult {

		/**
		 * Returns the total number of rows in this {@code QueryResult}.
		 *
		 * @return {@code int} indicating number of rows in the {@link QueryResult}
		 *
		 * @throws Exception if the underlying table does not exist, or if the {@link QueryResult} is already closed
		 */
		int getCount() throws Exception;

		/**
		 * Returns the value of the requested column as an {@code int}.
		 *
		 * @param columnIndex {@code int} zero-based index of the target column
		 *
		 * @return the value of the column as an {@code int}
		 *
		 * @throws Exception if the {@code columnIndex} is out of bounds, or if the {@link QueryResult} is already closed
		 */
		int getInt(final int columnIndex) throws Exception;

		/**
		 * Returns the value of the requested column as a {code double}.
		 *
		 * @param columnIndex {@code int} zero-based index of the target column
		 *
		 * @return the value of the column as a {@code double}
		 *
		 * @throws Exception if the {@code columnIndex} is out of bounds, or if the {@link QueryResult} is already closed
		 */
		double getDouble(final int columnIndex) throws Exception;

		/**
		 * Returns the value of the requested column as a {@code float}.
		 *
		 * @param columnIndex {@code int} zero-based index of the target column
		 *
		 * @return the value of the column as a {@code float}
		 *
		 * @throws Exception if the {@code columnIndex} is out of bounds, or if the {@link QueryResult} is already closed
		 */
		float getFloat(final int columnIndex) throws Exception;

		/**
		 * Returns the value of the requested column as a {@code long}.
		 *
		 * @param columnIndex {@code int} zero-based index of the target column
		 *
		 * @return the value of the column as a {@code long}
		 *
		 * @throws Exception if the {@code columnIndex} is out of bounds, or if the {@link QueryResult} is already closed
		 */
		long getLong(final int columnIndex) throws Exception;

		/**
		 * Returns the value of the requested column as a {@code String}.
		 *
		 * @param columnIndex {@code int} zero-based index of the target column
		 *
		 * @return the value of the column as a {@link String}
		 *
		 * @throws Exception if the {@code columnIndex} is out of bounds, or if the {@link QueryResult} is already closed
		 */
		String getString(final int columnIndex) throws Exception;

		/**
		 * Returns true if the value of the requested column is null, false otherwise.
		 *
		 * @param columnIndex {@code int} zero-based index of the target column
		 *
		 * @return {@code boolean} indicating whether the column value is null
		 *
		 * @throws Exception if the {@link QueryResult} is already closed
		 */
		boolean isNull(final int columnIndex) throws Exception;

		/**
		 * Move to the first row in the {@code QueryResult}
		 * <p>
		 * This method will return false if the {@link QueryResult} is empty.
		 *
		 * @return {@code boolean} indicating whether the move was successful
		 *
		 * @throws Exception if the {@code QueryResult} is already closed
		 */
		boolean moveToFirst() throws Exception;

		/**
		 * Move to the last row in the {@code QueryResult}
		 * <p>
		 * This method will return false if the {@link QueryResult} is empty.
		 *
		 * @return {@code boolean} indicating whether the move was successful
		 *
		 * @throws Exception if the {@code QueryResult} is already closed
		 */
		boolean moveToLast() throws Exception;

		/**
		 * Move to the next row in the {@code QueryResult}
		 * <p>
		 * This method will return false if the {@link QueryResult} is empty.
		 *
		 * @return {@code boolean} indicating whether the move was successful
		 *
		 * @throws Exception if the {@code QueryResult} is already closed
		 */
		boolean moveToNext() throws Exception;

		/**
		 * Close this {@code QueryResult}
		 */
		void close();

	}

}
