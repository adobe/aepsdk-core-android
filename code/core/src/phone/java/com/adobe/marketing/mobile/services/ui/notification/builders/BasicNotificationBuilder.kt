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
import android.content.BroadcastReceiver
import android.content.Context
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.adobe.marketing.mobile.core.R
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.ui.notification.NotificationConstructionFailedException
import com.adobe.marketing.mobile.services.ui.notification.PendingIntentUtils
import com.adobe.marketing.mobile.services.ui.notification.PushTemplateConstants
import com.adobe.marketing.mobile.services.ui.notification.PushTemplateImageUtil
import com.adobe.marketing.mobile.services.ui.notification.extensions.addActionButtons
import com.adobe.marketing.mobile.services.ui.notification.extensions.createNotificationChannelIfRequired
import com.adobe.marketing.mobile.services.ui.notification.templates.BasicPushTemplate

/**
 * Object responsible for constructing a [NotificationCompat.Builder] object containing a basic push template notification.
 */
internal object BasicNotificationBuilder {
    private const val SELF_TAG = "BasicTemplateNotificationBuilder"

    @Throws(NotificationConstructionFailedException::class)
    fun construct(
        context: Context,
        pushTemplate: BasicPushTemplate,
        trackerActivityClass: Class<out Activity>?,
        broadcastReceiverClass: Class<out BroadcastReceiver>?
    ): NotificationCompat.Builder {
        val cacheService = ServiceProvider.getInstance().cacheService
            ?: throw NotificationConstructionFailedException("Cache service is null, basic template notification will not be constructed.")

        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Building a basic template push notification."
        )
        val packageName = context.packageName
        val smallLayout = RemoteViews(packageName, R.layout.push_template_collapsed)
        val expandedLayout = RemoteViews(packageName, R.layout.push_template_expanded)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelIdToUse: String = notificationManager.createNotificationChannelIfRequired(
            context,
            pushTemplate.channelId,
            pushTemplate.sound,
            pushTemplate.getNotificationImportance(),
            pushTemplate.isFromIntent
        )

        // create the notification builder with the common settings applied
        val notificationBuilder = AEPPushNotificationBuilder.construct(
            context,
            pushTemplate,
            channelIdToUse,
            trackerActivityClass,
            smallLayout,
            expandedLayout,
            R.id.basic_expanded_layout
        )

        // set the image on the notification
        val imageUri = pushTemplate.imageUrl
        val pushImage = PushTemplateImageUtil.downloadImage(cacheService, imageUri)

        if (pushImage != null) {
            expandedLayout.setImageViewBitmap(R.id.expanded_template_image, pushImage)
        } else {
            Log.trace(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                "No image found for basic push template."
            )
            expandedLayout.setViewVisibility(R.id.expanded_template_image, View.GONE)
        }

        // add any action buttons defined for the notification
        notificationBuilder.addActionButtons(
            context,
            trackerActivityClass,
            pushTemplate.actionButtonsString,
            pushTemplate.tag,
            pushTemplate.isNotificationSticky ?: false
        )

        // add a remind later button if we have a label and an epoch or delay timestamp
        pushTemplate.remindLaterText?.let { remindLaterText ->
            if (pushTemplate.remindLaterEpochTimestamp != null ||
                pushTemplate.remindLaterDelaySeconds != null
            ) {
                val remindIntent = PendingIntentUtils.createRemindPendingIntent(
                    context,
                    broadcastReceiverClass,
                    channelIdToUse,
                    pushTemplate
                )
                notificationBuilder.addAction(0, remindLaterText, remindIntent)
            }
        }

        return notificationBuilder
    }

    @Throws(NotificationConstructionFailedException::class)
    internal fun fallbackToBasicNotification(
        context: Context,
        trackerActivityClass: Class<out Activity>?,
        broadcastReceiverClass: Class<out BroadcastReceiver>?,
        dataMap: MutableMap<String, String>
    ): NotificationCompat.Builder {
        val basicPushTemplate = BasicPushTemplate(dataMap)
        return construct(
            context,
            basicPushTemplate,
            trackerActivityClass,
            broadcastReceiverClass
        )
    }
}
