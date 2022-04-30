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

import com.adobe.marketing.mobile.internal.utility.UrlUtilities;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ContextDataUtil {
	private static final String LOG_TAG = ContextDataUtil.class.getSimpleName();
	private static final boolean[] contextDataMask = new boolean[] {
		false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
		false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
		false, false, false, false, false, false, false, false, false, false, false, false, false, false,  true, false,
		true,  true,  true,  true,  true,  true,  true,  true,  true,  true, false, false, false, false, false, false,
		false,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,
		true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true, false, false, false, false,  true,
		false,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,
		true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true, false, false, false, false, false,
		false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
		false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
		false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
		false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
		false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
		false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
		false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
		false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false
	};
	private static final Map<String, String> contextDataKeyWhiteList = new HashMap<String, String>(256);
	private static final int MAX_LIMIT = 250;
	private static int contextDataKeyWhiteListCount = 0;

	private ContextDataUtil() {}

	/**
	 * Translates string based context data into a nested dictionary format for serializing to query string.
	 * This method contains a recursive block.
	 * @param data the data map that we want to process
	 * @return a new ContextData object containing the provided datas
	 */
	public static ContextData translateContextData(final Map<String, String> data) {
		final ContextData tempContextData = new ContextData();

		for (Map.Entry<String, Object> entry : cleanContextDataDictionary(data).entrySet()) {
			String key = entry.getKey();
			List<String> list = new ArrayList<String>();
			int pos = 0;
			int end;

			while ((end = key.indexOf('.', pos)) >= 0) {
				list.add(key.substring(pos, end));
				pos = end + 1;
			}

			list.add(key.substring(pos, key.length()));
			addValueToHashMap(entry.getValue(), tempContextData, list, 0);
		}

		return tempContextData;
	}

	/**
	 * Cleans all the keys in the map given as parameter; it will remove pairs that have a null key.
	 * @param data The map that we want to process
	 * @return a new cleaned-up map
	 */
	public static Map<String, Object> cleanContextDataDictionary(final Map<String, String> data) {
		Map<String, Object> tempContextData = new HashMap<String, Object>();

		if (data == null) {
			return tempContextData;
		}

		for (Map.Entry<String, String> entry : data.entrySet()) {
			final String cleanedKey = cleanContextDataKey(entry.getKey());

			if (cleanedKey != null) {
				tempContextData.put(cleanedKey, entry.getValue());
			}
		}

		return tempContextData;
	}

	/**
	 * Cleans the input string by removing all characters that are disallowed in a context data key;
	 * context data keys only allow [A-Za-z0-9_.], periods may not prefix/suffix/occur consecutively.
	 * V4 Note: this is one of the hottest functions in the library.  this has been optimized to create
	 * minimal memory footprint while avoiding re-cleaning "known-good" keys.
	 *
	 * @param key that we want to process
	 * @return the sanitized key as described above or null if the key was null before or after processing it is invalid
	 */
	public static String cleanContextDataKey(final String key) {
		if (key == null) {
			return null;
		}

		// check to see if we've seen this key before
		synchronized (contextDataKeyWhiteList) {
			final String preCleanedKey = contextDataKeyWhiteList.get(key);

			// we've seen and cleaned this key before, return it
			if (preCleanedKey != null) {
				return preCleanedKey;
			}
		}

		String cleanKey;

		try {
			// get our current key buffer and an output buffer of equal length
			final byte[] utf8Key = key.getBytes(StringUtils.CHARSET_UTF_8);
			final byte[] outPut = new byte[utf8Key.length];
			final byte periodChar = (byte) 0x2E;
			final int mask = 0xFF;
			byte lastByte = 0;
			int outIndex = 0;

			// iterate characters
			for (final byte curByte : utf8Key) {
				// handle consecutive periods
				if (curByte == periodChar && lastByte == periodChar) {
					continue;
				}

				// check characters against allowed mask
				if (contextDataMask[curByte & mask]) {
					// put character into output array and increment index
					outPut[outIndex++] = curByte;
					lastByte = curByte;
				}
			}

			// handle empty outputs
			if (outIndex == 0) {
				return null;
			}

			// handle starting and ending periods
			final int startIndex = outPut[0] == periodChar ? 1 : 0;
			final int endTrim = outPut[outIndex - 1] == periodChar ? 1 : 0;
			final int totalLength = outIndex - endTrim - startIndex;

			// handle edge case where user inputted nothing but periods
			if (totalLength <= 0) {
				return null;
			}

			// create cleaned string
			cleanKey = new String(outPut, startIndex, totalLength, StringUtils.CHARSET_UTF_8);
		} catch (UnsupportedEncodingException exeption) {
			Log.error(LOG_TAG, "Unable to clean context data key (%s)", exeption);
			return null;
		}

		// add the cleaned key to our whitelist
		synchronized (contextDataKeyWhiteList) {
			// check to see if we're nearing our whitelist limit
			if (contextDataKeyWhiteListCount > MAX_LIMIT) {
				// purge whitelist to avoid resizing hashmap or taking too much memory
				contextDataKeyWhiteList.clear();
				contextDataKeyWhiteListCount = 0;
			}

			// add our item
			contextDataKeyWhiteList.put(key, cleanKey);
			contextDataKeyWhiteListCount++;
		}

		return cleanKey;
	}

	/**
	 * Serializes a map to key value pairs for url string.
	 * This method is recursive to handle the nested data objects.
	 * @param parameters the query parameters that we want to serialize
	 * @param builder used for recoursivity
	 */
	public static void serializeToQueryString(final Map<String, Object> parameters, final StringBuilder builder) {
		// bail if we have nothing
		if (parameters == null) {
			return;
		}

		for (Map.Entry<String, Object> entry : parameters.entrySet()) {
			String key = UrlUtilities.urlEncode(entry.getKey());

			if (key == null) {
				continue;
			}

			Object obj = entry.getValue();

			// handle context data types
			if (obj instanceof ContextData) {
				ContextData data = (ContextData)obj;

				if (data.value != null) {
					serializeKeyValuePair(key, data.value, builder);
				}

				// recurse to handle sub-context data
				if (data.data != null && data.data.size() > 0) {
					builder.append("&");
					builder.append(key);
					builder.append(".");
					serializeToQueryString(data.data, builder);
					builder.append("&.");
					builder.append(key);
				}
			} else { // just serialize the the pair for url
				serializeKeyValuePair(key, obj, builder);
			}
		}
	}

	/**
	 * Creates a new String composed of the elements joined together separated by the given delimiter
	 * @param elements the list of elements that we want ot merge
	 * @param delimiter the delimiter character
	 * @return the string with the elements joined together
	 */
	static String join(final Iterable<?> elements, final String delimiter) {
		final StringBuilder sb = new StringBuilder();
		final Iterator iter = elements.iterator();

		while (iter.hasNext()) {
			sb.append(iter.next());

			if (!iter.hasNext()) {
				break;
			}

			sb.append(delimiter);
		}

		return sb.toString();
	}

	/**
	 * Gets the url fragment, it deserialize it, appends the given map inside the context data node if that one
	 * exists and serialize it back to the initial format. Otherwise it returns the initial url fragment
	 *
	 * @param referrerData the map that we want to append to the initial url fragment
	 * @param source       the url fragment as string
	 * @return the url fragment that has the given map merged inside the context data node
	 */
	static String appendContextData(final Map<String, String> referrerData, final String source) {
		if (StringUtils.isNullOrEmpty(source) || referrerData == null || referrerData.isEmpty()) {
			return source;
		}

		Pattern pattern = Pattern.compile(".*(&c\\.(.*)&\\.c).*");
		Matcher matcher = pattern.matcher(source);

		boolean contextDataNotMatched = false;

		try {
			contextDataNotMatched = !matcher.matches() || matcher.group(2) == null;
		} catch (IndexOutOfBoundsException exception) {
			Log.debug(LOG_TAG, "Context data matcher failed with %s", exception);
		} catch (IllegalStateException exception) {
			Log.debug(LOG_TAG, "Context data matcher failed with %s", exception);
		}

		if (contextDataNotMatched) {
			StringBuilder urlSb = new StringBuilder(source);
			Map<String, Object> contextMap = new HashMap<String, Object>();
			contextMap.put("c", translateContextData(referrerData));
			serializeToQueryString(contextMap, urlSb);
			return urlSb.toString();
		}

		try {
			String contextDataString = matcher.group(2);
			// create dictionary to hold our decoded context data kvpairs
			Map<String, String> contextData = deserializeContextDataKeyValuePairs(contextDataString);

			// the referrer data should override the existing context data
			contextData.putAll(referrerData);

			StringBuilder urlSb = new StringBuilder(source.substring(0, matcher.start(1)));
			Map<String, Object> contextMap = new HashMap<String, Object>();
			contextMap.put("c", translateContextData(contextData));
			serializeToQueryString(contextMap, urlSb);
			urlSb.append(source.substring(matcher.end(1)));
			return urlSb.toString();
		} catch (IndexOutOfBoundsException exception) {
			Log.debug(LOG_TAG, "Context data matcher failed with %s", exception);
		} catch (IllegalStateException exception) {
			Log.debug(LOG_TAG, "Context data matcher failed with %s", exception);
		}

		return source;
	}

	private static void addValueToHashMap(final Object object, final ContextData table, final List<String> subKeyArray,
										  final int index) {
		if (table == null || subKeyArray == null) {
			return;
		}

		final int arrayCount = subKeyArray.size();
		final String keyName = index < arrayCount ? subKeyArray.get(index) : null;

		if (keyName == null) {
			return;
		}

		ContextData data = new ContextData();

		if (table.containsKey(keyName)) {
			data = table.get(keyName);
		}

		if (arrayCount - 1 == index) {
			// last node in the array
			data.value = object;
			table.put(keyName, data);
		} else {
			// more nodes to go through, add a HashMap to the caller if necessary
			table.put(keyName, data);
			addValueToHashMap(object, data, subKeyArray, index + 1);
		}
	}

	/**
	 * Encodes the key/value pair and prepares it in the URL format.
	 * If the value is a List, it will create a join string with the "," delimiter before encoding, otherwise it will use
	 * toString method on other objects.
	 * @param key the string value that we want to append to the builder
	 * @param value the object value that we want to encode and append to the builder
	 * @param builder it will contain the final result
	 */
	private static void serializeKeyValuePair(final String key, final Object value, final StringBuilder builder) {
		if (key == null || value == null || value instanceof ContextData || key.length() <= 0) {
			return;
		}

		if (value instanceof String && ((String) value).length() <= 0) {
			return;
		}

		builder.append("&");
		builder.append(key);
		builder.append("=");

		if (value instanceof List) {
			builder.append(UrlUtilities.urlEncode(join((List) value, ",")));
		} else {
			builder.append(UrlUtilities.urlEncode(value.toString()));
		}
	}

	/**
	 * Splits the context data string into key value pairs parameters and returns them as a map
	 * @param contextDataString the context data url fragment that we want to deserialize
	 * @return context data as map
	 */
	public static Map<String, String> deserializeContextDataKeyValuePairs(final String contextDataString) {
		final int mapCapacity = 64;
		final int listCapacity = 16;
		Map<String, String> contextData = new HashMap<String, String>(mapCapacity);
		List<String> keyPath = new ArrayList<String>(listCapacity);

		for (String param : contextDataString.split("&")) {
			if (param.endsWith(".") && !param.contains("=")) {
				keyPath.add(param);
			} else if (param.startsWith(".")) {
				if (!keyPath.isEmpty()) {
					keyPath.remove(keyPath.size() - 1);
				}
			} else {
				String[] kvpair = param.split("=");

				if (kvpair.length != 2) {
					continue;
				}

				String contextDataKey = contextDataStringPath(keyPath, kvpair[0]);

				try {
					contextData.put(contextDataKey, java.net.URLDecoder.decode(kvpair[1], StringUtils.CHARSET_UTF_8));
				} catch (UnsupportedEncodingException e) {
					Log.warning(LOG_TAG, "Appending the context data information failed with %s", e);
				}
			}
		}

		return contextData;
	}

	private static String contextDataStringPath(final List<String> keyPath, final String lastComponent) {
		StringBuilder sb = new StringBuilder();

		for (String pathComponent : keyPath) {
			sb.append(pathComponent);
		}

		sb.append(lastComponent);
		return sb.toString();

	}
}


