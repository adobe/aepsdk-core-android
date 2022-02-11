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

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

public class RemoteDownloaderTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private MockDownloader mockDownloader;
	private MockDownloader mockDownloaderWithRequestProperties;
	private MockNetworkService networkService;
	private MockSystemInfoService systemInfoService;
	private MockCacheManager mockCacheManager;
	private final static String MOCK_URL                        = "https://www.adobe.com/downloadcacheFile/fromremote";

	private final static long TIME1_LONG                        = 1492636145342L;
	private final static String TIME1_STRING                    = "Wed, 19 Apr 2017 21:09:05 GMT";
	private final static long TIME2_LONG                        = 1492636145000L;
	private final static String TIME2_String                    = "Wed, 19 Apr 2017 14:09:05 GMT";

	private final static String MOCK_RANGE_STRING               = "bytes=14-";
	private static final int DEFAULT_CONNECTION_TIMEOUT         = 10000;
	private static final int DEFAULT_READ_TIMEOUT               = 10000;
	private static final Map<String, String> REQUEST_PROPERTIES = new HashMap<String, String>() {
		{
			put("key1", "value1");
			put("key2", "value2");
			put("key3", "value3");
		}
	};


	@Before
	public void setup() throws MissingPlatformServicesException {

		FakePlatformServices platformServices = new FakePlatformServices();
		networkService = platformServices.getMockNetworkService();
		systemInfoService = platformServices.getMockSystemInfoService();

		mockCacheManager = new MockCacheManager(temporaryFolder, systemInfoService);
		mockDownloader = new MockDownloader(networkService, systemInfoService, MOCK_URL, mockCacheManager);
		mockDownloaderWithRequestProperties = new MockDownloader(networkService, systemInfoService, MOCK_URL, mockCacheManager,
				REQUEST_PROPERTIES);



		Log.setLoggingService(platformServices.getLoggingService());
		Log.setLogLevel(LoggingMode.VERBOSE);
	}

	@AfterClass
	public static void tearDownTests() {
		//Reset logger
		Log.setLogLevel(LoggingMode.ERROR);
		Log.setLoggingService(null);
	}

	@Test
	public void testStartDownload_When_NullURL() throws Exception {
		// test
		mockDownloader = new MockDownloader(networkService, systemInfoService, null, mockCacheManager);
		boolean didStartDownload = mockDownloader.startDownload();

		// verify
		assertFalse("should start download", didStartDownload);
		assertFalse("Network call should not be made", networkService.connectUrlWasCalled);
	}

	@Test
	public void testStartDownload_When_InvalidURL() throws Exception {
		// test
		mockDownloader = new MockDownloader(networkService, systemInfoService, "\"Invalid\"", mockCacheManager);
		boolean didStartDownload = mockDownloader.startDownload();

		// verify
		assertFalse("should start download", didStartDownload);
		assertFalse("Network call should not be made", networkService.connectUrlAsyncWasCalled);
	}

	@Test
	public void testStartDownload_When_CacheFilePresentForURL() throws Exception {
		// Setup
		File cacheFile = createNewFile("Cached content");
		MockCacheManager.FileWithMetadata fileWithMetadata = getFileWithMetadata(cacheFile, TIME1_LONG);
		mockCacheManager.cacheMap.put(MOCK_URL, fileWithMetadata);

		// test
		boolean didStartDownload = mockDownloader.startDownload();

		// verify
		assertTrue("should start download", didStartDownload);
		assertEquals(" Async Network calls should happen with the correct url", MOCK_URL,
					 networkService.connectUrlAsyncParametersUrl);
		assertEquals(" Async Network calls should happen with the correct HttpCommand", NetworkService.HttpCommand.GET,
					 networkService.connectUrlAsyncParametersCommand);
		assertEquals(" Async Network calls should happen with the correct ReadTimeOut", DEFAULT_READ_TIMEOUT,
					 networkService.connectUrlAsyncParametersReadTimeout);
		assertEquals(" Async Network calls should happen with the correct ConnectionParameters", DEFAULT_CONNECTION_TIMEOUT,
					 networkService.connectUrlAsyncParametersConnectTimeout);
		assertEquals("Connection should have the correct Request Parameter count", 3,
					 networkService.connectUrlAsyncParametersRequestProperty.size());
		assertEquals("Connection should have the correct Request Parameter -If-Range", TIME1_STRING,
					 networkService.connectUrlAsyncParametersRequestProperty.get("If-Range"));
		assertEquals("Connection should have the correct Request Parameter -If-Modified-Since", TIME1_STRING,
					 networkService.connectUrlAsyncParametersRequestProperty.get("If-Modified-Since"));
		assertEquals("Connection should have the correct Request Parameter - Range", MOCK_RANGE_STRING,
					 networkService.connectUrlAsyncParametersRequestProperty.get("Range"));
	}

	@Test
	public void testStartDownload_When_CacheFilePresentForURLWithAdditionalRequestProperties() throws Exception {
		// Setup
		File cacheFile = createNewFile("Cached content");
		MockCacheManager.FileWithMetadata fileWithMetadata = getFileWithMetadata(cacheFile, TIME1_LONG);
		mockCacheManager.cacheMap.put(MOCK_URL, fileWithMetadata);

		// test
		boolean didStartDownload = mockDownloaderWithRequestProperties.startDownload();

		// verify
		assertTrue("should start download", didStartDownload);
		assertEquals(" Async Network calls should happen with the correct url", MOCK_URL,
					 networkService.connectUrlAsyncParametersUrl);
		assertEquals(" Async Network calls should happen with the correct HttpCommand", NetworkService.HttpCommand.GET,
					 networkService.connectUrlAsyncParametersCommand);
		assertEquals(" Async Network calls should happen with the correct ReadTimeOut", DEFAULT_READ_TIMEOUT,
					 networkService.connectUrlAsyncParametersReadTimeout);
		assertEquals(" Async Network calls should happen with the correct ConnectionParameters", DEFAULT_CONNECTION_TIMEOUT,
					 networkService.connectUrlAsyncParametersConnectTimeout);
		assertEquals("Connection should have the correct Request Parameter count", 6,
					 networkService.connectUrlAsyncParametersRequestProperty.size());
		assertEquals("Connection should have the correct Request Parameter -If-Range", TIME1_STRING,
					 networkService.connectUrlAsyncParametersRequestProperty.get("If-Range"));
		assertEquals("Connection should have the correct Request Parameter -If-Modified-Since", TIME1_STRING,
					 networkService.connectUrlAsyncParametersRequestProperty.get("If-Modified-Since"));
		assertEquals("Connection should have the correct Request Parameter - Range", MOCK_RANGE_STRING,
					 networkService.connectUrlAsyncParametersRequestProperty.get("Range"));
		assertEquals("Connection should have the correct Request Parameter", "value1",
					 networkService.connectUrlAsyncParametersRequestProperty.get("key1"));
		assertEquals("Connection should have the correct Request Parameter", "value2",
					 networkService.connectUrlAsyncParametersRequestProperty.get("key2"));
		assertEquals("Connection should have the correct Request Parameter", "value3",
					 networkService.connectUrlAsyncParametersRequestProperty.get("key3"));
	}

	@Test
	public void testStartDownload_When_NoCacheFile() throws Exception {
		// test
		boolean didStartDownload = mockDownloader.startDownload();

		// verify
		assertTrue("should start download", didStartDownload);
		assertEquals(" Async Network calls should happen with the correct url", MOCK_URL,
					 networkService.connectUrlAsyncParametersUrl);
		assertEquals(" Async Network calls should happen with the correct HttpCommand", NetworkService.HttpCommand.GET,
					 networkService.connectUrlAsyncParametersCommand);
		assertEquals(" Async Network calls should happen with the correct ReadTimeOut", DEFAULT_READ_TIMEOUT,
					 networkService.connectUrlAsyncParametersReadTimeout);
		assertEquals(" Async Network calls should happen with the correct ConnectionParameters", DEFAULT_CONNECTION_TIMEOUT,
					 networkService.connectUrlAsyncParametersConnectTimeout);
		assertNull("Connection should have the no Request Parameter ", networkService.connectUrlAsyncParametersRequestProperty);
	}

	@Test
	public void testStartDownload_When_ContentNotFound() throws Exception {
		//Setup response
		networkService.connectUrlAsyncCallbackParametersConnection = new MockConnection(null,
				HttpURLConnection.HTTP_NOT_FOUND, null, null);

		// test
		mockDownloader.startDownload();
		//Verify
		assertTrue("Download complete should be called", mockDownloader.onDownloadCompleteCalled);
		assertNull("Downloaded file should be null", mockDownloader.onDownloadCompleteParametersDownloadedFile);
		assertTrue("Connection should be closed",
				   ((MockConnection)networkService.connectUrlAsyncCallbackParametersConnection).closeWasCalled);
	}

	@Test
	public void testStartDownload_When_NewContentAvailable() throws Exception {
		//Setup response
		final HashMap<String, String> mockConnectionResponse = new HashMap<String, String >();
		mockConnectionResponse.put("Last-Modified", TIME1_STRING);
		networkService.connectUrlAsyncCallbackParametersConnection  = new MockConnection("test content",
				HttpURLConnection.HTTP_OK, null,  mockConnectionResponse);

		//Test
		mockDownloader.startDownload();
		//Verify
		assertTrue("Download complete should be called", mockDownloader.onDownloadCompleteCalled);
		assertEquals("Downloaded file should have the following name", "cacheFileRenamed",
					 mockDownloader.onDownloadCompleteParametersDownloadedFile.getName());
		assertEquals("File should contain the same content", "test content",
					 getContent(mockDownloader.onDownloadCompleteParametersDownloadedFile));
		assertTrue("Connection should be closed",
				   ((MockConnection)networkService.connectUrlAsyncCallbackParametersConnection).closeWasCalled);
	}

	@Test
	public void testStartDownload_When_ModifiedContentAvailable_With_LocalCachePresent() throws Exception {
		// setup response
		final HashMap<String, String> mockConnectionResponse = new HashMap<String, String >();
		mockConnectionResponse.put("Last-Modified", TIME1_STRING);
		networkService.connectUrlAsyncCallbackParametersConnection  = new MockConnection(
			"test content", HttpURLConnection.HTTP_OK, null,  mockConnectionResponse);

		// set up cacheFile
		File cacheFile = createNewFile("Cached content");
		MockCacheManager.FileWithMetadata fileWithMetadata = getFileWithMetadata(cacheFile, TIME1_LONG);
		mockCacheManager.cacheMap.put(MOCK_URL, fileWithMetadata);

		//Test
		mockDownloader.startDownload();

		//Verify
		assertTrue("Download complete should be called", mockDownloader.onDownloadCompleteCalled);
		assertEquals("Downloaded file should have the following name", "cacheFileRenamed",
					 mockDownloader.onDownloadCompleteParametersDownloadedFile.getName());
		assertEquals("File should contain the same content", "test content",
					 getContent(mockDownloader.onDownloadCompleteParametersDownloadedFile));
		assertTrue("Connection should be closed",
				   ((MockConnection)networkService.connectUrlAsyncCallbackParametersConnection).closeWasCalled);
	}

	@Test
	public void testStartDownload_When_NotModifiedContent_With_LocalCachePresent() throws Exception {
		// setup response
		final HashMap<String, String> mockConnectionResponse = new HashMap<String, String >();
		mockConnectionResponse.put("Last-Modified", TIME1_STRING);
		networkService.connectUrlAsyncCallbackParametersConnection  = new MockConnection(
			"test content", HttpURLConnection.HTTP_OK, null,  mockConnectionResponse);

		// set up cacheFile
		File cacheFile = createNewFile("Cached content");
		MockCacheManager.FileWithMetadata fileWithMetadata = getFileWithMetadata(cacheFile, TIME1_LONG);
		mockCacheManager.cacheMap.put(MOCK_URL, fileWithMetadata);

		//Test
		mockDownloader.startDownload();

		//Verify
		assertTrue("Download complete should be called", mockDownloader.onDownloadCompleteCalled);
		assertEquals("Downloaded file should have the following name", "cacheFileRenamed",
					 mockDownloader.onDownloadCompleteParametersDownloadedFile.getName());
		assertEquals("File should contain the same content", "test content",
					 getContent(mockDownloader.onDownloadCompleteParametersDownloadedFile));
		assertTrue("Connection should be closed",
				   ((MockConnection)networkService.connectUrlAsyncCallbackParametersConnection).closeWasCalled);
	}

	@Test
	public void testStartDownload_PartialContent_With_NoLocalCachedContent() throws Exception {
		// setup response
		final HashMap<String, String> mockConnectionResponse = new HashMap<String, String >();
		mockConnectionResponse.put("Last-Modified", TIME1_STRING);
		networkService.connectUrlAsyncCallbackParametersConnection  = new MockConnection("test content",
		HttpURLConnection.HTTP_PARTIAL, null, new HashMap<String, String>() {
			{
				put("key1", "value1");
				put("key2", "value2");
				put("key3", "value3");
			}
		});
		//This test is testing the scenario where we lost the partially
		///downloaded cache file, but the server does not know about it.
		//That's why we are not adding any files explicitly to the cache.
		//Test
		mockDownloaderWithRequestProperties.startDownload();
		//Verify
		assertTrue("Download complete should be called", mockDownloaderWithRequestProperties.onDownloadCompleteCalled);
		assertNull("Downloaded file should be null",
				   mockDownloaderWithRequestProperties.onDownloadCompleteParametersDownloadedFile);
		assertTrue("Connection should be closed",
				   ((MockConnection)networkService.connectUrlAsyncCallbackParametersConnection).closeWasCalled);
		assertEquals("Connection should have the correct Request Parameter count", 3,
					 networkService.connectUrlAsyncParametersRequestProperty.size());
		assertEquals("Connection should have the correct Request Parameter", "value1",
					 networkService.connectUrlAsyncParametersRequestProperty.get("key1"));
		assertEquals("Connection should have the correct Request Parameter", "value2",
					 networkService.connectUrlAsyncParametersRequestProperty.get("key2"));
		assertEquals("Connection should have the correct Request Parameter", "value3",
					 networkService.connectUrlAsyncParametersRequestProperty.get("key3"));
	}

	@Test
	public void testStartDownload_PartialContent_With_LocalCachedContent() throws Exception {
		//Setup
		final String cachedFileContent = "Cached content partial ";
		final String newContent = "test content";
		final HashMap<String, String> mockConnectionResponse = new HashMap<String, String >();
		mockConnectionResponse.put("Last-Modified", TIME1_STRING);
		networkService.connectUrlAsyncCallbackParametersConnection  = new MockConnection(newContent,
				HttpURLConnection.HTTP_PARTIAL, null,  null);

		File cacheFile = createNewFile(cachedFileContent);
		MockCacheManager.FileWithMetadata fileWithMetadata = getFileWithMetadata(cacheFile, TIME1_LONG);
		mockCacheManager.cacheMap.put(MOCK_URL, fileWithMetadata);

		//Test
		mockDownloader.startDownload();

		//Verify
		assertTrue("Download complete should be called", mockDownloader.onDownloadCompleteCalled);
		assertEquals("Downloaded file should have the following name", "cacheFileRenamed",
					 mockDownloader.onDownloadCompleteParametersDownloadedFile.getName());
		assertEquals("File should contain the same content", cachedFileContent + newContent,
					 getContent(mockDownloader.onDownloadCompleteParametersDownloadedFile));
		assertTrue("Connection should be closed",
				   ((MockConnection)networkService.connectUrlAsyncCallbackParametersConnection).closeWasCalled);

	}

	@Test
	public void testStartDownload_RangeNotSatisfiable_With_NoLocal_CachedContent() throws Exception {
		//Setup
		networkService.connectUrlAsyncCallbackParametersConnection  = new MockConnection(
			"test content", 416, null,  null);

		//Test
		mockDownloader.startDownload();

		//Verify
		assertTrue("Download complete should be called", mockDownloader.onDownloadCompleteCalled);
		assertNull("Downloaded file should be null", mockDownloader.onDownloadCompleteParametersDownloadedFile);
		assertTrue("Connection should be closed",
				   ((MockConnection)networkService.connectUrlAsyncCallbackParametersConnection).closeWasCalled);
	}

	@Test
	public void testStartDownload_dateRangeNotSatisfiable_With_Local_CachedContent() throws Exception {
		//Setup
		final String cachedFileContent = "Cached content partial ";

		networkService.connectUrlAsyncCallbackParametersConnection  = new MockConnection(
			"test content", 416, null,  null);

		// create cached file
		File cacheFile = createNewFile(cachedFileContent);
		MockCacheManager.FileWithMetadata fileWithMetadata = getFileWithMetadata(cacheFile, TIME1_LONG);
		mockCacheManager.cacheMap.put(MOCK_URL, fileWithMetadata);

		//Test
		mockDownloader.startDownload();

		//Verify
		assertTrue("Download complete should be called", mockDownloader.onDownloadCompleteCalled);
		// TODO try to change name
		//assertEquals("Downloaded file should have the following name","cacheFileRenamed",mockDownloader.onDownloadCompleteParametersDownloadedFile.getName());
		assertEquals("File should contain the same content", cachedFileContent,
					 getContent(mockDownloader.onDownloadCompleteParametersDownloadedFile));
		assertTrue("Connection should be closed",
				   ((MockConnection)networkService.connectUrlAsyncCallbackParametersConnection).closeWasCalled);

	}

	@Test
	public void testStartDownload_Error_Response() throws Exception {
		//Setup
		networkService.connectUrlAsyncCallbackParametersConnection  = new MockConnection(
			"test content", HttpURLConnection.HTTP_BAD_GATEWAY, "Mock Error",  null);

		//Test
		mockDownloader.startDownload();

		//Verify
		assertTrue("Download complete should be called", mockDownloader.onDownloadCompleteCalled);
		assertNull("Downloaded file should be null", mockDownloader.onDownloadCompleteParametersDownloadedFile);
		assertTrue("Connection should be closed",
				   ((MockConnection)networkService.connectUrlAsyncCallbackParametersConnection).closeWasCalled);
	}

	@Test(expected = MissingPlatformServicesException.class)
	public void testStartDownloader_when_NetworkService_notInitialized() throws Exception {
		//Setup

		mockDownloader = new MockDownloader(null, systemInfoService, null, mockCacheManager);
		//Verify
		//Expecting to throw MissingPlatformServices Exception
	}


	@Test(expected = MissingPlatformServicesException.class)
	public void testStartDownloader_when_SystemInfoService_notInitialized() throws Exception {
		//Setup

		mockDownloader = new MockDownloader(networkService, null, null, mockCacheManager);
		//Verify
		//Expecting to throw MissingPlatformServices Exception
	}

	@Test
	public void testDownload_Cache_File_Creation_Error() throws Exception {
		//Setup
		final HashMap<String, String> mockConnectionResponse = new HashMap<String, String >();
		mockConnectionResponse.put("Last-Modified", TIME1_STRING);
		networkService.connectUrlAsyncCallbackParametersConnection  = new MockConnection("test content",
				HttpURLConnection.HTTP_OK, null,  mockConnectionResponse);

		mockCacheManager.allowNewCacheFileCreation = false;

		//Test
		mockDownloader.startDownload();

		//Verify
		assertTrue("Download complete should be called", mockDownloader.onDownloadCompleteCalled);
		assertNull("Downloaded file should be null", mockDownloader.onDownloadCompleteParametersDownloadedFile);
		assertTrue("Connection should be closed",
				   ((MockConnection)networkService.connectUrlAsyncCallbackParametersConnection).closeWasCalled);
	}

	@Test
	public void testDownload_Cache_File_rename_error() throws Exception {
		//Setup
		//Setup
		final HashMap<String, String> mockConnectionResponse = new HashMap<String, String >();
		mockConnectionResponse.put("Last-Modified", TIME1_STRING);
		networkService.connectUrlAsyncCallbackParametersConnection  = new MockConnection("test content",
				HttpURLConnection.HTTP_OK, null,  mockConnectionResponse);

		mockCacheManager.allowMarkingFileComplete = false;

		//Test
		mockDownloader.startDownload();

		//Verify
		assertTrue("Download complete should be called", mockDownloader.onDownloadCompleteCalled);
		assertNull("Downloaded file should be null", mockDownloader.onDownloadCompleteParametersDownloadedFile);
		assertTrue("Connection should be closed",
				   ((MockConnection)networkService.connectUrlAsyncCallbackParametersConnection).closeWasCalled);
	}

	@Test
	public void testDownload_With_PartialCacheOnDisk_Cache_File_Rename_Error_On_Complete() throws Exception {
		//Setup
		final String cachedFileContent = "Cached content partial ";
		final String newContent = "test content";
		final HashMap<String, String> mockConnectionResponse = new HashMap<String, String >();
		mockConnectionResponse.put("Last-Modified", TIME1_STRING);
		networkService.connectUrlAsyncCallbackParametersConnection  = new MockConnection(newContent,
				HttpURLConnection.HTTP_PARTIAL, null,  null);

		mockCacheManager.allowMarkingFileComplete = false;

		File cacheFile = createNewFile(cachedFileContent);
		MockCacheManager.FileWithMetadata fileWithMetadata = getFileWithMetadata(cacheFile, TIME1_LONG);
		mockCacheManager.cacheMap.put(MOCK_URL, fileWithMetadata);

		//Test
		mockDownloader.startDownload();

		//Verify
		assertTrue("Download complete should be called", mockDownloader.onDownloadCompleteCalled);
		assertNull("Downloaded file should be null", mockDownloader.onDownloadCompleteParametersDownloadedFile);
		assertTrue("Connection should be closed",
				   ((MockConnection)networkService.connectUrlAsyncCallbackParametersConnection).closeWasCalled);
	}

	@Test
	public void testStartDownloadSync_When_NullURL() throws Exception {
		// test
		mockDownloader = new MockDownloader(networkService, systemInfoService, null, mockCacheManager);
		File downloadedFile = mockDownloader.startDownloadSync();

		// verify
		assertNull("returned file should be null", downloadedFile);
	}

	@Test
	public void testStartDownloadSync_When_InvalidURL() throws Exception {
		// test
		mockDownloader = new MockDownloader(networkService, systemInfoService, "\"Invalid\"", mockCacheManager);
		File downloadedFile = mockDownloader.startDownloadSync();

		// verify
		assertNull("returned file should be null", downloadedFile);
	}

	@Test
	public void testStartDownloadSync_When_CacheFilePresentForURL() throws Exception {
		// Setup
		File cacheFile = createNewFile("Cached content");
		MockCacheManager.FileWithMetadata fileWithMetadata = getFileWithMetadata(cacheFile, TIME1_LONG);
		mockCacheManager.cacheMap.put(MOCK_URL, fileWithMetadata);

		// test
		mockDownloader.startDownloadSync();

		// verify
		assertEquals("Network calls should happen with the correct url", MOCK_URL, networkService.connectUrlParametersUrl);
		assertEquals("Network calls should happen with the correct HttpCommand", NetworkService.HttpCommand.GET,
					 networkService.connectUrlParametersCommand);
		assertEquals("Network calls should happen with the correct ReadTimeOut", DEFAULT_READ_TIMEOUT,
					 networkService.connectUrlParametersReadTimeout);
		assertEquals("Network calls should happen with the correct ConnectionParameters", DEFAULT_CONNECTION_TIMEOUT,
					 networkService.connectUrlParametersConnectTimeout);
		assertEquals("Connection should have the correct Request Parameter count", 3,
					 networkService.connectUrlParametersRequestProperty.size());
		assertEquals("Connection should have the correct Request Parameter -If-Range", TIME1_STRING,
					 networkService.connectUrlParametersRequestProperty.get("If-Range"));
		assertEquals("Connection should have the correct Request Parameter -If-Modified-Since", TIME1_STRING,
					 networkService.connectUrlParametersRequestProperty.get("If-Modified-Since"));
		assertEquals("Connection should have the correct Request Parameter - Range", MOCK_RANGE_STRING,
					 networkService.connectUrlParametersRequestProperty.get("Range"));
	}

	@Test
	public void testStartDownloadSync_When_NoCacheFile() throws Exception {
		// test
		mockDownloader.startDownloadSync();

		// verify
		assertEquals("Network calls should happen with the correct url", MOCK_URL, networkService.connectUrlParametersUrl);
		assertEquals("Network calls should happen with the correct HttpCommand", NetworkService.HttpCommand.GET,
					 networkService.connectUrlParametersCommand);
		assertEquals("Network calls should happen with the correct ReadTimeOut", DEFAULT_READ_TIMEOUT,
					 networkService.connectUrlParametersReadTimeout);
		assertEquals("Network calls should happen with the correct ConnectionParameters", DEFAULT_CONNECTION_TIMEOUT,
					 networkService.connectUrlParametersConnectTimeout);
		assertNull("Connection should have the no Request Parameter ", networkService.connectUrlParametersRequestProperty);
	}

	private String getContent(final File file) throws Exception {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;

		while ((line = br.readLine()) != null) {
			sb.append(line);
		}

		return sb.toString();
	}

	private File createNewFile(final String content) {
		try {
			File cache = temporaryFolder.newFile("existingCache" + UUID.randomUUID().toString());
			FileOutputStream fos = new FileOutputStream(cache);
			fos.write(content.getBytes());
			fos.close();
			return cache;
		} catch (Exception e) {
			Log.error("test", "Could not create a cache file for tests (%s)", e);
		}

		return null;
	}

	private MockCacheManager.FileWithMetadata getFileWithMetadata(final File cacheFile, final long modifiedTime) {
		MockCacheManager.FileWithMetadata fileWithMetadata = new MockCacheManager.FileWithMetadata();
		fileWithMetadata.file = cacheFile;
		fileWithMetadata.lastModifiedDate = modifiedTime;
		return fileWithMetadata;
	}

}
