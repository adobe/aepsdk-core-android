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

import androidx.annotation.VisibleForTesting
import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.EventSource
import com.adobe.marketing.mobile.EventType
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.internal.eventhub.EventPreprocessor

internal class LaunchRulesEvaluator(
    private val name: String,
    private val launchRulesEngine: LaunchRulesEngine,
    private val extensionApi: ExtensionApi
) : EventPreprocessor {

    private var cachedEvents: MutableList<Event>? = mutableListOf()
    private val launchRulesConsequence: LaunchRulesConsequence =
        LaunchRulesConsequence(extensionApi)

    @VisibleForTesting
    internal fun getCachedEventCount(): Int {
        return cachedEvents?.size ?: 0
    }

    override fun process(event: Event): Event {
        // if cachedEvents is null, we know rules are set and can skip to evaluation
        // else check if this is an event to start processing of cachedEvents
        // otherwise, add the event to cachedEvents till rules are set
        val matchedRules = launchRulesEngine.process(event)
        if (cachedEvents == null) {
            return launchRulesConsequence.evaluateRulesConsequence(event, matchedRules)
        } else if (event.type == EventType.RULES_ENGINE && event.source == EventSource.REQUEST_RESET) {
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
            EventType.RULES_ENGINE,
            EventSource.REQUEST_RESET
        ).build()
        extensionApi.dispatch(dispatchEvent)
    }
}
