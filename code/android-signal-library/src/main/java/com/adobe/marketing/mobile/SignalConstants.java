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
