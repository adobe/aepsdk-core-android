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

import com.adobe.marketing.mobile.JsonUtilityService.JSONObject;

import java.util.HashMap;
import java.util.Map;

class RuleConsequence {
	private static final String LOG_TAG = RuleConsequence.class.getSimpleName();

	private String id;
	private String consequenceType;
	private Map<String, Variant> detail;

	/**
	 * Parse the consequence {@code jsonobject} into a {@code RuleConsequence} object
	 *
	 * @param jsonObject The {@link JSONObject} rule consequence
	 * @param jsonUtilityService {@link JsonUtilityService} instance
	 * @return a {@link RuleConsequence} instance. null if required fields are missing from the {@code jsonObject}
	 */
	static RuleConsequence consequenceFromJson(final JSONObject jsonObject, final JsonUtilityService jsonUtilityService) {

		if (jsonObject == null || jsonObject.length() == 0) {
			return null;
		}

		final RuleConsequence consequence = new RuleConsequence();

		consequence.id = jsonObject.optString(ConfigurationConstants.EventDataKeys.RuleEngine.CONSEQUENCE_JSON_ID, null);

		if (StringUtils.isNullOrEmpty(consequence.id)) {
			Log.warning(LOG_TAG, "Unable to find field \"id\" in rules consequence.  This a required field.");
			return null;
		}

		consequence.consequenceType = jsonObject.optString(
										  ConfigurationConstants.EventDataKeys.RuleEngine.CONSEQUENCE_JSON_TYPE, null);

		if (StringUtils.isNullOrEmpty(consequence.consequenceType)) {
			Log.warning(LOG_TAG, "Unable to find field \"type\" in rules consequence.  This a required field.");
			return null;
		}

		final JSONObject detailJsonObject = jsonObject.optJSONObject(
												ConfigurationConstants.EventDataKeys.RuleEngine.CONSEQUENCE_JSON_DETAIL);

		if (detailJsonObject == null || detailJsonObject.length() == 0) {
			Log.warning(LOG_TAG, "Unable to find field \"detail\" in rules consequence.  This a required field.");
			return null;
		}

		try {
			consequence.detail = Variant.fromTypedObject(detailJsonObject,
								 new JsonObjectVariantSerializer(jsonUtilityService)).getVariantMap();
		} catch (VariantException ex) {
			// shouldn't ever happen, but just in case
			Log.warning(LOG_TAG, "Unable to convert detail json to a variant.");
			return null;
		}


		return consequence;
	}

	EventData generateEventData() {
		final EventData eventData = new EventData();

		HashMap<String, Variant> consequenceMap = new HashMap<String, Variant>();
		consequenceMap.put(ConfigurationConstants.EventDataKeys.RuleEngine.CONSEQUENCE_JSON_ID, Variant.fromString(id));
		consequenceMap.put(ConfigurationConstants.EventDataKeys.RuleEngine.CONSEQUENCE_JSON_TYPE,
						   Variant.fromString(consequenceType));
		consequenceMap.put(ConfigurationConstants.EventDataKeys.RuleEngine.CONSEQUENCE_JSON_DETAIL,
						   Variant.fromVariantMap(detail));

		eventData.putVariantMap(ConfigurationConstants.EventDataKeys.RuleEngine.CONSEQUENCE_TRIGGERED, consequenceMap);

		return eventData;
	}

}
