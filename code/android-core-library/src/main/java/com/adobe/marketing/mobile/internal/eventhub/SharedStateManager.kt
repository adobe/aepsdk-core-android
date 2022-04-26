package com.adobe.marketing.mobile.internal.eventhub

import android.util.LruCache
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.MobileCore
import java.util.TreeMap

/**
 * Responsible for managing the shared state operations for an extension via [ExtensionRuntime].
 * Employs a red-black tree to store the state data and their versions while also using a cache
 * to make a O(1) best effort retrieval.
 * It is intentionally agnostic of the type ([SharedStateType]) of the state it deals with.
 * The knowledge of whether or not a state is pending is deferred to the caller to ensure this class
 * is decoupled from the rules for a pending state.
 *
 * Note that the methods in this class fall on the public ExtensionApi path and, changes to method
 * behaviors may impact the shared state API behavior.
 */
internal class SharedStateManager {

    /**
     * A mapping between the version of the state to the state.
     */
    private val states: TreeMap<Int, SharedState> = TreeMap<Int, SharedState>()

    /**
     * Responsible for caching items that are most recently used. Useful for making
     * a best attempt O(1) retrieval.
     */
    private val cache: LruCache<Int, SharedState> = LruCache<Int, SharedState>(10)

    companion object {
        const val LOG_TAG = "SharedStateManager"
        const val VERSION_LATEST: Int = Int.MAX_VALUE
    }

    /**
     * Records the shared state for the extension at [version] as [data] if it does not already exist.
     *
     * @param [data] the content that the shared state needs to be populated with
     * @param [version] the version of the shared state to be created
     * @param [isPending] a boolean to indicate if the state content is not final (i.e will be updated later)
     * @return true - if a new shared state has been created at [version], false otherwise
     */
    @Synchronized
    fun createSharedState(
        data: Map<String, Any?>?,
        version: Int,
        isPending: Boolean
    ): Boolean {

        // Check if there exists a state at a version equal to, or higher than the one provided.
        if (states.ceilingEntry(version) != null) {
            MobileCore.log(LoggingMode.VERBOSE, LOG_TAG, String.format("Cannot create state st version %d. More recent state exists.", version))
            // If such a state exists a new state cannot be created, it can be updated, if pending,
            // via SharedStateManager#updateSharedState(..)
            return false
        }

        // At this point, there does not exist a state at the provided version. Create one and add it to cache
        // TODO: USE EventDataUtils.cloneMap to do an immutable clone when available
        val sharedState = SharedState(data?.toMap(), version, isPending)
        states[version] = sharedState
        cache.put(version, sharedState)

        // Only update the VERSION_LATEST to the cache. Not to the state store!
        cache.put(VERSION_LATEST, sharedState)

        return true
    }

    /**
     * Updates a previously existing pending shared state for the extension at
     * [version] as [data]. If such a pending state does not exists, no operation is done.
     *
     * @param [data] the content that the shared state needs to be updated with
     * @param [version] the version of the pending shared state to be updated
     * @param [isPending] a boolean to indicate if the new state content is not final
     * @return true - if a new shared state has been created at [version], false otherwise
     */
    @Synchronized
    fun updateSharedState(
        data: Map<String, Any?>?,
        version: Int,
        isPending: Boolean
    ): Boolean {

        // Check if new state is pending. A state cannot be overwritten by another pending state
        if (isPending) {
            MobileCore.log(LoggingMode.VERBOSE, LOG_TAG,
                    String.format("Cannot update pending state at version %d. With a pending state.", version))
            return false
        }

        // Check if state exists at the exact version provided for updating.
        val stateAtVersion = states[version] ?: return false

        // Check there is a valid pending state for updating.
        if (!stateAtVersion.isPending) {
            MobileCore.log(LoggingMode.WARNING, LOG_TAG,
                    String.format("Cannot update a non pending state state version %d.", version))
            return false
        }

        // At this point, there exists a previously recorded state at the version provided.
        // Overwrite its value with a confirmed state.
        // TODO: USE EventDataUtils.cloneMap to do an immutable clone when available
        val sharedState = SharedState(data?.toMap(), version, false)
        states[version] = sharedState
        cache.put(version, sharedState)

        // There is no need to update the VERSION_LATEST in the cache because update operation
        // should only happen on a state that exists in the tree. If we reach a point where
        // VERSION_LATEST should be updated, it means it is already in the cache

        return true
    }

    /**
     * Retrieves data for shared state at [version]. If such a version does not exist,
     * retrieves the most recent version of the shared state available.
     *
     * @param [version] the version of the shared state to be retrieved
     * @return shared state at [version] if it exists, or the most recent shared state before [version].
     *         null - If no state at or before [version] is found, or if the state fetched above is pending.
     */
    @Synchronized
    fun getSharedState(version: Int): Map<String, Any?>? {

        if (states.isEmpty()) {
            // No states have been added to the state store yet.
            return null
        }

        // Check if a state exists exactly at the version specified.
        // Find cache first to get in O(1)
        val stateAtVersion = cache.get(version) ?: states[version]

        if (stateAtVersion != null) {
            return if (stateAtVersion.isPending) {
                // State is yet to be set (is pending)
                null
            } else {
                // If shared state at exact version exists and it is not pending, use it
                stateAtVersion.data
            }
        }

        // Otherwise, find state at the highest version less than the version being queried for
        val resolvedSharedState: SharedState? = states.floorEntry(version)?.value

        // If the resolved state is not set or is pending, return null. Otherwise return the state
        return if (resolvedSharedState == null || resolvedSharedState.isPending) {
            null
        } else {
            cache.put(resolvedSharedState.version, resolvedSharedState)
            resolvedSharedState.data
        }
    }

    /**
     * Removes all the states being tracked from [states] and [cache]
     */
    @Synchronized
    fun clearSharedState() {
        states.clear()
        cache.evictAll()
    }
}

/**
 * Internal representation of a shared event state.
 * Allows associating version and pending behavior with the state data in a queryable way.
 */
private data class SharedState constructor(val data: Map<String, Any?>?, val version: Int, val isPending: Boolean)

/**
 * Represents the types of shared state that are supported.
 */
internal enum class SharedStateType {
    STANDARD,
    XDM
}
