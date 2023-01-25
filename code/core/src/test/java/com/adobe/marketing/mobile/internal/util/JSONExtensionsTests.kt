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

import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JSONExtensionsTests {

    @Test
    fun testJSONObjectToMapBasic() {
        val jsonString = """
        {
          "IntKey": 123,
          "StringKey": "StringValue",
          "DoubleKey": 1.23,
          "NullKey": null,
          "ObjectKey": {
            "innerKey": "innerValue"
          },
          "ArrayKey": [
            "StringValue",
            123,
            1.23,
            null,
            {
              "objKey": "objValue"
            }
          ]
        }
        """.trimIndent()
        val jsonObject = JSONTokener(jsonString).nextValue()
        assertTrue(jsonObject is JSONObject)
        val expectedMap = mapOf(
            "IntKey" to 123,
            "StringKey" to "StringValue",
            "DoubleKey" to 1.23,
            "NullKey" to null,
            "ObjectKey" to mapOf(
                "innerKey" to "innerValue"
            ),
            "ArrayKey" to listOf(
                "StringValue",
                123,
                1.23,
                null,
                mapOf(
                    "objKey" to "objValue"
                )
            )
        )
        assertEquals(expectedMap, jsonObject.toMap())
    }

    @Test
    fun testJSONObjectToNestedMap() {
        val jsonString = """
        {
          "rootKey": "rootValue",
          "nestedMap1": {
            "key1": "value1",
            "nestedMap2": {
              "key2": "value2",
              "nestedMap3": {
                "key3": "value3"
              }
            }
          }
        }
        """.trimIndent()
        val jsonObject = JSONTokener(jsonString).nextValue()
        assertTrue(jsonObject is JSONObject)
        val expectedMap = mapOf(
            "rootKey" to "rootValue",
            "nestedMap1" to mapOf(
                "key1" to "value1",
                "nestedMap2" to mapOf(
                    "key2" to "value2",
                    "nestedMap3" to mapOf(
                        "key3" to "value3"
                    )
                )
            )
        )
        assertEquals(expectedMap, jsonObject.toMap())
    }

    @Test
    fun testJSONArrayMappingBasic() {
        val jsonString = """
        [
          "a",
          "b",
          "c"
        ]
        """.trimIndent()
        val jsonArray = JSONTokener(jsonString).nextValue()
        assertTrue(jsonArray is JSONArray)
        val expectedList = listOf(
            "a",
            "b",
            "c"
        )
        assertEquals(
            expectedList,
            jsonArray.map {
                if (it is String) it else ""
            }
        )
    }

    @Test
    fun testJSONArrayMappingToMapList() {
        val jsonString = """
        [
          {
            "name": "obj1",
            "key": "value"
          },
          {
            "name": "obj2",
            "key": "value"
          }
        ]
        """.trimIndent()
        val jsonArray = JSONTokener(jsonString).nextValue()
        assertTrue(jsonArray is JSONArray)
        val expectedList = listOf(
            mapOf(
                "name" to "obj1",
                "key" to "value"
            ),
            mapOf(
                "name" to "obj2",
                "key" to "value"
            )
        )
        assertEquals(
            expectedList,
            jsonArray.map {
                if (it is JSONObject) it.toMap() else null
            }
        )
    }

    @Test
    fun testJSONArrayToAnyList() {
        val jsonString = """
        [
          {
            "name": "obj1",
            "key": "value"
          },
          {
            "name": "obj2",
            "key": "value"
          }
        ]
        """.trimIndent()
        val jsonArray = JSONTokener(jsonString).nextValue()
        assertTrue(jsonArray is JSONArray)
        val expectedList = listOf(
            mapOf(
                "name" to "obj1",
                "key" to "value"
            ),
            mapOf(
                "name" to "obj2",
                "key" to "value"
            )
        )
        assertEquals(expectedList, jsonArray.toList())
    }
}
