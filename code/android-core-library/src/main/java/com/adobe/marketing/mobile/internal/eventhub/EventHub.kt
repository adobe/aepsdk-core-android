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

import com.adobe.marketing.mobile.Extension
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.MobileCore
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * EventHub class is responsible for delivering events to listeners and maintaining registered extension's lifecycle.
 */
internal class EventHub {
    constructor()

    companion object {
        val LOG_TAG = "EventHub"
        public var shared = EventHub()
    }

    private val eventHubExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }
    private val registeredExtensions: ConcurrentHashMap<String, ExtensionContainer> = ConcurrentHashMap()
    private var hubStarted = false

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
            val container = ExtensionContainer(extensionClass, executor, completion)
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
}

// / Helper to get extension type name
private val Class<out Extension>.extensionTypeName
    get() = this.name
