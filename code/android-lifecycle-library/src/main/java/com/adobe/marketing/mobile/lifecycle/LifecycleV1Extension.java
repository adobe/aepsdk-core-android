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

import java.util.HashMap;
import java.util.Map;

public class LifecycleV1Extension {

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
    LifecycleV1Extension(final NamedCollection dataStore,
                         final DeviceInforming deviceInfoService,
                         final ExtensionApi extensionApi) {
        this.dataStore = dataStore;
        this.extensionApi = extensionApi;
        lifecycleState = new LifecycleState(dataStore, deviceInfoService);
    }

    /**
     * This constructor is used for testing purposes only
     *
     * @param dataStore {@code NamedCollection} instance
     * @param extensionApi {@code ExtensionApi} instance
     * @param lifecycleState {@code LifecycleState} instance
     */
    protected LifecycleV1Extension(final NamedCollection dataStore,
                                   final ExtensionApi extensionApi,
                                   final LifecycleState lifecycleState) {
        this.dataStore = dataStore;
        this.extensionApi = extensionApi;
        this.lifecycleState = lifecycleState;
    }

    /**
     * Start the lifecycle session for standard and XDM workflows
     *
     * @param startEvent current lifecycle event to be processed
     * @param configurationSharedState configuration shared state data for this event
     */
     boolean start(final Event startEvent,
                   final Map<String, Object> configurationSharedState,
                   boolean isInstall) {

         final long startTimestampInSeconds = startEvent.getTimestampInSeconds();

         Map<String, Object> eventData = startEvent.getEventData();
         Map<String, String> additionalContextData = null;

         if (eventData != null) {
             try {
                 additionalContextData = (Map<String, String>) eventData.get(LifecycleConstants.EventDataKeys.Lifecycle.ADDITIONAL_CONTEXT_DATA);
             } catch (Exception e) {
                 Log.trace(LifecycleConstants.LOG_TAG, "Request content event data error, event data is null");
             }
         }

         LifecycleSession.SessionInfo previousSessionInfo = lifecycleState.start(startTimestampInSeconds,
                 additionalContextData,
                 getAdvertisingIdentifier(startEvent),
                 getSessionTimeoutLength(configurationSharedState),
                 isInstall);

         if (previousSessionInfo == null) {
             // Analytics extension needs adjusted start date to calculate timeSinceLaunch param.
             if (dataStore != null) {
                 final long startTime = dataStore.getLong(LifecycleConstants.DataStoreKeys.START_DATE, 0L);
                 updateLifecycleSharedState(startEvent, startTime, lifecycleState.getContextData());
                 return false;
             }
         }

         updateLifecycleSharedState(startEvent, startTimestampInSeconds, lifecycleState.getContextData());
         if (previousSessionInfo != null) {
             dispatchSessionStart(startTimestampInSeconds, previousSessionInfo.getStartTimestampInSeconds(), previousSessionInfo.getPauseTimestampInSeconds());
         }

         if (isInstall) {
             persistInstallDate(startEvent);
         }

         return true;
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
     * Updates the lifecycle shared state with current context data and default data when a boot event is received
     *
     * @param bootEvent to be processed
     */
    void processBootEvent(final Event bootEvent) {
        updateLifecycleSharedState(bootEvent,
                0,
                lifecycleState.computeBootData(bootEvent.getTimestampInSeconds())
        );
    }

    /**
     * Gets advertising identifier.
     *
     * @param event Event containing advertising identifier data
     *
     * @return the advertising identifier
     */
    private String getAdvertisingIdentifier(final Event event) {
        SharedStateResult identitySharedState = extensionApi.getSharedState(LifecycleConstants.EventDataKeys.Identity.MODULE_NAME, event, false, SharedStateResolution.ANY);

        if (identitySharedState != null && identitySharedState.status == SharedStateStatus.PENDING) {
            return null;
        }

        if (identitySharedState != null && identitySharedState.value != null) {
            try {
                return (String) identitySharedState.value.get(LifecycleConstants.EventDataKeys.Identity.ADVERTISING_IDENTIFIER);
            } catch (Exception e) {
                return null;
            }
        }

        return null;
    }

    /**
     * Persist Application install date.
     *
     * @param event lifecycle start event.
     */
    private void persistInstallDate(final Event event) {
        if (dataStore == null) {
            return;
        }

        final long startTimestampInSeconds = event.getTimestampInSeconds();
        dataStore.setLong(LifecycleConstants.DataStoreKeys.INSTALL_DATE, startTimestampInSeconds);
    }

    /**
     * Reads the session timeout from the configuration shared state, if not found returns the default session timeout
     * @param configurationSharedState current configuration shared state
     * @return session timeout
     */
    private long getSessionTimeoutLength(Map<String, Object> configurationSharedState) {
        long sessionTimeoutInSeconds = LifecycleConstants.DEFAULT_LIFECYCLE_TIMEOUT;
        if (configurationSharedState != null) {
            Object sessionTimeout = configurationSharedState.get(LifecycleConstants.EventDataKeys.Configuration.LIFECYCLE_CONFIG_SESSION_TIMEOUT);
            if(sessionTimeout != null) {
                try {
                    sessionTimeoutInSeconds = (long) sessionTimeout;
                } catch (Exception e) {
                    return sessionTimeoutInSeconds;
                }
            }
        }
        return sessionTimeoutInSeconds;
    }

    /**
     * Updates lifecycle shared state versioned at {@code event} with {@code contextData}
     *
     * @param event the event to version the shared state at
     * @param startTimestampInSeconds  The current session start timestamp in seconds
     * @param contextData {@code Map<String, String>} context data to be updated
     */
    private void updateLifecycleSharedState(final Event event,
                                            final long startTimestampInSeconds,
                                            final Map<String, String> contextData) {
        Map<String, Object> lifecycleSharedState = new HashMap<>();
        lifecycleSharedState.put(LifecycleConstants.EventDataKeys.Lifecycle.SESSION_START_TIMESTAMP,
                startTimestampInSeconds);
        lifecycleSharedState.put(LifecycleConstants.EventDataKeys.Lifecycle.MAX_SESSION_LENGTH,
                LifecycleConstants.MAX_SESSION_LENGTH_SECONDS);
        lifecycleSharedState.put(LifecycleConstants.EventDataKeys.Lifecycle.LIFECYCLE_CONTEXT_DATA, contextData);
        extensionApi.createSharedState(lifecycleSharedState, event);
    }

    /**
     * Dispatches a Lifecycle response content event with appropriate event data
     * @param startTimestampInSeconds session start time
     * @param previousStartTime start time of previous session
     * @param previousPauseTime pause time of previous session
     */
    private void dispatchSessionStart(long startTimestampInSeconds, long previousStartTime, long previousPauseTime){
        // Dispatch a new event with session related data
        Map<String, Object> eventDataMap = new HashMap<>();
        eventDataMap.put(LifecycleConstants.EventDataKeys.Lifecycle.LIFECYCLE_CONTEXT_DATA, lifecycleState.getContextData());
        eventDataMap.put(LifecycleConstants.EventDataKeys.Lifecycle.SESSION_EVENT,
                LifecycleConstants.EventDataKeys.Lifecycle.LIFECYCLE_START);
        eventDataMap.put(LifecycleConstants.EventDataKeys.Lifecycle.SESSION_START_TIMESTAMP, startTimestampInSeconds);
        eventDataMap.put(LifecycleConstants.EventDataKeys.Lifecycle.MAX_SESSION_LENGTH,
                LifecycleConstants.MAX_SESSION_LENGTH_SECONDS);
        eventDataMap.put(LifecycleConstants.EventDataKeys.Lifecycle.PREVIOUS_SESSION_START_TIMESTAMP, previousStartTime);
        eventDataMap.put(LifecycleConstants.EventDataKeys.Lifecycle.PREVIOUS_SESSION_PAUSE_TIMESTAMP, previousPauseTime);

        final Event startEvent = new Event.Builder(
                LifecycleConstants.EventName.LIFECYCLE_START_EVENT,
                EventType.LIFECYCLE,
                EventSource.RESPONSE_CONTENT).setEventData(eventDataMap).build();

        extensionApi.dispatch(startEvent);
    }
}
