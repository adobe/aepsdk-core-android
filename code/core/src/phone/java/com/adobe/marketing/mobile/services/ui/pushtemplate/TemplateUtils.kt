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
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.adobe.marketing.mobile.services.Log

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
        context: Context,
        trackerActivity: Activity?,
        broadcastReceiver: BroadcastReceiver?,
        pushTemplate: AEPPushTemplate?,
        pushTemplateType: PushTemplateType
    ): NotificationCompat.Builder {
        if (pushTemplate == null) {
            throw NotificationConstructionFailedException("push template is null, cannot build a notification.")
        }

        val builder: Any
        when (pushTemplateType) {
            PushTemplateType.BASIC -> {
                Log.trace(
                    PushTemplateConstants.LOG_TAG,
                    SELF_TAG,
                    "Building a basic template push notification."
                )

                builder = BasicTemplateNotificationBuilder()
                    .pushTemplate(pushTemplate)
                    .trackerActivity(trackerActivity)
                    .broadcastReceiver(broadcastReceiver)
                return builder.build(context)
            }

            PushTemplateType.CAROUSEL -> {
                val carouselPushTemplate = pushTemplate as? CarouselPushTemplate
                val carouselOperationMode = carouselPushTemplate?.getCarouselOperationMode()
                val carouselType = carouselPushTemplate?.getCarouselLayoutType()

                Log.trace(
                    PushTemplateConstants.LOG_TAG,
                    SELF_TAG,
                    "Building a $carouselType carousel style push notification."
                )

                if (carouselOperationMode.equals(PushTemplateConstants.DefaultValues.AUTO_CAROUSEL_MODE)) {
                    builder = AutoCarouselTemplateNotificationBuilder()
                        .pushTemplate(pushTemplate)
                        .trackerActivity(trackerActivity)
                        .broadcastReceiver(broadcastReceiver)
                    return builder.build(context)
                } else {
                    return if (carouselType.equals(PushTemplateConstants.DefaultValues.FILMSTRIP_CAROUSEL_MODE)) {
                        builder = FilmstripCarouselTemplateNotificationBuilder()
                            .pushTemplate(pushTemplate)
                            .trackerActivity(trackerActivity)
                            .broadcastReceiver(broadcastReceiver)
                        builder.build(context)
                    } else {
                        builder = ManualCarouselTemplateNotificationBuilder()
                            .pushTemplate(pushTemplate)
                            .trackerActivity(trackerActivity)
                            .broadcastReceiver(broadcastReceiver)
                        builder.build(context)
                    }
                }
            }
            PushTemplateType.INPUT_BOX -> TODO()
            PushTemplateType.UNKNOWN -> TODO()
        }
    }

    @Throws(NotificationConstructionFailedException::class)
    @JvmStatic
    fun constructNotificationBuilder(
        context: Context,
        trackerActivity: Activity?,
        broadcastReceiver: BroadcastReceiver?,
        intent: Intent?,
        pushTemplateType: PushTemplateType
    ): NotificationCompat.Builder {
        if (intent == null) {
            throw NotificationConstructionFailedException("intent is null, cannot build a notification.")
        }

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
                    .trackerActivity(trackerActivity)
                    .broadcastReceiver(broadcastReceiver)
                return builder.build(context)
            }

            PushTemplateType.CAROUSEL -> {
                return if (intent.action.equals(PushTemplateConstants.IntentActions.MANUAL_CAROUSEL_LEFT_CLICKED) ||
                    intent.action.equals(PushTemplateConstants.IntentActions.MANUAL_CAROUSEL_RIGHT_CLICKED)
                ) {
                    builder = ManualCarouselTemplateNotificationBuilder()
                        .intent(intent)
                        .trackerActivity(trackerActivity)
                        .broadcastReceiver(broadcastReceiver)
                    builder.build(context)
                } else {
                    builder = FilmstripCarouselTemplateNotificationBuilder()
                        .intent(intent)
                        .trackerActivity(trackerActivity)
                        .broadcastReceiver(broadcastReceiver)
                    builder.build(context)
                }
            }

            PushTemplateType.INPUT_BOX -> TODO()
            PushTemplateType.UNKNOWN -> TODO()
        }
    }
}
