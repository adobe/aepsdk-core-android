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
import com.adobe.marketing.mobile.ExtensionErrorCallback
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.internal.utility.EventDataMerger
import com.adobe.marketing.mobile.rulesengine.DelimiterPair
import com.adobe.marketing.mobile.rulesengine.Template
import com.adobe.marketing.mobile.rulesengine.TokenFinder
import com.adobe.marketing.mobile.rulesengine.Transforming

internal class LaunchRulesEvaluator(
    private val name: String,
    private val launchRulesEngine: LaunchRulesEngine,
    private val extensionApi: ExtensionApi
    ) : EventPreprocessor {

    private var cachedEvents: MutableList<Event>? = mutableListOf()
    private val logTag = "LaunchRulesEvaluator_$name"
    private val launchRulesConsequence: LaunchRulesConsequence = LaunchRulesConsequence(launchRulesEngine, extensionApi)


    companion object {
        const val CACHED_EVENT_MAX = 99
        const val EVENT_SOURCE_REQUEST_RESET = "com.adobe.eventsource.requestreset"
        const val EVENT_TYPE_RULES_ENGINE = "com.adobe.eventtype.rulesengine"
    }

    override fun process(event: Event?): Event? {
        if (event == null) return null

        if (event.type == EVENT_TYPE_RULES_ENGINE && event.source == EVENT_SOURCE_REQUEST_RESET) {
            reprocessCachedEvents()
        } else {
            cacheEvent(event)
        }
        return launchRulesConsequence.evaluateRules(event)
    }

    private fun reprocessCachedEvents() {
        cachedEvents?.forEach { event ->
            launchRulesConsequence.evaluateRules(event)
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
                EVENT_TYPE_RULES_ENGINE,
                EVENT_SOURCE_REQUEST_RESET
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

