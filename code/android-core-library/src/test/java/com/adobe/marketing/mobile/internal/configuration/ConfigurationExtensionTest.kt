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

import com.adobe.marketing.mobile.CoreConstants.EventDataKeys.Configuration
import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.EventSource
import com.adobe.marketing.mobile.EventType
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.ExtensionHelper
import com.adobe.marketing.mobile.launch.rulesengine.LaunchRulesEvaluator
import com.adobe.marketing.mobile.services.CacheFileService
import com.adobe.marketing.mobile.services.ServiceProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyString
import org.mockito.Mockito.never
import org.mockito.Mockito.reset
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(MockitoJUnitRunner.Silent::class)
class ConfigurationExtensionTest {

    @Mock
    private lateinit var mockServiceProvider: ServiceProvider

    @Mock
    private lateinit var mockAppIdManager: AppIdManager

    @Mock
    private lateinit var mockCacheFileService: CacheFileService

    /**
     * Note that any values returned for [ConfigurationStateManager.environmentAwareConfiguration] in these tests
     * are dummy values meant for verification of event dispatch.
     */
    @Mock
    private lateinit var mockConfigStateManager: ConfigurationStateManager

    @Mock
    private lateinit var mockConfigurationRulesManager: ConfigurationRulesManager

    @Mock
    private lateinit var mockLaunchRulesEvaluator: LaunchRulesEvaluator

    @Mock
    private lateinit var mockExtensionApi: ExtensionApi

    @Mock
    private lateinit var mockExecutorService: ScheduledExecutorService

    companion object {
        private const val SAMPLE_SERVER = "downloaded_server"
        private const val SAMPLE_RSID = "downloaded_rsid"
        private const val ANALYTICS_SERVER_KEY = "Analytics.server"
        private const val ANALYTICS_RSID_KEY = "Analytics.rsids"
    }

    @Test
    fun `ConfigurationExtension - onRegistered when initial config exists`() {
        `when`(mockAppIdManager.loadAppId()).thenReturn("SampleAppID")
        val config = mutableMapOf<String, Any?>(
            ANALYTICS_RSID_KEY to SAMPLE_RSID,
            ANALYTICS_SERVER_KEY to SAMPLE_SERVER
        )
        `when`(mockConfigStateManager.loadInitialConfig()).thenReturn(config)
        `when`(mockConfigStateManager.environmentAwareConfiguration).thenReturn(config)

        val configurationExtension = ConfigurationExtension(
            mockExtensionApi,
            mockServiceProvider,
            mockAppIdManager,
            mockCacheFileService,
            mockLaunchRulesEvaluator,
            mockExecutorService,
            mockConfigStateManager,
            mockConfigurationRulesManager
        )

        ExtensionHelper.notifyRegistered(configurationExtension)

        verify(mockExtensionApi).registerEventListener(eq(EventType.CONFIGURATION), eq(EventSource.REQUEST_CONTENT), any())
        verify(mockConfigStateManager).loadInitialConfig()
        verify(mockConfigurationRulesManager).applyCachedRules(mockExtensionApi)
    }

    @Test
    fun `ConfigurationExtension - onRegistered when cached rules cannot be applied`() {
        `when`(mockAppIdManager.loadAppId()).thenReturn("SampleAppID")
        val config = mutableMapOf<String, Any?>(
            ANALYTICS_RSID_KEY to SAMPLE_RSID,
            ANALYTICS_SERVER_KEY to SAMPLE_SERVER
        )
        `when`(mockConfigurationRulesManager.applyCachedRules(mockExtensionApi)).thenReturn(false)
        `when`(mockConfigStateManager.loadInitialConfig()).thenReturn(config)
        `when`(mockConfigStateManager.environmentAwareConfiguration).thenReturn(config)

        val configurationExtension = ConfigurationExtension(
            mockExtensionApi,
            mockServiceProvider,
            mockAppIdManager,
            mockCacheFileService,
            mockLaunchRulesEvaluator,
            mockExecutorService,
            mockConfigStateManager,
            mockConfigurationRulesManager
        )

        ExtensionHelper.notifyRegistered(configurationExtension)

        verify(mockExtensionApi).registerEventListener(eq(EventType.CONFIGURATION), eq(EventSource.REQUEST_CONTENT), any())
        verify(mockConfigStateManager).loadInitialConfig()
        verify(mockConfigurationRulesManager).applyCachedRules(mockExtensionApi)
        verify(mockConfigurationRulesManager).applyBundledRules(mockExtensionApi)
    }

    @Test
    fun `ConfigurationExtension - onRegistered when initial config is empty`() {
        `when`(mockAppIdManager.loadAppId()).thenReturn("SampleAppID")
        val initialConfig = mutableMapOf<String, Any?>()
        `when`(mockConfigStateManager.loadInitialConfig()).thenReturn(initialConfig)
        `when`(mockConfigStateManager.environmentAwareConfiguration).thenReturn(initialConfig)

        val configurationExtension = ConfigurationExtension(
            mockExtensionApi,
            mockServiceProvider,
            mockAppIdManager,
            mockCacheFileService,
            mockLaunchRulesEvaluator,
            mockExecutorService,
            mockConfigStateManager,
            mockConfigurationRulesManager
        )

        ExtensionHelper.notifyRegistered(configurationExtension)

        verify(mockExtensionApi, never()).createSharedState(initialConfig, null)
        verify(mockConfigurationRulesManager, never()).applyCachedRules(mockExtensionApi)
        verifyEventDispatch(
            mapOf(
                Configuration.CONFIGURATION_REQUEST_CONTENT_JSON_APP_ID to "SampleAppID",
                "config.isinternalevent" to true
            ),
            null,
            1
        )

        verify(mockExtensionApi, times(1)).registerEventListener(anyString(), anyString(), any())
    }

    @Test
    fun `Handle Configuration Request Event - event missing valid key`() {
        val configurationExtension = ConfigurationExtension(
            mockExtensionApi,
            mockServiceProvider,
            mockAppIdManager,
            mockCacheFileService,
            mockLaunchRulesEvaluator,
            mockExecutorService,
            mockConfigStateManager,
            mockConfigurationRulesManager
        )

        val event: Event = Event.Builder("Configure With Event", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT).build()

        configurationExtension.handleConfigurationRequestEvent(event)

        verify(mockConfigStateManager, never()).replaceConfiguration(any())
        verify(mockExtensionApi, never()).createSharedState(any(), eq(event))
        verify(mockConfigurationRulesManager, never()).applyDownloadedRules("rules.url", mockExtensionApi)
        verifyNoEventDispatch()
    }

    @Test
    fun `Configure with AppId - Null App ID from event`() {
        val config = mutableMapOf<String, Any?>(
            ANALYTICS_RSID_KEY to SAMPLE_RSID,
            ANALYTICS_SERVER_KEY to SAMPLE_SERVER,
            ConfigurationExtension.RULES_CONFIG_URL to "rules.url"
        )
        val existingEnvAwareConfig = mutableMapOf<String, Any?>("SampleKey" to "SampleValue")

        `when`(mockConfigStateManager.getConfigFromFile(anyString())).thenReturn(config)
        `when`(mockConfigStateManager.environmentAwareConfiguration).thenReturn(
            existingEnvAwareConfig
        )

        val configurationExtension = ConfigurationExtension(
            mockExtensionApi,
            mockServiceProvider,
            mockAppIdManager,
            mockCacheFileService,
            mockLaunchRulesEvaluator,
            mockExecutorService,
            mockConfigStateManager,
            mockConfigurationRulesManager
        )
        reset(mockExtensionApi)

        val event: Event = Event.Builder(
            "Configure with appId",
            EventType.CONFIGURATION, EventSource.REQUEST_CONTENT
        )
            .setEventData(mapOf(Configuration.CONFIGURATION_REQUEST_CONTENT_JSON_APP_ID to null))
            .build()

        configurationExtension.handleConfigurationRequestEvent(event)

        verify(mockAppIdManager).removeAppIDFromPersistence()
        verify(mockExtensionApi).createSharedState(existingEnvAwareConfig, event)
        verify(mockExtensionApi, never()).stopEvents()
        verify(mockExtensionApi, never()).startEvents()
        verifyNoEventDispatch()
    }

    @Test
    fun `Configure with AppId - Empty App ID from event`() {
        `when`(mockAppIdManager.loadAppId()).thenReturn("SampleAppID")
        val config = mutableMapOf<String, Any?>(
            ANALYTICS_RSID_KEY to SAMPLE_RSID,
            ANALYTICS_SERVER_KEY to SAMPLE_SERVER,
            ConfigurationExtension.RULES_CONFIG_URL to "rules.url"
        )
        val existingEnvAwareConfig = mutableMapOf<String, Any?>("SampleKey" to "SampleValue")

        `when`(mockConfigStateManager.getConfigFromFile(anyString())).thenReturn(config)
        `when`(mockConfigStateManager.environmentAwareConfiguration).thenReturn(
            existingEnvAwareConfig
        )
        `when`(mockConfigStateManager.hasConfigExpired(anyString())).thenReturn(false)

        val configurationExtension = ConfigurationExtension(
            mockExtensionApi, mockServiceProvider,
            mockAppIdManager, mockCacheFileService, mockLaunchRulesEvaluator, mockExecutorService,
            mockConfigStateManager, mockConfigurationRulesManager
        )

        val event: Event = Event.Builder(
            "Configure with appId",
            EventType.CONFIGURATION, EventSource.REQUEST_CONTENT
        )
            .setEventData(mapOf(Configuration.CONFIGURATION_REQUEST_CONTENT_JSON_APP_ID to ""))
            .build()

        configurationExtension.handleConfigurationRequestEvent(event)

        verify(mockAppIdManager).removeAppIDFromPersistence()
        verify(mockExtensionApi).createSharedState(existingEnvAwareConfig, event)
        verify(mockExtensionApi, never()).stopEvents()
        verify(mockExtensionApi, never()).startEvents()
        verifyNoEventDispatch()
    }

    @Test
    fun `Configure with AppId - Config has expired`() {
        val newAppID = "UpdatedAppID"
        `when`(mockAppIdManager.loadAppId()).thenReturn("SampleAppID")
        val config = mutableMapOf<String, Any?>(
            ANALYTICS_RSID_KEY to SAMPLE_RSID,
            ANALYTICS_SERVER_KEY to SAMPLE_SERVER,
            ConfigurationExtension.RULES_CONFIG_URL to "rules.url"
        )

        `when`(mockConfigStateManager.hasConfigExpired(anyString())).thenReturn(true)
        `when`(mockConfigStateManager.environmentAwareConfiguration).thenReturn(config)

        val configurationExtension = ConfigurationExtension(
            mockExtensionApi, mockServiceProvider,
            mockAppIdManager, mockCacheFileService, mockLaunchRulesEvaluator, mockExecutorService,
            mockConfigStateManager, mockConfigurationRulesManager
        )

        val event: Event = Event.Builder(
            "Configure with appId",
            EventType.CONFIGURATION, EventSource.REQUEST_CONTENT
        )
            .setEventData(mapOf(Configuration.CONFIGURATION_REQUEST_CONTENT_JSON_APP_ID to newAppID))
            .build()

        configurationExtension.handleConfigurationRequestEvent(event)

        val appIdCaptor: KArgumentCaptor<String> = argumentCaptor()
        val completionCallbackCaptor: KArgumentCaptor<(Map<String, Any?>?) -> Unit> =
            argumentCaptor()
        verify(mockExtensionApi, times(1)).stopEvents()
        verify(mockConfigStateManager).updateConfigWithAppId(
            appIdCaptor.capture(),
            completionCallbackCaptor.capture()
        )
        assertEquals(newAppID, appIdCaptor.firstValue)
        assertNotNull(completionCallbackCaptor.firstValue)

        // Simulate invoking of callback
        completionCallbackCaptor.firstValue.invoke(config)

        verify(mockExtensionApi).createSharedState(config, event)
        verify(mockConfigurationRulesManager).applyDownloadedRules("rules.url", mockExtensionApi)
        verify(mockExtensionApi, times(1)).startEvents()
        verifyEventDispatch(config, event, 1)
    }

    @Test
    fun `Configure with AppId - Config failed to download`() {
        val newAppID = "UpdatedAppID"
        `when`(mockAppIdManager.loadAppId()).thenReturn("SampleAppID")
        val config = mutableMapOf<String, Any?>(
            ANALYTICS_RSID_KEY to SAMPLE_RSID,
            ANALYTICS_SERVER_KEY to SAMPLE_SERVER,
            ConfigurationExtension.RULES_CONFIG_URL to "rules.url"
        )

        `when`(mockConfigStateManager.hasConfigExpired(anyString())).thenReturn(true)

        val configurationExtension = ConfigurationExtension(
            mockExtensionApi,
            mockServiceProvider,
            mockAppIdManager,
            mockCacheFileService,
            mockLaunchRulesEvaluator,
            mockExecutorService,
            mockConfigStateManager,
            mockConfigurationRulesManager
        )

        val event: Event = Event.Builder(
            "Configure with appId",
            EventType.CONFIGURATION, EventSource.REQUEST_CONTENT
        )
            .setEventData(mapOf(Configuration.CONFIGURATION_REQUEST_CONTENT_JSON_APP_ID to newAppID))
            .build()

        configurationExtension.handleConfigurationRequestEvent(event)

        val appIdCaptor: KArgumentCaptor<String> = argumentCaptor()
        val completionCallbackCaptor: KArgumentCaptor<(Map<String, Any?>?) -> Unit> =
            argumentCaptor()
        verify(mockConfigStateManager).updateConfigWithAppId(
            appIdCaptor.capture(),
            completionCallbackCaptor.capture()
        )
        assertEquals(newAppID, appIdCaptor.firstValue)
        assertNotNull(completionCallbackCaptor.firstValue)

        // Simulate triggering of callback
        completionCallbackCaptor.firstValue.invoke(null)

        verify(mockExtensionApi, times(1)).stopEvents()
        verify(mockExecutorService).schedule(
            Mockito.any(Runnable::class.java),
            eq(5L),
            eq(TimeUnit.SECONDS)
        )
        verify(mockExtensionApi, never()).createSharedState(config, event)
        verify(mockConfigurationRulesManager, never()).applyDownloadedRules("rules.url", mockExtensionApi)
        // Verify that events are not yet accepted
        verify(mockExtensionApi, never()).startEvents()
        verifyNoEventDispatch()
    }

    @Test
    fun `Configure with valid file path`() {
        val config = mutableMapOf<String, Any?>(
            ANALYTICS_RSID_KEY to SAMPLE_RSID,
            ANALYTICS_SERVER_KEY to SAMPLE_SERVER,
            ConfigurationExtension.RULES_CONFIG_URL to "rules.url"
        )

        `when`(mockConfigStateManager.getConfigFromFile(anyString())).thenReturn(config)
        `when`(mockConfigStateManager.environmentAwareConfiguration).thenReturn(config)
        reset(mockExtensionApi)

        val configurationExtension = ConfigurationExtension(
            mockExtensionApi,
            mockServiceProvider,
            mockAppIdManager,
            mockCacheFileService,
            mockLaunchRulesEvaluator,
            mockExecutorService,
            mockConfigStateManager,
            mockConfigurationRulesManager
        )

        val event: Event = Event.Builder("Configure with file path", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
            .setEventData(mapOf(Configuration.CONFIGURATION_REQUEST_CONTENT_JSON_FILE_PATH to "some/file/path"))
            .build()

        configurationExtension.handleConfigurationRequestEvent(event)

        verify(mockConfigStateManager).replaceConfiguration(config)
        verify(mockExtensionApi).createSharedState(config, event)
        verify(mockConfigurationRulesManager).applyDownloadedRules("rules.url", mockExtensionApi)
        verifyEventDispatch(config, event, 1)
    }

    @Test
    fun `Configure with file path - file has no content`() {
        `when`(mockConfigStateManager.getConfigFromFile(anyString())).thenReturn(null)

        val configurationExtension = ConfigurationExtension(
            mockExtensionApi,
            mockServiceProvider,
            mockAppIdManager,
            mockCacheFileService,
            mockLaunchRulesEvaluator,
            mockExecutorService,
            mockConfigStateManager,
            mockConfigurationRulesManager
        )

        val event: Event = Event.Builder("Configure with file path", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
            .setEventData(mapOf(Configuration.CONFIGURATION_REQUEST_CONTENT_JSON_FILE_PATH to "some/file/path"))
            .build()

        configurationExtension.handleConfigurationRequestEvent(event)

        verify(mockConfigStateManager, never()).replaceConfiguration(any())
        verify(mockExtensionApi, never()).createSharedState(any(), eq(event))
        verify(mockConfigurationRulesManager, never()).applyDownloadedRules("rules.url", mockExtensionApi)
        verifyNoEventDispatch()
    }

    @Test
    fun `Configure with file path - Null file path`() {

        `when`(mockConfigStateManager.getConfigFromFile(anyString())).thenReturn(null)

        val configurationExtension = ConfigurationExtension(
            mockExtensionApi,
            mockServiceProvider,
            mockAppIdManager,
            mockCacheFileService,
            mockLaunchRulesEvaluator,
            mockExecutorService,
            mockConfigStateManager,
            mockConfigurationRulesManager
        )

        val event: Event = Event.Builder("Configure with file path", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
            .setEventData(mapOf(Configuration.CONFIGURATION_REQUEST_CONTENT_JSON_FILE_PATH to null))
            .build()

        configurationExtension.handleConfigurationRequestEvent(event)

        verify(mockConfigStateManager, never()).replaceConfiguration(any())
        verify(mockExtensionApi, never()).createSharedState(any(), eq(event))
        verify(mockConfigurationRulesManager, never()).applyDownloadedRules("rules.url", mockExtensionApi)
        verifyNoEventDispatch()
    }

    @Test
    fun `Configure with file path - Empty file path`() {

        `when`(mockConfigStateManager.getConfigFromFile(anyString())).thenReturn(null)

        val configurationExtension = ConfigurationExtension(
            mockExtensionApi,
            mockServiceProvider,
            mockAppIdManager,
            mockCacheFileService,
            mockLaunchRulesEvaluator,
            mockExecutorService,
            mockConfigStateManager,
            mockConfigurationRulesManager
        )

        val event: Event = Event.Builder("Configure with file path", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
            .setEventData(mapOf(Configuration.CONFIGURATION_REQUEST_CONTENT_JSON_FILE_PATH to ""))
            .build()

        configurationExtension.handleConfigurationRequestEvent(event)

        verify(mockConfigStateManager, never()).replaceConfiguration(any())
        verify(mockExtensionApi, never()).createSharedState(any(), eq(event))
        verify(mockConfigurationRulesManager, never()).applyDownloadedRules("rules.url", mockExtensionApi)
        verifyNoEventDispatch()
    }

    @Test
    fun `Configure with file asset - null asset`() {
        `when`(mockConfigStateManager.loadBundledConfig(anyString())).thenReturn(null)

        val configurationExtension = ConfigurationExtension(
            mockExtensionApi,
            mockServiceProvider,
            mockAppIdManager,
            mockCacheFileService,
            mockLaunchRulesEvaluator,
            mockExecutorService,
            mockConfigStateManager,
            mockConfigurationRulesManager
        )

        val event: Event = Event.Builder("Configure with file asset", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
            .setEventData(mapOf(Configuration.CONFIGURATION_REQUEST_CONTENT_JSON_ASSET_FILE to null))
            .build()

        configurationExtension.handleConfigurationRequestEvent(event)

        verify(mockConfigStateManager, never()).replaceConfiguration(any())
        verify(mockExtensionApi, never()).createSharedState(any(), eq(event))
        verifyNoEventDispatch()
        verify(
            mockConfigurationRulesManager,
            never()
        ).applyDownloadedRules(ConfigurationExtension.RULES_CONFIG_URL, mockExtensionApi)
    }

    @Test
    fun `Configure with file asset - null content from asset`() {
        `when`(mockConfigStateManager.loadBundledConfig(anyString())).thenReturn(null)

        val configurationExtension = ConfigurationExtension(
            mockExtensionApi,
            mockServiceProvider,
            mockAppIdManager,
            mockCacheFileService,
            mockLaunchRulesEvaluator,
            mockExecutorService,
            mockConfigStateManager,
            mockConfigurationRulesManager
        )

        val event: Event = Event.Builder("Configure with file asset", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
            .setEventData(mapOf(Configuration.CONFIGURATION_REQUEST_CONTENT_JSON_ASSET_FILE to "/some/asset"))
            .build()

        configurationExtension.handleConfigurationRequestEvent(event)

        verify(mockConfigStateManager, never()).replaceConfiguration(any())
        verify(mockExtensionApi, never()).createSharedState(any(), eq(event))
        verifyNoEventDispatch()
        verify(
            mockConfigurationRulesManager,
            never()
        ).applyDownloadedRules(ConfigurationExtension.RULES_CONFIG_URL, mockExtensionApi)
    }

    @Test
    fun `Configure with file asset - valid asset`() {
        val mockBundledConfig = mutableMapOf<String, Any?>(
            ANALYTICS_RSID_KEY to SAMPLE_RSID,
            ANALYTICS_SERVER_KEY to SAMPLE_SERVER,
            ConfigurationExtension.RULES_CONFIG_URL to "rules.url"
        )

        `when`(mockConfigStateManager.loadBundledConfig(anyString())).thenReturn(mockBundledConfig)
        `when`(mockConfigStateManager.environmentAwareConfiguration).thenReturn(mockBundledConfig)

        val configurationExtension = ConfigurationExtension(
            mockExtensionApi,
            mockServiceProvider,
            mockAppIdManager,
            mockCacheFileService,
            mockLaunchRulesEvaluator,
            mockExecutorService,
            mockConfigStateManager,
            mockConfigurationRulesManager
        )

        val event: Event = Event.Builder("Configure with file asset", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
            .setEventData(mapOf(Configuration.CONFIGURATION_REQUEST_CONTENT_JSON_ASSET_FILE to "some/file/path"))
            .build()

        configurationExtension.handleConfigurationRequestEvent(event)

        verifyEventDispatch(mockBundledConfig, event, 1)
        verify(mockConfigStateManager).replaceConfiguration(mockBundledConfig)
        verify(mockExtensionApi).createSharedState(mockBundledConfig, event)
        verify(mockConfigurationRulesManager).applyDownloadedRules("rules.url", mockExtensionApi)
    }

    @Test
    fun `Update Configuration - valid programmatic config`() {
        val mockBundledConfig = mutableMapOf<String, Any?>(
            ANALYTICS_RSID_KEY to SAMPLE_RSID,
            ANALYTICS_SERVER_KEY to SAMPLE_SERVER,
            ConfigurationExtension.RULES_CONFIG_URL to "rules.url"
        )

        val programmaticConfig = mapOf<String, Any?>(
            ANALYTICS_RSID_KEY to "UpdatedAnalyticsRSID",
            ANALYTICS_SERVER_KEY to "UpdatedAnalyticsRSID"
        )

        `when`(mockAppIdManager.loadAppId()).thenReturn("SampleAppID")
        `when`(mockConfigStateManager.loadBundledConfig(anyString())).thenReturn(mockBundledConfig)
        `when`(mockConfigStateManager.environmentAwareConfiguration).thenReturn(mockBundledConfig)

        val configurationExtension = ConfigurationExtension(
            mockExtensionApi,
            mockServiceProvider,
            mockAppIdManager,
            mockCacheFileService,
            mockLaunchRulesEvaluator,
            mockExecutorService,
            mockConfigStateManager,
            mockConfigurationRulesManager
        )
        reset(mockExtensionApi)

        val event: Event = Event.Builder("Update programmatic config", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
            .setEventData(mapOf(Configuration.CONFIGURATION_REQUEST_CONTENT_UPDATE_CONFIG to programmaticConfig))
            .build()

        configurationExtension.handleConfigurationRequestEvent(event)
        val updateConfigCaptor: KArgumentCaptor<Map<String, Any?>> = argumentCaptor()
        verify(mockConfigStateManager).updateProgrammaticConfig(updateConfigCaptor.capture())

        assertEquals(programmaticConfig, updateConfigCaptor.firstValue)
    }

    @Test
    fun `Clear updated configuration`() {
        val mockUpdatedConfig = mutableMapOf<String, Any?>(
            ANALYTICS_RSID_KEY to SAMPLE_RSID,
            ANALYTICS_SERVER_KEY to SAMPLE_SERVER,
            ConfigurationExtension.RULES_CONFIG_URL to "updated.rules.url"
        )

        `when`(mockAppIdManager.loadAppId()).thenReturn("SampleAppID")
        `when`(mockConfigStateManager.environmentAwareConfiguration).thenReturn(mockUpdatedConfig)

        val configurationExtension = ConfigurationExtension(
            mockExtensionApi,
            mockServiceProvider,
            mockAppIdManager,
            mockCacheFileService,
            mockLaunchRulesEvaluator,
            mockExecutorService,
            mockConfigStateManager,
            mockConfigurationRulesManager
        )
        reset(mockExtensionApi)

        val event: Event = Event.Builder("Clear updated configuration", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
            .setEventData(mapOf(Configuration.CONFIGURATION_REQUEST_CONTENT_CLEAR_UPDATED_CONFIG to true))
            .build()

        configurationExtension.handleConfigurationRequestEvent(event)

        verify(mockExtensionApi).createSharedState(eq(mockUpdatedConfig), eq(event))
        verify(mockConfigurationRulesManager).applyDownloadedRules("updated.rules.url", mockExtensionApi)
        verifyEventDispatch(mockUpdatedConfig, event, 1)
    }

    private fun verifyEventDispatch(
        expectedEventData: Map<String, Any?>?,
        triggerEvent: Event?,
        times: Int
    ) {
        if (times == 0) {
            verifyNoEventDispatch()
        }

        val eventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        verify(mockExtensionApi, times(times)).dispatch(eventCaptor.capture())
        val capturedEvent = eventCaptor.firstValue
        assertEquals(expectedEventData, capturedEvent.eventData)
        if (triggerEvent != null) {
            assertNotNull(triggerEvent)
            assertEquals(triggerEvent.uniqueIdentifier, capturedEvent.responseID)
        }
    }

    private fun verifyNoEventDispatch() {
        verify(mockExtensionApi, times(0)).dispatch(any())
    }
}
