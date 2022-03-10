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
        assertTrue(result.containsKey("key"))
        assertTrue(result.containsKey("newKey"))
    }

    @Test
    fun testConflictAndNotOverwrite() {
        val toMap = mapOf(
            "key" to "oldValue",
            "donotdelete" to "value"
        )
        val fromMap = mapOf(
            "newKey" to "newValue",
            "donotdelete" to null
        )
        val result = EventDataMerger.merge(fromMap, toMap, false)
        assertEquals(3, result.keys.size)
        assertTrue(result.containsKey("key"))
        assertTrue(result.containsKey("newKey"))
        assertEquals("value", result["donotdelete"])
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
        assertEquals(1, result.keys.size)
        val nestedMap = result["nested"] as? Map<*, *>
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
        assertEquals(1, result.keys.size)
        val nestedMap = result["nested"] as? Map<*, *>
        assertEquals(2, nestedMap?.keys?.size)
        assertEquals("oldValue", nestedMap?.get("key"))
        assertEquals("newValue", nestedMap?.get("newKey"))
    }

    @Test
    fun testListSimpleMerge() {
        val fromMap = mapOf(
            "key" to listOf("abc", "def")
        )
        val toMap = mapOf(
            "key" to listOf("0", "1")
        )
        val result = EventDataMerger.merge(fromMap, toMap, true)
        assertEquals(1, result.keys.size)
        assertTrue(result.containsKey("key"))
        val mergedList = result["key"] as? List<Any?>
        assertEquals("abc", mergedList?.get(0))
        assertEquals("def", mergedList?.get(1))
        assertEquals("0", mergedList?.get(2))
        assertEquals("1", mergedList?.get(3))
    }

    @Test
    fun testListWithDuplicatedItems() {
        val fromMap = mapOf(
            "key" to listOf("abc", "def")
        )
        val toMap = mapOf(
            "key" to listOf("abc", "1")
        )
        val result = EventDataMerger.merge(fromMap, toMap, true)
        assertEquals(1, result.keys.size)
        assertTrue(result.containsKey("key"))
        val mergedList = result["key"] as? List<Any?>
        assertEquals("abc", mergedList?.get(0))
        assertEquals("def", mergedList?.get(1))
        assertEquals("abc", mergedList?.get(2))
        assertEquals("1", mergedList?.get(3))
    }

    @Test
    fun testListWithDifferentTypes() {
        val fromMap = mapOf(
            "key" to listOf("abc", "def")
        )
        val toMap = mapOf(
            "key" to listOf(0, 1)
        )
        val result = EventDataMerger.merge(fromMap, toMap, true)
        assertEquals(1, result.keys.size)
        assertTrue(result.containsKey("key"))
        val mergedList = result["key"] as? List<Any?>
        assertEquals("abc", mergedList?.get(0))
        assertEquals("def", mergedList?.get(1))
        assertEquals(0, mergedList?.get(2))
        assertEquals(1, mergedList?.get(3))
    }

    @Test
    fun testWildCardSimpleMerge() {
        val toMap = mapOf(
            "list" to listOf(
                mapOf(
                    "k1" to "v1"
                ),
                mapOf(
                    "k2" to "v2"
                )
            )
        )
        val fromMap = mapOf(
            "list[*]" to mapOf(
                "newKey" to "newValue"
            )
        )
        val result = EventDataMerger.merge(fromMap, toMap, true)
        assertEquals(1, result.keys.size)
        assertTrue(result.containsKey("list"))
        val mergedList = result["list"] as? List<*>
        val item0 = mergedList?.get(0) as? Map<*, *>
        assertEquals("v1", item0?.get("k1"))
        assertEquals("newValue", item0?.get("newKey"))
        val item1 = mergedList?.get(1) as? Map<*, *>
        assertEquals("v2", item1?.get("k2"))
        assertEquals("newValue", item1?.get("newKey"))
    }

    @Test
    fun testWildCardMergeNotAtRootLevel() {
        val toMap = mapOf(
            "inner" to mapOf(
                "list" to listOf(
                    mapOf(
                        "k1" to "v1"
                    ),
                    mapOf(
                        "k2" to "v2"
                    )
                )
            )
        )
        val fromMap = mapOf(
            "inner" to mapOf(
                "list[*]" to mapOf(
                    "newKey" to "newValue"
                )
            )
        )
        val result = EventDataMerger.merge(fromMap, toMap, true)
        assertEquals(1, result.keys.size)
        assertTrue(result.containsKey("inner"))
        val innerMap = result["inner"] as? Map<*,*>
        assertTrue(innerMap?.containsKey("list") == true)
        val mergedList = innerMap?.get("list") as? List<*>
        val item0 = mergedList?.get(0) as? Map<*, *>
        assertEquals("v1", item0?.get("k1"))
        assertEquals("newValue", item0?.get("newKey"))
        val item1 = mergedList?.get(1) as? Map<*, *>
        assertEquals("v2", item1?.get("k2"))
        assertEquals("newValue", item1?.get("newKey"))
    }

    @Test
    fun testWildCardMergeWithoutTarget() {
        val toMap = mapOf(
            "list" to listOf(
                mapOf(
                    "k1" to "v1"
                ),
                mapOf(
                    "k2" to "v2"
                )
            )
        )
        val fromMap = mapOf(
            "lists[*]" to mapOf(
                "newKey" to "newValue"
            )
        )
        val result = EventDataMerger.merge(fromMap, toMap, true)
        assertEquals(1, result.keys.size)
        assertTrue(result.containsKey("list"))
        val mergedList = result["list"] as? List<*>
        val item0 = mergedList?.get(0) as? Map<*, *>
        assertEquals(1, item0?.size)
        assertEquals("v1", item0?.get("k1"))
        val item1 = mergedList?.get(1) as? Map<*, *>
        assertEquals(1, item1?.size)
        assertEquals("v2", item1?.get("k2"))
    }

    @Test
    fun testWildCardMergeOverwrite() {
        val toMap = mapOf(
            "list" to listOf(
                mapOf(
                    "k1" to "v1",
                    "key" to "oldValue"
                ),
                mapOf(
                    "k2" to "v2"
                )
            )
        )
        val fromMap = mapOf(
            "list[*]" to mapOf(
                "key" to "newValue"
            )
        )
        val result = EventDataMerger.merge(fromMap, toMap, true)
        assertEquals(1, result.keys.size)
        assertTrue(result.containsKey("list"))
        val mergedList = result["list"] as? List<*>
        val item0 = mergedList?.get(0) as? Map<*, *>
        assertEquals(2, item0?.size)
        assertEquals("v1", item0?.get("k1"))
        assertEquals("newValue", item0?.get("key"))
        val item1 = mergedList?.get(1) as? Map<*, *>
        assertEquals(2, item1?.size)
        assertEquals("v2", item1?.get("k2"))
        assertEquals("newValue", item1?.get("key"))
    }

    @Test
    fun testWildCardMergeOverwriteWithNoneMapItem() {
        val toMap = mapOf(
            "list" to listOf(
                mapOf(
                    "k1" to "v1",
                    "key" to "oldValue"
                ),
                "none_map_item"
            )
        )
        val fromMap = mapOf(
            "list[*]" to mapOf(
                "key" to "newValue"
            )
        )
        val result = EventDataMerger.merge(fromMap, toMap, true)
        assertEquals(1, result.keys.size)
        assertTrue(result.containsKey("list"))
        val mergedList = result["list"] as? List<*>
        val item0 = mergedList?.get(0) as? Map<*, *>
        assertEquals(2, item0?.size)
        assertEquals("v1", item0?.get("k1"))
        assertEquals("newValue", item0?.get("key"))
        assertEquals("none_map_item", mergedList?.get(1))
    }

    @Test
    fun testWildCardMergeNotOverwrite() {
        val toMap = mapOf(
            "list" to listOf(
                mapOf(
                    "k1" to "v1",
                    "key" to "oldValue"
                ),
                mapOf(
                    "k2" to "v2"
                )
            )
        )
        val fromMap = mapOf(
            "list[*]" to mapOf(
                "key" to "newValue"
            )
        )
        val result = EventDataMerger.merge(fromMap, toMap, false)
        assertEquals(1, result.keys.size)
        assertTrue(result.containsKey("list"))
        val mergedList = result["list"] as? List<*>
        val item0 = mergedList?.get(0) as? Map<*, *>
        assertEquals(2, item0?.size)
        assertEquals("v1", item0?.get("k1"))
        assertEquals("oldValue", item0?.get("key"))
        val item1 = mergedList?.get(1) as? Map<*, *>
        assertEquals(2, item1?.size)
        assertEquals("v2", item1?.get("k2"))
        assertEquals("newValue", item1?.get("key"))
    }

    @Test
    fun testWildCardNestedMapMergeOverwrite() {
        val toMap = mapOf(
            "list" to listOf(
                mapOf(
                    "k1" to "v1",
                    "inner" to mapOf(
                        "inner_k1" to "oldValue",
                        "key" to "oldValue"
                    )
                ),
                mapOf(
                    "k2" to "v2"
                )
            )
        )
        val fromMap = mapOf(
            "list[*]" to mapOf(
                "key" to "newValue",
                "inner" to mapOf(
                    "inner_k1" to "newValue",
                    "newKey" to "newValue"
                )
            )
        )
        val result = EventDataMerger.merge(fromMap, toMap, true)
        assertEquals(1, result.keys.size)
        assertTrue(result.containsKey("list"))
        val mergedList = result["list"] as? List<*>
        val item0 = mergedList?.get(0) as? Map<*, *>
        assertEquals(3, item0?.size)
        assertEquals("v1", item0?.get("k1"))
        assertEquals("newValue", item0?.get("key"))
        val item0innerMap = item0?.get("inner") as? Map<*, *>
        assertEquals("newValue", item0innerMap?.get("inner_k1"))
        assertEquals("oldValue", item0innerMap?.get("key"))
        assertEquals("newValue", item0innerMap?.get("newKey"))
    }

    @Test
    fun testWildCardNestedMapMergeNotOverwrite() {
        val toMap = mapOf(
            "list" to listOf(
                mapOf(
                    "k1" to "v1",
                    "inner" to mapOf(
                        "inner_k1" to "oldValue",
                        "key" to "oldValue"
                    )
                ),
                mapOf(
                    "k2" to "v2"
                )
            )
        )
        val fromMap = mapOf(
            "list[*]" to mapOf(
                "key" to "newValue",
                "inner" to mapOf(
                    "inner_k1" to "newValue",
                    "newKey" to "newValue"
                )
            )
        )
        val result = EventDataMerger.merge(fromMap, toMap, false)
        assertEquals(1, result.keys.size)
        assertTrue(result.containsKey("list"))
        val mergedList = result["list"] as? List<*>
        val item0 = mergedList?.get(0) as? Map<*, *>
        assertEquals(3, item0?.size)
        assertEquals("v1", item0?.get("k1"))
        assertEquals("newValue", item0?.get("key"))
        val item0innerMap = item0?.get("inner") as? Map<*, *>
        assertEquals("oldValue", item0innerMap?.get("inner_k1"))
        assertEquals("oldValue", item0innerMap?.get("key"))
        assertEquals("newValue", item0innerMap?.get("newKey"))
    }
}