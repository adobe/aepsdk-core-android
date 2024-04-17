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

package com.adobe.marketing.mobile.services.ui.notification

import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.util.DataReader
import com.adobe.marketing.mobile.util.StringUtils

/**
 * This class is used to parse the push template data payload or an intent and provide the necessary information
 * to build a notification.
 */
internal sealed class AEPPushTemplate {
    /** Enum to denote the type of action  */
    enum class ActionType {
        DEEPLINK, WEBURL, DISMISS, OPENAPP, NONE
    }

    /** Class representing the action button with label, link and type  */
    class ActionButton(val label: String, val link: String?, type: String?) {
        val type: ActionType

        init {
            this.type = ActionType.valueOf(type ?: ActionType.NONE.name)
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

    // Message data payload for the push template
    internal lateinit var messageData: MutableMap<String, String>
        private set

    // Required, title of the message shown in the collapsed and expanded push template layouts
    internal val title: String

    // Required, body of the message shown in the collapsed push template layout
    internal val body: String

    // Required, Version of the payload assigned by the authoring UI.
    internal var payloadVersion: Int?
        private set

    // begin optional values
    // Optional, sound to play when the notification is shown
    internal var sound: String?
        private set

    // Optional, number to show on the badge of the app
    internal var badgeCount: Int = 0
        private set

    // Optional, priority of the notification
    internal var notificationPriority: Int?
        private set

    // Optional, importance of the notification. Only used on Android N and above.
    private var notificationImportance: Int?

    // Optional, visibility of the notification
    private var notificationVisibility: Int?

    // Optional, notification channel to use when displaying the notification. Only used on Android O and above.
    internal var channelId: String?
        private set

    // Optional, small icon for the notification
    internal var smallIcon: String?
        private set

    // Optional, large icon for the notification
    internal var largeIcon: String?
        private set

    // Optional, image to show in the notification
    internal var imageUrl: String?
        private set

    // Optional, action type for the notification
    private var actionType: ActionType?

    // Optional, action uri for the notification
    internal var actionUri: String?
        private set

    // Optional, action buttons for the notification
    internal var actionButtonsString: String?
        private set

    // Optional, Body of the message shown in the expanded message layout (setCustomBigContentView)
    internal var expandedBodyText: String?
        private set

    // Optional, Text color for adb_body. Represented as six character hex, e.g. 00FF00
    internal var expandedBodyTextColor: String?
        private set

    // Optional, Text color for adb_title. Represented as six character hex, e.g. 00FF00
    internal var titleTextColor: String?
        private set

    // Optional, Color for the notification's small icon. Represented as six character hex, e.g.
    // 00FF00
    internal var smallIconColor: String?
        private set

    // Optional, Color for the notification's background. Represented as six character hex, e.g.
    // 00FF00
    internal var notificationBackgroundColor: String?
        private set

    // Optional, If present and a notification with the same tag is already being shown, the new
    // notification replaces the existing one in the notification drawer.
    internal var tag: String?
        private set

    // Optional, If present sets the "ticker" text, which is sent to accessibility services.
    internal var ticker: String?
        private set

    // Optional, the type of push template this payload contains
    internal var templateType: PushTemplateType?
        private set

    // Optional, when set to false or unset, the notification is automatically dismissed when the
    // user clicks it in the panel. When set to true, the notification persists even when the user
    // clicks it.
    internal var isNotificationSticky: Boolean?
        private set

    // flag to denote if the PushTemplate was built from an intent
    internal var isFromIntent: Boolean?

    /**
     * Constructor to create a push template object from the data payload.
     *
     * @param data [Map] of key-value pairs representing the push template data payload
     */
    constructor(data: Map<String, String>?) {
        // fast fail (via IllegalArgumentException) if required data is not present
        if (data.isNullOrEmpty()) throw IllegalArgumentException("Push template data is null.")
        this.messageData = data.toMutableMap()
        val title = DataReader.getString(data, PushTemplateConstants.PushPayloadKeys.TITLE)
        if (title.isNullOrEmpty()) throw IllegalArgumentException("Required field \"adb_title\" not found.")
        this.title = title

        var bodyText = DataReader.getString(
            data,
            PushTemplateConstants.PushPayloadKeys.BODY
        )
        if (bodyText.isNullOrEmpty()) {
            bodyText =
                DataReader.getString(data, PushTemplateConstants.PushPayloadKeys.ACC_PAYLOAD_BODY)
        }
        if (bodyText.isNullOrEmpty()) throw IllegalArgumentException("Required field \"adb_body\" or \"_msg\" not found.")
        this.body = bodyText

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
        actionUri =
            DataReader.optString(data, PushTemplateConstants.PushPayloadKeys.ACTION_URI, null)
        actionType = ActionType.valueOf(
            data[PushTemplateConstants.PushPayloadKeys.ACTION_TYPE] ?: ActionType.NONE.name
        )
        actionButtonsString = data[PushTemplateConstants.PushPayloadKeys.ACTION_BUTTONS]
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
        try {
            val count = data[PushTemplateConstants.PushPayloadKeys.BADGE_NUMBER]
            count?.let {
                badgeCount = count.toInt()
            }
        } catch (e: NumberFormatException) {
            Log.debug(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                "Exception in converting notification badge count to int - ${e.localizedMessage}."
            )
        }
        notificationPriority = NotificationPriority.from(
            data[PushTemplateConstants.PushPayloadKeys.NOTIFICATION_PRIORITY]
        )
        notificationImportance =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) getNotificationImportanceFromString(
                data[PushTemplateConstants.PushPayloadKeys.NOTIFICATION_PRIORITY]
            ) else null

        notificationVisibility = getNotificationVisibilityFromString(
            data[PushTemplateConstants.PushPayloadKeys.NOTIFICATION_VISIBILITY]
        )
        channelId =
            DataReader.optString(data, PushTemplateConstants.PushPayloadKeys.CHANNEL_ID, null)
        templateType =
            DataReader.optString(data, PushTemplateConstants.PushPayloadKeys.TEMPLATE_TYPE, null)
                ?.let { PushTemplateType.fromString(it) }
        tag = DataReader.optString(data, PushTemplateConstants.PushPayloadKeys.TAG, null)
        val stickyValue =
            DataReader.optString(data, PushTemplateConstants.PushPayloadKeys.STICKY, null)
        isNotificationSticky =
            if (StringUtils.isNullOrEmpty(stickyValue)) false else java.lang.Boolean.parseBoolean(
                stickyValue
            )
        ticker = DataReader.optString(data, PushTemplateConstants.PushPayloadKeys.TICKER, null)
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
        isFromIntent = false
    }

    /**
     * Constructor to create a push template object from an [Intent].
     *
     * @param intent [Intent] containing key value pairs extracted from a push template data payload
     */
    constructor(intent: Intent?) {
        val intentExtras =
            intent?.extras ?: throw IllegalArgumentException("Intent extras are null")
        // required values
        title = intentExtras.getString(PushTemplateConstants.IntentKeys.TITLE_TEXT)
            ?: throw IllegalArgumentException("Required field \"adb_title\" not found.")
        body = intentExtras.getString(PushTemplateConstants.IntentKeys.BODY_TEXT)
            ?: throw IllegalArgumentException("Required field \"adb_body\" not found.")
        payloadVersion = intentExtras.getInt(PushTemplateConstants.IntentKeys.PAYLOAD_VERSION)
        payloadVersion?.let {
            if (it < 1) throw IllegalArgumentException("Invalid \"payload version\" found.")
        }

        // optional values
        sound = intentExtras.getString(PushTemplateConstants.IntentKeys.CUSTOM_SOUND)
        imageUrl = intentExtras.getString(PushTemplateConstants.IntentKeys.IMAGE_URI)
        actionUri = intentExtras.getString(PushTemplateConstants.IntentKeys.ACTION_URI)
        actionType = ActionType.valueOf(
            intentExtras.getString(PushTemplateConstants.IntentKeys.ACTION_TYPE)
                ?: ActionType.NONE.name
        )
        expandedBodyText =
            intentExtras.getString(PushTemplateConstants.IntentKeys.EXPANDED_BODY_TEXT)
        actionButtonsString =
            intentExtras.getString(PushTemplateConstants.IntentKeys.ACTION_BUTTONS_STRING)
        smallIcon = intentExtras.getString(PushTemplateConstants.IntentKeys.SMALL_ICON)
        largeIcon = intentExtras.getString(PushTemplateConstants.IntentKeys.LARGE_ICON)
        badgeCount = intentExtras.getInt(PushTemplateConstants.IntentKeys.BADGE_COUNT)
        notificationPriority = intentExtras.getInt(PushTemplateConstants.IntentKeys.PRIORITY)
        notificationImportance = intentExtras.getInt(PushTemplateConstants.IntentKeys.IMPORTANCE)
        notificationVisibility = intentExtras.getInt(PushTemplateConstants.IntentKeys.VISIBILITY)
        channelId = intentExtras.getString(PushTemplateConstants.IntentKeys.CHANNEL_ID)
        templateType =
            PushTemplateType.fromString(intentExtras.getString(PushTemplateConstants.IntentKeys.TEMPLATE_TYPE))
        tag = intentExtras.getString(PushTemplateConstants.IntentKeys.TAG) as String
        isNotificationSticky = intentExtras.getBoolean(PushTemplateConstants.IntentKeys.STICKY)
        ticker = intentExtras.getString(PushTemplateConstants.IntentKeys.TICKER)
        expandedBodyText =
            intentExtras.getString(PushTemplateConstants.IntentKeys.EXPANDED_BODY_TEXT)
        expandedBodyTextColor =
            intentExtras.getString(PushTemplateConstants.IntentKeys.EXPANDED_BODY_TEXT_COLOR)
        titleTextColor = intentExtras.getString(PushTemplateConstants.IntentKeys.TITLE_TEXT_COLOR)
        smallIconColor = intentExtras.getString(PushTemplateConstants.IntentKeys.SMALL_ICON_COLOR)
        notificationBackgroundColor =
            intentExtras.getString(PushTemplateConstants.IntentKeys.NOTIFICATION_BACKGROUND_COLOR)
        isFromIntent = true
    }

    fun getNotificationImportance(): Int {
        notificationImportance?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) return notificationImportance as Int
        }
        return NotificationManager.IMPORTANCE_DEFAULT
    }

    fun getNotificationVisibility(): Int {
        return notificationVisibility ?: NotificationCompat.VISIBILITY_PRIVATE
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun getNotificationImportanceFromString(priority: String?): Int {
        return if (priority.isNullOrEmpty()) NotificationManager.IMPORTANCE_DEFAULT else notificationImportanceMap[priority]
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
        private const val SELF_TAG = "AEPPushTemplate"

        @RequiresApi(api = Build.VERSION_CODES.N)
        internal val notificationImportanceMap: Map<String?, Int?> = mapOf(
            NotificationPriority.PRIORITY_MIN to NotificationManager.IMPORTANCE_MIN,
            NotificationPriority.PRIORITY_LOW to NotificationManager.IMPORTANCE_LOW,
            NotificationPriority.PRIORITY_DEFAULT to NotificationManager.IMPORTANCE_DEFAULT,
            NotificationPriority.PRIORITY_HIGH to NotificationManager.IMPORTANCE_HIGH,
            NotificationPriority.PRIORITY_MAX to NotificationManager.IMPORTANCE_MAX
        )

        internal val notificationVisibilityMap: Map<String?, Int?> = mapOf(
            NotificationVisibility.PRIVATE to NotificationCompat.VISIBILITY_PRIVATE,
            NotificationVisibility.PUBLIC to NotificationCompat.VISIBILITY_PUBLIC,
            NotificationVisibility.SECRET to NotificationCompat.VISIBILITY_SECRET
        )

        internal val notificationPriorityMap: Map<String?, Int?> = mapOf(
            NotificationPriority.PRIORITY_MIN to NotificationCompat.PRIORITY_MIN,
            NotificationPriority.PRIORITY_LOW to NotificationCompat.PRIORITY_LOW,
            NotificationPriority.PRIORITY_DEFAULT to NotificationCompat.PRIORITY_DEFAULT,
            NotificationPriority.PRIORITY_HIGH to NotificationCompat.PRIORITY_HIGH,
            NotificationPriority.PRIORITY_MAX to NotificationCompat.PRIORITY_MAX
        )
    }
}
