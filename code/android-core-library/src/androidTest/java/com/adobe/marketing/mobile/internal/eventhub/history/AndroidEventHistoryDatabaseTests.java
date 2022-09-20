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

package com.adobe.marketing.mobile.internal.eventhub.history;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.adobe.marketing.mobile.TestUtils;
import com.adobe.marketing.mobile.internal.util.SQLiteDatabaseHelper;
import com.adobe.marketing.mobile.services.ServiceProvider;
import com.adobe.marketing.mobile.services.internal.context.App;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AndroidEventHistoryDatabaseTests {
	private EventHistoryDatabase androidEventHistoryDatabase;

	@Before
	public void beforeEach() {
		Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
		App.INSTANCE.setAppContext(context);
		TestUtils.deleteAllFilesInCacheDir(context);

		try {
			androidEventHistoryDatabase = new AndroidEventHistoryDatabase();
		} catch (EventHistoryDatabaseCreationException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testInsertThenSelect_Happy(){
		// test insert
		long startTimestamp = System.currentTimeMillis();

		for (int i = 0; i < 10; i++) {
			assertTrue(androidEventHistoryDatabase.insert(1234567890 + i));
		}

		long endTimestamp = System.currentTimeMillis();

		// test select
		final Cursor cursor = androidEventHistoryDatabase.select(1234567890 + 5, 0,
				System.currentTimeMillis());
		// verify
		String count = cursor.getString(0);
		String oldest = cursor.getString(1);
		String newest = cursor.getString(2);
		assertEquals("1", count);
		assertTrue(TestUtils.almostEqual(Long.parseLong(oldest), startTimestamp, 1000));
		assertTrue(TestUtils.almostEqual(Long.parseLong(newest), endTimestamp, 1000));
	}

	@Test
	public void testInsertThenDelete_Happy(){
		// test insert
		long startTimestamp = System.currentTimeMillis();

		for (int i = 0; i < 15; i++) {
			assertTrue(androidEventHistoryDatabase.insert(1111111111));
		}

		for (int i = 0; i < 10; i++) {
			assertTrue(androidEventHistoryDatabase.insert(222222222));
		}

		String dbPath = "/data/data/com.adobe.marketing.mobile.test/cache/com.adobe.marketing.db.eventhistory";
		SQLiteDatabase database = SQLiteDatabaseHelper.openDatabase(dbPath, SQLiteDatabaseHelper.DatabaseOpenMode.READ_WRITE);
		long dbSize = DatabaseUtils.queryNumEntries(database,"Events");
		SQLiteDatabaseHelper.closeDatabase(database);
		assertEquals(25, dbSize);
		// test delete
		int deleteCount = androidEventHistoryDatabase.delete(1111111111, startTimestamp, System.currentTimeMillis());
		database = SQLiteDatabaseHelper.openDatabase(dbPath, SQLiteDatabaseHelper.DatabaseOpenMode.READ_WRITE);
		dbSize = DatabaseUtils.queryNumEntries(database, "Events");
		SQLiteDatabaseHelper.closeDatabase(database);
		assertEquals(15, deleteCount);
		assertEquals(10, dbSize);
	}
}