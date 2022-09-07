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
import java.io.InputStream;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

public class MockSystemInfoService implements SystemInfoService {
	public  File applicationBaseDir;

	class MockDisplayInformation implements DisplayInformation {

		@Override
		public int getWidthPixels() {
			return 0;
		}

		@Override
		public int getHeightPixels() {
			return 0;
		}

		@Override
		public int getDensityDpi() {
			return 0;
		}

	}

	public MockSystemInfoService() {
		displayInformation = new MockDisplayInformation();
	}

	@Override
	public File getApplicationBaseDir() {
		return applicationBaseDir;
	}

	public File applicationCacheDir;
	@Override
	public File getApplicationCacheDir() {
		return applicationCacheDir;
	}

	public InputStream assetStream;
	public Map<String, InputStream> assetStreams = new HashMap<>();
	@Override
	public InputStream getAsset(String fileName) {
		return  assetStream != null ? assetStream : assetStreams.get(fileName);
	}

	private HashMap<String, String> propertyMap = new HashMap<String, String>();
	public String setPropertyValue(final String resourceKey, final String resourceValue) {
		return propertyMap.put(resourceKey, resourceValue);
	}
	@Override
	public String getProperty(final String resourceKey) {
		return propertyMap.get(resourceKey);
	}

	public String applicationName = "mockAppName";
	@Override
	public String getApplicationName() {
		return applicationName;
	}

	public String applicationPackageName = "mockAppPackageName";
	@Override
	public String getApplicationPackageName() {
		return applicationPackageName;
	}

	public String applicationVersion = "mockAppVersion";
	@Override
	public String getApplicationVersion() {
		return applicationVersion;
	}

	public String applicationVersionCode = "mockAppVersionCode";
	@Override
	public String getApplicationVersionCode() {
		return applicationVersionCode;
	}

	public Locale activeLocale = Locale.US;
	@Override
	public Locale getActiveLocale() {
		return activeLocale;
	}

	public DisplayInformation displayInformation = new MockDisplayInformation();
	@Override
	public DisplayInformation getDisplayInformation() {
		return displayInformation;
	}

	@Override
	public int getCurrentOrientation() {
		return 0;
	}

	public String operatingSystemName = "mockOSName";
	@Override
	public String getOperatingSystemName() {
		return operatingSystemName;
	}

	public String operatingSystemVersion = "mockOSVersion";
	@Override
	public String getOperatingSystemVersion() {
		return operatingSystemVersion;
	}

	public String deviceManufacturer = "mockDeviceManufacturer";
	@Override
	public String getDeviceManufacturer() {
		return deviceManufacturer;
	}

	public String mockCanonicalPlatformName = "mockPlatform";
	@Override
	public String getCanonicalPlatformName() {
		return mockCanonicalPlatformName;
	}

	public String deviceName = "mockDeviceName";
	@Override
	public String getDeviceName() {
		return deviceName;
	}

	public DeviceType deviceType = DeviceType.UNKNOWN;
	@Override
	public DeviceType getDeviceType() {
		return deviceType;
	}

	public String deviceBuildId = "mockDeviceBuildId";
	@Override
	public String getDeviceBuildId() {
		return deviceBuildId;
	}

	public String mobileCarrierName = "mockMobileCarrier";
	@Override
	public String getMobileCarrierName() {
		return mobileCarrierName;
	}

	public ConnectionStatus networkConnectionStatus = ConnectionStatus.CONNECTED;
	@Override
	public ConnectionStatus getNetworkConnectionStatus() {
		return networkConnectionStatus;
	}

	public String runMode = "Application";
	@Override
	public String getRunMode() {
		return runMode;
	}

	public String coreVersion = "mockSdkVersion";
	@Override
	public String getSdkVersion() {
		return coreVersion;
	}
	@Override
	public String getCoreVersion() {
		return coreVersion;
	}

	public String defaultUserAgent = "mockUserAgent";
	@Override
	public String getDefaultUserAgent() {
		return defaultUserAgent;
	}

	public String defaultLocaleString = "en-US";
	@Override
	public String getLocaleString() {
		return defaultLocaleString;
	}

	public boolean canRegisterNetworkActiveListener = true;
	public NetworkConnectionActiveListener networkConnectionActiveListener = null;
	@Override
	public boolean registerOneTimeNetworkConnectionActiveListener(NetworkConnectionActiveListener
			networkConnectionActiveListener) {
		this.networkConnectionActiveListener = networkConnectionActiveListener;
		return canRegisterNetworkActiveListener;
	}
}
