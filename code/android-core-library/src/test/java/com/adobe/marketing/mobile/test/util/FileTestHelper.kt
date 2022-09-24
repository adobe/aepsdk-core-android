package com.adobe.marketing.mobile.test.util

import java.io.File

object FileTestHelper {

    @JvmStatic
    @Throws(SecurityException::class)
    fun deleteFile(fileToDelete: File?, recursive: Boolean): Boolean {
        if (fileToDelete == null) {
            return false
        }
        return if (recursive) fileToDelete.deleteRecursively() else fileToDelete.delete()
    }
}
