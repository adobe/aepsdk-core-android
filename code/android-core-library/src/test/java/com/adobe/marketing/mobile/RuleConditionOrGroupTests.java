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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RuleConditionOrGroupTests {

	private PlatformServices platformServices;
	private RuleTokenParser ruleTokenParser;

	@Before
	public void beforeEachTest() {
		platformServices = new FakePlatformServices();
		final EventHub testEventHub = new EventHub("eventhub", platformServices);
		ruleTokenParser = new RuleTokenParser(testEventHub);
	}


	@Test
	public void evaluate_Success_When_OneOfTheConditionsMatch() throws Exception {
		//setup
		JsonUtilityService.JSONObject testRuleConditionGroupJson = platformServices.getJsonUtilityService()
				.createJSONObject("{\"logic\":\"or\",\"conditions\":[{\"type\":\"matcher\",\"definition\":{\"key\":\"key\",\"matcher\":\"gt\",\"values\":[0]}},{\"type\":\"matcher\",\"definition\":{\"key\":\"key1\",\"matcher\":\"eq\",\"values\":[\"something\"]}}]}");
		RuleConditionGroup testRuleConditionOrGroup = RuleConditionGroup.ruleConditionGroupFromJson(testRuleConditionGroupJson);

		EventData testEventData = new EventData();
		testEventData.putString("key", "1");
		testEventData.putString("key1", "value1");
		Event testEvent = new Event.Builder("test", EventType.ANALYTICS,
											EventSource.RESPONSE_CONTENT).setData(testEventData).build();

		//test
		boolean result = testRuleConditionOrGroup.evaluate(ruleTokenParser, testEvent);
		//verify
		assertTrue(result);
	}

	@Test
	public void evaluate_Failure_When_DataEmpty() throws Exception {
		//setup
		JsonUtilityService.JSONObject testRuleConditionGroupJson =
			platformServices.getJsonUtilityService().createJSONObject("{\"logic\":\"or\",\"conditions\":[{\"type\":\"matcher\",\"definition\":{\"key\":\"latitude\",\"matcher\":\"gt\",\"values\":[0]}},{\"type\":\"matcher\",\"definition\":{\"key\":\"longitude\",\"matcher\":\"lt\",\"values\":[0]}}]}");
		RuleConditionGroup testRuleConditionOrGroup = RuleConditionGroup.ruleConditionGroupFromJson(testRuleConditionGroupJson);

		EventData testEventData = new EventData();
		Event testEvent = new Event.Builder("test", EventType.ANALYTICS,
											EventSource.RESPONSE_CONTENT).setData(testEventData).build();
		//test
		boolean result = testRuleConditionOrGroup.evaluate(ruleTokenParser, testEvent);
		//verify
		assertFalse(result);
	}

	@Test
	public void evaluate_Failure_When_DataNull() throws Exception {
		//setup
		JsonUtilityService.JSONObject testRuleConditionGroupJson =
			platformServices.getJsonUtilityService().createJSONObject("{\"logic\":\"or\",\"conditions\":[{\"type\":\"matcher\",\"definition\":{\"key\":\"latitude\",\"matcher\":\"gt\",\"values\":[0]}},{\"type\":\"matcher\",\"definition\":{\"key\":\"longitude\",\"matcher\":\"lt\",\"values\":[0]}}]}");
		RuleConditionGroup testRuleConditionOrGroup = RuleConditionGroup.ruleConditionGroupFromJson(testRuleConditionGroupJson);

		Event testEvent = new Event.Builder("test", EventType.ANALYTICS,
											EventSource.RESPONSE_CONTENT).setData(null).build();

		//test
		boolean result = testRuleConditionOrGroup.evaluate(ruleTokenParser, testEvent);
		//verify
		assertFalse(result);
	}

	@Test
	public void evaluate_Failure_When_ConditionsNull() {
		//setup
		RuleConditionOrGroup testRuleConditionOrGroup = new RuleConditionOrGroup(null);

		EventData testEventData = new EventData();
		testEventData.putString("key", "1");
		testEventData.putString("key1", "value1");
		Event testEvent = new Event.Builder("test", EventType.ANALYTICS,
											EventSource.RESPONSE_CONTENT).setData(testEventData).build();

		//test
		boolean result = testRuleConditionOrGroup.evaluate(ruleTokenParser, testEvent);
		//verify
		assertFalse(result);
	}

	@Test
	public void evaluate_Failure_When_ConditionEmpty() {
		//setup
		RuleConditionOrGroup testRuleConditionOrGroup = new RuleConditionOrGroup(new ArrayList<RuleCondition>());

		EventData testEventData = new EventData();
		testEventData.putString("key", "1");
		testEventData.putString("key1", "value1");
		Event testEvent = new Event.Builder("test", EventType.ANALYTICS,
											EventSource.RESPONSE_CONTENT).setData(testEventData).build();

		//test
		boolean result = testRuleConditionOrGroup.evaluate(ruleTokenParser, testEvent);
		//verify
		assertFalse(result);
	}

	@Test
	public void toString_ShouldReturn_EmptyString_When_ConditionsAreEmpty() {
		//setup
		RuleConditionOrGroup testRuleConditionAndGroup = new RuleConditionOrGroup(new ArrayList<RuleCondition>());
		//test and Verify
		assertEquals("", testRuleConditionAndGroup.toString());
	}

	@Test
	public void toString_ShouldReturn_EmptyString_When_ConditionsAreNull() {
		//setup
		RuleConditionOrGroup testRuleConditionAndGroup = new RuleConditionOrGroup(null);
		//test and Verify
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

		RuleConditionOrGroup testRuleConditionAndGroup = new RuleConditionOrGroup(conditions);

		//Test and verify
		assertEquals("(" + ruleConditionMatcherLHS.toString() + " OR " + ruleConditionMatcherRHS + ")"
					 , testRuleConditionAndGroup.toString());
	}
}
