/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.internal.eventhub

import com.adobe.marketing.mobile.SharedStateResult
import com.adobe.marketing.mobile.SharedStateStatus
import com.adobe.marketing.mobile.internal.CoreConstants
import com.adobe.marketing.mobile.services.Log
import java.util.TreeMap

/**
 * Internal representation of a shared event state.
 * Allows associating version and pending behavior with the state data in a queryable way.
 */
private data class SharedState constructor(val version: Int, val status: SharedStateStatus, val data: Map<String, Any?>?) {
    fun getResult(): SharedStateResult = SharedStateResult(status, data)
}

/**
 * Represents the types of shared state that are supported.
 */
internal enum class SharedStateType {
    STANDARD,
    XDM
}

/**
 * Responsible for managing the shared state operations for an extension.
 * Employs a red-black tree to store the state data and their versions
 * The knowledge of whether or not a state is pending is deferred to the caller to ensure this class
 * is decoupled from the rules for a pending state.
 *
 * Note that the methods in this class fall on the public ExtensionApi path and, changes to method
 * behaviors may impact the shared state API behavior.
 */
internal class SharedStateManager(private val name: String) {

    private val LOG_TAG = "SharedStateManager($name)"

    /**
     * A mapping between the version of the state to the state.
     */
    private val states: TreeMap<Int, SharedState> = TreeMap<Int, SharedState>()

    companion object {
        const val VERSION_LATEST: Int = Int.MAX_VALUE
    }

    /**
     * Sets the shared state for the extension at [version] as [data] if it does not already exist.
     *
     * @param version the version of the shared state to be created
     * @param data the content that the shared state needs to be populated with
     * @return true - if a new shared state has been created at [version]
     */
    @Synchronized
    fun setState(version: Int, data: Map<String, Any?>?): Boolean {
        return set(version, SharedState(version, SharedStateStatus.SET, data))
    }

    /**
     * Sets the pending shared state for the extension at [version] if it does not already exist.
     *
     * @param version the version of the shared state to be created
     * @return true - if a new pending shared state has been created at [version]
     */
    @Synchronized
    fun setPendingState(version: Int): Boolean {
        return set(version, SharedState(version, SharedStateStatus.PENDING, resolve(VERSION_LATEST).value))
    }

    /**
     * Updates the pending shared state for the extension at [version] to contain [data]
     *
     * @param version the version of the shared state to be created
     * @param data the content that the shared state needs to be populated with
     * @return true - if the pending shared state is updated at [version] to contain [data]
     */
    @Synchronized
    fun updatePendingState(version: Int, data: Map<String, Any?>?): Boolean {
        val stateAtVersion = states[version] ?: return false
        if (stateAtVersion.status != SharedStateStatus.PENDING) {
            return false
        }

        // At this point, there exists a previously recorded state at the version provided.
        // Overwrite its value with a confirmed state.
        states[version] = SharedState(version, SharedStateStatus.SET, data)
        return true
    }

    /**
     * Retrieves data for shared state at [version]. If such a version does not exist,
     * retrieves the most recent version of the shared state available.
     *
     * @param version the version of the shared state to be retrieved
     * @return shared state at [version] if it exists, or the most recent shared state before [version] if
     *         shared state at [version] does not exist,
     *         null - If no state at or before [version] is found
     */
    @Synchronized
    fun resolve(version: Int): SharedStateResult {
        // Return first state equal to or less than version
        val resolvedState = states.floorEntry(version)?.value
        if (resolvedState != null) {
            return resolvedState.getResult()
        }

        // If not return the lowest shared state or null if empty
        return states.firstEntry()?.value?.getResult() ?: SharedStateResult(SharedStateStatus.NONE, null)
    }

    /**
     * Retrieves data for shared state at [version]. If such a version does not exist,
     * retrieves the most recent version of the shared state available.
     *
     * @param version the version of the shared state to be retrieved
     * @return shared state at [version] if it exists, or the most recent shared state before [version] if
     *         shared state at [version] does not exist,
     *         null - If no state at or before [version] is found
     */
    @Synchronized
    fun resolveLastSet(version: Int): SharedStateResult {
        // Return the first non pending state equal to or less than version
        states.descendingMap().tailMap(version).forEach {
            val state = it.value
            if (state.status != SharedStateStatus.PENDING) {
                return state.getResult()
            }
        }

        // If not return the lowest shared state if it is non pending or null otherwise
        val lowestState = states.firstEntry()?.value
        return if (lowestState?.status == SharedStateStatus.SET) {
            lowestState.getResult()
        } else {
            SharedStateResult(SharedStateStatus.NONE, null)
        }
    }

    /**
     * Removes all the states being tracked from [states]
     */
    @Synchronized
    fun clear() {
        states.clear()
    }

    /**
     * Checks if the [SharedStateManager] is empty.
     */
    @Synchronized
    fun isEmpty(): Boolean {
        return states.size == 0
    }

    /**
     * Sets the shared state at [version] as [state] if it does not already exist.
     *
     * @param version the version of the shared state to be created
     * @param state the [SharedState] object
     * @return true - if a new shared state has been created at [version]
     */
    private fun set(version: Int, state: SharedState): Boolean {
        // Check if there exists a state at a version equal to, or higher than the one provided.
        if (states.ceilingEntry(version) != null) {
            Log.trace(
                CoreConstants.LOG_TAG,
                LOG_TAG,
                "Cannot create $name shared state at version $version. " +
                    "More recent state exists."
            )
            return false
        }

        // At this point, there does not exist a state at the provided version.
        states[version] = state
        return true
    }
}
