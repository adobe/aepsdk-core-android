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
public class AndroidJsonObjectTests {

	private AndroidJsonUtility androidJsonUtility;

	@Before
	public void setup() {
		androidJsonUtility = new AndroidJsonUtility();
	}


	// =================================================================================================================
	// String optString(String name, String defaultValue)
	// =================================================================================================================

	@Test
	public void testJsonObject_OptString_When_Key_Invalid_Then_ReturnDefault() throws Exception {
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
		String value = object.optString("nonexistingKey", "defaultValue");
		assertEquals("defaultValue", value);
	}

	@Test
	public void testJsonObject_OptString_When_Key_Valid_Then_Return_ValidValue() throws Exception {
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
		String value = object.optString("key1", "1123132");
		assertEquals("1123132", value);
	}

	@Test
	public void testJsonObject_OptString_When_NullKey() throws Exception {
		// test
		JSONObject jsonObject = new JSONObject().put("key1", "value1");
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(jsonObject.toString());

		// verify
		assertEquals("1123132", object.optString(null, "1123132"));
	}

	@Test
	public void testJsonObject_OptString_When_NullDefaultValue() throws Exception {
		// test
		JSONObject jsonObject = new JSONObject().put("key1", "value1");
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(jsonObject.toString());

		// verify
		assertNull(null, object.optString("key2", null));
	}

	@Test
	public void testJsonObject_OptString_When_NullKeyAndDefaultValue() throws Exception {
		// test
		JSONObject jsonObject = new JSONObject().put("key1", "value1");
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(jsonObject.toString());

		// verify
		assertNull(null, object.optString(null, null));
	}

	// =================================================================================================================
	// JSONObject optJSONObject(String name)
	// =================================================================================================================

	@Test
	public void testJsonObject_optJSONObject_When_KeyInvalid_Then_ReturnNull() throws Exception {
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
		assertNull(object.optJSONObject("nonexistingKey"));
	}

	@Test
	public void testJsonObject_optJSONObject_When_KeyValid_Then_ReturnValidValue() throws Exception {
		// test
		JSONObject androidJsonObject = new JSONObject();
		androidJsonObject.put("key1", "1123132");
		androidJsonObject.put("key2", new JSONObject() {
			{
				put("nested_key1", "value1");
				put("nested_key2", "value2");
			}
		});
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(androidJsonObject.toString());
		// verify
		assertEquals(2, object.length());
		assertNotNull(object.optJSONObject("key2"));
	}

	@Test
	public void testJsonObject_optJsonObject_When_KeyNull() throws Exception {
		// test
		JSONObject jsonObject = new JSONObject()
		.put("key1", "4")
		.put("key2", new JSONObject()
			 .put("key1", "value1")
			 .put("key2", "value2")
			);
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(jsonObject.toString());
		// verify
		assertEquals(2, object.length());
		assertNull(object.optJSONObject(null));
	}

	// =================================================================================================================
	// JSONArray optJSONArray(String name);
	// =================================================================================================================

	@Test
	public void testJsonObject_optJSONArray_When_KeyInvalid_Then_ReturnNull() throws Exception {
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
		assertNull(object.optJSONArray("nonexistingKey"));
	}

	@Test
	public void testJsonObject_optJSONArray_When_KeyValid_Then_ReturnValidValue() throws Exception {
		// test
		JSONObject androidJsonObject = new JSONObject();
		androidJsonObject.put("key1", "1123132");
		androidJsonObject.put("key2", new JSONArray()
							  .put("nestedValue1")
							  .put("nestedValue2")
							 );
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(androidJsonObject.toString());
		// verify
		assertEquals(2, object.length());
		assertNotNull(object.optJSONArray("key2"));
	}

	@Test
	public void testJsonObject_optJSONArray_When_KeyNull() throws Exception {
		// test
		JSONObject androidJsonObject = new JSONObject();
		androidJsonObject.put("key1", "1123132");
		androidJsonObject.put("key2", new JSONArray()
							  .put("nestedValue1")
							  .put("nestedValue2")
							 );
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(androidJsonObject.toString());
		// verify
		assertEquals(2, object.length());
		assertNull(object.optJSONArray(null));
	}


	// =================================================================================================================
	// boolean optBoolean(String name, boolean defaultValue)
	// =================================================================================================================

	@Test
	public void testJsonObject_OptBoolean_When_Key_Invalid_Then_Return_Default() throws Exception {
		// test
		JSONObject jsonObject = new JSONObject()
		.put("key1", "4")
		.put("key2", new JSONObject()
			 .put("key1", "value1")
			 .put("key2", "value2")
			);
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(jsonObject.toString());
		// verify
		assertEquals(2, object.length());
		assertTrue(object.optBoolean("nonexistingKey", true));
	}

	@Test
	public void testJsonObject_OptBoolean_When_Key_Valid_Then_Return_ValidValue() throws Exception {
		// test
		JSONObject jsonObject = new JSONObject()
		.put("key1", "true")
		.put("key2", new JSONObject()
			 .put("key1", "value1")
			 .put("key2", "value2")
			);
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(jsonObject.toString());
		// verify
		assertEquals(2, object.length());
		assertTrue(object.optBoolean("key1", false));
	}

	@Test
	public void testJsonObject_OptBoolean_When_NullKey() throws Exception {
		// test
		JSONObject jsonObject = new JSONObject().put("key1", "value1");
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(jsonObject.toString());

		// verify
		assertTrue(object.optBoolean(null, true));
	}

	// =================================================================================================================
	// double optDouble(String name, double defaultValue)
	// =================================================================================================================

	@Test
	public void testJsonObject_OptDouble_When_Key_Invalid_Then_Return_Default() throws Exception {
		// test
		JSONObject jsonObject = new JSONObject()
		.put("key1", "1123132")
		.put("key2", new JSONObject()
			 .put("key1", "value1")
			 .put("key2", "value2")
			);
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(jsonObject.toString());
		// verify
		assertEquals(2, object.length());
		assertEquals(1, object.optDouble("nonexistingKey", 1), 0);
	}

	@Test
	public void testJsonObject_OptDouble_When_Key_Valid_Then_Return_ValidValue() throws Exception {
		// test
		JSONObject jsonObject = new JSONObject()
		.put("key1", "1.3")
		.put("key2", new JSONObject()
			 .put("key1", "value1")
			 .put("key2", "value2")
			);
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(jsonObject.toString());
		// verify
		assertEquals(2, object.length());
		assertEquals(1.3, object.optDouble("key1", 1.3), 0);
	}

	@Test
	public void testJsonObject_OptDouble_When_NullKey() throws Exception {
		// test
		JSONObject jsonObject = new JSONObject().put("key1", "value1");
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(jsonObject.toString());

		// verify
		assertEquals(5.55d, (double) object.optDouble(null, 5.55d), 0);
	}

	// =================================================================================================================
	// long optLong(String name, long defaultValue)
	// =================================================================================================================

	@Test
	public void testJsonObject_OptLong_When_Key_Invalid_Then_Return_Default() throws Exception {
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
		assertEquals(1, object.optLong("nonexistingKey", 1));
	}

	@Test
	public void testJsonObject_OptLong_When_Key_Valid_Then_Return_ValidValue() throws Exception {
		// test
		JSONObject jsonObject = new JSONObject()
		.put("key1", "4")
		.put("key2", new JSONObject()
			 .put("key1", "value1")
			 .put("key2", "value2")
			);
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(jsonObject.toString());
		// verify
		assertEquals(2, object.length());
		assertEquals(4, object.optLong("key1", 1));
	}

	@Test
	public void testJsonObject_OptLong_When_NullKey() throws Exception {
		// test
		JSONObject jsonObject = new JSONObject().put("key1", "value1");
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(jsonObject.toString());

		// verify
		assertEquals(3434343L, object.optLong(null, 3434343L));
	}

	// =================================================================================================================
	// int optInt(String name, int defaultValue)
	// =================================================================================================================

	@Test
	public void testJsonObject_OptInt_When_Key_Invalid_Then_Return_Default() throws Exception {
		// test
		JSONObject jsonObject = new JSONObject()
		.put("key1", "4")
		.put("key2", new JSONObject()
			 .put("key1", "value1")
			 .put("key2", "value2")
			);
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(jsonObject.toString());
		// verify
		assertEquals(2, object.length());
		assertEquals(1, object.optInt("nonexistingKey", 1));
	}

	@Test
	public void testJsonObject_OptInt_When_Key_Valid_Then_Return_ValidValue() throws Exception {
		// test
		JSONObject jsonObject = new JSONObject()
		.put("key1", "4")
		.put("key2", new JSONObject()
			 .put("key1", "value1")
			 .put("key2", "value2")
			);
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(jsonObject.toString());
		// verify
		assertEquals(2, object.length());
		assertEquals(4, object.optInt("key1", 1));
	}

	@Test
	public void testJsonObject_OptInt_When_NullKey() throws Exception {
		// test
		JSONObject jsonObject = new JSONObject().put("key1", "value1");
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(jsonObject.toString());

		// verify
		assertEquals(34, object.optInt(null, 34));
	}


	// =================================================================================================================
	// Object opt(String name)
	// =================================================================================================================

	@Test
	public void testJsonObject_Opt_When_InvalidKey() throws Exception {
		// test
		Object someObject = new Object();
		JSONObject jsonObject = new JSONObject()
		.put("key1", someObject);
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(jsonObject.toString());
		// verify
		assertEquals(1, object.length());
		assertNull(object.opt("nonexistingKey"));
	}

	@Test
	public void testJsonObject_Opt_When_ValidKey_Then_Return_ValidValue() throws Exception {
		// test
		Object someObject = new Object();
		JSONObject jsonObject = new JSONObject()
		.put("key1", someObject);
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(jsonObject.toString());
		// verify
		assertEquals(1, object.length());
		assertNotNull(object.opt("key1"));
	}

	@Test
	public void testJsonObject_Opt_NullKey() throws Exception {
		// test
		Object someObject = new Object();
		JSONObject jsonObject = new JSONObject().put("key1", someObject);
		JsonUtilityService.JSONObject object = androidJsonUtility.createJSONObject(jsonObject.toString());

		// verify
		assertNull(object.opt(null));
	}

	// =================================================================================================================
	// put and get API's
	// =================================================================================================================
	@Test
	public void testJsonObject_Put_Get() throws Exception {
		Object someObject = new Object();
		JsonUtilityService.JSONObject jsonObject = androidJsonUtility.createJSONObject("{}")
				.put("key_int", 5)
				.put("key_long", 55L)
				.put("key_double", 5.55d)
				.put("key_boolean", true)
				.put("key_string", "wat")
				.put("key_Object", someObject);

		JsonUtilityService.JSONObject innerJsonObject = androidJsonUtility.createJSONObject("{}").put("key", "value");
		jsonObject.put("key_jsonobject", innerJsonObject);
		JsonUtilityService.JSONArray innerJsonArray = androidJsonUtility.createJSONArray("[]").put("value");
		jsonObject.put("key_jsonarray", innerJsonArray);
		assertEquals(5, (int) jsonObject.get("key_int"));
		assertEquals(55L, (long) jsonObject.get("key_long"));
		assertEquals(5.55d, (double) jsonObject.get("key_double"), 0);
		assertTrue((boolean) jsonObject.get("key_boolean"));
		assertEquals("wat", jsonObject.get("key_string"));
		assertEquals(innerJsonObject.toString(), jsonObject.get("key_jsonobject").toString());
		assertEquals(innerJsonArray.toString(), jsonObject.get("key_jsonarray").toString());
		assertEquals(someObject, jsonObject.get("key_Object"));
	}


	@Test
	public void testJsonObject_PutNull_ShouldClearKeys() throws Exception {
		Object someObject = new Object();
		JsonUtilityService.JSONObject jsonObject = androidJsonUtility.createJSONObject("{}")
				.put("key_int", 5)
				.put("key_long", 55L)
				.put("key_double", 5.55d)
				.put("key_boolean", true)
				.put("key_string", "wat")
				.put("key_Object", someObject);
		JsonUtilityService.JSONObject innerJsonObject = androidJsonUtility.createJSONObject("{}").put("key", "value");
		jsonObject.put("key_jsonobject", innerJsonObject);
		JsonUtilityService.JSONArray innerJsonArray = androidJsonUtility.createJSONArray("[]").put("value");
		jsonObject.put("key_jsonarray", innerJsonArray);

		// Remove
		jsonObject.put("key_int", (Object)null);
		jsonObject.put("key_long", (Object)null);
		jsonObject.put("key_double", (Object)null);
		jsonObject.put("key_boolean", (Object)null);
		jsonObject.put("key_string", (Object)null);
		jsonObject.put("key_Object", (Object)null);
		jsonObject.put("key_jsonobject", (JSONObject)null);
		jsonObject.put("key_jsonarray", (JSONArray)null);

		assertEquals(0, jsonObject.length());
	}

	@Test
	public void testJsonObject_Put_Get_NullJsonObject() throws Exception {
		JsonUtilityService.JSONObject jsonObject = androidJsonUtility.createJSONObject("{}");
		JsonUtilityService.JSONObject innerJsonObject = null;
		jsonObject.put("key_jsonobject", innerJsonObject);
		// verify no keys are added
		assertEquals(0, jsonObject.length());
	}

	@Test
	public void testJsonObject_Put_Get_NullJsonArray() throws Exception {
		JsonUtilityService.JSONObject jsonObject = androidJsonUtility.createJSONObject("{}");
		JsonUtilityService.JSONArray innerJsonArray = null;
		jsonObject.put("key_jsonarray", innerJsonArray);
		// verify no keys are added
		assertEquals(0, jsonObject.length());
	}

	@Test
	public void testJsonObject_Get_When_NullValue() throws Exception {
		JsonUtilityService.JSONObject jsonObject = androidJsonUtility.createJSONObject("{}");
		jsonObject.put("key_null", JSONObject.NULL);
		assertNull(jsonObject.get("key_null"));
	}

	@Test(expected = JsonException.class)
	public void testJsonObject_Get_When_NullKey() throws Exception {
		JsonUtilityService.JSONObject jsonObject = androidJsonUtility.createJSONObject("{}")
				.put("key_string", "wat");
		jsonObject.get(null);
	}

	@Test(expected = JsonException.class)
	public void testJsonObject_PutString_NullName_ThrowsJSONException() throws Exception {
		JsonUtilityService.JSONObject jsonObject = androidJsonUtility.createJSONObject("{}");
		jsonObject.put(null, "sd");
	}

	@Test(expected = JsonException.class)
	public void testJsonObject_PutInt_NullName_ThrowsJSONException() throws Exception {
		JsonUtilityService.JSONObject jsonObject = androidJsonUtility.createJSONObject("{}");
		jsonObject.put(null, 5);
	}

	@Test(expected = JsonException.class)
	public void testJsonObject_PutLong_NullName_ThrowsJSONException() throws Exception {
		JsonUtilityService.JSONObject jsonObject = androidJsonUtility.createJSONObject("{}");
		jsonObject.put(null, 55L);
	}

	@Test(expected = JsonException.class)
	public void testJsonObject_PutDouble_NullName_ThrowsJSONException() throws Exception {
		JsonUtilityService.JSONObject jsonObject = androidJsonUtility.createJSONObject("{}");
		jsonObject.put(null, 5.55d);
	}

	@Test(expected = JsonException.class)
	public void testJsonObject_PutBoolean_NullName_ThrowsJSONException() throws Exception {
		JsonUtilityService.JSONObject jsonObject = androidJsonUtility.createJSONObject("{}");
		jsonObject.put(null, true);
	}

	@Test(expected = JsonException.class)
	public void testJsonObject_PutJSONObject_NullName_ThrowsJSONException() throws Exception {
		JsonUtilityService.JSONObject jsonObject = androidJsonUtility.createJSONObject("{}");
		JsonUtilityService.JSONObject innerJsonObject = androidJsonUtility.createJSONObject("{}").put("key", "value");
		jsonObject.put(null, innerJsonObject);
	}

	@Test(expected = JsonException.class)
	public void testJsonObject_PutJSONArray_NullName_ThrowsJSONException() throws Exception {
		JsonUtilityService.JSONObject jsonObject = androidJsonUtility.createJSONObject("{}");
		JsonUtilityService.JSONArray innerJsonArray = androidJsonUtility.createJSONArray("[]").put("value");
		jsonObject.put(null, innerJsonArray);
	}

	@Test(expected = JsonException.class)
	public void testJsonObject_Object_NullName_ThrowsJSONException() throws Exception {
		Object someObject = new Object();
		JsonUtilityService.JSONObject jsonObject = androidJsonUtility.createJSONObject("{}");
		jsonObject.put(null, someObject);
	}

}
