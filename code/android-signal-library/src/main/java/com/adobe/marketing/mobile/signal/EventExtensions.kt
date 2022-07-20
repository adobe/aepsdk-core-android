package com.adobe.marketing.mobile.signal

import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.utils.DataReader

@JvmSynthetic
internal fun Event.isPostback(): Boolean {
    return this.consequenceType() == SignalConstants.EventDataKeys.Signal.RULES_RESPONSE_CONSEQUENCE_TYPE_POSTBACKS
}

@JvmSynthetic
internal fun Event.isOpenUrl(): Boolean {
    return this.consequenceType() == SignalConstants.EventDataKeys.Signal.RULES_RESPONSE_CONSEQUENCE_TYPE_OPEN_URL
}

@JvmSynthetic
internal fun Event.isCollectPii(): Boolean {
    return this.consequenceType() == SignalConstants.EventDataKeys.Signal.RULES_RESPONSE_CONSEQUENCE_TYPE_PII
}

@JvmSynthetic
internal fun Event.contentType(): String? {
    return try {
        DataReader.getString(this.details(), SignalConstants.EventDataKeys.RuleEngine.CONTENT_TYPE)
    } catch (e: Exception) {
        null
    }
}

@JvmSynthetic
internal fun Event.templateUrl(): String? {
    return try {
        DataReader.getString(this.details(), SignalConstants.EventDataKeys.RuleEngine.TEMPLATE_URL)
    } catch (e: Exception) {
        null
    }
}

@JvmSynthetic
internal fun Event.templateBody(): String? {
    return try {
        DataReader.getString(this.details(), SignalConstants.EventDataKeys.RuleEngine.TEMPLATE_BODY)
    } catch (e: Exception) {
        null
    }
}

@JvmSynthetic
internal fun Event.urlToOpen(): String? {
    return try {
        DataReader.getString(this.details(), SignalConstants.EventDataKeys.RuleEngine.URL)
    } catch (e: Exception) {
        null
    }
}

@JvmSynthetic
internal fun Event.timeout(): Int? {
    return try {
        DataReader.getInt(this.details(), SignalConstants.EventDataKeys.RuleEngine.TIMEOUT)
    } catch (e: Exception) {
        null
    }
}

private fun Event.consequence(): Map<String, Any>? {
    this.eventData ?: return null
    return try {
        DataReader.getTypedMap(
            Any::class.java,
            this.eventData,
            SignalConstants.EventDataKeys.RuleEngine.CONSEQUENCE_TRIGGERED
        )
    } catch (e: Exception) {
        null
    }
}

private fun Event.consequenceId(): String? {
    return try {
        DataReader.getString(
            this.consequence(),
            SignalConstants.EventDataKeys.RuleEngine.RULES_RESPONSE_CONSEQUENCE_KEY_ID
        )
    } catch (e: Exception) {
        null
    }
}

private fun Event.details(): Map<String, Any>? {
    return try {
        DataReader.getTypedMap(
            Any::class.java,
            this.consequence(),
            SignalConstants.EventDataKeys.RuleEngine.RULES_RESPONSE_CONSEQUENCE_KEY_DETAIL
        )
    } catch (e: Exception) {
        null
    }
}

private fun Event.consequenceType(): String? {
    return try {
        DataReader.getString(
            this.consequence(),
            SignalConstants.EventDataKeys.RuleEngine.RULES_RESPONSE_CONSEQUENCE_KEY_TYPE
        )
    } catch (e: Exception) {
        null
    }
}
