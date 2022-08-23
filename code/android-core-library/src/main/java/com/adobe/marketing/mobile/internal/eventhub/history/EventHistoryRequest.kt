package com.adobe.marketing.mobile.internal.eventhub.history

import com.adobe.marketing.mobile.internal.utility.StringEncoder

/**
 *  Used for selecting or deleting Events from Event History.
 *
 * @property mask Key-value pairs that will be used to generate the hash when looking up an Event.
 * @property fromDate Date that represents the lower bounds of the date range used when looking up an Event. If not provided, the lookup will use the beginning of Event History as the lower bounds.
 * @property toDate Date that represents the upper bounds of the date range used when looking up an Event. If not provided, there will be no upper bound on the date range.
 */
data class EventHistoryRequest(
    val mask: Map<String, Any?>,
    val fromDate: Long,
    val toDate: Long
) {
    @JvmName("getMaskAsDecimalHash")
    internal fun getMaskAsDecimalHash(): Long {
        val kvpStringBuilder = StringBuilder()
        mask.forEach { entry ->
            kvpStringBuilder.append(entry.key + ":" + convertToString(entry.value))
        }
        return StringEncoder.convertStringToDecimalHash(kvpStringBuilder.toString())
    }
}

private fun convertToString(value: Any?): String {
    if (value == null) return ""
    return when (value) {
        is String -> value
        is Int -> value.toString()
        is Char -> value.toString()
        is Float -> value.toString()
        is Double -> value.toString()
        is Boolean -> value.toString()
        else -> value.toString()
    }
}
