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

import org.junit.*;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class EventDataTest {
	@Rule
	public final ExpectedException exception = ExpectedException.none();

	static class Circle {
		private final int radius;

		public Circle(final int radius) {
			this.radius = radius;
		}

		public int getRadius() {
			return radius;
		}

		@Override
		public boolean equals(final Object right) {
			if (right == null) {
				return false;
			}

			if (right.getClass() != getClass()) {
				return false;
			}

			Integer myRadius = radius;
			Integer rightRadius = ((Circle)right).radius;
			return myRadius.equals(rightRadius);
		}
	}

	static class CircleSerializer implements VariantSerializer<Circle> {
		private static final CircleSerializer instance = new CircleSerializer();
		private CircleSerializer() {}

		public static CircleSerializer getInstance() {
			return instance;
		}

		@Override
		public Variant serialize(final Circle obj) {
			if (obj == null) {
				return Variant.fromNull();
			}

			Map<String, Variant> map = new HashMap<String, Variant>();
			map.put("radius", Variant.fromInteger(obj.radius));
			return Variant.fromVariantMap(map);
		}

		@Override
		public Circle deserialize(final Variant obj) throws VariantException {
			if (obj.getKind() == VariantKind.NULL) {
				return null;
			}

			final Map<String, Variant> map = obj.getVariantMap();
			final int radius = Variant.getVariantFromMap(map, "radius").getInteger();
			return new Circle(radius);
		}
	}

	@Test
	public void getString_MapContainsEntry() {
		EventData eventData = new EventData();
		eventData.putString("key", "value");
		//test
		assertEquals("value", eventData.getString("key"));
	}

	@Test
	public void getStringReturnNull_MapDoesNotContainEntry() {
		EventData eventData = new EventData();
		//test
		assertNull(eventData.getString("key"));
	}

	@Test
	public void optString_MapContainsEntry() {
		EventData eventData = new EventData();
		eventData.putString("key", "value");
		//test
		assertEquals("value", eventData.optString("key", "wrong"));
	}

	@Test
	public void optStringReturnFallbackValue_MapDoesNotContainEntry() {
		EventData eventData = new EventData();
		//test
		assertEquals("fallback", eventData.optString("key", "fallback"));
	}

	@Test
	public void optStringReturnFallbackValue_MapContainsWrongTypeEntry() {
		EventData eventData = new EventData();
		eventData.putBoolean("key", true);
		//test
		assertEquals("fallback", eventData.optString("key", "fallback"));
	}

	@Test
	public void getLong_MapContainsEntry() {
		EventData eventData = new EventData();
		eventData.putLong("key", 3L);
		//test
		assertEquals(eventData.getLong("key", 0), 3L);
	}

	@Test
	public void getLongReturnDefault_MapDoesNotContainEntry() {
		EventData eventData = new EventData();
		//test
		assertEquals(0L, eventData.getLong("key", 0L));
	}

	@Test
	public void optLong_MapContainsEntry() {
		EventData eventData = new EventData();
		eventData.putLong("key", 3L);
		//test
		assertEquals(eventData.getLong("key", 5L), 3L);
	}

	@Test
	public void optLongReturnFallbackValue_MapDoesNotContainEntry() {
		EventData eventData = new EventData();
		//test
		assertEquals(eventData.getLong("key", 5L), 5L);
	}

	@Test
	public void optLongReturnFallbackValue_MapContainsWrongTypeEntry() {
		EventData eventData = new EventData();
		eventData.putBoolean("key", true);
		//test
		assertEquals(eventData.getLong("key", 5L), 5L);
	}

	@Test
	public void getInteger_MapContainsEntry() {
		EventData eventData = new EventData();
		eventData.putInt("key", 3);
		//test
		assertEquals(3, eventData.getInt("key", 0));
	}

	@Test
	public void getIntegerReturnDefault_MapDoesNotContainEntry() {
		EventData eventData = new EventData();
		//test
		assertEquals(0, eventData.getInt("key", 0));
	}

	@Test
	public void optInteger_MapContainsEntry() {
		EventData eventData = new EventData();
		eventData.putInt("key", 3);
		//test
		assertEquals(eventData.getInt("key", 5), 3);
	}

	@Test
	public void optIntegerReturnFallbackValue_MapDoesNotContainEntry() {
		EventData eventData = new EventData();
		//test
		assertEquals(eventData.getInt("key", 5), 5);
	}

	@Test
	public void optIntegerReturnFallbackValue_MapContainsWrongTypeEntry() {
		EventData eventData = new EventData();
		eventData.putBoolean("key", true);
		//test
		assertEquals(eventData.getInt("key", 5), 5);
	}

	@Test
	public void getBoolean_MapContainsEntry() {
		EventData eventData = new EventData();
		eventData.putBoolean("key", true);
		//test
		assertTrue(eventData.getBoolean("key", false));
	}

	@Test
	public void getBooleanReturnDefault_MapDoesNotContainEntry() {
		EventData eventData = new EventData();
		//test
		assertFalse(eventData.getBoolean("key", false));
	}

	@Test
	public void optBoolean_MapContainsEntry() {
		EventData eventData = new EventData();
		eventData.putBoolean("key", true);
		//test
		assertEquals(eventData.getBoolean("key", false), true);
	}

	@Test
	public void optBooleanReturnFallbackValue_MapDoesNotContainEntry() {
		EventData eventData = new EventData();
		//test
		assertEquals(eventData.getBoolean("key", false), false);
	}

	@Test
	public void optBooleanReturnFallbackValue_MapContainsWrongTypeEntry() {
		EventData eventData = new EventData();
		eventData.putString("key", "value");
		//test
		assertEquals(eventData.getBoolean("key", false), false);
	}

	@Test
	public void getMap_MapContainsEntry() {
		Map<String, String> testMap = new HashMap<String, String>() {
			{
				put("key", "value");
			}
		};
		EventData eventData = new EventData();
		eventData.putMap("key", testMap);
		//test
		assertEquals(eventData.getMap("key"), testMap);
	}

	@Test
	public void getMapnReturnNull_MapDoesNotContainEntry() {
		EventData eventData = new EventData();
		//test
		assertNull(eventData.getMap("key"));
	}

	@Test
	public void
	testGetMapReturnedMapDoesNotContainNullKey_TheOriginalMapContainsNullKeyAndNullValueEntries() {
		Map<String, String> testMap = new HashMap<String, String>() {
			{
				put("key", "value");
				put(null, "value");
				put("nullvalue", null);
			}
		};
		EventData eventData = new EventData();
		eventData.putMap("key", testMap);
		//test
		assertEquals(eventData.getMap("key"), new HashMap<String, String>() {
			{
				put("key", "value");
				put("nullvalue", null);
			}
		});
	}

	@Test
	public void copyConstructor() {
		EventData original = new EventData().putString("string", "value")
		.putInt("int", 55)
		.putBoolean("boolean", true)
		.putMap("map", new HashMap<String, String>() {
			{
				put("key0", "value0");
				put("key1", "value1");
			}
		})
		.putLong("long", 55L);
		EventData copy = new EventData(original);

		assertEquals(original, copy);
	}

	@Test
	public void hashCode_equalEventData() {
		EventData original = new EventData().putString("string", "value")
		.putInt("int", 55)
		.putBoolean("boolean", true)
		.putMap("map", new HashMap<String, String>() {
			{
				put("key0", "value0");
				put("key1", "value1");
			}
		})
		.putLong("long", 55L);
		EventData copy = new EventData(original);

		assertEquals(original.hashCode(), copy.hashCode());
	}

	@Test
	public void constructorFromMap() throws Exception {
		Map<String, Variant> map = new HashMap<String, Variant>() {
			{
				put("string", Variant.fromString("value"));
				put("int", Variant.fromInteger(55));
				put("boolean", Variant.fromBoolean(true));
				put("map", Variant.fromStringMap(new HashMap<String, String>() {
					{
						put("key0", "value0");
						put("key1", "value1");
					}
				}));
				put("long", Variant.fromLong(55L));
			}
		};
		EventData eventData = new EventData(map);

		assertEquals("value", eventData.getString("string"));
		assertEquals(55, eventData.getInt("int", 0));
		assertTrue(eventData.getBoolean("boolean", false));
		assertEquals(new HashMap<String, String>() {
			{
				put("key0", "value0");
				put("key1", "value1");
			}
		}, eventData.getMap("map"));
		assertEquals(55L, (long) eventData.getLong("long", 0L));
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorFromMapThrowsOnNullMap() {
		new EventData((Map<String, Variant>)null);
	}

	@Test
	public void constructorFromMapRemovesNullKeys() throws Exception {
		final Map<String, Variant> map = new HashMap<String, Variant>() {
			{
				put("key0", Variant.fromString("value0"));
				put(null, Variant.fromString("value1"));
				put("key2", Variant.fromString("value2"));
			}
		};
		final EventData eventData = new EventData(map);
		assertEquals(2, eventData.size());
		assertEquals("value0", eventData.getString2("key0"));
		assertEquals("value2", eventData.getString2("key2"));
	}

	@Test
	public void constructorFromMapReplacesJavaNullsWithVariantNulls() throws Exception {
		final Map<String, Variant> map = new HashMap<String, Variant>() {
			{
				put("key0", Variant.fromString("value0"));
				put("key1", null);
				put("key2", Variant.fromString("value2"));
			}
		};
		final EventData eventData = new EventData(map);
		assertEquals(3, eventData.size());
		assertEquals("value0", eventData.getString2("key0"));
		assertEquals(VariantKind.NULL, eventData.getKind("key1"));
		assertEquals("value2", eventData.getString2("key2"));
	}

	@Test
	public void copyConstructorWorks() throws Exception {
		final Map<String, Variant> map = new HashMap<String, Variant>() {
			{
				put("key0", Variant.fromString("value0"));
				put("key1", Variant.fromString("value1"));
			}
		};
		final EventData eventData1 = new EventData(map);
		final EventData eventData2 = new EventData(eventData1);
		assertEquals(2, eventData2.size());
		assertEquals("value0", eventData2.getString2("key0"));
		assertEquals("value1", eventData2.getString2("key1"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void copyConstructorThrowsOnNullEventData() {
		new EventData((EventData) null);
	}

	@Test
	public void isEmpty_True() {
		assertTrue(new EventData().isEmpty());
	}

	@Test
	public void isEmpty_False() {
		EventData eventData = new EventData().putString("key", "value");
		assertFalse(eventData.isEmpty());
	}

	@Test
	public void size_isEmpty() {
		assertEquals(0, new EventData().size());
	}

	@Test
	public void size_isNotEmpty() {
		assertEquals(2, new EventData().putString("key0", "value0")
					 .putString("key1", "value1")
					 .size());
	}

	@Test
	public void keys_isEmpty() {
		assertEquals(new HashSet<String>() {
			{
				add("key0");
				add("key1");
			}
		}, new EventData().putString("key0", "value0")
		.putString("key1", "value1")
		.keys());
	}

	@Test
	public void keys_isNotEmpty() {
		assertEquals(new HashSet<String>(), new EventData().keys());
	}

	@Test
	public void containsKey_True() {
		EventData eventData = new EventData().putString("key", "value");
		assertTrue(eventData.containsKey("key"));
	}

	@Test
	public void containsKey_False() {
		EventData eventData = new EventData();
		assertFalse(eventData.containsKey("key"));
	}

	/*
	@Test
	public void copy_eventDataEmptyWorks() {
	    EventData eventData = new EventData();
	    EventData eventDataCopy = eventData.copy();
	    assertNotEquals(eventData, eventDataCopy);
	}

	@Test
	public void copy_eventDataNotEmptyWorks() {
	    EventData eventData = new EventData()
	            .putString("key1", "value1")
	            .putString("key2", "value2");

	    EventData eventDataCopy = eventData.copy();
	    assertNotEquals(eventData, eventDataCopy);

	    eventData.putString("key3", "value3");
	    assertEquals("value1", eventDataCopy.optString("key1", "missing"));
	    assertEquals("value2", eventDataCopy.optString("key2", "missing"));
	    assertEquals("missing", eventDataCopy.optString("key3", "missing"));
	}
	*/
	@SuppressWarnings("all")
	@Test
	public void equals_Null() {
		EventData eventData = new EventData();
		assertFalse(eventData.equals(null));
	}

	@Test
	public void getKindWorksForPresentKey() throws Exception {
		EventData eventData = new EventData();
		eventData.putString("key", "value");
		VariantKind kind = eventData.getKind("key");
		assertNotNull(kind);
		assertEquals(VariantKind.STRING, kind);
	}

	@Test(expected = IllegalArgumentException.class)
	public void getKindThrowsOnNullKey() throws Exception {
		new EventData().getKind(null);
	}

	@Test(expected = VariantKeyNotFoundException.class)
	public void getKindThrowsOnMissingKey() throws Exception {
		new EventData().getKind("key");
	}

	@Test
	public void containsKeyWorksForPresentKey() {
		final EventData eventData = new EventData();
		eventData.putString("key", "value");
		assertTrue(eventData.containsKey("key"));
	}

	@Test
	public void containsKeyWorksForMissingKey() {
		assertFalse(new EventData().containsKey("key"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void containsKeyThrowsOnNullKey() {
		new EventData().containsKey(null);
	}

	@Test
	public void asMapCopyWorks() {
		final EventData eventData = new EventData();
		eventData.putString("key", "value");
		final Map<String, Variant> map = eventData.asMapCopy();
		assertNotNull(map);
		assertEquals(1, map.size());
		assertEquals(Variant.fromString("value"), map.get("key"));
	}

	@Test
	public void asMapCopyDoesNotModifyEventData() throws Exception {
		final EventData eventData = new EventData();
		eventData.putString("key", "value");
		final Map<String, Variant> map = eventData.asMapCopy();
		map.put("key", Variant.fromString("value2"));
		assertEquals("value", eventData.getString2("key"));
	}

	@Test
	public void copyWorks() throws VariantException {
		final EventData eventData = new EventData();
		eventData.putString("key", "value");
		final EventData copy = eventData.copy();
		assertTrue(eventData != copy);
		assertTrue(eventData.getClass() == copy.getClass());
		assertTrue(eventData.equals(copy));
		eventData.putString("key", "newValue");
		assertEquals("newValue", eventData.getString2("key"));
		assertEquals("value", copy.getString2("key"));
	}

	@Test
	public void testPutVariant() {
		final EventData eventData = new EventData();
		final Variant variant = Variant.fromString("hi");
		assertSame(eventData, eventData.putVariant("k", variant));
		assertEquals(variant, eventData.asMapCopy().get("k"));
	}

	@Test
	public void testPutNull() {
		final EventData eventData = new EventData();
		assertSame(eventData, eventData.putNull("k"));
		assertEquals(Variant.fromNull(), eventData.asMapCopy().get("k"));
	}

	@Test
	public void testPutString() {
		final EventData eventData = new EventData();
		assertSame(eventData, eventData.putString("k", "hi"));
		assertEquals(Variant.fromString("hi"), eventData.asMapCopy().get("k"));
	}

	@Test
	public void testPutBoolean() {
		final EventData eventData = new EventData();
		assertSame(eventData, eventData.putBoolean("k", true));
		assertEquals(Variant.fromBoolean(true), eventData.asMapCopy().get("k"));
	}

	@Test
	public void testPutInteger() {
		final EventData eventData = new EventData();
		assertSame(eventData, eventData.putInteger("k", 42));
		assertEquals(Variant.fromInteger(42), eventData.asMapCopy().get("k"));
	}

	@Test
	public void testPutLong() {
		final EventData eventData = new EventData();
		assertSame(eventData, eventData.putLong("k", 42L));
		assertEquals(Variant.fromLong(42L), eventData.asMapCopy().get("k"));
	}

	@Test
	public void testPutDouble() {
		final EventData eventData = new EventData();
		assertSame(eventData, eventData.putDouble("k", 4.2));
		assertEquals(Variant.fromDouble(4.2), eventData.asMapCopy().get("k"));
	}

	@Test
	public void testPutVariantMap() {
		final EventData eventData = new EventData();
		final Map<String, Variant> map = new HashMap<String, Variant>() {
			{
				put("one", Variant.fromInteger(1));
			}
		};
		assertSame(eventData, eventData.putVariantMap("k", map));
		assertEquals(Variant.fromVariantMap(map), eventData.asMapCopy().get("k"));
	}

	@Test
	public void testPutVariantList() {
		final EventData eventData = new EventData();
		final List<Variant> list = Arrays.asList(Variant.fromInteger(1));
		assertSame(eventData, eventData.putVariantList("k", list));
		assertEquals(Variant.fromVariantList(list), eventData.asMapCopy().get("k"));
	}

	@Test
	public void testPutStringList() {
		final EventData eventData = new EventData();
		final List<String> list = Arrays.asList("hi");
		assertSame(eventData, eventData.putStringList("k", list));
		assertEquals(Variant.fromStringList(list), eventData.asMapCopy().get("k"));
	}

	@Test
	public void testPutStringMap() {
		final EventData eventData = new EventData();
		final Map<String, String> map = new HashMap<String, String>() {
			{
				put("one", "1");
			}
		};
		assertSame(eventData, eventData.putStringMap("k", map));
		assertEquals(Variant.fromStringMap(map), eventData.asMapCopy().get("k"));
	}

	@Test
	public void testPutTypedObject() throws Exception {
		final EventData eventData = new EventData();
		final Circle circle = new Circle(42);
		assertSame(eventData, eventData.putTypedObject("k", circle, new CircleSerializer()));
		assertEquals(Variant.fromTypedObject(circle, new CircleSerializer()), eventData.asMapCopy().get("k"));
	}

	@Test
	public void testPutTypedList() {
		final EventData eventData = new EventData();
		final List<Circle> list = Arrays.asList(new Circle(42));
		assertSame(eventData, eventData.putTypedList("k", list, new CircleSerializer()));
		assertEquals(Variant.fromTypedList(list, new CircleSerializer()), eventData.asMapCopy().get("k"));
	}

	@Test
	public void testPutTypedMap() {
		final EventData eventData = new EventData();
		final Map<String, Circle> map = new HashMap<String, Circle>() {
			{
				put("one", new Circle(42));
			}
		};
		assertSame(eventData, eventData.putTypedMap("k", map, new CircleSerializer()));
		assertEquals(Variant.fromTypedMap(map, new CircleSerializer()), eventData.asMapCopy().get("k"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPutVariantWithNullKey() {
		new EventData().putVariant(null, Variant.fromInteger(1));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPutNullWithNullKey() {
		new EventData().putNull(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPutStringWithNullKey() {
		new EventData().putString(null, "hi");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPutBooleanWithNullKey() {
		new EventData().putBoolean(null, true);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPutIntegerWithNullKey() {
		new EventData().putInteger(null, 42);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPutLongWithNullKey() {
		new EventData().putLong(null, 42L);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPutDoubleWithNullKey() {
		new EventData().putDouble(null, 4.2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPutVariantMapWithNullKey() {
		final Map<String, Variant> map = new HashMap<String, Variant>() {
			{
				put("one", Variant.fromInteger(1));
			}
		};
		new EventData().putVariantMap(null, map);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPutVariantListWithNullKey() {

		final List<Variant> list = Arrays.asList(Variant.fromInteger(1));
		new EventData().putVariantList(null, list);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPutStringListWithNullKey() {
		final List<String> list = Arrays.asList("hi");
		new EventData().putStringList(null, list);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPutStringMapWithNullKey() {
		final Map<String, String> map = new HashMap<String, String>() {
			{
				put("one", "1");
			}
		};
		new EventData().putStringMap(null, map);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPutTypedObjectWithNullKey() throws Exception {
		new EventData().putTypedObject(null, new Circle(42), new CircleSerializer());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPutTypedListWithNullKey() {
		new EventData().putTypedList(null, Arrays.asList(new Circle(42)), new CircleSerializer());
	}

	@Test
	public void testPutTypedMapWithNullKey() {
		final Map<String, Circle> map = new HashMap<String, Circle>() {
			{
				put("one", new Circle(42));
			}
		};
		exception.expect(IllegalArgumentException.class);
		new EventData().putTypedMap(null, map, new CircleSerializer());
	}

	@Test
	public void testPutVariantWithNullValue() {
		final EventData eventData = new EventData().putVariant("k", null);
		assertEquals(Variant.fromNull(), eventData.asMapCopy().get("k"));
	}

	@Test
	public void testPutStringWithNullValue() {
		final EventData eventData = new EventData().putString("k", null);
		assertEquals(Variant.fromNull(), eventData.asMapCopy().get("k"));
	}

	@Test
	public void testPutVariantMapWithNullValue() {
		final EventData eventData = new EventData().putVariantMap("k", null);
		assertEquals(Variant.fromNull(), eventData.asMapCopy().get("k"));
	}

	@Test
	public void testPutVariantListWithNullValue() {
		final EventData eventData = new EventData().putVariantList("k", null);
		assertEquals(Variant.fromNull(), eventData.asMapCopy().get("k"));
	}

	@Test
	public void testPutStringListWithNullValue() {
		final EventData eventData = new EventData().putStringList("k", null);
		assertEquals(Variant.fromNull(), eventData.asMapCopy().get("k"));
	}

	@Test
	public void testPutStringMapWithNullValue() {
		final EventData eventData = new EventData().putStringMap("k", null);
		assertEquals(Variant.fromNull(), eventData.asMapCopy().get("k"));
	}

	@Test
	public void testPutTypedObjectWithNullValue() throws Exception {
		final EventData eventData = new EventData();
		eventData.putTypedObject("k", null, new CircleSerializer());
		assertEquals(Variant.fromNull(), eventData.getVariant("k"));
	}

	@Test
	public void testPutTypedListWithNullValue() {
		final EventData eventData = new EventData().putTypedList("k", null, new CircleSerializer());
		assertEquals(Variant.fromNull(), eventData.asMapCopy().get("k"));
	}

	@Test
	public void testPutTypedMapWithNullValue() {
		final EventData eventData = new EventData().putTypedMap("k", null, new CircleSerializer());
		assertEquals(Variant.fromNull(), eventData.asMapCopy().get("k"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPutTypedObjectWithNullSerializer() throws Exception {
		final Circle circle = new Circle(42);
		new EventData().putTypedObject("k", circle, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPutTypedListWithNullSerializer() {
		final List<Circle> list = Arrays.asList(new Circle(42));
		new EventData().putTypedList("k", list, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPutTypedMapWithNullSerializer() {
		final Map<String, Circle> map = new HashMap<String, Circle>() {
			{
				put("one", new Circle(42));
			}
		};
		new EventData().putTypedMap("k", map, null);
	}

	@Test
	public void testGetVariant() throws Exception {
		final EventData eventData = new EventData(new HashMap<String, Variant>() {
			{
				put("k", Variant.fromString("hi"));
			}
		});
		assertEquals(Variant.fromString("hi"), eventData.getVariant("k"));
	}

	@Test
	public void testGetString() throws Exception {
		final EventData eventData = new EventData(new HashMap<String, Variant>() {
			{
				put("k", Variant.fromString("hi"));
			}
		});
		assertSame("hi", eventData.getString2("k"));
	}

	@Test
	public void testGetBoolean() throws Exception {
		final EventData eventData = new EventData(new HashMap<String, Variant>() {
			{
				put("k", Variant.fromBoolean(true));
			}
		});
		assertEquals(true, eventData.getBoolean("k"));
	}

	@Test
	public void testGetInteger() throws Exception {
		final EventData eventData = new EventData(new HashMap<String, Variant>() {
			{
				put("k", Variant.fromInteger(42));
			}
		});
		assertEquals(42, eventData.getInteger("k"));
	}

	@Test
	public void testGetLong() throws Exception {
		final EventData eventData = new EventData(new HashMap<String, Variant>() {
			{
				put("k", Variant.fromLong(42L));
			}
		});
		assertEquals(42L, eventData.getLong("k"));
	}

	@Test
	public void testGetDouble() throws Exception {
		final EventData eventData = new EventData(new HashMap<String, Variant>() {
			{
				put("k", Variant.fromDouble(4.2));
			}
		});
		assertEquals(4.2, eventData.getDouble("k"), 0.00001);
	}

	@Test
	public void testGetVariantMap() throws Exception {
		final Map<String, Variant> map = new HashMap<String, Variant>() {
			{
				put("one", Variant.fromInteger(1));
			}
		};
		final EventData eventData = new EventData(new HashMap<String, Variant>() {
			{
				put("k", Variant.fromVariantMap(map));
			}
		});
		assertEquals(map, eventData.getVariantMap("k"));
	}

	@Test
	public void testGetVariantList() throws Exception {
		final List<Variant> list = Arrays.asList(Variant.fromInteger(1));
		final EventData eventData = new EventData(new HashMap<String, Variant>() {
			{
				put("k", Variant.fromVariantList(list));
			}
		});
		assertEquals(list, eventData.getVariantList("k"));
	}

	@Test
	public void testGetStringList() throws Exception {
		final List<String> list = Arrays.asList("hi");
		final EventData eventData = new EventData(new HashMap<String, Variant>() {
			{
				put("k", Variant.fromStringList(list));
			}
		});
		assertEquals(list, eventData.getStringList("k"));
	}

	@Test
	public void testGetStringMap() throws Exception {
		final Map<String, String> map = new HashMap<String, String>() {
			{
				put("one", "1");
			}
		};
		final EventData eventData = new EventData(new HashMap<String, Variant>() {
			{
				put("k", Variant.fromStringMap(map));
			}
		});
		assertEquals(map, eventData.getStringMap("k"));
	}

	@Test
	public void testGetTypedObject() throws Exception {
		final Circle circle = new Circle(42);
		final EventData eventData = new EventData(new HashMap<String, Variant>() {
			{
				put("k", Variant.fromTypedObject(circle, new CircleSerializer()));
			}
		});
		assertEquals(circle, eventData.getTypedObject("k", new CircleSerializer()));
	}

	@Test
	public void testGetTypedList() throws Exception {
		final List<Circle> list = Arrays.asList(new Circle(42));
		final EventData eventData = new EventData(new HashMap<String, Variant>() {
			{
				put("k", Variant.fromTypedList(list, new CircleSerializer()));
			}
		});
		assertEquals(list, eventData.getTypedList("k", new CircleSerializer()));
	}

	@Test
	public void testGetTypedMap() throws Exception {
		final Map<String, Circle> map = new HashMap<String, Circle>() {
			{
				put("one", new Circle(42));
			}
		};
		final EventData eventData = new EventData(new HashMap<String, Variant>() {
			{
				put("k", Variant.fromTypedMap(map, new CircleSerializer()));
			}
		});
		assertEquals(map, eventData.getTypedMap("k", new CircleSerializer()));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetVariantWithNullKey() throws Exception {
		new EventData().getVariant(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetStringWithNullKey() throws Exception {
		new EventData().getString2(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetBooleanWithNullKey() throws Exception {
		new EventData().getBoolean(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetIntegerWithNullKey() throws Exception {
		new EventData().getInteger(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetLongWithNullKey() throws Exception {
		new EventData().getLong(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetDoubleWithNullKey() throws Exception {
		new EventData().getDouble(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetVariantMapWithNullKey() throws Exception {
		new EventData().getVariantMap(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetVariantListWithNullKey() throws Exception {
		new EventData().getVariantList(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetStringListWithNullKey() throws Exception {
		new EventData().getStringList(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetStringMapWithNullKey() throws Exception {
		new EventData().getStringMap(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetTypedObjectWithNullKey() throws Exception {
		new EventData().getTypedObject(null, new CircleSerializer());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetTypedListWithNullKey() throws Exception {
		new EventData().getTypedList(null, new CircleSerializer());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetTypedMapWithNullKey() throws Exception {
		new EventData().getTypedMap(null, new CircleSerializer());
	}

	@Test(expected = VariantKeyNotFoundException.class)
	public void testGetVariantWithMissingKey() throws Exception {
		new EventData().getVariant("k");
	}

	@Test(expected = VariantKeyNotFoundException.class)
	public void testGetStringWithMissingKey() throws Exception {
		new EventData().getString2("k");
	}

	@Test(expected = VariantKeyNotFoundException.class)
	public void testGetBooleanWithMissingKey() throws Exception {
		new EventData().getBoolean("k");
	}

	@Test(expected = VariantKeyNotFoundException.class)
	public void testGetIntegerWithMissingKey() throws Exception {
		new EventData().getInteger("k");
	}

	@Test(expected = VariantKeyNotFoundException.class)
	public void testGetLongWithMissingKey() throws Exception {
		new EventData().getLong("k");
	}

	@Test(expected = VariantKeyNotFoundException.class)
	public void testGetDoubleWithMissingKey() throws Exception {
		new EventData().getDouble("k");
	}

	@Test(expected = VariantKeyNotFoundException.class)
	public void testGetVariantMapWithMissingKey() throws Exception {
		new EventData().getVariantMap("k");
	}

	@Test(expected = VariantKeyNotFoundException.class)
	public void testGetVariantListWithMissingKey() throws Exception {
		new EventData().getVariantList("k");
	}

	@Test(expected = VariantKeyNotFoundException.class)
	public void testGetStringListWithMissingKey() throws Exception {
		new EventData().getStringList("k");
	}

	@Test(expected = VariantKeyNotFoundException.class)
	public void testGetStringMapWithMissingKey() throws Exception {
		new EventData().getStringMap("k");
	}

	@Test(expected = VariantKeyNotFoundException.class)
	public void testGetTypedObjectWithMissingKey() throws Exception {
		new EventData().getTypedObject("k", new CircleSerializer());
	}

	@Test(expected = VariantKeyNotFoundException.class)
	public void testGetTypedListWithMissingKey() throws Exception {
		new EventData().getTypedList("k", new CircleSerializer());
	}

	@Test(expected = VariantKeyNotFoundException.class)
	public void testGetTypedMapWithMissingKey() throws Exception {
		new EventData().getTypedMap("k", new CircleSerializer());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetTypedObjectWithNullSerializer() throws Exception {
		final Circle circle = new Circle(42);
		final EventData eventData = new EventData(new HashMap<String, Variant>() {
			{
				put("k", Variant.fromTypedObject(circle, new CircleSerializer()));
			}
		});
		eventData.getTypedObject("k", null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetTypedListWithNullSerializer() throws Exception {
		final List<Circle> list = Arrays.asList(new Circle(42));
		final EventData eventData = new EventData(new HashMap<String, Variant>() {
			{
				put("k", Variant.fromTypedList(list, new CircleSerializer()));
			}
		});
		eventData.getTypedList("k", null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetTypedMapWithNullSerializer() throws Exception {
		final Map<String, Circle> map = new HashMap<String, Circle>() {
			{
				put("one", new Circle(42));
			}
		};
		final EventData eventData = new EventData(new HashMap<String, Variant>() {
			{
				put("k", Variant.fromTypedMap(map, new CircleSerializer()));
			}
		});
		eventData.getTypedMap("k", null);
	}

	@Test
	public void testOptVariant() {
		final Variant defaultValue = Variant.fromInteger(42);
		final EventData eventData = new EventData(new HashMap<String, Variant>() {
			{
				put("k", Variant.fromString("hi"));
			}
		});
		assertEquals(Variant.fromString("hi"), eventData.optVariant("k", defaultValue));
	}

	@Test
	public void testOptString() {
		final String defaultValue = "42";
		final EventData eventData = new EventData(new HashMap<String, Variant>() {
			{
				put("k", Variant.fromString("hi"));
			}
		});
		assertSame("hi", eventData.optString("k", defaultValue));
	}

	@Test
	public void testOptBoolean() {
		final boolean defaultValue = false;
		final EventData eventData = new EventData(new HashMap<String, Variant>() {
			{
				put("k", Variant.fromBoolean(true));
			}
		});
		assertEquals(true, eventData.optBoolean("k", defaultValue));
	}

	@Test
	public void testOptInteger() {
		final int defaultValue = 23;
		final EventData eventData = new EventData(new HashMap<String, Variant>() {
			{
				put("k", Variant.fromInteger(42));
			}
		});
		assertEquals(42, eventData.optInteger("k", defaultValue));
	}

	@Test
	public void testOptLong() {
		final long defaultValue = 23L;
		final EventData eventData = new EventData(new HashMap<String, Variant>() {
			{
				put("k", Variant.fromLong(42L));
			}
		});
		assertEquals(42L, eventData.optLong("k", defaultValue));
	}

	@Test
	public void testOptDouble() {
		final double defaultValue = 2.3;
		final EventData eventData = new EventData(new HashMap<String, Variant>() {
			{
				put("k", Variant.fromDouble(4.2));
			}
		});
		assertEquals(4.2, eventData.optDouble("k", defaultValue), 0.00001);
	}

	@Test
	public void testOptVariantMap() {
		final Map<String, Variant> defaultValue = new HashMap<String, Variant>();
		final Map<String, Variant> map = new HashMap<String, Variant>() {
			{
				put("one", Variant.fromInteger(1));
			}
		};
		final EventData eventData = new EventData(new HashMap<String, Variant>() {
			{
				put("k", Variant.fromVariantMap(map));
			}
		});
		assertEquals(map, eventData.optVariantMap("k", defaultValue));
	}

	@Test
	public void testOptVariantList() {
		final List<Variant> defaultValue = new ArrayList<Variant>();
		final List<Variant> list = Arrays.asList(Variant.fromInteger(1));
		final EventData eventData = new EventData(new HashMap<String, Variant>() {
			{
				put("k", Variant.fromVariantList(list));
			}
		});
		assertEquals(list, eventData.optVariantList("k", defaultValue));
	}

	@Test
	public void testOptStringList() {
		final List<String> defaultValue = new ArrayList<String>();
		final List<String> list = Arrays.asList("hi");
		final EventData eventData = new EventData(new HashMap<String, Variant>() {
			{
				put("k", Variant.fromStringList(list));
			}
		});
		assertEquals(list, eventData.optStringList("k", defaultValue));
	}

	@Test
	public void testOptStringMap() {
		final Map<String, String> defaultValue = new HashMap<String, String>();
		final Map<String, String> map = new HashMap<String, String>() {
			{
				put("one", "1");
			}
		};
		final EventData eventData = new EventData(new HashMap<String, Variant>() {
			{
				put("k", Variant.fromStringMap(map));
			}
		});
		assertEquals(map, eventData.optStringMap("k", defaultValue));
	}

	@Test
	public void testOptTypedObject() throws Exception {
		final Circle defaultValue = new Circle(23);
		final Circle circle = new Circle(42);
		final EventData eventData = new EventData(new HashMap<String, Variant>() {
			{
				put("k", Variant.fromTypedObject(circle, new CircleSerializer()));
			}
		});
		assertEquals(circle, eventData.optTypedObject("k", defaultValue, new CircleSerializer()));
	}

	@Test
	public void testOptTypedList() {
		final List<Circle> defaultValue = new ArrayList<Circle>();
		final List<Circle> list = Arrays.asList(new Circle(42));
		final EventData eventData = new EventData(new HashMap<String, Variant>() {
			{
				put("k", Variant.fromTypedList(list, new CircleSerializer()));
			}
		});
		assertEquals(list, eventData.optTypedList("k", defaultValue, new CircleSerializer()));
	}

	@Test
	public void testOptTypedMap() {
		final Map<String, Circle> defaultValue = new HashMap<String, Circle>();
		final Map<String, Circle> map = new HashMap<String, Circle>() {
			{
				put("one", new Circle(42));
			}
		};
		final EventData eventData = new EventData(new HashMap<String, Variant>() {
			{
				put("k", Variant.fromTypedMap(map, new CircleSerializer()));
			}
		});
		assertEquals(map, eventData.optTypedMap("k", defaultValue, new CircleSerializer()));
	}

	@Test
	public void testOptVariantWithNullKey() throws Exception {
		final Variant defaultValue = Variant.fromInteger(42);
		exception.expect(IllegalArgumentException.class);
		new EventData().optVariant(null, defaultValue);
	}

	@Test
	public void testOptStringWithNullKey() throws Exception {
		final String defaultValue = "42";
		exception.expect(IllegalArgumentException.class);
		new EventData().optString(null, defaultValue);
	}

	@Test
	public void testOptBooleanWithNullKey() throws Exception {
		final boolean defaultValue = true;
		exception.expect(IllegalArgumentException.class);
		new EventData().optBoolean(null, defaultValue);
	}

	@Test
	public void testOptIntegerWithNullKey() throws Exception {
		final int defaultValue = 42;
		exception.expect(IllegalArgumentException.class);
		new EventData().optInteger(null, defaultValue);
	}

	@Test
	public void testOptLongWithNullKey() throws Exception {
		final long defaultValue = 42L;
		exception.expect(IllegalArgumentException.class);
		new EventData().optLong(null, defaultValue);
	}

	@Test
	public void testOptDoubleWithNullKey() throws Exception {
		final double defaultValue = 4.2;
		exception.expect(IllegalArgumentException.class);
		new EventData().optDouble(null, defaultValue);
	}

	@Test
	public void testOptVariantMapWithNullKey() throws Exception {
		final Map<String, Variant> defaultValue = new HashMap<String, Variant>();
		exception.expect(IllegalArgumentException.class);
		new EventData().optVariantMap(null, defaultValue);
	}

	@Test
	public void testOptVariantListWithNullKey() throws Exception {
		final List<Variant> defaultValue = new ArrayList<Variant>();
		exception.expect(IllegalArgumentException.class);
		new EventData().optVariantList(null, defaultValue);
	}

	@Test
	public void testOptStringListWithNullKey() throws Exception {
		final List<String> defaultValue = new ArrayList<String>();
		exception.expect(IllegalArgumentException.class);
		new EventData().optStringList(null, defaultValue);
	}

	@Test
	public void testOptStringMapWithNullKey() throws Exception {
		final Map<String, String> defaultValue = new HashMap<String, String>();
		exception.expect(IllegalArgumentException.class);
		new EventData().optStringMap(null, defaultValue);
	}

	@Test
	public void testOptTypedObjectWithNullKey() throws Exception {
		final Circle defaultValue = new Circle(23);
		exception.expect(IllegalArgumentException.class);
		new EventData().optTypedObject(null, defaultValue, new CircleSerializer());
	}

	@Test
	public void testOptTypedListWithNullKey() throws Exception {
		final List<Circle> defaultValue = new ArrayList<Circle>();
		exception.expect(IllegalArgumentException.class);
		new EventData().optTypedList(null, defaultValue, new CircleSerializer());
	}

	@Test
	public void testOptTypedMapWithNullKey() throws Exception {
		final Map<String, Circle> defaultValue = new HashMap<String, Circle>();
		exception.expect(IllegalArgumentException.class);
		new EventData().optTypedMap(null, defaultValue, new CircleSerializer());
	}

	@Test
	public void testOptVariantWithMissingKey() throws Exception {
		final Variant defaultValue = Variant.fromInteger(42);
		assertEquals(defaultValue, new EventData().optVariant("k", defaultValue));
	}

	@Test
	public void testOptStringWithMissingKey() {
		final String defaultValue = "42";
		assertEquals(defaultValue, new EventData().optString("k", defaultValue));
	}

	@Test
	public void testOptBooleanWithMissingKey() {
		final boolean defaultValue = true;
		assertEquals(defaultValue, new EventData().optBoolean("k", defaultValue));
	}

	@Test
	public void testOptIntegerWithMissingKey() {
		final int defaultValue = 42;
		assertEquals(defaultValue, new EventData().optInteger("k", defaultValue));
	}

	@Test
	public void testOptLongWithMissingKey() {
		final long defaultValue = 42L;
		assertEquals(defaultValue, new EventData().optLong("k", defaultValue));
	}

	@Test
	public void testOptDoubleWithMissingKey() {
		final double defaultValue = 4.2;
		assertEquals(defaultValue, new EventData().optDouble("k", defaultValue), 0.0001);
	}

	@Test
	public void testOptVariantMapWithMissingKey() {
		final Map<String, Variant> defaultValue = new HashMap<String, Variant>();
		assertEquals(defaultValue, new EventData().optVariantMap("k", defaultValue));
	}

	@Test
	public void testOptVariantListWithMissingKey() {
		final List<Variant> defaultValue = new ArrayList<Variant>();
		assertEquals(defaultValue, new EventData().optVariantList("k", defaultValue));
	}

	@Test
	public void testOptStringListWithMissingKey() {
		final List<String> defaultValue = new ArrayList<String>();
		assertEquals(defaultValue, new EventData().optStringList("k", defaultValue));
	}

	@Test
	public void testOptStringMapWithMissingKey() {
		final Map<String, String> defaultValue = new HashMap<String, String>();
		assertEquals(defaultValue, new EventData().optStringMap("k", defaultValue));
	}

	@Test
	public void testOptTypedObjectWithMissingKey() {
		final Circle defaultValue = new Circle(23);
		assertEquals(defaultValue, new EventData().optTypedObject("k", defaultValue, new CircleSerializer()));
	}

	@Test
	public void testOptTypedListWithMissingKey() {
		final List<Circle> defaultValue = new ArrayList<Circle>();
		assertEquals(defaultValue, new EventData().optTypedList("k", defaultValue, new CircleSerializer()));
	}

	@Test
	public void testOptTypedMapWithMissingKey() {
		final Map<String, Circle> defaultValue = new HashMap<String, Circle>();
		assertEquals(defaultValue, new EventData().optTypedMap("k", defaultValue, new CircleSerializer()));
	}

	@Test
	public void testOptTypedObjectWithNullSerializer() throws Exception {
		final Circle defaultValue = new Circle(23);
		exception.expect(IllegalArgumentException.class);
		final Circle circle = new Circle(42);
		final EventData eventData = new EventData(new HashMap<String, Variant>() {
			{
				put("k", Variant.fromTypedObject(circle, new CircleSerializer()));
			}
		});
		eventData.optTypedObject("k", defaultValue, null);
	}

	@Test
	public void testOptTypedListWithNullSerializer() throws Exception {
		final List<Circle> defaultValue = new ArrayList<Circle>();
		exception.expect(IllegalArgumentException.class);
		final List<Circle> list = Arrays.asList(new Circle(42));
		final EventData eventData = new EventData(new HashMap<String, Variant>() {
			{
				put("k", Variant.fromTypedList(list, new CircleSerializer()));
			}
		});
		eventData.optTypedList("k", defaultValue, null);
	}

	@Test
	public void testOptTypedMapWithNullSerializer() throws Exception {
		final Map<String, Circle> defaultValue = new HashMap<String, Circle>();
		exception.expect(IllegalArgumentException.class);
		final Map<String, Circle> map = new HashMap<String, Circle>() {
			{
				put("one", new Circle(42));
			}
		};
		final EventData eventData = new EventData(new HashMap<String, Variant>() {
			{
				put("k", Variant.fromTypedMap(map, new CircleSerializer()));
			}
		});
		eventData.optTypedMap("k", defaultValue, null);
	}

	@Test
	public void testOptVariantWithNullDefaultValue() throws Exception {
		assertNull(new EventData().optVariant("k", null));
	}

	@Test
	public void testOptStringWithNullDefaultValue() {
		assertNull(new EventData().optString("k", null));
	}

	@Test
	public void testOptVariantMapWithNullDefaultValue() {
		assertNull(new EventData().optVariantMap("k", null));
	}

	@Test
	public void testOptVariantListWithNullDefaultValue() {
		assertNull(new EventData().optVariantList("k", null));
	}

	@Test
	public void testOptStringListWithNullDefaultValue() {
		assertNull(new EventData().optStringList("k", null));
	}

	@Test
	public void testOptStringMapWithNullDefaultValue() {
		assertNull(new EventData().optStringMap("k", null));
	}

	@Test
	public void testOptTypedObjectWithNullDefaultValue() {
		assertNull(new EventData().optTypedObject("k", null, new CircleSerializer()));
	}

	@Test
	public void testOptTypedListWithNullDefaultValue() {
		assertNull(new EventData().optTypedList("k", null, new CircleSerializer()));
	}

	@Test
	public void testOptTypedMapWithNullDefaultValue() {
		assertNull(new EventData().optTypedMap("k", null, new CircleSerializer()));
	}

	// FNV1a 32-bit hash tests
	// basic smoke tests for comparison with iOS
	@Test
	public void testGetFnv1aHash_String_Smoke() {
		// setup
		final Map<String, Variant> map = new HashMap<String, Variant>() {
			{
				put("key", Variant.fromString("value"));
			}
		};
		final EventData eventData = new EventData(map);
		// test
		final long hash = eventData.toFnv1aHash(null);
		// verify flattened map string "key:value"
		final long expectedHash = 4007910315l;
		assertEquals(expectedHash, hash);
	}

	@Test
	public void testGetFnv1aHash_Integer_Smoke() {
		// setup
		final Map<String, Variant> map = new HashMap<String, Variant>() {
			{
				put("key", Variant.fromInteger(552));
			}
		};
		final EventData eventData = new EventData(map);
		// test
		final long hash = eventData.toFnv1aHash(null);
		// verify flattened map string "key:552"
		final long expectedHash = 874166902;
		assertEquals(expectedHash, hash);
	}

	@Test
	public void testGetFnv1aHash_Boolean_Smoke() {
		// setup
		final Map<String, Variant> map = new HashMap<String, Variant>() {
			{
				put("key", Variant.fromBoolean(false));
			}
		};
		final EventData eventData = new EventData(map);
		// test
		final long hash = eventData.toFnv1aHash(null);
		// verify flattened map string "key:false"
		final long expectedHash = 138493769;
		assertEquals(expectedHash, hash);
	}

	@Test
	public void testGetFnv1aHash_AsciiSorted_Smoke() {
		// setup
		final Map<String, Variant> map = new HashMap<String, Variant>() {
			{
				put("key", Variant.fromString("value"));
				put("number", Variant.fromInteger(1234));
				put("UpperCase", Variant.fromString("abc"));
				put("_underscore", Variant.fromString("score"));
			}
		};
		final EventData eventData = new EventData(map);
		// test
		final long hash = eventData.toFnv1aHash(null);
		// verify flattened map string "UpperCase:abc_underscore:scorekey:valuenumber:1234"
		final long expectedHash = 960895195;
		assertEquals(expectedHash, hash);
	}

	@Test
	public void testGetFnv1aHash_NoMask_Happy() {
		// setup
		final Map<String, Variant> map = new HashMap<String, Variant>() {
			{
				put("aaa", Variant.fromString("1"));
				put("zzz", Variant.fromBoolean(true));
			}
		};
		final EventData eventData = new EventData(map);
		// test
		final long hash = eventData.toFnv1aHash(null);
		// verify flattened map string "aaa:1zzz:true"
		final long expectedHash = 3251025831l;
		assertEquals(expectedHash, hash);
	}

	@Test
	public void testGetFnv1aHash_WithMask_Happy() {
		// setup
		final Map<String, Variant> map = new HashMap<String, Variant>() {
			{
				put("aaa", Variant.fromString("1"));
				put("c", Variant.fromInteger(2));
				put("m", Variant.fromDouble(1.11));
				put("zzz", Variant.fromBoolean(true));
			}
		};
		final EventData eventData = new EventData(map);
		final String[] mask = new String[] {"c", "m"};
		// test
		final long hash = eventData.toFnv1aHash(mask);
		// verify flattened map string "c:2m:1.11"
		final long expectedHash = 2718815288l;
		assertEquals(expectedHash, hash);
	}

	@Test
	public void testGetFnv1aHash_ArrayOfMaps() {
		// setup
		final Map<String, Variant> map = new HashMap<String, Variant>() {
			{
				put("aaa", Variant.fromString("1"));
				put("zzz", Variant.fromBoolean(true));
			}
		};
		final Map<String, Variant> map2 = new HashMap<String, Variant>() {
			{
				put("number", Variant.fromInteger(123));
				put("double", Variant.fromDouble(1.5));
			}
		};
		final List<Variant> list = new ArrayList<>();
		list.add(Variant.fromVariantMap(map));
		list.add(Variant.fromVariantMap(map2));
		final EventData eventData = new EventData();
		eventData.putVariantList("key", list);
		// test
		final long hash = eventData.toFnv1aHash(null);
		// verify flattened map string "key:[{"aaa":"1","zzz":true},{"number":123,"double":1.5}]"
		final long expectedHash = 3841285024l;
		assertEquals(expectedHash, hash);
	}

	@Test
	public void testGetFnv1aHash_ArrayOfLists() {
		// setup
		final ArrayList<Variant> innerList = new ArrayList<Variant>() {
			{
				add(Variant.fromString("aaa"));
				add(Variant.fromString("zzz"));
				add(Variant.fromInteger(111));
			}
		};
		final ArrayList<Variant> innerList2 = new ArrayList<Variant>() {
			{
				add(Variant.fromString("2"));
			}
		};
		final List<Variant> list = new ArrayList<>();
		list.add(Variant.fromVariantList(innerList));
		list.add(Variant.fromVariantList(innerList2));
		final EventData eventData = new EventData();
		eventData.putVariantList("key", list);
		// test
		final long hash = eventData.toFnv1aHash(null);
		// verify flattened map string "key:[["aaa","zzz",111],["2"]]"
		final long expectedHash = 1785496830l;
		assertEquals(expectedHash, hash);
	}

	@Test
	public void testGetFnv1aHash_WithNestedMap() {
		// setup
		final Map<String, String> innerMap = new HashMap<String, String>() {
			{
				put("bbb", "5");
				put("hhh", "false");
			}
		};
		final Map<String, Variant> map = new HashMap<String, Variant>() {
			{
				put("aaa", Variant.fromString("1"));
				put("zzz", Variant.fromBoolean(true));
				put("inner", Variant.fromStringMap(innerMap));
			}
		};
		final EventData eventData = new EventData(map);
		// test
		final long hash = eventData.toFnv1aHash(null);
		// verify flattened map string "aaa:1inner.bbb:5inner.hhh:falsezzz:true"
		final long expectedHash = 4230384023l;
		assertEquals(expectedHash, hash);
	}

	@Test
	public void testGetFnv1aHash_WithNestedMapContainingNestedMap() {
		// setup
		final Map<String, String> secondInnerMap = new HashMap<String, String>() {
			{
				put("ccc", "10");
				put("iii", "1.1");
			}
		};
		final Map<String, Variant> innerMap = new HashMap<String, Variant>() {
			{
				put("bbb", Variant.fromInteger(5));
				put("hhh", Variant.fromBoolean(false));
				put("secondInner", Variant.fromStringMap(secondInnerMap));
			}
		};
		final Map<String, Variant> map = new HashMap<String, Variant>() {
			{
				put("aaa", Variant.fromString("1"));
				put("zzz", Variant.fromBoolean(true));
				put("inner", Variant.fromVariantMap(innerMap));
			}
		};
		final EventData eventData = new EventData(map);
		// test
		final long hash = eventData.toFnv1aHash(null);
		// verify flattened map string "aaa:1inner.bbb:5inner.hhh:falseinner.secondInner.ccc:10inner.secondInner.iii:1.1zzz:true"
		final long expectedHash = 1786696518;
		assertEquals(expectedHash, hash);
	}

	@Test
	public void testGetFnv1aHash_WithEmptyMask() {
		// setup
		final Map<String, Variant> map = new HashMap<String, Variant>() {
			{
				put("a", Variant.fromString("1"));
				put("b", Variant.fromString("2"));
			}
		};
		final EventData eventData = new EventData(map);
		final String[] mask = new String[] {};
		// test
		final long hash = eventData.toFnv1aHash(mask);
		// verify flattened map string "a:1b:2"
		final long expectedHash = 3371500665l;
		assertEquals(expectedHash, hash);
	}

	@Test
	public void testGetFnv1aHash_WithMaskMatchingNoKeys() {
		// setup
		final Map<String, Variant> map = new HashMap<String, Variant>() {
			{
				put("a", Variant.fromString("1"));
				put("b", Variant.fromString("2"));
			}
		};
		final EventData eventData = new EventData(map);
		final String[] mask = new String[] {"c", "d"};
		// test
		final long hash = eventData.toFnv1aHash(mask);
		// verify 0 / no hash generated due to mask keys not being present in the map
		final long expectedHash = 0;
		assertEquals(expectedHash, hash);
	}

	@Test
	public void testGetFnv1aHash_NoMask_VerifyEventDataMapSortedWithCaseSensitivity() {
		// setup
		final Map<String, Variant> map = new HashMap<String, Variant>() {
			{
				put("a", Variant.fromString("1"));
				put("A", Variant.fromString("2"));
				put("ba", Variant.fromString("3"));
				put("Ba", Variant.fromString("4"));
				put("Z", Variant.fromString("5"));
				put("z", Variant.fromString("6"));
				put("r", Variant.fromString("7"));
				put("R", Variant.fromString("8"));
				put("bc", Variant.fromString("9"));
				put("Bc", Variant.fromString("10"));
				put("1", Variant.fromInteger(1));
				put("222", Variant.fromInteger(222));
			}
		};
		final EventData eventData = new EventData(map);
		// test
		final long hash = eventData.toFnv1aHash(null);
		// verify flattened map string "1:1222:222A:2Ba:4Bc:10R:8Z:5a:1ba:3bc:9r:7z:6"
		final long expectedHash = 2933724447l;
		assertEquals(expectedHash, hash);
	}

	@Test
	public void testGetFnv1aHash_WithMask_VerifyEventDataMapSortedWithCaseSensitivity() {
		// setup
		final Map<String, Variant> map = new HashMap<String, Variant>() {
			{
				put("a", Variant.fromString("1"));
				put("A", Variant.fromString("2"));
				put("ba", Variant.fromString("3"));
				put("Ba", Variant.fromString("4"));
				put("Z", Variant.fromString("5"));
				put("z", Variant.fromString("6"));
				put("r", Variant.fromString("7"));
				put("R", Variant.fromString("8"));
				put("bc", Variant.fromString("9"));
				put("Bc", Variant.fromString("10"));
				put("1", Variant.fromInteger(1));
				put("222", Variant.fromInteger(222));
			}
		};
		final EventData eventData = new EventData(map);
		final String[] mask = new String[] {"A", "a", "ba", "Ba", "bc", "Bc", "1"};
		// test
		final long hash = eventData.toFnv1aHash(mask);
		// verify flattened map string "1:1A:2Ba:4Bc:10a:1ba:3bc:9"
		final long expectedHash = 3344627991l;
		assertEquals(expectedHash, hash);
	}
}
