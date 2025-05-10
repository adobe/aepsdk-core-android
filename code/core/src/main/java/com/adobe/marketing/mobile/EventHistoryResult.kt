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

package com.adobe.marketing.mobile

/**
 * Passed to handler of [ExtensionApi.getHistoricalEvents] API to represent the result of event look up in EventHistoryDatabase
 * @param count The number of occurrences of the event in the event history database
 * @param oldestOccurrence The time of the oldest occurrence of the event in the event history database. If count is 0, this value will be null
 * @param newestOccurrence The time of the newest occurrence of the event in the event history database. If count is 0, this value will be null
 */
data class EventHistoryResult(
    val count: Int,
    val oldestOccurrence: Long? = null,
    val newestOccurrence: Long? = null
)
