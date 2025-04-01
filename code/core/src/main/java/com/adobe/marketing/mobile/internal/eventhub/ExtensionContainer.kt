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
import com.adobe.marketing.mobile.EventHistoryRequest
import com.adobe.marketing.mobile.EventHistoryResultHandler
import com.adobe.marketing.mobile.Extension
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.ExtensionEventListener
import com.adobe.marketing.mobile.SharedStateResolution
import com.adobe.marketing.mobile.SharedStateResolver
import com.adobe.marketing.mobile.SharedStateResult
import com.adobe.marketing.mobile.internal.CoreConstants
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.util.SerialWorkDispatcher
import java.util.concurrent.ConcurrentLinkedQueue

internal class ExtensionContainer constructor(
    private val extensionClass: Class<out Extension>,
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

    var metadata: Map<String, String>? = null
        private set

    var lastProcessedEvent: Event? = null
        private set

    var extension: Extension? = null
        private set

    private var sharedStateManagers: Map<SharedStateType, SharedStateManager>? = null
    private val eventListeners: ConcurrentLinkedQueue<ExtensionListenerContainer> =
        ConcurrentLinkedQueue()

    /**
     * Implementation of [SerialWorkDispatcher.WorkHandler] that is responsible for dispatching
     * an [Event] "e". Dispatch is regarded complete when [SerialWorkDispatcher.WorkHandler.doWork] finishes for "e".
     */
    private val dispatchJob: SerialWorkDispatcher.WorkHandler<Event> =
        SerialWorkDispatcher.WorkHandler { event ->
            if (extension?.readyForEvent(event) != true) {
                return@WorkHandler false
            }

            eventListeners.forEach {
                if (it.shouldNotify(event)) {
                    it.notify(event)
                }
            }

            lastProcessedEvent = event
            return@WorkHandler true
        }

    private val initJob = Runnable {
        val extension = extensionClass.initWith(this)
        if (extension == null) {
            callback(EventHubError.ExtensionInitializationFailure)
            return@Runnable
        }

        val extensionName = extension.extensionName
        if (extensionName.isNullOrBlank()) {
            callback(EventHubError.InvalidExtensionName)
            return@Runnable
        }

        this.extension = extension
        sharedStateName = extensionName
        friendlyName = extension.extensionFriendlyName
        version = extension.extensionVersion
        metadata = extension.extensionMetadata

        sharedStateManagers = mapOf(
            SharedStateType.XDM to SharedStateManager(extensionName),
            SharedStateType.STANDARD to SharedStateManager(extensionName)
        )

        Log.debug(
            CoreConstants.LOG_TAG,
            getTag(),
            "Extension registered"
        )

        callback(EventHubError.None)

        // Notify that the extension is registered
        extension.onExtensionRegistered()
    }

    private val teardownJob = Runnable {
        extension?.onExtensionUnregistered()
        Log.debug(
            CoreConstants.LOG_TAG,
            getTag(),
            "Extension unregistered"
        )
    }

    val eventProcessor: SerialWorkDispatcher<Event> =
        SerialWorkDispatcher(extensionClass.extensionTypeName, dispatchJob)

    init {

        eventProcessor.setInitialJob(initJob)
        eventProcessor.setFinalJob(teardownJob)
        eventProcessor.start()
    }

    fun shutdown() {
        eventProcessor.shutdown()
    }

    /**
     * Returns instance of [SharedStateManager] for [SharedStateType]
     */
    fun getSharedStateManager(type: SharedStateType): SharedStateManager? {
        return sharedStateManagers?.get(type)
    }

    private fun getTag(): String {
        if (extension == null) {
            return LOG_TAG
        }

        return "ExtensionContainer[$sharedStateName($version)]"
    }

    // Override ExtensionApi Methods
    override fun registerEventListener(
        eventType: String,
        eventSource: String,
        eventListener: ExtensionEventListener
    ) {
        eventListeners.add(ExtensionListenerContainer(eventType, eventSource, eventListener))
    }

    override fun dispatch(
        event: Event
    ) {
        EventHub.shared.dispatch(event)
    }

    override fun startEvents() {
        eventProcessor.resume()
    }

    override fun stopEvents() {
        eventProcessor.pause()
    }

    override fun createSharedState(
        state: MutableMap<String, Any?>,
        event: Event?
    ) {
        val sharedStateName = this.sharedStateName ?: run {
            Log.warning(
                CoreConstants.LOG_TAG,
                getTag(),
                "ExtensionContainer is not fully initialized. createSharedState should not be called from Extension constructor"
            )
            return
        }

        EventHub.shared.createSharedState(
            SharedStateType.STANDARD,
            sharedStateName,
            state,
            event
        )
    }

    override fun createPendingSharedState(
        event: Event?
    ): SharedStateResolver? {
        val sharedStateName = this.sharedStateName ?: run {
            Log.warning(
                CoreConstants.LOG_TAG,
                getTag(),
                "ExtensionContainer is not fully initialized. createPendingSharedState should not be called from 'Extension' constructor"
            )
            return null
        }

        return EventHub.shared.createPendingSharedState(
            SharedStateType.STANDARD,
            sharedStateName,
            event
        )
    }

    override fun getSharedState(
        extensionName: String,
        event: Event?,
        barrier: Boolean,
        resolution: SharedStateResolution
    ): SharedStateResult? {
        return EventHub.shared.getSharedState(
            SharedStateType.STANDARD,
            extensionName,
            event,
            barrier,
            resolution
        )
    }

    override fun createXDMSharedState(
        state: MutableMap<String, Any?>,
        event: Event?
    ) {
        val sharedStateName = this.sharedStateName ?: run {
            Log.warning(
                CoreConstants.LOG_TAG,
                getTag(),
                "ExtensionContainer is not fully initialized. createXDMSharedState should not be called from Extension constructor"
            )
            return
        }

        EventHub.shared.createSharedState(SharedStateType.XDM, sharedStateName, state, event)
    }

    override fun createPendingXDMSharedState(
        event: Event?
    ): SharedStateResolver? {
        val sharedStateName = this.sharedStateName ?: run {
            Log.warning(
                CoreConstants.LOG_TAG,
                getTag(),
                "ExtensionContainer is not fully initialized. createPendingXDMSharedState should not be called from 'Extension' constructor"
            )
            return null
        }

        return EventHub.shared.createPendingSharedState(SharedStateType.XDM, sharedStateName, event)
    }

    override fun getXDMSharedState(
        extensionName: String,
        event: Event?,
        barrier: Boolean,
        resolution: SharedStateResolution
    ): SharedStateResult? {
        return EventHub.shared.getSharedState(
            SharedStateType.XDM,
            extensionName,
            event,
            barrier,
            resolution
        )
    }

    override fun unregisterExtension() {
        EventHub.shared.unregisterExtension(extensionClass) {}
    }

    override fun getHistoricalEvents(
        eventHistoryRequests: Array<out EventHistoryRequest>,
        enforceOrder: Boolean,
        handler: EventHistoryResultHandler<Int>
    ) {
        EventHub.shared.eventHistory?.getEvents(eventHistoryRequests, enforceOrder, handler)
    }

    override fun recordHistoricalEvent(event: Event, handler: EventHistoryResultHandler<Boolean>) {
        EventHub.shared.eventHistory?.recordEvent(event, handler)
    }
}
