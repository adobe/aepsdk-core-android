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

package com.adobe.marketing.mobile.internal.util

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

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

    // test "fnv1a32" hash algorithm and make sure the result should be the same in both iOS and Android SDKs.
    // tests in Swift Core => https://github.com/adobe/aepsdk-core-ios/blob/main/AEPCore/Tests/EventHubTests/HistoryTests/EventData%2BFNV1A32Tests.swift
    // Validations of this class are done against an online hash calculator: https://md5calc.com/hash/fnv1a32?str=
    // decimal to hex online converter: https://www.rapidtables.com/convert/number/decimal-to-hex.html
    @Test
    fun `test fnv1a32 - String`() {
        val eventData = mapOf(
            "key" to "value"
        )
        val hashCode = eventData.fnv1a32()
        assertEquals(4007910315, hashCode)
    }

    @Test
    fun `test fnv1a32 - optional String`() {
        val optional: String? = "value"
        val eventData = mapOf(
            "key" to optional
        )
        val hashCode = eventData.fnv1a32()
        assertEquals(4007910315, hashCode)
    }

    @Test
    fun `test fnv1a32 - Char`() {
        val eventData = mapOf(
            "key" to 'a'
        )
        val hashCode = eventData.fnv1a32()
        assertEquals(135500217, hashCode)
    }

    @Test
    fun `test fnv1a32 - optional Char`() {
        val optional: Char? = 'a'
        val eventData = mapOf(
            "key" to optional
        )
        val hashCode = eventData.fnv1a32()
        assertEquals(135500217, hashCode)
    }

    @Test
    fun `test fnv1a32 - Int`() {
        val eventData = mapOf(
            "key" to 552
        )
        val hashCode = eventData.fnv1a32()
        assertEquals(874166902, hashCode)
    }

    @Test
    fun `test fnv1a32 - optional Int`() {
        val optional: Int? = 552
        val eventData = mapOf(
            "key" to optional
        )
        val hashCode = eventData.fnv1a32()
        assertEquals(874166902, hashCode)
    }

    @Test
    fun `test fnv1a32 - Long`() {
        val eventData = mapOf(
            "key" to 24L
        )
        val hashCode = eventData.fnv1a32()
        assertEquals(2995581580, hashCode)
    }

    @Test
    fun `test fnv1a32 - optional Long`() {
        val optional: Long? = 24L
        val eventData = mapOf(
            "key" to optional
        )
        val hashCode = eventData.fnv1a32()
        assertEquals(2995581580, hashCode)
    }

    @Test
    fun `test fnv1a32 - Float`() {
        val eventData = mapOf(
            "key" to 5.52f
        )
        val hashCode = eventData.fnv1a32()
        assertEquals(1449854826, hashCode)
    }

    @Test
    fun `test fnv1a32 - optional Float`() {
        val optional: Float? = 5.52f
        val eventData = mapOf(
            "key" to optional
        )
        val hashCode = eventData.fnv1a32()
        assertEquals(1449854826, hashCode)
    }

    @Test
    fun `test fnv1a32 - Double`() {
        val eventData = mapOf(
            "key" to "5.52".toDouble()
        )
        val hashCode = eventData.fnv1a32()
        assertEquals(1449854826, hashCode)
    }

    @Test
    fun `test fnv1a32 - optional Double`() {
        val optional: Double? = "5.52".toDouble()
        val eventData = mapOf(
            "key" to optional
        )
        val hashCode = eventData.fnv1a32()
        assertEquals(1449854826, hashCode)
    }

    @Test
    fun `test fnv1a32 - Boolean`() {
        val eventData = mapOf(
            "key" to false
        )
        val hashCode = eventData.fnv1a32()
        assertEquals(138493769, hashCode)
    }

    @Test
    fun `test fnv1a32 - optional Boolean`() {
        val optional: Boolean? = false
        val eventData = mapOf(
            "key" to optional
        )
        val hashCode = eventData.fnv1a32()
        assertEquals(138493769, hashCode)
    }

    @Test
    fun `test fnv1a32 - mask key is present`() {
        val eventData = mapOf(
            "key" to "value",
            "unusedKey" to "unusedValue"
        )
        val hashCode = eventData.fnv1a32(arrayOf("key"))
        assertEquals(4007910315, hashCode)
    }

    @Test
    fun `test fnv1a32 - mask key is not present`() {
        val eventData = mapOf(
            "key" to "value"
        )
        val hashCode = eventData.fnv1a32(arrayOf("404"))
        assertEquals(0, hashCode)
    }

    @Test
    fun `test fnv1a32 - get keys Ascii sorted`() {
        val hashCode1 = mapOf(
            "key" to "value",
            "number" to 1234,
            "UpperCase" to "abc",
            "_underscore" to "score"
        ).fnv1a32()
        val hashCode2 = mapOf(
            "number" to 1234,
            "key" to "value",
            "_underscore" to "score",
            "UpperCase" to "abc"
        ).fnv1a32()
        assertEquals(960895195, hashCode1)
        assertEquals(hashCode2, hashCode1)
    }

    @Test
    fun `test fnv1a32 - big sort`() {
        val hashCode = mapOf(
            "a" to "1",
            "A" to "2",
            "ba" to "3",
            "Ba" to "4",
            "Z" to "5",
            "z" to "6",
            "r" to "7",
            "R" to "8",
            "bc" to "9",
            "Bc" to "10",
            "1" to 1,
            "222" to 222
        ).fnv1a32()
        assertEquals(2933724447, hashCode)
    }

    @Test
    fun `test prettify map`() {
        val data = mapOf(
            "a" to "13435454",
            "b" to mapOf(
                "b1" to 1235566,
                "b2" to null
            )
        )
        val expected = """{
    "a": "13435454",
    "b": {"b1": 1235566}
}"""
        assertEquals(expected, data.prettify())
    }
}
