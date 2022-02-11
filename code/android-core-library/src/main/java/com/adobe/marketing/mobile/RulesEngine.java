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

package com.adobe.marketing.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * {@code RulesEngine} is responsible for storing the relationship between modules and rules they have
 * registered.  It also provides the core mechanism for interpreting a set of rule conditions and
 * generating a list of token-expanded consequence events.
 */
class RulesEngine { // not marked as final, but should not be extended except for testing purposes
	protected final RuleTokenParser ruleTokenParser;
	protected final ConcurrentHashMap<Module, ConcurrentLinkedQueue<Rule>> moduleRuleAssociation;

	/**
	 * Keeps a map of last dispatched event in a chained dispatch consequence flow and the number of times dispatch consequence was triggered.
	 * The max number of times an event can be evaluated for dispatch rules is controlled by {@link RulesEngineConstants#MAX_CHAINED_CONSEQUENCE_COUNT}
	 * and it is meant to prevent unintended event loops when using the dispatch consequence.
	 * Map: key is the event unique id and value is the current count in the consequence chain
	 */
	private final ConcurrentHashMap<String, Integer> dispatchChainedEvents;

	private static final Object rulesOperationMutex = new Object();
	private static final String LOG_PREFIX = "Rules Engine";

	/**
	 * Constructor for {@code RulesEngine} class
	 * <p>
	 * Constructs a new instance of the {@code RulesEngine} class and associates it with
	 * it's owning {@link EventHub}.
	 *
	 * @param hub the {@link EventHub} parent instance.
	 */
	public RulesEngine(final EventHub hub) {
		ruleTokenParser = new RuleTokenParser(hub);
		moduleRuleAssociation = new ConcurrentHashMap<Module, ConcurrentLinkedQueue<Rule>>();
		dispatchChainedEvents = new ConcurrentHashMap<String, Integer>();
	}

	/**
	 * Adds a rule to the set of rules associated with the given module.
	 *
	 * @param owningModule {@link Module} instance that's adding the {@link Rule}
	 * @param rule {@link Rule} object to be added
	 */
	protected void addRule(final Module owningModule, final Rule rule) {
		synchronized (rulesOperationMutex) {
			if (rule == null) {
				return;
			}

			if (rule.getConsequenceEvents() == null || rule.getConsequenceEvents().isEmpty()) {
				return;
			}

			moduleRuleAssociation.putIfAbsent(owningModule, new ConcurrentLinkedQueue<Rule>());
			moduleRuleAssociation.get(owningModule).add(rule);
		}
	}

	/**
	 * Replaces all rules associated with the given module.
	 *
	 * @param owningModule {@link Module} instance that's adding the {@link Rule}
	 * @param rules a {@code List} of {@link Rule} object
	 */
	protected  void replaceRules(final Module owningModule, final List<Rule> rules) {
		synchronized (rulesOperationMutex) {
			if (rules == null) {
				moduleRuleAssociation.remove(owningModule);
			} else {
				moduleRuleAssociation.put(owningModule, new ConcurrentLinkedQueue<Rule>(rules));
			}
		}
	}

	ConcurrentHashMap<Module, ConcurrentLinkedQueue<Rule>>  getModuleRuleAssociation() {
		return this.moduleRuleAssociation;
	}


	/**
	 * Unregisters all rules for the given module.
	 *
	 * @param owningModule {@link Module} instance to clear all rules for.
	 */
	protected void unregisterAllRules(final Module owningModule) {
		synchronized (rulesOperationMutex) {
			moduleRuleAssociation.remove(owningModule);
		}
	}

	/**
	 * Evaluates all rules (across all registered modules) against the given triggerEvent
	 *
	 * @param triggerEvent {@link Event} object that the rules are being evaluated against
	 *
	 * @return a {@code List} of {@link Event} objects to publish as a result of the rule evaluations.
	 */
	protected List<Event> evaluateRules(final Event triggerEvent) {
		synchronized (rulesOperationMutex) {
			final List<Event> expandedConsequenceEvents = new ArrayList<Event>();
			// retrieve the current chained dispatch count for the trigger event before running through all rules
			int dispatchCount = removeCurrentChainedDispatchCount(triggerEvent.getUniqueIdentifier());

			for (final ConcurrentLinkedQueue<Rule> ruleList : moduleRuleAssociation.values()) {
				for (final Rule rule : ruleList) {
					List<Event> consequences = evaluateRuleForEvent(triggerEvent, rule, dispatchCount);
					expandedConsequenceEvents.addAll(consequences);
				}
			}

			return expandedConsequenceEvents;
		}

	}

	/**
	 * Evaluates provided rules against the given triggerEvent
	 *
	 * @param triggerEvent {@link Event} object that the rules are being evaluated against
	 * @param rules {@link Rule} object used to evaluate events
	 * @return a {@code List} of {@link Event} objects to publish as a result of the rule evaluations.
	 */
	protected List<Event> evaluateEventWithRules(final Event triggerEvent, final List<Rule> rules) {
		final List<Event> expandedConsequenceEvents = new ArrayList<Event>();

		synchronized (rulesOperationMutex) {
			// retrieve the current chained dispatch count for the trigger event before running through all rules
			int dispatchCount = removeCurrentChainedDispatchCount(triggerEvent.getUniqueIdentifier());

			for (final Rule rule : rules) {
				List<Event> consequences = evaluateRuleForEvent(triggerEvent, rule, dispatchCount);
				expandedConsequenceEvents.addAll(consequences);
			}
		}

		return expandedConsequenceEvents;
	}

	/**
	 * Evaluates the given {@code rule} against the supplied {@code triggerEvent}.
	 *
	 * @param triggerEvent The {@link Event} against which to evaluate the rule
	 * @param rule The {@link Rule} to be evaluated
	 * @param triggerEventDispatchCount The current number of chained dispatch consequences for the trigger event
	 *
	 * @return The list of consequences that will be dispatched if the rule evaluates to true. These consequences are already token expanded.
	 */
	protected List<Event> evaluateRuleForEvent(final Event triggerEvent, final Rule rule,
			final int triggerEventDispatchCount) {
		List<Event> expandedConsequenceEvents = new ArrayList<Event>();
		Log.trace(LOG_PREFIX, "Evaluating rule: %s for event number: %s", rule.toString(), triggerEvent.getEventNumber());

		if (!rule.evaluateCondition(ruleTokenParser, triggerEvent)) {
			return expandedConsequenceEvents;
		}

		// condition matched
		for (final Event consequenceEvent : rule.getConsequenceEvents()) {
			final EventData expandedEventData = getTokenExpandedEventData(consequenceEvent.getData(), triggerEvent);

			if (expandedEventData == null) {
				Log.debug(LOG_PREFIX, "Unable to process a RuleConsequence Event, unable to expand event data.");
				continue;
			}

			// AMSDK-8481
			// process attach data consequences in the rules engine
			final Map<String, Variant> consequenceMap = expandedEventData.optVariantMap(
						RulesEngineConstants.EventDataKeys.CONSEQUENCE_TRIGGERED, null);

			if (consequenceMap == null || consequenceMap.isEmpty()) {
				Log.debug(LOG_PREFIX, "Unable to process a RuleConsequence Event, 'triggeredconsequence' not found in payload.");
				continue;
			}

			if (!consequenceMap.containsKey(RulesEngineConstants.EventDataKeys.CONSEQUENCE_JSON_TYPE)) {
				Log.debug(LOG_PREFIX, "Unable to process a RuleConsequence Event, no 'type' has been specified.");
				continue;
			}

			final String consequenceType = consequenceMap.get(
											   RulesEngineConstants.EventDataKeys.CONSEQUENCE_JSON_TYPE).optString(null);

			if (StringUtils.isNullOrEmpty(consequenceType)) {
				Log.debug(LOG_PREFIX, "Unable to process a RuleConsequence Event, no 'type' has been specified.");
				continue;
			}

			if (RulesEngineConstants.ConsequenceType.ATTACH.equals(consequenceType)) {
				processAttachDataConsequence(consequenceMap, triggerEvent);
			} else if (RulesEngineConstants.ConsequenceType.MODIFY.equals(consequenceType)) {
				processModifyDataConsequence(consequenceMap, triggerEvent);
			} else if (RulesEngineConstants.ConsequenceType.DISPATCH.equals(consequenceType)) {
				Event resultingEvent = processDispatchConsequence(consequenceMap, triggerEvent, triggerEventDispatchCount);

				if (resultingEvent != null) {
					expandedConsequenceEvents.add(resultingEvent);
				}
			} else {
				final Event.Builder outputEventBuilder = new Event.Builder(consequenceEvent.getName(), consequenceEvent.getEventType(),
						consequenceEvent.getEventSource()).setData(expandedEventData);

				expandedConsequenceEvents.add(outputEventBuilder.build());
			}
		}

		return expandedConsequenceEvents;
	}

	protected void processModifyDataConsequence(final Map<String, Variant> consequenceMap, final Event triggeringEvent) {
		if (triggeringEvent == null) {
			return;
		}

		final Map<String, Variant> consequenceDetails = getConsequenceDetails(consequenceMap,
				RulesEngineConstants.ConsequenceType.MODIFY);

		if (consequenceDetails == null) {
			return;
		}

		if (!consequenceDetails.containsKey(RulesEngineConstants.EventDataKeys.CONSEQUENCE_DETAIL_EVENT_DATA)) {
			Log.debug(LOG_PREFIX, "Unable to process a ModifyDataConsequence Event, 'eventData' is missing from 'details'.");
			return;
		}

		final Map<String, Variant> newEventDataMap = consequenceDetails.get(
					RulesEngineConstants.EventDataKeys.CONSEQUENCE_DETAIL_EVENT_DATA).optVariantMap(null);
		final EventData newEventData = new EventData(newEventDataMap);

		// log the change and merge the data
		Log.debug(LOG_PREFIX, "Modifying EventData on Event #%d with type '%s' and source '%s'.",
				  triggeringEvent.getEventNumber(),
				  triggeringEvent.getEventType().getName(), triggeringEvent.getEventSource().getName());

		Log.debug(LOG_PREFIX, "Original EventData for Event #%d: %s", triggeringEvent.getEventNumber(),
				  triggeringEvent.getData().toString());

		triggeringEvent.getData().overwrite(newEventData);

		Log.debug(LOG_PREFIX, "New EventData for Event #%d: %s", triggeringEvent.getEventNumber(),
				  triggeringEvent.getData().toString());
	}

	protected void processAttachDataConsequence(final Map<String, Variant> consequenceMap, final Event triggeringEvent) {
		if (triggeringEvent == null) {
			return;
		}

		final Map<String, Variant> consequenceDetails = getConsequenceDetails(consequenceMap,
				RulesEngineConstants.ConsequenceType.ATTACH);

		if (consequenceDetails == null) {
			return;
		}

		if (!consequenceDetails.containsKey(RulesEngineConstants.EventDataKeys.CONSEQUENCE_DETAIL_EVENT_DATA)) {
			Log.debug(LOG_PREFIX, "Unable to process an AttachDataConsequence Event, 'eventData' is missing from 'details'.");
			return;
		}

		final Map<String, Variant> newEventDataMap = consequenceDetails.get(
					RulesEngineConstants.EventDataKeys.CONSEQUENCE_DETAIL_EVENT_DATA).optVariantMap(null);
		final EventData newEventData = new EventData(newEventDataMap);

		// log the change and merge the data
		Log.debug(LOG_PREFIX, "Adding EventData to Event #%d with type '%s' and source '%s'.", triggeringEvent.getEventNumber(),
				  triggeringEvent.getEventType().getName(), triggeringEvent.getEventSource().getName());

		Log.debug(LOG_PREFIX, "Original EventData for Event #%d: %s", triggeringEvent.getEventNumber(),
				  triggeringEvent.getData().toString());

		triggeringEvent.getData().merge(newEventData);

		Log.debug(LOG_PREFIX, "New EventData for Event #%d: %s", triggeringEvent.getEventNumber(),
				  triggeringEvent.getData().toString());
	}

	/**
	 * Processes the {@link RulesEngineConstants.ConsequenceType#DISPATCH} consequence and creates a new event with the
	 * type and source specified in the consequence, containing the event data from the {@code triggeringEvent}.
	 * This method updates the {@code dispatchChainedEvents} Map with the correct dispatch chained events count
	 * for the newly dispatched event.
	 *
	 * @param consequence consequence payload
	 * @param triggeringEvent the event that triggered this consequence
	 * @param triggerEventDispatchCount The current number of chained dispatch consequences for the trigger event
	 * @return the resulting event to be dispatched or null if the processing failed or the chained dispatch consequences
	 * 		are over {@link RulesEngineConstants#MAX_CHAINED_CONSEQUENCE_COUNT}
	 * @see #removeCurrentChainedDispatchCount
	 */
	protected Event processDispatchConsequence(final Map<String, Variant> consequence, final Event triggeringEvent,
			final int triggerEventDispatchCount) {
		if (triggeringEvent == null) {
			return null;
		}

		if (triggerEventDispatchCount >= RulesEngineConstants.MAX_CHAINED_CONSEQUENCE_COUNT) {
			Log.trace(LOG_PREFIX,
					  "Unable to process %s consequence, max chained limit of (%d) met for this event uuid (%s).",
					  RulesEngineConstants.ConsequenceType.DISPATCH, RulesEngineConstants.MAX_CHAINED_CONSEQUENCE_COUNT,
					  triggeringEvent.getUniqueIdentifier());
			return null;
		}

		final Map<String, Variant> consequenceDetails = getConsequenceDetails(consequence,
				RulesEngineConstants.ConsequenceType.DISPATCH);

		if (consequenceDetails == null) {
			return null;
		}

		// verify that the required keys for this consequence exist
		final String newEventType = getValueFromConsequenceDetails(consequenceDetails,
									RulesEngineConstants.EventDataKeys.CONSEQUENCE_DETAIL_TYPE,
									RulesEngineConstants.ConsequenceType.DISPATCH);
		final String newEventSource = getValueFromConsequenceDetails(consequenceDetails,
									  RulesEngineConstants.EventDataKeys.CONSEQUENCE_DETAIL_SOURCE,
									  RulesEngineConstants.ConsequenceType.DISPATCH);
		final String eventDataAction = getValueFromConsequenceDetails(consequenceDetails,
									   RulesEngineConstants.EventDataKeys.CONSEQUENCE_DETAIL_EVENT_DATA_ACTION,
									   RulesEngineConstants.ConsequenceType.DISPATCH);

		if (StringUtils.isNullOrEmpty(newEventType) || StringUtils.isNullOrEmpty(newEventSource)
				|| StringUtils.isNullOrEmpty(eventDataAction)) {
			return null;
		}

		Event resultingEvent;

		if (RulesEngineConstants.EventDataKeys.CONSEQUENCE_DETAIL_ACTION_COPY.equals(eventDataAction)) {
			resultingEvent =  new Event.Builder(RulesEngineConstants.DISPATCH_CONSEQUENCE_EVENT_NAME, newEventType,
												newEventSource).setData(triggeringEvent.getData()).build();
		} else if (RulesEngineConstants.EventDataKeys.CONSEQUENCE_DETAIL_ACTION_NEW.equals(eventDataAction)) {
			Map<String, Variant> newEventDataMap = null;

			if (consequenceDetails.containsKey(RulesEngineConstants.EventDataKeys.CONSEQUENCE_DETAIL_EVENT_DATA)) {
				newEventDataMap = consequenceDetails.get(
									  RulesEngineConstants.EventDataKeys.CONSEQUENCE_DETAIL_EVENT_DATA).optVariantMap(null);
			}

			if (newEventDataMap != null) {
				final EventData newEventData = new EventData(newEventDataMap);
				resultingEvent = new Event.Builder(RulesEngineConstants.DISPATCH_CONSEQUENCE_EVENT_NAME, newEventType,
												   newEventSource).setData(newEventData).build();
			} else {
				resultingEvent = new Event.Builder(RulesEngineConstants.DISPATCH_CONSEQUENCE_EVENT_NAME, newEventType,
												   newEventSource).build();
			}
		} else {
			Log.debug(LOG_PREFIX,
					  "Unable to process the %s consequence, unsupported (%s) 'eventdataaction', expected values are copy/new.",
					  RulesEngineConstants.ConsequenceType.DISPATCH, eventDataAction);
			return null;
		}

		if (resultingEvent != null) {
			dispatchChainedEvents.put(resultingEvent.getUniqueIdentifier(), triggerEventDispatchCount + 1);
		}

		return resultingEvent;
	}

	//TODO: too much code here, we should be able to clean this up into a simpler function with less repetitive code.

	/**
	 * Expands tokens in the given {@link EventData} object
	 *
	 * @param eventData pre-token-expansion {@link EventData} object to expand
	 * @param triggerEvent {@link Event} object that triggered the rule that owns this consequence event
	 *
	 * @return {@link EventData} Token-Expanded version of the input eventdata object
	 */
	@SuppressWarnings("unchecked")
	protected EventData getTokenExpandedEventData(final EventData eventData, final Event triggerEvent) {
		if (eventData == null) {
			return null;
		}

		final EventData expandedData = new EventData();

		for (final String key : eventData.keys()) {
			Object value = eventData.getObject(key);

			if (value instanceof Map) {
				expandedData.putObject(key, getTokenExpandedMap((Map<String, Object>) value, triggerEvent));
			} else if (value instanceof List) {
				expandedData.putObject(key, getTokenExpandedList((List<Object>) value, triggerEvent));
			} else if (value instanceof String) {
				expandedData.putObject(key, ruleTokenParser.expandTokensForString((String)value, triggerEvent));
			} else {
				expandedData.putObject(key, value);
			}
		}

		return expandedData;
	}

	/**
	 * Returns the original {@code Map} with tokens (if any) expanded with the appropriate values.
	 *
	 * <p>
	 * If the {@code Map} contains native {@link Collections} like a Map or a List then this function will recursively expand tokens within them.
	 * If the {@code Map} contains any other Object apart from primitive data type containers, or collections, then it will be returned as is.
	 *
	 * @param mapWithTokens The {@link Map} with more zero or more tokens
	 * @param event The {@link Event} that will be used to expand tokens
	 * @return A {@code Map} with all the tokens expanded
	 */
	@SuppressWarnings("unchecked")
	protected Map<String, Object> getTokenExpandedMap(final Map<String, Object> mapWithTokens, final Event event) {
		if (mapWithTokens == null || mapWithTokens.isEmpty()) {
			return mapWithTokens;
		}

		Map<String, Object> expandedMap = new HashMap<String, Object>();

		Set<Map.Entry<String, Object>> entries = mapWithTokens.entrySet();

		for (Map.Entry<String, Object> entry : entries) {
			Object value = entry.getValue();

			if (value instanceof Map) {
				expandedMap.put(entry.getKey(), getTokenExpandedMap((Map<String, Object>) value, event));
			} else if (value instanceof List) {
				expandedMap.put(entry.getKey(), getTokenExpandedList((List<Object>) value, event));
			} else if (value instanceof String) {
				expandedMap.put(entry.getKey(), ruleTokenParser.expandTokensForString((String)value, event));
			} else {
				expandedMap.put(entry.getKey(), entry.getValue());
			}

		}

		return expandedMap;

	}

	/**
	 * Returns the original list with tokens (if any) expanded with the appropriate values.
	 *
	 * <p>
	 * If the {@code List} contains native {@link Collections} like a Map or a List then this function will recursively expand tokens within them.
	 * If the {@code List} contains any other Object apart from primitive data type containers, or collections, then it will be returned as is.
	 *
	 * @param listWithTokens The {@link List} with more zero or more tokens
	 * @param event The {@link Event} that will be used to expand tokens
	 * @return A {@code List} with all the tokens expanded
	 */
	@SuppressWarnings("unchecked")
	protected List<Object> getTokenExpandedList(final List<Object> listWithTokens, final Event event) {
		if (listWithTokens == null || listWithTokens.isEmpty()) {
			return listWithTokens;
		}

		List<Object> expandedList = new ArrayList<Object>();

		for (final Object value : listWithTokens) {
			if (value instanceof Map) {
				expandedList.add(getTokenExpandedMap((Map<String, Object>) value, event));
			} else if (value instanceof List) {
				expandedList.add(getTokenExpandedList((List<Object>) value, event));
			} else if (value instanceof String) {
				expandedList.add(ruleTokenParser.expandTokensForString((String)value, event));
			} else {
				expandedList.add(value);
			}
		}

		return expandedList;
	}

	/**
	 * Used for testing
	 * @return unmodifiable copy of current {@code dispatchChainedEvents}
	 * @throws {@link UnsupportedOperationException} if any mutations are performed on the returned Map
	 */
	protected Map<String, Integer> getDispatchChainedEvents() {
		return Collections.unmodifiableMap(dispatchChainedEvents);
	}

	/**
	 * Helper to extract the consequence details payload.
	 *
	 * @param consequence consequence payload
	 * @param consequenceType the consequence type to be used for logging
	 * @return the details payload or null if an error occurred
	 */
	private Map<String, Variant> getConsequenceDetails(final Map<String, Variant> consequence,
			final String consequenceType) {
		if (consequence == null || consequence.isEmpty()) {
			return null;
		}

		if (!consequence.containsKey(RulesEngineConstants.EventDataKeys.CONSEQUENCE_DETAIL)) {
			Log.debug(LOG_PREFIX, String.format("Unexpected (%s) consequence format, 'details' object is missing.",
												consequenceType));
			return null;
		}

		final Map<String, Variant> consequenceDetails = consequence.get(
					RulesEngineConstants.EventDataKeys.CONSEQUENCE_DETAIL).optVariantMap(null);

		if (consequenceDetails == null || consequenceDetails.isEmpty()) {
			Log.debug(LOG_PREFIX, String.format("Unexpected (%s) consequence format, 'details' is null/empty.",
												consequenceType));
			return null;
		}

		return consequenceDetails;
	}

	/**
	 * Helper to extract a required {@link String} value for the specified {@code key}.
	 *
	 * @param consequenceDetails consequence details content
	 * @param key the expected {@link String} key in the consequence details
	 * @param consequenceType the consequence type to be used for logging
	 * @return the value for that key or null if an error occurred
	 */
	private String getValueFromConsequenceDetails(final Map<String, Variant> consequenceDetails, final String key,
			final String consequenceType) {
		if (consequenceDetails == null || StringUtils.isNullOrEmpty(key)) {
			return null;
		}

		if (!consequenceDetails.containsKey(key)) {
			Log.debug(LOG_PREFIX,
					  "Unexpected (%s) consequence format, required key (%s) is missing from 'details'",
					  consequenceType, key);
			return null;
		}

		final String value = consequenceDetails.get(key).optString(null);

		if (StringUtils.isNullOrEmpty(value)) {
			Log.debug(LOG_PREFIX,
					  "Unexpected (%s) consequence format, required key (%s) has null/empty value in 'details'.",
					  consequenceType, key);
			return null;
		}

		return value;
	}

	/**
	 * Retrieves current count of chained dispatch events for the provided {@code eventUniqueId} and removes it
	 * from the {@code dispatchChainedEvents}.
	 *
	 * @param eventUniqueId event unique identifier
	 * @return current count if a mapping exists in {@code dispatchChainedEvents} or 0 otherwise
	 */
	private int removeCurrentChainedDispatchCount(final String eventUniqueId) {
		Integer dispatchCount = dispatchChainedEvents.remove(eventUniqueId);
		return dispatchCount != null ? dispatchCount : 0;
	}
}
