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

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static com.adobe.marketing.mobile.DatabaseService.Database.ColumnDataType;
import static com.adobe.marketing.mobile.DatabaseService.Database.ColumnConstraint;

public class FakeEventHistoryDatabase implements EventHistoryDatabase {
	private Connection connection;
	private final Object dbMutex = new Object();
	List<EventHistoryDatabase> databases;
	private final static String TABLE_NAME = "TestEvents";
	private static final String COLUMN_HASH = "eventHash";
	private static final String COLUMN_TIMESTAMP = "timestamp";
	private static final String COUNT = "count";
	private static final String OLDEST = "oldest";
	private static final String NEWEST = "newest";

	public FakeEventHistoryDatabase() {
		synchronized (dbMutex) {
			initializeConnection();
			databases = new ArrayList<>();
		}
	}

	@Override
	public boolean openDatabase() {
		synchronized (dbMutex) {
			if (databases.size() == 0) {
				databases.add(new FakeEventHistoryDatabase());
				return true;
			}

			// database wasn't opened as one already exists
			return false;
		}
	}

	@Override
	public boolean deleteDatabase() {
		synchronized (dbMutex) {
			if (databases.size() > 0) {
				databases.remove(0);
				return true;
			}

			return false;
		}
	}

	// dataTypes is unused as the created table will use BIGINT for its data types
	@Override
	public boolean createTable(final String[] columnNames, final ColumnDataType[] dataTypes,
							   final List<List<ColumnConstraint>> columnConstraints) {
		PreparedStatement stmt = null;

		synchronized (dbMutex) {
			initializeConnection();

			try {
				String createTableQuery = QueryStringBuilder.getCreateTableQueryString(TABLE_NAME, columnNames, columnConstraints);
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
	public FakeQueryResult select(final long hash, final long from, final long to) {
		PreparedStatement stmt;

		synchronized (dbMutex) {
			try {
				StringBuilder sb = new StringBuilder();
				sb.append("SELECT ");
				sb.append(COUNT + "(*) as " + COUNT + ", ");
				sb.append("min(" + COLUMN_TIMESTAMP + ") as " + OLDEST + ", ");
				sb.append("max(" + COLUMN_TIMESTAMP + ") as " + NEWEST);
				sb.append(" FROM " + TABLE_NAME + " ");
				sb.append(" WHERE " + COLUMN_HASH + "=" + hash);
				sb.append(" AND " + COLUMN_TIMESTAMP + " >= " + from);
				sb.append(" AND " + COLUMN_TIMESTAMP + " <= " + to);

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

	public int rowCount = 0;
	@Override
	public boolean insert(final long hash) {
		synchronized (dbMutex) {
			HashMap<String, Object> values = new HashMap<>();
			values.put(COLUMN_HASH, hash);
			values.put(COLUMN_TIMESTAMP, System.currentTimeMillis());

			try {
				StringBuilder sb = new StringBuilder();
				sb.append("INSERT INTO ").append(TABLE_NAME);
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
				rowCount++;

				return true;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}
	}

	@Override
	public int delete (final long hash, final long from, final long to) {
		String newWhereClause = COLUMN_HASH + " = " + hash
								+ " AND " + COLUMN_TIMESTAMP + " >= " + from
								+ " AND " + COLUMN_TIMESTAMP + " <= " + to;

		PreparedStatement stmt;

		synchronized (dbMutex) {
			try {
				String query = "DELETE FROM " + TABLE_NAME;

				if (newWhereClause != null) {
					query += " WHERE " + newWhereClause;
				}

				stmt = connection.prepareStatement(query.toString());
				int deleteCount = stmt.executeUpdate();
				rowCount -= deleteCount;

				return deleteCount;
			} catch (SQLException e) {
				return 0;
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
												final List<List<ColumnConstraint>> columnConstraints) throws SQLException {

			if (StringUtils.isNullOrEmpty(name)
					|| columnNames == null || columnNames.length == 0
					|| (columnConstraints != null && !columnConstraints.isEmpty() && columnConstraints.size() != columnNames.length)) {
				return null;
			}

			List<String> columnConstraintsList = getColumnConstraints(columnConstraints);

			StringBuilder createTableQueryBuilder = new StringBuilder();
			createTableQueryBuilder.append("CREATE TABLE IF NOT EXISTS ");
			createTableQueryBuilder.append(name);
			createTableQueryBuilder.append("(");

			for (int columnIndex = 0; columnIndex < columnNames.length; columnIndex++) {
				createTableQueryBuilder.append(columnNames[columnIndex]);
				createTableQueryBuilder.append(" BIGINT");

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

		private static List<String> getColumnConstraints(final List<List<ColumnConstraint>>
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