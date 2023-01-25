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

import androidx.annotation.VisibleForTesting;
import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.EventSource;
import com.adobe.marketing.mobile.EventType;
import com.adobe.marketing.mobile.ExtensionApi;
import com.adobe.marketing.mobile.services.DeviceInforming;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.NamedCollection;
import com.adobe.marketing.mobile.util.DataReader;
import com.adobe.marketing.mobile.util.StringUtils;
import java.util.HashMap;
import java.util.Map;

/**
 * LifecycleV2Extension class
 *
 * <p>The responsibility of LifecycleV2Extension is to compute the application launch/close XDM
 * metrics, usually consumed by the Edge Network and related extensions
 */
class LifecycleV2Extension {

    private static final String SELF_LOG_TAG = "LifecycleV2Extension";
    private final LifecycleV2DataStoreCache dataStoreCache;
    private final LifecycleV2StateManager stateManager;
    private final LifecycleV2MetricsBuilder xdmMetricsBuilder;
    private final NamedCollection dataStore;
    private final DeviceInforming deviceInfoService;

    private final long BACKDATE_TIMESTAMP_OFFSET_MILLIS = 1000; // backdate timestamps by 1 second
    private final ExtensionApi extensionApi;

    /**
     * Constructor for the LifecycleV2Extension.
     *
     * @param dataStore {@code NamedCollection} instance
     * @param deviceInfoService {@code DeviceInforming} instance
     * @param extensionApi {@code ExtensionApi} instance
     */
    LifecycleV2Extension(
            final NamedCollection dataStore,
            final DeviceInforming deviceInfoService,
            final ExtensionApi extensionApi) {
        this(dataStore, deviceInfoService, null, extensionApi);
    }

    /**
     * This constructor is intended for testing purposes.
     *
     * @param dataStore {@code NamedCollection} instance
     * @param deviceInfoService {@code DeviceInforming} instance
     * @param metricsBuilder XDM LifecycleMetricsBuilder instance. If null, a new instance will be
     *     created
     */
    @VisibleForTesting
    LifecycleV2Extension(
            final NamedCollection dataStore,
            final DeviceInforming deviceInfoService,
            final LifecycleV2MetricsBuilder metricsBuilder,
            final ExtensionApi extensionApi) {
        this.dataStore = dataStore;
        this.deviceInfoService = deviceInfoService;
        this.extensionApi = extensionApi;
        stateManager = new LifecycleV2StateManager();
        dataStoreCache = new LifecycleV2DataStoreCache(dataStore);
        xdmMetricsBuilder =
                metricsBuilder != null
                        ? metricsBuilder
                        : new LifecycleV2MetricsBuilder(deviceInfoService);
    }

    /**
     * Handles the start use-case as application launch XDM event. If a previous abnormal close was
     * detected, an application close event will be dispatched first.
     *
     * @param event event containing lifecycle start data
     * @param isInstall boolean indicating whether this is an application install scenario
     */
    void start(final Event event, final boolean isInstall) {
        stateManager.updateState(
                LifecycleV2StateManager.State.START,
                updated -> {
                    if (!updated) {
                        return;
                    }

                    // detect a possible crash/incorrect start/pause implementation
                    if (!isInstall
                            && isCloseUnknown(
                                    dataStoreCache.getAppStartTimestampMillis(),
                                    dataStoreCache.getAppPauseTimestampMillis())) {
                        // in case of an unknown close situation, use the last known app close event
                        // timestamp
                        // if no close timestamp was persisted, backdate this event to start
                        // timestamp - 1 second
                        Map<String, Object> appCloseXDMData =
                                xdmMetricsBuilder.buildAppCloseXDMData(
                                        dataStoreCache.getAppStartTimestampMillis(),
                                        dataStoreCache.getCloseTimestampMillis(),
                                        event.getTimestamp() - BACKDATE_TIMESTAMP_OFFSET_MILLIS,
                                        true);
                        // Dispatch application close event with xdm data
                        dispatchApplicationClose(appCloseXDMData);
                    }

                    final long startTimestamp = event.getTimestamp();
                    dataStoreCache.setAppStartTimestamp(startTimestamp);

                    Map<String, Object> appLaunchXDMData =
                            xdmMetricsBuilder.buildAppLaunchXDMData(
                                    startTimestamp, isInstall, isUpgrade());
                    Map<String, String> freeFormData =
                            DataReader.optStringMap(
                                    event.getEventData(),
                                    LifecycleConstants.EventDataKeys.Lifecycle
                                            .ADDITIONAL_CONTEXT_DATA,
                                    null);

                    // Dispatch application launch event with xdm data
                    dispatchApplicationLaunch(appLaunchXDMData, freeFormData);

                    // persist App version to track App upgrades
                    persistAppVersion();
                });
    }

    /**
     * Handles the pause use-case as application close XDM event.
     *
     * @param event event containing lifecycle pause timestamp
     */
    void pause(final Event event) {
        stateManager.updateState(
                LifecycleV2StateManager.State.PAUSE,
                updated -> {
                    if (!updated) {
                        return;
                    }

                    final long pauseTimestamp = event.getTimestamp();
                    dataStoreCache.setAppPauseTimestamp(pauseTimestamp);

                    Map<String, Object> appCloseXDMData =
                            xdmMetricsBuilder.buildAppCloseXDMData(
                                    dataStoreCache.getAppStartTimestampMillis(),
                                    pauseTimestamp,
                                    pauseTimestamp,
                                    false);
                    // Dispatch application close event with xdm data
                    dispatchApplicationClose(appCloseXDMData);
                });
    }

    /**
     * Updates the last known event timestamp in cache and if needed in persistence
     *
     * @param event to be processed; should not be null
     */
    void updateLastKnownTimestamp(final Event event) {
        dataStoreCache.setLastKnownTimestamp(event.getTimestamp());
    }

    /**
     * This helper method identifies if the previous session ended due to an incorrect
     * implementation or possible app crash.
     *
     * @param previousAppStart start timestamp from previous session (milliseconds)
     * @param previousAppPause pause timestamp from previous session (milliseconds)
     * @return the status of the previous app close, true if this is considered an unknown close
     *     event
     */
    private boolean isCloseUnknown(final long previousAppStart, final long previousAppPause) {
        return previousAppStart <= 0 || previousAppStart > previousAppPause;
    }

    /**
     * Check if the application has been upgraded
     *
     * @return boolean whether the app has been upgraded
     */
    private boolean isUpgrade() {
        String previousAppVersion = "";

        if (dataStore != null) {
            previousAppVersion =
                    dataStore.getString(LifecycleV2Constants.DataStoreKeys.LAST_APP_VERSION, "");
        }

        return (deviceInfoService != null
                && !StringUtils.isNullOrEmpty(previousAppVersion)
                && !previousAppVersion.equalsIgnoreCase(deviceInfoService.getApplicationVersion()));
    }

    /** Persist the application version into datastore */
    private void persistAppVersion() {
        // Persist app version for xdm workflow
        if (dataStore != null && deviceInfoService != null) {
            dataStore.setString(
                    LifecycleV2Constants.DataStoreKeys.LAST_APP_VERSION,
                    deviceInfoService.getApplicationVersion());
        }
    }

    /**
     * Dispatches a lifecycle application launch event onto the EventHub containing the session info
     * as xdm event data
     *
     * @param appLaunchXDMData the current session start xdm data
     * @param freeFormData additional free-form context data
     */
    private void dispatchApplicationLaunch(
            final Map<String, Object> appLaunchXDMData, final Map<String, String> freeFormData) {
        if (appLaunchXDMData == null || appLaunchXDMData.isEmpty()) {
            Log.trace(
                    LifecycleConstants.LOG_TAG,
                    SELF_LOG_TAG,
                    "Not dispatching application launch event as xdm data was null");
            return;
        }
        Map<String, Object> launchEventData = new HashMap<>();
        launchEventData.put(LifecycleV2Constants.EventDataKeys.XDM, appLaunchXDMData);
        if (freeFormData != null && !freeFormData.isEmpty()) {
            launchEventData.put(LifecycleV2Constants.EventDataKeys.DATA, freeFormData);
        }
        Event lifecycleLaunchEvent =
                new Event.Builder(
                                LifecycleV2Constants.EventName.APPLICATION_LAUNCH_EVENT,
                                EventType.LIFECYCLE,
                                EventSource.APPLICATION_LAUNCH)
                        .setEventData(launchEventData)
                        .build();
        extensionApi.dispatch(lifecycleLaunchEvent);
    }

    /**
     * Dispatches a lifecycle application close event onto the EventHub containing the session info
     * as xdm event data
     *
     * @param appCloseXDMData the current session close xdm data
     */
    private void dispatchApplicationClose(final Map<String, Object> appCloseXDMData) {
        if (appCloseXDMData == null || appCloseXDMData.isEmpty()) {
            Log.trace(
                    LifecycleConstants.LOG_TAG,
                    SELF_LOG_TAG,
                    "Not dispatching application close event as xdm data was null");
            return;
        }
        Map<String, Object> closeEventData = new HashMap<>();
        closeEventData.put(LifecycleV2Constants.EventDataKeys.XDM, appCloseXDMData);

        Event lifecycleCloseEvent =
                new Event.Builder(
                                LifecycleV2Constants.EventName.APPLICATION_CLOSE_EVENT,
                                EventType.LIFECYCLE,
                                EventSource.APPLICATION_CLOSE)
                        .setEventData(closeEventData)
                        .build();
        extensionApi.dispatch(lifecycleCloseEvent);
    }
}
