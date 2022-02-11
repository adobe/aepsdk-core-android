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

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.*;

/**
 * A rule condition which represents historical events used to evaluate a rule.
 */
class RuleConditionHistorical extends RuleCondition {
	private static final String LOG_TAG = "RuleConditionHistorical";
	private static final int TIMEOUT = 1000;
	private static final int INVALID_VALUE = -1;
	private Matcher matcher;
	private EventHistoryRequest[] eventHistoryRequests;
	private String searchType;
	private int value;
	private long from;
	private long to;

	/**
	 * Evaluates if the stored {@link EventHistory} requests exist in the {@link EventHistoryDatabase}.
	 *
	 * @param ruleTokenParser {@link RuleTokenParser} instance. This is unused for {@link RuleConditionHistorical} objects.
	 * @param event triggering {@link Event} instance. This is unused for {@code RuleConditionHistorical} objects.
	 * @return true if the condition holds
	 */
	@Override
	protected boolean evaluate(final RuleTokenParser ruleTokenParser, final Event event) {
		if (eventHistoryRequests == null || eventHistoryRequests.length == 0) {
			Log.trace(LOG_TAG, "No event history requests found in the RuleConditionHistorical object.");
			return false;
		}

		final boolean isOrdered = !this.searchType.equals(RulesEngineConstants.EventHistory.ANY);
		final int[] eventHistoryResult = new int[1];
		final CountDownLatch latch = new CountDownLatch(1);
		final EventHistoryResultHandler<Integer> handler = new EventHistoryResultHandler<Integer>() {
			@Override
			public void call(final Integer results) {
				eventHistoryResult[0] = results;
				latch.countDown();
			}
		};
		final EventHistory eventHistory = EventHistoryProvider.getEventHistory();

		if (eventHistory == null) {
			Log.warning(LOG_TAG, "Unable to retrieve historical events, the event history is not available.");
			return false;
		}

		eventHistory.getEvents(eventHistoryRequests, isOrdered, handler);

		try {
			latch.await(TIMEOUT, TimeUnit.MILLISECONDS);
		} catch (final InterruptedException interruptedException) {
			Log.warning(LOG_TAG, "Interrupted Exception occurred while waiting for the latch: %s.",
						interruptedException.getMessage());
			return false;
		}

		return matcher.matches(eventHistoryResult[0]);
	}

	/**
	 * Creates a {@link RuleConditionHistorical} instance based on the given historical {@code JSONObject}.
	 * Searches the JSON object for a searchType, matcher type, value, "from" timestamp, and "to" timestamp and returns a {@code RuleConditionHistorical} instance
	 * populated with those values.
	 * <p>
	 * Returns null if an error occurs creating the {@code RuleConditionHistorical} instance.
	 *
	 * @param historicalConditionJson {@link JsonUtilityService.JSONObject} containing the definition for a {@code RuleConditionHistorical} instance
	 * @return the created {@code RuleConditionHistorical} object
	 */
	static RuleConditionHistorical historicalConditionFromJsonObject(final JsonUtilityService.JSONObject
			historicalConditionJson) {
		if (historicalConditionJson == null || historicalConditionJson.length() == 0) {
			Log.trace(LOG_TAG,
					  "error creating historical rule condition from the Json object as the definition was empty.");
			return null;
		}

		RuleConditionHistorical ruleConditionHistorical = retrieveHistoryValuesFromJson(historicalConditionJson);

		if (ruleConditionHistorical == null) {
			Log.trace(LOG_TAG,
					  "error creating historical rule condition from the Json object as a required value was missing.");
			return ruleConditionHistorical;
		}

		// set the matcher type and matcher value
		ruleConditionHistorical.matcher = Matcher.matcherWithJsonObject(historicalConditionJson);
		ruleConditionHistorical.matcher.values.add(ruleConditionHistorical.value);

		try {
			// create EventHistoryRequest objects and add them to the EventHistoryRequest array
			ruleConditionHistorical.eventHistoryRequests = parseEventHistoryRequestsFromJson(historicalConditionJson,
					ruleConditionHistorical);
		} catch (final JsonException exception) {
			Log.trace(LOG_TAG, "error creating historical rule condition from the Json object: %s", exception.getMessage());
			return null;
		}

		return ruleConditionHistorical;
	}

	/**
	 * Searches the JSON object for {@link EventHistory} data and adds it to the created {@link RuleConditionHistorical} object.
	 *
	 * @param definition {@link JsonUtilityService.JSONObject} containing the search type
	 */
	private static RuleConditionHistorical retrieveHistoryValuesFromJson(final JsonUtilityService.JSONObject
			definition) {
		RuleConditionHistorical ruleConditionHistorical = new RuleConditionHistorical();
		final String searchType = definition.optString(RulesEngineConstants.EventHistory.RuleDefinition.SEARCH_TYPE, "");
		final String matcherType = definition.optString(RulesEngineConstants.EventHistory.RuleDefinition.MATCHER, "");
		final int value = definition.optInt(RulesEngineConstants.EventHistory.RuleDefinition.VALUE, INVALID_VALUE);
		final long from = definition.optLong(RulesEngineConstants.EventHistory.RuleDefinition.FROM, 0l);
		final long to = definition.optLong(RulesEngineConstants.EventHistory.RuleDefinition.TO, System.currentTimeMillis());

		// historical search to be performed. values are "any" or "ordered" with "any" being the default value.
		if (!StringUtils.isNullOrEmpty(searchType)) {
			ruleConditionHistorical.searchType = searchType;
		} else {
			ruleConditionHistorical.searchType = "any";
			Log.trace(LOG_TAG, "%s (searchType), messages - setting searchType to any", Log.UNEXPECTED_EMPTY_VALUE);
		}

		// matcher type for evaluating the historical search result. this is required and the historical condition creation will fail if this is not present.
		if (StringUtils.isNullOrEmpty(matcherType)) {
			Log.trace(LOG_TAG, "%s (matcherType), messages - error creating historical condition", Log.UNEXPECTED_EMPTY_VALUE);
			return null;
		}

		// the value to evaluate the search result against. this is required and the historical condition creation will fail if this is not present.
		if (value > INVALID_VALUE) {
			ruleConditionHistorical.value = value;
		} else {
			Log.trace(LOG_TAG, "%s (value), messages - error creating historical condition", Log.UNEXPECTED_EMPTY_VALUE);
			return null;
		}

		// the "from" value is the beginning timestamp to start the search from and the "to" value is the end timestamp.
		// "0" is used if no "from" timestamp is provided and the current timestamp is used if no "to" value is provided.
		ruleConditionHistorical.from = from;
		ruleConditionHistorical.to = to;

		return ruleConditionHistorical;
	}

	/**
	 * Searches the JSON object for events and converts them to an array of {@link EventHistoryRequest}.
	 *
	 * @param definition {@link JsonUtilityService.JSONObject} containing an array of {@link EventHistory} masks
	 * @param ruleConditionHistorical {@link RuleConditionHistorical} object to be populated with the created {@code EventHistoryRequest}s
	 * @return an array of {@code EventHistoryRequest}s created from events contained in the rule definition
	 *
	 * @throws JsonException if no {@code EventHistory} masks are found in the provided JSON object
	 */
	private static EventHistoryRequest[] parseEventHistoryRequestsFromJson(final JsonUtilityService.JSONObject definition,
			final RuleConditionHistorical ruleConditionHistorical) throws JsonException {
		EventHistoryRequest requests[] = null;
		// loop through json array and populate the eventHistoryRequests list
		JsonUtilityService.JSONArray jsonArray = definition.getJSONArray(
					RulesEngineConstants.EventHistory.RuleDefinition.EVENTS);

		if (jsonArray == null || jsonArray.length() == 0) {
			Log.debug(LOG_TAG, "%s - error creating historical rule condition as the rule definition did not contain any events.",
					  Log.UNEXPECTED_EMPTY_VALUE);
			return null;
		}

		final int arrayLength = jsonArray.length();
		requests = new EventHistoryRequest[arrayLength];

		for (int i = 0; i < arrayLength; i++) {
			final JsonUtilityService.JSONObject jsonObject = (JsonUtilityService.JSONObject) jsonArray.get(i);
			final Iterator iterator = jsonObject.keys();
			final HashMap<String, Variant> mask = new HashMap<String, Variant>();

			while (iterator.hasNext()) {
				final String key = (String) iterator.next();
				mask.put(key, Variant.fromString(jsonObject.getString(key)));
			}

			requests[i] = new EventHistoryRequest(mask, ruleConditionHistorical.from,
												  ruleConditionHistorical.to);
		}

		return requests;
	}

	@Override public String toString() {
		final StringBuilder maskStringBuilder = new StringBuilder();
		maskStringBuilder.append("(HISTORICAL EVENTS FOUND: ");

		for (final EventHistoryRequest request : eventHistoryRequests) {
			maskStringBuilder.append(request.mask);
			maskStringBuilder.append(", ");
		}

		maskStringBuilder.setLength(maskStringBuilder.length() - 2);
		maskStringBuilder.append(")");
		return maskStringBuilder.toString();
	}

	// for unit tests
	public EventHistoryRequest[] getEventHistoryRequests() {
		return eventHistoryRequests;
	}
}
