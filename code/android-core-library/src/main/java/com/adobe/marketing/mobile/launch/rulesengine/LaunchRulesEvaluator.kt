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
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.MobileCore

internal class LaunchRulesEvaluator(
    private val name: String,
    private val launchRulesEngine: LaunchRulesEngine,
    private val extensionApi: ExtensionApi
) : EventPreprocessor {

    private var cachedEvents: MutableList<Event>? = mutableListOf()
    private val logTag = "LaunchRulesEvaluator_$name"
    private val launchRulesConsequence: LaunchRulesConsequence = LaunchRulesConsequence(extensionApi)

    companion object {
        // TODO: we should move the following event type/event source values to the public EventType/EventSource classes once we have those.
        const val EVENT_SOURCE_REQUEST_RESET = "com.adobe.eventsource.requestreset"
        const val EVENT_TYPE_RULES_ENGINE = "com.adobe.eventtype.rulesengine"
    }

    override fun process(event: Event?): Event? {
        if (event == null) return null

        // if cachedEvents is null, we know rules are set and can skip to evaluation
        // else check if this is an event to start processing of cachedEvents
        // otherwise, add the event to cachedEvents till rules are set
        val matchedRules = launchRulesEngine.process(event)
        if (cachedEvents == null) {
            return launchRulesConsequence.evaluateRulesConsequence(event, matchedRules)
        } else if (event.type == EVENT_TYPE_RULES_ENGINE && event.source == EVENT_SOURCE_REQUEST_RESET) {
            reprocessCachedEvents()
        } else {
            cachedEvents?.add(event)
        }
        return launchRulesConsequence.evaluateRulesConsequence(event, matchedRules)
    }

    private fun reprocessCachedEvents() {
        cachedEvents?.forEach { event ->
            val matchedRules = launchRulesEngine.process(event)
            launchRulesConsequence.evaluateRulesConsequence(event, matchedRules)
        }
        clearCachedEvents()
    }

    private fun clearCachedEvents() {
        cachedEvents?.clear()
        cachedEvents = null
    }

    /**
     * Reset the current [LaunchRule]s. A RulesEngine Reset [Event] will be dispatched to reprocess the cached events.
     *
     * @param rules a list of [LaunchRule]s
     */
    fun replaceRules(rules: List<LaunchRule?>?) {
        if (rules == null) return
        launchRulesEngine.replaceRules(rules)
        val dispatchEvent = Event.Builder(
            name,
            EVENT_TYPE_RULES_ENGINE,
            EVENT_SOURCE_REQUEST_RESET
        ).build()
        extensionApi.dispatch(dispatchEvent)
        MobileCore.log(
            LoggingMode.VERBOSE,
            logTag,
            "Successfully dispatched rules engine reset event."
        )
    }
}
