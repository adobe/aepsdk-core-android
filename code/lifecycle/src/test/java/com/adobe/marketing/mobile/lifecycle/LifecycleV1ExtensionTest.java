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

import static com.adobe.marketing.mobile.LifecycleEventGeneratorTestHelper.createStartEvent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class LifecycleV1ExtensionTest {

    @Mock ExtensionApi extensionApi;

    @Mock NamedCollection lifecycleDataStore;

    @Mock private DeviceInforming mockDeviceInfoService;

    Map<String, Object> configurationSharedState = new HashMap<>();
    private final long currentTimestampInMilliSeconds = System.currentTimeMillis();
    private final long currentTimestampInSeconds =
            TimeUnit.MILLISECONDS.toSeconds(currentTimestampInMilliSeconds);
    private LifecycleV1Extension lifecycleV1Extension;

    private static final String LIFECYCLE_CONFIG_SESSION_TIMEOUT = "lifecycle.sessionTimeout";
    private static final String SESSION_START_TIMESTAMP = "starttimestampmillis";
    private static final String MAX_SESSION_LENGTH = "maxsessionlength";
    private static final long MAX_SESSION_LENGTH_SECONDS = TimeUnit.DAYS.toSeconds(7);
    private static final String LIFECYCLE_CONTEXT_DATA = "lifecyclecontextdata";
    private static final String START_DATE = "SessionStart";

    private static final String IDENTITY_MODULE_NAME = "com.adobe.module.identity";
    private static final String ADVERTISING_IDENTIFIER = "advertisingidentifier";
    private static final String SESSION_EVENT = "sessionevent";
    private static final String LIFECYCLE_START = "start";
    private static final String PREVIOUS_SESSION_PAUSE_TIMESTAMP =
            "previoussessionpausetimestampmillis";
    private static final String PREVIOUS_SESSION_START_TIMESTAMP =
            "previoussessionstarttimestampmillis";

    @Before
    public void beforeEach() {
        configurationSharedState.put(LIFECYCLE_CONFIG_SESSION_TIMEOUT, 200L);
    }

    @Test
    public void testStart_PreviousSessionInfoNull_DefaultStartDate() {
        ArgumentCaptor<Long> startTimeCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Map<String, String>> additionalDataCaptor =
                ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<String> adIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> sessionTimeoutCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Boolean> isInstallCaptor = ArgumentCaptor.forClass(Boolean.class);

        try (MockedConstruction<LifecycleState> ignored =
                mockConstruction(
                        LifecycleState.class,
                        (mock, context) -> {
                            when(mock.start(
                                            startTimeCaptor.capture(),
                                            additionalDataCaptor.capture(),
                                            adIdCaptor.capture(),
                                            sessionTimeoutCaptor.capture(),
                                            isInstallCaptor.capture()))
                                    .thenReturn(null);
                            when(mock.getContextData()).thenReturn(null);
                        })) {
            LifecycleV1Extension lifecycleV1Extension =
                    new LifecycleV1Extension(
                            lifecycleDataStore, mockDeviceInfoService, extensionApi);

            Event lifecycleStartEvent = createStartEvent(null, currentTimestampInMilliSeconds);
            lifecycleV1Extension.start(lifecycleStartEvent, configurationSharedState, true);

            assertEquals(currentTimestampInSeconds, startTimeCaptor.getValue().longValue());
            assertNull(additionalDataCaptor.getValue());
            assertNull(adIdCaptor.getValue());
            assertEquals(200L, sessionTimeoutCaptor.getValue().longValue());
            assertTrue(isInstallCaptor.getValue());

            ArgumentCaptor<Map<String, Object>> lifecycleSharedState =
                    ArgumentCaptor.forClass(Map.class);
            ArgumentCaptor<Event> lifecycleEventCaptor = ArgumentCaptor.forClass(Event.class);
            verify(extensionApi, times(1))
                    .createSharedState(
                            lifecycleSharedState.capture(), lifecycleEventCaptor.capture());
            assertEquals(lifecycleStartEvent, lifecycleEventCaptor.getValue());
            assertEquals(0L, lifecycleSharedState.getValue().get(SESSION_START_TIMESTAMP));
            assertEquals(
                    MAX_SESSION_LENGTH_SECONDS,
                    lifecycleSharedState.getValue().get(MAX_SESSION_LENGTH));
            assertNull(lifecycleSharedState.getValue().get(LIFECYCLE_CONTEXT_DATA));

            verify(extensionApi, never()).dispatch(any());
        }
    }

    @Test
    public void testStart_PreviousSessionInfoNull_StartDateAndLifecycleContextDataSet() {
        ArgumentCaptor<Long> startTimeCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Map<String, String>> additionalDataCaptor =
                ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<String> adIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> sessionTimeoutCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Boolean> isInstallCaptor = ArgumentCaptor.forClass(Boolean.class);
        Map<String, String> contextData = new HashMap<>();
        contextData.put("key", "value");

        try (MockedConstruction<LifecycleState> ignored =
                mockConstruction(
                        LifecycleState.class,
                        (mock, context) -> {
                            when(mock.start(
                                            startTimeCaptor.capture(),
                                            additionalDataCaptor.capture(),
                                            adIdCaptor.capture(),
                                            sessionTimeoutCaptor.capture(),
                                            isInstallCaptor.capture()))
                                    .thenReturn(null);
                            when(mock.getContextData()).thenReturn(contextData);
                        })) {
            when(lifecycleDataStore.getLong(eq(START_DATE), anyLong())).thenReturn(1234L);

            LifecycleV1Extension lifecycleV1Extension =
                    new LifecycleV1Extension(
                            lifecycleDataStore, mockDeviceInfoService, extensionApi);

            Event lifecycleStartEvent = createStartEvent(null, currentTimestampInMilliSeconds);
            lifecycleV1Extension.start(lifecycleStartEvent, configurationSharedState, true);

            ArgumentCaptor<Map<String, Object>> lifecycleSharedState =
                    ArgumentCaptor.forClass(Map.class);
            ArgumentCaptor<Event> lifecycleStartEventCaptor = ArgumentCaptor.forClass(Event.class);
            verify(extensionApi, times(1))
                    .createSharedState(
                            lifecycleSharedState.capture(), lifecycleStartEventCaptor.capture());
            assertEquals(lifecycleStartEvent, lifecycleStartEventCaptor.getValue());
            assertEquals(1234L, lifecycleSharedState.getValue().get(SESSION_START_TIMESTAMP));
            assertEquals(
                    MAX_SESSION_LENGTH_SECONDS,
                    lifecycleSharedState.getValue().get(MAX_SESSION_LENGTH));
            assertEquals(contextData, lifecycleSharedState.getValue().get(LIFECYCLE_CONTEXT_DATA));

            verify(extensionApi, never()).dispatch(any());
        }
    }

    @Test
    public void testStart_PreviousSessionInfoNotNull() {
        ArgumentCaptor<Long> startTimeCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Map<String, String>> additionalDataCaptor =
                ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<String> adIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> sessionTimeoutCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Boolean> isInstallCaptor = ArgumentCaptor.forClass(Boolean.class);
        Map<String, String> contextData = new HashMap<>();
        contextData.put("key", "value");

        try (MockedConstruction<LifecycleState> ignored =
                mockConstruction(
                        LifecycleState.class,
                        (mock, context) -> {
                            LifecycleSession.SessionInfo sessionInfo =
                                    new LifecycleSession.SessionInfo(111L, 222L, false);

                            when(mock.start(
                                            startTimeCaptor.capture(),
                                            additionalDataCaptor.capture(),
                                            adIdCaptor.capture(),
                                            sessionTimeoutCaptor.capture(),
                                            isInstallCaptor.capture()))
                                    .thenReturn(sessionInfo);
                            when(mock.getContextData()).thenReturn(contextData);
                        })) {
            LifecycleV1Extension lifecycleV1Extension =
                    new LifecycleV1Extension(
                            lifecycleDataStore, mockDeviceInfoService, extensionApi);

            Event lifecycleStartEvent = createStartEvent(null, currentTimestampInMilliSeconds);
            lifecycleV1Extension.start(lifecycleStartEvent, configurationSharedState, true);

            ArgumentCaptor<Map<String, Object>> lifecycleSharedState =
                    ArgumentCaptor.forClass(Map.class);
            ArgumentCaptor<Event> lifecycleStartEventCaptor = ArgumentCaptor.forClass(Event.class);
            verify(extensionApi, times(1))
                    .createSharedState(
                            lifecycleSharedState.capture(), lifecycleStartEventCaptor.capture());
            assertEquals(lifecycleStartEvent, lifecycleStartEventCaptor.getValue());
            assertEquals(
                    TimeUnit.MILLISECONDS.toSeconds(currentTimestampInMilliSeconds),
                    lifecycleSharedState.getValue().get(SESSION_START_TIMESTAMP));
            assertEquals(
                    MAX_SESSION_LENGTH_SECONDS,
                    lifecycleSharedState.getValue().get(MAX_SESSION_LENGTH));
            assertEquals(contextData, lifecycleSharedState.getValue().get(LIFECYCLE_CONTEXT_DATA));

            ArgumentCaptor<Event> lifecycleResponseEvent = ArgumentCaptor.forClass(Event.class);
            verify(extensionApi, times(1)).dispatch(lifecycleResponseEvent.capture());
            Map<String, Object> lifecycleResponseEventData =
                    lifecycleResponseEvent.getValue().getEventData();
            assertEquals(contextData, lifecycleResponseEventData.get(LIFECYCLE_CONTEXT_DATA));
            assertEquals(LIFECYCLE_START, lifecycleResponseEventData.get(SESSION_EVENT));
            assertEquals(
                    TimeUnit.MILLISECONDS.toSeconds(currentTimestampInMilliSeconds),
                    lifecycleResponseEventData.get(SESSION_START_TIMESTAMP));
            assertEquals(
                    MAX_SESSION_LENGTH_SECONDS, lifecycleResponseEventData.get(MAX_SESSION_LENGTH));
            assertEquals(111L, lifecycleResponseEventData.get(PREVIOUS_SESSION_START_TIMESTAMP));
            assertEquals(222L, lifecycleResponseEventData.get(PREVIOUS_SESSION_PAUSE_TIMESTAMP));
        }
    }

    @Test
    public void testStart_AdditionalData() {
        ArgumentCaptor<Long> startTimeCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Map<String, String>> additionalDataCaptor =
                ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<String> adIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> sessionTimeoutCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Boolean> isInstallCaptor = ArgumentCaptor.forClass(Boolean.class);

        try (MockedConstruction<LifecycleState> ignored =
                mockConstruction(
                        LifecycleState.class,
                        (mock, context) -> {
                            when(mock.start(
                                            startTimeCaptor.capture(),
                                            additionalDataCaptor.capture(),
                                            adIdCaptor.capture(),
                                            sessionTimeoutCaptor.capture(),
                                            isInstallCaptor.capture()))
                                    .thenReturn(null);
                            when(mock.getContextData()).thenReturn(null);
                        })) {
            LifecycleV1Extension lifecycleV1Extension =
                    new LifecycleV1Extension(
                            lifecycleDataStore, mockDeviceInfoService, extensionApi);

            Map<String, String> additionalContextData = new HashMap<>();
            additionalContextData.put("key", "value");
            Event lifecycleStartEvent =
                    createStartEvent(additionalContextData, currentTimestampInMilliSeconds);
            lifecycleV1Extension.start(lifecycleStartEvent, configurationSharedState, true);

            assertEquals(currentTimestampInSeconds, startTimeCaptor.getValue().longValue());
            assertEquals(additionalContextData, additionalDataCaptor.getValue());
            assertNull(adIdCaptor.getValue());
            assertEquals(200L, sessionTimeoutCaptor.getValue().longValue());
            assertTrue(isInstallCaptor.getValue());
        }
    }

    @Test
    public void testHandleLifecycleRequestEvent_LifecycleStart_IdentitySharedStatePending() {
        ArgumentCaptor<Long> startTimeCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Map<String, String>> additionalDataCaptor =
                ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<String> adIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> sessionTimeoutCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Boolean> isInstallCaptor = ArgumentCaptor.forClass(Boolean.class);

        try (MockedConstruction<LifecycleState> ignored =
                mockConstruction(
                        LifecycleState.class,
                        (mock, context) -> {
                            when(mock.start(
                                            startTimeCaptor.capture(),
                                            additionalDataCaptor.capture(),
                                            adIdCaptor.capture(),
                                            sessionTimeoutCaptor.capture(),
                                            isInstallCaptor.capture()))
                                    .thenReturn(null);
                            when(mock.getContextData()).thenReturn(null);
                        })) {
            when(extensionApi.getSharedState(
                            eq(IDENTITY_MODULE_NAME),
                            any(),
                            eq(false),
                            eq(SharedStateResolution.ANY)))
                    .thenReturn(null);

            LifecycleV1Extension lifecycleV1Extension =
                    new LifecycleV1Extension(
                            lifecycleDataStore, mockDeviceInfoService, extensionApi);

            Event lifecycleStartEvent = createStartEvent(null, currentTimestampInMilliSeconds);
            lifecycleV1Extension.start(lifecycleStartEvent, configurationSharedState, true);

            assertEquals(currentTimestampInSeconds, startTimeCaptor.getValue().longValue());
            assertNull(additionalDataCaptor.getValue());
            assertNull(adIdCaptor.getValue());
            assertEquals(200L, sessionTimeoutCaptor.getValue().longValue());
            assertTrue(isInstallCaptor.getValue());
        }
    }

    @Test
    public void testHandleLifecycleRequestEvent_LifecycleStart_IdentitySharedStateNull() {
        ArgumentCaptor<Long> startTimeCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Map<String, String>> additionalDataCaptor =
                ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<String> adIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> sessionTimeoutCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Boolean> isInstallCaptor = ArgumentCaptor.forClass(Boolean.class);

        try (MockedConstruction<LifecycleState> ignored =
                mockConstruction(
                        LifecycleState.class,
                        (mock, context) -> {
                            when(mock.start(
                                            startTimeCaptor.capture(),
                                            additionalDataCaptor.capture(),
                                            adIdCaptor.capture(),
                                            sessionTimeoutCaptor.capture(),
                                            isInstallCaptor.capture()))
                                    .thenReturn(null);
                            when(mock.getContextData()).thenReturn(null);
                        })) {
            when(extensionApi.getSharedState(
                            eq(IDENTITY_MODULE_NAME),
                            any(),
                            eq(false),
                            eq(SharedStateResolution.ANY)))
                    .thenReturn(new SharedStateResult(SharedStateStatus.PENDING, null));

            LifecycleV1Extension lifecycleV1Extension =
                    new LifecycleV1Extension(
                            lifecycleDataStore, mockDeviceInfoService, extensionApi);

            Event lifecycleStartEvent = createStartEvent(null, currentTimestampInMilliSeconds);
            lifecycleV1Extension.start(lifecycleStartEvent, configurationSharedState, true);

            assertEquals(currentTimestampInSeconds, startTimeCaptor.getValue().longValue());
            assertNull(additionalDataCaptor.getValue());
            assertNull(adIdCaptor.getValue());
            assertEquals(200L, sessionTimeoutCaptor.getValue().longValue());
            assertTrue(isInstallCaptor.getValue());
        }
    }

    @Test
    public void testHandleLifecycleRequestEvent_LifecycleStart_AdIdSet() {
        ArgumentCaptor<Long> startTimeCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Map<String, String>> additionalDataCaptor =
                ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<String> adIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> sessionTimeoutCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Boolean> isInstallCaptor = ArgumentCaptor.forClass(Boolean.class);

        try (MockedConstruction<LifecycleState> ignored =
                mockConstruction(
                        LifecycleState.class,
                        (mock, context) -> {
                            when(mock.start(
                                            startTimeCaptor.capture(),
                                            additionalDataCaptor.capture(),
                                            adIdCaptor.capture(),
                                            sessionTimeoutCaptor.capture(),
                                            isInstallCaptor.capture()))
                                    .thenReturn(null);
                            when(mock.getContextData()).thenReturn(null);
                        })) {
            Map<String, Object> identitySharedState = new HashMap<>();
            identitySharedState.put(ADVERTISING_IDENTIFIER, "testAdid");
            when(extensionApi.getSharedState(
                            eq(IDENTITY_MODULE_NAME),
                            any(),
                            eq(false),
                            eq(SharedStateResolution.ANY)))
                    .thenReturn(new SharedStateResult(SharedStateStatus.SET, identitySharedState));

            LifecycleV1Extension lifecycleV1Extension =
                    new LifecycleV1Extension(
                            lifecycleDataStore, mockDeviceInfoService, extensionApi);

            Event lifecycleStartEvent = createStartEvent(null, currentTimestampInMilliSeconds);
            lifecycleV1Extension.start(lifecycleStartEvent, configurationSharedState, true);

            assertEquals(currentTimestampInSeconds, startTimeCaptor.getValue().longValue());
            assertNull(additionalDataCaptor.getValue());
            assertEquals("testAdid", adIdCaptor.getValue());
            assertEquals(200L, sessionTimeoutCaptor.getValue().longValue());
            assertTrue(isInstallCaptor.getValue());
        }
    }

    @Test
    public void testProcessLifecycleExtensionRegistration() {
        Map<String, String> bootData = new HashMap<>();
        bootData.put("key", "value");
        try (MockedConstruction<LifecycleState> ignored =
                mockConstruction(
                        LifecycleState.class,
                        (mock, context) -> when(mock.computeBootData()).thenReturn(bootData))) {
            LifecycleV1Extension lifecycleV1Extension =
                    new LifecycleV1Extension(
                            lifecycleDataStore, mockDeviceInfoService, extensionApi);
            lifecycleV1Extension.onRegistered();

            ArgumentCaptor<Map<String, Object>> lifecycleSharedState =
                    ArgumentCaptor.forClass(Map.class);
            ArgumentCaptor<Event> lifecycleStartEventCaptor = ArgumentCaptor.forClass(Event.class);
            verify(extensionApi, times(1))
                    .createSharedState(
                            lifecycleSharedState.capture(), lifecycleStartEventCaptor.capture());
            assertNull(lifecycleStartEventCaptor.getValue());
            assertEquals(0L, lifecycleSharedState.getValue().get(SESSION_START_TIMESTAMP));
            assertEquals(
                    MAX_SESSION_LENGTH_SECONDS,
                    lifecycleSharedState.getValue().get(MAX_SESSION_LENGTH));
            assertEquals(bootData, lifecycleSharedState.getValue().get(LIFECYCLE_CONTEXT_DATA));
        }
    }
}
