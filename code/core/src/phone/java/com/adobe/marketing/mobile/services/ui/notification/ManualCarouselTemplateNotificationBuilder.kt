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
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.adobe.marketing.mobile.core.R
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.ui.notification.PushTemplateImageHelper.populateManualCarouselImages

/**
 * Object responsible for constructing a [NotificationCompat.Builder] object containing a manual carousel push template notification.
 */
internal object ManualCarouselTemplateNotificationBuilder : AEPPushTemplateNotificationBuilder() {
    private const val SELF_TAG = "ManualCarouselTemplateNotificationBuilder"

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
        val extractedItemData = populateManualCarouselImages(
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
            return fallbackToBasicNotification(
                context,
                trackerActivityName,
                broadcastReceiverName,
                pushTemplate,
                downloadedImageUris
            )
        }

        // if we have an intent action then we need to calculate a new center index and update the view flipper
        val centerImageIndex = pushTemplate.centerImageIndex
        val newIndices: List<Int>
        if (pushTemplate.intentAction?.isNotEmpty() == true) {
            newIndices = CarouselTemplateHelpers.calculateNewIndices(
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

        // Create the notification channel if needed
        channelIdToUse = createChannelAndGetChannelID(
            context,
            pushTemplate.channelId,
            pushTemplate.sound,
            pushTemplate.getNotificationImportance()
        )

        // handle left and right navigation buttons
        val clickIntent = createClickIntent(
            context,
            pushTemplate,
            PushTemplateConstants.IntentActions.MANUAL_CAROUSEL_LEFT_CLICKED,
            broadcastReceiverName,
            trackerActivityName,
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

        // create the notification builder with the common settings applied
        return super.construct(
            context,
            pushTemplate,
            trackerActivityName,
            smallLayout,
            expandedLayout
        )
    }
}
