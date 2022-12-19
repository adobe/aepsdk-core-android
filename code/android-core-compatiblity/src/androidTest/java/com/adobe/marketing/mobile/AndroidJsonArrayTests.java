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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class AndroidJsonArrayTests {

	private AndroidJsonUtility androidJsonUtility = new AndroidJsonUtility();

	@Before
	public void setup() {
		androidJsonUtility = new AndroidJsonUtility();
	}

	@Test
	public void testJsonArray_OptString_When_Index_Invalid_Then_ReturnDefault() throws Exception {
		// test
		JSONArray jsonArray = new JSONArray()
		.put("value1")
		.put(new JSONArray()
			 .put("nestedValue1")
			 .put("nestedValue2"));
		JsonUtilityService.JSONArray array = androidJsonUtility.createJSONArray(jsonArray.toString());
		// verify
		assertEquals(2, array.length());
		String value = array.optString(3, "defaultValue");
		assertEquals("defaultValue", value);
	}

	@Test
	public void testJsonArray_OptString_When_Index_Valid_Then_Return_ValidValue() throws Exception {
		// test
		JSONArray jsonArray = new JSONArray()
		.put("value1")
		.put(new JSONArray()
			 .put("nestedValue1")
			 .put("nestedValue2"));
		JsonUtilityService.JSONArray array = androidJsonUtility.createJSONArray(jsonArray.toString());
		// verify
		assertEquals(2, array.length());
		String value = array.optString(0, "value6");
		assertEquals("value1", value);
	}

	@Test
	public void testJsonArray_optJsonObject_When_Index_Invalid_Then_ReturnNull() throws Exception {
		// test
		JSONArray androidJsonArray = new JSONArray()
		.put("value1")
		.put(new JSONObject()
			 .put("key1", "value1")
			 .put("key2", "value2"));
		JsonUtilityService.JSONArray array = androidJsonUtility.createJSONArray(androidJsonArray.toString());
		// verify
		assertEquals(2, array.length());
		assertNull(array.optJSONObject(3));
	}

	@Test
	public void testJsonArray_optJSONObject_When_Index_Valid_Then_Return_ValidValue() throws Exception {
		// test
		JSONArray androidJsonArray = new JSONArray()
		.put("value1")
		.put(new JSONObject()
			 .put("key1", "value1")
			 .put("key2", "value2"));
		JsonUtilityService.JSONArray array = androidJsonUtility.createJSONArray(androidJsonArray.toString());
		// verify
		assertEquals(2, array.length());
		assertEquals("value1", array.optJSONObject(1).getString("key1"));
		assertEquals("value2", array.optJSONObject(1).getString("key2"));
	}

	@Test
	public void testJsonArray_optJSONArray_ReturnNull_WhenInvalidIndex() throws Exception {
		// test
		JSONArray jsonArray = new JSONArray()
		.put("value1")
		.put(new JSONArray()
			 .put("nestedValue1")
			 .put("nestedValue2"));
		JsonUtilityService.JSONArray array = androidJsonUtility.createJSONArray(jsonArray.toString());
		// verify
		assertEquals(2, array.length());
		assertNull(array.optJSONArray(3));
	}

	@Test
	public void testJsonArray_optJSONArray_Returns_ValidValue() throws Exception {
		// test
		JSONArray androidJsonArray = new JSONArray()
		.put("value1")
		.put(new JSONArray()
			 .put("nestedValue1")
			 .put("nestedValue2"));
		JsonUtilityService.JSONArray array = androidJsonUtility.createJSONArray(androidJsonArray.toString());
		// verify
		assertEquals(2, array.length());
		assertEquals("value1", array.getString(0));
		assertEquals("nestedValue1", array.optJSONArray(1).getString(0));
		assertEquals("nestedValue2", array.optJSONArray(1).getString(1));
		assertNotNull(array.optJSONArray(1));
	}

	@Test
	public void testJsonArray_optJsonArray_When_NotJsonArray_ReturnNull() throws Exception {
		// test
		JSONArray androidJsonArray = new JSONArray()
		.put("value1")
		.put(new JSONArray()
			 .put("nestedValue1")
			 .put("nestedValue2"));
		JsonUtilityService.JSONArray array = androidJsonUtility.createJSONArray(androidJsonArray.toString());
		// verify
		assertEquals(2, array.length());
		assertNull(array.optJSONArray(0));
	}

	@Test
	public void testJsonArray_OptBoolean_When_Index_Invalid_Then_Return_Default() throws Exception {
		// test
		JSONArray androidJsonArray = new JSONArray()
		.put("value1")
		.put(new JSONObject()
			 .put("key1", "value1")
			 .put("key2", "value2"));
		JsonUtilityService.JSONArray array = androidJsonUtility.createJSONArray(androidJsonArray.toString());
		// verify
		assertEquals(2, array.length());
		assertTrue(array.optBoolean(0, true));
	}

	@Test
	public void testJsonArray_OptBoolean_When_Index_Valid_Then_Return_ValidValue() throws Exception {
		// test
		JSONArray androidJsonArray = new JSONArray()
		.put("true")
		.put(new JSONObject()
			 .put("key1", "value1")
			 .put("key2", "value2"));
		JsonUtilityService.JSONArray array = androidJsonUtility.createJSONArray(androidJsonArray.toString());
		// verify
		assertEquals(2, array.length());
		assertTrue(array.optBoolean(0, false));
	}

	@Test
	public void testJsonArray_OptDouble_When_Index_Invalid_Then_Return_Default() throws Exception {
		// test
		JSONArray androidJsonArray = new JSONArray()
		.put("value1")
		.put(new JSONObject()
			 .put("key1", "value1")
			 .put("key2", "value2"));
		JsonUtilityService.JSONArray array = androidJsonUtility.createJSONArray(androidJsonArray.toString());
		// verify
		assertEquals(2, array.length());
		assertEquals(1, array.optDouble(0, 1), 0);
	}

	@Test
	public void testJsonArray_OptDouble_When_Index_Valid_Then_Return_ValidValue() throws Exception {
		// test
		JSONArray androidJsonArray = new JSONArray()
		.put("value1")
		.put(new JSONObject()
			 .put("key1", "value1")
			 .put("key2", "value2"));
		JsonUtilityService.JSONArray array = androidJsonUtility.createJSONArray(androidJsonArray.toString());
		// verify
		assertEquals(2, array.length());
		assertEquals(1.3, array.optDouble(0, 1.3), 0);
	}

	@Test
	public void testJsonArray_OptLong_When_Index_Invalid_Then_Return_Default() throws Exception {
		// test
		JSONArray androidJsonArray = new JSONArray()
		.put("value1")
		.put(new JSONObject()
			 .put("key1", "value1")
			 .put("key2", "value2"));
		JsonUtilityService.JSONArray array = androidJsonUtility.createJSONArray(androidJsonArray.toString());
		// verify
		assertEquals(2, array.length());
		assertEquals(1, array.optLong(0, 1));
	}

	@Test
	public void testJsonArray_OptLong_When_Index_Valid_Then_Return_ValidValue() throws Exception {
		// test
		JSONArray androidJsonArray = new JSONArray()
		.put("4")
		.put(new JSONObject()
			 .put("key1", "value1")
			 .put("key2", "value2"));
		JsonUtilityService.JSONArray array = androidJsonUtility.createJSONArray(androidJsonArray.toString());
		// verify
		assertEquals(2, array.length());
		assertEquals(4, array.optLong(0, 1));
	}

	@Test
	public void testJsonArray_OptInt_When_Index_Invalid_Then_Return_Default() throws Exception {
		// test
		JSONArray androidJsonArray = new JSONArray()
		.put("value1")
		.put(new JSONObject()
			 .put("key1", "value1")
			 .put("key2", "value2"));
		JsonUtilityService.JSONArray array = androidJsonUtility.createJSONArray(androidJsonArray.toString());
		// verify
		assertEquals(2, array.length());
		assertEquals(1, array.optInt(0, 1));
	}

	@Test
	public void testJsonArray_OptInt_When_Index_Valid_Then_Return_ValidValue() throws Exception {
		// test
		JSONArray androidJsonArray = new JSONArray()
		.put("4")
		.put(new JSONObject()
			 .put("key1", "value1")
			 .put("key2", "value2"));
		JsonUtilityService.JSONArray array = androidJsonUtility.createJSONArray(androidJsonArray.toString());
		// verify
		assertEquals(2, array.length());
		assertEquals(4, array.optInt(0, 1));
	}

	@Test
	public void testJsonArray_Put_Get() throws Exception {
		JsonUtilityService.JSONArray jsonArray = androidJsonUtility.createJSONArray("[]")
				.put(5)
				.put(55L)
				.put(5.55d)
				.put(true)
				.put("wat");
		JsonUtilityService.JSONObject innerJsonObject = androidJsonUtility.createJSONObject("{}").put("key", "value");
		jsonArray.put(innerJsonObject);
		JsonUtilityService.JSONArray innerJsonArray = androidJsonUtility.createJSONArray("[]").put("value");
		jsonArray.put(innerJsonArray);
		assertEquals(5, (int) jsonArray.get(0));
		assertEquals(55L, (long) jsonArray.get(1));
		assertEquals(5.55d, (double) jsonArray.get(2), 0);
		assertTrue((boolean) jsonArray.get(3));
		assertEquals("wat", jsonArray.get(4));
		assertEquals(innerJsonObject.toString(), jsonArray.get(5).toString());
		assertEquals(innerJsonArray.toString(), jsonArray.get(6).toString());
	}

	@Test
	public void testJsonArray_PutNull() throws Exception {
		JsonUtilityService.JSONArray jsonArray = androidJsonUtility.createJSONArray("[]")
				.put(5)
				.put(55L)
				.put(5.55d)
				.put(true)
				.put("wat");
		JsonUtilityService.JSONObject innerJsonObject = androidJsonUtility.createJSONObject("{}").put("key", "value");
		jsonArray.put(innerJsonObject);
		JsonUtilityService.JSONArray innerJsonArray = androidJsonUtility.createJSONArray("[]").put("value");
		jsonArray.put(innerJsonArray);
		assertEquals(7, jsonArray.length());

		// Put Null
		jsonArray.put(0, (Object) null);
		jsonArray.put(0, (Object) null);
		jsonArray.put(0, (Object) null);
		jsonArray.put(0, (Object) null);
		jsonArray.put(0, (Object) null);
		jsonArray.put(0, (JSONObject) null);
		jsonArray.put(0, (JSONArray) null);
		assertEquals(7, jsonArray.length());

	}

	@Test(expected = JsonException.class)
	public void testJsonObject_Get_When_IndexOutOfBounds() throws Exception {
		JsonUtilityService.JSONArray jsonArray = androidJsonUtility.createJSONArray("[]").put("value");
		jsonArray.get(4);
	}

	@Test(expected = JsonException.class)
	public void testJsonArray_GetString_When_IndexOutOfBounds() throws Exception {
		JsonUtilityService.JSONArray jsonArray = androidJsonUtility.createJSONArray("[]").put("value");
		jsonArray.getString(4);
	}

	@Test(expected = JsonException.class)
	public void testJsonArray_GetInt_When_IndexOutOfBounds() throws Exception {
		JsonUtilityService.JSONArray jsonArray = androidJsonUtility.createJSONArray("[]").put("5");
		jsonArray.getInt(-2);
	}

	@Test(expected = JsonException.class)
	public void testJsonArray_GetLong_When_IndexOutOfBounds() throws Exception {
		JsonUtilityService.JSONArray jsonArray = androidJsonUtility.createJSONArray("[]").put("55");
		jsonArray.getLong(-1);
	}

	@Test(expected = JsonException.class)
	public void testJsonArray_GetDouble_When_IndexOutOfBounds() throws Exception {
		JsonUtilityService.JSONArray jsonArray = androidJsonUtility.createJSONArray("[]").put("55.33");
		jsonArray.getDouble(2);
	}

	@Test(expected = JsonException.class)
	public void testJsonArray_GetBoolean_When_IndexOutOfBounds() throws Exception {
		JsonUtilityService.JSONArray jsonArray = androidJsonUtility.createJSONArray("[]").put("true");
		jsonArray.getBoolean(3);
	}

	@Test(expected = JsonException.class)
	public void testJsonArray_GetJSONObject_When_IndexOutOfBounds() throws Exception {
		JsonUtilityService.JSONArray jsonArray = androidJsonUtility.createJSONArray("[]");
		JsonUtilityService.JSONObject innerJsonObject = androidJsonUtility.createJSONObject("{}").put("key", "value");
		jsonArray.put(innerJsonObject);
		jsonArray.getJSONObject(-1);
	}

	@Test(expected = JsonException.class)
	public void testJsonArray_GetJSONArray_When_IndexOutOfBounds() throws Exception {
		JsonUtilityService.JSONArray jsonArray = androidJsonUtility.createJSONArray("[]");
		JsonUtilityService.JSONArray innerJsonArray = androidJsonUtility.createJSONArray("[]").put("value");
		jsonArray.put(innerJsonArray);
		jsonArray.getJSONArray(-1);
	}

	@Test(expected = JsonException.class)
	public void testJsonArray_PutString_At_InvalidIndex() throws Exception {
		JsonUtilityService.JSONArray jsonArray = androidJsonUtility.createJSONArray("[]");
		jsonArray.put(-1, "string");
	}

	@Test(expected = JsonException.class)
	public void testJsonArray_PutInt_At_InvalidIndex() throws Exception {
		JsonUtilityService.JSONArray jsonArray = androidJsonUtility.createJSONArray("[]");
		jsonArray.put(-1, 5);
	}

	@Test(expected = JsonException.class)
	public void testJsonArray_PutLong_At_InvalidIndex() throws Exception {
		JsonUtilityService.JSONArray jsonArray = androidJsonUtility.createJSONArray("[]");
		jsonArray.put(-1, 55L);
	}

	@Test(expected = JsonException.class)
	public void testJsonArray_PutDouble_At_InvalidIndex() throws Exception {
		JsonUtilityService.JSONArray jsonArray = androidJsonUtility.createJSONArray("[]");
		jsonArray.put(-1, 5.55d);
	}

	@Test(expected = JsonException.class)
	public void testJsonArray_PutBoolean_At_InvalidIndex() throws Exception {
		JsonUtilityService.JSONArray jsonArray = androidJsonUtility.createJSONArray("[]");
		jsonArray.put(-1, true);
	}

	@Test(expected = JsonException.class)
	public void testJsonArray_PutJSONObject_At_InvalidIndex() throws Exception {
		JsonUtilityService.JSONArray jsonArray = androidJsonUtility.createJSONArray("[]");
		JsonUtilityService.JSONObject innerJsonObject = androidJsonUtility.createJSONObject("{}").put("key", "value");
		jsonArray.put(-1, innerJsonObject);
	}

	@Test(expected = JsonException.class)
	public void testJsonArray_PutJSONArray_At_InvalidIndex() throws Exception {
		JsonUtilityService.JSONArray jsonArray = androidJsonUtility.createJSONArray("[]");
		JsonUtilityService.JSONArray innerJsonArray = androidJsonUtility.createJSONArray("[]").put("value");
		jsonArray.put(-1, innerJsonArray);
	}

	@Test(expected = JsonException.class)
	public void testJsonArray_PutObject_At_InvalidIndex() throws Exception {
		Object someObject = new Object();
		JsonUtilityService.JSONArray jsonArray = androidJsonUtility.createJSONArray("[]");
		jsonArray.put(-1, someObject);
	}

	@Test
	public void testJsonObject_Get_When_Null_Value() throws Exception {
		JsonUtilityService.JSONArray jsonArray = androidJsonUtility.createJSONArray("[]").put(JSONObject.NULL);
		assertNull(jsonArray.get(0));
	}

	@Test
	public void testJsonArray_Put_At_FarIndex() throws Exception {
		JsonUtilityService.JSONArray jsonArray = androidJsonUtility.createJSONArray("[]");
		jsonArray.put(7, "string");

		assertEquals(8, jsonArray.length());
	}

	@Test
	public void testJsonArray_HashCode() throws  Exception {
		JSONArray androidJsonArray = new JSONArray()
		.put("4");
		assertNotNull(androidJsonArray.hashCode());

	}

}
