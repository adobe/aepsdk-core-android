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

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.EventSource;
import com.adobe.marketing.mobile.EventType;
import com.adobe.marketing.mobile.ExtensionApi;
import com.adobe.marketing.mobile.rulesengine.ConditionEvaluator;
import com.adobe.marketing.mobile.rulesengine.RulesEngine;
import com.adobe.marketing.mobile.util.DataReader;
import com.adobe.marketing.mobile.util.StringUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class LaunchRulesEngine {

    @VisibleForTesting static final String RULES_ENGINE_NAME = "name";
    private final String name;
    private final RulesEngine<LaunchRule> ruleRulesEngine;
    private final ExtensionApi extensionApi;
    private final LaunchRulesConsequence launchRulesConsequence;
    private final List<Event> cachedEvents = new ArrayList<>();
    private final AtomicBoolean initialRulesReceived = new AtomicBoolean(false);

    public LaunchRulesEngine(@NonNull final String name, @NonNull final ExtensionApi extensionApi) {
        this(
                name,
                extensionApi,
                new RulesEngine<>(
                        new ConditionEvaluator(ConditionEvaluator.Option.CASE_INSENSITIVE),
                        LaunchRuleTransformer.INSTANCE.createTransforming()),
                new LaunchRulesConsequence(extensionApi));
    }

    @VisibleForTesting
    LaunchRulesEngine(
            final String name,
            final ExtensionApi extensionApi,
            final RulesEngine<LaunchRule> ruleEngine,
            final LaunchRulesConsequence launchRulesConsequence) {
        if (StringUtils.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("LaunchRulesEngine cannot have a null/empty name");
        }

        this.name = name;
        this.launchRulesConsequence = launchRulesConsequence;
        this.extensionApi = extensionApi;
        this.ruleRulesEngine = ruleEngine;
    }

    /**
     * Set a new set of rules, the new rules replace the current rules.
     *
     * @param rules a list of {@link LaunchRule}s
     */
    public void replaceRules(final List<LaunchRule> rules) {
        if (rules == null) return;

        ruleRulesEngine.replaceRules(rules);
        initialRulesReceived.compareAndSet(false, true);
        // send a reset request event for the current LaunchRulesEngine
        final Event dispatchEvent =
                new Event.Builder(name, EventType.RULES_ENGINE, EventSource.REQUEST_RESET)
                        .setEventData(Collections.singletonMap(RULES_ENGINE_NAME, name))
                        .build();

        extensionApi.dispatch(dispatchEvent);
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
     * Processes the supplied event with all the rules that match it. This processing may result in
     * dispatch of the supplied event after attachment, modification of its data or dispatch of a
     * new event from the supplied event.
     *
     * @param event the event to be evaluated
     * @return the processed [Event] after token replacement.
     */
    public Event processEvent(@NonNull final Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Cannot evaluate null event.");
        }

        // if initial rule set has not been received, cache the event to be processed
        // when rules are set
        if (!initialRulesReceived.get()) {
            cachedEvents.add(event);
            return event;
        }

        if (!cachedEvents.isEmpty() && shouldProcessCachedEvents(event)) {
            reprocessCachedEvents();
        }

        final List<LaunchRule> matchedRules =
                ruleRulesEngine.evaluate(new LaunchTokenFinder(event, extensionApi));
        return launchRulesConsequence.process(event, matchedRules);
    }

    private boolean shouldProcessCachedEvents(final Event event) {
        return EventType.RULES_ENGINE.equals(event.getType())
                && EventSource.REQUEST_RESET.equals(event.getSource())
                && name.equals(DataReader.optString(event.getEventData(), RULES_ENGINE_NAME, ""));
    }

    /**
     * Evaluates the supplied event against the all current rules and returns the {@link
     * RuleConsequence}'s from the rules that matched the supplied event.
     *
     * @param event the event to be evaluated
     * @return a {@code List<RuleConsequence>} that match the supplied event.
     */
    public List<RuleConsequence> evaluateEvent(@NonNull final Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Cannot evaluate null event.");
        }

        final List<LaunchRule> matchedRules =
                ruleRulesEngine.evaluate(new LaunchTokenFinder(event, extensionApi));

        // get token replaced consequences
        return launchRulesConsequence.evaluate(event, matchedRules);
    }

    List<LaunchRule> getRules() {
        return ruleRulesEngine.getRules();
    }

    @VisibleForTesting
    int getCachedEventCount() {
        return cachedEvents.size();
    }

    private void reprocessCachedEvents() {
        for (Event cachedEvent : cachedEvents) {
            final List<LaunchRule> matchedRules =
                    ruleRulesEngine.evaluate(new LaunchTokenFinder(cachedEvent, extensionApi));
            launchRulesConsequence.process(cachedEvent, matchedRules);
        }
        // clear cached events and set the flag to indicate that rules were set at least once
        cachedEvents.clear();
    }
}
