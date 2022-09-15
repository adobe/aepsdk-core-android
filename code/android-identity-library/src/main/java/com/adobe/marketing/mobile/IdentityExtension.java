/* *****************************************************************************
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2018 Adobe
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains
 * the property of Adobe and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Adobe
 * and its suppliers and are protected by all applicable intellectual
 * property laws, including trade secret and copyright laws.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe.
 ******************************************************************************/

package com.adobe.marketing.mobile;

import com.adobe.marketing.mobile.IdentityConstants.DataStoreKeys;
import com.adobe.marketing.mobile.IdentityConstants.Defaults;
import com.adobe.marketing.mobile.LocalStorageService.*;

import java.net.HttpURLConnection;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * IdentityExtension class is responsible for interactions with the ECID Service
 *
 * The IdentityExtension handles the following use cases:
 * <ol>
 *     <li>Syncing new visitor identifiers to the ECID Service</li>
 *     <li>Modifying a base URL and returning with ECID Service values to facilitate hybrid app communication</li>
 *     <li>Returning a list of known identifiers for the user</li>
 * </ol>
 *
 * The IdentityExtension listens for the following {@link Event}s:
 * <ol>
 *     <li>{@link EventType#HUB} - {@link EventSource#BOOTED}</li>
 *     <li>{@code EventType.HUB} - {@link EventSource#SHARED_STATE}</li>
 *     <li>{@link EventType#IDENTITY} - {@link EventSource#REQUEST_IDENTITY}</li>
 * </ol>
 *
 * The IdentityExtension dispatches the following {@code Events}:
 * <ol>
 *     <li>{@link EventType#ANALYTICS} - {@link EventSource#REQUEST_CONTENT}</li>
 *     <li>{@code EventType.IDENTITY} - {@link EventSource#RESPONSE_IDENTITY}</li>
 *     <li>{@code EventType.CONFIGURATION} - {@link EventSource#REQUEST_CONTENT}</li>
 * </ol>
 *
 * The IdentityExtension has dependencies on the following Platform Services:
 * <ol>
 *     <li>{@link DatabaseService}</li>
 *     <li>{@link JsonUtilityService}</li>
 *     <li>{@link LocalStorageService}</li>
 *     <li>{@link NetworkService}</li>
 *     <li>{@link SystemInfoService}</li>
 * </ol>
 */
class IdentityExtension extends InternalModule {
	//**************************************************************
	// package-private fields
	//**************************************************************
	static final String LOG_SOURCE = "IdentityExtension";
	String mid;
	String advertisingIdentifier;
	String pushIdentifier;
	String blob;
	String locationHint;
	long lastSync;
	long ttl;
	List<VisitorID> customerIds;
	MobilePrivacyStatus privacyStatus;
	ConcurrentLinkedQueue<Event> eventsQueue;
	DataStore internalDataStore;
	IdentityHitsDatabase database;
	DispatcherIdentityResponseIdentityIdentity idResponseEventDispatcher;
	DispatcherAnalyticsRequestContentIdentity idAnalyticsEventDispatcher;
	DispatcherConfigurationRequestContentIdentity idConfigurationEventDispatcher;


	//**************************************************************
	// private fields
	//**************************************************************
	private static boolean pushEnabled = false;
	private static final Object _pushEnabledMutex = new Object();
	private ConfigurationSharedStateIdentity latestValidConfig;

	//**************************************************************
	// constructor
	//**************************************************************
	/**
	 * Constructor<p>
	 *
	 * Registers event listeners and loads any IdentityExtension data that has been persisted.
	 *
	 * Initializes the following class fields:
	 * <ul>
	 *     <li>{@link #database}</li>
	 *     <li>{@link #eventsQueue}</li>
	 *     <li>{@link #idResponseEventDispatcher}</li>
	 *     <li>{@link #idAnalyticsEventDispatcher}</li>
	 *     <li>{@link #idConfigurationEventDispatcher}</li>
	 * </ul>
	 *
	 * @param hub {@link EventHub} instance that owns this extension
	 * @param platformServices {@link PlatformServices} instance
	 */
	IdentityExtension(final EventHub hub, final PlatformServices platformServices) {
		super(IdentityConstants.EventDataKeys.Identity.MODULE_NAME, hub, platformServices);

		privacyStatus = Defaults.DEFAULT_MOBILE_PRIVACY;
		eventsQueue = new ConcurrentLinkedQueue<Event>();

		registerListener(EventType.HUB, EventSource.BOOTED, ListenerHubBootedIdentity.class);
		registerListener(EventType.IDENTITY, EventSource.REQUEST_IDENTITY, ListenerIdentityRequestIdentity.class);
		registerListener(EventType.IDENTITY, EventSource.RESPONSE_IDENTITY, IdentityListenerResponseIdentity.class);
		registerListener(EventType.HUB, EventSource.SHARED_STATE, ListenerHubSharedStateIdentity.class);
		registerListener(EventType.ANALYTICS, EventSource.RESPONSE_IDENTITY, ListenerAnalyticsResponseIdentity.class);
		registerListener(EventType.AUDIENCEMANAGER, EventSource.RESPONSE_CONTENT,
						 IdentityListenerAudienceResponseContent.class);
		registerListener(EventType.CONFIGURATION, EventSource.RESPONSE_CONTENT,
						 IdentityListenerConfigurationResponseContent.class);
		registerListener(EventType.GENERIC_IDENTITY, EventSource.REQUEST_CONTENT,
						 ListenerIdentityGenericIdentityRequestIdentity.class);
		registerListener(EventType.GENERIC_IDENTITY, EventSource.REQUEST_RESET,
						 ListenerIdentityGenericIdentityRequestReset.class);
		idResponseEventDispatcher = createDispatcher(DispatcherIdentityResponseIdentityIdentity.class);
		idAnalyticsEventDispatcher = createDispatcher(DispatcherAnalyticsRequestContentIdentity.class);
		idConfigurationEventDispatcher = createDispatcher(DispatcherConfigurationRequestContentIdentity.class);

		loadVariablesFromPersistentData();
	}

	//**************************************************************
	// package-protected methods
	//**************************************************************
	/**
	 * Adds an {@code Event} to the {@link #eventsQueue}
	 *
	 * @param event event to be added to the queue
	 */
	void enqueueEvent(final Event event) {
		if (event == null) {
			Log.debug(LOG_SOURCE,
					  "enqueueEvent : Unable to add the Identity event into the event queue because the event was null.");
			return;
		}

		eventsQueue.add(event);
		Log.trace(LOG_SOURCE, "enqueueEvent : An Identity event has been added into the event queue : %s", event);
	}

	/**
	 * Loops through the {@link #eventsQueue} and processes them<p>
	 *
	 * Hard dependency on {@code Configuration} shared state being non-null
	 */
	void processEventQueue() {
		while (!eventsQueue.isEmpty()) {
			final Event event = eventsQueue.peek();
			final EventData configEventData = getSharedEventState(IdentityConstants.EventDataKeys.Configuration.MODULE_NAME, event);

			if (configEventData == EventHub.SHARED_STATE_PENDING) {
				Log.trace(LOG_SOURCE,
						  "processEventQueue : Unable to process the Identity events in the event queue because the configuration shared state was pending.");
				break;
			}

			ConfigurationSharedStateIdentity configSharedState = new ConfigurationSharedStateIdentity();
			configSharedState.getConfigurationProperties(configEventData);

			// pop event from queue only if processing returns true
			if (processEvent(event, configSharedState)) {
				eventsQueue.poll();
			} else {
				break;
			}
		}
	}

	/**
	 * Forces an initial sync upon {@link EventHub} boot completion
	 *
	 * @param bootEvent {@link Event} of boot completion event
	 */
	void bootup(final Event bootEvent) {
		Log.trace(LOG_SOURCE, "bootup : Processing BOOTED event.");

		// The database is created when the privacy status changes or on the first sync event
		// which is why it is not created in bootup.
		loadPrivacyStatus(bootEvent); // attempt to load privacy status from Configuration state

		final Event forcedSyncEvent = createForcedSyncEvent(bootEvent.getEventNumber());
		enqueueEvent(forcedSyncEvent);
		processEventQueue();
		Log.trace(LOG_SOURCE, "bootup : Added an Identity force sync event on boot.");

		// Identity should always share its state
		// However, don't create a shared state twice, which will log an error
		// The force sync event processed above will create a shared state if the privacy is not opt-out
		if (privacyStatus == MobilePrivacyStatus.OPT_OUT) {
			Log.trace(LOG_SOURCE,
					  "bootup : Privacy status was opted out on boot, so created Identity shared state.");
			createSharedState(bootEvent.getEventNumber(), packageEventData());
		}
	}

	/**
	 * Attempts to set this IdentityExtension's {@code MobilePrivacyStatus} reference by retrieving the {@code Configuration} shared state
	 * for the given {@code event}.
	 * This method should be called during the extension's boot process.
	 * @param event the {@link Event} used to retrieve the {@code Configuration} state
	 */
	private void loadPrivacyStatus(final Event event) {
		EventData configState = getSharedEventState(IdentityConstants.EventDataKeys.Configuration.MODULE_NAME, event);

		if (configState == EventHub.SHARED_STATE_PENDING) {
			return;
		}

		String privacyString = configState.optString(IdentityConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY,
							   Defaults.DEFAULT_MOBILE_PRIVACY.getValue());

		privacyStatus = MobilePrivacyStatus.fromString(privacyString);
		Log.trace(LOG_SOURCE, "loadPrivacyStatus : Updated the database with the current privacy status: %s.", privacyString);
		initializeDatabaseWithCurrentPrivacyStatus();
	}


	/**
	 * This method creates an instance of the database if one does not exist already and sets this IdentityExtension's {@code MobilePrivacyStatus}
	 */
	private void initializeDatabaseWithCurrentPrivacyStatus() {
		if (database == null) {
			database = new IdentityHitsDatabase(this, getPlatformServices());
		}

		database.updatePrivacyStatus(privacyStatus);
	}


	/**
	 * Handles {@code Configuration} event passed on by the {@code Listener}
	 * <p>
	 *
	 * If the {@link MobilePrivacyStatus} is {@link MobilePrivacyStatus#OPT_OUT} then an opt out hit is potentially sent to the
	 * identity server.
	 *
	 * @param configurationEvent {@code Configuration} event to be processed
	 *
	 * @see #handleOptOut(Event)
	 * @see #updateLatestValidConfiguration(EventData)
	 */
	void handleConfiguration(final Event configurationEvent) {
		if (configurationEvent == null) {
			return;
		}

		EventData data = configurationEvent.getData();

		if (data == null) {
			return;
		}

		MobilePrivacyStatus mobilePrivacyStatus = MobilePrivacyStatus.
				fromString(data.optString(IdentityConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY,
										  Defaults.DEFAULT_MOBILE_PRIVACY.getValue()));

		if (mobilePrivacyStatus.equals(MobilePrivacyStatus.OPT_OUT)) {
			handleOptOut(configurationEvent);
		}

		// if config contains a new global privacy change, process the event
		// Do this after calling handleOptOut; clearing the identifiers before will cause handleOptOut to fail
		processPrivacyChange(configurationEvent.getEventNumber(), data);

		updateLatestValidConfiguration(data);

	}

	/**
	 * Handler for {@code EventType.ANALYTICS} {@code EventSource.RESPONSE_IDENTITY} events.
	 * Extracts the Analytics ID from the {@code event} and adds a sync event to the event queue.
	 *
	 * @param event {@link Event} containing the {@link IdentityConstants.EventDataKeys.Analytics#ANALYTICS_ID}
	 */
	void handleAnalyticsResponseIdentity(final Event event) {
		if (event == null) {
			return;
		}

		EventData data = event.getData();

		if (data == null) {
			return;
		}

		String aid = data.optString(IdentityConstants.EventDataKeys.Analytics.ANALYTICS_ID, null);

		if (StringUtils.isNullOrEmpty(aid)) {
			return;
		}

		final DataStore dataStore = getDataStore();

		if (dataStore == null) {
			return;
		}

		if (dataStore.contains(DataStoreKeys.AID_SYNCED_KEY)) {
			return;
		} else {
			dataStore.setBoolean(DataStoreKeys.AID_SYNCED_KEY, true);
		}

		Map<String, String> identifiers = new HashMap<String, String>();
		identifiers.put(IdentityConstants.EventDataKeys.Identity.ANALYTICS_ID, aid);

		final EventData syncData = new EventData();
		syncData.putStringMap(IdentityConstants.EventDataKeys.Identity.IDENTIFIERS, identifiers);
		syncData.putInteger(IdentityConstants.EventDataKeys.Identity.AUTHENTICATION_STATE,
							VisitorID.AuthenticationState.UNKNOWN.getValue());
		syncData.putBoolean(IdentityConstants.EventDataKeys.Identity.FORCE_SYNC, false);
		syncData.putBoolean(IdentityConstants.EventDataKeys.Identity.IS_SYNC_EVENT, true);

		Event avidEvent = new Event.Builder("AVID Sync",
											EventType.IDENTITY,
											EventSource.REQUEST_IDENTITY)
		.setData(syncData)
		.build();

		enqueueEvent(avidEvent);
		processEventQueue();
	}

	/**
	 * Creates a new shared state with latest in-memory data.
	 * @param event the Identity ResponseIdentity event that indicates if a shared state update is required.
	 *              The event number will be used for shared state update.
	 */
	void handleIdentityResponseIdentityForSharedState(final Event event) {
		if (event == null) {
			return;
		}

		EventData data = event.getData();

		if (data == null || !data.optBoolean(IdentityConstants.EventDataKeys.Identity.UPDATE_SHARED_STATE, false)) {
			return;
		}

		createSharedState(event.getEventNumber(), packageEventData());
	}

	/**
	 * Updates this extension's configuration with the latest {@code Configuration} data which contains a valid
	 * Experience Cloud organization ID.
	 *
	 * @param data {@link EventData} containing the {@code Configuration} data
	 * @see ConfigurationSharedStateIdentity
	 */
	void updateLatestValidConfiguration(final EventData data) {
		String orgId = data.optString(IdentityConstants.EventDataKeys.Configuration.CONFIG_EXPERIENCE_CLOUD_ORGID_KEY, null);

		if (!StringUtils.isNullOrEmpty(orgId)) {
			latestValidConfig = new ConfigurationSharedStateIdentity();
			latestValidConfig.getConfigurationProperties(data);
			processEventQueue();
		}
	}

	/**
	 * Handles the reset request by resetting all the persisted properties and generating a new ECID as a result of a force sync.
	 * @param event the request request {@link Event}
	 */
	void handleIdentityRequestReset(final Event event) {
		if (event == null) {
			Log.debug(LOG_SOURCE, "handleIdentityRequestReset: Ignoring null event");
			return;
		}

		if (privacyStatus == MobilePrivacyStatus.OPT_OUT) {
			Log.debug(LOG_SOURCE, "handleIdentityRequestReset: Privacy is opt-out, ignoring event.");
			return;
		}

		mid = null;
		advertisingIdentifier = null;
		blob = null;
		locationHint = null;
		customerIds = null;
		pushIdentifier = null;
		final DataStore dataStore = getDataStore();

		if (dataStore != null) {
			dataStore.remove(DataStoreKeys.AID_SYNCED_KEY);
			dataStore.remove(DataStoreKeys.PUSH_ENABLED);
		}

		clearEventsQueue();
		savePersistently(); // clear datastore

		// When resetting identifiers, need to generate new Experience Cloud ID for the user
		// Queue up a request to sync the new ID with the Identity Service
		// This will also create Identity shared state
		final Event forcedSyncEvent = createForcedSyncEvent(event.getEventNumber());
		eventsQueue.add(forcedSyncEvent);
		processEventQueue();
		Log.debug(LOG_SOURCE, "handleIdentityRequestReset: Did reset identifiers and queued force sync event.");
	}

	/**
	 * Process the {@code Audience} {@code Response_Content} event.
	 *
	 * <p>
	 * If the event contains the {@link IdentityConstants.EventDataKeys.Audience#OPTED_OUT_HIT_SENT} flag set to false,
	 * then and opt out hit is sent out to the IdentityExtension server configured. This hit is sent only if the current configuration
	 * shared state has {@link IdentityConstants.EventDataKeys.Configuration#GLOBAL_CONFIG_PRIVACY} set to {@link MobilePrivacyStatus#OPT_OUT}.
	 * If not, then nothing is done.
	 *
	 *
	 * @param audienceEvent The event heard by the {@link IdentityListenerAudienceResponseContent}
	 */
	void processAudienceResponse(final Event audienceEvent) {
		if (audienceEvent == null) {
			return;
		}

		EventData data = audienceEvent.getData();

		if (data == null) {
			return;
		}

		if (data.containsKey(IdentityConstants.EventDataKeys.Audience.OPTED_OUT_HIT_SENT)) {
			boolean optOutHitSent = data.optBoolean(IdentityConstants.EventDataKeys.Audience.OPTED_OUT_HIT_SENT, false);

			if (optOutHitSent) {
				return;
			}

			//IdentityExtension needs to send the hit since AAM did not
			EventData configuration = getSharedEventState(IdentityConstants.EventDataKeys.Configuration.MODULE_NAME, audienceEvent);

			if (configuration == EventHub.SHARED_STATE_PENDING) {
				Log.trace(LOG_SOURCE,
						  "processAudienceResponse : Unable to process the Identity events in the event queue because the configuration shared state is pending.");
				return;
			}

			//Make sure that the configuration shared state at this point has not changed the privacy status
			ConfigurationSharedStateIdentity configSharedState = new ConfigurationSharedStateIdentity();
			configSharedState.getConfigurationProperties(configuration);

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
	protected void sendOptOutHit(final ConfigurationSharedStateIdentity configSharedState) {
		String optOutUrl = buildOptOutURLString(configSharedState);

		if (StringUtils.isNullOrEmpty(optOutUrl)) {
			Log.debug(LOG_SOURCE, "sendOptOutHit : Unable to send network hit because the opt-out URL was null.");
			return;
		}

		PlatformServices platformServices;

		if ((platformServices = getPlatformServices()) != null) {
			NetworkService networkService = platformServices.getNetworkService();

			if (networkService == null) {
				Log.debug(LOG_SOURCE,
						  "sendOptOutHit : Unable to send network request to the opt-out URL (%s) because NetworkService is unavailable.",
						  optOutUrl);
				return;
			}

			Log.debug(LOG_SOURCE, "sendOptOutHit : Sending network request to the opt-out URL: (%s).", optOutUrl);
			networkService.connectUrlAsync(
				optOutUrl,
				NetworkService.HttpCommand.GET,
				null,
				null,
				IdentityConstants.Defaults.TIMEOUT,
				IdentityConstants.Defaults.TIMEOUT,
			new NetworkService.Callback() {
				@Override
				public void call(final NetworkService.HttpConnection connection) {
					if (connection == null) {
						return;
					}

					if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
						Log.trace(LOG_SOURCE, "sendOptOutHit - Successfully sent the opt-out hit.");
					} else {
						Log.trace(LOG_SOURCE, "sendOptOutHit - Failed to send the opt-out hit with connection status (%s).",
								  connection.getResponseCode());
					}

					connection.close();
				}
			});
		}
	}

	/**
	 * Loads values persisted in {@link DataStore}
	 * <p>
	 * Returns early without setting variables if {@link LocalStorageService} is unavailable
	 */
	void loadVariablesFromPersistentData() {

		final DataStore dataStore = getDataStore();

		if (dataStore == null) {
			Log.debug(LOG_SOURCE,
					  "loadVariablesFromPersistentData : Unable to load the Identity data from persistence because the LocalStorageService was null.");
			return;
		}

		mid = dataStore.getString(DataStoreKeys.MARKETING_CLOUD_ID, null);

		// reload the customer ids.
		final List<VisitorID> newCustomerIDs = convertVisitorIdsStringToVisitorIDObjects(dataStore.getString(
				DataStoreKeys.VISITOR_IDS_STRING, null));

		customerIds = newCustomerIDs == null || newCustomerIDs.isEmpty() ? null : newCustomerIDs;
		locationHint = dataStore.getString(DataStoreKeys.LOCATION_HINT, null);
		blob = dataStore.getString(DataStoreKeys.BLOB, null);
		ttl = dataStore.getLong(DataStoreKeys.TTL, Defaults.DEFAULT_TTL_VALUE);
		lastSync = dataStore.getLong(DataStoreKeys.LAST_SYNC, 0);
		advertisingIdentifier = dataStore.getString(DataStoreKeys.ADVERTISING_IDENTIFIER, null);
		pushIdentifier = dataStore.getString(DataStoreKeys.PUSH_IDENTIFIER, null);
		Log.trace(LOG_SOURCE, "loadVariablesFromPersistentData : Successfully loaded the Identity data from persistence.");

	}

	/**
	 * Marshals an {@link Event} and passes it to the correct method depending upon its {@link EventData}
	 *
	 * @param event {@code Event} to be marshaled
	 * @param configSharedState configuration valid for this event
	 * @return true if the {@code event} was processed and should be removed from the event queue,
	 * false if the {@code event} should remain in the event queue to be processed later
	 */
	boolean processEvent(final Event event, final ConfigurationSharedStateIdentity configSharedState) {
		if (configSharedState == null) {
			// sanity check, should never get here
			Log.trace(LOG_SOURCE,
					  "processEvent : Unable to process the Identity event in the event queue because the configuration was not available yet.");
			return true;
		}

		if (event == null) {
			Log.trace(LOG_SOURCE,
					  "processEvent : Unable to process the Identity event in the event queue because the event was null.");
			return true; // event can never be processed, throw it out of the queue
		}


		final EventData eventData = event.getData();

		if (eventData == null) {
			Log.trace(LOG_SOURCE,
					  "processEvent : Unable to process the Identity event in the event queue because the event data was null.");
			return true; // event can never be processed, throw it out of the queue
		}

		Log.trace(LOG_SOURCE, "processEvent : Processing the Identity event: %s", event);

		if (event.getEventType().equals(EventType.GENERIC_IDENTITY)
				&& event.getEventSource().equals(EventSource.REQUEST_RESET)) {
			handleIdentityRequestReset(event);
		} else if (eventData.optBoolean(IdentityConstants.EventDataKeys.Identity.IS_SYNC_EVENT, false)
				   || event.getEventType().equals(EventType.GENERIC_IDENTITY)) {
			if (!handleSyncIdentifiers(event, configSharedState)) {
				Log.warning(LOG_SOURCE,
							"ProcessEvent : Configuration is missing a valid experienceCloud.org which is needed to process Identity events. Processing will resume once a valid configuration is obtained.");
				return false;
			}
		} else if (eventData.containsKey(IdentityConstants.EventDataKeys.Identity.BASE_URL)) {
			final EventData analyticsSharedState = getSharedEventState(IdentityConstants.EventDataKeys.Analytics.MODULE_NAME,
												   event);

			if (shouldWaitForPendingSharedState(event, IdentityConstants.EventDataKeys.Analytics.MODULE_NAME,
												analyticsSharedState)) {
				Log.trace(LOG_SOURCE,
						  "ProcessEvent : Analytics is registered but has pending shared state. Waiting for Analytics state before processing appendToUrl event.");
				return false;
			}

			handleAppendURL(event, configSharedState, analyticsSharedState);
		} else if (eventData.optBoolean(IdentityConstants.EventDataKeys.Identity.URL_VARIABLES, false)) {
			final EventData analyticsSharedState = getSharedEventState(IdentityConstants.EventDataKeys.Analytics.MODULE_NAME,
												   event);

			if (shouldWaitForPendingSharedState(event, IdentityConstants.EventDataKeys.Analytics.MODULE_NAME,
												analyticsSharedState)) {
				Log.trace(LOG_SOURCE,
						  "ProcessEvent : Analytics is registered but has pending shared state. Waiting for Analytics state before processing getUrlVariables event.");
				return false;
			}

			handleGetUrlVariables(event, configSharedState, analyticsSharedState);
		} else {
			handleIdentityResponseEvent("IDENTITY_RESPONSE_CONTENT_ONE_TIME", packageEventData(),
										event.getResponsePairID());
		}

		return true; // successfully processed event, throw it out of the queue
	}

	/**
	 * Determines if it is necessary to halt processing of events until the given {@code extensionSharedState} is updated.
	 * This method will check the Event Hub shared state to see if the given {@code extensionName} is a registered extension.
	 * If an extension is registered and its shared state is null or pending, then processing of
	 * events should wait until the required shared state is updated.
	 *
	 * In the following cases this method will return true to indicate the processing should wait until the next shared state update:
	 * <ul>
	 * <li>The given {@code extensionSharedState} is pending and the Event Hub shared state contains {@code extensionName} in the list of registered extensions</li>
	 * <li>The given {@code extensionSharedState} is pending and the Event Hub shared state is pending</li>
	 * </ul>
	 *
	 * @param event the event used to get the Event Hub shared state
	 * @param extensionName name of the extension to check if registered with the Event Hub
	 * @param extensionSharedState shared state of extension with name {@code extensionName}
	 * @return true if processing of the given {@code event} must wait for a shared state update,
	 * or false if processing of the given {@code evnet} can continue
	 */
	boolean shouldWaitForPendingSharedState(final Event event, final String extensionName,
											final EventData extensionSharedState) {
		if (extensionSharedState != EventHub.SHARED_STATE_PENDING) {
			// Don't need to wait if we have a shared state from the extension
			return false;
		}

		EventData hubSharedState = getSharedEventState(IdentityConstants.EventDataKeys.EventHub.MODULE_NAME, event);

		if (hubSharedState == EventHub.SHARED_STATE_PENDING) {
			Log.trace(LOG_SOURCE,
					  "shouldWaitForPendingSharedState : Event Hub shared state is pending.");
			// Both Event Hub and extension states are pending/null, need to wait for either Event Hub or extensions state update
			return true;
		}

		Map<String, Variant> extensionDetailsMap = hubSharedState.optVariantMap(
					IdentityConstants.EventDataKeys.EventHub.EXTENSIONS_KEY, null);

		if (extensionDetailsMap == null) {
			Log.warning(LOG_SOURCE,
						"shouldWaitForPendingSharedState : Event Hub shared state did not have registered extensions info.");
			// Event Hub shared its state, but it's malformed. We have no information on registered extensions. Continue without waiting.
			return false;
		}

		// Return true if extension is in the list meaning we need to wait for shared state update
		return extensionDetailsMap.containsKey(extensionName);
	}

	void handleOptOut(final Event event) {
		final EventData configuration = getSharedEventState(IdentityConstants.EventDataKeys.Configuration.MODULE_NAME, event);

		//Check to see if at this moment we still have a valid config
		if (configuration == EventHub.SHARED_STATE_PENDING) {
			return;
		}

		//If the AAM server is configured let AAM handle opt out, else we send the opt out hit
		if (!configuration.containsKey(IdentityConstants.EventDataKeys.Configuration.AAM_CONFIG_SERVER)) {
			//Otherwise, check to see if currently we are still opt_out, and if so, send the OPT OUT hit
			ConfigurationSharedStateIdentity configSharedState = new ConfigurationSharedStateIdentity();
			configSharedState.getConfigurationProperties(configuration);

			if (configSharedState.privacyStatus.equals(MobilePrivacyStatus.OPT_OUT)) {
				sendOptOutHit(configSharedState);
			}
		}
	}

	/**
	 * Handler for sync identifiers calls
	 * <p>
	 * Calling this method will result in a Visitor ID Sync call being queued in the {@link IdentityHitsDatabase}
	 *
	 * @param event {@code Event} containing identifiers that need to be synced
	 * @param configSharedState {@code ConfigurationSharedStateIdentity} valid for this event
	 * @return true if the {@code event} was successfully processed, false if the {@code event} could not be processed
	 * at this time
	 */
	boolean handleSyncIdentifiers(final Event event, final ConfigurationSharedStateIdentity configSharedState) {
		if (configSharedState == null) {
			// sanity check, should never get here
			Log.debug(LOG_SOURCE, "handleSyncIdentifiers : Ignoring the Sync Identifiers call because the configuration was null.");
			return true;
		}

		// do not even extract any data if the config is opt_out.
		if (privacyStatus == MobilePrivacyStatus.OPT_OUT) {
			Log.debug(LOG_SOURCE,
					  "handleSyncIdentifiers : Ignoring the Sync Identifiers call because the privacy status was opt-out.");
			// did process this event but can't sync the call. Hence return true.
			return true;
		}

		if (event == null) {
			Log.debug(LOG_SOURCE, "handleSyncIdentifiers : Ignoring the Sync Identifiers call because the event sent was null.");
			return true;
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
				Log.debug(LOG_SOURCE,
						  "handleSyncIdentifiers : Unable to process sync identifiers request as the configuration did not contain a valid Experience Cloud organization ID. Will attempt to process event when a valid configuration is received.");
				return false;
			}
		}

		// check privacy again from the configuration object
		if (currentEventValidConfig.privacyStatus == MobilePrivacyStatus.OPT_OUT) {
			Log.debug(LOG_SOURCE,
					  "handleSyncIdentifiers : Ignored the Sync Identifiers call because the privacy status was opt-out.");
			return true; // cannot process event due to privacy setting, remove from queue
		}

		// if the marketingCloudServer is null or empty use the default server
		if (StringUtils.isNullOrEmpty(currentEventValidConfig.marketingCloudServer)) {
			currentEventValidConfig.marketingCloudServer = IdentityConstants.Defaults.SERVER;
			Log.debug(LOG_SOURCE,
					  "handleSyncIdentifiers : The experienceCloud.server was empty is the configuration, hence used the default server: (%s).",
					  currentEventValidConfig.marketingCloudServer);
		}

		// AMSDK-6861
		// When updating the push identifier, if the value changes from empty to set or vice versa,
		// an Analytics Request Content event is dispatched to track the enable/disable of the push ID.
		// This happens before the Identity shared state is created. However, Analytics doesn't (currently)
		// read the push ID from the Identity shared state when processing the event. If Analytics changes
		// to read the push ID, then the code here will need to change to dispatch the event after
		// creating the shared state.

		final EventData eventData = event.getData();

		// Extract dpId's
		final Map<String, String> dpids = extractDPID(eventData);

		// Extract identifiers
		final Map<String, String> identifiers = extractIdentifiers(eventData);

		// Extract Authentication state
		final VisitorID.AuthenticationState idState = VisitorID.AuthenticationState.fromInteger(
					eventData.optInteger(
						IdentityConstants.EventDataKeys.Identity.AUTHENTICATION_STATE, 0));

		// Extract isForceSync
		final boolean forceSync = eventData.optBoolean(IdentityConstants.EventDataKeys.Identity.FORCE_SYNC, false);

		List<VisitorID> currentCustomerIds = generateCustomerIds(identifiers, idState);

		// update adid if changed and extract the new adid value as VisitorID to be synced
		IdentityGenericPair<VisitorID, Boolean> adidPair = extractAndUpdateAdid(eventData);

		final boolean didAdidConsentChange = adidPair.getSecond();
		final VisitorID adidIdentifier = adidPair.getFirst();

		if (adidIdentifier != null) {
			currentCustomerIds.add(adidIdentifier);
		}

		// merge new identifiers with the existing ones and remove any VisitorIDs with empty id values
		// empty adid is also removed from the customerIds list by merging with the new ids then filtering out any empty ids
		customerIds = mergeCustomerIds(currentCustomerIds);
		customerIds = cleanupVisitorIdentifiers(customerIds);
		currentCustomerIds = cleanupVisitorIdentifiers(currentCustomerIds);

		// valid config: check if there's a need to sync. Don't if we're already up to date.
		if (shouldSync(currentCustomerIds, dpids, forceSync || didAdidConsentChange, currentEventValidConfig)) {
			final String urlString = buildURLString(currentCustomerIds, dpids, currentEventValidConfig, didAdidConsentChange);
			initializeDatabaseWithCurrentPrivacyStatus();
			database.queue(urlString, event, currentEventValidConfig);
		} else {
			// nothing to sync
			Log.debug(LOG_SOURCE,
					  "handleSyncIdentifiers : Ignoring ID sync because nothing new to sync after the last sync.");
		}

		// Update share state and persistence here. Any state changes from the server response will be included in the next shared state
		// it's more important to not block other extensions with an IdentityExtension pending state
		createSharedState(event.getEventNumber(), packageEventData());
		savePersistently();

		return true;
	}

	/**
	 * Extracts identifiers from the provided {@code eventData}.
	 * <p>
	 * Returns null if event data is null.
	 * If an advertising identifier is found in the event data and it is not null/empty, it appends it to the returned identifiers
	 *
	 * @param eventData {@code EventData} containing sync identifiers
	 *
	 * @return a map containing the identifiers or an empty map if event data is null or does not contain an any identifiers
	 */
	Map<String, String> extractIdentifiers(final EventData eventData) {
		Map<String, String> identifiers = new HashMap<String, String>();

		if (eventData == null || !eventData.containsKey(IdentityConstants.EventDataKeys.Identity.IDENTIFIERS)) {
			return identifiers;
		}

		final Map<String, String> identifiersMap = eventData.optStringMap(IdentityConstants.EventDataKeys.Identity.IDENTIFIERS,
				null);

		if (identifiersMap != null) {
			identifiers = new HashMap<String, String>(identifiersMap);
		}

		return identifiers;
	}

	/**
	 * Extracts the ADID from the provided {@code eventData} and updates the in-memory and persisted ADID if there is any value change.
	 * The new ADID value is returned as a {@link VisitorID} object to be synced along with other visitor identifiers.
	 * A boolean is returned to indicate if the ADID value changed to or from a null/empty value.
	 *
	 * @param eventData to be processed
	 * @return a {@link IdentityGenericPair} with the first element being the new ADID value extracted from the eventData or null if
	 * the eventData is null/empty or it does not contain an ADID, and the second element being a Boolean with value true
	 * if the ADID value changed to or from a null/empty value.
	 */
	IdentityGenericPair<VisitorID, Boolean> extractAndUpdateAdid(final EventData eventData) {
		VisitorID adidAsVisitorId = null;
		boolean didConsentChange = false;

		if (eventData == null || !eventData.containsKey(IdentityConstants.EventDataKeys.Identity.ADVERTISING_IDENTIFIER)) {
			return new IdentityGenericPair<VisitorID, Boolean>(adidAsVisitorId, didConsentChange);
		}

		// Extract Advertising Identifier
		try {
			String newAdid = eventData.optString(IdentityConstants.EventDataKeys.Identity.ADVERTISING_IDENTIFIER, "");

			// If ad id is all zeros, treat as if null/empty
			if (Defaults.ZERO_ADVERTISING_ID.equals(newAdid)) {
				newAdid = "";
			}

			// did the adid value change?
			if ((!newAdid.isEmpty() && !newAdid.equals(advertisingIdentifier))
					|| (newAdid.isEmpty() && !StringUtils.isNullOrEmpty(advertisingIdentifier))) {

				// Now we know the value changed, but did it change to/from null?
				// Handle case where advertisingIdentifier loaded from persistence with all zeros and new value is not empty.
				if (newAdid.isEmpty() || StringUtils.isNullOrEmpty(advertisingIdentifier)
						|| Defaults.ZERO_ADVERTISING_ID.equals(advertisingIdentifier)) {
					didConsentChange = true;
				}


				adidAsVisitorId = new VisitorID(IdentityConstants.UrlKeys.VISITOR_ID,
												IdentityConstants.EventDataKeys.Identity.ADID_DSID,
												newAdid,
												VisitorID.AuthenticationState.AUTHENTICATED);

				updateAdvertisingIdentifier(newAdid);
				Log.trace(LOG_SOURCE, "extractAndUpdateAdid : The advertising identifier was set to: (%s).", newAdid);
			}
		} catch (Exception e) {
			Log.error(LOG_SOURCE, "extractAndUpdateAdid : Unable to update the advertising identifier due to: (%s)", e);
		}

		return new IdentityGenericPair<VisitorID, Boolean>(adidAsVisitorId, didConsentChange);
	}

	/**
	 * Appends IdentityExtension data to the provided URL and returns it for use in Hybrid app web views
	 *
	 * @param event {@code Event} that contains the base URL
	 * @param configSharedState the Identity Configuration shared state object
	 */
	void handleAppendURL(final Event event,
						 final ConfigurationSharedStateIdentity configSharedState,
						 final EventData analyticsSharedState) {
		final EventData eventData = event.getData();
		final String urlString = eventData.optString(IdentityConstants.EventDataKeys.Identity.BASE_URL, null);

		appendVisitorInfoForURL(urlString, event.getResponsePairID(), configSharedState, analyticsSharedState);
	}

	void handleGetUrlVariables(final Event event,
							   final ConfigurationSharedStateIdentity configSharedState,
							   final EventData analyticsSharedState) {
		final StringBuilder idStringBuilder = generateVisitorIDURLPayload(configSharedState, analyticsSharedState);

		final EventData params = new EventData();
		params.putString(IdentityConstants.EventDataKeys.Identity.URL_VARIABLES, idStringBuilder.toString());
		handleIdentityResponseEvent("IDENTITY_URL_VARIABLES", params, event.getResponsePairID());
	}

	/**
	 * Updates the push token value in persistence if there is a value change and sets the
	 * analytics push sync flag if this is the first time {@code MobileCore#setPushIdentifier()} is called.
	 *
	 * @param pushId new push token value received from the event that needs to be updated
	 */
	void updatePushIdentifier(final String pushId) {
		pushIdentifier = pushId;

		if (!processNewPushToken(pushId)) {
			Log.debug(LOG_SOURCE,
					  "updatePushIdentifier : Ignored a push token (%s) as it matches with an existing token, and the push notification status will not be re-sent to Analytics.",
					  pushId);
			return;
		}

		if (pushId == null && !isPushEnabled()) {
			changePushStatusAndHitAnalytics(false);
			Log.debug(LOG_SOURCE, "updatePushIdentifier : First time sending a.push.optin false");
		} else if (pushId == null) { // push is enabled
			changePushStatusAndHitAnalytics(false);
		} else if (!isPushEnabled()) { // push ID is not null
			changePushStatusAndHitAnalytics(true);
		}
	}

	/**
	 * Compares the provided pushToken against the one in shared preferences (if it exists).
	 * If the push token is new, this method will store it in shared preferences
	 *
	 * @param pushToken new push token that should be compared against existing one
	 * @return true if the provided token does not match the existing one
	 */
	boolean processNewPushToken(final String pushToken) {
		final DataStore dataStore = getDataStore();

		if (dataStore == null) {
			Log.trace(LOG_SOURCE,
					  "processNewPushToken : Unable to update push settings because the LocalStorageService was not available.");
			return false;
		}

		final String existingPushToken = dataStore.getString(DataStoreKeys.PUSH_IDENTIFIER, null);
		final boolean analyticsSynced = dataStore.getBoolean(IdentityConstants.DataStoreKeys.ANALYTICS_PUSH_SYNC, false);
		final boolean areTokensEqual = (StringUtils.isNullOrEmpty(pushToken) && existingPushToken == null) ||
									   (existingPushToken != null && existingPushToken.equals(pushToken));

		// AMSDK-10414 process the update only if the value changed or if this is not the first time setting the push token to null
		if ((areTokensEqual && !StringUtils.isNullOrEmpty(pushToken)) || (areTokensEqual && analyticsSynced)) {
			return false;
		}

		// AMSDK-10414 if this is the first time setting the push identifier, update the value to avoid subsequent updates
		if (!analyticsSynced) {
			dataStore.setBoolean(IdentityConstants.DataStoreKeys.ANALYTICS_PUSH_SYNC, true);
		}

		// process token change in persistence
		if (!StringUtils.isNullOrEmpty(pushToken)) {
			dataStore.setString(DataStoreKeys.PUSH_IDENTIFIER, pushToken);
		} else {
			dataStore.remove(DataStoreKeys.PUSH_IDENTIFIER);
		}

		return true;
	}

	/**
	 * Adds IdentityExtension variables to the provided {@code baseURL} and dispatches an {@link Event} for the one-time listener
	 * @param baseURL {@code String } containing the base URL to which identifiers need to be appended
	 * @param pairID {@code String} event pair id for one-time listener
	 * @param configSharedState the Identity Configuration shared state object
	 * @param analyticsSharedState the Analytics shared state
	 */
	void appendVisitorInfoForURL(final String baseURL,
								 final String pairID,
								 final ConfigurationSharedStateIdentity configSharedState,
								 final EventData analyticsSharedState) {

		if (StringUtils.isNullOrEmpty(baseURL)) {
			// nothing to update, dispatch provided baseURL
			final EventData params = new EventData();
			params.putString(IdentityConstants.EventDataKeys.Identity.UPDATED_URL, baseURL);
			handleIdentityResponseEvent("IDENTITY_APPENDED_URL", params, pairID);
			return;
		}

		final StringBuilder modifiedURL = new StringBuilder(baseURL);
		final StringBuilder idStringBuilder = generateVisitorIDURLPayload(configSharedState, analyticsSharedState);

		if (!StringUtils.isNullOrEmpty(idStringBuilder.toString())) {
			// add separator based on if url contains query parameters
			int queryIndex = modifiedURL.indexOf("?");

			// account for anchors in url
			int anchorIndex = modifiedURL.indexOf("#");
			int insertIndex = anchorIndex > 0 ? anchorIndex : modifiedURL.length();

			// check for case where URL has no query but the fragment (anchor) contains a '?' character
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
		final EventData params = new EventData();
		params.putString(IdentityConstants.EventDataKeys.Identity.UPDATED_URL, modifiedURL.toString());
		handleIdentityResponseEvent("IDENTITY_APPENDED_URL", params, pairID);
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
		return String.format(Locale.US, "%019d%019d", most < 0 ? -most : most, least < 0 ? -least : least);
	}

	/**
	 * Generates a string with visitor ids where the values are url encoded.
	 *
	 * @param visitorIDs visitor id list
	 *
	 * @return url encoded customer identifiers string
	 */
	String generateURLEncodedValuesCustomerIdString(final List<VisitorID> visitorIDs) {
		if (visitorIDs == null || visitorIDs.isEmpty()) {
			Log.debug(LOG_SOURCE,
					  "generateURLEncodedValuesCustomerIdString : No Visitor ID exists in the provided list to generate for URL.");
			return null;
		}

		final StringBuilder customerIdString = new StringBuilder();

		for (VisitorID newVisitorID : visitorIDs) {
			customerIdString.append("&");
			customerIdString.append(IdentityConstants.UrlKeys.VISITOR_ID);
			customerIdString.append("=");
			customerIdString.append(UrlUtilities.urlEncode(newVisitorID.getIdType()));
			customerIdString.append(Defaults.CID_DELIMITER);

			final String urlEncodedID = UrlUtilities.urlEncode(newVisitorID.getId());

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
	 * Converts the {@code HashMap} containing {@code idType, id} key-value pairs to a list of {@link VisitorID} objects.
	 * Ignores {@code VisitorID}s with an empty or null idType value
	 * <p>
	 * Returns an empty list if {@code identifiers} param is null or empty
	 *
	 * @param identifiers map containing identifiers
	 * @param authenticationState authentication state
	 *
	 * @return {@code List<VisitorID>} list of generated {@code VisitorID}s
	 */
	List<VisitorID> generateCustomerIds(final Map<String, String> identifiers,
										final VisitorID.AuthenticationState authenticationState) {

		if (identifiers == null) {
			return Collections.emptyList();
		}

		final List<VisitorID> tempIds = new ArrayList<VisitorID>();

		for (Map.Entry<String, String> newID : identifiers.entrySet()) {
			try {
				VisitorID tempId = new VisitorID(IdentityConstants.UrlKeys.VISITOR_ID, newID.getKey(),
												 newID.getValue(), authenticationState);
				tempIds.add(tempId);
			} catch (final IllegalStateException ex) {
				Log.debug(LOG_SOURCE,
						  "generateCustomerIds : Unable to create Visitor IDs after encoding the provided list due to: (%s).", ex);
			}
		}

		return tempIds;
	}

	/**
	 * Packages {@link EventData} for the purposes of using it as Shared State
	 *
	 * @return {@code EventData} representing current {@link IdentityExtension} Shared State
	 */
	EventData packageEventData() {
		final EventData eventData = new EventData();

		if (!StringUtils.isNullOrEmpty(mid)) {
			eventData.putString(IdentityConstants.EventDataKeys.Identity.VISITOR_ID_MID, mid);
		}

		if (!StringUtils.isNullOrEmpty(advertisingIdentifier)) {
			eventData.putString(IdentityConstants.EventDataKeys.Identity.ADVERTISING_IDENTIFIER, advertisingIdentifier);
		}

		if (!StringUtils.isNullOrEmpty(pushIdentifier)) {
			eventData.putString(IdentityConstants.EventDataKeys.Identity.PUSH_IDENTIFIER, pushIdentifier);
		}

		if (!StringUtils.isNullOrEmpty(blob)) {
			eventData.putString(IdentityConstants.EventDataKeys.Identity.VISITOR_ID_BLOB, blob);
		}

		if (!StringUtils.isNullOrEmpty(locationHint)) {
			eventData.putString(IdentityConstants.EventDataKeys.Identity.VISITOR_ID_LOCATION_HINT, locationHint);
		}

		if (customerIds != null && !customerIds.isEmpty()) {
			eventData.putTypedList(IdentityConstants.EventDataKeys.Identity.VISITOR_IDS_LIST, customerIds,
								   VisitorID.VARIANT_SERIALIZER);
		}

		eventData.putLong(IdentityConstants.EventDataKeys.Identity.VISITOR_IDS_LAST_SYNC, lastSync);
		return eventData;
	}

	/**
	 * Parses the provided {@code idString} and generates a list of corresponding {@link VisitorID} objects. This method
	 * removes the duplicates (visitor ids with the same id type) by keeping only the last occurrence in the returned list.
	 *
	 * @param idString {@link String} to be parsed for valid {@code VisitorID} objects
	 * @return {@code List<VisitorID>} containing the objects represented in the {@code idString}, deduplicated by idTypes
	 */
	List<VisitorID> convertVisitorIdsStringToVisitorIDObjects(final String idString) {
		if (StringUtils.isNullOrEmpty(idString)) {
			return new ArrayList<VisitorID>();
		}

		final List<VisitorID> visitorIDs = new ArrayList<VisitorID>();
		String [] customerIdComponentsArray = idString.split("&");

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
	 * Generates a ECID Payload for use as query parameters in a URL.
	 * Extracts the experience cloud id from the config shared state and the analytics id and visitor identifier
	 * from the analytics shared state, if available.
	 *
	 * @param configSharedState the Identity Configuration shared state object
	 * @param analyticsSharedState the Analytics shared state
	 * @return {@link StringBuilder} containing the ECID payload for the URL
	 */
	StringBuilder generateVisitorIDURLPayload(final ConfigurationSharedStateIdentity configSharedState,
			final EventData analyticsSharedState) {
		final StringBuilder urlFragment = new StringBuilder();

		// construct the adobe_mc string
		String theIdString = appendKVPToVisitorIdString(null, IdentityConstants.UrlKeys.ADB_VISITOR_TIMESTAMP_KEY,
							 String.valueOf(TimeUtil.getUnixTimeInSeconds()));
		theIdString = appendKVPToVisitorIdString(theIdString, IdentityConstants.UrlKeys
					  .ADB_VISITOR_PAYLOAD_MARKETING_CLOUD_ID_KEY, mid);

		String vid = null;

		if (analyticsSharedState != null) {
			String aid = analyticsSharedState.optString(IdentityConstants.EventDataKeys.Analytics.ANALYTICS_ID, null);

			if (!StringUtils.isNullOrEmpty(aid)) {
				// add Analytics ID if found
				theIdString = appendKVPToVisitorIdString(theIdString, IdentityConstants.UrlKeys.ADB_VISITOR_PAYLOAD_ANALYTICS_ID_KEY,
							  aid);
			}

			// get Analytics VID for later
			vid = analyticsSharedState.optString(IdentityConstants.EventDataKeys.Analytics.VISITOR_IDENTIFIER, null);
		}

		// add Experience Cloud Org ID
		String orgId = configSharedState != null ? configSharedState.orgID : null;

		if (!StringUtils.isNullOrEmpty(orgId)) {
			theIdString = appendKVPToVisitorIdString(theIdString,
						  IdentityConstants.UrlKeys.ADB_VISITOR_PAYLOAD_MARKETING_CLOUD_ORG_ID,
						  orgId);
		}

		// after the adobe_mc string is created, we need to encode it again before adding it to the url
		urlFragment.append(IdentityConstants.UrlKeys.ADB_VISITOR_PAYLOAD_KEY);
		urlFragment.append("=");
		urlFragment.append(UrlUtilities.urlEncode(theIdString));

		// add 'adobe_aa_vid' if exists
		if (!StringUtils.isNullOrEmpty(vid)) {
			urlFragment.append("&");
			urlFragment.append(IdentityConstants.UrlKeys.ADB_ANALYTICS_PAYLOAD_KEY);
			urlFragment.append("=");
			urlFragment.append(UrlUtilities.urlEncode(vid));
		}

		return urlFragment;
	}

	/**
	 * Takes in a key-value pair and appends it to the source string
	 * <p>
	 * This method <b>does not</b> URL encode the provided {@code value} on the resulting string.
	 * If encoding is needed, make sure that the values are encoded before being passed into this function.
	 *
	 * @param originalString {@link String} to append the key and value to
	 * @param key key to append
	 * @param value value to append
	 *
	 * @return a new string with the key and value appended, or {@code originalString}
	 *         if {@code key} or {@code value} are null or empty
	 */
	String appendKVPToVisitorIdString(final String originalString, final String key, final String value) {
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
	 * Merges provided {@code newCustomerIds} with the existing {@code customerIds} and returns the resulted list;
	 * the existing identifiers (same idType) will be updated with the new id value and/or authentication state and
	 * the new identifiers will be appended to the {@code customerIds} list.
	 *
	 * @param newCustomerIds the new {@link VisitorID}s that need to be merged
	 * @return the identifiers merge result
	 */
	List<VisitorID> mergeCustomerIds(final List<VisitorID> newCustomerIds) {
		if (newCustomerIds == null || newCustomerIds.isEmpty()) {
			return customerIds;
		}

		final List<VisitorID> tempIds = customerIds != null ? new ArrayList<VisitorID>(customerIds) :
										new ArrayList<VisitorID>();

		for (final VisitorID newId : newCustomerIds) {
			VisitorID mergedId = null;
			VisitorID oldId = null;

			for (final VisitorID visitorID : tempIds) {
				// check if this is the same visitor id (same id type) with updated authentication state and/or id
				if (sameIdType(visitorID, newId)) {
					mergedId = new VisitorID(visitorID.getIdOrigin(), visitorID.getIdType(), newId.getId(), newId.getAuthenticationState());
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
	 * <p>
	 * Parses the response result, persists data locally, updates {@link IdentityExtension} shared state, and
	 * dispatches resulting {@link Event}, including a updateSharedState flag indicating if a shared state update should be executed.
	 * If this network response was triggered by a paired event, a paired event is also dispatched.
	 *
	 * @param result {@code HashMap<String, String>} containing the network response result
	 * @param pairID event pair id for one-time listeners
	 * @param stateVersion version to use for updating a shared state
	 */
	void networkResponseLoaded(final IdentityResponseObject result, final String pairID, final int stateVersion) {
		boolean requiresSharedStateUpdate = false;
		// regardless of response, update last sync time
		lastSync = TimeUtil.getUnixTimeInSeconds();

		// check privacy here in case the status changed while response was in-flight
		if (privacyStatus != MobilePrivacyStatus.OPT_OUT) {
			// update properties
			requiresSharedStateUpdate = handleNetworkResponseMap(result);

			// save persistently
			savePersistently();
		}

		// dispatch regular and paired response event
		EventData updatedResponse = packageEventData();

		if (requiresSharedStateUpdate) {
			// add updateSharedState event data key to indicate that an update is required for this response
			updatedResponse.putBoolean(IdentityConstants.EventDataKeys.Identity.UPDATE_SHARED_STATE, true);
		}

		handleIdentityResponseEvent("UPDATED_IDENTITY_RESPONSE", updatedResponse, null);

		if (!StringUtils.isNullOrEmpty(pairID)) {
			handleIdentityResponseEvent("UPDATED_IDENTITY_RESPONSE", updatedResponse, pairID);
		}
	}

	//**************************************************************
	// private methods
	//**************************************************************
	/**
	 * Loads default values of all the required fields in the {@link IdentityExtension} class
	 */
	private void loadDefaultValues() {
		latestValidConfig = null;
		mid = null;
		advertisingIdentifier = null;
		pushIdentifier = null;
		customerIds = null;
		blob = null;
		locationHint = null;
		lastSync = 0;
		ttl = Defaults.DEFAULT_TTL_VALUE;
		Log.debug(LOG_SOURCE, "loadDefaultValues : ECID Service did not return an ID, so generating one locally : " +
				  "(ttl: %d).", ttl);
	}

	/**
	 * Extracts all the DPIDs from the {@link EventData} parameter
	 * <p>
	 * If the {@code eventData} contains a push identifier, it saves it to the IdentityExtension {@link DataStore}
	 * <p>
	 * This method returns null if {@code eventData} does not contain a DPIDs map or if it contains an empty map
	 *
	 * @param eventData contains data necessary to process a sync identifier {@link Event}
	 *
	 * @return {@code Map<String, String>} of valid DPIDs
	 */
	private Map<String, String> extractDPID(final EventData eventData) {
		final Map<String, String> dpIDs = new HashMap<String, String>();

		// Extract pushIdentifier
		if (eventData.containsKey(IdentityConstants.EventDataKeys.Identity.PUSH_IDENTIFIER)) {
			try {
				Variant pushIdVariant = eventData.getVariant(IdentityConstants.EventDataKeys.Identity.PUSH_IDENTIFIER);
				String pushId;

				if (pushIdVariant.getKind().equals(VariantKind.NULL)) {
					pushId = null;
				} else {
					pushId = pushIdVariant.getString();
				}

				updatePushIdentifier(pushId);
				dpIDs.put(IdentityConstants.EventDataKeys.Identity.MCPNS_DPID, pushId);
			} catch (Exception e) {
				Log.error(LOG_SOURCE, "extractDPID : Unable to update the push identifier due to: (%s).", e);
			}
		}

		if (dpIDs.size() == 0) {
			return null;
		}

		return dpIDs;
	}

	/**
	 * Updates {@link #advertisingIdentifier} and writes it to the IdentityExtension {@link DataStore}
	 *
	 * @param adid advertising identifier string
	 */
	private void updateAdvertisingIdentifier(final String adid) {
		advertisingIdentifier = adid;
		savePersistently();
	}

	/**
	 * Updates the {@link #pushEnabled} field and dispatches an event to generate a corresponding Analytics request
	 *
	 * @param isEnabled whether the user is opted in to receive push notifications
	 */
	private void changePushStatusAndHitAnalytics(final boolean isEnabled) {
		setPushStatus(isEnabled);

		final HashMap<String, String>contextData = new HashMap<String, String>();
		contextData.put(IdentityConstants.EventDataKeys.Identity.EVENT_PUSH_STATUS, String.valueOf(isEnabled));

		final EventData analyticsData = new EventData();
		analyticsData.putString(IdentityConstants.EventDataKeys.Analytics.TRACK_ACTION,
								IdentityConstants.EventDataKeys.Identity.PUSH_ID_ENABLED_ACTION_NAME);
		analyticsData.putStringMap(IdentityConstants.EventDataKeys.Analytics.CONTEXT_DATA, contextData);

		idAnalyticsEventDispatcher.dispatchAnalyticsHit(analyticsData);
	}

	/**
	 * Determines whether the user is opted in to receive push notifications
	 *
	 * @return boolean indicating whether the user is opted in to receive push notifications
	 */
	private boolean isPushEnabled() {
		synchronized (_pushEnabledMutex) {
			final DataStore dataStore = getDataStore();

			if (dataStore == null) {
				Log.trace(LOG_SOURCE, "isPushEnabled : Unable to update push flag because the LocalStorageService was not available.");
				return false;
			}

			pushEnabled = dataStore.getBoolean(DataStoreKeys.PUSH_ENABLED, false);
		}

		return pushEnabled;
	}

	/**
	 * Updates the {@link #pushEnabled} flag in {@link DataStore} with the provided value.
	 *
	 * @param enabled new push status value to be updated
	 */
	private void setPushStatus(final boolean enabled) {
		synchronized (_pushEnabledMutex) {
			final DataStore dataStore = getDataStore();

			if (dataStore != null) {
				dataStore.setBoolean(DataStoreKeys.PUSH_ENABLED, enabled);
			} else {
				Log.trace(LOG_SOURCE, "setPushStatus : Unable to update push flag because the LocalStorageService was not available.");
			}

			pushEnabled = enabled;
			Log.trace(LOG_SOURCE, "setPushStatus : Push notifications status is now: " + (pushEnabled ? "Enabled" : "Disabled"));
		}
	}

	/**
	 * If {@link #idResponseEventDispatcher} is not null, dispatches an {@link Event} for the provided data
	 *
	 * @param eventName to be used to create the event object to be dispatched
	 * @param eventData to be used to create the event object to be dispatched
	 * @param pairID    for one time callback listener
	 */
	private void handleIdentityResponseEvent(final String eventName, final EventData eventData, final String pairID) {
		if (idResponseEventDispatcher != null) {
			idResponseEventDispatcher.dispatchResponse(eventName, eventData, pairID);
		}
	}

	/**
	 * If {@link #idConfigurationEventDispatcher} is not null, dispatches an {@link Event} for the provided data.
	 *
	 * @param eventData to be used to create the event object to be dispatched.
	 */
	private void handleIdentityConfigurationUpdateEvent(final EventData eventData) {
		if (idConfigurationEventDispatcher != null) {
			idConfigurationEventDispatcher.dispatchConfigUpdateRequest(eventData);
		}
	}

	/**
	 * Process a change to the global privacy status.
	 * Sets this extension's {@link MobilePrivacyStatus} reference. If the new status is {@link MobilePrivacyStatus#OPT_OUT}
	 * the identifiers are cleared, any queued events are cleared, and any database hits are deleted.
	 * When the privacy status changes from {@code MobilePrivacyStatus#OPT_OUT} to any other status, a new
	 * Experience Cloud ID (MID) is generated, the new ID is saved to local storage, a shared state is created,
	 * and the ID synced with the remote Identity Service.
	 *
	 * @param version the {@link Event} version for the {@code Configuration} change
	 * @param eventData the updated {@code Configuration} {@link EventData}
	 */
	void processPrivacyChange(final int version, final EventData eventData) {
		if (eventData == null) {
			return;
		}

		String privacyString = eventData.optString(IdentityConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY,
							   Defaults.DEFAULT_MOBILE_PRIVACY.getValue());

		MobilePrivacyStatus newPrivacyStatus = MobilePrivacyStatus.fromString(privacyString);

		if (privacyStatus == newPrivacyStatus) {
			return; // no change
		}

		privacyStatus = newPrivacyStatus;

		Log.trace(LOG_SOURCE, "processPrivacyChange : Processed privacy change request: [%d]. New privacy status is: (%s).",
				  version,
				  privacyStatus.getValue());

		if (privacyStatus == MobilePrivacyStatus.OPT_OUT) {
			mid = null;
			advertisingIdentifier = null;
			blob = null;
			locationHint = null;
			customerIds = null;

			final DataStore dataStore = getDataStore();

			if (dataStore != null) {
				dataStore.remove(DataStoreKeys.AID_SYNCED_KEY);
			}

			updatePushIdentifier(null);
			savePersistently(); // clear datastore
			createSharedState(version, packageEventData());
			clearEventsQueue();
		} else if (StringUtils.isNullOrEmpty(mid)) {
			// When changing privacy status from optedout, need to generate new Experience Cloud ID for the user
			// Queue up a request to sync the new ID with the Identity Service
			Event event = createForcedSyncEvent(version);
			eventsQueue.add(event);
			processEventQueue();
		}

		initializeDatabaseWithCurrentPrivacyStatus();
	}

	/**
	 * Clears events queue of pending events which may be cleared immediately.
	 * An IdentityExtension Response event is dispatched for each cleared event.
	 * Certain events, such as those requesting AppendUrl, will remain in the queue as
	 * they are still allowed regardless of privacy status.
	 */
	private void clearEventsQueue() {
		Iterator<Event> it = eventsQueue.iterator();

		while (it.hasNext()) {
			Event event = it.next();
			EventData data = event.getData();

			// clear events except for AppendUrl requests, those may continue even when optedout
			if (data == null || !data.containsKey(IdentityConstants.EventDataKeys.Identity.BASE_URL)) {
				// dispatch a response in case anyone is listening
				handleIdentityResponseEvent("IDENTITY_RESPONSE", data, event.getResponsePairID());
				it.remove();
			}
		}
	}

	/**
	 * If the {@link IdentityExtension} {@link DataStore} is available, writes {@code IdentityExtension} fields to persistence
	 */
	private void savePersistently() {
		final DataStore dataStore = getDataStore();

		if (dataStore == null) {
			Log.trace(LOG_SOURCE,
					  "savePersistently : Unable to save the IdentityExtension fields into persistence because the data store was null.");
			return;
		}

		storeOrRemovePersistentString(dataStore, DataStoreKeys.VISITOR_IDS_STRING, stringFromVisitorIdList(customerIds));
		storeOrRemovePersistentString(dataStore, DataStoreKeys.MARKETING_CLOUD_ID, mid);
		storeOrRemovePersistentString(dataStore, DataStoreKeys.PUSH_IDENTIFIER, pushIdentifier);
		storeOrRemovePersistentString(dataStore, DataStoreKeys.ADVERTISING_IDENTIFIER, advertisingIdentifier);
		storeOrRemovePersistentString(dataStore, DataStoreKeys.LOCATION_HINT, locationHint);
		storeOrRemovePersistentString(dataStore, DataStoreKeys.BLOB, blob);

		dataStore.setLong(DataStoreKeys.TTL, ttl);
		dataStore.setLong(DataStoreKeys.LAST_SYNC, lastSync);

		Log.trace(LOG_SOURCE,
				  "savePersistently : Successfully saved the Identity data into persistence.");
	}

	/**
	 * Helper to set or remove String from DataStore.
	 * @param store {@link DataStore}
	 * @param key {@link String} key in {@code DataStore}
	 * @param value {@link String} value for {@code key} in {@code store}
	 */
	private static void storeOrRemovePersistentString(final DataStore store, final String key, final String value) {
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
	 * @param configSharedState {@code ConfigurationSharedStateIdentity} configuration valid for this event
	 * @param addConsentFlag whether to add device consent flag to query parameters
	 *
	 * @return {@code String} containing a URL to be sent to the ECID Service
	 */
	private String buildURLString(final List<VisitorID> customerIds,
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
				// Need to add "integration code" so server-side knows which type of ad ID to opt-out
				queryParameters.put(IdentityConstants.UrlKeys.DEVICE_CONSENT, "0");
				queryParameters.put(IdentityConstants.UrlKeys.CONSENT_INTEGRATION_CODE,
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
		urlBuilder.enableSSL(Defaults.DEFAULT_SSL).addPath("id").setServer(configSharedState.marketingCloudServer)
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
	 * <p>
	 * The URL sends the {@link IdentityConstants.UrlKeys#ORGID} and {@link IdentityConstants.UrlKeys#MID}
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

		final Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put(IdentityConstants.UrlKeys.ORGID, configSharedState.orgID);
		queryParameters.put(IdentityConstants.UrlKeys.MID, mid);


		final URLBuilder urlBuilder = new URLBuilder();
		urlBuilder.enableSSL(Defaults.DEFAULT_SSL).addPath(IdentityConstants.UrlKeys.PATH_OPTOUT).setServer(
			configSharedState.marketingCloudServer)
		.addQueryParameters(queryParameters);

		return urlBuilder.build();
	}

	/**
	 * Determines whether or not an ID Sync is necessary
	 *
	 * @param identifiers {@code List<VisitorID>} for the current sync call
	 * @param dpids {@code Map<String, String>} of DPIDs for the current sync call
	 * @param forceResync {@code boolean} allowing an override to force the sync call
	 * @param configuration {@code ConfigurationSharedStateIdentity} configuration valid for this event
	 *
	 * @return whether identifiers should be synced to the server
	 */
	private boolean shouldSync(final List<VisitorID> identifiers,
							   final Map<String, String> dpids,
							   final boolean forceResync,
							   final ConfigurationSharedStateIdentity configuration) {
		boolean syncForProps = true;
		boolean syncForIds = true;

		if (!configuration.canSyncIdentifiersWithCurrentConfiguration()) {
			Log.debug(LOG_SOURCE, "shouldSync : Ignoring ID Sync due to privacy status opt-out or missing experienceCloud.org.");
			syncForProps = false;
		}

		final boolean needResync = (TimeUtil.getUnixTimeInSeconds() - lastSync > ttl) || forceResync;
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
	 * Takes the provided {@code Map<String, String>} of DPIDs and returns the corresponding query string of IDs.
	 * <p>
	 * Returns empty string if {@code dpids} is null or empty.
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
			internalIdString.append(UrlUtilities.urlEncode(entry.getKey()));
			internalIdString.append(Defaults.CID_DELIMITER);
			internalIdString.append(UrlUtilities.urlEncode(entry.getValue()));
		}

		if (internalIdString.charAt(0) == '&') {
			internalIdString.deleteCharAt(0);
		}

		return internalIdString.toString();
	}

	/**
	 * Cleanup method for a list of {@link VisitorID}s - it removes any identifier that has an empty/null id value
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
					// Note: Visitor ID service ignores identifiers with null/empty id values, but we do this cleanup for other
					// dependent extensions
					iterator.remove();
					Log.trace(LOG_SOURCE, "cleanupVisitorIdentifiers : VisitorID was discarded due to an empty/null identifier value.");
				}
			}
		} catch (NullPointerException e) {
			Log.error(LOG_SOURCE,
					  "cleanupVisitorIdentifiers : Caught NullPointerException while iterating through visitor identifiers: %s",
					  e.getLocalizedMessage());
		} catch (ClassCastException e) {
			Log.error(LOG_SOURCE,
					  "cleanupVisitorIdentifiers : Caught ClassCastException while iterating through visitor identifiers: %s",
					  e.getLocalizedMessage());
		}

		return cleanIdentifiers;
	}

	/**
	 * Generates a {@link VisitorID} object with a valid provided {@code customerIdString}
	 * <p>
	 * Returns null if the provided string does not represent a valid {@code VisitorID}
	 *
	 * @param customerIdString {@link String} representing a customer ID
	 * @return {@code VisitorID} object representing the values provided by the string parameter
	 */
	private VisitorID parseCustomerIDStringToVisitorIDObject(final String customerIdString) {

		// AMSDK-3868
		// in this case, having an equals sign in the value doesn't cause a crash (like it did in iOS),
		// but we are not handling loading the value from SharePreferences properly if
		// the value contains an equals, so the change needs to be made regardless
		final int firstEqualsIndex = customerIdString.indexOf('=');

		// quick out if there's no equals sign in our id string
		if (firstEqualsIndex == -1) {
			Log.debug(LOG_SOURCE,
					  "parseCustomerIDStringToVisitorIDObject : Unable to load Visitor ID from Shared Preferences: (%s).", customerIdString);
			return null;
		}

		String currentCustomerIdOrigin;
		String currentCustomerIdValue;

		// make sure we have a valid origin and value string
		try {
			currentCustomerIdOrigin = customerIdString.substring(0, firstEqualsIndex);
			currentCustomerIdValue = customerIdString.substring(firstEqualsIndex + 1);
		} catch (final IndexOutOfBoundsException ex) {
			Log.debug(LOG_SOURCE,
					  "parseCustomerIDStringToVisitorIDObject : Unable to load Visitor ID: (%s) from Shared Preference because the name or value was malformed as in the exception: (%s).",
					  customerIdString, ex);
			return null;
		}

		// make sure the value array has 3 entries
		final List<String> idInfo = Arrays.asList(currentCustomerIdValue.split(Defaults.CID_DELIMITER));

		if (idInfo.size() != IdentityConstants.ID_INFO_SIZE) {
			Log.debug(LOG_SOURCE,
					  "parseCustomerIDStringToVisitorIDObject : Unable to load Visitor ID from Shared Preferences because the value was malformed: (%s).",
					  currentCustomerIdValue);
			return null;
		}

		if (StringUtils.isNullOrEmpty(idInfo.get(1))) {
			Log.debug(LOG_SOURCE,
					  "parseCustomerIDStringToVisitorIDObject : Unable to load Visitor ID from Shared Preferences because the ECID had null or empty id: (%s).",
					  currentCustomerIdValue);
			return null;
		}

		try {
			return new VisitorID(currentCustomerIdOrigin, idInfo.get(0), idInfo.get(1),
								 VisitorID.AuthenticationState.fromInteger(Integer.parseInt(idInfo.get(2))));
		} catch (final NumberFormatException ex) {
			Log.debug(LOG_SOURCE,
					  "parseCustomerIDStringToVisitorIDObject : Unable to parse the ECID: (%s) due to an exception: (%s).",
					  customerIdString,
					  ex.getLocalizedMessage());
		} catch (final IllegalStateException ex) {
			Log.debug(LOG_SOURCE,
					  "parseCustomerIDStringToVisitorIDObject : Unable to create the ECID after encoding due to an exception: (%s).",
					  ex);
		}

		return null;
	}

	/**
	 * Takes a {@code List<VisitorID>} and returns a {@link String} representation
	 * <p>
	 * This method is used so the {@link VisitorID}s can be stored in the IdentityExtension {@link DataStore}
	 * <p>
	 * Returns empty string if the provided list is null or empty
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
	 * Parses the provided {@code IdentityResponseObject} and fetches latest data (blob, locationHint, ttl) and triggers the opt-out flow if needed.
	 * <p>
	 * If the {@code identityResponseObject} contains optOutList, this method will dispatch event for Configuration extension and update the privacy status.
	 * <p>
	 * If the {@code identityResponseObject} contains an error, this method will log the error and return.
	 * <p>
	 * If the response contains a valid {@code mid}, the following fields will be set:
	 * <ul>
	 *     <li>{@link #blob}</li>
	 *     <li>{@link #locationHint}</li>
	 *     <li>{@link #ttl}</li>
	 * </ul>
	 *
	 * @param identityResponseObject representing the parsed JSON response
	 * @return {@code boolean} indicating if there is a change in the local properties (mid, blob, locationHint)
	 */
	private boolean handleNetworkResponseMap(final IdentityResponseObject identityResponseObject) {
		boolean requiresSharedStateUpdate = false;

		if (identityResponseObject == null) {
			Log.debug(LOG_SOURCE,
					  "handleNetworkResponseMap : Received an empty JSON in response from ECID Service, so there is nothing to handle.");
			return requiresSharedStateUpdate;
		}

		if (identityResponseObject.optOutList != null && !identityResponseObject.optOutList.isEmpty()) {
			Log.debug(LOG_SOURCE,
					  "handleNetworkResponseMap : Received opt-out response from ECID Service, so updating the privacy status in the configuration to opt-out.");

			HashMap<String, Variant> updateConfig = new HashMap<String, Variant>();
			updateConfig.put(IdentityConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY,
							 Variant.fromString(MobilePrivacyStatus.OPT_OUT.getValue()));

			EventData eventData = new EventData();
			eventData.putVariantMap(IdentityConstants.EventDataKeys.Configuration.CONFIGURATION_REQUEST_CONTENT_UPDATE_CONFIG,
									updateConfig);
			handleIdentityConfigurationUpdateEvent(eventData);
		}

		//something's wrong - n/w call returned an error. update the pending state.
		if (!StringUtils.isNullOrEmpty(identityResponseObject.error)) {
			Log.warning(LOG_SOURCE, "handleNetworkResponseMap : ECID Service returned an error: (%s).",
						identityResponseObject.error);

			//should never happen bc we generate mid locally before n/w request.
			// Still, generate mid locally if there's none yet.
			if (mid == null) {
				// no valid id, generate locally
				mid = generateMID();
				requiresSharedStateUpdate = true;
			}

			return requiresSharedStateUpdate;
		}

		// Only update stored properties if mid in response is same as what we have locally
		if (!StringUtils.isNullOrEmpty(identityResponseObject.mid) && identityResponseObject.mid.equals(mid)) {
			try {
				if ((identityResponseObject.blob != null && !identityResponseObject.blob.equals(blob)) ||
						(StringUtils.isNullOrEmpty(identityResponseObject.blob) && !StringUtils.isNullOrEmpty(blob))) {
					requiresSharedStateUpdate = true;
				}

				if ((identityResponseObject.hint != null && !identityResponseObject.hint.equals(locationHint)) ||
						(StringUtils.isNullOrEmpty(identityResponseObject.hint) && !StringUtils.isNullOrEmpty(locationHint))) {
					requiresSharedStateUpdate = true;
				}

				blob = identityResponseObject.blob;
				locationHint = identityResponseObject.hint;
				ttl = identityResponseObject.ttl;
				Log.debug(LOG_SOURCE,
						  "handleNetworkResponseMap : ECID Service returned (mid: %s, blob: %s, hint: %s, ttl: %d).", mid, blob,
						  locationHint, ttl);
			} catch (final Exception ex) {
				Log.warning(LOG_SOURCE, "handleNetworkResponseMap : Error parsing the response from ECID Service : (%s).",
							ex);
			}
		}

		return requiresSharedStateUpdate;
	}

	/**
	 * Returns the {@link IdentityExtension} {@link DataStore}
	 * <p>
	 * Returns null if the {@link LocalStorageService} is unavailable
	 *
	 * @return {@link DataStore} for the {@code IdentityExtension}
	 */
	private DataStore getDataStore() {
		if (internalDataStore == null) {

			if (getPlatformServices() == null) {
				Log.debug(LOG_SOURCE, "getDataStore : Unable to get the data store as the platform services are not available.");
				return null;
			}

			final LocalStorageService localStorageService = getPlatformServices().getLocalStorageService();

			if (localStorageService == null) {
				Log.debug(LOG_SOURCE,
						  "getDataStore : Local storage service is null. Cannot fetch persisted values. Loading default values.");
				loadDefaultValues();
				return null;
			}

			internalDataStore = localStorageService.getDataStore(DataStoreKeys.IDENTITY_PROPERTIES_DATA_STORE_NAME);
		}

		return internalDataStore;
	}

	private Event createForcedSyncEvent(final int eventNumber) {
		final EventData eventData = new EventData();
		eventData.putBoolean(IdentityConstants.EventDataKeys.Identity.FORCE_SYNC, true);
		eventData.putBoolean(IdentityConstants.EventDataKeys.Identity.IS_SYNC_EVENT, true);
		eventData.putInteger(IdentityConstants.EventDataKeys.Identity.AUTHENTICATION_STATE,
							 VisitorID.AuthenticationState.UNKNOWN.getValue());

		final Event forcedSyncEvent = new Event.Builder("id-construct-forced-sync", EventType.IDENTITY,
				EventSource.REQUEST_IDENTITY).setData(eventData).build();
		forcedSyncEvent.setEventNumber(eventNumber);

		return forcedSyncEvent;
	}

	/**
	 * Checks if two {@link VisitorID}s have the same id type. This method is used for identifying if an id
	 * needs to be updated or if it is completely new.
	 *
	 * Find more context here: AMSDK-8729, AMSDK-3720
	 * @param visitorId1 first {@code VisitorID} to be compared
	 * @param visitorId2 second {@code VisitorID} to be compared
	 * @return status of the comparison between the two visitor identifier idTypes
	 * @see {@link #mergeCustomerIds(List)}
	 */
	private boolean sameIdType(final VisitorID visitorId1, final VisitorID visitorId2) {
		if (visitorId1 == null || visitorId2 == null) {
			return false;
		}

		return visitorId1.getIdType() != null ? visitorId1.getIdType().equals(visitorId2.getIdType()) : visitorId2.getIdType()
			   == null;
	}
}
