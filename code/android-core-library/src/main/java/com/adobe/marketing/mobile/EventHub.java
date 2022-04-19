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

import com.adobe.marketing.mobile.internal.eventhub.EventHubError;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * defines a core event loop for sdk activity
 *
 * @author Adobe Systems Incorporated
 * @version 5.0
 */
class EventHub {
	// shared state markers
	/**
	 * State that is "on the way" and will eventually be resolved.
	 */
	public static final EventData SHARED_STATE_PENDING = null;
	/**
	 * Special state that indicates that the state is not valid.
	 */
	public static final EventData SHARED_STATE_INVALID = new EventData();
	/**
	 * Special "marker" state that indicates that this state is equal to the next DATA/PENDING/INVALID state.
	 */
	public static final EventData SHARED_STATE_NEXT = new EventData();
	/**
	 * Special "marker" state that indicates that this state is equal to the previous state.
	 */
	public static final EventData SHARED_STATE_PREV = new EventData();

	private static final String STANDARD_STATE_CHANGE_EVENTNAME = "Shared state change";
	private static final String XDM_STATE_CHANGE_EVENTNAME = "Shared state change (XDM)";
	private static final String LOG_PROVIDED_MODULE_WAS_NULL = "Provided module was null";
	private static final String LOG_MODULE_WAS_NULL = "Module was null";
	private static final String LOG_CLASS_WAS_NULL = "Extension class was null";
	private static final String LOG_STATENAME_WAS_NULL = "StateName was null";
	private static final String LOG_MODULE_NOT_REGISTERED = "Module (%s) is not registered";
	private static final long DEFAULT_THREAD_POOL_KEEP_ALIVE_TIME_SECONDS = 60L;
	private static final int ONE_TIME_LISTENER_TIMEOUT_DEFAULT_MILLISECONDS = 5000;
	public static final int REPROCESS_EVENTS_AMOUNT_LIMIT = 100;
	private static final String SDK_VERSION_DELIMITER = "-";

	// instance variables
	private final String logPrefix;
	private final PlatformServices services;
	private final ConcurrentHashMap<String, Module> activeModules;
	private final ConcurrentHashMap<Module, ConcurrentLinkedQueue<EventListener>> moduleListeners;
	private final ConcurrentHashMap<String, RangedResolver<EventData>> moduleSharedStates;
	private final ConcurrentHashMap<String, RangedResolver<EventData>> moduleXdmSharedStates;
	private final ConcurrentHashMap<String, Boolean> sharedStateCircularCheck;
	private final LinkedList<Event> preBootEvents; // locked on bootMutex
	private final RulesEngine rulesEngine;
	private final AtomicInteger currentEventNumber;
	private final ExecutorService threadPool;
	private final ExecutorService eventHubThreadService;
	protected final EventData eventHubSharedState;
	protected final String coreVersion;
	private WrapperType wrapperType = WrapperType.NONE;

	private ScheduledExecutorService scheduledThreadPool;
	private final Object scheduledThreadPoolMutex = new Object();
	protected boolean isBooted; // locked on bootMutex
	private final Object bootMutex = new Object();

	private final EventBus eventBus;


	/**
	 * Returns an instance of the Event Hub
	 *
	 * @param name     the name of the {@code EventHub} to be created - for logging purposes
	 * @param services instance of {@code PlatformServices} class to provide platform-specific functionality
	 * @throws IllegalArgumentException If platform services is null
	 */
	public EventHub(final String name, final PlatformServices services) {
		this(name, services, "undefined");
	}

	/**
	 * Returns an instance of the Event Hub
	 *
	 * @param name        the name of the {@code EventHub} to be created - for logging purposes
	 * @param services    instance of {@code PlatformServices} class to provide platform-specific functionality
	 * @param coreVersion value passed from platform to indicate the running version of core
	 * @throws IllegalArgumentException If platform services is null
	 */
	public EventHub(final String name, final PlatformServices services, final String coreVersion) {
		logPrefix = String.format("%s(%s)", this.getClass().getSimpleName(), name);

		if (services == null) {
			throw new IllegalArgumentException("Cannot construct EventHub without a valid platform services instance");
		}

		this.coreVersion = coreVersion;
		this.services = services;
		this.activeModules = new ConcurrentHashMap<String, Module>();
		this.moduleListeners = new ConcurrentHashMap<Module, ConcurrentLinkedQueue<EventListener>>();
		this.moduleSharedStates = new ConcurrentHashMap<String, RangedResolver<EventData>>();
		this.moduleXdmSharedStates = new ConcurrentHashMap<String, RangedResolver<EventData>>();
		this.currentEventNumber = new AtomicInteger(1);
		this.preBootEvents = new LinkedList<Event>();
		this.sharedStateCircularCheck = new ConcurrentHashMap<String, Boolean>();
		this.threadPool = Executors.newCachedThreadPool();
		this.eventHubThreadService = new ThreadPoolExecutor(0, 1,
				DEFAULT_THREAD_POOL_KEEP_ALIVE_TIME_SECONDS, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>());
		this.eventHubSharedState = getInitialEventHubSharedState();
		this.isBooted = false;
		this.rulesEngine = new RulesEngine(this);
		this.eventBus = new EventBus();
	}

	/**
	 * Let event hub know that all the modules have been registered, so it can dispatch a booted event as the first event.
	 *
	 * @param completionCallback An optional {@link AdobeCallback} invoked after registrations are completed
	 */
	void finishModulesRegistration(final AdobeCallback<Void> completionCallback) {
		this.eventHubThreadService.submit(new Runnable() {
			@Override
			public void run() {
				synchronized (bootMutex) {
					if (isBooted) {
						Log.trace(logPrefix, "Eventhub has already been booted");
						return;
					}

					Event bootedEvent = new Event.Builder("EventHub", EventType.HUB, EventSource.BOOTED).build();
					bootedEvent.setEventNumber(0);
					eventHubThreadService.submit(new EventRunnable(bootedEvent));

					isBooted = true;

					// generate eventhub shared state prior to releasing "pre-boot" events.
					createEventHubSharedState(0);

					while (preBootEvents.peek() != null) {
						eventHubThreadService.submit(new EventRunnable(preBootEvents.poll()));
					}

					if (completionCallback != null) {
						eventHubThreadService.submit(new Runnable() {
							@Override
							public void run() {
								completionCallback.call(null);
							}
						});
					}
				}
			}
		});
	}

	/**
	 * Dispatches an event onto the Event queue
	 *
	 * @param e the event to be added to the queue
	 */
	void dispatch(final Event e) {
		synchronized (bootMutex) {
			e.setEventNumber(this.currentEventNumber.getAndIncrement());

			if (!this.isBooted) {
				Log.debug(logPrefix, "Event (%s, %s) was dispatched before module registration was finished",
						  e.getEventType().getName(), e.getEventSource().getName());
				preBootEvents.add(e);
			} else {
				this.eventHubThreadService.submit(new EventRunnable(e));
			}

			final EventHistory eventHistory = EventHistoryProvider.getEventHistory();

			// record the event in the event history  database if the event has a mask
			if (eventHistory != null && e.getMask() != null) {
				final EventHistoryResultHandler<Boolean> handler = new EventHistoryResultHandler<Boolean>() {
					@Override
					public void call(final Boolean value) {
						Log.trace(logPrefix, value ? "Successfully inserted an Event into EventHistory database" :
								  "Failed to insert an Event into EventHistory database");
					}
				};
				eventHistory.recordEvent(e, handler);
			}
		}
	}

	/**
	 * Creates a shared state for this module, then sends an event ( the event and the state will have the same event number )
	 *
	 * @param module      module instance to create the shared state
	 * @param sharedState {@code EventData} object containing the state to save
	 * @param event       the event to be dispatched to {@code EventHub}
	 */
	void createSharedStateAndDispatchEvent(final Module module, final EventData sharedState,
										   final Event event) throws InvalidModuleException {
		createSharedStateAndDispatchEvent(module, sharedState, event, SharedStateType.STANDARD);
	}

	/**
	 * Creates a shared state for this module, then sends an event ( the event and the state will have the same event number )
	 *
	 * @param module      module instance to create the shared state
	 * @param sharedState {@code EventData} object containing the state to save
	 * @param event       the event to be dispatched to {@code EventHub}
	 * @param sharedStateType the type of shared state to be created
	 */
	void createSharedStateAndDispatchEvent(final Module module, final EventData sharedState, final Event event,
										   final SharedStateType sharedStateType) throws InvalidModuleException {
		event.setEventNumber(this.currentEventNumber.getAndIncrement());
		createSharedState(module, event.getEventNumber(), sharedState, sharedStateType);
		this.eventHubThreadService.submit(new EventRunnable(event));
	}

	/**
	 * Checks if a module with the provided name is already registered. The comparison is not case-sensitive,
	 * the names will be lower cased before the compare.
	 * <p>
	 * This method iterates through the modules list without a mutex, and is supposed to be called from a thread safe method
	 *
	 * @param moduleName the {@link String} module name to search for
	 * @return the status of the search
	 */
	private boolean isRegisteredModule(final String moduleName) {
		if (moduleName == null) {
			return false;
		}

		return activeModules.containsKey(normalizeName(moduleName));
	}

	/**
	 * Returns the lower cased name
	 *
	 * @param name {@link String} name to be normalized
	 * @return the lower case name
	 */
	private String normalizeName(final String name) {
		return name != null ? name.toLowerCase() : null;
	}

	/**
	 * Interface for receiving callbacks when a module is registered
	 */
	protected interface RegisterModuleCallback {
		void registered(Module module);
	}

	/**
	 * For testing
	 *
	 * @return all loaded rules
	 */
	protected ConcurrentHashMap<Module, ConcurrentLinkedQueue<Rule>> getModuleRuleAssociation() {
		return this.rulesEngine.getModuleRuleAssociation();
	}

	/**
	 * Registers a module with the event hub. Modules must extend {@code Module}
	 *
	 * @param moduleClass a class that extends {@link Module}
	 * @param callback    class implementing {@code RegisterModuleCallback} called if module successfully registers
	 * @throws InvalidModuleException will be thrown if the {@code moduleClass} is null
	 */
	protected void registerModuleWithCallback(final Class<? extends Module> moduleClass,
			final RegisterModuleCallback callback) throws InvalidModuleException {
		registerModuleWithCallback(moduleClass, null, callback);
	}


	protected void registerModuleWithCallback(final Class<? extends Module> moduleClass,
			final ModuleDetails moduleDetails,
			final RegisterModuleCallback callback) throws InvalidModuleException {
		if (moduleClass == null) {
			throw new InvalidModuleException(LOG_CLASS_WAS_NULL);
		}

		// register the module on the event hub thread
		final EventHub hub = this;
		Future<?> f = this.eventHubThreadService.submit(new Runnable() {
			@Override
			public void run() {
				try {
					Module module;

					// check, not to register modules have same class name
					for (Module activeModule : hub.getActiveModules()) {
						if (activeModule.getClass().getName().equalsIgnoreCase(moduleClass.getName())) {
							Log.warning(logPrefix, "Failed to register extension, an extension with the same name (%s) already exists",
										activeModule.getModuleName());
							return;
						}
					}

					if (InternalModule.class.isAssignableFrom(moduleClass)) {
						Constructor<? extends Module> moduleConstructor = moduleClass.getDeclaredConstructor(EventHub.class,
								PlatformServices.class);
						moduleConstructor.setAccessible(true);
						module = moduleConstructor.newInstance(hub, services);
					} else {
						Constructor<? extends Module> moduleConstructor = moduleClass.getDeclaredConstructor(EventHub.class);
						moduleConstructor.setAccessible(true);
						module = moduleConstructor.newInstance(hub);
					}

					// check, not to register modules have same "module name"
					if (isRegisteredModule(module.getModuleName())) {
						Log.warning(logPrefix, "Failed to register extension, an extension with the same name (%s) already exists",
									module.getModuleName());
						return;
					}

					// set the module details that got passed in
					module.setModuleDetails(moduleDetails);
					addModuleToEventHubSharedState(module);

					activeModules.put(normalizeName(module.getModuleName()), module);
					moduleListeners.put(module, new ConcurrentLinkedQueue<EventListener>());

					if (callback != null) {
						callback.registered(module);
					}

				} catch (Exception e) {
					Log.error(logPrefix, "Unable to create instance of provided extension %s: %s", moduleClass.getSimpleName(), e);
				}
			}
		});
	}

	/**
	 * Registers a rule to a module
	 *
	 * @param module module instance to register rule for
	 * @param rule   {@code Rule} to register
	 * @throws InvalidModuleException if module is null
	 */
	final void registerModuleRule(final Module module, final Rule rule) throws InvalidModuleException {
		if (module == null) {
			throw new InvalidModuleException(LOG_PROVIDED_MODULE_WAS_NULL);
		}

		if (rule == null) {
			throw new IllegalArgumentException("Cannot register a null rule");
		}


		rulesEngine.addRule(module, rule);
	}

	final void replaceModuleRules(final Module module, final List<Rule> rules) throws InvalidModuleException {
		if (module == null) {
			throw new InvalidModuleException(LOG_PROVIDED_MODULE_WAS_NULL);
		}

		if (rules == null) {
			throw new IllegalArgumentException("Cannot register a null rule");
		}


		rulesEngine.replaceRules(module, rules);
	}


	/**
	 * Evaluates given events with supplied rules, then registers rules for this module
	 *
	 * @param module                 module instance to register rules for
	 * @param rules                  {@code Rule} to register
	 * @param reprocessEventsHandler handler to return custom events
	 */
	protected void replaceRulesAndEvaluateEvents(final Module module, final List<Rule> rules,
			final ReprocessEventsHandler reprocessEventsHandler) {

		if (reprocessEventsHandler == null) {
			Log.debug(logPrefix, "failed to reprocess events as <reprocessEventsHandler> is null ");
			return;
		}

		if (rules == null) {
			Log.debug(logPrefix, "failed to reprocess events as <rules> is null ");
			return;
		}

		this.eventHubThreadService.submit(new ReprocessEventsWithRules(reprocessEventsHandler, rules, module));
	}

	/**
	 * Unregisters all rules registered by the given {@code Module}
	 *
	 * @param module module instance to unregister rules for
	 * @throws InvalidModuleException if module is null
	 */
	final void unregisterModuleRules(final Module module) throws InvalidModuleException {
		if (module == null) {
			throw new InvalidModuleException(LOG_PROVIDED_MODULE_WAS_NULL);
		}

		rulesEngine.unregisterAllRules(module);
	}


	/**
	 * Registers a module with the event hub. Modules must extend {@code Module}
	 *
	 * @param moduleClass a class that extends {@link Module}
	 * @throws InvalidModuleException will be thrown if the {@code moduleClass} is null
	 */
	final void registerModule(final Class<? extends Module> moduleClass) throws InvalidModuleException {
		registerModuleWithCallback(moduleClass, null);
	}

	final void registerModule(final Class<? extends Module> moduleClass,
							  final ModuleDetails moduleDetails) throws InvalidModuleException {
		registerModuleWithCallback(moduleClass, moduleDetails, null);
	}

	/**
	 * Registers an extension with the event hub. Extensions must extend {@code Extension}
	 * <p>
	 * When the registration is completed, the extension class will be initialized with the {@link ExtensionApi}
	 *
	 * @param extensionClass a class that extends {@link Extension}
	 */
	void registerExtensionWithCallback(final Class<? extends Extension> extensionClass, final ExtensionErrorCallback<ExtensionError> errorCallback) {
		com.adobe.marketing.mobile.internal.eventhub.EventHub.Companion.getShared().registerExtension(extensionClass, new Function1<EventHubError, Unit>() {
			@Override
			public Unit invoke(EventHubError e) {
				if (errorCallback == null || EventHubError.None.equals(e)) {
					return null;
				}

				if (EventHubError.InvalidExtensionName.equals(e)) {
					errorCallback.error(ExtensionError.BAD_NAME);
				} else if (EventHubError.DuplicateExtensionName.equals(e)) {
					errorCallback.error(ExtensionError.DUPLICATE_NAME);
				} else {
					errorCallback.error(ExtensionError.UNEXPECTED_ERROR);
				}
				return null;
			}
		});
	}


	/**
	 * Unregisters a Module
	 * <p>
	 * This will remove all listeners, and will drop all references to the Module OneTime blocks that have been
	 * registered by the module will continue to exist until they execute
	 * If the {@code module} is a 3rd party extension, {@link ExtensionApi#onUnregistered()} will be called when the
	 * extension is unregistered
	 *
	 * @param module the instance of a Module class to unregister from the event loop
	 * @throws InvalidModuleException will be thrown if the module is null
	 */
	final void unregisterModule(final Module module) throws InvalidModuleException {
		if (module == null) {
			throw new InvalidModuleException(LOG_MODULE_WAS_NULL);
		}

		// unregister the module on the event hub thread
		this.eventHubThreadService.submit(new Runnable() {
			@Override
			public void run() {

				if (!isRegisteredModule(module.getModuleName())) {
					Log.error(logPrefix, "Failed to unregister module, " + LOG_MODULE_NOT_REGISTERED, module.getModuleName());
					return;
				}

				final Collection<EventListener> thisModulesListeners = moduleListeners.remove(module);

				if (thisModulesListeners != null) {
					for (EventListener listener : thisModulesListeners) {
						eventBus.removeListener(listener);
					}
				}

				activeModules.remove(normalizeName(module.getModuleName()));

				try {
					module.onUnregistered();
				} catch (Exception e) {
					Log.error(logPrefix, "%s.onUnregistered() threw %s", module.getClass().getSimpleName(), e);
				}
			}
		});

		removeModuleFromEventHubSharedState(module);
	}

	/**
	 * Registers an event listener for a module
	 *
	 * @param module        module instance to register listener with
	 * @param type          {@code EventType} to listen for
	 * @param source        {@code EventSource} to listen for
	 * @param pairID        pairID for one-time event.  May be {@code null}
	 * @param listenerClass class definition that extends {@code ModuleEventListener<T>}
	 * @param <T>           type of the listener class
	 * @throws InvalidModuleException if module is null
	 */
	final <T extends ModuleEventListener<?>>
	void registerModuleListener(final Module module,
								final EventType type,
								final EventSource source,
								final String pairID,
								final Class<T> listenerClass) throws InvalidModuleException {

		if (module == null) {
			throw new InvalidModuleException(LOG_MODULE_WAS_NULL);
		}

		if (listenerClass == null || type == null || source == null) {
			Log.debug(logPrefix, "%s (listenerClass, type or source), failed to register listener", Log.UNEXPECTED_NULL_VALUE);
			return;
		}

		// register the module listener on the event hub thread
		this.eventHubThreadService.submit(new Runnable() {
			@Override
			public void run() {

				// make sure module is registered
				if (!isRegisteredModule(module.getModuleName())) {
					Log.error(logPrefix, "Failed to register listener, " + LOG_MODULE_NOT_REGISTERED, module.getModuleName());
					return;
				}

				// unregister listener if already registered
				unregisterListenerIfPresent(module, type, source);

				// first try to find a constructor using the instance class (normal case)
				Class expectedModuleClass = module.getClass();
				Constructor<T> listenerConstructor = null;

				boolean isExtensionListener;

				try {
					// try the extension listener case first
					listenerConstructor = listenerClass.getDeclaredConstructor(expectedModuleClass,
										  String.class, String.class);
					isExtensionListener = true;
				} catch (NoSuchMethodException error) {
					// not an extension listener, try to register an internal listener instead
					isExtensionListener = false;
				}

				if (!isExtensionListener) {
					try {
						// try the regular listener constructor
						listenerConstructor = listenerClass.getDeclaredConstructor(expectedModuleClass,
											  EventType.class,
											  EventSource.class);
					} catch (NoSuchMethodException firstTryError) {

						// next try using the instances classes parent class (mocking case)
						expectedModuleClass = expectedModuleClass.getSuperclass();

						try {
							listenerConstructor = listenerClass.getDeclaredConstructor(expectedModuleClass,
												  EventType.class,
												  EventSource.class);

						} catch (NoSuchMethodException secondTryError) {
							Log.error(logPrefix, "Failed to find a constructor for class %s (%s)",
									  listenerClass.getSimpleName(), secondTryError);

							// if this is an extension, call the error callback
							if (ExtensionApi.class.isAssignableFrom(module.getClass())) {
								//ExtensionApi ext = (ExtensionApi) module;
								//ext.getExtension().onUnexpectedError(new ExtensionUnexpectedError("Failed to register listener",
								//									 ExtensionError.UNEXPECTED_ERROR));
							}
						}
					}
				}

				if (listenerConstructor != null) {
					try {
						// construct the listener
						listenerConstructor.setAccessible(true);
						T listener;

						if (isExtensionListener) {
							listener = listenerConstructor.newInstance(module, type.getName(), source.getName());
						} else {
							listener = listenerConstructor.newInstance(module, type, source);
						}

						// This allows EventHubTest tests to work without registering a module
						moduleListeners.putIfAbsent(module, new ConcurrentLinkedQueue<EventListener>());
						ConcurrentLinkedQueue<EventListener> moduleSpecificListeners = moduleListeners.get(module);

						// add the listener to the modules queue of listeners
						moduleSpecificListeners.add(listener);

						// add the listener to the event hub map of listeners
						eventBus.addListener(listener, type, source, pairID);

					} catch (Exception e) {
						Log.error(logPrefix, "Failed to register listener for class %s (%s)",
								  listenerClass.getSimpleName(), e);

//						// if this is an extension, call the error callback
//						if (ExtensionApi.class.isAssignableFrom(module.getClass())) {
//							ExtensionApi ext = (ExtensionApi) module;
//							ext.getExtension().onUnexpectedError(new ExtensionUnexpectedError("Failed to register listener", e,
//																 ExtensionError.UNEXPECTED_ERROR));
//						}
					}
				}
			}
		});

	}

	/**
	 * Registers a one-time block with the event hub
	 * a one-time block is an event handler that will cease to function after it successfully 'hears' one event
	 *
	 * @param type   the {@code EventType} of an event to listen for
	 * @param source the {@code EventSource} of an event to listen for
	 * @param pairID optional pairID to listen for -- this is primarily used for request/response events
	 * @param block  the block to call when the event is heard
	 * @see #registerOneTimeListener(String, Module.OneTimeListenerBlock)
	 * @deprecated this method is deprecated, please use {@link #registerOneTimeListener(String, Module.OneTimeListenerBlock)} instead
	 */
	@Deprecated
	void registerOneTimeListener(final EventType type,
								 final EventSource source,
								 final String pairID,
								 final Module.OneTimeListenerBlock block) {
		registerOneTimeListener(pairID, block);
	}

	void registerOneTimeListener(final String pairID,
								 final Module.OneTimeListenerBlock block) {
		registerOneTimeListener(pairID, block, null, 0);
	}

	/**
	 * Registers a one-time block with the {@code EventHub}. An one-time block is an {@code Event} handler that will
	 * cease to function after it successfully 'hears' one event. The one-time listener will also be unregistered by
	 * the event hub after the event is received. Use the default timeout value of 5000ms.
	 *
	 * @param pairID                 optional pairID to listen for -- this is primarily used for request/response events
	 * @param block                  the block to call when the event is heard
	 * @param adobeCallbackWithError the block to call when there is error or timeout
	 */
	void registerOneTimeListener(final String pairID,
								 final Module.OneTimeListenerBlock block,
								 final AdobeCallbackWithError adobeCallbackWithError) {
		registerOneTimeListener(pairID, block, adobeCallbackWithError, ONE_TIME_LISTENER_TIMEOUT_DEFAULT_MILLISECONDS);

	}

	/**
	 * Registers an event listener for the provided event type and source.
	 *
	 * @param eventType the type of the listened event
	 * @param eventSource the source of the listened event
	 * @param callback {@link AdobeCallbackWithError#call(Object)} will be called when the listened event is heard
	 */
	void registerEventListener(final EventType eventType, final EventSource eventSource,
							   final AdobeCallbackWithError<Event> callback) {
		if (callback == null) {
			Log.debug(logPrefix, "%s (callback), failed to register the event listener", Log.UNEXPECTED_NULL_VALUE);
			return;
		}

		this.eventHubThreadService.submit(new Runnable() {
			@Override
			public void run() {
				try {
					eventBus.addListener(new EventListener() {
						@Override
						public void hear(final Event e) {
							callback.call(e);
						}

						@Override
						public void onUnregistered() {}

						@Override
						public EventSource getEventSource() {
							return eventSource;
						}

						@Override
						public EventType getEventType() {
							return eventType;
						}
					}, eventType, eventSource, null);

				} catch (Exception e) {
					Log.error(logPrefix, "Failed to register the event listener - (%s)", e);
				}
			}
		});
	}
	/**
	 * Registers a one-time block with the {@code EventHub}. An one-time block is an {@code Event} handler that will
	 * cease to function after it successfully 'hears' one event. The one-time listener will also be unregistered by
	 * the event hub after the event is received.
	 *
	 * @param pairID                 optional pairID to listen for -- this is primarily used for request/response events
	 * @param block                  the block to call when the event is heard
	 * @param adobeCallbackWithError the block to call when there is error or timeout
	 * @param timeoutInMilliSec      the timeout value in milliseconds
	 */
	void registerOneTimeListener(final String pairID,
								 final Module.OneTimeListenerBlock block,
								 final AdobeCallbackWithError adobeCallbackWithError,
								 final int timeoutInMilliSec) {

		if (block == null) {
			Log.debug(logPrefix, "%s (callback block), failed to register one-time listener", Log.UNEXPECTED_NULL_VALUE);

			if (adobeCallbackWithError != null) {
				adobeCallbackWithError.fail(AdobeError.CALLBACK_NULL);
			}

			return;
		}

		final OneTimeListener oneTimeListener = new OneTimeListener(block);
		// register the one-time listener on the event hub thread
		this.eventHubThreadService.submit(new Runnable() {
			@Override
			public void run() {

				try {
					eventBus.addListener(oneTimeListener, null, null, pairID);

				} catch (Exception e) {
					Log.error(logPrefix, "Failed to register one-time listener", e);
				}
			}
		});

		if (timeoutInMilliSec > 0 && adobeCallbackWithError != null) {
			ScheduledExecutorService scheduledThreadPool = getScheduledExecutorService();
			scheduledThreadPool.schedule(new Runnable() {
				@Override
				public void run() {
					if (oneTimeListener.isCalled()) {
						return;
					}

					oneTimeListener.cancel();
					eventHubThreadService.submit(new Runnable() {
						@Override
						public void run() {
							eventBus.removeListener(oneTimeListener, null, null, pairID);
						}
					});
					adobeCallbackWithError.fail(AdobeError.CALLBACK_TIMEOUT);

				}
			}, timeoutInMilliSec, TimeUnit.MILLISECONDS);
		}


	}

	private ScheduledExecutorService getScheduledExecutorService() {
		if (scheduledThreadPool == null) {
			synchronized (scheduledThreadPoolMutex) {
				if (scheduledThreadPool == null) {
					scheduledThreadPool = Executors.newSingleThreadScheduledExecutor();
				}
			}
		}

		return scheduledThreadPool;
	}

	/**
	 * Private method that unregisters a specific listener. Assumes that it is running on the event hub thread.
	 *
	 * @param module module instance to unregister listener with
	 * @param type   the {@code EventType} of an event to listen for
	 * @param source the {@code EventSource} of an event to listen for
	 * @return {@code boolean} true if a listener was found and removed, false otherwise
	 */
	private boolean unregisterListenerIfPresent(final Module module, final EventType type, final EventSource source) {

		// check if listeners are registered for this module
		boolean listenerExist = false;

		final ConcurrentLinkedQueue<EventListener> moduleSpecificListeners = moduleListeners.get(module);

		if (moduleSpecificListeners == null || moduleSpecificListeners.isEmpty()) {
			return false;
		}

		for (EventListener listener : moduleSpecificListeners) {
			if (listener.getEventSource().equals(source) && listener.getEventType().equals(type)) {
				listenerExist = true;
				moduleSpecificListeners.remove(listener);
				this.eventBus.removeListener(listener);
			}
		}

		return listenerExist;
	}

	/**
	 * Unregisters all listeners that match a type/source pair from the {@code Module} and the
	 * {@code EventHub}
	 *
	 * @param module module instance to unregister listener for
	 * @param type   the {@code EventType} of an event to listen for
	 * @param source the {@code EventSource} of an event to listen for
	 * @throws InvalidModuleException if module is null or not registered with this event hub
	 */
	final void unregisterModuleListener(final Module module, final EventType type,
										final EventSource source) throws InvalidModuleException {
		if (module == null) {
			throw new InvalidModuleException(LOG_MODULE_WAS_NULL);
		}

		// un-register the module listener on the event hub thread
		this.eventHubThreadService.submit(new Runnable() {
			@Override
			public void run() {
				if (!unregisterListenerIfPresent(module, type, source)) {
					Log.debug(logPrefix, "Failed to unregister listener (no registered listener)");
				}
			}
		});

	}

	/**
	 * For testing, get a collection of all the active modules
	 *
	 * @return the collection of active modules
	 */
	final Collection<Module> getActiveModules() {
		return this.activeModules.values();
	}


	/**
	 * @return instance of {@code PlatformServices} that is associated with this {@code EventHub}
	 */
	final PlatformServices getPlatformServices() {
		return this.services;
	}

	/**
	 * Creates a shared state object for the given module versioned at the current event for this hub
	 *
	 * @param module  Module that owns this shared state
	 * @param version int version that this configuration should begin to be valid for
	 * @param state   EventData object containing the state to share. Must be data,
	 *                {@link EventHub#SHARED_STATE_PENDING}, or {@link EventHub#SHARED_STATE_INVALID}
	 * @throws InvalidModuleException if the provided module is null or the provided module has a null
	 *                                sharedStateName()
	 **/
	void createSharedState(final Module module, final int version,
						   final EventData state) throws InvalidModuleException {
		createOrUpdateSharedStateCommon(module, version, state, true, false, SharedStateType.STANDARD);
	}

	/**
	 * Updates an existing {@link EventHub#SHARED_STATE_PENDING} for the given module and version
	 *
	 * @param module  Module to update the shared state for
	 * @param version int version to update
	 * @param state   new state to replace with existing state. Must be data, {@link EventHub#SHARED_STATE_PENDING},
	 *                {@link EventHub#SHARED_STATE_INVALID}, {@link EventHub#SHARED_STATE_NEXT}, or {@link EventHub#SHARED_STATE_PREV}.
	 * @throws InvalidModuleException if the provided module is null or the provided module has a null
	 *                                sharedStateName()
	 **/
	void updateSharedState(final Module module, final int version,
						   final EventData state) throws InvalidModuleException {
		createOrUpdateSharedStateCommon(module, version, state, false, true, SharedStateType.STANDARD);
	}

	/**
	 * Creates or updates a shared state object for the given {@code module} and {@code version}.
	 * If no shared state exists for the module at the given version, then one is created with the given state.
	 * If a shared state already exists for the module at the given version and the state
	 * is {@link EventHub#SHARED_STATE_PENDING}, then the state is updated with the given state.
	 * <p>
	 * Only for use by Module.
	 *
	 * @param module  Module that owns this shared state
	 * @param version version of the existing shared state to add or replace
	 * @param state   {@link EventData} object containing the state to share. Must be data,
	 *                {@link EventHub#SHARED_STATE_PENDING}, or {@link EventHub#SHARED_STATE_INVALID}
	 *                when creating or data, {@link EventHub#SHARED_STATE_PENDING}, {@link EventHub#SHARED_STATE_INVALID},
	 *                {@link EventHub#SHARED_STATE_NEXT}, or {@link EventHub#SHARED_STATE_PREV} when updating
	 * @throws InvalidModuleException if the provided module is null.
	 **/
	void createOrUpdateSharedState(final Module module,
								   final int version,
								   final EventData state) throws InvalidModuleException {
		createOrUpdateSharedStateCommon(module, version, state, true, true, SharedStateType.STANDARD);

	}

	/**
	 * Creates a shared state object for the given module versioned at the current event number for this hub.
	 * <p>
	 * NOTE: Do not call this method if the {@code module} also calls {@link #createSharedState(Module, int, EventData)}
	 * or {@link #createOrUpdateSharedState(Module, int, EventData)}. It may lead to data loss when attempting
	 * to create a shared state.
	 *
	 * @param module Module that owns this shared state
	 * @param state  EventData object containing the state to share. Must be data,
	 *               {@link EventHub#SHARED_STATE_PENDING}, or {@link EventHub#SHARED_STATE_INVALID}
	 * @throws InvalidModuleException if the provided module is null or the provided module has a null
	 *                                sharedStateName()
	 **/
	void createOrUpdateSharedState(final Module module,
								   final EventData state) throws InvalidModuleException {
		int version = currentEventNumber.get();
		// as this is a new state version, only create and not update
		createOrUpdateSharedStateCommon(module, version, state, true, false, SharedStateType.STANDARD);

	}

	/**
	 * Creates a shared state object for the given module versioned at the current event for this hub
	 *
	 * @param module  Module that owns this shared state
	 * @param version int version that this configuration should begin to be valid for
	 * @param state   EventData object containing the state to share. Must be data,
	 *                {@link EventHub#SHARED_STATE_PENDING}, or {@link EventHub#SHARED_STATE_INVALID}
	 * @param sharedStateType the type of shared state to be created
	 * @throws InvalidModuleException if the provided module is null or the provided module has a null
	 *                                sharedStateName()
	 **/
	void createSharedState(final Module module, final int version,
						   final EventData state, final SharedStateType sharedStateType) throws InvalidModuleException {
		createOrUpdateSharedStateCommon(module, version, state, true, false, sharedStateType);
	}

	/**
	 * Updates an existing {@link EventHub#SHARED_STATE_PENDING} for the given module and version
	 *
	 * @param module  Module to update the shared state for
	 * @param version int version to update
	 * @param state   new state to replace with existing state. Must be data, {@link EventHub#SHARED_STATE_PENDING},
	 *                {@link EventHub#SHARED_STATE_INVALID}, {@link EventHub#SHARED_STATE_NEXT}, or {@link EventHub#SHARED_STATE_PREV}.
	 * @param sharedStateType the type of shared state to be updated
	 * @throws InvalidModuleException if the provided module is null or the provided module has a null
	 *                                sharedStateName()
	 **/
	void updateSharedState(final Module module, final int version,
						   final EventData state, final SharedStateType sharedStateType) throws InvalidModuleException {
		createOrUpdateSharedStateCommon(module, version, state, false, true, sharedStateType);
	}

	/**
	 * Creates or updates a shared state object for the given {@code module} and {@code version}.
	 * If no shared state exists for the module at the given version, then one is created with the given state.
	 * If an shared state already exists for the module at the given version and the state
	 * is {@link EventHub#SHARED_STATE_PENDING}, then the state is updated with the given state.
	 * <p>
	 * Only for use by Module.
	 *
	 * @param module  Module that owns this shared state
	 * @param version version of the existing shared state to add or replace
	 * @param state   {@link EventData} object containing the state to share. Must be data,
	 *                {@link EventHub#SHARED_STATE_PENDING}, or {@link EventHub#SHARED_STATE_INVALID}
	 *                when creating or data, {@link EventHub#SHARED_STATE_PENDING}, {@link EventHub#SHARED_STATE_INVALID},
	 *                {@link EventHub#SHARED_STATE_NEXT}, or {@link EventHub#SHARED_STATE_PREV} when updating
	 * @param sharedStateType the type of shared state to be created
	 * @throws InvalidModuleException if the provided module is null.
	 **/
	void createOrUpdateSharedState(final Module module,
								   final int version,
								   final EventData state,
								   final SharedStateType sharedStateType) throws InvalidModuleException {
		createOrUpdateSharedStateCommon(module, version, state, true, true, sharedStateType);

	}

	/**
	 * Creates a shared state object for the given module versioned at the current event number for this hub.
	 * <p>
	 * NOTE: Do not call this method if the {@code module} also calls {@link #createSharedState(Module, int, EventData)}
	 * or {@link #createOrUpdateSharedState(Module, int, EventData)}. It may lead to data loss when attempting
	 * to create an shared state.
	 *
	 * @param module Module that owns this shared state
	 * @param state  EventData object containing the state to share. Must be data,
	 *               {@link EventHub#SHARED_STATE_PENDING}, or {@link EventHub#SHARED_STATE_INVALID}
	 * @param sharedStateType the type of shared state to be created or updated
	 * @throws InvalidModuleException if the provided module is null or the provided module has a null
	 *                                sharedStateName()
	 **/
	void createOrUpdateSharedState(final Module module,
								   final EventData state, final SharedStateType sharedStateType) throws InvalidModuleException {
		int version = currentEventNumber.get();
		// as this is a new state version, only create and not update
		createOrUpdateSharedStateCommon(module, version, state, true, false, sharedStateType);

	}

	/**
	 * Common helper method for creating and update shared states.
	 *
	 * @param module       Module that owns this shared state
	 * @param version      version of the existing shared state to add or replace
	 * @param state        {@link EventData} object containing the state to share. Must be data,
	 *                     {@link EventHub#SHARED_STATE_PENDING}, or {@link EventHub#SHARED_STATE_INVALID}
	 *                     when creating or data, {@link EventHub#SHARED_STATE_PENDING}, {@link EventHub#SHARED_STATE_INVALID},
	 *                     {@link EventHub#SHARED_STATE_NEXT}, or {@link EventHub#SHARED_STATE_PREV} when updating
	 * @param shouldCreate if this method should attempt to create a new shared state
	 * @param shouldUpdate if this method should attempt to update an existing shared state
	 * @param sharedStateType type of shared state
	 * @throws InvalidModuleException if the provided module is null or the provided module has a null
	 *                                sharedStateName()
	 */
	private void createOrUpdateSharedStateCommon(final Module module, final int version,
			final EventData state, final boolean shouldCreate, final boolean shouldUpdate, final SharedStateType sharedStateType)
	throws InvalidModuleException {
		if (module == null) {
			throw new InvalidModuleException(LOG_MODULE_WAS_NULL);
		}

		final String stateName = module.getModuleName();

		if (stateName == null) {
			throw new InvalidModuleException(LOG_STATENAME_WAS_NULL);
		}

		createOrUpdateSharedStateCommon(stateName, version, state, shouldCreate, shouldUpdate, sharedStateType);
	}

	/**
	 * Common helper method for creating and update shared states.
	 *
	 * @param moduleName   name of the Module that owns this shared state
	 * @param version      version of the existing shared state to add or replace
	 * @param state        {@link EventData} object containing the state to share. Must be data,
	 *                     {@link EventHub#SHARED_STATE_PENDING}, or {@link EventHub#SHARED_STATE_INVALID}
	 *                     when creating or data, {@link EventHub#SHARED_STATE_PENDING}, {@link EventHub#SHARED_STATE_INVALID},
	 *                     {@link EventHub#SHARED_STATE_NEXT}, or {@link EventHub#SHARED_STATE_PREV} when updating
	 * @param shouldCreate if this method should attempt to create a new shared state
	 * @param shouldUpdate if this method should attempt to update an existing shared state
	 * @param sharedStateType type of shared state
	 * @throws InvalidModuleException if the provided module is null or the provided module has a null
	 *                                sharedStateName()
	 */
	private void createOrUpdateSharedStateCommon(final String moduleName, final int version,
			final EventData state, final boolean shouldCreate,
			final boolean shouldUpdate, final SharedStateType sharedStateType) {
		boolean didUpdate = false;
		boolean didCreate = false;
		ConcurrentHashMap<String, RangedResolver<EventData>> sharedStates = sharedStateType == SharedStateType.XDM ?
				moduleXdmSharedStates : moduleSharedStates;

		if (!sharedStates.containsKey(moduleName)) {
			if (shouldCreate) {
				// first shared state for this 'stateName' module
				RangedResolver<EventData> resolver = new RangedResolver<EventData>(
					SHARED_STATE_PENDING,
					SHARED_STATE_INVALID,
					SHARED_STATE_NEXT,
					SHARED_STATE_PREV);
				didCreate = resolver.add(version, state);
				sharedStates.put(moduleName, resolver);
			}
		} else {
			// module 'stateName' already shared state
			if (shouldCreate) {
				// attempt to add this 'version'
				didCreate = sharedStates.get(moduleName).add(version, state);
			}

			if (shouldUpdate && !didCreate) {
				// attempt to update at 'version' if did not just create
				didUpdate = sharedStates.get(moduleName).update(version, state);
			}
		}

		if (!didCreate && !didUpdate) {
			Log.warning(logPrefix, "Unable to create or update shared state for %s with version %d.", moduleName, version);
			return;
		}

		if (state == SHARED_STATE_PENDING) {
			Log.trace(logPrefix, "Will not fire shared state for %s with version %d, when this shared state is PENDING.",
					  moduleName, version);
		} else {
			fireStateChangeEvent(moduleName, sharedStateType);

			// we only want to run the expensive string manipulation code in the "prettyString"
			// method when we know that the resulting string is going to be used in the log
			if (Log.getLogLevel().id >= LoggingMode.VERBOSE.id) {
				Log.trace(logPrefix, "New shared state data for '%s' at version '%d': \n%s", moduleName, version,
						  state.prettyString(1));
			}
		}
	}

	/**
	 * Retrieves shared state by name that is valid for the given event. If {@code event} is null, retrieves the
	 * latest shared state for {@code stateName}.
	 *
	 * @param stateName     String identifier for the module that shared the state
	 * @param event         {@link Event}'s version used to retrieve corresponding state, or the latest state if null
	 * @param callingModule the module calling this method
	 * @return EventData object containing the valid state,
	 * {@link EventHub#SHARED_STATE_PENDING}, or {@link EventHub#SHARED_STATE_INVALID}
	 **/
	EventData getSharedEventState(final String stateName, final Event event, final Module callingModule) {
		return getSharedEventStateCommon(stateName, event, callingModule, SharedStateType.STANDARD);
	}

	/**
	 * Retrieves shared state by name that is valid for the given event. If {@code event} is null, retrieves the
	 * latest shared state for {@code stateName}.
	 *
	 * @param stateName     String identifier for the module that shared the state
	 * @param event         {@link Event}'s version used to retrieve corresponding state, or the latest state if null
	 * @param callingModule the module calling this method
	 * @param sharedStateType the type of shared state to be read
	 * @return EventData object containing the valid state,
	 * {@link EventHub#SHARED_STATE_PENDING}, or {@link EventHub#SHARED_STATE_INVALID}
	 **/
	EventData getSharedEventState(final String stateName, final Event event, final Module callingModule,
								  final SharedStateType sharedStateType) {
		return getSharedEventStateCommon(stateName, event, callingModule, sharedStateType);
	}

	private EventData getSharedEventStateCommon(final String stateName, final Event event, final Module callingModule,
			final SharedStateType sharedStateType) {
		if (stateName == null) {
			throw new IllegalArgumentException(LOG_STATENAME_WAS_NULL);
		}

		int eventVersion = Event.SHARED_STATE_NEWEST.getEventNumber(); // default to getting latest

		if (event != null) {
			eventVersion = event.getEventNumber();
		}

		// check for circular dependency (live lock risk)
		// this can occur if you have two modules (m1, m2) that are inter-dependent on shared states (m1->m2 and
		// m2->m1).
		// this code provides detection and logging, we can't avoid the livelock but we should at least detect it.
		if (Log.getLogLevel().id >= LoggingMode.DEBUG.id && callingModule != null) {
			final String callingModuleStateName = callingModule.getModuleName();
			this.sharedStateCircularCheck.put(callingModuleStateName + stateName, true);

			if (this.sharedStateCircularCheck.get(stateName + callingModuleStateName) != null) {
				Log.warning(logPrefix, "Circular shared-state dependency between %s and %s, you may have a " +
							"live-lock.",
							callingModuleStateName, stateName);
			}
		}

		final RangedResolver<EventData> sharedState = sharedStateType == SharedStateType.XDM ? this.moduleXdmSharedStates.get(
					stateName) : this.moduleSharedStates.get(stateName);

		if (sharedState != null) {
			return sharedState.resolve(eventVersion);
		}

		return SHARED_STATE_PENDING;
	}

	/**
	 * Determine if there are any shared states for a specified module.
	 * A module is considered to have a valid shared state if any state contains data or is {@link #SHARED_STATE_PENDING}.
	 * States which are {@link #SHARED_STATE_INVALID}, {@link #SHARED_STATE_NEXT}, or {@link #SHARED_STATE_PREV}
	 * are not considered valid.
	 *
	 * @param stateName {@link String} identifier for the module which shared a state
	 * @return true if the specified module has shared a state which either contains data or is {@link #SHARED_STATE_PENDING}
	 */
	boolean hasSharedEventState(final String stateName) {
		return hasSharedEventState(stateName, SharedStateType.STANDARD);
	}

	/**
	 * Determine if there are any shared states for a specified module.
	 * A module is considered to have a valid shared state if any state contains data or is {@link #SHARED_STATE_PENDING}.
	 * States which are {@link #SHARED_STATE_INVALID}, {@link #SHARED_STATE_NEXT}, or {@link #SHARED_STATE_PREV}
	 * are not considered valid.
	 *
	 * @param stateName {@link String} identifier for the module which shared a state
	 * @param sharedStateType the type of shared state checked
	 * @return true if the specified module has shared a state which either contains data or is {@link #SHARED_STATE_PENDING}
	 */
	boolean hasSharedEventState(final String stateName, final SharedStateType sharedStateType) {
		if (stateName == null) {
			throw new IllegalArgumentException(LOG_STATENAME_WAS_NULL);
		}

		final RangedResolver<EventData> sharedStates = sharedStateType == SharedStateType.XDM ? this.moduleXdmSharedStates.get(
					stateName) : this.moduleSharedStates.get(stateName);

		return sharedStates != null && sharedStates.containsValidState();
	}

	/**
	 * Clears all the shared states for the given module.
	 * <p>
	 * Only for use by Module.
	 *
	 * @param module {@link Module} to clear the shared states for
	 * @throws InvalidModuleException if the provided module is null or the provided module has a
	 *                                null {@link Module#getModuleName()}
	 **/
	void clearSharedStates(final Module module) throws InvalidModuleException {
		clearSharedStateCommon(module, SharedStateType.STANDARD);
	}

	/**
	 * Clears all the shared states for the given module.
	 * <p>
	 * Only for use by Module.
	 *
	 * @param module {@link Module} to clear the shared states for
	 * @param sharedStateType the type of shared state to be created
	 * @throws InvalidModuleException if the provided module is null or the provided module has a
	 *                                null {@link Module#getModuleName()}
	 **/
	void clearSharedStates(final Module module, final SharedStateType sharedStateType) throws InvalidModuleException {
		clearSharedStateCommon(module, sharedStateType);
	}

	private void clearSharedStateCommon(final Module module,
										final SharedStateType sharedStateType) throws InvalidModuleException {
		if (module == null) {
			throw new InvalidModuleException(LOG_MODULE_WAS_NULL);
		}

		final String stateName = module.getModuleName();

		if (stateName == null) {
			throw new InvalidModuleException(LOG_STATENAME_WAS_NULL);
		}

		if (sharedStateType == SharedStateType.XDM) {
			this.moduleXdmSharedStates.remove(stateName);
		} else {
			this.moduleSharedStates.remove(stateName);
		}

		fireStateChangeEvent(stateName, sharedStateType);
	}

	/**
	 * For test, need to shutdown the threadPoll after each test
	 */
	void shutdown() {
		this.eventHubThreadService.shutdownNow();
		this.threadPool.shutdownNow();
	}

	/**
	 * For platform tests, need to clear data from memory.
	 */
	void resetSharedStates() {
		this.moduleSharedStates.clear();
		this.moduleXdmSharedStates.clear();
	}

	/**
	 * For extensions tests, to check that a listener was registered/unregistered
	 *
	 * @param module the module for which to retrieve the listeners
	 * @return the list of listeners for the provided module
	 */
	ConcurrentLinkedQueue<EventListener> getModuleListeners(final Module module) {
		return this.moduleListeners.get(module);
	}

	/**
	 * Creates and publishes a state change event to this event hub
	 *
	 * @param stateName stateName state-name of the module that published the change
	 * @param sharedStateType the type of the shared state updated
	 **/
	private void fireStateChangeEvent(final String stateName, final SharedStateType sharedStateType) {
		final String eventName = sharedStateType == SharedStateType.STANDARD ? STANDARD_STATE_CHANGE_EVENTNAME :
								 XDM_STATE_CHANGE_EVENTNAME;
		final Event updateEvent = new Event.Builder(eventName, EventType.HUB, EventSource.SHARED_STATE)
		.setData(new EventData().putString(EventHubConstants.EventDataKeys.Configuration.EVENT_STATE_OWNER, stateName))
		.build();
		this.dispatch(updateEvent);
	}

	protected void createEventHubSharedState(final int eventNumber) {
		createOrUpdateSharedStateCommon(EventHubConstants.EventDataKeys.EventHub.SHARED_STATE_NAME,
										eventNumber, eventHubSharedState, true, false, SharedStateType.STANDARD);
	}

	protected void addModuleToEventHubSharedState(final Module module) {
		if (module == null) {
			return;
		}

		final ModuleDetails details = module.getModuleDetails();

		final String moduleName = module.getModuleName();
		final String friendlyName = details == null ? module.getModuleName() : details.getName();
		final String moduleVersion = details == null ? module.getModuleVersion() : details.getVersion();

		if (StringUtils.isNullOrEmpty(moduleName)) {
			return;
		}

		Log.trace(logPrefix, "Registering extension '%s' with version '%s'", moduleName, moduleVersion);

		final Map<String, Variant> extensionsMap = eventHubSharedState.optVariantMap(
					EventHubConstants.EventDataKeys.EventHub.EXTENSIONS, new HashMap<String, Variant>());
		final Map<String, Variant> moduleMap = new HashMap<String, Variant>();
		moduleMap.put(EventHubConstants.EventDataKeys.EventHub.VERSION,
					  Variant.fromString(moduleVersion != null ? moduleVersion : ""));
		moduleMap.put(EventHubConstants.EventDataKeys.EventHub.FRIENDLY_NAME,
					  Variant.fromString(friendlyName != null ? friendlyName : moduleName));
		extensionsMap.put(moduleName, Variant.fromVariantMap(moduleMap));
		eventHubSharedState.putVariantMap(EventHubConstants.EventDataKeys.EventHub.EXTENSIONS,
										  extensionsMap);

		// only update the shared state if we're beyond the boot event
		synchronized (bootMutex) {
			if (isBooted) {
				createEventHubSharedState(currentEventNumber.get());
			}
		}
	}

	protected void removeModuleFromEventHubSharedState(final Module module) {
		// quick out if the shared state doesn't exist yet
		if (module == null) {
			return;
		}

		final String moduleName = module.getModuleName();

		if (StringUtils.isNullOrEmpty(moduleName)) {
			return;
		}

		final Map<String, Variant> extensionsMap = eventHubSharedState.optVariantMap(
					EventHubConstants.EventDataKeys.EventHub.EXTENSIONS, new HashMap<String, Variant>());
		extensionsMap.remove(moduleName);
		eventHubSharedState.putVariantMap(EventHubConstants.EventDataKeys.EventHub.EXTENSIONS,
										  extensionsMap);

		// only update the shared state if we're beyond the boot event
		synchronized (bootMutex) {
			if (isBooted) {
				createEventHubSharedState(currentEventNumber.get());
			}
		}
	}

	protected EventData getInitialEventHubSharedState() {
		final EventData state = new EventData();

		state.putString(EventHubConstants.EventDataKeys.EventHub.VERSION, coreVersion);
		state.putVariantMap(EventHubConstants.EventDataKeys.EventHub.EXTENSIONS, new HashMap<String, Variant>());
		state.putStringMap(EventHubConstants.EventDataKeys.EventHub.WRAPPER, getWrapperInfo());

		return state;
	}

	/**
	 * Gets the SDK's current version with wrapper type.
	 */
	String getSdkVersion() {
		String version = coreVersion;

		if (wrapperType != WrapperType.NONE) {
			version += SDK_VERSION_DELIMITER + wrapperType.getWrapperTag();
		}

		return version;
	}

	/**
	 * Sets the SDK's current wrapper type. This API should only be used if
	 * being developed on platforms such as React Native.
	 *
	 * @param wrapperType the type of wrapper being used.
	 */
	void setWrapperType(final WrapperType wrapperType) {
		synchronized (bootMutex) {
			if (isBooted) {
				Log.warning(logPrefix, "Cannot set wrapper type to (%s) - (%s) as event hub has already booted.",
							wrapperType.getWrapperTag(), wrapperType.getFriendlyName());
				return;
			}

			this.wrapperType = wrapperType;
			// update wrapperType info in hub shared state
			eventHubSharedState.putStringMap(EventHubConstants.EventDataKeys.EventHub.WRAPPER, getWrapperInfo());
		}
	}

	/**
	 * Returns the SDK's current wrapper type.
	 *
	 * @return {@link WrapperType} enum.
	 */
	WrapperType getWrapperType() {
		return wrapperType;
	}

	/**
	 * Returns the wrapper info with wrapper tag and friendlyName.
	 *
	 * @return map containing wrapper tag and friendlyName.
	 */
	private HashMap<String, String> getWrapperInfo() {
		HashMap<String, String> wrapperInfo = new HashMap();
		wrapperInfo.put(EventHubConstants.EventDataKeys.EventHub.TYPE, wrapperType.getWrapperTag());
		wrapperInfo.put(EventHubConstants.EventDataKeys.EventHub.FRIENDLY_NAME, wrapperType.getFriendlyName());

		return wrapperInfo;
	}

	private final class ReprocessEventsWithRules implements Runnable {

		final ReprocessEventsHandler reprocessEventsHandler;
		final List<Rule> rules;
		final List<Event> consequenceEvents;
		final Module module;

		ReprocessEventsWithRules(final ReprocessEventsHandler reprocessEventsHandler, final List<Rule> rules,
								 final Module module) {
			this.reprocessEventsHandler = reprocessEventsHandler;
			this.rules = rules;
			this.module = module;
			this.consequenceEvents = new ArrayList<Event>();
		}

		@Override
		public void run() {
			try {
				List<Event> events = reprocessEventsHandler.getEvents();

				if (events.size() > REPROCESS_EVENTS_AMOUNT_LIMIT) {
					Log.debug(logPrefix, "Failed to reprocess cached events, since the amount of events (%s) reach the limits (%s)",
							  events.size(),
							  REPROCESS_EVENTS_AMOUNT_LIMIT);
				} else {
					for (final Event e : events) {
						List<Event> resultEvents = rulesEngine.evaluateEventWithRules(e, rules);
						consequenceEvents.addAll(resultEvents);
					}
				}

				reprocessEventsHandler.onEventReprocessingComplete();

				replaceModuleRules(module, rules);

				for (final Event e : consequenceEvents) {
					dispatch(e);
				}
			} catch (Exception e) {
				Log.debug(logPrefix, "Failed to reprocess cached events (%s)", e);
			}
		}
	}

	/**
	 * Class implementing the Runnable for event
	 */
	private final class EventRunnable implements Runnable {
		final Event event;

		EventRunnable(final Event event) {
			this.event = event;
		}

		@Override
		public void run() {
			// run rules
			final long preRulesTime = System.currentTimeMillis();
			final List<Event> resultEvents = rulesEngine.evaluateRules(event);

			for (final Event e : resultEvents) {
				dispatch(e);
			}

			final long totalRulesTime = System.currentTimeMillis() - preRulesTime;
			Log.trace(logPrefix, "Event (%s) #%d (%s) resulted in %d consequence events. Time in rules was %d milliseconds.",
					  event.getUniqueIdentifier(), event.getEventNumber(), event.getName(), resultEvents.size(), totalRulesTime);
			eventBus.dispatch(event);
		}
	}
}
