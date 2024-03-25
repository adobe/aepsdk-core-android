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
import androidx.core.app.NotificationCompat
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceProvider
import com.google.android.gms.common.util.CollectionUtils

/**
 * Top level object to construct a carousel push notification. The object is responsible for
 * determining the operation mode of the carousel push template and building the appropriate
 * carousel-style notification.
 */
internal object CarouselTemplateNotificationBuilder {
    private const val SELF_TAG = "CarouselTemplateNotificationBuilder"

    @Throws(NotificationConstructionFailedException::class)
    internal fun construct(
        context: Context,
        trackerActivity: Activity,
        broadcastReceiver: BroadcastReceiver,
        pushTemplate: CarouselPushTemplate
    ): NotificationCompat.Builder {
        val channelId: String = createChannelAndGetChannelID(
            context,
            pushTemplate.getChannelId(),
            pushTemplate.getSound(),
            pushTemplate.getNotificationImportance()
        )
        val packageName = ServiceProvider.getInstance()
            .appContextService
            .application
            ?.packageName

        packageName?.let {
            val carouselOperationMode = pushTemplate.getCarouselOperationMode()
            if (carouselOperationMode ==
                PushTemplateConstants.DefaultValues.MANUAL_CAROUSEL_MODE
            ) {
                return buildManualCarouselNotification(
                    pushTemplate,
                    context,
                    trackerActivity,
                    broadcastReceiver,
                    channelId,
                    packageName
                )
            }

            // default operation mode is auto
            Log.trace(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                "Building an auto carousel push notification."
            )
            return AutoCarouselTemplateNotificationBuilder.construct(
                context, trackerActivity, broadcastReceiver, pushTemplate, channelId, packageName
            )
        }
        throw NotificationConstructionFailedException("Failed to build carousel notification, package name is null.")
    }

    @Throws(NotificationConstructionFailedException::class)
    private fun buildManualCarouselNotification(
        pushTemplate: CarouselPushTemplate,
        context: Context,
        trackerActivity: Activity,
        broadcastReceiver: BroadcastReceiver,
        channelId: String,
        packageName: String
    ): NotificationCompat.Builder {
        val carouselLayoutType: String = pushTemplate.getCarouselLayoutType()
        if (carouselLayoutType ==
            PushTemplateConstants.DefaultValues.FILMSTRIP_CAROUSEL_MODE
        ) {
            Log.trace(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                "Building a manual filmstrip carousel push notification."
            )
            return FilmstripCarouselTemplateNotificationBuilder.construct(
                context, trackerActivity, broadcastReceiver, pushTemplate, packageName, channelId
            )
        }
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Building a default manual carousel push notification."
        )
        return ManualCarouselTemplateNotificationBuilder.construct(
            context, trackerActivity, broadcastReceiver, pushTemplate, packageName, channelId
        )
    }

    @Throws(NotificationConstructionFailedException::class)
    internal fun fallbackToBasicNotification(
        context: Context,
        trackerActivity: Activity,
        broadcastReceiver: BroadcastReceiver,
        pushTemplate: CarouselPushTemplate,
        downloadedImageUris: List<String?>
    ): NotificationCompat.Builder {
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Only %d image(s) for the carousel notification were downloaded while at least %d" +
                " were expected. Building a basic push notification instead.",
            downloadedImageUris.size,
            PushTemplateConstants.DefaultValues.CAROUSEL_MINIMUM_IMAGE_COUNT
        )
        if (!CollectionUtils.isEmpty(downloadedImageUris)) {
            // use the first downloaded image (if available) for the basic template notification
            pushTemplate.modifyData(
                PushTemplateConstants.PushPayloadKeys.IMAGE_URL, downloadedImageUris[0].toString()
            )
        }
        val basicPushTemplate = BasicPushTemplate(pushTemplate.data)
        return BasicTemplateNotificationBuilder.construct(
            context,
            trackerActivity,
            broadcastReceiver,
            basicPushTemplate
        )
    }
}
