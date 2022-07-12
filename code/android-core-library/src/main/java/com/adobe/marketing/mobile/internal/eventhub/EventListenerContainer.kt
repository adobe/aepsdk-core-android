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

package com.adobe.marketing.mobile.internal.eventhub

import com.adobe.marketing.mobile.AdobeCallbackWithError
import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.EventSource
import com.adobe.marketing.mobile.EventType
import com.adobe.marketing.mobile.ExtensionEventListener
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.MobileCore
import java.lang.Exception
import java.util.concurrent.ScheduledFuture

sealed class EventListenerContainer {
    abstract fun shouldNotify(event: Event): Boolean

    abstract fun notify(event: Event)
}

class ResponseListenerContainer(
    val triggerEventId: String,
    val timeoutTask: ScheduledFuture<Unit>?,
    val listener: AdobeCallbackWithError<Event>
) : EventListenerContainer() {
    override fun shouldNotify(event: Event): Boolean {
        return event.responseID == triggerEventId
    }

    override fun notify(event: Event) {
        try {
            listener.call(event)
        } catch (ex: Exception) {
            MobileCore.log(
                LoggingMode.DEBUG,
                "OneTimeListenerContainer",
                "Exception thrown for EventId ${event.uniqueIdentifier}. $ex"
            )
        }
    }
}

class ExtensionListenerContainer(val eventType: String, val eventSource: String, val listener: ExtensionEventListener) : EventListenerContainer() {
    override fun shouldNotify(event: Event): Boolean {
        // Wildcard listeners should only be notified of paired response events.
        return if (event.responseID != null) {
            (eventType == EventType.TYPE_WILDCARD && eventSource == EventSource.TYPE_WILDCARD)
        } else {
            eventType.equals(event.type, ignoreCase = true) && eventSource.equals(event.source, ignoreCase = true) ||
                eventType == EventType.TYPE_WILDCARD && eventSource == EventSource.TYPE_WILDCARD
        }
    }

    override fun notify(event: Event) {
        try {
            listener.hear(event)
        } catch (ex: Exception) {
            MobileCore.log(
                LoggingMode.DEBUG,
                "ExtensionListenerContainer",
                "Exception thrown for EventId ${event.uniqueIdentifier}. $ex"
            )
        }
    }
}
