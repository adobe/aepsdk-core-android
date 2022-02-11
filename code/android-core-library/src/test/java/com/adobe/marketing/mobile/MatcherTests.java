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

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.*;
import static org.junit.Assert.assertTrue;

public class MatcherTests {
	private PlatformServices platformServices;
	private JsonUtilityService jsonUtilityService;
	private RuleTokenParser ruleTokenParser;

	@Before
	public void setUp() {
		platformServices = new FakePlatformServices();
		EventHub testEventHub = new EventHub("eventhub", platformServices);
		ruleTokenParser = new RuleTokenParser(testEventHub);
		jsonUtilityService = platformServices.getJsonUtilityService();
	}


	// ================================================================================
	// constructor tests
	// ================================================================================
	@Test
	public void constructorEquals() {
		//setup
		Map<String, Object> testData = new HashMap<String, Object>();
		testData.put("key", "blah");
		testData.put("matcher", "eq");
		List<String> valuesList = new ArrayList<String>();
		valuesList.add("testValue1");
		testData.put("values", valuesList);
		JsonUtilityService.JSONObject jsonObject = jsonUtilityService.createJSONObject(new JSONObject(testData).toString());
		//test
		Matcher matcher = Matcher.matcherWithJsonObject(jsonObject);
		//assert
		assertNotNull(matcher);
		assertEquals(MatcherEquals.class, matcher.getClass());
	}

	@Test
	public void constructorEmptyMatcher() {
		//setup
		Map<String, Object> testData = new HashMap<String, Object>();
		testData.put("key", "blah");
		//Empty matcher string
		testData.put("matcher", "");
		List<String> valuesList = new ArrayList<String>();
		valuesList.add("testValue1");
		testData.put("values", valuesList);
		JsonUtilityService.JSONObject jsonObject = jsonUtilityService.createJSONObject(new JSONObject(testData).toString());
		//test
		Matcher matcher = Matcher.matcherWithJsonObject(jsonObject);
		//assert
		assertNotNull(matcher);
		assertEquals(MatcherUnknown.class, matcher.getClass());
	}

	@Test
	public void constructorNotEquals() {
		//setup
		Map<String, Object> testData = new HashMap<String, Object>();
		testData.put("key", "blah");
		testData.put("matcher", "ne");
		List<String> valuesList = new ArrayList<String>();
		valuesList.add("testValue1");
		testData.put("values", valuesList);
		JsonUtilityService.JSONObject jsonObject = jsonUtilityService.createJSONObject(new JSONObject(testData).toString());
		//setup
		Matcher matcher = Matcher.matcherWithJsonObject(jsonObject);
		//verify
		assertNotNull(matcher);
		assertEquals(MatcherNotEquals.class, matcher.getClass());
	}

	@Test
	public void constructorContains() {
		//setup
		Map<String, Object> testData = new HashMap<String, Object>();
		testData.put("key", "blah");
		testData.put("matcher", "co");
		List<String> valuesList = new ArrayList<String>();
		valuesList.add("testValue1");
		testData.put("values", valuesList);
		JsonUtilityService.JSONObject jsonObject = jsonUtilityService.createJSONObject(new JSONObject(testData).toString());
		//test
		Matcher matcher = Matcher.matcherWithJsonObject(jsonObject);
		//verify
		assertNotNull(matcher);
		assertEquals(MatcherContains.class, matcher.getClass());
	}

	@Test
	public void constructorNotContains() {
		//setup
		Map<String, Object> testData = new HashMap<String, Object>();
		testData.put("key", "blah");
		testData.put("matcher", "nc");
		List<String> valuesList = new ArrayList<String>();
		valuesList.add("testValue1");
		testData.put("values", valuesList);
		JsonUtilityService.JSONObject jsonObject = jsonUtilityService.createJSONObject(new JSONObject(testData).toString());
		//test
		Matcher matcher = Matcher.matcherWithJsonObject(jsonObject);
		//verify
		assertNotNull(matcher);
		assertEquals(MatcherNotContains.class, matcher.getClass());
	}

	@Test
	public void constructorGreaterThan() {
		//setup
		Map<String, Object> testData = new HashMap<String, Object>();
		testData.put("key", "blah");
		testData.put("matcher", "gt");
		List<String> valuesList = new ArrayList<String>();
		valuesList.add("testValue1");
		testData.put("values", valuesList);
		JsonUtilityService.JSONObject jsonObject = jsonUtilityService.createJSONObject(new JSONObject(testData).toString());
		//test
		Matcher matcher = Matcher.matcherWithJsonObject(jsonObject);
		//verify
		assertNotNull(matcher);
		assertEquals(MatcherGreaterThan.class, matcher.getClass());
	}

	@Test
	public void constructorGreaterThanOrEquals() {
		//setup
		Map<String, Object> testData = new HashMap<String, Object>();
		testData.put("key", "blah");
		testData.put("matcher", "ge");
		List<String> valuesList = new ArrayList<String>();
		valuesList.add("testValue1");
		testData.put("values", valuesList);
		JsonUtilityService.JSONObject jsonObject = jsonUtilityService.createJSONObject(new JSONObject(testData).toString());
		//test
		Matcher matcher = Matcher.matcherWithJsonObject(jsonObject);
		//verify
		assertNotNull(matcher);
		assertEquals(MatcherGreaterThanOrEqual.class, matcher.getClass());
	}

	@Test
	public void constructorLessThan() {
		//setup
		Map<String, Object> testData = new HashMap<String, Object>();
		testData.put("key", "blah");
		testData.put("matcher", "lt");
		List<String> valuesList = new ArrayList<String>();
		valuesList.add("testValue1");
		testData.put("values", valuesList);
		JsonUtilityService.JSONObject jsonObject = jsonUtilityService.createJSONObject(new JSONObject(testData).toString());
		//test
		Matcher matcher = Matcher.matcherWithJsonObject(jsonObject);
		//verify
		assertNotNull(matcher);
		assertEquals(MatcherLessThan.class, matcher.getClass());
	}

	@Test
	public void constructorLessThanOrEquals() {
		//setup
		Map<String, Object> testData = new HashMap<String, Object>();
		testData.put("key", "blah");
		testData.put("matcher", "le");
		List<String> valuesList = new ArrayList<String>();
		valuesList.add("testValue1");
		testData.put("values", valuesList);
		JsonUtilityService.JSONObject jsonObject = jsonUtilityService.createJSONObject(new JSONObject(testData).toString());
		//test
		Matcher matcher = Matcher.matcherWithJsonObject(jsonObject);
		//verify
		assertNotNull(matcher);
		assertEquals(MatcherLessThanOrEqual.class, matcher.getClass());
	}

	@Test
	public void constructorStartsWith() {
		//setup
		Map<String, Object> testData = new HashMap<String, Object>();
		testData.put("key", "blah");
		testData.put("matcher", "sw");
		List<String> valuesList = new ArrayList<String>();
		valuesList.add("testValue1");
		testData.put("values", valuesList);
		JsonUtilityService.JSONObject jsonObject = jsonUtilityService.createJSONObject(new JSONObject(testData).toString());
		//test
		Matcher matcher = Matcher.matcherWithJsonObject(jsonObject);
		//verify
		assertNotNull(matcher);
		assertEquals(MatcherStartsWith.class, matcher.getClass());
	}

	@Test
	public void constructorEndsWith() {
		//setup
		Map<String, Object> testData = new HashMap<String, Object>();
		testData.put("key", "blah");
		testData.put("matcher", "ew");
		List<String> valuesList = new ArrayList<String>();
		valuesList.add("testValue1");
		testData.put("values", valuesList);
		JsonUtilityService.JSONObject jsonObject = jsonUtilityService.createJSONObject(new JSONObject(testData).toString());
		//test
		Matcher matcher = Matcher.matcherWithJsonObject(jsonObject);
		//verify
		assertNotNull(matcher);
		assertEquals(MatcherEndsWith.class, matcher.getClass());
	}

	@Test
	public void constructorExists() {
		//setup
		Map<String, Object> testData = new HashMap<String, Object>();
		testData.put("key", "blah");
		testData.put("matcher", "ex");
		List<String> valuesList = new ArrayList<String>();
		valuesList.add("testValue1");
		testData.put("values", valuesList);
		JsonUtilityService.JSONObject jsonObject = jsonUtilityService.createJSONObject(new JSONObject(testData).toString());
		//test
		Matcher matcher = Matcher.matcherWithJsonObject(jsonObject);
		//verify
		assertNotNull(matcher);
		assertEquals(MatcherExists.class, matcher.getClass());
	}

	@Test
	public void constructorNotExists() {
		//setup
		Map<String, Object> testData = new HashMap<String, Object>();
		testData.put("key", "blah");
		testData.put("matcher", "nx");
		List<String> valuesList = new ArrayList<String>();
		valuesList.add("testValue1");
		testData.put("values", valuesList);
		JsonUtilityService.JSONObject jsonObject = jsonUtilityService.createJSONObject(new JSONObject(testData).toString());
		//test
		Matcher matcher = Matcher.matcherWithJsonObject(jsonObject);
		//verify
		assertNotNull(matcher);
		assertEquals(MatcherNotExists.class, matcher.getClass());
	}

	@Test
	public void constructorUnknown() {
		//setup
		Map<String, Object> testData = new HashMap<String, Object>();
		testData.put("key", "blah");
		testData.put("matcher", "UNKNOWN");
		List<String> valuesList = new ArrayList<String>();
		valuesList.add("testValue1");
		testData.put("values", valuesList);
		JsonUtilityService.JSONObject jsonObject = jsonUtilityService.createJSONObject(new JSONObject(testData).toString());
		//test
		Matcher matcher = Matcher.matcherWithJsonObject(jsonObject);
		//verify
		assertNotNull(matcher);
		assertEquals(MatcherUnknown.class, matcher.getClass());
	}

	@Test
	public void constructorUnknownNoMatchesParameter() {
		//setup
		Map<String, Object> testData = new HashMap<String, Object>();
		testData.put("key", "blah");
		List<String> valuesList = new ArrayList<String>();
		valuesList.add("testValue1");
		testData.put("values", valuesList);
		JsonUtilityService.JSONObject jsonObject = jsonUtilityService.createJSONObject(new JSONObject(testData).toString());
		//test
		Matcher matcher = Matcher.matcherWithJsonObject(jsonObject);
		//verify
		assertNotNull(matcher);
		assertEquals(MatcherUnknown.class, matcher.getClass());
	}

	@Test
	public void constructorMapsKeyProperly() {
		//setup
		Map<String, Object> testData = new HashMap<String, Object>();
		testData.put("key", "blah");
		List<String> valuesList = new ArrayList<String>();
		valuesList.add("testValue1");
		testData.put("values", valuesList);
		JsonUtilityService.JSONObject jsonObject = jsonUtilityService.createJSONObject(new JSONObject(testData).toString());
		//test
		Matcher matcher = Matcher.matcherWithJsonObject(jsonObject);
		//verify
		assertNotNull(matcher);
		assertNotNull(matcher.key);
		assertEquals("blah", matcher.key);
	}

	@Test
	public void constructorMapsValuesProperly() {
		//setup
		Map<String, Object> testData = new HashMap<String, Object>();
		testData.put("key", "blah");
		testData.put("matcher", "UNKNOWN");
		List<String> valuesList = new ArrayList<String>();
		valuesList.add("testValue1");
		valuesList.add("testValue2");
		testData.put("values", valuesList);
		JsonUtilityService.JSONObject jsonObject = jsonUtilityService.createJSONObject(new JSONObject(testData).toString());
		//test
		Matcher matcher = Matcher.matcherWithJsonObject(jsonObject);
		//verify
		assertNotNull(matcher);
		assertNotNull(matcher.values);
		assertEquals("testValue1", matcher.values.get(0));
		assertEquals("testValue2", matcher.values.get(1));
	}

	@Test
	public void constructorMapsNullKeyProperly() {
		//setup
		Map<String, Object> testData = new HashMap<String, Object>();
		testData.put("key", null);
		testData.put("matcher", "UNKNOWN");
		List<String> valuesList = new ArrayList<String>();
		valuesList.add("testValue1");
		testData.put("values", valuesList);
		JsonUtilityService.JSONObject jsonObject = jsonUtilityService.createJSONObject(new JSONObject(testData).toString());
		//test
		Matcher matcher = Matcher.matcherWithJsonObject(jsonObject);
		//verify
		assertNotNull(matcher);
		assertNull(matcher.key);
	}

	@Test
	public void constructorMapsNullValuesProperly() {
		//setup
		Map<String, Object> testData = new HashMap<String, Object>();
		testData.put("key", "blah");
		testData.put("matcher", "UNKNOWN");
		testData.put("values", null);
		JsonUtilityService.JSONObject jsonObject = jsonUtilityService.createJSONObject(new JSONObject(testData).toString());
		//test
		Matcher matcher = Matcher.matcherWithJsonObject(jsonObject);
		//verify
		assertNotNull(matcher);
		assertNotNull(matcher.values);
		assertEquals(0, matcher.values.size());
	}

	// ================================================================================
	// matcher functionality
	// ================================================================================
	private boolean runTestMatcher(Matcher matcher, Object value, List<Object> values) {
		matcher.key = "testKey";
		matcher.values = new ArrayList<Object>();
		matcher.values.addAll(values);

		EventData testEventData = new EventData();
		testEventData.putObject("testKey", value);
		Event testEvent = new Event.Builder("test", EventType.ANALYTICS,
											EventSource.RESPONSE_CONTENT).setData(testEventData).build();
		return matcher.matches(ruleTokenParser.expandKey(matcher.key, testEvent));
	}

	private String generateToStringExpected(Matcher matcher, String key, ArrayList<Object> values) {
		String string = "%s %s %s";
		StringBuilder sb = new StringBuilder();

		String matcherString = null;

		if (matcher instanceof MatcherNotExists) {
			return String.format("(%s NOT EXISTS)", key);
		} else if (matcher instanceof MatcherExists) {
			return String.format("(%s EXISTS)", key);
		}

		if (matcher instanceof MatcherStartsWith) {
			matcherString = "STARTS WITH";
		}

		if (matcher instanceof MatcherEndsWith) {
			matcherString = "ENDS WITH";
		}

		if (matcher instanceof MatcherContains) {
			matcherString = "CONTAINS";
		}

		if (matcher instanceof MatcherNotContains) {
			matcherString = "NOT CONTAINS";
		}

		if (matcher instanceof MatcherEquals) {
			matcherString = "EQUALS";
		}

		if (matcher instanceof MatcherNotEquals) {
			matcherString = "NOT EQUALS";
		}

		if (matcher instanceof MatcherLessThan) {
			matcherString = "LESS THAN";
		}

		if (matcher instanceof MatcherGreaterThan) {
			matcherString = "GREATER THAN";
		}

		if (matcher instanceof  MatcherGreaterThanOrEqual) {
			matcherString = "GREATER THAN OR EQUALS";
		}

		if (matcher instanceof MatcherLessThanOrEqual) {
			matcherString = "LESS THAN OR EQUALS";
		}

		ArrayList<Object> v = (values == null ? new ArrayList<Object>() : values);

		if (v.isEmpty()) {
			v = new ArrayList<Object>();
			v.add("<NOTHING SPECIFIED>");
		}

		for (Object s : v) {
			if (sb.length() > 0) {
				sb.append(" OR ");
			}

			sb.append(String.format(string, key, matcherString, s));
		}

		sb.insert(0, "(");
		sb.append(")");

		return sb.toString();

	}

	// ================================================================================
	// MatcherEquals
	// ================================================================================
	// string
	@Test
	public void equalsStringShouldPerformSingleMatchPass() {
		//setup
		MatcherEquals testMatcher = new MatcherEquals();
		List<Object> values = new ArrayList<Object>();
		values.add("test");
		//test
		assertTrue(runTestMatcher(testMatcher, "test", values));
	}

	@Test
	public void equalsStringShouldPerformSingleMatchFail() {
		MatcherEquals testMatcher = new MatcherEquals();
		List<Object> values = new ArrayList<Object>();
		values.add("test");
		assertFalse(runTestMatcher(testMatcher, "testingBadMatch", values));
	}

	@Test
	public void equalsStringShouldPerformMultipleMatchPass() {
		MatcherEquals testMatcher = new MatcherEquals();
		List<Object> values = new ArrayList<Object>();
		values.add("nope");
		values.add("stillnope");
		values.add("nothing to see here");
		values.add("tests");
		values.add("test");
		assertTrue(runTestMatcher(testMatcher, "test", values));
	}

	@Test
	public void equalsStringShouldPerformMultipleMatchFail() {
		MatcherEquals testMatcher = new MatcherEquals();
		List<Object> values = new ArrayList<Object>();
		values.add("nope");
		values.add("stillnope");
		values.add("nothing to see here");
		values.add("tests");
		values.add("testingBadMatch");
		assertFalse(runTestMatcher(testMatcher, "test", values));
	}

	@Test
	public void equalsStringShouldPerformSingleMatchPassCaseInsensitive() {
		MatcherEquals testMatcher = new MatcherEquals();
		List<Object> values = new ArrayList<Object>();
		values.add("test");
		assertTrue(runTestMatcher(testMatcher, "teSt", values));
	}

	@Test
	public void equalsStringShouldPerformMultipleMatchPassCaseInsensitive() {
		MatcherEquals testMatcher = new MatcherEquals();
		List<Object> values = new ArrayList<Object>();
		values.add("nope");
		values.add("stillnope");
		values.add("nothing to see here");
		values.add("tests");
		values.add("testingBadMatch");
		assertTrue(runTestMatcher(testMatcher, "tesTS", values));
	}

	@Test
	public void equalsStringShouldHandleIncorrectTypes() {
		MatcherEquals testMatcher = new MatcherEquals();
		List<Object> values = new ArrayList<Object>();
		values.add("test");
		assertFalse(runTestMatcher(testMatcher, 552, values));
	}

	// numeric
	@Test
	public void equalsNumericShouldPerformSingleMatchPass() {
		MatcherEquals testMatcher = new MatcherEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(552);
		assertTrue(runTestMatcher(testMatcher, 552, values));
	}

	@Test
	public void equalsNumericShouldPerformSingleMatchFail() {
		MatcherEquals testMatcher = new MatcherEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(553);
		assertFalse(runTestMatcher(testMatcher, 552, values));
	}

	@Test
	public void equalsNumericShouldPerformSinglePrecisionMatchPass() {
		MatcherEquals testMatcher = new MatcherEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(552.323);
		assertTrue(runTestMatcher(testMatcher, 552.323, values));
	}

	@Test
	public void equalsNumericShouldPerformSinglePrecisionMatchFail() {
		MatcherEquals testMatcher = new MatcherEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(552.323);
		assertFalse(runTestMatcher(testMatcher, 552.3, values));
	}

	@Test
	public void equalsNumericShouldPerformMultipleMatchPass() {
		MatcherEquals testMatcher = new MatcherEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(552);
		values.add(551);
		values.add(5544);
		values.add(557);
		assertTrue(runTestMatcher(testMatcher, 552, values));
	}

	@Test
	public void equalsNumericShouldPerformMultipleMatchFail() {
		MatcherEquals testMatcher = new MatcherEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(552);
		values.add(551);
		values.add(5544);
		values.add(557);
		assertFalse(runTestMatcher(testMatcher, 550, values));
	}

	@Test
	public void equalsNumericShouldPerformMultiplePrecisionMatchPass() {
		MatcherEquals testMatcher = new MatcherEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(552.222);
		values.add(551.654);
		values.add(5544.343);
		values.add(557.8755);
		assertTrue(runTestMatcher(testMatcher, 552.222, values));
	}

	@Test
	public void equalsNumericShouldPerformMultiplePrecisionMatchFail() {
		MatcherEquals testMatcher = new MatcherEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(552.222);
		values.add(551.654);
		values.add(5544.343);
		values.add(557.8755);
		assertFalse(runTestMatcher(testMatcher, 552.888, values));
	}

	@Test
	public void equalsNumericShouldPerformSingleMatchWithString() {
		MatcherEquals testMatcher = new MatcherEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(552);
		assertTrue(runTestMatcher(testMatcher, "552", values));
	}

	@Test
	public void equalsNumericShouldNotCrashWithStringThatIsNotANumber() {
		MatcherEquals testMatcher = new MatcherEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(552);
		assertFalse(runTestMatcher(testMatcher, "sdfsd", values));
	}

	@Test
	public void equalsNumericShouldHandleIncorrectTypesMatched() {
		MatcherEquals testMatcher = new MatcherEquals();
		List<Object> values = new ArrayList<Object>();
		values.add("552");
		assertTrue(runTestMatcher(testMatcher, 552, values));
	}

	@Test
	public void equalsNumericShouldHandleIncorrectTypesNotMatched() {
		MatcherEquals testMatcher = new MatcherEquals();
		List<Object> values = new ArrayList<Object>();
		values.add("554");
		assertFalse(runTestMatcher(testMatcher, 552, values));
	}

	@Test
	public void equalsToString_When_ValidData_Then_ReturnStringValue() {
		MatcherEquals testMatcher = new MatcherEquals();
		testMatcher.key = "key";
		testMatcher.values = new ArrayList<Object>();
		testMatcher.values.add("down");
		testMatcher.values.add("hammer");
		testMatcher.values.add("stahp");
		String expected = generateToStringExpected(testMatcher, testMatcher.key, testMatcher.values);
		assertEquals(expected, testMatcher.toString());
	}

	@Test
	public void equalsToString_When_NullKey_Then_ReturnStringWithDefaults() {
		MatcherEquals testMatcher = new MatcherEquals();
		testMatcher.values = new ArrayList<Object>();
		testMatcher.values.add("down");
		testMatcher.values.add("hammer");
		testMatcher.values.add("stahp");
		String expected = generateToStringExpected(testMatcher, testMatcher.key, testMatcher.values);
		assertEquals(expected, testMatcher.toString());
	}

	@Test
	public void equalsBooleanTrueShouldEqualsValueBooleanTrue() {
		MatcherEquals testMatcher = new MatcherEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(true);
		assertTrue(runTestMatcher(testMatcher, true, values));
	}

	@Test
	public void equalsBooleanFalseShouldEqualsValueBooleanFalse() {
		MatcherEquals testMatcher = new MatcherEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(false);
		assertTrue(runTestMatcher(testMatcher, false, values));
	}

	@Test
	public void equalsBooleanTrueShouldNotEqualsValueBooleanFalse() {
		MatcherEquals testMatcher = new MatcherEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(true);
		assertFalse(runTestMatcher(testMatcher, false, values));
	}

	@Test
	public void equalsBooleanTrueShouldEqualsValueStringTrue() {
		MatcherEquals testMatcher = new MatcherEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(true);
		assertTrue(runTestMatcher(testMatcher, "true", values));
	}

	@Test
	public void equalsBooleanTrueShouldEqualsValueStringTrueUpperCase() {
		MatcherEquals testMatcher = new MatcherEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(true);
		assertTrue(runTestMatcher(testMatcher, "TRUE", values));
	}

	@Test
	public void equalsBooleanTrueShouldEqualsValueString1() {
		MatcherEquals testMatcher = new MatcherEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(true);
		assertTrue(runTestMatcher(testMatcher, "1", values));
	}

	@Test
	public void equalsBooleanFalseShouldEqualsValueString1() {
		MatcherEquals testMatcher = new MatcherEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(false);
		assertTrue(runTestMatcher(testMatcher, "0", values));
	}

	@Test
	public void equalsBooleanFalseShouldNotEqualsValueStringABC() {
		MatcherEquals testMatcher = new MatcherEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(false);
		assertFalse(runTestMatcher(testMatcher, "abc", values));
	}

	@Test
	public void equalsBooleanTrueShouldNotEqualsValueStringABC() {
		MatcherEquals testMatcher = new MatcherEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(true);
		assertFalse(runTestMatcher(testMatcher, "abc", values));
	}

	@Test
	public void equalsBooleanTrueShouldEqualsValueInt1() {
		MatcherEquals testMatcher = new MatcherEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(true);
		assertTrue(runTestMatcher(testMatcher, 1, values));
	}

	@Test
	public void equalsBooleanTrueShouldEqualsValueLong1() {
		MatcherEquals testMatcher = new MatcherEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(true);
		assertTrue(runTestMatcher(testMatcher, 1l, values));
	}

	@Test
	public void equalsBooleanTrueShouldNotEqualsValueFloat1() {
		MatcherEquals testMatcher = new MatcherEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(true);
		assertFalse(runTestMatcher(testMatcher, 1f, values));
	}

	@Test
	public void equalsBooleanFalseShouldEqualsValueInt0() {
		MatcherEquals testMatcher = new MatcherEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(false);
		assertTrue(runTestMatcher(testMatcher, 0, values));
	}

	@Test
	public void equalsBooleanFalseShouldEqualsValueLong0() {
		MatcherEquals testMatcher = new MatcherEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(false);
		assertTrue(runTestMatcher(testMatcher, 0l, values));
	}

	@Test
	public void equalsBooleanFalseShouldNotEqualsValueFloat0() {
		MatcherEquals testMatcher = new MatcherEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(false);
		assertFalse(runTestMatcher(testMatcher, 0f, values));
	}

	@Test
	public void equalsBooleanTrueShouldNotEqualsValueInt2() {
		MatcherEquals testMatcher = new MatcherEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(true);
		assertFalse(runTestMatcher(testMatcher, 2, values));
	}

	@Test
	public void equalsBooleanFalseShouldNotEqualsValueInt2() {
		MatcherEquals testMatcher = new MatcherEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(false);
		assertFalse(runTestMatcher(testMatcher, 2f, values));
	}

	// ================================================================================
	// MatcherNotEquals
	// ================================================================================
	// string
	@Test
	public void notEqualsStringShouldPerformSingleMatchPass() {
		MatcherNotEquals testMatcher = new MatcherNotEquals();
		List<Object> values = new ArrayList<Object>();
		values.add("test");
		assertTrue(runTestMatcher(testMatcher, "test not match", values));
	}

	@Test
	public void notEqualsStringShouldPerformSingleMatchFail() {
		MatcherNotEquals testMatcher = new MatcherNotEquals();
		List<Object> values = new ArrayList<Object>();
		values.add("test");
		assertFalse(runTestMatcher(testMatcher, "test", values));
	}

	@Test
	public void notEqualsStringShouldPerformMultipleMatchPass() {
		MatcherNotEquals testMatcher = new MatcherNotEquals();
		List<Object> values = new ArrayList<Object>();
		values.add("test");
		values.add("testing more");
		values.add("still test");
		values.add("test the things");
		values.add("still not matching");
		assertTrue(runTestMatcher(testMatcher, "test not match", values));
	}

	@Test
	public void notEqualsStringShouldPerformMultipleMatchFail() {
		MatcherNotEquals testMatcher = new MatcherNotEquals();
		List<Object> values = new ArrayList<Object>();
		values.add("test");
		values.add("testing more");
		values.add("still test");
		values.add("test the things");
		values.add("still not matching");
		assertFalse(runTestMatcher(testMatcher, "still not matching", values));
	}

	@Test
	public void notEqualsStringShouldPerformSingleMatchPassCaseInsensitive() {
		MatcherNotEquals testMatcher = new MatcherNotEquals();
		List<Object> values = new ArrayList<Object>();
		values.add("test");
		assertFalse(runTestMatcher(testMatcher, "TeST", values));
	}

	@Test
	public void notEqualsStringShouldPerformMultipleMatchPassCaseInsensitive() {
		MatcherNotEquals testMatcher = new MatcherNotEquals();
		List<Object> values = new ArrayList<Object>();
		values.add("test");
		values.add("testing more");
		values.add("still test");
		values.add("test the things");
		values.add("still not matching");
		assertFalse(runTestMatcher(testMatcher, "tEsT", values));
	}

	// numeric
	@Test
	public void notEqualsNumericShouldPerformSingleMatchPass() {
		MatcherNotEquals testMatcher = new MatcherNotEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(552);
		assertFalse(runTestMatcher(testMatcher, 552, values));
	}

	@Test
	public void notEqualsNumericShouldPerformSingleMatchFail() {
		MatcherNotEquals testMatcher = new MatcherNotEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(553);
		assertTrue(runTestMatcher(testMatcher, 552, values));
	}

	@Test
	public void notEqualsNumericShouldPerformSinglePrecisionMatchPass() {
		MatcherNotEquals testMatcher = new MatcherNotEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(552.323);
		assertFalse(runTestMatcher(testMatcher, 552.323, values));
	}

	@Test
	public void notEqualsNumericShouldPerformSinglePrecisionMatchFail() {
		MatcherNotEquals testMatcher = new MatcherNotEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(552.323);
		assertTrue(runTestMatcher(testMatcher, 552.322, values));
	}

	@Test
	public void notEqualsNumericShouldPerformMultipleMatchPass() {
		MatcherNotEquals testMatcher = new MatcherNotEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(550);
		values.add(551);
		values.add(553);
		values.add(554);
		values.add(552);
		assertFalse(runTestMatcher(testMatcher, 552, values));
	}

	@Test
	public void notEqualsNumericShouldPerformMultipleMatchFail() {
		MatcherNotEquals testMatcher = new MatcherNotEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(550);
		values.add(551);
		values.add(553);
		values.add(554);
		values.add(555);
		assertTrue(runTestMatcher(testMatcher, 552, values));
	}

	@Test
	public void notEqualsNumericShouldPerformMultiplePrecisionMatchPass() {
		MatcherNotEquals testMatcher = new MatcherNotEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(552.111);
		values.add(552.222);
		values.add(552.333);
		values.add(552.444);
		values.add(552.555);
		assertFalse(runTestMatcher(testMatcher, 552.555, values));
	}

	@Test
	public void notEqualsNumericShouldPerformMultiplePrecisionMatchFail() {
		MatcherNotEquals testMatcher = new MatcherNotEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(552.111);
		values.add(552.222);
		values.add(552.333);
		values.add(552.444);
		values.add(552.55);
		assertTrue(runTestMatcher(testMatcher, 552.555, values));
	}

	@Test
	public void notEqualsNumericShouldPerformSingleMatchWithString() {
		MatcherNotEquals testMatcher = new MatcherNotEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(552);
		assertFalse(runTestMatcher(testMatcher, "552", values));
	}

	@Test
	public void notEqualsNumericShouldNotCrashWithStringThatIsNotANumber() {
		MatcherNotEquals testMatcher = new MatcherNotEquals();
		List<Object> values = new ArrayList<Object>();
		values.add(552);
		assertTrue(runTestMatcher(testMatcher, "not a number", values));
	}

	@Test
	public void notEqualsNumericShouldHandleIncorrectTypesMatched() {
		MatcherNotEquals testMatcher = new MatcherNotEquals();
		List<Object> values = new ArrayList<Object>();
		values.add("552");
		assertFalse(runTestMatcher(testMatcher, 552, values));
	}

	@Test
	public void notEqualsNumericShouldHandleIncorrectTypesNotMatched() {
		MatcherNotEquals testMatcher = new MatcherNotEquals();
		List<Object> values = new ArrayList<Object>();
		values.add("554");
		assertTrue(runTestMatcher(testMatcher, 552, values));
	}

	@Test
	public void notEqualsToString_When_ValidData_Then_ReturnStringValue() {
		MatcherNotEquals testMatcher = new MatcherNotEquals();
		testMatcher.key = "key";
		testMatcher.values = new ArrayList<Object>();
		testMatcher.values.add("down");
		testMatcher.values.add("hammer");
		testMatcher.values.add("stahp");
		String expected = generateToStringExpected(testMatcher, testMatcher.key, testMatcher.values);
		assertEquals(expected, testMatcher.toString());
	}

	@Test
	public void notEqualsToString_When_NullKey_Then_ReturnStringWithDefaults() {
		MatcherNotEquals testMatcher = new MatcherNotEquals();
		testMatcher.values = new ArrayList<Object>();
		testMatcher.values.add("down");
		testMatcher.values.add("hammer");
		testMatcher.values.add("stahp");
		String expected = generateToStringExpected(testMatcher, testMatcher.key, testMatcher.values);
		assertEquals(expected, testMatcher.toString());
	}


	// ================================================================================
	// MatcherGreaterThan
	// ================================================================================
	@Test
	public void greaterThanShouldMatchCorrectly() {
		MatcherGreaterThan testMatcher = new MatcherGreaterThan();
		List<Object> values = new ArrayList<Object>();
		values.add(552);
		assertTrue(runTestMatcher(testMatcher, 553, values));
	}

	@Test
	public void greaterThanShouldFailCorrectly() {
		MatcherGreaterThan testMatcher = new MatcherGreaterThan();
		List<Object> values = new ArrayList<Object>();
		values.add(552);
		assertFalse(runTestMatcher(testMatcher, 551, values));
	}

	@Test
	public void greaterThanShouldNotMatchEquals() {
		MatcherGreaterThan testMatcher = new MatcherGreaterThan();
		List<Object> values = new ArrayList<Object>();
		values.add(552);
		assertFalse(runTestMatcher(testMatcher, 552, values));
	}

	@Test
	public void greaterThanShouldMatchMultiplesCorrectly() {
		MatcherGreaterThan testMatcher = new MatcherGreaterThan();
		List<Object> values = new ArrayList<Object>();
		values.add(552);
		values.add(554);
		values.add(555);
		values.add(556);
		values.add(553);
		assertTrue(runTestMatcher(testMatcher, 553, values));
	}

	@Test
	public void greaterThanShouldFailMultiplesCorrectly() {
		MatcherGreaterThan testMatcher = new MatcherGreaterThan();
		List<Object> values = new ArrayList<Object>();
		values.add(554);
		values.add(555);
		values.add(556);
		values.add(553);
		assertFalse(runTestMatcher(testMatcher, 553, values));
	}

	@Test
	public void greaterThanShouldNotMatchMultipleEquals() {
		MatcherGreaterThan testMatcher = new MatcherGreaterThan();
		List<Object> values = new ArrayList<Object>();
		values.add(552);
		values.add(552);
		values.add(552);
		values.add(552);
		assertFalse(runTestMatcher(testMatcher, 552, values));
	}

	@Test
	public void greaterThanShouldMatchPrecisionCorrectly() {
		MatcherGreaterThan testMatcher = new MatcherGreaterThan();
		List<Object> values = new ArrayList<Object>();
		values.add(552.552);
		assertTrue(runTestMatcher(testMatcher, 552.553, values));
	}

	@Test
	public void greaterThanShouldFailPrecisionCorrectly() {
		MatcherGreaterThan testMatcher = new MatcherGreaterThan();
		List<Object> values = new ArrayList<Object>();
		values.add(552.552);
		assertFalse(runTestMatcher(testMatcher, 552.551, values));
	}

	@Test
	public void greaterThanShouldNotMatchPrecisionEquals() {
		MatcherGreaterThan testMatcher = new MatcherGreaterThan();
		List<Object> values = new ArrayList<Object>();
		values.add(552.552);
		assertFalse(runTestMatcher(testMatcher, 552.552, values));
	}

	@Test
	public void greaterThanShouldMatchPrecisionMultiplesCorrectly() {
		MatcherGreaterThan testMatcher = new MatcherGreaterThan();
		List<Object> values = new ArrayList<Object>();
		values.add(552.553);
		values.add(552.554);
		values.add(552.555);
		values.add(552.556);
		values.add(552.551);
		assertTrue(runTestMatcher(testMatcher, 552.552, values));
	}

	@Test
	public void greaterThanShouldFailPrecisionMultiplesCorrectly() {
		MatcherGreaterThan testMatcher = new MatcherGreaterThan();
		List<Object> values = new ArrayList<Object>();
		values.add(552.553);
		values.add(552.554);
		values.add(552.555);
		values.add(552.556);
		assertFalse(runTestMatcher(testMatcher, 552.552, values));
	}

	@Test
	public void greaterThanShouldConvertStringsWhenPossibleFail() {
		MatcherGreaterThan testMatcher = new MatcherGreaterThan();
		List<Object> values = new ArrayList<Object>();
		values.add(552);
		assertFalse(runTestMatcher(testMatcher, "551", values));
	}

	@Test
	public void greaterThanShouldConvertStringsWhenPossibleMatch() {
		MatcherGreaterThan testMatcher = new MatcherGreaterThan();
		List<Object> values = new ArrayList<Object>();
		values.add(552);
		assertTrue(runTestMatcher(testMatcher, "553", values));
	}

	@Test
	public void greaterThanShouldFailGracefulllyWithStringArray() {
		MatcherGreaterThan testMatcher = new MatcherGreaterThan();
		List<Object> values = new ArrayList<Object>();
		values.add("iamastring");
		assertFalse(runTestMatcher(testMatcher, 552, values));
	}

	@Test
	public void greaterThanShouldFailGracefulllyWithStringInput() {
		MatcherGreaterThan testMatcher = new MatcherGreaterThan();
		List<Object> values = new ArrayList<Object>();
		values.add(552);
		assertFalse(runTestMatcher(testMatcher, "iamastring", values));
	}

	@Test
	public void greaterThanToString_When_ValidData_Then_ReturnStringValue() {
		MatcherGreaterThan testMatcher = new MatcherGreaterThan();
		testMatcher.key = "key";
		testMatcher.values = new ArrayList<Object>();
		testMatcher.values.add("down");
		testMatcher.values.add("hammer");
		testMatcher.values.add("stahp");
		String expected = generateToStringExpected(testMatcher, testMatcher.key, testMatcher.values);
		assertEquals(expected, testMatcher.toString());
	}

	@Test
	public void greaterThanToString_When_NullKey_Then_ReturnStringWithDefaults() {
		MatcherGreaterThan testMatcher = new MatcherGreaterThan();
		testMatcher.values = new ArrayList<Object>();
		testMatcher.values.add("down");
		testMatcher.values.add("hammer");
		testMatcher.values.add("stahp");
		String expected = generateToStringExpected(testMatcher, testMatcher.key, testMatcher.values);
		assertEquals(expected, testMatcher.toString());
	}


	// ================================================================================
	// MatcherGreaterThanOrEqual
	// ================================================================================
	@Test
	public void greaterThanEqShouldMatchCorrectly() {
		MatcherGreaterThanOrEqual testMatcher = new MatcherGreaterThanOrEqual();
		List<Object> values = new ArrayList<Object>();
		values.add(552);
		assertTrue(runTestMatcher(testMatcher, 553, values));
	}

	@Test
	public void greaterThanEqShouldFailCorrectly() {
		MatcherGreaterThanOrEqual testMatcher = new MatcherGreaterThanOrEqual();
		List<Object> values = new ArrayList<Object>();
		values.add(552);
		assertFalse(runTestMatcher(testMatcher, 551, values));
	}

	@Test
	public void greaterThanEqShouldMatchEquals() {
		MatcherGreaterThanOrEqual testMatcher = new MatcherGreaterThanOrEqual();
		List<Object> values = new ArrayList<Object>();
		values.add(552);
		assertTrue(runTestMatcher(testMatcher, 552, values));
	}

	@Test
	public void greaterThanEqShouldMatchMultiplesCorrectly() {
		MatcherGreaterThanOrEqual testMatcher = new MatcherGreaterThanOrEqual();
		List<Object> values = new ArrayList<Object>();
		values.add(554);
		values.add(555);
		values.add(556);
		values.add(557);
		values.add(553);
		assertTrue(runTestMatcher(testMatcher, 553, values));
	}

	@Test
	public void greaterThanEqShouldFailMultiplesCorrectly() {
		MatcherGreaterThanOrEqual testMatcher = new MatcherGreaterThanOrEqual();
		List<Object> values = new ArrayList<Object>();
		values.add(554);
		values.add(555);
		values.add(556);
		values.add(557);
		values.add(558);
		assertFalse(runTestMatcher(testMatcher, 553, values));
	}

	@Test
	public void greaterThanEqShouldMatchMultipleEquals() {
		MatcherGreaterThanOrEqual testMatcher = new MatcherGreaterThanOrEqual();
		List<Object> values = new ArrayList<Object>();
		values.add(552);
		values.add(552);
		values.add(552);
		values.add(552);
		assertTrue(runTestMatcher(testMatcher, 552, values));
	}

	@Test
	public void greaterThanEqShouldMatchPrecisionCorrectly() {
		MatcherGreaterThanOrEqual testMatcher = new MatcherGreaterThanOrEqual();
		List<Object> values = new ArrayList<Object>();
		values.add(552.552);
		assertTrue(runTestMatcher(testMatcher, 552.553, values));
	}

	@Test
	public void greaterThanEqShouldFailPrecisionCorrectly() {
		MatcherGreaterThanOrEqual testMatcher = new MatcherGreaterThanOrEqual();
		List<Object> values = new ArrayList<Object>();
		values.add(552.552);
		assertFalse(runTestMatcher(testMatcher, 552.551, values));
	}

	@Test
	public void greaterThanEqShouldMatchPrecisionEquals() {
		MatcherGreaterThanOrEqual testMatcher = new MatcherGreaterThanOrEqual();
		List<Object> values = new ArrayList<Object>();
		values.add(552.552);
		assertTrue(runTestMatcher(testMatcher, 552.552, values));
	}

	@Test
	public void greaterThanEqShouldMatchPrecisionMultiplesCorrectly() {
		MatcherGreaterThanOrEqual testMatcher = new MatcherGreaterThanOrEqual();
		List<Object> values = new ArrayList<Object>();
		values.add(552.553);
		values.add(552.554);
		values.add(552.555);
		values.add(552.556);
		values.add(552.552);
		assertTrue(runTestMatcher(testMatcher, 552.552, values));
	}

	@Test
	public void greaterThanEqShouldFailPrecisionMultiplesCorrectly() {
		MatcherGreaterThanOrEqual testMatcher = new MatcherGreaterThanOrEqual();
		List<Object> values = new ArrayList<Object>();
		values.add(552.553);
		values.add(552.554);
		values.add(552.555);
		values.add(552.556);
		values.add(552.557);
		assertFalse(runTestMatcher(testMatcher, 552.552, values));
	}

	@Test
	public void greaterThanEqShouldConvertStringsWhenPossibleMatch() {
		MatcherGreaterThanOrEqual testMatcher = new MatcherGreaterThanOrEqual();
		List<Object> values = new ArrayList<Object>();
		values.add(552);
		assertTrue(runTestMatcher(testMatcher, "553", values));
	}

	@Test
	public void greaterThanEqShouldConvertStringsWhenPossibleFail() {
		MatcherGreaterThanOrEqual testMatcher = new MatcherGreaterThanOrEqual();
		List<Object> values = new ArrayList<Object>();
		values.add(552);
		assertFalse(runTestMatcher(testMatcher, "551", values));
	}

	@Test
	public void greaterThanEqShouldFailGracefulllyWithStringArray() {
		MatcherGreaterThanOrEqual testMatcher = new MatcherGreaterThanOrEqual();
		List<Object> values = new ArrayList<Object>();
		values.add("iamastring");
		assertFalse(runTestMatcher(testMatcher, 552, values));
	}

	@Test
	public void greaterThanEqShouldFailGracefulllyWithStringInput() {
		MatcherGreaterThanOrEqual testMatcher = new MatcherGreaterThanOrEqual();
		List<Object> values = new ArrayList<Object>();
		values.add(552);
		assertFalse(runTestMatcher(testMatcher, "iamastring", values));
	}

	@Test
	public void greaterThanOrEqualToString_When_ValidData_Then_ReturnStringValue() {
		MatcherGreaterThanOrEqual testMatcher = new MatcherGreaterThanOrEqual();
		testMatcher.key = "key";
		testMatcher.values = new ArrayList<Object>();
		testMatcher.values.add("down");
		testMatcher.values.add("hammer");
		testMatcher.values.add("stahp");
		String expected = generateToStringExpected(testMatcher, testMatcher.key, testMatcher.values);
		assertEquals(expected, testMatcher.toString());
	}

	@Test
	public void greaterThanOrEqualToString_When_NullKey_Then_ReturnStringWithDefaults() {
		MatcherGreaterThanOrEqual testMatcher = new MatcherGreaterThanOrEqual();
		testMatcher.values = new ArrayList<Object>();
		testMatcher.values.add("down");
		testMatcher.values.add("hammer");
		testMatcher.values.add("stahp");
		String expected = generateToStringExpected(testMatcher, testMatcher.key, testMatcher.values);
		assertEquals(expected, testMatcher.toString());
	}



	// ================================================================================
	// MatcherLessThan
	// ================================================================================
	@Test
	public void lessThanShouldMatchCorrectly() {
		MatcherLessThan testMatcher = new MatcherLessThan();
		List<Object> values = new ArrayList<Object>();
		values.add(552);
		assertTrue(runTestMatcher(testMatcher, 551, values));
	}

	@Test
	public void lessThanShouldFailCorrectly() {
		MatcherLessThan testMatcher = new MatcherLessThan();
		List<Object> values = new ArrayList<Object>();
		values.add(552);
		assertFalse(runTestMatcher(testMatcher, 553, values));
	}

	@Test
	public void lessThanShouldNotMatchEquals() {
		MatcherLessThan testMatcher = new MatcherLessThan();
		List<Object> values = new ArrayList<Object>();
		values.add(552);
		assertFalse(runTestMatcher(testMatcher, 552, values));
	}

	@Test
	public void lessThanShouldMatchMultiplesCorrectly() {
		MatcherLessThan testMatcher = new MatcherLessThan();
		List<Object> values = new ArrayList<Object>();
		values.add(549);
		values.add(550);
		values.add(551);
		values.add(552);
		values.add(554);
		assertTrue(runTestMatcher(testMatcher, 553, values));
	}

	@Test
	public void lessThanShouldFailMultiplesCorrectly() {
		MatcherLessThan testMatcher = new MatcherLessThan();
		List<Object> values = new ArrayList<Object>();
		values.add(549);
		values.add(550);
		values.add(551);
		values.add(552);
		values.add(553);
		assertFalse(runTestMatcher(testMatcher, 553, values));
	}

	@Test
	public void lessThanShouldMatchPrecisionCorrectly() {
		MatcherLessThan testMatcher = new MatcherLessThan();
		List<Object> values = new ArrayList<Object>();
		values.add(552.552);
		assertTrue(runTestMatcher(testMatcher, 552.551, values));
	}

	@Test
	public void lessThanShouldFailPrecisionCorrectly() {
		MatcherLessThan testMatcher = new MatcherLessThan();
		List<Object> values = new ArrayList<Object>();
		values.add(552.552);
		assertFalse(runTestMatcher(testMatcher, 552.553, values));
	}

	@Test
	public void lessThanShouldNotMatchPrecisionEquals() {
		MatcherLessThan testMatcher = new MatcherLessThan();
		List<Object> values = new ArrayList<Object>();
		values.add(552.552);
		assertFalse(runTestMatcher(testMatcher, 552.552, values));
	}

	@Test
	public void lessThanShouldMatchPrecisionMultiplesCorrectly() {
		MatcherLessThan testMatcher = new MatcherLessThan();
		List<Object> values = new ArrayList<Object>();
		values.add(552.549);
		values.add(552.550);
		values.add(552.551);
		values.add(552.552);
		values.add(552.553);
		assertTrue(runTestMatcher(testMatcher, 552.552, values));
	}

	@Test
	public void lessThanShouldFailPrecisionMultiplesCorrectly() {
		MatcherLessThan testMatcher = new MatcherLessThan();
		List<Object> values = new ArrayList<Object>();
		values.add(552.547);
		values.add(552.548);
		values.add(552.549);
		values.add(552.550);
		values.add(552.551);
		assertFalse(runTestMatcher(testMatcher, 552.552, values));
	}

	@Test
	public void lessThanShouldConvertStringsWhenPossibleMatch() {
		MatcherLessThan testMatcher = new MatcherLessThan();
		List<Object> values = new ArrayList<Object>();
		values.add(552);
		assertTrue(runTestMatcher(testMatcher, "551", values));
	}

	@Test
	public void lessThanShouldConvertStringsWhenPossibleFail() {
		MatcherLessThan testMatcher = new MatcherLessThan();
		List<Object> values = new ArrayList<Object>();
		values.add(552);
		assertFalse(runTestMatcher(testMatcher, "553", values));
	}

	@Test
	public void lessThanShouldFailGracefulllyWithStringArray() {
		MatcherLessThan testMatcher = new MatcherLessThan();
		List<Object> values = new ArrayList<Object>();
		values.add("iamastring");
		assertFalse(runTestMatcher(testMatcher, 552, values));
	}

	@Test
	public void lessThanShouldFailGracefulllyWithStringInput() {
		MatcherLessThan testMatcher = new MatcherLessThan();
		List<Object> values = new ArrayList<Object>();
		values.add(552);
		assertFalse(runTestMatcher(testMatcher, "iamastring", values));
	}

	@Test
	public void lessThanToString_When_ValidData_Then_ReturnStringValue() {
		MatcherLessThan testMatcher = new MatcherLessThan();
		testMatcher.key = "key";
		testMatcher.values = new ArrayList<Object>();
		testMatcher.values.add("down");
		testMatcher.values.add("hammer");
		testMatcher.values.add("stahp");
		String expected = generateToStringExpected(testMatcher, testMatcher.key, testMatcher.values);
		assertEquals(expected, testMatcher.toString());
	}

	@Test
	public void lessThanToString_When_NullKey_Then_ReturnStringWithDefaults() {
		MatcherLessThan testMatcher = new MatcherLessThan();
		testMatcher.values = new ArrayList<Object>();
		testMatcher.values.add("down");
		testMatcher.values.add("hammer");
		testMatcher.values.add("stahp");
		String expected = generateToStringExpected(testMatcher, testMatcher.key, testMatcher.values);
		assertEquals(expected, testMatcher.toString());
	}


	// ================================================================================
	// MatcherLessThanOrEqual
	// ================================================================================
	@Test
	public void lessThanEqShouldMatchCorrectly() {
		MatcherLessThanOrEqual testMatcher = new MatcherLessThanOrEqual();
		List<Object> values = new ArrayList<Object>();
		values.add(552);
		assertTrue(runTestMatcher(testMatcher, 551, values));
	}

	@Test
	public void lessThanEqShouldFailCorrectly() {
		MatcherLessThanOrEqual testMatcher = new MatcherLessThanOrEqual();
		List<Object> values = new ArrayList<Object>();
		values.add(552);
		assertFalse(runTestMatcher(testMatcher, 553, values));
	}

	@Test
	public void lessThanEqShouldMatchEquals() {
		MatcherLessThanOrEqual testMatcher = new MatcherLessThanOrEqual();
		List<Object> values = new ArrayList<Object>();
		values.add(552);
		assertTrue(runTestMatcher(testMatcher, 552, values));
	}

	@Test
	public void lessThanEqShouldMatchMultiplesCorrectly() {
		MatcherLessThanOrEqual testMatcher = new MatcherLessThanOrEqual();
		List<Object> values = new ArrayList<Object>();
		values.add(549);
		values.add(550);
		values.add(551);
		values.add(552);
		values.add(554);
		assertTrue(runTestMatcher(testMatcher, 553, values));
	}

	@Test
	public void lessThanEqShouldFailMultiplesCorrectly() {
		MatcherLessThanOrEqual testMatcher = new MatcherLessThanOrEqual();
		List<Object> values = new ArrayList<Object>();
		values.add(549);
		values.add(550);
		values.add(551);
		values.add(552);
		assertFalse(runTestMatcher(testMatcher, 553, values));
	}

	@Test
	public void lessThanEqShouldMatchPrecisionCorrectly() {
		MatcherLessThanOrEqual testMatcher = new MatcherLessThanOrEqual();
		List<Object> values = new ArrayList<Object>();
		values.add(552.552);
		assertTrue(runTestMatcher(testMatcher, 552.551, values));
	}

	@Test
	public void lessThanEqShouldFailPrecisionCorrectly() {
		MatcherLessThanOrEqual testMatcher = new MatcherLessThanOrEqual();
		List<Object> values = new ArrayList<Object>();
		values.add(552.552);
		assertFalse(runTestMatcher(testMatcher, 552.553, values));
	}

	@Test
	public void lessThanEqShouldMatchPrecisionEquals() {
		MatcherLessThanOrEqual testMatcher = new MatcherLessThanOrEqual();
		List<Object> values = new ArrayList<Object>();
		values.add(552.552);
		assertTrue(runTestMatcher(testMatcher, 552.552, values));
	}

	@Test
	public void lessThanEqShouldMatchPrecisionMultiplesCorrectly() {
		MatcherLessThanOrEqual testMatcher = new MatcherLessThanOrEqual();
		List<Object> values = new ArrayList<Object>();
		values.add(552.553);
		values.add(552.554);
		values.add(552.555);
		values.add(552.556);
		values.add(552.551);
		assertTrue(runTestMatcher(testMatcher, 552.552, values));
	}

	@Test
	public void lessThanEqShouldFailPrecisionMultiplesCorrectly() {
		MatcherLessThanOrEqual testMatcher = new MatcherLessThanOrEqual();
		List<Object> values = new ArrayList<Object>();
		values.add(552.547);
		values.add(552.548);
		values.add(552.549);
		values.add(552.550);
		values.add(552.551);
		assertFalse(runTestMatcher(testMatcher, 552.552, values));
	}

	@Test
	public void lessThanEqShouldConvertStringsWhenPossibleMatch() {
		MatcherLessThanOrEqual testMatcher = new MatcherLessThanOrEqual();
		List<Object> values = new ArrayList<Object>();
		values.add(552);
		assertTrue(runTestMatcher(testMatcher, "551", values));
	}

	@Test
	public void lessThanEqShouldConvertStringsWhenPossibleFail() {
		MatcherLessThanOrEqual testMatcher = new MatcherLessThanOrEqual();
		List<Object> values = new ArrayList<Object>();
		values.add(552);
		assertFalse(runTestMatcher(testMatcher, "553", values));
	}

	@Test
	public void lessThanEqShouldFailGracefulllyWithStringArray() {
		MatcherLessThanOrEqual testMatcher = new MatcherLessThanOrEqual();
		List<Object> values = new ArrayList<Object>();
		values.add("iamastring");
		assertFalse(runTestMatcher(testMatcher, 552, values));
	}

	@Test
	public void lessThanEqShouldFailGracefulllyWithStringInput() {
		MatcherLessThanOrEqual testMatcher = new MatcherLessThanOrEqual();
		List<Object> values = new ArrayList<Object>();
		values.add(552);
		assertFalse(runTestMatcher(testMatcher, "iamastring", values));
	}

	@Test
	public void lessThanOrEqualToString_When_ValidData_Then_ReturnStringValue() {
		MatcherLessThanOrEqual testMatcher = new MatcherLessThanOrEqual();
		testMatcher.key = "key";
		testMatcher.values = new ArrayList<Object>();
		testMatcher.values.add("down");
		testMatcher.values.add("hammer");
		testMatcher.values.add("stahp");
		String expected = generateToStringExpected(testMatcher, testMatcher.key, testMatcher.values);
		assertEquals(expected, testMatcher.toString());
	}

	@Test
	public void lessThanOrEqualToString_When_NullKey_Then_ReturnStringWithDefaults() {
		MatcherLessThanOrEqual testMatcher = new MatcherLessThanOrEqual();
		testMatcher.values = new ArrayList<Object>();
		testMatcher.values.add("down");
		testMatcher.values.add("hammer");
		testMatcher.values.add("stahp");
		String expected = generateToStringExpected(testMatcher, testMatcher.key, testMatcher.values);
		assertEquals(expected, testMatcher.toString());
	}


	// ================================================================================
	// MatcherContains
	// ================================================================================
	@Test
	public void containsShouldMatchExact() {
		MatcherContains testMatcher = new MatcherContains();
		List<Object> values = new ArrayList<Object>();
		values.add("matchme");
		assertTrue(runTestMatcher(testMatcher, "matchme", values));
	}

	@Test
	public void containsShouldMatchPartial() {
		MatcherContains testMatcher = new MatcherContains();
		List<Object> values = new ArrayList<Object>();
		values.add("match");
		assertTrue(runTestMatcher(testMatcher, "matchme", values));
	}

	@Test
	public void containsShouldNotMatchCorrectly() {
		MatcherContains testMatcher = new MatcherContains();
		List<Object> values = new ArrayList<Object>();
		values.add("matchme");
		assertFalse(runTestMatcher(testMatcher, "nope", values));
	}

	@Test
	public void containsShouldMatchInPhrase() {
		MatcherContains testMatcher = new MatcherContains();
		List<Object> values = new ArrayList<Object>();
		values.add("diamond");
		assertTrue(runTestMatcher(testMatcher, "sometimes when hunter dies, he loses his diamond gear and gets angry", values));
	}

	@Test
	public void containsShouldNotMatchCorrectlyInPhrase() {
		MatcherContains testMatcher = new MatcherContains();
		List<Object> values = new ArrayList<Object>();
		values.add("diamond");
		assertFalse(runTestMatcher(testMatcher, "sometimes when hunter dies, he loses his gear and gets angry", values));
	}

	@Test
	public void containsShouldMatchMultiplesInPhrase() {
		MatcherContains testMatcher = new MatcherContains();
		List<Object> values = new ArrayList<Object>();
		values.add("diamond");
		values.add("honey");
		values.add("sto");
		assertTrue(runTestMatcher(testMatcher, "stop, get down, hammer time", values));
	}

	@Test
	public void containsShouldNotMatchCorrectlyMultiplesInPhrase() {
		MatcherContains testMatcher = new MatcherContains();
		List<Object> values = new ArrayList<Object>();
		values.add("diamond");
		values.add("honey");
		values.add("stahp");
		assertFalse(runTestMatcher(testMatcher, "stop, get down, hammer time", values));
	}

	@Test
	public void containsShouldMatchExactCaseInsensitive() {
		MatcherContains testMatcher = new MatcherContains();
		List<Object> values = new ArrayList<Object>();
		values.add("matchme");
		assertTrue(runTestMatcher(testMatcher, "maTchMe", values));
		values.clear();
		values.add("maTchMe");
		assertTrue(runTestMatcher(testMatcher, "matchme", values));
	}

	@Test
	public void containsShouldMatchPartialCaseInsensitive() {
		MatcherContains testMatcher = new MatcherContains();
		List<Object> values = new ArrayList<Object>();
		values.add("match");
		assertTrue(runTestMatcher(testMatcher, "MaTChme", values));
		values.clear();
		values.add("mAtCh");
		assertTrue(runTestMatcher(testMatcher, "matchme", values));
	}

	@Test
	public void containsShouldNotMatchCorrectlyCaseInsensitive() {
		MatcherContains testMatcher = new MatcherContains();
		List<Object> values = new ArrayList<Object>();
		values.add("maTcH");
		assertFalse(runTestMatcher(testMatcher, "sTilL NopE", values));
	}

	@Test
	public void containsShouldMatchUnicode() {
		MatcherContains testMatcher = new MatcherContains();
		List<Object> values = new ArrayList<Object>();
		values.add("\\u0041");
		assertTrue(runTestMatcher(testMatcher, "there is a string called \\u0041 in this string", values));
	}

	@Test
	public void containsShouldHandleNonStrings() {
		MatcherContains testMatcher = new MatcherContains();
		List<Object> values = new ArrayList<Object>();
		values.add("allofthethings");
		assertFalse(runTestMatcher(testMatcher, 552, values));
	}

	@Test
	public void containsShouldMatchStringsIfTheyAreNumeric() {
		MatcherContains testMatcher = new MatcherContains();
		List<Object> values = new ArrayList<Object>();
		values.add("552");
		assertTrue(runTestMatcher(testMatcher, 38552834, values));
	}

	@Test
	public void containsToString_When_ValidData_Then_ReturnStringValue() {
		MatcherContains testMatcher = new MatcherContains();
		testMatcher.key = "key";
		testMatcher.values = new ArrayList<Object>();
		testMatcher.values.add("down");
		testMatcher.values.add("hammer");
		testMatcher.values.add("stahp");
		String expected = generateToStringExpected(testMatcher, testMatcher.key, testMatcher.values);
		assertEquals(expected, testMatcher.toString());
	}

	@Test
	public void containsToString_When_NullKey_Then_ReturnStringWithDefaults() {
		MatcherContains testMatcher = new MatcherContains();
		testMatcher.values = new ArrayList<Object>();
		testMatcher.values.add("down");
		testMatcher.values.add("hammer");
		testMatcher.values.add("stahp");
		String expected = generateToStringExpected(testMatcher, testMatcher.key, testMatcher.values);
		assertEquals(expected, testMatcher.toString());
	}


	// ================================================================================
	// MatcherNotContains
	// ================================================================================
	@Test
	public void notContainsShouldMatchCorrectly() {
		MatcherNotContains testMatcher = new MatcherNotContains();
		List<Object> values = new ArrayList<Object>();
		values.add("matchme");
		assertTrue(runTestMatcher(testMatcher, "nope", values));
	}

	@Test
	public void notContainsShouldNotMatchExact() {
		MatcherNotContains testMatcher = new MatcherNotContains();
		List<Object> values = new ArrayList<Object>();
		values.add("matchme");
		assertFalse(runTestMatcher(testMatcher, "matchme", values));
	}

	@Test
	public void notContainsShouldNotMatchPartial() {
		MatcherNotContains testMatcher = new MatcherNotContains();
		List<Object> values = new ArrayList<Object>();
		values.add("match");
		assertFalse(runTestMatcher(testMatcher, "matchme", values));
	}

	@Test
	public void notContainsShouldNotMatchInPhrase() {
		MatcherNotContains testMatcher = new MatcherNotContains();
		List<Object> values = new ArrayList<Object>();
		values.add("diamond");
		assertFalse(runTestMatcher(testMatcher, "sometimes when hunter dies, he loses his diamond gear and gets angry",
								   values));
	}

	@Test
	public void notContainsShouldMatchCorrectlyInPhrase() {
		MatcherNotContains testMatcher = new MatcherNotContains();
		List<Object> values = new ArrayList<Object>();
		values.add("diamond");
		assertTrue(runTestMatcher(testMatcher, "sometimes when hunter dies, he loses his gear and gets angry", values));
	}

	@Test
	public void notContainsShouldNotMatchMultiplesInPhrase() {
		MatcherNotContains testMatcher = new MatcherNotContains();
		List<Object> values = new ArrayList<Object>();
		values.add("diamond");
		values.add("honey");
		values.add("sto");
		assertFalse(runTestMatcher(testMatcher, "stop, get down, hammer time", values));
	}

	@Test
	public void notContainsShouldMatchCorrectlyMultiplesInPhrase() {
		MatcherNotContains testMatcher = new MatcherNotContains();
		List<Object> values = new ArrayList<Object>();
		values.add("diamond");
		values.add("honey");
		values.add("stahp");
		assertTrue(runTestMatcher(testMatcher, "stop, get down, hammer time", values));
	}

	@Test
	public void notContainsShouldNotMatchExactCaseInsensitive() {
		MatcherNotContains testMatcher = new MatcherNotContains();
		List<Object> values = new ArrayList<Object>();
		values.add("matchme");
		assertFalse(runTestMatcher(testMatcher, "maTchMe", values));
		values.clear();
		values.add("maTchMe");
		assertFalse(runTestMatcher(testMatcher, "matchme", values));
	}

	@Test
	public void notContainsShouldNotMatchPartialCaseInsensitive() {
		MatcherNotContains testMatcher = new MatcherNotContains();
		List<Object> values = new ArrayList<Object>();
		values.add("match");
		assertFalse(runTestMatcher(testMatcher, "MaTChme", values));
		values.clear();
		values.add("mAtCh");
		assertFalse(runTestMatcher(testMatcher, "matchme", values));
	}

	@Test
	public void notContainsShouldMatchCorrectlyCaseInsensitive() {
		MatcherNotContains testMatcher = new MatcherNotContains();
		List<Object> values = new ArrayList<Object>();
		values.add("maTcH");
		assertTrue(runTestMatcher(testMatcher, "sTilL NopE", values));
	}

	@Test
	public void notContainsShouldNotMatchUnicode() {
		MatcherNotContains testMatcher = new MatcherNotContains();
		List<Object> values = new ArrayList<Object>();
		values.add("\u2755");
		assertFalse(runTestMatcher(testMatcher, "there is a string called \u2755 in this string", values));
	}

	@Test
	public void notContainsShouldHandleNonStrings() {
		MatcherNotContains testMatcher = new MatcherNotContains();
		List<Object> values = new ArrayList<Object>();
		values.add("allofthethings");
		assertTrue(runTestMatcher(testMatcher, 552, values));
	}

	@Test
	public void notContainsShouldNotMatchStringsIfTheyAreNumeric() {
		MatcherNotContains testMatcher = new MatcherNotContains();
		List<Object> values = new ArrayList<Object>();
		values.add("552");
		assertFalse(runTestMatcher(testMatcher, 38552834, values));
	}

	@Test
	public void notContainsShouldMatchCorrectlyStringsIfTheyAreNumeric() {
		MatcherNotContains testMatcher = new MatcherNotContains();
		List<Object> values = new ArrayList<Object>();
		values.add("552");
		assertTrue(runTestMatcher(testMatcher, 3852834, values));
	}

	@Test
	public void notContainsToString_When_ValidData_Then_ReturnStringValue() {
		MatcherNotContains testMatcher = new MatcherNotContains();
		testMatcher.key = "key";
		testMatcher.values = new ArrayList<Object>();
		testMatcher.values.add("down");
		testMatcher.values.add("hammer");
		testMatcher.values.add("stahp");
		String expected = generateToStringExpected(testMatcher, testMatcher.key, testMatcher.values);
		assertEquals(expected, testMatcher.toString());
	}

	@Test
	public void notContainsToString_When_NullKey_Then_ReturnStringWithDefaults() {
		MatcherNotContains testMatcher = new MatcherNotContains();
		testMatcher.values = new ArrayList<Object>();
		testMatcher.values.add("down");
		testMatcher.values.add("hammer");
		testMatcher.values.add("stahp");
		String expected = generateToStringExpected(testMatcher, testMatcher.key, testMatcher.values);
		assertEquals(expected, testMatcher.toString());
	}


	// ================================================================================
	// MatcherStartsWith
	// ================================================================================
	@Test
	public void startsWithShouldMatchExact() {
		MatcherStartsWith testMatcher = new MatcherStartsWith();
		List<Object> values = new ArrayList<Object>();
		values.add("matchme");
		assertTrue(runTestMatcher(testMatcher, "matchme", values));
	}

	@Test
	public void startsWithShouldMatchPartialBeginning() {
		MatcherStartsWith testMatcher = new MatcherStartsWith();
		List<Object> values = new ArrayList<Object>();
		values.add("match");
		assertTrue(runTestMatcher(testMatcher, "matchme", values));
	}

	@Test
	public void startsWithShouldNotMatchCorrectly() {
		MatcherStartsWith testMatcher = new MatcherStartsWith();
		List<Object> values = new ArrayList<Object>();
		values.add("me");
		assertFalse(runTestMatcher(testMatcher, "matchme", values));
	}

	@Test
	public void startsWithShouldMatchInPhrase() {
		MatcherStartsWith testMatcher = new MatcherStartsWith();
		List<Object> values = new ArrayList<Object>();
		values.add("sometimes");
		assertTrue(runTestMatcher(testMatcher, "sometimes when hunter dies, he loses his diamond gear and gets angry", values));
	}

	@Test
	public void startsWithShouldNotMatchCorrectlyInPhrase() {
		MatcherStartsWith testMatcher = new MatcherStartsWith();
		List<Object> values = new ArrayList<Object>();
		values.add("diamond");
		assertFalse(runTestMatcher(testMatcher, "sometimes when hunter dies, he loses his diamond gear and gets angry",
								   values));
	}

	@Test
	public void startsWithShouldMatchMultiplesInPhrase() {
		MatcherStartsWith testMatcher = new MatcherStartsWith();
		List<Object> values = new ArrayList<Object>();
		values.add("diamond");
		values.add("honey");
		values.add("sto");
		assertTrue(runTestMatcher(testMatcher, "stop, get down, hammer time", values));
	}

	@Test
	public void startsWithShouldNotMatchCorrectlyMultiplesInPhrase() {
		MatcherStartsWith testMatcher = new MatcherStartsWith();
		List<Object> values = new ArrayList<Object>();
		values.add("down");
		values.add("hammer");
		values.add("stahp");
		assertFalse(runTestMatcher(testMatcher, "stop, get down, hammer time", values));
	}

	@Test
	public void startsWithToString_When_ValidData_Then_ReturnStringValue() {
		MatcherStartsWith testMatcher = new MatcherStartsWith();
		testMatcher.key = "key";
		testMatcher.values = new ArrayList<Object>();
		testMatcher.values.add("down");
		testMatcher.values.add("hammer");
		testMatcher.values.add("stahp");
		String expected = generateToStringExpected(testMatcher, testMatcher.key, testMatcher.values);
		assertEquals(expected, testMatcher.toString());
	}

	@Test
	public void startsWithToString_When_NullKey_Then_ReturnStringWithDefaults() {
		MatcherStartsWith testMatcher = new MatcherStartsWith();
		testMatcher.values = new ArrayList<Object>();
		testMatcher.values.add("down");
		testMatcher.values.add("hammer");
		testMatcher.values.add("stahp");
		String expected = generateToStringExpected(testMatcher, testMatcher.key, testMatcher.values);
		assertEquals(expected, testMatcher.toString());
	}



	@Test
	public void startsWithShouldMatchExactCaseInsensitive() {
		MatcherStartsWith testMatcher = new MatcherStartsWith();
		List<Object> values = new ArrayList<Object>();
		values.add("matchme");
		assertTrue(runTestMatcher(testMatcher, "maTchMe", values));
		values.clear();
		values.add("maTchMe");
		assertTrue(runTestMatcher(testMatcher, "matchme", values));
	}

	@Test
	public void startsWithShouldMatchPartialCaseInsensitive() {
		MatcherStartsWith testMatcher = new MatcherStartsWith();
		List<Object> values = new ArrayList<Object>();
		values.add("match");
		assertTrue(runTestMatcher(testMatcher, "MaTChme", values));
		values.clear();
		values.add("mAtCh");
		assertTrue(runTestMatcher(testMatcher, "matchme", values));
	}

	@Test
	public void startsWithShouldNotMatchCorrectlyCaseInsensitive() {
		MatcherStartsWith testMatcher = new MatcherStartsWith();
		List<Object> values = new ArrayList<Object>();
		values.add("aTCH");
		assertFalse(runTestMatcher(testMatcher, "MaTCH", values));
	}

	@Test
	public void startsWithShouldMatchUnicode() {
		MatcherStartsWith testMatcher = new MatcherStartsWith();
		List<Object> values = new ArrayList<Object>();
		values.add("\\u0041");
		assertTrue(runTestMatcher(testMatcher, "\\u0041 in this string", values));
	}

	@Test
	public void startsWithShouldHandleNonStrings() {
		MatcherStartsWith testMatcher = new MatcherStartsWith();
		List<Object> values = new ArrayList<Object>();
		values.add("allofthethings");
		assertFalse(runTestMatcher(testMatcher, 552, values));
	}

	@Test
	public void startsWithShouldMatchStringsIfTheyAreNumeric() {
		MatcherStartsWith testMatcher = new MatcherStartsWith();
		List<Object> values = new ArrayList<Object>();
		values.add("552");
		assertTrue(runTestMatcher(testMatcher, 552834, values));
	}

	@Test
	public void startsWithShouldFailStringsCorrectlyIfTheyAreNumeric() {
		MatcherStartsWith testMatcher = new MatcherStartsWith();
		List<Object> values = new ArrayList<Object>();
		values.add("552");
		assertFalse(runTestMatcher(testMatcher, 1552834, values));
	}

	// ================================================================================
	// MatcherEndsWith
	// ================================================================================
	@Test
	public void endsWithShouldMatchExact() {
		MatcherEndsWith testMatcher = new MatcherEndsWith();
		List<Object> values = new ArrayList<Object>();
		values.add("matchme");
		assertTrue(runTestMatcher(testMatcher, "matchme", values));
	}

	@Test
	public void endsWithShouldMatchPartialEnd() {
		MatcherEndsWith testMatcher = new MatcherEndsWith();
		List<Object> values = new ArrayList<Object>();
		values.add("me");
		assertTrue(runTestMatcher(testMatcher, "matchme", values));
	}

	@Test
	public void endsWithShouldNotMatchCorrectly() {
		MatcherEndsWith testMatcher = new MatcherEndsWith();
		List<Object> values = new ArrayList<Object>();
		values.add("match");
		assertFalse(runTestMatcher(testMatcher, "matchme", values));
	}

	@Test
	public void endsWithShouldMatchInPhrase() {
		MatcherEndsWith testMatcher = new MatcherEndsWith();
		List<Object> values = new ArrayList<Object>();
		values.add("angry");
		assertTrue(runTestMatcher(testMatcher, "sometimes when hunter dies, he loses his diamond gear and gets angry", values));
	}

	@Test
	public void endsWithShouldNotMatchCorrectlyInPhrase() {
		MatcherEndsWith testMatcher = new MatcherEndsWith();
		List<Object> values = new ArrayList<Object>();
		values.add("diamond");
		assertFalse(runTestMatcher(testMatcher, "sometimes when hunter dies, he loses his diamond gear and gets angry",
								   values));
	}

	@Test
	public void endsWithShouldMatchMultiplesInPhrase() {
		MatcherEndsWith testMatcher = new MatcherEndsWith();
		List<Object> values = new ArrayList<Object>();
		values.add("diamond");
		values.add("honey");
		values.add("me");
		assertTrue(runTestMatcher(testMatcher, "stop, get down, hammer time", values));
	}

	@Test
	public void endsWithShouldNotMatchCorrectlyMultiplesInPhrase() {
		MatcherEndsWith testMatcher = new MatcherEndsWith();
		List<Object> values = new ArrayList<Object>();
		values.add("diamond");
		values.add("hammer");
		values.add("tim");
		assertFalse(runTestMatcher(testMatcher, "stop, get down, hammer time", values));
	}

	@Test
	public void endsWithShouldMatchExactCaseInsensitive() {
		MatcherEndsWith testMatcher = new MatcherEndsWith();
		List<Object> values = new ArrayList<Object>();
		values.add("matchme");
		assertTrue(runTestMatcher(testMatcher, "maTchMe", values));
		values.clear();
		values.add("maTchMe");
		assertTrue(runTestMatcher(testMatcher, "matchme", values));
	}

	@Test
	public void endsWithShouldMatchPartialCaseInsensitive() {
		MatcherEndsWith testMatcher = new MatcherEndsWith();
		List<Object> values = new ArrayList<Object>();
		values.add("me");
		assertTrue(runTestMatcher(testMatcher, "MaTChmE", values));
		values.clear();
		values.add("mE");
		assertTrue(runTestMatcher(testMatcher, "matchme", values));
	}

	@Test
	public void endsWithShouldNotMatchCorrectlyCaseInsensitive() {
		MatcherEndsWith testMatcher = new MatcherEndsWith();
		List<Object> values = new ArrayList<Object>();
		values.add("MaTc");
		assertFalse(runTestMatcher(testMatcher, "MaTCH", values));
	}

	@Test
	public void endsWithShouldMatchUnicode() {
		MatcherEndsWith testMatcher = new MatcherEndsWith();
		List<Object> values = new ArrayList<Object>();
		values.add("\u0041");
		assertTrue(runTestMatcher(testMatcher, "in this string \u0041", values));
	}

	@Test
	public void endsWithShouldHandleNonStrings() {
		MatcherEndsWith testMatcher = new MatcherEndsWith();
		List<Object> values = new ArrayList<Object>();
		values.add("allofthethings");
		assertFalse(runTestMatcher(testMatcher, 552, values));
	}

	@Test
	public void endsWithShouldMatchStringsIfTheyAreNumeric() {
		MatcherEndsWith testMatcher = new MatcherEndsWith();
		List<Object> values = new ArrayList<Object>();
		values.add("552");
		assertTrue(runTestMatcher(testMatcher, 834552, values));
	}

	@Test
	public void endsWithShouldFailStringsCorrectlyIfTheyAreNumeric() {
		MatcherEndsWith testMatcher = new MatcherEndsWith();
		List<Object> values = new ArrayList<Object>();
		values.add("552");
		assertFalse(runTestMatcher(testMatcher, 1552834, values));
	}

	@Test
	public void endsWithToString_When_ValidData_Then_ReturnStringValue() {
		MatcherEndsWith testMatcher = new MatcherEndsWith();
		testMatcher.key = "key";
		testMatcher.values = new ArrayList<Object>();
		testMatcher.values.add("down");
		testMatcher.values.add("hammer");
		testMatcher.values.add("stahp");
		String expected = generateToStringExpected(testMatcher, testMatcher.key, testMatcher.values);
		assertEquals(expected, testMatcher.toString());
	}

	@Test
	public void endsWithToString_When_NullKey_Then_ReturnStringWithDefaults() {
		MatcherEndsWith testMatcher = new MatcherEndsWith();
		testMatcher.values = new ArrayList<Object>();
		testMatcher.values.add("down");
		testMatcher.values.add("hammer");
		testMatcher.values.add("stahp");
		String expected = generateToStringExpected(testMatcher, testMatcher.key, testMatcher.values);
		assertEquals(expected, testMatcher.toString());
	}

	// ================================================================================
	// MatcherExists
	// ================================================================================
	@Test
	public void existsShouldMatchIfKeyIsPresent() {
		MatcherExists matcher = new MatcherExists();
		matcher.key = "testKey";

		EventData testEventData = new EventData();
		testEventData.putString("testKey", "smoked hampster ribs");
		Event testEvent = new Event.Builder("test", EventType.ANALYTICS,
											EventSource.RESPONSE_CONTENT).setData(testEventData).build();

		assertTrue(matcher.matches(ruleTokenParser.expandKey(matcher.key, testEvent)));
	}

	@Test
	public void existsShouldNotMatchIfKeyIsNotPresent() {
		MatcherExists matcher = new MatcherExists();
		matcher.key = "testKey";

		EventData testEventData = new EventData();
		testEventData.putString("nottestKey", "smoked hampster ribs");
		Event testEvent = new Event.Builder("test", EventType.ANALYTICS,
											EventSource.RESPONSE_CONTENT).setData(testEventData).build();

		assertFalse(matcher.matches(ruleTokenParser.expandKey(matcher.key, testEvent)));
	}

	@Test
	public void existsToString_When_ValidData_Then_ReturnStringValue() {
		MatcherExists testMatcher = new MatcherExists();
		testMatcher.key = "key";
		String expected = generateToStringExpected(testMatcher, testMatcher.key, testMatcher.values);
		assertEquals(expected, testMatcher.toString());
	}

	@Test
	public void existsToString_When_NullKey_Then_ReturnStringWithDefaults() {
		MatcherExists testMatcher = new MatcherExists();
		String expected = generateToStringExpected(testMatcher, testMatcher.key, testMatcher.values);
		assertEquals(expected, testMatcher.toString());
	}


	// ================================================================================
	// MatcherNotExists
	// ================================================================================
	@Test
	public void notExistsShouldMatchIfKeyIsNotPresent() {
		MatcherNotExists matcher = new MatcherNotExists();
		matcher.key = "testKey";

		EventData testEventData = new EventData();
		testEventData.putString("nottestKey", "smoked hampster ribs");
		Event testEvent = new Event.Builder("test", EventType.ANALYTICS,
											EventSource.RESPONSE_CONTENT).setData(testEventData).build();

		assertTrue(matcher.matches(ruleTokenParser.expandKey(matcher.key, testEvent)));
	}

	@Test
	public void notExistsShouldNotMatchIfKeyIsPresent() {
		MatcherNotExists matcher = new MatcherNotExists();
		matcher.key = "testKey";

		EventData testEventData = new EventData();
		testEventData.putString("testKey", "smoked hampster ribs");
		Event testEvent = new Event.Builder("test", EventType.ANALYTICS,
											EventSource.RESPONSE_CONTENT).setData(testEventData).build();

		assertFalse(matcher.matches(ruleTokenParser.expandKey(matcher.key, testEvent)));
	}

	@Test
	public void notExistsToString_When_ValidData_Then_ReturnStringValue() {
		MatcherNotExists testMatcher = new MatcherNotExists();
		testMatcher.key = "key";
		String expected = generateToStringExpected(testMatcher, testMatcher.key, testMatcher.values);
		assertEquals(expected, testMatcher.toString());
	}

	@Test
	public void notExistsToString_When_NullKey_Then_ReturnStringWithDefaults() {
		MatcherNotExists testMatcher = new MatcherNotExists();
		String expected = generateToStringExpected(testMatcher, testMatcher.key, testMatcher.values);
		assertEquals(expected, testMatcher.toString());
	}

	// ================================================================================
	// MatcherUnknown
	// ================================================================================
	@Test
	public void unknownShouldAlwaysReturnFalse() {
		MatcherUnknown testMatcher = new MatcherUnknown();
		List<Object> values = new ArrayList<Object>();
		values.add("we match, but still false");
		assertFalse(runTestMatcher(testMatcher, "we match, but still false", values));
		values.clear();
		values.add("we no longer match...still false");
		assertFalse(runTestMatcher(testMatcher, "what he said", values));
	}

	@Test
	public void setMatcherValuesFromJson_When_MatcherIsNull_DoNothing() throws JsonException {
		//Test
		Matcher.setMatcherValuesFromJson(platformServices.getJsonUtilityService().createJSONObject("{}"), null);
		//Verify
		//Should not have a NPE / Exception

	}

	@Test
	public void setMatcherValuesFromJson_When_ValuesArrayIsEmpty_Then_DoNotChangeExistingMatcherValues() throws Exception {
		//Setup
		Map<String, Object> testData = new HashMap<String, Object>();
		testData.put("key", "blah");
		testData.put("matcher", "eq");
		testData.put("values", new ArrayList<String>());
		JsonUtilityService.JSONObject jsonObject = jsonUtilityService.createJSONObject(new JSONObject(testData).toString());

		Matcher matcher = new Matcher() {
			@Override
			public String toString() {
				return "Empty Matcher";
			}
		};

		matcher.values = new ArrayList<Object>();
		matcher.values.add("value");
		//Test
		Matcher.setMatcherValuesFromJson(jsonObject, matcher);
		//Verify
		assertEquals("Values should not change!", 1, matcher.values.size());
		assertEquals("value", matcher.values.get(0));

	}

	@Test
	public void setMatcherValuesFromJson_When_ValuesArrayIsEmptyAndMatcherValuesIsEmpty_Then_MatcherValuesRemainsEmpty()
	throws Exception {
		//Setup
		Map<String, Object> testData = new HashMap<String, Object>();
		testData.put("key", "blah");
		testData.put("matcher", "eq");
		testData.put("values", new ArrayList<String>());
		JsonUtilityService.JSONObject jsonObject = jsonUtilityService.createJSONObject(new JSONObject(testData).toString());

		Matcher matcher = new Matcher() {
			@Override
			public String toString() {
				return "Empty Matcher";
			}
		};

		matcher.values = new ArrayList<Object>();
		//Test
		Matcher.setMatcherValuesFromJson(jsonObject, matcher);
		//Verify
		assertEquals("Values should not change!", 0, matcher.values.size());
	}

	@Test
	public void setMatcherKeyFromJson_When_MatcherIsNull_DoNothing() {
		//Test
		Matcher.setMatcherKeyFromJson(platformServices.getJsonUtilityService().createJSONObject("{}"), null);
		//Verify
		//Should not have a NPE / Exception
	}

	@Test
	public void setMatcherKeyFromJson_When_KeyIsEmpty_Then_DoNotChangeExistingMatcherValues() {
		//Setup
		Map<String, Object> testData = new HashMap<String, Object>();
		testData.put("key", "");
		testData.put("matcher", "eq");
		ArrayList<String> values = new ArrayList<String>();
		values.add("blah");
		testData.put("values", values);
		JsonUtilityService.JSONObject jsonObject = jsonUtilityService.createJSONObject(new JSONObject(testData).toString());

		Matcher matcher = new Matcher() {
			@Override
			public String toString() {
				return "Empty Matcher";
			}
		};

		matcher.values = new ArrayList<Object>();
		matcher.values.add("value");
		matcher.key = "key";
		//Test
		Matcher.setMatcherKeyFromJson(jsonObject, matcher);
		//Verify
		assertEquals("Values should not change!", 1, matcher.values.size());
		assertEquals("value", matcher.values.get(0));
		assertEquals("key", matcher.key);

	}


	@Test
	public void matcherInMaps_When_MapsIsNull_Then_ReturnsFalse() {
		//setup
		Map<String, Object> testData = new HashMap<String, Object>();
		testData.put("key", "blah");
		testData.put("matcher", "eq");
		List<String> valuesList = new ArrayList<String>();
		valuesList.add("testValue1");
		testData.put("values", valuesList);
		JsonUtilityService.JSONObject jsonObject = jsonUtilityService.createJSONObject(new JSONObject(testData).toString());

		Matcher matcher = Matcher.matcherWithJsonObject(jsonObject);
		//Test and verify
		Event testEvent = new Event.Builder("test", EventType.ANALYTICS,
											EventSource.RESPONSE_CONTENT).build();

		assertFalse(matcher.matches(ruleTokenParser.expandKey(matcher.key, testEvent)));
	}

	@Test
	public void constructor_When_ErrorWhileInstantiatingMatcherClass_Then_ReturnNull() {
		//setup
		Map<String, Object> testData = new HashMap<String, Object>();
		testData.put("key", "blah");
		//Invalid matcher!!
		testData.put("matcher", "zz");
		List<String> valuesList = new ArrayList<String>();
		valuesList.add("testValue1");
		testData.put("values", valuesList);
		JsonUtilityService.JSONObject jsonObject = jsonUtilityService.createJSONObject(new JSONObject(testData).toString());
		//test
		Matcher._matcherTypeDictionary.put("zz", InAccessibleMatcher.class);
		Matcher matcher = Matcher.matcherWithJsonObject(jsonObject);
		//assert
		assertNull(matcher);
	}

	@Test
	public void constructor_When_ErrorWhileInstantiatingMatcherClass_Then_ReturnNull1() {
		//setup
		Map<String, Object> testData = new HashMap<String, Object>();
		testData.put("key", "blah");
		//Empty matcher string!!
		testData.put("matcher", "");
		List<String> valuesList = new ArrayList<String>();
		valuesList.add("testValue1");
		testData.put("values", valuesList);
		JsonUtilityService.JSONObject jsonObject = jsonUtilityService.createJSONObject(new JSONObject(testData).toString());
		//test
		Matcher matcher = Matcher.matcherWithJsonObject(jsonObject);
		//assert
		assertTrue(matcher instanceof MatcherUnknown);
	}
}
