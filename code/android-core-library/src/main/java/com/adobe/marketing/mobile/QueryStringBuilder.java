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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * QueryStringBuilder class exposes method to build create table query string given column names, data types and optional constraints.
 */
final class QueryStringBuilder {

	private QueryStringBuilder() {}

	private static final Map<ColumnConstraint, String> COLUMN_CONSTRAINT_STRING_MAP = new
	HashMap<ColumnConstraint, String>() {
		{
			put(ColumnConstraint.PRIMARY_KEY, "PRIMARY KEY");
			put(ColumnConstraint.AUTOINCREMENT, "AUTOINCREMENT");
			put(ColumnConstraint.NOT_NULL, "NOT NULL");
			put(ColumnConstraint.UNIQUE, "UNIQUE");
		}
	};

	private static final Map<ColumnDataType, String> COLUMN_DATA_TYPE_DEFAULT_MAP = new
	HashMap<ColumnDataType, String>() {
		{
			put(ColumnDataType.INTEGER, "0");
			put(ColumnDataType.REAL, "0.0");
			put(ColumnDataType.TEXT, "''");
		}
	};
	/**
	 * Returns a query string to create table with the given column names, data types and constraints.
	 *
	 * @param name {@link String} containing table name
	 * @param columnNames {@code String[]} array containing column names in the table
	 * @param columnDataTypes {@code ColumnDataType[]} array containing column data types for the table columns
	 * @param columnConstraints {@code List<List<ColumnConstraint>>} list of column constraints' lists for the table columns
	 *
	 * @return {@code String} specifying create table query
	 * @see ColumnDataType
	 * @see ColumnConstraint
	 */
	static String getCreateTableQueryString(final String name,
											final String[] columnNames,
											final ColumnDataType[] columnDataTypes,
											final List<List<ColumnConstraint>> columnConstraints,
											final boolean setColumnsDefault) {

		if (StringUtils.isNullOrEmpty(name)
				|| columnNames == null || columnNames.length == 0
				|| columnDataTypes == null || columnDataTypes.length != columnNames.length
				|| (columnConstraints != null && !columnConstraints.isEmpty() && columnConstraints.size() != columnNames.length)) {
			return null;
		}

		List<String> dataTypesList = getColumnDataTypes(columnDataTypes);
		List<String> columnConstraintsList = getColumnConstraints(columnConstraints);


		StringBuilder createTableQueryBuilder = new StringBuilder();
		createTableQueryBuilder.append("CREATE TABLE IF NOT EXISTS ");
		createTableQueryBuilder.append(name);
		createTableQueryBuilder.append("(");

		for (int columnIndex = 0; columnIndex < columnNames.length; columnIndex++) {
			createTableQueryBuilder.append(columnNames[columnIndex]);

			createTableQueryBuilder.append(" ");
			createTableQueryBuilder.append(dataTypesList.get(columnIndex));

			if (columnConstraintsList != null) {
				String constraints = columnConstraintsList.get(columnIndex);

				if (constraints != null) {
					createTableQueryBuilder.append(" ");
					createTableQueryBuilder.append(columnConstraintsList.get(columnIndex));
				}
			}

			if (setColumnsDefault && columnConstraintsList != null
					&& columnConstraints.get(columnIndex) != null
					&& !columnConstraints.get(columnIndex).contains(ColumnConstraint.AUTOINCREMENT)
					&& !columnConstraints.get(columnIndex).contains(ColumnConstraint.PRIMARY_KEY)) {
				createTableQueryBuilder.append(" DEFAULT ");
				createTableQueryBuilder.append(COLUMN_DATA_TYPE_DEFAULT_MAP.get(columnDataTypes[columnIndex]));
				createTableQueryBuilder.append(" ");
			}


			if (columnIndex < columnNames.length - 1) {
				createTableQueryBuilder.append(", ");
			}
		}

		createTableQueryBuilder.append(")");
		return createTableQueryBuilder.toString();
	}

	/**
	 * Returns the list of column data types.
	 *
	 * @param columnDataTypes {@code ColumnDataType[]} array containing column data types
	 * @return {@code List<String>} containing column data types expressed as {@link String}
	 * @see ColumnDataType
	 */
	private static List<String> getColumnDataTypes(final ColumnDataType[] columnDataTypes) {
		if (columnDataTypes == null || columnDataTypes.length == 0) {
			return null;
		}

		List<String> dataTypesList = new ArrayList<String>();

		for (ColumnDataType dataType : columnDataTypes) {
			dataTypesList.add(dataType.name());
		}

		return dataTypesList;
	}

	/**
	 * Returns the list of column constraints.
	 *
	 * @param columnConstraints {@code List<List<ColumnConstraint>>} list containing column constraints' lists
	 * @return {@code List<String>} containing column constraints expressed as {@link String}
	 * @see ColumnConstraint
	 */
	private static List<String> getColumnConstraints(List<List<ColumnConstraint>>
			columnConstraints) {
		if (columnConstraints == null || columnConstraints.isEmpty()) {
			return null;
		}

		List<String> constraints = new ArrayList<String>();

		for (List<ColumnConstraint> constraintList : columnConstraints) {
			if (constraintList == null || constraintList.isEmpty()) {
				constraints.add(null);
				continue;
			}

			StringBuilder columnConstraintBuilder = new StringBuilder();

			for (ColumnConstraint constraint : constraintList) {
				columnConstraintBuilder.append(COLUMN_CONSTRAINT_STRING_MAP.get(constraint));
				columnConstraintBuilder.append(" ");
			}

			constraints.add(columnConstraintBuilder.toString().trim());
		}

		return constraints;
	}

}
