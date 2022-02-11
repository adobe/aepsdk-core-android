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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PlatformAssertions {

	public static void assertAdditionalDataContains(String response, String key, String value) {
		assertEquals(value, getAdditionalData(getPostBody(response)).get(key));
	}

	public static void assertAdditionalDataContains(String response, int requestIndex, String key, String value) {
		assertEquals(value, getAdditionalData(getPostBody(response, requestIndex)).get(key));
	}

	public static void assertAdditionalDataNOTContains(String response, String key, String value) {
		assertNotEquals(value, getAdditionalData(getPostBody(response)).get(key));
	}

	public static void assertAdditionalDataNOTContains(String response, int requestIndex, String key, String value) {
		assertNotEquals(value, getAdditionalData(getPostBody(response, requestIndex)).get(key));
	}

	public static void assertContextDataContains(String response, String key, String value) {
		assertEquals(value, getContextData(getPostBody(response)).get(key));
	}

	public static void assertContextDataContains(String response, int requestIndex, String key, String value) {
		assertEquals(value, getContextData(getPostBody(response, requestIndex)).get(key));
	}

	public static void assertContextDataNOTContains(String response, String key, String value) {
		assertNotEquals(value, getContextData(getPostBody(response)).get(key));
	}

	public static void assertContextDataNOTContains(String response, int requestIndex, String key, String value) {
		assertNotEquals(value, getContextData(getPostBody(response, requestIndex)).get(key));
	}

	public static void assertContextDataNotNull(String response, String key) {
		assertNotNull(getContextData(getPostBody(response)).get(key));
	}

	public static void assertContextDataNotNull(String response, int requeastIndex, String key) {
		assertNotNull(getContextData(getPostBody(response, requeastIndex)).get(key));
	}

	public static void assertCidDataEquals(String response, String key, String value) {
		assertEquals(value, getCidData(getPostBody(response)).get(key));
	}

	public static void assertCidDataEqualsE2E(String response, String key, String value) {
		assertEquals(value, getCidData(response).get(key));
	}

	public static void assertCidDataEquals(String response, int requestIndex, String key, String value) {
		assertEquals(value, getCidData(getPostBody(response, requestIndex)).get(key));
	}

	public static void assertHeaderEquals(String response, String key, String value) {
		assertHeaderEquals(response, 0, key, value);
	}

	public static void assertHeaderEquals(String response, int requestIndex, String key, String value) {
		String headers;

		try {
			headers = getHeaders(response, requestIndex).getString(key);
		} catch (JSONException e) {
			e.printStackTrace();
			headers = null;
		}

		assertEquals(value, headers);
	}

	public static void assertUrlContains(String response, String substring) {
		assertUrlContains(response, 0, substring);
	}

	public static void assertUrlContains(String response, int requestIndex, String substring) {
		String url = getURL(response, requestIndex);
		assertTrue("url <" + url + "> does not contain the substring <" + substring + ">", url.contains(substring));
	}

	public static void assertUrlNOTContains(String response, String substring) {
		assertUrlNOTContains(response, 0, substring);
	}

	public static void assertUrlNOTContains(String response, int requestIndex, String substring) {
		String url = getURL(response, requestIndex);
		assertFalse("url <" + url + "> contains the substring <" + substring + ">", url.contains(substring));
	}

	public static void assertPostBodyContains(String response, String substring) {
		assertPostBodyContains(response, 0, substring);
	}

	public static void assertPostBodyContains(String response, int requestIndex, String substring) {
		String postBody = getPostBody(response, requestIndex);
		assertTrue("postBody <" + postBody + "> does not contain the substring <" + substring + ">",
				   postBody.contains(substring));
	}

	public static void assertPostBodyNOTContains(String response, String substring) {
		assertPostBodyNOTContains(response, 0, substring);
	}

	public static void assertPostBodyNOTContains(String response, int requestIndex, String substring) {
		String postBody = getPostBody(response, requestIndex);
		assertFalse("postBody" + postBody + "> does not contain the substring <" + substring + ">",
					postBody.contains(substring));
	}

	public static void assertUrlOrBodyContains(String response, String substring) {
		String url = getURL(response);
		String postBody = getPostBody(response);
		assertTrue(url.contains(substring) || postBody.contains(substring));
	}

	public static void assertUrlOrBodyNOTContains(String response, String substring) {
		String url = getURL(response);
		String postBody = getPostBody(response);
		assertFalse(url.contains(substring) || postBody.contains(substring));
	}

	public static void assertUrlOrBodyContains(String response, int requestIndex, String substring) {
		String url = getURL(response, requestIndex);
		String postBody = getPostBody(response, requestIndex);
		assertTrue(url.contains(substring) || postBody.contains(substring));
	}

	public static void assertUrlOrBodyNOTContains(String response, int requestIndex, String substring) {
		String url = getURL(response, requestIndex);
		String postBody = getPostBody(response, requestIndex);
		assertFalse(url.contains(substring) || postBody.contains(substring));
	}

	public static void assertUrlOrBodyMatches(String response, String regex) {
		String url = getURL(response);
		String postBody = getPostBody(response);
		assertTrue(url.matches(regex) || postBody.matches(regex));
	}

	public static void assertUrlOrBodyNOTMatches(String response, String regex) {
		String url = getURL(response);
		String postBody = getPostBody(response);
		assertFalse(url.matches(regex) || postBody.matches(regex));
	}

	public static void assertUrlOrBodyMatches(String response, int requestIndex, String regex) {
		String url = getURL(response, requestIndex);
		String postBody = getPostBody(response, requestIndex);
		assertTrue(url.matches(regex) || postBody.matches(regex));
	}

	public static void assertUrlOrBodyNOTMatches(String response, int requestIndex, String regex) {
		String url = getURL(response, requestIndex);
		String postBody = getPostBody(response, requestIndex);
		assertFalse(url.matches(regex) || postBody.matches(regex));
	}

	public static void assertResponseCount(String response, int count) {
		//exit if response is null or empty
		if (response == null || "".equals(response)) {
			assertEquals(count, 0);
		}

		JSONArray responseArray = null;

		try {
			responseArray = new JSONArray(response);
		} catch (final JSONException ex) {
			ex.printStackTrace();
		}

		assertEquals(count, responseArray == null ? 0 : responseArray.length());
	}

	public static String getPostBody(String response) {
		return getPostBody(response, 0);
	}

	public static String getPostBody(String response, int requestIndex) {
		if (response == null) {
			return "";
		}

		JSONArray responseArray;

		try {
			responseArray = new JSONArray(response);
			return ((JSONObject) responseArray.get(requestIndex)).getString("body");
		} catch (final JSONException ex) {
			ex.printStackTrace();
		}

		return "";
	}

	public static String getURL(String response) {
		return getURL(response, 0);
	}

	public static String getURL(String response, int requestIndex) {
		if (response == null) {
			return "";
		}

		JSONArray responseArray;

		try {
			responseArray = new JSONArray(response);
			return ((JSONObject) responseArray.get(requestIndex)).getString("url");
		} catch (final JSONException ex) {
			ex.printStackTrace();
		}

		return "";
	}

	public static JSONObject getHeaders(String response, int requestIndex) {
		if (response == null) {
			return new JSONObject();
		}

		JSONArray responseArray;

		try {
			responseArray = new JSONArray(response);
			String headersString = ((JSONObject) responseArray.get(requestIndex)).getString("headers");
			return new JSONObject(headersString);
		} catch (final JSONException ex) {
			ex.printStackTrace();
		}

		return new JSONObject();
	}

	public static Map<String, Object> getContextData(String source) {
		return getContextData(source, "c");
	}


	protected static Map<String, Object> getContextData(String source, String tag) {
		Map<String, Object> contextData = new HashMap<String, Object>(64);
		Pattern pattern = Pattern.compile(".*(&" + tag + "\\.(.*)&\\." + tag + ").*");
		Matcher matcher = pattern.matcher(source);

		if (!matcher.matches()) {
			return contextData;
		}

		String contextDataString = matcher.group(2);

		if (contextDataString == null) {
			return contextData;
		}

		Map<String, Object> additionalData = new HashMap<String, Object>(64);
		String additionalDataString = source.replace(contextDataString, "");

		for (String param : additionalDataString.split("&")) {
			String[] kvpair = param.split("=");

			if (kvpair.length != 2) {
				additionalData.put(param, "");
			} else {
				additionalData.put(kvpair[0], kvpair[1]);
			}
		}

		List<String> keyPath = new ArrayList<String>(16);

		for (String param : contextDataString.split("&")) {
			if (param.endsWith(".") && !param.contains("=")) {
				keyPath.add(param);
			} else if (param.startsWith(".") && keyPath.size() > 0) {
				keyPath.remove(keyPath.size() - 1);
			} else {
				String[] kvpair = param.split("=");

				if (kvpair.length != 2) {
					continue;
				}

				String contextDataKey = contextDataStringPath(keyPath, kvpair[0]);

				try {
					contextData.put(contextDataKey, java.net.URLDecoder.decode(kvpair[1], "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}

		return contextData;
	}

	protected static Map<String, Object> getCidData(String source) {
		return getContextData(source, "cid");
	}

	protected static Map<String, Object> getAdditionalData(String source) {
		Pattern pattern = Pattern.compile(".*(&c\\.(.*)&\\.c).*");
		Matcher matcher = pattern.matcher(source);
		String addtionalDataString = source;

		if (matcher.matches() && matcher.group(2) != null) {
			addtionalDataString = source.replace(matcher.group(2), "");
		}

		pattern = Pattern.compile(".*(&cid\\.(.*)&\\.cid).*");
		matcher = pattern.matcher(source);

		if (matcher.matches() && matcher.group(2) != null) {
			addtionalDataString = addtionalDataString.replace(matcher.group(2), "");
		}

		Map<String, Object> additionalData = new HashMap<String, Object>(64);

		for (String param : addtionalDataString.split("&")) {
			String[] kvpair = param.split("=");

			if (kvpair.length != 2) {
				additionalData.put(param, "");
			} else {
				additionalData.put(kvpair[0], kvpair[1]);
			}
		}

		return additionalData;
	}

	protected static String contextDataStringPath(List<String> keyPath, String lastComponent) {
		StringBuilder sb = new StringBuilder();

		for (String pathComponent : keyPath) {
			sb.append(pathComponent);
		}

		sb.append(lastComponent);
		return sb.toString();
	}


}
