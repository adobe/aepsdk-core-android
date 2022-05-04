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

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

@SuppressWarnings("all")
@RunWith(AndroidJUnit4.class)
public class AndroidDatabaseServiceTests {

	private AndroidDatabaseService androidDatabaseService;
	private Context                appContext;

	@Before
	public void beforeEach() {
		appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
		androidDatabaseService = new AndroidDatabaseService();
		App.setAppContext(appContext);
	}

	@Test
	public void testOpenDatabase_Happy() throws Exception {
		assertNotNull(androidDatabaseService.openDatabase(appContext.getDatabasePath("testOpenDatabase_Happy").getPath()));
		appContext.getDatabasePath("testOpenDatabase_Happy").delete();
	}

	@Test
	public void testOpenDatabase_Exists() throws Exception {
		assertNotNull(androidDatabaseService.openDatabase(appContext.getDatabasePath("testOpenDatabase_Happy").getPath()));
		appContext.getDatabasePath("testOpenDatabase_Happy").delete();
	}

	@Test
	public void testOpenDatabase_NullFilePath() throws Exception {
		assertNull(androidDatabaseService.openDatabase(null));
	}

	@Test
	public void testOpenDatabase_EmptyFilePath() throws Exception {
		assertNull(androidDatabaseService.openDatabase(""));
	}

	@Test
	public void testDeleteDatabase_Happy() {
		assertNotNull(androidDatabaseService.openDatabase(appContext.getDatabasePath("testOpenDatabase_Happy").getPath()));
		assertTrue(androidDatabaseService.deleteDatabase(appContext.getDatabasePath("testOpenDatabase_Happy").getPath()));
	}

	@Test
	public void testDeleteDatabase_DoesNotExist() {
		assertFalse(androidDatabaseService.deleteDatabase(appContext.getDatabasePath("testDeleteDatabase_DoesNotExist").getPath()));
	}

	@Test
	public void testDeleteDatabase_NullFilePath() {
		assertFalse(androidDatabaseService.deleteDatabase(null));
	}

	@Test
	public void testDeleteDatabase_EmptyFilePath() {
		assertFalse(androidDatabaseService.deleteDatabase(""));
	}

	@Test
	public void testDeleteDatabase_RelativePathMixedWorkTheSameWhenMatch1() {

		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		androidDatabaseService = new AndroidDatabaseService();
		assertNull(androidDatabaseService.openDatabase(appContext.getDatabasePath("/invalid/file/path").getPath()));
	}

	//
	//	TEST_F(DatabaseServiceTest, OpenAndDeleteRelativePathComponentMixedWorkTheSameWhenNotMatch) {
	//		EXPECT_NE(nullptr, database_service_->OpenDatabase("mydatabase\\..\\database1")) << "Open at relative path";
	//		EXPECT_FALSE(database_service_->DeleteDatabase("mydatabase/../../database1")) << "Delete at relative path";
	//	}
	//
	//	TEST_F(DatabaseServiceTest, OpenAndDeleteRelativePathComponentMixedWorkTheSameWhenMatch) {
	//		EXPECT_NE(nullptr, database_service_->OpenDatabase("mydatabase\\..\\..\\database1")) << "Open at relative path";
	//		EXPECT_TRUE(database_service_->DeleteDatabase("mydatabase/../../database1")) << "Delete at relative path";
	//	}

}
