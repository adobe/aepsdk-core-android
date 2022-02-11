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

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RuleConditionAndGroupTests {

	private PlatformServices platformServices;
	private RuleTokenParser ruleTokenParser;

	@Before
	public void beforeEachTest() {
		platformServices = new FakePlatformServices();
		final EventHub testEventHub = new EventHub("eventhub", platformServices);
		ruleTokenParser = new RuleTokenParser(testEventHub);
	}

	@Test
	public void evaluate_Success_When_ValidTestDataUsed() throws Exception {
		//setup
		JsonUtilityService.JSONObject testRuleConditionGroupJson = platformServices.getJsonUtilityService()
				.createJSONObject("{\"logic\":\"and\",\"conditions\":[{\"type\":\"matcher\",\"definition\":{\"key\":\"key\",\"matcher\":\"gt\",\"values\":[0]}},{\"type\":\"matcher\",\"definition\":{\"key\":\"key1\",\"matcher\":\"eq\",\"values\":[\"value1\"]}}]}");
		RuleConditionGroup testRuleConditionAndGroup = RuleConditionGroup.ruleConditionGroupFromJson(
					testRuleConditionGroupJson);

		EventData testEventData = new EventData();
		testEventData.putString("key", "1");
		testEventData.putString("key1", "value1");
		Event testEvent = new Event.Builder("test", EventType.ANALYTICS,
											EventSource.RESPONSE_CONTENT).setData(testEventData).build();

		//test
		boolean result = testRuleConditionAndGroup.evaluate(ruleTokenParser, testEvent);
		//verify
		assertTrue(result);
	}

	@Test
	public void evaluate_Fail_When_EmptyTestDataUsed() throws Exception {
		//setup
		JsonUtilityService.JSONObject testRuleConditionGroupJson = platformServices.getJsonUtilityService()
				.createJSONObject("{\"logic\":\"and\",\"conditions\":[{\"type\":\"matcher\",\"definition\":{\"key\":\"latitude\",\"matcher\":\"gt\",\"values\":[0]}},{\"type\":\"matcher\",\"definition\":{\"key\":\"longitude\",\"matcher\":\"lt\",\"values\":[0]}}]}");
		RuleConditionGroup testRuleConditionAndGroup = RuleConditionGroup.ruleConditionGroupFromJson(
					testRuleConditionGroupJson);

		EventData testEventData = new EventData();
		Event testEvent = new Event.Builder("test", EventType.ANALYTICS,
											EventSource.RESPONSE_CONTENT).setData(testEventData).build();
		//test
		boolean result = testRuleConditionAndGroup.evaluate(ruleTokenParser, testEvent);
		//verify
		assertFalse(result);
	}

	@Test
	public void evaluate_Fail_When_NullTestDataUsed() throws Exception {
		//setup
		JsonUtilityService.JSONObject testRuleConditionGroupJson = platformServices.getJsonUtilityService()
				.createJSONObject("{\"logic\":\"and\",\"conditions\":[{\"type\":\"matcher\",\"definition\":{\"key\":\"latitude\",\"matcher\":\"gt\",\"values\":[0]}},{\"type\":\"matcher\",\"definition\":{\"key\":\"longitude\",\"matcher\":\"lt\",\"values\":[0]}}]}");
		RuleConditionGroup testRuleConditionAndGroup = RuleConditionGroup.ruleConditionGroupFromJson(
					testRuleConditionGroupJson);

		Event testEvent = new Event.Builder("test", EventType.ANALYTICS,
											EventSource.RESPONSE_CONTENT).setData(null).build();

		//test
		boolean result = testRuleConditionAndGroup.evaluate(ruleTokenParser, testEvent);
		//verify
		assertFalse(result);
	}

	@Test
	public void evaluate_Fail_When_ConditionsAreNull() {
		//setup
		RuleConditionAndGroup testRuleConditionAndGroup = new RuleConditionAndGroup(null);

		EventData testEventData = new EventData();
		testEventData.putString("key", "1");
		testEventData.putString("key1", "value1");
		Event testEvent = new Event.Builder("test", EventType.ANALYTICS,
											EventSource.RESPONSE_CONTENT).setData(testEventData).build();

		//test
		boolean result = testRuleConditionAndGroup.evaluate(ruleTokenParser, testEvent);
		//verify
		assertFalse(result);
	}

	@Test
	public void evaluate_Fail_When_ConditionsAreEmpty() {
		//setup
		RuleConditionAndGroup testRuleConditionAndGroup = new RuleConditionAndGroup(new ArrayList<RuleCondition>());

		EventData testEventData = new EventData();
		testEventData.putString("key", "1");
		testEventData.putString("key1", "value1");
		Event testEvent = new Event.Builder("test", EventType.ANALYTICS,
											EventSource.RESPONSE_CONTENT).setData(testEventData).build();

		//test
		boolean result = testRuleConditionAndGroup.evaluate(ruleTokenParser, testEvent);
		//verify
		assertFalse(result);
	}

	@Test
	public void toString_ShouldReturn_EmptyString_When_ConditionsAreEmpty() {
		//setup
		RuleConditionAndGroup testRuleConditionAndGroup = new RuleConditionAndGroup(new ArrayList<RuleCondition>());
		//test

		//verify
		assertEquals("", testRuleConditionAndGroup.toString());
	}

	@Test
	public void toString_ShouldReturn_EmptyString_When_ConditionsAreNull() {
		//setup
		RuleConditionAndGroup testRuleConditionAndGroup = new RuleConditionAndGroup(null);
		//test

		//verify
		assertEquals("", testRuleConditionAndGroup.toString());
	}

	@Test
	public void toString_ShouldReturn_ValidString() {
		//setup
		ArrayList<RuleCondition> conditions = new ArrayList<RuleCondition>();
		MatcherGreaterThan gtMatcher = new MatcherGreaterThan();
		gtMatcher.key = "key";
		gtMatcher.values = new ArrayList<Object>();
		gtMatcher.values.add("value");

		RuleConditionMatcher ruleConditionMatcherLHS = new RuleConditionMatcher(gtMatcher);
		conditions.add(ruleConditionMatcherLHS);
		RuleConditionMatcher ruleConditionMatcherRHS = new RuleConditionMatcher(gtMatcher);
		conditions.add(ruleConditionMatcherRHS);

		RuleConditionAndGroup testRuleConditionAndGroup = new RuleConditionAndGroup(conditions);

		//Test and verify
		assertEquals("(" + ruleConditionMatcherLHS.toString() + " AND " + ruleConditionMatcherRHS + ")"
					 , testRuleConditionAndGroup.toString());
	}
}
