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
import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.EventSource
import com.adobe.marketing.mobile.EventType
import com.adobe.marketing.mobile.Extension
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.SharedStateResolver
import com.adobe.marketing.mobile.internal.compatibility.CacheManager
import com.adobe.marketing.mobile.internal.eventhub.EventHub
import com.adobe.marketing.mobile.launch.rulesengine.LaunchRulesEngine
import com.adobe.marketing.mobile.launch.rulesengine.LaunchRulesEvaluator
import com.adobe.marketing.mobile.services.CacheFileService
import com.adobe.marketing.mobile.services.ServiceProvider
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Configuration Extension.
 * Responsible for retrieving the configuration of the SDK, updating the shared state and
 * dispatching configuration updates through the `EventHub`
 */
internal class ConfigurationExtension : Extension {

    companion object {
        internal const val TAG = "ConfigurationExtension"
        private const val EXTENSION_NAME = "com.adobe.module.configuration"
        private const val EXTENSION_FRIENDLY_NAME = "Configuration"
        private const val EXTENSION_VERSION = "2.0.0"

        internal const val CONFIGURATION_REQUEST_CONTENT_JSON_APP_ID = "config.appId"
        internal const val CONFIGURATION_REQUEST_CONTENT_JSON_FILE_PATH = "config.filePath"
        internal const val CONFIGURATION_REQUEST_CONTENT_JSON_ASSET_FILE = "config.assetFile"
        internal const val CONFIGURATION_REQUEST_CONTENT_UPDATE_CONFIG = "config.update"
        internal const val CONFIGURATION_REQUEST_CONTENT_CLEAR_UPDATED_CONFIG =
            "config.clearUpdates"
        internal const val CONFIGURATION_REQUEST_CONTENT_RETRIEVE_CONFIG = "config.getData"
        internal const val CONFIGURATION_REQUEST_CONTENT_IS_INTERNAL_EVENT =
            "config.isinternalevent"
        internal const val DATASTORE_KEY = "AdobeMobile_ConfigState"
        internal const val RULES_CONFIG_URL = "rules.url"
        internal const val CONFIG_DOWNLOAD_RETRY_ATTEMPT_DELAY_MS = 5L
        internal const val CONFIGURATION_RESPONSE_IDENTITY_ALL_IDENTIFIERS = "config.allIdentifiers"
        internal const val EVENT_STATE_OWNER = "stateowner"
        internal const val GLOBAL_CONFIG_PRIVACY = "global.privacy"
    }

    /**
     * Represents the source from which rules can be loaded.
     */
    private enum class RulesSource {
        CACHE,
        BUNDLED,
        REMOTE
    }

    private val serviceProvider: ServiceProvider
    private val appIdManager: AppIdManager
    private val cacheFileService: CacheFileService
    private val launchRulesEvaluator: LaunchRulesEvaluator
    private val configurationStateManager: ConfigurationStateManager
    private val configurationRulesManager: ConfigurationRulesManager
    private val retryWorker: ScheduledExecutorService
    private var retryConfigurationCounter: Int = 0
    private var retryConfigTaskHandle: Future<*>? = null

    constructor(extensionApi: ExtensionApi) : this(extensionApi, ServiceProvider.getInstance())

    /**
     * Exists only for cascading components for dependency injection.
     */
    private constructor(
        extensionApi: ExtensionApi,
        serviceProvider: ServiceProvider
    ) : this(
        extensionApi, serviceProvider,
        AppIdManager(serviceProvider.dataStoreService, serviceProvider.deviceInfoService),
        CacheManager(serviceProvider.deviceInfoService),
        LaunchRulesEvaluator("Configuration", LaunchRulesEngine(extensionApi), extensionApi),
        Executors.newSingleThreadScheduledExecutor()
    )

    /**
     * Exists only for cascading components for dependency injection.
     */
    private constructor(
        extensionApi: ExtensionApi,
        serviceProvider: ServiceProvider,
        appIdManager: AppIdManager,
        cacheFileService: CacheFileService,
        launchRulesEvaluator: LaunchRulesEvaluator,
        retryWorker: ScheduledExecutorService
    ) : this(
        extensionApi,
        serviceProvider,
        appIdManager,
        cacheFileService,
        launchRulesEvaluator,
        retryWorker,
        ConfigurationStateManager(
            appIdManager,
            cacheFileService,
            serviceProvider.networkService,
            serviceProvider.deviceInfoService,
            serviceProvider.dataStoreService
        ),
        ConfigurationRulesManager(
            launchRulesEvaluator,
            serviceProvider.dataStoreService,
            serviceProvider.deviceInfoService,
            serviceProvider.networkService,
            cacheFileService
        )
    )

    @VisibleForTesting
    internal constructor(
        extensionApi: ExtensionApi,
        serviceProvider: ServiceProvider,
        appIdManager: AppIdManager,
        cacheFileService: CacheFileService,
        launchRulesEvaluator: LaunchRulesEvaluator,
        retryWorker: ScheduledExecutorService,
        configurationStateManager: ConfigurationStateManager,
        configurationRulesManager: ConfigurationRulesManager
    ) : super(extensionApi) {
        this.serviceProvider = serviceProvider
        this.appIdManager = appIdManager
        this.cacheFileService = cacheFileService
        this.launchRulesEvaluator = launchRulesEvaluator
        this.retryWorker = retryWorker
        this.configurationStateManager = configurationStateManager
        this.configurationRulesManager = configurationRulesManager

        loadInitialConfiguration()

        EventHub.shared.registerEventPreprocessor {
            launchRulesEvaluator.process(it)
        }
    }

    /**
     * Loads the initial configuration.
     */
    private fun loadInitialConfiguration() {
        val appId = this.appIdManager.loadAppId()

        if (!appId.isNullOrBlank()) {
            val eventData: MutableMap<String, Any?> =
                mutableMapOf(
                    CONFIGURATION_REQUEST_CONTENT_JSON_APP_ID to appId,
                    CONFIGURATION_REQUEST_CONTENT_IS_INTERNAL_EVENT to true
                )
            dispatchConfigurationRequest(eventData)
        }

        val initialConfig: Map<String, Any?> = this.configurationStateManager.loadInitialConfig()

        if (initialConfig.isNotEmpty()) {
            applyConfigurationChanges(null, RulesSource.CACHE, null)
        } else {
            MobileCore.log(
                LoggingMode.VERBOSE,
                TAG,
                "Initial configuration loaded is empty."
            )
        }
    }

    /**
     * Sets up the Configuration extension for the SDK by registering
     * event listeners for processing events.
     */
    override fun onRegistered() {
        super.onRegistered()

        // States should not be created in the constructor of the Extension.
        // Publish initial state that is pre-computed in the constructor when the
        // registration completes.
        val initialConfigState = configurationStateManager.environmentAwareConfiguration
        if (initialConfigState.isNotEmpty()) {
            publishConfigurationState(initialConfigState, null)
        }

        api.registerEventListener(
            EventType.CONFIGURATION,
            EventSource.REQUEST_CONTENT
        ) {
            handleConfigurationRequestEvent(it)
        }
    }

    override fun getName(): String {
        return EXTENSION_NAME
    }

    override fun getFriendlyName(): String {
        return EXTENSION_FRIENDLY_NAME
    }

    override fun getVersion(): String {
        return EXTENSION_VERSION
    }

    /**
     * Responsible for handling the incoming event requests directed towards [ConfigurationExtension].
     *
     * @param event the event request dispatched to the [ConfigurationExtension]
     */
    internal fun handleConfigurationRequestEvent(event: Event) {
        if (event.eventData == null) return

        when {
            event.eventData.containsKey(CONFIGURATION_REQUEST_CONTENT_JSON_APP_ID) -> {
                configureWithAppID(event, api.createPendingSharedState(event))
            }
            event.eventData.containsKey(CONFIGURATION_REQUEST_CONTENT_JSON_ASSET_FILE) -> {
                configureWithFileAsset(event, api.createPendingSharedState(event))
            }
            event.eventData.containsKey(CONFIGURATION_REQUEST_CONTENT_JSON_FILE_PATH) -> {
                configureWithFilePath(event, api.createPendingSharedState(event))
            }
            event.eventData.containsKey(CONFIGURATION_REQUEST_CONTENT_UPDATE_CONFIG) -> {
                updateConfiguration(event, api.createPendingSharedState(event))
            }
            event.eventData.containsKey(CONFIGURATION_REQUEST_CONTENT_CLEAR_UPDATED_CONFIG) -> {
                clearUpdatedConfiguration(event, api.createPendingSharedState(event))
            }
            event.eventData.containsKey(CONFIGURATION_REQUEST_CONTENT_RETRIEVE_CONFIG) -> {
                retrieveConfiguration(event)
            }
        }
    }

    /**
     * Downloads the configuration file based on the appId provided with the [event] and updates
     * the current app configuration with the resulting downloaded configuration.
     *
     * @param event the event requesting/triggering an update to configuration with appId.
     * @param sharedStateResolver the resolver should be used for resolving the current state
     */
    private fun configureWithAppID(event: Event, sharedStateResolver: SharedStateResolver) {
        val appId =
            event.eventData?.get(CONFIGURATION_REQUEST_CONTENT_JSON_APP_ID) as? String

        if (appId.isNullOrBlank()) {
            MobileCore.log(
                LoggingMode.VERBOSE,
                TAG,
                "AppId in configureWithAppID event is null.."
            )

            appIdManager.removeAppIdFromPersistence()
            sharedStateResolver.resolve(configurationStateManager.environmentAwareConfiguration)
            return
        }

        if (!configurationStateManager.hasConfigExpired(appId)) {
            sharedStateResolver.resolve(configurationStateManager.environmentAwareConfiguration)
            return
        }

        // Stop all event processing for the extension until new configuration download is attempted
        api.stopEvents()

        configurationStateManager.updateConfigWithAppId(appId) { config ->
            if (config != null) {
                cancelConfigRetry()
                applyConfigurationChanges(event, RulesSource.REMOTE, sharedStateResolver)
            } else {
                MobileCore.log(
                    LoggingMode.VERBOSE,
                    TAG,
                    "Failed to download configuration. Applying Will retry download."
                )

                // If the configuration download fails, publish current configuration and retry download again.
                sharedStateResolver.resolve(configurationStateManager.environmentAwareConfiguration)

                retryConfigTaskHandle = retryConfigDownload(appId)
            }

            // Start event processing again
            api.startEvents()
        }
    }

    /**
     * Retrieves the configuration from the file path specified in the [event]'s data and replaces
     * the current configuration with it.
     *
     * @param event the trigger event (whose event data contains the file path for retrieving the config)
     *              requesting a configuration change
     * @param sharedStateResolver the resolver should be used for resolving the current state
     */
    private fun configureWithFilePath(event: Event, sharedStateResolver: SharedStateResolver) {
        val filePath =
            event.eventData?.get(CONFIGURATION_REQUEST_CONTENT_JSON_FILE_PATH) as String?

        if (filePath.isNullOrBlank()) {
            MobileCore.log(
                LoggingMode.WARNING,
                TAG,
                "Unable to read config from provided file (filePath: $filePath is invalid)"
            )
            sharedStateResolver.resolve(configurationStateManager.environmentAwareConfiguration)
            return
        }

        val result = configurationStateManager.updateConfigWithFilePath(filePath)
        if (result) {
            applyConfigurationChanges(event, RulesSource.REMOTE, sharedStateResolver)
        } else {
            MobileCore.log(
                LoggingMode.DEBUG,
                TAG,
                "Could not update configuration from file path: $filePath"
            )
            sharedStateResolver.resolve(configurationStateManager.environmentAwareConfiguration)
        }
    }

    /**
     * Updates the current configuration with the content from a file asset.
     *
     * @param event which contains [CONFIGURATION_REQUEST_CONTENT_JSON_ASSET_FILE] as part of its event data
     * @param sharedStateResolver the resolver should be used for resolving the current state
     */
    private fun configureWithFileAsset(event: Event, sharedStateResolver: SharedStateResolver) {
        val fileAssetName =
            event.eventData?.get(CONFIGURATION_REQUEST_CONTENT_JSON_ASSET_FILE) as String?

        if (fileAssetName.isNullOrBlank()) {
            MobileCore.log(
                LoggingMode.DEBUG,
                TAG,
                "Asset file name for configuration is null or empty."
            )
            sharedStateResolver.resolve(configurationStateManager.environmentAwareConfiguration)
            return
        }

        val result = configurationStateManager.updateConfigWithFileAsset(fileAssetName)
        if (result) {
            applyConfigurationChanges(event, RulesSource.REMOTE, sharedStateResolver)
        } else {
            MobileCore.log(
                LoggingMode.DEBUG,
                TAG,
                "Could not update configuration from file asset: $fileAssetName"
            )
            sharedStateResolver.resolve(configurationStateManager.environmentAwareConfiguration)
        }
    }

    /**
     * Updates the programmatic configuration with the programmatic config in [event]'s data.
     *
     * @param event the event containing programmatic configuration that is to be applied on the
     *              current configuration
     * @param sharedStateResolver the resolver should be used for resolving the current state
     */
    @Suppress("UNCHECKED_CAST")
    private fun updateConfiguration(event: Event, sharedStateResolver: SharedStateResolver) {
        val config: MutableMap<*, *> =
            event.eventData?.get(CONFIGURATION_REQUEST_CONTENT_UPDATE_CONFIG) as?
                MutableMap<*, *> ?: return

        if (!config.keys.isAllString()) {
            MobileCore.log(
                LoggingMode.DEBUG,
                TAG,
                "Invalid configuration. Configuration contains non string keys."
            )
            sharedStateResolver.resolve(configurationStateManager.environmentAwareConfiguration)
            return
        }

        val programmaticConfig = try {
            config as? Map<String, Any?>
        } catch (e: Exception) {
            MobileCore.log(
                LoggingMode.VERBOSE,
                TAG,
                "Failed to load programmatic config. Invalid configuration."
            )
            sharedStateResolver.resolve(configurationStateManager.environmentAwareConfiguration)
            null
        }

        programmaticConfig?.let {
            configurationStateManager.updateProgrammaticConfig(it)
            applyConfigurationChanges(event, RulesSource.REMOTE, sharedStateResolver)
        }
    }

    /**
     * Clears any updates made to the configuration (more specifically the programmatic config)
     * maintained by the extension.
     *
     * @param event an event requesting configuration to be cleared.
     */
    private fun clearUpdatedConfiguration(event: Event, sharedStateResolver: SharedStateResolver) {
        configurationStateManager.clearProgrammaticConfig()
        applyConfigurationChanges(event, RulesSource.REMOTE, sharedStateResolver)
    }

    /**
     * Dispatches an event containing the current configuration
     *
     * @param event the event to which the current configuration should be
     *        dispatched as a response
     */
    private fun retrieveConfiguration(event: Event) {
        dispatchConfigurationResponse(
            configurationStateManager.environmentAwareConfiguration,
            event
        )
    }

    /**
     * Attempts to download the configuration associated with [appId] by dispatching
     * an internal configuration request event.
     *
     * @param appId the appId for which the config download should be attempted
     * @return the [Future] associated with the runnable that dispatches the configuration request event
     */
    private fun retryConfigDownload(appId: String): Future<*> {
        val retryDelay = ++retryConfigurationCounter * CONFIG_DOWNLOAD_RETRY_ATTEMPT_DELAY_MS
        return retryWorker.schedule(
            {
                dispatchConfigurationRequest(
                    mutableMapOf(
                        CONFIGURATION_REQUEST_CONTENT_JSON_APP_ID to appId,
                        CONFIGURATION_REQUEST_CONTENT_IS_INTERNAL_EVENT to true
                    )
                )
            },
            retryDelay,
            TimeUnit.SECONDS
        )
    }

    /**
     * Cancels the configuration retry attempt and resets the retry counter.
     */
    private fun cancelConfigRetry() {
        retryConfigTaskHandle?.cancel(false)
        retryConfigTaskHandle = null
        retryConfigurationCounter = 0
    }

    /**
     * Does three things
     *  - Publishes the current configuration as configuration state at [triggerEvent] per [publishState]
     *  - Dispatches an event response to the [triggerEvent]
     *  - Replaces current rules and applies the new rules based on [rulesSource]
     *
     *  @param triggerEvent the event that triggered the configuration change
     *  @param rulesSource the source of the rules that need to be applied
     *  @param sharedStateResolver resolver to be notified with current state.
     *         State will not be set if this is null.
     */
    private fun applyConfigurationChanges(
        triggerEvent: Event?,
        rulesSource: RulesSource,
        sharedStateResolver: SharedStateResolver?
    ) {
        val config = configurationStateManager.environmentAwareConfiguration

        sharedStateResolver?.resolve(config)

        dispatchConfigurationResponse(config, triggerEvent)

        val rulesReplaced = replaceRules(config, rulesSource)
        if (rulesSource == RulesSource.CACHE && !rulesReplaced) {
            configurationRulesManager.applyBundledRules(api)
        }
    }

    /**
     * Dispatches a configuration response event in response to to [triggerEvent]
     *
     * @param eventData the content of the event data for the response event
     * @param triggerEvent the [Event] to which the response is being dispatched
     */
    private fun dispatchConfigurationResponse(eventData: Map<String, Any?>, triggerEvent: Event?) {
        val builder = Event.Builder(
            "Configuration Response Event",
            EventType.CONFIGURATION, EventSource.RESPONSE_CONTENT
        ).setEventData(eventData)

        val event: Event = if (triggerEvent == null) {
            builder.build()
        } else {
            builder.inResponseToEvent(triggerEvent).build()
        }

        api.dispatch(event)
    }

    private fun dispatchConfigurationRequest(eventData: Map<String, Any?>) {
        val event = Event.Builder(
            "Configure with AppID Internal",
            EventType.CONFIGURATION, EventSource.REQUEST_CONTENT
        )
            .setEventData(eventData).build()
        api.dispatch(event)
    }

    /**
     * Publishes the state of [ConfigurationExtension] to [EventHub]
     *
     * @param state the configuration state that is to be published
     * @param event the event (if any) to which the state is to be published as a response.
     */
    private fun publishConfigurationState(state: Map<String, Any?>, event: Event?) {
        val successful = api.createSharedState(state, event)
        if (!successful) {
            MobileCore.log(
                LoggingMode.ERROR,
                TAG,
                "Failed to update configuration state."
            )
        }
    }

    /**
     * Replaces the existing rules in the rules engine.
     *
     * @param config the configuration from which the rules url is extracted
     * @param rulesSource the source of the rules that need to be applied
     */
    private fun replaceRules(config: Map<String, Any?>, rulesSource: RulesSource): Boolean {
        when (rulesSource) {
            RulesSource.CACHE -> {
                return configurationRulesManager.applyCachedRules(api)
            }

            RulesSource.BUNDLED -> {
                return configurationRulesManager.applyBundledRules(api)
            }

            RulesSource.REMOTE -> {
                val rulesURL: String? = config[RULES_CONFIG_URL] as? String
                return if (!rulesURL.isNullOrBlank()) {
                    configurationRulesManager.applyDownloadedRules(rulesURL, api)
                } else {
                    MobileCore.log(
                        LoggingMode.ERROR,
                        TAG,
                        "Cannot load rules form rules URL: $rulesURL}"
                    )
                    false
                }
            }
        }
    }

    private fun Set<*>.isAllString(): Boolean {
        this.forEach { if (it !is String) return false }
        return true
    }
}
