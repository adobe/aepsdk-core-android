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

import android.app.Activity;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("all")
@RunWith(AndroidJUnit4.class)
public class EventCoderTests {

	void verfiyEventEquals(final Event a, final Event b) {
		assertEquals(a.getName(), b.getName());
		assertEquals(a.getTimestamp(), b.getTimestamp());
		assertEquals(a.getType(), b.getType());
		assertEquals(a.getSource(), b.getSource());
		assertEquals(a.getUniqueIdentifier(), b.getUniqueIdentifier());
		assertEquals(a.getResponseID(), b.getResponseID());
		assertEquals(a.getEventData(), b.getEventData());
	}

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
		Event event = new Event.Builder(null, "type", "source")
		.setEventData(null)
		.setResponseId(null)
		.setUniqueIdentifier(null)
		.build();

		Event decodedEvent = EventCoder.decode(EventCoder.encode(event));
		verfiyEventEquals(event, decodedEvent);
	}

	@Test
	public void testEncodeDecode_When_AllTheFieldsAreValid() {
		Map<String, Object> data = new HashMap() {
			{
				put("int", 3);
			}
		};

		Event event = new Event.Builder("name", "type", "source")
		.setEventData(data)
		.setResponseId("response id")
		.setUniqueIdentifier("uuid")
		.build();
		String a = EventCoder.encode(event);
		Event decodedEvent = EventCoder.decode(EventCoder.encode(event));
		verfiyEventEquals(event, decodedEvent);
	}

	@Test
	public void testEncodeDecode_When_AllSupportEventDataTypes() {
		// Todo revisit these tests
		Map<String, Object> data = new HashMap() {
			{
				put("int", 3);
				put("doube", 3.11d);
				put("long", Long.MAX_VALUE);
				put("null", null);
				put("String", "abcd");
				put("boolean", true);
				put("map", new HashMap() {
					{
						put("int", 3);
						put("doube", 3.11d);
						put("long", Long.MAX_VALUE);
						put("null", null);
						put("String", "abcd");
						put("boolean", true);
						put("map", true);
					}
				});
				put("list", Arrays.asList(new Map[] {new HashMap() {
						{
							put("int", 3);
							put("doube", 3.11d);
							put("long", Long.MAX_VALUE);
							put("null", null);
							put("String", "abcd");
							put("boolean", true);
							put("map", true);
						}
					}, new HashMap() {
						{
							put("int", 3);
							put("doube", 3.11d);
							put("long", Long.MAX_VALUE);
							put("null", null);
							put("String", "abcd");
							put("boolean", true);
							put("map", true);
						}
					}
				}));
			}
		};

		Event event = new Event.Builder("name", "type", "source")
		.setEventData(data)
		.setResponseId("response id")
		.setUniqueIdentifier("uuid")
		.build();
		String a = EventCoder.encode(event);
		Event decodedEvent = EventCoder.decode(EventCoder.encode(event));
		verfiyEventEquals(event, decodedEvent);
	}

	@Test
	public void testEncodeDecode_When_PrimitiveArray() {
		Map<String, Object> data = new HashMap() {
			{
				put("array", new String[] {"a", "b", "c"});
			}
		};

		Event event = new Event.Builder("name", "type", "source")
		.setEventData(data)
		.setResponseId("response id")
		.setUniqueIdentifier("uuid")
		.build();
		String a = EventCoder.encode(event);
		Event decodedEvent = EventCoder.decode(EventCoder.encode(event));

		assertEquals(Arrays.asList(new String[] {"a", "b", "c"}), decodedEvent.getEventData().get("array"));
	}

	@Test
	public void testEncodeDecode_When_UnsupportedEventDataType() {
		Map<String, Object> data = new HashMap() {
			{
				put("object", new EventCoderTests());
			}
		};

		Event event = new Event.Builder("name", "type", "source")
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
		Map<String, Object> data = new HashMap() {
			{
				put("long", 3l);
			}
		};

		Event event = new Event.Builder("name", "type", "source")
		.setEventData(data)
		.setResponseId("response id")
		.setUniqueIdentifier("uuid")
		.build();
		String a = EventCoder.encode(event);
		Event decodedEvent = EventCoder.decode(EventCoder.encode(event));
		assertTrue(decodedEvent.getEventData().get("long") instanceof Integer);
		assertEquals(3, decodedEvent.getEventData().get("long"));
	}
}
