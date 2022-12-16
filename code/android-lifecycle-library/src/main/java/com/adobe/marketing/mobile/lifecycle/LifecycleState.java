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

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.services.DeviceInforming;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.NamedCollection;
import com.adobe.marketing.mobile.util.StringUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

// Manages the business logic of the Lifecycle extension
class LifecycleState {

    private static final String SELF_LOG_TAG = "LifecycleState";

    private final NamedCollection namedCollection;
    private final DeviceInforming deviceInfoService;
    private final LifecycleSession lifecycleSession;
    private final Map<String, String> lifecycleContextData = new HashMap<>();
    private final Map<String, String> previousSessionLifecycleContextData = new HashMap<>();

    LifecycleState(final NamedCollection namedCollection, final DeviceInforming deviceInfoService) {
        this.namedCollection = namedCollection;
        this.deviceInfoService = deviceInfoService;
        lifecycleSession = new LifecycleSession(namedCollection);
    }

    Map<String, String> computeBootData() {
        Map<String, String> contextData = new HashMap<>();
        Map<String, String> currentContextData = getContextData();

        if (currentContextData != null) {
            contextData.putAll(currentContextData);
        }

        Map<String, String> defaultData =
                new LifecycleMetricsBuilder(
                                deviceInfoService,
                                namedCollection,
                                TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()))
                        .addCoreData()
                        .addGenericData()
                        .build();
        contextData.putAll(defaultData);
        return contextData;
    }

    /**
     * Starts a new lifecycle session at the given date with the provided data
     *
     * @param startTimestampInSeconds start time for the event
     * @param additionalContextData additional context data for this start event
     * @param advertisingIdentifier The advertising identifier provided by the identity extension
     * @param sessionTimeoutInSeconds The session timeout for this start event,
     * @param isInstall Indicates whether this is an application install scenario
     * @return Instance of {@code LifecycleSession.SessionInfo} having the previous session info if
     *     it exists otherwise null
     */
    LifecycleSession.SessionInfo start(
            final long startTimestampInSeconds,
            final Map<String, String> additionalContextData,
            final String advertisingIdentifier,
            final long sessionTimeoutInSeconds,
            final boolean isInstall) {
        String previousOsVersion = "";
        String previousAppId = "";
        if (namedCollection != null) {
            previousOsVersion =
                    namedCollection.getString(LifecycleConstants.DataStoreKeys.OS_VERSION, "");
            previousAppId = namedCollection.getString(LifecycleConstants.DataStoreKeys.APP_ID, "");
        }

        LifecycleMetricsBuilder metricsBuilder =
                new LifecycleMetricsBuilder(
                        deviceInfoService, namedCollection, startTimestampInSeconds);
        Map<String, String> defaultData = metricsBuilder.addCoreData().addGenericData().build();

        if (!isInstall) {
            checkForApplicationUpgrade(
                    defaultData.get(LifecycleConstants.EventDataKeys.Lifecycle.APP_ID));
        }

        LifecycleSession.SessionInfo previousSessionInfo =
                lifecycleSession.start(
                        startTimestampInSeconds, sessionTimeoutInSeconds, defaultData);

        if (previousSessionInfo == null) {
            return null;
        }

        lifecycleContextData.clear();

        Map<String, String> lifecycleData = new HashMap<>();

        // determine config type (install, upgrade, or launch)
        if (isInstall) { // install hit
            metricsBuilder.addInstallData().addGenericData().addCoreData();
            lifecycleData.putAll(metricsBuilder.build());
        } else { // upgrade and launch hits
            metricsBuilder
                    .addLaunchData()
                    .addUpgradeData(isUpgrade())
                    .addCrashData(previousSessionInfo.isCrash())
                    .addGenericData()
                    .addCoreData();
            lifecycleData.putAll(metricsBuilder.build());

            Map<String, String> sessionContextData =
                    lifecycleSession.getSessionData(
                            startTimestampInSeconds, sessionTimeoutInSeconds, previousSessionInfo);

            lifecycleData.putAll(sessionContextData);

            if (!StringUtils.isNullOrEmpty(previousOsVersion)) {
                lifecycleData.put(
                        LifecycleConstants.EventDataKeys.Lifecycle.PREVIOUS_OS_VERSION,
                        previousOsVersion);
            }
            if (!StringUtils.isNullOrEmpty(previousAppId)) {
                lifecycleData.put(
                        LifecycleConstants.EventDataKeys.Lifecycle.PREVIOUS_APP_ID, previousAppId);
            }
        }

        if (additionalContextData != null) {
            lifecycleData.putAll(additionalContextData);
        }

        if (!StringUtils.isNullOrEmpty(advertisingIdentifier)) {
            lifecycleData.put(
                    LifecycleConstants.EventDataKeys.Identity.ADVERTISING_IDENTIFIER,
                    advertisingIdentifier);
        }

        // Update lifecycle context data and persist lifecycle info into local storage
        lifecycleContextData.putAll(lifecycleData);

        persistLifecycleContextData(startTimestampInSeconds);

        return previousSessionInfo;
    }

    /**
     * Pause the current lifecycle session
     *
     * @param event event containing lifecycle pause timestamp
     */
    void pause(final Event event) {
        lifecycleSession.pause(event.getTimestampInSeconds());
    }

    /**
     * Updates the application identifier in the local lifecycle context data in case of upgrade
     *
     * @param applicationIdentifier the application identifier
     */
    void checkForApplicationUpgrade(final String applicationIdentifier) {
        // early out if this isn't an upgrade or if it is an install
        if (!isUpgrade()) {
            return;
        }

        // get a map of lifecycle data in shared preferences or memory
        final Map<String, String> lifecycleData = getContextData();

        // no data to update
        if (lifecycleData == null || lifecycleData.isEmpty()) {
            return;
        }

        // update the version in our map
        lifecycleData.put(LifecycleConstants.EventDataKeys.Lifecycle.APP_ID, applicationIdentifier);

        if (lifecycleContextData.isEmpty()) {
            // update the previous session's map
            previousSessionLifecycleContextData.put(
                    LifecycleConstants.EventDataKeys.Lifecycle.APP_ID, applicationIdentifier);

            // update data in local storage
            if (namedCollection != null) {
                namedCollection.setMap(
                        LifecycleConstants.DataStoreKeys.LIFECYCLE_DATA, lifecycleData);
            }
        } else {
            // if we have the map in memory, update it
            lifecycleContextData.putAll(lifecycleData);
        }
    }

    /**
     * Gets lifecycle context data. If lifecycleContextData has elements, it returns a copy of the
     * existing key-value pairs Otherwise, if previousSessionLifecycleContextData has elements, it
     * returns a copy of the key-value pairs Otherwise it reads context data from persistence and
     * returns the keys
     *
     * @return {@code Map<String, String>} lifecycle context data
     */
    Map<String, String> getContextData() {
        // if we already have lifecycle data, return it
        if (!lifecycleContextData.isEmpty()) {
            return lifecycleContextData;
        }

        if (!previousSessionLifecycleContextData.isEmpty()) {
            return previousSessionLifecycleContextData;
        }

        previousSessionLifecycleContextData.putAll(getPersistedContextData());
        return previousSessionLifecycleContextData;
    }

    /**
     * Used for testing only Update lifecycle context data. Equivalent to calling
     * putAll(contextData) on existing lifecycle data
     *
     * @param contextData {@code Map<String, String>} context data to be updated
     */
    @VisibleForTesting
    void updateContextData(@NonNull final Map<String, String> contextData) {
        lifecycleContextData.putAll(contextData);
    }

    /**
     * Used for testing only Update previous session lifecycle context data. Equivalent to calling
     * putAll(contextData) on prev lifecycle data
     *
     * @param contextData {@code Map<String, String>} context data to be updated
     */
    @VisibleForTesting
    void updatePreviousSessionLifecycleContextData(@NonNull final Map<String, String> contextData) {
        previousSessionLifecycleContextData.putAll(contextData);
    }

    /**
     * Gets persisted lifecycle context data from local storage.
     *
     * @return {@code Map<String, String>} persisted lifecycle context data
     */
    Map<String, String> getPersistedContextData() {
        // if we didn't have any lifecycle data, pull what was persisted last
        if (namedCollection == null) {
            Log.warning(
                    LifecycleConstants.LOG_TAG,
                    SELF_LOG_TAG,
                    "Failed to read lifecycle data from persistence %s (DataStore)",
                    Log.UNEXPECTED_NULL_VALUE);
            return new HashMap<>();
        }
        Map<String, String> lifecycleData =
                namedCollection.getMap(LifecycleConstants.DataStoreKeys.LIFECYCLE_DATA);
        return lifecycleData != null ? lifecycleData : new HashMap<>();
    }

    /**
     * Check if the application has been upgraded
     *
     * @return true if the current app version does not equal the app version stored in the data
     *     store
     */
    private boolean isUpgrade() {
        String previousAppVersion = "";

        if (namedCollection != null) {
            previousAppVersion =
                    namedCollection.getString(LifecycleConstants.DataStoreKeys.LAST_VERSION, "");
        }

        return (deviceInfoService != null
                && !StringUtils.isNullOrEmpty(previousAppVersion)
                && !previousAppVersion.equalsIgnoreCase(deviceInfoService.getApplicationVersion()));
    }

    /**
     * Persist lifecycle context data.
     *
     * @param startTimestamp current event timestamp to be stored as last used date
     */
    private void persistLifecycleContextData(final long startTimestamp) {
        if (namedCollection == null) {
            Log.debug(
                    LifecycleConstants.LOG_TAG,
                    SELF_LOG_TAG,
                    "Failed to update lifecycle data, %s (DataStore)",
                    Log.UNEXPECTED_NULL_VALUE);
            return;
        }

        namedCollection.setMap(
                LifecycleConstants.DataStoreKeys.LIFECYCLE_DATA, lifecycleContextData);
        namedCollection.setLong(LifecycleConstants.DataStoreKeys.LAST_USED_DATE, startTimestamp);

        if (deviceInfoService != null) {
            namedCollection.setString(
                    LifecycleConstants.DataStoreKeys.LAST_VERSION,
                    deviceInfoService.getApplicationVersion());
        }
    }
}
