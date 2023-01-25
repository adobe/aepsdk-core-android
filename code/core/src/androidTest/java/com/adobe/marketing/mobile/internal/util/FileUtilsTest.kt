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

package com.adobe.marketing.mobile.internal.util

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.lang.Exception

@RunWith(AndroidJUnit4::class)
class FileUtilsTest {

    @get: Rule
    var folder = TemporaryFolder()

    var zipSource: File? = null
    private val sampleContent = "Some Random Content Written To File"
    private val additonalSaampleContent = "Some Additional Random Content Written To File"
    private val MOCK_CONTENT_DIR =
        InstrumentationRegistry.getInstrumentation().context.cacheDir.path + File.separator + "/TestDir"

    companion object {
        private const val RULES_ZIP = "rules.zip"
        private const val RULES_PK_ZIP = "rules_pkzip.zip"
        private const val RULES_ZIP_SLIP = "rules_zipslip.zip"
        private const val FOLDER_ASSETS = "assets"
        private const val JSON_FILE_RULES = "rules.json"
        private const val DUMMY_HTML_FILE = "foo.html"
    }

    @Test
    fun testReadAsString_FileHasContent() {
        File(MOCK_CONTENT_DIR).mkdirs()

        val fileToReadFrom = createFile("FileToRead.txt", sampleContent)
        assertEquals(sampleContent, FileUtils.readAsString(fileToReadFrom))

        deleteDir(File(MOCK_CONTENT_DIR))
    }

    @Test
    fun testReadAsString_FileHasEmptyContent() {
        File(MOCK_CONTENT_DIR).mkdirs()

        val fileToReadFrom = createFile("FileToRead.txt", "")
        assertEquals("", FileUtils.readAsString(fileToReadFrom))

        deleteDir(File(MOCK_CONTENT_DIR))
    }

    @Test
    fun testReadAsString_FileIsNotReadable() {
        File(MOCK_CONTENT_DIR).mkdirs()

        val fileToReadFrom = createFile("FileToRead.txt", "")
        fileToReadFrom.setReadable(false)

        assertEquals(null, FileUtils.readAsString(fileToReadFrom))

        deleteDir(File(MOCK_CONTENT_DIR))
    }

    @Test
    fun testReadAsString_NullFile() {
        assertEquals(null, FileUtils.readAsString(null))
    }

    @Test
    fun testReadAsString_FileDoesNotExist() {
        assertEquals(null, FileUtils.readAsString(File("SomeFile/That/DoesNot/Exist.txt")))
    }

    @Test
    fun readInputStreamToFile_FileDoesNotExist() {
        val nonExistentFile = File("SomeFile/That/DoesNot/Exist.txt")

        assertFalse(
            FileUtils.readInputStreamIntoFile(
                nonExistentFile,
                additonalSaampleContent.byteInputStream(),
                false
            )
        )
    }

    @Test
    fun readInputStreamToFile_FileIsNull() {
        assertFalse(
            FileUtils.readInputStreamIntoFile(
                null,
                additonalSaampleContent.byteInputStream(),
                false
            )
        )
    }

    @Test
    fun readInputStreamToFile_FileExists_Overwrite() {
        File(MOCK_CONTENT_DIR).mkdirs()
        val fileToReadInto = createFile("FileToReadInto.txt", sampleContent)

        assertTrue(
            FileUtils.readInputStreamIntoFile(
                fileToReadInto,
                additonalSaampleContent.byteInputStream(),
                false
            )
        )
        assertEquals(additonalSaampleContent, FileUtils.readAsString(fileToReadInto))

        deleteDir(File(MOCK_CONTENT_DIR))
    }

    @Test
    fun readInputStreamToFile_FileExists_StreamIsEmpty() {
        File(MOCK_CONTENT_DIR).mkdirs()
        val fileToReadInto = createFile("FileToReadInto.txt", sampleContent)

        assertTrue(FileUtils.readInputStreamIntoFile(fileToReadInto, "".byteInputStream(), false))
        assertEquals("", FileUtils.readAsString(fileToReadInto))

        deleteDir(File(MOCK_CONTENT_DIR))
    }

    @Test
    fun readInputStreamToFile_FileExists_Append() {
        File(MOCK_CONTENT_DIR).mkdirs()
        val fileToReadInto = createFile("FileToReadInto.txt", sampleContent)

        assertTrue(
            FileUtils.readInputStreamIntoFile(
                fileToReadInto,
                additonalSaampleContent.byteInputStream(),
                true
            )
        )
        assertEquals(
            "$sampleContent$additonalSaampleContent",
            FileUtils.readAsString(fileToReadInto)
        )

        deleteDir(File(MOCK_CONTENT_DIR))
    }

    @Test
    fun isReadable_FileIsNull() {
        assertFalse(FileUtils.isReadable(null))
    }

    @Test
    fun isReadable_FileDoesNotExist() {
        val nonExistentFile = File("SomeFile/That/DoesNot/Exist.txt")
        assertFalse(FileUtils.isReadable(nonExistentFile))
    }

    @Test
    fun isReadable_FileIsNotReadable() {
        File(MOCK_CONTENT_DIR).mkdirs()

        val fileToReadFrom = createFile("FileToRead.txt", "")
        fileToReadFrom.setReadable(false)

        assertFalse(FileUtils.isReadable(fileToReadFrom))

        deleteDir(File(MOCK_CONTENT_DIR))
    }

    @Test
    fun isReadable_FileIsADirectory() {
        val dir = File(MOCK_CONTENT_DIR)
        dir.mkdirs()
        assertTrue(dir.isDirectory)

        assertFalse(FileUtils.isReadable(dir))

        deleteDir(File(MOCK_CONTENT_DIR))
    }

    @Test
    fun testExtract_ValidZipFile() {
        zipSource = createZipFromAsset(RULES_ZIP)

        val tempDestination = folder.newFolder()
        assertTrue(
            FileUtils.extractFromZip(
                zipSource!!,
                tempDestination.path
            )
        )

        // Verify all the files
        val files = tempDestination.listFiles()
        Assert.assertTrue(existsInList(files, JSON_FILE_RULES))
        Assert.assertTrue(existsInList(files, FOLDER_ASSETS))

        // The test zip file also contains a rules.json (dummy file) inside assets folder.
        // Verify that it got extracted properly
        val assetFolderFiles = File(tempDestination, FOLDER_ASSETS).listFiles()
        Assert.assertTrue(existsInList(assetFolderFiles, JSON_FILE_RULES))
    }

    @Test
    fun testExtract_ValidPkzip() {
        zipSource = createZipFromAsset(RULES_PK_ZIP)
        val tempDestination = folder.newFolder()
        assertTrue(
            FileUtils.extractFromZip(
                zipSource!!,
                tempDestination.path
            )
        )

        // Verify all the files
        val files = tempDestination.listFiles()
        Assert.assertTrue(existsInList(files, JSON_FILE_RULES))
        Assert.assertTrue(existsInList(files, FOLDER_ASSETS))

        // The test zip file also contains a foo.html (dummy file) inside of assets folder.
        // Verify that it got extracted properly
        val assetFolderFiles = File(tempDestination, FOLDER_ASSETS).listFiles()
        assertTrue(existsInList(assetFolderFiles, DUMMY_HTML_FILE))
    }

    @Test
    @Throws(IOException::class)
    fun testExtract_Zipsilp() {
        zipSource = createZipFromAsset(RULES_ZIP_SLIP)
        val tempDestination = folder.newFolder()
        assertFalse(
            FileUtils.extractFromZip(
                zipSource!!,
                tempDestination.path
            )
        )
    }

    @Test
    @Throws(IOException::class)
    fun testExtract_EmptySourceFile() {
        zipSource = createZipFromAsset(RULES_ZIP)
        val emptySourceFile = folder.newFile() // empty file
        val tempDestination = folder.newFolder()
        assertFalse(
            FileUtils.extractFromZip(
                emptySourceFile,
                tempDestination.path
            )
        )
    }

    @Test
    fun testExtract_InvalidSourceFile() {
        val invalidSourceFile = folder.newFile() // Invalid File

        try {
            FileOutputStream(invalidSourceFile).use { fos ->
                fos.write("SomeRandomContents".toByteArray())
            }
        } catch (e: Exception) {
            Assert.fail()
        }

        val destinationFolder = folder.newFolder()
        Assert.assertFalse(
            FileUtils.extractFromZip(
                invalidSourceFile,
                destinationFolder.path
            )
        )
    }

    @Test
    fun testExtract_DestinationNotYetCreated() {
        zipSource = createZipFromAsset(RULES_ZIP)
        // This has not been created yet = extract should create it
        val destination = zipSource!!.absolutePath + "temp123"
        assertTrue(FileUtils.extractFromZip(zipSource!!, destination))
    }

    @Test
    fun testRemoveRelativePath_RelativePathBackslashClearnedUp() {
        assertEquals(FileUtils.removeRelativePath("/mydatabase\\..\\..\\database1"), "mydatabase_database1")
    }

    @Test
    fun testRemoveRelativePath_RelativePathForwardslashCleanedUp() {
        assertEquals(
            FileUtils.removeRelativePath("/mydatabase/../../database1"),
            "mydatabase_database1"
        )
    }

    @Test
    fun testRemoveRelativePath_RelativePathBackslashDoesNotChangeDir() {
        assertEquals(
            FileUtils.removeRelativePath("/mydatabase\\..\\database1"),
            "mydatabase_database1"
        )
    }

    @Test
    fun testRemoveRelativePath_RelativePathForwardslashDoesNotChangeDir() {
        assertEquals(
            FileUtils.removeRelativePath("/mydatabase/../database1"),
            "mydatabase_database1"
        )
    }

    private fun createZipFromAsset(filename: String?): File? {
        try {
            val res = InstrumentationRegistry.getInstrumentation().context.resources
            val instream = res.assets.open(filename!!)
            val tempSource: File = folder.newFile()
            try {
                FileOutputStream(tempSource).use { outputStream ->
                    instream.copyTo(outputStream, 1024)
                }
            } catch (e: Exception) {
                Assert.fail("Unexpected exception while attempting to write to file: ${tempSource.path} ($e)")
                return null
            }
            return tempSource
        } catch (e: Exception) {
            return null
        }
    }

    private fun existsInList(files: Array<File>, fileName: String): Boolean {
        var found = false
        for (file in files) {
            if ((file.name == fileName).also { found = it }) {
                break
            }
        }
        return found
    }

    private fun createFile(fileName: String, content: String?): File {
        val cacheFile: File =
            File(MOCK_CONTENT_DIR + File.separator + fileName)
        try {
            cacheFile.createNewFile()
            val fileWriter = FileWriter(cacheFile)
            fileWriter.write(content)
            fileWriter.flush()
            fileWriter.close()
        } catch (ex: IOException) {
        }
        return cacheFile
    }

    /**
     * Removes the cache directory recursively
     */
    private fun deleteDir(dir: File) {
        val allContents = dir.listFiles()
        if (allContents != null) {
            for (file in allContents) {
                deleteDir(file)
            }
        }
        dir.delete()
    }
}
