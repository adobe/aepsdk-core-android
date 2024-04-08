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

import android.app.Activity
import android.content.Context
import androidx.core.app.NotificationCompat
import com.adobe.marketing.mobile.services.Log

/**
 * Object responsible for constructing a legacy (non [RemoteViews] based) push notification.
 */
internal object LegacyNotificationBuilder {
    private const val SELF_TAG = "LegacyNotificationBuilder"

    internal fun construct(
        context: Context,
        trackerActivity: Activity?,
        pushTemplate: AEPPushTemplate
    ): NotificationCompat.Builder {
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Building a legacy style push notification."
        )
        val channelId: String = createChannelAndGetChannelID(
            context,
            pushTemplate.getChannelId(),
            pushTemplate.getSound(),
            pushTemplate.getNotificationImportance()
        )
        val builder = NotificationCompat.Builder(
            context, channelId
        )
            .setTicker(pushTemplate.getTicker())
            .setContentTitle(pushTemplate.getTitle())
            .setContentText(pushTemplate.getBody())
            .setNumber(pushTemplate.getBadgeCount())
            .setPriority(pushTemplate.getNotificationImportance())
        setLargeIcon(
            builder,
            pushTemplate.getImageUrl(),
            pushTemplate.getTitle(),
            pushTemplate.getExpandedBodyText()
        )
        setSmallIcon(
            context,
            builder,
            pushTemplate.getSmallIcon(),
            pushTemplate.getSmallIconColor()
        ) // Small Icon must be present, otherwise the
        // notification will not be displayed.
        setVisibility(
            builder, pushTemplate.getNotificationVisibility()
        )
        addActionButtons(
            context,
            trackerActivity,
            builder,
            pushTemplate.getActionButtons(),
            pushTemplate.getMessageId(),
            pushTemplate.getDeliveryId(),
            pushTemplate.getTag(),
            pushTemplate.getStickyStatus() ?: false
        ) // Add action buttons if any
        setSound(context, builder, pushTemplate.getSound())
        setNotificationClickAction(
            context,
            trackerActivity,
            builder,
            pushTemplate.getMessageId(),
            pushTemplate.getDeliveryId(),
            pushTemplate.getActionUri(),
            pushTemplate.getTag(),
            pushTemplate.getStickyStatus() ?: false
        )
        setNotificationDeleteAction(
            context,
            trackerActivity,
            builder,
            pushTemplate.getMessageId(),
            pushTemplate.getDeliveryId()
        )
        return builder
    }
}
