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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.ExtensionApi;
import com.adobe.marketing.mobile.SharedStateResolution;
import com.adobe.marketing.mobile.SharedStateResult;
import com.adobe.marketing.mobile.SharedStateStatus;
import com.adobe.marketing.mobile.services.NamedCollection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RunWith(MockitoJUnitRunner.Silent.class)
public class LifecycleV1ExtensionTest {

    @Mock
    ExtensionApi extensionApi;

    @Mock
    NamedCollection lifecycleDataStore;

    @Mock
    LifecycleState mockLifecycleState;

    Map<String, Object> configurationSharedState = new HashMap<>();
    private final long currentTimestampInMilliSeconds = System.currentTimeMillis();
    private final long currentTimestampInSeconds = TimeUnit.MILLISECONDS.toSeconds(currentTimestampInMilliSeconds);
    private LifecycleV1Extension lifecycleV1Extension;

    private static final String LIFECYCLE_CONFIG_SESSION_TIMEOUT = "lifecycle.sessionTimeout";
    private static final String SESSION_START_TIMESTAMP = "starttimestampmillis";
    private static final String MAX_SESSION_LENGTH      = "maxsessionlength";
    private static final long MAX_SESSION_LENGTH_SECONDS = TimeUnit.DAYS.toSeconds(7);

    private static final String IDENTITY_MODULE_NAME = "com.adobe.module.identity";
    private static final String ADVERTISING_IDENTIFIER = "advertisingidentifier";


    @Before
    public void beforeEach() {
        configurationSharedState.put(LIFECYCLE_CONFIG_SESSION_TIMEOUT, 200L);

        lifecycleV1Extension = new LifecycleV1Extension(lifecycleDataStore,
                extensionApi,
                mockLifecycleState);
    }

    @Test
    public void handleLifecycleRequestEvent_LifecycleStart_FirstLaunch() {
        Event lifecycleStartEvent = createStartEvent(null, currentTimestampInMilliSeconds);

        lifecycleV1Extension.start(lifecycleStartEvent, configurationSharedState, true);

        ArgumentCaptor<Long> startTimeCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Map<String, String>> additionalDataCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<String> adIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> sessionTimeoutCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Boolean> isInstallCaptor = ArgumentCaptor.forClass(Boolean.class);

        verify(mockLifecycleState, times(1)).start(startTimeCaptor.capture(),
                additionalDataCaptor.capture(),
                adIdCaptor.capture(),
                sessionTimeoutCaptor.capture(),
                isInstallCaptor.capture());
        assertEquals(currentTimestampInSeconds, startTimeCaptor.getValue().longValue());
        assertNull(additionalDataCaptor.getValue());
        assertNull(adIdCaptor.getValue());
        assertEquals(200L, sessionTimeoutCaptor.getValue().longValue());
        assertTrue(isInstallCaptor.getValue());
    }

    @Test
    public void handleLifecycleRequestEvent_LifecycleStart_SubsequentLaunch() {
        Event lifecycleStartEvent = createStartEvent(null, currentTimestampInMilliSeconds);

        lifecycleV1Extension.start(lifecycleStartEvent, configurationSharedState, false);

        ArgumentCaptor<Long> startTimeCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Map<String, String>> additionalDataCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<String> adIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> sessionTimeoutCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Boolean> isInstallCaptor = ArgumentCaptor.forClass(Boolean.class);

        verify(mockLifecycleState, times(1)).start(startTimeCaptor.capture(),
                additionalDataCaptor.capture(),
                adIdCaptor.capture(),
                sessionTimeoutCaptor.capture(),
                isInstallCaptor.capture());
        assertEquals(currentTimestampInSeconds, startTimeCaptor.getValue().longValue());
        assertNull(additionalDataCaptor.getValue());
        assertNull(adIdCaptor.getValue());
        assertEquals(200L, sessionTimeoutCaptor.getValue().longValue());
        assertFalse(isInstallCaptor.getValue());
    }

    @Test
    public void handleLifecycleRequestEvent_LifecycleStart_AdditionalData() {
        Map<String, String> additionalData = new HashMap<>();
        additionalData.put("testKey", "testVal");
        Event lifecycleStartEvent = createStartEvent(additionalData, currentTimestampInMilliSeconds);

        lifecycleV1Extension.start(lifecycleStartEvent, configurationSharedState, true);

        ArgumentCaptor<Long> startTimeCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Map<String, String>> additionalDataCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<String> adIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> sessionTimeoutCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Boolean> isInstallCaptor = ArgumentCaptor.forClass(Boolean.class);

        verify(mockLifecycleState, times(1)).start(startTimeCaptor.capture(),
                additionalDataCaptor.capture(),
                adIdCaptor.capture(),
                sessionTimeoutCaptor.capture(),
                isInstallCaptor.capture());
        assertEquals(currentTimestampInSeconds, startTimeCaptor.getValue().longValue());
        assertEquals(additionalData, additionalDataCaptor.getValue());
        assertNull(adIdCaptor.getValue());
        assertEquals(200L, sessionTimeoutCaptor.getValue().longValue());
        assertTrue(isInstallCaptor.getValue());
    }

    @Test
    public void handleLifecycleRequestEvent_LifecycleStart_IdentitySharedStatePending() {
        when(extensionApi.getSharedState(
                eq(IDENTITY_MODULE_NAME),
                any(),
                eq(false),
                eq(SharedStateResolution.ANY)
        )).thenReturn(new SharedStateResult(SharedStateStatus.PENDING, new HashMap<>()));

        Map<String, String> additionalData = new HashMap<>();
        additionalData.put("testKey", "testVal");
        Event lifecycleStartEvent = createStartEvent(additionalData, currentTimestampInMilliSeconds);

        lifecycleV1Extension.start(lifecycleStartEvent, configurationSharedState, true);

        ArgumentCaptor<Long> startTimeCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Map<String, String>> additionalDataCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<String> adIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> sessionTimeoutCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Boolean> isInstallCaptor = ArgumentCaptor.forClass(Boolean.class);

        verify(mockLifecycleState, times(1)).start(startTimeCaptor.capture(),
                additionalDataCaptor.capture(),
                adIdCaptor.capture(),
                sessionTimeoutCaptor.capture(),
                isInstallCaptor.capture());
        assertEquals(currentTimestampInSeconds, startTimeCaptor.getValue().longValue());
        assertEquals(additionalData, additionalDataCaptor.getValue());
        assertNull(adIdCaptor.getValue());
        assertEquals(200L, sessionTimeoutCaptor.getValue().longValue());
        assertTrue(isInstallCaptor.getValue());
    }

    @Test
    public void handleLifecycleRequestEvent_LifecycleStart_AdIdSet() {
        Map<String, Object> identitySharedState = new HashMap<>();
        identitySharedState.put(ADVERTISING_IDENTIFIER, "testAdid");
        when(extensionApi.getSharedState(
                eq(IDENTITY_MODULE_NAME),
                any(),
                eq(false),
                eq(SharedStateResolution.ANY)
        )).thenReturn(new SharedStateResult(SharedStateStatus.SET, identitySharedState));

        Map<String, String> additionalData = new HashMap<>();
        additionalData.put("testKey", "testVal");
        Event lifecycleStartEvent = createStartEvent(additionalData, currentTimestampInMilliSeconds);

        lifecycleV1Extension.start(lifecycleStartEvent, configurationSharedState, true);

        ArgumentCaptor<Long> startTimeCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Map<String, String>> additionalDataCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<String> adIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> sessionTimeoutCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Boolean> isInstallCaptor = ArgumentCaptor.forClass(Boolean.class);

        verify(mockLifecycleState, times(1)).start(startTimeCaptor.capture(),
                additionalDataCaptor.capture(),
                adIdCaptor.capture(),
                sessionTimeoutCaptor.capture(),
                isInstallCaptor.capture());
        assertEquals(currentTimestampInSeconds, startTimeCaptor.getValue().longValue());
        assertEquals(additionalData, additionalDataCaptor.getValue());
        assertEquals("testAdid", adIdCaptor.getValue());
        assertEquals(200L, sessionTimeoutCaptor.getValue().longValue());
        assertTrue(isInstallCaptor.getValue());
    }

    @Test
    public void handleLifecycleRequestEvent_LifecycleStart_SessionTimeoutNotSet() {
        Event lifecycleStartEvent = createStartEvent(null, currentTimestampInMilliSeconds);

        lifecycleV1Extension.start(lifecycleStartEvent, new HashMap<>(), true);

        ArgumentCaptor<Long> startTimeCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Map<String, String>> additionalDataCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<String> adIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> sessionTimeoutCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Boolean> isInstallCaptor = ArgumentCaptor.forClass(Boolean.class);

        verify(mockLifecycleState, times(1)).start(startTimeCaptor.capture(),
                additionalDataCaptor.capture(),
                adIdCaptor.capture(),
                sessionTimeoutCaptor.capture(),
                isInstallCaptor.capture());
        assertEquals(currentTimestampInSeconds, startTimeCaptor.getValue().longValue());
        assertNull(additionalDataCaptor.getValue());
        assertNull(adIdCaptor.getValue());
        assertEquals(300L, sessionTimeoutCaptor.getValue().longValue());
        assertTrue(isInstallCaptor.getValue());
    }

    @Test
    public void processLifecycleExtensionRegistration() {
        lifecycleV1Extension.onRegistered();

        verify(mockLifecycleState, times(1)).computeBootData();

        ArgumentCaptor<Map<String, Object>> lifecycleStateCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(extensionApi, times(1)).createSharedState(lifecycleStateCaptor.capture(), eventCaptor.capture());
        assertEquals(0L, lifecycleStateCaptor.getValue().get(SESSION_START_TIMESTAMP));
        assertEquals(MAX_SESSION_LENGTH_SECONDS, lifecycleStateCaptor.getValue().get(MAX_SESSION_LENGTH));
    }
}
