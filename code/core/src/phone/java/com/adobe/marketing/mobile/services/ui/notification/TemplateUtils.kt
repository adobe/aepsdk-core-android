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
        val pushTemplateType =
            PushTemplateType.fromString(messageData[PushTemplateConstants.PushPayloadKeys.TEMPLATE_TYPE])

        when (pushTemplateType) {
            PushTemplateType.BASIC -> {
                val basicPushTemplate = BasicPushTemplate(messageData as MutableMap<String, String>)
                return BasicTemplateNotificationBuilder.construct(context, null, basicPushTemplate, trackerActivityName, broadcastReceiverName)
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
                    return AutoCarouselTemplateNotificationBuilder.construct(context, carouselPushTemplate, trackerActivityName, broadcastReceiverName)
                } else {
                    return if (carouselType.equals(PushTemplateConstants.DefaultValues.FILMSTRIP_CAROUSEL_MODE)) {
                        FilmstripCarouselTemplateNotificationBuilder.construct(context, null, carouselPushTemplate, trackerActivityName, broadcastReceiverName)
                    } else {
                       return ManualCarouselTemplateNotificationBuilder.construct(context, null, carouselPushTemplate, trackerActivityName, broadcastReceiverName)
                    }
                }
            }

            PushTemplateType.UNKNOWN -> {
                val basicPushTemplate = BasicPushTemplate(messageData as MutableMap<String, String>)
                return LegacyNotificationBuilder.construct(context, basicPushTemplate, trackerActivityName)
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

        when (pushTemplateType) {
            PushTemplateType.BASIC -> {
                Log.trace(
                    PushTemplateConstants.LOG_TAG,
                    SELF_TAG,
                    "Building a basic style push notification."
                )
                return BasicTemplateNotificationBuilder.construct(context, intent, null, trackerActivityName, broadcastReceiverName)
            }

            PushTemplateType.CAROUSEL -> {
                return if (intent.action.equals(PushTemplateConstants.IntentActions.MANUAL_CAROUSEL_LEFT_CLICKED) ||
                    intent.action.equals(PushTemplateConstants.IntentActions.MANUAL_CAROUSEL_RIGHT_CLICKED)
                ) {
                    ManualCarouselTemplateNotificationBuilder.construct(context, intent, null, trackerActivityName, broadcastReceiverName)
                } else {
                    FilmstripCarouselTemplateNotificationBuilder.construct(context, intent, null, trackerActivityName, broadcastReceiverName)
                }
            }
        }
        throw NotificationConstructionFailedException("Failed to build notification for the given intent with push template type ${pushTemplateType.value}.")
    }
}
