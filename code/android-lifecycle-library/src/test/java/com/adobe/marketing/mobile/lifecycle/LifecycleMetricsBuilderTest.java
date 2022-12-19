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

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.adobe.marketing.mobile.services.DeviceInforming;
import com.adobe.marketing.mobile.services.NamedCollection;
import java.util.Map;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class LifecycleMetricsBuilderTest {

    @Mock NamedCollection lifecycleDataStore;

    @Mock DeviceInforming mockDeviceInfoService;

    private static final String CARRIER_NAME = "carriername";
    private static final String RUN_MODE = "runmode";
    private static final String OPERATING_SYSTEM = "osversion";
    private static final String APPLICATION_IDENTIFIER = "appid";
    private static final String DEVICE_NAME = "devicename";
    private static final String DEVICE_RESOLUTION = "resolution";

    private static final String DATASTORE_KEY_INSTALL_DATE = "InstallDate";
    private static final String DATASTORE_KEY_LAST_USED_DATE = "LastDateUsed";
    private static final String DATASTORE_KEY_LAUNCHES = "Launches";
    private static final String DATASTORE_KEY_LAUNCHES_AFTER_UPGRADE = "LaunchesAfterUpgrade";
    private static final String DATASTORE_KEY_LAST_VERSION = "LastVersion";
    private static final String DATASTORE_KEY_PAUSE_DATE = "PauseDate";
    private static final String DATASTORE_KEY_UPGRADE_DATE = "UpgradeDate";

    private static final String CONTEXT_DATA_KEY_CRASH_EVENT = "crashevent";
    private static final String CONTEXT_DATA_KEY_EVENT_INSTALL = "installevent";
    private static final String CONTEXT_DATA_KEY_EVENT_UPGRADE = "upgradeevent";
    private static final String CONTEXT_DATA_KEY_EVENT_DAILY_ENGAGED = "dailyenguserevent";
    private static final String CONTEXT_DATA_KEY_EVENT_MONTHLY_ENGAGED = "monthlyenguserevent";
    private static final String CONTEXT_DATA_KEY_INSTALL_DATE = "installdate";
    private static final String CONTEXT_DATA_KEY_LAUNCH_EVENT_KEY = "launchevent";
    private static final String CONTEXT_DATA_KEY_DAYS_SINCE_FIRST_LAUNCH = "dayssincefirstuse";
    private static final String CONTEXT_DATA_KEY_DAYS_SINCE_LAST_LAUNCH = "dayssincelastuse";
    private static final String CONTEXT_DATA_KEY_DAYS_SINCE_LAST_UPGRADE = "dayssincelastupgrade";
    private static final String CONTEXT_DATA_KEY_LAUNCHES_SINCE_UPGRADE = "launchessinceupgrade";
    private static final String CONTEXT_DATA_KEY_LAUNCHES = "launches";
    private static final String CONTEXT_DATA_KEY_HOUR_OF_DAY = "hourofday";
    private static final String CONTEXT_DATA_KEY_DAY_OF_WEEK = "dayofweek";

    private static final String CONTEXT_DATA_VALUE_UPGRADE_EVENT = "UpgradeEvent";
    private static final String CONTEXT_DATA_VALUE_LAUNCH_EVENT = "LaunchEvent";
    private static final String CONTEXT_DATA_VALUE_CRASH_EVENT = "CrashEvent";
    private static final String CONTEXT_DATA_VALUE_INSTALL_EVENT = "InstallEvent";
    private static final String CONTEXT_DATA_VALUE_DAILY_ENG_USER_EVENT = "DailyEngUserEvent";
    private static final String CONTEXT_DATA_VALUE_MONTHLY_ENG_USER_EVENT = "MonthlyEngUserEvent";

    @Before
    public void beforeEach() {
        // set GMT-08:00 required to have the same expected result, no mather what timezone is used
        System.setProperty("user.timezone", "America/Los_Angeles");
        TimeZone.setDefault(null); // this will reset the default timezone set at JVM startup

        LifecycleTestHelper.initDeviceInfoService(mockDeviceInfoService);
    }

    @Test
    public void testLifecycleBuilder_ReturnsCorrectData_WhenInstallEvent() {
        // Sunday, January 8, 2017 7:32:48 AM
        Map<String, String> installData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, lifecycleDataStore, 1483889568L)
                        .addInstallData()
                        .build();

        // Check data equals expected
        assertEquals("1/8/2017", installData.get(CONTEXT_DATA_KEY_INSTALL_DATE));
        assertEquals(
                CONTEXT_DATA_VALUE_INSTALL_EVENT, installData.get(CONTEXT_DATA_KEY_EVENT_INSTALL));
    }

    @Test
    public void testLifecycleBuilder_ReturnsCorrectData_WhenInstallEvent_TwoDigitMonth() {
        // Sunday, November 8, 2017 7:32:48 AM
        Map<String, String> installData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, lifecycleDataStore, 1510155168L)
                        .addInstallData()
                        .build();

        // Check data equals expected
        assertEquals("11/8/2017", installData.get(CONTEXT_DATA_KEY_INSTALL_DATE));
        assertEquals(
                CONTEXT_DATA_VALUE_INSTALL_EVENT, installData.get(CONTEXT_DATA_KEY_EVENT_INSTALL));
    }

    @Test
    public void testLifecycleBuilder_ReturnsCorrectData_WhenInstallEvent_TwoDigitDay() {
        // Sunday, January 10, 2017 7:32:48 AM
        Map<String, String> installData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, lifecycleDataStore, 1484062368L)
                        .addInstallData()
                        .build();

        // Check data equals expected
        assertEquals("1/10/2017", installData.get(CONTEXT_DATA_KEY_INSTALL_DATE));
        assertEquals(
                CONTEXT_DATA_VALUE_INSTALL_EVENT, installData.get(CONTEXT_DATA_KEY_EVENT_INSTALL));

        // Verify we increase persisted launch number
        assertEquals(0, lifecycleDataStore.getInt("LaunchNumber", 0));
    }

    @Test
    public void testLifecycleBuilder_ReturnsCorrectData_WhenInstallEvent_TwoDigitMonthAndDay() {
        // Sunday, November 10, 2017 7:32:48 AM
        Map<String, String> installData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, lifecycleDataStore, 1510327968L)
                        .addInstallData()
                        .build();

        // Check data equals expected
        assertEquals("11/10/2017", installData.get(CONTEXT_DATA_KEY_INSTALL_DATE));
        assertEquals(
                CONTEXT_DATA_VALUE_INSTALL_EVENT, installData.get(CONTEXT_DATA_KEY_EVENT_INSTALL));

        // Verify we increase persisted launch number
        assertEquals(0, lifecycleDataStore.getInt("LaunchNumber", 0));
    }

    @Test
    public void
            testLifecycleBuilder_ReturnsCorrectData_WhenInstallEvent_WhenLifecycleDataStoreIsNull() {
        // Sunday, November 10, 2017 7:32:48 AM
        Map<String, String> installData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, null, 1510327968L)
                        .addInstallData()
                        .build();

        // Check data equals expected - no of lunches shouldn't be there
        assertEquals(4, installData.size());
        assertEquals(
                CONTEXT_DATA_VALUE_DAILY_ENG_USER_EVENT,
                installData.get(CONTEXT_DATA_KEY_EVENT_DAILY_ENGAGED));
        assertEquals(
                CONTEXT_DATA_VALUE_MONTHLY_ENG_USER_EVENT,
                installData.get(CONTEXT_DATA_KEY_EVENT_MONTHLY_ENGAGED));
        assertEquals(
                CONTEXT_DATA_VALUE_INSTALL_EVENT, installData.get(CONTEXT_DATA_KEY_EVENT_INSTALL));
        assertEquals("11/10/2017", installData.get(CONTEXT_DATA_KEY_INSTALL_DATE));
    }

    @Test
    public void testLifecycleBuilder_ReturnsCorrectData_WhenLaunchEvent() {
        // Saturday December 31, 2016 23:32:48 (pm) GMT-08:00
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_INSTALL_DATE), anyLong()))
                .thenReturn(1483255968L);
        // Saturday January 07, 2017 23:32:48 (pm) GMT-08:00
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_LAST_USED_DATE), anyLong()))
                .thenReturn(1483860768L);
        // Sunday January 08, 2017 00:32:48 (am) GMT-08:00
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_PAUSE_DATE), anyLong()))
                .thenReturn(1483864368L);

        // Saturday January 07, 2017 23:37:48 (pm)  GMT-08:00
        Map<String, String> launchData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, lifecycleDataStore, 1483861068L)
                        .addLaunchData()
                        .build();

        // Check data equals expected
        assertEquals(2, launchData.size());
        assertEquals("7", launchData.get(CONTEXT_DATA_KEY_DAYS_SINCE_FIRST_LAUNCH));
        assertEquals("0", launchData.get(CONTEXT_DATA_KEY_DAYS_SINCE_LAST_LAUNCH));
    }

    @Test
    public void testLifecycleBuilder_ReturnsCorrectData_WhenLaunchEvent_LaunchNewDay() {
        // Saturday December 31, 2016 23:32:48 (pm) GMT-08:00
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_INSTALL_DATE), anyLong()))
                .thenReturn(1483255968L);
        // Saturday January 07, 2017 23:32:48 (pm) GMT-08:00
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_LAST_USED_DATE), anyLong()))
                .thenReturn(1483860768L);
        // Sunday January 08, 2017 00:32:48 (am) GMT-08:00
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_PAUSE_DATE), anyLong()))
                .thenReturn(1483864368L);

        // Monday January 09, 2017 04:32:48 (am) GMT-08:00
        Map<String, String> launchData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, lifecycleDataStore, 1483965168L)
                        .addLaunchData()
                        .build();

        // Check data equals expected
        assertEquals(3, launchData.size());
        assertEquals(
                CONTEXT_DATA_VALUE_DAILY_ENG_USER_EVENT,
                launchData.get(CONTEXT_DATA_KEY_EVENT_DAILY_ENGAGED));
        assertEquals("9", launchData.get(CONTEXT_DATA_KEY_DAYS_SINCE_FIRST_LAUNCH));
        assertEquals("2", launchData.get(CONTEXT_DATA_KEY_DAYS_SINCE_LAST_LAUNCH));
    }

    @Test
    public void testLifecycleBuilder_NoDaysSinceLastLaunch_PriorUpgradeYearIsInFuture() {
        // Monday, March 3, 2031 11:15:31 PM
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_INSTALL_DATE), anyLong()))
                .thenReturn(1930346131L);
        // Monday, March 3, 2031 11:15:31 PM
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_LAST_USED_DATE), anyLong()))
                .thenReturn(1930346131L);
        // Sunday January 08, 2017 00:32:48 (am) GMT-08:00

        // Tuesday, January 12, 2016 8:32:48 PM
        Map<String, String> upgradeData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, lifecycleDataStore, 1452630768L)
                        .addUpgradeData(false)
                        .build();

        assertNull(upgradeData.get(CONTEXT_DATA_KEY_DAYS_SINCE_LAST_LAUNCH));
        assertNull(upgradeData.get(CONTEXT_DATA_KEY_DAYS_SINCE_FIRST_LAUNCH));
    }

    @Test
    public void testLifecycleBuilder_NoDaysSinceLastLaunch_PriorUpgradeYearIsInvalid() {
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_INSTALL_DATE), anyLong()))
                .thenReturn(400L);
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_LAST_USED_DATE), anyLong()))
                .thenReturn(400L);

        // Tuesday, January 12, 2016 8:32:48 PM
        Map<String, String> upgradeData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, lifecycleDataStore, 1452630768L)
                        .addUpgradeData(false)
                        .build();

        assertNull(upgradeData.get(CONTEXT_DATA_KEY_DAYS_SINCE_LAST_LAUNCH));
        assertNull(upgradeData.get(CONTEXT_DATA_KEY_DAYS_SINCE_FIRST_LAUNCH));
    }

    @Test
    public void
            testLifecycleBuilder_ReturnsCorrectData_WhenLaunchEvent_LaunchEndOfDayToBeginningOfDay() {
        // Sunday, January 1, 2017 12:32:48 AM GMT-08:00
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_INSTALL_DATE), anyLong()))
                .thenReturn(1483259568L);
        // Sunday, January 8, 2017 11:55:55 PM GMT-08:00
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_LAST_USED_DATE), anyLong()))
                .thenReturn(1483948555L);
        // Monday, January 9, 2017 12:00:01 AM GMT-08:00
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_PAUSE_DATE), anyLong()))
                .thenReturn(1483948801L);
        when(lifecycleDataStore.getInt(eq(DATASTORE_KEY_LAUNCHES), anyInt())).thenReturn(10);

        // Monday, January 9, 2017 12:00:55 AM GMT-08:00
        Map<String, String> launchData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, lifecycleDataStore, 1483948855L)
                        .addLaunchData()
                        .build();

        // Check data equals expected
        assertEquals(3, launchData.size());
        assertEquals(
                CONTEXT_DATA_VALUE_DAILY_ENG_USER_EVENT,
                launchData.get(CONTEXT_DATA_KEY_EVENT_DAILY_ENGAGED));
        assertEquals("8", launchData.get(CONTEXT_DATA_KEY_DAYS_SINCE_FIRST_LAUNCH));
        assertEquals("1", launchData.get(CONTEXT_DATA_KEY_DAYS_SINCE_LAST_LAUNCH));
    }

    @Test
    public void testLifecycleBuilder_ReturnsCorrectData_WhenLaunchEvent_LaunchNewMonth() {
        // Saturday December 31, 2016 23:32:48 (pm) GMT-08:00
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_INSTALL_DATE), anyLong()))
                .thenReturn(1483255968L);
        // Saturday January 07, 2017 23:32:48 (pm) GMT-08:00
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_LAST_USED_DATE), anyLong()))
                .thenReturn(1483860768L);
        // Sunday January 08, 2017 00:32:48 (am) GMT-08:00
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_PAUSE_DATE), anyLong()))
                .thenReturn(1483864368L);
        when(lifecycleDataStore.getInt(eq(DATASTORE_KEY_LAUNCHES), anyInt())).thenReturn(1);

        // Tuesday February 07, 2017 23:32:48 (pm) GMT-08:00
        Map<String, String> launchData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, lifecycleDataStore, 1486539168L)
                        .addLaunchData()
                        .build();

        // Verify
        assertEquals(4, launchData.size());
        assertEquals(
                CONTEXT_DATA_VALUE_DAILY_ENG_USER_EVENT,
                launchData.get(CONTEXT_DATA_KEY_EVENT_DAILY_ENGAGED));
        assertEquals(
                CONTEXT_DATA_VALUE_MONTHLY_ENG_USER_EVENT,
                launchData.get(CONTEXT_DATA_KEY_EVENT_MONTHLY_ENGAGED));
        assertEquals("38", launchData.get(CONTEXT_DATA_KEY_DAYS_SINCE_FIRST_LAUNCH));
        assertEquals("31", launchData.get(CONTEXT_DATA_KEY_DAYS_SINCE_LAST_LAUNCH));
    }

    @Test
    public void
            testLifecycleBuilder_ReturnsCorrectData_WhenLaunchEvent_LaunchEndOfMonthToBeginningOfMonth() {
        // Sunday, January 1, 2017 12:32:48 AM GMT-08:00
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_INSTALL_DATE), anyLong()))
                .thenReturn(1483259568L);
        // Tuesday, January 31, 2017 11:32:48 PM GMT-08:00
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_LAST_USED_DATE), anyLong()))
                .thenReturn(1485934368L);
        // Tuesday, January 31, 2017 11:50:48 PM GMT-08:00
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_PAUSE_DATE), anyLong()))
                .thenReturn(1485935448L);

        // Wednesday, February 1, 2017 12:32:48 AM GMT-08:00
        Map<String, String> launchData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, lifecycleDataStore, 1485937968L)
                        .addLaunchData()
                        .build();

        // Verify
        assertEquals(4, launchData.size());
        assertEquals(
                CONTEXT_DATA_VALUE_DAILY_ENG_USER_EVENT,
                launchData.get(CONTEXT_DATA_KEY_EVENT_DAILY_ENGAGED));
        assertEquals(
                CONTEXT_DATA_VALUE_MONTHLY_ENG_USER_EVENT,
                launchData.get(CONTEXT_DATA_KEY_EVENT_MONTHLY_ENGAGED));
        assertEquals("31", launchData.get(CONTEXT_DATA_KEY_DAYS_SINCE_FIRST_LAUNCH));
        assertEquals("1", launchData.get(CONTEXT_DATA_KEY_DAYS_SINCE_LAST_LAUNCH));
    }

    @Test
    public void
            testLifecycleBuilder_ReturnsCorrectData_WhenLaunchEvent_LaunchFirstOfMonthToFirstOfMonth() {
        // Sunday, January 1, 2017 12:32:48 AM
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_INSTALL_DATE), anyLong()))
                .thenReturn(1483302768L);
        // Sunday, October 1, 2017 1:01:00 AM
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_LAST_USED_DATE), anyLong()))
                .thenReturn(1506844860L);
        // Sunday, October 1, 2017 1:10:00 AM
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_PAUSE_DATE), anyLong()))
                .thenReturn(1506845400L);

        // Wednesday, November 1, 2017 1:01:00 AM GMT-08:00
        Map<String, String> launchData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, lifecycleDataStore, 1509523260L)
                        .addLaunchData()
                        .build();

        // Verify
        assertEquals(4, launchData.size());
        assertEquals(
                CONTEXT_DATA_VALUE_DAILY_ENG_USER_EVENT,
                launchData.get(CONTEXT_DATA_KEY_EVENT_DAILY_ENGAGED));
        assertEquals(
                CONTEXT_DATA_VALUE_MONTHLY_ENG_USER_EVENT,
                launchData.get(CONTEXT_DATA_KEY_EVENT_MONTHLY_ENGAGED));
        assertEquals("304", launchData.get(CONTEXT_DATA_KEY_DAYS_SINCE_FIRST_LAUNCH));
        assertEquals("31", launchData.get(CONTEXT_DATA_KEY_DAYS_SINCE_LAST_LAUNCH));
    }

    @Test
    public void testLifecycleBuilder_ReturnsCorrectData_WhenLaunchEvent_LaunchSameDayNewYear() {
        // Saturday October 01, 2016 01:01:00 (am) GTM-8
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_INSTALL_DATE), anyLong()))
                .thenReturn(1475308860L);
        // Saturday October 01, 2016 01:01:00 (am) GTM-8
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_LAST_USED_DATE), anyLong()))
                .thenReturn(1475308800L);
        // Saturday October 01, 2016 01:10:00 (am) GTM-8
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_PAUSE_DATE), anyLong()))
                .thenReturn(1475309400L);
        when(lifecycleDataStore.getInt(eq(DATASTORE_KEY_LAUNCHES), anyInt())).thenReturn(1);

        // Sunday October 01, 2017 01:10:00 (am) GMT-08:00
        Map<String, String> launchData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, lifecycleDataStore, 1506845400L)
                        .addLaunchData()
                        .build();

        // Verify
        assertEquals(4, launchData.size());
        assertEquals(
                CONTEXT_DATA_VALUE_DAILY_ENG_USER_EVENT,
                launchData.get(CONTEXT_DATA_KEY_EVENT_DAILY_ENGAGED));
        assertEquals(
                CONTEXT_DATA_VALUE_MONTHLY_ENG_USER_EVENT,
                launchData.get(CONTEXT_DATA_KEY_EVENT_MONTHLY_ENGAGED));
        assertEquals("365", launchData.get(CONTEXT_DATA_KEY_DAYS_SINCE_FIRST_LAUNCH));
        assertEquals("365", launchData.get(CONTEXT_DATA_KEY_DAYS_SINCE_LAST_LAUNCH));
    }

    @Test
    public void testLifecycleBuilder_ReturnsEmptyMap_WhenLaunchEvent_WhenLifecycleDataStoreNull() {
        // Sunday, October 1, 2017 1:10:00 AM GMT-07:00
        Map<String, String> launchData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, null, 1506845400L)
                        .addLaunchData()
                        .build();

        // Verify
        assertTrue(launchData.isEmpty());
    }

    @Test
    public void
            testLifecycleBuilder_ReturnsEmptyMap_WhenLaunchEvent_AndSystemInfoNull_AndDataStoreNull() {
        // Sunday, October 1, 2017 1:10:00 AM GMT-07:00
        Map<String, String> launchData =
                new LifecycleMetricsBuilder(null, null, 1506845400L).addLaunchData().build();

        // Verify
        assertTrue(launchData.isEmpty());
    }

    @Test
    public void testLifecycleBuilder_ReturnsCorrectData_WhenUpgradeEvent() {
        when(lifecycleDataStore.getInt(eq(DATASTORE_KEY_LAUNCHES_AFTER_UPGRADE), anyInt()))
                .thenReturn(5);

        Map<String, String> upgradeData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, lifecycleDataStore, 1452601968L)
                        .addUpgradeData(true)
                        .build();

        assertEquals(1, upgradeData.size());
        assertEquals(
                CONTEXT_DATA_VALUE_UPGRADE_EVENT, upgradeData.get(CONTEXT_DATA_KEY_EVENT_UPGRADE));

        // Verify we persist new upgrade date
        ArgumentCaptor<Long> upgradeDateCaptor = ArgumentCaptor.forClass(Long.class);
        verify(lifecycleDataStore, times(1))
                .setLong(eq(DATASTORE_KEY_UPGRADE_DATE), upgradeDateCaptor.capture());
        assertEquals(1452601968L, upgradeDateCaptor.getValue().longValue());

        // Verify we reset persisted launches after upgrade
        ArgumentCaptor<Integer> launchesAfterUpgradeCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(lifecycleDataStore, times(1))
                .setInt(
                        eq(DATASTORE_KEY_LAUNCHES_AFTER_UPGRADE),
                        launchesAfterUpgradeCaptor.capture());
        assertEquals(0, launchesAfterUpgradeCaptor.getValue().intValue());
    }

    @Test
    public void testLifecycleBuilder_ReturnsEmptyMap_WhenUpgradeEvent_NoUpgrade() {
        Map<String, String> upgradeData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, lifecycleDataStore, 1452601968L)
                        .addUpgradeData(false)
                        .build();

        assertTrue(upgradeData.isEmpty());
    }

    @Test
    public void testLifecycleBuilder_ReturnsCorrectData_WhenUpgradeEvent_PriorUpgradeYear() {
        // Monday, January 12, 2015 7:32:48 PM
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_UPGRADE_DATE), anyLong()))
                .thenReturn(1421091168L);
        when(lifecycleDataStore.getString(eq(DATASTORE_KEY_LAST_VERSION), anyString()))
                .thenReturn("1.1.1");
        when(lifecycleDataStore.getInt(eq(DATASTORE_KEY_LAUNCHES_AFTER_UPGRADE), anyInt()))
                .thenReturn(30);

        // Tuesday, January 12, 2016 8:32:48 PM
        Map<String, String> upgradeData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, lifecycleDataStore, 1452630768L)
                        .addUpgradeData(false)
                        .build();

        assertEquals("365", upgradeData.get(CONTEXT_DATA_KEY_DAYS_SINCE_LAST_UPGRADE));
        assertEquals("31", upgradeData.get(CONTEXT_DATA_KEY_LAUNCHES_SINCE_UPGRADE));
        // Verify we increase persisted launches after upgrade
        ArgumentCaptor<Integer> launchesAfterUpgradeCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(lifecycleDataStore, times(1))
                .setInt(
                        eq(DATASTORE_KEY_LAUNCHES_AFTER_UPGRADE),
                        launchesAfterUpgradeCaptor.capture());
        assertEquals(31, launchesAfterUpgradeCaptor.getValue().intValue());
    }

    @Test
    public void testLifecycleBuilder_NoDaysSinceLastUpgrade_PriorUpgradeYearIsInFuture() {
        // Monday, March 3, 2031 11:15:31 PM
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_UPGRADE_DATE), anyLong()))
                .thenReturn(1930346131L);

        // Tuesday, January 12, 2016 8:32:48 PM
        Map<String, String> upgradeData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, lifecycleDataStore, 1452630768L)
                        .addUpgradeData(false)
                        .build();

        assertNull(upgradeData.get(CONTEXT_DATA_KEY_DAYS_SINCE_LAST_UPGRADE));
    }

    @Test
    public void testLifecycleBuilder_NoDaysSinceLastUpgrade_PriorUpgradeYearIsInvalid() {
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_UPGRADE_DATE), anyLong()))
                .thenReturn(400L);

        // Tuesday, January 12, 2016 8:32:48 PM
        Map<String, String> upgradeData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, lifecycleDataStore, 1452630768L)
                        .addUpgradeData(false)
                        .build();

        assertNull(upgradeData.get(CONTEXT_DATA_KEY_DAYS_SINCE_LAST_UPGRADE));
    }

    @Test
    public void testLifecycleBuilder_ReturnsCorrectData_WhenUpgradeEvent_PriorUpgradeLeapYear() {
        // Tuesday, January 12, 2016 8:32:48 PM
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_UPGRADE_DATE), anyLong()))
                .thenReturn(1452630768L);
        when(lifecycleDataStore.getString(eq(DATASTORE_KEY_LAST_VERSION), anyString()))
                .thenReturn("1.1.1");
        when(lifecycleDataStore.getInt(eq(DATASTORE_KEY_LAUNCHES_AFTER_UPGRADE), anyInt()))
                .thenReturn(0);

        // Thursday, January 12, 2017 8:32:48 PM
        Map<String, String> upgradeData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, lifecycleDataStore, 1484253168L)
                        .addUpgradeData(false)
                        .build();

        assertEquals("366", upgradeData.get(CONTEXT_DATA_KEY_DAYS_SINCE_LAST_UPGRADE));
        assertEquals("1", upgradeData.get(CONTEXT_DATA_KEY_LAUNCHES_SINCE_UPGRADE));
        // Verify we increase persisted launches after upgrade
        ArgumentCaptor<Integer> launchesAfterUpgradeCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(lifecycleDataStore, times(1))
                .setInt(
                        eq(DATASTORE_KEY_LAUNCHES_AFTER_UPGRADE),
                        launchesAfterUpgradeCaptor.capture());
        assertEquals(1, launchesAfterUpgradeCaptor.getValue().intValue());
    }

    @Test
    public void
            testLifecycleBuilder_ReturnsCorrectData_WhenUpgradeEvent_NoUpgradeWhenDataStoreNull() {
        Map<String, String> upgradeData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, null, 1452601968L)
                        .addUpgradeData(false)
                        .build();

        assertTrue(upgradeData.isEmpty());
    }

    @Test
    public void
            testLifecycleBuilder_ReturnsCorrectData_WhenUpgradeEvent_WhenLifecycleDataStoreIsNull() {
        // Sunday, November 10, 2017 7:32:48 AM
        Map<String, String> upgradeData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, null, 1510327968L)
                        .addUpgradeData(true)
                        .build();

        // Check data equals expected
        assertEquals(1, upgradeData.size());
        assertEquals(
                CONTEXT_DATA_VALUE_UPGRADE_EVENT, upgradeData.get(CONTEXT_DATA_KEY_EVENT_UPGRADE));
    }

    @Test
    public void testLifecycleBuilder_ReturnsCorrectData_WhenCrashEvent() {
        Map<String, String> crashData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, lifecycleDataStore, 1452601968L)
                        .addCrashData(true)
                        .build();

        assertEquals(1, crashData.size());
        assertEquals(CONTEXT_DATA_VALUE_CRASH_EVENT, crashData.get(CONTEXT_DATA_KEY_CRASH_EVENT));
    }

    @Test
    public void testLifecycleBuilder_ReturnsCorrectData_WhenNoCrashEvent() {
        Map<String, String> crashData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, lifecycleDataStore, 1452601968L)
                        .addCrashData(false)
                        .build();

        assertEquals(0, crashData.size());
    }

    @Test
    public void testLifecycleBuilder_ReturnsCorrectData_WhenCoreEvent() {
        Map<String, String> coreData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, lifecycleDataStore, 1452601968L)
                        .addCoreData()
                        .build();

        assertEquals("deviceName", coreData.get(DEVICE_NAME));
        assertEquals("TEST_CARRIER", coreData.get(CARRIER_NAME));
        assertEquals("TEST_APPLICATION_NAME 1.1 (12345)", coreData.get(APPLICATION_IDENTIFIER));
        assertEquals("TEST_OS 5.55", coreData.get(OPERATING_SYSTEM));
        assertEquals("100x100", coreData.get(DEVICE_RESOLUTION));
        assertEquals("APPLICATION", coreData.get(RUN_MODE));
    }

    @Test
    public void testLifecycleBuilder_ReturnsCorrectData_WhenCoreEvent_EmptyOperatingSystemName() {
        when(mockDeviceInfoService.getOperatingSystemName()).thenReturn("");

        Map<String, String> coreData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, lifecycleDataStore, 1452601968L)
                        .addCoreData()
                        .build();

        assertEquals("deviceName", coreData.get(DEVICE_NAME));
        assertEquals("TEST_CARRIER", coreData.get(CARRIER_NAME));
        assertEquals("TEST_APPLICATION_NAME 1.1 (12345)", coreData.get(APPLICATION_IDENTIFIER));
        assertEquals(" 5.55", coreData.get(OPERATING_SYSTEM));
        assertEquals("100x100", coreData.get(DEVICE_RESOLUTION));
        assertEquals("APPLICATION", coreData.get(RUN_MODE));
    }

    @Test
    public void
            testLifecycleBuilder_ReturnsCorrectData_WhenCoreEvent_EmptyOperatingSystemVersion() {
        when(mockDeviceInfoService.getOperatingSystemVersion()).thenReturn("");

        Map<String, String> coreData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, lifecycleDataStore, 1452601968L)
                        .addCoreData()
                        .build();

        assertEquals("deviceName", coreData.get(DEVICE_NAME));
        assertEquals("TEST_CARRIER", coreData.get(CARRIER_NAME));
        assertEquals("TEST_APPLICATION_NAME 1.1 (12345)", coreData.get(APPLICATION_IDENTIFIER));
        assertEquals("TEST_OS ", coreData.get(OPERATING_SYSTEM));
        assertEquals("100x100", coreData.get(DEVICE_RESOLUTION));
        assertEquals("APPLICATION", coreData.get(RUN_MODE));
    }

    @Test
    public void testLifecycleBuilder_ReturnsCorrectData_WhenCoreEvent_EmptyApplicationName() {
        when(mockDeviceInfoService.getApplicationName()).thenReturn("");

        Map<String, String> coreData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, lifecycleDataStore, 1452601968L)
                        .addCoreData()
                        .build();

        assertEquals("deviceName", coreData.get(DEVICE_NAME));
        assertEquals("TEST_CARRIER", coreData.get(CARRIER_NAME));
        assertEquals(" 1.1 (12345)", coreData.get(APPLICATION_IDENTIFIER));
        assertEquals("TEST_OS 5.55", coreData.get(OPERATING_SYSTEM));
        assertEquals("100x100", coreData.get(DEVICE_RESOLUTION));
        assertEquals("APPLICATION", coreData.get(RUN_MODE));
    }

    @Test
    public void testLifecycleBuilder_ReturnsCorrectData_WhenCoreEvent_EmptyApplicationVersion() {
        when(mockDeviceInfoService.getApplicationVersion()).thenReturn("");

        Map<String, String> coreData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, lifecycleDataStore, 1452601968L)
                        .addCoreData()
                        .build();

        assertEquals("deviceName", coreData.get(DEVICE_NAME));
        assertEquals("TEST_CARRIER", coreData.get(CARRIER_NAME));
        assertEquals("TEST_APPLICATION_NAME (12345)", coreData.get(APPLICATION_IDENTIFIER));
        assertEquals("TEST_OS 5.55", coreData.get(OPERATING_SYSTEM));
        assertEquals("100x100", coreData.get(DEVICE_RESOLUTION));
        assertEquals("APPLICATION", coreData.get(RUN_MODE));
    }

    @Test
    public void
            testLifecycleBuilder_ReturnsCorrectData_WhenCoreEvent_EmptyApplicationVersionCode() {
        when(mockDeviceInfoService.getApplicationVersionCode()).thenReturn("");

        Map<String, String> coreData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, lifecycleDataStore, 1452601968L)
                        .addCoreData()
                        .build();

        assertEquals("deviceName", coreData.get(DEVICE_NAME));
        assertEquals("TEST_CARRIER", coreData.get(CARRIER_NAME));
        assertEquals("TEST_APPLICATION_NAME 1.1", coreData.get(APPLICATION_IDENTIFIER));
        assertEquals("TEST_OS 5.55", coreData.get(OPERATING_SYSTEM));
        assertEquals("100x100", coreData.get(DEVICE_RESOLUTION));
        assertEquals("APPLICATION", coreData.get(RUN_MODE));
    }

    @Test
    public void testLifecycleBuilder_ReturnsCorrectData_WhenCoreEvent_EmptyCarrierName() {
        when(mockDeviceInfoService.getMobileCarrierName()).thenReturn("");

        Map<String, String> coreData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, lifecycleDataStore, 1452601968L)
                        .addCoreData()
                        .build();

        assertEquals("deviceName", coreData.get(DEVICE_NAME));
        assertEquals("TEST_APPLICATION_NAME 1.1 (12345)", coreData.get(APPLICATION_IDENTIFIER));
        assertEquals("TEST_OS 5.55", coreData.get(OPERATING_SYSTEM));
        assertEquals("100x100", coreData.get(DEVICE_RESOLUTION));
        assertEquals("APPLICATION", coreData.get(RUN_MODE));
        assertFalse(coreData.containsKey(CARRIER_NAME));
    }

    @Test
    public void testLifecycleBuilder_ReturnsCorrectData_WhenCoreEvent_EmptyDeviceName() {
        when(mockDeviceInfoService.getDeviceName()).thenReturn("");

        Map<String, String> coreData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, lifecycleDataStore, 1452601968L)
                        .addCoreData()
                        .build();

        assertEquals("TEST_CARRIER", coreData.get(CARRIER_NAME));
        assertEquals("TEST_APPLICATION_NAME 1.1 (12345)", coreData.get(APPLICATION_IDENTIFIER));
        assertEquals("TEST_OS 5.55", coreData.get(OPERATING_SYSTEM));
        assertEquals("100x100", coreData.get(DEVICE_RESOLUTION));
        assertEquals("APPLICATION", coreData.get(RUN_MODE));
        assertFalse(coreData.containsKey(DEVICE_NAME));
    }

    @Test
    public void testLifecycleBuilder_ReturnsCorrectData_WhenCoreEvent_EmptyDisplayInformation() {
        when(mockDeviceInfoService.getDisplayInformation()).thenReturn(null);

        Map<String, String> coreData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, lifecycleDataStore, 1452601968L)
                        .addCoreData()
                        .build();

        assertEquals("deviceName", coreData.get(DEVICE_NAME));
        assertEquals("TEST_CARRIER", coreData.get(CARRIER_NAME));
        assertEquals("TEST_APPLICATION_NAME 1.1 (12345)", coreData.get(APPLICATION_IDENTIFIER));
        assertEquals("TEST_OS 5.55", coreData.get(OPERATING_SYSTEM));
        assertEquals("APPLICATION", coreData.get(RUN_MODE));
        assertFalse(coreData.containsKey(DEVICE_RESOLUTION));
    }

    @Test
    public void testLifecycleBuilder_ReturnsCorrectData_WhenCoreEvent_DataStoreNull() {
        Map<String, String> coreData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, null, 1452601968L)
                        .addCoreData()
                        .build();

        assertEquals("deviceName", coreData.get(DEVICE_NAME));
        assertEquals("TEST_CARRIER", coreData.get(CARRIER_NAME));
        assertEquals("TEST_APPLICATION_NAME 1.1 (12345)", coreData.get(APPLICATION_IDENTIFIER));
        assertEquals("TEST_OS 5.55", coreData.get(OPERATING_SYSTEM));
        assertEquals("100x100", coreData.get(DEVICE_RESOLUTION));
        assertEquals("APPLICATION", coreData.get(RUN_MODE));
    }

    @Test
    public void testLifecycleBuilder_ReturnsEmptyMap_WhenCoreEvent_SystemInfoNull() {
        Map<String, String> coreData =
                new LifecycleMetricsBuilder(null, null, 1452601968L).addCoreData().build();

        assertTrue(coreData.isEmpty());
    }

    @Test
    public void testLifecycleBuilder_ReturnsCorrectData_WhenAddGeneralDataCalled_PM() {
        when(lifecycleDataStore.getInt(eq(DATASTORE_KEY_LAUNCHES), anyInt())).thenReturn(4);

        // Saturday January 07, 2017 23:37:48 (pm)  GMT-08:00
        Map<String, String> generalData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, lifecycleDataStore, 1483861068L)
                        .addGenericData()
                        .build();

        // Check data equals expected
        assertEquals(4, generalData.size());
        assertEquals(
                CONTEXT_DATA_VALUE_LAUNCH_EVENT,
                generalData.get(CONTEXT_DATA_KEY_LAUNCH_EVENT_KEY));
        assertEquals("4", generalData.get(CONTEXT_DATA_KEY_LAUNCHES));
        assertEquals("23", generalData.get(CONTEXT_DATA_KEY_HOUR_OF_DAY));
        assertEquals("7", generalData.get(CONTEXT_DATA_KEY_DAY_OF_WEEK));
    }

    @Test
    public void testLifecycleBuilder_ReturnsCorrectData_WhenAddGeneralDataCalled_AM() {
        when(lifecycleDataStore.getInt(eq(DATASTORE_KEY_LAUNCHES), anyInt())).thenReturn(1);

        // Monday, January 9, 2017 12:00:55 AM GMT-08:00
        Map<String, String> generalData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, lifecycleDataStore, 1483948855L)
                        .addGenericData()
                        .build();

        // Check data equals expected
        assertEquals(4, generalData.size());
        assertEquals(
                CONTEXT_DATA_VALUE_LAUNCH_EVENT,
                generalData.get(CONTEXT_DATA_KEY_LAUNCH_EVENT_KEY));
        assertEquals("1", generalData.get(CONTEXT_DATA_KEY_LAUNCHES));
        assertEquals("0", generalData.get(CONTEXT_DATA_KEY_HOUR_OF_DAY));
        assertEquals("2", generalData.get(CONTEXT_DATA_KEY_DAY_OF_WEEK));
    }

    @Test
    public void testLifecycleBuilder_ReturnsCorrectData_WhenAddGeneralDataCalled_LaunchesNotSet() {
        when(lifecycleDataStore.getInt(eq(DATASTORE_KEY_LAUNCHES), anyInt())).thenReturn(-1);

        // Monday, January 9, 2017 12:00:55 AM GMT-08:00
        Map<String, String> generalData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, lifecycleDataStore, 1483948855L)
                        .addGenericData()
                        .build();

        // Check data equals expected - no of lunches shouldn't be there
        assertEquals(3, generalData.size());
        assertEquals(
                CONTEXT_DATA_VALUE_LAUNCH_EVENT,
                generalData.get(CONTEXT_DATA_KEY_LAUNCH_EVENT_KEY));
        assertEquals("0", generalData.get(CONTEXT_DATA_KEY_HOUR_OF_DAY));
        assertEquals("2", generalData.get(CONTEXT_DATA_KEY_DAY_OF_WEEK));
    }

    @Test
    public void testLifecycleBuilder_ReturnsCorrectData_WhenLifecycleDataStoreIsNull() {
        // Saturday January 07, 2017 23:37:48 (pm)  GMT-08:00
        Map<String, String> generalData =
                new LifecycleMetricsBuilder(mockDeviceInfoService, null, 1483861068L)
                        .addGenericData()
                        .build();

        // Check data equals expected - no of lunches shouldn't be there
        assertEquals(3, generalData.size());
        assertEquals(
                CONTEXT_DATA_VALUE_LAUNCH_EVENT,
                generalData.get(CONTEXT_DATA_KEY_LAUNCH_EVENT_KEY));
        assertEquals("23", generalData.get(CONTEXT_DATA_KEY_HOUR_OF_DAY));
        assertEquals("7", generalData.get(CONTEXT_DATA_KEY_DAY_OF_WEEK));
    }
}
