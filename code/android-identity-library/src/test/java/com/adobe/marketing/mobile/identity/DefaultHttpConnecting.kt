package com.adobe.marketing.mobile.identity

import com.adobe.marketing.mobile.services.HttpConnecting
import java.io.InputStream

internal open class DefaultHttpConnecting : HttpConnecting {
    override fun getInputStream(): InputStream? {
        return null
    }

    override fun getErrorStream(): InputStream? {
        return null
    }

    override fun getResponseCode(): Int {
        return 0
    }

    override fun getResponseMessage(): String {
        return ""
    }

    override fun getResponsePropertyValue(responsePropertyKey: String?): String {
        return ""
    }

    override fun close() {}
}