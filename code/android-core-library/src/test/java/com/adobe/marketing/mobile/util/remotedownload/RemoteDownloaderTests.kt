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

package com.adobe.marketing.mobile.util.remotedownload

import com.adobe.marketing.mobile.services.CacheFileService
import com.adobe.marketing.mobile.services.Networking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@RunWith(MockitoJUnitRunner.Silent::class)
class RemoteDownloaderTests {

    @Mock
    private lateinit var mockNetworking: Networking

    @Mock
    private lateinit var mockCacheFileService: CacheFileService

    @Mock
    private lateinit var mockMetadataProvider: MetadataProvider

    @Mock
    private lateinit var mockCompletionCallback: (DownloadResult) -> Unit

    @Mock
    private lateinit var mockRemoteDownloadJob: RemoteDownloadJob

    private var mockDownloadJobSupplier: (Networking, CacheFileService, url: String, downloadDirectory: String?, MetadataProvider) -> RemoteDownloadJob =
        { _, _, _, _, _ -> mockRemoteDownloadJob }

    private lateinit var remoteDownloader: RemoteDownloader

    companion object {
        const val VALID_URL = "http://assets.adobe.com/1234"
        const val INVALID_URL = "__/1234"
        const val VALID_DIRECTORY = "downloaded/rules/folder"
    }

    @Before
    fun setUp() {
        remoteDownloader = RemoteDownloader(mockNetworking, mockCacheFileService)

        val field = RemoteDownloader::class.java.getDeclaredField("downloadJobSupplier")
        field.isAccessible = true
        field.set(remoteDownloader, mockDownloadJobSupplier)
    }

    @Test
    fun `Download with null cache dir`() {
        remoteDownloader.download(
            VALID_URL,
            null,
            mockMetadataProvider,
            mockCompletionCallback
        )
        verify(mockRemoteDownloadJob).download(mockCompletionCallback)
    }

    @Test
    fun `Download with valid url`() {
        remoteDownloader.download(
            VALID_URL,
            VALID_DIRECTORY,
            mockMetadataProvider,
            mockCompletionCallback
        )
        verify(mockRemoteDownloadJob).download(mockCompletionCallback)
    }

    @Test
    fun `Download with invalid url`() {
        remoteDownloader.download(
            INVALID_URL,
            VALID_DIRECTORY,
            mockMetadataProvider,
            mockCompletionCallback
        )

        val downloadResultCaptor: KArgumentCaptor<DownloadResult> = argumentCaptor()
        verify(mockCompletionCallback).invoke(downloadResultCaptor.capture())

        val capturedDownloadResult = downloadResultCaptor.firstValue
        Assert.assertNull(capturedDownloadResult.data)
        Assert.assertEquals(DownloadResult.Reason.INVALID_URL, capturedDownloadResult.reason)

        verify(mockRemoteDownloadJob, never()).download(mockCompletionCallback)
    }
}
