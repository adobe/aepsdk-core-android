/*
  Copyright 2025 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.internal.util

import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.EventHistoryRequest

/**
 * Creates an [EventHistoryRequest] from this event.
 *
 * @param from the start time that represents the lower bounds of the date range used when looking up an Event
 * @param to the end time that represents the upper bounds of the date range used when looking up an Event
 * @return an [EventHistoryRequest] with mask derived from this event's data
 */
internal fun Event.toEventHistoryRequest(
    from: Long = 0,
    to: Long = 0
): EventHistoryRequest {
    val flattenedData = eventData?.flattening() ?: emptyMap()

    // Filter the flattened data based on mask if provided
    val filteredData: Map<String, Any?> = if (mask != null) {
        // Convert mask array to a set for O(1) lookups
        val maskSet = mask.toSet()
        flattenedData.filter { maskSet.contains(it.key) }
    } else {
        // If no mask is provided, use all the data
        flattenedData
    }

    return EventHistoryRequest(filteredData, from, to)
}
