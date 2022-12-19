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

import static com.adobe.marketing.mobile.LifecycleEventGeneratorTestHelper.createLifecycleEvent;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.ExtensionApi;
import com.adobe.marketing.mobile.SharedStateResolution;
import com.adobe.marketing.mobile.SharedStateResult;
import com.adobe.marketing.mobile.SharedStateStatus;
import com.adobe.marketing.mobile.services.DeviceInforming;
import com.adobe.marketing.mobile.services.NamedCollection;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class LifecycleV2ExtensionTest {

    private long currentTimestampInMilliseconds;
    private long timestampTenMinEarlierInMilliseconds;
    private long timestampOneHourEarlierInMilliseconds;
    private long timestampOneDayEarlierMilliseconds;
    private LifecycleV2Extension lifecycleV2;

    @Mock private ExtensionApi extensionApi;

    @Mock private LifecycleV2MetricsBuilder mockBuilder;

    @Mock private NamedCollection lifecycleDataStore;

    @Mock private DeviceInforming mockDeviceInfoService;

    private static final String DATASTORE_KEY_INSTALL_DATE = "InstallDate";
    private static final String DATASTORE_KEY_APP_START_TIMESTAMP_MILLIS =
            "v2AppStartTimestampMillis";
    private static final String DATASTORE_KEY_APP_PAUSE_TIMESTAMP_MILLIS =
            "v2AppPauseTimestampMillis";
    private static final String DATASTORE_KEY_APP_CLOSE_TIMESTAMP_MILLIS =
            "v2AppCloseTimestampMillis";
    private static final String DATASTORE_KEY_LAST_APP_VERSION = "v2LastAppVersion";
    private static final String EVENT_DATA_KEY_ADDITIONAL_CONTEXT_DATA = "additionalcontextdata";

    private static final long STATE_UPDATE_TIMEOUT_MILLIS = 2000;
    private static final long CLOSE_TIMESTAMP_OFFSET_MILLIS = 2000;

    private static final String CONFIGURATION_MODULE_NAME = "com.adobe.module.configuration";
    private static final String LIFECYCLE_CONFIG_SESSION_TIMEOUT = "lifecycle.sessionTimeout";

    public static final String TYPE_LIFECYCLE = "com.adobe.eventType.lifecycle";
    public static final String TYPE_REQUEST_CONTENT = "com.adobe.eventSource.requestContent";

    @Before
    public void beforeEach() {
        Map<String, Object> configurationSharedState = new HashMap<>();
        configurationSharedState.put(LIFECYCLE_CONFIG_SESSION_TIMEOUT, 200L);
        when(extensionApi.getSharedState(
                        eq(CONFIGURATION_MODULE_NAME),
                        any(),
                        eq(false),
                        eq(SharedStateResolution.ANY)))
                .thenReturn(new SharedStateResult(SharedStateStatus.SET, configurationSharedState));
        LifecycleTestHelper.initDeviceInfoService(mockDeviceInfoService);

        lifecycleV2 =
                new LifecycleV2Extension(lifecycleDataStore, mockDeviceInfoService, extensionApi);

        initTimestamps();
    }

    private void initTimestamps() {
        currentTimestampInMilliseconds = System.currentTimeMillis();
        timestampTenMinEarlierInMilliseconds =
                currentTimestampInMilliseconds - TimeUnit.MINUTES.toMillis(10000);
        timestampOneHourEarlierInMilliseconds =
                currentTimestampInMilliseconds - TimeUnit.HOURS.toMillis(1000);
        timestampOneDayEarlierMilliseconds =
                currentTimestampInMilliseconds - TimeUnit.DAYS.toMillis(1000);
    }

    // ------------ Test XDM related methods ------------

    @Test
    public void testStart_happy_regularLaunch() {
        mockPersistence(
                timestampOneHourEarlierInMilliseconds,
                timestampTenMinEarlierInMilliseconds,
                timestampTenMinEarlierInMilliseconds,
                false);
        lifecycleV2 =
                new LifecycleV2Extension(
                        lifecycleDataStore, mockDeviceInfoService, mockBuilder, extensionApi);

        Map<String, String> additionalContextData = new HashMap<>();
        additionalContextData.put("TEST_KEY1", "TEXT_VAL1");

        Map<String, Object> eventData = new HashMap<>();
        eventData.put(EVENT_DATA_KEY_ADDITIONAL_CONTEXT_DATA, additionalContextData);

        Event testEvent = createLifecycleEvent(eventData, currentTimestampInMilliseconds);
        lifecycleV2.start(testEvent, false);

        ArgumentCaptor<Long> appStartTimestampCaptor = ArgumentCaptor.forClass(Long.class);
        verify(lifecycleDataStore, times(1))
                .setLong(
                        eq(DATASTORE_KEY_APP_START_TIMESTAMP_MILLIS),
                        appStartTimestampCaptor.capture());
        assertEquals(
                currentTimestampInMilliseconds, appStartTimestampCaptor.getValue().longValue());

        ArgumentCaptor<Long> launchTimestampCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Boolean> isInstallCaptor = ArgumentCaptor.forClass(Boolean.class);
        ArgumentCaptor<Boolean> isUpgradeCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(mockBuilder, times(1))
                .buildAppLaunchXDMData(
                        launchTimestampCaptor.capture(),
                        isInstallCaptor.capture(),
                        isUpgradeCaptor.capture());
        assertEquals(currentTimestampInMilliseconds, launchTimestampCaptor.getValue().longValue());
        assertFalse(isInstallCaptor.getValue());
        assertFalse(isUpgradeCaptor.getValue());

        verify(mockBuilder, never())
                .buildAppCloseXDMData(anyLong(), anyLong(), anyLong(), anyBoolean());
    }

    @Test
    public void testStart_consecutiveStartEvents_updatesOnlyFirstTime() {
        mockPersistence(
                timestampOneHourEarlierInMilliseconds,
                timestampTenMinEarlierInMilliseconds,
                timestampTenMinEarlierInMilliseconds,
                true);
        lifecycleV2 =
                new LifecycleV2Extension(
                        lifecycleDataStore, mockDeviceInfoService, mockBuilder, extensionApi);

        Map<String, String> additionalContextData = new HashMap<String, String>();
        additionalContextData.put("TEST_KEY1", "TEXT_VAL1");

        Map<String, Object> eventData = new HashMap<>();
        eventData.put(EVENT_DATA_KEY_ADDITIONAL_CONTEXT_DATA, additionalContextData);

        long timestampTwoSecondsEarlierMilliseconds = currentTimestampInMilliseconds - 2000;

        Event testEvent1 = createLifecycleEvent(eventData, timestampTwoSecondsEarlierMilliseconds);
        lifecycleV2.start(testEvent1, false);

        Event testEvent2 = createLifecycleEvent(eventData, currentTimestampInMilliseconds);
        lifecycleV2.start(testEvent2, false);

        ArgumentCaptor<Long> appStartTimestampCaptor = ArgumentCaptor.forClass(Long.class);
        verify(lifecycleDataStore, times(1))
                .setLong(
                        eq(DATASTORE_KEY_APP_START_TIMESTAMP_MILLIS),
                        appStartTimestampCaptor.capture());
        assertEquals(
                timestampTwoSecondsEarlierMilliseconds,
                appStartTimestampCaptor.getValue().longValue());

        ArgumentCaptor<Long> launchTimestampCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Boolean> isInstallCaptor = ArgumentCaptor.forClass(Boolean.class);
        ArgumentCaptor<Boolean> isUpgradeCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(mockBuilder, times(1))
                .buildAppLaunchXDMData(
                        launchTimestampCaptor.capture(),
                        isInstallCaptor.capture(),
                        isUpgradeCaptor.capture());
        assertEquals(
                timestampTwoSecondsEarlierMilliseconds,
                launchTimestampCaptor.getValue().longValue());
        assertFalse(isInstallCaptor.getValue());
        assertTrue(isUpgradeCaptor.getValue());

        verify(mockBuilder, never())
                .buildAppCloseXDMData(anyLong(), anyLong(), anyLong(), anyBoolean());
    }

    @Test
    public void testStart_onInstall_doesNotBuildCloseEvent() {
        lifecycleV2 =
                new LifecycleV2Extension(
                        lifecycleDataStore, mockDeviceInfoService, mockBuilder, extensionApi);

        Map<String, String> additionalContextData = new HashMap<>();
        additionalContextData.put("TEST_KEY1", "TEXT_VAL1");

        Map<String, Object> eventData = new HashMap<>();
        eventData.put(EVENT_DATA_KEY_ADDITIONAL_CONTEXT_DATA, additionalContextData);

        Event testEvent = createLifecycleEvent(eventData, currentTimestampInMilliseconds);
        lifecycleV2.start(testEvent, true);

        ArgumentCaptor<Long> appStartTimestampCaptor = ArgumentCaptor.forClass(Long.class);
        verify(lifecycleDataStore, times(1))
                .setLong(
                        eq(DATASTORE_KEY_APP_START_TIMESTAMP_MILLIS),
                        appStartTimestampCaptor.capture());
        assertEquals(
                currentTimestampInMilliseconds, appStartTimestampCaptor.getValue().longValue());

        verify(mockBuilder, times(1)).buildAppLaunchXDMData(anyLong(), anyBoolean(), anyBoolean());

        verify(mockBuilder, never())
                .buildAppCloseXDMData(anyLong(), anyLong(), anyLong(), anyBoolean());
    }

    @Test
    public void testStart_onCloseUnknown_missingPause_buildsCloseEvent() {
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_APP_START_TIMESTAMP_MILLIS), anyLong()))
                .thenReturn(timestampOneHourEarlierInMilliseconds);
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_INSTALL_DATE), anyLong()))
                .thenReturn(timestampOneDayEarlierMilliseconds);
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_APP_CLOSE_TIMESTAMP_MILLIS), anyLong()))
                .thenReturn(timestampTenMinEarlierInMilliseconds);

        lifecycleV2 =
                new LifecycleV2Extension(
                        lifecycleDataStore, mockDeviceInfoService, mockBuilder, extensionApi);

        Map<String, String> additionalContextData = new HashMap<>();
        additionalContextData.put("TEST_KEY1", "TEXT_VAL1");

        Map<String, Object> eventData = new HashMap<>();
        eventData.put(EVENT_DATA_KEY_ADDITIONAL_CONTEXT_DATA, additionalContextData);

        Event testEvent = createLifecycleEvent(eventData, currentTimestampInMilliseconds);
        lifecycleV2.start(testEvent, false);

        ArgumentCaptor<Long> appStartTimestampCaptor = ArgumentCaptor.forClass(Long.class);
        verify(lifecycleDataStore, times(1))
                .setLong(
                        eq(DATASTORE_KEY_APP_START_TIMESTAMP_MILLIS),
                        appStartTimestampCaptor.capture());
        assertEquals(
                currentTimestampInMilliseconds, appStartTimestampCaptor.getValue().longValue());

        verify(mockBuilder, times(1)).buildAppLaunchXDMData(anyLong(), anyBoolean(), anyBoolean());

        ArgumentCaptor<Long> launchTimestampCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> closeTimestampCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> fallbackCloseEventTimestampCaptor =
                ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Boolean> isCloseKnownCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(mockBuilder, times(1))
                .buildAppCloseXDMData(
                        launchTimestampCaptor.capture(),
                        closeTimestampCaptor.capture(),
                        fallbackCloseEventTimestampCaptor.capture(),
                        isCloseKnownCaptor.capture());

        assertEquals(
                timestampOneHourEarlierInMilliseconds,
                launchTimestampCaptor.getValue().longValue());
        assertEquals(
                timestampTenMinEarlierInMilliseconds + CLOSE_TIMESTAMP_OFFSET_MILLIS,
                closeTimestampCaptor.getValue().longValue());
        assertEquals(
                currentTimestampInMilliseconds - 1000,
                fallbackCloseEventTimestampCaptor.getValue().longValue()); // backdated start time
        assertEquals(true, isCloseKnownCaptor.getValue());
    }

    @Test
    public void testStart_onCloseUnknown_missingStart_buildsCloseEvent() {
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_APP_PAUSE_TIMESTAMP_MILLIS), anyLong()))
                .thenReturn(timestampTenMinEarlierInMilliseconds);
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_INSTALL_DATE), anyLong()))
                .thenReturn(timestampOneDayEarlierMilliseconds);
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_APP_CLOSE_TIMESTAMP_MILLIS), anyLong()))
                .thenReturn(timestampTenMinEarlierInMilliseconds);

        lifecycleV2 =
                new LifecycleV2Extension(
                        lifecycleDataStore, mockDeviceInfoService, mockBuilder, extensionApi);

        Map<String, String> additionalContextData = new HashMap<>();
        additionalContextData.put("TEST_KEY1", "TEXT_VAL1");

        Map<String, Object> eventData = new HashMap<>();
        eventData.put(EVENT_DATA_KEY_ADDITIONAL_CONTEXT_DATA, additionalContextData);

        Event testEvent = createLifecycleEvent(eventData, currentTimestampInMilliseconds);
        lifecycleV2.start(testEvent, false);

        ArgumentCaptor<Long> appStartTimestampCaptor = ArgumentCaptor.forClass(Long.class);
        verify(lifecycleDataStore, times(1))
                .setLong(
                        eq(DATASTORE_KEY_APP_START_TIMESTAMP_MILLIS),
                        appStartTimestampCaptor.capture());
        assertEquals(
                currentTimestampInMilliseconds, appStartTimestampCaptor.getValue().longValue());

        verify(mockBuilder, times(1)).buildAppLaunchXDMData(anyLong(), anyBoolean(), anyBoolean());

        ArgumentCaptor<Long> launchTimestampCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> closeTimestampCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> fallbackCloseEventTimestampCaptor =
                ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Boolean> isCloseKnownCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(mockBuilder, times(1))
                .buildAppCloseXDMData(
                        launchTimestampCaptor.capture(),
                        closeTimestampCaptor.capture(),
                        fallbackCloseEventTimestampCaptor.capture(),
                        isCloseKnownCaptor.capture());

        assertEquals(0L, launchTimestampCaptor.getValue().longValue());
        assertEquals(
                timestampTenMinEarlierInMilliseconds + CLOSE_TIMESTAMP_OFFSET_MILLIS,
                closeTimestampCaptor.getValue().longValue());
        assertEquals(
                currentTimestampInMilliseconds - 1000,
                fallbackCloseEventTimestampCaptor.getValue().longValue()); // backdated start time
        assertEquals(true, isCloseKnownCaptor.getValue());
    }

    @Test
    public void testStart_onCloseUnknown_startAfterPause_buildsCloseEvent() {
        mockPersistence(
                timestampTenMinEarlierInMilliseconds,
                timestampOneHourEarlierInMilliseconds,
                timestampTenMinEarlierInMilliseconds,
                false);
        lifecycleV2 =
                new LifecycleV2Extension(
                        lifecycleDataStore, mockDeviceInfoService, mockBuilder, extensionApi);

        Map<String, String> additionalContextData = new HashMap<String, String>();
        additionalContextData.put("TEST_KEY1", "TEXT_VAL1");

        Map<String, Object> eventData = new HashMap<>();
        eventData.put(EVENT_DATA_KEY_ADDITIONAL_CONTEXT_DATA, additionalContextData);

        Event testEvent = createLifecycleEvent(eventData, currentTimestampInMilliseconds);
        lifecycleV2.start(testEvent, false);

        ArgumentCaptor<Long> appStartTimestampCaptor = ArgumentCaptor.forClass(Long.class);
        verify(lifecycleDataStore, times(1))
                .setLong(
                        eq(DATASTORE_KEY_APP_START_TIMESTAMP_MILLIS),
                        appStartTimestampCaptor.capture());
        assertEquals(
                currentTimestampInMilliseconds, appStartTimestampCaptor.getValue().longValue());

        verify(mockBuilder, times(1)).buildAppLaunchXDMData(anyLong(), anyBoolean(), anyBoolean());

        ArgumentCaptor<Long> launchTimestampCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> closeTimestampCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> fallbackCloseEventTimestampCaptor =
                ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Boolean> isCloseKnownCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(mockBuilder, times(1))
                .buildAppCloseXDMData(
                        launchTimestampCaptor.capture(),
                        closeTimestampCaptor.capture(),
                        fallbackCloseEventTimestampCaptor.capture(),
                        isCloseKnownCaptor.capture());

        assertEquals(
                timestampTenMinEarlierInMilliseconds, launchTimestampCaptor.getValue().longValue());
        assertEquals(
                timestampTenMinEarlierInMilliseconds + CLOSE_TIMESTAMP_OFFSET_MILLIS,
                closeTimestampCaptor.getValue().longValue());
        assertEquals(
                currentTimestampInMilliseconds - 1000,
                fallbackCloseEventTimestampCaptor.getValue().longValue()); // backdated start time
        assertEquals(true, isCloseKnownCaptor.getValue());
    }

    @Test
    public void testPause_Happy() throws Exception {
        mockPersistence(
                timestampTenMinEarlierInMilliseconds,
                timestampTenMinEarlierInMilliseconds,
                timestampOneDayEarlierMilliseconds,
                false);

        lifecycleV2 =
                new LifecycleV2Extension(
                        lifecycleDataStore, mockDeviceInfoService, mockBuilder, extensionApi);

        Event testEvent = createLifecycleEvent(null, currentTimestampInMilliseconds);
        lifecycleV2.pause(testEvent);

        Thread.sleep(STATE_UPDATE_TIMEOUT_MILLIS + 10);

        ArgumentCaptor<Long> appPauseTimestampCaptor = ArgumentCaptor.forClass(Long.class);
        verify(lifecycleDataStore, times(1))
                .setLong(
                        eq(DATASTORE_KEY_APP_PAUSE_TIMESTAMP_MILLIS),
                        appPauseTimestampCaptor.capture());
        assertEquals(
                currentTimestampInMilliseconds, appPauseTimestampCaptor.getValue().longValue());

        verify(mockBuilder, never()).buildAppLaunchXDMData(anyLong(), anyBoolean(), anyBoolean());

        ArgumentCaptor<Long> launchTimestampCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> closeTimestampCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> fallbackCloseEventTimestampCaptor =
                ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Boolean> isCloseKnownCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(mockBuilder, times(1))
                .buildAppCloseXDMData(
                        launchTimestampCaptor.capture(),
                        closeTimestampCaptor.capture(),
                        fallbackCloseEventTimestampCaptor.capture(),
                        isCloseKnownCaptor.capture());

        assertEquals(
                timestampTenMinEarlierInMilliseconds, launchTimestampCaptor.getValue().longValue());
        assertEquals(currentTimestampInMilliseconds, closeTimestampCaptor.getValue().longValue());
        assertEquals(
                currentTimestampInMilliseconds,
                fallbackCloseEventTimestampCaptor.getValue().longValue()); // backdated start time
        assertEquals(false, isCloseKnownCaptor.getValue());
    }

    @Test
    public void testPause_consecutivePauseEvents_updatesLastTime() throws Exception {
        mockPersistence(
                timestampTenMinEarlierInMilliseconds,
                timestampOneDayEarlierMilliseconds,
                timestampOneDayEarlierMilliseconds,
                false);
        lifecycleV2 =
                new LifecycleV2Extension(
                        lifecycleDataStore, mockDeviceInfoService, mockBuilder, extensionApi);

        long timestampTwoSecondsEarlierMilliseconds = currentTimestampInMilliseconds - 2000;
        Event testEvent1 = createLifecycleEvent(null, timestampTwoSecondsEarlierMilliseconds);
        lifecycleV2.pause(testEvent1);
        Event testEvent2 = createLifecycleEvent(null, currentTimestampInMilliseconds);
        lifecycleV2.pause(testEvent2);

        Thread.sleep(STATE_UPDATE_TIMEOUT_MILLIS + 10);
        ArgumentCaptor<Long> appPauseTimestampCaptor = ArgumentCaptor.forClass(Long.class);
        verify(lifecycleDataStore, times(1))
                .setLong(
                        eq(DATASTORE_KEY_APP_PAUSE_TIMESTAMP_MILLIS),
                        appPauseTimestampCaptor.capture());
        assertEquals(
                currentTimestampInMilliseconds, appPauseTimestampCaptor.getValue().longValue());

        verify(mockBuilder, never()).buildAppLaunchXDMData(anyLong(), anyBoolean(), anyBoolean());

        ArgumentCaptor<Long> launchTimestampCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> closeTimestampCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> fallbackCloseEventTimestampCaptor =
                ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Boolean> isCloseKnownCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(mockBuilder, times(1))
                .buildAppCloseXDMData(
                        launchTimestampCaptor.capture(),
                        closeTimestampCaptor.capture(),
                        fallbackCloseEventTimestampCaptor.capture(),
                        isCloseKnownCaptor.capture());

        assertEquals(
                timestampTenMinEarlierInMilliseconds, launchTimestampCaptor.getValue().longValue());
        assertEquals(currentTimestampInMilliseconds, closeTimestampCaptor.getValue().longValue());
        assertEquals(
                currentTimestampInMilliseconds,
                fallbackCloseEventTimestampCaptor.getValue().longValue()); // backdated start time
        assertEquals(false, isCloseKnownCaptor.getValue());
    }

    @Test
    public void updateLastKnownTimestamp_Happy() {
        Event testEvent = createLifecycleEvent(null, currentTimestampInMilliseconds);
        lifecycleV2.updateLastKnownTimestamp(testEvent);
        ArgumentCaptor<Long> appCloseTimestampCaptor = ArgumentCaptor.forClass(Long.class);
        verify(lifecycleDataStore, times(1))
                .setLong(
                        eq(DATASTORE_KEY_APP_CLOSE_TIMESTAMP_MILLIS),
                        appCloseTimestampCaptor.capture());
        assertEquals(
                currentTimestampInMilliseconds, appCloseTimestampCaptor.getValue().longValue());
    }

    private void mockPersistence(
            final long startTimeSec,
            final long pauseTimeSec,
            final long closeTimeSec,
            final boolean isUpgrade) {
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_APP_START_TIMESTAMP_MILLIS), anyLong()))
                .thenReturn(startTimeSec);
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_APP_PAUSE_TIMESTAMP_MILLIS), anyLong()))
                .thenReturn(pauseTimeSec);
        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_APP_CLOSE_TIMESTAMP_MILLIS), anyLong()))
                .thenReturn(closeTimeSec);

        when(lifecycleDataStore.getLong(eq(DATASTORE_KEY_INSTALL_DATE), anyLong()))
                .thenReturn(timestampOneDayEarlierMilliseconds);
        when(lifecycleDataStore.getString(eq(DATASTORE_KEY_LAST_APP_VERSION), anyString()))
                .thenReturn("1.0");

        if (!isUpgrade) {
            when(lifecycleDataStore.getString(eq(DATASTORE_KEY_LAST_APP_VERSION), anyString()))
                    .thenReturn("1.1");
        }
    }
}
