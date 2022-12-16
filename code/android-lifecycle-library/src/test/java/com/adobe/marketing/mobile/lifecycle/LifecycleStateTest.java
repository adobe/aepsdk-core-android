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

import static com.adobe.marketing.mobile.LifecycleEventGeneratorTestHelper.createPauseEvent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.services.DeviceInforming;
import com.adobe.marketing.mobile.services.NamedCollection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class LifecycleStateTest {

    @Mock NamedCollection lifecycleDataStore;

    @Mock DeviceInforming deviceInfoService;

    private long currentTimestampInMilliSeconds;
    private long currentTimestampInSeconds;
    private long timestampOneSecEarlierInSeconds;
    private long timestampTenMinEarlierInSeconds;
    private long timestampOneHourEarlierInSeconds;
    private long timestampOneDayEarlierSeconds;
    private static final long DEFAULT_LIFECYCLE_TIMEOUT = 200L;
    private String dayOfWeek;
    private String hourOfDay;
    private final String mockAppVersion = "1.1";
    private LifecycleState lifecycleState;

    private static final String DATASTORE_KEY_LIFECYCLE_DATA = "LifecycleData";
    private static final String DATASTORE_KEY_START_DATE = "SessionStart";
    private static final String DATASTORE_KEY_INSTALL_DATE = "InstallDate";
    private static final String DATASTORE_KEY_LAST_USED_DATE = "LastDateUsed";
    private static final String DATASTORE_KEY_LAUNCHES = "Launches";
    private static final String DATASTORE_KEY_LAST_VERSION = "LastVersion";
    private static final String DATASTORE_KEY_PAUSE_DATE = "PauseDate";
    private static final String DATASTORE_KEY_SUCCESSFUL_CLOSE = "SuccessfulClose";
    private static final String DATASTORE_KEY_OS_VERSION = "OsVersion";
    private static final String DATASTORE_KEY_APP_ID = "AppId";

    private static final String CONTEXT_DATA_KEY_EVENT_LAUNCH = "launchevent";
    private static final String CONTEXT_DATA_KEY_CRASH_EVENT = "crashevent";
    private static final String CONTEXT_DATA_KEY_LAUNCHES = "launches";
    private static final String CONTEXT_DATA_KEY_DAYS_SINCE_FIRST_LAUNCH = "dayssincefirstuse";
    private static final String CONTEXT_DATA_KEY_DAYS_SINCE_LAST_LAUNCH = "dayssincelastuse";
    private static final String CONTEXT_DATA_KEY_HOUR_OF_DAY = "hourofday";
    private static final String CONTEXT_DATA_KEY_DAY_OF_WEEK = "dayofweek";
    private static final String CONTEXT_DATA_KEY_OPERATING_SYSTEM = "osversion";
    private static final String CONTEXT_DATA_KEY_APPLICATION_IDENTIFIER = "appid";
    private static final String CONTEXT_DATA_KEY_DEVICE_NAME = "devicename";
    private static final String CONTEXT_DATA_KEY_DEVICE_RESOLUTION = "resolution";
    private static final String CONTEXT_DATA_KEY_CARRIER_NAME = "carriername";
    private static final String CONTEXT_DATA_KEY_LOCALE = "locale";
    private static final String CONTEXT_DATA_KEY_RUN_MODE = "runmode";
    private static final String CONTEXT_DATA_KEY_PREVIOUS_SESSION_LENGTH = "prevsessionlength";
    private static final String CONTEXT_DATA_KEY_PREVIOUS_OS_VERSION = "previousosversion";
    private static final String CONTEXT_DATA_KEY_PREVIOUS_APP_ID = "previousappid";
    private static final String CONTEXT_DATA_KEY_INSTALL_EVENT = "installevent";
    private static final String CONTEXT_DATA_KEY_DAILY_ENGAGED_EVENT = "dailyenguserevent";
    private static final String CONTEXT_DATA_KEY_MONTHLY_ENGAGED_EVENT = "monthlyenguserevent";
    private static final String CONTEXT_DATA_KEY_UPGRADE_EVENT = "upgradeevent";
    private static final String CONTEXT_DATA_KEY_IGNORED_SESSION_LENGTH = "ignoredsessionlength";

    private static final String CONTEXT_DATA_VALUE_LAUNCH_EVENT = "LaunchEvent";
    private static final String CONTEXT_DATA_VALUE_CRASH_EVENT = "CrashEvent";
    private static final String CONTEXT_DATA_VALUE_INSTALL_EVENT = "InstallEvent";

    private static final String IDENTITY_KEY_ADVERTISING_IDENTIFIER = "advertisingidentifier";

    @Before
    public void beforeEach() {
        LifecycleTestHelper.initDeviceInfoService(deviceInfoService);
        initTimestamps();

        lifecycleState = new LifecycleState(lifecycleDataStore, deviceInfoService);
    }

    private void initTimestamps() {
        currentTimestampInMilliSeconds = System.currentTimeMillis();
        currentTimestampInSeconds = TimeUnit.MILLISECONDS.toSeconds(currentTimestampInMilliSeconds);
        timestampOneSecEarlierInSeconds = currentTimestampInSeconds - 1;
        timestampTenMinEarlierInSeconds =
                currentTimestampInSeconds - TimeUnit.MINUTES.toSeconds(10);
        timestampOneHourEarlierInSeconds = currentTimestampInSeconds - TimeUnit.HOURS.toSeconds(1);
        timestampOneDayEarlierSeconds = currentTimestampInSeconds - TimeUnit.DAYS.toSeconds(1);
        final Date currentDate = new Date(currentTimestampInMilliSeconds);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currentTimestampInMilliSeconds);
        int dayOfWeekNumber = cal.get(Calendar.DAY_OF_WEEK);
        dayOfWeek = Integer.toString(dayOfWeekNumber);

        DateFormat hourOfDayDateFormat = new SimpleDateFormat("H");
        hourOfDay = hourOfDayDateFormat.format(currentDate);
    }

    @Test
    public void testComputeBootData_Happy() {
        Map<String, String> actualContextData = lifecycleState.computeBootData();
        assertNotNull(actualContextData.get(CONTEXT_DATA_KEY_APPLICATION_IDENTIFIER));
        assertEquals("100x100", actualContextData.get(CONTEXT_DATA_KEY_DEVICE_RESOLUTION));
        assertEquals("TEST_CARRIER", actualContextData.get(CONTEXT_DATA_KEY_CARRIER_NAME));
        assertEquals("TEST_OS 5.55", actualContextData.get(CONTEXT_DATA_KEY_OPERATING_SYSTEM));
        assertEquals("deviceName", actualContextData.get(CONTEXT_DATA_KEY_DEVICE_NAME));
        assertNotNull(actualContextData.get(CONTEXT_DATA_KEY_DAY_OF_WEEK));
        assertNotNull(actualContextData.get(CONTEXT_DATA_KEY_HOUR_OF_DAY));
        assertNull(actualContextData.get(CONTEXT_DATA_KEY_INSTALL_EVENT));
        assertEquals("en-US", actualContextData.get(CONTEXT_DATA_KEY_LOCALE));
        assertEquals("APPLICATION", actualContextData.get(CONTEXT_DATA_KEY_RUN_MODE));
    }

    @Test
    public void testComputeBootDataWithPersistedData() {
        Map<String, String> testMap = new HashMap<>();
        testMap.put("updateContextDataTestKey", "updateContextDataTestValue");
        when(lifecycleDataStore.getMap(DATASTORE_KEY_LIFECYCLE_DATA)).thenReturn(testMap);

        Map<String, String> actualContextData = lifecycleState.computeBootData();

        assertNotNull(actualContextData.get(CONTEXT_DATA_KEY_APPLICATION_IDENTIFIER));
        assertEquals("100x100", actualContextData.get(CONTEXT_DATA_KEY_DEVICE_RESOLUTION));
        assertEquals("TEST_CARRIER", actualContextData.get(CONTEXT_DATA_KEY_CARRIER_NAME));
        assertEquals("TEST_OS 5.55", actualContextData.get(CONTEXT_DATA_KEY_OPERATING_SYSTEM));
        assertEquals("deviceName", actualContextData.get(CONTEXT_DATA_KEY_DEVICE_NAME));
        assertNotNull(actualContextData.get(CONTEXT_DATA_KEY_DAY_OF_WEEK));
        assertNotNull(actualContextData.get(CONTEXT_DATA_KEY_HOUR_OF_DAY));
        assertNull(actualContextData.get(CONTEXT_DATA_KEY_INSTALL_EVENT));
        assertEquals("en-US", actualContextData.get(CONTEXT_DATA_KEY_LOCALE));
        assertEquals("APPLICATION", actualContextData.get(CONTEXT_DATA_KEY_RUN_MODE));
        assertEquals(
                "updateContextDataTestValue", actualContextData.get("updateContextDataTestKey"));
    }

    @Test
    public void testStart_Happy() {
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_PAUSE_DATE), anyLong()))
                .thenReturn(timestampOneSecEarlierInSeconds);
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_START_DATE), anyLong()))
                .thenReturn(timestampTenMinEarlierInSeconds);
        when(lifecycleDataStore.getString(eq(DATASTORE_KEY_LAST_VERSION), anyString()))
                .thenReturn(mockAppVersion);

        LifecycleSession.SessionInfo prevSessionInfo =
                lifecycleState.start(
                        currentTimestampInSeconds, null, null, DEFAULT_LIFECYCLE_TIMEOUT, true);

        assertNull(prevSessionInfo);

        ArgumentCaptor<Long> startDateCaptor = ArgumentCaptor.forClass(Long.class);
        verify(lifecycleDataStore, times(1))
                .setLong(eq(DATASTORE_KEY_START_DATE), startDateCaptor.capture());
        assertEquals(timestampTenMinEarlierInSeconds + 1, startDateCaptor.getValue().longValue());

        ArgumentCaptor<Boolean> successfulCloseCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(lifecycleDataStore, times(1))
                .setBoolean(eq(DATASTORE_KEY_SUCCESSFUL_CLOSE), successfulCloseCaptor.capture());
        assertFalse(successfulCloseCaptor.getValue());

        verify(lifecycleDataStore, times(1)).remove(DATASTORE_KEY_PAUSE_DATE);
        verify(lifecycleDataStore, never()).setString(eq(DATASTORE_KEY_LAST_VERSION), anyString());
    }

    @Test
    public void testStart_PreviousSessionCrashed() {
        final String osVersion = "ios 10.2";
        final String appId = "app_id_123";
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_INSTALL_DATE), anyLong()))
                .thenReturn(timestampOneDayEarlierSeconds);
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_LAST_USED_DATE), anyLong()))
                .thenReturn(0L);
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_START_DATE), anyLong()))
                .thenReturn(timestampTenMinEarlierInSeconds);
        when(lifecycleDataStore.getString(eq(DATASTORE_KEY_LAST_VERSION), anyString()))
                .thenReturn("1.0");
        when(lifecycleDataStore.getBoolean(eq(DATASTORE_KEY_SUCCESSFUL_CLOSE), anyBoolean()))
                .thenReturn(false);
        when(lifecycleDataStore.getString(eq(DATASTORE_KEY_OS_VERSION), anyString()))
                .thenReturn(osVersion);
        when(lifecycleDataStore.getString(eq(DATASTORE_KEY_APP_ID), anyString())).thenReturn(appId);

        LifecycleSession.SessionInfo prevSessionInfo =
                lifecycleState.start(
                        currentTimestampInSeconds, null, null, DEFAULT_LIFECYCLE_TIMEOUT, false);
        assertEquals(timestampTenMinEarlierInSeconds, prevSessionInfo.getStartTimestampInSeconds());
        assertEquals(0, prevSessionInfo.getPauseTimestampInSeconds());

        ArgumentCaptor<Integer> launchesCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(lifecycleDataStore, times(1))
                .setInt(eq(DATASTORE_KEY_LAUNCHES), launchesCaptor.capture());
        assertEquals(1, launchesCaptor.getValue().intValue());

        Map<String, String> actualContextData = lifecycleState.getContextData();
        assertEquals(
                CONTEXT_DATA_VALUE_CRASH_EVENT,
                actualContextData.get(CONTEXT_DATA_KEY_CRASH_EVENT));
        assertEquals(actualContextData.get(CONTEXT_DATA_KEY_PREVIOUS_OS_VERSION), osVersion);
        assertEquals(actualContextData.get(CONTEXT_DATA_KEY_PREVIOUS_APP_ID), appId);
        assertNotNull(actualContextData.get(CONTEXT_DATA_KEY_APPLICATION_IDENTIFIER));
        assertEquals("TEST_CARRIER", actualContextData.get(CONTEXT_DATA_KEY_CARRIER_NAME));
        assertTrue(actualContextData.containsKey(CONTEXT_DATA_KEY_DAILY_ENGAGED_EVENT));
        assertNotNull(actualContextData.get(CONTEXT_DATA_KEY_DAY_OF_WEEK));
        assertNotNull(actualContextData.get(CONTEXT_DATA_KEY_HOUR_OF_DAY));
        assertEquals("1", actualContextData.get(CONTEXT_DATA_KEY_DAYS_SINCE_FIRST_LAUNCH));
        assertNotNull(actualContextData.get(CONTEXT_DATA_KEY_IGNORED_SESSION_LENGTH));
        assertTrue(actualContextData.containsKey(CONTEXT_DATA_KEY_EVENT_LAUNCH));
        assertEquals("en-US", actualContextData.get(CONTEXT_DATA_KEY_LOCALE));
        assertTrue(actualContextData.containsKey(CONTEXT_DATA_KEY_MONTHLY_ENGAGED_EVENT));
        assertEquals("100x100", actualContextData.get(CONTEXT_DATA_KEY_DEVICE_RESOLUTION));
        assertEquals("deviceName", actualContextData.get(CONTEXT_DATA_KEY_DEVICE_NAME));
        assertEquals("APPLICATION", actualContextData.get(CONTEXT_DATA_KEY_RUN_MODE));
        assertTrue(actualContextData.containsKey(CONTEXT_DATA_KEY_UPGRADE_EVENT));
        assertNull(actualContextData.get(CONTEXT_DATA_KEY_INSTALL_EVENT));
    }

    @Test
    public void testStart_PreviousAppId() {
        final String osVersion = "ios 10.2";
        final String appId = "app_id_123";
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_INSTALL_DATE), anyLong()))
                .thenReturn(timestampOneDayEarlierSeconds);
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_START_DATE), anyLong()))
                .thenReturn(timestampTenMinEarlierInSeconds);
        when(lifecycleDataStore.getString(eq(DATASTORE_KEY_LAST_VERSION), anyString()))
                .thenReturn(mockAppVersion);
        when(lifecycleDataStore.getBoolean(eq(DATASTORE_KEY_SUCCESSFUL_CLOSE), anyBoolean()))
                .thenReturn(true);
        when(lifecycleDataStore.getString(eq(DATASTORE_KEY_OS_VERSION), anyString()))
                .thenReturn(osVersion);
        when(lifecycleDataStore.getString(eq(DATASTORE_KEY_APP_ID), anyString())).thenReturn(appId);

        lifecycleState.start(
                currentTimestampInSeconds, null, null, DEFAULT_LIFECYCLE_TIMEOUT, false);

        Map<String, String> actualContextData = lifecycleState.getContextData();
        assertTrue(actualContextData.containsKey(CONTEXT_DATA_KEY_LAUNCHES));
        assertEquals(osVersion, actualContextData.get(CONTEXT_DATA_KEY_PREVIOUS_OS_VERSION));
        assertEquals(appId, actualContextData.get(CONTEXT_DATA_KEY_PREVIOUS_APP_ID));
    }

    @Test
    public void testStart_AppResume_VersionUpgrade_NoLifecycleInMemory() {
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_PAUSE_DATE), anyLong()))
                .thenReturn(timestampOneSecEarlierInSeconds);
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_START_DATE), anyLong()))
                .thenReturn(timestampTenMinEarlierInSeconds);
        when(lifecycleDataStore.getString(eq(DATASTORE_KEY_LAST_VERSION), anyString()))
                .thenReturn(mockAppVersion);
        Map<String, String> mockLifecycleMap = new HashMap<>();
        mockLifecycleMap.put(CONTEXT_DATA_KEY_APPLICATION_IDENTIFIER, "NEW_APP_ID");
        when(lifecycleDataStore.getMap(DATASTORE_KEY_LIFECYCLE_DATA)).thenReturn(mockLifecycleMap);

        LifecycleSession.SessionInfo prevSessionInfo =
                lifecycleState.start(
                        currentTimestampInSeconds, null, null, DEFAULT_LIFECYCLE_TIMEOUT, false);

        assertNull(prevSessionInfo);
        assertEquals(
                "NEW_APP_ID",
                lifecycleState.getContextData().get(CONTEXT_DATA_KEY_APPLICATION_IDENTIFIER));
    }

    @Test
    public void testStart_AppResume_VersionUpgrade_LifecycleIsInMemory() {
        Map<String, String> mockLifecycleMap = new HashMap<>();
        mockLifecycleMap.put(CONTEXT_DATA_KEY_APPLICATION_IDENTIFIER, "A_DIFFERENT_APP_ID");
        lifecycleState.updateContextData(mockLifecycleMap);

        LifecycleSession.SessionInfo prevSessionInfo =
                lifecycleState.start(
                        currentTimestampInSeconds, null, null, DEFAULT_LIFECYCLE_TIMEOUT, true);

        assertEquals(0, prevSessionInfo.getStartTimestampInSeconds());
        assertEquals(0, prevSessionInfo.getPauseTimestampInSeconds());

        ArgumentCaptor<Integer> launchesCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(lifecycleDataStore, times(1))
                .setInt(eq(DATASTORE_KEY_LAUNCHES), launchesCaptor.capture());
        assertEquals(1, launchesCaptor.getValue().intValue());

        Map<String, String> actualContextData = lifecycleState.getContextData();
        assertEquals(
                "TEST_APPLICATION_NAME 1.1 (12345)",
                actualContextData.get(CONTEXT_DATA_KEY_APPLICATION_IDENTIFIER));
        assertEquals("TEST_CARRIER", actualContextData.get(CONTEXT_DATA_KEY_CARRIER_NAME));
        assertTrue(actualContextData.containsKey(CONTEXT_DATA_KEY_DAILY_ENGAGED_EVENT));
        assertNotNull(actualContextData.get(CONTEXT_DATA_KEY_DAY_OF_WEEK));
        assertNotNull(actualContextData.get(CONTEXT_DATA_KEY_HOUR_OF_DAY));
        assertEquals("deviceName", actualContextData.get(CONTEXT_DATA_KEY_DEVICE_NAME));
        assertEquals(
                CONTEXT_DATA_VALUE_INSTALL_EVENT,
                actualContextData.get(CONTEXT_DATA_KEY_INSTALL_EVENT));
        assertEquals(
                CONTEXT_DATA_VALUE_LAUNCH_EVENT,
                actualContextData.get(CONTEXT_DATA_KEY_EVENT_LAUNCH));
        assertEquals("en-US", actualContextData.get(CONTEXT_DATA_KEY_LOCALE));
        assertTrue(actualContextData.containsKey(CONTEXT_DATA_KEY_MONTHLY_ENGAGED_EVENT));
        assertEquals("TEST_OS 5.55", actualContextData.get(CONTEXT_DATA_KEY_OPERATING_SYSTEM));
        assertEquals("100x100", actualContextData.get(CONTEXT_DATA_KEY_DEVICE_RESOLUTION));
        assertEquals("APPLICATION", actualContextData.get(CONTEXT_DATA_KEY_RUN_MODE));
        assertNull(actualContextData.get(CONTEXT_DATA_KEY_UPGRADE_EVENT));
        assertNull(actualContextData.get(CONTEXT_DATA_KEY_CRASH_EVENT));
    }

    @Test
    public void testStart_AppResume_VersionsAreSame() {
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_PAUSE_DATE), anyLong()))
                .thenReturn(timestampOneSecEarlierInSeconds);
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_START_DATE), anyLong()))
                .thenReturn(timestampTenMinEarlierInSeconds);
        when(lifecycleDataStore.getString(eq(DATASTORE_KEY_LAST_VERSION), anyString()))
                .thenReturn(mockAppVersion);
        Map<String, String> mockLifecycleMap = new HashMap<>();
        String mockAppName = "TEST_APPLICATION_NAME";
        mockLifecycleMap.put(
                CONTEXT_DATA_KEY_APPLICATION_IDENTIFIER, mockAppName + " " + mockAppVersion + " ");
        when(lifecycleDataStore.getMap(DATASTORE_KEY_LIFECYCLE_DATA)).thenReturn(mockLifecycleMap);

        lifecycleState.start(
                currentTimestampInSeconds, null, null, DEFAULT_LIFECYCLE_TIMEOUT, false);

        assertEquals(
                mockAppName + " " + mockAppVersion + " ",
                lifecycleState.getContextData().get(CONTEXT_DATA_KEY_APPLICATION_IDENTIFIER));
    }

    @Test
    public void testStart_OverTimeout_AdditionalData() {
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_PAUSE_DATE), anyLong()))
                .thenReturn(timestampTenMinEarlierInSeconds);
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_START_DATE), anyLong()))
                .thenReturn(timestampOneHourEarlierInSeconds);
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_INSTALL_DATE), anyLong()))
                .thenReturn(timestampOneDayEarlierSeconds);
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_LAST_USED_DATE), anyLong()))
                .thenReturn(timestampTenMinEarlierInSeconds);
        when(lifecycleDataStore.getBoolean(eq(DATASTORE_KEY_SUCCESSFUL_CLOSE), anyBoolean()))
                .thenReturn(true);
        when(lifecycleDataStore.getString(eq(DATASTORE_KEY_LAST_VERSION), anyString()))
                .thenReturn(mockAppVersion);
        Map<String, String> additionalData = new HashMap<>();
        additionalData.put("testKey1", "testVal1");

        LifecycleSession.SessionInfo prevSessionInfo =
                lifecycleState.start(
                        currentTimestampInSeconds,
                        additionalData,
                        "adId",
                        DEFAULT_LIFECYCLE_TIMEOUT,
                        false);
        assertEquals(
                timestampOneHourEarlierInSeconds, prevSessionInfo.getStartTimestampInSeconds());
        assertEquals(timestampTenMinEarlierInSeconds, prevSessionInfo.getPauseTimestampInSeconds());

        ArgumentCaptor<Integer> launchesCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(lifecycleDataStore, times(1))
                .setInt(eq(DATASTORE_KEY_LAUNCHES), launchesCaptor.capture());
        assertEquals(1, launchesCaptor.getValue().intValue());

        ArgumentCaptor<Long> lastUsedDateCaptor = ArgumentCaptor.forClass(Long.class);
        verify(lifecycleDataStore, times(1))
                .setLong(eq(DATASTORE_KEY_LAST_USED_DATE), lastUsedDateCaptor.capture());
        assertEquals(currentTimestampInSeconds, lastUsedDateCaptor.getValue().longValue());

        ArgumentCaptor<Long> startDateCaptor = ArgumentCaptor.forClass(Long.class);
        verify(lifecycleDataStore, times(1))
                .setLong(eq(DATASTORE_KEY_START_DATE), startDateCaptor.capture());
        assertEquals(currentTimestampInSeconds, startDateCaptor.getValue().longValue());

        ArgumentCaptor<String> lastVersionCaptor = ArgumentCaptor.forClass(String.class);
        verify(lifecycleDataStore, times(1))
                .setString(eq(DATASTORE_KEY_LAST_VERSION), lastVersionCaptor.capture());
        assertEquals(mockAppVersion, lastVersionCaptor.getValue());

        ArgumentCaptor<Boolean> successfulCloseCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(lifecycleDataStore, times(1))
                .setBoolean(eq(DATASTORE_KEY_SUCCESSFUL_CLOSE), successfulCloseCaptor.capture());
        assertFalse(successfulCloseCaptor.getValue());

        verify(lifecycleDataStore, times(1)).remove(DATASTORE_KEY_PAUSE_DATE);

        Map<String, String> actualContextData = lifecycleState.getContextData();
        assertEquals(
                "TEST_APPLICATION_NAME 1.1 (12345)",
                actualContextData.get(CONTEXT_DATA_KEY_APPLICATION_IDENTIFIER));
        assertEquals("100x100", actualContextData.get(CONTEXT_DATA_KEY_DEVICE_RESOLUTION));
        assertEquals("TEST_CARRIER", actualContextData.get(CONTEXT_DATA_KEY_CARRIER_NAME));
        assertEquals("deviceName", actualContextData.get(CONTEXT_DATA_KEY_DEVICE_NAME));
        assertEquals(dayOfWeek, actualContextData.get(CONTEXT_DATA_KEY_DAY_OF_WEEK));
        assertEquals(hourOfDay, actualContextData.get(CONTEXT_DATA_KEY_HOUR_OF_DAY));
        assertEquals("1", actualContextData.get(CONTEXT_DATA_KEY_DAYS_SINCE_FIRST_LAUNCH));
        assertEquals("0", actualContextData.get(CONTEXT_DATA_KEY_DAYS_SINCE_LAST_LAUNCH));
        assertEquals("TEST_OS 5.55", actualContextData.get(CONTEXT_DATA_KEY_OPERATING_SYSTEM));
        assertEquals(
                CONTEXT_DATA_VALUE_LAUNCH_EVENT,
                actualContextData.get(CONTEXT_DATA_KEY_EVENT_LAUNCH));
        assertEquals("en-US", actualContextData.get(CONTEXT_DATA_KEY_LOCALE));
        assertEquals("APPLICATION", actualContextData.get(CONTEXT_DATA_KEY_RUN_MODE));
        assertEquals("3000", actualContextData.get(CONTEXT_DATA_KEY_PREVIOUS_SESSION_LENGTH));
        assertEquals("adId", actualContextData.get(IDENTITY_KEY_ADVERTISING_IDENTIFIER));
        assertEquals("testVal1", actualContextData.get("testKey1"));
    }

    @Test
    public void testPause_Happy() {
        Event testEvent = createPauseEvent(currentTimestampInMilliSeconds);

        lifecycleState.pause(testEvent);
        ArgumentCaptor<Boolean> successfulCloseCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(lifecycleDataStore, times(1))
                .setBoolean(eq(DATASTORE_KEY_SUCCESSFUL_CLOSE), successfulCloseCaptor.capture());
        assertTrue(successfulCloseCaptor.getValue());

        ArgumentCaptor<Long> pauseDateCaptor = ArgumentCaptor.forClass(Long.class);
        verify(lifecycleDataStore, times(1))
                .setLong(eq(DATASTORE_KEY_PAUSE_DATE), pauseDateCaptor.capture());
        assertEquals(currentTimestampInSeconds, pauseDateCaptor.getValue().longValue());
    }

    @Test
    public void testCheckForApplicationUpgrade_ContextDataNull() {
        lifecycleState.checkForApplicationUpgrade("");
        assertTrue(lifecycleState.getContextData().isEmpty());
    }

    @Test
    public void testCheckForApplicationUpgrade_ExistingLifecycleDataEmpty() {
        Map<String, String> mockLifecycleData = new HashMap<>();
        lifecycleState.updateContextData(mockLifecycleData);

        lifecycleState.checkForApplicationUpgrade(null);

        Map<String, String> lifecycleData = lifecycleState.getContextData();
        assertEquals(mockLifecycleData.size(), lifecycleData.size());
    }

    @Test
    public void testCheckForApplicationUpgrade_ExistingLifecycleDataInMemory() {
        Map<String, String> mockLifecycleData = new HashMap<>();
        mockLifecycleData.put(CONTEXT_DATA_KEY_APPLICATION_IDENTIFIER, "NEW_APP_ID");
        lifecycleState.updateContextData(mockLifecycleData);

        lifecycleState.checkForApplicationUpgrade("NEW_APP_ID");

        Map<String, String> lifecycleData = lifecycleState.getContextData();
        assertEquals("NEW_APP_ID", lifecycleData.get(CONTEXT_DATA_KEY_APPLICATION_IDENTIFIER));
    }

    @Test
    public void testCheckForApplicationUpgrade_ExistingLifecycleDataPersisted() {
        final Map<String, String> mockLifecycleMapJson = new HashMap<>();
        mockLifecycleMapJson.put(CONTEXT_DATA_KEY_APPLICATION_IDENTIFIER, "NEW_APP_ID");
        when(lifecycleDataStore.getMap(DATASTORE_KEY_LIFECYCLE_DATA))
                .thenReturn(mockLifecycleMapJson);

        lifecycleState.updateContextData(new HashMap<>());

        lifecycleState.checkForApplicationUpgrade("NEW_APP_ID");

        Map<String, String> lifecycleData = lifecycleState.getContextData();
        assertEquals("NEW_APP_ID", lifecycleData.get(CONTEXT_DATA_KEY_APPLICATION_IDENTIFIER));
    }

    @Test
    public void testCheckForApplicationUpgrade_AppUpgrade_Happy() {
        final Map<String, String> mockLifecycleMapJson = new HashMap<>();
        mockLifecycleMapJson.put(CONTEXT_DATA_KEY_APPLICATION_IDENTIFIER, "OLD_APP_ID");
        when(lifecycleDataStore.getMap(DATASTORE_KEY_LIFECYCLE_DATA))
                .thenReturn(mockLifecycleMapJson);
        when(lifecycleDataStore.getString(eq(DATASTORE_KEY_INSTALL_DATE), anyString()))
                .thenReturn("mockDate");
        when(lifecycleDataStore.getString(eq(DATASTORE_KEY_LAST_VERSION), anyString()))
                .thenReturn("OLD_APP_ID");
        when(deviceInfoService.getApplicationVersion()).thenReturn("newVersion");

        lifecycleState.checkForApplicationUpgrade("NEW_APP_ID");

        Map<String, String> lifecycleData = lifecycleState.getContextData();
        assertEquals("NEW_APP_ID", lifecycleData.get(CONTEXT_DATA_KEY_APPLICATION_IDENTIFIER));
    }

    @Test
    public void testGetContextData_Happy() {
        Map<String, String> testMap = new HashMap<>();
        testMap.put("updateContextDataTestKey", "updateContextDataTestValue");
        lifecycleState.updateContextData(testMap);
        Map<String, String> returnData = lifecycleState.getContextData();
        assertEquals(returnData.get("updateContextDataTestKey"), "updateContextDataTestValue");
    }

    @Test
    public void testRemoveContextData() {
        Map<String, String> testMap = new HashMap<>();
        testMap.put("removeContextDataTestKey", "removeContextDataTestValue");
        lifecycleState.updateContextData(testMap);
        testMap.put("removeContextDataTestKey", null);
        lifecycleState.updateContextData(testMap);

        Map<String, String> returnData = lifecycleState.getContextData();
        assertEquals(returnData.get("removeContextDataTestKey"), (null));
    }

    @Test
    public void testUpdateContextData_ContextDataExists_And_PreviousSessionExists() {
        Map<String, String> testMap = new HashMap<>();
        testMap.put("updateContextDataTestKey", "updateContextDataTestValue");
        lifecycleState.updateContextData(testMap);

        Map<String, String> testMap2 = new HashMap<>();
        testMap2.put("updateContextDataTestKey1", "updateContextDataTestValue1");
        lifecycleState.updatePreviousSessionLifecycleContextData(testMap2);

        Map<String, String> returnData = lifecycleState.getContextData();
        assertEquals(returnData.get("updateContextDataTestKey"), "updateContextDataTestValue");
    }

    @Test
    public void testUpdateContextData_ContextDataNull_And_PreviousSessionExists() {
        Map<String, String> testMap2 = new HashMap<>();
        testMap2.put("updateContextDataTestKey1", "updateContextDataTestValue1");
        lifecycleState.updatePreviousSessionLifecycleContextData(testMap2);

        Map<String, String> returnData = lifecycleState.getContextData();
        assertEquals(returnData.get("updateContextDataTestKey1"), "updateContextDataTestValue1");
    }

    @Test
    public void
            testUpdateContextData_ContextDataNull_And_PreviousSessionNull_AndPersistedDataExists() {
        Map<String, String> testMap = new HashMap<>();
        testMap.put("updateContextDataTestKey", "updateContextDataTestValue");
        when(lifecycleDataStore.getMap(DATASTORE_KEY_LIFECYCLE_DATA)).thenReturn(testMap);

        Map<String, String> returnData = lifecycleState.getContextData();
        assertEquals(returnData.get("updateContextDataTestKey"), "updateContextDataTestValue");
    }

    @Test
    public void testGetPersistedContextData_EmptyData() {
        assertTrue(lifecycleState.getPersistedContextData().isEmpty());
    }

    @Test
    public void testGetPersistedContextData() {
        Map<String, String> testMap = new HashMap<>();
        testMap.put("updateContextDataTestKey", "updateContextDataTestValue");
        when(lifecycleDataStore.getMap(DATASTORE_KEY_LIFECYCLE_DATA)).thenReturn(testMap);

        Map<String, String> returnData = lifecycleState.getPersistedContextData();
        assertEquals(returnData.get("updateContextDataTestKey"), "updateContextDataTestValue");
    }
}
