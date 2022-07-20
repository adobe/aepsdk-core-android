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

import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.Log
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.internal.util.FileUtils
import com.adobe.marketing.mobile.launch.rulesengine.LaunchRulesEvaluator
import com.adobe.marketing.mobile.launch.rulesengine.json.JSONRulesParser
import com.adobe.marketing.mobile.services.CacheFileService
import com.adobe.marketing.mobile.services.DataStoring
import com.adobe.marketing.mobile.services.DeviceInforming
import com.adobe.marketing.mobile.services.NamedCollection
import com.adobe.marketing.mobile.services.Networking
import java.io.File
import java.io.InputStream

/**
 * Facilitates notifying [LaunchRulesEvaluator] about replacing current rules with cached or newly
 * downloaded rules.
 */
internal class ConfigurationRulesManager(
    private val launchRulesEvaluator: LaunchRulesEvaluator,
    dataStoreService: DataStoring,
    private val deviceInfoService: DeviceInforming,
    private val networkService: Networking,
    private val cacheFileService: CacheFileService
) {

    companion object {
        private const val LOG_TAG = "ConfigurationRulesManager"
        internal const val PERSISTED_RULES_URL = "config.last.rules.url"
        internal const val RULES_CACHE_FOLDER = "configRules"
        internal const val RULES_JSON_FILE_NAME = "rules.json"
        internal const val BUNDLED_RULES_FILE_NAME = "ADBMobileConfig-rules.zip"
        internal const val BUNDLED_RULES_DIR = "ADBMobileConfig-rules"
        private const val ADOBE_CACHE_DIR = "adbdownloadcache"
    }

    private val configDataStore: NamedCollection? =
        dataStoreService.getNamedCollection(ConfigurationExtension.DATASTORE_KEY)

    /**
     * A convenience wrapper for supplying a new [ConfigurationRulesDownloader] to allow mocking in tests.
     */
    private val configRulesDownloaderSupplier: (Networking, CacheFileService, ZipFileMetadataProvider) -> ConfigurationRulesDownloader =
        { networkService, fileCacheService, metadataProvider ->
            ConfigurationRulesDownloader(
                networkService,
                fileCacheService,
                metadataProvider
            )
        }

    /**
     * Replaces the rules with the ones cached locally.
     *
     * @return true if a rule replacement was triggered, false otherwise
     */
    internal fun applyCachedRules(extensionApi: ExtensionApi): Boolean {

        if (configDataStore == null) {
            MobileCore.log(
                LoggingMode.VERBOSE,
                ConfigurationExtension.TAG,
                "$LOG_TAG - Cannot load rules from ${ConfigurationExtension.DATASTORE_KEY}. Cannot apply cached rules"
            )
            return false
        }

        val persistedRulesUrl =
            configDataStore.getString(PERSISTED_RULES_URL, null)

        if (persistedRulesUrl.isNullOrBlank()) {
            MobileCore.log(
                LoggingMode.VERBOSE,
                ConfigurationExtension.TAG,
                "$LOG_TAG - Persisted rules url is null or empty. Cannot apply cached rules"
            )
            return false
        }

        val cachedRulesDirectory: File? = cacheFileService
            .getCacheFile(
                persistedRulesUrl,
                RULES_CACHE_FOLDER,
                false
            )
        return replaceRules(cachedRulesDirectory, extensionApi)
    }

    /**
     * Replaces the rules after downloading them from a URL.
     *
     * @param url the URL from which the rules must be downloaded
     * @return true if a rule replacement was triggered, false otherwise
     */
    internal fun applyDownloadedRules(url: String, extensionApi: ExtensionApi): Boolean {
        if (configDataStore == null) {
            Log.trace(
                ConfigurationExtension.TAG,
                LOG_TAG,
                "$LOG_TAG - Cannot load rules from ${ConfigurationExtension.DATASTORE_KEY}. Cannot apply cached rules"
            )
            return false
        }

        configDataStore.setString(PERSISTED_RULES_URL, url)

        val configurationRulesDownloader: ConfigurationRulesDownloader =
            configRulesDownloaderSupplier(
                networkService,
                cacheFileService,
                ZipFileMetadataProvider()
            )
        configurationRulesDownloader.download(url, RULES_CACHE_FOLDER) { rulesFileDir ->
            replaceRules(rulesFileDir, extensionApi)
        }
        return true
    }

    /**
     * Loads and replaced the existing rules with ones from asset with name [BUNDLED_RULES_FILE_NAME]
     *
     * @return true if a rule replacement was triggered, false otherwise
     */
    internal fun applyBundledRules(api: ExtensionApi): Boolean {
        MobileCore.log(
            LoggingMode.DEBUG,
            ConfigurationExtension.TAG,
            "$LOG_TAG - Attempting to apply bundled rules."
        )
        val applicationCacheDir = deviceInfoService.applicationCacheDir?.absolutePath
        if (applicationCacheDir == null) {
            MobileCore.log(
                LoggingMode.VERBOSE,
                ConfigurationExtension.TAG,
                "$LOG_TAG - Cannot locate application cache directory. Will not load bundled rules."
            )
            return false
        }

        val cacheDir = File(applicationCacheDir, ADOBE_CACHE_DIR)

        MobileCore.log(LoggingMode.VERBOSE, LOG_TAG, "Cache dir is: $cacheDir")
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            MobileCore.log(
                LoggingMode.VERBOSE,
                ConfigurationExtension.TAG,
                "$LOG_TAG - Cannot create/write to cache directory. Will not load bundled rules."
            )
            return false
        }

        val bundledRulesStream: InputStream? = deviceInfoService.getAsset(BUNDLED_RULES_FILE_NAME)
        if (bundledRulesStream == null) {
            MobileCore.log(
                LoggingMode.VERBOSE,
                ConfigurationExtension.TAG,
                "$LOG_TAG - Cannot load bundled rules."
            )
            return false
        }

        // location in the cache where the asset stream should be read into
        val bundledRulesFilePath = cacheDir.path + File.separator + BUNDLED_RULES_FILE_NAME
        val fileRead =
            FileUtils.readInputStreamIntoFile(File(bundledRulesFilePath), bundledRulesStream, false)

        if (!fileRead) {
            MobileCore.log(
                LoggingMode.VERBOSE,
                ConfigurationExtension.TAG,
                "$LOG_TAG - Failed to read bundled rules into cache."
            )
            return false
        }

        val extracted = FileUtils.extractFromZip(File(bundledRulesFilePath), cacheDir.path)

        if (!extracted) {
            MobileCore.log(
                LoggingMode.VERBOSE, ConfigurationExtension.TAG,
                "$LOG_TAG - Failed to extract bundled rules."
            )
            return false
        }

        MobileCore.log(
            LoggingMode.VERBOSE, ConfigurationExtension.TAG,
            "$LOG_TAG - Applying bundled rules."
        )

        return replaceRules(File(cacheDir.path + File.separator + BUNDLED_RULES_DIR), api)
    }

    /**
     * Parses the rules from [rulesDirectory] and notifies [LaunchRulesEvaluator]
     * about the new rules.
     *
     * @param rulesDirectory the directory from which rules must be parsed
     * @param extensionApi extensionApi
     * @return true if a rule replacement was triggered, false otherwise
     */
    private fun replaceRules(rulesDirectory: File?, extensionApi: ExtensionApi): Boolean {
        if (rulesDirectory == null || !rulesDirectory.isDirectory) {
            MobileCore.log(
                LoggingMode.VERBOSE,
                ConfigurationExtension.TAG,
                "$LOG_TAG - Invalid rules directory: $rulesDirectory. Cannot apply cached rules"
            )
            return false
        }

        val rulesFilePath = "${rulesDirectory.path}${File.separator}$RULES_JSON_FILE_NAME"
        val rulesFile = File(rulesFilePath)
        val content = FileUtils.readAsString(rulesFile)
        if (content == null) {
            MobileCore.log(
                LoggingMode.VERBOSE,
                ConfigurationExtension.TAG,
                "$LOG_TAG - Rules file content is null. Cannot apply new rules."
            )
            return false
        }

        val rules = JSONRulesParser.parse(content, extensionApi)
        return if (rules == null) {
            MobileCore.log(
                LoggingMode.VERBOSE,
                ConfigurationExtension.TAG,
                "$LOG_TAG - Parsed rules are null. Cannot apply new rules."
            )
            false
        } else {
            MobileCore.log(
                LoggingMode.VERBOSE,
                ConfigurationExtension.TAG,
                "$LOG_TAG - Replacing rules."
            )
            launchRulesEvaluator.replaceRules(rules)
            true
        }
    }
}
