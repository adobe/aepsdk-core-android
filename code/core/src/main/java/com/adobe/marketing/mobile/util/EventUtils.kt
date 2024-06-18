/*
  Copyright 2024 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

@file:JvmName("EventUtils") // Allows Java callers to use EventUtils.<> instead of EventUtilsKt.<>

package com.adobe.marketing.mobile.util

import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.EventSource
import com.adobe.marketing.mobile.EventType

private const val KEY_EVENT_DATA_DEBUG = "debug"
private const val KEY_DEBUG_EVENT_TYPE = "eventType"
private const val KEY_DEBUG_EVENT_SOURCE = "eventSource"

/**
 * Returns the debug event type (identified by debug.eventType) from the event data if present, otherwise null.
 * @return the debug event type if present, otherwise null
 */
fun Event.getDebugEventType(): String? {
    val debugData = getDebugEventData() ?: return null
    val debugEventType = debugData[KEY_DEBUG_EVENT_TYPE]
    if (debugEventType !is String) return null

    return debugEventType
}

/**
 * Returns the debug event source (identified by debug.eventSource) from the event data if present, otherwise null.
 * @return the debug event source if present, otherwise null
 */
fun Event.getDebugEventSource(): String? {
    val debugData = getDebugEventData() ?: return null
    val debugEventSource = debugData[KEY_DEBUG_EVENT_SOURCE]
    if (debugEventSource !is String) return null

    return debugEventSource
}

/**
 * Returns the debug event data (identified by data.debug) from the event if present, otherwise null.
 * @return the content of "debug" key within "Event.data" if present,
 *         null if the event is not a debug event or if the debug data does not exist
 */
private fun Event.getDebugEventData(): Map<String, Any?>? {
    if (type != EventType.SYSTEM || source != EventSource.DEBUG) return null

    if (eventData == null) return null

    return DataReader.optTypedMap(Any::class.java, eventData, KEY_EVENT_DATA_DEBUG, null) ?: null
}
