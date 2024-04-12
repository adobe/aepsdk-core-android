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

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.adobe.marketing.mobile.core.R
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.caching.CacheService
import com.adobe.marketing.mobile.services.ui.notification.CarouselPushTemplate.CarouselItem

/**
 * Object responsible for constructing a [NotificationCompat.Builder] object containing a manual carousel push template notification.
 */
internal object ManualCarouselTemplateNotificationBuilder : AEPPushTemplateNotificationBuilder() {
    private const val SELF_TAG = "ManualCarouselTemplateNotificationBuilder"
    private const val IMAGE_URIS_KEY = "imageUris"
    private const val IMAGE_CAPTIONS_KEY = "imageCaptions"
    private const val IMAGE_ACTIONS_KEY = "imageActions"

    // TODO: create extension function for AEPPushTemplate that creates click intent from the template (or within AEPPushTemplateNotificationBuilder)
    // TODO: only extras are the carousel specific values: image urls, image captions, image click actions
    @Throws(NotificationConstructionFailedException::class)
    fun construct(
        context: Context,
        pushTemplate: ManualCarouselPushTemplate?,
        trackerActivityName: String?,
        broadcastReceiverName: String?
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

        // load images into the carousel
        val fallbackActionUri = pushTemplate.actionUri
        val items = pushTemplate.carouselItems
        val extractedItemData = populateImages(
            context,
            trackerActivityName,
            cacheService,
            expandedLayout,
            items,
            packageName,
            pushTemplate.tag,
            fallbackActionUri,
            pushTemplate.isNotificationSticky
        )
        val downloadedImageUris = extractedItemData[IMAGE_URIS_KEY]
        val imageCaptions = extractedItemData[IMAGE_CAPTIONS_KEY]
        val imageClickActions = extractedItemData[IMAGE_ACTIONS_KEY]

        // fallback to a basic push template notification builder if less than 3 images were able
        // to be downloaded
        if (!downloadedImageUris.isNullOrEmpty() && downloadedImageUris.size < PushTemplateConstants.DefaultValues.CAROUSEL_MINIMUM_IMAGE_COUNT) {
            return fallbackToBasicNotification(
                context,
                trackerActivityName,
                broadcastReceiverName,
                pushTemplate,
                downloadedImageUris
            )
        }

        // if we have an intent action then we need to calculate a new center index
        var centerImageIndex = pushTemplate.centerImageIndex ?: PushTemplateConstants.DefaultValues.MANUAL_CAROUSEL_START_INDEX
        var newIndices: List<Int> = emptyList()
        pushTemplate.intentAction?.let {
            newIndices = CarouselTemplateHelpers.calculateNewIndices(centerImageIndex, downloadedImageUris?.size, pushTemplate.intentAction)
            centerImageIndex = newIndices[1]
        }

        // set a new center image if the center index has changed
        if (newIndices.isNotEmpty()) {
            val items = ArrayList<CarouselItem>()
            downloadedImageUris?.let {
                val centerCarouselItem = CarouselItem(
                    downloadedImageUris[centerImageIndex],
                    imageCaptions?.get(centerImageIndex),
                    imageClickActions?.get(centerImageIndex)
                )
                items.add(centerCarouselItem)
                populateImages(
                    context,
                    trackerActivityName,
                    cacheService,
                    expandedLayout,
                    items,
                    packageName,
                    pushTemplate.tag,
                    fallbackActionUri,
                    pushTemplate.isNotificationSticky
                )
            }
        }

        val titleText = pushTemplate.title
        val smallBodyText = pushTemplate.body
        val expandedBodyText = pushTemplate.expandedBodyText
        smallLayout.setTextViewText(R.id.notification_title, titleText)
        smallLayout.setTextViewText(R.id.notification_body, smallBodyText)
        expandedLayout.setTextViewText(R.id.notification_title, titleText)
        expandedLayout.setTextViewText(R.id.notification_body_expanded, expandedBodyText)

        // Create the notification channel if needed
        channelIdToUse = createChannelAndGetChannelID(
            context,
            pushTemplate.channelId,
            pushTemplate.sound,
            pushTemplate.getNotificationImportance()
        )

        // handle left and right navigation buttons
        val clickIntent = Intent(
            PushTemplateConstants.IntentActions.MANUAL_CAROUSEL_LEFT_CLICKED
        )
        broadcastReceiverName?.let {
            val broadcastReceiverClass = Class.forName(broadcastReceiverName)
            clickIntent.setClass(context.applicationContext, broadcastReceiverClass::class.java)
        }

        clickIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        clickIntent.putExtra(
            PushTemplateConstants.IntentKeys.TYPE,
            pushTemplate.templateType?.value
        )
        clickIntent.putExtra(PushTemplateConstants.IntentKeys.TRACKER_NAME, trackerActivityName)
        clickIntent.putExtra(
            PushTemplateConstants.IntentKeys.BROADCAST_RECEIVER_NAME,
            broadcastReceiverName
        )
        clickIntent.putExtra(PushTemplateConstants.IntentKeys.CHANNEL_ID, channelIdToUse)
        clickIntent.putExtra(
            PushTemplateConstants.IntentKeys.CUSTOM_SOUND, pushTemplate.sound
        )
        clickIntent.putExtra(PushTemplateConstants.IntentKeys.CENTER_IMAGE_INDEX, centerImageIndex)
        clickIntent.putExtra(PushTemplateConstants.IntentKeys.IMAGE_URLS, downloadedImageUris)
        clickIntent.putExtra(PushTemplateConstants.IntentKeys.IMAGE_CAPTIONS, imageCaptions)
        clickIntent.putExtra(
            PushTemplateConstants.IntentKeys.IMAGE_CLICK_ACTIONS, imageClickActions
        )
        clickIntent.putExtra(PushTemplateConstants.IntentKeys.TITLE_TEXT, titleText)
        clickIntent.putExtra(PushTemplateConstants.IntentKeys.BODY_TEXT, smallBodyText)
        clickIntent.putExtra(PushTemplateConstants.IntentKeys.EXPANDED_BODY_TEXT, expandedBodyText)
        clickIntent.putExtra(
            PushTemplateConstants.IntentKeys.NOTIFICATION_BACKGROUND_COLOR,
            pushTemplate.notificationBackgroundColor
        )
        clickIntent.putExtra(
            PushTemplateConstants.IntentKeys.TITLE_TEXT_COLOR,
            pushTemplate.titleTextColor
        )
        clickIntent.putExtra(
            PushTemplateConstants.IntentKeys.EXPANDED_BODY_TEXT_COLOR,
            pushTemplate.expandedBodyTextColor
        )
        clickIntent.putExtra(
            PushTemplateConstants.IntentKeys.SMALL_ICON, pushTemplate.smallIcon
        )
        clickIntent.putExtra(
            PushTemplateConstants.IntentKeys.SMALL_ICON_COLOR,
            pushTemplate.smallIconColor
        )
        clickIntent.putExtra(
            PushTemplateConstants.IntentKeys.LARGE_ICON, pushTemplate.largeIcon
        )
        clickIntent.putExtra(
            PushTemplateConstants.IntentKeys.VISIBILITY,
            pushTemplate.getNotificationVisibility()
        )
        clickIntent.putExtra(
            PushTemplateConstants.IntentKeys.IMPORTANCE,
            pushTemplate.getNotificationImportance()
        )
        clickIntent.putExtra(
            PushTemplateConstants.IntentKeys.STICKY, pushTemplate.isNotificationSticky
        )
        clickIntent.putExtra(
            PushTemplateConstants.IntentKeys.TAG, pushTemplate.tag
        )
        clickIntent.putExtra(
            PushTemplateConstants.IntentKeys.TICKER, pushTemplate.ticker
        )
        clickIntent.putExtra(PushTemplateConstants.IntentKeys.ACTION_URI, fallbackActionUri)
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

        // create the notification builder with the common settings applied
        return super.construct(context, pushTemplate, trackerActivityName, smallLayout, expandedLayout)
    }

    // TODO: move to ImageHelper object that can populate images for all push templates
    private fun populateImages(
        context: Context,
        trackerActivityName: String?,
        cacheService: CacheService,
        expandedLayout: RemoteViews,
        items: ArrayList<CarouselItem>,
        packageName: String?,
        tag: String?,
        actionUri: String?,
        autoCancel: Boolean?
    ): Map<String, ArrayList<String>> {
        val downloadedImageUris = ArrayList<String>()
        val imageCaptions = ArrayList<String>()
        val imageClickActions = ArrayList<String>()
        val itemData: MutableMap<String, ArrayList<String>> = mutableMapOf()
        val imageProcessingStartTime = System.currentTimeMillis()
        for (item: CarouselItem in items) {
            val imageUri = item.imageUri
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
            item.captionText?.let { imageCaptions.add(it) }
            carouselItem.setImageViewBitmap(R.id.carousel_item_image_view, pushImage)
            carouselItem.setTextViewText(R.id.carousel_item_caption, item.captionText)

            // assign a click action pending intent for each carousel item
            val interactionUri = item.interactionUri ?: actionUri
            interactionUri?.let {
                imageClickActions.add(interactionUri)
                setRemoteViewClickAction(
                    context,
                    trackerActivityName,
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
        itemData[IMAGE_URIS_KEY] = downloadedImageUris
        itemData[IMAGE_CAPTIONS_KEY] = imageCaptions
        itemData[IMAGE_ACTIONS_KEY] = imageClickActions
        return itemData
    }
}
