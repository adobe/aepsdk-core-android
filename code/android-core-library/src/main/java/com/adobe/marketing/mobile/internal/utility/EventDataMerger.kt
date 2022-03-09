package com.adobe.marketing.mobile.internal.utility

object EventDataMerger {

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun merge(
        from: Map<String, Any?>?,
        to: Map<String, Any?>?,
        overwrite: Boolean
    ): Map<String, Any?>? {
        return merge(from, to, fun(fromValue, toValue): Any? {
            if (!overwrite) {
                return toValue
            }
            if (fromValue is Map<*, *> && toValue is Map<*, *>) {
                return merge(
                    fromValue as? MutableMap<String, Any?>,
                    toValue as? MutableMap<String, Any?>,
                    overwrite
                )
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

    @JvmStatic
    private fun merge(
        from: Map<String, Any?>?,
        to: Map<String, Any?>?,
        overwriteStrategy: (fromValue: Any?, toValue: Any?) -> Any?
    ): Map<String, Any?> {
        val returnMap = HashMap<String, Any?>()
        to?.let(returnMap::putAll)
        from?.forEach { (k, v) ->
            if (returnMap.containsKey(k)) {
                val resolvedValue = overwriteStrategy(v, returnMap[k])
                resolvedValue?.let {returnMap[k] = resolvedValue} ?: returnMap.remove(k)
            } else {
                returnMap[k] = v
            }
        }
        return returnMap
    }

}