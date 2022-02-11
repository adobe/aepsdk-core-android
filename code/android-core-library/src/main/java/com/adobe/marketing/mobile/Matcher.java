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
import java.util.HashMap;
import java.util.Map;

/**
 * Base Matcher class. Matcher classes evaluate given values to values mapped to a given key. Derived Matcher
 * classes specialize in the match operation used to evaluate the values.
 */
abstract class Matcher {
	// ================================================================================
	// protected members
	// ================================================================================
	protected String            key;
	protected ArrayList<Object> values = new ArrayList<Object>();

	// ================================================================================
	// private string constants
	// ================================================================================
	private  static final String LOG_TAG = Matcher.class.getSimpleName();

	private  static final String MATCHER_JSON_KEY = "key";
	private  static final String MATCHER_JSON_MATCHES = "matcher";
	private  static final String MATCHER_JSON_VALUES = "values";

	private  static final String MATCHER_STRING_EQUALS                 = "eq";
	private  static final String MATCHER_STRING_NOT_EQUALS             = "ne";
	private  static final String MATCHER_STRING_GREATER_THAN           = "gt";
	private  static final String MATCHER_STRING_GREATER_THAN_OR_EQUALS = "ge";
	private  static final String MATCHER_STRING_LESS_THAN              = "lt";
	private  static final String MATCHER_STRING_LESS_THAN_OR_EQUALS    = "le";
	private  static final String MATCHER_STRING_CONTAINS               = "co";
	private  static final String MATCHER_STRING_NOT_CONTAINS           = "nc";
	private  static final String MATCHER_STRING_STARTS_WITH            = "sw";
	private  static final String MATCHER_STRING_ENDS_WITH              = "ew";
	private  static final String MATCHER_STRING_EXISTS                 = "ex";
	private  static final String MATCHER_STRING_NOT_EXISTS             = "nx";

	// ================================================================================
	// convenience constructor
	// ================================================================================
	protected static final Map<String, Class> _matcherTypeDictionary = initializeMatcherTypeDictionary();

	/**
	 * Creates a mapping of all supported rule condition matcher types to their class objects.
	 *
	 * @return a mapping of rule condition matcher types to their class object
	 */
	private static Map<String, Class> initializeMatcherTypeDictionary() {
		HashMap<String, Class> matcherTypeDictionary = new HashMap<String, Class>();
		matcherTypeDictionary.put(MATCHER_STRING_EQUALS, MatcherEquals.class);
		matcherTypeDictionary.put(MATCHER_STRING_NOT_EQUALS, MatcherNotEquals.class);
		matcherTypeDictionary.put(MATCHER_STRING_GREATER_THAN, MatcherGreaterThan.class);
		matcherTypeDictionary.put(MATCHER_STRING_GREATER_THAN_OR_EQUALS, MatcherGreaterThanOrEqual.class);
		matcherTypeDictionary.put(MATCHER_STRING_LESS_THAN, MatcherLessThan.class);
		matcherTypeDictionary.put(MATCHER_STRING_LESS_THAN_OR_EQUALS, MatcherLessThanOrEqual.class);
		matcherTypeDictionary.put(MATCHER_STRING_CONTAINS, MatcherContains.class);
		matcherTypeDictionary.put(MATCHER_STRING_NOT_CONTAINS, MatcherNotContains.class);
		matcherTypeDictionary.put(MATCHER_STRING_STARTS_WITH, MatcherStartsWith.class);
		matcherTypeDictionary.put(MATCHER_STRING_ENDS_WITH, MatcherEndsWith.class);
		matcherTypeDictionary.put(MATCHER_STRING_EXISTS, MatcherExists.class);
		matcherTypeDictionary.put(MATCHER_STRING_NOT_EXISTS, MatcherNotExists.class);
		return matcherTypeDictionary;
	}

	/**
	 * Creates a Matcher instance based on the given {@code JSONObject}.
	 * Searches the JSON object for a matcher operator, key, and values and returns a Matcher instance
	 * populated with those values. Returns null if an error occurs creating the Matcher instance.
	 *
	 * @param dictionary {@link JsonUtilityService.JSONObject} containing the definition for a {@code Matcher} instance
	 * @return a new {@code Matcher} instance, or null if a {@code Matcher} could not be created
	 */
	public static Matcher matcherWithJsonObject(final JsonUtilityService.JSONObject dictionary) {
		Class matcherClass;
		Matcher matcher = null;
		final String matcherString = dictionary.optString(MATCHER_JSON_MATCHES, "");

		if (matcherString.length() <= 0) {
			Log.debug(LOG_TAG, "Messages - message matcher type is empty");
		}

		// get the correct class type and instantiate it
		matcherClass = _matcherTypeDictionary.get(matcherString);

		if (matcherClass == null) {
			matcherClass = MatcherUnknown.class;
			Log.debug(LOG_TAG, "Messages - message matcher type \"%s\" is invalid", matcherString);
		}

		try {
			matcher = (Matcher) matcherClass.newInstance();
		} catch (InstantiationException ex) {
			Log.error(LOG_TAG, "Messages - Error creating matcher (%s)", ex);
		} catch (IllegalAccessException ex) {
			Log.error(LOG_TAG, "Messages - Error creating matcher (%s)", ex);
		}

		if (matcher != null) {
			setMatcherKeyFromJson(dictionary, matcher);

			try {
				// if this is an exists matcher, we know we don't have anything in the values array
				if (matcher instanceof MatcherExists) {
					return matcher;
				}

				setMatcherValuesFromJson(dictionary, matcher);
			} catch (JsonException ex) {
				Log.warning(LOG_TAG, "Messages - error creating matcher, values is required (%s)", ex);
			}
		}

		return matcher;
	}

	/**
	 * Searches the JSON object for matcher values and adds them to the given Matcher object.
	 *
	 * @param dictionary {@link JsonUtilityService.JSONObject} containing a matcher values array
	 * @param matcher the {@link Matcher} instance to add the found values
	 * @throws JsonException if no matcher values are found in the provided JSON object
	 */
	static void setMatcherValuesFromJson(final JsonUtilityService.JSONObject dictionary, final Matcher matcher)
	throws JsonException {

		if (matcher == null) {
			return;
		}

		// loop through json array and put things in the values array
		JsonUtilityService.JSONArray jsonArray = dictionary.getJSONArray(MATCHER_JSON_VALUES);

		if (jsonArray == null) {
			return;
		}

		int arrayLength = jsonArray.length();


		for (int i = 0; i < arrayLength; i++) {
			matcher.values.add(jsonArray.get(i));
		}

		if (matcher.values.isEmpty()) {
			Log.debug(LOG_TAG, "%s (matcher values), messages - error creating matcher", Log.UNEXPECTED_EMPTY_VALUE);
		}
	}

	/**
	 * Searches the JSON object for a matcher key and adds it to the given Matcher object.
	 * If an error occurs parsing the JSON {@code dictionary}, an error is logged and the
	 * {@code matcher}'s key is not set.
	 *
	 * @param dictionary {@link JsonUtilityService.JSONObject} containing the matcher key
	 * @param matcher the {@link Matcher} instance to add the found key
	 */
	static void setMatcherKeyFromJson(final JsonUtilityService.JSONObject dictionary, final Matcher matcher) {
		if (matcher == null) {
			return;
		}

		String key = dictionary.optString(MATCHER_JSON_KEY, "");

		// we toLowercase the key so we can have case insensitive matchers
		if (key.length() > 0) {
			matcher.key = key;
		} else {
			Log.debug(LOG_TAG, "%s (key), messages - error creating matcher", Log.UNEXPECTED_EMPTY_VALUE);
		}
	}

	/**
	 * Evaluates the given {@code value} against this {@code Matcher}'s values.
	 * This base class's implementation always returns false and should be overwritten by the derived class
	 * if the desired behavior is different.
	 *
	 * @param value the value to match. may be null
	 * @return true if {@code value} matches against this {@code Matcher}'s values
	 */
	@SuppressWarnings("squid:S1172")//Unused param value will need to exist here
	protected boolean matches(final Object value) {
		return false;
	}

	/**
	 * Convenience method to parse a {@code Double} from the given {@code value} object.
	 * @param value an object to attempt to parse a {@code Double}
	 * @return a {@code Double} instance from the given {@code value}, or null if {@code value} could not
	 * be parsed to a {@code Double}
	 */
	protected Double tryParseDouble(final Object value) {
		try {
			return Double.valueOf(value.toString());
		} catch (Exception ex) {
			Log.trace(LOG_TAG, "Could not parse into a Double (%s)", ex);
			return null;
		}
	}

	@Override
	public abstract String toString();
}
