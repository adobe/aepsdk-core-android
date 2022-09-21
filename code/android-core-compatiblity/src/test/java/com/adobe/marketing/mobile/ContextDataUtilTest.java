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

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.adobe.marketing.mobile.internal.util.ContextDataUtil.cleanContextDataKey;
import static org.junit.Assert.*;

import com.adobe.marketing.mobile.internal.util.ContextData;
import com.adobe.marketing.mobile.internal.util.ContextDataUtil;

public class ContextDataUtilTest {
	private Map<String, String> testData = new HashMap<String, String>();
	private Map<String, String> expectedResult = new HashMap<String, String>();

	@Before
	public void setup() {
		testData.put("key1", "val1");
		testData.put("key2A", "val2A");
		testData.put("key3.A", "val3.A");
		testData.put("key4...B", "val4...B");
		testData.put("key5!", "val!");
		testData.put("key6@", "val@");
		testData.put("key7#", "val#");
		testData.put("key8$", "val$");
		testData.put("key9%", "val%");
		expectedResult.put("key1", "val1");
		expectedResult.put("key2A", "val2A");
		expectedResult.put("key3.A", "val3.A"); // this one should have an inner A key
		expectedResult.put("key4.B", "val4...B"); // this one should have an inner B key
		expectedResult.put("key5", "val!");
		expectedResult.put("key6", "val@");
		expectedResult.put("key7", "val#");
		expectedResult.put("key8", "val$");
		expectedResult.put("key9", "val%");
	}

	@Test
	public void testTranslateContextData() throws Exception {
		ContextData result = ContextDataUtil.translateContextData(testData);
		assertEquals(expectedResult.size(), result.size());
		ContextData cDataObj;
		assertEquals(expectedResult.get("key1"), result.get("key1").value);
		assertEquals(expectedResult.get("key2A"), result.get("key2A").value);
		cDataObj = result.get("key3");
		Map<String, Object> cDataSubObj = cDataObj.data;
		assertTrue(cDataSubObj.containsKey("A"));
		ContextData cDataValObj = (ContextData) cDataSubObj.get("A");
		assertEquals(expectedResult.get("key3.A"), cDataValObj.value);
		cDataObj = result.get("key4");
		cDataSubObj = cDataObj.data;
		assertTrue(cDataSubObj.containsKey("B"));
		cDataValObj = (ContextData) cDataSubObj.get("B");
		assertEquals(expectedResult.get("key4.B"), cDataValObj.value);
		assertEquals(expectedResult.get("key5"), result.get("key5").value);
		assertEquals(expectedResult.get("key6"), result.get("key6").value);
		assertEquals(expectedResult.get("key7"), result.get("key7").value);
		assertEquals(expectedResult.get("key8"), result.get("key8").value);
		assertEquals(expectedResult.get("key9"), result.get("key9").value);
	}

	@Test
	public void testCleanContextDataDictionary() throws Exception {
		Map<String, Object> result = ContextDataUtil.cleanContextDataDictionary(testData);
		assertEquals(expectedResult.size(), result.size());
		assertEquals(expectedResult.get("key1"), result.get("key1"));
		assertEquals(expectedResult.get("key2"), result.get("key2"));
		assertEquals(expectedResult.get("key3.A"), result.get("key3.A"));
		assertEquals(expectedResult.get("key4.B"), result.get("key4.B"));
		assertEquals(expectedResult.get("key5"), result.get("key5"));
		assertEquals(expectedResult.get("key6"), result.get("key6"));
		assertEquals(expectedResult.get("key7"), result.get("key7"));
		assertEquals(expectedResult.get("key8"), result.get("key8"));
		assertEquals(expectedResult.get("key9"), result.get("key9"));
	}

	@Test
	public void testSerializeToQueryString_When_DictionaryIsNull() throws Exception {
		StringBuilder test = new StringBuilder();
		ContextDataUtil.serializeToQueryString(null, test);
		assertNotNull(test);
		assertTrue(test.toString().isEmpty());
	}

	@Test
	public void testSerializeToQueryString_When_DictionaryWithNullKey() throws Exception {
		HashMap<String, Object> dict = new HashMap<String, Object>();
		dict.put(null, "val1");
		dict.put("key2", "val2");
		dict.put("key3", "val3");
		StringBuilder test = new StringBuilder();
		ContextDataUtil.serializeToQueryString(dict, test);
		String result = test.toString();
		assertFalse(result.contains("valq"));
		assertTrue(result.contains("&key2=val2"));
		assertTrue(result.contains("&key3=val3"));
	}

	@Test
	public void testSerializeToQueryString_When_ValuesAreString() throws Exception {
		HashMap<String, Object> dict = new HashMap<String, Object>();
		dict.put("key1", "val1");
		dict.put("key2", "val2");
		dict.put("key3", "val3");
		StringBuilder test = new StringBuilder();
		ContextDataUtil.serializeToQueryString(dict, test);
		String result = test.toString();
		assertTrue(result.contains("&key3=val3"));
		assertTrue(result.contains("&key2=val2"));
		assertTrue(result.contains("&key1=val1"));
	}

	@Test
	public void testSerializeToQueryString_When_ValueNonString() throws Exception {
		Map<String, Object> dict = new HashMap<String, Object>();
		dict.put("key1", 5);
		StringBuilder test = new StringBuilder();
		ContextDataUtil.serializeToQueryString(dict, test);
		assertEquals("&key1=5", test.toString());
	}

	@Test
	public void testSerializeToQueryString_When_ValueIsContextDataInstance() throws Exception {
		ContextData data2 = new ContextData();
		data2.value = "val2";
		ContextData data = new ContextData();
		data.value = "val1";
		data.put("subkey1", data2);
		HashMap<String, Object> dict = new HashMap<String, Object>();
		dict.put("key1", data);
		StringBuilder result = new StringBuilder();
		ContextDataUtil.serializeToQueryString(dict, result);
		assertEquals("Context data objects did not serialize properly", "&key1=val1&key1.&subkey1=val2&.key1",
					 result.toString());
	}

	@Test
	public void testSerializeToQueryString_When_ValueIsArrayList() throws Exception {
		ArrayList<String> list = new ArrayList<String>();
		list.add("TestArrayList1");
		list.add("TestArrayList2");
		list.add("TestArrayList3");
		list.add("TestArrayList4");
		Map<String, Object> dict = new HashMap<String, Object>();
		dict.put("key1", list);
		StringBuilder result = new StringBuilder();
		ContextDataUtil.serializeToQueryString(dict, result);
		assertEquals("&key1=TestArrayList1%2CTestArrayList2%2CTestArrayList3%2CTestArrayList4", result.toString());
	}

	@Test
	public void testJoin() throws Exception {
		List<String> list = new ArrayList<String>();
		list.add("TestArrayList1");
		list.add("TestArrayList2");
		list.add("TestArrayList3");
		list.add("TestArrayList4");
		assertEquals("TestArrayList1,TestArrayList2,TestArrayList3,TestArrayList4", ContextDataUtil.join(list, ","));
	}

	@Test
	public void testCleanContextDataKey_AllowsUnderscore() throws Exception {
		assertEquals("__key__", cleanContextDataKey("__key__"));
	}

	@Test
	public void testCleanContextDataKey_RemovesSpecialCharacters() throws Exception {
		String[] specialCharacters = new String[] {"!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "-", "+", "=", "{", "}",
				"[", "]", "|", "\\", ":", ";", "\"", "'", "<", ">", ",", "/", "?", "~", "`", " "
												  };
		String expectedKey = "key";

		for (String character : specialCharacters) {
			assertEquals("Assertion failed for [key" + character + "]", expectedKey, cleanContextDataKey("key" + character));
		}
	}

	@Test
	public void testCleanContextData_KeyMultiPeriod() throws Exception {
		String expectedKey = "key.key";
		assertEquals(expectedKey, cleanContextDataKey("key.key"));
		assertEquals(expectedKey, cleanContextDataKey("key..key"));
		assertEquals(expectedKey, cleanContextDataKey("key...key"));
		assertEquals(expectedKey, cleanContextDataKey("key....key"));
	}

	@Test
	public void testCleanContextDataKey_PeriodBeginningEnd() throws Exception {
		String expectedKey = "key.key";
		assertEquals(expectedKey, cleanContextDataKey(".key.key."));
		assertEquals(expectedKey, cleanContextDataKey("..key..key.."));
		assertEquals(expectedKey, cleanContextDataKey("...key...key..."));
		assertEquals(expectedKey, cleanContextDataKey("....key....key...."));
	}

	@Test
	public void testCleanContextData_KeyUnicode() throws Exception {
		assertEquals("test", cleanContextDataKey("test网页"));
	}

	@Test
	public void testCleanContextDataKeyReturnsNull_When_NullKey() throws Exception {
		assertNull(cleanContextDataKey(null));
	}

	@Test
	public void testCleanContextDataKeyReturnsNull_When_EmptyString() throws Exception {
		assertNull(cleanContextDataKey(""));
	}

	@Test
	public void testCleanContextDataKeyReturnsNull_When_KeyHasOnlyPeriods() throws Exception {
		assertNull(cleanContextDataKey("......."));
	}

	@Test
	public void testCleanContextDataKeyReturnsNull_When_OnlyDisallowedCharacters() throws Exception {
		assertNull(cleanContextDataKey("???????&!@#!@#*&(**^^@#(@#()$)"));
	}

	@Test
	public void testAppendContextData_When_EmptySource() throws Exception {
		Map<String, String> data = new HashMap<String, String>() {
			{
				put("new-key", "value");
			}
		};
		assertEquals("", ContextDataUtil.appendContextData(data, ""));
	}

	@Test
	public void testAppendContextData_When_NoContextDataInSource() throws Exception {
		Map<String, String> data = new HashMap<String, String>() {
			{
				put("new-key", "value");
			}
		};
		assertEquals("abcde&c.&newkey=value&.c", ContextDataUtil.appendContextData(data, "abcde"));
	}

	@Test
	public void testAppendContextData_When_NullSource() throws Exception {
		Map<String, String> data = new HashMap<String, String>() {
			{
				put("new-key", "value");
			}
		};

		assertNull(ContextDataUtil.appendContextData(data, null));
	}

	@Test
	public void testAppendContextData_When_NullReferrerData() throws Exception {
		assertEquals("&c.&newkey=value&.c", ContextDataUtil.appendContextData(null, "&c.&newkey=value&.c"));
	}

	@Test
	public void testAppendContextData_When_ContextDataOnePair() throws Exception {
		Map<String, String> data = new HashMap<String, String>() {
			{
				put("key", "value");
			}
		};
		assertEquals("&c.&key=value&.c", ContextDataUtil.appendContextData(data, "&c.&.c"));
	}
	@Test
	public void testAppendContextData_When_ContextDataTwoPair() throws Exception {
		Map<String, String> data = new HashMap<String, String>() {
			{
				put("key", "value");
				put("key1", "value1");
			}
		};
		String result = ContextDataUtil.appendContextData(data, "&c.&.c");

		assertTrue(contextDataInCorrectSequence(result, "key=value", "&c.", "&.c"));
		assertTrue(contextDataInCorrectSequence(result, "key1=value1", "&c.", "&.c"));
	}
	@Test
	public void testAppendContextData_When_ContextDataWithNestedKeyName() throws Exception {
		Map<String, String> data = new HashMap<String, String>() {
			{
				put("key", "value");
				put("key.nest", "value1");
			}
		};

		String result = ContextDataUtil.appendContextData(data, "&c.&.c");

		assertTrue(contextDataInCorrectSequence(result, "key=value", "&c.", "&.c"));
		assertTrue(contextDataInCorrectSequence(result, "nest=value1", "&key.", "&.key"));
	}

	@Test
	public void testAppendContextData_When_NestedKeyNameOverrideOldValue() throws Exception {
		Map<String, String> data = new HashMap<String, String>() {
			{
				put("key", "new-value");
				put("key.nest", "new-value1");
			}
		};

		String result = ContextDataUtil.appendContextData(data, "&c.&key=value&key.&nest=value1&.key&.c");

		assertTrue(contextDataInCorrectSequence(result, "key=new-value", "&c.", "&.c"));
		assertTrue(contextDataInCorrectSequence(result, "nest=new-value1", "&key.", "&.key"));
	}

	@Test
	public void testAppendContextData_When_NestedKeyNameAppendToExistingLevel() throws Exception {
		Map<String, String> data = new HashMap<String, String>() {
			{
				put("key.new", "value");
				put("key1.new", "value");
			}
		};

		String result = ContextDataUtil.appendContextData(data,
						"&c.&key=value&key.&nest=value1&.key&key1.&nest=value1&.key1&.c");

		assertTrue(contextDataInCorrectSequence(result, "new=value", "&key1.", "&.key1"));
		assertTrue(contextDataInCorrectSequence(result, "nest=value1", "&key1.", "&.key1"));
		assertTrue(contextDataInCorrectSequence(result, "key=value", "&c.", "&.c"));
		assertTrue(contextDataInCorrectSequence(result, "new=value", "&key.", "&.key"));
		assertTrue(contextDataInCorrectSequence(result, "nest=value1", "&key.", "&.key"));
	}

	@Test
	public void testAppendContextData_When_NestedKeyNameAppendToExistingLevel_4Level() throws Exception {
		Map<String, String> data = new HashMap<String, String>() {
			{
				put("level1.level2.level3.level4.new", "new");
				put("key1.new", "value");
				put("key.new", "value");
			}
		};

		String result = ContextDataUtil.appendContextData(data,
						"&c.&key=value&key.&nest=value1&.key&key1.&nest=value1&.key1&level1.&level2.&level3.&level4.&old=old&.level4&.level3&.level2&.level1&.c");

		assertTrue(contextDataInCorrectSequence(result, "&level2.", "&level1.", "&.level1"));
		assertTrue(contextDataInCorrectSequence(result, "&level3.", "&level2.", "&.level2"));
		assertTrue(contextDataInCorrectSequence(result, "&level4.", "&level3.", "&.level3"));
		assertTrue(contextDataInCorrectSequence(result, "old=old", "&level4.", "&.level4"));
		assertTrue(contextDataInCorrectSequence(result, "new=new", "&level4.", "&.level4"));
	}

	@Test
	public void testAppendContextData_When_ContextDataWithUTF8() throws Exception {
		Map<String, String> data = new HashMap<String, String>() {
			{
				put("level1.level2.level3.level4.new", "中文");
				put("key1.new", "value");
				put("key.new", "value");
			}
		};

		String result = ContextDataUtil.appendContextData(data,
						"&c.&key=value&key.&nest=value1&.key&key1.&nest=value1&.key1&level1.&level2.&level3.&level4.&old=old&.level4&.level3&.level2&.level1&.c");

		assertTrue(contextDataInCorrectSequence(result, "new=%E4%B8%AD%E6%96%87", "&level4.", "&.level4"));
	}

	@Test
	public void testAppendContextData_When_ContextDataUTF8_And_SourceContainsUTF8() throws Exception {
		Map<String, String> data = new HashMap<String, String>() {
			{
				put("level1.level2.level3.level4.new", "中文");
				put("key1.new", "value");
				put("key.new", "value");
			}
		};

		String result = ContextDataUtil.appendContextData(data,
						"&c.&key=value&key.&nest=value1&.key&key1.&nest=%E4%B8%AD%E6%96%87&.key1&level1.&level2.&level3.&level4.&old=old&.level4&.level3&.level2&.level1&.c");

		assertTrue(contextDataInCorrectSequence(result, "new=%E4%B8%AD%E6%96%87", "&level4.", "&.level4"));
		assertTrue(contextDataInCorrectSequence(result, "nest=%E4%B8%AD%E6%96%87", "&key1.", "&.key1"));

	}

	@Test
	public void testAppendContextData_When_SourceIsARealHit() throws Exception {
		Map<String, String> data = new HashMap<String, String>() {
			{
				put("key1.new", "value");
				put("key.new", "value");
			}
		};

		String result = ContextDataUtil.appendContextData(data,
						"ndh=1&pe=lnk_o&pev2=ADBINTERNAL%3ALifecycle&pageName=My%20Application%201.0%20%281%29&t=00%2F00%2F0000%2000%3A00%3A00%200%20360&ts=1432159549&c.&a.&DeviceName=SAMSUNG-SGH-I337&Resolution=1080x1920&OSVersion=Android%204.3&CarrierName=&internalaction=Lifecycle&AppID=My%20Application%201.0%20%281%29&Launches=1&InstallEvent=InstallEvent&DayOfWeek=4&InstallDate=5%2F20%2F2015&LaunchEvent=LaunchEvent&DailyEngUserEvent=DailyEngUserEvent&RunMode=Application&HourOfDay=16&MonthlyEngUserEvent=MonthlyEngUserEvent&.a&.c&mid=45872199741202307594993613744306256830&ce=UTF-8");

		assertTrue(contextDataInCorrectSequence(result, "new=value", "&key.", "&.key"));
		assertTrue(contextDataInCorrectSequence(result, "new=value", "&key1.", "&.key1"));
	}

	private boolean contextDataInCorrectSequence(String source, String target, String start, String end) {
		int startIndex = source.indexOf(start);
		int endIndex = source.indexOf(end, startIndex);
		int targetIndex = source.indexOf(target, startIndex);

		return targetIndex >= 0 && targetIndex <= endIndex;
	}
}
