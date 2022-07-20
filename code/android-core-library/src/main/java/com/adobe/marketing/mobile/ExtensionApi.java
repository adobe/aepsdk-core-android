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

import com.adobe.marketing.mobile.internal.util.StringUtils;

import java.util.Map;

/**
 * Class that defines all the public methods an {@code Extension} may call to interface with the Adobe Cloud Platform SDK.
 *
 * @author Adobe Systems Incorporated
 * @version 5.0
 */
public final class ExtensionApi extends Module {
	private static final String              LOG_TAG = ExtensionApi.class.getSimpleName();
	private              Extension           extension;

	ExtensionApi(final EventHub hub) {
		super(null, hub); // moduleName is placeholder -- it will be set by `setExtension`
		this.extension = null;
	}

	/**
	 * Sets the {@code Extension} that this object is tied to. This should only be called by event hub.
	 * All calls after the first one will be ignored.
	 * @param extension the {@link Extension} the event hub will be calling back into from this object.
	 */
	final void setExtension(final Extension extension) {
		if (this.extension == null) {
			this.extension = extension;
			this.setModuleName(extension.getName());
			this.setModuleVersion(extension.getVersion());
		}
	}

	/**
	 * Gets the {@code Extension} that this object is tied to. Used to distinguish internal from external extensions.
	 * @return {@link Extension} reference
	 */
	final Extension getExtension() {
		return this.extension;
	}

	/**
	 * Called by the event hub when the extension is unregistered.
	 */
	protected final void onUnregistered() {
		if (this.extension != null) {
			this.extension.onUnregistered();
		}
	}

	/**
	 * Registers a new event listener for current extension for the provided event type and source.
	 * <p>
	 * This method executes asynchronously, returning immediately and registering the provided listener on the event hub
	 * thread. The provided {@link ExtensionListener}'s constructor will be called when the registration is completed.
	 *
	 * @param eventType required parameter, the event type as a valid string (not null or empty)
	 * @param eventSource required parameter, the event source as a valid string (not null or empty)
	 * @param extensionListenerClass required parameter, the listener class which extends the {@link ExtensionListener} parent class
	 * @param errorCallback optional {@link ExtensionErrorCallback} which will be called if any error occurs during registration
	 * @param <T> type of current event listener
	 * @return {@code boolean} indicating the listener registration status
	 */
	public final <T extends ExtensionListener> boolean registerEventListener(final String eventType,
			final String eventSource,
			final Class<T> extensionListenerClass,
			final ExtensionErrorCallback<ExtensionError> errorCallback) {

		if (StringUtils.isNullOrEmpty(eventType)) {
			Log.debug(getLogTag(), "%s.registerEventListener Event type cannot be null or empty.", LOG_TAG);

			if (errorCallback != null) {
				errorCallback.error(ExtensionError.EVENT_TYPE_NOT_SUPPORTED);
			}

			return false;
		}

		if (StringUtils.isNullOrEmpty(eventSource)) {
			Log.debug(getLogTag(), "%s.registerEventListener Event source cannot be null or empty.", LOG_TAG);

			if (errorCallback != null) {
				errorCallback.error(ExtensionError.EVENT_SOURCE_NOT_SUPPORTED);
			}

			return false;
		}

		if (extensionListenerClass == null) {
			Log.debug(getLogTag(), "%s (%s.registerEventListener Event listener class)", Log.UNEXPECTED_NULL_VALUE, LOG_TAG);

			if (errorCallback != null) {
				errorCallback.error(ExtensionError.UNEXPECTED_ERROR);
			}

			return false;
		}

		Log.trace(getLogTag(), "%s.registerEventListener called for event type '%s' and source '%s'.", LOG_TAG, eventType,
				  eventSource);
		EventType type = EventType.get(eventType);
		EventSource source = EventSource.get(eventSource);
		super.registerListener(type, source, extensionListenerClass);
		return true;
	}

	/**
	 * Registers a new wildcard event listener for the current extension. This listener will receive all events that are
	 * dispatched by the event hub.
	 * <p>
	 * You can register only one wildcard listener for your extension. If this method is called multiple times, the
	 * the existing wildcard listener will be unregistered before the new listener is registered.
	 * <p>
	 * This method executes asynchronously, returning immediately and registering the provided listener on the event hub
	 * thread. The provided {@link ExtensionListener}'s constructor will be called when the registration is completed.
	 * <p>
	 * A wildcard listener is intended for debugging purposes only and it is strongly recommended to not use it
	 * in a production environment.
	 *
	 * @param extensionListenerClass required parameter, the listener class which extends the {@link ExtensionListener} parent class
	 * @param errorCallback optional {@link ExtensionErrorCallback} which will be called if any error occurs during registration
	 * @param <T> type of current event listener
	 * @return {@code boolean} indicating the listener registration status
	 */
	public final <T extends ExtensionListener> boolean registerWildcardListener(
		final Class<T> extensionListenerClass,
		final ExtensionErrorCallback<ExtensionError> errorCallback) {

		if (extensionListenerClass == null) {
			Log.debug(getLogTag(), "%s (%s.registerWildcardListener Event listener class)", Log.UNEXPECTED_NULL_VALUE, LOG_TAG);

			if (errorCallback != null) {
				errorCallback.error(ExtensionError.UNEXPECTED_ERROR);
			}

			return false;
		}

		Log.debug(getLogTag(),
				  "Registering a wildcard listener. If this is a production environment, consider using the regular listener instead.");
		super.registerWildcardListener(extensionListenerClass);
		return true;
	}

	/**
	 * Called by extension to set a shared state for itself. Usually called from a listener during event processing.
	 *
	 * @param state {@code Map<String, Object>} representing current state of this extension. Passing null will set the extension's
	 *              shared state on pending until it is resolved. Another call with non-null state is expected in order
	 *              to resolve this share state.
	 * @param event The {@link Event} for which the state is being set. Passing null will set the state for the next shared
	 *              state version.
	 * @param errorCallback optional {@link ExtensionErrorCallback} which will be called if an error occurred
	 * @return {@code boolean} indicating if the shared state was successfully set
	 */
	public final boolean setSharedEventState(final Map<String, Object> state, final Event event,
			final ExtensionErrorCallback<ExtensionError> errorCallback) {
		return setSharedStateCommon(state, event, errorCallback, SharedStateType.STANDARD);
	}

	/**
	 * Called by extension to set an XDM shared state for itself. Usually called from a listener during event processing.
	 * The state passed to this API needs to be mapped to known XDM mixins.
	 * If an extension uses multiple mixins, the current data for all of them should be provided when the XDM shared state is set.
	 *
	 * @param state {@code Map<String, Object>} representing current XDM state of this extension. Passing null will set the extension's
	 *              XDM shared state on pending until it is resolved. Another call with non-null state is expected in order
	 *              to resolve this share state.
	 * @param event The {@link Event} for which the state is being set. Passing null will set the state for the next shared
	 *              state version.
	 * @param errorCallback optional {@link ExtensionErrorCallback} which will be called if an error occurred
	 * @return {@code boolean} indicating if the XDM shared state was successfully set
	 */
	public final boolean setXDMSharedEventState(final Map<String, Object> state, final Event event,
			final ExtensionErrorCallback<ExtensionError> errorCallback) {
		return setSharedStateCommon(state, event, errorCallback, SharedStateType.XDM);
	}

	private boolean setSharedStateCommon(final Map<String, Object> state, final Event event,
										 final ExtensionErrorCallback<ExtensionError> errorCallback, final SharedStateType sharedStateType) {
		boolean success = false;

		try {
			final EventData eventData = state != null ? EventData.fromObjectMap(state) : EventHub.SHARED_STATE_PENDING;

			if (event == null) {
				if (sharedStateType == SharedStateType.XDM) {
					createOrUpdateXDMSharedState(eventData);
				} else {
					createOrUpdateSharedState(eventData);
				}
			} else {
				if (sharedStateType == SharedStateType.XDM) {
					createOrUpdateXDMSharedState(event.getEventNumber(), eventData);
				} else {
					createOrUpdateSharedState(event.getEventNumber(), eventData);
				}
			}

			success = true;
		} catch (final Exception e) {
			String callerName = sharedStateType == SharedStateType.XDM ? "setXDMSharedEventState" : "setSharedEventState";
			Log.warning(getLogTag(), "%s.%s Failed to set the shared state. %s", LOG_TAG, callerName, e);

			if (errorCallback != null) {
				errorCallback.error(ExtensionError.UNEXPECTED_ERROR);
			}
		}

		return success;
	}

	/**
	 * Called by extension to clear all shared state it has previously set. Usually called during {@code Extension.onUnregistered()}.
	 *
	 * @param errorCallback optional {@link ExtensionErrorCallback} which will be called if an error occurred
	 * @return {@code boolean} indicating if the shared states were successfully cleared
	 * @see Extension#onUnregistered()
	 */
	public final boolean clearSharedEventStates(final ExtensionErrorCallback<ExtensionError> errorCallback) {
		return clearSharedStateCommon(errorCallback, SharedStateType.STANDARD);
	}

	/**
	 * Called by extension to clear XDM shared state it has previously set. Usually called during {@code Extension.onUnregistered()}.
	 *
	 * @param errorCallback optional {@link ExtensionErrorCallback} which will be called if an error occurred
	 * @return {@code boolean} indicating if the shared states were successfully cleared
	 * @see Extension#onUnregistered()
	 */
	public final boolean clearXDMSharedEventStates(final ExtensionErrorCallback<ExtensionError> errorCallback) {
		return clearSharedStateCommon(errorCallback, SharedStateType.XDM);
	}

	private boolean clearSharedStateCommon(final ExtensionErrorCallback<ExtensionError> errorCallback,
										   final SharedStateType sharedStateType) {
		try {
			if (sharedStateType == SharedStateType.XDM) {
				return super.clearXDMSharedStates();
			} else {
				return super.clearSharedStates();
			}
		} catch (final Exception e) {
			String callerName = sharedStateType == SharedStateType.XDM ? "clearXDMSharedEventStates" : "clearSharedEventStates";
			Log.warning(getLogTag(), "%s.%s Failed to clear the shared states. %s", LOG_TAG, callerName, e);

			if (errorCallback != null) {
				errorCallback.error(ExtensionError.UNEXPECTED_ERROR);
			}
		}

		return false;
	}

	/**
	 * Called by extension to get another extension's shared state. Usually called from a listener during event processing.
	 *
	 * @param stateName extension name for which to retrieve data. See documentation for the list of available states
	 * @param event the {@link Event} for which the state is being requested. Passing null will retrieve latest state available.
	 * @param errorCallback optional {@link ExtensionErrorCallback} which will be called if an error occurred or if {@code stateName} is null
	 * @return {@code Map<String, Object>} containing shared state data at that version. Returns null if state does not exists,
	 * 			is PENDING, or an error is returned in the {@code errorCallback}
	*/
	public final Map<String, Object> getSharedEventState(final String stateName, final Event event,
			final ExtensionErrorCallback<ExtensionError> errorCallback) {

		return getSharedStateCommon(stateName, event, errorCallback, SharedStateType.STANDARD);
	}


	/**
	 * Called by extension to get another extension's XDM shared state. Usually called from a listener during event processing.
	 * If the {@code stateName} extension populates multiple mixins in their shared state, all the data will be returned at once and it can be accessed using path discovery.
	 *
	 * @param stateName extension name for which to retrieve data. See documentation for the list of available states
	 * @param event the {@link Event} for which the state is being requested. Passing null will retrieve latest state available.
	 * @param errorCallback optional {@link ExtensionErrorCallback} which will be called if an error occurred or if {@code stateName} is null
	 * @return {@code Map<String, Object>} containing XDM shared state data at that version. Returns null if state does not exists,
	 * 			is PENDING, or an error is returned in the {@code errorCallback}
	 */
	public final Map<String, Object> getXDMSharedEventState(final String stateName, final Event event,
			final ExtensionErrorCallback<ExtensionError> errorCallback) {

		return getSharedStateCommon(stateName, event, errorCallback, SharedStateType.XDM);
	}

	private Map<String, Object> getSharedStateCommon(final String stateName, final Event event,
			final ExtensionErrorCallback<ExtensionError> errorCallback, final SharedStateType sharedStateType) {
		if (stateName == null) {
			String callerName = sharedStateType == SharedStateType.XDM ? "getXDMSharedEventState" : "getSharedEventState";
			Log.debug(getLogTag(), "%s (%s.%s State name)",
					  Log.UNEXPECTED_NULL_VALUE,
					  LOG_TAG,
					  callerName);

			if (errorCallback != null) {
				errorCallback.error(ExtensionError.UNEXPECTED_ERROR);
			}

			return null;
		}

		try {
			final EventData eventData;

			if (sharedStateType == SharedStateType.XDM) {
				eventData = super.getXDMSharedEventState(stateName, event);
			} else {
				eventData = super.getSharedEventState(stateName, event);
			}

			// if shared state is PENDING, eventData will be null
			return eventData == null ? null : eventData.toObjectMap();
		} catch (final Exception e) {
			String callerName = sharedStateType == SharedStateType.XDM ? "getXDMSharedEventState" : "getSharedEventState";
			Log.warning(getLogTag(), "%s.%s Failed to retrieve the shared state %s, %s",
						LOG_TAG, callerName, stateName, e);

			if (errorCallback != null) {
				errorCallback.error(ExtensionError.UNEXPECTED_ERROR);
			}
		}

		return null;
	}

	/**
	 * Unregisters current extension.
	 * <p>
	 * This method executes asynchronously, unregistering the extension on the event hub thread. {@link Extension#onUnregistered}
	 * method will be called at the end of this operation.
	 *
	 * @see Extension#onUnregistered()
	 */
	public final void unregisterExtension() {
		super.unregisterModule();
	}

	/**
	 * Returns the logging tag which can be either the module name if valid, or the class name otherwise
	 * @return {@link String} to be used as logging tag
	 */
	String getLogTag() {
		if (extension == null) {
			return LOG_TAG;
		}

		if (extension.getVersion() == null) {
			return extension.getName();
		}

		return extension.getName() + "(" + extension.getVersion() + ")";
	}
}
