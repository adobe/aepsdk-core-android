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

import android.content.Context
import android.graphics.Bitmap
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.adobe.marketing.mobile.core.R
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.caching.CacheService

/**
 * Object responsible for constructing a [NotificationCompat.Builder] object containing a auto carousel push template notification.
 */
internal object AutoCarouselTemplateNotificationBuilder : AEPPushTemplateNotificationBuilder() {
    private const val SELF_TAG = "AutoCarouselTemplateNotificationBuilder"

    fun construct(
        context: Context,
        pushTemplate: AutoCarouselPushTemplate?,
        trackerActivityName: String?,
        broadcastReceiverName: String?
    ): NotificationCompat.Builder {
        if (pushTemplate == null) {
            throw NotificationConstructionFailedException(
                "push template is null, cannot build an auto carousel template notification."
            )
        }
        val cacheService = ServiceProvider.getInstance().cacheService
            ?: throw NotificationConstructionFailedException(
                (
                    "Cache service is null, auto carousel push notification will not be" +
                        " constructed."
                    )
            )
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Building an auto carousel template push notification."
        )

        val packageName = context.packageName
        val smallLayout = RemoteViews(packageName, R.layout.push_template_collapsed)
        val expandedLayout = RemoteViews(packageName, R.layout.push_template_auto_carousel)

        // load images into the carousel
        val items: ArrayList<CarouselPushTemplate.CarouselItem> =
            pushTemplate.carouselItems
        val downloadedImageUris = populateImages(
            context,
            trackerActivityName,
            cacheService,
            expandedLayout,
            pushTemplate,
            items,
            packageName
        )

        // fallback to a basic push template notification builder if less than 3 images were able to be downloaded
        if ((
            downloadedImageUris.size
                < PushTemplateConstants.DefaultValues.CAROUSEL_MINIMUM_IMAGE_COUNT
            )
        ) {
            return fallbackToBasicNotification(
                context, trackerActivityName, broadcastReceiverName, pushTemplate, downloadedImageUris
            )
        }
        smallLayout.setTextViewText(R.id.notification_title, pushTemplate.title)
        smallLayout.setTextViewText(R.id.notification_body, pushTemplate.body)
        expandedLayout.setTextViewText(R.id.notification_title, pushTemplate.title)
        expandedLayout.setTextViewText(
            R.id.notification_body_expanded, pushTemplate.expandedBodyText
        )

        // Create the notification channel if needed
        channelIdToUse = createChannelAndGetChannelID(
            context,
            pushTemplate.channelId,
            pushTemplate.sound,
            pushTemplate.getNotificationImportance()
        )

        // create the notification builder with the common settings applied
        return super.construct(context, pushTemplate, trackerActivityName, smallLayout, expandedLayout)
    }

    private fun populateImages(
        context: Context,
        activityName: String?,
        cacheService: CacheService,
        expandedLayout: RemoteViews,
        pushTemplate: CarouselPushTemplate,
        items: ArrayList<CarouselPushTemplate.CarouselItem>,
        packageName: String?
    ): List<String> {
        val imageProcessingStartTime = System.currentTimeMillis()
        val downloadedImageUris = ArrayList<String>()
        for (item: CarouselPushTemplate.CarouselItem in items) {
            val imageUri: String = item.imageUri
            val pushImage: Bitmap? = downloadImage(cacheService, imageUri)
            if (pushImage == null) {
                Log.trace(
                    PushTemplateConstants.LOG_TAG,
                    SELF_TAG,
                    "Failed to retrieve an image from %s, will not create a new carousel item.",
                    imageUri
                )
                break
            }
            val carouselItem = RemoteViews(packageName, R.layout.push_template_carousel_item)
            downloadedImageUris.add(imageUri)
            carouselItem.setImageViewBitmap(R.id.carousel_item_image_view, pushImage)
            carouselItem.setTextViewText(R.id.carousel_item_caption, item.captionText)

            // assign a click action pending intent for each carousel item if we have a tracker activity
            activityName?.let {
                val interactionUri = item.interactionUri ?: pushTemplate.actionUri
                setRemoteViewClickAction(
                    context,
                    activityName,
                    carouselItem,
                    R.id.carousel_item_image_view,
                    interactionUri,
                    pushTemplate.tag,
                    pushTemplate.isNotificationSticky ?: false
                )
            }

            // add the carousel item to the view flipper
            expandedLayout.addView(R.id.auto_carousel_view_flipper, carouselItem)
        }

        // log time needed to process the carousel images
        val imageProcessingElapsedTime = System.currentTimeMillis() - imageProcessingStartTime
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Processed %d auto carousel image(s) in %d milliseconds.",
            downloadedImageUris.size,
            imageProcessingElapsedTime
        )
        return downloadedImageUris
    }
}
