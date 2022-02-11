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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class HitQueueTest {
//	private static final String TEST_TABLE = "test_table";
//	private static final String TEST_DATABASE_FILE = "test_file.sqlite";
//	private static final String COL_ID = "ID";
//	private static final String COL_STRING = "STRING";
//	private static final String COL_INT = "INT";
//	private static final String COL_BOOL = "BOOL";
//	private FakePlatformServices fakePlatformServices;
//	private FakeDatabaseService fakeDatabaseService;
//	private HitQueue hitQueue;
//	private FakeHitDatabase fakeHitDatabase;
//	private FakeHitSchema fakeHitSchema;
//	private FakeHit testHit;
//
//	class FakeHitSchema extends AbstractHitSchema<FakeHit> {
//		private boolean generateDataMapWasCalled;
//		private boolean generateHitWasCalled;
//		private FakeHit generateDataMapParamHit;
//
//		FakeHitSchema() {
//			this.columnConstraints =
//				new ArrayList<List<DatabaseService.Database.ColumnConstraint>>();
//			List<DatabaseService.Database.ColumnConstraint> idColumnConstraints =
//				new ArrayList<DatabaseService.Database.ColumnConstraint>();
//			idColumnConstraints.add(DatabaseService.Database.ColumnConstraint.PRIMARY_KEY);
//			idColumnConstraints.add(DatabaseService.Database.ColumnConstraint.AUTOINCREMENT);
//			columnConstraints.add(idColumnConstraints);
//			columnConstraints.add(new ArrayList<DatabaseService.Database.ColumnConstraint>());
//			columnConstraints.add(new ArrayList<DatabaseService.Database.ColumnConstraint>());
//			columnConstraints.add(new ArrayList<DatabaseService.Database.ColumnConstraint>());
//
//			this.columnNames = new String[] {COL_ID, COL_STRING, COL_INT, COL_BOOL};
//
//			this.columnDataTypes = new DatabaseService.Database.ColumnDataType[] {
//				DatabaseService.Database.ColumnDataType.INTEGER,
//				DatabaseService.Database.ColumnDataType.TEXT,
//				DatabaseService.Database.ColumnDataType.INTEGER,
//				DatabaseService.Database.ColumnDataType.INTEGER
//			};
//		}
//
//		@Override
//		Map<String, Object> generateDataMap(final FakeHit hit) {
//			generateDataMapWasCalled = true;
//			generateDataMapParamHit = hit;
//			Map<String, Object> dataMap = new HashMap<String, Object>();
//			dataMap.put(COL_STRING, hit.stringParameter);
//			dataMap.put(COL_INT, hit.intParameter);
//			dataMap.put(COL_BOOL, hit.boolParameter);
//			return dataMap;
//		}
//
//		@Override
//		FakeHit generateHit(final DatabaseService.QueryResult queryResult) {
//			generateHitWasCalled = true;
//			FakeHit hit = new FakeHit();
//
//			try {
//				hit.stringParameter = queryResult.getString(1);
//				hit.intParameter = queryResult.getInt(2);
//				hit.boolParameter = queryResult.getInt(3) > 0;
//			} catch (Exception ex) {
//				return null;
//			}
//
//			return hit;
//		}
//	}
//
//	class FakeHit extends AbstractHit {
//		String stringParameter;
//		boolean boolParameter;
//		int intParameter;
//	}
//
//	class FakeHitDatabase implements HitQueue.IHitProcessor<FakeHit> {
//		boolean hitWasProcessed;
//
//		@Override
//		public HitQueue.RetryType process(FakeHit hit) {
//			hitWasProcessed = true;
//			return HitQueue.RetryType.NO;
//		}
//	}
//
//	@Before
//	public void setup() {
//		fakePlatformServices = new FakePlatformServices();
//		fakeDatabaseService = fakePlatformServices.fakeDatabaseService;
//		fakeHitDatabase = new FakeHitDatabase();
//		fakeHitSchema = new FakeHitSchema();
//		File dbFile = new File(TEST_DATABASE_FILE);
//		testHit = new FakeHit();
//		testHit.stringParameter = "testingString";
//		testHit.intParameter = 1000;
//		testHit.boolParameter = true;
//
//		hitQueue = new HitQueue(fakePlatformServices, dbFile, TEST_TABLE, fakeHitSchema, fakeHitDatabase);
//
//	}
//
//	@After
//	public void tearDown() {
//
//	}
//
//	// ===============================================================
//	// void initializeDatabase()
//	// ===============================================================
//	@Test
//	public void testInitializeDatabase_createsDatabase() {
//		hitQueue.initializeDatabase();
//		assertFalse(fakeDatabaseService.mapping.isEmpty());
//		assertNotNull(fakeDatabaseService.mapping.get(TEST_DATABASE_FILE));
//	}
//
//	// ===============================================================
//	// protected boolean queue(final T hit)
//	// ===============================================================
//	@Test
//	public void testQueue_InsertsHit_Happy() {
//		hitQueue.initializeDatabase();
//		assertTrue(hitQueue.queue(testHit));
//		assertTrue(fakeHitSchema.generateDataMapWasCalled);
//		assertEquals(testHit, fakeHitSchema.generateDataMapParamHit);
//	}
//
//	@Test
//	public void testQueue_When_NullHit_ReturnsFalse() {
//		hitQueue.initializeDatabase();
//		assertFalse(hitQueue.queue(null));
//		assertFalse(fakeHitSchema.generateDataMapWasCalled);
//	}
//
//	@Test
//	public void testQueue_When_DatabaseStatusIsFatal_ReturnsFalse() {
//		hitQueue.initializeDatabase();
//		hitQueue.databaseStatus = AbstractHitsDatabase.DatabaseStatus.FATAL_ERROR;
//		assertFalse(hitQueue.queue(testHit));
//		assertFalse(fakeHitSchema.generateDataMapWasCalled);
//	}
//
//	@Test
//	public void testQueue_When_DatabaseIsNull_ReturnsFalse() {
//		hitQueue.initializeDatabase();
//		hitQueue.database = null;
//		assertFalse(hitQueue.queue(testHit));
//		assertFalse(fakeHitSchema.generateDataMapWasCalled);
//	}
//
//	// ===============================================================
//	// T queryHit(final Query query)
//	// ===============================================================
//	@Test
//	public void testQueryHit_When_EmptyDatabase_ReturnsNull() {
//		hitQueue.initializeDatabase();
//		Query.Builder queryBuilder = new Query.Builder(TEST_TABLE, fakeHitSchema.getColumnNames());
//		queryBuilder.orderBy("ID ASC");
//		queryBuilder.limit("1");
//
//		final FakeHit result = (FakeHit) hitQueue.queryHit(queryBuilder.build());
//		assertNull(result);
//		assertFalse(fakeHitSchema.generateHitWasCalled);
//	}
//
//	@Test
//	public void testQueryHit_When_ValidDatabase_ReturnsHit() {
//		hitQueue.initializeDatabase();
//		hitQueue.queue(testHit);
//		Query.Builder queryBuilder = new Query.Builder(TEST_TABLE, fakeHitSchema.getColumnNames());
//		queryBuilder.orderBy("ID ASC");
//		queryBuilder.limit("1");
//
//		final FakeHit result = (FakeHit) hitQueue.queryHit(queryBuilder.build());
//		assertTrue(fakeHitSchema.generateHitWasCalled);
//		assertNotNull(result);
//	}
//
//	@Test
//	public void testQueryHit_When_NullQuery_ReturnsNull() {
//		final FakeHit result = (FakeHit) hitQueue.queryHit(null);
//		assertNull(result);
//		assertFalse(fakeHitSchema.generateHitWasCalled);
//	}
//
//	@Test
//	public void testQueryHit_When_NullDatabase_ReturnsNull() {
//		hitQueue.database = null;
//		Query.Builder queryBuilder = new Query.Builder(TEST_TABLE, fakeHitSchema.getColumnNames());
//		queryBuilder.orderBy("ID ASC");
//		queryBuilder.limit("1");
//
//		final FakeHit result = (FakeHit) hitQueue.queryHit(queryBuilder.build());
//		assertNull(result);
//		assertFalse(fakeHitSchema.generateHitWasCalled);
//	}
//
//	// ===============================================================
//	// T selectOldestHit()
//	// ===============================================================
//	@Test
//	public void testSelectOldestHit_When_EmptyDatabase_ReturnsNull() {
//		hitQueue.initializeDatabase();
//
//		final FakeHit result = (FakeHit) hitQueue.selectOldestHit();
//		assertNull(result);
//		assertFalse(fakeHitSchema.generateHitWasCalled);
//	}
//
//	@Test
//	public void testSelectOldestHit_When_ValidDatabase_ReturnsHit() {
//		hitQueue.initializeDatabase();
//		hitQueue.queue(testHit);
//
//		final FakeHit result = (FakeHit) hitQueue.selectOldestHit();
//		assertTrue(fakeHitSchema.generateHitWasCalled);
//		assertNotNull(result);
//	}
//
//	// ===============================================================
//	// void suspend()
//	// ===============================================================
//	@Test
//	public void testSuspend_Happy() {
//		assertFalse(hitQueue.isSuspended());
//		hitQueue.suspend();
//		assertTrue(hitQueue.isSuspended());
//	}
//
//	@Test
//	public void testSuspend_When_BringOnlineIsCalled_SuspendedFlagGetsResetted() {
//		assertFalse(hitQueue.isSuspended());
//		hitQueue.suspend();
//		hitQueue.bringOnline();
//		assertFalse(hitQueue.isSuspended());
//	}
//
//	// ===============================================================
//	// boolean updateAllHits(final Map<String, Object> parameters)
//	// ===============================================================
//	@Test
//	public void testUpdateAllHits_When_DatabaseIsNull_ReturnsFalse() {
//		hitQueue.database = null;
//		Map<String, String> data = new HashMap<String, String>();
//		assertFalse(hitQueue.updateAllHits(data));
//	}
//
//	@Test
//	public void testUpdateAllHits_When_DatabaseStatusIsFatal_ReturnsFalse() {
//		hitQueue.databaseStatus = AbstractHitsDatabase.DatabaseStatus.FATAL_ERROR;
//		Map<String, String> data = new HashMap<String, String>();
//		assertFalse(hitQueue.updateAllHits(data));
//	}
//
//	@Test
//	public void testUpdateAllHits_When_DatabaseIsEmpty_ReturnsTrue() {
//		hitQueue.initializeDatabase();
//		Map<String, String> data = new HashMap<String, String>();
//		assertTrue(hitQueue.updateAllHits(data));
//	}
//
//	@Test
//	public void testUpdateAllHits_When_DatabaseWithHits_Happy() {
//		hitQueue.initializeDatabase();
//		hitQueue.queue(testHit);
//		Map<String, String> data = new HashMap<String, String>();
//		data.put(COL_STRING, "overrideString");
//
//		assertTrue(hitQueue.updateAllHits(data));
//		FakeHit hit = (FakeHit) hitQueue.selectOldestHit();
//		assertEquals("overrideString", hit.stringParameter);
//	}
//
//	// ===============================================================
//	// boolean updateHit(final T hit)
//	// ===============================================================
//	@Test
//	public void testUpdateHit_When_DatabaseIsNull_ReturnsFalse() {
//		hitQueue.database = null;
//		FakeHit hit = new FakeHit();
//		hit.identifier = "1";
//		hit.stringParameter = "testValue";
//		assertFalse(hitQueue.updateHit(hit));
//	}
//
//	@Test
//	public void testUpdateHit_When_DatabaseStatusIsFatal_ReturnsFalse() {
//		hitQueue.databaseStatus = AbstractHitsDatabase.DatabaseStatus.FATAL_ERROR;
//		FakeHit hit = new FakeHit();
//		hit.identifier = "1";
//		hit.stringParameter = "testValue";
//		assertFalse(hitQueue.updateHit(hit));
//	}
//
//	@Test
//	public void testUpdateHit_When_DatabaseIsEmpty_ReturnsTrue() {
//		hitQueue.initializeDatabase();
//		FakeHit hit = new FakeHit();
//		hit.identifier = "1";
//		hit.stringParameter = "testValue";
//		assertTrue(hitQueue.updateHit(hit));
//	}
//
//	@Test
//	public void testUpdateHit_When_HitIdentifierIsNull_ReturnsFalse() {
//		hitQueue.initializeDatabase();
//		FakeHit hit = new FakeHit();
//		hit.identifier = null;
//		hit.stringParameter = "testValue";
//		assertFalse(hitQueue.updateHit(hit));
//	}
//
//	@Test
//	public void testUpdateHit_When_DatabaseWithHits_Happy() {
//		hitQueue.initializeDatabase();
//		hitQueue.queue(testHit);
//		FakeHit hit = new FakeHit();
//		hit.identifier = "1";
//		hit.stringParameter = "overrideString";
//
//		assertTrue(hitQueue.updateHit(hit));
//		FakeHit result = (FakeHit) hitQueue.selectOldestHit();
//		assertEquals("overrideString", result.stringParameter);
//	}
//
//	// ===============================================================
//	// void bringOnline()
//	// ===============================================================
//	@Test
//	public void testBringOnline_StartsProcessingTheHits() throws Exception {
//		hitQueue.initializeDatabase();
//		hitQueue.queue(testHit);
//		fakePlatformServices.mockSystemInfoService.networkConnectionStatus = SystemInfoService.ConnectionStatus.CONNECTED;
//		hitQueue.bringOnline();
//		Thread.sleep(1000);
//		assertTrue(fakeHitDatabase.hitWasProcessed);
//	}
//
//	@Test
//	public void testBringOnline_When_NotConnected_DoesNotProcessHits() throws Exception {
//		hitQueue.initializeDatabase();
//		hitQueue.queue(testHit);
//		fakePlatformServices.mockSystemInfoService.networkConnectionStatus = SystemInfoService.ConnectionStatus.DISCONNECTED;
//		hitQueue.bringOnline();
//		Thread.sleep(1000);
//		assertFalse(fakeHitDatabase.hitWasProcessed);
//	}
//
//	@Test
//	public void testBringOnline_When_NullDatabase_DoesNotFail() throws Exception {
//		hitQueue.database = null;
//		hitQueue.bringOnline();
//		Thread.sleep(1000);
//		assertFalse(fakeHitDatabase.hitWasProcessed);
//	}
}
