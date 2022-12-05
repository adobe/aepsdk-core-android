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
import com.adobe.marketing.mobile.launch.rulesengine.LaunchRule
import com.adobe.marketing.mobile.rulesengine.ComparisonExpression
import com.adobe.marketing.mobile.test.util.buildJSONObject
import org.json.JSONException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner.Silent::class)
class JSONRuleTests {

    private lateinit var extensionApi: ExtensionApi

    @Before
    fun setup() {
        extensionApi = Mockito.mock(ExtensionApi::class.java)
    }

    @Test
    fun testNormal() {
        val jsonString = """
        {
          "condition": {
            "type": "matcher",
            "definition": {
              "key": "~type",
              "matcher": "eq",
              "values": [
                "com.adobe.eventType.lifecycle"
              ]
            }
          },
          "consequences": [
            {
              "id": "RC2500e6b0140744d49e6fd503a55a66d4",
              "type": "pb",
              "detail": {
                "timeout": 0,
                "templateurl": "http://www.adobe.com"
              }
            }
          ]
        }
        """.trimIndent()
        val jsonRule = JSONRule(buildJSONObject(jsonString))
        assertTrue(jsonRule is JSONRule)
        val launchRule = jsonRule.toLaunchRule(extensionApi)
        assertTrue(launchRule is LaunchRule)
        assertEquals(1, launchRule.consequenceList.size)
        assertEquals("pb", launchRule.consequenceList[0].type)
        assertTrue(launchRule.condition is ComparisonExpression<*, *>)
    }

    @Test
    fun testNullInput() {
        assertNull(JSONRule(null))
    }

    @Test(expected = JSONException::class)
    fun testBadJSONFormat() {
        val jsonString = """
        {
          "condition": {
            "type": "matcher",
            "definition": {
              "key": "~type",
              "matcher": "eq",
              "values": [
                "com.adobe.eventType.lifecycle"
              ]
            }
          }
        }
        """.trimIndent()
        JSONRule(buildJSONObject(jsonString))
    }
}
