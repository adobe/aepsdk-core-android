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

/**
 * Object responsible for constructing a [NotificationCompat.Builder] object containing a manual filmstrip carousel template notification.
 */
internal object FilmstripCarouselTemplateNotificationBuilder : AEPPushTemplateNotificationBuilder() {
    private const val SELF_TAG = "FilmstripCarouselTemplateNotificationBuilder"

    @Throws(NotificationConstructionFailedException::class)
    fun construct(
        context: Context,
        pushTemplate: ManualCarouselPushTemplate?,
        trackerActivityName: String?,
        broadcastReceiverName: String?
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

        // download the carousel images and populate the image uri, image caption, and image click
        // action arrays
        val imageProcessingStartTime = System.currentTimeMillis()
        val items: List<CarouselPushTemplate.CarouselItem> = pushTemplate.carouselItems
        val downloadedImages = ArrayList<Bitmap?>()
        val downloadedImageUris = ArrayList<String?>()
        val imageCaptions = ArrayList<String?>()
        val imageClickActions = ArrayList<String?>()
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

        // fallback to a basic push template notification builder if less than 3 images were able
        // to be downloaded
        if ((downloadedImageUris.size < PushTemplateConstants.DefaultValues.CAROUSEL_MINIMUM_IMAGE_COUNT)) {
            return fallbackToBasicNotification(
                context,
                trackerActivityName,
                broadcastReceiverName,
                pushTemplate,
                downloadedImageUris
            )
        }

        // if we have an intent action then we need to calculate a new center index
        var centerImageIndex = pushTemplate.centerImageIndex ?: PushTemplateConstants.DefaultValues.FILMSTRIP_CAROUSEL_CENTER_INDEX
        var newIndices: List<Int> = emptyList()
        pushTemplate.intentAction?.let {
            newIndices = CarouselTemplateHelpers.calculateNewIndices(centerImageIndex, downloadedImageUris.size, pushTemplate.intentAction)
            centerImageIndex = newIndices[1]
        }

        // set the carousel images in the filmstrip carousel
        setCarouselImages(
            context,
            downloadedImages,
            imageCaptions,
            imageClickActions,
            newIndices,
            pushTemplate,
            trackerActivityName,
            expandedLayout
        )

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
            PushTemplateConstants.IntentActions.FILMSTRIP_LEFT_CLICKED
        )
        broadcastReceiverName?.let {
            val broadcastReceiver = Class.forName(broadcastReceiverName)
            clickIntent.setClass(context.applicationContext, broadcastReceiver::class.java)
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
        clickIntent.putExtra(
            PushTemplateConstants.IntentKeys.CENTER_IMAGE_INDEX,
            centerImageIndex
        )
        clickIntent.putExtra(
            PushTemplateConstants.IntentKeys.IMAGE_URLS,
            downloadedImageUris
        )
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
            PushTemplateConstants.IntentKeys.LARGE_ICON, pushTemplate.largeIcon
        )
        clickIntent.putExtra(
            PushTemplateConstants.IntentKeys.SMALL_ICON_COLOR,
            pushTemplate.smallIconColor
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
            PushTemplateConstants.IntentKeys.TICKER, pushTemplate.ticker
        )
        clickIntent.putExtra(
            PushTemplateConstants.IntentKeys.TAG, pushTemplate.tag
        )
        clickIntent.putExtra(
            PushTemplateConstants.IntentKeys.STICKY, pushTemplate.isNotificationSticky
        )
        clickIntent.putExtra(PushTemplateConstants.IntentKeys.ACTION_URI, pushTemplate.actionUri)
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

        // create the notification builder with the common settings applied
        return super.construct(context, pushTemplate, trackerActivityName, smallLayout, expandedLayout)
    }

    private fun setCarouselImages(
        context: Context,
        downloadedImages: List<Bitmap?>,
        imageCaptions: List<String?>,
        imageClickActions: List<String?>,
        newIndices: List<Int>,
        pushTemplate: ManualCarouselPushTemplate,
        trackerActivityName: String?,
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
        val interactionUri = if (imageClickActions[newCenterIndex].isNullOrEmpty()) imageClickActions[newCenterIndex] else pushTemplate.actionUri
        setRemoteViewClickAction(
            context,
            trackerActivityName,
            expandedLayout,
            R.id.manual_carousel_filmstrip_center,
            interactionUri,
            pushTemplate.tag,
            pushTemplate.isNotificationSticky ?: false
        )
    }
}
