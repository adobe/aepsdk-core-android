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

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class ConfigurationDownloaderTest extends BaseTest {

	private ConfigurationDownloader downloader;

	@Before
	public void beforeEach() {
		super.beforeEach();
		platformServices.getMockSystemInfoService().applicationCacheDir = new File(
			this.getClass().getResource("").getPath());

		try {
			downloader = new ConfigurationDownloader(platformServices.getNetworkService(),
					platformServices.getSystemInfoService(),
					"https://dtm.adobe.com/path/to/configs/appID");
		} catch (MissingPlatformServicesException e) {
			e.printStackTrace();
		}
	}

	// =================================================================================================================
	// protected void onDownloadComplete(final File downloadedFile)
	// =================================================================================================================
	@Test
	public void testOnDownload_when_doesNothing() {
		// Test
		downloader.onDownloadComplete(null);
	}

	@Test
	public void testDownloadConfig_when_NullReturnedFromDownloader() {
		// Test
		assertNull(downloader.downloadConfig());
	}

	@Test
	public void testDownloadConfig_when_ValidFile() {
		// setup
		createSampleFileWithData("dummyString");
		//downloader.downloadedFile = createSampleFileWithData("dummyString");;
		// Test
		assertEquals("dummyString", downloader.downloadConfig());
		deleteTempAppDirectory();
	}

	@Test
	public void testLoadCachedConfig_when_CachedFilePresent() {
		// Test
		createSampleFileWithData("dummyString");
		final String cacheString = downloader.loadCachedConfig();
		// Verify
		assertEquals("dummyString", cacheString);
		deleteTempAppDirectory();
	}

	@Test
	public void testLoadCachedConfig_when_NoCachedFile() {
		// Test
		final String cacheString = downloader.loadCachedConfig();
		// Verify
		assertNull(cacheString);
	}

	// =================================================================================================================
	// Helper Methods
	// =================================================================================================================

	private File createSampleFileWithData(String fileData) {
		File appDirectory = new File(this.getClass().getResource("").getPath() + "adbdownloadcache");
		appDirectory.mkdir();
		File file = null;

		try {
			file = new File(appDirectory + File.separator +
							"8c9723cff3a9472502c9f2453d78fa968c650ee8eb6c1349e7df23a0e10a3495.1445412480000");
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write(fileData);
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return file;
	}

	private void deleteTempAppDirectory() {
		File appDirectory = new File(this.getClass().getResource("").getPath() + "adbdownloadcache");
		String[] files = appDirectory.list();

		if (files != null) {
			for (String file : files) {
				File currentFile = new File(appDirectory.getPath(), file);
				currentFile.delete();
			}
		}

		appDirectory.delete();
	}

}


