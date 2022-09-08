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

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class UrlUtilitiesTest {
	// TODO: should move performance testing to a new set of tests
	// public void urlEncodePerformance() throws Exception {
	//     long startTime = System.currentTimeMillis();

	//     for(int i = 0; i < 100000; i++) {
	//         URLUtils.urlEncode("test");
	//     }

	//     long totalTime = System.currentTimeMillis() - startTime;
	//     System.out.println("Total time = " + totalTime);
	//     System.out.println();

	//     assertNull(null);
	// }

	@Test
	public void testClassIsWellDefined() {
		try {
			TestHelper.assertUtilityClassWellDefined(UrlUtilities.class);
		} catch (Exception e) {
			fail("UrlUtilities class is not well defined, throwing exception " + e);
		}
	}

	@Test
	public void testSerializeToQueryString() {
		HashMap<String, Variant> dict = new HashMap<String, Variant>();
		dict.put("key1", Variant.fromString("val1"));
		dict.put("key2", Variant.fromString("val2"));
		dict.put("key3", Variant.fromString("val3"));
		String valueUnderTest = UrlUtilities.serializeToQueryString(dict);
		Assert.assertTrue(valueUnderTest + " should contain &key3=val3", valueUnderTest.contains("&key3=val3"));
		Assert.assertTrue(valueUnderTest + " should contain &key2=val2", valueUnderTest.contains("&key2=val2"));
		Assert.assertTrue(valueUnderTest + " should contain &key1=val1", valueUnderTest.contains("&key1=val1"));
	}

	@Test
	public void testSerializeToQueryStringNullInput() {
		HashMap<String, Variant> dict = null;
		String valueUnderTest = UrlUtilities.serializeToQueryString(dict);
		Assert.assertNull(valueUnderTest + " should be null", valueUnderTest);
	}

	@Test
	public void testSerializeToQueryStringNullKeyParameter() {
		HashMap<String, Variant> dict = new HashMap<String, Variant>();
		dict.put("key1", Variant.fromString("val1"));
		dict.put(null, Variant.fromString("val2"));
		String valueUnderTest = UrlUtilities.serializeToQueryString(dict);
		Assert.assertTrue(valueUnderTest + " should contain &key1=val1", valueUnderTest.contains("&key1=val1"));
		Assert.assertFalse(valueUnderTest + " should not contain &key2=val2", valueUnderTest.contains("&key2=val2"));
	}

	@Test
	public void testSerializeToQueryStringNullValueParameter() {
		HashMap<String, Variant> dict = new HashMap<String, Variant>();
		dict.put("key1", Variant.fromString("val1"));
		dict.put("key2", Variant.fromNull());
		String valueUnderTest = UrlUtilities.serializeToQueryString(dict);
		Assert.assertTrue(valueUnderTest + " should contain &key1=val1", valueUnderTest.contains("&key1=val1"));
		Assert.assertFalse(valueUnderTest + " should not contain &key2=val2", valueUnderTest.contains("&key2=val2"));
	}

	@Test
	public void testSerializeToQueryStringEmptyKeyParameter() {
		HashMap<String, Variant> dict = new HashMap<String, Variant>();
		dict.put("key1", Variant.fromString("val1"));
		dict.put("", Variant.fromString("val2"));
		String valueUnderTest = UrlUtilities.serializeToQueryString(dict);
		Assert.assertTrue(valueUnderTest + " should contain &key1=val1", valueUnderTest.contains("&key1=val1"));
		Assert.assertFalse(valueUnderTest + " should notcontain &key2=val2", valueUnderTest.contains("&key2=val2"));
	}

	@Test
	public void testSerializeToQueryStringEmptyValueParameter() {
		HashMap<String, Variant> dict = new HashMap<String, Variant>();
		dict.put("key1", Variant.fromString("val1"));
		dict.put("key2", Variant.fromString(""));
		String valueUnderTest = UrlUtilities.serializeToQueryString(dict);
		Assert.assertTrue(valueUnderTest + " should contain &key1=val1", valueUnderTest.contains("&key1=val1"));
		Assert.assertTrue(valueUnderTest + " should contain &key2=val2", valueUnderTest.contains("&key2="));
	}

	@Test
	public void testSerializeToQueryStringNonString() {
		HashMap<String, Variant> dict = new HashMap<String, Variant>();
		dict.put("key1", Variant.fromInteger(5));
		String valueUnderTest = UrlUtilities.serializeToQueryString(dict);
		assertEquals("&key1=5", valueUnderTest);
	}

	@Test
	public void testSerializeToQueryStringArrayList() {
		ArrayList<String> list = new ArrayList<String>();
		list.add("TestArrayList1");
		list.add("TestArrayList2");
		list.add("TestArrayList3");
		list.add("TestArrayList4");
		HashMap<String, Variant> dict = new HashMap<String, Variant>();
		dict.put("key1", Variant.fromStringList(list));
		String valueUnderTest = UrlUtilities.serializeToQueryString(dict);
		assertEquals("&key1=TestArrayList1%2CTestArrayList2%2CTestArrayList3%2CTestArrayList4", valueUnderTest);
	}

	@Test
	public void testSerializeToQueryStringArrayListNullObject() {
		ArrayList<String> list = new ArrayList<String>();
		list.add("TestArrayList1");
		list.add("TestArrayList2");
		list.add(null);
		list.add("TestArrayList4");
		HashMap<String, Variant> dict = new HashMap<String, Variant>();
		dict.put("key1", Variant.fromStringList(list));
		String valueUnderTest = UrlUtilities.serializeToQueryString(dict);
		assertEquals("&key1=TestArrayList1%2CTestArrayList2%2Cnull%2CTestArrayList4", valueUnderTest);
	}

	@Test
	public void testSerializeToQueryStringArrayListEmptyObject() {
		ArrayList<String> list = new ArrayList<String>();
		list.add("TestArrayList1");
		list.add("TestArrayList2");
		list.add("");
		list.add("TestArrayList4");
		HashMap<String, Variant> dict = new HashMap<String, Variant>();
		dict.put("key1", Variant.fromStringList(list));
		String valueUnderTest = UrlUtilities.serializeToQueryString(dict);
		assertEquals("&key1=TestArrayList1%2CTestArrayList2%2C%2CTestArrayList4", valueUnderTest);
	}

	@Test
	public void testJoin_when_validDelimiterAndTokens_happy() throws Exception {
		List<String> tokens = new ArrayList<String>();
		tokens.add("my");
		tokens.add("Test");
		tokens.add("Example");
		assertEquals("my+Test+Example", UrlUtilities.join(tokens, "+"));
	}

	@Test
	public void testJoin_when_validDelimiterAndOneToken() throws Exception {
		List<String> tokens = new ArrayList<String>();
		tokens.add("Test");
		assertEquals("Test", UrlUtilities.join(tokens, "+"));
	}

	@Test
	public void testJoin_when_validDelimiterAndEmptyTokens() throws Exception {
		assertEquals("", UrlUtilities.join(new ArrayList<String>(), "+"));
	}
}
