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
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.adobe.marketing.mobile.core.R
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.util.StringUtils

/**
 * Object responsible for constructing a push notification using the basic push template.
 */
internal object BasicTemplateNotificationBuilder {
    private const val SELF_TAG = "BasicTemplateNotificationBuilder"

    @Throws(NotificationConstructionFailedException::class)
    internal fun construct(
        context: Context,
        trackerActivity: Activity,
        broadcastReceiver: BroadcastReceiver,
        pushTemplate: BasicPushTemplate
    ): NotificationCompat.Builder {
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Building a basic template push notification."
        )

        val channelIdToUse = createChannelAndGetChannelID(
            context,
            pushTemplate.getChannelId(),
            pushTemplate.getSound(),
            pushTemplate.getNotificationImportance()
        )

        val packageName = ServiceProvider.getInstance()
            .appContextService
            .application
            ?.packageName
        val smallLayout = RemoteViews(packageName, R.layout.push_template_collapsed)
        val expandedLayout = RemoteViews(packageName, R.layout.push_template_expanded)
        val cacheService = ServiceProvider.getInstance().cacheService
            ?: throw NotificationConstructionFailedException(
                (
                    "Cache service is null, basic template push notification will not be" +
                        " constructed."
                    )
            )

        // get push payload data
        val imageUri = pushTemplate.getImageUrl()
        val pushImage = downloadImage(cacheService, imageUri)
        pushImage?.let {
            expandedLayout.setImageViewBitmap(R.id.expanded_template_image, pushImage)
        }
        smallLayout.setTextViewText(R.id.notification_title, pushTemplate.getTitle())
        smallLayout.setTextViewText(R.id.notification_body, pushTemplate.getBody())
        expandedLayout.setTextViewText(R.id.notification_title, pushTemplate.getTitle())
        expandedLayout.setTextViewText(
            R.id.notification_body_expanded, pushTemplate.getExpandedBodyText()
        )

        // set any custom colors if needed
        setCustomNotificationColors(
            pushTemplate.getNotificationBackgroundColor(),
            pushTemplate.getTitleTextColor(),
            pushTemplate.getExpandedBodyTextColor(),
            smallLayout,
            expandedLayout,
            R.id.basic_expanded_layout
        )

        // Create the notification
        val builder = NotificationCompat.Builder(context, channelIdToUse)
            .setTicker(pushTemplate.getTicker())
            .setNumber(pushTemplate.getBadgeCount())
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(smallLayout)
            .setCustomBigContentView(expandedLayout)

        // small icon must be present, otherwise the notification will not be displayed.
        setSmallIcon(
            context, builder, pushTemplate.getSmallIcon(), pushTemplate.getSmallIconColor()
        )

        // set a large icon if one is present
        setRemoteViewLargeIcon(pushTemplate.getLargeIcon(), smallLayout)
        setRemoteViewLargeIcon(
            pushTemplate.getLargeIcon(), expandedLayout
        )

        // set notification visibility
        setVisibility(
            builder, pushTemplate.getNotificationVisibility()
        )

        // add any action buttons defined for the notification
        addActionButtons(
            context,
            trackerActivity,
            builder,
            pushTemplate.getActionButtons(),
            pushTemplate.getMessageId(),
            pushTemplate.getDeliveryId(),
            pushTemplate.getTag(),
            pushTemplate.getStickyStatus() ?: false
        )

        // add a remind later button if we have a label and a timestamp
        pushTemplate.getRemindLaterText()?.let { remindLaterText ->
            pushTemplate.getRemindLaterTimestamp()?.let {
                val remindIntent =
                    createRemindPendingIntent(
                        context,
                        trackerActivity,
                        broadcastReceiver,
                        channelIdToUse,
                        pushTemplate
                    )
                builder.addAction(0, remindLaterText, remindIntent)
            }
        }

        // set custom sound, note this applies to API 25 and lower only as API 26 and up set the
        // sound on the notification channel
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

        // if API level is below 26 (prior to notification channels) then notification priority is
        // set on the notification builder
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(LongArray(0)) // hack to enable heads up notifications as a HUD style
            // notification requires a tone or vibration
        }
        return builder
    }

    @Throws(NotificationConstructionFailedException::class)
    internal fun construct(
        context: Context,
        trackerActivity: Activity,
        broadcastReceiver: BroadcastReceiver,
        intent: Intent
    ): NotificationCompat.Builder {
        val intentExtras = intent.extras
            ?: throw NotificationConstructionFailedException(
                (
                    "Intent extras are null, will not create a notification from the received" +
                        " intent with action " +
                        intent.action
                    )
            )
        val cacheService = ServiceProvider.getInstance().cacheService
            ?: throw NotificationConstructionFailedException(
                (
                    "Cache service is null, basic template push notification will not be" +
                        " constructed."
                    )
            )
        val packageName = ServiceProvider.getInstance()
            .appContextService
            .application
            ?.packageName

        // get basic notification values from the intent extras
        val titleText = intentExtras.getString(PushTemplateConstants.IntentKeys.TITLE_TEXT)
        val bodyText = intentExtras.getString(PushTemplateConstants.IntentKeys.BODY_TEXT)
        val expandedBodyText =
            intentExtras.getString(PushTemplateConstants.IntentKeys.EXPANDED_BODY_TEXT)
        val imageUri = intentExtras.getString(PushTemplateConstants.IntentKeys.IMAGE_URI)
        val pushImage = downloadImage(cacheService, imageUri)
        val smallLayout = RemoteViews(packageName, R.layout.push_template_collapsed)
        val expandedLayout = RemoteViews(packageName, R.layout.push_template_expanded)
        if (pushImage != null) {
            expandedLayout.setImageViewBitmap(R.id.expanded_template_image, pushImage)
        }
        smallLayout.setTextViewText(R.id.notification_title, titleText)
        smallLayout.setTextViewText(R.id.notification_body, bodyText)
        expandedLayout.setTextViewText(R.id.notification_title, titleText)
        expandedLayout.setTextViewText(R.id.notification_body_expanded, expandedBodyText)
        val actionUri = intentExtras.getString(PushTemplateConstants.IntentKeys.ACTION_URI)
        val messageId =
            intentExtras.getString(PushTemplateConstants.IntentKeys.MESSAGE_ID) as String
        val deliveryId =
            intentExtras.getString(PushTemplateConstants.IntentKeys.DELIVERY_ID) as String
        val remindLaterTimestamp =
            intentExtras.getLong(PushTemplateConstants.IntentKeys.REMIND_TS)
        val remindLaterText =
            intentExtras.getString(PushTemplateConstants.IntentKeys.REMIND_LABEL)
        val badgeCount = intentExtras.getInt(PushTemplateConstants.IntentKeys.BADGE_COUNT)
        val visibility = intentExtras.getInt(PushTemplateConstants.IntentKeys.VISIBILITY)
        val importance = intentExtras.getInt(PushTemplateConstants.IntentKeys.IMPORTANCE)
        val channelId = intentExtras.getString(PushTemplateConstants.IntentKeys.CHANNEL_ID)
        val notificationBackgroundColor = intentExtras.getString(
            PushTemplateConstants.IntentKeys.NOTIFICATION_BACKGROUND_COLOR
        )
        val titleTextColor =
            intentExtras.getString(PushTemplateConstants.IntentKeys.TITLE_TEXT_COLOR)
        val expandedBodyTextColor =
            intentExtras.getString(PushTemplateConstants.IntentKeys.EXPANDED_BODY_TEXT_COLOR)
        val smallIcon = intentExtras.getString(PushTemplateConstants.IntentKeys.SMALL_ICON)
        val smallIconColor =
            intentExtras.getString(PushTemplateConstants.IntentKeys.SMALL_ICON_COLOR)
        val largeIcon = intentExtras.getString(PushTemplateConstants.IntentKeys.LARGE_ICON)
        val customSound = intentExtras.getString(PushTemplateConstants.IntentKeys.CUSTOM_SOUND)
        val actionButtonsString =
            intentExtras.getString(PushTemplateConstants.IntentKeys.ACTION_BUTTONS_STRING)
        val ticker = intentExtras.getString(PushTemplateConstants.IntentKeys.TICKER)
        val tag = intentExtras.getString(PushTemplateConstants.IntentKeys.TAG) as String
        val sticky = intentExtras.getBoolean(PushTemplateConstants.IntentKeys.STICKY)
        val channelIdToUse = createChannelAndGetChannelID(
            context, channelId, customSound, importance
        )

        // set any custom colors if needed
        setCustomNotificationColors(
            notificationBackgroundColor,
            titleTextColor,
            expandedBodyTextColor,
            smallLayout,
            expandedLayout,
            R.id.basic_expanded_layout
        )

        // Create the notification
        val builder = NotificationCompat.Builder(context, channelIdToUse)
            .setTicker(ticker)
            .setNumber(badgeCount)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(smallLayout)
            .setCustomBigContentView(expandedLayout)

        // small Icon must be present, otherwise the notification will not be displayed.
        setSmallIcon(context, builder, smallIcon, smallIconColor)

        // set a large icon if one is present
        setRemoteViewLargeIcon(largeIcon, smallLayout)
        setRemoteViewLargeIcon(largeIcon, expandedLayout)

        // set notification visibility
        setVisibility(builder, visibility)

        // add any action buttons defined for the notification
        addActionButtons(
            context,
            trackerActivity,
            builder,
            actionButtonsString,
            messageId,
            deliveryId,
            tag,
            sticky
        ) // Add action buttons if any

        // add a remind later button if we have a label and a timestamp
        if (!StringUtils.isNullOrEmpty(remindLaterText) && remindLaterTimestamp > 0) {
            val remindPendingIntent =
                createRemindPendingIntent(context, intentExtras, broadcastReceiver)
            builder.addAction(0, remindLaterText, remindPendingIntent)
        }

        // set custom sound, note this applies to API 25 and lower only as API 26 and up set the
        // sound on the notification channel
        setSound(context, builder, customSound)
        setNotificationClickAction(
            context, trackerActivity, builder, messageId, deliveryId, actionUri, tag, sticky
        )
        setNotificationDeleteAction(
            context, trackerActivity, builder, messageId, deliveryId
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

    private fun createRemindPendingIntent(
        context: Context,
        trackerActivity: Activity,
        broadcastReceiver: BroadcastReceiver,
        channelId: String,
        pushTemplate: AEPPushTemplate
    ): PendingIntent {
        val remindIntent = Intent(
            PushTemplateConstants.IntentActions.REMIND_LATER_CLICKED,
            null,
            context,
            trackerActivity::class.java
        )
        remindIntent.setClass(context, broadcastReceiver::class.java)
        remindIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.IMAGE_URI, pushTemplate.getImageUrl()
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.ACTION_URI, pushTemplate.getActionUri()
        )
        remindIntent.putExtra(PushTemplateConstants.IntentKeys.CHANNEL_ID, channelId)
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.CUSTOM_SOUND, pushTemplate.getSound()
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.TITLE_TEXT,
            pushTemplate.getTitle()
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.BODY_TEXT,
            pushTemplate.getBody()
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.EXPANDED_BODY_TEXT,
            pushTemplate.getExpandedBodyText()
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.NOTIFICATION_BACKGROUND_COLOR,
            pushTemplate.getNotificationBackgroundColor()
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.TITLE_TEXT_COLOR,
            pushTemplate.getTitleTextColor()
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.EXPANDED_BODY_TEXT_COLOR,
            pushTemplate.getExpandedBodyTextColor()
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.MESSAGE_ID, pushTemplate.getMessageId()
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.DELIVERY_ID, pushTemplate.getDeliveryId()
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.SMALL_ICON, pushTemplate.getSmallIcon()
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.SMALL_ICON_COLOR,
            pushTemplate.getSmallIconColor()
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.LARGE_ICON, pushTemplate.getLargeIcon()
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.VISIBILITY,
            pushTemplate.getNotificationVisibility()
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.IMPORTANCE,
            pushTemplate.getNotificationImportance()
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.BADGE_COUNT, pushTemplate.getBadgeCount()
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.REMIND_TS, pushTemplate.getRemindLaterTimestamp()
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.REMIND_LABEL, pushTemplate.getRemindLaterText()
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.ACTION_BUTTONS_STRING,
            pushTemplate.getActionButtons()
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.STICKY, pushTemplate.getStickyStatus()
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.TAG, pushTemplate.getTag()
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.TICKER, pushTemplate.getTicker()
        )
        return PendingIntent.getBroadcast(
            context,
            0,
            remindIntent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun createRemindPendingIntent(
        context: Context,
        intentExtras: Bundle,
        broadcastReceiver: BroadcastReceiver
    ): PendingIntent {
        val remindIntent = Intent(
            PushTemplateConstants.IntentActions.REMIND_LATER_CLICKED,
            null,
            context,
            broadcastReceiver::class.java
        )
        remindIntent.setClass(context, broadcastReceiver::class.java)
        remindIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        remindIntent.putExtras(intentExtras)
        return PendingIntent.getBroadcast(
            context,
            0,
            remindIntent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}
