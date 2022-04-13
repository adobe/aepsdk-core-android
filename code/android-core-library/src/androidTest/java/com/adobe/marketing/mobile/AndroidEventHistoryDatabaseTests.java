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

import static com.adobe.marketing.mobile.DatabaseService.Database.ColumnConstraint.PRIMARY_KEY;
import static com.adobe.marketing.mobile.DatabaseService.Database.ColumnDataType.INTEGER;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import android.content.Context;
import android.database.DatabaseUtils;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.adobe.marketing.mobile.services.ServiceProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class AndroidEventHistoryDatabaseTests {
	private EventHistoryDatabase androidEventHistoryDatabase;

	@Before
	public void beforeEach() {
		Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
		ServiceProvider.getInstance().setContext(context);
		TestUtils.deleteAllFilesInCacheDir(context);

		try {
			androidEventHistoryDatabase = new AndroidEventHistoryDatabase();
		} catch (EventHistoryDatabaseCreationException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testCreateTable_Happy() {
		boolean success = androidEventHistoryDatabase.createTable(new String[] {"eventHash", "timestamp"},
						  new DatabaseService.Database.ColumnDataType[] {INTEGER, INTEGER},
		new ArrayList<List<DatabaseService.Database.ColumnConstraint>>() {
			{
				add(new ArrayList<DatabaseService.Database.ColumnConstraint>() {
					{
						add(PRIMARY_KEY);
					}
				});
				add(null);
			}
		});
		assertTrue(success);
	}

	@Test
	public void testInsertThenSelect_Happy() throws Exception {
		// test insert
		long startTimestamp = System.currentTimeMillis();
		assertTrue(androidEventHistoryDatabase.createTable(new String[] {"eventHash", "timestamp"}, new
				   DatabaseService.Database.ColumnDataType[] {INTEGER, INTEGER},
				   null));

		for (int i = 0; i < 10; i++) {
			assertTrue(androidEventHistoryDatabase.insert(1234567890 + i));
		}

		long endTimestamp = System.currentTimeMillis();

		// test select
		final DatabaseService.QueryResult result = androidEventHistoryDatabase.select(1234567890 + 5, 0,
				System.currentTimeMillis());
		// verify
		String count = result.getString(0);
		String oldest = result.getString(1);
		String newest = result.getString(2);
		assertEquals("1", count);
		assertTrue(TestUtils.almostEqual(Long.parseLong(oldest), startTimestamp, 1000));
		assertTrue(TestUtils.almostEqual(Long.parseLong(newest), endTimestamp, 1000));
	}

	@Test
	public void testInsertThenDelete_Happy() throws Exception {
		// test insert
		long startTimestamp = System.currentTimeMillis();
		assertTrue(androidEventHistoryDatabase.createTable(new String[] {"eventHash", "timestamp"}, new
				   DatabaseService.Database.ColumnDataType[] {INTEGER, INTEGER},
				   null));

		for (int i = 0; i < 15; i++) {
			assertTrue(androidEventHistoryDatabase.insert(1111111111));
		}

		for (int i = 0; i < 10; i++) {
			assertTrue(androidEventHistoryDatabase.insert(222222222));
		}

		long dbSize = DatabaseUtils.queryNumEntries(((AndroidEventHistoryDatabase) androidEventHistoryDatabase).getDatabase(),
					  "Events");
		assertEquals(25, dbSize);
		// test delete
		int deleteCount = androidEventHistoryDatabase.delete(1111111111, startTimestamp, System.currentTimeMillis());
		dbSize = DatabaseUtils.queryNumEntries(((AndroidEventHistoryDatabase) androidEventHistoryDatabase).getDatabase(),
											   "Events");
		assertEquals(15, deleteCount);
		assertEquals(10, dbSize);
	}
}