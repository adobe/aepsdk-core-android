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
import android.content.BroadcastReceiver
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.ui.notification.builders.AutoCarouselNotificationBuilder
import com.adobe.marketing.mobile.services.ui.notification.builders.BasicNotificationBuilder
import com.adobe.marketing.mobile.services.ui.notification.builders.FilmstripCarouselNotificationBuilder
import com.adobe.marketing.mobile.services.ui.notification.builders.LegacyNotificationBuilder
import com.adobe.marketing.mobile.services.ui.notification.builders.ManualCarouselNotificationBuilder
import com.adobe.marketing.mobile.services.ui.notification.models.AepPushTemplate
import com.adobe.marketing.mobile.services.ui.notification.models.AutoCarouselPushTemplate
import com.adobe.marketing.mobile.services.ui.notification.models.BasicPushTemplate
import com.adobe.marketing.mobile.services.ui.notification.models.CarouselPushTemplate
import com.adobe.marketing.mobile.services.ui.notification.models.ManualCarouselPushTemplate

/**
 * Public facing object to construct a [NotificationCompat.Builder] object for the specified [PushTemplateType].
 * The [constructNotificationBuilder] methods will build the appropriate notification based on the provided
 * [AepPushTemplate] or [Intent].
 */
object AepNotificationUtil {
    private const val SELF_TAG = "TemplateUtils"

    @JvmStatic
    val notificationCompatPriorityMap: Map<Int, String> = mapOf(
        NotificationCompat.PRIORITY_MIN to
            AepPushTemplate.NotificationPriority.PRIORITY_MIN,
        NotificationCompat.PRIORITY_LOW to
            AepPushTemplate.NotificationPriority.PRIORITY_LOW,
        NotificationCompat.PRIORITY_DEFAULT to
            AepPushTemplate.NotificationPriority.PRIORITY_DEFAULT,
        NotificationCompat.PRIORITY_HIGH to
            AepPushTemplate.NotificationPriority.PRIORITY_HIGH,
        NotificationCompat.PRIORITY_MAX to
            AepPushTemplate.NotificationPriority.PRIORITY_MAX
    )

    @JvmStatic
    val notificationCompatVisibilityMap: Map<Int, String> = mapOf(
        NotificationCompat.VISIBILITY_PRIVATE to
            AepPushTemplate.NotificationVisibility.PRIVATE,
        NotificationCompat.VISIBILITY_PUBLIC to
            AepPushTemplate.NotificationVisibility.PUBLIC,
        NotificationCompat.VISIBILITY_SECRET to
            AepPushTemplate.NotificationVisibility.SECRET
    )

    @Throws(NotificationConstructionFailedException::class)
    @JvmStatic
    fun constructNotificationBuilder(
        trackerActivityClass: Class<out Activity>?,
        broadcastReceiverClass: Class<out BroadcastReceiver>?,
        messageData: Map<String, String>
    ): NotificationCompat.Builder {
        val context = ServiceProvider.getInstance().appContextService.applicationContext
            ?: throw NotificationConstructionFailedException("Application context is null, cannot build a notification.")
        val pushTemplateType =
            PushTemplateType.fromString(messageData[PushTemplateConstants.PushPayloadKeys.TEMPLATE_TYPE])

        when (pushTemplateType) {
            PushTemplateType.BASIC -> {
                val basicPushTemplate = BasicPushTemplate(messageData as MutableMap<String, String>)
                return BasicNotificationBuilder.construct(
                    context,
                    basicPushTemplate,
                    trackerActivityClass,
                    broadcastReceiverClass
                )
            }

            PushTemplateType.CAROUSEL -> {
                val carouselPushTemplate =
                    CarouselPushTemplate(messageData as MutableMap<String, String>)
                val carouselOperationMode = carouselPushTemplate.carouselOperationMode
                val carouselType = carouselPushTemplate.carouselLayoutType

                Log.trace(
                    PushTemplateConstants.LOG_TAG,
                    SELF_TAG,
                    "Building a $carouselType carousel style push notification."
                )

                if (carouselOperationMode == PushTemplateConstants.DefaultValues.AUTO_CAROUSEL_MODE) {
                    return AutoCarouselNotificationBuilder.construct(
                        context,
                        AutoCarouselPushTemplate(messageData),
                        trackerActivityClass,
                        broadcastReceiverClass
                    )
                } else {
                    return if (carouselType == PushTemplateConstants.DefaultValues.FILMSTRIP_CAROUSEL_MODE) {
                        FilmstripCarouselNotificationBuilder.construct(
                            context,
                            ManualCarouselPushTemplate(messageData),
                            trackerActivityClass,
                            broadcastReceiverClass
                        )
                    } else {
                        return ManualCarouselNotificationBuilder.construct(
                            context,
                            ManualCarouselPushTemplate(messageData),
                            trackerActivityClass,
                            broadcastReceiverClass
                        )
                    }
                }
            }

            PushTemplateType.UNKNOWN -> {
                val basicPushTemplate = BasicPushTemplate(messageData as MutableMap<String, String>)
                return LegacyNotificationBuilder.construct(
                    context,
                    basicPushTemplate,
                    trackerActivityClass
                )
            }
        }
        throw NotificationConstructionFailedException("Failed to build notification for the given push template type ${pushTemplateType.value}.")
    }

    @Throws(NotificationConstructionFailedException::class)
    @JvmStatic
    fun constructNotificationBuilder(
        intent: Intent?,
        trackerActivityClass: Class<out Activity>?,
        broadcastReceiverClass: Class<out BroadcastReceiver>?
    ): NotificationCompat.Builder {
        if (intent == null) {
            throw NotificationConstructionFailedException("intent is null, cannot build a notification.")
        }

        val context = ServiceProvider.getInstance().appContextService.applicationContext
            ?: throw NotificationConstructionFailedException("Application context is null, cannot build a notification.")
        val pushTemplateType =
            PushTemplateType.fromString(intent.getStringExtra(PushTemplateConstants.IntentKeys.TEMPLATE_TYPE))

        when (pushTemplateType) {
            PushTemplateType.BASIC -> {
                Log.trace(
                    PushTemplateConstants.LOG_TAG,
                    SELF_TAG,
                    "Building a basic style push notification."
                )
                return BasicNotificationBuilder.construct(
                    context,
                    BasicPushTemplate(intent),
                    trackerActivityClass,
                    broadcastReceiverClass
                )
            }

            PushTemplateType.CAROUSEL -> {
                val pushTemplate = ManualCarouselPushTemplate(intent)
                return if (intent.action.equals(PushTemplateConstants.IntentActions.MANUAL_CAROUSEL_LEFT_CLICKED) ||
                    intent.action.equals(PushTemplateConstants.IntentActions.MANUAL_CAROUSEL_RIGHT_CLICKED)
                ) {
                    ManualCarouselNotificationBuilder.construct(
                        context,
                        pushTemplate,
                        trackerActivityClass,
                        broadcastReceiverClass
                    )
                } else {
                    FilmstripCarouselNotificationBuilder.construct(
                        context,
                        pushTemplate,
                        trackerActivityClass,
                        broadcastReceiverClass
                    )
                }
            }
        }
        throw NotificationConstructionFailedException("Failed to build notification for the given intent with push template type ${pushTemplateType.value}.")
    }
}
