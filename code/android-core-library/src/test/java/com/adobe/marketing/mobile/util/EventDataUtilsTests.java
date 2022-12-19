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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import org.junit.Test;

public class EventDataUtilsTests {

    @Test
    public void testClone_SimpleObjects() throws CloneFailedException {
        Map<String, Object> values = new HashMap<>();
        values.put("boolean", true);

        byte b = 100;
        values.put("byte", b);

        short s = 1000;
        values.put("short", s);

        int i = 10000;
        values.put("int", i);

        long l = 100000L;
        values.put("long", l);

        float f = 1.1F;
        values.put("float", f);

        double d = 1.1e10D;
        values.put("double", d);

        BigDecimal bd = BigDecimal.TEN;
        values.put("bigdecimal", bd);

        BigInteger bi = BigInteger.TEN;
        values.put("biginteger", bi);

        char c = 'a';
        values.put("char", c);

        String str = "hello";
        values.put("string", str);

        UUID uuid = UUID.randomUUID();
        values.put("uuid", uuid);

        Map<String, Object> clonedValues = EventDataUtils.clone(values);
        assertEquals(values, clonedValues);
    }

    @Test
    public void testClone_NestedObjects() throws CloneFailedException {
        Map<String, Object> nestedMap = new TreeMap<>();
        nestedMap.put("integer", 1);
        nestedMap.put("float", 1f);
        nestedMap.put("double", 1d);
        nestedMap.put("string", "hello");

        List<Object> nestedList = new LinkedList<>();
        nestedList.add(1);
        nestedList.add(1f);
        nestedList.add(1d);
        nestedList.add("hello");

        Map<String, Object> data = new HashMap<>();
        data.put("map", nestedList);
        data.put("list", nestedList);
        data.put("string", "top level");
        data.put("uuid", UUID.randomUUID());
        data.put("null", null);

        Map<String, Object> clonedData = EventDataUtils.clone(data);
        assertEquals(data, clonedData);
    }

    @Test
    public void testClone_MapWithNonStringKeys() throws CloneFailedException {
        Map<Object, Object> nestedMap = new HashMap<>();
        nestedMap.put(null, 1);
        nestedMap.put(new Integer(1), 1);
        nestedMap.put(new Double(1.1), 1d);
        nestedMap.put("string", "hello");

        Map<String, Object> data = new HashMap<>();
        data.put("map", nestedMap);

        Map<String, Object> expectedNestedMap = new HashMap<>();
        expectedNestedMap.put("string", "hello");
        Map<String, Object> expectedData = new HashMap<>();
        expectedData.put("map", expectedNestedMap);

        Map<String, Object> clonedData = EventDataUtils.clone(data);
        assertEquals(expectedData, clonedData);
    }

    @Test
    public void testClone_Array() throws CloneFailedException {
        String[] stringArray = new String[] {"string1", "string2"};

        Map<String, Object> data = new HashMap<>();
        data.put("stringArray", stringArray);

        Map[] mapArray =
                new Map[] {
                    new HashMap(),
                    new HashMap() {
                        {
                            put("k1", "v1");
                            put("k2", "v2");
                        }
                    },
                    new HashMap() {
                        {
                            put("integer", 1);
                            put("float", 1f);
                            put("double", 1d);
                            put("string", "hello");
                        }
                    },
                };
        data.put("mapArray", mapArray);

        Map<String, Object> clonedData = EventDataUtils.clone(data);

        assertEquals(clonedData.get("stringArray"), Arrays.asList(stringArray));
        assertEquals(clonedData.get("mapArray"), Arrays.asList(mapArray));
    }

    @Test
    public void testClone_FailUnsupportedTypes() {
        class Data {}

        Map<String, Object> map = new HashMap<>();
        map.put("data", new Data());

        Exception ex = assertThrows(CloneFailedException.class, () -> EventDataUtils.clone(map));

        assertEquals(ex.getMessage(), "Object is of unsupported type");
    }

    @Test
    public void testCloneFailCircularReference() {
        Map<String, Object> map = new HashMap<>();
        List<Object> list = new ArrayList<>();
        list.add(map);

        map.put("list", list);

        Exception ex = assertThrows(CloneFailedException.class, () -> EventDataUtils.clone(map));

        assertEquals(ex.getMessage(), "Max depth reached");
    }

    @Test
    public void testImmutableClone() throws CloneFailedException {
        Map<String, Object> nestedMap = new TreeMap<>();
        nestedMap.put("integer", 1);
        nestedMap.put("float", 1f);
        nestedMap.put("double", 1d);
        nestedMap.put("string", "hello");

        List<Object> nestedList = new LinkedList<>();
        nestedList.add(1);
        nestedList.add(1f);
        nestedList.add(1d);
        nestedList.add("hello");

        Map<String, Object> data = new HashMap<>();
        data.put("map", nestedMap);
        data.put("list", nestedList);

        Map<String, Object> clonedData = EventDataUtils.immutableClone(data);

        assertThrows(
                UnsupportedOperationException.class, () -> clonedData.put("newKey", "newValue"));

        assertThrows(
                UnsupportedOperationException.class,
                () -> {
                    Map<String, Object> clonedNestedMap =
                            (Map<String, Object>) clonedData.get("map");
                    clonedNestedMap.put("newKey", "newValue");
                });

        assertThrows(
                UnsupportedOperationException.class,
                () -> {
                    List<Object> clonedNestedList = (List<Object>) clonedData.get("list");
                    clonedNestedList.add("value");
                });
    }

    @Test
    public void testCastFromGenericType_SimpleObjects() {
        Map<String, Object> values = new HashMap<>();
        values.put("boolean", true);

        byte b = 100;
        values.put("byte", b);

        short s = 1000;
        values.put("short", s);

        int i = 10000;
        values.put("int", i);

        long l = 100000L;
        values.put("long", l);

        float f = 1.1F;
        values.put("float", f);

        double d = 1.1e10D;
        values.put("double", d);

        BigDecimal bd = BigDecimal.TEN;
        values.put("bigdecimal", bd);

        BigInteger bi = BigInteger.TEN;
        values.put("biginteger", bi);

        char c = 'a';
        values.put("char", c);

        String str = "hello";
        values.put("string", str);

        UUID uuid = UUID.randomUUID();
        values.put("uuid", uuid);

        Map<String, Object> castMap = EventDataUtils.castFromGenericType(values);
        assertEquals(values, castMap);
    }

    @Test
    public void testCastGenericType_NestedObjects() {
        Map<String, Object> nestedMap = new TreeMap<>();
        nestedMap.put("integer", 1);
        nestedMap.put("float", 1f);
        nestedMap.put("double", 1d);
        nestedMap.put("string", "hello");

        List<Object> nestedList = new LinkedList<>();
        nestedList.add(1);
        nestedList.add(1f);
        nestedList.add(1d);
        nestedList.add("hello");

        Map<String, Object> data = new HashMap<>();
        data.put("map", nestedList);
        data.put("list", nestedList);
        data.put("string", "top level");
        data.put("uuid", UUID.randomUUID());
        data.put("null", null);

        Map<String, Object> castMap = EventDataUtils.castFromGenericType(data);
        assertEquals(data, castMap);
    }

    @Test
    public void testCastGenericType_MapWithNonStringKeys() {
        Map<Object, Object> data = new HashMap<>();
        data.put(null, 1);
        data.put(new Integer(1), 1);
        data.put(new Double(1.1), 1d);
        data.put("string", "hello");

        Map<String, Object> castMap = EventDataUtils.castFromGenericType(data);
        assertNull(castMap);
    }

    @Test
    public void testCastGenericType_Array() {
        String[] stringArray = new String[] {"string1", "string2"};

        Map<String, Object> data = new HashMap<>();
        data.put("stringArray", stringArray);

        Map[] mapArray =
                new Map[] {
                    new HashMap(),
                    new HashMap() {
                        {
                            put("k1", "v1");
                            put("k2", "v2");
                        }
                    },
                    new HashMap() {
                        {
                            put("integer", 1);
                            put("float", 1f);
                            put("double", 1d);
                            put("string", "hello");
                        }
                    },
                };
        data.put("mapArray", mapArray);

        Map<String, Object> castMap = EventDataUtils.castFromGenericType(data);

        assertEquals(castMap.get("stringArray"), stringArray);
        assertEquals(castMap.get("mapArray"), mapArray);
    }
}
