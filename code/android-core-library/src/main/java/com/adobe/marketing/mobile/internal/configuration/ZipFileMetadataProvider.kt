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

import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.internal.util.FileUtils.readAsString
import com.adobe.marketing.mobile.util.TimeUtils
import com.adobe.marketing.mobile.util.remotedownload.MetadataProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.NumberFormatException
import java.util.Locale
import java.util.TimeZone

/**
 * Responsible for creating and retrieving metadata for aiding remote downloads of zip files
 */
internal class ZipFileMetadataProvider : MetadataProvider {
    companion object {
        private const val LOG_TAG = "ZipFileMetadataProvider"
        private const val DEFAULT_CHARSET = "UTF-8"
        private const val META_FILE_NAME = "meta.txt"
        private const val SEPARATOR = "|"
    }

    /**
     * Creates and adds metadata for the zip file into a separate file in [parentDirectory]
     *
     * @param parentDirectory the directory where the [META_FILE_NAME] should be created
     * @param bundleSize size of the zip file
     * @param lastModifiedDateEpoch epoch when the file was last modified
     */
    fun createMetadata(
        parentDirectory: String?,
        bundleSize: Long,
        lastModifiedDateEpoch: Long
    ) {
        val metadataFile = File(parentDirectory, META_FILE_NAME)

        try {
            FileOutputStream(metadataFile).use { fileOutputStream ->
                val metadataString = "${lastModifiedDateEpoch}${SEPARATOR}${bundleSize}$SEPARATOR"
                fileOutputStream.write(metadataString.toByteArray(charset(DEFAULT_CHARSET)))
            }
        } catch (e: IOException) {
            Log.debug(
                ConfigurationExtension.TAG,
                LOG_TAG,
                "Failed to write metadata into metadata file: $metadataFile"
            )
        }
    }

    /**
     * Parse the metadata from a string to map of metadata headers used for downloads.
     *
     * @param metadataString the string to parse metadata from
     * @return a map of metadata headers if parsing the content is successful, null otherwise
     */
    private fun getMetadataFromString(metadataString: String?): Map<String, String>? {
        if (metadataString == null) {
            return null
        }

        val metadata = mutableMapOf<String, String>()
        val tokens = metadataString.split(SEPARATOR)
        val date: Long
        val size: Long
        try {
            if (tokens.size >= 2) {
                date = tokens[0].toLong()
                size = tokens[1].toLong()
            } else {
                Log.debug(ConfigurationExtension.TAG, LOG_TAG, "Could not de-serialize metadata!")
                return null
            }
        } catch (ne: NumberFormatException) {
            Log.warning(
                ConfigurationExtension.TAG,
                LOG_TAG,
                "Could not read metadata for zip file from string: $metadataString.  $ne"
            )
            return null
        }

        if (date > 0L) {
            // TODO : Implement cache control header instead - https://github.com/adobe/aepsdk-core-android/issues/135
            val lastModified: String =
                TimeUtils.getRFC2822Date(date, TimeZone.getTimeZone("GMT"), Locale.US)
            metadata[MetadataProvider.HTTP_HEADER_IF_RANGE] = lastModified
            metadata[MetadataProvider.HTTP_HEADER_IF_MODIFIED_SINCE] = lastModified
        }

        metadata[MetadataProvider.HTTP_HEADER_RANGE] = String.format(Locale.US, "bytes=%d-", size)
        return metadata
    }

    override fun getMetadata(file: File): Map<String, String>? {
        val metaFile = File(file, META_FILE_NAME)
        return getMetadataFromString(readAsString(metaFile))
    }
}
