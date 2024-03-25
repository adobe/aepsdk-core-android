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
        trackerActivity: Activity,
        broadcastReceiver: BroadcastReceiver,
        pushTemplate: AEPPushTemplate?,
        pushTemplateType: PushTemplateType
    ): NotificationCompat.Builder {
        if (pushTemplate == null) {
            throw NotificationConstructionFailedException("push template is null, cannot build a notification.")
        }

        when (pushTemplateType) {
            PushTemplateType.BASIC -> {
                Log.trace(
                    PushTemplateConstants.LOG_TAG,
                    SELF_TAG,
                    "Building a basic style push notification."
                )

                return BasicTemplateNotificationBuilder.construct(
                    context,
                    trackerActivity,
                    broadcastReceiver,
                    pushTemplate as BasicPushTemplate
                )
            }

            PushTemplateType.CAROUSEL -> {
                val carouselPushTemplate = pushTemplate as? CarouselPushTemplate
                val carouselType = carouselPushTemplate?.getCarouselLayoutType()

                Log.trace(
                    PushTemplateConstants.LOG_TAG,
                    SELF_TAG,
                    "Building a $carouselType carousel style push notification."
                )
                if (carouselPushTemplate != null) {
                    return CarouselTemplateNotificationBuilder.construct(
                        context,
                        trackerActivity,
                        broadcastReceiver,
                        carouselPushTemplate
                    )
                }
            }

            PushTemplateType.INPUT_BOX -> TODO()
            PushTemplateType.UNKNOWN -> TODO()
        }
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Building a legacy style push notification."
        )
        return LegacyNotificationBuilder.construct(context, trackerActivity, pushTemplate)
    }

    @Throws(NotificationConstructionFailedException::class)
    @JvmStatic
    fun constructNotificationBuilder(
        context: Context,
        trackerActivity: Activity,
        broadcastReceiver: BroadcastReceiver,
        intent: Intent?,
        pushTemplateType: PushTemplateType
    ): NotificationCompat.Builder {
        if (intent == null) {
            throw NotificationConstructionFailedException("intent is null, cannot build a notification.")
        }

        when (pushTemplateType) {
            PushTemplateType.BASIC -> {
                Log.trace(
                    PushTemplateConstants.LOG_TAG,
                    SELF_TAG,
                    "Building a basic style push notification."
                )
                return BasicTemplateNotificationBuilder.construct(
                    context,
                    trackerActivity,
                    broadcastReceiver,
                    intent
                )
            }

            PushTemplateType.CAROUSEL -> {
                return if (intent.action.equals(PushTemplateConstants.IntentActions.MANUAL_CAROUSEL_LEFT_CLICKED) ||
                    intent.action.equals(PushTemplateConstants.IntentActions.MANUAL_CAROUSEL_RIGHT_CLICKED)
                ) {
                    ManualCarouselTemplateNotificationBuilder.construct(
                        context,
                        trackerActivity,
                        broadcastReceiver,
                        intent
                    )
                } else {
                    FilmstripCarouselTemplateNotificationBuilder.construct(
                        context,
                        trackerActivity,
                        broadcastReceiver,
                        intent
                    )
                }
            }

            PushTemplateType.INPUT_BOX -> TODO()
            PushTemplateType.UNKNOWN -> TODO()
        }
    }
}
