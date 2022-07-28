package com.adobe.marketing.mobile.internal.eventhub

import com.adobe.marketing.mobile.AdobeCallback
import com.adobe.marketing.mobile.Extension
import com.adobe.marketing.mobile.ExtensionError
import com.adobe.marketing.mobile.ExtensionErrorCallback

// To prevent multiple shared state updates from eventhub, this class makes sure EventHub.start() is called only after all the extensions registered before MobileCore.start() is completed.
internal class EventHubExtensionRegistarar {
    companion object {
        var shared = EventHubExtensionRegistarar()
    }

    private val mutex = Any()
    // Boolean to prevent start() API called multiple times.
    private var startCalled = false
    private var startCompletionCallback: AdobeCallback<*>? = null
    private var pendingHubStart = false
    private val extensionsRegisteredBeforeStart = mutableListOf<Class<out Extension>>()

    fun registerExtension(
        extensionClass: Class<out Extension>,
        errorCallback: ExtensionErrorCallback<ExtensionError>?,
    ): Boolean {
        synchronized(mutex) {
            if (!startCalled) {
                extensionsRegisteredBeforeStart.add(extensionClass)
            }
        }

        EventHub.shared.registerExtension(extensionClass) { e: EventHubError ->
            val extensionError = when (e) {
                EventHubError.None -> null
                EventHubError.InvalidExtensionName -> ExtensionError.BAD_NAME
                EventHubError.DuplicateExtensionName -> ExtensionError.DUPLICATE_NAME
                else -> ExtensionError.UNEXPECTED_ERROR
            }

            extensionError?.let {
                errorCallback?.error(it)
            }

            var callback: AdobeCallback<*>? = null
            synchronized(mutex) {
                extensionsRegisteredBeforeStart.remove(extensionClass)
                if (checkAndStartHub()) {
                    // Call outside the mutex.
                    callback = startCompletionCallback
                    clearPendingState()
                }
            }

            callback?.call(null)
        }
        return true
    }

    fun start(completionCallback: AdobeCallback<*>?) {
        var callback: AdobeCallback<*>? = null
        synchronized(mutex) {
            if (startCalled) {
                return
            }

            startCalled = true
            pendingHubStart = true
            startCompletionCallback = completionCallback
            if (checkAndStartHub()) {
                // Call completion callback outside lock.
                callback = startCompletionCallback
                clearPendingState()
            }
        }

        callback?.call(null)
    }

    private fun checkAndStartHub(): Boolean {
        val shouldStart = (pendingHubStart && extensionsRegisteredBeforeStart.size == 0)
        if (shouldStart) {
            EventHub.shared.start()
        }
        return shouldStart
    }

    private fun clearPendingState() {
        pendingHubStart = false
        startCompletionCallback = null
    }
}
