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
        assertFalse(rule.meta.reEvaluable)
        assertEquals(mockEvaluable, rule.evaluable)
    }

    @Test
    fun `test LaunchRule creation with revaluable as false`() {
        val rule = LaunchRule(mockEvaluable, listOf(mockConsequence))
        assertFalse(rule.meta.reEvaluable)
        assertEquals(mockEvaluable, rule.evaluable)
    }

    @Test
    fun `test LaunchRule creation with revaluable as true`() {
        val rule = LaunchRule(mockEvaluable, listOf(mockConsequence), RuleMeta(true))
        assertTrue(rule.meta.reEvaluable)
        assertEquals(mockEvaluable, rule.evaluable)
    }
}