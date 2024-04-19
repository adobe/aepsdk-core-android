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
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.adobe.marketing.mobile.core.R
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceProvider

/**
 * Object responsible for constructing a [NotificationCompat.Builder] object containing an input box push template notification.
 */
internal object InputBoxTemplateNotificationBuilder {
    private const val SELF_TAG = "InputBoxPushTemplateNotificationBuilder"

    @Throws(NotificationConstructionFailedException::class)
    fun construct(
        context: Context,
        pushTemplate: InputBoxPushTemplate?,
        trackerActivityClass: Class<out Activity>?,
        broadcastReceiverClass: Class<out BroadcastReceiver>?
    ): NotificationCompat.Builder {
        if (pushTemplate == null) {
            throw NotificationConstructionFailedException(
                "push template is null, cannot build an input box template notification."
            )
        }
        val cacheService = ServiceProvider.getInstance().cacheService
            ?: throw NotificationConstructionFailedException("Cache service is null, input box template notification will not be constructed.")

        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Building an input box template push notification."
        )
        val packageName = context.packageName
        val smallLayout = RemoteViews(packageName, R.layout.push_template_collapsed)
        val expandedLayout = RemoteViews(packageName, R.layout.push_template_expanded)

        // Create the notification channel if needed
        val channelIdToUse = AEPPushTemplateNotificationBuilder.createChannelAndGetChannelID(
            context,
            pushTemplate.channelId,
            pushTemplate.sound,
            pushTemplate.getNotificationImportance()
        )

        // create the notification builder with the common settings applied
        val notificationBuilder = AEPPushTemplateNotificationBuilder.construct(
            context,
            pushTemplate,
            channelIdToUse,
            trackerActivityClass,
            smallLayout,
            expandedLayout
        )

        // get push payload data
        val imageUri: String
        val notificationBodyText: String
        if (pushTemplate.isFromIntent != true) {
            imageUri = pushTemplate.imageUrl ?: ""
            notificationBodyText = pushTemplate.body ?: ""
        } else {
            imageUri = pushTemplate.feedbackImage ?: ""
            notificationBodyText = pushTemplate.feedbackText ?: ""
        }

        val pushImage = PushTemplateHelpers.downloadImage(cacheService, imageUri)
        pushImage?.let {
            expandedLayout.setImageViewBitmap(R.id.expanded_template_image, pushImage)
        }
        smallLayout.setTextViewText(R.id.notification_title, pushTemplate.title)
        smallLayout.setTextViewText(R.id.notification_body, notificationBodyText)
        expandedLayout.setTextViewText(R.id.notification_title, pushTemplate.title)
        expandedLayout.setTextViewText(
            R.id.notification_body_expanded, notificationBodyText
        )

        // add an input box to capture user feedback
        if (pushTemplate.isFromIntent != true && !pushTemplate.inputBoxIntentActionName.isNullOrEmpty()) {
            addInputTextAction(
                context,
                trackerActivityClass,
                broadcastReceiverClass,
                notificationBuilder,
                channelIdToUse,
                pushTemplate)
        }

        return notificationBuilder
    }

    /**
     * Adds an input text action for the notification.
     *
     * @param context the application [Context]
     * @param trackerActivityClass the [Activity] class to use as the tracker activity
     * @param broadcastReceiverClass the [BroadcastReceiver] class to use as the broadcast receiver
     * @param builder the [NotificationCompat.Builder] to attach the action buttons
     * @param channelId the [String] containing the channel ID to use for the notification
     * @param pushTemplate the [InputBoxPushTemplate] object containing the input box push template data
     * button is pressed
     */
    private fun addInputTextAction(
        context: Context,
        trackerActivityClass: Class<out Activity>?,
        broadcastReceiverClass: Class<out BroadcastReceiver>?,
        builder: NotificationCompat.Builder,
        channelId: String,
        pushTemplate: InputBoxPushTemplate
    ) {
        val remoteInput = pushTemplate.inputBoxIntentActionName?.let {
            androidx.core.app.RemoteInput.Builder(it)
                .setLabel(pushTemplate.inputTextHint)
                .build()
        }

        if (remoteInput == null) {
            Log.warning(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                "Failed to create a remote input for the input box action, will not add the action."
            )
            return
        }

        val inputReceivedIntent = createInputReceivedIntent(
            context,
            trackerActivityClass,
            broadcastReceiverClass,
            channelId,
            pushTemplate
        )

        val replyPendingIntent =
            inputReceivedIntent?.let {
                PendingIntent.getBroadcast(
                    context,
                    pushTemplate.tag.hashCode(),
                    it,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
            }

        val action =
            NotificationCompat.Action.Builder(R.drawable.ic_reply_icon,
                pushTemplate.inputTextHint, replyPendingIntent)
                .addRemoteInput(remoteInput)
                .build()

        builder.addAction(action)
    }

    private fun createInputReceivedIntent(
        context: Context,
        trackerActivityClass: Class<out Activity>?,
        broadcastReceiverClass: Class<out BroadcastReceiver>?,
        channelId: String,
        pushTemplate: InputBoxPushTemplate
    ): Intent? {
        if (broadcastReceiverClass == null) {
            return null
        }
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Creating a text input received intent from a push template object."
        )

        val inputReceivedIntent = Intent(pushTemplate.inputBoxIntentActionName)
        broadcastReceiverClass.let {
            inputReceivedIntent.setClass(context.applicationContext, broadcastReceiverClass)
        }

        inputReceivedIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        inputReceivedIntent.putExtra(
            PushTemplateConstants.IntentKeys.TEMPLATE_TYPE, pushTemplate.templateType?.value
        )
        inputReceivedIntent.putExtra(PushTemplateConstants.IntentKeys.TRACKER_NAME, trackerActivityClass)
        inputReceivedIntent.putExtra(
            PushTemplateConstants.IntentKeys.BROADCAST_RECEIVER_NAME,
            broadcastReceiverClass
        )
        inputReceivedIntent.putExtra(
            PushTemplateConstants.IntentKeys.IMAGE_URI, pushTemplate.imageUrl
        )
        inputReceivedIntent.putExtra(
            PushTemplateConstants.IntentKeys.ACTION_URI, pushTemplate.actionUri
        )
        inputReceivedIntent.putExtra(PushTemplateConstants.IntentKeys.CHANNEL_ID, channelId)
        inputReceivedIntent.putExtra(
            PushTemplateConstants.IntentKeys.CUSTOM_SOUND, pushTemplate.sound
        )
        inputReceivedIntent.putExtra(
            PushTemplateConstants.IntentKeys.TITLE_TEXT,
            pushTemplate.title
        )
        inputReceivedIntent.putExtra(
            PushTemplateConstants.IntentKeys.BODY_TEXT,
            pushTemplate.body
        )
        inputReceivedIntent.putExtra(
            PushTemplateConstants.IntentKeys.EXPANDED_BODY_TEXT,
            pushTemplate.expandedBodyText
        )
        inputReceivedIntent.putExtra(
            PushTemplateConstants.IntentKeys.NOTIFICATION_BACKGROUND_COLOR,
            pushTemplate.notificationBackgroundColor
        )
        inputReceivedIntent.putExtra(
            PushTemplateConstants.IntentKeys.TITLE_TEXT_COLOR,
            pushTemplate.titleTextColor
        )
        inputReceivedIntent.putExtra(
            PushTemplateConstants.IntentKeys.EXPANDED_BODY_TEXT_COLOR,
            pushTemplate.expandedBodyTextColor
        )
        inputReceivedIntent.putExtra(
            PushTemplateConstants.IntentKeys.SMALL_ICON, pushTemplate.smallIcon
        )
        inputReceivedIntent.putExtra(
            PushTemplateConstants.IntentKeys.SMALL_ICON_COLOR,
            pushTemplate.smallIconColor
        )
        inputReceivedIntent.putExtra(
            PushTemplateConstants.IntentKeys.LARGE_ICON, pushTemplate.largeIcon
        )
        inputReceivedIntent.putExtra(
            PushTemplateConstants.IntentKeys.VISIBILITY,
            pushTemplate.getNotificationVisibility()
        )
        inputReceivedIntent.putExtra(
            PushTemplateConstants.IntentKeys.IMPORTANCE,
            pushTemplate.getNotificationImportance()
        )
        inputReceivedIntent.putExtra(
            PushTemplateConstants.IntentKeys.BADGE_COUNT, pushTemplate.badgeCount
        )
        inputReceivedIntent.putExtra(
            PushTemplateConstants.IntentKeys.INPUT_BOX_FEEDBACK_TEXT, pushTemplate.feedbackText
        )
        inputReceivedIntent.putExtra(
            PushTemplateConstants.IntentKeys.INPUT_BOX_FEEDBACK_IMAGE, pushTemplate.feedbackImage
        )
        inputReceivedIntent.putExtra(
            PushTemplateConstants.IntentKeys.ACTION_BUTTONS_STRING,
            pushTemplate.actionButtonsString
        )
        inputReceivedIntent.putExtra(
            PushTemplateConstants.IntentKeys.STICKY, pushTemplate.isNotificationSticky
        )
        inputReceivedIntent.putExtra(
            PushTemplateConstants.IntentKeys.TAG, pushTemplate.tag
        )
        inputReceivedIntent.putExtra(
            PushTemplateConstants.IntentKeys.TICKER, pushTemplate.ticker
        )
        inputReceivedIntent.putExtra(
            PushTemplateConstants.IntentKeys.PAYLOAD_VERSION, pushTemplate.payloadVersion
        )
        inputReceivedIntent.putExtra(
            PushTemplateConstants.IntentKeys.PRIORITY,
            pushTemplate.notificationPriority
        )

        return inputReceivedIntent
    }
}
