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

import java.util.List;
import java.util.Map;

/**
 * Concrete {@code RuleCondition} class for an AND group. If all conditions in this group evaluate
 * to true, then the entire group evaluates to true.
 */
class RuleConditionAndGroup extends RuleConditionGroup {

	/**
	 * Constructs a new {@code RuleConditionAndGroup}
	 * @param conditions the {@link RuleCondition}s in this group
	 */
	RuleConditionAndGroup(final List<RuleCondition> conditions) {
		this.conditions = conditions;
	}

	/**
	 * Evaluate the data in {@code Event} object against the conditions in this rule condition group.
	 *
	 * @param ruleTokenParser {@link RuleTokenParser} instance to process keys in rule condition
	 * @param event triggering {@link Event} instance
	 *
	 * @return true if all conditions in this group evaluate to true.
	 */
	@Override
	protected boolean evaluate(final RuleTokenParser ruleTokenParser, final Event event) {
		if (conditions == null || conditions.isEmpty()) {
			return false;
		}

		for (RuleCondition ruleCondition : conditions) {
			if (!ruleCondition.evaluate(ruleTokenParser, event)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public String toString() {
		if (conditions == null || conditions.isEmpty()) {
			return "";
		}

		StringBuilder andGroupString = new StringBuilder();
		andGroupString.append("(");
		StringBuilder orGroupConditions = new StringBuilder();

		for (RuleCondition ruleCondition : conditions) {
			if (orGroupConditions.length() > 0) {
				orGroupConditions.append(" AND ");
			}

			orGroupConditions.append(ruleCondition.toString());
		}

		andGroupString.append(orGroupConditions);
		andGroupString.append(")");
		return andGroupString.toString();
	}

}
