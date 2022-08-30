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

package com.adobe.marketing.mobile.internal.compatibility;

import static org.junit.Assert.*;

import com.adobe.marketing.mobile.services.CacheFileService;
import com.adobe.marketing.mobile.internal.utility.StringEncoder;
import com.adobe.marketing.mobile.services.DeviceInforming;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

// TODO: Remove this class when Java version of ConfigurationExtension is deleted
@RunWith(JUnit4.class)
public class CacheManagerTest {

    @Mock
    private DeviceInforming mockDeviceInfoService;

    private static final String CACHE_DIRECTORY = "adbdownloadcache";

    private CacheManager cacheManager;
    private File applicationBaseDirForTest;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        cacheManager = new CacheManager(mockDeviceInfoService);
        applicationBaseDirForTest = new File(this.getClass().getResource("").getPath()+"/MockApplicationCacheDir");
        applicationBaseDirForTest.mkdirs();
    }

    @Test
    public void testCreateCacheFile_ValidEtag_NullURL() {
        // Setup
        final String url = null;
        final String etag = "W/\"3a2-bMnM1spT5zNBH3xgDTaqZQ\"";
        final String cacheDir = "usethisdir";
        final Date date = new Date(1484711165000L);
        Mockito.when(mockDeviceInfoService.getApplicationCacheDir()).thenReturn(applicationBaseDirForTest);

        final HashMap<String, String> testMetadata = new HashMap<>();
        testMetadata.put(CacheFileService.METADATA_KEY_ETAG, etag);
        testMetadata.put(CacheFileService.METADATA_KEY_LAST_MODIFIED_EPOCH, String.valueOf(date.getTime()));

        // Test
        final File createdFile = cacheManager.createCacheFile(url, testMetadata, cacheDir);

        // Verify
        assertNull(createdFile);

        
    }

    @Test
    public void testCreateCacheFile_ValidEtag_NulLastModified() {
        // Setup
        final String url = null;
        final String etag = "W/\"3a2-bMnM1spT5zNBH3xgDTaqZQ\"";
        final String cacheDir = "usethisdir";
        Mockito.when(mockDeviceInfoService.getApplicationCacheDir()).thenReturn(applicationBaseDirForTest);

        final HashMap<String, String> testMetadata = new HashMap<>();
        testMetadata.put(CacheFileService.METADATA_KEY_ETAG, etag);
        testMetadata.put(CacheFileService.METADATA_KEY_LAST_MODIFIED_EPOCH, null);

        // Test
        final File createdFile = cacheManager.createCacheFile(url, testMetadata, cacheDir);

        // Verify
        assertNull(createdFile);

        
    }

    @Test
    public void testCreateCacheFile_ValidEtag_NullAppCacheDirectory() {
        // Setup
        final String url = "www.sample.com";
        final String etag = "12345ABCDE";
        final String cacheDir = "usethisdir";
        final Date date = new Date(1484711165000L);
        final HashMap<String, String> testMetadata = new HashMap<>();
        testMetadata.put(CacheFileService.METADATA_KEY_ETAG, etag);
        testMetadata.put(CacheFileService.METADATA_KEY_LAST_MODIFIED_EPOCH, String.valueOf(date.getTime()));

        Mockito.when(mockDeviceInfoService.getApplicationCacheDir()).thenReturn(null);

        // Test
        final File createdFile = cacheManager.createCacheFile(url, testMetadata, cacheDir);

        // Verify
        assertNull(createdFile);

        
    }

    @Test
    public void testCreateCacheFile_ValidEtag_ValidMetadata() throws IOException {
        // Setup
        final String url = "www.sample.com";
        final String etag = "W/\"3a2-bMnM1spT5zNBH3xgDTaqZQ\"";
        final String hexEtag = StringEncoder.getHexString(etag);
        final String cacheDir = "usethisdir";
        final Date date = new Date(1484711165000L);
        final HashMap<String, String> testMetadata = new HashMap<>();
        testMetadata.put(CacheFileService.METADATA_KEY_ETAG, etag);
        testMetadata.put(CacheFileService.METADATA_KEY_LAST_MODIFIED_EPOCH, String.valueOf(date.getTime()));

        Mockito.when(mockDeviceInfoService.getApplicationCacheDir()).thenReturn(applicationBaseDirForTest);

        // Test
        final File createdFile = cacheManager.createCacheFile(url, testMetadata, cacheDir);
        final String expectedPath = applicationBaseDirForTest.getPath() +
                File.separator +
                cacheDir +
                File.separator +
                "dd6681ae7411ac16cdc5756c483ec438b5512e26013715b76e8ae7587120486a" +
                "." +
                hexEtag +
                "." +
                date.getTime() +
                "_partial";

        // Verify
        assertNotNull(createdFile);
        assertEquals(expectedPath, createdFile.getPath());

        createdFile.deleteOnExit();
        assertTrue(createdFile.createNewFile());

        
    }

    @Test
    public void testCreateCacheFile_ValidMetadata_NoCustomCacheDir() throws IOException {
        // Setup
        final String url = "www.sample.com";
        final String etag = "W/\"3a2-bMnM1spT5zNBH3xgDTaqZQ\"";
        final String hexEtag = StringEncoder.getHexString(etag);
        final Date date = new Date(1484711165000L);
        final HashMap<String, String> testMetadata = new HashMap<>();
        testMetadata.put(CacheFileService.METADATA_KEY_ETAG, etag);
        testMetadata.put(CacheFileService.METADATA_KEY_LAST_MODIFIED_EPOCH, String.valueOf(date.getTime()));

        Mockito.when(mockDeviceInfoService.getApplicationCacheDir()).thenReturn(applicationBaseDirForTest);

        // Test
        final File createdFile = cacheManager.createCacheFile(url, testMetadata, null);
        final String expectedPath = applicationBaseDirForTest.getPath() +
                File.separator +
                "adbdownloadcache" +
                File.separator +
                "dd6681ae7411ac16cdc5756c483ec438b5512e26013715b76e8ae7587120486a" +
                "." +
                hexEtag +
                "." +
                date.getTime() +
                "_partial";

        // Verify
        assertNotNull(createdFile);
        assertEquals(expectedPath, createdFile.getPath());

        createdFile.deleteOnExit();
        assertTrue(createdFile.createNewFile());

        
    }

    @Test
    public void testCreateCacheFile_NoCustomDirectory_NullCacheDirectory() {
        // Setup
        final String url = "www.sample.com";
        final String etag = "W/\"3a2-bMnM1spT5zNBH3xgDTaqZQ\"";
        final String hexEtag = StringEncoder.getHexString(etag);
        final Date date = new Date(1484711165000L);
        final HashMap<String, String> testMetadata = new HashMap<>();
        testMetadata.put(CacheFileService.METADATA_KEY_ETAG, etag);
        testMetadata.put(CacheFileService.METADATA_KEY_LAST_MODIFIED_EPOCH, String.valueOf(date.getTime()));

        Mockito.when(mockDeviceInfoService.getApplicationCacheDir()).thenReturn(null);

        // Test
        final File createdFile = cacheManager.createCacheFile(url, testMetadata, null);

        // Verify
        assertNull(createdFile);

        
    }

    @Test
    public void testCreateCacheFile_NoCustomDirectory_NullURL() {
        // Setup
        final String url = "www.sample.com";
        final String etag = "W/\"3a2-bMnM1spT5zNBH3xgDTaqZQ\"";
        final String hexEtag = StringEncoder.getHexString(etag);
        final Date date = new Date(1484711165000L);
        final HashMap<String, String> testMetadata = new HashMap<>();
        testMetadata.put(CacheFileService.METADATA_KEY_ETAG, etag);
        testMetadata.put(CacheFileService.METADATA_KEY_LAST_MODIFIED_EPOCH, String.valueOf(date.getTime()));

        Mockito.when(mockDeviceInfoService.getApplicationCacheDir()).thenReturn(applicationBaseDirForTest);

        // Test
        final File createdFile = cacheManager.createCacheFile(null, testMetadata, null);

        // Verify
        assertNull(createdFile);

        
    }

    @Test
    public void testDeleteCachedFile_NullURL() {
        // Setup
        final String url = null;
        final String directoryOverride = "usethisdir";
        Mockito.when(mockDeviceInfoService.getApplicationCacheDir()).thenReturn(applicationBaseDirForTest);

        // Test
        final boolean isDeleted = cacheManager.deleteCacheFile(url, directoryOverride);

        // Verify
        assertFalse(isDeleted);

        
    }

    @Test
    public void testDeleteCachedFile_EmptyURL() {
        // Setup
        final String url = "";
        final String directoryOverride = "usethisdir";
        Mockito.when(mockDeviceInfoService.getApplicationCacheDir()).thenReturn(applicationBaseDirForTest);

        // Test
        final boolean isDeleted = cacheManager.deleteCacheFile(url, directoryOverride);

        // Verify
        assertFalse(isDeleted);

        
    }

    @Test
    public void testDeleteCachedFile_NullApplicationCacheDir() {
        // Setup
        final String url = "";
        final String directoryOverride = "usethisdir";
        Mockito.when(mockDeviceInfoService.getApplicationCacheDir()).thenReturn(null);

        // Test
        final boolean isDeleted = cacheManager.deleteCacheFile(url, directoryOverride);

        // Verify
        assertFalse(isDeleted);

        // Reset
        
    }

    @Test
    public void testDeleteCachedFile_With_NoFilePresent() {
        // Setup
        final String url = "https://www.someurl.com";
        final String directoryOverride = "usethisdir";

        Mockito.when(mockDeviceInfoService.getApplicationCacheDir()).thenReturn(applicationBaseDirForTest);
        final File cacheDirectory = new File(applicationBaseDirForTest.getPath()+"/"+directoryOverride);
        final File unrelatedFile = new File(cacheDirectory.getPath(), "somethingElse.1484711165000_someETag");
        createAndWriteRandomContentToFile(cacheDirectory, unrelatedFile);

        // Test
        final boolean isDeleted = cacheManager.deleteCacheFile(url, directoryOverride);

        // Verify
        assertFalse(isDeleted);

        
    }

    @Test
    public void testDeleteCachedDataForURL_FilePresent() {
        // Setup
        final String url = "https://www.someurl.com";
        final String directoryOverride = "usethisdir";
        Mockito.when(mockDeviceInfoService.getApplicationCacheDir()).thenReturn(applicationBaseDirForTest);
        final String expectedFileName =
                StringEncoder.sha2hash(url)
                + "."
                + "1484711165000"
                + "."
                + "someETag";

        final File cacheDirectory = new File(applicationBaseDirForTest.getPath()+"/"+directoryOverride);
        final File fileForURL = new File(cacheDirectory.getPath(), expectedFileName);
        createAndWriteRandomContentToFile(cacheDirectory, fileForURL);

        // Test
        final boolean isDeleted = cacheManager.deleteCacheFile(url, directoryOverride);
        // Verify
        assertTrue( isDeleted);

        
    }

    @Test
    public void testDeleteCachedDataForURL_PartialFilePresent() {
        // Setup
        final String url = "https://www.someurl.com";
        final String directoryOverride = "usethisdir";
        Mockito.when(mockDeviceInfoService.getApplicationCacheDir()).thenReturn(applicationBaseDirForTest);
        final String expectedFileName = StringEncoder.sha2hash(url)
                        + "."
                        + "1484711165000"
                        + "."
                        + "someETag"
                        + "_partial";

        final File cacheDirectory = new File(applicationBaseDirForTest.getPath()+"/"+directoryOverride);
        final File fileForURL = new File(cacheDirectory.getPath(), expectedFileName);
        createAndWriteRandomContentToFile(cacheDirectory, fileForURL);

        // Test
        final boolean isDeleted = cacheManager.deleteCacheFile(url, directoryOverride);
        // Verify
        assertTrue( isDeleted);

        
    }

    @Test
    public void testDeleteCachedDataForURL_NoDirectoryOverride_NullURL() {
        // Setup
        Mockito.when(mockDeviceInfoService.getApplicationCacheDir()).thenReturn(applicationBaseDirForTest);
        final File cacheDirectory = new File(applicationBaseDirForTest.getPath());
        final File unrelatedFile = new File(cacheDirectory.getPath(), "somethingElse.1484711165000_someETag");
        createAndWriteRandomContentToFile(cacheDirectory, unrelatedFile);


        // Test
        final boolean isDeleted = cacheManager.deleteCacheFile(null, null);

        // Verify
        assertFalse(isDeleted);

        
    }

    @Test
    public void testDeleteCachedDataForURL_NoDirectoryOverride_EmptyURL() {
        // Setup
        Mockito.when(mockDeviceInfoService.getApplicationCacheDir()).thenReturn(applicationBaseDirForTest);
        final File cacheDirectory = new File(applicationBaseDirForTest.getPath());
        final File unrelatedFile = new File(cacheDirectory.getPath(), "somethingElse.1484711165000_someETag");
        createAndWriteRandomContentToFile(cacheDirectory, unrelatedFile);

        // Test
        final boolean isDeleted = cacheManager.deleteCacheFile("", null);

        // Verify
        assertFalse(isDeleted);

        
    }

    @Test
    public void testDeleteCachedDataForURL_NoDirectoryOverride_NullApplicationCacheDir() {
        // Setup
        Mockito.when(mockDeviceInfoService.getApplicationCacheDir()).thenReturn(null);
        final String url = "https://www.someurl.com";

        // Test
        final boolean isDeleted = cacheManager.deleteCacheFile(url, null);

        // Verify
        assertFalse(isDeleted);
    }

    @Test
    public void testDeleteCachedDataForURL_NoDirectoryOverride_NoFilePresent() {
        // Setup
        Mockito.when(mockDeviceInfoService.getApplicationCacheDir()).thenReturn(applicationBaseDirForTest);
        final String url = "https://www.someurl.com";
        final File cacheDirectory = new File(applicationBaseDirForTest.getPath()+ "/"+ CACHE_DIRECTORY);
        final File unrelatedFile = new File(cacheDirectory.getPath(), "somethingElse.1484711165000_someETag");
        createAndWriteRandomContentToFile(cacheDirectory, unrelatedFile);

        // Test
        final boolean isDeleted = cacheManager.deleteCacheFile(url, null);

        // Verify
        assertFalse(isDeleted);
    }

    @Test
    public void testDeleteCachedDataForURL_NoDirectoryOverride_FilePresent() {
        // Setup
        final String url = "https://www.someurl.com";
        Mockito.when(mockDeviceInfoService.getApplicationCacheDir()).thenReturn(applicationBaseDirForTest);
        final File cacheDirectory = new File(applicationBaseDirForTest.getPath()+ "/"+ CACHE_DIRECTORY);
        final String expectedFileName =
                StringEncoder.sha2hash(url)
                        + "."
                        + "1484711165000"
                        + "."
                        + "someETag"
                        +"_partial";
        final File validCacheFile = new File(cacheDirectory.getPath(), expectedFileName);
        createAndWriteRandomContentToFile(cacheDirectory, validCacheFile);
        // Test
        final boolean isDeleted = cacheManager.deleteCacheFile(url, null);
        // Verify
        assertTrue(isDeleted);
    }

    @Test
    public void testGetCacheFile_With_NullURL() {
        // Setup
        final String url = null ;
        Mockito.when(mockDeviceInfoService.getApplicationCacheDir()).thenReturn(applicationBaseDirForTest);

        // Test
        final File cachedFile = cacheManager.getCacheFile(url, null, false);
        // Verify
        assertNull("cached file should be null", cachedFile);

        
    }

    @Test
    public void testGetCacheFile_With_EmptyURL() {
        // Setup
        final String url = "";
        Mockito.when(mockDeviceInfoService.getApplicationCacheDir()).thenReturn(applicationBaseDirForTest);
        // Test
        final File cachedFile = cacheManager.getCacheFile(url, null, false);
        // Verify
        assertNull("cached file should be null", cachedFile);

            }

    @Test
    public void testGetCacheFile_NullCacheDirectory() {
        // Setup
        final String url = "https://www.someurl.com";
        Mockito.when(mockDeviceInfoService.getApplicationCacheDir()).thenReturn(null);
        // Test
        final File cachedFile = cacheManager.getCacheFile(url, null, false);
        // Verify
        assertNull("cached file should be null", cachedFile);
    }

    @Test
    public void testGetCacheFile_NoCacheFilesExists() {
        // Setup
        final String url = "https://www.someurl.com";

        Mockito.when(mockDeviceInfoService.getApplicationCacheDir()).thenReturn(applicationBaseDirForTest);
        final File cacheDirectory = new File(applicationBaseDirForTest.getPath()+ "/"+ CACHE_DIRECTORY);
        final File unrelatedFile = new File(cacheDirectory.getPath(), "somethingElse.1484711165000_someETag");
        createAndWriteRandomContentToFile(cacheDirectory, unrelatedFile);

        // Test
        final File cachedFile = cacheManager.getCacheFile(url, null, false);
        // Verify
        assertNull(cachedFile);
    }

    @Test
    public void testGetCacheFile_CacheFilesExists() {
        // Setup
        final String url = "https://www.someurl.com";
        Mockito.when(mockDeviceInfoService.getApplicationCacheDir()).thenReturn(applicationBaseDirForTest);
        final File cacheDirectory = new File(applicationBaseDirForTest.getPath()+ "/"+ CACHE_DIRECTORY);
        final String expectedFileName =
                StringEncoder.sha2hash(url)
                        + "."
                        + "1484711165000"
                        + "."
                        + "someETag";
        final File validCacheFile = new File(cacheDirectory.getPath(), expectedFileName);
        createAndWriteRandomContentToFile(cacheDirectory, validCacheFile);

        // Test
        final File cachedFile = cacheManager.getCacheFile(url, null, false);
        // Verify
        assertNotNull(cachedFile);

        deleteDirContent(applicationBaseDirForTest);
    }

    @Test
    public void testGetCacheFile_PartialCacheFilesExists_DoNotIgnorePartial() {
        // Setup
        final String url = "https://www.someurl.com";
        Mockito.when(mockDeviceInfoService.getApplicationCacheDir()).thenReturn(applicationBaseDirForTest);
        final File cacheDirectory = new File(applicationBaseDirForTest.getPath()+ "/"+ CACHE_DIRECTORY);

        final String expectedFileName =
                StringEncoder.sha2hash(url)
                        + "."
                        + "1484711165000"
                        + "."
                        + "someETag"
                        +"_partial";

        final File validCacheFile = new File(cacheDirectory.getPath(), expectedFileName);
        createAndWriteRandomContentToFile(cacheDirectory, validCacheFile);

        // Test
        final File cachedFile = cacheManager.getCacheFile(url, null, false);
        // Verify
        assertNotNull(cachedFile);
    }

    @Test
    public void testGetCacheFile_PartialCacheFilesExists_IgnorePartial() {
        // Setup
        final String url = "https://www.someurl.com";
        Mockito.when(mockDeviceInfoService.getApplicationCacheDir()).thenReturn(applicationBaseDirForTest);
        final File cacheDirectory = new File(applicationBaseDirForTest.getPath()+ "/"+ CACHE_DIRECTORY);
        final String expectedFileName =
                StringEncoder.sha2hash(url)
                        + "."
                        + "1484711165000"
                        + "."
                        + "someETag"
                        +"_partial";
        final File validCacheFile = new File(cacheDirectory.getPath(), expectedFileName);
        createAndWriteRandomContentToFile(cacheDirectory, validCacheFile);

        // Test
        final File cachedFile = cacheManager.getCacheFile(url, null, true);
        // Verify
        assertNull(cachedFile);
    }

    @Test
    public void testGetCacheFile_CustomCacheDir_CacheFilesExistsInDifferentDirectory() {
        // Setup
        final String url = "https://www.someurl.com";
        final String cacheDirOverride = "usethisdir";
        Mockito.when(mockDeviceInfoService.getApplicationCacheDir()).thenReturn(applicationBaseDirForTest);
        final File cacheDirectory = new File(applicationBaseDirForTest.getPath()+ "/"+ cacheDirOverride+"/subDir");
        final String irrelevantCacheFileName =
                StringEncoder.sha2hash(url)
                        + "."
                        + "1484711165000"
                        + "."
                        + "someETag"
                        +"_partial";
        final File irrelevantCacheFile = new File(cacheDirectory.getPath(), irrelevantCacheFileName);
        createAndWriteRandomContentToFile(cacheDirectory, irrelevantCacheFile);

        // Test
        final File cachedFile = cacheManager.getCacheFile(url, cacheDirOverride, false);
        // Verify
        assertNull(cachedFile);
    }

    @After
    public void teardown() {
        deleteDirContent(applicationBaseDirForTest);
    }

    /**
     * Removes the cache directory recursively
     */
    void deleteDirContent(final File dir) {
        File[] allContents = dir.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirContent(file);
            }
        }
        dir.delete();
    }

    private void createAndWriteRandomContentToFile(final File directory, final File file) {
        directory.mkdirs();
        try {
            file.createNewFile();
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write("SomeRandomContent");
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException ex) {}

    }
}