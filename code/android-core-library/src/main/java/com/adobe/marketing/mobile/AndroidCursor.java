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

import android.database.Cursor;

/**
 * AndroidCursor class implements QueryResult Interface for reading query results from a Database.
 */
class AndroidCursor implements QueryResult {

	private final Cursor cursor;

	/**
	 * Constructor
	 *
	 * @param cursor {@link Cursor} for accessing the result set from a database query
	 */
	AndroidCursor(final Cursor cursor) {
		this.cursor = cursor;
	}

	/**
	 * Returns the total number of rows in this {@code QueryResult}.
	 *
	 * @return {@code int} indicating number of rows in the {@link QueryResult}
	 *
	 * @throws Exception if the underlying table does not exist, or if the {@link QueryResult} is already closed
	 */
	@Override
	public int getCount() throws Exception {
		return cursor.getCount();
	}

	/**
	 * Returns the value of the requested column as an {@code int}.
	 *
	 * @param columnIndex {@code int} zero-based index of the target column
	 *
	 * @return the value of the column as an {@code int}
	 *
	 * @throws Exception if the {@code columnIndex} is out of bounds, or if the {@link QueryResult} is already closed
	 */
	@Override
	public int getInt(final int columnIndex) throws Exception {
		return cursor.getInt(columnIndex);
	}

	/**
	 * Returns the value of the requested column as a {code double}.
	 *
	 * @param columnIndex {@code int} zero-based index of the target column
	 *
	 * @return the value of the column as a {@code double}
	 *
	 * @throws Exception if the {@code columnIndex} is out of bounds, or if the {@link QueryResult} is already closed
	 */
	@Override
	public double getDouble(final int columnIndex) throws Exception {
		return cursor.getDouble(columnIndex);
	}

	/**
	 * Returns the value of the requested column as a {@code float}.
	 *
	 * @param columnIndex {@code int} zero-based index of the target column
	 *
	 * @return the value of the column as a {@code float}
	 *
	 * @throws Exception if the {@code columnIndex} is out of bounds, or if the {@link QueryResult} is already closed
	 */
	@Override
	public float getFloat(final int columnIndex) throws Exception {
		return cursor.getFloat(columnIndex);
	}

	/**
	 * Returns the value of the requested column as a {@code long}.
	 *
	 * @param columnIndex {@code int} zero-based index of the target column
	 *
	 * @return the value of the column as a {@code long}
	 *
	 * @throws Exception if the {@code columnIndex} is out of bounds, or if the {@link QueryResult} is already closed
	 */
	@Override
	public long getLong(final int columnIndex) throws Exception {
		return cursor.getLong(columnIndex);
	}

	/**
	 * Returns the value of the requested column as a {@code String}.
	 *
	 * @param columnIndex {@code int} zero-based index of the target column
	 *
	 * @return the value of the column as a {@link String}
	 *
	 * @throws Exception if the {@code columnIndex} is out of bounds, or if the {@link QueryResult} is already closed
	 */
	@Override
	public String getString(final int columnIndex) throws Exception {
		return cursor.getString(columnIndex);
	}

	/**
	 * Returns true if the value of the requested column is null, false otherwise.
	 *
	 * @param columnIndex {@code int} zero-based index of the target column
	 *
	 * @return {@code boolean} indicating whether the column value is null
	 *
	 * @throws Exception if the {@link QueryResult} is already closed
	 */
	@Override
	public boolean isNull(final int columnIndex) throws Exception {
		return cursor.isNull(columnIndex);
	}

	/**
	 * Move to the first row in the {@code QueryResult}
	 * <p>
	 * This method will return false if the {@link QueryResult} is empty.
	 *
	 * @return {@code boolean} indicating whether the move was successful
	 *
	 * @throws Exception if the {@code QueryResult} is already closed
	 */
	@Override
	public boolean moveToFirst() throws Exception {
		return cursor.moveToFirst();
	}

	/**
	 * Move to the last row in the {@code QueryResult}
	 * <p>
	 * This method will return false if the {@link QueryResult} is empty.
	 *
	 * @return {@code boolean} indicating whether the move was successful
	 *
	 * @throws Exception if the {@code QueryResult} is already closed
	 */
	@Override
	public boolean moveToLast() throws Exception {
		return cursor.moveToLast();
	}

	/**
	 * Move to the next row in the {@code QueryResult}
	 * <p>
	 * This method will return false if the {@link QueryResult} is empty.
	 *
	 * @return {@code boolean} indicating whether the move was successful
	 *
	 * @throws Exception if the {@code QueryResult} is already closed
	 */
	@Override
	public boolean moveToNext() throws Exception {
		return cursor.moveToNext();
	}

	/**
	 * Close this {@code QueryResult}
	 */
	@Override
	public void close() {
		try {
			cursor.close();
		} catch (Exception e) {
			// do nothing
		}
	}
}
