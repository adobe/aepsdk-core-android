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
import android.graphics.BitmapFactory
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.adobe.marketing.mobile.core.R
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.caching.CacheService
import com.adobe.marketing.mobile.services.ui.notification.NotificationConstructionFailedException
import com.adobe.marketing.mobile.services.ui.notification.PushTemplateConstants
import com.adobe.marketing.mobile.services.ui.notification.PushTemplateImageUtil
import com.adobe.marketing.mobile.services.ui.notification.models.CarouselPushTemplate
import com.adobe.marketing.mobile.services.ui.notification.models.ManualCarouselPushTemplate

/**
 * Object responsible for constructing a [NotificationCompat.Builder] object containing a manual or filmstrip carousel push template notification.
 */
internal object ManualCarouselNotificationBuilder {
    private const val SELF_TAG = "ManualCarouselTemplateNotificationBuilder"

    @Throws(NotificationConstructionFailedException::class)
    fun construct(
        context: Context,
        pushTemplate: ManualCarouselPushTemplate,
        trackerActivityClass: Class<out Activity>?,
        broadcastReceiverClass: Class<out BroadcastReceiver>?
    ): NotificationCompat.Builder {
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

        // create a silent notification channel if needed
        if (pushTemplate.isFromIntent == true && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            AEPPushNotificationBuilder.setupSilentNotificationChannel(
                notificationManager,
                pushTemplate.getNotificationImportance()
            )
        }

        // create the notification channel if needed
        val channelIdToUse = AEPPushNotificationBuilder.createChannel(
            context,
            pushTemplate.channelId,
            pushTemplate.sound,
            pushTemplate.getNotificationImportance()
        )

        // set the expanded layout depending on the carousel type
        val packageName = context.packageName
        val smallLayout = RemoteViews(packageName, R.layout.push_template_collapsed)
        val expandedLayout =
            if (pushTemplate.carouselLayoutType == PushTemplateConstants.DefaultValues.FILMSTRIP_CAROUSEL_MODE)
                RemoteViews(
                    packageName,
                    R.layout.push_template_filmstrip_carousel
                ) else RemoteViews(packageName, R.layout.push_template_manual_carousel)

        // create the notification builder with the common settings applied
        val notificationBuilder = AEPPushNotificationBuilder.construct(
            context,
            pushTemplate,
            channelIdToUse,
            trackerActivityClass,
            smallLayout,
            expandedLayout,
            R.id.carousel_container_layout
        )

        // download carousel images
        val validCarouselItems = downloadCarouselItems(cacheService, pushTemplate.carouselItems)

        // fallback to a basic push template notification builder if less than 3 images were able
        // to be downloaded
        if (validCarouselItems.size < PushTemplateConstants.DefaultValues.CAROUSEL_MINIMUM_IMAGE_COUNT) {
            Log.trace(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                "Less than 3 images are available for the manual carousel push template, falling back to a basic push template."
            )
            val imageUris = validCarouselItems.map { it.imageUri }
            val modifiedDataMap = pushTemplate.messageData
            modifiedDataMap[PushTemplateConstants.PushPayloadKeys.IMAGE_URL] = imageUris[0]
            return BasicNotificationBuilder.fallbackToBasicNotification(
                context,
                trackerActivityClass,
                broadcastReceiverClass,
                modifiedDataMap
            )
        }

        // extract image uris, captions, and interaction uris from the validated carousel items
        val imageUris = validCarouselItems.map { it.imageUri }
        val captions = validCarouselItems.map { it.captionText }
        val interactionUris = validCarouselItems.map { it.interactionUri }
        val fallbackActionUri = pushTemplate.actionUri

        // get the indices for the carousel
        val carouselIndices = getCarouselIndices(pushTemplate, imageUris)

        // populate the images for the manual carousel
        setupCarouselImages(
            context,
            cacheService,
            captions,
            interactionUris,
            carouselIndices,
            pushTemplate,
            trackerActivityClass,
            expandedLayout,
            validCarouselItems,
            packageName,
            fallbackActionUri
        )

        // set title text and body text
        val titleText = pushTemplate.title
        val smallBodyText = pushTemplate.body
        val expandedBodyText = pushTemplate.expandedBodyText
        smallLayout.setTextViewText(R.id.notification_title, titleText)
        smallLayout.setTextViewText(R.id.notification_body, smallBodyText)
        expandedLayout.setTextViewText(R.id.notification_title, titleText)
        expandedLayout.setTextViewText(R.id.notification_body_expanded, expandedBodyText)

        // handle left and right navigation buttons
        setupNavigationButtons(
            context,
            pushTemplate,
            broadcastReceiverClass,
            imageUris,
            captions,
            interactionUris,
            expandedLayout
        )

        return notificationBuilder
    }

    /**
     * Downloads the images for a carousel push template.
     *
     * @param cacheService the [CacheService] used to cache the downloaded images
     * @param items the list of [CarouselPushTemplate.CarouselItem] objects to be displayed in the filmstrip carousel
     * @return a list of `CarouselPushTemplate.CarouselItem` objects that were successfully downloaded
     */
    private fun downloadCarouselItems(
        cacheService: CacheService,
        items: List<CarouselPushTemplate.CarouselItem>
    ): List<CarouselPushTemplate.CarouselItem> {
        val validCarouselItems = mutableListOf<CarouselPushTemplate.CarouselItem>()
        for (item: CarouselPushTemplate.CarouselItem in items) {
            val imageUri: String = item.imageUri
            val pushImage: Bitmap? = PushTemplateImageUtil.downloadImage(cacheService, imageUri)
            if (pushImage == null) {
                Log.trace(
                    PushTemplateConstants.LOG_TAG,
                    SELF_TAG,
                    "Failed to retrieve an image from $imageUri, will not create a new carousel item."
                )
                continue
            }
            validCarouselItems.add(item)
        }
        return validCarouselItems
    }

    private fun getCarouselIndices(
        pushTemplate: ManualCarouselPushTemplate,
        imageUris: List<String?>
    ): Triple<Int, Int, Int> {
        val carouselIndices: Triple<Int, Int, Int>
        if (pushTemplate.intentAction?.isNotEmpty() == true) {
            carouselIndices =
                if (pushTemplate.intentAction == PushTemplateConstants.IntentActions.MANUAL_CAROUSEL_LEFT_CLICKED || pushTemplate.intentAction == PushTemplateConstants.IntentActions.FILMSTRIP_LEFT_CLICKED) {
                    getNewIndicesForNavigateLeft(pushTemplate.centerImageIndex, imageUris.size)
                } else {
                    getNewIndicesForNavigateRight(pushTemplate.centerImageIndex, imageUris.size)
                }
            pushTemplate.centerImageIndex =
                carouselIndices.second
        } else { // setup default indices if not building the notification from the intente
            carouselIndices =
                if (pushTemplate.carouselLayoutType == PushTemplateConstants.DefaultValues.FILMSTRIP_CAROUSEL_MODE) {
                    Triple(
                        PushTemplateConstants.DefaultValues.FILMSTRIP_CAROUSEL_CENTER_INDEX - 1,
                        PushTemplateConstants.DefaultValues.FILMSTRIP_CAROUSEL_CENTER_INDEX,
                        PushTemplateConstants.DefaultValues.FILMSTRIP_CAROUSEL_CENTER_INDEX + 1
                    )
                } else {
                    Triple(
                        PushTemplateConstants.DefaultValues.MANUAL_CAROUSEL_START_INDEX,
                        PushTemplateConstants.DefaultValues.MANUAL_CAROUSEL_START_INDEX + 1,
                        PushTemplateConstants.DefaultValues.MANUAL_CAROUSEL_START_INDEX + 2
                    )
                }
        }

        return carouselIndices
    }

    private fun setupCarouselImages(
        context: Context,
        cacheService: CacheService,
        captions: List<String?>,
        interactionUris: List<String?>,
        newIndices: Triple<Int, Int, Int>,
        pushTemplate: ManualCarouselPushTemplate,
        trackerActivityClass: Class<out Activity>?,
        expandedLayout: RemoteViews,
        validCarouselItems: List<CarouselPushTemplate.CarouselItem>,
        packageName: String?,
        fallbackActionUri: String?
    ) {
        if (pushTemplate.carouselLayoutType == PushTemplateConstants.DefaultValues.FILMSTRIP_CAROUSEL_MODE) {
            populateFilmstripCarouselImages(
                context,
                cacheService,
                captions,
                interactionUris,
                newIndices,
                pushTemplate,
                trackerActivityClass,
                expandedLayout
            )
        } else {
            populateManualCarouselImages(
                context,
                trackerActivityClass,
                cacheService,
                expandedLayout,
                validCarouselItems,
                packageName,
                pushTemplate.tag,
                fallbackActionUri,
                pushTemplate.isNotificationSticky,
                newIndices.second
            )
        }
    }

    private fun setupNavigationButtons(
        context: Context,
        pushTemplate: ManualCarouselPushTemplate,
        broadcastReceiverClass: Class<out BroadcastReceiver>?,
        imageUris: List<String?>,
        captions: List<String?>,
        interactionUris: List<String?>,
        expandedLayout: RemoteViews
    ) {
        val clickPair =
            if (pushTemplate.carouselLayoutType == PushTemplateConstants.DefaultValues.DEFAULT_MANUAL_CAROUSEL_MODE) {
                Pair(
                    PushTemplateConstants.IntentActions.MANUAL_CAROUSEL_LEFT_CLICKED,
                    PushTemplateConstants.IntentActions.MANUAL_CAROUSEL_RIGHT_CLICKED
                )
            } else {
                Pair(
                    PushTemplateConstants.IntentActions.FILMSTRIP_LEFT_CLICKED,
                    PushTemplateConstants.IntentActions.FILMSTRIP_RIGHT_CLICKED
                )
            }

        val leftClickIntent = AEPPushNotificationBuilder.createClickIntent(
            context,
            pushTemplate,
            clickPair.first,
            broadcastReceiverClass,
            imageUris,
            captions,
            interactionUris
        )
        val pendingIntentLeftButton = PendingIntent.getBroadcast(
            context,
            0,
            leftClickIntent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val rightClickIntent = AEPPushNotificationBuilder.createClickIntent(
            context,
            pushTemplate,
            clickPair.second,
            broadcastReceiverClass,
            imageUris,
            captions,
            interactionUris
        )
        val pendingIntentRightButton = PendingIntent.getBroadcast(
            context,
            0,
            rightClickIntent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        expandedLayout.setOnClickPendingIntent(R.id.leftImageButton, pendingIntentLeftButton)
        expandedLayout.setOnClickPendingIntent(R.id.rightImageButton, pendingIntentRightButton)
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
        autoCancel: Boolean?,
        centerIndex: Int
    ) {
        for (item: CarouselPushTemplate.CarouselItem in items) {
            val imageUri = item.imageUri
            val pushImage: Bitmap? = PushTemplateImageUtil.downloadImage(cacheService, imageUri)
            if (pushImage == null) {
                Log.trace(
                    PushTemplateConstants.LOG_TAG,
                    SELF_TAG,
                    "Failed to retrieve an image from $imageUri, will not create a new carousel item."
                )
                continue
            }
            val carouselItemRemoteView =
                RemoteViews(packageName, R.layout.push_template_carousel_item)
            carouselItemRemoteView.setImageViewBitmap(R.id.carousel_item_image_view, pushImage)
            carouselItemRemoteView.setTextViewText(R.id.carousel_item_caption, item.captionText)

            // assign a click action pending intent for each carousel item
            val interactionUri =
                if (item.interactionUri.isNullOrEmpty()) actionUri else item.interactionUri
            interactionUri?.let {
                AEPPushNotificationBuilder.setRemoteViewClickAction(
                    context,
                    trackerActivityClass,
                    carouselItemRemoteView,
                    R.id.carousel_item_image_view,
                    interactionUri,
                    tag,
                    autoCancel ?: true
                )
            }

            // add the carousel item to the view flipper
            expandedLayout.addView(R.id.manual_carousel_view_flipper, carouselItemRemoteView)

            // set the center image
            expandedLayout.setDisplayedChild(
                R.id.manual_carousel_view_flipper,
                centerIndex
            )
        }
    }

    /**
     * Populates the images for a manual filmstrip carousel push template.
     *
     * @param context the current [Context] of the application
     * @param cacheService the [CacheService] used to cache the downloaded images
     * @param imageCaptions the list of [String] captions for each filmstrip carousel image
     * @param imageClickActions the list of [String] click actions for each filmstrip carousel image
     * @param newIndices a [Triple] of [Int] indices for the new left, center, and right images
     * @param pushTemplate the [ManualCarouselPushTemplate] object containing the push template data
     * @param trackerActivityClass the [Class] of the activity that will be used for tracking interactions with the carousel item
     * @param expandedLayout the [RemoteViews] containing the expanded layout of the notification
     */
    private fun populateFilmstripCarouselImages(
        context: Context,
        cacheService: CacheService,
        imageCaptions: List<String?>,
        imageClickActions: List<String?>,
        newIndices: Triple<Int, Int, Int>,
        pushTemplate: ManualCarouselPushTemplate,
        trackerActivityClass: Class<out Activity>?,
        expandedLayout: RemoteViews
    ) {
        // get all captions present then set center caption text
        val centerCaptionText = imageCaptions[newIndices.second]
        expandedLayout.setTextViewText(
            R.id.manual_carousel_filmstrip_caption,
            centerCaptionText
        )

        // set the downloaded bitmaps in the filmstrip image views
        val assetCacheLocation = PushTemplateImageUtil.getAssetCacheLocation()
        if (assetCacheLocation.isNullOrEmpty()) {
            Log.trace(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                "Asset cache location is null or empty, unable to retrieve filmstrip carousel images."
            )
            return
        }

        var cacheResult =
            cacheService.get(
                assetCacheLocation,
                pushTemplate.carouselItems[newIndices.first].imageUri
            )
        val newLeftImage = BitmapFactory.decodeStream(cacheResult?.data)
        expandedLayout.setImageViewBitmap(
            R.id.manual_carousel_filmstrip_left, newLeftImage
        )

        cacheResult = cacheService.get(
            assetCacheLocation,
            pushTemplate.carouselItems[newIndices.second].imageUri
        )
        val newCenterImage = BitmapFactory.decodeStream(cacheResult?.data)
        expandedLayout.setImageViewBitmap(
            R.id.manual_carousel_filmstrip_center, newCenterImage
        )

        cacheResult =
            cacheService.get(
                assetCacheLocation,
                pushTemplate.carouselItems[newIndices.third].imageUri
            )
        val newRightImage = BitmapFactory.decodeStream(cacheResult?.data)
        expandedLayout.setImageViewBitmap(
            R.id.manual_carousel_filmstrip_right, newRightImage
        )

        // assign a click action pending intent to the center image view
        val interactionUri =
            if (!imageClickActions[newIndices.second].isNullOrEmpty()) imageClickActions[newIndices.second] else pushTemplate.actionUri
        AEPPushNotificationBuilder.setRemoteViewClickAction(
            context,
            trackerActivityClass,
            expandedLayout,
            R.id.manual_carousel_filmstrip_center,
            interactionUri,
            pushTemplate.tag,
            pushTemplate.isNotificationSticky ?: false
        )
    }

    /**
     * Calculates a new left, center, and right index for a carousel skip left press given the current center index and total number
     * of images
     *
     * @param centerIndex [Int] containing the current center image index
     * @param listSize `Int` containing the total number of images
     * @return [Triple] containing the calculated left, center, and right indices
     */
    private fun getNewIndicesForNavigateLeft(
        centerIndex: Int,
        listSize: Int
    ): Triple<Int, Int, Int> {
        val newCenterIndex = (centerIndex - 1 + listSize) % listSize
        val newLeftIndex = (newCenterIndex - 1 + listSize) % listSize
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Calculated new indices. New center index is $newCenterIndex, new left index is $newLeftIndex, and new right index is $centerIndex."
        )
        return Triple(newLeftIndex, newCenterIndex, centerIndex)
    }

    /**
     * Calculates a new left, center, and right index for a carousel skip right press given the current center index and total number
     * of images
     *
     * @param centerIndex [Int] containing the current center image index
     * @param listSize `Int` containing the total number of images
     * @return [Triple] containing the calculated left, center, and right indices
     */
    private fun getNewIndicesForNavigateRight(
        centerIndex: Int,
        listSize: Int
    ): Triple<Int, Int, Int> {
        val newCenterIndex = (centerIndex + 1) % listSize
        val newRightIndex = (newCenterIndex + 1) % listSize
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Calculated new indices. New center index is $newCenterIndex, new left index is $centerIndex, and new right index is $newRightIndex."
        )
        return Triple(centerIndex, newCenterIndex, newRightIndex)
    }
}
