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

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.adobe.marketing.mobile.services.Log

/**
 * Object responsible for constructing a legacy push notification.
 */
internal object LegacyNotificationBuilder {
    private const val SELF_TAG = "LegacyNotificationBuilder"

    fun construct(
        context: Context,
        pushTemplate: BasicPushTemplate?,
        trackerActivityClass: Class<out Activity>?
    ): NotificationCompat.Builder {
        if (pushTemplate == null) {
            throw NotificationConstructionFailedException(
                "push template is null, cannot build a notification."
            )
        }

        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Building a legacy style push notification."
        )

        // create the notification channel
        val channelId = AEPPushTemplateNotificationBuilder.createChannelAndGetChannelID(
            context,
            pushTemplate.channelId,
            pushTemplate.sound,
            pushTemplate.getNotificationImportance()
        )

        // Create the notification builder object and set the ticker, title, body, and badge count
        val builder = NotificationCompat.Builder(
            context, channelId
        )
            .setTicker(pushTemplate.ticker)
            .setContentTitle(pushTemplate.title)
            .setContentText(pushTemplate.body)
            .setNumber(pushTemplate.badgeCount)

        // set a large icon if one is present
        AEPPushTemplateNotificationBuilder.setLargeIcon(
            builder,
            pushTemplate.imageUrl,
            pushTemplate.title,
            pushTemplate.expandedBodyText
        )

        // small Icon must be present, otherwise the notification will not be displayed.
        AEPPushTemplateNotificationBuilder.setSmallIcon(
            context,
            builder,
            pushTemplate.smallIcon,
            pushTemplate.smallIconColor
        )

        // set notification visibility
        AEPPushTemplateNotificationBuilder.setVisibility(
            builder, pushTemplate.getNotificationVisibility()
        )

        // add any action buttons defined for the notification
        BasicTemplateNotificationBuilder.addActionButtons(
            context,
            trackerActivityClass,
            builder,
            pushTemplate.actionButtonsString,
            pushTemplate.tag,
            pushTemplate.isNotificationSticky ?: false
        )

        // set custom sound, note this applies to API 25 and lower only as API 26 and up set the
        // sound on the notification channel
        AEPPushTemplateNotificationBuilder.setSound(context, builder, pushTemplate.sound)

        // assign a click action pending intent to the notification
        AEPPushTemplateNotificationBuilder.setNotificationClickAction(
            context,
            trackerActivityClass,
            builder,
            pushTemplate.actionUri,
            pushTemplate.tag,
            pushTemplate.isNotificationSticky ?: false
        )

        // set notification delete action
        AEPPushTemplateNotificationBuilder.setNotificationDeleteAction(
            context,
            trackerActivityClass,
            builder
        )

        // if API level is below 26 (prior to notification channels) then notification priority is
        // set on the notification builder
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(LongArray(0)) // hack to enable heads up notifications as a HUD style
            // notification requires a tone or vibration
        }

        return builder
    }
}
