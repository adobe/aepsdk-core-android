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

import java.util.Collections;
import java.util.List;

class Rule {
	protected RuleCondition       condition;
	protected List<Event> 		consequenceEvents;

	/**
	 * Constructs a new Rule object with the given {@code RuleCondition} and list of consequence {@code Event} objects.
	 *
	 * @param condition a {@code RuleCondition} object defining the requirements for this rule
	 * @param ruleConsequenceEvents a list of consequence {@code Event} objects.
	 */
	Rule(final RuleCondition condition, final List<Event> ruleConsequenceEvents) throws IllegalArgumentException {
		if (condition == null) {
			throw new IllegalArgumentException("Cannot create rule with null condition");
		}

		if (ruleConsequenceEvents == null) {
			throw new IllegalArgumentException("Cannot create rule with null consequence events");
		}

		this.condition = condition;
		this.consequenceEvents = ruleConsequenceEvents;
	}

	/**
	 * Evaluates this {@code Rule} condition against the given {@code Event}.
	 *
	 * @param ruleTokenParser a {@code RuleTokenParser} instance
	 * @param event the {@code Event} to evaluate conditions against
	 *
	 * @return true if condition is satisfied, false otherwise.
	 */
	public boolean evaluateCondition(final RuleTokenParser ruleTokenParser, final Event event) {
		return condition.evaluate(ruleTokenParser, event);
	}

	/**
	 * Returns an unmodifiable list of {@code Event} objects to trigger
	 * @return An unmodifiable list. Will not be null.
	 */
	public List<Event> getConsequenceEvents() {
		return Collections.unmodifiableList(this.consequenceEvents);
	}

	@Override
	public String toString() {
		return "{\n\tCondition: " +
			   (condition == null ? "null" : condition.toString()) +
			   "\n\tConsequences: " +
			   (consequenceEvents == null ? "null" : consequenceEvents.toString()) +
			   "\n}";
	}

}
