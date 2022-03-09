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

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EventDataMergerTests {
    @Test
    fun testSimpleMerge() {
        val fromMap = mapOf(
            "key" to "oldValue"
        )
        val toMap = mapOf(
            "newKey" to "newValue"
        )
        val result = EventDataMerger.merge(fromMap, toMap, true)
        assertEquals(2, result?.keys?.size)
        assertTrue(result?.containsKey("key") == true)
        assertTrue(result?.containsKey("newKey") == true)
    }
    @Test
    fun testConflictAndNotOverwrite() {
        val toMap = mapOf(
            "key" to "oldValue",
            "donotdelete" to "value"
        )
        val fromMap = mapOf(
            "newKey" to "newValue",
            "donotdelete" to  null
        )
        val result = EventDataMerger.merge(fromMap, toMap, false)
        assertEquals(3, result?.keys?.size)
        assertTrue(result?.containsKey("key") == true)
        assertTrue(result?.containsKey("newKey") == true)
        assertEquals("value", result?.get("donotdelete"))
    }
    @Test
    fun testNestedMapSimpleMerge() {
        val toMap = mapOf(
            "nested" to mapOf(
                "key" to "oldValue"
            )
        )
        val fromMap = mapOf(
            "nested" to mapOf(
                "newKey" to "newValue"
            )
        )
        val result = EventDataMerger.merge(fromMap, toMap, true)
        assertEquals(1, result?.keys?.size)
        val nestedMap = result?.get("nested") as? Map<*, *>
        assertEquals(2, nestedMap?.keys?.size)
        assertEquals("oldValue", nestedMap?.get("key"))
        assertEquals("newValue", nestedMap?.get("newKey"))
    }
    @Test
    fun testNestedMapConflictAndOverwrite() {
        val toMap = mapOf(
            "nested" to mapOf(
                "key" to "oldValue",
                "toBeDeleted" to "value"
            )
        )
        val fromMap = mapOf(
            "nested" to mapOf(
                "newKey" to "newValue",
                "toBeDeleted" to null
            )
        )
        val result = EventDataMerger.merge(fromMap, toMap, true)
        assertEquals(1, result?.keys?.size)
        val nestedMap = result?.get("nested") as? Map<*, *>
        assertEquals(2, nestedMap?.keys?.size)
        assertEquals("oldValue", nestedMap?.get("key"))
        assertEquals("newValue", nestedMap?.get("newKey"))
    }
}