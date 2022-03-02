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

package com.adobe.marketing.mobile.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class ObjectUtilsTests {
    @Test
    public void testCloneBasicTypes() throws CloneFailedException {
        Boolean bool = (Boolean) ObjectUtils.deepClone(true);
        assertTrue(bool);

        byte b = 100;
        assertEquals(ObjectUtils.deepClone(b), b);

        short s = 1000;
        assertEquals(ObjectUtils.deepClone(s), s);

        int i = 10000;
        assertEquals(ObjectUtils.deepClone(i), i);

        long l = 100000L;
        assertEquals(ObjectUtils.deepClone(l), l);

        float f = 1.1F;
        assertEquals((float) ObjectUtils.deepClone(f), f, 0.0000001);

        double d = 1.1e10D;
        assertEquals((double) ObjectUtils.deepClone(d), d, 0.0000001);

        BigDecimal bd = (BigDecimal) ObjectUtils.deepClone((BigDecimal.TEN));
        assertEquals(bd , BigDecimal.TEN);

        BigInteger bi = (BigInteger) ObjectUtils.deepClone((BigInteger.TEN));
        assertEquals(bi , BigInteger.TEN);

        char c = 'a';
        assertEquals(ObjectUtils.deepClone(c), c);

        String str = "hello";
        assertEquals(ObjectUtils.deepClone(str) , str);

        UUID uuid = UUID.randomUUID();
        UUID clonedUuid = (UUID) ObjectUtils.deepClone(uuid);
        assertEquals(uuid , clonedUuid);
    }

    @Test
    public void testCloneMap() throws CloneFailedException {
        Map<String, Object> map = new HashMap<>();
        map.put("integer", 1);
        map.put("float", 1f);
        map.put("double", 1d);
        map.put("string", "hello");

        Map<?, ?> clonedMap = (Map<?, ?>) ObjectUtils.deepClone(map);
        assertEquals(map, clonedMap);
    }

    @Test
    public void testCloneList() throws CloneFailedException {
        List<Object> list = new LinkedList<>();
        list.add(1);
        list.add(1f);
        list.add(1d);
        list.add("hello");

        List<?> clonedList = (List<?>) ObjectUtils.deepClone(list);
        assertEquals(list, clonedList);
    }

    @Test
    public void testCloneNestedObject() throws CloneFailedException {
        Map<String, Object> map = new TreeMap<>();
        map.put("integer", 1);
        map.put("float", 1f);
        map.put("double", 1d);
        map.put("string", "hello");

        List<Object> list = new LinkedList<>();
        list.add(1);
        list.add(1f);
        list.add(1d);
        list.add("hello");

        Map<String, Object> data = new HashMap<>();
        data.put("map", map);
        data.put("list", list);
        data.put("string", "top level");
        data.put("uuid", UUID.randomUUID());
        data.put("null", null);

        Map<?,?> clonedData = (Map<?,?>) ObjectUtils.deepClone(data);
        assertEquals(data, clonedData);
    }

    @Test
    public void testCloneFailUnsupportedTypes() {
        class Data {}

        Exception ex = assertThrows(
                CloneFailedException.class,
                () -> ObjectUtils.deepClone(new Data()));

        assertEquals(ex.getMessage(), "Object is of unsupported type");
    }

    @Test
    public void testCloneFailCircularReference() {
        List<Object> list = new ArrayList<>();
        list.add(list);

        Exception ex = assertThrows(
                CloneFailedException.class,
                () -> ObjectUtils.deepClone(list));

        assertEquals(ex.getMessage(), "Max depth reached");
    }
}
