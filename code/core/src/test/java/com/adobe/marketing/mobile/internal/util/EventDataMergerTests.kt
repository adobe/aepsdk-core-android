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

class EventDataMergerTests {

    @Test
    fun testSimpleMerge() {
        val fromMap = mapOf(
            "key" to "oldValue"
        )
        val toMap = mapOf(
            "newKey" to "newValue"
        )
        val expectedMap = mapOf(
            "key" to "oldValue",
            "newKey" to "newValue"
        )
        assertEquals(expectedMap, EventDataMerger.merge(fromMap, toMap, true))
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
        val expectedMap = mapOf(
            "key" to "oldValue",
            "donotdelete" to "value",
            "newKey" to "newValue"
        )
        assertEquals(expectedMap, EventDataMerger.merge(fromMap, toMap, false))
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
        val expectedMap = mapOf(
            "nested" to mapOf(
                "key" to "oldValue",
                "newKey" to "newValue"
            )
        )
        assertEquals(expectedMap, EventDataMerger.merge(fromMap, toMap, true))
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
        val expectedMap = mapOf(
            "nested" to mapOf(
                "key" to "oldValue",
                "newKey" to "newValue"
            )
        )
        assertEquals(expectedMap, EventDataMerger.merge(fromMap, toMap, true))
    }

    @Test
    fun testListSimpleMerge() {
        val fromMap = mapOf(
            "key" to listOf("abc", "def")
        )
        val toMap = mapOf(
            "key" to listOf("0", "1")
        )
        val expectedMap = mapOf(
            "key" to listOf("abc", "def", "0", "1")
        )
        assertEquals(expectedMap, EventDataMerger.merge(fromMap, toMap, true))
    }

    @Test
    fun testListWithDuplicatedItems() {
        val fromMap = mapOf(
            "key" to listOf("abc", "def")
        )
        val toMap = mapOf(
            "key" to listOf("abc", "1")
        )
        val expectedMap = mapOf(
            "key" to listOf("abc", "def", "abc", "1")
        )
        assertEquals(expectedMap, EventDataMerger.merge(fromMap, toMap, true))
    }

    @Test
    fun testListWithDifferentTypes() {
        val fromMap = mapOf(
            "key" to listOf("abc", "def")
        )
        val toMap = mapOf(
            "key" to listOf(0, 1)
        )
        val expectedMap = mapOf(
            "key" to listOf("abc", "def", 0, 1)
        )
        assertEquals(expectedMap, EventDataMerger.merge(fromMap, toMap, true))
    }

    @Test
    fun testWildCardMergeBasic() {
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
        val expectedMap = mapOf(
            "list" to listOf(
                mapOf(
                    "k1" to "v1",
                    "newKey" to "newValue"
                ),
                mapOf(
                    "k2" to "v2",
                    "newKey" to "newValue"
                )
            )
        )
        assertEquals(expectedMap, EventDataMerger.merge(fromMap, toMap, true))
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
        val expectedMap = mapOf(
            "inner" to mapOf(
                "list" to listOf(
                    mapOf(
                        "k1" to "v1",
                        "newKey" to "newValue"
                    ),
                    mapOf(
                        "k2" to "v2",
                        "newKey" to "newValue"
                    )
                )
            )
        )
        assertEquals(expectedMap, EventDataMerger.merge(fromMap, toMap, true))
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
        val expectedMap = mapOf(
            "list" to listOf(
                mapOf(
                    "k1" to "v1"
                ),
                mapOf(
                    "k2" to "v2"
                )
            )
        )
        assertEquals(expectedMap, EventDataMerger.merge(fromMap, toMap, true))
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
        val expectedMap = mapOf(
            "list" to listOf(
                mapOf(
                    "k1" to "v1",
                    "key" to "newValue"
                ),
                mapOf(
                    "k2" to "v2",
                    "key" to "newValue"
                )
            )
        )
        assertEquals(expectedMap, EventDataMerger.merge(fromMap, toMap, true))
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
        val expectedMap = mapOf(
            "list" to listOf(
                mapOf(
                    "k1" to "v1",
                    "key" to "newValue"
                ),
                "none_map_item"
            )
        )
        assertEquals(expectedMap, EventDataMerger.merge(fromMap, toMap, true))
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
        val expectedMap = mapOf(
            "list" to listOf(
                mapOf(
                    "k1" to "v1",
                    "key" to "oldValue"
                ),
                mapOf(
                    "k2" to "v2",
                    "key" to "newValue"
                )
            )
        )
        assertEquals(expectedMap, EventDataMerger.merge(fromMap, toMap, false))
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
        val expectedMap = mapOf(
            "list" to listOf(
                mapOf(
                    "k1" to "v1",
                    "inner" to mapOf(
                        "inner_k1" to "newValue",
                        "key" to "oldValue",
                        "newKey" to "newValue"
                    ),
                    "key" to "newValue"
                ),
                mapOf(
                    "k2" to "v2",
                    "key" to "newValue",
                    "inner" to mapOf(
                        "inner_k1" to "newValue",
                        "newKey" to "newValue"
                    )
                )
            )
        )
        assertEquals(expectedMap, EventDataMerger.merge(fromMap, toMap, true))
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
        val expectedMap = mapOf(
            "list" to listOf(
                mapOf(
                    "k1" to "v1",
                    "inner" to mapOf(
                        "inner_k1" to "oldValue",
                        "key" to "oldValue",
                        "newKey" to "newValue"
                    ),
                    "key" to "newValue"
                ),
                mapOf(
                    "k2" to "v2",
                    "key" to "newValue",
                    "inner" to mapOf(
                        "inner_k1" to "newValue",
                        "newKey" to "newValue"
                    )
                )
            )
        )
        assertEquals(expectedMap, EventDataMerger.merge(fromMap, toMap, false))
    }

    @Test
    fun testFromMapWithNonStringKey() {
        val toMap = mapOf(
            "k1" to "v1",
            "k2" to "v2",
            "nested" to mapOf(
                1 to "ValueForIntKey",
                "k1" to "v1"
            )
        )
        val fromMap = mapOf(
            "nested" to mapOf(
                "k1" to "v11",
                "k2" to "v2"
            )
        )
        val expectedMap = mapOf(
            "k1" to "v1",
            "k2" to "v2",
            "nested" to mapOf(
                1 to "ValueForIntKey",
                "k1" to "v1"
            )
        )
        assertEquals(expectedMap, EventDataMerger.merge(fromMap, toMap, true))
    }

    @Test
    fun testToMapWithNonStringKey() {
        val toMap = mapOf(
            "k1" to "v1",
            "k2" to "v2",
            "nested" to mapOf(
                "k1" to "v1",
                "k2" to "v2"
            )
        )
        val fromMap = mapOf(
            "nested" to mapOf(
                1 to "ValueForIntKey",
                "k1" to "v11"
            )
        )
        val expectedMap = mapOf(
            "k1" to "v1",
            "k2" to "v2",
            "nested" to mapOf(
                "k1" to "v1",
                "k2" to "v2"
            )
        )
        assertEquals(expectedMap, EventDataMerger.merge(fromMap, toMap, true))
    }
}
