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

import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.rulesengine.*

/**
 * The class representing a matcher condition
 */
internal class MatcherCondition(val definition: JSONDefinition) : JSONCondition() {

    companion object {
        private const val LOG_TAG = "MatcherCondition"
        private const val OPERATION_NAME_OR = "or"
        private val MATCHER_MAPPING = mapOf(
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
            MobileCore.log(
                LoggingMode.ERROR,
                LOG_TAG,
                "[key] or [matcher] is not String, failed to build Evaluable from definition JSON: \n $definition"
            )
            return null
        }
        val values: List<Any?> = definition.values ?: listOf()
        return when (values.size) {
            0 -> convert(definition.key, definition.matcher, "")
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
            MobileCore.log(
                LoggingMode.ERROR,
                LOG_TAG,
                "Failed to build Evaluable from [type:matcher] json, [definition.matcher = $matcher] is not supported."
            )
            return null
        }
        val javaClass: Any? = when (value) {
            is String -> String::class.java
            is Int -> Number::class.java
            is Double -> Number::class.java
            //note: Kotlin.Boolean is not mapped to java.lang.Boolean correctly
            is Boolean -> java.lang.Boolean::class.java
            is Float -> Number::class.java
            else -> null
        }
        return if (javaClass != null) {
            ComparisonExpression(
                OperandMustacheToken("{{$key}}", javaClass as Class<*>),
                operationName,
                OperandLiteral(value)
            )
        } else {
            null
        }
    }
}
