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

import com.adobe.marketing.mobile.utils.DataReader;

public class EventAssertions {

	public static void assertEvent(Event event, String expectedEventType, String expectedEventSource) {
		assertEventSource(event, expectedEventSource);
		assertEventType(event, expectedEventType);
	}

	public static void assertEventSource(Event event, String expectedEventSource) {
		assertEquals(expectedEventSource, event.getSource());
	}

	public static void assertEventType(Event event, String expectedEventType) {
		assertEquals(expectedEventType, event.getType());
	}

	public static void assertEventResponseID(Event event, String expectedResponseID) {
		assertEquals(expectedResponseID, event.getResponseID());
	}

	public static void assertEventTimeStamp(Event event, long expectedTimeStamp) {
		assertEquals(expectedTimeStamp, event.getTimestamp());
	}

	public static void assertEventDataEquals(Event event, Map<String, Object> eventData) {
		assertEquals(eventData, event.getEventData());
	}

	public static void assertEventDataContains(Event event, String key, Map<String, String> value) {
		assertEventDataContains(event, key, value);
	}

	public static void assertEventDataContains(Event event, String key, boolean value) {
		assertEventDataContains(event, key, value);
	}

	public static void assertEventDataContains(Event event, String key, String value) {
		assertEventDataContains(event, key, value);
	}

	public static void assertEventDataContains(Event event, String key, Object value) {
		assertEventDataContains(event, key);
		Map<String, Object> eventData = event.getEventData();
		assertEquals(value, eventData.get(key));
	}

	public static void assertEventDataContains(Event event, String key) {
		Map<String, Object> eventData = event.getEventData();
		assertNotNull("Event Data was null", eventData);
		assertTrue("Event Data does not contain key: " + key, eventData.containsKey(key));
	}

}
