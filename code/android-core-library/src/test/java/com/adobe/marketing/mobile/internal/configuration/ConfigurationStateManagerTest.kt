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

import com.adobe.marketing.mobile.internal.util.FileUtils
import com.adobe.marketing.mobile.services.DataStoring
import com.adobe.marketing.mobile.services.DeviceInforming
import com.adobe.marketing.mobile.services.NamedCollection
import com.adobe.marketing.mobile.services.Networking
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.caching.CacheResult
import com.adobe.marketing.mobile.services.caching.CacheService
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import java.io.File
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner.Silent::class)
class ConfigurationStateManagerTest {

    @Mock
    private lateinit var mockAppIdManager: AppIdManager

    @Mock
    private lateinit var mockCacheService: CacheService

    @Mock
    private lateinit var mockNetworkService: Networking

    @Mock
    private lateinit var mockDeviceInfoService: DeviceInforming

    @Mock
    private lateinit var mockDataStoreService: DataStoring

    @Mock
    private lateinit var mockConfigurationDownloader: ConfigurationDownloader

    @Mock
    private lateinit var mockNamedCollection: NamedCollection

    @Mock
    private lateinit var mockCompletionCallback: (Map<String, Any?>?) -> Unit

    @Mock
    private lateinit var mockServiceProvider: ServiceProvider

    private lateinit var mockedStaticServiceProvider: MockedStatic<ServiceProvider>

    private lateinit var mockFileUtils: MockedStatic<FileUtils>

    private lateinit var configurationStateManager: ConfigurationStateManager

    private val mockPersistedProgrammaticConfigJson = "{" +
        "\"Key1\":\"persisted_Key1Value\"," +
        "\"Key2\":0," +
        "\"Key3\":false," +
        "}"

    private val mockBundledConfigJson = "{\"Key\":true}"

    private val mockCachedConfigJson = "{" +
        "\"Key4\":\"cached_Key4Value\"," +
        "\"Key5\":1," +
        "\"Key6\":true," +
        "}"

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        `when`(mockDataStoreService.getNamedCollection(ConfigurationExtension.DATASTORE_KEY)).thenReturn(
            mockNamedCollection
        )
        `when`(
            mockNamedCollection.getString(
                ConfigurationStateManager.PERSISTED_OVERRIDDEN_CONFIG,
                null
            )
        ).thenReturn(mockPersistedProgrammaticConfigJson)

        `when`(mockDeviceInfoService.getAsset(ConfigurationStateManager.CONFIG_BUNDLED_FILE_NAME)).thenReturn(
            mockBundledConfigJson.byteInputStream()
        )

        mockFileUtils = Mockito.mockStatic(FileUtils::class.java)

        mockedStaticServiceProvider = Mockito.mockStatic(ServiceProvider::class.java)
        mockedStaticServiceProvider.`when`<Any> { ServiceProvider.getInstance() }.thenReturn(mockServiceProvider)
        `when`(mockServiceProvider.dataStoreService).thenReturn(mockDataStoreService)
        `when`(mockServiceProvider.deviceInfoService).thenReturn(mockDeviceInfoService)
        `when`(mockServiceProvider.networkService).thenReturn(mockNetworkService)
        `when`(mockServiceProvider.cacheService).thenReturn(mockCacheService)

        configurationStateManager =
            ConfigurationStateManager(mockAppIdManager, mockConfigurationDownloader)
    }

    @Test
    fun `Load Initial Config - loads bundled config when app id is null`() {
        `when`(mockAppIdManager.loadAppId()).thenReturn(null)

        val expectedInitialConfig = mapOf<String, Any?>(
            "Key" to true,
            "Key1" to "persisted_Key1Value",
            "Key2" to 0,
            "Key3" to false
        )

        configurationStateManager.loadInitialConfig()

        assertNotNull(configurationStateManager.environmentAwareConfiguration)
        assertEquals(expectedInitialConfig, configurationStateManager.environmentAwareConfiguration)
    }

    @Test
    fun `Load Initial Config - loads cached config when app id is not null`() {
        `when`(mockAppIdManager.loadAppId()).thenReturn("SampleAppId")

        val mockCacheResult = mock(CacheResult::class.java)
        `when`(mockCacheResult.data).thenReturn(mockCachedConfigJson.byteInputStream())
        `when`(
            mockCacheService.get(
                ConfigurationDownloader.CONFIG_CACHE_NAME,
                "https://assets.adobedtm.com/SampleAppId.json"
            )
        ).thenReturn(mockCacheResult)

        val expectedInitialConfig = mapOf<String, Any?>(
            "Key1" to "persisted_Key1Value",
            "Key2" to 0,
            "Key3" to false,
            "Key4" to "cached_Key4Value",
            "Key5" to 1,
            "Key6" to true
        )

        configurationStateManager.loadInitialConfig()

        assertNotNull(configurationStateManager.environmentAwareConfiguration)
        assertEquals(expectedInitialConfig, configurationStateManager.environmentAwareConfiguration)
    }

    @Test
    fun `Load Initial Config - loads bundled config when cached config is invalid`() {
        `when`(mockAppIdManager.loadAppId()).thenReturn("SampleAppId")
        val mockCacheResult = mock(CacheResult::class.java)

        val malformedCachedConfig = "{SomeMalformedContent}"
        `when`(mockCacheResult.data).thenReturn(malformedCachedConfig.byteInputStream())
        `when`(
            mockCacheService.get(
                ConfigurationDownloader.CONFIG_CACHE_NAME,
                "https://assets.adobedtm.com/SampleAppId.json"
            )
        ).thenReturn(mockCacheResult)

        val expectedInitialConfig = mapOf<String, Any?>(
            "Key" to true,
            "Key1" to "persisted_Key1Value",
            "Key2" to 0,
            "Key3" to false
        )

        configurationStateManager.loadInitialConfig()

        assertNotNull(configurationStateManager.environmentAwareConfiguration)
        assertEquals(expectedInitialConfig, configurationStateManager.environmentAwareConfiguration)
    }

    @Test
    fun `Load Initial Config - loads bundled config when cached config is null`() {
        `when`(mockAppIdManager.loadAppId()).thenReturn("SampleAppId")
        `when`(
            mockCacheService.get(
                ConfigurationDownloader.CONFIG_CACHE_NAME,
                "https://assets.adobedtm.com/SampleAppId.json"
            )
        ).thenReturn(null)

        val expectedInitialConfig = mapOf<String, Any?>(
            "Key" to true,
            "Key1" to "persisted_Key1Value",
            "Key2" to 0,
            "Key3" to false
        )

        configurationStateManager.loadInitialConfig()

        assertNotNull(configurationStateManager.environmentAwareConfiguration)
        assertEquals(expectedInitialConfig, configurationStateManager.environmentAwareConfiguration)
    }

    @Test
    fun `Get bundled config - returns null when asset does not exist`() {
        val assetFileName = "SampleAssetFile"
        `when`(mockDeviceInfoService.getAsset(assetFileName)).thenReturn(null)

        val bundledConfig = configurationStateManager.loadBundledConfig(assetFileName)
        assertNull(bundledConfig)
    }

    @Test
    fun `Get bundled config - returns null when asset content is malformed`() {
        val assetFileName = "SampleAssetFile"
        val malformedInputStream = "{SomeMalformedContent}".byteInputStream()
        `when`(mockDeviceInfoService.getAsset(assetFileName)).thenReturn(malformedInputStream)

        val bundledConfig = configurationStateManager.loadBundledConfig(assetFileName)
        assertNull(bundledConfig)
    }

    @Test
    fun `Get bundled config - returns valid config when asset content is valid`() {
        val assetFileName = "SampleAssetFile"
        val malformedInputStream = mockBundledConfigJson.byteInputStream()
        `when`(mockDeviceInfoService.getAsset(assetFileName)).thenReturn(malformedInputStream)

        val bundledConfig = configurationStateManager.loadBundledConfig(assetFileName)
        assertEquals(mapOf("Key" to true), bundledConfig)
    }

    @Test
    fun `Get config from file - returns null when file content is null`() {
        val mockFile = mock(File::class.java)
        `when`(mockFile.absolutePath).thenReturn("some/path/to/file.txt")

        mockFileUtils.`when`<Any> {
            FileUtils.readAsString(mockFile)
        }.thenReturn(null)

        val configFromFile = configurationStateManager.getConfigFromFile(mockFile.absolutePath)
        assertNull(configFromFile)
    }

    @Test
    fun `Get config from file - returns null when file content is empty`() {
        val mockFile = mock(File::class.java)
        `when`(mockFile.absolutePath).thenReturn("some/path/to/file.txt")

        mockFileUtils.`when`<Any> {
            FileUtils.readAsString(mockFile)
        }.thenReturn("")

        val configFromFile = configurationStateManager.getConfigFromFile(mockFile.absolutePath)
        assertNull(configFromFile)
    }

    @Test
    fun `Update Config with AppID - when config download fails`() {
        configurationStateManager.updateConfigWithAppId("NewAppID", mockCompletionCallback)
        val expectedURL = "https://assets.adobedtm.com/NewAppID.json"
        val callbackCaptor: KArgumentCaptor<(Map<String, Any?>?) -> Unit> = argumentCaptor()

        verify(mockConfigurationDownloader).download(eq(expectedURL), callbackCaptor.capture())

        val capturedCallback = callbackCaptor.firstValue

        // Simulate null download response
        capturedCallback.invoke(null)

        verify(mockCompletionCallback).invoke(null)
    }

    @Test
    fun `Update Config with AppID - when config download succeeds`() {
        // reset any programmatic config
        configurationStateManager.clearProgrammaticConfig()
        assertTrue(configurationStateManager.hasConfigExpired("NewAppID"))

        configurationStateManager.updateConfigWithAppId("NewAppID", mockCompletionCallback)
        val expectedURL = "https://assets.adobedtm.com/NewAppID.json"
        val mockDownloadConfigResponse = mapOf(
            "DownloadedKey1" to "DownloadedValue1",
            "DownloadedKey2" to "DownloadedValue2"
        )

        val callbackCaptor: KArgumentCaptor<(Map<String, Any?>?) -> Unit> = argumentCaptor()

        verify(mockConfigurationDownloader).download(eq(expectedURL), callbackCaptor.capture())

        val capturedCallback = callbackCaptor.firstValue

        // Simulate null download response
        capturedCallback.invoke(mockDownloadConfigResponse)

        assertFalse(configurationStateManager.hasConfigExpired("NewAppID"))
        verify(mockCompletionCallback).invoke(mockDownloadConfigResponse)
    }

    @Test
    fun `Clear programmatic config - programmatic config exists`() {
        // Setup with initial config
        `when`(mockAppIdManager.loadAppId()).thenReturn("SampleAppId")

        val mockCacheResult = mock(CacheResult::class.java)
        `when`(mockCacheResult.data).thenReturn(mockCachedConfigJson.byteInputStream())
        `when`(
            mockCacheService.get(
                ConfigurationDownloader.CONFIG_CACHE_NAME,
                "https://assets.adobedtm.com/SampleAppId.json"
            )
        ).thenReturn(mockCacheResult)

        val expectedInitialConfig = mapOf<String, Any?>(
            "Key1" to "persisted_Key1Value",
            "Key2" to 0,
            "Key3" to false,
            "Key4" to "cached_Key4Value",
            "Key5" to 1,
            "Key6" to true
        )

        configurationStateManager.loadInitialConfig()

        assertNotNull(configurationStateManager.environmentAwareConfiguration)
        assertEquals(
            expectedInitialConfig,
            configurationStateManager.environmentAwareConfiguration
        )

        configurationStateManager.clearProgrammaticConfig()

        verify(mockNamedCollection).remove(ConfigurationStateManager.PERSISTED_OVERRIDDEN_CONFIG)
        val expectedConfigAfterClear = mapOf<String, Any?>(
            "Key4" to "cached_Key4Value",
            "Key5" to 1,
            "Key6" to true
        )

        assertEquals(
            expectedConfigAfterClear,
            configurationStateManager.environmentAwareConfiguration
        )
    }

    @Test
    fun `Clear programmatic config - programmatic config does not exist`() {
        // Setup with empty programmatic config and load initial config
        `when`(mockDataStoreService.getNamedCollection(ConfigurationExtension.DATASTORE_KEY)).thenReturn(
            mockNamedCollection
        )
        `when`(
            mockNamedCollection.getString(
                ConfigurationStateManager.PERSISTED_OVERRIDDEN_CONFIG,
                null
            )
        ).thenReturn(null)
        `when`(mockDeviceInfoService.getAsset(ConfigurationStateManager.CONFIG_BUNDLED_FILE_NAME)).thenReturn(
            mockBundledConfigJson.byteInputStream()
        )

        configurationStateManager =
            ConfigurationStateManager(mockAppIdManager, mockConfigurationDownloader)

        configurationStateManager.loadInitialConfig()

        assertNotNull(configurationStateManager.environmentAwareConfiguration)
        // Only contains bundled config
        assertEquals(
            mapOf("Key" to true),
            configurationStateManager.environmentAwareConfiguration
        )

        // Test clearing configuration
        configurationStateManager.clearProgrammaticConfig()

        // Verify
        verify(mockNamedCollection).remove(ConfigurationStateManager.PERSISTED_OVERRIDDEN_CONFIG)

        assertEquals(
            mapOf("Key" to true),
            configurationStateManager.environmentAwareConfiguration
        )
    }

    @Test
    fun `Replace configuration - retains programmatic config`() {
        // Setup to load programmatic config + cached config
        val mockCacheResult = mock(CacheResult::class.java)
        `when`(mockCacheResult.data).thenReturn(mockCachedConfigJson.byteInputStream())

        `when`(mockAppIdManager.loadAppId()).thenReturn("SampleAppId")
        `when`(
            mockCacheService.get(
                ConfigurationDownloader.CONFIG_CACHE_NAME,
                "https://assets.adobedtm.com/SampleAppId.json"
            )
        ).thenReturn(mockCacheResult)

        val expectedInitialConfig = mapOf<String, Any?>(
            "Key1" to "persisted_Key1Value",
            "Key2" to 0,
            "Key3" to false,
            "Key4" to "cached_Key4Value",
            "Key5" to 1,
            "Key6" to true
        )

        configurationStateManager.loadInitialConfig()

        assertNotNull(configurationStateManager.environmentAwareConfiguration)
        assertEquals(
            expectedInitialConfig,
            configurationStateManager.environmentAwareConfiguration
        )

        // Test config replacement
        val configReplacement = mapOf<String, Any?>(
            "Key1" to "replaced_Key1Value",
            "Key2" to 1,
            "Key3" to true,
            "Key4" to "replaced_Key4Value",
            "Key5" to 5,
            "Key6" to false,
            "Key7" to listOf(0, 1, 2, 3)
        )
        configurationStateManager.replaceConfiguration(configReplacement)

        // Verify that programmatic config is retained, non-programmatic config is overwritten and
        // new keys as added where applicable.
        val expectedConfigAfterReplacement = mapOf<String, Any?>(
            "Key1" to "persisted_Key1Value", // programmatic config is not overwritten
            "Key2" to 0, // programmatic config is not overwritten
            "Key3" to false, // programmatic config is not overwritten
            "Key4" to "replaced_Key4Value", // non - programmatic config is overwritten
            "Key5" to 5, // non -programmatic config is overwritten
            "Key6" to false, // non - programmatic config is overwritten
            "Key7" to listOf<Int>(0, 1, 2, 3) // new keys are added
        )

        assertEquals(
            expectedConfigAfterReplacement,
            configurationStateManager.environmentAwareConfiguration
        )
    }

    @Test
    fun `Replace configuration - when replacing config is empty`() {
        // Setup to load programmatic config + cached config
        val mockCacheResult = mock(CacheResult::class.java)
        `when`(mockCacheResult.data).thenReturn(mockCachedConfigJson.byteInputStream())

        `when`(mockAppIdManager.loadAppId()).thenReturn("SampleAppId")
        `when`(
            mockCacheService.get(
                ConfigurationDownloader.CONFIG_CACHE_NAME,
                "https://assets.adobedtm.com/SampleAppId.json"
            )
        ).thenReturn(mockCacheResult)

        val expectedInitialConfig = mapOf<String, Any?>(
            "Key1" to "persisted_Key1Value",
            "Key2" to 0,
            "Key3" to false,
            "Key4" to "cached_Key4Value",
            "Key5" to 1,
            "Key6" to true
        )

        configurationStateManager.loadInitialConfig()

        assertNotNull(configurationStateManager.environmentAwareConfiguration)
        assertEquals(
            expectedInitialConfig,
            configurationStateManager.environmentAwareConfiguration
        )

        // Test config replacement
        val configReplacement = emptyMap<String, Any?>()
        configurationStateManager.replaceConfiguration(configReplacement)

        // Verify that programmatic config is retained, non-programmatic config is overwritten and
        // new keys as added where applicable.
        val expectedConfigAfterReplacement = mapOf<String, Any?>(
            "Key1" to "persisted_Key1Value", // programmatic config is not overwritten
            "Key2" to 0, // programmatic config is not overwritten
            "Key3" to false // programmatic config is not overwritten
        )

        assertEquals(
            expectedConfigAfterReplacement,
            configurationStateManager.environmentAwareConfiguration
        )
    }

    @Test
    fun `Replace configuration - when config is null`() {
        // Setup to load programmatic config + cached config
        val mockCacheResult = mock(CacheResult::class.java)
        `when`(mockCacheResult.data).thenReturn(mockCachedConfigJson.byteInputStream())

        `when`(mockAppIdManager.loadAppId()).thenReturn("SampleAppId")
        `when`(
            mockCacheService.get(
                ConfigurationDownloader.CONFIG_CACHE_NAME,
                "https://assets.adobedtm.com/SampleAppId.json"
            )
        ).thenReturn(mockCacheResult)

        val expectedInitialConfig = mapOf<String, Any?>(
            "Key1" to "persisted_Key1Value",
            "Key2" to 0,
            "Key3" to false,
            "Key4" to "cached_Key4Value",
            "Key5" to 1,
            "Key6" to true
        )

        configurationStateManager.loadInitialConfig()

        assertNotNull(configurationStateManager.environmentAwareConfiguration)
        assertEquals(
            expectedInitialConfig,
            configurationStateManager.environmentAwareConfiguration
        )

        // Test config replacement
        configurationStateManager.replaceConfiguration(null)

        // Verify that programmatic config is retained, non-programmatic config is overwritten and
        // new keys as added where applicable.
        val expectedConfigAfterReplacement = mapOf<String, Any?>(
            "Key1" to "persisted_Key1Value", // programmatic config is not overwritten
            "Key2" to 0, // programmatic config is not overwritten
            "Key3" to false // programmatic config is not overwritten
        )

        assertEquals(
            expectedConfigAfterReplacement,
            configurationStateManager.environmentAwareConfiguration
        )
    }

    @Test
    fun `Compute dev environment aware config when environment specific key does not exist`() {
        val configurationUpdate = mapOf<String, Any?>(
            "build.environment" to "dev",
            "global.ssl" to true,
            "rules.url" to "https://assets.adobedtm.com/launch-rules.zip",
            "experienceCloud.org" to "B1F855165B4C9EA50A495E06@AdobeOrg",
            // prod & stage "campaign.pkey" but not dev
            "__stage__campaign.pkey" to "@BoOkfxbtfRRqALRp3rL7KOM5Xd2M4M-stage-campaignkey",
            "campaign.pkey" to "@BoOkfxbtfRRqALRp3rL7KOM5Xd2M4M-campaignkey"
        )

        configurationStateManager.updateProgrammaticConfig(configurationUpdate)

        val expectedEnvAwareConfig = mapOf<String, Any?>(
            "build.environment" to "dev",
            "global.ssl" to true,
            "rules.url" to "https://assets.adobedtm.com/launch-rules.zip",
            "experienceCloud.org" to "B1F855165B4C9EA50A495E06@AdobeOrg",
            "campaign.pkey" to "@BoOkfxbtfRRqALRp3rL7KOM5Xd2M4M-campaignkey",

            // Persisted programmatic config
            "Key1" to "persisted_Key1Value",
            "Key2" to 0,
            "Key3" to false
        )

        assertEquals(
            expectedEnvAwareConfig,
            configurationStateManager.environmentAwareConfiguration
        )
    }

    @Test
    fun `Compute dev environment aware config when environment specific key exists`() {
        val configurationUpdate = mapOf<String, Any?>(
            "build.environment" to "dev",
            "global.ssl" to true,
            "rules.url" to "https://assets.adobedtm.com/launch-rules.zip",
            "experienceCloud.org" to "B1F855165B4C9EA50A495E06@AdobeOrg",
            // prod & dev & stage "campaign.pkey" exist
            "__stage__campaign.pkey" to "@BoOkfxbtfRRqALRp3rL7KOM5Xd2M4M-stage-campaignkey",
            "campaign.pkey" to "@BoOkfxbtfRRqALRp3rL7KOM5Xd2M4M-campaignkey",
            "__dev__campaign.pkey" to "@BoOkfxbtfRRqALRp3rL7KOM5Xd2M4M-dev-campaignkey"
        )

        configurationStateManager.updateProgrammaticConfig(configurationUpdate)

        val expectedEnvAwareConfig = mapOf<String, Any?>(
            "build.environment" to "dev",
            "global.ssl" to true,
            "rules.url" to "https://assets.adobedtm.com/launch-rules.zip",
            "experienceCloud.org" to "B1F855165B4C9EA50A495E06@AdobeOrg",
            "campaign.pkey" to "@BoOkfxbtfRRqALRp3rL7KOM5Xd2M4M-dev-campaignkey",

            // Persisted programmatic config
            "Key1" to "persisted_Key1Value",
            "Key2" to 0,
            "Key3" to false
        )

        assertEquals(
            expectedEnvAwareConfig,
            configurationStateManager.environmentAwareConfiguration
        )
    }

    @Test
    fun `Compute stage environment aware config when environment specific key exists`() {
        val configurationUpdate = mapOf<String, Any?>(
            "build.environment" to "stage",
            "global.ssl" to true,
            "rules.url" to "https://assets.adobedtm.com/launch-rules.zip",
            "experienceCloud.org" to "B1F855165B4C9EA50A495E06@AdobeOrg",
            // prod & dev & stage "campaign.pkey" exist
            "__stage__campaign.pkey" to "@BoOkfxbtfRRqALRp3rL7KOM5Xd2M4M-stage-campaignkey",
            "campaign.pkey" to "@BoOkfxbtfRRqALRp3rL7KOM5Xd2M4M-campaignkey",
            "__dev__campaign.pkey" to "@BoOkfxbtfRRqALRp3rL7KOM5Xd2M4M-dev-campaignkey"
        )

        configurationStateManager.updateProgrammaticConfig(configurationUpdate)

        val expectedEnvAwareConfig = mapOf<String, Any?>(
            "build.environment" to "stage",
            "global.ssl" to true,
            "rules.url" to "https://assets.adobedtm.com/launch-rules.zip",
            "experienceCloud.org" to "B1F855165B4C9EA50A495E06@AdobeOrg",
            "campaign.pkey" to "@BoOkfxbtfRRqALRp3rL7KOM5Xd2M4M-stage-campaignkey",

            // Persisted programmatic config
            "Key1" to "persisted_Key1Value",
            "Key2" to 0,
            "Key3" to false
        )

        assertEquals(
            expectedEnvAwareConfig,
            configurationStateManager.environmentAwareConfiguration
        )
    }

    @Test
    fun `Compute stage environment aware config when environment specific key does not exist`() {
        val configurationUpdate = mapOf<String, Any?>(
            "build.environment" to "stage",
            "global.ssl" to true,
            "rules.url" to "https://assets.adobedtm.com/launch-rules.zip",
            "experienceCloud.org" to "B1F855165B4C9EA50A495E06@AdobeOrg",
            // prod & dev "campaign.pkey" but not stage
            "__dev__campaign.pkey" to "@BoOkfxbtfRRqALRp3rL7KOM5Xd2M4M-dev-campaignkey",
            "campaign.pkey" to "@BoOkfxbtfRRqALRp3rL7KOM5Xd2M4M-campaignkey"
        )

        configurationStateManager.updateProgrammaticConfig(configurationUpdate)

        val expectedEnvAwareConfig = mapOf<String, Any?>(
            "build.environment" to "stage",
            "global.ssl" to true,
            "rules.url" to "https://assets.adobedtm.com/launch-rules.zip",
            "experienceCloud.org" to "B1F855165B4C9EA50A495E06@AdobeOrg",
            "campaign.pkey" to "@BoOkfxbtfRRqALRp3rL7KOM5Xd2M4M-campaignkey",

            // Persisted programmatic config
            "Key1" to "persisted_Key1Value",
            "Key2" to 0,
            "Key3" to false
        )

        assertEquals(
            expectedEnvAwareConfig,
            configurationStateManager.environmentAwareConfiguration
        )
    }

    @Test
    fun `Compute prod environment aware config when environment overrides exist`() {
        val configurationUpdate = mapOf<String, Any?>(
            "build.environment" to "prod",
            "global.ssl" to true,
            "rules.url" to "https://assets.adobedtm.com/launch-rules.zip",
            "experienceCloud.org" to "B1F855165B4C9EA50A495E06@AdobeOrg",
            // prod & dev & stage "campaign.pkey" exist
            "__stage__campaign.pkey" to "@BoOkfxbtfRRqALRp3rL7KOM5Xd2M4M-stage-campaignkey",
            "campaign.pkey" to "@BoOkfxbtfRRqALRp3rL7KOM5Xd2M4M-campaignkey",
            "__dev__campaign.pkey" to "@BoOkfxbtfRRqALRp3rL7KOM5Xd2M4M-dev-campaignkey"
        )

        configurationStateManager.updateProgrammaticConfig(configurationUpdate)

        val expectedEnvAwareConfig = mapOf<String, Any?>(
            "build.environment" to "prod",
            "global.ssl" to true,
            "rules.url" to "https://assets.adobedtm.com/launch-rules.zip",
            "experienceCloud.org" to "B1F855165B4C9EA50A495E06@AdobeOrg",
            "campaign.pkey" to "@BoOkfxbtfRRqALRp3rL7KOM5Xd2M4M-campaignkey",

            // Persisted programmatic config
            "Key1" to "persisted_Key1Value",
            "Key2" to 0,
            "Key3" to false
        )

        assertNotNull(configurationStateManager.environmentAwareConfiguration)
        assertEquals(
            expectedEnvAwareConfig,
            configurationStateManager.environmentAwareConfiguration
        )
    }

    @After
    fun teardown() {
        mockFileUtils.close()
        mockedStaticServiceProvider.close()
    }
}
