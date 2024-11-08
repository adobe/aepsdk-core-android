/*
  Copyright 2024 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.util

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JSONAssertsHelpersTests {
    @Test
    fun testJSONRepresentation_whenBothValuesAreNull_shouldPass() {
        val result = JSONAsserts.getJSONRepresentation(null)
        assertEquals(JSONObject.NULL, result)
    }

    @Test
    fun testJSONRepresentation_whenValueIsJSONObject_shouldPass() {
        val jsonObject = JSONObject(mapOf("key" to "value"))
        val result = JSONAsserts.getJSONRepresentation(jsonObject)
        assertEquals(jsonObject, result)
    }

    @Test
    fun testJSONRepresentation_whenValueIsJSONArray_shouldPass() {
        val jsonArray = JSONArray(listOf("value1", "value2"))
        val result = JSONAsserts.getJSONRepresentation(jsonArray)
        assertEquals(jsonArray, result)
    }

    @Test
    fun testJSONRepresentation_whenValueIsStringRepresentingJSONObject_shouldPass() {
        val jsonString = """{"key":"value"}"""
        val result = JSONAsserts.getJSONRepresentation(jsonString)
        assertTrue(result is JSONObject)
        assertEquals("value", (result as JSONObject).getString("key"))
    }

    @Test
    fun testJSONRepresentation_whenValueIsStringRepresentingJSONArray_shouldPass() {
        val jsonString = """["value1", "value2"]"""
        val result = JSONAsserts.getJSONRepresentation(jsonString)
        assertTrue(result is JSONArray)
        assertEquals("value1", (result as JSONArray).getString(0))
        assertEquals("value2", (result as JSONArray).getString(1))
    }

    @Test
    fun testJSONRepresentation_whenKeyIsInvalidJSONString_shouldPass() {
        val jsonString = """{key:"value"}""" // Invalid JSON key string
        val result = JSONAsserts.getJSONRepresentation(jsonString)
        assertTrue(result is JSONObject)
        val jsonObject = result as JSONObject
        assertEquals("value", jsonObject.getString("key"))
    }

    @Test
    fun testJSONRepresentation_whenValueIsMapWithNullValues_shouldReplaceNullWithJSONObjectNull() {
        val map = mapOf("key1" to "value1", "key2" to null)
        val result = JSONAsserts.getJSONRepresentation(map)
        assertTrue(result is JSONObject)
        val jsonObject = result as JSONObject
        assertEquals("value1", jsonObject.getString("key1"))
        assertEquals(JSONObject.NULL, jsonObject.get("key2"))
    }

    @Test
    fun testJSONRepresentation_whenValueIsListWithNullValues_shouldReplaceNullWithJSONObjectNull() {
        val list = listOf("value1", null, "value2")
        val result = JSONAsserts.getJSONRepresentation(list)
        assertTrue(result is JSONArray)
        val jsonArray = result as JSONArray
        assertEquals("value1", jsonArray.getString(0))
        assertEquals(JSONObject.NULL, jsonArray.get(1))
        assertEquals("value2", jsonArray.getString(2))
    }

    @Test
    fun testJSONRepresentation_whenValueIsArrayWithNullValues_shouldReplaceNullWithJSONObjectNull() {
        val array = arrayOf("value1", null, "value2")
        val result = JSONAsserts.getJSONRepresentation(array)
        assertTrue(result is JSONArray)
        val jsonArray = result as JSONArray
        assertEquals("value1", jsonArray.getString(0))
        assertEquals(JSONObject.NULL, jsonArray.get(1))
        assertEquals("value2", jsonArray.getString(2))
    }

    // region - Nested collection tests
    @Test
    fun testNestedMapToListWithNullValue() {
        val input = mapOf(
            "key1" to listOf("value1", null, "value2")
        )
        val expected = JSONObject(
            mapOf(
                "key1" to JSONArray(listOf("value1", JSONObject.NULL, "value2"))
            )
        )
        val result = JSONAsserts.getJSONRepresentation(input)
        assertEquals(expected.toString(), result.toString())
    }

    @Test
    fun testNestedMapToArrayWithNullValue() {
        val input = mapOf(
            "key1" to arrayOf("value1", null, "value2")
        )
        val expected = JSONObject(
            mapOf(
                "key1" to JSONArray(listOf("value1", JSONObject.NULL, "value2"))
            )
        )
        val result = JSONAsserts.getJSONRepresentation(input)
        assertEquals(expected.toString(), result.toString())
    }

    @Test
    fun testNestedArrayToMapWithNullValue() {
        val input = arrayOf(
            mapOf("key1" to "value1", "key2" to null)
        )
        val expected = JSONArray(
            listOf(
                JSONObject(
                    mapOf("key1" to "value1", "key2" to JSONObject.NULL)
                )
            )
        )
        val result = JSONAsserts.getJSONRepresentation(input)
        assertEquals(expected.toString(), result.toString())
    }

    @Test
    fun testNestedListToMapWithNullValue() {
        val input = listOf(
            mapOf("key1" to "value1", "key2" to null)
        )
        val expected = JSONArray(
            listOf(
                JSONObject(
                    mapOf("key1" to "value1", "key2" to JSONObject.NULL)
                )
            )
        )
        val result = JSONAsserts.getJSONRepresentation(input)
        assertEquals(expected.toString(), result.toString())
    }

    @Test
    fun testNestedArrayToListWithNullValue() {
        val input = arrayOf(
            listOf("value1", null, "value2")
        )
        val expected = JSONArray(
            listOf(
                JSONArray(listOf("value1", JSONObject.NULL, "value2"))
            )
        )
        val result = JSONAsserts.getJSONRepresentation(input)
        assertEquals(expected.toString(), result.toString())
    }

    @Test
    fun testNestedListToArrayWithNullValue() {
        val input = listOf(
            arrayOf("value1", null, "value2")
        )
        val expected = JSONArray(
            listOf(
                JSONArray(listOf("value1", JSONObject.NULL, "value2"))
            )
        )
        val result = JSONAsserts.getJSONRepresentation(input)
        assertEquals(expected.toString(), result.toString())
    }

    @Test
    fun testNestedMapWithinMapWithNullValue() {
        val input = mapOf(
            "key1" to mapOf("nestedKey1" to "value1", "nestedKey2" to null)
        )
        val expected = JSONObject(
            mapOf(
                "key1" to JSONObject(
                    mapOf("nestedKey1" to "value1", "nestedKey2" to JSONObject.NULL)
                )
            )
        )
        val result = JSONAsserts.getJSONRepresentation(input)
        assertEquals(expected.toString(), result.toString())
    }

    @Test
    fun testNestedListWithinListWithNullValue() {
        val input = listOf(
            listOf("value1", null, "value2")
        )
        val expected = JSONArray(
            listOf(
                JSONArray(listOf("value1", JSONObject.NULL, "value2"))
            )
        )
        val result = JSONAsserts.getJSONRepresentation(input)
        assertEquals(expected.toString(), result.toString())
    }

    @Test
    fun testNestedArrayWithinArrayWithNullValue() {
        val input = arrayOf(
            arrayOf("value1", null, "value2")
        )
        val expected = JSONArray(
            listOf(
                JSONArray(listOf("value1", JSONObject.NULL, "value2"))
            )
        )
        val result = JSONAsserts.getJSONRepresentation(input)
        assertEquals(expected.toString(), result.toString())
    }
    // endregion - Nested collection tests

    // region - Invalid input tests
    @Test(expected = IllegalArgumentException::class)
    fun testJSONRepresentation_whenValueIsNotJSONString_shouldThrowException() {
        val value = "simple string"
        JSONAsserts.getJSONRepresentation(value)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testJSONRepresentation_whenValueIsNumber_shouldThrowException() {
        val value = 123.456
        JSONAsserts.getJSONRepresentation(value)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testJSONRepresentation_whenValueIsBoolean_shouldThrowException() {
        val value = true
        JSONAsserts.getJSONRepresentation(value)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testJSONRepresentation_whenValueIsUnsupportedType_shouldThrowException() {
        val value = Any() // Unsupported type
        JSONAsserts.getJSONRepresentation(value)
    }
    // endregion - Invalid input tests
}
