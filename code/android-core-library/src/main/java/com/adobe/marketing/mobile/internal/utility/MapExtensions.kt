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

import com.adobe.marketing.mobile.internal.utility.UrlUtilities.urlEncode

/**
 * Convert map to a decimal FNV1a 32-bit hash. If a mask is provided, only use keys in the provided mask and alphabetize their order.
 *
 * @param masks contain keys to be hashed.
 * @return the decimal FNV1a 32-bit hash.
 */
@JvmSynthetic
internal fun Map<String, Any?>.fnv1a32(masks: Array<String>? = null): Long {
    val flattenedMap = this.flattening()
    val kvPairs = StringBuilder()
    var innerMasks = masks
    if (innerMasks?.isEmpty() == true) innerMasks = null
    innerMasks?.let {
        it.sortedArray().forEach { mask ->
            if (mask.isNotEmpty() && flattenedMap.containsKey(mask)) {
                kvPairs.append(mask).append(":").append(flattenedMap[mask].toString())
            }
        }
    } ?: run {
        flattenedMap.toSortedMap().forEach { entry ->
            kvPairs.append(entry.key).append(":").append(entry.value.toString())
        }
    }
    return StringEncoder.convertStringToDecimalHash(kvPairs.toString())
}

/**
 * Flatten nested [Map]s and concatenate [String] keys
 * For example, an input [Map] of:
 *  `[rootKey: [key1: value1, key2: value2]]`
 * will return a [Map] represented as:
 *  `[rootKey.key1: value1, rootKey.key2: value2]`
 *
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

// TODO need to decide if we need to keep top level keys or implement similar to iOS
/*
/**
 * Returns a [Map] which has been flattened in the following way:
 * The Keys are of the format "eventdataKey"."nestedMapKeyOrFlatDictionaryKey"....
 * So For example, a [Map] in the following format:
 * `[mapKey: [key1: value1], key2: value2]`
 * will be flattened to
 * `mapKey: [key1: value1], mapKey.key1: value1, key2: value2]`
 *
 * @param prefix a prefix to append to the front of the key
 * @return Flattened KV pairs
 */
@JvmSynthetic
internal fun Map<String, Any?>.getFlattenedDataMap(prefix: String = ""): Map<String, Any?> {
    val keyPrefix = if (prefix.isNotEmpty()) "$prefix." else prefix
    val flattenedMap = mutableMapOf<String, Any?>()
    this.forEach { entry ->
        val expandedKey = keyPrefix + entry.key
        val value = entry.value
        flattenedMap[expandedKey] = value
        if (value is Map<*, *> && value.keys.isAllString()) {
            @Suppress("UNCHECKED_CAST")
            flattenedMap.putAll((value as Map<String, Any?>).flattening(expandedKey))
        }
    }
    return flattenedMap
}*/

/**
 * Serializes a map to key value pairs for url string.
 * This method is recursive to handle the nested data objects.
 *
 * @return resulted serialized query parameters as [String]
 */
@JvmSynthetic
internal fun Map<String, Any?>.serializeToQueryString(): String {
    val builder = StringBuilder()
    for ((key, value) in this.entries) {
        val encodedKey = urlEncode(key) ?: continue
        var encodedValue: String?

        // TODO add serializing for custom objects
        encodedValue = if (value is List<*>) {
            urlEncode(join(value, ","))
        } else {
            urlEncode(value?.toString())
        }

        val serializedKVP = serializeKeyValuePair(encodedKey, encodedValue)
        if (serializedKVP != null) {
            builder.append(serializedKVP)
        }
    }

    return if (builder.isNotEmpty()) builder.substring(1).toString() else builder.toString()
}

private fun Set<*>.isAllString(): Boolean {
    this.forEach {
        if (it !is String) return false
    }
    return true
}

/**
 * Encodes the key/value pair and prepares it in the URL format.
 * If the value is a List, it will create a join string with the "," delimiter before encoding,
 * otherwise it will use toString method on other objects.
 *
 * @param key the string value that we want to append to the builder
 * @param value the object value that we want to encode and append to the builder
 * @return [String] containing key/value pair encoded in URL format
 */
private fun serializeKeyValuePair(key: String?, value: String?): String? {
    if (key.isNullOrBlank() || value == null) {
        return null
    }
    return "&$key=$value"
}

/**
 * Returns a [String] containing the elements joined by delimiters.
 *
 * @param elements an array objects to be joined. A [String] will be formed from the objects
 * by calling object.toString().
 * @param delimiter the `String` to be used as the delimiter between all elements
 * @return [String] containing the elements joined by delimiters
 */
fun join(elements: Iterable<*>, delimiter: String?): String {
    val sBuilder = java.lang.StringBuilder()
    val iterator = elements.iterator()

    // TODO: consider breaking on null items, otherwise we end up with sample1,null,sample3 instead of sample1,sample3
    while (iterator.hasNext()) {
        sBuilder.append(iterator.next())
        if (iterator.hasNext()) {
            sBuilder.append(delimiter)
        }
    }
    return sBuilder.toString()
}
