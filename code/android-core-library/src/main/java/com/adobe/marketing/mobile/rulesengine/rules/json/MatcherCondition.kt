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

package com.adobe.marketing.mobile.rulesengine.rules.json

import com.adobe.marketing.mobile.rulesengine.*

internal class MatcherCondition(val definition: JSONDefinition) : JSONCondition() {

    companion object {
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

    override fun toEvaluable(): Evaluable? {
        if (definition.matcher !is String || definition.key !is String) {
            //TODO: logging error
            return null
        }
        // || definition.values !is List<*>
        val values: List<Any?> = definition.values ?: listOf()
        return when (values.size) {
            0 -> convert(definition.key, definition.matcher, "")
            1 -> convert(definition.key, definition.matcher, values[0])
            in 1..Int.MAX_VALUE -> {
                val operands = values.map { convert(definition.key, definition.matcher, it) }
                if (operands.isEmpty()) null else LogicalExpression(operands, "or")
            }
            else -> null
        }
    }

    private fun convert(key: String, matcher: String, value: Any?): Evaluable? {
        val operationName = MATCHER_MAPPING[matcher]
        if (operationName !is String) {
            //TODO: logging error
            return null
        }
        return when (value) {
            is String -> ComparisonExpression(
                //TODO: ?????
                OperandMustacheToken("{{$key}}", String.javaClass),
                operationName,
                OperandLiteral(value)
            )
            is Int -> ComparisonExpression(
                OperandMustacheToken("{{$key}}", Int.javaClass),
                operationName,
                OperandLiteral(value)
            )
            is Double -> ComparisonExpression(
                OperandMustacheToken("{{$key}}", Double.javaClass),
                operationName,
                OperandLiteral(value)
            )
            is Boolean -> ComparisonExpression(
                OperandMustacheToken("{{$key}}", Boolean.javaClass),
                operationName,
                OperandLiteral(value)
            )
            is Float -> ComparisonExpression(
                OperandMustacheToken("{{$key}}", Float.javaClass),
                operationName,
                OperandLiteral(value)
            )
            else -> null
        }
    }
}
