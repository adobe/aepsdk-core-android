/* ************************************************************************
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2022 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 **************************************************************************/

package com.adobe.marketing.mobile.launch.rulesengine

import com.adobe.marketing.mobile.internal.utility.UrlUtilities
import com.adobe.marketing.mobile.rulesengine.Transformer
import com.adobe.marketing.mobile.rulesengine.Transforming

/**
 * Generates the [Transforming] instance used by Launch Rules Engine.
 *
 * @return instance of [Transforming]
 **/
internal class LaunchRuleTransformer {

    companion object {
        fun createTransforming(): Transforming {
            val transformer = Transformer()
            addConsequenceTransform(transformer)
            addTypeTransform(transformer)
            return transformer
        }

        /**
         * Registers a [TransformerBlock] for [LaunchRulesConstants.Transform.URL_ENCODING_FUNCTION]
         * to encode a `String` value to url format.
         *
         * @param[transformer] [Transformer] instance used to register the [TransformerBlock]
         */
        private fun addConsequenceTransform(transformer: Transformer) {
            transformer.register(LaunchRulesConstants.Transform.URL_ENCODING_FUNCTION) { value ->
                if (value is String) {
                    UrlUtilities.urlEncode(value)
                } else value
            }
        }

        /**
         * Registers multiple [TransformerBlock] to transform a value into one of
         * [LaunchRulesConstants.Transform] types.
         *
         * @param[transformer] [Transformer] instance used to register the [TransformerBlock]
         */
        private fun addTypeTransform(transformer: Transformer) {
            transformer.register(LaunchRulesConstants.Transform.TRANSFORM_TO_INT) { value ->
                when (value) {
                    is String -> {
                        try {
                            value.toInt()
                        } catch (e: NumberFormatException) {
                            null
                        }
                    }
                    is Double -> value.toInt()
                    is Boolean -> if (value) 1 else 0
                    else -> value
                }
            }
            transformer.register(LaunchRulesConstants.Transform.TRANSFORM_TO_STRING) { value ->
                value?.toString()
            }
            transformer.register(LaunchRulesConstants.Transform.TRANSFORM_TO_DOUBLE) { value ->
                when (value) {
                    is String -> {
                        try {
                            value.toDouble()
                        } catch (e: NumberFormatException) {
                            null
                        }
                    }
                    is Int -> value.toDouble()
                    is Boolean -> if (value) 1.0 else 0.0
                    else -> value
                }
            }
            transformer.register(LaunchRulesConstants.Transform.TRANSFORM_TO_BOOL) { value ->
                when (value) {
                    is String -> java.lang.Boolean.parseBoolean(value)
                    is Int -> value == 1
                    is Double -> value == 1.0
                    else -> value
                }
            }
        }
    }
}