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
import static com.adobe.marketing.mobile.LifecycleEventGeneratorTestHelper.createPauseEvent;
import static com.adobe.marketing.mobile.LifecycleEventGeneratorTestHelper.createStartEvent;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.ExtensionApi;
import com.adobe.marketing.mobile.SharedStateResolution;
import com.adobe.marketing.mobile.SharedStateResult;
import com.adobe.marketing.mobile.SharedStateStatus;
import com.adobe.marketing.mobile.services.NamedCollection;
import java.util.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class LifecycleExtensionTest {

    @Mock ExtensionApi extensionApi;

    @Mock NamedCollection lifecycleDataStore;

    @Mock LifecycleV1Extension mockLifecycleV1Extension;

    @Mock LifecycleV2Extension mockLifecycleV2Extension;

    private final long currentTimestampInMilliSeconds = System.currentTimeMillis();
    private LifecycleExtension lifecycle;

    private static final String LIFECYCLE_ACTION_KEY = "action";

    private static final String LIFECYCLE_CONFIG_SESSION_TIMEOUT = "lifecycle.sessionTimeout";
    private static final String CONFIGURATION_MODULE_NAME = "com.adobe.module.configuration";

    private static final String EVENT_TYPE_GENERIC_LIFECYCLE =
            "com.adobe.eventType.generic.lifecycle";
    private static final String EVENT_SOURCE_REQUEST_CONTENT =
            "com.adobe.eventSource.requestContent";
    private static final String EVENT_TYPE_NON_GENERIC_LIFECYCLE =
            "com.adobe.eventType.non.generic.lifecycle";

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

        lifecycle =
                new LifecycleExtension(
                        extensionApi,
                        lifecycleDataStore,
                        mockLifecycleV1Extension,
                        mockLifecycleV2Extension);
    }

    @Test
    public void testGetName() {
        assertEquals("com.adobe.module.lifecycle", lifecycle.getName());
    }

    @Test
    public void testGetFriendlyName() {
        assertEquals("Lifecycle", lifecycle.getFriendlyName());
    }

    @Test
    public void testGetVersion() {
        assertEquals("2.0.1", lifecycle.getVersion());
    }

    @Test
    public void readyForEvent_ConfigurationSharedStateSet() {
        Event event =
                new Event.Builder(
                                "Lifecycle_queueEvent_Happy",
                                EVENT_TYPE_GENERIC_LIFECYCLE,
                                EVENT_SOURCE_REQUEST_CONTENT)
                        .build();

        assertTrue(lifecycle.readyForEvent(event));
    }

    @Test
    public void readyForEvent_ConfigurationSharedStatePending() {
        // set config shared state to pending
        when(extensionApi.getSharedState(
                        eq(CONFIGURATION_MODULE_NAME),
                        any(),
                        eq(false),
                        eq(SharedStateResolution.ANY)))
                .thenReturn(new SharedStateResult(SharedStateStatus.PENDING, new HashMap<>()));

        Event event =
                new Event.Builder(
                                "Lifecycle_queueEvent_Happy",
                                EVENT_TYPE_GENERIC_LIFECYCLE,
                                EVENT_SOURCE_REQUEST_CONTENT)
                        .build();

        assertFalse(lifecycle.readyForEvent(event));
    }

    @Test
    public void readyForEvent_ConfigurationSharedStateNull() {
        // set config shared state to null
        when(extensionApi.getSharedState(
                        eq(CONFIGURATION_MODULE_NAME),
                        any(),
                        eq(false),
                        eq(SharedStateResolution.ANY)))
                .thenReturn(null);

        Event event =
                new Event.Builder(
                                "Lifecycle_queueEvent_Happy",
                                EVENT_TYPE_GENERIC_LIFECYCLE,
                                EVENT_SOURCE_REQUEST_CONTENT)
                        .build();

        assertFalse(lifecycle.readyForEvent(event));
    }

    @Test
    public void readyForEvent_NotGenericLifecycleEvent() {
        Event event =
                new Event.Builder(
                                "Non generic lifecycle event",
                                EVENT_TYPE_NON_GENERIC_LIFECYCLE,
                                EVENT_SOURCE_REQUEST_CONTENT)
                        .build();

        assertTrue(lifecycle.readyForEvent(event));
    }

    @Test
    public void handleLifecycleRequestEvent_ConfigurationSharedStateNull() {
        when(extensionApi.getSharedState(
                        eq(CONFIGURATION_MODULE_NAME),
                        any(),
                        eq(false),
                        eq(SharedStateResolution.ANY)))
                .thenReturn(null);

        lifecycle.handleLifecycleRequestEvent(
                createStartEvent(null, currentTimestampInMilliSeconds));

        verifyNoInteractions(mockLifecycleV1Extension);
    }

    @Test
    public void handleLifecycleRequestEvent_ConfigurationSharedStatePending() {
        when(extensionApi.getSharedState(
                        eq(CONFIGURATION_MODULE_NAME),
                        any(),
                        eq(false),
                        eq(SharedStateResolution.ANY)))
                .thenReturn(new SharedStateResult(SharedStateStatus.PENDING, new HashMap<>()));

        lifecycle.handleLifecycleRequestEvent(
                createStartEvent(null, currentTimestampInMilliSeconds));

        verifyNoInteractions(mockLifecycleV1Extension);
    }

    @Test
    public void handleLifecycleRequestEvent_EventDataEmpty() {
        Event lifecycleEvent = createLifecycleEvent(null, currentTimestampInMilliSeconds);

        lifecycle.handleLifecycleRequestEvent(lifecycleEvent);

        verifyNoInteractions(mockLifecycleV1Extension);
    }

    @Test
    public void handleLifecycleRequestEvent_LifecyclePause() {
        Event lifecyclePauseEvent = createPauseEvent(currentTimestampInMilliSeconds);

        lifecycle.handleLifecycleRequestEvent(lifecyclePauseEvent);

        verify(mockLifecycleV1Extension, times(1)).pause(lifecyclePauseEvent);
        verify(mockLifecycleV2Extension, times(1)).pause(lifecyclePauseEvent);
    }

    @Test
    public void handleLifecycleRequestEvent_InvalidEventData() {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put(LIFECYCLE_ACTION_KEY, "invalid_action");
        Event lifecycleRequestEvent =
                createLifecycleEvent(eventData, currentTimestampInMilliSeconds);
        lifecycle.handleLifecycleRequestEvent(lifecycleRequestEvent);

        verifyNoInteractions(mockLifecycleV1Extension);
    }

    @Test
    public void handleLifecycleRequestEvent_EmptyEventData() {
        Event lifecycleRequestEvent = createLifecycleEvent(null, currentTimestampInMilliSeconds);
        lifecycle.handleLifecycleRequestEvent(lifecycleRequestEvent);

        verifyNoInteractions(mockLifecycleV1Extension);
    }

    @Test
    public void handleUpdateLastKnownTimestamp() {
        Event lifecycleRequestEvent = createLifecycleEvent(null, currentTimestampInMilliSeconds);
        lifecycle.updateLastKnownTimestamp(lifecycleRequestEvent);
        verify(mockLifecycleV2Extension, times(1)).updateLastKnownTimestamp(lifecycleRequestEvent);
    }
}
