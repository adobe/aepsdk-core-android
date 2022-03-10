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

object EventDataMerger {
    private const val WILD_CARD_SUFFIX_FOR_LIST = "[*]"

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun merge(
        from: Map<String, Any?>?,
        to: Map<String, Any?>?,
        overwrite: Boolean
    ): Map<String, Any?> {
        return merge(from, to, overwrite, fun(fromValue, toValue): Any? {
            if (fromValue is Map<*, *> && toValue is Map<*, *>) {
                return merge(
                    fromValue as? MutableMap<String, Any?>,
                    toValue as? MutableMap<String, Any?>,
                    overwrite
                )
            }
            if (!overwrite) {
                return toValue
            }
            if (fromValue is Collection<*> && toValue is Collection<*>) {
                return mergeCollection(
                    fromValue as? Collection<Any?>?,
                    toValue as? Collection<Any?>?
                )
            }
            return fromValue
        })
    }

    @JvmStatic
    private fun mergeCollection(from: Collection<Any?>?, to: Collection<Any?>?): Collection<Any?> {
        return object : ArrayList<Any?>() {
            init {
                from?.let { addAll(it) }
                to?.let { addAll(it) }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    private fun merge(
        from: Map<String, Any?>?,
        to: Map<String, Any?>?,
        overwrite: Boolean,
        overwriteStrategy: (fromValue: Any?, toValue: Any?) -> Any?
    ): Map<String, Any?> {
        val returnMap = HashMap<String, Any?>()
        to?.let(returnMap::putAll)
        from?.forEach { (k, v) ->
            if (returnMap.containsKey(k)) {
                val resolvedValue = overwriteStrategy(v, returnMap[k])
                resolvedValue?.let { returnMap[k] = resolvedValue } ?: returnMap.remove(k)
            } else if (k.endsWith(WILD_CARD_SUFFIX_FOR_LIST)) {
                val wildCardKey = k.dropLast(WILD_CARD_SUFFIX_FOR_LIST.length)
                if (returnMap[wildCardKey] is Collection<*> && v is Map<*, *>) {
                    val targetList = returnMap[wildCardKey] as? Collection<Any>
                    val wildCardValue = v as? Map<String, Any?>
                    wildCardValue?.let {
                        targetList?.let { list ->
                            val newList = ArrayList<Any>()
                            list.forEach {
                                val itMap = it as? Map<String, Any?>
                                itMap?.let { newList.add(merge(wildCardValue, itMap, overwrite)) }
                                    ?: run {
                                        newList.add(it)
                                    }
                            }
                            returnMap[wildCardKey] = newList
                        }
                    }

                }
            } else {
                returnMap[k] = v
            }
        }
        return returnMap
    }


}