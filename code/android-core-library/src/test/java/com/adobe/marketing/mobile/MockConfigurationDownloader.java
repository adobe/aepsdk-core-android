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

import com.adobe.marketing.mobile.services.ServiceProvider;

import java.io.File;

// TODO: Remove this class when Java version of ConfigurationExtension is deleted
public class MockConfigurationDownloader extends ConfigurationDownloader {

	String configurationDownloaderParametersUrl;
	MockConfigurationDownloader(final NetworkService networkService, final SystemInfoService systemInfoService,
								final String url, final Event event) throws MissingPlatformServicesException {
		super(ServiceProvider.getInstance().getNetworkService(), url);
		this.configurationDownloaderParametersUrl = url;
	}


	boolean startDownloadWasCalled = false;
	@Override
	public boolean startDownload() {
		startDownloadWasCalled = true;
		return super.startDownload();
	}

	boolean downloadConfigWasCalled = false;
	String downloadConfigReturnValue = null;
	@Override
	protected String downloadConfig() {
		downloadConfigWasCalled = true;
		return downloadConfigReturnValue ;
	}

	@Override
	protected void onDownloadComplete(final File downloadedFile) {
	}

	String cachedConfigString;
	@Override
	protected String loadCachedConfig() {
		return cachedConfigString;
	}
}