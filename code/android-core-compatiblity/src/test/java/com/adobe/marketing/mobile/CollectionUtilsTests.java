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

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class CollectionUtilsTests extends BaseTest {

	/*
	to_map:
	 {
	    "key1": "value1",
	    "key2": "value2",
	    "anInt": 552,
	    "aMap": {
	        "embeddedString": "embeddedStringValue"
	    },
	    "aList": [
	        "stringInList"
	    ],
	    "listOfObjects": [
	    	{
	            "name": "request1",
	            "details": {
	                "size": "large",
	                "color": "red"
	            }
	        },
	        {
	            "name": "request2",
	            "location": "central"
	        }
	    ]
	 }
	 */
	private Map<String, Variant> getToMap() {
		final Map<String, Variant> toMap = new HashMap<String, Variant>();
		toMap.put("key1", Variant.fromString("value1"));
		toMap.put("key2", Variant.fromString("value2"));
		toMap.put("anInt", Variant.fromInteger(552));
		final Map<String, Variant> aMap = new HashMap<String, Variant>();
		aMap.put("embeddedString", Variant.fromString("embeddedStringValue"));
		toMap.put("aMap", Variant.fromVariantMap(aMap));
		final List<Variant> aList = new ArrayList<Variant>();
		aList.add(Variant.fromString("stringInList"));
		toMap.put("aList", Variant.fromVariantList(aList));
		final List<Variant> listOfObjects = new ArrayList<Variant>();
		final Map<String, Variant> obj1 = new HashMap<String, Variant>();
		obj1.put("name", Variant.fromString("request1"));
		final Map<String, Variant> obj1Details = new HashMap<String, Variant>();
		obj1Details.put("size", Variant.fromString("large"));
		obj1Details.put("color", Variant.fromString("red"));
		obj1.put("details", Variant.fromVariantMap(obj1Details));
		final Map<String, Variant> obj2 = new HashMap<String, Variant>();
		obj2.put("name", Variant.fromString("request2"));
		obj2.put("location", Variant.fromString("central"));
		listOfObjects.add(Variant.fromVariantMap(obj1));
		listOfObjects.add(Variant.fromVariantMap(obj2));
		toMap.put("listOfObjects", Variant.fromVariantList(listOfObjects));

		return toMap;
	}

	/*
	{
	    "attachedKey": "attachedValue",
	    "key1": "updatedValue1",
	    "newInt": 123,
	    "newLong": 123456,
	    "newDouble": 32.23,
	    "newBool": false,
	    "newNull": null,
	    "aMap": {
	        "embeddedString": "changedEmbeddedStringValue",
	        "newEmbeddedString": "newEmbeddedStringValue"
	    },
	    "newMap": {
	        "newMapKey": "newMapValue"
	    },
	    "aList": [
	        "stringInList",   // <<< this is a duplicate entry, we need to make sure we don't get 2
	        "newStringInList"
	    ],
	    "newList": [
	        "newListString"
	    ],
	    "listOfObjects[*]": {
	        "details": {
	            "color": "orange",
	            "temp": 58.8
	         }
	    }
	 }
	 */
	private Map<String, Variant> getFromMap() {
		final Map<String, Variant> fromMap = new HashMap<String, Variant>();

		fromMap.put("attachedKey", Variant.fromString("attachedValue"));
		fromMap.put("key1", Variant.fromString("updatedValue1"));
		fromMap.put("newInt", Variant.fromInteger(123));
		fromMap.put("newLong", Variant.fromLong(123456));
		fromMap.put("newDouble", Variant.fromDouble(32.23));
		fromMap.put("newBool", Variant.fromBoolean(false));
		fromMap.put("newNull", Variant.fromNull());
		final Map<String, Variant> aMap = new HashMap<String, Variant>();
		aMap.put("embeddedString", Variant.fromString("changedEmbeddedStringValue"));
		aMap.put("newEmbeddedString", Variant.fromString("newEmbeddedStringValue"));
		fromMap.put("aMap", Variant.fromVariantMap(aMap));
		final Map<String, Variant> newMap = new HashMap<String, Variant>();
		newMap.put("newMapKey", Variant.fromString("newMapValue"));
		fromMap.put("newMap", Variant.fromVariantMap(newMap));
		final List<Variant> aList = new ArrayList<Variant>();
		aList.add(Variant.fromString("stringInList"));
		aList.add(Variant.fromString("newStringInList"));
		fromMap.put("aList", Variant.fromVariantList(aList));
		final List<Variant> newList = new ArrayList<Variant>();
		newList.add(Variant.fromString("newListString"));
		fromMap.put("newList", Variant.fromVariantList(newList));
		final Map<String, Variant> listOfObjectsAsMap = new HashMap<String, Variant>();
		final Map<String, Variant> details = new HashMap<String, Variant>();
		details.put("color", Variant.fromString("orange"));
		details.put("temp", Variant.fromDouble(58.8));
		listOfObjectsAsMap.put("details", Variant.fromVariantMap(details));
		fromMap.put("listOfObjects[*]", Variant.fromVariantMap(listOfObjectsAsMap));

		return fromMap;
	}

	private List<Variant> getToList() {
		final List<Variant> toList = new ArrayList<Variant>();

		toList.add(Variant.fromString("listString"));
		toList.add(Variant.fromInteger(552));

		final Map<String, Variant> aMap = new HashMap<String, Variant>();
		aMap.put("embeddedString", Variant.fromString("embeddedStringValue"));
		toList.add(Variant.fromVariantMap(aMap));

		final List<Variant> aList = new ArrayList<Variant>();
		aList.add(Variant.fromString("stringInList"));
		toList.add(Variant.fromVariantList(aList));

		return toList;
	}

	private List<Variant> getFromList() {
		final List<Variant> fromList = new ArrayList<Variant>();

		fromList.add(Variant.fromString("listString"));
		fromList.add(Variant.fromString("listString2"));
		fromList.add(Variant.fromInteger(552));
		fromList.add(Variant.fromInteger(553));
		fromList.add(Variant.fromLong(123456));
		fromList.add(Variant.fromDouble(32.23));
		fromList.add(Variant.fromBoolean(false));
		fromList.add(Variant.fromNull());

		final Map<String, Variant> aMap = new HashMap<String, Variant>();
		aMap.put("anotherEmbeddedString", Variant.fromString("anotherEmbeddedStringValue"));
		fromList.add(Variant.fromVariantMap(aMap));

		final List<Variant> aList = new ArrayList<Variant>();
		aList.add(Variant.fromString("anotherStringInList"));
		fromList.add(Variant.fromVariantList(aList));

		return fromList;
	}

	// =================================================================================================================
	// static Map<String, Variant> addDataToMap(final Map<String, Variant> toMap, final Map<String, Variant> fromMap, final boolean deleteIfEmpty, final boolean isToMapConsequenceData)
	// =================================================================================================================
	@Test
	public void addDataToMap_When_Happy_Then_AddsDataToMapWithDelete() throws Exception {
		Map<String, Variant> fromMap = getFromMap();

		fromMap.put("key2", Variant.fromNull());
		fromMap.put("aMap", Variant.fromNull());
		fromMap.put("aList", Variant.fromNull());
		fromMap.put("listOfObjects[*]", Variant.fromNull());

		// test
		final Map<String, Variant> result = CollectionUtils.addDataToMap(fromMap, getToMap(), true, true);
		assertEquals(9, result.size());
		assertEquals("updatedValue1", result.get("key1").getString());
		assertFalse(result.containsKey("key2"));
		assertEquals(552, result.get("anInt").getInteger());
		assertEquals(123, result.get("newInt").getInteger());
		assertEquals(123456, result.get("newLong").getLong());
		assertEquals(32.23, result.get("newDouble").getDouble(), .01);
		assertFalse(result.get("newBool").getBoolean());
		assertFalse(result.containsKey("newNull"));

		assertFalse(result.containsKey("aMap"));

		final Map<String, Variant> newMap = result.get("newMap").getVariantMap();
		assertEquals("newMapValue", newMap.get("newMapKey").getString());

		assertFalse(result.containsKey("aList"));

		final List<Variant> newList = result.get("newList").getVariantList();
		assertEquals(1, newList.size());
		assertEquals("newListString", newList.get(0).getString());
	}

	@Test
	public void addDataToMap_When_Happy_Then_AddDataToMapWithInnerDelete() throws Exception {
		Map<String, Variant> fromMap = getFromMap();

		fromMap.put("key2", Variant.fromNull());
		fromMap.put("aMap", Variant.fromNull());
		fromMap.put("aList", Variant.fromNull());
		Map <String, Variant> objectMap = new HashMap<String, Variant>();
		Map <String, Variant> innerDetails = new HashMap<String, Variant>();
		innerDetails.put("size", Variant.fromNull());
		innerDetails.put("temp", Variant.fromDouble(58.8));
		objectMap.put("details", Variant.fromVariantMap(innerDetails));
		fromMap.put("listOfObjects[*]", Variant.fromVariantMap(objectMap));

		// test
		final Map<String, Variant> result = CollectionUtils.addDataToMap(fromMap, getToMap(), true, true);

		assertEquals(10, result.size());
		assertTrue(result.containsKey("attachedKey"));
		assertEquals("attachedValue", result.get("attachedKey").getString());
		assertEquals("updatedValue1", result.get("key1").getString());
		assertEquals(552, result.get("anInt").getInteger());
		assertEquals(123, result.get("newInt").getInteger());
		assertEquals(123456, result.get("newLong").getLong());
		assertEquals(32.23, result.get("newDouble").getDouble(), .01);
		assertFalse(result.get("newBool").getBoolean());

		final Map<String, Variant> newMap = result.get("newMap").getVariantMap();
		assertEquals("newMapValue", newMap.get("newMapKey").getString());

		final List<Variant> newList = result.get("newList").getVariantList();
		assertEquals(1, newList.size());
		assertEquals("newListString", newList.get(0).getString());

		// deleted keys
		assertFalse(result.containsKey("key2"));
		assertFalse(result.containsKey("aMap"));
		assertFalse(result.containsKey("aList"));
		assertFalse(result.containsKey("newNull"));

		// inner delete from listOfObjects
		final List<Variant> listOfObjects = result.get("listOfObjects").getVariantList();
		assertEquals(2, listOfObjects.size());

		final Map<String, Variant> obj1 = listOfObjects.get(0).getVariantMap();
		assertEquals(2, obj1.size());
		assertEquals("request1", obj1.get("name").getString());
		final Map<String, Variant> obj1Details = obj1.get("details").getVariantMap();
		assertEquals(2, obj1Details.size());
		assertEquals("red", obj1Details.get("color").getString());
		assertFalse(obj1Details.containsKey("size"));
		assertEquals(58.8, obj1Details.get("temp").getDouble(), .01);

		final Map<String, Variant> obj2 = listOfObjects.get(1).getVariantMap();
		assertEquals(3, obj2.size());
		assertEquals("request2", obj2.get("name").getString());
		assertEquals("central", obj2.get("location").getString());
		final Map<String, Variant> obj2Details = obj2.get("details").getVariantMap();
		assertEquals(1, obj2Details.size());
		assertEquals(58.8, obj2Details.get("temp").getDouble(), .01);
	}

	@Test
	public void addDataToMap_When_Happy_Then_AddDataToMapWithDeleteNoNullValues() throws Exception {
		// this test should behave the same as passing false in to AddDataToMap
		// test
		final Map<String, Variant> result = CollectionUtils.addDataToMap(getFromMap(), getToMap(), true, true);
		assertEquals(13, result.size());
		assertTrue(result.containsKey("attachedKey"));
		assertEquals("attachedValue", result.get("attachedKey").getString());
		assertEquals("updatedValue1", result.get("key1").getString());
		assertEquals("value2", result.get("key2").getString());
		assertEquals(552, result.get("anInt").getInteger());
		assertEquals(123, result.get("newInt").getInteger());
		assertEquals(123456, result.get("newLong").getLong());
		assertEquals(32.23, result.get("newDouble").getDouble(), .01);
		assertFalse(result.get("newBool").getBoolean());

		final Map<String, Variant> aMap = result.get("aMap").getVariantMap();
		assertEquals("changedEmbeddedStringValue", aMap.get("embeddedString").getString());
		assertEquals("newEmbeddedStringValue", aMap.get("newEmbeddedString").getString());

		final Map<String, Variant> newMap = result.get("newMap").getVariantMap();
		assertEquals("newMapValue", newMap.get("newMapKey").getString());

		final List<Variant> aList = result.get("aList").getVariantList();
		assertEquals(2, aList.size());
		assertEquals("stringInList", aList.get(0).getString());
		assertEquals("newStringInList", aList.get(1).getString());

		final List<Variant> newList = result.get("newList").getVariantList();
		assertEquals(1, newList.size());
		assertEquals("newListString", newList.get(0).getString());

		final List<Variant> listOfObjects = result.get("listOfObjects").getVariantList();
		assertEquals(2, listOfObjects.size());

		final Map<String, Variant> obj1 = listOfObjects.get(0).getVariantMap();
		assertEquals(2, obj1.size());
		assertEquals("request1", obj1.get("name").getString());
		final Map<String, Variant> obj1Details = obj1.get("details").getVariantMap();
		assertEquals(3, obj1Details.size());
		assertEquals("orange", obj1Details.get("color").getString());
		assertEquals("large", obj1Details.get("size").getString());
		assertEquals(58.8, obj1Details.get("temp").getDouble(), .01);

		final Map<String, Variant> obj2 = listOfObjects.get(1).getVariantMap();
		assertEquals(3, obj2.size());
		assertEquals("request2", obj2.get("name").getString());
		assertEquals("central", obj2.get("location").getString());
		final Map<String, Variant> obj2Details = obj2.get("details").getVariantMap();
		assertEquals(2, obj2Details.size());
		assertEquals("orange", obj2Details.get("color").getString());
		assertEquals(58.8, obj2Details.get("temp").getDouble(), .01);
	}

	// =================================================================================================================
	// static Map<String, Variant> addDataToMap(final Map<String, Variant> toMap, final Map<String, Variant> fromMap)
	// =================================================================================================================
	@Test
	public void addDataToMap_When_Happy_Then_AddsDataAndDoesNotOverwriteExistingData() throws Exception {
		// test
		final Map<String, Variant> result = CollectionUtils.addDataToMap(getToMap(), getFromMap());

		// verify
		assertEquals(13, result.size());
		assertTrue(result.containsKey("attachedKey"));
		assertEquals("attachedValue", result.get("attachedKey").getString());
		assertEquals("value1", result.get("key1").getString());
		assertEquals("value2", result.get("key2").getString());
		assertEquals(552, result.get("anInt").getInteger());
		assertEquals(123, result.get("newInt").getInteger());
		assertEquals(123456, result.get("newLong").getLong());
		assertEquals(32.23, result.get("newDouble").getDouble(), .01);
		assertFalse(result.get("newBool").getBoolean());
		assertFalse(result.containsKey("newNull"));

		final Map<String, Variant> aMap = result.get("aMap").getVariantMap();
		assertEquals("embeddedStringValue", aMap.get("embeddedString").getString());
		assertEquals("newEmbeddedStringValue", aMap.get("newEmbeddedString").getString());

		final Map<String, Variant> newMap = result.get("newMap").getVariantMap();
		assertEquals("newMapValue", newMap.get("newMapKey").getString());

		final List<Variant> aList = result.get("aList").getVariantList();
		assertEquals(2, aList.size());
		assertEquals("stringInList", aList.get(0).getString());
		assertEquals("newStringInList", aList.get(1).getString());

		final List<Variant> newList = result.get("newList").getVariantList();
		assertEquals(1, newList.size());
		assertEquals("newListString", newList.get(0).getString());

		final List<Variant> listOfObjects = result.get("listOfObjects").getVariantList();
		assertEquals(2, listOfObjects.size());

		final Map<String, Variant> obj1 = listOfObjects.get(0).getVariantMap();
		assertEquals(2, obj1.size());
		assertEquals("request1", obj1.get("name").getString());
		final Map<String, Variant> obj1Details = obj1.get("details").getVariantMap();
		assertEquals(3, obj1Details.size());
		assertEquals("red", obj1Details.get("color").getString());
		assertEquals("large", obj1Details.get("size").getString());
		assertEquals(58.8, obj1Details.get("temp").getDouble(), .01);

		final Map<String, Variant> obj2 = listOfObjects.get(1).getVariantMap();
		assertEquals(3, obj2.size());
		assertEquals("request2", obj2.get("name").getString());
		assertEquals("central", obj2.get("location").getString());
		final Map<String, Variant> obj2Details = obj2.get("details").getVariantMap();
		assertEquals(2, obj2Details.size());
		assertEquals("orange", obj2Details.get("color").getString());
		assertEquals(58.8, obj2Details.get("temp").getDouble(), .01);
	}

	@Test
	public void addDataToMap_When_AllTheNestedMaps_Then_StillDoWhatItShould() throws Exception {
		// setup
		final Map<String, Variant> toMap = new HashMap<String, Variant>();
		toMap.put("key1", Variant.fromString("value1"));
		final Map<String, Variant> toMapInner = new HashMap<String, Variant>();
		toMapInner.put("innerKey1", Variant.fromString("innerValue1"));
		toMapInner.put("innerKey2", Variant.fromString("innerValue2"));
		final Map<String, Variant> toMapInnerInner = new HashMap<String, Variant>();
		toMapInnerInner.put("innerInnerKey1", Variant.fromString("innerInnerValue1"));
		toMapInner.put("innerInner", Variant.fromVariantMap(toMapInnerInner));
		toMap.put("inner", Variant.fromVariantMap(toMapInner));

		final Map<String, Variant> fromMap = new HashMap<String, Variant>();
		fromMap.put("key1", Variant.fromString("value1Changed"));
		fromMap.put("key2", Variant.fromString("value2"));
		final Map<String, Variant> fromMapInner = new HashMap<String, Variant>();
		fromMapInner.put("innerKey1", Variant.fromString("innerValue1Changed"));
		fromMapInner.put("innerKey3", Variant.fromString("innerValue3"));
		final Map<String, Variant> fromMapInnerInner = new HashMap<String, Variant>();
		fromMapInnerInner.put("innerInnerKey1", Variant.fromString("innerInnerValue1Changed"));
		fromMapInnerInner.put("innerInnerKey2", Variant.fromString("innerInnerValue2"));
		fromMapInner.put("innerInner", Variant.fromVariantMap(fromMapInnerInner));
		fromMap.put("inner", Variant.fromVariantMap(fromMapInner));

		// test
		final Map<String, Variant> result = CollectionUtils.addDataToMap(toMap, fromMap);

		// verify
		assertEquals("Should use value in toMap for key1", "value1", result.get("key1").getString());
		assertEquals("value2", result.get("key2").getString());

		final Map<String, Variant> inner = result.get("inner").getVariantMap();
		assertEquals("innerValue1", inner.get("innerKey1").getString());
		assertEquals("Should use value in toMap for inner.innerKey2", "innerValue2", inner.get("innerKey2").getString());
		assertEquals("innerValue3", inner.get("innerKey3").getString());

		final Map<String, Variant> innerInner = inner.get("innerInner").getVariantMap();
		assertEquals("Should use value in toMap for inner.innerInner.innerInnerKey1", "innerInnerValue1",
					 innerInner.get("innerInnerKey1").getString());
		assertEquals("innerInnerValue2", innerInner.get("innerInnerKey2").getString());
	}

	@Test
	public void addDataToMap_When_NestedForEachObject_Then_RecursivelyModifyAllTheObjects() throws Exception {
		// setup
		/* toMap json
		 {
			"key1": "value1",
			"listOfObjects": [
				{
					"name": "request1",
					"details": {
						"size": "large",
						"color": "red",
						"innerList": [
							{
								"innerKey": "innerValue"
							},
							{
								"innerKey2": "innerValue2"
							}
						]
					}
				},
				{
					"name": "request2",
					"location": "central",
					"details": {
						"innerList": [
							{
								"innerKey2": "innerValue2"
							},
							{
								"innerKey": "innerValue"
							}
						]
					}
				}
			]
		 }
		 */
		// begin toMap
		final Map<String, Variant> toMap = new HashMap<String, Variant>();
		toMap.put("key1", Variant.fromString("value1"));

		final List<Variant> listOfObjects = new ArrayList<Variant>();

		final Map<String, Variant> obj1 = new HashMap<String, Variant>();
		obj1.put("name", Variant.fromString("request1"));
		final Map<String, Variant> obj1Details = new HashMap<String, Variant>();
		obj1Details.put("size", Variant.fromString("large"));
		obj1Details.put("color", Variant.fromString("red"));
		final List<Variant> obj1InnerList = new ArrayList<Variant>();
		final Map<String, Variant> obj1InnerListFirst = new HashMap<String, Variant>();
		obj1InnerListFirst.put("innerKey", Variant.fromString("innerValue"));
		final Map<String, Variant> obj1InnerListSecond = new HashMap<String, Variant>();
		obj1InnerListSecond.put("innerKey2", Variant.fromString("innerValue2"));
		obj1InnerList.add(Variant.fromVariantMap(obj1InnerListFirst));
		obj1InnerList.add(Variant.fromVariantMap(obj1InnerListSecond));
		obj1Details.put("innerList", Variant.fromVariantList(obj1InnerList));
		obj1.put("details", Variant.fromVariantMap(obj1Details));
		listOfObjects.add(Variant.fromVariantMap(obj1));

		final Map<String, Variant> obj2 = new HashMap<String, Variant>();
		obj2.put("name", Variant.fromString("request2"));
		obj2.put("location", Variant.fromString("central"));
		final Map<String, Variant> obj2Details = new HashMap<String, Variant>();
		final List<Variant> obj2InnerList = new ArrayList<Variant>();
		final Map<String, Variant> obj2InnerListFirst = new HashMap<String, Variant>();
		obj2InnerListFirst.put("innerKey2", Variant.fromString("innerValue2"));
		final Map<String, Variant> obj2InnerListSecond = new HashMap<String, Variant>();
		obj2InnerListSecond.put("innerKey", Variant.fromString("innerValue"));
		obj2InnerList.add(Variant.fromVariantMap(obj2InnerListFirst));
		obj2InnerList.add(Variant.fromVariantMap(obj2InnerListSecond));
		obj2Details.put("innerList", Variant.fromVariantList(obj2InnerList));
		obj2.put("details", Variant.fromVariantMap(obj2Details));
		listOfObjects.add(Variant.fromVariantMap(obj2));

		toMap.put("listOfObjects", Variant.fromVariantList(listOfObjects));
		// end toMap

		/* fromMap json
		 {
			"listOfObjects[*]": {
				"details": {
					"color": "orange",
					"temp": 58.8,
					"innerList[*]": {
						"innerKey": "changedValue",
						"innerKey2": "changedValue2",
						"newKey": "newValue"
					}
				}
			}
		 }
		*/
		// begin fromMap
		final Map<String, Variant> fromMap = new HashMap<String, Variant>();
		final Map<String, Variant> listOfObjectsAsMap = new HashMap<String, Variant>();
		final Map<String, Variant> details = new HashMap<String, Variant>();
		details.put("color", Variant.fromString("orange"));
		details.put("temp", Variant.fromDouble(58.8));
		final Map<String, Variant> innerListAsMap = new HashMap<String, Variant>();
		innerListAsMap.put("innerKey", Variant.fromString("changedValue"));
		innerListAsMap.put("innerKey2", Variant.fromString("changedValue2"));
		innerListAsMap.put("newKey", Variant.fromString("newValue"));
		details.put("innerList[*]", Variant.fromVariantMap(innerListAsMap));
		listOfObjectsAsMap.put("details", Variant.fromVariantMap(details));

		fromMap.put("listOfObjects[*]", Variant.fromVariantMap(listOfObjectsAsMap));
		// end fromMap

		// test
		final Map<String, Variant> result = CollectionUtils.addDataToMap(toMap, fromMap);

		// verify
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals("value1", result.get("key1").getString());
		final List<Variant> resultListOfObjects = result.get("listOfObjects").getVariantList();

		final Map<String, Variant> resultObj1 = resultListOfObjects.get(0).getVariantMap();
		assertEquals(2, resultObj1.size());
		assertEquals("request1", resultObj1.get("name").getString());
		final Map<String, Variant> resultObj1Details = resultObj1.get("details").getVariantMap();
		assertEquals(4, resultObj1Details.size());
		assertEquals("large", resultObj1Details.get("size").getString());
		assertEquals("red", resultObj1Details.get("color").getString());
		assertEquals(58.8, resultObj1Details.get("temp").getDouble(), 0.01);
		final List<Variant> resultObj1InnerList = resultObj1Details.get("innerList").getVariantList();
		final Map<String, Variant> result1InnerObj1 = resultObj1InnerList.get(0).getVariantMap();
		assertEquals(3, result1InnerObj1.size());
		assertEquals("innerValue", result1InnerObj1.get("innerKey").getString());
		assertEquals("changedValue2", result1InnerObj1.get("innerKey2").getString());
		assertEquals("newValue", result1InnerObj1.get("newKey").getString());
		final Map<String, Variant> result1InnerObj2 = resultObj1InnerList.get(1).getVariantMap();
		assertEquals(3, result1InnerObj2.size());
		assertEquals("changedValue", result1InnerObj2.get("innerKey").getString());
		assertEquals("innerValue2", result1InnerObj2.get("innerKey2").getString());
		assertEquals("newValue", result1InnerObj2.get("newKey").getString());

		final Map<String, Variant> resultObj2 = resultListOfObjects.get(1).getVariantMap();
		assertEquals(3, resultObj2.size());
		assertEquals("request2", resultObj2.get("name").getString());
		assertEquals("central", resultObj2.get("location").getString());
		final Map<String, Variant> resultObj2Details = resultObj2.get("details").getVariantMap();
		assertEquals(3, resultObj2Details.size());
		assertEquals("orange", resultObj2Details.get("color").getString());
		assertEquals(58.8, resultObj2Details.get("temp").getDouble(), 0.01);
		final List<Variant> resultObj2InnerList = resultObj2Details.get("innerList").getVariantList();
		final Map<String, Variant> result2InnerObj1 = resultObj2InnerList.get(0).getVariantMap();
		assertEquals(3, result2InnerObj1.size());
		assertEquals("changedValue", result2InnerObj1.get("innerKey").getString());
		assertEquals("innerValue2", result2InnerObj1.get("innerKey2").getString());
		assertEquals("newValue", result2InnerObj1.get("newKey").getString());
		final Map<String, Variant> result2InnerObj2 = resultObj2InnerList.get(1).getVariantMap();
		assertEquals(3, result2InnerObj2.size());
		assertEquals("innerValue", result2InnerObj2.get("innerKey").getString());
		assertEquals("changedValue2", result2InnerObj2.get("innerKey2").getString());
		assertEquals("newValue", result2InnerObj2.get("newKey").getString());
	}

	@Test
	public void addDataToMap_When_ToMapObjectsDoNotHaveMatchingInnerObjects_Then_AddNewObjects() throws Exception {
		// setup
		/* toMap json
		 {
			"listOfObjects": [
				{
					"name": "request1"
				},
				{
					"name": "request2"
				}
			]
		 }
		 */
		// begin toMap
		final Map<String, Variant> toMap = new HashMap<String, Variant>();
		final List<Variant> toMapListOfObjects = new ArrayList<Variant>();

		final Map<String, Variant> toMapObj1 = new HashMap<String, Variant>();
		toMapObj1.put("name", Variant.fromString("request1"));
		toMapListOfObjects.add(Variant.fromVariantMap(toMapObj1));

		final Map<String, Variant> toMapObj2 = new HashMap<String, Variant>();
		toMapObj2.put("name", Variant.fromString("request2"));
		toMapListOfObjects.add(Variant.fromVariantMap(toMapObj2));

		toMap.put("listOfObjects", Variant.fromVariantList(toMapListOfObjects));
		// end toMap

		/* fromMap json
		 {
			"listOfObjects[*]": {
				"details": {
					"color": "orange",
					"temp": 58.8
				}
			}
		 }
		*/
		// begin fromMap
		final Map<String, Variant> fromMap = new HashMap<String, Variant>();
		final Map<String, Variant> listOfObjectsAsMap = new HashMap<String, Variant>();
		final Map<String, Variant> details = new HashMap<String, Variant>();
		details.put("color", Variant.fromString("orange"));
		details.put("temp", Variant.fromDouble(58.8));
		listOfObjectsAsMap.put("details", Variant.fromVariantMap(details));

		fromMap.put("listOfObjects[*]", Variant.fromVariantMap(listOfObjectsAsMap));
		// end fromMap

		// test
		final Map<String, Variant> result = CollectionUtils.addDataToMap(toMap, fromMap);

		// verify
		assertNotNull(result);
		assertEquals(1, result.size());
		final List<Variant> resultListOfObjects = result.get("listOfObjects").getVariantList();

		final Map<String, Variant> resultObj1 = resultListOfObjects.get(0).getVariantMap();
		assertEquals(2, resultObj1.size());
		assertEquals("request1", resultObj1.get("name").getString());
		final Map<String, Variant> resultObj1Details = resultObj1.get("details").getVariantMap();
		assertEquals(2, resultObj1Details.size());
		assertEquals("orange", resultObj1Details.get("color").getString());
		assertEquals(58.8, resultObj1Details.get("temp").getDouble(), 0.01);

		final Map<String, Variant> resultObj2 = resultListOfObjects.get(1).getVariantMap();
		assertEquals(2, resultObj2.size());
		assertEquals("request2", resultObj2.get("name").getString());
		final Map<String, Variant> resultObj2Details = resultObj2.get("details").getVariantMap();
		assertEquals(2, resultObj2Details.size());
		assertEquals("orange", resultObj2Details.get("color").getString());
		assertEquals(58.8, resultObj2Details.get("temp").getDouble(), 0.01);
	}

	@Test
	public void addDataToMap_When_EmptyFromMap_Then_ReturnsToMap() {
		// setup
		final Map<String, Variant> toMap = getToMap();
		final Map<String, Variant> fromMap = new HashMap<String, Variant>();

		// test
		final Map<String, Variant> result = CollectionUtils.addDataToMap(toMap, fromMap);

		// verify
		assertEquals(toMap, result);
	}

	@Test
	public void addDataToMap_When_NullVariant_Then_Ignore() throws Exception {
		// setup
		final Map<String, Variant> toMap = new HashMap<String, Variant>();
		toMap.put("stringKey", Variant.fromString("stringValue"));
		final Map<String, Variant> fromMap = new HashMap<String, Variant>();
		fromMap.put("nullKey", Variant.fromNull());
		fromMap.put("newStringKey", Variant.fromString("newStringValue"));

		// test
		final Map<String, Variant> result = CollectionUtils.addDataToMap(toMap, fromMap);

		// verify
		assertEquals(2, result.size());
		assertEquals("stringValue", result.get("stringKey").getString());
		assertEquals("newStringValue", result.get("newStringKey").getString());
		assertFalse(result.containsKey("nullKey"));
	}

	@Test
	public void addDataToMap_When_MapHasMatchingKeyWithDifferentVariantType_Then_Ignore() throws Exception {
		// setup
		final Map<String, Variant> toMap = new HashMap<String, Variant>();
		toMap.put("existingKey", Variant.fromString("nonMapValue"));
		final Map<String, Variant> fromMap = new HashMap<String, Variant>();
		final Map<String, Variant> innerMap = new HashMap<String, Variant>();
		innerMap.put("innerKey", Variant.fromString("innerValue"));
		fromMap.put("existingKey", Variant.fromVariantMap(innerMap));

		// test
		final Map<String, Variant> result = CollectionUtils.addDataToMap(toMap, fromMap);

		// verify
		assertEquals(1, result.size());
		assertEquals("nonMapValue", result.get("existingKey").getString());
	}

	@Test
	public void addDataToMap_When_ListHasMatchingKeyWithDifferentVariantType_Then_Ignore() throws Exception {
		// setup
		final Map<String, Variant> toMap = new HashMap<String, Variant>();
		toMap.put("existingKey", Variant.fromString("nonListValue"));
		final Map<String, Variant> fromMap = new HashMap<String, Variant>();
		final List<Variant> innerList = new ArrayList<Variant>();
		innerList.add(Variant.fromString("innerValue"));
		fromMap.put("existingKey", Variant.fromVariantList(innerList));

		// test
		final Map<String, Variant> result = CollectionUtils.addDataToMap(toMap, fromMap);

		// verify
		assertEquals(1, result.size());
		assertEquals("nonListValue", result.get("existingKey").getString());
	}

	// =================================================================================================================
	// static boolean keyExistsAndValueIsNull(final String key, final Map<String, Variant> map)
	// =================================================================================================================
	@Test
	public void keyExistsAndValueIsNull_When_Happy_KeyIsEmptyString() throws Exception {
		assertFalse(CollectionUtils.keyExistsAndValueIsNull("", getFromMap()));
	}

	@Test
	public void keyExistsAndValueIsNull_When_Happy_KeyExistsValueIsNull() throws Exception {
		assertTrue(CollectionUtils.keyExistsAndValueIsNull("newNull", getFromMap()));
	}

	@Test
	public void keyExistsAndValueIsNull_When_Happy_KeyExistsValueNotNull() throws Exception {
		assertFalse(CollectionUtils.keyExistsAndValueIsNull("newInt", getFromMap()));
	}

	@Test
	public void keyExistsAndValueIsNull_When_Happy_KeyDoesNotExist() throws Exception {
		assertFalse(CollectionUtils.keyExistsAndValueIsNull("notHere", getFromMap()));
	}

	// =================================================================================================================
	// static List<Variant> addDataToList(final List<Variant> toList, final List<Variant> fromList)
	// =================================================================================================================
	@Test
	public void addDataToList_When_Happy_Then_MergeAndDeDuplicate() throws Exception {
		// test
		final List<Variant> result = CollectionUtils.addDataToList(getToList(), getFromList());

		// verify
		assertEquals(11, result.size());

		// begin validation of values in toList
		assertEquals("listString", result.get(0).getString());
		assertEquals(552, result.get(1).getInteger());

		final Map<String, Variant> embeddedMap = result.get(2).getVariantMap();
		assertEquals("embeddedStringValue", embeddedMap.get("embeddedString").getString());

		final List<Variant> embeddedList = result.get(3).getVariantList();
		assertEquals("stringInList", embeddedList.get(0).getString());
		// end validation of values in toList

		// begin validation of values in fromList
		// fromList has two values that are also in toList: "listString", and 552
		// we ensure they are not duplicated by accounting for each element in the resulting list
		assertEquals("listString2", result.get(4).getString());
		assertEquals(553, result.get(5).getInteger());
		assertEquals(123456, result.get(6).getLong());
		assertEquals(32.23, result.get(7).getDouble(), .01);
		assertFalse(result.get(8).getBoolean());

		final Map<String, Variant> embeddedMap2 = result.get(9).getVariantMap();
		assertEquals("anotherEmbeddedStringValue", embeddedMap2.get("anotherEmbeddedString").getString());

		final List<Variant> embeddedList2 = result.get(10).getVariantList();
		assertEquals("anotherStringInList", embeddedList2.get(0).getString());
		// end validation of values in fromList
	}

	@Test
	public void addDataToList_When_EmptyFromList_Then_ReturnToList() {
		// setup
		final List<Variant> toList = getToList();
		final List<Variant> fromList = new ArrayList<Variant>();

		// test
		final List<Variant> result = CollectionUtils.addDataToList(toList, fromList);

		// verify
		assertEquals(toList, result);
	}

	@Test
	public void addDataToList_When_NullVariants_Then_Ignore() throws Exception {
		// setup
		final List<Variant> toList = new ArrayList<Variant>();
		toList.add(Variant.fromString("stringValue"));
		final List<Variant> fromList = new ArrayList<Variant>();
		fromList.add(Variant.fromNull());
		fromList.add(Variant.fromString("newStringValue"));

		// test
		final List<Variant> result = CollectionUtils.addDataToList(toList, fromList);

		// verify
		assertEquals(2, result.size());
		assertEquals("stringValue", result.get(0).getString());
		assertEquals("newStringValue", result.get(1).getString());
	}

	// =================================================================================================================
	// static Map<String, Variant> deleteIfEmpty(final Map<String, Variant> map)
	// =================================================================================================================
	@Test
	public void deleteIfEmpty_When_NullVariants_Then_DeleteFromMap() throws Exception {
		Map<String, Variant> fromMap = getFromMap();

		// test
		Map<String, Variant> result  = CollectionUtils.deleteIfEmpty(fromMap);

		assertEquals(fromMap.size() - 2, result.size());
		assertFalse(result.containsKey("newNull"));
		assertFalse(result.containsKey("listOfObjects[*]"));
	}

	@Test
	public void deleteIfEmpty_When_Happy_Then_DeleteNullsFromMapEmptyParam() throws Exception {
		Map<String, Variant> fromMap = new HashMap<String, Variant>();

		// test
		Map<String, Variant> result  = CollectionUtils.deleteIfEmpty(fromMap);

		assertEquals(fromMap, result);
	}

	@Test
	public void deleteIfEmpty_When_Happy_Then_DeleteNullsFromMapInnerDelete() throws Exception {
		Map<String, Variant> fromMap = getFromMap();
		Map <String, Variant> objectMap = new HashMap<String, Variant>();
		Map <String, Variant> innerDetails = new HashMap<String, Variant>();
		innerDetails.put("size", Variant.fromNull());
		innerDetails.put("temp", Variant.fromDouble(58.8));
		objectMap.put("details", Variant.fromVariantMap(innerDetails));
		fromMap.put("listOfObjects", Variant.fromVariantMap(objectMap));

		// test
		Map<String, Variant> result  = CollectionUtils.deleteIfEmpty(fromMap);

		assertEquals(fromMap.size() - 2, result.size());
		assertFalse(result.containsKey("newNull"));
		assertFalse(result.containsKey("listOfObjects[*]"));

		Map<String, Variant> listOfObjectsMap = result.get("listOfObjects").optVariantMap(new HashMap<String, Variant>());
		assertEquals(1, listOfObjectsMap.size());
		assertFalse(listOfObjectsMap.get("details").optVariantMap(new HashMap<String, Variant>()).containsKey("size"));
		assertEquals(58.8, listOfObjectsMap.get("details").optVariantMap(new
					 HashMap<String, Variant>()).get("temp").getDouble(), 0.01);
	}
}
