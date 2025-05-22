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

import com.adobe.marketing.mobile.AdobeCallbackWithError
import com.adobe.marketing.mobile.AdobeError
import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.EventHistoryRequest
import com.adobe.marketing.mobile.EventHistoryResult
import com.adobe.marketing.mobile.internal.CoreConstants
import com.adobe.marketing.mobile.internal.eventhub.history.EventHistoryConstants.EVENT_HISTORY_ERROR
import com.adobe.marketing.mobile.internal.util.convertMapToFnv1aHash
import com.adobe.marketing.mobile.services.Log
import java.util.concurrent.Executors
import kotlin.math.max

/**
 * The Android implementation of [EventHistory] which provides functionality for performing
 * database operations on an [AndroidEventHistoryDatabase].
 */
internal class AndroidEventHistory(
    private val androidEventHistoryDatabase: AndroidEventHistoryDatabase = AndroidEventHistoryDatabase()
) : EventHistory {
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
     * @param callback whose call method will be called with a `boolean` indicating if the database operation was successful
     * or `fail` if the database failure occurred
     */
    override fun recordEvent(event: Event, callback: AdobeCallbackWithError<Boolean>?) {
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
            notifyHandler(callback, res, !res)
        }
    }

    /**
     * Query the [AndroidEventHistoryDatabase] for [Event]s which match the contents of
     * the [EventHistoryRequest] array.
     *
     * @param eventHistoryRequests an array of `EventHistoryRequest`s to be matched
     * @param enforceOrder `boolean` if true, consecutive lookups will use the oldest
     * timestamp from the previous event as their from date
     * @param callback whose `call` method will be called with an array of [EventHistoryResult], one for each provided request,
     * containing the the total number of matching events in the [AndroidEventHistoryDatabase] along with the timestamp of the oldest and newest of the event
     * or `fail` if the database failure occurred
     */
    override fun getEvents(
        eventHistoryRequests: Array<out EventHistoryRequest>,
        enforceOrder: Boolean,
        callback: AdobeCallbackWithError<Array<EventHistoryResult>>
    ) {
        executor.submit {
            var dbError = false
            val results = mutableListOf<EventHistoryResult>()
            var previousEventOldestOccurrence: Long? = null
            eventHistoryRequests.forEachIndexed { index, request ->
                val eventHash = request.maskAsDecimalHash
                val adjustedFromDate = if (enforceOrder) request.adjustedFromDate(previousEventOldestOccurrence) else request.fromDate
                val res = androidEventHistoryDatabase.query(eventHash, adjustedFromDate, request.adjustedToDate)

                Log.debug(
                    CoreConstants.LOG_TAG,
                    LOG_TAG,
                    "EventHistoryRequest[%d] - (%d of %d) for hash(%d) from %d to %d" +
                        " with enforceOrder (%s) returned %d records",
                    eventHistoryRequests.hashCode(),
                    index + 1,
                    eventHistoryRequests.size,
                    eventHash,
                    adjustedFromDate,
                    request.adjustedToDate,
                    if (enforceOrder) "true" else "false",
                    res.count
                )

                if (enforceOrder) {
                    if (res.count == EVENT_HISTORY_ERROR) {
                        dbError = true
                    }
                    previousEventOldestOccurrence = res.oldestOccurrence
                }
                results.add(res)
            }
            notifyHandler(callback, results.toTypedArray(), dbError)
        }
    }

    /**
     * Delete rows from the [AndroidEventHistoryDatabase] that contain [Event]s which
     * match the contents of the [EventHistoryRequest] array.
     *
     * @param eventHistoryRequests an array of `EventHistoryRequest`s to be deleted
     * @param callback whose `call` method will be called with a `int` containing the total number
     * of rows deleted from the [AndroidEventHistoryDatabase] or `fail` if the database failure occurred
     */
    override fun deleteEvents(
        eventHistoryRequests: Array<out EventHistoryRequest>,
        callback: AdobeCallbackWithError<Int>?
    ) {
        var dbError = false
        executor.submit {
            val deletedRows = eventHistoryRequests.fold(0) { acc, request ->
                val noOfDeletedRows = androidEventHistoryDatabase.delete(request.maskAsDecimalHash, request.fromDate, request.adjustedToDate)
                if (noOfDeletedRows == EVENT_HISTORY_ERROR) {
                    dbError = true
                    acc
                } else {
                    acc + noOfDeletedRows
                }
            }
            notifyHandler(callback, deletedRows, dbError)
        }
    }

    private fun <T> notifyHandler(handler: AdobeCallbackWithError<T>?, value: T, dbError: Boolean) {
        try {
            if (dbError) {
                handler?.fail(AdobeError.DATABASE_ERROR)
            } else {
                handler?.call(value)
            }
        } catch (ex: Exception) {
            Log.debug(
                CoreConstants.LOG_TAG,
                LOG_TAG,
                "Exception executing event history AdobeCallbackWithError $ex"
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
