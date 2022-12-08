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

import com.adobe.marketing.mobile.EventHistoryRequest
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.services.Log
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

private const val LOG_TAG = "historicalEventsQuerying"
private const val SEARCH_TYPE_ANY = "any"
private const val ASYNC_TIMEOUT = 1000L

@JvmSynthetic
internal fun historicalEventsQuerying(
    requests: List<EventHistoryRequest>,
    searchType: String,
    extensionApi: ExtensionApi
): Int {
    return try {
        val latch = CountDownLatch(1)
        var eventCounts = 0
        extensionApi.getHistoricalEvents(
            requests.toTypedArray(),
            searchType == SEARCH_TYPE_ANY
        ) {
            latch.countDown()
            eventCounts = it
        }
        latch.await(ASYNC_TIMEOUT, TimeUnit.MILLISECONDS)
        eventCounts
    } catch (e: Exception) {
        Log.warning(
            LaunchRulesEngineConstants.LOG_TAG,
            LOG_TAG,
            "Unable to retrieve historical events, caused by the exception: ${e.localizedMessage}"
        )
        0
    }
}
