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

import com.adobe.marketing.mobile.internal.util.UrlEncoder
import com.adobe.marketing.mobile.rulesengine.Transformer
import com.adobe.marketing.mobile.rulesengine.TransformerBlock
import com.adobe.marketing.mobile.rulesengine.Transforming

internal object LaunchRuleTransformer {

    /**
     * Generates the [Transforming] instance used by Launch Rules Engine.
     *
     * @return instance of [Transforming]
     **/
    fun createTransforming(): Transforming {
        val transformer = Transformer()
        addConsequenceTransform(transformer)
        addTypeTransform(transformer)
        return transformer
    }

    /**
     * Registers a [TransformerBlock] for [LaunchRulesEngineConstants.Transform.URL_ENCODING_FUNCTION]
     * to encode a `String` value to url format.
     *
     * @param[transformer] [Transformer] instance used to register the [TransformerBlock]
     */
    private fun addConsequenceTransform(transformer: Transformer) {
        transformer.register(LaunchRulesEngineConstants.Transform.URL_ENCODING_FUNCTION) { value ->
            if (value is String) {
                UrlEncoder.urlEncode(value)
            } else {
                value
            }
        }
    }

    /**
     * Registers multiple [TransformerBlock] to transform a value into one of
     * [LaunchRulesEngineConstants.Transform] types.
     *
     * @param[transformer] [Transformer] instance used to register the [TransformerBlock]
     */
    private fun addTypeTransform(transformer: Transformer) {
        transformer.register(LaunchRulesEngineConstants.Transform.TRANSFORM_TO_INT) { value ->
            when (value) {
                is String -> {
                    try {
                        value.toInt()
                    } catch (e: NumberFormatException) {
                        value
                    }
                }
                is Number -> value.toInt()
                is Boolean -> if (value) 1 else 0
                else -> value
            }
        }
        transformer.register(LaunchRulesEngineConstants.Transform.TRANSFORM_TO_STRING) { value ->
            value?.toString()
        }
        transformer.register(LaunchRulesEngineConstants.Transform.TRANSFORM_TO_DOUBLE) { value ->
            when (value) {
                is String -> {
                    try {
                        value.toDouble()
                    } catch (e: NumberFormatException) {
                        value
                    }
                }
                is Number -> value.toDouble()
                is Boolean -> if (value) 1.0 else 0.0
                else -> value
            }
        }
        transformer.register(LaunchRulesEngineConstants.Transform.TRANSFORM_TO_BOOL) { value ->
            when (value) {
                is String -> java.lang.Boolean.parseBoolean(value)
                is Number -> (value.toLong() == 1L && value.toDouble() == 1.0)
                else -> value
            }
        }
    }
}
