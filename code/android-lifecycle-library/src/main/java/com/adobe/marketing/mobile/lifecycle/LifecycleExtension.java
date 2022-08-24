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

import static com.adobe.marketing.mobile.lifecycle.LifecycleConstants.UNEXPECTED_NULL_VALUE;

import androidx.annotation.VisibleForTesting;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.EventSource;
import com.adobe.marketing.mobile.EventType;
import com.adobe.marketing.mobile.Extension;
import com.adobe.marketing.mobile.ExtensionApi;
import com.adobe.marketing.mobile.Lifecycle;
import com.adobe.marketing.mobile.Log;
import com.adobe.marketing.mobile.SharedStateResolution;
import com.adobe.marketing.mobile.SharedStateResult;
import com.adobe.marketing.mobile.SharedStateStatus;
import com.adobe.marketing.mobile.services.DeviceInforming;
import com.adobe.marketing.mobile.services.NamedCollection;
import com.adobe.marketing.mobile.services.ServiceProvider;

import java.util.HashMap;
import java.util.Map;

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
public class LifecycleExtension extends Extension {

	private static final String SELF_LOG_TAG                = "LifecycleExtension";
	private final NamedCollection lifecycleDataStore;
	private final DeviceInforming deviceInfoService;
	private final LifecycleV1Extension lifecycleV1;
	private final LifecycleV2Extension lifecycleV2;

	/**
	 * Constructor for the LifecycleExtension, must be called by inheritors.
	 * It is called by the Mobile SDK when registering the extension and it initializes the extension and registers event listeners.
	 *
	 * @param extensionApi {@code ExtensionApi} instance
	 */
	protected LifecycleExtension(final ExtensionApi extensionApi) {
		super(extensionApi);
		lifecycleDataStore = getDataStore();
		deviceInfoService = getDeviceInfoService();
		lifecycleV1 = new LifecycleV1Extension(lifecycleDataStore, deviceInfoService, getApi());
		lifecycleV2 = new LifecycleV2Extension(lifecycleDataStore, deviceInfoService, getApi());
	}

	/**
	 * This constructor is intended for testing purposes.
	 *
	 * @param extensionApi {@code ExtensionApi} instance
	 * @param namedCollection {@code NamedCollection} instance
	 * @param deviceInfoService {@code DeviceInforming} instance
	 */
	@VisibleForTesting
	protected LifecycleExtension(final ExtensionApi extensionApi,
								 final NamedCollection namedCollection,
								 final DeviceInforming deviceInfoService) {
		super(extensionApi);
		lifecycleDataStore = namedCollection;
		this.deviceInfoService = deviceInfoService;
		lifecycleV1 = new LifecycleV1Extension(lifecycleDataStore, deviceInfoService, getApi());
		lifecycleV2 = new LifecycleV2Extension(lifecycleDataStore, deviceInfoService, getApi());
	}

	/**
	 * This constructor is intended for testing purposes.
	 *
	 * @param extensionApi {@code ExtensionApi} instance
	 * @param namedCollection {@code NamedCollection} instance
	 * @param deviceInfoService {@code DeviceInforming} instance
	 * @param lifecycleV1Extension {@code LifecycleV1Extension} instance
	 * @param lifecycleV2Extension {@code LifecycleV2Extension} instance
	 */
	@VisibleForTesting
	protected LifecycleExtension(final ExtensionApi extensionApi,
								 final NamedCollection namedCollection,
								 final DeviceInforming deviceInfoService,
								 final LifecycleV1Extension lifecycleV1Extension,
								 final LifecycleV2Extension lifecycleV2Extension) {
		super(extensionApi);
		lifecycleDataStore = namedCollection;
		this.deviceInfoService = deviceInfoService;
		this.lifecycleV1 = lifecycleV1Extension;
		lifecycleV2 = lifecycleV2Extension;
	}

	@Override
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
		getApi().registerEventListener(EventType.GENERIC_LIFECYCLE, EventSource.REQUEST_CONTENT, this::handleLifecycleRequestEvent);
		getApi().registerEventListener(EventType.HUB, EventSource.BOOTED, this::handleEventHubBootEvent);
		getApi().registerEventListener(EventType.WILDCARD, EventSource.WILDCARD, this::updateLastKnownTimestamp);
	}

	@Override
	public boolean readyForEvent(Event event) {
		if (event.getType().equalsIgnoreCase(EventType.GENERIC_LIFECYCLE) && event.getSource().equalsIgnoreCase(EventSource.REQUEST_CONTENT)) {
			SharedStateResult configurationSharedState = getApi().getSharedState(LifecycleConstants.EventDataKeys.Configuration.MODULE_NAME, event, false, SharedStateResolution.ANY);
			if (configurationSharedState != null) {
				return configurationSharedState.status == SharedStateStatus.SET;
			} else {
				return false;
			}
		}
		return true;
	}

	/**
	 * Processes an event of type generic lifecycle and source request content
	 * @param event lifecycle request content {@code Event}
	 */
	void handleLifecycleRequestEvent(final Event event) {
		SharedStateResult configurationSharedState = getApi().getSharedState(LifecycleConstants.EventDataKeys.Configuration.MODULE_NAME,
				event,
				false,
				SharedStateResolution.ANY);

		if (configurationSharedState == null ||  configurationSharedState.status == SharedStateStatus.PENDING) {
			Log.trace(LifecycleConstants.LOG_TAG, "%s - Configuration is pending, waiting...", SELF_LOG_TAG);
			return;
		}

		Map<String, Object> eventData = event.getEventData();

		if (eventData == null) {
			Log.trace(LifecycleConstants.LOG_TAG, "%s - Failed to process lifecycle event '%s for %s'", SELF_LOG_TAG,
					UNEXPECTED_NULL_VALUE,
					event.getName());
			return;
		}

		String lifecycleAction = (String) eventData.get(LifecycleConstants.EventDataKeys.Lifecycle.LIFECYCLE_ACTION_KEY);

		if (LifecycleConstants.EventDataKeys.Lifecycle.LIFECYCLE_START.equals(lifecycleAction)) {
			Log.debug(LifecycleConstants.LOG_TAG, "%s - Starting lifecycle", SELF_LOG_TAG);
			startApplicationLifecycle(event, configurationSharedState.value);
		} else if (LifecycleConstants.EventDataKeys.Lifecycle.LIFECYCLE_PAUSE.equals(lifecycleAction)) {
			Log.debug(LifecycleConstants.LOG_TAG, "%s - Pausing lifecycle", SELF_LOG_TAG);
			pauseApplicationLifecycle(event);
		} else {
			Log.warning(LifecycleConstants.LOG_TAG, "%s - Failed to read lifecycle data from persistence", SELF_LOG_TAG);
		}
	}

	/**
	 * Processes event hub boot event
	 *
	 * @param event event hub boot event
	 */
	void handleEventHubBootEvent(final Event event) {
		lifecycleV1.processBootEvent(event);
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
	 * @param event current lifecycle event to be processed
	 * @param configurationSharedState configuration shared state data for this event
	 */
	private void startApplicationLifecycle(final Event event, final Map<String, Object> configurationSharedState) {
		boolean isInstall = isInstall();
		if (lifecycleV1.start(event, configurationSharedState, isInstall)) {
			lifecycleV2.start(event, isInstall);
		}
	}

	/**
	 * Pause the lifecycle session for standard and XDM workflows
	 *
	 * @param event current lifecycle event to be processed
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
		return lifecycleDataStore != null && !lifecycleDataStore.contains(LifecycleConstants.DataStoreKeys.INSTALL_DATE);
	}

	/**
	 * Fetches the {@link DeviceInforming} from {@link ServiceProvider}
	 *
	 * @return {@code DeviceInforming} or null if something went wrong
	 */
	private DeviceInforming getDeviceInfoService() {
		return ServiceProvider.getInstance().getDeviceInfoService();
	}

	/**
	 * Fetches the {@link NamedCollection} for LifecycleExtension from {@link ServiceProvider}
	 *
	 * @return {@code NamedCollection} for LifecycleExtension or null if something went wrong
	 */
	private NamedCollection getDataStore() {
		return ServiceProvider.getInstance().getDataStoreService().getNamedCollection(LifecycleConstants.DATA_STORE_NAME);
	}
}
