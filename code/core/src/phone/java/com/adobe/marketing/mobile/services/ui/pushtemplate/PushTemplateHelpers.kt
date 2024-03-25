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
import android.app.TaskStackBuilder
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.RectF
import android.media.RingtoneManager
import android.net.Uri
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.adobe.marketing.mobile.core.R
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.caching.CacheEntry
import com.adobe.marketing.mobile.services.caching.CacheExpiry
import com.adobe.marketing.mobile.services.caching.CacheService
import com.adobe.marketing.mobile.util.StringUtils
import com.adobe.marketing.mobile.util.UrlUtils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Random
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * General utility functions to assist in building push template notifications.
 */
private const val SELF_TAG = "PushTemplateHelpers"
private const val FULL_BITMAP_QUALITY = 100
private const val DOWNLOAD_TIMEOUT = 10
private const val MINIMUM_FILMSTRIP_SIZE = 3

/**
 * Asset cache location to use for downloaded push template images.
 *
 * @return [String] containing the asset cache location to use for storing downloaded push template images.
 */
internal val assetCacheLocation: String?
    get() {
        val deviceInfoService = ServiceProvider.getInstance().deviceInfoService
            ?: return null
        val applicationCacheDir = deviceInfoService.applicationCacheDir
        return if ((applicationCacheDir == null)) null else (
            (
                applicationCacheDir
                    .toString() + File.separator +
                    PushTemplateConstants.CACHE_BASE_DIR
                ) + File.separator +
                PushTemplateConstants.PUSH_IMAGE_CACHE
            )
    }

private val executor: ExecutorService
    get() = ExecutorHolder.INSTANCE

private object ExecutorHolder {
    val INSTANCE: ExecutorService = Executors.newSingleThreadExecutor()
}

private class DownloadImageCallable(val url: String?) :
    Callable<Bitmap?> {
    override fun call(): Bitmap? {
        val bitmap: Bitmap?
        var connection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        try {
            val imageUrl = URL(url)
            connection = imageUrl.openConnection() as HttpURLConnection
            inputStream = connection.inputStream
            bitmap = BitmapFactory.decodeStream(inputStream)
        } catch (e: IOException) {
            Log.warning(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                "Failed to download push notification image from url (%s). Exception: %s",
                url,
                e.message
            )
            return null
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close()
                } catch (e: IOException) {
                    Log.warning(
                        PushTemplateConstants.LOG_TAG,
                        SELF_TAG,
                        (
                            "IOException during closing Input stream while push notification" +
                                " image from url (%s). Exception: %s "
                            ),
                        url,
                        e.message
                    )
                }
            }
            connection?.disconnect()
        }
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Downloaded push notification image from url (%s)",
            url
        )
        return bitmap
    }
}

/**
 * Downloads an image using the provided uri `String`. Prior to downloading, the image uri
 * is used to retrieve a [CacheResult] containing a previously cached image. If no cache
 * result is returned, a call to [download] is made to download then cache the image.
 *
 * If a valid cache result is returned then no image is downloaded. Instead, a `Bitmap`
 * is created from the cache result and returned by this method.
 *
 * @param cacheService the AEPSDK [CacheService] to use for caching or retrieving
 * downloaded image assets
 * @param uri [String] containing an image asset url
 * @return [Bitmap] containing the image referenced by the `String` uri
 */
internal fun downloadImage(cacheService: CacheService, uri: String?): Bitmap? {
    if (assetCacheLocation.isNullOrEmpty() || uri.isNullOrEmpty()) {
        return null
    }
    val cacheResult = cacheService[assetCacheLocation!!, uri]
    if (cacheResult != null) {
        Log.trace(PushTemplateConstants.LOG_TAG, SELF_TAG, "Found cached image for %s.", uri)
        return BitmapFactory.decodeStream(cacheResult.data)
    }
    if (!UrlUtils.isValidUrl(uri)) {
        return null
    }
    val image = download(uri) ?: return null
    Log.trace(
        PushTemplateConstants.LOG_TAG,
        SELF_TAG,
        "Successfully download image from %s",
        uri
    )
    // scale down the bitmap to 300dp x 200dp as we don't want to use a full
    // size image due to memory constraints
    val pushImage = scaleBitmap(image)
    // write bitmap to cache
    try {
        bitmapToInputStream(pushImage).use { bitmapInputStream ->
            cacheBitmapInputStream(
                cacheService,
                bitmapInputStream,
                uri
            )
        }
    } catch (exception: IOException) {
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Exception occurred creating an input stream from a" + " bitmap: %s.",
            exception.localizedMessage
        )
    }
    return pushImage
}

/**
 * Downloads an image using the provided uri `String`. A [Future] task is created to download
 * the image using a [DownloadImageCallable]. The task is submitted to an [ExecutorService].
 * @param url [String] containing the image url to download
 * @return [Bitmap] containing the downloaded image
 */
internal fun download(url: String?): Bitmap? {
    var bitmap: Bitmap? = null
    val executorService = executor
    val downloadTask = executorService.submit(DownloadImageCallable(url))
    try {
        bitmap = downloadTask[DOWNLOAD_TIMEOUT.toLong(), TimeUnit.SECONDS]
    } catch (e: Exception) {
        downloadTask.cancel(true)
    }
    return bitmap
}

/**
 * Calculates a new left, center, and right index given the current center index, total number
 * of images, and the intent action.
 *
 * @param centerIndex [Int] containing the current center image index
 * @param listSize `Int` containing the total number of images
 * @param action [String] containing the action found in the broadcast [Intent]
 * @return [List] containing the new calculated left, center, and right indices
 */
internal fun calculateNewIndices(
    centerIndex: Int,
    listSize: Int?,
    action: String?
): List<Int>? {
    if (listSize == null || listSize < MINIMUM_FILMSTRIP_SIZE) return null
    val newIndices: MutableList<Int> = ArrayList()
    var newCenterIndex = 0
    var newLeftIndex = 0
    var newRightIndex = 0
    Log.trace(
        PushTemplateConstants.LOG_TAG,
        SELF_TAG,
        "Current center index is %d and list size is %d.",
        centerIndex,
        listSize
    )
    if ((
        (action == PushTemplateConstants.IntentActions.FILMSTRIP_LEFT_CLICKED) || (
            action ==
                PushTemplateConstants.IntentActions.MANUAL_CAROUSEL_LEFT_CLICKED
            )
        )
    ) {
        newCenterIndex = (centerIndex - 1 + listSize) % listSize
        newLeftIndex = (newCenterIndex - 1 + listSize) % listSize
        newRightIndex = centerIndex
    } else if ((
        (action == PushTemplateConstants.IntentActions.FILMSTRIP_RIGHT_CLICKED) || (
            action ==
                PushTemplateConstants.IntentActions.MANUAL_CAROUSEL_RIGHT_CLICKED
            )
        )
    ) {
        newCenterIndex = (centerIndex + 1) % listSize
        newLeftIndex = centerIndex
        newRightIndex = (newCenterIndex + 1) % listSize
    }
    newIndices.add(newLeftIndex)
    newIndices.add(newCenterIndex)
    newIndices.add(newRightIndex)
    Log.trace(
        PushTemplateConstants.LOG_TAG,
        SELF_TAG,
        (
            "Calculated new indices. New center index is %d, new left index is %d, and new" +
                " right index is %d."
            ),
        newCenterIndex,
        newLeftIndex,
        newRightIndex
    )
    return newIndices
}

/**
 * Converts a [Bitmap] into an [InputStream] to be used in caching images.
 *
 * @param bitmap [Bitmap] to be converted into an [InputStream]
 * @return an `InputStream` created from the provided bitmap
 */
private fun bitmapToInputStream(bitmap: Bitmap): InputStream {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, FULL_BITMAP_QUALITY, byteArrayOutputStream)
    val bitmapData = byteArrayOutputStream.toByteArray()
    return ByteArrayInputStream(bitmapData)
}

/**
 * Writes the provided [InputStream] to the downloaded push template image [assetCacheLocation].
 *
 * @param cacheService [CacheService] the AEPSDK cache service
 * @param bitmapInputStream [InputStream] created from a download [Bitmap]
 * @param imageUri [String] containing the image uri to be used a cache key
 */
private fun cacheBitmapInputStream(
    cacheService: CacheService,
    bitmapInputStream: InputStream,
    imageUri: String
) {
    Log.trace(
        PushTemplateConstants.LOG_TAG,
        SELF_TAG,
        "Caching image downloaded from %s.",
        imageUri
    )
    assetCacheLocation?.let {
        // cache push notification images for 3 days
        val cacheEntry = CacheEntry(
            bitmapInputStream,
            CacheExpiry.after(
                PushTemplateConstants.DefaultValues.PUSH_NOTIFICATION_IMAGE_CACHE_EXPIRY_IN_MILLISECONDS
            ),
            null
        )
        cacheService[it, imageUri] = cacheEntry
    }
}

/**
 * Scales a downloaded [Bitmap] to a maximum width and height of 300dp x 200dp.
 * The scaling is done using a [Matrix] object to maintain the aspect ratio of the original
 * image.
 *
 * @param downloadedBitmap [Bitmap] to be scaled
 * @return [Bitmap] containing the scaled image
 */
private fun scaleBitmap(downloadedBitmap: Bitmap): Bitmap {
    val matrix = Matrix()
    matrix.setRectToRect(
        RectF(0f, 0f, downloadedBitmap.width.toFloat(), downloadedBitmap.height.toFloat()),
        RectF(
            0f,
            0f,
            PushTemplateConstants.DefaultValues.CAROUSEL_MAX_BITMAP_WIDTH.toFloat(),
            PushTemplateConstants.DefaultValues.CAROUSEL_MAX_BITMAP_HEIGHT.toFloat()
        ),
        Matrix.ScaleToFit.CENTER
    )
    return Bitmap.createBitmap(
        downloadedBitmap,
        0,
        0,
        downloadedBitmap.width,
        downloadedBitmap.height,
        matrix,
        true
    )
}

/**
 * Creates a pending intent for a notification.
 *
 * @param context the application [Context]
 * @param messageId [String] containing the message id from the received push notification
 * @param deliveryId `String` containing the delivery id from the received push
 * notification
 * @param actionUri the action uri
 * @param actionID the action ID
 * @param stickyNotification [Boolean] if false, remove the notification after it is interacted with
 * @return the created [PendingIntent]
 */
private fun createPendingIntent(
    context: Context,
    trackerActivity: Activity,
    messageId: String,
    deliveryId: String,
    actionUri: String?,
    actionID: String?,
    tag: String,
    stickyNotification: Boolean
): PendingIntent {
    val intent = Intent(PushTemplateConstants.NotificationAction.BUTTON_CLICKED)
    intent.setClass(context.applicationContext, trackerActivity::class.java)
    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    intent.putExtra(PushTemplateConstants.Tracking.Keys.MESSAGE_ID, messageId)
    intent.putExtra(PushTemplateConstants.Tracking.Keys.DELIVERY_ID, deliveryId)
    intent.putExtra(PushTemplateConstants.PushPayloadKeys.TAG, tag)
    intent.putExtra(PushTemplateConstants.PushPayloadKeys.STICKY, stickyNotification)
    addActionDetailsToIntent(
        intent,
        actionUri,
        actionID
    )

    // adding tracking details
    return TaskStackBuilder.create(context)
        .addNextIntentWithParentStack(intent)
        .getPendingIntent(
            Random().nextInt(),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
}

/**
 * Adds action details to the provided [Intent].
 *
 * @param intent the intent
 * @param actionUri [String] containing the action uri
 * @param actionId `String` containing the action ID
 */
private fun addActionDetailsToIntent(
    intent: Intent,
    actionUri: String?,
    actionId: String?
) {
    if (!StringUtils.isNullOrEmpty(actionUri)) {
        intent.putExtra(PushTemplateConstants.Tracking.Keys.ACTION_URI, actionUri)
    }
    if (!StringUtils.isNullOrEmpty(actionId)) {
        intent.putExtra(PushTemplateConstants.Tracking.Keys.ACTION_ID, actionId)
    }
}

/**
 * Sets the sound for the legacy style notification. If a sound is received from the payload, the same is
 * used. If a sound is not received from the payload, the default sound is used.
 *
 * @param context the application [Context]
 * @param notificationBuilder the [NotificationCompat.Builder]
 * @param customSound [String] containing the custom sound file name to load from the
 * bundled assets
 */
internal fun setSound(
    context: Context,
    notificationBuilder: NotificationCompat.Builder,
    customSound: String?
) {
    if (StringUtils.isNullOrEmpty(customSound)) {
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            (
                "No custom sound found in the push template, using the default notification" +
                    " sound."
                )
        )
        notificationBuilder.setSound(
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        )
        return
    }
    Log.trace(
        PushTemplateConstants.LOG_TAG,
        SELF_TAG,
        "Setting sound from bundle named %s.",
        customSound
    )
    notificationBuilder.setSound(
        getSoundUriForResourceName(customSound, context)
    )
}

/**
 * Returns the Uri for the sound file with the given name. The sound file must be in the res/raw
 * directory. The sound file should be in format of .mp3, .wav, or .ogg
 *
 * @param soundName [String] containing the name of the sound file
 * @param context the application [Context]
 * @return the [Uri] for the sound file with the given name
 */
internal fun getSoundUriForResourceName(
    soundName: String?,
    context: Context
): Uri {
    return Uri.parse(
        ContentResolver.SCHEME_ANDROID_RESOURCE +
            "://" +
            context.packageName +
            "/raw/" +
            soundName
    )
}

/**
 * Sets the provided image url as the large icon for the legacy style notification. If a large icon url is received
 * from the payload, the image is downloaded and the notification style is set to
 * BigPictureStyle. If large icon url is not received from the payload, default style is used
 * for the notification.
 *
 * @param notificationBuilder the [NotificationCompat.Builder]
 * @param imageUrl [String] containing the image url
 * @param title `String` containing the title
 * @param bodyText `String` containing the body text
 */
internal fun setLargeIcon(
    notificationBuilder: NotificationCompat.Builder,
    imageUrl: String?,
    title: String?,
    bodyText: String?
) {
    // Quick bail out if there is no image url
    if (StringUtils.isNullOrEmpty(imageUrl)) return
    val bitmap: Bitmap = download(imageUrl) ?: return

    // Bail out if the download fails
    notificationBuilder.setLargeIcon(bitmap)
    val bigPictureStyle = NotificationCompat.BigPictureStyle()
    bigPictureStyle.bigPicture(bitmap)
    bigPictureStyle.bigLargeIcon(null)
    bigPictureStyle.setBigContentTitle(title)
    bigPictureStyle.setSummaryText(bodyText)
    notificationBuilder.setStyle(bigPictureStyle)
}

internal fun setVisibility(
    notificationBuilder: NotificationCompat.Builder,
    visibility: Int
) {
    when (visibility) {
        NotificationCompat.VISIBILITY_PUBLIC -> notificationBuilder.setVisibility(
            NotificationCompat.VISIBILITY_PUBLIC
        )

        NotificationCompat.VISIBILITY_PRIVATE -> notificationBuilder.setVisibility(
            NotificationCompat.VISIBILITY_PRIVATE
        )

        NotificationCompat.VISIBILITY_SECRET -> notificationBuilder.setVisibility(
            NotificationCompat.VISIBILITY_SECRET
        )

        else -> {
            notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            Log.debug(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                (
                    "Invalid visibility value received from the payload. Using the default" +
                        " visibility value."
                    )
            )
        }
    }
}

/**
 * Sets custom colors to UI elements present in the specified [RemoteViews] object.
 *
 * @param backgroundColor [String] containing the hex color code for the notification
 * background
 * @param titleTextColor `String` containing the hex color code for the notification title
 * text
 * @param expandedBodyTextColor `String` containing the hex color code for the expanded
 * notification body text
 * @param smallLayout [RemoteViews] object for a collapsed custom notification
 * @param expandedLayout `RemoteViews` object for an expanded custom notification
 * @param containerViewId [Int] containing the resource id of the layout container
 */
internal fun setCustomNotificationColors(
    backgroundColor: String?,
    titleTextColor: String?,
    expandedBodyTextColor: String?,
    smallLayout: RemoteViews,
    expandedLayout: RemoteViews,
    containerViewId: Int
) {
    // get custom color from hex string and set it the notification background
    if (!backgroundColor.isNullOrEmpty()) {
        setElementColor(
            smallLayout,
            R.id.basic_small_layout,
            "#$backgroundColor",
            PushTemplateConstants.MethodNames.SET_BACKGROUND_COLOR,
            PushTemplateConstants.FriendlyViewNames.NOTIFICATION_BACKGROUND
        )
        setElementColor(
            expandedLayout,
            containerViewId,
            "#$backgroundColor",
            PushTemplateConstants.MethodNames.SET_BACKGROUND_COLOR,
            PushTemplateConstants.FriendlyViewNames.NOTIFICATION_BACKGROUND
        )
    }

    // get custom color from hex string and set it the notification title
    if (!titleTextColor.isNullOrEmpty()) {
        setElementColor(
            smallLayout,
            R.id.notification_title,
            "#$titleTextColor",
            PushTemplateConstants.MethodNames.SET_TEXT_COLOR,
            PushTemplateConstants.FriendlyViewNames.NOTIFICATION_TITLE
        )
        setElementColor(
            expandedLayout,
            R.id.notification_title,
            "#$titleTextColor",
            PushTemplateConstants.MethodNames.SET_TEXT_COLOR,
            PushTemplateConstants.FriendlyViewNames.NOTIFICATION_TITLE
        )
    }

    // get custom color from hex string and set it the notification body text
    if (!expandedBodyTextColor.isNullOrEmpty()) {
        setElementColor(
            smallLayout,
            R.id.notification_body,
            "#$expandedBodyTextColor",
            PushTemplateConstants.MethodNames.SET_TEXT_COLOR,
            PushTemplateConstants.FriendlyViewNames.NOTIFICATION_BODY_TEXT
        )
        setElementColor(
            expandedLayout,
            R.id.notification_body_expanded,
            "#$expandedBodyTextColor",
            PushTemplateConstants.MethodNames.SET_TEXT_COLOR,
            PushTemplateConstants.FriendlyViewNames.NOTIFICATION_BODY_TEXT
        )
    }
}

/**
 * Sets a provided color hex string to a UI element contained in a specified [RemoteViews]
 * view.
 *
 * @param remoteView `RemoteViews` object containing a UI element to be updated
 * @param elementId [Int] containing the resource id of the UI element
 * @param colorHex [String] containing the color hex string
 * @param methodName `String` containing the method to be called on the UI element to
 * update the color
 * @param viewFriendlyName `String` containing the friendly name of the view to be used
 * for logging purposes
 */
private fun setElementColor(
    remoteView: RemoteViews,
    elementId: Int,
    colorHex: String,
    methodName: String,
    viewFriendlyName: String
) {
    if (StringUtils.isNullOrEmpty(methodName)) {
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            (
                "Null or empty method name provided, custom color will not" +
                    " be applied to" +
                    viewFriendlyName
                )
        )
        return
    }
    try {
        if (!StringUtils.isNullOrEmpty(colorHex)) {
            remoteView.setInt(elementId, methodName, Color.parseColor(colorHex))
        }
    } catch (exception: IllegalArgumentException) {
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            (
                "Unrecognized hex string passed to Color.parseColor(), custom color will not" +
                    " be applied to" +
                    viewFriendlyName
                )
        )
    }
}

/**
 * Sets the click action for the specified view in the custom push template [RemoteViews].
 *
 * @param context the application [Context]
 * @param trackerActivity the [Activity] to set in the created pending intent for tracking purposes
 * @param pushTemplateRemoteView `RemoteViews` the parent view representing a push
 * template notification
 * @param targetViewResourceId [Int] containing the resource id of the view to attach the
 * click action
 * @param messageId [String] containing the message id from the received push notification
 * @param deliveryId `String` containing the delivery id from the received push
 * notification
 * @param actionUri `String` containing the action uri defined for the push template image
 * @param tag `String` containing the tag to use when scheduling the notification
 * @param stickyNotification [Boolean] if false, remove the [NotificationCompat] after the `RemoteViews` is pressed
 */
internal fun setRemoteViewClickAction(
    context: Context,
    trackerActivity: Activity,
    pushTemplateRemoteView: RemoteViews,
    targetViewResourceId: Int,
    messageId: String,
    deliveryId: String,
    actionUri: String?,
    tag: String,
    stickyNotification: Boolean
) {
    if (actionUri.isNullOrEmpty()) {
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "No valid action uri found for the clicked view with id %s. No click action" +
                " will be assigned.",
            targetViewResourceId
        )
        return
    }
    Log.trace(
        PushTemplateConstants.LOG_TAG,
        SELF_TAG,
        "Setting remote view click action uri: %s ",
        actionUri
    )

    val pendingIntent: PendingIntent =
        createPendingIntent(
            context,
            trackerActivity,
            messageId,
            deliveryId,
            actionUri,
            null,
            tag,
            stickyNotification
        )
    pushTemplateRemoteView.setOnClickPendingIntent(targetViewResourceId, pendingIntent)
}

/**
 * Adds action buttons for the notification.
 *
 * @param context the application [Context]
 * @param trackerActivity the [Activity] to set in the created pending intent for tracking purposes
 * @param builder the [NotificationCompat.Builder] to attach the action buttons
 * @param actionButtonsString `String` a JSON string containing action buttons to attach
 * to the notification
 * @param messageId `String` containing the message id from the received push notification
 * @param deliveryId `String` containing the delivery id from the received push
 * notification
 * @param tag `String` containing the tag to use when scheduling the notification
 * @param stickyNotification [Boolean]  if false, remove the notification after the action
 * button is pressed
 */
internal fun addActionButtons(
    context: Context,
    trackerActivity: Activity,
    builder: NotificationCompat.Builder,
    actionButtonsString: String?,
    messageId: String,
    deliveryId: String,
    tag: String,
    stickyNotification: Boolean
) {
    val actionButtons: List<AEPPushTemplate.ActionButton>? =
        AEPPushTemplate.getActionButtonsFromString(actionButtonsString)
    if (actionButtons.isNullOrEmpty()) {
        return
    }
    for (eachButton in actionButtons) {
        val pendingIntent: PendingIntent =
            if (eachButton.type === AEPPushTemplate.ActionType.DEEPLINK ||
                eachButton.type === AEPPushTemplate.ActionType.WEBURL
            ) {
                createPendingIntent(
                    context,
                    trackerActivity,
                    messageId,
                    deliveryId,
                    eachButton.link,
                    eachButton.label,
                    tag,
                    stickyNotification
                )
            } else {
                createPendingIntent(
                    context,
                    trackerActivity,
                    messageId,
                    deliveryId,
                    null,
                    eachButton.label,
                    tag,
                    stickyNotification
                )
            }
        builder.addAction(0, eachButton.label, pendingIntent)
    }
}

/**
 * Sets the click action for the notification.
 *
 * @param context the application [Context]
 * @param trackerActivity the [Activity] to set in the created pending intent for tracking purposes
 * @param notificationBuilder the [NotificationCompat.Builder] to attach the click action
 * @param messageId [String] containing the message id from the received push notification
 * @param deliveryId `String` containing the delivery id from the received push
 * notification
 * @param actionUri `String` containing the action uri
 * @param tag `String` containing the tag to use when scheduling the notification
 * @param stickyNotification `boolean` if false, remove the notification after the `RemoteViews` is pressed
 */
internal fun setNotificationClickAction(
    context: Context,
    trackerActivity: Activity,
    notificationBuilder: NotificationCompat.Builder,
    messageId: String,
    deliveryId: String,
    actionUri: String?,
    tag: String,
    stickyNotification: Boolean
) {
    val pendingIntent: PendingIntent =
        createPendingIntent(
            context,
            trackerActivity,
            messageId,
            deliveryId,
            actionUri,
            null,
            tag,
            stickyNotification
        )
    notificationBuilder.setContentIntent(pendingIntent)
}

/**
 * Sets the delete action for the notification.
 *
 * @param context the application [Context]
 * @param trackerActivity the [Activity] to set in the created pending intent for tracking purposes
 * @param builder the [NotificationCompat.Builder] to attach the delete action
 * @param messageId `String` containing the message id from the received push notification
 * @param deliveryId `String` containing the delivery id from the received push
 * notification
 */
internal fun setNotificationDeleteAction(
    context: Context,
    trackerActivity: Activity,
    builder: NotificationCompat.Builder,
    messageId: String,
    deliveryId: String
) {
    val deleteIntent = Intent(PushTemplateConstants.NotificationAction.DISMISSED)
    deleteIntent.setClass(context, trackerActivity::class.java)
    deleteIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    deleteIntent.putExtra(PushTemplateConstants.Tracking.Keys.MESSAGE_ID, messageId)
    deleteIntent.putExtra(PushTemplateConstants.Tracking.Keys.DELIVERY_ID, deliveryId)
    val intent = PendingIntent.getActivity(
        context,
        Random().nextInt(),
        deleteIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    builder.setDeleteIntent(intent)
}
