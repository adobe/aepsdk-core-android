package com.adobe.marketing.mobile.internal.eventhub


sealed class Tenant(val id: String) {
    data class Id(val tenantId: String) : Tenant(id = tenantId)
    object Default : Tenant(id = "default")
}