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

import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.Extension
import com.adobe.marketing.mobile.ExtensionError
import com.adobe.marketing.mobile.ExtensionErrorCallback
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.MobileCore
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

/**
 * EventHub class is responsible for delivering events to listeners and maintaining registered extension's lifecycle.
 */
internal class EventHub {
    constructor()

    companion object {
        const val LOG_TAG = "EventHub"
        public var shared = EventHub()
    }

    private val eventHubExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }
    private val registeredExtensions: ConcurrentHashMap<String, ExtensionContainer> = ConcurrentHashMap()
    private val lastEventNumber: AtomicInteger = AtomicInteger(0)
    private var hubStarted = false

    /**
     * A cache that maps UUID of an Event to an internal sequence of its dispatch.
     */
    private val eventNumberMap: ConcurrentHashMap<String, Int> = ConcurrentHashMap<String, Int>()

    init {
        registerExtension(EventHubPlaceholderExtension::class.java) {}
    }

    /**
     * `EventHub` will begin processing `Event`s when this API is invoked.
     */
    fun start() {
        eventHubExecutor.submit {
            this.hubStarted = true

            this.shareEventHubSharedState()
            MobileCore.log(LoggingMode.DEBUG, LOG_TAG, "Event Hub successfully started")
        }
    }

    /**
     * Registers a new `Extension` to the `EventHub`. This `Extension` must extends `Extension` class
     *
     * @property extensionClass The class of extension to register
     * @property completion Invoked when the extension has been registered or failed to register
     */
    fun registerExtension(extensionClass: Class<out Extension>?, completion: (error: EventHubError) -> Unit) {
        eventHubExecutor.submit {
            if (extensionClass == null) {
                completion(EventHubError.ExtensionInitializationFailure)
                return@submit
            }

            val extensionName = extensionClass.extensionTypeName
            if (registeredExtensions.containsKey(extensionName)) {
                completion(EventHubError.DuplicateExtensionName)
                return@submit
            }

            val executor = Executors.newSingleThreadExecutor()
            val stateManagers: Map<SharedStateType, SharedStateManager> = mapOf(
                    SharedStateType.STANDARD to SharedStateManager(),
                    SharedStateType.XDM to SharedStateManager()
            )

            val container = ExtensionContainer(extensionClass, ExtensionRuntime(), stateManagers, executor, completion)
            registeredExtensions[extensionName] = container
        }
    }

    /**
     * Unregisters the extension from the `EventHub` if registered
     * @property extensionClass The class of extension to unregister
     * @property completion Invoked when the extension has been unregistered or failed to unregister
     */
    fun unregisterExtension(extensionClass: Class<out Extension>?, completion: ((error: EventHubError) -> Unit)) {
        eventHubExecutor.submit {
            val extensionName = extensionClass?.extensionTypeName
            val container = registeredExtensions.remove(extensionName)
            if (container != null) {
                container?.shutdown()
                shareEventHubSharedState()
                completion(EventHubError.None)
            } else {
                completion(EventHubError.ExtensionNotRegistered)
            }
        }
    }

    /**
     * Sets the shared state for the extension - [extensionName] with [data]
     *
     * @param sharedStateType the type of shared state that needs to be set.
     * @param extensionName the name of the extension for which the state is being set
     * @param data a map representing state of [extensionName] extension. Passing null will set the extension's
     *              shared state on pending until it is resolved. Another call with non-null state is expected in order
     *              to resolve this share state.
     * @param event The [Event] for which the state is being set. Passing null will set the state for the next shared
     *              state version.
     * @param errorCallback the callback which will be notified in the event of an error
     *
     * @return true if the state was successfully set, false otherwise
     */
    fun setSharedState(
        sharedStateType: SharedStateType,
        extensionName: String?,
        data: MutableMap<String, Any?>?,
        event: Event?,
        errorCallback: ExtensionErrorCallback<ExtensionError>?
    ): Boolean {

        val setSharedStateCallable: Callable<Boolean> = Callable<Boolean> {

            if (extensionName.isNullOrEmpty() || extensionName.isBlank()) {
                MobileCore.log(LoggingMode.ERROR, LOG_TAG,
                        String.format("Unable to set SharedState for extension: [%s]. ExtensionName is invalid.", extensionName))

                errorCallback?.error(ExtensionError.BAD_NAME)
                return@Callable false
            }

            val extensionContainer: ExtensionContainer? = registeredExtensions[extensionName]

            if (extensionContainer == null) {
                MobileCore.log(LoggingMode.ERROR, LOG_TAG,
                        String.format("Error seting SharedState for extension: [%s]. Extension may not have been registered.", extensionName))

                errorCallback?.error(ExtensionError.UNEXPECTED_ERROR)
                return@Callable false
            }

            // Find the version where this state needs to be set
            val version: Int = if (event == null) {
                // Use the next available version if event is null
                lastEventNumber.incrementAndGet()
            } else {
                // Fetch the event number for the event if it has been dispatched.
                // If no such event exists, use the next available sequence number
                getEventNumber(event) ?: lastEventNumber.incrementAndGet()
            }

            val wasSet: Boolean = extensionContainer.setSharedState(sharedStateType, data, version)

            // Check if the new state can be dispatched as a state change event(currently implies a
            // non null/non pending state according to the ExtensionAPI)
            val shouldDispatch: Boolean = (data == null)

            if (shouldDispatch && wasSet) {
                // If the new state can be dispatched and was successfully
                // set (via a new state being created or a state being updated),
                // dispatch a shared state notification.
                //  TODO: dispatch()
            }
            return@Callable wasSet
        }

        return eventHubExecutor.submit(setSharedStateCallable).get()
    }

    /**
     * Retrieves the shared state for the extension [extensionName] at the [event]
     *
     * @param sharedStateType the type of shared state that needs to be retrieved.
     * @param extensionName the name of the extension for which the state is being retrieved
     * @param event The [Event] for which the state is being retrieved. Passing null will retrieve latest state available.
     *              state version.
     * @param errorCallback the callback which will be notified in the event of an error
     * @return a [Map] containing the shared state data at [event],
     *         null if the state is pending, not yet set or, in case of an error
     */
    fun getSharedState(
        sharedStateType: SharedStateType,
        extensionName: String?,
        event: Event?,
        errorCallback: ExtensionErrorCallback<ExtensionError>?
    ): Map<String, Any?>? {

        val getSharedStateCallable: Callable<Map<String, Any?>?> = Callable {
            if (extensionName.isNullOrEmpty() || extensionName.isBlank()) {
                MobileCore.log(LoggingMode.ERROR, LOG_TAG, String.format("Unable to get SharedState. State name [%s] is invalid.", extensionName))

                errorCallback?.error(ExtensionError.BAD_NAME)
                return@Callable null
            }

            val extensionContainer: ExtensionContainer? = registeredExtensions[extensionName]

            if (extensionContainer == null) {
                MobileCore.log(LoggingMode.ERROR, LOG_TAG,
                        String.format("Error retrieving SharedState for extension: [%s]." +
                        "Extension may not have been registered.", extensionName))
                errorCallback?.error(ExtensionError.UNEXPECTED_ERROR)
                return@Callable null
            }

            val version: Int = if (event == null) {
                // Get the most recent number if event is not specified
                SharedStateManager.VERSION_LATEST
            } else {
                // Fetch event number from the provided event.
                // If not such event was dispatched, return the most recent state.
                getEventNumber(event) ?: SharedStateManager.VERSION_LATEST
            }

            return@Callable extensionContainer.getSharedState(sharedStateType, version)
        }

        return eventHubExecutor.submit(getSharedStateCallable).get()
    }

    /**
     * Clears all shared state previously set by [extensionName].
     *
     * @param sharedStateType the type of shared state that needs to be cleared.
     * @param extensionName the name of the extension for which the state is being cleared
     * @param errorCallback the callback which will be notified in the event of an error
     */
    fun clearSharedState(
        sharedStateType: SharedStateType,
        extensionName: String?,
        errorCallback: ExtensionErrorCallback<ExtensionError>?
    ): Boolean {
        val clearSharedStateCallable: Callable<Boolean> = Callable {
            if (extensionName.isNullOrEmpty() || extensionName.isBlank()) {
                MobileCore.log(LoggingMode.ERROR, LOG_TAG, String.format("Unable to clear SharedState. State name [%s] is invalid.", extensionName))

                errorCallback?.error(ExtensionError.BAD_NAME)
                return@Callable null
            }

            val extensionContainer: ExtensionContainer? = registeredExtensions[extensionName]

            if (extensionContainer == null) {
                MobileCore.log(LoggingMode.ERROR,
                        LOG_TAG,
                        String.format("Error clearing SharedState for extension: [%s]. Extension may not have been registered."))
                errorCallback?.error(ExtensionError.UNEXPECTED_ERROR)
                return@Callable false
            }

            return@Callable extensionContainer.clearSharedState(sharedStateType)
        }

        return eventHubExecutor.submit(clearSharedStateCallable).get()
    }

    /**
     * Stops processing events and shuts down all registered extensions.
     */
    fun shutdown() {
        // Todo : Stop event processing

        // Shutdown and clear all the extensions.
        eventHubExecutor.submit {
            // Unregister all extensions
            registeredExtensions.forEach { (_, extensionContainer) ->
                extensionContainer.shutdown()
            }
            registeredExtensions.clear()
        }
        eventHubExecutor.shutdown()
    }

    private fun shareEventHubSharedState() {
        if (!hubStarted) return
        // Update shared state with registered extensions
    }

    /**
     * Retrieve the event number for the Event from the [eventNumberMap]
     *
     * @param [event] the Event for which the event number should be resolved
     * @return the event number for the event if it exists (if it has been recorded/dispatched),
     *         null otherwise
     */
    private fun getEventNumber(event: Event?): Int? {
        val eventUUID = event?.uniqueIdentifier
        return eventNumberMap[eventUUID]
    }
}

/**
 * Helper to get extension type name
 */
private val Class<out Extension>.extensionTypeName
    get() = this.name
