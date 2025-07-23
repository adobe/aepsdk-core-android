/*
  Copyright 2025 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.internal.util

/**
 * Recursively flattens nested [Map]s, [List]s and [Array]s values inside a List<Any?> into a
 * single-level Map<String, Any?> with dot-separated index.
 * Other nested values are added as objects without any flattening.
 * For example, an input [List] of:
 *  `[["key": "value"],
 *    [value0, value1]]`
 * will return a [Map] represented as:
 *  `{0.key: value, 1.0: value0, 1.1: value1}`
 *
 *  Key names are not escaped; if flattened keys collide, the last value written wins.
 *  The resolution order in this case is undefined and may change.
 *
 * @param prefix a prefix to append to the front of the key
 * @return flattened [Map]
 */
@JvmSynthetic
internal fun List<Any?>.flattening(prefix: String = ""): Map<String, Any?> {
    val flattenedMap = mutableMapOf<String, Any?>()
    this.forEachIndexed { index, item ->
        val expandedKey = if (prefix.isNotEmpty()) "$prefix.$index" else "$index"
        if (item is Map<*, *> && item.keys.isAllString()) {
            @Suppress("UNCHECKED_CAST")
            flattenedMap.putAll((item as Map<String, Any?>).flattening(expandedKey))
        } else if (item is List<*>) {
            flattenedMap.putAll((item as List<Any?>).flattening(prefix = expandedKey))
        } else if (item is Array<*>) {
            flattenedMap.putAll((item as Array<out Any?>).flattening(prefix = expandedKey))
        } else {
            flattenedMap[expandedKey] = item
        }
    }
    return flattenedMap
}

/**
 * Recursively flattens nested [Map]s, [List]s and [Array]s values inside an Array<outAny?> into a
 * single-level Map<String, Any?> with dot-separated index.
 */
@JvmSynthetic
internal fun Array<out Any?>.flattening(prefix: String = ""): Map<String, Any?> = toList().flattening(prefix)
