package com.adobe.marketing.mobile.internal.eventhub

internal enum class EventHubError {
    invalidExtensionName,
    duplicateExtensionName,
    extensionInitializationFailure,
    extensionNotRegistered,
    unknown,
    none
}