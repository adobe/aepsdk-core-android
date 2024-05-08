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
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ui.notification.templates.BasicPushTemplate
import com.adobe.marketing.mobile.services.ui.notification.templates.ManualCarouselPushTemplate
import java.util.Random

private const val SELF_TAG = "IntentUtils"
internal object PendingIntentUtils {

    /**
     * Creates a pending intent for a notification.
     *
     * @param context the application [Context]
     * @param trackerActivityClass the [Class] of the activity to set in the created pending intent for tracking purposes
     * notification
     * @param actionUri the action uri
     * @param actionID the action ID
     * @param stickyNotification [Boolean] if false, remove the notification after it is interacted with
     * @return the created [PendingIntent]
     */
    internal fun createPendingIntent(
        context: Context,
        trackerActivityClass: Class<out Activity>?,
        actionUri: String?,
        actionID: String?,
        tag: String?,
        stickyNotification: Boolean
    ): PendingIntent? {
        val intent = Intent(PushTemplateConstants.NotificationAction.BUTTON_CLICKED)
        trackerActivityClass?.let {
            intent.setClass(context.applicationContext, trackerActivityClass)
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra(PushTemplateConstants.PushPayloadKeys.TAG, tag)
        intent.putExtra(PushTemplateConstants.PushPayloadKeys.STICKY, stickyNotification)
        addActionDetailsToIntent(
            intent,
            actionUri,
            actionID
        )

        return PendingIntent.getActivity(
            context,
            Random().nextInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Adds action details to the provided [Intent].
     *
     * @param intent the intent
     * @param actionUri [String] containing the action uri
     * @param actionId `String` containing the action ID
     */
    private fun addActionDetailsToIntent(
        intent: Intent,
        actionUri: String?,
        actionId: String?
    ) {
        if (!actionUri.isNullOrEmpty()) {
            intent.putExtra(PushTemplateConstants.Tracking.TrackingKeys.ACTION_URI, actionUri)
        }
        if (!actionId.isNullOrEmpty()) {
            intent.putExtra(PushTemplateConstants.Tracking.TrackingKeys.ACTION_ID, actionId)
        }
    }

    /**
     * Creates a click intent for the specified [Intent] action. This intent is used to handle interactions
     * with the skip left and skip right buttons in a filmstrip or manual carousel push template notification.
     *
     * @param context the application [Context]
     * @param pushTemplate the [ManualCarouselPushTemplate] object containing the manual carousel push template data
     * @param intentAction [String] containing the intent action
     * @param broadcastReceiverClass the [Class] of the broadcast receiver to set in the created pending intent
     * @param downloadedImageUris [List] of String` containing the downloaded image URIs
     * @param imageCaptions `List` of String` containing the image captions
     * @param imageClickActions `List` of String` containing the image click actions
     * @return the created click [Intent]
     */
    internal fun createCarouselNavigationClickPendingIntent(
        context: Context,
        pushTemplate: ManualCarouselPushTemplate,
        intentAction: String,
        broadcastReceiverClass: Class<out BroadcastReceiver>?,
        downloadedImageUris: List<String?>,
        imageCaptions: List<String?>,
        imageClickActions: List<String?>,
        channelId: String
    ): PendingIntent {
        val clickIntent = Intent(intentAction).apply {
            broadcastReceiverClass?.let {
                setClass(context, broadcastReceiverClass)
            }

            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(
                PushTemplateConstants.IntentKeys.TEMPLATE_TYPE,
                pushTemplate.templateType?.value
            )
            putExtra(
                PushTemplateConstants.IntentKeys.CHANNEL_ID,
                channelId
            )
            putExtra(
                PushTemplateConstants.IntentKeys.CUSTOM_SOUND, pushTemplate.sound
            )
            putExtra(
                PushTemplateConstants.IntentKeys.CENTER_IMAGE_INDEX,
                pushTemplate.centerImageIndex
            )
            putExtra(
                PushTemplateConstants.IntentKeys.IMAGE_URLS,
                downloadedImageUris.toTypedArray()
            )
            putExtra(
                PushTemplateConstants.IntentKeys.IMAGE_CAPTIONS,
                imageCaptions.toTypedArray()
            )
            putExtra(
                PushTemplateConstants.IntentKeys.IMAGE_CLICK_ACTIONS,
                imageClickActions.toTypedArray()
            )
            putExtra(PushTemplateConstants.IntentKeys.TITLE_TEXT, pushTemplate.title)
            putExtra(PushTemplateConstants.IntentKeys.BODY_TEXT, pushTemplate.body)
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
                PushTemplateConstants.IntentKeys.LARGE_ICON, pushTemplate.largeIcon
            )
            putExtra(
                PushTemplateConstants.IntentKeys.SMALL_ICON_COLOR,
                pushTemplate.smallIconColor
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
                PushTemplateConstants.IntentKeys.TICKER, pushTemplate.ticker
            )
            putExtra(
                PushTemplateConstants.IntentKeys.TAG, pushTemplate.tag
            )
            putExtra(
                PushTemplateConstants.IntentKeys.STICKY, pushTemplate.isNotificationSticky
            )
            putExtra(PushTemplateConstants.IntentKeys.ACTION_URI, pushTemplate.actionUri)
            putExtra(
                PushTemplateConstants.IntentKeys.PAYLOAD_VERSION, pushTemplate.payloadVersion
            )
            putExtra(
                PushTemplateConstants.IntentKeys.CAROUSEL_ITEMS,
                pushTemplate.rawCarouselItems
            )
            putExtra(
                PushTemplateConstants.IntentKeys.CAROUSEL_LAYOUT_TYPE,
                pushTemplate.carouselLayoutType
            )
        }

        return PendingIntent.getBroadcast(
            context,
            0,
            clickIntent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    /**
     * Creates a remind later pending intent for a notification.
     *
     * @param context the application [Context]
     * @param broadcastReceiverClass the [Class] of the broadcast receiver to set in the created pending intent
     * @param channelId [String] containing the notification channel ID
     * @param pushTemplate the [BasicPushTemplate] object containing the basic push template data
     * @return the created remind later [PendingIntent]
     */
    internal fun createRemindPendingIntent(
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
