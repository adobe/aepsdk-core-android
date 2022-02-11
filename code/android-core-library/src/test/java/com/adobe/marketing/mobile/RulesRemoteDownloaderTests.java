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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.*;

public class RulesRemoteDownloaderTests extends BaseTest {

	private RulesRemoteDownloader rulesRemoteDownloader;

	private static final String DEFAULT_CACHE_DIR = "adbdownloadcache";

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	class MockProtocolHandler implements RulesRemoteDownloader.RulesBundleNetworkProtocolHandler {
		RulesRemoteDownloader.Metadata getMetadataReturn;
		@Override
		public RulesRemoteDownloader.Metadata getMetadata(File cachedBundlePath) {
			return getMetadataReturn;
		}

		boolean processDownloadedBundleReturn;
		@Override
		public boolean processDownloadedBundle(File downloadedBundle, String outputPath, long lastModifiedDateForBundle) {
			return processDownloadedBundleReturn;
		}
	}

	class MockMetadata implements RulesRemoteDownloader.Metadata {
		long getLastModifiedDateReturn;
		@Override
		public long getLastModifiedDate() {
			return getLastModifiedDateReturn;
		}

		long getSizeReturn;
		@Override
		public long getSize() {
			return getSizeReturn;
		}
	}

	@Before
	@Override
	public void beforeEach() {
		super.beforeEach();
	}

	@After
	@Override
	public void afterEach() {
		super.afterEach();
	}


	@Test
	public void getRequestParameters_When_ProtocolHandlerIsNull_Then_EmptyRequestParametersIsReturned() throws Exception {
		//Setup
		CacheManager cacheManager = new CacheManager(platformServices.mockSystemInfoService);
		rulesRemoteDownloader = getVerifiableRulesRemoteDownloaderForUrl("http://mock.com", cacheManager);

		File cachedFile = new File("cachedFile");
		//Test
		HashMap<String, String> map = rulesRemoteDownloader.getRequestParameters(cachedFile);

		//verify
		assertEquals(0, map.size());
	}

	@Test
	public void getRequestParameters_When_ProtocolHandlerIsNotNull_Then_ValidRequestParametersIsReturned() throws
		Exception {
		//Setup
		CacheManager cacheManager = new CacheManager(platformServices.mockSystemInfoService);
		rulesRemoteDownloader = getVerifiableRulesRemoteDownloaderForUrl("http://mock.com", cacheManager);

		//set a valid protocol handler
		rulesRemoteDownloader.setRulesBundleProtocolHandler(
			getProtocolHandler(1234568989987L, 1234, true));

		File cachedFile = new File("cachedFile");
		//Test
		HashMap<String, String> map = rulesRemoteDownloader.getRequestParameters(cachedFile);

		//verify
		assertEquals(3, map.size());
		assertEquals(rulesRemoteDownloader.createRFC2822Formatter().format(1234568989987L),
					 map.get("If-Range"));
		assertEquals(rulesRemoteDownloader.createRFC2822Formatter().format(1234568989987L),
					 map.get("If-Modified-Since"));
		assertEquals(String.format(Locale.US, "bytes=%d-", 1234), map.get("Range"));

	}

	@Test
	public void getRequestParameters_When_CacheFileIsNull_Then_EmptyRequestParametersIsReturned() throws Exception {
		//Setup
		CacheManager cacheManager = new CacheManager(platformServices.mockSystemInfoService);
		rulesRemoteDownloader = getVerifiableRulesRemoteDownloaderForUrl("http://mock.com", cacheManager);

		//set a valid protocol handler
		rulesRemoteDownloader.setRulesBundleProtocolHandler(
			getProtocolHandler(1234568989987L, 1234, true));

		//Test
		HashMap<String, String> map = rulesRemoteDownloader.getRequestParameters(null);

		//verify
		assertEquals(0, map.size());
	}

	@Test
	public void startDownloadSync_When_ProtocolHandlerIsNull_ThenBundlePathNull()throws Exception {
		//Setup
		platformServices.mockSystemInfoService.applicationCacheDir = temporaryFolder.getRoot();
		CacheManager cacheManager = new CacheManager(platformServices.mockSystemInfoService);
		rulesRemoteDownloader = getVerifiableRulesRemoteDownloaderForUrl("http://mock.com", cacheManager);
		Map<String, String> requestProperties = new HashMap<String, String>();
		requestProperties.put("Last-Modified", "Fri, 1 Jan 2010 00:00:00 UTC");

		platformServices.mockNetworkService.connectUrlReturnValue = new MockConnection("test response", 200, "",
				requestProperties);
		//test
		File bundle = rulesRemoteDownloader.startDownloadSync();
		//verify
		assertNull(bundle);
		File sdkCacheDir = new File(temporaryFolder.getRoot(), DEFAULT_CACHE_DIR);
		assertEquals(0, sdkCacheDir.list().length);
	}

	@Test
	public void startDownloadSync_When_ProtocolHandlerIsNotNull_ThenBundlePathValid() throws Exception {
		//Setup
		String mockUrl = "http://mock.com";
		CacheManager cacheManager = new CacheManager(platformServices.mockSystemInfoService);
		File expectedBundleFile = new File(temporaryFolder.getRoot(),
										   DEFAULT_CACHE_DIR + "/" + cacheManager.sha2hash(mockUrl));
		platformServices.mockSystemInfoService.applicationCacheDir = temporaryFolder.getRoot();
		rulesRemoteDownloader = getVerifiableRulesRemoteDownloaderForUrl(mockUrl, cacheManager);
		Map<String, String> requestProperties = new HashMap<String, String>();
		requestProperties.put("Last-Modified", "Fri, 1 Jan 2010 00:00:00 UTC");

		platformServices.mockNetworkService.connectUrlReturnValue = new MockConnection("test response", 200, "",
				requestProperties);

		//set a valid protocol handler
		rulesRemoteDownloader.setRulesBundleProtocolHandler(
			getProtocolHandler(1234568989987L, 1234, true));

		//test
		File bundle = rulesRemoteDownloader.startDownloadSync();
		//verify
		assertEquals(expectedBundleFile.getAbsolutePath(), bundle.getAbsolutePath());
		File sdkCacheDir = new File(temporaryFolder.getRoot(), DEFAULT_CACHE_DIR);
		assertEquals(1, sdkCacheDir.list().length);
	}

	@Test
	public void startDownloadSync_When_ProtocolHandlerCannotProcessBundle_ThenBundlePathValid1() throws Exception {
		//Setup
		String mockUrl = "http://mock.com";
		CacheManager cacheManager = new CacheManager(platformServices.mockSystemInfoService);
		platformServices.mockSystemInfoService.applicationCacheDir = temporaryFolder.getRoot();
		rulesRemoteDownloader = getVerifiableRulesRemoteDownloaderForUrl(mockUrl, cacheManager);
		Map<String, String> requestProperties = new HashMap<String, String>();
		requestProperties.put("Last-Modified", "Fri, 1 Jan 2010 00:00:00 UTC");

		platformServices.mockNetworkService.connectUrlReturnValue = new MockConnection("test response", 200, "",
				requestProperties);

		//set a valid protocol handler
		rulesRemoteDownloader.setRulesBundleProtocolHandler(
			getProtocolHandler(1234568989987L, 1234, false));

		//test
		File bundle = rulesRemoteDownloader.startDownloadSync();
		//verify
		assertNull(bundle);
		File sdkCacheDir = new File(temporaryFolder.getRoot(), DEFAULT_CACHE_DIR);
		assertEquals(0, sdkCacheDir.list().length);
	}

	@Test
	public void startDownloadSync_When_DownloadedFileNull_ThenBundlePathNull() throws Exception {
		//Setup
		String mockUrl = "http://mock.com";
		CacheManager cacheManager = new CacheManager(platformServices.mockSystemInfoService);
		platformServices.mockSystemInfoService.applicationCacheDir = temporaryFolder.getRoot();
		rulesRemoteDownloader = getVerifiableRulesRemoteDownloaderForUrl(mockUrl, cacheManager);

		//This will cause the remote download to return null file
		platformServices.mockNetworkService.connectUrlReturnValue = null;

		//set a valid protocol handler
		rulesRemoteDownloader.setRulesBundleProtocolHandler(
			getProtocolHandler(1234568989987L, 1234, true));

		//test
		File bundle = rulesRemoteDownloader.startDownloadSync();
		//verify
		assertNull(bundle);
		File sdkCacheDir = new File(temporaryFolder.getRoot(), DEFAULT_CACHE_DIR);
		assertEquals(0, sdkCacheDir.list().length);
	}

	@Test
	public void startDownloadSync_When_DownloadedFileIsDirectory_ThenBundlePathSameAsDownloadedPath() throws Exception {
		//Setup
		String mockUrl = "http://mock.com";
		CacheManager cacheManager = new CacheManager(platformServices.mockSystemInfoService);
		File cachedDirectoryFile = new File(temporaryFolder.getRoot(),
											DEFAULT_CACHE_DIR + "/" + cacheManager.sha2hash(mockUrl));
		assertTrue(cachedDirectoryFile.mkdirs());
		//
		File dummyRules = new File(cachedDirectoryFile, "rules.json");
		dummyRules.createNewFile();
		//

		platformServices.mockSystemInfoService.applicationCacheDir = temporaryFolder.getRoot();
		rulesRemoteDownloader = getVerifiableRulesRemoteDownloaderForUrl(mockUrl, cacheManager);
		Map<String, String> requestProperties = new HashMap<String, String>();
		requestProperties.put("Last-Modified", "Fri, 1 Jan 2010 00:00:00 UTC");

		//This will cause the remote download to return null file
		platformServices.mockNetworkService.connectUrlReturnValue = new MockConnection("test response", 416, "",
				requestProperties);

		//set a valid protocol handler
		rulesRemoteDownloader.setRulesBundleProtocolHandler(
			getProtocolHandler(1234568989987L, 1234, true));

		//test
		File bundle = rulesRemoteDownloader.startDownloadSync();
		//verify
		assertEquals(cachedDirectoryFile.getAbsolutePath(), bundle.getAbsolutePath());
		assertEquals(1, cachedDirectoryFile.list().length);
		assertEquals("rules.json", cachedDirectoryFile.list()[0]);
	}

	private RulesRemoteDownloader getVerifiableRulesRemoteDownloaderForUrl(final String url,
			final CacheManager cacheManager) {
		try {
			return new RulesRemoteDownloader(platformServices.getMockNetworkService(),
											 platformServices.mockSystemInfoService, url, cacheManager);
		} catch (Exception e) {
			fail("Could not create the RulesRemoteDownloader instance - " + e);
		}

		return null;
	}

	private RulesRemoteDownloader.RulesBundleNetworkProtocolHandler getProtocolHandler(long lastModifiedDate,
			long bundleSize,
			boolean protocolDownloadedBundleReturnValue) {
		MockMetadata mockMetadata = new MockMetadata();
		mockMetadata.getLastModifiedDateReturn = lastModifiedDate;
		mockMetadata.getSizeReturn = bundleSize;

		MockProtocolHandler protocolHandler = new MockProtocolHandler();
		protocolHandler.getMetadataReturn = mockMetadata;
		protocolHandler.processDownloadedBundleReturn = protocolDownloadedBundleReturnValue;

		return protocolHandler;
	}
}
