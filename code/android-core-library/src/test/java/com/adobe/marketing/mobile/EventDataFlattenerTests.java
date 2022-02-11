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

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;


public class EventDataFlattenerTests {

	@Test
	public void getFlattenedMap_ReturnsEmptyMap_WhenEventDataNull() {
		//Test
		assertEquals("The returned flattened map should be empty!", 0, EventDataFlattener.getFlattenedDataMap(null).size());

	}

	@Test
	public void getFlattenedMap_ReturnsFlattenedMap_WhenEventDataNotNull() throws  Exception {
		//Setup
		EventData eventData = new EventData();
		eventData.putBoolean("boolKey", true);
		eventData.putInt("intKey", 1);
		eventData.putLong("longKey", 100L);
		eventData.putString("stringKey", "stringValue");
		Map<String, String> mapValue = new HashMap<String, String>();
		mapValue.put("mapStrKey", "mapStrValue");
		eventData.putMap("mapKey", mapValue);
		eventData.putObject("objKey", new StringBuilder());

		//Test
		Map<String, Variant> flattenedmap = EventDataFlattener.getFlattenedDataMap(eventData);

		//Verify
		assertEquals("The value should be Boolean(true)", true, flattenedmap.get("boolKey").getBoolean());
		assertEquals("The value should be an int (1)", 1, flattenedmap.get("intKey").getInteger());
		assertEquals("The value should be a Long (100)", 100L, flattenedmap.get("longKey").getLong());
		assertEquals("The value should be a String (\"stringValue\")", "stringValue",
					 flattenedmap.get("stringKey").getString());
		assertEquals("The value should be a String (mapStrValue). The key expected is a flattenned key (\"mapKey.mapStrKey\")",
					 "mapStrValue", flattenedmap.get("mapKey.mapStrKey").getString());
		assertTrue("The value should be of type StringBuilder",
				   flattenedmap.get("objKey").getObject() instanceof StringBuilder);

	}
}
