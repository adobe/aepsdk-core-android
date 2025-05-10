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

package com.adobe.marketing.mobile.internal.eventhub.history

import com.adobe.marketing.mobile.AdobeCallback
import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.EventHistoryRequest
import com.adobe.marketing.mobile.EventHistoryResult

/** Defines an interface for performing database operations on an [EventHistoryDatabase].  */
internal interface EventHistory {
    /**
     * Record an event in the [EventHistoryDatabase].
     *
     * @param event the [Event] to be recorded
     * @param handler [AdobeCallback] a callback which will contain a `boolean` indicating if the database operation was successful
     */
    fun recordEvent(event: Event, handler: AdobeCallback<Boolean>?)

    /**
     * Query the [EventHistoryDatabase] for [Event]s which match the contents of the
     * [EventHistoryRequest] array.
     *
     * @param eventHistoryRequests an array of `EventHistoryRequest`s to be matched
     * @param enforceOrder `boolean` if true, consecutive lookups will use the oldest
     * timestamp from the previous event as their from date
     * @param handler a callback which will be called with an array of [EventHistoryResult], one for each provided request,
     * containing the the total number of matching events in the `EventHistoryDatabase` along with the timestamp of the oldest and newest of the event
     * or "-1" if the database failure occurred
     * */
    fun getEvents(
        eventHistoryRequests: Array<out EventHistoryRequest>,
        enforceOrder: Boolean,
        handler: AdobeCallback<Array<EventHistoryResult>>
    )

    /**
     * Delete rows from the [EventHistoryDatabase] that contain [Event]s which match the
     * contents of the [EventHistoryRequest] array.
     *
     * @param eventHistoryRequests an array of `EventHistoryRequest`s to be deleted
     * @param handler a callback which will be called with a `int` containing the total number
     * of rows deleted from the `EventHistoryDatabase`
     */
    fun deleteEvents(
        eventHistoryRequests: Array<out EventHistoryRequest>,
        handler: AdobeCallback<Int>?
    )
}
