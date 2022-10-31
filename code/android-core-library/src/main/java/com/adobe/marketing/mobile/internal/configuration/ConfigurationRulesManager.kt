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
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.launch.rulesengine.LaunchRulesEvaluator
import com.adobe.marketing.mobile.launch.rulesengine.download.RulesLoadResult
import com.adobe.marketing.mobile.launch.rulesengine.download.RulesLoader
import com.adobe.marketing.mobile.launch.rulesengine.json.JSONRulesParser
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.NamedCollection
import com.adobe.marketing.mobile.services.ServiceProvider

/**
 * Facilitates notifying [LaunchRulesEvaluator] about replacing current rules with cached or newly
 * downloaded rules.
 */
internal class ConfigurationRulesManager {

    companion object {
        private const val LOG_TAG = "ConfigurationRulesManager"
        internal const val PERSISTED_RULES_URL = "config.last.rules.url"
        internal const val RULES_CACHE_NAME = "config.rules"
        internal const val BUNDLED_RULES_FILE_NAME = "ADBMobileConfig-rules.zip"
    }

    private val launchRulesEvaluator: LaunchRulesEvaluator
    private val rulesLoader: RulesLoader
    private val configDataStore: NamedCollection?

    constructor(launchRulesEvaluator: LaunchRulesEvaluator) : this(
        launchRulesEvaluator,
        RulesLoader(RULES_CACHE_NAME)
    )

    @VisibleForTesting
    constructor(launchRulesEvaluator: LaunchRulesEvaluator, rulesLoader: RulesLoader) {
        this.launchRulesEvaluator = launchRulesEvaluator
        this.rulesLoader = rulesLoader
        configDataStore =
            ServiceProvider.getInstance().dataStoreService.getNamedCollection(ConfigurationExtension.DATASTORE_KEY)
    }

    /**
     * Replaces the rules with the ones cached locally.
     *
     * @return true if a rule replacement was triggered, false otherwise
     */
    internal fun applyCachedRules(extensionApi: ExtensionApi): Boolean {

        if (configDataStore == null) {
            Log.debug(
                ConfigurationExtension.TAG,
                LOG_TAG,
                "Cannot load rules from ${ConfigurationExtension.DATASTORE_KEY}. Cannot apply cached rules"
            )
            return false
        }

        val persistedRulesUrl =
            configDataStore.getString(PERSISTED_RULES_URL, null)

        if (persistedRulesUrl.isNullOrBlank()) {
            Log.debug(
                ConfigurationExtension.TAG,
                LOG_TAG,
                "Persisted rules url is null or empty. Cannot apply cached rules"
            )
            return false
        }

        val rulesLoadResult: RulesLoadResult = rulesLoader.loadFromCache(persistedRulesUrl)

        if (rulesLoadResult.reason != RulesLoadResult.Reason.SUCCESS) {
            Log.debug(
                ConfigurationExtension.TAG,
                LOG_TAG,
                "Cannot apply cached rules - ${rulesLoadResult.reason}"
            )
            return false
        }

        Log.trace(
            ConfigurationExtension.TAG,
            LOG_TAG,
            "Attempting to replace rules with cached rules"
        )
        return replaceRules(rulesLoadResult.data, extensionApi)
    }

    /**
     * Replaces the rules after downloading them from a URL.
     *
     * @param url the URL from which the rules must be downloaded
     * @return true if a rule replacement was triggered, false otherwise
     */
    internal fun applyDownloadedRules(url: String, extensionApi: ExtensionApi): Boolean {
        if (configDataStore == null) {
            Log.debug(
                ConfigurationExtension.TAG,
                LOG_TAG,
                "Cannot load rules from ${ConfigurationExtension.DATASTORE_KEY}. Cannot apply downloaded rules"
            )
            return false
        }

        configDataStore.setString(PERSISTED_RULES_URL, url)

        rulesLoader.loadFromUrl(url, null) { rulesDownloadResult ->
            val reason = rulesDownloadResult.reason
            Log.trace(
                ConfigurationExtension.TAG,
                LOG_TAG,
                "Rule Download result: $reason"
            )

            if (reason == RulesLoadResult.Reason.NOT_MODIFIED) {
                Log.debug(
                    ConfigurationExtension.TAG,
                    LOG_TAG,
                    "Rules from $url have not been modified. Will not apply rules."
                )
            } else {
                Log.trace(
                    ConfigurationExtension.TAG,
                    LOG_TAG,
                    "Attempting to replace rules with downloaded rules."
                )

                replaceRules(rulesDownloadResult.data, extensionApi)
            }
        }

        return true
    }

    /**
     * Loads and replaces the existing rules with ones from asset with name [BUNDLED_RULES_FILE_NAME]
     *
     * @return true if a rule replacement was triggered, false otherwise
     */
    internal fun applyBundledRules(api: ExtensionApi): Boolean {
        val rulesLoadResult: RulesLoadResult =
            rulesLoader.loadFromAsset(BUNDLED_RULES_FILE_NAME)
        if (rulesLoadResult.reason != RulesLoadResult.Reason.SUCCESS) {
            Log.debug(
                ConfigurationExtension.TAG,
                LOG_TAG,
                "Cannot apply bundled rules - ${rulesLoadResult.reason}"
            )
            return false
        }

        Log.trace(
            ConfigurationExtension.TAG,
            LOG_TAG,
            "Attempting to replace rules with bundled rules"
        )
        return replaceRules(rulesLoadResult.data, api)
    }

    /**
     * Parses the rules from [rulesJson] and notifies [LaunchRulesEvaluator]
     * about the new rules.
     *
     * @param rulesJson the input json string from which rules must be parsed
     * @param extensionApi extensionApi
     * @return true if a rule replacement was triggered, false otherwise
     */
    private fun replaceRules(rulesJson: String?, extensionApi: ExtensionApi): Boolean {
        if (rulesJson == null) {
            Log.debug(
                ConfigurationExtension.TAG,
                LOG_TAG,
                "Rules file content is null. Cannot apply new rules."
            )
            return false
        }

        val rules = JSONRulesParser.parse(rulesJson, extensionApi)
        return if (rules == null) {
            Log.debug(
                ConfigurationExtension.TAG,
                LOG_TAG,
                "Parsed rules are null. Cannot apply new rules."
            )
            false
        } else {
            Log.trace(
                ConfigurationExtension.TAG,
                LOG_TAG,
                "Replacing rules."
            )
            launchRulesEvaluator.replaceRules(rules)
            true
        }
    }
}
