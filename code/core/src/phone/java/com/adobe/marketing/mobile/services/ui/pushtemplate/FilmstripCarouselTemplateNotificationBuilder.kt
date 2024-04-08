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
import com.adobe.marketing.mobile.util.StringUtils
import com.google.android.gms.common.util.CollectionUtils

/**
 * Class responsible for constructing a [NotificationCompat.Builder] object containing a manual filmstrip carousel template notification.
 */
internal class FilmstripCarouselTemplateNotificationBuilder :
    TemplateNotificationBuilder() {
    companion object {
        private const val SELF_TAG = "FilmstripCarouselTemplateNotificationBuilder"

        private fun construct(
            context: Context,
            intent: Intent?,
            pushTemplate: CarouselPushTemplate?,
            trackerActivity: Activity?,
            broadcastReceiver: BroadcastReceiver?
        ): NotificationCompat.Builder {
            if (pushTemplate == null && intent == null) {
                throw NotificationConstructionFailedException(
                    "push template and intent are null, cannot build a notification."
                )
            }

            return if (pushTemplate != null) {
                construct(
                    context,
                    trackerActivity,
                    broadcastReceiver,
                    pushTemplate
                )
            } else {
                construct(
                    context,
                    trackerActivity,
                    broadcastReceiver,
                    intent!!
                )
            }
        }

        @Throws(NotificationConstructionFailedException::class)
        private fun construct(
            context: Context,
            trackerActivity: Activity?,
            broadcastReceiver: BroadcastReceiver?,
            pushTemplate: CarouselPushTemplate
        ): NotificationCompat.Builder {
            val packageName = context.packageName
            val smallLayout = RemoteViews(packageName, R.layout.push_template_collapsed)
            val expandedLayout = RemoteViews(packageName, R.layout.push_template_filmstrip_carousel)
            val cacheService = ServiceProvider.getInstance().cacheService
                ?: throw NotificationConstructionFailedException(
                    (
                        "Cache service is null, filmstrip carousel notification will not be" +
                            " constructed."
                        )
                )

            // download the carousel images and populate the image uri, image caption, and image click
            // action arrays
            val centerImageIndex: Int =
                PushTemplateConstants.DefaultValues.FILMSTRIP_CAROUSEL_CENTER_INDEX // center index defaults to 1
            val imageProcessingStartTime = System.currentTimeMillis()
            val items: List<CarouselPushTemplate.CarouselItem> = pushTemplate.getCarouselItems()
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
            if ((
                downloadedImageUris.size
                    < PushTemplateConstants.DefaultValues.CAROUSEL_MINIMUM_IMAGE_COUNT
                )
            ) {
                return fallbackToBasicNotification(
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

            // get all captions present then set center caption text
            val centerCaptionText = imageCaptions[centerImageIndex]
            expandedLayout.setTextViewText(
                R.id.manual_carousel_filmstrip_caption,
                centerCaptionText
            )

            // set the downloaded bitmaps in the filmstrip image views
            expandedLayout.setImageViewBitmap(
                R.id.manual_carousel_filmstrip_left, downloadedImages[0]
            )
            expandedLayout.setImageViewBitmap(
                R.id.manual_carousel_filmstrip_center, downloadedImages[1]
            )
            expandedLayout.setImageViewBitmap(
                R.id.manual_carousel_filmstrip_right, downloadedImages[2]
            )

            // assign a click action pending intent to the center image view
            val fallbackActionUri: String? = pushTemplate.getActionUri()
            val interactionUri = if (!StringUtils.isNullOrEmpty(
                    imageClickActions[centerImageIndex]
                )
            ) imageClickActions[centerImageIndex] else fallbackActionUri
            trackerActivity?.let {
                setRemoteViewClickAction(
                    context,
                    trackerActivity,
                    expandedLayout,
                    R.id.manual_carousel_filmstrip_center,
                    pushTemplate.getMessageId(),
                    pushTemplate.getDeliveryId(),
                    interactionUri,
                    pushTemplate.getTag(),
                    pushTemplate.getStickyStatus() ?: false
                )
            }

            // set any custom colors if needed
            setCustomNotificationColors(
                pushTemplate.getNotificationBackgroundColor(),
                pushTemplate.getTitleTextColor(),
                pushTemplate.getExpandedBodyTextColor(),
                smallLayout,
                expandedLayout,
                R.id.carousel_container_layout
            )

            val channelId = createChannelAndGetChannelID(
                context,
                pushTemplate.getChannelId(),
                pushTemplate.getSound(),
                pushTemplate.getNotificationImportance()
            )

            // handle left and right navigation buttons
            broadcastReceiver?.let {
                val clickIntent = Intent(
                    PushTemplateConstants.IntentActions.FILMSTRIP_LEFT_CLICKED,
                    null,
                    context,
                    broadcastReceiver::class.java
                )
                clickIntent.setClass(context, broadcastReceiver::class.java)
                clickIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                clickIntent.putExtra(PushTemplateConstants.IntentKeys.TYPE, pushTemplate.getTemplateType()?.value)
                clickIntent.putExtra(PushTemplateConstants.IntentKeys.CHANNEL_ID, channelId)
                clickIntent.putExtra(
                    PushTemplateConstants.IntentKeys.CUSTOM_SOUND, pushTemplate.getSound()
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
                clickIntent.putExtra(
                    PushTemplateConstants.IntentKeys.EXPANDED_BODY_TEXT,
                    expandedBodyText
                )
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
                    PushTemplateConstants.IntentKeys.LARGE_ICON, pushTemplate.getLargeIcon()
                )
                clickIntent.putExtra(
                    PushTemplateConstants.IntentKeys.SMALL_ICON_COLOR,
                    pushTemplate.getSmallIconColor()
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
                    PushTemplateConstants.IntentKeys.TICKER, pushTemplate.getTicker()
                )
                clickIntent.putExtra(
                    PushTemplateConstants.IntentKeys.TAG, pushTemplate.getTag()
                )
                clickIntent.putExtra(
                    PushTemplateConstants.IntentKeys.STICKY, pushTemplate.getStickyStatus()
                )
                clickIntent.putExtra(PushTemplateConstants.IntentKeys.ACTION_URI, fallbackActionUri)
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
                expandedLayout.setOnClickPendingIntent(
                    R.id.leftImageButton,
                    pendingIntentLeftButton
                )
                expandedLayout.setOnClickPendingIntent(
                    R.id.rightImageButton,
                    pendingIntentRightButton
                )
            }

            // Create the notification
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

            // set notification delete action
            setNotificationDeleteAction(
                context,
                trackerActivity,
                builder,
                pushTemplate.getMessageId(),
                pushTemplate.getDeliveryId()
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
        private fun construct(
            context: Context,
            trackerActivity: Activity?,
            broadcastReceiver: BroadcastReceiver?,
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
                        "Cache service is null, filmstrip carousel notification will not be" +
                            " constructed."
                        )
                )
            val assetCacheLocation = assetCacheLocation
                ?: throw NotificationConstructionFailedException(
                    (
                        "Asset cache location is null, filmstrip carousel notification will not be" +
                            " constructed."
                        )
                )
            val packageName = ServiceProvider.getInstance()
                .appContextService
                .getApplication()
                ?.packageName

            // get filmstrip notification values from the intent extras
            val templateType = intentExtras.getString(PushTemplateConstants.IntentKeys.TYPE)
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
            val tag = intentExtras.getString(PushTemplateConstants.IntentKeys.TAG) as String
            val sticky = intentExtras.getBoolean(PushTemplateConstants.IntentKeys.STICKY)
            val fallbackActionUri =
                intentExtras.getString(PushTemplateConstants.IntentKeys.ACTION_URI)

            // as we are handling an intent, the image URLS should already be cached
            if (imageUrls != null && !CollectionUtils.isEmpty(imageUrls)) {
                for (imageUri: String? in imageUrls) {
                    imageUri?.let {
                        val cacheResult = cacheService.get(assetCacheLocation, imageUri)
                        cacheResult.let {
                            cachedImages.add(BitmapFactory.decodeStream(cacheResult?.getData()))
                        }
                    }
                }
            }
            val smallLayout = RemoteViews(packageName, R.layout.push_template_collapsed)
            val expandedLayout = RemoteViews(packageName, R.layout.push_template_filmstrip_carousel)
            smallLayout.setTextViewText(R.id.notification_title, titleText)
            smallLayout.setTextViewText(R.id.notification_body, bodyText)
            expandedLayout.setTextViewText(R.id.notification_title, titleText)
            expandedLayout.setTextViewText(R.id.notification_body_expanded, expandedBodyText)
            val action = intent.action
            val centerImageIndex =
                intentExtras.getInt(PushTemplateConstants.IntentKeys.CENTER_IMAGE_INDEX)
            val newIndices: List<Int>? =
                calculateNewIndices(centerImageIndex, imageUrls?.size, action)
            val newCenterCaption: String?
            val newLeftImage: Bitmap
            val newCenterImage: Bitmap
            val newRightImage: Bitmap
            val newCenterIndex: Int
            val newLeftIndex: Int
            val newRightIndex: Int
            if (newIndices == null) {
                Log.trace(
                    PushTemplateConstants.LOG_TAG,
                    SELF_TAG,
                    (
                        "Unable to calculate new left, center, and right indices. Using default center" +
                            " image index of 1."
                        )
                )
                newCenterIndex = PushTemplateConstants.DefaultValues.FILMSTRIP_CAROUSEL_CENTER_INDEX
                newLeftImage = cachedImages[
                    (
                        PushTemplateConstants.DefaultValues.FILMSTRIP_CAROUSEL_CENTER_INDEX -
                            1
                        )
                ]
                newCenterImage =
                    cachedImages[PushTemplateConstants.DefaultValues.FILMSTRIP_CAROUSEL_CENTER_INDEX]
                newRightImage = cachedImages[
                    (
                        PushTemplateConstants.DefaultValues.FILMSTRIP_CAROUSEL_CENTER_INDEX +
                            1
                        )
                ]
                newCenterCaption =
                    imageCaptions?.get(PushTemplateConstants.DefaultValues.FILMSTRIP_CAROUSEL_CENTER_INDEX)
            } else {
                newLeftIndex = newIndices[0]
                newCenterIndex = newIndices[1]
                newRightIndex = newIndices[2]
                newCenterImage = cachedImages[newCenterIndex]
                newLeftImage = cachedImages[newLeftIndex]
                newRightImage = cachedImages[newRightIndex]
                newCenterCaption = imageCaptions?.get(newCenterIndex)
            }
            expandedLayout.setImageViewBitmap(R.id.manual_carousel_filmstrip_center, newCenterImage)
            expandedLayout.setImageViewBitmap(R.id.manual_carousel_filmstrip_left, newLeftImage)
            expandedLayout.setImageViewBitmap(R.id.manual_carousel_filmstrip_right, newRightImage)
            expandedLayout.setTextViewText(R.id.manual_carousel_filmstrip_caption, newCenterCaption)

            // assign a click action pending intent to the center image view
            val interactionUri = imageClickActions?.get(newCenterIndex) ?: fallbackActionUri
            trackerActivity?.let {
                setRemoteViewClickAction(
                    context,
                    trackerActivity,
                    expandedLayout,
                    R.id.manual_carousel_filmstrip_center,
                    messageId,
                    deliveryId,
                    interactionUri,
                    tag,
                    sticky
                )
            }

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
            broadcastReceiver?.let {
                val clickIntent = Intent(
                    PushTemplateConstants.IntentActions.FILMSTRIP_LEFT_CLICKED,
                    null,
                    context,
                    broadcastReceiver::class.java
                )
                clickIntent.setClass(context, broadcastReceiver::class.java)
                clickIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                clickIntent.putExtra(PushTemplateConstants.IntentKeys.TYPE, templateType)
                clickIntent.putExtra(PushTemplateConstants.IntentKeys.CHANNEL_ID, channelId)
                clickIntent.putExtra(PushTemplateConstants.IntentKeys.CUSTOM_SOUND, customSound)
                clickIntent.putExtra(
                    PushTemplateConstants.IntentKeys.CENTER_IMAGE_INDEX,
                    newCenterIndex
                )
                clickIntent.putExtra(PushTemplateConstants.IntentKeys.IMAGE_URLS, imageUrls)
                clickIntent.putExtra(PushTemplateConstants.IntentKeys.IMAGE_CAPTIONS, imageCaptions)
                clickIntent.putExtra(
                    PushTemplateConstants.IntentKeys.IMAGE_CLICK_ACTIONS, imageClickActions
                )
                clickIntent.putExtra(PushTemplateConstants.IntentKeys.TITLE_TEXT, titleText)
                clickIntent.putExtra(PushTemplateConstants.IntentKeys.BODY_TEXT, bodyText)
                clickIntent.putExtra(
                    PushTemplateConstants.IntentKeys.EXPANDED_BODY_TEXT,
                    expandedBodyText
                )
                clickIntent.putExtra(
                    PushTemplateConstants.IntentKeys.NOTIFICATION_BACKGROUND_COLOR,
                    notificationBackgroundColor
                )
                clickIntent.putExtra(
                    PushTemplateConstants.IntentKeys.TITLE_TEXT_COLOR,
                    titleTextColor
                )
                clickIntent.putExtra(
                    PushTemplateConstants.IntentKeys.EXPANDED_BODY_TEXT_COLOR, expandedBodyTextColor
                )
                clickIntent.putExtra(PushTemplateConstants.IntentKeys.MESSAGE_ID, messageId)
                clickIntent.putExtra(PushTemplateConstants.IntentKeys.DELIVERY_ID, deliveryId)
                clickIntent.putExtra(PushTemplateConstants.IntentKeys.SMALL_ICON, smallIcon)
                clickIntent.putExtra(
                    PushTemplateConstants.IntentKeys.SMALL_ICON_COLOR,
                    smallIconColor
                )
                clickIntent.putExtra(PushTemplateConstants.IntentKeys.LARGE_ICON, largeIcon)
                clickIntent.putExtra(PushTemplateConstants.IntentKeys.VISIBILITY, visibility)
                clickIntent.putExtra(PushTemplateConstants.IntentKeys.IMPORTANCE, importance)
                clickIntent.putExtra(PushTemplateConstants.IntentKeys.TICKER, ticker)
                clickIntent.putExtra(PushTemplateConstants.IntentKeys.TAG, tag)
                clickIntent.putExtra(PushTemplateConstants.IntentKeys.STICKY, sticky)
                clickIntent.putExtra(PushTemplateConstants.IntentKeys.ACTION_URI, fallbackActionUri)
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
                expandedLayout.setOnClickPendingIntent(
                    R.id.leftImageButton,
                    pendingIntentLeftButton
                )
                expandedLayout.setOnClickPendingIntent(
                    R.id.rightImageButton,
                    pendingIntentRightButton
                )
            }

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
            setVisibility(builder, visibility)

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
    }

    override fun build(
        context: Context
    ): NotificationCompat.Builder {
        return construct(
            context,
            intent,
            pushTemplate as? CarouselPushTemplate,
            trackerActivity,
            broadcastReceiver
        )
    }
}
