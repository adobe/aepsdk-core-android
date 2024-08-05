package com.adobe.marketing.mobile.internal.eventhub


data class Tenant(
    val id: String? = null
) {
    companion object {
        const val DEFAULT_TENANT = "default"
    }
}
