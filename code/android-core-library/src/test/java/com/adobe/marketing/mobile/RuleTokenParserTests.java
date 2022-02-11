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

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class RuleTokenParserTests extends BaseTest {
	private RuleTokenParser ruleTokenParser;

	@BeforeClass
	public static void setupTests() {
		PlatformServices mockPlatformServices = new FakePlatformServices();
		Log.setLoggingService(mockPlatformServices.getLoggingService());
		Log.setLogLevel(LoggingMode.VERBOSE);
	}

	@AfterClass
	public static void tearDownTests() {
		//Reset logger
		Log.setLogLevel(LoggingMode.ERROR);
		Log.setLoggingService(null);
	}

	@Before
	public void setup() {
		super.beforeEach();
		ruleTokenParser = new RuleTokenParser(eventHub);
	}

	@Test
	public void expandKey_ReturnsNull_When_KeyIsNull() {
		//setup
		Event testEvent = getDefaultEvent();
		//test
		String result = ruleTokenParser.expandKey(null, testEvent);
		//verify
		assertNull("expandKey should return null on null input string", result);
	}

	@Test
	public void expandKey_ReturnsNull_When_KeyIsEmpty() {
		//setup
		Event testEvent = getDefaultEvent();
		//test
		String result = ruleTokenParser.expandKey("", testEvent);
		//verify
		assertNull("expandKey should return null on empty input string", result);
	}


	@Test
	public void expandKey_ReturnsNull_When_EntryIsNotExist() {
		//setup
		Event testEvent = getDefaultEvent();
		//test
		String result = ruleTokenParser.expandKey("abc", testEvent);
		//verify
		assertNull("expandKey should return null on none-exist entry", result);
	}

	@Test
	public void expandKey_ReturnsNull_When_ValueIsNull() {
		//setup
		EventData testEventData = new EventData();
		testEventData.putNull("key1");
		Event testEvent = getEvent(EventType.ANALYTICS, EventSource.REQUEST_CONTENT, testEventData);
		//test
		String result = ruleTokenParser.expandKey("key1", testEvent);
		//verify
		assertNull("expandKey should return null on null variant value", result);
	}

	@Test
	public void expandKey_ReturnsEmptyString_When_ValueIsList() {
		//setup
		EventData testEventData = new EventData();
		testEventData.putVariantList("key1", new ArrayList<Variant>());
		Event testEvent = getEvent(EventType.ANALYTICS, EventSource.REQUEST_CONTENT, testEventData);
		//test
		String result = ruleTokenParser.expandKey("key1", testEvent);
		//verify
		assertEquals("expandKey should return empty string on list variant", "", result);
	}

	@Test
	public void expandKey_ReturnsEmptyString_When_ValueIsMap() {
		//setup
		EventData testEventData = new EventData();
		testEventData.putVariantMap("key1", new HashMap<String, Variant>());
		Event testEvent = getEvent(EventType.ANALYTICS, EventSource.REQUEST_CONTENT, testEventData);
		//test
		String result = ruleTokenParser.expandKey("key1", testEvent);
		//verify
		assertEquals("expandKey should return empty string on map variant", "", result);
	}

	@Test
	public void expandKey_ReturnsEventType_When_KeyPrefixIsType() {
		//setup
		Event testEvent = getEvent(EventType.ANALYTICS, EventSource.REQUEST_CONTENT);
		//test
		String result = ruleTokenParser.expandKey("~type", testEvent);
		//verify
		assertEquals("expandKey should return Event Type on valid Event", result, "com.adobe.eventtype.analytics");
	}

	@Test
	public void expandKey_ReturnsEventSource_When_KeyPrefixIsSource() {
		//setup
		Event testEvent = getEvent(EventType.ANALYTICS, EventSource.REQUEST_CONTENT);
		//test
		String result = ruleTokenParser.expandKey("~source", testEvent);
		//verify
		assertEquals("expandKey should return Event Source on valid event", result, "com.adobe.eventsource.requestcontent");
	}

	@Test
	public void expandKey_ReturnsCurrentUnixTimestamp_When_KeyPrefixIsTimestampu() {
		//setup
		Event testEvent = getDefaultEvent();
		//test
		String result = ruleTokenParser.expandKey("~timestampu", testEvent);
		//verify
		assertNotNull("expandKey should return current unit timestamp on valid event", result);
	}

	@Test
	public void expandKey_ReturnsCurrentISO8601Timestamp_When_KeyPrefixIsTimestampz() {
		//setup
		Event testEvent = getDefaultEvent();
		//test
		String result = ruleTokenParser.expandKey("~timestampz", testEvent);
		//verify
		assertNotNull("expandKey should return current ISO8601 timestamp on valid event", result);
	}

	@Test
	public void expandKey_ReturnsCurrentSdkVersion_When_KeyPrefixIsSdkVersion() {
		//setup
		Event testEvent = getDefaultEvent();
		//test
		String result = ruleTokenParser.expandKey("~sdkver", testEvent);
		//verify
		assertEquals("expandKey should return current sdk version on valid event", result, "mockSdkVersion");
	}

	@Test
	public void expandKey_ReturnsRandomNumber_When_KeyPrefixIsCachebust() {
		//setup
		Event testEvent = getDefaultEvent();
		//test
		String result = ruleTokenParser.expandKey("~cachebust", testEvent);
		//verify
		assertNotNull("expandKey should return random cachebust on valid event", result);
	}

	@Test
	public void expandKey_ReturnsUrlEncoded_When_KeyPrefixIsAllUrl() {
		//setup
		EventData testEventData = new EventData();
		testEventData.putString("key1", "value 1");
		testEventData.putString("key2", "value 2");
		Event testEvent = getEvent(EventType.ANALYTICS, EventSource.REQUEST_CONTENT, testEventData);
		//test
		String result = ruleTokenParser.expandKey("~all_url", testEvent);
		//verify
		assertEquals("expandKey should return all variables on valid event", result, "&key1=value%201&key2=value%202");
	}

	@Test
	public void expandKey_ReturnsJson_When_KeyPrefixIsAllJson() {
		//setup
		EventData testEventData = new EventData();
		testEventData.putString("key1", "value1");
		testEventData.putString("key2", "value2");
		Event testEvent = getEvent(EventType.ANALYTICS, EventSource.REQUEST_CONTENT, testEventData);

		//test
		String result = ruleTokenParser.expandKey("~all_json", testEvent);
		//verify
		assertEquals("{\"key1\":\"value1\",\"key2\":\"value2\"}", result);
	}

	@Test
	public void expandKey_ReturnsSharedStateKey_When_KeyPrefixIsState() {
		//setup
		EventData testEventData = new EventData();
		testEventData.putString("key1", "value1");
		testEventData.putString("key2", "value2");
		Event testEvent = getEvent(EventType.ANALYTICS, EventSource.REQUEST_CONTENT, testEventData);

		EventData lcdata = new EventData();
		Map<String, String> lifecycleSharedState = new HashMap<String, String>();
		lifecycleSharedState.put("akey", "avalue");
		lcdata.putMap("analytics.contextData", lifecycleSharedState);
		eventHub.setSharedState("com.adobe.marketing.mobile.Analytics", lcdata);

		//test
		String result = ruleTokenParser.expandKey("~state.com.adobe.marketing.mobile.Analytics/analytics.contextData.akey",
						testEvent);
		//verify
		assertEquals("expandKey should return current sdk version on valid event", result, "avalue");
	}


	@Test
	public void expandKey_ReturnsNull_When_KeyPrefixIsStateAndKeyNotExist() {
		//setup
		EventData testEventData = new EventData();
		Event testEvent = getEvent(EventType.ANALYTICS, EventSource.REQUEST_CONTENT, testEventData);

		EventData lcdata = new EventData();
		Map<String, String> lifecycleSharedState = new HashMap<String, String>();
		eventHub.setSharedState("com.adobe.marketing.mobile.Analytics", lcdata);

		//test
		String result = ruleTokenParser.expandKey("~state.com.adobe.marketing.mobile.Analytics/analytics.contextData.akey",
						testEvent);
		//verify
		assertEquals(null, result);
	}

	@Test
	public void expandTokensForString_ReturnsEmptyString_When_InputStringIsEmpty() {
		//setup
		Event testEvent = getDefaultEvent();

		//test
		String result = ruleTokenParser.expandTokensForString("", testEvent);
		//verify
		assertEquals("expandTokensForString should return current sdk version on valid event", result, "");
	}

	@Test
	public void expandTokensForString_ReturnsNull_When_InputStringIsNull() {
		//setup
		Event testEvent = getDefaultEvent();

		//test
		String result = ruleTokenParser.expandTokensForString(null, testEvent);
		//verify
		assertNull("expandTokensForString should return current sdk version on valid event", result);
	}

	@Test
	public void expandTokensForString_ReplacesTokens_When_InputStringHasValidTokens() {
		//setup
		EventData testEventData = new EventData();
		testEventData.putString("key1", "value1");
		testEventData.putString("key2", "value2");
		Event testEvent = getEvent(EventType.ANALYTICS, EventSource.REQUEST_CONTENT, testEventData);

		//test
		String result =
			ruleTokenParser.expandTokensForString("This is {%key1%} and this is {%key2%} for my event of {%~type%} and {%~source%}",
					testEvent);
		//verify
		assertEquals(result,
					 "This is value1 and this is value2 for my event of com.adobe.eventtype.analytics and com.adobe.eventsource.requestcontent");
	}

	@Test
	public void expandTokensForString_ReplacesUnknownTokens_When_InputStringHasUnknownTokens() {
		//setup
		Event testEvent = getDefaultEvent();

		//test
		String result = ruleTokenParser.expandTokensForString("This key is unkn{%key3%}own to sdk version {%~sdkver%}",
						testEvent);
		//verify
		assertEquals(result, "This key is unknown to sdk version mockSdkVersion");
	}

	@Test
	public void expandTokensForString_IgnoresTokens_When_InputStringHasMalformedTokens() {
		//setup
		Event testEvent = getDefaultEvent();

		//test
		String result = ruleTokenParser.expandTokensForString("{%key1%} is a replacement for {key1}", testEvent);
		//verify
		assertEquals(result, "value1 is a replacement for {key1}");
	}

	private Event getEvent(final EventType type, final EventSource source, final EventData eventData) {
		return new Event.Builder("TEST", type, source)
			   .setData(eventData).build();
	}

	private Event getEvent(final EventType type, final EventSource source) {
		EventData testEventData = new EventData();
		testEventData.putString("key1", "value1");
		return getEvent(type, source, testEventData);
	}

	private Event getDefaultEvent() {
		EventData testEventData = new EventData();
		testEventData.putString("key1", "value1");
		return getEvent(EventType.ANALYTICS, EventSource.REQUEST_CONTENT, testEventData);
	}

	@Test
	public void expandTokensForString_ReplacesTokens_When_InputStringHasUrlEncodingInValidTokens() {
		//setup
		EventData testEventData = new EventData();
		testEventData.putString("token", "value 1");
		Event testEvent = getEvent(EventType.ANALYTICS, EventSource.REQUEST_CONTENT, testEventData);

		//test
		String result =
			ruleTokenParser.expandTokensForString("Testing {%urlenc(token%}, {%urlenctoken%}, {%~urlenc%}, {%urlenc%}, {%urlencoding(token)%}, {%urlenctest_token%} and {%~test_tokenurlenc%} for my event. Only get {%urlenc(token)%} when formed correctly",
					testEvent);
		//verify
		assertEquals(result,
					 "Testing , , , , {%urlencoding(token)%},  and  for my event. Only get value%201 when formed correctly");
	}

	@Test
	public void expandTokensForString_ReplacesTokens_When_InputStringHasUrlEncodingValidTokens() {
		//setup
		EventData testEventData = new EventData();
		testEventData.putString("key1", "value 1");
		testEventData.putString("key2", "value 2");
		Event testEvent = getEvent(EventType.ANALYTICS, EventSource.REQUEST_CONTENT, testEventData);

		//test
		String result =
			ruleTokenParser.expandTokensForString("{%urlenc(key1)%} is a replacement for {%urlenc(key2)%}",
					testEvent);
		//verify
		assertEquals(result,
					 "value%201 is a replacement for value%202");
	}
}
