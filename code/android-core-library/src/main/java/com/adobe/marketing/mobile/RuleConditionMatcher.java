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

/**
 * Rule condition matcher class which evaluates a specific rule condition.
 */
class RuleConditionMatcher extends RuleCondition {
	Matcher matcher;

	/**
	 * Constructs a RuleConditionMatcher instance.
	 * @param matcher the condition matcher
	 */
	RuleConditionMatcher(final Matcher matcher) {
		this.matcher = matcher;
	}

	/**
	 * Evaluate the data in current {@code Event} object against this condition matcher.
	 *
	 * @param ruleTokenParser {@link RuleTokenParser} instance to process keys in rule condition
	 * @param event triggering {@link Event} instance
	 *
	 * @return true if {@code Event} data validates against the matcher
	 */
	@Override
	protected boolean evaluate(final RuleTokenParser ruleTokenParser, final Event event) {
		if (ruleTokenParser == null || event == null || matcher == null) {
			return false;
		}

		String value = ruleTokenParser.expandKey(matcher.key, event);
		return matcher.matches(value);
	}

	/**
	 * Create a new RuleConditionMatcher instance from a json file.
	 *
	 * <p>
	 *     The required fields are <br>
	 *         <ul>
	 *             <li>
	 *                 {@code matcher} One of the supported matchers (example "ex", "eq")
	 *             </li>
	 *             <li>
	 *                 {@code key} The key to be matched
	 *             </li>
	 *         </ul>
	 *
	 * <p>
	 *     The {@code value} json key is not required if the matcher type is "ex" or "nx". Otherwise it is required.
	 * </p>
	 * @param ruleConditionMatcherJson the {@link JsonUtilityService.JSONObject} containing the definition of a
	 * {@code RuleConditionMatcher}
	 * @return {@link RuleConditionMatcher} based on {@code ruleConditionMatcherJson}
	 *
	 * @throws UnsupportedConditionException If the JSON was could not be parsed
	 */
	protected static RuleConditionMatcher ruleConditionMatcherFromJson(
		final JsonUtilityService.JSONObject ruleConditionMatcherJson) throws UnsupportedConditionException {

		if (ruleConditionMatcherJson == null || ruleConditionMatcherJson.length() == 0) {
			return null;
		}

		Matcher matcher = Matcher.matcherWithJsonObject(ruleConditionMatcherJson);

		if (matcher == null) {
			throw new UnsupportedConditionException("Could not create instance of a matcher!");
		}

		return new RuleConditionMatcher(matcher);
	}

	@Override
	public String toString() {
		if (matcher == null) {
			return "";
		}

		return matcher.toString();
	}

}
