/*
  Copyright 2024 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.services.ui.notification.models

import android.content.Intent
import com.adobe.marketing.mobile.services.ui.notification.PushTemplateConstants
import com.adobe.marketing.mobile.util.DataReader

internal class BasicPushTemplate : AEPPushTemplate {
    // Optional, If present, show a "remind later" button using the value provided as its label
    internal var remindLaterText: String?
        private set

    // Optional, If present, schedule this notification to be re-delivered at this epoch timestamp (in seconds) provided.
    internal var remindLaterEpochTimestamp: Long?
        private set

    // Optional, If present, schedule this notification to be re-delivered after this provided time (in seconds).
    internal var remindLaterDelaySeconds: Int?
        private set

    constructor(data: Map<String, String>) : super(data) {
        remindLaterText = DataReader.optString(
            data, PushTemplateConstants.PushPayloadKeys.REMIND_LATER_TEXT, null
        )
        val epochTimestampString = DataReader.optString(
            data, PushTemplateConstants.PushPayloadKeys.REMIND_LATER_EPOCH_TIMESTAMP, null
        )
        remindLaterEpochTimestamp =
            if (epochTimestampString.isNullOrEmpty()) null else epochTimestampString.toLong()

        val delaySeconds = DataReader.optString(
            data,
            PushTemplateConstants.PushPayloadKeys.REMIND_LATER_DELAY_SECONDS, null
        )
        remindLaterDelaySeconds = if (delaySeconds.isNullOrEmpty()) null else delaySeconds.toInt()
    }

    constructor(intent: Intent) : super(intent) {
        val intentExtras =
            intent.extras ?: throw IllegalArgumentException("Intent extras are null")
        remindLaterEpochTimestamp =
            intentExtras.getLong(PushTemplateConstants.IntentKeys.REMIND_EPOCH_TS)
        remindLaterDelaySeconds =
            intentExtras.getInt(PushTemplateConstants.IntentKeys.REMIND_DELAY_SECONDS)
        remindLaterText =
            intentExtras.getString(PushTemplateConstants.IntentKeys.REMIND_LABEL)
    }
}
