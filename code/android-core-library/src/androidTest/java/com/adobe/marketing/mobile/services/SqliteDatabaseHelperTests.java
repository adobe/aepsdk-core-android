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

package com.adobe.marketing.mobile.services;

import android.content.ContentValues;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.adobe.marketing.mobile.internal.utility.SQLiteDatabaseHelper;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class SqliteDatabaseHelperTests {

	private static final String TABLE_NAME = "TB_AEP_DATA_ENTITY";
	private static final String TB_KEY_UNIQUE_IDENTIFIER = "uniqueIdentifier";
	private static final String TB_KEY_TIMESTAMP = "timestamp";
	private static final String TB_KEY_DATA = "data";
	private SQLiteDatabaseHelper sqLiteDatabaseHelper;
	private String dbPath;

	@Before
	public void setUp() {
		dbPath = new File(InstrumentationRegistry.getInstrumentation().getContext().getCacheDir(), "test.sqlite").getPath();
		sqLiteDatabaseHelper = new SQLiteDatabaseHelper();
		createTable();
	}

	@After
	public void dispose() {
		sqLiteDatabaseHelper.clearTable(dbPath, TABLE_NAME);
	}

	private void createTable() {
		final String tableCreationQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
										  " (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, " +
										  "uniqueIdentifier TEXT NOT NULL UNIQUE, " +
										  "timestamp INTEGER NOT NULL, " +
										  "data TEXT);";

		sqLiteDatabaseHelper.createTableIfNotExist(dbPath, tableCreationQuery);
	}

	@Test
	public void testTableIsEmptyInitially() {
		//Action
		int size = sqLiteDatabaseHelper.getTableSize(dbPath, TABLE_NAME);

		//Assert
		Assert.assertEquals(size, 0);
	}

	@Test
	public void testAddData_Success() {
		//Prepare data
		DataEntity dataEntity = new DataEntity("dataentity1");
		Map<String, Object> row = new HashMap<>();
		row.put(TB_KEY_DATA, dataEntity.getData());
		row.put(TB_KEY_UNIQUE_IDENTIFIER, dataEntity.getUniqueIdentifier());
		row.put(TB_KEY_TIMESTAMP, dataEntity.getTimestamp().getTime());

		//Action
		Assert.assertTrue(sqLiteDatabaseHelper.insertRow(dbPath, TABLE_NAME, row));

		//Assert
		Assert.assertEquals(sqLiteDatabaseHelper.getTableSize(dbPath, TABLE_NAME), 1);
	}

	@Test
	public void testAddData_Failure() {
		//Prepare data
		DataEntity dataEntity = new DataEntity("dataentity1");
		Map<String, Object> row = new HashMap<>();
		row.put(TB_KEY_DATA, dataEntity.getData());
		row.put(TB_KEY_UNIQUE_IDENTIFIER, dataEntity.getUniqueIdentifier());
		row.put(TB_KEY_TIMESTAMP,  dataEntity.getTimestamp().getTime());

		String incorrectDbPath = "incorrect_db_path";

		//Action
		Assert.assertFalse(sqLiteDatabaseHelper.insertRow(incorrectDbPath, TABLE_NAME, row));

		//Assert
		Assert.assertEquals(sqLiteDatabaseHelper.getTableSize(dbPath, TABLE_NAME), 0);
	}

	@Test
	public void testQueryDb_Success() {
		//Prepare data
		final String dataEntityName = "dataentity";
		DataEntity dataEntity = new DataEntity(dataEntityName);
		Map<String, Object> row = new HashMap<>();
		row.put(TB_KEY_DATA, dataEntity.getData());
		row.put(TB_KEY_UNIQUE_IDENTIFIER, dataEntity.getUniqueIdentifier());
		row.put(TB_KEY_TIMESTAMP,  dataEntity.getTimestamp().getTime());

		//Action
		Assert.assertTrue(sqLiteDatabaseHelper.insertRow(dbPath, TABLE_NAME, row));

		List<ContentValues> contentValues = sqLiteDatabaseHelper.query(dbPath, TABLE_NAME, new String[] {TB_KEY_UNIQUE_IDENTIFIER, TB_KEY_TIMESTAMP, TB_KEY_DATA},
											1);

		//Assert
		Assert.assertEquals(contentValues.size(), 1);
		Assert.assertEquals(contentValues.get(0).get(TB_KEY_DATA), dataEntityName);
		Assert.assertNotNull(contentValues.get(0).get(TB_KEY_TIMESTAMP));
		Assert.assertNotNull(contentValues.get(0).get(TB_KEY_UNIQUE_IDENTIFIER));

	}

	@Test
	public void testQueryDb_Failure() {
		//Prepare data
		final String dataEntityName = "dataentity";
		DataEntity dataEntity = new DataEntity(dataEntityName);
		Map<String, Object> row = new HashMap<>();
		row.put(TB_KEY_DATA, dataEntity.getData());
		row.put(TB_KEY_UNIQUE_IDENTIFIER, dataEntity.getUniqueIdentifier());
		row.put(TB_KEY_TIMESTAMP,  dataEntity.getTimestamp().getTime());

		String incorrectDbPath = "incorrect_db_path";
		//Action
		Assert.assertFalse(sqLiteDatabaseHelper.insertRow(incorrectDbPath, TABLE_NAME, row));

		List<ContentValues> contentValues = sqLiteDatabaseHelper.query(incorrectDbPath, TABLE_NAME, new String[] {TB_KEY_UNIQUE_IDENTIFIER, TB_KEY_TIMESTAMP, TB_KEY_DATA},
											1);

		//Assert
		Assert.assertTrue(contentValues.isEmpty());
	}

	@Test
	public void testGetTableSize_Success() {
		//Prepare data
		final String dataEntityName = "dataentity";
		DataEntity dataEntity = new DataEntity(dataEntityName);
		Map<String, Object> row = new HashMap<>();
		row.put(TB_KEY_DATA, dataEntity.getData());
		row.put(TB_KEY_UNIQUE_IDENTIFIER, dataEntity.getUniqueIdentifier());
		row.put(TB_KEY_TIMESTAMP,  dataEntity.getTimestamp().getTime());

		//Action
		Assert.assertTrue(sqLiteDatabaseHelper.insertRow(dbPath, TABLE_NAME, row));

		int tableSize = sqLiteDatabaseHelper.getTableSize(dbPath, TABLE_NAME);

		//Assert
		Assert.assertEquals(tableSize, 1);
	}

	@Test
	public void testGetTableSize_Failure() {
		//Prepare data
		final String dataEntityName = "dataentity";
		DataEntity dataEntity = new DataEntity(dataEntityName);
		Map<String, Object> row = new HashMap<>();
		row.put(TB_KEY_DATA, dataEntity.getData());
		row.put(TB_KEY_UNIQUE_IDENTIFIER, dataEntity.getUniqueIdentifier());
		row.put(TB_KEY_TIMESTAMP,  dataEntity.getTimestamp().getTime());


		final String incorrectDbPath = "incorrect_database_path";

		//Action
		Assert.assertFalse(sqLiteDatabaseHelper.insertRow(incorrectDbPath, TABLE_NAME, row));

		//Action
		Assert.assertEquals(sqLiteDatabaseHelper.getTableSize(incorrectDbPath, TABLE_NAME), 0);
	}

	@Test
	public void testRemoveRows_Success() {
		//Prepare data
		final String dataEntityName = "dataentity";
		DataEntity dataEntity = new DataEntity(dataEntityName);
		Map<String, Object> row = new HashMap<>();
		row.put(TB_KEY_DATA, dataEntity.getData());
		row.put(TB_KEY_UNIQUE_IDENTIFIER, dataEntity.getUniqueIdentifier());
		row.put(TB_KEY_TIMESTAMP,  dataEntity.getTimestamp().getTime());

		//Action
		Assert.assertTrue(sqLiteDatabaseHelper.insertRow(dbPath, TABLE_NAME, row));
		Assert.assertEquals(1, sqLiteDatabaseHelper.getTableSize(dbPath, TABLE_NAME));

		sqLiteDatabaseHelper.removeRows(dbPath, TABLE_NAME, "id", 1);

		int tableSize = sqLiteDatabaseHelper.getTableSize(dbPath, TABLE_NAME);

		//Assert
		Assert.assertEquals(tableSize, 0);
	}

	@Test
	public void testRemoveRows_Failure() {
		//Prepare data
		final String dataEntityName = "dataentity";
		DataEntity dataEntity = new DataEntity(dataEntityName);
		Map<String, Object> row = new HashMap<>();
		row.put(TB_KEY_DATA, dataEntity.getData());
		row.put(TB_KEY_UNIQUE_IDENTIFIER, dataEntity.getUniqueIdentifier());
		row.put(TB_KEY_TIMESTAMP,  dataEntity.getTimestamp().getTime());

		//Action
		Assert.assertTrue(sqLiteDatabaseHelper.insertRow(dbPath, TABLE_NAME, row));
		Assert.assertEquals(1, sqLiteDatabaseHelper.getTableSize(dbPath, TABLE_NAME));

		final String incorrectDbPath = "incorrect_db_path";

		Assert.assertEquals(-1, sqLiteDatabaseHelper.removeRows(incorrectDbPath, TABLE_NAME, "id", 1));
		Assert.assertEquals(1, sqLiteDatabaseHelper.getTableSize(dbPath, TABLE_NAME));
	}

	@Test
	public void testClearTable_Success() {
		//Prepare data
		final String dataEntityName = "dataentity";
		DataEntity dataEntity = new DataEntity(dataEntityName);
		Map<String, Object> row = new HashMap<>();
		row.put(TB_KEY_DATA, dataEntity.getData());
		row.put(TB_KEY_UNIQUE_IDENTIFIER, dataEntity.getUniqueIdentifier());
		row.put(TB_KEY_TIMESTAMP,  dataEntity.getTimestamp().getTime());

		//Action
		Assert.assertTrue(sqLiteDatabaseHelper.insertRow(dbPath, TABLE_NAME, row));
		Assert.assertEquals(1, sqLiteDatabaseHelper.getTableSize(dbPath, TABLE_NAME));

		sqLiteDatabaseHelper.clearTable(dbPath, TABLE_NAME);

		//Assert
		Assert.assertTrue(sqLiteDatabaseHelper.clearTable(dbPath, TABLE_NAME));
		Assert.assertEquals(0, sqLiteDatabaseHelper.getTableSize(dbPath, TABLE_NAME));
	}

	@Test
	public void testClearTable_Failure() {
		//Prepare data
		final String dataEntityName = "dataentity";
		DataEntity dataEntity = new DataEntity(dataEntityName);
		Map<String, Object> row = new HashMap<>();
		row.put(TB_KEY_DATA, dataEntity.getData());
		row.put(TB_KEY_UNIQUE_IDENTIFIER, dataEntity.getUniqueIdentifier());
		row.put(TB_KEY_TIMESTAMP,  dataEntity.getTimestamp().getTime());

		//Action
		Assert.assertTrue(sqLiteDatabaseHelper.insertRow(dbPath, TABLE_NAME, row));
		Assert.assertEquals(1, sqLiteDatabaseHelper.getTableSize(dbPath, TABLE_NAME));


		String incorrectDBPath = "incorrect_database_path";

		//Assert
		Assert.assertFalse(sqLiteDatabaseHelper.clearTable(incorrectDBPath, TABLE_NAME));
		Assert.assertEquals(1, sqLiteDatabaseHelper.getTableSize(dbPath, TABLE_NAME));
	}
}
