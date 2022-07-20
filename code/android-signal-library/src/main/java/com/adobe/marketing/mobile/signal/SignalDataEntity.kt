package com.adobe.marketing.mobile.signal

import com.adobe.marketing.mobile.services.DataEntity
import org.json.JSONObject

private const val URL = "url"
private const val BODY = "body"
private const val CONTENT_TYPE = "contentType"
private const val TIME_OUT = "timeout"
private const val EMPTY_JSON = ""

@JvmSynthetic
internal fun generateDataEntity(
    url: String,
    body: String,
    contentType: String,
    timeout: Int
): DataEntity {
    val map = mapOf<String, Any>(
        URL to url,
        BODY to body,
        CONTENT_TYPE to contentType,
        TIME_OUT to timeout
    )
    val json = try {
        JSONObject(map).toString()
    } catch (e: Exception) {
        EMPTY_JSON
    }
    return DataEntity(json)
}

internal class SignalDataEntity(dataEntity: DataEntity) {
    private val jsonObject: JSONObject
    
    init {
        val json = dataEntity.data ?: EMPTY_JSON
        jsonObject = try {
            JSONObject(json)
        } catch (e: Exception) {
            JSONObject()
        }
    }

    @JvmSynthetic
    internal fun url(): String {
        return jsonObject.optString(URL)
    }

    @JvmSynthetic
    internal fun body(): String {
        return jsonObject.optString(BODY)
    }

    @JvmSynthetic
    internal fun contentType(): String {
        return jsonObject.optString(CONTENT_TYPE)
    }

    @JvmSynthetic
    internal fun timeout(default: Int): Int {
        return jsonObject.optInt(TIME_OUT, default)
    }
}



