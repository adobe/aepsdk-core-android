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

import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.junit.Test

class JSONRulesParserTests {
    @Test
    fun testBadJSONFormat() {
        val result = JSONRulesParser.parse("")
        assertNull(result)
    }

    @Test
    fun testNormal() {
        val fileTxt = this::class.java.classLoader?.getResource("rules_parser/launch_rule_root.json")
            ?.readText()
        assertNotNull(fileTxt)
        val result = JSONRulesParser.parse(fileTxt)
        assertNotNull(result)
        assertEquals(1, result.size)
    }
}
