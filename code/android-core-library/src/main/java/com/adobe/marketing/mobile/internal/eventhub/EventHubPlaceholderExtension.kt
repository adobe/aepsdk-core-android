package com.adobe.marketing.mobile.internal.eventhub

import com.adobe.marketing.mobile.Extension
import com.adobe.marketing.mobile.ExtensionApi

/**
 * An `Extension` for `EventHub`. This serves no purpose other than to allow `EventHub` to store share state and manage event listeners.
 */

internal class EventHubPlaceholderExtension(val extensionApi: ExtensionApi): Extension(extensionApi) {
    override fun getName() = EventHubConstants.NAME
    override fun getFriendlyName() = EventHubConstants.FRIENDLY_NAME
    override fun getVersion() = EventHubConstants.VERSION_NUMBER
}

