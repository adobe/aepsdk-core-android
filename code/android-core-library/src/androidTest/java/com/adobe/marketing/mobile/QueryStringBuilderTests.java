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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import java.util.ArrayList;
import java.util.List;

import static com.adobe.marketing.mobile.DatabaseService.Database.ColumnConstraint;
import static com.adobe.marketing.mobile.DatabaseService.Database.ColumnConstraint.AUTOINCREMENT;
import static com.adobe.marketing.mobile.DatabaseService.Database.ColumnConstraint.NOT_NULL;
import static com.adobe.marketing.mobile.DatabaseService.Database.ColumnConstraint.PRIMARY_KEY;
import static com.adobe.marketing.mobile.DatabaseService.Database.ColumnConstraint.UNIQUE;
import static com.adobe.marketing.mobile.DatabaseService.Database.ColumnDataType;
import static com.adobe.marketing.mobile.DatabaseService.Database.ColumnDataType.INTEGER;
import static com.adobe.marketing.mobile.DatabaseService.Database.ColumnDataType.REAL;
import static com.adobe.marketing.mobile.DatabaseService.Database.ColumnDataType.TEXT;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

@SuppressWarnings("all")
@RunWith(AndroidJUnit4.class)
public class QueryStringBuilderTests {

	@Before
	public void beforeEach() {
		Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
	}

	@After
	public void afterEach() {
	}

	@Test
	public void testGetCreateTableQueryString_Happy() throws Exception {
		String testTableName = "test_table";
		String[] columns = new String[] {"test_col0", "test_col1", "test_col2"};
		ColumnDataType[] columnDataTypes = new ColumnDataType[] {INTEGER, TEXT, REAL};
		List<List<ColumnConstraint>> columnConstraints = new ArrayList<List<ColumnConstraint>>() {
			{
				add(new ArrayList<ColumnConstraint>() {
					{
						add(PRIMARY_KEY);
						add(AUTOINCREMENT);
					}
				});
				add(new ArrayList<ColumnConstraint>() {
					{
						add(UNIQUE);
					}
				});
				add(new ArrayList<ColumnConstraint>() {
					{
						add(NOT_NULL);
					}
				});
			}
		};
		String createTableQuery = QueryStringBuilder.getCreateTableQueryString(testTableName, columns, columnDataTypes,
								  columnConstraints, false);
		assertEquals("CREATE TABLE IF NOT EXISTS test_table(test_col0 INTEGER PRIMARY KEY AUTOINCREMENT, test_col1 TEXT UNIQUE, test_col2 REAL NOT NULL)",
					 createTableQuery);
	}

	@Test
	public void testGetCreateTableQueryString_Happy_PartialConstraints() throws Exception {
		String testTableName = "test_table";
		String[] columns = new String[] {"test_col0", "test_col1", "test_col2"};
		ColumnDataType[] columnDataTypes = new ColumnDataType[] {INTEGER, TEXT, REAL};
		List<List<ColumnConstraint>> columnConstraints = new ArrayList<List<ColumnConstraint>>() {
			{
				add(new ArrayList<ColumnConstraint>() {
					{
						add(PRIMARY_KEY);
						add(AUTOINCREMENT);
					}
				});
				add(new ArrayList<ColumnConstraint>());
				add(new ArrayList<ColumnConstraint>() {
					{
						add(NOT_NULL);
					}
				});
			}
		};
		String createTableQuery = QueryStringBuilder.getCreateTableQueryString(testTableName, columns, columnDataTypes,
								  columnConstraints, false);
		assertEquals("CREATE TABLE IF NOT EXISTS test_table(test_col0 INTEGER PRIMARY KEY AUTOINCREMENT, test_col1 TEXT, test_col2 REAL NOT NULL)",
					 createTableQuery);
	}

	@Test
	public void testGetCreateTableQueryString_Happy_NullConstraints() throws Exception {
		String testTableName = "test_table";
		String[] columns = new String[] {"test_col0", "test_col1", "test_col2"};
		ColumnDataType[] columnDataTypes = new ColumnDataType[] {INTEGER, TEXT, REAL};
		List<List<ColumnConstraint>> columnConstraints = null;
		String createTableQuery = QueryStringBuilder.getCreateTableQueryString(testTableName, columns, columnDataTypes,
								  columnConstraints, false);
		assertEquals("CREATE TABLE IF NOT EXISTS test_table(test_col0 INTEGER, test_col1 TEXT, test_col2 REAL)",
					 createTableQuery);
	}

	@Test
	public void testGetCreateTableQueryString_EmptyConstraints() throws Exception {
		String testTableName = "test_table";
		String[] columns = new String[] {"test_col0", "test_col1", "test_col2"};
		ColumnDataType[] columnDataTypes = new ColumnDataType[] {INTEGER, TEXT, REAL};
		List<List<ColumnConstraint>> columnConstraints = new ArrayList<List<ColumnConstraint>>();
		String createTableQuery = QueryStringBuilder.getCreateTableQueryString(testTableName, columns, columnDataTypes,
								  columnConstraints, false);
		assertEquals("CREATE TABLE IF NOT EXISTS test_table(test_col0 INTEGER, test_col1 TEXT, test_col2 REAL)",
					 createTableQuery);
	}

	@Test
	public void testGetCreateTableQueryString_NullTableName() throws Exception {
		String testTableName = null;
		String[] columns = new String[] {"test_col0", "test_col1", "test_col2"};
		ColumnDataType[] columnDataTypes = new ColumnDataType[] {INTEGER, TEXT, REAL};
		List<List<ColumnConstraint>> columnConstraints = new ArrayList<List<ColumnConstraint>>() {
			{
				add(new ArrayList<ColumnConstraint>() {
					{
						add(PRIMARY_KEY);
						add(AUTOINCREMENT);
					}
				});
				add(new ArrayList<ColumnConstraint>() {
					{
						add(UNIQUE);
					}
				});
				add(new ArrayList<ColumnConstraint>() {
					{
						add(NOT_NULL);
					}
				});
			}
		};
		String createTableQuery = QueryStringBuilder.getCreateTableQueryString(testTableName, columns, columnDataTypes,
								  columnConstraints, false);
		assertNull(createTableQuery);
	}

	@Test
	public void testGetCreateTableQueryString_EmptyTableName() throws Exception {
		String testTableName = "";
		String[] columns = new String[] {"test_col0", "test_col1", "test_col2"};
		ColumnDataType[] columnDataTypes = new ColumnDataType[] {INTEGER, TEXT, REAL};
		List<List<ColumnConstraint>> columnConstraints = new ArrayList<List<ColumnConstraint>>() {
			{
				add(new ArrayList<ColumnConstraint>() {
					{
						add(PRIMARY_KEY);
						add(AUTOINCREMENT);
					}
				});
				add(new ArrayList<ColumnConstraint>() {
					{
						add(UNIQUE);
					}
				});
				add(new ArrayList<ColumnConstraint>() {
					{
						add(NOT_NULL);
					}
				});
			}
		};
		String createTableQuery = QueryStringBuilder.getCreateTableQueryString(testTableName, columns, columnDataTypes,
								  columnConstraints, false);
		assertNull(createTableQuery);
	}

	@Test
	public void testGetCreateTableQueryString_NullColumns() throws Exception {
		String testTableName = "test_table";
		String[] columns = null;
		ColumnDataType[] columnDataTypes = new ColumnDataType[] {INTEGER, TEXT, REAL};
		List<List<ColumnConstraint>> columnConstraints = new ArrayList<List<ColumnConstraint>>() {
			{
				add(new ArrayList<ColumnConstraint>() {
					{
						add(PRIMARY_KEY);
						add(AUTOINCREMENT);
					}
				});
				add(new ArrayList<ColumnConstraint>() {
					{
						add(UNIQUE);
					}
				});
				add(new ArrayList<ColumnConstraint>() {
					{
						add(NOT_NULL);
					}
				});
			}
		};
		String createTableQuery = QueryStringBuilder.getCreateTableQueryString(testTableName, columns, columnDataTypes,
								  columnConstraints, false);
		assertNull(createTableQuery);
	}

	@Test
	public void testGetCreateTableQueryString_EmptyColumns() throws Exception {
		String testTableName = "test_table";
		String[] columns = new String[] {};
		ColumnDataType[] columnDataTypes = new ColumnDataType[] {INTEGER, TEXT, REAL};
		List<List<ColumnConstraint>> columnConstraints = new ArrayList<List<ColumnConstraint>>() {
			{
				add(new ArrayList<ColumnConstraint>() {
					{
						add(PRIMARY_KEY);
						add(AUTOINCREMENT);
					}
				});
				add(new ArrayList<ColumnConstraint>() {
					{
						add(UNIQUE);
					}
				});
				add(new ArrayList<ColumnConstraint>() {
					{
						add(NOT_NULL);
					}
				});
			}
		};
		String createTableQuery = QueryStringBuilder.getCreateTableQueryString(testTableName, columns, columnDataTypes,
								  columnConstraints, false);
		assertNull(createTableQuery);
	}

	@Test
	public void testGetCreateTableQueryString_IncorrectNumDataTypes() throws Exception {
		String testTableName = "test_table";
		String[] columns = new String[] {"test_col0", "test_col1", "test_col2"};
		ColumnDataType[] columnDataTypes = new ColumnDataType[] {INTEGER, TEXT, REAL, INTEGER};
		List<List<ColumnConstraint>> columnConstraints = new ArrayList<List<ColumnConstraint>>() {
			{
				add(new ArrayList<ColumnConstraint>() {
					{
						add(PRIMARY_KEY);
						add(AUTOINCREMENT);
					}
				});
				add(new ArrayList<ColumnConstraint>() {
					{
						add(UNIQUE);
					}
				});
				add(new ArrayList<ColumnConstraint>() {
					{
						add(NOT_NULL);
					}
				});
			}
		};
		String createTableQuery = QueryStringBuilder.getCreateTableQueryString(testTableName, columns, columnDataTypes,
								  columnConstraints, false);
		assertNull(createTableQuery);
	}

	@Test
	public void testGetCreateTableQueryString_IncorrectNumConstraints() throws Exception {
		String testTableName = "test_table";
		String[] columns = new String[] {"test_col0", "test_col1", "test_col2"};
		ColumnDataType[] columnDataTypes = new ColumnDataType[] {INTEGER, TEXT, REAL};
		List<List<ColumnConstraint>> columnConstraints = new ArrayList<List<ColumnConstraint>>() {
			{
				add(new ArrayList<ColumnConstraint>() {
					{
						add(PRIMARY_KEY);
						add(AUTOINCREMENT);
					}
				});
				add(new ArrayList<ColumnConstraint>() {
					{
						add(UNIQUE);
					}
				});
				add(new ArrayList<ColumnConstraint>() {
					{
						add(UNIQUE);
						add(NOT_NULL);
					}
				});
				add(new ArrayList<ColumnConstraint>() {
					{
						add(NOT_NULL);
					}
				});
			}
		};
		String createTableQuery = QueryStringBuilder.getCreateTableQueryString(testTableName, columns, columnDataTypes,
								  columnConstraints, false);
		assertNull(createTableQuery);
	}

	@Test
	public void testGetCreateTableQueryString_AllNull() throws Exception {
		String testTableName = null;
		String[] columns = null;
		ColumnDataType[] columnDataTypes = null;
		List<List<ColumnConstraint>> columnConstraints = null;
		String createTableQuery = QueryStringBuilder.getCreateTableQueryString(testTableName, columns, columnDataTypes,
								  columnConstraints, false);
		assertNull(createTableQuery);
	}

	@Test
	public void testGetCreateTableQueryString_DefaultValue() throws Exception {
		String testTableName = "test_table";
		String[] columns = new String[] {"test_col0", "test_col1", "test_col2", "test_col3"};
		ColumnDataType[] columnDataTypes = new ColumnDataType[] {INTEGER, TEXT, REAL, INTEGER};
		List<List<ColumnConstraint>> columnConstraints = new ArrayList<List<ColumnConstraint>>() {
			{
				add(new ArrayList<ColumnConstraint>() {
					{
						add(PRIMARY_KEY);
						add(AUTOINCREMENT);
					}
				});
				add(new ArrayList<ColumnConstraint>() {
					{
						add(UNIQUE);
					}
				});
				add(new ArrayList<ColumnConstraint>() {
					{
						add(NOT_NULL);
					}
				});
				add(new ArrayList<ColumnConstraint>() {
					{
						add(NOT_NULL);
					}
				});
			}
		};
		String createTableQuery = QueryStringBuilder.getCreateTableQueryString(testTableName, columns, columnDataTypes,
								  columnConstraints, true);
		assertEquals("CREATE TABLE IF NOT EXISTS test_table(test_col0 INTEGER PRIMARY KEY AUTOINCREMENT, test_col1 TEXT UNIQUE DEFAULT '' , test_col2 REAL NOT NULL DEFAULT 0.0 , test_col3 INTEGER NOT NULL DEFAULT 0 )",
					 createTableQuery);
	}
}
