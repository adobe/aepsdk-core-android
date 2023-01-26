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

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.EventSource;
import com.adobe.marketing.mobile.EventType;
import com.adobe.marketing.mobile.ExtensionApi;
import com.adobe.marketing.mobile.SharedStateResolution;
import com.adobe.marketing.mobile.SharedStateResult;
import com.adobe.marketing.mobile.SharedStateStatus;
import com.adobe.marketing.mobile.services.DeviceInforming;
import com.adobe.marketing.mobile.services.NamedCollection;
import com.adobe.marketing.mobile.util.DataReader;
import java.util.HashMap;
import java.util.Map;

class LifecycleV1Extension {

    private static final String SELF_LOG_TAG = "LifecycleV1Extension";
    private final NamedCollection dataStore;
    private final ExtensionApi extensionApi;
    private final LifecycleState lifecycleState;

    /**
     * Constructor for the LifecycleV2Extension.
     *
     * @param dataStore {@code NamedCollection} instance
     * @param deviceInfoService {@code DeviceInforming} instance
     * @param extensionApi {@code ExtensionApi} instance
     */
    LifecycleV1Extension(
            final NamedCollection dataStore,
            final DeviceInforming deviceInfoService,
            final ExtensionApi extensionApi) {
        this.dataStore = dataStore;
        this.extensionApi = extensionApi;
        lifecycleState = new LifecycleState(dataStore, deviceInfoService);
    }

    /**
     * Start the lifecycle session for standard and XDM workflows
     *
     * @param startEvent current lifecycle event to be processed
     * @param configurationSharedState configuration shared state data for this event
     */
    void start(
            final Event startEvent,
            final Map<String, Object> configurationSharedState,
            final boolean isInstall) {
        final long startTimestampInSeconds = startEvent.getTimestampInSeconds();

        Map<String, Object> eventData = startEvent.getEventData();
        Map<String, String> additionalContextData =
                DataReader.optStringMap(
                        eventData,
                        LifecycleConstants.EventDataKeys.Lifecycle.ADDITIONAL_CONTEXT_DATA,
                        null);

        LifecycleSession.SessionInfo previousSessionInfo =
                lifecycleState.start(
                        startTimestampInSeconds,
                        additionalContextData,
                        getAdvertisingIdentifier(startEvent),
                        getSessionTimeoutLength(configurationSharedState),
                        isInstall);

        if (previousSessionInfo == null) {
            // Analytics extension needs adjusted start date to calculate timeSinceLaunch param.
            if (dataStore != null) {
                final long startTime =
                        dataStore.getLong(LifecycleConstants.DataStoreKeys.START_DATE, 0L);
                updateLifecycleSharedState(startEvent, startTime, lifecycleState.getContextData());
                return;
            }
        }

        updateLifecycleSharedState(
                startEvent, startTimestampInSeconds, lifecycleState.getContextData());
        if (previousSessionInfo != null) {
            dispatchSessionStart(
                    startTimestampInSeconds,
                    previousSessionInfo.getStartTimestampInSeconds(),
                    previousSessionInfo.getPauseTimestampInSeconds());
        }
    }

    /**
     * Pause the lifecycle session for standard lifecycle workflow
     *
     * @param pauseEvent current lifecycle event to be processed
     */
    void pause(final Event pauseEvent) {
        lifecycleState.pause(pauseEvent);
    }

    /**
     * Updates the lifecycle shared state with current context data and default data when extension
     * is registered
     */
    void onRegistered() {
        updateLifecycleSharedState(null, 0, lifecycleState.computeBootData());
    }

    /**
     * Gets advertising identifier.
     *
     * @param event Event containing advertising identifier data
     * @return the advertising identifier
     */
    private String getAdvertisingIdentifier(final Event event) {
        SharedStateResult identitySharedState =
                extensionApi.getSharedState(
                        LifecycleConstants.EventDataKeys.Identity.MODULE_NAME,
                        event,
                        false,
                        SharedStateResolution.ANY);

        if (identitySharedState != null
                && identitySharedState.getStatus() == SharedStateStatus.SET) {
            return DataReader.optString(
                    identitySharedState.getValue(),
                    LifecycleConstants.EventDataKeys.Identity.ADVERTISING_IDENTIFIER,
                    null);
        }
        return null;
    }

    /**
     * Reads the session timeout from the configuration shared state, if not found returns the
     * default session timeout
     *
     * @param configurationSharedState current configuration shared state
     * @return session timeout
     */
    private long getSessionTimeoutLength(final Map<String, Object> configurationSharedState) {
        return DataReader.optLong(
                configurationSharedState,
                LifecycleConstants.EventDataKeys.Configuration.LIFECYCLE_CONFIG_SESSION_TIMEOUT,
                LifecycleConstants.DEFAULT_LIFECYCLE_TIMEOUT);
    }

    /**
     * Updates lifecycle shared state versioned at {@code event} with {@code contextData}
     *
     * @param event the event to version the shared state at
     * @param startTimestampInSeconds The current session start timestamp in seconds
     * @param contextData {@code Map<String, String>} context data to be updated
     */
    private void updateLifecycleSharedState(
            final Event event,
            final long startTimestampInSeconds,
            final Map<String, String> contextData) {
        Map<String, Object> lifecycleSharedState = new HashMap<>();
        lifecycleSharedState.put(
                LifecycleConstants.EventDataKeys.Lifecycle.SESSION_START_TIMESTAMP,
                startTimestampInSeconds);
        lifecycleSharedState.put(
                LifecycleConstants.EventDataKeys.Lifecycle.MAX_SESSION_LENGTH,
                LifecycleConstants.MAX_SESSION_LENGTH_SECONDS);
        lifecycleSharedState.put(
                LifecycleConstants.EventDataKeys.Lifecycle.LIFECYCLE_CONTEXT_DATA, contextData);
        extensionApi.createSharedState(lifecycleSharedState, event);
    }

    /**
     * Dispatches a Lifecycle response content event with appropriate event data
     *
     * @param startTimestampInSeconds session start time
     * @param previousStartTime start time of previous session
     * @param previousPauseTime pause time of previous session
     */
    private void dispatchSessionStart(
            final long startTimestampInSeconds,
            final long previousStartTime,
            final long previousPauseTime) {
        // Dispatch a new event with session related data
        Map<String, Object> eventDataMap = new HashMap<>();
        eventDataMap.put(
                LifecycleConstants.EventDataKeys.Lifecycle.LIFECYCLE_CONTEXT_DATA,
                lifecycleState.getContextData());
        eventDataMap.put(
                LifecycleConstants.EventDataKeys.Lifecycle.SESSION_EVENT,
                LifecycleConstants.EventDataKeys.Lifecycle.LIFECYCLE_START);
        eventDataMap.put(
                LifecycleConstants.EventDataKeys.Lifecycle.SESSION_START_TIMESTAMP,
                startTimestampInSeconds);
        eventDataMap.put(
                LifecycleConstants.EventDataKeys.Lifecycle.MAX_SESSION_LENGTH,
                LifecycleConstants.MAX_SESSION_LENGTH_SECONDS);
        eventDataMap.put(
                LifecycleConstants.EventDataKeys.Lifecycle.PREVIOUS_SESSION_START_TIMESTAMP,
                previousStartTime);
        eventDataMap.put(
                LifecycleConstants.EventDataKeys.Lifecycle.PREVIOUS_SESSION_PAUSE_TIMESTAMP,
                previousPauseTime);

        final Event startEvent =
                new Event.Builder(
                                LifecycleConstants.EventName.LIFECYCLE_START_EVENT,
                                EventType.LIFECYCLE,
                                EventSource.RESPONSE_CONTENT)
                        .setEventData(eventDataMap)
                        .build();

        extensionApi.dispatch(startEvent);
    }
}
