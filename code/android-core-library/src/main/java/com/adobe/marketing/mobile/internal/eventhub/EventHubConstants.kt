package com.adobe.marketing.mobile.internal.eventhub

internal object EventHubConstants {
    const val NAME = "com.adobe.module.eventhub"
    const val FRIENDLY_NAME = "EventHub"
    const val VERSION_NUMBER = "2.0.0"

    object EventDataKeys {
        const val VERSION = "version"
        const val EXTENSIONS = "extensions"
        const val WRAPPER = "wrapper"
        const val TYPE = "type"
        const val METADATA = "metadata"
        const val FRIENDLY_NAME = "friendlyName"
    }
}