package com.adobe.marketing.mobile.internal.eventhub

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

object EventHubManager {
    // Store for tenant-specific EventHub instances
    private val tenantStore: ConcurrentHashMap<Tenant, EventHub> = ConcurrentHashMap(
        mapOf(Tenant() to EventHub())
    )

    private val accessLock = ReentrantLock()

    // Create or retrieve an EventHub instance for a specific tenant
    internal fun createInstance(tenant: Tenant): EventHub {
//        return accessLock.withLock {
           return tenantStore[tenant] ?: return EventHub(tenant).also { tenantStore[tenant] = it }
//        }
    }

    // Retrieve an EventHub instance for a specific tenant or return the default instance
    internal fun instance(tenant: Tenant): EventHub? {
//        return accessLock.withLock {
           return tenantStore[tenant]
//        }
    }
}

