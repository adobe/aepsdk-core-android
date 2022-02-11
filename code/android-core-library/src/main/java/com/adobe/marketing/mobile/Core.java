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

package com.adobe.marketing.mobile;

import java.util.HashMap;
import java.util.Map;

class Core {
	private static final String LOG_TAG = Core.class.getSimpleName();
	private boolean startActionCalled;

	EventHub eventHub;

	Core(final PlatformServices platformServices) {
		this(platformServices, "undefined");
	}

	Core(final PlatformServices platformServices, final String coreVersion) {
		Log.setLoggingService(platformServices.getLoggingService());
		eventHub = new EventHub("AMSEventHub", platformServices, coreVersion);

		try {
			eventHub.registerModule(ConfigurationExtension.class, new ConfigurationModuleDetails(coreVersion));
		} catch (InvalidModuleException e) {
			Log.error(LOG_TAG, "Failed to register Configuration extension (%s)", e);
		}

		Log.trace(LOG_TAG, "Core initialization was successful");
	}

	Core(final PlatformServices platformServices, final EventHub eventHub) {
		Log.setLoggingService(platformServices.getLoggingService());
		this.eventHub = eventHub;
		Log.trace(LOG_TAG, "Core initialization was successful");
	}


	// ------------------------------------ 3rd party extensions --------------------------------------

	/**
	 * Registers an extension class which has {@code Extension} as parent.
	 *
	 * @param extensionClass a class whose parent is {@link Extension}
	 * @param errorCallback  an optional {@link ExtensionErrorCallback} for the eventuality of an error,
	 *                       called when this method returns false
	 */
	void registerExtension(final Class<? extends Extension> extensionClass,
						   final ExtensionErrorCallback<ExtensionError> errorCallback) {
		try {
			eventHub.registerExtension(extensionClass);
		} catch (InvalidModuleException e) {
			Log.debug(LOG_TAG, "Core.registerExtension - Failed to register extension class %s (%s)",
					  extensionClass.getSimpleName(), e);

			if (errorCallback != null) {
				errorCallback.error(ExtensionError.UNEXPECTED_ERROR);
			}
		}
	}

	/**
	 * Clones the provided {@code event} and dispatches it through the {@code EventHub}.
	 * Passes an {@link ExtensionError} to {@code errorCallback} if the event is null.
	 *
	 * @param event         provided {@link Event} from the public API
	 * @param errorCallback callback to return {@link ExtensionError} if an error occurs
	 * @return {@code boolean} indicating if the event was dispatched or not
	 */
	boolean dispatchEvent(final Event event, final ExtensionErrorCallback<ExtensionError> errorCallback) {
		if (event == null) {
			Log.debug(LOG_TAG, "%s (Core.dispatchEvent) - The event was not dispatched", Log.UNEXPECTED_NULL_VALUE);

			if (errorCallback != null) {
				errorCallback.error(ExtensionError.EVENT_NULL);
			}

			return false;
		}

		eventHub.dispatch(event);
		return true;
	}

	/**
	 * This method will be used when the provided {@code Event} is used as a trigger and a response event is expected in return.
	 * The returned event needs to be sent using {@link #dispatchResponseEvent(Event, Event, ExtensionErrorCallback)} method.
	 * <p>
	 * Clones the provided {@code event}, registers a {@link OneTimeListener} for the response and dispatches it through the
	 * {@code EventHub}. Passes an {@link ExtensionError} to {@code errorCallback} if the event is null.
	 *
	 * @param event            provided {@link Event} from the public API
	 * @param responseCallback {@link AdobeCallback} to be called with the response event received in the {@link OneTimeListener}
	 * @param errorCallback    callback to return {@link ExtensionError} if an error occurs
	 * @return {@code boolean} indicating if the paired event was dispatched or not
	 * @see #dispatchResponseEvent
	 */
	boolean dispatchEventWithResponseCallback(final Event event,
			final AdobeCallback<Event> responseCallback,
			final ExtensionErrorCallback<ExtensionError> errorCallback) {
		if (responseCallback == null) {
			Log.debug(LOG_TAG,
					  "%s (Core.dispatchEventWithResponseCallback) - The event was not dispatched", Log.UNEXPECTED_NULL_VALUE);

			if (errorCallback != null) {
				errorCallback.error(ExtensionError.CALLBACK_NULL);
			}

			return false;
		}

		if (event == null) {
			Log.debug(LOG_TAG, "%s (Core.dispatchEventWithResponseCallback) - The event was not dispatched",
					  Log.UNEXPECTED_NULL_VALUE);

			if (errorCallback != null) {
				errorCallback.error(ExtensionError.EVENT_NULL);
			}

			return false;
		}

		eventHub.registerOneTimeListener(event.getResponsePairID(), new Module.OneTimeListenerBlock() {
			@Override
			public void call(final Event e) {
				responseCallback.call(e);
			}
		});
		eventHub.dispatch(event);
		return true;
	}

	/**
	 * This method will be used when the provided {@code Event} is used as a trigger and a response event is expected in return.
	 * The returned event needs to be sent using {@link #dispatchResponseEvent(Event, Event, ExtensionErrorCallback)} method.
	 * <p>
	 * Clones the provided {@code event}, registers a {@link OneTimeListener} for the response and dispatches it through the
	 * {@code EventHub} if the given {@link Event} and {@link AdobeCallbackWithError} are not null.
	 * The one-time listener will be unregistered by the event hub after the response event is received, or when event processing timeout (5000ms) occurs.
	 *
	 * @param event            provided {@link Event} from the public API
	 * @param responseCallback {@link AdobeCallback} to be called with the response event received in the {@link OneTimeListener}
	 * @see #dispatchResponseEvent
	 */
	void dispatchEventWithResponseCallback(final Event event,
										   final AdobeCallbackWithError<Event> responseCallback) {
		if (event == null || responseCallback == null) {
			Log.debug(LOG_TAG,
					  "(Core.dispatchEventWithResponseCallback) - The event was not dispatched, the given Event or AdobeCallbackWithError is null");
			return;
		}

		eventHub.registerOneTimeListener(event.getResponsePairID(), new Module.OneTimeListenerBlock() {
			@Override
			public void call(final Event e) {
				responseCallback.call(e);
			}
		}, responseCallback);
		eventHub.dispatch(event);
	}

	/**
	 * This method will be used when a response event should be dispatched for a paired event that was previously sent
	 * using {@code dispatchEventWithResponseCallback}
	 * <p>
	 * Clones the provided {@code event}, sets the pair id associated with the request event's pair id and dispatches
	 * it through the {@link EventHub}. Passes an {@link ExtensionError} to {@code errorCallback} if the event is null.
	 *
	 * @param responseEvent provided response {@link Event} from the public API
	 * @param requestEvent  the trigger {@link Event} for the dispatched event
	 * @param errorCallback callback to return {@link ExtensionError} if an error occurs
	 * @return {@code boolean} indicating if the response event was dispatched or not
	 * @see #dispatchEventWithResponseCallback
	 */
	boolean dispatchResponseEvent(final Event responseEvent, final Event requestEvent,
								  final ExtensionErrorCallback<ExtensionError> errorCallback) {
		if (requestEvent == null) {
			Log.debug(LOG_TAG,
					  "%s (Core.dispatchResponseEvent) - The response event was not dispatched", Log.UNEXPECTED_NULL_VALUE);

			if (errorCallback != null) {
				errorCallback.error(ExtensionError.EVENT_NULL);
			}

			return false;
		}

		if (responseEvent == null) {
			Log.warning(LOG_TAG, "%s (Core.dispatchResponseEvent) - The response event was not dispatched",
						Log.UNEXPECTED_NULL_VALUE);

			if (errorCallback != null) {
				errorCallback.error(ExtensionError.EVENT_NULL);
			}

			return false;
		}

		responseEvent.setPairId(requestEvent.getResponsePairID());
		eventHub.dispatch(responseEvent);
		return true;
	}

	/**
	 * Registers an event listener for the provided event type and source.
	 *
	 * @param eventType required parameter, the event type as a valid string (not null or empty)
	 * @param eventSource required parameter, the event source as a valid string (not null or empty)
	 * @param callback required parameter, {@link AdobeCallbackWithError#call(Object)} will be called when the event is heard
	 */
	void registerEventListener(final String eventType, final String eventSource,
							   final AdobeCallbackWithError<Event> callback) {
		eventHub.registerEventListener(EventType.get(eventType), EventSource.get(eventSource), callback);
	}


	// ------------------------------ Configuration methods ------------------------------

	/**
	 * Load remote configuration specified by the given application ID.
	 * <p>
	 * Configure the SDK by downloading the remote configuration file hosted on Adobe servers
	 * specified by the given application ID. The configuration file is cached once downloaded
	 * and used in subsequent calls to this API. If the remote file is updated after the first
	 * download, the updated file is downloaded and replaces the cached file.
	 * <p>
	 * The {@code appId} is preserved, and on application restarts, the remote configuration file specified by {@code appId}
	 * is downloaded and applied to the SDK.
	 * <p>
	 * On failure to download the remote configuration file, the SDK is configured using the cached
	 * file if it exists, or if no cache file exists then the existing configuration remains unchanged.
	 * <p>
	 * Calls to this API will replace any existing SDK configuration except those set using
	 * {@link #updateConfiguration(Map)} or {@link #setPrivacyStatus(MobilePrivacyStatus)}. Configuration updates
	 * made using {@link #updateConfiguration(Map)} and {@link #setPrivacyStatus(MobilePrivacyStatus)}
	 * are always applied on top of configuration changes made using this API.
	 *
	 * @param appId a unique identifier assigned to the app instance by the Adobe Mobile Services. It is automatically
	 *              added to the Mobile configuration JSON file when downloaded from the Adobe Mobile Services UI and can be
	 *              found in Manage App Settings. A value of {@code null} or empty {@code String} will clear the preserved value.
	 */
	void configureWithAppID(final String appId) {
		EventData eventData = new EventData();
		eventData.putString(CoreConstants.EventDataKeys.Configuration.CONFIGURATION_REQUEST_CONTENT_JSON_APP_ID, appId);
		final Event event = new Event.Builder("Configure with AppID", EventType.CONFIGURATION,
											  EventSource.REQUEST_CONTENT).setData(eventData).build();
		eventHub.dispatch(event);
	}

	/**
	 * Load configuration from local file.
	 * <p>
	 * Configure the SDK by reading a local file containing the JSON configuration.  On application relaunch,
	 * the configuration from the file at {@code filepath} is not preserved and this method must be called again if desired.
	 * <p>
	 * On failure to read the file or parse the JSON contents, the existing configuration remains unchanged.
	 * <p>
	 * Calls to this API will replace any existing SDK configuration except those set using
	 * {@link #updateConfiguration(Map)} or {@link #setPrivacyStatus(MobilePrivacyStatus)}.
	 * Configuration updates made using {@link #updateConfiguration(Map)} and {@link #setPrivacyStatus(MobilePrivacyStatus)}
	 * are always applied on top of configuration changes made using this API.
	 *
	 * @param filepath absolute path to a local configuration file. A value of {@code null} has no effect.
	 */
	void configureWithFileInPath(final String filepath) {
		if (StringUtils.isNullOrEmpty(filepath)) {
			Log.warning("Configuration", "Unable to configure with null or empty remoteURL");
			return;
		}

		EventData eventData = new EventData();
		eventData.putString(CoreConstants.EventDataKeys.Configuration.CONFIGURATION_REQUEST_CONTENT_JSON_FILE_PATH, filepath);
		final Event event = new Event.Builder("Configure with FilePath", EventType.CONFIGURATION,
											  EventSource.REQUEST_CONTENT).setData(eventData).build();
		eventHub.dispatch(event);
	}

	/**
	 * Load configuration from an asset file.
	 * @param fileName  the name of the configure file in the assets folder. A value of {@code null} has no effect.
	 */
	void configureWithFileInAssets(final String fileName) {
		if (StringUtils.isNullOrEmpty(fileName)) {
			Log.warning("Configuration", "Unable to configure with null or empty file name");
			return;
		}

		EventData eventData = new EventData();
		eventData.putString(CoreConstants.EventDataKeys.Configuration.CONFIGURATION_REQUEST_CONTENT_JSON_ASSET_FILE, fileName);
		final Event event = new Event.Builder("Configure with FilePath", EventType.CONFIGURATION,
											  EventSource.REQUEST_CONTENT).setData(eventData).build();
		eventHub.dispatch(event);
	}

	/**
	 * Update specific configuration parameters.
	 * <p>
	 * Update the current SDK configuration with specific key/value pairs. Keys not found in the current
	 * configuration are added. Configuration updates are preserved and applied over existing or new
	 * configurations set by calling {@link #configureWithAppID(String)} or {@link #configureWithFileInPath(String)},
	 * even across application restarts.
	 * <p>
	 * Using {@code null} values is allowed and effectively removes the configuration parameter from the current configuration.
	 *
	 * @param configMap configuration key/value pairs to be updated or added. A value of {@code null} has no effect.
	 */
	void updateConfiguration(final Map<String, Object> configMap) {
		// Create a EventData Map <String, Object>
		HashMap<String, Variant> eventDataMap = new HashMap<String, Variant>();
		Variant configMapVariant = Variant.fromTypedMap(configMap, PermissiveVariantSerializer.DEFAULT_INSTANCE);
		eventDataMap.put(CoreConstants.EventDataKeys.Configuration.CONFIGURATION_REQUEST_CONTENT_UPDATE_CONFIG,
						 configMapVariant);
		EventData eventData = new EventData(eventDataMap);
		final Event event = new Event.Builder("Configuration Update", EventType.CONFIGURATION,
											  EventSource.REQUEST_CONTENT).setData(eventData).build();
		eventHub.dispatch(event);
	}

	/**
	 * Clear the changes made by {@link #updateConfiguration(Map)} to the initial configuration provided either by
	 * {@link #configureWithAppID(String)} or {@link #configureWithFileInPath(String)} or {@link #configureWithFileInAssets(String)}
	 */
	void clearUpdatedConfiguration() {
		EventData eventData = new EventData();
		eventData.putBoolean(CoreConstants.EventDataKeys.Configuration.CONFIGURATION_REQUEST_CONTENT_CLEAR_UPDATED_CONFIG,
							 true);
		final Event event = new Event.Builder("Clear updated configuration", EventType.CONFIGURATION,
											  EventSource.REQUEST_CONTENT).setData(eventData).build();
		eventHub.dispatch(event);
	}
	/**
	 * Gets the SDK's current version with wrapper type.
	 */
	String getSdkVersion() {
		return eventHub.getSdkVersion();
	}

	/**
	 * Sets the SDK's current wrapper type. This API should only be used if
	 * being developed on platforms such as React Native.
	 *
	 * @param wrapperType the type of wrapper being used.
	 */
	void setWrapperType(final WrapperType wrapperType) {
		eventHub.setWrapperType(wrapperType);
	}

	/**
	 * Set the Adobe Mobile Privacy status.
	 * <p>
	 * Sets the {@link MobilePrivacyStatus} for this SDK. The set privacy status is preserved and applied over any new
	 * configuration changes from calls to {@link #configureWithAppID(String)} or {@link #configureWithFileInPath(String)},
	 * even across application restarts.
	 *
	 * @param privacyStatus {@link MobilePrivacyStatus} to be set to the SDK
	 * @see MobilePrivacyStatus
	 */
	void setPrivacyStatus(final MobilePrivacyStatus privacyStatus) {
		final Map<String, Object> privacyStatusUpdateConfig = new HashMap<String, Object>();
		final String privacyStatusString = (privacyStatus == null ? null : privacyStatus.getValue());
		privacyStatusUpdateConfig.put(CoreConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY, privacyStatusString);
		updateConfiguration(privacyStatusUpdateConfig);
	}

	/**
	 * Get the current Adobe Mobile Privacy Status.
	 * <p>
	 * Gets the currently configured {@link MobilePrivacyStatus} and passes it as a parameter to the given
	 * {@link AdobeCallback#call(Object)} function.
	 *
	 * @param callback {@link AdobeCallback} instance which is invoked with the configured privacy status as a parameter
	 */
	void getPrivacyStatus(final AdobeCallback<MobilePrivacyStatus> callback) {
		if (callback == null) {
			return;
		}

		EventData eventData = new EventData();
		eventData.putBoolean(CoreConstants.EventDataKeys.Configuration.CONFIGURATION_REQUEST_CONTENT_RETRIEVE_CONFIG, true);
		Event event = new Event.Builder("PrivacyStatusRequest", EventType.CONFIGURATION, EventSource.REQUEST_CONTENT)
		.setData(eventData).build();


		final AdobeCallbackWithError adobeCallbackWithError = callback instanceof AdobeCallbackWithError ?
				(AdobeCallbackWithError) callback : null;

		eventHub.registerOneTimeListener(event.getResponsePairID(), new Module.OneTimeListenerBlock() {
			@Override
			public void call(final Event e) {
				EventData eventData = e.getData();
				callback.call(MobilePrivacyStatus.fromString(eventData.getString(ConfigurationConstants.EventDataKeys
							  .Configuration.GLOBAL_CONFIG_PRIVACY)));
			}
		}, adobeCallbackWithError);

		eventHub.dispatch(event);

	}

	/**
	 * Retrieve all identities stored by/known to the SDK in a JSON {@code String} format.
	 * <p>
	 * Dispatches an {@link EventType#CONFIGURATION} - {@link EventSource#REQUEST_IDENTITY} {@code Event}.
	 * <p>
	 * Returns an empty string if the SDK is unable to retrieve any identifiers.
	 *
	 * @param callback {@link AdobeCallback} instance which is invoked with all the known identifier in JSON {@link String} format
	 * @see AdobeCallback
	 */
	void getSdkIdentities(final AdobeCallback<String> callback) {
		if (callback == null) {
			return;
		}

		final AdobeCallbackWithError adobeCallbackWithError = callback instanceof AdobeCallbackWithError ?
				(AdobeCallbackWithError) callback : null;

		Event event = new Event.Builder("getSdkIdentities", EventType.CONFIGURATION, EventSource.REQUEST_IDENTITY).build();
		eventHub.registerOneTimeListener(event.getResponsePairID(), new Module.OneTimeListenerBlock() {
			@Override
			public void call(final Event e) {
				EventData eventData = e.getData();
				callback.call(eventData.optString(
								  ConfigurationConstants.EventDataKeys.Configuration.CONFIGURATION_RESPONSE_IDENTITY_ALL_IDENTIFIERS, "{}"));
			}
		}, adobeCallbackWithError);

		eventHub.dispatch(event);
	}

	/**
	 * Dispatches a track action request event.
	 *
	 * @param action      The action string
	 * @param contextData Any context data that needs to be associated with the {@code action} or {@code state}
	 */
	void trackAction(final String action, final Map<String, String> contextData) {
		EventData trackData = new EventData();
		trackData.putString(CoreConstants.EventDataKeys.Analytics.TRACK_ACTION, action);
		trackData.putStringMap(CoreConstants.EventDataKeys.Analytics.CONTEXT_DATA,
							   contextData == null ? new HashMap<String, String>() : contextData);
		Event event = new Event.Builder("Analytics Track", EventType.GENERIC_TRACK, EventSource.REQUEST_CONTENT)
		.setData(trackData).build();

		eventHub.dispatch(event);
	}

	/**
	 * Dispatches a track state request event.
	 *
	 * @param state       The state string
	 * @param contextData Any context data that needs to be associated with the {@code action} or {@code state}
	 */
	void trackState(final String state, final Map<String, String> contextData) {
		EventData trackData = new EventData();
		trackData.putString(CoreConstants.EventDataKeys.Analytics.TRACK_STATE, state);
		trackData.putStringMap(CoreConstants.EventDataKeys.Analytics.CONTEXT_DATA,
							   contextData == null ? new HashMap<String, String>() : contextData);
		Event event = new Event.Builder("Analytics Track", EventType.GENERIC_TRACK, EventSource.REQUEST_CONTENT)
		.setData(trackData).build();

		eventHub.dispatch(event);
	}

	/**
	 * Dispatches an event with the Advertising Identifier
	 *
	 * @param adid the advertising idenifier string.
	 */
	void setAdvertisingIdentifier(final String adid) {
		EventData eventData = new EventData();
		eventData.putString(CoreConstants.EventDataKeys.Identity.ADVERTISING_IDENTIFIER, adid);

		Event event = new Event.Builder("SetAdvertisingIdentifier", EventType.GENERIC_IDENTITY, EventSource.REQUEST_CONTENT)
		.setData(eventData)
		.build();

		eventHub.dispatch(event);

	}

	/**
	 * Dispatches an event with the push token
	 *
	 * @param registrationID push token that needs to be set.
	 */
	void setPushIdentifier(final String registrationID) {
		EventData eventData = new EventData();
		eventData.putString(CoreConstants.EventDataKeys.Identity.PUSH_IDENTIFIER, registrationID);

		Event event = new Event.Builder("SetPushIdentifier", EventType.GENERIC_IDENTITY, EventSource.REQUEST_CONTENT)
		.setData(eventData)
		.build();

		eventHub.dispatch(event);
	}

	/**
	 * Dispatches an event to resume/start a lifecycle session
	 *
	 * @param additionalContextData {@code Map<String, String>} context data
	 */
	void lifecycleStart(final Map<String, String> additionalContextData) {
		EventData eventData = new EventData();
		eventData.putString(CoreConstants.EventDataKeys.Lifecycle.LIFECYCLE_ACTION_KEY,
							CoreConstants.EventDataKeys.Lifecycle.LIFECYCLE_START);

		eventData.putStringMap(CoreConstants.EventDataKeys.Lifecycle.ADDITIONAL_CONTEXT_DATA, additionalContextData);
		Event event = new Event.Builder("LifecycleResume", EventType.GENERIC_LIFECYLE, EventSource.REQUEST_CONTENT)
		.setData(eventData)
		.build();

		eventHub.dispatch(event);
	}

	/**
	 * Dispatches an event to pause/stop a lifecycle session
	 */
	void lifecyclePause() {
		EventData eventData = new EventData();
		eventData.putString(CoreConstants.EventDataKeys.Lifecycle.LIFECYCLE_ACTION_KEY,
							CoreConstants.EventDataKeys.Lifecycle.LIFECYCLE_PAUSE);

		Event event = new Event.Builder("LifecyclePause", EventType.GENERIC_LIFECYLE, EventSource.REQUEST_CONTENT)
		.setData(eventData)
		.build();

		eventHub.dispatch(event);
	}

	/**
	 * Create collect PII event, which is listened by Rules Engine module to determine if the data matches any PII request.
	 *
	 * @param data the PII data to be collected, which will be used in Rules Engine comparison and request token replacement.
	 */
	void collectPii(final Map<String, String> data) {
		if (data == null || data.isEmpty()) {
			Log.debug(LOG_TAG, "Could not trigger PII, the data is null or empty.");
			return;
		}

		final EventData eventData = new EventData()
		.putStringMap(CoreConstants.EventDataKeys.Signal.SIGNAL_CONTEXT_DATA, data);
		eventHub.dispatch(new Event.Builder("CollectPII", EventType.GENERIC_PII,
											EventSource.REQUEST_CONTENT).setData(eventData).build());
		Log.trace(LOG_TAG, "Collect PII event was sent");
	}

	/**
	 * Create collect data event, which may contain deep link information, messages info or referrer data.
	 * Dispatches an {@link EventType#GENERIC_DATA} {@link EventSource#OS} event to the {@link EventHub}.
	 *
	 * @param marshalledData OS launch data marshalled as {@code Map<String, Object>}
	 */
	void collectData(final Map<String, Object> marshalledData) {
		if (marshalledData == null || marshalledData.isEmpty()) {
			Log.debug(LOG_TAG, "collectData: Could not dispatch generic data event, data is null or empty.");
			return;
		}

		Event event = new Event.Builder("CollectData", EventType.GENERIC_DATA, EventSource.OS)
		.setEventData(marshalledData)
		.build();
		eventHub.dispatch(event);
		Log.trace(LOG_TAG, "collectData: generic data OS event dispatched.");
	}

	/**
	 * Dispatches a generic identity event to notify extensions to reset their stored identities
	 */
	void resetIdentities() {
		Event event = new Event.Builder("Reset Identities Request", EventType.GENERIC_IDENTITY, EventSource.REQUEST_RESET)
		.build();

		eventHub.dispatch(event);
	}

	/**
	 * Start the Core processing. This should be called after the initial set of extensions have been registered.
	 * <p>
	 * This call will wait for any outstanding registrations to complete and then start event processing.
	 * You can use the callback to kickoff additional operations immediately after any operations kicked off during registration.
	 *
	 * @param completionCallback An optional {@link AdobeCallback} invoked after registrations are completed
	 */
	void start(final AdobeCallback completionCallback) {
		if (startActionCalled) {
			Log.debug(LOG_TAG, "Can't start Core more than once.");
			return;
		}

		startActionCalled = true;
		eventHub.finishModulesRegistration(completionCallback);
	}



}
