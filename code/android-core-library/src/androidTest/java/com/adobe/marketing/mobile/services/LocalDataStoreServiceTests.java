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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LocalDataStoreServiceTests {

    private NamedCollection sharedPreferencesNamedCollection;
    private MockAppContextService mockAppContextService;

    @Before
    public void beforeEach() {
        mockAppContextService = new MockAppContextService();
        mockAppContextService.appContext = ApplicationProvider.getApplicationContext();
        ServiceProviderModifier.setAppContextService(mockAppContextService);

        this.sharedPreferencesNamedCollection =
                new LocalDataStoreService().getNamedCollection("AndroidLocalStorageServiceTests");
        this.sharedPreferencesNamedCollection.removeAll();
    }

    @After
    public void afterEach() {
        this.sharedPreferencesNamedCollection.removeAll();
    }

    @Test
    public void testCollectionNameIsNullOrEmpty() {
        NamedCollection namedCollection = new LocalDataStoreService().getNamedCollection("");
        assertNull(namedCollection);
        namedCollection = new LocalDataStoreService().getNamedCollection(null);
        assertNull(namedCollection);
    }

    @Test
    public void testApplicationContextIsNotSet() {
        mockAppContextService.appContext = null;
        NamedCollection namedCollection =
                new LocalDataStoreService().getNamedCollection("AndroidLocalStorageServiceTests");
        assertNull(namedCollection);
    }

    @Test
    public void testSetGetInt_Happy() throws Exception {
        this.sharedPreferencesNamedCollection.setInt("testSetInt_Happy", 10);
        int value = this.sharedPreferencesNamedCollection.getInt("testSetInt_Happy", 0);
        assertEquals(10, value);
    }

    @Test
    public void testSetInt_Overwrite() throws Exception {
        this.sharedPreferencesNamedCollection.setInt("testSetInt_Overwrite", 10);
        this.sharedPreferencesNamedCollection.setInt("testSetInt_Overwrite", 20);
        int value = this.sharedPreferencesNamedCollection.getInt("testSetInt_Overwrite", 0);
        assertEquals(20, value);
    }

    @Test
    public void testGetInt_EntryNotInSharedPreferences() throws Exception {
        int value = this.sharedPreferencesNamedCollection.getInt("testGetInt_NotInSharedPrefs", 0);
        assertEquals(0, value);
    }

    @Test
    public void testSetGetString_Happy() throws Exception {
        this.sharedPreferencesNamedCollection.setString("testSetString_Happy", "string");
        String value = this.sharedPreferencesNamedCollection.getString("testSetString_Happy", null);
        assertEquals("string", value);
    }

    @Test
    public void testSetString_Overwrite() throws Exception {
        this.sharedPreferencesNamedCollection.setString("testSetString_Overwrite", "string1");
        this.sharedPreferencesNamedCollection.setString("testSetString_Overwrite", "string2");
        String value =
                this.sharedPreferencesNamedCollection.getString("testSetString_Overwrite", null);
        assertEquals("string2", value);
    }

    @Test
    public void testGetString_EntryNotInSharedPreferences() throws Exception {
        String value =
                this.sharedPreferencesNamedCollection.getString(
                        "testGetString_NotInSharedPrefs", null);
        assertNull(value);
    }

    @Test
    public void testSetGetDouble_Happy() throws Exception {
        this.sharedPreferencesNamedCollection.setDouble("testSetDouble_Happy", 10.1234d);
        double value = this.sharedPreferencesNamedCollection.getDouble("testSetDouble_Happy", 0d);
        assertEquals(10.1234d, value);
    }

    @Test
    public void testSetDouble_Overwrite() throws Exception {
        this.sharedPreferencesNamedCollection.setDouble("testSetDouble_Overwrite", 10.1234d);
        this.sharedPreferencesNamedCollection.setDouble("testSetDouble_Overwrite", 20.5678d);
        double value =
                this.sharedPreferencesNamedCollection.getDouble("testSetDouble_Overwrite", 0d);
        assertEquals(20.5678d, value);
    }

    @Test
    public void testGetDouble_EntryNotInSharedPreferences() throws Exception {
        double value =
                this.sharedPreferencesNamedCollection.getDouble(
                        "testGetDouble_EntryNotInSharedPrefs", 0);
        assertEquals(0d, value);
    }

    @Test
    public void testSetGetLong_Happy() throws Exception {
        this.sharedPreferencesNamedCollection.setLong("testSetLong_Happy", 10L);
        long value = this.sharedPreferencesNamedCollection.getLong("testSetLong_Happy", 0L);
        assertEquals(10L, value);
    }

    @Test
    public void testSetLong_Overwrite() throws Exception {
        this.sharedPreferencesNamedCollection.setLong("testSetLong_Overwrite", 10L);
        this.sharedPreferencesNamedCollection.setLong("testSetLong_Overwrite", 20L);
        long value = this.sharedPreferencesNamedCollection.getLong("testSetLong_Overwrite", 0L);
        assertEquals(20L, value);
    }

    @Test
    public void testGetLong_EntryNotInSharedPreferences() throws Exception {
        long value =
                this.sharedPreferencesNamedCollection.getLong("testGetLong_NotInSharedPrefs", 0L);
        assertEquals(0L, value);
    }

    @Test
    public void testSetGetFloat_Happy() throws Exception {
        this.sharedPreferencesNamedCollection.setFloat("testSetFloat_Happy", 10.1234f);
        float value = this.sharedPreferencesNamedCollection.getFloat("testSetFloat_Happy", 0f);
        assertEquals(10.1234f, value);
    }

    @Test
    public void testSetFloat_Overwrite() throws Exception {
        this.sharedPreferencesNamedCollection.setFloat("testSetFloat_Overwrite", 10.1234f);
        this.sharedPreferencesNamedCollection.setFloat("testSetFloat_Overwrite", 20.5678f);
        float value = this.sharedPreferencesNamedCollection.getFloat("testSetFloat_Overwrite", 0f);
        assertEquals(20.5678f, value);
    }

    @Test
    public void testGetFloat_EntryNotInSharedPreferences() throws Exception {
        float value =
                this.sharedPreferencesNamedCollection.getFloat("testGetFloat_NotInSharedPrefs", 0f);
        assertEquals(0f, value);
    }

    @Test
    public void testSetGetBoolean_Happy() throws Exception {
        this.sharedPreferencesNamedCollection.setBoolean("testSetBoolean_Happy", true);
        boolean value =
                this.sharedPreferencesNamedCollection.getBoolean("testSetBoolean_Happy", false);
        assertTrue(value);
    }

    @Test
    public void testSetBoolean_Overwrite() throws Exception {
        this.sharedPreferencesNamedCollection.setBoolean("testSetBoolean_Overwrite", false);
        this.sharedPreferencesNamedCollection.setBoolean("testSetBoolean_Overwrite", true);
        boolean value =
                this.sharedPreferencesNamedCollection.getBoolean("testSetBoolean_Overwrite", false);
        assertTrue(value);
    }

    @Test
    public void testGetBoolean_EntryNotInSharedPreferences() throws Exception {
        boolean value =
                this.sharedPreferencesNamedCollection.getBoolean(
                        "testGetBoolean_NotInSharedPrefs", true);
        assertTrue(value);
    }

    @Test
    public void testSetGetMap_Happy() throws Exception {
        Map testMap =
                new HashMap<String, String>() {
                    {
                        put("key", "value");
                        put("key1", "value1");
                    }
                };
        this.sharedPreferencesNamedCollection.setMap("testSetMap_Happy", testMap);
        Map value = this.sharedPreferencesNamedCollection.getMap("testSetMap_Happy");
        assertEquals(testMap, value);
    }

    @Test
    public void testSetMap_Overwrite() throws Exception {
        Map testMap =
                new HashMap<String, String>() {
                    {
                        put("key", "value");
                        put("key1", "value1");
                    }
                };
        Map testMap1 =
                new HashMap<String, String>() {
                    {
                        put("key2", "value2");
                        put("key3", "value3");
                    }
                };
        this.sharedPreferencesNamedCollection.setMap("testSetMap_Overwrite", testMap);
        this.sharedPreferencesNamedCollection.setMap("testSetMap_Overwrite", testMap1);
        Map value = this.sharedPreferencesNamedCollection.getMap("testSetMap_Overwrite");
        assertEquals(testMap1, value);
    }

    @Test
    public void testGetMap_EntryNotInSharedPreferences() throws Exception {
        Map value = this.sharedPreferencesNamedCollection.getMap("testGetMap_NotInSharedPrefs");
        assertNull(value);
    }

    @Test
    public void testContains_Happy() throws Exception {
        this.sharedPreferencesNamedCollection.setInt("testContains_Int", 10);
        this.sharedPreferencesNamedCollection.setLong("testContains_Long", 5000L);
        this.sharedPreferencesNamedCollection.setDouble("testContains_Double", 10.1234d);
        this.sharedPreferencesNamedCollection.setFloat("testContains_Float", 5.6789f);
        this.sharedPreferencesNamedCollection.setString("testContains_String", "testString");
        this.sharedPreferencesNamedCollection.setBoolean("testContains_Boolean", true);
        assertTrue(this.sharedPreferencesNamedCollection.contains("testContains_Int"));
        assertTrue(this.sharedPreferencesNamedCollection.contains("testContains_Long"));
        assertTrue(this.sharedPreferencesNamedCollection.contains("testContains_Double"));
        assertTrue(this.sharedPreferencesNamedCollection.contains("testContains_Float"));
        assertTrue(this.sharedPreferencesNamedCollection.contains("testContains_String"));
        assertTrue(this.sharedPreferencesNamedCollection.contains("testContains_Boolean"));
    }

    @Test
    public void testContains_EntryNotInSharedPreferences() throws Exception {
        assertFalse(
                this.sharedPreferencesNamedCollection.contains(
                        "testContains_Int_NotInSharedPrefs"));
        assertFalse(
                this.sharedPreferencesNamedCollection.contains(
                        "testContains_Long_NotInSharedPrefs"));
        assertFalse(
                this.sharedPreferencesNamedCollection.contains(
                        "testContains_Double_NotInSharedPrefs"));
        assertFalse(
                this.sharedPreferencesNamedCollection.contains(
                        "testContains_Float_NotInSharedPrefs"));
        assertFalse(
                this.sharedPreferencesNamedCollection.contains(
                        "testContains_String_NotInSharedPrefs"));
    }

    @Test
    public void testRemove_Happy() throws Exception {
        this.sharedPreferencesNamedCollection.setInt("testRemove_Happy_Int", 10);
        this.sharedPreferencesNamedCollection.remove("testRemove_Happy_Int");
        assertFalse(this.sharedPreferencesNamedCollection.contains("testRemove_Happy_Int"));
    }

    @Test
    public void testRemove_EntryNotInSharedPreferences() throws Exception {
        this.sharedPreferencesNamedCollection.remove("testRemove_Happy_NotInSharedPrefs");
        assertFalse(
                this.sharedPreferencesNamedCollection.contains(
                        "testRemove_Happy_NotInSharedPrefs"));
    }

    @Test
    public void testRemoveAll_Happy() throws Exception {
        this.sharedPreferencesNamedCollection.setInt("testRemoveAll_Int", 10);
        this.sharedPreferencesNamedCollection.setLong("testRemoveAll_Long", 5000L);
        this.sharedPreferencesNamedCollection.setDouble("testRemoveAll_Double", 10.1234d);
        this.sharedPreferencesNamedCollection.setFloat("testRemoveAll_Float", 5.6789f);
        this.sharedPreferencesNamedCollection.setString("testRemoveAll_String", "testString");
        this.sharedPreferencesNamedCollection.removeAll();
        assertEquals(0, this.sharedPreferencesNamedCollection.getInt("testRemoveAll_Int", 0));
        assertEquals(0L, this.sharedPreferencesNamedCollection.getLong("testRemoveAll_Long", 0L));
        assertEquals(
                0d, this.sharedPreferencesNamedCollection.getDouble("testRemoveAll_Double", 0d));
        assertEquals(0f, this.sharedPreferencesNamedCollection.getFloat("testRemoveAll_Float", 0f));
        assertNull(this.sharedPreferencesNamedCollection.getString("testRemoveAll_String", null));
    }

    @Test
    public void testRemoveAll_EmptySharedPreferences() throws Exception {
        this.sharedPreferencesNamedCollection.removeAll();
    }
}
