/* **************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2018 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 *
 * *************************************************************************/
package com.adobe.marketing.mobile;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * LifecycleExtension class
 *
 * The responsibility of LifecycleExtension is to handle the calculation and population of a base set of data within
 * the SDK. This data will consist of information about the lifecycle of the app involving launches, installs
 * and upgrades.
 *
 * This extension handles two main scenarios:
 * <ul>
 * 		<li> Computing standard lifecycle sessions, usually consumed by the Analytics extension</li>
 *  	<li> Computing the application launch/close XDM metrics, usually consumed by the Edge Network and related extensions</li>
 * </ul>
 */
class LifecycleExtension extends InternalModule {

	private static final String SELF_LOG_TAG                = "LifecycleExtension";

	private final Map<String, String> lifecycleContextData                = new HashMap<String, String>();
	private final Map<String, String> previousSessionLifecycleContextData = new HashMap<String, String>();
	private final LifecycleSession lifecycleSession;
	private final Queue<Event> eventQueue = new ConcurrentLinkedQueue<Event>();
	private final LifecycleV2Extension lifecycleV2;
	private LifecycleDispatcherResponseContent lifecycleDispatcherResponseContent;

	/**
	 * Constructor for the LifecycleExtension, must be called by inheritors.
	 * It is called by the Mobile SDK when registering the extension and it initializes the extension and registers event listeners.
	 *
	 * @param hub      EventHub instance of Event Hub that owns this extension
	 * @param services PlatformServices instance
	 */
	LifecycleExtension(final EventHub hub, final PlatformServices services) {
		super(LifecycleConstants.EventDataKeys.Lifecycle.MODULE_NAME, hub, services);
		lifecycleSession = new LifecycleSession(getDataStore());
		lifecycleV2 = new LifecycleV2Extension(getDataStore(), getSystemInfoService(),
											   createDispatcher(LifecycleV2DispatcherApplicationState.class));
		registerListeners();
		createDispatchers();
	}

	/**
	 * Register lifecycleExtension listeners
	 */
	private void registerListeners() {
		registerListener(EventType.GENERIC_LIFECYLE, EventSource.REQUEST_CONTENT, LifecycleListenerRequestContent.class);
		registerListener(EventType.HUB, EventSource.SHARED_STATE, LifecycleListenerSharedState.class);
		registerListener(EventType.HUB, EventSource.BOOTED, LifecycleListenerHubBooted.class);
		registerListener(EventType.WILDCARD, EventSource.WILDCARD, LifecycleV2ListenerWildcard.class);
	}

	/**
	 * Create lifecycleExtension dispatchers
	 */
	private void createDispatchers() {
		lifecycleDispatcherResponseContent = createDispatcher(LifecycleDispatcherResponseContent.class);
	}

	void queueEvent(final Event event) {
		if (event == null) {
			return;
		}

		eventQueue.add(event);
		processQueuedEvents();
	}

	Queue<Event> getEventQueue() {
		return eventQueue;
	}

	/**
	 * Check for application upgrade.
	 *
	 * @param coreData coreData collected from LifecycleMetrics Builder
	 */
	void checkForApplicationUpgrade(final Map<String, String> coreData) {
		// early out if this isn't an upgrade or if it is an install
		if (isInstall() || !isUpgrade()) {
			return;
		}

		// get a map of lifecycle data in shared preferences or memory
		final Map<String, String> lifecycleData = getContextData();

		// no data to update
		if (lifecycleData == null || lifecycleData.isEmpty()) {
			return;
		}

		// update the version in our map
		final String applicationIdentifier = coreData.get(LifecycleConstants.EventDataKeys.Lifecycle.APP_ID);
		lifecycleData.put(LifecycleConstants.EventDataKeys.Lifecycle.APP_ID, applicationIdentifier);

		if (lifecycleContextData.isEmpty()) {
			// update the previous session's map
			previousSessionLifecycleContextData.put(LifecycleConstants.EventDataKeys.Lifecycle.APP_ID, applicationIdentifier);

			// update data in local storage
			final LocalStorageService.DataStore dataStore = getDataStore();
			final JsonUtilityService jsonUtilityService = getJsonUtilityService();
			JsonUtilityService.JSONObject lifecycleJSON = null;

			if (jsonUtilityService != null) {
				lifecycleJSON = jsonUtilityService.createJSONObject(lifecycleData);
			}

			if (dataStore != null && lifecycleJSON != null) {
				dataStore.setString(LifecycleConstants.DataStoreKeys.LIFECYCLE_DATA, lifecycleJSON.toString());
			}
		} else {
			// if we have the map in memory, update it
			lifecycleContextData.putAll(lifecycleData);
		}
	}

	/**
	 * Start/resume a new lifecycle session
	 *
	 * @param event 		event containing lifecycle start data
	 * @param configuration current configuration shared state
	 * @param isInstall boolean indicating whether this is an application install scenario
	 */
	void start(final Event event, final EventData configuration, final boolean isInstall) {
		// add device info and application identifier
		final long startTimestampInSeconds = event.getTimestampInSeconds();
		final SystemInfoService systemInfoService = getSystemInfoService();
		final LocalStorageService.DataStore dataStore = getDataStore();

		final String previousOsVersion = dataStore.getString(LifecycleConstants.DataStoreKeys.OS_VERSION, "");
		final String previousAppId = dataStore.getString(LifecycleConstants.DataStoreKeys.APP_ID, "");

		Map<String, String> defaultData = new LifecycleMetricsBuilder(systemInfoService, dataStore, startTimestampInSeconds)
		.addCoreData().addGenericData().build();
		checkForApplicationUpgrade(defaultData);

		long sessionTimeoutInSeconds = configuration.optInteger(
										   LifecycleConstants.EventDataKeys.Configuration.LIFECYCLE_CONFIG_SESSION_TIMEOUT,
										   LifecycleConstants.DEFAULT_LIFECYCLE_TIMEOUT);
		LifecycleSession.SessionInfo previousSessionInfo = lifecycleSession.start(startTimestampInSeconds,
				sessionTimeoutInSeconds, defaultData);

		if (previousSessionInfo == null) {
			// Analytics extension needs adjusted start date to calculate timeSinceLaunch param.
			final long startTime = dataStore.getLong(LifecycleConstants.DataStoreKeys.START_DATE, 0L);
			updateLifecycleSharedState(event.getEventNumber(), startTime, getContextData());
			return;
		}

		lifecycleContextData.clear();

		Map<String, String> lifecycleData = new HashMap<String, String>();

		// determine config type (install, upgrade, or launch)
		if (isInstall) { // install hit
			LifecycleMetricsBuilder builder = new LifecycleMetricsBuilder(systemInfoService,
					dataStore, startTimestampInSeconds)
			.addInstallData()
			.addGenericData()
			.addCoreData();
			lifecycleData.putAll(builder.build());
		} else { // upgrade and launch hits
			LifecycleMetricsBuilder builder = new LifecycleMetricsBuilder(systemInfoService,
					dataStore, startTimestampInSeconds)
			.addLaunchData()
			.addUpgradeData(isUpgrade())
			.addCrashData(previousSessionInfo.isCrash())
			.addGenericData()
			.addCoreData();
			lifecycleData.putAll(builder.build());

			Map<String, String> sessionContextData = lifecycleSession.getSessionData(startTimestampInSeconds,
					sessionTimeoutInSeconds, previousSessionInfo);

			if (sessionContextData != null) {
				lifecycleData.putAll(sessionContextData);
			}

			if (!previousOsVersion.isEmpty()) {
				lifecycleData.put(LifecycleConstants.EventDataKeys.Lifecycle.PREVIOUS_OS_VERSION, previousOsVersion);
			}

			if (!previousAppId.isEmpty()) {
				lifecycleData.put(LifecycleConstants.EventDataKeys.Lifecycle.PREVIOUS_APP_ID, previousAppId);
			}
		}

		EventData eventData = event.getData();
		Map<String, String> additionalContextData = eventData.optStringMap(
					LifecycleConstants.EventDataKeys.Lifecycle.ADDITIONAL_CONTEXT_DATA, null);

		if (additionalContextData != null) {
			lifecycleData.putAll(additionalContextData);
		}

		final String advertisingIdentifier = getAdvertisingIdentifier(event);

		if (!StringUtils.isNullOrEmpty(advertisingIdentifier)) {
			lifecycleData.put(LifecycleConstants.EventDataKeys.Identity.ADVERTISING_IDENTIFIER,
							  advertisingIdentifier);
		}

		// Update lifecycle context data and persist lifecycle info into local storage
		lifecycleContextData.putAll(lifecycleData);

		persistLifecycleContextData(startTimestampInSeconds);

		updateLifecycleSharedState(event.getEventNumber(), startTimestampInSeconds, getContextData());

		// Dispatch a new event with session related data
		lifecycleDispatcherResponseContent.dispatchSessionStart(startTimestampInSeconds, getContextData(),
				previousSessionInfo.getStartTimestampInSeconds(), previousSessionInfo.getPauseTimestampInSeconds());
	}

	/**
	 * Pause lifecycle session
	 *
	 * @param event event containing lifecycle pause timestamp
	 */
	void pause(final Event event) {
		lifecycleSession.pause(event.getTimestampInSeconds());
	}

	/**
	 * Persist lifecycle context data.
	 *
	 * @param startTimestamp current event timestamp to be stored as last used date
	 */
	private void persistLifecycleContextData(final long startTimestamp) {
		LocalStorageService.DataStore dataStore = getDataStore();

		if (dataStore == null) {
			Log.debug(LifecycleConstants.LOG_TAG, "%s - Failed to update lifecycle data, %s (DataStore)", SELF_LOG_TAG,
					  Log.UNEXPECTED_NULL_VALUE);
			return;
		}

		JsonUtilityService jsonUtilityService = getJsonUtilityService();

		if (jsonUtilityService != null) {
			JsonUtilityService.JSONObject lifecycleJSON = jsonUtilityService.createJSONObject(lifecycleContextData);

			if (lifecycleJSON != null) {
				dataStore.setString(LifecycleConstants.DataStoreKeys.LIFECYCLE_DATA, lifecycleJSON.toString());
			}
		}

		dataStore.setLong(LifecycleConstants.DataStoreKeys.LAST_USED_DATE, startTimestamp);

		SystemInfoService systemInfoService = getSystemInfoService();

		if (systemInfoService != null) {
			dataStore.setString(LifecycleConstants.DataStoreKeys.LAST_VERSION, systemInfoService.getApplicationVersion());
		}
	}

	/**
	 * Gets lifecycle context data. If lifecycleContextData has elements, it returns a copy of the existing key-value pairs
	 * Otherwise, if previousSessionLifecycleContextData has elements, it returns a copy of the key-value pairs
	 * Otherwise it reads context data from persistance and returns the keys
	 *
	 * @return {@code Map<String, String>} lifecycle context data
	 */
	Map<String, String> getContextData() {
		// if we already have lifecycle data, return it
		if (!lifecycleContextData.isEmpty()) {
			return new HashMap<String, String>(lifecycleContextData);
		}

		if (!previousSessionLifecycleContextData.isEmpty()) {
			return new HashMap<String, String>(previousSessionLifecycleContextData);
		}

		previousSessionLifecycleContextData.putAll(getPersistedContextData());
		return new HashMap<String, String>(previousSessionLifecycleContextData);
	}

	/**
	 * Used for testing only
	 * Update lifecycle context data. Equivalent to calling putAll(contextData)
	 * on existing lifecycle data
	 *
	 * @param contextData  {@code Map<String, String>} context data to be updated
	 * @param stateVersion state version
	 */
	void updateContextData(final Map<String, String> contextData, final int stateVersion) {
		if (contextData == null) {
			return;
		}

		lifecycleContextData.putAll(contextData);
		EventData lifecycleSharedState = new EventData()
		.putStringMap(LifecycleConstants.EventDataKeys.Lifecycle.LIFECYCLE_CONTEXT_DATA,
					  new HashMap<String, String>(lifecycleContextData));

		final LocalStorageService.DataStore dataStore = getDataStore();
		final long startTime = dataStore != null ? dataStore.getLong(LifecycleConstants.DataStoreKeys.START_DATE, 0L) : 0;
		updateLifecycleSharedState(stateVersion, startTime, lifecycleContextData);
	}

	/**
	 * Gets persisted lifecycle context data from local storage.
	 *
	 * @return {@code Map<String, String>} persisted lifecycle context data
	 */
	Map<String, String> getPersistedContextData() {
		LocalStorageService.DataStore dataStore = getDataStore();
		JsonUtilityService jsonUtilityService = getJsonUtilityService();
		Map<String, String> contextData = new HashMap<String, String>();

		// if we didn't have any lifecycle data, pull what was persisted last
		if (dataStore != null && jsonUtilityService != null) {
			String lifecycleJSONString = dataStore.getString(LifecycleConstants.DataStoreKeys.LIFECYCLE_DATA, null);
			Map<String, String> persistedData = null;

			if (!StringUtils.isNullOrEmpty(lifecycleJSONString)) {
				JsonUtilityService.JSONObject lifecycleData = jsonUtilityService.createJSONObject(lifecycleJSONString);
				persistedData = jsonUtilityService.mapFromJsonObject(lifecycleData);
			}

			if (persistedData != null) {
				contextData.putAll(persistedData);
			} else {
				Log.warning(LifecycleConstants.LOG_TAG, "%s - Failed to read lifecycle data from persistence", SELF_LOG_TAG);
			}
		}

		return contextData;
	}

	/**
	 * Gets advertising identifier.
	 *
	 * @param event Event containing advertising identifier data
	 *
	 * @return the advertising identifier
	 */
	String getAdvertisingIdentifier(final Event event) {
		if (event == null) {
			Log.trace(LifecycleConstants.LOG_TAG, "%s - Failed to get advertising identifier, %s (Event)", SELF_LOG_TAG,
					  Log.UNEXPECTED_NULL_VALUE);
			return null;
		}

		EventData identitySharedState = getSharedEventState(LifecycleConstants.EventDataKeys.Identity.MODULE_NAME, event);

		if (identitySharedState == EventHub.SHARED_STATE_PENDING) {
			return null;
		}

		return identitySharedState.optString(LifecycleConstants.EventDataKeys.Identity.ADVERTISING_IDENTIFIER, null);
	}

	/**
	 * Starts processing all the queued events.
	 * Important, method accesses shared resource. Call in a thread-safe way.
	 */
	void processQueuedEvents() {
		while (!eventQueue.isEmpty()) {
			Event event = eventQueue.peek();

			EventData configuration = getSharedEventState(LifecycleConstants.EventDataKeys.Configuration.MODULE_NAME, event);

			if (configuration == EventHub.SHARED_STATE_PENDING) {
				Log.trace(LifecycleConstants.LOG_TAG, "%s - Configuration is pending, waiting...", SELF_LOG_TAG);
				return;
			}

			processRequestContentEvent(eventQueue.poll(), configuration);
		}
	}

	/**
	 * Checks if event's state owner is Configuration, if so starts processing the events
	 *
	 * @param event to be processed
	 */
	void handleSharedStateUpdateEvent(final Event event) {
		if (event == null) {
			Log.trace(LifecycleConstants.LOG_TAG, "%s - Failed to process state change event, %s(Event)", SELF_LOG_TAG,
					  Log.UNEXPECTED_NULL_VALUE);
			return;
		}

		EventData eventData = event.getData();

		if (eventData == null) {
			Log.trace(LifecycleConstants.LOG_TAG, "%s - Failed to process state change event, %s (Data)", SELF_LOG_TAG,
					  Log.UNEXPECTED_NULL_VALUE);
			return;
		}

		String stateChangeOwner = eventData.optString(LifecycleConstants.EventDataKeys.STATE_OWNER, null);

		if (LifecycleConstants.EventDataKeys.Configuration.MODULE_NAME.equals(stateChangeOwner)) {
			processQueuedEvents();
		}
	}

	/**
	 * Updates the lifecycle shared state with current context data and default data when a boot event is received
	 *
	 * @param event to be processed
	 */
	void handleEventHubBootEvent(final Event event) {
		Map<String, String> contextData = new HashMap<String, String>();
		Map<String, String> currentContextData = getContextData();

		if (currentContextData != null) {
			contextData.putAll(currentContextData);
		}

		Map<String, String> defaultData = new LifecycleMetricsBuilder(getSystemInfoService(), getDataStore(),
				event.getTimestampInSeconds()).addCoreData().addGenericData().build();
		contextData.putAll(defaultData);

		updateLifecycleSharedState(event.getEventNumber(), 0, contextData);
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
	 * Processes the lifecycle request content event and calls start/pause based on the event type.
	 * It returns without doing anything if event data is null/empty.
	 *
	 * @param event						current lifecycle event to be processed
	 * @param configurationSharedState	configuration shared state data for this event
	 */
	private void processRequestContentEvent(final Event event, final EventData configurationSharedState) {
		EventData eventData = event.getData();

		if (eventData == null) {
			Log.trace(LifecycleConstants.LOG_TAG, "%s - Failed to process lifecycle event '%s for %s (%d)'", SELF_LOG_TAG,
					  Log.UNEXPECTED_NULL_VALUE,
					  event.getName(),
					  event.getEventNumber());
			return;
		}

		String lifecycleAction = eventData.optString(LifecycleConstants.EventDataKeys.Lifecycle.LIFECYCLE_ACTION_KEY, null);

		if (LifecycleConstants.EventDataKeys.Lifecycle.LIFECYCLE_START.equals(lifecycleAction)) {
			startApplicationLifecycle(event, configurationSharedState);
		} else if (LifecycleConstants.EventDataKeys.Lifecycle.LIFECYCLE_PAUSE.equals(lifecycleAction)) {
			pauseApplicationLifecycle(event);
		} else {
			Log.trace(LifecycleConstants.LOG_TAG, "%s - Failed to process lifecycle event, invalid action (%s)", SELF_LOG_TAG,
					  lifecycleAction);
		}
	}

	/**
	 * Start the lifecycle session for standard and XDM workflows
	 *
	 * @param event current lifecycle event to be processed
	 * @param configurationSharedState configuration shared state data for this event
	 */
	private void startApplicationLifecycle(final Event event,  final EventData configurationSharedState) {
		boolean isInstall = isInstall();
		start(event, configurationSharedState, isInstall);
		lifecycleV2.start(event, isInstall);

		if (isInstall) {
			persistInstallDate(event);
		}
	}

	/**
	 * Pause the lifecycle session for standard and XDM workflows
	 *
	 * @param event current lifecycle event to be processed
	 */
	private void pauseApplicationLifecycle(final Event event) {
		pause(event);
		lifecycleV2.pause(event);
	}


	/**
	 * Persist Application install date.
	 *
	 * @param event lifecycle start event.
	 */
	private void persistInstallDate(final Event event) {
		LocalStorageService.DataStore dataStore = getDataStore();

		if (dataStore == null) {
			return;
		}

		final long startTimestampInSeconds = event.getTimestampInSeconds();
		dataStore.setLong(LifecycleConstants.DataStoreKeys.INSTALL_DATE, startTimestampInSeconds);
	}

	/**
	 * Check if install has been processed
	 *
	 * @return boolean whether app install has been processed before
	 */
	private boolean isInstall() {
		LocalStorageService.DataStore dataStore = getDataStore();
		return dataStore != null && !dataStore.contains(LifecycleConstants.DataStoreKeys.INSTALL_DATE);
	}

	/**
	 * Check if the application has been upgraded
	 *
	 * @return boolean whether the app has been upgraded
	 */
	private boolean isUpgrade() {
		final LocalStorageService.DataStore dataStore = getDataStore();
		String previousAppVersion = "";

		if (dataStore != null) {
			previousAppVersion = dataStore.getString(LifecycleConstants.DataStoreKeys.LAST_VERSION, "");
		}

		final SystemInfoService systemInfoService = getSystemInfoService();
		return systemInfoService != null && !previousAppVersion.isEmpty()
			   && !previousAppVersion.equalsIgnoreCase(systemInfoService.getApplicationVersion());
	}

	/**
	 * Fetches the System Info Service from PlatformServices
	 *
	 * @return SystemInfoServiceInterface or null if something went wrong
	 */
	private SystemInfoService getSystemInfoService() {
		PlatformServices platformService = getPlatformServices();

		if (platformService == null) {
			Log.debug(LifecycleConstants.LOG_TAG, "%s - Unable to retrieve System Services, %s (Platform Services)", SELF_LOG_TAG,
					  Log.UNEXPECTED_NULL_VALUE);
			return null;
		}

		return platformService.getSystemInfoService();
	}

	/**
	 * Fetches the Json Utility Service from PlatformServices
	 *
	 * @return JsonUtilityServiceInterface or null if something went wrong
	 */
	private JsonUtilityService getJsonUtilityService() {
		final PlatformServices platformService = getPlatformServices();

		if (platformService == null) {
			Log.debug(LifecycleConstants.LOG_TAG, "%s - Unable to retrieve JsonUtilityService, %s (Platform Services)",
					  SELF_LOG_TAG, Log.UNEXPECTED_NULL_VALUE);
			return null;
		}

		return platformService.getJsonUtilityService();
	}

	/**
	 * Fetches the Data Store for LifecycleExtension from the PlatformServicesInterface
	 *
	 * @return DataStoreInterface for LifecycleExtension Data Store or null if something went wrong
	 */
	private LocalStorageService.DataStore getDataStore() {
		final PlatformServices platformService = getPlatformServices();

		if (platformService == null) {
			Log.debug(LifecycleConstants.LOG_TAG, "%s - Unable to retrieve LocalStorageService, %s (Platform Service)",
					  SELF_LOG_TAG, Log.UNEXPECTED_NULL_VALUE);
			return null;
		}

		LocalStorageService localStorageService = platformService.getLocalStorageService();

		if (localStorageService == null) {
			return null;
		}

		return localStorageService.getDataStore(LifecycleConstants.DATA_STORE_NAME);
	}

	/**
	 * Updates lifecycle shared state with current context data
	 *
	 * @param eventNumber the shared state version to be updated
	 * @param startTimestampInSeconds  The current session start timestamp in seconds
	 * @param contextData {@code Map<String, String>} context data to be updated
	 */
	private void updateLifecycleSharedState(final int eventNumber,
											final long startTimestampInSeconds,
											final Map<String, String> contextData) {
		EventData lifecycleSharedState = new EventData();
		lifecycleSharedState.putLong(LifecycleConstants.EventDataKeys.Lifecycle.SESSION_START_TIMESTAMP,
									 startTimestampInSeconds);
		lifecycleSharedState.putLong(LifecycleConstants.EventDataKeys.Lifecycle.MAX_SESSION_LENGTH,
									 LifecycleConstants.MAX_SESSION_LENGTH_SECONDS);
		lifecycleSharedState.putStringMap(LifecycleConstants.EventDataKeys.Lifecycle.LIFECYCLE_CONTEXT_DATA, contextData);
		createSharedState(eventNumber, lifecycleSharedState);
	}
}
