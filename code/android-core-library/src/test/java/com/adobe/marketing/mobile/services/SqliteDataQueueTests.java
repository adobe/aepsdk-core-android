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
import android.database.sqlite.SQLiteException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;

import com.adobe.marketing.mobile.internal.utility.SQLiteDatabaseHelper;

@RunWith(MockitoJUnitRunner.class)
public class SqliteDataQueueTests {

	private DataQueue dataQueue;

	@Mock
	private SQLiteDatabaseHelper database;

	@Mock
	ContentValues contentValues;

	private static final String DATABASE_NAME = "test.sqlite";
	private static final String TABLE_NAME = "TB_AEP_DATA_ENTITY";
	private static final String EMPTY_JSON_STRING = "{}";
	private static final String TB_KEY_UNIQUE_IDENTIFIER = "uniqueIdentifier";
	private static final String TB_KEY_TIMESTAMP = "timestamp";
	private static final String TB_KEY_DATA = "data";

	public SqliteDataQueueTests() {
	}

	@Before
	public void setUp() {
		dataQueue = new SQLiteDataQueue(null, DATABASE_NAME, database);
	}


	@Test
	public void addDataEntitySuccess() {
		//Setup
		DataEntity dataEntity = new DataEntity(EMPTY_JSON_STRING);
		Mockito.when(database.insertRow(Mockito.anyString(), Mockito.anyString(),
										Mockito.<String, Object>anyMap())).thenReturn(true);

		//Actions
		boolean result = dataQueue.add(dataEntity);

		//Assertions
		assertTrue(result);

	}

	@Test
	public void addDataEntityFailure() {
		//Setup

		DataEntity dataEntity = new DataEntity(EMPTY_JSON_STRING);
		Mockito.when(database.insertRow(Mockito.anyString(), Mockito.anyString(),
										Mockito.<String, Object>anyMap())).thenReturn(false);

		//Action
		boolean result = dataQueue.add(dataEntity);

		//Assertions
		assertFalse(result);
	}

	@Test
	public void testPeekSuccess() {
		//Setup

		Mockito.when(contentValues.getAsString(TB_KEY_UNIQUE_IDENTIFIER)).thenReturn(TB_KEY_UNIQUE_IDENTIFIER);
		Mockito.when(contentValues.getAsLong(TB_KEY_TIMESTAMP)).thenReturn(System.currentTimeMillis());
		Mockito.when(contentValues.getAsString(TB_KEY_DATA)).thenReturn(EMPTY_JSON_STRING);
		Mockito.when(database.query(anyString(), anyString(), (String[]) Mockito.any(),
									anyInt())).thenReturn(Arrays.asList(contentValues));

		//Actions
		DataEntity dataEntity = dataQueue.peek();

		//Assertions
		assertTrue(dataEntity != null);

	}


	@Test
	public void testPeekNSuccess() {
		//Setup
		Mockito.when(contentValues.getAsString(TB_KEY_UNIQUE_IDENTIFIER)).thenReturn(TB_KEY_UNIQUE_IDENTIFIER);
		Mockito.when(contentValues.getAsLong(TB_KEY_TIMESTAMP)).thenReturn(System.currentTimeMillis());
		Mockito.when(contentValues.getAsString(TB_KEY_DATA)).thenReturn(EMPTY_JSON_STRING);
		Mockito.when(database.query(anyString(), anyString(), (String[]) Mockito.any(),
									anyInt())).thenReturn(Arrays.asList(contentValues, contentValues));

		//Actions
		List<DataEntity> dataEntityList = dataQueue.peek(2);

		//Assertions
		assertEquals(2, dataEntityList.size());

	}

	@Test
	public void testRemoveRows() {
		//setup
		Mockito.when(database.removeRows(anyString(), anyString(), anyString(), anyInt())).thenReturn(1);

		//Action
		boolean result = dataQueue.remove();

		//Assertions
		assertTrue(result);
	}

	@Test
	public void testRemoveNRows() {
		//Setup
		Mockito.when(database.removeRows(anyString(), anyString(), anyString(), anyInt())).thenReturn(1);

		//Actions
		boolean result = dataQueue.remove(1);

		//Assertions
		assertTrue(result);
	}

	@Test
	public void testClearTable() {
		//etup
		Mockito.when(database.clearTable(anyString(), anyString())).thenReturn(true);

		//Actions
		boolean result = database.clearTable(DATABASE_NAME, TABLE_NAME);

		//Assertions
		assertTrue(result);
	}

	@Test
	public void testTableCount() {
		//Setup
		final int mockedTableSize = 10;
		Mockito.when(database.getTableSize(anyString(), anyString())).thenReturn(mockedTableSize);

		//Actions
		int tableSize = database.getTableSize(DATABASE_NAME, TABLE_NAME);

		//Assertions
		assertEquals(tableSize, mockedTableSize);
	}

	@Test
	public void testClose() {
		//Actions
		dataQueue.close();

		//Assertions
		assertFalse(dataQueue.add(new DataEntity(EMPTY_JSON_STRING)));
		assertNull(dataQueue.peek());
		assertNull(dataQueue.peek(10));
		assertFalse(dataQueue.remove());
		assertFalse(dataQueue.remove(10));
		assertFalse(dataQueue.clear());
		assertEquals(dataQueue.count(), 0);
	}

	//Unit test failure in opening database in different scenarios.

	@Test
	public void addDataEntityWithDatabaseOpenError() {

		Mockito.when(database.insertRow(anyString(), anyString(),
										ArgumentMatchers.<String, Object>anyMap())).thenCallRealMethod();
		Mockito.when(database.openDatabase(DATABASE_NAME,
										   SQLiteDatabaseHelper.DatabaseOpenMode.READ_WRITE)).thenThrow(SQLiteException.class);

		boolean result = dataQueue.add(new DataEntity(EMPTY_JSON_STRING));

		//Assertions
		Assert.assertFalse(result);
	}

	@Test
	public void peekNWithDatabaseOpenError() {

		Mockito.when(database.removeRows(anyString(), anyString(), anyString(), anyInt())).thenCallRealMethod();
		Mockito.when(database.openDatabase(DATABASE_NAME,
										   SQLiteDatabaseHelper.DatabaseOpenMode.READ_WRITE)).thenThrow(SQLiteException.class);

		boolean result = dataQueue.remove(2);

		//Assertions
		Assert.assertFalse(result);
	}

	@Test
	public void clearTableWithDatabaseOpenError() {

		Mockito.when(database.clearTable(anyString(), anyString())).thenCallRealMethod();
		Mockito.when(database.openDatabase(DATABASE_NAME,
										   SQLiteDatabaseHelper.DatabaseOpenMode.READ_WRITE)).thenThrow(SQLiteException.class);

		boolean result = dataQueue.clear();

		//Assertions
		Assert.assertFalse(result);
	}

	@Test
	public void getTableSizeWithDatabaseOpenError() {

		Mockito.when(database.getTableSize(anyString(), anyString())).thenCallRealMethod();
		Mockito.when(database.openDatabase(DATABASE_NAME,
										   SQLiteDatabaseHelper.DatabaseOpenMode.READ_ONLY)).thenThrow(SQLiteException.class);

		int result = dataQueue.count();

		//Assertions
		Assert.assertEquals(result, 0);
	}
}
