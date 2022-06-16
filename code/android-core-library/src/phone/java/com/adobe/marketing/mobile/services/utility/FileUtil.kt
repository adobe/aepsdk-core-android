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

import java.io.File
import kotlin.jvm.Throws

internal object FileUtil {
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
}
