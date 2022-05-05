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

import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.EventPreprocessor
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.MobileCore

internal class LaunchRulesEvaluator(val name: String) : EventPreprocessor {
    private val launchRulesEngine = LaunchRulesEngine()
    private var cachedEvents: MutableList<Event>? = mutableListOf()
    private val logTag = "JSONRulesParser_$name"

    companion object {
        const val CACHED_EVENT_MAX = 99
        // TODO: we should move the following event type/event source values to the public EventType/EventSource classes once we have those.
        const val EVENT_SOURCE = "com.adobe.eventSource.requestReset"
        const val EVENT_TYPE = "com.adobe.eventType.rulesEngine"
    }

    override fun process(event: Event?): Event? {
        if (event == null) return null

        if (event.type == EVENT_TYPE && event.source == EVENT_SOURCE) {
            reprocessCachedEvents()
        } else {
            cacheEvent(event)
            launchRulesEngine.process(event)
            // TODO: handle rules consequence
        }
        return event
    }

    private fun reprocessCachedEvents() {
        cachedEvents?.forEach { event ->
            launchRulesEngine.process(event)
            // TODO: handle rules consequence
        }
        cachedEvents = null
    }


    private fun cacheEvent(event: Event) {
        cachedEvents?.let {
            if (cachedEvents?.size ?: -1 > CACHED_EVENT_MAX) {
                cachedEvents = null
                MobileCore.log(
                    LoggingMode.WARNING,
                    logTag,
                    "Will not to reprocess cached events as the cached events have reached the limit: $CACHED_EVENT_MAX"
                )
            }
            cachedEvents?.add(event)
        }
    }


    fun replaceRules(rules: List<LaunchRule?>?) {
        launchRulesEngine.replaceRules(rules)
        MobileCore.dispatchEvent(
            Event.Builder(
                name,
                EVENT_TYPE,
                EVENT_SOURCE
            ).build()
        ) { extensionError ->
            MobileCore.log(
                LoggingMode.ERROR,
                logTag,
                "Failed to reprocess cached events, caused by the error: ${extensionError.errorName}"
            )
        }
    }
}