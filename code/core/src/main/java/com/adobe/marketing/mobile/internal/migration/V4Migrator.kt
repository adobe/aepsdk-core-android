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

import android.content.SharedPreferences
import com.adobe.marketing.mobile.MobilePrivacyStatus
import com.adobe.marketing.mobile.internal.CoreConstants
import com.adobe.marketing.mobile.internal.migration.MigrationConstants.V4
import com.adobe.marketing.mobile.internal.migration.MigrationConstants.V5
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceProvider
import org.json.JSONException
import org.json.JSONObject
import java.io.File

internal class V4Migrator {

    private val v4SharedPreferences: SharedPreferences? by lazy {
        val appContext = ServiceProvider.getInstance().appContextService.applicationContext
        appContext?.getSharedPreferences(V4.DATASTORE_NAME, 0)
    }

    private val isMigrationRequired: Boolean
        get() {
            return v4SharedPreferences?.contains(V4.Lifecycle.INSTALL_DATE) ?: false
        }

    private val isConfigurationMigrationRequired: Boolean
        get() {
            return v4SharedPreferences?.contains(V4.Configuration.GLOBAL_PRIVACY_KEY) ?: false
        }

    private val isVisitorIdMigrationRequired: Boolean
        get() {
            val identityV5DataStore =
                ServiceProvider.getInstance().dataStoreService.getNamedCollection(V5.Identity.DATASTORE_NAME)
            return identityV5DataStore?.contains(V5.Identity.VISITOR_ID) ?: false
        }

    fun migrate() {
        if (v4SharedPreferences == null) {
            Log.debug(
                CoreConstants.LOG_TAG,
                LOG_TAG,
                "%s (v4 shared preferences), failed to migrate v4 storage",
                Log.UNEXPECTED_NULL_VALUE
            )
        }

        if (isMigrationRequired) {
            Log.debug(
                CoreConstants.LOG_TAG,
                LOG_TAG,
                "Migrating Adobe SDK v4 SharedPreferences for use with AEP SDK."
            )
            migrateLocalStorage()
            migrateConfigurationLocalStorage()
            removeV4Databases()
            Log.debug(
                CoreConstants.LOG_TAG,
                LOG_TAG,
                "Full migration of v4 SharedPreferences successful."
            )
        } else if (isConfigurationMigrationRequired) {
            Log.debug(
                CoreConstants.LOG_TAG,
                LOG_TAG,
                "Migrating Adobe SDK v4 Configuration SharedPreferences for use with AEP SDK."
            )
            migrateConfigurationLocalStorage()
            Log.debug(
                CoreConstants.LOG_TAG,
                LOG_TAG,
                "Full migration of v4 Configuration SharedPreferences successful."
            )
        }

        if (isVisitorIdMigrationRequired) {
            Log.debug(
                CoreConstants.LOG_TAG,
                LOG_TAG,
                "Migrating visitor identifier from Identity to Analytics."
            )
            migrateVisitorId()
            Log.debug(
                CoreConstants.LOG_TAG,
                LOG_TAG,
                "Full migration of visitor identifier from Identity to Analytics successful."
            )
        }
    }

    private fun migrateLocalStorage() {
        val v4DataStore = v4SharedPreferences ?: return
        val v4DataStoreEditor = v4DataStore.edit()
        val installDateMillis = v4DataStore.getLong(V4.Lifecycle.INSTALL_DATE, 0L)

        // don't remove V4.Acquisition.REFERRER_DATA at here, it will be removed by the acquisition
        // extension
        // v4DataStoreEditor.remove(V4.Acquisition.REFERRER_DATA);
        v4DataStoreEditor.remove(V4.Acquisition.DEFAULTS_KEY_REFERRER_UTM_SOURCE)
        v4DataStoreEditor.remove(V4.Acquisition.DEFAULTS_KEY_REFERRER_UTM_MEDIUM)
        v4DataStoreEditor.remove(V4.Acquisition.DEFAULTS_KEY_REFERRER_UTM_TERM)
        v4DataStoreEditor.remove(V4.Acquisition.DEFAULTS_KEY_REFERRER_UTM_CONTENT)
        v4DataStoreEditor.remove(V4.Acquisition.DEFAULTS_KEY_REFERRER_UTM_CAMPAIGN)
        v4DataStoreEditor.remove(V4.Acquisition.DEFAULTS_KEY_REFERRER_TRACKINGCODE)
        v4DataStoreEditor.remove(V4.Messages.SHARED_PREFERENCES_BLACK_LIST)
        v4DataStoreEditor.apply()
        Log.debug(CoreConstants.LOG_TAG, LOG_TAG, "Migration complete for Mobile Services data.")

        // acquisition
        val acquisitionV5DataStore = ServiceProvider.getInstance()
            .dataStoreService
            .getNamedCollection(V5.Acquisition.DATASTORE_NAME)
        acquisitionV5DataStore.setString(
            V5.Acquisition.REFERRER_DATA,
            v4DataStore.getString(V4.Acquisition.REFERRER_DATA, null)
        )
        v4DataStoreEditor.remove(V4.Acquisition.REFERRER_DATA)
        v4DataStoreEditor.apply()
        Log.debug(CoreConstants.LOG_TAG, LOG_TAG, "Migration complete for Acquisition data.")

        // analytics
        val analyticsV5DataStore = ServiceProvider.getInstance()
            .dataStoreService
            .getNamedCollection(V5.Analytics.DATASTORE_NAME)
        analyticsV5DataStore.setString(
            V5.Analytics.AID,
            v4DataStore.getString(V4.Analytics.AID, null)
        )
        analyticsV5DataStore.setBoolean(
            V5.Analytics.IGNORE_AID,
            v4DataStore.getBoolean(V4.Analytics.IGNORE_AID, false)
        )
        analyticsV5DataStore.setString(
            V5.Analytics.VID,
            v4DataStore.getString(V4.Identity.VISITOR_ID, null)
        )
        v4DataStoreEditor.remove(V4.Analytics.AID)
        v4DataStoreEditor.remove(V4.Analytics.IGNORE_AID)
        v4DataStoreEditor.remove(V4.Analytics.LAST_KNOWN_TIMESTAMP)
        v4DataStoreEditor.apply()
        Log.debug(CoreConstants.LOG_TAG, LOG_TAG, "Migration complete for Analytics data.")

        // audience manager
        val audienceV5DataStore = ServiceProvider.getInstance()
            .dataStoreService
            .getNamedCollection(V5.AudienceManager.DATASTORE_NAME)
        audienceV5DataStore.setString(
            V5.AudienceManager.USER_ID,
            v4DataStore.getString(V4.AudienceManager.USER_ID, null)
        )
        v4DataStoreEditor.remove(V4.AudienceManager.USER_ID)
        v4DataStoreEditor.remove(V4.AudienceManager.USER_PROFILE)
        v4DataStoreEditor.apply()
        Log.debug(CoreConstants.LOG_TAG, LOG_TAG, "Migration complete for Audience Manager data.")

        // identity
        val identityV5DataStore = ServiceProvider.getInstance()
            .dataStoreService
            .getNamedCollection(V5.Identity.DATASTORE_NAME)
        identityV5DataStore.setString(
            V5.Identity.MID,
            v4DataStore.getString(V4.Identity.MID, null)
        )
        identityV5DataStore.setString(
            V5.Identity.BLOB,
            v4DataStore.getString(V4.Identity.BLOB, null)
        )
        identityV5DataStore.setString(
            V5.Identity.HINT,
            v4DataStore.getString(V4.Identity.HINT, null)
        )
        identityV5DataStore.setString(
            V5.Identity.VISITOR_IDS,
            v4DataStore.getString(V4.Identity.VISITOR_IDS, null)
        )
        identityV5DataStore.setBoolean(
            V5.Identity.PUSH_ENABLED,
            v4DataStore.getBoolean(V4.Identity.PUSH_ENABLED, false)
        )
        v4DataStoreEditor.remove(V4.Identity.MID)
        v4DataStoreEditor.remove(V4.Identity.BLOB)
        v4DataStoreEditor.remove(V4.Identity.HINT)
        v4DataStoreEditor.remove(V4.Identity.VISITOR_ID)
        v4DataStoreEditor.remove(V4.Identity.VISITOR_IDS)
        v4DataStoreEditor.remove(V4.Identity.VISITOR_ID_SYNC)
        v4DataStoreEditor.remove(V4.Identity.VISITOR_ID_TTL)
        v4DataStoreEditor.remove(V4.Identity.ADVERTISING_IDENTIFIER)
        v4DataStoreEditor.remove(V4.Identity.PUSH_IDENTIFIER)
        v4DataStoreEditor.remove(V4.Identity.PUSH_ENABLED)
        v4DataStoreEditor.remove(V4.Identity.AID_SYNCED)
        v4DataStoreEditor.apply()
        Log.debug(
            CoreConstants.LOG_TAG,
            LOG_TAG,
            "Migration complete for Identity (Visitor ID Service) data."
        )

        // lifecycle
        val lifecycleV5DataStore = ServiceProvider.getInstance()
            .dataStoreService
            .getNamedCollection(V5.Lifecycle.DATASTORE_NAME)
        if (installDateMillis > 0) {
            // convert milliseconds to seconds as it is handled in v5
            lifecycleV5DataStore.setLong(
                V5.Lifecycle.INSTALL_DATE,
                convertMsToSec(installDateMillis)
            )
        }
        lifecycleV5DataStore.setString(
            V5.Lifecycle.LAST_VERSION,
            v4DataStore.getString(V4.Lifecycle.LAST_VERSION, null)
        )
        val lastUsedDateMillis = v4DataStore.getLong(V4.Lifecycle.LAST_USED_DATE, 0L)
        if (lastUsedDateMillis > 0) {
            lifecycleV5DataStore.setLong(
                V5.Lifecycle.LAST_USED_DATE,
                convertMsToSec(lastUsedDateMillis)
            )
        }
        lifecycleV5DataStore.setInt(
            V5.Lifecycle.LAUNCHES,
            v4DataStore.getInt(V4.Lifecycle.LAUNCHES, 0)
        )
        lifecycleV5DataStore.setBoolean(
            V5.Lifecycle.SUCCESFUL_CLOSE,
            v4DataStore.getBoolean(V4.Lifecycle.SUCCESFUL_CLOSE, false)
        )
        v4DataStoreEditor.remove(V4.Lifecycle.INSTALL_DATE)
        v4DataStoreEditor.remove(V4.Lifecycle.LAST_VERSION)
        v4DataStoreEditor.remove(V4.Lifecycle.LAST_USED_DATE)
        v4DataStoreEditor.remove(V4.Lifecycle.LAUNCHES)
        v4DataStoreEditor.remove(V4.Lifecycle.SUCCESFUL_CLOSE)
        v4DataStoreEditor.remove(V4.Lifecycle.CONTEXT_DATA)
        v4DataStoreEditor.remove(V4.Lifecycle.START_DATE)
        v4DataStoreEditor.remove(V4.Lifecycle.PAUSE_DATE)
        v4DataStoreEditor.remove(V4.Lifecycle.LAUNCHES_AFTER_UPGRADE)
        v4DataStoreEditor.remove(V4.Lifecycle.UPGRADE_DATE)
        v4DataStoreEditor.remove(V4.Lifecycle.OS)
        v4DataStoreEditor.remove(V4.Lifecycle.APPLICATION_ID)
        v4DataStoreEditor.apply()
        Log.debug(CoreConstants.LOG_TAG, LOG_TAG, "Migration complete for Lifecycle data.")

        // target
        val targetV5DataStore = ServiceProvider.getInstance()
            .dataStoreService
            .getNamedCollection(V5.Target.DATASTORE_NAME)
        targetV5DataStore.setString(
            V5.Target.TNT_ID,
            v4DataStore.getString(V4.Target.TNT_ID, null)
        )
        targetV5DataStore.setString(
            V5.Target.THIRD_PARTY_ID,
            v4DataStore.getString(V4.Target.THIRD_PARTY_ID, null)
        )
        targetV5DataStore.setString(
            V5.Target.SESSION_ID,
            v4DataStore.getString(V4.Target.SESSION_ID, null)
        )
        targetV5DataStore.setString(
            V5.Target.EDGE_HOST,
            v4DataStore.getString(V4.Target.EDGE_HOST, null)
        )
        v4DataStoreEditor.remove(V4.Target.TNT_ID)
        v4DataStoreEditor.remove(V4.Target.THIRD_PARTY_ID)
        v4DataStoreEditor.remove(V4.Target.SESSION_ID)
        v4DataStoreEditor.remove(V4.Target.EDGE_HOST)
        v4DataStoreEditor.remove(V4.Target.LAST_TIMESTAMP)
        v4DataStoreEditor.remove(V4.Target.COOKIE_EXPIRES)
        v4DataStoreEditor.remove(V4.Target.COOKIE_VALUE)
        v4DataStoreEditor.apply()
        Log.debug(CoreConstants.LOG_TAG, LOG_TAG, "Migrating complete for Target data.")
    }

    private fun migrateConfigurationLocalStorage() {
        val v4DataStore = v4SharedPreferences ?: return
        val v4DataStoreEditor = v4DataStore.edit()

        // Configuration
        val configurationV5DataStore = ServiceProvider.getInstance()
            .dataStoreService
            .getNamedCollection(V5.Configuration.DATASTORE_NAME)
        val v4PrivacyStatus = v4DataStore.getInt(V4.Configuration.GLOBAL_PRIVACY_KEY, -1)
        if (v4PrivacyStatus in 0..2) {
            val v5PrivacyStatus = when (v4PrivacyStatus) {
                0 -> MobilePrivacyStatus.OPT_IN
                1 -> MobilePrivacyStatus.OPT_OUT
                2 -> MobilePrivacyStatus.UNKNOWN
                else -> MobilePrivacyStatus.UNKNOWN
            }
            val v5OverriddenConfig = configurationV5DataStore.getString(
                V5.Configuration.PERSISTED_OVERRIDDEN_CONFIG,
                null
            )
            if (v5OverriddenConfig != null) {
                try {
                    val v5JsonObj = JSONObject(v5OverriddenConfig)
                    if (!v5JsonObj.has(V5.Configuration.GLOBAL_PRIVACY_KEY)) {
                        // V5 has overridden config data, but global privacy is not set, migrate v4
                        // value
                        v5JsonObj.put(
                            V5.Configuration.GLOBAL_PRIVACY_KEY,
                            v5PrivacyStatus.value
                        )
                        configurationV5DataStore.setString(
                            V5.Configuration.PERSISTED_OVERRIDDEN_CONFIG,
                            v5JsonObj.toString()
                        )
                    } else {
                        Log.debug(
                            CoreConstants.LOG_TAG,
                            LOG_TAG,
                            "V5 configuration data already contains setting for global" +
                                " privacy. V4 global privacy not migrated."
                        )
                    }
                } catch (e: JSONException) {
                    Log.error(
                        CoreConstants.LOG_TAG,
                        LOG_TAG,
                        "Failed to serialize v5 configuration data. Unable to migrate v4" +
                            " configuration data to v5. %s",
                        e.localizedMessage
                    )
                }
            } else {
                // V5 does not contain overridden data, so add one with just migrated privacy status
                val v5ConfigMap: MutableMap<String?, Any?> = HashMap()
                v5ConfigMap[V5.Configuration.GLOBAL_PRIVACY_KEY] = v5PrivacyStatus.value
                val v5JsonObj = JSONObject(v5ConfigMap)
                configurationV5DataStore.setString(
                    V5.Configuration.PERSISTED_OVERRIDDEN_CONFIG,
                    v5JsonObj.toString()
                )
            }
        }
        v4DataStoreEditor.remove(V4.Configuration.GLOBAL_PRIVACY_KEY)
        v4DataStoreEditor.apply()
        Log.debug(CoreConstants.LOG_TAG, LOG_TAG, "Migration complete for Configuration data.")
    }

    private fun migrateVisitorId() {
        val identityV5DataStore = ServiceProvider.getInstance()
            .dataStoreService
            .getNamedCollection(V5.Identity.DATASTORE_NAME)
        val analyticsV5DataStore = ServiceProvider.getInstance()
            .dataStoreService
            .getNamedCollection(V5.Analytics.DATASTORE_NAME)
        if (identityV5DataStore == null || analyticsV5DataStore == null) {
            Log.debug(
                CoreConstants.LOG_TAG,
                LOG_TAG,
                "%s (Identity or Analytics data store), failed to migrate visitor id.",
                Log.UNEXPECTED_NULL_VALUE
            )
            return
        }
        if (!analyticsV5DataStore.contains(V5.Analytics.VID)) {
            val vid = identityV5DataStore.getString(V5.Identity.VISITOR_ID, null)
            analyticsV5DataStore.setString(V5.Analytics.VID, vid)
        }
        identityV5DataStore.remove(V5.Identity.VISITOR_ID)
    }

    private fun removeV4Databases() {
        val cacheDirectory = ServiceProvider.getInstance().deviceInfoService.applicationCacheDir
        if (cacheDirectory == null) {
            Log.debug(
                CoreConstants.LOG_TAG,
                LOG_TAG,
                "%s (cache directory), failed to delete V4 databases",
                Log.UNEXPECTED_NULL_VALUE
            )
            return
        }

        V4.DATABASE_NAMES.forEach { databaseName ->
            try {
                val databaseFile = File(cacheDirectory, databaseName)
                if (databaseFile.exists() && databaseFile.delete()) {
                    Log.debug(
                        CoreConstants.LOG_TAG,
                        LOG_TAG,
                        "Removed V4 database %s successfully",
                        databaseName
                    )
                }
            } catch (e: SecurityException) {
                Log.debug(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "Failed to delete V4 database with name %s (%s)",
                    databaseName,
                    e
                )
            }
        }
    }

    private fun convertMsToSec(timestampMs: Long): Long {
        return timestampMs / 1000
    }

    companion object {
        private const val LOG_TAG = "MobileCore/V4Migrator"
    }
}
