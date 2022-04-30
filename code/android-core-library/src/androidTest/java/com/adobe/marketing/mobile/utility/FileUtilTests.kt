package com.adobe.marketing.mobile.utility

import com.adobe.marketing.mobile.services.utility.FileUtil
import org.junit.Assert.assertEquals
import org.junit.Test

class FileUtilTests {

    @Test
    fun testRemoveRelativePath_RelativePathBackslashClearnedUp() {
        assertEquals(FileUtil.removeRelativePath("/mydatabase\\..\\..\\database1"), "mydatabase_database1")
    }

    @Test
    fun testRemoveRelativePath_RelativePathForwardslashClearnedUp() {
        assertEquals(FileUtil.removeRelativePath("/mydatabase/../../database1"), "mydatabase_database1")
    }

    @Test
    fun testRemoveRelativePath_RelativePathBackslashDoesNotChangeDir() {
        assertEquals(FileUtil.removeRelativePath("/mydatabase\\..\\database1"), "mydatabase_database1")
    }

    @Test
    fun testRemoveRelativePath_RelativePathForwardslashDoesNotChangeDir() {
        assertEquals(FileUtil.removeRelativePath("/mydatabase/../database1"), "mydatabase_database1")
    }
}
