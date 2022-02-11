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

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

public class EventHistoryMaskTests {
//	private FakeEventHistory testEventHistory;
//	private HashMap<String, Object> data = new HashMap<String, Object>();
//	private static FakePlatformServices services = new FakePlatformServices();
//	private EventHub testEventHub;
//
//	@Before
//	public void setup() {
//		testEventHub = new EventHub("Test Hub", services);
//		testEventHistory = new FakeEventHistory();
//		EventHistoryProvider.setEventHistory(testEventHistory);
//	}
//
//	@After
//	public void teardown() {
//		testEventHistory.fakeEventHistoryDatabase.deleteDatabase();
//	}

//	@Test
//	public void eventDispatched_WithMask_ThenVerifyEntryAddedUsingEventHistoryGetEvents() throws Exception {
//		// setup
//		CountDownLatch latch = new CountDownLatch(1);
//		final String[] mask = {"a", "c"};
//		// setup event to be dispatched
//		data.put("a", "1");
//		data.put("b", "2");
//		data.put("c", 3);
//		final Event event = new Event.Builder("testName", "testType", EventSource.REQUEST_CONTENT.getName(), mask)
//		.setEventData(data)
//		.build();
//		testEventHub.dispatch(event);
//		// setup EventHistoryRequest and handler arrays for results
//		final int[] results = {0};
//		final HashMap<String, Variant> eventDataToMatch = new HashMap<String, Variant>();
//		eventDataToMatch.put("a", Variant.fromString("1"));
//		eventDataToMatch.put("c", Variant.fromInteger(3));
//		final EventHistoryRequest[] requests = new EventHistoryRequest[1];
//		final EventHistoryResultHandler<Integer> handler = new
//		EventHistoryResultHandler<Integer>() {
//			@Override
//			public void call(Integer value) {
//				results[0] = value;
//				latch.countDown();
//			}
//		};
//		final long currentTimeInMillis = System.currentTimeMillis();
//		final EventHistoryRequest request = new EventHistoryRequest(eventDataToMatch, 0,
//				currentTimeInMillis);
//		requests[0] = request;
//		// test
//		testEventHistory.getEvents(requests, false, handler);
//		latch.await();
//		// verify event was added to the event history database
//		int resultCount = results[0];
//		// verify 1 match in the event history database
//		assertEquals(1, resultCount);
//	}

//	@Test
//	public void multipleEventsDispatched_WithMask_ThenVerifyEntriesAddedUsingEventHistoryGetEvents() throws Exception {
//		// setup
//		CountDownLatch latch = new CountDownLatch(1);
//		final String[] mask = {"key1"};
//
//		// setup then dispatch events
//		for (int i = 0; i < 5; i++) {
//			// setup test data
//			data.put("key1", "valueA");
//			data.put("key2", "valueA");
//			final Event event = new Event.Builder("testName", "testType", EventSource.REQUEST_CONTENT.getName(), mask)
//			.setEventData(data)
//			.build();
//			testEventHub.dispatch(event);
//		}
//
//		for (int i = 0; i < 3; i++) {
//			// setup test data
//			data.put("key1", "valueB");
//			data.put("key2", "valueB");
//			final Event event = new Event.Builder("testName", "testType", EventSource.REQUEST_CONTENT.getName(), mask)
//			.setEventData(data)
//			.build();
//			testEventHub.dispatch(event);
//		}
//
//		// setup EventHistoryRequest and handler arrays for results
//		final int[] results = {0};
//		final HashMap<String, Variant> eventDataToMatch = new HashMap<String, Variant>();
//		eventDataToMatch.put("key1", Variant.fromString("valueA"));
//		final HashMap<String, Variant> eventDataToMatch2 = new HashMap<String, Variant>();
//		eventDataToMatch2.put("key1", Variant.fromString("valueB"));
//		final EventHistoryRequest[] requests = new EventHistoryRequest[2];
//		final EventHistoryResultHandler<Integer> handler = new
//		EventHistoryResultHandler<Integer>() {
//			@Override
//			public void call(Integer value) {
//				results[0] = value;
//				latch.countDown();
//			}
//		};
//		final long currentTimeInMillis = System.currentTimeMillis();
//		final EventHistoryRequest request = new EventHistoryRequest(eventDataToMatch, 0,
//				currentTimeInMillis);
//		final EventHistoryRequest request2 = new EventHistoryRequest(eventDataToMatch2, 0,
//				currentTimeInMillis);
//		requests[0] = request;
//		requests[1] = request2;
//		// test
//		testEventHistory.getEvents(requests, false, handler);
//		latch.await();
//		// verify 8 total events were added to the event history database
//		assertEquals(8, results[0]);
//	}

//	@Test
//	public void eventDispatched_WithNoMask_ThenVerifyEntryNotAddedUsingEventHistoryGetEvents() throws Exception {
//		// setup
//		CountDownLatch latch = new CountDownLatch(1);
//		// setup event to be dispatched
//		// setup test data
//		data.put("a", "1");
//		data.put("b", "2");
//		data.put("c", 3);
//		final Event event = new Event.Builder("testName", "testType", EventSource.REQUEST_CONTENT.getName())
//		.setEventData(data)
//		.build();
//		testEventHub.dispatch(event);
//		// setup EventHistoryRequest and handler for results
//		final int[] results = {0};
//		final HashMap<String, Variant> eventDataToMatch = new HashMap<String, Variant>();
//		eventDataToMatch.put("a", Variant.fromString("1"));
//		eventDataToMatch.put("b", Variant.fromString("2"));
//		eventDataToMatch.put("c", Variant.fromInteger(3));
//		final EventHistoryResultHandler<Integer> handler = new
//		EventHistoryResultHandler<Integer>() {
//			@Override
//			public void call(Integer value) {
//				results[0] = value;
//				latch.countDown();
//			}
//		};
//		final long currentTimeInMillis = System.currentTimeMillis();
//		final EventHistoryRequest request = new EventHistoryRequest(eventDataToMatch, 0,
//				currentTimeInMillis);
//		final EventHistoryRequest[] requests = new EventHistoryRequest[1];
//		requests[0] = request;
//		// test
//		testEventHistory.getEvents(requests, false, handler);
//		latch.await();
//		// verify no matches in the event history database
//		assertEquals(0, results[0]);
//	}

//	@Test
//	public void multipleEventsDispatched_ThenEventHistoryDeleteEvents_VerifyMatchingEventsDeleted() throws Exception {
//		// setup
//		CountDownLatch latch = new CountDownLatch(1);
//		final String[] mask = {"key1"};
//
//		// setup then dispatch events
//		for (int i = 0; i < 6; i++) {
//			// setup test data
//			data.put("key1", "valueA");
//			data.put("key2", "valueA");
//			final Event event = new Event.Builder("testName", "testType", EventSource.REQUEST_CONTENT.getName(), mask)
//			.setEventData(data)
//			.build();
//			testEventHub.dispatch(event);
//		}
//
//		for (int i = 0; i < 8; i++) {
//			// setup test data
//			data.put("key1", "valueB");
//			data.put("key2", "valueB");
//			final Event event = new Event.Builder("testName", "testType", EventSource.REQUEST_CONTENT.getName(), mask)
//			.setEventData(data)
//			.build();
//			testEventHub.dispatch(event);
//		}
//
//		for (int i = 0; i < 5; i++) {
//			// setup test data
//			data.put("key1", "valueC");
//			data.put("key2", "valueC");
//			final Event event = new Event.Builder("testName", "testType", EventSource.REQUEST_CONTENT.getName(), mask)
//			.setEventData(data)
//			.build();
//			testEventHub.dispatch(event);
//		}
//
//		// setup EventHistoryRequest and handler arrays for results
//		final int results[] = {0};
//		final HashMap<String, Variant> eventDataToMatch = new HashMap<String, Variant>();
//		eventDataToMatch.put("key1", Variant.fromString("valueA"));
//		final HashMap<String, Variant> eventDataToMatch2 = new HashMap<String, Variant>();
//		eventDataToMatch2.put("key1", Variant.fromString("valueB"));
//		final EventHistoryRequest[] requests = new EventHistoryRequest[2];
//		final EventHistoryResultHandler<Integer> handler = new
//		EventHistoryResultHandler<Integer>() {
//			@Override
//			public void call(Integer value) {
//				results[0] = value;
//				latch.countDown();
//			}
//		};
//		final long currentTimeInMillis = System.currentTimeMillis();
//		final EventHistoryRequest request = new EventHistoryRequest(eventDataToMatch, 0,
//				currentTimeInMillis);
//		final EventHistoryRequest request2 = new EventHistoryRequest(eventDataToMatch2, 0,
//				currentTimeInMillis);
//		requests[0] = request;
//		requests[1] = request2;
//		// test
//		testEventHistory.deleteEvents(requests, handler);
//		latch.await();
//		// verify 14 events deleted
//		assertEquals(14, results[0]);
//	}
}