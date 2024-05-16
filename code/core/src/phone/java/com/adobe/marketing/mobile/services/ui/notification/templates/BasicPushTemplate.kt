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
import androidx.annotation.VisibleForTesting
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ui.notification.PushTemplateConstants
import com.adobe.marketing.mobile.util.DataReader
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

internal class BasicPushTemplate : AEPPushTemplate {

    private val SELF_TAG = "BasicPushTemplate"

    /** Class representing the action button with label, link and type  */
    class ActionButton(val label: String, val link: String?, type: String?) {
        val type: PushTemplateConstants.ActionType

        init {
            this.type = try {
                PushTemplateConstants.ActionType.valueOf(
                    type ?: PushTemplateConstants.ActionType.NONE.name
                )
            } catch (e: IllegalArgumentException) {
                Log.warning(
                    PushTemplateConstants.LOG_TAG,
                    SELF_TAG,
                    "Invalid action button type provided, defaulting to NONE. Error : ${e.localizedMessage}"
                )
                PushTemplateConstants.ActionType.NONE
            }
        }

        companion object {
            private const val SELF_TAG = "ActionButton"

            /**
             * Converts the json object representing an action button to an [ActionButton].
             * Action button must have a non-empty label, type and uri
             *
             * @param jsonObject [JSONObject] containing the action button details
             * @return an [ActionButton] or null if the conversion fails
             */
            fun getActionButtonFromJSONObject(jsonObject: JSONObject): ActionButton? {
                return try {
                    val label = jsonObject.getString(PushTemplateConstants.ActionButtons.LABEL)
                    if (label.isEmpty()) {
                        Log.debug(
                            PushTemplateConstants.LOG_TAG,
                            SELF_TAG, "Label is empty"
                        )
                        return null
                    }
                    var uri: String? = null
                    val type = jsonObject.getString(PushTemplateConstants.ActionButtons.TYPE)
                    if (type == PushTemplateConstants.ActionType.WEBURL.name || type == PushTemplateConstants.ActionType.DEEPLINK.name) {
                        uri = jsonObject.optString(PushTemplateConstants.ActionButtons.URI)
                    }
                    Log.trace(
                        PushTemplateConstants.LOG_TAG,
                        SELF_TAG,
                        "Creating an ActionButton with label ($label), uri ($uri), and type ($type)."
                    )
                    ActionButton(label, uri, type)
                } catch (e: JSONException) {
                    Log.warning(
                        PushTemplateConstants.LOG_TAG,
                        SELF_TAG,
                        "Exception in converting actionButtons json string to json object, Error : ${e.localizedMessage}."
                    )
                    null
                }
            }
        }
    }

    // Optional, action buttons for the notification
    internal var actionButtonsString: String?
        private set

    // Optional, list of ActionButton for the notification
    internal var actionButtonsList: List<ActionButton>?
        private set

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
        actionButtonsString = DataReader.optString(data, PushTemplateConstants.PushPayloadKeys.ACTION_BUTTONS, null)
        actionButtonsList = getActionButtonsFromString(actionButtonsString)
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
        actionButtonsString =
            intentExtras.getString(PushTemplateConstants.IntentKeys.ACTION_BUTTONS_STRING)
        actionButtonsList = getActionButtonsFromString(actionButtonsString)
        remindLaterEpochTimestamp =
            intentExtras.getLong(PushTemplateConstants.IntentKeys.REMIND_EPOCH_TS)
        remindLaterDelaySeconds =
            intentExtras.getInt(PushTemplateConstants.IntentKeys.REMIND_DELAY_SECONDS)
        remindLaterText =
            intentExtras.getString(PushTemplateConstants.IntentKeys.REMIND_LABEL)
    }

    /**
     * Converts the string containing json array of actionButtons to a list of [ActionButton].
     *
     * @param actionButtons [String] containing the action buttons json string
     * @return a list of [ActionButton] or null if the conversion fails
     */
    @VisibleForTesting
    internal fun getActionButtonsFromString(actionButtons: String?): List<ActionButton>? {
        if (actionButtons == null) {
            Log.debug(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                "Exception in converting actionButtons json string to json object, Error :" +
                    " actionButtons is null"
            )
            return null
        }
        val actionButtonList = mutableListOf<ActionButton>()
        try {
            val jsonArray = JSONArray(actionButtons)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val button = ActionButton.getActionButtonFromJSONObject(jsonObject) ?: continue
                actionButtonList.add(button)
            }
        } catch (e: JSONException) {
            Log.warning(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                "Exception in converting actionButtons json string to json object, Error : ${e.localizedMessage}"
            )
            return null
        }
        return actionButtonList
    }
}
