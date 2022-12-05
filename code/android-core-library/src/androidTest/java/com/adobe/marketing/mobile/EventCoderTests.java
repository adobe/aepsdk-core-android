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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class EventCoderTests {

    @Test
    public void testEncode_When_EventIsNull() {
        assertNull(EventCoder.encode(null));
    }

    @Test
    public void testDecode_When_StringIsNull() {
        assertNull(EventCoder.decode(null));
    }

    @Test
    public void testDecode_When_StringIsInvalidJson() {
        assertNull(EventCoder.decode(""));
        assertNull(EventCoder.decode("{}"));
        assertNull(EventCoder.decode("null"));
    }

    @Test
    public void testEncodeDecode_When_AllTheFieldsAreNull() {
        Event event =
                new Event.Builder(null, "type", "source")
                        .setEventData(null)
                        .setResponseId(null)
                        .setUniqueIdentifier(null)
                        .build();

        Event decodedEvent = EventCoder.decode(EventCoder.encode(event));
        verifyEventEquals(event, decodedEvent);
    }

    @Test
    public void testEncodeDecode_When_AllTheFieldsAreValid() {
        Map<String, Object> data =
                new HashMap<String, Object>() {
                    {
                        put("int", 3);
                    }
                };

        Event event =
                new Event.Builder("name", "type", "source", new String[] {"mask_1", "mask_2"})
                        .setEventData(data)
                        .setResponseId("response id")
                        .setUniqueIdentifier("uuid")
                        .build();
        Event decodedEvent = EventCoder.decode(EventCoder.encode(event));
        verifyEventEquals(event, decodedEvent);
    }

    @Test
    public void testEncodeDecode_When_AllSupportEventDataTypes() {
        Map<String, Object> data =
                new HashMap<String, Object>() {
                    {
                        // Basic types
                        put("int", 3);
                        put("double", 3.11d);
                        put("long", Long.MAX_VALUE);
                        put("String", "abcd");
                        put("boolean", true);

                        // Lists of items
                        put("listOfStrings", Arrays.asList("One", "Two", "Three"));
                        put("listOfInts", Arrays.asList(1, 2, 3));
                        put("listOfLong", Arrays.asList(Long.MIN_VALUE, Long.MAX_VALUE));
                        put("listOfDouble", Arrays.asList(1.2d, 2.3d, 3.4d));
                        put("listWithNull", Arrays.asList("NonNull", null));
                        put("listOfBooleans", Arrays.asList(true, false));

                        // Map of items
                        put(
                                "map",
                                new HashMap<String, Object>() {
                                    {
                                        put("int", 3);
                                        put("double", 3.11d);
                                        put("long", Long.MAX_VALUE);
                                        put("null", null);
                                        put("String", "abcd");
                                        put("boolean", true);
                                        put("map", true);
                                        put("listOfInts", Arrays.asList(1, 2, 3));
                                        put(
                                                "listOfLong",
                                                Arrays.asList(Long.MIN_VALUE, Long.MAX_VALUE));
                                        put("listOfDouble", Arrays.asList(1.2d, 2.3d, 3.4d));
                                        put("listWithNull", Arrays.asList("NonNull", null));
                                        put("listOfBooleans", Arrays.asList(true, false));
                                    }
                                });

                        // List of Maps
                        put(
                                "list",
                                Arrays.asList(
                                        new HashMap<String, Object>() {
                                            {
                                                put("int", 3);
                                                put("doube", 3.11d);
                                                put("long", Long.MAX_VALUE);
                                                put("null", null);
                                                put("String", "abcd");
                                                put("boolean", true);
                                                put("map", true);
                                                put("listOfInts", Arrays.asList(1, 2, 3));
                                                put(
                                                        "listOfLong",
                                                        Arrays.asList(
                                                                Long.MIN_VALUE, Long.MAX_VALUE));
                                                put(
                                                        "listOfDouble",
                                                        Arrays.asList(1.2d, 2.3d, 3.4d));
                                                put("listWithNull", Arrays.asList("NonNull", null));
                                                put("listOfBooleans", Arrays.asList(true, false));
                                            }
                                        },
                                        new HashMap<String, Object>() {
                                            {
                                                put("int", 3);
                                                put("double", 3.11d);
                                                put("long", Long.MAX_VALUE);
                                                put("null", null);
                                                put("String", "abcd");
                                                put("boolean", true);
                                                put("map", true);
                                            }
                                        }));
                    }
                };

        Event event =
                new Event.Builder("name", "type", "source", new String[] {"mask1", "mask2"})
                        .setEventData(data)
                        .setResponseId("response id")
                        .setUniqueIdentifier("uuid")
                        .build();
        Event decodedEvent = EventCoder.decode(EventCoder.encode(event));
        verifyEventEquals(event, decodedEvent);
    }

    @Test
    public void testEncodeDecode_When_PrimitiveArray() {
        Map<String, Object> data =
                new HashMap<String, Object>() {
                    {
                        put("array", new String[] {"a", "b", "c"});
                    }
                };

        Event event =
                new Event.Builder("name", "type", "source")
                        .setEventData(data)
                        .setResponseId("response id")
                        .setUniqueIdentifier("uuid")
                        .build();
        Event decodedEvent = EventCoder.decode(EventCoder.encode(event));

        assertEquals(Arrays.asList("a", "b", "c"), decodedEvent.getEventData().get("array"));
    }

    @Test
    public void testEncodeDecode_When_UnsupportedEventDataType() {
        Map<String, Object> data =
                new HashMap<String, Object>() {
                    {
                        put("object", new EventCoderTests());
                    }
                };

        Event event =
                new Event.Builder("name", "type", "source")
                        .setEventData(data)
                        .setResponseId("response id")
                        .setUniqueIdentifier("uuid")
                        .build();

        Event decodedEvent = EventCoder.decode(EventCoder.encode(event));
        assertNull(event.getEventData());
        assertNull(decodedEvent.getEventData());
    }

    @Test
    public void testEncodeDecode_When_SmallLongValue() {
        Map<String, Object> data =
                new HashMap<String, Object>() {
                    {
                        put("long", 3L);
                    }
                };

        Event event =
                new Event.Builder("name", "type", "source")
                        .setEventData(data)
                        .setResponseId("response id")
                        .setUniqueIdentifier("uuid")
                        .build();

        Event decodedEvent = EventCoder.decode(EventCoder.encode(event));
        assertTrue(decodedEvent.getEventData().get("long") instanceof Integer);
        assertEquals(3, decodedEvent.getEventData().get("long"));
    }

    private void verifyEventEquals(final Event a, final Event b) {
        assertEquals(a.getName(), b.getName());
        assertEquals(a.getTimestamp(), b.getTimestamp());
        assertEquals(a.getType(), b.getType());
        assertEquals(a.getSource(), b.getSource());
        assertEquals(a.getUniqueIdentifier(), b.getUniqueIdentifier());
        assertEquals(a.getResponseID(), b.getResponseID());
        assertEquals(a.getEventData(), b.getEventData());
        assertTrue(Arrays.equals(a.getMask(), b.getMask()));
    }
}
