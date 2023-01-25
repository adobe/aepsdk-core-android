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

internal object EventDataMerger {
    private const val WILD_CARD_SUFFIX_FOR_LIST = "[*]"

    /**
     * Merge one [Map] into another
     *
     * @param from the map containing new data
     * @param to the map to be merged to
     * @param overwrite true, if the from map should take priority
     * @return the merged [Map]
     */
    @JvmStatic
    fun merge(
        from: Map<String, Any?>?,
        to: Map<String, Any?>?,
        overwrite: Boolean
    ): Map<String, Any?> {
        return innerMerge(
            from,
            to,
            overwrite,
            fun(fromValue, toValue): Any? {
                if (fromValue is Map<*, *> && toValue is Map<*, *>) {
                    return mergeWildcardMaps(fromValue, toValue, overwrite)
                }
                if (!overwrite) {
                    return toValue
                }
                if (fromValue is Collection<*> && toValue is Collection<*>) {
                    return mergeCollection(fromValue, toValue)
                }
                return fromValue
            }
        )
    }

    private fun mergeCollection(from: Collection<*>?, to: Collection<*>?): Collection<Any?> {
        return object : ArrayList<Any?>() {
            init {
                from?.let { addAll(it) }
                to?.let { addAll(it) }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun mergeWildcardMaps(
        from: Map<*, *>?,
        to: Map<*, *>?,
        overwrite: Boolean
    ): Map<*, *>? {
        from?.let { if (!it.keys.isAllString()) return to }
        to?.let { if (!it.keys.isAllString()) return to }
        return try {
            merge(from as? Map<String, Any?>, to as? Map<String, Any?>, overwrite)
        } catch (e: Exception) {
            to
        }
    }

    private fun innerMerge(
        from: Map<String, Any?>?,
        to: Map<String, Any?>?,
        overwrite: Boolean,
        overwriteStrategy: (fromValue: Any?, toValue: Any?) -> Any?
    ): Map<String, Any?> {
        val mergedMap = HashMap<String, Any?>()
        to?.let(mergedMap::putAll)
        from?.forEach { (k, v) ->
            when {
                mergedMap.containsKey(k) -> {
                    val resolvedValue = overwriteStrategy(v, mergedMap[k])
                    resolvedValue?.let { mergedMap[k] = resolvedValue } ?: mergedMap.remove(k)
                }
                k.endsWith(WILD_CARD_SUFFIX_FOR_LIST) -> {
                    if (v is Map<*, *>) handleWildcardMerge(mergedMap, k, v, overwrite)
                }
                else -> {
                    mergedMap[k] = v
                }
            }
        }
        return mergedMap
    }

    /**
     * If the map contains the specific key and its value is a [Collection], merge `data` to each item of the [Collection]
     *
     * For example:
     * for the wildcard key: `list[*]`, merge the following data:
     *      {"k":"v"}
     * to the target [Map]:
     *     {"list":[{"k1":"v1"},{"k2":"v2"}]}
     * the result is:
     *     {"list":[{"k1":"v1","k":"v"},{"k2":"v2","k":"v"}]}
     *
     * @param targetMap the [Map] to be merged with the given `data` if it contains the target key
     * @param wildcardKey the target key with suffix `[*]`
     * @param data the new data to be merged to the `targetMap`
     * @param overwrite true, if the new data should take priority
     */
    private fun handleWildcardMerge(
        targetMap: HashMap<String, Any?>,
        wildcardKey: String,
        data: Map<*, *>,
        overwrite: Boolean
    ) {
        val targetKey = wildcardKey.dropLast(WILD_CARD_SUFFIX_FOR_LIST.length)
        val targetValueAsList = targetMap[targetKey]
        if (targetValueAsList is Collection<*>) {
            val newList = ArrayList<Any?>()
            targetValueAsList.forEach {
                val itMap = it as? Map<*, *>
                itMap?.let { newList.add(mergeWildcardMaps(data, itMap, overwrite)) }
                    ?: run { newList.add(it) }
            }
            targetMap[targetKey] = newList
        }
    }
}
