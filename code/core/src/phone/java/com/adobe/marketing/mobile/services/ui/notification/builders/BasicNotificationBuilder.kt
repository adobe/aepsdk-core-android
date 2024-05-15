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
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.adobe.marketing.mobile.core.R
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ui.notification.NotificationConstructionFailedException
import com.adobe.marketing.mobile.services.ui.notification.PushTemplateConstants
import com.adobe.marketing.mobile.services.ui.notification.PushTemplateImageUtils
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
        val downloadedImageCount = PushTemplateImageUtils.cacheImages(listOf(imageUri))

        if (downloadedImageCount == 0) {
            Log.trace(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                "No image found for basic push template."
            )
            expandedLayout.setViewVisibility(R.id.expanded_template_image, View.GONE)
        } else {
            expandedLayout.setImageViewBitmap(
                R.id.expanded_template_image,
                PushTemplateImageUtils.getCachedImage(imageUri)
            )
        }

        // add any action buttons defined for the notification
        notificationBuilder.addActionButtons(
            context,
            trackerActivityClass,
            pushTemplate.actionButtonsList,
            pushTemplate.tag,
            pushTemplate.isNotificationSticky ?: false
        )

        // add a remind later button if we have a label and an epoch or delay timestamp
        pushTemplate.remindLaterText?.let { remindLaterText ->
            if (pushTemplate.remindLaterEpochTimestamp != null ||
                pushTemplate.remindLaterDelaySeconds != null
            ) {
                val remindIntent = createRemindPendingIntent(
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

    /**
     * Creates a pending intent for remind later button in a notification.
     *
     * @param context the application [Context]
     * @param broadcastReceiverClass the [Class] of the broadcast receiver to set in the created pending intent
     * @param channelId [String] containing the notification channel ID
     * @param pushTemplate the [BasicPushTemplate] object containing the basic push template data
     * @return the created remind later [PendingIntent]
     */
    private fun createRemindPendingIntent(
        context: Context,
        broadcastReceiverClass: Class<out BroadcastReceiver>?,
        channelId: String,
        pushTemplate: BasicPushTemplate
    ): PendingIntent? {
        if (broadcastReceiverClass == null) {
            return null
        }
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Creating a remind later pending intent from a push template object."
        )

        val remindIntent = Intent(PushTemplateConstants.IntentActions.REMIND_LATER_CLICKED).apply {
            setClass(context.applicationContext, broadcastReceiverClass)

            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(
                PushTemplateConstants.IntentKeys.TEMPLATE_TYPE, pushTemplate.templateType?.value
            )
            putExtra(
                PushTemplateConstants.IntentKeys.IMAGE_URI, pushTemplate.imageUrl
            )
            putExtra(
                PushTemplateConstants.IntentKeys.ACTION_URI, pushTemplate.actionUri
            )
            putExtra(PushTemplateConstants.IntentKeys.CHANNEL_ID, channelId)
            putExtra(
                PushTemplateConstants.IntentKeys.CUSTOM_SOUND, pushTemplate.sound
            )
            putExtra(
                PushTemplateConstants.IntentKeys.TITLE_TEXT,
                pushTemplate.title
            )
            putExtra(
                PushTemplateConstants.IntentKeys.BODY_TEXT,
                pushTemplate.body
            )
            putExtra(
                PushTemplateConstants.IntentKeys.EXPANDED_BODY_TEXT,
                pushTemplate.expandedBodyText
            )
            putExtra(
                PushTemplateConstants.IntentKeys.NOTIFICATION_BACKGROUND_COLOR,
                pushTemplate.notificationBackgroundColor
            )
            putExtra(
                PushTemplateConstants.IntentKeys.TITLE_TEXT_COLOR,
                pushTemplate.titleTextColor
            )
            putExtra(
                PushTemplateConstants.IntentKeys.EXPANDED_BODY_TEXT_COLOR,
                pushTemplate.expandedBodyTextColor
            )
            putExtra(
                PushTemplateConstants.IntentKeys.SMALL_ICON, pushTemplate.smallIcon
            )
            putExtra(
                PushTemplateConstants.IntentKeys.SMALL_ICON_COLOR,
                pushTemplate.smallIconColor
            )
            putExtra(
                PushTemplateConstants.IntentKeys.LARGE_ICON, pushTemplate.largeIcon
            )
            putExtra(
                PushTemplateConstants.IntentKeys.VISIBILITY,
                pushTemplate.getNotificationVisibility()
            )
            putExtra(
                PushTemplateConstants.IntentKeys.IMPORTANCE,
                pushTemplate.getNotificationImportance()
            )
            putExtra(
                PushTemplateConstants.IntentKeys.BADGE_COUNT, pushTemplate.badgeCount
            )
            putExtra(
                PushTemplateConstants.IntentKeys.REMIND_EPOCH_TS,
                pushTemplate.remindLaterEpochTimestamp
            )
            putExtra(
                PushTemplateConstants.IntentKeys.REMIND_DELAY_SECONDS,
                pushTemplate.remindLaterDelaySeconds
            )
            putExtra(
                PushTemplateConstants.IntentKeys.REMIND_LABEL, pushTemplate.remindLaterText
            )
            putExtra(
                PushTemplateConstants.IntentKeys.ACTION_BUTTONS_STRING,
                pushTemplate.actionButtonsString
            )
            putExtra(
                PushTemplateConstants.IntentKeys.STICKY, pushTemplate.isNotificationSticky
            )
            putExtra(
                PushTemplateConstants.IntentKeys.TAG, pushTemplate.tag
            )
            putExtra(
                PushTemplateConstants.IntentKeys.TICKER, pushTemplate.ticker
            )
            putExtra(
                PushTemplateConstants.IntentKeys.PAYLOAD_VERSION, pushTemplate.payloadVersion
            )
            putExtra(
                PushTemplateConstants.IntentKeys.PRIORITY,
                pushTemplate.notificationPriority
            )
        }

        return PendingIntent.getBroadcast(
            context,
            0,
            remindIntent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}
