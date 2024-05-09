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
import com.adobe.marketing.mobile.services.ui.notification.builders.InputBoxNotificationBuilder
import com.adobe.marketing.mobile.services.ui.notification.builders.LegacyNotificationBuilder
import com.adobe.marketing.mobile.services.ui.notification.builders.ManualCarouselNotificationBuilder
import com.adobe.marketing.mobile.services.ui.notification.templates.AEPPushTemplate
import com.adobe.marketing.mobile.services.ui.notification.templates.AutoCarouselPushTemplate
import com.adobe.marketing.mobile.services.ui.notification.templates.BasicPushTemplate
import com.adobe.marketing.mobile.services.ui.notification.templates.CarouselPushTemplate
import com.adobe.marketing.mobile.services.ui.notification.templates.InputBoxPushTemplate
import com.adobe.marketing.mobile.services.ui.notification.templates.ManualCarouselPushTemplate

/**
 * Public facing object to construct a [NotificationCompat.Builder] object for the specified [PushTemplateType].
 * The [constructNotificationBuilder] methods will build the appropriate notification based on the provided
 * [AEPPushTemplate] or [Intent].
 */
object NotificationBuilder {
    private const val SELF_TAG = "NotificationBuilder"

    @Throws(NotificationConstructionFailedException::class)
    @JvmStatic
    fun constructNotificationBuilder(
        messageData: Map<String, String>,
        trackerActivityClass: Class<out Activity>?,
        broadcastReceiverClass: Class<out BroadcastReceiver>?
    ): NotificationCompat.Builder {
        val context = ServiceProvider.getInstance().appContextService.applicationContext
            ?: throw NotificationConstructionFailedException("Application context is null, cannot build a notification.")
        val pushTemplateType =
            PushTemplateType.fromString(messageData[PushTemplateConstants.PushPayloadKeys.TEMPLATE_TYPE])

        when (pushTemplateType) {
            PushTemplateType.BASIC -> {
                val basicPushTemplate = BasicPushTemplate(messageData)
                return BasicNotificationBuilder.construct(
                    context,
                    basicPushTemplate,
                    trackerActivityClass,
                    broadcastReceiverClass
                )
            }

            PushTemplateType.CAROUSEL -> {
                val carouselPushTemplate =
                    CarouselPushTemplate.createCarouselPushTemplate(messageData)

                when (carouselPushTemplate) {
                    is AutoCarouselPushTemplate -> {
                        return AutoCarouselNotificationBuilder.construct(
                            context,
                            carouselPushTemplate,
                            trackerActivityClass,
                            broadcastReceiverClass
                        )
                    }

                    is ManualCarouselPushTemplate -> {
                        return ManualCarouselNotificationBuilder.construct(
                            context,
                            carouselPushTemplate,
                            trackerActivityClass,
                            broadcastReceiverClass
                        )
                    }

                    else -> {
                        Log.trace(
                            PushTemplateConstants.LOG_TAG,
                            SELF_TAG,
                            "Unknown carousel push template type, creating a legacy style notification."
                        )
                        return LegacyNotificationBuilder.construct(
                            context,
                            BasicPushTemplate(messageData),
                            trackerActivityClass
                        )
                    }
                }
            }

            PushTemplateType.INPUT_BOX -> {
                return InputBoxNotificationBuilder.construct(
                    context,
                    InputBoxPushTemplate(messageData),
                    trackerActivityClass,
                    broadcastReceiverClass
                )
            }

            PushTemplateType.UNKNOWN -> {
                return LegacyNotificationBuilder.construct(
                    context,
                    BasicPushTemplate(messageData),
                    trackerActivityClass
                )
            }
        }
    }

    @Throws(NotificationConstructionFailedException::class)
    @JvmStatic
    fun constructNotificationBuilder(
        intent: Intent,
        trackerActivityClass: Class<out Activity>?,
        broadcastReceiverClass: Class<out BroadcastReceiver>?
    ): NotificationCompat.Builder {
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
                return ManualCarouselNotificationBuilder.construct(
                    context,
                    ManualCarouselPushTemplate(intent),
                    trackerActivityClass,
                    broadcastReceiverClass
                )
            }

            PushTemplateType.INPUT_BOX -> {
                return InputBoxNotificationBuilder.construct(
                    context,
                    InputBoxPushTemplate(intent),
                    trackerActivityClass,
                    broadcastReceiverClass
                )
            }

            PushTemplateType.UNKNOWN -> {
                return LegacyNotificationBuilder.construct(
                    context,
                    BasicPushTemplate(intent),
                    trackerActivityClass
                )
            }
        }
    }
}
