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

import android.database.sqlite.SQLiteDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FakeDatabase implements DatabaseService.Database {
	private Connection connection;
	private final Object dbMutex = new Object();
	public FakeDatabase() {
		synchronized (dbMutex) {
			initializeConnection();
		}
	}

	@Override
	public boolean createTable(final String name, final String[] columnNames, final ColumnDataType[] dataTypes,
							   final List<List<ColumnConstraint>> columnConstraints) {

		PreparedStatement stmt = null;

		synchronized (dbMutex) {
			initializeConnection();

			try {
				String createTableQuery = QueryStringBuilder.getCreateTableQueryString(name, columnNames, dataTypes,
										  columnConstraints);
				stmt = connection.prepareStatement(createTableQuery);
				stmt.executeUpdate();
				return true;
			} catch (SQLException e) {
				return false;
			} finally {
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public DatabaseService.QueryResult query(final Query query) {
		PreparedStatement stmt;

		synchronized (dbMutex) {
			try {
				// this one is needed so we can count the number of rows and then scroll back to first row in the table
				StringBuilder sb = new StringBuilder();
				sb.append("SELECT ");
				String[] columns = query.getColumns();

				if (columns != null && columns.length > 0) {
					for (String column : columns) {
						sb.append(column).append(", ");
					}

					sb.delete(sb.length() - 2, sb.length());
				} else {
					sb.append("*");
				}

				sb.append(" FROM ");
				sb.append(query.getTable());
				sb.append(" ");
				String selection = query.getSelection();

				if (selection != null && selection.length() > 0) {
					String newWhereClause = selection;
					String[] selectionArgs = query.getSelectionArgs();

					if (selectionArgs != null && selectionArgs.length > 0) {
						for (String arg : selectionArgs) {
							newWhereClause = newWhereClause.replaceFirst("\\?", arg);
						}
					}

					sb.append(" WHERE " + newWhereClause);
				}

				String groupBy = query.getGroupBy();

				if (groupBy != null && groupBy.length() > 0) {
					sb.append(" GROUP BY ").append(groupBy).append(" ");
				}

				String having = query.getHaving();

				if (having != null && having.length() > 0) {
					sb.append(" HAVING ").append(having).append(" ");
				}

				String orderBy = query.getOrderBy();

				if (orderBy != null && orderBy.length() > 0) {
					sb.append(" ORDER BY ").append(orderBy).append(" ");
				}

				String limit = query.getLimit();

				if (limit != null && limit.length() > 0) {
					sb.append(" LIMIT ").append(limit).append(" ");
				}

				stmt = connection.prepareStatement(sb.toString(), ResultSet.TYPE_SCROLL_SENSITIVE,
												   ResultSet.CONCUR_READ_ONLY);
				ResultSet resultSet = stmt.executeQuery();
				return new FakeQueryResult(resultSet);
			} catch (SQLException e) {
				return null;
			}
		}
	}

	private void sqlBindStatement(final String sb, final Map<String, Object> values) throws SQLException {
		PreparedStatement stmt = (PreparedStatement) connection.prepareStatement(sb);

		int index = 0;

		for (Map.Entry<String, Object> entry : values.entrySet()) {
			Object ob = entry.getValue();
			index += 1;

			if (ob instanceof String) {
				stmt.setString(index, ob.toString());
			} else if (ob instanceof Integer) {
				stmt.setInt(index, ((Integer) ob).intValue());
			} else if (ob instanceof Long) {
				stmt.setLong(index, ((Long) ob).longValue());
			} else if (ob instanceof Double) {
				stmt.setDouble(index, ((Double) ob).doubleValue());
			} else if (ob instanceof Boolean) {
				stmt.setBoolean(index, ((Boolean) ob).booleanValue());
			} else if (ob instanceof Float) {
				stmt.setFloat(index, ((Float) ob).floatValue());
			}
		}

		stmt.executeUpdate();
	}

	@Override
	public boolean insert(final String table, final Map<String, Object> values) {
		synchronized (dbMutex) {

			try {
				StringBuilder sb = new StringBuilder();
				sb.append("INSERT INTO ").append(table);
				sb.append("(");

				for (Map.Entry<String, Object> entry : values.entrySet()) {
					sb.append(entry.getKey()).append(", ");
				}

				if (values.size() > 0) {
					sb.delete(sb.length() - 2, sb.length());
				}

				sb.append(") VALUES (");

				for (Map.Entry<String, Object> entry : values.entrySet()) {
					sb.append("?, ");
				}

				if (values.size() > 0) {
					sb.delete(sb.length() - 2, sb.length());
				}

				sb.append(")");
				sqlBindStatement(sb.toString(), values);
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}
	}

	@Override
	public boolean update(final String table, final Map<String, Object> values, final String whereClause,
						  final String[] whereArgs) {
		String newWhereClause = whereClause;

		if (whereArgs != null && whereArgs.length > 0) {
			for (String arg : whereArgs) {
				newWhereClause = newWhereClause.replaceFirst("\\?", arg);
			}
		}

		PreparedStatement stmt;

		synchronized (dbMutex) {
			try {
				StringBuilder sb = new StringBuilder();
				sb.append("UPDATE ").append(table);
				sb.append(" SET ");

				for (Map.Entry<String, Object> entry : values.entrySet()) {
					sb.append(entry.getKey()).append("=?, ");
				}

				if (values.size() > 0) {
					sb.delete(sb.length() - 2, sb.length());
				}

				if (newWhereClause != null) {
					sb.append(" WHERE " + newWhereClause);
				}

				sqlBindStatement(sb.toString(), values);
				return true;
			} catch (SQLException e) {
				return false;
			}
		}
	}

	@Override
	public boolean delete (final String table, final String whereClause, final String[] whereArgs) {
		String newWhereClause = whereClause;

		if (whereArgs != null && whereArgs.length > 0) {
			for (String arg : whereArgs) {
				newWhereClause = newWhereClause.replaceFirst("\\?", arg);
			}
		}

		PreparedStatement stmt;

		synchronized (dbMutex) {
			try {
				String query = "DELETE FROM " + table;

				if (newWhereClause != null) {
					query += " WHERE " + newWhereClause;
				}

				stmt = connection.prepareStatement(query.toString());
				return stmt.executeUpdate() > 0;
			} catch (SQLException e) {
				return false;
			}
		}
	}

	public boolean closeWasCalled = false;
	@Override
	public void close() {
		synchronized (dbMutex) {
			closeWasCalled = true;

			try {
				this.connection.close();
			} catch (Exception e) {
			}
		}
	}

	private void initializeConnection() {
		try {
			if (connection == null || connection.isClosed()) {
				this.connection = DriverManager.getConnection("jdbc:h2:mem:");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// copied from Android code
	private static class QueryStringBuilder {

		private static final Map<ColumnConstraint, String> COLUMN_CONSTRAINT_STRING_MAP = new
		HashMap<ColumnConstraint, String>() {
			{
				put(ColumnConstraint.PRIMARY_KEY, "PRIMARY KEY");
				put(ColumnConstraint.AUTOINCREMENT, "AUTO_INCREMENT");
				put(ColumnConstraint.NOT_NULL, "NOT NULL");
				put(ColumnConstraint.UNIQUE, "UNIQUE");
			}
		};

		static String getCreateTableQueryString(final String name,
												final String[] columnNames,
												final ColumnDataType[] columnDataTypes,
												final List<List<ColumnConstraint>> columnConstraints) throws SQLException {

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

				if (columnIndex < columnNames.length - 1) {
					createTableQueryBuilder.append(", ");
				}
			}

			createTableQueryBuilder.append(")");
			return createTableQueryBuilder.toString();
		}

		private static List<String> getColumnDataTypes(final ColumnDataType[] columnDataTypes) throws SQLException {
			if (columnDataTypes == null || columnDataTypes.length == 0) {
				return null;
			}

			List<String> dataTypesList = new ArrayList<String>();

			for (ColumnDataType dataType : columnDataTypes) {
				dataTypesList.add(dataType.name());
			}

			return dataTypesList;
		}

		private static List<String> getColumnConstraints(List<List<ColumnConstraint>>
				columnConstraints) throws SQLException {
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
}
