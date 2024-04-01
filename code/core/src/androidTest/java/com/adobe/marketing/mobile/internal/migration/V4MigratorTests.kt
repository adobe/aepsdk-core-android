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

package com.adobe.marketing.mobile.internal.migration

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import com.adobe.marketing.mobile.internal.migration.MigrationConstants.V4
import com.adobe.marketing.mobile.internal.migration.MigrationConstants.V5
import com.adobe.marketing.mobile.services.MockAppContextService
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.ServiceProviderModifier
import org.json.JSONObject
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class V4MigratorTests {

    private lateinit var v4DataStore: SharedPreferences
    private lateinit var v4DataStoreEditor: SharedPreferences.Editor
    private lateinit var migrationTool: V4Migrator

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val mockAppContextService = MockAppContextService().apply {
            appContext = context
        }
        ServiceProviderModifier.setAppContextService(mockAppContextService)

        migrationTool = V4Migrator()

        v4DataStore = context.getSharedPreferences(V4.DATASTORE_NAME, 0)
        v4DataStoreEditor = v4DataStore.edit()
        v4DataStoreEditor.clear()
        v4DataStoreEditor.commit()
    }

    @After
    fun tearDown() {
        val stores = arrayListOf(
            V5.Acquisition.DATASTORE_NAME,
            V5.Analytics.DATASTORE_NAME,
            V5.AudienceManager.DATASTORE_NAME,
            V5.Target.DATASTORE_NAME,
            V5.Identity.DATASTORE_NAME,
            V5.Configuration.DATASTORE_NAME,
            V5.MobileServices.DATASTORE_NAME,
            V5.Lifecycle.DATASTORE_NAME
        )
        stores.forEach {
            ServiceProvider.getInstance().dataStoreService.getNamedCollection(it)?.removeAll()
        }
    }

    @Test
    fun testDataMigration_ExistingV4Data() {
        // mock acquisition data
        v4DataStoreEditor.putString(V4.Acquisition.REFERRER_DATA, "{\"acqkey\":\"acqvalue\"}")
        v4DataStoreEditor.putString(V4.Acquisition.DEFAULTS_KEY_REFERRER_UTM_SOURCE, "utm_source")
        v4DataStoreEditor.putString(V4.Acquisition.DEFAULTS_KEY_REFERRER_UTM_MEDIUM, "utm_medium")
        v4DataStoreEditor.putString(V4.Acquisition.DEFAULTS_KEY_REFERRER_UTM_TERM, "utm_term")
        v4DataStoreEditor.putString(V4.Acquisition.DEFAULTS_KEY_REFERRER_UTM_CONTENT, "utm_content")
        v4DataStoreEditor.putString(
            V4.Acquisition.DEFAULTS_KEY_REFERRER_UTM_CAMPAIGN,
            "utm_campaign"
        )
        v4DataStoreEditor.putString(
            V4.Acquisition.DEFAULTS_KEY_REFERRER_TRACKINGCODE,
            "trackingcode"
        )

        // mock messages data
        v4DataStoreEditor.putString(V4.Messages.SHARED_PREFERENCES_BLACK_LIST, "blacklist")

        // mock analytics data
        v4DataStoreEditor.putString(V4.Analytics.AID, "aid")
        v4DataStoreEditor.putBoolean(V4.Analytics.IGNORE_AID, true)
        v4DataStoreEditor.putLong(V4.Analytics.LAST_KNOWN_TIMESTAMP, 12345)

        // mock audience data
        v4DataStoreEditor.putString(V4.AudienceManager.USER_ID, "aamUserId")
        v4DataStoreEditor.putString(V4.AudienceManager.USER_PROFILE, "{\"aamkey\": \"aamvalue\"}")

        // mock identity data
        v4DataStoreEditor.putBoolean(V4.Identity.AID_SYNCED, true)
        v4DataStoreEditor.putString(V4.Identity.MID, "identityMid")
        v4DataStoreEditor.putString(V4.Identity.BLOB, "blob")
        v4DataStoreEditor.putString(V4.Identity.HINT, "hint")
        v4DataStoreEditor.putString(V4.Identity.VISITOR_ID, "vid")
        v4DataStoreEditor.putLong(V4.Identity.VISITOR_ID_TTL, 12345)
        v4DataStoreEditor.putLong(V4.Identity.VISITOR_ID_SYNC, 123456)
        v4DataStoreEditor.putString(V4.Identity.VISITOR_IDS, "identityIds")
        v4DataStoreEditor.putString(V4.Identity.PUSH_IDENTIFIER, "pushtoken")
        v4DataStoreEditor.putBoolean(V4.Identity.PUSH_ENABLED, true)

        // mock lifecycle data
        v4DataStoreEditor.putLong(V4.Lifecycle.INSTALL_DATE, 123456)
        v4DataStoreEditor.putLong(V4.Lifecycle.UPGRADE_DATE, 123456)
        v4DataStoreEditor.putLong(V4.Lifecycle.LAST_USED_DATE, 123456)
        v4DataStoreEditor.putString(V4.Lifecycle.OS, "os")
        v4DataStoreEditor.putString(V4.Lifecycle.APPLICATION_ID, "appid")
        v4DataStoreEditor.putInt(V4.Lifecycle.LAUNCHES, 552)
        v4DataStoreEditor.putInt(V4.Lifecycle.LAUNCHES_AFTER_UPGRADE, 3)
        v4DataStoreEditor.putBoolean(V4.Lifecycle.SUCCESFUL_CLOSE, true)
        v4DataStoreEditor.putLong(V4.Lifecycle.PAUSE_DATE, 123456)
        v4DataStoreEditor.putLong(V4.Lifecycle.START_DATE, 123456)
        v4DataStoreEditor.putString(V4.Lifecycle.LAST_VERSION, "version")
        v4DataStoreEditor.putString(
            V4.Lifecycle.CONTEXT_DATA,
            "{\"lifecyclekey\": \"lifecyclevalue\"}"
        )

        // mock target data
        v4DataStoreEditor.putString(V4.Target.TNT_ID, "tntid")
        v4DataStoreEditor.putString(V4.Target.THIRD_PARTY_ID, "3rdpartyid")
        v4DataStoreEditor.putLong(V4.Target.LAST_TIMESTAMP, 123456)
        v4DataStoreEditor.putLong(V4.Target.COOKIE_EXPIRES, 123456)
        v4DataStoreEditor.putString(V4.Target.COOKIE_VALUE, "cookie")
        v4DataStoreEditor.putString(V4.Target.SESSION_ID, "sessionid")
        v4DataStoreEditor.putString(V4.Target.EDGE_HOST, "edgehost")

        // mock configuration data
        v4DataStoreEditor.putInt(V4.Configuration.GLOBAL_PRIVACY_KEY, 1) // OptOut
        v4DataStoreEditor.commit()

        // test
        migrationTool.migrate()

        // verify v4 keys have been removed
        Assert.assertFalse(v4DataStore.contains(V4.Acquisition.REFERRER_DATA))
        Assert.assertFalse(v4DataStore.contains(V4.Analytics.AID))
        Assert.assertFalse(v4DataStore.contains(V4.Analytics.IGNORE_AID))
        Assert.assertFalse(v4DataStore.contains(V4.Analytics.LAST_KNOWN_TIMESTAMP))
        Assert.assertFalse(v4DataStore.contains(V4.AudienceManager.USER_ID))
        Assert.assertFalse(v4DataStore.contains(V4.AudienceManager.USER_PROFILE))
        Assert.assertFalse(v4DataStore.contains(V4.Identity.AID_SYNCED))
        Assert.assertFalse(v4DataStore.contains(V4.Identity.MID))
        Assert.assertFalse(v4DataStore.contains(V4.Identity.BLOB))
        Assert.assertFalse(v4DataStore.contains(V4.Identity.HINT))
        Assert.assertFalse(v4DataStore.contains(V4.Identity.VISITOR_ID))
        Assert.assertFalse(v4DataStore.contains(V4.Identity.VISITOR_ID_TTL))
        Assert.assertFalse(v4DataStore.contains(V4.Identity.VISITOR_ID_SYNC))
        Assert.assertFalse(v4DataStore.contains(V4.Identity.VISITOR_IDS))
        Assert.assertFalse(v4DataStore.contains(V4.Identity.PUSH_IDENTIFIER))
        Assert.assertFalse(v4DataStore.contains(V4.Identity.PUSH_ENABLED))
        Assert.assertFalse(v4DataStore.contains(V4.Lifecycle.INSTALL_DATE))
        Assert.assertFalse(v4DataStore.contains(V4.Lifecycle.UPGRADE_DATE))
        Assert.assertFalse(v4DataStore.contains(V4.Lifecycle.LAST_USED_DATE))
        Assert.assertFalse(v4DataStore.contains(V4.Lifecycle.OS))
        Assert.assertFalse(v4DataStore.contains(V4.Lifecycle.APPLICATION_ID))
        Assert.assertFalse(v4DataStore.contains(V4.Lifecycle.LAUNCHES))
        Assert.assertFalse(v4DataStore.contains(V4.Lifecycle.LAUNCHES_AFTER_UPGRADE))
        Assert.assertFalse(v4DataStore.contains(V4.Lifecycle.SUCCESFUL_CLOSE))
        Assert.assertFalse(v4DataStore.contains(V4.Lifecycle.PAUSE_DATE))
        Assert.assertFalse(v4DataStore.contains(V4.Lifecycle.START_DATE))
        Assert.assertFalse(v4DataStore.contains(V4.Lifecycle.LAST_VERSION))
        Assert.assertFalse(v4DataStore.contains(V4.Lifecycle.CONTEXT_DATA))
        Assert.assertFalse(v4DataStore.contains(V4.Target.TNT_ID))
        Assert.assertFalse(v4DataStore.contains(V4.Target.THIRD_PARTY_ID))
        Assert.assertFalse(v4DataStore.contains(V4.Target.LAST_TIMESTAMP))
        Assert.assertFalse(v4DataStore.contains(V4.Target.COOKIE_EXPIRES))
        Assert.assertFalse(v4DataStore.contains(V4.Target.COOKIE_VALUE))
        Assert.assertFalse(v4DataStore.contains(V4.Target.SESSION_ID))
        Assert.assertFalse(v4DataStore.contains(V4.Target.EDGE_HOST))
        Assert.assertFalse(v4DataStore.contains(V4.Acquisition.REFERRER_DATA))
        Assert.assertFalse(v4DataStore.contains(V4.Acquisition.DEFAULTS_KEY_REFERRER_UTM_SOURCE))
        Assert.assertFalse(v4DataStore.contains(V4.Acquisition.DEFAULTS_KEY_REFERRER_UTM_MEDIUM))
        Assert.assertFalse(v4DataStore.contains(V4.Acquisition.DEFAULTS_KEY_REFERRER_UTM_TERM))
        Assert.assertFalse(v4DataStore.contains(V4.Acquisition.DEFAULTS_KEY_REFERRER_UTM_CONTENT))
        Assert.assertFalse(v4DataStore.contains(V4.Acquisition.DEFAULTS_KEY_REFERRER_UTM_CAMPAIGN))
        Assert.assertFalse(v4DataStore.contains(V4.Acquisition.DEFAULTS_KEY_REFERRER_TRACKINGCODE))
        Assert.assertFalse(v4DataStore.contains(V4.Messages.SHARED_PREFERENCES_BLACK_LIST))
        Assert.assertFalse(v4DataStore.contains(V4.Configuration.GLOBAL_PRIVACY_KEY))

        // verify v5 data was set
        val v5Acquisition = ServiceProvider.getInstance()
            .dataStoreService
            .getNamedCollection(V5.Acquisition.DATASTORE_NAME)
        Assert.assertEquals(
            "{\"acqkey\":\"acqvalue\"}",
            v5Acquisition.getString(V5.Acquisition.REFERRER_DATA, null)
        )
        val v5Analytics = ServiceProvider.getInstance()
            .dataStoreService
            .getNamedCollection(V5.Analytics.DATASTORE_NAME)
        Assert.assertEquals("aid", v5Analytics.getString(V5.Analytics.AID, null))
        Assert.assertEquals("vid", v5Analytics.getString(V5.Analytics.VID, null))
        Assert.assertTrue(v5Analytics.getBoolean(V5.Analytics.IGNORE_AID, false))
        val v5AudienceManager = ServiceProvider.getInstance()
            .dataStoreService
            .getNamedCollection(V5.AudienceManager.DATASTORE_NAME)
        Assert.assertEquals(
            "aamUserId",
            v5AudienceManager.getString(V5.AudienceManager.USER_ID, null)
        )
        Assert.assertNull(v5AudienceManager.getString(V5.AudienceManager.USER_PROFILE, null))
        val v5Identity = ServiceProvider.getInstance()
            .dataStoreService
            .getNamedCollection(V5.Identity.DATASTORE_NAME)
        Assert.assertEquals("identityMid", v5Identity.getString(V5.Identity.MID, null))
        Assert.assertEquals("identityIds", v5Identity.getString(V5.Identity.VISITOR_IDS, null))
        Assert.assertEquals("blob", v5Identity.getString(V5.Identity.BLOB, null))
        Assert.assertEquals("hint", v5Identity.getString(V5.Identity.HINT, null))
        Assert.assertTrue(v5Identity.getBoolean(V5.Identity.PUSH_ENABLED, false))
        val v5Lifecycle = ServiceProvider.getInstance()
            .dataStoreService
            .getNamedCollection(V5.Lifecycle.DATASTORE_NAME)
        Assert.assertEquals(123, v5Lifecycle.getLong(V5.Lifecycle.INSTALL_DATE, 0))
        Assert.assertEquals(552, v5Lifecycle.getInt(V5.Lifecycle.LAUNCHES, 0).toLong())
        Assert.assertEquals("version", v5Lifecycle.getString(V5.Lifecycle.LAST_VERSION, null))
        Assert.assertEquals(123, v5Lifecycle.getLong(V5.Lifecycle.LAST_USED_DATE, 0))
        Assert.assertTrue(v5Lifecycle.getBoolean(V5.Lifecycle.SUCCESFUL_CLOSE, false))
        val v5Target = ServiceProvider.getInstance()
            .dataStoreService
            .getNamedCollection(V5.Target.DATASTORE_NAME)
        Assert.assertEquals("tntid", v5Target.getString(V5.Target.TNT_ID, null))
        Assert.assertEquals("3rdpartyid", v5Target.getString(V5.Target.THIRD_PARTY_ID, null))
        val v5MobileServices = ServiceProvider.getInstance()
            .dataStoreService
            .getNamedCollection(V5.MobileServices.DATASTORE_NAME)
        Assert.assertEquals(
            "utm_source",
            v5MobileServices.getString(
                V5.MobileServices.DEFAULTS_KEY_REFERRER_UTM_SOURCE,
                null
            )
        )
        Assert.assertEquals(
            "utm_medium",
            v5MobileServices.getString(
                V5.MobileServices.DEFAULTS_KEY_REFERRER_UTM_MEDIUM,
                null
            )
        )
        Assert.assertEquals(
            "utm_term",
            v5MobileServices.getString(V5.MobileServices.DEFAULTS_KEY_REFERRER_UTM_TERM, null)
        )
        Assert.assertEquals(
            "utm_content",
            v5MobileServices.getString(
                V5.MobileServices.DEFAULTS_KEY_REFERRER_UTM_CONTENT,
                null
            )
        )
        Assert.assertEquals(
            "utm_campaign",
            v5MobileServices.getString(
                V5.MobileServices.DEFAULTS_KEY_REFERRER_UTM_CAMPAIGN,
                null
            )
        )
        Assert.assertEquals(
            "trackingcode",
            v5MobileServices.getString(
                V5.MobileServices.DEFAULTS_KEY_REFERRER_TRACKINGCODE,
                null
            )
        )
        Assert.assertEquals(
            "blacklist",
            v5MobileServices.getString(V5.MobileServices.SHARED_PREFERENCES_BLACK_LIST, null)
        )
        val v5Configuration = ServiceProvider.getInstance()
            .dataStoreService
            .getNamedCollection(V5.Configuration.DATASTORE_NAME)
        Assert.assertEquals(
            "{\"global.privacy\":\"optedout\"}",
            v5Configuration.getString(V5.Configuration.PERSISTED_OVERRIDDEN_CONFIG, null)
        )
    }

    @Test
    fun testDataMigration_SomeKeysArePreserved() {
        val lifecycleValue = "ADB_LIFETIME_VALUE"
        v4DataStoreEditor.putInt(lifecycleValue, 789)
        v4DataStoreEditor.putLong(V4.Lifecycle.INSTALL_DATE, 123456)
        v4DataStoreEditor.commit()
        migrationTool.migrate()
        Assert.assertEquals(789, v4DataStore.getInt(lifecycleValue, 0))
    }

    @Test
    fun testDataMigration_DoesNotThrow_WhenNullContext() {
        ServiceProviderModifier.setAppContextService(MockAppContextService())
        try {
            migrationTool.migrate()
        } catch (e: Throwable) {
            Assert.fail("The migration tool should not throw for null app context")
        }
    }

    @Test
    fun testDataMigration_OnlyPrivacyKeyInV4_onlyMigratesConfiguration() {
        // mock target data
        v4DataStoreEditor.putString(V4.Target.TNT_ID, "tntid")
        v4DataStoreEditor.putString(V4.Target.THIRD_PARTY_ID, "3rdpartyid")
        v4DataStoreEditor.putString(V4.Target.SESSION_ID, "sessionid")
        v4DataStoreEditor.putString(V4.Target.EDGE_HOST, "edgehost")

        // mock configuration data
        v4DataStoreEditor.putInt(V4.Configuration.GLOBAL_PRIVACY_KEY, 1) // OptOut
        v4DataStoreEditor.commit()

        // test, note, Lifecycle Install key does not exist in v4
        migrationTool.migrate()

        // Target data should not get migrated or removed
        Assert.assertTrue(v4DataStore.contains(V4.Target.TNT_ID))
        Assert.assertTrue(v4DataStore.contains(V4.Target.THIRD_PARTY_ID))
        Assert.assertTrue(v4DataStore.contains(V4.Target.SESSION_ID))
        Assert.assertTrue(v4DataStore.contains(V4.Target.EDGE_HOST))

        // configuration data is migrated and removed
        Assert.assertFalse(v4DataStore.contains(V4.Configuration.GLOBAL_PRIVACY_KEY))

        // verify v5 data was set
        val v5Configuration = ServiceProvider.getInstance()
            .dataStoreService
            .getNamedCollection(V5.Configuration.DATASTORE_NAME)
        val actualOverriddenString =
            v5Configuration.getString(V5.Configuration.PERSISTED_OVERRIDDEN_CONFIG, null)
        val actual = JSONObject(actualOverriddenString)

        // verify v5
        Assert.assertEquals(1, actual.length().toLong())
        Assert.assertEquals("optedout", actual.getString("global.privacy"))
        val v5Target = ServiceProvider.getInstance()
            .dataStoreService
            .getNamedCollection(V5.Target.DATASTORE_NAME)
        Assert.assertFalse(v5Target.contains(V5.Target.TNT_ID))
        Assert.assertFalse(v5Target.contains(V5.Target.THIRD_PARTY_ID))
        Assert.assertFalse(v5Target.contains(V5.Target.SESSION_ID))
        Assert.assertFalse(v5Target.contains(V5.Target.EDGE_HOST))
    }

    @Test
    fun testDataMigration_V5ConfigurationDataExistsWithoutPrivacyStatus() {
        // mock configuration data
        v4DataStoreEditor.putInt(V4.Configuration.GLOBAL_PRIVACY_KEY, 1) // OptOut
        val v5Configuration = ServiceProvider.getInstance()
            .dataStoreService
            .getNamedCollection(V5.Configuration.DATASTORE_NAME)

        // add existing config data
        val v5ConfigMap: MutableMap<String?, Any?> = HashMap()
        v5ConfigMap["global.ssl"] = true
        val v5JsonObj = JSONObject(v5ConfigMap)
        v5Configuration.setString(
            V5.Configuration.PERSISTED_OVERRIDDEN_CONFIG,
            v5JsonObj.toString()
        )
        v4DataStoreEditor.commit()

        // test, note, Lifecycle Install key does not exist in v4
        migrationTool.migrate()

        // configuration data is migrated and removed
        Assert.assertFalse(v4DataStore.contains(V4.Configuration.GLOBAL_PRIVACY_KEY))
        val actualOverriddenString =
            v5Configuration.getString(V5.Configuration.PERSISTED_OVERRIDDEN_CONFIG, null)
        val actual = JSONObject(actualOverriddenString)

        // verify v5 data was set
        Assert.assertEquals(2, actual.length().toLong())
        Assert.assertEquals("optedout", actual.getString("global.privacy"))
        Assert.assertTrue(actual.getBoolean("global.ssl"))
    }

    @Test
    fun testDataMigration_V5ConfigurationDataExistsWithExistingPrivacyStatus() {
        // mock configuration data
        v4DataStoreEditor.putInt(V4.Configuration.GLOBAL_PRIVACY_KEY, 1) // OptOut
        val v5Configuration = ServiceProvider.getInstance()
            .dataStoreService
            .getNamedCollection(V5.Configuration.DATASTORE_NAME)
        // add existing config data
        val v5ConfigMap: MutableMap<String?, Any?> = HashMap()
        v5ConfigMap["global.ssl"] = true
        v5ConfigMap[V5.Configuration.GLOBAL_PRIVACY_KEY] = "optedin"
        val v5JsonObj = JSONObject(v5ConfigMap)
        v5Configuration.setString(
            V5.Configuration.PERSISTED_OVERRIDDEN_CONFIG,
            v5JsonObj.toString()
        )
        v4DataStoreEditor.commit()

        // test, note, Lifecycle Install key does not exist in v4
        migrationTool.migrate()

        // configuration data is removed
        Assert.assertFalse(v4DataStore.contains(V4.Configuration.GLOBAL_PRIVACY_KEY))
        val actualOverriddenString =
            v5Configuration.getString(V5.Configuration.PERSISTED_OVERRIDDEN_CONFIG, null)
        val actual = JSONObject(actualOverriddenString)

        // verify v5 data was not changed
        Assert.assertEquals(2, actual.length().toLong())
        Assert.assertEquals("optedin", actual.getString("global.privacy"))
        Assert.assertTrue(actual.getBoolean("global.ssl"))
    }

    @Test
    fun testDataMigration_V5ConfigurationDataIsNotValidJson_failsGracefully() {
        // mock configuration data
        v4DataStoreEditor.putInt(V4.Configuration.GLOBAL_PRIVACY_KEY, 1) // OptOut
        val v5Configuration = ServiceProvider.getInstance()
            .dataStoreService
            .getNamedCollection(V5.Configuration.DATASTORE_NAME)
        // add existing config data which is not valid JSON
        v5Configuration.setString(V5.Configuration.PERSISTED_OVERRIDDEN_CONFIG, "hello world")
        v4DataStoreEditor.commit()

        // test, note, Lifecycle Install key does not exist in v4
        migrationTool.migrate()

        // v4 configuration data is removed
        Assert.assertFalse(v4DataStore.contains(V4.Configuration.GLOBAL_PRIVACY_KEY))

        // verify v5 data was not changed
        Assert.assertEquals(
            "hello world",
            v5Configuration.getString(V5.Configuration.PERSISTED_OVERRIDDEN_CONFIG, null)
        )
    }

    @Test
    fun testDataMigration_V4PrivacyKeyInvalid_V5ConfigurationMigrationFails() {
        // mock configuration data
        v4DataStoreEditor.putInt(V4.Configuration.GLOBAL_PRIVACY_KEY, -1) // Invalid
        val v5Configuration = ServiceProvider.getInstance()
            .dataStoreService
            .getNamedCollection(V5.Configuration.DATASTORE_NAME)
        v4DataStoreEditor.commit()

        // test, note, Lifecycle Install key does not exist in v4
        migrationTool.migrate()

        // configuration data is migrated and removed
        Assert.assertFalse(v4DataStore.contains(V4.Configuration.GLOBAL_PRIVACY_KEY))

        // verify v5 was not set
        Assert.assertFalse(v5Configuration.contains(V5.Configuration.PERSISTED_OVERRIDDEN_CONFIG))
    }

    @Test
    fun testDataMigration_V4PrivacyKeyOptIn_V5ConfigurationMigrationOptIn() {
        // mock configuration data
        v4DataStoreEditor.putInt(V4.Configuration.GLOBAL_PRIVACY_KEY, 0) // v4 OptIn
        val v5Configuration = ServiceProvider.getInstance()
            .dataStoreService
            .getNamedCollection(V5.Configuration.DATASTORE_NAME)
        v4DataStoreEditor.commit()

        // test, note, Lifecycle Install key does not exist in v4
        migrationTool.migrate()

        // configuration data is migrated and removed
        Assert.assertFalse(v4DataStore.contains(V4.Configuration.GLOBAL_PRIVACY_KEY))
        val actualOverriddenString =
            v5Configuration.getString(V5.Configuration.PERSISTED_OVERRIDDEN_CONFIG, null)
        val actual = JSONObject(actualOverriddenString)

        // verify v5
        Assert.assertEquals(1, actual.length().toLong())
        Assert.assertEquals("optedin", actual.getString("global.privacy"))
    }

    @Test
    fun testDataMigration_V4PrivacyKeyOptOut_V5ConfigurationMigrationOptOut() {
        // mock configuration data
        v4DataStoreEditor.putInt(V4.Configuration.GLOBAL_PRIVACY_KEY, 1) // v4 OptOut
        v4DataStoreEditor.commit()

        // test, note, Lifecycle Install key does not exist in v4
        migrationTool.migrate()

        // configuration data is migrated and removed
        Assert.assertFalse(v4DataStore.contains(V4.Configuration.GLOBAL_PRIVACY_KEY))
        val v5Configuration = ServiceProvider.getInstance()
            .dataStoreService
            .getNamedCollection(V5.Configuration.DATASTORE_NAME)
        val actualOverriddenString =
            v5Configuration.getString(V5.Configuration.PERSISTED_OVERRIDDEN_CONFIG, null)
        val actual = JSONObject(actualOverriddenString)

        // verify v5
        Assert.assertEquals(1, actual.length().toLong())
        Assert.assertEquals("optedout", actual.getString("global.privacy"))
    }

    @Test
    fun testDataMigration_V4PrivacyKeyUnknown_V5ConfigurationMigrationUnknown() {
        // mock configuration data
        v4DataStoreEditor.putInt(V4.Configuration.GLOBAL_PRIVACY_KEY, 2) // v4 Unknown
        val v5Configuration = ServiceProvider.getInstance()
            .dataStoreService
            .getNamedCollection(V5.Configuration.DATASTORE_NAME)
        v4DataStoreEditor.commit()

        // test, note, Lifecycle Install key does not exist in v4
        migrationTool.migrate()

        // configuration data is migrated and removed
        Assert.assertFalse(v4DataStore.contains(V4.Configuration.GLOBAL_PRIVACY_KEY))
        val actualOverriddenString =
            v5Configuration.getString(V5.Configuration.PERSISTED_OVERRIDDEN_CONFIG, null)
        val actual = JSONObject(actualOverriddenString)

        // verify v5
        Assert.assertEquals(1, actual.length().toLong())
        Assert.assertEquals("optunknown", actual.getString("global.privacy"))
    }

    @Test
    fun testDataMigration_V5VisitorIdFromIdentityToAnalytics() {
        val v5Identity = ServiceProvider.getInstance()
            .dataStoreService
            .getNamedCollection(V5.Identity.DATASTORE_NAME)
        val v5Analytics = ServiceProvider.getInstance()
            .dataStoreService
            .getNamedCollection(V5.Analytics.DATASTORE_NAME)
        Assert.assertFalse(v5Analytics.contains(V5.Analytics.VID))
        v5Identity.setString(V5.Identity.VISITOR_ID, "test-vid")
        migrationTool.migrate()
        Assert.assertEquals("test-vid", v5Analytics.getString(V5.Analytics.VID, null))
        Assert.assertFalse(v5Identity.contains(V5.Identity.VISITOR_ID))
    }

    @Test
    fun testDataMigration_V5VisitorIdNoMigrationFromIdentity() {
        val v5Identity = ServiceProvider.getInstance()
            .dataStoreService
            .getNamedCollection(V5.Identity.DATASTORE_NAME)
        val v5Analytics = ServiceProvider.getInstance()
            .dataStoreService
            .getNamedCollection(V5.Analytics.DATASTORE_NAME)
        v5Analytics.setString(V5.Analytics.VID, "existing-vid")
        v5Identity.setString(V5.Identity.VISITOR_ID, "test-vid")
        migrationTool.migrate()
        Assert.assertEquals("existing-vid", v5Analytics.getString(V5.Analytics.VID, null))
        Assert.assertFalse(v5Identity.contains(V5.Identity.VISITOR_ID))
    }
}
