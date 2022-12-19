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

@Deprecated
class AndroidPlatformServices implements PlatformServices {

	private AndroidJsonUtility jsonUtilityService;

	private AndroidLocalStorageService localStorageService;

	private AndroidNetworkService networkService;

	private AndroidSystemInfoService systemInfoService;

	private AndroidLoggingService loggingService;

	private AndroidUIService uiService;

	private DeepLinkService deepLinkService;

	private EncodingService encodingService;

	private CompressedFileService compressedFileService;

	AndroidPlatformServices() {
		jsonUtilityService = new AndroidJsonUtility();
		systemInfoService = new AndroidSystemInfoService();
		networkService = new AndroidNetworkService(ServiceProvider.getInstance().getNetworkService());
		loggingService = new AndroidLoggingService();
		uiService = new AndroidUIService();
		localStorageService = new AndroidLocalStorageService();
		deepLinkService = new AndroidDeepLinkService();
		encodingService = new AndroidEncodingService();
		compressedFileService = new AndroidCompressedFileService();
	}

	@Override
	public JsonUtilityService getJsonUtilityService() {
		return jsonUtilityService;
	}

	@Override
	public DeepLinkService getDeepLinkService() {
		return deepLinkService;
	}

	@Override
	public EncodingService getEncodingService() {
		return encodingService;
	}

	@Override
	public LoggingService getLoggingService() {
		return loggingService;
	}

	@Override
	public NetworkService getNetworkService() {
		return networkService;
	}

	@Override
	public LocalStorageService getLocalStorageService() {
		return localStorageService;
	}

	@Override
	public SystemNotificationService getSystemNotificationService() {
		return null;
	}

	@Override
	public SystemInfoService getSystemInfoService() {
		return systemInfoService;
	}

	@Override
	public UIService getUIService() {
		return uiService;
	}

	@Override
	public CompressedFileService getCompressedFileService() {
		return compressedFileService;
	}
}
