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

import com.adobe.marketing.mobile.internal.utility.FileUtils
import com.adobe.marketing.mobile.launch.rulesengine.LaunchRulesEvaluator
import com.adobe.marketing.mobile.services.CacheFileService
import com.adobe.marketing.mobile.services.DataStoring
import com.adobe.marketing.mobile.services.NamedCollection
import com.adobe.marketing.mobile.services.Networking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyZeroInteractions
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import org.powermock.reflect.Whitebox
import java.io.File
import kotlin.test.assertEquals

@RunWith(PowerMockRunner::class)
@PrepareForTest(LaunchRulesEvaluator::class, ConfigurationRulesDownloader::class, FileUtils::class)
class ConfigurationRulesManagerTest {

    @Mock
    private lateinit var mockLaunchRulesEvaluator: LaunchRulesEvaluator

    @Mock
    private lateinit var mockDataStoreService: DataStoring

    @Mock
    private lateinit var mockNetworkService: Networking

    @Mock
    private lateinit var mockCacheFileService: CacheFileService

    @Mock
    private lateinit var mockNamedCollection: NamedCollection

    @Mock
    private lateinit var mockConfigurationRuleDownloader: ConfigurationRulesDownloader

    @Mock
    private lateinit var mockDownloadedRulesDir: File

    @Mock
    private var mockConfigRuleDownloaderSupplier: (Networking, CacheFileService, ZipFileMetadataProvider) -> ConfigurationRulesDownloader =
        { _, _, _ ->
            mockConfigurationRuleDownloader
        }

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
            mockDataStoreService, mockNetworkService, mockCacheFileService
        )

        Whitebox.setInternalState(
            configurationRulesManager,
            "configRulesDownloaderSupplier",
            mockConfigRuleDownloaderSupplier
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

        configurationRulesManager.applyCachedRules()

        verifyZeroInteractions(mockCacheFileService)
        verifyZeroInteractions(mockLaunchRulesEvaluator)
    }

    @Test
    fun `Apply Cached Rules - Persisted Rules URL is Null`() {
        `when`(
            mockNamedCollection.getString(
                ConfigurationRulesManager.PERSISTED_RULES_URL,
                null
            )
        ).thenReturn(null)

        configurationRulesManager.applyCachedRules()

        verifyZeroInteractions(mockCacheFileService)
        verifyZeroInteractions(mockLaunchRulesEvaluator)
    }

    @Test
    fun `Apply Cached Rules - Cached rule directory for URL does not exist`() {
        val persistedRulesURL = "www.example.com/rules"
        `when`(
            mockNamedCollection.getString(
                ConfigurationRulesManager.PERSISTED_RULES_URL,
                null
            )
        ).thenReturn(persistedRulesURL)
        `when`(
            mockCacheFileService.getCacheFile(
                persistedRulesURL,
                ConfigurationRulesManager.RULES_CACHE_FOLDER,
                false
            )
        ).thenReturn(null)

        configurationRulesManager.applyCachedRules()

        verifyZeroInteractions(mockLaunchRulesEvaluator)
    }

    @Test
    fun `Apply Cached Rules - Cached rule directory for URL is not a directory`() {
        val persistedRulesURL = "www.example.com/rules"
        `when`(
            mockNamedCollection.getString(
                ConfigurationRulesManager.PERSISTED_RULES_URL,
                null
            )
        ).thenReturn(persistedRulesURL)
        `when`(mockDownloadedRulesDir.isDirectory).thenReturn(false)
        `when`(
            mockCacheFileService.getCacheFile(
                persistedRulesURL,
                ConfigurationRulesManager.RULES_CACHE_FOLDER,
                false
            )
        ).thenReturn(mockDownloadedRulesDir)

        configurationRulesManager.applyCachedRules()

        verifyZeroInteractions(mockLaunchRulesEvaluator)
    }

    @Test
    fun `Apply Cached Rules - Cached rule directory has null content`() {
        val persistedRulesURL = "www.example.com/rules"
        `when`(
            mockNamedCollection.getString(
                ConfigurationRulesManager.PERSISTED_RULES_URL,
                null
            )
        ).thenReturn(persistedRulesURL)
        `when`(
            mockCacheFileService.getCacheFile(
                persistedRulesURL,
                ConfigurationRulesManager.RULES_CACHE_FOLDER,
                false
            )
        ).thenReturn(mockDownloadedRulesDir)

        PowerMockito.mockStatic(FileUtils::class.java)
        `when`(FileUtils.readAsString(any())).thenReturn(null)

        configurationRulesManager.applyCachedRules()

        val fileCaptor: KArgumentCaptor<File> = argumentCaptor()
        PowerMockito.verifyStatic(FileUtils::class.java, times(1))
        FileUtils.readAsString(fileCaptor.capture())
        assertEquals(
            "${mockDownloadedRulesDir.path}${File.separator}${ConfigurationRulesManager.RULES_JSON_FILE_NAME}",
            fileCaptor.firstValue.path
        )
        verifyZeroInteractions(mockLaunchRulesEvaluator)
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
        `when`(
            mockCacheFileService.getCacheFile(
                persistedRulesURL,
                ConfigurationRulesManager.RULES_CACHE_FOLDER,
                false
            )
        ).thenReturn(mockDownloadedRulesDir)

        PowerMockito.mockStatic(FileUtils::class.java)
        `when`(FileUtils.readAsString(any())).thenReturn(validRulesJson)

        configurationRulesManager.applyCachedRules()

        val fileCaptor: KArgumentCaptor<File> = argumentCaptor()
        PowerMockito.verifyStatic(FileUtils::class.java, times(1))
        FileUtils.readAsString(fileCaptor.capture())
        assertEquals(
            "${mockDownloadedRulesDir.path}${File.separator}${ConfigurationRulesManager.RULES_JSON_FILE_NAME}",
            fileCaptor.firstValue.path
        )

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
        `when`(
            mockCacheFileService.getCacheFile(
                persistedRulesURL,
                ConfigurationRulesManager.RULES_CACHE_FOLDER,
                false
            )
        ).thenReturn(mockDownloadedRulesDir)

        PowerMockito.mockStatic(FileUtils::class.java)
        `when`(FileUtils.readAsString(any())).thenReturn(invalidRulesJson)

        configurationRulesManager.applyCachedRules()

        val fileCaptor: KArgumentCaptor<File> = argumentCaptor()
        PowerMockito.verifyStatic(FileUtils::class.java, times(1))
        FileUtils.readAsString(fileCaptor.capture())
        assertEquals(
            "${mockDownloadedRulesDir.path}${File.separator}${ConfigurationRulesManager.RULES_JSON_FILE_NAME}",
            fileCaptor.firstValue.path
        )

        verifyZeroInteractions(mockLaunchRulesEvaluator)
    }

    @Test
    fun `Apply Downloaded Rules - Persists URL and triggers download`() {
        val urlForRules = "www.example.com/rules"

        configurationRulesManager.applyDownloadedRules(urlForRules)

        verify(mockNamedCollection).setString(
            ConfigurationRulesManager.PERSISTED_RULES_URL,
            urlForRules
        )
        verify(mockConfigurationRuleDownloader).download(
            eq(urlForRules),
            eq(ConfigurationRulesManager.RULES_CACHE_FOLDER),
            any()
        )
    }

    @Test
    fun `Apply Downloaded Rules - Downloaded rules directory is null`() {
        val urlForRules = "www.example.com/rules"

        configurationRulesManager.applyDownloadedRules(urlForRules)

        verify(mockNamedCollection).setString(
            ConfigurationRulesManager.PERSISTED_RULES_URL,
            urlForRules
        )
        val callbackCaptor: KArgumentCaptor<(file: File?) -> Unit> = argumentCaptor()

        verify(mockConfigurationRuleDownloader).download(
            eq(urlForRules),
            eq(ConfigurationRulesManager.RULES_CACHE_FOLDER),
            callbackCaptor.capture()
        )

        val capturedCallback = callbackCaptor.firstValue

        // Simulate callback invocation
        capturedCallback.invoke(null)
        verifyZeroInteractions(mockLaunchRulesEvaluator)
    }

    @Test
    fun `Apply Downloaded Rules - Downloaded rules directory is not a directory`() {
        val urlForRules = "www.example.com/rules"
        `when`(mockDownloadedRulesDir.isDirectory).thenReturn(false)
        configurationRulesManager.applyDownloadedRules(urlForRules)

        verify(mockNamedCollection).setString(
            ConfigurationRulesManager.PERSISTED_RULES_URL,
            urlForRules
        )
        val callbackCaptor: KArgumentCaptor<(file: File?) -> Unit> = argumentCaptor()

        verify(mockConfigurationRuleDownloader).download(
            eq(urlForRules),
            eq(ConfigurationRulesManager.RULES_CACHE_FOLDER),
            callbackCaptor.capture()
        )

        val capturedCallback = callbackCaptor.firstValue

        // Simulate callback invocation
        capturedCallback.invoke(mockDownloadedRulesDir)
        verifyZeroInteractions(mockLaunchRulesEvaluator)
    }

    @Test
    fun `Apply Downloaded Rules - Downloaded rules directory is does not have rules file`() {
        val urlForRules = "www.example.com/rules"
        PowerMockito.mockStatic(FileUtils::class.java)
        `when`(FileUtils.readAsString(any())).thenReturn(null)

        configurationRulesManager.applyDownloadedRules(urlForRules)

        verify(mockNamedCollection).setString(
            ConfigurationRulesManager.PERSISTED_RULES_URL,
            urlForRules
        )
        val callbackCaptor: KArgumentCaptor<(file: File?) -> Unit> = argumentCaptor()

        verify(mockConfigurationRuleDownloader).download(
            eq(urlForRules),
            eq(ConfigurationRulesManager.RULES_CACHE_FOLDER),
            callbackCaptor.capture()
        )

        val capturedCallback = callbackCaptor.firstValue

        // Simulate callback invocation
        capturedCallback.invoke(mockDownloadedRulesDir)
        verifyZeroInteractions(mockLaunchRulesEvaluator)
    }

    @Test
    fun `Apply Downloaded Rules - Downloaded rules directory has invalid rules json`() {
        val urlForRules = "www.example.com/rules"
        val invalidRulesJson = "{InvalidRulesJson}"
        PowerMockito.mockStatic(FileUtils::class.java)
        `when`(FileUtils.readAsString(any())).thenReturn(invalidRulesJson)

        configurationRulesManager.applyDownloadedRules(urlForRules)

        verify(mockNamedCollection).setString(
            ConfigurationRulesManager.PERSISTED_RULES_URL,
            urlForRules
        )
        val callbackCaptor: KArgumentCaptor<(file: File?) -> Unit> = argumentCaptor()

        verify(mockConfigurationRuleDownloader).download(
            eq(urlForRules),
            eq(ConfigurationRulesManager.RULES_CACHE_FOLDER),
            callbackCaptor.capture()
        )
        val capturedCallback = callbackCaptor.firstValue
        // Simulate callback invocation
        capturedCallback.invoke(mockDownloadedRulesDir)
        verifyZeroInteractions(mockLaunchRulesEvaluator)
    }

    @Test
    fun `Apply Downloaded Rules - Downloaded rules directory has valid rules`() {
        val urlForRules = "www.example.com/rules"
        PowerMockito.mockStatic(FileUtils::class.java)
        `when`(FileUtils.readAsString(any())).thenReturn(validRulesJson)

        configurationRulesManager.applyDownloadedRules(urlForRules)

        verify(mockNamedCollection).setString(
            ConfigurationRulesManager.PERSISTED_RULES_URL,
            urlForRules
        )
        val callbackCaptor: KArgumentCaptor<(file: File?) -> Unit> = argumentCaptor()

        verify(mockConfigurationRuleDownloader).download(
            eq(urlForRules),
            eq(ConfigurationRulesManager.RULES_CACHE_FOLDER),
            callbackCaptor.capture()
        )
        val capturedCallback = callbackCaptor.firstValue
        // Simulate callback invocation
        capturedCallback.invoke(mockDownloadedRulesDir)

        verify(mockLaunchRulesEvaluator, times(1)).replaceRules(any())
    }
}