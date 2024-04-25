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

import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.EventHistoryRequest
import com.adobe.marketing.mobile.EventHistoryResultHandler
import com.adobe.marketing.mobile.internal.CoreConstants
import com.adobe.marketing.mobile.internal.util.convertMapToFnv1aHash
import com.adobe.marketing.mobile.services.Log
import java.util.concurrent.Executors
import kotlin.math.max

/**
 * The Android implementation of [EventHistory] which provides functionality for performing
 * database operations on an [AndroidEventHistoryDatabase].
 */
internal class AndroidEventHistory : EventHistory {
    private val androidEventHistoryDatabase = AndroidEventHistoryDatabase()
    companion object {
        private const val LOG_TAG = "AndroidEventHistory"
    }

    /**
     * Responsible for holding a single thread executor for lazy initialization only if
     * AndroidEventHistory operations are used.
     */
    private val executor by lazy { Executors.newSingleThreadExecutor() }

    /**
     * Record an event in the [AndroidEventHistoryDatabase].
     *
     * @param event the [Event] to be recorded
     * @param handler [EventHistoryResultHandler] a callback which will contain a `boolean` indicating if the database operation was successful
     */
    override fun recordEvent(event: Event, handler: EventHistoryResultHandler<Boolean>?) {
        executor.submit {
            val fnv1aHash = convertMapToFnv1aHash(event.eventData, event.mask)
            Log.debug(
                CoreConstants.LOG_TAG,
                LOG_TAG,
                "%s hash($fnv1aHash) for Event(${event.uniqueIdentifier})",
                if (fnv1aHash == 0L) "Not Recording" else "Recording"
            )
            val res = if (fnv1aHash != 0L) {
                androidEventHistoryDatabase.insert(fnv1aHash, event.timestamp)
            } else {
                false
            }
            notifyHandler(handler, res)
        }
    }

    /**
     * Query the [AndroidEventHistoryDatabase] for [Event]s which match the contents of
     * the [EventHistoryRequest] array.
     *
     * @param eventHistoryRequests an array of `EventHistoryRequest`s to be matched
     * @param enforceOrder `boolean` if true, consecutive lookups will use the oldest
     * timestamp from the previous event as their from date
     * @param handler If `enforceOrder` is false, `EventHistoryResultHandler<Integer>` containing the the total number of
     * matching events in the `EventHistoryDatabase`. If `enforceOrder` is true, the handler will contain a "1" if the event history requests
     * were found in the order specified in the eventHistoryRequests array and a "0" if the events were not found in the order specified.
     * The handler will contain a "-1" if the database failure occurred.
     */
    override fun getEvents(
        eventHistoryRequests: Array<out EventHistoryRequest>,
        enforceOrder: Boolean,
        handler: EventHistoryResultHandler<Int>
    ) {
        executor.submit {
            var dbError = false
            var count = 0
            var latestEventOccurrence: Long? = null
            eventHistoryRequests.forEachIndexed { index, request ->
                val eventHash = request.maskAsDecimalHash
                val adjustedFromDate = if (enforceOrder) request.adjustedFromDate(latestEventOccurrence) else request.fromDate
                val res = androidEventHistoryDatabase.query(eventHash, adjustedFromDate, request.adjustedToDate)

                Log.debug(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "EventHistoryRequest[%d] - (%d of %d) for hash(%d)" +
                        " with enforceOrder(%s) returned %d events",
                    eventHistoryRequests.hashCode(),
                    index + 1,
                    eventHistoryRequests.size,
                    eventHash,
                    if (enforceOrder) "true" else "false",
                    res?.count ?: -1
                )

                if (res == null) {
                    dbError = true
                    return@forEachIndexed
                }

                if (enforceOrder && res.count == 0) {
                    return@forEachIndexed
                }

                latestEventOccurrence = res.newestTimeStamp
                count += res.count
            }

            val result = when {
                dbError -> -1
                enforceOrder && count == eventHistoryRequests.size -> 1
                enforceOrder && count != eventHistoryRequests.size -> 0
                else -> count
            }
            notifyHandler(handler, result)
        }
    }

    /**
     * Delete rows from the [AndroidEventHistoryDatabase] that contain [Event]s which
     * match the contents of the [EventHistoryRequest] array.
     *
     * @param eventHistoryRequests an array of `EventHistoryRequest`s to be deleted
     * @param handler a callback which will be called with a `int` containing the total number
     * of rows deleted from the `AndroidEventHistoryDatabase`
     */
    override fun deleteEvents(
        eventHistoryRequests: Array<out EventHistoryRequest>,
        handler: EventHistoryResultHandler<Int>?
    ) {
        executor.submit {
            val deletedRows = eventHistoryRequests.fold(0) { acc, request ->
                acc + androidEventHistoryDatabase.delete(request.maskAsDecimalHash, request.fromDate, request.adjustedToDate)
            }
            notifyHandler(handler, deletedRows)
        }
    }

    private fun <T> notifyHandler(handler: EventHistoryResultHandler<T>?, value: T) {
        try {
            handler?.call(value)
        } catch (ex: Exception) {
            Log.debug(
                CoreConstants.LOG_TAG,
                LOG_TAG,
                "Exception executing event history result handler $ex"
            )
        }
    }
}

private val EventHistoryRequest.adjustedToDate
    get() = if (this.toDate == 0L) { System.currentTimeMillis() } else { this.toDate }

private fun EventHistoryRequest.adjustedFromDate(latestEventOccurrence: Long?): Long {
    if (latestEventOccurrence == null) {
        return this.fromDate
    }

    return max(latestEventOccurrence, this.fromDate)
}
