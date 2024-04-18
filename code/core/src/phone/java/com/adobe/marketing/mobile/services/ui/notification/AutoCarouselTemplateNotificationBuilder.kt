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
import android.content.Context
import android.graphics.Bitmap
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.adobe.marketing.mobile.core.R
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.caching.CacheService
import com.adobe.marketing.mobile.services.ui.notification.AEPPushTemplateNotificationBuilder.createChannelAndGetChannelID

/**
 * Object responsible for constructing a [NotificationCompat.Builder] object containing a auto carousel push template notification.
 */
internal object AutoCarouselTemplateNotificationBuilder {
    private const val SELF_TAG = "AutoCarouselTemplateNotificationBuilder"

    fun construct(
        context: Context,
        pushTemplate: AutoCarouselPushTemplate?,
        trackerActivityClass: Class<out Activity>?,
        broadcastReceiverClass: Class<out BroadcastReceiver>?,
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

        // Create the notification channel if needed
        val channelIdToUse = createChannelAndGetChannelID(
            context,
            pushTemplate.channelId,
            pushTemplate.sound,
            pushTemplate.getNotificationImportance()
        )

        // create the notification builder with the common settings applied
        val notificationBuilder = AEPPushTemplateNotificationBuilder.construct(
            context,
            pushTemplate,
            channelIdToUse,
            trackerActivityClass,
            smallLayout,
            expandedLayout
        )

        // load images into the carousel
        val downloadedImageUris = populateAutoCarouselImages(
            context,
            trackerActivityClass,
            cacheService,
            expandedLayout,
            pushTemplate,
            pushTemplate.carouselItems,
            packageName
        )

        // fallback to a basic push template notification builder if less than 3 images were able to be downloaded
        if ((
            downloadedImageUris.size
                < PushTemplateConstants.DefaultValues.CAROUSEL_MINIMUM_IMAGE_COUNT
            )
        ) {
            Log.trace(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                "Less than 3 images are available for the auto carousel push template, falling back to a basic push template."
            )
            return PushTemplateHelpers.fallbackToBasicNotification(
                context,
                trackerActivityClass,
                broadcastReceiverClass,
                pushTemplate,
                downloadedImageUris
            )
        }
        smallLayout.setTextViewText(R.id.notification_title, pushTemplate.title)
        smallLayout.setTextViewText(R.id.notification_body, pushTemplate.body)
        expandedLayout.setTextViewText(R.id.notification_title, pushTemplate.title)
        expandedLayout.setTextViewText(
            R.id.notification_body_expanded, pushTemplate.expandedBodyText
        )

        return notificationBuilder
    }

    /**
     * Populates the images for a automatic carousel push template.
     *
     * @param context the current [Context] of the application
     * @param trackerActivityClass the [Class] of the activity that will be used for tracking interactions with the carousel item
     * @param cacheService the [CacheService] used to cache the downloaded images
     * @param expandedLayout the [RemoteViews] containing the expanded layout of the notification
     * @param pushTemplate the [CarouselPushTemplate] object containing the push template data
     * @param items the list of [CarouselPushTemplate.CarouselItem] objects to be displayed in the carousel
     * @param packageName the `String` name of the application package used to locate the layout resources
     * @return a [List] of downloaded image URIs
     */
    private fun populateAutoCarouselImages(
        context: Context,
        trackerActivityClass: Class<out Activity>?,
        cacheService: CacheService,
        expandedLayout: RemoteViews,
        pushTemplate: CarouselPushTemplate,
        items: MutableList<CarouselPushTemplate.CarouselItem>,
        packageName: String?
    ): List<String?> {
        val imageProcessingStartTime = System.currentTimeMillis()
        val downloadedImageUris = mutableListOf<String>()
        for (item: CarouselPushTemplate.CarouselItem in items) {
            val imageUri: String = item.imageUri
            val pushImage: Bitmap? = PushTemplateHelpers.downloadImage(cacheService, imageUri)
            if (pushImage == null) {
                Log.trace(
                    PushTemplateConstants.LOG_TAG,
                    SELF_TAG,
                    "Failed to retrieve an image from $imageUri, will not create a new carousel item."
                )
                break
            }
            val carouselItem = RemoteViews(packageName, R.layout.push_template_carousel_item)
            downloadedImageUris.add(imageUri)
            carouselItem.setImageViewBitmap(R.id.carousel_item_image_view, pushImage)
            carouselItem.setTextViewText(R.id.carousel_item_caption, item.captionText)

            // assign a click action pending intent for each carousel item if we have a tracker activity
            trackerActivityClass?.let {
                val interactionUri = item.interactionUri ?: pushTemplate.actionUri
                AEPPushTemplateNotificationBuilder.setRemoteViewClickAction(
                    context,
                    trackerActivityClass,
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
