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

package com.adobe.marketing.mobile.launch.rulesengine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.adobe.marketing.mobile.rulesengine.Evaluable;
import java.util.Collections;
import org.junit.Test;

public class LaunchRuleJavaTests {

    private final Evaluable mockEvaluable = mock(Evaluable.class);
    private final RuleConsequence mockConsequence = mock(RuleConsequence.class);

    @Test
    public void testLaunchRuleCreationWithoutRevaluable() {
        final LaunchRule rule =
                new LaunchRule(mockEvaluable, Collections.singletonList(mockConsequence));
        assertFalse(rule.getMeta().getReEvaluate());
        assertEquals(mockEvaluable, rule.getEvaluable());
    }

    @Test
    public void testLaunchRuleCreationWithRevaluableFalse() {
        final LaunchRule rule =
                new LaunchRule(mockEvaluable, Collections.singletonList(mockConsequence));
        assertFalse(rule.getMeta().getReEvaluate());
        assertEquals(mockEvaluable, rule.getEvaluable());
    }

    @Test
    public void testLaunchRuleCreationWithRevaluableTrue() {
        final LaunchRule rule =
                new LaunchRule(
                        mockEvaluable,
                        Collections.singletonList(mockConsequence),
                        new RuleMeta(true));
        assertTrue(rule.getMeta().getReEvaluate());
        assertEquals(mockEvaluable, rule.getEvaluable());
    }
}
