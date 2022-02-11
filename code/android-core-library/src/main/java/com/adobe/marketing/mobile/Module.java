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

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Base class for the definition of a Module
 *
 * @author Adobe Systems Incorporated
 * @version 5.0
 */
@SuppressWarnings("unused")
abstract class Module {

	private String moduleName;
	private String moduleVersion;
	private final EventHub parentHub;

	private final Object executorMutex = new Object();
	private ExecutorService executor;

	private ModuleDetails moduleDetails;

	/**
	 * Defines a callback block used by the {@code OneTimeListener} functionality.
	 */
	interface OneTimeListenerBlock {
		/**
		 * called when the block is executed
		 *
		 * @param e {@code Event} to consume
		 */
		void call(final Event e);
	}

	/**
	 * Default constructor for Module, must be called by an inheritors.
	 *
	 * @param moduleName the name of the module
	 * @param hub EventHub to register this module to
	 */
	protected Module(final String moduleName, final EventHub hub) {
		this.parentHub = hub;
		this.moduleName = moduleName;
	}

	/**
	 * Creates, registers, and returns an event dispatcher for this module.
	 *
	 * @param dispatcherClass class extending {@code ModuleEventDispatcher<T>}
	 * @param <T>             type of the current module
	 *
	 * @return instance of the dispatcherClass registered with the current module and eventhub
	 */
	protected final <T extends ModuleEventDispatcher<?>> T createDispatcher(final Class<T> dispatcherClass) {
		T dispatcher = null;

		// first try to find a constructor using the instance class (normal case)
		Class expectedModuleClass = this.getClass();
		Constructor<T> dispatcherConstructor = null;

		try {
			dispatcherConstructor = dispatcherClass.getDeclaredConstructor(EventHub.class, expectedModuleClass);

		} catch (NoSuchMethodException firstTryError) {

			// next try using the instances classes parent class (mocking case)
			expectedModuleClass = expectedModuleClass.getSuperclass();

			try {
				dispatcherConstructor = dispatcherClass.getDeclaredConstructor(EventHub.class, expectedModuleClass);

			} catch (NoSuchMethodException secondTryError) {
				Log.error(moduleName, "Failed to find a constructor for class %s (%s)",
						  dispatcherClass.getSimpleName(), secondTryError);
			}
		}

		// construct the dispatcher
		if (dispatcherConstructor != null) {
			try {
				dispatcherConstructor.setAccessible(true);
				dispatcher = dispatcherConstructor.newInstance(parentHub, this);
			} catch (Exception constructionError) {
				Log.error(moduleName, "Failed to create dispatcher for class %s (%s)",
						  dispatcherClass.getSimpleName(), constructionError);
			}
		}

		return dispatcher;
	}

	/**
	 * Registers an event listener for this module
	 * Only one listener per type/source pair is allowed per module. Any collisions will be resolved by replacing
	 * the existing listener with the new listener when registerListener is called.
	 *
	 * @param type          {@code EventType} to listen for
	 * @param source        {@code EventSource} to listen for
	 * @param listenerClass class definition that extends {@code ModuleEventListener<T>}
	 * @param <T>           type of the current module
	 */
	protected final <T extends ModuleEventListener<?>> void registerListener(final EventType type,
			final EventSource source,
			final Class<T> listenerClass) {

		if (type == null || source == null || listenerClass == null) {
			Log.error(moduleName,
					  "Failed to register listener. EventType, EventSource and listenerClass must be non-null values");
			return;
		}

		try {
			parentHub.registerModuleListener(this, type, source, null, listenerClass);
		} catch (final InvalidModuleException e) {
			Log.debug(moduleName, "Failed to register listener (%s)", e);
		}
	}

	/**
	 * Registers a rule with the {@code EventHub} for this module.
	 *
	 * @param rule rule definition object containing the conditions to be met and the consequence events to be published.
	 */
	protected final void registerRule(final Rule rule) {
		try {
			parentHub.registerModuleRule(this, rule);
		} catch (final InvalidModuleException e) {
			Log.debug(moduleName, "Failed to register rule (%s)", e);
		}
	}

	protected final void replaceRules(final List<Rule> rules) {
		try {
			parentHub.replaceModuleRules(this, rules);
		} catch (final InvalidModuleException e) {
			Log.debug(moduleName, "Failed to register rule (%s)", e);
		}
	}

	/**
	 * Re-evaluates custom events with the provided rules, then registers the rules for this module
	 *
	 * @param rules {@code Rule} to register
	 * @param reprocessEventsHandler handler to return custom events
	 */
	protected final void replaceRulesAndEvaluateEvents(final List<Rule> rules,
			final ReprocessEventsHandler reprocessEventsHandler) {
		parentHub.replaceRulesAndEvaluateEvents(this, rules, reprocessEventsHandler);
	}


	/**
	 * Registers a wild card event listener for this module.
	 *
	 * <p>
	 *
	 * The wild card listener will receive all events that are dispatched by the event hub.
	 * Internally, this method uses the {@link #registerListener(EventType, EventSource, Class)} to register an event listener
	 * for {@link EventType#WILDCARD} and {@link EventSource#WILDCARD}.
	 *
	 * @param listenerClass class definition that extends {@code ModuleEventListener<T>}
	 * @param <T> type of the current module
	 */
	protected final <T extends ModuleEventListener<?>> void registerWildcardListener(final Class<T> listenerClass) {
		registerListener(EventType.WILDCARD, EventSource.WILDCARD, listenerClass);
	}

	/**
	 * Unregisters this module from the parent event hub
	 */
	protected final void unregisterModule() {
		try {
			parentHub.unregisterModule(this);
		} catch (InvalidModuleException e) {
			Log.debug(moduleName, "Failed to unregister module (%s)", e);
		}
	}

	/**
	 * Called from event hub thread prior to completely unregistering this.
	 */
	protected void onUnregistered() {
	}

	/**
	 * Unregisters a listener that matches the provided type/source
	 *
	 * @param type   {@code EventType} of target listener
	 * @param source {@code EventSource} of target listener
	 */
	protected final void unregisterListener(final EventType type, final EventSource source) {
		try {
			parentHub.unregisterModuleListener(this, type, source);

		} catch (InvalidModuleException e) {
			Log.debug(moduleName, "Failed to unregister listener (%s)", e);
		}
	}

	/**
	 * Unregisters a wild card listener that was registered earlier.
	 *
	 * @see #registerWildcardListener(Class)
	 */
	protected final void unregisterWildcardListener() {
		unregisterListener(EventType.WILDCARD, EventSource.WILDCARD);
	}

	/**
	 * Unregisters all {@code Rule} objects that have been registered by this {@code Module} instance with the parent
	 * {@code EventHub} object.
	 */
	protected final void unregisterAllRules() {
		try {
			parentHub.unregisterModuleRules(this);
		} catch (final InvalidModuleException e) {
			Log.debug(moduleName, "Failed ot unregister rules for module (%s)", e);
		}
	}

	/**
	 * Creates a shared state for this module, then sends an event ( the event and the state will have the same event number )
	 * @param sharedState {@code EventData} object containing the state to save
	 * @param event the event to be dispatched to {@code EventHub}
	 */
	protected final void createSharedStateAndDispatchEvent(final EventData sharedState, final Event event) {
		if (sharedState == null || event == null) {
			Log.debug(moduleName, "failed to create the shared state and dispatch the event ( null sharedState or null event)");
			return;
		}

		try {
			parentHub.createSharedStateAndDispatchEvent(this, sharedState, event);
		} catch (InvalidModuleException e) {
			Log.error(moduleName, "Unable to create shared state (%s)", e);
		}
	}

	/**
	 * Creates a shared state for this module versioned at the current event count of the registering hub
	 *
	 * @param state   {@code EventData} object containing the state to save (can be {@code null})
	 * @param version {@code int} containing the version that this shared state should be begin to be valid for
	 **/
	protected final void createSharedState(final int version, final EventData state) {
		try {
			parentHub.createSharedState(this, version, state);
		} catch (final InvalidModuleException e) {
			Log.error(moduleName, "Unable to create shared state (%s)", e);
		}
	}

	/**
	 * Updates an existing shared state for this module
	 *
	 * @param version  version of the existing shared state to replace
	 * @param newState new state to replace existing state with
	 **/
	protected final void updateSharedState(final int version, final EventData newState) {
		try {
			parentHub.updateSharedState(this, version, newState);
		} catch (final InvalidModuleException e) {
			Log.error(moduleName, "Unable to update shared state (%s)", e);
		}
	}


	/**
	 * Creates or updates a shared state for this module versioned at the current event count of the registering hub.
	 *
	 * @param state   {@link EventData} object containing the state to save (can be null)
	 * @param version version this shared state will be valid for
	 **/
	protected final void createOrUpdateSharedState(final int version, final EventData state) {
		try {
			parentHub.createOrUpdateSharedState(this, version, state);
		} catch (final InvalidModuleException e) {
			Log.error(moduleName, "Unable to create or update shared state with version (%s)", e);
		}
	}

	/**
	 * Creates a shared state for this module versioned at the next event count of the registering hub
	 * <p>
	 *  NOTE: Do not call this method if the {@code module} also calls {@link #createSharedState(int, EventData)}
	 *  or {@link #createOrUpdateSharedState(int, EventData)}. It may lead to data loss when attempting
	 *  to create a shared state.
	 *
	 * @param state   {@link EventData} object containing the state to save (can be null)
	 **/
	protected final void createOrUpdateSharedState(final EventData state) {
		try {
			parentHub.createOrUpdateSharedState(this, state);
		} catch (final InvalidModuleException e) {
			Log.error(moduleName, "Unable to create or update shared state (%s)", e);
		}
	}

	/**
	 * Retrieves the named shared state that is valid for the given event
	 *
	 * @param stateName name of the shared state to retrieve
	 * @param event     event to retrieve named state for
	 *
	 * @return EventData object containing the valid state
	 **/
	protected final EventData getSharedEventState(final String stateName, final Event event) {
		try {
			return parentHub.getSharedEventState(stateName, event, this);
		} catch (final IllegalArgumentException e) {
			Log.error(moduleName, "Unable to retrieve shared event state (%s)", e);
		}

		return null;
	}

	/**
	 * Determines if the named module contains any valid shared states.
	 *
	 * @param stateName name of the module to query for valid states.
	 *
	 * @return true if the named module contains any valid shared states
	 **/
	protected final boolean hasSharedEventState(final String stateName) {
		try {
			return parentHub.hasSharedEventState(stateName);
		} catch (final IllegalArgumentException e) {
			Log.error(moduleName, "Unable to query shared event state (%s)", e);
		}

		return false;
	}

	/**
	 * Clear all the shared states that exist for this module
	 *
	 * @return {@code boolean} indicating if the shared states were successfully cleared
	 */
	protected final boolean clearSharedStates() {
		try {
			parentHub.clearSharedStates(this);
			return true;
		} catch (final InvalidModuleException e) {
			Log.error(moduleName, "Unable to clear the shared event states (%s)", e);
		}

		return false;
	}

	/**
	 * Creates an XDM shared state for this module, then sends an event ( the event and the state will have the same event number )
	 * The state passed to this API needs to be mapped to known XDM mixins; if an extension uses multiple mixins, the current data for all of them should be provided when the XDM shared state is set.
	 *
	 * @param sharedState {@code EventData} object containing the state to save
	 * @param event the event to be dispatched to {@code EventHub}
	 */
	protected final void createXDMSharedStateAndDispatchEvent(final EventData sharedState, final Event event) {
		if (sharedState == null || event == null) {
			Log.debug(moduleName, "failed to create XDM shared state and dispatch the event ( null sharedState or null event)");
			return;
		}

		try {
			parentHub.createSharedStateAndDispatchEvent(this, sharedState, event, SharedStateType.XDM);
		} catch (InvalidModuleException e) {
			Log.error(moduleName, "Unable to create XDM shared state (%s)", e);
		}
	}

	/**
	 * Creates an XDM shared state for this module versioned at the current event count of the registering hub
	 * The state passed to this API needs to be mapped to known XDM mixins; if an extension uses multiple mixins, the current data for all of them should be provided when the XDM shared state is set.
	 *
	 * @param state   {@code EventData} object containing the state to save (can be {@code null})
	 * @param version {@code int} containing the version that this shared state should be begin to be valid for
	 **/
	protected final void createXDMSharedState(final int version, final EventData state) {
		try {
			parentHub.createSharedState(this, version, state, SharedStateType.XDM);
		} catch (final InvalidModuleException e) {
			Log.error(moduleName, "Unable to create XDM shared state (%s)", e);
		}
	}

	/**
	 * Updates an existing XDM shared state for this module
	 *
	 * @param version  version of the existing shared state to replace
	 * @param newState new state to replace existing state with
	 **/
	protected final void updateXDMSharedState(final int version, final EventData newState) {
		try {
			parentHub.updateSharedState(this, version, newState, SharedStateType.XDM);
		} catch (final InvalidModuleException e) {
			Log.error(moduleName, "Unable to update XDM shared state (%s)", e);
		}
	}

	/**
	 * Creates or updates an XDM shared state for this module versioned at the current event count of the registering hub.
	 *
	 * @param state   {@link EventData} object containing the state to save (can be null)
	 * @param version version this shared state will be valid for
	 **/
	protected final void createOrUpdateXDMSharedState(final int version, final EventData state) {
		try {
			parentHub.createOrUpdateSharedState(this, version, state, SharedStateType.XDM);
		} catch (final InvalidModuleException e) {
			Log.error(moduleName, "Unable to create or update XDM shared state with version (%s)", e);
		}
	}

	/**
	 * Creates a XDM shared state for this module versioned at the next event count of the registering hub
	 * <p>
	 *  NOTE: Do not call this method if the {@code module} also calls {@link #createXDMSharedState(int, EventData)}
	 *  or {@link #createOrUpdateXDMSharedState(int, EventData)}. It may lead to data loss when attempting
	 *  to create an XDM shared state.
	 *
	 * @param state   {@link EventData} object containing the XDM state to save (can be null)
	 **/
	protected final void createOrUpdateXDMSharedState(final EventData state) {
		try {
			parentHub.createOrUpdateSharedState(this, state, SharedStateType.XDM);
		} catch (final InvalidModuleException e) {
			Log.error(moduleName, "Unable to create or update XDM shared state (%s)", e);
		}
	}

	/**
	 * Retrieves the named XDM shared state that is valid for the given event
	 * If the {@code stateName} extension populates multiple mixins in their shared state, all the data will be returned at once and it can be accessed using path discovery.
	 *
	 * @param stateName name of the shared state to retrieve
	 * @param event     event to retrieve named state for
	 *
	 * @return EventData object containing the valid state
	 **/
	protected final EventData getXDMSharedEventState(final String stateName, final Event event) {
		try {
			return parentHub.getSharedEventState(stateName, event, this, SharedStateType.XDM);
		} catch (final IllegalArgumentException e) {
			Log.error(moduleName, "Unable to retrieve XDM shared event state (%s)", e);
		}

		return null;
	}

	/**
	 * Determines if the named module contains any valid XDM shared states.
	 *
	 * @param stateName name of the module to query for valid states.
	 *
	 * @return true if the named module contains any valid XDM shared states
	 **/
	protected final boolean hasXDMSharedEventState(final String stateName) {
		try {
			return parentHub.hasSharedEventState(stateName, SharedStateType.XDM);
		} catch (final IllegalArgumentException e) {
			Log.error(moduleName, "Unable to query XDM shared event state (%s)", e);
		}

		return false;
	}

	/**
	 * Clear all the XDM shared states that exist for this module
	 *
	 * @return {@code boolean} indicating if the shared states were successfully cleared
	 */
	protected final boolean clearXDMSharedStates() {
		try {
			parentHub.clearSharedStates(this, SharedStateType.XDM);
			return true;
		} catch (final InvalidModuleException e) {
			Log.error(moduleName, "Unable to clear the XDM shared event states (%s)", e);
		}

		return false;
	}

	/**
	 * Name to store shared state for this module under
	 *
	 * @return String containing the container name for shared state for this module can be {@code null}
	 **/
	public String getModuleName() {
		return moduleName;
	}

	/**
	 * Version for this module. Only used for logging.
	 *
	 * @return {@link String} containing the version number of this module, can be {@code null}
	 **/
	public String getModuleVersion() {
		return moduleVersion;
	}

	/**
	 * Set the moduleName. This should only be called by (@code ExtensionApi} or test code.
	 * @param newModuleName The name to use for this module.
	 **/
	void setModuleName(final String newModuleName) {
		moduleName = newModuleName;
	}

	/**
	 * Set the moduleVersion. This should only be called by (@code ExtensionApi}.
	 * @param newModuleVersion The name to use for this module.
	 **/
	void setModuleVersion(final String newModuleVersion) {
		moduleVersion = newModuleVersion;
	}

	/**
	 * Get this module's executor.
	 * All long running tasks specific to this module should be run on this executor.
	 *
	 * @return module {@link ExecutorService}
	 */
	protected final ExecutorService getExecutor() {
		synchronized (executorMutex) {
			if (executor == null) {
				executor = Executors.newSingleThreadExecutor();
			}

			return executor;
		}
	}

	void setModuleDetails(final ModuleDetails newDetails) {
		moduleDetails = newDetails;
	}

	ModuleDetails getModuleDetails() {
		return moduleDetails;
	}
}
