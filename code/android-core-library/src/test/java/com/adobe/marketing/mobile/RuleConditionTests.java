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

import static org.junit.Assert.*;

public class RuleConditionTests {

	private PlatformServices platformServices;
	private EventHub testEventHub;

	@Before
	public void beforeEachTest() {
		platformServices = new FakePlatformServices();
		testEventHub = new EventHub("eventhub", platformServices);
	}


	@Test
	public void ruleConditionFromJson_ReturnsNull_When_NullJsonObjectProvided() throws Exception {
		assertNull(RuleCondition.ruleConditionFromJson(null));
	}

	@Test
	public void ruleConditionFromJson_ReturnsNull_When_EmptyJsonObjectProvided() throws Exception {
		//test
		JsonUtilityService.JSONObject testRuleConditionJson = platformServices.getJsonUtilityService().createJSONObject("{}");
		RuleCondition testRuleCondition = RuleCondition.ruleConditionFromJson(testRuleConditionJson);
		//verify
		assertNull(testRuleCondition);
	}

	@Test(expected = UnsupportedConditionException.class)
	public void ruleConditionFromJson_ThrowsException_When_ConditionJsonWithInvalidTypeProvided() throws Exception {
		//test
		JsonUtilityService.JSONObject testRuleConditionJson = platformServices.getJsonUtilityService()
				.createJSONObject("{\"type\":\"invalid_type\",\"definition\":{\"logic\":\"and\",\"conditions\":[{\"type\":\"matcher\",\"definition\":{\"key\":\"latitude\",\"matcher\":\"lt\",\"values\":[0]}},{\"type\":\"matcher\",\"definition\":{\"key\":\"longitude\",\"matcher\":\"gt\",\"values\":[0]}}]}}");
		RuleCondition testRuleCondition = RuleCondition.ruleConditionFromJson(testRuleConditionJson);
		//verify
		assertNull(testRuleCondition);
	}

	@Test(expected = UnsupportedConditionException.class)
	public void ruleConditionFromJson_ThrowsException_When_ConditionJsonWithInvalidTypeProvidedInANestedCondition() throws
		Exception {
		//test
		JsonUtilityService.JSONObject testRuleConditionJson = platformServices.getJsonUtilityService()
				.createJSONObject("{\"type\":\"group\",\"definition\":{\"logic\":\"and\",\"conditions\":[{\"type\":\"invalid_type\",\"definition\":{\"key\":\"latitude\",\"matcher\":\"lt\",\"values\":[0]}},{\"type\":\"matcher\",\"definition\":{\"key\":\"longitude\",\"matcher\":\"gt\",\"values\":[0]}}]}}");
		RuleCondition testRuleCondition = RuleCondition.ruleConditionFromJson(testRuleConditionJson);
		//verify
		assertNull(testRuleCondition);
	}

	@Test
	public void ruleConditionFromJson_ReturnsRuleConditionMatcherInstance_When_ValidJsonObjectWithTypeMatIsProvided()
	throws Exception {
		//test
		JsonUtilityService.JSONObject testRuleConditionJson =
			platformServices.getJsonUtilityService().createJSONObject("{\"type\":\"matcher\",\"definition\":{\"key\":\"key\",\"matcher\":\"eq\",\"values\":[\"value\"]}}");
		RuleCondition testRuleCondition = RuleCondition.ruleConditionFromJson(testRuleConditionJson);
		//verify
		assertTrue(testRuleCondition instanceof RuleConditionMatcher);
	}

	@Test
	public void ruleConditionFromJson_ReturnsRuleConditionMatcherInstance_When_MatcherUnknown() throws Exception {
		//test
		JsonUtilityService.JSONObject testRuleConditionJson =
			platformServices.getJsonUtilityService().createJSONObject("{\"type\":\"matcher\",\"definition\":{\"key\":\"key\",\"matcher\":\"xx\",\"values\":[\"value\"]}}");
		RuleCondition testRuleCondition = RuleCondition.ruleConditionFromJson(testRuleConditionJson);
		//verify
		assertEquals("(UNKNOWN)", testRuleCondition.toString());
	}

	@Test
	public void ruleConditionMatcher_toString_ShouldReturnValidString_When_ValidMatcherInstance() {
		//Setup
		MockRuleTokenParser mockRuleTokenParser = new MockRuleTokenParser(testEventHub, "value");
		//test
		MatcherEquals matcherEquals = new MatcherEquals();
		matcherEquals.key = "key";
		matcherEquals.values = new ArrayList<Object>();
		matcherEquals.values.add("value");

		RuleConditionMatcher testRuleCondition = new RuleConditionMatcher(matcherEquals);
		//verify
		assertEquals(matcherEquals.toString(), testRuleCondition.toString());
		assertTrue(testRuleCondition.evaluate(mockRuleTokenParser, new Event.Builder("", EventType.CONFIGURATION,
											  EventSource.RESPONSE_CONTENT).build()));
	}

	@Test
	public void ruleConditionMatcher_toString_ShouldReturnEmptyString_When_NullMatcherInstance() {
		//test
		RuleConditionMatcher testRuleCondition = new RuleConditionMatcher(null);
		//verify
		assertEquals("", testRuleCondition.toString());
	}

	@Test
	public void ruleConditionFromJson_ReturnsRuleConditionMatcherInstance_ShouldReturnNull_When_NullJsonObjectProvided()
	throws Exception {
		//test and verify
		assertNull(RuleConditionMatcher.ruleConditionMatcherFromJson(null));
	}

	@Test
	public void ruleConditionFromJson_ReturnsRuleConditionMatcherInstance_ShouldReturnNull_When_EmptyJsonObjectProvided()
	throws Exception {
		//test and verify
		assertNull(RuleConditionMatcher.ruleConditionMatcherFromJson(
					   platformServices.getJsonUtilityService().createJSONObject("{}")));
	}

	@Test
	public void ruleConditionFromJson_ReturnsRuleConditionGroup_When_ValidJsonObjectWithTypeGrpIsProvided()
	throws JsonException, UnsupportedConditionException {
		//test
		JsonUtilityService.JSONObject testRuleConditionJson =
			platformServices.getJsonUtilityService().createJSONObject("{\"type\":\"group\",\"definition\":{\"logic\":\"and\",\"conditions\":[{\"type\":\"matcher\",\"definition\":{\"key\":\"latitude\",\"matcher\":\"lt\",\"values\":[0]}},{\"type\":\"matcher\",\"definition\":{\"key\":\"longitude\",\"matcher\":\"gt\",\"values\":[0]}}]}}");
		RuleCondition testRuleCondition = RuleCondition.ruleConditionFromJson(testRuleConditionJson);
		//verify
		assertTrue(testRuleCondition instanceof RuleConditionGroup);
	}

	@Test
	public void ruleConditionMatcher_Equals_Evaluate_ShouldReturnTrue_ForValidMatch() {
		//Setup
		MockRuleTokenParser mockRuleTokenParser = new MockRuleTokenParser(testEventHub, "value");
		//test
		MatcherEquals matcherEquals = new MatcherEquals();
		matcherEquals.key = "key";
		matcherEquals.values = new ArrayList<Object>();
		matcherEquals.values.add("value");

		RuleConditionMatcher testRuleCondition = new RuleConditionMatcher(matcherEquals);
		//verify
		assertTrue(testRuleCondition.evaluate(mockRuleTokenParser, new Event.Builder("", EventType.CONFIGURATION,
											  EventSource.RESPONSE_CONTENT).build()));
	}

	@Test
	public void ruleConditionMatcher_Equals_Evaluate_ShouldReturnFalse_ForInValidMatch() {
		//Setup
		MockRuleTokenParser mockRuleTokenParser = new MockRuleTokenParser(testEventHub, "not-a-value");
		//test
		MatcherEquals matcherEquals = new MatcherEquals();
		matcherEquals.key = "key";
		matcherEquals.values = new ArrayList<Object>();
		matcherEquals.values.add("value");

		RuleConditionMatcher testRuleCondition = new RuleConditionMatcher(matcherEquals);
		//verify
		assertFalse(testRuleCondition.evaluate(mockRuleTokenParser, new Event.Builder("", EventType.CONFIGURATION,
											   EventSource.RESPONSE_CONTENT).build()));
	}

	@Test
	public void ruleConditionMatcher_NotEquals_Evaluate_ShouldReturnTrue_ForValidMatch() {
		//Setup
		MockRuleTokenParser mockRuleTokenParser = new MockRuleTokenParser(testEventHub, "not-a-value");
		//test
		MatcherEquals matcherNotEquals = new MatcherNotEquals();
		matcherNotEquals.key = "key";
		matcherNotEquals.values = new ArrayList<Object>();
		matcherNotEquals.values.add("value");

		RuleConditionMatcher testRuleCondition = new RuleConditionMatcher(matcherNotEquals);
		//verify
		assertTrue(testRuleCondition.evaluate(mockRuleTokenParser, new Event.Builder("", EventType.CONFIGURATION,
											  EventSource.RESPONSE_CONTENT).build()));
	}

	@Test
	public void ruleConditionMatcher_NotEquals_Evaluate_ShouldReturnFalse_ForInValidMatch() {
		//Setup
		MockRuleTokenParser mockRuleTokenParser = new MockRuleTokenParser(testEventHub, "value");
		//test
		MatcherEquals matcherNotEquals = new MatcherNotEquals();
		matcherNotEquals.key = "key";
		matcherNotEquals.values = new ArrayList<Object>();
		matcherNotEquals.values.add("value");

		RuleConditionMatcher testRuleCondition = new RuleConditionMatcher(matcherNotEquals);
		//verify
		assertFalse(testRuleCondition.evaluate(mockRuleTokenParser, new Event.Builder("", EventType.CONFIGURATION,
											   EventSource.RESPONSE_CONTENT).build()));
	}

	@Test
	public void ruleConditionMatcher_NotExists_Evaluate_ShouldReturnTrue_ForValidMatch() {
		//Setup
		MockRuleTokenParser mockRuleTokenParser = new MockRuleTokenParser(testEventHub, null);
		//test
		MatcherNotExists matcherNotExists = new MatcherNotExists();
		matcherNotExists.key = "key";

		RuleConditionMatcher testRuleCondition = new RuleConditionMatcher(matcherNotExists);
		//verify
		assertTrue(testRuleCondition.evaluate(mockRuleTokenParser, new Event.Builder("", EventType.CONFIGURATION,
											  EventSource.RESPONSE_CONTENT).build()));
	}

	@Test
	public void ruleConditionMatcher_NotExists_Evaluate_ShouldReturnFalse_ForInValidMatch() {
		//Setup
		MockRuleTokenParser mockRuleTokenParser = new MockRuleTokenParser(testEventHub, "value");
		//test
		MatcherNotExists matcherNotExists = new MatcherNotExists();
		matcherNotExists.key = "key";

		RuleConditionMatcher testRuleCondition = new RuleConditionMatcher(matcherNotExists);
		//verify
		assertFalse(testRuleCondition.evaluate(mockRuleTokenParser, new Event.Builder("", EventType.CONFIGURATION,
											   EventSource.RESPONSE_CONTENT).build()));
	}

	@Test
	public void ruleConditionMatcher_Exists_Evaluate_ShouldReturnTrue_ForValidMatch() {
		//Setup
		MockRuleTokenParser mockRuleTokenParser = new MockRuleTokenParser(testEventHub, "value");
		//test
		MatcherExists matcherExists = new MatcherExists();
		matcherExists.key = "key";

		RuleConditionMatcher testRuleCondition = new RuleConditionMatcher(matcherExists);
		//verify
		assertTrue(testRuleCondition.evaluate(mockRuleTokenParser, new Event.Builder("", EventType.CONFIGURATION,
											  EventSource.RESPONSE_CONTENT).build()));
	}

	@Test
	public void ruleConditionMatcher_Exists_Evaluate_ShouldReturnFalse_ForInValidMatch() {
		//Setup
		MockRuleTokenParser mockRuleTokenParser = new MockRuleTokenParser(testEventHub, null);
		//test
		MatcherExists matcherExists = new MatcherExists();
		matcherExists.key = "key";

		RuleConditionMatcher testRuleCondition = new RuleConditionMatcher(matcherExists);
		//verify
		assertFalse(testRuleCondition.evaluate(mockRuleTokenParser, new Event.Builder("", EventType.CONFIGURATION,
											   EventSource.RESPONSE_CONTENT).build()));
	}


	@Test
	public void ruleConditionMatcher_Contains_Evaluate_ShouldReturnTrue_ForValidMatch() {
		//Setup
		MockRuleTokenParser mockRuleTokenParser = new MockRuleTokenParser(testEventHub, "valueWithA Bigger value");
		//test
		MatcherContains matcherContains = new MatcherContains();
		matcherContains.key = "key";
		matcherContains.values = new ArrayList<Object>();
		matcherContains.values.add("value");

		RuleConditionMatcher testRuleCondition = new RuleConditionMatcher(matcherContains);
		//verify
		assertTrue(testRuleCondition.evaluate(mockRuleTokenParser, new Event.Builder("", EventType.CONFIGURATION,
											  EventSource.RESPONSE_CONTENT).build()));
	}

	@Test
	public void ruleConditionMatcher_Contains_Evaluate_ShouldReturnFalse_ForInValidMatch() {
		//Setup
		MockRuleTokenParser mockRuleTokenParser = new MockRuleTokenParser(testEventHub, "valueWithA Bigger value");
		//test
		MatcherContains matcherContains = new MatcherContains();
		matcherContains.key = "key";
		matcherContains.values = new ArrayList<Object>();
		matcherContains.values.add("no-value");

		RuleConditionMatcher testRuleCondition = new RuleConditionMatcher(matcherContains);
		assertFalse(testRuleCondition.evaluate(mockRuleTokenParser, new Event.Builder("", EventType.CONFIGURATION,
											   EventSource.RESPONSE_CONTENT).build()));
	}

	@Test
	public void ruleConditionMatcher_DoesNotContains_Evaluate_ShouldReturnTrue_ForValidMatch() {
		//Setup
		MockRuleTokenParser mockRuleTokenParser = new MockRuleTokenParser(testEventHub, "valueWithA Bigger value");
		//test
		MatcherContains matcherNotContains = new MatcherNotContains();
		matcherNotContains.key = "key";
		matcherNotContains.values = new ArrayList<Object>();
		matcherNotContains.values.add("no-value");

		RuleConditionMatcher testRuleCondition = new RuleConditionMatcher(matcherNotContains);
		//verify
		assertTrue(testRuleCondition.evaluate(mockRuleTokenParser, new Event.Builder("", EventType.CONFIGURATION,
											  EventSource.RESPONSE_CONTENT).build()));
	}

	@Test
	public void ruleConditionMatcher_DoesNotContains_Evaluate_ShouldReturnFalse_ForInValidMatch() {
		//Setup
		MockRuleTokenParser mockRuleTokenParser = new MockRuleTokenParser(testEventHub, "valueWithA Bigger value");
		//test
		MatcherContains matcherNotContains = new MatcherNotContains();
		matcherNotContains.key = "key";
		matcherNotContains.values = new ArrayList<Object>();
		matcherNotContains.values.add("value");

		RuleConditionMatcher testRuleCondition = new RuleConditionMatcher(matcherNotContains);
		//verify
		assertFalse(testRuleCondition.evaluate(mockRuleTokenParser, new Event.Builder("", EventType.CONFIGURATION,
											   EventSource.RESPONSE_CONTENT).build()));
	}

	@Test
	public void ruleConditionMatcher_GreaterThan_Evaluate_ShouldReturnTrue_ForValidMatch() {
		//Setup
		MockRuleTokenParser mockRuleTokenParser = new MockRuleTokenParser(testEventHub, "6");
		//test
		MatcherGreaterThan matcherGreaterThan = new MatcherGreaterThan();
		matcherGreaterThan.key = "key";
		matcherGreaterThan.values = new ArrayList<Object>();
		matcherGreaterThan.values.add(5);

		RuleConditionMatcher testRuleCondition = new RuleConditionMatcher(matcherGreaterThan);
		//verify
		assertTrue(testRuleCondition.evaluate(mockRuleTokenParser, new Event.Builder("", EventType.CONFIGURATION,
											  EventSource.RESPONSE_CONTENT).build()));
	}

	@Test
	public void ruleConditionMatcher_GreaterThan_Evaluate_ShouldReturnFalse_ForInValidMatch() {
		//Setup
		MockRuleTokenParser mockRuleTokenParser = new MockRuleTokenParser(testEventHub, "4");
		//test
		MatcherGreaterThan matcherGreaterThan = new MatcherGreaterThan();
		matcherGreaterThan.key = "key";
		matcherGreaterThan.values = new ArrayList<Object>();
		matcherGreaterThan.values.add(5);

		RuleConditionMatcher testRuleCondition = new RuleConditionMatcher(matcherGreaterThan);
		//verify
		assertFalse(testRuleCondition.evaluate(mockRuleTokenParser, new Event.Builder("", EventType.CONFIGURATION,
											   EventSource.RESPONSE_CONTENT).build()));
	}

	@Test
	public void ruleConditionMatcher_LessThan_Evaluate_ShouldReturnTrue_ForValidMatch() {
		//Setup
		MockRuleTokenParser mockRuleTokenParser = new MockRuleTokenParser(testEventHub, "4");
		//test
		MatcherLessThan matcherContains = new MatcherLessThan();
		matcherContains.key = "key";
		matcherContains.values = new ArrayList<Object>();
		matcherContains.values.add(5);

		RuleConditionMatcher testRuleCondition = new RuleConditionMatcher(matcherContains);
		//verify
		assertTrue(testRuleCondition.evaluate(mockRuleTokenParser, new Event.Builder("", EventType.CONFIGURATION,
											  EventSource.RESPONSE_CONTENT).build()));
	}

	@Test
	public void ruleConditionMatcher_LessThan_Evaluate_ShouldReturnFalse_ForInValidMatch() {
		//Setup
		MockRuleTokenParser mockRuleTokenParser = new MockRuleTokenParser(testEventHub, "6");
		//test
		MatcherLessThan matcherContains = new MatcherLessThan();
		matcherContains.key = "key";
		matcherContains.values = new ArrayList<Object>();
		matcherContains.values.add(5);

		RuleConditionMatcher testRuleCondition = new RuleConditionMatcher(matcherContains);
		//verify
		assertFalse(testRuleCondition.evaluate(mockRuleTokenParser, new Event.Builder("", EventType.CONFIGURATION,
											   EventSource.RESPONSE_CONTENT).build()));
	}

	@Test
	public void ruleConditionMatcher_LessThanOrEqualTo_Evaluate_ShouldReturnTrue_ForValidMatch() {
		//Setup
		MockRuleTokenParser mockRuleTokenParser = new MockRuleTokenParser(testEventHub, "5");
		//test
		MatcherLessThanOrEqual matcherLessThanOrEqual = new MatcherLessThanOrEqual();
		matcherLessThanOrEqual.key = "key";
		matcherLessThanOrEqual.values = new ArrayList<Object>();
		matcherLessThanOrEqual.values.add(5);

		RuleConditionMatcher testRuleCondition = new RuleConditionMatcher(matcherLessThanOrEqual);
		//verify
		assertTrue(testRuleCondition.evaluate(mockRuleTokenParser, new Event.Builder("", EventType.CONFIGURATION,
											  EventSource.RESPONSE_CONTENT).build()));
	}

	@Test
	public void ruleConditionMatcher_LessThanOrEqualTo_Evaluate_ShouldReturnFalse_ForInValidMatch() {
		//Setup
		MockRuleTokenParser mockRuleTokenParser = new MockRuleTokenParser(testEventHub, "6");
		//test
		MatcherLessThanOrEqual matcherLessThanOrEqual = new MatcherLessThanOrEqual();
		matcherLessThanOrEqual.key = "key";
		matcherLessThanOrEqual.values = new ArrayList<Object>();
		matcherLessThanOrEqual.values.add(5);

		RuleConditionMatcher testRuleCondition = new RuleConditionMatcher(matcherLessThanOrEqual);
		//verify
		assertFalse(testRuleCondition.evaluate(mockRuleTokenParser, new Event.Builder("", EventType.CONFIGURATION,
											   EventSource.RESPONSE_CONTENT).build()));
	}

	@Test
	public void ruleConditionMatcher_GreaterThanOrEqualTo_Evaluate_ShouldReturnTrue_ForValidMatch() {
		//Setup
		MockRuleTokenParser mockRuleTokenParser = new MockRuleTokenParser(testEventHub, "5");
		//test
		MatcherGreaterThanOrEqual matcherGreaterThanOrEqual = new MatcherGreaterThanOrEqual();
		matcherGreaterThanOrEqual.key = "key";
		matcherGreaterThanOrEqual.values = new ArrayList<Object>();
		matcherGreaterThanOrEqual.values.add(5);

		RuleConditionMatcher testRuleCondition = new RuleConditionMatcher(matcherGreaterThanOrEqual);
		//verify
		assertTrue(testRuleCondition.evaluate(mockRuleTokenParser, new Event.Builder("", EventType.CONFIGURATION,
											  EventSource.RESPONSE_CONTENT).build()));
	}

	@Test
	public void ruleConditionMatcher_GreaterThanOrEqualTo_Evaluate_ShouldReturnFalse_ForInValidMatch() {
		//Setup
		MockRuleTokenParser mockRuleTokenParser = new MockRuleTokenParser(testEventHub, "4");
		//test
		MatcherGreaterThanOrEqual matcherGreaterThanOrEqual = new MatcherGreaterThanOrEqual();
		matcherGreaterThanOrEqual.key = "key";
		matcherGreaterThanOrEqual.values = new ArrayList<Object>();
		matcherGreaterThanOrEqual.values.add(5);

		RuleConditionMatcher testRuleCondition = new RuleConditionMatcher(matcherGreaterThanOrEqual);
		//verify
		assertFalse(testRuleCondition.evaluate(mockRuleTokenParser, new Event.Builder("", EventType.CONFIGURATION,
											   EventSource.RESPONSE_CONTENT).build()));
	}

	@Test
	public void ruleConditionMatcher_EndsWith_Evaluate_ShouldReturnTrue_ForValidMatch() {
		//Setup
		MockRuleTokenParser mockRuleTokenParser = new MockRuleTokenParser(testEventHub, "valueending");
		//test
		MatcherEndsWith matcherEndsWith = new MatcherEndsWith();
		matcherEndsWith.key = "key";
		matcherEndsWith.values = new ArrayList<Object>();
		matcherEndsWith.values.add("ending");

		RuleConditionMatcher testRuleCondition = new RuleConditionMatcher(matcherEndsWith);
		//verify
		assertTrue(testRuleCondition.evaluate(mockRuleTokenParser, new Event.Builder("", EventType.CONFIGURATION,
											  EventSource.RESPONSE_CONTENT).build()));
	}

	@Test
	public void ruleConditionMatcher_EndsWith_Evaluate_ShouldReturnFalse_ForInValidMatch() {
		//Setup
		MockRuleTokenParser mockRuleTokenParser = new MockRuleTokenParser(testEventHub, "valueending_doesnot");
		//test
		MatcherEndsWith matcherEndsWith = new MatcherEndsWith();
		matcherEndsWith.key = "key";
		matcherEndsWith.values = new ArrayList<Object>();
		matcherEndsWith.values.add("ending");

		RuleConditionMatcher testRuleCondition = new RuleConditionMatcher(matcherEndsWith);
		//verify
		assertFalse(testRuleCondition.evaluate(mockRuleTokenParser, new Event.Builder("", EventType.CONFIGURATION,
											   EventSource.RESPONSE_CONTENT).build()));
	}

	@Test
	public void ruleConditionMatcher_StartsWith_Evaluate_ShouldReturnTrue_ForValidMatch() {
		//Setup
		MockRuleTokenParser mockRuleTokenParser = new MockRuleTokenParser(testEventHub, "value_starting");
		//test
		MatcherStartsWith matcherEndsWith = new MatcherStartsWith();
		matcherEndsWith.key = "key";
		matcherEndsWith.values = new ArrayList<Object>();
		matcherEndsWith.values.add("value");

		RuleConditionMatcher testRuleCondition = new RuleConditionMatcher(matcherEndsWith);
		//verify
		assertTrue(testRuleCondition.evaluate(mockRuleTokenParser, new Event.Builder("", EventType.CONFIGURATION,
											  EventSource.RESPONSE_CONTENT).build()));
	}

	@Test
	public void ruleConditionMatcher_StartsWith_Evaluate_ShouldReturnFalse_ForInValidMatch() {
		//Setup
		MockRuleTokenParser mockRuleTokenParser = new MockRuleTokenParser(testEventHub, "does_not_start_with_value");
		//test
		MatcherStartsWith matcherEndsWith = new MatcherStartsWith();
		matcherEndsWith.key = "key";
		matcherEndsWith.values = new ArrayList<Object>();
		matcherEndsWith.values.add("value");

		RuleConditionMatcher testRuleCondition = new RuleConditionMatcher(matcherEndsWith);
		//verify
		assertFalse(testRuleCondition.evaluate(mockRuleTokenParser, new Event.Builder("", EventType.CONFIGURATION,
											   EventSource.RESPONSE_CONTENT).build()));
	}
}
