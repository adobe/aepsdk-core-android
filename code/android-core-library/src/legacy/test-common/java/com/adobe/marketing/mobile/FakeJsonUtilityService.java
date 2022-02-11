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

import org.json.JSONException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class FakeJsonUtilityService implements JsonUtilityService {

	@Override
	public JSONObject createJSONObject(String json) {
		if (json == null) {
			return null;
		}

		try {
			org.json.JSONObject jsonObject = new org.json.JSONObject(json);
			return new FakeJSONObject(jsonObject);
		} catch (JSONException ex) {
			return null;
		}
	}

	@Override
	public JSONObject createJSONObject(Map map) {
		if (map == null) {
			return null;
		}

		return new FakeJSONObject(new org.json.JSONObject(map));
	}

	@Override
	public JSONArray createJSONArray(String json) {
		try {
			org.json.JSONArray jsonArray = new org.json.JSONArray(json);
			return new FakeJSONArray(jsonArray);
		} catch (JSONException ex) {
			return null;
		}
	}

	@Override
	public Map<String, String> mapFromJsonObject(JSONObject jsonData) {
		if (jsonData == null) {
			return null;
		}

		@SuppressWarnings("unchecked")
		Iterator<String> keyItr = jsonData.keys();
		Map<String, String> map = new HashMap<String, String>();

		while (keyItr.hasNext()) {
			String name = keyItr.next();

			try {
				map.put(name, String.valueOf(jsonData.get(name)));
			} catch (JsonException e) {
				//todo: log error
			}
		}

		return map;
	}
}
