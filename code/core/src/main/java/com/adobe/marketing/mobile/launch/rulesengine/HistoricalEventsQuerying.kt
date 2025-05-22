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

package com.adobe.marketing.mobile.launch.rulesengine

import com.adobe.marketing.mobile.AdobeCallbackWithError
import com.adobe.marketing.mobile.AdobeError
import com.adobe.marketing.mobile.EventHistoryRequest
import com.adobe.marketing.mobile.EventHistoryResult
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.internal.eventhub.history.EventHistoryConstants.EVENT_HISTORY_ERROR
import com.adobe.marketing.mobile.internal.eventhub.history.EventHistoryConstants.EVENT_HISTORY_RESULT_NOT_FOUND
import com.adobe.marketing.mobile.services.Log
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

private const val LOG_TAG = "historicalEventsQuerying"
private const val ASYNC_TIMEOUT = 1000L
internal const val SEARCH_TYPE_ORDERED = "ordered"
internal const val SEARCH_TYPE_MOST_RECENT = "mostRecent"

/**
 * Queries the historical events database for the specified events in the requests
 * For an "any" search (event order does not matter), the value returned will be the count of all matching events in EventHistory
 * For an `.ordered` search (events must occur in the provided order), the value returned will be 1 if the events were found in the provided order, or 0 otherwise.
 * If an error occurred, this method will always return -1
 *
 * @param requests `List<EventHistoryRequest>` the list of events to query
 * @param searchType `String` the type of search to perform, either "any" or "ordered"
 * @param extensionApi `ExtensionApi` the extension API to use for querying
 */
@JvmSynthetic
internal fun historicalEventsQuerying(
    requests: List<EventHistoryRequest>,
    searchType: String,
    extensionApi: ExtensionApi
): Int {
    // Early exit with error value for unsupported search types
    if (searchType == SEARCH_TYPE_MOST_RECENT) {
        return EVENT_HISTORY_ERROR
    }
    return try {
        val latch = CountDownLatch(1)
        var eventCounts = 0
        extensionApi.getHistoricalEvents(
            requests.toTypedArray(),
            searchType == SEARCH_TYPE_ORDERED,
            object : AdobeCallbackWithError<Array<EventHistoryResult>> {
                override fun call(results: Array<EventHistoryResult>) {
                    eventCounts = convertEventHistoryResultToInt(
                        searchType == SEARCH_TYPE_ORDERED,
                        results
                    )
                    latch.countDown()
                }

                override fun fail(error: AdobeError) {
                    Log.warning(
                        LaunchRulesEngineConstants.LOG_TAG,
                        LOG_TAG,
                        "Unable to retrieve historical events, caused by the error: ${error.errorName}"
                    )
                    eventCounts = EVENT_HISTORY_ERROR
                    latch.countDown()
                }
            }
        )
        latch.await(ASYNC_TIMEOUT, TimeUnit.MILLISECONDS)
        eventCounts
    } catch (e: Exception) {
        Log.warning(
            LaunchRulesEngineConstants.LOG_TAG,
            LOG_TAG,
            "Unable to retrieve historical events, caused by the exception: ${e.localizedMessage}"
        )
        EVENT_HISTORY_ERROR
    }
}

/**
 * Converts the result of an event history lookup query to an integer.
 * For an "any" search (event order does not matter), the value returned will be the count of all matching events in EventHistory
 * For an `.ordered` search (events must occur in the provided order), the value returned will be 1 if the events were found in the provided order, or 0 otherwise.
 * If a database error occurred, this method will always return -1
 *
 * @param enforceOrder `boolean` if true, the events must have been recorded in the same order as the request
 * @param eventHistoryResult `Array<EventHistoryResult>` the result of the event history lookup query
 */
@JvmSynthetic
internal fun convertEventHistoryResultToInt(
    enforceOrder: Boolean,
    eventHistoryResult: Array<EventHistoryResult>
): Int {
    if (enforceOrder) {
        for (result in eventHistoryResult) {
            // Early exit on default value or database error
            if (result.count == EVENT_HISTORY_ERROR) {
                return EVENT_HISTORY_ERROR
            }
            // Early exit on ordered searches if any event result returned no records
            if (result.count == EVENT_HISTORY_RESULT_NOT_FOUND) {
                return EVENT_HISTORY_RESULT_NOT_FOUND
            }
        }
        // if all events are found, return 1
        return 1
    } else {
        var totalCount = 0
        for (result in eventHistoryResult) {
            // Early exit on database error
            if (result.count == EVENT_HISTORY_ERROR) {
                return EVENT_HISTORY_ERROR
            }
            totalCount += result.count
        }
        return totalCount
    }
}
