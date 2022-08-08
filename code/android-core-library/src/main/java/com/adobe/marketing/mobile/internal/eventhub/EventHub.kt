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

package com.adobe.marketing.mobile.internal.eventhub

import com.adobe.marketing.mobile.AdobeCallback
import com.adobe.marketing.mobile.AdobeCallbackWithError
import com.adobe.marketing.mobile.AdobeError
import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.EventSource
import com.adobe.marketing.mobile.EventType
import com.adobe.marketing.mobile.Extension
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.SharedStateResolution
import com.adobe.marketing.mobile.SharedStateResolver
import com.adobe.marketing.mobile.SharedStateResult
import com.adobe.marketing.mobile.SharedStateStatus
import com.adobe.marketing.mobile.WrapperType
import com.adobe.marketing.mobile.internal.eventhub.history.AndroidEventHistory
import com.adobe.marketing.mobile.internal.eventhub.history.EventHistory
import com.adobe.marketing.mobile.internal.utility.prettify
import com.adobe.marketing.mobile.util.SerialWorkDispatcher
import com.adobe.marketing.mobile.utils.EventDataUtils
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * EventHub class is responsible for delivering events to listeners and maintaining registered extension's lifecycle.
 */
internal class EventHub {

    companion object {
        const val LOG_TAG = "EventHub"
        var shared = EventHub()
    }

    /**
     * Executor to initialize and shutdown extensions
     */
    private val extensionInitExecutor: ExecutorService by lazy { Executors.newCachedThreadPool() }

    /**
     * Executor for scheduled response listeners
     */
    private val scheduledExecutor: ScheduledExecutorService by lazy { Executors.newSingleThreadScheduledExecutor() }

    /**
     * Executor to serialize EventHub operations
     */
    private val eventHubExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }

    private val registeredExtensions: ConcurrentHashMap<String, ExtensionContainer> =
        ConcurrentHashMap()
    private val responseEventListeners: ConcurrentLinkedQueue<ResponseListenerContainer> =
        ConcurrentLinkedQueue()
    private val lastEventNumber: AtomicInteger = AtomicInteger(0)
    private var hubStarted = false
    internal val eventHistory: EventHistory? = try {
        AndroidEventHistory()
    } catch (e: Exception) {
        null
    }

    /**
     * Implementation of [SerialWorkDispatcher.WorkHandler] that is responsible for dispatching
     * an [Event] "e". Dispatch is regarded complete when [SerialWorkDispatcher.WorkHandler.doWork] finishes for "e".
     */
    private val dispatchJob: SerialWorkDispatcher.WorkHandler<Event> =
        SerialWorkDispatcher.WorkHandler { event ->
            // TODO: Perform pre-processing

            // Handle response event listeners
            if (event.responseID != null) {
                val matchingResponseListeners = responseEventListeners.filterRemove { listener ->
                    if (listener.shouldNotify(event)) {
                        listener.timeoutTask?.cancel(false)
                        true
                    } else {
                        false
                    }
                }

                matchingResponseListeners.forEach { listener ->
                    listener.notify(event)
                }
            }

            // Notify to extensions for processing
            registeredExtensions.values.forEach {
                it.eventProcessor.offer(event)
            }

            // TODO: Record events in event history database.
        }

    /**
     * Responsible for processing and dispatching each event.
     */
    private val eventDispatcher: SerialWorkDispatcher<Event> =
        SerialWorkDispatcher("EventHub", dispatchJob)

    /**
     * A cache that maps UUID of an Event to an internal sequence of its dispatch.
     */
    private val eventNumberMap: ConcurrentHashMap<String, Int> = ConcurrentHashMap<String, Int>()

    init {
        registerExtension(EventHubPlaceholderExtension::class.java) {}
    }

    private var _wrapperType = WrapperType.NONE
    var wrapperType: WrapperType
        get() {
            return eventHubExecutor.submit(
                Callable {
                    return@Callable _wrapperType
                }
            ).get()
        }
        set(value) {
            eventHubExecutor.submit(
                Callable {
                    if (hubStarted) {
                        MobileCore.log(
                            LoggingMode.WARNING,
                            LOG_TAG,
                            "Wrapper type can not be set after EventHub starts processing events"
                        )
                        return@Callable
                    }

                    _wrapperType = value
                }
            ).get()
        }

    /**
     * `EventHub` will begin processing `Event`s when this API is invoked.
     */
    fun start() {
        eventHubExecutor.submit {
            this.hubStarted = true
            this.eventDispatcher.start()
            this.shareEventHubSharedState()
            MobileCore.log(LoggingMode.DEBUG, LOG_TAG, "Event Hub successfully started")
        }
    }

    /**
     * Dispatches a new [Event] to all listeners who have registered for the event type and source.
     * If the `event` has a `mask`, this method will attempt to record the `event` in `eventHistory`.
     * See [eventDispatcher] for more details.
     *
     * @param event the [Event] to be dispatched to listeners
     */
    fun dispatch(event: Event) {
        eventHubExecutor.submit {
            dispatchInternal(event)
        }
    }

    /**
     * Internal method to dispatch an event
     */
    private fun dispatchInternal(event: Event) {
        val eventNumber = lastEventNumber.incrementAndGet()
        eventNumberMap[event.uniqueIdentifier] = eventNumber

        // Offer event to the serial dispatcher to perform operations on the event.
        if (!eventDispatcher.offer(event)) {
            MobileCore.log(
                LoggingMode.WARNING,
                LOG_TAG,
                "Failed to dispatch event #$eventNumber - ($event)"
            )
        }

        if (MobileCore.getLogLevel() == LoggingMode.VERBOSE) {
            MobileCore.log(
                LoggingMode.VERBOSE,
                LOG_TAG,
                "Dispatched Event #$eventNumber - ($event)"
            )
        }

        event.mask?.let {
            eventHistory?.recordEvent(event) { result ->
                MobileCore.log(
                    LoggingMode.VERBOSE,
                    LOG_TAG,
                    if (result) "Successfully inserted an Event into EventHistory database" else "Failed to insert an Event into EventHistory database"
                )
            }
        }
    }

    /**
     * Registers a new [Extension] to the `EventHub`. This extension must extend [Extension] class
     *
     * @param extensionClass The class of extension to register
     * @param completion Invoked when the extension has been registered or failed to register
     */
    fun registerExtension(
        extensionClass: Class<out Extension>?,
        completion: (error: EventHubError) -> Unit
    ) {
        eventHubExecutor.submit {
            if (extensionClass == null) {
                completion(EventHubError.ExtensionInitializationFailure)
                return@submit
            }

            val extensionTypeName = extensionClass.extensionTypeName
            if (registeredExtensions.containsKey(extensionTypeName)) {
                completion(EventHubError.DuplicateExtensionName)
                return@submit
            }

            val container = ExtensionContainer(extensionClass, extensionInitExecutor, completion)
            registeredExtensions[extensionTypeName] = container
        }
    }

    /**
     * Unregisters the extension from the `EventHub` if registered
     * @param extensionClass The class of extension to unregister
     * @param completion Invoked when the extension has been unregistered or failed to unregister
     */
    fun unregisterExtension(
        extensionClass: Class<out Extension>?,
        completion: ((error: EventHubError) -> Unit)
    ) {
        eventHubExecutor.submit {
            val extensionName = extensionClass?.extensionTypeName
            val container = registeredExtensions.remove(extensionName)

            if (container != null) {
                container.shutdown()
                shareEventHubSharedState()
                completion(EventHubError.None)
            } else {
                completion(EventHubError.ExtensionNotRegistered)
            }
        }
    }

    /**
     * Registers an event listener which will be invoked when the response event to trigger event is dispatched
     * @param triggerEvent An [Event] which will trigger a response event
     * @param timeoutMS A timeout in milliseconds, if the response listener is not invoked within the timeout, then the `EventHub` invokes the fail method.
     * @param listener An [AdobeCallbackWithError] which will be invoked whenever the `EventHub` receives the response event for trigger event
     */
    fun registerResponseListener(
        triggerEvent: Event,
        timeoutMS: Long,
        listener: AdobeCallbackWithError<Event>
    ) {
        eventHubExecutor.submit {
            val triggerEventId = triggerEvent.uniqueIdentifier
            val timeoutCallable: Callable<Unit> = Callable {
                responseEventListeners.filterRemove { it.triggerEventId == triggerEventId }
                try {
                    listener.fail(AdobeError.CALLBACK_TIMEOUT)
                } catch (ex: Exception) {
                    MobileCore.log(
                        LoggingMode.DEBUG,
                        LOG_TAG,
                        "Exception thrown from ResponseListener - $ex"
                    )
                }
            }
            val timeoutTask =
                scheduledExecutor.schedule(timeoutCallable, timeoutMS, TimeUnit.MILLISECONDS)

            responseEventListeners.add(
                ResponseListenerContainer(
                    triggerEventId,
                    timeoutTask,
                    listener
                )
            )
        }
    }

    /**
     * Registers an event listener which will be invoked whenever an [Event] with matched type and source is dispatched
     * @param eventType A String indicating the event type the current listener is listening for
     * @param eventSource A `String` indicating the event source the current listener is listening for
     * @param listener An [AdobeCallback] which will be invoked whenever the `EventHub` receives a event with matched type and source
     */
    fun registerListener(eventType: String, eventSource: String, listener: AdobeCallback<Event>) {
        eventHubExecutor.submit {
            val eventHubContainer = getExtensionContainer(EventHubPlaceholderExtension::class.java)
            eventHubContainer?.registerEventListener(eventType, eventSource) { listener.call(it) }
        }
    }

    /**
     * Creates a new shared state for the extension with provided data, versioned at [Event]
     * If `event` is nil, one of two behaviors will be observed:
     * 1. If this extension has not previously published a shared state, shared state will be versioned at 0
     * 2. If this extension has previously published a shared state, shared state will be versioned at the latest
     * @param sharedStateType The type of shared state to be set
     * @param extensionName Extension whose shared state is to be updated
     * @param state Map which contains data for the shared state
     * @param event [Event] for which the `SharedState` should be versioned
     * @return true - if shared state is created successfully
     */
    fun createSharedState(
        sharedStateType: SharedStateType,
        extensionName: String,
        state: MutableMap<String, Any?>?,
        event: Event?,
    ): Boolean {
        val immutableState = try {
            EventDataUtils.immutableClone(state)
        } catch (ex: Exception) {
            MobileCore.log(
                LoggingMode.WARNING,
                LOG_TAG,
                "Creating $sharedStateType shared state for extension $extensionName at event ${event?.uniqueIdentifier} with null - Cloning state failed with exception $ex"
            )
            null
        }

        val callable = Callable {
            return@Callable createSharedStateInternal(
                sharedStateType,
                extensionName,
                immutableState,
                event
            )
        }
        return eventHubExecutor.submit(callable).get()
    }

    /**
     * Internal method to creates a new shared state for the extension with provided data, versioned at [Event]
     */
    private fun createSharedStateInternal(
        sharedStateType: SharedStateType,
        extensionName: String,
        state: MutableMap<String, Any?>?,
        event: Event?,
    ): Boolean {
        val sharedStateManager = getSharedStateManager(sharedStateType, extensionName)
        sharedStateManager ?: run {
            MobileCore.log(
                LoggingMode.WARNING,
                LOG_TAG,
                "Create $sharedStateType shared state for extension $extensionName for event ${event?.uniqueIdentifier} failed - SharedStateManager is null"
            )
            return false
        }

        val version = resolveSharedStateVersion(sharedStateManager, event)
        val didSet = sharedStateManager.setState(version, state)
        if (!didSet) {
            MobileCore.log(
                LoggingMode.WARNING,
                LOG_TAG,
                "Create $sharedStateType shared state for extension $extensionName for event ${event?.uniqueIdentifier} failed - SharedStateManager failed"
            )
        } else {
            dispatchSharedStateEvent(sharedStateType, extensionName)
            MobileCore.log(
                LoggingMode.DEBUG,
                LOG_TAG,
                "Created $sharedStateType shared state for extension $extensionName with version $version and data ${state?.prettify()}"
            )
        }

        return didSet
    }

    /**
     * Sets the shared state for the extension to pending at event's version and returns a [SharedStateResolver] which is to be invoked with data for the shared state once available.
     * If event is nil, one of two behaviors will be observed:
     * 1. If this extension has not previously published a shared state, shared state will be versioned at 0
     * 2. If this extension has previously published a shared state, shared state will be versioned at the latest
     * @param sharedStateType The type of shared state to be set
     * @param extensionName Extension whose shared state is to be updated
     * @param event [Event] for which the `SharedState` should be versioned
     * @return A [SharedStateResolver] which is invoked to set pending the shared state versioned at [Event]
     */
    fun createPendingSharedState(
        sharedStateType: SharedStateType,
        extensionName: String,
        event: Event?,
    ): SharedStateResolver? {
        val callable = Callable<SharedStateResolver?> {
            val sharedStateManager = getSharedStateManager(sharedStateType, extensionName)
            sharedStateManager ?: run {
                MobileCore.log(
                    LoggingMode.WARNING,
                    LOG_TAG,
                    "Create pending $sharedStateType shared state for extension $extensionName for event ${event?.uniqueIdentifier} failed - SharedStateManager is null"
                )
                return@Callable null
            }

            val pendingVersion = resolveSharedStateVersion(sharedStateManager, event)
            val didSetPending = sharedStateManager.setPendingState(pendingVersion)
            if (!didSetPending) {
                MobileCore.log(
                    LoggingMode.WARNING,
                    LOG_TAG,
                    "Create pending $sharedStateType shared state for extension $extensionName for event ${event?.uniqueIdentifier} failed - SharedStateManager failed"
                )
                return@Callable null
            }

            MobileCore.log(
                LoggingMode.DEBUG,
                LOG_TAG,
                "Created pending $sharedStateType shared state for extension $extensionName with version $pendingVersion"
            )
            return@Callable SharedStateResolver {
                resolvePendingSharedState(sharedStateType, extensionName, it, pendingVersion)
            }
        }

        return eventHubExecutor.submit(callable).get()
    }

    /**
     * Updates a pending shared state and dispatches it to the `EventHub`
     * Providing a version for which there is no pending state will result in a no-op.
     * @param sharedStateType The type of shared state to be set
     * @param extensionName Extension whose shared state is to be updated
     * @param state Map which contains data for the shared state
     * @param version An `Int` containing the version of the state being updated
     */
    private fun resolvePendingSharedState(
        sharedStateType: SharedStateType,
        extensionName: String,
        state: MutableMap<String, Any?>?,
        version: Int
    ) {
        val immutableState = try {
            EventDataUtils.immutableClone(state)
        } catch (ex: Exception) {
            MobileCore.log(
                LoggingMode.WARNING,
                LOG_TAG,
                "Resolving pending $sharedStateType shared state for extension $extensionName and version $version with null - Clone failed with exception $ex"
            )
            null
        }

        val callable = Callable {
            val sharedStateManager = getSharedStateManager(sharedStateType, extensionName) ?: run {
                MobileCore.log(
                    LoggingMode.WARNING,
                    LOG_TAG,
                    "Resolve pending $sharedStateType shared state for extension $extensionName and version $version failed - SharedStateManager is null"
                )
                return@Callable
            }

            val didUpdate = sharedStateManager.updatePendingState(version, immutableState)
            if (!didUpdate) {
                MobileCore.log(
                    LoggingMode.WARNING,
                    LOG_TAG,
                    "Resolve pending $sharedStateType shared state for extension $extensionName and version $version failed - SharedStateManager failed"
                )
                return@Callable
            }

            dispatchSharedStateEvent(sharedStateType, extensionName)
            MobileCore.log(
                LoggingMode.DEBUG,
                LOG_TAG,
                "Resolved pending $sharedStateType shared state for $extensionName and version $version with data ${immutableState?.prettify()}"
            )
        }
        eventHubExecutor.submit(callable).get()
    }

    /**
     * Retrieves the shared state for a specific extension
     * @param sharedStateType The type of shared state to be set
     * @param extensionName Extension whose shared state will be returned
     * @param event If not nil, will retrieve the shared state that corresponds with this event's version or latest if not yet versioned. If event is nil will return the latest shared state
     * @param barrier If true, the `EventHub` will only return [SharedStateStatus.SET] if [extensionName] has moved past [Event]
     * @param resolution The [SharedStateResolution] to determine how to resolve the shared state
     * @return The shared state data and status for the extension with [extensionName]
     */
    fun getSharedState(
        sharedStateType: SharedStateType,
        extensionName: String,
        event: Event?,
        barrier: Boolean,
        resolution: SharedStateResolution,
    ): SharedStateResult? {
        val callable = Callable<SharedStateResult?> {
            val container = getExtensionContainer(extensionName) ?: run {
                MobileCore.log(
                    LoggingMode.WARNING,
                    LOG_TAG,
                    "Get $sharedStateType shared state for extension $extensionName and event ${event?.uniqueIdentifier} failed - ExtensionContainer is null"
                )
                return@Callable null
            }

            val sharedStateManager = getSharedStateManager(sharedStateType, extensionName) ?: run {
                MobileCore.log(
                    LoggingMode.WARNING,
                    LOG_TAG,
                    "Get $sharedStateType shared state for extension $extensionName and event ${event?.uniqueIdentifier} failed - SharedStateManager is null"
                )
                return@Callable null
            }

            val version = getEventNumber(event) ?: SharedStateManager.VERSION_LATEST

            val result: SharedStateResult = when (resolution) {
                SharedStateResolution.ANY -> sharedStateManager.resolve(version)
                SharedStateResolution.LAST_SET -> sharedStateManager.resolveLastSet(version)
            }

            val stateProviderLastVersion = getEventNumber(container.lastProcessedEvent) ?: 0
            // shared state is still considered pending if barrier is used and the state provider has not processed past the previous event
            val hasProcessedEvent =
                if (event == null) true else stateProviderLastVersion > version - 1
            return@Callable if (barrier && !hasProcessedEvent && result.status == SharedStateStatus.SET) {
                SharedStateResult(SharedStateStatus.PENDING, result.value)
            } else {
                result
            }
        }

        return eventHubExecutor.submit(callable).get()
    }

    /**
     * Clears all shared state previously set by [extensionName].
     *
     * @param sharedStateType the type of shared state that needs to be cleared.
     * @param extensionName the name of the extension for which the state is being cleared
     * @return true - if the shared state has been cleared, false otherwise
     */
    fun clearSharedState(
        sharedStateType: SharedStateType,
        extensionName: String,
    ): Boolean {
        val callable = Callable {
            val sharedStateManager = getSharedStateManager(sharedStateType, extensionName) ?: run {
                MobileCore.log(
                    LoggingMode.WARNING,
                    LOG_TAG,
                    "Clear $sharedStateType shared state for extension $extensionName failed - SharedStateManager is null"
                )
                return@Callable false
            }

            sharedStateManager.clear()
            MobileCore.log(
                LoggingMode.VERBOSE,
                LOG_TAG,
                "Cleared $sharedStateType shared state for extension $extensionName"
            )
            return@Callable true
        }

        return eventHubExecutor.submit(callable).get()
    }

    /**
     * Stops processing events and shuts down all registered extensions.
     */
    fun shutdown() {
        // Shutdown and clear all the extensions.
        eventHubExecutor.submit {
            eventDispatcher.shutdown()

            // Unregister all extensions
            registeredExtensions.forEach { (_, extensionContainer) ->
                extensionContainer.shutdown()
            }
            registeredExtensions.clear()
        }
        eventHubExecutor.shutdown()
    }

    /**
     * Retrieve the event number for the Event from the [eventNumberMap]
     *
     * @param event the [Event] for which the event number should be resolved
     * @return the event number for the event if it exists (if it has been recorded/dispatched),
     *         null otherwise
     */
    private fun getEventNumber(event: Event?): Int? {
        if (event == null) {
            return null
        }
        val eventUUID = event.uniqueIdentifier
        return eventNumberMap[eventUUID]
    }

    /**
     * Retrieves a registered [ExtensionContainer] with [extensionClass] provided.
     *
     * @param extensionClass the extension class for which an [ExtensionContainer] should be fetched.
     * @return [ExtensionContainer] with [extensionName] provided if one was registered,
     *         null if no extension is registered with the [extensionName]
     */
    internal fun getExtensionContainer(extensionClass: Class<out Extension>): ExtensionContainer? {
        return registeredExtensions[extensionClass.extensionTypeName]
    }

    /**
     * Retrieves a registered [ExtensionContainer] with [extensionTypeName] provided.
     *
     * @param extensionName the name of the extension for which an [ExtensionContainer] should be fetched.
     *        This should match [Extension.name] of an extension registered with the event hub.
     * @return [ExtensionContainer] with [extensionName] provided if one was registered,
     *         null if no extension is registered with the [extensionName]
     */
    private fun getExtensionContainer(extensionName: String): ExtensionContainer? {
        val extensionContainer = registeredExtensions.entries.firstOrNull {
            return@firstOrNull (
                it.value.sharedStateName?.equals(
                    extensionName,
                    true
                ) ?: false
                )
        }
        return extensionContainer?.value
    }

    /**
     * Retrieves the [SharedStateManager] of type [sharedStateType] with [extensionName] provided.
     *
     * @param sharedStateType the [SharedStateType] for which an [SharedStateManager] should be fetched.
     * @param extensionName the name of the extension for which an [SharedStateManager] should be fetched.
     *        This should match [Extension.name] of an extension registered with the event hub.
     * @return [SharedStateManager] with [extensionName] provided if one was registered and initialized
     *         null otherwise
     */
    private fun getSharedStateManager(
        sharedStateType: SharedStateType,
        extensionName: String
    ): SharedStateManager? {
        val extensionContainer = getExtensionContainer(extensionName) ?: run {
            return null
        }
        val sharedStateManager = extensionContainer.getSharedStateManager(sharedStateType) ?: run {
            return null
        }
        return sharedStateManager
    }

    /**
     * Retrieves the appropriate shared state version for the event.
     *
     * @param sharedStateManager A [SharedStateManager] to version the event.
     * @param event An [Event] which may contain a specific event from which the correct shared state can be retrieved
     * @return Int denoting the version number
     */
    private fun resolveSharedStateVersion(
        sharedStateManager: SharedStateManager,
        event: Event?,
    ): Int {
        // 1) If event is not null, pull the version number from internal map
        // 2) If event is null, start with version 0 if shared state is empty.
        //    We start with '0' because extensions can call createSharedState() to export initial state
        //    before handling any event and other extensions should be able to read this state.
        var version = 0
        if (event != null) {
            version = getEventNumber(event) ?: 0
        } else if (!sharedStateManager.isEmpty()) {
            version = lastEventNumber.incrementAndGet()
        }

        return version
    }

    /**
     * Dispatch shared state update event for the [sharedStateType] and [extensionName]
     * @param sharedStateType The type of shared state set
     * @param extensionName Extension whose shared state was updated
     */
    private fun dispatchSharedStateEvent(sharedStateType: SharedStateType, extensionName: String) {
        val eventName =
            if (sharedStateType == SharedStateType.STANDARD) EventHubConstants.STATE_CHANGE else EventHubConstants.XDM_STATE_CHANGE
        val data =
            mapOf(EventHubConstants.EventDataKeys.Configuration.EVENT_STATE_OWNER to extensionName)

        val event = Event.Builder(eventName, EventType.TYPE_HUB, EventSource.TYPE_SHARED_STATE)
            .setEventData(data).build()
        dispatchInternal(event)
    }

    private fun shareEventHubSharedState() {
        if (!hubStarted) return

        val extensionsInfo = mutableMapOf<String, Any?>()
        registeredExtensions.values.forEach {
            val extensionName = it.sharedStateName
            if (extensionName != null && extensionName != EventHubConstants.NAME) {
                val extensionInfo = mutableMapOf<String, Any?>(
                    EventHubConstants.EventDataKeys.FRIENDLY_NAME to it.friendlyName,
                    EventHubConstants.EventDataKeys.VERSION to it.version
                )
                it.metadata?.let { metadata ->
                    extensionInfo[EventHubConstants.EventDataKeys.METADATA] = metadata
                }

                extensionsInfo[extensionName] = extensionInfo
            }
        }

        val wrapperInfo = mapOf(
            EventHubConstants.EventDataKeys.TYPE to _wrapperType.wrapperTag,
            EventHubConstants.EventDataKeys.FRIENDLY_NAME to _wrapperType.friendlyName
        )

        val data = mapOf(
            EventHubConstants.EventDataKeys.VERSION to EventHubConstants.VERSION_NUMBER,
            EventHubConstants.EventDataKeys.WRAPPER to wrapperInfo,
            EventHubConstants.EventDataKeys.EXTENSIONS to extensionsInfo
        )

        createSharedStateInternal(
            SharedStateType.STANDARD,
            EventHubConstants.NAME,
            EventDataUtils.clone(data),
            null
        )
    }
}

private fun <T> MutableCollection<T>.filterRemove(predicate: (T) -> Boolean): MutableCollection<T> {
    val ret = mutableListOf<T>()
    this.removeAll {
        if (predicate(it)) {
            ret.add(it)
            true
        } else {
            false
        }
    }
    return ret
}
