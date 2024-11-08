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

import com.adobe.marketing.mobile.util.JSONAsserts.assertEquals
import com.adobe.marketing.mobile.util.JSONAsserts.assertExactMatch
import com.adobe.marketing.mobile.util.JSONAsserts.assertTypeMatch
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Suite
import kotlin.test.assertFailsWith

interface TestParams {
    val expected: Any
    val actual: Any
}

data class PlainParams(override val expected: Any, override val actual: Any) : TestParams

data class JSONArrayWrappedParams(private val expectedVal: Any, private val actualVal: Any?) : TestParams {
    override val expected: JSONArray = JSONArray().put(expectedVal)
    override val actual: JSONArray = JSONArray().put(actualVal)
}

data class JSONObjectWrappedParams(val keyName: String, private val expectedVal: Any, private val actualVal: Any?) : TestParams {
    override val expected: JSONObject = JSONObject().put(keyName, expectedVal)
    override val actual: JSONObject = JSONObject().put(keyName, actualVal)
}

@RunWith(Suite::class)
@Suite.SuiteClasses(
    JSONAssertsParameterizedTests.ValueValidationTests::class,
    JSONAssertsParameterizedTests.TypeValidationTests::class,
    JSONAssertsParameterizedTests.ExtensibleCollectionValidationTests::class,
    JSONAssertsParameterizedTests.TypeValidationFailureTests::class,
    JSONAssertsParameterizedTests.SpecialKeyTest::class,
    JSONAssertsParameterizedTests.AlternatePathValueDictionaryTest::class,
    JSONAssertsParameterizedTests.AlternatePathValueArrayTest::class,
    JSONAssertsParameterizedTests.AlternatePathTypeDictionaryTest::class,
    JSONAssertsParameterizedTests.AlternatePathTypeArrayTest::class,
    JSONAssertsParameterizedTests.SpecialKeyAlternatePathTest::class,
    JSONAssertsParameterizedTests.ExpectedArrayLargerTest::class,
    JSONAssertsParameterizedTests.ExpectedDictionaryLargerTest::class
)
class JSONAssertsParameterizedTests {
    companion object {
        fun createPlainParams(vararg pairs: Pair<Any, Any>): List<TestParams> {
            return pairs.map { (expected, actual) -> PlainParams(expected, actual) }
        }

        fun createJSONArrayWrappedParams(vararg pairs: Pair<Any, Any?>): List<TestParams> {
            return pairs.map { (expected, actual) -> JSONArrayWrappedParams(expected, actual) }
        }

        fun createJSONObjectWrappedParams(keyName: String, vararg pairs: Pair<Any, Any?>): List<TestParams> {
            return pairs.map { (expected, actual) -> JSONObjectWrappedParams(keyName, expected, actual) }
        }
    }

    /**
     * Validates that [JSONArray]s and [JSONObject]s compare correctly, including nested structures.
     */
    @RunWith(Parameterized::class)
    class ValueValidationTests(private val params: TestParams) {
        companion object {
            @JvmStatic
            @Parameterized.Parameters(name = "{index}: test with expected={0}, actual={1}")
            fun data(): Collection<Any> {
                return createPlainParams(
                    JSONArray() to JSONArray(), // Empty array
                    JSONArray("[[[]]]") to JSONArray("[[[]]]"), // Nested arrays
                    JSONObject() to JSONObject(), // Empty dictionary
                    JSONObject("""{ "key1": 1 }""") to JSONObject("""{ "key1": 1 }"""), // Simple key value pair
                    JSONObject("""{ "key1": { "key2": {} } }""") to JSONObject("""{ "key1": { "key2": {} } }""") // Nested objects
                )
            }
        }

        @Test
        fun `should match basic values`() {
            assertEquals(params.expected, params.actual)
            assertExactMatch(params.expected, params.actual)
            assertTypeMatch(params.expected, params.actual)
        }
    }

    /**
     * Validates that various value types inside a [JSONArray] compare correctly, including nested structures.
     */
    @RunWith(Parameterized::class)
    class ValueValidationArrayTests(private val params: TestParams) {
        companion object {
            @JvmStatic
            @Parameterized.Parameters(name = "{index}: test with expected={0}, actual={1}")
            fun data(): Collection<Any> {
                return createJSONArrayWrappedParams(
                    5 to 5,
                    5.0 to 5.0,
                    true to true,
                    "a" to "a",
                    "안녕하세요" to "안녕하세요",
                    JSONObject.NULL to JSONObject.NULL,
                    JSONArray() to JSONArray(),
                    JSONArray("[[[]]]") to JSONArray("[[[]]]"),
                    JSONObject() to JSONObject(),
                    JSONObject("""{ "key1": 1 }""") to JSONObject("""{ "key1": 1 }"""),
                    JSONObject("""{ "key1": { "key2": {} } }""") to JSONObject("""{ "key1": { "key2": {} } }""")
                )
            }
        }

        @Test
        fun `should match basic values`() {
            assertEquals(params.expected, params.actual)
            assertExactMatch(params.expected, params.actual)
            assertTypeMatch(params.expected, params.actual)
        }
    }

    /**
     * Validates that various value types inside a [JSONObject] compare correctly, including nested structures.
     */
    @RunWith(Parameterized::class)
    class ValueValidationDictionaryTests(private val params: TestParams) {
        companion object {
            @JvmStatic
            @Parameterized.Parameters(name = "{index}: test with expected={0}, actual={1}")
            fun data(): Collection<Any> {
                return createJSONObjectWrappedParams(
                    "key",
                    5 to 5,
                    5.0 to 5.0,
                    true to true,
                    "a" to "a",
                    "안녕하세요" to "안녕하세요",
                    JSONObject.NULL to JSONObject.NULL,
                    JSONArray() to JSONArray(),
                    JSONArray("[[[]]]") to JSONArray("[[[]]]"),
                    JSONObject() to JSONObject(),
                    JSONObject("""{ "key1": 1 }""") to JSONObject("""{ "key1": 1 }"""),
                    JSONObject("""{ "key1": { "key2": {} } }""") to JSONObject("""{ "key1": { "key2": {} } }""")
                )
            }
        }

        @Test
        fun `should match basic values`() {
            assertEquals(params.expected, params.actual)
            assertExactMatch(params.expected, params.actual)
            assertTypeMatch(params.expected, params.actual)
        }
    }

    @RunWith(Parameterized::class)
    class TypeValidationTests(private val params: TestParams) {
        companion object {
            @JvmStatic
            @Parameterized.Parameters(name = "{index}: test with expected={0}, actual={1}")
            fun data(): Collection<Any> {
                return createJSONArrayWrappedParams(
                    5 to 10, // Int
                    5.0 to 10.0, // Double
                    true to false, // Boolean
                    "a" to "b", // String
                    "안" to "안녕하세요", // Non-Latin String
                    JSONObject("""{ "key1": 1 }""") to JSONObject("""{ "key1": 2 }"""), // Key value pair
                    JSONObject("""{ "key1": { "key2": "a" } }""") to JSONObject("""{ "key1": { "key2": "b", "key3": 3 } }""") // Nested partial by type
                )
            }
        }

        @Test
        fun `should match only by type for values of the same type`() {
            Assert.assertThrows(AssertionError::class.java) {
                assertEquals(params.expected, params.actual)
            }
            Assert.assertThrows(AssertionError::class.java) {
                assertExactMatch(params.expected, params.actual)
            }
            assertTypeMatch(params.expected, params.actual)
        }
    }

    @RunWith(Parameterized::class)
    class ExtensibleCollectionValidationTests(private val params: TestParams) {
        companion object {
            @JvmStatic
            @Parameterized.Parameters(name = "{index}: test with expected={0}, actual={1}")
            fun data(): Collection<Any> {
                return createPlainParams(
                    JSONArray() to JSONArray(listOf(1)),
                    JSONArray(listOf(1, 2, 3)) to JSONArray(listOf(1, 2, 3, 4)),
                    JSONObject() to JSONObject(mapOf("k" to "v")),
                    JSONObject(mapOf("key1" to 1, "key2" to "a", "key3" to 1.0, "key4" to true)) to
                        JSONObject(mapOf("key1" to 1, "key2" to "a", "key3" to 1.0, "key4" to true, "key5" to "extra"))
                )
            }
        }

        @Test
        fun `should pass flexible matching when expected is a subset`() {
            assertFailsWith<AssertionError>("Validation should fail when collection sizes are different.") {
                assertEquals(params.expected, params.actual)
            }
            assertExactMatch(params.expected, params.actual)
            assertTypeMatch(params.expected, params.actual)
        }
    }

    @RunWith(Parameterized::class)
    class TypeValidationFailureTests(private val params: TestParams) {
        companion object {
            @JvmStatic
            @Parameterized.Parameters(name = "{index}: test with expected={0}, actual={1}")
            fun data(): Collection<Any> {
                return createJSONArrayWrappedParams(
                    1 to 2.0,
                    1 to "a",
                    1 to true,
                    1 to JSONObject(),
                    1 to JSONArray(),
                    1 to JSONObject.NULL,
                    1 to null,
                    2.0 to "a",
                    2.0 to true,
                    2.0 to JSONObject(),
                    2.0 to JSONArray(),
                    2.0 to JSONObject.NULL,
                    2.0 to null,
                    "a" to true,
                    "a" to JSONObject(),
                    "a" to JSONArray(),
                    "a" to JSONObject.NULL,
                    "a" to null,
                    true to JSONObject(),
                    true to JSONArray(),
                    true to JSONObject.NULL,
                    true to null,
                    JSONObject() to JSONArray(),
                    JSONObject() to JSONObject.NULL,
                    JSONObject() to null,
                    JSONArray() to JSONObject.NULL,
                    JSONArray() to null,
                    JSONObject("""{ "key1": 1 }""") to JSONObject("""{ "key2": 1 }""")
                )
            }
        }

        @Test
        fun `should detect type mismatch or nullability issues`() {
            assertFailsWith<AssertionError>("Validation should fail when value types mismatch.") {
                assertEquals(params.expected, params.actual)
            }
            assertFailsWith<AssertionError>("Validation should fail when value types mismatch.") {
                assertTypeMatch(params.expected, params.actual)
            }
            assertFailsWith<AssertionError>("Validation should fail when value types mismatch.") {
                assertExactMatch(params.expected, params.actual)
            }
        }
    }

    @RunWith(Parameterized::class)
    class SpecialKeyTest(private val params: TestParams) {
        companion object {
            @JvmStatic
            @Parameterized.Parameters(name = "{index}: test with expected={0}, actual={1}")
            fun data(): Collection<Any> {
                return createPlainParams(
                    JSONObject("""{ "": 1 }""") to JSONObject("""{ "": 1 }"""),
                    JSONObject("""{ "\\": 1 }""") to JSONObject("""{ "\\": 1 }"""),
                    JSONObject("""{ "\\\\": 1 }""") to JSONObject("""{ "\\\\": 1 }"""),
                    JSONObject("""{ ".": 1 }""") to JSONObject("""{ ".": 1 }"""),
                    JSONObject("""{ "k.1.2.3": 1 }""") to JSONObject("""{ "k.1.2.3": 1 }"""),
                    JSONObject("""{ "k.": 1 }""") to JSONObject("""{ "k.": 1 }"""),
                    JSONObject("""{ "\"": 1 }""") to JSONObject("""{ "\"": 1 }"""),
                    JSONObject("""{ "'": 1 }""") to JSONObject("""{ "'": 1 }"""),
                    JSONObject("""{ "\'": 1 }""") to JSONObject("""{ "\'": 1 }"""),
                    JSONObject("""{ "key with space": 1 }""") to JSONObject("""{ "key with space": 1 }"""),
                    JSONObject("""{ "\n": 1 }""") to JSONObject("""{ "\n": 1 }"""),
                    JSONObject("""{ "key \t \n newline": 1 }""") to JSONObject("""{ "key \t \n newline": 1 }"""),
                    JSONObject("""{ "안녕하세요": 1 }""") to JSONObject("""{ "안녕하세요": 1 }""")
                )
            }
        }

        @Test
        fun `should handle special characters in JSON keys correctly`() {
            assertEquals(params.expected, params.actual)
            assertExactMatch(params.expected, params.actual)
            assertTypeMatch(params.expected, params.actual)
        }
    }

    @RunWith(Parameterized::class)
    class AlternatePathValueDictionaryTest(private val params: TestParams) {
        companion object {
            @JvmStatic
            @Parameterized.Parameters(name = "{index}: test with key={0}, expected={1}, actual={2}")
            fun data(): Collection<Any> {
                return createPlainParams(
                    JSONObject("""{ "key1": 1 }""") to JSONObject("""{ "key1": 1 }"""),
                    JSONObject("""{ "key1": 2.0 }""") to JSONObject("""{ "key1": 2.0 }"""),
                    JSONObject("""{ "key1": "a" }""") to JSONObject("""{ "key1": "a" }"""),
                    JSONObject("""{ "key1": true }""") to JSONObject("""{ "key1": true }"""),
                    JSONObject("""{ "key1": {} }""") to JSONObject("""{ "key1": {} }"""),
                    JSONObject("""{ "key1": [] }""") to JSONObject("""{ "key1": [] }"""),
                    JSONObject("""{ "key1": null }""") to JSONObject("""{ "key1": null }""")
                )
            }
        }

        @Test
        fun `should not fail because of alternate path`() {
            assertExactMatch(params.expected, params.actual, ValueTypeMatch("key1"))
            assertTypeMatch(params.expected, params.actual, ValueExactMatch("key1"))
        }
    }

    @RunWith(Parameterized::class)
    class AlternatePathValueArrayTest(private val params: TestParams) {
        companion object {
            @JvmStatic
            @Parameterized.Parameters(name = "{index}: test with key={0}, expected={1}, actual={2}")
            fun data(): Collection<Any> {
                return createPlainParams(
                    JSONArray("[1]") to JSONArray("[1]"),
                    JSONArray("[2.0]") to JSONArray("[2.0]"),
                    JSONArray("[\"a\"]") to JSONArray("[\"a\"]"),
                    JSONArray("[true]") to JSONArray("[true]"),
                    JSONArray("[{}]") to JSONArray("[{}]"),
                    JSONArray("[[]]") to JSONArray("[[]]"),
                    JSONArray("[null]") to JSONArray("[null]")
                )
            }
        }

        @Test
        fun `should not fail because of alternate path`() {
            assertExactMatch(params.expected, params.actual, ValueTypeMatch("[0]"))
            assertTypeMatch(params.expected, params.actual, ValueExactMatch("[0]"))

            assertExactMatch(params.expected, params.actual, ValueTypeMatch("[*]"))
            assertTypeMatch(params.expected, params.actual, ValueExactMatch("[*]"))
        }
    }

    @RunWith(Parameterized::class)
    class AlternatePathTypeDictionaryTest(private val params: TestParams) {
        companion object {
            @JvmStatic
            @Parameterized.Parameters(name = "{index}: test with key={0}, expected={1}, actual={2}")
            fun data(): Collection<Any> {
                return createJSONObjectWrappedParams(
                    "key",
                    1 to 2,
                    "a" to "b",
                    1.0 to 2.0,
                    true to false
                )
            }
        }

        @Test
        fun `should apply alternate path to matching logic`() {
            assertExactMatch(params.expected, params.actual, ValueTypeMatch("key"))
            assertFailsWith<AssertionError>("Validation should fail when path option is not satisfied.") {
                assertTypeMatch(params.expected, params.actual, ValueExactMatch("key"))
            }
        }
    }

    @RunWith(Parameterized::class)
    class AlternatePathTypeArrayTest(private val params: TestParams) {
        companion object {
            @JvmStatic
            @Parameterized.Parameters(name = "{index}: test with key={0}, expected={1}, actual={2}")
            fun data(): Collection<Any> {
                return createJSONArrayWrappedParams(
                    1 to 2,
                    "a" to "b",
                    1.0 to 2.0,
                    true to false
                )
            }
        }

        @Test
        fun `should apply alternate path to matching logic`() {
            assertExactMatch(params.expected, params.actual, ValueTypeMatch("[0]"))
            assertFailsWith<AssertionError>("Validation should fail when mismatched types are not equivalent under alternate paths.") {
                assertTypeMatch(params.expected, params.actual, ValueExactMatch("[0]"))
            }
        }
    }

    @RunWith(Parameterized::class)
    class SpecialKeyAlternatePathTest(private val keyPath: String, private val params: TestParams) {
        companion object {
            @JvmStatic
            @Parameterized.Parameters(name = "{index}: test with key={0}, expected={1}, actual={2}")
            fun data(): Collection<Any> {
                return listOf(
                    "key1." to (JSONObject("""{ "key1": { "": 1 } }""") to JSONObject("""{ "key1": { "": 2 } }""")),
                    "key1..key3" to (JSONObject("""{ "key1": { "": { "key3": 1 } } }""") to JSONObject("""{ "key1": { "": { "key3": 2 } } }""")),
                    ".key2." to (JSONObject("""{ "": { "key2": { "": 1 } } }""") to JSONObject("""{ "": { "key2": { "": 2 } } }""")),
                    "\\\\." to (JSONObject("""{ "\\.": 1 }""") to JSONObject("""{ "\\.": 2 }""")),
                    "" to (JSONObject("""{ "": 1 }""") to JSONObject("""{ "": 2 }""")),
                    "." to (JSONObject("""{ "": { "": 1 } }""") to JSONObject("""{ "": { "": 2 } }""")),
                    "..." to (JSONObject("""{ "": { "": { "": { "": 1 } } } }""") to JSONObject("""{ "": { "": { "": { "": 2 } } } }""")),
                    "\\" to (JSONObject("""{ "\\": 1 }""") to JSONObject("""{ "\\": 2 }""")),
                    "\\\\" to (JSONObject("""{ "\\\\": 1 }""") to JSONObject("""{ "\\\\": 2 }""")),
                    "\\." to (JSONObject("""{ ".": 1 }""") to JSONObject("""{ ".": 2 }""")),
                    "k\\.1\\.2\\.3" to (JSONObject("""{ "k.1.2.3": 1 }""") to JSONObject("""{ "k.1.2.3": 2 }""")),
                    "k\\." to (JSONObject("""{ "k.": 1 }""") to JSONObject("""{ "k.": 2 }""")),
                    "\"" to (JSONObject("""{ "\"": 1 }""") to JSONObject("""{ "\"": 2 }""")),
                    "\'" to (JSONObject("""{ "\'": 1 }""") to JSONObject("""{ "\'": 2 }""")),
                    "'" to (JSONObject("""{ "'": 1 }""") to JSONObject("""{ "'": 2 }""")),
                    "key with space" to (JSONObject("""{ "key with space": 1 }""") to JSONObject("""{ "key with space": 2 }""")),
                    "\n" to (JSONObject("""{ "\n": 1 }""") to JSONObject("""{ "\n": 2 }""")),
                    "key \t \n newline" to (JSONObject("""{ "key \t \n newline": 1 }""") to JSONObject("""{ "key \t \n newline": 2 }""")),
                    "안녕하세요" to (JSONObject("""{ "안녕하세요": 1 }""") to JSONObject("""{ "안녕하세요": 2 }""")),
                    "a]" to (JSONObject("""{ "a]": 1 }""") to JSONObject("""{ "a]": 2 }""")),
                    "a[" to (JSONObject("""{ "a[": 1 }""") to JSONObject("""{ "a[": 2 }""")),
                    "a[1]b" to (JSONObject("""{ "a[1]b": 1 }""") to JSONObject("""{ "a[1]b": 2 }""")),
                    "key1\\[0\\]" to (JSONObject("""{ "key1[0]": 1 }""") to JSONObject("""{ "key1[0]": 2 }""")),
                    "\\[1\\][0]" to (JSONObject("""{ "[1]": [1] }""") to JSONObject("""{ "[1]": [2] }""")),
                    "\\[1\\\\][0]" to (JSONObject("""{ "[1\\]": [1] }""") to JSONObject("""{ "[1\\]": [2] }"""))
                ).map { (keyPath, pair) ->
                    arrayOf(keyPath, PlainParams(pair.first, pair.second))
                }
            }
        }

        @Test
        fun `should handle special keys in alternate paths`() {
            assertExactMatch(params.expected, params.actual, ValueTypeMatch(keyPath))
            assertFailsWith<AssertionError>("Validation should fail when special key paths result in type mismatches.") {
                assertTypeMatch(params.expected, params.actual, ValueExactMatch(keyPath))
            }
        }
    }

    @RunWith(Parameterized::class)
    class ExpectedArrayLargerTest(private val keyPaths: List<String>, private val params: TestParams) {
        companion object {
            @JvmStatic
            @Parameterized.Parameters(name = "{index}: test with alternateMatchPaths={0}")
            fun data(): Collection<Any> {
                return listOf(
                    listOf<String>() to (JSONArray("[1, 2]") to JSONArray("[1]")),
                    listOf("[0]") to (JSONArray("[1, 2]") to JSONArray("[1]")),
                    listOf("[1]") to (JSONArray("[1, 2]") to JSONArray("[1]")),
                    listOf("[0]", "[1]") to (JSONArray("[1, 2]") to JSONArray("[1]")),
                    listOf("[*]") to (JSONArray("[1, 2]") to JSONArray("[1]"))
                ).map { (keyPaths, pair) ->
                    arrayOf(keyPaths, PlainParams(pair.first, pair.second))
                }
            }
        }

        /**
         * Validates that a larger expected array compared to actual will throw errors
         * even when using alternate match paths.
         *
         * Consequence: Guarantees that array size validation isn't affected by alternate paths.
         */
        @Test
        fun `should error on larger expected arrays`() {
            assertFailsWith<AssertionError>("Validation should fail when expected array is larger regardless of alternate paths.") {
                assertEquals(params.expected, params.actual)
            }
            assertFailsWith<AssertionError>("Validation should fail when exact matching is enforced with larger expected arrays.") {
                assertExactMatch(params.expected, params.actual, ValueTypeMatch(keyPaths))
            }
            assertFailsWith<AssertionError>("Validation should fail on type matching with larger expected arrays.") {
                assertTypeMatch(params.expected, params.actual, ValueExactMatch(keyPaths))
            }
        }
    }

    @RunWith(Parameterized::class)
    class ExpectedDictionaryLargerTest(private val keyPaths: List<String>, private val params: TestParams) {
        companion object {
            @JvmStatic
            @Parameterized.Parameters(name = "{index}: test with alternateMatchPaths={0}")
            fun data(): Collection<Any> {
                return listOf(
                    emptyList<String>() to (JSONObject("""{ "key1": 1, "key2": 2 }""") to JSONObject("""{ "key1": 1}""")),
                    listOf("key1") to (JSONObject("""{ "key1": 1, "key2": 2 }""") to JSONObject("""{ "key1": 1}""")),
                    listOf("key2") to (JSONObject("""{ "key1": 1, "key2": 2 }""") to JSONObject("""{ "key1": 1}""")),
                    listOf("key1", "key2") to (JSONObject("""{ "key1": 1, "key2": 2 }""") to JSONObject("""{ "key1": 1}""")),
                ).map { (keyPaths, pair) ->
                    arrayOf(keyPaths, PlainParams(pair.first, pair.second))
                }
            }
        }

        /**
         * Validates that a larger expected dictionary compared to actual will throw errors
         * even when using alternate match paths.
         *
         * Consequence: Guarantees that dictionary size validation isn't affected by alternate paths.
         */
        @Test
        fun `should error on larger expected maps`() {
            Assert.assertThrows(AssertionError::class.java) {
                assertEquals(params.expected, params.actual)
            }
            Assert.assertThrows(AssertionError::class.java) {
                assertExactMatch(params.expected, params.actual, ValueTypeMatch(keyPaths))
            }
            Assert.assertThrows(AssertionError::class.java) {
                assertTypeMatch(params.expected, params.actual, ValueExactMatch(keyPaths))
            }
        }
    }
}
