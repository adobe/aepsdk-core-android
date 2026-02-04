/*
  Copyright 2026 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.launch.rulesengine

import com.adobe.marketing.mobile.rulesengine.Evaluable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock

class LaunchRuleTests {

    private val mockEvaluable: Evaluable = mock()
    private val mockConsequence: RuleConsequence = mock()

    @Test
    fun `test LaunchRule creation without revaluable parameter`() {
        val rule = LaunchRule(mockEvaluable, listOf(mockConsequence))
        assertFalse(rule.meta.reEvaluate)
        assertEquals(mockEvaluable, rule.evaluable)
    }

    @Test
    fun `test LaunchRule creation with revaluable as false`() {
        val rule = LaunchRule(mockEvaluable, listOf(mockConsequence))
        assertFalse(rule.meta.reEvaluate)
        assertEquals(mockEvaluable, rule.evaluable)
    }

    @Test
    fun `test LaunchRule creation with revaluable as true`() {
        val rule = LaunchRule(mockEvaluable, listOf(mockConsequence), RuleMeta(true))
        assertTrue(rule.meta.reEvaluate)
        assertEquals(mockEvaluable, rule.evaluable)
    }
}
