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

final class CoreConstants {

	private CoreConstants() {}

	static class EventDataKeys {

		private EventDataKeys() {}

		/**
		 * Holds {@code EventData} keys for the {@code Analytics} module.
		 */
		static final class Analytics {
			static final String TRACK_ACTION     = "action";
			static final String TRACK_STATE      = "state";
			static final String CONTEXT_DATA     = "contextdata";

			private Analytics() {}
		}

		/**
		 * Holds {@code EventData} keys for the {@code Configuration} module.
		 */
		public static final class Configuration {
			static final String GLOBAL_CONFIG_PRIVACY            = "global.privacy";

			// Configuration EventData Keys
			static final String CONFIGURATION_REQUEST_CONTENT_JSON_APP_ID			= "config.appId";
			static final String CONFIGURATION_REQUEST_CONTENT_JSON_FILE_PATH		= "config.filePath";
			static final String CONFIGURATION_REQUEST_CONTENT_JSON_ASSET_FILE		= "config.assetFile";
			static final String CONFIGURATION_REQUEST_CONTENT_UPDATE_CONFIG			= "config.update";
			static final String CONFIGURATION_REQUEST_CONTENT_CLEAR_UPDATED_CONFIG = "config.clearUpdates";
			static final String CONFIGURATION_REQUEST_CONTENT_RETRIEVE_CONFIG 		= "config.getData";
			static final String CONFIGURATION_RESPONSE_IDENTITY_ALL_IDENTIFIERS 	= "config.allIdentifiers";

			private Configuration() {}
		}

		/**
		 * Holds {@code EventData} keys for the {@code Identity} module.
		 */
		public static final class Identity {
			/*
			 * Event Data key for setting advertising identifier when creating Request Identity event.
			 * Also, Event Data key for reading advertising identifier from Response Identity event dispatched by the module.
			 * */
			static final String ADVERTISING_IDENTIFIER = "advertisingidentifier";

			/*
			 * Event Data key for setting push identifier when creating Request Identity event.
			 * Also, Event Data key for reading push identifier from Response Identity event dispatched by the module.
			 * */
			static final String PUSH_IDENTIFIER = "pushidentifier";

			private Identity() {}
		}

		/**
		 * Holds {@code EventData} keys for the {@code Lifecycle} module.
		 */
		public static final class Lifecycle {
			static final String ADDITIONAL_CONTEXT_DATA = "additionalcontextdata";
			static final String LIFECYCLE_ACTION_KEY = "action";
			static final String LIFECYCLE_START = "start";
			static final String LIFECYCLE_PAUSE = "pause";

			private Lifecycle() {}
		}

		/**
		 * Holds {@code EventData} keys for the {@code RulesEngine} module.
		 */
		static final class RuleEngine {
			/**
			 * Event Data key for marking an event to force download rules.
			 */
			static final String RULES_REQUEST_CONTENT_DOWNLOAD_RULES = "download_rules";

			private RuleEngine() {}
		}

		/**
		 * Holds {@code EventData} keys for the {@code Signal} module.
		 */
		static final class Signal {
			static final String SIGNAL_CONTEXT_DATA = "contextdata";

			private Signal() {}
		}
	}

}
