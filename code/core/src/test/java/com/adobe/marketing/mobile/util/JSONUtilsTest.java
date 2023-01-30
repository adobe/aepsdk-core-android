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

package com.adobe.marketing.mobile.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class JSONUtilsTest {

    @Test
    public void testIsNullOrEmpty_withJSONObject_whenNull() {
        assertTrue(JSONUtils.isNullOrEmpty((JSONObject) null));
    }

    @Test
    public void testIsNullOrEmpty_withJSONObject_whenEmpty() {
        assertTrue(JSONUtils.isNullOrEmpty(new JSONObject()));
    }

    @Test
    public void testIsNullOrEmpty_withJSONObject_whenNonEmpty() throws JSONException {
        JSONObject test = new JSONObject();
        test.put("key", "value");
        assertFalse(JSONUtils.isNullOrEmpty(test));
    }

    @Test
    public void testIsNullOrEmpty_withJSONArray_whenNull() {
        assertTrue(JSONUtils.isNullOrEmpty((JSONArray) null));
    }

    @Test
    public void testIsNullOrEmpty_withJSONArray_whenEmpty() {
        assertTrue(JSONUtils.isNullOrEmpty(new JSONArray()));
    }

    @Test
    public void testIsNullOrEmpty_withJSONArray_whenNonEmpty() {
        JSONArray test = new JSONArray();
        test.put("test");
        assertFalse(JSONUtils.isNullOrEmpty(test));
    }

    @Test
    public void testToList_Null() throws JSONException {
        assertNull(JSONUtils.toList(null));
    }

    @Test
    public void testToList_IntegerArray() throws JSONException {
        final List<Integer> listOfInts = Arrays.asList(1, 2, 3, 4);
        assertEquals(listOfInts, JSONUtils.toList(new JSONArray(listOfInts)));
    }

    @Test
    public void testToList_LongArray() throws JSONException {
        final List<Long> listOfInts = Arrays.asList(1L, 2L, Long.MAX_VALUE);
        assertEquals(listOfInts, JSONUtils.toList(new JSONArray(listOfInts)));
    }

    @Test
    public void testToList_DoubleArray() throws JSONException {
        final List<Double> listOfDoubles = Arrays.asList(1.1d, 2.1d, 3.1d, 4.1d);
        assertEquals(listOfDoubles, JSONUtils.toList(new JSONArray(listOfDoubles)));
    }

    @Test
    public void testToList_StringArray() throws JSONException {
        final List<String> listOfStrings = Arrays.asList("One", "Two", "Three", "Four");
        assertEquals(listOfStrings, JSONUtils.toList(new JSONArray(listOfStrings)));
    }

    @Test
    public void testToList_BooleanArray() throws JSONException {
        final List<Boolean> listOfBoolean = Arrays.asList(true, false, false, null);
        assertEquals(listOfBoolean, JSONUtils.toList(new JSONArray(listOfBoolean)));
    }

    @Test
    public void testToList_ArrayOfLists() throws JSONException {
        final List<Object> listWithLists =
                Arrays.asList(
                        Arrays.asList("One", "Two", "Three"),
                        Arrays.asList(1L, 2L, 3L),
                        Arrays.asList(1.1d, 2.1d, 3.1d, 4.1d),
                        Arrays.asList(true, false, false),
                        Arrays.asList(null, 1, 3.4d, "One"));
        assertEquals(listWithLists, JSONUtils.toList(new JSONArray(listWithLists)));
    }

    @Test
    public void testToList_ArrayOfArraysAndMaps() throws JSONException {
        final List<Object> listWithLists =
                Arrays.asList(
                        Arrays.asList("One", "Two", "Three"),
                        Arrays.asList(1L, 2L, 3L),
                        Arrays.asList(1.1d, 2.1d, 3.1d, 4.1d),
                        Arrays.asList(true, false, false),
                        Arrays.asList(null, 1, 3.4d, "One"),
                        new HashMap<String, Object>() {
                            {
                                put("int", 3);
                                put("double", 3.11d);
                                put("long", Long.MAX_VALUE);
                                put("String", "abcd");
                                put("boolean", true);
                                put("emptyList", Collections.EMPTY_LIST);
                                put("listOfInts", Arrays.asList(1, 2, 3));
                                put("listOfLong", Arrays.asList(Long.MIN_VALUE, Long.MAX_VALUE));
                                put("listOfDouble", Arrays.asList(1.2d, 2.3d, 3.4d));
                                put("listWithNull", Arrays.asList("NonNull", null));
                                put("listOfBooleans", Arrays.asList(true, false));
                                put(
                                        "nestedMap",
                                        new HashMap<String, Object>() {
                                            {
                                                put("int", 3);
                                                put("double", 3.11d);
                                                put("long", Long.MAX_VALUE);
                                                put("String", "abcd");
                                                put("boolean", true);
                                            }
                                        });
                            }
                        });
        assertEquals(listWithLists, JSONUtils.toList(new JSONArray(listWithLists)));
    }

    @Test
    public void testToMap_Null() throws JSONException {
        assertNull(JSONUtils.toMap(null));
    }

    @Test
    public void testToMap_AllValidTypes() throws JSONException {
        final Map<String, Object> mapOfAllTypes =
                new HashMap<String, Object>() {
                    {
                        put("int", 3);
                        put("double", 3.11d);
                        put("long", Long.MAX_VALUE);
                        put("String", "abcd");
                        put("boolean", true);
                        put("emptyList", Collections.EMPTY_LIST);
                        put("listOfInts", Arrays.asList(1, 2, 3));
                        put("listOfLong", Arrays.asList(Long.MIN_VALUE, Long.MAX_VALUE));
                        put("listOfDouble", Arrays.asList(1.2d, 2.3d, 3.4d));
                        put("listWithNull", Arrays.asList("NonNull", null));
                        put("listOfBooleans", Arrays.asList(true, false));
                        put(
                                "listOfLists",
                                Arrays.asList(
                                        Arrays.asList("One", "Two", "Three"),
                                        Arrays.asList(1L, 2L, 3L),
                                        Arrays.asList(1.1d, 2.1d, 3.1d, 4.1d),
                                        Arrays.asList(true, false, false),
                                        Arrays.asList(null, 1, 3.4d, "One")));
                        put(
                                "nestedMap",
                                new HashMap<String, Object>() {
                                    {
                                        put("int", 3);
                                        put("double", 3.11d);
                                        put("long", Long.MAX_VALUE);
                                        put("String", "abcd");
                                        put("boolean", true);
                                    }
                                });
                    }
                };

        assertEquals(mapOfAllTypes, JSONUtils.toMap(new JSONObject(mapOfAllTypes)));
    }
}
