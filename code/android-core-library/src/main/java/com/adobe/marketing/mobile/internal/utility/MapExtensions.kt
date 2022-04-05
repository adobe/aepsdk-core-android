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
internal fun Map<String, Any?>.fnv1a32(): Long {
    val flattenedMap = this.flattening()

    return 0
}

@JvmSynthetic
internal fun Map<String, Any?>.flattening(prefix: String = ""): Map<String, Any?> {
    val keyPrefix = if (prefix.isNotEmpty()) "$prefix." else prefix
    val flattenedMap = mutableMapOf<String, Any?>()
    this.forEach { entry ->
        val expandedKey = keyPrefix + entry.key
        val value = entry.value
        if (value is Map<*, *>) {
            try {
                @Suppress("UNCHECKED_CAST")
                flattenedMap.putAll((value as Map<String, Any?>).flattening(expandedKey))
            } catch (e: Exception) {
                // logging errors
            }
        } else {
            flattenedMap[expandedKey] = value
        }
    }
    return flattenedMap
}

