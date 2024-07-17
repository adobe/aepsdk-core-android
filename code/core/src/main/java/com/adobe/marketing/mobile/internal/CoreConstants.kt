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

package com.adobe.marketing.mobile.internal

internal object CoreConstants {
    const val LOG_TAG = "MobileCore"
    const val VERSION = "3.1.1"

    object EventDataKeys {
        /**
         * Holds `EventData` keys for the `Analytics` module.
         */
        object Analytics {
            const val TRACK_ACTION = "action"
            const val TRACK_STATE = "state"
            const val CONTEXT_DATA = "contextdata"
        }

        /**
         * Holds `EventData` keys for the `Configuration` module.
         */
        object Configuration {
            const val GLOBAL_CONFIG_PRIVACY = "global.privacy"

            // Configuration EventData Keys
            const val CONFIGURATION_REQUEST_CONTENT_JSON_APP_ID = "config.appId"
            const val CONFIGURATION_REQUEST_CONTENT_JSON_FILE_PATH = "config.filePath"
            const val CONFIGURATION_REQUEST_CONTENT_JSON_ASSET_FILE = "config.assetFile"
            const val CONFIGURATION_REQUEST_CONTENT_UPDATE_CONFIG = "config.update"
            const val CONFIGURATION_REQUEST_CONTENT_CLEAR_UPDATED_CONFIG = "config.clearUpdates"
            const val CONFIGURATION_REQUEST_CONTENT_RETRIEVE_CONFIG = "config.getData"
            const val CONFIGURATION_RESPONSE_IDENTITY_ALL_IDENTIFIERS = "config.allIdentifiers"
        }

        /**
         * Holds `EventData` keys for the `Identity` module.
         */
        object Identity {
            const val ADVERTISING_IDENTIFIER = "advertisingidentifier"
            const val PUSH_IDENTIFIER = "pushidentifier"
        }

        /**
         * Holds `EventData` keys for the `Lifecycle` module.
         */
        object Lifecycle {
            const val ADDITIONAL_CONTEXT_DATA = "additionalcontextdata"
            const val LIFECYCLE_ACTION_KEY = "action"
            const val LIFECYCLE_START = "start"
            const val LIFECYCLE_PAUSE = "pause"
        }

        /**
         * Holds `EventData` keys for the `Signal` module.
         */
        object Signal {
            const val SIGNAL_CONTEXT_DATA = "contextdata"
        }
    }

    /**
     * Holds `Wrapper` constants
     */
    object Wrapper {
        object Name {
            const val REACT_NATIVE = "React Native"
            const val FLUTTER = "Flutter"
            const val CORDOVA = "Cordova"
            const val UNITY = "Unity"
            const val XAMARIN = "Xamarin"
            const val NONE = "None"
        }

        object Type {
            const val REACT_NATIVE = "R"
            const val FLUTTER = "F"
            const val CORDOVA = "C"
            const val UNITY = "U"
            const val XAMARIN = "X"
            const val NONE = "N"
        }
    }

    object EventNames {
        const val ANALYTICS_TRACK = "Analytics Track"
        const val COLLECT_PII = "Collect PII"
        const val COLLECT_DATA = "Collect Data"
        const val CONFIGURE_WITH_APP_ID = "Configure with App ID"
        const val CONFIGURE_WITH_FILE_PATH = "Configure with File Path"
        const val CLEAR_UPDATED_CONFIGURATION = "Clear Updated Configuration"
        const val CONFIGURATION_REQUEST = "Configuration Request"
        const val CONFIGURATION_RESPONSE = "Configuration Response"
        const val CONFIGURATION_UPDATE = "Configuration Update"
        const val GET_SDK_IDENTITIES = "Get SDK Identities"
        const val LIFECYCLE_PAUSE = "Lifecycle Pause"
        const val LIFECYCLE_RESUME = "Lifecycle Resume"
        const val PRIVACY_STATUS_REQUEST = "Privacy Status Request"
        const val SET_PUSH_IDENTIFIER = "Set Push Identifier"
        const val SET_ADVERTISING_IDENTIFIER = "Set Advertising Identifier"
        const val RESET_IDENTITIES_REQUEST = "Reset Identities Request"
    }
}
