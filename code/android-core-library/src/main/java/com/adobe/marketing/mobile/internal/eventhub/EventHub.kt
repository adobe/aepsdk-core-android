package com.adobe.marketing.mobile.internal.eventhub

import com.adobe.marketing.mobile.*
import java.util.concurrent.*

/**
 * EventHub class is responsible for delivering events to listeners and maintaining registered extension's lifecycle.
 */
internal class EventHub {
    companion object {
        val LOG_TAG  = "EventHub"
        public var shared = EventHub()
    }

    private val eventHubExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }
    private val registeredExtensions: ConcurrentHashMap<String, ExtensionContainer> = ConcurrentHashMap()
    private var hubStarted = false;


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

/// Helper to get extension type name
private val Class<out Extension>.extensionTypeName
    get() = this.name
