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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.adobe.marketing.mobile.DatabaseService.Database.ColumnConstraint;
import static com.adobe.marketing.mobile.DatabaseService.Database.ColumnConstraint.AUTOINCREMENT;
import static com.adobe.marketing.mobile.DatabaseService.Database.ColumnConstraint.PRIMARY_KEY;
import static com.adobe.marketing.mobile.DatabaseService.Database.ColumnDataType;
import static com.adobe.marketing.mobile.DatabaseService.Database.ColumnDataType.INTEGER;
import static com.adobe.marketing.mobile.DatabaseService.Database.ColumnDataType.TEXT;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotEquals;

@SuppressWarnings("all")
@RunWith(AndroidJUnit4.class)
public class AndroidDatabaseTests {

	@Rule
	public TestName name = new TestName();

	private DatabaseService androidDatabaseService;
	private DatabaseService.Database androidDatabase;
	private DatabaseService.QueryResult queryResult;
	private Query testQuery;

	@Before
	public void beforeEach() {
		Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
		androidDatabaseService = new AndroidDatabaseService(null);
		androidDatabase = androidDatabaseService.openDatabase(TestUtils.getCacheDir(
							  InstrumentationRegistry.getInstrumentation().getTargetContext()) + "/" +
						  name.getMethodName());
	}

	@After
	public void afterEach() {
		TestUtils.deleteAllFilesInCacheDir(InstrumentationRegistry.getInstrumentation().getTargetContext());

		if (queryResult != null) {
			queryResult.close();
		}
	}

	@Test
	public void testCreateTable_Happy() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
		new ArrayList<List<ColumnConstraint>>() {
			{
				add(new ArrayList<ColumnConstraint>() {
					{
						add(PRIMARY_KEY);
						add(AUTOINCREMENT);
					}
				});
				add(null);
			}
		});
		assertTrue(success);
	}

	@Test
	public void testCreateTable_Migrate() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"z_test_col0", "y_test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
		new ArrayList<List<ColumnConstraint>>() {
			{
				add(new ArrayList<ColumnConstraint>() {
					{
						add(PRIMARY_KEY);
						add(AUTOINCREMENT);
					}
				});
				add(null);
			}
		});
		assertTrue(success);


		for (int i = 0; i < 10; i++) {
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("z_test_col0", i);
			values.put("y_test_col1", "string_" + i);
			assertTrue(androidDatabase.insert("test_table", values));
		}

		testQuery = new Query.Builder("test_table", new String[] {"z_test_col0", "y_test_col1"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;
		}

		assertEquals(10, numRows);

		boolean successNewTable = androidDatabase.createTable("test_table", new String[] {"z_test_col0", "y_test_col1", "x_test_col2"},
								  new
								  ColumnDataType[] {INTEGER, TEXT, TEXT},
		new ArrayList<List<ColumnConstraint>>() {
			{
				add(new ArrayList<ColumnConstraint>() {
					{
						add(PRIMARY_KEY);
						add(AUTOINCREMENT);
					}
				});
				add(null);
				add(null);
			}
		});
		assertTrue(successNewTable);

		testQuery = new Query.Builder("test_table", new String[] {"z_test_col0", "y_test_col1",  "x_test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRowsInNewTable = 0;

		while (queryResult.moveToNext()) {
			int c0 = queryResult.getInt(0);
			String c1 = queryResult.getString(1);
			String c2 = queryResult.getString(2);
			assertEquals(numRowsInNewTable, c0);
			assertEquals("string_" + numRowsInNewTable, c1);
			assertNull(c2);
			numRowsInNewTable++;
		}

		assertEquals(10, numRowsInNewTable);

	}


	@Test
	public void testCreateTable_Happy_NullConstraints() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
						  null);
		assertTrue(success);
	}

	@Test
	public void testCreateTable_EmptyConstraints() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
						  new ArrayList<List<ColumnConstraint>>());
		assertFalse(success);
	}

	@Test
	public void testCreateTable_Happy_EmptyConstraintsForEachColumn() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
		new ArrayList<List<ColumnConstraint>>() {
			{
				add(new ArrayList<ColumnConstraint>());
				add(new ArrayList<ColumnConstraint>());
			}
		});
		assertTrue(success);
	}

	@Test
	public void testCreateTable_NullTableName() throws Exception {
		boolean success = androidDatabase.createTable(null, new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
		new ArrayList<List<ColumnConstraint>>() {
			{
				add(new ArrayList<ColumnConstraint>() {
					{
						add(PRIMARY_KEY);
						add(AUTOINCREMENT);
					}
				});
				add(null);
			}
		});
		assertFalse(success);
	}

	@Test
	public void testCreateTable_EmptyTableName() throws Exception {
		boolean success = androidDatabase.createTable("", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
		new ArrayList<List<ColumnConstraint>>() {
			{
				add(new ArrayList<ColumnConstraint>() {
					{
						add(PRIMARY_KEY);
						add(AUTOINCREMENT);
					}
				});
				add(null);
			}
		});
		assertFalse(success);
	}

	@Test
	public void testCreateTable_NullColumns() throws Exception {
		boolean success = androidDatabase.createTable("test_table", null, new
						  ColumnDataType[] {INTEGER, TEXT},
		new ArrayList<List<ColumnConstraint>>() {
			{
				add(new ArrayList<ColumnConstraint>() {
					{
						add(PRIMARY_KEY);
						add(AUTOINCREMENT);
					}
				});
				add(null);
			}
		});
		assertFalse(success);
	}

	@Test
	public void testCreateTable_EmptyColumns() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {}, new
						  ColumnDataType[] {INTEGER, TEXT},
		new ArrayList<List<ColumnConstraint>>() {
			{
				add(new ArrayList<ColumnConstraint>() {
					{
						add(PRIMARY_KEY);
						add(AUTOINCREMENT);
					}
				});
				add(null);
			}
		});
		assertFalse(success);
	}

	@Test
	public void testCreateTable_NullDataTypes() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, null,
		new ArrayList<List<ColumnConstraint>>() {
			{
				add(new ArrayList<ColumnConstraint>() {
					{
						add(PRIMARY_KEY);
						add(AUTOINCREMENT);
					}
				});
				add(null);
			}
		});
		assertFalse(success);
	}

	@Test
	public void testCreateTable_EmptyDataTypes() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {},
		new ArrayList<List<ColumnConstraint>>() {
			{
				add(new ArrayList<ColumnConstraint>() {
					{
						add(PRIMARY_KEY);
						add(AUTOINCREMENT);
					}
				});
				add(null);
			}
		});
		assertFalse(success);
	}

	@Test
	public void testCreateTable_IncorrectNumDataTypes() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT, ColumnDataType.REAL},
		new ArrayList<List<ColumnConstraint>>() {
			{
				add(new ArrayList<ColumnConstraint>() {
					{
						add(PRIMARY_KEY);
						add(AUTOINCREMENT);
					}
				});
				add(null);
			}
		});
		assertFalse(success);
	}

	@Test
	public void testQuery_Happy() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
						  null);
		assertTrue(success);

		for (int i = 0; i < 10; i++) {
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("test_col0", i);
			values.put("test_col1", "string_" + UUID.randomUUID());
			androidDatabase.insert("test_table", values);
		}

		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1"}).selection("test_col0 < ?",
				new String[] {"5"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;
		}

		assertEquals(5, numRows);
	}

	@Test
	public void testQuery_NullTableName() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
						  null);
		assertTrue(success);

		for (int i = 0; i < 10; i++) {
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("test_col0", i);
			values.put("test_col1", "string_" + UUID.randomUUID());
			androidDatabase.insert("test_table", values);
		}

		testQuery = new Query.Builder(null, new String[] {"test_col0", "test_col1"}).selection("test_col0 < ?", new String[] {"5"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNull(queryResult);
	}

	@Test
	public void testQuery_EmptyTableName() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
						  null);
		assertTrue(success);

		for (int i = 0; i < 10; i++) {
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("test_col0", i);
			values.put("test_col1", "string_" + UUID.randomUUID());
			androidDatabase.insert("test_table", values);
		}

		testQuery = new Query.Builder("", new String[] {"test_col0", "test_col1"}).selection("test_col0 < ?", new String[] {"5"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNull(queryResult);
	}

	@Test
	public void testQuery_NullColumns() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
						  null);
		assertTrue(success);

		for (int i = 0; i < 10; i++) {
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("test_col0", i);
			values.put("test_col1", "string_" + UUID.randomUUID());
			androidDatabase.insert("test_table", values);
		}

		testQuery = new Query.Builder("test_table", null).selection("test_col0 < ?", new String[] {"5"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;
		}

		assertEquals(5, numRows);
	}

	@Test
	public void testQuery_EmptyColumns() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
						  null);
		assertTrue(success);

		for (int i = 0; i < 10; i++) {
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("test_col0", i);
			values.put("test_col1", "string_" + UUID.randomUUID());
			androidDatabase.insert("test_table", values);
		}

		testQuery = new Query.Builder("test_table", new String[] {}).selection("test_col0 < ?", new String[] {"5"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;
		}

		assertEquals(5, numRows);
	}

	@Test
	public void testQuery_NullSelection() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
						  null);
		assertTrue(success);

		for (int i = 0; i < 10; i++) {
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("test_col0", i);
			values.put("test_col1", "string_" + UUID.randomUUID());
			androidDatabase.insert("test_table", values);
		}

		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1"}).selection(null, new String[] {"5"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNull(queryResult);
	}

	@Test
	public void testQuery_EmptySelection() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
						  null);
		assertTrue(success);

		for (int i = 0; i < 10; i++) {
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("test_col0", i);
			values.put("test_col1", "string_" + UUID.randomUUID());
			androidDatabase.insert("test_table", values);
		}

		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1"}).selection("",  new String[] {"5"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNull(queryResult);
	}

	@Test
	public void testQuery_NullSelectionArgs() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
						  null);
		assertTrue(success);

		for (int i = 0; i < 10; i++) {
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("test_col0", i);
			values.put("test_col1", "string_" + UUID.randomUUID());
			androidDatabase.insert("test_table", values);
		}

		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1"}).selection("test_col0 < ?",
				null).build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;
		}

		assertEquals(0, numRows);
	}

	@Test
	public void testQuery_EmptySelectionArgs() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
						  null);
		assertTrue(success);

		for (int i = 0; i < 10; i++) {
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("test_col0", i);
			values.put("test_col1", "string_" + UUID.randomUUID());
			androidDatabase.insert("test_table", values);
		}

		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1"}).selection("test_col0 < ?",
				new String[] {}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;
		}

		assertEquals(0, numRows);
	}

	@Test
	public void testQuery_EmptyTable() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
						  null);
		assertTrue(success);
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1"}).selection("test_col0 < ?",
				new String[] {"5"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;
		}

		assertEquals(0, numRows);
	}

	@Test
	public void testQuery_TableNotExists() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
						  null);
		assertTrue(success);

		for (int i = 0; i < 10; i++) {
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("test_col0", i);
			values.put("test_col1", "string_" + UUID.randomUUID());
			androidDatabase.insert("test_table", values);
		}

		testQuery = new Query.Builder("test_table_not_exists", new String[] {"test_col0", "test_col1"}).selection("test_col0 < ?",
				new String[] {"5"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNull(queryResult);
	}

	@Test
	public void testQuery_Happy_GroupBy() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
						  ColumnDataType[] {INTEGER, TEXT, TEXT},
						  null);
		assertTrue(success);

		for (int i = 0; i < 10; i++) {
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("test_col0", i);
			values.put("test_col1", "string_" + UUID.randomUUID());
			values.put("test_col2", "group");
			androidDatabase.insert("test_table", values);
		}

		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).selection("test_col0 < ?",
				new String[] {"5"}).groupBy("test_col2").build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;
		}

		assertEquals(1, numRows);
	}

	@Test
	public void testQuery_EmptyGroupBy() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
						  null);
		assertTrue(success);

		for (int i = 0; i < 10; i++) {
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("test_col0", i);
			values.put("test_col1", "string_" + UUID.randomUUID());
			androidDatabase.insert("test_table", values);
		}

		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1"}).selection("test_col0 < ?",
				new String[] {"5"}).groupBy("").build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;
		}

		assertEquals(5, numRows);
	}

	@Test
	public void testQuery_Happy_EmptyHaving() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
						  null);
		assertTrue(success);

		for (int i = 0; i < 10; i++) {
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("test_col0", i);
			values.put("test_col1", "string_" + UUID.randomUUID());
			androidDatabase.insert("test_table", values);
		}

		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1"}).selection("test_col0 < ?",
				new String[] {"5"}).having("").build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;
		}

		assertEquals(5, numRows);
	}

	@Ignore
	@Test
	public void testQuery_Happy_Having() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
						  ColumnDataType[] {INTEGER, TEXT, TEXT},
						  null);
		assertTrue(success);
		List<Map<String, Object>> values = new ArrayList<Map<String, Object>>() {
			{
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 1);
						put("test_col1", "string0");
						put("test_col2", "group0");
					}
				});
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 1);
						put("test_col1", "string0");
						put("test_col2", "group1");
					}
				});
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 1);
						put("test_col1", "string1");
						put("test_col2", "group0");
					}
				});
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 1);
						put("test_col1", "string1");
						put("test_col2", "group1");
					}
				});
			}
		};

		for (Map value : values) {
			androidDatabase.insert("test_table", value);
		}

		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).groupBy("test_col2").having("test_col1 = 'string1'").build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;
		}

		assertEquals(2, numRows);
	}

	@Test
	public void testQuery_Having_NoGroupBy() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
						  ColumnDataType[] {INTEGER, TEXT, TEXT},
						  null);
		assertTrue(success);
		List<Map<String, Object>> values = new ArrayList<Map<String, Object>>() {
			{
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 1);
						put("test_col1", "string0");
						put("test_col2", "group0");
					}
				});
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 1);
						put("test_col1", "string0");
						put("test_col2", "group1");
					}
				});
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 1);
						put("test_col1", "string1");
						put("test_col2", "group0");
					}
				});
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 1);
						put("test_col1", "string1");
						put("test_col2", "group1");
					}
				});
			}
		};

		for (Map value : values) {
			androidDatabase.insert("test_table", value);
		}

		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).having("test_col1 = 'string1'").build();
		queryResult = androidDatabase.query(testQuery);
		assertNull(queryResult);
	}

	@Test
	public void testQuery_Happy_Limit() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
						  null);
		assertTrue(success);

		for (int i = 0; i < 10; i++) {
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("test_col0", i);
			values.put("test_col1", "string_" + UUID.randomUUID());
			androidDatabase.insert("test_table", values);
		}

		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1"}).selection("test_col0 < ?",
				new String[] {"5"}).limit("2").build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;
		}

		assertEquals(2, numRows);
	}

	@Test
	public void testQuery_Happy_EmptyLimit() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
						  null);
		assertTrue(success);

		for (int i = 0; i < 10; i++) {
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("test_col0", i);
			values.put("test_col1", "string_" + UUID.randomUUID());
			androidDatabase.insert("test_table", values);
		}

		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1"}).selection("test_col0 < ?",
				new String[] {"5"}).limit("").build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;
		}

		assertEquals(5, numRows);
	}


	//	long insert(String table,
	//				Map<String, Object> values);

	@Test
	public void testInsert_Happy() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
						  null);
		assertTrue(success);

		for (int i = 0; i < 10; i++) {
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("test_col0", i);
			values.put("test_col1", "string_" + UUID.randomUUID());
			assertTrue(androidDatabase.insert("test_table", values));
		}

		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;
		}

		assertEquals(10, numRows);
	}

	@Test
	public void testInsert_NullTableName() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
						  null);
		assertTrue(success);

		for (int i = 0; i < 10; i++) {
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("test_col0", i);
			values.put("test_col1", "string_" + UUID.randomUUID());
			assertFalse(androidDatabase.insert(null, values));
		}

		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;
		}

		assertEquals(0, numRows);
	}

	@Test
	public void testInsert_EmptyTableName() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
						  null);
		assertTrue(success);

		for (int i = 0; i < 10; i++) {
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("test_col0", i);
			values.put("test_col1", "string_" + UUID.randomUUID());
			assertFalse(androidDatabase.insert("", values));
		}

		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;
		}

		assertEquals(0, numRows);
	}

	@Test
	public void testInsert_TableNotExists() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
						  null);
		assertTrue(success);

		for (int i = 0; i < 10; i++) {
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("test_col0", i);
			values.put("test_col1", "string_" + UUID.randomUUID());
			assertFalse(androidDatabase.insert("test_table_not_exists", values));
		}

		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;
		}

		assertEquals(0, numRows);
	}

	@Test
	public void testInsert_NullValues() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
						  null);
		assertTrue(success);

		for (int i = 0; i < 10; i++) {
			assertFalse(androidDatabase.insert("test_table", null));
		}

		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;
		}

		assertEquals(0, numRows);
	}

	@Test
	public void testInsert_EmptyValues() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
						  null);
		assertTrue(success);

		for (int i = 0; i < 10; i++) {
			assertFalse(androidDatabase.insert("test_table", new HashMap<String, Object>()));
		}

		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;
		}

		assertEquals(0, numRows);
	}

	@Test
	public void testInsert_IncorrectColumnNames() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
						  null);
		assertTrue(success);

		for (int i = 0; i < 10; i++) {
			assertFalse(androidDatabase.insert("test_table", new HashMap<String, Object>() {
				{
					put("test_col_not_exists", "suh");
					put("test_col1", "dude");
				}
			}));
		}

		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;
		}

		assertEquals(0, numRows);
	}

	//todo fix expectation? android's sqlitedatabase api will insert rows with incorrect value types. in this case it uses the int value 0 for the column test_col0 when attempting to insert a string value
	//	@Test
	public void testInsert_IncorrectValueTypes() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
						  null);
		assertTrue(success);

		for (int i = 0; i < 10; i++) {
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("test_col0", "this_should_be_an_int_" + UUID.randomUUID());
			values.put("test_col1", "string_" + UUID.randomUUID());
			//			assertFalse(androidDatabase.insert("test_table", values));
			androidDatabase.insert("test_table", values);
		}

		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1"}).build();
		queryResult = androidDatabase.query(testQuery);

		while (queryResult.moveToNext()) {
			int testCol0Value = queryResult.getInt(0);
			String testCol1Value = queryResult.getString(1);
			Log.d("QUERY RESULT: ", "col0 = " + testCol0Value + " col1 = " + testCol1Value);
		}

		assertNull(queryResult);
	}

	@Test
	public void testUpdate_Happy() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
						  null);
		assertTrue(success);
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("test_col0", 0);
		values.put("test_col1", "string_" + UUID.randomUUID());
		assertTrue(androidDatabase.insert("test_table", values));
		Map<String, Object> newValues = new HashMap<>();
		newValues.put("test_col0", 1);
		newValues.put("test_col1", "newString");
		assertTrue(androidDatabase.update("test_table", newValues, null, null));
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;
		}

		assertEquals(1, numRows);
		queryResult.moveToFirst();
		assertEquals(1, queryResult.getInt(0));
		assertEquals("newString", queryResult.getString(1));
	}

	@Test
	public void testUpdate_NullTableName() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
						  null);
		assertTrue(success);
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("test_col0", 0);
		values.put("test_col1", "string_" + UUID.randomUUID());
		assertTrue(androidDatabase.insert("test_table", values));
		Map<String, Object> newValues = new HashMap<>();
		newValues.put("test_col0", 1);
		newValues.put("test_col1", "newString");
		assertFalse(androidDatabase.update(null, newValues, null, null));
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;
		}

		assertEquals(1, numRows);
		queryResult.moveToFirst();
		assertEquals(0, queryResult.getInt(0));
	}

	@Test
	public void testUpdate_EmptyTableName() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
						  null);
		assertTrue(success);
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("test_col0", 0);
		values.put("test_col1", "string_" + UUID.randomUUID());
		assertTrue(androidDatabase.insert("test_table", values));
		Map<String, Object> newValues = new HashMap<>();
		newValues.put("test_col0", 1);
		newValues.put("test_col1", "newString");
		assertFalse(androidDatabase.update("", newValues, null, null));
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;
		}

		assertEquals(1, numRows);
		queryResult.moveToFirst();
		assertEquals(0, queryResult.getInt(0));
	}

	@Test
	public void testUpdate_TableNotExists() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
						  null);
		assertTrue(success);
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("test_col0", 0);
		values.put("test_col1", "string_" + UUID.randomUUID());
		assertTrue(androidDatabase.insert("test_table", values));
		Map<String, Object> newValues = new HashMap<>();
		newValues.put("test_col0", 1);
		newValues.put("test_col1", "newString");
		assertFalse(androidDatabase.update("test_table_not_exists", newValues, null, null));
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;
		}

		assertEquals(1, numRows);
		queryResult.moveToFirst();
		assertEquals(0, queryResult.getInt(0));
	}

	@Test
	public void testUpdate_NullValues() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
						  null);
		assertTrue(success);
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("test_col0", 0);
		values.put("test_col1", "string_" + UUID.randomUUID());
		assertTrue(androidDatabase.insert("test_table", values));
		Map<String, Object> newValues = new HashMap<>();
		newValues.put("test_col0", 1);
		newValues.put("test_col1", "newString");
		assertFalse(androidDatabase.update("test_table", null, null, null));
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;
		}

		assertEquals(1, numRows);
		queryResult.moveToFirst();
		assertEquals(0, queryResult.getInt(0));
	}

	@Test
	public void testUpdate_EmptyValues() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
						  null);
		assertTrue(success);
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("test_col0", 0);
		values.put("test_col1", "string_" + UUID.randomUUID());
		assertTrue(androidDatabase.insert("test_table", values));
		Map<String, Object> newValues = new HashMap<>();
		assertFalse(androidDatabase.update("test_table", newValues, null, null));
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;
		}

		assertEquals(1, numRows);
		queryResult.moveToFirst();
		assertEquals(0, queryResult.getInt(0));
	}

	@Test
	public void testUpdate_IncorrectValues() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
						  null);
		assertTrue(success);
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("test_col0", "this_should_be_an_int");
		values.put("test_col1", "string_" + UUID.randomUUID());
		assertTrue(androidDatabase.insert("test_table", values));
		Map<String, Object> newValues = new HashMap<>();
		newValues.put("test_col0", 1);
		newValues.put("test_col1", "newString");
		assertTrue(androidDatabase.update("test_table", newValues, null, null));
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;
		}

		assertEquals(1, numRows);
		queryResult.moveToFirst();
		assertEquals(1, queryResult.getInt(0));
		assertEquals("newString", queryResult.getString(1));
	}

	@Test
	public void testUpdate_EmptyWhereClause() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
						  ColumnDataType[] {INTEGER, TEXT},
						  null);
		assertTrue(success);
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("test_col0", 0);
		values.put("test_col1", "string_" + UUID.randomUUID());
		assertTrue(androidDatabase.insert("test_table", values));
		Map<String, Object> newValues = new HashMap<>();
		newValues.put("test_col0", 1);
		newValues.put("test_col1", "newString");
		assertTrue(androidDatabase.update("test_table", newValues, "", null));
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;
		}

		assertEquals(1, numRows);
		queryResult.moveToFirst();
		assertEquals(1, queryResult.getInt(0));
		assertEquals("newString", queryResult.getString(1));
	}

	@Test
	public void testUpdate_Happy_WhereClause() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
						  ColumnDataType[] {INTEGER, TEXT, TEXT},
						  null);
		assertTrue(success);
		List<Map<String, Object>> values = new ArrayList<Map<String, Object>>() {
			{
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 0);
						put("test_col1", "string_" + UUID.randomUUID());
						put("test_col2", "group0");
					}
				});
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 1);
						put("test_col1", "string_" + UUID.randomUUID());
						put("test_col2", "group0");
					}
				});
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 2);
						put("test_col1", "string_" + UUID.randomUUID());
						put("test_col2", "group1");
					}
				});
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 3);
						put("test_col1", "string_" + UUID.randomUUID());
						put("test_col2", "group1");
					}
				});
			}
		};

		for (Map value : values) {
			assertTrue(androidDatabase.insert("test_table", value));
		}

		Map<String, Object> newValues = new HashMap<>();
		newValues.put("test_col1", "newString");
		assertTrue(androidDatabase.update("test_table", newValues, "test_col2 = ?", new String[] {"group0"}));
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;

			if (queryResult.getString(2).equals("group0")) {
				assertEquals("newString", queryResult.getString(1));
			}
		}

		assertEquals(4, numRows);
	}

	@Test
	public void testUpdate_NullWhereArgs() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
						  ColumnDataType[] {INTEGER, TEXT, TEXT},
						  null);
		assertTrue(success);
		List<Map<String, Object>> values = new ArrayList<Map<String, Object>>() {
			{
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 0);
						put("test_col1", "string_" + UUID.randomUUID());
						put("test_col2", "group0");
					}
				});
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 1);
						put("test_col1", "string_" + UUID.randomUUID());
						put("test_col2", "group0");
					}
				});
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 2);
						put("test_col1", "string_" + UUID.randomUUID());
						put("test_col2", "group1");
					}
				});
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 3);
						put("test_col1", "string_" + UUID.randomUUID());
						put("test_col2", "group1");
					}
				});
			}
		};

		for (Map value : values) {
			assertTrue(androidDatabase.insert("test_table", value));
		}

		Map<String, Object> newValues = new HashMap<>();
		newValues.put("test_col1", "newString");
		assertTrue(androidDatabase.update("test_table", newValues, "test_col2 = 'group0'", null));
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;

			if (queryResult.getString(2).equals("group0")) {
				assertEquals("newString", queryResult.getString(1));
			}
		}

		assertEquals(4, numRows);
	}

	@Test
	public void testUpdate_EmptyWhereArgs() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
						  ColumnDataType[] {INTEGER, TEXT, TEXT},
						  null);
		assertTrue(success);
		List<Map<String, Object>> values = new ArrayList<Map<String, Object>>() {
			{
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 0);
						put("test_col1", "string_" + UUID.randomUUID());
						put("test_col2", "group0");
					}
				});
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 1);
						put("test_col1", "string_" + UUID.randomUUID());
						put("test_col2", "group0");
					}
				});
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 2);
						put("test_col1", "string_" + UUID.randomUUID());
						put("test_col2", "group1");
					}
				});
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 3);
						put("test_col1", "string_" + UUID.randomUUID());
						put("test_col2", "group1");
					}
				});
			}
		};

		for (Map value : values) {
			assertTrue(androidDatabase.insert("test_table", value));
		}

		Map<String, Object> newValues = new HashMap<>();
		newValues.put("test_col1", "newString");
		assertFalse(androidDatabase.update("test_table", newValues, "test_col2 = ?", new String[] {}));
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;

			if (queryResult.getString(2).equals("group0")) {
				assertNotEquals("newString", queryResult.getString(1));
			}
		}

		assertEquals(4, numRows);
	}

	@Test
	public void testDelete_Happy() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
						  ColumnDataType[] {INTEGER, TEXT, TEXT},
						  null);
		assertTrue(success);
		List<Map<String, Object>> values = new ArrayList<Map<String, Object>>() {
			{
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 0);
						put("test_col1", "string_" + UUID.randomUUID());
						put("test_col2", "group0");
					}
				});
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 1);
						put("test_col1", "string_" + UUID.randomUUID());
						put("test_col2", "group0");
					}
				});
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 2);
						put("test_col1", "string_" + UUID.randomUUID());
						put("test_col2", "group1");
					}
				});
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 3);
						put("test_col1", "string_" + UUID.randomUUID());
						put("test_col2", "group1");
					}
				});
			}
		};

		for (Map value : values) {
			assertTrue(androidDatabase.insert("test_table", value));
		}

		assertTrue(androidDatabase.delete("test_table", "test_col2 = ?", new String[] {"group1"}));
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;
			assertNotEquals("group1", queryResult.getString(2));
		}

		assertEquals(2, numRows);
	}

	@Test
	public void testDelete_NullTableName() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
						  ColumnDataType[] {INTEGER, TEXT, TEXT},
						  null);
		assertTrue(success);
		List<Map<String, Object>> values = new ArrayList<Map<String, Object>>() {
			{
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 0);
						put("test_col1", "string_" + UUID.randomUUID());
						put("test_col2", "group0");
					}
				});
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 1);
						put("test_col1", "string_" + UUID.randomUUID());
						put("test_col2", "group0");
					}
				});
			}
		};

		for (Map value : values) {
			assertTrue(androidDatabase.insert("test_table", value));
		}

		assertFalse(androidDatabase.delete(null, "test_col2 = ?", new String[] {"group0"}));
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;

			if (queryResult.getString(2).equals("group0")) {
				assertNotEquals("newString", queryResult.getString(1));
			}
		}

		assertEquals(2, numRows);
	}

	@Test
	public void testDelete_EmptyTableName() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
						  ColumnDataType[] {INTEGER, TEXT, TEXT},
						  null);
		assertTrue(success);
		List<Map<String, Object>> values = new ArrayList<Map<String, Object>>() {
			{
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 0);
						put("test_col1", "string_" + UUID.randomUUID());
						put("test_col2", "group0");
					}
				});
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 1);
						put("test_col1", "string_" + UUID.randomUUID());
						put("test_col2", "group0");
					}
				});
			}
		};

		for (Map value : values) {
			assertTrue(androidDatabase.insert("test_table", value));
		}

		assertFalse(androidDatabase.delete("", "test_col2 = ?", new String[] {"group0"}));
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;
		}

		assertEquals(2, numRows);
	}

	@Test
	public void testDelete_TableNotExists() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
						  ColumnDataType[] {INTEGER, TEXT, TEXT},
						  null);
		assertTrue(success);
		List<Map<String, Object>> values = new ArrayList<Map<String, Object>>() {
			{
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 0);
						put("test_col1", "string_" + UUID.randomUUID());
						put("test_col2", "group0");
					}
				});
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 1);
						put("test_col1", "string_" + UUID.randomUUID());
						put("test_col2", "group0");
					}
				});
			}
		};

		for (Map value : values) {
			assertTrue(androidDatabase.insert("test_table", value));
		}

		assertFalse(androidDatabase.delete("test_table_not_exists", "test_col2 = ?", new String[] {"group0"}));
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;
		}

		assertEquals(2, numRows);
	}

	@Test
	public void testDelete_EmptyWhereClause() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
						  ColumnDataType[] {INTEGER, TEXT, TEXT},
						  null);
		assertTrue(success);
		List<Map<String, Object>> values = new ArrayList<Map<String, Object>>() {
			{
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 0);
						put("test_col1", "string_" + UUID.randomUUID());
						put("test_col2", "group0");
					}
				});
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 1);
						put("test_col1", "string_" + UUID.randomUUID());
						put("test_col2", "group1");
					}
				});
			}
		};

		for (Map value : values) {
			assertTrue(androidDatabase.insert("test_table", value));
		}

		assertTrue(androidDatabase.delete("test_table", "", null));
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;
		}

		assertEquals(0, numRows);
	}

	@Test
	public void testDelete_NullWhereArgs() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
						  ColumnDataType[] {INTEGER, TEXT, TEXT},
						  null);
		assertTrue(success);
		List<Map<String, Object>> values = new ArrayList<Map<String, Object>>() {
			{
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 0);
						put("test_col1", "string_" + UUID.randomUUID());
						put("test_col2", "group0");
					}
				});
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 1);
						put("test_col1", "string_" + UUID.randomUUID());
						put("test_col2", "group0");
					}
				});
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 2);
						put("test_col1", "string_" + UUID.randomUUID());
						put("test_col2", "group1");
					}
				});
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 3);
						put("test_col1", "string_" + UUID.randomUUID());
						put("test_col2", "group1");
					}
				});
			}
		};

		for (Map value : values) {
			assertTrue(androidDatabase.insert("test_table", value));
		}

		assertTrue(androidDatabase.delete("test_table", "test_col2 = ?", null));
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;
		}

		assertEquals(4, numRows);
	}

	@Test
	public void testDelete_EmptyWhereArgs() throws Exception {
		boolean success = androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
						  ColumnDataType[] {INTEGER, TEXT, TEXT},
						  null);
		assertTrue(success);
		List<Map<String, Object>> values = new ArrayList<Map<String, Object>>() {
			{
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 0);
						put("test_col1", "string_" + UUID.randomUUID());
						put("test_col2", "group0");
					}
				});
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 1);
						put("test_col1", "string_" + UUID.randomUUID());
						put("test_col2", "group0");
					}
				});
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 2);
						put("test_col1", "string_" + UUID.randomUUID());
						put("test_col2", "group1");
					}
				});
				add(new HashMap<String, Object>() {
					{
						put("test_col0", 3);
						put("test_col1", "string_" + UUID.randomUUID());
						put("test_col2", "group1");
					}
				});
			}
		};

		for (Map value : values) {
			assertTrue(androidDatabase.insert("test_table", value));
		}

		assertTrue(androidDatabase.delete("test_table", "test_col2 = ?", new String[] {}));
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertNotNull(queryResult);
		int numRows = 0;

		while (queryResult.moveToNext()) {
			numRows++;

			if (queryResult.getString(2).equals("group0")) {
				assertNotEquals("newString", queryResult.getString(1));
			}
		}

		assertEquals(4, numRows);
	}


	@Test
	public void testClose_Happy() throws Exception {
		androidDatabase.close();
	}

	@Test
	public void testDelete_FromEmptyTable() throws Exception {
		boolean tableCreationResult = androidDatabase.createTable("test_table", new String[] {"col0", "col1"},
		new ColumnDataType[] {TEXT, TEXT}, new ArrayList<List<ColumnConstraint>>() {
			{
				List<ColumnConstraint> col1Constraints = new ArrayList<>();
				List<ColumnConstraint> col2Constraints = new ArrayList<>();
				col1Constraints.add(ColumnConstraint.NOT_NULL);
				col2Constraints.add(ColumnConstraint.NOT_NULL);
				add(col1Constraints);
				add(col2Constraints);
			}
		});
		Assert.assertTrue(tableCreationResult);

		//Fetch all rows in table. Count should be 0.
		Query query = new Query.Builder("test_table", new String[] {"col0", "col1"}).selection(null, null).build();
		DatabaseService.QueryResult queryResult = androidDatabase.query(query);
		Assert.assertTrue("Table is expected to be empty.", queryResult.getCount() == 0);

		boolean emptyTableDeletionResult = androidDatabase.delete("test_table", null, null);
		Assert.assertTrue("Tried to delete empty table, result should be true", emptyTableDeletionResult);
	}

	// AMSDK-7648
	// This test should no longer receive an IllegalStateException because we now handle it
	// gracefully.
	@Test
	public void testQueryAfterClose() throws Exception {
		androidDatabase.close();
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
									ColumnDataType[] {INTEGER, TEXT},
		new ArrayList<List<ColumnConstraint>>() {
			{
				add(new ArrayList<ColumnConstraint>() {
					{
						add(PRIMARY_KEY);
						add(AUTOINCREMENT);
					}
				});
				add(null);
			}
		});
	}

}
