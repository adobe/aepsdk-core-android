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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class FakeQueryResult implements DatabaseService.QueryResult {
	ResultSet resultSet;
	ResultSetMetaData resultMetaData;

	FakeQueryResult(ResultSet resultSet) throws SQLException {
		this.resultSet = resultSet;
		this.resultMetaData = resultSet.getMetaData();
	}

	@Override
	public int getCount() throws Exception {
		int totalRows;

		resultSet.last();
		totalRows = resultSet.getRow();
		resultSet.beforeFirst();

		return totalRows ;
	}

	@Override
	public int getInt(final int columnIndex) throws Exception {
		return resultSet.getInt(columnIndex + 1);
	}

	@Override
	public double getDouble(final int columnIndex) throws Exception {
		return resultSet.getDouble(columnIndex + 1);
	}

	@Override
	public float getFloat(final int columnIndex) throws Exception {
		return resultSet.getFloat(columnIndex + 1);
	}

	@Override
	public long getLong(final int columnIndex) throws Exception {
		return resultSet.getLong(columnIndex + 1);
	}

	@Override
	public String getString(final int columnIndex) throws Exception {
		return resultSet.getString(columnIndex + 1);
	}

	@Override
	public boolean isNull(final int columnIndex) throws Exception {
		return resultSet.getObject(columnIndex + 1) == null;
	}

	@Override
	public boolean moveToFirst() throws Exception {
		this.resultSet.beforeFirst();
		this.resultSet.next();
		return true;
	}

	@Override
	public boolean moveToLast() throws Exception {
		this.resultSet.afterLast();
		return true;
	}

	@Override
	public boolean moveToNext() throws Exception {
		return this.resultSet.next();
	}

	@Override
	public void close() {
		try {
			this.resultSet.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public String getColumnName(int columnIndex) throws Exception {
		return this.resultMetaData.getColumnName(columnIndex);
	}

	public String getColumnTypeName(int columnIndex) throws Exception {
		return this.resultMetaData.getColumnTypeName(columnIndex);
	}

	public int getColumnCount() throws Exception {
		return this.resultMetaData.getColumnCount();
	}

	public boolean isAutoincrement(int columnIndex) throws Exception {
		return this.resultMetaData.isAutoIncrement(columnIndex);
	}
}
