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

import com.adobe.marketing.mobile.internal.utility.FileUtils
import com.adobe.marketing.mobile.services.CacheFileService
import com.adobe.marketing.mobile.utils.RemoteDownloader
import com.adobe.marketing.mobile.utils.RemoteDownloader.Reason
import com.adobe.marketing.mobile.utils.RemoteDownloader.RemoteDownloadResult
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.times
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import java.io.File
import java.util.Date
import kotlin.test.assertEquals

@RunWith(MockitoJUnitRunner.Silent::class)
class ConfigurationRulesDownloaderTest {
    @Mock
    private lateinit var mockCacheFileService: CacheFileService

    @Mock
    private lateinit var mockRemoteDownloader: RemoteDownloader

    @Mock
    private lateinit var mockMetadataProvider: ZipFileMetadataProvider

    @Mock
    private lateinit var mockCompletionCallback: (File?) -> Unit

    @Mock
    private lateinit var mockDownloadedFile: File

    private lateinit var mockFileUtils: MockedStatic<FileUtils>

    private lateinit var configurationRulesDownloader: ConfigurationRulesDownloader

    companion object {
        const val SAMPLE_URL = "http://assets.adobe.com/1234"
        const val SAMPLE_DIRECTORY = "downloaded/rules/folder"
        const val VALID_OUTPUT_PATH = "downloaded/rules/folder/rules"
    }

    @Before
    fun setUp() {
        mockFileUtils = mockStatic(FileUtils::class.java)

        configurationRulesDownloader =
            ConfigurationRulesDownloader(mockCacheFileService, mockMetadataProvider, mockRemoteDownloader)
    }

    @Test
    fun `Process rules when downloaded zip is null`() {
        val callbackCaptor: KArgumentCaptor<(RemoteDownloadResult) -> Unit> = argumentCaptor()

        configurationRulesDownloader.download(SAMPLE_URL, SAMPLE_DIRECTORY, mockCompletionCallback)
        verify(mockRemoteDownloader).download(
            eq(SAMPLE_URL),
            eq(SAMPLE_DIRECTORY),
            eq(mockMetadataProvider),
            callbackCaptor.capture()
        )

        // Simulate null zip file
        callbackCaptor.firstValue.invoke(RemoteDownloadResult(null, Reason.NO_DATA))

        // Verify that the completion callback is invoked with null
        verify(mockCompletionCallback).invoke(null)
    }

    @Test
    fun `Process rules when downloaded zip is a directory`() {
        `when`(mockDownloadedFile.isDirectory).thenReturn(true)

        configurationRulesDownloader.download(SAMPLE_URL, SAMPLE_DIRECTORY, mockCompletionCallback)
        val callbackCaptor: KArgumentCaptor<(RemoteDownloadResult) -> Unit> = argumentCaptor()
        verify(mockRemoteDownloader).download(
            eq(SAMPLE_URL),
            eq(SAMPLE_DIRECTORY),
            eq(mockMetadataProvider),
            callbackCaptor.capture()
        )

        callbackCaptor.firstValue.invoke(RemoteDownloadResult(mockDownloadedFile, Reason.SUCCESS))

        verify(mockMetadataProvider, never()).createMetadata(any(), any(), any())
        verify(mockCacheFileService, never()).getBaseFilePath(SAMPLE_URL, SAMPLE_DIRECTORY)
        // Verify that the completion callback is invoked with the downloaded file itself
        verify(mockCompletionCallback).invoke(mockDownloadedFile)
    }

    @Test
    fun `Process rules zip when cache directory open or make for URL fails`() {
        `when`(mockDownloadedFile.isDirectory).thenReturn(false)
        `when`(mockCacheFileService.getBaseFilePath(SAMPLE_URL, SAMPLE_DIRECTORY)).thenReturn(null)

        configurationRulesDownloader.download(SAMPLE_URL, SAMPLE_DIRECTORY, mockCompletionCallback)
        val callbackCaptor: KArgumentCaptor<(RemoteDownloadResult) -> Unit> = argumentCaptor()
        verify(mockRemoteDownloader).download(
            eq(SAMPLE_URL),
            eq(SAMPLE_DIRECTORY),
            eq(mockMetadataProvider),
            callbackCaptor.capture()
        )

        callbackCaptor.firstValue.invoke(RemoteDownloadResult(mockDownloadedFile, Reason.SUCCESS))

        verify(mockCacheFileService, times(1)).getBaseFilePath(SAMPLE_URL, SAMPLE_DIRECTORY)
        verify(mockMetadataProvider, never()).createMetadata(any(), any(), any())
        verify(mockCompletionCallback).invoke(null)
    }

    @Test
    fun `Process rules zip when downloaded zip extraction fails`() {
        `when`(mockDownloadedFile.isDirectory).thenReturn(false)
        `when`(mockCacheFileService.getBaseFilePath(SAMPLE_URL, SAMPLE_DIRECTORY)).thenReturn(
            VALID_OUTPUT_PATH
        )
        mockFileUtils.`when`<Any> { FileUtils.extractFromZip(mockDownloadedFile, VALID_OUTPUT_PATH) }.thenReturn(false)

        configurationRulesDownloader.download(SAMPLE_URL, SAMPLE_DIRECTORY, mockCompletionCallback)
        val callbackCaptor: KArgumentCaptor<(RemoteDownloadResult) -> Unit> = argumentCaptor()
        verify(mockRemoteDownloader).download(
            eq(SAMPLE_URL),
            eq(SAMPLE_DIRECTORY),
            eq(mockMetadataProvider),
            callbackCaptor.capture()
        )

        callbackCaptor.firstValue.invoke(RemoteDownloadResult(mockDownloadedFile, Reason.SUCCESS))

        verify(mockCacheFileService, times(1)).getBaseFilePath(SAMPLE_URL, SAMPLE_DIRECTORY)
        verify(mockMetadataProvider, never()).createMetadata(any(), any(), any())
        verify(mockCompletionCallback).invoke(null)
        verify(mockDownloadedFile).delete()
    }

    @Test
    fun `Process rules zip when downloaded zip extraction succeeds`() {
        `when`(mockDownloadedFile.isDirectory).thenReturn(false)
        `when`(mockCacheFileService.getBaseFilePath(SAMPLE_URL, SAMPLE_DIRECTORY)).thenReturn(
            VALID_OUTPUT_PATH
        )
        val mockDownloadFileLastModified = Date().time
        val mockDownloadFileSize = 50L
        `when`(mockCacheFileService.getMetadata(CacheFileService.METADATA_KEY_LAST_MODIFIED_EPOCH, mockDownloadedFile.path)).thenReturn(
            mockDownloadFileLastModified.toString()
        )
        `when`(mockDownloadedFile.length()).thenReturn(mockDownloadFileSize)

        mockFileUtils.`when`<Any> { FileUtils.extractFromZip(mockDownloadedFile, VALID_OUTPUT_PATH) }.thenReturn(true)

        configurationRulesDownloader.download(SAMPLE_URL, SAMPLE_DIRECTORY, mockCompletionCallback)
        val callbackCaptor: KArgumentCaptor<(RemoteDownloadResult) -> Unit> = argumentCaptor()
        verify(mockRemoteDownloader).download(
            eq(SAMPLE_URL),
            eq(SAMPLE_DIRECTORY),
            eq(mockMetadataProvider),
            callbackCaptor.capture()
        )

        callbackCaptor.firstValue.invoke(RemoteDownloadResult(mockDownloadedFile, Reason.SUCCESS))

        verify(mockCacheFileService, times(1)).getBaseFilePath(SAMPLE_URL, SAMPLE_DIRECTORY)
        verify(mockMetadataProvider, times(1)).createMetadata(
            VALID_OUTPUT_PATH,
            mockDownloadFileSize,
            mockDownloadFileLastModified
        )
        val processedFileCaptor: KArgumentCaptor<File> = argumentCaptor()
        verify(mockCompletionCallback).invoke(processedFileCaptor.capture())

        val capturedProcessedFile = processedFileCaptor.firstValue
        assertEquals(VALID_OUTPUT_PATH, capturedProcessedFile.path)
        verify(mockDownloadedFile).delete()
        verify(mockCompletionCallback).invoke(capturedProcessedFile)
    }

    @After
    fun teardown() {
        mockFileUtils.close()
    }
}
