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

package com.adobe.marketing.mobile.signal.internal

import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.util.DataReader

// extends Event class represented as a rule consequence Event for Signal extension

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
internal fun Event.contentType(): String {
    return DataReader.optString(
        this.details(),
        SignalConstants.EventDataKeys.RuleEngine.CONTENT_TYPE,
        ""
    )
}

@JvmSynthetic
internal fun Event.templateUrl(): String? {
    return DataReader.optString(
        this.details(),
        SignalConstants.EventDataKeys.RuleEngine.TEMPLATE_URL,
        null
    )
}

@JvmSynthetic
internal fun Event.templateBody(): String? {
    return DataReader.optString(
        this.details(),
        SignalConstants.EventDataKeys.RuleEngine.TEMPLATE_BODY,
        null
    )
}

@JvmSynthetic
internal fun Event.urlToOpen(): String? {
    return DataReader.optString(this.details(), SignalConstants.EventDataKeys.RuleEngine.URL, null)
}

@JvmSynthetic
internal fun Event.timeout(): Int {
    return DataReader.optInt(this.details(), SignalConstants.EventDataKeys.RuleEngine.TIMEOUT, 0)
}

private fun Event.consequence(): Map<String, Any>? {
    this.eventData ?: return null
    return DataReader.optTypedMap(
        Any::class.java,
        this.eventData,
        SignalConstants.EventDataKeys.RuleEngine.CONSEQUENCE_TRIGGERED,
        null
    )
}

private fun Event.consequenceId(): String? {
    return DataReader.optString(
        this.consequence(),
        SignalConstants.EventDataKeys.RuleEngine.RULES_RESPONSE_CONSEQUENCE_KEY_ID,
        null
    )
}

private fun Event.details(): Map<String, Any>? {
    return DataReader.optTypedMap(
        Any::class.java,
        this.consequence(),
        SignalConstants.EventDataKeys.RuleEngine.RULES_RESPONSE_CONSEQUENCE_KEY_DETAIL,
        null
    )
}

private fun Event.consequenceType(): String? {
    return DataReader.optString(
        this.consequence(),
        SignalConstants.EventDataKeys.RuleEngine.RULES_RESPONSE_CONSEQUENCE_KEY_TYPE,
        null
    )
}
