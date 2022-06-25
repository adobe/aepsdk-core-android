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
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.internal.utility.EventDataMerger
import com.adobe.marketing.mobile.internal.utility.prettify
import com.adobe.marketing.mobile.rulesengine.DelimiterPair
import com.adobe.marketing.mobile.rulesengine.Template
import com.adobe.marketing.mobile.rulesengine.TokenFinder
import com.adobe.marketing.mobile.utils.EventDataUtils

class LaunchRulesConsequence(
    private val extensionApi: ExtensionApi
) {

    private val logTag = "LaunchRulesConsequence"
    private var dispatchChainedEventsCount = mutableMapOf<String, Int>()
    companion object {
        // TODO: we should move the following event type/event source values to the public EventType/EventSource classes once we have those.
        private const val EVENT_SOURCE_RESPONSE_CONTENT = "com.adobe.eventSource.responseContent"
        private const val EVENT_TYPE_RULES_ENGINE = "com.adobe.eventtype.rulesengine"
        private const val LAUNCH_RULE_TOKEN_LEFT_DELIMITER = "{%"
        private const val LAUNCH_RULE_TOKEN_RIGHT_DELIMITER = "%}"
        private const val CONSEQUENCE_TYPE_ADD = "add"
        private const val CONSEQUENCE_TYPE_MOD = "mod"
        private const val CONSEQUENCE_TYPE_DISPATCH = "dispatch"
        private const val CONSEQUENCE_DETAIL_ACTION_COPY = "copy"
        private const val CONSEQUENCE_DETAIL_ACTION_NEW = "new"
        // Do not process Dispatch consequence if chained event count is greater than max
        private const val MAX_CHAINED_CONSEQUENCE_COUNT = 1
        private const val CONSEQUENCE_DISPATCH_EVENT_NAME = "Dispatch Consequence Result"
        private const val CONSEQUENCE_EVENT_DATA_KEY_ID = "id"
        private const val CONSEQUENCE_EVENT_DATA_KEY_TYPE = "type"
        private const val CONSEQUENCE_EVENT_DATA_KEY_DETAIL = "detail"
        private const val CONSEQUENCE_EVENT_DATA_KEY_CONSEQUENCE = "triggeredconsequence"
        private const val CONSEQUENCE_EVENT_NAME = "Rules Consequence Event"
    }

    fun evaluateRulesConsequence(event: Event, matchedRules: List<LaunchRule>): Event? {
        val dispatchChainCount = dispatchChainedEventsCount.remove(event.uniqueIdentifier) ?: 0
        val launchTokenFinder = LaunchTokenFinder(event, extensionApi)
        var processedEvent: Event = event
        for (rule in matchedRules) {
            for (consequence in rule.consequenceList) {
                val consequenceWithConcreteValue = replaceToken(consequence, launchTokenFinder)
                when (consequenceWithConcreteValue.type) {
                    CONSEQUENCE_TYPE_ADD -> {
                        val attachedEventData = processAttachDataConsequence(
                            consequenceWithConcreteValue,
                            processedEvent.eventData
                        ) ?: continue
                        processedEvent = processedEvent.cloneWithEventData(attachedEventData)
                    }
                    CONSEQUENCE_TYPE_MOD -> {
                        val modifiedEventData = processModifyDataConsequence(
                            consequenceWithConcreteValue,
                            processedEvent.eventData
                        ) ?: continue
                        processedEvent = processedEvent.cloneWithEventData(modifiedEventData)
                    }
                    CONSEQUENCE_TYPE_DISPATCH -> {
                        if (dispatchChainCount >= MAX_CHAINED_CONSEQUENCE_COUNT) {
                            MobileCore.log(
                                LoggingMode.VERBOSE,
                                logTag,
                                "Unable to process dispatch consequence, max chained " +
                                        "dispatch consequences limit of $MAX_CHAINED_CONSEQUENCE_COUNT" +
                                        "met for this event uuid ${event.uniqueIdentifier}"
                            )
                            continue
                        }
                        val dispatchEvent = processDispatchConsequence(
                            consequenceWithConcreteValue,
                            processedEvent.eventData
                        ) ?: continue
                        MobileCore.log(
                            LoggingMode.VERBOSE,
                            logTag,
                            " Generating new dispatch consequence result event $dispatchEvent"
                        )
                        MobileCore.dispatchEvent(dispatchEvent) {
                            MobileCore.log(
                                LoggingMode.WARNING,
                                logTag,
                                "An error occurred when dispatching dispatch consequence result event"
                            )
                        }
                        dispatchChainedEventsCount[dispatchEvent.uniqueIdentifier] = dispatchChainCount + 1
                    }
                    else -> {
                        val consequenceEvent = generateConsequenceEvent(consequenceWithConcreteValue)
                        MobileCore.log(
                            LoggingMode.VERBOSE,
                            logTag,
                            "Generating new consequence event $consequenceEvent"
                        )
                        MobileCore.dispatchEvent(consequenceEvent) {
                            MobileCore.log(
                                LoggingMode.WARNING,
                                logTag,
                                "An error occurred when dispatching consequence result event"
                            )
                        }
                    }
                }
            }
        }
        return processedEvent
    }

    /**
     * Replace tokens inside the provided [RuleConsequence] with the right value
     *
     * @param consequence [RuleConsequence] instance that may contain tokens
     * @param tokenFinder [TokenFinder] instance which replaces the tokens with values
     * @return the [RuleConsequence] with replaced tokens
     */
    private fun replaceToken(consequence: RuleConsequence, tokenFinder: TokenFinder): RuleConsequence {
        val tokenReplacedMap = replaceToken(consequence.detail, tokenFinder)
        return RuleConsequence(consequence.id, consequence.type, tokenReplacedMap)
    }

    private fun replaceToken(detail: Map<String, Any?>?, tokenFinder: TokenFinder): Map<String, Any?>? {
        if (detail.isNullOrEmpty())
            return null
        val mutableDetail = detail.toMutableMap()
        for ((key, value) in detail) {
            when (value) {
                is String -> mutableDetail[key] = replaceToken(value, tokenFinder)
                is Map<*, *> -> mutableDetail[key] = replaceToken(
                    EventDataUtils.castFromGenericType(value),
                    tokenFinder)
                else -> continue
            }
        }
        return mutableDetail
    }

    private fun replaceToken(value: String, tokenFinder: TokenFinder): String {
        val template = Template(value, DelimiterPair(LAUNCH_RULE_TOKEN_LEFT_DELIMITER, LAUNCH_RULE_TOKEN_RIGHT_DELIMITER))
        return template.render(tokenFinder, LaunchRuleTransformer.createTransforming())
    }

    /**
     * Process an attach data consequence event.  Attaches the triggering event data from the [RuleConsequence] to the
     * triggering event data without overwriting the original event data. If either the event data
     * from the [RuleConsequence] or the triggering event data is null then the processing is aborted.
     *
     * @param consequence the [RuleConsequence] which contains the event data to attach
     * @param eventData the event data of the triggering [Event]
     * @return [Map] with the [RuleConsequence] data attached to the triggering event data, or
     * null if the processing fails
     */
    private fun processAttachDataConsequence(consequence: RuleConsequence, eventData: Map<String, Any?>?): Map<String, Any?>? {
        val from = EventDataUtils.castFromGenericType(consequence.eventData) ?: run {
            MobileCore.log(
                LoggingMode.ERROR,
                logTag,
                "Unable to process an AttachDataConsequence Event, 'eventData' is missing from 'details'"
            )
            return null
        }
        val to = eventData ?: run {
            MobileCore.log(
                LoggingMode.ERROR,
                logTag,
                "Unable to process an AttachDataConsequence Event, 'eventData' is missing from original event"
            )
            return null
        }
        MobileCore.log(
            LoggingMode.VERBOSE,
            logTag,
            "Attaching event data with ${from.prettify()}"
        )
        return EventDataMerger.merge(from, to, false)
    }

    /**
     * Process a modify data consequence event.  Modifies the triggering event data by merging the
     * event data from the [RuleConsequence] onto it. If either the event data
     * from the [RuleConsequence] or the triggering event data is null then the processing is aborted.
     *
     * @param consequence the [RuleConsequence] which contains the event data to attach
     * @param eventData the event data of the triggering [Event]
     * @return [Map] with the Event data modified with the [RuleConsequence] data, or
     * null if the processing fails
     */
    private fun processModifyDataConsequence(consequence: RuleConsequence, eventData: Map<String, Any?>?): Map<String, Any?>? {
        val from = EventDataUtils.castFromGenericType(consequence.eventData) ?: run {
            MobileCore.log(
                LoggingMode.ERROR,
                logTag,
                "Unable to process a ModifyDataConsequence Event, 'eventData' is missing from 'details'"
            )
            return null
        }
        val to = eventData ?: run {
            MobileCore.log(
                LoggingMode.ERROR,
                logTag,
                "Unable to process a ModifyDataConsequence Event, 'eventData' is missing from original event"
            )
            return null
        }
        MobileCore.log(
            LoggingMode.VERBOSE,
            logTag,
            "Modifying event data with ${from.prettify()}"
        )
        return EventDataMerger.merge(from, to, true)
    }

    /**
     * Process a dispatch consequence event. Generates a new [Event] from the details contained within the [RuleConsequence]
     *
     * @param consequence the [RuleConsequence] which contains details on the new [Event] to generate
     * @param eventData the triggering Event data
     * @return a new [Event] to be dispatched to the [EventHub], or null if the processing failed.
     */
    private fun processDispatchConsequence(consequence: RuleConsequence, eventData: Map<String, Any?>?): Event? {
        val type = consequence.eventType ?: run {
            MobileCore.log(
                LoggingMode.ERROR,
                logTag,
                "Unable to process a DispatchConsequence Event, 'type' is missing from 'details'"
            )
            return null
        }
        val source = consequence.eventSource ?: run {
            MobileCore.log(
                LoggingMode.ERROR,
                logTag,
                "Unable to process a DispatchConsequence Event, 'source' is missing from 'details'"
            )
            return null
        }
        val action = consequence.eventDataAction ?: run {
            MobileCore.log(
                LoggingMode.ERROR,
                logTag,
                "Unable to process a DispatchConsequence Event, 'eventdataaction' is missing from 'details'"
            )
            return null
        }
        val dispatchEventData: Map<String, Any?>?
        when (action) {
            CONSEQUENCE_DETAIL_ACTION_COPY -> {
                dispatchEventData = eventData
            }
            CONSEQUENCE_DETAIL_ACTION_NEW -> {
                val data = EventDataUtils.castFromGenericType(consequence.eventData)
                dispatchEventData = data?.filterValues { it != null }
            }
            else -> {
                MobileCore.log(
                    LoggingMode.ERROR,
                    logTag,
                    "Unable to process a DispatchConsequence Event, unsupported 'eventdataaction', expected values copy/new"
                )
                return null
            }
        }
        return Event.Builder(CONSEQUENCE_DISPATCH_EVENT_NAME, type, source)
            .setEventData(dispatchEventData)
            .build()
    }

    /**
     * Generate a consequence event with provided consequence data
     * @param consequence [RuleConsequence] of the rule
     * @return a consequence [Event]
     */
    private fun generateConsequenceEvent(consequence: RuleConsequence): Event? {
        val eventData = mutableMapOf<String, Any?>()
        eventData[CONSEQUENCE_EVENT_DATA_KEY_DETAIL] = consequence.detail
        eventData[CONSEQUENCE_EVENT_DATA_KEY_ID] = consequence.id
        eventData[CONSEQUENCE_EVENT_DATA_KEY_TYPE] = consequence.type
        return Event.Builder(
            CONSEQUENCE_EVENT_NAME,
            EVENT_TYPE_RULES_ENGINE,
            EVENT_SOURCE_RESPONSE_CONTENT)
            .setEventData(mapOf(CONSEQUENCE_EVENT_DATA_KEY_CONSEQUENCE to eventData))
            .build()
    }
}

// Extend RuleConsequence with helper methods for processing consequence events.
val RuleConsequence.eventData: Map<*, *>?
    get() = detail?.get("eventdata") as? Map<*, *>

val RuleConsequence.eventSource: String?
    get() = detail?.get("source") as? String

val RuleConsequence.eventType: String?
    get() = detail?.get("type") as? String

val RuleConsequence.eventDataAction: String?
    get() = detail?.get("eventdataaction") as? String
