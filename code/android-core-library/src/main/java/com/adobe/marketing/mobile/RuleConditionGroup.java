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
import java.util.List;

/**
 * Base class for a group of {@code RuleConditions}.
 */
abstract class RuleConditionGroup extends RuleCondition {

	private static final String RULE_CONDITIONS_JSON_KEY                 = "conditions";
	private static final String RULE_CONDITION_JSON_DEFINITION_LOGIC     = "logic";
	private static final String RULE_CONDITION_JSON_DEFINITION_LOGIC_AND = "and";
	private static final String RULE_CONDITION_JSON_DEFINITION_LOGIC_OR  = "or";

	List<RuleCondition> conditions;

	/**
	 * Creates a new {@code RuleConditionGroup} from the given {@code ruleConditionDefinitionJson} object.
	 *
	 * <p>
	 * The required keys are <br>
	 *     <ul>
	 *         <li>
	 *             {@code logic} The logic operator. Currently supported are "or" and "and"
	 *         </li>
	 *         <li>
	 *             {@code conditions} The list of conditions part of the group
	 *         </li>
	 *     </ul>
	 *
	 * @param ruleConditionDefinitionJson {@link JsonUtilityService.JSONObject} which defines the structure of a
	 * {@code RuleConditionGroup}
	 * @return new instance of a {@code RuleConditionGroup}, or null if the JSON definition is not valid

	 * @throws JsonException if the json format is not supported.
	 * @throws UnsupportedConditionException If {@code ruleConditionDefinitionJson} does not contain a valid logic operator, or the json contains unsupported keys, or
	 * the conditions in the group are themselves invalid
	 **/
	protected static RuleConditionGroup ruleConditionGroupFromJson(
		final JsonUtilityService.JSONObject ruleConditionDefinitionJson) throws JsonException, UnsupportedConditionException {

		if (ruleConditionDefinitionJson == null) {
			return null;
		}

		String logicalOperator = ruleConditionDefinitionJson.getString(RULE_CONDITION_JSON_DEFINITION_LOGIC);

		if (logicalOperator == null) {
			return null;
		}

		List<RuleCondition> conditions = new ArrayList<RuleCondition>();
		JsonUtilityService.JSONArray conditionsJson = ruleConditionDefinitionJson.getJSONArray(RULE_CONDITIONS_JSON_KEY);

		if (conditionsJson == null) {
			return null;
		}

		for (int conditionIndex = 0; conditionIndex < conditionsJson.length(); conditionIndex++) {
			JsonUtilityService.JSONObject conditionJson = conditionsJson.getJSONObject(conditionIndex);

			if (conditionJson == null) {
				continue;
			}

			RuleCondition ruleCondition = ruleConditionFromJson(conditionJson);
			conditions.add(ruleCondition);
		}

		RuleConditionGroup ruleConditionGroup = null;

		if (logicalOperator.equals(RULE_CONDITION_JSON_DEFINITION_LOGIC_AND)) {
			ruleConditionGroup = new RuleConditionAndGroup(conditions);
		} else if (logicalOperator.equals(RULE_CONDITION_JSON_DEFINITION_LOGIC_OR)) {
			ruleConditionGroup = new RuleConditionOrGroup(conditions);
		}

		if (ruleConditionGroup == null) {
			throw  new UnsupportedConditionException("Could not create an instance of a condition group!");
		}

		return ruleConditionGroup;
	}

}
