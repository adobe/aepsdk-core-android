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
import static com.adobe.marketing.mobile.LifecycleEventGeneratorTestHelper.createStartEvent;
import static com.adobe.marketing.mobile.lifecycle.LifecycleConstants.MAX_SESSION_LENGTH_SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.app.Application;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.EventSource;
import com.adobe.marketing.mobile.EventType;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.SharedStateStatus;
import com.adobe.marketing.mobile.TestableExtensionApi;
import com.adobe.marketing.mobile.services.DeviceInforming;
import com.adobe.marketing.mobile.services.NamedCollection;
import com.adobe.marketing.mobile.services.ServiceProvider;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LifecycleFunctionalTest {

    private TestableExtensionApi mockExtensionApi;
    private MockDeviceInfoService mockDeviceInfoService;
    private NamedCollection lifecycleDataStore;

    private static final String APP_ID = "appid";
    private static final String CARRIER_NAME = "carriername";
    private static final String CRASH_EVENT = "crashevent";
    private static final String DAILY_ENGAGED_EVENT = "dailyenguserevent";
    private static final String DAY_OF_WEEK = "dayofweek";
    private static final String DAYS_SINCE_FIRST_LAUNCH = "dayssincefirstuse";
    private static final String DAYS_SINCE_LAST_LAUNCH = "dayssincelastuse";
    private static final String DAYS_SINCE_LAST_UPGRADE = "dayssincelastupgrade";
    private static final String DEVICE_NAME = "devicename";
    private static final String DEVICE_RESOLUTION = "resolution";
    private static final String HOUR_OF_DAY = "hourofday";
    private static final String IGNORED_SESSION_LENGTH = "ignoredsessionlength";
    private static final String INSTALL_DATE = "installdate";
    private static final String INSTALL_EVENT = "installevent";
    private static final String LAUNCH_EVENT = "launchevent";
    private static final String LAUNCHES = "launches";
    private static final String LIFECYCLE_CONTEXT_DATA = "lifecyclecontextdata";
    private static final String LIFECYCLE_START = "start";
    private static final String LOCALE = "locale";
    private static final String MAX_SESSION_LENGTH = "maxsessionlength";
    private static final String MONTHLY_ENGAGED_EVENT = "monthlyenguserevent";
    private static final String OPERATING_SYSTEM = "osversion";
    private static final String PREVIOUS_SESSION_LENGTH = "prevsessionlength";
    private static final String PREVIOUS_APPID = "previousappid";
    private static final String PREVIOUS_OS = "previousosversion";
    private static final String PREVIOUS_SESSION_PAUSE_TIMESTAMP =
            "previoussessionpausetimestampmillis";
    private static final String PREVIOUS_SESSION_START_TIMESTAMP =
            "previoussessionstarttimestampmillis";
    private static final String RUN_MODE = "runmode";
    private static final String SESSION_EVENT = "sessionevent";
    private static final String SESSION_START_TIMESTAMP = "starttimestampmillis";
    private static final String UPGRADE_EVENT = "upgradeevent";
    private static final String DATA_STORE_NAME = "AdobeMobile_Lifecycle";
    private static final String LIFECYCLE_CONFIG_SESSION_TIMEOUT = "lifecycle.sessionTimeout";

    private String dayOfWeek;
    private String hourOfDay;
    private String dayMonthYearDate;
    private long currentTimestampMillis;

    @Before
    public void beforeEach() {
        setupMockDeviceInfoService();
        MobileCore.setApplication(
                (Application)
                        InstrumentationRegistry.getInstrumentation()
                                .getContext()
                                .getApplicationContext());
        lifecycleDataStore =
                ServiceProvider.getInstance()
                        .getDataStoreService()
                        .getNamedCollection(DATA_STORE_NAME);

        mockExtensionApi = new TestableExtensionApi();
        LifecycleExtension lifecycleExtension =
                new LifecycleExtension(mockExtensionApi, lifecycleDataStore, mockDeviceInfoService);
        lifecycleExtension.onRegistered();
        mockExtensionApi.resetDispatchedEventAndCreatedSharedState();
        mockExtensionApi.ignoreEvent(EventType.LIFECYCLE, EventSource.APPLICATION_CLOSE);
        mockExtensionApi.ignoreEvent(EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH);
        lifecycleDataStore.removeAll();
        initTimestamps();

        Map<String, Object> configurationMap = new HashMap<>();
        configurationMap.put(LIFECYCLE_CONFIG_SESSION_TIMEOUT, 30L);
        mockExtensionApi.simulateSharedState(
                "com.adobe.module.configuration", SharedStateStatus.SET, configurationMap);
    }

    private void setupMockDeviceInfoService() {
        mockDeviceInfoService = new MockDeviceInfoService();
        mockDeviceInfoService.applicationName = "TEST_APPLICATION_NAME";
        mockDeviceInfoService.applicationVersion = "1.1";
        mockDeviceInfoService.deviceName = "deviceName";
        mockDeviceInfoService.applicationVersionCode = "12345";
        mockDeviceInfoService.displayInformation =
                new DeviceInforming.DisplayInformation() {
                    @Override
                    public int getWidthPixels() {
                        return 100;
                    }

                    @Override
                    public int getHeightPixels() {
                        return 100;
                    }

                    @Override
                    public int getDensityDpi() {
                        return 100;
                    }
                };
        mockDeviceInfoService.deviceBuildId = "TEST_PLATFORM";
        mockDeviceInfoService.operatingSystemName = "TEST_OS";
        mockDeviceInfoService.operatingSystemVersion = "5.55";
        mockDeviceInfoService.mobileCarrierName = "TEST_CARRIER";
        mockDeviceInfoService.activeLocale = new Locale("en", "US");
        mockDeviceInfoService.runMode = "APPLICATION";
    }

    private void initTimestamps() {
        currentTimestampMillis =
                1483864368225L; // start: Sunday, January 8, 2017 8:32:48.225 AM GMT
        final Date currentDate = new Date(currentTimestampMillis);

        dayOfWeek = getDayOfWeek(currentTimestampMillis);

        DateFormat hourOfDayDateFormat = new SimpleDateFormat("H");
        hourOfDay = hourOfDayDateFormat.format(currentDate);

        DateFormat dayMonthYearFormat = new SimpleDateFormat("M/d/yyyy");
        dayMonthYearDate = dayMonthYearFormat.format(currentDate);
    }

    private String getDayOfWeek(long timestampMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestampMillis);
        int dayOfWeekNumber = cal.get(Calendar.DAY_OF_WEEK);
        return Integer.toString(dayOfWeekNumber);
    }

    @Test
    public void
            testLifecycle__When__Start__Then__DispatchLifecycleContextDataUpdate_InstallEvent() {
        // test
        mockExtensionApi.simulateComingEvent(createStartEvent(null, currentTimestampMillis));

        // verify session start dispatches response content event
        List<Event> events = mockExtensionApi.dispatchedEvents;
        assertEquals(1, events.size());

        // verify session start updates lifecycle shared state
        List<Map<String, Object>> sharedStateList = mockExtensionApi.createdSharedState;
        assertEquals(1, sharedStateList.size());

        Map<String, String> expectedContextData =
                new HashMap<String, String>() {
                    {
                        put(INSTALL_EVENT, "InstallEvent");
                        put(LAUNCH_EVENT, "LaunchEvent");
                        put(INSTALL_DATE, dayMonthYearDate);
                        put(HOUR_OF_DAY, hourOfDay);
                        put(DAY_OF_WEEK, dayOfWeek);
                        put(LAUNCHES, "1");
                        put(OPERATING_SYSTEM, "TEST_OS 5.55");
                        put(LOCALE, "en-US");
                        put(DEVICE_RESOLUTION, "100x100");
                        put(CARRIER_NAME, "TEST_CARRIER");
                        put(DEVICE_NAME, "deviceName");
                        put(APP_ID, "TEST_APPLICATION_NAME 1.1 (12345)");
                        put(RUN_MODE, "APPLICATION");
                        put(DAILY_ENGAGED_EVENT, "DailyEngUserEvent");
                        put(MONTHLY_ENGAGED_EVENT, "MonthlyEngUserEvent");
                    }
                };
        Map<String, Object> expectedEventData =
                new HashMap<String, Object>() {
                    {
                        put(PREVIOUS_SESSION_START_TIMESTAMP, 0L);
                        put(PREVIOUS_SESSION_PAUSE_TIMESTAMP, 0L);
                        put(MAX_SESSION_LENGTH, MAX_SESSION_LENGTH_SECONDS);
                        put(
                                SESSION_START_TIMESTAMP,
                                TimeUnit.MILLISECONDS.toSeconds(currentTimestampMillis));
                        put(SESSION_EVENT, LIFECYCLE_START);
                        put(LIFECYCLE_CONTEXT_DATA, expectedContextData);
                    }
                };
        Map<String, Object> expectedSharedState =
                new HashMap<String, Object>() {
                    {
                        put(MAX_SESSION_LENGTH, MAX_SESSION_LENGTH_SECONDS);
                        put(
                                SESSION_START_TIMESTAMP,
                                TimeUnit.MILLISECONDS.toSeconds(currentTimestampMillis));
                        put(LIFECYCLE_CONTEXT_DATA, expectedContextData);
                    }
                };

        Event sessionStartResponseEvent = events.get(0);
        assertEquals(EventType.LIFECYCLE, sessionStartResponseEvent.getType());
        assertEquals(EventSource.RESPONSE_CONTENT, sessionStartResponseEvent.getSource());
        assertEquals(expectedEventData, sessionStartResponseEvent.getEventData());

        Map<String, Object> updatedLifecycleSharedState = sharedStateList.get(0);
        assertEquals(expectedSharedState, updatedLifecycleSharedState);

        assertEquals(
                TimeUnit.MILLISECONDS.toSeconds(currentTimestampMillis),
                lifecycleDataStore.getLong("InstallDate", 0L));
    }

    @Test
    public void
            testLifecycle__When__Start__WithAdditionalData__Then__DispatchLifecycleContextDataUpdateWithAdditionalData() {
        // test
        Map<String, String> additionalContextData = new HashMap<>();
        additionalContextData.put("testKey", "testVal");
        mockExtensionApi.simulateComingEvent(
                createStartEvent(additionalContextData, currentTimestampMillis));

        // verify session start dispatches response content event
        List<Event> events = mockExtensionApi.dispatchedEvents;
        assertEquals(1, events.size());

        // verify session start updates lifecycle shared state
        List<Map<String, Object>> sharedStateList = mockExtensionApi.createdSharedState;
        assertEquals(1, sharedStateList.size());

        // verify updated shared state context data has additional context data from lifecycle
        // request event
        Map<String, String> sessionStartContextData =
                (Map<String, String>) events.get(0).getEventData().get(LIFECYCLE_CONTEXT_DATA);
        assertEquals("testVal", sessionStartContextData.get("testKey"));
    }

    @Test
    public void testLifecycle__When__SecondLaunch_BeforeSessionTimeout__Then__GetNoLifecycleHit() {
        // setup
        long firstSessionStartTimeMillis = currentTimestampMillis;
        long firstSessionPauseTimeMillis =
                firstSessionStartTimeMillis + TimeUnit.SECONDS.toMillis(10);
        long secondSessionStartTimeMillis =
                firstSessionStartTimeMillis + TimeUnit.SECONDS.toMillis(30);

        // test
        mockExtensionApi.simulateComingEvent(createStartEvent(null, firstSessionStartTimeMillis));
        mockExtensionApi.simulateComingEvent(createPauseEvent(firstSessionPauseTimeMillis));
        mockExtensionApi.simulateComingEvent(createStartEvent(null, secondSessionStartTimeMillis));

        // verify only first session start dispatches response content event
        assertEquals(1, mockExtensionApi.dispatchedEvents.size());

        // verify both session starts updates lifecycle shared state
        assertEquals(2, mockExtensionApi.createdSharedState.size());

        Map<String, Object> firstSessionStartSharedState =
                mockExtensionApi.createdSharedState.get(0);
        assertEquals(
                TimeUnit.MILLISECONDS.toSeconds(firstSessionStartTimeMillis),
                firstSessionStartSharedState.get(SESSION_START_TIMESTAMP));

        Map<String, Object> secondSessionStartSharedState =
                mockExtensionApi.createdSharedState.get(1);
        // Adjusted start time =  previous start time + new start time - previous pause time
        assertEquals(
                TimeUnit.MILLISECONDS.toSeconds(
                        firstSessionStartTimeMillis
                                + secondSessionStartTimeMillis
                                - firstSessionPauseTimeMillis),
                secondSessionStartSharedState.get(SESSION_START_TIMESTAMP));
    }

    @Test
    public void
            testLifecycle__When__Start__Then__DispatchLifecycleContextDataUpdate_SecondLaunchEvent() {
        // setup
        long firstSessionStartTimeMillis = currentTimestampMillis;
        long firstSessionPauseTimeMillis = firstSessionStartTimeMillis + 100L;
        long secondSessionStartTimeMillis =
                firstSessionStartTimeMillis + TimeUnit.SECONDS.toMillis(40);

        // test
        mockExtensionApi.simulateComingEvent(createStartEvent(null, firstSessionStartTimeMillis));
        mockExtensionApi.simulateComingEvent(createPauseEvent(firstSessionPauseTimeMillis));
        mockExtensionApi.simulateComingEvent(createStartEvent(null, secondSessionStartTimeMillis));

        // verify both session start dispatches response content event
        assertEquals(2, mockExtensionApi.dispatchedEvents.size());

        // verify both session starts updates lifecycle shared state
        assertEquals(2, mockExtensionApi.createdSharedState.size());

        Map<String, String> expectedContextData =
                new HashMap<String, String>() {
                    {
                        put(LAUNCH_EVENT, "LaunchEvent");
                        put(HOUR_OF_DAY, hourOfDay);
                        put(DAY_OF_WEEK, dayOfWeek);
                        put(LAUNCHES, "2");
                        put(PREVIOUS_OS, "TEST_OS 5.55");
                        put(OPERATING_SYSTEM, "TEST_OS 5.55");
                        put(LOCALE, "en-US");
                        put(DEVICE_RESOLUTION, "100x100");
                        put(CARRIER_NAME, "TEST_CARRIER");
                        put(DEVICE_NAME, "deviceName");
                        put(PREVIOUS_APPID, "TEST_APPLICATION_NAME 1.1 (12345)");
                        put(APP_ID, "TEST_APPLICATION_NAME 1.1 (12345)");
                        put(RUN_MODE, "APPLICATION");
                        put(DAYS_SINCE_FIRST_LAUNCH, "0");
                        put(DAYS_SINCE_LAST_LAUNCH, "0");
                        put(IGNORED_SESSION_LENGTH, "0");
                    }
                };

        Map<String, Object> secondSessionStartResponseEventData =
                mockExtensionApi.dispatchedEvents.get(1).getEventData();
        assertEquals(
                expectedContextData,
                secondSessionStartResponseEventData.get(LIFECYCLE_CONTEXT_DATA));

        Map<String, Object> secondSessionStartSharedState =
                mockExtensionApi.createdSharedState.get(1);
        assertEquals(
                expectedContextData, secondSessionStartSharedState.get(LIFECYCLE_CONTEXT_DATA));
    }

    @Test
    public void testLifecycle__When__SecondLaunch_AfterSessionTimeout__Then__GetLaunchEvent() {
        // setup
        long firstSessionStartTimeMillis = currentTimestampMillis;
        long firstSessionPauseTimeMillis =
                firstSessionStartTimeMillis + TimeUnit.SECONDS.toMillis(10);
        long secondSessionStartTimeMillis =
                firstSessionStartTimeMillis + TimeUnit.SECONDS.toMillis(40);

        // test
        mockExtensionApi.simulateComingEvent(createStartEvent(null, firstSessionStartTimeMillis));
        mockExtensionApi.simulateComingEvent(createPauseEvent(firstSessionPauseTimeMillis));
        mockExtensionApi.simulateComingEvent(createStartEvent(null, secondSessionStartTimeMillis));

        // verify both session start dispatches response content event
        assertEquals(2, mockExtensionApi.dispatchedEvents.size());

        // verify both session starts updates lifecycle shared state
        assertEquals(2, mockExtensionApi.createdSharedState.size());

        Map<String, String> firstSessionStartResponseEventContextData =
                (Map<String, String>)
                        mockExtensionApi
                                .dispatchedEvents
                                .get(0)
                                .getEventData()
                                .get(LIFECYCLE_CONTEXT_DATA);
        assertEquals("1", firstSessionStartResponseEventContextData.get(LAUNCHES));

        Map<String, String> expectedContextData =
                new HashMap<String, String>() {
                    {
                        put(LAUNCH_EVENT, "LaunchEvent");
                        put(HOUR_OF_DAY, hourOfDay);
                        put(DAY_OF_WEEK, dayOfWeek);
                        put(LAUNCHES, "2");
                        put(OPERATING_SYSTEM, "TEST_OS 5.55");
                        put(LOCALE, "en-US");
                        put(DEVICE_RESOLUTION, "100x100");
                        put(CARRIER_NAME, "TEST_CARRIER");
                        put(DEVICE_NAME, "deviceName");
                        put(APP_ID, "TEST_APPLICATION_NAME 1.1 (12345)");
                        put(RUN_MODE, "APPLICATION");
                        put(PREVIOUS_SESSION_LENGTH, "10");
                        put(DAYS_SINCE_FIRST_LAUNCH, "0");
                        put(DAYS_SINCE_LAST_LAUNCH, "0");
                        put(PREVIOUS_APPID, "TEST_APPLICATION_NAME 1.1 (12345)");
                        put(PREVIOUS_OS, "TEST_OS 5.55");
                    }
                };
        Map<String, Object> expectedEventData =
                new HashMap<String, Object>() {
                    {
                        put(
                                PREVIOUS_SESSION_START_TIMESTAMP,
                                TimeUnit.MILLISECONDS.toSeconds(firstSessionStartTimeMillis));
                        put(
                                PREVIOUS_SESSION_PAUSE_TIMESTAMP,
                                TimeUnit.MILLISECONDS.toSeconds(firstSessionPauseTimeMillis));
                        put(MAX_SESSION_LENGTH, MAX_SESSION_LENGTH_SECONDS);
                        put(
                                SESSION_START_TIMESTAMP,
                                TimeUnit.MILLISECONDS.toSeconds(secondSessionStartTimeMillis));
                        put(SESSION_EVENT, LIFECYCLE_START);
                        put(LIFECYCLE_CONTEXT_DATA, expectedContextData);
                    }
                };

        Map<String, Object> expectedSharedState =
                new HashMap<String, Object>() {
                    {
                        put(MAX_SESSION_LENGTH, MAX_SESSION_LENGTH_SECONDS);
                        put(
                                SESSION_START_TIMESTAMP,
                                TimeUnit.MILLISECONDS.toSeconds(secondSessionStartTimeMillis));
                        put(LIFECYCLE_CONTEXT_DATA, expectedContextData);
                    }
                };

        Map<String, Object> secondSessionStartResponseEventData =
                mockExtensionApi.dispatchedEvents.get(1).getEventData();
        assertEquals(expectedEventData, secondSessionStartResponseEventData);

        Map<String, Object> secondSessionSharedState = mockExtensionApi.createdSharedState.get(1);
        assertEquals(expectedSharedState, secondSessionSharedState);
    }

    @Test
    public void testLifecycle__When__Crash() {
        // setup
        Map<String, Object> configurationMap = new HashMap<>();
        configurationMap.put(LIFECYCLE_CONFIG_SESSION_TIMEOUT, 1L);

        TestableExtensionApi mockExtensionApi2 = new TestableExtensionApi();
        mockExtensionApi2.ignoreEvent(EventType.LIFECYCLE, EventSource.APPLICATION_CLOSE);
        mockExtensionApi2.ignoreEvent(EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH);
        mockExtensionApi2.simulateSharedState(
                "com.adobe.module.configuration", SharedStateStatus.SET, configurationMap);

        // test
        mockExtensionApi.simulateComingEvent(createStartEvent(null, currentTimestampMillis));

        LifecycleExtension lifecycleSession2 = new LifecycleExtension(mockExtensionApi2);
        lifecycleSession2.onRegistered();
        mockExtensionApi2.resetDispatchedEventAndCreatedSharedState();
        mockExtensionApi2.simulateComingEvent(createStartEvent(null, currentTimestampMillis));

        // verify second session start dispatches lifecycle response event
        assertEquals(1, mockExtensionApi2.dispatchedEvents.size());

        Map<String, String> secondSessionStartResponseEventContextData =
                (Map<String, String>)
                        mockExtensionApi2
                                .dispatchedEvents
                                .get(0)
                                .getEventData()
                                .get(LIFECYCLE_CONTEXT_DATA);
        assertEquals("CrashEvent", secondSessionStartResponseEventContextData.get(CRASH_EVENT));
    }

    @Test
    public void
            testLifecycle__When__SecondLaunch_OverMaxSessionTime__Then__GetLaunchEvent_WithIgnoredSessionLength() {
        // setup
        long firstSessionStartTimeMillis = currentTimestampMillis;
        long firstSessionPauseTimeMillis = firstSessionStartTimeMillis + TimeUnit.DAYS.toMillis(8);
        long secondSessionStartTimeMillis =
                firstSessionPauseTimeMillis + TimeUnit.SECONDS.toMillis(40);

        // test
        mockExtensionApi.simulateComingEvent(createStartEvent(null, firstSessionStartTimeMillis));
        mockExtensionApi.simulateComingEvent(createPauseEvent(firstSessionPauseTimeMillis));
        mockExtensionApi.simulateComingEvent(createStartEvent(null, secondSessionStartTimeMillis));

        // verify both session start dispatches response content event
        assertEquals(2, mockExtensionApi.dispatchedEvents.size());

        // verify both session starts updates lifecycle shared state
        assertEquals(2, mockExtensionApi.createdSharedState.size());

        Map<String, Object> secondSessionStartResponseEventData =
                mockExtensionApi.dispatchedEvents.get(1).getEventData();
        Map<String, String> secondSessionStartResponseEventContextData =
                (Map<String, String>)
                        secondSessionStartResponseEventData.get(LIFECYCLE_CONTEXT_DATA);
        assertEquals(
                String.valueOf(TimeUnit.DAYS.toSeconds(8)),
                secondSessionStartResponseEventContextData.get(IGNORED_SESSION_LENGTH));

        Map<String, Object> secondSessionStartSharedState =
                mockExtensionApi.createdSharedState.get(1);
        Map<String, String> secondSessionStartSharedStateContextData =
                (Map<String, String>) secondSessionStartSharedState.get(LIFECYCLE_CONTEXT_DATA);
        assertEquals(
                String.valueOf(TimeUnit.DAYS.toSeconds(8)),
                secondSessionStartSharedStateContextData.get(IGNORED_SESSION_LENGTH));

        assertEquals(
                MAX_SESSION_LENGTH_SECONDS,
                secondSessionStartResponseEventData.get(MAX_SESSION_LENGTH));
        assertEquals(
                MAX_SESSION_LENGTH_SECONDS, secondSessionStartSharedState.get(MAX_SESSION_LENGTH));
    }

    @Test
    public void testLifecycle__When__SecondLaunch_VersionNumberChanged__Then__GetUpgradeEvent() {
        // setup
        long firstSessionStartTimeMillis = currentTimestampMillis;
        long firstSessionPauseTimeMillis =
                firstSessionStartTimeMillis + TimeUnit.SECONDS.toMillis(10);
        long secondSessionStartTimeMillis = firstSessionPauseTimeMillis + TimeUnit.DAYS.toMillis(1);

        Map<String, Object> configurationMap = new HashMap<>();
        configurationMap.put(LIFECYCLE_CONFIG_SESSION_TIMEOUT, 30L);

        TestableExtensionApi mockExtensionApi2 = new TestableExtensionApi();
        mockExtensionApi2.ignoreEvent(EventType.LIFECYCLE, EventSource.APPLICATION_CLOSE);
        mockExtensionApi2.ignoreEvent(EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH);
        mockExtensionApi2.simulateSharedState(
                "com.adobe.module.configuration", SharedStateStatus.SET, configurationMap);

        // test
        mockExtensionApi.simulateComingEvent(createStartEvent(null, firstSessionStartTimeMillis));
        mockExtensionApi.simulateComingEvent(createPauseEvent(firstSessionPauseTimeMillis));

        mockDeviceInfoService.applicationVersion = "1.2";
        LifecycleExtension lifecycleSession2 =
                new LifecycleExtension(
                        mockExtensionApi2, lifecycleDataStore, mockDeviceInfoService);
        lifecycleSession2.onRegistered();
        mockExtensionApi2.resetDispatchedEventAndCreatedSharedState();
        mockExtensionApi2.simulateComingEvent(createStartEvent(null, secondSessionStartTimeMillis));

        // verify
        Map<String, String> expectedContextData =
                new HashMap<String, String>() {
                    {
                        put(LAUNCH_EVENT, "LaunchEvent");
                        put(UPGRADE_EVENT, "UpgradeEvent");
                        put(HOUR_OF_DAY, hourOfDay);
                        put(DAY_OF_WEEK, getDayOfWeek(secondSessionStartTimeMillis));
                        put(LAUNCHES, "2");
                        put(OPERATING_SYSTEM, "TEST_OS 5.55");
                        put(LOCALE, "en-US");
                        put(DEVICE_RESOLUTION, "100x100");
                        put(CARRIER_NAME, "TEST_CARRIER");
                        put(DEVICE_NAME, "deviceName");
                        put(APP_ID, "TEST_APPLICATION_NAME 1.2 (12345)");
                        put(RUN_MODE, "APPLICATION");
                        put(PREVIOUS_SESSION_LENGTH, "10");
                        put(DAYS_SINCE_FIRST_LAUNCH, "1");
                        put(DAYS_SINCE_LAST_LAUNCH, "1");
                        put(PREVIOUS_APPID, "TEST_APPLICATION_NAME 1.1 (12345)");
                        put(PREVIOUS_OS, "TEST_OS 5.55");
                        put(DAILY_ENGAGED_EVENT, "DailyEngUserEvent");
                    }
                };
        Map<String, Object> expectedEventData =
                new HashMap<String, Object>() {
                    {
                        put(
                                PREVIOUS_SESSION_START_TIMESTAMP,
                                TimeUnit.MILLISECONDS.toSeconds(firstSessionStartTimeMillis));
                        put(
                                PREVIOUS_SESSION_PAUSE_TIMESTAMP,
                                TimeUnit.MILLISECONDS.toSeconds(firstSessionPauseTimeMillis));
                        put(MAX_SESSION_LENGTH, MAX_SESSION_LENGTH_SECONDS);
                        put(
                                SESSION_START_TIMESTAMP,
                                TimeUnit.MILLISECONDS.toSeconds(secondSessionStartTimeMillis));
                        put(SESSION_EVENT, LIFECYCLE_START);
                        put(LIFECYCLE_CONTEXT_DATA, expectedContextData);
                    }
                };
        Map<String, Object> expectedSharedState =
                new HashMap<String, Object>() {
                    {
                        put(MAX_SESSION_LENGTH, MAX_SESSION_LENGTH_SECONDS);
                        put(
                                SESSION_START_TIMESTAMP,
                                TimeUnit.MILLISECONDS.toSeconds(secondSessionStartTimeMillis));
                        put(LIFECYCLE_CONTEXT_DATA, expectedContextData);
                    }
                };

        Map<String, Object> secondSessionStartResponseEventData =
                mockExtensionApi2.dispatchedEvents.get(0).getEventData();
        assertEquals(expectedEventData, secondSessionStartResponseEventData);
        Map<String, Object> secondSessionStarSharedState =
                mockExtensionApi2.createdSharedState.get(0);
        assertEquals(expectedSharedState, secondSessionStarSharedState);

        assertEquals(
                TimeUnit.MILLISECONDS.toSeconds(firstSessionStartTimeMillis),
                lifecycleDataStore.getLong("InstallDate", 0L));
        assertEquals(
                TimeUnit.MILLISECONDS.toSeconds(secondSessionStartTimeMillis),
                lifecycleDataStore.getLong("UpgradeDate", 0L));
    }

    @Test
    public void
            testLifecycle__When__SecondLaunch_ThreeDaysAfterInstall__Then__DaysSinceFirstUseIs3() {
        // setup
        long firstSessionStartTime = currentTimestampMillis;
        long firstSessionPauseTime = firstSessionStartTime + TimeUnit.DAYS.toMillis(3);
        long secondSessionStartTime = firstSessionPauseTime + TimeUnit.SECONDS.toMillis(30);

        // test
        mockExtensionApi.simulateComingEvent(createStartEvent(null, firstSessionStartTime));
        mockExtensionApi.simulateComingEvent(createPauseEvent(firstSessionPauseTime));
        mockExtensionApi.resetDispatchedEventAndCreatedSharedState();
        mockExtensionApi.simulateComingEvent(createStartEvent(null, secondSessionStartTime));

        // verify
        Map<String, String> secondSessionStartResponseEventContextData =
                (Map<String, String>)
                        mockExtensionApi
                                .dispatchedEvents
                                .get(0)
                                .getEventData()
                                .get(LIFECYCLE_CONTEXT_DATA);
        assertEquals("3", secondSessionStartResponseEventContextData.get(DAYS_SINCE_FIRST_LAUNCH));
        assertEquals(
                "DailyEngUserEvent",
                secondSessionStartResponseEventContextData.get(DAILY_ENGAGED_EVENT));
        assertNull(secondSessionStartResponseEventContextData.get(MONTHLY_ENGAGED_EVENT));

        Map<String, String> secondSessionStartSharedStateContextData =
                (Map<String, String>)
                        mockExtensionApi.createdSharedState.get(0).get(LIFECYCLE_CONTEXT_DATA);
        assertEquals("3", secondSessionStartSharedStateContextData.get(DAYS_SINCE_FIRST_LAUNCH));
        assertEquals(
                "DailyEngUserEvent",
                secondSessionStartSharedStateContextData.get(DAILY_ENGAGED_EVENT));
        assertNull(secondSessionStartSharedStateContextData.get(MONTHLY_ENGAGED_EVENT));
    }

    @Test
    public void
            testLifecycle__When__SecondLaunch_ThreeDaysAfterLastUse__Then__DaysSinceLastUseIs3() {
        // setup
        long firstSessionStartTime = currentTimestampMillis;
        long firstSessionPauseTime = firstSessionStartTime + TimeUnit.SECONDS.toMillis(20);
        long secondSessionStartTime = firstSessionPauseTime + TimeUnit.DAYS.toMillis(3);

        // test
        mockExtensionApi.simulateComingEvent(createStartEvent(null, firstSessionStartTime));
        mockExtensionApi.simulateComingEvent(createPauseEvent(firstSessionPauseTime));
        mockExtensionApi.resetDispatchedEventAndCreatedSharedState();
        mockExtensionApi.simulateComingEvent(createStartEvent(null, secondSessionStartTime));

        // verify
        Map<String, String> secondSessionStartResponseEventContextData =
                (Map<String, String>)
                        mockExtensionApi
                                .dispatchedEvents
                                .get(0)
                                .getEventData()
                                .get(LIFECYCLE_CONTEXT_DATA);
        assertEquals("3", secondSessionStartResponseEventContextData.get(DAYS_SINCE_LAST_LAUNCH));
        Map<String, String> secondSessionStartSharedStateContextData =
                (Map<String, String>)
                        mockExtensionApi.createdSharedState.get(0).get(LIFECYCLE_CONTEXT_DATA);
        assertEquals("3", secondSessionStartSharedStateContextData.get(DAYS_SINCE_LAST_LAUNCH));
    }

    @Test
    public void testLifecycle__When__ThreeDaysAfterUpgrade__Then__DaysSinceLastUpgradeIs3() {
        // setup
        long firstSessionStartTimeMillis = currentTimestampMillis;
        long firstSessionPauseTimeMillis =
                firstSessionStartTimeMillis + TimeUnit.SECONDS.toMillis(20);
        long secondSessionStartTimeMillis =
                firstSessionPauseTimeMillis + TimeUnit.SECONDS.toMillis(30);
        long secondSessionPauseTimeMillis =
                secondSessionStartTimeMillis + TimeUnit.SECONDS.toMillis(20);
        long thirdSessionStartTimeMillis = secondSessionPauseTimeMillis + TimeUnit.DAYS.toMillis(3);

        // test
        mockExtensionApi.simulateComingEvent(createStartEvent(null, firstSessionStartTimeMillis));
        mockExtensionApi.simulateComingEvent(createPauseEvent(firstSessionPauseTimeMillis));
        mockExtensionApi.resetDispatchedEventAndCreatedSharedState();

        mockDeviceInfoService.applicationVersion = "1.2";
        mockExtensionApi.simulateComingEvent(createStartEvent(null, secondSessionStartTimeMillis));
        mockExtensionApi.simulateComingEvent(createPauseEvent(secondSessionPauseTimeMillis));
        mockExtensionApi.resetDispatchedEventAndCreatedSharedState();

        mockExtensionApi.simulateComingEvent(createStartEvent(null, thirdSessionStartTimeMillis));

        // verify
        Map<String, String> secondSessionStartResponseEventContextData =
                (Map<String, String>)
                        mockExtensionApi
                                .dispatchedEvents
                                .get(0)
                                .getEventData()
                                .get(LIFECYCLE_CONTEXT_DATA);
        assertEquals("3", secondSessionStartResponseEventContextData.get(DAYS_SINCE_LAST_UPGRADE));
        Map<String, String> secondSessionStartSharedStateContextData =
                (Map<String, String>)
                        mockExtensionApi.createdSharedState.get(0).get(LIFECYCLE_CONTEXT_DATA);
        assertEquals("3", secondSessionStartSharedStateContextData.get(DAYS_SINCE_LAST_UPGRADE));
    }

    @Test
    public void
            testLifecycle__When__SecondLaunch_OneMonthAfterInstall__Then__MonthlyEngUserEvent() {
        // setup
        long firstSessionStartTime = currentTimestampMillis;
        long firstSessionPauseTime = firstSessionStartTime + TimeUnit.SECONDS.toMillis(30);
        long secondSessionStartTime = firstSessionPauseTime + TimeUnit.DAYS.toMillis(30);

        // test
        mockExtensionApi.simulateComingEvent(createStartEvent(null, firstSessionStartTime));
        mockExtensionApi.simulateComingEvent(createPauseEvent(firstSessionPauseTime));
        mockExtensionApi.resetDispatchedEventAndCreatedSharedState();
        mockExtensionApi.simulateComingEvent(createStartEvent(null, secondSessionStartTime));

        // verify
        Map<String, String> secondSessionStartResponseEventContextData =
                (Map<String, String>)
                        mockExtensionApi
                                .dispatchedEvents
                                .get(0)
                                .getEventData()
                                .get(LIFECYCLE_CONTEXT_DATA);
        assertEquals(
                "MonthlyEngUserEvent",
                secondSessionStartResponseEventContextData.get(MONTHLY_ENGAGED_EVENT));
        Map<String, String> secondSessionStartSharedStateContextData =
                (Map<String, String>)
                        mockExtensionApi.createdSharedState.get(0).get(LIFECYCLE_CONTEXT_DATA);
        assertEquals(
                "MonthlyEngUserEvent",
                secondSessionStartSharedStateContextData.get(MONTHLY_ENGAGED_EVENT));
    }

    @Test
    public void
            testLifecycle__When__ForceCloseAndRestartWithinSessionTimeout_ThenRestorePreviousSession() {
        // setup
        long firstSessionStartTime = currentTimestampMillis;
        long firstSessionPauseTime = firstSessionStartTime + 100;
        long secondSessionStartTime = firstSessionPauseTime + 10;

        Map<String, Object> configurationMap = new HashMap<>();
        configurationMap.put(LIFECYCLE_CONFIG_SESSION_TIMEOUT, 30L);

        TestableExtensionApi mockExtensionApi2 = new TestableExtensionApi();
        mockExtensionApi2.ignoreEvent(EventType.LIFECYCLE, EventSource.APPLICATION_CLOSE);
        mockExtensionApi2.ignoreEvent(EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH);
        mockExtensionApi2.simulateSharedState(
                "com.adobe.module.configuration", SharedStateStatus.SET, configurationMap);

        // test
        mockExtensionApi.simulateComingEvent(createStartEvent(null, firstSessionStartTime));
        mockExtensionApi.simulateComingEvent(createPauseEvent(firstSessionPauseTime));

        LifecycleExtension lifecycleSession2 =
                new LifecycleExtension(
                        mockExtensionApi2, lifecycleDataStore, mockDeviceInfoService);
        lifecycleSession2.onRegistered();
        mockExtensionApi2.simulateComingEvent(createStartEvent(null, secondSessionStartTime));

        // verify second session start does not dispatch response event
        assertEquals(0, mockExtensionApi2.dispatchedEvents.size());

        // verify second session start updates shared state
        assertEquals(2, mockExtensionApi2.createdSharedState.size());

        Map<String, String> expectedContextData =
                new HashMap<String, String>() {
                    {
                        put(INSTALL_EVENT, "InstallEvent");
                        put(LAUNCH_EVENT, "LaunchEvent");
                        put(INSTALL_DATE, dayMonthYearDate);
                        put(HOUR_OF_DAY, hourOfDay);
                        put(DAY_OF_WEEK, dayOfWeek);
                        put(LAUNCHES, "1");
                        put(OPERATING_SYSTEM, "TEST_OS 5.55");
                        put(LOCALE, "en-US");
                        put(DEVICE_RESOLUTION, "100x100");
                        put(CARRIER_NAME, "TEST_CARRIER");
                        put(DEVICE_NAME, "deviceName");
                        put(APP_ID, "TEST_APPLICATION_NAME 1.1 (12345)");
                        put(RUN_MODE, "APPLICATION");
                        put(DAILY_ENGAGED_EVENT, "DailyEngUserEvent");
                        put(MONTHLY_ENGAGED_EVENT, "MonthlyEngUserEvent");
                    }
                };
        Map<String, String> secondSessionStartResponseEventContextData =
                (Map<String, String>)
                        mockExtensionApi2.createdSharedState.get(1).get(LIFECYCLE_CONTEXT_DATA);
        assertEquals(expectedContextData, secondSessionStartResponseEventContextData);
    }
}
