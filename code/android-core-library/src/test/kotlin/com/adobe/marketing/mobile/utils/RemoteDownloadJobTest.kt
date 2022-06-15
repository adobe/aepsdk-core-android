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

package com.adobe.marketing.mobile.utils

import com.adobe.marketing.mobile.internal.utility.FileUtils
import com.adobe.marketing.mobile.internal.utility.TimeUtil
import com.adobe.marketing.mobile.services.CacheFileService
import com.adobe.marketing.mobile.services.HttpConnecting
import com.adobe.marketing.mobile.services.HttpMethod
import com.adobe.marketing.mobile.services.NetworkCallback
import com.adobe.marketing.mobile.services.NetworkRequest
import com.adobe.marketing.mobile.services.Networking
import com.adobe.marketing.mobile.utils.RemoteDownloader.MetadataProvider
import com.adobe.marketing.mobile.utils.RemoteDownloader.MetadataProvider.MetadataKeys.HTTP_HEADER_IF_MODIFIED_SINCE
import com.adobe.marketing.mobile.utils.RemoteDownloader.MetadataProvider.MetadataKeys.HTTP_HEADER_IF_RANGE
import com.adobe.marketing.mobile.utils.RemoteDownloader.MetadataProvider.MetadataKeys.HTTP_HEADER_RANGE
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyBoolean
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import java.io.File
import java.net.HttpURLConnection
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

@RunWith(PowerMockRunner::class)
@PrepareForTest(FileUtils::class)
class RemoteDownloadJobTest {

    @Mock
    private lateinit var mockCacheFileService: CacheFileService

    @Mock
    private lateinit var mockNetworkService: Networking

    @Mock
    private lateinit var mockMetadataProvider: MetadataProvider

    @Mock
    private lateinit var mockHttpConnecting: HttpConnecting

    @Mock
    private lateinit var mockCachedFile: File

    @Mock
    private lateinit var mockCompletedFile: File

    private lateinit var remoteDownloadJob: RemoteDownloadJob

    private var mockCallBack = mock<(File?) -> Unit>()

    companion object {
        const val VALID_URL = "http://assets.adobe.com/1234"
        const val INVALID_URL = "__/1234"
        const val VALID_DIRECTORY = "downloaded/rules/folder"
        const val SAMPLE_RESPONSE_BODY = "{\"SomeKey\": \"SomeValue\"}"
        const val LAST_MODIFIED = "Mon, 11 Jul 2022 22:15:22 GMT"
        const val ETAG = "W/\"3a2-bMnM1spT5zNBH3xgDTaqZQ\""
    }

    private val lastModifiedEpoch = TimeUtil.parseRFC2822Date(LAST_MODIFIED, TimeZone.getTimeZone("GMT"), Locale.US)?.time.toString()

    @Before
    fun setUp() {
        `when`(mockNetworkService.connectAsync(any(), any())).doAnswer { arguments ->
            val callback = arguments.getArgument<NetworkCallback>(1)
            callback.call(mockHttpConnecting)
        }
    }

    @Test
    fun `Download when url is invalid`() {
        remoteDownloadJob = setupRemoteDownloader(INVALID_URL, VALID_DIRECTORY)

        remoteDownloadJob.download(mockCallBack)

        verify(mockCallBack).invoke(null)
        verify(mockCacheFileService, never()).getCacheFile(VALID_URL, VALID_DIRECTORY, false)
        verify(mockNetworkService, never()).connectAsync(
            Mockito.any(NetworkRequest::class.java), Mockito.any(NetworkCallback::class.java)
        )
    }

    @Test
    fun `Download with no conditional fetch params`() {
        val expectedNetworkRequest = NetworkRequest(
            VALID_URL,
            HttpMethod.GET,
            null,
            emptyMap(),
            RemoteDownloadJob.DEFAULT_CONNECTION_TIMEOUT_MS,
            RemoteDownloadJob.DEFAULT_READ_TIMEOUT_MS
        )

        `when`(mockCacheFileService.getCacheFile(VALID_URL, VALID_DIRECTORY, false)).thenReturn(
            null
        )

        remoteDownloadJob = setupRemoteDownloader(VALID_URL, VALID_DIRECTORY)

        remoteDownloadJob.download(mockCallBack)
        verify(mockCacheFileService).getCacheFile(VALID_URL, VALID_DIRECTORY, false)
        val networkRequestCaptor: KArgumentCaptor<NetworkRequest> = argumentCaptor()
        verify(mockNetworkService).connectAsync(
            networkRequestCaptor.capture(),
            any()
        )

        val capturedNetworkRequest = networkRequestCaptor.firstValue
        verifyNetworkRequestParams(expectedNetworkRequest, capturedNetworkRequest)
    }

    @Test
    fun `Download with conditional fetch parameters`() {
        val cacheFileLastModified = Date(Date().time - TimeUnit.HOURS.toMillis(2)).time
        val rfc2822LastModifiedDate =
            TimeUtil.getRFC2822Date(cacheFileLastModified, TimeZone.getTimeZone("GMT"), Locale.US)
        val expectedHeaders = mapOf<String, String?>(
            HTTP_HEADER_IF_RANGE to rfc2822LastModifiedDate,
            HTTP_HEADER_IF_MODIFIED_SINCE to rfc2822LastModifiedDate,
            HTTP_HEADER_RANGE to String.format("bytes=%d-", 50)
        )
        val expectedNetworkRequest = NetworkRequest(
            VALID_URL,
            HttpMethod.GET,
            null,
            expectedHeaders,
            RemoteDownloadJob.DEFAULT_CONNECTION_TIMEOUT_MS,
            RemoteDownloadJob.DEFAULT_READ_TIMEOUT_MS
        )

        `when`(mockCacheFileService.getCacheFile(VALID_URL, VALID_DIRECTORY, false)).thenReturn(
            mockCachedFile
        )

        `when`(mockMetadataProvider.getMetadata(mockCachedFile)).thenReturn(
            mapOf(
                HTTP_HEADER_IF_MODIFIED_SINCE to rfc2822LastModifiedDate,
                HTTP_HEADER_RANGE to String.format("bytes=%d-", 50),
                HTTP_HEADER_IF_RANGE to rfc2822LastModifiedDate
            )
        )

        remoteDownloadJob = setupRemoteDownloader(VALID_URL, VALID_DIRECTORY)

        remoteDownloadJob.download(mockCallBack)
        verify(mockCacheFileService).getCacheFile(VALID_URL, VALID_DIRECTORY, false)
        val networkRequestCaptor: KArgumentCaptor<NetworkRequest> = argumentCaptor()
        verify(mockNetworkService).connectAsync(
            networkRequestCaptor.capture(),
            any()
        )

        val capturedNetworkRequest = networkRequestCaptor.firstValue
        verifyNetworkRequestParams(expectedNetworkRequest, capturedNetworkRequest)
    }

    @Test
    fun `Download when response is HTTP_NOT_FOUND`() {
        `when`(mockCacheFileService.getCacheFile(VALID_URL, VALID_DIRECTORY, false)).thenReturn(
            null
        )
        `when`(mockHttpConnecting.responseCode).thenReturn(HttpURLConnection.HTTP_NOT_FOUND)

        remoteDownloadJob = setupRemoteDownloader(VALID_URL, VALID_DIRECTORY)

        remoteDownloadJob.download(mockCallBack)
        verify(mockCacheFileService).getCacheFile(VALID_URL, VALID_DIRECTORY, false)
        verify(mockNetworkService).connectAsync(any(), any())
        verify(mockCallBack).invoke(null)
    }

    @Test
    fun `Download when response is HTTP_OK and no cache file exists`() {
        `when`(mockCacheFileService.getCacheFile(VALID_URL, VALID_DIRECTORY, false)).thenReturn(null)
        setupConnectionResponseMock(HttpURLConnection.HTTP_OK, LAST_MODIFIED, ETAG, SAMPLE_RESPONSE_BODY)
        `when`(mockCacheFileService.createCacheFile(any(), any(), any())).thenReturn(
            mockCachedFile
        )
        PowerMockito.mockStatic(FileUtils::class.java)
        `when`(FileUtils.readInputStreamIntoFile(any(), any(), anyBoolean())).thenReturn(true)
        `when`(mockCacheFileService.markComplete(any())).thenReturn(mockCompletedFile)

        remoteDownloadJob = setupRemoteDownloader(VALID_URL, VALID_DIRECTORY)

        remoteDownloadJob.download(mockCallBack)

        val networkRequestCaptor: KArgumentCaptor<NetworkRequest> = argumentCaptor()
        verify(mockNetworkService).connectAsync(networkRequestCaptor.capture(), any())

        verify(mockCacheFileService).getCacheFile(VALID_URL, VALID_DIRECTORY, false)
        verify(mockCacheFileService).deleteCacheFile(VALID_URL, VALID_DIRECTORY)
        val expectedMetadata = mapOf<String, String>(
            CacheFileService.METADATA_KEY_ETAG to ETAG,
            CacheFileService.METADATA_KEY_LAST_MODIFIED_EPOCH to lastModifiedEpoch
        )

        verify(mockCacheFileService).createCacheFile(VALID_URL, expectedMetadata, VALID_DIRECTORY)

        verify(mockCacheFileService).markComplete(mockCachedFile)
        verify(mockCallBack).invoke(mockCompletedFile)
    }

    @Test
    fun `Download when result is HTTP_OK and cache file fails to save`() {
        `when`(mockCacheFileService.getCacheFile(VALID_URL, VALID_DIRECTORY, false)).thenReturn(null)
        setupConnectionResponseMock(HttpURLConnection.HTTP_OK, LAST_MODIFIED, ETAG, SAMPLE_RESPONSE_BODY)
        `when`(mockCacheFileService.createCacheFile(any(), any(), any())).thenReturn(
            mockCachedFile
        )
        PowerMockito.mockStatic(FileUtils::class.java)
        `when`(FileUtils.readInputStreamIntoFile(any(), any(), anyBoolean())).thenReturn(true)
        // Simulate file fail to save
        `when`(mockCacheFileService.markComplete(mockCachedFile)).thenReturn(null)

        remoteDownloadJob = setupRemoteDownloader(VALID_URL, VALID_DIRECTORY)

        remoteDownloadJob.download(mockCallBack)

        verify(mockCacheFileService).getCacheFile(VALID_URL, VALID_DIRECTORY, false)
        verify(mockCacheFileService).deleteCacheFile(VALID_URL, VALID_DIRECTORY)
        verify(mockCacheFileService).createCacheFile(
            VALID_URL,
            mapOf(
                CacheFileService.METADATA_KEY_ETAG to ETAG,
                CacheFileService.METADATA_KEY_LAST_MODIFIED_EPOCH to lastModifiedEpoch
            ),
            VALID_DIRECTORY
        )
        val networkRequestCaptor: KArgumentCaptor<NetworkRequest> = argumentCaptor()
        verify(mockNetworkService).connectAsync(networkRequestCaptor.capture(), any())
        verify(mockCacheFileService).markComplete(mockCachedFile)

        verify(mockCallBack).invoke(null)
    }

    @Test
    fun `Download when result is HTTP_OK and input stream fails to copy`() {
        `when`(mockCacheFileService.getCacheFile(VALID_URL, VALID_DIRECTORY, false)).thenReturn(null)
        `when`(mockCacheFileService.createCacheFile(any(), any(), any())).thenReturn(
            mockCachedFile
        )
        setupConnectionResponseMock(HttpURLConnection.HTTP_OK, LAST_MODIFIED, ETAG, SAMPLE_RESPONSE_BODY)
        PowerMockito.mockStatic(FileUtils::class.java)
        `when`(FileUtils.readInputStreamIntoFile(any(), any(), anyBoolean())).thenReturn(false)

        remoteDownloadJob = setupRemoteDownloader(VALID_URL, VALID_DIRECTORY)

        remoteDownloadJob.download(mockCallBack)

        verify(mockCacheFileService).getCacheFile(VALID_URL, VALID_DIRECTORY, false)
        verify(mockCacheFileService).deleteCacheFile(VALID_URL, VALID_DIRECTORY)
        verify(mockCacheFileService).createCacheFile(
            VALID_URL,
            mapOf(
                CacheFileService.METADATA_KEY_ETAG to ETAG,
                CacheFileService.METADATA_KEY_LAST_MODIFIED_EPOCH to lastModifiedEpoch
            ),
            VALID_DIRECTORY
        )
        val networkRequestCaptor: KArgumentCaptor<NetworkRequest> = argumentCaptor()
        verify(mockNetworkService).connectAsync(networkRequestCaptor.capture(), any())
        verify(mockCacheFileService, never()).markComplete(mockCachedFile)

        verify(mockCallBack).invoke(null)
    }

    @Test
    fun `Download when result is HTTP_OK and cache file already exists`() {
        `when`(mockCacheFileService.getCacheFile(VALID_URL, VALID_DIRECTORY, false)).thenReturn(
            mockCachedFile
        )

        setupConnectionResponseMock(HttpURLConnection.HTTP_OK, LAST_MODIFIED, ETAG, SAMPLE_RESPONSE_BODY)
        `when`(mockCacheFileService.createCacheFile(any(), any(), any())).thenReturn(
            mockCachedFile
        )
        PowerMockito.mockStatic(FileUtils::class.java)
        `when`(FileUtils.readInputStreamIntoFile(any(), any(), anyBoolean())).thenReturn(true)
        `when`(mockCacheFileService.markComplete(mockCachedFile)).thenReturn(mockCompletedFile)

        remoteDownloadJob = setupRemoteDownloader(VALID_URL, VALID_DIRECTORY)

        remoteDownloadJob.download(mockCallBack)

        verify(mockCacheFileService).getCacheFile(VALID_URL, VALID_DIRECTORY, false)
        // Verify that For HTTP OK we always delete the existing cache file and create a new one
        verify(mockCacheFileService).deleteCacheFile(VALID_URL, VALID_DIRECTORY)
        verify(mockCacheFileService).createCacheFile(
            VALID_URL,
            mapOf(
                CacheFileService.METADATA_KEY_ETAG to ETAG,
                CacheFileService.METADATA_KEY_LAST_MODIFIED_EPOCH to lastModifiedEpoch
            ),
            VALID_DIRECTORY
        )

        val networkRequestCaptor: KArgumentCaptor<NetworkRequest> = argumentCaptor()
        verify(mockNetworkService).connectAsync(networkRequestCaptor.capture(), any())
        verify(mockCacheFileService).markComplete(mockCachedFile)

        verify(mockCallBack).invoke(mockCompletedFile)
    }

    @Test
    fun `Download when result is HTTP_PARTIAL and cache file already exists`() {
        `when`(mockCacheFileService.getCacheFile(VALID_URL, VALID_DIRECTORY, false)).thenReturn(
            mockCachedFile
        )
        setupConnectionResponseMock(HttpURLConnection.HTTP_PARTIAL, LAST_MODIFIED, ETAG, SAMPLE_RESPONSE_BODY)
        `when`(mockCacheFileService.createCacheFile(any(), any(), any())).thenReturn(
            mockCachedFile
        )
        PowerMockito.mockStatic(FileUtils::class.java)
        `when`(FileUtils.readInputStreamIntoFile(any(), any(), anyBoolean())).thenReturn(true)
        `when`(mockCacheFileService.markComplete(mockCachedFile)).thenReturn(mockCompletedFile)

        remoteDownloadJob = setupRemoteDownloader(VALID_URL, VALID_DIRECTORY)

        remoteDownloadJob.download(mockCallBack)

        verify(mockCacheFileService).getCacheFile(VALID_URL, VALID_DIRECTORY, false)
        verify(mockCacheFileService, never()).deleteCacheFile(eq(VALID_URL), eq(VALID_DIRECTORY))
        verify(mockCacheFileService, never()).createCacheFile(
            VALID_URL,
            mapOf(
                CacheFileService.METADATA_KEY_ETAG to ETAG,
                CacheFileService.METADATA_KEY_LAST_MODIFIED_EPOCH to lastModifiedEpoch
            ),
            VALID_DIRECTORY
        )

        val networkRequestCaptor: KArgumentCaptor<NetworkRequest> = argumentCaptor()
        verify(mockNetworkService).connectAsync(networkRequestCaptor.capture(), any())
        verify(mockCacheFileService).markComplete(mockCachedFile)

        verify(mockCallBack).invoke(mockCompletedFile)
    }

    @Test
    fun `Download when result is HTTP_PARTIAL and cache does not exist`() {
        `when`(mockCacheFileService.getCacheFile(VALID_URL, VALID_DIRECTORY, false)).thenReturn(null)
        setupConnectionResponseMock(HttpURLConnection.HTTP_PARTIAL, LAST_MODIFIED, ETAG, SAMPLE_RESPONSE_BODY)
        `when`(mockCacheFileService.createCacheFile(any(), any(), any())).thenReturn(
            mockCachedFile
        )
        PowerMockito.mockStatic(FileUtils::class.java)
        `when`(FileUtils.readInputStreamIntoFile(any(), any(), anyBoolean())).thenReturn(true)
        `when`(mockCacheFileService.markComplete(any())).thenReturn(mockCompletedFile)

        remoteDownloadJob = setupRemoteDownloader(VALID_URL, VALID_DIRECTORY)

        remoteDownloadJob.download(mockCallBack)

        verify(mockCacheFileService).getCacheFile(VALID_URL, VALID_DIRECTORY, false)

        // Verify that For HTTP PARTIAL with no cache file we always safe-delete the existing cache file
        // and create a new one
        verify(mockCacheFileService).deleteCacheFile(VALID_URL, VALID_DIRECTORY)
        verify(mockCacheFileService).createCacheFile(
            VALID_URL,
            mapOf(
                CacheFileService.METADATA_KEY_ETAG to ETAG,
                CacheFileService.METADATA_KEY_LAST_MODIFIED_EPOCH to lastModifiedEpoch
            ),
            VALID_DIRECTORY
        )

        val networkRequestCaptor: KArgumentCaptor<NetworkRequest> = argumentCaptor()
        verify(mockNetworkService).connectAsync(networkRequestCaptor.capture(), any())
        verify(mockCacheFileService).markComplete(mockCachedFile)

        verify(mockCallBack).invoke(mockCompletedFile)
    }

    private fun setupRemoteDownloader(url: String, directory: String): RemoteDownloadJob {
        return RemoteDownloadJob(
            mockNetworkService,
            mockCacheFileService,
            url,
            directory,
            mockMetadataProvider
        )
    }

    private fun verifyNetworkRequestParams(
        expectedNetworkRequest: NetworkRequest,
        actualNetworkRequest: NetworkRequest
    ) {
        assertEquals(expectedNetworkRequest.url, actualNetworkRequest.url)
        assertEquals(expectedNetworkRequest.method, actualNetworkRequest.method)
        assertEquals(expectedNetworkRequest.body, actualNetworkRequest.body)
        assertEquals(expectedNetworkRequest.connectTimeout, actualNetworkRequest.connectTimeout)
        assertEquals(expectedNetworkRequest.connectTimeout, actualNetworkRequest.connectTimeout)
        assertEquals(expectedNetworkRequest.readTimeout, actualNetworkRequest.readTimeout)
        assertEquals(expectedNetworkRequest.headers, actualNetworkRequest.headers)
    }

    private fun setupConnectionResponseMock(responseCode: Int, lastModified: String, eTag: String, responseBody: String?) {
        `when`(mockHttpConnecting.responseCode).thenReturn(responseCode)
        `when`(mockHttpConnecting.getResponsePropertyValue("Last-Modified")).thenReturn(lastModified)
        `when`(mockHttpConnecting.getResponsePropertyValue("ETag")).thenReturn(eTag)
        `when`(mockHttpConnecting.inputStream).thenReturn(responseBody?.byteInputStream())
    }
}
