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

package com.adobe.marketing.mobile.services.ui.notification.templates

import android.content.Intent
import com.adobe.marketing.mobile.services.ui.notification.PushTemplateConstants
import com.adobe.marketing.mobile.util.DataReader

/**
 * This class is used to parse the push template data payload or an intent and provide the necessary information
 * to build a notification containing an input box.
 */
internal class InputBoxPushTemplate : AEPPushTemplate {
    // Required, the intent action name to be used when the user submits the feedback.
    internal var inputBoxReceiverName: String

    // Optional, If present, use it as the placeholder text for the text input field. Otherwise, use the default placeholder text of "Reply".
    internal var inputTextHint: String?
        private set

    // Optional, once feedback has been submitted, use this text as the notification's body
    internal var feedbackText: String?
        private set

    // Optional, once feedback has been submitted, use this as the notification's image
    internal var feedbackImage: String?
        private set

    constructor(data: Map<String, String>) : super(data) {
        inputBoxReceiverName = DataReader.optString(
            data, PushTemplateConstants.PushPayloadKeys.INPUT_BOX_RECEIVER_NAME, null
        )
            ?: throw IllegalArgumentException("Required field \"${PushTemplateConstants.PushPayloadKeys.INPUT_BOX_RECEIVER_NAME}\" not found.")
        inputTextHint = DataReader.optString(
            data, PushTemplateConstants.PushPayloadKeys.INPUT_BOX_HINT, null
        )
        feedbackText = DataReader.optString(
            data, PushTemplateConstants.PushPayloadKeys.INPUT_BOX_FEEDBACK_TEXT, null
        )
        feedbackImage = DataReader.optString(
            data, PushTemplateConstants.PushPayloadKeys.INPUT_BOX_FEEDBACK_IMAGE, null
        )
    }

    constructor(intent: Intent) : super(intent) {
        val intentExtras =
            intent.extras ?: throw IllegalArgumentException("Intent extras are null")
        val receiverName =
            intentExtras.getString(PushTemplateConstants.IntentKeys.INPUT_BOX_RECEIVER_NAME)
        inputBoxReceiverName = receiverName
            ?: throw IllegalArgumentException("Required field \"${PushTemplateConstants.IntentKeys.INPUT_BOX_RECEIVER_NAME}\" not found.")
        inputTextHint = intentExtras.getString(
            PushTemplateConstants.IntentKeys.INPUT_BOX_HINT,
            PushTemplateConstants.DefaultValues.INPUT_BOX_DEFAULT_REPLY_TEXT
        )
        feedbackText =
            intentExtras.getString(PushTemplateConstants.IntentKeys.INPUT_BOX_FEEDBACK_TEXT)
        feedbackImage =
            intentExtras.getString(PushTemplateConstants.IntentKeys.INPUT_BOX_FEEDBACK_IMAGE)
    }
}
