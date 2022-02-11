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

import org.junit.Test;

import java.util.HashMap;

import static com.adobe.marketing.mobile.DatabaseService.Database.ColumnDataType.*;

/**
 * Created by jgeng on 2/28/17.
 */
public class DataBaseTest {
//	@Test
//	public void testdb() {
//		FakeDatabase mockDatabase = new FakeDatabase();
//
//		try {
//			mockDatabase.createTable("test_table", new String[] {"test_col0", "test_col1"}, new
//									 DatabaseService.Database.ColumnDataType[] {INTEGER, TEXT},
//									 null);
//			mockDatabase.insert("test_table", new HashMap<String, Object>() {
//				{
//					put("test_col0", 3);
//					put("test_col1", "123");
//				}
//			});
//			mockDatabase.insert("test_table", new HashMap<String, Object>() {
//				{
//					put("test_col0", 1);
//					put("test_col1", "abc");
//				}
//			});
//			mockDatabase.update("test_table", new HashMap<String, Object>() {
//				{
//					put("test_col0", 4);
//					put("test_col1", "456");
//				}
//			}, "WHERE test_col0=3", null);
//			Query testQuery = new Query.Builder("test_table", new String[] {"test_col1"}).build();
//			DatabaseService.QueryResult result = mockDatabase.query(testQuery);
//			mockDatabase.delete("test_table", "WHERE test_col0=4", null);
//			result = mockDatabase.query(testQuery);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
}
