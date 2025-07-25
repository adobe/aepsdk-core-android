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
    fun testSimpleMapFlatteningWithAllPrimitiveTypes() {
        val map = mapOf(
            "outerKey" to mapOf(
                "a" to "string_value",
                "b" to 123,
                "c" to 45.67,
                "d" to false,
                "e" to null
            )
        )
        val flattenedMap = map.flattening()
        val expectedMap = mapOf(
            "outerKey.a" to "string_value",
            "outerKey.b" to 123,
            "outerKey.c" to 45.67,
            "outerKey.d" to false,
            "outerKey.e" to null
        )
        assertEquals(5, flattenedMap.size)
        assertEquals(expectedMap, flattenedMap)
    }

    @Test
    fun testMapFlatteningWithList() {
        val map = mapOf(
            "a" to mapOf(
                "b" to mapOf(
                    "c" to "a_b_c_value"
                )
            ),
            "d" to mapOf(
                "e" to listOf(
                    mapOf(
                        "value1" to "d_e_value1"
                    ),
                    mapOf(
                        "value2" to "d_e_value2"
                    )
                ),
            )
        )
        val flattenedMap = map.flattening()
        val expectedMap = mapOf(
            "a.b.c" to "a_b_c_value",
            "d.e.0.value1" to "d_e_value1",
            "d.e.1.value2" to "d_e_value2"
        )
        assertEquals(3, flattenedMap.size)
        assertEquals(expectedMap, flattenedMap)
    }

    @Test
    fun testMapFlatteningWithListOfAllPrimitiveTypes() {
        val map = mapOf(
            "a" to mapOf(
                "b" to mapOf(
                    "c" to "a_b_c_value"
                )
            ),
            "d" to mapOf(
                "e" to listOf(
                    "stringInArray", 123, 4.56, false, null
                )
            )
        )
        val flattenedMap = map.flattening()
        val expectedMap = mapOf(
            "a.b.c" to "a_b_c_value",
            "d.e.0" to "stringInArray",
            "d.e.1" to 123,
            "d.e.2" to 4.56,
            "d.e.3" to false,
            "d.e.4" to null
        )
        assertEquals(6, flattenedMap.size)
        assertEquals(expectedMap, flattenedMap)
    }

    @Test
    fun testMapFlatteningWithListFlatteningDisabled() {
        val map = mapOf(
            "a" to mapOf(
                "b" to mapOf(
                    "c" to "a_b_c_value"
                )
            ),
            "d" to mapOf(
                "e" to listOf(
                    mapOf(
                        "value1" to "d_e_value1"
                    ),
                    mapOf(
                        "value2" to "d_e_value2"
                    )
                ),
            )
        )
        val flattenedMap = map.flattening(flattenListAndArray = false)
        assertEquals(2, flattenedMap.size)
        assertEquals("a_b_c_value", flattenedMap["a.b.c"])
        assertTrue(flattenedMap["d.e"] is List<*>)
    }

    @Test
    fun testMapFlatteningWithNestedLists() {
        val map = mapOf(
            "a" to listOf(
                listOf("nested1", "nested2"),
                listOf("nested3", "nested4")
            )
        )
        val flattenedMap = map.flattening()
        val expectedMap = mapOf(
            "a.0.0" to "nested1",
            "a.0.1" to "nested2",
            "a.1.0" to "nested3",
            "a.1.1" to "nested4"
        )
        assertEquals(expectedMap, flattenedMap)
    }

    @Test
    fun testMapFlatteningWithMapsInsideLists() {
        val map = mapOf(
            "a" to listOf(
                mapOf("key1" to "value1", "key2" to "value2"),
                mapOf("key3" to "value3", "key4" to "value4")
            )
        )
        val flattenedMap = map.flattening()
        val expectedMap = mapOf(
            "a.0.key1" to "value1",
            "a.0.key2" to "value2",
            "a.1.key3" to "value3",
            "a.1.key4" to "value4"
        )
        assertEquals(expectedMap, flattenedMap)
    }

    @Test
    fun testMapFlatteningWithComplexNestedStructures() {
        val map = mapOf(
            "root" to mapOf(
                "list1" to listOf(
                    mapOf(
                        "nestedList" to listOf("a", "b"),
                        "nestedMap" to mapOf("x" to "y")
                    ),
                    mapOf(
                        "nestedList" to listOf("c", "d"),
                        "nestedMap" to mapOf("z" to "w")
                    )
                ),
                "map1" to mapOf(
                    "list2" to listOf(
                        mapOf("p" to "q"),
                        mapOf("r" to "s")
                    )
                )
            )
        )
        val flattenedMap = map.flattening()
        val expectedMap = mapOf(
            "root.list1.0.nestedList.0" to "a",
            "root.list1.0.nestedList.1" to "b",
            "root.list1.0.nestedMap.x" to "y",
            "root.list1.1.nestedList.0" to "c",
            "root.list1.1.nestedList.1" to "d",
            "root.list1.1.nestedMap.z" to "w",
            "root.map1.list2.0.p" to "q",
            "root.map1.list2.1.r" to "s"
        )
        assertEquals(expectedMap, flattenedMap)
    }

    @Test
    fun testMapFlatteningWithArrays() {
        val map = mapOf(
            "a" to arrayOf(
                arrayOf("nested1", "nested2"),
                arrayOf("nested3", "nested4")
            )
        )
        val flattenedMap = map.flattening()
        val expectedMap = mapOf(
            "a.0.0" to "nested1",
            "a.0.1" to "nested2",
            "a.1.0" to "nested3",
            "a.1.1" to "nested4"
        )
        assertEquals(expectedMap, flattenedMap)
    }

    @Test
    fun testMapFlatteningWithArraysOfAllPrimitiveTypes() {
        val map = mapOf(
            "a" to arrayOf(
                "string_value",
                123,
                45.67,
                false,
                null
            )
        )
        val flattenedMap = map.flattening()
        val expectedMap = mapOf(
            "a.0" to "string_value",
            "a.1" to 123,
            "a.2" to 45.67,
            "a.3" to false,
            "a.4" to null
        )
        assertEquals(expectedMap, flattenedMap)
    }

    @Test
    fun testMapFlatteningWithArraysFlatteningDisabled() {
        val map = mapOf(
            "a" to mapOf(
                "b" to mapOf(
                    "c" to "a_b_c_value"
                )
            ),
            "d" to mapOf(
                "e" to arrayOf(
                    mapOf(
                        "value1" to "d_e_value1"
                    ),
                    mapOf(
                        "value2" to "d_e_value2"
                    )
                ),
            )
        )
        val flattenedMap = map.flattening(flattenListAndArray = false)
        assertEquals(2, flattenedMap.size)
        assertEquals("a_b_c_value", flattenedMap["a.b.c"])
        assertTrue(flattenedMap["d.e"] is Array<*>)
    }

    @Test
    fun testMapFlatteningWithMapsInsideArrays() {
        val map = mapOf(
            "a" to arrayOf(
                mapOf("key1" to "value1", "key2" to "value2"),
                mapOf("key3" to "value3", "key4" to "value4")
            )
        )
        val flattenedMap = map.flattening()
        val expectedMap = mapOf(
            "a.0.key1" to "value1",
            "a.0.key2" to "value2",
            "a.1.key3" to "value3",
            "a.1.key4" to "value4"
        )
        assertEquals(expectedMap, flattenedMap)
    }

    @Test
    fun testMapFlatteningWithMixedArraysAndLists() {
        val map = mapOf(
            "a" to listOf(
                arrayOf("nested1", "nested2"),
                listOf("nested3", "nested4")
            )
        )
        val flattenedMap = map.flattening()
        val expectedMap = mapOf(
            "a.0.0" to "nested1",
            "a.0.1" to "nested2",
            "a.1.0" to "nested3",
            "a.1.1" to "nested4"
        )
        assertEquals(expectedMap, flattenedMap)
    }

    @Test
    fun testMapFlatteningNullValueMap() {
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
    fun testMapFlatteningMultipleNestedKeys() {
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
    fun testMapFlatteningContainsNonStringKeys() {
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
    fun testMapFlatteningWithDotInKey() {
        val map = mapOf(
            "a.b" to 1,
            "a" to mapOf(
                "b" to 2
            )
        )
        val flattenedMap = map.flattening()
        assertEquals(1, flattenedMap.size)
        assertEquals("a.b", flattenedMap.keys.first())
    }

    @Test
    fun testFlatteningWithEmptyMap() {
        val map = emptyMap<String, Any>()
        val flattenedMap = map.flattening()
        assertTrue(flattenedMap.isEmpty())
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
    fun `test fnv1a32 - Char`() {
        val eventData = mapOf(
            "key" to 'a'
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
    fun `test fnv1a32 - Long`() {
        val eventData = mapOf(
            "key" to 24L
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
    fun `test fnv1a32 - Double`() {
        val eventData = mapOf(
            "key" to "5.52".toDouble()
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
    fun `test fnv1a32 - mask is null`() {
        val eventData = mapOf(
            "key" to "value"
        )
        val hashCode = eventData.fnv1a32(null)
        assertEquals(4007910315, hashCode)
    }

    @Test
    fun `test fnv1a32 - mask is empty`() {
        val eventData = mapOf(
            "key" to "value"
        )
        val hashCode = eventData.fnv1a32(emptyArray())
        assertEquals(0, hashCode)
    }

    @Test
    fun `test fnv1a32 - event data is empty`() {
        val eventData = emptyMap<String, Any>()
        val hashCode = eventData.fnv1a32(emptyArray())
        assertEquals(0, hashCode)
    }

    @Test
    fun `test fnv1a32 - event data has null value`() {
        val eventData = mapOf(
            "key" to null
        )
        val hashCode = eventData.fnv1a32(emptyArray())
        assertEquals(0, hashCode)
    }

    @Test
    fun `test fnv1a32 - event data has empty value`() {
        val eventData = mapOf(
            "key" to ""
        )
        val hashCode = eventData.fnv1a32(emptyArray())
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
