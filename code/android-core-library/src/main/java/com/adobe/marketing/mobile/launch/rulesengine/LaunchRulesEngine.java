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
package com.adobe.marketing.mobile.launch.rulesengine;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.rulesengine.ConditionEvaluator;
import com.adobe.marketing.mobile.rulesengine.RulesEngine;
import com.adobe.marketing.mobile.rulesengine.TokenFinder;

import java.util.List;

public class LaunchRulesEngine {
    private final RulesEngine<LaunchRule> ruleRulesEngine;

    @SuppressWarnings("rawtypes")
    public LaunchRulesEngine() {
        ruleRulesEngine = new RulesEngine<>(new ConditionEvaluator(), LaunchRuleTransformer.INSTANCE.createTransforming());
    }

    /**
     * Set a new set of rules, the new rules replace the current rules.
     * @param rules a list of {@link LaunchRule}s
     */
    public void replaceRules(final List<LaunchRule> rules) {
        ruleRulesEngine.replaceRules(rules);
    }

    /**
     * Evaluates all the current rules against the supplied {@link Event}.
     * @param event the {@link Event} against which to evaluate the rules
     * @return the  processed {@link Event}
     */
    public Event process(Event event) {
        ruleRulesEngine.evaluate(new LaunchTokenFinder(event));
        return event;
    }
}
