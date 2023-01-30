/*
  Copyright 2023 Adobe. All rights reserved.
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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class MapUtilsTests {

    @Test
    public void testIsNullOrEmpty_withMap_whenNull() {
        assertTrue(MapUtils.isNullOrEmpty((Map) null));
    }

    @Test
    public void testIsNullOrEmpty_withMap_whenEmpty() {
        assertTrue(MapUtils.isNullOrEmpty(new HashMap<>()));
    }

    @Test
    public void testIsNullOrEmpty_withMap_whenNonEmpty() {
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("key", "value");
        assertFalse(MapUtils.isNullOrEmpty(objectMap));

        Map<String, Integer> integerMap = new HashMap<>();
        integerMap.put("key", 1);
        assertFalse(MapUtils.isNullOrEmpty(integerMap));
    }

    @Test
    public void testPutIfNotEmpty_whenNullMap() {
        MapUtils.putIfNotEmpty(null, "key", "value");
    }

    @Test
    public void testPutIfNotEmpty_whenNullKey() {
        Map<String, Object> map = new HashMap<>();
        MapUtils.putIfNotEmpty(map, null, "value");
        assertTrue(map.isEmpty());
    }

    @Test
    public void testPutIfNotEmpty_whenNullValue() {
        Map<String, Object> map = new HashMap<>();
        MapUtils.putIfNotEmpty(map, "key", null);
        assertTrue(map.isEmpty());
    }

    @Test
    public void testPutIfNotEmpty_withBoolean() {
        Map<String, Object> map = new HashMap<>();
        MapUtils.putIfNotEmpty(map, "key", true);
        assertEquals(1, map.size());
        assertEquals(true, map.get("key"));
    }

    @Test
    public void testPutIfNotEmpty_withString_whenValidValue() {
        Map<String, Object> map = new HashMap<>();
        MapUtils.putIfNotEmpty(map, "key", "value");
        assertEquals(1, map.size());
        assertEquals("value", map.get("key"));
    }

    @Test
    public void testPutIfNotEmpty_withString_whenEmpty() {
        Map<String, Object> map = new HashMap<>();
        MapUtils.putIfNotEmpty(map, "key", "");
        assertEquals(0, map.size());
    }

    @Test
    public void testPutIfNotEmpty_withCollection_whenValidValue() {
        Map<String, Object> map = new HashMap<>();
        List<Integer> expectedList = Arrays.asList(1, 2, 3, 4);
        MapUtils.putIfNotEmpty(map, "key", expectedList);
        assertEquals(1, map.size());
        assertEquals(expectedList, map.get("key"));
    }

    @Test
    public void testPutIfNotEmpty_withCollection_whenEmpty() {
        Map<String, Object> map = new HashMap<>();
        MapUtils.putIfNotEmpty(map, "key", new ArrayList<Integer>());
        assertEquals(0, map.size());
    }

    @Test
    public void testPutIfNotEmpty_withMap_whenValidValue() {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> innerMap = new HashMap<>();
        innerMap.put("test", true);
        MapUtils.putIfNotEmpty(map, "key", innerMap);
        assertEquals(1, map.size());
        assertEquals(innerMap, map.get("key"));
    }

    @Test
    public void testPutIfNotEmpty_withMap_whenEmpty() {
        Map<String, Object> map = new HashMap<>();
        MapUtils.putIfNotEmpty(map, "key", new HashMap<String, Object>());
        assertEquals(0, map.size());
    }
}
