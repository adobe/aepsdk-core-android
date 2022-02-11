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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class TestableDatabase {
	private String databaseName;
	private String tableName;

	private FakeDatabaseService testableDatabaseService;
	private FakeDatabase        testableDatabase;
	MockSystemInfoService mockSystemInfoService;

	TestableDatabase(final MockEventHubModuleTest eventHub, final String databaseName, final String tableName,
					 final String[] columnNames, final DatabaseService.Database.ColumnDataType[]
					 columnDataTypes) {
		this.databaseName = databaseName;
		this.tableName = tableName;

		testableDatabaseService = (FakeDatabaseService) eventHub.getPlatformServices().getDatabaseService();
		mockSystemInfoService = (MockSystemInfoService) eventHub.getPlatformServices().getSystemInfoService();
		initializeDatabase(columnNames, columnDataTypes);
	}

	private void openDatabase() {
		File dbPath = new File(mockSystemInfoService.applicationCacheDir, databaseName);
		testableDatabase = (FakeDatabase) testableDatabaseService.openDatabase(dbPath.getPath());
	}

	private void initializeDatabase(final String[] columnNames,
									final DatabaseService.Database.ColumnDataType[] columnDataTypes) {
		if (testableDatabase == null) {
			openDatabase();
		}

		int columnsNo = columnDataTypes.length;
		List<List<DatabaseService.Database.ColumnConstraint>> columnConstraints =
			new ArrayList<List<DatabaseService.Database.ColumnConstraint>>();
		List<DatabaseService.Database.ColumnConstraint> idColumnConstraints =
			new ArrayList<DatabaseService.Database.ColumnConstraint>();
		idColumnConstraints.add(DatabaseService.Database.ColumnConstraint.PRIMARY_KEY);
		idColumnConstraints.add(DatabaseService.Database.ColumnConstraint.AUTOINCREMENT);
		columnConstraints.add(idColumnConstraints);

		for (int i = 0; i < columnsNo - 1; i++) {
			columnConstraints.add(new ArrayList<DatabaseService.Database.ColumnConstraint>());
		}

		testableDatabase.createTable(tableName, columnNames, columnDataTypes, columnConstraints);
	}

	void closeDatabase() {
		File dbPath = new File(mockSystemInfoService.applicationCacheDir, databaseName);
		testableDatabase.close();
		testableDatabaseService.deleteDatabase(dbPath.getPath());
	}

	boolean insert(final Map<String, Object> hit) {
		return testableDatabase.insert(tableName, hit);
	}

	void insertNHits(final Map<String, Object> hit, final int hitsNumber) {
		for (int i = 0; i < hitsNumber; i++) {
			testableDatabase.insert(tableName, hit);
		}
	}

	boolean delete (final int hitIdentifier) {
		return testableDatabase.delete(tableName, "ID = ?", new String[] {String.valueOf(hitIdentifier)});
	}

	boolean deleteAll() {
		return testableDatabase.delete(tableName, null, null);
	}

	int count() {
		try {
			return testableDatabase.query(new Query.Builder(tableName, new String[] {"ID", "URL", "TIMESTAMP"}).build())
				   .getCount();
		} catch (Exception e) {
			return 0;
		}
	}
}
