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
import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.rulesengine.ConditionEvaluator;
import com.adobe.marketing.mobile.rulesengine.Log;
import com.adobe.marketing.mobile.rulesengine.LogLevel;
import com.adobe.marketing.mobile.rulesengine.Logging;
import com.adobe.marketing.mobile.rulesengine.RulesEngine;
import com.adobe.marketing.mobile.rulesengine.TokenFinder;

import java.util.List;

public class LaunchRulesEngine {
    private final RulesEngine<LaunchRule> ruleRulesEngine;
    private final ExtensionApi extensionApi;

    // TODO pass in extensionApi to the constructor
    @SuppressWarnings("rawtypes")
    public LaunchRulesEngine(final ExtensionApi extensionApi) {
        ruleRulesEngine = new RulesEngine<>(new ConditionEvaluator(ConditionEvaluator.Option.CASE_INSENSITIVE), LaunchRuleTransformer.INSTANCE.createTransforming());
        Log.setLogging(new Logging() {
            @Override
            public void log(LogLevel level, String tag, String message) {
                LoggingMode loggingMode;
                switch (level) {
                    case DEBUG:
                        loggingMode = LoggingMode.DEBUG;
                        break;
                    case ERROR:
                        loggingMode = LoggingMode.ERROR;
                        break;
                    case WARNING:
                        loggingMode = LoggingMode.WARNING;
                        break;
                    default:
                        loggingMode = LoggingMode.VERBOSE;
                }
                MobileCore.log(loggingMode, tag, message);
            }
        });
        this.extensionApi = extensionApi;
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
     *
     * @param event the {@link Event} against which to evaluate the rules
     * @return the matched {@link List<LaunchRule>}
     */
    public List<LaunchRule> process(Event event) {
        return ruleRulesEngine.evaluate(new LaunchTokenFinder(event, extensionApi));
    }
}
