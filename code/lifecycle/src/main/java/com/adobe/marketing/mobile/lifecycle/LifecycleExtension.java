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
import com.adobe.marketing.mobile.EventSource;
import com.adobe.marketing.mobile.EventType;
import com.adobe.marketing.mobile.Extension;
import com.adobe.marketing.mobile.ExtensionApi;
import com.adobe.marketing.mobile.Lifecycle;
import com.adobe.marketing.mobile.SharedStateResolution;
import com.adobe.marketing.mobile.SharedStateResult;
import com.adobe.marketing.mobile.SharedStateStatus;
import com.adobe.marketing.mobile.services.DeviceInforming;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.NamedCollection;
import com.adobe.marketing.mobile.services.ServiceProvider;
import com.adobe.marketing.mobile.util.DataReader;
import java.util.Map;

/**
 * LifecycleExtension class
 *
 * <p>The responsibility of LifecycleExtension is to handle the calculation and population of a base
 * set of data within the SDK. This data will consist of information about the lifecycle of the app
 * involving launches, installs and upgrades.
 *
 * <p>This extension handles two main scenarios:
 *
 * <ul>
 *   <li>Computing standard lifecycle sessions, usually consumed by the Analytics extension
 *   <li>Computing the application launch/close XDM metrics, usually consumed by the Edge Network
 *       and related extensions
 * </ul>
 */
public class LifecycleExtension extends Extension {

    private static final String SELF_LOG_TAG = "LifecycleExtension";
    private final NamedCollection lifecycleDataStore;
    private final LifecycleV1Extension lifecycleV1;
    private final LifecycleV2Extension lifecycleV2;

    /**
     * Constructor for the LifecycleExtension, must be called by inheritors. It is called by the
     * Mobile SDK when registering the extension and it initializes the extension and registers
     * event listeners.
     *
     * @param extensionApi {@code ExtensionApi} instance
     */
    protected LifecycleExtension(final ExtensionApi extensionApi) {
        this(
                extensionApi,
                ServiceProvider.getInstance()
                        .getDataStoreService()
                        .getNamedCollection(LifecycleConstants.DATA_STORE_NAME),
                ServiceProvider.getInstance().getDeviceInfoService());
    }

    /**
     * This constructor is intended for testing purposes.
     *
     * @param extensionApi {@code ExtensionApi} instance
     * @param namedCollection {@code NamedCollection} instance
     * @param deviceInfoService {@code DeviceInforming} instance
     */
    @VisibleForTesting
    protected LifecycleExtension(
            final ExtensionApi extensionApi,
            final NamedCollection namedCollection,
            final DeviceInforming deviceInfoService) {
        this(
                extensionApi,
                namedCollection,
                new LifecycleV1Extension(namedCollection, deviceInfoService, extensionApi),
                new LifecycleV2Extension(namedCollection, deviceInfoService, extensionApi));
    }

    /**
     * This constructor is intended for testing purposes.
     *
     * @param extensionApi {@code ExtensionApi} instance
     * @param namedCollection {@code NamedCollection} instance
     * @param lifecycleV1Extension {@code LifecycleV1Extension} instance
     * @param lifecycleV2Extension {@code LifecycleV2Extension} instance
     */
    @VisibleForTesting
    protected LifecycleExtension(
            final ExtensionApi extensionApi,
            final NamedCollection namedCollection,
            final LifecycleV1Extension lifecycleV1Extension,
            final LifecycleV2Extension lifecycleV2Extension) {
        super(extensionApi);
        lifecycleDataStore = namedCollection;
        lifecycleV1 = lifecycleV1Extension;
        lifecycleV2 = lifecycleV2Extension;
    }

    @NonNull @Override
    protected String getName() {
        return LifecycleConstants.EventDataKeys.Lifecycle.MODULE_NAME;
    }

    @Override
    protected String getVersion() {
        return Lifecycle.extensionVersion();
    }

    @Override
    protected String getFriendlyName() {
        return LifecycleConstants.FRIENDLY_NAME;
    }

    @Override
    protected void onRegistered() {
        getApi().registerEventListener(
                        EventType.GENERIC_LIFECYCLE,
                        EventSource.REQUEST_CONTENT,
                        this::handleLifecycleRequestEvent);
        getApi().registerEventListener(
                        EventType.WILDCARD, EventSource.WILDCARD, this::updateLastKnownTimestamp);
        lifecycleV1.onRegistered();
    }

    @Override
    public boolean readyForEvent(final Event event) {
        if (event.getType().equalsIgnoreCase(EventType.GENERIC_LIFECYCLE)
                && event.getSource().equalsIgnoreCase(EventSource.REQUEST_CONTENT)) {
            SharedStateResult configurationSharedState =
                    getApi().getSharedState(
                                    LifecycleConstants.EventDataKeys.Configuration.MODULE_NAME,
                                    event,
                                    false,
                                    SharedStateResolution.ANY);
            return configurationSharedState != null
                    && configurationSharedState.getStatus() == SharedStateStatus.SET;
        }
        return true;
    }

    /**
     * Processes an event of type generic lifecycle and source request content
     *
     * @param event lifecycle request content {@code Event}
     */
    void handleLifecycleRequestEvent(final Event event) {
        SharedStateResult configurationSharedState =
                getApi().getSharedState(
                                LifecycleConstants.EventDataKeys.Configuration.MODULE_NAME,
                                event,
                                false,
                                SharedStateResolution.ANY);

        if (configurationSharedState == null
                || configurationSharedState.getStatus() == SharedStateStatus.PENDING) {
            Log.trace(
                    LifecycleConstants.LOG_TAG,
                    SELF_LOG_TAG,
                    "Waiting for configuration to process lifecycle request event");
            return;
        }

        Map<String, Object> eventData = event.getEventData();

        if (eventData == null) {
            Log.trace(
                    LifecycleConstants.LOG_TAG,
                    SELF_LOG_TAG,
                    "Failed to process lifecycle event '%s for event data'",
                    Log.UNEXPECTED_NULL_VALUE);
            return;
        }

        String lifecycleAction =
                DataReader.optString(
                        eventData,
                        LifecycleConstants.EventDataKeys.Lifecycle.LIFECYCLE_ACTION_KEY,
                        "");

        if (LifecycleConstants.EventDataKeys.Lifecycle.LIFECYCLE_START.equals(lifecycleAction)) {
            Log.debug(LifecycleConstants.LOG_TAG, SELF_LOG_TAG, "Starting lifecycle");
            startApplicationLifecycle(event, configurationSharedState.getValue());
        } else if (LifecycleConstants.EventDataKeys.Lifecycle.LIFECYCLE_PAUSE.equals(
                lifecycleAction)) {
            Log.debug(LifecycleConstants.LOG_TAG, SELF_LOG_TAG, "Pausing lifecycle");
            pauseApplicationLifecycle(event);
        } else {
            Log.warning(
                    LifecycleConstants.LOG_TAG,
                    SELF_LOG_TAG,
                    "Invalid action for lifecycle request event");
        }
    }

    /**
     * Updates the last known event timestamp in cache and if needed in persistence
     *
     * @param event to be processed; should not be null
     */
    void updateLastKnownTimestamp(final Event event) {
        lifecycleV2.updateLastKnownTimestamp(event);
    }

    /**
     * Start the lifecycle session for standard and XDM workflows
     *
     * @param event current lifecycle request event with start action
     * @param configurationSharedState configuration shared state data for this event
     */
    private void startApplicationLifecycle(
            final Event event, final Map<String, Object> configurationSharedState) {
        boolean isInstall = isInstall();
        lifecycleV1.start(event, configurationSharedState, isInstall);
        lifecycleV2.start(event, isInstall);
        if (isInstall) {
            persistInstallDate(event);
        }
    }

    /**
     * Pause the lifecycle session for standard and XDM workflows
     *
     * @param event current lifecycle request event with pause action
     */
    private void pauseApplicationLifecycle(final Event event) {
        lifecycleV1.pause(event);
        lifecycleV2.pause(event);
    }

    /**
     * Check if install has been processed
     *
     * @return true if there is no install date stored in the data store
     */
    private boolean isInstall() {
        return (lifecycleDataStore != null
                && !lifecycleDataStore.contains(LifecycleConstants.DataStoreKeys.INSTALL_DATE));
    }

    /**
     * Persist Application install date.
     *
     * @param event lifecycle start event.
     */
    private void persistInstallDate(final Event event) {
        if (lifecycleDataStore == null) {
            return;
        }

        final long startTimestampInSeconds = event.getTimestampInSeconds();
        lifecycleDataStore.setLong(
                LifecycleConstants.DataStoreKeys.INSTALL_DATE, startTimestampInSeconds);
    }
}
