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

package com.adobe.marketing.mobile.services.ui.notification.builders

import android.app.Activity
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.adobe.marketing.mobile.core.R
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.caching.CacheService
import com.adobe.marketing.mobile.services.ui.notification.CarouselTemplateUtil
import com.adobe.marketing.mobile.services.ui.notification.NotificationConstructionFailedException
import com.adobe.marketing.mobile.services.ui.notification.PushTemplateConstants
import com.adobe.marketing.mobile.services.ui.notification.PushTemplateImageUtil
import com.adobe.marketing.mobile.services.ui.notification.models.CarouselPushTemplate
import com.adobe.marketing.mobile.services.ui.notification.models.ManualCarouselPushTemplate

/**
 * Object responsible for constructing a [NotificationCompat.Builder] object containing a manual carousel push template notification.
 */
internal object ManualCarouselNotificationBuilder {
    private const val SELF_TAG = "ManualCarouselTemplateNotificationBuilder"

    @Throws(NotificationConstructionFailedException::class)
    fun construct(
        context: Context,
        pushTemplate: ManualCarouselPushTemplate?,
        trackerActivityClass: Class<out Activity>?,
        broadcastReceiverClass: Class<out BroadcastReceiver>?
    ): NotificationCompat.Builder {
        if (pushTemplate == null) {
            throw NotificationConstructionFailedException(
                "push template is null, cannot build a manual carousel notification."
            )
        }
        val cacheService = ServiceProvider.getInstance().cacheService
            ?: throw NotificationConstructionFailedException(
                (
                    "Cache service is null, manual carousel push notification will not be constructed."
                    )
            )
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Building a manual carousel template push notification."
        )

        val packageName = context.packageName
        val smallLayout = RemoteViews(packageName, R.layout.push_template_collapsed)
        val expandedLayout = RemoteViews(packageName, R.layout.push_template_manual_carousel)

        // create a silent notification channel if needed
        if (pushTemplate.isFromIntent == true && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            AepPushNotificationBuilder.setupSilentNotificationChannel(
                notificationManager,
                pushTemplate.getNotificationImportance()
            )
        }

        // create the notification channel if needed
        val channelIdToUse = AepPushNotificationBuilder.createChannel(
            context,
            pushTemplate.channelId,
            pushTemplate.sound,
            pushTemplate.getNotificationImportance()
        )

        // create the notification builder with the common settings applied
        val notificationBuilder = AepPushNotificationBuilder.construct(
            context,
            pushTemplate,
            channelIdToUse,
            trackerActivityClass,
            smallLayout,
            expandedLayout
        )

        // load images into the carousel
        val fallbackActionUri = pushTemplate.actionUri
        val items = pushTemplate.carouselItems
        val extractedItemData = populateManualCarouselImages(
            context,
            trackerActivityClass,
            cacheService,
            expandedLayout,
            items,
            packageName,
            pushTemplate.tag,
            fallbackActionUri,
            pushTemplate.isNotificationSticky
        )
        val downloadedImageUris =
            extractedItemData[PushTemplateConstants.CarouselListKeys.IMAGE_URIS_KEY] ?: emptyList()
        val imageCaptions =
            extractedItemData[PushTemplateConstants.CarouselListKeys.IMAGE_CAPTIONS_KEY]
                ?: emptyList()
        val imageClickActions =
            extractedItemData[PushTemplateConstants.CarouselListKeys.IMAGE_ACTIONS_KEY]
                ?: emptyList()

        // fallback to a basic push template notification builder if less than 3 images were able
        // to be downloaded
        if (downloadedImageUris.isNotEmpty() && downloadedImageUris.size < PushTemplateConstants.DefaultValues.CAROUSEL_MINIMUM_IMAGE_COUNT) {
            Log.trace(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                "Less than 3 images are available for the manual carousel push template, falling back to a basic push template."
            )
            return BasicNotificationBuilder.fallbackToBasicNotification(
                context,
                trackerActivityClass,
                broadcastReceiverClass,
                pushTemplate,
                downloadedImageUris
            )
        }

        // if we have an intent action then we need to calculate a new center index and update the view flipper
        val centerImageIndex = pushTemplate.centerImageIndex
        val newIndices: List<Int>
        if (pushTemplate.intentAction?.isNotEmpty() == true) {
            newIndices = CarouselTemplateUtil.calculateNewIndices(
                centerImageIndex,
                downloadedImageUris.size,
                pushTemplate.intentAction
            )
            pushTemplate.centerImageIndex = newIndices[1]

            // set the new center carousel item
            expandedLayout.setDisplayedChild(
                R.id.manual_carousel_view_flipper,
                pushTemplate.centerImageIndex
            )
        }

        val titleText = pushTemplate.title
        val smallBodyText = pushTemplate.body
        val expandedBodyText = pushTemplate.expandedBodyText
        smallLayout.setTextViewText(R.id.notification_title, titleText)
        smallLayout.setTextViewText(R.id.notification_body, smallBodyText)
        expandedLayout.setTextViewText(R.id.notification_title, titleText)
        expandedLayout.setTextViewText(R.id.notification_body_expanded, expandedBodyText)

        // handle left and right navigation buttons
        val clickIntent = AepPushNotificationBuilder.createClickIntent(
            context,
            pushTemplate,
            PushTemplateConstants.IntentActions.MANUAL_CAROUSEL_LEFT_CLICKED,
            broadcastReceiverClass,
            downloadedImageUris,
            imageCaptions,
            imageClickActions
        )
        val pendingIntentLeftButton = PendingIntent.getBroadcast(
            context,
            0,
            clickIntent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        clickIntent.setAction(PushTemplateConstants.IntentActions.MANUAL_CAROUSEL_RIGHT_CLICKED)
        val pendingIntentRightButton = PendingIntent.getBroadcast(
            context,
            0,
            clickIntent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        expandedLayout.setOnClickPendingIntent(R.id.leftImageButton, pendingIntentLeftButton)
        expandedLayout.setOnClickPendingIntent(R.id.rightImageButton, pendingIntentRightButton)

        return notificationBuilder
    }

    /**
     * Populates the images for a manual carousel push template.
     *
     * @param context the current [Context] of the application
     * @param trackerActivityClass the [Class] of the activity that will be used for tracking interactions with the carousel item
     * @param cacheService the [CacheService] used to cache the downloaded images
     * @param expandedLayout the [RemoteViews] containing the expanded layout of the notification
     * @param items the list of [CarouselPushTemplate.CarouselItem] objects to be displayed in the carousel
     * @param packageName the `String` name of the application package used to locate the layout resources
     * @param tag the `String` tag used to identify the notification
     * @param actionUri the `String` URI to be used when the carousel item is clicked
     * @param autoCancel the `Boolean` value to determine if the notification should be automatically canceled when clicked
     * @return a [Map] containing the image URIs, captions, and click actions for each carousel item
     */
    private fun populateManualCarouselImages(
        context: Context,
        trackerActivityClass: Class<out Activity>?,
        cacheService: CacheService,
        expandedLayout: RemoteViews,
        items: List<CarouselPushTemplate.CarouselItem>,
        packageName: String?,
        tag: String?,
        actionUri: String?,
        autoCancel: Boolean?
    ): Map<String, List<String?>> {
        val imageProcessingStartTime = System.currentTimeMillis()
        val downloadedImageUris = mutableListOf<String>()
        val imageCaptions = mutableListOf<String>()
        val imageClickActions = mutableListOf<String>()
        val itemData: MutableMap<String, List<String?>> = mutableMapOf()
        for (item: CarouselPushTemplate.CarouselItem in items) {
            val imageUri = item.imageUri
            val pushImage: Bitmap? = PushTemplateImageUtil.downloadImage(cacheService, imageUri)
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
            item.captionText?.let { imageCaptions.add(it) }
            carouselItem.setImageViewBitmap(R.id.carousel_item_image_view, pushImage)
            carouselItem.setTextViewText(R.id.carousel_item_caption, item.captionText)

            // assign a click action pending intent for each carousel item
            val interactionUri =
                if (item.interactionUri.isNullOrEmpty()) actionUri else item.interactionUri
            interactionUri?.let {
                imageClickActions.add(it)
                AepPushNotificationBuilder.setRemoteViewClickAction(
                    context,
                    trackerActivityClass,
                    carouselItem,
                    R.id.carousel_item_image_view,
                    interactionUri,
                    tag,
                    autoCancel ?: true
                )
            }

            // add the carousel item to the view flipper
            expandedLayout.addView(R.id.manual_carousel_view_flipper, carouselItem)
        }

        // log time needed to process the carousel images
        val imageProcessingElapsedTime = System.currentTimeMillis() - imageProcessingStartTime
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Processed %d manual carousel image(s) in %d milliseconds.",
            downloadedImageUris.size,
            imageProcessingElapsedTime
        )
        itemData[PushTemplateConstants.CarouselListKeys.IMAGE_URIS_KEY] = downloadedImageUris
        itemData[PushTemplateConstants.CarouselListKeys.IMAGE_CAPTIONS_KEY] = imageCaptions
        itemData[PushTemplateConstants.CarouselListKeys.IMAGE_ACTIONS_KEY] = imageClickActions
        return itemData
    }
}
