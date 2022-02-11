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

public class FakePlatformServices implements PlatformServices {

	MockNetworkService            mockNetworkService;
	FakeJsonUtilityService        fakeJsonUtilityService;
	FakeLocalStorageService       fakeLocalStorageService;
	FakeLoggingService            fakeLoggingService;
	FakeDatabaseService           fakeDatabaseService;
	MockSystemInfoService         mockSystemInfoService;
	MockSystemNotificationService mockSystemNotificationService;
	MockUIService                 mockUIService;
	MockDeepLinkService           mockDeepLinkService;
	FakeEncodingService           fakeEncodingService;
	MockCompressedFileService mockCompressedFileService;

	public FakePlatformServices() {
		mockNetworkService = new MockNetworkService();
		fakeJsonUtilityService = new FakeJsonUtilityService();
		fakeLocalStorageService = new FakeLocalStorageService();
		fakeLoggingService = new FakeLoggingService();
		fakeDatabaseService = new FakeDatabaseService();
		mockSystemInfoService = new MockSystemInfoService();
		mockSystemNotificationService = new MockSystemNotificationService();
		mockUIService = new MockUIService();
		mockDeepLinkService = new MockDeepLinkService();
		fakeEncodingService = new FakeEncodingService();
		mockCompressedFileService = new MockCompressedFileService();
	}

	public MockNetworkService getMockNetworkService() {
		return mockNetworkService;
	}

	public MockSystemNotificationService getMockSystemNotificationService() {
		return mockSystemNotificationService;
	}

	public MockSystemInfoService getMockSystemInfoService() {
		return mockSystemInfoService;
	}

	public MockDeepLinkService getMockDeepLinkService() {
		return mockDeepLinkService;
	}

	public MockUIService getMockUIService() {
		return mockUIService;
	}

	@Override
	public LoggingService getLoggingService() {
		return fakeLoggingService;
	}

	@Override
	public NetworkService getNetworkService() {
		return mockNetworkService;
	}

	@Override
	public LocalStorageService getLocalStorageService() {
		return fakeLocalStorageService;
	}

	@Override
	public DatabaseService getDatabaseService() {
		return fakeDatabaseService;
	}

	@Override
	public SystemNotificationService getSystemNotificationService() {
		return mockSystemNotificationService;
	}

	@Override
	public SystemInfoService getSystemInfoService() {
		return mockSystemInfoService;
	}

	@Override
	public UIService getUIService() {
		return mockUIService;
	}

	@Override
	public JsonUtilityService getJsonUtilityService() {
		return fakeJsonUtilityService;
	}

	@Override
	public DeepLinkService getDeepLinkService() {
		return mockDeepLinkService;
	}

	@Override
	public EncodingService getEncodingService() {
		return fakeEncodingService;
	}


	@Override
	public CompressedFileService getCompressedFileService() {
		return mockCompressedFileService;
	}
}
