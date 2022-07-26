/* **************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2021 Adobe Systems Incorporated
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

import java.util.Map;

/**
 * LifecycleV2Extension class
 *
 * The responsibility of LifecycleV2Extension is to compute the application launch/close XDM metrics,
 * usually consumed by the Edge Network and related extensions
 */
class LifecycleV2Extension {
	private static final String SELF_LOG_TAG = "LifecycleV2Extension";
	private final LifecycleV2DataStoreCache dataStoreCache;
	private final LifecycleV2StateManager stateManager;
	private final LifecycleV2MetricsBuilder xdmMetricsBuilder;
	private final LocalStorageService.DataStore dataStore;
	private final SystemInfoService systemInfoService;
	private final LifecycleV2DispatcherApplicationState lifecycleV2DispatcherApplicationState;

	private final long BACKDATE_TIMESTAMP_OFFSET_MILLIS = 1000; //backdate timestamps by 1 second

	/**
	 * Constructor for the LifecycleV2Extension.
	 *
	 * @param dataStore   LocalStorageService.DataStore instance
	 * @param systemInfoService SystemInfoService instance
	 */
	LifecycleV2Extension(final LocalStorageService.DataStore dataStore, final SystemInfoService systemInfoService,
						 final LifecycleV2DispatcherApplicationState applicationStateDispatcher) {
		this(dataStore, systemInfoService, applicationStateDispatcher, null);
	}

	/**
	 * This constructor is intended for testing purposes.
	 *
	 * @param dataStore    LocalStorageService.DataStore instance
	 * @param systemInfoService SystemInfoService instance
	 * @param metricsBuilder	XDM LifecycleMetricsBuilder instance. If null, a new instance will be created
	 */
	LifecycleV2Extension(final LocalStorageService.DataStore dataStore, final SystemInfoService systemInfoService,
						 final LifecycleV2DispatcherApplicationState applicationStateDispatcher,
						 final LifecycleV2MetricsBuilder metricsBuilder) {
		this.dataStore = dataStore;
		this.systemInfoService = systemInfoService;
		this.lifecycleV2DispatcherApplicationState = applicationStateDispatcher;
		stateManager = new LifecycleV2StateManager();
		dataStoreCache = new LifecycleV2DataStoreCache(this.dataStore);
		xdmMetricsBuilder = metricsBuilder != null ? metricsBuilder : new LifecycleV2MetricsBuilder(this.systemInfoService);
	}

	/**
	 * Handles the start use-case as application launch XDM event. If a previous abnormal close was detected, an application close
	 * event will be dispatched first.
	 *
	 * @param event event containing lifecycle start data
	 * @param isInstall boolean indicating whether this is an application install scenario
	 */
	void start(final Event event, final boolean isInstall) {
		stateManager.updateState(LifecycleV2StateManager.State.START, new AdobeCallback<Boolean>() {
			@Override
			public void call(final Boolean updated) {
				if (!updated) {
					return;
				}

				// detect a possible crash/incorrect start/pause implementation
				if (!isInstall
						&& isCloseUnknown(dataStoreCache.getAppStartTimestampMillis(), dataStoreCache.getAppPauseTimestampMillis())) {
					// in case of an unknown close situation, use the last known app close event timestamp
					// if no close timestamp was persisted, backdate this event to start timestamp - 1 second
					Map<String, Object> appCloseXDMData =
						xdmMetricsBuilder.buildAppCloseXDMData(
							dataStoreCache.getAppStartTimestampMillis(),
							dataStoreCache.getCloseTimestampMillis(),
							event.getTimestamp() - BACKDATE_TIMESTAMP_OFFSET_MILLIS,
							true);
					// Dispatch application close event with xdm data
					lifecycleV2DispatcherApplicationState.dispatchApplicationClose(appCloseXDMData);
				}

				final long startTimestamp = event.getTimestamp();
				dataStoreCache.setAppStartTimestamp(startTimestamp);

				Map<String, Object> appLaunchXDMData = xdmMetricsBuilder.buildAppLaunchXDMData(startTimestamp, isInstall,
													   isUpgrade());
				Map<String, String> freeFormData = event.getData().optStringMap(
													   LifecycleConstants.EventDataKeys.Lifecycle.ADDITIONAL_CONTEXT_DATA, null);

				// Dispatch application launch event with xdm data
				lifecycleV2DispatcherApplicationState.dispatchApplicationLaunch(appLaunchXDMData,
						freeFormData);

				// persist App version to track App upgrades
				persistAppVersion();
			}
		});
	}

	/**
	 * Handles the pause use-case as application close XDM event.
	 *
	 * @param event event containing lifecycle pause timestamp
	 */
	void pause(final Event event) {
		stateManager.updateState(LifecycleV2StateManager.State.PAUSE, new AdobeCallback<Boolean>() {
			@Override
			public void call(final Boolean updated) {
				if (!updated) {
					return;
				}

				final long pauseTimestamp = event.getTimestamp();
				dataStoreCache.setAppPauseTimestamp(pauseTimestamp);

				Map<String, Object> appCloseXDMData = xdmMetricsBuilder.buildAppCloseXDMData(
						dataStoreCache.getAppStartTimestampMillis(), pauseTimestamp, pauseTimestamp, false);
				// Dispatch application close event with xdm data
				lifecycleV2DispatcherApplicationState.dispatchApplicationClose(appCloseXDMData);
			}
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
	 * This helper method identifies if the previous session ended due to an incorrect implementation or possible app crash.
	 *
	 * @param previousAppStart start timestamp from previous session (milliseconds)
	 * @param previousAppPause pause timestamp from previous session (milliseconds)
	 * @return the status of the previous app close, true if this is considered an unknown close event
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
			previousAppVersion = dataStore.getString(LifecycleV2Constants.DataStoreKeys.LAST_APP_VERSION, "");
		}

		return systemInfoService != null && !previousAppVersion.isEmpty()
			   && !previousAppVersion.equalsIgnoreCase(systemInfoService.getApplicationVersion());
	}


	/**
	 * Persist the application version into datastore
	 */
	private void persistAppVersion() {

		// Persist app version for xdm workflow
		if (dataStore != null && systemInfoService != null) {
			dataStore.setString(LifecycleV2Constants.DataStoreKeys.LAST_APP_VERSION, systemInfoService.getApplicationVersion());
		}
	}

}
