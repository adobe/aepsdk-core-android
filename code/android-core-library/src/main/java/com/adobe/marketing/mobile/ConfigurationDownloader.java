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


import com.adobe.marketing.mobile.services.Networking;

import java.io.File;

// TODO: Remove this class when Java version of ConfigurationExtension is deleted
class ConfigurationDownloader extends RemoteDownloader {

	private String url;

	ConfigurationDownloader(final Networking networkService,
							final String url)
	throws MissingPlatformServicesException {
		super(networkService, url, (String)null);
		this.url = url;
	}

	/**
	 * Retrieves the contents of the cached file for this ConfigurationDownloader instance. If there is no cached
	 * file, <tt>null</tt> is returned.
	 * @return the String contents of the cached file, or <tt>null</tt> if there is no cached file or there was
	 * an error reading the file contents.
	 */
	protected String loadCachedConfig() {
		final File cacheFile = super.cacheManager.getFileForCachedURL(url, null, false);

		if (cacheFile != null) {
			Log.trace(ConfigurationExtension.LOG_SOURCE, "Loaded cached config file");
			return FileUtil.readStringFromFile(cacheFile);
		}

		Log.debug(ConfigurationExtension.LOG_SOURCE,
				  "Either there was no cached config, or there was a problem loading the cached config.");
		return null;
	}

	/**
	 * This method is called to make a sync network request for configuration download
	 *
	 * @return The contents of the file in JSON String format. Returns null if there is any problem with the network
	 * request or with reading the cached file.
	 */
	protected String downloadConfig() {
		final File downloadedFile = super.startDownloadSync();

		if (downloadedFile != null) {
			Log.trace(ConfigurationExtension.LOG_SOURCE, "Downloaded config file");
			return FileUtil.readStringFromFile(downloadedFile);
		}

		Log.debug(ConfigurationExtension.LOG_SOURCE, "Problem while downloading config.");
		return null;
	}


}
