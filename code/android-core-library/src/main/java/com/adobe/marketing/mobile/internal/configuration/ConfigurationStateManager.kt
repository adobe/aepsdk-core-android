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

import androidx.annotation.VisibleForTesting
import com.adobe.marketing.mobile.internal.util.FileUtils
import com.adobe.marketing.mobile.internal.util.toMap
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.NamedCollection
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.caching.CacheResult
import com.adobe.marketing.mobile.util.StreamUtils
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.io.File
import java.io.InputStream
import java.util.Date

/**
 * Responsible for storing, retrieving and maintaining the latest configuration state for the environment
 * based on inputs from different sources.
 */
internal class ConfigurationStateManager {
    companion object {
        private const val LOG_TAG = "ConfigurationStateManager"
        private const val CONFIGURATION_TTL_MS = 15000L
        private const val BUILD_ENVIRONMENT = "build.environment"
        private const val ENVIRONMENT_PREFIX_DELIMITER = "__"
        private const val CONFIGURATION_URL_BASE = "https://assets.adobedtm.com/%s.json"

        internal const val DATASTORE_KEY = "AdobeMobile_ConfigState"
        internal const val PERSISTED_OVERRIDDEN_CONFIG = "config.overridden.map"
        internal const val CONFIG_MANIFEST_APPID_KEY = "ADBMobileAppID"
        internal const val PERSISTED_APPID = "config.appID"
        internal const val CONFIG_BUNDLED_FILE_NAME = "ADBMobileConfig.json"
    }

    private val appIdManager: AppIdManager

    /**
     * Responsible for downloading the configuration from a given appId.
     */
    private val configDownloader: ConfigurationDownloader

    /**
     * Maintains the configuration that has not been subjected to programmatic
     * configuration changes.
     */
    private val unmergedConfiguration: MutableMap<String, Any?> = mutableMapOf()

    /**
     * Maintains the current programmatic configuration.
     */
    private val programmaticConfiguration: MutableMap<String, Any?> = mutableMapOf()

    /**
     * Maintains the entire config (i.e with environment keys).
     */
    private val currentConfiguration: MutableMap<String, Any?> = mutableMapOf()

    /**
     * Maintains the configuration associated with the current environment.
     * Determined by the [BUILD_ENVIRONMENT] key in the [currentConfiguration]
     */
    internal var environmentAwareConfiguration: Map<String, Any?> = mapOf()
        private set

    /**
     * Maintains a mapping between appId and the most recent download time when the config for that
     * appId has been downloaded
     */
    private val configDownloadMap: MutableMap<String, Date> = mutableMapOf()

    constructor(
        appIdManager: AppIdManager
    ) : this(
        appIdManager,
        ConfigurationDownloader()
    )

    @VisibleForTesting
    internal constructor (
        appIdManager: AppIdManager,
        configDownloader: ConfigurationDownloader
    ) {
        this.appIdManager = appIdManager
        this.configDownloader = configDownloader

        // retrieve the persisted programmatic configuration
        // but do not load it anywhere
        getPersistedProgrammaticConfig()?.let {
            programmaticConfiguration.putAll(it)
        }
    }

    /**
     * Loads the first configuration at launch inferred from
     * bundled config, cached config and programmatic configs.
     *
     * @return the initial configuration at launch
     */
    internal fun loadInitialConfig(): Map<String, Any?> {
        currentConfiguration.clear()

        val appId: String? = appIdManager.loadAppId()

        val config: Map<String, Any?>? = if (appId.isNullOrEmpty()) {
            // Load bundled config
            Log.trace(
                ConfigurationExtension.TAG,
                LOG_TAG,
                "AppID from persistence and manifest is null."
            )
            loadBundledConfig(CONFIG_BUNDLED_FILE_NAME)
        } else {
            loadCachedConfig(appId) ?: loadBundledConfig(CONFIG_BUNDLED_FILE_NAME)
        }

        // Replace the exiting configuration with new config.
        replaceConfiguration(config)

        return environmentAwareConfiguration
    }

    /**
     * Retrieves the configuration from config file bundled along with the app.
     *
     * @param bundledConfigFileName the file name of the bundled configuration
     * @return the configuration parsed from the bundled config file [bundledConfigFileName] if successful,
     *         null otherwise.
     */
    @VisibleForTesting
    internal fun loadBundledConfig(bundledConfigFileName: String): Map<String, Any?>? {
        Log.trace(
            ConfigurationExtension.TAG,
            LOG_TAG,
            "Attempting to load bundled config."
        )
        val contentStream: InputStream? =
            ServiceProvider.getInstance().deviceInfoService.getAsset(bundledConfigFileName)
        val contentString =
            StreamUtils.readAsString(
                contentStream
            )

        if (contentString.isNullOrEmpty()) {
            Log.debug(
                ConfigurationExtension.TAG,
                LOG_TAG,
                "Bundled config asset is not present/is empty. Cannot load bundled config."
            )
            return null
        }

        return try {
            val bundledConfigJson = JSONObject(JSONTokener(contentString))
            bundledConfigJson.toMap()
        } catch (exception: JSONException) {
            Log.debug(
                ConfigurationExtension.TAG,
                LOG_TAG,
                "Failed to load bundled config $exception"
            )
            null
        }
    }

    /**
     * Updates the existing configuration with the one from [fileAssetName]
     *
     * @param fileAssetName the asset file name from which new configuration
     *        should be read
     * @return true if the configuration has been updated with content from [fileAssetName],
     *         false otherwise
     */
    internal fun updateConfigWithFileAsset(fileAssetName: String): Boolean {
        val config = loadBundledConfig(fileAssetName)

        if (config.isNullOrEmpty()) {
            Log.debug(
                ConfigurationExtension.TAG,
                LOG_TAG,
                "Empty configuration found when processing JSON string."
            )
            return false
        }

        replaceConfiguration(config)
        return true
    }

    /**
     * Updates the existing configuration with the one from [filePath]
     *
     * @param filePath the file name from which new configuration
     *        should be read
     * @return true if the configuration has been updated with content from [filePath],
     *         false otherwise
     */
    internal fun updateConfigWithFilePath(filePath: String): Boolean {
        val config = getConfigFromFile(filePath)
        if (config == null) {
            Log.debug(
                ConfigurationExtension.TAG,
                LOG_TAG,
                "Unable to read config from provided file (content is invalid)"
            )
            return false
        }

        replaceConfiguration(config)
        return true
    }

    /**
     * Retrieves the configuration from config file specified by [filePath].
     *
     * @param filePath the file path from which the config must be retrieved
     * @return the configuration as a map parsed from the file if successful,
     *         null otherwise.
     */
    @VisibleForTesting
    internal fun getConfigFromFile(filePath: String): Map<String, Any?>? {
        val configFile = File(filePath)
        val configFileContent = FileUtils.readAsString(configFile)
        if (configFileContent.isNullOrEmpty()) {
            Log.debug(
                ConfigurationExtension.TAG,
                LOG_TAG,
                "Empty configuration from file path while configuring with file path."
            )
            return null
        }

        val config = try {
            val configJson = JSONObject(JSONTokener(configFileContent))
            configJson.toMap()
        } catch (exception: JSONException) {
            Log.warning(
                ConfigurationExtension.TAG,
                LOG_TAG,
                "Failed to parse JSON config from file while configuring with file path."
            )
            null
        }

        if (config.isNullOrEmpty()) {
            Log.debug(
                ConfigurationExtension.TAG,
                LOG_TAG,
                "Empty configuration found when processing JSON string."
            )
            return null
        }
        return config
    }

    /**
     * Retrieves the persisted programmatic configuration.
     *
     * @return the persisted programmatic configuration as a map if it exists,
     *         null otherwise.
     */
    private fun getPersistedProgrammaticConfig(): Map<String, Any?>? {
        val configStore: NamedCollection =
            ServiceProvider.getInstance().dataStoreService.getNamedCollection(DATASTORE_KEY)
        val persistedConfigContent = configStore.getString(PERSISTED_OVERRIDDEN_CONFIG, null)

        if (persistedConfigContent.isNullOrEmpty()) return null

        try {
            val overriddenConfigObj = JSONObject(JSONTokener(persistedConfigContent))
            Log.trace(
                ConfigurationExtension.TAG,
                LOG_TAG,
                "Loaded persisted programmatic Configuration"
            )
            return overriddenConfigObj.toMap()
        } catch (exception: JSONException) {
            Log.debug(
                ConfigurationExtension.TAG,
                LOG_TAG,
                "Unable to parse the Configuration from JSON Object. Exception: ($exception)"
            )
        }

        return null
    }

    /**
     * Retrieves configuration from a previously cached appId.
     *
     * @param appId the appId whose configuration is to be retrieved, if cached
     * @return configuration associated with [appId] if it was cached earlier,
     *         null otherwise
     */
    private fun loadCachedConfig(appId: String): Map<String, Any?>? {
        Log.trace(
            ConfigurationExtension.TAG,
            LOG_TAG,
            "Attempting to load cached config."
        )

        val url = String.format(CONFIGURATION_URL_BASE, appId)
        val cacheResult: CacheResult? =
            ServiceProvider.getInstance().cacheService.get(
                ConfigurationDownloader.CONFIG_CACHE_NAME,
                url
            )

        val contentString = StreamUtils.readAsString(cacheResult?.data)
        if (contentString.isNullOrEmpty()) {
            Log.trace(
                ConfigurationExtension.TAG,
                LOG_TAG,
                "Cached config is null/empty."
            )
            return null
        }

        return try {
            val cachedConfig = JSONObject(JSONTokener(contentString))
            cachedConfig.toMap()
        } catch (exception: JSONException) {
            Log.debug(
                ConfigurationExtension.TAG,
                LOG_TAG,
                "Failed to load cached config $exception"
            )
            null
        }
    }

    /**
     * Downloads and replaces the current configuration with the one associated with [appId]
     *
     * @param appId a non-null application Id which should be used for downloading the configuration
     * @param completion the callback to be invoked with the downloaded configuration
     */
    internal fun updateConfigWithAppId(appId: String, completion: (Map<String, Any?>?) -> Unit) {
        if (appId.isBlank()) {
            Log.trace(
                ConfigurationExtension.TAG,
                LOG_TAG,
                "Attempting to set empty App Id into persistence."
            )
            return
        }

        appIdManager.saveAppIdToPersistence(appId)
        val url = String.format(CONFIGURATION_URL_BASE, appId)

        configDownloader.download(url) { config ->
            if (config != null) {
                // replace the configuration with downloaded content first
                replaceConfiguration(config)

                // Update the last time of config download via appID
                configDownloadMap[appId] = Date()

                // return the environment  configuration
                completion.invoke(environmentAwareConfiguration)
            } else {
                completion.invoke(null)
            }
        }
    }

    /**
     * Clears any stored programmatic configuration and refreshes
     * the [currentConfiguration] & [environmentAwareConfiguration] with [unmergedConfiguration]
     */
    internal fun clearProgrammaticConfig() {
        // Clear programmatic config from persistence
        val configStore: NamedCollection =
            ServiceProvider.getInstance().dataStoreService.getNamedCollection(DATASTORE_KEY)
        configStore.remove(PERSISTED_OVERRIDDEN_CONFIG)

        // Clear im memory programmatic config
        programmaticConfiguration.clear()

        // Update the current configuration to reflect changes in programmatic config
        currentConfiguration.clear()
        currentConfiguration.putAll(unmergedConfiguration)
        computeEnvironmentAwareConfig()
        Log.trace(
            ConfigurationExtension.TAG,
            LOG_TAG,
            "Cleared programmatic configuration."
        )
    }

    /**
     * Changes the current configuration with the [config] provided.
     * Note that any persisted programmatic configuration will be applied on top of [config].
     *
     * @param config the configuration that should replace the [unmergedConfiguration]
     */
    @VisibleForTesting
    internal fun replaceConfiguration(config: Map<String, Any?>?) {
        // Replace the unmerged programmatic config with the new config
        unmergedConfiguration.clear()
        config?.let { unmergedConfiguration.putAll(config) }

        // Reset the current config and apply new configuration and programmatic config incrementally
        currentConfiguration.clear()
        currentConfiguration.putAll(unmergedConfiguration)
        currentConfiguration.putAll(programmaticConfiguration)
        computeEnvironmentAwareConfig()
        Log.trace(
            ConfigurationExtension.TAG,
            LOG_TAG,
            "Replaced configuration."
        )
    }

    /**
     * Updates the previously persisted programmatic config with contents of [config]
     *
     * @param config the entries which must be updated in the programmatic configuration
     */
    internal fun updateProgrammaticConfig(config: Map<String, Any?>) {
        programmaticConfiguration.putAll(config)

        // Save the new/modified programmatic config to persistence
        val configStore: NamedCollection =
            ServiceProvider.getInstance().dataStoreService.getNamedCollection(DATASTORE_KEY)
        val jsonString = JSONObject(programmaticConfiguration).toString()
        configStore.setString(PERSISTED_OVERRIDDEN_CONFIG, jsonString)

        // Update the current configuration to reflect changes in programmatic config
        currentConfiguration.putAll(programmaticConfiguration)
        computeEnvironmentAwareConfig()
        Log.debug(
            ConfigurationExtension.TAG,
            LOG_TAG,
            "Updated programmatic configuration."
        )
    }

    /**
     * Checks if the configuration associated with [appId] has been downloaded and is unexpired
     *
     * @return true if the configuration associated with [appId] has been downloaded and is unexpired,
     *         false otherwise
     */
    internal fun hasConfigExpired(appId: String): Boolean {
        val latestDownloadDate: Date? = configDownloadMap[appId]
        return latestDownloadDate == null || Date(latestDownloadDate.time + CONFIGURATION_TTL_MS) < Date()
    }

    /**
     * Computes the configuration for the current environment (defined by [BUILD_ENVIRONMENT] in the config)
     * based on [currentConfiguration] and updates [environmentAwareConfiguration]
     */
    private fun computeEnvironmentAwareConfig() {
        val buildEnvironment: String = currentConfiguration[BUILD_ENVIRONMENT] as? String ?: ""

        // use this map to accumulate the environment aware configuration
        val rollingEnvironmentAwareMap = mutableMapOf<String, Any?>()

        // NOTE: [currentConfiguration] assumes that all config keys for an app have an associated prod key.
        // So it it sufficient to iterate over it and replace values by forming environment based keys.
        // This logic needs to change if the assumption is no longer valid.
        currentConfiguration.entries.forEach { entry ->
            val key = entry.key

            // We only want to construct environment keys and find environment specific values for prod keys.
            if (!key.startsWith(ENVIRONMENT_PREFIX_DELIMITER)) {
                var environmentAwareKey = getKeyForEnvironment(key, buildEnvironment)
                environmentAwareKey = if (currentConfiguration[environmentAwareKey] != null) {
                    environmentAwareKey
                } else {
                    key
                }

                // If a config value for the current build environment exists, use `key`'s
                // value with `environmentKey`'s value
                val value = currentConfiguration[environmentAwareKey]
                value?.let { rollingEnvironmentAwareMap[key] = value }
            }
        }

        environmentAwareConfiguration = rollingEnvironmentAwareMap
    }

    /**
     * Returns the correct key from configuration json based on the build environment and base key
     *
     * For configuration, the base_key will always be the name of the production configuration value. e.g. :
     * - Production Key  -  myKeyName
     * - Staging Key     -  __stage__myKeyName
     * - Development Key -  __dev__myKeyName
     *
     * @param baseKey the production key name to use as the base for the result
     * @param environment the value from build.environment in the configuration json provided by Launch
     * @return a string representing the correct key to use given the provided environment
     */
    private fun getKeyForEnvironment(baseKey: String, environment: String): String {
        return if (environment.isEmpty()) {
            baseKey
        } else {
            ENVIRONMENT_PREFIX_DELIMITER + environment + ENVIRONMENT_PREFIX_DELIMITER + baseKey
        }
    }
}
