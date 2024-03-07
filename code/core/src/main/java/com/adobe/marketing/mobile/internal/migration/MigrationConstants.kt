/*
  Copyright 2023 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.internal.migration

internal object MigrationConstants {
    object V4 {
        const val DATASTORE_NAME = "APP_MEASUREMENT_CACHE"

        object Lifecycle {
            const val INSTALL_DATE = "ADMS_InstallDate"
            const val UPGRADE_DATE = "ADMS_UpgradeDate"
            const val LAST_USED_DATE = "ADMS_LastDateUsed"
            const val LAUNCHES_AFTER_UPGRADE = "ADMS_LaunchesAfterUpgrade"
            const val LAUNCHES = "ADMS_Launches"
            const val LAST_VERSION = "ADMS_LastVersion"
            const val START_DATE = "ADMS_SessionStart"
            const val PAUSE_DATE = "ADMS_PauseDate"
            const val SUCCESFUL_CLOSE = "ADMS_SuccessfulClose"
            const val OS = "ADOBEMOBILE_STOREDDEFAULTS_OS"
            const val APPLICATION_ID = "ADOBEMOBILE_STOREDDEFAULTS_APPID"
            const val CONTEXT_DATA = "ADMS_LifecycleData"
        }

        object Acquisition {
            const val REFERRER_DATA = "ADMS_Referrer_ContextData_Json_String"
            const val DEFAULTS_KEY_REFERRER_UTM_SOURCE = "utm_source"
            const val DEFAULTS_KEY_REFERRER_UTM_MEDIUM = "utm_medium"
            const val DEFAULTS_KEY_REFERRER_UTM_TERM = "utm_term"
            const val DEFAULTS_KEY_REFERRER_UTM_CONTENT = "utm_content"
            const val DEFAULTS_KEY_REFERRER_UTM_CAMPAIGN = "utm_campaign"
            const val DEFAULTS_KEY_REFERRER_TRACKINGCODE = "trackingcode"
        }

        object AudienceManager {
            const val USER_ID = "AAMUserId"
            const val USER_PROFILE = "AAMUserProfile"
        }

        object Target {
            const val THIRD_PARTY_ID = "ADBMOBILE_TARGET_3RD_PARTY_ID"
            const val TNT_ID = "ADBMOBILE_TARGET_TNT_ID"
            const val LAST_TIMESTAMP = "ADBMOBILE_TARGET_LAST_TIMESTAMP"
            const val SESSION_ID = "ADBMOBILE_TARGET_SESSION_ID"
            const val EDGE_HOST = "ADBMOBILE_TARGET_EDGE_HOST"
            const val COOKIE_EXPIRES = "mboxPC_Expires"
            const val COOKIE_VALUE = "mboxPC_Value"
        }

        object Analytics {
            const val AID = "ADOBEMOBILE_STOREDDEFAULTS_AID"
            const val IGNORE_AID = "ADOBEMOBILE_STOREDDEFAULTS_IGNORE_AID"
            const val LAST_KNOWN_TIMESTAMP = "ADBLastKnownTimestampKey"
        }

        object Identity {
            const val MID = "ADBMOBILE_PERSISTED_MID"
            const val BLOB = "ADBMOBILE_PERSISTED_MID_BLOB"
            const val HINT = "ADBMOBILE_PERSISTED_MID_HINT"
            const val VISITOR_IDS = "ADBMOBILE_VISITORID_IDS"
            const val VISITOR_ID_SYNC = "ADBMOBILE_VISITORID_SYNC"
            const val VISITOR_ID_TTL = "ADBMOBILE_VISITORID_TTL"
            const val VISITOR_ID = "APP_MEASUREMENT_VISITOR_ID"
            const val ADVERTISING_IDENTIFIER = "ADOBEMOBILE_STOREDDEFAULTS_ADVERTISING_IDENTIFIER"
            const val PUSH_IDENTIFIER = "ADBMOBILE_KEY_PUSH_TOKEN"
            const val PUSH_ENABLED = "ADBMOBILE_KEY_PUSH_ENABLED"
            const val AID_SYNCED = "ADOBEMOBILE_STOREDDEFAULTS_AID_SYNCED"
        }

        object Messages {
            const val SHARED_PREFERENCES_BLACK_LIST = "messagesBlackList"
        }

        object Configuration {
            const val GLOBAL_PRIVACY_KEY = "PrivacyStatus"
        }

        val DATABASE_NAMES = arrayListOf(
            "ADBMobile3rdPartyDataCache.sqlite", // signals
            "ADBMobilePIICache.sqlite", // signals pii
            "ADBMobileDataCache.sqlite", // analytics db
            "ADBMobileTimedActionsCache.sqlite" // analytics timed actions
        )
    }

    object V5 {

        object Lifecycle {
            const val DATASTORE_NAME = "AdobeMobile_Lifecycle"
            const val INSTALL_DATE = "InstallDate"
            const val UPGRADE_DATE = "UpgradeDate"
            const val LAST_USED_DATE = "LastDateUsed"
            const val LAUNCHES_AFTER_UPGRADE = "LaunchesAfterUpgrade"
            const val LAUNCHES = "Launches"
            const val LAST_VERSION = "LastVersion"
            const val START_DATE = "ADMS_SessionStart"
            const val PAUSE_DATE = "PauseDate"
            const val SUCCESFUL_CLOSE = "SuccessfulClose"
            const val OS = "OperatingSystem"
            const val APPLICATION_ID = "ApplicationId"
        }

        object Acquisition {
            const val DATASTORE_NAME = "Acquisition"
            const val REFERRER_DATA = "ADMS_Referrer_ContextData_Json_String"
        }

        object AudienceManager {
            const val DATASTORE_NAME = "AAMDataStore"
            const val USER_ID = "AAMUserId"
            const val USER_PROFILE = "AAMUserProfile"
        }

        object Target {
            const val DATASTORE_NAME = "ADOBEMOBILE_TARGET"
            const val THIRD_PARTY_ID = "THIRD_PARTY_ID"
            const val TNT_ID = "TNT_ID"
            const val SESSION_ID = "SESSION_ID"
            const val EDGE_HOST = "EDGE_HOST"
        }

        object Analytics {
            const val DATASTORE_NAME = "AnalyticsDataStorage"
            const val AID = "ADOBEMOBILE_STOREDDEFAULTS_AID"
            const val IGNORE_AID = "ADOBEMOBILE_STOREDDEFAULTS_IGNORE_AID"
            const val VID = "ADOBEMOBILE_STOREDDEFAULTS_VISITOR_IDENTIFIER"
        }

        object MobileServices {
            const val DATASTORE_NAME = "ADBMobileServices"
            const val DEFAULTS_KEY_INSTALLDATE = "ADMS_Legacy_InstallDate"
            const val REFERRER_DATA_JSON_STRING = "ADMS_Referrer_ContextData_Json_String"
            const val SHARED_PREFERENCES_BLACK_LIST = "messagesBlackList"
            const val DEFAULTS_KEY_REFERRER_UTM_SOURCE = "utm_source"
            const val DEFAULTS_KEY_REFERRER_UTM_MEDIUM = "utm_medium"
            const val DEFAULTS_KEY_REFERRER_UTM_TERM = "utm_term"
            const val DEFAULTS_KEY_REFERRER_UTM_CONTENT = "utm_content"
            const val DEFAULTS_KEY_REFERRER_UTM_CAMPAIGN = "utm_campaign"
            const val DEFAULTS_KEY_REFERRER_TRACKINGCODE = "trackingcode"
        }

        object Identity {
            const val DATASTORE_NAME = "visitorIDServiceDataStore"
            const val MID = "ADOBEMOBILE_PERSISTED_MID"
            const val BLOB = "ADOBEMOBILE_PERSISTED_MID_BLOB"
            const val HINT = "ADOBEMOBILE_PERSISTED_MID_HINT"
            const val VISITOR_IDS = "ADOBEMOBILE_VISITORID_IDS"
            const val VISITOR_ID = "ADOBEMOBILE_VISITOR_ID"
            const val PUSH_ENABLED = "ADOBEMOBILE_PUSH_ENABLED"
        }

        object Configuration {
            const val DATASTORE_NAME = "AdobeMobile_ConfigState"
            const val PERSISTED_OVERRIDDEN_CONFIG = "config.overridden.map"
            const val GLOBAL_PRIVACY_KEY = "global.privacy"
        }
    }
}
