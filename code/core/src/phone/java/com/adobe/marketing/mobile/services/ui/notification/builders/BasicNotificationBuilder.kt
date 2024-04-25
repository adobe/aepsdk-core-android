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
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.adobe.marketing.mobile.core.R
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.ui.notification.NotificationConstructionFailedException
import com.adobe.marketing.mobile.services.ui.notification.PushTemplateConstants
import com.adobe.marketing.mobile.services.ui.notification.PushTemplateImageUtil
import com.adobe.marketing.mobile.services.ui.notification.models.AEPPushTemplate
import com.adobe.marketing.mobile.services.ui.notification.models.BasicPushTemplate
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

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

        // create a silent notification channel if needed
        if (pushTemplate.isFromIntent == true && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            AEPPushNotificationBuilder.setupSilentNotificationChannel(
                notificationManager,
                pushTemplate.getNotificationImportance()
            )
        }

        // create the notification channel if needed
        val channelIdToUse = AEPPushNotificationBuilder.createChannel(
            context,
            pushTemplate.channelId,
            pushTemplate.sound,
            pushTemplate.getNotificationImportance()
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

        // get push payload data
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

        smallLayout.setTextViewText(R.id.notification_title, pushTemplate.title)
        smallLayout.setTextViewText(R.id.notification_body, pushTemplate.body)
        expandedLayout.setTextViewText(R.id.notification_title, pushTemplate.title)
        expandedLayout.setTextViewText(
            R.id.notification_body_expanded, pushTemplate.expandedBodyText
        )

        // add any action buttons defined for the notification
        addActionButtons(
            context,
            trackerActivityClass,
            notificationBuilder,
            pushTemplate.actionButtonsString,
            pushTemplate.tag,
            pushTemplate.isNotificationSticky ?: false
        )

        // add a remind later button if we have a label and an epoch or delay timestamp
        pushTemplate.remindLaterText?.let { remindLaterText ->
            if (pushTemplate.remindLaterEpochTimestamp != null ||
                pushTemplate.remindLaterDelaySeconds != null
            ) {
                val remindIntent =
                    createRemindPendingIntent(
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

    /**
     * Adds action buttons for the notification.
     *
     * @param context the application [Context]
     * @param trackerActivityClass the [Activity] class to use as the tracker activity
     * @param builder the [NotificationCompat.Builder] to attach the action buttons
     * @param actionButtonsString `String` a JSON string containing action buttons to attach
     * to the notification
     * notification
     * @param tag `String` containing the tag to use when scheduling the notification
     * @param stickyNotification [Boolean]  if false, remove the notification after the action
     * button is pressed
     */
    internal fun addActionButtons(
        context: Context,
        trackerActivityClass: Class<out Activity>?,
        builder: NotificationCompat.Builder,
        actionButtonsString: String?,
        tag: String?,
        stickyNotification: Boolean
    ) {
        val actionButtons: List<AEPPushTemplate.ActionButton>? =
            getActionButtonsFromString(actionButtonsString)
        if (actionButtons.isNullOrEmpty()) {
            return
        }
        for (eachButton in actionButtons) {
            val pendingIntent: PendingIntent? =
                if (eachButton.type === AEPPushTemplate.ActionType.DEEPLINK ||
                    eachButton.type === AEPPushTemplate.ActionType.WEBURL
                ) {
                    AEPPushNotificationBuilder.createPendingIntent(
                        context,
                        trackerActivityClass,
                        eachButton.link,
                        eachButton.label,
                        tag,
                        stickyNotification
                    )
                } else {
                    AEPPushNotificationBuilder.createPendingIntent(
                        context,
                        trackerActivityClass,
                        null,
                        eachButton.label,
                        tag,
                        stickyNotification
                    )
                }
            builder.addAction(0, eachButton.label, pendingIntent)
        }
    }

    private fun getActionButtonsFromString(actionButtons: String?): List<AEPPushTemplate.ActionButton>? {
        if (actionButtons == null) {
            Log.debug(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                "Exception in converting actionButtons json string to json object, Error :" +
                    " actionButtons is null"
            )
            return null
        }
        val actionButtonList = mutableListOf<AEPPushTemplate.ActionButton>()
        try {
            val jsonArray = JSONArray(actionButtons)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val button = getActionButton(jsonObject) ?: continue
                actionButtonList.add(button)
            }
        } catch (e: JSONException) {
            Log.warning(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                "Exception in converting actionButtons json string to json object, Error : ${e.localizedMessage}"
            )
            return null
        }
        return actionButtonList
    }

    private fun getActionButton(jsonObject: JSONObject): AEPPushTemplate.ActionButton? {
        return try {
            val label = jsonObject.getString(AEPPushTemplate.ActionButtons.LABEL)
            if (label.isEmpty()) {
                Log.debug(PushTemplateConstants.LOG_TAG, SELF_TAG, "Label is empty")
                return null
            }
            var uri: String? = null
            val type = jsonObject.getString(AEPPushTemplate.ActionButtons.TYPE)
            if (type == AEPPushTemplate.ActionButtonType.WEBURL || type == AEPPushTemplate.ActionButtonType.DEEPLINK) {
                uri = jsonObject.optString(AEPPushTemplate.ActionButtons.URI)
            }
            Log.trace(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                "Creating an ActionButton with label ($label), uri ($uri), and type ($type)."
            )
            AEPPushTemplate.ActionButton(label, uri, type)
        } catch (e: JSONException) {
            Log.warning(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                "Exception in converting actionButtons json string to json object, Error : ${e.localizedMessage}."
            )
            null
        }
    }

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

        val remindIntent = Intent(PushTemplateConstants.IntentActions.REMIND_LATER_CLICKED)
        broadcastReceiverClass.let {
            remindIntent.setClass(context.applicationContext, broadcastReceiverClass)
        }

        remindIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.TEMPLATE_TYPE, pushTemplate.templateType?.value
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.IMAGE_URI, pushTemplate.imageUrl
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.ACTION_URI, pushTemplate.actionUri
        )
        remindIntent.putExtra(PushTemplateConstants.IntentKeys.CHANNEL_ID, channelId)
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.CUSTOM_SOUND, pushTemplate.sound
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.TITLE_TEXT,
            pushTemplate.title
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.BODY_TEXT,
            pushTemplate.body
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.EXPANDED_BODY_TEXT,
            pushTemplate.expandedBodyText
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.NOTIFICATION_BACKGROUND_COLOR,
            pushTemplate.notificationBackgroundColor
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.TITLE_TEXT_COLOR,
            pushTemplate.titleTextColor
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.EXPANDED_BODY_TEXT_COLOR,
            pushTemplate.expandedBodyTextColor
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.SMALL_ICON, pushTemplate.smallIcon
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.SMALL_ICON_COLOR,
            pushTemplate.smallIconColor
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.LARGE_ICON, pushTemplate.largeIcon
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
            PushTemplateConstants.IntentKeys.BADGE_COUNT, pushTemplate.badgeCount
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.REMIND_EPOCH_TS, pushTemplate.remindLaterEpochTimestamp
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.REMIND_DELAY_SECONDS,
            pushTemplate.remindLaterDelaySeconds
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.REMIND_LABEL, pushTemplate.remindLaterText
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.ACTION_BUTTONS_STRING,
            pushTemplate.actionButtonsString
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.STICKY, pushTemplate.isNotificationSticky
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.TAG, pushTemplate.tag
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.TICKER, pushTemplate.ticker
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.PAYLOAD_VERSION, pushTemplate.payloadVersion
        )
        remindIntent.putExtra(
            PushTemplateConstants.IntentKeys.PRIORITY,
            pushTemplate.notificationPriority
        )

        return PendingIntent.getBroadcast(
            context,
            0,
            remindIntent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
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
