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
}
