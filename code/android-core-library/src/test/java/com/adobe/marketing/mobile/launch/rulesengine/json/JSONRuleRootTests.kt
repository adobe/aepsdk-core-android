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
import com.adobe.marketing.mobile.test.util.buildJSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner.Silent::class)
class JSONRuleRootTests {

    private lateinit var extensionApi: ExtensionApi

    @Before
    fun setup() {
        extensionApi = Mockito.mock(ExtensionApi::class.java)
    }

    @Test
    fun testNormal() {
        val jsonString = """
        {
          "version": 1,
          "rules": [
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
          ]
        }
        """.trimIndent()
        val jsonRuleRoot = JSONRuleRoot(buildJSONObject(jsonString))
        assertTrue(jsonRuleRoot is JSONRuleRoot)
        val launchRules = jsonRuleRoot.toLaunchRules(extensionApi)
        assertEquals(1, launchRules.size)
    }

    @Test
    fun testWithoutVersion() {
        val jsonString = """
        {
          "rules": [
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
          ]
        }
        """.trimIndent()
        val jsonRuleRoot = JSONRuleRoot(buildJSONObject(jsonString))
        assertTrue(jsonRuleRoot is JSONRuleRoot)
        assertEquals("0", jsonRuleRoot.version)
    }
}
