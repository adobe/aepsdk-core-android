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
import com.adobe.marketing.mobile.ExtensionApi;
import com.adobe.marketing.mobile.rulesengine.ConditionEvaluator;
import com.adobe.marketing.mobile.rulesengine.RulesEngine;
import java.util.List;

public class LaunchRulesEngine {

    private final RulesEngine<LaunchRule> ruleRulesEngine;
    private final ExtensionApi extensionApi;

    public LaunchRulesEngine(final ExtensionApi extensionApi) {
        ruleRulesEngine =
                new RulesEngine<>(
                        new ConditionEvaluator(ConditionEvaluator.Option.CASE_INSENSITIVE),
                        LaunchRuleTransformer.INSTANCE.createTransforming());
        this.extensionApi = extensionApi;
    }

    /**
     * Set a new set of rules, the new rules replace the current rules.
     *
     * @param rules a list of {@link LaunchRule}s
     */
    public void replaceRules(final List<LaunchRule> rules) {
        ruleRulesEngine.replaceRules(rules);
    }

    /**
     * Adds a new set of rules, the new rules are added to the current rules.
     *
     * @param rules a list of {@link LaunchRule}s
     */
    public void addRules(final List<LaunchRule> rules) {
        ruleRulesEngine.addRules(rules);
    }

    /**
     * Evaluates all the current rules against the supplied {@link Event}.
     *
     * @param event the {@link Event} against which to evaluate the rules
     * @return the matched {@code List<LaunchRule>}
     */
    public List<LaunchRule> process(final Event event) {
        return ruleRulesEngine.evaluate(new LaunchTokenFinder(event, extensionApi));
    }

    List<LaunchRule> getRules() {
        return ruleRulesEngine.getRules();
    }
}
