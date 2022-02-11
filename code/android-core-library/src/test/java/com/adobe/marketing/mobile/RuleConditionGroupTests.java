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

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RuleConditionGroupTests {

	private PlatformServices platformServices;

	@Before
	public void beforeEachTest() {
		platformServices = new FakePlatformServices();
	}

	@Test
	public void ruleConditionGroupFromJson_ReturnsValidRuleConditionAndGroupInstance_When_ValidJsonStringUsed()
	throws Exception {
		//setup
		JsonUtilityService.JSONObject testRuleConditionGroupJson = platformServices.getJsonUtilityService()
				.createJSONObject("{\"logic\":\"and\",\"conditions\":[{\"type\":\"matcher\",\"definition\":{\"key\":\"latitude\",\"matcher\":\"gt\",\"values\":[0]}},{\"type\":\"matcher\",\"definition\":{\"key\":\"longitude\",\"matcher\":\"lt\",\"values\":[0]}}]}");
		//test
		RuleConditionGroup testRuleConditionGroup = RuleConditionGroup.ruleConditionGroupFromJson(testRuleConditionGroupJson);
		//verify
		assertTrue(testRuleConditionGroup instanceof RuleConditionAndGroup);
		assertNotNull(testRuleConditionGroup.conditions);
		assertEquals(2, testRuleConditionGroup.conditions.size());
	}

	@Test
	public void ruleConditionGroupFromJson_Returns_ValidRuleConditionOrGroupInstance_When_ValidJsonStringUsed()
	throws Exception {
		//setup
		JsonUtilityService.JSONObject testRuleConditionGroupJson = platformServices.getJsonUtilityService()
				.createJSONObject("{\"logic\":\"or\",\"conditions\":[{\"type\":\"matcher\",\"definition\":{\"key\":\"latitude\",\"matcher\":\"gt\",\"values\":[0]}},{\"type\":\"matcher\",\"definition\":{\"key\":\"longitude\",\"matcher\":\"lt\",\"values\":[0]}}]}");
		//test
		RuleConditionGroup testRuleConditionGroup = RuleConditionGroup.ruleConditionGroupFromJson(testRuleConditionGroupJson);
		//verify
		assertTrue(testRuleConditionGroup instanceof RuleConditionOrGroup);
		assertNotNull(testRuleConditionGroup.conditions);
		assertEquals(2, testRuleConditionGroup.conditions.size());
	}

	@Test
	@SuppressWarnings("SameParameterValue")
	public void ruleConditionGroupFromJson_ReturnsNull_When_NullJsonStringUsed() throws Exception {
		assertNull(RuleConditionGroup.ruleConditionGroupFromJson(null));
	}

	@Test
	public void ruleConditionGroupFromJson_ReturnsNull_When_EmptyJsonStringUsed() throws Exception {
		//setup
		JsonUtilityService.JSONObject testRuleConditionGroupJson =
			platformServices.getJsonUtilityService().createJSONObject("{}");
		//test
		RuleConditionGroup.ruleConditionGroupFromJson(testRuleConditionGroupJson);
	}

	@Test
	public void ruleConditionGroupFromJson_ReturnsNull_When_InvalidJsonStringUsed() throws Exception {
		//setup
		JsonUtilityService.JSONObject testRuleConditionGroupJson = platformServices.getJsonUtilityService()
				.createJSONObject("{\"invalid_type\":\"group\",\"invalid_definition\":{\"logic\":\"and\",\"invalid_condition\":[{\"type\":\"matcher\",\"definition\":{\"key\":\"latitude\",\"matcher\":\"gt\",\"values\":[0]}},{\"type\":\"matcher\",\"definition\":{\"key\":\"longitude\",\"matcher\":\"lt\",\"values\":[0]}}]}}");
		//test
		RuleConditionGroup.ruleConditionGroupFromJson(testRuleConditionGroupJson);
	}
}
