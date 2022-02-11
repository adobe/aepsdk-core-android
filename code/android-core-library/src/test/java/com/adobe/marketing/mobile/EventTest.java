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
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class EventTest {

	private String mockEventName;
	private EventSource mockEventSource;
	private EventType mockEventType;
	private String mockPairId;
	private String mockResponsePairId;
	private EventData mockEventData;
	private long mockTimestamp;
	private String mockError;
	private int mockEventNumber;
	private String mockStringEventType;
	private String mockStringEventSource;

	@Before()
	public void setup() {
		mockEventName = "testEventName";
		mockEventType = EventType.ANALYTICS;
		mockEventSource = EventSource.REQUEST_CONTENT;
		mockPairId = "testPairId";
		mockResponsePairId = "testResponsePairId";
		mockEventData = new EventData().putString("key", "value");
		mockTimestamp = 12345L;
		mockError = "testError";
		mockEventNumber = 1234;
		mockStringEventSource = "my.event.source";
		mockStringEventType = "my.event.type";
	}

	@Test
	public void EventConstructor_HappyPath() {
		Event event = new Event.Builder(mockEventName, mockEventType, mockEventSource)
		.build();

		assertEquals(mockEventName, event.getName());
		assertEquals(mockEventType, event.getEventType());
		assertEquals(mockEventSource, event.getEventSource());
		assertNotEquals(0, event.getTimestamp());
		assertTrue(event.getUniqueIdentifier().length() > 0);
	}

	@Test
	public void EventConstructor_StringParams_HappyPath() {
		Event event = new Event.Builder(mockEventName, mockStringEventType, mockStringEventSource)
		.build();

		assertEquals(mockEventName, event.getName());
		assertEquals(mockStringEventType, event.getType());
		assertEquals(mockStringEventSource, event.getSource());
		assertNotEquals(0, event.getTimestamp());
		assertTrue(event.getUniqueIdentifier().length() > 0);
	}

	@Test
	public void EventConstructor_StringParams_NullEventType() {
		Event event = new Event.Builder(mockEventName, null, mockStringEventSource)
		.build();

		assertNull(event);
	}

	@Test
	public void EventConstructor_StringParams_NullEventSource() {
		Event event = new Event.Builder(mockEventName, mockStringEventType, null)
		.build();

		assertNull(event);
	}

	@Test
	public void EventConstructor_StringParams_NullTypeAndSource() {
		Event event = new Event.Builder(mockEventName, (String)null, null)
		.build();

		assertNull(event);
	}

	@Test
	public void Event_pairIdWorksProperly() {
		Event event = new Event.Builder(mockEventName, mockEventType, mockEventSource)
		.setPairID(mockPairId)
		.build();

		assertEquals(mockPairId, event.getPairID());
	}

	@Test
	public void Event_responsePairIdWorksProperly() {
		Event event = new Event.Builder(mockEventName, mockEventType, mockEventSource)
		.setResponsePairID(mockResponsePairId)
		.build();

		assertEquals(mockResponsePairId, event.getResponsePairID());
	}

	@Test
	public void Event_eventDataWorksProperly() {
		Event event = new Event.Builder(mockEventName, mockEventType, mockEventSource)
		.setData(mockEventData)
		.build();

		assertEquals(mockEventData, event.getData());
	}

	@Test
	public void Event_uniqueIdWorksProperly() {
		Event event = new Event.Builder(mockEventName, mockEventType, mockEventSource)
		.setPairID(mockPairId)
		.build();

		Event event1 = new Event.Builder(mockEventName, mockEventType, mockEventSource)
		.setPairID(mockPairId)
		.build();

		assertNotEquals(event.getUniqueIdentifier(), event1.getUniqueIdentifier());
	}

	@Test
	public void Event_setEventDataMap_worksProperly() throws Exception {
		Map<String, Object> eventData = new HashMap<String, Object>();
		eventData.put("string", "key");
		eventData.put("int", 200);
		eventData.put("list", new ArrayList<String>() {
			{
				add("bla");
			}
		});
		EventData expectedData = EventData.fromObjectMap(eventData);
		Event event = new Event.Builder(mockEventName, mockEventType, mockEventSource)
		.setEventData(eventData)
		.build();

		assertEquals(expectedData, event.getData());
	}

	@Test
	public void Event_setEventDataMap_mapIsImmutable() throws Exception {
		Map<String, Object> eventData = new HashMap<String, Object>();
		eventData.put("string", "key");
		eventData.put("int", 200);
		eventData.put("list", new ArrayList<String>() {
			{
				add("bla");
			}
		});
		EventData expectedData = EventData.fromObjectMap(eventData);
		Event event = new Event.Builder(mockEventName, mockEventType, mockEventSource)
		.setEventData(eventData)
		.build();
		eventData.put("new", "key");
		eventData.put("awesome", "test");

		assertEquals(3, event.getData().size());
		assertEquals(expectedData, event.getData());
	}

	@Test
	public void Event_setEventDataMap_nullData() {
		Event event = new Event.Builder(mockEventName, mockEventType, mockEventSource)
		.setEventData(null)
		.build();

		assertTrue(event.getData().isEmpty());
	}

	@Test
	public void Event_getEventDataMap_worksProperly() throws Exception {
		Event event = new Event.Builder(mockEventName, mockEventType, mockEventSource)
		.setData(mockEventData)
		.build();

		assertEquals(mockEventData.toObjectMap(), event.getEventData());
	}

	@Test
	public void Event_getEventDataMap_returnedMapIsImmutable() throws Exception {
		Event event = new Event.Builder(mockEventName, mockEventType, mockEventSource)
		.setData(mockEventData)
		.build();

		Map<String, Object> resultData = event.getEventData();
		assertEquals(1, resultData.size());
		resultData.put("new", "key");
		assertEquals(1, event.getEventData().size());
		resultData.clear();
		assertEquals(1, event.getEventData().size());
	}

	@Test
	public void Event_getEventDataMap_nullData() {
		Event event = new Event.Builder(mockEventName, mockEventType, mockEventSource).build();

		assertTrue(event.getEventData().isEmpty());
	}

	@Test
	public void Event_timestampWorksProperly() {
		Event event = new Event.Builder(mockEventName, mockEventType, mockEventSource)
		.setTimestamp(mockTimestamp)
		.build();

		assertEquals(mockTimestamp, event.getTimestamp());
	}

	@Test
	public void Event_eventNumberWorksProperly() {
		Event event = new Event.Builder(mockEventName, mockEventType, mockEventSource)
		.setEventNumber(mockEventNumber)
		.build();

		assertEquals(mockEventNumber, event.getEventNumber());
	}

	@Test
	public void sharedStateOldest_EventNumberEqualsIntMinValue() {
		assertEquals(0, Event.SHARED_STATE_OLDEST.getEventNumber());
	}

	@Test
	public void sharedStateNewest_EventNumberEqualsIntMaxValue() {
		assertEquals(Integer.MAX_VALUE, Event.SHARED_STATE_NEWEST.getEventNumber());
	}

	@Test
	public void Event_builderWorksProperly() {
		Event event = new Event.Builder(mockEventName, mockEventType, mockEventSource)
		.setPairID(mockPairId)
		.setResponsePairID(mockResponsePairId)
		.setTimestamp(mockTimestamp)
		.setData(mockEventData)
		.build();

		event.setEventNumber(mockEventNumber);

		assertEquals(mockEventName, event.getName());
		assertEquals(mockEventType, event.getEventType());
		assertEquals(mockEventSource, event.getEventSource());
		assertEquals(mockEventNumber, event.getEventNumber());
		assertEquals(mockTimestamp, event.getTimestamp());
		assertEquals(mockPairId, event.getPairID());
		assertEquals(mockResponsePairId, event.getResponsePairID());
		assertEquals(mockEventData, event.getData());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void Event_usingBuilderAfterBuildThrows() throws UnsupportedOperationException {
		Event.Builder builder = new Event.Builder(mockEventName, mockEventType, mockEventSource);

		builder
		.setPairID(mockPairId)
		.setResponsePairID(mockResponsePairId)
		.setTimestamp(mockTimestamp)
		.setData(mockEventData)
		.build();

		builder.setPairID(mockPairId);
	}

	@Test
	public void Event_copyWorksProperly() {
		EventData eventData = new EventData().putString("key", "value");
		Event event = new Event.Builder(mockEventName, mockEventType, mockEventSource)
		.setPairID(mockPairId)
		.setResponsePairID(mockResponsePairId)
		.setTimestamp(mockTimestamp)
		.setData(eventData)
		.build();

		event.setEventNumber(mockEventNumber);
		Event eventCopy = event.copy();

		assertEquals(eventCopy.getName(), event.getName());
		assertEquals(eventCopy.getEventType(), event.getEventType());
		assertEquals(eventCopy.getEventSource(), event.getEventSource());
		assertEquals(eventCopy.getEventNumber(), event.getEventNumber());
		assertEquals(eventCopy.getTimestamp(), event.getTimestamp());
		assertEquals(eventCopy.getPairID(), event.getPairID());
		assertEquals(eventCopy.getResponsePairID(), event.getResponsePairID());
		assertEquals(eventCopy.getData(), event.getData());
		assertEquals(eventCopy.getUniqueIdentifier(), event.getUniqueIdentifier());
		assertEquals(eventCopy.getEventData(), event.getEventData());
	}

	@Test
	public void EventType_getWorksProperly() {
		assertEquals(EventType.ANALYTICS, EventType.get("com.adobe.eventType.analytics"));
		assertEquals(EventType.AUDIENCEMANAGER, EventType.get("com.adobe.eventType.audienceManager"));
		assertEquals(EventType.TARGET, EventType.get("com.adobe.eventType.target"));
		assertEquals(EventType.LIFECYCLE, EventType.get("com.adobe.eventType.lifecycle"));
		assertEquals(EventType.LOCATION, EventType.get("com.adobe.eventType.location"));
		assertEquals(EventType.PII, EventType.get("com.adobe.eventType.pii"));
		assertEquals(EventType.IDENTITY, EventType.get("com.adobe.eventType.identity"));
		assertEquals(EventType.CONFIGURATION, EventType.get("com.adobe.eventType.configuration"));
		assertEquals(EventType.CUSTOM, EventType.get("com.adobe.eventType.custom"));
		assertEquals(EventType.ACQUISITION, EventType.get("com.adobe.eventType.acquisition"));
		assertEquals(EventType.SYSTEM, EventType.get("com.adobe.eventType.system"));
		assertEquals(EventType.USERPROFILE, EventType.get("com.adobe.eventType.userProfile"));
		assertEquals(EventType.HUB, EventType.get("com.adobe.eventType.hub"));
		assertEquals(EventType.RULES_ENGINE, EventType.get("com.adobe.eventType.rulesEngine"));
		assertEquals(EventType.CAMPAIGN, EventType.get("com.adobe.eventType.campaign"));
		assertEquals(EventType.SIGNAL, EventType.get("com.adobe.eventType.signal"));
	}

	@Test
	public void EventType_getWorksCaseInsensitive() {
		assertEquals(EventType.ANALYTICS, EventType.get("Com.aDObe.evEnTTypE.ANalYTicS"));
	}

	@Test
	public void EventSource_getWorksProperly() {
		assertEquals(EventSource.NONE, EventSource.get("com.adobe.eventSource.none"));
		assertEquals(EventSource.OS, EventSource.get("com.adobe.eventSource.os"));
		assertEquals(EventSource.REQUEST_CONTENT, EventSource.get("com.adobe.eventSource.requestContent"));
		assertEquals(EventSource.REQUEST_IDENTITY, EventSource.get("com.adobe.eventSource.requestIdentity"));
		assertEquals(EventSource.REQUEST_PROFILE, EventSource.get("com.adobe.eventSource.requestProfile"));
		assertEquals(EventSource.REQUEST_RESET, EventSource.get("com.adobe.eventSource.requestReset"));
		assertEquals(EventSource.RESPONSE_CONTENT, EventSource.get("com.adobe.eventSource.responseContent"));
		assertEquals(EventSource.RESPONSE_IDENTITY, EventSource.get("com.adobe.eventSource.responseIdentity"));
		assertEquals(EventSource.RESPONSE_PROFILE, EventSource.get("com.adobe.eventSource.responseProfile"));
		assertEquals(EventSource.SHARED_STATE, EventSource.get("com.adobe.eventSource.sharedState"));
		assertEquals(EventSource.BOOTED, EventSource.get("com.adobe.eventSource.booted"));
	}

	@Test
	public void EventSource_getWorksCaseInsensitive() {
		assertEquals(EventSource.REQUEST_IDENTITY, EventSource.get("Com.aDObe.EvEnTsOuRcE.reQUESTidentitY"));
	}
}
