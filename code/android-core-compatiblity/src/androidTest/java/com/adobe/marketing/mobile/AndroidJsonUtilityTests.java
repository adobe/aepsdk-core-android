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


import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class AndroidJsonUtilityTests {

	private AndroidJsonUtility androidJsonUtility = new AndroidJsonUtility();

	@Before
	public void setup() {
		androidJsonUtility = new AndroidJsonUtility();
	}

	@Test
	public void testToString() throws Exception {
		//Setup
		HashMap<String, String> map = new HashMap<>();
		map.put("key1", "value1");
		// test
		String s = androidJsonUtility.createJSONObject(map).toString();
		// verify
		JSONObject jsonObject = new JSONObject(s);
		assertEquals("value1", jsonObject.getString("key1"));
	}

	@Test
	public void testToString_When_MapContains_AnotherMap_Then_Convert_Into_NestedJSONObject() throws Exception {
		//Setup
		HashMap<String, Object> map = new HashMap<>();
		HashMap<String, Object> valueMap = new HashMap<>();
		valueMap.put("nestedKey1", "nestedValue1");
		valueMap.put("nestedKey2", 1234);

		Map<String, Object> nestedInTheValueMap = new HashMap<>();
		nestedInTheValueMap.put("nestedintheValueMapKey1", 123456);
		String [] strArray = {
			"stringItem1", "stringItem2"
		};

		nestedInTheValueMap.put("nestedintheValueMapKey2", strArray);
		valueMap.put("nestedKey3", nestedInTheValueMap);
		valueMap.put("nestedKey4", false);
		valueMap.put("nestedKey5", 12345L);
		valueMap.put("nestedKey6", 1234.667);
		int [] intArray = {12, 34, 56};
		valueMap.put("nestedKey7", intArray);
		Object[] objArray = new Object[2];
		objArray[0] = Boolean.TRUE;
		objArray[1] = strArray;
		valueMap.put("nestedKey8", objArray);


		map.put("key1", "value1");
		map.put("key2", valueMap);
		// test
		String s = androidJsonUtility.createJSONObject(map).toString();
		// verify
		JSONObject jsonObject = new JSONObject(s);
		assertEquals("value1", jsonObject.getString("key1"));
		JSONObject valueObject = jsonObject.getJSONObject("key2");
		assertEquals("nestedValue1", valueObject.getString("nestedKey1"));
		assertEquals(1234, valueObject.getInt("nestedKey2"));
		JSONObject nestedValueObject = valueObject.getJSONObject("nestedKey3");
		assertEquals(123456, nestedValueObject.getInt("nestedintheValueMapKey1"));
		JSONArray nestedValueStrArray = nestedValueObject.getJSONArray("nestedintheValueMapKey2");
		assertEquals("stringItem1", nestedValueStrArray.getString(0));
		assertEquals("stringItem2", nestedValueStrArray.getString(1));
		assertFalse(valueObject.getBoolean("nestedKey4"));
		assertEquals(12345L, valueObject.getLong("nestedKey5"));
		assertEquals(1234.667, valueObject.getDouble("nestedKey6"), 0);
		JSONArray jsonIntArray = valueObject.getJSONArray("nestedKey7");
		assertEquals(12, jsonIntArray.getInt(0));
		assertEquals(34, jsonIntArray.getInt(1));
		assertEquals(56, jsonIntArray.getInt(2));
		JSONArray jsonObjArray = valueObject.getJSONArray("nestedKey8");
		assertTrue(jsonObjArray.getBoolean(0));
		JSONArray strArrayNested = jsonObjArray.getJSONArray(1);
		assertEquals("stringItem1", strArrayNested.getString(0));

	}

	@Test
	public void testToString_When_ParamNull_Then_ResultNull() throws Exception {
		// test
		Map map = null;
		JsonUtilityService.JSONObject jsonObject = androidJsonUtility.createJSONObject(map);
		// verify
		assertNull(jsonObject);
	}

	@Test
	public void testParseJsonArray_When_Input_EmptyString_Then_Exception() throws Exception {
		// test
		assertNull(androidJsonUtility.createJSONArray(""));
	}

	@Test
	public void testParseJsonArray_When_Input_Null_Then_ReturnNull() throws Exception {
		// test
		assertNull(androidJsonUtility.createJSONArray(null));
	}

	@Test
	public void testParseJsonObject_When_Input_Null_Then_ReturnNull() throws Exception {
		// test
		assertNull(androidJsonUtility.createJSONObject((String)null));
	}

	@Test
	public void testParseJsonArray_When_Input_Not_JsonArray_Then_Exception() throws Exception {
		// test
		JsonUtilityService.JSONArray array =
			androidJsonUtility.createJSONArray("{\"key2\":{\"nestedKey1\":\"nestedValue1\"},\"key1\":\"value1\"}");
		// verify
		assertNull(array);
	}

	@Test
	public void testParseJsonArray_When_Input_Valid() throws Exception {
		// test
		JSONArray jsonArray = new JSONArray()
		.put("value1")
		.put("value2");
		JsonUtilityService.JSONArray array = androidJsonUtility.createJSONArray(jsonArray.toString());
		// verify
		assertEquals(2, array.length());
		assertEquals("value1", array.getString(0));
		assertEquals("value2", array.getString(1));
	}

	@Test
	public void testParseJsonArray_With_Input_Containing_NestedJsonObject() throws Exception {
		// test
		JSONArray jsonArray = new JSONArray()
		.put("value1")
		.put(new JSONObject()
			 .put("key1", "value1")
			 .put("key2", "value2"));
		JsonUtilityService.JSONArray array = androidJsonUtility.createJSONArray(jsonArray.toString());
		// verify
		assertEquals(2, array.length());
		assertEquals("value1", array.getString(0));
		assertNotNull(array.getJSONObject(1));
	}

	@Test
	public void testParseJsonArray_GetInt_With_Input_Containing_NestedJsonObject() throws Exception {
		// test
		JSONArray jsonArray = new JSONArray()
		.put(1)
		.put(new JSONObject()
			 .put("key1", "value1")
			 .put("key2", "value2"));
		JsonUtilityService.JSONArray array = androidJsonUtility.createJSONArray(jsonArray.toString());
		// verify
		assertEquals(2, array.length());
		assertEquals(1, array.getInt(0));
		assertNotNull(array.getJSONObject(1));
	}

	@Test
	public void testParseJsonArray_GetBoolean_With_Input_Containing_NestedJsonObject() throws Exception {
		// test
		JSONArray jsonArray = new JSONArray();
		jsonArray.put(true)
		.put(new JSONObject()
			 .put("key1", "value1")
			 .put("key2", "value2")
			);
		JsonUtilityService.JSONArray array = androidJsonUtility.createJSONArray(jsonArray.toString());
		// verify
		assertEquals(2, array.length());
		assertTrue(array.getBoolean(0));
		assertNotNull(array.getJSONObject(1));
	}

	@Test
	public void testParseJsonArray_GetDouble_With_Input_Containing_NestedJsonObject() throws Exception {
		// test
		JSONArray jsonArray = new JSONArray()
		.put("1.11")
		.put(new JSONObject()
			 .put("key1", "value1")
			 .put("key2", "value2"));
		JsonUtilityService.JSONArray array = androidJsonUtility.createJSONArray(jsonArray.toString());
		// verify
		assertEquals(2, array.length());
		assertEquals(1.11, array.getDouble(0), 0);
		assertNotNull(array.getJSONObject(1));
	}

	@Test
	public void testParseJsonArray_GetLong_With_Input_Containing_NestedJsonObject() throws Exception {
		// test
		JSONArray jsonArray = new JSONArray()
		.put("1123132")
		.put(new JSONObject()
			 .put("key1", "value1")
			 .put("key2", "value2"));
		JsonUtilityService.JSONArray array = androidJsonUtility.createJSONArray(jsonArray.toString());
		// verify
		assertEquals(2, array.length());
		assertEquals(1123132, array.getLong(0));
		assertNotNull(array.getJSONObject(1));
	}

	@Test
	public void testParseJsonArray_GetJSONArray_With_Input_Containing_NestedJsonObject() throws Exception {
		// test
		JSONArray jsonArray = new JSONArray()
		.put("value1")
		.put(new JSONArray()
			 .put("nestedValue1")
			 .put("nestedValue2"));
		JsonUtilityService.JSONArray array = androidJsonUtility.createJSONArray(jsonArray.toString());
		// verify
		assertEquals(2, array.length());
		assertEquals("value1", array.getString(0));
		assertNotNull(array.getJSONArray(1));
	}

	@Test(expected = JsonException.class)
	public void testParseJsonArray_GetInt_When_IndexInvalid_Then_Throw_Exception() throws Exception {
		// test
		JSONArray jsonArray = new JSONArray()
		.put("14525")
		.put(new JSONObject()
			 .put("key1", "value1")
			 .put("key2", "value2"));
		JsonUtilityService.JSONArray array = androidJsonUtility.createJSONArray(jsonArray.toString());
		// verify
		assertEquals(2, array.length());
		//Use of invalid index should throw exception
		array.getInt(2);
	}

	@Test(expected = JsonException.class)
	public void testParseJsonArray_GetLong_With_IndexInvalid_Then_Throw_Exception() throws Exception {
		// test
		JSONArray jsonArray = new JSONArray()
		.put("14525")
		.put(new JSONObject()
			 .put("key1", "value1")
			 .put("key2", "value2"));
		JsonUtilityService.JSONArray array = androidJsonUtility.createJSONArray(jsonArray.toString());
		// verify
		assertEquals(2, array.length());
		//Use of invalid index should throw exception
		array.getLong(2);
	}

	@Test(expected = JsonException.class)
	public void testParseJsonArray_GetBoolean_With_IndexInvalid_Then_Throw_Exception() throws Exception {
		// test
		JSONArray jsonArray = new JSONArray()
		.put("14525")
		.put(new JSONObject()
			 .put("key1", "value1")
			 .put("key2", "value2"));
		JsonUtilityService.JSONArray array = androidJsonUtility.createJSONArray(jsonArray.toString());
		// verify
		assertEquals(2, array.length());
		//Use of invalid index should throw exception
		array.getBoolean(2);
	}

	@Test(expected = JsonException.class)
	public void testParseJsonArray_GetJSONArray_With_IndexInvalid_Then_Throw_Exception() throws Exception {
		// test
		JSONArray jsonArray = new JSONArray()
		.put("14525")
		.put(new JSONObject()
			 .put("key1", "value1")
			 .put("key2", "value2"));
		JsonUtilityService.JSONArray array = androidJsonUtility.createJSONArray(jsonArray.toString());
		// verify
		assertEquals(2, array.length());
		//Use of invalid index should throw exception
		array.getJSONArray(2);
	}

	@Test(expected = JsonException.class)
	public void testParseJsonArray_GetDouble_When_IndexInvalid_Then_Throw_Exception() throws Exception {
		// test
		JSONArray jsonArray = new JSONArray()
		.put("14525")
		.put(new JSONObject()
			 .put("key1", "value1")
			 .put("key2", "value2"));
		JsonUtilityService.JSONArray array = androidJsonUtility.createJSONArray(jsonArray.toString());
		// verify
		assertEquals(2, array.length());
		//Use of invalid index should throw exception
		array.getDouble(2);
	}

	@Test(expected = JsonException.class)
	public void testParseJsonArray_GetJSONObject_When_IndexInvalid_Then_Throw_Exception() throws Exception {
		// test
		JSONArray jsonArray = new JSONArray()
		.put("14525")
		.put(new JSONObject()
			 .put("key1", "value1")
			 .put("key2", "value2"));
		JsonUtilityService.JSONArray array = androidJsonUtility.createJSONArray(jsonArray.toString());
		// verify
		assertEquals(2, array.length());
		//Use of invalid index should throw exception
		array.getJSONObject(2);
	}

	@Test(expected = JsonException.class)
	public void testParseJsonArray_GetString_When_IndexInvalid_Then_Throw_Exception() throws Exception {
		// test
		JSONArray jsonArray = new JSONArray()
		.put("14525")
		.put(new JSONObject()
			 .put("key1", "value1")
			 .put("key2", "value2"));
		JsonUtilityService.JSONArray array = androidJsonUtility.createJSONArray(jsonArray.toString());
		// verify
		assertEquals(2, array.length());
		//Use of invalid index should throw exception
		array.getString(2);
	}

	@Test
	public void testParseJsonObject_When_Input_EmptyString_Then_Exception() throws Exception {
		// test
		assertNull(androidJsonUtility.createJSONObject(""));
	}

	@Test
	public void testParseJsonObject_When_Input_Null_Then_Exception() throws Exception {
		// test
		Map map = null;
		assertNull(androidJsonUtility.createJSONObject(map));
	}

	@Test
	public void testParseJsonObject_When_Input_Not_JsonObject_Then_Exception() throws Exception {
		// test
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(
				"[\n" +
				"  \"value1\",\n" +
				"  [\n" +
				"    \"nestedValue1\",\n" +
				"    \"nestedValue2\"\n" +
				"  ]\n" +
				"]");
		// verify
		assertNull(object);
	}

	@Test
	public void testParseJsonObject_When_Input_Valid() throws Exception {
		// test
		JSONObject jsonObject = new JSONObject()
		.put("key1", "value1")
		.put("key2", new JSONArray()
			 .put("nestedValue1")
			 .put("nestedValue2")
			);
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(jsonObject.toString());
		// verify
		assertEquals(2, object.length());
		assertEquals("value1", object.getString("key1"));
		assertNotNull(object.getJSONArray("key2"));
	}

	@Test
	public void testParseJsonObject_GetInt_With_Input_Containing_NestedJsonArray() throws Exception {
		// test
		JSONObject jsonObject = new JSONObject()
		.put("key1", "1")
		.put("key2", new JSONArray()
			 .put("nestedValue1")
			 .put("nestedValue2")
			);
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(jsonObject.toString());
		// verify
		assertEquals(2, object.length());
		assertEquals(1, object.getInt("key1"));
		assertNotNull(object.getJSONArray("key2"));
	}

	@Test
	public void testParseJsonObject_GetBoolean_With_Input_Containing_NestedJsonArray() throws Exception {
		// test
		JSONObject jsonObject = new JSONObject()
		.put("key1", "true")
		.put("key2", new JSONArray()
			 .put("nestedValue1")
			 .put("nestedValue2")
			);
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(jsonObject.toString());
		// verify
		assertEquals(2, object.length());
		assertTrue(object.getBoolean("key1"));
		assertNotNull(object.getJSONArray("key2"));
	}

	@Test
	public void testParseJsonObject_GetDouble_With_Input_Containing_NestedJsonArray() throws Exception {
		// test
		JSONObject jsonObject = new JSONObject()
		.put("key1", "1.11")
		.put("key2", new JSONArray()
			 .put("nestedValue1")
			 .put("nestedValue2")
			);
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(jsonObject.toString());
		// verify
		assertEquals(2, object.length());
		assertEquals(1.11, object.getDouble("key1"), 0);
		assertNotNull(object.getJSONArray("key2"));
	}

	@Test
	public void testParseJsonObject_GetLong_With_Input_Containing_NestedJsonArray() throws Exception {
		// test
		JSONObject jsonObject = new JSONObject()
		.put("key1", "1123132")
		.put("key2", new JSONArray()
			 .put("nestedValue1")
			 .put("nestedValue2")
			);
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(jsonObject.toString());
		// verify
		assertEquals(2, object.length());
		assertEquals(1123132, object.getLong("key1"));
		assertNotNull(object.getJSONArray("key2"));
	}

	@Test
	public void testParseJsonObject_GetJSONObject_With_Input_Containing_NestedJsonArray() throws Exception {
		// test
		JSONObject jsonObject = new JSONObject()
		.put("key1", "value1")
		.put("key2", new JSONObject()
			 .put("nestedKey1", "nestedValue1")
			 .put("nestedKey2", "nestedValue2")
			);
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(jsonObject.toString());
		// verify
		assertEquals(2, object.length());
		assertNotNull(object.getJSONObject("key2"));
	}

	@Test
	public void testParseJsonObject_GetKeys_When_JsonObject_Has_Two_Keys_Return_Valid_Iterator() throws Exception {
		// test
		JSONObject jsonObject = new JSONObject()
		.put("key1", "value1")
		.put("key2", new JSONObject()
			 .put("nestedKey1", "nestedValue1")
			 .put("nestedKey2", "nestedValue2")
			);
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(jsonObject.toString());
		// verify
		assertEquals(2, object.length());
		Iterator<String> keys = object.keys();
		assertTrue(keys.hasNext());
	}

	@Test
	public void testParseJsonObject_GetKeys_When_JsonObject_Has_No_Keys_Return_Iterator_With_No_Items() throws Exception {
		// test
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(
				"{}");
		// verify
		assertEquals(0, object.length());
		Iterator<String> keys = object.keys();
		assertFalse(keys.hasNext());
	}

	@Test(expected = JsonException.class)
	public void testParseJsonObject_With_When_GetString_KeyInvalid_Then_Throw_Exception() throws Exception {
		// test
		JSONObject jsonObject = new JSONObject()
		.put("key1", "value1")
		.put("key2", new JSONArray()
			 .put("nestedValue1")
			 .put("nestedValue2")
			);
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(jsonObject.toString());
		// verify
		assertEquals(2, object.length());
		//Use of invalid key should throw exception
		object.getString("nonexistingKey");
	}

	@Test(expected = JsonException.class)
	public void testParseJsonObject_With_When_GetJSONArray_KeyInvalid_Then_Throw_Exception() throws Exception {
		// test
		JSONObject jsonObject = new JSONObject()
		.put("key1", "value1")
		.put("key2", new JSONArray()
			 .put("nestedValue1")
			 .put("nestedValue2")
			);
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(jsonObject.toString());
		// verify
		assertEquals(2, object.length());
		//Use of invalid key should throw exception
		object.getJSONArray("nonexistingKey");
	}

	@Test(expected = JsonException.class)
	public void testParseJsonObject_With_When_GetBoolean_KeyInvalid_Then_Throw_Exception() throws Exception {
		// test
		JSONObject jsonObject = new JSONObject()
		.put("key1", "value1")
		.put("key2", new JSONArray()
			 .put("nestedValue1")
			 .put("nestedValue2")
			);
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(jsonObject.toString());
		// verify
		assertEquals(2, object.length());
		//Use of invalid key should throw exception
		object.getBoolean("nonexistingKey");
	}

	@Test(expected = JsonException.class)
	public void testParseJsonObject_With_When_GetDouble_KeyInvalid_Then_Throw_Exception() throws Exception {
		// test
		JSONObject jsonObject = new JSONObject()
		.put("key1", "value1")
		.put("key2", new JSONArray()
			 .put("nestedValue1")
			 .put("nestedValue2")
			);
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(jsonObject.toString());
		// verify
		assertEquals(2, object.length());
		//Use of invalid key should throw exception
		object.getDouble("nonexistingKey");
	}

	@Test(expected = JsonException.class)
	public void testParseJsonObject_With_When_GetInt_KeyInvalid_Then_Throw_Exception() throws Exception {
		// test
		JSONObject jsonObject = new JSONObject()
		.put("key1", "value1")
		.put("key2", new JSONArray()
			 .put("nestedValue1")
			 .put("nestedValue2")
			);
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(jsonObject.toString());
		// verify
		assertEquals(2, object.length());
		//Use of invalid key should throw exception
		object.getInt("nonexistingKey");
	}

	@Test(expected = JsonException.class)
	public void testParseJsonObject_With_When_GetLong_KeyInvalid_Then_Throw_Exception() throws Exception {
		// test
		JSONObject jsonObject = new JSONObject()
		.put("key1", "value1")
		.put("key2", new JSONArray()
			 .put("nestedValue1")
			 .put("nestedValue2")
			);
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(jsonObject.toString());
		// verify
		assertEquals(2, object.length());
		//Use of invalid key should throw exception
		object.getLong("nonexistingKey");
	}

	@Test(expected = JsonException.class)
	public void testParseJsonObject_With_When_GetJSONObject_KeyInvalid_Then_Throw_Exception() throws Exception {
		// test
		JSONObject jsonObject = new JSONObject()
		.put("key1", "value1")
		.put("key2", new JSONArray()
			 .put("nestedValue1")
			 .put("nestedValue2")
			);
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(jsonObject.toString());
		// verify
		assertEquals(2, object.length());
		//Use of invalid key should throw exception
		object.getJSONObject("nonexistingKey");
	}

	@Test
	public void testCreateJsonObject() throws Exception {
		JsonUtilityService.JSONObject jsonObject = androidJsonUtility.createJSONObject("{}");
		assertNotNull(jsonObject);
		assertEquals(0, jsonObject.length());
	}

	@Test
	public void testCreateJsonArray() throws Exception {
		JsonUtilityService.JSONArray jsonArray = androidJsonUtility.createJSONArray("[]");
		assertNotNull(jsonArray);
		assertEquals(0, jsonArray.length());
	}

}