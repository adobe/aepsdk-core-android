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
import com.adobe.marketing.mobile.rulesengine.ComparisonExpression
import com.adobe.marketing.mobile.rulesengine.Evaluable
import com.adobe.marketing.mobile.rulesengine.LogicalExpression
import com.adobe.marketing.mobile.rulesengine.OperandLiteral
import com.adobe.marketing.mobile.rulesengine.OperandMustacheToken
import com.adobe.marketing.mobile.rulesengine.UnaryExpression
import com.adobe.marketing.mobile.services.Log

/**
 * The class representing a matcher condition
 */
internal class MatcherCondition(private val definition: JSONDefinition) : JSONCondition() {

    companion object {
        private const val LOG_TAG = "MatcherCondition"
        private const val OPERATION_NAME_OR = "or"
        internal val MATCHER_MAPPING = mapOf(
            "eq" to "equals",
            "ne" to "notEquals",
            "gt" to "greaterThan",
            "ge" to "greaterEqual",
            "lt" to "lessThan",
            "le" to "lessEqual",
            "co" to "contains",
            "nc" to "notContains",
            "sw" to "startsWith",
            "ew" to "endsWith",
            "ex" to "exists",
            "nx" to "notExist"
        )
    }

    @JvmSynthetic
    override fun toEvaluable(): Evaluable? {
        if (definition.matcher !is String || definition.key !is String) {
            Log.error(
                LaunchRulesEngineConstants.LOG_TAG,
                LOG_TAG,
                "[key] or [matcher] is not String, failed to build Evaluable from definition JSON: \n $definition"
            )
            return null
        }
        val values: List<Any?> = definition.values ?: listOf()
        return when (values.size) {
            0 -> convert(definition.key, definition.matcher, null)
            1 -> convert(definition.key, definition.matcher, values[0])
            in 2..Int.MAX_VALUE -> {
                val operands = values.map { convert(definition.key, definition.matcher, it) }
                if (operands.isEmpty()) null else LogicalExpression(operands, OPERATION_NAME_OR)
            }
            else -> null
        }
    }

    private fun convert(key: String, matcher: String, value: Any?): Evaluable? {
        val operationName = MATCHER_MAPPING[matcher]
        if (operationName == null) {
            Log.error(
                LaunchRulesEngineConstants.LOG_TAG,
                LOG_TAG,
                "Failed to build Evaluable from [type:matcher] json, [definition.matcher = $matcher] is not supported."
            )
            return null
        }
        if (value == null) {
            return UnaryExpression(
                OperandMustacheToken("{{$key}}", Any::class.java as Class<*>),
                operationName
            )
        }
        val (javaClass: Any, token: String) = when (value) {
            is String -> Pair(
                String::class.java,
                "{{${LaunchRulesEngineConstants.Transform.TRANSFORM_TO_STRING}($key)}}"
            )
            is Int -> Pair(
                Number::class.java,
                "{{${LaunchRulesEngineConstants.Transform.TRANSFORM_TO_INT}($key)}}"
            )
            is Double -> Pair(
                Number::class.java,
                "{{${LaunchRulesEngineConstants.Transform.TRANSFORM_TO_DOUBLE}($key)}}"
            )
            // note: Kotlin.Boolean is not mapped to java.lang.Boolean correctly
            is Boolean -> Pair(
                java.lang.Boolean::class.java,
                "{{${LaunchRulesEngineConstants.Transform.TRANSFORM_TO_BOOL}($key)}}"
            )
            is Float -> Pair(
                Number::class.java,
                "{{${LaunchRulesEngineConstants.Transform.TRANSFORM_TO_DOUBLE}($key)}}"
            )
            else -> Pair(Any::class.java, "{{$key}}")
        }
        return ComparisonExpression(
            OperandMustacheToken(token, javaClass as Class<*>),
            operationName,
            OperandLiteral(value)
        )
    }
}
