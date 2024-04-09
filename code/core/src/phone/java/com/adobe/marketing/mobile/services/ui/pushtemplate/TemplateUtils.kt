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
import android.content.BroadcastReceiver
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceProvider

/**
 * Public facing object to construct a [NotificationCompat.Builder] object for the specified [PushTemplateType].
 * The [constructNotificationBuilder] methods will build the appropriate notification based on the provided
 * [AEPPushTemplate] or [Intent].
 */
object TemplateUtils {
    private const val SELF_TAG = "TemplateUtils"

    @Throws(NotificationConstructionFailedException::class)
    @JvmStatic
    fun constructNotificationBuilder(
        broadcastReceiverName: String?,
        trackerActivityName: String?,
        messageData: Map<String, String>?
    ): NotificationCompat.Builder {
        if (messageData.isNullOrEmpty()) {
            throw NotificationConstructionFailedException("message data is null, cannot build a notification.")
        }

        val context = ServiceProvider.getInstance().appContextService.applicationContext ?: throw NotificationConstructionFailedException("Application context is null, cannot build a notification.")

        broadcastReceiverName?.let {
            PushTemplateTrackers.getInstance().setBroadcastReceiver(broadcastReceiverName, createClassInstance(context, broadcastReceiverName) as? BroadcastReceiver)
        }
        trackerActivityName?.let {
            PushTemplateTrackers.getInstance().setTrackerActivity(trackerActivityName, createClassInstance(context, trackerActivityName) as? Activity)
        }
        val pushTemplateType =
            PushTemplateType.fromString(messageData[PushTemplateConstants.PushPayloadKeys.TEMPLATE_TYPE])

        val builder: Any
        when (pushTemplateType) {
            PushTemplateType.BASIC -> {
                builder = BasicTemplateNotificationBuilder()
                    .pushTemplate(BasicPushTemplate(messageData as MutableMap<String, String>))
                    .trackerActivityName(trackerActivityName)
                    .broadcastReceiverName(broadcastReceiverName)
                return builder.build(context)
            }

            PushTemplateType.CAROUSEL -> {
                val carouselPushTemplate =
                    CarouselPushTemplate(messageData as MutableMap<String, String>)
                val carouselOperationMode = carouselPushTemplate.getCarouselOperationMode()
                val carouselType = carouselPushTemplate.getCarouselLayoutType()

                Log.trace(
                    PushTemplateConstants.LOG_TAG,
                    SELF_TAG,
                    "Building a $carouselType carousel style push notification."
                )

                if (carouselOperationMode.equals(PushTemplateConstants.DefaultValues.AUTO_CAROUSEL_MODE)) {
                    builder = AutoCarouselTemplateNotificationBuilder()
                        .pushTemplate(carouselPushTemplate)
                        .trackerActivityName(trackerActivityName)
                        .broadcastReceiverName(broadcastReceiverName)
                    return builder.build(context)
                } else {
                    return if (carouselType.equals(PushTemplateConstants.DefaultValues.FILMSTRIP_CAROUSEL_MODE)) {
                        builder = FilmstripCarouselTemplateNotificationBuilder()
                            .pushTemplate(carouselPushTemplate)
                            .trackerActivityName(trackerActivityName)
                            .broadcastReceiverName(broadcastReceiverName)
                        builder.build(context)
                    } else {
                        builder = ManualCarouselTemplateNotificationBuilder()
                            .pushTemplate(carouselPushTemplate)
                            .trackerActivityName(trackerActivityName)
                            .broadcastReceiverName(broadcastReceiverName)
                        builder.build(context)
                    }
                }
            }

            PushTemplateType.UNKNOWN -> {
                return LegacyNotificationBuilder()
                    .pushTemplate(BasicPushTemplate(messageData as MutableMap<String, String>))
                    .trackerActivityName(trackerActivityName)
                    .build(context)
            }
        }
        throw NotificationConstructionFailedException("Failed to build notification for the given push template type ${pushTemplateType.value}.")
    }

    @Throws(NotificationConstructionFailedException::class)
    @JvmStatic
    fun constructNotificationBuilder(
        intent: Intent?
    ): NotificationCompat.Builder {
        if (intent == null) {
            throw NotificationConstructionFailedException("intent is null, cannot build a notification.")
        }

        val context = ServiceProvider.getInstance().appContextService.applicationContext ?: throw NotificationConstructionFailedException("Application context is null, cannot build a notification.")
        // use the previously created tracker activity and/or broadcast receiver
        val trackerActivityName = intent.getStringExtra(PushTemplateConstants.IntentKeys.TRACKER_NAME)
        val broadcastReceiverName = intent.getStringExtra(PushTemplateConstants.IntentKeys.BROADCAST_RECEIVER_NAME)
        val pushTemplateType = PushTemplateType.fromString(intent.getStringExtra(PushTemplateConstants.IntentKeys.TYPE))

        val builder: Any
        when (pushTemplateType) {
            PushTemplateType.BASIC -> {
                Log.trace(
                    PushTemplateConstants.LOG_TAG,
                    SELF_TAG,
                    "Building a basic style push notification."
                )
                builder = BasicTemplateNotificationBuilder()
                    .intent(intent)
                    .trackerActivityName(trackerActivityName)
                    .broadcastReceiverName(broadcastReceiverName)
                return builder.build(context)
            }

            PushTemplateType.CAROUSEL -> {
                return if (intent.action.equals(PushTemplateConstants.IntentActions.MANUAL_CAROUSEL_LEFT_CLICKED) ||
                    intent.action.equals(PushTemplateConstants.IntentActions.MANUAL_CAROUSEL_RIGHT_CLICKED)
                ) {
                    builder = ManualCarouselTemplateNotificationBuilder()
                        .intent(intent)
                        .trackerActivityName(trackerActivityName)
                        .broadcastReceiverName(broadcastReceiverName)
                    builder.build(context)
                } else {
                    builder = FilmstripCarouselTemplateNotificationBuilder()
                        .intent(intent)
                        .trackerActivityName(trackerActivityName)
                        .broadcastReceiverName(broadcastReceiverName)
                    builder.build(context)
                }
            }
        }
        throw NotificationConstructionFailedException("Failed to build notification for the given intent with push template type ${pushTemplateType.value}.")
    }
}
