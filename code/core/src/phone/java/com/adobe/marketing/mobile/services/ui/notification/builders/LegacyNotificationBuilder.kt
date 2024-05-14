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

package com.adobe.marketing.mobile.services.ui.notification.builders

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ui.notification.PushTemplateConstants
import com.adobe.marketing.mobile.services.ui.notification.extensions.addActionButtons
import com.adobe.marketing.mobile.services.ui.notification.extensions.createNotificationChannelIfRequired
import com.adobe.marketing.mobile.services.ui.notification.extensions.setLargeIcon
import com.adobe.marketing.mobile.services.ui.notification.extensions.setNotificationClickAction
import com.adobe.marketing.mobile.services.ui.notification.extensions.setNotificationDeleteAction
import com.adobe.marketing.mobile.services.ui.notification.extensions.setSmallIcon
import com.adobe.marketing.mobile.services.ui.notification.extensions.setSound
import com.adobe.marketing.mobile.services.ui.notification.templates.BasicPushTemplate

/**
 * Object responsible for constructing a legacy push notification.
 */
internal object LegacyNotificationBuilder {
    private const val SELF_TAG = "LegacyNotificationBuilder"

    fun construct(
        context: Context,
        pushTemplate: BasicPushTemplate,
        trackerActivityClass: Class<out Activity>?
    ): NotificationCompat.Builder {
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Building a legacy style push notification."
        )

        // create the notification channel
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = notificationManager.createNotificationChannelIfRequired(
            context,
            pushTemplate.channelId,
            pushTemplate.sound,
            pushTemplate.getNotificationImportance(),
            pushTemplate.isFromIntent
        )

        // Create the notification builder object and set the ticker, title, body, and badge count
        val builder = NotificationCompat.Builder(context, channelId)
            .setTicker(pushTemplate.ticker)
            .setContentTitle(pushTemplate.title)
            .setContentText(pushTemplate.body)
            .setNumber(pushTemplate.badgeCount)
            // set a large icon if one is present
            .setLargeIcon(pushTemplate.imageUrl, pushTemplate.title, pushTemplate.expandedBodyText)
            // small Icon must be present, otherwise the notification will not be displayed.
            .setSmallIcon(context, pushTemplate.smallIcon, pushTemplate.smallIconColor)
            // set notification visibility
            .setVisibility(pushTemplate.getNotificationVisibility())
            // add any action buttons defined for the notification
            .addActionButtons(
                context,
                trackerActivityClass,
                pushTemplate.actionButtonsList,
                pushTemplate.tag,
                pushTemplate.isNotificationSticky ?: false
            )
            // set custom sound, note this applies to API 25 and lower only as API 26 and up set the
            // sound on the notification channel
            .setSound(context, pushTemplate.sound)
            // assign a click action pending intent to the notification
            .setNotificationClickAction(
                context,
                trackerActivityClass,
                pushTemplate.actionUri,
                pushTemplate.tag,
                pushTemplate.isNotificationSticky ?: false
            )
            // set notification delete action
            .setNotificationDeleteAction(
                context,
                trackerActivityClass
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
