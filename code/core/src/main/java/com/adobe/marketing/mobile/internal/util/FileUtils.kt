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

import com.adobe.marketing.mobile.internal.CoreConstants
import com.adobe.marketing.mobile.services.Log
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.util.zip.ZipInputStream

internal object FileUtils {
    const val TAG = "FileUtils"
    private const val MAX_BUFFER_SIZE = 4096

    /**
     * Verifies if the directory is writable
     *
     * @param directory the directory to check for
     * @return true if [directory] represents a directory and the directory is writable.
     *         false otherwise
     */
    @JvmStatic
    fun isWritableDirectory(directory: File?): Boolean {
        return directory != null && directory.isDirectory && directory.canWrite()
    }

    /**
     * Reads the content of [file] as a [String].
     *
     * @param file the file whose contents need to be read
     * @return contents of the file as a string or,
     *         null if the file cannot be read
     */
    @JvmStatic
    fun readAsString(file: File?): String? {
        if (!isReadable(file)) {
            Log.debug(
                CoreConstants.LOG_TAG,
                TAG,
                "Failed to read file: ($file)"
            )
            return null
        }

        val content = StringBuilder()
        try {
            BufferedReader(InputStreamReader(FileInputStream(file), Charsets.UTF_8)).use { br ->
                var line: String?
                while (br.readLine().also { line = it } != null) {
                    content.append(line)
                }
            }
        } catch (e: Exception) {
            Log.debug(
                CoreConstants.LOG_TAG,
                TAG,
                "Failed to read $file contents. $e"
            )
            return null
        }

        return content.toString()
    }

    /**
     * Checks if the [file] content can be read.
     *
     * @param file the source file whose readability is to be validated
     * @return true if [file] exists, is not null, is a file and has read permissions;
     *         false otherwise
     */
    @JvmStatic
    fun isReadable(file: File?): Boolean {
        try {
            if (file == null || !file.exists() || !file.canRead() || !file.isFile) {
                Log.debug(
                    CoreConstants.LOG_TAG,
                    TAG,
                    "File does not exist or doesn't have read permission $file"
                )
                return false
            }
            return true
        } catch (e: SecurityException) {
            Log.debug(
                CoreConstants.LOG_TAG,
                TAG,
                "Failed to read file ($e)"
            )
            return false
        }
    }

    /**
     * Reads the content of [inputStream] into [file].
     *
     * @param file the file whose contents need to be created/updated read from the [inputStream]
     * @param inputStream the [InputStream] from which the contents should be read
     * @param append if the contents of [inputStream] should be appended to the contents of [file]
     * @return true if the contents of the input stream were added to the file,
     *         false otherwise
     */
    @JvmStatic
    fun readInputStreamIntoFile(file: File?, inputStream: InputStream, append: Boolean): Boolean {
        return try {
            FileOutputStream(file, append).use { outputStream ->
                inputStream.copyTo(outputStream, MAX_BUFFER_SIZE)
            }

            true
        } catch (e: Exception) {
            Log.debug(
                CoreConstants.LOG_TAG,
                TAG,
                "Unexpected exception while attempting to write to file: ${file?.path} ($e)"
            )
            false
        }
    }

    /**
     * Extracts the zip file to an output directory.
     *
     * @param zipFile the zip file that needs to be extracted
     * @param outputDirectoryPath the destination for the extracted [zipFile] contents
     * @return true if the zip file has been successfully extracted
     *         false otherwise
     */
    @JvmStatic
    fun extractFromZip(zipFile: File?, outputDirectoryPath: String): Boolean {
        if (zipFile == null) return false

        val folder = File(outputDirectoryPath)
        if (!folder.exists() && !folder.mkdir()) {
            Log.debug(
                CoreConstants.LOG_TAG,
                TAG,
                "Could not create the output directory $outputDirectoryPath"
            )
            return false
        }

        var extractedSuccessfully = true
        try {
            ZipInputStream(FileInputStream(zipFile)).use { zipInputStream ->
                // get the zipped file list entry
                var ze = zipInputStream.nextEntry
                val outputFolderCanonicalPath = folder.canonicalPath
                if (ze == null) {
                    // Invalid zip file!
                    Log.debug(
                        CoreConstants.LOG_TAG,
                        TAG,
                        "Zip file was invalid"
                    )
                    return false
                }
                var entryProcessedSuccessfully = true
                while (ze != null && entryProcessedSuccessfully) {
                    val fileName = ze.name
                    val newZipEntryFile = File(outputDirectoryPath + File.separator + fileName)
                    if (!newZipEntryFile.canonicalPath.startsWith(outputFolderCanonicalPath)) {
                        Log.debug(
                            CoreConstants.LOG_TAG,
                            TAG,
                            "The zip file contained an invalid path. Verify that your zip file is formatted correctly and has not been tampered with."
                        )
                        return false
                    }
                    entryProcessedSuccessfully = if (ze.isDirectory) {
                        // handle directory
                        (newZipEntryFile.exists() || newZipEntryFile.mkdirs())
                    } else {
                        // handle file
                        val parentFolder = newZipEntryFile.parentFile
                        if (parentFolder != null && (parentFolder.exists() || parentFolder.mkdirs())) {
                            readInputStreamIntoFile(newZipEntryFile, zipInputStream, false)
                        } else {
                            Log.debug(
                                CoreConstants.LOG_TAG,
                                TAG,
                                "Could not extract the file ${newZipEntryFile.absolutePath}"
                            )
                            return false
                        }
                    }
                    extractedSuccessfully = extractedSuccessfully && entryProcessedSuccessfully

                    zipInputStream.closeEntry()
                    ze = zipInputStream.nextEntry
                }
                zipInputStream.closeEntry()
            }
        } catch (ex: Exception) {
            Log.debug(
                CoreConstants.LOG_TAG,
                TAG,
                "Extraction failed - $ex"
            )
            extractedSuccessfully = false
        }

        return extractedSuccessfully
    }

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
        return if (filePath.isBlank()) {
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

    /**
     * Move file from `src` to `dest`.
     *
     * @param src [File] source file
     * @param dest [File] destination to move the file
     * @throws Exception if `src` is not present or it does not have read permissions
     */
    @JvmStatic
    @Throws(Exception::class)
    fun moveFile(src: File, dest: File) {
        if (dest.parentFile != null && !dest.parentFile.exists()) {
            dest.parentFile.mkdirs()
        }

        if (!dest.exists()) {
            dest.createNewFile()
        }

        copyFile(src, dest)
        deleteFile(src, false)
    }

    /**
     * Deletes the file
     *
     * @param fileToDelete [File] which needs to be deleted
     * @param recursive [Boolean] if true, delete this file with all its children.
     * @throws SecurityException if it does not have permission to delete file
     */
    @JvmStatic
    @Throws(SecurityException::class)
    fun deleteFile(fileToDelete: File?, recursive: Boolean): Boolean {
        if (fileToDelete == null) {
            return false
        }
        return if (recursive) fileToDelete.deleteRecursively() else fileToDelete.delete()
    }
}
