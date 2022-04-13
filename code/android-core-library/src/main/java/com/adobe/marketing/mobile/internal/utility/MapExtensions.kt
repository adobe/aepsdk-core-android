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

package com.adobe.marketing.mobile.internal.utility

@JvmSynthetic
internal fun Map<String, Any?>.fnv1a32(masks: Array<String>? = null): Long {
    val flattenedMap = this.flattening()
    val kvPairs = StringBuilder()
    masks?.let {
        it.sortedArray().forEach { mask ->
            if (mask.isNotEmpty() && flattenedMap.containsKey(mask)) {
                kvPairs.append(mask).append(":").append(flattenedMap[mask].toString())
            }
        }
    } ?: run {
        flattenedMap.forEach { entry ->
            kvPairs.append(entry.key).append(":").append(entry.value.toString())
        }
    }
    return StringEncoder.convertStringToDecimalHash(kvPairs.toString())
}

/**
 * Return a `flattened` [Map] which will not contain any [Map<String, Any?>] map
 * For example, an input [Map] of:
 *  `[rootKey: [key1: value1, key2: value2]]`
 * will return a [Map] represented as:
 *  `[rootKey.key1: value1, rootKey.key2: value2]`
 *
 *  This method uses recursion.
 *
 * @param prefix a prefix to append to the front of the key
 * @return flattened [Map]
 */
@JvmSynthetic
internal fun Map<String, Any?>.flattening(prefix: String = ""): Map<String, Any?> {
    val keyPrefix = if (prefix.isNotEmpty()) "$prefix." else prefix
    val flattenedMap = mutableMapOf<String, Any?>()
    this.forEach { entry ->
        val expandedKey = keyPrefix + entry.key
        val value = entry.value
        if (value is Map<*, *> && value.keys.isAllString()) {
            @Suppress("UNCHECKED_CAST")
            flattenedMap.putAll((value as Map<String, Any?>).flattening(expandedKey))
        } else {
            flattenedMap[expandedKey] = value
        }
    }
    return flattenedMap
}

private fun Set<*>.isAllString(): Boolean {
    this.forEach {
        if (it !is String) return false
    }
    return true
}
