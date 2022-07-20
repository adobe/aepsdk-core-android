package com.adobe.marketing.mobile.signal

import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.LoggingMode

internal object Log {
    const val UNEXPECTED_NULL_VALUE = "Unexpected Null Value"
    @JvmStatic
    fun trace(source: String?, format: String?, vararg params: Any?) {
        MobileCore.log(LoggingMode.VERBOSE, source, String.format(format!!, *params))
    }

    @JvmStatic
    fun debug(source: String?, format: String?, vararg params: Any?) {
        MobileCore.log(LoggingMode.DEBUG, source, String.format(format!!, *params))
    }

    @JvmStatic
    fun warning(source: String?, format: String?, vararg params: Any?) {
        MobileCore.log(LoggingMode.WARNING, source, String.format(format!!, *params))
    }

    fun error(source: String?, format: String?, vararg params: Any?) {
        MobileCore.log(LoggingMode.ERROR, source, String.format(format!!, *params))
    }
}