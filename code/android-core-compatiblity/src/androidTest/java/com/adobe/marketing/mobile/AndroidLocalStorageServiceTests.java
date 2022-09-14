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
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import com.adobe.marketing.mobile.services.internal.context.App;

@SuppressWarnings("all")
@RunWith(AndroidJUnit4.class)
public class AndroidLocalStorageServiceTests {
	private LocalStorageService androidLocalStorageService;
	private LocalStorageService.DataStore androidSharedPreferences;

	@Before
	public void beforeEach() {
		Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
		App.setAppContext(context.getApplicationContext());
		androidLocalStorageService = new AndroidLocalStorageService();
		androidSharedPreferences = androidLocalStorageService.getDataStore("AndroidLocalStorageServiceTests");
		androidSharedPreferences.removeAll();
	}

	@After
	public void afterEach() {
		androidSharedPreferences.removeAll();
	}

	@Test
	public void testConstructor_Happy() throws Exception {
		androidLocalStorageService = new AndroidLocalStorageService();
	}

	@Test
	public void testSetInt_Happy() throws Exception {
		androidSharedPreferences.setInt("testSetInt_Happy", 10);
		int value = androidSharedPreferences.getInt("testSetInt_Happy", 0);
		assertEquals(10, value);
	}

	@Test
	public void testSetInt_Overwrite() throws Exception {
		androidSharedPreferences.setInt("testSetInt_Overwrite", 10);
		androidSharedPreferences.setInt("testSetInt_Overwrite", 20);
		int value = androidSharedPreferences.getInt("testSetInt_Overwrite", 0);
		assertEquals(20, value);
	}

	@Test
	public void testGetInt_Happy() throws Exception {
		androidSharedPreferences.setInt("testGetInt_Happy", 10);
		int value = androidSharedPreferences.getInt("testGetInt_Happy", 0);
		assertEquals(10, value);
	}

	@Test
	public void testGetInt_EntryNotInSharedPreferences() throws Exception {
		int value = androidSharedPreferences.getInt("testGetInt_NotInSharedPrefs", 0);
		assertEquals(0, value);
	}

	@Test
	public void testSetString_Happy() throws Exception {
		androidSharedPreferences.setString("testSetString_Happy", "string");
		String value = androidSharedPreferences.getString("testSetString_Happy", null);
		assertEquals("string", value);
	}

	@Test
	public void testSetString_Overwrite() throws Exception {
		androidSharedPreferences.setString("testSetString_Overwrite", "string1");
		androidSharedPreferences.setString("testSetString_Overwrite", "string2");
		String value = androidSharedPreferences.getString("testSetString_Overwrite", null);
		assertEquals("string2", value);
	}

	@Test
	public void testGetString_Happy() throws Exception {
		androidSharedPreferences.setString("testGetString_Happy", "string");
		String value = androidSharedPreferences.getString("testGetString_Happy", null);
		assertEquals("string", value);
	}

	@Test
	public void testGetString_EntryNotInSharedPreferences() throws Exception {
		String value = androidSharedPreferences.getString("testGetString_NotInSharedPrefs", null);
		assertNull(value);
	}

	@Test
	public void testSetDouble_Happy() throws Exception {
		androidSharedPreferences.setDouble("testSetDouble_Happy", 10.1234d);
		double value = androidSharedPreferences.getDouble("testSetDouble_Happy", 0d);
		assertEquals(10.1234d, value);
	}

	@Test
	public void testSetDouble_Overwrite() throws Exception {
		androidSharedPreferences.setDouble("testSetDouble_Overwrite", 10.1234d);
		androidSharedPreferences.setDouble("testSetDouble_Overwrite", 20.5678d);
		double value = androidSharedPreferences.getDouble("testSetDouble_Overwrite", 0d);
		assertEquals(20.5678d, value);
	}

	@Test
	public void testGetDouble_Happy() throws Exception {
		androidSharedPreferences.setDouble("testGetDouble_Happy", 10.1234d);
		double value = androidSharedPreferences.getDouble("testGetDouble_Happy", 0d);
		assertEquals(10.1234d, value);
	}

	@Test
	public void testGetDouble_EntryNotInSharedPreferences() throws Exception {
		double value = androidSharedPreferences.getDouble("testGetDouble_EntryNotInSharedPrefs", 0);
		assertEquals(0d, value);
	}

	@Test
	public void testSetLong_Happy() throws Exception {
		androidSharedPreferences.setLong("testSetLong_Happy", 10L);
		long value = androidSharedPreferences.getLong("testSetLong_Happy", 0L);
		assertEquals(10L, value);
	}

	@Test
	public void testSetLong_Overwrite() throws Exception {
		androidSharedPreferences.setLong("testSetLong_Overwrite", 10L);
		androidSharedPreferences.setLong("testSetLong_Overwrite", 20L);
		long value = androidSharedPreferences.getLong("testSetLong_Overwrite", 0L);
		assertEquals(20L, value);
	}

	@Test
	public void testGetLong_Happy() throws Exception {
		androidSharedPreferences.setLong("testGetLong_Happy", 10L);
		long value = androidSharedPreferences.getLong("testGetLong_Happy", 0L);
		assertEquals(10L, value);
	}

	@Test
	public void testGetLong_EntryNotInSharedPreferences() throws Exception {
		long value = androidSharedPreferences.getLong("testGetLong_NotInSharedPrefs", 0L);
		assertEquals(0L, value);
	}

	@Test
	public void testSetFloat_Happy() throws Exception {
		androidSharedPreferences.setFloat("testSetFloat_Happy", 10.1234f);
		float value = androidSharedPreferences.getFloat("testSetFloat_Happy", 0f);
		assertEquals(10.1234f, value);
	}

	@Test
	public void testSetFloat_Overwrite() throws Exception {
		androidSharedPreferences.setFloat("testSetFloat_Overwrite", 10.1234f);
		androidSharedPreferences.setFloat("testSetFloat_Overwrite", 20.5678f);
		float value = androidSharedPreferences.getFloat("testSetFloat_Overwrite", 0f);
		assertEquals(20.5678f, value);
	}

	@Test
	public void testGetFloat_Happy() throws Exception {
		androidSharedPreferences.setFloat("testGetFloat_Happy", 10.1234f);
		float value = androidSharedPreferences.getFloat("testGetFloat_Happy", 0f);
		assertEquals(10.1234f, value);
	}

	@Test
	public void testGetFloat_EntryNotInSharedPreferences() throws Exception {
		float value = androidSharedPreferences.getFloat("testGetFloat_NotInSharedPrefs", 0f);
		assertEquals(0f, value);
	}

	@Test
	public void testSetBoolean_Happy() throws Exception {
		androidSharedPreferences.setBoolean("testSetBoolean_Happy", true);
		boolean value = androidSharedPreferences.getBoolean("testSetBoolean_Happy", false);
		assertTrue(value);
	}

	@Test
	public void testSetBoolean_Overwrite() throws Exception {
		androidSharedPreferences.setBoolean("testSetBoolean_Overwrite", false);
		androidSharedPreferences.setBoolean("testSetBoolean_Overwrite", true);
		boolean value = androidSharedPreferences.getBoolean("testSetBoolean_Overwrite", false);
		assertTrue(value);
	}

	@Test
	public void testGetBoolean_Happy() throws Exception {
		androidSharedPreferences.setBoolean("testGetBoolean_Happy", false);
		boolean value = androidSharedPreferences.getBoolean("testGetBoolean_Happy", true);
		assertFalse(value);
	}

	@Test
	public void testGetBoolean_EntryNotInSharedPreferences() throws Exception {
		boolean value = androidSharedPreferences.getBoolean("testGetBoolean_NotInSharedPrefs", true);
		assertTrue(value);
	}

	@Test
	public void testSetMap_Happy() throws Exception {
		Map testMap = new HashMap<String, String>() {
			{
				put("key", "value");
				put("key1", "value1");
			}
		};
		androidSharedPreferences.setMap("testSetMap_Happy", testMap);
		Map value = androidSharedPreferences.getMap("testSetMap_Happy");
		assertEquals(testMap, value);
	}

	@Test
	public void testSetMap_Overwrite() throws Exception {
		Map testMap = new HashMap<String, String>() {
			{
				put("key", "value");
				put("key1", "value1");
			}
		};
		Map testMap1 = new HashMap<String, String>() {
			{
				put("key2", "value2");
				put("key3", "value3");
			}
		};
		androidSharedPreferences.setMap("testSetMap_Overwrite", testMap);
		androidSharedPreferences.setMap("testSetMap_Overwrite", testMap1);
		Map value = androidSharedPreferences.getMap("testSetMap_Overwrite");
		assertEquals(testMap1, value);
	}

	@Test
	public void testGetMap_Happy() throws Exception {
		Map testMap = new HashMap<String, String>() {
			{
				put("key", "value");
				put("key1", "value1");
			}
		};
		androidSharedPreferences.setMap("testGetMap_Happy", testMap);
		Map value = androidSharedPreferences.getMap("testGetMap_Happy");
		assertEquals(testMap, value);
	}

	@Test
	public void testGetMap_EntryNotInSharedPreferences() throws Exception {
		Map value = androidSharedPreferences.getMap("testGetMap_NotInSharedPrefs");
		assertNull(value);
	}

	@Test
	public void testContains_Happy() throws Exception {
		androidSharedPreferences.setInt("testContains_Int", 10);
		androidSharedPreferences.setLong("testContains_Long", 5000L);
		androidSharedPreferences.setDouble("testContains_Double", 10.1234d);
		androidSharedPreferences.setFloat("testContains_Float", 5.6789f);
		androidSharedPreferences.setString("testContains_String", "testString");
		assertTrue(androidSharedPreferences.contains("testContains_Int"));
		assertTrue(androidSharedPreferences.contains("testContains_Long"));
		assertTrue(androidSharedPreferences.contains("testContains_Double"));
		assertTrue(androidSharedPreferences.contains("testContains_Float"));
		assertTrue(androidSharedPreferences.contains("testContains_String"));
	}

	@Test
	public void testContains_EntryNotInSharedPreferences() throws Exception {
		assertFalse(androidSharedPreferences.contains("testContains_Int_NotInSharedPrefs"));
		assertFalse(androidSharedPreferences.contains("testContains_Long_NotInSharedPrefs"));
		assertFalse(androidSharedPreferences.contains("testContains_Double_NotInSharedPrefs"));
		assertFalse(androidSharedPreferences.contains("testContains_Float_NotInSharedPrefs"));
		assertFalse(androidSharedPreferences.contains("testContains_String_NotInSharedPrefs"));
	}

	@Test
	public void testRemove_Happy() throws Exception {
		androidSharedPreferences.setInt("testRemove_Happy_Int", 10);
		androidSharedPreferences.remove("testRemove_Happy_Int");
		assertFalse(androidSharedPreferences.contains("testRemove_Happy_Int"));
	}

	@Test
	public void testRemove_EntryNotInSharedPreferences() throws Exception {
		androidSharedPreferences.remove("testRemove_Happy_NotInSharedPrefs");
		assertFalse(androidSharedPreferences.contains("testRemove_Happy_NotInSharedPrefs"));
	}

	@Test
	public void testRemoveAll_Happy() throws Exception {
		androidSharedPreferences.setInt("testRemoveAll_Int", 10);
		androidSharedPreferences.setLong("testRemoveAll_Long", 5000L);
		androidSharedPreferences.setDouble("testRemoveAll_Double", 10.1234d);
		androidSharedPreferences.setFloat("testRemoveAll_Float", 5.6789f);
		androidSharedPreferences.setString("testRemoveAll_String", "testString");
		androidSharedPreferences.removeAll();
		assertEquals(0, androidSharedPreferences.getInt("testRemoveAll_Int", 0));
		assertEquals(0L, androidSharedPreferences.getLong("testRemoveAll_Long", 0L));
		assertEquals(0d, androidSharedPreferences.getDouble("testRemoveAll_Double", 0d));
		assertEquals(0f, androidSharedPreferences.getFloat("testRemoveAll_Float", 0f));
		assertNull(androidSharedPreferences.getString("testRemoveAll_String", null));
	}

	@Test
	public void testRemoveAll_EmptySharedPreferences() throws Exception {
		androidSharedPreferences.removeAll();
	}

}
