package com.adobe.marketing.mobile.launch.rulesengine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import com.adobe.marketing.mobile.rulesengine.Evaluable;

import org.junit.Test;

import java.util.Collections;

public class LaunchRuleJavaTests {

    private final Evaluable mockEvaluable = mock(Evaluable.class);
    private final RuleConsequence mockConsequence = mock(RuleConsequence.class);

    @Test
    public void testLaunchRuleCreationWithoutRevaluable() {
        final LaunchRule rule = new LaunchRule(mockEvaluable, Collections.singletonList(mockConsequence));
        assertFalse(rule.getMeta().getReEvaluable());
        assertEquals(mockEvaluable, rule.getEvaluable());
    }

    @Test
    public void testLaunchRuleCreationWithRevaluableFalse() {
        final LaunchRule rule = new LaunchRule(mockEvaluable, Collections.singletonList(mockConsequence));
        assertFalse(rule.getMeta().getReEvaluable());
        assertEquals(mockEvaluable, rule.getEvaluable());
    }

    @Test
    public void testLaunchRuleCreationWithRevaluableTrue() {
        final LaunchRule rule = new LaunchRule(mockEvaluable, Collections.singletonList(mockConsequence), new RuleMeta(true));
        assertTrue(rule.getMeta().getReEvaluable());
        assertEquals(mockEvaluable, rule.getEvaluable());
    }
}