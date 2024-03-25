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
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.adobe.marketing.mobile.core.R
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.caching.CacheService
import com.adobe.marketing.mobile.services.ui.pushtemplate.CarouselPushTemplate.CarouselItem
import com.google.android.gms.common.util.CollectionUtils

internal object ManualCarouselTemplateNotificationBuilder {
    private const val SELF_TAG = "ManualCarouselTemplateNotificationBuilder"
    private const val IMAGE_URIS_KEY = "imageUris"
    private const val IMAGE_CAPTIONS_KEY = "imageCaptions"
    private const val IMAGE_ACTIONS_KEY = "imageActions"

    // TODO: migrate logic of building the notification to a common class to be used by
    // FilmstripCarouselTemplateNotificationBuilder and ManualCarouselTemplateNotificationBuilder
    @Throws(NotificationConstructionFailedException::class)
    internal fun construct(
        context: Context,
        trackerActivity: Activity,
        broadcastReceiver: BroadcastReceiver,
        pushTemplate: CarouselPushTemplate,
        packageName: String,
        channelId: String
    ): NotificationCompat.Builder {
        val cacheService = ServiceProvider.getInstance().cacheService
            ?: throw NotificationConstructionFailedException(
                (
                    "Cache service is null, default manual carousel push notification will not be" +
                        " constructed."
                    )
            )
        val smallLayout = RemoteViews(packageName, R.layout.push_template_collapsed)
        val expandedLayout = RemoteViews(packageName, R.layout.push_template_manual_carousel)
        val fallbackActionUri = pushTemplate.getActionUri()

        // load images into the carousel
        val items = pushTemplate.getCarouselItems()
        val extractedItemData = populateImages(
            context,
            trackerActivity,
            cacheService,
            expandedLayout,
            items,
            packageName,
            pushTemplate.getMessageId(),
            pushTemplate.getDeliveryId(),
            pushTemplate.getTag(),
            fallbackActionUri,
            pushTemplate.getStickyStatus()
        )
        val downloadedImageUris = extractedItemData[IMAGE_URIS_KEY]
        val imageCaptions = extractedItemData[IMAGE_CAPTIONS_KEY]
        val imageClickActions = extractedItemData[IMAGE_ACTIONS_KEY]

        // fallback to a basic push template notification builder if less than 3 images were able
        // to be downloaded
        if (!downloadedImageUris.isNullOrEmpty() && downloadedImageUris.size < PushTemplateConstants.DefaultValues.CAROUSEL_MINIMUM_IMAGE_COUNT) {
            return CarouselTemplateNotificationBuilder.fallbackToBasicNotification(
                context, trackerActivity, broadcastReceiver, pushTemplate, downloadedImageUris
            )
        }
        val titleText = pushTemplate.getTitle()
        val smallBodyText = pushTemplate.getBody()
        val expandedBodyText = pushTemplate.getExpandedBodyText()
        smallLayout.setTextViewText(R.id.notification_title, titleText)
        smallLayout.setTextViewText(R.id.notification_body, smallBodyText)
        expandedLayout.setTextViewText(R.id.notification_title, titleText)
        expandedLayout.setTextViewText(R.id.notification_body_expanded, expandedBodyText)
        val centerImageIndex: Int =
            PushTemplateConstants.DefaultValues.MANUAL_CAROUSEL_START_INDEX // start index defaults to 0

        // set any custom colors if needed
        setCustomNotificationColors(
            pushTemplate.getNotificationBackgroundColor(),
            pushTemplate.getTitleTextColor(),
            pushTemplate.getExpandedBodyTextColor(),
            smallLayout,
            expandedLayout,
            R.id.carousel_container_layout
        )

        // handle left and right navigation buttons
        val clickIntent = Intent(
            PushTemplateConstants.IntentActions.MANUAL_CAROUSEL_LEFT_CLICKED,
            null,
            context,
            broadcastReceiver::class.java
        )
        clickIntent.setClass(context, broadcastReceiver::class.java)
        clickIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        clickIntent.putExtra(PushTemplateConstants.IntentKeys.CHANNEL_ID, channelId)
        clickIntent.putExtra(
            PushTemplateConstants.IntentKeys.CUSTOM_SOUND, pushTemplate.getSound()
        )
        clickIntent.putExtra(PushTemplateConstants.IntentKeys.CENTER_IMAGE_INDEX, centerImageIndex)
        clickIntent.putExtra(PushTemplateConstants.IntentKeys.IMAGE_URLS, downloadedImageUris)
        clickIntent.putExtra(PushTemplateConstants.IntentKeys.IMAGE_CAPTIONS, imageCaptions)
        clickIntent.putExtra(
            PushTemplateConstants.IntentKeys.IMAGE_CLICK_ACTIONS, imageClickActions
        )
        clickIntent.putExtra(PushTemplateConstants.IntentKeys.TITLE_TEXT, titleText)
        clickIntent.putExtra(PushTemplateConstants.IntentKeys.BODY_TEXT, pushTemplate.getBody())
        clickIntent.putExtra(PushTemplateConstants.IntentKeys.EXPANDED_BODY_TEXT, expandedBodyText)
        clickIntent.putExtra(
            PushTemplateConstants.IntentKeys.NOTIFICATION_BACKGROUND_COLOR,
            pushTemplate.getNotificationBackgroundColor()
        )
        clickIntent.putExtra(
            PushTemplateConstants.IntentKeys.TITLE_TEXT_COLOR,
            pushTemplate.getTitleTextColor()
        )
        clickIntent.putExtra(
            PushTemplateConstants.IntentKeys.EXPANDED_BODY_TEXT_COLOR,
            pushTemplate.getExpandedBodyTextColor()
        )
        clickIntent.putExtra(
            PushTemplateConstants.IntentKeys.MESSAGE_ID, pushTemplate.getMessageId()
        )
        clickIntent.putExtra(
            PushTemplateConstants.IntentKeys.DELIVERY_ID, pushTemplate.getDeliveryId()
        )
        clickIntent.putExtra(
            PushTemplateConstants.IntentKeys.SMALL_ICON, pushTemplate.getSmallIcon()
        )
        clickIntent.putExtra(
            PushTemplateConstants.IntentKeys.SMALL_ICON_COLOR,
            pushTemplate.getSmallIconColor()
        )
        clickIntent.putExtra(
            PushTemplateConstants.IntentKeys.LARGE_ICON, pushTemplate.getLargeIcon()
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
            PushTemplateConstants.IntentKeys.STICKY, pushTemplate.getStickyStatus()
        )
        clickIntent.putExtra(
            PushTemplateConstants.IntentKeys.TAG, pushTemplate.getTag()
        )
        clickIntent.putExtra(
            PushTemplateConstants.IntentKeys.TICKER, pushTemplate.getTicker()
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
        val builder = NotificationCompat.Builder(context, channelId)
            .setTicker(pushTemplate.getTicker())
            .setNumber(pushTemplate.getBadgeCount())
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(smallLayout)
            .setCustomBigContentView(expandedLayout)

        // set custom sound, note this applies to API 25 and lower only as API 26 and up set the
        // sound on the notification channel
        setSound(context, builder, pushTemplate.getSound())

        // small Icon must be present, otherwise the notification will not be displayed.
        setSmallIcon(
            context, builder, pushTemplate.getSmallIcon(), pushTemplate.getSmallIconColor()
        )

        // set a large icon if one is present
        setRemoteViewLargeIcon(pushTemplate.getLargeIcon(), smallLayout)
        setRemoteViewLargeIcon(
            pushTemplate.getLargeIcon(), expandedLayout
        )

        // set notification visibility
        setVisibility(
            builder, pushTemplate.getNotificationVisibility()
        )

        // if API level is below 26 (prior to notification channels) then notification priority is
        // set on the notification builder
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(LongArray(0)) // hack to enable heads up notifications as a HUD style
            // notification requires a tone or vibration
        }
        return builder
    }

    @Throws(NotificationConstructionFailedException::class)
    internal fun construct(
        context: Context,
        trackerActivity: Activity,
        broadcastReceiver: BroadcastReceiver,
        intent: Intent
    ): NotificationCompat.Builder {
        val intentExtras = intent.extras
            ?: throw NotificationConstructionFailedException(
                (
                    "Intent extras are null, will not create a notification from the received" +
                        " intent with action " +
                        intent.action
                    )
            )
        val cacheService = ServiceProvider.getInstance().cacheService
            ?: throw NotificationConstructionFailedException(
                (
                    "Cache service is null, default manual carousel notification will not be" +
                        " constructed."
                    )
            )
        val assetCacheLocation = assetCacheLocation
            ?: throw NotificationConstructionFailedException(
                (
                    "Asset cache location is null, default manual carousel notification will not be" +
                        " constructed."
                    )
            )
        val packageName = ServiceProvider.getInstance()
            .appContextService
            .getApplication()
            ?.packageName

        // get manual carousel notification values from the intent extras
        val messageId =
            intentExtras.getString(PushTemplateConstants.IntentKeys.MESSAGE_ID) as String
        val deliveryId =
            intentExtras.getString(PushTemplateConstants.IntentKeys.DELIVERY_ID) as String
        val channelId = intentExtras.getString(PushTemplateConstants.IntentKeys.CHANNEL_ID)
        val badgeCount = intentExtras.getInt(PushTemplateConstants.IntentKeys.BADGE_COUNT)
        val visibility = intentExtras.getInt(PushTemplateConstants.IntentKeys.VISIBILITY)
        val importance = intentExtras.getInt(PushTemplateConstants.IntentKeys.IMPORTANCE)
        val cachedImages = ArrayList<Bitmap>()
        val imageUrls =
            intentExtras.getParcelableArrayList(
                PushTemplateConstants.IntentKeys.IMAGE_URLS,
                String::class.java
            )
        val imageCaptions =
            intentExtras.getParcelableArrayList(
                PushTemplateConstants.IntentKeys.IMAGE_CAPTIONS,
                String::class.java
            )
        val imageClickActions =
            intentExtras.getParcelableArrayList(
                PushTemplateConstants.IntentKeys.IMAGE_CLICK_ACTIONS,
                String::class.java
            )
        val titleText = intentExtras.getString(PushTemplateConstants.IntentKeys.TITLE_TEXT)
        val bodyText = intentExtras.getString(PushTemplateConstants.IntentKeys.BODY_TEXT)
        val expandedBodyText =
            intentExtras.getString(PushTemplateConstants.IntentKeys.EXPANDED_BODY_TEXT)
        val notificationBackgroundColor = intentExtras.getString(
            PushTemplateConstants.IntentKeys.NOTIFICATION_BACKGROUND_COLOR
        )
        val titleTextColor =
            intentExtras.getString(PushTemplateConstants.IntentKeys.TITLE_TEXT_COLOR)
        val expandedBodyTextColor =
            intentExtras.getString(PushTemplateConstants.IntentKeys.EXPANDED_BODY_TEXT_COLOR)
        val smallIcon = intentExtras.getString(PushTemplateConstants.IntentKeys.SMALL_ICON)
        val smallIconColor =
            intentExtras.getString(PushTemplateConstants.IntentKeys.SMALL_ICON_COLOR)
        val largeIcon = intentExtras.getString(PushTemplateConstants.IntentKeys.LARGE_ICON)
        val customSound = intentExtras.getString(PushTemplateConstants.IntentKeys.CUSTOM_SOUND)
        val ticker = intentExtras.getString(PushTemplateConstants.IntentKeys.TICKER)
        val sticky = intentExtras.getBoolean(PushTemplateConstants.IntentKeys.STICKY)
        val tag = intentExtras.getString(PushTemplateConstants.IntentKeys.TAG) as String
        val fallbackActionUri = intentExtras.getString(PushTemplateConstants.IntentKeys.ACTION_URI)

        // as we are handling an intent, the image URLS should already be cached
        if (imageUrls != null && !CollectionUtils.isEmpty(imageUrls)) {
            for (imageUri: String? in imageUrls) {
                imageUri?.let {
                    val cacheResult = cacheService.get(assetCacheLocation, imageUri)
                    if (cacheResult != null) {
                        cachedImages.add(BitmapFactory.decodeStream(cacheResult.getData()))
                    }
                }
            }
        }
        val smallLayout = RemoteViews(packageName, R.layout.push_template_collapsed)
        val expandedLayout = RemoteViews(packageName, R.layout.push_template_manual_carousel)
        smallLayout.setTextViewText(R.id.notification_title, titleText)
        smallLayout.setTextViewText(R.id.notification_body, bodyText)
        expandedLayout.setTextViewText(R.id.notification_title, titleText)
        expandedLayout.setTextViewText(R.id.notification_body_expanded, expandedBodyText)
        val action = intent.action
        val centerImageIndex =
            intentExtras.getInt(PushTemplateConstants.IntentKeys.CENTER_IMAGE_INDEX)
        val newIndices: List<Int>? =
            calculateNewIndices(centerImageIndex, imageUrls?.size, action)
        val newCenterIndex: Int = if (newIndices == null) {
            Log.trace(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                (
                    "Unable to calculate new left, center, and right indices. Using default start" +
                        " image index of 0."
                    )
            )
            PushTemplateConstants.DefaultValues.MANUAL_CAROUSEL_START_INDEX
        } else {
            newIndices[1]
        }

        // update the carousel view flipper with the new center index
        val items = ArrayList<CarouselItem>()
        val imageUri = imageUrls?.get(newCenterIndex)
        imageUri?.let {
            val centerCarouselItem = CarouselItem(
                imageUri,
                imageCaptions?.get(newCenterIndex),
                imageClickActions?.get(newCenterIndex)
            )
            items.add(centerCarouselItem)
        }

        populateImages(
            context,
            trackerActivity,
            cacheService,
            expandedLayout,
            items,
            packageName,
            messageId,
            deliveryId,
            tag,
            fallbackActionUri,
            sticky
        )

        // set any custom colors if needed
        setCustomNotificationColors(
            notificationBackgroundColor,
            titleTextColor,
            expandedBodyTextColor,
            smallLayout,
            expandedLayout,
            R.id.carousel_container_layout
        )

        // handle left and right navigation buttons
        val clickIntent = Intent(
            PushTemplateConstants.IntentActions.MANUAL_CAROUSEL_LEFT_CLICKED,
            null,
            context,
            broadcastReceiver::class.java
        )
        clickIntent.setClass(context, broadcastReceiver::class.java)
        clickIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        clickIntent.putExtra(PushTemplateConstants.IntentKeys.CHANNEL_ID, channelId)
        clickIntent.putExtra(PushTemplateConstants.IntentKeys.CUSTOM_SOUND, customSound)
        clickIntent.putExtra(PushTemplateConstants.IntentKeys.CENTER_IMAGE_INDEX, newCenterIndex)
        clickIntent.putExtra(PushTemplateConstants.IntentKeys.IMAGE_URLS, imageUrls)
        clickIntent.putExtra(PushTemplateConstants.IntentKeys.IMAGE_CAPTIONS, imageCaptions)
        clickIntent.putExtra(
            PushTemplateConstants.IntentKeys.IMAGE_CLICK_ACTIONS, imageClickActions
        )
        clickIntent.putExtra(PushTemplateConstants.IntentKeys.TITLE_TEXT, titleText)
        clickIntent.putExtra(PushTemplateConstants.IntentKeys.BODY_TEXT, bodyText)
        clickIntent.putExtra(PushTemplateConstants.IntentKeys.EXPANDED_BODY_TEXT, expandedBodyText)
        clickIntent.putExtra(
            PushTemplateConstants.IntentKeys.NOTIFICATION_BACKGROUND_COLOR,
            notificationBackgroundColor
        )
        clickIntent.putExtra(PushTemplateConstants.IntentKeys.TITLE_TEXT_COLOR, titleTextColor)
        clickIntent.putExtra(
            PushTemplateConstants.IntentKeys.EXPANDED_BODY_TEXT_COLOR, expandedBodyTextColor
        )
        clickIntent.putExtra(PushTemplateConstants.IntentKeys.MESSAGE_ID, messageId)
        clickIntent.putExtra(PushTemplateConstants.IntentKeys.DELIVERY_ID, deliveryId)
        clickIntent.putExtra(PushTemplateConstants.IntentKeys.SMALL_ICON, smallIcon)
        clickIntent.putExtra(PushTemplateConstants.IntentKeys.SMALL_ICON_COLOR, smallIconColor)
        clickIntent.putExtra(PushTemplateConstants.IntentKeys.LARGE_ICON, largeIcon)
        clickIntent.putExtra(PushTemplateConstants.IntentKeys.VISIBILITY, visibility)
        clickIntent.putExtra(PushTemplateConstants.IntentKeys.IMPORTANCE, importance)
        clickIntent.putExtra(PushTemplateConstants.IntentKeys.TICKER, ticker)
        clickIntent.putExtra(PushTemplateConstants.IntentKeys.TAG, tag)
        clickIntent.putExtra(PushTemplateConstants.IntentKeys.STICKY, sticky)
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

        // set onclick intents for the skip left and skip right buttons
        expandedLayout.setOnClickPendingIntent(R.id.leftImageButton, pendingIntentLeftButton)
        expandedLayout.setOnClickPendingIntent(R.id.rightImageButton, pendingIntentRightButton)

        // we need to create a silent notification as this will be re-displaying a notification
        // rather than showing a new one.
        // the silent sound is set on the notification channel and notification builder.
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Displaying a silent notification after handling an intent."
        )

        // Create the notification
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(
            context,
            PushTemplateConstants.DefaultValues.SILENT_NOTIFICATION_CHANNEL_ID
        )
            .setSound(null)
            .setTicker(ticker)
            .setNumber(badgeCount)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(smallLayout)
            .setCustomBigContentView(expandedLayout)

        // small Icon must be present, otherwise the notification will not be displayed.
        setSmallIcon(context, builder, smallIcon, smallIconColor)

        // set a large icon if one is present
        setRemoteViewLargeIcon(largeIcon, smallLayout)
        setRemoteViewLargeIcon(largeIcon, expandedLayout)

        // set notification visibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setVisibility(builder, visibility)
        }

        // set notification delete action
        setNotificationDeleteAction(
            context, trackerActivity, builder, messageId, deliveryId
        )

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
        trackerActivity: Activity,
        cacheService: CacheService,
        expandedLayout: RemoteViews,
        items: ArrayList<CarouselItem>,
        packageName: String?,
        messageId: String,
        deliveryId: String,
        tag: String,
        actionUri: String?,
        autoCancel: Boolean?
    ): MutableMap<String, ArrayList<String?>> {
        val downloadedImageUris = ArrayList<String?>()
        val imageCaptions = ArrayList<String?>()
        val imageClickActions = ArrayList<String?>()
        val itemData: MutableMap<String, ArrayList<String?>> = HashMap()
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
            imageClickActions.add(interactionUri)
            setRemoteViewClickAction(
                context,
                trackerActivity,
                carouselItem,
                R.id.carousel_item_image_view,
                messageId,
                deliveryId,
                interactionUri,
                tag,
                autoCancel ?: true
            )

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
