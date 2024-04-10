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

import androidx.core.app.NotificationCompat
import kotlin.jvm.Throws

/**
 * Class representing the data push payload received from Firebase.
 *
 * @param data [Map] containing the message data from a remote message
 */
class AEPPushPayload(private var data: Map<String, String>?) {
    lateinit var messageData: MutableMap<String, String>
        private set
    lateinit var messageId: String
        private set
    lateinit var deliveryId: String
        private set
    init {
        if (data == null) {
            throw IllegalArgumentException("Failed to create AEPPushPayload, remote message data is null.")
        }
        validateMessageData(data ?: emptyMap())
    }

    /**
     * Validates then stores the message data, message id, and delivery id.
     *
     * @param messageData [MutableMap] containing the message data
     * @throws [IllegalArgumentException] if the message data, message id, or delivery id is null or empty
     */
    @Throws(IllegalArgumentException::class)
    private fun validateMessageData(messageData: Map<String, String>) {
        messageId = messageData[PushTemplateConstants.Tracking.Keys.MESSAGE_ID] ?: ""
        require(messageId.isNotEmpty()) { throw IllegalArgumentException("Failed to create AEPPushPayload, message id is null or empty.") }
        deliveryId = messageData[PushTemplateConstants.Tracking.Keys.DELIVERY_ID] ?: ""
        require(deliveryId.isNotEmpty()) { throw IllegalArgumentException("Failed to create AEPPushPayload, delivery id is null or empty.") }
        data = messageData
    }

    fun getTag(): String? {
        return messageData.getOrDefault(PushTemplateConstants.PushPayloadKeys.TAG, messageData[PushTemplateConstants.PushPayloadKeys.MESSAGE_ID])
    }

    companion object {
        @JvmStatic
        val notificationCompatPriorityMap: Map<Int, String> = mapOf(
            NotificationCompat.PRIORITY_MIN to
                    AEPPushTemplate.NotificationPriority.PRIORITY_MIN,
            NotificationCompat.PRIORITY_LOW to
                    AEPPushTemplate.NotificationPriority.PRIORITY_LOW,
            NotificationCompat.PRIORITY_DEFAULT to
                    AEPPushTemplate.NotificationPriority.PRIORITY_DEFAULT,
            NotificationCompat.PRIORITY_HIGH to
                    AEPPushTemplate.NotificationPriority.PRIORITY_HIGH,
            NotificationCompat.PRIORITY_MAX to
                    AEPPushTemplate.NotificationPriority.PRIORITY_MAX)

        @JvmStatic
        val notificationCompatVisibilityMap: Map<Int, String> = mapOf(
            NotificationCompat.VISIBILITY_PRIVATE to
                    AEPPushTemplate.NotificationVisibility.PRIVATE,
            NotificationCompat.VISIBILITY_PUBLIC to
                    AEPPushTemplate.NotificationVisibility.PUBLIC,
            NotificationCompat.VISIBILITY_SECRET to
                    AEPPushTemplate.NotificationVisibility.SECRET)
    }
}
