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

package com.adobe.marketing.mobile.internal.configuration

import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.NamedCollection
import com.adobe.marketing.mobile.services.ServiceProvider

/**
 * Manages the storage and retrieval of AEP appID from shared preferences and the app manifest.
 */
internal class AppIdManager {

    companion object {
        private const val LOG_TAG = "AppIdManager"
    }

    private val configStateStoreCollection: NamedCollection? =
        ServiceProvider.getInstance().dataStoreService
            .getNamedCollection(ConfigurationStateManager.DATASTORE_KEY)

    /**
     * Saves the appId provided into shared preferences.
     *
     * @param appId the appId to store/update to shared preferences
     */
    internal fun saveAppIdToPersistence(appId: String) {
        if (appId.isBlank()) {
            Log.trace(ConfigurationExtension.TAG, LOG_TAG, "Attempting to set empty App Id into persistence.")
            return
        }

        configStateStoreCollection?.setString(ConfigurationStateManager.PERSISTED_APPID, appId)
    }

    /**
     * Removes the existing appId stored in shared preferences.
     */
    internal fun removeAppIdFromPersistence() {
        Log.trace(ConfigurationExtension.TAG, LOG_TAG, "Attempting to set empty App Id into persistence.")
        configStateStoreCollection?.remove(ConfigurationStateManager.PERSISTED_APPID)
    }

    /**
     * Retrieves appId from persistence when available. Falls back to
     * retrieving it from manifest if nothing is persisted.
     *
     * @return the appId stored in manifest or persistence (in that order) if one exists;
     *         null otherwise
     */
    internal fun loadAppId(): String? {
        return getAppIDFromPersistence().also { persistedAppId ->
            persistedAppId?.let {
                Log.trace(
                    ConfigurationExtension.TAG,
                    LOG_TAG,
                    "Retrieved AppId from persistence."
                )
            }
        } ?: getAppIDFromManifest().also { manifestAppId ->
            manifestAppId?.let {
                Log.trace(
                    ConfigurationExtension.TAG,
                    LOG_TAG,
                    "Retrieved AppId from manifest."
                )
                saveAppIdToPersistence(it)
            }
        }
    }

    /**
     * Retrieves the existing appId stored in shared preferences.
     *
     * @return the existing appId stored in shared preferences if it exists,
     *         null otherwise.
     */
    private fun getAppIDFromPersistence(): String? {
        return configStateStoreCollection?.getString(
            ConfigurationStateManager.PERSISTED_APPID,
            null
        )
    }

    /**
     * Retrieves the existing appId from the manifest of the app.
     *
     * @return the existing appId from manifest if it exists,
     *         null otherwise.
     */
    private fun getAppIDFromManifest(): String? {
        return ServiceProvider.getInstance().deviceInfoService
            .getPropertyFromManifest(ConfigurationStateManager.CONFIG_MANIFEST_APPID_KEY)
    }
}
