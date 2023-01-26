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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LifecycleV2FunctionalTest {

    private TestableExtensionApi mockExtensionApi;
    private MockDeviceInfoService mockDeviceInfoService;
    private NamedCollection lifecycleDataStore;

    private static final String DATA = "data";
    private static final String XDM = "xdm";
    private static final String DATA_STORE_NAME = "AdobeMobile_Lifecycle";
    private static final String LIFECYCLE_CONFIG_SESSION_TIMEOUT = "lifecycle.sessionTimeout";

    private long currentTimestampMillis;

    private final Map<String, Object> expectedEnvironmentInfo = new HashMap<>();
    private final Map<String, Object> expectedDeviceInfo = new HashMap<>();

    static final int WAIT_EVENT_TIMEOUT_MS = 2000;

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
        mockExtensionApi.ignoreEvent(EventType.LIFECYCLE, EventSource.RESPONSE_CONTENT);
        lifecycleDataStore.removeAll();
        initTimestamps();

        Map<String, Object> configurationMap = new HashMap<>();
        configurationMap.put(LIFECYCLE_CONFIG_SESSION_TIMEOUT, 30L);
        mockExtensionApi.simulateSharedState(
                "com.adobe.module.configuration", SharedStateStatus.SET, configurationMap);

        expectedEnvironmentInfo.put("carrier", "TEST_CARRIER");
        expectedEnvironmentInfo.put("operatingSystemVersion", "5.55");
        expectedEnvironmentInfo.put("operatingSystem", "TEST_OS");
        expectedEnvironmentInfo.put("type", "application");
        Map<String, Object> localeMap = new HashMap<>();
        localeMap.put("language", "en-US");
        expectedEnvironmentInfo.put("_dc", localeMap);

        expectedDeviceInfo.put("manufacturer", "Android");
        expectedDeviceInfo.put("model", "deviceName");
        expectedDeviceInfo.put("modelNumber", "TEST_PLATFORM");
        expectedDeviceInfo.put("type", "mobile");
        expectedDeviceInfo.put("screenHeight", 100);
        expectedDeviceInfo.put("screenWidth", 100);
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
        mockDeviceInfoService.deviceManufacturer = "Android";
        mockDeviceInfoService.applicationPackageName = "TEST_PACKAGE_NAME";
    }

    private void initTimestamps() {
        currentTimestampMillis = System.currentTimeMillis();
    }

    @Test
    public void
            testLifecycleV2__When__Start__Then__DispatchLifecycleApplicationLaunchEvent_When__WithFreeFormData()
                    throws InterruptedException {
        // setup
        setExpectationEvent(
                EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH, 1, mockExtensionApi);

        Map<String, String> expectedFreeFormData = new HashMap<>();
        expectedFreeFormData.put("key1", "val1");
        expectedFreeFormData.put("key2", "val2");

        // test
        long sessionStartTimeInMillis = currentTimestampMillis;
        mockExtensionApi.simulateComingEvent(
                createStartEvent(expectedFreeFormData, sessionStartTimeInMillis));

        // verify
        assertExpectedEvents(mockExtensionApi);

        Map<String, Object> expectedApplicationInfo = new HashMap<>();
        expectedApplicationInfo.put("name", "TEST_APPLICATION_NAME");
        expectedApplicationInfo.put("version", "1.1 (12345)");
        expectedApplicationInfo.put("isInstall", true);
        expectedApplicationInfo.put("isLaunch", true);
        expectedApplicationInfo.put("id", "TEST_PACKAGE_NAME");

        Map<String, Object> expectedXDMData =
                new HashMap<String, Object>() {
                    {
                        put("environment", expectedEnvironmentInfo);
                        put("device", expectedDeviceInfo);
                        put("application", expectedApplicationInfo);
                        put(
                                "timestamp",
                                LifecycleUtil.dateTimeISO8601String(
                                        new Date(sessionStartTimeInMillis)));
                        put("eventType", "application.launch");
                    }
                };
        Map<String, Object> expectedEventData =
                new HashMap<String, Object>() {
                    {
                        put(XDM, expectedXDMData);
                        put(DATA, expectedFreeFormData);
                    }
                };
        Event sessionStartApplicationLaunchEvent = mockExtensionApi.dispatchedEvents.get(0);
        assertEquals(
                "Application Launch (Foreground)", sessionStartApplicationLaunchEvent.getName());
        assertEquals(EventType.LIFECYCLE, sessionStartApplicationLaunchEvent.getType());
        assertEquals(
                EventSource.APPLICATION_LAUNCH, sessionStartApplicationLaunchEvent.getSource());
        assertEquals(expectedEventData, sessionStartApplicationLaunchEvent.getEventData());
    }

    @Test
    public void testLifecycleV2__When__Pause__Then__DispatchLifecycleApplicationClose()
            throws InterruptedException {
        // setup
        setExpectationEvent(
                EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH, 1, mockExtensionApi);
        setExpectationEvent(
                EventType.LIFECYCLE, EventSource.APPLICATION_CLOSE, 1, mockExtensionApi);

        // test
        mockExtensionApi.simulateComingEvent(createStartEvent(null, currentTimestampMillis));
        long sessionPauseTimeInMillis = currentTimestampMillis + 1000;
        mockExtensionApi.simulateComingEvent(createPauseEvent(sessionPauseTimeInMillis));

        // verify
        assertExpectedEvents(mockExtensionApi);

        Map<String, Object> expectedApplicationInfo = new HashMap<>();
        expectedApplicationInfo.put("closeType", "close");
        expectedApplicationInfo.put("isClose", true);
        expectedApplicationInfo.put("sessionLength", 1);

        Map<String, Object> expectedXDMData =
                new HashMap<String, Object>() {
                    {
                        put("application", expectedApplicationInfo);
                        put(
                                "timestamp",
                                LifecycleUtil.dateTimeISO8601String(
                                        new Date(sessionPauseTimeInMillis)));
                        put("eventType", "application.close");
                    }
                };
        Map<String, Object> expectedEventData =
                new HashMap<String, Object>() {
                    {
                        put(XDM, expectedXDMData);
                    }
                };
        Event sessionPauseApplicationCloseEvent = mockExtensionApi.dispatchedEvents.get(1);
        assertEquals("Application Close (Background)", sessionPauseApplicationCloseEvent.getName());
        assertEquals(EventType.LIFECYCLE, sessionPauseApplicationCloseEvent.getType());
        assertEquals(EventSource.APPLICATION_CLOSE, sessionPauseApplicationCloseEvent.getSource());
        assertEquals(expectedEventData, sessionPauseApplicationCloseEvent.getEventData());
    }

    @Test
    public void
            testLifecycleV2__When__SecondLaunch_VersionNumberChanged__Then__GetApplicationLaunchEvent__withIsUpgradeTrue()
                    throws InterruptedException {
        // setup
        // both session starts dispatch application launch event
        setExpectationEvent(
                EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH, 2, mockExtensionApi);
        // session pauses dispatches application close event
        setExpectationEvent(
                EventType.LIFECYCLE, EventSource.APPLICATION_CLOSE, 1, mockExtensionApi);

        // test
        mockExtensionApi.simulateComingEvent(createStartEvent(null, currentTimestampMillis));
        mockExtensionApi.simulateComingEvent(
                createPauseEvent(currentTimestampMillis + TimeUnit.SECONDS.toMillis(10)));

        sleep(1000);
        mockDeviceInfoService.applicationVersion = "1.2";

        long secondSessionStartTimeInMillis = currentTimestampMillis + TimeUnit.DAYS.toMillis(1);
        mockExtensionApi.simulateComingEvent(
                createStartEvent(null, secondSessionStartTimeInMillis));

        // verify
        assertExpectedEvents(mockExtensionApi);

        Map<String, Object> expectedApplicationInfo = new HashMap<>();
        expectedApplicationInfo.put("name", "TEST_APPLICATION_NAME");
        expectedApplicationInfo.put("version", "1.2 (12345)");
        expectedApplicationInfo.put("isUpgrade", true);
        expectedApplicationInfo.put("isLaunch", true);
        expectedApplicationInfo.put("id", "TEST_PACKAGE_NAME");

        Map<String, Object> expectedXDMData =
                new HashMap<String, Object>() {
                    {
                        put("environment", expectedEnvironmentInfo);
                        put("device", expectedDeviceInfo);
                        put("application", expectedApplicationInfo);
                        put(
                                "timestamp",
                                LifecycleUtil.dateTimeISO8601String(
                                        new Date(secondSessionStartTimeInMillis)));
                        put("eventType", "application.launch");
                    }
                };
        Map<String, Object> expectedEventData =
                new HashMap<String, Object>() {
                    {
                        put(XDM, expectedXDMData);
                    }
                };

        Event secondSessionStartApplicationLaunchEvent = mockExtensionApi.dispatchedEvents.get(2);
        assertEquals(
                "Application Launch (Foreground)",
                secondSessionStartApplicationLaunchEvent.getName());
        assertEquals(EventType.LIFECYCLE, secondSessionStartApplicationLaunchEvent.getType());
        assertEquals(
                EventSource.APPLICATION_LAUNCH,
                secondSessionStartApplicationLaunchEvent.getSource());
        assertEquals(expectedEventData, secondSessionStartApplicationLaunchEvent.getEventData());
    }

    @Test
    public void
            testLifecycleV2__When__SecondLaunch_VersionNumberNotChanged__Then__GetApplicationLaunchEvent()
                    throws InterruptedException {
        // setup
        // both session starts dispatch application launch event
        setExpectationEvent(
                EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH, 2, mockExtensionApi);
        // session pauses dispatches application close event
        setExpectationEvent(
                EventType.LIFECYCLE, EventSource.APPLICATION_CLOSE, 1, mockExtensionApi);

        // test
        mockExtensionApi.simulateComingEvent(createStartEvent(null, currentTimestampMillis));
        mockExtensionApi.simulateComingEvent(
                createPauseEvent(currentTimestampMillis + TimeUnit.SECONDS.toMillis(10)));

        sleep(1000);

        long secondSessionStartTimeInMillis = currentTimestampMillis + TimeUnit.DAYS.toMillis(1);
        mockExtensionApi.simulateComingEvent(
                createStartEvent(null, secondSessionStartTimeInMillis));

        // verify
        assertExpectedEvents(mockExtensionApi);

        Map<String, Object> expectedApplicationInfo = new HashMap<>();
        expectedApplicationInfo.put("name", "TEST_APPLICATION_NAME");
        expectedApplicationInfo.put("version", "1.1 (12345)");
        expectedApplicationInfo.put("isLaunch", true);
        expectedApplicationInfo.put("id", "TEST_PACKAGE_NAME");

        Map<String, Object> expectedXDMData =
                new HashMap<String, Object>() {
                    {
                        put("environment", expectedEnvironmentInfo);
                        put("device", expectedDeviceInfo);
                        put("application", expectedApplicationInfo);
                        put(
                                "timestamp",
                                LifecycleUtil.dateTimeISO8601String(
                                        new Date(secondSessionStartTimeInMillis)));
                        put("eventType", "application.launch");
                    }
                };
        Map<String, Object> expectedEventData =
                new HashMap<String, Object>() {
                    {
                        put(XDM, expectedXDMData);
                    }
                };
        Event secondSessionStartApplicationLaunchEvent = mockExtensionApi.dispatchedEvents.get(2);
        assertEquals(
                "Application Launch (Foreground)",
                secondSessionStartApplicationLaunchEvent.getName());
        assertEquals(EventType.LIFECYCLE, secondSessionStartApplicationLaunchEvent.getType());
        assertEquals(
                EventSource.APPLICATION_LAUNCH,
                secondSessionStartApplicationLaunchEvent.getSource());
        assertEquals(expectedEventData, secondSessionStartApplicationLaunchEvent.getEventData());
    }

    @Test
    public void testLifecycleV2__When__Crash__Then__withCloseTypeUnknown()
            throws InterruptedException {
        // setup
        // first session start dispatches application launch event
        setExpectationEvent(
                EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH, 1, mockExtensionApi);

        // test
        long firstSessionStartTimeInMillis = currentTimestampMillis;
        mockExtensionApi.simulateComingEvent(createStartEvent(null, firstSessionStartTimeInMillis));

        TestableExtensionApi mockExtensionApi2 = new TestableExtensionApi();
        mockExtensionApi2.ignoreEvent(EventType.LIFECYCLE, EventSource.RESPONSE_CONTENT);
        Map<String, Object> configurationMap = new HashMap<>();
        configurationMap.put(LIFECYCLE_CONFIG_SESSION_TIMEOUT, 30L);
        mockExtensionApi2.simulateSharedState(
                "com.adobe.module.configuration", SharedStateStatus.SET, configurationMap);
        LifecycleExtension lifecycleSession2 = new LifecycleExtension(mockExtensionApi2);
        lifecycleSession2.onRegistered();

        // second session start after crash dispatches application close event
        setExpectationEvent(
                EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH, 1, mockExtensionApi2);
        long secondSessionStartTimeInMillis = currentTimestampMillis;
        mockExtensionApi2.simulateComingEvent(
                createStartEvent(null, secondSessionStartTimeInMillis));

        // verify
        assertExpectedEvents(mockExtensionApi);
        assertExpectedEvents(mockExtensionApi2);

        Map<String, Object> expectedApplicationInfo = new HashMap<>();
        expectedApplicationInfo.put("closeType", "unknown");
        expectedApplicationInfo.put("isClose", true);
        expectedApplicationInfo.put("sessionLength", 2);

        Map<String, Object> expectedXDMData =
                new HashMap<String, Object>() {
                    {
                        put("application", expectedApplicationInfo);
                        put(
                                "timestamp",
                                LifecycleUtil.dateTimeISO8601String(
                                        new Date(
                                                firstSessionStartTimeInMillis
                                                        + LifecycleV2Constants
                                                                .CACHE_TIMEOUT_MILLIS)));
                        put("eventType", "application.close");
                    }
                };
        Map<String, Object> expectedEventData =
                new HashMap<String, Object>() {
                    {
                        put(XDM, expectedXDMData);
                    }
                };
        Event secondSessionStartApplicationCloseEvent = mockExtensionApi2.dispatchedEvents.get(0);
        Map<String, Object> xdm =
                (Map<String, Object>)
                        secondSessionStartApplicationCloseEvent.getEventData().get(XDM);
        assertEquals(
                "Application Close (Background)",
                secondSessionStartApplicationCloseEvent.getName());
        assertEquals(EventType.LIFECYCLE, secondSessionStartApplicationCloseEvent.getType());
        assertEquals(
                EventSource.APPLICATION_CLOSE, secondSessionStartApplicationCloseEvent.getSource());
        assertEquals(expectedEventData, secondSessionStartApplicationCloseEvent.getEventData());
    }

    @Test
    public void
            testLifecycleV2__When__CrashWithCloseTimestampMissing__Then__BackdateToStartTimestampMinusOneSecond()
                    throws InterruptedException {
        // setup
        // first session start dispatches application launch event
        setExpectationEvent(
                EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH, 1, mockExtensionApi);

        // test
        mockExtensionApi.simulateComingEvent(createStartEvent(null, currentTimestampMillis));

        lifecycleDataStore.remove(LifecycleV2Constants.DataStoreKeys.APP_CLOSE_TIMESTAMP_MILLIS);

        TestableExtensionApi mockExtensionApi2 = new TestableExtensionApi();
        mockExtensionApi2.ignoreEvent(EventType.LIFECYCLE, EventSource.RESPONSE_CONTENT);
        Map<String, Object> configurationMap = new HashMap<>();
        configurationMap.put(LIFECYCLE_CONFIG_SESSION_TIMEOUT, 30L);
        mockExtensionApi2.simulateSharedState(
                "com.adobe.module.configuration", SharedStateStatus.SET, configurationMap);
        LifecycleExtension lifecycleSession2 = new LifecycleExtension(mockExtensionApi2);
        lifecycleSession2.onRegistered();

        // second session start after crash dispatches application close event
        setExpectationEvent(
                EventType.LIFECYCLE, EventSource.APPLICATION_LAUNCH, 1, mockExtensionApi2);
        Event startEvent2 = createStartEvent(null, currentTimestampMillis);
        mockExtensionApi2.simulateComingEvent(startEvent2);

        // verify
        assertExpectedEvents(mockExtensionApi);
        assertExpectedEvents(mockExtensionApi2);

        Map<String, Object> expectedApplicationInfo = new HashMap<>();
        expectedApplicationInfo.put("closeType", "unknown");
        expectedApplicationInfo.put("isClose", true);

        // if no close timestamp was persisted, backdate this event to start timestamp - 1 second
        Map<String, Object> expectedXDMData =
                new HashMap<String, Object>() {
                    {
                        put("application", expectedApplicationInfo);
                        put(
                                "timestamp",
                                LifecycleUtil.dateTimeISO8601String(
                                        new Date(startEvent2.getTimestamp() - 1000)));
                        put("eventType", "application.close");
                    }
                };
        Map<String, Object> expectedEventData =
                new HashMap<String, Object>() {
                    {
                        put(XDM, expectedXDMData);
                    }
                };

        Event secondSessionStartApplicationCloseEvent = mockExtensionApi2.dispatchedEvents.get(0);
        assertEquals(
                "Application Close (Background)",
                secondSessionStartApplicationCloseEvent.getName());
        assertEquals(EventType.LIFECYCLE, secondSessionStartApplicationCloseEvent.getType());
        assertEquals(
                EventSource.APPLICATION_CLOSE, secondSessionStartApplicationCloseEvent.getSource());
        assertEquals(expectedEventData, secondSessionStartApplicationCloseEvent.getEventData());
    }

    /**
     * Asserts if all the expected events were received and fails if an unexpected event was seen.
     *
     * @throws InterruptedException
     */
    private void assertExpectedEvents(final TestableExtensionApi mockExtensionApi)
            throws InterruptedException {
        Map<TestableExtensionApi.EventSpec, CountDownLatch> expectedEvents =
                mockExtensionApi.getExpectedEvents();

        if (expectedEvents.isEmpty()) {
            fail(
                    "There are no event expectations set, use this API after calling"
                            + " setExpectationEvent");
            return;
        }

        for (Map.Entry<TestableExtensionApi.EventSpec, CountDownLatch> expected :
                expectedEvents.entrySet()) {
            boolean awaitResult =
                    expected.getValue().await(WAIT_EVENT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertTrue(
                    "Timed out waiting for event type "
                            + expected.getKey().type
                            + " and source "
                            + expected.getKey().source,
                    awaitResult);
            assertEquals(
                    "Failed to receive as many events as expected",
                    0,
                    expected.getValue().getCount());
        }
    }

    /**
     * Sets an expectation for a specific event type and source and how many times the event should
     * be dispatched.
     *
     * @param type the event type
     * @param source the event source
     * @param count the expected number of times the event is dispatched
     * @throws IllegalArgumentException if {@code count} is less than 1
     */
    private void setExpectationEvent(
            final String type,
            final String source,
            final int count,
            final TestableExtensionApi mockExtensionApi) {
        if (count < 1) {
            throw new IllegalArgumentException("Cannot set expectation event count less than 1!");
        }

        mockExtensionApi.setExpectedEvent(type, source, count);
    }

    /**
     * Pause test execution for the given {@code milliseconds}
     *
     * @param milliseconds the time to sleep the current thread.
     */
    public static void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
