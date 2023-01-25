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

package com.adobe.marketing.mobile.launch.rulesengine.json

import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.rulesengine.ComparisonExpression
import com.adobe.marketing.mobile.rulesengine.Evaluable
import com.adobe.marketing.mobile.rulesengine.LogicalExpression
import com.adobe.marketing.mobile.test.util.buildJSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner.Silent::class)
class JSONConditionTests {
    private lateinit var extensionApi: ExtensionApi

    @Before
    fun setup() {
        extensionApi = Mockito.mock(ExtensionApi::class.java)
    }

    @Test
    fun testBadJsonFormatWithoutMatcherValue() {
        val jsonConditionString = """
        {
          "type": "matcher",
          "definition": {
            "key": "~state.com.adobe.module.lifecycle/lifecyclecontextdata.launches",
            "values": [
              2
            ]
          }
        }
        """.trimIndent()
        val jsonCondition = JSONCondition.build(buildJSONObject(jsonConditionString), extensionApi)
        assertTrue(jsonCondition is MatcherCondition)
        val evaluable = jsonCondition.toEvaluable()
        assertTrue(evaluable == null)
    }

    @Test
    fun testBadJsonFormatWithUnsupportedMatcherValue() {
        val jsonConditionString = """
        {
          "type": "matcher",
          "definition": {
            "key": "~state.com.adobe.module.lifecycle/lifecyclecontextdata.launches",
            "matcher": "ge1",
            "values": [
              2
            ]
          }
        }
        """.trimIndent()
        val jsonCondition = JSONCondition.build(buildJSONObject(jsonConditionString), extensionApi)
        assertTrue(jsonCondition is MatcherCondition)
        val evaluable = jsonCondition.toEvaluable()
        assertTrue(evaluable == null)
    }

    @Test
    fun testMatcherWithOneValue() {
        val jsonConditionString = """
        {
          "type": "matcher",
          "definition": {
            "key": "~state.com.adobe.module.lifecycle/lifecyclecontextdata.launches",
            "matcher": "ge",
            "values": [
              2
            ]
          }
        }
        """.trimIndent()
        val jsonCondition = JSONCondition.build(buildJSONObject(jsonConditionString), extensionApi)
        assertTrue(jsonCondition is MatcherCondition)
        val evaluable = jsonCondition.toEvaluable()
        assertTrue(evaluable is Evaluable)
        assertTrue(evaluable is ComparisonExpression<*, *>)
    }

    @Test
    fun testMatcherWithoutValue() {
        val jsonConditionString = """
        {
          "type": "matcher",
          "definition": {
            "key": "~state.com.adobe.module.lifecycle/lifecyclecontextdata.launches",
            "matcher": "ge",
            "values": [1]
          }
        }
        """.trimIndent()
        val jsonCondition = JSONCondition.build(buildJSONObject(jsonConditionString), extensionApi)
        assertTrue(jsonCondition is MatcherCondition)
        val evaluable = jsonCondition.toEvaluable()
        assertTrue(evaluable is Evaluable)
        assertTrue(evaluable is ComparisonExpression<*, *>)
    }

    @Test
    fun testMatcherWithMultipleValues() {
        val jsonConditionString = """
        {
          "type": "matcher",
          "definition": {
            "key": "~state.com.adobe.module.lifecycle/lifecyclecontextdata.carriername",
            "matcher": "co",
            "values": [
              "ATT",
              "VERIZON"
            ]
          }
        }
        """.trimIndent()
        val jsonCondition = JSONCondition.build(buildJSONObject(jsonConditionString), extensionApi)
        assertTrue(jsonCondition is MatcherCondition)
        val evaluable = jsonCondition.toEvaluable()
        assertTrue(evaluable is Evaluable)
        assertTrue(evaluable is LogicalExpression)
    }

    @Test
    fun testAndGroup() {
        val jsonConditionString = """
        {
          "type": "group",
          "definition": {
            "logic": "and",
            "conditions": [
              {
                "type": "matcher",
                "definition": {
                  "key": "~type",
                  "matcher": "eq",
                  "values": [
                    "com.adobe.eventType.lifecycle"
                  ]
                }
              },
              {
                "type": "matcher",
                "definition": {
                  "key": "~source",
                  "matcher": "eq",
                  "values": [
                    "com.adobe.eventSource.responseContent"
                  ]
                }
              }
            ]
          }
        }
        """.trimIndent()
        val jsonCondition = JSONCondition.build(buildJSONObject(jsonConditionString), extensionApi)
        assertTrue(jsonCondition is GroupCondition)
        val evaluable = jsonCondition.toEvaluable()
        assertTrue(evaluable is Evaluable)
        assertTrue(evaluable is LogicalExpression)
    }
}
