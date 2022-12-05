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

package com.adobe.marketing.mobile.internal.util

import org.json.JSONArray
import org.json.JSONObject

/**
 * Returns a list containing the results of applying the given [transform] function
 * to each element in the [JSONArray].
 *
 */
@JvmSynthetic
internal fun <T> JSONArray.map(transform: (Any) -> T): List<T> {
    return (0 until this.length()).asSequence().map { transform(this.get(it)) }.toList()
}

/**
 * Converts the [JSONObject] to a map of the contained contents.
 *
 */
@JvmSynthetic
internal fun JSONObject.toMap(): Map<String, Any?> {
    return this.keys().asSequence().associateWith { key ->
        when (val value = this.get(key)) {
            is JSONObject -> {
                value.toMap()
            }
            is JSONArray -> {
                value.toList()
            }
            JSONObject.NULL -> null
            else -> value
        }
    }
}

/**
 * Converts the [JSONArray] to a list of the contained contents,
 * the list could contains [JSONObject], [JSONArray], `null` or the `primitive types`.
 *
 */
@JvmSynthetic
internal fun JSONArray.toList(): List<Any?> {
    val list = mutableListOf<Any?>()
    (0 until this.length()).forEach { index ->
        when (val value = this.get(index)) {
            is JSONObject -> {
                list.add(value.toMap())
            }
            is JSONArray -> {
                list.add(value.toList())
            }
            JSONObject.NULL -> list.add(null)
            else -> list.add(value)
        }
    }
    return list
}
