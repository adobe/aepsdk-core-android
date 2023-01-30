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

package com.adobe.marketing.mobile;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.adobe.marketing.mobile.util.StringUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileTestHelper {

    static final String CACHE_DIRECTORY = "adbdownloadcache";
    static final String FILE_DIRECTORY = "adbdownloadfile";
    static final String MOCK_FILE_NAME =
            "c0a6221b2b55775b6bc5761fdb1ac0c965cc823c55e8db0b3f903b24f82fcb90.1484711165000_someETag";
    static final String MOCK_CONFIG_JSON = "{'someJsonKey':'someJsonValue'}";

    List<String> getFilesInDirectory(final String directory) {
        File cacheDir = getCacheDirectory(directory);
        String[] files = cacheDir.list();

        if (files == null || files.length <= 0) {
            return null;
        }

        return new ArrayList<String>(Arrays.asList(files));
    }

    File getCacheDirectory(final String cacheDirOverride) {
        final String directory =
                StringUtils.isNullOrEmpty(cacheDirOverride) ? CACHE_DIRECTORY : cacheDirOverride;
        File cacheDirectory = new File(getClass().getResource("").getPath(), directory);

        if (!cacheDirectory.exists()) {
            assertTrue(
                    "Could not create test cache directory - "
                            + this.getClass().getResource("").getPath()
                            + directory,
                    cacheDirectory.mkdirs());
        }

        return cacheDirectory;
    }

    File getCacheDirectory() {
        return getCacheDirectory(null);
    }

    File sampleApplicationBaseDir() {
        return new File(this.getClass().getResource("").getPath());
    }

    void deleteTempCacheDirectory() {
        deleteTempCacheDirectory(null);
    }

    /** Removes the cache directory recursively */
    void deleteTempCacheDirectory(final String dirName) {
        File cacheDirectory = getCacheDirectory(dirName);
        String[] files = cacheDirectory.list();

        if (files != null) {
            for (String file : files) {
                File currentFile = new File(cacheDirectory.getPath(), file);

                if (currentFile.isDirectory()) {
                    deleteTempCacheDirectory(
                            File.separator
                                    + (StringUtils.isNullOrEmpty(dirName)
                                            ? CACHE_DIRECTORY
                                            : dirName)
                                    + File.separator
                                    + file);
                } else {
                    currentFile.delete();
                }
            }
        }

        cacheDirectory.delete();
    }

    File placeSampleCacheFile() {
        return createFile(MOCK_FILE_NAME);
    }

    void placeSampleCacheFile(final String dirName) {
        createFile(dirName, MOCK_FILE_NAME);
    }

    void placeOtherCacheFile() {
        createFile("somethingElse.1484711165000_someETag");
    }

    void placeOtherCacheFile(final String dirName) {
        createFile(dirName, "somethingElse.1484711165000_someETag");
    }

    File placePartiallyDownloadedCacheFile() {
        return createFile(MOCK_FILE_NAME + "_partial");
    }

    void placeInvalidCacheFile(final String dirName) {
        createFile(dirName, "1484711165000-1484711165000-1484711165000");
    }

    File placeSampleCacheDirectory(final String dirName, final String fileName) {
        File cacheDirectory =
                new File(getCacheDirectory(CACHE_DIRECTORY) + File.separator + dirName);
        cacheDirectory.mkdir();
        File file =
                new File(
                        getCacheDirectory(CACHE_DIRECTORY)
                                + File.separator
                                + dirName
                                + File.separator
                                + fileName);

        try {
            file.createNewFile();
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(MOCK_CONFIG_JSON);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException ex) {
            fail("Could not create test directory and files " + ex);
        }

        return cacheDirectory;
    }

    private File createFile(final String fileName) {
        return createFile(null, fileName);
    }

    private File createFile(final String directoryName, final String fileName) {
        File cacheFile = new File(getCacheDirectory(directoryName) + File.separator + fileName);

        try {
            cacheFile.createNewFile();
            FileWriter fileWriter = new FileWriter(cacheFile);
            fileWriter.write(MOCK_CONFIG_JSON);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException ex) {
        }

        return cacheFile;
    }
}
