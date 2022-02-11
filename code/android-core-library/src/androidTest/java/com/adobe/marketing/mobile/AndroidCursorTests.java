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


import com.adobe.marketing.mobile.DatabaseService.Database.ColumnDataType;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import static com.adobe.marketing.mobile.DatabaseService.Database.ColumnDataType.INTEGER;
import static com.adobe.marketing.mobile.DatabaseService.Database.ColumnDataType.REAL;
import static com.adobe.marketing.mobile.DatabaseService.Database.ColumnDataType.TEXT;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@SuppressWarnings("all")
@RunWith(AndroidJUnit4.class)
public class AndroidCursorTests {

	@Rule
	public TestName          name              = new TestName();

	private DatabaseService androidDatabaseService;
	private DatabaseService.Database    androidDatabase;
	private String                            cacheDir;
	private DatabaseService.QueryResult queryResult;
	private Query testQuery;

	@Before
	public void beforeEach() {
		androidDatabaseService = new AndroidDatabaseService(null);
		androidDatabase = androidDatabaseService.openDatabase(TestUtils.getCacheDir(
							  InstrumentationRegistry.getInstrumentation().getTargetContext()) + "/" +
						  name.getMethodName());
	}

	@After
	public void afterEach() {
		if (queryResult != null) {
			queryResult.close();
		}

		TestUtils.deleteAllFilesInCacheDir(InstrumentationRegistry.getInstrumentation().getTargetContext());
	}

	@Test
	public void testGetCount_Happy() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									DatabaseService.Database.ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		final Random random = new Random();

		for (int i = 0; i < 10; i++) {
			androidDatabase.insert("test_table", new HashMap<String, Object>() {
				{
					put("test_col0", random.nextInt());
					put("test_col1", "string_" + UUID.randomUUID());
					put("test_col2", random.nextDouble());
				}
			});
		}

		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertEquals(10, queryResult.getCount());
	}

	@Test
	public void testGetCount_EmptyTable() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertEquals(0, queryResult.getCount());
	}

	@Test
	public void testGetInt_Happy() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 1234);
				put("test_col1", "string");
				put("test_col2", 5555.1245);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertTrue(queryResult.moveToFirst());
		assertEquals(1234, queryResult.getInt(0));
	}

	@Test
	public void testGetInt_IncorrectType() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 1234);
				put("test_col1", "string");
				put("test_col2", 5555.1245);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertTrue(queryResult.moveToFirst());
		assertEquals(0, queryResult.getInt(1));
	}

	@Test(expected = Exception.class)
	public void testGetInt_InvalidIndex() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 1234);
				put("test_col1", "string");
				put("test_col2", 5555.1245);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		queryResult.moveToFirst();
		queryResult.getInt(-1);
	}

	@Test(expected = Exception.class)
	public void testGetInt_IndexDoesNotExist() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 1234);
				put("test_col1", "string");
				put("test_col2", 5555.1245);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		queryResult.moveToFirst();
		queryResult.getInt(1000);
	}

	@Test
	public void testGetDouble_Happy() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 1234);
				put("test_col1", "string");
				put("test_col2", 5555.1245);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertTrue(queryResult.moveToFirst());
		assertEquals(5555.1245d, queryResult.getDouble(2));
	}

	@Test
	public void testGetDouble_IncorrectType() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 1234);
				put("test_col1", "string");
				put("test_col2", 5555.1245);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertTrue(queryResult.moveToFirst());
		assertEquals(0d, queryResult.getDouble(1));
	}

	@Test(expected = Exception.class)
	public void testGetDouble_InvalidIndex() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 1234);
				put("test_col1", "string");
				put("test_col2", 5555.1245);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		queryResult.moveToFirst();
		queryResult.getDouble(-1);
	}

	@Test(expected = Exception.class)
	public void testGetDouble_IndexDoesNotExist() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 1234);
				put("test_col1", "string");
				put("test_col2", 5555.1245);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		queryResult.moveToFirst();
		queryResult.getDouble(1000);
	}

	@Test
	public void testGetFloat_Happy() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 1234);
				put("test_col1", "string");
				put("test_col2", 5555.1245);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertTrue(queryResult.moveToFirst());
		assertEquals(5555.1245f, queryResult.getFloat(2));
	}

	@Test
	public void testGetFloat_IncorrectType() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 1234);
				put("test_col1", "string");
				put("test_col2", 5555.1245);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertTrue(queryResult.moveToFirst());
		assertEquals(0f, queryResult.getFloat(1));
	}

	@Test(expected = Exception.class)
	public void testGetFloat_InvalidIndex() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 1234);
				put("test_col1", "string");
				put("test_col2", 5555.1245);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		queryResult.moveToFirst();
		queryResult.getFloat(-1);
	}

	@Test(expected = Exception.class)
	public void testGetFloat_IndexDoesNotExist() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 1234);
				put("test_col1", "string");
				put("test_col2", 5555.1245);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		queryResult.moveToFirst();
		queryResult.getFloat(1000);
	}

	@Test
	public void testGetLong_Happy() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 1234);
				put("test_col1", "string");
				put("test_col2", 5555.1245);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertTrue(queryResult.moveToFirst());
		assertEquals(1234L, queryResult.getLong(0));
	}

	@Test
	public void testGetLong_IncorrectType() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 1234);
				put("test_col1", "string");
				put("test_col2", 5555.1245);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertTrue(queryResult.moveToFirst());
		assertEquals(0L, queryResult.getLong(1));
	}

	@Test(expected = Exception.class)
	public void testGetLong_InvalidIndex() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 1234);
				put("test_col1", "string");
				put("test_col2", 5555.1245);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		queryResult.moveToFirst();
		queryResult.getLong(-1);
	}

	@Test(expected = Exception.class)
	public void testGetLong_IndexDoesNotExist() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 1234);
				put("test_col1", "string");
				put("test_col2", 5555.1245);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		queryResult.moveToFirst();
		queryResult.getLong(1000);
	}

	@Test
	public void testGetString_Happy() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 1234);
				put("test_col1", "string");
				put("test_col2", 5555.1245);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertTrue(queryResult.moveToFirst());
		assertEquals("string", queryResult.getString(1));
	}

	@Test
	public void testGetString_IncorrectType() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 1234);
				put("test_col1", "string");
				put("test_col2", 5555.1245);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertTrue(queryResult.moveToFirst());
		assertEquals("1234", queryResult.getString(0));
	}

	@Test(expected = Exception.class)
	public void testGetString_InvalidIndex() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 1234);
				put("test_col1", "string");
				put("test_col2", 5555.1245);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		queryResult.moveToFirst();
		queryResult.getString(-1);
	}

	@Test(expected = Exception.class)
	public void testGetString_IndexDoesNotExist() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 1234);
				put("test_col1", "string");
				put("test_col2", 5555.1245);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		queryResult.moveToFirst();
		queryResult.getString(1000);
	}

	@Test
	public void testIsNull_Happy() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 1234);
				put("test_col1", null);
				put("test_col2", 5555.1245);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertTrue(queryResult.moveToFirst());
		assertTrue(queryResult.isNull(1));
	}

	@Test
	public void testIsNull_Happy_NotNull() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 1234);
				put("test_col1", "string");
				put("test_col2", 5555.1245);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertTrue(queryResult.moveToFirst());
		assertFalse(queryResult.isNull(1));
	}

	@Test
	public void testIsNull_DoubleType() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 1234);
				put("test_col1", "string");
				put("test_col2", null);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertTrue(queryResult.moveToFirst());
		assertEquals(0d, queryResult.getDouble(2));
		assertTrue(queryResult.isNull(2));
	}

	@Test
	public void testIsNull_InvalidIndex() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 1234);
				put("test_col1", "string");
				put("test_col2", null);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertTrue(queryResult.moveToFirst());
		assertTrue(queryResult.isNull(-1));
	}

	@Test
	public void testIsNull_IndexDoesNotExist() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 1234);
				put("test_col1", "string");
				put("test_col2", null);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertTrue(queryResult.moveToFirst());
		assertTrue(queryResult.isNull(1000));
	}

	@Test
	public void testMoveToFirst_Happy() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 1234);
				put("test_col1", "string");
				put("test_col2", 5555.1245);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertTrue(queryResult.moveToFirst());
		assertEquals(1234, queryResult.getInt(0));
	}

	@Test
	public void testMoveToFirst_EmptyTable() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertFalse(queryResult.moveToFirst());
	}

	@Test
	public void testMoveToLast_Happy() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 111);
				put("test_col1", "string0");
				put("test_col2", 100.1234);
			}
		});
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 222);
				put("test_col1", "string1");
				put("test_col2", 200.9876);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertTrue(queryResult.moveToLast());
		assertEquals(222, queryResult.getInt(0));
		assertEquals("string1", queryResult.getString(1));
		assertEquals(200.9876, queryResult.getDouble(2));
	}

	@Test
	public void testMoveToLast_EmptyTable() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertFalse(queryResult.moveToLast());
	}

	@Test
	public void testMoveToNext_Happy() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 111);
				put("test_col1", "string0");
				put("test_col2", 100.1234);
			}
		});
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 222);
				put("test_col1", "string1");
				put("test_col2", 200.9876);
			}
		});
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 333);
				put("test_col1", "string2");
				put("test_col2", 300.5678);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertTrue(queryResult.moveToNext());
		assertEquals(111, queryResult.getInt(0));
		assertEquals("string0", queryResult.getString(1));
		assertEquals(100.1234, queryResult.getDouble(2));
		assertTrue(queryResult.moveToNext());
		assertEquals(222, queryResult.getInt(0));
		assertEquals("string1", queryResult.getString(1));
		assertEquals(200.9876, queryResult.getDouble(2));
	}

	@Test
	public void testMoveToNext_EmptyTable() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertFalse(queryResult.moveToNext());
	}

	@Test
	public void testClose_Happy() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 1234);
				put("test_col1", "string");
				put("test_col2", 5555.1245);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		assertTrue(queryResult.moveToFirst());
		assertEquals(1234, queryResult.getInt(0));
		queryResult.close();
	}

	@Test(expected = Exception.class)
	public void testClose_GetDoubleAfterClose() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 1234);
				put("test_col1", "string");
				put("test_col2", 5555.1245d);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		queryResult.moveToFirst();
		queryResult.close();
		queryResult.getDouble(2);
	}

	@Test(expected = Exception.class)
	public void testClose_GetIntAfterClose() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 1234);
				put("test_col1", "string");
				put("test_col2", 5555.1245);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		queryResult.moveToFirst();
		queryResult.close();
		queryResult.getInt(0);
	}

	@Test(expected = Exception.class)
	public void testClose_GetStringAfterClose() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 1234);
				put("test_col1", "string");
				put("test_col2", 5555.1245);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		queryResult.moveToFirst();
		queryResult.close();
		queryResult.getString(1);
	}

	@Test(expected = Exception.class)
	public void testClose_GetFloatAfterClose() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 1234);
				put("test_col1", "string");
				put("test_col2", 5555.1245f);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		queryResult.moveToFirst();
		queryResult.close();
		queryResult.getFloat(2);
	}

	@Test(expected = Exception.class)
	public void testClose_GetLongAfterClose() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 1234L);
				put("test_col1", "string");
				put("test_col2", 5555.1245);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		queryResult.moveToFirst();
		queryResult.close();
		queryResult.getLong(0);
	}

	@Test(expected = Exception.class)
	public void testClose_MoveToFirstAfterClose() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 1234);
				put("test_col1", "string");
				put("test_col2", 5555.1245);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		queryResult.close();
		queryResult.moveToFirst();
	}

	@Test(expected = Exception.class)
	public void testClose_MoveToLastAfterClose() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 1234);
				put("test_col1", "string");
				put("test_col2", 5555.1245);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		queryResult.close();
		queryResult.moveToLast();
	}

	@Test(expected = Exception.class)
	public void testClose_MoveToNextAfterClose() throws Exception {
		androidDatabase.createTable("test_table", new String[] {"test_col0", "test_col1", "test_col2"}, new
									ColumnDataType[] {INTEGER, TEXT, REAL}, null);
		androidDatabase.insert("test_table", new HashMap<String, Object>() {
			{
				put("test_col0", 1234);
				put("test_col1", "string");
				put("test_col2", 5555.1245);
			}
		});
		testQuery = new Query.Builder("test_table", new String[] {"test_col0", "test_col1", "test_col2"}).build();
		queryResult = androidDatabase.query(testQuery);
		queryResult.close();
		queryResult.moveToNext();
	}

}
