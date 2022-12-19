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

package com.adobe.marketing.mobile.launch.rulesengine.json

import com.adobe.marketing.mobile.EventHistoryRequest
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.launch.rulesengine.LaunchRulesEngineConstants
import com.adobe.marketing.mobile.launch.rulesengine.historicalEventsQuerying
import com.adobe.marketing.mobile.rulesengine.ComparisonExpression
import com.adobe.marketing.mobile.rulesengine.Evaluable
import com.adobe.marketing.mobile.rulesengine.OperandFunction
import com.adobe.marketing.mobile.rulesengine.OperandLiteral
import com.adobe.marketing.mobile.services.Log

internal class HistoricalCondition(
    private val definition: JSONDefinition,
    private val extensionApi: ExtensionApi
) : JSONCondition() {

    companion object {
        private const val LOG_TAG = "HistoricalCondition"
    }

    override fun toEvaluable(): Evaluable? {
        val valueAsInt = definition.value
        val operationName = MatcherCondition.MATCHER_MAPPING[definition.matcher]
        if (definition.events !is List<*> ||
            operationName !is String ||
            valueAsInt !is Int
        ) {
            Log.error(
                LaunchRulesEngineConstants.LOG_TAG,
                LOG_TAG,
                "Failed to build Evaluable from definition JSON: \n $definition"
            )
            return null
        }
        val fromDate = definition.from ?: 0
        val toDate = definition.to ?: 0
        val searchType = definition.searchType ?: "any"
        val requestEvents = definition.events.map {
            EventHistoryRequest(it, fromDate, toDate)
        }
        return ComparisonExpression(
            OperandFunction(
                {
                    try {
                        @Suppress("UNCHECKED_CAST")
                        historicalEventsQuerying(
                            it[0] as List<EventHistoryRequest>,
                            it[1] as String,
                            extensionApi
                        )
                    } catch (e: Exception) {
                        0
                    }
                },
                requestEvents,
                searchType
            ),
            operationName,
            OperandLiteral(valueAsInt)
        )
    }
}
