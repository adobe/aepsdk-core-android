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

package com.adobe.marketing.mobile.lifecycle;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.adobe.marketing.mobile.services.DeviceInforming;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class LifecycleV2MetricsBuilderTest {

    @Mock private DeviceInforming deviceInfoService;

    private final long startTimestamp =
            1483889568301L; // Sunday, January 8, 2017 3:32:48.301 PM GMT
    private LifecycleV2MetricsBuilder xdmMetricsBuilder;
    private static final Map<String, Object> expectedDeviceEnvironmentData = new HashMap<>();

    @BeforeClass
    public static void beforeAll() {
        expectedDeviceEnvironmentData.put(
                "environment",
                new HashMap<String, Object>() {
                    {
                        put("carrier", "TEST_CARRIER");
                        put("operatingSystemVersion", "5.55");
                        put("operatingSystem", "TEST_OS");
                        put("type", "application");
                        put(
                                "_dc",
                                new HashMap<String, Object>() {
                                    {
                                        put("language", "en-US");
                                    }
                                });
                    }
                });
        expectedDeviceEnvironmentData.put(
                "device",
                new HashMap<String, Object>() {
                    {
                        put("manufacturer", "TEST_MANUFACTURER");
                        put("model", "deviceName");
                        put("modelNumber", "TEST_PLATFORM");
                        put("type", "mobile");
                        put("screenHeight", 100);
                        put("screenWidth", 100);
                    }
                });
    }

    @Before
    public void beforeEach() {
        LifecycleTestHelper.initDeviceInfoService(deviceInfoService);
        when(deviceInfoService.getDeviceManufacturer()).thenReturn("TEST_MANUFACTURER");
        when(deviceInfoService.getApplicationPackageName()).thenReturn("test.package.name");
        when(deviceInfoService.getDeviceType()).thenReturn(DeviceInforming.DeviceType.PHONE);
        xdmMetricsBuilder = new LifecycleV2MetricsBuilder(deviceInfoService);
    }

    @Test
    public void testBuildAppLaunchXDMData_returnsCorrectData_whenIsInstall() {
        Map<String, Object> actualAppLaunchData =
                xdmMetricsBuilder.buildAppLaunchXDMData(startTimestamp, true, false);

        // Verify
        Map<String, Object> expectedData = new HashMap<>();
        expectedData.put(
                "application",
                new HashMap<String, Object>() {
                    {
                        put("name", "TEST_APPLICATION_NAME");
                        put("id", "test.package.name");
                        put("version", "1.1 (12345)");
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
        Map<String, Object> actualAppLaunchData =
                xdmMetricsBuilder.buildAppLaunchXDMData(startTimestamp, false, true);

        // Verify
        Map<String, Object> expectedData = new HashMap<String, Object>();
        expectedData.put(
                "application",
                new HashMap<String, Object>() {
                    {
                        put("name", "TEST_APPLICATION_NAME");
                        put("id", "test.package.name");
                        put("version", "1.1 (12345)");
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
    public void
            testBuildAppLaunchXDMData_returnsCorrectData_whenIsInstall_whenNullSystemInfoService() {
        xdmMetricsBuilder = new LifecycleV2MetricsBuilder(null);
        Map<String, Object> actualAppLaunchData =
                xdmMetricsBuilder.buildAppLaunchXDMData(startTimestamp, true, false);

        // Verify
        Map<String, Object> expectedData = new HashMap<String, Object>();
        expectedData.put(
                "application",
                new HashMap<String, Object>() {
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
        Map<String, Object> actualAppLaunchData =
                xdmMetricsBuilder.buildAppLaunchXDMData(startTimestamp, false, false);

        // Verify
        Map<String, Object> expectedData = new HashMap<String, Object>();
        expectedData.put(
                "application",
                new HashMap<String, Object>() {
                    {
                        put("name", "TEST_APPLICATION_NAME");
                        put("id", "test.package.name");
                        put("version", "1.1 (12345)");
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
        Map<String, Object> actualAppCloseData =
                xdmMetricsBuilder.buildAppCloseXDMData(
                        1483864368225L, // start: Sunday, January 8, 2017 8:32:48.225 AM GMT
                        1483864390225L, // close: Sunday, January 8, 2017 8:33:10.225 AM GMT
                        1483864390225L,
                        true);
        // Verify
        Map<String, Object> expectedData = new HashMap<String, Object>();
        expectedData.put(
                "application",
                new HashMap<String, Object>() {
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
    public void
            testBuildAppCloseXDMData_returnsCorrectData_whenIsCloseWithCloseUnknown_noCloseTimestamp() {
        Map<String, Object> actualAppCloseData =
                xdmMetricsBuilder.buildAppCloseXDMData(
                        1483864368225L, // start: Sunday, January 8, 2017 8:32:48.225 AM GMT
                        0, // simulate crash, no previous close timestamp
                        1483864390225L, // fallback close (new start ts - 1): Sunday, January 8,
                        // 2017 8:33:10.225 AM GMT
                        true);
        // Verify
        Map<String, Object> expectedData = new HashMap<String, Object>();
        expectedData.put(
                "application",
                new HashMap<String, Object>() {
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
        Map<String, Object> actualAppCloseData =
                xdmMetricsBuilder.buildAppCloseXDMData(
                        1483864368225L, // start: Sunday, January 8, 2017 8:32:48.225 AM GMT
                        1483864390225L, // pause: Sunday, January 8, 2017 8:33:10.225 AM GMT
                        1483864390225L,
                        false);

        // Verify
        Map<String, Object> expectedData = new HashMap<String, Object>();
        expectedData.put(
                "application",
                new HashMap<String, Object>() {
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
        Map<String, Object> actualAppCloseData =
                xdmMetricsBuilder.buildAppCloseXDMData(
                        1483864368225L, // start: Sunday, January 8, 2017 8:32:48.225 AM GMT
                        1483864390225L, // pause: Sunday, January 8, 2017 8:33:10.225 AM GMT
                        1483864390225L,
                        false);

        // Verify
        Map<String, Object> expectedData = new HashMap<String, Object>();
        expectedData.put(
                "application",
                new HashMap<String, Object>() {
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
    public void
            testBuildAppCloseXDMData_zeroSessionLength_when_launchTimestamp_greaterThan_closeTimestamp() {
        xdmMetricsBuilder = new LifecycleV2MetricsBuilder(null);
        Map<String, Object> actualAppCloseData =
                xdmMetricsBuilder.buildAppCloseXDMData(
                        1483864390225L, // start: Sunday, January 8, 2017 8:33:10.225 AM GMT
                        1483864368225L, // pause: Sunday, January 8, 2017 8:32:48.225 AM GMT
                        1483864390225L,
                        false);

        // Verify
        Map<String, Object> expectedData = new HashMap<String, Object>();
        expectedData.put(
                "application",
                new HashMap<String, Object>() {
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
        Map<String, Object> actualAppCloseData =
                xdmMetricsBuilder.buildAppCloseXDMData(
                        0L, // start:
                        1483864390225L, // pause: Sunday, January 8, 2017 8:33:10.225 AM GMT
                        1483864390225L,
                        false);

        // Verify
        Map<String, Object> expectedData = new HashMap<String, Object>();
        expectedData.put(
                "application",
                new HashMap<String, Object>() {
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
        Map<String, Object> actualAppCloseData =
                xdmMetricsBuilder.buildAppCloseXDMData(
                        1L, (Integer.MAX_VALUE * 1000L) + 1L, 1483864390225L, false);

        // Verify
        Map<String, Object> expectedData = new HashMap<String, Object>();
        expectedData.put(
                "application",
                new HashMap<String, Object>() {
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
        // When close timestamp is Long MAX_VALUE, the resulting session length is larger than
        // Integer MAX_VALUE
        xdmMetricsBuilder = new LifecycleV2MetricsBuilder(null);
        Map<String, Object> actualAppCloseData =
                xdmMetricsBuilder.buildAppCloseXDMData(1L, Long.MAX_VALUE, 1483864390225L, false);

        // Verify
        Map<String, Object> expectedData = new HashMap<String, Object>();
        expectedData.put(
                "application",
                new HashMap<String, Object>() {
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
