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

package com.adobe.marketing.mobile.services.ui.pushtemplate

import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.util.DataReader
import com.adobe.marketing.mobile.util.StringUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Class representing the push template notification message data payload.
 * This class is used to parse the push template data payload and provide the necessary information
 * to build a notification.
 *
 * @param data [MutableMap] containing the push template notification message data payload
 */
internal sealed class AEPPushTemplate(val data: MutableMap<String, String>) {
    /** Enum to denote the type of action  */
    enum class ActionType {
        DEEPLINK, WEBURL, DISMISS, OPENAPP, NONE
    }

    /** Class representing the action button with label, link and type  */
    class ActionButton(val label: String, val link: String?, type: String?) {
        val type: ActionType

        init {
            this.type = getActionTypeFromString(type)
        }
    }

    internal object ActionButtonType {
        const val DEEPLINK = "DEEPLINK"
        const val WEBURL = "WEBURL"
        const val DISMISS = "DISMISS"
        const val OPENAPP = "OPENAPP"
    }

    internal object ActionButtons {
        const val LABEL = "label"
        const val URI = "uri"
        const val TYPE = "type"
    }

    internal object NotificationPriority {
        fun from(priority: String?): Int {
            return if (priority == null) NotificationCompat.PRIORITY_DEFAULT else notificationPriorityMap[priority]
                ?: return NotificationCompat.PRIORITY_DEFAULT
        }

        const val PRIORITY_DEFAULT = "PRIORITY_DEFAULT"
        const val PRIORITY_MIN = "PRIORITY_MIN"
        const val PRIORITY_LOW = "PRIORITY_LOW"
        const val PRIORITY_HIGH = "PRIORITY_HIGH"
        const val PRIORITY_MAX = "PRIORITY_MAX"
    }

    internal object NotificationVisibility {
        const val PUBLIC = "PUBLIC"
        const val PRIVATE = "PRIVATE"
        const val SECRET = "SECRET"
    }

    private val title: String
    private val body: String
    private val sound: String?
    private var badgeCount = 0
    private var notificationPriority = NotificationCompat.PRIORITY_DEFAULT

    @RequiresApi(Build.VERSION_CODES.N)
    private var notificationImportance = NotificationManager.IMPORTANCE_DEFAULT
    private var notificationVisibility = NotificationCompat.VISIBILITY_PRIVATE
    private val channelId: String?
    private val smallIcon: String?
    private val largeIcon: String?
    private val imageUrl: String?
    private val actionType: ActionType?
    private val actionUri: String?
    private val actionButtonsString: String?
    private val messageId: String
    private val deliveryId: String

    // push template payload values
    // Required, Version of the payload assigned by the authoring UI.
    private val payloadVersion: Int

    // Optional, Body of the message shown in the expanded message layout (setCustomBigContentView)
    private val expandedBodyText: String?

    // Optional, Text color for adb_body. Represented as six character hex, e.g. 00FF00
    private val expandedBodyTextColor: String?

    // Optional, Text color for adb_title. Represented as six character hex, e.g. 00FF00
    private val titleTextColor: String?

    // Optional, Color for the notification's small icon. Represented as six character hex, e.g.
    // 00FF00
    private val smallIconColor: String?

    // Optional, Color for the notification's background. Represented as six character hex, e.g.
    // 00FF00
    private val notificationBackgroundColor: String?

    // Optional, If present, show a "remind later" button using the value provided as its label
    private val remindLaterText: String?

    // Optional, If present, schedule this notification to be re-delivered at this epoch timestamp
    // (in seconds) provided.
    private val remindLaterTimestamp: Long?

    // Optional, If present and a notification with the same tag is already being shown, the new
    // notification replaces the existing one in the notification drawer.
    private val tag: String?

    // Optional, If present sets the "ticker" text, which is sent to accessibility services.
    private val ticker: String?

    // Optional, the type of push template this payload contains
    private val templateType: PushTemplateType?

    // Optional, when set to false or unset, the notification is automatically dismissed when the
    // user clicks it in the panel. When set to true, the notification persists even when the user
    // clicks it.
    private val isNotificationSticky: Boolean?

    init {
        // fast fail (via IllegalArgumentException) if required data is not present
        val title = DataReader.getString(data, PushTemplateConstants.PushPayloadKeys.TITLE)
        if (title.isNullOrEmpty()) throw IllegalArgumentException("Required field \"adb_title\" not found.")
        this.title = title

        var bodyText = DataReader.getString(
            data,
            PushTemplateConstants.PushPayloadKeys.BODY
        )
        if (bodyText.isNullOrEmpty()) {
            bodyText = DataReader.getString(data, PushTemplateConstants.PushPayloadKeys.ACC_PAYLOAD_BODY)
        }
        if (bodyText.isNullOrEmpty()) throw IllegalArgumentException("Required field \"adb_body\" or \"_msg\" not found.")
        this.body = bodyText

        val messageId = DataReader.getString(data, PushTemplateConstants.Tracking.Keys.MESSAGE_ID)
        if (messageId.isNullOrEmpty()) throw IllegalArgumentException("Required field \"_mId\" not found.")
        this.messageId = messageId

        val deliveryId = DataReader.getString(data, PushTemplateConstants.Tracking.Keys.DELIVERY_ID)
        if (deliveryId.isNullOrEmpty()) throw IllegalArgumentException("Required field \"_dId\" not found.")
        this.deliveryId = deliveryId

        val payloadVersion =
            DataReader.getString(
                data,
                PushTemplateConstants.PushPayloadKeys.VERSION
            )
        if (payloadVersion.isNullOrEmpty()) throw IllegalArgumentException("Required field \"adb_version\" not found.")
        this.payloadVersion = payloadVersion.toInt()

        // optional push template data
        sound = DataReader.optString(data, PushTemplateConstants.PushPayloadKeys.SOUND, null)
        imageUrl = DataReader.optString(data, PushTemplateConstants.PushPayloadKeys.IMAGE_URL, null)
        channelId =
            DataReader.optString(data, PushTemplateConstants.PushPayloadKeys.CHANNEL_ID, null)
        actionUri =
            DataReader.optString(data, PushTemplateConstants.PushPayloadKeys.ACTION_URI, null)
        var smallIcon =
            DataReader.optString(data, PushTemplateConstants.PushPayloadKeys.SMALL_ICON, null)
        if (smallIcon.isNullOrEmpty()) {
            Log.debug(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                "The \"adb_small_icon\" key is not present in the message data payload, attempting to use \"adb_icon\" key instead."
            )
            smallIcon = DataReader.optString(
                data,
                PushTemplateConstants.PushPayloadKeys.LEGACY_SMALL_ICON,
                null
            )
        }
        this.smallIcon = smallIcon
        largeIcon =
            DataReader.optString(data, PushTemplateConstants.PushPayloadKeys.LARGE_ICON, null)
        expandedBodyText = DataReader.optString(
            data, PushTemplateConstants.PushPayloadKeys.EXPANDED_BODY_TEXT, null
        )
        expandedBodyTextColor = DataReader.optString(
            data, PushTemplateConstants.PushPayloadKeys.EXPANDED_BODY_TEXT_COLOR, null
        )
        titleTextColor = DataReader.optString(
            data, PushTemplateConstants.PushPayloadKeys.TITLE_TEXT_COLOR, null
        )
        smallIconColor = DataReader.optString(
            data, PushTemplateConstants.PushPayloadKeys.SMALL_ICON_COLOR, null
        )
        notificationBackgroundColor = DataReader.optString(
            data,
            PushTemplateConstants.PushPayloadKeys.NOTIFICATION_BACKGROUND_COLOR,
            null
        )
        remindLaterText = DataReader.optString(
            data, PushTemplateConstants.PushPayloadKeys.REMIND_LATER_TEXT, ""
        )
        val timestampString = DataReader.optString(
            data, PushTemplateConstants.PushPayloadKeys.REMIND_LATER_TIMESTAMP, null
        )
        remindLaterTimestamp =
            if (StringUtils.isNullOrEmpty(timestampString)) PushTemplateConstants.DefaultValues.DEFAULT_REMIND_LATER_TIMESTAMP else timestampString.toLong()
        tag = DataReader.optString(data, PushTemplateConstants.PushPayloadKeys.TAG, null)
        ticker = DataReader.optString(data, PushTemplateConstants.PushPayloadKeys.TICKER, null)
        templateType = DataReader.optString(data, PushTemplateConstants.PushPayloadKeys.TEMPLATE_TYPE, null)?.let { PushTemplateType.fromString(it) }
        val stickyValue =
            DataReader.optString(data, PushTemplateConstants.PushPayloadKeys.STICKY, null)
        isNotificationSticky =
            if (StringUtils.isNullOrEmpty(stickyValue)) false else java.lang.Boolean.parseBoolean(
                stickyValue
            )
        try {
            val count = data[PushTemplateConstants.PushPayloadKeys.BADGE_NUMBER]
            count?.let {
                badgeCount = count.toInt()
            }
        } catch (e: NumberFormatException) {
            Log.debug(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                "Exception in converting notification badge count to int - %s",
                e.localizedMessage
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notificationImportance = getNotificationImportanceFromString(
                data[PushTemplateConstants.PushPayloadKeys.NOTIFICATION_PRIORITY]
            )
        } else {
            notificationPriority = NotificationPriority.from(
                data[PushTemplateConstants.PushPayloadKeys.NOTIFICATION_PRIORITY]
            )
        }

        notificationVisibility = getNotificationVisibilityFromString(
            data[PushTemplateConstants.PushPayloadKeys.NOTIFICATION_VISIBILITY]
        )
        actionType = getActionTypeFromString(
            data[PushTemplateConstants.PushPayloadKeys.ACTION_TYPE]
        )
        actionButtonsString = data[PushTemplateConstants.PushPayloadKeys.ACTION_BUTTONS]
    }

    fun getTemplateType(): PushTemplateType? {
        return templateType
    }

    fun getTitle(): String {
        return title
    }

    fun getBadgeCount(): Int {
        return badgeCount
    }

    fun getBody(): String {
        return body
    }

    fun getSound(): String? {
        return sound
    }

    fun getChannelId(): String? {
        return channelId
    }

    fun getSmallIcon(): String? {
        return smallIcon
    }

    fun getLargeIcon(): String? {
        return largeIcon
    }

    fun getImageUrl(): String? {
        return imageUrl
    }

    fun getMessageId(): String {
        return messageId
    }

    fun getDeliveryId(): String {
        return deliveryId
    }

    fun getExpandedBodyText(): String? {
        return expandedBodyText
    }

    fun getExpandedBodyTextColor(): String? {
        return expandedBodyTextColor
    }

    fun getTitleTextColor(): String? {
        return titleTextColor
    }

    fun getSmallIconColor(): String? {
        return smallIconColor
    }

    fun getNotificationBackgroundColor(): String? {
        return notificationBackgroundColor
    }

    fun getRemindLaterText(): String? {
        return remindLaterText
    }

    fun getRemindLaterTimestamp(): Long? {
        return remindLaterTimestamp
    }

    fun getActionButtons(): String? {
        return actionButtonsString
    }

    fun getTag(): String {
        tag?.let { return tag } ?: return messageId
    }

    fun getTicker(): String? {
        return ticker
    }

    fun getStickyStatus(): Boolean? {
        return isNotificationSticky
    }

    fun getNotificationImportance(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notificationImportance
        } else {
            // return default importance of "3" for versions < API 24
            PushTemplateConstants.DEFAULT_NOTIFICATION_IMPORTANCE
        }
    }

    fun getNotificationVisibility(): Int {
        return notificationVisibility
    }

    fun getActionType(): ActionType? {
        return actionType
    }

    fun getActionUri(): String? {
        return actionUri
    }

    /**
     * Convenience method to modify the notification data payload. This is used in the following
     * scenario:
     * - Setting a carousel image URI as the data map's image URI to allow a basic push template notification to be shown in a fallback situation.
     *
     * @param key [String] value containing the key to modify
     * @param value `String` value containing the new value to be used
     */
    fun modifyData(key: String, value: String) {
        data[key] = value
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun getNotificationImportanceFromString(priority: String?): Int {
        return if (StringUtils.isNullOrEmpty(priority)) NotificationManager.IMPORTANCE_DEFAULT else notificationImportanceMap[priority]
            ?: return NotificationManager.IMPORTANCE_DEFAULT
    }

    /**
     * Returns the notification visibility from the string. If the string is null or not a valid
     * visibility, returns Notification.VISIBILITY_PRIVATE.
     *
     * @param visibility [String] representing the visibility of the notification
     * @return [Int] representing the visibility of the notification
     */
    private fun getNotificationVisibilityFromString(visibility: String?): Int {
        return if (StringUtils.isNullOrEmpty(visibility)) NotificationCompat.VISIBILITY_PRIVATE else notificationVisibilityMap[visibility]
            ?: return NotificationCompat.VISIBILITY_PRIVATE
    }

    companion object {
        const val SELF_TAG = "AEPPushTemplate"

        // Legacy push payload values
        private const val ACTION_BUTTON_CAPACITY = 3

        @RequiresApi(api = Build.VERSION_CODES.N)
        val notificationImportanceMap: HashMap<String?, Int?> = object : HashMap<String?, Int?>() {
            init {
                put(NotificationPriority.PRIORITY_MIN, NotificationManager.IMPORTANCE_MIN)
                put(NotificationPriority.PRIORITY_LOW, NotificationManager.IMPORTANCE_LOW)
                put(
                    NotificationPriority.PRIORITY_DEFAULT,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                put(NotificationPriority.PRIORITY_HIGH, NotificationManager.IMPORTANCE_HIGH)
                put(NotificationPriority.PRIORITY_MAX, NotificationManager.IMPORTANCE_MAX)
            }
        }

        val notificationVisibilityMap: HashMap<String?, Int?> = object : HashMap<String?, Int?>() {
            init {
                put(
                    NotificationVisibility.PRIVATE,
                    NotificationCompat.VISIBILITY_PRIVATE
                )
                put(
                    NotificationVisibility.PUBLIC,
                    NotificationCompat.VISIBILITY_PUBLIC
                )
                put(
                    NotificationVisibility.SECRET,
                    NotificationCompat.VISIBILITY_SECRET
                )
            }
        }
        val notificationPriorityMap: HashMap<String?, Int?> = object : HashMap<String?, Int?>() {
            init {
                put(NotificationPriority.PRIORITY_MIN, NotificationCompat.PRIORITY_MIN)
                put(NotificationPriority.PRIORITY_LOW, NotificationCompat.PRIORITY_LOW)
                put(NotificationPriority.PRIORITY_DEFAULT, NotificationCompat.PRIORITY_DEFAULT)
                put(NotificationPriority.PRIORITY_HIGH, NotificationCompat.PRIORITY_HIGH)
                put(NotificationPriority.PRIORITY_MAX, NotificationCompat.PRIORITY_MAX)
            }
        }

        private fun getActionTypeFromString(type: String?): ActionType {
            if (StringUtils.isNullOrEmpty(type)) {
                return ActionType.NONE
            }
            when (type) {
                ActionButtonType.DEEPLINK -> return ActionType.DEEPLINK
                ActionButtonType.WEBURL -> return ActionType.WEBURL
                ActionButtonType.DISMISS -> return ActionType.DISMISS
                ActionButtonType.OPENAPP -> return ActionType.OPENAPP
            }
            return ActionType.NONE
        }

        fun getActionButtonsFromString(actionButtons: String?): List<ActionButton>? {
            if (actionButtons == null) {
                Log.debug(
                    PushTemplateConstants.LOG_TAG,
                    SELF_TAG,
                    "Exception in converting actionButtons json string to json object, Error :" +
                        " actionButtons is null"
                )
                return null
            }
            val actionButtonList: MutableList<ActionButton> = ArrayList(
                ACTION_BUTTON_CAPACITY
            )
            try {
                val jsonArray = JSONArray(actionButtons)
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val button = getActionButton(jsonObject) ?: continue
                    actionButtonList.add(button)
                }
            } catch (e: JSONException) {
                Log.warning(
                    PushTemplateConstants.LOG_TAG,
                    SELF_TAG,
                    "Exception in converting actionButtons json string to json object, Error : %s",
                    e.localizedMessage
                )
                return null
            }
            return actionButtonList
        }

        private fun getActionButton(jsonObject: JSONObject): ActionButton? {
            return try {
                val label = jsonObject.getString(ActionButtons.LABEL)
                if (label.isEmpty()) {
                    Log.debug(PushTemplateConstants.LOG_TAG, SELF_TAG, "Label is empty")
                    return null
                }
                var uri: String? = null
                val type = jsonObject.getString(ActionButtons.TYPE)
                if (type == ActionButtonType.WEBURL || type == ActionButtonType.DEEPLINK) {
                    uri = jsonObject.optString(ActionButtons.URI)
                }
                Log.trace(
                    PushTemplateConstants.LOG_TAG,
                    SELF_TAG,
                    "Creating an ActionButton with label (%s), uri (%s), and type (%s)",
                    label,
                    uri,
                    type
                )
                ActionButton(label, uri, type)
            } catch (e: JSONException) {
                Log.warning(
                    PushTemplateConstants.LOG_TAG,
                    SELF_TAG,
                    "Exception in converting actionButtons json string to json object, Error : %s",
                    e.localizedMessage
                )
                null
            }
        }
    }
}
