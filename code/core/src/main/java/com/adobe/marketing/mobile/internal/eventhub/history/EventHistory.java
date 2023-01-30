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

package com.adobe.marketing.mobile.internal.eventhub.history;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.EventHistoryRequest;
import com.adobe.marketing.mobile.EventHistoryResultHandler;

/** Defines an interface for performing database operations on an {@link EventHistoryDatabase}. */
public interface EventHistory {
    /**
     * Record an event in the {@link EventHistoryDatabase}.
     *
     * @param event the {@link Event} to be recorded
     * @param handler {@link EventHistoryResultHandler} a callback which will contain a {@code
     *     boolean} indicating if the database operation was successful
     */
    void recordEvent(Event event, EventHistoryResultHandler<Boolean> handler);

    /**
     * Query the {@link EventHistoryDatabase} for {@link Event}s which match the contents of the
     * {@link EventHistoryRequest} array.
     *
     * @param eventHistoryRequests an array of {@code EventHistoryRequest}s to be matched
     * @param enforceOrder {@code boolean} if true, consecutive lookups will use the oldest
     *     timestamp from the previous event as their from date
     * @param handler {@code EventHistoryResultHandler<Integer>} containing the the total number of
     *     matching events in the {@code EventHistoryDatabase} if an "any" search was done. If an
     *     "ordered" search was done, the handler will contain a "1" if the event history requests
     *     were found in the order specified in the eventHistoryRequests array and a "0" if the
     *     events were not found in the order specified.
     */
    void getEvents(
            EventHistoryRequest[] eventHistoryRequests,
            boolean enforceOrder,
            EventHistoryResultHandler<Integer> handler);

    /**
     * Delete rows from the {@link EventHistoryDatabase} that contain {@link Event}s which match the
     * contents of the {@link EventHistoryRequest} array.
     *
     * @param eventHistoryRequests an array of {@code EventHistoryRequest}s to be deleted
     * @param handler a callback which will be called with a {@code int} containing the total number
     *     of rows deleted from the {@code EventHistoryDatabase}
     */
    void deleteEvents(
            EventHistoryRequest[] eventHistoryRequests, EventHistoryResultHandler<Integer> handler);
}
