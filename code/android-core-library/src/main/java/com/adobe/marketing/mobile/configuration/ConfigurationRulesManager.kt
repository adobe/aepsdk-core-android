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

package com.adobe.marketing.mobile.configuration

import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.internal.utility.FileUtils
import com.adobe.marketing.mobile.launch.rulesengine.LaunchRulesEvaluator
import com.adobe.marketing.mobile.launch.rulesengine.json.JSONRulesParser
import com.adobe.marketing.mobile.services.CacheFileService
import com.adobe.marketing.mobile.services.DataStoring
import com.adobe.marketing.mobile.services.NamedCollection
import com.adobe.marketing.mobile.services.Networking
import java.io.File

/**
 * Facilitates notifying [LaunchRulesEvaluator] about replacing current rules with cached or newly
 * downloaded rules.
 */
internal class ConfigurationRulesManager(
    private val launchRulesEvaluator: LaunchRulesEvaluator,
    private val dataStoreService: DataStoring,
    private val networkService: Networking,
    private val cacheFileService: CacheFileService
) {

    companion object {
        private const val TAG = "ConfigurationRulesManager"
        internal const val PERSISTED_RULES_URL = "config.last.rules.url"
        internal const val RULES_CACHE_FOLDER = "configRules"
        internal const val RULES_JSON_FILE_NAME = "rules.json"
    }

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
     */
    internal fun applyCachedRules(extensionApi: ExtensionApi) {
        val rulesCollection: NamedCollection? = dataStoreService.getNamedCollection(
            ConfigurationExtension.DATASTORE_KEY
        )

        if (rulesCollection == null) {
            MobileCore.log(
                LoggingMode.VERBOSE,
                TAG,
                "Cannot load rules from ${ConfigurationExtension.DATASTORE_KEY}. Cannot apply cached rules"
            )
            return
        }

        val persistedRulesUrl =
            rulesCollection.getString(PERSISTED_RULES_URL, null)

        if (persistedRulesUrl.isNullOrBlank()) {
            MobileCore.log(
                LoggingMode.VERBOSE,
                TAG,
                "Persisted rules url is null or empty. Cannot apply cached rules"
            )
            return
        }

        val cachedRulesDirectory: File? = cacheFileService
            .getCacheFile(
                persistedRulesUrl,
                RULES_CACHE_FOLDER,
                false
            )
        replaceRules(cachedRulesDirectory, extensionApi)
    }

    /**
     * Replaces the rules after downloading them from a URL.
     *
     * @param url the URL from which the rules must be downloaded
     */
    internal fun applyDownloadedRules(url: String, extensionApi: ExtensionApi) {
        val rulesCollection: NamedCollection? = dataStoreService.getNamedCollection(
            ConfigurationExtension.DATASTORE_KEY
        )

        if (rulesCollection == null) {
            MobileCore.log(
                LoggingMode.VERBOSE,
                TAG,
                "Cannot load rules from ${ConfigurationExtension.DATASTORE_KEY}. Cannot apply cached rules"
            )
            return
        }

        rulesCollection.setString(PERSISTED_RULES_URL, url)

        val configurationRulesDownloader: ConfigurationRulesDownloader =
            configRulesDownloaderSupplier(
                networkService,
                cacheFileService,
                ZipFileMetadataProvider()
            )
        configurationRulesDownloader.download(url, RULES_CACHE_FOLDER) { rulesFileDir ->
            replaceRules(rulesFileDir, extensionApi)
        }
    }

    /**
     * Parses the rules from [rulesDirectory] and notifies [LaunchRulesEvaluator]
     * about the new rules.
     *
     * @param rulesDirectory the directory from which rules must be parsed
     */
    private fun replaceRules(rulesDirectory: File?, extensionApi: ExtensionApi) {
        if (rulesDirectory == null || !rulesDirectory.isDirectory) {
            MobileCore.log(
                LoggingMode.VERBOSE,
                TAG,
                "Invalid rules directory: $rulesDirectory. Cannot apply cached rules"
            )
            return
        }

        val rulesFilePath =
            "${rulesDirectory.path}${File.separator}$RULES_JSON_FILE_NAME"
        val rulesFile = File(rulesFilePath)
        val content = FileUtils.readAsString(rulesFile)
        if (content != null) {
            val rules = JSONRulesParser.parse(content, extensionApi)
            if (rules == null) {
                MobileCore.log(
                    LoggingMode.VERBOSE,
                    TAG,
                    "Parsed rules are null. Cannot apply new rules."
                )
            } else {
                launchRulesEvaluator.replaceRules(rules)
            }
        } else {
            MobileCore.log(
                LoggingMode.VERBOSE,
                TAG,
                "Rules file content is null. Cannot apply new rules."
            )
        }
    }
}
