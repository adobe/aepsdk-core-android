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

/**
 * This class contains the {@link String} constants used as keys for creating {@code EventData} from the data supplied
 * through the Platform APIs.
 * <p>
 * These keys should be used by {@code Modules} to extract data from the public APIs.
 * <p>
 * Please refer the Platform Public API class for further information as to how the keys are used.
 */
@Deprecated
final class EventDataKeys {

	public static final String CORE_VERSION                  = "0.1";
	public static final String DEEPLINK_SCHEME				 = "adbinapp";
	public static final String DEEPLINK_SCHEME_PATH_CANCEL	 = "cancel";
	public static final String DEEPLINK_SCHEME_PATH_CONFIRM  = "confirm";
	public static final String EVENT_STATE_OWNER 			 = "stateowner";


	public static final String DATA_MAP                      = "adb_data_map";
	public static final String PAGE_NAME                     = "adb_page_name";
	public static final String LOC_LATITUDE                  = "adb_loc_lat";
	public static final String LOC_LONGITUDE                 = "adb_loc_long";
	public static final String DEEPLINK_URI                  = "adb_deeplink_uri";
	public static final String DEFAULT_DATA_KEY              = "defaultdata";




	private EventDataKeys() {}

	/**
	 * Holds {@code EventData} keys for the {@code Acquisition} module.
	 */
	public static final class Acquisition {
		public static final String MODULE_NAME                         = "com.adobe.module.acquisition";
		public static final String LIFECYCLE_PUSH_MESSAGE_ID_KEY       = "pushmessageid";
		public static final String LIFECYCLE_LOCAL_NOTIFICATION_ID_KEY = "notificationid";
		public static final String APP_ID_KEY                          = "appid";
		public static final String DATA_DEEPLINK_KEY                   = "deeplink";
		public static final String REFERRER_DATA_KEY 				   = "receivedreferrerdata";
		public static final String ACQUISITION_TYPE_KEY                = "acquisitiontype";
		public static final String ACQUISITION_TYPE_INSTALL            = "install";
		public static final String ACQUISITION_TYPE_RETENTION          = "retention";
		public static final String REFERRER_DATA                       = "referrerdata";

		private Acquisition() {}
	}

	/**
	 * Holds {@code EventData} keys for the {@code Analytics} module.
	 */
	public static final class Analytics {
		public static final String MODULE_NAME       = "com.adobe.module.analytics";
		public static final String ANALYTICS_SERVER_RESPONSE_KEY = "analyticsserverresponse";
		public static final String FORCE_KICK_HITS  = "forcekick";
		public static final String CLEAR_HITS_QUEUE = "clearhitsqueue";
		public static final String ANALYTICS_ID     = "aid";
		public static final String GET_QUEUE_SIZE   = "getqueuesize";
		public static final String QUEUE_SIZE       = "queuesize";
		public static final String TRACK_INTERNAL   = "trackinternal";
		public static final String TRACK_ACTION     = "action";
		public static final String TRACK_STATE      = "state";
		public static final String CONTEXT_DATA     = "contextdata";

		private Analytics() {}

		/**
		 * Analytics context data keys
		 */
		static class ContextDataKeys {
			static final String INSTALL_EVENT_KEY         = "a.InstallEvent";
			static final String LAUNCH_EVENT_KEY          = "a.LaunchEvent";
			static final String CRASH_EVENT_KEY           = "a.CrashEvent";
			static final String UPGRADE_EVENT_KEY         = "a.UpgradeEvent";
			static final String DAILY_ENGAGED_EVENT_KEY   = "a.DailyEngUserEvent";
			static final String MONTHLY_ENGAGED_EVENT_KEY = "a.MonthlyEngUserEvent";
			static final String INSTALL_DATE              = "a.InstallDate";
			static final String LAUNCHES                  = "a.Launches";
			static final String PREVIOUS_SESSION_LENGTH   = "a.PrevSessionLength";
			static final String DAYS_SINCE_FIRST_LAUNCH   = "a.DaysSinceFirstUse";
			static final String DAYS_SINCE_LAST_LAUNCH    = "a.DaysSinceLastUse";
			static final String HOUR_OF_DAY               = "a.HourOfDay";
			static final String DAY_OF_WEEK               = "a.DayOfWeek";
			static final String OPERATING_SYSTEM          = "a.OSVersion";
			static final String APPLICATION_IDENTIFIER    = "a.AppID";
			static final String DAYS_SINCE_LAST_UPGRADE   = "a.DaysSinceLastUpgrade";
			static final String LAUNCHES_SINCE_UPGRADE    = "a.LaunchesSinceUpgrade";
			static final String ADVERTISING_IDENTIFIER    = "a.adid";
			static final String DEVICE_NAME               = "a.DeviceName";
			static final String DEVICE_RESOLUTION         = "a.Resolution";
			static final String CARRIER_NAME              = "a.CarrierName";
			static final String LOCALE                    = "a.locale";
			static final String RUN_MODE                  = "a.RunMode";
			static final String IGNORED_SESSION_LENGTH    = "a.ignoredSessionLength";
			static final String ACTION_KEY                = "a.action";
			static final String INTERNAL_ACTION_KEY       = "a.internalaction";
			static final String TIME_SINCE_LAUNCH_KEY     = "a.TimeSinceLaunch";
			static final String BEACON_MAJOR_KEY          = "a.beacon.major";
			static final String BEACON_MINOR_KEY          = "a.beacon.minor";
			static final String BEACON_UUID_KEY           = "a.beacon.uuid";
			static final String BEACON_PROX_KEY           = "a.beacon.prox";

			private ContextDataKeys() {}
		}

		/**
		 * Analytics context data values
		 */
		static class ContextDataValues {
			static final String UPGRADE_EVENT = "UpgradeEvent";
			static final String CRASH_EVENT = "CrashEvent";
			static final String LAUNCH_EVENT = "LaunchEvent";
			static final String INSTALL_EVENT = "InstallEvent";
			static final String DAILY_ENG_USER_EVENT = "DailyEngUserEvent";
			static final String MONTHLY_ENG_USER_EVENT = "MonthlyEngUserEvent";

			private ContextDataValues() {}
		}
	}

	/**
	 * Holds {@code EventData} keys for the {@code Audience} module.
	 */
	public static final class Audience {
		public static final String MODULE_NAME       = "com.adobe.module.audience";

		// request keys
		public static final String VISITOR_TRAITS = "aamtraits";

		// response keys
		public static final String VISITOR_PROFILE = "aamprofile";
		public static final String DPID            = "dpid";
		public static final String DPUUID          = "dpuuid";
		public static final String UUID			   = "uuid";

		private Audience() {}
	}

	/**
	 * Holds {@code EventData} keys for the {@code Configuration} module.
	 */
	public static final class Configuration {
		public static final String MODULE_NAME       = "com.adobe.module.configuration";

		// config response keys
		public static final String GLOBAL_CONFIG_SSL                = "global.ssl";
		public static final String GLOBAL_CONFIG_PRIVACY            = "global.privacy";
		public static final String AAM_CONFIG_SERVER                = "audience.server";
		public static final String AAM_CONFIG_TIMEOUT               = "audience.timeout";
		public static final String CONFIG_EXPERIENCE_CLOUD_ORGID_KEY = "experienceCloud.org";

		public static final String ANALYTICS_CONFIG_AAMFORWARDING    = "analytics.aamForwardingEnabled";
		public static final String ANALYTICS_CONFIG_BATCH_LIMIT      = "analytics.batchLimit";
		public static final String ANALYTICS_CONFIG_OFFLINE_TRACKING = "analytics.offlineEnabled";
		public static final String ANALYTICS_CONFIG_REPORT_SUITES    = "analytics.rsids";
		public static final String ANALYTICS_CONFIG_SERVER           = "analytics.server";
		public static final String ANALYTICS_CONFIG_REFERRER_TIMEOUT = "analytics.referrerTimeout";
		public static final String ANALYTICS_CONFIG_LIFECYCLE_TIMEOUT 			= "analytics.lifecycleTimeout";
		public static final String ANALYTICS_CONFIG_BACKDATE_PREVIOUS_SESSION 	= "analytics.backdatePreviousSessionInfo";

		public static final String LIFECYCLE_CONFIG_SESSION_TIMEOUT  = "lifecycle.sessionTimeout";

		public static final String ACQUISITION_CONFIG_APPID  = "acquisition.appid";
		public static final String ACQUISITION_CONFIG_SERVER = "acquisition.server";

		public static final String IDENTITY_ADID_ENABLED = "identity.adidEnabled";

		public static final String SIGNAL_CONFIG_URL = "signals.url";
		public static final String RULES_CONFIG_URL = "rules.url";

		public static final String MESSAGING_URL = "messaging.url";

		public static final String TARGET_CLIENT_CODE = "target.clientCode";
		public static final String TARGET_NETWORK_TIMEOUT = "target.timeout";
		public static final String TARGET_ENVIRONMENT_ID = "target.environmentId";

		// Configuration EventData Keys
		public static final String CONFIGURATION_REQUEST_CONTENT_JSON_APP_ID			= "config.appId";
		public static final String CONFIGURATION_REQUEST_CONTENT_JSON_FILE_PATH			= "config.filePath";
		public static final String CONFIGURATION_REQUEST_CONTENT_UPDATE_CONFIG			= "config.update";
		public static final String CONFIGURATION_REQUEST_CONTENT_INTERNAL_BOOT			= "config.internal.boot";
		public static final String CONFIGURATION_REQUEST_CONTENT_RETRIEVE_CONFIG 		= "config.getData";
		public static final String CONFIGURATION_RESPONSE_IDENTITY_ALL_IDENTIFIERS 		= "config.allIdentifiers";

		private Configuration() {}
	}

	/**
	 * Holds {@code EventData} keys for the {@code Identity} module.
	 */
	public static final class Identity {
		public static final String MODULE_NAME = "com.adobe.module.identity";
		public static final String MCPNS_DPID = "20919";
		public static final String ADID_DSID  = "DSID_20914";

		//******************************************************
		// Getter Keys: Key constants in reading from Response Identity events.
		//*******************************************************

		//Event Data key for fetching marketing cloud id from the Identity Response Event.
		public static final String VISITOR_ID_MID = "mid";

		//Event Data key for fetching blob from the Identity Response Event.
		public static final String VISITOR_ID_BLOB = "blob";

		//Event Data key for fetching location hint from the Identity Response Event.
		public static final String VISITOR_ID_LOCATION_HINT = "locationhint";

		//Event Data key for fetching last sync long value from the Identity Response Event.
		public static final String VISITOR_IDS_LAST_SYNC = "lastsync";

		//Event Data key for reading a list of maps, with each map representing a visitor id,from Response Identity event dispatched by the module.
		public static final String VISITOR_IDS_LIST = "visitoridslist";

		//Event Data key for reading the updated url in the event received by the one time event listener as a response to setting BASE_URL in Requent Identity event.
		public static final String UPDATED_URL = "updatedurl";


		//******************************************************
		// Setter Keys: Key constants in creating Request Identity events.
		//******************************************************

		//Event Data key for base URL for appending visitor data to, when creating Request Identity event for appendToURL()
		public static final String BASE_URL = "baseurl";

		//Event Data key for forcing syncing of identifiers, when creating Request Identity event for syncIdenfiers()
		public static final String FORCE_SYNC = "forcesync";

		//Event Data key for setting <String,String> map of identifiers, when creating Request Identity event for syncIdenfiers()
		public static final String IDENTIFIERS = "visitoridentifiers";

		//Event Data key for setting "dpids" when creating Request Identity event for syncIdenfiers()
		public static final String DPIDS = "dpids";

		/*
		 * Event Data key for marking an event of sync type when creating Request Identity event .
		 * Setting this value to true will result in a sync identifiers network call.
		 * */
		public static final String IS_SYNC_EVENT = "issyncevent";


		//**********************************************************************************************************
		// Both: Key constants in creating Request Identity events and reading from Identity Response events.
		//**********************************************************************************************************
		/*
		 * Event Data key for setting user identifier when creating Request Identity event.
		 * Also, Event Data key for reading user identifier from Response Identity event dispatched by the module.
		 * */
		public static final String USER_IDENTIFIER = "vid";

		/*
		 * Event Data key for setting advertising identifier when creating Request Identity event.
		 * Also, Event Data key for reading advertising identifier from Response Identity event dispatched by the module.
		 * */
		public static final String ADVERTISING_IDENTIFIER = "advertisingidentifier";

		/*
		 * Event Data key for setting push identifier when creating Request Identity event.
		 * Also, Event Data key for reading push identifier from Response Identity event dispatched by the module.
		 * */
		public static final String PUSH_IDENTIFIER = "pushidentifier";

		/*
		 * Event Data key for setting visitor id authentication value in Request Idenity event for syncIdentifiers.
		 * Also, Event Data key for reading visitor id authentication value from Response Identity event dispatched by the module.
		 * */
		public static final String AUTHENTICATION_STATE = "authenticationstate";

		//***********************************************************
		// Key constants in reading Identity Analytics Internal Event
		//***********************************************************
		public static final String EVENT_PUSH_STATUS = "a.push.optin";
		public static final String PUSH_ID_ENABLED_ACTION_NAME = "Push";

		private Identity() {}
	}

	/**
	 * Holds {@code EventData} keys for the {@code Lifecycle} module.
	 */
	public static final class Lifecycle {
		public static final String MODULE_NAME = "com.adobe.module.lifecycle";

		public static final String ADDITIONAL_CONTEXT_DATA = "additionalcontextdata";
		public static final String LIFECYCLE_ACTION_KEY = "action";
		public static final String LIFECYCLE_START = "start";
		public static final String LIFECYCLE_PAUSE = "pause";
		public static final String LIFECYCLE_CONTEXT_DATA = "lifecyclecontextdata";
		public static final String SESSION_START_TIMESTAMP = "starttimestampmillis";
		public static final String PREVIOUS_SESSION_START_TIMESTAMP = "previoussessionstarttimestampmillis";
		public static final String PREVIOUS_SESSION_PAUSE_TIMESTAMP = "previousSessionPauseTimestampmillis";
		public static final String SESSION_EVENT = "sessionevent";
		public static final String MAX_SESSION_LENGTH = "maxsessionlength";

		private Lifecycle() {}
	}

	/**
	 * Holds {@code EventData} keys for the {@code RulesEngine} module.
	 */
	public static final class RuleEngine {

		public static final String MODULE_NAME = "com.adobe.module.rulesEngine";

		public static final String RULES_RESPONSE_CONSEQUENCE_KEY_TYPE = "type";
		public static final String RULES_RESPONSE_CONSEQUENCE_KEY_ID = "id";
		public static final String RULES_RESPONSE_CONSEQUENCE_KEY_DETAIL = "detail";

		public static final String RULES_RESPONSE_CONSEQUENCE_TYPE_POSTBACKS = "pb";
		public static final String RULES_RESPONSE_CONSEQUENCE_TYPE_PII = "pii";
		public static final String RULES_RESPONSE_CONSEQUENCE_TYPE_OPEN_URL = "url";

		/**
		 * Value will be a {@link java.util.Map} of KV pairs of the loaded consequence json.
		 */
		public static final String CONSEQUENCES_LOADED = "loadedconsequences";
		/**
		 * Value will be a {@link java.util.Map} of KV pairs of the triggered consequence json.
		 */
		public static final String CONSEQUENCE_TRIGGERED = "triggeredconsequence";
		/**
		* Value will be a {@link java.util.Map} of KV pairs containing the system data that might have been used to
		* trigger the rule actions. For some of the actions, this map needs to be bundled along with the action data for
		* the target module to consume.
		*/
		public static final String RULES_RESPONSE_CONTENT_CONTEXT_DATA = "context_data";
		/**
		 * Event Data key for marking an event to force download rules.
		 */
		public static final String RULES_REQUEST_CONTENT_DOWNLOAD_RULES = "download_rules";

		public static final String CONSEQUENCE_JSON_ID = "id";
		public static final String CONSEQUENCE_JSON_TYPE = "type";
		public static final String CONSEQUENCE_JSON_DETAIL = "detail";
		public static final String CONSEQUENCE_ASSETS_PATH = "assetsPath";

		private RuleEngine() {}
	}

	/**
	 * Holds {@code EventData} keys for the {@code Target} module.
	 */
	public static final class Target {
		public static final String MODULE_NAME = "com.adobe.module.target";
		public static final String TNT_ID         = "tntid";
		public static final String THIRD_PARTY_ID = "thirdpartyid";
		public static final String LOAD_REQUESTS = "request";
		public static final String TARGET_CONTENT = "content";
		public static final String LOCATION_CLICKED = "locationclicked";


		public static final String PREFETCH_REQUESTS = "prefetch";
		public static final String PREFETCH_RESULT = "prefetchresult";
		public static final String RESET_EXPERIENCE = "resetexperience";
		public static final String CLEAR_PREFETCH_CACHE = "clearcache";
		public static final String PROFILE_PARAMETERS = "profileparams";
		public static final String PREVIEW_RESTART_DEEP_LINK = "restartdeeplink";

		public static final String MBOX_NAME = "mboxname";
		public static final String MBOX_PARAMETERS = "mboxparameters";
		public static final String ORDER_PARAMETERS = "orderparameters";
		public static final String PRODUCT_PARAMETERS = "productparameters";
		public static final String DEFAULT_CONTENT = "defaultcontent";
		public static final String BASE_CLASS = "baseclass";
		public static final String RESPONSE_PAIR_ID = "responsepairid";

		private Target() {}
	}

	/**
	 * Holds {@code EventData} keys for the {@code UserProfile} module.
	 */
	public static final class UserProfile {
		public static final String MODULE_NAME = "com.adobe.module.userProfile";
		/**
		 * This is the EventData key for the UserProfile Response event. The value for the key
		 * will be a {@link java.util.Map}. Use {@code EventData.getObject(String)} to read the value.
		 */
		public static final String USER_PROFILE_DATA_KEY            = "userprofiledata";

		/**
		 * This is the EventData key for the UserProfile Request Profile event. The value expected by the
		 * UserProfile module for this key is a {@link java.util.Map}.
		 */
		public static final String UPDATE_DATA_KEY 				= "userprofileupdatekey";

		/**
		 * This is the EventData key for the UserProfile Request Reset event. The value expected by the
		 * UserProfile module for this key is a {@link String}.
		 */
		public static final String REMOVE_DATA_KEY            	= "userprofileremovekey";


		/**
		 * This is the EventData key for the Rules Response content event. A {@link String} value is expected indicating
		 * the type of operation (write or delete).
		 */
		public static final String CONSEQUENCE_OPERATION        = "operation";

		/**
		 * This is the EventData key for the Rules Response content event. A {@link String} value representing a profile attribute
		 * key
		 */
		public static final String CONSEQUENCE_KEY        		= "key";

		/**
		 * This is the EventData key for the Rules Response content event. A {@link String} value representing token expanded
		 * profile attribute value
		 */
		public static final String CONSEQUENCE_VALUE        	= "value";


		private UserProfile() {}
	}

	/**
	 * Holds {@code EventData} keys for the {@code Messages} module.
	 */
	public static final class Messages {
		public static final String MODULE_NAME = "com.adobe.module.messages";
		private Messages() {}
	}

	/**
	 * Holds {@code EventData} keys for the {@code Signal} module.
	 */
	public static final class Signal {
		public static final String MODULE_NAME = "com.adobe.module.signal";
		public static final String SIGNAL_CONTEXT_DATA = "contextdata";

		private Signal() {}
	}

}