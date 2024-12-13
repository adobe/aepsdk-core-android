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

import com.adobe.marketing.mobile.util.JSONAsserts.assertExactMatch
import com.adobe.marketing.mobile.util.JSONAsserts.assertTypeMatch
import com.adobe.marketing.mobile.util.NodeConfig.Scope.Subtree
import org.junit.Test
import kotlin.test.assertFailsWith

class PathOptionElementCountTests {
    @Test
    fun testElementCount_withArray_passes() {
        val actual = "[1, \"abc\", true, null]"

        assertExactMatch("[]", actual, ElementCount(4))
        assertTypeMatch("[]", actual, ElementCount(4))
    }

    @Test
    fun testElementCount_withNestedArray_passes() {
        val actual = "[1, [\"abc\", true, null]]"

        assertExactMatch("[]", actual, ElementCount(4, Subtree))
        assertTypeMatch("[]", actual, ElementCount(4, Subtree))
    }

    /**
     * Validates that when child nodes are present, a subtree ElementCount that overlaps with those children
     * does not propagate the element count requirement incorrectly.
     * Also validates that this is true regardless of the order the path options are supplied.
     */
    @Test
    fun testElementCount_withNestedArray_whenUnrelatedChildNodes_subtreeElementCountDoesNotPropagate_passes() {
        val actual = """
        [
            1,
            [
                "abc",
                true,
                null
            ]
        ]
        """

        assertExactMatch("[]", actual, AnyOrderMatch("[1]"), ElementCount(4, Subtree))
        assertExactMatch("[]", actual, ElementCount(4, Subtree), AnyOrderMatch("[1]"))
        assertTypeMatch("[]", actual, AnyOrderMatch("[1]"), ElementCount(4, Subtree))
        assertTypeMatch("[]", actual, ElementCount(4, Subtree), AnyOrderMatch("[1]"))
    }

    /**
     * Validates that when wildcard child nodes are present, a subtree ElementCount that overlaps with those children
     * does not propagate the element count requirement incorrectly.
     * Also validates that this is true regardless of the order the path options are supplied.
     */
    @Test
    fun testElementCount_withNestedArray_whenWildcardChildNodes_subtreeElementCountDoesNotPropagate_passes() {
        val actual = """
        [
            [
                1,
                2
            ],
            [
                1,
                2
            ]
        ]
        """

        // These test cases validate that the `ElementCount(4, Subtree)` requirement is not propagated
        // to the wildcard set at the top level of the JSON hierarchy. If propagated incorrectly,
        // for example, "key1" would also have a 4 element count assertion requirement.
        assertExactMatch("[]", actual, AnyOrderMatch("[*]"), ElementCount(4, Subtree))
        assertExactMatch("[]", actual, ElementCount(4, Subtree), AnyOrderMatch("[*]"))
        assertTypeMatch("[]", actual, AnyOrderMatch("[*]"), ElementCount(4, Subtree))
        assertTypeMatch("[]", actual, ElementCount(4, Subtree), AnyOrderMatch("[*]"))
    }

    /**
     * Validates that when both specific and wildcard child nodes are present, a subtree ElementCount
     * that overlaps with those children does not propagate the element count requirement incorrectly.
     * Also validates that this is true regardless of the order the path options are supplied.
     */
    @Test
    fun testElementCount_withNestedArray_whenAllChildNodes_subtreeElementCountDoesNotPropagate_passes() {
        val actual = """
        [
            [
                1,
                2
            ],
            [
                1,
                2
            ]
        ]
        """

        // These test cases validate that the `ElementCount(4, Subtree)` requirement is not propagated
        // to any of the child nodes. If propagated incorrectly, for example, "key1" would also have a
        // 4 element count assertion requirement.
        assertExactMatch("[]", actual, AnyOrderMatch("[*]"), AnyOrderMatch("[0]"), ElementCount(4, Subtree))
        assertExactMatch("[]", actual, AnyOrderMatch("[*]"), ElementCount(4, Subtree), AnyOrderMatch("[0]"))
        assertExactMatch("[]", actual, AnyOrderMatch("[0]"), AnyOrderMatch("[*]"), ElementCount(4, Subtree))
        assertExactMatch("[]", actual, AnyOrderMatch("[0]"), ElementCount(4, Subtree), AnyOrderMatch("[*]"))
        assertExactMatch("[]", actual, ElementCount(4, Subtree), AnyOrderMatch("[*]"), AnyOrderMatch("[0]"))
        assertExactMatch("[]", actual, ElementCount(4, Subtree), AnyOrderMatch("[0]"), AnyOrderMatch("[*]"))

        assertTypeMatch("[]", actual, AnyOrderMatch("[*]"), AnyOrderMatch("[0]"), ElementCount(4, Subtree))
        assertTypeMatch("[]", actual, AnyOrderMatch("[*]"), ElementCount(4, Subtree), AnyOrderMatch("[0]"))
        assertTypeMatch("[]", actual, AnyOrderMatch("[0]"), AnyOrderMatch("[*]"), ElementCount(4, Subtree))
        assertTypeMatch("[]", actual, AnyOrderMatch("[0]"), ElementCount(4, Subtree), AnyOrderMatch("[*]"))
        assertTypeMatch("[]", actual, ElementCount(4, Subtree), AnyOrderMatch("[*]"), AnyOrderMatch("[0]"))
        assertTypeMatch("[]", actual, ElementCount(4, Subtree), AnyOrderMatch("[0]"), AnyOrderMatch("[*]"))
    }

    @Test
    fun testElementCount_withNestedArray_whenSingleNodeScope_passes() {
        val actual = "[1, [\"abc\", true, null]]"

        assertExactMatch("[]", actual, ElementCount(1))
        assertTypeMatch("[]", actual, ElementCount(1))
    }

    @Test
    fun testElementCount_withNestedArray_whenSingleNodeScope_innerPath_passes() {
        val actual = "[1, [\"abc\", true, null]]"

        assertExactMatch("[]", actual, ElementCount(3, "[1]"))
        assertTypeMatch("[]", actual, ElementCount(3, "[1]"))
    }

    @Test
    fun testElementCount_withNestedArray_whenSimultaneousSingleNodeScope_passes() {
        val actual = "[1, [\"abc\", true, null]]"

        assertExactMatch("[]", actual, ElementCount(1), ElementCount(3, "[1]"))
        assertTypeMatch("[]", actual, ElementCount(1), ElementCount(3, "[1]"))
    }

    @Test
    fun testElementCount_withNestedArray_whenSimultaneousSingleNodeAndSubtreeScope_passes() {
        val actual = "[1, [\"abc\", true, null]]"

        assertExactMatch(
            "[]",
            actual,
            ElementCount(1),
            ElementCount(4, Subtree)
        )
        assertTypeMatch(
            "[]",
            actual,
            ElementCount(1),
            ElementCount(4, Subtree)
        )
    }

    @Test
    fun testElementCount_withArray_whenCountNotEqual_fails() {
        val actual = "[1, \"abc\", true, null]"

        assertFailsWith<AssertionError>("Validation should fail when path option is not satisfied") {
            assertExactMatch("[]", actual, ElementCount(5))
        }
        assertFailsWith<AssertionError>("Validation should fail when path option is not satisfied") {
            assertExactMatch("[]", actual, ElementCount(3))
        }
        assertFailsWith<AssertionError>("Validation should fail when path option is not satisfied") {
            assertTypeMatch("[]", actual, ElementCount(5))
        }
        assertFailsWith<AssertionError>("Validation should fail when path option is not satisfied") {
            assertTypeMatch("[]", actual, ElementCount(3))
        }
    }

    @Test
    fun testElementCount_withArray_whenSingleNodeDisabled_passes() {
        val actual = "[1, \"abc\", true, null]"

        assertExactMatch(
            "[]",
            actual,
            ElementCount(3, Subtree),
            ElementCount(null, false, "[0]")
        )
        assertTypeMatch(
            "[]",
            actual,
            ElementCount(3, Subtree),
            ElementCount(null, false, "[0]")
        )
    }

    @Test
    fun testElementCount_withNestedArray_whenMiddleCollectionDisablesElementCount_passes() {
        val actual = "[1, [\"abc\", [true, null]]]"

        assertExactMatch(
            "[]",
            actual,
            ElementCount(3, Subtree),
            ElementCount(null, false, "[1]")
        )
        assertTypeMatch(
            "[]",
            actual,
            ElementCount(3, Subtree),
            ElementCount(null, false, "[1]")
        )
    }

    @Test
    fun testElementCount_withNestedArray_whenNestedSandwichedSubtreeOverrides_passes() {
        val actual = "[1, [\"abc\", [true, null]]]"

        assertExactMatch(
            "[]",
            actual,
            ElementCount(null, false, Subtree),
            ElementCount(1, Subtree, "[1]"),
            ElementCount(null, false, Subtree, "[1][1]")
        )
        assertTypeMatch(
            "[]",
            actual,
            ElementCount(null, false, Subtree),
            ElementCount(1, Subtree, "[1]"),
            ElementCount(null, false, Subtree, "[1][1]")
        )
    }

    @Test
    fun testElementCount_withNestedArray_whenNestedSingleNodeOverrides_passes() {
        val actual = "[1, [\"abc\", [true, null]]]"

        assertExactMatch(
            "[]",
            actual,
            ElementCount(null, false, Subtree),
            ElementCount(1, "[1]")
        )
        assertTypeMatch(
            "[]",
            actual,
            ElementCount(null, false, Subtree),
            ElementCount(1, "[1]")
        )
    }

    @Test
    fun testElementCount_withNestedArray_whenNestedSubtreeOverrides_passes() {
        val actual = "[1, [\"abc\", [true, null]]]"

        assertExactMatch(
            "[]",
            actual,
            ElementCount(null, false, Subtree),
            ElementCount(3, Subtree, "[1]")
        )
        assertTypeMatch(
            "[]",
            actual,
            ElementCount(null, false, Subtree),
            ElementCount(3, Subtree, "[1]")
        )
    }

    /**
     * Counts are checked only at the collection level, so any ElementCount conditions placed on elements
     * directly are ignored.
     */
    @Test
    fun testElementCount_withArray_whenAppliedToElement_fails() {
        val actual = "[1]"

        assertFailsWith<AssertionError>("Validation should fail when invalid path option is set") {
            assertExactMatch("[]", actual, ElementCount(1, "[0]"))
        }
        assertFailsWith<AssertionError>("Validation should fail when invalid path option is set") {
            assertTypeMatch("[]", actual, ElementCount(1, "[0]"))
        }
    }

    @Test
    fun testElementCount_withArray_whenWildcard_passes() {
        val actual = "[[1],[1]]"

        assertExactMatch("[]", actual, ElementCount(1, "[*]"))
        assertTypeMatch("[]", actual, ElementCount(1, "[*]"))
    }

    @Test
    fun testElementCount_withDictionary_passes() {
        val actual = """
        {
            "key1": 1,
            "key2": "abc",
            "key3": true,
            "key4": null
        }
        """

        assertExactMatch("{}", actual, ElementCount(4))
        assertTypeMatch("{}", actual, ElementCount(4))
    }

    @Test
    fun testElementCount_withNestedDictionary_passes() {
        val actual = """
        {
            "key1": 1,
            "key2": {
                "key2_1": "abc",
                "key2_2": true,
                "key2_3": null
            }
        }
        """

        assertExactMatch("{}", actual, ElementCount(4, Subtree))
        assertTypeMatch("{}", actual, ElementCount(4, Subtree))
    }

    /**
     * Validates that when child nodes are present, a subtree ElementCount that overlaps with those children
     * does not propagate the element count requirement incorrectly.
     * Also validates that this is true regardless of the order the path options are supplied.
     */
    @Test
    fun testElementCount_withNestedDictionary_whenUnrelatedChildNodes_subtreeElementCountDoesNotPropagate_passes() {
        val actual = """
        {
            "key1": 1,
            "key2": {
                "key2_1": "abc",
                "key2_2": true,
                "key2_3": null
            }
        }
        """

        assertExactMatch("{}", actual, AnyOrderMatch("key1.key2"), ElementCount(4, Subtree))
        assertExactMatch("{}", actual, ElementCount(4, Subtree), AnyOrderMatch("key1.key2"))
        assertTypeMatch("{}", actual, AnyOrderMatch("key1.key2"), ElementCount(4, Subtree))
        assertTypeMatch("{}", actual, ElementCount(4, Subtree), AnyOrderMatch("key1.key2"))
    }

    /**
     * Validates that when wildcard child nodes are present, a subtree ElementCount that overlaps with those children
     * does not propagate the element count requirement incorrectly.
     * Also validates that this is true regardless of the order the path options are supplied.
     */
    @Test
    fun testElementCount_withNestedDictionary_whenWildcardChildNodes_subtreeElementCountDoesNotPropagate_passes() {
        val actual = """
        {
            "key1": {
                "key3_1": 1,
                "key3_2": 2
            },
            "key2": {
                "key4_1": 1,
                "key4_2": 2
            }
        }
        """

        // These test cases validate that the `ElementCount(4, Subtree)` requirement is not propagated
        // to the wildcard set at the top level of the JSON hierarchy. If propagated incorrectly,
        // for example, "key1" would also have a 4 element count assertion requirement.
        assertExactMatch("{}", actual, AnyOrderMatch("*"), ElementCount(4, Subtree))
        assertExactMatch("{}", actual, ElementCount(4, Subtree), AnyOrderMatch("*"))
        assertTypeMatch("{}", actual, AnyOrderMatch("*"), ElementCount(4, Subtree))
        assertTypeMatch("{}", actual, ElementCount(4, Subtree), AnyOrderMatch("*"))
    }

    /**
     * Validates that when both specific and wildcard child nodes are present, a subtree ElementCount
     * that overlaps with those children does not propagate the element count requirement incorrectly.
     * Also validates that this is true regardless of the order the path options are supplied.
     */
    @Test
    fun testElementCount_withNestedDictionary_whenAllChildNodes_subtreeElementCountDoesNotPropagate_passes() {
        val actual = """
        {
            "key1": {
                "key3_1": 1,
                "key3_2": 2
            },
            "key2": {
                "key4_1": 1,
                "key4_2": 2
            }
        }
        """

        // These test cases validate that the `ElementCount(4, Subtree)` requirement is not propagated
        // to any of the child nodes. If propagated incorrectly, for example, "key1" would also have a
        // 4 element count assertion requirement.
        assertExactMatch("{}", actual, AnyOrderMatch("*"), AnyOrderMatch("key1"), ElementCount(4, Subtree))
        assertExactMatch("{}", actual, AnyOrderMatch("*"), ElementCount(4, Subtree), AnyOrderMatch("key1"))
        assertExactMatch("{}", actual, AnyOrderMatch("key1"), AnyOrderMatch("*"), ElementCount(4, Subtree))
        assertExactMatch("{}", actual, AnyOrderMatch("key1"), ElementCount(4, Subtree), AnyOrderMatch("*"))
        assertExactMatch("{}", actual, ElementCount(4, Subtree), AnyOrderMatch("*"), AnyOrderMatch("key1"))
        assertExactMatch("{}", actual, ElementCount(4, Subtree), AnyOrderMatch("key1"), AnyOrderMatch("*"))

        assertTypeMatch("{}", actual, AnyOrderMatch("*"), AnyOrderMatch("key1"), ElementCount(4, Subtree))
        assertTypeMatch("{}", actual, AnyOrderMatch("*"), ElementCount(4, Subtree), AnyOrderMatch("key1"))
        assertTypeMatch("{}", actual, AnyOrderMatch("key1"), AnyOrderMatch("*"), ElementCount(4, Subtree))
        assertTypeMatch("{}", actual, AnyOrderMatch("key1"), ElementCount(4, Subtree), AnyOrderMatch("*"))
        assertTypeMatch("{}", actual, ElementCount(4, Subtree), AnyOrderMatch("*"), AnyOrderMatch("key1"))
        assertTypeMatch("{}", actual, ElementCount(4, Subtree), AnyOrderMatch("key1"), AnyOrderMatch("*"))
    }

    @Test
    fun testElementCount_withNestedDictionary_whenSingleNodeScope_passes() {
        val actual = """
        {
            "key1": 1,
            "key2": {
                "key2_1": "abc",
                "key2_2": true,
                "key2_3": null
            }
        }
        """

        assertExactMatch("{}", actual, ElementCount(1))
        assertTypeMatch("{}", actual, ElementCount(1))
    }

    @Test
    fun testElementCount_withNestedDictionary_whenSingleNodeScope_innerPath_passes() {
        val actual = """
        {
            "key1": 1,
            "key2": {
                "key2_1": "abc",
                "key2_2": true,
                "key2_3": null
            }
        }
        """

        assertExactMatch("{}", actual, ElementCount(3, "key2"))
        assertTypeMatch("{}", actual, ElementCount(3, "key2"))
    }

    @Test
    fun testElementCount_withNestedDictionary_whenSimultaneousSingleNodeScope_passes() {
        val actual = """
        {
            "key1": 1,
            "key2": {
                "key2_1": "abc",
                "key2_2": true,
                "key2_3": null
            }
        }
        """

        assertExactMatch("{}", actual, ElementCount(1), ElementCount(3, "key2"))
        assertTypeMatch("{}", actual, ElementCount(1), ElementCount(3, "key2"))
    }

    @Test
    fun testElementCount_withNestedDictionary_whenSimultaneousSingleNodeAndSubtreeScope_passes() {
        val actual = """
        {
            "key1": 1,
            "key2": {
                "key2_1": "abc",
                "key2_2": true,
                "key2_3": null
            }
        }
        """

        assertExactMatch(
            "{}",
            actual,
            ElementCount(1),
            ElementCount(4, Subtree)
        )
        assertTypeMatch(
            "{}",
            actual,
            ElementCount(1),
            ElementCount(4, Subtree)
        )
    }

    @Test
    fun testElementCount_withDictionary_whenCountNotEqual_fails() {
        val actual = """
        {
            "key1": 1,
            "key2": "abc",
            "key3": true,
            "key4": null
        }
        """

        assertFailsWith<AssertionError>("Validation should fail when path option is not satisfied") {
            assertExactMatch("{}", actual, ElementCount(5))
        }
        assertFailsWith<AssertionError>("Validation should fail when path option is not satisfied") {
            assertExactMatch("{}", actual, ElementCount(3))
        }
        assertFailsWith<AssertionError>("Validation should fail when path option is not satisfied") {
            assertTypeMatch("{}", actual, ElementCount(5))
        }
        assertFailsWith<AssertionError>("Validation should fail when path option is not satisfied") {
            assertTypeMatch("{}", actual, ElementCount(3))
        }
    }

    @Test
    fun testElementCount_withDictionary_whenSingleNodeDisabled_passes() {
        val actual = """
        {
            "key1": 1,
            "key2": "abc",
            "key3": true,
            "key4": null
        }
        """

        assertExactMatch(
            "{}",
            actual,
            ElementCount(3, Subtree),
            ElementCount(null, false, "key1")
        )
        assertTypeMatch(
            "{}",
            actual,
            ElementCount(3, Subtree),
            ElementCount(null, false, "key1")
        )
    }

    @Test
    fun testElementCount_withNestedDictionary_whenMiddleCollectionDisablesElementCount_passes() {
        val actual = """
        {
            "key1": 1,
            "key2": {
                "key2_1": "abc",
                "key2_2": {
                    "key3_1": true,
                    "key3_2": null
                }
            }
        }
        """

        assertExactMatch(
            "{}",
            actual,
            ElementCount(3, Subtree),
            ElementCount(null, false, "key2")
        )
        assertTypeMatch(
            "{}",
            actual,
            ElementCount(3, Subtree),
            ElementCount(null, false, "key2")
        )
    }

    @Test
    fun testElementCount_withNestedDictionary_whenNestedSandwichedSubtreeOverrides_passes() {
        val actual = """
        {
            "key1": 1,
            "key2": {
                "key2_1": "abc",
                "key2_2": {
                    "key3_1": true,
                    "key3_2": null
                }
            }
        }
        """

        assertExactMatch(
            "{}",
            actual,
            ElementCount(null, false, Subtree),
            ElementCount(1, Subtree, "key2"),
            ElementCount(null, false, Subtree, "key2.key2_2")
        )
        assertTypeMatch(
            "{}",
            actual,
            ElementCount(null, false, Subtree),
            ElementCount(1, Subtree, "key2"),
            ElementCount(null, false, Subtree, "key2.key2_2")
        )
    }

    @Test
    fun testElementCount_withNestedDictionary_whenNestedSingleNodeOverrides_passes() {
        val actual = """
        {
            "key1": 1,
            "key2": {
                "key2_1": "abc",
                "key2_2": {
                    "key3_1": true,
                    "key3_2": null
                }
            }
        }
        """

        assertExactMatch(
            "{}",
            actual,
            ElementCount(null, false, Subtree),
            ElementCount(1, "key2"),
        )
        assertTypeMatch(
            "{}",
            actual,
            ElementCount(null, false, Subtree),
            ElementCount(1, "key2"),
        )
    }

    @Test
    fun testElementCount_withNestedDictionary_whenNestedSubtreeOverrides_passes() {
        val actual = """
        {
            "key1": 1,
            "key2": {
                "key2_1": "abc",
                "key2_2": {
                    "key3_1": true,
                    "key3_2": null
                }
            }
        }
        """

        assertExactMatch(
            "{}",
            actual,
            ElementCount(null, false, Subtree),
            ElementCount(3, Subtree, "key2"),
        )
        assertTypeMatch(
            "{}",
            actual,
            ElementCount(null, false, Subtree),
            ElementCount(3, Subtree, "key2"),
        )
    }

    /**
     * Counts are checked only at the collection level, so any ElementCount conditions placed on elements
     * directly are ignored.
     */
    @Test
    fun testElementCount_withDictionary_whenAppliedToElement_fails() {
        val actual = """{ "key1": 1 }"""

        assertFailsWith<AssertionError>("Validation should fail when invalid path option is set") {
            assertExactMatch("{}", actual, ElementCount(1, "key1"))
        }
        assertFailsWith<AssertionError>("Validation should fail when invalid path option is set") {
            assertTypeMatch("{}", actual, ElementCount(1, "key1"))
        }
    }
}
