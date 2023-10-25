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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.Map;

/**
 * Class that defines all the public methods an {@code Extension} may call to interface with the AEP
 * SDK.
 */
public abstract class ExtensionApi {

    /**
     * Registers a new event listener for current extension for the provided event type and source.
     *
     * @param eventType required parameter, the event type as a valid string (not null or empty)
     * @param eventSource required parameter, the event source as a valid string (not null or empty)
     * @param eventListener required parameter, the listener which extends the {@link
     *     ExtensionEventListener} interface
     */
    public abstract void registerEventListener(
            @NonNull final String eventType,
            @NonNull final String eventSource,
            @NonNull final ExtensionEventListener eventListener);

    /**
     * Dispatches an `Event` to the `EventHub`
     *
     * @param event An Event to be dispatched to the {@code EventHub}
     */
    public abstract void dispatch(@NonNull final Event event);

    /** Starts the `Event` queue for this extension */
    public abstract void startEvents();

    /** Stops the `Event` queue for this extension */
    public abstract void stopEvents();

    // Shared state

    /**
     * Creates a new shared state for this extension. If event is null, one of two behaviors will be
     * observed:
     *
     * <ul>
     *   <li>If this extension has not previously published a shared state, shared state will be
     *       versioned at 0
     *   <li>If this extension has previously published a shared state, shared state will be
     *       versioned at the latest
     * </ul>
     *
     * @param state {@code Map<String, Object>} representing current state of this extension
     * @param event The {@link Event} for which the state is being set. Passing null will set the
     *     state for the next shared state version
     */
    public abstract void createSharedState(
            @NonNull final Map<String, Object> state, @Nullable final Event event);

    /**
     * Creates a pending shared state for this extension.
     *
     * <ul>
     *   <li>If this extension has not previously published a shared state, shared state will be
     *       versioned at 0
     *   <li>If this extension has previously published a shared state, shared state will be
     *       versioned at the latest
     * </ul>
     *
     * @param event The {@link Event} for which pending shared state is being set. Passing null will
     *     set the state for the next shared state version.
     * @return {@link SharedStateResolver} that should be called with the shared state data when it
     *     is ready
     */
    public abstract @Nullable SharedStateResolver createPendingSharedState(
            @Nullable final Event event);

    /**
     * Gets the shared state data for a specified extension.
     *
     * @param extensionName extension name for which to retrieve data. See documentation for the
     *     list of available states.
     * @param event the {@link Event} for which the state is being requested. Passing null will
     *     retrieve latest state available.
     * @param barrier If true, the {@code EventHub} will only return {@code set} if extensionName
     *     has moved past event.
     * @param resolution the {@link SharedStateResolution} to resolve for return {@code
     *     SharedStateResult} for the requested extensionName and event
     */
    public abstract @Nullable SharedStateResult getSharedState(
            @NonNull final String extensionName,
            @Nullable final Event event,
            final boolean barrier,
            @NonNull final SharedStateResolution resolution);

    // XDM Shared state

    /**
     * Creates a new XDM shared state for this extension. The state passed to this API needs to be
     * mapped to known XDM mixins. If an extension uses multiple mixins, the current data for all of
     * them should be provided when the XDM shared state is set. If event is null, one of two
     * behaviors will be observed:
     *
     * <ul>
     *   <li>If this extension has not previously published a shared state, shared state will be
     *       versioned at 0
     *   <li>If this extension has previously published a shared state, shared state will be
     *       versioned at the latest
     * </ul>
     *
     * @param state {@code Map<String, Object>} representing current state of this extension
     * @param event The {@link Event} for which the state is being set. Passing null will set the
     *     state for the next shared state version
     */
    public abstract void createXDMSharedState(
            @NonNull final Map<String, Object> state, @Nullable final Event event);

    /**
     * Creates a pending XDM shared state for this extension.
     *
     * <ul>
     *   <li>If this extension has not previously published a shared state, shared state will be
     *       versioned at 0
     *   <li>If this extension has previously published a shared state, shared state will be
     *       versioned at the latest
     * </ul>
     *
     * @param event The {@link Event} for which pending shared state is being set. Passing null will
     *     set the state for the next shared state version.
     * @return {@link SharedStateResolver} that should be called with the shared state data when it
     *     is ready
     */
    public abstract @Nullable SharedStateResolver createPendingXDMSharedState(
            @Nullable final Event event);

    /**
     * Gets the XDM shared state data for a specified extension. If the stateName extension
     * populates multiple mixins in their shared state, all the data will be returned at once and it
     * can be accessed using path discovery.
     *
     * @param extensionName extension name for which to retrieve data. See documentation for the
     *     list of available states.
     * @param event the {@link Event} for which the state is being requested. Passing null will
     *     retrieve latest state available.
     * @param barrier If true, the {@code EventHub} will only return {@code set} if extensionName
     *     has moved past event.
     * @param resolution the {@link SharedStateResolution} to resolve for return {@code
     *     SharedStateResult} for the requested extensionName and event
     */
    public abstract @Nullable SharedStateResult getXDMSharedState(
            @NonNull final String extensionName,
            @Nullable final Event event,
            final boolean barrier,
            @NonNull final SharedStateResolution resolution);

    /**
     * Unregisters current extension. <br>
     * This method executes asynchronously, unregistering the extension on the event hub thread.
     * {@link Extension#onUnregistered} method will be called at the end of this operation.
     *
     * @see Extension#onUnregistered()
     */
    public abstract void unregisterExtension();

    /**
     * Retrieves a count of historical events matching the provided requests.
     *
     * @param eventHistoryRequests an array of {@link EventHistoryRequest}s used to generate the
     *     hash and timeframe for the event lookup
     * @param enforceOrder if `true`, consecutive lookups will use the oldest timestamp from the
     *     previous event as their from date
     * @param handler the {@link EventHistoryResultHandler} for each provided request
     */
    public abstract void getHistoricalEvents(
            @NonNull EventHistoryRequest[] eventHistoryRequests,
            boolean enforceOrder,
            @NonNull EventHistoryResultHandler<Integer> handler);
}
