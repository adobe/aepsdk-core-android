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

import java.util.Map;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class RuleConsequenceTests {

	private static final String CONSEQUENCE_JSON_ID = "id";
	private static final String CONSEQUENCE_JSON_TYPE = "type";
	private static final String CONSEQUENCE_JSON_DETAIL = "detail";
	private static final String CONSEQUENCE_TRIGGERED = "triggeredconsequence";

	private JsonUtilityService jsonUtilityService;

	private JsonUtilityService.JSONObject validConsequenceJSON() {
		return jsonUtilityService.createJSONObject("        {\n" +
				"          \"id\": \"71335c64ae133c890603a73fd01f2df70119bb42\",\n" +
				"          \"type\": \"iam\",\n" +
				"          \"detail\": {\n" +
				"            \"template\": \"local\",\n" +
				"            \"content\": \"Hey Scooby, you have items in your cart!\",\n" +
				"            \"delay\": 5\n" +
				"          }\n" +
				"        }");
	}

	private JsonUtilityService.JSONObject consequenceJSON_NoID() {
		return jsonUtilityService.createJSONObject("        {\n" +
				"          \"type\": \"iam\",\n" +
				"          \"detail\": {\n" +
				"            \"template\": \"local\",\n" +
				"            \"content\": \"Hey Scooby, you have items in your cart!\",\n" +
				"            \"delay\": 5\n" +
				"          }\n" +
				"        }");
	}

	private JsonUtilityService.JSONObject consequenceJSON_NoType() {
		return jsonUtilityService.createJSONObject("        {\n" +
				"          \"id\": \"71335c64ae133c890603a73fd01f2df70119bb42\",\n" +
				"          \"detail\": {\n" +
				"            \"template\": \"local\",\n" +
				"            \"content\": \"Hey Scooby, you have items in your cart!\",\n" +
				"            \"delay\": 5\n" +
				"          }\n" +
				"        }");
	}

	private JsonUtilityService.JSONObject consequenceJSON_NoDetail() {
		return jsonUtilityService.createJSONObject("        {\n" +
				"          \"id\": \"71335c64ae133c890603a73fd01f2df70119bb42\",\n" +
				"          \"type\": \"iam\"\n" +
				"        }");
	}

	private JsonUtilityService.JSONObject emptyJSON() {
		return jsonUtilityService.createJSONObject("{}");
	}

	@Before
	public void beforeEachTest() {
		final PlatformServices platformServices = new FakePlatformServices();
		jsonUtilityService = platformServices.getJsonUtilityService();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void consequenceFromJson_Happy() {
		// test
		RuleConsequence ruleConsequence = RuleConsequence.consequenceFromJson(validConsequenceJSON(), jsonUtilityService);
		// verify
		assertNotNull(ruleConsequence);
		final EventData ruleConsequenceEventData = ruleConsequence.generateEventData();
		final Map<String, Object> consequenceTriggeredMap = (HashMap<String, Object>) ruleConsequenceEventData.getObject(
					CONSEQUENCE_TRIGGERED);
		final Map<String, Object> detailMap = (HashMap<String, Object>) consequenceTriggeredMap.get(
				CONSEQUENCE_JSON_DETAIL);

		// verify consequence details
		assertEquals("71335c64ae133c890603a73fd01f2df70119bb42",
					 consequenceTriggeredMap.get(CONSEQUENCE_JSON_ID));
		assertEquals("iam", consequenceTriggeredMap.get(CONSEQUENCE_JSON_TYPE));
		assertNotNull(detailMap);
		assertEquals(5, detailMap.get("delay"));
		assertEquals("local", detailMap.get("template"));
		assertEquals("Hey Scooby, you have items in your cart!", detailMap.get("content"));
	}

	@Test
	public void consequenceFromJson_NullJSONObject() {
		assertNull(RuleConsequence.consequenceFromJson(null, jsonUtilityService));
	}

	@Test
	public void consequenceFromJson_EmptyJSON() {
		assertNull(RuleConsequence.consequenceFromJson(emptyJSON(), jsonUtilityService));
	}

	@Test
	public void consequenceFromJson_NoID() {
		assertNull(RuleConsequence.consequenceFromJson(consequenceJSON_NoID(), jsonUtilityService));
	}

	@Test
	public void consequenceFromJson_NoType() {
		assertNull(RuleConsequence.consequenceFromJson(consequenceJSON_NoType(), jsonUtilityService));
	}

	@Test
	public void consequenceFromJson_NoDetails() {
		assertNull(RuleConsequence.consequenceFromJson(consequenceJSON_NoDetail(), jsonUtilityService));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void generateEventData_Happy() {
		// setup
		final Map<String, Object> expandedDetails = new HashMap<String, Object>();
		expandedDetails.put("template", "local");
		expandedDetails.put("content", "Hey Scooby, you have items in your cart!");
		expandedDetails.put("delay", 5);


		// test
		final RuleConsequence ruleConsequence = RuleConsequence.consequenceFromJson(validConsequenceJSON(), jsonUtilityService);
		final EventData eventData = ruleConsequence.generateEventData();

		// verify
		final Map<String, Object> consequenceMap = (HashMap<String, Object>) eventData.getObject(
					CONSEQUENCE_TRIGGERED);
		assertNotNull(consequenceMap);
		assertEquals("71335c64ae133c890603a73fd01f2df70119bb42",
					 consequenceMap.get(CONSEQUENCE_JSON_ID));
		assertEquals("iam", consequenceMap.get(CONSEQUENCE_JSON_TYPE));
		assertEquals(expandedDetails, consequenceMap.get(
						 CONSEQUENCE_JSON_DETAIL));
	}
}
