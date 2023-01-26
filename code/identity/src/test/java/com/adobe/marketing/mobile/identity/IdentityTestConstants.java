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

package com.adobe.marketing.mobile.identity;

import com.adobe.marketing.mobile.MobilePrivacyStatus;

/**
 * Holds all the constant values to be used by various classes in the {@link IdentityExtension}
 * module
 */
final class IdentityTestConstants {

    static final class Defaults {

        static final long DEFAULT_TTL_VALUE = 600;
        static final String SERVER = "dpm.demdex.net";
        static final String CID_DELIMITER = "%01";
        static final int TIMEOUT = 2000;
        static final boolean DEFAULT_SSL = true;
        static final MobilePrivacyStatus DEFAULT_MOBILE_PRIVACY = MobilePrivacyStatus.UNKNOWN;
        static final String ZERO_ADVERTISING_ID = "00000000-0000-0000-0000-000000000000";

        private Defaults() {}
    }

    static final int ID_INFO_SIZE = 3;

    // Configuration module string constants
    static final String JSON_CONFIG_ORGID_KEY = "experienceCloud.org";
    static final String JSON_CONFIG_PRIVACY_KEY = "global.privacy";
    static final String JSON_EXPERIENCE_CLOUD_SERVER_KEY = "experienceCloud.server";

    static class DataStoreKeys {

        static final String IDENTITY_PROPERTIES_DATA_STORE_NAME = "visitorIDServiceDataStore";
        static final String VISITOR_IDS_STRING = "ADOBEMOBILE_VISITORID_IDS";
        static final String MARKETING_CLOUD_ID = "ADOBEMOBILE_PERSISTED_MID";
        static final String LOCATION_HINT = "ADOBEMOBILE_PERSISTED_MID_HINT";
        static final String BLOB = "ADOBEMOBILE_PERSISTED_MID_BLOB";
        static final String TTL = "ADOBEMOBILE_VISITORID_TTL";
        static final String LAST_SYNC = "ADOBEMOBILE_VISITORID_SYNC";
        static final String ADVERTISING_IDENTIFIER = "ADOBEMOBILE_ADVERTISING_IDENTIFIER";
        static final String PUSH_IDENTIFIER = "ADOBEMOBILE_PUSH_IDENTIFIER";
        static final String PUSH_ENABLED = "ADOBEMOBILE_PUSH_ENABLED";
        static final String ANALYTICS_PUSH_SYNC = "ADOBEMOBILE_ANALYTICS_PUSH_SYNC";
        static final String AID_SYNCED_KEY = "ADOBEMOBILE_AID_SYNCED";

        private DataStoreKeys() {}
    }

    static class UrlKeys {

        // Constants for preparing sync network call
        static final String MID = "d_mid";
        static final String ORGID = "d_orgid";
        static final String BLOB = "d_blob";
        static final String HINT = "dcs_region";
        static final String TTL = "id_sync_ttl";
        static final String RESPONSE_ERROR = "error_msg";
        static final String OPT_OUT = "d_optout";
        static final String DEVICE_CONSENT = "device_consent";
        static final String CONSENT_INTEGRATION_CODE = "d_consent_ic";
        static final String VISITOR_ID = "d_cid_ic";
        // Constants for optout hit
        static final String PATH_OPTOUT = "demoptout.jpg";
        // payload parameters
        static final String ADB_VISITOR_PAYLOAD_KEY = "adobe_mc";
        static final String ADB_VISITOR_TIMESTAMP_KEY = "TS";
        static final String ADB_VISITOR_PAYLOAD_MARKETING_CLOUD_ORG_ID = "MCORGID";
        static final String ADB_VISITOR_PAYLOAD_MARKETING_CLOUD_ID_KEY = "MCMID";
        static final String ADB_ANALYTICS_PAYLOAD_KEY = "adobe_aa_vid";
        static final String ADB_VISITOR_PAYLOAD_ANALYTICS_ID_KEY = "MCAID";

        private UrlKeys() {}
    }

    private IdentityTestConstants() {}

    static final class EventDataKeys {

        static final String STATE_OWNER = "stateowner";

        private EventDataKeys() {}

        static final class Analytics {

            static final String MODULE_NAME = "com.adobe.module.analytics";
            static final String ANALYTICS_ID = "aid";
            static final String VISITOR_IDENTIFIER = "vid";
            static final String TRACK_INTERNAL = "trackinternal";
            static final String TRACK_ACTION = "action";
            static final String CONTEXT_DATA = "contextdata";

            private Analytics() {}
        }

        static final class EventHub {

            static final String MODULE_NAME = "com.adobe.module.eventhub";
            static final String EXTENSIONS = "extensions";
            static final String VERSION = "version";
        }

        static final class Configuration {

            static final String MODULE_NAME = "com.adobe.module.configuration";
            static final String GLOBAL_CONFIG_PRIVACY = "global.privacy";
            static final String CONFIGURATION_REQUEST_CONTENT_UPDATE_CONFIG = "config.update";
            static final String CONFIG_EXPERIENCE_CLOUD_ORGID_KEY = "experienceCloud.org";
            static final String AAM_CONFIG_SERVER = "audience.server";

            private Configuration() {}
        }

        static final class Audience {

            // opted out response key
            static final String OPTED_OUT_HIT_SENT = "optedouthitsent";

            private Audience() {}
        }

        static final class Identity {

            static final String MODULE_NAME = "com.adobe.module.identity";
            static final String MCPNS_DPID = "20919";
            static final String ADID_DSID = "DSID_20914";

            // Event Data key for fetching marketing cloud id from the IdentityExtension Response
            // Event.
            static final String VISITOR_ID_MID = "mid";

            // Event Data key for fetching blob from the IdentityExtension Response Event.
            static final String VISITOR_ID_BLOB = "blob";

            // Event Data key for fetching location hint from the IdentityExtension Response Event.
            static final String VISITOR_ID_LOCATION_HINT = "locationhint";

            // Event Data key for fetching last sync long value from the IdentityExtension Response
            // Event.
            static final String VISITOR_IDS_LAST_SYNC = "lastsync";

            // Event Data key for reading a list of maps, with each map representing a visitor
            // id,from Response IdentityExtension event dispatched by the module.
            static final String VISITOR_IDS_LIST = "visitoridslist";

            // Event Data key for reading the updated url in the event received by the one time
            // event listener as a response to setting BASE_URL in Requent IdentityExtension event.
            static final String UPDATED_URL = "updatedurl";

            // Event Data key for url variable string when creating a Request or receiving a
            // Response for getUrlVariables()
            static final String URL_VARIABLES = "urlvariables";

            // Event Data key for base URL for appending visitor data to, when creating Request
            // IdentityExtension event for appendToURL()
            static final String BASE_URL = "baseurl";

            // Event Data key for forcing syncing of identifiers, when creating Request
            // IdentityExtension event for syncIdenfiers()
            static final String FORCE_SYNC = "forcesync";

            // Event Data key for setting <String,String> map of identifiers, when creating Request
            // IdentityExtension event for syncIdenfiers()
            static final String IDENTIFIERS = "visitoridentifiers";

            // Event Data key for setting "dpids" when creating Request IdentityExtension event for
            // syncIdenfiers()
            static final String DPIDS = "dpids";

            /*
             * Event Data key for marking an event of sync type when creating Request IdentityExtension event .
             * Setting this value to true will result in a sync identifiers network call.
             * */
            static final String IS_SYNC_EVENT = "issyncevent";

            /*
             * Event Data key for setting advertising identifier when creating Request IdentityExtension event.
             * Also, Event Data key for reading advertising identifier from Response IdentityExtension event dispatched by the module.
             * */
            static final String ADVERTISING_IDENTIFIER = "advertisingidentifier";

            /*
             * Event Data key for setting push identifier when creating Request IdentityExtension event.
             * Also, Event Data key for reading push identifier from Response IdentityExtension event dispatched by the module.
             * */
            static final String PUSH_IDENTIFIER = "pushidentifier";

            /*
             * Event Data key for setting visitor id authentication value in Request Idenity event for syncIdentifiers.
             * Also, Event Data key for reading visitor id authentication value from Response IdentityExtension event dispatched by the module.
             * */
            static final String AUTHENTICATION_STATE = "authenticationstate";

            // Event Data Key for setting Analytics ID in 'visitoridentifiers' map when sending to
            // Identity Service
            static final String ANALYTICS_ID = "AVID";

            // ***********************************************************
            // Key constants in reading IdentityExtension Analytics Internal Event
            // ***********************************************************
            static final String EVENT_PUSH_STATUS = "a.push.optin";
            static final String PUSH_ID_ENABLED_ACTION_NAME = "Push";

            static final String UPDATE_SHARED_STATE = "updatesharedstate";

            private Identity() {}
        }
    }
}
