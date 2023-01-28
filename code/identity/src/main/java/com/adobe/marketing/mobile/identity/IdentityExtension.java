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

package com.adobe.marketing.mobile.identity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.EventSource;
import com.adobe.marketing.mobile.EventType;
import com.adobe.marketing.mobile.Extension;
import com.adobe.marketing.mobile.ExtensionApi;
import com.adobe.marketing.mobile.Identity;
import com.adobe.marketing.mobile.MobilePrivacyStatus;
import com.adobe.marketing.mobile.SharedStateResolution;
import com.adobe.marketing.mobile.SharedStateResult;
import com.adobe.marketing.mobile.SharedStateStatus;
import com.adobe.marketing.mobile.VisitorID;
import com.adobe.marketing.mobile.identity.IdentityConstants.DataStoreKeys;
import com.adobe.marketing.mobile.identity.IdentityConstants.Defaults;
import com.adobe.marketing.mobile.internal.util.VisitorIDSerializer;
import com.adobe.marketing.mobile.services.DataQueue;
import com.adobe.marketing.mobile.services.HitQueuing;
import com.adobe.marketing.mobile.services.HttpMethod;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.NamedCollection;
import com.adobe.marketing.mobile.services.NetworkRequest;
import com.adobe.marketing.mobile.services.Networking;
import com.adobe.marketing.mobile.services.PersistentHitQueue;
import com.adobe.marketing.mobile.services.ServiceProvider;
import com.adobe.marketing.mobile.util.DataReader;
import com.adobe.marketing.mobile.util.SQLiteUtils;
import com.adobe.marketing.mobile.util.StringUtils;
import com.adobe.marketing.mobile.util.TimeUtils;
import com.adobe.marketing.mobile.util.URLBuilder;
import com.adobe.marketing.mobile.util.UrlUtils;
import java.net.HttpURLConnection;
import java.util.*;

/**
 * IdentityExtension class is responsible for interactions with the ECID Service
 *
 * <p>The IdentityExtension handles the following use cases:
 *
 * <ol>
 *   <li>Syncing new visitor identifiers to the ECID Service
 *   <li>Modifying a base URL and returning with ECID Service values to facilitate hybrid app
 *       communication
 *   <li>Returning a list of known identifiers for the user
 * </ol>
 *
 * <p>The IdentityExtension listens for the following {@link Event}s:
 *
 * <ol>
 *   <li>{@code EventType.HUB} - {@link EventSource#SHARED_STATE}
 *   <li>{@link EventType#IDENTITY} - {@link EventSource#REQUEST_IDENTITY}
 * </ol>
 *
 * <p>The IdentityExtension dispatches the following {@code Events}:
 *
 * <ol>
 *   <li>{@link EventType#ANALYTICS} - {@link EventSource#REQUEST_CONTENT}
 *   <li>{@code EventType.IDENTITY} - {@link EventSource#RESPONSE_IDENTITY}
 *   <li>{@code EventType.CONFIGURATION} - {@link EventSource#REQUEST_CONTENT}
 * </ol>
 *
 * <p>
 */
public final class IdentityExtension extends Extension {

    private static final String LOG_SOURCE = "IdentityExtension";
    private HitQueuing hitQueue;
    private static boolean pushEnabled = false;
    private static final Object pushEnabledMutex = new Object();
    private ConfigurationSharedStateIdentity latestValidConfig;
    private final NamedCollection namedCollection;

    private String mid;
    private String advertisingIdentifier;
    private String pushIdentifier;
    private String blob;
    private String locationHint;
    private long lastSync;
    private long ttl;
    private List<VisitorID> customerIds;
    private MobilePrivacyStatus privacyStatus = IdentityConstants.Defaults.DEFAULT_MOBILE_PRIVACY;
    private boolean hasSynced = false;
    private boolean didCreateInitialSharedState = false;

    /**
     * Construct the extension and initialize with the {@code ExtensionApi}.
     *
     * @param extensionApi the {@link ExtensionApi} this extension will use
     */
    IdentityExtension(@NonNull final ExtensionApi extensionApi) {
        this(
                extensionApi,
                ServiceProvider.getInstance()
                        .getDataStoreService()
                        .getNamedCollection(DataStoreKeys.IDENTITY_PROPERTIES_DATA_STORE_NAME),
                null);
    }

    @VisibleForTesting
    IdentityExtension(
            @NonNull final ExtensionApi extensionApi,
            @Nullable final NamedCollection namedCollection,
            @Nullable final HitQueuing hitQueue) {
        super(extensionApi);
        this.namedCollection = namedCollection;
        this.hitQueue = hitQueue;
    }

    @NonNull @Override
    protected String getName() {
        return IdentityConstants.EXTENSION_NAME;
    }

    @NonNull @Override
    protected String getFriendlyName() {
        return IdentityConstants.FRIENDLY_NAME;
    }

    @NonNull @Override
    protected String getVersion() {
        return Identity.extensionVersion();
    }

    @Override
    protected void onRegistered() {
        // listen to Identity requests
        getApi().registerEventListener(
                        EventType.IDENTITY,
                        EventSource.REQUEST_IDENTITY,
                        this::processIdentityRequest);
        getApi().registerEventListener(
                        EventType.IDENTITY,
                        EventSource.RESPONSE_IDENTITY,
                        this::handleIdentityResponseIdentityForSharedState);
        getApi().registerEventListener(
                        EventType.GENERIC_IDENTITY,
                        EventSource.REQUEST_CONTENT,
                        this::processIdentityRequest);
        getApi().registerEventListener(
                        EventType.GENERIC_IDENTITY,
                        EventSource.REQUEST_RESET,
                        this::processIdentityRequest);
        // listen to Analytics response
        getApi().registerEventListener(
                        EventType.ANALYTICS,
                        EventSource.RESPONSE_IDENTITY,
                        this::handleAnalyticsResponseIdentity);
        // listen to AudienceManager response
        getApi().registerEventListener(
                        EventType.AUDIENCEMANAGER,
                        EventSource.RESPONSE_CONTENT,
                        this::processAudienceResponse);
        // listen to Configuration response
        getApi().registerEventListener(
                        EventType.CONFIGURATION,
                        EventSource.RESPONSE_CONTENT,
                        this::handleConfiguration);
        boot();
    }

    @Override
    protected void onUnregistered() {
        hitQueue.close();
    }

    @Override
    public boolean readyForEvent(@NonNull final Event event) {

        if (!forceSyncIdentifiers(event)) {
            return false;
        }

        // Returns true if the event is either getExperienceCloudId event or getIdentifiers event
        if (event.getType().equals(EventType.IDENTITY)
                && event.getSource().equals(EventSource.REQUEST_IDENTITY)
                && (event.getEventData() == null || event.getEventData().isEmpty())) {
            return true;
        }

        if (isSyncEvent(event)) {
            return readyForSyncIdentifiers(event);
        }

        if (isAppendUrlEvent(event) || isGetUrlVarsEvent(event)) {
            if (!hasValidSharedState(
                    IdentityConstants.EventDataKeys.Analytics.MODULE_NAME, event)) {
                Log.trace(
                        IdentityConstants.LOG_TAG,
                        LOG_SOURCE,
                        "Waiting for the Analytics shared state to get required configuration"
                                + " fields before processing [event: %s].",
                        event.getName());
                return false;
            }
        }

        if (!hasValidSharedState(
                IdentityConstants.EventDataKeys.Configuration.MODULE_NAME, event)) {
            Log.trace(
                    IdentityConstants.LOG_TAG,
                    LOG_SOURCE,
                    "Waiting for the Configuration shared state to get required configuration"
                            + " fields before processing [event: %s].",
                    event.getName());
            return false;
        } else {
            return true;
        }
    }

    @VisibleForTesting
    boolean readyForSyncIdentifiers(final Event event) {
        Map<String, Object> configuration =
                getApi().getSharedState(
                                IdentityConstants.EventDataKeys.Configuration.MODULE_NAME,
                                event,
                                false,
                                SharedStateResolution.LAST_SET)
                        .getValue();
        String orgId =
                DataReader.optString(
                        configuration,
                        IdentityConstants.EventDataKeys.Configuration
                                .CONFIG_EXPERIENCE_CLOUD_ORGID_KEY,
                        "");
        return !orgId.isEmpty();
    }

    @VisibleForTesting
    boolean forceSyncIdentifiers(@NonNull final Event event) {
        if (hasSynced) {
            return true;
        }

        SharedStateResult configState =
                getApi().getSharedState(
                        IdentityConstants.EventDataKeys.Configuration.MODULE_NAME,
                        null,
                        false,
                        SharedStateResolution.LAST_SET);
        if (configState == null || configState.getStatus() != SharedStateStatus.SET) {
            return false;
        }

        if (!readyForSyncIdentifiers(event)) {
            return false;
        }

        // Get privacy status from configuration, set global "privacyStatus" variable, update hit queue
        loadPrivacyStatusFromConfigurationState(configState.getValue());
        hitQueue.handlePrivacyChange(privacyStatus);

        Map<String, Object> configuration = configState.getValue();

        ConfigurationSharedStateIdentity configSharedState = new ConfigurationSharedStateIdentity();
        configSharedState.getConfigurationProperties(configuration);

        hasSynced = handleSyncIdentifiers(event, configSharedState, true) || MobilePrivacyStatus.OPT_OUT.equals(privacyStatus);

        if (hasSynced && !didCreateInitialSharedState) {
            getApi().createSharedState(packageEventData(), event);
            didCreateInitialSharedState = true;
        }

        return hasSynced;
    }

    private boolean hasValidSharedState(final String extensionName, final Event event) {
        SharedStateResult sharedStateResult =
                getApi().getSharedState(
                                extensionName, event, false, SharedStateResolution.LAST_SET);
        if (sharedStateResult == null || sharedStateResult.getStatus() != SharedStateStatus.SET) {
            return false;
        }
        Map<String, Object> sharedStateValue = sharedStateResult.getValue();
        return sharedStateValue != null && !sharedStateValue.isEmpty();
    }

    private void boot() {
        loadVariablesFromPersistentData();
        deleteDeprecatedV5HitDatabase();
        initializeHitQueueDatabase();
        if (!StringUtils.isNullOrEmpty(mid)) {
            getApi().createSharedState(packageEventData(), null);
            didCreateInitialSharedState = true;
        }
    }

    /** Delete the deprecated V5 hit database file if exists */
    private void deleteDeprecatedV5HitDatabase() {
        SQLiteUtils.deleteDBFromCacheDir(IdentityConstants.DEPRECATED_1X_HIT_DATABASE_FILENAME);
    }

    /**
     * This method creates an instance of the database if one does not exist already and sets this
     * IdentityExtension's {@code MobilePrivacyStatus}
     */
    private void initializeHitQueueDatabase() {
        if (hitQueue == null) {
            DataQueue dataQueue =
                    ServiceProvider.getInstance()
                            .getDataQueueService()
                            .getDataQueue(IdentityConstants.EXTENSION_NAME);
            hitQueue = new PersistentHitQueue(dataQueue, new IdentityHitsProcessing(this));
        }
    }

    /**
     * Handles {@code Configuration} event passed on by the {@code Listener}
     *
     * <p>
     *
     * <p>If the {@link MobilePrivacyStatus} is {@link MobilePrivacyStatus#OPT_OUT} then an opt out
     * hit is potentially sent to the identity server.
     *
     * @param configurationEvent {@code Configuration} event to be processed
     */
    void handleConfiguration(final Event configurationEvent) {
        if (configurationEvent == null) {
            return;
        }

        Map<String, Object> configuration = configurationEvent.getEventData();

        if (configuration == null) {
            return;
        }

        MobilePrivacyStatus mobilePrivacyStatus =
                MobilePrivacyStatus.fromString(
                        DataReader.optString(
                                configuration,
                                IdentityConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY,
                                Defaults.DEFAULT_MOBILE_PRIVACY.getValue()));
        hitQueue.handlePrivacyChange(mobilePrivacyStatus);
        if (mobilePrivacyStatus.equals(MobilePrivacyStatus.OPT_OUT)) {
            handleOptOut(configuration);
        }

        // if config contains a new global privacy change, process the event
        // Do this after calling handleOptOut; clearing the identifiers before will cause
        // handleOptOut to fail
        processPrivacyChange(configurationEvent, configuration);

        updateLatestValidConfiguration(configuration);
    }

    /**
     * Handler for {@code EventType.ANALYTICS} {@code EventSource.RESPONSE_IDENTITY} events.
     * Extracts the Analytics ID from the {@code event} and adds a sync event to the event queue.
     *
     * @param event {@link Event} containing the {@link
     *     IdentityConstants.EventDataKeys.Analytics#ANALYTICS_ID}
     */
    void handleAnalyticsResponseIdentity(final Event event) {
        if (event == null) {
            return;
        }

        Map<String, Object> data = event.getEventData();

        if (data == null) {
            return;
        }

        String aid =
                DataReader.optString(
                        data, IdentityConstants.EventDataKeys.Analytics.ANALYTICS_ID, null);

        if (StringUtils.isNullOrEmpty(aid)) {
            return;
        }

        if (namedCollection == null) {
            return;
        }

        if (namedCollection.contains(DataStoreKeys.AID_SYNCED_KEY)) {
            return;
        } else {
            namedCollection.setBoolean(DataStoreKeys.AID_SYNCED_KEY, true);
        }

        Map<String, String> identifiers = new HashMap<>();
        identifiers.put(IdentityConstants.EventDataKeys.Identity.ANALYTICS_ID, aid);

        final Map<String, Object> syncData = new HashMap<>();
        syncData.put(IdentityConstants.EventDataKeys.Identity.IDENTIFIERS, identifiers);
        syncData.put(
                IdentityConstants.EventDataKeys.Identity.AUTHENTICATION_STATE,
                VisitorID.AuthenticationState.UNKNOWN.getValue());
        syncData.put(IdentityConstants.EventDataKeys.Identity.FORCE_SYNC, false);
        syncData.put(IdentityConstants.EventDataKeys.Identity.IS_SYNC_EVENT, true);

        Event avidEvent =
                new Event.Builder("AVID Sync", EventType.IDENTITY, EventSource.REQUEST_IDENTITY)
                        .setEventData(syncData)
                        .build();
        getApi().dispatch(avidEvent);
    }

    /**
     * Creates a new shared state with latest in-memory data.
     *
     * @param event the Identity ResponseIdentity event that indicates if a shared state update is
     *     required. The event number will be used for shared state update.
     */
    void handleIdentityResponseIdentityForSharedState(final Event event) {
        if (event == null) {
            return;
        }

        Map<String, Object> data = event.getEventData();

        if (data == null
                || !DataReader.optBoolean(
                        data,
                        IdentityConstants.EventDataKeys.Identity.UPDATE_SHARED_STATE,
                        false)) {
            return;
        }
        getApi().createSharedState(packageEventData(), event);
    }

    /**
     * Updates this extension's configuration with the latest {@code Configuration} data which
     * contains a valid Experience Cloud organization ID.
     *
     * @param data EventData containing the {@code Configuration} data
     * @see ConfigurationSharedStateIdentity
     */
    void updateLatestValidConfiguration(final Map<String, Object> data) {
        String orgId =
                DataReader.optString(
                        data,
                        IdentityConstants.EventDataKeys.Configuration
                                .CONFIG_EXPERIENCE_CLOUD_ORGID_KEY,
                        null);

        if (!StringUtils.isNullOrEmpty(orgId)) {
            latestValidConfig = new ConfigurationSharedStateIdentity();
            latestValidConfig.getConfigurationProperties(data);
        }
    }

    /**
     * Handles the reset request by resetting all the persisted properties and generating a new ECID
     * as a result of a force sync.
     *
     * @param event the request request {@link Event}
     */
    void handleIdentityRequestReset(final Event event) {
        if (event == null) {
            Log.debug(
                    IdentityConstants.LOG_TAG,
                    LOG_SOURCE,
                    LOG_SOURCE,
                    "handleIdentityRequestReset: Ignoring null event");
            return;
        }

        if (privacyStatus == MobilePrivacyStatus.OPT_OUT) {
            Log.debug(
                    IdentityConstants.LOG_TAG,
                    LOG_SOURCE,
                    "handleIdentityRequestReset: Privacy is opt-out, ignoring event.");
            return;
        }
        mid = null;
        advertisingIdentifier = null;
        blob = null;
        locationHint = null;
        customerIds = null;
        pushIdentifier = null;

        if (namedCollection != null) {
            namedCollection.remove(DataStoreKeys.AID_SYNCED_KEY);
            namedCollection.remove(DataStoreKeys.PUSH_ENABLED);
        }

        savePersistently(); // clear datastore

        // When resetting identifiers, need to generate new Experience Cloud ID for the user
        ConfigurationSharedStateIdentity configSharedState = new ConfigurationSharedStateIdentity();

        SharedStateResult configState =
                getApi().getSharedState(
                        IdentityConstants.EventDataKeys.Configuration.MODULE_NAME,
                        event,
                        false,
                        SharedStateResolution.LAST_SET);
        if (configState != null) {
            configSharedState.getConfigurationProperties(configState.getValue());
        }

        if (handleSyncIdentifiers(event, configSharedState, true)) {
            getApi().createSharedState(packageEventData(), event);
        }
    }

    /**
     * Process the {@code Audience} {@code Response_Content} event.
     *
     * <p>If the event contains the {@link
     * IdentityConstants.EventDataKeys.Audience#OPTED_OUT_HIT_SENT} flag set to false, then and opt
     * out hit is sent out to the IdentityExtension server configured. This hit is sent only if the
     * current configuration shared state has {@link
     * IdentityConstants.EventDataKeys.Configuration#GLOBAL_CONFIG_PRIVACY} set to {@link
     * MobilePrivacyStatus#OPT_OUT}. If not, then nothing is done.
     *
     * @param audienceEvent The trigger event
     */
    void processAudienceResponse(final Event audienceEvent) {
        if (audienceEvent == null) {
            return;
        }

        Map<String, Object> data = audienceEvent.getEventData();

        if (data == null) {
            return;
        }

        if (data.containsKey(IdentityConstants.EventDataKeys.Audience.OPTED_OUT_HIT_SENT)) {
            boolean optOutHitSent =
                    DataReader.optBoolean(
                            data,
                            IdentityConstants.EventDataKeys.Audience.OPTED_OUT_HIT_SENT,
                            false);

            if (optOutHitSent) {
                return;
            }

            // IdentityExtension needs to send the hit since AAM did not
            SharedStateResult identitySharedState =
                    getApi().getSharedState(
                                    IdentityConstants.EventDataKeys.Configuration.MODULE_NAME,
                                    audienceEvent,
                                    false,
                                    SharedStateResolution.ANY);

            if (identitySharedState == null
                    || identitySharedState.getStatus() != SharedStateStatus.SET) {
                Log.trace(
                        IdentityConstants.LOG_TAG,
                        LOG_SOURCE,
                        "processAudienceResponse : Unable to process the Identity events in the"
                            + " event queue because the configuration shared state is pending.");
                return;
            }

            // Make sure that the configuration shared state at this point has not changed the
            // privacy status
            ConfigurationSharedStateIdentity configSharedState =
                    new ConfigurationSharedStateIdentity();
            configSharedState.getConfigurationProperties(identitySharedState.getValue());

            if (configSharedState.privacyStatus.equals(MobilePrivacyStatus.OPT_OUT)) {
                sendOptOutHit(configSharedState);
            }
        }
    }

    /**
     * Send an opt out hit to the IdentityExtension servers
     *
     * @param configSharedState The current configuration shared state
     * @see #buildOptOutURLString(ConfigurationSharedStateIdentity)
     */
    @VisibleForTesting
    void sendOptOutHit(final ConfigurationSharedStateIdentity configSharedState) {
        String optOutUrl = buildOptOutURLString(configSharedState);

        if (StringUtils.isNullOrEmpty(optOutUrl)) {
            Log.debug(
                    IdentityConstants.LOG_TAG,
                    LOG_SOURCE,
                    "sendOptOutHit : Unable to send network hit because the opt-out URL was null.");
            return;
        }
        Networking networkService = ServiceProvider.getInstance().getNetworkService();
        if (networkService == null) {
            Log.debug(
                    IdentityConstants.LOG_TAG,
                    LOG_SOURCE,
                    "sendOptOutHit : Unable to send network request to the opt-out URL (%s)"
                            + " because NetworkService is unavailable.",
                    optOutUrl);
            return;
        }

        Log.debug(
                IdentityConstants.LOG_TAG,
                LOG_SOURCE,
                "sendOptOutHit : Sending network request to the opt-out URL: (%s).",
                optOutUrl);

        NetworkRequest networkRequest =
                new NetworkRequest(
                        optOutUrl,
                        HttpMethod.GET,
                        null,
                        null,
                        IdentityConstants.Defaults.TIMEOUT,
                        IdentityConstants.Defaults.TIMEOUT);
        networkService.connectAsync(
                networkRequest,
                connection -> {
                    if (connection == null) {
                        return;
                    }

                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        Log.trace(
                                IdentityConstants.LOG_TAG,
                                LOG_SOURCE,
                                "sendOptOutHit - Successfully sent the opt-out hit.");
                    } else {
                        Log.trace(
                                IdentityConstants.LOG_TAG,
                                LOG_SOURCE,
                                "sendOptOutHit - Failed to send the opt-out hit with connection"
                                        + " status (%s).",
                                connection.getResponseCode());
                    }

                    connection.close();
                });
    }

    /**
     * Loads values persisted in {@link NamedCollection}
     *
     * <p>Returns early without setting variables if LocalStorageService is unavailable
     */
    @VisibleForTesting
    void loadVariablesFromPersistentData() {
        if (namedCollection == null) {
            Log.debug(
                    IdentityConstants.LOG_TAG,
                    LOG_SOURCE,
                    "loadVariablesFromPersistentData : Unable to load the Identity data from"
                            + " persistence because the LocalStorageService was null.");
            return;
        }
        mid = namedCollection.getString(IdentityConstants.DataStoreKeys.MARKETING_CLOUD_ID, null);

        // reload the customer ids.
        final List<VisitorID> newCustomerIDs =
                convertVisitorIdsStringToVisitorIDObjects(
                        namedCollection.getString(
                                IdentityConstants.DataStoreKeys.VISITOR_IDS_STRING, null));

        customerIds = newCustomerIDs == null || newCustomerIDs.isEmpty() ? null : newCustomerIDs;
        int customerIdsSize =
                newCustomerIDs == null || newCustomerIDs.isEmpty() ? 0 : customerIds.size();
        Log.trace(
                IdentityConstants.LOG_TAG,
                LOG_SOURCE,
                "Load the store VisitorIDs from persistence: size = %s",
                customerIdsSize);
        locationHint =
                namedCollection.getString(IdentityConstants.DataStoreKeys.LOCATION_HINT, null);
        blob = namedCollection.getString(IdentityConstants.DataStoreKeys.BLOB, null);
        ttl =
                namedCollection.getLong(
                        IdentityConstants.DataStoreKeys.TTL,
                        IdentityConstants.Defaults.DEFAULT_TTL_VALUE);
        lastSync = namedCollection.getLong(IdentityConstants.DataStoreKeys.LAST_SYNC, 0);
        advertisingIdentifier =
                namedCollection.getString(
                        IdentityConstants.DataStoreKeys.ADVERTISING_IDENTIFIER, null);
        pushIdentifier =
                namedCollection.getString(IdentityConstants.DataStoreKeys.PUSH_IDENTIFIER, null);
        Log.trace(
                IdentityConstants.LOG_TAG,
                LOG_SOURCE,
                "loadVariablesFromPersistentData : Successfully loaded the Identity data from"
                        + " persistence.");
    }

    @VisibleForTesting
    boolean isSyncEvent(@NonNull final Event event) {
        return (DataReader.optBoolean(
                        event.getEventData(),
                        IdentityConstants.EventDataKeys.Identity.IS_SYNC_EVENT,
                        false)
                || event.getType().equals(EventType.GENERIC_IDENTITY));
    }

    private boolean isResetIdentityEvent(@NonNull final Event event) {
        return (event.getType().equals(EventType.GENERIC_IDENTITY)
                && event.getSource().equals(EventSource.REQUEST_RESET));
    }

    @VisibleForTesting
    boolean isAppendUrlEvent(@NonNull final Event event) {
        return (event.getEventData() != null
                && event.getEventData()
                        .containsKey(IdentityConstants.EventDataKeys.Identity.BASE_URL));
    }

    @VisibleForTesting
    boolean isGetUrlVarsEvent(@NonNull final Event event) {
        return DataReader.optBoolean(
                event.getEventData(),
                IdentityConstants.EventDataKeys.Identity.URL_VARIABLES,
                false);
    }

    /**
     * Marshals an {@link Event} and passes it to the correct method depending upon its EventData
     *
     * @param event {@code Event} to be marshaled
     */
    void processIdentityRequest(@NonNull final Event event) {

        if (event.getType().equals(EventType.IDENTITY)
                && event.getSource().equals(EventSource.REQUEST_IDENTITY)
                && (event.getEventData() == null || event.getEventData().isEmpty())) {
            handleIdentityResponseEvent(
                    "IDENTITY_RESPONSE_CONTENT_ONE_TIME", packageEventData(), event);
            return;
        }

        SharedStateResult result =
                getApi().getSharedState(
                                IdentityConstants.EventDataKeys.Configuration.MODULE_NAME,
                                event,
                                false,
                                SharedStateResolution.LAST_SET);
        if (result == null) {
            return;
        }

        Map<String, Object> configuration = result.getValue();

        ConfigurationSharedStateIdentity configSharedState = new ConfigurationSharedStateIdentity();
        configSharedState.getConfigurationProperties(configuration);

        Log.trace(
                IdentityConstants.LOG_TAG,
                LOG_SOURCE,
                "processEvent : Processing the Identity event: %s",
                event);

        if (isResetIdentityEvent(event)) {
            handleIdentityRequestReset(event);
        } else if (isSyncEvent(event) || event.getType().equals(EventType.GENERIC_IDENTITY)) {
            if (handleSyncIdentifiers(event, configSharedState, false)) {
                getApi().createSharedState(packageEventData(), event);
            }
        } else if (isAppendUrlEvent(event)) {
            SharedStateResult sharedStateResult =
                    getApi().getSharedState(
                                    IdentityConstants.EventDataKeys.Analytics.MODULE_NAME,
                                    event,
                                    false,
                                    SharedStateResolution.LAST_SET);
            Map<String, Object> analyticsSharedState = null;
            if (sharedStateResult != null) {
                analyticsSharedState = sharedStateResult.getValue();
            }
            handleAppendURL(event, configSharedState, analyticsSharedState);
        } else if (isGetUrlVarsEvent(event)) {
            SharedStateResult sharedStateResult =
                    getApi().getSharedState(
                                    IdentityConstants.EventDataKeys.Analytics.MODULE_NAME,
                                    event,
                                    false,
                                    SharedStateResolution.LAST_SET);
            Map<String, Object> analyticsSharedState = null;
            if (sharedStateResult != null) {
                analyticsSharedState = sharedStateResult.getValue();
            }

            handleGetUrlVariables(event, configSharedState, analyticsSharedState);
        }
    }

    void handleOptOut(final Map<String, Object> configuration) {
        // If the AAM server is configured let AAM handle opt out, else we send the opt out hit
        if (!configuration.containsKey(
                IdentityConstants.EventDataKeys.Configuration.AAM_CONFIG_SERVER)) {
            // Otherwise, check to see if currently we are still opt_out, and if so, send the OPT
            // OUT hit
            ConfigurationSharedStateIdentity configSharedState =
                    new ConfigurationSharedStateIdentity();
            configSharedState.getConfigurationProperties(configuration);

            if (configSharedState.privacyStatus.equals(MobilePrivacyStatus.OPT_OUT)) {
                sendOptOutHit(configSharedState);
            }
        }
    }

    /**
     * Handler for sync identifiers calls
     *
     * <p>Calling this method will result in a Visitor ID Sync call being queued in the Identity
     * database
     *
     * @param event {@code Event} containing identifiers that need to be synced
     * @param configSharedState {@code ConfigurationSharedStateIdentity} valid for this event
     * @param forceSync
     * @return true if the identifiers were successfully processed and a shared state needs
     * to be created, false if the identifiers could not be processed at this time.
     */
    @VisibleForTesting
    boolean handleSyncIdentifiers(
            final Event event, final ConfigurationSharedStateIdentity configSharedState, boolean forceSync) {
        if (configSharedState == null) {
            // sanity check, should never get here
            Log.debug(
                    IdentityConstants.LOG_TAG,
                    LOG_SOURCE,
                    "handleSyncIdentifiers : Ignoring the Sync Identifiers call because the"
                            + " configuration was null.");
            return false;
        }

        // do not even extract any data if the config is opt_out.
        if (privacyStatus == MobilePrivacyStatus.OPT_OUT) {
            Log.debug(
                    IdentityConstants.LOG_TAG,
                    LOG_SOURCE,
                    "handleSyncIdentifiers : Ignoring the Sync Identifiers call because the"
                            + " privacy status was opt-out.");
            // did process this event but can't sync the call. Hence return true.
            return false;
        }

        if (event == null) {
            Log.debug(
                    IdentityConstants.LOG_TAG,
                    LOG_SOURCE,
                    "handleSyncIdentifiers : Ignoring the Sync Identifiers call because the event"
                            + " sent was null.");
            return false;
        }

        // org id is a requirement.
        // Use what's in current config shared state. if that's missing, check latest config.
        // if latest config doesn't have org id either, IdentityExtension can't proceed.
        ConfigurationSharedStateIdentity currentEventValidConfig;

        if (!StringUtils.isNullOrEmpty(configSharedState.orgID)) {
            currentEventValidConfig = configSharedState;
        } else {
            if (latestValidConfig != null) {
                currentEventValidConfig = latestValidConfig;
            } else {
                // can't process this event. return false to break execution loop
                Log.debug(
                        IdentityConstants.LOG_TAG,
                        LOG_SOURCE,
                        "handleSyncIdentifiers : Unable to process sync identifiers request as the"
                                + " configuration did not contain a valid Experience Cloud"
                                + " organization ID. Will attempt to process event when a valid"
                                + " configuration is received.");
                return false;
            }
        }

        // check privacy again from the configuration object
        if (currentEventValidConfig.privacyStatus == MobilePrivacyStatus.OPT_OUT) {
            Log.debug(
                    IdentityConstants.LOG_TAG,
                    LOG_SOURCE,
                    "handleSyncIdentifiers : Ignored the Sync Identifiers call because the privacy"
                            + " status was opt-out.");
            return false;
        }

        // if the marketingCloudServer is null or empty use the default server
        if (StringUtils.isNullOrEmpty(currentEventValidConfig.marketingCloudServer)) {
            currentEventValidConfig.marketingCloudServer = IdentityConstants.Defaults.SERVER;
            Log.debug(
                    IdentityConstants.LOG_TAG,
                    LOG_SOURCE,
                    "handleSyncIdentifiers : The experienceCloud.server was empty is the"
                            + " configuration, hence used the default server: (%s).",
                    currentEventValidConfig.marketingCloudServer);
        }

        final Map<String, Object> eventData = event.getEventData();

        // Extract dpId's
        final Map<String, String> dpids = extractDPID(eventData);

        // Extract identifiers
        final Map<String, String> identifiers = extractIdentifiers(eventData);

        // Extract Authentication state
        final VisitorID.AuthenticationState idState =
                VisitorID.AuthenticationState.fromInteger(
                        DataReader.optInt(
                                eventData,
                                IdentityConstants.EventDataKeys.Identity.AUTHENTICATION_STATE,
                                0));

        // Extract isForceSync
        final boolean shouldForceSync = forceSync ||
                DataReader.optBoolean(
                        eventData, IdentityConstants.EventDataKeys.Identity.FORCE_SYNC, false);

        List<VisitorID> currentCustomerIds = generateCustomerIds(identifiers, idState);

        // update adid if changed and extract the new adid value as VisitorID to be synced
        IdentityGenericPair<VisitorID, Boolean> adidPair = extractAndUpdateAdid(eventData);

        final boolean didAdidConsentChange = adidPair.getSecond();
        final VisitorID adidIdentifier = adidPair.getFirst();

        if (adidIdentifier != null) {
            currentCustomerIds.add(adidIdentifier);
        }

        // merge new identifiers with the existing ones and remove any VisitorIDs with empty id
        // values
        // empty adid is also removed from the customerIds list by merging with the new ids then
        // filtering out any empty ids
        customerIds = mergeCustomerIds(currentCustomerIds);
        customerIds = cleanupVisitorIdentifiers(customerIds);
        currentCustomerIds = cleanupVisitorIdentifiers(currentCustomerIds);

        // valid config: check if there's a need to sync. Don't if we're already up to date.
        if (shouldSync(
                currentCustomerIds,
                dpids,
                shouldForceSync || didAdidConsentChange,
                currentEventValidConfig)) {
            final String urlString =
                    buildURLString(
                            currentCustomerIds,
                            dpids,
                            currentEventValidConfig,
                            didAdidConsentChange);
            IdentityHit hit = new IdentityHit(urlString, event);
            hitQueue.queue(hit.toDataEntity());
        } else {
            // nothing to sync
            Log.debug(
                    IdentityConstants.LOG_TAG,
                    LOG_SOURCE,
                    "handleSyncIdentifiers : Ignoring ID sync because nothing new to sync after"
                            + " the last sync.");
        }

        savePersistently();

        return true; // sync successful, signal to create new shared state
    }

    /**
     * Extracts identifiers from the provided {@code eventData}.
     *
     * <p>Returns null if event data is null. If an advertising identifier is found in the event
     * data and it is not null/empty, it appends it to the returned identifiers
     *
     * @param eventData {@code EventData} containing sync identifiers
     * @return a map containing the identifiers or an empty map if event data is null or does not
     *     contain an any identifiers
     */
    @VisibleForTesting
    Map<String, String> extractIdentifiers(final Map<String, Object> eventData) {
        Map<String, String> identifiers = new HashMap<>();

        if (eventData == null
                || !eventData.containsKey(IdentityConstants.EventDataKeys.Identity.IDENTIFIERS)) {
            return identifiers;
        }

        final Map<String, String> identifiersMap =
                DataReader.optTypedMap(
                        String.class,
                        eventData,
                        IdentityConstants.EventDataKeys.Identity.IDENTIFIERS,
                        null);

        if (identifiersMap != null) {
            return identifiersMap;
        }

        return identifiers;
    }

    /**
     * Extracts the ADID from the provided {@code eventData} and updates the in-memory and persisted
     * ADID if there is any value change. The new ADID value is returned as a {@link VisitorID}
     * object to be synced along with other visitor identifiers. A boolean is returned to indicate
     * if the ADID value changed to or from a null/empty value.
     *
     * @param eventData to be processed
     * @return a {@link IdentityGenericPair} with the first element being the new ADID value
     *     extracted from the eventData or null if the eventData is null/empty or it does not
     *     contain an ADID, and the second element being a Boolean with value true if the ADID value
     *     changed to or from a null/empty value.
     */
    IdentityGenericPair<VisitorID, Boolean> extractAndUpdateAdid(
            final Map<String, Object> eventData) {
        VisitorID adidAsVisitorId = null;
        boolean didConsentChange = false;

        if (eventData == null
                || !eventData.containsKey(
                        IdentityConstants.EventDataKeys.Identity.ADVERTISING_IDENTIFIER)) {
            return new IdentityGenericPair<>(adidAsVisitorId, didConsentChange);
        }

        // Extract Advertising Identifier
        try {
            String newAdid =
                    DataReader.optString(
                            eventData,
                            IdentityConstants.EventDataKeys.Identity.ADVERTISING_IDENTIFIER,
                            "");

            // If ad id is all zeros, treat as if null/empty
            if (Defaults.ZERO_ADVERTISING_ID.equals(newAdid)) {
                newAdid = "";
            }

            // did the adid value change?
            if ((!newAdid.isEmpty() && !newAdid.equals(advertisingIdentifier))
                    || (newAdid.isEmpty() && !StringUtils.isNullOrEmpty(advertisingIdentifier))) {
                // Now we know the value changed, but did it change to/from null?
                // Handle case where advertisingIdentifier loaded from persistence with all zeros
                // and new value is not empty.
                if (newAdid.isEmpty()
                        || StringUtils.isNullOrEmpty(advertisingIdentifier)
                        || Defaults.ZERO_ADVERTISING_ID.equals(advertisingIdentifier)) {
                    didConsentChange = true;
                }

                adidAsVisitorId =
                        new VisitorID(
                                IdentityConstants.UrlKeys.VISITOR_ID,
                                IdentityConstants.EventDataKeys.Identity.ADID_DSID,
                                newAdid,
                                VisitorID.AuthenticationState.AUTHENTICATED);

                updateAdvertisingIdentifier(newAdid);
                Log.trace(
                        IdentityConstants.LOG_TAG,
                        LOG_SOURCE,
                        "extractAndUpdateAdid : The advertising identifier was set to: (%s).",
                        newAdid);
            }
        } catch (Exception e) {
            Log.error(
                    IdentityConstants.LOG_TAG,
                    LOG_SOURCE,
                    "extractAndUpdateAdid : Unable to update the advertising identifier due to:"
                            + " (%s)",
                    e);
        }

        return new IdentityGenericPair<>(adidAsVisitorId, didConsentChange);
    }

    /**
     * Appends IdentityExtension data to the provided URL and returns it for use in Hybrid app web
     * views
     *
     * @param event {@code Event} that contains the base URL
     * @param configSharedState the Identity Configuration shared state object
     */
    void handleAppendURL(
            final Event event,
            final ConfigurationSharedStateIdentity configSharedState,
            final Map<String, Object> analyticsSharedState) {
        final Map<String, Object> eventData = event.getEventData();

        final String urlString =
                DataReader.optString(
                        eventData, IdentityConstants.EventDataKeys.Identity.BASE_URL, null);

        appendVisitorInfoForURL(urlString, event, configSharedState, analyticsSharedState, null);
    }

    void handleGetUrlVariables(
            final Event event,
            final ConfigurationSharedStateIdentity configSharedState,
            @Nullable final Map<String, Object> analyticsSharedState) {
        final StringBuilder idStringBuilder =
                generateVisitorIDURLPayload(configSharedState, analyticsSharedState);

        final Map<String, Object> params = new HashMap<>();
        params.put(
                IdentityConstants.EventDataKeys.Identity.URL_VARIABLES, idStringBuilder.toString());
        handleIdentityResponseEvent("IDENTITY_URL_VARIABLES", params, event);
    }

    /**
     * Updates the push token value in persistence if there is a value change and sets the analytics
     * push sync flag if this is the first time {@code MobileCore#setPushIdentifier()} is called.
     *
     * @param pushId new push token value received from the event that needs to be updated
     */
    void updatePushIdentifier(final String pushId) {
        pushIdentifier = pushId;

        if (!processNewPushToken(pushId)) {
            Log.debug(
                    IdentityConstants.LOG_TAG,
                    LOG_SOURCE,
                    "updatePushIdentifier : Ignored a push token (%s) as it matches with an"
                        + " existing token, and the push notification status will not be re-sent"
                        + " to Analytics.",
                    pushId);
            return;
        }

        if (pushId == null && !isPushEnabled()) {
            changePushStatusAndHitAnalytics(false);
            Log.debug(
                    IdentityConstants.LOG_TAG,
                    LOG_SOURCE,
                    "updatePushIdentifier : First time sending a.push.optin false");
        } else if (pushId == null) { // push is enabled
            changePushStatusAndHitAnalytics(false);
        } else if (!isPushEnabled()) { // push ID is not null
            changePushStatusAndHitAnalytics(true);
        }
    }

    /**
     * Compares the provided pushToken against the one in shared preferences (if it exists). If the
     * push token is new, this method will store it in shared preferences
     *
     * @param pushToken new push token that should be compared against existing one
     * @return true if the provided token does not match the existing one
     */
    boolean processNewPushToken(final String pushToken) {
        if (namedCollection == null) {
            Log.trace(
                    IdentityConstants.LOG_TAG,
                    LOG_SOURCE,
                    "processNewPushToken : Unable to update push settings because the"
                            + " LocalStorageService was not available.");
            return false;
        }

        final String existingPushToken =
                namedCollection.getString(DataStoreKeys.PUSH_IDENTIFIER, null);
        final boolean analyticsSynced =
                namedCollection.getBoolean(
                        IdentityConstants.DataStoreKeys.ANALYTICS_PUSH_SYNC, false);
        final boolean areTokensEqual =
                (StringUtils.isNullOrEmpty(pushToken) && existingPushToken == null)
                        || (existingPushToken != null && existingPushToken.equals(pushToken));

        // AMSDK-10414 process the update only if the value changed or if this is not the first time
        // setting the push token to null
        if ((areTokensEqual && !StringUtils.isNullOrEmpty(pushToken))
                || (areTokensEqual && analyticsSynced)) {
            return false;
        }

        // AMSDK-10414 if this is the first time setting the push identifier, update the value to
        // avoid subsequent updates
        if (!analyticsSynced) {
            namedCollection.setBoolean(IdentityConstants.DataStoreKeys.ANALYTICS_PUSH_SYNC, true);
        }

        // process token change in persistence
        if (!StringUtils.isNullOrEmpty(pushToken)) {
            namedCollection.setString(DataStoreKeys.PUSH_IDENTIFIER, pushToken);
        } else {
            namedCollection.remove(DataStoreKeys.PUSH_IDENTIFIER);
        }

        return true;
    }

    /**
     * Adds IdentityExtension variables to the provided {@code baseURL} and dispatches an {@link
     * Event} for the one-time listener
     *
     * @param baseURL {@code String } containing the base URL to which identifiers need to be
     *     appended
     * @param event {@code String} event pair id for one-time listener
     * @param configSharedState the Identity Configuration shared state object
     * @param analyticsSharedState the Analytics shared state
     */
    void appendVisitorInfoForURL(
            final String baseURL,
            final Event event,
            final ConfigurationSharedStateIdentity configSharedState,
            final Map<String, Object> analyticsSharedState,
            final StringBuilder idStringBuilderForTesting) {
        if (StringUtils.isNullOrEmpty(baseURL)) {
            // nothing to update, dispatch provided baseURL
            final Map<String, Object> params = new HashMap<>();
            params.put(IdentityConstants.EventDataKeys.Identity.UPDATED_URL, baseURL);
            handleIdentityResponseEvent("IDENTITY_APPENDED_URL", params, event);
            return;
        }

        final StringBuilder modifiedURL = new StringBuilder(baseURL);
        final StringBuilder idStringBuilder =
                idStringBuilderForTesting != null
                        ? idStringBuilderForTesting
                        : generateVisitorIDURLPayload(configSharedState, analyticsSharedState);

        if (!StringUtils.isNullOrEmpty(idStringBuilder.toString())) {
            // add separator based on if url contains query parameters
            int queryIndex = modifiedURL.indexOf("?");

            // account for anchors in url
            int anchorIndex = modifiedURL.indexOf("#");
            int insertIndex = anchorIndex > 0 ? anchorIndex : modifiedURL.length();

            // check for case where URL has no query but the fragment (anchor) contains a '?'
            // character
            boolean isQueryAfterAnchor = anchorIndex > 0 && anchorIndex < queryIndex;

            // insert query delimiter, account for fragment which contains '?' character
            if (queryIndex > 0 && queryIndex != modifiedURL.length() - 1 && !isQueryAfterAnchor) {
                idStringBuilder.insert(0, "&");
            } else if (queryIndex < 0 || isQueryAfterAnchor) {
                idStringBuilder.insert(0, "?");
            }

            // insert idString at appropriate index
            modifiedURL.insert(insertIndex, idStringBuilder.toString());
        }

        // dispatch the modified URL
        final Map<String, Object> params = new HashMap<>();
        params.put(IdentityConstants.EventDataKeys.Identity.UPDATED_URL, modifiedURL.toString());
        handleIdentityResponseEvent("IDENTITY_APPENDED_URL", params, event);
    }

    /**
     * Generate an MID locally
     *
     * @return {@code String} generated MID
     */
    String generateMID() {
        final UUID uuid = UUID.randomUUID();
        final long most = uuid.getMostSignificantBits();
        final long least = uuid.getLeastSignificantBits();
        // return formatted string, flip negatives if they're set.
        return String.format(
                Locale.US, "%019d%019d", most < 0 ? -most : most, least < 0 ? -least : least);
    }

    /**
     * Generates a string with visitor ids where the values are url encoded.
     *
     * @param visitorIDs visitor id list
     * @return url encoded customer identifiers string
     */
    String generateURLEncodedValuesCustomerIdString(final List<VisitorID> visitorIDs) {
        if (visitorIDs == null || visitorIDs.isEmpty()) {
            Log.debug(
                    IdentityConstants.LOG_TAG,
                    LOG_SOURCE,
                    "generateURLEncodedValuesCustomerIdString : No Visitor ID exists in the"
                            + " provided list to generate for URL.");
            return null;
        }

        final StringBuilder customerIdString = new StringBuilder();

        for (VisitorID newVisitorID : visitorIDs) {
            customerIdString.append("&");
            customerIdString.append(IdentityConstants.UrlKeys.VISITOR_ID);
            customerIdString.append("=");
            customerIdString.append(UrlUtils.urlEncode(newVisitorID.getIdType()));
            customerIdString.append(Defaults.CID_DELIMITER);

            final String urlEncodedID = UrlUtils.urlEncode(newVisitorID.getId());

            if (urlEncodedID != null) {
                customerIdString.append(urlEncodedID);
            }

            customerIdString.append(Defaults.CID_DELIMITER);
            customerIdString.append(newVisitorID.getAuthenticationState().getValue());
        }

        if (customerIdString.charAt(0) == '&') {
            customerIdString.deleteCharAt(0);
        }

        return customerIdString.toString();
    }

    /**
     * Converts the {@code HashMap} containing {@code idType, id} key-value pairs to a list of
     * {@link VisitorID} objects. Ignores {@code VisitorID}s with an empty or null idType value
     *
     * <p>Returns an empty list if {@code identifiers} param is null or empty
     *
     * @param identifiers map containing identifiers
     * @param authenticationState authentication state
     * @return {@code List<VisitorID>} list of generated {@code VisitorID}s
     */
    List<VisitorID> generateCustomerIds(
            final Map<String, String> identifiers,
            final VisitorID.AuthenticationState authenticationState) {
        if (identifiers == null) {
            return Collections.emptyList();
        }

        final List<VisitorID> tempIds = new ArrayList<VisitorID>();

        for (Map.Entry<String, String> newID : identifiers.entrySet()) {
            try {
                VisitorID tempId =
                        new VisitorID(
                                IdentityConstants.UrlKeys.VISITOR_ID,
                                newID.getKey(),
                                newID.getValue(),
                                authenticationState);
                tempIds.add(tempId);
            } catch (final IllegalStateException ex) {
                Log.debug(
                        IdentityConstants.LOG_TAG,
                        LOG_SOURCE,
                        "generateCustomerIds : Unable to create Visitor IDs after encoding the"
                                + " provided list due to: (%s).",
                        ex);
            }
        }

        return tempIds;
    }

    /**
     * Packages EventData for the purposes of using it as Shared State
     *
     * @return {@code EventData} representing current {@link IdentityExtension} Shared State
     */
    Map<String, Object> packageEventData() {
        final Map<String, Object> eventData = new HashMap<>();

        if (!StringUtils.isNullOrEmpty(mid)) {
            eventData.put(IdentityConstants.EventDataKeys.Identity.VISITOR_ID_MID, mid);
        }

        if (!StringUtils.isNullOrEmpty(advertisingIdentifier)) {
            eventData.put(
                    IdentityConstants.EventDataKeys.Identity.ADVERTISING_IDENTIFIER,
                    advertisingIdentifier);
        }

        if (!StringUtils.isNullOrEmpty(pushIdentifier)) {
            eventData.put(IdentityConstants.EventDataKeys.Identity.PUSH_IDENTIFIER, pushIdentifier);
        }

        if (!StringUtils.isNullOrEmpty(blob)) {
            eventData.put(IdentityConstants.EventDataKeys.Identity.VISITOR_ID_BLOB, blob);
        }

        if (!StringUtils.isNullOrEmpty(locationHint)) {
            eventData.put(
                    IdentityConstants.EventDataKeys.Identity.VISITOR_ID_LOCATION_HINT,
                    locationHint);
        }

        if (customerIds != null && !customerIds.isEmpty()) {
            eventData.put(
                    IdentityConstants.EventDataKeys.Identity.VISITOR_IDS_LIST,
                    convertVisitorIds(customerIds));
        }

        eventData.put(IdentityConstants.EventDataKeys.Identity.VISITOR_IDS_LAST_SYNC, lastSync);
        return eventData;
    }

    private List<Map<String, Object>> convertVisitorIds(final List<VisitorID> visitorIDList) {
        List<Map<String, Object>> data = new ArrayList<>();
        for (VisitorID vId : visitorIDList) {
            data.add(VisitorIDSerializer.convertVisitorId(vId));
        }
        return data;
    }

    /**
     * Parses the provided {@code idString} and generates a list of corresponding {@link VisitorID}
     * objects. This method removes the duplicates (visitor ids with the same id type) by keeping
     * only the last occurrence in the returned list.
     *
     * @param idString {@link String} to be parsed for valid {@code VisitorID} objects
     * @return {@code List<VisitorID>} containing the objects represented in the {@code idString},
     *     deduplicated by idTypes
     */
    static List<VisitorID> convertVisitorIdsStringToVisitorIDObjects(final String idString) {
        if (StringUtils.isNullOrEmpty(idString)) {
            return new ArrayList<>();
        }

        final List<VisitorID> visitorIDs = new ArrayList<>();
        String[] customerIdComponentsArray = idString.split("&");

        for (final String customerIdString : customerIdComponentsArray) {
            if (!StringUtils.isNullOrEmpty(customerIdString)) {
                final VisitorID id = parseCustomerIDStringToVisitorIDObject(customerIdString);

                /*
                 * AMSDK-8729
                 * if there are any duplicated visitor ids (same idType) in persistence from previous sessions
                 * (generated using Identity 1.1.1 and below), we need to deduplicate those and keep only the most recent
                 * one (we will consider the last element in the list is the most recent one)
                 */
                VisitorID duplicateId = null;

                for (final VisitorID visitorID : visitorIDs) {
                    // check if the same visitor id (same id type) exists in the visitorIDs list
                    if (sameIdType(visitorID, id)) {
                        duplicateId = visitorID;
                        break;
                    }
                }

                if (duplicateId != null) {
                    visitorIDs.remove(duplicateId);
                }

                if (id != null) {
                    visitorIDs.add(id);
                }
            }
        }

        return visitorIDs;
    }

    /**
     * Generates a ECID Payload for use as query parameters in a URL. Extracts the experience cloud
     * id from the config shared state and the analytics id and visitor identifier from the
     * analytics shared state, if available.
     *
     * @param configSharedState the Identity Configuration shared state object
     * @param analyticsSharedState the Analytics shared state
     * @return {@link StringBuilder} containing the ECID payload for the URL
     */
    StringBuilder generateVisitorIDURLPayload(
            final ConfigurationSharedStateIdentity configSharedState,
            @Nullable final Map<String, Object> analyticsSharedState) {
        final StringBuilder urlFragment = new StringBuilder();

        // construct the adobe_mc string
        String theIdString =
                appendKVPToVisitorIdString(
                        null,
                        IdentityConstants.UrlKeys.ADB_VISITOR_TIMESTAMP_KEY,
                        String.valueOf(TimeUtils.getUnixTimeInSeconds()));
        theIdString =
                appendKVPToVisitorIdString(
                        theIdString,
                        IdentityConstants.UrlKeys.ADB_VISITOR_PAYLOAD_MARKETING_CLOUD_ID_KEY,
                        mid);

        String vid = null;

        if (analyticsSharedState != null) {
            String aid =
                    DataReader.optString(
                            analyticsSharedState,
                            IdentityConstants.EventDataKeys.Analytics.ANALYTICS_ID,
                            null);

            if (!StringUtils.isNullOrEmpty(aid)) {
                // add Analytics ID if found
                theIdString =
                        appendKVPToVisitorIdString(
                                theIdString,
                                IdentityConstants.UrlKeys.ADB_VISITOR_PAYLOAD_ANALYTICS_ID_KEY,
                                aid);
            }

            // get Analytics VID for later

            vid =
                    DataReader.optString(
                            analyticsSharedState,
                            IdentityConstants.EventDataKeys.Analytics.VISITOR_IDENTIFIER,
                            null);
        }

        // add Experience Cloud Org ID
        String orgId = configSharedState != null ? configSharedState.orgID : null;

        if (!StringUtils.isNullOrEmpty(orgId)) {
            theIdString =
                    appendKVPToVisitorIdString(
                            theIdString,
                            IdentityConstants.UrlKeys.ADB_VISITOR_PAYLOAD_MARKETING_CLOUD_ORG_ID,
                            orgId);
        }

        // after the adobe_mc string is created, we need to encode it again before adding it to the
        // url
        urlFragment.append(IdentityConstants.UrlKeys.ADB_VISITOR_PAYLOAD_KEY);
        urlFragment.append("=");
        urlFragment.append(UrlUtils.urlEncode(theIdString));

        // add 'adobe_aa_vid' if exists
        if (!StringUtils.isNullOrEmpty(vid)) {
            urlFragment.append("&");
            urlFragment.append(IdentityConstants.UrlKeys.ADB_ANALYTICS_PAYLOAD_KEY);
            urlFragment.append("=");
            urlFragment.append(UrlUtils.urlEncode(vid));
        }

        return urlFragment;
    }

    /**
     * Takes in a key-value pair and appends it to the source string
     *
     * <p>This method <b>does not</b> URL encode the provided {@code value} on the resulting string.
     * If encoding is needed, make sure that the values are encoded before being passed into this
     * function.
     *
     * @param originalString {@link String} to append the key and value to
     * @param key key to append
     * @param value value to append
     * @return a new string with the key and value appended, or {@code originalString} if {@code
     *     key} or {@code value} are null or empty
     */
    String appendKVPToVisitorIdString(
            final String originalString, final String key, final String value) {
        // quickly return original string if key or value are empty
        if (StringUtils.isNullOrEmpty(key) || StringUtils.isNullOrEmpty(value)) {
            return originalString;
        }

        // get the value for the new variable
        final String newUrlVariable = String.format("%s=%s", key, value);

        // if the original string is not empty, we need to append a pipe before we return
        if (StringUtils.isNullOrEmpty(originalString)) {
            return newUrlVariable;
        } else {
            return String.format("%s|%s", originalString, newUrlVariable);
        }
    }

    /**
     * Merges provided {@code newCustomerIds} with the existing {@code customerIds} and returns the
     * resulted list; the existing identifiers (same idType) will be updated with the new id value
     * and/or authentication state and the new identifiers will be appended to the {@code
     * customerIds} list.
     *
     * @param newCustomerIds the new {@link VisitorID}s that need to be merged
     * @return the identifiers merge result
     */
    List<VisitorID> mergeCustomerIds(final List<VisitorID> newCustomerIds) {
        if (newCustomerIds == null || newCustomerIds.isEmpty()) {
            return customerIds;
        }

        final List<VisitorID> tempIds =
                customerIds != null
                        ? new ArrayList<VisitorID>(customerIds)
                        : new ArrayList<VisitorID>();

        for (final VisitorID newId : newCustomerIds) {
            VisitorID mergedId = null;
            VisitorID oldId = null;

            for (final VisitorID visitorID : tempIds) {
                // check if this is the same visitor id (same id type) with updated authentication
                // state and/or id
                if (sameIdType(visitorID, newId)) {
                    mergedId =
                            new VisitorID(
                                    visitorID.getIdOrigin(),
                                    visitorID.getIdType(),
                                    newId.getId(),
                                    newId.getAuthenticationState());
                    oldId = visitorID;
                    break;
                }
            }

            if (mergedId != null) {
                tempIds.remove(oldId);
                tempIds.add(mergedId);
            } else {
                tempIds.add(newId);
            }
        }

        return tempIds;
    }

    /**
     * Callback for network handler when a JSON response is available
     *
     * <p>Parses the response result, persists data locally, updates {@link IdentityExtension}
     * shared state, and dispatches resulting {@link Event}, including a updateSharedState flag
     * indicating if a shared state update should be executed. If this network response was
     * triggered by a paired event, a paired event is also dispatched.
     *
     * @param result {@code HashMap<String, String>} containing the network response result
     * @param event the trigger event
     */
    void networkResponseLoaded(final IdentityResponseObject result, final Event event) {
        boolean requiresSharedStateUpdate = false;
        // regardless of response, update last sync time
        lastSync = TimeUtils.getUnixTimeInSeconds();

        // check privacy here in case the status changed while response was in-flight
        if (privacyStatus != MobilePrivacyStatus.OPT_OUT) {
            // update properties
            requiresSharedStateUpdate = handleNetworkResponseMap(result);

            // save persistently
            savePersistently();
        }

        // dispatch regular and paired response event
        Map<String, Object> updatedResponse = packageEventData();

        if (requiresSharedStateUpdate) {
            // add updateSharedState event data key to indicate that an update is required for this
            // response
            updatedResponse.put(IdentityConstants.EventDataKeys.Identity.UPDATE_SHARED_STATE, true);
        }

        handleIdentityResponseEvent("UPDATED_IDENTITY_RESPONSE", updatedResponse, null);

        if (event != null) {
            handleIdentityResponseEvent("UPDATED_IDENTITY_RESPONSE", updatedResponse, event);
        }
    }

    /**
     * Extracts all the DPIDs from the EventData parameter
     *
     * <p>If the {@code eventData} contains a push identifier, it saves it to the IdentityExtension
     * DataStore
     *
     * <p>This method returns null if {@code eventData} does not contain a DPIDs map or if it
     * contains an empty map
     *
     * @param eventData contains data necessary to process a sync identifier {@link Event}
     * @return {@code Map<String, String>} of valid DPIDs
     */
    private Map<String, String> extractDPID(final Map<String, Object> eventData) {
        if (eventData == null) {
            return null;
        }

        final Map<String, String> dpIDs = new HashMap<>();

        // Extract pushIdentifier
        if (eventData.containsKey(IdentityConstants.EventDataKeys.Identity.PUSH_IDENTIFIER)) {
            try {
                String pushId =
                        DataReader.optString(
                                eventData,
                                IdentityConstants.EventDataKeys.Identity.PUSH_IDENTIFIER,
                                null);
                updatePushIdentifier(pushId);
                dpIDs.put(IdentityConstants.EventDataKeys.Identity.MCPNS_DPID, pushId);
            } catch (Exception e) {
                Log.error(
                        IdentityConstants.LOG_TAG,
                        LOG_SOURCE,
                        "extractDPID : Unable to update the push identifier due to: (%s).",
                        e);
            }
        }

        if (dpIDs.size() == 0) {
            return null;
        }

        return dpIDs;
    }

    /**
     * Updates {@link #advertisingIdentifier} and writes it to the IdentityExtension DataStore
     *
     * @param adid advertising identifier string
     */
    private void updateAdvertisingIdentifier(final String adid) {
        advertisingIdentifier = adid;
        savePersistently();
    }

    /**
     * Updates the {@link #pushEnabled} field and dispatches an event to generate a corresponding
     * Analytics request
     *
     * @param isEnabled whether the user is opted in to receive push notifications
     */
    private void changePushStatusAndHitAnalytics(final boolean isEnabled) {
        setPushStatus(isEnabled);

        final HashMap<String, String> contextData = new HashMap<>();
        contextData.put(
                IdentityConstants.EventDataKeys.Identity.EVENT_PUSH_STATUS,
                String.valueOf(isEnabled));

        final Map<String, Object> analyticsData = new HashMap<>();
        analyticsData.put(
                IdentityConstants.EventDataKeys.Analytics.TRACK_ACTION,
                IdentityConstants.EventDataKeys.Identity.PUSH_ID_ENABLED_ACTION_NAME);
        analyticsData.put(IdentityConstants.EventDataKeys.Analytics.CONTEXT_DATA, contextData);

        analyticsData.put(IdentityConstants.EventDataKeys.Analytics.TRACK_INTERNAL, true);

        final Event analyticsForMessageEvent =
                new Event.Builder(
                                IdentityConstants.ANALYTICS_FOR_IDENTITY_REQUEST_EVENT_NAME,
                                EventType.ANALYTICS,
                                EventSource.REQUEST_CONTENT)
                        .setEventData(analyticsData)
                        .build();

        getApi().dispatch(analyticsForMessageEvent);
        Log.trace(
                IdentityConstants.LOG_TAG,
                LOG_SOURCE,
                "dispatchAnalyticsHit : Analytics event has been added to event hub : (%s)",
                analyticsForMessageEvent);
    }

    /**
     * Determines whether the user is opted in to receive push notifications
     *
     * @return boolean indicating whether the user is opted in to receive push notifications
     */
    private boolean isPushEnabled() {
        synchronized (pushEnabledMutex) {
            if (namedCollection == null) {
                Log.trace(
                        IdentityConstants.LOG_TAG,
                        LOG_SOURCE,
                        "isPushEnabled : Unable to update push flag because the"
                                + " LocalStorageService was not available.");
                return false;
            }

            pushEnabled = namedCollection.getBoolean(DataStoreKeys.PUSH_ENABLED, false);
        }

        return pushEnabled;
    }

    /**
     * Updates the {@link #pushEnabled} flag in DataStore with the provided value.
     *
     * @param enabled new push status value to be updated
     */
    private void setPushStatus(final boolean enabled) {
        synchronized (pushEnabledMutex) {
            if (namedCollection != null) {
                namedCollection.setBoolean(DataStoreKeys.PUSH_ENABLED, enabled);
            } else {
                Log.trace(
                        IdentityConstants.LOG_TAG,
                        LOG_SOURCE,
                        "setPushStatus : Unable to update push flag because the"
                                + " LocalStorageService was not available.");
            }

            pushEnabled = enabled;
            Log.trace(
                    IdentityConstants.LOG_TAG,
                    LOG_SOURCE,
                    "setPushStatus : Push notifications status is now: "
                            + (pushEnabled ? "Enabled" : "Disabled"));
        }
    }

    /**
     * @param eventName to be used to create the event object to be dispatched
     * @param eventData to be used to create the event object to be dispatched
     * @param event for one time callback listener
     */
    private void handleIdentityResponseEvent(
            final String eventName, final Map<String, Object> eventData, final Event event) {
        Event newEvent;
        if (event == null) {
            newEvent =
                    new Event.Builder(eventName, EventType.IDENTITY, EventSource.RESPONSE_IDENTITY)
                            .setEventData(eventData)
                            .build();
        } else {
            newEvent =
                    new Event.Builder(eventName, EventType.IDENTITY, EventSource.RESPONSE_IDENTITY)
                            .setEventData(eventData)
                            .inResponseToEvent(event)
                            .build();
        }

        getApi().dispatch(newEvent);
        Log.trace(
                IdentityConstants.LOG_TAG,
                LOG_SOURCE,
                "dispatchResponse : Identity Response event has been added to event hub : %s",
                newEvent.toString());
    }

    /** @param eventData to be used to create the event object to be dispatched. */
    @VisibleForTesting
    void handleIdentityConfigurationUpdateEvent(final Map<String, Object> eventData) {
        final Event event =
                new Event.Builder(
                                "Configuration Update From IdentityExtension",
                                EventType.CONFIGURATION,
                                EventSource.REQUEST_CONTENT)
                        .setEventData(eventData)
                        .build();

        getApi().dispatch(event);
        Log.trace(
                IdentityConstants.LOG_TAG,
                LOG_SOURCE,
                "dispatchConfigUpdateRequest : Configuration Update event has been added to event"
                        + " hub : %s",
                event.toString());
    }

    /**
     * Process a change to the global privacy status. Sets this extension's {@link
     * MobilePrivacyStatus} reference. If the new status is {@link MobilePrivacyStatus#OPT_OUT} the
     * identifiers are cleared, any queued events are cleared, and any database hits are deleted.
     * When the privacy status changes from {@code MobilePrivacyStatus#OPT_OUT} to any other status,
     * a new Experience Cloud ID (MID) is generated, the new ID is saved to local storage, a shared
     * state is created, and the ID synced with the remote Identity Service.
     *
     * @param event the {@link Event} for the {@code Configuration} change
     * @param eventData the updated {@code Configuration} event data
     */
    void processPrivacyChange(final Event event, final Map<String, Object> eventData) {
        if (eventData == null) {
            return;
        }

        String privacyString =
                DataReader.optString(
                        eventData,
                        IdentityConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY,
                        Defaults.DEFAULT_MOBILE_PRIVACY.getValue());

        MobilePrivacyStatus newPrivacyStatus = MobilePrivacyStatus.fromString(privacyString);

        if (privacyStatus == newPrivacyStatus) {
            return; // no change
        }

        privacyStatus = newPrivacyStatus;

        Log.trace(
                IdentityConstants.LOG_TAG,
                LOG_SOURCE,
                "processPrivacyChange : Processed privacy change request. New privacy status is:"
                        + " (%s).",
                privacyStatus.getValue());

        if (privacyStatus == MobilePrivacyStatus.OPT_OUT) {
            mid = null;
            advertisingIdentifier = null;
            blob = null;
            locationHint = null;
            customerIds = null;

            if (namedCollection != null) {
                namedCollection.remove(DataStoreKeys.AID_SYNCED_KEY);
            }

            updatePushIdentifier(null);
            savePersistently(); // clear datastore
            getApi().createSharedState(packageEventData(), event);
        } else if (StringUtils.isNullOrEmpty(mid)) {
            // Need to generate new Experience Cloud ID for the user
            ConfigurationSharedStateIdentity configSharedState = new ConfigurationSharedStateIdentity();

            SharedStateResult configState =
                    getApi().getSharedState(
                            IdentityConstants.EventDataKeys.Configuration.MODULE_NAME,
                            event,
                            false,
                            SharedStateResolution.LAST_SET);
            if (configState != null) {
                configSharedState.getConfigurationProperties(configState.getValue());
            }

            if (handleSyncIdentifiers(event, configSharedState, true)) {
                getApi().createSharedState(packageEventData(), event);
            }
        }

        initializeHitQueueDatabase();
    }

    /**
     * If the {@link IdentityExtension} DataStore is available, writes {@code IdentityExtension}
     * fields to persistence
     */
    @VisibleForTesting
    void savePersistently() {
        if (namedCollection == null) {
            Log.trace(
                    IdentityConstants.LOG_TAG,
                    LOG_SOURCE,
                    "savePersistently : Unable to save the IdentityExtension fields into"
                            + " persistence because the data store was null.");
            return;
        }

        storeOrRemovePersistentString(
                namedCollection,
                DataStoreKeys.VISITOR_IDS_STRING,
                stringFromVisitorIdList(customerIds));
        storeOrRemovePersistentString(namedCollection, DataStoreKeys.MARKETING_CLOUD_ID, mid);
        storeOrRemovePersistentString(
                namedCollection, DataStoreKeys.PUSH_IDENTIFIER, pushIdentifier);
        storeOrRemovePersistentString(
                namedCollection, DataStoreKeys.ADVERTISING_IDENTIFIER, advertisingIdentifier);
        storeOrRemovePersistentString(namedCollection, DataStoreKeys.LOCATION_HINT, locationHint);
        storeOrRemovePersistentString(namedCollection, DataStoreKeys.BLOB, blob);

        namedCollection.setLong(DataStoreKeys.TTL, ttl);
        namedCollection.setLong(DataStoreKeys.LAST_SYNC, lastSync);

        Log.trace(
                IdentityConstants.LOG_TAG,
                LOG_SOURCE,
                "savePersistently : Successfully saved the Identity data into persistence.");
    }

    /**
     * Helper to set or remove String from DataStore.
     *
     * @param store {@link NamedCollection}
     * @param key {@link String} key in {@code DataStore}
     * @param value {@link String} value for {@code key} in {@code store}
     */
    private static void storeOrRemovePersistentString(
            final NamedCollection store, final String key, final String value) {
        if (StringUtils.isNullOrEmpty(value)) {
            store.remove(key);
        } else {
            store.setString(key, value);
        }
    }

    /**
     * Builds a URL string used for the sync call with the ECID Service
     *
     * @param customerIds list of custom VisitorIDs that need to be synced
     * @param dpids {@code Map<String, String>} of DPIDs used to generate internal identifiers
     * @param configSharedState {@code ConfigurationSharedStateIdentity} configuration valid for
     *     this event
     * @param addConsentFlag whether to add device consent flag to query parameters
     * @return {@code String} containing a URL to be sent to the ECID Service
     */
    private String buildURLString(
            final List<VisitorID> customerIds,
            final Map<String, String> dpids,
            final ConfigurationSharedStateIdentity configSharedState,
            final boolean addConsentFlag) {
        // LinkedHashMap keeps parameters in same order as they are set
        final Map<String, String> queryParameters = new LinkedHashMap<String, String>();
        queryParameters.put("d_ver", "2");
        queryParameters.put("d_rtbd", "json");

        if (addConsentFlag) {
            if (StringUtils.isNullOrEmpty(advertisingIdentifier)) {
                // As Ad ID is being opted out, it will not appear in hit.
                // Need to add "integration code" so server-side knows which type of ad ID to
                // opt-out
                queryParameters.put(IdentityConstants.UrlKeys.DEVICE_CONSENT, "0");
                queryParameters.put(
                        IdentityConstants.UrlKeys.CONSENT_INTEGRATION_CODE,
                        IdentityConstants.EventDataKeys.Identity.ADID_DSID);
            } else {
                queryParameters.put(IdentityConstants.UrlKeys.DEVICE_CONSENT, "1");
            }
        }

        queryParameters.put(IdentityConstants.UrlKeys.ORGID, configSharedState.orgID);

        if (mid != null) {
            queryParameters.put(IdentityConstants.UrlKeys.MID, mid);
        }

        if (blob != null) {
            queryParameters.put(IdentityConstants.UrlKeys.BLOB, blob);
        }

        if (locationHint != null) {
            queryParameters.put(IdentityConstants.UrlKeys.HINT, locationHint);
        }

        final URLBuilder urlBuilder = new URLBuilder();
        urlBuilder
                .addPath("id")
                .setServer(configSharedState.marketingCloudServer)
                .addQueryParameters(queryParameters);

        final String customerIdsString = generateURLEncodedValuesCustomerIdString(customerIds);

        if (!StringUtils.isNullOrEmpty(customerIdsString)) {
            urlBuilder.addQuery(customerIdsString, URLBuilder.EncodeType.NONE);
        }

        final String internalIdsString = generateInternalIdString(dpids);

        if (!StringUtils.isNullOrEmpty(internalIdsString)) {
            urlBuilder.addQuery(internalIdsString, URLBuilder.EncodeType.NONE);
        }

        return urlBuilder.build();
    }

    /**
     * Build the Opt Out hit url that will notify IdentityExtension that the user has opted out.
     *
     * <p>The URL sends the {@link IdentityConstants.UrlKeys#ORGID} and {@link
     * IdentityConstants.UrlKeys#MID}
     *
     * @param configSharedState The current configuration shared state
     * @return The {@link String} URL
     * @see #sendOptOutHit(ConfigurationSharedStateIdentity)
     */
    private String buildOptOutURLString(final ConfigurationSharedStateIdentity configSharedState) {
        if (configSharedState == null) {
            return null;
        }

        if (configSharedState.orgID == null || mid == null) {
            return null;
        }

        final Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put(IdentityConstants.UrlKeys.ORGID, configSharedState.orgID);
        queryParameters.put(IdentityConstants.UrlKeys.MID, mid);

        final URLBuilder urlBuilder = new URLBuilder();
        urlBuilder
                .addPath(IdentityConstants.UrlKeys.PATH_OPTOUT)
                .setServer(configSharedState.marketingCloudServer)
                .addQueryParameters(queryParameters);

        return urlBuilder.build();
    }

    /**
     * Determines whether or not an ID Sync is necessary
     *
     * @param identifiers {@code List<VisitorID>} for the current sync call
     * @param dpids {@code Map<String, String>} of DPIDs for the current sync call
     * @param forceResync {@code boolean} allowing an override to force the sync call
     * @param configuration {@code ConfigurationSharedStateIdentity} configuration valid for this
     *     event
     * @return whether identifiers should be synced to the server
     */
    private boolean shouldSync(
            final List<VisitorID> identifiers,
            final Map<String, String> dpids,
            final boolean forceResync,
            final ConfigurationSharedStateIdentity configuration) {
        boolean syncForProps = true;
        boolean syncForIds = true;

        if (!configuration.canSyncIdentifiersWithCurrentConfiguration()) {
            Log.debug(
                    IdentityConstants.LOG_TAG,
                    LOG_SOURCE,
                    "shouldSync : Ignoring ID Sync due to privacy status opt-out or missing"
                            + " experienceCloud.org.");
            syncForProps = false;
        }

        final boolean needResync =
                (TimeUtils.getUnixTimeInSeconds() - lastSync > ttl) || forceResync;
        final boolean hasIdentifiers = identifiers != null && !identifiers.isEmpty();
        final boolean hasDpids = dpids != null;

        if (!StringUtils.isNullOrEmpty(mid) && !hasIdentifiers && !hasDpids && !needResync) {
            syncForIds = false;
        } else if (StringUtils.isNullOrEmpty(mid)) {
            mid = generateMID(); // generate ID before network call
        }

        return syncForIds && syncForProps;
    }

    /**
     * Takes the provided {@code Map<String, String>} of DPIDs and returns the corresponding query
     * string of IDs.
     *
     * <p>Returns empty string if {@code dpids} is null or empty.
     *
     * @param dpids {@code Map<String, String>} containing the internal IDs.
     * @return {@link String} representing valid query parameters for a URL.
     */
    String generateInternalIdString(final Map<String, String> dpids) {
        if (dpids == null || dpids.isEmpty()) {
            return "";
        }

        final HashMap<String, String> dpidsCopy = new HashMap<String, String>(dpids);
        final StringBuilder internalIdString = new StringBuilder();

        for (final Map.Entry<String, String> entry : dpidsCopy.entrySet()) {
            internalIdString.append("&d_cid=");
            internalIdString.append(UrlUtils.urlEncode(entry.getKey()));
            internalIdString.append(Defaults.CID_DELIMITER);
            internalIdString.append(UrlUtils.urlEncode(entry.getValue()));
        }

        if (internalIdString.charAt(0) == '&') {
            internalIdString.deleteCharAt(0);
        }

        return internalIdString.toString();
    }

    /**
     * Cleanup method for a list of {@link VisitorID}s - it removes any identifier that has an
     * empty/null id value
     *
     * @param identifiers the {@code VisitorID}s that need to be cleaned-up
     * @return curated identifiers list
     */
    List<VisitorID> cleanupVisitorIdentifiers(final List<VisitorID> identifiers) {
        if (identifiers == null) {
            return null;
        }

        List<VisitorID> cleanIdentifiers = new ArrayList<VisitorID>(identifiers);

        try {
            // try-catch for Android 6 work around.
            // [MOB-15919] Customer reported NPEs from ArrayListIterator from Android 6 devices.

            Iterator<VisitorID> iterator = cleanIdentifiers.iterator();

            while (iterator.hasNext()) {
                VisitorID identifier = iterator.next();

                if (StringUtils.isNullOrEmpty(identifier.getId())) {
                    // ignore VisitorIDs that have null/empty id value
                    // Note: Visitor ID service ignores identifiers with null/empty id values, but
                    // we do this cleanup for other
                    // dependent extensions
                    iterator.remove();
                    Log.trace(
                            IdentityConstants.LOG_TAG,
                            LOG_SOURCE,
                            "cleanupVisitorIdentifiers : VisitorID was discarded due to an"
                                    + " empty/null identifier value.");
                }
            }
        } catch (NullPointerException e) {
            Log.error(
                    IdentityConstants.LOG_TAG,
                    LOG_SOURCE,
                    "cleanupVisitorIdentifiers : Caught NullPointerException while iterating"
                            + " through visitor identifiers: %s",
                    e.getLocalizedMessage());
        } catch (ClassCastException e) {
            Log.error(
                    IdentityConstants.LOG_TAG,
                    LOG_SOURCE,
                    "cleanupVisitorIdentifiers : Caught ClassCastException while iterating through"
                            + " visitor identifiers: %s",
                    e.getLocalizedMessage());
        }

        return cleanIdentifiers;
    }

    /**
     * Generates a {@link VisitorID} object with a valid provided {@code customerIdString}
     *
     * <p>Returns null if the provided string does not represent a valid {@code VisitorID}
     *
     * @param customerIdString {@link String} representing a customer ID
     * @return {@code VisitorID} object representing the values provided by the string parameter
     */
    private static VisitorID parseCustomerIDStringToVisitorIDObject(final String customerIdString) {
        // AMSDK-3868
        // in this case, having an equals sign in the value doesn't cause a crash (like it did in
        // iOS),
        // but we are not handling loading the value from SharePreferences properly if
        // the value contains an equals, so the change needs to be made regardless
        final int firstEqualsIndex = customerIdString.indexOf('=');

        // quick out if there's no equals sign in our id string
        if (firstEqualsIndex == -1) {
            Log.debug(
                    IdentityConstants.LOG_TAG,
                    LOG_SOURCE,
                    "parseCustomerIDStringToVisitorIDObject : Unable to load Visitor ID from"
                            + " Shared Preferences: (%s).",
                    customerIdString);
            return null;
        }

        String currentCustomerIdOrigin;
        String currentCustomerIdValue;

        // make sure we have a valid origin and value string
        try {
            currentCustomerIdOrigin = customerIdString.substring(0, firstEqualsIndex);
            currentCustomerIdValue = customerIdString.substring(firstEqualsIndex + 1);
        } catch (final IndexOutOfBoundsException ex) {
            Log.debug(
                    IdentityConstants.LOG_TAG,
                    LOG_SOURCE,
                    "parseCustomerIDStringToVisitorIDObject : Unable to load Visitor ID: (%s) from"
                            + " Shared Preference because the name or value was malformed as in the"
                            + " exception: (%s).",
                    customerIdString,
                    ex);
            return null;
        }

        // make sure the value array has 3 entries
        final List<String> idInfo =
                Arrays.asList(currentCustomerIdValue.split(Defaults.CID_DELIMITER));

        if (idInfo.size() != IdentityConstants.ID_INFO_SIZE) {
            Log.debug(
                    IdentityConstants.LOG_TAG,
                    LOG_SOURCE,
                    "parseCustomerIDStringToVisitorIDObject : Unable to load Visitor ID from"
                            + " Shared Preferences because the value was malformed: (%s).",
                    currentCustomerIdValue);
            return null;
        }

        if (StringUtils.isNullOrEmpty(idInfo.get(1))) {
            Log.debug(
                    IdentityConstants.LOG_TAG,
                    LOG_SOURCE,
                    "parseCustomerIDStringToVisitorIDObject : Unable to load Visitor ID from"
                            + " Shared Preferences because the ECID had null or empty id: (%s).",
                    currentCustomerIdValue);
            return null;
        }

        try {
            return new VisitorID(
                    currentCustomerIdOrigin,
                    idInfo.get(0),
                    idInfo.get(1),
                    VisitorID.AuthenticationState.fromInteger(Integer.parseInt(idInfo.get(2))));
        } catch (final NumberFormatException | IllegalStateException ex) {
            Log.debug(
                    IdentityConstants.LOG_TAG,
                    LOG_SOURCE,
                    "parseCustomerIDStringToVisitorIDObject : Unable to parse the ECID: (%s) due"
                            + " to an exception: (%s).",
                    customerIdString,
                    ex.getLocalizedMessage());
        }

        return null;
    }

    /**
     * Takes a {@code List<VisitorID>} and returns a {@link String} representation
     *
     * <p>This method is used so the {@link VisitorID}s can be stored in the IdentityExtension
     * DataStore
     *
     * <p>Returns empty string if the provided list is null or empty
     *
     * @param visitorIDs {@code List<VisitorID>} containing the identifiers for this user
     * @return {@code String} representing the list of {@code VisitorID}s provided
     */
    private String stringFromVisitorIdList(final List<VisitorID> visitorIDs) {
        if (visitorIDs == null) {
            return "";
        }

        final StringBuilder customerIdString = new StringBuilder();

        for (final VisitorID visitorID : visitorIDs) {
            customerIdString.append("&");
            customerIdString.append(IdentityConstants.UrlKeys.VISITOR_ID);
            customerIdString.append("=");
            customerIdString.append(visitorID.getIdType());
            customerIdString.append(Defaults.CID_DELIMITER);

            if (visitorID.getId() != null) {
                customerIdString.append(visitorID.getId());
            }

            customerIdString.append(Defaults.CID_DELIMITER);
            customerIdString.append(visitorID.getAuthenticationState().getValue());
        }

        return customerIdString.toString();
    }

    /**
     * Parses the provided {@code IdentityResponseObject} and fetches latest data (blob,
     * locationHint, ttl) and triggers the opt-out flow if needed.
     *
     * <p>If the {@code identityResponseObject} contains optOutList, this method will dispatch event
     * for Configuration extension and update the privacy status.
     *
     * <p>If the {@code identityResponseObject} contains an error, this method will log the error
     * and return.
     *
     * <p>If the response contains a valid {@code mid}, the following fields will be set:
     *
     * <ul>
     *   <li>{@link #blob}
     *   <li>{@link #locationHint}
     *   <li>{@link #ttl}
     * </ul>
     *
     * @param identityResponseObject representing the parsed JSON response
     * @return {@code boolean} indicating if there is a change in the local properties (mid, blob,
     *     locationHint)
     */
    @VisibleForTesting
    boolean handleNetworkResponseMap(final IdentityResponseObject identityResponseObject) {
        boolean requiresSharedStateUpdate = false;

        if (identityResponseObject == null) {
            Log.debug(
                    IdentityConstants.LOG_TAG,
                    LOG_SOURCE,
                    "handleNetworkResponseMap : Received an empty JSON in response from ECID"
                            + " Service, so there is nothing to handle.");
            return false;
        }

        if (identityResponseObject.optOutList != null
                && !identityResponseObject.optOutList.isEmpty()) {
            Log.debug(
                    IdentityConstants.LOG_TAG,
                    LOG_SOURCE,
                    "handleNetworkResponseMap : Received opt-out response from ECID Service, so"
                            + " updating the privacy status in the configuration to opt-out.");

            Map<String, Object> updateConfig = new HashMap<>();
            updateConfig.put(
                    IdentityConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY,
                    MobilePrivacyStatus.OPT_OUT.getValue());

            Map<String, Object> eventData = new HashMap<>();
            eventData.put(
                    IdentityConstants.EventDataKeys.Configuration
                            .CONFIGURATION_REQUEST_CONTENT_UPDATE_CONFIG,
                    updateConfig);
            handleIdentityConfigurationUpdateEvent(eventData);
        }

        // something's wrong - n/w call returned an error. update the pending state.
        if (!StringUtils.isNullOrEmpty(identityResponseObject.error)) {
            Log.warning(
                    IdentityConstants.LOG_TAG,
                    LOG_SOURCE,
                    "handleNetworkResponseMap : ECID Service returned an error: (%s).",
                    identityResponseObject.error);

            // should never happen bc we generate mid locally before n/w request.
            // Still, generate mid locally if there's none yet.
            if (mid == null) {
                // no valid id, generate locally
                mid = generateMID();
                requiresSharedStateUpdate = true;
            }

            return requiresSharedStateUpdate;
        }

        // Only update stored properties if mid in response is same as what we have locally
        if (!StringUtils.isNullOrEmpty(identityResponseObject.mid)
                && identityResponseObject.mid.equals(mid)) {
            try {
                if ((identityResponseObject.blob != null
                                && !identityResponseObject.blob.equals(blob))
                        || (StringUtils.isNullOrEmpty(identityResponseObject.blob)
                                && !StringUtils.isNullOrEmpty(blob))) {
                    requiresSharedStateUpdate = true;
                }

                if ((identityResponseObject.hint != null
                                && !identityResponseObject.hint.equals(locationHint))
                        || (StringUtils.isNullOrEmpty(identityResponseObject.hint)
                                && !StringUtils.isNullOrEmpty(locationHint))) {
                    requiresSharedStateUpdate = true;
                }

                blob = identityResponseObject.blob;
                locationHint = identityResponseObject.hint;
                ttl = identityResponseObject.ttl;
                Log.debug(
                        IdentityConstants.LOG_TAG,
                        LOG_SOURCE,
                        "handleNetworkResponseMap : ECID Service returned (mid: %s, blob: %s,"
                                + " hint: %s, ttl: %d).",
                        mid,
                        blob,
                        locationHint,
                        ttl);
            } catch (final Exception ex) {
                Log.warning(
                        IdentityConstants.LOG_TAG,
                        LOG_SOURCE,
                        "handleNetworkResponseMap : Error parsing the response from ECID Service :"
                                + " (%s).",
                        ex);
            }
        }

        return requiresSharedStateUpdate;
    }

    private Event createForcedSyncEvent() {
        final Map<String, Object> eventData = new HashMap<>();
        eventData.put(IdentityConstants.EventDataKeys.Identity.FORCE_SYNC, true);
        eventData.put(IdentityConstants.EventDataKeys.Identity.IS_SYNC_EVENT, true);
        eventData.put(
                IdentityConstants.EventDataKeys.Identity.AUTHENTICATION_STATE,
                VisitorID.AuthenticationState.UNKNOWN.getValue());

        return new Event.Builder(
                        "id-construct-forced-sync",
                        EventType.IDENTITY,
                        EventSource.REQUEST_IDENTITY)
                .setEventData(eventData)
                .build();
    }

    /**
     * Checks if two {@link VisitorID}s have the same id type. This method is used for identifying
     * if an id needs to be updated or if it is completely new.
     *
     * <p>Find more context here: AMSDK-8729, AMSDK-3720
     *
     * @param visitorId1 first {@code VisitorID} to be compared
     * @param visitorId2 second {@code VisitorID} to be compared
     * @return status of the comparison between the two visitor identifier idTypes
     * @see {@link #mergeCustomerIds(List)}
     */
    private static boolean sameIdType(final VisitorID visitorId1, final VisitorID visitorId2) {
        if (visitorId1 == null || visitorId2 == null) {
            return false;
        }

        return visitorId1.getIdType() != null
                ? visitorId1.getIdType().equals(visitorId2.getIdType())
                : visitorId2.getIdType() == null;
    }

    /**
     * Attempts to set this IdentityExtension's {@code MobilePrivacyStatus} reference by retrieving
     * the {@code Configuration} shared state for the given {@code event}. This method should be
     * called during the extension's boot process.
     *
     * @param configState the Configuration shared state
     */
    private void loadPrivacyStatusFromConfigurationState(final Map<String, Object> configState) {
        String privacyString =
                DataReader.optString(
                        configState,
                        IdentityConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY,
                        Defaults.DEFAULT_MOBILE_PRIVACY.getValue());

        privacyStatus = MobilePrivacyStatus.fromString(privacyString);
    }

    @VisibleForTesting
    String getMid() {
        return mid;
    }

    @VisibleForTesting
    void setLastSync(final long lastSync) {
        this.lastSync = lastSync;
    }

    @VisibleForTesting
    long getLastSync() {
        return lastSync;
    }

    @VisibleForTesting
    void setMid(final String mid) {
        this.mid = mid;
    }

    @VisibleForTesting
    void setHasSynced(final boolean hasSynced) {
        this.hasSynced = hasSynced;
    }

    @VisibleForTesting
    void setPrivacyStatus(final MobilePrivacyStatus privacyStatus) {
        this.privacyStatus = privacyStatus;
    }

    @VisibleForTesting
    void setBlob(final String blob) {
        this.blob = blob;
    }

    @VisibleForTesting
    void setLocationHint(final String locationHint) {
        this.locationHint = locationHint;
    }

    @VisibleForTesting
    ConfigurationSharedStateIdentity getLatestValidConfig() {
        return this.latestValidConfig;
    }
}
