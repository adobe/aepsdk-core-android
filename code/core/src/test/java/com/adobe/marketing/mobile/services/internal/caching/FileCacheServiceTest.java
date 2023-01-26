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

import static org.mockito.Mockito.when;

import com.adobe.marketing.mobile.services.DeviceInforming;
import com.adobe.marketing.mobile.services.ServiceProvider;
import com.adobe.marketing.mobile.services.caching.CacheEntry;
import com.adobe.marketing.mobile.services.caching.CacheExpiry;
import com.adobe.marketing.mobile.services.caching.CacheResult;
import com.adobe.marketing.mobile.test.util.FileTestHelper;
import com.adobe.marketing.mobile.util.StreamUtils;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class FileCacheServiceTest {

    @Mock private DeviceInforming mockDeviceInfoService;

    @Mock private ServiceProvider mockServiceProvider;

    private MockedStatic<ServiceProvider> mockedStaticServiceProvider;

    private static final String TEST_CACHE_NAME = "testCacheName";
    private static final String TEST_CACHE_KEY1 = "testCacheKey1";
    private static final String TEST_CACHE_KEY2 = "testCacheKey2";

    private FileCacheService fileCacheService;
    private File mockCacheDir;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        mockCacheDir =
                new File(
                        this.getClass().getClassLoader().getResource("").getPath()
                                + File.separator
                                + "TestCacheDir");
        mockCacheDir.mkdirs();

        mockedStaticServiceProvider = Mockito.mockStatic(ServiceProvider.class);
        mockedStaticServiceProvider
                .when(ServiceProvider::getInstance)
                .thenReturn(mockServiceProvider);

        when(mockDeviceInfoService.getApplicationCacheDir()).thenReturn(mockCacheDir);
        when(mockServiceProvider.getDeviceInfoService()).thenReturn(mockDeviceInfoService);

        fileCacheService = new FileCacheService();
    }

    @Test
    public void testSet_CacheNameIsEmpty() {
        final CacheEntry cacheEntry =
                new CacheEntry(
                        new ByteArrayInputStream("random content".getBytes(StandardCharsets.UTF_8)),
                        CacheExpiry.never(),
                        null);

        Assert.assertFalse(fileCacheService.set("", TEST_CACHE_KEY1, cacheEntry));
    }

    @Test
    public void testSet_CacheKeyIsEmpty() {
        final CacheEntry cacheEntry =
                new CacheEntry(
                        new ByteArrayInputStream("random content".getBytes(StandardCharsets.UTF_8)),
                        CacheExpiry.never(),
                        null);

        Assert.assertFalse(fileCacheService.set(TEST_CACHE_NAME, "", cacheEntry));
    }

    @Test
    public void testSet_CacheDirIsNotAccessible() {
        mockCacheDir.setReadable(false);
        mockCacheDir.setWritable(false);

        final CacheEntry cacheEntry =
                new CacheEntry(
                        new ByteArrayInputStream("random content".getBytes(StandardCharsets.UTF_8)),
                        CacheExpiry.never(),
                        null);

        Assert.assertFalse(fileCacheService.set(TEST_CACHE_NAME, TEST_CACHE_KEY1, cacheEntry));
    }

    @Test
    public void testSet_Success() {
        final CacheEntry cacheEntry =
                new CacheEntry(
                        new ByteArrayInputStream("random content".getBytes(StandardCharsets.UTF_8)),
                        CacheExpiry.never(),
                        null);

        Assert.assertTrue(fileCacheService.set(TEST_CACHE_NAME, TEST_CACHE_KEY1, cacheEntry));
    }

    @Test
    public void testGet_EmptyCacheKey() {
        Assert.assertNull(fileCacheService.get(TEST_CACHE_NAME, ""));
    }

    @Test
    public void testGet_EmptyCacheName() {
        Assert.assertNull(fileCacheService.get("", TEST_CACHE_KEY1));
    }

    @Test
    public void testGet_CacheEntryHasExpired() {
        final Date expiration = new Date(500L);
        final CacheEntry cacheEntry =
                new CacheEntry(
                        new ByteArrayInputStream("random content".getBytes(StandardCharsets.UTF_8)),
                        CacheExpiry.at(expiration),
                        null);
        Assert.assertTrue(fileCacheService.set(TEST_CACHE_NAME, TEST_CACHE_KEY1, cacheEntry));

        Assert.assertNull(fileCacheService.get(TEST_CACHE_NAME, TEST_CACHE_KEY1));
    }

    @Test
    public void testGet_Success() {
        final Date expiration = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(2));
        final HashMap<String, String> metadata = new HashMap<>();
        metadata.put("MetadataKey", "MetadataValue");
        final CacheEntry cacheEntry =
                new CacheEntry(
                        new ByteArrayInputStream("random content".getBytes(StandardCharsets.UTF_8)),
                        CacheExpiry.at(expiration),
                        metadata);
        Assert.assertTrue(fileCacheService.set(TEST_CACHE_NAME, TEST_CACHE_KEY1, cacheEntry));

        final CacheResult cacheResult = fileCacheService.get(TEST_CACHE_NAME, TEST_CACHE_KEY1);

        Assert.assertNotNull(cacheResult);
        Assert.assertEquals(expiration, cacheResult.getExpiry().getExpiration());
        Assert.assertNotNull(cacheResult.getMetadata());
        Assert.assertEquals(
                String.valueOf(expiration.getTime()),
                cacheResult.getMetadata().get(FileCacheResult.METADATA_KEY_EXPIRY_IN_MILLIS));
        Assert.assertEquals(cacheResult.getMetadata().get("MetadataKey"), "MetadataValue");
        Assert.assertEquals("random content", StreamUtils.readAsString(cacheResult.getData()));
    }

    @Test
    public void testGet_ReturnsCorrectValue() {
        final Date expiration1 = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(2));
        final HashMap<String, String> metadata1 = new HashMap<>();
        metadata1.put("MetadataKey", "MetadataValue");
        final CacheEntry cacheEntry1 =
                new CacheEntry(
                        new ByteArrayInputStream("random content".getBytes(StandardCharsets.UTF_8)),
                        CacheExpiry.at(expiration1),
                        metadata1);
        Assert.assertTrue(fileCacheService.set(TEST_CACHE_NAME, TEST_CACHE_KEY1, cacheEntry1));

        final Date expiration2 = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
        final HashMap<String, String> metadata2 = new HashMap<>();
        metadata2.put("AnotherMetadataKey", "AnotherMetadataValue");
        final CacheEntry cacheEntry2 =
                new CacheEntry(
                        new ByteArrayInputStream("random content".getBytes(StandardCharsets.UTF_8)),
                        CacheExpiry.at(expiration2),
                        metadata2);
        Assert.assertTrue(fileCacheService.set(TEST_CACHE_NAME, TEST_CACHE_KEY1, cacheEntry1));
        Assert.assertTrue(fileCacheService.set(TEST_CACHE_NAME, TEST_CACHE_KEY2, cacheEntry2));

        final CacheResult cacheResult = fileCacheService.get(TEST_CACHE_NAME, TEST_CACHE_KEY2);

        Assert.assertNotNull(cacheResult);
        Assert.assertEquals(expiration2, cacheResult.getExpiry().getExpiration());
        Assert.assertNotNull(cacheResult.getMetadata());
        Assert.assertEquals(
                String.valueOf(expiration2.getTime()),
                cacheResult.getMetadata().get(FileCacheResult.METADATA_KEY_EXPIRY_IN_MILLIS));
        Assert.assertEquals(
                "AnotherMetadataValue", cacheResult.getMetadata().get("AnotherMetadataKey"));
        Assert.assertEquals("random content", StreamUtils.readAsString(cacheResult.getData()));
    }

    @Test
    public void testSetAndGet_ValueIsCorrectlyOverwritten() {
        final Date expiration1 = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(2));
        final HashMap<String, String> metadata1 = new HashMap<>();
        metadata1.put("MetadataKey", "MetadataValue");
        final CacheEntry cacheEntry1 =
                new CacheEntry(
                        new ByteArrayInputStream("random content".getBytes(StandardCharsets.UTF_8)),
                        CacheExpiry.at(expiration1),
                        metadata1);
        Assert.assertTrue(fileCacheService.set(TEST_CACHE_NAME, TEST_CACHE_KEY1, cacheEntry1));

        final Date overwrittenExpiration =
                new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
        final HashMap<String, String> overwrittenMetadata = new HashMap<>();
        overwrittenMetadata.put("AnotherMetadataKey", "AnotherMetadataValue");
        final CacheEntry overwrittentCacheEntry =
                new CacheEntry(
                        new ByteArrayInputStream(
                                "random other content".getBytes(StandardCharsets.UTF_8)),
                        CacheExpiry.at(overwrittenExpiration),
                        overwrittenMetadata);

        Assert.assertTrue(
                fileCacheService.set(TEST_CACHE_NAME, TEST_CACHE_KEY2, overwrittentCacheEntry));

        final CacheResult cacheResult = fileCacheService.get(TEST_CACHE_NAME, TEST_CACHE_KEY2);

        Assert.assertNotNull(cacheResult);
        Assert.assertEquals(overwrittenExpiration, cacheResult.getExpiry().getExpiration());
        Assert.assertNotNull(cacheResult.getMetadata());
        Assert.assertEquals(
                String.valueOf(overwrittenExpiration.getTime()),
                cacheResult.getMetadata().get(FileCacheResult.METADATA_KEY_EXPIRY_IN_MILLIS));
        Assert.assertEquals(
                "AnotherMetadataValue", cacheResult.getMetadata().get("AnotherMetadataKey"));
        Assert.assertEquals(
                "random other content", StreamUtils.readAsString(cacheResult.getData()));
    }

    @Test
    public void testRemove_NoEntryExists() {
        Assert.assertTrue(fileCacheService.remove(TEST_CACHE_NAME, TEST_CACHE_KEY2));
    }

    @Test
    public void testRemove_EntryExists() {
        final Date expiration = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(2));
        final HashMap<String, String> metadata = new HashMap<>();
        metadata.put("MetadataKey", "MetadataValue");
        final CacheEntry cacheEntry =
                new CacheEntry(
                        new ByteArrayInputStream("random content".getBytes(StandardCharsets.UTF_8)),
                        CacheExpiry.at(expiration),
                        metadata);
        Assert.assertTrue(fileCacheService.set(TEST_CACHE_NAME, TEST_CACHE_KEY1, cacheEntry));

        final CacheResult cacheResult = fileCacheService.get(TEST_CACHE_NAME, TEST_CACHE_KEY1);
        Assert.assertNotNull(cacheResult);

        Assert.assertTrue(fileCacheService.remove(TEST_CACHE_NAME, TEST_CACHE_KEY1));
        Assert.assertNull(fileCacheService.get(TEST_CACHE_NAME, TEST_CACHE_KEY1));
    }

    @After
    public void tearDown() throws Exception {
        mockCacheDir.setReadable(true);
        mockCacheDir.setWritable(true);
        FileTestHelper.deleteFile(mockCacheDir, true);
        mockedStaticServiceProvider.close();
    }
}
