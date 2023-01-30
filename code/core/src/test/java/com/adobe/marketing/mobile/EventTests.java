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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class EventTests {

    private String mockEventName;
    private String mockEventSource;
    private String mockEventType;
    private Map<String, Object> mockEventData;
    private long mockTimestamp;

    @Before
    public void setup() {
        mockEventName = "testEventName";
        mockEventType = EventType.ANALYTICS;
        mockEventSource = EventSource.REQUEST_CONTENT;

        mockEventData = new HashMap<>();
        mockEventData.put("key", "value");

        mockTimestamp = 12345L;
    }

    @Test
    public void EventConstructor_HappyPath() {
        Event event = new Event.Builder(mockEventName, mockEventType, mockEventSource).build();

        assertEquals(mockEventName, event.getName());
        assertEquals(mockEventType, event.getType());
        assertEquals(mockEventSource, event.getSource());
        assertNotEquals(0, event.getTimestamp());
        assertTrue(event.getUniqueIdentifier().length() > 0);
    }

    @Test
    public void EventConstructor_StringParams_NullEventType() {
        Event event = new Event.Builder(mockEventName, null, mockEventSource).build();

        assertNull(event);
    }

    @Test
    public void EventConstructor_StringParams_NullEventSource() {
        Event event = new Event.Builder(mockEventName, mockEventType, null).build();

        assertNull(event);
    }

    @Test
    public void EventConstructor_StringParams_NullTypeAndSource() {
        Event event = new Event.Builder(mockEventName, (String) null, null).build();

        assertNull(event);
    }

    @Test
    public void Event_inResponseToEventWorksProperly() {
        Event triggerEvent =
                new Event.Builder("TriggerEvent", "responseType", "responseSource").build();
        Event event =
                new Event.Builder(mockEventName, mockEventType, mockEventSource)
                        .inResponseToEvent(triggerEvent)
                        .build();

        assertEquals(triggerEvent.getUniqueIdentifier(), event.getResponseID());
    }

    @Test(expected = NullPointerException.class)
    public void Event_inResponseToNullEventWorksProperly() {
        Event event =
                new Event.Builder(mockEventName, mockEventType, mockEventSource)
                        .inResponseToEvent(null)
                        .build();

        assertEquals(null, event.getResponseID());
    }

    @Test
    public void Event_eventDataWorksProperly() {
        Event event =
                new Event.Builder(mockEventName, mockEventType, mockEventSource)
                        .setEventData(mockEventData)
                        .build();

        assertEquals(mockEventData, event.getEventData());
    }

    @Test
    public void Event_uniqueIdWorksProperly() {
        Event event = new Event.Builder(mockEventName, mockEventType, mockEventSource).build();

        Event event1 = new Event.Builder(mockEventName, mockEventType, mockEventSource).build();

        assertNotEquals(event.getUniqueIdentifier(), event1.getUniqueIdentifier());
    }

    @Test
    public void Event_setEventDataMap_worksProperly() throws Exception {
        Map<String, Object> eventData = new HashMap<String, Object>();
        eventData.put("string", "key");
        eventData.put("int", 200);
        eventData.put(
                "list",
                new ArrayList<String>() {
                    {
                        add("bla");
                    }
                });
        Event event =
                new Event.Builder(mockEventName, mockEventType, mockEventSource)
                        .setEventData(eventData)
                        .build();

        assertEquals(eventData, event.getEventData());
    }

    @Test
    public void Event_setEventDataMap_mapIsImmutable() throws Exception {
        Map<String, Object> eventData = new HashMap<String, Object>();
        eventData.put("string", "key");
        eventData.put("int", 200);
        eventData.put(
                "list",
                new ArrayList<String>() {
                    {
                        add("bla");
                    }
                });

        Event event =
                new Event.Builder(mockEventName, mockEventType, mockEventSource)
                        .setEventData(eventData)
                        .build();
        eventData.put("new", "key");
        eventData.put("awesome", "test");

        Map<String, Object> expectedData = new HashMap<String, Object>();
        expectedData.put("string", "key");
        expectedData.put("int", 200);
        expectedData.put(
                "list",
                new ArrayList<String>() {
                    {
                        add("bla");
                    }
                });
        assertEquals(3, event.getEventData().size());
        assertEquals(expectedData, event.getEventData());
    }

    @Test
    public void Event_setEventDataMap_nullData() {
        Event event =
                new Event.Builder(mockEventName, mockEventType, mockEventSource)
                        .setEventData(null)
                        .build();

        assertNull(event.getEventData());
    }

    @Test
    public void Event_getEventDataMap_worksProperly() throws Exception {
        Event event =
                new Event.Builder(mockEventName, mockEventType, mockEventSource)
                        .setEventData(mockEventData)
                        .build();

        assertEquals(mockEventData, event.getEventData());
    }

    @Test
    public void Event_getEventDataMap_returnedMapIsImmutable() throws Exception {
        Event event =
                new Event.Builder(mockEventName, mockEventType, mockEventSource)
                        .setEventData(mockEventData)
                        .build();

        Map<String, Object> resultData = event.getEventData();
        assertEquals(1, resultData.size());
        assertThrows(
                UnsupportedOperationException.class,
                () -> {
                    resultData.put("new", "key");
                });
        assertEquals(1, event.getEventData().size());
        assertThrows(
                UnsupportedOperationException.class,
                () -> {
                    resultData.clear();
                });
        assertEquals(1, event.getEventData().size());
    }

    @Test
    public void Event_getEventDataMap_nullData() {
        Event event = new Event.Builder(mockEventName, mockEventType, mockEventSource).build();
        assertEquals(event.getEventData(), null);
    }

    @Test
    public void Event_timestampWorksProperly() {
        Event event =
                new Event.Builder(mockEventName, mockEventType, mockEventSource)
                        .setTimestamp(mockTimestamp)
                        .build();

        assertEquals(mockTimestamp, event.getTimestamp());
    }

    @Test
    public void Event_builderWorksProperly() {
        Event triggerEvent =
                new Event.Builder("TriggerEvent", "responseType", "responseSource").build();

        Event event =
                new Event.Builder(mockEventName, mockEventType, mockEventSource)
                        .inResponseToEvent(triggerEvent)
                        .setTimestamp(mockTimestamp)
                        .setEventData(mockEventData)
                        .build();

        assertEquals(mockEventName, event.getName());
        assertEquals(mockEventType, event.getType());
        assertEquals(mockEventSource, event.getSource());
        assertEquals(mockTimestamp, event.getTimestamp());
        assertEquals(triggerEvent.getUniqueIdentifier(), event.getResponseID());
        assertEquals(mockEventData, event.getEventData());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void Event_usingBuilderAfterBuildThrows() throws UnsupportedOperationException {
        Event.Builder builder = new Event.Builder(mockEventName, mockEventType, mockEventSource);

        builder.setTimestamp(mockTimestamp).setEventData(mockEventData).build();

        builder.setEventData(mockEventData);
    }
}
