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

import com.adobe.marketing.mobile.utils.TimeUtils
import com.adobe.marketing.mobile.utils.remotedownload.MetadataProvider
import junit.framework.TestCase.assertEquals
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull

class ZipFileMetadataProviderTest {
    companion object {
        const val METADATA_FILE_NAME = "meta.txt"
    }
    private val sampleLastModifiedDate = Date()
    private val expectedLastModifiedHeader: String =
        TimeUtils.getRFC2822Date(sampleLastModifiedDate.time, TimeZone.getTimeZone("GMT"), Locale.US)

    @get: Rule
    var mockDirectory = TemporaryFolder()

    private lateinit var zipFileMetadataProvider: ZipFileMetadataProvider

    @Before
    fun setUp() {
        zipFileMetadataProvider = ZipFileMetadataProvider()
    }

    @Test
    fun testGetMetadata_ValidFileExists() {
        // write a valid meta content
        val metaFile: File = File(mockDirectory.root, METADATA_FILE_NAME)
        writeMetadataContentToFile("${sampleLastModifiedDate.time}|1234|", metaFile)

        val metadata: Map<String, String>? =
            zipFileMetadataProvider.getMetadata(mockDirectory.root)

        assertEquals(3, metadata!!.size)

        // verify
        Assert.assertEquals(expectedLastModifiedHeader, metadata[MetadataProvider.HTTP_HEADER_IF_MODIFIED_SINCE])
        Assert.assertEquals(expectedLastModifiedHeader, metadata[MetadataProvider.HTTP_HEADER_IF_RANGE])
        Assert.assertEquals("bytes=1234-", metadata[MetadataProvider.HTTP_HEADER_RANGE])
    }

    @Test
    fun testGetMetadata_MetadataFormatIsIncorrect() {
        // write a invalid valid meta content
        val metaFile: File = File(mockDirectory.root, METADATA_FILE_NAME)
        writeMetadataContentToFile("23123123/1234/", metaFile)

        val metadata: Map<String, String>? =
            zipFileMetadataProvider.getMetadata(mockDirectory.root)
        assertNull(metadata)
    }

    @Test
    fun testGetMetadata_LastModifiedDateIsMalformed() {
        // write a invalid valid meta content
        val metaFile: File = File(mockDirectory.root, METADATA_FILE_NAME)
        writeMetadataContentToFile("NotAValidDate|1234|", metaFile)

        val metadata: Map<String, String>? =
            zipFileMetadataProvider.getMetadata(mockDirectory.root)
        assertNull(metadata)
    }

    @Test
    fun testGetMetadata_SizeIsNonNumeric() {
        // write a invalid valid meta content
        val metaFile: File = File(mockDirectory.root, METADATA_FILE_NAME)
        writeMetadataContentToFile("213123|NotAValidSize|", metaFile)

        val metadata: Map<String, String>? =
            zipFileMetadataProvider.getMetadata(mockDirectory.root)
        assertNull(metadata)
    }

    @Test
    fun testGetMetadata_MetadataFileIsEmpty() {
        // empty metadata content
        val metaFile: File = File(mockDirectory.root, METADATA_FILE_NAME)
        writeMetadataContentToFile("", metaFile)

        val metadata: Map<String, String>? =
            zipFileMetadataProvider.getMetadata(mockDirectory.root)
        assertNull(metadata)
    }

    @Test
    fun testGetMetadata_MetadataDoesNotExist() {
        val metadata: Map<String, String>? = zipFileMetadataProvider.getMetadata(mockDirectory.root)
        assertNull(metadata)
    }

    @Test
    fun testCreateMetadata_ValidDirectory() {
        zipFileMetadataProvider.createMetadata(mockDirectory.root.absolutePath, 500, sampleLastModifiedDate.time)
        val metadata: Map<String, String>? = zipFileMetadataProvider.getMetadata(mockDirectory.root)

        assertNotNull(metadata)
        Assert.assertEquals(expectedLastModifiedHeader, metadata!![MetadataProvider.HTTP_HEADER_IF_MODIFIED_SINCE])
        Assert.assertEquals(expectedLastModifiedHeader, metadata[MetadataProvider.HTTP_HEADER_IF_RANGE])
        Assert.assertEquals("bytes=500-", metadata[MetadataProvider.HTTP_HEADER_RANGE])
    }

    @Test
    fun testCreateMetadata_NullDirectory() {
        val lastModified = Date().time
        zipFileMetadataProvider.createMetadata(null, 500, lastModified)
        val metadata: Map<String, String>? = zipFileMetadataProvider.getMetadata(mockDirectory.root)

        assertNull(metadata)
    }

    @Test
    fun testCreateMetadata_InvalidDirectory() {
        val lastModified = Date().time
        val invalidParentDir: File = File(mockDirectory.root, "SomeTextFile.txt")
        zipFileMetadataProvider.createMetadata(invalidParentDir.absolutePath, 500, lastModified)
        val metadata: Map<String, String>? = zipFileMetadataProvider.getMetadata(mockDirectory.root)

        assertNull(metadata)
    }

    private fun writeMetadataContentToFile(content: String, file: File) {
        FileOutputStream(file).use { fileOutputStream ->
            fileOutputStream.write(content.toByteArray(charset("UTF-8")))
        }
    }
}
