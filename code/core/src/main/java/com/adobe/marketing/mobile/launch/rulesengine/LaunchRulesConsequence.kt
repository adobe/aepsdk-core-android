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

import com.adobe.marketing.mobile.AdobeCallbackWithError
import com.adobe.marketing.mobile.AdobeError
import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.EventHistoryResult
import com.adobe.marketing.mobile.EventSource
import com.adobe.marketing.mobile.EventType
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.internal.eventhub.EventHub
import com.adobe.marketing.mobile.internal.util.EventDataMerger
import com.adobe.marketing.mobile.internal.util.fnv1a32
import com.adobe.marketing.mobile.internal.util.prettify
import com.adobe.marketing.mobile.internal.util.toEventHistoryRequest
import com.adobe.marketing.mobile.rulesengine.DelimiterPair
import com.adobe.marketing.mobile.rulesengine.Template
import com.adobe.marketing.mobile.rulesengine.TokenFinder
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.util.DataReader
import com.adobe.marketing.mobile.util.EventDataUtils
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

internal class LaunchRulesConsequence(
    private val extensionApi: ExtensionApi
) {

    private val logTag = "LaunchRulesConsequence"
    private var dispatchChainedEventsCount = mutableMapOf<String, Int>()

    companion object {
        private const val LAUNCH_RULE_TOKEN_LEFT_DELIMITER = "{%"
        private const val LAUNCH_RULE_TOKEN_RIGHT_DELIMITER = "%}"
        private const val CONSEQUENCE_TYPE_ADD = "add"
        private const val CONSEQUENCE_TYPE_MOD = "mod"
        private const val CONSEQUENCE_TYPE_DISPATCH = "dispatch"
        private const val CONSEQUENCE_DETAIL_ACTION_COPY = "copy"
        private const val CONSEQUENCE_DETAIL_ACTION_NEW = "new"
        private const val CONSEQUENCE_TYPE_SCHEMA = "schema"

        // Do not process Dispatch consequence if chained event count is greater than max
        private const val MAX_CHAINED_CONSEQUENCE_COUNT = 1
        private const val CONSEQUENCE_DISPATCH_EVENT_NAME = "Dispatch Consequence Result"
        private const val CONSEQUENCE_EVENT_DATA_KEY_ID = "id"
        private const val CONSEQUENCE_EVENT_DATA_KEY_TYPE = "type"
        private const val CONSEQUENCE_EVENT_DATA_KEY_DETAIL = "detail"
        private const val CONSEQUENCE_EVENT_DATA_KEY_CONSEQUENCE = "triggeredconsequence"
        private const val CONSEQUENCE_EVENT_NAME = "Rules Consequence Event"

        // Event history operation constants
        private const val CONSEQUENCE_SCHEMA_EVENT_HISTORY =
            "https://ns.adobe.com/personalization/eventHistoryOperation"
        private const val EVENT_HISTORY_OPERATION_KEY = "operation"
        private const val EVENT_HISTORY_CONTENT_KEY = "content"
        private const val CONSEQUENCE_EVENT_HISTORY_OPERATION_INSERT = "insert"
        private const val CONSEQUENCE_EVENT_HISTORY_OPERATION_INSERT_IF_NOT_EXISTS =
            "insertIfNotExists"
        private const val ASYNC_TIMEOUT = 1000L
    }

    /**
     * Processes the [matchedRules] against the supplied [event]. This evaluation may result
     * in dispatch of the supplied event after attachment, modification of its data or dispatch of
     * a new event from the supplied event
     *
     * @param event the event to be processed
     * @param matchedRules the rules against which the current events is to be processed
     * @return the token replaced [Event] after token replacement
     */
    fun process(event: Event, matchedRules: List<LaunchRule>): Event {
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
                            Log.warning(
                                LaunchRulesEngineConstants.LOG_TAG,
                                logTag,
                                "Unable to process dispatch consequence, max chained " +
                                    "dispatch consequences limit of $MAX_CHAINED_CONSEQUENCE_COUNT" +
                                    "met for this event uuid ${event.uniqueIdentifier}"
                            )
                            continue
                        }
                        val dispatchEvent = processDispatchConsequence(
                            consequenceWithConcreteValue,
                            processedEvent
                        ) ?: continue

                        Log.trace(
                            LaunchRulesEngineConstants.LOG_TAG,
                            logTag,
                            "processDispatchConsequence - Dispatching event - ${dispatchEvent.uniqueIdentifier}"
                        )
                        extensionApi.dispatch(dispatchEvent)
                        dispatchChainedEventsCount[dispatchEvent.uniqueIdentifier] =
                            dispatchChainCount + 1
                    }

                    CONSEQUENCE_TYPE_SCHEMA -> {
                        processSchemaConsequence(consequenceWithConcreteValue, processedEvent)
                        continue
                    }

                    else -> {
                        val consequenceEvent =
                            generateConsequenceEvent(consequenceWithConcreteValue, processedEvent)
                        Log.trace(
                            LaunchRulesEngineConstants.LOG_TAG,
                            logTag,
                            "evaluateRulesConsequence - Dispatching consequence event ${consequenceEvent.uniqueIdentifier}"
                        )
                        extensionApi.dispatch(consequenceEvent)
                    }
                }
            }
        }
        return processedEvent
    }

    /**
     * Evaluates the supplied event against the matched rules and returns the [RuleConsequence]'s
     *
     * @param event the event to be evaluated
     * @param matchedRules the rules whose consequences are to be processed
     * @return a token replaced list of [RuleConsequence]'s that match the supplied event.
     */
    fun evaluate(event: Event, matchedRules: List<LaunchRule>): List<RuleConsequence> {
        val processedConsequences = mutableListOf<RuleConsequence>()
        val launchTokenFinder = LaunchTokenFinder(event, extensionApi)

        matchedRules.forEach { matchedRule ->
            matchedRule.consequenceList.forEach { consequence ->
                processedConsequences.add(replaceToken(consequence, launchTokenFinder))
            }
        }
        return processedConsequences
    }

    /**
     * Replace tokens inside the provided [RuleConsequence] with the right value
     *
     * @param consequence [RuleConsequence] instance that may contain tokens
     * @param tokenFinder [TokenFinder] instance which replaces the tokens with values
     * @return the [RuleConsequence] with replaced tokens
     */
    private fun replaceToken(
        consequence: RuleConsequence,
        tokenFinder: TokenFinder
    ): RuleConsequence {
        val tokenReplacedMap = replaceToken(consequence.detail, tokenFinder)
        return RuleConsequence(consequence.id, consequence.type, tokenReplacedMap)
    }

    private fun replaceToken(value: Any?, tokenFinder: TokenFinder): Any? {
        return when (value) {
            is String -> replaceToken(value, tokenFinder)
            is Map<*, *> -> replaceToken(EventDataUtils.castFromGenericType(value), tokenFinder)
            is List<*> -> replaceToken(value, tokenFinder)
            else -> value
        }
    }

    private fun replaceToken(
        detail: Map<String, Any?>?,
        tokenFinder: TokenFinder
    ): Map<String, Any?>? {
        if (detail.isNullOrEmpty()) {
            return null
        }
        val mutableDetail = detail.toMutableMap()
        for ((key, value) in detail) {
            mutableDetail[key] = replaceToken(value, tokenFinder)
        }
        return mutableDetail
    }

    private fun replaceToken(value: List<Any?>, tokenFinder: TokenFinder): List<Any?> {
        return value.map { replaceToken(it, tokenFinder) }
    }

    private fun replaceToken(value: String, tokenFinder: TokenFinder): String {
        val template = Template(
            value,
            DelimiterPair(LAUNCH_RULE_TOKEN_LEFT_DELIMITER, LAUNCH_RULE_TOKEN_RIGHT_DELIMITER)
        )
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
    private fun processAttachDataConsequence(
        consequence: RuleConsequence,
        eventData: Map<String, Any?>?
    ): Map<String, Any?>? {
        val from = EventDataUtils.castFromGenericType(consequence.eventData) ?: run {
            Log.error(
                LaunchRulesEngineConstants.LOG_TAG,
                logTag,
                "Unable to process an AttachDataConsequence Event, 'eventData' is missing from 'details'"
            )
            return null
        }
        val to = eventData ?: run {
            Log.error(
                LaunchRulesEngineConstants.LOG_TAG,
                logTag,
                "Unable to process an AttachDataConsequence Event, 'eventData' is missing from original event"
            )
            return null
        }

        if (Log.getLogLevel() == LoggingMode.VERBOSE) {
            Log.trace(
                LaunchRulesEngineConstants.LOG_TAG,
                logTag,
                "Attaching event data with ${from.prettify()}"
            )
        }
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
    private fun processModifyDataConsequence(
        consequence: RuleConsequence,
        eventData: Map<String, Any?>?
    ): Map<String, Any?>? {
        val from = EventDataUtils.castFromGenericType(consequence.eventData) ?: run {
            Log.error(
                LaunchRulesEngineConstants.LOG_TAG,
                logTag,
                "Unable to process a ModifyDataConsequence Event, 'eventData' is missing from 'details'"
            )
            return null
        }
        val to = eventData ?: run {
            Log.error(
                LaunchRulesEngineConstants.LOG_TAG,
                logTag,
                "Unable to process a ModifyDataConsequence Event, 'eventData' is missing from original event"
            )
            return null
        }

        if (Log.getLogLevel() == LoggingMode.VERBOSE) {
            Log.trace(
                LaunchRulesEngineConstants.LOG_TAG,
                logTag,
                "Modifying event data with ${from.prettify()}"
            )
        }
        return EventDataMerger.merge(from, to, true)
    }

    /**
     * Process a dispatch consequence event. Generates a new [Event] from the details contained within the [RuleConsequence]
     *
     * @param consequence the [RuleConsequence] which contains details on the new [Event] to generate
     * @param parentEvent the triggering Event for which a consequence is being generated
     * @return a new [Event] to be dispatched to the [EventHub], or null if the processing failed.
     */
    private fun processDispatchConsequence(
        consequence: RuleConsequence,
        parentEvent: Event
    ): Event? {
        val type = consequence.eventType ?: run {
            Log.error(
                LaunchRulesEngineConstants.LOG_TAG,
                logTag,
                "Unable to process a DispatchConsequence Event, 'type' is missing from 'details'"
            )
            return null
        }
        val source = consequence.eventSource ?: run {
            Log.error(
                LaunchRulesEngineConstants.LOG_TAG,
                logTag,
                "Unable to process a DispatchConsequence Event, 'source' is missing from 'details'"
            )
            return null
        }
        val action = consequence.eventDataAction ?: run {
            Log.error(
                LaunchRulesEngineConstants.LOG_TAG,
                logTag,
                "Unable to process a DispatchConsequence Event, 'eventdataaction' is missing from 'details'"
            )
            return null
        }
        val dispatchEventData: Map<String, Any?>?
        when (action) {
            CONSEQUENCE_DETAIL_ACTION_COPY -> {
                dispatchEventData = parentEvent.eventData
            }

            CONSEQUENCE_DETAIL_ACTION_NEW -> {
                val data = EventDataUtils.castFromGenericType(consequence.eventData)
                dispatchEventData = data?.filterValues { it != null }
            }

            else -> {
                Log.error(
                    LaunchRulesEngineConstants.LOG_TAG,
                    logTag,
                    "Unable to process a DispatchConsequence Event, unsupported 'eventdataaction', expected values copy/new"
                )
                return null
            }
        }
        return Event.Builder(CONSEQUENCE_DISPATCH_EVENT_NAME, type, source)
            .setEventData(dispatchEventData)
            .chainToParentEvent(parentEvent)
            .build()
    }

    /**
     * Generate a consequence event with provided consequence data
     * @param consequence [RuleConsequence] of the rule
     * @param parentEvent the event for which the consequence is to be generated
     * @return a consequence [Event]
     */
    private fun generateConsequenceEvent(consequence: RuleConsequence, parentEvent: Event): Event {
        val eventData = mutableMapOf<String, Any?>()
        eventData[CONSEQUENCE_EVENT_DATA_KEY_DETAIL] = consequence.detail
        eventData[CONSEQUENCE_EVENT_DATA_KEY_ID] = consequence.id
        eventData[CONSEQUENCE_EVENT_DATA_KEY_TYPE] = consequence.type
        return Event.Builder(
            CONSEQUENCE_EVENT_NAME,
            EventType.RULES_ENGINE,
            EventSource.RESPONSE_CONTENT
        )
            .setEventData(mapOf(CONSEQUENCE_EVENT_DATA_KEY_CONSEQUENCE to eventData))
            .chainToParentEvent(parentEvent)
            .build()
    }

    /**
     * Process a schema consequence event.  Handles different schema types including event history operations.
     *
     * @param consequence the [RuleConsequence] which contains the schema details
     * @param parentEvent the event that triggered the rule
     */
    private fun processSchemaConsequence(consequence: RuleConsequence, parentEvent: Event) {
        if (consequence.detailId == null ||
            consequence.schema == null ||
            consequence.detailData == null
        ) {
            Log.warning(
                LaunchRulesEngineConstants.LOG_TAG,
                logTag,
                "Unable to process Schema Consequence for consequence ${consequence.id}, 'id', 'schema' or 'data' is missing from 'details'"
            )
            return
        }
        if (consequence.schema == CONSEQUENCE_SCHEMA_EVENT_HISTORY) {
            processEventHistoryOperation(consequence, parentEvent)
        } else {
            Log.warning(
                LaunchRulesEngineConstants.LOG_TAG,
                logTag,
                "Unable to process Schema Consequence for consequence ${consequence.id}, unsupported schema type ${consequence.schema}"
            )
        }
    }

    /**
     * Process an event history operation consequence. Records events in the Event History database.
     *
     * @param consequence the [RuleConsequence] which contains the event history operation details
     * @param parentEvent the event that triggered the rule
     */
    private fun processEventHistoryOperation(consequence: RuleConsequence, parentEvent: Event) {
        val schemaData = consequence.detailData
        if (schemaData.isNullOrEmpty()) {
            Log.warning(
                LaunchRulesEngineConstants.LOG_TAG,
                logTag,
                "Unable to process eventHistoryOperation operation for consequence ${consequence.id}, 'data' is missing from 'details'"
            )
            return
        }
        val operation = DataReader.optString(schemaData, EVENT_HISTORY_OPERATION_KEY, "")
        if (operation.isNullOrBlank()) {
            Log.warning(
                LaunchRulesEngineConstants.LOG_TAG,
                logTag,
                "Unable to process eventHistoryOperation operation for consequence ${consequence.id}, 'operation' is missing from 'details.data'"
            )
            return
        }

        // Note `content` doesn't need to be resolved here because it was already resolved by LaunchRulesConsequence.process(event: Event, matchedRules: List<LaunchRule>)
        val content =
            DataReader.optTypedMap(Any::class.java, schemaData, EVENT_HISTORY_CONTENT_KEY, null)

        if (content.isNullOrEmpty()) {
            Log.warning(
                LaunchRulesEngineConstants.LOG_TAG,
                logTag,
                "Unable to process eventHistoryOperation operation for consequence ${consequence.id}, 'content' is either missing or improperly formatted in 'details.data'"
            )
            return
        }

        // Create the event to record into history using the provided `content` data, and also dispatch as a rules consequence event
        val eventToRecord = Event.Builder(
            CONSEQUENCE_DISPATCH_EVENT_NAME,
            EventType.RULES_ENGINE,
            EventSource.RESPONSE_CONTENT
        )
            .setEventData(content)
            .chainToParentEvent(parentEvent)
            .build()

        if (!(
            operation == CONSEQUENCE_EVENT_HISTORY_OPERATION_INSERT ||
                operation == CONSEQUENCE_EVENT_HISTORY_OPERATION_INSERT_IF_NOT_EXISTS
            )
        ) {
            Log.warning(
                LaunchRulesEngineConstants.LOG_TAG,
                logTag,
                "Event History operation for id ${consequence.id} - Unsupported history operation '$operation'"
            )
            return
        }

        val eventHash = eventToRecord.eventData.fnv1a32()
        if (eventHash == 0L) {
            Log.warning(
                LaunchRulesEngineConstants.LOG_TAG,
                logTag,
                "Event History operation for id ${consequence.id} - event hash is 0"
            )
        }

        // For INSERT_IF_NOT_EXISTS, check if the event exists first
        if (operation == CONSEQUENCE_EVENT_HISTORY_OPERATION_INSERT_IF_NOT_EXISTS) {
            // Check if the event exists before inserting
            var eventCounts = 0
            try {
                val latch = CountDownLatch(1)
                extensionApi.getHistoricalEvents(
                    arrayOf(eventToRecord.toEventHistoryRequest()),
                    false,
                    object : AdobeCallbackWithError<Array<EventHistoryResult>> {
                        override fun call(results: Array<EventHistoryResult>) {
                            eventCounts = if (results.isNotEmpty()) results[0].count else -1
                            latch.countDown()
                        }

                        override fun fail(error: AdobeError) {
                            eventCounts = -1
                            latch.countDown()
                        }
                    }
                )
                latch.await(ASYNC_TIMEOUT, TimeUnit.MILLISECONDS)
            } catch (e: Exception) {
                Log.warning(
                    LaunchRulesEngineConstants.LOG_TAG,
                    logTag,
                    "Event History operation for id ${consequence.id} - Unable to retrieve historical events, caused by the exception: ${e.localizedMessage}"
                )
                return
            }
            if (eventCounts == -1) {
                Log.trace(
                    LaunchRulesEngineConstants.LOG_TAG,
                    logTag,
                    "Event History operation for id ${consequence.id} - Unable to retrieve historical events due to database error, skipping 'insertIfNotExists' operation"
                )
                return
            }
            if (eventCounts >= 1) {
                Log.trace(
                    LaunchRulesEngineConstants.LOG_TAG,
                    logTag,
                    "Event History operation for id ${consequence.id} - Event already exists in history, skipping 'insertIfNotExists' operation"
                )
                return
            }
        }
        Log.trace(
            LaunchRulesEngineConstants.LOG_TAG,
            logTag,
            "Event History operation for id ${consequence.id} - Recording event in history with operation '$operation'"
        )

        extensionApi.recordHistoricalEvent(
            eventToRecord,
            object : AdobeCallbackWithError<Boolean> {
                override fun call(result: Boolean) {
                    if (result) {
                        extensionApi.dispatch(eventToRecord)
                    } else {
                        Log.warning(
                            LaunchRulesEngineConstants.LOG_TAG,
                            logTag,
                            "Event History operation for id ${consequence.id} - Failed to record event in history for '$operation'"
                        )
                    }
                }

                override fun fail(error: AdobeError) {
                    Log.warning(
                        LaunchRulesEngineConstants.LOG_TAG,
                        logTag,
                        "Event History operation for id ${consequence.id} - Failed to record event in history for '$operation', caused by the error: ${error.errorName}"
                    )
                }
            }
        )
    }
}

// Extend RuleConsequence with helper methods for processing consequence events.
private val RuleConsequence.eventData: Map<*, *>?
    get() = detail?.get("eventdata") as? Map<*, *>

private val RuleConsequence.eventSource: String?
    get() = detail?.get("source") as? String

private val RuleConsequence.eventType: String?
    get() = detail?.get("type") as? String

private val RuleConsequence.eventDataAction: String?
    get() = detail?.get("eventdataaction") as? String

private val RuleConsequence.detailId: String?
    get() = detail?.get("id") as? String

private val RuleConsequence.schema: String?
    get() = detail?.get("schema") as? String

private val RuleConsequence.detailData: Map<String, Any>?
    get() = DataReader.optTypedMap(Any::class.java, detail, "data", null)
