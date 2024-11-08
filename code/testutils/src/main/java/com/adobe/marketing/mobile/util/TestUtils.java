/*
  Copyright 2021 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.util;

import static com.adobe.marketing.mobile.util.TestConstants.LOG_TAG;

import com.adobe.marketing.mobile.services.Log;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONObject;

public class TestUtils {

	private static final String LOG_SOURCE = "TestUtils";

	private TestUtils() {}

	/**
	 * Serialize the given {@code map} to a JSON Object, then flattens to {@code Map<String, String>}.
	 * For example, a JSON such as "{xdm: {stitchId: myID, eventType: myType}}" is flattened
	 * to two map elements "xdm.stitchId" = "myID" and "xdm.eventType" = "myType".
	 * @param map map with JSON structure to flatten
	 * @return new map with flattened structure
	 */
	public static Map<String, String> flattenMap(final Map<String, Object> map) {
		if (map == null || map.isEmpty()) {
			return Collections.emptyMap();
		}

		try {
			JSONObject jsonObject = new JSONObject(map);
			Map<String, String> payloadMap = new HashMap<>();
			addKeys("", new ObjectMapper().readTree(jsonObject.toString()), payloadMap);
			return payloadMap;
		} catch (IOException e) {
			Log.error(LOG_TAG, LOG_SOURCE, "Failed to parse JSON object to tree structure.");
		}

		return Collections.emptyMap();
	}

	/**
	 * Deserialize the given {@code bytes} to a flattened {@code Map<String, String>}.
	 * For example, a JSON such as "{xdm: {stitchId: myID, eventType: myType}}" is flattened
	 * to two map elements "xdm.stitchId" = "myID" and "xdm.eventType" = "myType".
	 * The given {@code bytes} must be a serialized JSON Object.
	 * @param bytes serialized JSON Object string
	 * @return new map with flattned structure
	 */
	public static Map<String, String> flattenBytes(final byte[] bytes) {
		if (bytes == null || bytes.length == 0) {
			return Collections.emptyMap();
		}

		try {
			Map<String, String> payloadMap = new HashMap<>();
			TestUtils.addKeys("", new ObjectMapper().readTree(bytes), payloadMap);
			return payloadMap;
		} catch (IOException e) {
			Log.error(LOG_TAG, LOG_SOURCE, "Failed to parse JSON payload to tree structure.");
			return Collections.emptyMap();
		}
	}

	/**
	 * Deserialize {@code JsonNode} and flatten to provided {@code map}.
	 * For example, a JSON such as "{xdm: {stitchId: myID, eventType: myType}}" is flattened
	 * to two map elements "xdm.stitchId" = "myID" and "xdm.eventType" = "myType".
	 *
	 * Method is called recursively. To use, call with an empty path such as
	 * {@code addKeys("", new ObjectMapper().readTree(JsonNodeAsString), map);}
	 *
	 * @param currentPath the path in {@code JsonNode} to process
	 * @param jsonNode {@link JsonNode} to deserialize
	 * @param map {@code Map<String, String>} instance to store flattened JSON result
	 *
	 * @see <a href="https://stackoverflow.com/a/24150263">Stack Overflow post</a>
	 */
	private static void addKeys(String currentPath, JsonNode jsonNode, Map<String, String> map) {
		if (jsonNode.isObject()) {
			ObjectNode objectNode = (ObjectNode) jsonNode;
			Iterator<Map.Entry<String, JsonNode>> iter = objectNode.fields();
			String pathPrefix = currentPath.isEmpty() ? "" : currentPath + ".";

			while (iter.hasNext()) {
				Map.Entry<String, JsonNode> entry = iter.next();
				addKeys(pathPrefix + entry.getKey(), entry.getValue(), map);
			}
		} else if (jsonNode.isArray()) {
			ArrayNode arrayNode = (ArrayNode) jsonNode;

			for (int i = 0; i < arrayNode.size(); i++) {
				addKeys(currentPath + "[" + i + "]", arrayNode.get(i), map);
			}
		} else if (jsonNode.isValueNode()) {
			ValueNode valueNode = (ValueNode) jsonNode;
			map.put(currentPath, valueNode.asText());
		}
	}
}
