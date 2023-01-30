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

import com.adobe.marketing.mobile.AdobeCallback
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.launch.rulesengine.LaunchRulesEvaluator
import com.adobe.marketing.mobile.launch.rulesengine.download.RulesLoadResult
import com.adobe.marketing.mobile.launch.rulesengine.download.RulesLoader
import com.adobe.marketing.mobile.services.DataStoring
import com.adobe.marketing.mobile.services.DeviceInforming
import com.adobe.marketing.mobile.services.NamedCollection
import com.adobe.marketing.mobile.services.Networking
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.caching.CacheService
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.io.File
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner.Silent::class)
class ConfigurationRulesManagerTest {

    @Mock
    private lateinit var mockLaunchRulesEvaluator: LaunchRulesEvaluator

    @Mock
    private lateinit var mockDataStoreService: DataStoring

    @Mock
    private lateinit var mockDeviceInfoService: DeviceInforming

    @Mock
    private lateinit var mockNetworkService: Networking

    @Mock
    private lateinit var mockCacheService: CacheService

    @Mock
    private lateinit var mockNamedCollection: NamedCollection

    @Mock
    private lateinit var mockRulesLoader: RulesLoader

    @Mock
    private lateinit var mockDownloadedRulesDir: File

    @Mock
    private lateinit var mockExtensionApi: ExtensionApi

    @Mock
    private lateinit var mockServiceProvider: ServiceProvider

    private lateinit var mockedStaticServiceProvider: MockedStatic<ServiceProvider>

    private lateinit var configurationRulesManager: ConfigurationRulesManager
    private val validRulesJson =
        this::class.java.classLoader?.getResource("rules_parser/launch_rule_root.json")!!.readText()

    @Before
    fun setUp() {
        `when`(mockDataStoreService.getNamedCollection(ConfigurationExtension.DATASTORE_KEY)).thenReturn(
            mockNamedCollection
        )

        mockedStaticServiceProvider = Mockito.mockStatic(ServiceProvider::class.java)
        mockedStaticServiceProvider.`when`<Any> { ServiceProvider.getInstance() }.thenReturn(mockServiceProvider)
        `when`(mockServiceProvider.dataStoreService).thenReturn(mockDataStoreService)
        `when`(mockServiceProvider.deviceInfoService).thenReturn(mockDeviceInfoService)
        `when`(mockServiceProvider.networkService).thenReturn(mockNetworkService)

        `when`(mockDownloadedRulesDir.isDirectory).thenReturn(true)

        configurationRulesManager = ConfigurationRulesManager(mockLaunchRulesEvaluator, mockRulesLoader)
    }

    @Test
    fun `Apply Cached Rules - Persisted Rules URL is Empty`() {
        `when`(
            mockNamedCollection.getString(
                ConfigurationRulesManager.PERSISTED_RULES_URL,
                null
            )
        ).thenReturn(" ")

        assertFalse(configurationRulesManager.applyCachedRules(mockExtensionApi))

        verifyNoInteractions(mockCacheService)
        verifyNoInteractions(mockLaunchRulesEvaluator)
    }

    @Test
    fun `Apply Cached Rules - Persisted Rules URL is Null`() {
        `when`(
            mockNamedCollection.getString(
                ConfigurationRulesManager.PERSISTED_RULES_URL,
                null
            )
        ).thenReturn(null)

        assertFalse(configurationRulesManager.applyCachedRules(mockExtensionApi))

        verifyNoInteractions(mockCacheService)
        verifyNoInteractions(mockLaunchRulesEvaluator)
    }

    @Test
    fun `Apply Cached Rules - Cached entry for URL does not exist`() {
        val persistedRulesURL = "www.example.com/rules"
        `when`(
            mockNamedCollection.getString(
                ConfigurationRulesManager.PERSISTED_RULES_URL,
                null
            )
        ).thenReturn(persistedRulesURL)

        val rulesLoadResult = mock(RulesLoadResult::class.java)
        `when`(rulesLoadResult.data).thenReturn(null)
        `when`(
            mockRulesLoader.loadFromCache(persistedRulesURL)
        ).thenReturn(rulesLoadResult)

        assertFalse(configurationRulesManager.applyCachedRules(mockExtensionApi))

        verifyNoInteractions(mockLaunchRulesEvaluator)
    }

    @Test
    fun `Apply Cached Rules - Cached rule directory has Valid content`() {
        val persistedRulesURL = "www.example.com/rules"
        `when`(
            mockNamedCollection.getString(
                ConfigurationRulesManager.PERSISTED_RULES_URL,
                null
            )
        ).thenReturn(persistedRulesURL)

        val rulesLoadResult = mock(RulesLoadResult::class.java)
        `when`(rulesLoadResult.data).thenReturn(validRulesJson)
        `when`(rulesLoadResult.reason).thenReturn(RulesLoadResult.Reason.SUCCESS)

        `when`(
            mockRulesLoader.loadFromCache(persistedRulesURL)
        ).thenReturn(rulesLoadResult)

        assertTrue(configurationRulesManager.applyCachedRules(mockExtensionApi))

        verify(mockLaunchRulesEvaluator, times(1)).replaceRules(any())
    }

    @Test
    fun `Apply Cached Rules - Cached rule directory rule content fails to parse`() {
        val persistedRulesURL = "www.example.com/rules"
        val invalidRulesJson = "{InvalidRulesJson}"
        `when`(
            mockNamedCollection.getString(
                ConfigurationRulesManager.PERSISTED_RULES_URL,
                null
            )
        ).thenReturn(persistedRulesURL)

        val rulesLoadResult = mock(RulesLoadResult::class.java)
        `when`(rulesLoadResult.data).thenReturn(invalidRulesJson)

        `when`(
            mockRulesLoader.loadFromCache(persistedRulesURL)
        ).thenReturn(rulesLoadResult)

        verifyNoInteractions(mockLaunchRulesEvaluator)
    }

    @Test
    fun `Apply Downloaded Rules - Persists URL and triggers download`() {
        val urlForRules = "www.example.com/rules"

        configurationRulesManager.applyDownloadedRules(urlForRules, mockExtensionApi)

        verify(mockNamedCollection).setString(
            ConfigurationRulesManager.PERSISTED_RULES_URL,
            urlForRules
        )
        verify(mockRulesLoader).loadFromUrl(
            eq(urlForRules),
            any()
        )
    }

    @Test
    fun `Apply Downloaded Rules - Downloaded rules are null`() {
        val urlForRules = "www.example.com/rules"

        configurationRulesManager.applyDownloadedRules(urlForRules, mockExtensionApi)

        verify(mockNamedCollection).setString(
            ConfigurationRulesManager.PERSISTED_RULES_URL,
            urlForRules
        )

        val callbackCaptor: KArgumentCaptor<AdobeCallback<RulesLoadResult>> = argumentCaptor()

        verify(mockRulesLoader).loadFromUrl(
            eq(urlForRules),
            callbackCaptor.capture()
        )

        val capturedCallback = callbackCaptor.firstValue

        // Simulate callback invocation
        capturedCallback.call(
            RulesLoadResult(
                null,
                RulesLoadResult.Reason.SUCCESS
            )
        )

        verifyNoInteractions(mockLaunchRulesEvaluator)
    }

    @Test
    fun `Apply Downloaded Rules - Downloaded rules directory has invalid rules json`() {
        val urlForRules = "www.example.com/rules"
        val invalidRulesJson = "{InvalidRulesJson}"

        configurationRulesManager.applyDownloadedRules(urlForRules, mockExtensionApi)

        verify(mockNamedCollection).setString(
            ConfigurationRulesManager.PERSISTED_RULES_URL,
            urlForRules
        )

        val callbackCaptor: KArgumentCaptor<AdobeCallback<RulesLoadResult>> = argumentCaptor()

        verify(mockRulesLoader).loadFromUrl(
            eq(urlForRules),
            callbackCaptor.capture()
        )

        val capturedCallback = callbackCaptor.firstValue

        // Simulate callback invocation
        capturedCallback.call(
            RulesLoadResult(
                invalidRulesJson,
                RulesLoadResult.Reason.SUCCESS
            )
        )

        verifyNoInteractions(mockLaunchRulesEvaluator)
    }

    @Test
    fun `Apply Downloaded Rules - Downloaded rules directory has valid rules`() {
        val urlForRules = "www.example.com/rules"

        configurationRulesManager.applyDownloadedRules(urlForRules, mockExtensionApi)

        verify(mockNamedCollection).setString(
            ConfigurationRulesManager.PERSISTED_RULES_URL,
            urlForRules
        )

        val callbackCaptor: KArgumentCaptor<AdobeCallback<RulesLoadResult>> = argumentCaptor()

        verify(mockRulesLoader).loadFromUrl(
            eq(urlForRules),
            callbackCaptor.capture()
        )

        val capturedCallback = callbackCaptor.firstValue

        // Simulate callback invocation
        capturedCallback.call(
            RulesLoadResult(
                validRulesJson,
                RulesLoadResult.Reason.SUCCESS
            )
        )

        verify(mockLaunchRulesEvaluator, times(1)).replaceRules(any())
    }

    @Test
    fun `Apply Bundled Rules - bundled rules cannot be extracted`() {
        `when`(mockRulesLoader.loadFromAsset(ConfigurationRulesManager.BUNDLED_RULES_FILE_NAME)).thenReturn(
            RulesLoadResult(
                null,
                RulesLoadResult.Reason.ZIP_EXTRACTION_FAILED
            )
        )

        configurationRulesManager.applyBundledRules(mockExtensionApi)

        verify(mockLaunchRulesEvaluator, never()).replaceRules(any())
    }

    @Test
    fun `Apply Bundled Rules - RulesDownloadResult has null data`() {
        `when`(mockRulesLoader.loadFromAsset(ConfigurationRulesManager.BUNDLED_RULES_FILE_NAME)).thenReturn(
            RulesLoadResult(
                null,
                RulesLoadResult.Reason.SUCCESS
            )
        )

        configurationRulesManager.applyBundledRules(mockExtensionApi)

        verify(mockLaunchRulesEvaluator, never()).replaceRules(any())
    }

    @Test
    fun `Apply Bundled Rules - temporary cache cannot be written into`() {
        `when`(mockRulesLoader.loadFromAsset(ConfigurationRulesManager.BUNDLED_RULES_FILE_NAME)).thenReturn(
            RulesLoadResult(
                null,
                RulesLoadResult.Reason.CANNOT_CREATE_TEMP_DIR
            )
        )

        configurationRulesManager.applyBundledRules(mockExtensionApi)

        verify(mockLaunchRulesEvaluator, never()).replaceRules(any())
    }

    @Test
    fun `Apply Bundled Rules - RulesDownloadResult has valid data`() {
        `when`(mockRulesLoader.loadFromAsset(ConfigurationRulesManager.BUNDLED_RULES_FILE_NAME)).thenReturn(
            RulesLoadResult(
                validRulesJson,
                RulesLoadResult.Reason.SUCCESS
            )
        )

        configurationRulesManager.applyBundledRules(mockExtensionApi)

        verify(mockLaunchRulesEvaluator, times(1)).replaceRules(any())
    }

    @After
    fun teardown() {
        mockedStaticServiceProvider.close()
    }
}
