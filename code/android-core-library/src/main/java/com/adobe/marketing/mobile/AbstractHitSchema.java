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

import com.adobe.marketing.mobile.DatabaseService.Database.ColumnConstraint;
import com.adobe.marketing.mobile.DatabaseService.Database.ColumnDataType;
import com.adobe.marketing.mobile.DatabaseService.QueryResult;

import java.util.List;
import java.util.Map;

/**
 * Base class for any {@code HitSchema} class.  Defines the database table architecture and is needed
 * for an implementation of {@link AbstractHitsDatabase}.
 *
 * @param <T> {@link AbstractHit} subclass that represents this implementation's database schema
 */
abstract class AbstractHitSchema<T extends AbstractHit> {

	List<List<ColumnConstraint>> columnConstraints;
	ColumnDataType[] columnDataTypes;
	String[] columnNames;

	// ========================================================================================
	// abstract methods
	// ========================================================================================
	/**
	 * Receives a T (extends {@link AbstractHit}) object and returns a {@code Map<String, Object>} to be used
	 * as a parameter for a database query.
	 *
	 * @param hit T (extends {@link AbstractHit}) object to be written to the underlying database
	 * @return {@code Map<String, Object>} where the key is the column name and the value is the corresponding
	 *         column value represented by {@code hit}
	 */
	abstract Map<String, Object> generateDataMap(final T hit);

	/**
	 * Receives a {@code QueryResult} object and returns its object representation.
	 *
	 * @param queryResult {@link QueryResult} representing the result of a database query
	 * @return T (extends {@link AbstractHit}) instance representing the provided {@code queryResult}
	 */
	abstract T generateHit(final QueryResult queryResult);

	// ========================================================================================
	// package-private methods
	// ========================================================================================
	/**
	 * Gets the column constraints represented by this schema.
	 *
	 * @return an ordered {@link List} of {@code List<ColumnConstraint>} containing any constraints for the given field
	 */
	final List<List<ColumnConstraint>> getColumnConstraints() {
		return columnConstraints;
	}

	/**
	 * Gets the column data types represented by this schema.
	 *
	 * @return an ordered {@code ColumnDataType} array containing the data types for each field in the table
	 */
	final ColumnDataType[] getColumnDataTypes() {
		return columnDataTypes;
	}

	/**
	 * Gets the column names represented by this schema.
	 *
	 * @return an ordered {@code String} array containing the names of each field in the table
	 */
	final String[] getColumnNames() {
		return columnNames;
	}
}