/*
  Copyright 2023 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.util

import androidx.annotation.VisibleForTesting
import com.adobe.marketing.mobile.util.NodeConfig.OptionKey.AnyOrderMatch
import com.adobe.marketing.mobile.util.NodeConfig.OptionKey.ElementCount
import com.adobe.marketing.mobile.util.NodeConfig.OptionKey.KeyMustBeAbsent
import com.adobe.marketing.mobile.util.NodeConfig.OptionKey.PrimitiveExactMatch
import com.adobe.marketing.mobile.util.NodeConfig.Scope.SingleNode
import com.adobe.marketing.mobile.util.NodeConfig.Scope.Subtree
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail

object JSONAsserts {
    data class ValidationResult(val isValid: Boolean, val elementCount: Int)

    /**
     * Asserts exact equality between two [JSONObject] or [JSONArray] instances.
     *
     * @param expected The expected [JSONObject] or [JSONArray] to compare.
     * @param actual The actual [JSONObject] or [JSONArray] to compare.
     *
     * @throws AssertionError If the [expected] and [actual] JSON structures are not exactly equal.
     */
    @JvmStatic
    fun assertEquals(expected: Any?, actual: Any?) {
        if (expected == null && actual == null) {
            return
        }
        if (expected == null || actual == null) {
            fail(
                """
                ${if (expected == null) "Expected is null" else "Actual is null"} and 
                ${if (expected == null) "Actual" else "Expected"} is non-null.
        
                Expected: $expected
        
                Actual: $actual
                """.trimIndent()
            )
            return
        }
        // Exact equality is just a special case of exact match
        assertExactMatch(expected, actual, CollectionEqualCount(true, NodeConfig.Scope.Subtree))
    }

    /**
     * Performs JSON validation where only the values from the `expected` JSON are required by default.
     * By default, the comparison logic uses the value type match option, only validating that both values are of the same type.
     *
     * Both objects and arrays use extensible collections by default, meaning that only the elements in `expected` are
     * validated.
     *
     * Path options allow for powerful customizations to the comparison logic; see structs conforming to [MultiPathConfig]:
     * - [AnyOrderMatch]
     * - [CollectionEqualCount]
     * - [KeyMustBeAbsent]
     * - [ValueExactMatch], [ValueTypeMatch]
     *
     * For example, given an expected JSON like:
     * ```
     * {
     *   "key1": "value1",
     *   "key2": [{ "nest1": 1}, {"nest2": 2}],
     *   "key3": { "key4": 1 },
     *   "key.name": 1,
     *   "key[123]": 1
     * }
     * ```
     * An example path for this JSON would be: `"key2[1].nest2"`.
     *
     * Paths must begin from the top level of the expected JSON. Multiple paths and path options can be used at the same time.
     * Path options are applied sequentially. If an option overrides an existing one, the overriding will occur in the order in which
     * the path options are specified.
     *
     * Formats for object keys:
     * - Standard keys - The key name itself: `"key1"`
     * - Nested keys - Use dot notation: `"key3.key4"`.
     * - Keys with dots in the name: Escape the dot notation with a backslash: `"key\.name"`.
     *
     * Formats for arrays:
     * - Standard index - The index integer inside square brackets: `[<INT>]` (e.g., `[0]`, `[28]`).
     * - Keys with array brackets in the name - Escape the brackets with backslashes: `key\[123\]`.
     *
     * Formats for wildcard object key and array index names:
     * - Array wildcard - All children elements of the array: `[*]` (ex: `key1[*].key3`)
     * - Object wildcard - All children elements of the object: `*` (ex: `key1.*.key3`)
     * - Key whose name is asterisk - Escape the asterisk with backslash: `"\*"`
     * - Note that wildcard path options also apply to any existing specific nodes at the same level.
     *
     * - Parameters:
     *   - expected: The expected JSON ([JSONObject], [JSONArray], or types supported by [getJSONRepresentation]) to compare.
     *   - actual: The actual JSON ([JSONObject], [JSONArray], or types supported by [getJSONRepresentation]) to compare.
     *   - pathOptions: The path options to use in the validation process.
     */
    @JvmStatic
    fun assertTypeMatch(expected: Any, actual: Any?, pathOptions: List<MultiPathConfig>) {
        val treeDefaults = listOf(
            AnyOrderMatch(isActive = false),
            CollectionEqualCount(isActive = false),
            // ElementCount subtree default is set to `true` so that all elements are counted by default
            ElementCount(null, isActive = true),
            KeyMustBeAbsent(isActive = false),
            ValueTypeMatch(),
            ValueNotEqual(isActive = false)
        )
        validate(expected, actual, pathOptions.toList(), treeDefaults)
    }

    /**
     * Performs JSON validation where only the values from the `expected` JSON are required by default.
     * By default, the comparison logic uses the value type match option, only validating that both values are of the same type.
     *
     * Both objects and arrays use extensible collections by default, meaning that only the elements in `expected` are
     * validated.
     *
     * Path options allow for powerful customizations to the comparison logic; see structs conforming to [MultiPathConfig]:
     * - [AnyOrderMatch]
     * - [CollectionEqualCount]
     * - [KeyMustBeAbsent]
     * - [ValueExactMatch], [ValueTypeMatch]
     *
     * For example, given an expected JSON like:
     * ```
     * {
     *   "key1": "value1",
     *   "key2": [{ "nest1": 1}, {"nest2": 2}],
     *   "key3": { "key4": 1 },
     *   "key.name": 1,
     *   "key[123]": 1
     * }
     * ```
     * An example path for this JSON would be: `"key2[1].nest2"`.
     *
     * Paths must begin from the top level of the expected JSON. Multiple paths and path options can be used at the same time.
     * Path options are applied sequentially. If an option overrides an existing one, the overriding will occur in the order in which
     * the path options are specified.
     *
     * Formats for object keys:
     * - Standard keys - The key name itself: `"key1"`
     * - Nested keys - Use dot notation: `"key3.key4"`.
     * - Keys with dots in the name: Escape the dot notation with a backslash: `"key\.name"`.
     *
     * Formats for arrays:
     * - Standard index - The index integer inside square brackets: `[<INT>]` (e.g., `[0]`, `[28]`).
     * - Keys with array brackets in the name - Escape the brackets with backslashes: `key\[123\]`.
     *
     * Formats for wildcard object key and array index names:
     * - Array wildcard - All children elements of the array: `[*]` (ex: `key1[*].key3`)
     * - Object wildcard - All children elements of the object: `*` (ex: `key1.*.key3`)
     * - Key whose name is asterisk - Escape the asterisk with backslash: `"\*"`
     * - Note that wildcard path options also apply to any existing specific nodes at the same level.
     *
     * - Parameters:
     *   - expected: The expected JSON ([JSONObject], [JSONArray], or types supported by [getJSONRepresentation]) to compare.
     *   - actual: The actual JSON ([JSONObject], [JSONArray], or types supported by [getJSONRepresentation]) to compare.
     *   - pathOptions: The path options to use in the validation process.
     */
    @JvmStatic
    fun assertTypeMatch(expected: Any, actual: Any?, vararg pathOptions: MultiPathConfig) {
        assertTypeMatch(expected, actual, pathOptions.toList())
    }

    /**
     * Performs JSON validation where only the values from the `expected` JSON are required by default.
     * By default, the comparison logic uses the value exact match option, validating that both values are of the same type
     * **and** have the same literal value.
     *
     * Both objects and arrays use extensible collections by default, meaning that only the elements in `expected` are
     * validated.
     *
     * Path options allow for powerful customizations to the comparison logic; see structs conforming to [MultiPathConfig]:
     * - [AnyOrderMatch]
     * - [CollectionEqualCount]
     * - [KeyMustBeAbsent]
     * - [ValueExactMatch], [ValueTypeMatch]
     *
     * For example, given an expected JSON like:
     * ```
     * {
     *   "key1": "value1",
     *   "key2": [{ "nest1": 1}, {"nest2": 2}],
     *   "key3": { "key4": 1 },
     *   "key.name": 1,
     *   "key[123]": 1
     * }
     * ```
     * An example path for this JSON would be: `"key2[1].nest2"`.
     *
     * Paths must begin from the top level of the expected JSON. Multiple paths and path options can be used at the same time.
     * Path options are applied sequentially. If an option overrides an existing one, the overriding will occur in the order in which
     * the path options are specified.
     *
     * Formats for object keys:
     * - Standard keys - The key name itself: `"key1"`
     * - Nested keys - Use dot notation: `"key3.key4"`.
     * - Keys with dots in the name: Escape the dot notation with a backslash: `"key\.name"`.
     *
     * Formats for arrays:
     * - Standard index - The index integer inside square brackets: `[<INT>]` (e.g., `[0]`, `[28]`).
     * - Keys with array brackets in the name - Escape the brackets with backslashes: `key\[123\]`.
     *
     * Formats for wildcard object key and array index names:
     * - Array wildcard - All children elements of the array: `[*]` (ex: `key1[*].key3`)
     * - Object wildcard - All children elements of the object: `*` (ex: `key1.*.key3`)
     * - Key whose name is asterisk - Escape the asterisk with backslash: `"\*"`
     * - Note that wildcard path options also apply to any existing specific nodes at the same level.
     *
     * - Parameters:
     *   - expected: The expected JSON ([JSONObject], [JSONArray], or types supported by [getJSONRepresentation]) to compare.
     *   - actual: The actual JSON ([JSONObject], [JSONArray], or types supported by [getJSONRepresentation]) to compare.
     *   - pathOptions: The path options to use in the validation process.
     */
    @JvmStatic
    fun assertExactMatch(expected: Any, actual: Any?, pathOptions: List<MultiPathConfig>) {
        val treeDefaults = listOf(
            AnyOrderMatch(isActive = false),
            CollectionEqualCount(isActive = false),
            // ElementCount subtree default is set to `true` so that all elements are counted by default
            ElementCount(null, isActive = true),
            KeyMustBeAbsent(isActive = false),
            ValueExactMatch(),
            ValueNotEqual(isActive = false)
        )
        validate(expected, actual, pathOptions.toList(), treeDefaults)
    }

    /**
     * Performs JSON validation where only the values from the `expected` JSON are required by default.
     * By default, the comparison logic uses the value exact match option, validating that both values are of the same type
     * **and** have the same literal value.
     *
     * Both objects and arrays use extensible collections by default, meaning that only the elements in `expected` are
     * validated.
     *
     * Path options allow for powerful customizations to the comparison logic; see structs conforming to [MultiPathConfig]:
     * - [AnyOrderMatch]
     * - [CollectionEqualCount]
     * - [KeyMustBeAbsent]
     * - [ValueExactMatch], [ValueTypeMatch]
     *
     * For example, given an expected JSON like:
     * ```
     * {
     *   "key1": "value1",
     *   "key2": [{ "nest1": 1}, {"nest2": 2}],
     *   "key3": { "key4": 1 },
     *   "key.name": 1,
     *   "key[123]": 1
     * }
     * ```
     * An example path for this JSON would be: `"key2[1].nest2"`.
     *
     * Paths must begin from the top level of the expected JSON. Multiple paths and path options can be used at the same time.
     * Path options are applied sequentially. If an option overrides an existing one, the overriding will occur in the order in which
     * the path options are specified.
     *
     * Formats for object keys:
     * - Standard keys - The key name itself: `"key1"`
     * - Nested keys - Use dot notation: `"key3.key4"`.
     * - Keys with dots in the name: Escape the dot notation with a backslash: `"key\.name"`.
     *
     * Formats for arrays:
     * - Standard index - The index integer inside square brackets: `[<INT>]` (e.g., `[0]`, `[28]`).
     * - Keys with array brackets in the name - Escape the brackets with backslashes: `key\[123\]`.
     *
     * Formats for wildcard object key and array index names:
     * - Array wildcard - All children elements of the array: `[*]` (ex: `key1[*].key3`)
     * - Object wildcard - All children elements of the object: `*` (ex: `key1.*.key3`)
     * - Key whose name is asterisk - Escape the asterisk with backslash: `"\*"`
     * - Note that wildcard path options also apply to any existing specific nodes at the same level.
     *
     * - Parameters:
     *   - expected: The expected JSON ([JSONObject], [JSONArray], or types supported by [getJSONRepresentation]) to compare.
     *   - actual: The actual JSON ([JSONObject], [JSONArray], or types supported by [getJSONRepresentation]) to compare.
     *   - pathOptions: The path options to use in the validation process.
     */
    @JvmStatic
    fun assertExactMatch(expected: Any, actual: Any?, vararg pathOptions: MultiPathConfig) {
        assertExactMatch(expected, actual, pathOptions.toList())
    }

    private fun validate(
        expected: Any,
        actual: Any?,
        pathOptions: List<MultiPathConfig>,
        treeDefaults: List<MultiPathConfig>
    ) {
        try {
            val nodeTree = generateNodeTree(pathOptions, treeDefaults)

            val expectedJSON = getJSONRepresentation(expected)
            if (expectedJSON == null) {
                fail("Failed to convert expected to valid JSON representation.")
            }
            val actualJSON = getJSONRepresentation(actual)

            validateActual(actualJSON, nodeTree = nodeTree)
            validateJSON(expectedJSON, actualJSON, nodeTree = nodeTree)
        } catch (e: java.lang.IllegalArgumentException) {
            fail("Invalid JSON provided: ${e.message}")
        }
    }

    /**
     * Performs a customizable validation between the given `expected` and `actual` values, using the configured options.
     * In case of a validation failure **and** if `shouldAssert` is `true`, a test failure occurs.
     *
     * @param expected The expected value to compare.
     * @param actual The actual value to compare.
     * @param keyPath A list of keys or array indexes representing the path to the current value being compared. Defaults to an empty list.
     * @param nodeTree A tree of configuration objects used to control various validation settings.
     * @param shouldAssert Indicates if an assertion error should be thrown if `expected` and `actual` are not equal. Defaults to `true`.
     * @return `true` if `expected` and `actual` are equal based on the settings in `nodeTree`, otherwise returns `false`.
     */
    private fun validateJSON(
        expected: Any?,
        actual: Any?,
        keyPath: List<Any> = emptyList(),
        nodeTree: NodeConfig,
        shouldAssert: Boolean = true
    ): Boolean {
        if (expected == null || expected == JSONObject.NULL) {
            return true
        }
        if (actual == null || actual == JSONObject.NULL) {
            if (shouldAssert) {
                fail(
                    """
                    Expected JSON is non-null but Actual JSON is null.
                    Expected: $expected
                    Actual: $actual
                    Key path: ${keyPathAsString(keyPath)}
                    """.trimIndent()
                )
            }
            return false
        }

        return when {
            expected is String && actual is String ||
                expected is Boolean && actual is Boolean ||
                expected is Int && actual is Int ||
                expected is Double && actual is Double -> {
                if (nodeTree.valueNotEqual.isActive) {
                    if (shouldAssert) assertNotEquals(
                        """
                        Expected and Actual values should not be equal.
                        Expected: $expected (Type: ${expected::class.qualifiedName})
                        Actual: $actual (Type: ${actual::class.qualifiedName})
                        Key path: ${keyPathAsString(keyPath)}
                        """.trimIndent(),
                        expected,
                        actual
                    )
                    expected != actual
                } else if (nodeTree.primitiveExactMatch.isActive) {
                    if (shouldAssert) assertEquals(
                        "Key path: ${keyPathAsString(keyPath)}",
                        expected,
                        actual
                    )
                    expected == actual
                } else {
                    true
                }
            }
            expected is JSONObject && actual is JSONObject -> validateJSON(
                expected as? JSONObject,
                actual as? JSONObject,
                keyPath,
                nodeTree,
                shouldAssert
            )
            expected is JSONArray && actual is JSONArray -> validateJSON(
                expected as? JSONArray,
                actual as? JSONArray,
                keyPath,
                nodeTree,
                shouldAssert
            )
            else -> {
                if (shouldAssert) {
                    fail(
                        """
                        Expected and Actual types are not the same.
                        Expected: $expected (Type: ${expected::class.qualifiedName})
                        Actual: $actual (Type: ${actual::class.qualifiedName})
                        Key path: ${keyPathAsString(keyPath)}
                        """.trimIndent()
                    )
                }
                false
            }
        }
    }

    /**
     * Performs a customizable validation between the given `expected` and `actual` arrays, using the configured options.
     * In case of a validation failure **and** if `shouldAssert` is `true`, a test failure occurs.
     *
     * @param expected The expected array to compare.
     * @param actual The actual array to compare.
     * @param keyPath A list of keys or array indexes representing the path to the current value being compared.
     * @param nodeTree A tree of configuration objects used to control various validation settings.
     * @param shouldAssert Indicates if an assertion error should be thrown if `expected` and `actual` are not equal.
     * @return `true` if `expected` and `actual` are equal based on the settings in `nodeTree`, otherwise returns `false`.
     */
    private fun validateJSON(
        expected: JSONArray?,
        actual: JSONArray?,
        keyPath: List<Any>,
        nodeTree: NodeConfig,
        shouldAssert: Boolean = true
    ): Boolean {
        if (expected == null) {
            return true
        }
        if (actual == null) {
            if (shouldAssert) {
                fail(
                    """
                    Expected JSON is non-null but Actual JSON is null.
        
                    Expected: $expected
                    Actual: $actual
        
                    Key path: ${keyPathAsString(keyPath)}
                    """.trimIndent()
                )
            }
            return false
        }

        if (nodeTree.collectionEqualCount.isActive) {
            if (expected.length() != actual.length()) {
                if (shouldAssert) {
                    fail(
                        """
                        Expected JSON count does not match Actual JSON.
    
                        Expected count: ${expected.length()}
                        Actual count: ${actual.length()}
    
                        Expected: $expected
                        Actual: $actual
    
                        Key path: ${keyPathAsString(keyPath)}
                        """.trimIndent()
                    )
                }
                return false
            }
        } else if (expected.length() > actual.length()) {
            if (shouldAssert) {
                fail(
                    """
                    Expected JSON has more elements than Actual JSON.
    
                    Expected count: ${expected.length()}
                    Actual count: ${actual.length()}
    
                    Expected: $expected
                    Actual: $actual
    
                    Key path: ${keyPathAsString(keyPath)}
                    """.trimIndent()
                )
            }
            return false
        }

        val expectedIndexes = (0 until expected.length()).associate { index ->
            index.toString() to NodeConfig.resolveOption(AnyOrderMatch, index, nodeTree)
        }.toMutableMap()
        val anyOrderIndexes = expectedIndexes.filter { it.value.isActive }

        for (key in anyOrderIndexes.keys) {
            expectedIndexes.remove(key)
        }

        val availableWildcardActualIndexes = mutableSetOf<String>().apply {
            addAll((0 until actual.length()).map { it.toString() })
            removeAll(expectedIndexes.keys)
        }

        var validationResult = true

        for ((index, config) in expectedIndexes) {
            val intIndex = index.toInt()
            validationResult = validateJSON(
                expected[intIndex],
                actual.opt(intIndex),
                keyPath + intIndex,
                nodeTree.getNextNode(index),
                shouldAssert
            ) && validationResult
        }

        for ((index, config) in anyOrderIndexes) {
            val intIndex = index.toInt()

            val actualIndex = availableWildcardActualIndexes.firstOrNull {
                validateJSON(
                    expected[intIndex],
                    actual.opt(it.toInt()),
                    keyPath + intIndex,
                    nodeTree.getNextNode(index),
                    shouldAssert = false
                )
            }
            if (actualIndex == null) {
                if (shouldAssert) {
                    fail(
                        """
                        Wildcard ${if (NodeConfig.resolveOption(PrimitiveExactMatch, index, nodeTree).isActive) "exact" else "type"}
                        match found no matches on Actual side satisfying the Expected requirement.
                
                        Requirement: $nodeTree
                
                        Expected: ${expected[intIndex]}
                        Actual (remaining unmatched elements): ${availableWildcardActualIndexes.map { actual[it.toInt()] }}
                
                        Key path: ${keyPathAsString(keyPath)}
                        """.trimIndent()
                    )
                }
                validationResult = false
                break
            } else {
                availableWildcardActualIndexes.remove(actualIndex)
            }
        }
        return validationResult
    }

    /**
     * Performs a customizable validation between the given `expected` and `actual` dictionaries, using the configured options.
     * In case of a validation failure **and** if `shouldAssert` is `true`, a test failure occurs.
     *
     * @param expected The expected dictionary to compare.
     * @param actual The actual dictionary to compare.
     * @param keyPath A list of keys or array indexes representing the path to the current value being compared.
     * @param nodeTree A tree of configuration objects used to control various validation settings.
     * @param shouldAssert Indicates if an assertion error should be thrown if `expected` and `actual` are not equal.
     * @return `true` if `expected` and `actual` are equal based on the settings in `nodeTree`, otherwise returns `false`.
     */
    private fun validateJSON(
        expected: JSONObject?,
        actual: JSONObject?,
        keyPath: List<Any>,
        nodeTree: NodeConfig,
        shouldAssert: Boolean = true
    ): Boolean {
        if (expected == null) {
            return true
        }
        if (actual == null) {
            if (shouldAssert) {
                fail(
                    """
                    Expected JSON is non-null but Actual JSON is null.
        
                    Expected: $expected
                    Actual: $actual
        
                    Key path: ${keyPathAsString(keyPath)}
                    """.trimIndent()
                )
            }
            return false
        }

        if (nodeTree.collectionEqualCount.isActive) {
            if (expected.length() != actual.length()) {
                if (shouldAssert) {
                    fail(
                        """
                    Expected JSON count does not match Actual JSON.

                    Expected count: ${expected.length()}
                    Actual count: ${actual.length()}

                    Expected: $expected
                    Actual: $actual

                    Key path: ${keyPathAsString(keyPath)}
                        """.trimIndent()
                    )
                }
                return false
            }
        } else if (expected.length() > actual.length()) {
            if (shouldAssert) {
                fail(
                    """
                Expected JSON has more elements than Actual JSON.

                Expected count: ${expected.length()}
                Actual count: ${actual.length()}

                Expected: $expected
                Actual: $actual

                Key path: ${keyPathAsString(keyPath)}
                    """.trimIndent()
                )
            }
            return false
        }

        var validationResult = true

        for (key in expected.keys()) {
            validationResult = validateJSON(
                expected.get(key),
                actual.opt(key),
                keyPath + key,
                nodeTree.getNextNode(key),
                shouldAssert
            ) && validationResult
        }
        return validationResult
    }

    /**
     * Validates the provided `actual` value against a specified `nodeTree` configuration.
     *
     * This method traverses a `NodeConfig` tree to validate the `actual` value according to the specified node configuration.
     * It handles different types of values including dictionaries and arrays, and applies the relevant validation rules
     * based on the configuration of each node in the tree.
     *
     * Note that this logic is meant to perform negative validation (for example, the absence of keys), and this means when `actual` nodes run out
     * validation automatically passes. Positive validation should use `expected` + `validateJSON`.
     *
     * @param actual The value to be validated, either [JSONObject] or [JSONArray].
     * @param keyPath An array representing the current traversal path in the node tree. Starts as an empty array.
     * @param nodeTree The root of the `NodeConfig` tree against which the validation is performed.
     * @return A `Boolean` indicating whether the `actual` value is valid based on the `nodeTree` configuration.
     */
    private fun validateActual(
        actual: Any?,
        keyPath: List<Any> = listOf(),
        nodeTree: NodeConfig
    ): ValidationResult {
        return when (actual) {
            // Handle dictionaries
            is JSONObject -> validateActual(
                actual = actual,
                keyPath = keyPath,
                nodeTree = nodeTree
            )
            // Handle arrays
            is JSONArray -> validateActual(
                actual = actual,
                keyPath = keyPath,
                nodeTree = nodeTree
            )
            else -> {
                // Check SingleNode and Subtree options set for ElementCount for this specific node.
                // If it hits this case, then the ElementCount assertion was set on a non-collection type element
                // and should emit a test failure.
                if (nodeTree.getSingleNodeOption(ElementCount)?.elementCount != null || nodeTree.getSubtreeNodeOption(ElementCount)?.elementCount != null) {
                    fail(
                        """
                        Invalid ElementCount assertion on a non-collection element.
                        Remove ElementCount requirements from this key path in the test setup.
            
                        Key path: ${keyPathAsString(keyPath)}
                        """.trimIndent()
                    )
                    ValidationResult(false, 1)
                }
                ValidationResult(true, 1)
            }
        }
    }

    /**
     * Validates a [JSONArray]'s values against the provided node configuration tree.
     *
     * This method iterates through each element in the given [JSONArray] and performs validation
     * based on the provided [NodeConfig].
     *
     * @param actual The [JSONArray] to be validated.
     * @param keyPath An array representing the current path in the node tree during the traversal.
     * @param nodeTree The current node in the [NodeConfig] tree against which the [actual] values are validated.
     * @return A [Boolean] indicating whether all elements in the [actual] array are valid according to the node tree configuration.
     */
    private fun validateActual(
        actual: JSONArray?,
        keyPath: List<Any>,
        nodeTree: NodeConfig
    ): ValidationResult {
        var validationResult = true
        var singleNodeElementCount = 0
        var subtreeElementCount = 0

        if (actual != null) {
            for (index in 0 until actual.length()) {
                // ElementCount check
                val currentElement = actual.opt(index)
                val result = validateActual(currentElement, keyPath + index, nodeTree.getNextNode(index))
                validationResult = result.isValid && validationResult
                when (currentElement) {
                    is JSONObject, is JSONArray -> {
                        subtreeElementCount += result.elementCount
                    }
                    else -> {
                        if (NodeConfig.resolveOption(ElementCount, index, nodeTree).isActive) {
                            singleNodeElementCount += result.elementCount
                        }
                    }
                }
            }
        }

        var totalElementCount = singleNodeElementCount + subtreeElementCount
        validationResult = validateElementCount(SingleNode, keyPath, nodeTree, singleNodeElementCount) && validationResult
        validationResult = validateElementCount(Subtree, keyPath, nodeTree, totalElementCount) && validationResult

        return ValidationResult(validationResult, totalElementCount)
    }

    /**
     * Validates a dictionary of [JSONObject] values against the provided node configuration tree.
     *
     * This method iterates through each key-value pair in the given dictionary and performs validation
     * based on the provided `NodeConfig`.
     *
     * @param actual The dictionary of [JSONObject] values to be validated.
     * @param keyPath An array representing the current path in the node tree during the traversal.
     * @param nodeTree The current node in the `NodeConfig` tree against which the `actual` values are validated.
     * @return A `Boolean` indicating whether all key-value pairs in the `actual` dictionary are valid according to the node tree configuration.
     */
    private fun validateActual(
        actual: JSONObject?,
        keyPath: List<Any>,
        nodeTree: NodeConfig
    ): ValidationResult {
        var validationResult = true
        var singleNodeElementCount = 0
        var subtreeElementCount = 0

        if (actual != null) {
            for (key in actual.keys()) {
                // KeyMustBeAbsent check
                // Check for keys that must be absent in the current node
                NodeConfig.resolveOption(KeyMustBeAbsent, key, nodeTree)
                    .takeIf { it.isActive }
                    ?.let {
                        fail(
                            """
                            Actual JSON must not have key with name: $key
                
                            Actual: $actual
                
                            Key path: ${keyPathAsString(keyPath + listOf(key))}
                            """.trimIndent()
                        )
                        validationResult = false
                    }

                // ElementCount check
                val currentElement = actual.get(key)
                val result = validateActual(currentElement, keyPath + key, nodeTree.getNextNode(key))
                validationResult = result.isValid && validationResult
                when (currentElement) {
                    is JSONObject, is JSONArray -> {
                        subtreeElementCount += result.elementCount
                    }
                    else -> {
                        if (NodeConfig.resolveOption(ElementCount, key, nodeTree).isActive) {
                            singleNodeElementCount += result.elementCount
                        }
                    }
                }
            }
        }

        var totalElementCount = singleNodeElementCount + subtreeElementCount
        validationResult = validateElementCount(SingleNode, keyPath, nodeTree, singleNodeElementCount) && validationResult
        validationResult = validateElementCount(Subtree, keyPath, nodeTree, totalElementCount) && validationResult

        return ValidationResult(validationResult, totalElementCount)
    }

    /**
     * Validates that the element count for a given scope is satisfied within the node configuration.
     * Used to validate [ElementCount] assertions.
     *
     * @param scope The scope of the element count check, either SingleNode or Subtree.
     * @param keyPath The current path in the JSON hierarchy traversal. Used for test failure message info.
     * @param node The current node in the NodeConfig tree against which the element count is validated.
     * @param elementCount The actual count of elements to be validated.
     * @return `true` if the element count is satisfied, `false` otherwise.
     */
    private fun validateElementCount(scope: NodeConfig.Scope, keyPath: List<Any>, node: NodeConfig, elementCount: Int): Boolean {
        var validationResult = true
        val config = when (scope) {
            SingleNode -> node.getSingleNodeOption(ElementCount)
            Subtree -> node.getSubtreeNodeOption(ElementCount)
        }
        // Check if the element count is satisfied
        config?.takeIf { it.isActive && it.elementCount != null }?.also {
            validationResult = it.elementCount == elementCount
            assertTrue(
                """
                The ${if (scope == NodeConfig.Scope.Subtree) "subtree" else "single node"} expected element count 
                is not equal to the actual number of elements.
    
                Expected count: ${it.elementCount}
                Actual count: $elementCount
    
                Key path: ${keyPathAsString(keyPath)}
                """.trimIndent(),
                validationResult
            )
        }
        return validationResult
    }

    /**
     * Generates a tree structure from an array of path `String`s.
     *
     * This function processes each path in `paths`, extracts its individual components using `processPathComponents`, and
     * constructs a nested dictionary structure. The constructed dictionary is then merged into the main tree. If the resulting tree
     * is empty after processing all paths, this function returns `null`.
     *
     * @param pathOptions An array of path `String`s to be processed. Each path represents a nested structure to be transformed
     * into a tree-like dictionary.
     * @param treeDefaults Defaults used for tree configuration.
     * @return A tree-like dictionary structure representing the nested structure of the provided paths. Returns `null` if the
     * resulting tree is empty.
     */
    private fun generateNodeTree(
        pathOptions: List<MultiPathConfig>,
        treeDefaults: List<MultiPathConfig>
    ): NodeConfig {
        // Create the first node using the incoming defaults
        val subtreeOptions: MutableMap<NodeConfig.OptionKey, NodeConfig.Config> = mutableMapOf()
        for (treeDefault in treeDefaults) {
            val key = treeDefault.optionKey
            subtreeOptions[key] = treeDefault.config
        }
        val rootNode = NodeConfig(name = null, subtreeOptions = subtreeOptions)

        for (pathConfig in pathOptions) {
            rootNode.createOrUpdateNode(pathConfig)
        }

        return rootNode
    }

    /**
     * Converts a key path represented by a list of JSON object keys and array indexes into a human-readable string format.
     *
     * The key path is used to trace the recursive traversal of a nested JSON structure.
     * For instance, the key path for the value "Hello" in the JSON `{ "a": { "b": [ "World", "Hello" ] } }`
     * would be ["a", "b", 1].
     * This method would convert it to the string "a.b[1]".
     *
     * Special considerations:
     * 1. If a key in the JSON object contains a dot (.), it will be escaped with a backslash in the resulting string.
     * 2. Empty keys in the JSON object will be represented as "" in the resulting string.
     *
     * @param keyPath A list of keys or array indexes representing the path to a value in a nested JSON structure.
     *
     * @return A human-readable string representation of the key path.
     */
    private fun keyPathAsString(keyPath: List<Any>): String {
        var result = ""
        for (item in keyPath) {
            when (item) {
                is String -> {
                    if (result.isNotEmpty()) {
                        result += "."
                    }
                    result += when {
                        item.contains(".") -> item.replace(".", "\\.")
                        item.isEmpty() -> "\"\""
                        else -> item
                    }
                }
                is Int -> result += "[$item]"
            }
        }
        return result
    }

    /**
     * Converts the given object to its JSON representation.
     *
     * This method handles various types of input including null, strings representing JSON objects or arrays,
     * maps, lists, and arrays. Null values within maps, lists, and arrays are replaced with `JSONObject.NULL`.
     * All other inputs throw an exception.
     *
     * @param obj the object to be converted to JSON representation. Can be null, a JSON string, JSONObject, JSONArray,
     * a map, a list, or an array.
     * @return the JSON representation of the given object. Can be a `JSONObject`, `JSONArray`, or null if null was the input.
     * @throws IllegalArgumentException if the input string is an invalid JSON string or if the input type is unsupported.
     */
    @VisibleForTesting
    fun getJSONRepresentation(obj: Any?): Any? {
        return when (obj) {
            null -> null
            is String -> {
                try {
                    JSONObject(obj) // Attempt to parse as JSONObject first.
                } catch (e: JSONException) {
                    try {
                        JSONArray(obj) // Attempt to parse as JSONArray if JSONObject fails.
                    } catch (e: JSONException) {
                        throw IllegalArgumentException("Failed to convert to JSON representation: Invalid JSON string '$obj'")
                    }
                }
            }
            is JSONObject, is JSONArray, is Map<*, *>, is List<*>, is Array<*> -> recursiveJSONRepresentation(obj)
            else -> throw IllegalArgumentException("Failed to convert to JSON representation: $obj, with reason: Unsupported type ${obj.javaClass.kotlin}")
        }
    }

    /**
     * Recursively converts the given object to its JSON representation.
     *
     * This method handles nested structures like maps, lists, and arrays, replacing null values with `JSONObject.NULL`.
     * Basic types such as strings, numbers, and booleans are returned as-is.
     *
     * @param obj the object to be converted to JSON representation. Can be null, a `JSONObject`, a `JSONArray`, a map,
     *            a list, an array, or a basic type (string, number, boolean).
     * @return the JSON representation of the given object. Can be a `JSONObject`, `JSONArray`, or a basic type.
     * @throws IllegalArgumentException if the input map contains non-string keys or if the input type is unsupported.
     */
    private fun recursiveJSONRepresentation(obj: Any?): Any? {
        return when (obj) {
            null -> JSONObject.NULL
            is JSONObject, is JSONArray -> obj
            is Map<*, *> -> {
                try {
                    // Validate all strings are keys before trying to convert to JSON
                    if (obj.keys.all { it is String }) {
                        // Create a new map where null values are replaced with JSONObject.NULL
                        // and other values are recursively processed
                        val updatedMap = obj.mapValues { entry ->
                            if (entry.value == null) JSONObject.NULL else recursiveJSONRepresentation(entry.value)
                        }
                        JSONObject(updatedMap)
                    } else {
                        throw IllegalArgumentException("Failed to convert to JSON representation: Invalid JSON dictionary keys. Keys must be strings. Found: ${obj.keys}")
                    }
                } catch (e: Exception) {
                    throw IllegalArgumentException("Failed to create JSONObject: $obj, with reason: ${e.message}")
                }
            }
            is List<*> -> try {
                // Recursively process each element in the list
                JSONArray(obj.map { recursiveJSONRepresentation(it) })
            } catch (e: Exception) {
                throw IllegalArgumentException("Failed to create JSONArray from List: $obj, with reason: ${e.message}")
            }
            is Array<*> -> try {
                // Convert array to list and recursively process each element
                JSONArray(obj.map { recursiveJSONRepresentation(it) })
            } catch (e: Exception) {
                throw IllegalArgumentException("Failed to create JSONArray from Array: $obj, with reason: ${e.message}")
            }
            is String, is Number, is Boolean -> obj
            else -> throw IllegalArgumentException("Failed to convert to JSON representation: $obj, with reason: Unsupported type ${obj.javaClass.kotlin}")
        }
    }
    // endregion
}
