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

package com.adobe.marketing.mobile.launch.rulesengine.download;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.adobe.marketing.mobile.internal.util.StringEncoder;
import com.adobe.marketing.mobile.services.DeviceInforming;
import com.adobe.marketing.mobile.services.ServiceProvider;
import com.adobe.marketing.mobile.test.util.FileTestHelper;
import com.adobe.marketing.mobile.util.StreamUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class RulesZipProcessingHelperTest {

    private static final String TEST_CACHE_KEY = "test_cache_key";

    @Mock private DeviceInforming mockDeviceInfoService;

    @Mock private ServiceProvider mockServiceProvider;

    private MockedStatic<ServiceProvider> mockedStaticServiceProvider;

    private File mockCacheDir;
    private File mockRulesZip;
    private RulesZipProcessingHelper rulesZipProcessingHelper;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        mockCacheDir =
                new File(
                        this.getClass().getClassLoader().getResource("").getPath()
                                + File.separator
                                + "TestAppCache");

        mockedStaticServiceProvider = Mockito.mockStatic(ServiceProvider.class);
        mockedStaticServiceProvider
                .when(ServiceProvider::getInstance)
                .thenReturn(mockServiceProvider);

        when(mockDeviceInfoService.getApplicationCacheDir()).thenReturn(mockCacheDir);
        when(mockServiceProvider.getDeviceInfoService()).thenReturn(mockDeviceInfoService);
        when(mockDeviceInfoService.getApplicationCacheDir()).thenReturn(mockCacheDir);

        rulesZipProcessingHelper = new RulesZipProcessingHelper();
    }

    @Test
    public void testCreateTemporaryRulesDirectory_NullKey() {
        assertFalse(rulesZipProcessingHelper.createTemporaryRulesDirectory(null));
    }

    @Test
    public void testCreateTemporaryRulesDirectory_EmptyKey() {
        assertFalse(rulesZipProcessingHelper.createTemporaryRulesDirectory("  "));
        assertFalse(rulesZipProcessingHelper.createTemporaryRulesDirectory(""));
    }

    @Test
    public void testCreateTemporaryRulesDirectory_Successful() {
        assertTrue(rulesZipProcessingHelper.createTemporaryRulesDirectory(TEST_CACHE_KEY));
    }

    @Test
    public void testStoreRulesInTemporaryDirectory_NullKey() {
        assertFalse(
                rulesZipProcessingHelper.storeRulesInTemporaryDirectory(
                        null, mock(InputStream.class)));
    }

    @Test
    public void testStoreRulesInTemporaryDirectory_EmptyKey() {
        assertFalse(
                rulesZipProcessingHelper.storeRulesInTemporaryDirectory(
                        "  ", mock(InputStream.class)));
        assertFalse(
                rulesZipProcessingHelper.storeRulesInTemporaryDirectory(
                        "", mock(InputStream.class)));
    }

    @Test
    public void testStoreRulesInTemporaryDirectory_NoTempDirCreated() throws FileNotFoundException {
        final String assetName = "rules_zip_happy/ADBMobileConfig-rules.zip";
        mockRulesZip = getResourceFile(assetName);

        assertFalse(
                rulesZipProcessingHelper.storeRulesInTemporaryDirectory(
                        TEST_CACHE_KEY, new FileInputStream(mockRulesZip)));
    }

    @Test
    public void testStoreRulesInTemporaryDirectory_Success() throws FileNotFoundException {
        // Create a temp dir for the key
        assertTrue(rulesZipProcessingHelper.createTemporaryRulesDirectory(TEST_CACHE_KEY));

        final String assetName = "rules_zip_happy/ADBMobileConfig-rules.zip";
        mockRulesZip = getResourceFile(assetName);

        assertTrue(
                rulesZipProcessingHelper.storeRulesInTemporaryDirectory(
                        TEST_CACHE_KEY, new FileInputStream(mockRulesZip)));
    }

    @Test
    public void testStoreRulesInTemporaryDirectory_FileCannotBeRead() throws IOException {
        // Create a temp dir for the key
        assertTrue(rulesZipProcessingHelper.createTemporaryRulesDirectory(TEST_CACHE_KEY));

        InputStream mockInputStream = mock(InputStream.class);
        when(mockInputStream.read(any())).thenThrow(new SecurityException());

        assertFalse(
                rulesZipProcessingHelper.storeRulesInTemporaryDirectory(
                        TEST_CACHE_KEY, mockInputStream));
    }

    @Test
    public void testUnZipRules_NoFileToExtractInTempDir() {
        assertNull(rulesZipProcessingHelper.unzipRules(TEST_CACHE_KEY));
    }

    @Test
    public void testUnZipRules_UnExtractableZip() throws FileNotFoundException {
        final String assetName = "rules_zip_invalid/ADBMobileConfig-rules.zip";
        mockRulesZip = getResourceFile(assetName);

        // Create a temp dir for the key
        assertTrue(rulesZipProcessingHelper.createTemporaryRulesDirectory(TEST_CACHE_KEY));
        // Copy invalid zip file
        assertTrue(
                rulesZipProcessingHelper.storeRulesInTemporaryDirectory(
                        TEST_CACHE_KEY, new FileInputStream(mockRulesZip)));

        assertNull(rulesZipProcessingHelper.unzipRules(TEST_CACHE_KEY));
    }

    @Test
    public void testUnZipRules_NoRulesJsonInZip() throws FileNotFoundException {
        final String assetName = "rules_zip_invalid_content/ADBMobileConfig-rules.zip";
        mockRulesZip = getResourceFile(assetName);

        // Create a temp dir for the key
        assertTrue(rulesZipProcessingHelper.createTemporaryRulesDirectory(TEST_CACHE_KEY));
        // Copy invalid zip file
        assertTrue(
                rulesZipProcessingHelper.storeRulesInTemporaryDirectory(
                        TEST_CACHE_KEY, new FileInputStream(mockRulesZip)));

        assertNull(rulesZipProcessingHelper.unzipRules(TEST_CACHE_KEY));
    }

    @Test
    public void testUnZipRules_ValidZip() throws FileNotFoundException {
        final String assetName = "rules_zip_happy/ADBMobileConfig-rules.zip";
        mockRulesZip = getResourceFile(assetName);

        // Create a temp dir for the key
        assertTrue(rulesZipProcessingHelper.createTemporaryRulesDirectory(TEST_CACHE_KEY));
        // Copy invalid zip file
        assertTrue(
                rulesZipProcessingHelper.storeRulesInTemporaryDirectory(
                        TEST_CACHE_KEY, new FileInputStream(mockRulesZip)));

        assertEquals(
                StreamUtils.readAsString(
                        new FileInputStream(
                                getResourceFile("rules_zip_happy/expected_rules.json"))),
                rulesZipProcessingHelper.unzipRules(TEST_CACHE_KEY));
    }

    @Test
    public void testGetTemporaryDirectory() {
        final String expectedTempDirPath =
                mockCacheDir.getPath()
                        + File.separator
                        + RulesZipProcessingHelper.TEMP_DOWNLOAD_DIR
                        + File.separator
                        + StringEncoder.sha2hash(TEST_CACHE_KEY);

        assertEquals(
                rulesZipProcessingHelper.getTemporaryDirectory(TEST_CACHE_KEY).getPath(),
                expectedTempDirPath);
    }

    @Test
    public void testDeleteTemporaryDirectory() {
        rulesZipProcessingHelper.createTemporaryRulesDirectory(TEST_CACHE_KEY);

        final String expectedTempDirPath =
                mockCacheDir.getPath()
                        + File.separator
                        + RulesZipProcessingHelper.TEMP_DOWNLOAD_DIR
                        + File.separator
                        + StringEncoder.sha2hash(TEST_CACHE_KEY);
        assertTrue(new File(expectedTempDirPath).exists());

        rulesZipProcessingHelper.deleteTemporaryDirectory(TEST_CACHE_KEY);
        assertFalse(new File(expectedTempDirPath).exists());
    }

    @After
    public void tearDown() {
        if (mockCacheDir != null) {
            mockCacheDir.setWritable(true);
            mockCacheDir.setReadable(true);
        }

        if (mockRulesZip != null) {
            mockRulesZip.setReadable(true);
            mockRulesZip.setWritable(true);
        }

        FileTestHelper.deleteFile(mockCacheDir, true);
        mockedStaticServiceProvider.close();
    }

    private File getResourceFile(final String zipFileResourcePath) {
        try {
            return new File(
                    this.getClass().getClassLoader().getResource(zipFileResourcePath).getPath());
        } catch (final Exception e) {
            return null;
        }
    }
}
