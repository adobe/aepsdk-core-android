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

internal object SignalConstants {
    const val EXTENSION_NAME = "com.adobe.module.signal"
    const val FRIENDLY_NAME = "Signal"
    const val LOG_TAG = "Signal"
    const val DEFAULT_NETWORK_TIMEOUT = 2
    const val DEPRECATED_1X_HIT_DATABASE_FILENAME = "ADBMobileSignalDataCache.sqlite"

    const val NETWORK_REQUEST_HEATER_CONTENT_TYPE = "Content-Type"
    val RECOVERABLE_ERROR_CODES = intArrayOf(408, 504, 503)
    val HTTP_SUCCESS_CODES = (200..299).toList().toIntArray()

    internal object EventDataKeys {

        internal object Configuration {
            const val MODULE_NAME = "com.adobe.module.configuration"
            const val GLOBAL_CONFIG_PRIVACY = "global.privacy"
        }

        internal object RuleEngine {
            const val RULES_RESPONSE_CONSEQUENCE_KEY_TYPE = "type"
            const val RULES_RESPONSE_CONSEQUENCE_KEY_ID = "id"
            const val RULES_RESPONSE_CONSEQUENCE_KEY_DETAIL = "detail"
            const val CONSEQUENCE_TRIGGERED = "triggeredconsequence"
            const val CONTENT_TYPE = "contenttype"
            const val TEMPLATE_BODY = "templatebody"
            const val TEMPLATE_URL = "templateurl"
            const val TIMEOUT = "timeout"
            const val URL = "url"
        }

        internal object Signal {
            const val RULES_RESPONSE_CONSEQUENCE_TYPE_POSTBACKS = "pb"
            const val RULES_RESPONSE_CONSEQUENCE_TYPE_PII = "pii"
            const val RULES_RESPONSE_CONSEQUENCE_TYPE_OPEN_URL = "url"
        }
    }
}
