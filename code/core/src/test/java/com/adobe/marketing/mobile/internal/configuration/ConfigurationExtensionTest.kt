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

import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.EventSource
import com.adobe.marketing.mobile.EventType
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.ExtensionHelper
import com.adobe.marketing.mobile.SharedStateResolver
import com.adobe.marketing.mobile.internal.configuration.ConfigurationExtension.Companion.CONFIGURATION_REQUEST_CONTENT_CLEAR_UPDATED_CONFIG
import com.adobe.marketing.mobile.internal.configuration.ConfigurationExtension.Companion.CONFIGURATION_REQUEST_CONTENT_JSON_APP_ID
import com.adobe.marketing.mobile.internal.configuration.ConfigurationExtension.Companion.CONFIGURATION_REQUEST_CONTENT_JSON_ASSET_FILE
import com.adobe.marketing.mobile.internal.configuration.ConfigurationExtension.Companion.CONFIGURATION_REQUEST_CONTENT_JSON_FILE_PATH
import com.adobe.marketing.mobile.internal.configuration.ConfigurationExtension.Companion.CONFIGURATION_REQUEST_CONTENT_RETRIEVE_CONFIG
import com.adobe.marketing.mobile.internal.configuration.ConfigurationExtension.Companion.CONFIGURATION_REQUEST_CONTENT_UPDATE_CONFIG
import com.adobe.marketing.mobile.internal.eventhub.EventHub
import com.adobe.marketing.mobile.internal.eventhub.EventPreprocessor
import com.adobe.marketing.mobile.launch.rulesengine.LaunchRulesEvaluator
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.caching.CacheService
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.reset
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner.Silent::class)
class ConfigurationExtensionTest {

    @Mock
    private lateinit var mockServiceProvider: ServiceProvider

    @Mock
    private lateinit var mockAppIdManager: AppIdManager

    @Mock
    private lateinit var mockCacheService: CacheService

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

    @Mock
    private lateinit var mockSharedStateResolver: SharedStateResolver

    private lateinit var mockedStaticServiceProvider: MockedStatic<ServiceProvider>

    companion object {
        private const val SAMPLE_SERVER = "downloaded_server"
        private const val SAMPLE_RSID = "downloaded_rsid"
        private const val ANALYTICS_SERVER_KEY = "Analytics.server"
        private const val ANALYTICS_RSID_KEY = "Analytics.rsids"
    }

    @Before
    fun setup() {
        mockedStaticServiceProvider = Mockito.mockStatic(ServiceProvider::class.java)
        mockedStaticServiceProvider.`when`<Any> { ServiceProvider.getInstance() }.thenReturn(mockServiceProvider)
        `when`(mockServiceProvider.cacheService).thenReturn(mockCacheService)
    }

    @Test
    fun `ConfigurationExtension - constructor when initial config exists`() {
        val mockEventHub = mock(EventHub::class.java)
        EventHub.shared = mockEventHub

        `when`(mockAppIdManager.loadAppId()).thenReturn("SampleAppID")
        val config = mutableMapOf<String, Any?>(
            ANALYTICS_RSID_KEY to SAMPLE_RSID,
            ANALYTICS_SERVER_KEY to SAMPLE_SERVER
        )
        `when`(mockConfigStateManager.loadInitialConfig()).thenReturn(config)
        `when`(mockConfigStateManager.environmentAwareConfiguration).thenReturn(config)

        val configurationExtension = ConfigurationExtension(
            mockExtensionApi,
            mockAppIdManager,
            mockLaunchRulesEvaluator,
            mockExecutorService,
            mockConfigStateManager,
            mockConfigurationRulesManager
        )

        verify(mockAppIdManager).loadAppId()
        verify(mockConfigStateManager).loadInitialConfig()
        verify(mockConfigurationRulesManager).applyCachedRules(mockExtensionApi)
        verify(mockExtensionApi, never()).createSharedState(any(), any())

        val eventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        verify(mockExtensionApi, times(2)).dispatch(eventCaptor.capture())
        verifyDispatchedEvent(
            eventCaptor.firstValue,
            EventType.CONFIGURATION,
            EventSource.REQUEST_CONTENT,
            mapOf(
                CONFIGURATION_REQUEST_CONTENT_JSON_APP_ID to "SampleAppID",
                ConfigurationExtension.CONFIGURATION_REQUEST_CONTENT_IS_INTERNAL_EVENT to true
            ),
            null
        )
        verifyDispatchedEvent(
            eventCaptor.secondValue,
            EventType.CONFIGURATION,
            EventSource.RESPONSE_CONTENT,
            config,
            null
        )

        // Verify that launch rule evaluator is registered and configured correctly
        val preprocessorCaptor: KArgumentCaptor<EventPreprocessor> = argumentCaptor()
        verify(mockEventHub).registerEventPreprocessor(preprocessorCaptor.capture())
        val mockEvent = Event.Builder("Verify preprocessor event", "name", "source").build()
        preprocessorCaptor.firstValue.process(mockEvent)
        verify(mockLaunchRulesEvaluator).process(mockEvent)
    }

    @Test
    fun `ConfigurationExtension - constructor when cached rules cannot be applied`() {
        val mockEventHub = mock(EventHub::class.java)
        EventHub.shared = mockEventHub

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
            mockAppIdManager,
            mockLaunchRulesEvaluator,
            mockExecutorService,
            mockConfigStateManager,
            mockConfigurationRulesManager
        )

        verify(mockAppIdManager).loadAppId()
        verify(mockConfigStateManager).loadInitialConfig()
        verify(mockExtensionApi, never()).createSharedState(any(), any())
        verify(mockConfigurationRulesManager).applyCachedRules(mockExtensionApi)
        verify(mockConfigurationRulesManager).applyBundledRules(mockExtensionApi)

        // Verify that launch rule evaluator is registered and configured correctly
        val preprocessorCaptor: KArgumentCaptor<EventPreprocessor> = argumentCaptor()
        verify(mockEventHub).registerEventPreprocessor(preprocessorCaptor.capture())
        val mockEvent = Event.Builder("Verify preprocessor event", "name", "source").build()
        preprocessorCaptor.firstValue.process(mockEvent)
        verify(mockLaunchRulesEvaluator).process(mockEvent)
    }

    @Test
    fun `ConfigurationExtension - constructor when initial config is empty`() {
        `when`(mockAppIdManager.loadAppId()).thenReturn("SampleAppID")
        val initialConfig = mutableMapOf<String, Any?>()
        `when`(mockConfigStateManager.loadInitialConfig()).thenReturn(initialConfig)
        `when`(mockConfigStateManager.environmentAwareConfiguration).thenReturn(initialConfig)

        val configurationExtension = ConfigurationExtension(
            mockExtensionApi,
            mockAppIdManager,
            mockLaunchRulesEvaluator,
            mockExecutorService,
            mockConfigStateManager,
            mockConfigurationRulesManager
        )

        verify(mockExtensionApi, never()).createSharedState(initialConfig, null)
        verify(mockConfigurationRulesManager, never()).applyCachedRules(mockExtensionApi)

        val eventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        verify(mockExtensionApi, times(1)).dispatch(eventCaptor.capture())

        verifyDispatchedEvent(
            eventCaptor.firstValue,
            EventType.CONFIGURATION,
            EventSource.REQUEST_CONTENT,
            mapOf(
                CONFIGURATION_REQUEST_CONTENT_JSON_APP_ID to "SampleAppID",
                "config.isinternalevent" to true
            ),
            null
        )
    }

    @Test
    fun `ConfigurationExtension - registers listener onRegistered`() {
        val configurationExtension = ConfigurationExtension(
            mockExtensionApi,
            mockAppIdManager,
            mockLaunchRulesEvaluator,
            mockExecutorService,
            mockConfigStateManager,
            mockConfigurationRulesManager
        )

        ExtensionHelper.notifyRegistered(configurationExtension)

        verify(mockExtensionApi).registerEventListener(
            eq(EventType.CONFIGURATION),
            eq(EventSource.REQUEST_CONTENT),
            any()
        )

        verify(mockExtensionApi).registerEventListener(
            eq(EventType.CONFIGURATION),
            eq(EventSource.REQUEST_IDENTITY),
            any()
        )
    }

    @Test
    fun `Handle Configuration Request Event - event missing valid key`() {
        val configurationExtension = ConfigurationExtension(
            mockExtensionApi,
            mockAppIdManager,
            mockLaunchRulesEvaluator,
            mockExecutorService,
            mockConfigStateManager,
            mockConfigurationRulesManager
        )

        val event: Event = Event.Builder(
            "Configure With Event",
            EventType.CONFIGURATION,
            EventSource.REQUEST_CONTENT
        ).build()
        `when`(mockExtensionApi.createPendingSharedState(event)).thenReturn(mockSharedStateResolver)

        configurationExtension.handleConfigurationRequestEvent(event)

        verify(mockConfigStateManager, never()).replaceConfiguration(any())
        verify(mockExtensionApi, never()).createSharedState(any(), eq(event))
        verify(mockConfigurationRulesManager, never()).applyDownloadedRules(
            "rules.url",
            mockExtensionApi
        )
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
            mockAppIdManager,
            mockLaunchRulesEvaluator,
            mockExecutorService,
            mockConfigStateManager,
            mockConfigurationRulesManager
        )
        reset(mockExtensionApi)

        val event: Event = Event.Builder(
            "Configure with appId",
            EventType.CONFIGURATION,
            EventSource.REQUEST_CONTENT
        )
            .setEventData(mapOf(CONFIGURATION_REQUEST_CONTENT_JSON_APP_ID to null))
            .build()
        `when`(mockExtensionApi.createPendingSharedState(event)).thenReturn(mockSharedStateResolver)

        configurationExtension.handleConfigurationRequestEvent(event)

        verify(mockAppIdManager).removeAppIdFromPersistence()
        verify(mockSharedStateResolver).resolve(existingEnvAwareConfig)
        verify(mockExtensionApi, never()).stopEvents()
        verify(mockExtensionApi, never()).startEvents()
        verifyNoEventDispatch()
    }

    @Test
    fun `Configure with AppId - Empty App ID from event`() {
        // `when`(mockAppIdManager.loadAppId()).thenReturn("SampleAppID")
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
            mockExtensionApi,
            mockAppIdManager,
            mockLaunchRulesEvaluator,
            mockExecutorService,
            mockConfigStateManager,
            mockConfigurationRulesManager
        )

        val event: Event = Event.Builder(
            "Configure with appId",
            EventType.CONFIGURATION,
            EventSource.REQUEST_CONTENT
        )
            .setEventData(mapOf(CONFIGURATION_REQUEST_CONTENT_JSON_APP_ID to ""))
            .build()
        `when`(mockExtensionApi.createPendingSharedState(event)).thenReturn(mockSharedStateResolver)

        configurationExtension.handleConfigurationRequestEvent(event)

        verify(mockAppIdManager).removeAppIdFromPersistence()
        verify(mockSharedStateResolver).resolve(existingEnvAwareConfig)
        verify(mockExtensionApi, never()).stopEvents()
        verify(mockExtensionApi, never()).startEvents()
        verifyNoEventDispatch()
    }

    @Test
    fun `Configure with AppId - Config has expired`() {
        val newAppID = "UpdatedAppID"

        `when`(mockAppIdManager.loadAppId()).thenReturn("SampleAppID")
        val config = mapOf<String, String?>(
            ANALYTICS_RSID_KEY to SAMPLE_RSID,
            ANALYTICS_SERVER_KEY to SAMPLE_SERVER,
            ConfigurationExtension.RULES_CONFIG_URL to "rules.url"
        )

        `when`(mockConfigStateManager.hasConfigExpired(anyString())).thenReturn(true)
        `when`(mockConfigStateManager.environmentAwareConfiguration).thenReturn(config)
        `when`(mockConfigStateManager.updateConfigWithAppId(any(), any())).then {
            // Simulate invoking of callback
            val completionCallback = it.getArgument<(Map<String, String?>) -> Unit>(1)
            completionCallback.invoke(config)
        }

        val configurationExtension = ConfigurationExtension(
            mockExtensionApi,
            mockAppIdManager,
            mockLaunchRulesEvaluator,
            mockExecutorService,
            mockConfigStateManager,
            mockConfigurationRulesManager
        )

        val event: Event = Event.Builder(
            "Configure with appId",
            EventType.CONFIGURATION,
            EventSource.REQUEST_CONTENT
        )
            .setEventData(mapOf(CONFIGURATION_REQUEST_CONTENT_JSON_APP_ID to newAppID))
            .build()
        `when`(mockExtensionApi.createPendingSharedState(event)).thenReturn(mockSharedStateResolver)

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

        verify(mockSharedStateResolver).resolve(config)
        verify(mockConfigurationRulesManager).applyDownloadedRules("rules.url", mockExtensionApi)
        verify(mockExtensionApi, times(1)).startEvents()

        val eventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        verify(mockExtensionApi, times(2)).dispatch(eventCaptor.capture())

        verifyDispatchedEvent(
            eventCaptor.firstValue,
            EventType.CONFIGURATION,
            EventSource.REQUEST_CONTENT,
            mapOf(
                CONFIGURATION_REQUEST_CONTENT_JSON_APP_ID to "SampleAppID",
                "config.isinternalevent" to true
            ),
            null
        )

        verifyDispatchedEvent(
            eventCaptor.secondValue,
            EventType.CONFIGURATION,
            EventSource.RESPONSE_CONTENT,
            config,
            null
        )
    }

    @Test
    fun `Configure with AppId - Config failed to download twice`() {
        `when`(mockAppIdManager.loadAppId()).thenReturn("SampleAppID")
        val currentConfig = mutableMapOf<String, Any?>(
            ANALYTICS_RSID_KEY to SAMPLE_RSID,
            ANALYTICS_SERVER_KEY to SAMPLE_SERVER,
            ConfigurationExtension.RULES_CONFIG_URL to "rules.url"
        )

        `when`(mockConfigStateManager.hasConfigExpired(anyString())).thenReturn(true)

        val newAppID = "UpdatedAppID"
        val newConfig = mapOf<String, Any?>(
            ANALYTICS_RSID_KEY to SAMPLE_RSID,
            ANALYTICS_SERVER_KEY to SAMPLE_SERVER,
            ConfigurationExtension.RULES_CONFIG_URL to "new.rules.url"
        )

        `when`(mockConfigStateManager.environmentAwareConfiguration)
            .thenReturn(currentConfig)
            .thenReturn(currentConfig)
            .thenReturn(newConfig)

        val mockFuture = mock(ScheduledFuture::class.java)
        `when`(mockExecutorService.schedule(any(), any(), any())).then {
            val runnable: Runnable = it.getArgument(0) as Runnable
            runnable.run()
            return@then mockFuture
        }

        // Simulate returning cached config for the first 2 times and then return the new config
        `when`(mockConfigStateManager.updateConfigWithAppId(any(), any())).then {
            val completionCallback = it.getArgument<(Map<String, String>?) -> Unit>(1)
            completionCallback.invoke(null)
        }.then {
            val completionCallback = it.getArgument<(Map<String, String>?) -> Unit>(1)
            completionCallback.invoke(null)
        }.then {
            val completionCallback = it.getArgument<(Map<String, Any?>?) -> Unit>(1)
            completionCallback.invoke(newConfig)
        }

        val configurationExtension = ConfigurationExtension(
            mockExtensionApi,
            mockAppIdManager,
            mockLaunchRulesEvaluator,
            mockExecutorService,
            mockConfigStateManager,
            mockConfigurationRulesManager
        )

        `when`(mockExtensionApi.dispatch(any())).then {
            val dispatchedEvent = it.getArgument<Event>(0)
            configurationExtension.handleConfigurationRequestEvent(dispatchedEvent)
        }

        val event: Event = Event.Builder(
            "Configure with appId",
            EventType.CONFIGURATION,
            EventSource.REQUEST_CONTENT
        )
            .setEventData(mapOf(CONFIGURATION_REQUEST_CONTENT_JSON_APP_ID to newAppID))
            .build()
        `when`(mockExtensionApi.createPendingSharedState(any())).thenReturn(mockSharedStateResolver)

        configurationExtension.handleConfigurationRequestEvent(event)

        val appIdCaptor: KArgumentCaptor<String> = argumentCaptor()
        val completionCallbackCaptor: KArgumentCaptor<(Map<String, Any?>?) -> Unit> =
            argumentCaptor()

        // Should invoke update on state manager 2 times for retry and 3rd time for success
        verify(mockConfigStateManager, times(3)).updateConfigWithAppId(
            appIdCaptor.capture(),
            completionCallbackCaptor.capture()
        )

        // Verify first retry scheduling
        verify(mockExecutorService).schedule(
            Mockito.any(Runnable::class.java),
            eq(5000L),
            eq(TimeUnit.MILLISECONDS)
        )

        // Verify second retry scheduling
        verify(mockExecutorService).schedule(
            Mockito.any(Runnable::class.java),
            eq(10000L),
            eq(TimeUnit.MILLISECONDS)
        )

        // Verify that cached state is set twice for 2 failed downloads
        verify(mockSharedStateResolver, times(2)).resolve(eq(currentConfig))

        // Verify that new/downloaded state is set for final successful download
        verify(mockSharedStateResolver, times(1)).resolve(eq(newConfig))

        // verify that old rules are never re-applied
        verify(mockConfigurationRulesManager, times(0)).applyDownloadedRules(
            "rules.url",
            mockExtensionApi
        )

        // Should download new rules from config
        verify(mockConfigurationRulesManager, times(1)).applyDownloadedRules(
            "new.rules.url",
            mockExtensionApi
        )

        // Should start and resume for all 3 attempts
        verify(mockExtensionApi, times(3)).stopEvents()
        verify(mockExtensionApi, times(3)).startEvents()
    }

    @Test
    fun `Configure with valid file path`() {
        val filePath = "some/file/path"
        `when`(mockConfigStateManager.updateConfigWithFilePath(filePath)).thenReturn(true)

        val config = mutableMapOf<String, Any?>(
            ANALYTICS_RSID_KEY to SAMPLE_RSID,
            ANALYTICS_SERVER_KEY to SAMPLE_SERVER,
            ConfigurationExtension.RULES_CONFIG_URL to "rules.url"
        )

        `when`(mockConfigStateManager.getConfigFromFile(anyString())).thenReturn(config)
        `when`(mockConfigStateManager.environmentAwareConfiguration).thenReturn(config)

        val configurationExtension = ConfigurationExtension(
            mockExtensionApi,
            mockAppIdManager,
            mockLaunchRulesEvaluator,
            mockExecutorService,
            mockConfigStateManager,
            mockConfigurationRulesManager
        )

        val event: Event = Event.Builder(
            "Configure with file path",
            EventType.CONFIGURATION,
            EventSource.REQUEST_CONTENT
        )
            .setEventData(mapOf(CONFIGURATION_REQUEST_CONTENT_JSON_FILE_PATH to filePath))
            .build()
        `when`(mockExtensionApi.createPendingSharedState(event)).thenReturn(mockSharedStateResolver)

        configurationExtension.handleConfigurationRequestEvent(event)

        verify(mockConfigStateManager).updateConfigWithFilePath(filePath)
        verify(mockSharedStateResolver).resolve(config)
        verify(mockConfigurationRulesManager).applyDownloadedRules("rules.url", mockExtensionApi)

        val eventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        verify(mockExtensionApi, times(1)).dispatch(eventCaptor.capture())
        verifyDispatchedEvent(
            eventCaptor.firstValue,
            EventType.CONFIGURATION,
            EventSource.RESPONSE_CONTENT,
            config,
            null
        )
    }

    @Test
    fun `Configure with file path - file has no content`() {
        `when`(mockConfigStateManager.getConfigFromFile(anyString())).thenReturn(null)

        val configurationExtension = ConfigurationExtension(
            mockExtensionApi,
            mockAppIdManager,
            mockLaunchRulesEvaluator,
            mockExecutorService,
            mockConfigStateManager,
            mockConfigurationRulesManager
        )

        val event: Event = Event.Builder(
            "Configure with file path",
            EventType.CONFIGURATION,
            EventSource.REQUEST_CONTENT
        )
            .setEventData(mapOf(CONFIGURATION_REQUEST_CONTENT_JSON_FILE_PATH to "some/file/path"))
            .build()
        `when`(mockExtensionApi.createPendingSharedState(event)).thenReturn(mockSharedStateResolver)

        configurationExtension.handleConfigurationRequestEvent(event)

        verify(mockConfigStateManager, never()).replaceConfiguration(any())
        verify(mockExtensionApi, never()).createSharedState(any(), eq(event))
        verify(mockConfigurationRulesManager, never()).applyDownloadedRules(
            "rules.url",
            mockExtensionApi
        )
        verifyNoEventDispatch()
    }

    @Test
    fun `Configure with file path - Null file path`() {
        `when`(mockConfigStateManager.getConfigFromFile(anyString())).thenReturn(null)

        val configurationExtension = ConfigurationExtension(
            mockExtensionApi,
            mockAppIdManager,
            mockLaunchRulesEvaluator,
            mockExecutorService,
            mockConfigStateManager,
            mockConfigurationRulesManager
        )

        val event: Event = Event.Builder(
            "Configure with file path",
            EventType.CONFIGURATION,
            EventSource.REQUEST_CONTENT
        )
            .setEventData(mapOf(CONFIGURATION_REQUEST_CONTENT_JSON_FILE_PATH to null))
            .build()
        `when`(mockExtensionApi.createPendingSharedState(event)).thenReturn(mockSharedStateResolver)

        configurationExtension.handleConfigurationRequestEvent(event)

        verify(mockConfigStateManager, never()).replaceConfiguration(any())
        verify(mockExtensionApi, never()).createSharedState(any(), eq(event))
        verify(mockConfigurationRulesManager, never()).applyDownloadedRules(
            "rules.url",
            mockExtensionApi
        )
        verifyNoEventDispatch()
    }

    @Test
    fun `Configure with file path - Empty file path`() {
        `when`(mockConfigStateManager.getConfigFromFile(anyString())).thenReturn(null)

        val configurationExtension = ConfigurationExtension(
            mockExtensionApi,
            mockAppIdManager,
            mockLaunchRulesEvaluator,
            mockExecutorService,
            mockConfigStateManager,
            mockConfigurationRulesManager
        )

        val event: Event = Event.Builder(
            "Configure with file path",
            EventType.CONFIGURATION,
            EventSource.REQUEST_CONTENT
        )
            .setEventData(mapOf(CONFIGURATION_REQUEST_CONTENT_JSON_FILE_PATH to ""))
            .build()
        `when`(mockExtensionApi.createPendingSharedState(event)).thenReturn(mockSharedStateResolver)

        configurationExtension.handleConfigurationRequestEvent(event)

        verify(mockConfigStateManager, never()).replaceConfiguration(any())
        verify(mockExtensionApi, never()).createSharedState(any(), eq(event))
        verify(mockConfigurationRulesManager, never()).applyDownloadedRules(
            "rules.url",
            mockExtensionApi
        )
        verifyNoEventDispatch()
    }

    @Test
    fun `Configure with file asset - null asset`() {
        `when`(mockConfigStateManager.loadBundledConfig(anyString())).thenReturn(null)

        val configurationExtension = ConfigurationExtension(
            mockExtensionApi,
            mockAppIdManager,
            mockLaunchRulesEvaluator,
            mockExecutorService,
            mockConfigStateManager,
            mockConfigurationRulesManager
        )

        val event: Event = Event.Builder(
            "Configure with file asset",
            EventType.CONFIGURATION,
            EventSource.REQUEST_CONTENT
        )
            .setEventData(mapOf(CONFIGURATION_REQUEST_CONTENT_JSON_ASSET_FILE to null))
            .build()
        `when`(mockExtensionApi.createPendingSharedState(event)).thenReturn(mockSharedStateResolver)

        configurationExtension.handleConfigurationRequestEvent(event)

        verify(mockConfigStateManager, never()).updateConfigWithFileAsset(any())
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

        val fileAssetName = "/some/asset"
        val configurationExtension = ConfigurationExtension(
            mockExtensionApi,
            mockAppIdManager,
            mockLaunchRulesEvaluator,
            mockExecutorService,
            mockConfigStateManager,
            mockConfigurationRulesManager
        )

        val event: Event = Event.Builder(
            "Configure with file asset",
            EventType.CONFIGURATION,
            EventSource.REQUEST_CONTENT
        )
            .setEventData(mapOf(CONFIGURATION_REQUEST_CONTENT_JSON_ASSET_FILE to fileAssetName))
            .build()
        `when`(mockExtensionApi.createPendingSharedState(event)).thenReturn(mockSharedStateResolver)

        configurationExtension.handleConfigurationRequestEvent(event)

        verify(mockConfigStateManager).updateConfigWithFileAsset(fileAssetName)
        verify(mockSharedStateResolver).resolve(any())
        verifyNoEventDispatch()
        verify(
            mockConfigurationRulesManager,
            never()
        ).applyDownloadedRules(ConfigurationExtension.RULES_CONFIG_URL, mockExtensionApi)
    }

    @Test
    fun `Configure with file asset - valid asset`() {
        // Setup
        val mockBundledConfig = mutableMapOf<String, Any?>(
            ANALYTICS_RSID_KEY to SAMPLE_RSID,
            ANALYTICS_SERVER_KEY to SAMPLE_SERVER,
            ConfigurationExtension.RULES_CONFIG_URL to "rules.url"
        )
        val fileAsset = "some/file/path"

        `when`(mockConfigStateManager.loadBundledConfig(anyString())).thenReturn(mockBundledConfig)
        `when`(mockConfigStateManager.updateConfigWithFileAsset(fileAsset)).thenReturn(true)
        `when`(mockConfigStateManager.environmentAwareConfiguration).thenReturn(mockBundledConfig)

        val configurationExtension = ConfigurationExtension(
            mockExtensionApi,
            mockAppIdManager,
            mockLaunchRulesEvaluator,
            mockExecutorService,
            mockConfigStateManager,
            mockConfigurationRulesManager
        )

        val event: Event = Event.Builder(
            "Configure with file asset",
            EventType.CONFIGURATION,
            EventSource.REQUEST_CONTENT
        )
            .setEventData(mapOf(CONFIGURATION_REQUEST_CONTENT_JSON_ASSET_FILE to fileAsset))
            .build()
        `when`(mockExtensionApi.createPendingSharedState(event)).thenReturn(mockSharedStateResolver)

        // Test
        configurationExtension.handleConfigurationRequestEvent(event)

        // Verify
        verify(mockConfigStateManager).updateConfigWithFileAsset(fileAsset)
        verify(mockSharedStateResolver).resolve(any())
        verify(mockConfigurationRulesManager).applyDownloadedRules("rules.url", mockExtensionApi)

        val eventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        verify(mockExtensionApi, times(1)).dispatch(eventCaptor.capture())
        verifyDispatchedEvent(
            eventCaptor.firstValue,
            EventType.CONFIGURATION,
            EventSource.RESPONSE_CONTENT,
            mockBundledConfig,
            null
        )
    }

    @Test
    fun `Update Configuration - no programmatic config`() {
        val mockBundledConfig = mutableMapOf<String, Any?>(
            ANALYTICS_RSID_KEY to SAMPLE_RSID,
            ANALYTICS_SERVER_KEY to SAMPLE_SERVER,
            ConfigurationExtension.RULES_CONFIG_URL to "rules.url"
        )

        `when`(mockAppIdManager.loadAppId()).thenReturn("SampleAppID")
        `when`(mockConfigStateManager.loadBundledConfig(anyString())).thenReturn(mockBundledConfig)
        `when`(mockConfigStateManager.environmentAwareConfiguration).thenReturn(mockBundledConfig)

        val configurationExtension = ConfigurationExtension(
            mockExtensionApi,
            mockAppIdManager,
            mockLaunchRulesEvaluator,
            mockExecutorService,
            mockConfigStateManager,
            mockConfigurationRulesManager
        )
        reset(mockExtensionApi)

        val event: Event = Event.Builder(
            "Update invalid programmatic config",
            EventType.CONFIGURATION,
            EventSource.REQUEST_CONTENT
        )
            .setEventData(mapOf(CONFIGURATION_REQUEST_CONTENT_UPDATE_CONFIG to null))
            .build()
        `when`(mockExtensionApi.createPendingSharedState(event)).thenReturn(mockSharedStateResolver)

        configurationExtension.handleConfigurationRequestEvent(event)

        verify(mockConfigStateManager, never()).updateProgrammaticConfig(any())
        verify(mockSharedStateResolver).resolve(mockBundledConfig)
    }

    @Test
    fun `Update Configuration - invalid programmatic config`() {
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
            mockAppIdManager,
            mockLaunchRulesEvaluator,
            mockExecutorService,
            mockConfigStateManager,
            mockConfigurationRulesManager
        )
        reset(mockExtensionApi)

        val event: Event = Event.Builder(
            "Update programmatic config",
            EventType.CONFIGURATION,
            EventSource.REQUEST_CONTENT
        )
            .setEventData(mapOf(CONFIGURATION_REQUEST_CONTENT_UPDATE_CONFIG to programmaticConfig))
            .build()
        `when`(mockExtensionApi.createPendingSharedState(event)).thenReturn(mockSharedStateResolver)

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
            mockAppIdManager,
            mockLaunchRulesEvaluator,
            mockExecutorService,
            mockConfigStateManager,
            mockConfigurationRulesManager
        )
        reset(mockExtensionApi)

        val event: Event = Event.Builder(
            "Clear updated configuration",
            EventType.CONFIGURATION,
            EventSource.REQUEST_CONTENT
        )
            .setEventData(mapOf(CONFIGURATION_REQUEST_CONTENT_CLEAR_UPDATED_CONFIG to true))
            .build()
        `when`(mockExtensionApi.createPendingSharedState(event)).thenReturn(mockSharedStateResolver)

        configurationExtension.handleConfigurationRequestEvent(event)

        verify(mockSharedStateResolver).resolve(mockUpdatedConfig)
        verify(mockConfigurationRulesManager).applyDownloadedRules(
            "updated.rules.url",
            mockExtensionApi
        )

        val eventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        verify(mockExtensionApi, times(1)).dispatch(eventCaptor.capture())
        verifyDispatchedEvent(
            eventCaptor.firstValue,
            EventType.CONFIGURATION,
            EventSource.RESPONSE_CONTENT,
            mockUpdatedConfig,
            null
        )
    }

    @Test
    fun `Retrieve configuration attaches responseId to dispatched response`() {
        val mockBundledConfig = mutableMapOf<String, Any?>(
            ANALYTICS_RSID_KEY to SAMPLE_RSID,
            ANALYTICS_SERVER_KEY to SAMPLE_SERVER,
            ConfigurationExtension.RULES_CONFIG_URL to "rules.url"
        )

        `when`(mockAppIdManager.loadAppId()).thenReturn("SampleAppID")
        `when`(mockConfigStateManager.loadBundledConfig(anyString())).thenReturn(mockBundledConfig)
        `when`(mockConfigStateManager.environmentAwareConfiguration).thenReturn(mockBundledConfig)

        val configurationExtension = ConfigurationExtension(
            mockExtensionApi,
            mockAppIdManager,
            mockLaunchRulesEvaluator,
            mockExecutorService,
            mockConfigStateManager,
            mockConfigurationRulesManager
        )
        reset(mockExtensionApi)

        val event: Event = Event.Builder(
            "Retrieve config",
            EventType.CONFIGURATION,
            EventSource.REQUEST_CONTENT
        )
            .setEventData(mapOf(CONFIGURATION_REQUEST_CONTENT_RETRIEVE_CONFIG to null))
            .build()
        `when`(mockExtensionApi.createPendingSharedState(event)).thenReturn(mockSharedStateResolver)

        configurationExtension.handleConfigurationRequestEvent(event)

        val eventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        verify(mockExtensionApi, times(1)).dispatch(eventCaptor.capture())
        verifyDispatchedEvent(
            eventCaptor.firstValue,
            EventType.CONFIGURATION,
            EventSource.RESPONSE_CONTENT,
            mockBundledConfig,
            event
        )
    }

    @Test
    fun `retrieveSDKIdentifiers dispatches paired Configuration ResponseIdentity event`() {
        val configurationExtension = ConfigurationExtension(
            mockExtensionApi,
            mockAppIdManager,
            mockLaunchRulesEvaluator,
            mockExecutorService,
            mockConfigStateManager,
            mockConfigurationRulesManager
        )

        val event: Event = Event.Builder(
            "SDKIdentifiers request",
            EventType.CONFIGURATION,
            EventSource.REQUEST_IDENTITY
        ).build()

        configurationExtension.retrieveSDKIdentifiers(event)
        val eventCaptor: KArgumentCaptor<Event> = argumentCaptor()
        verify(mockExtensionApi, times(1)).dispatch(eventCaptor.capture())

        assertEquals(EventType.CONFIGURATION, eventCaptor.firstValue.type)
        assertEquals(EventSource.RESPONSE_IDENTITY, eventCaptor.firstValue.source)
        assertTrue(eventCaptor.firstValue.eventData.containsKey("config.allIdentifiers"), "But EventData was: " + eventCaptor.firstValue.eventData)
        assertEquals(event.uniqueIdentifier, eventCaptor.firstValue.responseID)
    }

    @After
    fun teardown() {
        mockedStaticServiceProvider.close()
    }

    private fun verifyDispatchedEvent(
        capturedEvent: Event,
        expectedEventType: String,
        expectedEventSource: String,
        expectedEventData: Map<String, Any?>?,
        triggerEvent: Event?
    ) {
        assertEquals(expectedEventType, capturedEvent.type)
        assertEquals(expectedEventSource, capturedEvent.source)
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
