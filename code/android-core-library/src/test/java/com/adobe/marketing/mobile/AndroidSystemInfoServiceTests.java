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

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@SuppressWarnings("all")
@RunWith(MockitoJUnitRunner.Silent.class)
public class AndroidSystemInfoServiceTests {

	private static final int    MAXIMUM_READ_SIZE = 65535;
	public static final  String CHARSET_UTF_8     = "UTF-8";

	@Mock
	Activity mockActivity;

	@Mock
	Context mockContext;

	@Mock
	Resources mockResources;

	@Mock
	android.content.res.Configuration mockConfiguration;

	@Mock
	ApplicationInfo mockApplicationInfo;

	@Mock
	PackageManager mockPackageManager;

	@Mock
	PackageInfo mockPackageInfo;

	@Mock
	TelephonyManager mockTelephonyManager;

	@Mock
	DisplayMetrics mockDisplayMetrics;

	@Mock
	ConnectivityManager mockConnectivityManager;

	@Mock
	NetworkInfo mockNetworkInfo;

	@Mock
	AssetManager mockAssetManager;

	@Mock
	Bundle mockBundle;

	@Mock
	LocaleList localeList;

	@Before
	public void beforeEach() {
		when(mockContext.getApplicationContext()).thenReturn(mockContext);
		App.setAppContext(mockContext);
	}

	@Test
	public void testConstructor_Happy() throws Exception {
		new AndroidSystemInfoService();
	}

	@Test
	public void testGetApplicationBaseDir_Happy() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getApplicationInfo()).thenReturn(mockApplicationInfo);
		mockApplicationInfo.dataDir = "this/is/a/test/path";
		File expectedFile = new File(mockApplicationInfo.dataDir);
		assertEquals(expectedFile, systemInfoService.getApplicationBaseDir());
	}

	@Test
	public void testGetApplicationBaseDir_NullContext() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		App.setAppContext(null);
		Runtime.getRuntime().gc();
		assertNull(systemInfoService.getApplicationBaseDir());
	}

	@Test
	public void testGetApplicationBaseDir_NullApplicationInfo() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getApplicationInfo()).thenReturn(null);
		assertNull(systemInfoService.getApplicationBaseDir());
	}

	@Test
	public void testGetApplicationCacheDir_Happy() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		File testCacheDir = new File("testCacheDir");
		when(mockContext.getCacheDir()).thenReturn(testCacheDir);
		assertEquals(testCacheDir, systemInfoService.getApplicationCacheDir());
	}

	@Test
	public void testGetApplicationCacheDir_NullContext() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		App.setAppContext(null);
		Runtime.getRuntime().gc();
		assertNull(systemInfoService.getApplicationCacheDir());
	}

	@Test
	public void testGetApplicationFilesDir_Happy() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		File testFilesDir = new File("testFilesDir");
		when(mockContext.getFilesDir()).thenReturn(testFilesDir);
		assertEquals(testFilesDir, systemInfoService.getApplicationFilesDir());
	}

	@Test
	public void testGetApplicationFilesDir_NullContext() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		App.setAppContext(null);
		Runtime.getRuntime().gc();
		assertNull(systemInfoService.getApplicationFilesDir());
	}

	@Test
	public void testGetApplicationName_Happy() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getPackageName()).thenReturn("com.adobe.mobile.testPackage");
		when(mockContext.getPackageManager()).thenReturn(mockPackageManager);
		when(mockPackageManager.getApplicationInfo(anyString(), anyInt())).thenReturn(mockApplicationInfo);
		when(mockPackageManager.getApplicationLabel(mockApplicationInfo)).thenReturn("testApp");
		assertEquals("testApp", systemInfoService.getApplicationName());
	}

	@Test
	public void testGetApplicationName_NullContext() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		App.setAppContext(null);
		Runtime.getRuntime().gc();
		assertNull(systemInfoService.getApplicationName());
	}

	@Test
	public void testGetApplicationName_NullPackageManager() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getPackageName()).thenReturn("com.adobe.mobile.testPackage");
		when(mockContext.getPackageManager()).thenReturn(null);
		assertNull(systemInfoService.getApplicationName());
	}

	@Test
	public void testGetApplicationPackageName_Happy() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getPackageName()).thenReturn("com.adobe.mobile.testPackage");
		assertEquals("com.adobe.mobile.testPackage", systemInfoService.getApplicationPackageName());
	}

	@Test
	public void testGetApplicationPackageName_NullContext() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		App.setAppContext(null);
		Runtime.getRuntime().gc();
		assertNull(systemInfoService.getApplicationPackageName());
	}

	@Test
	public void testGetCurrentOrientation_Happy() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		App.setCurrentActivity(mockActivity);
		when(mockActivity.getResources()).thenReturn(mockResources);
		when(mockResources.getConfiguration()).thenReturn(mockConfiguration);
		mockConfiguration.orientation = 1;
		assertEquals(1, systemInfoService.getCurrentOrientation());
	}

	@Test
	public void testGetCurrentOrientation_NullContext() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		App.setCurrentActivity(null);
		assertEquals(0, systemInfoService.getCurrentOrientation());
	}

	@Test
	public void testGetApplicationVersion_Happy() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getPackageManager()).thenReturn(mockPackageManager);
		when(mockContext.getPackageName()).thenReturn("com.adobe.mobile.test");
		when(mockPackageManager.getPackageInfo("com.adobe.mobile.test", 0)).thenReturn(mockPackageInfo);
		mockPackageInfo.versionName = "wat";
		assertEquals("wat", systemInfoService.getApplicationVersion());
	}

	@Test
	public void testGetApplicationVersion_NullContext() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		App.setAppContext(null);
		Runtime.getRuntime().gc();
		assertNull(systemInfoService.getApplicationVersion());
	}

	@Test
	public void testGetApplicationVersionCode_HappyFlow() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getPackageManager()).thenReturn(mockPackageManager);
		when(mockContext.getPackageName()).thenReturn("com.adobe.mobile.test");
		when(mockPackageManager.getPackageInfo("com.adobe.mobile.test", 0)).thenReturn(mockPackageInfo);
		mockPackageInfo.versionCode = 2;
		assertEquals("2", systemInfoService.getApplicationVersionCode());
	}

	@Test
	public void testGetApplicationVersionCode_ReturnsNull_WhenNullContext() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		App.setAppContext(null);
		Runtime.getRuntime().gc();
		assertNull(systemInfoService.getApplicationVersionCode());
	}

	@Test
	public void testGetApplicationVersionCode_ReturnsNull_WhenNullPackageManager() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getPackageManager()).thenReturn(null);
		assertNull(systemInfoService.getApplicationVersionCode());
	}

	@Test
	public void testGetApplicationVersionCode_ReturnsNull_WhenNullPackageInfo() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getPackageManager()).thenReturn(mockPackageManager);
		when(mockContext.getPackageName()).thenReturn("com.adobe.mobile.test");
		when(mockPackageManager.getPackageInfo("com.adobe.mobile.test", 0)).thenReturn(null);
		assertNull(systemInfoService.getApplicationVersionCode());
	}

	@Test
	public void testGetApplicationVersionCode_ReturnsNull_WhenCodeIsZero() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getPackageManager()).thenReturn(mockPackageManager);
		when(mockContext.getPackageName()).thenReturn("com.adobe.mobile.test");
		when(mockPackageManager.getPackageInfo("com.adobe.mobile.test", 0)).thenReturn(mockPackageInfo);
		mockPackageInfo.versionCode = 0;
		assertNull(systemInfoService.getApplicationVersionCode());
	}

	@Test
	public void testGetApplicationVersionCode_ReturnsNull_WhenPackageManagerNotFound() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getPackageManager()).thenReturn(mockPackageManager);
		when(mockPackageManager.getPackageInfo(anyString(), anyInt())).thenThrow(PackageManager.NameNotFoundException.class);
		assertNull(systemInfoService.getApplicationVersionCode());
	}

	@Test
	public void testGetActiveLocale_Happy_SDKVersionLessThanAndroidNougat() throws Exception {
		setFinalStatic(Build.VERSION.class.getField("SDK_INT"), 22);
		setFinalStatic(Build.VERSION_CODES.class.getField("N"), 24);

		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getResources()).thenReturn(mockResources);
		when(mockResources.getConfiguration()).thenReturn(mockConfiguration);
		Locale testLocale = new Locale("en");
		mockConfiguration.locale = testLocale;
		Locale activeLocale = systemInfoService.getActiveLocale();
		assertEquals(testLocale, activeLocale);
	}

	@Test
	public void testGetActiveLocale_Happy_SDKVersionGreaterThanAndroidNougat() throws Exception {
		setFinalStatic(Build.VERSION.class.getField("SDK_INT"), 27);
		setFinalStatic(Build.VERSION_CODES.class.getField("N"), 24);

		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getResources()).thenReturn(mockResources);
		when(mockResources.getConfiguration()).thenReturn(mockConfiguration);
		Locale testLocale = new Locale("en");
		when(mockConfiguration.getLocales()).thenReturn(localeList);
		when(localeList.get(0)).thenReturn(testLocale);
		Locale activeLocale = systemInfoService.getActiveLocale();
		assertEquals(testLocale, activeLocale);
	}

	@Test
	public void testGetActiveLocale_WhenNullConfigutation_returnsNull() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getResources()).thenReturn(mockResources);
		when(mockResources.getConfiguration()).thenReturn(null);
		Locale activeLocale = systemInfoService.getActiveLocale();
		assertNull(activeLocale);
	}

	@Test
	public void testGetActiveLocale_NullContext() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		App.setAppContext(null);
		Runtime.getRuntime().gc();
		assertNull(systemInfoService.getActiveLocale());
	}

	@Test
	public void testGetDisplayInformation_Happy() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getResources()).thenReturn(mockResources);
		DisplayMetrics testDisplayMetrics = new DisplayMetrics();
		when(mockResources.getDisplayMetrics()).thenReturn(testDisplayMetrics);
		SystemInfoService.DisplayInformation info = systemInfoService.getDisplayInformation();
		assertNotNull(info);
		assertEquals(testDisplayMetrics.widthPixels, info.getWidthPixels());
		assertEquals(testDisplayMetrics.heightPixels, info.getHeightPixels());
		assertEquals(testDisplayMetrics.densityDpi, info.getDensityDpi());
	}

	@Test
	public void testGetDisplayInformation_NullResource() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getResources()).thenReturn(null);
		assertNull(systemInfoService.getDisplayInformation());
	}

	@Test
	public void testGetDisplayInformation_NullContext() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		App.setAppContext(null);
		Runtime.getRuntime().gc();
		assertNull(systemInfoService.getDisplayInformation());
	}

	@Test
	public void testGetOperatingSystemName_Happy() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		assertNotNull(systemInfoService.getOperatingSystemName());
	}

	@Test
	public void testGetOperatingSystemName_NullContext() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		App.setAppContext(null);
		Runtime.getRuntime().gc();
		assertNotNull(systemInfoService.getOperatingSystemName());
	}

	@Test
	public void testGetMobileCarrierName_Happy() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getSystemService(Application.TELEPHONY_SERVICE)).thenReturn(mockTelephonyManager);
		when(mockTelephonyManager.getNetworkOperatorName()).thenReturn("carrierName");
		assertEquals("carrierName", systemInfoService.getMobileCarrierName());
	}

	@Test
	public void testGetMobileCarrierName_NullContext() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		App.setAppContext(null);
		Runtime.getRuntime().gc();
		assertNull(systemInfoService.getMobileCarrierName());
	}

	@Test
	public void testGetNetworkConnectionStatusReturnsUnknown_When_NullContext() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		App.setAppContext(null);
		Runtime.getRuntime().gc();
		assertEquals(SystemInfoService.ConnectionStatus.UNKNOWN, systemInfoService.getNetworkConnectionStatus());
	}

	@Test
	public void testGetNetworkConnectionStatusReturnsUnknown_When_NullConnectivityService() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(null);
		assertEquals(SystemInfoService.ConnectionStatus.UNKNOWN, systemInfoService.getNetworkConnectionStatus());
	}

	@Test
	public void testGetNetworkConnectionStatusReturnsUnknown_When_NullActiveNetworkInfo() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(mockConnectivityManager);
		when(mockConnectivityManager.getActiveNetworkInfo()).thenReturn(null);
		assertEquals(SystemInfoService.ConnectionStatus.UNKNOWN, systemInfoService.getNetworkConnectionStatus());
	}

	@Test
	public void testGetNetworkConnectionStatusReturnsConnected_HappyFlow() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(mockConnectivityManager);
		when(mockConnectivityManager.getActiveNetworkInfo()).thenReturn(mockNetworkInfo);
		when(mockNetworkInfo.isAvailable()).thenReturn(true);
		when(mockNetworkInfo.isConnected()).thenReturn(true);
		assertEquals(SystemInfoService.ConnectionStatus.CONNECTED, systemInfoService.getNetworkConnectionStatus());
	}

	@Test
	public void testGetNetworkConnectionStatusReturnsDisconnected_When_ConnectivityAvailableButNotConnected() throws
		Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(mockConnectivityManager);
		when(mockConnectivityManager.getActiveNetworkInfo()).thenReturn(mockNetworkInfo);
		when(mockNetworkInfo.isAvailable()).thenReturn(true);
		when(mockNetworkInfo.isConnected()).thenReturn(false);
		assertEquals(SystemInfoService.ConnectionStatus.DISCONNECTED, systemInfoService.getNetworkConnectionStatus());
	}

	@Test
	public void testGetNetworkConnectionStatusReturnsDisconnected_When_ConnectivityNotAvailable() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(mockConnectivityManager);
		when(mockConnectivityManager.getActiveNetworkInfo()).thenReturn(mockNetworkInfo);
		when(mockNetworkInfo.isAvailable()).thenReturn(false);
		when(mockNetworkInfo.isConnected()).thenReturn(true);
		assertEquals(SystemInfoService.ConnectionStatus.DISCONNECTED, systemInfoService.getNetworkConnectionStatus());
	}

	@Test
	public void testGetAssets_when_FileNameisNull_should_returnNull() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getResources()).thenReturn(mockResources);
		when(mockResources.getAssets()).thenReturn(mockAssetManager);
		when(mockAssetManager.open("fileName")).thenReturn(new ByteArrayInputStream(
					"fileContents".getBytes(CHARSET_UTF_8)));
		assertNull(StringUtils.streamToString(systemInfoService.getAsset(null)));
	}

	@Test
	public void testGetAssets_when_FileNameisEmpty_should_returnNull() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getResources()).thenReturn(mockResources);
		when(mockResources.getAssets()).thenReturn(mockAssetManager);
		when(mockAssetManager.open("fileName")).thenReturn(new ByteArrayInputStream(
					"fileContents".getBytes(CHARSET_UTF_8)));
		assertNull(StringUtils.streamToString(systemInfoService.getAsset("")));
	}

	@Test
	public void testGetAssets_when_FileNameisWrong_should_returnNull() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getResources()).thenReturn(mockResources);
		when(mockResources.getAssets()).thenReturn(mockAssetManager);
		when(mockAssetManager.open("fileName")).thenReturn(new ByteArrayInputStream(
					"fileContents".getBytes(CHARSET_UTF_8)));
		assertNull(StringUtils.streamToString(systemInfoService.getAsset("WrongfileName")));
	}

	@Test
	public void testGetAssets_when_nullContext_should_returnNull() throws Exception {
		App.setAppContext(null);
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getResources()).thenReturn(mockResources);
		when(mockResources.getAssets()).thenReturn(mockAssetManager);
		when(mockAssetManager.open("fileName")).thenReturn(new ByteArrayInputStream(
					"fileContents".getBytes(CHARSET_UTF_8)));
		assertNull(StringUtils.streamToString(systemInfoService.getAsset("fileName")));
	}

	@Test
	public void testGetAssets_when_nullResourceManager_should_returnNull() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getResources()).thenReturn(null);
		when(mockResources.getAssets()).thenReturn(mockAssetManager);
		when(mockAssetManager.open("fileName")).thenReturn(new ByteArrayInputStream(
					"fileContents".getBytes(CHARSET_UTF_8)));
		assertNull(StringUtils.streamToString(systemInfoService.getAsset("fileName")));
	}


	@Test
	public void testGetAssets_when_nullAssetManager_should_returnNull() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getResources()).thenReturn(mockResources);
		when(mockResources.getAssets()).thenReturn(null);
		when(mockAssetManager.open("fileName")).thenReturn(new ByteArrayInputStream(
					"fileContents".getBytes(CHARSET_UTF_8)));
		assertNull(StringUtils.streamToString(systemInfoService.getAsset("fileName")));
	}

	@Test
	public void testGetAssets_when_FileNameisValid_should_returnContentOftheFile() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getResources()).thenReturn(mockResources);
		when(mockResources.getAssets()).thenReturn(mockAssetManager);
		when(mockAssetManager.open("fileName")).thenReturn(new ByteArrayInputStream(
					"fileContents".getBytes(CHARSET_UTF_8)));
		assertEquals("fileContents", StringUtils.streamToString(systemInfoService.getAsset("fileName")));
	}

	@Test
	public void testGetProperty_when_PropertyKeyNull_should_returnNull() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getPackageManager()).thenReturn(mockPackageManager);
		when(mockPackageManager.getApplicationInfo(anyString(), anyInt())).thenReturn(mockApplicationInfo);
		mockApplicationInfo.metaData = mockBundle;
		when(mockBundle.getString("propertykey")).thenReturn("propertyValue");
		assertNull(systemInfoService.getProperty(null));
	}

	@Test
	public void testGetProperty_when_PropertyKeyWrong_should_returnNull() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getPackageName()).thenReturn("com.adobe.mobile.test");
		when(mockContext.getPackageManager()).thenReturn(mockPackageManager);
		when(mockPackageManager.getApplicationInfo(anyString(), anyInt())).thenReturn(mockApplicationInfo);
		mockApplicationInfo.metaData = mockBundle;
		when(mockBundle.getString("propertykey")).thenReturn("propertyValue");
		assertNull(systemInfoService.getProperty("WrongKey"));
	}


	@Test
	public void testGetProperty_when_nullContext_should_returnNull() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();

		when(mockPackageManager.getApplicationInfo(anyString(), anyInt())).thenReturn(mockApplicationInfo);
		mockApplicationInfo.metaData = mockBundle;
		when(mockBundle.getString("propertykey")).thenReturn("propertyValue");
		assertNull(systemInfoService.getProperty("propertykey"));
	}

	@Test
	public void testGetProperty_when_nullPackageManager_should_returnNull() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getPackageManager()).thenReturn(null);
		when(mockPackageManager.getApplicationInfo(anyString(), anyInt())).thenReturn(mockApplicationInfo);
		mockApplicationInfo.metaData = mockBundle;
		when(mockBundle.getString("propertykey")).thenReturn("propertyValue");
		assertNull(systemInfoService.getProperty("propertykey"));
	}

	@Test
	public void testGetProperty_when_nullApplicationInfo_should_returnNull() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getPackageManager()).thenReturn(mockPackageManager);
		when(mockContext.getPackageName()).thenReturn("com.adobe.mobile.test");
		when(mockPackageManager.getApplicationInfo(anyString(), anyInt())).thenReturn(null);
		mockApplicationInfo.metaData = mockBundle;
		when(mockBundle.getString("propertykey")).thenReturn("propertyValue");
		assertNull(systemInfoService.getProperty("propertykey"));
	}

	@Test
	public void testGetProperty_when_nullBundle_should_returnNull() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getPackageManager()).thenReturn(mockPackageManager);
		when(mockContext.getPackageName()).thenReturn("com.adobe.mobile.test");
		when(mockPackageManager.getApplicationInfo(anyString(), anyInt())).thenReturn(mockApplicationInfo);
		mockApplicationInfo.metaData = null;
		when(mockBundle.getString("propertykey")).thenReturn("propertyValue");
		assertNull(systemInfoService.getProperty("propertykey"));
	}

	@Test
	public void testGetProperty_when_PropertyKeyValid_should_returnCorrectValue() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getPackageManager()).thenReturn(mockPackageManager);
		when(mockContext.getPackageName()).thenReturn("com.adobe.mobile.test");
		when(mockPackageManager.getApplicationInfo(anyString(), anyInt())).thenReturn(mockApplicationInfo);
		mockApplicationInfo.metaData = mockBundle;
		when(mockBundle.getString("propertykey")).thenReturn("propertyValue");
		assertEquals("propertyValue", systemInfoService.getProperty("propertykey"));
	}

	@Test
	public void testgetCanonicalPlatformName_returns_Android() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		assertEquals("android", systemInfoService.getCanonicalPlatformName());
	}


	@Test
	public void testGetOperatingSystemVersion_Happy() throws Exception {
		setFinalStatic(Build.VERSION.class.getField("RELEASE"), "MockedOS");
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		assertEquals("MockedOS", systemInfoService.getOperatingSystemVersion());
	}


	@Test
	public void testgetRunMode_returns_Application() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		assertEquals("Application", systemInfoService.getRunMode());
	}

	@Test
	public void testgetDeviceBuildId_returns_CorrectData() throws Exception {
		// Stub Build ID
		setFinalStatic(Build.class.getField("ID"), "BuildIDValue");

		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		assertEquals("BuildIDValue", systemInfoService.getDeviceBuildId());
	}

	@Test
	public void testgetDeviceManufacturer_returns_CorrectData() throws Exception {
		// Stub Build MANUFACTURER
		setFinalStatic(Build.class.getField("MANUFACTURER"), "manufacturerName");

		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		assertEquals("manufacturerName", systemInfoService.getDeviceManufacturer());
	}

	@Test
	public void testgetDeviceName_returns_CorrectData() throws Exception {
		// Stub Build MODEL
		setFinalStatic(Build.class.getField("MODEL"), "modelName");

		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		assertEquals("modelName", systemInfoService.getDeviceName());
	}

	@Test
	public void testgetDeviceType_returns_CorrectData() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getResources()).thenReturn(mockResources);
		when(mockResources.getConfiguration()).thenReturn(mockConfiguration);
		when(mockResources.getDisplayMetrics()).thenReturn(mockDisplayMetrics);
		mockConfiguration.uiMode = 1;
		assertEquals(SystemInfoService.DeviceType.PHONE, systemInfoService.getDeviceType());
	}

	@Test
	public void testGetLocaleStringReturnDefault_When_DefaultLocale() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		String result = systemInfoService.getLocaleString();
		assertEquals("en-US", result);
	}

	@Test
	public void testGetLocaleString_When_UKLocale() throws Exception {
		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getResources()).thenReturn(mockResources);
		when(mockResources.getConfiguration()).thenReturn(mockConfiguration);
		mockConfiguration.locale = Locale.UK;
		String result = systemInfoService.getLocaleString();
		assertEquals("en-GB", result);
	}

	@Test
	public void testGetLocaleString_Happy_SDKVersionLessThanAndroidLollipop() throws Exception {
		setFinalStatic(Build.VERSION.class.getField("SDK_INT"), 20);
		setFinalStatic(Build.VERSION_CODES.class.getField("LOLLIPOP"), 21);

		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getResources()).thenReturn(mockResources);
		when(mockResources.getConfiguration()).thenReturn(mockConfiguration);
		mockConfiguration.locale = Locale.CANADA;

		String result = systemInfoService.getLocaleString();

		assertEquals(result, "en-CA");
	}

	@Test
	public void testGetLocaleString_Happy_SDKVersionIsAndroidLollipop() throws Exception {
		setFinalStatic(Build.VERSION.class.getField("SDK_INT"), 21);
		setFinalStatic(Build.VERSION_CODES.class.getField("LOLLIPOP"), 21);

		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getResources()).thenReturn(mockResources);
		when(mockResources.getConfiguration()).thenReturn(mockConfiguration);
		mockConfiguration.locale = Locale.CHINA;

		String result = systemInfoService.getLocaleString();

		assertEquals(result, "zh-CN");
	}

	@Test
	public void testGetLocaleString_Happy_SDKVersionHigherThanAndroidLollipop() throws Exception {
		setFinalStatic(Build.VERSION.class.getField("SDK_INT"), 25);
		setFinalStatic(Build.VERSION_CODES.class.getField("LOLLIPOP"), 21);

		AndroidSystemInfoService systemInfoService = new AndroidSystemInfoService();
		when(mockContext.getResources()).thenReturn(mockResources);
		when(mockResources.getConfiguration()).thenReturn(mockConfiguration);
		when(mockConfiguration.getLocales()).thenReturn(localeList);
		when(localeList.get(0)).thenReturn(Locale.FRANCE);

		String result = systemInfoService.getLocaleString();

		assertEquals(result, "fr-FR");
	}

	static void setFinalStatic(Field field, Object newValue) throws Exception {
		field.setAccessible(true);
		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
		field.set(null, newValue);
	}


}
