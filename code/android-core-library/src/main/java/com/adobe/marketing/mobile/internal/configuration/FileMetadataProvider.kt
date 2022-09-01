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

import com.adobe.marketing.mobile.services.CacheFileService
import com.adobe.marketing.mobile.util.TimeUtils
import com.adobe.marketing.mobile.util.remotedownload.MetadataProvider
import com.adobe.marketing.mobile.util.remotedownload.MetadataProvider.HTTP_HEADER_IF_MODIFIED_SINCE
import com.adobe.marketing.mobile.util.remotedownload.MetadataProvider.HTTP_HEADER_IF_RANGE
import com.adobe.marketing.mobile.util.remotedownload.MetadataProvider.HTTP_HEADER_RANGE
import java.io.File
import java.lang.NumberFormatException
import java.util.Locale
import java.util.TimeZone

/**
 * Responsible for creating and retrieving metadata for aiding remote downloads of non-zip files
 */
internal class FileMetadataProvider(private val cacheFileService: CacheFileService) : MetadataProvider {

    override fun getMetadata(file: File): MutableMap<String, String>? {
        val params = mutableMapOf<String, String>()
        val lastModified: String? = try {
            val epochString = cacheFileService.getMetadata(CacheFileService.METADATA_KEY_LAST_MODIFIED_EPOCH, file.path)
            if (epochString == null) {
                null
            } else {
                TimeUtils.getRFC2822Date(
                    epochString.toLong(),
                    TimeZone.getTimeZone("GMT"),
                    Locale.US
                )
            }
        } catch (nfe: NumberFormatException) {
            null
        }

        if (lastModified != null) {
            // TODO : Implement cache control header instead - https://github.com/adobe/aepsdk-core-android/issues/135
            params[HTTP_HEADER_IF_RANGE] = lastModified
            params[HTTP_HEADER_IF_MODIFIED_SINCE] = lastModified
        }

        val size: Long = file.length()
        params[HTTP_HEADER_RANGE] = String.format(Locale.US, "bytes=%d-", size)
        return params
    }
}
