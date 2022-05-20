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
package com.adobe.marketing.mobile.internal.utility

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.Test

class MapExtensionsTests {

    @Test
    fun testSimpleMapFlattening() {
        val map = mapOf(
            "a" to mapOf(
                "b" to mapOf(
                    "c" to "a_b_c_value"
                )
            ),
            "d" to "d_value"
        )
        val flattenedMap = map.flattening()
        val expectedMap = mapOf(
            "a.b.c" to "a_b_c_value",
            "d" to "d_value"
        )
        assertEquals(2, flattenedMap.size)
        assertEquals(expectedMap, flattenedMap)
    }

    @Test
    fun testNullValueMapFlattening() {
        val map = mapOf(
            "a" to mapOf(
                "b1" to mapOf(
                    "c" to "a_b1_c_value"
                ),
                "b2" to null
            ),
            "d" to "d_value"
        )
        val flattenedMap = map.flattening()
        val expectedMap = mapOf(
            "a.b1.c" to "a_b1_c_value",
            "a.b2" to null,
            "d" to "d_value"
        )
        assertEquals(3, flattenedMap.size)
        assertEquals(expectedMap, flattenedMap)
    }

    @Test
    fun testMultipleNestedKeysMapFlattening() {
        val map = mapOf(
            "a" to mapOf(
                "b1" to mapOf(
                    "c1" to "a_b1_c1_value"
                ),
                "b2" to mapOf(
                    "c2" to "a_b2_c2_value"
                ),
                "b3" to mapOf(
                    "c3" to "a_b3_c3_value",
                    "c4" to "a_b3_c4_value"
                ),
                "b4" to "a_b4_value"
            ),
            "d" to "d_value"
        )
        val flattenedMap = map.flattening()
        assertEquals(6, flattenedMap.size)
        val expectedMap = mapOf(
            "a.b1.c1" to "a_b1_c1_value",
            "a.b2.c2" to "a_b2_c2_value",
            "a.b3.c3" to "a_b3_c3_value",
            "a.b3.c4" to "a_b3_c4_value",
            "a.b4" to "a_b4_value",
            "d" to "d_value"
        )
        assertEquals(expectedMap, flattenedMap)
    }

    @Test
    fun testContainsNonStringKeysMapFlattening() {
        val map = mapOf(
            "a" to mapOf(
                "b1" to mapOf(
                    "c1" to "a_b1_c1_value"
                ),
                "b2" to mapOf(
                    1 to "a_b2_value",
                    2 to "a_b2_value"
                ),
                "b3" to mapOf(
                    1 to "a_b3_value",
                    "2" to "a_b3_value"
                )
            ),
            "d" to "d_value"
        )
        val flattenedMap = map.flattening()
        assertEquals(4, flattenedMap.size)
        val expectedMap = mapOf(
            "a.b1.c1" to "a_b1_c1_value",
            "a.b2" to mapOf(
                1 to "a_b2_value",
                2 to "a_b2_value"
            ),
            "a.b3" to mapOf(
                1 to "a_b3_value",
                "2" to "a_b3_value"
            ),
            "d" to "d_value"
        )
        assertEquals(expectedMap, flattenedMap)
    }

    @Test
    @Throws(Exception::class)
    fun getFlattenedMap_ReturnsFlattenedMap_WhenEventDataNotNull() {
        val map = mapOf(
            "boolKey" to "true",
            "intKey" to 1,
            "longKey" to 100L,
            "stringKey" to "stringValue",
            "mapStrKey" to mapOf(
                "mapKey" to "mapValue"
            )
        )
        val flattenedMap = map.getFlattenedDataMap()
        val expectedMap = mapOf(
            "boolKey" to "true",
            "intKey" to 1,
            "longKey" to 100L,
            "stringKey" to "stringValue",
            "mapStrKey" to mapOf(
                "mapKey" to "mapValue"
            ),
            "mapStrKey.mapKey" to "mapValue"
        )
        assertEquals(expectedMap, flattenedMap)
    }

    @Test
    fun testSerializeToQueryString() {
        val dict = HashMap<String, Any?>()
        dict["key1"] = "val1"
        dict["key2"] = "val2"
        dict["key3"] = "val3"
        val valueUnderTest = dict.serializeToQueryString()
        assertTrue(valueUnderTest.contains("key3=val3"))
        assertTrue(valueUnderTest.contains("key2=val2"))
        assertTrue(valueUnderTest.contains("key1=val1"))
    }

    @Test
    fun testSerializeToQueryStringNullInput() {
        val dict = null
        val valueUnderTest = dict?.serializeToQueryString()
        assertNull(valueUnderTest)
    }

    @Test
    fun testSerializeToQueryStringNullValueParameter() {
        val dict = HashMap<String, Any?>()
        dict["key1"] = "val1"
        dict["key2"] = null
        val valueUnderTest = dict.serializeToQueryString()
        assertTrue(valueUnderTest.contains("key1=val1"))
        assertFalse(valueUnderTest.contains("key2=val2"))
    }

    @Test
    fun testSerializeToQueryStringEmptyKeyParameter() {
        val dict = HashMap<String, Any?>()
        dict["key1"] = "val1"
        dict[""] = "val2"
        val valueUnderTest = dict.serializeToQueryString()
        assertTrue(valueUnderTest.contains("key1=val1"))
        assertFalse(valueUnderTest.contains("key2=val2"))
    }

    @Test
    fun testSerializeToQueryStringEmptyValueParameter() {
        val dict = HashMap<String, Any?>()
        dict["key1"] = "val1"
        dict["key2"] = ""
        val valueUnderTest = dict.serializeToQueryString()
        assertTrue(valueUnderTest.contains("key1=val1"))
        assertTrue(valueUnderTest.contains("key2="))
    }

    @Test
    fun testSerializeToQueryStringNonString() {
        val dict = HashMap<String, Any?>()
        dict["key1"] = 5
        val valueUnderTest = dict.serializeToQueryString()
        assertEquals("key1=5", valueUnderTest)
    }

    @Test
    fun testSerializeToQueryStringArrayList() {
        val list = ArrayList<String>()
        list.add("TestArrayList1")
        list.add("TestArrayList2")
        list.add("TestArrayList3")
        list.add("TestArrayList4")
        val dict = HashMap<String, Any?>()
        dict["key1"] = list
        val valueUnderTest = dict.serializeToQueryString()
        assertEquals(
            "key1=TestArrayList1%2CTestArrayList2%2CTestArrayList3%2CTestArrayList4",
            valueUnderTest
        )
    }

    @Test
    fun testSerializeToQueryStringArrayListNullObject() {
        val list = ArrayList<String?>()
        list.add("TestArrayList1")
        list.add("TestArrayList2")
        list.add(null)
        list.add("TestArrayList4")
        val dict = HashMap<String, Any?>()
        dict["key1"] = list
        val valueUnderTest = dict.serializeToQueryString()
        assertEquals(
            "key1=TestArrayList1%2CTestArrayList2%2Cnull%2CTestArrayList4",
            valueUnderTest
        )
    }

    @Test
    fun testSerializeToQueryStringArrayListEmptyObject() {
        val list = ArrayList<String>()
        list.add("TestArrayList1")
        list.add("TestArrayList2")
        list.add("")
        list.add("TestArrayList4")
        val dict = HashMap<String, Any?>()
        dict["key1"] = list
        val valueUnderTest = dict.serializeToQueryString()
        assertEquals(
            "key1=TestArrayList1%2CTestArrayList2%2C%2CTestArrayList4",
            valueUnderTest
        )
    }
}
