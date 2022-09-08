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

class ConfigurationConstants {
	private ConfigurationConstants() {}

	static final String FRIENDLY_NAME = "Configuration";

	static final class EventDataKeys {

		private EventDataKeys() { }

		static final class Configuration {
			static final String MODULE_NAME       									= "com.adobe.module.configuration";
			static final String CONFIGURATION_REQUEST_CONTENT_JSON_APP_ID			= "config.appId";
			static final String CONFIGURATION_REQUEST_CONTENT_JSON_FILE_PATH		= "config.filePath";
			static final String CONFIGURATION_REQUEST_CONTENT_JSON_ASSET_FILE		= "config.assetFile";
			static final String CONFIGURATION_REQUEST_CONTENT_UPDATE_CONFIG			= "config.update";
			static final String CONFIGURATION_REQUEST_CONTENT_CLEAR_UPDATED_CONFIG = "config.clearUpdates";
			static final String CONFIGURATION_REQUEST_CONTENT_RETRIEVE_CONFIG 		= "config.getData";
			static final String CONFIGURATION_RESPONSE_IDENTITY_ALL_IDENTIFIERS 	= "config.allIdentifiers";
			static final String CONFIGURATION_REQUEST_CONTENT_IS_INTERNAL_EVENT 	= "config.isinternalevent";
			static final String EVENT_STATE_OWNER 			 						= "stateowner";
			static final String RULES_CONFIG_URL 									= "rules.url";
			static final String GLOBAL_CONFIG_PRIVACY            					= "global.privacy";
			static final String BUILD_ENVIRONMENT 									= "build.environment";
			static final String ENVIRONMENT_PREFIX_DELIMITER 						= "__";

			private Configuration() {}
		}

		static final class Lifecycle {
			static final String LIFECYCLE_START = "start";
			static final String SESSION_EVENT 	= "sessionevent";

			private Lifecycle() {}
		}
		static final class RuleEngine {
			static final String CONSEQUENCE_JSON_ID = "id";
			static final String CONSEQUENCE_JSON_TYPE = "type";
			static final String CONSEQUENCE_JSON_DETAIL = "detail";
			static final String CONSEQUENCE_TRIGGERED = "triggeredconsequence";
			private RuleEngine() {}
		}

	}

	static final class SharedStateKeys {
		private SharedStateKeys() {}

		static final class Analytics {
			static final String MODULE_NAME      = "com.adobe.module.analytics";
			static final String ANALYTICS_ID     = "aid";
			static final String USER_IDENTIFIER = "vid";

			private Analytics() {}
		}

		static final class Audience {
			static final String MODULE_NAME     = "com.adobe.module.audience";
			static final String DPID            = "dpid";
			static final String DPUUID          = "dpuuid";
			static final String UUID			= "uuid";
			private Audience() {}
		}

		static final class Identity {
			static final String MODULE_NAME = "com.adobe.module.identity";
			static final String MID = "mid";
			static final String VISITOR_IDS_LIST = "visitoridslist";
			static final String ADVERTISING_IDENTIFIER = "advertisingidentifier";
			static final String PUSH_IDENTIFIER = "pushidentifier";
			private Identity() {}
		}

		static final class Target {
			static final String MODULE_NAME = "com.adobe.module.target";
			static final String TNT_ID         = "tntid";
			static final String THIRD_PARTY_ID = "thirdpartyid";
			private Target() {}
		}

		static final class Configuration {
			static final String CONFIG_EXPERIENCE_CLOUD_ORGID_KEY = "experienceCloud.org";
			static final String ANALYTICS_CONFIG_REPORT_SUITES    = "analytics.rsids";
			private Configuration() {}
		}

	}
}
