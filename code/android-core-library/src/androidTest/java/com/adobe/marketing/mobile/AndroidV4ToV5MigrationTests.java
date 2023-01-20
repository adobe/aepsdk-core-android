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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.test.core.app.ApplicationProvider;
import com.adobe.marketing.mobile.services.MockAppContextService;
import com.adobe.marketing.mobile.services.NamedCollection;
import com.adobe.marketing.mobile.services.ServiceProvider;
import com.adobe.marketing.mobile.services.ServiceProviderModifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.Assert;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AndroidV4ToV5MigrationTests {

    private static class V4 {

        private static final String DATASTORE_NAME = "APP_MEASUREMENT_CACHE";

        private static final String MESSAGES_BLACKLIST = "messagesBlackList";
        private static final String LIFETIME_VALUE = "ADB_LIFETIME_VALUE";

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

        private static class Identity {

            private static final String DATASTORE_NAME = "visitorIDServiceDataStore";
            private static final String MID = "ADOBEMOBILE_PERSISTED_MID";
            private static final String BLOB = "ADOBEMOBILE_PERSISTED_MID_BLOB";
            private static final String HINT = "ADOBEMOBILE_PERSISTED_MID_HINT";
            private static final String VISITOR_IDS = "ADOBEMOBILE_VISITORID_IDS";
            private static final String VISITORID_SYNC = "ADOBEMOBILE_VISITORID_SYNC";
            private static final String VISITORID_TTL = "ADOBEMOBILE_VISITORID_TTL";
            private static final String VISITOR_ID = "ADOBEMOBILE_VISITOR_ID";
            private static final String ADVERTISING_IDENTIFIER =
                    "ADOBEMOBILE_ADVERTISING_IDENTIFIER";
            private static final String PUSH_IDENTIFIER = "ADOBEMOBILE_PUSH_IDENTIFIER";
            private static final String PUSH_ENABLED = "ADOBEMOBILE_PUSH_ENABLED";

            private Identity() {}
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

        private static class Configuration {

            private static final String DATASTORE_NAME = "AdobeMobile_ConfigState";
            private static final String PERSISTED_OVERRIDDEN_CONFIG = "config.overridden.map";
            private static final String GLOBAL_PRIVACY_KEY = "global.privacy";

            private Configuration() {}
        }

        private V5() {}
    }

    private SharedPreferences v4DataStore;
    private SharedPreferences.Editor v4DataStoreEditor;
    private V4ToV5Migration migrationTool;
    private MockAppContextService mockAppContextService;

    @Before
    public void setup() {
        Context context = ApplicationProvider.getApplicationContext();

        mockAppContextService = new MockAppContextService();
        mockAppContextService.appContext = context;
        ServiceProviderModifier.setAppContextService(mockAppContextService);

        migrationTool = new V4ToV5Migration();
        v4DataStore = context.getSharedPreferences(V4.DATASTORE_NAME, 0);
        v4DataStoreEditor = v4DataStore.edit();
        v4DataStoreEditor.clear();
        v4DataStoreEditor.commit();
    }

    @After
    public void tearDown() {
        List<String> stores = new ArrayList<>();
        stores.add(V5.Acquisition.DATASTORE_NAME);
        stores.add(V5.Analytics.DATASTORE_NAME);
        stores.add(V5.AudienceManager.DATASTORE_NAME);
        stores.add(V5.Target.DATASTORE_NAME);
        stores.add(V5.Identity.DATASTORE_NAME);
        stores.add(V5.Configuration.DATASTORE_NAME);
        stores.add(V5.MobileServices.DATASTORE_NAME);
        stores.add(V5.Lifecycle.DATASTORE_NAME);

        NamedCollection dataStore;

        for (String store : stores) {
            dataStore =
                    ServiceProvider.getInstance().getDataStoreService().getNamedCollection(store);

            if (dataStore != null) {
                dataStore.removeAll();
            }
        }
    }

    @Test
    public void testDataMigration_ExistingV4Data() {
        // mock acquisition data
        v4DataStoreEditor.putString(V4.Acquisition.REFERRER_DATA, "{\"acqkey\":\"acqvalue\"}");
        v4DataStoreEditor.putString(V4.Acquisition.DEFAULTS_KEY_REFERRER_UTM_SOURCE, "utm_source");
        v4DataStoreEditor.putString(V4.Acquisition.DEFAULTS_KEY_REFERRER_UTM_MEDIUM, "utm_medium");
        v4DataStoreEditor.putString(V4.Acquisition.DEFAULTS_KEY_REFERRER_UTM_TERM, "utm_term");
        v4DataStoreEditor.putString(
                V4.Acquisition.DEFAULTS_KEY_REFERRER_UTM_CONTENT, "utm_content");
        v4DataStoreEditor.putString(
                V4.Acquisition.DEFAULTS_KEY_REFERRER_UTM_CAMPAIGN, "utm_campaign");
        v4DataStoreEditor.putString(
                V4.Acquisition.DEFAULTS_KEY_REFERRER_TRACKINGCODE, "trackingcode");

        // mock messages data
        v4DataStoreEditor.putString(V4.Messages.SHARED_PREFERENCES_BLACK_LIST, "blacklist");

        // mock analytics data
        v4DataStoreEditor.putString(V4.Analytics.AID, "aid");
        v4DataStoreEditor.putBoolean(V4.Analytics.IGNORE_AID, true);
        v4DataStoreEditor.putLong(V4.Analytics.LAST_KNOWN_TIMESTAMP, 12345);

        // mock audience data
        v4DataStoreEditor.putString(V4.AudienceManager.USER_ID, "aamUserId");
        v4DataStoreEditor.putString(V4.AudienceManager.USER_PROFILE, "{\"aamkey\": \"aamvalue\"}");

        // mock identity data
        v4DataStoreEditor.putBoolean(V4.Identity.AID_SYNCED, true);
        v4DataStoreEditor.putString(V4.Identity.MID, "identityMid");
        v4DataStoreEditor.putString(V4.Identity.BLOB, "blob");
        v4DataStoreEditor.putString(V4.Identity.HINT, "hint");
        v4DataStoreEditor.putString(V4.Identity.VISITOR_ID, "vid");
        v4DataStoreEditor.putLong(V4.Identity.VISITOR_ID_TTL, 12345);
        v4DataStoreEditor.putLong(V4.Identity.VISITOR_ID_SYNC, 123456);
        v4DataStoreEditor.putString(V4.Identity.VISITOR_IDS, "identityIds");
        v4DataStoreEditor.putString(V4.Identity.PUSH_IDENTIFIER, "pushtoken");
        v4DataStoreEditor.putBoolean(V4.Identity.PUSH_ENABLED, true);

        // mock lifecycle data
        v4DataStoreEditor.putLong(V4.Lifecycle.INSTALL_DATE, 123456);
        v4DataStoreEditor.putLong(V4.Lifecycle.UPGRADE_DATE, 123456);
        v4DataStoreEditor.putLong(V4.Lifecycle.LAST_USED_DATE, 123456);
        v4DataStoreEditor.putString(V4.Lifecycle.OS, "os");
        v4DataStoreEditor.putString(V4.Lifecycle.APPLICATION_ID, "appid");
        v4DataStoreEditor.putInt(V4.Lifecycle.LAUNCHES, 552);
        v4DataStoreEditor.putInt(V4.Lifecycle.LAUNCHES_AFTER_UPGRADE, 3);
        v4DataStoreEditor.putBoolean(V4.Lifecycle.SUCCESFUL_CLOSE, true);
        v4DataStoreEditor.putLong(V4.Lifecycle.PAUSE_DATE, 123456);
        v4DataStoreEditor.putLong(V4.Lifecycle.START_DATE, 123456);
        v4DataStoreEditor.putString(V4.Lifecycle.LAST_VERSION, "version");
        v4DataStoreEditor.putString(
                V4.Lifecycle.CONTEXT_DATA, "{\"lifecyclekey\": \"lifecyclevalue\"}");

        // mock target data
        v4DataStoreEditor.putString(V4.Target.TNT_ID, "tntid");
        v4DataStoreEditor.putString(V4.Target.THIRD_PARTY_ID, "3rdpartyid");
        v4DataStoreEditor.putLong(V4.Target.LAST_TIMESTAMP, 123456);
        v4DataStoreEditor.putLong(V4.Target.COOKIE_EXPIRES, 123456);
        v4DataStoreEditor.putString(V4.Target.COOKIE_VALUE, "cookie");
        v4DataStoreEditor.putString(V4.Target.SESSION_ID, "sessionid");
        v4DataStoreEditor.putString(V4.Target.EDGE_HOST, "edgehost");

        // mock configuration data
        v4DataStoreEditor.putInt(V4.Configuration.GLOBAL_PRIVACY_KEY, 1); // OptOut

        v4DataStoreEditor.commit();

        // test
        migrationTool.migrate();

        // verify v4 keys have been removed
        assertFalse(v4DataStore.contains(V4.Acquisition.REFERRER_DATA));
        assertFalse(v4DataStore.contains(V4.Analytics.AID));
        assertFalse(v4DataStore.contains(V4.Analytics.IGNORE_AID));
        assertFalse(v4DataStore.contains(V4.Analytics.LAST_KNOWN_TIMESTAMP));
        assertFalse(v4DataStore.contains(V4.AudienceManager.USER_ID));
        assertFalse(v4DataStore.contains(V4.AudienceManager.USER_PROFILE));
        assertFalse(v4DataStore.contains(V4.Identity.AID_SYNCED));
        assertFalse(v4DataStore.contains(V4.Identity.MID));
        assertFalse(v4DataStore.contains(V4.Identity.BLOB));
        assertFalse(v4DataStore.contains(V4.Identity.HINT));
        assertFalse(v4DataStore.contains(V4.Identity.VISITOR_ID));
        assertFalse(v4DataStore.contains(V4.Identity.VISITOR_ID_TTL));
        assertFalse(v4DataStore.contains(V4.Identity.VISITOR_ID_SYNC));
        assertFalse(v4DataStore.contains(V4.Identity.VISITOR_IDS));
        assertFalse(v4DataStore.contains(V4.Identity.PUSH_IDENTIFIER));
        assertFalse(v4DataStore.contains(V4.Identity.PUSH_ENABLED));
        assertFalse(v4DataStore.contains(V4.Lifecycle.INSTALL_DATE));
        assertFalse(v4DataStore.contains(V4.Lifecycle.UPGRADE_DATE));
        assertFalse(v4DataStore.contains(V4.Lifecycle.LAST_USED_DATE));
        assertFalse(v4DataStore.contains(V4.Lifecycle.OS));
        assertFalse(v4DataStore.contains(V4.Lifecycle.APPLICATION_ID));
        assertFalse(v4DataStore.contains(V4.Lifecycle.LAUNCHES));
        assertFalse(v4DataStore.contains(V4.Lifecycle.LAUNCHES_AFTER_UPGRADE));
        assertFalse(v4DataStore.contains(V4.Lifecycle.SUCCESFUL_CLOSE));
        assertFalse(v4DataStore.contains(V4.Lifecycle.PAUSE_DATE));
        assertFalse(v4DataStore.contains(V4.Lifecycle.START_DATE));
        assertFalse(v4DataStore.contains(V4.Lifecycle.LAST_VERSION));
        assertFalse(v4DataStore.contains(V4.Lifecycle.CONTEXT_DATA));
        assertFalse(v4DataStore.contains(V4.Target.TNT_ID));
        assertFalse(v4DataStore.contains(V4.Target.THIRD_PARTY_ID));
        assertFalse(v4DataStore.contains(V4.Target.LAST_TIMESTAMP));
        assertFalse(v4DataStore.contains(V4.Target.COOKIE_EXPIRES));
        assertFalse(v4DataStore.contains(V4.Target.COOKIE_VALUE));
        assertFalse(v4DataStore.contains(V4.Target.SESSION_ID));
        assertFalse(v4DataStore.contains(V4.Target.EDGE_HOST));
        assertFalse(v4DataStore.contains(V4.Acquisition.REFERRER_DATA));
        assertFalse(v4DataStore.contains(V4.Acquisition.DEFAULTS_KEY_REFERRER_UTM_SOURCE));
        assertFalse(v4DataStore.contains(V4.Acquisition.DEFAULTS_KEY_REFERRER_UTM_MEDIUM));
        assertFalse(v4DataStore.contains(V4.Acquisition.DEFAULTS_KEY_REFERRER_UTM_TERM));
        assertFalse(v4DataStore.contains(V4.Acquisition.DEFAULTS_KEY_REFERRER_UTM_CONTENT));
        assertFalse(v4DataStore.contains(V4.Acquisition.DEFAULTS_KEY_REFERRER_UTM_CAMPAIGN));
        assertFalse(v4DataStore.contains(V4.Acquisition.DEFAULTS_KEY_REFERRER_TRACKINGCODE));
        assertFalse(v4DataStore.contains(V4.Messages.SHARED_PREFERENCES_BLACK_LIST));
        assertFalse(v4DataStore.contains(V4.Configuration.GLOBAL_PRIVACY_KEY));

        // verify v5 data was set
        NamedCollection v5Acquisition =
                ServiceProvider.getInstance()
                        .getDataStoreService()
                        .getNamedCollection(V5.Acquisition.DATASTORE_NAME);
        assertEquals(
                "{\"acqkey\":\"acqvalue\"}",
                v5Acquisition.getString(V5.Acquisition.REFERRER_DATA, null));

        NamedCollection v5Analytics =
                ServiceProvider.getInstance()
                        .getDataStoreService()
                        .getNamedCollection(V5.Analytics.DATASTORE_NAME);
        assertEquals("aid", v5Analytics.getString(V5.Analytics.AID, null));
        assertEquals("vid", v5Analytics.getString(V5.Analytics.VID, null));
        assertEquals(true, v5Analytics.getBoolean(V5.Analytics.IGNORE_AID, false));

        NamedCollection v5AudienceManager =
                ServiceProvider.getInstance()
                        .getDataStoreService()
                        .getNamedCollection(V5.AudienceManager.DATASTORE_NAME);
        assertEquals("aamUserId", v5AudienceManager.getString(V5.AudienceManager.USER_ID, null));
        assertEquals(null, v5AudienceManager.getString(V5.AudienceManager.USER_PROFILE, null));

        NamedCollection v5Identity =
                ServiceProvider.getInstance()
                        .getDataStoreService()
                        .getNamedCollection(V5.Identity.DATASTORE_NAME);
        assertEquals("identityMid", v5Identity.getString(V5.Identity.MID, null));
        assertEquals("identityIds", v5Identity.getString(V5.Identity.VISITOR_IDS, null));
        assertEquals("blob", v5Identity.getString(V5.Identity.BLOB, null));
        assertEquals("hint", v5Identity.getString(V5.Identity.HINT, null));
        assertEquals(true, v5Identity.getBoolean(V5.Identity.PUSH_ENABLED, false));

        NamedCollection v5Lifecycle =
                ServiceProvider.getInstance()
                        .getDataStoreService()
                        .getNamedCollection(V5.Lifecycle.DATASTORE_NAME);
        assertEquals(123, v5Lifecycle.getLong(V5.Lifecycle.INSTALL_DATE, 0));
        assertEquals(552, v5Lifecycle.getInt(V5.Lifecycle.LAUNCHES, 0));
        assertEquals("version", v5Lifecycle.getString(V5.Lifecycle.LAST_VERSION, null));
        assertEquals(123, v5Lifecycle.getLong(V5.Lifecycle.LAST_USED_DATE, 0));
        assertEquals(true, v5Lifecycle.getBoolean(V5.Lifecycle.SUCCESFUL_CLOSE, false));

        NamedCollection v5Target =
                ServiceProvider.getInstance()
                        .getDataStoreService()
                        .getNamedCollection(V5.Target.DATASTORE_NAME);
        assertEquals("tntid", v5Target.getString(V5.Target.TNT_ID, null));
        assertEquals("3rdpartyid", v5Target.getString(V5.Target.THIRD_PARTY_ID, null));

        NamedCollection v5MobileServices =
                ServiceProvider.getInstance()
                        .getDataStoreService()
                        .getNamedCollection(V5.MobileServices.DATASTORE_NAME);
        assertEquals(
                "utm_source",
                v5MobileServices.getString(
                        V5.MobileServices.DEFAULTS_KEY_REFERRER_UTM_SOURCE, null));
        assertEquals(
                "utm_medium",
                v5MobileServices.getString(
                        V5.MobileServices.DEFAULTS_KEY_REFERRER_UTM_MEDIUM, null));
        assertEquals(
                "utm_term",
                v5MobileServices.getString(V5.MobileServices.DEFAULTS_KEY_REFERRER_UTM_TERM, null));
        assertEquals(
                "utm_content",
                v5MobileServices.getString(
                        V5.MobileServices.DEFAULTS_KEY_REFERRER_UTM_CONTENT, null));
        assertEquals(
                "utm_campaign",
                v5MobileServices.getString(
                        V5.MobileServices.DEFAULTS_KEY_REFERRER_UTM_CAMPAIGN, null));
        assertEquals(
                "trackingcode",
                v5MobileServices.getString(
                        V5.MobileServices.DEFAULTS_KEY_REFERRER_TRACKINGCODE, null));

        assertEquals(
                "blacklist",
                v5MobileServices.getString(V5.MobileServices.SHARED_PREFERENCES_BLACK_LIST, null));

        NamedCollection v5Configuration =
                ServiceProvider.getInstance()
                        .getDataStoreService()
                        .getNamedCollection(V5.Configuration.DATASTORE_NAME);
        assertEquals(
                "{\"global.privacy\":\"optedout\"}",
                v5Configuration.getString(V5.Configuration.PERSISTED_OVERRIDDEN_CONFIG, null));
    }

    @Test
    public void testDataMigration_SomeKeysArePreserved() {
        v4DataStoreEditor.putInt(V4.LIFETIME_VALUE, 789);
        v4DataStoreEditor.putLong(V4.Lifecycle.INSTALL_DATE, 123456);
        v4DataStoreEditor.commit();

        migrationTool.migrate();

        assertEquals(789, v4DataStore.getInt(V4.LIFETIME_VALUE, 0));
    }

    @Test
    public void testDataMigration_DoesNotThrow_WhenNullContext() {
        mockAppContextService.appContext = null;
        try {
            migrationTool.migrate();
        } catch (Throwable e) {
            Assert.fail("The migration tool should not throw for null app context");
        }
    }

    @Test
    public void testDataMigration_OnlyPrivacyKeyInV4_onlyMigratesConfiguration() throws Exception {
        // mock target data
        v4DataStoreEditor.putString(V4.Target.TNT_ID, "tntid");
        v4DataStoreEditor.putString(V4.Target.THIRD_PARTY_ID, "3rdpartyid");
        v4DataStoreEditor.putString(V4.Target.SESSION_ID, "sessionid");
        v4DataStoreEditor.putString(V4.Target.EDGE_HOST, "edgehost");

        // mock configuration data
        v4DataStoreEditor.putInt(V4.Configuration.GLOBAL_PRIVACY_KEY, 1); // OptOut

        v4DataStoreEditor.commit();

        // test, note, Lifecycle Install key does not exist in v4
        migrationTool.migrate();

        // Target data should not get migrated or removed
        assertTrue(v4DataStore.contains(V4.Target.TNT_ID));
        assertTrue(v4DataStore.contains(V4.Target.THIRD_PARTY_ID));
        assertTrue(v4DataStore.contains(V4.Target.SESSION_ID));
        assertTrue(v4DataStore.contains(V4.Target.EDGE_HOST));

        // configuration data is migrated and removed
        assertFalse(v4DataStore.contains(V4.Configuration.GLOBAL_PRIVACY_KEY));

        // verify v5 data was set
        NamedCollection v5Configuration =
                ServiceProvider.getInstance()
                        .getDataStoreService()
                        .getNamedCollection(V5.Configuration.DATASTORE_NAME);
        String actualOverriddenString =
                v5Configuration.getString(V5.Configuration.PERSISTED_OVERRIDDEN_CONFIG, null);
        JSONObject actual = new JSONObject(actualOverriddenString);

        // verify v5
        assertEquals(1, actual.length());
        assertEquals("optedout", actual.getString("global.privacy"));

        NamedCollection v5Target =
                ServiceProvider.getInstance()
                        .getDataStoreService()
                        .getNamedCollection(V5.Target.DATASTORE_NAME);
        assertFalse(v5Target.contains(V5.Target.TNT_ID));
        assertFalse(v5Target.contains(V5.Target.THIRD_PARTY_ID));
        assertFalse(v5Target.contains(V5.Target.SESSION_ID));
        assertFalse(v5Target.contains(V5.Target.EDGE_HOST));
    }

    @Test
    public void testDataMigration_V5ConfigurationDataExistsWithoutPrivacyStatus() throws Exception {
        // mock configuration data
        v4DataStoreEditor.putInt(V4.Configuration.GLOBAL_PRIVACY_KEY, 1); // OptOut

        NamedCollection v5Configuration =
                ServiceProvider.getInstance()
                        .getDataStoreService()
                        .getNamedCollection(V5.Configuration.DATASTORE_NAME);

        // add existing config data
        Map<String, Object> v5ConfigMap = new HashMap<>();
        v5ConfigMap.put("global.ssl", true);

        JSONObject v5JsonObj = new JSONObject(v5ConfigMap);
        v5Configuration.setString(
                V5.Configuration.PERSISTED_OVERRIDDEN_CONFIG, v5JsonObj.toString());

        v4DataStoreEditor.commit();

        // test, note, Lifecycle Install key does not exist in v4
        migrationTool.migrate();

        // configuration data is migrated and removed
        assertFalse(v4DataStore.contains(V4.Configuration.GLOBAL_PRIVACY_KEY));

        String actualOverriddenString =
                v5Configuration.getString(V5.Configuration.PERSISTED_OVERRIDDEN_CONFIG, null);
        JSONObject actual = new JSONObject(actualOverriddenString);

        // verify v5 data was set
        assertEquals(2, actual.length());
        assertEquals("optedout", actual.getString("global.privacy"));
        assertTrue(actual.getBoolean("global.ssl"));
    }

    @Test
    public void testDataMigration_V5ConfigurationDataExistsWithExistingPrivacyStatus()
            throws Exception {
        // mock configuration data
        v4DataStoreEditor.putInt(V4.Configuration.GLOBAL_PRIVACY_KEY, 1); // OptOut

        NamedCollection v5Configuration =
                ServiceProvider.getInstance()
                        .getDataStoreService()
                        .getNamedCollection(V5.Configuration.DATASTORE_NAME);
        // add existing config data
        Map<String, Object> v5ConfigMap = new HashMap<>();
        v5ConfigMap.put("global.ssl", true);
        v5ConfigMap.put(V5.Configuration.GLOBAL_PRIVACY_KEY, "optedin");

        JSONObject v5JsonObj = new JSONObject(v5ConfigMap);
        v5Configuration.setString(
                V5.Configuration.PERSISTED_OVERRIDDEN_CONFIG, v5JsonObj.toString());

        v4DataStoreEditor.commit();

        // test, note, Lifecycle Install key does not exist in v4
        migrationTool.migrate();

        // configuration data is removed
        assertFalse(v4DataStore.contains(V4.Configuration.GLOBAL_PRIVACY_KEY));

        String actualOverriddenString =
                v5Configuration.getString(V5.Configuration.PERSISTED_OVERRIDDEN_CONFIG, null);
        JSONObject actual = new JSONObject(actualOverriddenString);

        // verify v5 data was not changed
        assertEquals(2, actual.length());
        assertEquals("optedin", actual.getString("global.privacy"));
        assertTrue(actual.getBoolean("global.ssl"));
    }

    @Test
    public void testDataMigration_V5ConfigurationDataIsNotValidJson_failsGracefully() {
        // mock configuration data
        v4DataStoreEditor.putInt(V4.Configuration.GLOBAL_PRIVACY_KEY, 1); // OptOut

        NamedCollection v5Configuration =
                ServiceProvider.getInstance()
                        .getDataStoreService()
                        .getNamedCollection(V5.Configuration.DATASTORE_NAME);
        // add existing config data which is not valid JSON
        v5Configuration.setString(V5.Configuration.PERSISTED_OVERRIDDEN_CONFIG, "hello world");

        v4DataStoreEditor.commit();

        // test, note, Lifecycle Install key does not exist in v4
        migrationTool.migrate();

        // v4 configuration data is removed
        assertFalse(v4DataStore.contains(V4.Configuration.GLOBAL_PRIVACY_KEY));

        // verify v5 data was not changed
        assertEquals(
                "hello world",
                v5Configuration.getString(V5.Configuration.PERSISTED_OVERRIDDEN_CONFIG, null));
    }

    @Test
    public void testDataMigration_V4PrivacyKeyInvalid_V5ConfigurationMigrationFails() {
        // mock configuration data
        v4DataStoreEditor.putInt(V4.Configuration.GLOBAL_PRIVACY_KEY, -1); // Invalid

        NamedCollection v5Configuration =
                ServiceProvider.getInstance()
                        .getDataStoreService()
                        .getNamedCollection(V5.Configuration.DATASTORE_NAME);

        v4DataStoreEditor.commit();

        // test, note, Lifecycle Install key does not exist in v4
        migrationTool.migrate();

        // configuration data is migrated and removed
        assertFalse(v4DataStore.contains(V4.Configuration.GLOBAL_PRIVACY_KEY));

        // verify v5 was not set
        assertFalse(v5Configuration.contains(V5.Configuration.PERSISTED_OVERRIDDEN_CONFIG));
    }

    @Test
    public void testDataMigration_V4PrivacyKeyOptIn_V5ConfigurationMigrationOptIn()
            throws Exception {
        // mock configuration data
        v4DataStoreEditor.putInt(V4.Configuration.GLOBAL_PRIVACY_KEY, 0); // v4 OptIn

        NamedCollection v5Configuration =
                ServiceProvider.getInstance()
                        .getDataStoreService()
                        .getNamedCollection(V5.Configuration.DATASTORE_NAME);
        v4DataStoreEditor.commit();

        // test, note, Lifecycle Install key does not exist in v4
        migrationTool.migrate();

        // configuration data is migrated and removed
        assertFalse(v4DataStore.contains(V4.Configuration.GLOBAL_PRIVACY_KEY));

        String actualOverriddenString =
                v5Configuration.getString(V5.Configuration.PERSISTED_OVERRIDDEN_CONFIG, null);
        JSONObject actual = new JSONObject(actualOverriddenString);

        // verify v5
        assertEquals(1, actual.length());
        assertEquals("optedin", actual.getString("global.privacy"));
    }

    @Test
    public void testDataMigration_V4PrivacyKeyOptOut_V5ConfigurationMigrationOptOut()
            throws Exception {
        // mock configuration data
        v4DataStoreEditor.putInt(V4.Configuration.GLOBAL_PRIVACY_KEY, 1); // v4 OptOut
        v4DataStoreEditor.commit();

        // test, note, Lifecycle Install key does not exist in v4
        migrationTool.migrate();

        // configuration data is migrated and removed
        assertFalse(v4DataStore.contains(V4.Configuration.GLOBAL_PRIVACY_KEY));

        NamedCollection v5Configuration =
                ServiceProvider.getInstance()
                        .getDataStoreService()
                        .getNamedCollection(V5.Configuration.DATASTORE_NAME);
        String actualOverriddenString =
                v5Configuration.getString(V5.Configuration.PERSISTED_OVERRIDDEN_CONFIG, null);
        JSONObject actual = new JSONObject(actualOverriddenString);

        // verify v5
        assertEquals(1, actual.length());
        assertEquals("optedout", actual.getString("global.privacy"));
    }

    @Test
    public void testDataMigration_V4PrivacyKeyUnknown_V5ConfigurationMigrationUnknown()
            throws Exception {
        // mock configuration data
        v4DataStoreEditor.putInt(V4.Configuration.GLOBAL_PRIVACY_KEY, 2); // v4 Unknown

        NamedCollection v5Configuration =
                ServiceProvider.getInstance()
                        .getDataStoreService()
                        .getNamedCollection(V5.Configuration.DATASTORE_NAME);

        v4DataStoreEditor.commit();

        // test, note, Lifecycle Install key does not exist in v4
        migrationTool.migrate();

        // configuration data is migrated and removed
        assertFalse(v4DataStore.contains(V4.Configuration.GLOBAL_PRIVACY_KEY));

        String actualOverriddenString =
                v5Configuration.getString(V5.Configuration.PERSISTED_OVERRIDDEN_CONFIG, null);
        JSONObject actual = new JSONObject(actualOverriddenString);

        // verify v5
        assertEquals(1, actual.length());
        assertEquals("optunknown", actual.getString("global.privacy"));
    }

    @Test
    public void testDataMigration_V5VisitorIdFromIdentityToAnalytics() throws Exception {
        NamedCollection v5Identity =
                ServiceProvider.getInstance()
                        .getDataStoreService()
                        .getNamedCollection(V5.Identity.DATASTORE_NAME);
        NamedCollection v5Analytics =
                ServiceProvider.getInstance()
                        .getDataStoreService()
                        .getNamedCollection(V5.Analytics.DATASTORE_NAME);

        assertFalse(v5Analytics.contains(V5.Analytics.VID));
        v5Identity.setString(V5.Identity.VISITOR_ID, "test-vid");

        migrationTool.migrate();

        assertEquals("test-vid", v5Analytics.getString(V5.Analytics.VID, null));
        assertFalse(v5Identity.contains(V5.Identity.VISITOR_ID));
    }

    @Test
    public void testDataMigration_V5VisitorIdNoMigrationFromIdentity() throws Exception {
        NamedCollection v5Identity =
                ServiceProvider.getInstance()
                        .getDataStoreService()
                        .getNamedCollection(V5.Identity.DATASTORE_NAME);
        NamedCollection v5Analytics =
                ServiceProvider.getInstance()
                        .getDataStoreService()
                        .getNamedCollection(V5.Analytics.DATASTORE_NAME);

        v5Analytics.setString(V5.Analytics.VID, "existing-vid");
        v5Identity.setString(V5.Identity.VISITOR_ID, "test-vid");

        migrationTool.migrate();

        assertEquals("existing-vid", v5Analytics.getString(V5.Analytics.VID, null));
        assertFalse(v5Identity.contains(V5.Identity.VISITOR_ID));
    }
}
