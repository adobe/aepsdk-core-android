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

import androidx.core.app.NotificationCompat
import com.adobe.marketing.mobile.util.MapUtils
import com.adobe.marketing.mobile.util.StringUtils
import com.google.firebase.messaging.RemoteMessage
import kotlin.jvm.Throws

/**
 * Class representing the data push payload received from Firebase.
 *
 * @param message [RemoteMessage] object received from the [com.google.firebase.messaging.FirebaseMessagingService]
 */
class AEPPushPayload(val message: RemoteMessage?) {
    private lateinit var messageData: MutableMap<String, String>
    private lateinit var messageId: String
    private lateinit var deliveryId: String
    private var tag: String? = null

    private val notificationCompatPriorityMap: HashMap<Int, String> =
        object : HashMap<Int, String>() {
            init {
                put(
                    NotificationCompat.PRIORITY_MIN,
                    AEPPushTemplate.NotificationPriority.PRIORITY_MIN
                )
                put(
                    NotificationCompat.PRIORITY_LOW,
                    AEPPushTemplate.NotificationPriority.PRIORITY_LOW
                )
                put(
                    NotificationCompat.PRIORITY_DEFAULT,
                    AEPPushTemplate.NotificationPriority.PRIORITY_DEFAULT
                )
                put(
                    NotificationCompat.PRIORITY_HIGH,
                    AEPPushTemplate.NotificationPriority.PRIORITY_HIGH
                )
                put(
                    NotificationCompat.PRIORITY_MAX,
                    AEPPushTemplate.NotificationPriority.PRIORITY_MAX
                )
            }
        }
    private val notificationCompatVisibilityMap: HashMap<Int, String> =
        object : HashMap<Int, String>() {
            init {
                put(
                    NotificationCompat.VISIBILITY_PRIVATE,
                    AEPPushTemplate.NotificationVisibility.PRIVATE
                )
                put(
                    NotificationCompat.VISIBILITY_PUBLIC,
                    AEPPushTemplate.NotificationVisibility.PUBLIC
                )
                put(
                    NotificationCompat.VISIBILITY_SECRET,
                    AEPPushTemplate.NotificationVisibility.SECRET
                )
            }
        }

    init {
        if (message == null) {
            throw IllegalArgumentException("Failed to create AEPPushPayload, remote message is null.")
        }

        validateMessageData(message.data)

        // migrate any push notification object payload keys if needed
        val notification: RemoteMessage.Notification? = message.notification
        if (notification != null) {
            convertNotificationPayloadData(notification)
        }
    }

    /**
     * Validates the message data and stores the message id, delivery id, and tag.
     *
     * @param messageData [MutableMap] containing the message data
     * @throws [IllegalArgumentException] if the message data, message id, or delivery id is null or empty
     */
    @Throws(IllegalArgumentException::class)
    private fun validateMessageData(messageData: MutableMap<String, String>) {
        require(!MapUtils.isNullOrEmpty(messageData)) {
            throw IllegalArgumentException("Failed to create AEPPushPayload, remote message data payload is null or empty.")
        }
        messageId = messageData[PushTemplateConstants.Tracking.Keys.MESSAGE_ID] ?: ""
        require(messageId.isNotEmpty()) { throw IllegalArgumentException("Failed to create AEPPushPayload, message id is null or empty.") }
        deliveryId = messageData[PushTemplateConstants.Tracking.Keys.DELIVERY_ID] ?: ""
        require(deliveryId.isNotEmpty()) { throw IllegalArgumentException("Failed to create AEPPushPayload, delivery id is null or empty.") }
        this.messageData = messageData
        tag = messageData[PushTemplateConstants.PushPayloadKeys.TAG]
    }

    private fun convertNotificationPayloadData(notification: RemoteMessage.Notification) {
        // Migrate the 13 ACC KVP to "adb" prefixed keys.
        // Note, the key value pairs present in the data payload are preferred over the notification
        // key value pairs.
        // The notification key value pairs will only be added to the message data if the
        // corresponding key
        // does not have a value.
        // message.android.notification.icon to adb_small_icon
        // message.android.notification.sound to adb_sound
        // message.android.notification.tag	to adb_tag
        // message.android.notification.click_action to adb_uri
        // message.android.notification.channel_id to adb_channel_id
        // message.android.notification.ticker to adb_ticker
        // message.android.notification.sticky to adb_sticky
        // message.android.notification.visibility to adb_n_visibility
        // message.android.notification.notification_priority to adb_n_priority
        // message.android.notification.notification_count to adb_n_count
        // message.notification.body to adb_body
        // message.notification.title to adb_title
        // message.notification.image to adb_image
        if (StringUtils.isNullOrEmpty(messageData[PushTemplateConstants.PushPayloadKeys.TAG])) {
            notification.tag?.let {
                tag = it
                messageData[PushTemplateConstants.PushPayloadKeys.TAG] = it
            }
        }
        if (StringUtils.isNullOrEmpty(
                messageData[PushTemplateConstants.PushPayloadKeys.SMALL_ICON]
            )
        ) {
            notification.icon?.let {
                messageData[PushTemplateConstants.PushPayloadKeys.SMALL_ICON] = it
            }
        }
        if (StringUtils.isNullOrEmpty(
                messageData[PushTemplateConstants.PushPayloadKeys.SOUND]
            )
        ) {
            notification.sound?.let {
                messageData[PushTemplateConstants.PushPayloadKeys.SOUND] = it
            }
        }
        if (StringUtils.isNullOrEmpty(
                messageData[PushTemplateConstants.PushPayloadKeys.ACTION_URI]
            )
        ) {
            notification.clickAction?.let {
                messageData[PushTemplateConstants.PushPayloadKeys.ACTION_URI] = it
            }
        }
        if (StringUtils.isNullOrEmpty(
                messageData[PushTemplateConstants.PushPayloadKeys.CHANNEL_ID]
            )
        ) {
            notification.channelId?.let {
                messageData[PushTemplateConstants.PushPayloadKeys.CHANNEL_ID] = it
            }
        }
        if (StringUtils.isNullOrEmpty(
                messageData[PushTemplateConstants.PushPayloadKeys.TICKER]
            )
        ) {
            notification.ticker?.let {
                messageData[PushTemplateConstants.PushPayloadKeys.TICKER] = it
            }
        }
        if (StringUtils.isNullOrEmpty(
                messageData[PushTemplateConstants.PushPayloadKeys.STICKY]
            )
        ) {
            notification.sticky.let {
                messageData[PushTemplateConstants.PushPayloadKeys.STICKY] = it.toString()
            }
        }
        if (StringUtils.isNullOrEmpty(
                messageData[PushTemplateConstants.PushPayloadKeys.NOTIFICATION_VISIBILITY]
            )
        ) {
            notificationCompatVisibilityMap[notification.visibility]?.let {
                messageData[PushTemplateConstants.PushPayloadKeys.NOTIFICATION_VISIBILITY] = it
            }
        }
        if (StringUtils.isNullOrEmpty(
                messageData[PushTemplateConstants.PushPayloadKeys.NOTIFICATION_PRIORITY]
            )
        ) {
            notificationCompatPriorityMap[notification.notificationPriority]?.let {
                messageData[PushTemplateConstants.PushPayloadKeys.NOTIFICATION_PRIORITY] = it
            }
        }
        if (StringUtils.isNullOrEmpty(
                messageData[PushTemplateConstants.PushPayloadKeys.BADGE_NUMBER]
            )
        ) {
            notification.notificationCount?.let {
                messageData[PushTemplateConstants.PushPayloadKeys.BADGE_NUMBER] =
                    java.lang.String.valueOf(notification.notificationCount)
            }
        }
        if (StringUtils.isNullOrEmpty(
                messageData[PushTemplateConstants.PushPayloadKeys.BODY]
            )
        ) {
            notification.body?.let {
                messageData[PushTemplateConstants.PushPayloadKeys.BODY] = it
            }
        }
        if (StringUtils.isNullOrEmpty(
                messageData[PushTemplateConstants.PushPayloadKeys.TITLE]
            )
        ) {
            notification.title?.let {
                messageData[PushTemplateConstants.PushPayloadKeys.TITLE] = it
            }
        }
        if (StringUtils.isNullOrEmpty(
                messageData[PushTemplateConstants.PushPayloadKeys.IMAGE_URL]
            )
        ) {
            notification.imageUrl?.let {
                messageData[PushTemplateConstants.PushPayloadKeys.IMAGE_URL] = it.toString()
            }
        }
    }

    fun getMessageData(): MutableMap<String, String> {
        return messageData
    }

    fun getMessageId(): String {
        return messageId
    }

    fun getDeliveryId(): String {
        return deliveryId
    }

    fun getTag(): String? {
        return tag
    }
}
