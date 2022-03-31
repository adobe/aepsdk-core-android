package com.adobe.marketing.mobile.internal.eventhub

import com.adobe.marketing.mobile.*
import java.util.concurrent.ExecutorService

internal class ExtensionRuntime(): ExtensionApi() {
    var extension: Extension? = null
        set(value) {
            field = value
            extensionName = value?.name
            extensionFriendlyName = value?.friendlyName
            extensionVersion = value?.version
        }

    // Fetch these values on initialization
    var extensionName: String? = null
        private set
    var extensionFriendlyName: String? = null
        private set
    var extensionVersion: String? = null
        private set

    override fun <T : ExtensionListener?> registerEventListener(
        eventType: String?,
        eventSource: String?,
        extensionListenerClass: Class<T>?,
        errorCallback: ExtensionErrorCallback<ExtensionError>?
    ): Boolean {
        return false
    }

    override fun <T : ExtensionListener?> registerWildcardListener(
        extensionListenerClass: Class<T>?,
        errorCallback: ExtensionErrorCallback<ExtensionError>?
    ): Boolean {
        return false
    }

    override fun setSharedEventState(
        state: MutableMap<String, Any>?,
        event: Event?,
        errorCallback: ExtensionErrorCallback<ExtensionError>?
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun setXDMSharedEventState(
        state: MutableMap<String, Any>?,
        event: Event?,
        errorCallback: ExtensionErrorCallback<ExtensionError>?
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun clearSharedEventStates(errorCallback: ExtensionErrorCallback<ExtensionError>?): Boolean {
        TODO("Not yet implemented")
    }

    override fun clearXDMSharedEventStates(errorCallback: ExtensionErrorCallback<ExtensionError>?): Boolean {
        TODO("Not yet implemented")
    }

    override fun getSharedEventState(
        stateName: String?,
        event: Event?,
        errorCallback: ExtensionErrorCallback<ExtensionError>?
    ): MutableMap<String, Any> {
        TODO("Not yet implemented")
    }

    override fun getXDMSharedEventState(
        stateName: String?,
        event: Event?,
        errorCallback: ExtensionErrorCallback<ExtensionError>?
    ): MutableMap<String, Any> {
        TODO("Not yet implemented")
    }

    override fun unregisterExtension() {
        if (extension == null) {
            return
        }
        EventHub.shared.unregisterExtension(extension?.javaClass) {}
    }
}

internal class ExtensionContainer constructor(
    private val extensionClass: Class<out Extension>,
    private val taskExecutor: ExecutorService,
    callback: (EventHubError) -> Unit
) {

    private val extensionRuntime = ExtensionRuntime()

    val sharedStateName: String?
        get() = extensionRuntime.extensionName

    val friendlyName: String?
        get() = extensionRuntime.extensionFriendlyName

    val version: String?
        get() = extensionRuntime.extensionVersion

    init {
        taskExecutor.submit {
            val extension = extensionClass.initWith(extensionRuntime)
            if (extension == null) {
                callback(EventHubError.extensionInitializationFailure)
                return@submit
            }

            if (extension.name == null) {
                callback(EventHubError.invalidExtensionName)
                return@submit
            }

            // We set this circular reference because ExtensionApi exposes an API to get the underlying extension.
            extensionRuntime.extension = extension
            callback(EventHubError.none)
        }
    }

    fun shutdown() {
        taskExecutor.run {
            extensionRuntime.extension?.onUnregistered()
        }
        taskExecutor.shutdown()
    }
}
