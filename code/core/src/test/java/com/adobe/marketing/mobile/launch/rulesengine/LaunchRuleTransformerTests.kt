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

package com.adobe.marketing.mobile.launch.rulesengine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.ArrayList
import java.util.HashMap

class LaunchRuleTransformerTests {

    @Test
    fun transform_ReturnsEncodedURL_WhenTransformingStringToUrlEnc() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("urlenc", "this is a test string")
        assertEquals("transform should return url encoded string when url encoding string", "this%20is%20a%20test%20string", result)
    }

    @Test
    fun transform_ReturnsInt_WhenTransformingIntToUrlEnc() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("urlenc", 3)
        assertEquals("transform should return int when url encoding int", 3, result)
    }

    @Test
    fun transform_ReturnsDouble_WhenTransformingDoubleToUrlEnc() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("urlenc", 3.33)
        assertEquals("transform should return double when url encoding double", 3.33, result)
    }

    @Test
    fun transform_ReturnsBoolean_WhenTransformingBooleanToUrlEnc() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("urlenc", true)
        assertEquals("transform should return boolean when url encoding boolean", true, result)
    }

    @Test
    fun transform_ReturnsInt_WhenTransformingIntToInt() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("int", 3)
        assertEquals("transform should return int when transforming int to int", 3, result)
    }

    @Test
    fun transform_ReturnsInt_WhenTransformingBooleanToInt() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("int", true)
        assertEquals("transform should return int when transforming boolean to int", 1, result)
    }

    @Test
    fun transform_ReturnsInt_WhenTransformingDoubleToInt() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("int", 3.33)
        assertEquals("transform should return int when transforming double to int", 3, result)
    }

    @Test
    fun transform_ReturnsInt_WhenTransformingValidStringToInt() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("int", "3")
        assertEquals("transform should return int when transforming valid string to int", 3, result)
    }

    @Test
    fun transform_ReturnsInt_WhenTransformingInvalidStringToInt() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("int", "something")
        assertEquals("transform should return value when transforming invalid string to int", "something", result)
    }

    @Test
    fun transform_ReturnsNull_WhenTransformingNullToInt() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("int", null)
        assertNull("transform should return null when transforming null to int", result)
    }

    @Test
    fun transform_ReturnsList_WhenTransformingListToInt() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("int", getDefaultList())
        assertEquals("transform should return list when transforming list to int", getDefaultList(), result)
    }

    @Test
    fun transform_ReturnsMap_WhenTransformingMapToInt() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("int", getDefaultMap())
        assertEquals("transform should return map when transforming map to int", getDefaultMap(), result)
    }

    @Test
    fun transform_ReturnsString_WhenTransformingIntToString() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("string", 3)
        assertEquals("transform should return string when transforming int to string", "3", result)
    }

    @Test
    fun transform_ReturnsString_WhenTransformingBooleanToString() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("string", true)
        assertEquals("transform should return string when transforming boolean to string", "true", result)
    }

    @Test
    fun transform_ReturnsString_WhenTransformingDoubleToString() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("string", 3.33)
        assertEquals("transform should return string when transforming string to string", "3.33", result)
    }

    @Test
    fun transform_ReturnsNull_WhenTransformingNullToString() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("string", null)
        assertNull("transform should return null when transforming null to string", result)
    }

    @Test
    fun transform_ReturnsString_WhenTransformingListToString() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("string", getDefaultList())
        assertEquals("transform should return string when transforming list to string", "[item1, item2]", result)
    }

    @Test
    fun transform_ReturnsString_WhenTransformingMapToString() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("string", getDefaultMap())
        assertEquals("transform should return string when transforming map to string", "{key1=value1, key2=value2}", result)
    }

    @Test
    fun transform_ReturnsDouble_WhenTransformingIntToDouble() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("double", 3)
        assertEquals("transform should return double when transforming int to double", 3.0, result)
    }

    @Test
    fun transform_ReturnsDouble_WhenTransformingBooleanToDouble() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("double", true)
        assertEquals("transform should return double when transforming boolean to double", 1.0, result)
    }

    @Test
    fun transform_ReturnsDouble_WhenTransformingDoubleToDouble() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("double", 3.33)
        assertEquals("transform should return double when transforming double to double", 3.33, result)
    }

    @Test
    fun transform_ReturnsDouble_WhenTransformingValidStringToDouble() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("double", "3.33")
        assertEquals("transform should return double when transforming valid string to double", 3.33, result)
    }

    @Test
    fun transform_ReturnsDouble_WhenTransformingInvalidStringToDouble() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("double", "something")
        assertEquals("transform should return value when transforming invalid string to double", "something", result)
    }

    @Test
    fun transform_ReturnsNull_WhenTransformingNullToDouble() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("double", null)
        assertNull("transform should return null when transforming null to double", result)
    }

    @Test
    fun transform_ReturnsList_WhenTransformingListToDouble() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("double", getDefaultList())
        assertEquals("transform should return list when transforming list to double", getDefaultList(), result)
    }

    @Test
    fun transform_ReturnsMap_WhenTransformingMapToDouble() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("double", getDefaultMap())
        assertEquals("transform should return map when transforming map to double", getDefaultMap(), result)
    }

    @Test
    fun transform_ReturnsFalse_WhenTransformingInt0ToBoolean() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("bool", 0)
        assertEquals("transform should return false when transforming int 0 to boolean", false, result)
    }

    @Test
    fun transform_ReturnsTrue_WhenTransformingInt1ToBoolean() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("bool", 1)
        assertEquals("transform should return true when transforming int 1 to boolean", true, result)
    }

    @Test
    fun transform_ReturnsFalse_WhenTransformingIntRandomToBoolean() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("bool", 3)
        assertEquals("transform should return false when transforming random int to boolean", false, result)
    }

    @Test
    fun transform_ReturnsBoolean_WhenTransformingBooleanToBoolean() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("bool", true)
        assertEquals("transform should return boolean when transforming boolean to boolean", true, result)
    }

    @Test
    fun transform_ReturnsFalse_WhenTransformingDouble0ToBoolean() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("bool", 0.0)
        assertEquals("transform should return false when transforming double 0.0 to boolean", false, result)
    }

    @Test
    fun transform_ReturnsTrue_WhenTransformingDouble1ToBoolean() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("bool", 1.0)
        assertEquals("transform should return true when transforming double 1.0 to boolean", true, result)
    }

    @Test
    fun transform_ReturnsFalse_WhenTransformingDoubleRandomToBoolean() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("bool", 1.123)
        assertEquals("transform should return boolean when transforming random double to boolean", false, result)
    }

    @Test
    fun transform_ReturnsFalse_WhenTransformingValidStringFalseToBoolean() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("bool", "false")
        assertEquals("transform should return false when transforming valid string false to boolean", false, result)
    }

    @Test
    fun transform_ReturnsTrue_WhenTransformingValidStringTrueToBoolean() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("bool", "true")
        assertEquals("transform should return true when transforming valid string true to boolean", true, result)
    }

    @Test
    fun transform_ReturnsFalse_WhenTransformingInvalidStringToBoolean() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("bool", "something")
        assertEquals("transform should return false when transforming invalid string to boolean", false, result)
    }

    @Test
    fun transform_ReturnsNull_WhenTransformingNullToBoolean() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("bool", null)
        assertNull("transform should return null when transforming null to boolean", result)
    }

    @Test
    fun transform_ReturnsList_WhenTransformingListToBoolean() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("bool", getDefaultList())
        assertEquals("transform should return list when transforming list to boolean", getDefaultList(), result)
    }

    @Test
    fun transform_ReturnsMap_WhenTransformingMapToBoolean() {
        val transformer = LaunchRuleTransformer.createTransforming()
        val result = transformer.transform("bool", getDefaultMap())
        assertEquals("transform should return map when transforming map to boolean", getDefaultMap(), result)
    }

    private fun getDefaultList(): List<String> {
        val list: MutableList<String> = ArrayList()
        list.add("item1")
        list.add("item2")
        return list
    }

    private fun getDefaultMap(): Map<String, String> {
        val map: MutableMap<String, String> = HashMap()
        map["key1"] = "value1"
        map["key2"] = "value2"
        return map
    }
}
