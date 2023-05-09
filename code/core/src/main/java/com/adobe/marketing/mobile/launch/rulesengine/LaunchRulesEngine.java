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
import java.util.UUID;

public class LaunchRulesEngine {

    private static final String DEFAULT_PREFIX = "LaunchRulesEngine";
    @VisibleForTesting static final String RULES_ENGINE_NAME = "name";
    private final String name;
    private final RulesEngine<LaunchRule> ruleRulesEngine;
    private final ExtensionApi extensionApi;
    private final LaunchRulesConsequence launchRulesConsequence;
    private List<Event> cachedEvents = new ArrayList<>();

    public LaunchRulesEngine(final ExtensionApi extensionApi) {
        this(String.format("%s-%s", DEFAULT_PREFIX, UUID.randomUUID()), extensionApi);
    }

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
     * Evaluates all the current rules against the supplied {@link Event}.
     *
     * @param event the {@link Event} against which to evaluate the rules
     * @return the matched {@code List<LaunchRule>}
     */
    public List<LaunchRule> process(final Event event) {
        return ruleRulesEngine.evaluate(new LaunchTokenFinder(event, extensionApi));
    }

    /**
     * Evaluates all the current rules against the supplied event. This evaluation may result in
     * dispatch of the supplied event after attachment, modification of its data or dispatch of a
     * new event from the supplied event.
     *
     * @param event the event to be evaluated
     * @return the processed [Event] after token replacement.
     */
    public Event evaluate(@NonNull final Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Cannot evaluate null event.");
        }

        final List<LaunchRule> matchedRules = process(event);

        // if cachedEvents is null, we know that rules are set and can skip to evaluation
        if (cachedEvents == null) {
            return launchRulesConsequence.evaluate(event, matchedRules);
        }

        // If this is an event to start processing of cachedEvents reprocess cached events
        // otherwise, add the event to cachedEvents till rules are set
        if (EventType.RULES_ENGINE.equals(event.getType())
                && EventSource.REQUEST_RESET.equals(event.getSource())
                && name.equals(DataReader.optString(event.getEventData(), RULES_ENGINE_NAME, ""))) {
            reprocessCachedEvents();
        } else {
            cachedEvents.add(event);
        }

        return launchRulesConsequence.evaluate(event, matchedRules);
    }

    /**
     * Evaluates the supplied event against the all current rules and returns the {@link
     * RuleConsequence}'s from the rules that matched the supplied event.
     *
     * @param event the event to be evaluated
     * @return a {@code List<RuleConsequence>} that match the supplied event.
     */
    public List<RuleConsequence> evaluateConsequence(@NonNull final Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Cannot evaluate null event.");
        }

        final List<LaunchRule> matchedRules =
                ruleRulesEngine.evaluate(new LaunchTokenFinder(event, extensionApi));

        // get token replaced consequences
        final LaunchRulesConsequence launchRulesConsequence =
                new LaunchRulesConsequence(extensionApi);
        return launchRulesConsequence.evaluateConsequence(event, matchedRules);
    }

    List<LaunchRule> getRules() {
        return ruleRulesEngine.getRules();
    }

    @VisibleForTesting
    int getCachedEventCount() {
        return cachedEvents != null ? cachedEvents.size() : 0;
    }

    private void reprocessCachedEvents() {
        if (cachedEvents != null) {
            for (Event cachedEvent : cachedEvents) {
                final List<LaunchRule> matchedRules = process(cachedEvent);
                launchRulesConsequence.evaluate(cachedEvent, matchedRules);
            }
        }
        clearCachedEvents();
    }

    private void clearCachedEvents() {
        if (cachedEvents == null) return;

        cachedEvents.clear();
        cachedEvents = null;
    }
}
