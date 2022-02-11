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
package com.adobe.marketing.mobile;

import java.util.Map;
import java.util.TreeMap;

/**
 * Provides a mechanism for resolving an object across a range of versions
 * <p>
 * States will always be one of the following:
 * <ul>
 * <li>A {@code DATA} state is a normal, valid shared state.</li>
 * <li>The {@code PENDING} state is a state that is "on the way" and will eventually be resolved.</li>
 * <li>The {@code INVALID} state is a special state that indicates that the state is not valid.</li>
 * <li>The {@code NEXT} state is a special "marker" state that indicates that this state is equal to the next {@code DATA}/{@code PENDING}/{@code INVALID} state.</li>
 * <li>The {@code PREV} state is a special "marker" state that indicates that this state is equal to the previous state.</li>
 * </ul>
 * <p>
 * Modules will be able to perform the following operations:
 * <ul>
 *  <li>Create can insert a {@code DATA}, {@code PENDING}, or {@code INVALID} state.</li>
 *  <li>Update can change a {@code PENDING} state to a...
 *   <ul>
 *    <li>{@code DATA} state upon successful asynchronous operations.<li>
 *    <li>{@code INVALID} state for asynchronous operations that invalidate the shared state.<li>
 *    <li>{@code NEXT} state for asynchronous operations that should return the next state.<li>
 *    <li>{@code PREV} state for asynchronous operations that should revert to the previous state.<li>
 *    </ul>
 *   </li>
 *  <li>Get for a version v should...
 *   <ul>
 *     <li>If the state at version <em>v</em> is {@code DATA}, {@code PENDING}, or {@code INVALID}, return it.</li>
 *     <li>If the state at version <em>v</em> is {@code NEXT}, return the first state after <em>v</em> that is either {@code DATA}, {@code PENDING}, or {@code INVALID}.
 *      <ul>
 *       <li>If there are no such states after <em>v</em>, return {@code PENDING}.</li>
 *      </ul>
 *     </li>
 *     <li>If the state at version <em>v</em> is {@code PREV}, return Get(<em>v_prev</em>) where <em>v_prev</em> is the version of the first state with version &lt; <em>v</em> that is either {@code DATA}, {@code PENDING}, {@code INVALID}, or {@code NEXT}.
 *      <ul>
 *       <li>If there are no such states before <em>v</em>, return {@code PENDING}.</li>
 *      </ul>
 *     </li>
 *     <li>If no state has exactly version <em>v</em>, return Get(<em>v_prev</em>) where <em>v_prev</em> is the version of the first state with version &lt; <em>v</em>.
 *      <ul>
 *       <li>If there are no such states before <em>v</em>, return {@code PENDING}.</li>
 *      </ul>
 *     </li>
 *   </ul>
 *  </li>
 * </ul>
 * <p>
 * Notice that:
 *  <ul>
 *   <li>Get(<em>v</em>) will always return {@code DATA}, {@code PENDING}, or {@code INVALID}.</li>
 *   <li>Only {@code PENDING} states can be updated.</li>
 *   <li>Once a state is {@code DATA} or {@code INVALID}, Gets for that state will always return the same value. This prevents history from changing.</li>
 *  </ul>
 * <p>
 * Under this solution, asynchronous operations would typically follow this process:
 * <ul>
 * <li>Create(version, {@code PENDING})</li>
 * <li>Initiate the operation</li>
 * <li>When the operation completes,
 *  <ul>
 *    <li>If the operation succeeds, Update(version, &lt;the new shared state&gt;)</li>
 *    <li>Else (the operation failed):
 *      <ul>
 *       <li>If state should use the previous valid shared state, Update(version, {@code PREV})</li>
 *       <li>If state should use the next valid shared state, Update(version, {@code NEXT})</li>
 *       <li>If state should be invalid, Update(version, {@code INVALID})</li>
 *      </ul>
 *    </li>
 *   </ul>
 *   </li>
 * </ul>
 *
 * @author Adobe Systems Incorporated
 * @version 5.0
 **/
class RangedResolver<T> {
	private TreeMap<Integer, T> states_;

	// state markers
	private T PENDING; // state that is "on the way" and will eventually be resolved.
	private T INVALID; // special "marker" state that indicates that this state is equal to the previous state.
	private T NEXT; // special "marker" state that indicates that this state is equal to the next DATA/PENDING/INVALID state.
	private T PREV; // special state that indicates that the state is not valid.

	/**
	 * Creates a new Ranged Resolver instance.
	 *
	 * Creates a new RangedResolver instance using the given special marker objects. The special state markers
	 * must be unique with each other and should be unique within your system.
	 *
	 * @param pendingState a unique marker object indicating a state will eventuall be resolved
	 * @param invalidState a unique marker object indication a state is not valid
	 * @param nextState a unique marker object indicating the state is equal to the next valid state
	 * @param prevState a unique marker object indicating the state is equal to the previous valid state
	 */
	RangedResolver(final T pendingState, final T invalidState, final T nextState, final T prevState) {

		this.PENDING = pendingState;
		this.INVALID = invalidState;
		this.NEXT = nextState;
		this.PREV = prevState;

		boolean found_equality = false;
		found_equality |= (PENDING == INVALID);
		found_equality |= (PENDING == NEXT);
		found_equality |= (PENDING == PREV);
		found_equality |= (INVALID == NEXT);
		found_equality |= (INVALID == PREV);
		found_equality |= (PREV == NEXT);

		if (found_equality) {
			Log.warning("RangedResolver", "Found equality between marker states! Pending(%x) Invalid(%x) Next(%x) Previous(%x)",
						System.identityHashCode(PENDING),
						System.identityHashCode(INVALID),
						System.identityHashCode(NEXT),
						System.identityHashCode(PREV));
		}

		this.states_ = new TreeMap<Integer, T>();
		this.states_.put(-1, NEXT);
	}

	/**
	 * Add new state for a specific version.
	 *
	 * Adds a new shared state to the list for the given version.
	 * Only adds the state if the version is greater than the previous state's version.
	 * Only adds the state if it is equal to DATA, PENDING, or INVALID.
	 *
	 * @param version the version of this state to add
	 * @param object the state to add
	 * @return true if the state was added
	 */
	boolean add(final int version, final T object) {
		// only add states of DATA, PENDING, or INVALID
		if (NEXT == object || PREV == object) {
			return false;
		}

		synchronized (this) {
			if (version > states_.lastKey()) {
				states_.put(version, object);
				return true;
			}
		}

		return false;
	}

	/**
	 * Update an existing state.
	 *
	 * Update an existing PENDING state at the specified version.
	 * NOTE: Only existing PENDING states may be updated.
	 *
	 * @param version the version of the state to update
	 * @param object the state to replace the existing state
	 * @return true if the state was updated
	 */
	boolean update(final int version, final T object) {
		// Update may only update PENDING states.
		// Return false for updating PENDING with PENDING so it does not trigger a state change event.
		if (object == PENDING) {
			return false;
		}

		synchronized (this) {
			// as the collection allows null values, need to check if it contains the key first
			if (states_.containsKey(version) && states_.get(version) == PENDING) {
				states_.put(version, object);
				return true;
			}
		}

		return false;
	}

	/**
	 * Resolves the given version to a shared state.
	 *
	 * Resolves the given version by traversing the list of shared states, returning the appropriate state.
	 * <ul>
	 *  <li>If the state at version <em>v</em> is DATA, PENDING, or INVALID, return it.</li>
	 *  <li>If the state at version <em>v</em> is NEXT, return the first state after <em>v</em> that is either DATA, PENDING, or INVALID.
	 *   <ul>
	 *       <li>If there are no such states after <em>v</em>, return PENDING.</li>
	 *   </ul>
	 *  </li>
	 *  <li>If the state at version <em>v</em> is PREV, return Get(<em>v_prev</em>) where <em>v_prev</em> is the version of the first state with version &lt; <em>v</em> that is either DATA, PENDING, INVALID, or NEXT.
	 *   <ul>
	 *       <li>If there are no such states before <em>v</em>, return PENDING.</li>
	 *   </ul>
	 *  </li>
	 *  <li> If no state has exactly version <em>v</em>, return Get(<em>v_prev</em>) where <em>v_prev</em> is the version of the first state with version &lt; <em>v</em>.
	 *      <ul>
	 *          <li>If there are no such states before <em>v</em>, return PENDING.</li>
	 *      </ul>
	 *  </li>
	 * </ul>
	 *
	 * @param version the version to resolve to a state
	 * @return a state of either EventData, RangedResolver::PENDING, or RangedResolver::INVALID
	 */
	synchronized T resolve(final int version) {
		int i = version;

		if (i < 0) { // range check here, return oldest entry if requesting lower than valid version
			i = 0;
		}

		Map.Entry<Integer, T> entry = states_.floorEntry(i); // greatest key less than or equal to 'version'

		if (entry == null) { // found no key for or less than version
			// this should not occur as the map is initialized with a starting value
			return PENDING;
		}

		return walkStates(entry);
	}

	/**
	 * Determines if there are any valid states contained within this {@code RangedResolver}.
	 * A valid state is considered to be any value other than {@code INVALID}, {@code NEXT}, or {@code PREV}.
	 * A {@code PENDING} state is considered valid as it is the expectation of data.
	 *
	 * @return true if this {@link RangedResolver} contains any value which is not
	 * {@code INVALID}, {@code NEXT}, or {@code PREV}
	 */
	synchronized boolean containsValidState() {
		Map.Entry<Integer, T> e = states_.lastEntry();

		while (e.getKey() >= 0) {
			if (e.getValue() != INVALID && e.getValue() != NEXT && e.getValue() != PREV) {
				return true; // state is either DATA or PENDING
			}

			e = states_.lowerEntry(e.getKey());
		}

		return false; // reached beginning, no valid entries
	}

	/**
	 * Iterate over the map of states starting at the given states iterator to find the first valid state.
	 * Returns the state value (DATA, PENDING, or INVALID).
	 * If the state is equal to the end of the states map, returns PENDING.
	 *
	 * @param entry a Map.Entry
	 * @return state value of DATA, PENDING, or INVALID
	 */
	private synchronized T walkStates(final Map.Entry<Integer, T> entry) {
		Map.Entry<Integer, T> e = entry;

		// walk back over state entries
		while (e.getValue() == PREV) {
			e = states_.lowerEntry(e.getKey());
		}

		// after walking back, walk forward and don't look back
		while (e != null && (e.getValue() == NEXT || e.getValue() == PREV)) {
			e = states_.higherEntry(e.getKey());
		}

		// walked off the end
		if (e == null) {
			return PENDING;
		}

		return e.getValue();
	}


}



