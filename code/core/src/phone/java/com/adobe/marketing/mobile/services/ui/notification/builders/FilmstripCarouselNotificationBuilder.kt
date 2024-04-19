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
 * Object responsible for constructing a [NotificationCompat.Builder] object containing a manual filmstrip carousel template notification.
 */
internal object FilmstripCarouselNotificationBuilder {
    private const val SELF_TAG = "FilmstripCarouselTemplateNotificationBuilder"

    @Throws(NotificationConstructionFailedException::class)
    fun construct(
        context: Context,
        pushTemplate: ManualCarouselPushTemplate?,
        trackerActivityClass: Class<out Activity>?,
        broadcastReceiverClass: Class<out BroadcastReceiver>?,
    ): NotificationCompat.Builder {
        if (pushTemplate == null) {
            throw NotificationConstructionFailedException(
                "push template is null, cannot build a manual filmstrip carousel template notification."
            )
        }
        val cacheService = ServiceProvider.getInstance().cacheService
            ?: throw NotificationConstructionFailedException(
                (
                    "Cache service is null, manual filmstrip carousel push notification will not be" +
                        " constructed."
                    )
            )
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Building a manual filmstrip carousel template push notification."
        )

        val packageName = context.packageName
        val smallLayout = RemoteViews(packageName, R.layout.push_template_collapsed)
        val expandedLayout = RemoteViews(packageName, R.layout.push_template_filmstrip_carousel)

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

        // download the carousel images and populate the image uri, image caption, and image click
        // action arrays
        val extractedItemData = downloadFilmstripImages(cacheService, pushTemplate.carouselItems)
        val downloadedImages = extractedItemData[PushTemplateConstants.CarouselListKeys.IMAGES_KEY]
        val downloadedImageUris =
            extractedItemData[PushTemplateConstants.CarouselListKeys.IMAGE_URIS_KEY]
        val imageCaptions =
            extractedItemData[PushTemplateConstants.CarouselListKeys.IMAGE_CAPTIONS_KEY]
        val imageClickActions =
            extractedItemData[PushTemplateConstants.CarouselListKeys.IMAGE_ACTIONS_KEY]

        // fallback to a basic push template notification builder if less than 3 images were able
        // to be downloaded
        if ((downloadedImageUris.isNullOrEmpty() || downloadedImageUris.size < PushTemplateConstants.DefaultValues.CAROUSEL_MINIMUM_IMAGE_COUNT)) {
            Log.trace(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                "Less than 3 images are available for the filmstrip carousel push template, falling back to a basic push template."
            )
            return BasicNotificationBuilder.fallbackToBasicNotification(
                context,
                trackerActivityClass,
                broadcastReceiverClass,
                pushTemplate,
                downloadedImageUris as List<String?>
            )
        }

        // if we have an intent action then we need to calculate a new center index
        val centerImageIndex = pushTemplate.centerImageIndex
        val newIndices: List<Int>
        if (pushTemplate.intentAction?.isNotEmpty() == true) {
            newIndices = CarouselTemplateUtil.calculateNewIndices(
                centerImageIndex,
                downloadedImageUris.size,
                pushTemplate.intentAction
            )
            pushTemplate.centerImageIndex = newIndices[1]
        } else {
            newIndices = listOf(centerImageIndex - 1, centerImageIndex, centerImageIndex + 1)
        }

        // set the carousel images in the filmstrip carousel
        populateFilmstripCarouselImages(
            context,
            downloadedImages as List<Bitmap?>,
            imageCaptions as List<String?>,
            imageClickActions as List<String?>,
            newIndices,
            pushTemplate,
            trackerActivityClass,
            expandedLayout
        )

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
            PushTemplateConstants.IntentActions.FILMSTRIP_LEFT_CLICKED,
            broadcastReceiverClass,
            downloadedImageUris as List<String?>,
            imageCaptions,
            imageClickActions
        )
        val pendingIntentLeftButton = PendingIntent.getBroadcast(
            context,
            0,
            clickIntent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        clickIntent.setAction(PushTemplateConstants.IntentActions.FILMSTRIP_RIGHT_CLICKED)
        val pendingIntentRightButton = PendingIntent.getBroadcast(
            context,
            0,
            clickIntent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // set onclick intents for the skip left and skip right buttons
        expandedLayout.setOnClickPendingIntent(R.id.leftImageButton, pendingIntentLeftButton)
        expandedLayout.setOnClickPendingIntent(R.id.rightImageButton, pendingIntentRightButton)

        return notificationBuilder
    }

    /**
     * Downloads the images for a filmstrip carousel push template.
     *
     * @param cacheService the [CacheService] used to cache the downloaded images
     * @param items the list of [CarouselPushTemplate.CarouselItem] objects to be displayed in the filmstrip carousel
     * @return a [Map] containing the downloaded images, image URIs, image captions, and image click actions for each carousel item
     */
    private fun downloadFilmstripImages(
        cacheService: CacheService,
        items: List<CarouselPushTemplate.CarouselItem>
    ): Map<String, List<Any?>> {
        val downloadedImages = mutableListOf<Bitmap?>()
        val downloadedImageUris = mutableListOf<String?>()
        val imageCaptions = mutableListOf<String?>()
        val imageClickActions = mutableListOf<String?>()
        val itemData: MutableMap<String, List<Any?>> = mutableMapOf()

        for (item: CarouselPushTemplate.CarouselItem in items) {
            val imageUri: String = item.imageUri
            val pushImage: Bitmap? = PushTemplateImageUtil.downloadImage(cacheService, imageUri)
            if (pushImage == null) {
                Log.trace(
                    PushTemplateConstants.LOG_TAG,
                    SELF_TAG,
                    "Failed to retrieve an image from $imageUri, will not create a new carousel item."
                )
                break
            }
            downloadedImages.add(pushImage)
            downloadedImageUris.add(imageUri)
            imageCaptions.add(item.captionText)
            imageClickActions.add(item.interactionUri)
        }

        itemData[PushTemplateConstants.CarouselListKeys.IMAGE_URIS_KEY] = downloadedImageUris
        itemData[PushTemplateConstants.CarouselListKeys.IMAGE_CAPTIONS_KEY] = imageCaptions
        itemData[PushTemplateConstants.CarouselListKeys.IMAGE_ACTIONS_KEY] = imageClickActions
        itemData[PushTemplateConstants.CarouselListKeys.IMAGES_KEY] = downloadedImages
        return itemData.toMap()
    }

    /**
     * Populates the images for a manual filmstrip carousel push template.
     *
     * @param context the current [Context] of the application
     * @param downloadedImages the list of [Bitmap] objects downloaded for the filmstrip carousel
     * @param imageCaptions the list of [String] captions for each filmstrip carousel image
     * @param imageClickActions the list of [String] click actions for each filmstrip carousel image
     * @param newIndices the list of [Int] indices for the new left, center, and right images
     * @param pushTemplate the [ManualCarouselPushTemplate] object containing the push template data
     * @param trackerActivityClass the [Class] of the activity that will be used for tracking interactions with the carousel item
     * @param expandedLayout the [RemoteViews] containing the expanded layout of the notification
     */
    private fun populateFilmstripCarouselImages(
        context: Context,
        downloadedImages: List<Bitmap?>,
        imageCaptions: List<String?>,
        imageClickActions: List<String?>,
        newIndices: List<Int>,
        pushTemplate: ManualCarouselPushTemplate,
        trackerActivityClass: Class<out Activity>?,
        expandedLayout: RemoteViews
    ) {
        val newLeftIndex = newIndices[0]
        val newCenterIndex = newIndices[1]
        val newRightIndex = newIndices[2]

        // get all captions present then set center caption text
        val centerCaptionText = imageCaptions[newCenterIndex]
        expandedLayout.setTextViewText(
            R.id.manual_carousel_filmstrip_caption,
            centerCaptionText
        )

        // set the downloaded bitmaps in the filmstrip image views
        expandedLayout.setImageViewBitmap(
            R.id.manual_carousel_filmstrip_left, downloadedImages[newLeftIndex]
        )
        expandedLayout.setImageViewBitmap(
            R.id.manual_carousel_filmstrip_center, downloadedImages[newCenterIndex]
        )
        expandedLayout.setImageViewBitmap(
            R.id.manual_carousel_filmstrip_right, downloadedImages[newRightIndex]
        )

        // assign a click action pending intent to the center image view
        val interactionUri =
            if (!imageClickActions[newCenterIndex].isNullOrEmpty()) imageClickActions[newCenterIndex] else pushTemplate.actionUri
        AepPushNotificationBuilder.setRemoteViewClickAction(
            context,
            trackerActivityClass,
            expandedLayout,
            R.id.manual_carousel_filmstrip_center,
            interactionUri,
            pushTemplate.tag,
            pushTemplate.isNotificationSticky ?: false
        )
    }
}
