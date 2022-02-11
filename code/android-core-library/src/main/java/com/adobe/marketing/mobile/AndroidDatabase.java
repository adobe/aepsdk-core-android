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

import android.database.sqlite.SQLiteStatement;
import com.adobe.marketing.mobile.DatabaseService.QueryResult;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * AndroidDatabase class implements Database Interface to define relational database operations.
 */
class AndroidDatabase implements DatabaseService.Database {
	private final Object dbMutex = new Object();
	private static final String LOG_TAG = AndroidDatabase.class.getSimpleName();
	private static final String MIGRATION_DATABASE_TAG = "_MIGRATION";
	private final SQLiteDatabase database;

	/**
	 * Constructor
	 *
	 * @param database {@link SQLiteDatabase} instance
	 */
	AndroidDatabase(final SQLiteDatabase database) {
		synchronized (dbMutex) {
			this.database = database;
		}
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
	@Override
	public boolean createTable(final String name, final String[] columnNames, final ColumnDataType[] columnDataTypes,
							   final List<List<ColumnConstraint>> columnConstraints) {
		return createTable(name, columnNames, columnDataTypes, columnConstraints, false);

	}

	public boolean createTable(final String tableName, final String[] columnNames, final ColumnDataType[] columnDataTypes,
							   final List<List<ColumnConstraint>> columnConstraints, final boolean setColumnsDefault) {

		if (StringUtils.isNullOrEmpty(tableName)
				|| columnNames == null || columnNames.length == 0
				|| columnDataTypes == null || columnDataTypes.length == 0
				|| columnDataTypes.length != columnNames.length || (columnConstraints != null
						&& columnConstraints.size() != columnNames.length)) {
			Log.warning(LOG_TAG, "Failed to create table, one or more input parameters is invalid.");
			return false;
		}

		if (!databaseIsWritable()) {
			return false;
		}

		String cleanTableName = cleanString(tableName);
		String[] cleanColumnNames = cleanColumnNames(columnNames);


		synchronized (dbMutex) {
			try {
				final String createTableQuery = QueryStringBuilder.getCreateTableQueryString(cleanTableName, cleanColumnNames,
												columnDataTypes,
												columnConstraints, setColumnsDefault);
				SQLiteStatement stmt = database.compileStatement(createTableQuery);
				stmt.execute();
				stmt.close();

				migrateDatabaseIfNeeded(cleanTableName, cleanColumnNames, columnDataTypes, columnConstraints);
				return true;
			} catch (Exception e) {
				Log.debug(LOG_TAG, "Failed to create table (%s)",
						  (e.getLocalizedMessage() != null ? e.getLocalizedMessage() : e.getMessage()));
				return false;
			}
		}
	}


	private boolean migrateDatabaseIfNeeded(final String name,
											final String[] columnNames,
											final ColumnDataType[] columnDataTypes,
											final List<List<ColumnConstraint>> columnConstraints) {
		if (Arrays.equals(getColumnNames(name), columnNames)) {
			return true;
		}

		return migrateDatabase(name, columnNames, columnDataTypes, columnConstraints);

	}

	private boolean migrateDatabase(final String name,
									final String[] columnNames,
									final ColumnDataType[] columnDataTypes,
									final List<List<ColumnConstraint>> columnConstraints) {
		synchronized (dbMutex) {
			try {

				// 1. Create a temp migration table
				// 2. Find which columns are present in both schemas and copy their data into the new migration table
				// 3. Delete the original table and rename migration table to original table name

				database.beginTransaction();
				final String tempTableName = name + MIGRATION_DATABASE_TAG;

				if (!createTable(tempTableName, columnNames, columnDataTypes, columnConstraints, true)) {
					return false;
				}

				final String[] unionColumns = getUnionColumns(name, columnNames);

				if (!copyColumnsFromTableToTable(name, tempTableName, unionColumns)
						|| !deleteTable(name)
						|| !renameTable(tempTableName, name)) {
					return false;
				}


				database.setTransactionSuccessful();
			} catch (Exception e) {
				Log.warning(LOG_TAG, "Failed to execute query (%s)",
							(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : e.getMessage()));
				return false;
			} finally {
				database.endTransaction();
			}

			return true;
		}
	}

	private boolean copyColumnsFromTableToTable(final String fromTableName, final String toTableName,
			final String[] columnNames) {

		String[] cleanColumnNames = cleanColumnNames(columnNames);
		StringBuilder queryStringBuilder = new StringBuilder();
		queryStringBuilder.append("INSERT INTO ").append(toTableName).append(" (");

		StringBuilder columnsStringBuilder = new StringBuilder();

		for (int i = 0; i < cleanColumnNames.length ; i++) {
			columnsStringBuilder.append(cleanColumnNames[i]).append(" ");

			if (i != cleanColumnNames.length - 1) {
				columnsStringBuilder.append(", ");
			}
		}

		queryStringBuilder.append(columnsStringBuilder);
		queryStringBuilder.append(") SELECT ");
		queryStringBuilder.append(columnsStringBuilder);
		queryStringBuilder.append(" FROM ");
		queryStringBuilder.append(fromTableName);
		queryStringBuilder.append(";");

		synchronized (dbMutex) {
			try {
				SQLiteStatement stmt = database.compileStatement(queryStringBuilder.toString());
				stmt.execute();
				stmt.close();
				return true;
			} catch (Exception e) {
				Log.debug(LOG_TAG, "Failed to create table (%s)",
						  (e.getLocalizedMessage() != null ? e.getLocalizedMessage() : e.getMessage()));
				return false;
			}
		}

	}

	private String[] getUnionColumns(final String name,
									 final String[] columnNames) {
		String[] oldColumnNames = getColumnNames(name);
		Arrays.sort(oldColumnNames);

		List<String> unionColumnNameList
			= new ArrayList<String>();

		for (int i = 0; i < columnNames.length; i++) {
			if (Arrays.binarySearch(oldColumnNames, columnNames[i]) >= 0) {
				unionColumnNameList.add(columnNames[i]);
			}
		}

		return unionColumnNameList.toArray(new String[] {});

	}

	private String[] getColumnNames(final String tableName) {

		synchronized (dbMutex) {
			Cursor dbCursor = null;

			try {
				dbCursor = database.query(tableName, null, null, null, null, null, null);
				return cleanColumnNames(dbCursor.getColumnNames());
			} catch (Exception e) {
				Log.warning(LOG_TAG, "Failed to execute query (%s)",
							(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : e.getMessage()));
				return new String[] {};
			} finally {
				try {
					if (dbCursor != null) {
						dbCursor.close();
					}
				} catch (Exception e) {
					// no-op
				}
			}

		}
	}


	/**
	 * Query a table in the database.
	 *
	 * @param query {@link Query} object indicating the query to execute
	 *
	 * @return {@link QueryResult} the result of this query, positioned at the first row
	 */
	@Override
	public QueryResult query(final Query query) {
		if (query == null) {
			Log.debug(LOG_TAG, "%s (Query), could not provide query result.", Log.UNEXPECTED_NULL_VALUE);
			return null;
		}

		synchronized (dbMutex) {
			try {
				@SuppressLint("Recycle")
				Cursor cursor = database.query(cleanString(query.getTable()),
											   cleanColumnNames(query.getColumns()),
											   query.getSelection(),
											   query.getSelectionArgs(),
											   query.getGroupBy(),
											   query.getHaving(),
											   query.getOrderBy(),
											   query.getLimit());
				return new AndroidCursor(cursor);
			} catch (Exception e) {
				Log.warning(LOG_TAG, "Failed to execute query (%s)",
							(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : e.getMessage()));
				return null;
			}
		}
	}

	/**
	 * Insert a row into a table in the database.
	 *
	 * @param table {@link String} containing the table name to insert the row into
	 * @param values {@code Map<String, Object>} map containing the column name value pairs for the row
	 *
	 * @return {@code boolean} indicating whether the insert operation was successful
	 */
	@Override
	public boolean insert(final String table, final Map<String, Object> values) {
		if (StringUtils.isNullOrEmpty(table) || values == null || values.isEmpty()) {
			Log.debug(LOG_TAG, "Could not insert row, table name or column values were empty or null.");
			return false;
		}

		if (!databaseIsWritable()) {
			return false;
		}

		synchronized (dbMutex) {
			try {
				return database.insert(cleanString(table), null, getContentValueFromMap(values)) != -1;
			} catch (Exception e) {
				Log.warning(LOG_TAG, "Failed to insert rows into the table (%s)",
							(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : e.getMessage()));
				return false;
			}
		}
	}

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
	@Override
	public boolean update(final String table, final Map<String, Object> values, final String whereClause,
						  final String[] whereArgs) {
		if (StringUtils.isNullOrEmpty(table) || values == null || values.isEmpty()) {
			Log.debug(LOG_TAG, "Could not update rows, table name or column values were empty or null.");
			return false;
		}

		if (!databaseIsWritable()) {
			return false;
		}

		synchronized (dbMutex) {
			try {
				return database.update(cleanString(table), getContentValueFromMap(values), whereClause, whereArgs) != 0;
			} catch (Exception e) {
				Log.warning(LOG_TAG, "Failed to update table rows (%s)",
							(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : e.getMessage()));
				return false;
			}
		}
	}

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
	@Override
	public boolean delete (final String table, final String whereClause, final String[] whereArgs) {
		if (!databaseIsWritable()) {
			return false;
		}

		synchronized (dbMutex) {
			try {
				//To remove all rows and get a count pass "1" as the whereClause.
				int affectedRowsCount = database.delete(cleanString(table), (whereClause != null ? whereClause : "1"), whereArgs);
				Log.trace(LOG_TAG, "Count of rows deleted in table %s are %d", table, affectedRowsCount);

				return true;
			} catch (Exception e) {
				Log.debug(LOG_TAG, "Failed to delete table rows (%s)",
						  (e.getLocalizedMessage() != null ? e.getLocalizedMessage() : e.getMessage()));
				return false;
			}
		}
	}

	boolean deleteTable(final String tableName) {
		synchronized (dbMutex) {
			try {
				database.execSQL("DROP TABLE IF EXISTS " + cleanString(tableName));
				return true;
			} catch (Exception e) {
				Log.debug(LOG_TAG, "Failed to delete table (%s)",
						  (e.getLocalizedMessage() != null ? e.getLocalizedMessage() : e.getMessage()));
				return false;
			}
		}
	}

	boolean renameTable(final String tableName, final String newTableName) {
		synchronized (dbMutex) {
			try {
				database.execSQL(String.format("ALTER TABLE %s RENAME TO %s;", tableName, newTableName));
				return true;
			} catch (Exception e) {
				Log.debug(LOG_TAG, "Failed to rename table (%s)",
						  (e.getLocalizedMessage() != null ? e.getLocalizedMessage() : e.getMessage()));
				return false;
			}
		}
	}

	/**
	 * Close this database.
	 */
	@Override
	public void close() {
		synchronized (dbMutex) {
			database.close();
		}
	}

	private boolean databaseIsWritable() {
		synchronized (dbMutex) {
			if (database == null) {
				Log.debug(LOG_TAG, "%s (Database), unable to write", Log.UNEXPECTED_NULL_VALUE);
				return false;
			}

			if (!database.isOpen()) {
				Log.debug(LOG_TAG, "Unable to write to database, it is not open");
				return false;
			}

			if (database.isReadOnly()) {
				Log.debug(LOG_TAG, "Unable to write to database, it is read-only");
				return false;
			}

			return true;
		}
	}



	/**
	 * Populates and returns the {@link ContentValues} with the provided table hit details from the {@link Map}.
	 * <p>
	 * Always returns a non-null content value.
	 * Map elements containing types other than {@code String}, {@code long}, {@code Integer}, {@code Short}, {@code Byte}
	 * {@code Float}, {@code Boolean} ,{@code Double} will be ignored to be put in contentValue.
	 *
	 * @param values A {Map} containing columnNames and columnValues of a database hit
	 */
	private static ContentValues getContentValueFromMap(final Map<String, Object> values) {
		ContentValues contentValues = new ContentValues();

		for (Map.Entry<String, Object> value : values.entrySet()) {
			String columnName = value.getKey();
			Object columnValue = value.getValue();

			if (columnValue == null) {
				contentValues.putNull(columnName);
			} else if (columnValue instanceof String) {
				contentValues.put(columnName, (String) columnValue);
			} else if (columnValue instanceof Long) {
				contentValues.put(columnName, (Long) columnValue);
			} else if (columnValue instanceof Integer) {
				contentValues.put(columnName, (Integer) columnValue);
			} else if (columnValue instanceof Short) {
				contentValues.put(columnName, (Short) columnValue);
			} else if (columnValue instanceof Byte) {
				contentValues.put(columnName, (Byte) columnValue);
			} else if (columnValue instanceof Double) {
				contentValues.put(columnName, (Double) columnValue);
			} else if (columnValue instanceof Float) {
				contentValues.put(columnName, (Float) columnValue);
			} else if (columnValue instanceof Boolean) {
				contentValues.put(columnName, (Boolean) columnValue);
			} else if (columnValue instanceof byte[]) {
				contentValues.put(columnName, (byte[]) columnValue);
			} else {
				Log.warning(LOG_TAG, "Unsupported data type received for database insertion: columnName "  + columnName + " value: " +
							columnValue);
			}

		}

		return contentValues;
	}


	private static String cleanString(final String input) {
		return input.replaceAll("[^a-zA-Z0-9_]", "");
	}

	private static String[] cleanColumnNames(final String[] columnNames) {
		if (columnNames == null) {
			return null;
		}

		String[] cleanedColumnNames = new String[columnNames.length];

		for (int i = 0; i < columnNames.length; i++) {
			cleanedColumnNames[i] = cleanString(columnNames[i]);
		}

		return cleanedColumnNames;
	}

}
