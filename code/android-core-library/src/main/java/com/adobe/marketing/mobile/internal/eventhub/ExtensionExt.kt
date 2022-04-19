package com.adobe.marketing.mobile.internal.eventhub

import com.adobe.marketing.mobile.*
import java.lang.Exception

/// Type extensions for [Extension] to allow for easier usage

/// Function to initialize Extension with [ExtensionApi]
internal fun  Class<out Extension>.initWith(extensionApi: ExtensionApi): Extension? {
    try {
        val extensionConstructor = this.getDeclaredConstructor(ExtensionApi::class.java)
        extensionConstructor.setAccessible(true)
        return extensionConstructor.newInstance(extensionApi)
    } catch(ex: Exception) {
        MobileCore.log(LoggingMode.DEBUG,"Extension", "Initializing Extension $this failed with $ex")
    }

    return null
}

/// Property to get Extension name
internal val Extension.name: String?
    get() = ExtensionHelper.getName(this)

/// Property to get Extension version
internal val Extension.version: String?
    get() = ExtensionHelper.getVersion(this)

/// Property to get Extension friendly name
internal val Extension.friendlyName: String?
    get() = ExtensionHelper.getFriendlyName(this)

/// Function to notify that the Extension has been unregistered
internal fun Extension.onUnregistered() {
    ExtensionHelper.onUnregistered(this)
}

/// Type extensions for [ExtensionListener] to allow for easier usage

/// Function to initialize ExtensionListener with [ExtensionApi], type and source.
internal fun Class<out ExtensionListener>.initWith(extensionApi: ExtensionApi, type: String, source: String): ExtensionListener? {
    try {
        val extensionListenerConstructor = this.getDeclaredConstructor(ExtensionApi::class.java, String::class.java, String::class.java)
        extensionListenerConstructor.setAccessible(true)
        return extensionListenerConstructor.newInstance(extensionApi, type, source)
    } catch (ex: Exception) {
        MobileCore.log(LoggingMode.DEBUG,"Extension", "Initializing Extension $this failed with $ex")
    }

    return null
}

