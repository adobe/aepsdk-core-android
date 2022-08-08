/* **************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2021 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 *
 * *************************************************************************/

package com.adobe.marketing.mobile;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class LifecycleV2MetricsBuilderTest extends BaseTest {
	private FakePlatformServices          fakePlatformServices;
	private LocalStorageService.DataStore lifecycleDataStore;
	private MockSystemInfoService         mockSystemInfoService;
	private long startTimestamp = 1483889568301L; // Sunday, January 8, 2017 3:32:48.301 PM GMT
	private long closeTimestamp = 1483889123301L; // Sunday, January 8, 2017 3:25:23.301 PM GMT, previous session
	private LifecycleV2MetricsBuilder xdmMetricsBuilder;
	private static Map<String, Object> expectedDeviceEnvironmentData = new HashMap<String, Object>();

	@BeforeClass
	public static void beforeAll() {
		expectedDeviceEnvironmentData.put("environment", new HashMap<String, Object>() {
			{
				put("carrier", "TEST_CARRIER_NAME");
				put("operatingSystemVersion", "5.55");
				put("operatingSystem", "TEST_OS");
				put("type", "application");
				put("_dc", new HashMap<String, Object>() {
					{
						put("language", "en-US");
					}
				});
			}
		});
		expectedDeviceEnvironmentData.put("device", new HashMap<String, Object>() {
			{
				put("manufacturer", "TEST_MANUFACTURER");
				put("model", "TEST_DEVICE_NAME");
				put("modelNumber", "TEST_DEVICE_ID");
				put("type", "mobile");
				put("screenHeight", 750);
				put("screenWidth", 1334);
			}
		});
	}

	@Before
	public void beforeEach() {
		fakePlatformServices = new FakePlatformServices();
		lifecycleDataStore = fakePlatformServices.getLocalStorageService().getDataStore("LIFECYCLE_DATASTORE");
		mockSystemInfoService = fakePlatformServices.getMockSystemInfoService();
		mockSystemInfoService.applicationName = "TEST_APPLICATION_NAME";
		mockSystemInfoService.applicationVersion = "1.11";
		mockSystemInfoService.applicationVersionCode = "12345";
		mockSystemInfoService.deviceName = "TEST_DEVICE_NAME";
		mockSystemInfoService.deviceBuildId = "TEST_DEVICE_ID";
		mockSystemInfoService.mockCanonicalPlatformName = "TEST_OS";
		mockSystemInfoService.operatingSystemName = "TEST_OS";
		mockSystemInfoService.operatingSystemVersion = "5.55";
		mockSystemInfoService.mobileCarrierName = "TEST_CARRIER_NAME";
		mockSystemInfoService.runMode = "APPLICATION";
		mockSystemInfoService.activeLocale = new Locale("en", "US");
		mockSystemInfoService.deviceManufacturer = "TEST_MANUFACTURER";
		mockSystemInfoService.applicationPackageName = "test.package.name";
		mockSystemInfoService.displayInformation = new SystemInfoService.DisplayInformation() {
			@Override
			public int getWidthPixels() {
				return 1334;
			}

			@Override
			public int getHeightPixels() {
				return 750;
			}

			@Override
			public int getDensityDpi() {
				return 500;
			}
		};
		xdmMetricsBuilder = new LifecycleV2MetricsBuilder(mockSystemInfoService);
	}

	@After
	public void cleanUp() {
		if (lifecycleDataStore != null) {
			lifecycleDataStore.removeAll();
		}
	}

	@Test
	public void testBuildAppLaunchXDMData_returnsCorrectData_whenIsInstall() {
		Map<String, Object> actualAppLaunchData = xdmMetricsBuilder.buildAppLaunchXDMData(startTimestamp, true, false);

		// Verify
		Map<String, Object> expectedData = new HashMap<String, Object>();
		expectedData.put("application", new HashMap<String, Object>() {
			{
				put("name", "TEST_APPLICATION_NAME");
				put("id", "test.package.name");
				put("version", "1.11 (12345)");
				put("isInstall", true);
				put("isLaunch", true);
			}
		});
		expectedData.put("eventType", "application.launch");
		expectedData.put("timestamp", "2017-01-08T15:32:48.301Z");
		expectedData.putAll(expectedDeviceEnvironmentData);

		assertEquals(expectedData, actualAppLaunchData);
	}

	@Test
	public void testBuildAppLaunchXDMData_returnsCorrectData_whenIsUpgradeEvent() {
		Map<String, Object> actualAppLaunchData = xdmMetricsBuilder.buildAppLaunchXDMData(startTimestamp, false, true);

		// Verify
		Map<String, Object> expectedData = new HashMap<String, Object>();
		expectedData.put("application", new HashMap<String, Object>() {
			{
				put("name", "TEST_APPLICATION_NAME");
				put("id", "test.package.name");
				put("version", "1.11 (12345)");
				put("isUpgrade", true);
				put("isLaunch", true);
			}
		});
		expectedData.put("eventType", "application.launch");
		expectedData.put("timestamp", "2017-01-08T15:32:48.301Z");
		expectedData.putAll(expectedDeviceEnvironmentData);

		assertEquals(expectedData, actualAppLaunchData);
	}


	@Test
	public void testBuildAppLaunchXDMData_returnsCorrectData_whenIsInstall_whenNullSystemInfoService() {
		xdmMetricsBuilder = new LifecycleV2MetricsBuilder(null);
		Map<String, Object> actualAppLaunchData = xdmMetricsBuilder.buildAppLaunchXDMData(startTimestamp, true, false);

		// Verify
		Map<String, Object> expectedData = new HashMap<String, Object>();
		expectedData.put("application", new HashMap<String, Object>() {
			{
				put("isInstall", true);
				put("isLaunch", true);
			}
		});
		expectedData.put("eventType", "application.launch");
		expectedData.put("timestamp", "2017-01-08T15:32:48.301Z");

		assertEquals(expectedData, actualAppLaunchData);
	}

	@Test
	public void testBuildAppLaunchXDMData_returnsCorrectData_whenIsLaunch() {
		Map<String, Object> actualAppLaunchData = xdmMetricsBuilder.buildAppLaunchXDMData(startTimestamp, false, false);

		// Verify
		Map<String, Object> expectedData = new HashMap<String, Object>();
		expectedData.put("application", new HashMap<String, Object>() {
			{
				put("name", "TEST_APPLICATION_NAME");
				put("id", "test.package.name");
				put("version", "1.11 (12345)");
				put("isLaunch", true);
			}
		});
		expectedData.put("eventType", "application.launch");
		expectedData.put("timestamp", "2017-01-08T15:32:48.301Z");
		expectedData.putAll(expectedDeviceEnvironmentData);

		assertEquals(expectedData, actualAppLaunchData);
	}

	@Test
	public void testBuildAppCloseXDMData_returnsCorrectData_whenIsCloseWithCloseUnknown() {
		Map<String, Object> actualAppCloseData = xdmMetricsBuilder.buildAppCloseXDMData(
					1483864368225L, // start: Sunday, January 8, 2017 8:32:48.225 AM GMT
					1483864390225L, // close: Sunday, January 8, 2017 8:33:10.225 AM GMT
					1483864390225L,
					true);
		// Verify
		Map<String, Object> expectedData = new HashMap<String, Object>();
		expectedData.put("application", new HashMap<String, Object>() {
			{
				put("isClose", true);
				put("closeType", "unknown");
				put("sessionLength", 22);
			}
		});
		expectedData.put("eventType", "application.close");
		expectedData.put("timestamp", "2017-01-08T08:33:10.225Z");

		assertEquals(expectedData, actualAppCloseData);
	}

	@Test
	public void testBuildAppCloseXDMData_returnsCorrectData_whenIsCloseWithCloseUnknown_noCloseTimestamp() {
		Map<String, Object> actualAppCloseData = xdmMetricsBuilder.buildAppCloseXDMData(
					1483864368225L, // start: Sunday, January 8, 2017 8:32:48.225 AM GMT
					0, // simulate crash, no previous close timestamp
					1483864390225L, // fallback close (new start ts - 1): Sunday, January 8, 2017 8:33:10.225 AM GMT
					true);
		// Verify
		Map<String, Object> expectedData = new HashMap<String, Object>();
		expectedData.put("application", new HashMap<String, Object>() {
			{
				put("isClose", true);
				put("closeType", "unknown");
			}
		});
		expectedData.put("eventType", "application.close");
		expectedData.put("timestamp", "2017-01-08T08:33:10.225Z");

		assertEquals(expectedData, actualAppCloseData);
	}

	@Test
	public void testBuildAppCloseXDMData_returnsCorrectData_whenIsCloseCorrectSession() {
		Map<String, Object> actualAppCloseData = xdmMetricsBuilder.buildAppCloseXDMData(
					1483864368225L, // start: Sunday, January 8, 2017 8:32:48.225 AM GMT
					1483864390225L, // pause: Sunday, January 8, 2017 8:33:10.225 AM GMT
					1483864390225L,
					false);

		// Verify
		Map<String, Object> expectedData = new HashMap<String, Object>();
		expectedData.put("application", new HashMap<String, Object>() {
			{
				put("isClose", true);
				put("closeType", "close");
				put("sessionLength", 22);
			}
		});
		expectedData.put("eventType", "application.close");
		expectedData.put("timestamp", "2017-01-08T08:33:10.225Z");

		assertEquals(expectedData, actualAppCloseData);
	}

	@Test
	public void testBuildAppCloseXDMData_doesNotCrash_whenNullSystemInfo() {
		xdmMetricsBuilder = new LifecycleV2MetricsBuilder(null);
		Map<String, Object> actualAppCloseData = xdmMetricsBuilder.buildAppCloseXDMData(
					1483864368225L, // start: Sunday, January 8, 2017 8:32:48.225 AM GMT
					1483864390225L, // pause: Sunday, January 8, 2017 8:33:10.225 AM GMT
					1483864390225L,
					false);

		// Verify
		Map<String, Object> expectedData = new HashMap<String, Object>();
		expectedData.put("application", new HashMap<String, Object>() {
			{
				put("isClose", true);
				put("closeType", "close");
				put("sessionLength", 22);
			}
		});
		expectedData.put("eventType", "application.close");
		expectedData.put("timestamp", "2017-01-08T08:33:10.225Z");
		assertEquals(expectedData, actualAppCloseData);
	}

	@Test
	public void testBuildAppCloseXDMData_zeroSessionLength_when_launchTimestamp_greaterThan_closeTimestamp() {
		xdmMetricsBuilder = new LifecycleV2MetricsBuilder(null);
		Map<String, Object> actualAppCloseData = xdmMetricsBuilder.buildAppCloseXDMData(
					1483864390225L, // start: Sunday, January 8, 2017 8:33:10.225 AM GMT
					1483864368225L, // pause: Sunday, January 8, 2017 8:32:48.225 AM GMT
					1483864390225L,
					false);

		// Verify
		Map<String, Object> expectedData = new HashMap<String, Object>();
		expectedData.put("application", new HashMap<String, Object>() {
			{
				put("isClose", true);
				put("closeType", "close");
			}
		});
		expectedData.put("eventType", "application.close");
		expectedData.put("timestamp", "2017-01-08T08:32:48.225Z");
		assertEquals(expectedData, actualAppCloseData);
	}

	@Test
	public void testBuildAppCloseXDMData_zeroSessionLength_when_launchTimestamp_isZero() {
		xdmMetricsBuilder = new LifecycleV2MetricsBuilder(null);
		Map<String, Object> actualAppCloseData = xdmMetricsBuilder.buildAppCloseXDMData(
					0L, // start:
					1483864390225L, // pause: Sunday, January 8, 2017 8:33:10.225 AM GMT
					1483864390225L,
					false);

		// Verify
		Map<String, Object> expectedData = new HashMap<String, Object>();
		expectedData.put("application", new HashMap<String, Object>() {
			{
				put("isClose", true);
				put("closeType", "close");
			}
		});
		expectedData.put("eventType", "application.close");
		expectedData.put("timestamp", "2017-01-08T08:33:10.225Z");
		assertEquals(expectedData, actualAppCloseData);
	}

	@Test
	public void testBuildAppCloseXDMData_validSessionLength_when_sessionLength_integerMaxValue() {
		// sessionLengthSeconds = (Close timestamp - Launch timestamp) / 1000 = Integer.MAX_VALUE
		xdmMetricsBuilder = new LifecycleV2MetricsBuilder(null);
		Map<String, Object> actualAppCloseData = xdmMetricsBuilder.buildAppCloseXDMData(
					1L,
					(Integer.MAX_VALUE * 1000L) + 1L,
					1483864390225L,
					false);

		// Verify
		Map<String, Object> expectedData = new HashMap<String, Object>();
		expectedData.put("application", new HashMap<String, Object>() {
			{
				put("isClose", true);
				put("closeType", "close");
				put("sessionLength", 2147483647);
			}
		});
		expectedData.put("eventType", "application.close");
		expectedData.put("timestamp", "2038-01-19T03:14:07.001Z");
		assertEquals(expectedData, actualAppCloseData);
	}

	@Test
	public void testBuildAppCloseXDMData_zeroSessionLength_when_sessionLength_overflows() {
		// When close timestamp is Long MAX_VALUE, the resulting session length is larger than Integer MAX_VALUE
		xdmMetricsBuilder = new LifecycleV2MetricsBuilder(null);
		Map<String, Object> actualAppCloseData = xdmMetricsBuilder.buildAppCloseXDMData(
					1L,
					Long.MAX_VALUE,
					1483864390225L,
					false);

		// Verify
		Map<String, Object> expectedData = new HashMap<String, Object>();
		expectedData.put("application", new HashMap<String, Object>() {
			{
				put("isClose", true);
				put("closeType", "close");
			}
		});
		expectedData.put("eventType", "application.close");
		expectedData.put("timestamp", "292278994-08-17T07:12:55.807Z");
		assertEquals(expectedData, actualAppCloseData);
	}
}