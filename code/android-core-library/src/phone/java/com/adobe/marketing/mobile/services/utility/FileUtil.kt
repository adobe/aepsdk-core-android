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
