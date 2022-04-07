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
 * Base class for a {@code Rule}'s set of conditions used to evaluate a rule.
 */
abstract class RuleCondition {
	private static final String RULE_CONDITION_TYPE_KEY_JSON     = "type";
	private static final String RULE_CONDITION_TYPE_GROUP_JSON   = "group";
	private static final String RULE_CONDITION_TYPE_MATCHER_JSON = "matcher";
	private static final String RULE_CONDITION_TYPE_HISTORICAL_JSON = "historical";

	private static final String RULE_CONDITION_DEFINITION_KEY_JSON = "definition";

	/**
	 * Evaluate the condition and return true if the condition holds, with the data supplied in triggering event.
	 *
	 * @param ruleTokenParser {@link RuleTokenParser} instance to process keys in rule condition
	 * @param event triggering {@link Event} instance
	 * @return true if the condition holds
	 */
	protected abstract boolean evaluate(final RuleTokenParser ruleTokenParser, final Event event);

	@Override
	public abstract String toString();

	/**
	 * Instantiate a Rule condition class. The condition types supported are "group" (Condition Group) and "matcher" (Condition Matcher).
	 *
	 * <p>
	 *
	 * Required keys are <br>
	 *     <ul>
	 *         <li>
	 *             {@code type} Types supported are "group" (Condition Group) and "matcher" (Condition Matcher).
	 *         </li>
	 *         <li>
	 *             {@code definition} Defines the group or matcher conditions
	 *         </li>
	 *     </ul>
	 *
	 * @param conditionJson The json representing the rule condition.
	 * @return A {@link RuleCondition} instance
	 *
	 * @throws JsonException if the json format is not supported.
	 * @throws UnsupportedConditionException If the condition json contains unsupported keys or type values
	 */
	protected static RuleCondition ruleConditionFromJson(final JsonUtilityService.JSONObject conditionJson) throws
		JsonException, UnsupportedConditionException {
		if (conditionJson == null || conditionJson.length() == 0) {
			return null;
		}

		RuleCondition ruleCondition = null;

		if (conditionJson.getString(RULE_CONDITION_TYPE_KEY_JSON).equals(RULE_CONDITION_TYPE_GROUP_JSON)) {
			ruleCondition = RuleConditionGroup.ruleConditionGroupFromJson(conditionJson.getJSONObject(
								RULE_CONDITION_DEFINITION_KEY_JSON));
		} else if (conditionJson.getString(RULE_CONDITION_TYPE_KEY_JSON).equals(RULE_CONDITION_TYPE_MATCHER_JSON)) {
			ruleCondition = RuleConditionMatcher.ruleConditionMatcherFromJson(conditionJson.getJSONObject(
								RULE_CONDITION_DEFINITION_KEY_JSON));
		}

		if (ruleCondition == null) {
			throw new UnsupportedConditionException("Could not create a condition instance!");
		}

		return ruleCondition;
	}
}
