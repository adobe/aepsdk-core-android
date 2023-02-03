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

import com.adobe.marketing.mobile.services.HttpConnecting
import com.adobe.marketing.mobile.services.HttpMethod
import com.adobe.marketing.mobile.services.NetworkCallback
import com.adobe.marketing.mobile.services.NetworkRequest
import com.adobe.marketing.mobile.services.Networking
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.caching.CacheExpiry
import com.adobe.marketing.mobile.services.caching.CacheResult
import com.adobe.marketing.mobile.services.caching.CacheService
import com.adobe.marketing.mobile.util.TimeUtils
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.io.InputStream
import java.net.HttpURLConnection
import java.util.Locale
import java.util.TimeZone

@RunWith(MockitoJUnitRunner.Silent::class)
class ConfigurationDownloaderTest {

    companion object {
        const val SAMPLE_URL = "http://assets.adobe.com/1234"
        const val INVALID__URL = "someinvalid url"
    }

    @Mock
    private lateinit var mockNetworkService: Networking

    @Mock
    private lateinit var mockCacheService: CacheService

    @Mock
    private lateinit var mockCompletionCallback: (Map<String, Any?>?) -> Unit

    @Mock
    private lateinit var mockServiceProvider: ServiceProvider

    private lateinit var mockedStaticServiceProvider: MockedStatic<ServiceProvider>

    private lateinit var configurationDownloader: ConfigurationDownloader

    @Before
    fun setUp() {
        mockedStaticServiceProvider = Mockito.mockStatic(ServiceProvider::class.java)
        mockedStaticServiceProvider.`when`<Any> { ServiceProvider.getInstance() }.thenReturn(mockServiceProvider)
        `when`(mockServiceProvider.networkService).thenReturn(mockNetworkService)
        `when`(mockServiceProvider.cacheService).thenReturn(mockCacheService)

        configurationDownloader = ConfigurationDownloader()
    }

    @Test
    fun `Download never makes a network request for an invalid url`() {
        configurationDownloader.download(INVALID__URL, mockCompletionCallback)

        verify(mockCacheService, never()).get(ConfigurationDownloader.CONFIG_CACHE_NAME, SAMPLE_URL)
        verify(mockNetworkService, never()).connectAsync(any(), any())
        // verify that the original callback is invoked with right content
        verify(mockCompletionCallback).invoke(null)
    }

    @Test
    fun `Download always makes a network request for a valid url`() {
        val mockConfigJson = "{}"
        val simulatedResponse = simulateNetworkResponse(
            HttpURLConnection.HTTP_OK,
            mockConfigJson.byteInputStream(),
            mapOf()
        )

        `when`(mockNetworkService.connectAsync(any(), any())).then {
            val callback = it.getArgument<NetworkCallback>(1)

            callback.call(simulatedResponse)
        }

        configurationDownloader.download(SAMPLE_URL, mockCompletionCallback)

        verify(mockCacheService, times(1)).get(
            ConfigurationDownloader.CONFIG_CACHE_NAME,
            SAMPLE_URL
        )
        verify(mockNetworkService, times(1)).connectAsync(any(), any())
        // verify that the original callback is invoked with right content
        verify(mockCompletionCallback).invoke(emptyMap())
        verify(simulatedResponse).close()
    }

    @Test
    fun `Download when network response is null`() {
        `when`(mockNetworkService.connectAsync(any(), any())).then {
            val callback = it.getArgument<NetworkCallback>(1)

            callback.call(null)
        }

        configurationDownloader.download(SAMPLE_URL, mockCompletionCallback)

        verify(mockCacheService, times(1)).get(
            ConfigurationDownloader.CONFIG_CACHE_NAME,
            SAMPLE_URL
        )
        verify(mockNetworkService, times(1)).connectAsync(any(), any())

        // verify that the original callback is invoked with null content
        verify(mockCompletionCallback).invoke(null)
    }

    @Test
    fun `Download when RemoteDownload result is null`() {
        val simulatedResponse =
            simulateNetworkResponse(HttpURLConnection.HTTP_OK, null, mapOf())

        `when`(mockNetworkService.connectAsync(any(), any())).then {
            val callback = it.getArgument<NetworkCallback>(1)
            callback.call(simulatedResponse)
        }

        configurationDownloader.download(SAMPLE_URL, mockCompletionCallback)

        verify(mockCacheService).get(ConfigurationDownloader.CONFIG_CACHE_NAME, SAMPLE_URL)
        verify(mockNetworkService, times(1)).connectAsync(any(), any())
        verify(mockCompletionCallback).invoke(null)
        verify(simulatedResponse).close()
    }

    @Test
    fun `Download when download result file cannot be parsed`() {
        val mockConfigJson = "{invalid json}"
        val simulatedResponse = simulateNetworkResponse(
            HttpURLConnection.HTTP_OK,
            mockConfigJson.byteInputStream(),
            mapOf()
        )

        `when`(mockNetworkService.connectAsync(any(), any())).then {
            val callback = it.getArgument<NetworkCallback>(1)
            callback.call(simulatedResponse)
        }

        configurationDownloader.download(SAMPLE_URL, mockCompletionCallback)

        verify(mockCacheService).get(ConfigurationDownloader.CONFIG_CACHE_NAME, SAMPLE_URL)
        verify(mockNetworkService, times(1)).connectAsync(any(), any())
        verify(mockCompletionCallback).invoke(null)
        verify(simulatedResponse).close()
    }

    @Test
    fun `Download when download result is empty`() {
        val mockConfigJson = ""
        val simulatedResponse = simulateNetworkResponse(
            HttpURLConnection.HTTP_OK,
            mockConfigJson.byteInputStream(),
            mapOf()
        )

        `when`(mockNetworkService.connectAsync(any(), any())).then {
            val callback = it.getArgument<NetworkCallback>(1)

            callback.call(simulatedResponse)
        }

        configurationDownloader.download(SAMPLE_URL, mockCompletionCallback)

        verify(mockCacheService).get(ConfigurationDownloader.CONFIG_CACHE_NAME, SAMPLE_URL)
        verify(mockNetworkService, times(1)).connectAsync(any(), any())
        // verify that the original callback is invoked with right content
        verify(mockCompletionCallback).invoke(emptyMap())
        verify(simulatedResponse).close()
    }

    @Test
    fun `Download when RemoteDownload result file has valid config content`() {
        val mockConfigContentJson = "{" +
            "\"build.environment\":\"stage\",\n" +
            "\"global.ssl\": true,\n" +
            "\"rules.url\":\"https://assets.adobedtm.com/launch-rules.zip\",\n" +
            "\"experienceCloud.org\":\"B1F855165B4C9EA50A495E06@AdobeOrg\",\n" +
            "\"campaign.pkey\":\"@BoOkfxbtfRRqALRp3rL7KOM5Xd2M4M-campaignkey\"," +
            "\"timeout\": 30.5" +
            "}"

        val simulatedResponse = simulateNetworkResponse(
            HttpURLConnection.HTTP_OK,
            mockConfigContentJson.byteInputStream(),
            mapOf()
        )

        `when`(mockNetworkService.connectAsync(any(), any())).then {
            val callback = it.getArgument<NetworkCallback>(1)
            callback.call(simulatedResponse)
        }

        configurationDownloader.download(SAMPLE_URL, mockCompletionCallback)

        // verify that the original callback is invoked with right content
        val expectedConfig = mapOf<String, Any?>(
            "build.environment" to "stage",
            "global.ssl" to true,
            "rules.url" to "https://assets.adobedtm.com/launch-rules.zip",
            "experienceCloud.org" to "B1F855165B4C9EA50A495E06@AdobeOrg",
            "campaign.pkey" to "@BoOkfxbtfRRqALRp3rL7KOM5Xd2M4M-campaignkey",
            "timeout" to 30.5
        )

        verify(mockCacheService).get(ConfigurationDownloader.CONFIG_CACHE_NAME, SAMPLE_URL)
        verify(mockNetworkService, times(1)).connectAsync(any(), any())
        verify(mockCompletionCallback).invoke(expectedConfig)
        verify(simulatedResponse).close()
    }

    @Test
    fun `Download when RemoteDownload result file has malformed config content`() {
        val mockConfigContentJson = "{" +
            "\"build.environment\":\"stage\",\n" +
            "\"global.ssl\": true,\n" +
            "\"rules.url\"= \"https://assets.adobedtm.com/launch-rules.zip\"," + // This line has = instead of :
            "}"

        val simulatedResponse = simulateNetworkResponse(
            HttpURLConnection.HTTP_OK,
            mockConfigContentJson.byteInputStream(),
            mapOf()
        )

        `when`(mockNetworkService.connectAsync(any(), any())).then {
            val callback = it.getArgument<NetworkCallback>(1)

            callback.call(simulatedResponse)
        }

        configurationDownloader.download(SAMPLE_URL, mockCompletionCallback)

        verify(mockCacheService).get(ConfigurationDownloader.CONFIG_CACHE_NAME, SAMPLE_URL)
        verify(mockNetworkService, times(1)).connectAsync(any(), any())
        verify(mockCompletionCallback).invoke(null)
        verify(simulatedResponse).close()
    }

    @Test
    fun `Download when response is HTTP_NOT_MODIFIED`() {
        val simulatedResponse = simulateNetworkResponse(HttpURLConnection.HTTP_NOT_MODIFIED, null, mapOf())

        `when`(mockNetworkService.connectAsync(any(), any())).then {
            val callback = it.getArgument<NetworkCallback>(1)
            callback.call(simulatedResponse)
        }

        val cachedContentData = "{\"cachedKey\":\"value\"}"
        val mockCacheResult = Mockito.mock(CacheResult::class.java)
        `when`(mockCacheResult.data).thenReturn(cachedContentData.byteInputStream())
        `when`(mockCacheResult.expiry).thenReturn(CacheExpiry.never())
        val cacheMetadata = mapOf(
            ConfigurationDownloader.HTTP_HEADER_ETAG to "someETag",
            ConfigurationDownloader.HTTP_HEADER_LAST_MODIFIED to "500"
        )
        `when`(mockCacheResult.metadata).thenReturn(cacheMetadata)

        `when`(
            mockCacheService.get(
                ConfigurationDownloader.CONFIG_CACHE_NAME,
                SAMPLE_URL
            )
        ).thenReturn(mockCacheResult)

        // Test
        configurationDownloader.download(SAMPLE_URL, mockCompletionCallback)

        // Verify
        val networkRequestCaptor: KArgumentCaptor<NetworkRequest> = argumentCaptor()
        verify(mockNetworkService, times(1)).connectAsync(networkRequestCaptor.capture(), any())
        val expectedNetworkRequest = NetworkRequest(
            SAMPLE_URL,
            HttpMethod.GET,
            null,
            mapOf(
                ConfigurationDownloader.HTTP_HEADER_IF_NONE_MATCH to "someETag",
                ConfigurationDownloader.HTTP_HEADER_IF_MODIFIED_SINCE to TimeUtils.getRFC2822Date(
                    500L,
                    TimeZone.getTimeZone("GMT"),
                    Locale.US
                )
            ),
            10000,
            10000
        )
        verifyNetworkRequestParams(expectedNetworkRequest, networkRequestCaptor.firstValue)

        verify(mockCacheService, times(2)).get(
            ConfigurationDownloader.CONFIG_CACHE_NAME,
            SAMPLE_URL
        ) // Cache service should be invoked twice, once for initial headers and then to get the cached data.

        // verify that the original callback is invoked with right content
        val expectedConfig = mapOf("cachedKey" to "value")
        verify(mockCompletionCallback).invoke(expectedConfig)
        verify(simulatedResponse).close()
    }

    @After
    fun teardown() {
        mockedStaticServiceProvider.close()
    }

    private fun verifyNetworkRequestParams(
        expectedNetworkRequest: NetworkRequest,
        actualNetworkRequest: NetworkRequest
    ) {
        Assert.assertEquals(expectedNetworkRequest.url, actualNetworkRequest.url)
        Assert.assertEquals(expectedNetworkRequest.method, actualNetworkRequest.method)
        Assert.assertEquals(expectedNetworkRequest.body, actualNetworkRequest.body)
        Assert.assertEquals(expectedNetworkRequest.connectTimeout, actualNetworkRequest.connectTimeout)
        Assert.assertEquals(expectedNetworkRequest.readTimeout, actualNetworkRequest.readTimeout)
        Assert.assertEquals(expectedNetworkRequest.headers, actualNetworkRequest.headers)
    }

    private fun simulateNetworkResponse(
        responseCode: Int,
        responseStream: InputStream?,
        metadata: Map<String, String>
    ): HttpConnecting {
        val mockResponse = Mockito.mock(HttpConnecting::class.java)
        `when`(mockResponse.responseCode).thenReturn(responseCode)
        `when`(mockResponse.inputStream).thenReturn(responseStream)
        `when`(mockResponse.getResponsePropertyValue(any())).then {
            return@then metadata[it.getArgument(0)]
        }
        return mockResponse
    }
}
