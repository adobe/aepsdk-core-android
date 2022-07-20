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

import com.adobe.marketing.mobile.internal.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Signal template. Each signal consequence as received in Rules Response event will be converted to a signal template.
 * This class also generates a signal hit object which can be saved int database.
 */
class SignalTemplate {

	private static final String LOGTAG = SignalTemplate.class.getSimpleName();
	private static final String ADB_TEMPLATE_CALLBACK_URL = "templateurl";
	private static final String ADB_TEMPLATE_CALLBACK_BODY = "templatebody";
	private static final String ADB_TEMPLATE_CALLBACK_CONTENT_TYPE = "contenttype";
	private static final String ADB_TEMPLATE_CALLBACK_TIMEOUT = "timeout";
	private static final int ADB_TEMPLATE_TIMEOUT_DEFAULT = 2;

	private String signalId;
	private String urlTemplate;
	private String bodyTemplate;
	private String contentType;
	private int timeout;

	/**
	 * create a new signal hit.
	 * @return a new signal hit object
	 */
	// TODO refactor to use public hit queue
	/* SignalHit getSignalHit() {
		SignalHit signalHit = new SignalHit();
		signalHit.url = this.urlTemplate;
		signalHit.body = this.bodyTemplate;
		signalHit.contentType = this.contentType;
		signalHit.timeout = this.timeout;

		return signalHit;
	} */

	/**
	 * return the id of the signal template
	 * @return the id of the signal template
	 */
	String getSignalTemplateId() {
		return this.signalId;
	}

	/**
	 * A static method to create the signal template from consequence object which is a Map of String,Object.
	 * @param signalConsequence the signal detail String,String Map
	 * @return the new SignalTemplate object converted from input consequence map.
	 */
	static SignalTemplate createSignalTemplateFromConsequence(final Map<String, Variant> signalConsequence) {

		if (signalConsequence == null || signalConsequence.isEmpty()) {
			return null;
		}

		// id
		final String consequenceId = Variant.optVariantFromMap(signalConsequence,
									 SignalConstants.EventDataKeys.RuleEngine.RULES_RESPONSE_CONSEQUENCE_KEY_ID).optString(null);

		if (StringUtils.isNullOrEmpty(consequenceId)) {
			Log.debug(LOGTAG, "Triggered rule does not have ID. Return.");
			return null;
		}

		//detail
		Map<String, Variant> consequenceDetail = Variant.optVariantFromMap(signalConsequence,
				SignalConstants.EventDataKeys.RuleEngine.RULES_RESPONSE_CONSEQUENCE_KEY_DETAIL).optVariantMap(null);

		if (consequenceDetail == null || consequenceDetail.isEmpty()) {
			Log.debug(LOGTAG, "No detail found for the consequence with id %s", consequenceId);
			return null;
		}

		final String signalTemplateUrl = Variant.optVariantFromMap(consequenceDetail,
										 ADB_TEMPLATE_CALLBACK_URL).optString(null);

		// Per requirements, if this is a pii signal, url must be https.
		String consequenceType = Variant.optVariantFromMap(signalConsequence,
								 SignalConstants.EventDataKeys.RuleEngine.RULES_RESPONSE_CONSEQUENCE_KEY_TYPE).optString(null);

		if (StringUtils.isNullOrEmpty(signalTemplateUrl) || !isValidTemplateUrl(signalTemplateUrl, consequenceType)) {
			Log.warning(LOGTAG, "Unable to create signal template, \"templateUrl\" is invalid \n");
			return null;
		}

		// all good to go!
		final SignalTemplate signalTemplate = new SignalTemplate();

		signalTemplate.signalId = consequenceId;
		signalTemplate.urlTemplate = signalTemplateUrl;
		signalTemplate.timeout = Variant.optVariantFromMap(consequenceDetail,
								 ADB_TEMPLATE_CALLBACK_TIMEOUT).optInteger(ADB_TEMPLATE_TIMEOUT_DEFAULT);
		signalTemplate.bodyTemplate = Variant.optVariantFromMap(consequenceDetail, ADB_TEMPLATE_CALLBACK_BODY).optString("");

		if (!StringUtils.isNullOrEmpty(signalTemplate.bodyTemplate)) {
			signalTemplate.contentType = Variant.optVariantFromMap(consequenceDetail,
										 ADB_TEMPLATE_CALLBACK_CONTENT_TYPE).optString("");
		}

		return signalTemplate;
	}

	/**
	 * A static method to evaluate if the template url is a valid url for the given consequence type.
	 *
	 * @param url The URL to check for
	 * @param type The {@link SignalConstants.EventDataKeys.RuleEngine#RULES_RESPONSE_CONSEQUENCE_KEY_TYPE} from the {@code RulesEngine} event
	 * @return false if the input url is invalid OR if it is a non HTTPS url for pii type consequence, true otherwise.
	 *
	 */
	static boolean isValidTemplateUrl(final String url, final String type) {

		try {
			URL templateUrl = new URL(url);

			if (SignalConstants.EventDataKeys.Signal.RULES_RESPONSE_CONSEQUENCE_TYPE_PII.equals(type)
					&& !("https".equalsIgnoreCase(templateUrl.getProtocol()))) {
				return false;
			}
		} catch (MalformedURLException e) {
			return false;
		}

		return true;
	}
}
