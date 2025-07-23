/*
  Copyright 2025 Adobe. All rights reserved.
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

class ListExtensionsTests {

    @Test
    fun testSimpleListFlattening() {
        val list = listOf("a", "b", "c", "d")
        val flattenedList = list.flattening()
        val expectedMap = mapOf(
            "0" to "a",
            "1" to "b",
            "2" to "c",
            "3" to "d"
        )
        assertEquals(expectedMap, flattenedList)
    }

    @Test
    fun testSimpleListFlatteningWithAllPrimitiveTypes() {
        val list = listOf("string_Value", 1234, 45.67, false, null)
        val flattenedList = list.flattening()
        val expectedMap = mapOf(
            "0" to "string_Value",
            "1" to 1234,
            "2" to 45.67,
            "3" to false,
            "4" to null
        )
        assertEquals(expectedMap, flattenedList)
    }

    @Test
    fun testNestedListFlattening() {
        val list = listOf(
            "a",
            listOf("b1", "b2"),
        )
        val flattenedList = list.flattening()
        val expectedMap = mapOf(
            "0" to "a",
            "1.0" to "b1",
            "1.1" to "b2",
        )
        assertEquals(expectedMap, flattenedList)
    }

    @Test
    fun testListFlatteningWithArray() {
        val list = listOf(
            "a",
            arrayOf("b1", "b2")
        )
        val flattenedList = list.flattening()
        val expectedMap = mapOf(
            "0" to "a",
            "1.0" to "b1",
            "1.1" to "b2"
        )
        assertEquals(expectedMap, flattenedList)
    }

    @Test
    fun testListFlatteningWithMap() {
        val list = listOf(
            "a",
            mapOf("key1" to "value1", "key2" to "value2")
        )
        val flattenedList = list.flattening()
        val expectedMap = mapOf(
            "0" to "a",
            "1.key1" to "value1",
            "1.key2" to "value2",
        )
        assertEquals(expectedMap, flattenedList)
    }

    @Test
    fun testListFlatteningWithMixedTypes() {
        val list = listOf(
            "a",
            listOf("b1", "b2"),
            arrayOf("c1", "c2"),
            mapOf("key1" to "value1")
        )
        val flattenedList = list.flattening()
        val expectedMap = mapOf(
            "0" to "a",
            "1.0" to "b1",
            "1.1" to "b2",
            "2.0" to "c1",
            "2.1" to "c2",
            "3.key1" to "value1"
        )
        assertEquals(expectedMap, flattenedList)
    }

    @Test
    fun testEmptyListFlattening() {
        val list = emptyList<Any?>()
        val flattenedList = list.flattening()
        val expectedMap = emptyMap<String, Any?>()
        assertEquals(expectedMap, flattenedList)
    }

    @Test
    fun testSimpleArrayFlattening() {
        val array = arrayOf("a", "b", "c", "d")
        val flattenedArray = array.flattening()
        val expectedMap = mapOf(
            "0" to "a",
            "1" to "b",
            "2" to "c",
            "3" to "d"
        )
        assertEquals(expectedMap, flattenedArray)
    }

    @Test
    fun testNestedArrayFlattening() {
        val array = arrayOf(
            "a",
            arrayOf("b1", "b2"),
        )
        val flattenedArray = array.flattening()
        val expectedMap = mapOf(
            "0" to "a",
            "1.0" to "b1",
            "1.1" to "b2",
        )
        assertEquals(expectedMap, flattenedArray)
    }

    @Test
    fun testArrayFlatteningWithList() {
        val array = arrayOf(
            "a",
            listOf("b1", "b2")
        )
        val flattenedArray = array.flattening()
        val expectedMap = mapOf(
            "0" to "a",
            "1.0" to "b1",
            "1.1" to "b2"
        )
        assertEquals(expectedMap, flattenedArray)
    }

    @Test
    fun testArrayFlatteningWithMap() {
        val array = arrayOf(
            "a",
            mapOf("key1" to "value1", "key2" to "value2")
        )
        val flattenedArray = array.flattening()
        val expectedMap = mapOf(
            "0" to "a",
            "1.key1" to "value1",
            "1.key2" to "value2",
        )
        assertEquals(expectedMap, flattenedArray)
    }

    @Test
    fun testArrayFlatteningWithMixedTypes() {
        val array = arrayOf(
            "a",
            listOf("b1", "b2"),
            arrayOf("c1", "c2"),
            mapOf("key1" to "value1")
        )
        val flattenedArray = array.flattening()
        val expectedMap = mapOf(
            "0" to "a",
            "1.0" to "b1",
            "1.1" to "b2",
            "2.0" to "c1",
            "2.1" to "c2",
            "3.key1" to "value1"
        )
        assertEquals(expectedMap, flattenedArray)
    }

    @Test
    fun testEmptyArrayFlattening() {
        val array = emptyArray<Any?>()
        val flattenedArray = array.flattening()
        val expectedMap = emptyMap<String, Any?>()
        assertEquals(expectedMap, flattenedArray)
    }
}
