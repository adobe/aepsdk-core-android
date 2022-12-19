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

public class MockPlatformServices implements PlatformServices {
	@Override
	public LoggingService getLoggingService() {
		return null;
	}

	@Override
	public NetworkService getNetworkService() {
		return null;
	}

	@Override
	public LocalStorageService getLocalStorageService() {
		return null;
	}

	@Override
	public SystemNotificationService getSystemNotificationService() {
		return null;
	}

	@Override
	public SystemInfoService getSystemInfoService() {
		return null;
	}

	@Override
	public UIService getUIService() {
		return null;
	}

	@Override
	public JsonUtilityService getJsonUtilityService() {
		return null;
	}

	@Override
	public DeepLinkService getDeepLinkService() {
		return null;
	}

	@Override
	public EncodingService getEncodingService() {
		return null;
	}

	@Override
	public CompressedFileService getCompressedFileService() {
		return null;
	}
}
