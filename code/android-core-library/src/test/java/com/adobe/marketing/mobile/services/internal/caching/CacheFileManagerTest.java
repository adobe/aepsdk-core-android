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

package com.adobe.marketing.mobile.services.internal.caching;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import com.adobe.marketing.mobile.internal.util.StringEncoder;
import com.adobe.marketing.mobile.services.DeviceInforming;
import com.adobe.marketing.mobile.services.ServiceProvider;
import com.adobe.marketing.mobile.services.caching.CacheEntry;
import com.adobe.marketing.mobile.services.caching.CacheExpiry;
import com.adobe.marketing.mobile.test.util.FileTestHelper;
import com.adobe.marketing.mobile.util.StreamUtils;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class CacheFileManagerTest {

    @Mock private DeviceInforming mockDeviceInfoService;

    @Mock private ServiceProvider mockServiceProvider;

    private MockedStatic<ServiceProvider> mockedStaticServiceProvider;
    private File mockCacheDir;
    private File mockCacheBucket;

    private CacheFileManager cacheFileManager;
    private static final String TEST_CACHE_CONTENT = "This is sample cache content";
    private static final String TEST_CACHE_NAME = "JustARandomCacheBucket";
    private static final String TEST_CACHE_KEY = "JustARandomKey";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        mockCacheDir =
                new File(
                        this.getClass().getClassLoader().getResource("").getPath()
                                + File.separator
                                + "TestFolder");
        mockCacheDir.mkdirs();

        mockedStaticServiceProvider = Mockito.mockStatic(ServiceProvider.class);
        mockedStaticServiceProvider
                .when(ServiceProvider::getInstance)
                .thenReturn(mockServiceProvider);

        when(mockDeviceInfoService.getApplicationCacheDir()).thenReturn(mockCacheDir);
        when(mockServiceProvider.getDeviceInfoService()).thenReturn(mockDeviceInfoService);
        mockCacheBucket = createCacheBucket(FileCacheService.ROOT_CACHE_DIR_NAME, TEST_CACHE_NAME);

        cacheFileManager = new CacheFileManager(FileCacheService.ROOT_CACHE_DIR_NAME);
    }

    @Test
    public void testCreateCacheFile_Success() throws FileNotFoundException {
        final HashMap<String, String> metadata = new HashMap<>();
        metadata.put("One", "1");
        final CacheEntry cacheEntry =
                new CacheEntry(
                        new ByteArrayInputStream(
                                TEST_CACHE_CONTENT.getBytes(StandardCharsets.UTF_8)),
                        CacheExpiry.at(new Date(500L)),
                        metadata);

        final HashMap<String, String> expectedCacheMetadata = new HashMap<>();
        expectedCacheMetadata.put(FileCacheResult.METADATA_KEY_EXPIRY_IN_MILLIS, "500");
        expectedCacheMetadata.put(
                FileCacheResult.METADATA_KEY_PATH_TO_FILE,
                cacheFileManager.getCacheLocation(TEST_CACHE_NAME, TEST_CACHE_KEY));

        expectedCacheMetadata.putAll(metadata);
        final String expectedCacheFileLocation =
                mockCacheBucket.getPath() + File.separator + StringEncoder.sha2hash(TEST_CACHE_KEY);
        final String expectedCacheMetadataPath = expectedCacheFileLocation + "_metadata.txt";

        Assert.assertTrue(
                cacheFileManager.createCacheFile(TEST_CACHE_NAME, TEST_CACHE_KEY, cacheEntry));

        assertTrue(new File(expectedCacheFileLocation).exists());
        assertTrue(new File(expectedCacheMetadataPath).exists());
        assertEquals(
                TEST_CACHE_CONTENT,
                StreamUtils.readAsString(new FileInputStream(expectedCacheFileLocation)));
        assertEquals(
                new JSONObject(expectedCacheMetadata).toString(),
                StreamUtils.readAsString(new FileInputStream(expectedCacheMetadataPath)));
    }

    @Test
    public void testCreateCacheFile_EmptyCacheName() {
        final HashMap<String, String> metadata = new HashMap<>();
        metadata.put("One", "1");
        final CacheEntry cacheEntry =
                new CacheEntry(
                        new ByteArrayInputStream(
                                TEST_CACHE_CONTENT.getBytes(StandardCharsets.UTF_8)),
                        CacheExpiry.at(new Date(500L)),
                        metadata);

        Assert.assertFalse(cacheFileManager.createCacheFile("", TEST_CACHE_KEY, cacheEntry));
    }

    @Test
    public void testCreateCacheFile_EmptyCacheKey() {
        final HashMap<String, String> metadata = new HashMap<>();
        metadata.put("One", "1");
        final CacheEntry cacheEntry =
                new CacheEntry(
                        new ByteArrayInputStream(
                                TEST_CACHE_CONTENT.getBytes(StandardCharsets.UTF_8)),
                        CacheExpiry.at(new Date(500L)),
                        metadata);

        Assert.assertFalse(cacheFileManager.createCacheFile(TEST_CACHE_NAME, " ", cacheEntry));
    }

    @Test
    public void testCreateCacheFile_MetadataSaveFails() throws IOException {
        final HashMap<String, String> metadata = new HashMap<>();
        metadata.put("One", "1");
        final CacheEntry cacheEntry =
                new CacheEntry(
                        new ByteArrayInputStream(
                                TEST_CACHE_CONTENT.getBytes(StandardCharsets.UTF_8)),
                        CacheExpiry.at(new Date(500L)),
                        metadata);

        // Simulate metadata file not being writable.
        final String expectedCacheFileLocation =
                mockCacheBucket.getPath() + File.separator + StringEncoder.sha2hash(TEST_CACHE_KEY);
        final File expectedMetadataFile = new File(expectedCacheFileLocation + "_metadata.txt");
        Files.write(expectedMetadataFile.toPath(), "".getBytes(StandardCharsets.UTF_8));
        expectedMetadataFile.setWritable(false);

        // Test
        Assert.assertFalse(
                cacheFileManager.createCacheFile(TEST_CACHE_NAME, TEST_CACHE_KEY, cacheEntry));

        // Verify that the cache file is also deleted.
        Assert.assertFalse(new File(expectedCacheFileLocation).exists());

        // reset
        expectedMetadataFile.setWritable(true);
    }

    @Test
    public void testGetCacheFile_EmptyKey() {
        final File fetchedCacheFile = cacheFileManager.getCacheFile(TEST_CACHE_NAME, "");
        Assert.assertNull(fetchedCacheFile);
    }

    @Test
    public void testGetCacheFile_EmptyCacheName() {
        final File fetchedCacheFile = cacheFileManager.getCacheFile(" ", TEST_CACHE_KEY);
        Assert.assertNull(fetchedCacheFile);
    }

    @Test
    public void testGetCacheFile_EntryExists() throws FileNotFoundException {
        final HashMap<String, String> metadata = new HashMap<>();
        metadata.put("One", "1");
        final CacheEntry cacheEntry =
                new CacheEntry(
                        new ByteArrayInputStream(
                                TEST_CACHE_CONTENT.getBytes(StandardCharsets.UTF_8)),
                        CacheExpiry.at(new Date(500L)),
                        metadata);
        metadata.put("One", "1");
        Assert.assertTrue(
                cacheFileManager.createCacheFile(TEST_CACHE_NAME, TEST_CACHE_KEY, cacheEntry));

        final File fetchedCacheFile =
                cacheFileManager.getCacheFile(TEST_CACHE_NAME, TEST_CACHE_KEY);
        Assert.assertNotNull(fetchedCacheFile);
        Assert.assertEquals(
                TEST_CACHE_CONTENT,
                StreamUtils.readAsString(new FileInputStream(fetchedCacheFile)));
    }

    @Test
    public void testGetCacheFile_EntryDoesNotExist() {
        final File fetchedCacheFile =
                cacheFileManager.getCacheFile(TEST_CACHE_NAME, TEST_CACHE_KEY);
        Assert.assertNull(fetchedCacheFile);
    }

    @Test
    public void testGetMetadata_EmptyKey() {
        final Map<String, String> metadata = cacheFileManager.getCacheMetadata(TEST_CACHE_NAME, "");
        assertNull(metadata);
    }

    @Test
    public void testGetMetadata_EmptyCacheName() {
        final Map<String, String> metadata = cacheFileManager.getCacheMetadata(" ", TEST_CACHE_KEY);
        assertNull(metadata);
    }

    @Test
    public void testGetMetadata_NoMetadataFile() {
        final Map<String, String> metadata =
                cacheFileManager.getCacheMetadata(TEST_CACHE_NAME, TEST_CACHE_KEY);
        assertNull(metadata);
    }

    @Test
    public void testGetMetadata_MetadataIsMalformed() throws IOException {
        // Add an entry to create metadata.
        final HashMap<String, String> metadata = new HashMap<>();
        metadata.put("One", "1");
        final CacheEntry cacheEntry =
                new CacheEntry(
                        new ByteArrayInputStream(
                                TEST_CACHE_CONTENT.getBytes(StandardCharsets.UTF_8)),
                        CacheExpiry.at(new Date(500L)),
                        metadata);
        Assert.assertTrue(
                cacheFileManager.createCacheFile(TEST_CACHE_NAME, TEST_CACHE_KEY, cacheEntry));

        final File expectedMetadataFile =
                new File(
                        mockCacheBucket.getPath()
                                + File.separator
                                + StringEncoder.sha2hash(TEST_CACHE_KEY)
                                + "_metadata.txt");
        // Overwrite with corrupt metadata
        Files.write(
                expectedMetadataFile.toPath(),
                "Some content that is not json".getBytes(StandardCharsets.UTF_8));

        // Test
        final Map<String, String> retrievedMetadata =
                cacheFileManager.getCacheMetadata(TEST_CACHE_NAME, TEST_CACHE_KEY);

        assertNull(retrievedMetadata);
    }

    @Test
    public void testGetMetadata_MetadataIsPresent() {
        // Add an entry to create metadata.
        final HashMap<String, String> metadata = new HashMap<>();
        metadata.put("One", "1");
        final CacheEntry cacheEntry =
                new CacheEntry(
                        new ByteArrayInputStream(
                                TEST_CACHE_CONTENT.getBytes(StandardCharsets.UTF_8)),
                        CacheExpiry.at(new Date(500L)),
                        metadata);
        assertTrue(cacheFileManager.createCacheFile(TEST_CACHE_NAME, TEST_CACHE_KEY, cacheEntry));

        // Test
        final Map<String, String> retrievedMetadata =
                cacheFileManager.getCacheMetadata(TEST_CACHE_NAME, TEST_CACHE_KEY);

        assertNotNull(retrievedMetadata);
        assertEquals("1", retrievedMetadata.get("One"));
    }

    @Test
    public void testDeleteCacheFile_EmptyKey() {
        assertFalse(cacheFileManager.deleteCacheFile(TEST_CACHE_NAME, ""));
    }

    @Test
    public void testDeleteCacheFile_EmptyCacheName() {
        assertFalse(cacheFileManager.deleteCacheFile("", TEST_CACHE_KEY));
    }

    @Test
    public void testDeleteCacheFile_EntryPresent() {
        // Prepare - Add a cache entry.
        final HashMap<String, String> metadata = new HashMap<>();
        metadata.put("One", "1");
        final CacheEntry cacheEntry =
                new CacheEntry(
                        new ByteArrayInputStream(
                                TEST_CACHE_CONTENT.getBytes(StandardCharsets.UTF_8)),
                        CacheExpiry.at(new Date(500L)),
                        metadata);
        final File expectedCacheLocation =
                new File(
                        mockCacheBucket.getPath()
                                + File.separator
                                + StringEncoder.sha2hash(TEST_CACHE_KEY));
        assertTrue(cacheFileManager.createCacheFile(TEST_CACHE_NAME, TEST_CACHE_KEY, cacheEntry));
        assertNotNull(cacheFileManager.getCacheFile(TEST_CACHE_NAME, TEST_CACHE_KEY));
        assertTrue(expectedCacheLocation.exists());

        // Test
        assertTrue(cacheFileManager.deleteCacheFile(TEST_CACHE_NAME, TEST_CACHE_KEY));

        // Verify
        assertNull(cacheFileManager.getCacheFile(TEST_CACHE_NAME, TEST_CACHE_KEY));
        assertFalse(expectedCacheLocation.exists());
    }

    @Test
    public void testDeleteCacheFile_EntryNotPresent() {
        // Setup with no cache file
        assertNull(cacheFileManager.getCacheFile(TEST_CACHE_NAME, TEST_CACHE_KEY));

        // Test
        assertTrue(cacheFileManager.deleteCacheFile(TEST_CACHE_NAME, TEST_CACHE_KEY));
    }

    @After
    public void tearDown() throws Exception {
        FileTestHelper.deleteFile(mockCacheDir, true);
        mockedStaticServiceProvider.close();
    }

    private File createCacheBucket(final String cacheRoot, final String cacheName) {
        File cacheBucket =
                new File(
                        mockCacheDir.getPath()
                                + File.separator
                                + cacheRoot
                                + File.separator
                                + cacheName);
        cacheBucket.mkdirs();
        return cacheBucket;
    }
}
