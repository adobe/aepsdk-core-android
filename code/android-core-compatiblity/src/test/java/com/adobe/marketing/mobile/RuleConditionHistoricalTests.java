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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class RuleConditionHistoricalTests {
//
//	private PlatformServices platformServices;
//	private EventHub testEventHub;
//	private FakeEventHistory testEventHistory;
//
//	@Before
//	public void beforeEachTest() {
//		platformServices = new FakePlatformServices();
//		testEventHub = new EventHub("eventhub", platformServices);
//		testEventHistory = new FakeEventHistory();
//		EventHistoryProvider.setEventHistory(testEventHistory);
//	}
//
//	@Test
//	public void
//	ruleConditionFromJson_ReturnsRuleConditionHistorical_When_ValidJsonObjectContainingTypeHistoricalIsProvided()
//	throws Exception {
//		//test
//		JsonUtilityService.JSONObject testRuleConditionJson =
//			platformServices.getJsonUtilityService().createJSONObject("{\"definition\":{\"events\":[{\"xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID\":\"UIA-23558765\",\"xdm.eventType\":\"inapp.display\"},{\"xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID\":\"UIA-65098551\",\"xdm.eventType\":\"inapp.display\"}],\"searchType\":\"any\",\"matcher\":\"eq\",\"value\":0,\"from\":1634886000000,\"to\":1672470000000},\"type\":\"historical\"}]}");
//		RuleCondition testRuleCondition = RuleConditionHistorical.ruleConditionFromJson(testRuleConditionJson);
//		//verify
//		assertTrue(testRuleCondition instanceof RuleConditionHistorical);
//	}
//
//	@Test
//	public void
//	ruleConditionFromJson_ReturnsRuleConditionHistoricalWithNullHistoricalRequests_When_HistoricalConditionWithEmptyEventsArrayIsProvided()
//	throws Exception {
//		//test
//		JsonUtilityService.JSONObject testRuleConditionJson =
//			platformServices.getJsonUtilityService().createJSONObject("{\"definition\":{\"events\":[],\"searchType\":\"any\",\"matcher\":\"eq\",\"value\":0,\"from\":1634886000000,\"to\":1672470000000},\"type\":\"historical\"}]}");
//		RuleCondition testRuleCondition = RuleConditionHistorical.ruleConditionFromJson(testRuleConditionJson);
//		//verify
//		assertTrue(testRuleCondition instanceof RuleConditionHistorical);
//		assertNull(((RuleConditionHistorical) testRuleCondition).getEventHistoryRequests());
//	}
//
//	@Test(expected = UnsupportedConditionException.class)
//	public void
//	ruleConditionFromJson_Throws_UnsupportedConditionException_When_HistoricalConditionWithInvalidTypeIsProvided()
//	throws Exception {
//		//test
//		JsonUtilityService.JSONObject testRuleConditionJson =
//			platformServices.getJsonUtilityService().createJSONObject("{\"definition\":{\"events\":[{\"xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID\":\"UIA-23558765\",\"xdm.eventType\":\"inapp.display\"},{\"xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID\":\"UIA-65098551\",\"xdm.eventType\":\"inapp.display\"}],\"searchType\":\"any\",\"matcher\":\"eq\",\"value\":0,\"from\":1634886000000,\"to\":1672470000000},\"type\":\"invalid\"}]}");
//		RuleCondition testRuleCondition = RuleConditionHistorical.ruleConditionFromJson(testRuleConditionJson);
//	}
//
//	@Test(expected = UnsupportedConditionException.class)
//	public void
//	ruleConditionFromJson_Throws_UnsupportedConditionException_When_HistoricalConditionWithEmptyDefinitionIsProvided()
//	throws Exception {
//		//test
//		JsonUtilityService.JSONObject testRuleConditionJson =
//			platformServices.getJsonUtilityService().createJSONObject("{\"definition\":{},\"type\":\"historical\"}]}");
//		RuleCondition testRuleCondition = RuleConditionHistorical.ruleConditionFromJson(testRuleConditionJson);
//	}
//
//	@Test(expected = UnsupportedConditionException.class)
//	public void
//	ruleConditionFromJson_Throws_UnsupportedConditionException_When_HistoricalConditionWithMissingMatcherTypeIsProvided()
//	throws Exception {
//		//test
//		JsonUtilityService.JSONObject testRuleConditionJson =
//			platformServices.getJsonUtilityService().createJSONObject("{\"definition\":{\"events\":[{\"xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID\":\"UIA-23558765\",\"xdm.eventType\":\"inapp.display\"},{\"xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID\":\"UIA-65098551\",\"xdm.eventType\":\"inapp.display\"}],\"searchType\":\"any\",\"value\":0,\"from\":1634886000000,\"to\":1672470000000},\"type\":\"historical\"}]}");
//		RuleCondition testRuleCondition = RuleConditionHistorical.ruleConditionFromJson(testRuleConditionJson);
//	}
//
//	@Test
//	public void
//	ruleConditionFromJson_ReturnsRuleConditionGroupContainingHistoricalCondition_When_ValidJsonObjectContainingTypeHistoricalIsProvided()
//	throws Exception {
//		//test
//		JsonUtilityService.JSONObject testRuleConditionJson =
//			platformServices.getJsonUtilityService().createJSONObject("{\"type\":\"group\",\"definition\":{\"conditions\":[{\"definition\":{\"key\":\"regioneventtype\",\"matcher\":\"eq\",\"values\":[\"entry\"]},\"type\":\"matcher\"},{\"definition\":{\"key\":\"triggeringregion.regionname\",\"matcher\":\"eq\",\"values\":[\"NormanY.MinetaSanJoseInternationalAirport\"]},\"type\":\"matcher\"},{\"definition\":{\"events\":[{\"xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID\":\"UIA-23558765\",\"xdm.eventType\":\"inapp.display\"},{\"xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID\":\"UIA-65098551\",\"xdm.eventType\":\"inapp.display\"}],\"searchType\":\"any\",\"matcher\":\"eq\",\"value\":0,\"from\":1634886000000,\"to\":1672470000000},\"type\":\"historical\"}],\"logic\":\"and\"}}");
//		RuleCondition testRuleCondition = RuleCondition.ruleConditionFromJson(testRuleConditionJson);
//		//verify
//		assertTrue(testRuleCondition instanceof RuleConditionAndGroup);
//		List<RuleCondition> conditions = ((RuleConditionAndGroup) testRuleCondition).conditions;
//		assertTrue(conditions.get(2) instanceof RuleConditionHistorical);
//	}
//
//	@Test
//	public void
//	historicalConditionToString_ReturnsHistoricalConditionInformation()
//	throws Exception {
//		// setup
//		String expectedString =
//			"(HISTORICAL EVENTS FOUND: {xdm.eventType=\"inapp.display\", xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID=\"UIA-23558765\"}, {xdm.eventType=\"inapp.display\", xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID=\"UIA-65098551\"})";
//		//test
//		JsonUtilityService.JSONObject testRuleConditionJson =
//			platformServices.getJsonUtilityService().createJSONObject("{\"definition\":{\"events\":[{\"xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID\":\"UIA-23558765\",\"xdm.eventType\":\"inapp.display\"},{\"xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID\":\"UIA-65098551\",\"xdm.eventType\":\"inapp.display\"}],\"searchType\":\"any\",\"matcher\":\"eq\",\"value\":0,\"from\":1634886000000,\"to\":1672470000000},\"type\":\"historical\"}]}");
//		RuleCondition testRuleCondition = RuleConditionHistorical.ruleConditionFromJson(testRuleConditionJson);
//		//verify
//		assertTrue(testRuleCondition instanceof RuleConditionHistorical);
//		assertEquals(expectedString, testRuleCondition.toString());
//	}
//
//	@Test
//	public void evaluate_Success_When_AnySearch_And_ValidHistoricalConditionTestEventsUsed() throws Exception {
//		// setup
//		// any search, matcher greater than 0, and 1 matching request
//		RuleTokenParser ruleTokenParser = new RuleTokenParser(testEventHub);
//		JsonUtilityService.JSONObject testHistoricalCondition =
//			platformServices.getJsonUtilityService().createJSONObject("{\"events\":[{\"xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID\":\"UIA-23558765\",\"xdm.eventType\":\"inapp.display\"},{\"xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID\":\"UIA-65098551\",\"xdm.eventType\":\"inapp.display\"}],\"searchType\":\"any\",\"matcher\":\"gt\",\"value\":0,\"from\":1634886000000,\"to\":1672470000000}");
//		RuleCondition testRuleHistoricalCondition = RuleConditionHistorical.historicalConditionFromJsonObject(
//					testHistoricalCondition);
//
//		String[] mask = {"xdm.eventType", "xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID"};
//		EventData testEventData = new EventData();
//		testEventData.putString("xdm.eventType", "inapp.display");
//		testEventData.putString("xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID",
//								"UIA-23558765");
//		Event testEvent = new Event.Builder("test", "edge",
//											EventSource.REQUEST_CONTENT.getName(), mask).setData(testEventData).build();
//		// record event in the TestEventHistory object
//		CountDownLatch latch = new CountDownLatch(1);
//		EventHistoryResultHandler<Boolean> handler = new EventHistoryResultHandler<Boolean>() {
//			@Override
//			public void call(Boolean value) {
//				assertTrue(value);
//				latch.countDown();
//			}
//		};
//		testEventHistory.recordEvent(testEvent, handler);
//		latch.await(1, TimeUnit.SECONDS);
//
//		//test
//		boolean result = testRuleHistoricalCondition.evaluate(ruleTokenParser, testEvent);
//		//verify
//		assertTrue(result);
//	}
//
//	@Test
//	public void evaluate_Failed_When_AnySearch_And_SearchResultsLessThanExpectedValue() throws Exception {
//		// setup
//		// any search, matcher greater than 2, and 1 matching request
//		RuleTokenParser ruleTokenParser = new RuleTokenParser(testEventHub);
//		JsonUtilityService.JSONObject testHistoricalCondition =
//			platformServices.getJsonUtilityService().createJSONObject("{\"events\":[{\"xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID\":\"UIA-23558765\",\"xdm.eventType\":\"inapp.display\"},{\"xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID\":\"UIA-65098551\",\"xdm.eventType\":\"inapp.display\"}],\"searchType\":\"any\",\"matcher\":\"gt\",\"value\":2,\"from\":1634886000000,\"to\":1672470000000}");
//		RuleCondition testRuleHistoricalCondition = RuleConditionHistorical.historicalConditionFromJsonObject(
//					testHistoricalCondition);
//
//		String[] mask = {"xdm.eventType", "xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID"};
//		EventData testEventData = new EventData();
//		testEventData.putString("xdm.eventType", "inapp.display");
//		testEventData.putString("xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID",
//								"UIA-23558765");
//		Event testEvent = new Event.Builder("test", "edge",
//											EventSource.REQUEST_CONTENT.getName(), mask).setData(testEventData).build();
//		// record event in the TestEventHistory object
//		CountDownLatch latch = new CountDownLatch(1);
//		EventHistoryResultHandler<Boolean> handler = new EventHistoryResultHandler<Boolean>() {
//			@Override
//			public void call(Boolean value) {
//				assertTrue(value);
//				latch.countDown();
//			}
//		};
//		testEventHistory.recordEvent(testEvent, handler);
//		latch.await(1, TimeUnit.SECONDS);
//
//		//test
//		boolean result = testRuleHistoricalCondition.evaluate(ruleTokenParser, testEvent);
//		//verify
//		assertFalse(result);
//	}
//
//	@Test
//	public void evaluate_Failed_When_RuleConditionHistoricalContainsEmptyRequestArray() throws Exception {
//		// setup
//		// any search, matcher greater than 2, and 1 matching request
//		RuleTokenParser ruleTokenParser = new RuleTokenParser(testEventHub);
//		JsonUtilityService.JSONObject testHistoricalCondition =
//			platformServices.getJsonUtilityService().createJSONObject("{\"events\":[],\"searchType\":\"any\",\"matcher\":\"gt\",\"value\":2,\"from\":1634886000000,\"to\":1672470000000}");
//		RuleCondition testRuleHistoricalCondition = RuleConditionHistorical.historicalConditionFromJsonObject(
//					testHistoricalCondition);
//
//		String[] mask = {"xdm.eventType", "xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID"};
//		EventData testEventData = new EventData();
//		testEventData.putString("xdm.eventType", "inapp.display");
//		testEventData.putString("xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID",
//								"UIA-23558765");
//		Event testEvent = new Event.Builder("test", "edge",
//											EventSource.REQUEST_CONTENT.getName(), mask).setData(testEventData).build();
//		// record event in the TestEventHistory object
//		CountDownLatch latch = new CountDownLatch(1);
//		EventHistoryResultHandler<Boolean> handler = new EventHistoryResultHandler<Boolean>() {
//			@Override
//			public void call(Boolean value) {
//				assertTrue(value);
//				latch.countDown();
//			}
//		};
//		testEventHistory.recordEvent(testEvent, handler);
//		latch.await(1, TimeUnit.SECONDS);
//
//		//test
//		boolean result = testRuleHistoricalCondition.evaluate(ruleTokenParser, testEvent);
//		//verify
//		assertFalse(result);
//	}
//
//	@Test
//	public void evaluate_Failed_When_RuleConditionHistoricalIsMissingRequestArray() throws Exception {
//		// setup
//		RuleTokenParser ruleTokenParser = new RuleTokenParser(testEventHub);
//		JsonUtilityService.JSONObject testHistoricalCondition =
//			platformServices.getJsonUtilityService().createJSONObject("{\"searchType\":\"any\",\"matcher\":\"gt\",\"value\":2,\"from\":1634886000000,\"to\":1672470000000}");
//		RuleCondition testRuleHistoricalCondition = RuleConditionHistorical.historicalConditionFromJsonObject(
//					testHistoricalCondition);
//
//		String[] mask = {"xdm.eventType", "xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID"};
//		EventData testEventData = new EventData();
//		testEventData.putString("xdm.eventType", "inapp.display");
//		testEventData.putString("xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID",
//								"UIA-23558765");
//		Event testEvent = new Event.Builder("test", "edge",
//											EventSource.REQUEST_CONTENT.getName(), mask).setData(testEventData).build();
//		// record event in the TestEventHistory object
//		CountDownLatch latch = new CountDownLatch(1);
//		EventHistoryResultHandler<Boolean> handler = new EventHistoryResultHandler<Boolean>() {
//			@Override
//			public void call(Boolean value) {
//				assertTrue(value);
//				latch.countDown();
//			}
//		};
//		testEventHistory.recordEvent(testEvent, handler);
//		latch.await(1, TimeUnit.SECONDS);
//
//		//test
//		boolean result = testRuleHistoricalCondition.evaluate(ruleTokenParser, testEvent);
//		//verify
//		assertFalse(result);
//	}
//
//	@Test
//	public void evaluate_Failed_When_NonMatchingHistoricalConditionTestEventsUsed() throws Exception {
//		// setup
//		// any search, matcher greater than 0, and 0 matching requests
//		RuleTokenParser ruleTokenParser = new RuleTokenParser(testEventHub);
//		JsonUtilityService.JSONObject testHistoricalCondition =
//			platformServices.getJsonUtilityService().createJSONObject("{\"events\":[{\"xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID\":\"UIA-23558765\",\"xdm.eventType\":\"inapp.display\"},{\"xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID\":\"UIA-65098551\",\"xdm.eventType\":\"inapp.display\"}],\"searchType\":\"any\",\"matcher\":\"gt\",\"value\":0,\"from\":1634886000000,\"to\":1672470000000}");
//		RuleCondition testRuleHistoricalCondition = RuleConditionHistorical.historicalConditionFromJsonObject(
//					testHistoricalCondition);
//
//		String[] mask = {"xdm.eventType", "xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID"};
//		EventData testEventData = new EventData();
//		testEventData.putString("xdm.eventType", "inapp.display");
//		testEventData.putString("xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID",
//								"a_different_execution_id");
//		Event testEvent = new Event.Builder("test", "edge",
//											EventSource.REQUEST_CONTENT.getName(), mask).setData(testEventData).build();
//		// record event in the TestEventHistory object
//		CountDownLatch latch = new CountDownLatch(1);
//		EventHistoryResultHandler<Boolean> handler = new EventHistoryResultHandler<Boolean>() {
//			@Override
//			public void call(Boolean value) {
//				assertTrue(value);
//				latch.countDown();
//			}
//		};
//		testEventHistory.recordEvent(testEvent, handler);
//		latch.await(1, TimeUnit.SECONDS);
//
//		//test
//		boolean result = testRuleHistoricalCondition.evaluate(ruleTokenParser, testEvent);
//		//verify
//		assertFalse(result);
//	}
//
//	@Test
//	public void evaluate_Success_When_OrderedSearch_And_OrderedHistoricalConditionTestEventsUsed_1() throws Exception {
//		// setup
//		// rule expects ordered events with message execution id "1", "2", "3" and events will be recorded in the order of "1","2","3"
//		RuleTokenParser ruleTokenParser = new RuleTokenParser(testEventHub);
//		JsonUtilityService.JSONObject testHistoricalCondition =
//			platformServices.getJsonUtilityService().createJSONObject("{\"events\":[{\"xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID\":\"1\",\"xdm.eventType\":\"inapp.display\"},{\"xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID\":\"2\",\"xdm.eventType\":\"inapp.display\"},{\"xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID\":\"3\",\"xdm.eventType\":\"inapp.display\"}],\"searchType\":\"ordered\",\"matcher\":\"eq\",\"value\":1,\"from\":0,\"to\":0}");
//		RuleCondition testRuleHistoricalCondition = RuleConditionHistorical.historicalConditionFromJsonObject(
//					testHistoricalCondition);
//
//		String[] mask = {"xdm.eventType", "xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID"};
//		// event 1
//		EventData testEventData = new EventData();
//		testEventData.putString("xdm.eventType", "inapp.display");
//		testEventData.putString("xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID",
//								"1");
//		Event testEvent = new Event.Builder("test", "edge",
//											EventSource.REQUEST_CONTENT.getName(), mask).setData(testEventData).build();
//		// record event 1 in the TestEventHistory object
//		final CountDownLatch latch = new CountDownLatch(1);
//		EventHistoryResultHandler<Boolean> handler = new EventHistoryResultHandler<Boolean>() {
//			@Override
//			public void call(Boolean value) {
//				assertTrue(value);
//				latch.countDown();
//			}
//		};
//		testEventHistory.recordEvent(testEvent, handler);
//		latch.await(1, TimeUnit.SECONDS);
//
//		// event 2
//		testEventData = new EventData();
//		testEventData.putString("xdm.eventType", "inapp.display");
//		testEventData.putString("xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID",
//								"2");
//		testEvent = new Event.Builder("test", "edge",
//									  EventSource.REQUEST_CONTENT.getName(), mask).setData(testEventData).build();
//		// record event 2 in the TestEventHistory object
//		final CountDownLatch latch2 = new CountDownLatch(1);
//		handler = new EventHistoryResultHandler<Boolean>() {
//			@Override
//			public void call(Boolean value) {
//				assertTrue(value);
//				latch2.countDown();
//			}
//		};
//		testEventHistory.recordEvent(testEvent, handler);
//		latch2.await(1, TimeUnit.SECONDS);
//
//		// event 3
//		testEventData = new EventData();
//		testEventData.putString("xdm.eventType", "inapp.display");
//		testEventData.putString("xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID",
//								"3");
//		testEvent = new Event.Builder("test", "edge",
//									  EventSource.REQUEST_CONTENT.getName(), mask).setData(testEventData).build();
//		// record event 3 in the TestEventHistory object
//		final CountDownLatch latch3 = new CountDownLatch(1);
//		handler = new EventHistoryResultHandler<Boolean>() {
//			@Override
//			public void call(Boolean value) {
//				assertTrue(value);
//				latch3.countDown();
//			}
//		};
//		testEventHistory.recordEvent(testEvent, handler);
//		latch3.await(1, TimeUnit.SECONDS);
//
//		//test
//		boolean result = testRuleHistoricalCondition.evaluate(ruleTokenParser, testEvent);
//		//verify
//		assertTrue(result);
//	}
//
//	@Test
//	public void evaluate_Success_When_OrderedSearch_And_OrderedHistoricalConditionTestEventsUsed_2() throws Exception {
//		// setup
//		// rule expects ordered events with message execution id "1", "2a", "3" and events will be recorded in the order of "1","2a","2b","3"
//		RuleTokenParser ruleTokenParser = new RuleTokenParser(testEventHub);
//		JsonUtilityService.JSONObject testHistoricalCondition =
//			platformServices.getJsonUtilityService().createJSONObject("{\"events\":[{\"xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID\":\"1\",\"xdm.eventType\":\"inapp.display\"},{\"xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID\":\"2a\",\"xdm.eventType\":\"inapp.display\"},{\"xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID\":\"3\",\"xdm.eventType\":\"inapp.display\"}],\"searchType\":\"ordered\",\"matcher\":\"eq\",\"value\":1,\"from\":0,\"to\":0}");
//		RuleCondition testRuleHistoricalCondition = RuleConditionHistorical.historicalConditionFromJsonObject(
//					testHistoricalCondition);
//
//		String[] mask = {"xdm.eventType", "xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID"};
//		// event 1
//		EventData testEventData = new EventData();
//		testEventData.putString("xdm.eventType", "inapp.display");
//		testEventData.putString("xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID",
//								"1");
//		Event testEvent = new Event.Builder("test", "edge",
//											EventSource.REQUEST_CONTENT.getName(), mask).setData(testEventData).build();
//		// record event 1 in the TestEventHistory object
//		final CountDownLatch latch = new CountDownLatch(1);
//		EventHistoryResultHandler<Boolean> handler = new EventHistoryResultHandler<Boolean>() {
//			@Override
//			public void call(Boolean value) {
//				assertTrue(value);
//				latch.countDown();
//			}
//		};
//		testEventHistory.recordEvent(testEvent, handler);
//		latch.await(1, TimeUnit.SECONDS);
//
//		// event 2
//		testEventData = new EventData();
//		testEventData.putString("xdm.eventType", "inapp.display");
//		testEventData.putString("xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID",
//								"2a");
//		testEvent = new Event.Builder("test", "edge",
//									  EventSource.REQUEST_CONTENT.getName(), mask).setData(testEventData).build();
//		// record event 2 in the TestEventHistory object
//		final CountDownLatch latch2 = new CountDownLatch(1);
//		handler = new EventHistoryResultHandler<Boolean>() {
//			@Override
//			public void call(Boolean value) {
//				assertTrue(value);
//				latch2.countDown();
//			}
//		};
//		testEventHistory.recordEvent(testEvent, handler);
//		latch2.await(1, TimeUnit.SECONDS);
//
//		// event 3
//		testEventData = new EventData();
//		testEventData.putString("xdm.eventType", "inapp.display");
//		testEventData.putString("xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID",
//								"2b");
//		testEvent = new Event.Builder("test", "edge",
//									  EventSource.REQUEST_CONTENT.getName(), mask).setData(testEventData).build();
//		// record event 3 in the TestEventHistory object
//		final CountDownLatch latch3 = new CountDownLatch(1);
//		handler = new EventHistoryResultHandler<Boolean>() {
//			@Override
//			public void call(Boolean value) {
//				assertTrue(value);
//				latch3.countDown();
//			}
//		};
//		testEventHistory.recordEvent(testEvent, handler);
//		latch3.await(1, TimeUnit.SECONDS);
//
//		// event 4
//		testEventData = new EventData();
//		testEventData.putString("xdm.eventType", "inapp.display");
//		testEventData.putString("xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID",
//								"3");
//		testEvent = new Event.Builder("test", "edge",
//									  EventSource.REQUEST_CONTENT.getName(), mask).setData(testEventData).build();
//		// record event 4 in the TestEventHistory object
//		final CountDownLatch latch4 = new CountDownLatch(1);
//		handler = new EventHistoryResultHandler<Boolean>() {
//			@Override
//			public void call(Boolean value) {
//				assertTrue(value);
//				latch4.countDown();
//			}
//		};
//		testEventHistory.recordEvent(testEvent, handler);
//		latch4.await(1, TimeUnit.SECONDS);
//
//		//test
//		boolean result = testRuleHistoricalCondition.evaluate(ruleTokenParser, testEvent);
//		//verify
//		assertTrue(result);
//	}
//
//	@Test
//	public void evaluate_Failed_When_OrderedSearch_And_OutOfOrderHistoricalConditionTestEventsUsed() throws Exception {
//		// setup
//		// rule expects ordered events with message execution id "b","a","c" but events will be recorded in the order of "b","c","a"
//		RuleTokenParser ruleTokenParser = new RuleTokenParser(testEventHub);
//		JsonUtilityService.JSONObject testHistoricalCondition =
//			platformServices.getJsonUtilityService().createJSONObject("{\"events\":[{\"xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID\":\"b\",\"xdm.eventType\":\"inapp.display\"},{\"xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID\":\"a\",\"xdm.eventType\":\"inapp.display\"},{\"xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID\":\"c\",\"xdm.eventType\":\"inapp.display\"}],\"searchType\":\"ordered\",\"matcher\":\"eq\",\"value\":1,\"from\":0,\"to\":0}");
//		RuleCondition testRuleHistoricalCondition = RuleConditionHistorical.historicalConditionFromJsonObject(
//					testHistoricalCondition);
//
//		String[] mask = {"xdm.eventType", "xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID"};
//		// event 1
//		EventData testEventData = new EventData();
//		testEventData.putString("xdm.eventType", "inapp.display");
//		testEventData.putString("xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID",
//								"b");
//		Event testEvent = new Event.Builder("test", "edge",
//											EventSource.REQUEST_CONTENT.getName(), mask).setData(testEventData).build();
//		// record event 1 in the TestEventHistory object
//		final CountDownLatch latch = new CountDownLatch(1);
//		EventHistoryResultHandler<Boolean> handler = new EventHistoryResultHandler<Boolean>() {
//			@Override
//			public void call(Boolean value) {
//				assertTrue(value);
//				latch.countDown();
//			}
//		};
//		testEventHistory.recordEvent(testEvent, handler);
//		latch.await(1, TimeUnit.SECONDS);
//		Thread.sleep(50);
//
//		// event 2
//		testEventData = new EventData();
//		testEventData.putString("xdm.eventType", "inapp.display");
//		testEventData.putString("xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID",
//								"c");
//		testEvent = new Event.Builder("test", "edge",
//									  EventSource.REQUEST_CONTENT.getName(), mask).setData(testEventData).build();
//		// record event 2 in the TestEventHistory object
//		final CountDownLatch latch2 = new CountDownLatch(1);
//		handler = new EventHistoryResultHandler<Boolean>() {
//			@Override
//			public void call(Boolean value) {
//				assertTrue(value);
//				latch2.countDown();
//			}
//		};
//		testEventHistory.recordEvent(testEvent, handler);
//		latch2.await(1, TimeUnit.SECONDS);
//		Thread.sleep(50);
//
//		// event 3
//		testEventData = new EventData();
//		testEventData.putString("xdm.eventType", "inapp.display");
//		testEventData.putString("xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID",
//								"a");
//		testEvent = new Event.Builder("test", "edge",
//									  EventSource.REQUEST_CONTENT.getName(), mask).setData(testEventData).build();
//		// record event 3 in the TestEventHistory object
//		final CountDownLatch latch3 = new CountDownLatch(1);
//		handler = new EventHistoryResultHandler<Boolean>() {
//			@Override
//			public void call(Boolean value) {
//				assertTrue(value);
//				latch3.countDown();
//			}
//		};
//		testEventHistory.recordEvent(testEvent, handler);
//		latch3.await(1, TimeUnit.SECONDS);
//
//		//test
//		boolean result = testRuleHistoricalCondition.evaluate(ruleTokenParser, testEvent);
//		//verify
//		assertFalse(result);
//	}
//
//	@Test
//	public void evaluate_Failed_When_OrderedSearch_And_IncompleteHistoricalConditionTestEventsUsed() throws Exception {
//		// setup
//		// rule expects ordered events with message execution id "b","a","c" but 2 events will be recorded in the order of "b","a"
//		RuleTokenParser ruleTokenParser = new RuleTokenParser(testEventHub);
//		JsonUtilityService.JSONObject testHistoricalCondition =
//			platformServices.getJsonUtilityService().createJSONObject("{\"events\":[{\"xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID\":\"b\",\"xdm.eventType\":\"inapp.display\"},{\"xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID\":\"a\",\"xdm.eventType\":\"inapp.display\"},{\"xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID\":\"c\",\"xdm.eventType\":\"inapp.display\"}],\"searchType\":\"ordered\",\"matcher\":\"eq\",\"value\":1,\"from\":0,\"to\":0}");
//		RuleCondition testRuleHistoricalCondition = RuleConditionHistorical.historicalConditionFromJsonObject(
//					testHistoricalCondition);
//
//		String[] mask = {"xdm.eventType", "xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID"};
//		// event 1
//		EventData testEventData = new EventData();
//		testEventData.putString("xdm.eventType", "inapp.display");
//		testEventData.putString("xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID",
//								"b");
//		Event testEvent = new Event.Builder("test", "edge",
//											EventSource.REQUEST_CONTENT.getName(), mask).setData(testEventData).build();
//		// record event 1 in the TestEventHistory object
//		final CountDownLatch latch = new CountDownLatch(1);
//		EventHistoryResultHandler<Boolean> handler = new EventHistoryResultHandler<Boolean>() {
//			@Override
//			public void call(Boolean value) {
//				assertTrue(value);
//				latch.countDown();
//			}
//		};
//		testEventHistory.recordEvent(testEvent, handler);
//		latch.await(1, TimeUnit.SECONDS);
//		Thread.sleep(50);
//
//		// event 2
//		testEventData = new EventData();
//		testEventData.putString("xdm.eventType", "inapp.display");
//		testEventData.putString("xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID",
//								"a");
//		testEvent = new Event.Builder("test", "edge",
//									  EventSource.REQUEST_CONTENT.getName(), mask).setData(testEventData).build();
//		// record event 2 in the TestEventHistory object
//		final CountDownLatch latch2 = new CountDownLatch(1);
//		handler = new EventHistoryResultHandler<Boolean>() {
//			@Override
//			public void call(Boolean value) {
//				assertTrue(value);
//				latch2.countDown();
//			}
//		};
//		testEventHistory.recordEvent(testEvent, handler);
//		latch2.await(1, TimeUnit.SECONDS);
//
//		//test
//		boolean result = testRuleHistoricalCondition.evaluate(ruleTokenParser, testEvent);
//		//verify
//		assertFalse(result);
//	}
//
//	@Test
//	public void evaluate_Success_When_OrderedSearch_And_OrderedHistoricalConditionWithinExtraTestEventsUsed() throws
//		Exception {
//		// setup
//		// rule expects ordered events with message execution id "a", "b", "c" and events will be recorded in the order of "a","bb","b","bbb","c"
//		RuleTokenParser ruleTokenParser = new RuleTokenParser(testEventHub);
//		JsonUtilityService.JSONObject testHistoricalCondition =
//			platformServices.getJsonUtilityService().createJSONObject("{\"events\":[{\"xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID\":\"a\",\"xdm.eventType\":\"inapp.display\"},{\"xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID\":\"b\",\"xdm.eventType\":\"inapp.display\"},{\"xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID\":\"c\",\"xdm.eventType\":\"inapp.display\"}],\"searchType\":\"ordered\",\"matcher\":\"eq\",\"value\":1,\"from\":0,\"to\":0}");
//		RuleCondition testRuleHistoricalCondition = RuleConditionHistorical.historicalConditionFromJsonObject(
//					testHistoricalCondition);
//
//		String[] mask = {"xdm.eventType", "xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID"};
//		// event 1
//		EventData testEventData = new EventData();
//		testEventData.putString("xdm.eventType", "inapp.display");
//		testEventData.putString("xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID",
//								"a");
//		Event testEvent = new Event.Builder("test", "edge",
//											EventSource.REQUEST_CONTENT.getName(), mask).setData(testEventData).build();
//		// record event 1 in the TestEventHistory object
//		final CountDownLatch latch = new CountDownLatch(1);
//		EventHistoryResultHandler<Boolean> handler = new EventHistoryResultHandler<Boolean>() {
//			@Override
//			public void call(Boolean value) {
//				assertTrue(value);
//				latch.countDown();
//			}
//		};
//		testEventHistory.recordEvent(testEvent, handler);
//		latch.await(1, TimeUnit.SECONDS);
//
//		// event 2
//		testEventData = new EventData();
//		testEventData.putString("xdm.eventType", "inapp.display");
//		testEventData.putString("xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID",
//								"bb");
//		testEvent = new Event.Builder("test", "edge",
//									  EventSource.REQUEST_CONTENT.getName(), mask).setData(testEventData).build();
//		// record event 2 in the TestEventHistory object
//		final CountDownLatch latch2 = new CountDownLatch(1);
//		handler = new EventHistoryResultHandler<Boolean>() {
//			@Override
//			public void call(Boolean value) {
//				assertTrue(value);
//				latch2.countDown();
//			}
//		};
//		testEventHistory.recordEvent(testEvent, handler);
//		latch2.await(1, TimeUnit.SECONDS);
//
//		// event 3
//		testEventData = new EventData();
//		testEventData.putString("xdm.eventType", "inapp.display");
//		testEventData.putString("xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID",
//								"b");
//		testEvent = new Event.Builder("test", "edge",
//									  EventSource.REQUEST_CONTENT.getName(), mask).setData(testEventData).build();
//		// record event 3 in the TestEventHistory object
//		final CountDownLatch latch3 = new CountDownLatch(1);
//		handler = new EventHistoryResultHandler<Boolean>() {
//			@Override
//			public void call(Boolean value) {
//				assertTrue(value);
//				latch3.countDown();
//			}
//		};
//		testEventHistory.recordEvent(testEvent, handler);
//		latch3.await(1, TimeUnit.SECONDS);
//
//		// event 4
//		testEventData = new EventData();
//		testEventData.putString("xdm.eventType", "inapp.display");
//		testEventData.putString("xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID",
//								"bbb");
//		testEvent = new Event.Builder("test", "edge",
//									  EventSource.REQUEST_CONTENT.getName(), mask).setData(testEventData).build();
//		// record event 4 in the TestEventHistory object
//		final CountDownLatch latch4 = new CountDownLatch(1);
//		handler = new EventHistoryResultHandler<Boolean>() {
//			@Override
//			public void call(Boolean value) {
//				assertTrue(value);
//				latch4.countDown();
//			}
//		};
//		testEventHistory.recordEvent(testEvent, handler);
//		latch4.await(1, TimeUnit.SECONDS);
//
//		// event 5
//		testEventData = new EventData();
//		testEventData.putString("xdm.eventType", "inapp.display");
//		testEventData.putString("xdm._experience.customerJourneyManagement.messageExecution.messageExecutionID",
//								"c");
//		testEvent = new Event.Builder("test", "edge",
//									  EventSource.REQUEST_CONTENT.getName(), mask).setData(testEventData).build();
//		// record event 5 in the TestEventHistory object
//		final CountDownLatch latch5 = new CountDownLatch(1);
//		handler = new EventHistoryResultHandler<Boolean>() {
//			@Override
//			public void call(Boolean value) {
//				assertTrue(value);
//				latch5.countDown();
//			}
//		};
//		testEventHistory.recordEvent(testEvent, handler);
//		latch5.await(1, TimeUnit.SECONDS);
//
//
//		//test
//		boolean result = testRuleHistoricalCondition.evaluate(ruleTokenParser, testEvent);
//		//verify
//		assertTrue(result);
//	}
}
