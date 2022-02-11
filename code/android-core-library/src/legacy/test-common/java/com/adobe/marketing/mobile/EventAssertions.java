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

import java.util.Map;

import static org.junit.Assert.*;

public class EventAssertions {

	public static void assertEvent(Event event, EventType expectedEventType, EventSource expectedEventSource) {
		assertEventSource(event, expectedEventSource);
		assertEventType(event, expectedEventType);
	}

	public static void assertEventSource(Event event, EventSource expectedEventSource) {
		assertEquals(expectedEventSource, event.getEventSource());
	}

	public static void assertEventType(Event event, EventType expectedEventType) {
		assertEquals(expectedEventType, event.getEventType());
	}

	public static void assertEventNumber(Event event, int expectedEventNumber) {
		assertEquals(expectedEventNumber, event.getEventNumber());
	}

	public static void assertEventResponsePairID(Event event, String expectedResponsePairID) {
		assertEquals(expectedResponsePairID, event.getResponsePairID());
	}

	public static void assertEventTimeStamp(Event event, long expectedTimeStamp) {
		assertEquals(expectedTimeStamp, event.getTimestamp());
	}

	public static void assertEventDataEquals(Event event, EventData eventData) {
		assertEquals(eventData, event.getData());
	}

	public static void assertEventDataContains(Event event, String key, Map<String, String> value) {
		assertEventDataContains(event, key, Variant.fromStringMap(value));
	}

	public static void assertEventDataContains(Event event, String key, boolean value) {
		assertEventDataContains(event, key, Variant.fromBoolean(value));
	}

	public static void assertEventDataContains(Event event, String key, String value) {
		assertEventDataContains(event, key, Variant.fromString(value));
	}

	public static void assertEventDataContains(Event event, String key, Variant value) {
		assertEventDataContains(event, key);
		EventData eventData = event.getData();
		assertEquals(value, eventData.optVariant(key, null));
	}

	public static void assertEventDataContains(Event event, String key) {
		EventData eventData = event.getData();
		assertNotNull("Event Data was null", eventData);
		assertTrue("Event Data does not contain key: " + key, eventData.containsKey(key));
	}

}
