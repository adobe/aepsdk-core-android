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

import java.io.File;
import java.util.Map;

public class MockDownloader extends RemoteDownloader {
	public MockDownloader(final NetworkService networkService, final SystemInfoService systemInfoService, final String url,
						  final CacheManager cacheManager) throws MissingPlatformServicesException {
		super(networkService, systemInfoService, url, cacheManager);
	}

	public MockDownloader(final NetworkService networkService, final SystemInfoService systemInfoService, final String url,
						  final CacheManager cacheManager, final Map<String, String> requestProperties) throws MissingPlatformServicesException {
		super(networkService, systemInfoService, url, cacheManager, requestProperties);
	}

	public boolean onDownloadCompleteCalled;
	public File onDownloadCompleteParametersDownloadedFile;
	@Override
	protected void onDownloadComplete(File downloadedFile) {
		onDownloadCompleteCalled = true;
		onDownloadCompleteParametersDownloadedFile = downloadedFile;
	}

}
