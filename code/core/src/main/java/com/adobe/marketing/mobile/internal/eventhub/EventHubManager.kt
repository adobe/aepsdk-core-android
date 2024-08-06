package com.adobe.marketing.mobile.internal.eventhub

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock

object EventHubManager {
    // Store for tenant-specific EventHub instances
    private val tenantStore: ConcurrentHashMap<String, EventHub> = ConcurrentHashMap(
        mapOf(Tenant.Default.id to EventHub())
    )

    private val accessLock = ReentrantLock()

    // Create or retrieve an EventHub instance for a specific tenant
    internal fun createInstance(tenant: Tenant): EventHub {
//        return accessLock.withLock {


        tenant.id.let {
            return tenantStore[it] ?: return EventHub(tenant).also { hub ->
                tenantStore[it] = hub
            }
        }


//        }
    }

    // Retrieve an EventHub instance for a specific tenant or return the default instance
    internal fun instance(tenant: Tenant): EventHub? {
//        return accessLock.withLock {
        return tenantStore[tenant.id] ?: tenantStore[Tenant.Default.id]
//        }
    }
}

