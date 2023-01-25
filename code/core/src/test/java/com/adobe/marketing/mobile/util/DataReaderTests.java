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
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

interface CheckedBiConsumer {
    void accept(Map<String, Object> map, String data) throws Exception;
}

interface CheckedConsumer {
    void accept(String data) throws Exception;
}

public class DataReaderTests {

    final String NULL_KEY_ERROR_MSG = "Map or key is null";
    final String NULL_VALUE_ERROR_MSG = "Map contains null value for key";
    final String OVERFLOW_ERROR_MSG = "Value overflows type ";
    final String MAP_ERROR_MSG = "Value is not a map";
    final String MAP_ENTRY_ERROR_MSG = "Map entry is not of expected type";
    final String LIST_ERROR_MSG = "Value is not a list";
    final String LIST_ENTRY_ERROR_MSG = "List entry is not of expected type";

    final double DELTA = 0.000001d;

    Map<String, Object> data = new HashMap<>();

    {
        data.put("BOOL_TRUE", Boolean.TRUE);
        data.put("BOOL_FALSE", Boolean.FALSE);

        data.put("BYTE_MAX", Byte.MAX_VALUE);
        data.put("BYTE", (byte) 1);
        data.put("SHORT_MAX", Short.MAX_VALUE);
        data.put("SHORT", (short) 2);
        data.put("INT_MAX", Integer.MAX_VALUE);
        data.put("INT", 3);
        data.put("LONG_MAX", Long.MAX_VALUE);
        data.put("LONG", 4L);
        data.put("FLOAT_MAX", Float.MAX_VALUE);
        data.put("FLOAT", 5.5F);
        data.put("DOUBLE_MAX", Double.MAX_VALUE);
        data.put("DOUBLE", 6.6);

        data.put("STRING", "STRING");
        data.put("NULL", null);
        data.put("EMPTY_MAP", new HashMap<>());
        data.put("EMPTY_LIST", new ArrayList<>());

        data.put(
                "INT_MAP",
                new HashMap<String, Integer>() {
                    {
                        put("a", 1);
                        put("b", 2);
                    }
                });
        data.put("INT_LIST", Arrays.asList(1, 2));

        data.put(
                "STRING_MAP",
                new HashMap<String, String>() {
                    {
                        put("a", "a");
                        put("b", "b");
                    }
                });
        data.put("STRING_LIST", Arrays.asList("a", "b"));

        data.put(
                "STRINGMAP_LIST",
                Arrays.asList(
                        new HashMap<String, String>() {
                            {
                                put("a", "a");
                                put("b", "b");
                            }
                        },
                        new HashMap<String, String>() {
                            {
                                put("c", "c");
                                put("d", "d");
                            }
                        }));

        data.put(
                "INTMAP_LIST",
                Arrays.asList(
                        new HashMap<String, Integer>() {
                            {
                                put("a", 1);
                                put("b", 2);
                            }
                        },
                        new HashMap<String, Integer>() {
                            {
                                put("c", 3);
                                put("d", 4);
                            }
                        }));
    }

    void checkNullArguments(CheckedBiConsumer fn) {
        Exception ex;
        ex = assertThrows(DataReaderException.class, () -> fn.accept(null, "key"));
        assertEquals(NULL_KEY_ERROR_MSG, ex.getMessage());

        ex = assertThrows(DataReaderException.class, () -> fn.accept(data, null));
        assertEquals(NULL_KEY_ERROR_MSG, ex.getMessage());
    }

    void checkCastException(CheckedConsumer fn, List<String> keys) {
        keys.forEach(
                key -> {
                    Exception ex = assertThrows(DataReaderException.class, () -> fn.accept(key));
                    if (ex != null && ex.getCause() != null) {
                        assertEquals(ClassCastException.class, ex.getCause().getClass());
                    } else {
                        fail();
                    }
                });
    }

    void checkExceptionMessage(CheckedConsumer fn, List<String> keys, String errorMessage) {
        keys.forEach(
                key -> {
                    Exception ex = assertThrows(DataReaderException.class, () -> fn.accept(key));
                    assertEquals(ex.getMessage(), errorMessage);
                });
    }

    @Test
    public void testReadBoolean() throws Exception {
        assertTrue(DataReader.getBoolean(data, "BOOL_TRUE"));
        assertFalse(DataReader.getBoolean(data, "BOOL_FALSE"));

        checkNullArguments(DataReader::getBoolean);
        checkExceptionMessage(
                key -> DataReader.getBoolean(data, key),
                Arrays.asList("NULL", "INVALID"),
                NULL_VALUE_ERROR_MSG);
        checkCastException(
                key -> DataReader.getBoolean(data, key),
                Arrays.asList("FLOAT", "BYTE", "INT", "DOUBLE", "LONG", "STRING"));
    }

    @Test
    public void testOptBoolean() {
        assertTrue(DataReader.optBoolean(data, "BOOL_TRUE", false));
        assertFalse(DataReader.optBoolean(data, "BOOL_FALSE", true));

        assertTrue(DataReader.optBoolean(data, "FLOAT", true));
        assertTrue(DataReader.optBoolean(data, "LONG", true));
        assertTrue(DataReader.optBoolean(data, "STRING", true));
        assertTrue(DataReader.optBoolean(data, "NULL", true));
        assertTrue(DataReader.optBoolean(data, "INVALID", true));

        assertTrue(DataReader.optBoolean(null, "BOOL_FALSE", true));
        assertTrue(DataReader.optBoolean(data, null, true));
    }

    @Test
    public void testReadInt() throws Exception {
        assertEquals(Integer.MAX_VALUE, DataReader.getInt(data, "INT_MAX"));
        assertEquals(Short.MAX_VALUE, DataReader.getInt(data, "SHORT_MAX"));
        assertEquals(Byte.MAX_VALUE, DataReader.getInt(data, "BYTE_MAX"));

        assertEquals(1, DataReader.getInt(data, "BYTE"));
        assertEquals(2, DataReader.getInt(data, "SHORT"));
        assertEquals(3, DataReader.getInt(data, "INT"));
        assertEquals(4, DataReader.getInt(data, "LONG"));
        assertEquals(5, DataReader.getInt(data, "FLOAT"));
        assertEquals(6, DataReader.getInt(data, "DOUBLE"));

        checkNullArguments(DataReader::getInt);
        checkExceptionMessage(
                key -> DataReader.getInt(data, key),
                Arrays.asList("NULL", "INVALID"),
                NULL_VALUE_ERROR_MSG);

        String errorMessage = OVERFLOW_ERROR_MSG + Integer.class;
        checkExceptionMessage(
                key -> DataReader.getInt(data, key),
                Arrays.asList("LONG_MAX", "FLOAT_MAX", "DOUBLE_MAX"),
                errorMessage);

        checkCastException(
                key -> DataReader.getInt(data, key),
                Arrays.asList("STRING", "STRING_MAP", "STRING_LIST"));
    }

    @Test
    public void testOptInt() {
        assertEquals(Integer.MAX_VALUE, DataReader.optInt(data, "INT_MAX", 123));
        assertEquals(Short.MAX_VALUE, DataReader.optInt(data, "SHORT_MAX", 123));
        assertEquals(Byte.MAX_VALUE, DataReader.optInt(data, "BYTE_MAX", 123));

        assertEquals(1, DataReader.optInt(data, "BYTE", 123));
        assertEquals(2, DataReader.optInt(data, "SHORT", 123));
        assertEquals(3, DataReader.optInt(data, "INT", 123));
        assertEquals(4, DataReader.optInt(data, "LONG", 123));
        assertEquals(5, DataReader.optInt(data, "FLOAT", 123));
        assertEquals(6, DataReader.optInt(data, "DOUBLE", 123));

        assertEquals(123, DataReader.optInt(data, "LONG_MAX", 123));
        assertEquals(123, DataReader.optInt(data, "FLOAT_MAX", 123));
        assertEquals(123, DataReader.optInt(data, "DOUBLE_MAX", 123));
        assertEquals(123, DataReader.optInt(data, "STRING", 123));
        assertEquals(123, DataReader.optInt(data, "NULL", 123));
        assertEquals(123, DataReader.optInt(data, "INVALID", 123));

        assertEquals(123, DataReader.optInt(null, "INT", 123));
        assertEquals(123, DataReader.optInt(data, null, 123));
    }

    @Test
    public void testReadLong() throws Exception {
        assertEquals(Long.MAX_VALUE, DataReader.getLong(data, "LONG_MAX"));
        assertEquals(Integer.MAX_VALUE, DataReader.getLong(data, "INT_MAX"));
        assertEquals(Short.MAX_VALUE, DataReader.getLong(data, "SHORT_MAX"));
        assertEquals(Byte.MAX_VALUE, DataReader.getLong(data, "BYTE_MAX"));

        assertEquals(1, DataReader.getLong(data, "BYTE"));
        assertEquals(2, DataReader.getLong(data, "SHORT"));
        assertEquals(3, DataReader.getLong(data, "INT"));
        assertEquals(4, DataReader.getLong(data, "LONG"));
        assertEquals(5, DataReader.getLong(data, "FLOAT"));
        assertEquals(6, DataReader.getLong(data, "DOUBLE"));

        checkNullArguments(DataReader::getLong);
        checkExceptionMessage(
                key -> DataReader.getLong(data, key),
                Arrays.asList("NULL", "INVALID"),
                NULL_VALUE_ERROR_MSG);

        String errorMessage = OVERFLOW_ERROR_MSG + Long.class;
        checkExceptionMessage(
                key -> DataReader.getLong(data, key),
                Arrays.asList("FLOAT_MAX", "DOUBLE_MAX"),
                errorMessage);

        checkCastException(
                key -> DataReader.getLong(data, key),
                Arrays.asList("STRING", "STRING_MAP", "STRING_LIST"));
    }

    @Test
    public void testOptLong() {
        assertEquals(Long.MAX_VALUE, DataReader.optLong(data, "LONG_MAX", 123));
        assertEquals(Integer.MAX_VALUE, DataReader.optLong(data, "INT_MAX", 123));
        assertEquals(Short.MAX_VALUE, DataReader.optLong(data, "SHORT_MAX", 123));
        assertEquals(Byte.MAX_VALUE, DataReader.optLong(data, "BYTE_MAX", 123));

        assertEquals(1, DataReader.optLong(data, "BYTE", 123));
        assertEquals(2, DataReader.optLong(data, "SHORT", 123));
        assertEquals(3, DataReader.optLong(data, "INT", 123));
        assertEquals(4, DataReader.optLong(data, "LONG", 123));
        assertEquals(5, DataReader.optLong(data, "FLOAT", 123));
        assertEquals(6, DataReader.optLong(data, "DOUBLE", 123));

        assertEquals(123, DataReader.optLong(data, "FLOAT_MAX", 123));
        assertEquals(123, DataReader.optLong(data, "DOUBLE_MAX", 123));
        assertEquals(123, DataReader.optLong(data, "STRING", 123));
        assertEquals(123, DataReader.optLong(data, "NULL", 123));
        assertEquals(123, DataReader.optLong(data, "INVALID", 123));

        assertEquals(123, DataReader.optLong(null, "INVALID", 123));
        assertEquals(123, DataReader.optLong(data, null, 123));
    }

    @Test
    public void testReadFloat() throws Exception {
        assertEquals(Float.MAX_VALUE, DataReader.getFloat(data, "FLOAT_MAX"), DELTA);
        assertEquals((float) Long.MAX_VALUE, DataReader.getFloat(data, "LONG_MAX"), DELTA);
        assertEquals((float) Integer.MAX_VALUE, DataReader.getFloat(data, "INT_MAX"), DELTA);
        assertEquals((float) Short.MAX_VALUE, DataReader.getFloat(data, "SHORT_MAX"), DELTA);
        assertEquals((float) Byte.MAX_VALUE, DataReader.getFloat(data, "BYTE_MAX"), DELTA);

        assertEquals(1, DataReader.getFloat(data, "BYTE"), DELTA);
        assertEquals(2, DataReader.getFloat(data, "SHORT"), DELTA);
        assertEquals(3, DataReader.getFloat(data, "INT"), DELTA);
        assertEquals(4, DataReader.getFloat(data, "LONG"), DELTA);
        assertEquals(5.5, DataReader.getFloat(data, "FLOAT"), DELTA);
        assertEquals(6.6, DataReader.getFloat(data, "DOUBLE"), DELTA);

        checkNullArguments(DataReader::getFloat);
        checkExceptionMessage(
                key -> DataReader.getFloat(data, key),
                Arrays.asList("NULL", "INVALID"),
                NULL_VALUE_ERROR_MSG);

        String errorMessage = OVERFLOW_ERROR_MSG + Float.class;
        checkExceptionMessage(
                key -> DataReader.getFloat(data, key), Arrays.asList("DOUBLE_MAX"), errorMessage);

        checkCastException(
                key -> DataReader.getFloat(data, key),
                Arrays.asList("STRING", "STRING_MAP", "STRING_LIST"));
    }

    @Test
    public void testOptFloat() {
        assertEquals(Float.MAX_VALUE, DataReader.optFloat(data, "FLOAT_MAX", 3.3F), DELTA);
        assertEquals((float) Long.MAX_VALUE, DataReader.optFloat(data, "LONG_MAX", 3.3F), DELTA);
        assertEquals((float) Integer.MAX_VALUE, DataReader.optFloat(data, "INT_MAX", 3.3F), DELTA);
        assertEquals((float) Short.MAX_VALUE, DataReader.optFloat(data, "SHORT_MAX", 3.3F), DELTA);
        assertEquals((float) Byte.MAX_VALUE, DataReader.optFloat(data, "BYTE_MAX", 3.3F), DELTA);

        assertEquals(1, DataReader.optFloat(data, "BYTE", 3.3F), DELTA);
        assertEquals(2, DataReader.optFloat(data, "SHORT", 3.3F), DELTA);
        assertEquals(3, DataReader.optFloat(data, "INT", 3.3F), DELTA);
        assertEquals(4, DataReader.optFloat(data, "LONG", 3.3F), DELTA);
        assertEquals(5.5, DataReader.optFloat(data, "FLOAT", 3.3F), DELTA);
        assertEquals(6.6, DataReader.optFloat(data, "DOUBLE", 3.3F), DELTA);

        assertEquals(3.3, DataReader.optFloat(data, "DOUBLE_MAX", 3.3F), DELTA);
        assertEquals(3.3, DataReader.optFloat(data, "STRING", 3.3F), DELTA);
        assertEquals(3.3, DataReader.optFloat(data, "NULL", 3.3F), DELTA);
        assertEquals(3.3, DataReader.optFloat(data, "INVALID", 3.3F), DELTA);

        assertEquals(3.3, DataReader.optFloat(null, "NULL", 3.3F), DELTA);
        assertEquals(3.3, DataReader.optFloat(data, null, 3.3F), DELTA);
    }

    @Test
    public void testReadDouble() throws Exception {
        assertEquals(Double.MAX_VALUE, DataReader.getDouble(data, "DOUBLE_MAX"), DELTA);
        assertEquals(Float.MAX_VALUE, DataReader.getDouble(data, "FLOAT_MAX"), DELTA);
        assertEquals(Long.MAX_VALUE, DataReader.getDouble(data, "LONG_MAX"), DELTA);
        assertEquals(Integer.MAX_VALUE, DataReader.getDouble(data, "INT_MAX"), DELTA);
        assertEquals(Short.MAX_VALUE, DataReader.getDouble(data, "SHORT_MAX"), DELTA);
        assertEquals(Byte.MAX_VALUE, DataReader.getDouble(data, "BYTE_MAX"), DELTA);

        assertEquals(1, DataReader.getDouble(data, "BYTE"), DELTA);
        assertEquals(2, DataReader.getDouble(data, "SHORT"), DELTA);
        assertEquals(3, DataReader.getDouble(data, "INT"), DELTA);
        assertEquals(4, DataReader.getDouble(data, "LONG"), DELTA);
        assertEquals(5.5, DataReader.getDouble(data, "FLOAT"), DELTA);
        assertEquals(6.6, DataReader.getDouble(data, "DOUBLE"), DELTA);

        checkNullArguments(DataReader::getDouble);
        checkExceptionMessage(
                key -> DataReader.getDouble(data, key),
                Arrays.asList("NULL", "INVALID"),
                NULL_VALUE_ERROR_MSG);
        checkCastException(key -> DataReader.getDouble(data, key), Arrays.asList("STRING"));
    }

    @Test
    public void testOptDouble() {
        assertEquals(Double.MAX_VALUE, DataReader.optDouble(data, "DOUBLE_MAX", 3.3), DELTA);
        assertEquals(Float.MAX_VALUE, DataReader.optDouble(data, "FLOAT_MAX", 3.3), DELTA);
        assertEquals(Long.MAX_VALUE, DataReader.optDouble(data, "LONG_MAX", 3.3), DELTA);
        assertEquals(Integer.MAX_VALUE, DataReader.optDouble(data, "INT_MAX", 3.3), DELTA);
        assertEquals(Short.MAX_VALUE, DataReader.optDouble(data, "SHORT_MAX", 3.3), DELTA);
        assertEquals(Byte.MAX_VALUE, DataReader.optDouble(data, "BYTE_MAX", 3.3), DELTA);

        assertEquals(1, DataReader.optDouble(data, "BYTE", 3.3), DELTA);
        assertEquals(2, DataReader.optDouble(data, "SHORT", 3.3), DELTA);
        assertEquals(3, DataReader.optDouble(data, "INT", 3.3), DELTA);
        assertEquals(4, DataReader.optDouble(data, "LONG", 3.3), DELTA);
        assertEquals(5.5, DataReader.optDouble(data, "FLOAT", 3.3), DELTA);
        assertEquals(6.6, DataReader.optDouble(data, "DOUBLE", 3.3), DELTA);

        assertEquals(3.3, DataReader.optDouble(data, "STRING", 3.3), DELTA);
        assertEquals(3.3, DataReader.optDouble(data, "NULL", 3.3), DELTA);
        assertEquals(3.3, DataReader.optDouble(data, "INVALID", 3.3), DELTA);

        assertEquals(3.3, DataReader.optDouble(data, null, 3.3), DELTA);
        assertEquals(3.3, DataReader.optDouble(null, "FLOAT", 3.3), DELTA);
    }

    @Test
    public void testReadString() throws Exception {
        checkCastException(
                key -> DataReader.getString(data, key),
                Arrays.asList("FLOAT", "BYTE", "INT", "DOUBLE"));
        checkCastException(
                key -> DataReader.getString(data, key),
                Arrays.asList(
                        "LONG_MAX", "INT_MAX", "SHORT_MAX", "BYTE_MAX", "FLOAT_MAX", "DOUBLE_MAX"));
        assertEquals("STRING", DataReader.getString(data, "STRING"));
        assertNull(DataReader.getString(data, "NULL"));
        assertNull(DataReader.getString(data, "INVALID"));

        checkNullArguments(DataReader::getString);
    }

    @Test
    public void testOptString() {
        assertEquals("STRING", DataReader.optString(data, "STRING", "DEFAULT"));
        assertEquals("DEFAULT", DataReader.optString(data, "BYTE", "DEFAULT"));
        assertEquals("DEFAULT", DataReader.optString(data, "SHORT", "DEFAULT"));
        assertEquals("DEFAULT", DataReader.optString(data, "INT", "DEFAULT"));
        assertEquals("DEFAULT", DataReader.optString(data, "LONG", "DEFAULT"));
        assertEquals("DEFAULT", DataReader.optString(data, "FLOAT", "DEFAULT"));
        assertEquals("DEFAULT", DataReader.optString(data, "DOUBLE", "DEFAULT"));
        assertEquals("DEFAULT", DataReader.optString(data, "NULL", "DEFAULT"));
        assertEquals("DEFAULT", DataReader.optString(data, "INVALID", "DEFAULT"));

        assertEquals("DEFAULT", DataReader.optString(null, "STRING", "DEFAULT"));
        assertEquals("DEFAULT", DataReader.optString(data, null, "DEFAULT"));
    }

    @Test
    public void testGetStringMap() throws Exception {
        Map<String, String> map;
        map = DataReader.getStringMap(data, "EMPTY_MAP");
        assertEquals(map, new HashMap<>());

        map = DataReader.getStringMap(data, "STRING_MAP");
        assertEquals(
                map,
                new HashMap<String, String>() {
                    {
                        put("a", "a");
                        put("b", "b");
                    }
                });

        checkExceptionMessage(
                key -> DataReader.getStringMap(data, key),
                Arrays.asList("STRING", "INT", "STRING_LIST"),
                MAP_ERROR_MSG);
        checkExceptionMessage(
                key -> DataReader.getStringMap(data, key),
                Arrays.asList("INT_MAP"),
                MAP_ENTRY_ERROR_MSG);
        checkNullArguments(DataReader::getStringMap);
    }

    @Test
    public void testOptStringMap() {
        Map<String, String> defaultMap =
                new HashMap<String, String>() {
                    {
                        put("c", "c");
                        put("d", "d");
                    }
                };

        Map<String, String> map;
        map = DataReader.optStringMap(data, "EMPTY_MAP", defaultMap);
        assertEquals(map, new HashMap<>());

        map = DataReader.optStringMap(data, "STRING_MAP", defaultMap);
        assertEquals(
                map,
                new HashMap<String, String>() {
                    {
                        put("a", "a");
                        put("b", "b");
                    }
                });

        map = DataReader.optStringMap(data, "INT_MAP", defaultMap);
        assertEquals(map, defaultMap);

        map = DataReader.optStringMap(data, "STRING", defaultMap);
        assertEquals(map, defaultMap);
        map = DataReader.optStringMap(data, "STRING_LIST", defaultMap);
        assertEquals(map, defaultMap);

        map = DataReader.optStringMap(data, null, defaultMap);
        assertEquals(map, defaultMap);
        map = DataReader.optStringMap(null, "STRING_LIST", defaultMap);
        assertEquals(map, defaultMap);
    }

    @Test
    public void testGetStringList() throws Exception {
        List<String> list;
        list = DataReader.getStringList(data, "EMPTY_LIST");
        assertEquals(list, new ArrayList<>());

        list = DataReader.getStringList(data, "STRING_LIST");
        assertEquals(list, Arrays.asList("a", "b"));

        checkExceptionMessage(
                key -> DataReader.getStringList(data, key),
                Arrays.asList("STRING", "INT", "STRING_MAP"),
                LIST_ERROR_MSG);
        checkExceptionMessage(
                key -> DataReader.getStringList(data, key),
                Arrays.asList("INT_LIST"),
                LIST_ENTRY_ERROR_MSG);
        checkNullArguments(DataReader::getStringList);
    }

    @Test
    public void testOptStringList() {
        List<String> defaultList = Arrays.asList("c", "d");
        List<String> list;
        list = DataReader.optStringList(data, "EMPTY_LIST", defaultList);
        assertEquals(list, new ArrayList<>());

        list = DataReader.optStringList(data, "STRING_LIST", defaultList);
        assertEquals(list, Arrays.asList("a", "b"));

        list = DataReader.optStringList(data, "INT_LIST", defaultList);
        assertEquals(list, defaultList);
        list = DataReader.optStringList(data, "STRING", defaultList);
        assertEquals(list, defaultList);
        list = DataReader.optStringList(data, "STRING_MAP", defaultList);
        assertEquals(list, defaultList);

        list = DataReader.optStringList(null, "STRING_MAP", defaultList);
        assertEquals(list, defaultList);

        list = DataReader.optStringList(data, null, defaultList);
        assertEquals(list, defaultList);
    }

    @Test
    public void testGetTypedMap() throws Exception {
        Map<String, Integer> map = DataReader.getTypedMap(Integer.class, data, "INT_MAP");
        assertEquals(
                map,
                new HashMap<String, Integer>() {
                    {
                        put("a", 1);
                        put("b", 2);
                    }
                });

        checkExceptionMessage(
                key -> DataReader.getTypedMap(Integer.class, data, key),
                Arrays.asList("STRING", "INT", "STRING_LIST"),
                MAP_ERROR_MSG);
        checkExceptionMessage(
                key -> DataReader.getTypedMap(Integer.class, data, key),
                Arrays.asList("STRING_MAP"),
                MAP_ENTRY_ERROR_MSG);
        checkNullArguments((m, k) -> DataReader.getTypedMap(Integer.class, m, k));
    }

    @Test
    public void testGetTypedMapWithNullValue() throws Exception {
        Map<String, Object> data =
                new HashMap<String, Object>() {
                    {
                        put(
                                "INT_MAP",
                                new HashMap<String, Object>() {
                                    {
                                        put("a", 1);
                                        put("b", null);
                                    }
                                });
                    }
                };
        Map<String, Integer> map = DataReader.getTypedMap(Integer.class, data, "INT_MAP");
        assertEquals(
                map,
                new HashMap<String, Integer>() {
                    {
                        put("a", 1);
                        put("b", null);
                    }
                });
    }

    @Test
    public void testOptTypedMap() {
        Map<String, Integer> defaultMap =
                new HashMap<String, Integer>() {
                    {
                        put("c", 3);
                        put("d", 4);
                    }
                };

        Map<String, Integer> map =
                DataReader.optTypedMap(Integer.class, data, "INT_MAP", defaultMap);
        assertEquals(
                map,
                new HashMap<String, Integer>() {
                    {
                        put("a", 1);
                        put("b", 2);
                    }
                });

        map = DataReader.optTypedMap(Integer.class, data, "STRING", defaultMap);
        assertEquals(map, defaultMap);
        map = DataReader.optTypedMap(Integer.class, data, "STRING_MAP", defaultMap);
        assertEquals(map, defaultMap);

        map = DataReader.optTypedMap(null, data, "INT_MAP", defaultMap);
        assertEquals(map, defaultMap);
        map = DataReader.optTypedMap(Integer.class, null, "INT_MAP", defaultMap);
        assertEquals(map, defaultMap);
        map = DataReader.optTypedMap(Integer.class, data, "null", defaultMap);
        assertEquals(map, defaultMap);
    }

    @Test
    public void testGetTypedList() throws Exception {
        List<Integer> list = DataReader.getTypedList(Integer.class, data, "INT_LIST");
        assertEquals(list, Arrays.asList(1, 2));

        checkExceptionMessage(
                key -> DataReader.getTypedList(Integer.class, data, key),
                Arrays.asList("STRING", "INT", "STRING_MAP"),
                LIST_ERROR_MSG);
        checkExceptionMessage(
                key -> DataReader.getTypedList(Integer.class, data, key),
                Arrays.asList("STRING_LIST"),
                LIST_ENTRY_ERROR_MSG);
        checkNullArguments((m, k) -> DataReader.getTypedList(Integer.class, m, k));
    }

    @Test
    public void testOptTypedList() {
        List<Integer> defaultList = Arrays.asList(3, 4);

        List<Integer> list;
        list = DataReader.optTypedList(Integer.class, data, "INT_LIST", defaultList);
        assertEquals(list, Arrays.asList(1, 2));

        list = DataReader.optTypedList(Integer.class, data, "STRING", defaultList);
        assertEquals(list, defaultList);
        list = DataReader.optTypedList(Integer.class, data, "STRING_LIST", defaultList);
        assertEquals(list, defaultList);

        list = DataReader.optTypedList(Integer.class, null, "STRING_LIST", defaultList);
        assertEquals(list, defaultList);
        list = DataReader.optTypedList(Integer.class, data, null, defaultList);
        assertEquals(list, defaultList);
        list = DataReader.optTypedList(null, data, "INT_LIST", defaultList);
        assertEquals(list, defaultList);
    }

    @Test
    public void testReadNestedValue() throws Exception {
        Map<String, Object> clonedData = EventDataUtils.immutableClone(data);

        Map<String, Integer> map = DataReader.getTypedMap(Integer.class, clonedData, "INT_MAP");
        int value = DataReader.getInt(map, "a");
        assertEquals(value, 1);

        List<String> stringList = DataReader.getStringList(clonedData, "STRING_LIST");
        assertEquals("a", stringList.get(0));
    }

    @Test
    public void testGetTypedListOfMap() throws Exception {
        List<Map<String, Integer>> list =
                DataReader.getTypedListOfMap(Integer.class, data, "INTMAP_LIST");
        assertEquals(
                list,
                Arrays.asList(
                        new HashMap<String, Integer>() {
                            {
                                put("a", 1);
                                put("b", 2);
                            }
                        },
                        new HashMap<String, Integer>() {
                            {
                                put("c", 3);
                                put("d", 4);
                            }
                        }));

        checkExceptionMessage(
                key -> DataReader.getTypedListOfMap(Integer.class, data, key),
                Arrays.asList("STRING", "INT", "STRING_MAP"),
                LIST_ERROR_MSG);
        checkExceptionMessage(
                key -> DataReader.getTypedListOfMap(Integer.class, data, key),
                Arrays.asList("STRING_LIST"),
                LIST_ENTRY_ERROR_MSG);
        checkExceptionMessage(
                key -> DataReader.getTypedListOfMap(Integer.class, data, key),
                Arrays.asList("STRINGMAP_LIST"),
                MAP_ENTRY_ERROR_MSG);
        checkNullArguments((m, k) -> DataReader.getTypedList(Integer.class, m, k));
    }

    @Test
    public void testOptTypedListOfMap() {
        List<Map<String, Integer>> defaultList = new ArrayList<>();

        List<Map<String, Integer>> list;
        list = DataReader.optTypedListOfMap(Integer.class, data, "INTMAP_LIST", defaultList);
        assertEquals(
                list,
                Arrays.asList(
                        new HashMap<String, Integer>() {
                            {
                                put("a", 1);
                                put("b", 2);
                            }
                        },
                        new HashMap<String, Integer>() {
                            {
                                put("c", 3);
                                put("d", 4);
                            }
                        }));

        list = DataReader.optTypedListOfMap(Integer.class, data, "STRING", defaultList);
        assertEquals(list, defaultList);
        list = DataReader.optTypedListOfMap(Integer.class, data, "STRING_LIST", defaultList);
        assertEquals(list, defaultList);

        list = DataReader.optTypedListOfMap(Integer.class, null, "STRING_LIST", defaultList);
        assertEquals(list, defaultList);
        list = DataReader.optTypedListOfMap(Integer.class, data, null, defaultList);
        assertEquals(list, defaultList);
        list = DataReader.optTypedListOfMap(null, data, "INT_LIST", defaultList);
        assertEquals(list, defaultList);
        list = DataReader.optTypedListOfMap(null, data, "STRINGMAP_LIST", defaultList);
        assertEquals(list, defaultList);
    }
}
