/* **************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2018 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 *
 * *************************************************************************/

package com.adobe.marketing.mobile;

class SignalConstants {
	private SignalConstants() {}

	static final class EventDataKeys {

		private EventDataKeys() {}

		static final class Configuration {
			static final String MODULE_NAME       = "com.adobe.module.configuration";
			static final String GLOBAL_CONFIG_PRIVACY            = "global.privacy";

			private Configuration() {}
		}

		static final class RuleEngine {
			static final String RULES_RESPONSE_CONSEQUENCE_KEY_TYPE = "type";
			static final String RULES_RESPONSE_CONSEQUENCE_KEY_ID = "id";
			static final String RULES_RESPONSE_CONSEQUENCE_KEY_DETAIL = "detail";
			static final String CONSEQUENCE_TRIGGERED = "triggeredconsequence";

			private RuleEngine() {}
		}

		static final class Signal {
			static final String MODULE_NAME = "com.adobe.module.signal";
			static final String RULES_RESPONSE_CONSEQUENCE_TYPE_POSTBACKS = "pb";
			static final String RULES_RESPONSE_CONSEQUENCE_TYPE_PII = "pii";
			static final String RULES_RESPONSE_CONSEQUENCE_TYPE_OPEN_URL = "url";
			static final String RULES_RESPONSE_CONTENT_OPENURL_URLS = "url";
			static final String SIGNAL_CONTEXT_DATA = "contextdata";

			private Signal() {}
		}
	}
}
