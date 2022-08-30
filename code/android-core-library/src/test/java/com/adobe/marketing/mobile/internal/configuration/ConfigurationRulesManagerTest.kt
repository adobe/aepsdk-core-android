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
import com.adobe.marketing.mobile.internal.utility.FileUtils
import com.adobe.marketing.mobile.launch.rulesengine.LaunchRulesEvaluator
import com.adobe.marketing.mobile.services.CacheFileService
import com.adobe.marketing.mobile.services.DataStoring
import com.adobe.marketing.mobile.services.DeviceInforming
import com.adobe.marketing.mobile.services.NamedCollection
import com.adobe.marketing.mobile.services.Networking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
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
import java.io.InputStream
import java.lang.reflect.Field
import kotlin.test.assertEquals
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
    private lateinit var mockCacheFileService: CacheFileService

    @Mock
    private lateinit var mockNamedCollection: NamedCollection

    @Mock
    private lateinit var mockConfigurationRuleDownloader: ConfigurationRulesDownloader

    @Mock
    private lateinit var mockDownloadedRulesDir: File

    @Mock
    private lateinit var mockExtensionApi: ExtensionApi

    private lateinit var mockFileUtils: MockedStatic<FileUtils>

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
            mockDataStoreService,
            mockDeviceInfoService,
            mockNetworkService,
            mockCacheFileService
        )
        mockFileUtils = Mockito.mockStatic(FileUtils::class.java)

        val field: Field = ConfigurationRulesManager::class.java.getDeclaredField("configRulesDownloaderSupplier")
        field.isAccessible = true
        field.set(configurationRulesManager, mockConfigRuleDownloaderSupplier)
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

        verifyNoInteractions(mockCacheFileService)
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

        verifyNoInteractions(mockCacheFileService)
        verifyNoInteractions(mockLaunchRulesEvaluator)
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

        assertFalse(configurationRulesManager.applyCachedRules(mockExtensionApi))

        verifyNoInteractions(mockLaunchRulesEvaluator)
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

        assertFalse(configurationRulesManager.applyCachedRules(mockExtensionApi))

        verifyNoInteractions(mockLaunchRulesEvaluator)
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

        mockFileUtils.`when`<Any> { FileUtils.readAsString(any()) }.thenReturn(null)

        assertFalse(configurationRulesManager.applyCachedRules(mockExtensionApi))

        val fileCaptor: KArgumentCaptor<File> = argumentCaptor()
        mockFileUtils.verify { FileUtils.readAsString(fileCaptor.capture()) }

        assertEquals(
            "${mockDownloadedRulesDir.path}${File.separator}${ConfigurationRulesManager.RULES_JSON_FILE_NAME}",
            fileCaptor.firstValue.path
        )
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
        `when`(
            mockCacheFileService.getCacheFile(
                persistedRulesURL,
                ConfigurationRulesManager.RULES_CACHE_FOLDER,
                false
            )
        ).thenReturn(mockDownloadedRulesDir)

        mockFileUtils.`when`<Any> {
            FileUtils.readAsString(any())
        }.thenReturn(validRulesJson)

        assertTrue(configurationRulesManager.applyCachedRules(mockExtensionApi))

        val fileCaptor: KArgumentCaptor<File> = argumentCaptor()

        mockFileUtils.verify {
            FileUtils.readAsString(fileCaptor.capture())
        }
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

        mockFileUtils.`when`<Any> { FileUtils.readAsString(any()) }.thenReturn(invalidRulesJson)

        assertFalse(configurationRulesManager.applyCachedRules(mockExtensionApi))

        val fileCaptor: KArgumentCaptor<File> = argumentCaptor()
        mockFileUtils.verify {
            FileUtils.readAsString(fileCaptor.capture())
        }

        assertEquals(
            "${mockDownloadedRulesDir.path}${File.separator}${ConfigurationRulesManager.RULES_JSON_FILE_NAME}",
            fileCaptor.firstValue.path
        )

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
        verify(mockConfigurationRuleDownloader).download(
            eq(urlForRules),
            eq(ConfigurationRulesManager.RULES_CACHE_FOLDER),
            any()
        )
    }

    @Test
    fun `Apply Downloaded Rules - Downloaded rules directory is null`() {
        val urlForRules = "www.example.com/rules"

        configurationRulesManager.applyDownloadedRules(urlForRules, mockExtensionApi)

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
        verifyNoInteractions(mockLaunchRulesEvaluator)
    }

    @Test
    fun `Apply Downloaded Rules - Downloaded rules directory is not a directory`() {
        val urlForRules = "www.example.com/rules"
        `when`(mockDownloadedRulesDir.isDirectory).thenReturn(false)

        configurationRulesManager.applyDownloadedRules(urlForRules, mockExtensionApi)

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
        verifyNoInteractions(mockLaunchRulesEvaluator)
    }

    @Test
    fun `Apply Downloaded Rules - Downloaded rules directory is does not have rules file`() {
        val urlForRules = "www.example.com/rules"

        mockFileUtils.`when`<Any> { FileUtils.readAsString(any()) }.thenReturn(null)

        configurationRulesManager.applyDownloadedRules(urlForRules, mockExtensionApi)

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
        verifyNoInteractions(mockLaunchRulesEvaluator)
    }

    @Test
    fun `Apply Downloaded Rules - Downloaded rules directory has invalid rules json`() {
        val urlForRules = "www.example.com/rules"
        val invalidRulesJson = "{InvalidRulesJson}"

        mockFileUtils.`when`<Any> { FileUtils.readAsString(any()) }.thenReturn(invalidRulesJson)

        configurationRulesManager.applyDownloadedRules(urlForRules, mockExtensionApi)

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
        verifyNoInteractions(mockLaunchRulesEvaluator)
    }

    @Test
    fun `Apply Downloaded Rules - Downloaded rules directory has valid rules`() {
        val urlForRules = "www.example.com/rules"

        mockFileUtils.`when`<Any> { FileUtils.readAsString(any()) }.thenReturn(validRulesJson)

        configurationRulesManager.applyDownloadedRules(urlForRules, mockExtensionApi)

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

    @Test
    fun `Apply Bundled Rules - Application Cache Dir is null`() {
        `when`(mockDeviceInfoService.applicationCacheDir).thenReturn(null)

        assertFalse(configurationRulesManager.applyBundledRules(mockExtensionApi))
        verify(mockLaunchRulesEvaluator, never()).replaceRules(any())
    }

    @Test
    fun `Apply Bundled Rules - Application Cache Dir cannot be created`() {
        val mockApplicationCacheDir = mock(File::class.java)
        val mockCacheDirPath = "/path/to/appCacheDir/"
        `when`(mockApplicationCacheDir.absolutePath).thenReturn(mockCacheDirPath)
        `when`(mockApplicationCacheDir.exists()).thenReturn(false)
        `when`(mockApplicationCacheDir.mkdirs()).thenReturn(false)
        `when`(mockDeviceInfoService.applicationCacheDir).thenReturn(mockApplicationCacheDir)

        assertFalse(configurationRulesManager.applyBundledRules(mockExtensionApi))
        verify(mockLaunchRulesEvaluator, never()).replaceRules(any())
    }

    @Test
    fun `Apply Bundled Rules - Bundled Asset cannot be read`() {
        val mockCacheDirPath = this::class.java.classLoader?.getResource("")?.path
        val mockApplicationCacheDir = mock(File::class.java)
        `when`(mockApplicationCacheDir.exists()).thenReturn(true)
        `when`(mockApplicationCacheDir.mkdirs()).thenReturn(true)
        `when`(mockApplicationCacheDir.absolutePath).thenReturn(mockCacheDirPath)

        `when`(mockDeviceInfoService.applicationCacheDir).thenReturn(mockApplicationCacheDir)
        val mockBundledRulesStream = mock(InputStream::class.java)
        `when`(mockDeviceInfoService.getAsset(ConfigurationRulesManager.BUNDLED_RULES_FILE_NAME))
            .thenReturn(mockBundledRulesStream)
        mockFileUtils.`when`<Any> {
            FileUtils.readInputStreamIntoFile(
                any(),
                eq(mockBundledRulesStream), eq(false)
            )
        }.thenReturn(false)

        assertFalse(configurationRulesManager.applyBundledRules(mockExtensionApi))

        verify(mockDeviceInfoService).getAsset(ConfigurationRulesManager.BUNDLED_RULES_FILE_NAME)
        verify(mockLaunchRulesEvaluator, never()).replaceRules(any())
    }

    @Test
    fun `Apply Bundled Rules - Bundled Asset cannot be extracted`() {
        val mockCacheDirPath = this::class.java.classLoader?.getResource("")?.path
        val mockApplicationCacheDir = mock(File::class.java)
        `when`(mockApplicationCacheDir.exists()).thenReturn(true)
        `when`(mockApplicationCacheDir.mkdirs()).thenReturn(true)
        `when`(mockApplicationCacheDir.absolutePath).thenReturn(mockCacheDirPath)
        `when`(mockDeviceInfoService.applicationCacheDir).thenReturn(mockApplicationCacheDir)
        val mockBundledRulesStream = mock(InputStream::class.java)
        `when`(mockDeviceInfoService.getAsset(ConfigurationRulesManager.BUNDLED_RULES_FILE_NAME))
            .thenReturn(mockBundledRulesStream)
        mockFileUtils.`when`<Any> {
            FileUtils.readInputStreamIntoFile(
                any(),
                eq(mockBundledRulesStream), eq(false)
            )
        }.thenReturn(true)
        mockFileUtils.`when`<Any> { FileUtils.extractFromZip(any(), any()) }.thenReturn(false)

        assertFalse(configurationRulesManager.applyBundledRules(mockExtensionApi))

        verify(mockDeviceInfoService).getAsset(ConfigurationRulesManager.BUNDLED_RULES_FILE_NAME)
        mockFileUtils.verify {
            FileUtils.extractFromZip(
                any(),
                eq(mockCacheDirPath + "adbdownloadcache")
            )
        }
        verify(mockLaunchRulesEvaluator, never()).replaceRules(any())
    }

    @Test
    fun `Apply Bundled Rules - Bundled Asset is valid`() {
        val mockCacheDirPath = this::class.java.classLoader?.getResource("")?.path
        val bundledRulesPath = File(mockCacheDirPath, "adbdownloadcache/${ConfigurationRulesManager.BUNDLED_RULES_DIR}")
        bundledRulesPath.mkdirs()
        val mockApplicationCacheDir = mock(File::class.java)
        `when`(mockApplicationCacheDir.absolutePath).thenReturn(mockCacheDirPath)
        `when`(mockDeviceInfoService.applicationCacheDir).thenReturn(mockApplicationCacheDir)
        val mockBundledRulesStream = mock(InputStream::class.java)
        `when`(mockDeviceInfoService.getAsset(ConfigurationRulesManager.BUNDLED_RULES_FILE_NAME))
            .thenReturn(mockBundledRulesStream)
        mockFileUtils.`when`<Any> {
            FileUtils.readInputStreamIntoFile(
                any(),
                eq(mockBundledRulesStream), eq(false)
            )
        }.thenReturn(true)
        mockFileUtils.`when`<Any> { FileUtils.extractFromZip(any(), any()) }.thenReturn(true)
        mockFileUtils.`when`<Any> { FileUtils.readAsString(any()) }.thenReturn(validRulesJson)

        assertTrue(configurationRulesManager.applyBundledRules(mockExtensionApi))

        verify(mockDeviceInfoService).getAsset(ConfigurationRulesManager.BUNDLED_RULES_FILE_NAME)
        verify(mockLaunchRulesEvaluator).replaceRules(any())

        bundledRulesPath.deleteRecursively()
    }

    @After
    fun teardown() {
        mockFileUtils.close()
    }
}
