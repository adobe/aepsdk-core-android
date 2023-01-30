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

import com.adobe.marketing.mobile.launch.rulesengine.LaunchRulesEngineConstants
import com.adobe.marketing.mobile.rulesengine.Evaluable
import com.adobe.marketing.mobile.rulesengine.LogicalExpression
import com.adobe.marketing.mobile.services.Log
import java.util.Locale

/**
 * The class representing a group of [JSONCondition]s
 */
internal class GroupCondition(private val definition: JSONDefinition) : JSONCondition() {
    companion object {
        private const val LOG_TAG = "GroupCondition"
        private val LOGICAL_OPERATORS = listOf("or", "and")
    }

    @JvmSynthetic
    override fun toEvaluable(): Evaluable? {
        if (definition.logic !is String || definition.conditions !is List<*> || definition.conditions.isEmpty()) {
            return null
        }
        val logicalOperator = definition.logic.toLowerCase(Locale.ROOT)

        if (logicalOperator !in LOGICAL_OPERATORS) {
            Log.error(
                LaunchRulesEngineConstants.LOG_TAG,
                LOG_TAG,
                "Unsupported logical operator: $logicalOperator"
            )
            return null
        }
        val evaluableList = definition.conditions.map { it.toEvaluable() }
        return LogicalExpression(evaluableList, logicalOperator)
    }
}
