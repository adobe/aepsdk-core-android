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

import java.util.Map;

/**
 * Class that defines all the public methods an {@code Extension} may call to interface with the AEP SDK.
 */
public abstract class ExtensionApi {
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
	public abstract <T extends ExtensionListener> boolean registerEventListener(final String eventType,
																			 final String eventSource,
																			 final Class<T> extensionListenerClass,
																			 final ExtensionErrorCallback<ExtensionError> errorCallback);

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
	public abstract <T extends ExtensionListener> boolean registerWildcardListener(
			final Class<T> extensionListenerClass,
			final ExtensionErrorCallback<ExtensionError> errorCallback);

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
	public abstract boolean setSharedEventState(final Map<String, Object> state, final Event event,
			final ExtensionErrorCallback<ExtensionError> errorCallback);


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
	public abstract boolean setXDMSharedEventState(final Map<String, Object> state, final Event event,
			final ExtensionErrorCallback<ExtensionError> errorCallback);

	/**
	 * Called by extension to clear all shared state it has previously set. Usually called during {@code Extension.onUnregistered()}.
	 *
	 * @param errorCallback optional {@link ExtensionErrorCallback} which will be called if an error occurred
	 * @return {@code boolean} indicating if the shared states were successfully cleared
	 * @see Extension#onUnregistered()
	 */
	public abstract boolean clearSharedEventStates(final ExtensionErrorCallback<ExtensionError> errorCallback);

	/**
	 * Called by extension to clear XDM shared state it has previously set. Usually called during {@code Extension.onUnregistered()}.
	 *
	 * @param errorCallback optional {@link ExtensionErrorCallback} which will be called if an error occurred
	 * @return {@code boolean} indicating if the shared states were successfully cleared
	 * @see Extension#onUnregistered()
	 */
	public abstract boolean clearXDMSharedEventStates(final ExtensionErrorCallback<ExtensionError> errorCallback);

	/**
	 * Called by extension to get another extension's shared state. Usually called from a listener during event processing.
	 *
	 * @param stateName extension name for which to retrieve data. See documentation for the list of available states
	 * @param event the {@link Event} for which the state is being requested. Passing null will retrieve latest state available.
	 * @param errorCallback optional {@link ExtensionErrorCallback} which will be called if an error occurred or if {@code stateName} is null
	 * @return {@code Map<String, Object>} containing shared state data at that version. Returns null if state does not exists,
	 * 			is PENDING, or an error is returned in the {@code errorCallback}
	*/
	public abstract Map<String, Object> getSharedEventState(final String stateName, final Event event,
			final ExtensionErrorCallback<ExtensionError> errorCallback);


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
	public abstract Map<String, Object> getXDMSharedEventState(final String stateName, final Event event,
			final ExtensionErrorCallback<ExtensionError> errorCallback);


	/**
	 * Unregisters current extension.
	 * <p>
	 * This method executes asynchronously, unregistering the extension on the event hub thread. {@link Extension#onUnregistered}
	 * method will be called at the end of this operation.
	 *
	 * @see Extension#onUnregistered()
	 */
	public abstract void unregisterExtension();
}
