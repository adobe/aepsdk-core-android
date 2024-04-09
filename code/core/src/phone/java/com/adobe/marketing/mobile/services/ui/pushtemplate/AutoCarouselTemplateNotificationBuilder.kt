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
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.adobe.marketing.mobile.core.R
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.caching.CacheService

/**
 * Class responsible for constructing a [NotificationCompat.Builder] object containing a auto carousel push template notification.
 */
internal class AutoCarouselTemplateNotificationBuilder :
    TemplateNotificationBuilder() {
    companion object {
        private const val SELF_TAG = "AutoCarouselTemplateNotificationBuilder"
        private fun construct(
            context: Context,
            pushTemplate: CarouselPushTemplate?,
            trackerActivityName: String?,
            broadcastReceiverName: String?
        ): NotificationCompat.Builder {
            val packageName = context.packageName
            val smallLayout = RemoteViews(context.packageName, R.layout.push_template_collapsed)
            val expandedLayout =
                RemoteViews(context.packageName, R.layout.push_template_auto_carousel)
            val cacheService = ServiceProvider.getInstance().cacheService
                ?: throw NotificationConstructionFailedException(
                    (
                        "Cache service is null, auto carousel push notification will not be" +
                            " constructed."
                        )
                )
            pushTemplate
                ?: throw NotificationConstructionFailedException(
                    "push template is null, cannot build a notification."
                )

            val trackerActivity = PushTemplateTrackers.getInstance().getTrackerActivity(trackerActivityName)
            // load images into the carousel
            val items: ArrayList<CarouselPushTemplate.CarouselItem> =
                pushTemplate.getCarouselItems()
            val downloadedImageUris = populateImages(
                context,
                trackerActivity,
                cacheService,
                expandedLayout,
                pushTemplate,
                items,
                packageName
            )

            // fallback to a basic push template notification builder if less than 3 images were able
            // to be downloaded
            if ((
                downloadedImageUris.size
                    < PushTemplateConstants.DefaultValues.CAROUSEL_MINIMUM_IMAGE_COUNT
                )
            ) {
                return fallbackToBasicNotification(
                    context, trackerActivityName, broadcastReceiverName, pushTemplate, downloadedImageUris
                )
            }
            smallLayout.setTextViewText(R.id.notification_title, pushTemplate.getTitle())
            smallLayout.setTextViewText(R.id.notification_body, pushTemplate.getBody())
            expandedLayout.setTextViewText(R.id.notification_title, pushTemplate.getTitle())
            expandedLayout.setTextViewText(
                R.id.notification_body_expanded, pushTemplate.getExpandedBodyText()
            )

            // set any custom colors if needed
            setCustomNotificationColors(
                pushTemplate.getNotificationBackgroundColor(),
                pushTemplate.getTitleTextColor(),
                pushTemplate.getExpandedBodyTextColor(),
                smallLayout,
                expandedLayout,
                R.id.carousel_container_layout
            )

            // Create the notification
            val channelId = createChannelAndGetChannelID(
                context,
                pushTemplate.getChannelId(),
                pushTemplate.getSound(),
                pushTemplate.getNotificationImportance()
            )
            val builder = NotificationCompat.Builder(
                context,
                channelId
            )
                .setNumber(pushTemplate.getBadgeCount())
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(smallLayout)
                .setCustomBigContentView(expandedLayout)

            // small Icon must be present, otherwise the notification will not be displayed.
            setSmallIcon(
                context, builder, pushTemplate.getSmallIcon(), pushTemplate.getSmallIconColor()
            )
            setVisibility(
                builder, pushTemplate.getNotificationVisibility()
            )

            // set a large icon if one is present
            setRemoteViewLargeIcon(pushTemplate.getLargeIcon(), smallLayout)
            setRemoteViewLargeIcon(
                pushTemplate.getLargeIcon(), expandedLayout
            )

            // set custom sound, note this applies to API 25 and lower only as API 26 and up set the
            // sound on the notification channel
            setSound(context, builder, pushTemplate.getSound())

            // if API level is below 26 (prior to notification channels) then notification priority is
            // set on the notification builder
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                builder.setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVibrate(LongArray(0)) // hack to enable heads up notifications as a HUD style
                // notification requires a tone or vibration
            }
            return builder
        }

        private fun populateImages(
            context: Context,
            activity: Activity?,
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
                activity?.let {
                    val interactionUri = item.interactionUri ?: pushTemplate.getActionUri()
                    setRemoteViewClickAction(
                        context,
                        activity,
                        carouselItem,
                        R.id.carousel_item_image_view,
                        pushTemplate.getMessageId(),
                        pushTemplate.getDeliveryId(),
                        interactionUri,
                        pushTemplate.getTag(),
                        pushTemplate.getStickyStatus() ?: false
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

    override fun build(
        context: Context
    ): NotificationCompat.Builder {
        return construct(context, pushTemplate as? CarouselPushTemplate, trackerActivityName, broadcastReceiverName)
    }
}
