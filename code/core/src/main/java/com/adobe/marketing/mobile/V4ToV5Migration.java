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

import android.content.Context;
import android.content.SharedPreferences;
import com.adobe.marketing.mobile.internal.CoreConstants;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.NamedCollection;
import com.adobe.marketing.mobile.services.ServiceProvider;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

class V4ToV5Migration {

    private static final String LOG_TAG = "Configuration";

    private static class V4 {

        private static final String DATASTORE_NAME = "APP_MEASUREMENT_CACHE";

        private static class Lifecycle {

            private static final String INSTALL_DATE = "ADMS_InstallDate";
            private static final String UPGRADE_DATE = "ADMS_UpgradeDate";
            private static final String LAST_USED_DATE = "ADMS_LastDateUsed";
            private static final String LAUNCHES_AFTER_UPGRADE = "ADMS_LaunchesAfterUpgrade";
            private static final String LAUNCHES = "ADMS_Launches";
            private static final String LAST_VERSION = "ADMS_LastVersion";
            private static final String START_DATE = "ADMS_SessionStart";
            private static final String PAUSE_DATE = "ADMS_PauseDate";
            private static final String SUCCESFUL_CLOSE = "ADMS_SuccessfulClose";
            private static final String OS = "ADOBEMOBILE_STOREDDEFAULTS_OS";
            private static final String APPLICATION_ID = "ADOBEMOBILE_STOREDDEFAULTS_APPID";
            private static final String CONTEXT_DATA = "ADMS_LifecycleData";

            private Lifecycle() {}
        }

        private static class Acquisition {

            private static final String REFERRER_DATA = "ADMS_Referrer_ContextData_Json_String";
            private static final String DEFAULTS_KEY_REFERRER_UTM_SOURCE = "utm_source";
            private static final String DEFAULTS_KEY_REFERRER_UTM_MEDIUM = "utm_medium";
            private static final String DEFAULTS_KEY_REFERRER_UTM_TERM = "utm_term";
            private static final String DEFAULTS_KEY_REFERRER_UTM_CONTENT = "utm_content";
            private static final String DEFAULTS_KEY_REFERRER_UTM_CAMPAIGN = "utm_campaign";
            private static final String DEFAULTS_KEY_REFERRER_TRACKINGCODE = "trackingcode";

            private Acquisition() {}
        }

        private static class AudienceManager {

            private static final String USER_ID = "AAMUserId";
            private static final String USER_PROFILE = "AAMUserProfile";

            private AudienceManager() {}
        }

        private static class Target {

            private static final String THIRD_PARTY_ID = "ADBMOBILE_TARGET_3RD_PARTY_ID";
            private static final String TNT_ID = "ADBMOBILE_TARGET_TNT_ID";
            private static final String LAST_TIMESTAMP = "ADBMOBILE_TARGET_LAST_TIMESTAMP";
            private static final String SESSION_ID = "ADBMOBILE_TARGET_SESSION_ID";
            private static final String EDGE_HOST = "ADBMOBILE_TARGET_EDGE_HOST";
            private static final String COOKIE_EXPIRES = "mboxPC_Expires";
            private static final String COOKIE_VALUE = "mboxPC_Value";

            private Target() {}
        }

        private static class Analytics {

            private static final String AID = "ADOBEMOBILE_STOREDDEFAULTS_AID";
            private static final String IGNORE_AID = "ADOBEMOBILE_STOREDDEFAULTS_IGNORE_AID";
            private static final String LAST_KNOWN_TIMESTAMP = "ADBLastKnownTimestampKey";

            private Analytics() {}
        }

        private static class Identity {

            private static final String MID = "ADBMOBILE_PERSISTED_MID";
            private static final String BLOB = "ADBMOBILE_PERSISTED_MID_BLOB";
            private static final String HINT = "ADBMOBILE_PERSISTED_MID_HINT";
            private static final String VISITOR_IDS = "ADBMOBILE_VISITORID_IDS";
            private static final String VISITOR_ID_SYNC = "ADBMOBILE_VISITORID_SYNC";
            private static final String VISITOR_ID_TTL = "ADBMOBILE_VISITORID_TTL";
            private static final String VISITOR_ID = "APP_MEASUREMENT_VISITOR_ID";
            private static final String ADVERTISING_IDENTIFIER =
                    "ADOBEMOBILE_STOREDDEFAULTS_ADVERTISING_IDENTIFIER";
            private static final String PUSH_IDENTIFIER = "ADBMOBILE_KEY_PUSH_TOKEN";
            private static final String PUSH_ENABLED = "ADBMOBILE_KEY_PUSH_ENABLED";
            private static final String AID_SYNCED = "ADOBEMOBILE_STOREDDEFAULTS_AID_SYNCED";

            private Identity() {}
        }

        private static class Messages {

            private static final String SHARED_PREFERENCES_BLACK_LIST = "messagesBlackList";

            private Messages() {}
        }

        private static class Configuration {

            private static final String GLOBAL_PRIVACY_KEY = "PrivacyStatus";

            private Configuration() {}
        }

        private V4() {}
    }

    private static class V5 {

        private static class Lifecycle {

            private static final String DATASTORE_NAME = "AdobeMobile_Lifecycle";
            private static final String INSTALL_DATE = "InstallDate";
            private static final String UPGRADE_DATE = "UpgradeDate";
            private static final String LAST_USED_DATE = "LastDateUsed";
            private static final String LAUNCHES_AFTER_UPGRADE = "LaunchesAfterUpgrade";
            private static final String LAUNCHES = "Launches";
            private static final String LAST_VERSION = "LastVersion";
            private static final String START_DATE = "ADMS_SessionStart";
            private static final String PAUSE_DATE = "PauseDate";
            private static final String SUCCESFUL_CLOSE = "SuccessfulClose";
            private static final String OS = "OperatingSystem";
            private static final String APPLICATION_ID = "ApplicationId";

            private Lifecycle() {}
        }

        private static class Acquisition {

            private static final String DATASTORE_NAME = "Acquisition";
            private static final String REFERRER_DATA = "ADMS_Referrer_ContextData_Json_String";

            private Acquisition() {}
        }

        private static class AudienceManager {

            private static final String DATASTORE_NAME = "AAMDataStore";
            private static final String USER_ID = "AAMUserId";
            private static final String USER_PROFILE = "AAMUserProfile";

            private AudienceManager() {}
        }

        private static class Target {

            private static final String DATASTORE_NAME = "ADOBEMOBILE_TARGET";
            private static final String THIRD_PARTY_ID = "THIRD_PARTY_ID";
            private static final String TNT_ID = "TNT_ID";
            private static final String SESSION_ID = "SESSION_ID";
            private static final String EDGE_HOST = "EDGE_HOST";

            private Target() {}
        }

        private static class Analytics {

            private static final String DATASTORE_NAME = "AnalyticsDataStorage";
            private static final String AID = "ADOBEMOBILE_STOREDDEFAULTS_AID";
            private static final String IGNORE_AID = "ADOBEMOBILE_STOREDDEFAULTS_IGNORE_AID";
            private static final String VID = "ADOBEMOBILE_STOREDDEFAULTS_VISITOR_IDENTIFIER";

            private Analytics() {}
        }

        private static class MobileServices {

            private static final String DATASTORE_NAME = "ADBMobileServices";
            private static final String DEFAULTS_KEY_INSTALLDATE = "ADMS_Legacy_InstallDate";
            private static final String REFERRER_DATA_JSON_STRING =
                    "ADMS_Referrer_ContextData_Json_String";
            private static final String SHARED_PREFERENCES_BLACK_LIST = "messagesBlackList";

            private static final String DEFAULTS_KEY_REFERRER_UTM_SOURCE = "utm_source";
            private static final String DEFAULTS_KEY_REFERRER_UTM_MEDIUM = "utm_medium";
            private static final String DEFAULTS_KEY_REFERRER_UTM_TERM = "utm_term";
            private static final String DEFAULTS_KEY_REFERRER_UTM_CONTENT = "utm_content";
            private static final String DEFAULTS_KEY_REFERRER_UTM_CAMPAIGN = "utm_campaign";
            private static final String DEFAULTS_KEY_REFERRER_TRACKINGCODE = "trackingcode";

            private MobileServices() {}
        }

        private static class Identity {

            private static final String DATASTORE_NAME = "visitorIDServiceDataStore";
            private static final String MID = "ADOBEMOBILE_PERSISTED_MID";
            private static final String BLOB = "ADOBEMOBILE_PERSISTED_MID_BLOB";
            private static final String HINT = "ADOBEMOBILE_PERSISTED_MID_HINT";
            private static final String VISITOR_IDS = "ADOBEMOBILE_VISITORID_IDS";
            private static final String VISITOR_ID = "ADOBEMOBILE_VISITOR_ID";
            private static final String PUSH_ENABLED = "ADOBEMOBILE_PUSH_ENABLED";

            private Identity() {}
        }

        private static class Configuration {

            private static final String DATASTORE_NAME = "AdobeMobile_ConfigState";
            private static final String PERSISTED_OVERRIDDEN_CONFIG = "config.overridden.map";
            private static final String GLOBAL_PRIVACY_KEY = "global.privacy";

            private Configuration() {}
        }

        private V5() {}
    }

    private static SharedPreferences prefs = null;

    protected void migrate() {
        if (isMigrationRequired()) {
            Log.debug(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "Migrating Adobe SDK v4 SharedPreferences for use with Adobe SDK v5.");
            migrateLocalStorage();
            migrateConfigurationLocalStorage();
            removeV4Databases();
            Log.debug(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "Full migrating of SharedPreferences successful.");
        } else if (isConfigurationMigrationRequired()) {
            Log.debug(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "Migrating Adobe SDK v4 Configuration SharedPreferences for use with Adobe SDK"
                            + " v5.");
            migrateConfigurationLocalStorage();
            Log.debug(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "Full migrating of v4 Configuration SharedPreferences successful.");
        }

        if (isVisitorIdMigrationRequired()) {
            Log.debug(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "Migrating visitor identifier from Identity to Analytics.");
            migrateVisitorId();
            Log.debug(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "Full migration of visitor identifier from Identity to Analytics successful.");
        }
    }

    private void migrateLocalStorage() {
        SharedPreferences v4DataStore = getV4SharedPreferences();

        if (v4DataStore == null) {
            Log.debug(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "%s (application context), failed to migrate v4 storage",
                    Log.UNEXPECTED_NULL_VALUE);
            return;
        }

        SharedPreferences.Editor v4DataStoreEditor = v4DataStore.edit();

        // mobile services
        NamedCollection mobileServicesV5DataStore =
                ServiceProvider.getInstance()
                        .getDataStoreService()
                        .getNamedCollection(V5.MobileServices.DATASTORE_NAME);
        long installDateMillis = v4DataStore.getLong(V4.Lifecycle.INSTALL_DATE, 0L);

        if (installDateMillis > 0) {
            // convert milliseconds to seconds as it is handled in v5
            mobileServicesV5DataStore.setLong(
                    V5.MobileServices.DEFAULTS_KEY_INSTALLDATE, convertMsToSec(installDateMillis));
        }

        mobileServicesV5DataStore.setString(
                V5.MobileServices.REFERRER_DATA_JSON_STRING,
                v4DataStore.getString(V4.Acquisition.REFERRER_DATA, null));

        mobileServicesV5DataStore.setString(
                V5.MobileServices.DEFAULTS_KEY_REFERRER_UTM_SOURCE,
                v4DataStore.getString(V4.Acquisition.DEFAULTS_KEY_REFERRER_UTM_SOURCE, null));
        mobileServicesV5DataStore.setString(
                V5.MobileServices.DEFAULTS_KEY_REFERRER_UTM_MEDIUM,
                v4DataStore.getString(V4.Acquisition.DEFAULTS_KEY_REFERRER_UTM_MEDIUM, null));
        mobileServicesV5DataStore.setString(
                V5.MobileServices.DEFAULTS_KEY_REFERRER_UTM_TERM,
                v4DataStore.getString(V4.Acquisition.DEFAULTS_KEY_REFERRER_UTM_TERM, null));
        mobileServicesV5DataStore.setString(
                V5.MobileServices.DEFAULTS_KEY_REFERRER_UTM_CONTENT,
                v4DataStore.getString(V4.Acquisition.DEFAULTS_KEY_REFERRER_UTM_CONTENT, null));
        mobileServicesV5DataStore.setString(
                V5.MobileServices.DEFAULTS_KEY_REFERRER_UTM_CAMPAIGN,
                v4DataStore.getString(V4.Acquisition.DEFAULTS_KEY_REFERRER_UTM_CAMPAIGN, null));
        mobileServicesV5DataStore.setString(
                V5.MobileServices.DEFAULTS_KEY_REFERRER_TRACKINGCODE,
                v4DataStore.getString(V4.Acquisition.DEFAULTS_KEY_REFERRER_TRACKINGCODE, null));

        mobileServicesV5DataStore.setString(
                V5.MobileServices.SHARED_PREFERENCES_BLACK_LIST,
                v4DataStore.getString(V4.Messages.SHARED_PREFERENCES_BLACK_LIST, null));

        // don't remove V4.Acquisition.REFERRER_DATA at here, it will be removed by the acquisition
        // extension
        // v4DataStoreEditor.remove(V4.Acquisition.REFERRER_DATA);

        v4DataStoreEditor.remove(V4.Acquisition.DEFAULTS_KEY_REFERRER_UTM_SOURCE);
        v4DataStoreEditor.remove(V4.Acquisition.DEFAULTS_KEY_REFERRER_UTM_MEDIUM);
        v4DataStoreEditor.remove(V4.Acquisition.DEFAULTS_KEY_REFERRER_UTM_TERM);
        v4DataStoreEditor.remove(V4.Acquisition.DEFAULTS_KEY_REFERRER_UTM_CONTENT);
        v4DataStoreEditor.remove(V4.Acquisition.DEFAULTS_KEY_REFERRER_UTM_CAMPAIGN);
        v4DataStoreEditor.remove(V4.Acquisition.DEFAULTS_KEY_REFERRER_TRACKINGCODE);
        v4DataStoreEditor.remove(V4.Messages.SHARED_PREFERENCES_BLACK_LIST);
        v4DataStoreEditor.apply();

        Log.debug(CoreConstants.LOG_TAG, LOG_TAG, "Migration complete for Mobile Services data.");

        // acquisition
        NamedCollection acquisitionV5DataStore =
                ServiceProvider.getInstance()
                        .getDataStoreService()
                        .getNamedCollection(V5.Acquisition.DATASTORE_NAME);
        acquisitionV5DataStore.setString(
                V5.Acquisition.REFERRER_DATA,
                v4DataStore.getString(V4.Acquisition.REFERRER_DATA, null));
        v4DataStoreEditor.remove(V4.Acquisition.REFERRER_DATA);
        v4DataStoreEditor.apply();
        Log.debug(CoreConstants.LOG_TAG, LOG_TAG, "Migration complete for Acquisition data.");

        // analytics
        NamedCollection analyticsV5DataStore =
                ServiceProvider.getInstance()
                        .getDataStoreService()
                        .getNamedCollection(V5.Analytics.DATASTORE_NAME);
        analyticsV5DataStore.setString(
                V5.Analytics.AID, getV4SharedPreferences().getString(V4.Analytics.AID, null));
        analyticsV5DataStore.setBoolean(
                V5.Analytics.IGNORE_AID,
                getV4SharedPreferences().getBoolean(V4.Analytics.IGNORE_AID, false));
        analyticsV5DataStore.setString(
                V5.Analytics.VID, getV4SharedPreferences().getString(V4.Identity.VISITOR_ID, null));

        v4DataStoreEditor.remove(V4.Analytics.AID);
        v4DataStoreEditor.remove(V4.Analytics.IGNORE_AID);
        v4DataStoreEditor.remove(V4.Analytics.LAST_KNOWN_TIMESTAMP);
        v4DataStoreEditor.apply();
        Log.debug(CoreConstants.LOG_TAG, LOG_TAG, "Migration complete for Analytics data.");

        // audience manager
        NamedCollection audienceV5DataStore =
                ServiceProvider.getInstance()
                        .getDataStoreService()
                        .getNamedCollection(V5.AudienceManager.DATASTORE_NAME);
        audienceV5DataStore.setString(
                V5.AudienceManager.USER_ID,
                v4DataStore.getString(V4.AudienceManager.USER_ID, null));
        v4DataStoreEditor.remove(V4.AudienceManager.USER_ID);
        v4DataStoreEditor.remove(V4.AudienceManager.USER_PROFILE);
        v4DataStoreEditor.apply();
        Log.debug(CoreConstants.LOG_TAG, LOG_TAG, "Migration complete for Audience Manager data.");

        // identity
        NamedCollection identityV5DataStore =
                ServiceProvider.getInstance()
                        .getDataStoreService()
                        .getNamedCollection(V5.Identity.DATASTORE_NAME);
        identityV5DataStore.setString(
                V5.Identity.MID, v4DataStore.getString(V4.Identity.MID, null));
        identityV5DataStore.setString(
                V5.Identity.BLOB, v4DataStore.getString(V4.Identity.BLOB, null));
        identityV5DataStore.setString(
                V5.Identity.HINT, v4DataStore.getString(V4.Identity.HINT, null));
        identityV5DataStore.setString(
                V5.Identity.VISITOR_IDS, v4DataStore.getString(V4.Identity.VISITOR_IDS, null));
        identityV5DataStore.setBoolean(
                V5.Identity.PUSH_ENABLED, v4DataStore.getBoolean(V4.Identity.PUSH_ENABLED, false));
        v4DataStoreEditor.remove(V4.Identity.MID);
        v4DataStoreEditor.remove(V4.Identity.BLOB);
        v4DataStoreEditor.remove(V4.Identity.HINT);
        v4DataStoreEditor.remove(V4.Identity.VISITOR_ID);
        v4DataStoreEditor.remove(V4.Identity.VISITOR_IDS);
        v4DataStoreEditor.remove(V4.Identity.VISITOR_ID_SYNC);
        v4DataStoreEditor.remove(V4.Identity.VISITOR_ID_TTL);
        v4DataStoreEditor.remove(V4.Identity.ADVERTISING_IDENTIFIER);
        v4DataStoreEditor.remove(V4.Identity.PUSH_IDENTIFIER);
        v4DataStoreEditor.remove(V4.Identity.PUSH_ENABLED);
        v4DataStoreEditor.remove(V4.Identity.AID_SYNCED);
        v4DataStoreEditor.apply();
        Log.debug(
                CoreConstants.LOG_TAG,
                LOG_TAG,
                "Migration complete for Identity (Visitor ID Service) data.");

        // lifecycle
        NamedCollection lifecycleV5DataStore =
                ServiceProvider.getInstance()
                        .getDataStoreService()
                        .getNamedCollection(V5.Lifecycle.DATASTORE_NAME);

        if (installDateMillis > 0) {
            // convert milliseconds to seconds as it is handled in v5
            lifecycleV5DataStore.setLong(
                    V5.Lifecycle.INSTALL_DATE, convertMsToSec(installDateMillis));
        }

        lifecycleV5DataStore.setString(
                V5.Lifecycle.LAST_VERSION, v4DataStore.getString(V4.Lifecycle.LAST_VERSION, null));
        long lastUsedDateMillis = v4DataStore.getLong(V4.Lifecycle.LAST_USED_DATE, 0L);

        if (lastUsedDateMillis > 0) {
            lifecycleV5DataStore.setLong(
                    V5.Lifecycle.LAST_USED_DATE, convertMsToSec(lastUsedDateMillis));
        }

        lifecycleV5DataStore.setInt(
                V5.Lifecycle.LAUNCHES, v4DataStore.getInt(V4.Lifecycle.LAUNCHES, 0));
        lifecycleV5DataStore.setBoolean(
                V5.Lifecycle.SUCCESFUL_CLOSE,
                v4DataStore.getBoolean(V4.Lifecycle.SUCCESFUL_CLOSE, false));
        v4DataStoreEditor.remove(V4.Lifecycle.INSTALL_DATE);
        v4DataStoreEditor.remove(V4.Lifecycle.LAST_VERSION);
        v4DataStoreEditor.remove(V4.Lifecycle.LAST_USED_DATE);
        v4DataStoreEditor.remove(V4.Lifecycle.LAUNCHES);
        v4DataStoreEditor.remove(V4.Lifecycle.SUCCESFUL_CLOSE);
        v4DataStoreEditor.remove(V4.Lifecycle.CONTEXT_DATA);
        v4DataStoreEditor.remove(V4.Lifecycle.START_DATE);
        v4DataStoreEditor.remove(V4.Lifecycle.PAUSE_DATE);
        v4DataStoreEditor.remove(V4.Lifecycle.LAUNCHES_AFTER_UPGRADE);
        v4DataStoreEditor.remove(V4.Lifecycle.UPGRADE_DATE);
        v4DataStoreEditor.remove(V4.Lifecycle.OS);
        v4DataStoreEditor.remove(V4.Lifecycle.APPLICATION_ID);
        v4DataStoreEditor.apply();
        Log.debug(CoreConstants.LOG_TAG, LOG_TAG, "Migration complete for Lifecycle data.");

        // target
        NamedCollection targetV5DataStore =
                ServiceProvider.getInstance()
                        .getDataStoreService()
                        .getNamedCollection(V5.Target.DATASTORE_NAME);
        targetV5DataStore.setString(
                V5.Target.TNT_ID, v4DataStore.getString(V4.Target.TNT_ID, null));
        targetV5DataStore.setString(
                V5.Target.THIRD_PARTY_ID, v4DataStore.getString(V4.Target.THIRD_PARTY_ID, null));
        targetV5DataStore.setString(
                V5.Target.SESSION_ID, v4DataStore.getString(V4.Target.SESSION_ID, null));
        targetV5DataStore.setString(
                V5.Target.EDGE_HOST, v4DataStore.getString(V4.Target.EDGE_HOST, null));
        v4DataStoreEditor.remove(V4.Target.TNT_ID);
        v4DataStoreEditor.remove(V4.Target.THIRD_PARTY_ID);
        v4DataStoreEditor.remove(V4.Target.SESSION_ID);
        v4DataStoreEditor.remove(V4.Target.EDGE_HOST);
        v4DataStoreEditor.remove(V4.Target.LAST_TIMESTAMP);
        v4DataStoreEditor.remove(V4.Target.COOKIE_EXPIRES);
        v4DataStoreEditor.remove(V4.Target.COOKIE_VALUE);
        v4DataStoreEditor.apply();
        Log.debug(CoreConstants.LOG_TAG, LOG_TAG, "Migrating complete for Target data.");
    }

    @SuppressWarnings("checkstyle:NestedIfDepth")
    private void migrateConfigurationLocalStorage() {
        SharedPreferences v4DataStore = getV4SharedPreferences();

        if (v4DataStore == null) {
            Log.debug(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "%s (application context), failed to migrate v4 storage",
                    Log.UNEXPECTED_NULL_VALUE);
            return;
        }

        SharedPreferences.Editor v4DataStoreEditor = v4DataStore.edit();

        // Configuration
        NamedCollection configurationV5DataStore =
                ServiceProvider.getInstance()
                        .getDataStoreService()
                        .getNamedCollection(V5.Configuration.DATASTORE_NAME);
        int v4PrivacyStatus = v4DataStore.getInt(V4.Configuration.GLOBAL_PRIVACY_KEY, -1);

        if (v4PrivacyStatus >= 0 && v4PrivacyStatus <= 2) {
            MobilePrivacyStatus v5PrivacyStatus;

            switch (v4PrivacyStatus) {
                case 0: // v4 OptIn
                    v5PrivacyStatus = MobilePrivacyStatus.OPT_IN;
                    break;
                case 1: // v4 OptOut
                    v5PrivacyStatus = MobilePrivacyStatus.OPT_OUT;
                    break;
                case 2: // v4 Unknown
                default:
                    v5PrivacyStatus = MobilePrivacyStatus.UNKNOWN;
                    break;
            }

            String v5OverriddenConfig =
                    configurationV5DataStore.getString(
                            V5.Configuration.PERSISTED_OVERRIDDEN_CONFIG, null);

            if (v5OverriddenConfig != null) {
                try {
                    JSONObject v5JsonObj = new JSONObject(v5OverriddenConfig);

                    if (!v5JsonObj.has(V5.Configuration.GLOBAL_PRIVACY_KEY)) {
                        // V5 has overridden config data, but global privacy is not set, migrate v4
                        // value
                        v5JsonObj.put(
                                V5.Configuration.GLOBAL_PRIVACY_KEY, v5PrivacyStatus.getValue());
                        configurationV5DataStore.setString(
                                V5.Configuration.PERSISTED_OVERRIDDEN_CONFIG, v5JsonObj.toString());
                    } else {
                        Log.debug(
                                CoreConstants.LOG_TAG,
                                LOG_TAG,
                                "V5 configuration data already contains setting for global"
                                        + " privacy. V4 global privacy not migrated.");
                    }
                } catch (JSONException e) {
                    Log.error(
                            CoreConstants.LOG_TAG,
                            LOG_TAG,
                            "Failed to serialize v5 configuration data. Unable to migrate v4"
                                    + " configuration data to v5. %s",
                            e.getLocalizedMessage());
                }
            } else {
                // V5 does not contain overridden data, so add one with just migrated privacy status
                Map<String, Object> v5ConfigMap = new HashMap<>();
                v5ConfigMap.put(V5.Configuration.GLOBAL_PRIVACY_KEY, v5PrivacyStatus.getValue());

                JSONObject v5JsonObj = new JSONObject(v5ConfigMap);
                configurationV5DataStore.setString(
                        V5.Configuration.PERSISTED_OVERRIDDEN_CONFIG, v5JsonObj.toString());
            }
        }

        v4DataStoreEditor.remove(V4.Configuration.GLOBAL_PRIVACY_KEY);
        v4DataStoreEditor.apply();
        Log.debug(CoreConstants.LOG_TAG, LOG_TAG, "Migration complete for Configuration data.");
    }

    private void migrateVisitorId() {
        NamedCollection identityV5DataStore =
                ServiceProvider.getInstance()
                        .getDataStoreService()
                        .getNamedCollection(V5.Identity.DATASTORE_NAME);
        NamedCollection analyticsV5DataStore =
                ServiceProvider.getInstance()
                        .getDataStoreService()
                        .getNamedCollection(V5.Analytics.DATASTORE_NAME);

        if (identityV5DataStore == null || analyticsV5DataStore == null) {
            Log.debug(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "%s (Identity or Analytics data store), failed to migrate visitor id.",
                    Log.UNEXPECTED_NULL_VALUE);
            return;
        }

        if (!analyticsV5DataStore.contains(V5.Analytics.VID)) {
            String vid = identityV5DataStore.getString(V5.Identity.VISITOR_ID, null);
            analyticsV5DataStore.setString(V5.Analytics.VID, vid);
        }

        identityV5DataStore.remove(V5.Identity.VISITOR_ID);
    }

    private void removeV4Databases() {
        List<String> databaseNames = new ArrayList<String>();
        databaseNames.add("ADBMobile3rdPartyDataCache.sqlite"); // signals
        databaseNames.add("ADBMobilePIICache.sqlite"); // signals pii
        databaseNames.add("ADBMobileDataCache.sqlite"); // analytics db
        databaseNames.add("ADBMobileTimedActionsCache.sqlite"); // analytics timed actions

        File cacheDirectory =
                ServiceProvider.getInstance().getDeviceInfoService().getApplicationCacheDir();
        if (cacheDirectory == null) {
            Log.debug(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "%s (cache directory), failed to delete V4 databases",
                    Log.UNEXPECTED_NULL_VALUE);
            return;
        }

        for (String databaseName : databaseNames) {
            try {
                File databaseFile = new File(cacheDirectory, databaseName);

                if (databaseFile.exists() && databaseFile.delete()) {
                    Log.debug(
                            CoreConstants.LOG_TAG,
                            LOG_TAG,
                            "Removed V4 database %s successfully",
                            databaseName);
                }
            } catch (SecurityException e) {
                Log.debug(
                        CoreConstants.LOG_TAG,
                        LOG_TAG,
                        "Failed to delete V4 database with name %s (%s)",
                        databaseName,
                        e);
                continue;
            }
        }
    }

    private boolean isMigrationRequired() {
        SharedPreferences sharedPrefs = getV4SharedPreferences();

        if (sharedPrefs == null) {
            Log.debug(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "%s (application context), failed to migrate v4 data",
                    Log.UNEXPECTED_NULL_VALUE);
            return false;
        }

        return sharedPrefs.contains(V4.Lifecycle.INSTALL_DATE);
    }

    private boolean isConfigurationMigrationRequired() {
        SharedPreferences sharedPrefs = getV4SharedPreferences();

        if (sharedPrefs == null) {
            Log.debug(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "%s (application context), failed to migrate v4 configuration data",
                    Log.UNEXPECTED_NULL_VALUE);
            return false;
        }

        return sharedPrefs.contains(V4.Configuration.GLOBAL_PRIVACY_KEY);
    }

    private boolean isVisitorIdMigrationRequired() {
        NamedCollection identityV5DataStore =
                ServiceProvider.getInstance()
                        .getDataStoreService()
                        .getNamedCollection(V5.Identity.DATASTORE_NAME);

        if (identityV5DataStore == null) {
            Log.debug(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "%s (application context), failed to migrate v5 visitor identifier",
                    Log.UNEXPECTED_NULL_VALUE);
            return false;
        }

        return identityV5DataStore.contains(V5.Identity.VISITOR_ID);
    }

    private static SharedPreferences getV4SharedPreferences() {
        if (prefs == null) {
            Context appContext =
                    ServiceProvider.getInstance().getAppContextService().getApplicationContext();

            if (appContext != null) {
                prefs = appContext.getSharedPreferences(V4.DATASTORE_NAME, 0);
            }
        }

        return prefs;
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    private long convertMsToSec(final long timestampMs) {
        return timestampMs / 1000;
    }
}
