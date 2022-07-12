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
import com.adobe.marketing.mobile.EventSource
import com.adobe.marketing.mobile.EventType
import com.adobe.marketing.mobile.Extension
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.ExtensionError
import com.adobe.marketing.mobile.ExtensionErrorCallback
import com.adobe.marketing.mobile.ExtensionEventListener
import com.adobe.marketing.mobile.ExtensionListener
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.SharedStateResolution
import com.adobe.marketing.mobile.SharedStateResolver
import com.adobe.marketing.mobile.SharedStateResult
import com.adobe.marketing.mobile.util.SerialWorkDispatcher
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ExecutorService
import kotlin.Exception

internal class ExtensionContainer constructor(
    private val extensionClass: Class<out Extension>,
    private val taskExecutor: ExecutorService,
    callback: (EventHubError) -> Unit
) : ExtensionApi() {

    companion object {
        const val LOG_TAG = "ExtensionContainer"
    }

    var sharedStateName: String? = null
        private set

    var friendlyName: String? = null
        private set

    var version: String? = null
        private set

    var lastProcessedEvent: Event? = null
        private set

    var extension: Extension? = null
        private set

    private var sharedStateManagers: Map<SharedStateType, SharedStateManager>? = null
    private val eventListeners: ConcurrentLinkedQueue<ExtensionListenerContainer> = ConcurrentLinkedQueue()

    /**
     * Implementation of [SerialWorkDispatcher.WorkHandler] that is responsible for dispatching
     * an [Event] "e". Dispatch is regarded complete when [SerialWorkDispatcher.WorkHandler.doWork] finishes for "e".
     */
    private val dispatchJob: SerialWorkDispatcher.WorkHandler<Event> = SerialWorkDispatcher.WorkHandler { event ->
        if (extension?.readyForEvent(event) != true) {
            return@WorkHandler
        }

        eventListeners.forEach {
            if (it.shouldNotify(event)) {
                it.notify(event)
            }
        }

        lastProcessedEvent = event
    }

    val eventProcessor: SerialWorkDispatcher<Event> = SerialWorkDispatcher(extensionClass.extensionTypeName, dispatchJob)

    init {
        taskExecutor.submit {
            val extension = extensionClass.initWith(this)
            if (extension == null) {
                callback(EventHubError.ExtensionInitializationFailure)
                return@submit
            }

            val extensionName = extension.extensionName
            if (extensionName.isNullOrBlank()) {
                callback(EventHubError.InvalidExtensionName)
                return@submit
            }

            this.extension = extension
            sharedStateName = extensionName
            friendlyName = extension.extensionFriendlyName
            version = extension.extensionVersion
            sharedStateManagers = mapOf(
                SharedStateType.XDM to SharedStateManager(extensionName),
                SharedStateType.STANDARD to SharedStateManager(extensionName)
            )
            eventProcessor.start()
            callback(EventHubError.None)

            // Notify that the extension is registered
            extension.onExtensionRegistered()
        }
    }

    fun shutdown() {
        taskExecutor.run {
            eventProcessor.shutdown()
            extension?.onExtensionUnregistered()
        }
    }

    /**
     * Sets the shared state for the extension at [version] as [data] and type [sharedStateType]
     * If [data] is null, the shared state being set is regarded as pending.
     * If a pending shared state at [version] already exists, an attempt will be made to update it.
     *
     * @param sharedStateType the type of the shared state that need to be set
     * @param data the content that the shared state needs to be populated with
     * @param version the version of the shared state to be set
     * @return [SharedState.Status.SET] if a new shared state has been created or updated at [version],
     *         [SharedState.Status.PENDING] if the shared state is set to pending,
     *         [SharedState.Status.NOT_SET] if the shared state was not set.
     */
    fun setSharedState(
        sharedStateType: SharedStateType,
        data: MutableMap<String, Any?>?,
        version: Int
    ): SharedState.Status {
        return taskExecutor.submit(
            Callable<SharedState.Status> {
                val stateManager: SharedStateManager = sharedStateManagers?.get(sharedStateType)
                    ?: return@Callable SharedState.Status.NOT_SET

                // Existing public API infers a pending state as one with no data
                val isPending: Boolean = (data == null)

                // Attempt to create the state first
                val createResult: SharedState.Status = stateManager.createSharedState(data, version, isPending)

                if (createResult != SharedState.Status.NOT_SET) {
                    // If the creation was successful i.e the result of the operation was either SET or
                    // PENDING, return the result.
                    return@Callable createResult
                } else {
                    // else, attempt to update it and return the update result.
                    return@Callable stateManager.updateSharedState(data, version, isPending)
                }
            }
        ).get()
    }

    /**
     * Clears the shares states of type [sharedStateType] for this extension.
     * @param sharedStateType the type of shared state that needs to be cleared
     *
     * @return false if an exception occurs clearing the state or if the extension is unregistered,
     *         true otherwise.
     */
    fun clearSharedState(sharedStateType: SharedStateType): Boolean {
        if (taskExecutor.isShutdown) return false

        return taskExecutor.submit(
            Callable<Boolean> {
                sharedStateManagers?.get(sharedStateType)?.clearSharedState()
                return@Callable true
            }
        ).get()
    }

    /**
     * Gets the shared state of type [sharedStateType] at [version] or the most recent one before [version]
     * if it is unavailable.
     *
     * @param sharedStateType the type of the shared state that need to be retrieved
     * @param version the version of the pending shared state to be retrieved
     * @return [SharedState] at [version] or the most recent shared state before [version] if state at [version] does not exist.
     *         null - if no state at or before [version] is found or, if the extension is unregistered
     */
    fun getSharedState(
        sharedStateType: SharedStateType,
        version: Int
    ): SharedState? {
        if (taskExecutor.isShutdown) return null

        return taskExecutor.submit(
            Callable {
                return@Callable sharedStateManagers?.get(sharedStateType)?.getSharedState(version)
            }
        ).get()
    }

    private fun getTag(): String {
        if (extension == null) {
            return LOG_TAG
        }
        return "$sharedStateName-$version"
    }

    // Override ExtensionApi Methods
    override fun registerEventListener(
        eventType: String?,
        eventSource: String?,
        eventListener: ExtensionEventListener?,
        errorCallback: ExtensionErrorCallback<ExtensionError>?
    ): Boolean {

        if (eventType == null) {
            errorCallback?.error(ExtensionError.EVENT_TYPE_NOT_SUPPORTED)
            return false
        }

        if (eventSource == null) {
            errorCallback?.error(ExtensionError.EVENT_SOURCE_NOT_SUPPORTED)
            return false
        }

        if (eventListener == null) {
            errorCallback?.error(ExtensionError.CALLBACK_NULL)
            return false
        }

        eventListeners.add(ExtensionListenerContainer(eventType, eventSource, eventListener))
        return true
    }

    override fun createSharedState(
        state: MutableMap<String, Any>?,
        event: Event?,
        errorCallback: ExtensionErrorCallback<ExtensionError>?,
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun createPendingSharedState(
        event: Event?,
        errorCallback: ExtensionErrorCallback<ExtensionError>?,
    ): SharedStateResolver? {
        TODO("Not yet implemented")
    }

    override fun getSharedState(
        extensionName: String?,
        event: Event?,
        barrier: Boolean,
        resolution: SharedStateResolution?,
        errorCallback: ExtensionErrorCallback<ExtensionError>?
    ): SharedStateResult {
        TODO("Not yet implemented")
    }

    override fun clearSharedEventStates(errorCallback: ExtensionErrorCallback<ExtensionError>?): Boolean {
        TODO("Not yet implemented")
    }

    override fun createXDMSharedState(
        state: MutableMap<String, Any>?,
        event: Event?,
        errorCallback: ExtensionErrorCallback<ExtensionError>?,
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun createPendingXDMSharedState(
        event: Event?,
        errorCallback: ExtensionErrorCallback<ExtensionError>?,
    ): SharedStateResolver? {
        TODO("Not yet implemented")
    }

    override fun getXDMSharedState(
        extensionName: String?,
        event: Event?,
        barrier: Boolean,
        resolution: SharedStateResolution?,
        errorCallback: ExtensionErrorCallback<ExtensionError>?
    ): SharedStateResult {
        TODO("Not yet implemented")
    }

    override fun clearXDMSharedEventStates(errorCallback: ExtensionErrorCallback<ExtensionError>?): Boolean {
        TODO("Not yet implemented")
    }

    override fun unregisterExtension() {
        TODO("Not yet implemented")
    }

    // Deprecated ExtensionApi methods
    override fun setSharedEventState(
        state: MutableMap<String, Any?>?,
        event: Event?,
        errorCallback: ExtensionErrorCallback<ExtensionError>?,
    ): Boolean {
        try {
            return EventHub.shared.setSharedState(SharedStateType.STANDARD, sharedStateName, state, event, errorCallback)
        } catch (exception: Exception) {
            MobileCore.log(
                LoggingMode.ERROR, getTag(),
                "Failed to set shared state at EventID: ${event?.uniqueIdentifier}. $exception"
            )
            errorCallback?.error(ExtensionError.UNEXPECTED_ERROR)
        }

        return false
    }

    override fun setXDMSharedEventState(
        state: MutableMap<String, Any?>?,
        event: Event?,
        errorCallback: ExtensionErrorCallback<ExtensionError>?,
    ): Boolean {
        try {
            return EventHub.shared.setSharedState(SharedStateType.XDM, sharedStateName, state, event, errorCallback)
        } catch (exception: Exception) {
            MobileCore.log(
                LoggingMode.ERROR, getTag(),
                "Failed to set shared state at EventID: ${event?.uniqueIdentifier}. $exception"
            )
            errorCallback?.error(ExtensionError.UNEXPECTED_ERROR)
        }

        return false
    }

    override fun getSharedEventState(
        stateName: String?,
        event: Event?,
        errorCallback: ExtensionErrorCallback<ExtensionError>?,
    ): MutableMap<String, Any> {
        TODO("Not yet implemented")
    }

    override fun getXDMSharedEventState(
        stateName: String?,
        event: Event?,
        errorCallback: ExtensionErrorCallback<ExtensionError>?,
    ): MutableMap<String, Any> {
        TODO("Not yet implemented")
    }

    override fun <T : ExtensionListener> registerEventListener(
        eventType: String?,
        eventSource: String?,
        extensionListenerClass: Class<T>?,
        errorCallback: ExtensionErrorCallback<ExtensionError>?,
    ): Boolean {
        val extensionListener = extensionListenerClass?.initWith(this, eventType, eventSource)
        if (extensionListener == null) {
            errorCallback?.error(ExtensionError.UNEXPECTED_ERROR)
            return false
        }
        return registerEventListener(eventType, eventSource, { extensionListener.hear(it) }, errorCallback)
    }

    override fun <T : ExtensionListener> registerWildcardListener(
        extensionListenerClass: Class<T>?,
        errorCallback: ExtensionErrorCallback<ExtensionError>?,
    ): Boolean {
        val extensionListener = extensionListenerClass?.initWith(this, EventType.TYPE_WILDCARD, EventSource.TYPE_WILDCARD)
        if (extensionListener == null) {
            errorCallback?.error(ExtensionError.UNEXPECTED_ERROR)
            return false
        }
        return registerEventListener(EventType.TYPE_WILDCARD, EventSource.TYPE_WILDCARD, { extensionListener.hear(it) }, errorCallback)
    }
}
