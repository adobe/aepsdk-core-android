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
package com.adobe.marketing.mobile.services.utility

import android.content.Context
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.MobileCore
import java.io.File

internal object FileUtil {

    private const val LOG_TAG = "FileUtil"

    /**
     * Removes the relative part of the file name(if exists).
     *
     * for ex: File name `/mydatabase/../../database1` will be converted to `mydatabase_database1`
     *
     * @param filePath the file name
     * @return file name without relative path
     */
    @JvmStatic
    fun removeRelativePath(filePath: String): String {
        return if (filePath.isEmpty()) {
            filePath
        } else {
            var result = filePath.replace("\\.[/\\\\]".toRegex(), "\\.")
            result = result.replace("[/\\\\](\\.{2,})".toRegex(), "_")
            result = result.replace("/".toRegex(), "")
            result
        }
    }

    /**
     * Copies the contents from `src` to `dest`.
     *
     * @param src [File] from which the contents are read
     * @param dest [File] to which contents are written to
     * @throws Exception if `src` or `dest` is not present or it does not have read permissions
     */
    @JvmStatic
    @Throws(Exception::class)
    fun copyFile(src: File, dest: File) {
        src.copyTo(dest, true)
    }

    @JvmStatic
    fun migrateSharedPreference(context: Context, dataStoreName: String, prefix: String) {
        val existingSharedPreferenceFile = File(getSharedPreferenceFilePath(context, dataStoreName))
        if (existingSharedPreferenceFile.exists()) {
            val newSharedPreferenceFile =
                File(getSharedPreferenceFilePath(context, prefix + dataStoreName))
            try {
                copyFile(existingSharedPreferenceFile, newSharedPreferenceFile)
                existingSharedPreferenceFile.delete()
            } catch (e: java.lang.Exception) {
                MobileCore.log(LoggingMode.DEBUG,
                    LOG_TAG,
                    "Unable to migrate SharedPreference file $e"
                )
            }
        }
    }

    private fun getSharedPreferenceFilePath(context: Context, dataStoreName: String): String {
        return "/data/data" + context.packageName + "/shared_prefs/" + dataStoreName + ".xml"
    }
}
