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
import android.content.Context
import android.graphics.Bitmap
import android.widget.RemoteViews
import com.adobe.marketing.mobile.core.R
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.caching.CacheService

internal object PushTemplateImageHelper {
    const val SELF_TAG = "PushTemplateImageHelper"

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
    internal fun populateManualCarouselImages(
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
            val pushImage: Bitmap? = downloadImage(cacheService, imageUri)
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
                AEPPushTemplateNotificationBuilder.setRemoteViewClickAction(
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
    internal fun populateAutoCarouselImages(
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
            val pushImage: Bitmap? = downloadImage(cacheService, imageUri)
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

    /**
     * Downloads the images for a filmstrip carousel push template.
     *
     * @param cacheService the [CacheService] used to cache the downloaded images
     * @param items the list of [CarouselPushTemplate.CarouselItem] objects to be displayed in the filmstrip carousel
     * @return a [Map] containing the downloaded images, image URIs, image captions, and image click actions for each carousel item
     */
    internal fun downloadFilmstripImages(
        cacheService: CacheService,
        items: List<CarouselPushTemplate.CarouselItem>
    ): Map<String, List<Any?>> {
        val imageProcessingStartTime = System.currentTimeMillis()
        val downloadedImages = mutableListOf<Bitmap?>()
        val downloadedImageUris = mutableListOf<String?>()
        val imageCaptions = mutableListOf<String?>()
        val imageClickActions = mutableListOf<String?>()
        val itemData: MutableMap<String, List<Any?>> = mutableMapOf()

        for (item: CarouselPushTemplate.CarouselItem in items) {
            val imageUri: String = item.imageUri
            val pushImage: Bitmap? = downloadImage(cacheService, imageUri)
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

        // log time needed to process the carousel images
        val imageProcessingElapsedTime = System.currentTimeMillis() - imageProcessingStartTime
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Processed %d manual filmstrip carousel image(s) in %d milliseconds.",
            downloadedImageUris.size,
            imageProcessingElapsedTime
        )

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
    internal fun populateFilmstripCarouselImages(
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
        AEPPushTemplateNotificationBuilder.setRemoteViewClickAction(
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
