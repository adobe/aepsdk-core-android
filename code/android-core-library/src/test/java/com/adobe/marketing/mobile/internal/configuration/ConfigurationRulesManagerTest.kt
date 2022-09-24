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
import com.adobe.marketing.mobile.launch.rulesengine.LaunchRulesEvaluator
import com.adobe.marketing.mobile.rulesengine.download.RulesDownloadCallback
import com.adobe.marketing.mobile.rulesengine.download.RulesDownloadResult
import com.adobe.marketing.mobile.rulesengine.download.RulesDownloader
import com.adobe.marketing.mobile.services.DataStoring
import com.adobe.marketing.mobile.services.DeviceInforming
import com.adobe.marketing.mobile.services.NamedCollection
import com.adobe.marketing.mobile.services.Networking
import com.adobe.marketing.mobile.services.caching.CacheResult
import com.adobe.marketing.mobile.services.caching.CacheService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verifyNoInteractions
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
    private lateinit var mockRulesDownloader: RulesDownloader

    @Mock
    private lateinit var mockDownloadedRulesDir: File

    @Mock
    private lateinit var mockExtensionApi: ExtensionApi

    private lateinit var configurationRulesManager: ConfigurationRulesManager
    private val validRulesJson =
        this::class.java.classLoader?.getResource("rules_parser/launch_rule_root.json")!!.readText()

    @Before
    fun setUp() {

        `when`(mockDataStoreService.getNamedCollection(ConfigurationExtension.DATASTORE_KEY)).thenReturn(
            mockNamedCollection
        )

        `when`(mockDownloadedRulesDir.isDirectory).thenReturn(true)

        configurationRulesManager = ConfigurationRulesManager(
            mockLaunchRulesEvaluator,
            mockDataStoreService,
            mockDeviceInfoService,
            mockNetworkService,
            mockCacheService,
            mockRulesDownloader
        )
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
        `when`(
            mockCacheService.get(
                ConfigurationRulesManager.RULES_CACHE_NAME,
                persistedRulesURL,
            )
        ).thenReturn(null)

        assertFalse(configurationRulesManager.applyCachedRules(mockExtensionApi))

        verifyNoInteractions(mockLaunchRulesEvaluator)
    }

    @Test
    fun `Apply Cached Rules - Cached rules are null`() {
        val persistedRulesURL = "www.example.com/rules"
        `when`(
            mockNamedCollection.getString(
                ConfigurationRulesManager.PERSISTED_RULES_URL,
                null
            )
        ).thenReturn(persistedRulesURL)

        val mockCacheResult = mock(CacheResult::class.java)
        `when`(mockCacheResult.data).thenReturn(null)

        `when`(
            mockCacheService.get(
                ConfigurationRulesManager.RULES_CACHE_NAME,
                persistedRulesURL
            )
        ).thenReturn(mockCacheResult)

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

        val mockCacheResult = mock(CacheResult::class.java)
        `when`(mockCacheResult.data).thenReturn(validRulesJson.byteInputStream())

        `when`(
            mockCacheService.get(
                ConfigurationRulesManager.RULES_CACHE_NAME,
                persistedRulesURL
            )
        ).thenReturn(mockCacheResult)

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

        val mockCacheResult = mock(CacheResult::class.java)
        `when`(mockCacheResult.data).thenReturn(invalidRulesJson.byteInputStream())

        `when`(
            mockCacheService.get(
                ConfigurationRulesManager.RULES_CACHE_NAME,
                persistedRulesURL
            )
        ).thenReturn(mockCacheResult)

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
        verify(mockRulesDownloader).load(
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

        val callbackCaptor: KArgumentCaptor<RulesDownloadCallback> = argumentCaptor()

        verify(mockRulesDownloader).load(
            eq(urlForRules),
            callbackCaptor.capture()
        )

        val capturedCallback = callbackCaptor.firstValue

        // Simulate callback invocation
        capturedCallback.call(
            RulesDownloadResult(
                null,
                RulesDownloadResult.Reason.SUCCESS
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

        val callbackCaptor: KArgumentCaptor<RulesDownloadCallback> = argumentCaptor()

        verify(mockRulesDownloader).load(
            eq(urlForRules),
            callbackCaptor.capture()
        )

        val capturedCallback = callbackCaptor.firstValue

        // Simulate callback invocation
        capturedCallback.call(
            RulesDownloadResult(
                invalidRulesJson, RulesDownloadResult.Reason.SUCCESS
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

        val callbackCaptor: KArgumentCaptor<RulesDownloadCallback> = argumentCaptor()

        verify(mockRulesDownloader).load(
            eq(urlForRules),
            callbackCaptor.capture()
        )

        val capturedCallback = callbackCaptor.firstValue

        // Simulate callback invocation
        capturedCallback.call(
            RulesDownloadResult(
                validRulesJson, RulesDownloadResult.Reason.SUCCESS
            )
        )

        verify(mockLaunchRulesEvaluator, times(1)).replaceRules(any())
    }

    @Test
    fun `Apply Bundled Rules - bundled rules cannot be extracted`() {
        `when`(mockRulesDownloader.load(ConfigurationRulesManager.BUNDLED_RULES_FILE_NAME)).thenReturn(
            RulesDownloadResult(null, RulesDownloadResult.Reason.ZIP_EXTRACTION_FAILED)
        )

        configurationRulesManager.applyBundledRules(mockExtensionApi)

        verify(mockLaunchRulesEvaluator, never()).replaceRules(any())
    }

    @Test
    fun `Apply Bundled Rules - RulesDownloadResult has null data`() {
        `when`(mockRulesDownloader.load(ConfigurationRulesManager.BUNDLED_RULES_FILE_NAME)).thenReturn(
            RulesDownloadResult(null, RulesDownloadResult.Reason.SUCCESS)
        )

        configurationRulesManager.applyBundledRules(mockExtensionApi)

        verify(mockLaunchRulesEvaluator, never()).replaceRules(any())
    }

    @Test
    fun `Apply Bundled Rules - temporary cache cannot be written into`() {
        `when`(mockRulesDownloader.load(ConfigurationRulesManager.BUNDLED_RULES_FILE_NAME)).thenReturn(
            RulesDownloadResult(null, RulesDownloadResult.Reason.CANNOT_CREATE_TEMP_DIR)
        )

        configurationRulesManager.applyBundledRules(mockExtensionApi)

        verify(mockLaunchRulesEvaluator, never()).replaceRules(any())
    }

    @Test
    fun `Apply Bundled Rules - RulesDownloadResult has valid data`() {
        `when`(mockRulesDownloader.load(ConfigurationRulesManager.BUNDLED_RULES_FILE_NAME)).thenReturn(
            RulesDownloadResult(validRulesJson, RulesDownloadResult.Reason.SUCCESS)
        )

        configurationRulesManager.applyBundledRules(mockExtensionApi)

        verify(mockLaunchRulesEvaluator, times(1)).replaceRules(any())
    }
}
