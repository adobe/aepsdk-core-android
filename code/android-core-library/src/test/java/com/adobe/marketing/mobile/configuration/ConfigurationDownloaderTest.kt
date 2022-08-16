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
import com.adobe.marketing.mobile.utils.RemoteDownloader
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mockStatic
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import java.io.File
import kotlin.test.assertEquals

@RunWith(MockitoJUnitRunner.Silent::class)
class ConfigurationDownloaderTest {

    companion object {
        const val SAMPLE_URL = "http://assets.adobe.com/1234"
        const val SAMPLE_DIRECTORY = "downloaded/rules/folder"
    }

    @Mock
    private lateinit var mockMetadataProvider: FileMetadataProvider

    @Mock
    private lateinit var mockCompletionCallback: (Map<String, Any?>?) -> Unit

    @Mock
    private lateinit var mockDownloadedFile: File

    @Mock
    private lateinit var mockRemoteDownloader: RemoteDownloader

    private lateinit var mockFileUtils: MockedStatic<FileUtils>

    private lateinit var configurationDownloader: ConfigurationDownloader

    @Before
    fun setUp() {
        mockFileUtils = mockStatic(FileUtils::class.java)

        configurationDownloader =
            ConfigurationDownloader(mockRemoteDownloader, mockMetadataProvider)
    }

    @Test
    fun `Download invokes remote downloader `() {
        val mockConfigJson = "{}"

        mockFileUtils.`when`<Any> { FileUtils.readAsString(mockDownloadedFile) }.thenReturn(mockConfigJson)

        val callbackCaptor: KArgumentCaptor<(File?) -> Unit> = argumentCaptor()

        configurationDownloader.download(SAMPLE_URL, SAMPLE_DIRECTORY, mockCompletionCallback)

        verify(mockRemoteDownloader).download(
            eq(SAMPLE_URL),
            eq(SAMPLE_DIRECTORY),
            eq(mockMetadataProvider),
            callbackCaptor.capture()
        )
        val capturedCallback = callbackCaptor.firstValue

        // Simulate callback from RemoteDownloader.download()
        capturedCallback.invoke(mockDownloadedFile)

        // verify that the original callback is invoked with right content
        verify(mockCompletionCallback).invoke(emptyMap())
    }

    @Test
    fun `Download when RemoteDownload result is a null file`() {
        val callbackCaptor: KArgumentCaptor<(File?) -> Unit> = argumentCaptor()

        configurationDownloader.download(SAMPLE_URL, SAMPLE_DIRECTORY, mockCompletionCallback)

        verify(mockRemoteDownloader).download(
            eq(SAMPLE_URL),
            eq(SAMPLE_DIRECTORY),
            eq(mockMetadataProvider),
            callbackCaptor.capture()
        )
        val capturedCallback = callbackCaptor.firstValue

        // Simulate callback from RemoteDownloader.download()
        capturedCallback.invoke(null)

        // verify that the original callback is invoked with right content
        verify(mockCompletionCallback).invoke(null)
    }

    @Test
    fun `Download when RemoteDownload result file cannot be parsed`() {
        mockFileUtils.`when`<Any> { FileUtils.readAsString(mockDownloadedFile) }.thenReturn(null)

        val callbackCaptor: KArgumentCaptor<(File?) -> Unit> = argumentCaptor()

        configurationDownloader.download(SAMPLE_URL, SAMPLE_DIRECTORY, mockCompletionCallback)

        verify(mockRemoteDownloader).download(
            eq(SAMPLE_URL),
            eq(SAMPLE_DIRECTORY),
            eq(mockMetadataProvider),
            callbackCaptor.capture()
        )
        val capturedCallback = callbackCaptor.firstValue

        // Simulate callback from RemoteDownloader.download()
        capturedCallback.invoke(mockDownloadedFile)

        // verify that the original callback is invoked with right content
        verify(mockCompletionCallback).invoke(null)
    }

    @Test
    fun `Download when RemoteDownload result file is empty`() {
        mockFileUtils.`when`<Any> { FileUtils.readAsString(mockDownloadedFile) }.thenReturn("")

        val callbackCaptor: KArgumentCaptor<(File?) -> Unit> = argumentCaptor()

        configurationDownloader.download(SAMPLE_URL, SAMPLE_DIRECTORY, mockCompletionCallback)

        verify(mockRemoteDownloader).download(
            eq(SAMPLE_URL),
            eq(SAMPLE_DIRECTORY),
            eq(mockMetadataProvider),
            callbackCaptor.capture()
        )
        val capturedCallback = callbackCaptor.firstValue

        // Simulate callback from RemoteDownloader.download()
        capturedCallback.invoke(mockDownloadedFile)

        // verify that the original callback is invoked with right content
        verify(mockCompletionCallback).invoke(emptyMap())
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

        mockFileUtils.`when`<Any> { FileUtils.readAsString(mockDownloadedFile) }.thenReturn(mockConfigContentJson)

        val callbackCaptor: KArgumentCaptor<(File?) -> Unit> = argumentCaptor()

        configurationDownloader.download(SAMPLE_URL, SAMPLE_DIRECTORY, mockCompletionCallback)

        verify(mockRemoteDownloader).download(
            eq(SAMPLE_URL),
            eq(SAMPLE_DIRECTORY),
            eq(mockMetadataProvider),
            callbackCaptor.capture()
        )
        val capturedCallback = callbackCaptor.firstValue

        // Simulate callback from RemoteDownloader.download()
        capturedCallback.invoke(mockDownloadedFile)

        // verify that the original callback is invoked with right content
        val expectedConfig = mapOf<String, Any?>(
            "build.environment" to "stage",
            "global.ssl" to true,
            "rules.url" to "https://assets.adobedtm.com/launch-rules.zip",
            "experienceCloud.org" to "B1F855165B4C9EA50A495E06@AdobeOrg",
            "campaign.pkey" to "@BoOkfxbtfRRqALRp3rL7KOM5Xd2M4M-campaignkey",
            "timeout" to 30.5
        )

        val configMapCaptor: KArgumentCaptor<Map<String, Any?>?> = argumentCaptor()
        verify(mockCompletionCallback).invoke(configMapCaptor.capture())
        assertEquals(expectedConfig, configMapCaptor.firstValue)
    }

    @Test
    fun `Download when RemoteDownload result file has malformed config content`() {
        val mockConfigContentJson = "{" +
            "\"build.environment\":\"stage\",\n" +
            "\"global.ssl\": true,\n" +
            "\"rules.url\"= \"https://assets.adobedtm.com/launch-rules.zip\"," + // This line has = instead of :
            "}"
        mockFileUtils.`when`<Any> { FileUtils.readAsString(mockDownloadedFile) }.thenReturn(mockConfigContentJson)

        val callbackCaptor: KArgumentCaptor<(File?) -> Unit> = argumentCaptor()

        configurationDownloader.download(SAMPLE_URL, SAMPLE_DIRECTORY, mockCompletionCallback)

        verify(mockRemoteDownloader).download(
            eq(SAMPLE_URL),
            eq(SAMPLE_DIRECTORY),
            eq(mockMetadataProvider),
            callbackCaptor.capture()
        )
        val capturedCallback = callbackCaptor.firstValue

        // Simulate callback from RemoteDownloader.download()
        capturedCallback.invoke(mockDownloadedFile)

        verify(mockCompletionCallback).invoke(null)
    }

    @After
    fun teardown() {
        mockFileUtils.close()
    }
}
