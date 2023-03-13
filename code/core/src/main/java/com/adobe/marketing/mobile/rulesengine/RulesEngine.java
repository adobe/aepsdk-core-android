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

package com.adobe.marketing.mobile.rulesengine;

import java.util.ArrayList;
import java.util.List;

/** RulesEngine to evaluate matching rules for given input data */
public class RulesEngine<T extends Rule> {

    private final Object rulesEngineMutex = new Object();
    private final Evaluating evaluator;
    private final Transforming transformer;
    private List<T> rules;

    public RulesEngine(final Evaluating evaluator, final Transforming transformer) {
        this.evaluator = evaluator;
        this.transformer = transformer;
        this.rules = new ArrayList<>();
    }

    public List<T> evaluate(final TokenFinder tokenFinder) {
        synchronized (rulesEngineMutex) {
            final Context context = new Context(tokenFinder, evaluator, transformer);
            List<T> triggerRules = new ArrayList<>();

            for (final T rule : rules) {
                RulesResult result = rule.getEvaluable().evaluate(context);
                if (result.isSuccess()) {
                    triggerRules.add(rule);
                }
            }
            return triggerRules;
        }
    }

    public void replaceRules(final List<T> newRules) {
        if (newRules == null) {
            return;
        }

        synchronized (rulesEngineMutex) {
            rules = new ArrayList<>(newRules);
        }
    }

    public void addRules(final List<T> newRules) {
        synchronized (rulesEngineMutex) {
            rules.addAll(newRules);
        }
    }

    public List<T> getRules() {
        return new ArrayList<>(rules);
    }
}
