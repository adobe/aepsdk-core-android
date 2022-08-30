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

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.adobe.marketing.mobile.FileTestHelper.CACHE_DIRECTORY;
import static com.adobe.marketing.mobile.FileTestHelper.MOCK_FILE_NAME;
import static org.junit.Assert.*;

import com.adobe.marketing.mobile.internal.utility.StringEncoder;
import com.adobe.marketing.mobile.services.DeviceInforming;
import com.adobe.marketing.mobile.services.ServiceProvider;
@Ignore
public class CacheManagerTest {

	private CacheManager cacheManager;
	private MockSystemInfoService systemInfoService;
	private FileTestHelper fileTestHelper;
	@Mock
	private DeviceInforming mockDeviceInforming;

	@Before
	public void setup() throws MissingPlatformServicesException {
		FakePlatformServices platformServices = new FakePlatformServices();
		systemInfoService = platformServices.getMockSystemInfoService();
		cacheManager = new CacheManager(mockDeviceInforming);
		fileTestHelper = new FileTestHelper();
	}

	@After
	public void tearDown() {
		fileTestHelper.deleteTempCacheDirectory();
	}

	// ===============================================================
	// public CacheManager(final SystemInfoService systemInfoService)
	// ===============================================================
	@Test
	public void testCacheManager_Constructor_With_ValidInput() {
		// setup
		MissingPlatformServicesException exception = null;

		// test
		try {
			cacheManager = new CacheManager(mockDeviceInforming);
		} catch (MissingPlatformServicesException ex) {
			exception = ex;
		}

		// verify
		assertNotNull("cacheManager should not be null", cacheManager);
		assertNull("MissingPlatformServicesException should not be thrown", exception);
	}


	// ===============================================================
	// public CacheManager(final SystemInfoService systemInfoService)
	// ===============================================================
	@Test
	public void testCacheManager_Constructor_With_Null() {
		// setup
		MissingPlatformServicesException exception = null;

		// test
		try {
			cacheManager = new CacheManager(null);
		} catch (MissingPlatformServicesException ex) {
			exception = ex;
		}

		// verify
		assertNotNull("MissingPlatformServicesException should be thrown", exception);
	}

	// =================================================================================================================
	// File createNewCacheFile(final String url, final String etag, final String cacheDirectoryOverride, final Date lastModified)
	// =================================================================================================================
	@Test
	public void testCreateNewCacheFile_With_Etag_And_NullURL() {
		// Setup
		final String url = null;
		final String etag = "W/\"3a2-bMnM1spT5zNBH3xgDTaqZQ\"";
		final String cacheDir = "usethisdir";
		final Date date = new Date(1484711165000L);
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		// Test
		final File createdFile = cacheManager.createNewCacheFile(url, etag, cacheDir, date);
		// Verify
		assertNull("created file should be null", createdFile);
	}

	@Test
	public void testCreateNewCacheFile_With_Etag_And_NullDate() {
		// Setup
		final String url = "www.sample.com";
		final String etag = "12345ABCDE";
		final String cacheDir = "usethisdir";
		final Date date = null;
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		// Test
		final File createdFile = cacheManager.createNewCacheFile(url, etag, cacheDir, date);
		// Verify
		assertNull("created file should be null", createdFile);
	}

	@Test
	public void testCreateNewCacheFile_With_Etag_And_NullCacheDirectory() {
		// Setup
		final String url = "www.sample.com";
		final String etag = "12345ABCDE";
		final String cacheDir = "usethisdir";
		final Date date = new Date(1484711165000L);
		systemInfoService.applicationCacheDir = null;
		// Test
		final File createdFile = cacheManager.createNewCacheFile(url, etag, cacheDir, date);
		// Verify
		assertNull("created file should be null", createdFile);
	}

	@Test
	public void testCreateNewCacheFile_With_Etag_And_ValidParameter() throws IOException {
		// Setup
		final String url = "www.sample.com";
		final String etag = "W/\"3a2-bMnM1spT5zNBH3xgDTaqZQ\"";
		final String hexEtag = StringEncoder.getHexString(etag);
		final String cacheDir = "usethisdir";
		final Date date = new Date(1484711165000L);
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		// Test
		final File createdFile = cacheManager.createNewCacheFile(url, etag, cacheDir, date);
		// Verify
		assertNotNull("created file should not be null", createdFile);
		assertEquals("created file should have the given Absoulte Path",
					 fileTestHelper.getCacheDirectory(cacheDir) +
					 "/dd6681ae7411ac16cdc5756c483ec438b5512e26013715b76e8ae7587120486a." + hexEtag + ".1484711165000_partial",
					 createdFile.getPath());
		createdFile.deleteOnExit();
		assertTrue(createdFile.createNewFile());
	}

	// =================================================================================================================
	// File createNewCacheFile(final String url, final String cacheDirectoryOverride, final Date lastModified)
	// =================================================================================================================
	@Test
	public void testCreateNewCacheFile_With_NullURL() {
		// Setup
		final String url = null;
		final String cacheDir = "usethisdir";
		final Date date = new Date(1484711165000L);
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		// Test
		final File createdFile = cacheManager.createNewCacheFile(url, cacheDir, date);
		// Verify
		assertNull("created file should be null", createdFile);
	}

	@Test
	public void testCreateNewCacheFile_With_NullDate() {
		// Setup
		final String url = "www.sample.com";
		final String cacheDir = "usethisdir";
		final Date date = null;
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		// Test
		final File createdFile = cacheManager.createNewCacheFile(url, cacheDir, date);
		// Verify
		assertNull("created file should be null", createdFile);
	}

	@Test
	public void testCreateNewCacheFile_With_NullCacheDirectory() {
		// Setup
		final String url = "www.sample.com";
		final String cacheDir = "usethisdir";
		final Date date = new Date(1484711165000L);
		systemInfoService.applicationCacheDir = null;
		// Test
		final File createdFile = cacheManager.createNewCacheFile(url, cacheDir, date);
		// Verify
		assertNull("created file should be null", createdFile);
	}

	@Test
	public void testCreateNewCacheFile_With_ValidParameter() {
		// Setup
		final String url = "www.sample.com";
		final String cacheDir = "usethisdir";
		final Date date = new Date(1484711165000L);
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		// Test
		final File createdFile = cacheManager.createNewCacheFile(url, cacheDir, date);
		// Verify
		assertNotNull("created file should not be null", createdFile);
		assertEquals("created file should have the given Absoulte Path",
					 fileTestHelper.getCacheDirectory(cacheDir) +
					 "/dd6681ae7411ac16cdc5756c483ec438b5512e26013715b76e8ae7587120486a.1484711165000_partial",
					 createdFile.getPath());
	}

	// =================================================================================================================
	// File createNewCacheFile(final String url, final Date lastModified)
	// =================================================================================================================
	@Test
	public void testCreateNewCacheFile_With_NoCustomDirectoryNullURL() {
		// Setup
		final String url = null;
		final String eTag = "someETag";
		final Date date = new Date(1484711165000L);
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		// Test
		final File createdFile = cacheManager.createNewCacheFile(url, date);
		// Verify
		assertNull("created file should be null", createdFile);
	}

	@Test
	public void testCreateNewCacheFile_With_NoCustomDirectoryNullDate() {
		// Setup
		final String url = "www.sample.com";
		final String eTag = "someETag";
		final Date date = null;
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		// Test
		final File createdFile = cacheManager.createNewCacheFile(url, date);
		// Verify
		assertNull("created file should be null", createdFile);
	}

	@Test
	public void testCreateNewCacheFile_With_NoCustomDirectoryNullCacheDirectory() {
		// Setup
		final String url = "www.sample.com";
		final String eTag = "someETag";
		final Date date = new Date(1484711165000L);
		systemInfoService.applicationCacheDir = null;
		// Test
		final File createdFile = cacheManager.createNewCacheFile(url, date);
		// Verify
		assertNull("created file should be null", createdFile);
	}

	@Test
	public void testCreateNewCacheFile_With_NoCustomDirectoryValidParameter() {
		// Setup
		final String url = "www.sample.com";
		final String eTag = "someETag";
		final Date date = new Date(1484711165000L);
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		// Test
		final File createdFile = cacheManager.createNewCacheFile(url, date);
		// Verify
		assertNotNull("created file should not be null", createdFile);
		assertEquals("created file should have the given Absoulte Path",
					 fileTestHelper.getCacheDirectory() +
					 "/dd6681ae7411ac16cdc5756c483ec438b5512e26013715b76e8ae7587120486a.1484711165000_partial", createdFile.getPath());
	}

	// =================================================================================================================
	// boolean deleteCachedDataForURL(final String url, final String cacheDirectoryOverride)
	// =================================================================================================================
	@Test
	public void testDeleteCachedDataForURL_With_NullURL() {
		// Setup
		final String url = null;
		final String directoryOverride = "usethisdir";
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		// Test
		final boolean isDeleted = cacheManager.deleteCachedDataForURL(url, directoryOverride);
		// Verify
		assertFalse("the function should return false", isDeleted);
		// Reset
		fileTestHelper.deleteTempCacheDirectory(directoryOverride);
	}

	@Test
	public void testDeleteCachedDataForURL_With_EmptyURL() {
		// Setup
		final String url = "";
		final String directoryOverride = "usethisdir";
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		// Test
		final boolean isDeleted = cacheManager.deleteCachedDataForURL(url, directoryOverride);
		// Verify
		assertFalse("the function should return false", isDeleted);
		fileTestHelper.deleteTempCacheDirectory(directoryOverride);
	}

	@Test
	public void testDeleteCachedDataForURL_With_NullCacheDirectory() {
		// Setup
		final String url = "https://www.someurl.com";
		final String directoryOverride = "usethisdir";
		systemInfoService.applicationCacheDir = null;
		// Test
		final boolean isDeleted = cacheManager.deleteCachedDataForURL(url, directoryOverride);
		// Verify
		assertFalse("the function should return false", isDeleted);
		// Reset
		fileTestHelper.deleteTempCacheDirectory(directoryOverride);
	}

	@Test
	public void testDeleteCachedDataForURL_With_NoFilePresent() {
		// Setup
		final String url = "https://www.someurl.com";
		final String directoryOverride = "usethisdir";
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		fileTestHelper.placeOtherCacheFile(directoryOverride);
		// Test
		final boolean isDeleted = cacheManager.deleteCachedDataForURL(url, directoryOverride);
		// Verify
		assertFalse("the function should return false", isDeleted);
		// Reset
		fileTestHelper.deleteTempCacheDirectory(directoryOverride);
	}

	@Test
	public void testDeleteCachedDataForURL_With_FilePresent() {
		// Setup
		final String url = "https://www.someurl.com";
		final String directoryOverride = "usethisdir";
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		fileTestHelper.placeSampleCacheFile(directoryOverride);
		fileTestHelper.placeOtherCacheFile(directoryOverride);
		// Test
		final boolean isDeleted = cacheManager.deleteCachedDataForURL(url, directoryOverride);
		// Verify
		assertTrue("the function should return true", isDeleted);
		// Reset
		fileTestHelper.deleteTempCacheDirectory(directoryOverride);
	}

	// =================================================================================================================
	// boolean deleteCachedDataForURL(final String url, final String cacheDirectoryOverride)
	// =================================================================================================================
	@Test
	public void testDeleteCachedDataForURL_With_noDirectoryOverride_andNullURL() {
		// Setup
		final String url = null;
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		// Test
		final boolean isDeleted = cacheManager.deleteCachedDataForURL(url);
		// Verify
		assertFalse("the function should return false", isDeleted);
	}

	@Test
	public void testDeleteCachedDataForURL_With_noDirectoryOverride_andEmptyURL() {
		// Setup
		final String url = "";
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		// Test
		final boolean isDeleted = cacheManager.deleteCachedDataForURL(url);
		// Verify
		assertFalse("the function should return false", isDeleted);
	}

	@Test
	public void testDeleteCachedDataForURL_With_noDirectoryOverride_andNullCacheDirectory() {
		// Setup
		final String url = "https://www.someurl.com";
		systemInfoService.applicationCacheDir = null;
		// Test
		final boolean isDeleted = cacheManager.deleteCachedDataForURL(url);
		// Verify
		assertFalse("the function should return false", isDeleted);
	}

	@Test
	public void testDeleteCachedDataForURL_With_noDirectoryOverride_andNoFilePresent() {
		// Setup
		final String url = "https://www.someurl.com";
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		fileTestHelper.placeOtherCacheFile();
		// Test
		final boolean isDeleted = cacheManager.deleteCachedDataForURL(url);
		// Verify
		assertFalse("the function should return false", isDeleted);
	}

	@Test
	public void testDeleteCachedDataForURL_With_noDirectoryOverride_and_FilePresent() {
		// Setup
		final String url = "https://www.someurl.com";
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		fileTestHelper.placeSampleCacheFile();
		fileTestHelper.placeOtherCacheFile();
		// Test
		final boolean isDeleted = cacheManager.deleteCachedDataForURL(url);
		// Verify
		assertTrue("the function should return true", isDeleted);
	}

	// =================================================================================================================
	// protected File markFileAsCompleted(final File cacheFile)
	// =================================================================================================================
	@Test
	public void testMarkFileAsCompleted_When_FilePresent() {
		// Setup
		File renamedFile = cacheManager.markFileAsCompleted(fileTestHelper.placePartiallyDownloadedCacheFile());
		// Test
		assertEquals("the file should be renamed by removed _partial suffix",
					 fileTestHelper.getCacheDirectory() +
					 "/c0a6221b2b55775b6bc5761fdb1ac0c965cc823c55e8db0b3f903b24f82fcb90.1484711165000_someETag", renamedFile.getPath());
	}


	@Test
	public void testMarkFileAsCompleted_When_FileIsNull() {
		// Setup
		File renamedFile = cacheManager.markFileAsCompleted(null);
		// Test
		assertNull("the method should return null", renamedFile);
	}

	@Test
	public void testMarkFileAsCompleted_When_FileAlreadySetComplete() {
		// Setup
		File renamedFile = cacheManager.markFileAsCompleted(fileTestHelper.getCacheDirectory());
		// Test
		assertEquals("the method should the same file", renamedFile.getAbsolutePath(),
					 fileTestHelper.getCacheDirectory().getAbsolutePath());
	}

	// =================================================================================================================
	// File getFileForCachedURL(final String url, final String cacheDirectoryOverride, final boolean ignorePartial)
	// =================================================================================================================
	@Test
	public void testGetFileForCachedURL_With_NullURL() {
		// Setup
		final String url = null ;
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		// Test
		final File cachedFile = cacheManager.getFileForCachedURL(url, null, false);
		// Verify
		assertNull("cached file should be null", cachedFile);
	}

	@Test
	public void testGetFileForCachedURL_With_EmptyURL() {
		// Setup
		final String url = "";
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		// Test
		final File cachedFile = cacheManager.getFileForCachedURL(url, null, false);
		// Verify
		assertNull("cached file should be null", cachedFile);
	}

	@Test
	public void testGetFileForCachedURL_With_NullCacheDirectory() {
		// Setup
		final String url = "https://www.someurl.com";
		systemInfoService.applicationCacheDir = null;
		// Test
		final File cachedFile = cacheManager.getFileForCachedURL(url, null, false);
		// Verify
		assertNull("cached file should be null", cachedFile);
	}

	@Test
	public void testGetFileForCachedURL_With_NoCacheFilesExists() {
		// Setup
		final String url = "https://www.someurl.com";
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		// Test
		final File cachedFile = cacheManager.getFileForCachedURL(url, null, false);
		// Verify
		assertNull("cached file should be null", cachedFile);
	}

	@Test
	public void testGetFileForCachedURL_With_CacheFilesExists() {
		// Setup
		final String url = "https://www.someurl.com";
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		final File cacheDirectory = fileTestHelper.getCacheDirectory();
		fileTestHelper.placeSampleCacheFile();
		// Test
		final File cachedFile = cacheManager.getFileForCachedURL(url, null, false);
		// Verify
		assertNotNull("cached file should not be null", cachedFile);
		assertEquals("cached file should be equal to the compared file",
					 new File(cacheDirectory + File.separator + MOCK_FILE_NAME), cachedFile);
	}

	@Test
	public void testGetCachedFileForURL_With_CacheDirectoryExistsWithOtherFiles() {
		// Setup
		final String url = "https://www.someurl.com";
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		fileTestHelper.placeOtherCacheFile();
		// Test
		final File cachedFile = cacheManager.getFileForCachedURL(url, null, false);
		// Verify
		assertNull("cached file should be null", cachedFile);
	}

	@Test
	public void testGetCachedFileForURL_With_PartialFilePresent_IgnorePartial_False() {
		// Setup
		final String url = "https://www.someurl.com";
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		fileTestHelper.placePartiallyDownloadedCacheFile();
		// Test
		final File cachedFile = cacheManager.getFileForCachedURL(url, null, false);
		// Verify
		assertNotNull("cached file should not be null", cachedFile);
		assertEquals("cached file should be equal to the compared file",
					 new File(fileTestHelper.getCacheDirectory() + File.separator + MOCK_FILE_NAME + "_partial"), cachedFile);
	}

	@Test
	public void testGetCachedFileForURL_With_PartialFilePresent_IgnorePartial_True() {
		// Setup
		final String url = "https://www.someurl.com";
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		fileTestHelper.placePartiallyDownloadedCacheFile();
		// Test
		final File cachedFile = cacheManager.getFileForCachedURL(url, null, true);
		// Verify
		assertNull("cached file should be null", cachedFile);
	}

	@Test
	public void testGetCachedFileForURL_With_OverriddenCacheDirectory_then_shouldIgnoreMatchingFilesInTheWrongDirectory()
	throws Exception {
		// Setup
		final String url = "https://www.someurl.com";
		final String cacheDirOverride = "usethisdir";
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		fileTestHelper.placeSampleCacheFile();
		// Test
		final File cachedFile = cacheManager.getFileForCachedURL(url, cacheDirOverride, false);
		// Verify
		assertNull("cached file should be null because we are looking in a different directory", cachedFile);
	}

	@Test
	public void testGetCachedFileForURL_With_OverriddenCacheDirectory_then_shouldFindMatchingFilesInTheSpecifiedDirectory()
	throws Exception {
		// Setup
		final String url = "https://www.someurl.com";
		final String cacheDirOverride = "usethisdir";
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		fileTestHelper.placeSampleCacheFile(cacheDirOverride);
		// Test
		final File cachedFile = cacheManager.getFileForCachedURL(url, cacheDirOverride, false);
		// Verify
		assertNotNull("cached file should exist in specified directory", cachedFile);
		// Reset
		fileTestHelper.deleteTempCacheDirectory(cacheDirOverride);
	}

	@Test
	public void
	testGetCachedFileForURL_With_FilesAndDirectoriesInCacheDirectory_then_shouldNotFindMatchingFilesInTheSubDirectory()
	throws Exception {
		// Setup
		final String url = "https://www.someurl.com";
		final String cacheDirOverride = CACHE_DIRECTORY + File.separator + "usethisdir";
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		fileTestHelper.placeSampleCacheFile(cacheDirOverride);
		fileTestHelper.placeOtherCacheFile();
		// Test
		final File cachedFile = cacheManager.getFileForCachedURL(url, null, false);
		// Verify
		assertNull("cached file should not exist in specified directory", cachedFile);
		// Reset
		fileTestHelper.deleteTempCacheDirectory(cacheDirOverride);
	}

	@Test
	public void
	testGetCachedFileForURL_With_FilesAndDirectoriesInCacheDirectory_then_shouldFindMatchingFilesInSpecifiedSubDir()
	throws Exception {
		// Setup
		final String url = "https://www.someurl.com";
		final String cacheDirOverride = CACHE_DIRECTORY + File.separator + "usethisdir";
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		fileTestHelper.placeSampleCacheFile(cacheDirOverride);
		fileTestHelper.placeOtherCacheFile();
		// Test
		final File cachedFile = cacheManager.getFileForCachedURL(url, cacheDirOverride, false);
		// Verify
		assertNotNull("cached file should exist in specified directory", cachedFile);
		// Reset
		fileTestHelper.deleteTempCacheDirectory(cacheDirOverride);
	}

	// =================================================================================================================
	// File getFileForCachedURL(final String url, final boolean ignorePartial)
	// =================================================================================================================
	@Test
	public void testGetFileForCachedURL_With_NoCustomDirectoryNoCacheFilesExists() {
		// Setup
		final String url = "https://www.someurl.com";
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		// Test
		final File cachedFile = cacheManager.getFileForCachedURL(url, null, false);
		// Verify
		assertNull("cached file should be null", cachedFile);
	}

	@Test
	public void testGetFileForCachedURL_With_NoCustomDirectoryCacheFilesExists() {
		// Setup
		final String url = "https://www.someurl.com";
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		final File cacheDirectory = fileTestHelper.getCacheDirectory();
		fileTestHelper.placeSampleCacheFile();
		// Test
		final File cachedFile = cacheManager.getFileForCachedURL(url, null, false);
		// Verify
		assertNotNull("cached file should not be null", cachedFile);
		assertEquals("cached file should be equal to the compared file",
					 new File(cacheDirectory + File.separator + MOCK_FILE_NAME), cachedFile);
	}

	@Test
	public void testGetFileForCachedURL_With_InvalidCacheFilesExists() {
		// Setup
		final String url = "https://www.someurl.com";
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		fileTestHelper.placeInvalidCacheFile(null);
		// Test method call does NOT throw StringIndexOutOfBoundsException
		final File cachedFile = cacheManager.getFileForCachedURL(url, null, false);
		// Verify
		assertNull("cached file should be null", cachedFile);
	}

	// =================================================================================================================
	// void deleteFilesNotInList(final List<String> files, final String cacheDirectoryOverride)
	// =================================================================================================================
	@Test
	public void testDeleteFilesNotInList_With_OverriddenCacheDirectory_then_shouldDeleteAllFilesNotInTheList() throws
		Exception {
		// Setup
		final String url = "https://www.someurl.com";
		final String cacheDirOverride = "usethisdir";
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		fileTestHelper.placeSampleCacheFile(cacheDirOverride);
		fileTestHelper.placeOtherCacheFile(cacheDirOverride);
		final List<String> urls = new ArrayList<String>();
		urls.add(url);

		// pre-verify
		List<String> preFiles = fileTestHelper.getFilesInDirectory(cacheDirOverride);
		assertNotNull("directory should not be empty", preFiles);
		assertEquals("directory should have both files", 2, preFiles.size());

		// Test
		cacheManager.deleteFilesNotInList(urls, cacheDirOverride);

		// Verify
		List<String> files = fileTestHelper.getFilesInDirectory(cacheDirOverride);
		assertNotNull("directory should not be empty", files);
		assertEquals("directory should have no other files", 1, files.size());
		final String firstFile = files.get(0);
		assertEquals("sample file should still exist in directory",
					 "c0a6221b2b55775b6bc5761fdb1ac0c965cc823c55e8db0b3f903b24f82fcb90.1484711165000_someETag",
					 firstFile);

		// Reset
		fileTestHelper.deleteTempCacheDirectory(cacheDirOverride);
	}

	@Test
	public void testDeleteFilesNotInList_With_OverriddenCacheDirectoryNullList_then_shouldDeleteAllFilesInDirectory() throws
		Exception {
		// Setup
		final String url = "https://www.someurl.com";
		final String cacheDirOverride = "usethisdir";
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		fileTestHelper.placeSampleCacheFile(cacheDirOverride);
		fileTestHelper.placeOtherCacheFile(cacheDirOverride);

		// pre-verify
		List<String> preFiles = fileTestHelper.getFilesInDirectory(cacheDirOverride);
		assertNotNull("directory should not be empty", preFiles);
		assertEquals("directory should have both files", 2, preFiles.size());

		// Test
		cacheManager.deleteFilesNotInList(null, cacheDirOverride);

		// Verify
		List<String> files = fileTestHelper.getFilesInDirectory(cacheDirOverride);
		assertNull("directory should be empty", files);

		// Reset
		fileTestHelper.deleteTempCacheDirectory(cacheDirOverride);
	}

	// =================================================================================================================
	// void deleteFilesNotInList(final List<String> files, final String cacheDirectoryOverride, final boolean recursiveDelete)
	// =================================================================================================================
	@Test
	public void
	testDeleteFilesNotInList_With_OverriddenCacheDirectory_RecursiveFalse_then_shouldDeleteAllFilesNotInTheList() throws
		Exception {
		// Setup
		final String url = "https://www.someurl.com";
		final String cacheDirOverride = "usethisdir";
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		fileTestHelper.placeSampleCacheFile(cacheDirOverride);
		fileTestHelper.placeOtherCacheFile(cacheDirOverride);
		final List<String> urls = new ArrayList<String>();
		urls.add(url);

		// pre-verify
		List<String> preFiles = fileTestHelper.getFilesInDirectory(cacheDirOverride);
		assertNotNull("directory should not be empty", preFiles);
		assertEquals("directory should have both files", 2, preFiles.size());

		// Test
		cacheManager.deleteFilesNotInList(urls, cacheDirOverride, false);

		// Verify
		List<String> files = fileTestHelper.getFilesInDirectory(cacheDirOverride);
		assertNotNull("directory should not be empty", files);
		assertEquals("directory should have no other files", 1, files.size());
		final String firstFile = files.get(0);
		assertEquals("sample file should still exist in directory",
					 "c0a6221b2b55775b6bc5761fdb1ac0c965cc823c55e8db0b3f903b24f82fcb90.1484711165000_someETag",
					 firstFile);

		// Reset
		fileTestHelper.deleteTempCacheDirectory(cacheDirOverride);
	}

	@Test
	public void testDeleteFilesNotInList_With_NullOverriddenCacheDirectory_then_shouldDeleteFilesFromDefaultDirectory()
	throws Exception {
		// Setup
		final String url = "https://www.someurl.com";
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		fileTestHelper.placeSampleCacheFile(CACHE_DIRECTORY);
		fileTestHelper.placeOtherCacheFile(CACHE_DIRECTORY);

		final List<String> urls = new ArrayList<String>();
		urls.add(url);

		// Test
		cacheManager.deleteFilesNotInList(urls, null, false);

		// Verify
		List<String> files = fileTestHelper.getFilesInDirectory(CACHE_DIRECTORY);
		assertNotNull("directory should not be empty", files);
		assertEquals("directory should have no other files", 1, files.size());
		final String firstFile = files.get(0);
		assertEquals("sample file should still exist in directory",
					 "c0a6221b2b55775b6bc5761fdb1ac0c965cc823c55e8db0b3f903b24f82fcb90.1484711165000_someETag",
					 firstFile);
	}

	@Test
	public void
	testDeleteFilesNotInList_With_OverriddenCacheDirectory_RecursiveDelete_then_shouldDeleteFilesAndDirectories() throws
		Exception {
		// Setup
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		fileTestHelper.placeSampleCacheFile(CACHE_DIRECTORY);
		fileTestHelper.placeOtherCacheFile(CACHE_DIRECTORY);
		fileTestHelper.placeSampleCacheDirectory("MY_TEST_DIRECTORY", "testFileName");
		final List<String> urls = new ArrayList<String>();
		urls.add("https://www.someurl.com");

		// pre-verify
		List<String> preFiles = fileTestHelper.getFilesInDirectory(CACHE_DIRECTORY);
		assertNotNull("directory should not be empty", preFiles);
		assertEquals("directory should have both files", 3, preFiles.size());

		// Test
		cacheManager.deleteFilesNotInList(urls, CACHE_DIRECTORY, true);

		// Verify
		List<String> files = fileTestHelper.getFilesInDirectory(CACHE_DIRECTORY);
		assertNotNull("directory should not be empty", files);
		assertEquals("directory should have no other files", 1, files.size());
		final String firstFile = files.get(0);
		assertEquals("sample file should still exist in directory",
					 "c0a6221b2b55775b6bc5761fdb1ac0c965cc823c55e8db0b3f903b24f82fcb90.1484711165000_someETag",
					 firstFile);

		// Reset
		fileTestHelper.deleteTempCacheDirectory(CACHE_DIRECTORY);
	}

	// =================================================================================================================
	// void deleteFilesNotInList(final List<String> urls)
	// =================================================================================================================
	@Test
	public void testDeleteFilesNotInList_With_validList_then_shouldDeleteAllFilesNotInTheList() throws Exception {
		// Setup
		final String url = "https://www.someurl.com";
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		fileTestHelper.placeSampleCacheFile();
		fileTestHelper.placeOtherCacheFile();
		final List<String> urls = new ArrayList<String>();
		urls.add(url);

		// pre-verify
		List<String> preFiles = fileTestHelper.getFilesInDirectory(CACHE_DIRECTORY);
		assertNotNull("directory should not be empty", preFiles);
		assertEquals("directory should have both files", 2, preFiles.size());

		// Test
		cacheManager.deleteFilesNotInList(urls);

		// Verify
		List<String> files = fileTestHelper.getFilesInDirectory(CACHE_DIRECTORY);
		assertNotNull("directory should not be empty", files);
		assertEquals("directory should have no other files", 1, files.size());
		final String firstFile = files.get(0);
		assertEquals("sample file should still exist in directory",
					 "c0a6221b2b55775b6bc5761fdb1ac0c965cc823c55e8db0b3f903b24f82fcb90.1484711165000_someETag",
					 firstFile);
	}

	@Test
	public void testDeleteFilesNotInList_With_NullList_then_shouldDeleteAllFilesInDirectory() throws Exception {
		// Setup
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		fileTestHelper.placeSampleCacheFile();
		fileTestHelper.placeOtherCacheFile();

		// pre-verify
		List<String> preFiles = fileTestHelper.getFilesInDirectory(CACHE_DIRECTORY);
		assertNotNull("directory should not be empty", preFiles);
		assertEquals("directory should have both files", 2, preFiles.size());

		// Test
		cacheManager.deleteFilesNotInList(null);

		// Verify
		List<String> files = fileTestHelper.getFilesInDirectory(CACHE_DIRECTORY);
		assertNull("directory should be empty", files);
	}


	// =================================================================================================================
	// File getDownloadCacheDirectory()
	// =================================================================================================================

	@Test
	public void testGetDownloadCacheDirectory_when_systemAppDirectory_isNull() {
		//Setup
		systemInfoService.applicationCacheDir = null;
		// Test
		final File downloadDir = cacheManager.getDownloadCacheDirectory();
		// Verify
		assertNull("Download cache directory should should be null", downloadDir);
	}

	@Test
	public void testGetDownloadCacheDirectory_when_systemAppDirectory_isValid() {
		//Setup
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		// Test
		final File downloadDir = cacheManager.getDownloadCacheDirectory();
		// Verify
		assertEquals("Download cache directory should should be null", fileTestHelper.getCacheDirectory(), downloadDir);
	}

	// =================================================================================================================
	// protected File getDownloadCacheDirectory(final String cacheDirectoryOverride)
	// =================================================================================================================
	@Test
	public void testGetDownloadCacheDirectory_when_directoryOverrideIsPresent_then_shouldUseProvidedDirectory() throws
		Exception {
		// setup
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();
		final String overrideDir = "usethisdir";

		// execute
		final File dir = cacheManager.getDownloadCacheDirectory(overrideDir);

		// verify
		assertNotNull("returned directory should not be null", dir);
		assertTrue("returned directory has override directory in its path", dir.getAbsolutePath().contains(overrideDir));
	}

	@Test
	public void testGetDownloadCacheDirectory_when_directoryOverrideIsNull_then_shouldUseDefaultDirectory() throws
		Exception {
		// setup
		systemInfoService.applicationCacheDir = fileTestHelper.sampleApplicationBaseDir();

		// execute
		final File dir = cacheManager.getDownloadCacheDirectory(null);

		// verify
		assertNotNull("returned directory should not be null", dir);
		assertTrue("returned directory has override directory in its path", dir.getAbsolutePath().contains(CACHE_DIRECTORY));
	}

	// =================================================================================================================
	// 	static long getLastModifiedOfFile(final String path)
	// =================================================================================================================

	@Test
	public void testGetLastModifiedOfFile_With_Null() {
		// Setup
		final String filename = null;
		// Test
		final long lastModifiedDate = cacheManager.getLastModifiedOfFile(filename);
		// Verify
		assertEquals("the last modified date should be O", lastModifiedDate, 0L);
	}

	@Test
	public void testGetLastModifiedOfFile_With_EmptyFileName()  {
		// Setup
		final String filename = "";
		// Test
		final long lastModifiedDate = cacheManager.getLastModifiedOfFile(filename);
		// Verify
		assertEquals("the last modified date should be O", lastModifiedDate, 0L);
	}
	@Test
	public void testGetLastModifiedOfFile_With_InValidFileName() {
		// Setup
		final String filename = "fileName.1405626365261eTagFoesHere";
		// Test
		final long lastModifiedDate = cacheManager.getLastModifiedOfFile(filename);
		// Verify
		assertEquals("the last modified date should be O", lastModifiedDate, 0L);
	}

	@Test
	public void testGetLastModifiedOfFile_With_ValidFileName() {
		// Setup
		final String filename = "fileName.1405626365261_eTagFoesHere";
		// Test
		final long lastModifiedDate = cacheManager.getLastModifiedOfFile(filename);
		// Verify
		assertEquals("the last modified date should be 1405626365261L", lastModifiedDate, 1405626365261L);
	}


	// =================================================================================================================
	// static String sha2hash(final String url)
	// =================================================================================================================

	@Test
	public void testSha2HashWith_NullParameter() {
		// Setup
		final String url = null;
		// Test
		final String hashedString = cacheManager.sha2hash(url);
		// Verify
		assertNull("the Hashed String should be null", hashedString);
	}

	@Test
	public void testSha2HashWith_EmptyString() {
		// Setup
		final String url = "";
		// Test
		final String hashedString = cacheManager.sha2hash(url);
		// Verify
		assertNull("the Hashed String should be null", hashedString);
	}

	@Test
	public void testSha2HashWith_ValidString() {
		// Setup
		final String url = "www.url.com";
		// Test
		final String hashedString = cacheManager.sha2hash(url);
		// Verify
		assertEquals("the Hashed String should be equal to the given value", hashedString,
					 "2cc11694c44aae7d8654f9963108c31267439453d6ef27d176f32e1f2de3b478");
	}


	// =================================================================================================================
	// static String getPathExtension(final String path)
	// =================================================================================================================

	@Test
	public void testGetPathExtension_With_NullPath() {
		// Setup
		final String path = null;
		// Test
		final String extension = cacheManager.getPathExtension(path);
		// Verify
		assertNull("path extension should be null", extension);
	}


	@Test
	public void testGetPathExtension_With_EmptyPath() {
		// Setup
		final String path = "";
		// Test
		final String extension = cacheManager.getPathExtension(path);
		// Verify
		assertNull("path extension should be null", extension);
	}


	@Test
	public void testGetPathExtension_With_ValidPath()  {
		// Setup
		final String path = "filename.epochTime_eTagGoesHere";
		// Test
		final String extension = cacheManager.getPathExtension(path);
		// Verify
		assertEquals("path extension should be epochTime_eTagGoesHere", extension, "epochTime_eTagGoesHere");
	}


	// =================================================================================================================
	// static String[] splitPathExtension(final String extension)
	// =================================================================================================================

	@Test
	public void testSplitPathExtension_With_NullString()  {
		// Setup
		final String extension = null;
		// Test
		final String[] splitString = cacheManager.splitPathExtension(extension);
		// Verify
		assertArrayEquals(new String[] {}, splitString);
	}

	@Test
	public void testSplitPathExtension_With_EmptyString()  {
		// Setup
		final String extension = "";
		// Test
		final String[] splitString = cacheManager.splitPathExtension(extension);
		// Verify
		assertArrayEquals(new String[] {}, splitString);
	}

	@Test
	public void testSplitPathExtension_With_ValidPath()  {
		// Setup
		final String extension = "epochTime_eTagGoesHere";
		//Test
		final String[] splitString = cacheManager.splitPathExtension(extension);
		//Verify
		assertEquals("the first element of splitString should be epochTime", splitString[0], "epochTime");
		assertEquals("the second element of splitString should be eTagGoesHere", splitString[1], "eTagGoesHere");
	}
}
