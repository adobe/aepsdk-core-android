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

internal class LaunchRulesEvaluator(
    private val name: String,
    private val launchRulesEngine: LaunchRulesEngine
) : EventPreprocessor {

    private var cachedEvents: MutableList<Event>? = mutableListOf()
    private val logTag = "LaunchRulesEvaluator_$name"

    companion object {
        const val CACHED_EVENT_MAX = 99

        // TODO: we should move the following event type/event source values to the public EventType/EventSource classes once we have those.
        const val EVENT_SOURCE = "com.adobe.eventsource.requestreset"
        const val EVENT_TYPE = "com.adobe.eventtype.rulesengine"
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
        clearCachedEvents()
    }

    private fun clearCachedEvents() {
        cachedEvents?.clear()
        cachedEvents = null
    }

    private fun cacheEvent(event: Event) {
        cachedEvents?.let {
            if ((cachedEvents?.size ?: -1) > CACHED_EVENT_MAX) {
                clearCachedEvents()
                MobileCore.log(
                    LoggingMode.WARNING,
                    logTag,
                    "Will not to reprocess cached events as the cached events have reached the limit: $CACHED_EVENT_MAX"
                )
            }
            cachedEvents?.add(event)
        }
    }

    /**
     * Reset the current [LaunchRule]s. A RulesEngine Reset [Event] will be dispatched to reprocess the cached events.
     *
     * @param rules a list of [LaunchRule]s
     */
    fun replaceRules(rules: List<LaunchRule?>?) {
        if (rules == null) return
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
